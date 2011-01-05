package eu.iksproject.fise.sourcesense.confluence.utils;

import org.apache.clerezza.rdf.core.*;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * RdfJsonParsingProvider with no SLF4J to avoid LinkageException when deployed inside Confluence
 *
 */
@SupportedFormat(SupportedFormat.RDF_JSON)
public class RDFJsonParsingProviderNoLog implements ParsingProvider {


  public void parse(MGraph target, InputStream stream, String formatIdentifier, UriRef baseUri) {

    JSONParser parser = new JSONParser();
    InputStreamReader reader = new InputStreamReader(stream);
    try {
      JSONObject root = (JSONObject) parser.parse(reader);
      Map<String, NonLiteral> subjects = createSubjectsFromJSONObjects(root);
      for (String keyString : subjects.keySet()) {
        NonLiteral key = subjects.get(keyString);
        JSONObject predicates = (JSONObject) root.get(keyString);
        addValuesToGraph(key, subjects, predicates, target);
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe.getMessage());
    } catch (ParseException pe) {
      throw new RuntimeException(pe.getMessage());
    }
  }

  private Map<String, NonLiteral> createSubjectsFromJSONObjects(
          JSONObject root) {
    Map<String, NonLiteral> subjectsAsJSONObjects = new HashMap<String, NonLiteral>();

    for (Object key : root.keySet()) {
      String keyString = (String) key;
      if (keyString.startsWith("_:")) {
        BNode bNode = new BNode();
        subjectsAsJSONObjects.put(keyString, bNode);
      } else {
        UriRef uri = new UriRef(keyString);
        subjectsAsJSONObjects.put(keyString, uri);
      }
    }
    return subjectsAsJSONObjects;
  }

  private void addValuesToGraph(NonLiteral key, Map<String, NonLiteral> subjects,
                                JSONObject predicates, MGraph mGraph) {
    for (Object predicate : predicates.keySet()) {
      JSONArray objects = (JSONArray) predicates.get(predicate);
      for (Object object : objects) {
        JSONObject values = (JSONObject) object;
        String value = (String) values.get("value");
        if (values.get("type").equals("literal")) {
          if (values.containsKey("datatype")
                  && !values.get("datatype").equals("")
                  && values.get("datatype") != null) {
            mGraph.add(new TripleImpl(key, new UriRef(
                    (String) predicate), LiteralFactory
                    .getInstance()
                    .createTypedLiteral(value)));
          } else if (values.containsKey("lang")
                  && !values.get("lang").equals("")
                  && values.get("lang") != null) {
            mGraph.add(new TripleImpl(key, new UriRef(
                    (String) predicate),
                    new PlainLiteralImpl(value,
                            new Language((String) values
                                    .get("lang")))));
          } else {
            mGraph.add(new TripleImpl(key, new UriRef(
                    (String) predicate),
                    new PlainLiteralImpl(value)));
          }
        } else if (values.get("type").equals("uri")) {
          mGraph.add(new TripleImpl(key, new UriRef(
                  (String) predicate), new UriRef(value)));
        } else if (values.get("type").equals("bnode")) {
          mGraph.add(new TripleImpl(key, new UriRef(
                  (String) predicate), subjects.get(value)));
        }
      }
    }
  }
}
