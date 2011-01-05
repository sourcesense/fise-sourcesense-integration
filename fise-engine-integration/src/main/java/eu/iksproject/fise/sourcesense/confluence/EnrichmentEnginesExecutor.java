package eu.iksproject.fise.sourcesense.confluence;

import java.util.Collection;

/**
 * An interface representing a tagger object to extract tags (labels) from text
 */
public interface EnrichmentEnginesExecutor {

  Collection<String> getTags(String content) throws Exception;

}
