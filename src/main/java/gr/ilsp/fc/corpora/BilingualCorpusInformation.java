package gr.ilsp.fc.corpora;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;

public class BilingualCorpusInformation {

	private static final Logger logger = LoggerFactory.getLogger(BilingualCorpusInformation.class);
	
	private int zeroToOneAlignmentsSize;
	private int l1SegmentsSize;
	private int l2SegmentsSize;
	private int l1TokensSize, cleanl1TokensSize;
	private int l2TokensSize, cleanl2TokensSize;
	private List<File> tmxFiles;
	
	private List<ILSPAlignment> alignmentList;
	private int alignmentsSize;

	private String l1;
	private String l2;

	private int lenInTUs, cleanlenInTUs;
	private int lenInWordsL1, lenInWordsL2;
	private int vocSizeInL1, vocSizeInL2;
	private int cleanlenInWordsL1, cleanlenInWordsL2;
	private int cleanvocSizeInL1, cleanvocSizeInL2;
	private String availability;
	private List<String> crawledSites;

	private String domain;
	private String domainId;

	private boolean mergedAndFilteredTMX = false;
	private String creationDescription = "Acquisition of bilingual data (from multilingual websites), normalization, cleaning, deduplication and identification of parallel documents: ILSP-FC. "
			+ "Maligna aligner was used for alignment of segments.";
			//#As a post-processing serging/filtering of segment pairs has also been applied.";
	
	private String name;
	private String description;
	private String projectId = "ELRC"; 
	private String projectURL = "http://lr-coordination.eu/"; 
	private String organization = "ILSP"; 
	private String organizationURL = "http://www.ilsp.gr/"; 
	
	public BilingualCorpusInformation(String name, String l1, String l2, 
			List<ILSPAlignment> alignmentList, int lenInTUs, int cleanlenInTUs,
			int lenInWordsL1, int lenInWordsL2, int vocSizeInL1, int vocSizeInL2,
			int cleanlenInWordsL1, int cleanlenInWordsL2, int cleanvocSizeInL1, int cleanvocSizeInL2,
			String domain, String domainId,
			String availability, String creationDescription,
			String projectId, String projectURL,
			String organization, String organizationURL) {
		
		logger.debug(domain);
		
		this.l1 = l1;
		this.l2 = l2;
		this.alignmentList = alignmentList;
		this.domain = domain;
		this.lenInTUs = lenInTUs;
		this.cleanlenInTUs = cleanlenInTUs;
		this.lenInWordsL1 = lenInWordsL1;
		this.lenInWordsL2 = lenInWordsL2;
		this.cleanlenInWordsL1 = cleanlenInWordsL1;
		this.cleanlenInWordsL2 = cleanlenInWordsL2;
		this.vocSizeInL1 = vocSizeInL1;
		this.vocSizeInL2 = vocSizeInL2;
		this.cleanvocSizeInL1 = cleanvocSizeInL1;
		this.cleanvocSizeInL2 = cleanvocSizeInL2;
		this.availability = availability;
		this.domainId = domainId;
		this.domain = domain;
		this.creationDescription = creationDescription;
		this.projectId = projectId;
		this.projectURL = 	projectURL;
		this.organization = organization;
		this.organizationURL = organizationURL;
		this.description = "Parallel (" + l1 + "-" + l2 + ") " + "corpus of " +lenInTUs  + " translation units in the " + domain + " domain. ";
		if (StringUtils.isBlank(domain)) 
			this.description = "Parallel (" + l1 + "-" + l2 + ") " + "corpus of " + lenInTUs  + " translation units.";
		this.name = name;
	}
	
	
	public BilingualCorpusInformation() {
	}

	/**
	 * @return the alignmentsSize
	 */
	public int getAlignmentsSize() {
		return alignmentsSize;
	}
	/**
	 * @param alignmentsSize the alignmentsSize to set
	 */
	public void setAlignmentsSize(int alignmentsSize) {
		this.alignmentsSize = alignmentsSize;
	}
	/**
	 * @return the l1SegmentsSize
	 */
	public int getL1SegmentsSize() {
		return l1SegmentsSize;
	}
	/**
	 * @param l1SegmentsSize the l1SegmentsSize to set
	 */
	public void setL1SegmentsSize(int l1SegmentsSize) {
		this.l1SegmentsSize = l1SegmentsSize;
	}
	/**
	 * @return the l2SegmentsSize
	 */
	public int getL2SegmentsSize() {
		return l2SegmentsSize;
	}
	/**
	 * @param l2SegmentsSize the l2SegmentsSize to set
	 */
	public void setL2SegmentsSize(int l2SegmentsSize) {
		this.l2SegmentsSize = l2SegmentsSize;
	}
	/**
	 * @return the l1TokensSize
	 */
	public int getL1TokensSize() {
		return l1TokensSize;
	}
	/**
	 * @param l1TokensSize the l1TokensSize to set
	 */
	public void setL1TokensSize(int l1TokensSize) {
		this.l1TokensSize = l1TokensSize;
	}
	/**
	 * @return the cleanl1TokensSize
	 */
	public int getCleanL1TokensSize() {
		return cleanl1TokensSize;
	}
	/**
	 * @param cleanl1TokensSize the cleanl1TokensSize to set
	 */
	public void setCleanL1TokensSize(int cleanl1TokensSize) {
		this.cleanl1TokensSize = cleanl1TokensSize;
	}
	/**
	 * @return the l2TokensSize
	 */
	public int getL2TokensSize() {
		return l2TokensSize;
	}
	/**
	 * @param l2TokensSize the l2TokensSize to set
	 */
	public void setL2TokensSize(int l2TokensSize) {
		this.l2TokensSize = l2TokensSize;
	}
	/**
	 * @return the cleanl2TokensSize
	 */
	public int getCleanL2TokensSize() {
		return cleanl2TokensSize;
	}
	/**
	 * @param cleanl2TokensSize the cleanl2TokensSize to set
	 */
	public void setCleanL2TokensSize(int cleanl2TokensSize) {
		this.cleanl2TokensSize = cleanl2TokensSize;
	}
	/**
	 * @return the tmxFiles
	 */
	public List<File> getTmxFiles() {
		return tmxFiles;
	}
	/**
	 * @param tmxFiles the tmxFiles to set
	 */
	public void setTmxFiles(List<File> tmxFiles) {
		this.tmxFiles = tmxFiles;
	}
	/**
	 * @return the l1
	 */
	public String getL1() {
		return l1;
	}
	/**
	 * @param l1 the l1 to set
	 */
	public void setL1(String l1) {
		this.l1 = l1;
	}
	/**
	 * @return the l2
	 */
	public String getL2() {
		return l2;
	}
	/**
	 * @param l2 the l2 to set
	 */
	public void setL2(String l2) {
		this.l2 = l2;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BilingualCorpusInformation [alignmentsSize=" + alignmentsSize
				+ ", l1SegmentsSize=" + l1SegmentsSize + ", l2SegmentsSize="
				+ l2SegmentsSize + ", l1TokensSize=" + l1TokensSize
				+ ", l2TokensSize=" + l2TokensSize + ", "
				//+ (l1Files != null ? "l1Files=" + l1Files.size() + ", " : "")
				//+ (l2Files != null ? "l2Files=" + l2Files.size() + ", " : "")
				+ (tmxFiles != null ? "tmxFiles=" + tmxFiles.size() + ", " : "")
				+ (l1 != null ? "l1=" + l1 + ", " : "")
				+ (l2 != null ? "l2=" + l2 : "") + "]";
	}
	/**
	 * @return the zeroToOneAlignmentsSize
	 */
	public int getZeroToOneAlignmentsSize() {
		return zeroToOneAlignmentsSize;
	}
	/**
	 * @param zeroToOneAlignmentsSize the zeroToOneAlignmentsSize to set
	 */
	public void setZeroToOneAlignmentsSize(int zeroToOneAlignmentsSize) {
		this.zeroToOneAlignmentsSize = zeroToOneAlignmentsSize;
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
	 * @return the lenInTUs
	 */
	public int getLenInTUs() {
		return lenInTUs;
	}
	/**
	 * @param lenInTUs the lenInTUs to set
	 */
	public void setLenInTUs(int lenInTUs) {
		this.lenInTUs = lenInTUs;
	}
	
	/**
	 * @return the cleanlenInTUs
	 */
	public int getCleanLenInTUs() {
		return cleanlenInTUs;
	}
	/**
	 * @param cleanlenInTUs the cleanlenInTUs to set
	 */
	public void setCleanLenInTUs(int cleanlenInTUs) {
		this.cleanlenInTUs = cleanlenInTUs;
	}
	/**
	 * @return the lenInWordsL1
	 */
	public int getLenInWordsL1() {
		return lenInWordsL1;
	}
	/**
	 * @param lenInWordsL1 the lenInWordsL1 to set
	 */
	public void setLenInWordsL1(int lenInWordsL1) {
		this.lenInWordsL1 = lenInWordsL1;
	}
	/**
	 * @return the cleanlenInWordsL1
	 */
	public int getCleanLenInWordsL1() {
		return cleanlenInWordsL1;
	}
	/**
	 * @param cleanlenInWordsL1 the cleanlenInWordsL1 to set
	 */
	public void setCleanLenInWordsL1(int cleanlenInWordsL1) {
		this.cleanlenInWordsL1 = cleanlenInWordsL1;
	}
	/**
	 * @return the lenInWordsL2
	 */
	public int getLenInWordsL2() {
		return lenInWordsL2;
	}
	/**
	 * @param lenInWordsL2 the lenInWordsL2 to set
	 */
	public void setLenInWordsL2(int lenInWordsL2) {
		this.lenInWordsL2 = lenInWordsL2;
	}
	
	/**
	 * @return the cleanlenInWordsL2
	 */
	public int getCleanLenInWordsL2() {
		return cleanlenInWordsL2;
	}
	/**
	 * @param cleanlenInWordsL2 the cleanlenInWordsL2 to set
	 */
	public void setCleanLenInWordsL2(int cleanlenInWordsL2) {
		this.cleanlenInWordsL2 = cleanlenInWordsL2;
	}
	/**
	 * @return the cleanvocSizeInL1
	 */
	public int getCleanVocSizeInL1() {
		return cleanvocSizeInL1;
	}
	/**
	 * @param cleanvocSizeInL1 the cleanvocSizeInL1 to set
	 */
	public void setCleanVocSizeInL1(int cleanvocSizeInL1) {
		this.cleanvocSizeInL1 = cleanvocSizeInL1;
	}
	/**
	 * @return the vocSizeInL1
	 */
	public int getVocSizeInL1() {
		return vocSizeInL1;
	}
	/**
	 * @param vocSizeInL1 the vocSizeInL1 to set
	 */
	public void setVocSizeInL1(int vocSizeInL1) {
		this.vocSizeInL1 = vocSizeInL1;
	}
	/**
	 * @return the vocSizeInL2
	 */
	public int getVocSizeInL2() {
		return vocSizeInL2;
	}
	/**
	 * @param vocSizeInL2 the vocSizeInL2 to set
	 */
	public void setVocSizeInL2(int vocSizeInL2) {
		this.vocSizeInL2 = vocSizeInL2;
	}
	/**
	 * @return the cleanvocSizeInL2
	 */
	public int getCleanVocSizeInL2() {
		return cleanvocSizeInL2;
	}
	/**
	 * @param cleanvocSizeInL2 the cleanvocSizeInL2 to set
	 */
	public void setCleanVocSizeInL2(int cleanvocSizeInL2) {
		this.cleanvocSizeInL2 = cleanvocSizeInL2;
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
	/**
	 * @return the crawledSites
	 */
	public List<String> getCrawledSites() {
		return crawledSites;
	}
	/**
	 * @param crawledSites the crawledSites to set
	 */
	public void setCrawledSites(List<String> crawledSites) {
		this.crawledSites = crawledSites;
	}
	/**
	 * @return the alignmentList
	 */
	public List<ILSPAlignment> getAlignmentList() {
		return alignmentList;
	}
	/**
	 * @param alignmentList the alignmentList to set
	 */
	public void setAlignmentList(List<ILSPAlignment> alignmentList) {
		this.alignmentList = alignmentList;
	}
	/**
	 * @return the mergedAndFilteredTMX
	 */
	public boolean isMergedAndFilteredTMX() {
		return mergedAndFilteredTMX;
	}
	/**
	 * @param mergedAndFilteredTMX the mergedAndFilteredTMX to set
	 */
	public void setMergedAndFilteredTMX(boolean mergedAndFilteredTMX) {
		this.mergedAndFilteredTMX = mergedAndFilteredTMX;
	}
	/**
	 * @return the creationDescription
	 */
	public String getCreationDescription() {
		return creationDescription;
	}
	/**
	 * @param creationDescription the creationDescription to set
	 */
	public void setCreationDescription(String creationDescription) {
		this.creationDescription = creationDescription;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the projectURL
	 */
	public String getProjectURL() {
		return projectURL;
	}

	/**
	 * @param projectURL the projectURL to set
	 */
	public void setProjectURL(String projectURL) {
		this.projectURL = projectURL;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the organizationURL
	 */
	public String getOrganizationURL() {
		return organizationURL;
	}

	/**
	 * @param organizationURL the organizationURL to set
	 */
	public void setOrganizationURL(String organizationURL) {
		this.organizationURL = organizationURL;
	}

}
