package gr.ilsp.fc.corpora;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonolingualCorpusInformation extends CorpusInformation {
	private int tokensNum;
	private int sentencesNum;
	private int paragraphsNum;
	private int filesNum;
	private int vocSize;
	private String lang;
	private String level;
	
	private static final Logger logger = LoggerFactory.getLogger(MonolingualCorpusInformation.class);

	public MonolingualCorpusInformation(String name, String lang, String level, 
			int filesNum, int paragraphsNum, int sentencesNum, int tokensNum, int vocSize, 
			String domain, String domainId, String availability, String description, String creationDescription,
			String projectId, String projectURL, String organization, String organizationURL) {

		logger.debug(domain);

		this.lang = lang;
		this.domain = domain;
		this.level = level;
		this.filesNum = filesNum;
		this.tokensNum = tokensNum;
		this.sentencesNum = sentencesNum;
		this.paragraphsNum = paragraphsNum;
		this.vocSize = vocSize;
		this.availability = availability;
		this.domainId = domainId;
		this.domain = domain; 
		this.creationDescription = creationDescription;
		this.projectId = projectId;
		this.projectURL = 	projectURL;
		this.organization = organization;
		this.organizationURL = organizationURL;
		this.name = name;
		this.description = description;
	}


	public MonolingualCorpusInformation() {
	}


	/**
	 * @return the paragraphsNum
	 */
	public int getParagraphsNum() {
		return paragraphsNum;
	}

	/**
	 * @param paragraphsNum the paragraphsNum to set
	 */
	public void setParagraphsNum(int paragraphsNum) {
		this.paragraphsNum = paragraphsNum;
	}
	
	
	/**
	 * @return the filesNum
	 */
	public int getFilesNum() {
		return filesNum;
	}

	/**
	 * @param filesNum the filesNum to set
	 */
	public void setFilesNum(int filesNum) {
		this.filesNum = filesNum;
	}
	
	
	/**
	 * @return the tokensNum
	 */
	public int getTokensNum() {
		return tokensNum;
	}

	/**
	 * @param tokensNum the tokensNum to set
	 */
	public void setTokensNum(int tokensNum) {
		this.tokensNum = tokensNum;
	}

	/**
	 * @return the sentencesNum
	 */
	public int getSentenceNum() {
		return sentencesNum;
	}

	/**
	 * @param sentencesNum the sentencesNum to set
	 */
	public void setSentenceNum(int sentencesNum) {
		this.sentencesNum = sentencesNum;
	}

	/**
	 * @return the vocSize
	 */
	public int getLexTypesNum() {
		return vocSize;
	}

	/**
	 * @param vocSize the vocSize to set
	 */
	public void setLexTypesNum(int vocSize) {
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
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}
	
	
	/**
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}
}
