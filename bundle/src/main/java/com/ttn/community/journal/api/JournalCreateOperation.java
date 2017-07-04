package com.ttn.community.journal.api;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.PostOperation;

import com.adobe.cq.social.commons.annotation.Endpoint;
import com.adobe.cq.social.commons.annotation.Parameters;
import com.adobe.cq.social.commons.comments.endpoints.AbstractCommentOperation;
import com.adobe.cq.social.journal.client.endpoints.JournalOperations;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.cq.social.scf.SocialOperationResult;

@Endpoint(name = "JournalCommentCreateOperation", resourceType = "blog/components/hbs/journal", description = "Create a new journal entry or comment.", example = "curl -X POST -H \"Accept:application/json\" -d \"subject=newEntry&message=test&id=nobot&:operation=social:createJournal\" -uaparker@geometrixx.info:aparkerhttp:hostname:port/path/to/journal.social.json")
@Parameters({
		@com.adobe.cq.social.commons.annotation.Parameter(name = ":operation", value = "social:createJournal", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "id", value = "nobot", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "tags", value = "Array of tag ids", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "_charset_", value = "UTF-8 or equivalent to support double byte characters", required = false),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "subject", value = "The new topic subject", required = true),
		@com.adobe.cq.social.commons.annotation.Parameter(name = "message", value = "the new topic content.", required = true) })
@Component(immediate = true)
@Service
@Property(name = PostOperation.PROP_OPERATION_NAME, value = { "social:createJournal" })
public class JournalCreateOperation extends AbstractCommentOperation<JournalOperations> implements PostOperation {

	@Reference(target="(JournalOperationService=CustomJournalOperationService)")
	JournalOperations journalOperations;

	@Override
	protected SocialOperationResult performOperation(SlingHttpServletRequest request,
			Session session) throws OperationException {
		Resource post = getCommentOperationService().create(request, session);
	    return new SocialOperationResult(getSocialComponentForComment(post, request), "created", 201, post.getPath());
	}

	@Override
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
