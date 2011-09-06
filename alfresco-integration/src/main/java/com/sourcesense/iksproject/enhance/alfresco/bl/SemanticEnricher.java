package com.sourcesense.iksproject.enhance.alfresco.bl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
	private NodeService nodeService;
	private ContentService contentService;
	private TaggingService taggingService;
	private DictionaryService dictionaryService;
	private ContentTransformerRegistry contentTransformerRegistry;
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

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setContentTransformerRegistry(ContentTransformerRegistry contentTransformerRegistry) {
		this.contentTransformerRegistry = contentTransformerRegistry;
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

		// Check for temp files
		if (!nodeService.exists(nodeRef)) {
			if (log.isDebugEnabled()) {
				log.debug("Node " + nodeRef + " does not exist anymore. Temp file?");
			}
			return Collections.emptyList();
		}
		
		// Check whether the content is already plain text
		ContentReader contentReader = contentService.getReader(nodeRef,
				ContentModel.PROP_CONTENT);
		String mimetype = contentReader.getMimetype();
		String content = null;
		if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(mimetype)) {
			// Get the content from node
			content = contentReader.getContentString();
		} else {
			// Otherwise transform it to text
			ContentTransformer transformer = 
				contentTransformerRegistry.getTransformer(mimetype, MimetypeMap.MIMETYPE_TEXT_PLAIN,
						new TransformationOptions());
			if (transformer == null) {
				if (log.isDebugEnabled()) {
					log.debug("No transformer from " + mimetype + " to text/plain.");
				}
				return Collections.emptyList();
			}
			log.debug("Transforming from " + mimetype + " to text/plain.");
			ContentWriter contentWriter = contentService.getTempWriter();
			contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
			contentWriter.setEncoding("UTF-8");
			contentWriter.setLocale(contentReader.getLocale());
			transformer.transform(contentReader, contentWriter);
			content = contentWriter.getReader().getContentString();
		}
		
		try {
			Collection<Tag> tags = enrichmentEnginesExecutor.getTags(content);
			final Set<String> tagSet = new HashSet<String>();
			String exp = (String) dictionaryService.
					getConstraint(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "filename")).
					getConstraint().getParameters().get("expression");
			for (Tag tag : tags) {
				// Remove illegal characters from tag names
				String tagName = tag.getContent();
				tagName = tagName.replaceAll(exp, "").toLowerCase();
				// Ignore tags shorter than 3 characters
				if (tagName.length() > 2) {
					tagSet.add(tagName);
				}
			}
			if (tagSet.isEmpty()) {
				if (log.isDebugEnabled()) {
					log.debug("No tags found for " + nodeRef);
				}
				return Collections.emptyList();
			}
			final List<String> tagNames = new ArrayList<String>(tagSet);
			if (log.isDebugEnabled()) {
				log.debug("Adding tags " + tagNames + " to " + nodeRef);
			}
			// We have to run as admin to manipulate tags
			return AuthenticationUtil.runAs(new RunAsWork<Collection<String>>() {

				@Override
				public Collection<String> doWork() throws Exception {
					taggingService.addTags(nodeRef, tagNames);
					return tagSet;
				}
			}, "admin");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return Collections.emptyList();
	}

}
