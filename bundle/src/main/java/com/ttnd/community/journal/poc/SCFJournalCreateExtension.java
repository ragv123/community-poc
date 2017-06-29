package com.ttnd.community.journal.poc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.journal.client.api.JournalEntryComment;
import com.adobe.cq.social.journal.client.endpoints.JournalOperationExtension;
import com.adobe.cq.social.scf.Operation;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.granite.security.user.UserProperties;

@Component(name = "Journal Extension", immediate = true, metatype = true)
@Service
public class SCFJournalCreateExtension implements JournalOperationExtension {

	private static final Logger LOG = LoggerFactory.getLogger(SCFJournalCreateExtension.class);

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "custom journal";
	}

	@Override
	public List<JournalOperation> getOperationsToHookInto() {
		return Arrays.asList(JournalOperation.CREATE,JournalOperation.UPDATE, JournalOperation.DELETE,JournalOperation.UPLOADIMAGE);
	}

	@Override
	public void beforeAction(Operation paramOperation, Session paramSession, Resource paramResource,
			Map<String, Object> paramMap) throws OperationException {
		//paramResource.getResourceResolver().adaptTo(Session.class);
		ResourceResolver resourceResolver = paramResource.getResourceResolver();
		UserProperties up = (UserProperties) resourceResolver.adaptTo(UserProperties.class);
		String userIdentifier = (up == null) ? null : up.getAuthorizableID();
		System.out.println("userIdentifier : "+ userIdentifier);
		String [] selectedGroup = paramResource.adaptTo(ValueMap.class).get("oauth.create.users.groups",new String[0]);
		System.out.println("selectedGroup : "+ selectedGroup);
		System.out.println("group from map : "+ paramMap.get("oauth.create.users.groups"));
		
		
		
		
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
        /* to get the current user */ 
        Authorizable auth;
		try {
			auth = userManager.getAuthorizable(userIdentifier);
			/* to get the groups it is member of */ 
	          Iterator<Group> groups = auth.memberOf();
	          //String [] selectedGroup = targetCommentSystemResource.adaptTo(ValueMap.class).get("oauth.create.users.groups",new String[0]);
	          //Arrays.sort(selectedGroup);
	          List<String> userGroups = Arrays.asList(selectedGroup);
	          while(groups.hasNext()){
	        	  String grpId = groups.next().getID();
	        	  System.out.println("group id : "+grpId);
	        	  if(userGroups.contains(grpId)){
			          System.out.println("result true");
			          paramMap.put("approved", Boolean.valueOf(true));
			          break;
	        	  }else{
	        		  System.out.println("result false");
	        	  }
	          }
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} 
		
		
		
		
		
		/*if (ResourceUtil.isA(paramResource, "social/journal/components/hbs/journal")) {
			LOG.info("Approved Value" + paramMap);
        	LOG.info("User is : " + paramSession.getUserID());
        	Boolean isApproved = true;
        	paramMap.put("approved", isApproved);
            if(paramSession.getUserID().equals("admin")){
            	paramMap.put("approved", isApproved);
            }
		}*/
		
	}

	@Override
	public void afterAction(Operation paramOperation, Session paramSession, JournalEntryComment paramT,
			Map<String, Object> paramMap) throws OperationException {
		// TODO Auto-generated method stub
		
	}

}
