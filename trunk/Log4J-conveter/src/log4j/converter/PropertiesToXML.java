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
            StreamResult sr=new StreamResult(new StringWriter());
            doConversion(sr);
            StringWriter wr= (StringWriter) sr.getWriter();
            System.out.print(wr.getBuffer().toString());
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param outputFile Log4J XML output file
     */
    public void Convert(String outputFile)
    {
        File output= new File(outputFile);
        try (FileOutputStream fs = new FileOutputStream(output)) {
            StreamResult sr=new StreamResult(fs);
            doConversion(sr);
            fs.flush();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
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
    
    private Element convertConfig(Properties properties, SortedSet<String> keys){
        Element configurationElement = doc.createElement("log4j:configuration");
        configurationElement.setAttribute("xmlns:log4j", "http://jakarta.apache.org/log4j/");
        if(keys.contains("log4j.threshold")){
            configurationElement.setAttribute("threshold", properties.getProperty("log4j.threshold").toLowerCase());
        }
        
        if(keys.contains("log4j.debug")){
            configurationElement.setAttribute("debug", properties.getProperty("log4j.debug").toLowerCase());
        }
        return configurationElement;
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
                String lastKey=key;
                lastKey = mantainLastKey(processErrorHandler(properties,keys,currentAppenderPrefix,appender),lastKey);
                lastKey = mantainLastKey(processParams(properties,keys,currentAppenderPrefix,appender,1,new String[]{"",
                "errorhandler", "rollingPolicy", "triggeringPolicy", "connectionSource",
                "layout", "filter", "appender-ref"}), lastKey);
                //TODO otestovat tyhle tři
                lastKey = mantainLastKey(processRollingPolicy(properties,keys,currentAppenderPrefix,appender), lastKey);
                lastKey = mantainLastKey(processTriggeringPolicy(properties,keys,currentAppenderPrefix,appender), lastKey);
                lastKey = mantainLastKey(processConnectionSource(properties,keys,currentAppenderPrefix,appender), lastKey);
                //
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
        
        SortedSet<String> keys = new TreeSet(properties.keySet());
        
        Element configurationElement = convertConfig(properties, keys);
        doc.appendChild(configurationElement);
        
        //TODO otestovat renderery
        convertRenderes(properties,keys,configurationElement);
        
        convertAppenders(properties, keys, configurationElement);
        
        //zkonvertuju loggery
        convertLoggers(properties,keys,configurationElement);
        
        //zkonvertuju root loger
        convertRoot(properties,keys,configurationElement);
        
        
        //zkonvertuju LoggerFactory
        convertLoggerFactory(properties, keys, configurationElement);
        
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
            processParams(properties, myKeys, prefix, hander, 0, new String[]{"","root-ref","logger-ref","appender-ref"});
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

    private String processParams(Properties properties, SortedSet<String> keys, String basePrefix, Element element, int position, String[] ignored) {
        String prefix=basePrefix+".";
        NodeList childNodes = element.getChildNodes();
        Node refChild = childNodes.item(position);
        for(String key :keys.tailSet(prefix)){
            if(!key.startsWith(prefix)) {
                return key;
            }
            String name=key.substring(prefix.length());
            boolean isParam=true;
            for(String s : ignored){
                if(name.split("\\.")[0].equals(s)){
                    isParam=false;
                    break;
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

    private String processRollingPolicy(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".rollingPolicy";
        SortedSet<String> myKeys=keys.tailSet(prefix);
        if(myKeys.isEmpty()){
           return null; 
        }else{
            String key=myKeys.first();
            if(key.equals(prefix)){
                Element rollingPolicy= doc.createElement("rollingPolicy");
                rollingPolicy.setAttribute("class", properties.getProperty(key));
                appender.appendChild(rollingPolicy);
                return processParams(properties, keys, prefix, rollingPolicy, -1, new String[]{});
            }else{
                //nemam jak rozlišit jmeno od parametru =>
                return null;
            }
        }
    }

    private String processTriggeringPolicy(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".triggeringPolicy";
        SortedSet<String> myKeys=keys.tailSet(prefix);
        if(myKeys.isEmpty()){
           return null; 
        }else{
            String key=myKeys.first();
            if(key.equals(prefix)){
                Element triggeringPolicy = doc.createElement("triggeringPolicy");
                triggeringPolicy.setAttribute("class", properties.getProperty(key));
                String lastKey = key;
                lastKey = mantainLastKey(processFilters(properties, myKeys, prefix, triggeringPolicy), lastKey);
                lastKey = mantainLastKey(processParams(properties, keys, prefix, triggeringPolicy, -1, new String[]{"filter"}), lastKey);
                appender.appendChild(triggeringPolicy);
                return lastKey;
            }else{
                //nemam jak jednoduše rozlišit jmeno od parametru => pojmenovane ignoruju
                return null;
            }
        }
    }

    private String processConnectionSource(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".connectionSource";
        SortedSet<String> myKeys=keys.tailSet(prefix);
        if(myKeys.isEmpty()){
           return null; 
        }else{
            String key=myKeys.first();
            if(key.equals(prefix)){
                Element connectionSource = doc.createElement("connectionSource");
                connectionSource.setAttribute("class", properties.getProperty(key));
                String lastKey = key;
                lastKey = mantainLastKey(processDataSource(properties, myKeys, prefix, connectionSource), lastKey);
                lastKey = mantainLastKey(processParams(properties, keys, prefix, connectionSource, -1, new String[]{"filter"}), lastKey);
                appender.appendChild(connectionSource);
                return lastKey;
            }else{
                return key;
            }
        }
    }

    private String processDataSource(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".dataSource";
        SortedSet<String> myKeys=keys.tailSet(prefix);
        if(myKeys.isEmpty()){
           return null; 
        }else{
            String key=myKeys.first();
            if(key.equals(prefix)){
                Element dataSource = doc.createElement("dataSource");
                dataSource.setAttribute("class", properties.getProperty(key));
                String lastKey = key;
                lastKey = mantainLastKey(processParams(properties, keys, prefix, dataSource, -1, new String[]{}), lastKey);
                appender.appendChild(dataSource);
                return lastKey;
            }else{
                return key;
            }
        }
    }

    private String processFilters(Properties properties, SortedSet<String> keys, String currentAppenderPrefix, Element appender) {
        final String prefix=currentAppenderPrefix+".filter";
        Iterator<String> it= keys.tailSet(prefix).iterator();
        boolean hasKey=false;
        String key="";
        while(hasKey || it.hasNext()){
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
/**
 * 
 * @param newKey new key
 * @param lastKey currently biggest key
 * @return the biggest key
 */
    private String mantainLastKey(String newKey, String lastKey) {
        if(null==newKey) return lastKey;
        if(newKey.compareTo(lastKey)>0){
            return newKey;
        }else{
            return lastKey;
        }
    }
}
