/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package log4j.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void wrongInputFileArgumentNoExtension() throws IOException {
        Log4JConveter.getFileExtension("test");
    }
    
    @Test
    public void doConversion() {
        try {
            propertiesToXML = new PropertiesToXML("test/log4j/converter/resources/correctProperties.properties");
            StreamResult sr=new StreamResult(new StringWriter());
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
    
    @Test
    public void doConversionWrongInput() {
        try {
            propertiesToXML = new PropertiesToXML("test/log4j/converter/resources/nonExistant.properties");
            StreamResult sr=new StreamResult(new StringWriter());
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
