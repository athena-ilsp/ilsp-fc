package gr.ilsp.fc.utils;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.corpora.BilingualTmxMetashareDescriptor;
import gr.ilsp.fc.tmxhandler.TMXHandler;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import net.loomchild.maligna.util.bind.tmx.Tu;

public class GetTMXsubset {

	private static final Logger logger = LoggerFactory.getLogger(GetTMXsubset.class);
	private final static String mes1 = "non-letters";
	private final static String mes2 = "equal TUVs";
	//private final static String mes3 = "very long token, longer than ";
	//private final static String mes3a = "very long tokens, median length is longer than ";
	private final static String mes4 = "very short segments, shorter than ";
	private final static String mes5 = "charlength ratio of TUVs is lower than ";
	private final static String mes5a = " or higher than ";
	private final static String mes6 = "different numbers in TUVs";
	private final static String mes7 = "duplicate";
	private final static String eAddressNode ="site"; 
	private final static String licenseNode = "license";
	private static final String SPACE_SEPARATOR = " ";
	private final static String SCORE = "score";
	private final static String SEGMENTTYPE = "type"; 
	private static boolean iso6393=false;
	private static boolean oxslt=false;
	private static boolean cc=true;
	private static boolean keepem = true;
	private static boolean keepiden = true;
	private static boolean keepdup = true;
	private static boolean keepsn=false;
	private static int minTuvLen=3;
	private static double minPerce01Align=0.16;
	private static double minTuLenRatio = 0.6;
	private static double maxTuLenRatio = 1.6;
	private static double median_word_length=20;
	private static double max_word_length=30;
	private static Set<String> segs = new HashSet<String>() ;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	private static final String HTML =".html";
	private static final String TMXEXT = ".tmx";
	private final static String MetadataExt = ".md.xml";
	private final static String TAB = "\t";
	private static File baseName=null;
	private static File outTMX=null;
	private static File outHTML=null;
	private static String targetdomain="culture";

	private final static String UNKNOWN_STR ="unknown";
	private final static String FREE_STR="free";

	private static String alignerStr = "Maligna";

	private static String creationDescription = "The ILSP Focused Crawler was used for the acquisition "
			+ "of bilingual data from multilingual websites, and for the normalization, cleaning, deduplication and identification of parallel documents. "
			+ "The " + alignerStr + " sentence aligner was used for extracting segment alignments from crawled parallel documents. "
			+ "As a post-processing step, alignments were merged into one TMX file. "
			+ "The following filters were applied: ";
	private static String filter6 = " Alignments in which different digits appear in each TUV were kept and annotated.";
	private static String filter7 = " Alignments with identical TUVs (after normalization) were removed.";
	private static String filter8 = " Alignments with only non-letters in at least one of their TUVs were removed";
	private static String filter9 = " Duplicate alignments were discarded.";


	public static void main(String[] args){
		File tmxFile = new File(args[0]);
		String l1=args[1];
		String l2=args[2];
		baseName = new File(args[3]);
		outTMX = new File(baseName.getAbsolutePath()+TMXEXT);
		outHTML =  new File(baseName.getAbsolutePath() + HTML);
		targetdomain = args[4];
		logger.info("Reading " + tmxFile.getAbsolutePath());
		logger.info("Languages: " + l1 + " " + l2 + " ");
		logger.info("Constructing " + outTMX.getAbsolutePath());
		getTMXsubset(tmxFile, l1, l2);
	}




	public static void getTMXsubset(File tmxFile, String l1, String l2) {

		String filter1=" TMX files generated from document pairs which have been identified by non-"+ "aupdih" + " methods were discarded.";
		String filter2=" TMX files with a zeroToOne_alignments/total_alignments ratio is larger than "+ minPerce01Align + ", were discarded.";
		String filter3=" Alignments of non-" + "1:1"+ " were discarded.";
		String filter4=" Alignments with a TUV (after normalization) that has less than "+ minTuvLen + " tokens, were discarded/annotated.";
		String filter5=" Alignments with a TUVs' length ratio less than " + minTuLenRatio+ " or more than "+ maxTuLenRatio + ", were discarded/annotated.";
		if (keepsn)
			filter6=" Alignments in which different digits appear in each TUV were discarded.";
		if (keepiden)
			filter7=" Alignments with identical TUVs (after normalization) were kept and annotated.";
		if (keepem)
			filter8=" Alignments with only non-letters in at least one of their TUVs were kept and annotated.";
		if (keepdup)
			filter9=" Duplicate alignments were kept and annotated.";
		logger.info(filter1+"\n"+filter2+"\n"+filter3+"\n"+filter4+"\n"+filter5+"\n"+filter6+"\n"+filter7+"\n"+filter8+"\n"+filter9);

		List<String> domains = new ArrayList<String>();
		domains.add(targetdomain);
		List<String> domainEurovocIds=TMXHandler.getEurovocId(domains);
		String domain = StringUtils.join(domains, ',');
		String domainEurovocId = StringUtils.join(domainEurovocIds, ',');

		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();

		List<SegPair> segpairs = getTUsFromTMX(tmxFile,0, minPerce01Align, l1, l2, true);

		alignmentList = getSegs(segpairs,alignmentList, keepem, keepiden, keepdup, keepsn, cc);

		if (!alignmentList.isEmpty()){
			int[] stats1 =TMXHandlerUtils.countWordsInTMX(alignmentList, 1, true);
			int[] stats2 =TMXHandlerUtils.countWordsInTMX(alignmentList, 2, true);

			String organization = "ILSP";
			String organizationURL = "http://www.ilsp.gr"; 
			String projectId= "ELRC"; 
			String projectURL = "http://lr-coordination.eu/"; 

			creationDescription = creationDescription+filter1+" ; "+filter2+" ; "+filter3+" ; "+filter4+" ; "+filter5+" ; "+filter6+" ; "+filter7+" ; "+filter8+" ; "+filter9;

			BilingualCorpusInformation bilingualCorpusInfo;
			if (cc) {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), l1, l2, 
						alignmentList, alignmentList.size(), stats1[5], stats1[0], stats2[0],stats1[1], stats2[1], stats1[2], stats2[2],stats1[3], stats2[3],
						domain, domainEurovocId, FREE_STR, creationDescription,
						projectId, projectURL, organization, organizationURL);
			} else {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), l1, l2, 
						alignmentList, alignmentList.size(), stats1[5], stats1[0], stats2[0],stats1[1], stats2[1], stats1[2], stats2[2],stats1[3], stats2[3], 
						domain, domainEurovocId, UNKNOWN_STR, creationDescription,
						projectId, projectURL, organization, organizationURL);
			}
			if (oxslt) 
				outHTML =  new File(baseName.getAbsolutePath() + HTML);
			String[] languages = new String[2]; languages[0]=l1; languages[1]=l2;
			TMXHandler.generateMergedTMX(outTMX, languages, bilingualCorpusInfo, outHTML);

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName.getAbsolutePath()+ MetadataExt);
			logger.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			if (iso6393) {
				bilingualTmxMetashareDescriptor.setMetadataLang("eng");
			} else {
				bilingualTmxMetashareDescriptor.setMetadataLang("en");
			}
			bilingualTmxMetashareDescriptor.run();
		}else{
			logger.info("No proper TUs found.");
		}
	}


	/**
	 * gets the tus of a tmx if the 0:1 alignments are less than thr
	 * @param tmxFile
	 * @param thr
	 * @param minPerce01Align 
	 */
	public static List<SegPair>  getTUsFromTMX(File tmxFile, int thr, double minPerce01Align, String lang1, String lang2, boolean lic) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxFile));
			int alignments = 0;
			int sourceSegments = 0;
			int targetSegments = 0;
			List<Tu> tus = tmx.getBody().getTu();
			for (Tu tu: tus) {
				alignments = alignments+1;
				List<Object> tuProps = tu.getNoteOrProp();
				String type="";
				String site="";
				String license="";
				String other = "";
				double score = 0;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(SCORE)) {
						//meanAlignmentScore = meanAlignmentScore + Double.parseDouble(prop.getContent().get(0));
						score = Double.parseDouble(prop.getContent().get(0));
					} else if (prop.getType().equals(SEGMENTTYPE)) {
						type = prop.getContent().get(0);
						String[] segs = StringUtils.split(prop.getContent().get(0), ":");
						sourceSegments = sourceSegments + Integer.parseInt(segs[0]);
						targetSegments = targetSegments + Integer.parseInt(segs[1]);
					}else if (prop.getType().equals(eAddressNode)){
						if (prop.getContent().isEmpty())
							site  = "";
						else
							site  = prop.getContent().get(0);
					}else if (prop.getType().equals(licenseNode)){
						if (prop.getContent().isEmpty())
							license="";
						else
							license  = prop.getContent().get(0);
					}
				}
				segpairs.add(new SegPair(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, lang1), SPACE_SEPARATOR), 
						StringUtils.join(TMXHandlerUtils.createSegmentList(tu, lang2), SPACE_SEPARATOR),
						score, type, "", site, license, other));
			}
			logger.debug("Examining " + tmxFile.getAbsolutePath() + SPACE_SEPARATOR + tus.size());
		} catch (FileNotFoundException e) {
			logger.warn("Problem in reading "+ tmxFile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}



	private static List<ILSPAlignment> getSegs(List<SegPair> segpairs,	List<ILSPAlignment> alignmentList, 
			boolean keepem, boolean keepiden, boolean keepdup, boolean ksn, boolean cc) {

		float ratio;
		for (SegPair segpair:segpairs){
			String info="";
			if (cc && segpair.license.isEmpty())
				continue;
			//FIXME add constrains for length, or other "filters"
			String normS = ContentNormalizer.normtext(segpair.seg1);
			String normT = ContentNormalizer.normtext(segpair.seg2);
			if ( normS.isEmpty() || normT.isEmpty()){
				if (!keepem)
					continue;
				info =  mes1;
			}
			if (normS.equals(normT)){
				if (!keepiden)
					continue;
				if (info.isEmpty()){	info =  mes2;}		else{	info =  info + " | "+mes2;}	
			}

			List<String> stokens = FCStringUtils.getTokens(normS);
			List<String> ttokens = FCStringUtils.getTokens(normT);
			Double[] stokenslen= FCStringUtils.getTokensLength(stokens);
			Double[] ttokenslen= FCStringUtils.getTokensLength(ttokens);
			if (Statistics.getMax(stokenslen)>max_word_length || Statistics.getMax(ttokenslen)>max_word_length){
				continue;
			}else{
				if (Statistics.getMedian(stokenslen)>median_word_length || Statistics.getMedian(ttokenslen)>median_word_length){
					continue;
				}
			}
			if (FCStringUtils.countTokens(normS)<minTuvLen || FCStringUtils.countTokens(normT)<minTuvLen){
				if (info.isEmpty()){	info = mes4+minTuvLen ;}		else{	info = info + " | "+mes4+minTuvLen ;}
			}
			ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
			if (ratio>maxTuLenRatio || ratio < minTuLenRatio){
				if (info.isEmpty()){	info = mes5+  minTuLenRatio +mes5a+ maxTuLenRatio;}		else{	info = info + " | "+mes5+  minTuLenRatio +mes5a+ maxTuLenRatio ;}
			}
			String num1=segpair.seg1.replaceAll("\\D+","");
			String num2=segpair.seg2.replaceAll("\\D+","");
			if (!num1.equals(num2)){
				if (ksn)
					continue;
				if (info.isEmpty()){	info =  mes6;}		else{	info =  info + " | "+mes6;}	
			}
			String temp = normS+TAB+normT;
			if (segs.contains(temp)){
				if (info.isEmpty()){	info =  mes7;}		else{	info =  info + " | "+mes7;}
				if (!keepdup)
					continue;
			}else
				segs.add(temp);
			ILSPAlignment alignment = new ILSPAlignment();
			alignment.addSourceSegment(segpair.seg1);
			alignment.addTargetSegment(segpair.seg2);
			alignment.setScore((float)segpair.score);
			alignment.setSite(segpair.site);
			alignment.setMethod(segpair.method);
			alignment.setLicense(segpair.license);
			alignment.setType(segpair.type);
			alignment.setLengthRatio(Float.toString(ratio));
			alignment.setInfo(info);
			alignmentList.add(alignment);
		}
		return alignmentList;
	}

}
