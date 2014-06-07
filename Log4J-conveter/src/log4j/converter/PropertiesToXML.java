package log4j.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
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
    
    private void convertRenderes(Properties properties, SortedSet<String> keys, Element configurationElemnt){
        final String prefix = "log4j.renderer.";
        SortedSet<String> logerKeys=keys.tailSet(prefix);
        for(String key : logerKeys){
            if(!key.startsWith(prefix)) break;
            Element renderer = doc.createElement("renderer");
            String renderedClassName=key.substring(prefix.length());
            renderer.setAttribute("renderedClass", renderedClassName);
            renderer.setAttribute("renderingClass", properties.getProperty(key));
            configurationElemnt.appendChild(renderer);
        }
    }
    
    private void convertAppenders(Properties properties, SortedSet<String> keys, Element configurationElemnt){
        final String prefix = "log4j.appender.";
        SortedSet<String> logerKeys = keys.tailSet(prefix);
        Iterator<String> it = logerKeys.iterator();
        String key="";
        boolean unprocesedKey=false;
        while(unprocesedKey || it.hasNext()){
            if(!unprocesedKey) {
                key = it.next();
            }
            unprocesedKey=false;
            if(!key.startsWith(prefix)) break;
            String currentAppenderPrefix = key;
            Element appender = doc.createElement("appender");
            String appenderName=key.substring(prefix.length());
            appender.setAttribute(appenderName, properties.getProperty(key));
            
            while(it.hasNext()){
                key = it.next();
                if(!key.startsWith(currentAppenderPrefix)){
                    unprocesedKey=true;
                    break;
                }
                String bigestKey="";
                String lastKey;
//                processErrorHandler(properties,keys,currentAppenderPrefix,appender);
//                processParams(properties,keys,currentAppenderPrefix,appender);
//                processRollingPolicy(properties,keys,currentAppenderPrefix,appender);
//                processTriggeringPolicy(properties,keys,currentAppenderPrefix,appender);
//                processConnectionSource(properties,keys,currentAppenderPrefix,appender);
                lastKey=processLayout(properties,keys,currentAppenderPrefix,appender);
                if(lastKey.compareTo(bigestKey)>0){
                    bigestKey=lastKey;
                }
//                processFilters(properties,keys,currentAppenderPrefix,appender);
//                processAppenderRefs(properties,keys,currentAppenderPrefix,appender);
                it=keys.tailSet(bigestKey).iterator();
            }
            configurationElemnt.appendChild(appender);
        }
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
        
        //TODO otestovat renderery
        convertRenderes(properties,keys,rootElement);
        
        convertAppenders(properties, keys, rootElement);
        
        //zkonvertuju loggery
        //TODO additivity
        convertLoggers(properties,keys,rootElement);
        
        //zkonvertuju root loger
        convertRoot(properties,keys,rootElement);
        
        //naplním výsledek
        fillXMLResult(doc, result);
    }

    /**
     * 
     * @param logerValue    surová hodnota z properties
     * @param logger    element loggeru
     */
    private Element parseLogger(String logerValue, Element logger) throws DOMException {
        Element level = doc.createElement("level");
        //rozdělím logerValue na level a appenderNames
        String[] values=logerValue.split(",");
        String levelValue=values[0].toLowerCase();//v .propertie se používají velká písmena a v xml malá
        
        if(isStandartLevelValue(levelValue)){//standartní hodnotu jen přiřadím
            level.setAttribute("value", levelValue);
        }else{//jinak sparsuju custom level
            String[] customLevel=values[0].split("#");
            if(customLevel.length==2){
                level.setAttribute("class", customLevel[1]);
                level.setAttribute("value", customLevel[0]);
            }else{
                //TODO asi nějakou vyjímku
            }
        }
        logger.appendChild(level);
        for(int i=1; i<values.length; i++){
            Element appenderRef = doc.createElement("appender-ref");
            appenderRef.setAttribute("ref", values[i].trim());
            logger.appendChild(appenderRef);
        }
        return logger;
    }
    
    private boolean isStandartLevelValue(String levelValue){
        return levelValue.matches("all|trace|debug|info|warn|error|fatal|off|null");
    }

    private void processErrorHandler(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String processParams(Properties properties, SortedSet<String> keys, String prefix, Element element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        for(String key :keys.tailSet(prefix)){
//            if(!key.startsWith(prefix)) {
//                return key;
//            }
//            processParam(properties, key, prefix, element);
//        }
//        return null;
    }
    private void processParam(Properties properties, String key, String prefix, Element element){
            Element param = doc.createElement("param");
            param.setAttribute("name", key.substring(prefix.length()));//prefix neobsahuje tečku, proto +1
            param.setAttribute("value", properties.getProperty(key));
            element.appendChild(param);
    }

    private void processRollingPolicy(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void processTriggeringPolicy(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void processConnectionSource(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void processFilters(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String processLayout(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".layout";
        Element layout=null;
        for(String key :keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) {
                return key;
            }
            
            if(null==layout){
                if(key.equals(prefix)){
                    layout = doc.createElement("layout");
                    layout.setAttribute("class", properties.getProperty(key));
                    appender.appendChild(layout);
                }else{
                    //TODO nějakou vyjimku
                }
            }else{
                processParam(properties, key, prefix+".", layout);
            }
        }
        return null;
    }

    private void processAppenderRefs(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
