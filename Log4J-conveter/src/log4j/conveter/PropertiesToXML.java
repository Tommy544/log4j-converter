package log4j.conveter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Heno
 * 
 * Converts Log4J properties to XML
 */
public class PropertiesToXML {
    String inputFile;
    
    /**
     * Document containing curent results
     */
    protected Document doc;
    
    /**
     * @param inputFile input file with Log4J properties
     * @throws java.io.IOException
     */
    public PropertiesToXML(String inputFile) throws IOException
    {
        if (! new File(inputFile).isFile())
            throw new IOException("Input file does not exist");
        
        this.inputFile = inputFile;
    }
    
    /**
     * Prints to Standard output
     */
    public void Convert()
    {
        try {
            // TODO
            
            StreamResult sr=new StreamResult(new StringWriter());
            doConversion(sr);
            StringWriter wr= (StringWriter) sr.getWriter();
            System.out.print(wr.getBuffer().toString());
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param outputFile Log4J XML output file
     */
    public void Convert(String outputFile)
    {
        // TODO
        File output= new File(outputFile);
        try (FileOutputStream fs = new FileOutputStream(output)) {
            StreamResult sr=new StreamResult(fs);
            doConversion(sr);
            fs.flush();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Properties loadProperties() throws IOException{
        InputStream source = new FileInputStream(inputFile);
        Properties props = new Properties();
        props.load(source);
        return props;
    }
    
    private Document createNewDocument() throws ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }
    /**
     * Napnlní result daty z dokumentu doc 
     */
    private StreamResult fillXMLResult(Document doc, StreamResult result) throws TransformerConfigurationException, TransformerException{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        
        //přidám referenci na DTD
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "log4j.dtd");
        transformer.transform(new DOMSource(doc), result);
        return result;
    }
    /**
     * 
     * @param properties    vstupní data
     * @param keys      seřazené klíče z properties
     * @param configurationElemnt   kořenový element dokumentu
     */
    private void convertLoggers(Properties properties, SortedSet<String> keys, Element configurationElemnt){
        final String prefix = "log4j.logger.";
        SortedSet<String> logerKeys=keys.tailSet(prefix);
        for(String key : logerKeys){
            if(!key.startsWith(prefix)) break;
            Element logger = doc.createElement("logger");
            String loggerName=key.substring(prefix.length());
            logger.setAttribute("name", loggerName);
            if(null != parseLogger(properties.getProperty(key), logger)){
                configurationElemnt.appendChild(logger);
            }
        }
    }
    
    /**
     * 
     * @param properties vstupní properties
     * @param keys seřazené klíče z properties
     * @param configurationElemnt kořenový element dokumentu
     */
    private void convertRoot(Properties properties, SortedSet<String> keys, Element configurationElemnt){
        final String rootPrefix = "log4j.rootLogger";
        Element root=null;
        
        //U prefixu logeru sice předpokládám jen jeden výskyt, a tak by tu cyklus
        //být nemusel, ale projistotu to zkontroluju
        for(String key :keys.tailSet(rootPrefix)){
            if(!key.startsWith(rootPrefix)) break;
            if(null != root){
                //TODO vyhodit vyjimku
                break;
            }
            root = doc.createElement("root");
            if(null != parseLogger(properties.getProperty(key), root)){
                configurationElemnt.appendChild(root);
            }
        }
    }
    /**
     * 
     * @param result    StreamResult do kterého se naplní výsledné xml
     */
    protected void doConversion(StreamResult result) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException{
        Properties properties = loadProperties();
        
        //vyrobým nový dokument s kořenovým elementem
        doc = createNewDocument();
        Element rootElement = doc.createElement("log4j:configuration");
        rootElement.setAttribute("xmlns:log4j", "http://jakarta.apache.org/log4j/");
        doc.appendChild(rootElement);
        
        SortedSet<String> keys = new TreeSet(properties.keySet());
        
        //zkonvertuju loggery
        convertLoggers(properties,keys,rootElement);
        
        //zkonvertuju root loger
        convertRoot(properties,keys,rootElement);
        
        //naplním výsledek
        fillXMLResult(doc, result);
    }

    /**
     * 
     * @param levelValue    surová hodnota z properties
     * @param logger    element loggeru
     */
    private Element parseLogger(String levelValue, Element logger) throws DOMException {
        Element level = doc.createElement("level");
        //Zatim pro jednoduchost zatim předpokládám ze log4j.rootLogger=level
        //TODO rozdělit na level a appenderNames
        level.setAttribute("value", levelValue);
        logger.appendChild(level);
        return logger;
    }
}
