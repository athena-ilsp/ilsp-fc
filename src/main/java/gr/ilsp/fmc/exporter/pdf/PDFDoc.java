package gr.ilsp.fmc.exporter.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;


public class PDFDoc {
	protected static Matcher skipRuleM =  Pattern.compile("^(\\s*)||(#.*)$").matcher("");

	private static final Logger logger = LoggerFactory.getLogger(PDFDoc.class);

	public String topWebDomain;
	
	public String domain;

	public String genre;
	
	public String project;
	
	public String projectWebSite;

	public String language;

	public File textFile;
	
	public File pdfFile;

	public String crawlDate;
	
	public File corpusDir;
	
	public File outputDir;

	/**
	 * @return the topWebDomain
	 */
	public String getTopWebDomain() {
		return topWebDomain;
	}

	/**
	 * @param topWebDomain the topWebDomain to set
	 */
	public void setTopWebDomain(String topWebDomain) {
		this.topWebDomain = topWebDomain;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the genre
	 */
	public String getGenre() {
		return genre;
	}

	/**
	 * @param genre the genre to set
	 */
	public void setGenre(String genre) {
		this.genre = genre;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the crawlDate
	 */
	public String getCrawlDate() {
		return crawlDate;
	}

	/**
	 * @param crawlDate the crawlDate to set
	 */
	public void setCrawlDate(String crawlDate) {
		this.crawlDate = crawlDate;
	}

	/**
	 * @return the corpusDir
	 */
	public File getCorpusDir() {
		return corpusDir;
	}

	/**
	 * @param corpusDir the corpusDir to set
	 */
	public void setCorpusDir(File corpusDir) {
		this.corpusDir = corpusDir;
	}

	/**
	 * @return the outputDir
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * @param outputDir the outputDir to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	

	public void export() throws JDOMException, IOException {
		logger.info(this.toString());
		Document cesDoc = DefaultCesDoc.getDefaultCesDoc();
		
		
		upDateCesHeader(cesDoc, pdfFile);
		upDateCesDocBody(cesDoc, textFile);
		
		XMLOutputter xmlOutputter = new XMLOutputter();
		Format format = Format.getPrettyFormat();
		format.setLineSeparator("\n");
		xmlOutputter.setFormat(format);
		xmlOutputter.output(cesDoc, 
				new FileWriter(FilenameUtils.concat(outputDir.getAbsolutePath(), 
				StringUtils.replace(pdfFile.getName(), ".pdf", ".xml"))));
	}

	private void upDateCesHeader(Document cesDoc, File pdfFile) {
		try {
			Namespace ns = Namespace.getNamespace("", "http://www.xces.org/schema/2003");
			Element header = cesDoc.getRootElement().getChild("cesHeader", ns);	

			// Distributor
			Element distributor = header.
					getChild("fileDesc", ns).getChild("publicationStmt", ns).getChild("distributor", ns);
			distributor.setText(project + "/ILSP");
			Element distributorEAddress = header.
					getChild("fileDesc", ns).getChild("publicationStmt", ns).getChild("eAddress", ns);
			distributorEAddress.setText(projectWebSite);
			
			// Source pdf
			Element eAddress =  header.getChild("fileDesc", ns).getChild("sourceDesc", ns).
					getChild("biblStruct", ns).getChild("monogr", ns).getChild("imprint", ns).getChild("eAddress", ns);
			eAddress.setText(topWebDomain);
			Element format =  header.getChild("fileDesc", ns).getChild("sourceDesc", ns).
					getChild("biblStruct", ns).getChild("monogr", ns).getChild("imprint", ns).getChild("format", ns);
			format.setText("application/pdf");
			Element pubDate = header.getChild("fileDesc", ns).getChild("publicationStmt", ns).getChild("pubDate", ns);
			pubDate.setText(crawlDate);

			
			// ProfileDesc
			Element language = header.getChild("profileDesc", ns).
			getChild("langUsage", ns).getChild("language", ns);
			language.setAttribute("iso639", this.language);
			
			Element domain = header.getChild("profileDesc", ns).
			getChild("textClass", ns).getChild("domain", ns);
			domain.setAttribute("confidence", "1.0");
			domain.setText(this.domain);
			
			Element annotation = header.getChild("profileDesc", ns).
			getChild("annotations", ns).getChild("annotation", ns);
			annotation.setText(pdfFile.getName());
			
			// Tika metadata
			
			ContentHandler handler = new DefaultHandler();
			Metadata metadata = new Metadata();
			InputStream stream = TikaInputStream.get(pdfFile);
			PDFParser parser = new  PDFParser();
			parser.parse(stream, handler, metadata, new ParseContext());	
			stream.close();
			for (String name : metadata.names()) {
				String value = metadata.get(name);
				if (name.equalsIgnoreCase("title") && value != null) {
					Element title =  header.getChild("fileDesc", ns).getChild("titleStmt", ns).getChild("title", ns);
					title.setText(value);
					title =  header.getChild("fileDesc", ns).getChild("sourceDesc", ns).getChild("biblStruct", ns).getChild("monogr", ns).getChild("title", ns);
					title.setText(value);
					
				} else if (name.equalsIgnoreCase("author") && value != null) {
					Element author =  header.getChild("fileDesc", ns).getChild("sourceDesc", ns).getChild("biblStruct", ns).getChild("monogr", ns).getChild("author", ns);
					author.setText(value);
				} else if (name.equalsIgnoreCase("Creation-Date") || (name.equalsIgnoreCase("Last-Modified")) && value != null) {
					Element pdfPubDate =  header.getChild("fileDesc", ns).getChild("sourceDesc", ns).
							getChild("biblStruct", ns).getChild("monogr", ns).getChild("imprint", ns).getChild("pubDate", ns);
					pdfPubDate.setText(value);
				} else {
					logger.debug(name);
				}

			}
		} catch (Exception e) {
			logger.warn("Skipping metadata extraction for " + pdfFile);
			logger.warn(e.getMessage());
		}
	}

	private void upDateCesDocBody(Document cesDoc, File textFile) throws IOException {
		Namespace ns = Namespace.getNamespace("", "http://www.xces.org/schema/2003");
		Element body = cesDoc.getRootElement().getChild("text", ns).getChild("body", ns);	

		BufferedReader br = null;  
		try {  
			br = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
			String line;  
			while ((line = br.readLine()) != null) {  
				if (skipRuleM.reset(line).matches()) {
					continue;
				} else {
					Element p = new Element("p", ns);
					p.setText(line.trim());
					body.addContent(p);
				}
			}  
		}  catch (FileNotFoundException e) {  
			logger.error(e.toString()) ;  
		}  catch (IOException e) {  
			logger.error(e.toString()) ;  
		} finally {  
			if(br != null) br.close();  
		}
		logger.info("Finished exporting to " + outputDir);

		
	}

	/**
	 * @return the pdfFile
	 */
	public File getPdfFile() {
		return pdfFile;
	}

	/**
	 * @param pdfFile the pdfFile to set
	 */
	public void setPdfFile(File pdfFile) {
		this.pdfFile = pdfFile;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PDFDoc [topWebDomain=" + topWebDomain + ", domain=" + domain
				+ ", genre=" + genre + ", project=" + project + ", language="
				+ language + ", textFile=" + textFile + ", pdfFile=" + pdfFile
				+ ", crawlDate=" + crawlDate + ", corpusDir=" + corpusDir
				+ ", outputDir=" + outputDir + "]";
	}

	/**
	 * @return the textFile
	 */
	public File getTextFile() {
		return textFile;
	}

	/**
	 * @param textFile the textFile to set
	 */
	public void setTextFile(File textFile) {
		this.textFile = textFile;
	}

	/**
	 * @return the projectWebSite
	 */
	public String getProjectWebSite() {
		return projectWebSite;
	}

	/**
	 * @param projectWebSite the projectWebSite to set
	 */
	public void setProjectWebSite(String projectWebSite) {
		this.projectWebSite = projectWebSite;
	}

	
	
}
