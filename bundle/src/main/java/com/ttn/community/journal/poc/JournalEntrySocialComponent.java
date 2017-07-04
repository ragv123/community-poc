package com.ttn.community.journal.poc;

import java.text.SimpleDateFormat;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentList;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.forum.client.api.AbstractPost;
import com.adobe.cq.social.journal.client.api.JournalEntryComment;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.core.DefaultResourceID;
import com.adobe.cq.social.scf.core.ResourceID;
import com.adobe.cq.social.ugc.api.PathConstraintType;

public class JournalEntrySocialComponent extends AbstractPost implements JournalEntryComment {

	// private static final String ENTRY_HTML_SUFFIX = ".entry.html";

	public JournalEntrySocialComponent(Resource resource, ClientUtilities clientUtils,
			CommentSocialComponentListProviderManager listProviderManager) throws RepositoryException {
		super(resource, clientUtils, QueryRequestInfo.DEFAULT_QUERY_INFO_FACTORY.create(), listProviderManager);
		CommentSocialComponentList list = (CommentSocialComponentList) getItems();
		list.setPathConstraint(PathConstraintType.IsChildNode);
	}

	public JournalEntrySocialComponent(Resource resource, ClientUtilities clientUtils, QueryRequestInfo queryInfo,
			CommentSocialComponentListProviderManager listProviderManager) throws RepositoryException {
		super(resource, clientUtils, queryInfo, listProviderManager);
		CommentSocialComponentList list = (CommentSocialComponentList) getItems();
		list.setPathConstraint(PathConstraintType.IsChildNode);
	}

	public JournalEntrySocialComponent(Resource resource, ClientUtilities clientUtils, QueryRequestInfo queryInfo,
			Resource latestPost, int numReplies, CommentSocialComponentListProviderManager listProviderManager)
			throws RepositoryException {
		super(resource, clientUtils, queryInfo, latestPost, numReplies, listProviderManager);
		CommentSocialComponentList list = (CommentSocialComponentList) getItems();
		list.setPathConstraint(PathConstraintType.IsChildNode);
	}

	public String getFriendlyUrl() {
		Resource journal = getJournal(getResource());
		String pagePath = this.clientUtils.getSocialUtils().getContainingPage(journal).getPath();
		ResourceID urlId = this.id;
		if (!isTopLevel()) {
			Resource topicResource = getJournalEntry(getResource());
			urlId = new DefaultResourceID(topicResource);
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd");
		String creationDateString = dateFormat.format(getCreated().getTime());
		return this.clientUtils.externalLink(pagePath, Boolean.valueOf(false)) + ".entry.html" + "/"
				+ creationDateString + "/" + StringUtils.substringAfterLast(urlId.getResourceIdentifier(), "/")
				+ ".html";
	}

	private Resource getJournalEntry(Resource resource) {
		if (ResourceUtil.isA(resource, "social/journal/components/hbs/comment")) {
			return getParent(resource);
		}
		if (ResourceUtil.isA(resource, "social/journal/components/hbs/entry_topic")) {
			return resource;
		}
		return null;
	}

	private Resource getParent(Resource resource) {
		if (resource == null) {
			return null;
		}
		return resource.getParent();
	}

	private Resource getJournal(Resource resource) {
		if (ResourceUtil.isA(resource, "blog/components/hbs/comment")) {
			return getParent(getParent(resource));
		}
		if (ResourceUtil.isA(resource, "blog/components/hbs/entry_topic")) {
			return getParent(resource);
		}
		if (ResourceUtil.isA(resource, "blog/components/hbs/journal")) {
			return resource;
		}
		return null;
	}
}
