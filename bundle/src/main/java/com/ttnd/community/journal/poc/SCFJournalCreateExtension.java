package com.ttnd.community.journal.poc;
public class SCFJournalCreateExtension {
	
}
/*import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.journal.client.api.JournalEntryComment;
import com.adobe.cq.social.journal.client.endpoints.JournalOperationExtension;
import com.adobe.cq.social.scf.Operation;
import com.adobe.cq.social.scf.OperationException;

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
		
		if (ResourceUtil.isA(paramResource, "scf/components/hbs/journal")) {
			LOG.info("Approved Value" + paramMap);
        	LOG.info("User is : " + paramSession.getUserID());
        	Boolean isApproved = true;
        	paramMap.put("approved", isApproved);
            if(paramSession.getUserID().equals("admin")){
            	paramMap.put("approved", isApproved);
            }
		}
		
	}

	@Override
	public void afterAction(Operation paramOperation, Session paramSession, JournalEntryComment paramT,
			Map<String, Object> paramMap) throws OperationException {
		// TODO Auto-generated method stub
		
	}

}
*/