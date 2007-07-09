/*
 * Main.java
 *
 * Created on July 3, 2007, 11:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package existgrammarcache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Properties;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.exist.Namespaces;
import org.exist.util.XMLReaderObjectFactory;
import org.exist.util.serializer.SAXSerializer;
import org.exist.validation.GrammarPool;
import org.exist.validation.resolver.eXistXMLCatalogResolver;
import org.xml.sax.InputSource;
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
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Writer writer = null;
        try {
            //FileOutputStream fos = new FileOutputStream(out);
            fos = new FileOutputStream(out);
            bos= new BufferedOutputStream(fos);
            
            
            
            FileInputStream fis = new FileInputStream(in);
            
            writer = new OutputStreamWriter(bos, "UTF-8");
            
            Properties props = new Properties();
            SAXSerializer serializer = new SAXSerializer(writer, props);
            
            reader.setContentHandler(serializer);
            reader.setErrorHandler( report );
            
            logger.debug("Validation started.");
            
            InputSource source = new InputSource(fis);
            reader.parse(source);
            
            logger.debug("Validation stopped.");
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println( "isValid="+report.isValid());
        }
        
        
        try {
            writer.close();
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    /** Creates a new instance of Main */
    public Main() {
        grammarPool = new GrammarPool();
        //grammarPool = new XMLGrammarPoolImpl();
        resolver =  new eXistXMLCatalogResolver();
        
        File cat = new File("grammar/catalog.xml");
        try {
            resolver.setCatalogList( new String[]{cat.toURL().toString()});
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
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
        
        // DWES the trick **work around**
        // check grammarpoolimpl on http://www.jdocs.com/tab/12/org/apache/xerces/util/XMLGrammarPoolImpl.html
        try {
            GrammarPool gp = (GrammarPool) reader.getProperty(XMLReaderObjectFactory.PROPERTIES_INTERNAL_GRAMMARPOOL);
            if(gp!=null){
                gp.clearDTDs();
            }

        } catch (SAXNotRecognizedException ex) {
            ex.printStackTrace();
        } catch (SAXNotSupportedException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("#######2");
        reader = mn.getReader();
        
        mn.parse( reader, new File("dblp.xml"), new File("out2.dat"));
    }
    
}
