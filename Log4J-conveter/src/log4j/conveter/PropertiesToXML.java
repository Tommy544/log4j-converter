package log4j.conveter;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Heno
 * 
 * Converts Log4J properties to XML
 */
public class PropertiesToXML {
    String inputFile;
    
    /**
     * @param inputFile input file with Log4J properties
     * @throws java.io.IOException
     */
    public PropertiesToXML(String inputFile) throws IOException
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
     * @param outputFile Log4J XML output file
     */
    public void Convert(String outputFile)
    {
        // TODO
    }
    
}
