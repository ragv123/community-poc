package com.ttn.community.journal.factory;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.core.AbstractSocialComponentFactory;
import com.ttn.community.journal.poc.CustomJournalSidebar;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service({SocialComponentFactory.class})
public class CustomJournalSidebarSocialComponentFactory
  extends AbstractSocialComponentFactory
  implements SocialComponentFactory
{
  private static final Logger LOG = LoggerFactory.getLogger(CustomJournalSidebarSocialComponentFactory.class);
  @Reference
  private CommentSocialComponentListProviderManager listProviderManager;
  
  public String getSupportedResourceType()
  {
    return "blog/components/hbs/sidebar";
  }
  
  public SocialComponent getSocialComponent(Resource resource)
  {
    return new CustomJournalSidebar(resource, getClientUtilities(resource.getResourceResolver()), getQueryRequestInfo(null), this.listProviderManager);
  }
  
  public SocialComponent getSocialComponent(Resource resource, SlingHttpServletRequest request)
  {
    return new CustomJournalSidebar(resource, getClientUtilities(request), getQueryRequestInfo(request), this.listProviderManager);
  }
  
  public SocialComponent getSocialComponent(Resource resource, ClientUtilities clientUtils, QueryRequestInfo queryInfo)
  {
    return new CustomJournalSidebar(resource, clientUtils, queryInfo, this.listProviderManager);
  }
  
  protected void bindListProviderManager(CommentSocialComponentListProviderManager paramCommentSocialComponentListProviderManager)
  {
    this.listProviderManager = paramCommentSocialComponentListProviderManager;
  }
  
  protected void unbindListProviderManager(CommentSocialComponentListProviderManager paramCommentSocialComponentListProviderManager)
  {
    if (this.listProviderManager == paramCommentSocialComponentListProviderManager) {
      this.listProviderManager = null;
    }
  }
}

