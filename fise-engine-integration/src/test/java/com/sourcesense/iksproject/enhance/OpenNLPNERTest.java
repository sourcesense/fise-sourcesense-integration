package com.sourcesense.iksproject.enhance;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link OpenNLPEngineEnrichmentEnginesExecutor} TestCase
 */
public class OpenNLPNERTest {
  @Test
  public void testEnrichment() {
    try {
      OpenNLPEngineEnrichmentEnginesExecutor openNLPEngineEnrichmentEnginesExecutor = new OpenNLPEngineEnrichmentEnginesExecutor();
      Collection<Tag> tags = openNLPEngineEnrichmentEnginesExecutor.getTags("During the morning, Fabio Aiazzi (their " +
              "manager) asked me some information about Alfresco Records Management, because they would like to adopt" +
              " Alfresco to manage their internal invoicing process. This commercial module is implemented by Alfresco " +
              "for the U.S. law, but it is also a framework to implement other processes, for example for the italian " +
              "law. We don't have any experience on this");
      assertTrue(tags != null);
      assertTrue(tags.size() > 0);
      for (Tag tag : tags) {
        assertTrue(tag.getContent() != null && tag.getContent().length() > 0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }
}
