/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package log4j.converter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author vlado
 */
public class XMLToPropertiesTest {
    
    public static final Logger logger = Logger.getLogger(XMLToProperties.class.getName());
    
    private Log4JConveter converter;
    private XMLToProperties xmlToProperties;
    
//    private Formatter formatter;  
//    private ByteArrayOutputStream out;
//    private Handler handler;
    
    @Before
    public void setUp() {
        converter = new Log4JConveter();
        
        //logger = Logger.getLogger(Log4JConveter.class.getName());
//        formatter = new SimpleFormatter();
//        out = new ByteArrayOutputStream();
//        handler = new StreamHandler();
//        logger.addHandler(handler);
    }
    
    @After
    public void tearDown() {
        xmlToProperties = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongInputFileArgumentNoExtension() throws IOException {
        Log4JConveter.getFileExtension("test");
    }
    
    @Test
    public void correctXML() {
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/correctXML.xml");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "correctXML.xml not found", ex);
            fail("correctXML.xml file not found!");
        }
        try {
            xmlToProperties.validate();
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXMLTest.class.getName()).log(Level.SEVERE, "DTD check file not found", ex);
            fail("DTD check file not found!");
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "correctXML.xml ParserConfigurationExeption", ex);
            fail("correctXML.xml ParserConfigurationExeption!");
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, "correctXML.xml SAXException", ex);
            fail("correctXML.xml SAXException!");
        }
    }
    
    @Test
    public void noWellFormedXML() {
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/noWellFormedXML.xml");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "noWellFormedXML.xml file not found", ex);
            fail("noWellFormedXML.xml file not found!");
        }
        try {
            xmlToProperties.validate();
            fail("Test should fail");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXMLTest.class.getName()).log(Level.SEVERE, "DTD check file not found", ex);
            fail("DTD check file not found!");
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "noWellFormedXML.xml ParserConfigurationExeption", ex);
            fail("noWellFormedXML.xml ParserConfigurationExeption!");
        } catch (SAXException ex) {
            assertNotNull("The element type \"root\" must be terminated by the matching end-tag \"</root>\"", ex.getMessage());
        }
    }
    
    @Test
    public void wrongNamespace() {
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/wrongNamespace.xml");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "wrongNamespace.xml file not found", ex);
            fail("wrongNamespace.xml file not found!");
        }
        try {
            xmlToProperties.validate();
            fail("Test should fail");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXMLTest.class.getName()).log(Level.SEVERE, "DTD check file not found", ex);
            fail("DTD check file not found!");
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "wrongNamespace.xml ParserConfigurationExeption", ex);
            fail("wrongNamespace.xml ParserConfigurationExeption!");
        } catch (SAXException ex) {
            assertNotNull("Document root element \"log4j\", must match DOCTYPE root \"log4j:configuration\"", ex.getMessage());
        }
    }
    
    @Test
    public void multipleRootElements() {
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/multipleRootElements.xml");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "multipleRootElements.xml file not found", ex);
            fail("multipleRootElements.xml file not found!");
        }
        try {
            xmlToProperties.validate();
            fail("Test should fail");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXMLTest.class.getName()).log(Level.SEVERE, "DTD check file not found", ex);
            fail("DTD check file not found!");
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "multipleRootElements.xml ParserConfigurationExeption", ex);
            fail("multipleRootElements.xml ParserConfigurationExeption!");
        } catch (SAXException ex) {
            assertNotNull("The content of element type \"log4j:configuration\" must match \"(renderer*,appender*,plugin*,(category|logger)*,root?,(categoryFactory|loggerFactory)?)\".", ex.getMessage());
        }
    }
    
    @Test
    public void missingAttribute() {
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/missingAttribute.xml");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "missingAttribute.xml file not found", ex);
            fail("missingAttribute.xml file not found!");
        }
        try {
            xmlToProperties.validate();
            fail("Test should fail");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesToXMLTest.class.getName()).log(Level.SEVERE, "DTD check file not found", ex);
            fail("DTD check file not found!");
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "missingAttribute.xml ParserConfigurationExeption", ex);
            fail("missingAttribute.xml ParserConfigurationExeption!");
        } catch (SAXException ex) {
            assertNotNull("Attribute \"name\" is required and must be specified for element type \"appender\".", ex.getMessage());
        }
    }
    
    @Test
    public void testConvertRootWithLevel(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/rootWithLevel.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("rootWithLevel.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=INFO");
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    @Test
    public void testConvertRootWithoutLevel(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/rootWithoutLevel.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("rootWithoutLevel.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=file, stdout");
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    @Test
    public void testConvertConfigurationAttributes(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/configurationAttributes.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("configurationAttributes.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.debug=true");
            expectedProperties.add("log4j.threshold=all");
            expectedProperties.add("log4j.reset=true");
            expectedProperties.add("log4j.rootLogger=INFO");
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    @Test
    public void testConvertAppenders(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/appenders.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("appenders.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=INFO, file, stdout");
            expectedProperties.add("log4j.appender.file=org.apache.log4j.RollingFileAppender");
            expectedProperties.add("log4j.appender.file.errorHandler=org.jboss.logging.util.OnlyOnceErrorHandler");
            expectedProperties.add("log4j.appender.file.errorHandler.root-ref=true");
            expectedProperties.add("log4j.appender.file.errorHandler.logger-ref=lr");
            expectedProperties.add("log4j.appender.file.errorHandler.appender-ref=ar");
            expectedProperties.add("log4j.appender.file.errorHandler.ehparam=ehvalue");
            expectedProperties.add("log4j.appender.file.File=C:\\loging.log");
            expectedProperties.add("log4j.appender.file.MaxBackupIndex=1");
            expectedProperties.add("log4j.appender.file.MaxFileSize=1MB");
            expectedProperties.add("log4j.appender.file.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy");
            expectedProperties.add("log4j.appender.file.rollingPolicy.FileNamePattern=/applogs/myportal/portal.%d.log.gz");
            expectedProperties.add("log4j.appender.file.triggeringPolicy=tp");
            expectedProperties.add("log4j.appender.file.triggeringPolicy.tpparam=tpvalue");
            expectedProperties.add("log4j.appender.file.connectionSource=cs");
            expectedProperties.add("log4j.appender.file.connectionSource.dataSource=ds");
            expectedProperties.add("log4j.appender.file.connectionSource.dataSource.dsparam=dsvalue");
            expectedProperties.add("log4j.appender.file.connectionSource.csparam=csvalue");
            expectedProperties.add("log4j.appender.file.layout=org.apache.log4j.PatternLayout");
            expectedProperties.add("log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
            expectedProperties.add("log4j.appender.stdout=org.apache.log4j.ConsoleAppender");

            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    @Test
    public void testConvertRenderers(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/renderers.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("renderers.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=INFO, file, stdout");
            expectedProperties.add("log4j.renderer.com.acando.norolnes.MyClass=com.acando.norolnes.MyClassRenderer");
            expectedProperties.add("log4j.renderer.rendered=rendering");
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    @Test
    public void testConvertPlugin(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/plugin.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("plugin.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=INFO, file, stdout");
            expectedProperties.add("log4j.plugin.pluginname=pluginclass");
            expectedProperties.add("log4j.plugin.pluginname.connectionSource=cs");
            expectedProperties.add("log4j.plugin.pluginname.connectionSource.dataSource=ds");
            expectedProperties.add("log4j.plugin.pluginname.connectionSource.dataSource.dsparam=dsvalue");
            expectedProperties.add("log4j.plugin.pluginname.connectionSource.csparam=csvalue");
            
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    @Test
    public void testConvertLoggers(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/loggers.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("loggers.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=INFO, file, stdout");
            expectedProperties.add("log4j.logger.loggername=LEVELVALUE, ar");
            expectedProperties.add("log4j.logger.loggername2=LEVELVALUE2, ar, ar2");
            expectedProperties.add("log4j.logger.loggername3=ar, ar2");
            expectedProperties.add("log4j.loggerFactory=loggerfactory");
            expectedProperties.add("log4j.loggerFactory.lfparam=lfvalue");
            
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    @Test
    public void testConvertCategories(){
        try {
            xmlToProperties = new XMLToProperties("test/log4j/converter/resources/categories.xml");
        } catch (IOException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("categories.xml file not found!");
        }
        try {
            xmlToProperties.Convert("test/log4j/converter/resources/outputProperties.properties");
        } catch (TransformerException ex) {
            Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error during the transformation.");
        }
        
        try {
            BufferedReader output = new BufferedReader(new FileReader("test/log4j/converter/resources/outputProperties.properties"));
            String s;
           
            List<String> expectedProperties = new ArrayList<>();
            expectedProperties.add("log4j.reset=false");
            expectedProperties.add("log4j.rootLogger=INFO, file, stdout");
            expectedProperties.add("log4j.category.category=LEVELVALUE, ar");
            expectedProperties.add("log4j.category.category.categoryparam=categoryvalue");
            expectedProperties.add("log4j.category.category2=ar");
            expectedProperties.add("log4j.category.category2.categoryparam2=categoryvalue2");
            expectedProperties.add("log4j.category.category3=ar, ar2");
            expectedProperties.add("log4j.category.category3.categoryparam3=categoryvalue3");
            expectedProperties.add("log4j.category.category4=PRIORITYVALUE, ar, ar2");
            expectedProperties.add("log4j.category.category4.categoryparam4=categoryvalue4");
            expectedProperties.add("log4j.categoryFactory=categoryfactory");
            expectedProperties.add("log4j.categoryFactory.cfparam=cfvalue");
                        
            List<String> outputProperties = new ArrayList<>();
            
            while ((s = output.readLine()) != null) {
                if(!(s.isEmpty())){
                    outputProperties.add(s);
                }
            }
            output.close();          
            assertEquals(expectedProperties,outputProperties);
        } catch (IOException ex) {
                Logger.getLogger(XMLToPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
                fail("I/O error.");
        }  
                    
    };
    
    
        
    
}
