package com.ttn.community.journal.util;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

public class CustomJournalUtils
{
  private static final Logger LOG = LoggerFactory.getLogger(CustomJournalUtils.class);
  
  public static Resource getParent(Resource resource)
  {
    if (resource == null) {
      return null;
    }
    return resource.getParent();
  }
  
  public static Resource getJournal(Resource resource)
  {
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
  
  public static Resource getJournalEntry(Resource resource)
  {
    if (ResourceUtil.isA(resource, "blog/components/hbs/comment")) {
      return getParent(resource);
    }
    if (ResourceUtil.isA(resource, "blog/components/hbs/entry_topic")) {
      return resource;
    }
    return null;
  }
  
  public static Page findPage(Resource resource)
  {
    Page page = null;
    Resource temp = resource;
    ResourceResolver resourceResolver = temp.getResourceResolver();
    try
    {
      while (temp.getPath().lastIndexOf("/") > 0)
      {
        page = (Page)temp.adaptTo(Page.class);
        if ((page != null) && 
          ("".equals(page.getProperties().get("journalarchive", "")))) {
          return page;
        }
        temp = resourceResolver.getResource(temp.getPath().substring(0, temp.getPath().lastIndexOf("/")));
      }
    }
    catch (Exception ignored) {}
    return page;
  }
}

