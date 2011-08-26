package com.sourcesense.iksproject.enhance.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TestCase for {@link MockedNamedEntityExtractionEnhancementEngine}
 */
public class MockedNamedEntityExtractionEnhancementTest {

  @Test
  public void testCreation() {
    try {
      /* this can seem useless but it's needed to test JVM heap allocation settings as
      the MockedNamedEntityExtractionEnhancementEngine is memory greedy
       */
      assertTrue(new MockedNamedEntityExtractionEnhancementEngine() != null);
    }
    catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }

}
