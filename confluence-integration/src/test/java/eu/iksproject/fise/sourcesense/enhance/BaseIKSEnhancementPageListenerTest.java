package eu.iksproject.fise.sourcesense.enhance;

import com.sourcesense.iksproject.enhance.confluence.BaseIKSEnhancementPageListener;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link BaseIKSEnhancementPageListener} test case
 */
public class BaseIKSEnhancementPageListenerTest {
  private BaseIKSEnhancementPageListener listener;

  @Before
  public void seUp() {
    listener = new BaseIKSEnhancementPageListener();
  }

  @Test
  public void testHandledEvents() {
    try {
      Class[] handledEventClasses = listener.getHandledEventClasses();
      assertTrue(handledEventClasses.length == 4);
      for (Class c : handledEventClasses) {
        Class[] interfaces = c.getInterfaces();
        assertTrue(interfaces != null);
        for (Class i : interfaces) {
          assertTrue(i.getName().contains("Created") || i.getName().contains("Updated"));
        }
      }
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }
}
