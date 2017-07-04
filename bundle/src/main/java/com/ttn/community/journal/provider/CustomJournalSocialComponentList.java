package com.ttn.community.journal.provider;

import com.adobe.cq.social.commons.comments.api.Comment;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentList;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.CollectionPagination;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.QueryRequestInfo.QueryRequestInfoFactory;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.SocialComponentFactoryManager;
import com.adobe.cq.social.scf.core.CollectionSortedOrder;
import com.adobe.cq.social.ugc.api.PathConstraintType;
import com.adobe.cq.social.ugc.api.SearchResults;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomJournalSocialComponentList extends AbstractList<Object> implements CommentSocialComponentList {

	private static final Logger LOG = LoggerFactory.getLogger(CustomJournalSocialComponentList.class);
	private final List<Comment> entries;
	private final Long totalSize;
	protected PathConstraintType pathConstraintType;

	public CustomJournalSocialComponentList(SearchResults<Resource> results, ClientUtilities clientUtils) {
		this.totalSize = Long.valueOf(results.getTotalNumberOfResults());
		this.entries = new ArrayList();
		this.pathConstraintType = PathConstraintType.IsDescendantNode;
		for (Resource rstResource : results.getResults()) {
			SocialComponentFactory sc = clientUtils.getSocialComponentFactoryManager()
					.getSocialComponentFactory(rstResource);
			if (sc != null) {
				SocialComponent component = sc.getSocialComponent(rstResource, clientUtils,
						QueryRequestInfo.DEFAULT_QUERY_INFO_FACTORY.create());
				if (component != null) {
					this.entries.add((Comment) component);
				} else {
					LOG.warn("Error getting social component {}", rstResource.getPath());
				}
			} else {
				LOG.warn("Error obtaining SocialComponentFactory for {}", rstResource.getPath());
			}
		}
	}

	public Object get(int index) {
		return this.entries.get(index);
	}

	public int size() {
		return this.entries.size();
	}

	public int getTotalSize() {
		return this.totalSize.intValue();
	}

	public void setPagination(CollectionPagination pagination) {
	}

	public void setSortedOrder(CollectionSortedOrder sortedOrder) {
	}

	public CollectionPagination getPagination() {
		return null;
	}

	public List<Comment> getComments() throws RepositoryException {
		return this.entries;
	}

	public void setPathConstraint(PathConstraintType pathConstraintType) {
		this.pathConstraintType = pathConstraintType;
	}
}
