package log4j.converter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author Michal
 */
public class Log4JConveter {

    public static final Logger logger = Logger.getLogger(Log4JConveter.class.getName());
    
    /**
     * Gets file extension
     * 
     * @param fileName file name
     * 
     * @return file extension
     */
    public static String getFileExtension(String fileName) {
        String[] strings = fileName.split("\\.");
        if (strings.length <= 1) {
            System.err.println("File argument must end with .xml or .properties");
            logger.log(Level.SEVERE, "File argument did not end with .xml or .properties.");
            throw new IllegalArgumentException("fileName doesn't end with .xml or .properties");
        }

        return strings[strings.length - 1];
    }

    /**
     * Main method
     * 
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No input file specified by argument.");
            System.exit(4);
        }
        String fileExtension = null;
        try {
            fileExtension = getFileExtension(args[0]);
        } catch (IllegalArgumentException e) {
            System.exit(1);
        }
        switch (fileExtension) {
            case "xml":
                XMLToProperties xmlToProperties = new XMLToProperties(args[0]);
                try {
                    xmlToProperties.validate();
                } catch (ParserConfigurationException | SAXException ex) {
                    logger.log(Level.SEVERE, "Cought exception while validating XML.", ex);
                }
                try {
                    xmlToProperties.Convert();
                } catch (TransformerException ex) {
                    Logger.getLogger(Log4JConveter.class.getName())
                            .log(Level.SEVERE, "Exception while performing XSLT.", ex);
                }
                break;
            case "properties":
                PropertiesToXML propertiesToXml = new PropertiesToXML(args[0]);
                propertiesToXml.Convert();
                break;
            default:
                System.err.println("File name must end with .xml or .properties");
                System.exit(2);
        }

    }
}
