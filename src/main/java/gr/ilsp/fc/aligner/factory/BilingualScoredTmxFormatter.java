package gr.ilsp.fc.aligner.factory;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BilingualScoredTmxFormatter implements Formatter  {
	private static final String SEGMENT_CATEGROY = "segmentType";

	private static final String SCORE = "score";
	
	private static final String SOURCE_FILE = "sourceFile";

	private static final String TARGET_FILE = "targetFile";

	private static final String SOURCE_FILE_HTML = "sourceFileHtml";

	private static final String TARGET_FILE_HTML = "targetFileHtml";

	
	private static final String TARGET_LANG = "targetLang";

	private static final String SOURCE_LANG = "sourceLang";

	private static final String EMPTY_STR = "";

	private static final String SPACE_STR = " ";

	public static final String TMX_VERSION = "1.4b";

	public static final String TMX_ADMINLANG = "en";
	private static final Logger logger = LoggerFactory.getLogger(BilingualScoredTmxFormatter.class);

	public static final String TMX_CREATIONTOOL = "mALIGNa";

	public static final String TMX_CREATIONTOOLVERSION = "2";

	public static final String TMX_SEGTYPE = "block";

	public static final String TMX_DATATYPE = "plaintext";
	
	public static final String TMX_OTMF = "al";

	private String sourceLanguageCode;

	private String targetLanguageCode;
	//private String numOfTUs;
	//private String numOfTokensSL;
	//private String numOfTokensTL;
	private File sourceFile;
	private File targetFile;
	private Writer writer;
	
	private boolean skipZeroToOneAlignments = false;
	private boolean printAlignmentCategory = true;

		
	public BilingualScoredTmxFormatter(Writer writer, String sourceLanguageCode, String targetLanguageCode, File sourceFile, File targetFile) {
		this.writer = writer;
		this.sourceLanguageCode = sourceLanguageCode;
		this.targetLanguageCode = targetLanguageCode;
				
		if (sourceFile!=null) {
			this.sourceFile = sourceFile;
		} 
		if (targetFile!=null) {
			this.targetFile = targetFile;
		}
	}
	
	
/*	public void formatILSPAlignments(List<ILSPAlignment> alignmentList) {
		logger.debug("Formatting alignment list...");
		
		Tmx tmx = new Tmx();
		Body body = new Body();
		createTmxHeader(tmx);
		
		int tuid = 1;
		for (ILSPAlignment alignment : alignmentList) {
			Tu tu = new Tu();
			tu.setTuid(EMPTY_STR+tuid++);
			Prop scoreProperty = new Prop();
			scoreProperty.setType(SCORE);
			scoreProperty.getContent().add(EMPTY_STR+alignment.getScore());
			tu.getNoteOrProp().add(scoreProperty);
			Prop typeProperty = new Prop();
			typeProperty.setType(TYPE);
			typeProperty.getContent().add(EMPTY_STR+alignment.getType());
			tu.getNoteOrProp().add(typeProperty);
			Prop lengthRatio = new Prop();
			lengthRatio.setType(LENGTHRATIO);
			lengthRatio.getContent().add(EMPTY_STR+alignment.getLengthRatio());
			tu.getNoteOrProp().add(lengthRatio);
			Prop siteProperty = new Prop();
			siteProperty.setType(SITE);
			siteProperty.getContent().add(EMPTY_STR+alignment.getSite());
			tu.getNoteOrProp().add(siteProperty);
			Prop licenseProperty = new Prop();
			licenseProperty.setType(LICENSE);
			licenseProperty.getContent().add(EMPTY_STR+alignment.getLicense());
			tu.getNoteOrProp().add(licenseProperty);
						
			List<String> sourceSegments = alignment.getSourceSegmentList();
			List<String> targetSegments = alignment.getTargetSegmentList();
			
			if (skipZeroToOneAlignments && ( sourceSegments.size() == 0 || targetSegments.size() == 0 )) {
				continue;
			}
			
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
			if (tu.getTuv().size() > 0) {
				body.getTu().add(tu);
			}
		}
		tmx.setBody(body);
		TmxMarshallerUnmarshaller.getInstance().marshal(tmx, writer);	
	}*/
	
	public void format(List<Alignment> alignmentList) {
		logger.debug("Formatting alignment list...");
		
		Tmx tmx = new Tmx();
		createTmxHeader(tmx);
		Body body = new Body();
		int tuid = 1;
		for (Alignment alignment : alignmentList) {
			Tu tu = new Tu();
			tu.setTuid(EMPTY_STR+tuid++);
			Prop scoreProperty = new Prop();
			scoreProperty.setType(SCORE);
			scoreProperty.getContent().add(EMPTY_STR+alignment.getScore());
			tu.getNoteOrProp().add(scoreProperty);

			List<String> sourceSegments = alignment.getSourceSegmentList();
			List<String> targetSegments = alignment.getTargetSegmentList();
			
			if (skipZeroToOneAlignments && ( sourceSegments.size() == 0 || targetSegments.size() == 0 )) {
				continue;
			}
			
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
			if (tu.getTuv().size() > 0) {
				body.getTu().add(tu);
			}
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
		
		Prop sourceLangProperty = new Prop();
		sourceLangProperty.setType(SOURCE_LANG);
		sourceLangProperty.getContent().add(sourceLanguageCode);
		header.getNoteOrPropOrUde().add(sourceLangProperty);

		Prop targetLangProperty = new Prop();
		targetLangProperty.setType(TARGET_LANG);
		targetLangProperty.getContent().add(targetLanguageCode);
		header.getNoteOrPropOrUde().add(targetLangProperty);
		tmx.setHeader(header);
	}

		
	
	
	


	private void createTuv(Tu tu, String languageCode, 
			List<String> segmentList) {
		if (segmentList.size() > 0) {
			Tuv tuv = new Tuv();			
			tuv.setLang(languageCode);

			Seg seg = new Seg();
			String segment = StringUtils.join(segmentList, SPACE_STR);
			//String segment = merge(segmentList); 
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
