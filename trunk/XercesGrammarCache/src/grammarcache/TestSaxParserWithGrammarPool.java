/*
 * TestSaxParserWithGrammarPool.java
 *
 * Created on July 9, 2007, 9:05 PM
 *
 */
package grammarcache;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Example of the xerces issue with GrammarPool.
 *
 * This file parses the same xml document twice. When the GrammarPool is
 * coupled to the parser, the second parse always fails. 
 * If the cached grammar is removed from the cache before the third parse, all just
 * work fine again.
 *
 * @author wessels
 */
public class TestSaxParserWithGrammarPool {

    public final static String SAX_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    public final static String SAX_NAMESPACES = "http://xml.org/sax/features/namespaces";
    public final static String SAX_NAMESPACES_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    public final static String SAX_VALIDATION = "http://xml.org/sax/features/validation";
    public final static String SAX_VALIDATION_DYNAMIC = "http://apache.org/xml/features/validation/dynamic";
    public final static String FEATURES_VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";
    public final static String PROPERTIES_INTERNAL_GRAMMARPOOL = "http://apache.org/xml/properties/internal/grammar-pool";
    public final static String PROPERTIES_LOAD_EXT_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    public final static String PROPERTIES_ENTITYRESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    public XMLGrammarPool grammarPool;
    public XMLCatalogResolver resolver;

    /** 
     *  setup grammarpool and resolver 
     */
    public TestSaxParserWithGrammarPool() {
        
        try {
            grammarPool = new XMLGrammarPoolImpl();
            resolver = new XMLCatalogResolver(new String[]{new File("grammar/catalog.xml").toURL().toString()}  );
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** couple pool and resolver to parser */
    public void setupSaxParser(SAXParser saxParser) {
        try {
            // Setup grammar cache
            saxParser.setProperty(PROPERTIES_INTERNAL_GRAMMARPOOL, grammarPool);

            // Setup catalog resolver
            saxParser.setProperty(PROPERTIES_ENTITYRESOLVER, resolver);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    /** not needed, for testing purposes only */
    public void cleanSaxParser(SAXParser saxParser) {
        try {
            // Setup grammar cache
            saxParser.setProperty(PROPERTIES_INTERNAL_GRAMMARPOOL, null);

            // Setup catalog resolver
            saxParser.setProperty(PROPERTIES_ENTITYRESOLVER, null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    

    /** create brand new parser using factory */
    public SAXParser createSaxParser() {
        
        System.err.println("Create new sax parser");

        SAXParser saxParser = null;

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();

        try {
            saxFactory.setValidating(true);
            saxFactory.setNamespaceAware(true);
            saxFactory.setFeature(SAX_NAMESPACES_PREFIXES, true);
            saxFactory.setFeature(SAX_VALIDATION, true);
            saxFactory.setFeature(SAX_VALIDATION_DYNAMIC, true);
            saxFactory.setFeature(FEATURES_VALIDATION_SCHEMA, true);
            saxFactory.setFeature(PROPERTIES_LOAD_EXT_DTD, true);
            //saxFactory.setFeature(Namespaces.SAX_NAMESPACES_PREFIXES, true);

            saxParser = saxFactory.newSAXParser();
            
            setupSaxParser(saxParser);

        } catch (Exception e) {
            // ignore: feature only recognized by xerces
            e.printStackTrace();
        }

        return saxParser;
    }



    void parse(SAXParser parser, File in) {

        MyErrorHandler myhandler = new MyErrorHandler();

        try {
            XMLReader reader = parser.getXMLReader();

            FileInputStream fis = new FileInputStream(in);

            reader.setErrorHandler(myhandler);

            InputSource source = new InputSource(fis);
            reader.parse(source);

            fis.close();
            
            System.err.println("isValid=" + myhandler.isValid());

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        TestSaxParserWithGrammarPool txr = new TestSaxParserWithGrammarPool();

        // create parser
        SAXParser parser = txr.createSaxParser();

        /* First parse just works */
        System.err.println("#######1");
        txr.setupSaxParser(parser);
        txr.parse(parser, new File("dblp.xml"));  // OK

        /* Second parse fails */
        System.err.println("#######2");
        txr.parse(parser, new File("dblp.xml")); // Fail

        /* clean up and re-setup does not help */
        System.err.println("#######3");
        txr.cleanSaxParser(parser);
        parser.reset();
        txr.setupSaxParser(parser);
        txr.parse(parser, new File("dblp.xml"));  // OK 

        /* Fail expected, lost resolver */
        System.err.println("#######4");
        parser.reset();
        txr.parse(parser, new File("dblp.xml"));  // Fail 
        
        /* works because cache is clean again  */
        System.err.println("#######5");
        txr.grammarPool.clear();
        parser.reset();
        txr.setupSaxParser(parser);
        txr.parse(parser, new File("dblp.xml"));  // OK again 

    }
}
