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
import java.util.Set;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
            
            appender.setAttribute("name",appenderName);
            appender.setAttribute("class", properties.getProperty(key));
            
            while(it.hasNext()){
                key = it.next();
                if(!key.startsWith(currentAppenderPrefix)){
                    unprocesedKey=true;
                    break;
                }
                String lastKey="";
                lastKey = mantainLastKey(processErrorHandler(properties,keys,currentAppenderPrefix,appender),lastKey);
                lastKey = mantainLastKey(processParams(properties,keys,currentAppenderPrefix,appender,1,new String[]{"",
                "errorHandler", //"rollingPolicy", "triggeringPolicy", "connectionSource",
                "layout", "filter", "appender-ref"}), lastKey);
//                lastKey = mantainLastKey(processRollingPolicy(properties,keys,currentAppenderPrefix,appender), lastKey);
//                lastKey = mantainLastKey(processTriggeringPolicy(properties,keys,currentAppenderPrefix,appender), lastKey);
//                lastKey = mantainLastKey(processConnectionSource(properties,keys,currentAppenderPrefix,appender), lastKey);
                lastKey = mantainLastKey(processLayout(properties,keys,currentAppenderPrefix,appender), lastKey);
                lastKey = mantainLastKey(processAppenderRefs(properties,keys,currentAppenderPrefix,appender), lastKey);
                lastKey = mantainLastKey(processFilters(properties,keys,currentAppenderPrefix,appender), lastKey);
                it=keys.tailSet(lastKey).iterator();
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
                String additivity=properties.getProperty("log4j.additivity."+loggerName);
                if(null!=additivity){
                    logger.setAttribute("additivity", additivity);
                }
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
    
    private void convertLoggerFactory(Properties properties, SortedSet<String> keys, Element configurationElemnt){
        final String prefix = "log4j.loggerFactory";
        Element factory=null;
        for(String key : keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) break;
            if(null==factory){
                factory = doc.createElement("loggerFactory");
                factory.setAttribute("class", key);
            }else{
                String paramPefix=prefix+".";
                if(!key.startsWith(paramPefix)) break;
                processParam(properties, key, paramPefix, factory);
            }
            
            
        }
        if(null!=factory){
            configurationElemnt.appendChild(factory);
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
        convertLoggers(properties,keys,rootElement);
        
        //zkonvertuju root loger
        convertRoot(properties,keys,rootElement);
        
        
        //zkonvertuju LoggerFactory
        convertLoggerFactory(properties, keys, rootElement);
        
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
    //<editor-fold defaultstate="collapsed" desc="Processing errorHandlers">
    
    private String processErrorHandler(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".errorhandler";
        Element hander;
        SortedSet myKeys=keys.tailSet(prefix);
        if(myKeys.isEmpty()) return null;
        if(myKeys.first().equals(prefix)){
            hander = doc.createElement("errorHandler");
            hander.setAttribute("class", properties.getProperty(keys.first()));
            appender.appendChild(hander);
        }else{
            return keys.first();
        }
            processRootRef(properties, myKeys, prefix, hander);
            processLoggerRefs(properties, myKeys, prefix, hander);
            processAppenderRefs(properties, myKeys, prefix, hander);
            processParams(properties, myKeys, prefix+".", hander, 0, new String[]{"",".root-ref",".logger-ref",".appender-ref"});
        return null;
    }
    
    private String processRootRef(Properties properties, SortedSet<String> keys, String handlerPrefix, Element element){
        String prefix=handlerPrefix+".root-ref";
        for(String key :keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) {
                return key;
            }
            if(properties.getProperty(key).equalsIgnoreCase("true")){
                element.appendChild(doc.createElement("root-ref"));
            }
        }
        return null;
    }
    
    private String processLoggerRefs(Properties properties, SortedSet<String> keys, String handlerPrefix, Element element){
        String prefix=handlerPrefix+".logger-ref";
        for(String key :keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) {
                return key;
            }
            Element ref=doc.createElement("logger-ref");
            ref.setAttribute("ref", properties.getProperty(key));
            element.appendChild(ref);
        }
        return null;
    }
    
    //</editor-fold>

    private String processParams(Properties properties, SortedSet<String> keys, String prefix, Element element, int position, String[] ignored) {
        NodeList childNodes = element.getChildNodes();
        Node refChild = childNodes.item(position);
        for(String key :keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) {
                return key;
            }
            String name=key.substring(prefix.length());
            boolean isParam=true;
            for(String s : ignored){
                if(key.startsWith(s)){
                    isParam=false;
                    continue;
                }
            }
            if(isParam) {
                Element param = doc.createElement("param");
                param.setAttribute("name", name);
                param.setAttribute("value", properties.getProperty(key));
                element.insertBefore(param, refChild);
            }
        }
        return null;
    }
    private void processParam(Properties properties, String key, String prefix, Element element){
            Element param = doc.createElement("param");
            param.setAttribute("name", key.substring(prefix.length()));
            param.setAttribute("value", properties.getProperty(key));
            element.appendChild(param);
    }

//    private String processRollingPolicy(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    private String processTriggeringPolicy(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    private String processConnectionSource(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    private String processFilters(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".filter";
        Iterator<String> it= keys.tailSet(prefix).iterator();
        boolean hasKey=false;
        String key="";
        while(hasKey || it.hasNext()){//TODO iteratory
            if(!hasKey) {
                key=it.next();
            }
            if(!key.startsWith(prefix)) {
                return key;
            }
            
            String filtrPrefix=key+".";
            Element filter = doc.createElement("filter");
            filter.setAttribute("class", properties.getProperty(key));
            appender.appendChild(filter);
            
            while(it.hasNext()){
                key=it.next();
                if(!key.startsWith(filtrPrefix)) {
                    hasKey=true;
                    break;
                }
                processParam(properties, key, filtrPrefix, filter);
            }
        }
        return null;
    
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

    
    private String processAppenderRefs(Properties properties, SortedSet<String> keys, String handlerPrefix, Element element){
        String prefix=handlerPrefix+".appender-ref";
        for(String key :keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) {
                return key;
            }
            Element ref=doc.createElement("appender-ref");
            ref.setAttribute("ref", properties.getProperty(key));
            element.appendChild(ref);
        }
        return null;
    }

    private String mantainLastKey(String newKey, String lastKey) {
        if(null==newKey) return lastKey;
        if(newKey.compareTo(lastKey)>0){
            return newKey;
        }else{
            return lastKey;
        }
    }
}
