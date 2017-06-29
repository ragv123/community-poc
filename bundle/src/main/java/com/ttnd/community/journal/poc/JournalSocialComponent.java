package com.ttnd.community.journal.poc;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentList;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.forum.client.api.AbstractForum;
import com.adobe.cq.social.journal.client.api.Journal;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.ugc.api.PathConstraintType;

/**
 * Created by Rajeev.
 */
public class JournalSocialComponent extends AbstractForum implements Journal {

	private static final Logger LOG = LoggerFactory.getLogger(JournalSocialComponent.class);

	public JournalSocialComponent(Resource resource, ClientUtilities clientUtilities, CommentSocialComponentListProviderManager listProviderManager)
    {
      super(resource, clientUtilities, listProviderManager);
    }

	public JournalSocialComponent(Resource resource, ClientUtilities clientUtilities, QueryRequestInfo queryRequestInfo, CommentSocialComponentListProviderManager listProviderManager)
    {
      super(resource, clientUtilities, queryRequestInfo, listProviderManager);
      setupListProvider(queryRequestInfo);
    }

	protected void setupListProvider(QueryRequestInfo queryRequestInfo) {
		if ((queryRequestInfo != null) && (queryRequestInfo.isQuery())) {
			CommentSocialComponentList list = (CommentSocialComponentList) getItems();
			list.setPathConstraint(PathConstraintType.IsChildNode);
		}
	}
}
