package eu.iksproject.fise.sourcesense.confluence;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostEvent;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.spring.container.ContainerManager;

import java.util.List;

/**
 * Confluence page listener for IKS enhancement engine
 *
 */
public class BaseIKSEnhancementPageListener implements EventListener {

  private LabelManager labelManager;

  private EnrichmentEnginesExecutor enrichmentEnginesExecutor;

  /**
   * setter for Spring based {@link LabelManager} DI
   *
   * @param labelManager handles {@link Label}s' creation
   */
  @SuppressWarnings("unused")
  public void setLabelManager(LabelManager labelManager) {
    this.labelManager = labelManager;
  }

  @SuppressWarnings("unchecked")
  public Class[] getHandledEventClasses() {
    return new Class[]{PageEvent.class, BlogPostEvent.class};
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
        throw new RuntimeException("unexpected event "+event.toString());


      enrichmentEnginesExecutor = (EnrichmentEnginesExecutor) ContainerManager.getComponent("enrichmentEnginesExecutor");

      String pageContent = page.getContent();
      if (pageContent != null && !"".equals(pageContent)) {
        for (String tag : enrichmentEnginesExecutor.getTags(pageContent)) {
          tag = cleanTag(tag); // clean tags from unwanted chars
          if (tag.length() > 2) {
            List<Label> pageLabels = page.getLabels();
            Label label = new Label(tag);
            if (!pageLabels.contains(label)) {
              if (labelManager.getLabel(label) == null)
                labelManager.createLabel(label);
              labelManager.addLabel(page, labelManager.getLabel(label));
            }
          }
        }

      }

    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private String cleanTag(String tag) {
    tag = tag.replaceAll("[-|_|,|:|;]"," ");
    return tag;
  }

  public void setEnrichmentEnginesExecutor(EnrichmentEnginesExecutor enrichmentEnginesExecutor) {
    this.enrichmentEnginesExecutor = enrichmentEnginesExecutor;
  }
}
