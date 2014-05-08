package log4j.conveter;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Heno
 * 
 * Converts Log4J from XML to properties
 */
public class XMLToProperties {
    String inputFile;
    
    /**
     * @param inputFile Log4J XML input file
     * @throws java.io.IOException
     */
    public XMLToProperties(String inputFile) throws IOException
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
        // TODO
    }
    
    /**
     * @param outputFile output file with Log4J properties
     */
    public void Convert(String outputFile)
    {
        // TODO
    }
}
