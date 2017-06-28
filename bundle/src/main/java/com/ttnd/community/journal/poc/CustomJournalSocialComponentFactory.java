package com.ttnd.community.journal.poc;

import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.comments.api.Comment;
import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.journal.client.api.JournalCommentSocialComponentFactory;
import com.adobe.cq.social.journal.client.api.JournalEntrySocialComponentFactory;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.core.AbstractSocialComponentFactory;

/**
 * Created by Rajeev.
 */

/**
 * CustomCommentFactory extends the default CommentSocialComponentFactory to leverage the default comment social
 * component implementation. This makes it possible to only make changes needed for customization without having to
 * implement all the APIs specified by {@link Comment}.
 */
@Component(name = "Blog Social journal Component Factory",immediate=true)
@Service
public class CustomJournalSocialComponentFactory extends JournalEntrySocialComponentFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CustomJournalSocialComponentFactory.class);
    @Reference
    private CommentSocialComponentListProviderManager commentListProviderManager;

    public SocialComponent getSocialComponent(Resource resource) {
        try {
            LOG.info("This Resource ----------------------------------------------------------"+ resource);
            LOG.info("commentListProviderManager ----------------------------------------------------------"+ commentListProviderManager);
            return new JournalSocialComponent(resource, this.getClientUtilities(resource.getResourceResolver()),commentListProviderManager);
        } catch (RepositoryException e) {
            return null;
        }
    }

    public SocialComponent getSocialComponent(Resource resource, final SlingHttpServletRequest request) {
        try {
            LOG.info("This Resource ----------------------------------------------------------"+ resource);
            LOG.info("commentListProviderManager ----------------------------------------------------------"+ commentListProviderManager);
            LOG.info("request ----------------------------------------------------------"+ request);
            LOG.info("Component Factory ----------------------------------------------------------"+ this);
            return new JournalSocialComponent(resource, this.getClientUtilities(request),this.getQueryRequestInfo(request),commentListProviderManager);
        } catch (RepositoryException e) {
            return null;
        }
    }

    public SocialComponent getSocialComponent(Resource resource, ClientUtilities clientUtils, QueryRequestInfo listInfo) {
        try {
            LOG.info("This Resource ----------------------------------------------------------"+ resource);
            LOG.info("commentListProviderManager ----------------------------------------------------------"+ commentListProviderManager);
            return new JournalSocialComponent(resource, clientUtils, listInfo,commentListProviderManager);
        } catch (RepositoryException e) {
            return null;
        }
    }


    
     /** (non-Javadoc)
     * @see com.adobe.cq.social.commons.client.api.AbstractSocialComponentFactory#getPriority() Set the priority to a
     * number greater than 0 to override the default SocialComponentFactory for comments.*/
     
    protected void bindCommentListProviderManager(CommentSocialComponentListProviderManager paramCommentSocialComponentListProviderManager)
    {
      this.commentListProviderManager = paramCommentSocialComponentListProviderManager;
    }
    
    protected void unbindCommentListProviderManager(CommentSocialComponentListProviderManager paramCommentSocialComponentListProviderManager)
    {
      if (this.commentListProviderManager == paramCommentSocialComponentListProviderManager) {
        this.commentListProviderManager = null;
      }
    }
    
   /* @Override
    public int getPriority() {
        return 100;
    }*/

    public String getSupportedResourceType() {
        return "journal-poc/components/hbs/entry_topic";
    }

}
