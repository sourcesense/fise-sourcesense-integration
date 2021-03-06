package com.sourcesense.iksproject.enhance;

/**
 * A {@link TagCleaner} is responsible of removing/fixing/etc data in a {@link Tag}
 */
public interface TagCleaner {
  void clean(Tag tag);
}
