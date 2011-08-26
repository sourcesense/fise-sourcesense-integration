package com.sourcesense.iksproject.enhance.alfresco.policies;

import com.sourcesense.iksproject.enhance.alfresco.bl.IKSFiseAlfrescoBl;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * This class contains the custom behaviour to execute the enrichment of
 * metadata using Fise
 * 
 * @author Piergiorgio Lucidi
 * @version $Id$
 * 
 */
public class FISEExtractAndEnrichContentPolicy implements
		NodeServicePolicies.OnCreateNodePolicy {

	private IKSFiseAlfrescoBl iksFiseAlfrescoBl;
	private ContentService contentService;
	private PolicyComponent policyComponent;
	
	public void setIksFiseAlfrescoBl(IKSFiseAlfrescoBl iksFiseAlfrescoBl) {
		this.iksFiseAlfrescoBl = iksFiseAlfrescoBl;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
	
	/**
     * Spring initilaise method used to register the policy behaviours
     */
    public void init()
    {
        // Register the policy behaviours
        this.policyComponent.bindClassBehaviour(
        						 QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                                 ContentModel.TYPE_CONTENT,
                                 new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    }

	@Override
	public void onCreateNode(ChildAssociationRef childAssociationRef) {
		NodeRef nodeRef = childAssociationRef.getChildRef();
		extractAndEnrichContent(nodeRef);
	}
	
	/**
	 * The enrichment will be executed only if the mimetype is one of the supported mimetypes for FISE
	 * @param nodeRef
	 */
	private void extractAndEnrichContent(NodeRef nodeRef){
		if(checkMimetype(nodeRef))
			iksFiseAlfrescoBl.extractAndEnrichContent(nodeRef);
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
	

}
