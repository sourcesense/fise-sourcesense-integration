package eu.iksproject.fise.sourcesense.enhance;

import java.util.Collection;

/**
 * An interface representing a tagger object to extract tags (labels) from text
 */
public interface EnrichmentEnginesExecutor {

  Collection<Tag> getTags(String content) throws Exception;

}
