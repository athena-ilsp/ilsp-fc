package gr.ilsp.fc.corpora;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.metashare.CorpusTextInfo;
import gr.ilsp.metashare.DomainInfoType;
import gr.ilsp.metashare.LanguageInfoType;
import gr.ilsp.metashare.LingualityInfo;
import gr.ilsp.metashare.ObjectFactory;
import gr.ilsp.metashare.SizeInfoType;

/**
 * Class to generate metashare compatible descriptors for monolingual datasets.
 * 
 */
public class MonolingualMetashareDescriptor extends MetashareDescriptor {

	private String lang;
	private List<SizeInfoType> langSizeInfoTypes = new ArrayList<SizeInfoType>();
	
	private static final String LINGUALITY = "monolingual";
	private static final String DOCUMENTS = "files";
	private static final String SENTENCES = "sentences";
	private static final String PARAGRAPHS = "paragraphs";
	
	private static final Logger logger = LoggerFactory.getLogger(MonolingualMetashareDescriptor.class);
	
	public MonolingualMetashareDescriptor(MonolingualCorpusInformation monolingualCorpusInformation) {
		logger.debug("Initializing from monolingualCorpusInfo");
		
		if (monolingualCorpusInformation.getAvailability().equalsIgnoreCase("unknown")) {
			this.setLicense("underReview");
		}
		
		this.setMimetype("application/xml");
		this.setOrganizationShortName(monolingualCorpusInformation.getOrganization());
		this.setOrganizationUrl(monolingualCorpusInformation.getOrganizationURL());
		this.setProject(monolingualCorpusInformation.getProjectId());
		this.setProjectURL(monolingualCorpusInformation.getProjectURL());
		
		this.setLang(monolingualCorpusInformation.getLang());
		this.setResourceName(monolingualCorpusInformation.getName());
		this.setDescription(monolingualCorpusInformation.getDescription());
		this.setCreationDescription(monolingualCorpusInformation.getCreationDescription());

		SizeInfoType docSizeInfoType = new SizeInfoType();
		if (monolingualCorpusInformation.getLevel().equals(DOCUMENTS))
			docSizeInfoType.setSize(Integer.toString(monolingualCorpusInformation.getFilesNum()));
		if (monolingualCorpusInformation.getLevel().equals(PARAGRAPHS))
			docSizeInfoType.setSize(Integer.toString(monolingualCorpusInformation.getParagraphsNum()));
		if (monolingualCorpusInformation.getLevel().equals(SENTENCES))
			docSizeInfoType.setSize(Integer.toString(monolingualCorpusInformation.getSentenceNum()));
		docSizeInfoType.setSizeUnit(monolingualCorpusInformation.getLevel());		//docSizeInfoType.setSizeUnit("files");
		
		langSizeInfoTypes.add(docSizeInfoType);

		SizeInfoType wordsSizeInfoType = new SizeInfoType();
		wordsSizeInfoType.setSize(Integer.toString(monolingualCorpusInformation.getTokensNum()));
		wordsSizeInfoType.setSizeUnit("words");
		langSizeInfoTypes.add(wordsSizeInfoType);

		SizeInfoType vocSizeInfoType = new SizeInfoType();
		vocSizeInfoType.setSize(Integer.toString(monolingualCorpusInformation.getLexTypesNum()));
		vocSizeInfoType.setSizeUnit("lexicalTypes");
		langSizeInfoTypes.add(vocSizeInfoType);
		
		this.setDomain(monolingualCorpusInformation.getDomain());
		this.setDomainId(monolingualCorpusInformation.getDomainId());
	}

	@Override
	public void createDomainInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) {
		DomainInfoType domainInfoType = objectFactory.createDomainInfoType();
		domainInfoType.getDomain().add(getDomain());
		domainInfoType.getDomainId().add(getDomainId());
		if (StringUtils.isBlank(getDomainId())) {
			domainInfoType.setConformanceToClassificationScheme("");
		} else {
			domainInfoType.setConformanceToClassificationScheme(getDomainClassConformance());
		}
		corpusTextInfo.getDomainInfo().add(domainInfoType);
	}
	
	
	@Override
	public void createLingualityInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo)  {
		LingualityInfo lingualityInfo = objectFactory.createLingualityInfo();
		lingualityInfo.setLingualityType(LINGUALITY);
		corpusTextInfo.setLingualityInfo(lingualityInfo);

		LanguageInfoType languageInfoType = objectFactory.createLanguageInfoType();
		languageInfoType.setLanguageId(this.getLang());
		corpusTextInfo.getLanguageInfo().add(languageInfoType);
	}
	
	@Override
	public void createCorpusSizeInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) {
		LanguageInfoType langInfo = objectFactory.createLanguageInfoType();
		langInfo.setLanguageId(getLang());
		
		for (SizeInfoType langSizeInfoType: langSizeInfoTypes) {
			corpusTextInfo.getSizeInfo().add(langSizeInfoType);
		}

	}

	@Override
	public void createAnnotationInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) {
//		AnnotationInfoType annotationInfoType = objectFactory.createAnnotationInfoType();
//		annotationInfoType.setAnnotationType("");
//		annotationInfoType.setAnnotationStandoff(false);
//		annotationInfoType.getConformanceToStandardsBestPractices().add("XML");
//		annotationInfoType.getSegmentationLevel().add("document");
//		annotationInfoType.setAnnotationFormat("application/xml");
//		annotationInfoType.setAnnotationMode("automatic");
//		TargetResourceInfoType toolResourceInfoType = objectFactory.createTargetResourceInfoType();
//		toolResourceInfoType.setTargetResourceNameURI("ilsp-fc url");
//		annotationInfoType.getAnnotationTool().add(toolResourceInfoType);
//		corpusTextInfo.getAnnotationInfo().add(annotationInfoType);
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




}
