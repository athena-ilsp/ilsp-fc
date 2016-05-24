package gr.ilsp.fc.corpora;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonolingualCorpusInformation {
	private int tokensSize;
	private int documentsSize;
	private int paragraphsSize;
	private String lang;
	private String name;
	private String description;
	private String projectId = "ELRC"; 
	private String projectURL = "http://lr-coordination.eu/"; 
	private String organization = "ILSP"; 
	private String organizationURL = "http://www.ilsp.gr/"; 
	private String availability;
	private String domain;
	private String domainId;
	private String creationDescription = "Acquisition of monolingual data (from websites), normalization, cleaning, and deduplication by ILSP-FC ";
	
	
	private static final Logger logger = LoggerFactory.getLogger(MonolingualCorpusInformation.class);
	
	public MonolingualCorpusInformation(String name, String l1, 
			int numToks, String domain, String domainId, String availability, String creationDescription,
			String projectId, String projectURL,String organization, String organizationURL) {
		
		logger.debug(domain);
		
		this.lang = l1;
		this.domain = domain;
		this.tokensSize = numToks;
		this.availability = availability;
		this.domainId = domainId;
		this.domain = domain;
		this.creationDescription = creationDescription;
		this.projectId = projectId;
		this.projectURL = 	projectURL;
		this.organization = organization;
		this.organizationURL = organizationURL;
		this.description = "Monolingual (" + l1 + ") " + "corpus of " +numToks  + " tokenss in the " + domain + " domain.";
		if (StringUtils.isBlank(domain)) {
			this.description = "Monolingual (" + l1 + ") " + "corpus of " +numToks  + " tokenss in the " + domain + " domain.";
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
	 * @return the documentsSize
	 */
	public int getDocumentsSize() {
		return documentsSize;
	}
	/**
	 * @param documentsSize the documentsSize to set
	 */
	public void setDocumentsSize(int documentsSize) {
		this.documentsSize = documentsSize;
	}
	/**
	 * @return the paragraphsSize
	 */
	public int getParagraphsSize() {
		return paragraphsSize;
	}
	/**
	 * @param paragraphsSize the paragraphsSize to set
	 */
	public void setParagraphsSize(int paragraphsSize) {
		this.paragraphsSize = paragraphsSize;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MonolingualCorpusInformation [tokensSize=" + tokensSize
				+ ", documentsSize=" + documentsSize + ", paragraphsSize="
				+ paragraphsSize + ", " + (lang != null ? "lang=" + lang : "")
				+ "]";
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * @return the domainId
	 */
	public String getDomainId() {
		return domainId;
	}
	/**
	 * @param domainId the domainId to set
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	
	/**
	 * @return the availability
	 */
	public String getAvailability() {
		return availability;
	}
	/**
	 * @param availability the availability to set
	 */
	public void setAvailability(String availability) {
		this.availability = availability;
	}
}
