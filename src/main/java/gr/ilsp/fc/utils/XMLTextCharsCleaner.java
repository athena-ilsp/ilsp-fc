package gr.ilsp.fc.utils;

import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;


public class XMLTextCharsCleaner {

	 private static final Logger logger = LoggerFactory.getLogger(XMLTextCharsCleaner.class);
     Tidy tidy = null;

     public static void main(String[] args) throws IOException  {
         XMLTextCharsCleaner app = new XMLTextCharsCleaner();
         File inputFile = new File(args[0]);
         File outputFile = new File(args[1]);
         logger.info("Started examining files:");
         logger.info("File1: " + inputFile.toString());
         logger.info("File2: " + outputFile.toString());
         app.initialize();
         app.xmlTextCharsClean(inputFile, outputFile);
     }

     private void initialize() {
         tidy = new Tidy();

         tidy.setInputEncoding(Constants.UTF8);
         tidy.setOutputEncoding(Constants.UTF8);
         tidy.setXmlTags(true);
         tidy.setWraplen(Integer.MAX_VALUE);
         tidy.setForceOutput(true) ;
         tidy.setXmlOut(true);
     }

     private void xmlTextCharsClean(File inputFile, File outputFile) throws IOException {
         InputStream inputStream = new FileInputStream(inputFile);
         OutputStream outputStream = new FileOutputStream(outputFile);
         tidy.parseDOM(inputStream, outputStream);
         outputStream.close();
         inputStream.close();
     }

	public static void clean(String infile, String outfile) {
		XMLTextCharsCleaner app = new XMLTextCharsCleaner();
        File inputFile = new File(infile);
        File outputFile = new File(outfile);
        logger.info("Started examining files:");
        logger.info("File1: " + inputFile.toString());
        logger.info("File2: " + outputFile.toString());
        app.initialize();
        try {
			app.xmlTextCharsClean(inputFile, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
