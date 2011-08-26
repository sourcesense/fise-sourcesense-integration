package com.sourcesense.iksproject.enhance;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * {@link SimpleTagCleaner} testCase
 */
public class SimpleTagCleanerTest {
  private SimpleTagCleaner tagCleaner;

  @Before
  public void setUp() {
    tagCleaner = new SimpleTagCleaner();
  }

  @Test
  public void testSimpleCleaning() {
    try {
      Tag tag = new Tag("pow^2");
      tagCleaner.clean(tag);
      assertEquals("pow 2", tag.getContent());
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }
}
