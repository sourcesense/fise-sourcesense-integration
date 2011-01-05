package eu.iksproject.fise.sourcesense.alfresco.actions;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import eu.iksproject.fise.sourcesense.alfresco.bl.IKSFiseAlfrescoBl;

/**
 * This class contains the custom action to execute the enrichment of metadata
 * using Fise. This action can be configured in the Alfresco
 * Explorer to set all the conditions and the content scope for contents using the Content Rules Wizard.
 * 
 * @author Piergiorgio Lucidi
 * @version $Id$
 * 
 */
public class FISEExtractAndEnrichContentAction extends
		ActionExecuterAbstractBase {

	private IKSFiseAlfrescoBl iksFiseAlfrescoBl;

	public void setIksFiseAlfrescoBl(IKSFiseAlfrescoBl iksFiseAlfrescoBl) {
		this.iksFiseAlfrescoBl = iksFiseAlfrescoBl;
	}

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		iksFiseAlfrescoBl.extractAndEnrichContent(nodeRef);
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> arg0) {

	}

}
