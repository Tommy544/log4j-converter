/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package log4j.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.xml.parsers.ParserConfigurationException;
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
}
