/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package log4j.converter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Michal
 */
public class Log4JConveter {
    public static final Logger logger = Logger.getLogger(Log4JConveter.class.getName());

    private static String getFileExtension(String fileName) {
        String[] strings = fileName.split("\\.");
        if (strings.length <= 1) {
            System.err.println("File argument must end with .xml or .properties");
            logger.log(Level.SEVERE, "File argument did not end with .xml or .properties.");
            System.exit(1);
        }

        return strings[strings.length - 1];
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No input file specified by argument.");
            System.exit(4);
        }
        String fileExtension = getFileExtension(args[0]);
        switch (fileExtension) {
            case "xml":
                XMLToProperties xmlToProperties = new XMLToProperties(args[0]);
                xmlToProperties.validate();
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
