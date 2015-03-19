# Usage #

The application is designed to be used as a command line application. The following options are available:
  1. User starts the application with one argument pointing to a .properties file
    * The application transforms .properties file to an XML syntax and prints the result to standard output
  1. User starts the application with one argument pointing to a .xml file
    * The application transforms .xml file to a .properties syntax and prints the result to standard output
  1. User starts the application with two arguments. The first argument points to an .properties file (input) and the second argument points to a desired .xml file (output)
    * The Application transforms .properties file to a .xml file and saves the result as a file (given by second argument)
  1. User starts the application with two arguments. The first argument points to an .xml file (input) and the second argument points to a desired .properties file (output)
    * The application transforms .xml file to a .properties file and saves the result as a file (given by second argument)


**Example:**

java -jar Log4j-converter input.xml [output.properties]

java -jar Log4j-converter input.properties [output.xml]

---


# Implementation #

The conversion from .properties to .xml is done using Document Object Model (DOM).

The conversion from .xml to .properties is done using Extensible Stylesheet Language Transformations (XSLT). The file is also checked for validity by DTD prior to transformations.

# Documentation #

## Java Packages used ##

  * java.io

  * java.util

  * java.util.logging

  * javax.xml.parsers

  * javax.xml.transform

  * javax.xml.transform.dom

  * javax.xml.transform.stream

  * org.w3c.dom

  * org.xml.sax

## Source classes ##

  * [Log4JConveter](Log4JConveter.md)

  * [XMLToProperties](XMLToProperties.md)

  * [PropertiesToXML](PropertiesToXML.md)

## Test classes ##

  * PropertiesToXMLTest

  * XMLToPropertiesTest

# Class Diagram #

![https://code.google.com/p/log4j-converter/source/browse/wiki/img/Log4J-converterCD.png](https://code.google.com/p/log4j-converter/source/browse/wiki/img/Log4J-converterCD.png)