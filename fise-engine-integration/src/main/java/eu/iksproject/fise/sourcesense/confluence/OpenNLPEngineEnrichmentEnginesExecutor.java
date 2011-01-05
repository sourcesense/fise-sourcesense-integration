package eu.iksproject.fise.sourcesense.confluence;

import eu.iksproject.fise.engines.opennlp.impl.NamedEntityExtractionEnhancementEngine;
import eu.iksproject.fise.sourcesense.confluence.utils.MockedNamedEntityExtractionEnhancementEngine;

import java.util.Collection;
import java.util.HashSet;

/**
 * Bunlde FISE OpenNLP NER inside Confluence but need much memory so use it with care :-)
 *
 */
public class OpenNLPEngineEnrichmentEnginesExecutor implements EnrichmentEnginesExecutor {

  private NamedEntityExtractionEnhancementEngine entityExtractionEnhancementEngine;

  public OpenNLPEngineEnrichmentEnginesExecutor() {
    entityExtractionEnhancementEngine = new MockedNamedEntityExtractionEnhancementEngine();
  }

  @Override
  public Collection<String> getTags(String content) throws Exception {

    Collection<String> tags = new HashSet<String>();
    tags.addAll(entityExtractionEnhancementEngine.extractPersonNames(content));
    tags.addAll(entityExtractionEnhancementEngine.extractLocationNames(content));
    tags.addAll(entityExtractionEnhancementEngine.extractOrganizationNames(content));
    return tags;
  }

}
