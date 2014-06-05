package log4j.converter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Heno
 *
 * Converts Log4J from XML to properties
 */
public class XMLToProperties {

    public static final Logger logger = Logger.getLogger(XMLToProperties.class.getName());
    private String inputFile;
    private boolean foundError = false;

    /**
     * Validates input XML file using official log4j.dtd
     */
    public void validate() {
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    System.err.println("Warning while validating input XML file:\nWarning:"
                            + exception.getMessage());
                    foundError = true;
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    System.err.println("Error while validating input XML file:\nError: "
                            + exception.getMessage());
                    foundError = true;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    System.err.println("Fatal Error while validating input XML file:\nFatal Error: "
                            + exception.getMessage());
                    foundError = true;
                }
            });
            Document doc = db.parse(inputFile);

            if (foundError) {
                System.exit(3);
            } else {
                logger.log(Level.INFO, "File validated successfuly.");
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.log(Level.SEVERE, "Cought exception while validating XML.", ex);
        }
    }

    /**
     * @param inputFile Log4J XML input file
     * @throws java.io.IOException
     */
    public XMLToProperties(String inputFile) throws IOException {
        if (!new File(inputFile).isFile()) {
            throw new IOException("Input file does not exist");
        }

        this.inputFile = inputFile;
    }

    /**
     * Prints to Standard output
     *
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public void Convert() throws TransformerConfigurationException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();

        System.out.println(tf.getClass());

        Transformer xsltProc = tf.newTransformer(
                new StreamSource(new File("src/log4j/converter/XMLToProperties.xsl")));

        xsltProc.transform(
                new StreamSource(new File(inputFile)),
                new StreamResult(System.out));
    }

    /**
     * @param outputFile output file with Log4J properties
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public void Convert(String outputFile) throws TransformerConfigurationException, TransformerException {

        // TODO
    }
}