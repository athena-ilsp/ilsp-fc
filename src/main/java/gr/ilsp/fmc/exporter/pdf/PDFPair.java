package gr.ilsp.fmc.exporter.pdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFPair {
	
	private static final Logger logger = LoggerFactory.getLogger(PDFPair.class);
	
	public String topWebDomain;
	
	public String domain;

	public String genre;
	
	public String project;
	
	public String projectWebSite;

	public String language1;

	public String language2;

	public File textFile1;

	public File textFile2;
	
	public File pdfFile1;

	public File pdfFile2;
	
	public String crawlDate;
	
	public File corpusDir;
	
	public File outputDir;

	public void export() throws JDOMException, IOException {
		logger.info(this.toString());
		PDFDoc pdfDoc1 = new PDFDoc();
		pdfDoc1.setLanguage(language1);
		pdfDoc1.setTextFile(textFile1);	
		pdfDoc1.setPdfFile(pdfFile1);

		pdfDoc1.setCorpusDir(corpusDir);
		pdfDoc1.setOutputDir(outputDir);
		pdfDoc1.setCrawlDate(crawlDate);
		pdfDoc1.setGenre(genre); 
		pdfDoc1.setTopWebDomain(topWebDomain); 		
		pdfDoc1.setDomain(domain); 
		pdfDoc1.setProject(project); 		
		pdfDoc1.setProjectWebSite(projectWebSite); 	

		pdfDoc1.export(); 	

		PDFDoc pdfDoc2 = new PDFDoc();
		pdfDoc2.setLanguage(language2);
		pdfDoc2.setTextFile(textFile2);
		pdfDoc2.setPdfFile(pdfFile2);		
		pdfDoc2.setCorpusDir(corpusDir);
		pdfDoc2.setOutputDir(outputDir);
		pdfDoc2.setCrawlDate(crawlDate);
		pdfDoc2.setGenre(genre); 
		pdfDoc2.setTopWebDomain(topWebDomain); 		
		pdfDoc2.setDomain(domain); 
		pdfDoc2.setProject(project); 	
		pdfDoc2.setProjectWebSite(projectWebSite); 	
		pdfDoc2.export();
		
		this.exportCesAlign();
	}
	
	private void exportCesAlign() throws JDOMException, IOException {
		Document cesAlign = DefaultCesAlign.getDefaultCesAlign();
		try {
			Namespace ns = Namespace.getNamespace("", "http://www.xces.org/schema/2003");	
			Namespace nsXml = Namespace.getNamespace("xml","http://www.w3.org/XML/1998/namespace"); 
			
			Element translations = cesAlign.getRootElement().getChild("cesHeader", ns).getChild("profileDesc", ns).
					getChild("translations", ns);
			
			Element translation1 = (Element) translations.getChildren("translation", ns).get(0);
			translation1.setAttribute("n", "1");	
			translation1.setAttribute("lang", language1, nsXml);
			translation1.setAttribute("trans.loc", StringUtils.replace(pdfFile1.getName(), ".pdf", ".xml"));

			Element translation2 = (Element) translations.getChildren("translation", ns).get(1);
			translation2.setAttribute("n", "2");
			translation2.setAttribute("lang", language2, nsXml);
			translation2.setAttribute("trans.loc", StringUtils.replace(pdfFile2.getName(), ".pdf", ".xml"));
			
			XMLOutputter xmlOutputer = new XMLOutputter();
			xmlOutputer.setFormat(Format.getPrettyFormat());
			String cesAlignF =  FilenameUtils.concat(outputDir.getAbsolutePath(), 
					(
							StringUtils.replace(pdfFile1.getName(), ".pdf", "") 
							+ "_" 
							+ StringUtils.replace(pdfFile2.getName(), ".pdf", "") 
							+ ".xml"
					));
			logger.debug(cesAlignF);
			xmlOutputer.output(cesAlign,new FileWriter(cesAlignF));

			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		

	}

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

	/**
	 * @return the language1
	 */
	public String getLanguage1() {
		return language1;
	}

	/**
	 * @param language1 the language1 to set
	 */
	public void setLanguage1(String language1) {
		this.language1 = language1;
	}

	/**
	 * @return the language2
	 */
	public String getLanguage2() {
		return language2;
	}

	/**
	 * @param language2 the language2 to set
	 */
	public void setLanguage2(String language2) {
		this.language2 = language2;
	}

	/**
	 * @return the textFile1
	 */
	public File getTextFile1() {
		return textFile1;
	}

	/**
	 * @param textFile1 the textFile1 to set
	 */
	public void setTextFile1(File textFile1) {
		this.textFile1 = textFile1;
	}

	/**
	 * @return the textFile2
	 */
	public File getTextFile2() {
		return textFile2;
	}

	/**
	 * @param textFile2 the textFile2 to set
	 */
	public void setTextFile2(File textFile2) {
		this.textFile2 = textFile2;
	}

	/**
	 * @return the pdfFile1
	 */
	public File getPdfFile1() {
		return pdfFile1;
	}

	/**
	 * @param pdfFile1 the pdfFile1 to set
	 */
	public void setPdfFile1(File pdfFile1) {
		this.pdfFile1 = pdfFile1;
	}

	/**
	 * @return the pdfFile2
	 */
	public File getPdfFile2() {
		return pdfFile2;
	}

	/**
	 * @param pdfFile2 the pdfFile2 to set
	 */
	public void setPdfFile2(File pdfFile2) {
		this.pdfFile2 = pdfFile2;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PDFPair [topWebDomain=" + topWebDomain + ", domain=" + domain
				+ ", genre=" + genre + ", project=" + project + ", language1="
				+ language1 + ", language2=" + language2 + ", textFile1="
				+ textFile1 + ", textFile2=" + textFile2 + ", pdfFile1="
				+ pdfFile1 + ", pdfFile2=" + pdfFile2 + ", crawlDate="
				+ crawlDate + ", corpusDir=" + corpusDir
				+ ", outputDir=" + outputDir + "]";
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
