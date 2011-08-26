package com.sourcesense.iksproject.enhance.utils;

import org.apache.stanbol.enhancer.engines.autotagging.impl.ConfiguredAutotaggerProvider;
import org.apache.stanbol.enhancer.engines.opennlp.impl.NamedEntityExtractionEnhancementEngine;
import org.osgi.framework.*;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @deprecated Mock {@link NamedEntityExtractionEnhancementEngine} to be able to run it in a non-OSGi env
 */
public class MockedNamedEntityExtractionEnhancementEngine extends NamedEntityExtractionEnhancementEngine {
  public MockedNamedEntityExtractionEnhancementEngine() {
    Dictionary<String, Object> properties = new Hashtable<String, Object>();
    properties.put(ConfiguredAutotaggerProvider.LUCENE_INDEX_PATH, "");
    MockComponentContext context = new MockComponentContext(properties);
    try {
      this.activate(context);
    } catch (IOException e) {
      // do nothing
    }
  }

  

  private class MockComponentContext implements ComponentContext {
    protected final Dictionary<String, Object> properties;

    public MockComponentContext() {
      properties = new Hashtable<String, Object>();
    }

    public MockComponentContext(Dictionary<String, Object> properties) {
      this.properties = properties;
    }

    public void disableComponent(String name) {
    }

    public void enableComponent(String name) {
    }

    public BundleContext getBundleContext() {
      return new BundleContext() {

        @Override
        public boolean ungetService(ServiceReference reference) {
          return false;
        }

        @Override
        public void removeServiceListener(ServiceListener listener) {
        }

        @Override
        public void removeFrameworkListener(FrameworkListener listener) {
        }

        @Override
        public void removeBundleListener(BundleListener listener) {
        }

        @Override
        public ServiceRegistration registerService(String clazz,
                                                   Object service, Dictionary properties) {
          return null;
        }

        @Override
        public ServiceRegistration registerService(String[] clazzes,
                                                   Object service, Dictionary properties) {
          return null;
        }

        @Override
        public Bundle installBundle(String location, InputStream input)
                throws BundleException {
          return null;
        }

        @Override
        public Bundle installBundle(String location) throws BundleException {
          return null;
        }

        @Override
        public ServiceReference[] getServiceReferences(String clazz,
                                                       String filter) throws InvalidSyntaxException {
          return null;
        }

        @Override
        public ServiceReference getServiceReference(String clazz) {
          return null;
        }

        @Override
        public Object getService(ServiceReference reference) {
          return null;
        }

        @Override
        public String getProperty(String key) {
          return null;
        }

        @Override
        public File getDataFile(String filename) {
          return new File(System.getProperty("java.io.tmpdir"));
        }

        @Override
        public Bundle[] getBundles() {
          return null;
        }

        @Override
        public Bundle getBundle(long id) {
          return null;
        }

        @Override
        public Bundle getBundle() {
          return null;
        }

        @Override
        public ServiceReference[] getAllServiceReferences(String clazz,
                                                          String filter) throws InvalidSyntaxException {
          return null;
        }

        @Override
        public Filter createFilter(String filter)
                throws InvalidSyntaxException {
          return null;
        }

        @Override
        public void addServiceListener(ServiceListener listener,
                                       String filter) throws InvalidSyntaxException {

        }

        @Override
        public void addServiceListener(ServiceListener listener) {
        }

        @Override
        public void addFrameworkListener(FrameworkListener listener) {
        }

        @Override
        public void addBundleListener(BundleListener listener) {
        }
      };
    }

    public ComponentInstance getComponentInstance() {
      return null;
    }

    public Dictionary<String, Object> getProperties() {
      return properties;
    }

    public ServiceReference getServiceReference() {
      return null;
    }

    public Bundle getUsingBundle() {
      return null;
    }

    public Object locateService(String name) {
      return null;
    }

    public Object locateService(String name, ServiceReference reference) {
      return null;
    }

    public Object[] locateServices(String name) {
      return null;
    }
  }
}
