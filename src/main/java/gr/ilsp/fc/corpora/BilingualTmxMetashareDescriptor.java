package gr.ilsp.fc.corpora;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.metashare.AnnotationInfoType;
import gr.ilsp.metashare.CorpusTextInfo;
import gr.ilsp.metashare.DomainInfoType;
import gr.ilsp.metashare.LanguageInfoType;
import gr.ilsp.metashare.LingualityInfo;
import gr.ilsp.metashare.ObjectFactory;
import gr.ilsp.metashare.SizeInfoType;
import gr.ilsp.metashare.TargetResourceInfoType;

/**
 * Class to generate metashare compatible descriptors for one-tmx file datasets.
 * 
 * @author prokopis
 *
 */
public class BilingualTmxMetashareDescriptor extends MetashareDescriptor {

	private String l1;
	private String l2;
	
	private List<SizeInfoType> l1SizeInfoTypes = new ArrayList<SizeInfoType>();
	private List<SizeInfoType> l2SizeInfoTypes = new ArrayList<SizeInfoType>();
	
	private String tuSize;
	private String tuSizeUnit;		

	private static final String LINGUALITY = "bilingual";
	
	private static final Logger logger = LoggerFactory.getLogger(BilingualTmxMetashareDescriptor.class);
	
	public BilingualTmxMetashareDescriptor(BilingualCorpusInformation bilingualCorpusInfo) {
		logger.debug("Initializing from bilingualCorpusInfo");
		
		if (bilingualCorpusInfo.getAvailability().equalsIgnoreCase("unknown")) {
			this.setLicense("underReview");
		}
		
		this.setOrganizationShortName(bilingualCorpusInfo.getOrganization());
		this.setOrganizationUrl(bilingualCorpusInfo.getOrganizationURL());
		this.setProject(bilingualCorpusInfo.getProjectId());
		this.setProjectURL(bilingualCorpusInfo.getProjectURL());
		
		this.setL1(bilingualCorpusInfo.getL1());
		this.setL2(bilingualCorpusInfo.getL2());
		this.setResourceName(bilingualCorpusInfo.getName());
		this.setDescription(bilingualCorpusInfo.getDescription());
		this.setCreationDescription(bilingualCorpusInfo.getCreationDescription());

		// L1
		SizeInfoType l1LenInWordsInfoType = new SizeInfoType();
		l1LenInWordsInfoType.setSize(Integer.toString(bilingualCorpusInfo.getLenInWordsL1()));
		l1LenInWordsInfoType.setSizeUnit("words");
		l1SizeInfoTypes.add(l1LenInWordsInfoType);

		SizeInfoType l1VocabInfoType = new SizeInfoType();
		l1VocabInfoType.setSize(Integer.toString(bilingualCorpusInfo.getVocSizeInL1()));
		l1VocabInfoType.setSizeUnit("lexicalTypes");
		l1SizeInfoTypes.add(l1VocabInfoType);
		
		// L2		
		SizeInfoType l2LenInWordsInfoType = new SizeInfoType();
		l2LenInWordsInfoType.setSize(Integer.toString(bilingualCorpusInfo.getLenInWordsL2()));
		l2LenInWordsInfoType.setSizeUnit("words");
		l2SizeInfoTypes.add(l2LenInWordsInfoType);

		SizeInfoType l2VocabInfoType = new SizeInfoType();
		l2VocabInfoType.setSize(Integer.toString(bilingualCorpusInfo.getVocSizeInL2()));
		l2VocabInfoType.setSizeUnit("lexicalTypes");
		l2SizeInfoTypes.add(l2VocabInfoType);

		tuSize = Integer.toString(bilingualCorpusInfo.getAlignmentList().size());
		tuSizeUnit = "translationUnits";
		
		this.setDomain(bilingualCorpusInfo.getDomain());
		this.setDomainId(bilingualCorpusInfo.getDomainId());
	}

	@Override
	public void createDomainInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) {
		DomainInfoType domain1InfoType = objectFactory.createDomainInfoType();
		domain1InfoType.getDomain().add(getDomain());
		domain1InfoType.getDomainId().add(getDomainId());
		if (StringUtils.isBlank(getDomainId())) {
			domain1InfoType.setConformanceToClassificationScheme("");
		} else {
			domain1InfoType.setConformanceToClassificationScheme(getDomainClassConformance());
		}
		corpusTextInfo.getDomainInfo().add(domain1InfoType);
	}
	
	
	@Override
	public void createLingualityInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo)  {

		LingualityInfo lingualityInfo = objectFactory.createLingualityInfo();
		lingualityInfo.setLingualityType(LINGUALITY);
		corpusTextInfo.setLingualityInfo(lingualityInfo);


		LanguageInfoType l1Info = objectFactory.createLanguageInfoType();
		l1Info.setLanguageId(getL1());
		
		for (SizeInfoType l1SizeInfoType: l1SizeInfoTypes) {
			l1Info.getSizePerLanguage().add(l1SizeInfoType);
		}

		LanguageInfoType l2Info = objectFactory.createLanguageInfoType();
		l2Info.setLanguageId(getL2());

		for (SizeInfoType l2SizeInfoType: l2SizeInfoTypes) {
			l2Info.getSizePerLanguage().add(l2SizeInfoType);
		}
		
		corpusTextInfo.getLanguageInfo().add(l1Info);
		corpusTextInfo.getLanguageInfo().add(l2Info);

	}
	
	@Override
	public void createCorpusSizeInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) {
		SizeInfoType corpusSizeInfoType = objectFactory.createSizeInfoType();
		corpusSizeInfoType.setSize(getTuSize());
		corpusSizeInfoType.setSizeUnit(getTuSizeUnit());
		corpusTextInfo.getSizeInfo().add(corpusSizeInfoType);
	}

	@Override
	public void createAnnotationInfo(ObjectFactory objectFactory, CorpusTextInfo corpusTextInfo) {
		AnnotationInfoType annotationInfoType = objectFactory.createAnnotationInfoType();
		annotationInfoType.setAnnotationType("sentence alignment");
		annotationInfoType.setAnnotationStandoff(false);
		annotationInfoType.getConformanceToStandardsBestPractices().add("TMX");
		annotationInfoType.getSegmentationLevel().add("sentence");
		annotationInfoType.setAnnotationFormat("application/x-tmx+xml");
		annotationInfoType.setAnnotationMode("automatic");
		TargetResourceInfoType alignerResourceInfoType = objectFactory.createTargetResourceInfoType();
		alignerResourceInfoType.setTargetResourceNameURI("ILSP-FC alignment and TMX filtering module");
		annotationInfoType.getAnnotationTool().add(alignerResourceInfoType);
		corpusTextInfo.getAnnotationInfo().add(annotationInfoType);
	}

	/**
	 * @return the tuSize
	 */
	public String getTuSize() {
		return tuSize;
	}

	/**
	 * @param tuSize the tuSize to set
	 */
	public void setTuSize(String tuSize) {
		this.tuSize = tuSize;
	}

	/**
	 * @return the tuSizeUnit
	 */
	public String getTuSizeUnit() {
		return tuSizeUnit;
	}

	/**
	 * @param tuSizeUnit the tuSizeUnit to set
	 */
	public void setTuSizeUnit(String tuSizeUnit) {
		this.tuSizeUnit = tuSizeUnit;
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


}
