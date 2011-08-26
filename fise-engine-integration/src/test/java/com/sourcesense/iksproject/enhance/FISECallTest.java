package com.sourcesense.iksproject.enhance;

import com.sourcesense.iksproject.enhance.utils.RDFJsonParsingProviderNoLog;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URLConnection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test Call to Nuxeo FISE demo instance
 * note that this is not a real test for this project, rather a test for FISE
 *
 */
public class FISECallTest {

  public final String content = "During the morning, Fabio Aiazzi (their manager) asked me some information about Alfresco Records Management, because they would like to adopt Alfresco to manage their internal invoicing process. This commercial module is implemented by Alfresco for the U.S. law, but it is also a framework to implement other processes, for example for the italian law. We don't have any experience on this \n" +
          "\n" +
          "I also mentioned to him an italian open source project named Sinekarta:\n" +
          "\n" +
          "http://www.sinekarta.org/\n" +
          "\n" +
          "This is an Alfresco extension to manage documents accorting to the italian law. I think that we have to take a look at it \n" +
          "\n" +
          "I have to wait the next week to finish the JDBC connection for Stealth because their DBA is working on the new database for Alfresco. They also sent me the JDBC driver for DB2 5.4.0.\n" +
          "\n" +
          "So today I finished the custom action implemented with a mock Stealth business logic. \n" +
          "In this way I tested the action implementation on Alfresco.\n" +
          "\n" +
          "They sent me the VPN account with the installer for Windows 7 64bit. \n" +
          "Tomorrow I have to:\n" +
          "\n" +
          "I will install it using VirtualBox to test the VPN\n" +
          "finish the advanced search form for Ufficio Acquisti";

  @Test
  public void testFISECall() {
    try {
      URI fiseURI = URI.create("http://fise.demo.nuxeo.com:8080/engines/");
      URLConnection urlConn = fiseURI.toURL().openConnection();
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);
      urlConn.setUseCaches(false);
      urlConn.addRequestProperty("Content-type", "text/plain");
      urlConn.addRequestProperty("Accept", SupportedFormat.RDF_JSON);
      OutputStreamWriter printout = new OutputStreamWriter(urlConn.getOutputStream());
      String content = "data=" + this.content;
      printout.write(content);
      printout.flush();
      printout.close();

      urlConn.connect();
      InputStream enrichedContentStream = new BufferedInputStream(urlConn.getInputStream());
      RDFJsonParsingProviderNoLog provider = new RDFJsonParsingProviderNoLog();
      MGraph deserializedGraph = new SimpleMGraph();
      provider.parse(deserializedGraph,enrichedContentStream, SupportedFormat.RDF_JSON, null);

      assertTrue(deserializedGraph.size() > 0);
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }

  }

}

