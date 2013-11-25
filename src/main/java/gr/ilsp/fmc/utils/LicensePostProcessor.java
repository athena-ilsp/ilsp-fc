package gr.ilsp.fmc.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A script used for postprocessing of the XML files of a corpus for license extraction and manipulation.  
 * 
 * 
 * @author prokopis
 * 
 */
public class LicensePostProcessor {
	private static final String LICENSE = "license";
	private static final String AVAILABILITY = "availability";
	private static final String GENERIC_CC_TEXT = "Distributed under a Creative Commons license (auto detected in document)";
	private Namespace ns = Namespace.getNamespace("", "http://www.xces.org/schema/2003");

	private SuffixFileFilter sff = new SuffixFileFilter("xml");

	private File directory;
	private boolean moveLicenseInfo = true;
	private Matcher CCMatcher = Pattern.compile(".*Creative Commons.*", Pattern.CASE_INSENSITIVE).matcher("");

//	private Matcher BY = Pattern.compile(".*/by/.*").matcher("");
//	private Matcher BYND = Pattern.compile(".*/by-nd/.*").matcher("");
//	private Matcher BYSA = Pattern.compile(".*/by-sa/.*").matcher("");
//	private Matcher BYNC = Pattern.compile(".*/by-nc/.*").matcher("");
//	private Matcher BYNCND = Pattern.compile(".*/by-nc-nd/.*").matcher("");
//	private Matcher BYNCSA = Pattern.compile(".*/by-nc-sa/.*").matcher("");
	
	/**
	 * Logger for this class
	 */
	static Logger logger = LoggerFactory.getLogger(LicensePostProcessor.class.getCanonicalName());
	
	public static void main(String... args) throws Exception {
		File directory = new File(args[0]);

		logger.info("Input Directory: " + directory.toString());
		LicensePostProcessor lpp = new LicensePostProcessor();
		lpp.setDirectory(directory);
		lpp.run();
	}

	private void run() throws FileNotFoundException,  IOException, JDOMException {
		
		Vector<File> fileList = FileUtils.listFiles(this.getDirectory(), this.getSff(), false);
		for (File xmlFile: fileList) {
			logger.info("Parsing " + xmlFile.getAbsolutePath());
					
			SAXBuilder builder = new SAXBuilder();	
			Document doc = (Document) builder.build(xmlFile);

			Element header = doc.getRootElement().getChild("cesHeader", ns);	

			if (!hasLicenseInfoInPublicationStmt(header)) {	
				Element body = doc.getRootElement().getChild("text", ns).getChild("body", ns);	
				Element licenseFromText = getCCLicenseInfoFromText(body);
				if (licenseFromText!=null) {
					Element availability = header.getChild("fileDesc", ns).getChild("publicationStmt", ns).getChild(AVAILABILITY, ns);
					availability.setText("");
					availability.addContent(licenseFromText);

				}
			}
			
			if (moveLicenseInfo && hasLicenseInfoInPublicationStmt(header)) {
				moveLicenseInfo(header);
			}
			File outFile = new File(xmlFile.getAbsoluteFile() + ".out");
			XMLOutputter xmlOutputer = new XMLOutputter();
			xmlOutputer.setFormat(Format.getPrettyFormat());
			xmlOutputer.output(doc, new FileWriter(outFile));
		}
	}
	
	private Element getCCLicenseInfoFromText(Element body) {
		@SuppressWarnings("unchecked")
		List<Element> paragraphs = body.getChildren("p", ns);
		for (Element paragraph:paragraphs) {
			String text = paragraph.getTextNormalize();
			if (CCMatcher.reset(text).matches()) {
				logger.info("CC FOUND " +text);
				Element license = new Element("license", ns);
				license.setText(GENERIC_CC_TEXT);
				license.setAttribute("target", "http://creativecommons.org/licenses/by/3.0/");
				return license;
			}
		}
		
		return null;
	}

	private void moveLicenseInfo(Element header) {	
		logger.info("Moving license");
		Element availability = header.getChild("fileDesc", ns).getChild("publicationStmt", ns).getChild(AVAILABILITY, ns);
		Element license = availability.getChild(LICENSE, ns);
		license =  (Element) license.detach();
		Attribute target = license.getAttribute("target");
//		String licenseUrl = target.getValue();
//		String licenseText = "";
//		if (BYNC.reset(licenseUrl).matches() ) {
//			licenseText = ""
//		}
		logger.debug(target.getValue());
		Element imprint= header.getChild("fileDesc", ns).getChild("sourceDesc", ns).getChild("biblStruct", ns).getChild("monogr", ns).getChild("imprint", ns);
		imprint.addContent(license);
		availability.setText("Under review");
	}


	private boolean hasLicenseInfoInPublicationStmt(Element header) {
		Element publicationStmt = header.getChild("fileDesc", ns).getChild("publicationStmt", ns);
		if (publicationStmt.getChild(AVAILABILITY, ns) != null 
				&& publicationStmt.getChild(AVAILABILITY, ns).getChild(LICENSE, ns)!=null) {
			logger.debug("Found license");
			return true;
		}
		return false;
	}


	public void processComment(Comment comment) {
	    // Do something with comments
	}

	public void processProcessingInstruction(ProcessingInstruction pi) {
	    // Do something with PIs
	}

	public void processEntityRef(EntityRef entityRef) {
	    // Do something with entity references
	}

	public void processText(Text text) {
	    // Do something with text
	}

	public void processCDATA(CDATA cdata) {
	    // Do something with CDATA
	}


	/**
	 * @return the directory
	 */
	public File getDirectory() {
		return directory;
	}


	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
	}


	/**
	 * @return the sff
	 */
	public SuffixFileFilter getSff() {
		return sff;
	}
	
}