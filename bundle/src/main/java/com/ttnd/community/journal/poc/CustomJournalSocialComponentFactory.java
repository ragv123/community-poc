package com.ttnd.community.journal.poc;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.comments.api.Comment;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.core.AbstractSocialComponentFactory;

/**
 * Created by Rajeev.
 */

/**
 * CustomCommentFactory extends the default CommentSocialComponentFactory to
 * leverage the default comment social component implementation. This makes it
 * possible to only make changes needed for customization without having to
 * implement all the APIs specified by {@link Comment}.
 */
@Component(name = "Blog Social journal Component Factory", immediate = true)
@Service
public class CustomJournalSocialComponentFactory extends AbstractSocialComponentFactory
		implements SocialComponentFactory {
	private static final Logger LOG = LoggerFactory.getLogger(CustomJournalSocialComponentFactory.class);
	@Reference
	private CommentSocialComponentListProviderManager listProviderManager;

	public SocialComponent getSocialComponent(Resource resource) {
		return new JournalSocialComponent(resource, getClientUtilities(resource.getResourceResolver()),
				this.listProviderManager);
	}

	public SocialComponent getSocialComponent(Resource resource, SlingHttpServletRequest request) {
		return new JournalSocialComponent(resource, getClientUtilities(request), getQueryRequestInfo(request),
				this.listProviderManager);
	}

	public SocialComponent getSocialComponent(Resource resource, ClientUtilities clientUtils,
			QueryRequestInfo queryInfo) {
		return new JournalSocialComponent(resource, clientUtils, queryInfo, this.listProviderManager);
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

	
	 @Override public int getPriority() { return 100; }
	 

	public String getSupportedResourceType() {
		return "blog/components/hbs/journal";
	}

}
