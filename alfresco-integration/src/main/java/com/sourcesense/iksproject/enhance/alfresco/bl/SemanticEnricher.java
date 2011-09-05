package com.sourcesense.iksproject.enhance.alfresco.bl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sourcesense.iksproject.enhance.EnrichmentEnginesExecutor;
import com.sourcesense.iksproject.enhance.Tag;

/**
 * Enrich an Alfresco document with tags extracted from the IKS Fise Engine.
 *
 * @author Piergiorgio Lucidi
 */
public class SemanticEnricher {

	private Log log = LogFactory.getLog(SemanticEnricher.class);
	private ContentService contentService;
	private TaggingService taggingService;
	private EnrichmentEnginesExecutor enrichmentEnginesExecutor;

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setTaggingService(TaggingService taggingService) {
		this.taggingService = taggingService;
	}

	public void setEnrichmentEnginesExecutor(EnrichmentEnginesExecutor enrichmentEnginesExecutor) {
		this.enrichmentEnginesExecutor = enrichmentEnginesExecutor;
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
	public Collection<String> extractAndEnrichContent(final NodeRef nodeRef) {

		//getting the content from node
		ContentReader contentReader = contentService.getReader(nodeRef,
				ContentModel.PROP_CONTENT);

		String mimetype = contentReader.getMimetype();
		String content = contentReader.getContentString();

		if (mimetype.startsWith("text/")) {
			try {
				Collection<Tag> tags = enrichmentEnginesExecutor.getTags(content);
				final List<String> tagNames = new LinkedList<String>();
				for (Tag tag : tags) {
					tagNames.add(tag.getContent());
				}
				// We have to run as admin to manipulate tags
				return AuthenticationUtil.runAs(new RunAsWork<Collection<String>>() {

					@Override
					public Collection<String> doWork() throws Exception {
						taggingService.addTags(nodeRef, tagNames);
						return tagNames;
					}
				}, "admin");
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
		}
		return Collections.emptyList();
	}

}
