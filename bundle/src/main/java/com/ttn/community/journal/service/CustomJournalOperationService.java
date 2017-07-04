package com.ttn.community.journal.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.CollabUtil;
import com.adobe.cq.social.commons.CommentException;
import com.adobe.cq.social.commons.CommentSystem;
import com.adobe.cq.social.forum.client.endpoints.AbstractForumOperationsService;
import com.adobe.cq.social.journal.client.api.JournalEntryComment;
import com.adobe.cq.social.journal.client.api.JournalEvent;
import com.adobe.cq.social.journal.client.endpoints.JournalOperationExtension;
import com.adobe.cq.social.journal.client.endpoints.JournalOperationExtension.JournalOperation;
import com.adobe.cq.social.journal.client.endpoints.JournalOperations;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.SocialComponentFactoryManager;
import com.adobe.cq.social.ugcbase.core.SocialResourceUtils;
import com.adobe.granite.security.user.UserProperties;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;

@Component(immediate = true, metatype = true, label = "AEM Communities JournalOperationProvider", description = "This component serves the journals")
@Service({ JournalOperations.class })
@Reference(name = "forumOperationExtension", referenceInterface = JournalOperationExtension.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
@Property(name = "JournalOperationService", value = "CustomJournalOperationService")
public class CustomJournalOperationService extends
		AbstractForumOperationsService<JournalOperationExtension, JournalOperationExtension.JournalOperation, JournalEntryComment>
		implements JournalOperations {

	private final Logger LOGGER = LoggerFactory.getLogger(CustomJournalOperationService.class);
	private static final String PN_ENTRY_RESOURCETYPE = "entryresourcetype";
	private static final String PN_COMMENT_RESOURCETYPE = "commentresourcetype";

	protected void postCreateEvent(JournalEntryComment journalEntry, String userId) {
		if (journalEntry.isTopic()) {
			postEvent(new JournalEvent(journalEntry, userId, JournalEvent.JournalActions.CREATED));
		} else {
			postEvent(new JournalEvent(journalEntry, userId, JournalEvent.JournalActions.REPLY_CREATED));
		}
	}

	protected void postDeleteEvent(JournalEntryComment journalEntry, String userId) {
		if (journalEntry.isTopic()) {
			postEvent(new JournalEvent(journalEntry, userId, JournalEvent.JournalActions.DELETED));
		} else {
			postEvent(new JournalEvent(journalEntry, userId, JournalEvent.JournalActions.REPLY_DELETED));
		}
	}

	protected void postUpdateEvent(JournalEntryComment journalEntry, String userId) {
		if (journalEntry.isTopic()) {
			postEvent(new JournalEvent(journalEntry, userId, JournalEvent.JournalActions.EDITED));
		} else {
			postEvent(new JournalEvent(journalEntry, userId, JournalEvent.JournalActions.REPLY_EDITED));
		}
	}

	protected void bindForumOperationExtension(JournalOperationExtension extension) {
		addOperationExtension(extension);
	}

	protected void unbindForumOperationExtension(JournalOperationExtension extension) {
		removeOperationExtension(extension);
	}

	protected String getEventTopic() {
		return "journal";
	}

	protected JournalOperationExtension.JournalOperation getCreateOperation() {
		return JournalOperationExtension.JournalOperation.CREATE;
	}

	protected JournalOperationExtension.JournalOperation getUpdateOperation() {
		return JournalOperationExtension.JournalOperation.UPDATE;
	}

	protected JournalOperationExtension.JournalOperation getDeleteOperation() {
		return JournalOperationExtension.JournalOperation.DELETE;
	}

	protected JournalOperationExtension.JournalOperation getUploadImageOperation() {
		return JournalOperationExtension.JournalOperation.UPLOADIMAGE;
	}

	public String getTopicDesignResourceType() {
		return this.PN_ENTRY_RESOURCETYPE;
	}

	public String getPostDesignResourceType() {
		return this.PN_COMMENT_RESOURCETYPE;
	}

	public SocialComponentFactoryManager getSocialComponentFactoryManager() {
		return this.componentFactoryManager;
	}

	protected String getResourceType(Resource root) {
		if (ResourceUtil.isA(root, "blog/components/hbs/journal")) {
			return getJournalEntryResourceType(root);
		}
		return getJournalCommentResourceType(root);
	}

	protected JournalEntryComment getSocialComponentForResource(Resource resource) {
		if (resource == null) {
			return null;
		}
		SocialComponentFactory factory = this.componentFactoryManager.getSocialComponentFactory(resource);
		SocialComponent component = factory.getSocialComponent(resource);
		if ((component instanceof JournalEntryComment)) {
			return (JournalEntryComment) component;
		}
		return null;
	}

	private String getJournalEntryResourceType(Resource resource) {
		String topicResourceType = "blog/components/hbs/entry_topic";
		Designer forumDesign = (Designer) resource.getResourceResolver().adaptTo(Designer.class);
		if (null != forumDesign) {
			Style currentStyle = forumDesign.getStyle(resource);
			if ((null != currentStyle) && (StringUtils.equals((String) currentStyle.get("sling:resourceType"),
					resource.getResourceType()))) {
				topicResourceType = (String) currentStyle.get(this.PN_ENTRY_RESOURCETYPE, topicResourceType);
			}
		}
		return topicResourceType;
	}

	private String getJournalCommentResourceType(Resource root) {
		String postResourceType = "blog/components/hbs/comment";
		Designer forumDesign = (Designer) root.getResourceResolver().adaptTo(Designer.class);
		ValueMap forumProperties = (ValueMap) root.adaptTo(ValueMap.class);
		if (null != forumDesign) {
			Style currentStyle = forumDesign.getStyle(root);
			if ((null != currentStyle) && (StringUtils.equals((String) currentStyle.get("sling:resourceType"),
					(CharSequence) forumProperties.get("sling:resourceType", String.class)))) {
				postResourceType = (String) currentStyle.get(this.PN_COMMENT_RESOURCETYPE, postResourceType);
			}
		}
		return postResourceType;
	}

	protected Resource create(Resource targetCommentSystemResource, CommentSystem cs, String author,
			Map<String, Object> props, List<DataSource> attachments, Session session) throws OperationException {
		JournalOperation createOperation = getCreateOperation();
		performBeforeActions(createOperation, session, targetCommentSystemResource, props);
		if (cs == null) {
			throw new OperationException(
					"Failed to get comment system for target '" + targetCommentSystemResource.getPath() + "' ", 404);
		}
		String message;
		try {
			message = getStringProperty("message", props);
		} catch (RepositoryException e) {
			throw new OperationException("Failed to get the new message value", e, 500);
		}
		if ((message == null) || ("".equals(message))) {
			throw new OperationException("Comment value is empty", 400);
		}
		com.adobe.cq.social.commons.Comment parent = (com.adobe.cq.social.commons.Comment) targetCommentSystemResource
				.adaptTo(com.adobe.cq.social.commons.Comment.class);
		if ((parent != null) && (parent.isClosed())) {
			throw new OperationException("Reply attempted on closed comment: " + targetCommentSystemResource.getPath(),
					400);
		}
		if ((parent != null) && (!mayReply(targetCommentSystemResource, cs))) {
			throw new OperationException("Reply is not allowed: " + targetCommentSystemResource.getPath(), 403);
		}
		long messageCharacterLimit = cs.getMessageCharacterLimit();
		String normalizedMessage = Normalizer.normalize(message, Normalizer.Form.NFC);
		if (normalizedMessage.codePointCount(0, normalizedMessage.length()) > messageCharacterLimit) {
			throw new OperationException("Parameter message exceeded character limit", 400);
		}
		boolean rootPathExists = getResource(cs.getRootPath(), session) != null;
		if (props.containsKey("message")) {
			props.put("jcr:description", props.get("message"));
			props.remove("message");
		}
		if (props.containsKey("tags")) {
			props.put("cq:tags", props.get("tags"));
			props.remove("tags");
		}
		if ((props.containsKey("publishDate")) && ((props.get("publishDate") instanceof Calendar))) {
			Calendar d = Calendar.getInstance();
			d.setTime(((Calendar) props.get("publishDate")).getTime());
			props.put("publishDate", d);
		}
		if (props.containsKey("cq:tags")) {
			Object v = props.get("cq:tags");
			if (!(v instanceof String[])) {
				if ((v instanceof String)) {
					if (String.valueOf(v).isEmpty()) {
						props.remove("cq:tags");
					} else {
						props.put("cq:tags", new String[] { (String) v });
					}
				} else {
					throw new OperationException("Parameter cq:tags is not a String Array", 400);
				}
			}
		}
		for (String key : props.keySet()) {
			if (StringUtils.startsWith(key, "scf:")) {
				props.remove(key);
			}
		}
		try {
			com.adobe.cq.social.commons.Comment comment = cs.addComment(message, author, attachments, "",
					getResourceType(targetCommentSystemResource), props);
			if (SocialResourceUtils.isSocialResource(comment.getResource())) {
				ModifiableValueMap vm = (ModifiableValueMap) comment.getResource().adaptTo(ModifiableValueMap.class);
				if (vm != null) {
					String entityUrl = getEntityUrl(comment.getResource());
					if (!StringUtils.isEmpty(entityUrl)) {
						vm.put("social:entity", entityUrl);
					}
					if (!cs.isModerated()) {
						vm.put("approved", Boolean.valueOf(true));
					}
					try {
						if ("blog/components/hbs/journal".equals(targetCommentSystemResource.getResourceType())) {
							ValueMap properties = targetCommentSystemResource.getValueMap();
							ResourceResolver resourceResolver = targetCommentSystemResource.getResourceResolver();
							String[] selectedGroup = properties.get("oauth.create.users.groups", new String[0]);
							Iterator<Group> groups = getUserGroups(resourceResolver);
							List<String> userGroups = Arrays.asList(selectedGroup);
							while (groups.hasNext()) {
								if (userGroups.contains(groups.next().getID())) {
									vm.put("approved", Boolean.valueOf(true));
									break;
								}
							}
						} else {
							vm.put("approved", Boolean.valueOf(true));
						}
					} catch (RepositoryException e) {
						LOGGER.error("Repository exception : ", e);
					}
					vm.put("eventTopic", getEventTopic());
					if ((vm.get("publishDate", Calendar.class) == null)
							&& (!((Boolean) vm.get("isDraft", Boolean.valueOf(false))).booleanValue())) {
						vm.put("publishDate", Calendar.getInstance());
					}
				}
			}
			String userId = author;
			if (props.containsKey("composedFor")) {
				userId = (String) comment.getProperty("composedFor", author);
			}
			boolean throwEvent = true;
			if (((Boolean) comment.getProperty("isDraft", Boolean.valueOf(false))).booleanValue()) {
				throwEvent = false;
				if (comment.getProperty("publishDate", Calendar.class) != null) {
					Calendar publishDate = (Calendar) comment.getProperty("publishDate", Calendar.class);
					if (publishDate.compareTo(Calendar.getInstance()) <= 0) {
						ModifiableValueMap vm = (ModifiableValueMap) comment.getResource()
								.adaptTo(ModifiableValueMap.class);
						vm.put("isDraft", Boolean.valueOf(false));
						vm.put("cq:lastModified", publishDate);
						vm.put("added", publishDate.getTime());
						throwEvent = true;
					}
				}
			}
			cs.save();
			// LOG.info("Comment created: " + comment.getPath());

			JournalEntryComment commentComp = getSocialComponentForResource(comment.getResource());
			performAfterActions(createOperation, session, commentComp, props);
			if (throwEvent) {
				postCreateEvent(commentComp, userId);
			}
			return comment.getResource();
		} catch (CommentException e) {
			cleanupFailure(session);
			throw new OperationException("Failed to create comment.", e, 500);
		}
	}

	private void cleanupFailure(Session session) {
		try {
			session.refresh(false);
		} catch (RepositoryException e) {
			// LOG.info("Failed to refresh the session", e);
		}
	}

	private String getStringProperty(String key, Map<String, Object> props) throws RepositoryException {
		Object obj = props.get(key);
		if (obj == null) {
			return null;
		}
		if ((obj instanceof Value)) {
			return ((Value) obj).getString();
		}
		return obj.toString();
	}
	protected void bindComponentFactoryManager(SocialComponentFactoryManager paramSocialComponentFactoryManager) {
		this.componentFactoryManager = paramSocialComponentFactoryManager;
	}

	protected void unbindComponentFactoryManager(SocialComponentFactoryManager paramSocialComponentFactoryManager) {
		if (this.componentFactoryManager == paramSocialComponentFactoryManager) {
			this.componentFactoryManager = null;
		}
	}
	
	
	
	protected Resource update(Resource commentResource, CommentSystem cs, Map<String, Object> props,
			List<DataSource> attachments, Session session, String author) throws OperationException {
		com.adobe.cq.social.commons.Comment comment = getComment(commentResource, session);
		if (comment == null) {
			throw new OperationException("Failed to get Commment for target " + commentResource.getPath(), 404);
		}
		if ((comment.isClosed()) && (!CollabUtil.hasModeratePermissions(commentResource))) {
			throw new OperationException("Update attempted on closed comment: " + commentResource.getPath(), 400);
		}
		JournalOperation updateOperation = getUpdateOperation();
		performBeforeActions(updateOperation, session, comment.getResource(), props);
		if (cs == null) {
			throw new OperationException(
					"Failed to get comment system for target '" + comment.getResource().getPath() + "' ", 404);
		}
		try {
			ModifiableValueMap properties = (ModifiableValueMap) comment.getResource()
					.adaptTo(ModifiableValueMap.class);

			Calendar oldPublishDate = (Calendar) properties.get("publishDate", Calendar.class);
			boolean usedToBeDraft = ((Boolean) properties.get("isDraft", Boolean.valueOf(false))).booleanValue();
			if (props.containsKey("tags")) {
				props.put("cq:tags", props.get("tags"));
				props.remove("tags");
			}
			properties.remove("cq:tags");
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				if (entry.getKey() == "message") {
					long messageCharacterLimit = cs.getMessageCharacterLimit();
					String message = CollabUtil.getValueString(entry.getValue());
					if (message == null) {
						throw new OperationException("Null value for comment message.", 400);
					}
					String normalizedMessage = Normalizer.normalize(message, Normalizer.Form.NFC);
					if (normalizedMessage.codePointCount(0, normalizedMessage.length()) > messageCharacterLimit) {
						throw new OperationException("Parameter message exceeded character limit", 400);
					}
					properties.put("jcr:description", entry.getValue());
				} else {
					properties.put(entry.getKey(), entry.getValue());
				}
			}
			if (cs.isModerated()) {
				properties.remove("approved");
			}
			if ("blog/components/hbs/entry_topic".equals(commentResource.getResourceType())) {
				String root = properties.get("social:rootCommentSystem", String.class);
				ResourceResolver resourceResolver = comment.getResource().getResourceResolver();
				ValueMap rootResourceMap = resourceResolver.resolve(root).adaptTo(ValueMap.class);
				String[] selectedGroup = rootResourceMap.get("oauth.create.users.groups", new String[0]);
				Iterator<Group> groups = getUserGroups(resourceResolver);
				List<String> userGroups = Arrays.asList(selectedGroup);
				while (groups.hasNext()) {
					if (userGroups.contains(groups.next().getID())) {
						properties.put("approved", Boolean.valueOf(true));
						break;
					}
				}
			} else {
				properties.put("approved", Boolean.valueOf(true));
			}
			properties.put("moderate", Boolean.TRUE);
			properties.put("cq:lastModified", Calendar.getInstance());
			if (CollabUtil.hasModeratePermissions(comment.getResource())) {
				properties.put("cq:lastModifiedBy", session.getUserID());
			} else {
				properties.put("cq:lastModifiedBy", comment.getAuthor().getId());
			}
			updateAttachments(comment, attachments);

			boolean isCurrentDraft = false;
			if (props.containsKey("isDraft")) {
				isCurrentDraft = ((Boolean) props.get("isDraft")).booleanValue();
			}
			Calendar currentPublishDate = null;
			if (props.containsKey("publishDate")) {
				currentPublishDate = (Calendar) props.get("publishDate");
			}
			boolean cancelJob = false;
			boolean forcePublish = false;
			if ((!isCurrentDraft) && (usedToBeDraft) && (oldPublishDate != null)) {
				cancelJob = true;
			} else if ((isCurrentDraft) && (oldPublishDate != null)
					&& ((currentPublishDate == null) || (oldPublishDate.compareTo(currentPublishDate) != 0))) {
				cancelJob = true;
			}
			if ((isCurrentDraft) && (currentPublishDate != null)
					&& (currentPublishDate.compareTo(Calendar.getInstance()) <= 0)) {
				properties.put("isDraft", Boolean.FALSE);
				properties.put("cq:lastModified", currentPublishDate);
				properties.put("added", currentPublishDate.getTime());
				cancelJob = true;
				forcePublish = true;
			}
			if (cancelJob) {
				String oldJob = (String) properties.get("publishJobId", String.class);
				if (currentPublishDate == null) {
					properties.remove("publishDate");
				}
				if (StringUtils.isNotEmpty(oldJob)) {
					this.futurePostScheduler.unschedule(oldJob);
					properties.remove("publishJobId");
				}
			}
			comment.getResource().getResourceResolver().commit();
			com.adobe.cq.social.commons.Comment updatedComment = (com.adobe.cq.social.commons.Comment) comment
					.getResource().adaptTo(com.adobe.cq.social.commons.Comment.class);

			JournalEntryComment commentComp = getSocialComponentForResource(updatedComment.getResource());
			if (!isCurrentDraft) {
				if (!usedToBeDraft) {
					postUpdateEvent(commentComp, author);
				} else {
					postCreateEvent(commentComp, author);
				}
			} else if (forcePublish) {
				postCreateEvent(commentComp, author);
			}
			performAfterActions(updateOperation, session, commentComp, props);
			return updatedComment.getResource();
		} catch (RepositoryException e) {
			throw new OperationException("Failed to update comment", e, 500);
		} catch (PersistenceException e) {
			throw new OperationException("Failed to update comment", e, 500);
		} catch (IllegalArgumentException e) {
			throw new OperationException("Failed to update comment", e, 500);
		}
	}
			  
	private Iterator<Group> getUserGroups(ResourceResolver resourceResolver)
			throws RepositoryException {
		UserProperties up = (UserProperties) resourceResolver.adaptTo(UserProperties.class);
		String userIdentifier = (up == null) ? null : up.getAuthorizableID();
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
		Authorizable auth;
		auth = userManager.getAuthorizable(userIdentifier);
		return auth.memberOf();
	}
	

}
