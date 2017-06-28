package com.ttnd.community.journal.poc;

import com.adobe.cq.social.commons.comments.listing.CommentSocialComponentListProviderManager;
import com.adobe.cq.social.forum.client.api.AbstractForum;
import com.adobe.cq.social.forum.client.api.AbstractPost;
import com.adobe.cq.social.forum.client.api.ForumConfiguration;
import com.adobe.cq.social.forum.client.api.Post;
import com.adobe.cq.social.journal.client.api.Journal;
import com.adobe.cq.social.journal.client.api.JournalEntryComment;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Priyanku on 1/4/2016.
 */
public class JournalSocialComponent extends AbstractPost implements JournalEntryComment{

    private static final Logger LOG = LoggerFactory.getLogger(JournalSocialComponent.class);
    //private Tag techTag;
    //private List<Tag> tags;
    private ValueMap properties;
    Logger log = LoggerFactory.getLogger(JournalSocialComponent.class);

    /**
     * Construct a comment for the specified resource and client utilities.
     * @param resource the specified resource
     * @param clientUtils the client utilities instance
     * @param commentListProviderManager list manager to use for listing content
     * @throws RepositoryException if an error occurs
     */
    public JournalSocialComponent(final Resource resource, final ClientUtilities clientUtils,
                               final CommentSocialComponentListProviderManager commentListProviderManager) throws RepositoryException{
        super(resource, clientUtils, commentListProviderManager);
        //filterTags();
        this.properties = ResourceUtil.getValueMap(resource);
    }

    /**
     * Constructor of a comment.
     * @param resource the specified {@link com.adobe.cq.social.commons.Comment}
     * @param clientUtils the client utilities instance
     * @param queryInfo the query info.
     * @param commentListProviderManager list manager to use for listing content
     * @throws RepositoryException if an error occurs
     */
    public JournalSocialComponent(final Resource resource, final ClientUtilities clientUtils,
                               final QueryRequestInfo queryInfo, final CommentSocialComponentListProviderManager commentListProviderManager)
            throws RepositoryException {
        super(resource, clientUtils, queryInfo, commentListProviderManager);
        //filterTags();
        this.properties = ResourceUtil.getValueMap(resource);
    }
}
