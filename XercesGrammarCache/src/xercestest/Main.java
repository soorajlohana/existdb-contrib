/*
 * Main.java
 *
 * Created on July 3, 2007, 11:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xercestest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.exist.Namespaces;
import org.exist.util.XMLReaderObjectFactory;
import org.exist.util.serializer.SAXSerializer;
import org.exist.validation.GrammarPool;
import org.exist.validation.resolver.eXistXMLCatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 *
 * @author dwessels
 */
public class Main {
    
    private final static Logger logger = Logger.getLogger(Main.class);
    
    private static  XMLGrammarPool grammarPool;
    private static  eXistXMLCatalogResolver resolver;
    
    public XMLReader getReader() {
        
        XMLReader xmlReader = null;
        
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setValidating(true);
        saxFactory.setNamespaceAware(true);
        
        try {
            saxFactory.setFeature(Namespaces.SAX_NAMESPACES_PREFIXES, true);
            // TODO check does this work?
            // http://xerces.apache.org/xerces2-j/features.html
            saxFactory.setFeature(Namespaces.SAX_VALIDATION, true);
            
            saxFactory.setFeature(Namespaces.SAX_VALIDATION_DYNAMIC,true);
            
            saxFactory.setFeature(XMLReaderObjectFactory.FEATURES_VALIDATION_SCHEMA,true);
            
            saxFactory.setFeature(XMLReaderObjectFactory.PROPERTIES_LOAD_EXT_DTD,true);
            
            // Attempt to make validation function equal to inser mode
            //saxFactory.setFeature(Namespaces.SAX_NAMESPACES_PREFIXES, true);
            
            SAXParser saxParser = saxFactory.newSAXParser();
            
            // Setup grammar cache
            saxParser.setProperty(XMLReaderObjectFactory.PROPERTIES_INTERNAL_GRAMMARPOOL, grammarPool);
            
            xmlReader = saxParser.getXMLReader();
            
            // Setup catalog resolver
            xmlReader.setProperty(XMLReaderObjectFactory.PROPERTIES_ENTITYRESOLVER, resolver);
            
        } catch (Exception e) {
            // ignore: feature only recognized by xerces
            e.printStackTrace();
        }
        
        return xmlReader;
    }
    
    void parse(XMLReader reader, File in, File out){
        MyErrorHandler report = new MyErrorHandler();
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            //FileOutputStream fos = new FileOutputStream(out);
            fw = new FileWriter(out);
            bw= new BufferedWriter(fw);
            
            FileInputStream fis = new FileInputStream(in);
            
            Properties props = new Properties();
            SAXSerializer serializer = new SAXSerializer(bw, props);
            
            reader.setContentHandler(serializer);
            reader.setErrorHandler( report );
            
            logger.debug("Validation started.");
            
            InputSource source = new InputSource(fis);
            reader.parse(source);
            
            // DWES the trick
//            reader.setProperty(XMLReaderObjectFactory.PROPERTIES_INTERNAL_GRAMMARPOOL, null);
            
            logger.debug("Validation stopped.");
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println( "isValid="+report.isValid());
        }
        
        
        try {
            bw.close();
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    /** Creates a new instance of Main */
    public Main() {
        grammarPool = new GrammarPool();
        //grammarPool = new XMLGrammarPoolImpl();
        resolver =  new eXistXMLCatalogResolver();
        resolver.setCatalogList( new String[]{"grammar/catalog.xml"});
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        BasicConfigurator.configure();
        Main mn = new Main();
        
        System.out.println("#######1");
        XMLReader reader = mn.getReader();
        mn.parse( reader, new File("dblp.xml"), new File("out1.dat"));
           
        System.out.println("#######2");
        reader = mn.getReader();
                
        mn.parse( reader, new File("dblp.xml"), new File("out2.dat"));
    }
    
}
