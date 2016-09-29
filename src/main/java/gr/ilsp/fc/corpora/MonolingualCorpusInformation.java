package gr.ilsp.fc.corpora;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonolingualCorpusInformation extends CorpusInformation {
	private int tokensSize;
	private int sentenceSize;
	private int vocSize;
	private String lang;

	private static final Logger logger = LoggerFactory.getLogger(MonolingualCorpusInformation.class);
	
	public MonolingualCorpusInformation(String name, String lang, 
			int filesSize, int tokensSize, int vocSize, String domain, String domainId, String availability, String creationDescription,
			String projectId, String projectURL, String organization, String organizationURL) {
		
		logger.debug(domain);
		
		this.lang = lang;
		this.domain = domain;
		this.filesSize = filesSize;
		this.tokensSize = tokensSize;
		//this.lenInSentences = sentenceSize;
		this.vocSize = vocSize;
		this.availability = availability;
		this.domainId = domainId;
		this.domain = domain; 
		this.creationDescription = "Acquisition of monolingual data (from websites), normalization, cleaning, and deduplication by ILSP-FC ";
		this.projectId = projectId;
		this.projectURL = 	projectURL;
		this.organization = organization;
		this.organizationURL = organizationURL;
		this.description = "Monolingual (" + lang + ") " + "corpus, containing " + tokensSize  + " tokens and "+ vocSize + " lexical types in the " + domain + " domain.";
		if (StringUtils.isBlank(domain)) {
			this.description = "Monolingual (" + lang + ") " + "corpus, containing " +tokensSize  + " tokens and "+ vocSize +" lexical types in the " + domain + " domain.";
		}
		this.name = name;
	}
	
	
	public MonolingualCorpusInformation() {
	}


	/**
	 * @return the tokensSize
	 */
	public int getTokensSize() {
		return tokensSize;
	}


	/**
	 * @param tokensSize the tokensSize to set
	 */
	public void setTokensSize(int tokensSize) {
		this.tokensSize = tokensSize;
	}


	/**
	 * @return the sentenceSize
	 */
	public int getSentenceSize() {
		return sentenceSize;
	}


	/**
	 * @param sentenceSize the sentenceSize to set
	 */
	public void setSentenceSize(int sentenceSize) {
		this.sentenceSize = sentenceSize;
	}


	/**
	 * @return the vocSize
	 */
	public int getVocSize() {
		return vocSize;
	}


	/**
	 * @param vocSize the vocSize to set
	 */
	public void setVocSize(int vocSize) {
		this.vocSize = vocSize;
	}


	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}


	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}


	/**
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}


	
}
