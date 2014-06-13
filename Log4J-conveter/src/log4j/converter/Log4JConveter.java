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
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No input file specified by argument.");
            System.exit(4);
        }

        switch (args.length) {
            case 1:
                String fileExtension = null;
                try {
                    fileExtension = getFileExtension(args[0]);
                } catch (IllegalArgumentException e) {
                    System.exit(1);
                }
                switch (fileExtension) {
                    case "xml":
                        XMLToProperties xmlToProperties = null;
                        try {
                            xmlToProperties = new XMLToProperties(args[0]);
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, "input xml file does not exist", ex);
                            System.err.println("The input file passed as argument does not exist!");
                            System.exit(18);
                        }
                        try {
                            xmlToProperties.validate();
                        } catch (ParserConfigurationException | SAXException | IOException ex) {
                            logger.log(Level.SEVERE, "Cought exception while validating XML.", ex);
                            System.err.println("An error occured while validating input XML. XML is probably invalid: " + ex.getMessage());
                            System.exit(15);
                        }
                        try {
                            xmlToProperties.Convert();
                        } catch (TransformerException ex) {
                            Logger.getLogger(Log4JConveter.class.getName())
                                    .log(Level.SEVERE, "Exception while performing XSLT.", ex);
                            System.err.println("An error occured while transforming XML to properties: " + ex.getMessage());
                            System.exit(16);
                        }
                        break;
                    case "properties":
                        PropertiesToXML propertiesToXml = null;
                        try {
                            propertiesToXml = new PropertiesToXML(args[0]);
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, "input properties file does not exist", ex);
                            System.err.println("The input file passed as argument does not exist!");
                            System.exit(18);
                        }
                        try {
                            propertiesToXml.Convert();
                        } catch (IOException | ParserConfigurationException | TransformerException ex) {
                            Logger.getLogger(PropertiesToXML.class.getName()).log(Level.SEVERE, null, ex);
                            System.err.println("An error occured while converting properties to XML: " + ex.getMessage());
                            System.exit(17);
                        }
                        break;
                    default:
                        System.err.println("File name must end with .xml or .properties");
                        System.exit(2);
                }
                break;

            case 2:
                String inputFileExtension = null;
                String OutputFileExtensiou = null;

                try {
                    inputFileExtension = getFileExtension(args[0]);
                    OutputFileExtensiou = getFileExtension(args[1]);
                } catch (IllegalArgumentException e) {
                    System.exit(10);
                }

                if (inputFileExtension.equals("xml") && OutputFileExtensiou.equals("properties")) {
                    XMLToProperties xmlToProperties = null;
                    try {
                        xmlToProperties = new XMLToProperties(args[0]);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "input xml file does not exist", ex);
                        System.err.println("The input file passed as argument does not exist!");
                        System.exit(18);
                    }
                    try {
                        xmlToProperties.validate();
                    } catch (ParserConfigurationException | SAXException ex) {
                        logger.log(Level.SEVERE, "Cought exception while validating XML.", ex);
                        System.err.println("An error occured while validating input XML. XML is probably invalid: " + ex.getMessage());
                        System.exit(15);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "output properties could not be opened", ex);
                        System.err.println("Output file passed as argument could not be opened");
                        System.exit(19);
                    }
                    try {
                        xmlToProperties.Convert(args[1]);
                    } catch (TransformerException ex) {
                        Logger.getLogger(Log4JConveter.class.getName())
                                .log(Level.SEVERE, "Exception while performing XSLT.", ex);
                        System.err.println("An error occured while transforming XML to properties: " + ex.getMessage());
                        System.exit(16);
                    }
                    System.out.println("Conversion successfull!");
                } else if (inputFileExtension.equals("properties") && OutputFileExtensiou.equals("xml")) {
                    PropertiesToXML propertiesToXml = null;
                    try {
                        propertiesToXml = new PropertiesToXML(args[0]);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "input properties file does not exist", ex);
                        System.err.println("The input file passed as argument does not exist!");
                        System.exit(18);
                    }
                    try {
                        propertiesToXml.Convert(args[1]);
                    } catch (IOException | ParserConfigurationException | TransformerException ex) {
                        logger.log(Level.SEVERE, "Exception while converting properties to XML", ex);
                        System.err.println("An error occured while converting properties to XML: " + ex.getMessage());
                        System.exit(17);
                    }
                    System.out.println("Conversion successful!");
                } else {
                    System.err.println("Valid input:\nXML and properties or\nproperties and XML\n");
                    System.exit(20);
                }
                break;
        }

    }
}
