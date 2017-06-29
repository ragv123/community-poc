package com.ttnd.community.journal.poc;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentList;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.forum.client.api.AbstractPost;
import com.adobe.cq.social.journal.client.api.JournalEntryComment;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
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
}
