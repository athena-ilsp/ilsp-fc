package gr.ilsp.fc.corpora;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.utils.CalendarUtils;
import gr.ilsp.metashare.ActorInfoType;
import gr.ilsp.metashare.CharacterEncodingInfoType;
import gr.ilsp.metashare.CommunicationInfoType;
import gr.ilsp.metashare.CorpusInfoType;
import gr.ilsp.metashare.CorpusInfoType.CorpusMediaType;
import gr.ilsp.metashare.CorpusTextInfo;
import gr.ilsp.metashare.CreationInfoType;
import gr.ilsp.metashare.DistributionInfo;
import gr.ilsp.metashare.IdentificationInfoType;
import gr.ilsp.metashare.LicenceInfo;
import gr.ilsp.metashare.ObjectFactory;
import gr.ilsp.metashare.OrganizationInfoType;
import gr.ilsp.metashare.OrganizationInfoType.OrganizationShortName;
import gr.ilsp.metashare.ProjectInfoType;
import gr.ilsp.metashare.ResourceCreationInfoType;
import gr.ilsp.metashare.ResourceInfoType;
import gr.ilsp.metashare.ResourceInfoType.ResourceComponentType;
import gr.ilsp.metashare.TargetResourceInfoType;
import gr.ilsp.metashare.TextFormatInfoType;

/**
 * Abstract class for metashare descriptors
 * @author prokopis
 */

public abstract class MetashareDescriptor {

	private static final String UTF_8 = "UTF-8";
	private static final String MIMETYPE = "application/x-tmx+xml";
	private static final String TEXT = "text";
	private static final String RESOURCE_TYPE = "corpus";
	private static final String ENG = "eng";
	private static final Logger logger = LoggerFactory.getLogger(MetashareDescriptor.class);
	//private static final String ILSPFC_NAME = "ILSP-FC";
	private static final String ILSPFC_URL = "http://nlp.ilsp.gr/redmine/projects/ilsp-fc/";

	private String metadataLang = ENG;
	private String resourceName = "resourceName";
	private String description = "resource description";

	private String organizationShortName = "ILSP";
	private String license = "CC-BY-NC-SA";
	private String organizationUrl = "http://www.ilsp.gr";

	private String givenNameStr = "ILSP";
	private String surNameStr = "ILSP";
	
	private String domain = "domain";
	private String domainId = "123";
	private String domainClassConformance = "Eurovoc";

	private String project = "ELRC";
	private String projectURL = "ELRC";
	
	private String creationDescription ="crawling; pair detection; segment alignments";
	private File outFile ;
	
	/**
	 * Once all appropriate metadata fields have been set, call this method to
	 * generate the metashare descriptors to the outFile
	 */
	public void run() {
		
		ObjectFactory objectFactory = new ObjectFactory();
		ResourceInfoType resourceInfoType = objectFactory.createResourceInfoType();
		
		//
		IdentificationInfoType identificationInfoType = objectFactory.createIdentificationInfoType();
		IdentificationInfoType.ResourceName resourceNameEle = new IdentificationInfoType.ResourceName();
		resourceNameEle.setLang(getMetadataLang());
		resourceNameEle.setValue(getResourceName());
		identificationInfoType.getResourceName().add(resourceNameEle);
		IdentificationInfoType.Description description = new IdentificationInfoType.Description();
		description.setLang(getMetadataLang());
		description.setValue(getDescription());
		identificationInfoType.getDescription().add(description);
	
		// 
		DistributionInfo distributionInfo = objectFactory.createDistributionInfo();
		LicenceInfo licenseInfo = objectFactory.createLicenceInfo();
		licenseInfo.setLicence(getLicense());
		
		distributionInfo.getLicenceInfo().add(licenseInfo);

		ResourceComponentType resourceComponentType = new ResourceComponentType();
		
		CorpusInfoType corpusInfoType = objectFactory.createCorpusInfoType();		
		resourceComponentType.setCorpusInfo(corpusInfoType);

		corpusInfoType.setResourceType(RESOURCE_TYPE);
		CorpusMediaType corpusMediaType = objectFactory.createCorpusInfoTypeCorpusMediaType();
		corpusInfoType.setCorpusMediaType(corpusMediaType);

		CorpusTextInfo corpusTextInfo = objectFactory.createCorpusTextInfo();
		corpusTextInfo.setMediaType(TEXT);
		
		TextFormatInfoType textFormatInfoType = objectFactory.createTextFormatInfoType();
		textFormatInfoType.setMimeType(MIMETYPE);
		
		CharacterEncodingInfoType characterEncodingInfoType = objectFactory.createCharacterEncodingInfoType();
		characterEncodingInfoType.setCharacterEncoding(UTF_8);

		createDomainInfo(objectFactory, corpusTextInfo);

		// 
		ResourceCreationInfoType resourceCreationInfoType = objectFactory.createResourceCreationInfoType();
		ProjectInfoType projectInfoType = objectFactory.createProjectInfoType();
		projectInfoType.setProjectID(getProject());
		projectInfoType.getUrl().add(projectURL);
		resourceCreationInfoType.getFundingProject().add(projectInfoType);
		
		ActorInfoType actorInfoType = objectFactory.createActorInfoType();
		OrganizationInfoType organization = objectFactory.createOrganizationInfoType();
		OrganizationShortName organizationShortName = objectFactory.createOrganizationInfoTypeOrganizationShortName();
		organizationShortName.setLang(this.getMetadataLang());
		organizationShortName.setValue(getOrganizationShortName());
		organization.getOrganizationShortName().add(organizationShortName);

		CommunicationInfoType communicationInfoType = objectFactory.createCommunicationInfoType();
		communicationInfoType.getUrl().add(organizationUrl);
		organization.setCommunicationInfo(communicationInfoType);
		actorInfoType.setOrganizationInfo(organization);

		resourceCreationInfoType.getResourceCreator().add(actorInfoType);
		
		try {
			resourceCreationInfoType.setCreationEndDate(CalendarUtils.getXMLGregorianCalendarNow());
		} catch (DatatypeConfigurationException e1) {
			logger.warn("Cannot set resourceCreationInfoType. Skipping. " );
			e1.printStackTrace();
		}
		
		//
		CreationInfoType creationInfoType = objectFactory.createCreationInfoType();
		creationInfoType.setCreationModeDetails (creationDescription);
		
		TargetResourceInfoType ilspfcResourceInfoType = objectFactory.createTargetResourceInfoType();
		ilspfcResourceInfoType.setTargetResourceNameURI(ILSPFC_URL);
		creationInfoType.getCreationTool().add(ilspfcResourceInfoType);
	
		createLingualityInfo(objectFactory, corpusTextInfo);
		createCorpusSizeInfo(objectFactory, corpusTextInfo);

		corpusTextInfo.getTextFormatInfo().add(textFormatInfoType);
		corpusTextInfo.getCharacterEncodingInfo().add(characterEncodingInfoType);
		corpusTextInfo.setCreationInfo(creationInfoType);
		corpusMediaType.getCorpusTextInfo().add(corpusTextInfo);
		
		createAnnotationInfo(objectFactory, corpusTextInfo);
		
		// FIXME
		resourceInfoType.setResourceCreationInfo(resourceCreationInfoType);
		resourceInfoType.setResourceComponentType(resourceComponentType);
		resourceInfoType.setIdentificationInfo(identificationInfoType);
		resourceInfoType.setDistributionInfo(distributionInfo);
		
		logger.debug("Starting creating metashare descriptor ");

		try {
			JAXBContext context= JAXBContext.newInstance("gr.ilsp.metashare");
			Marshaller jaxbMarshaller = context.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(resourceInfoType, getOutFile());

		} catch (Exception e) {
			logger.error("Cannot generate metashare descriptor for " + identificationInfoType.getResourceName());
			e.printStackTrace();
		}
	}

	public abstract void createAnnotationInfo(ObjectFactory objectFactory,  CorpusTextInfo corpusTextInfo) ;

	public abstract void createDomainInfo(ObjectFactory objectFactory,  CorpusTextInfo corpusTextInfo) ;

	public abstract void createCorpusSizeInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) ;

	protected abstract void createLingualityInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) ;

	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceName the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
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
	 * @return the domainClassConformance
	 */
	public String getDomainClassConformance() {
		return domainClassConformance;
	}

	/**
	 * @param domainClassConformance the domainClassConformance to set
	 */
	public void setDomainClassConformance(String domainClassConformance) {
		this.domainClassConformance = domainClassConformance;
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
	 * @return the eng
	 */
	public static String getEng() {
		return ENG;
	}

	/**
	 * @return the givenNameStr
	 */
	public String getGivenNameStr() {
		return givenNameStr;
	}

	/**
	 * @param givenNameStr the givenNameStr to set
	 */
	public void setGivenNameStr(String givenNameStr) {
		this.givenNameStr = givenNameStr;
	}

	/**
	 * @return the surNameStr
	 */
	public String getSurNameStr() {
		return surNameStr;
	}

	/**
	 * @param surNameStr the surNameStr to set
	 */
	public void setSurNameStr(String surNameStr) {
		this.surNameStr = surNameStr;
	}

	/**
	 * @return the organizationShortName
	 */
	public String getOrganizationShortName() {
		return organizationShortName;
	}

	/**
	 * @param organizationShortName the organizationShortName to set
	 */
	public void setOrganizationShortName(String organizationShortName) {
		this.organizationShortName = organizationShortName;
	}

	/**
	 * @return the organizationUrl
	 */
	public String getOrganizationUrl() {
		return organizationUrl;
	}

	/**
	 * @param organizationUrl the organizationUrl to set
	 */
	public void setOrganizationUrl(String organizationUrl) {
		this.organizationUrl = organizationUrl;
	}

	/**
	 * @return the outFile
	 */
	public File getOutFile() {
		return outFile;
	}

	/**
	 * @param outFile the outFile to set
	 */
	public void setOutFile(File outFile) {
		this.outFile = outFile;
	}

	/**
	 * @return the metadataLang
	 */
	public String getMetadataLang() {
		return metadataLang;
	}

	/**
	 * @param metadataLang the metadataLang to set
	 */
	public void setMetadataLang(String metadataLang) {
		this.metadataLang = metadataLang;
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

}
