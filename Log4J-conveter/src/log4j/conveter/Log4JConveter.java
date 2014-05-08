/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package log4j.conveter;

import java.io.IOException;

/**
 *
 * @author Michal
 */
public class Log4JConveter {

    private static String getFileExtension(String fileName) {
        String[] strings = fileName.split("\\.");
        if (strings.length <= 1) {
            System.err.println("File argument must end with .xml or .properties");
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
                break;
            case "properties":
                PropertiesToXML propertiesToXml = new PropertiesToXML(args[0]);
                break;
            default:
                System.err.println("File name must end with .xml or .properties");
                System.exit(2);
        }

    }
}
