package com.ttn.community.journal.extension;

public class SCFJournalCreateExtension {
	
}
/*import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
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
import com.adobe.cq.social.ugcbase.moderation.AutoModeration;
import com.adobe.granite.security.user.UserProperties;

@Component(name = "Journal Extension", immediate = true, metatype = true)
@Service
public class SCFJournalCreateExtension implements JournalOperationExtension {

	private static final Logger LOG = LoggerFactory.getLogger(SCFJournalCreateExtension.class);
	@Reference
	private AutoModeration autoModeration;

	@Override
	public int getOrder() {
		return 1;
	}

	@Override
	public String getName() {
		return "custom blog";
	}

	@Override
	public List<JournalOperation> getOperationsToHookInto() {
		return Arrays.asList(JournalOperation.CREATE, JournalOperation.UPDATE);
	}

	@Override
	public void beforeAction(Operation paramOperation, Session paramSession, Resource paramResource,
			Map<String, Object> paramMap) throws OperationException {
		ValueMap resourceMap = paramResource.adaptTo(ValueMap.class);
		ResourceResolver resourceResolver = paramResource.getResourceResolver();
		try {
			if (ResourceUtil.isA(paramResource, "blog/components/hbs/journal")) {
				String[] selectedGroup = resourceMap.get("oauth.create.users.groups", new String[0]);
				Iterator<Group> groups = getUserGroups(resourceResolver);
				List<String> userGroups = Arrays.asList(selectedGroup);
				while (groups.hasNext()) {
					if (userGroups.contains(groups.next().getID())) {
						paramMap.put("approved", Boolean.valueOf(true));
						break;
					}
				}
			} else if (ResourceUtil.isA(paramResource, "blog/components/hbs/entry_topic")
					|| ResourceUtil.isA(paramResource, "social/journal/components/hbs/comment")) {
				String root = resourceMap.get("social:rootCommentSystem", String.class);
				ValueMap rootResourceMap = resourceResolver.resolve(root).adaptTo(ValueMap.class);
				if (rootResourceMap.get("premoderatedComments", false)) {
					paramMap.put("approved", Boolean.valueOf(true));
					paramMap.put("isFlagged", Boolean.valueOf(false));
					paramMap.put("isSpam", Boolean.valueOf(false));
					paramMap.put("isFlaggedHidden", Boolean.valueOf(false));
					paramMap.put("moderate", Boolean.valueOf(true));
				}
			}
		} catch (RepositoryException e) {
			LOG.error("Repository Exception : ", e);
		}
	}

	private Iterator<Group> getUserGroups(ResourceResolver resourceResolver) throws RepositoryException {
		UserProperties up = (UserProperties) resourceResolver.adaptTo(UserProperties.class);
		String userIdentifier = (up == null) ? null : up.getAuthorizableID();
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
		Authorizable auth;
		auth = userManager.getAuthorizable(userIdentifier);
		return auth.memberOf();
	}

	@Override
	public void afterAction(Operation paramOperation, Session paramSession, JournalEntryComment paramT,
			Map<String, Object> paramMap) throws OperationException {
		try {
			Resource resource = paramT.getResource();
			ResourceResolver resourceResolver = resource.getResourceResolver();
			ModifiableValueMap resourceMap = resource.adaptTo(ModifiableValueMap.class);
			if ("blog/components/hbs/entry_topic".equals(resource.getResourceType())) {
				String root = resourceMap.get("social:rootCommentSystem", String.class);
				ValueMap rootResourceMap = resourceResolver.resolve(root).adaptTo(ValueMap.class);
				String[] selectedGroup = rootResourceMap.get("oauth.create.users.groups", new String[0]);
				Iterator<Group> groups = getUserGroups(resourceResolver);
				List<String> userGroups = Arrays.asList(selectedGroup);
				while (groups.hasNext()) {
					if (userGroups.contains(groups.next().getID())) {
						resourceMap.put("approved", Boolean.valueOf(true));
						break;
					}
				}
			} else {
				resourceMap.put("approved", Boolean.valueOf(true));
			}
			resourceResolver.commit();
		} catch (PersistenceException e) {
			LOG.error("PersistenceException : ", e);
		} catch (RepositoryException e) {
			LOG.error("RepositoryException : ", e);
		}
	}

}
*/