package com.ttnd.community.journal.poc;

import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.core.AbstractSocialComponentFactory;

@Component(name = "Blog Social journal Comment Component Factory", immediate = true)
@Service
public class CustomJournalCommentSocialComponentFactory extends AbstractSocialComponentFactory
		implements SocialComponentFactory {

	private static final Logger LOG = LoggerFactory.getLogger(CustomJournalCommentSocialComponentFactory.class);
	@Reference
	private CommentSocialComponentListProviderManager listProviderManager;

	public SocialComponent getSocialComponent(Resource resource) {
		return getSocialComponent(resource, null);
	}

	public SocialComponent getSocialComponent(Resource resource, SlingHttpServletRequest request) {
		if (request == null) {
			return getSocialComponent(resource, getClientUtilities(resource.getResourceResolver()),
					getQueryRequestInfo(request));
		}
		return getSocialComponent(resource, getClientUtilities(request), getQueryRequestInfo(request));
	}

	public SocialComponent getSocialComponent(Resource resource, ClientUtilities clientUtils,
			QueryRequestInfo queryInfo) {
		try {
			return new JournalEntrySocialComponent(resource, clientUtils, queryInfo, this.listProviderManager);
		} catch (RepositoryException e) {
			LOG.error("Failed to create Post instance for %1", resource, e);
		}
		return null;
	}

	@Override
	public int getPriority() {
		return 100;
	}

	public String getSupportedResourceType() {
		return "blog/components/hbs/comment";
	}

	protected void bindListProviderManager(
			CommentSocialComponentListProviderManager paramCommentSocialComponentListProviderManager) {
		this.listProviderManager = paramCommentSocialComponentListProviderManager;
	}

	protected void unbindListProviderManager(
			CommentSocialComponentListProviderManager paramCommentSocialComponentListProviderManager) {
		if (this.listProviderManager == paramCommentSocialComponentListProviderManager) {
			this.listProviderManager = null;
		}
	}
}
