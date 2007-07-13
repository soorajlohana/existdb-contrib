/*
 * Parser.java
 *
 * Created on July 9, 2007, 9:05 PM
 *
 */

package resolver;

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
public class Parser {
    
    public final static String SAX_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    public final static String SAX_NAMESPACES = "http://xml.org/sax/features/namespaces";
    public final static String SAX_NAMESPACES_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    public final static String SAX_VALIDATION = "http://xml.org/sax/features/validation";
    public final static String SAX_VALIDATION_DYNAMIC = "http://apache.org/xml/features/validation/dynamic";
    
    public final static String FEATURES_VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";
    public final static String PROPERTIES_INTERNAL_GRAMMARPOOL = "http://apache.org/xml/properties/internal/grammar-pool";
    public final static String PROPERTIES_LOAD_EXT_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    public final static String PROPERTIES_ENTITYRESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    
//    public XMLGrammarPool grammarPool;
    public XMLCatalogResolver resolver;
    
    /** Creates a new instance of Parser */
    public Parser(String grammar) {
//        grammarPool=new XMLGrammarPoolImpl();
        try {
            resolver=new XMLCatalogResolver( new String[]{new File(grammar).toURL().toString()});
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    public XMLReader getReader() {
        
        XMLReader xmlReader = null;
        
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setValidating(true);
        saxFactory.setNamespaceAware(true);
        
        try {
            saxFactory.setFeature(SAX_NAMESPACES_PREFIXES, true);
            saxFactory.setFeature(SAX_VALIDATION, true);
            saxFactory.setFeature(SAX_VALIDATION_DYNAMIC,true);
            saxFactory.setFeature(FEATURES_VALIDATION_SCHEMA,true);
            saxFactory.setFeature(PROPERTIES_LOAD_EXT_DTD,true);
            //saxFactory.setFeature(Namespaces.SAX_NAMESPACES_PREFIXES, true);
            
            SAXParser saxParser = saxFactory.newSAXParser();
            
            // Setup grammar cache
//            saxParser.setProperty(PROPERTIES_INTERNAL_GRAMMARPOOL, grammarPool);
            
            // Setup catalog resolver
            saxParser.setProperty(PROPERTIES_ENTITYRESOLVER, resolver);
            
            xmlReader = saxParser.getXMLReader();
            
            
            
        } catch (Exception e) {
            // ignore: feature only recognized by xerces
            e.printStackTrace();
        }
        
        return xmlReader;
    }
    
    void parse(XMLReader reader, File in){
        
        MyErrorHandler myhandler = new MyErrorHandler();

        try {
            FileInputStream fis = new FileInputStream(in);
            
            reader.setErrorHandler( myhandler );
    
            InputSource source = new InputSource(fis);
            reader.parse(source);
            
            fis.close();
            
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println( ex.getMessage() );
            
        } finally {
            System.out.println( "isValid="+myhandler.isValid());
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Parser p = new Parser("personal/grammar/catalog.xml");
        
        System.out.println("#######1");
        XMLReader reader = p.getReader();
        p.parse(reader, new File("personal/personal-noNSL.xml") );  // NOK
        
        System.out.println("#######2");
        p = new Parser("personal/grammar/catalog.xml");
        reader = p.getReader();
        p.parse(reader, new File("personal/personal-SLabsolute.xml") );  // OK
        
        System.out.println("#######3");
        p = new Parser("personal/grammar/catalog.xml");
        reader = p.getReader();
        p.parse(reader, new File("personal/personal-SLrelative.xml") );  // OK
        
        
    }
    
}
