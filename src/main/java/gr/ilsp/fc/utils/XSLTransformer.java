package gr.ilsp.fc.utils;

//import gr.ilsp.fc.main.Crawl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class XSLTransformer {
	private static Logger logger = Logger.getLogger(XSLTransformer.class);
	private TransformerFactory transFact = null;
	private Transformer transformer= null;
	private String baseDir= null;
	private static String  tempFileExt = ".xml.html";
	private Source xsltSource ;
	private Templates cachedXSLT ;
	public XSLTransformer() throws MalformedURLException, IOException, TransformerConfigurationException {
		xsltSource = new StreamSource(new URL("http://nlp.ilsp.gr/xslt/ilsp-fc/cesDoc.xsl").openStream());
	    transFact = TransformerFactory.newInstance();
	    cachedXSLT = transFact.newTemplates(xsltSource);
	}

	public XSLTransformer(String urlStr) throws MalformedURLException, IOException, TransformerConfigurationException {
		xsltSource = new StreamSource(new URL(urlStr).openStream());
	    transFact = TransformerFactory.newInstance();
	    cachedXSLT = transFact.newTemplates(xsltSource);

	}

	public void transform (File inFile, File outFile) throws TransformerException, MalformedURLException {
		transformer = cachedXSLT.newTransformer();
	    if (getBaseDir()!=null) {
	    	//logger.info(getBaseDir());
	       	transformer.setParameter("baseDir", getBaseDir());
	    }
		Source xmlSource = new StreamSource(inFile);
        transformer.transform(xmlSource, new StreamResult(outFile));
	}
	
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            logger.error("Usage: XSLTransformer xmlfile [xsltfile]");
            System.exit(1);
        }
        
        File inFile = new File(args[0]);
        File outFile = new File(FilenameUtils.removeExtension(args[0]) + tempFileExt);  
        
        logger.info("Transforming " + inFile.getAbsolutePath() + " to " + outFile.getAbsolutePath());
        XSLTransformer xslTransformer = new XSLTransformer("http://nlp.ilsp.gr/xslt/ilsp-fc/cesAlign.xsl");
        xslTransformer.setBaseDir(inFile.getParent());
        xslTransformer.transform(inFile, outFile);
    }

	/**
	 * @return the baseDir
	 */
	public String getBaseDir() {
		return baseDir;
	}

	/**
	 * @param baseDir the baseDir to set
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir+System.getProperty("file.separator");
	}
}