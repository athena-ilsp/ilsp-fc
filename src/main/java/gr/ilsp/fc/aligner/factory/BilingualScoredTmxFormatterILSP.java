package gr.ilsp.fc.aligner.factory;


import gr.ilsp.fc.corpora.BilingualCorpusInformation;

import java.io.File;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import net.loomchild.maligna.coretypes.Alignment;
import net.loomchild.maligna.formatter.Formatter;
import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Body;
import net.loomchild.maligna.util.bind.tmx.Header;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Seg;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;
import net.loomchild.maligna.util.bind.tmx.Tuv;
import net.loomchild.maligna.util.date.DateParser;

import gr.ilsp.nlp.commons.Constants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BilingualScoredTmxFormatterILSP implements Formatter  {
	private static final String SEGMENT_CATEGROY = "segmentType";

	private static final String SCORE = "score";
	private static final String TYPE = "type";
	//private static final String SITE = "site";
	private static final String L1URL = "l1-url";
	private static final String L2URL = "l2-url";
	private static final String INFO = "info";
	private static final String LICENSE = "license";
	private static final String LENGTHRATIO = "lengthRatio";
	private static final String AVAILABILITY = "availability";
	private static final String DESCRIPTION ="description";
	private static final String SOURCE_FILE = "File in l1";
	private static final String TARGET_FILE = "File in l2";
	private static final String SOURCE_FILE_HTML = "FileHtml in l1";
	private static final String TARGET_FILE_HTML = "FileHtml in l2";
	private static final String TARGET_LANG = "l2";
	private static final String SOURCE_LANG = "l1";
	private static final String LENGTH_IN_TUs = "lengthInTUs";
	private static final String LENGTH_IN_TOKENS_L1 = "# of words in l1";
	private static final String LENGTH_IN_TOKENS_L2 = "# of words in l2";
	private static final String Num_Of_Unique_Words_L1 = "# of unique words in l1";
	private static final String Num_Of_Unique_Words_L2 = "# of unique words in l2";
	private static final String Mean_Aligner_Scores = "mean aligner's score";
	private static final String Std_Aligner_Scores = "std aligner's score";
	
	public static final String TMX_VERSION = "1.4";
	public static final String TMX_ADMINLANG = "en";
	private static final Logger logger = LoggerFactory.getLogger(BilingualScoredTmxFormatterILSP.class);
	public static final String TMX_CREATIONTOOL = "mALIGNa";
	public static final String TMX_CREATIONTOOLVERSION = "2";
	public static final String TMX_SEGTYPE = "block";
	public static final String TMX_DATATYPE = "plaintext";
	public static final String TMX_OTMF = "al";
	public static final String DISTRIBUTOR = "distributor";
	
	public static String description = "Acquisition of bilingual data (from multilingual websites), normalization, cleaning, deduplication and identification of parallel documents have been done by ILSP-FC tool. "
			+ "Maligna aligner was used for alignment of segments. Merging/filtering of segment pairs has also been applied.";
	
	public static String distributor ="ELRC project";
	
	private String sourceLanguageCode;
	
	private String targetLanguageCode;
	private File sourceFile;
	private File targetFile;
	private BilingualCorpusInformation corpusinfo;
	private Writer writer;
	
	private boolean skipZeroToOneAlignments = false;
	private boolean printAlignmentCategory = true;

	public BilingualScoredTmxFormatterILSP(Writer writer, String sourceLanguageCode, 
			String targetLanguageCode, File sourceFile, File targetFile, BilingualCorpusInformation bilingualCorpusInformation) {
		this.writer = writer;
		this.sourceLanguageCode = sourceLanguageCode;
		this.targetLanguageCode = targetLanguageCode;
		this.corpusinfo = bilingualCorpusInformation;
		if (sourceFile!=null) 
			this.sourceFile = sourceFile;
		if (targetFile!=null) 
			this.targetFile = targetFile;
	}
		
	public void formatILSPAlignments(List<ILSPAlignment> alignmentList) {
		logger.debug("Formatting alignment list...");
		
		Tmx tmx = new Tmx();
		Body body = new Body();
		createTmxHeader(tmx);
		
		int tuid = 1;
		for (ILSPAlignment alignment : alignmentList) {
			Tu tu = new Tu();
			tu.setTuid(Constants.EMPTY_STRING+tuid++);
			Prop scoreProperty = new Prop();
			scoreProperty.setType(SCORE);
			scoreProperty.getContent().add(Constants.EMPTY_STRING+alignment.getScore());
			tu.getNoteOrProp().add(scoreProperty);
			Prop typeProperty = new Prop();
			typeProperty.setType(TYPE);
			typeProperty.getContent().add(Constants.EMPTY_STRING+alignment.getType());
			tu.getNoteOrProp().add(typeProperty);
			Prop lengthRatio = new Prop();
			lengthRatio.setType(LENGTHRATIO);
			lengthRatio.getContent().add(Constants.EMPTY_STRING+alignment.getLengthRatio());
			tu.getNoteOrProp().add(lengthRatio);
			
			/*Prop siteProperty = new Prop();
			siteProperty.setType(SITE);
			siteProperty.getContent().add(EMPTY_STR+alignment.getSite());
			tu.getNoteOrProp().add(siteProperty);*/
						
			Prop l1urlProperty = new Prop();
			l1urlProperty.setType(L1URL);
			l1urlProperty.getContent().add(Constants.EMPTY_STRING+alignment.getL1url());
			tu.getNoteOrProp().add(l1urlProperty);
			
			Prop l2urlProperty = new Prop();
			l2urlProperty.setType(L2URL);
			l2urlProperty.getContent().add(Constants.EMPTY_STRING+alignment.getL2url());
			tu.getNoteOrProp().add(l2urlProperty);
						
			Prop licenseProperty = new Prop();
			licenseProperty.setType(LICENSE);
			licenseProperty.getContent().add(Constants.EMPTY_STRING+alignment.getLicense());
			tu.getNoteOrProp().add(licenseProperty);
			Prop infoProperty = new Prop();
			infoProperty.setType(INFO);
			infoProperty.getContent().add(Constants.EMPTY_STRING+alignment.getInfo());
			tu.getNoteOrProp().add(infoProperty);
						
			List<String> sourceSegments = alignment.getSourceSegmentList();
			List<String> targetSegments = alignment.getTargetSegmentList();
			
			if (skipZeroToOneAlignments && ( sourceSegments.size() == 0 || targetSegments.size() == 0 ))
				continue;
			
			if (printAlignmentCategory) {
				Prop alignmentCategoryProperty = new Prop();
				alignmentCategoryProperty.setType(SEGMENT_CATEGROY);
				alignmentCategoryProperty.getContent().add(sourceSegments.size()+ ":"+ targetSegments.size());
				tu.getNoteOrProp().add(alignmentCategoryProperty);
			}
			
			if (sourceSegments.size() > 1 || targetSegments.size() > 1) {
				logger.debug("Source " + sourceSegments.size());
				for (String sourceSegment : sourceSegments) {
					logger.debug(sourceSegment);
				}
				logger.debug("Target " + targetSegments.size());
				for (String targetSegment : targetSegments) {
					logger.debug(targetSegment);
				}
			}
			createTuv(tu, sourceLanguageCode, sourceSegments);
			createTuv(tu, targetLanguageCode, targetSegments);
			if (tu.getTuv().size() > 0)
				body.getTu().add(tu);
		}
		tmx.setBody(body);
		TmxMarshallerUnmarshaller.getInstance().marshal(tmx, writer);	
	}
	
	public void format(List<Alignment> alignmentList) {
		logger.debug("Formatting alignment list...");
		
		Tmx tmx = new Tmx();
		createTmxHeader(tmx);
		Body body = new Body();
		int tuid = 1;
		for (Alignment alignment : alignmentList) {
			Tu tu = new Tu();
			tu.setTuid(Constants.EMPTY_STRING+tuid++);
			Prop scoreProperty = new Prop();
			scoreProperty.setType(SCORE);
			scoreProperty.getContent().add(Constants.EMPTY_STRING+alignment.getScore());
			tu.getNoteOrProp().add(scoreProperty);

			List<String> sourceSegments = alignment.getSourceSegmentList();
			List<String> targetSegments = alignment.getTargetSegmentList();
			
			if (skipZeroToOneAlignments && ( sourceSegments.size() == 0 || targetSegments.size() == 0 )) 
				continue;
			
			if (printAlignmentCategory) {
				Prop alignmentCategoryProperty = new Prop();
				alignmentCategoryProperty.setType(SEGMENT_CATEGROY);
				alignmentCategoryProperty.getContent().add(sourceSegments.size()+ ":"+ targetSegments.size());
				tu.getNoteOrProp().add(alignmentCategoryProperty);
			}
			
			if (sourceSegments.size() > 1 || targetSegments.size() > 1) {
				logger.debug("Source " + sourceSegments.size());
				for (String sourceSegment : sourceSegments) {
					logger.debug(sourceSegment);
				}
				logger.debug("Target " + targetSegments.size());
				for (String targetSegment : targetSegments) {
					logger.debug(targetSegment);
				}
			}
			createTuv(tu, sourceLanguageCode, sourceSegments);
			createTuv(tu, targetLanguageCode, targetSegments);
			if (tu.getTuv().size() > 0) 
				body.getTu().add(tu);
		}
		tmx.setBody(body);
		TmxMarshallerUnmarshaller.getInstance().marshal(tmx, writer);
	}
	

	/**
	 * @param tmx
	 */
	private void createTmxHeader(Tmx tmx) {
		tmx.setVersion(TMX_VERSION);
		Header header = new Header();
		header.setAdminlang(TMX_ADMINLANG);
		header.setSrclang(sourceLanguageCode);
		header.setCreationtool(TMX_CREATIONTOOL);
		header.setCreationtoolversion(TMX_CREATIONTOOLVERSION);
		header.setSegtype(TMX_SEGTYPE);
		header.setDatatype(TMX_DATATYPE);
		header.setOTmf(TMX_OTMF);
		header.setCreationdate(DateParser.getIsoDateNoMillis(new Date()));

		if (!(sourceFile==null || targetFile==null)) {
			Prop sourceFileProperty = new Prop();
			sourceFileProperty.setType(SOURCE_FILE);
			sourceFileProperty.getContent().add(sourceFile.getName());
			header.getNoteOrPropOrUde().add(sourceFileProperty);
	
			Prop targetFileProperty = new Prop();
			targetFileProperty.setType(TARGET_FILE);
			targetFileProperty.getContent().add(targetFile.getName());
			header.getNoteOrPropOrUde().add(targetFileProperty);
	
			Prop sourceFileHtmlProperty = new Prop();
			sourceFileHtmlProperty.setType(SOURCE_FILE_HTML);
			sourceFileHtmlProperty.getContent().add(sourceFile.getName().replace(".xml", ".html"));
			header.getNoteOrPropOrUde().add(sourceFileHtmlProperty);
	
			Prop targetFileHtmlProperty = new Prop();
			targetFileHtmlProperty.setType(TARGET_FILE_HTML);
			targetFileHtmlProperty.getContent().add(targetFile.getName().replace(".xml", ".html"));
			header.getNoteOrPropOrUde().add(targetFileHtmlProperty);
		}
		
		Prop distributorProperty = new Prop();
		distributorProperty.setType(DISTRIBUTOR);
		distributorProperty.getContent().add(distributor);
		header.getNoteOrPropOrUde().add(distributorProperty);
		
		Prop descrProperty = new Prop();
		descrProperty.setType(DESCRIPTION);
		descrProperty.getContent().add(description);
		header.getNoteOrPropOrUde().add(descrProperty);
		
		Prop availProperty = new Prop();
		availProperty.setType(AVAILABILITY);
		availProperty.getContent().add(corpusinfo.getAvailability());
		header.getNoteOrPropOrUde().add(availProperty);
		
		Prop sourceLangProperty = new Prop();
		sourceLangProperty.setType(SOURCE_LANG);
		sourceLangProperty.getContent().add(sourceLanguageCode);
		header.getNoteOrPropOrUde().add(sourceLangProperty);

		Prop targetLangProperty = new Prop();
		targetLangProperty.setType(TARGET_LANG);
		targetLangProperty.getContent().add(targetLanguageCode);
		header.getNoteOrPropOrUde().add(targetLangProperty);
		//tmx.setHeader(header);
		
		Prop LengthInTUsProperty = new Prop();
		LengthInTUsProperty.setType(LENGTH_IN_TUs);
		LengthInTUsProperty.getContent().add(Integer.toString(corpusinfo.getAlignmentsSize()));
		header.getNoteOrPropOrUde().add(LengthInTUsProperty);
		//tmx.setHeader(header);
		
		Prop LengthInTokensPropertyL1 = new Prop();
		LengthInTokensPropertyL1.setType(LENGTH_IN_TOKENS_L1);
		LengthInTokensPropertyL1.getContent().add(Integer.toString(corpusinfo.getLenInWordsL1()));
		header.getNoteOrPropOrUde().add(LengthInTokensPropertyL1);
		//tmx.setHeader(header);
		
		Prop LengthInTokensPropertyL2 = new Prop();
		LengthInTokensPropertyL2.setType(LENGTH_IN_TOKENS_L2);
		LengthInTokensPropertyL2.getContent().add(Integer.toString(corpusinfo.getLenInWordsL2()));
		header.getNoteOrPropOrUde().add(LengthInTokensPropertyL2);
		//tmx.setHeader(header);
		
		Prop NumOfUniqueWordsPropertyL1 = new Prop();
		NumOfUniqueWordsPropertyL1.setType(Num_Of_Unique_Words_L1);
		NumOfUniqueWordsPropertyL1.getContent().add(Integer.toString(corpusinfo.getVocSizeInL1()));
		header.getNoteOrPropOrUde().add(NumOfUniqueWordsPropertyL1);
		//tmx.setHeader(header);
		
		Prop NumOfUniqueWordsPropertyL2 = new Prop();
		NumOfUniqueWordsPropertyL2.setType(Num_Of_Unique_Words_L2);
		NumOfUniqueWordsPropertyL2.getContent().add(Integer.toString(corpusinfo.getVocSizeInL2()));
		header.getNoteOrPropOrUde().add(NumOfUniqueWordsPropertyL2);
		
		Prop Mean_Aligner_Scores1 = new Prop();
		Mean_Aligner_Scores1.setType(Mean_Aligner_Scores);
		Mean_Aligner_Scores1.getContent().add(Double.toString(corpusinfo.getMeanAlignScore()));
		header.getNoteOrPropOrUde().add(Mean_Aligner_Scores1);
		
		Prop Std_Aligner_Scores1 = new Prop();
		Std_Aligner_Scores1.setType(Std_Aligner_Scores);
		Std_Aligner_Scores1.getContent().add(Double.toString(corpusinfo.getStdAlignScore()));
		header.getNoteOrPropOrUde().add(Std_Aligner_Scores1);
		
		tmx.setHeader(header);
	}

	private void createTuv(Tu tu, String languageCode, 
			List<String> segmentList) {
		if (segmentList.size() > 0) {
			Tuv tuv = new Tuv();			
			tuv.setLang(languageCode);
			Seg seg = new Seg();
			String segment = StringUtils.join(segmentList, Constants.SPACE);
			seg.getContent().add(segment.trim());
			tuv.setSeg(seg);
			tu.getTuv().add(tuv);
		}
	}

	/**
	 * @return the skipZeroToOneAlignments
	 */
	public Boolean getSkipZeroToOneAlignments() {
		return skipZeroToOneAlignments;
	}

	/**
	 * @param skipZeroToOneAlignments the skipZeroToOneAlignments to set
	 */
	public void setSkipZeroToOneAlignments(Boolean skipZeroToOneAlignments) {
		this.skipZeroToOneAlignments = skipZeroToOneAlignments;
	}

	/**
	 * @return the printAlignmentCategory
	 */
	public boolean isPrintAlignmentCategory() {
		return printAlignmentCategory;
	}

	/**
	 * @param printAlignmentCategory the printAlignmentCategory to set
	 */
	public void setPrintAlignmentCategory(boolean printAlignmentCategory) {
		this.printAlignmentCategory = printAlignmentCategory;
	}

}
