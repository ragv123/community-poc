package com.ttn.community.journal.poc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.journal.client.api.JournalSidebar;
import com.adobe.cq.social.journal.client.api.JournalSocialComponentFactory;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.CollectionPagination;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.core.BaseSocialComponent;
import com.adobe.cq.social.scf.core.CollectionSortedOrder;
import com.adobe.cq.social.srp.FacetRangeField;
import com.adobe.cq.social.srp.FacetSearchResult;
import com.adobe.cq.social.srp.SocialResourceProvider;
import com.adobe.cq.social.ugcbase.SocialUtils;
import com.adobe.granite.security.user.UserProperties;
import com.day.cq.commons.date.DateUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.ttn.community.journal.util.CustomJournalUtils;

public class CustomJournalSidebar extends BaseSocialComponent implements JournalSidebar {
	private static final Logger LOG = LoggerFactory.getLogger(JournalSocialComponentFactory.class);
	public static final String RANGE_FACET_FIELD_NAME = "added";
	public static final String ITEM_FILTER_LIMIT = "itemFilterLimit";
	private Map<String, Tag> tags;
	private Map<String, Map<String, String>> authorsMap;
	private Map<String, Map<String, String>> categoryMap;
	private Map<String, Integer> authorCounts;
	private Map<String, Integer> archiveCounts;
	private Map<String, Integer> tagCounts;
	private Resource journalResource;
	private CustomJournalArchive archive;
	private String viewType;
	private int maxItem;
	private final SocialUtils socialUtils;
	private List items;
	private Map<String, Integer> countMap;
	private Map<String, Map<String, String>> itemsMap;

	public CustomJournalSidebar(Resource resource, ClientUtilities clientUtils, QueryRequestInfo queryRequestInfo,
			CommentSocialComponentListProviderManager listProviderManager) {
		super(resource, clientUtils);
		this.socialUtils = clientUtils.getSocialUtils();
		this.initCollection(resource, queryRequestInfo, listProviderManager);
	}

	private void initCollection(Resource resource, QueryRequestInfo queryRequestInfo,
			CommentSocialComponentListProviderManager listProviderManager) {
		Map predicates = queryRequestInfo.getPredicates();
		ResourceResolver resourceResolver = resource.getResourceResolver();
		if (predicates.get("jpath") == null && predicates.get("epath") != null) {
			Resource journalEntryResource = CustomJournalUtils
					.getJournal(resourceResolver.getResource(((String[]) predicates.get("epath"))[0]));
			this.journalResource = journalEntryResource != null ? resourceResolver.getResource(
					(String) ((ValueMap) journalEntryResource.adaptTo(ValueMap.class)).get("commentsNode", (Object) ""))
					: null;
		} else if (predicates.get("jpath") != null) {
			this.journalResource = resourceResolver.getResource(((String[]) predicates.get("jpath"))[0]);
		} else {
			String journalComponentPath = this.properties.getProperty("journalComponentPath", null);
			if (journalComponentPath != null) {
				this.journalResource = resourceResolver.getResource(journalComponentPath);
			} else {
				Page page = this.socialUtils.getContainingPage(resource);
				this.journalResource = page.getContentResource("content/primary/blog");
			}
		}
		this.maxItem = Integer.parseInt(this.properties.getProperty("itemFilterLimit", "-1"));
		this.viewType = this.properties.getProperty("viewType", "authors");
		if (this.viewType.equals("categories")) {
			this.items = this.getCategories();
			this.countMap = this.tagCounts;
			this.itemsMap = this.categoryMap;
		} else if (this.viewType.equals("archives")) {
			this.items = this.getArchives(0);
			this.countMap = this.archiveCounts;
		} else {
			this.items = this.getAuthors();
			this.itemsMap = this.authorsMap;
			this.countMap = this.authorCounts;
		}
	}

	public String getViewType() {
		return StringUtils.capitalize((String) this.viewType);
	}

	public Map<String, Map<String, String>> getItemsMap() {
		return this.itemsMap;
	}

	private List<String> getAuthors() {
		ArrayList<String> authors = new ArrayList<String>();
		this.authorsMap = new HashMap<String, Map<String, String>>();
		this.authorCounts = new HashMap<String, Integer>();
		if (this.journalResource != null) {
			String[] categoryFields = new String[] { "userIdentifier" };
			try {
				Map<String, Integer> categories = this.getFacetsMap(categoryFields, "userIdentifier");
				for (String keys : categories.keySet()) {
					UserProperties userProperties = this.socialUtils
							.getUserProperties(this.journalResource.getResourceResolver(), keys);
					String temp = userProperties != null ? userProperties.getDisplayName() : "Unknown";
					if (authors.contains(temp))
						continue;
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("authorizableID", userProperties != null ? userProperties.getAuthorizableID() : "admin");
					map.put("avatarUrl", this.socialUtils.getAvatar(userProperties));
					map.put("count", categories.get(keys).toString());
					map.put("friendlyUrl", this.getJournalFriendlyUrl());
					this.authorsMap.put(temp, map);
					authors.add(temp);
					this.authorCounts.put(temp, categories.get(keys));
				}
			} catch (RepositoryException e) {
				LOG.error("RepositoryException: ", (Throwable) e);
			}
			Collections.sort(authors);
		}
		return authors;
	}

	private Map<String, Integer> getFacetsMap(String[] categoryFields, String query) throws RepositoryException {
		Resource ugcResource = this.socialUtils.getUGCResource(this.journalResource);
		SocialResourceProvider provider = this.socialUtils.getConfiguredProvider(ugcResource);
		Map facets = provider.findFacets(ugcResource.getResourceResolver(), Arrays.asList(categoryFields),
				"blog/components/hbs/entry_topic", this.journalResource.getPath(), this.maxItem, true);
		if (facets.size() == 0) {
			return new LinkedHashMap<String, Integer>();
		}
		return (Map) facets.get(query);
	}

	private List<String> getCategories() {
		this.initTags();
		if (this.journalResource != null) {
			Collection<Tag> tagCollection = this.tags.values();
			Tag[] sortedTags = tagCollection.toArray(new Tag[this.tags.size()]);
			Arrays.sort(sortedTags, new Comparator<Tag>() {
				public int compare(Tag tag1, Tag tag2) {
					return tag1.getTitle().compareTo(tag2.getTitle());
				}
			});
			String[] outTags = new String[sortedTags.length];
			for (int i = 0; i < sortedTags.length; ++i) {
				outTags[i] = sortedTags[i].getTitle();
			}
			return Arrays.asList(outTags);
		}
		return new ArrayList<String>();
	}

	private void initTags() {
		if (this.tags == null && this.journalResource != null) {
			this.tags = new HashMap<String, Tag>();
			this.categoryMap = new HashMap<String, Map<String, String>>();
			this.tagCounts = new HashMap<String, Integer>();
			String[] categoryFields = new String[] { "cq:tags" };
			try {
				Map<String, Integer> categories = this.getFacetsMap(categoryFields, "cq:tags");
				TagManager tm = (TagManager) this.journalResource.getResourceResolver().adaptTo(TagManager.class);
				for (Map.Entry<String, Integer> entry : categories.entrySet()) {
					String tagString = entry.getKey();
					Tag tag = tm.resolve(tagString);
					if (tag == null)
						continue;
					String key = tag.getPath();
					this.tags.put(key, tag);
					HashMap<String, String> item = new HashMap<String, String>();
					item.put("id", tag.getTagID());
					item.put("description", tag.getDescription());
					item.put("count", Integer.toString(entry.getValue()));
					item.put("friendlyUrl", this.getJournalFriendlyUrl());
					this.categoryMap.put(tag.getTitle(), item);
					this.tagCounts.put(tag.getTitle(), entry.getValue());
				}
			} catch (Exception e) {
				LOG.error("RepositoryException: ", (Throwable) e);
			}
		}
	}

	private String getJournalFriendlyUrl() {
		String pagePath = this.clientUtils.getSocialUtils().getContainingPage(this.journalResource).getPath();
		return this.clientUtils.externalLink(pagePath, Boolean.valueOf(false)) + ".html";
	}

	private CustomJournalArchive getArchive(int max, DateFormat dateFormat) {
		if (this.archive == null) {
			this.archive = new CustomJournalArchive(this.journalResource, max, dateFormat);
		}
		return this.archive;
	}

	private List<CustomJournalArchive.Period> getArchives(int max) {
		List<CustomJournalArchive.Period> periods = new ArrayList<CustomJournalArchive.Period>();
		if (this.journalResource != null) {
			Resource ugcResource = this.socialUtils.getUGCResource(this.journalResource);
			SocialResourceProvider provider = this.socialUtils.getConfiguredProvider(ugcResource);
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			Date nowDate = calendar.getTime();
			calendar.add(1, -5);
			int month = calendar.get(2);
			int year = calendar.get(1);
			calendar.clear();
			calendar.set(year, month, 1, 0, 0, 0);
			Date oneYearBeforeNowDate = calendar.getTime();
			FacetRangeField facetRangeField = new FacetRangeField("added", oneYearBeforeNowDate, nowDate, "+1MONTH");
			ArrayList<FacetRangeField> facetRangeFields = new ArrayList<FacetRangeField>();
			facetRangeFields.add(facetRangeField);
			FacetSearchResult facets = provider.findFacets(this.journalResource.getResourceResolver(), new ArrayList(),
					facetRangeFields, "blog/components/hbs/entry_topic", this.journalResource.getPath(),
					Integer.MAX_VALUE);
			Map rangeResult = facets.getRangeResult();
			this.archiveCounts = new HashMap<String, Integer>();
			DateFormat dateFormat = DateUtil.getDateFormat((String) ((String) this.properties.get("dateFormat")),
					(String) "yyyy MMMMM",
					(Locale) CustomJournalUtils.findPage(this.journalResource).getLanguage(false));
			SimpleDateFormat incomingDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			SimpleDateFormat expectedDateFormat = new SimpleDateFormat("yyyy/MM");
			try {
				Map<String, Integer> dateFieldResults = (Map) rangeResult.get("added");
				for (String key : dateFieldResults.keySet()) {
					String periodTitle = CustomJournalArchive
							.getPeriodTitle(expectedDateFormat.format(incomingDateFormat.parse(key)), dateFormat);
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
					Date date = incomingDateFormat.parse(key);
					CustomJournalArchive journalArchive = this.getArchive(max, dateFormat);
					//journalArchive.getClass();
					periods.add(journalArchive.new Period(format.format(date), periodTitle,
							((Integer) dateFieldResults.get(key)).toString()));
					this.archiveCounts.put(periodTitle, (Integer) dateFieldResults.get(key));
				}
			} catch (ParseException e) {
				periods = this.getArchive(max, dateFormat).getPeriods();
			} catch (Exception e) {
				periods = this.getArchive(max, dateFormat).getPeriods();
			}
			Collections.sort(periods, new Comparator<CustomJournalArchive.Period>() {
				public int compare(CustomJournalArchive.Period p1, CustomJournalArchive.Period p2) {
					return p1.getId().compareTo(p2.getId());
				}
			});
		}
		return periods;
	}

	public int getTotalSize() {
		return this.items.size();
	}

	public void setPagination(CollectionPagination collectionPagination) {
	}

	public void setSortedOrder(CollectionSortedOrder collectionSortedOrder) {
	}

	public List<Object> getItems() {
		return this.items;
	}

	public Map<String, Integer> getCountMap() {
		return this.countMap;
	}

}