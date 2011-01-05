package eu.iksproject.fise.sourcesense.alfresco.bl;

import eu.iksproject.fise.sourcesense.confluence.EnrichmentEnginesExecutor;
import eu.iksproject.fise.sourcesense.confluence.OpenNLPEngineEnrichmentEnginesExecutor;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the business logic dedicated to exchange data between Alfresco
 * and the IKS Fise Engine.
 *
 * @author Piergiorgio Lucidi
 */
public class IKSFiseAlfrescoBl {

  private Log log = LogFactory.getLog(IKSFiseAlfrescoBl.class);
  private NodeService nodeService;
  private ContentService contentService;

  private EnrichmentEnginesExecutor enrichmentEnginesExecutor;

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * This is the main method that it will execute the following steps:
   * <p/>
   * 1. For each new node in the repository it sends the extracted content
   * from Alfresco to the FISE engine
   * <p/>
   * 2. Takes the response from FISE to enrich metadata for the current node
   * in Alfresco
   *
   * @param nodeRef
   */
  public void extractAndEnrichContent(NodeRef nodeRef) {

    //getting the content from node
    ContentReader contentReader = contentService.getReader(nodeRef,
            ContentModel.PROP_CONTENT);

    String mimetype = contentReader.getMimetype();
    String content = contentReader.getContentString();

    if (mimetype.startsWith("text/")) {
      if (enrichmentEnginesExecutor == null)
        initializeEnrichmentEnginesExecutor();
      try {
        log.debug("Integration with FISE: Connecting...");
        Collection<String> tags = enrichmentEnginesExecutor.getTags(content);

        log.debug("Saving new properties in the node: " + nodeRef);

        // save the new properties received from Fise
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(QName.createQName("tags"), tags.toArray());
        nodeService.setProperties(nodeRef, properties);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
      }
    }


  }

  private void initializeEnrichmentEnginesExecutor() {

    enrichmentEnginesExecutor = new OpenNLPEngineEnrichmentEnginesExecutor();

  }

}
