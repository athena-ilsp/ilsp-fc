package gr.ilsp.fmc.aligner.factory;

import gr.ilsp.fmc.aligner.IOtools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
//import java.nio.file.FileSystems;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;


public class AlignerFactoryExample {
	
	private static final Logger logger = LoggerFactory.getLogger(AlignerFactoryExample.class);

	public static void main(String[] args) throws IOException, ParserConfigurationException {

		AlignerFactory alignerFactory = new AlignerFactory();
		Aligner aligner = alignerFactory.getAligner("maligna");
		
        // Create example
        URL cesAlignListURL = new URL("http://nlp.ilsp.gr/xslt/ilsp-fc/output_MEDNET_EN-EL.txt");
        String basename = FilenameUtils.getBaseName(cesAlignListURL.toString());
        
        File tempDir = new File(FilenameUtils.concat(System.getProperty( "user.home" ),"public_html/aligner")); 
        tempDir.mkdir();
	    // java.nio.file.Files.createTempDirectory(
	    //		FileSystems.getDefault().getPath(FilenameUtils.concat(System.getProperty( "user.home" ),"public_html")), 
	    //		"aligner-").toFile();

        File cesAlignListFile =  new File (FilenameUtils.concat(tempDir.getAbsolutePath(), basename+".txt"));
        cesAlignListFile.deleteOnExit();
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        
        FileUtils.copyURLToFile(cesAlignListURL, cesAlignListFile);
        List<String> lines = FileUtils.readLines(cesAlignListFile);

        List<URL> tmxURLList = new ArrayList<URL>();
        List<URL> htmlTmxURLList = new ArrayList<URL>();

        String toReplace = FilenameUtils.concat(System.getProperty( "user.home" ),"public_html");
        String replacement = "http://10.1.1.38/~" + System.getProperty( "user.name" );
        
        for (String line : lines) {
        	logger.info(line);
            URL cesAlignURL = new URL(line);
            String cesAlignBasename = FilenameUtils.getBaseName(cesAlignURL.toString());
            File cesAlignFile = new File(FilenameUtils.concat(tempDir.toString(), cesAlignBasename+".xml"));
            cesAlignFile.deleteOnExit();
            FileUtils.copyURLToFile(cesAlignURL, cesAlignFile);
            logger.debug(cesAlignURL.toString());
            logger.debug(cesAlignFile.toString());

            List<URL> urlList = new ArrayList<URL>();
            List<String> basenameList = new ArrayList<String>();
            List<String> langsList = new ArrayList<String>();

            try {
                Document doc = dBuilder.parse(cesAlignFile);
                NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("translation");

                for(int i=0; i< translationNodes.getLength(); i++){
                    Element translationURLElement = (Element)translationNodes.item(i);
                    String url = translationURLElement.getAttribute("trans.loc");
                    if (!url.startsWith("http")) {
                           String pathToBasename = cesAlignURL.toString().substring(0, cesAlignURL.toString().lastIndexOf('/')+1);
                           url = pathToBasename + url;
                    }
                    urlList.add(new URL(url));
                    basenameList.add(FilenameUtils.getBaseName(url));
                    langsList.add(translationURLElement.getAttribute("xml:lang"));
                }

    			String sourceLang  = langsList.get(0);
    			String targetLang  = langsList.get(1);

    			File sourceFile = new File(FilenameUtils.concat(tempDir.toString(), basenameList.get(0) + ".xml"));;
    			File targetFile = new File(FilenameUtils.concat(tempDir.toString(), basenameList.get(1) + ".xml"));;
    			FileUtils.copyURLToFile(urlList.get(0), sourceFile);
    			FileUtils.copyURLToFile(urlList.get(1), targetFile);
    			sourceFile.deleteOnExit();
    			targetFile.deleteOnExit();

                logger.debug(sourceLang);
                logger.debug(" " + urlList.get(0));
                logger.debug(targetLang);
                logger.debug(" " + urlList.get(1));
                logger.debug(sourceFile.getAbsolutePath());
                logger.debug(targetFile.getAbsolutePath());
    			
    			File tmxFile = 	new File(FilenameUtils.concat(tempDir.toString(),  
    					basenameList.get(0)  + "_" + basenameList.get(1) + ".tmx"));
    			aligner.initialize(langsList.get(0), langsList.get(1));
    			aligner.setUseBoilerplateParagraphs(false);			
    			
    			if (aligner instanceof MalignaAligner) {
    				// Default values. See MalignaAligner class
    				((MalignaAligner) aligner).setCounter("word"); 
    				((MalignaAligner) aligner).setCalculator("poisson");
    				((MalignaAligner) aligner).setCls("viterbi");
    				((MalignaAligner) aligner).setSearch("iterative-band");		
    				logger.debug(((MalignaAligner) aligner).toString());

    			}
    			aligner.process(sourceFile, targetFile, tmxFile);
    			URL tmxURL = new URL(tmxFile.getAbsolutePath().replace(toReplace, replacement)); 
    			tmxURLList.add(tmxURL);
    			logger.debug("Exported results to tmx file " + tmxFile);
    			logger.debug("Exported results to tmx URL " + tmxURL);    
    			
    			File htmlTmxFile =   new File(FilenameUtils.removeExtension(tmxFile.getAbsolutePath()) + ".html");
    	        IOtools.tmxTOhtml(tmxFile.getAbsolutePath(), htmlTmxFile.getAbsolutePath());
    			URL htmlTmxURL = new URL(htmlTmxFile.getAbsolutePath().replace(toReplace, replacement)); 
    			htmlTmxURLList.add(htmlTmxURL);
    			logger.debug("Exported results to html tmx file " + htmlTmxFile);
    			logger.debug("Exported results to html tmx URL " + htmlTmxURL);    			
            } catch (Exception ex) {
            	logger.warn(ex.getMessage());
            }
		}
        
        File htmlTmxListFile = new File(FilenameUtils.concat(tempDir.toString(), basename+".tmx.html"));
        StringBuffer htmlTmxContent = new StringBuffer();
        	
        htmlTmxContent.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        for (URL htmlTmxURL : htmlTmxURLList){
        	htmlTmxContent.append("<br />"+"<a href=\""+htmlTmxURL.toString()+"\">\n"+htmlTmxURL.toString()+"</a>"+"\n");
    	}
        htmlTmxContent.append("</html>\n");

	PrintWriter writer = new PrintWriter(htmlTmxListFile, "UTF-8");
	writer.print(htmlTmxContent.toString());
	writer.close();
        //Files.write(Paths.get(htmlTmxListFile.toURI()), htmlTmxContent.toString().getBytes());
	
	logger.info("Created list of html tmx results in " + htmlTmxListFile.getAbsolutePath().replace(toReplace, replacement));    			
        
        tempDir.setReadable(true, false);
        tempDir.setExecutable(true, false);
        tempDir.setWritable(true, true);
	}

}




/*package gr.ilsp.fmc.aligner.factory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AlignerFactoryExample {
	
	private static final Logger logger = LoggerFactory.getLogger(AlignerFactoryExample.class);

	public static void main(String[] args) throws IOException, ParserConfigurationException {

		AlignerFactory alignerFactory = new AlignerFactory();
		Aligner aligner = alignerFactory.getAligner("maligna");
		
        // Create example
        URL cesAlignListURL = new URL("http://nlp.ilsp.gr/soaplab2-results/IELST_ilsp.ilsp_fbc_IELEN__375401cc.147669957fe._7fdf_output");
        String basename = FilenameUtils.getBaseName(cesAlignListURL.toString());
        
        File tempDir = java.nio.file.Files.createTempDirectory(
        		FileSystems.getDefault().getPath(FilenameUtils.concat(System.getProperty( "user.home" ),"public_html")), 
        		"aligner-").toFile();

        File cesAlignListFile =  new File (FilenameUtils.concat(tempDir.getAbsolutePath(), basename+".txt"));
        cesAlignListFile.deleteOnExit();
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        
        FileUtils.copyURLToFile(cesAlignListURL, cesAlignListFile);
        List<String> lines = FileUtils.readLines(cesAlignListFile);

        List<URL> tmxURLList = new ArrayList<URL>();

        String toReplace = FilenameUtils.concat(System.getProperty( "user.home" ),"public_html");
        String replacement = "http://localhost/~" + System.getProperty( "user.name" );
        
        for (String line : lines) {
            URL cesAlignURL = new URL(line);
            String cesAlignBasename = FilenameUtils.getBaseName(cesAlignURL.toString());
            File cesAlignFile = new File(FilenameUtils.concat(tempDir.toString(), cesAlignBasename+".xml"));
            cesAlignFile.deleteOnExit();
            FileUtils.copyURLToFile(cesAlignURL, cesAlignFile);
            logger.debug(cesAlignURL.toString());
            logger.debug(cesAlignFile.toString());

            List<URL> urlList = new ArrayList<URL>();
            List<String> basenameList = new ArrayList<String>();
            List<String> langsList = new ArrayList<String>();

            try {
                Document doc = dBuilder.parse(cesAlignFile);
                NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("translation");

                for(int i=0; i< translationNodes.getLength(); i++){
                    Element translationURLElement = (Element)translationNodes.item(i);
                    String url = translationURLElement.getAttribute("trans.loc");
                    if (!url.startsWith("http")) {
                           String pathToBasename = cesAlignURL.toString().substring(0, cesAlignURL.toString().lastIndexOf('/')+1);
                           url = pathToBasename + url;
                    }
                    urlList.add(new URL(url));
                    basenameList.add(FilenameUtils.getBaseName(url));
                    langsList.add(translationURLElement.getAttribute("xml:lang"));
                }

    			String sourceLang  = langsList.get(0);
    			String targetLang  = langsList.get(1);

    			File sourceFile = new File(FilenameUtils.concat(tempDir.toString(), basenameList.get(0) + ".xml"));;
    			File targetFile = new File(FilenameUtils.concat(tempDir.toString(), basenameList.get(1) + ".xml"));;
    			FileUtils.copyURLToFile(urlList.get(0), sourceFile);
    			FileUtils.copyURLToFile(urlList.get(1), targetFile);
    			sourceFile.deleteOnExit();
    			targetFile.deleteOnExit();

                logger.debug(sourceLang);
                logger.debug(" " + urlList.get(0));
                logger.debug(targetLang);
                logger.debug(" " + urlList.get(1));
                logger.debug(sourceFile.getAbsolutePath());
                logger.debug(targetFile.getAbsolutePath());
    			
    			File tmxFile = 	new File(FilenameUtils.concat(tempDir.toString(),  
    					basenameList.get(0)  + "_" + basenameList.get(1) + ".tmx"));
    			aligner.initialize(langsList.get(0), langsList.get(1));
    			aligner.setUseBoilerplateParagraphs(false);			
    			
    			if (aligner instanceof MalignaAligner) {
    				// Default values. See MalignaAligner class
    				((MalignaAligner) aligner).setCounter("word"); 
    				((MalignaAligner) aligner).setCalculator("poisson");
    				((MalignaAligner) aligner).setCls("viterbi");
    				((MalignaAligner) aligner).setSearch("iterative-band");		
    				logger.debug(((MalignaAligner) aligner).toString());

    			}
    			aligner.process(sourceFile, targetFile, tmxFile);
    			URL tmxURL = new URL(tmxFile.getAbsolutePath().replace(toReplace, replacement)); 
    			tmxURLList.add(tmxURL);
    			logger.info("Exported results to tmx file " + tmxFile);
    			logger.info("Exported results to tmx URL " + tmxURL);    			
    			
            } catch (Exception ex) {
            	logger.warn(ex.getMessage());
            }
		}
        
        tempDir.setReadable(true, false);
        tempDir.setExecutable(true, false);
        tempDir.setWritable(true, true);
	}

}
*/