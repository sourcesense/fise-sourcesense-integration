package com.sourcesense.iksproject.enhance;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

import com.sourcesense.iksproject.enhance.utils.RDFJsonParsingProviderNoLog;

/**
 * EnrichmentEnginesExecutor that calls Nuxeo FISE demo instance
 */
public class FISEServerEnrichmentEnginesExecutor implements EnrichmentEnginesExecutor {

	private static final String SELECTED_TEXT_URI = "http://fise.iks-project.eu/ontology/selected-text";
	private static final String OBJECT_XMLSCHEMA_STRING = "^^<http://www.w3.org/2001/XMLSchema#string>";
	private static final String TEXT_PLAIN = "text/plain";

	private URL serverURL;

	public void setServerURL(URL serverURL) {
		this.serverURL = serverURL;
	}

	public Collection<Tag> getTags(String content) throws Exception {
		URLConnection urlConn = serverURL.openConnection();
		sendContent(content, urlConn);
		return extractTags(urlConn);
	}

	private Collection<Tag> extractTags(URLConnection urlConn) throws IOException {
		Collection<Tag> tags = new HashSet<Tag>();
		InputStream enrichedContentStream = null;
		try {
			enrichedContentStream = new BufferedInputStream(urlConn.getInputStream());

			RDFJsonParsingProviderNoLog provider = new RDFJsonParsingProviderNoLog();
			MGraph deserializedGraph = new SimpleMGraph();
			provider.parse(deserializedGraph, enrichedContentStream, SupportedFormat.RDF_JSON, null);


			UriRef entity = new UriRef(SELECTED_TEXT_URI);
			for (Iterator<Triple> it = deserializedGraph.iterator(); it.hasNext(); ) {
				Triple triple = it.next();
				if (triple.getPredicate().equals(entity)) {
					String labelExtracted = triple.getObject().toString();
					String finalLabel = labelExtracted.replace(OBJECT_XMLSCHEMA_STRING, "");
					Tag tag = new Tag(finalLabel.replaceAll("\\\"", ""));
					// TODO : add relevance, if presentt
					tags.add(tag);
				}
			}
		} finally {
			closeQuietly(enrichedContentStream);
		}

		return tags;
	}

	private void closeQuietly(InputStream enrichedContentStream) {
		if (enrichedContentStream != null) {
			try {
				enrichedContentStream.close();
			} catch (IOException e1) {
				enrichedContentStream = null;
			}
		}
	}

	private void sendContent(String pageContent, URLConnection urlConn) throws IOException {
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.addRequestProperty("Content-type", TEXT_PLAIN);
		urlConn.addRequestProperty("Accept", SupportedFormat.RDF_JSON);
		DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());
		String content = new StringBuffer("data=").append(pageContent).toString();
		printout.writeBytes(content);
		printout.flush();
		printout.close();
	}
}
