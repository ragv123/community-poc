package com.ttnd.community.journal.poc;

/*import com.adobe.cq.social.commons.annotation.Endpoint;
import com.adobe.cq.social.commons.annotation.Parameters;
import com.adobe.cq.social.commons.comments.endpoints.AbstractCommentOperation;

import com.adobe.cq.social.commons.client.api.SocialComponent;
import com.adobe.cq.social.commons.client.api.SocialComponentFactory;
import com.adobe.cq.social.commons.client.api.SocialComponentFactoryManager;
import com.adobe.cq.social.commons.client.endpoints.OperationException;
import com.adobe.cq.social.commons.client.endpoints.SocialOperationResult;

import com.adobe.cq.social.forum.client.endpoints.ForumOperations;
import com.adobe.cq.social.journal.client.endpoints.JournalOperations;
import com.adobe.cq.social.scf.*;
import com.adobe.cq.social.scf.core.operations.AbstractSocialOperation;
//import com.ttn.communities.poc.endpoints.impl.JournalCommentCreateOperation;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.PostOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;*/

/*@Endpoint(name="JournalCommentCreateOperation", resourceType="scf/components/hbs/journal", description="Create a new journal entry or comment.", example="curl -X POST -H \"Accept:application/json\" -d \"subject=newEntry&message=test&id=nobot&:operation=social:createJournalComment\" -uaparker@geometrixx.info:aparkerhttp:hostname:port/path/to/journal.social.json")
@Parameters({@com.adobe.cq.social.commons.annotation.Parameter(name=":operation", value="social:createJournalComment", required=true), @com.adobe.cq.social.commons.annotation.Parameter(name="id", value="nobot", required=true), @com.adobe.cq.social.commons.annotation.Parameter(name="tags", value="Array of tag ids", required=true), @com.adobe.cq.social.commons.annotation.Parameter(name="_charset_", value="UTF-8 or equivalent to support double byte characters", required=false), @com.adobe.cq.social.commons.annotation.Parameter(name="subject", value="The new topic subject", required=true), @com.adobe.cq.social.commons.annotation.Parameter(name="message", value="the new topic content.", required=true)})
@Component(immediate = true)
@Service
@Property(name="sling.post.operation", value={"social:createJournalComment"})*/
public class CreateOperation {
//extends AbstractCommentOperation<JournalOperations> {

	
	
	/*public static final String CREATE_COMMENT_OPERATION = "social:createJournalComment";
	  private static final Logger LOG = LoggerFactory.getLogger(CreateOperation.class);
	  @Reference
	  private JournalOperations journalOperations;
	  
	  protected SocialOperationResult performOperation(SlingHttpServletRequest request, Session session)
	    throws OperationException
	  {
	    Resource post = getCommentOperationService().create(request, session);
	    LOG.info("post object",post);
	    SocialOperationResult result = new SocialOperationResult(getSocialComponentForComment(post, request), "created", 201, post.getPath());
	    LOG.info("result object",result);
	    return result;
	  }
	  
	  protected JournalOperations getCommentOperationService()
	  {
	    return this.journalOperations;
	  }
	  
	  protected void bindJournalOperations(JournalOperations paramJournalOperations)
	  {
	    this.journalOperations = paramJournalOperations;
	  }
	  
	  protected void unbindJournalOperations(JournalOperations paramJournalOperations)
	  {
	    if (this.journalOperations == paramJournalOperations) {
	      this.journalOperations = null;
	    }
	  }*/
	
	
	
	
	
	
	
	
	
	
    /*Logger logger = LoggerFactory.getLogger(CreateOperation.class);
    @Reference
    private ForumOperations forumService;

    @Reference
    private SocialComponentFactoryManager srf;

    @Override
    protected SocialOperationResult performOperation(SlingHttpServletRequest req) throws OperationException, OperationException {
        int likes = Integer.parseInt(req.getParameter("likes"));
        Map<String,Object> updates = new HashMap<String,Object>();
        updates.put("likes", ++likes);
        Resource updatedResource = forumService.update(req.getResource(), updates, null, req.getResourceResolver().adaptTo(Session.class));
        return new SocialOperationResult(this.getSocialComponentForResource(updatedResource, req), 200, updatedResource.getPath());
    }

    private SocialComponent getSocialComponentForResource(Resource newProject, SlingHttpServletRequest request) {
        if (newProject == null) {
            return null;
        }
        final SocialComponentFactory factory = this.srf.getSocialComponentFactory(newProject);
        return factory.getSocialComponent(newProject, request);
    }

	@Override
	protected JournalOperations getCommentOperationService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SocialOperationResult performOperation(SlingHttpServletRequest paramSlingHttpServletRequest,
			Session paramSession) throws OperationException {
		// TODO Auto-generated method stub
		return null;
	}*/

}
