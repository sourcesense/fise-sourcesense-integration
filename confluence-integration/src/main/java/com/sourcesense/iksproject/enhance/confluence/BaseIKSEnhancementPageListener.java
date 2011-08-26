package com.sourcesense.iksproject.enhance.confluence;

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
import com.sourcesense.iksproject.enhance.EnrichmentEnginesExecutor;
import com.sourcesense.iksproject.enhance.Tag;
import com.sourcesense.iksproject.enhance.TagCleaner;

import java.util.List;

/**
 * Confluence page listener for IKS enhancement engine
 */
public class BaseIKSEnhancementPageListener implements EventListener {

  private LabelManager labelManager;

  private EnrichmentEnginesExecutor enrichmentEnginesExecutor;

  private TagCleaner tagCleaner;


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
        for (Tag tag : enrichmentEnginesExecutor.getTags(pageContent)) {
          tagCleaner.clean(tag); // clean tags from unwanted chars
          if (tag.getContent().length() > 2 && tag.getContent().length() < 255) {
            addTag(page, tag.getContent());
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

  @SuppressWarnings("unused")
  public void setEnrichmentEnginesExecutor(EnrichmentEnginesExecutor enrichmentEnginesExecutor) {
    this.enrichmentEnginesExecutor = enrichmentEnginesExecutor;
  }

  public void setTagCleaner(TagCleaner tagCleaner) {
    this.tagCleaner = tagCleaner;
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
