package eu.iksproject.fise.sourcesense.enhance;

/**
 * {@link SimpleTagCleaner} test case
 */
public class SimpleTagCleaner implements TagCleaner {

  @Override
  public void clean(Tag tag) {
    String cleanContent = tag.getContent().replaceAll("\\W", " ");
    tag.setContent(cleanContent);
  }
}
