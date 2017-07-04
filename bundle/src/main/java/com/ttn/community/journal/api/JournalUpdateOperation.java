package com.ttn.community.journal.api;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.PostOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.annotation.Parameters;
import com.adobe.cq.social.commons.comments.endpoints.AbstractCommentOperation;
import com.adobe.cq.social.journal.client.endpoints.JournalOperations;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.cq.social.scf.SocialOperationResult;
import com.adobe.cq.social.commons.annotation.Endpoint;

@Endpoint(name = "CustomJournalCommentUpdateOperation", resourceType = "blog/components/hbs/journal", description = "Update a journal entry or comment.", example = "curl -X POST -H \"Accept:application/json\" -d \"message=newmessage&id=nobot&:operation=social:updateJournalComment\" -uaparker@geometrixx.info:aparkerhttp:hostname:port/path/to/journal.social.json")
@Parameters({
		@com.adobe.cq.social.commons.annotation.Parameter(name = ":operation", value = "social:updateJournalComment", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "id", value = "nobot", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "_charset_", value = "UTF-8 or equivalent to support double byte characters", required = false),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "subject", value = "The new topic subject", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "message", value = "the new topic content.", required = true) })
@Component(immediate = true)
@Service
@Property(name = "sling.post.operation", value = { "social:updateJournal" })
public class JournalUpdateOperation extends AbstractCommentOperation<JournalOperations> implements PostOperation {

	public static final String UPDATE_COMMENT_OPERATION = "social:updateJournalComment";
	private static final Logger LOG = LoggerFactory.getLogger(JournalUpdateOperation.class);
	
	@Reference(target="(JournalOperationService=CustomJournalOperationService)")
	private JournalOperations journalOperations;

	protected SocialOperationResult performOperation(SlingHttpServletRequest request, Session session)
			throws OperationException {
		Resource post = getCommentOperationService().update(request, session);
		return new SocialOperationResult(getSocialComponentForComment(post, request), "update", 200, post.getPath());
	}

	protected JournalOperations getCommentOperationService() {
		return this.journalOperations;
	}

	protected void bindJournalOperations(JournalOperations paramJournalOperations) {
		this.journalOperations = paramJournalOperations;
	}

	protected void unbindJournalOperations(JournalOperations paramJournalOperations) {
		if (this.journalOperations == paramJournalOperations) {
			this.journalOperations = null;
		}
	}

}
