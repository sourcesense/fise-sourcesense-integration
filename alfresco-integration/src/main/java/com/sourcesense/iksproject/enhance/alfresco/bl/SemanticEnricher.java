package com.sourcesense.iksproject.enhance.alfresco.bl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sourcesense.iksproject.enhance.EnrichmentEnginesExecutor;
import com.sourcesense.iksproject.enhance.FISEServerEnrichmentEnginesExecutor;
import com.sourcesense.iksproject.enhance.Tag;

/**
 * Enrich an Alfresco document with tags extracted from the IKS Fise Engine.
 *
 * @author Piergiorgio Lucidi
 */
public class SemanticEnricher {

  private Log log = LogFactory.getLog(SemanticEnricher.class);
  private NodeService nodeService;
  private ContentService contentService;
  private TaggingService taggingService;

  private EnrichmentEnginesExecutor enrichmentEnginesExecutor;

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  public void setTaggingService(TaggingService taggingService) {
	this.taggingService = taggingService;
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
        Collection<Tag> tags = enrichmentEnginesExecutor.getTags(content);
        List<String> tagNames = new LinkedList<String>();
        for (Tag tag : tags) {
        	tagNames.add(tag.getContent());
        }
        log.debug("Adding tags " + tagNames + " to the node " + nodeRef);

        taggingService.addTags(nodeRef, tagNames);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }


  }

  private void initializeEnrichmentEnginesExecutor() {

    enrichmentEnginesExecutor = new FISEServerEnrichmentEnginesExecutor();

  }

}
