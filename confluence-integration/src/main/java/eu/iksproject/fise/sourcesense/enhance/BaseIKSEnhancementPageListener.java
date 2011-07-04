package eu.iksproject.fise.sourcesense.enhance;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostUpdateEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.spring.container.ContainerManager;

import java.util.List;

/**
 * Confluence page listener for IKS enhancement engine
 */
public class BaseIKSEnhancementPageListener implements EventListener {

  private LabelManager labelManager;

  private EnrichmentEnginesExecutor enrichmentEnginesExecutor;


  @SuppressWarnings("unchecked")
  public Class[] getHandledEventClasses() {
    return new Class[]{PageUpdateEvent.class, PageCreateEvent.class, BlogPostUpdateEvent.class, BlogPostCreateEvent.class};
  }

  public void handleEvent(Event event) {
    ContentEvent pageEvent = (ContentEvent) event;
    try {
      AbstractPage page;
      if (pageEvent instanceof PageEvent)
        page = ((PageEvent) pageEvent).getPage();
      else if (pageEvent instanceof BlogPostEvent)
        page = ((BlogPostEvent) pageEvent).getBlogPost();
      else
        throw new RuntimeException("unexpected event " + event.toString());


      enrichmentEnginesExecutor = (EnrichmentEnginesExecutor) ContainerManager.getComponent("enrichmentEnginesExecutor");

      String pageContent = page.getContent();
      if (pageContent != null && pageContent.length() > 0) {
        for (String tag : enrichmentEnginesExecutor.getTags(pageContent)) {
          tag = cleanTag(tag); // clean tags from unwanted chars
          if (tag.length() > 2 && tag.length() < 255) {
            addTag(page, tag);
          }
        }
      }
    } catch (Throwable e) {
      // do nothing - this should be error safe
    }
  }

  private void addTag(AbstractPage page, String tag) {
    try {
      List<Label> pageLabels = page.getLabels();
      Label label = new Label(tag);
      if (!pageLabels.contains(label)) {
        if (labelManager.getLabel(label) == null)
          labelManager.createLabel(label);
        labelManager.addLabel(page, labelManager.getLabel(label));
      }
    } catch (Exception e) {
      // a tag could not be added for some reason
    }
  }

  private String cleanTag(String tag) {
    return tag.replaceAll("\\W", " ");
  }

  @SuppressWarnings("unused")
  public void setEnrichmentEnginesExecutor(EnrichmentEnginesExecutor enrichmentEnginesExecutor) {
    this.enrichmentEnginesExecutor = enrichmentEnginesExecutor;
  }

  /**
   * setter for Spring based {@link LabelManager} DI
   *
   * @param labelManager handles {@link Label}s' creation
   */
  @SuppressWarnings("unused")
  public void setLabelManager(LabelManager labelManager) {
    this.labelManager = labelManager;
  }
}
