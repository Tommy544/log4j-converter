/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package log4j.converter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author vlado
 */
public class PropertiesToXMLTest {
    
    private static final Logger logger = Logger.getLogger(PropertiesToXML.class.getName());
    
    private Log4JConveter converter;
    private PropertiesToXML propertiesToXML;
    
    @Before
    public void setUp() {
        converter = new Log4JConveter();
    }
    
    @After
    public void tearDown() {
        propertiesToXML = null;
    }
    
    public Document getDocument(String input) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            
            @Override
            public void warning(SAXParseException e) throws SAXException {                
                System.out.println("Warning: ");  
                printInfo(e);
                fail("Warning SAXParseException: " + e);
            }

            @Override
            public void error(SAXParseException e) throws SAXException {     
                System.out.println("Error: ");  
                printInfo(e);
                fail("Error SAXParseException: " + e);
            }

            @Override
            public void fatalError(SAXParseException e)
                    throws SAXException {
                System.out.println("Fattal error: ");                
                printInfo(e);
                fail("Fatal Error SAXParseException: " + e);
            }

            private void printInfo(SAXParseException e) {
                System.out.println("   Public ID: " + e.getPublicId());
                System.out.println("   System ID: " + e.getSystemId());
                System.out.println("   Line number: " + e.getLineNumber());
                System.out.println("   Column number: " + e.getColumnNumber());
                System.out.println("   Message: " + e.getMessage());
            }
        });
        return builder.parse(input);
    }
    
    public XPath getXPath() {
        XPathFactory factory = XPathFactory.newInstance();
        return factory.newXPath();
    }
    
    public void doConversion(String input, String output) {
        try {
            propertiesToXML = new PropertiesToXML(input);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "file " + input + " not found", ex);
            fail("file " + input + " not found!");
        }
        try {
            propertiesToXML.Convert(output);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "file " + input + " not found", ex);
            fail("file " + output + " could not be created!");
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "ParserConfigurationException", ex);
            fail("Encountered ParserConfigurationException" + ex.getMessage());
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "TransformerException", ex);
            fail("Encountered TransformerException" + ex.getMessage());
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void wrongInputFileArgumentNoExtension() throws IOException {
        Log4JConveter.getFileExtension("test");
    }
    
    @Test
    public void doConversion() {
        try {
            propertiesToXML = new PropertiesToXML("test/log4j/converter/resources/correctProperties.properties");
            StreamResult sr = new StreamResult(new StringWriter());
            propertiesToXML.doConversion(sr);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "doConversion test failed: IOException", ex);
            fail("correctProperties.properties could not be opened!");
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "TransformerException", ex);
            fail("Encountered TransformerException: " + ex);
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "ParserConfigurationException", ex);
            fail("Encountered ParserConfigurationException: " + ex);
        }
    }
    
    //@Test
    public void doConversionWrongInput() {
        try {
            propertiesToXML = new PropertiesToXML("test/log4j/converter/resources/nonExistant.properties");
            StreamResult sr = new StreamResult(new StringWriter());
            propertiesToXML.doConversion(sr);
            fail("Test should fail!");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "doConversionWrongInput test passed: IOException", ex.getMessage());
            assertNotNull("IOException", ex.getMessage());
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "TransformerException", ex);
            fail("Encountered TransformerException: " + ex);
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "ParserConfigurationException", ex);
            fail("Encountered ParserConfigurationException: " + ex);
        }
    }
    
    @Test
    public void convertTestCorrectProperties() {
        
        Document doc = null;
        
        doConversion("test/log4j/converter/resources/correctProperties.properties", "test/log4j/converter/resources/outputXML.xml");
        
        try {
            doc = getDocument("test/log4j/converter/resources/outputXML.xml"); // also checks validity
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.log(Level.SEVERE, "Exception while getting Document", ex);
            fail("Exception while getting Document: " + ex);
        }
        
        XPath xPath = getXPath();
        XPathExpression expression;
        
        try {
            expression = xPath.compile("/configuration/appender/@class");
            assertEquals("org.apache.log4j.ConsoleAppender", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/@name");
            assertEquals("A1", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/@class");
            assertEquals("org.apache.log4j.PatternLayout", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param/@value");
            assertEquals("%d [%t] %-5p %c - %m%n", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param/@name");
            assertEquals("ConversionPattern", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger/@name");
            assertEquals("com.foo", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger/level/@value");
            assertEquals("warn", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/root/level/@value");
            assertEquals("debug", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/root/appender-ref/@ref");
            assertEquals("A1", (String) expression.evaluate(doc, XPathConstants.STRING));
        } catch (XPathExpressionException ex) {
            logger.log(Level.SEVERE, "XPathExpressionException", ex);
            fail("Encountered XPathExpressionException");
        }
    }
    
    @Test
    public void convertTestCorrectProperties1() {
        
        Document doc = null;
        
        doConversion("test/log4j/converter/resources/correctProperties1.properties", "test/log4j/converter/resources/outputXML.xml");
        
        try {
            doc = getDocument("test/log4j/converter/resources/outputXML.xml"); // also checks validity
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.log(Level.SEVERE, "Exception while getting Document", ex);
            fail("Exception while getting Document: " + ex);
        }
        
        XPath xPath = getXPath();
        XPathExpression expression;
        
        try {
            expression = xPath.compile("/configuration/appender[@class='org.apache.log4j.net.SyslogAppender']/@class");
            assertEquals("org.apache.log4j.net.SyslogAppender", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender[@name='A1']/@name");
            assertEquals("A1", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/param[@value='www.abc.net']/@value");
            assertEquals("www.abc.net", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/param[@name='SyslogHost']/@name");
            assertEquals("SyslogHost", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout[@class='org.apache.log4j.PatternLayout']/@class");
            assertEquals("org.apache.log4j.PatternLayout", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param[@value='%-4r %-5p %c{2} %M.%L %x - %m\n']/@value");
            assertEquals("%-4r %-5p %c{2} %M.%L %x - %m\n", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param[@name='ConversionPattern']/@name");
            assertEquals("ConversionPattern", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender[@class='org.apache.log4j.RollingFileAppender']/@class");
            assertEquals("org.apache.log4j.RollingFileAppender", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender[@name='A2']/@name");
            assertEquals("A2", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/param[@value='1']/@value");
            assertEquals("1", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/param[@name='MaxBackupIndex']/@name");
            assertEquals("MaxBackupIndex", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/param[@value='10MB']/@value");
            assertEquals("10MB", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/param[@name='MaxFileSize']/@name");
            assertEquals("MaxFileSize", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout[@class='org.apache.log4j.TTCCLayout']/@class");
            assertEquals("org.apache.log4j.TTCCLayout", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param[@value='enabled']/@value");
            assertEquals("enabled", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param[@name='ContextPrinting']/@name");
            assertEquals("ContextPrinting", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param[@value='ISO8601']/@value");
            assertEquals("ISO8601", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/appender/layout/param[@name='DateFormat']/@name");
            assertEquals("DateFormat", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger[@name='class.of.the.day']/@name");
            assertEquals("class.of.the.day", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger[@name='class.of.the.day']/level[@value='inherit']/@value");
            assertEquals("inherit", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger[@name='SECURITY']/@name");
            assertEquals("SECURITY", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger[@additivity='false']/@additivity");
            assertEquals("false", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger[@additivity='false']/level[@value='inherit']/@value");
            assertEquals("inherit", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger/appender-ref[@ref='A1']/@ref");
            assertEquals("A1", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger[@name='SECURITY.access']/@name");
            assertEquals("SECURITY.access", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/logger/level[@value='warn']/@value");
            assertEquals("warn", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/root/level[@value='debug']/@value");
            assertEquals("debug", (String) expression.evaluate(doc, XPathConstants.STRING));
            expression = xPath.compile("/configuration/root/appender-ref[@ref='A2']/@ref");
            assertEquals("A2", (String) expression.evaluate(doc, XPathConstants.STRING));
        } catch (XPathExpressionException ex) {
            logger.log(Level.SEVERE, "XPathExpressionException", ex);
            fail("Encountered XPathExpressionException");
        }
    }

//    @Rule 
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
//    
//    @Test
//    public void wrongInputFileArgumentWrongExtension() throws IOException {
//        converter.main(new String[]{"test.prop"});
//        
//        handler.flush();
//        String logMsg = out.toString();
//        assertEquals("File argument must end with .xml or .properties", logMsg);
//    }
}
