package gr.ilsp.fc.exporter.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prokopis
 *
 */
public class PDFCorpus {
	/**
	 * 
	 */
	protected static Matcher skipRuleM =  Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final Logger logger = LoggerFactory.getLogger(PDFCorpus.class);
	String inCharsetName = "UTF-8";
	/**
	 * @throws IOException 
	 * @throws JDOMException 
	 *  
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		PDFCorpus pdfCorpus = new PDFCorpus();

		pdfCorpus.setCorpusDir(new File(args[0]));
		pdfCorpus.setCorpusDescriptionFile(new File(args[1]));
		pdfCorpus.setOutputDir(new File(args[2]));

		List<String> languages = new ArrayList<String>();
		languages.add("pt"); // FIXME: Add options for this. FIXME Assumes order is same with the corpusDescriptionFile
		languages.add("el"); // FIXME: Add options for this
		pdfCorpus.setLanguages(languages);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		pdfCorpus.setCrawlDate(sdf.format(pdfCorpus.getCorpusDir().lastModified()));

		pdfCorpus.setGenre("Information"); // FIXME: Add options for this
		pdfCorpus.setTopWebDomain("http://www.mhcs.health.nsw.gov.au"); // FIXME: Add options for this		
		pdfCorpus.setDomain("Medical"); // FIXME: Add options for this
		pdfCorpus.setProject("QTLP"); // FIXME: Add options for this
		pdfCorpus.setProjectWebSite("http://www.qt21.eu"); // FIXME: Add options for this
		
		if (languages.size()>1) {
			pdfCorpus.setLinguality("multilingual");
		} else {
			pdfCorpus.setLinguality("monolingual");
		}
		pdfCorpus.processPdfFiles(pdfCorpus.getCorpusDir(), pdfCorpus.getCorpusDescriptionFile());
	}
	
	private void  processPdfFiles(File corpusDir,	File corpusDescriptionFile) throws IOException, JDOMException {
		logger.info("Reading: " + corpusDescriptionFile);
		BufferedReader br = null;  
		try {  
			br = new BufferedReader(new InputStreamReader(new FileInputStream(corpusDescriptionFile), inCharsetName));
			String line;  
			if (languages.size()>1) {
				while ((line = br.readLine()) != null) {  
					if (skipRuleM.reset(line).matches()) {
						continue;
					} else {
						String[] files = line.split("\\s+");
						File file1 = new File(FilenameUtils.concat(corpusDir.getAbsolutePath(), new File(files[0]).getName()));
						File file2 = new File(FilenameUtils.concat(corpusDir.getAbsolutePath(), new File(files[1]).getName()));
						PDFPair pdfPair = new PDFPair();

						pdfPair.setTextFile1(file1);
						pdfPair.setTextFile2(file2);
						pdfPair.setPdfFile1(new File(StringUtils.replace(file1.getAbsolutePath(), ".pdf.txt", ".pdf")));
						pdfPair.setPdfFile2(new File(StringUtils.replace(file2.getAbsolutePath(), ".pdf.txt", ".pdf")));
						pdfPair.setLanguage1(languages.get(0));
						pdfPair.setLanguage2(languages.get(1));						

						pdfPair.setCrawlDate(crawlDate);
						pdfPair.setGenre(genre); 
						pdfPair.setTopWebDomain(topWebDomain); 		
						pdfPair.setDomain(domain); 
						pdfPair.setProject(project); 
						pdfPair.setProjectWebSite(projectWebSite); 	
	
						pdfPair.setCorpusDir(corpusDir);
						pdfPair.setOutputDir(outputDir);
						pdfPair.export();
					}
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
	 * 
	 */
	public PDFCorpus() {
		super();
	}


	public String topWebDomain;
	
	public String domain;

	public String genre;
	
	public String project;
	
	public String projectWebSite;

	public String linguality;

	public List<String> languages;
	
	public String crawlDate;
	
	public File corpusDir;
	
	public File corpusDescriptionFile;	
	
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
	 * @return the linguality
	 */
	public String getLinguality() {
		return linguality;
	}

	/**
	 * @param linguality the linguality to set
	 */
	public void setLinguality(String linguality) {
		this.linguality = linguality;
	}

	/**
	 * @return the languages
	 */
	public List<String> getLanguages() {
		return languages;
	}

	/**
	 * @param languages the languages to set
	 */
	public void setLanguages(List<String> languages) {
		this.languages = languages;
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
	 * @return the corpusDescriptionFile
	 */
	public File getCorpusDescriptionFile() {
		return corpusDescriptionFile;
	}

	/**
	 * @param corpusDescriptionFile the corpusDescriptionFile to set
	 */
	public void setCorpusDescriptionFile(File corpusDescriptionFile) {
		this.corpusDescriptionFile = corpusDescriptionFile;
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
