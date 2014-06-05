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
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
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

    private Log4JConveter converter;
    
    private Logger logger;
    private Formatter formatter;  
    private ByteArrayOutputStream out;
    private Handler handler;
    
    @Before
    public void setUp() {
        converter = new Log4JConveter();
        
        logger = Logger.getLogger(Log4JConveter.class.getName());
        formatter = new SimpleFormatter();
        out = new ByteArrayOutputStream();
        handler = new StreamHandler();
        logger.addHandler(handler);
    }
    
    @After
    public void tearDown() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongInputFileArgumentNoExtension() throws IOException {
        Log4JConveter.getFileExtension("test");
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
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
