package com.ttn.community.journal.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.SocialException;
import com.adobe.cq.social.commons.comments.api.Comment;
import com.adobe.cq.social.commons.comments.api.CommentCollection;
import com.adobe.cq.social.commons.comments.api.CommentCollectionConfiguration;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentList;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProvider;
import com.adobe.cq.social.commons.listing.QueryFilterUtil;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.ugc.api.ComparisonType;
import com.adobe.cq.social.ugc.api.Constraint;
import com.adobe.cq.social.ugc.api.ConstraintGroup;
import com.adobe.cq.social.ugc.api.Operator;
import com.adobe.cq.social.ugc.api.PathConstraint;
import com.adobe.cq.social.ugc.api.PathConstraintType;
import com.adobe.cq.social.ugc.api.SearchResults;
import com.adobe.cq.social.ugc.api.UgcFilter;
import com.adobe.cq.social.ugc.api.UgcSearch;
import com.adobe.cq.social.ugc.api.UgcSort;
import com.adobe.cq.social.ugc.api.ValueConstraint;
import com.adobe.cq.social.ugcbase.SocialUtils;
import com.adobe.cq.social.ugcbase.core.SocialResourceUtils;

@Component
@Service
public class CustomJournalListProvider implements CommentSocialComponentListProvider {

	private static final Logger LOG = LoggerFactory.getLogger(CustomJournalListProvider.class);
	private static final String PROP_FILTER_NAME = "filter";

	public <C extends Comment, T extends CommentCollectionConfiguration> CommentSocialComponentList getCommentSocialComponentList(
			CommentCollection<C, T> commentCollection, QueryRequestInfo listInfo, ClientUtilities clientUtils) {
		SearchResults<Resource> hits = queryForJournalEntries(commentCollection.getResource(), listInfo, clientUtils);
		return new CustomJournalSocialComponentList(hits, clientUtils);
	}

	public <C extends Comment> CommentSocialComponentList getCommentSocialComponentList(C comment,
			QueryRequestInfo listInfo, ClientUtilities clientUtils) {
		SearchResults<Resource> hits = queryForJournalEntries(comment.getResource(), listInfo, clientUtils);
		return new CustomJournalSocialComponentList(hits, clientUtils);
	}

	public boolean checkResource(Resource commentCollectionResource, SocialUtils socialUtils) {
		return (SocialResourceUtils.isSocialResource(commentCollectionResource))
				|| (socialUtils.getStorageConfig(commentCollectionResource) != null);
	}

	public List<String> getSupportedResourceType() {
		return Arrays.asList(new String[] { "blog/components/hbs/journal" });
	}

	public boolean acceptQuery(QueryRequestInfo listInfo) {
		return listInfo.isQuery();
	}

	private SearchResults<Resource> queryForJournalEntries(Resource resource, QueryRequestInfo queryInfo,
			ClientUtilities clientUtils) {
		boolean isModerator = clientUtils.getSocialUtils().hasModeratePermissions(resource);
		ResourceResolver resolver = resource.getResourceResolver();
		UgcSearch ugcSearch = (UgcSearch) resolver.adaptTo(UgcSearch.class);
		UgcFilter filter = new UgcFilter();
		boolean addUserConstraint = false;
		String authorizableId = clientUtils.getAuthorizedUserId();
		Map<String, String[]> predicates = queryInfo.getPredicates();
		if (predicates != null) {
			String[] filters = (String[]) predicates.get("filter");
			if ((filters != null) && (filters.length > 0)) {
				for (String subFilter : filters) {
					String[] subFilters = subFilter.split("(?<!\\\\),");
					for (int i = 0; i < subFilters.length; i++) {
						try {
							QueryFilterUtil.QueryFilter queryExpression = QueryFilterUtil.QueryFilter
									.parse(subFilters[i], Operator.Or);
							String constraintGroupName = queryExpression.getName();
							if ((StringUtils.equals("isDraft", constraintGroupName))
									|| (StringUtils.equals("publishDate", constraintGroupName))) {
								if (!isModerator) {
									addUserConstraint = true;
								}
							}
						} catch (QueryFilterUtil.QueryFilterException e) {
							LOG.error(
									"Error parsing input while trying to determine is user constraint needs to be added",
									e);
						}
					}
				}
				List<ConstraintGroup> constraints = null;
				try {
					constraints = QueryFilterUtil.parseFilter(filters);
				} catch (QueryFilterUtil.QueryFilterException e) {
					LOG.error("Unable to search Journal enties: as the query couldnt be parsed, returning empty list",
							e);

					throw new SocialException(
							String.format("Could not parse the query %1$s", new Object[] { Arrays.toString(filters) }));
				}
				if ((constraints != null) && (!constraints.isEmpty())) {
					for (ConstraintGroup cg : constraints) {
						filter.and(cg);
					}
				}
			}
		}
		if ((addUserConstraint) && (StringUtils.isNotEmpty(authorizableId)) && (!clientUtils.userIsAnonymous())) {
			ConstraintGroup authorGroup = new ConstraintGroup(Operator.Or);
			Constraint privUserConstraint = new ValueConstraint("composedBy", authorizableId, ComparisonType.Equals,
					Operator.Or);

			Constraint userConstraint = new ValueConstraint("userIdentifier", authorizableId, ComparisonType.Equals,
					Operator.Or);

			authorGroup.addConstraint(userConstraint);
			authorGroup.addConstraint(privUserConstraint);
			filter.and(authorGroup);
		} else if (queryInfo.isQuery()) {
			if (!isModerator) {
				ConstraintGroup draftFilters = new ConstraintGroup(Operator.And);
				if ((!clientUtils.userIsAnonymous()) && (StringUtils.isNotEmpty(authorizableId))) {
					ConstraintGroup draftUserFilter = new ConstraintGroup(Operator.Or);
					Constraint userDraftConstraint = new ValueConstraint("isDraft", Boolean.TRUE, ComparisonType.Equals,
							Operator.And);

					ConstraintGroup draftAuthor = new ConstraintGroup(Operator.Or);
					Constraint userConstraint = new ValueConstraint("userIdentifier", authorizableId,
							ComparisonType.Equals, Operator.Or);

					Constraint privUserConstraint = new ValueConstraint("composedBy", authorizableId,
							ComparisonType.Equals, Operator.Or);

					draftAuthor.addConstraint(privUserConstraint);
					draftAuthor.addConstraint(userConstraint);
					draftUserFilter.addConstraint(draftAuthor);
					draftUserFilter.addConstraint(userDraftConstraint);

					draftFilters.addConstraint(draftUserFilter);
				}
				Constraint nonDraftConstraint = new ValueConstraint("isDraft", Boolean.TRUE, ComparisonType.NotEquals,
						Operator.Or);

				draftFilters.addConstraint(nonDraftConstraint);

				filter.and(draftFilters);
			}
		} else {
			Constraint nonDraftConstraint = new ValueConstraint("isDraft", Boolean.TRUE, ComparisonType.NotEquals,
					Operator.Or);

			filter.and(nonDraftConstraint);
		}
		ConstraintGroup constraintGroup = new ConstraintGroup();
		constraintGroup.and(new PathConstraint(clientUtils.getSocialUtils().resourceToUGCStoragePath(resource),
				PathConstraintType.IsDescendantNode));

		constraintGroup.and(new ValueConstraint("sling:resourceType", "blog/components/hbs/entry_topic"));

		filter.addSort(new UgcSort("publishDate", UgcSort.Direction.Desc));
		filter.addSort(new UgcSort("added", UgcSort.Direction.Desc));
		filter.addConstraint(constraintGroup);
		SearchResults<Resource> resourceSearchResults;
		try {
			resourceSearchResults = ugcSearch.find(null, resource.getResourceResolver(), filter,
					queryInfo.getPagination().getOffset(), queryInfo.getPagination().getSize(), true);
		} catch (RepositoryException e) {
			resourceSearchResults = getEmptySearchList();
			LOG.error("Unable to get Journal enties: ", e);
		}
		return resourceSearchResults;
	}

	private SearchResults<Resource> getEmptySearchList() {
		return new SearchResults<Resource>() {
			public long getTotalNumberOfResults() {
				return 0L;
			}

			public List<Resource> getResults() {
				return new ArrayList<Resource>();
			}
		};
	}
}
