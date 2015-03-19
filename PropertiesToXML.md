# PropertiesToXML #

Class PropertiesToXML converts Log4J properties to XML format.


---


## Properties ##

### String inputFile ###
> Input file path
### Document doc ###
> DOM document


---


## Methods ##

### public PropertiesToXML(String inputFile) ###
> Constructor
  * parameter inputFile: input file with Log4J properties

  * throws java.io.IOException if inputFile does not exist


### public void Convert() ###
> Convert and prints XML to standard output
  * throws java.io.IOException
  * throws javax.xml.parsers.ParserConfigurationException
  * throws javax.xml.transform.TransformerException


### public void Convert(String outputFile) ###
> Convert and prints XML to file
  * parameter outputFile: Log4J XML output file

  * throws java.io.IOException
  * throws javax.xml.parsers.ParserConfigurationException
  * throws javax.xml.transform.TransformerException