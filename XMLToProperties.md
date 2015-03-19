# XMLToProperties #

Class XMLToProperties converts from XML to Log4J properties


---


## Properties ##

### String inputFile ###
Input file path

### String xslFile ###
XSL file path


---


## Methods ##

### public XMLToProperties(String inputFile) ###
> Constructor
  * parameter inputFile: XML input file
  * throws java.io.IOException if inputFile does not exist

### public void validate() ###
> Validates XML file, should be called before Convert method
  * throws javax.xml.parsers.ParserConfigurationException
  * throws org.xml.sax.SAXException
  * throws java.io.IOException

### public void Convert() ###
> Converts and prints XML to standard output
  * throws javax.xml.transform.TransformerConfigurationException
  * throws javax.xml.transform.TransformerException

### public void Convert(String outputFile) ###
> Converts and prints XML to file
  * parameter outputFile: output file

  * throws javax.xml.transform.TransformerConfigurationException
  * throws javax.xml.transform.TransformerException