package eu.iksproject.fise.sourcesense.enhance;

import eu.iksproject.fise.engines.opennlp.impl.NamedEntityExtractionEnhancementEngine;
import eu.iksproject.fise.sourcesense.enhance.utils.MockedNamedEntityExtractionEnhancementEngine;

import java.util.Collection;
import java.util.HashSet;

/**
 * Bunlde FISE OpenNLP NER inside Confluence but need much memory so use it with care :-)
 */
public class OpenNLPEngineEnrichmentEnginesExecutor implements EnrichmentEnginesExecutor {

  private NamedEntityExtractionEnhancementEngine entityExtractionEnhancementEngine;

  public OpenNLPEngineEnrichmentEnginesExecutor() {
    entityExtractionEnhancementEngine = new MockedNamedEntityExtractionEnhancementEngine();
  }

  @Override
  public Collection<Tag> getTags(String content) throws Exception {

    Collection<Tag> tags = new HashSet<Tag>();
    Collection<String> personNames = entityExtractionEnhancementEngine.extractPersonNames(content);
    Collection<String> organizationNames = entityExtractionEnhancementEngine.extractOrganizationNames(content);
    Collection<String> locationNames = entityExtractionEnhancementEngine.extractLocationNames(content);

    // TODO : use extractSOMETHINGNameOccurrences as a NameOccurence contains the 'confidence' too

    for (String name : personNames) {
      Tag t = new Tag(name);
      tags.add(t);
    }
    for (String name : organizationNames) {
      Tag t = new Tag(name);
      tags.add(t);
    }
    for (String name : locationNames) {
      Tag t = new Tag(name);
      tags.add(t);
    }
    return tags;
  }

}
