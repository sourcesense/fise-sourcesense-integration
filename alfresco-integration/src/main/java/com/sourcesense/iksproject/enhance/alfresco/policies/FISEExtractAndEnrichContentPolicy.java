package com.sourcesense.iksproject.enhance.alfresco.policies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sourcesense.iksproject.enhance.alfresco.bl.SemanticEnricher;

/**
 * This class contains the custom behaviour to execute the enrichment of
 * metadata using Fise
 * 
 */
public class FISEExtractAndEnrichContentPolicy implements
		NodeServicePolicies.OnCreateNodePolicy,
		ContentServicePolicies.OnContentUpdatePolicy {
	
    /** A key that keeps track of nodes that have been updated */
    private static final String KEY_UPDATED_NODES = FISEExtractAndEnrichContentPolicy.class.getName() + ".nodes";

	private SemanticEnricher iksFiseAlfrescoBl;
	private ContentService contentService;
	private TransactionService transactionService;
	private PolicyComponent policyComponent;
    private ThreadPoolExecutor threadExecutor;
    
    private TransactionListener transactionListener;

    private static Log logger = LogFactory.getLog(FISEExtractAndEnrichContentPolicy.class);

	public FISEExtractAndEnrichContentPolicy() {
        this.transactionListener = new SemanticEnricherTransactionListener();
	}
	
	public void setIksFiseAlfrescoBl(SemanticEnricher iksFiseAlfrescoBl) {
		this.iksFiseAlfrescoBl = iksFiseAlfrescoBl;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
	
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setThreadExecutor(ThreadPoolExecutor threadExecutor)
    {
        this.threadExecutor = threadExecutor;
    }
	
	/**
     * Spring initialization method used to register the policy behaviors
     */
    public void init()
    {
        // Register the policy behaviors
        this.policyComponent.bindClassBehaviour(
        						 NodeServicePolicies.OnCreateNodePolicy.QNAME,
                                 ContentModel.TYPE_CONTENT,
                                 new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(
				 				 ContentServicePolicies.OnContentUpdatePolicy.QNAME,
				 				 ContentModel.TYPE_CONTENT,
				 				 new JavaBehaviour(this, "onContentUpdate", NotificationFrequency.TRANSACTION_COMMIT));
    }

	@Override
	public void onCreateNode(ChildAssociationRef childAssociationRef) {
		NodeRef nodeRef = childAssociationRef.getChildRef();
        // Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
        // Get the set of nodes written
        @SuppressWarnings("unchecked")
        Set<NodeRef> updatedNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_UPDATED_NODES);
        if (updatedNodes == null)
        {
            updatedNodes = new HashSet<NodeRef>(5);
            AlfrescoTransactionSupport.bindResource(KEY_UPDATED_NODES, updatedNodes);
        }
        updatedNodes.add(nodeRef);
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
        // Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
        // Get the set of nodes written
        @SuppressWarnings("unchecked")
        Set<NodeRef> updatedNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_UPDATED_NODES);
        if (updatedNodes == null)
        {
            updatedNodes = new HashSet<NodeRef>(5);
            AlfrescoTransactionSupport.bindResource(KEY_UPDATED_NODES, updatedNodes);
        }
        updatedNodes.add(nodeRef);
	}
	
	private boolean checkMimetype(NodeRef nodeRef) {
		
		/**
		 * TODO What are all the supported mimetypes?
		 */
		
		ContentReader contentReader = contentService.getReader(nodeRef,
				ContentModel.PROP_CONTENT);
		String mimetype = contentReader.getMimetype();
		if (MimetypeMap.MIMETYPE_PDF.equals(mimetype)
				|| MimetypeMap.MIMETYPE_HTML.equals(mimetype)
				|| MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT.equals(mimetype)
				|| MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(mimetype))
			return true;
		else
			return false;
	}
    
    private class SemanticEnricherTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            @SuppressWarnings("unchecked")
            Set<NodeRef> nodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_UPDATED_NODES);
            if (nodeRefs != null) {
                for (NodeRef nodeRef : nodeRefs) {
                    Runnable runnable = new BackgroundTagger(nodeRef);
                    threadExecutor.execute(runnable);
                }
            }
        }
    }
	
    private class BackgroundTagger implements Runnable {
        private NodeRef nodeRef;
        
        private BackgroundTagger(NodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }
        
        /**
         * Get the tags from the IKS engine and tag the node
         */
        public void run() {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            RetryingTransactionCallback<Collection<String>> callback = new RetryingTransactionCallback<Collection<String>>() {
                public Collection<String> execute() throws Throwable {
                    try {
                		if (checkMimetype(nodeRef)) {
                			Collection<String> tags = iksFiseAlfrescoBl.extractAndEnrichContent(nodeRef);
                			if (logger.isDebugEnabled()) {
                				logger.debug("New tags for node " + nodeRef + ": " + tags);
                			}
                		}
                    	return Collections.emptyList();
                    }
                    finally {
                    }
                }
            };
            try
            {
                txnHelper.doInTransaction(callback, false, true);
            }
            catch (InvalidNodeRefException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Unable to update tags on missing node: " + nodeRef);
                }
            }
            catch (Throwable e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(e);
                }
                logger.error("Failed to update tags on node: " + nodeRef);
                // We are the last call on the thread
            }
        }
    }
    

}
