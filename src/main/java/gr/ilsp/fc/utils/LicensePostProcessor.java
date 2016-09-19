package gr.ilsp.fc.utils;

import gr.ilsp.fc.main.WriteResources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
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
	private static final String type_p ="p";
	private static final String AVAILABILITY = "availability";
	private static final String GENERIC_CC_TEXT = "Distributed under a Creative Commons license (auto detected in document)";
	private static final String GENERIC_Europe_TEXT = "©European Union, 1995-2014. Reuse is authorised, provided the source is acknowledged.";
	private Namespace ns = Namespace.getNamespace("", "http://www.xces.org/schema/2003");
	private static final String UNDERSCORE_STR = "_";
	
	private SuffixFileFilter sff = new SuffixFileFilter("xml");

	private File directory;
	private boolean moveLicenseInfo = true;
	private Matcher CCMatcher = Pattern.compile(".*Creative Commons.*", Pattern.CASE_INSENSITIVE).matcher("");
	//private String genre="Reference";
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
		//File directory = new File(args[0]);
		File directory = new File("C:\\QTLaunchPad\\MEDICAL\\PT-EL\\delivered\\qtlp_20131016_155447\\000d8449-ad3f-4633-8333-d35128a6cabd\\ttt");
		logger.info("Input Directory: " + directory.toString());
		LicensePostProcessor lpp = new LicensePostProcessor();
		lpp.setDirectory(directory);
		lpp.run();
	}
	public void run() throws FileNotFoundException,  IOException, JDOMException {
		List<File> fileList = FcFileUtils.listFiles(this.getDirectory(), this.getSff(), false);
		for (File xmlFile: fileList) {
			logger.info("Parsing " + xmlFile.getAbsolutePath());
			//if (xmlFile.getName().contains(UNDERSCORE_STR))
			//	continue;
			SAXBuilder builder = new SAXBuilder();	
			Document doc = (Document) builder.build(xmlFile);
			Element header = doc.getRootElement().getChild("cesHeader", ns);	
			//boolean found1=false;			boolean found2=false;
			if (!hasLicenseInfoInPublicationStmt(header)) {	
				Element body = doc.getRootElement().getChild("text", ns).getChild("body", ns);	
				Element licenseFromText = getCCLicenseInfoFromText(body);
				if (licenseFromText!=null) {
					Element availability = header.getChild("fileDesc", ns).getChild("publicationStmt", ns).getChild(AVAILABILITY, ns);
					availability.setText("");					availability.addContent(licenseFromText);
					logger.info("LINECENSE in TEXT: "+ xmlFile.getName());
					//found1=true;
				}
			}else
				logger.info(xmlFile.getName());
			if (moveLicenseInfo && hasLicenseInfoInPublicationStmt(header)) {
				moveLicenseInfo(header);
				//found2=true;
			}
			//if (found1 | found2){
				File outFile = new File(xmlFile.getAbsoluteFile() + ".out");
				XMLOutputter xmlOutputer = new XMLOutputter();
				xmlOutputer.setFormat(Format.getPrettyFormat());
				xmlOutputer.output(doc, new FileWriter(outFile));
			//}
		}
		//movefiles(this.getDirectory());
		copyfiles(this.getDirectory());
		//copyFile( srcFile, destFile );
		replaceinXMLfiles(this.getDirectory());
		
	}
	
	private void copyfiles(File directory1) {
		File newDir = new File(directory1.getParent()+"\\xml_out");
		newDir.mkdir();
		
//		String p = directory1.getAbsolutePath();
	
		File[] dirfiles = directory1.listFiles();
		for (File curFile: dirfiles) {
			logger.info(curFile.getAbsolutePath());
			//if (curFile.getName().endsWith("xml") & !curFile.getName().contains(UNDERSCORE_STR))
			if (curFile.getName().endsWith("xml"))
				continue;
			File dest; 
			if (curFile.getName().endsWith("out"))
				dest = new File(newDir.getAbsolutePath()+"\\"+curFile.getName().replace("xml.out", "xml"));	
			else
				dest = new File(newDir.getAbsolutePath()+"\\"+curFile.getName());
			
			logger.info(dest.getAbsolutePath());
			//curFile.renameTo(dest);
			try {
				FileUtils.copyFile(curFile, dest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
	}
	private void replaceinXMLfiles(File directory1) throws IOException {
		File newDir = new File(directory1.getParent()+"\\xml_out");
		File[] dirfiles = newDir.listFiles();
		for (File curFile: dirfiles) {
			//if (curFile.getName().endsWith("xml") & !curFile.getName().contains(UNDERSCORE_STR)){
			if (curFile.getName().endsWith("xml") )	{
				String text = FileUtils.readFileToString(curFile);
				text = text.replace("<distributor>ILSP project</distributor>",
						"<distributor>QTLP</distributor>");
				text = text.replace("<distributor>QTLP/ILSP</distributor>",
						"<distributor>QTLP</distributor>");
				
				text=text.replace(">project_website</eAddress>",
						">http://www.qt21.eu/launchpad/</eAddress>");
				text=text.replace("<domain></domain>",
						"<domain confidence=\"1.0\">MEDICAL</domain>");
				text=text.replace("<domain />",
						"<domain confidence=\"1.0\">MEDICAL</domain>");
				text=text.replace("<genre></genre>",
						"<genre>"+"Reference"+"</genre>");
				text=text.replace("<genre />",
						"<genre>"+"Reference"+"</genre>");
				if (text.contains("              <license target=")){ 
					text=text.replace("        <availability>Under review</availability>\r\n", "");
				}
				//WriteResources.writetextfile(curFile.getAbsolutePath(),text);
				FileUtils.writeStringToFile(curFile, text);
//	text.replace("<availability>Under review</availability>",);
			}
		}
	}

	/*private void movefiles(File directory1) {
		
		File newDir = new File(directory1.getParent()+"\\xml_out");
		newDir.mkdir();
		
//		String p = directory1.getAbsolutePath();
	
		File[] dirfiles = directory1.listFiles();
		for (File curFile: dirfiles) {
			logger.info(curFile.getAbsolutePath());
			//if (curFile.getName().endsWith("xml") & !curFile.getName().contains(UNDERSCORE_STR))
			if (curFile.getName().endsWith("xml"))
				continue;
			File dest; 
			if (curFile.getName().endsWith("out"))
				dest = new File(newDir.getAbsolutePath()+"\\"+curFile.getName().replace("xml.out", "xml"));	
			else
				dest = new File(newDir.getAbsolutePath()+"\\"+curFile.getName());
			
			logger.info(dest.getAbsolutePath());
			//curFile.renameTo(dest);
			try {
				FileUtils.moveFile(curFile, dest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
*/
	private Element getCCLicenseInfoFromText(Element body) {
		@SuppressWarnings("unchecked")
		List<Element> paragraphs = body.getChildren(type_p, ns);
		for (Element paragraph:paragraphs) {
			String text = paragraph.getTextNormalize();
			//if (CCMatcher.reset(text).matches()) {
			if (true) {	
				logger.info("CC FOUND " +text);
				Element license = new Element("license", ns);
				license.setText(GENERIC_CC_TEXT);
				//license.setAttribute("target", "http://creativecommons.org/licenses/by/3.0/");
				license.setAttribute("target", "http://ec.europa.eu/geninfo/legal_notices_en.htm#copyright");
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