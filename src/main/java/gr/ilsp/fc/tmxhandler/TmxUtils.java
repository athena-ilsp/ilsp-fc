package gr.ilsp.fc.tmxhandler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.corpora.BilingualTmxMetashareDescriptor;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsNumStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsRatioStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsScoreStats;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.nlp.commons.Constants;

public class TmxUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmxUtils.class);
	private static final String TOK_EXT = ".tok";
	private static final String TXT_EXT = ".txt";
	private static final String LIST = "list";
	private final static String SAMPLE = ".sample";
	private final static String SITES = ".sites";


	private final static String mes1 = "non-letters";
	private final static String mes2 = "equal TUVs";
	//private final static String mes3 = "very long token, longer than ";
	//private final static String mes3a = "very long tokens, median length is longer than ";
	private final static String mes4 = "very short segments, shorter than ";
	private final static String mes5 = "charlength ratio of TUVs is lower than ";
	private final static String mes5a = " or higher than ";
	private final static String mes6 = "different numbers in TUVs";
	private final static String mes7 = "duplicate";
	private final static String mes8 = "e-mail address";
	private final static String mes9 = "url";
	private final static double median_word_length=25;
	private final static double max_word_length=30;

	public static void main(String[] args) throws Exception{  //throws Exception
		
		File ej = new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/crawls/law_crawls_eng-el");
		
		File[] ejfs = ej.listFiles();
		for (File ejf:ejfs){
			if (ejf.isDirectory()){
				File[] d = ejf.listFiles();
				if (d.length==1){
					System.out.println(d[0].getAbsolutePath()+"\t"+ejf.getName());
				}
			}
		}
		System.exit(0);
		
		List<String> res1_el =   FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt1_ILSP-FC_ell-eng.el"));
		List<String> res1_en =   FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt1_ILSP-FC_ell-eng.en"));
		List<String> res04_el =  FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt04_ILSP-FC_ell-eng.el"));
		List<String> res04_en =  FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt04_ILSP-FC_ell-eng.en"));
		//List<String> res1_all =  FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt04_ILSP-FC_ell-eng.en"));
		
		
		for (int ii=0;ii<res1_el.size();ii++){
			if (res04_el.contains(res1_el.get(ii)) && res04_en.contains(res1_en.get(ii)) )
				continue;
			System.out.println(res1_el.get(ii)+"\t"+res1_en.get(ii));
		}
		
		System.exit(0);
		
		List<String> websites = null;
		try {
			websites = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-HR/total_run/output_NEWALIGN_ILSP-FC_eng-hrv.sites.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<SegPair> tus = TMXHandlerUtils.getTUsFromTMX(new File("C:/Users/vpapa/ELRC/EN-HR/total_run/output_NEWALIGN_ILSP-FC_eng-hrv.tmx"), new ArrayList<String>(), "en", "hr");
		
		
		for (String website:websites){
			int matches=0;
			for (SegPair tu:tus){
				String host = null;
				try {
					host = new URL(tu.l1url).getHost();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (host.equals(website))
					matches++;
			}
			System.out.println(website+"\t"+matches);
		}
		
		System.exit(0);
		
		
/*		
		File file11 = new File("C:/Users/vpapa/ELRC/created_datasets/datasets_v2/culture_v2_elrc_ell-eng.tmx");
		List<String> sites = ReadResources.extractListFromNodesOfXML(file11.getAbsolutePath(), "prop", true);
		List<String> aaa =new ArrayList<String>();
		for (String site:sites){
			String t;
			try {
				t = new URL(site).getHost();
			} catch (MalformedURLException e) {
				continue;
			}
			if (!aaa.contains(t)){
				System.out.println(t);
				aaa.add(t);
			}
		}
		System.exit(0);*/
		/*		File xlif = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/319/raport_uzp_2006.xlf");
		List<String> tttt = ReadResources.extractListFromNodesOfXML(xlif.getAbsolutePath(), "source",false);
		List<String> newtttt=new ArrayList<String>();
		for (String t:tttt){
			if (newtttt.contains(t))
				continue;
			newtttt.add(t);
		}
		System.out.println(newtttt.size());

		List<String> tttt1 = ReadResources.extractListFromNodesOfXML(xlif.getAbsolutePath(), "target",false);
		List<String> newtttt1=new ArrayList<String>();
		for (String t:tttt1){
			if (newtttt1.contains(t))
				continue;
			newtttt1.add(t);
		}
		System.out.println(newtttt.size());
		System.out.println(newtttt.size()+newtttt1.size());
		System.exit(0);*/


		/*List<String> lines = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/merged.txt"), "UTF-8");
		List<String> bg_lines = new ArrayList<String>();
		List<String> en_lines = new ArrayList<String>();

		for (String line:lines){
			String[] temp = line.split("\t");
			if (temp.length<2)
				continue;
			bg_lines.add(ContentNormalizer.normalizeText(temp[0]));
			en_lines.add(ContentNormalizer.normalizeText(temp[1]));
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/merged_bg.txt"), bg_lines);
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/merged_en.txt"), en_lines);
		System.exit(0);*/

		File inFile1 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_226/Convention against torture");
		//File inFile2 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/200/nws/setimes_lexacctrain.ro");
		//String sep="####";
		String[] langs = new String[2];
		langs[0] = "el";
		langs[1] = "en";
		String domain= "LAW";
		String baseName = "C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/final";

		String organization = "Ministry of Justice, Transparency and Human Rights";
		String creationModeDescription = "Convention against Torture and Other Cruel, Inhuman or Degrading Treatment or Punishment - United Nations (English-Greek)"
				+ "It was created/offered by the " + organization
				+ ". As post-processing processes: text extracted from the provided PDF files, two CesDoc files (one for each language) were generated, sentence alignment was applied and several filters were applied to discard/annotate alignments that might be incorrect";

		String lic = "CC-BY_4.0";
		double sampleSizePerCe = 0.1;
		String MetadataExt = ".md.xml";
		File baseNamef = new File(baseName);
		File outTMX = new File(baseNamef.getAbsolutePath()+".tmx");
		File outHTML = new File(baseNamef.getAbsolutePath()+".tmx.html");

		//List<SegPair> segpairslist = txts2tmx(inFile1, inFile2);

		//List<SegPair> segpairslist = txt2tmx(inFile1, sep);

		//List<SegPair> segpairslist = xlif2tmx(inFile1);

		//List<SegPair> segpairslist = tmx2tmx(inFile1);

		List<SegPair> segpairslist = tmxiel2tmx(inFile1);



		List<ILSPAlignment> alignmentList = aligns(segpairslist);
		BilingualCorpusInformation bilingualCorpusInfo= bilinfo( alignmentList,  langs,  domain,  outTMX,  creationModeDescription,organization,lic);
		if (bilingualCorpusInfo!=null){

			TMXHandler.generateMergedTMX(outTMX, langs, bilingualCorpusInfo, outHTML);

			LOGGER.info("Generating language files");
			TMXHandlerUtils.splitIntolangFiles(alignmentList, langs, baseNamef);

			int sampleSizeCe = (int)((double)alignmentList.size()*sampleSizePerCe);
			int sampleSize = Math.min(1500, sampleSizeCe);
			File samplefile = new File(baseName + SAMPLE);
			LOGGER.info("Generating sample file " + samplefile.getAbsolutePath());
			TMXHandlerUtils.generateSample(alignmentList, sampleSize, samplefile);

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName+ MetadataExt);
			LOGGER.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			bilingualTmxMetashareDescriptor.setMetadataLang("en");
			bilingualTmxMetashareDescriptor.run();	
		}else{
			LOGGER.info("No proper TUs found.");
		}

	}

	private static List<SegPair> tmxiel2tmx(File inFile1) {
		List<SegPair> segpairslist = new ArrayList<SegPair>();
		List<SegPair> newsegpairslist = new ArrayList<SegPair>();
		List<File> tmxs = new ArrayList<File>();
		String ext = "tmx";
		String[] extensions = { ext };
		if (inFile1.isDirectory())
			tmxs = (List<File>) FileUtils.listFiles(inFile1, extensions, false);
		else
			tmxs.add(inFile1);
		for (File tmx:tmxs)
			segpairslist.addAll(TMXHandlerUtils.getTUsFromTMX(tmx,100, 0.16 , 0.4, "bg", "en", false,null));

		for (SegPair sp:segpairslist){
			if (sp.seg1.isEmpty() || sp.seg2.isEmpty() || !sp.type.equals("1:1"))
				continue;
			newsegpairslist.add(sp);
		}
		return newsegpairslist;
	}

	private static List<SegPair> tmx2tmx(File inFile1) {
		List<File> tmxs = new ArrayList<File>();
		String ext = "tmx";
		String[] extensions = { ext };
		if (inFile1.isDirectory())
			tmxs = (List<File>) FileUtils.listFiles(inFile1, extensions, false);
		else
			tmxs.add(inFile1);
		List<String> ls1 = new ArrayList<String>();
		List<String> ls2 = new ArrayList<String>();
		for (File tmx:tmxs){
			List<String> ts = null;
			try {
				ts = FileUtils.readLines(tmx, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			boolean t1=false, t2=false; 
			for (String t:ts){
				if (t.startsWith("<seg>") && !t1 ){ //
					ls1.add(ContentNormalizer.normalizeText(t.substring(5, t.length()-6)) );
					t1=true;
					t2=false;
					continue;
				}
				if (t.startsWith("<seg>") && !t2){ //
					ls2.add(ContentNormalizer.normalizeText(t.substring(5, t.length()-6)) );
					t2=true;
					t1=false;
					continue;
				}
			}
		}
		List<SegPair> segpairslist = new ArrayList<SegPair>();
		for (int ii=0;ii<ls1.size();ii++){
			String seg1 = ls1.get(ii);
			String seg2 = ls2.get(ii);
			if (seg1.equals("∅") || seg2.equals("∅") || seg1.isEmpty() || seg2.isEmpty()) 
				continue;
			double ratio = (double)seg1.length() / (double)seg2.length();
			SegPair s = new SegPair(seg1, seg2, Double.parseDouble("0"),
					"", "","", "", "", "", ratio,"");
			segpairslist.add(s);
		}
		return segpairslist;
	}

	private static List<SegPair> xlif2tmx(File inFile1) {
		List<File> xlifs = new ArrayList<File>();
		String ext = "xlf";
		String[] extensions = { ext };
		if (inFile1.isDirectory())
			xlifs = (List<File>) FileUtils.listFiles(inFile1, extensions, false);
		else
			xlifs.add(inFile1);
		List<String> ls1 = new ArrayList<String>();
		List<String> ls2 = new ArrayList<String>();
		for (File xlif:xlifs){
			List<String> temp = new ArrayList<String>();
			temp = ReadResources.extractListFromNodesOfXML(xlif.getAbsolutePath(), "source",false);
			ls1.addAll(temp);
			temp = ReadResources.extractListFromNodesOfXML(xlif.getAbsolutePath(), "target",false);
			ls2.addAll(temp);
		}

		List<SegPair> segpairslist = new ArrayList<SegPair>();
		for (int ii=0;ii<ls1.size();ii++){
			String seg1 = ls1.get(ii);
			String seg2 = ls2.get(ii);
			if (seg1.equals("∅") || seg2.equals("∅") || seg1.isEmpty() || seg2.isEmpty()) 
				continue;
			double ratio = (double)seg1.length() / (double)seg2.length();
			SegPair s = new SegPair(seg1, seg2, Double.parseDouble("0"),
					"", "","", "", "", "", ratio,"");
			segpairslist.add(s);
		}
		return segpairslist;
	}

	private static List<SegPair> txts2tmx(File inFile1, File inFile2) throws Exception{
		List<String> ls1 = FileUtils.readLines(inFile1);
		List<String> ls2 = FileUtils.readLines(inFile2);
		List<SegPair> segpairslist = new ArrayList<SegPair>();
		for (int ii=0;ii<ls1.size();ii++){
			String seg1 = ContentNormalizer.normalizeText(ls1.get(ii));
			String seg2 = ContentNormalizer.normalizeText(ls2.get(ii));
			if (seg1.equals("∅") || seg2.equals("∅") || seg1.isEmpty() || seg2.isEmpty()) 
				continue;
			double ratio = (double)seg1.length() / (double)seg2.length();
			SegPair s = new SegPair(seg1, seg2, Double.parseDouble("0"),
					"", "","", "", "", "", ratio,"");
			segpairslist.add(s);
		}
		return segpairslist;
	}

	private static List<SegPair> txt2tmx(File infile, String sep) throws Exception{
		List<String> segpairs = FileUtils.readLines(infile);
		List<SegPair> segpairslist = new ArrayList<SegPair>();
		for (String segpair:segpairs){
			String[] temp = segpair.split(sep);
			String seg1 = ContentNormalizer.normalizeText(temp[0]);
			String seg2 = ContentNormalizer.normalizeText(temp[1]);
			if (seg1.equals("∅") || seg2.equals("∅") || seg1.isEmpty() || seg2.isEmpty()) 
				continue;
			double ratio = (double)seg1.length() / (double)seg2.length();
			SegPair s = new SegPair(seg1, seg2, Double.parseDouble(temp[2].trim()),
					"", "","", "", "", "", ratio,"");
			segpairslist.add(s);
		}
		return segpairslist;
	}


	public static void main1(String[] args) throws IOException {

		LOGGER.info("\n1st argument: the targeted TMX file\n"
				+ "2nd argument: the \"source\" language\n"
				+ "3rd argument: the \"target\" language\n"
				+ "4th argument: the annotation type to exclude (i.e. some of \"short;ratio;equal;duplicate;address;numbers;letters\" or \"\" for getting all)\n"
				+ "5th argument: the absolute sample size\n");
		File tmxFile = new File(args[0]).getAbsoluteFile();
		LOGGER.info("Reading " + tmxFile.getAbsolutePath());
		String lang1=args[1];
		String lang2=args[2];
		LOGGER.info("Languages: " + lang1 + Constants.SPACE + lang2 + Constants.SPACE);
		List<String> info = new ArrayList<String>(); 	//List<String> info = Arrays.asList(new String[] {"short", "ratio", "equal", "duplicate", "address", "numbers", "letters" });
		if (!args[3].equals("\"\"")){
			String[] temp= args[3].split(";");
			info =Arrays.asList(temp);
		}
		int samplesize = Integer.parseInt(args[4]);

		//---------------list of Segment Pairs
		List<SegPair> segs = TMXHandlerUtils.getTUsFromTMX(tmxFile, info, lang1, lang2);
		List<String> segsl1 = new ArrayList<String>();
		List<String> segsl2 = new ArrayList<String>();
		for (SegPair seg:segs){
			segsl1.add(seg.seg1);
			segsl2.add(seg.seg2);
		}

		//---------------tokenized segments in 2 two text files (1 for each language)
		List<List<String>> tokedsegs = TMXHandlerUtils.getTokedSegs(segsl1, segsl2, lang1,lang2);
		File l1file= new File(tmxFile.getAbsolutePath()+TOK_EXT+lang1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for tokenized segments in "+lang1);
		FileUtils.writeLines(l1file, tokedsegs.get(0));
		File l2file= new File(tmxFile.getAbsolutePath()+TOK_EXT+lang2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for tokenized segments in "+lang2);
		FileUtils.writeLines(l2file, tokedsegs.get(1));

		//--------------segments in 2 two text files (1 for each language)
		l1file= new File(tmxFile.getAbsolutePath()+Constants.DOT+lang1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for segments in "+lang1);
		FileUtils.writeLines(l1file, segsl1);
		l2file= new File(tmxFile.getAbsolutePath()+Constants.DOT+lang2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for segments in "+lang2);
		FileUtils.writeLines(l2file, segsl2);

		//----------segments and props in a text file (tab separated)
		File tsvFile = new File(tmxFile.getAbsolutePath()+Constants.DOT+LIST);
		List<String> outLines = TMXHandlerUtils.getSegsAndProps(segs, lang1, lang2);
		FileUtils.writeLines(tsvFile, outLines);

		//------------Sample---------------------------
		List<String> sampleSegs = TMXHandlerUtils.getSampleSegs(segs, samplesize);
		File sampleFile = new File(tmxFile.getAbsolutePath()+SAMPLE+samplesize+TXT_EXT);
		FileUtils.writeLines(sampleFile, sampleSegs);

		//---------------- Sites
		List<String> sites = TMXHandlerUtils.getSitesFromSegPairs(segs, false);
		File sitesFile = new File(tmxFile.getAbsolutePath()+SITES+samplesize+TXT_EXT);
		FileUtils.writeLines(sitesFile, sites);
	}


	private static List<ILSPAlignment> aligns(List<SegPair> segpairslist){
		int minTuvLen=2;
		double minTuLenRatio = 0.6;
		double maxTuLenRatio = 1.6;
		Set<String> segs = new HashSet<String>() ;
		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		for (SegPair segpair:segpairslist){
			if (segpair.l1url.contains("twitter.") || segpair.l2url.contains("twitter.")) //temp addition. it should be removed 
				continue;

			String info="";//, info1="";
			//FIXME add constrains for length, or other "filters"
			String normS = ContentNormalizer.normtext(segpair.seg1);
			String normT = ContentNormalizer.normtext(segpair.seg2);
			if ( normS.isEmpty() || normT.isEmpty()){
				//if (clean)
				//	continue;
				//if (!keepem)
				//	continue;
				info =  mes1;
			}
			if (normS.equals(normT)){
				//if (clean)
				//	continue;
				//if (!keepiden)
				//	continue;
				if (info.isEmpty()){	info =  mes2;}		else{	info =  info + " | "+mes2;}	
			}
			if (TMXHandlerUtils.checkemail(segpair.seg1) || TMXHandlerUtils.checkemail(segpair.seg2)){
				//if (clean)
				//	continue;
				//if (!keepem)
				//	continue;
				if (info.isEmpty()){	info =  mes8;}		else{	info =  info + " | "+mes8;}	
			}
			if (TMXHandlerUtils.checkurl(segpair.seg1) || TMXHandlerUtils.checkurl(segpair.seg2)){
				//if (clean)
				//	continue;
				//if (!keepem)
				//	continue;
				if (info.isEmpty()){	info =  mes9;}		else{	info =  info + " | "+mes9;}	
			}

			/*List<String> stokens = FCStringUtils.getTokens(segpair.seg1);
			List<String> ttokens = FCStringUtils.getTokens(segpair.seg2);
			Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
			Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);

			if (Statistics.getMax(stokenslen)>max_word_length || Statistics.getMax(ttokenslen)>max_word_length){
				LOGGER.info("discarded TU, very large word (due to bad text extraction from pdf):"+ segpair.seg1 +"\t"+ segpair.seg2);
				//String info1 = "a: "+ max_word_length;
				continue;
			}else{
				if (Statistics.getMedian(stokenslen)>=median_word_length || Statistics.getMedian(ttokenslen)>=median_word_length){
					LOGGER.info("discarded TU, very large words (due to bad text extraction from pdf):"+ segpair.seg1 +"\t"+ segpair.seg2);
					//	String info1 = "a: "+ median_word_length;
					continue;
				}
			}*/
			//if (!info1.isEmpty()){
			//	if (info.isEmpty()){	info = info1;}	else{	info = info + " | "+info1;}
			//}	
			if (FCStringUtils.countTokens(normS)<minTuvLen || FCStringUtils.countTokens(normT)<minTuvLen){
				//if (clean)
				//	continue;
				if (info.isEmpty()){	info = mes4+minTuvLen ;}		else{	info = info + " | "+mes4+minTuvLen ;}
			}
			float ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
			if (ratio>maxTuLenRatio || ratio < minTuLenRatio){
				//if (clean)
				//	continue;
				if (info.isEmpty()){	info = mes5+  minTuLenRatio +mes5a+ maxTuLenRatio;}	
				else{	info = info + " | "+mes5+  minTuLenRatio +mes5a+ maxTuLenRatio ;}
			}
			String num1=segpair.seg1.replaceAll("\\D+","");
			String num2=segpair.seg2.replaceAll("\\D+","");
			if (!num1.equals(num2)){
				//if (clean)
				//	continue;
				//if (ksn)
				//	continue;
				if (info.isEmpty()){	info =  mes6;}		else{	info =  info + " | "+mes6;}	
			}
			String temp = normS+Constants.TAB+normT;
			if (segs.contains(temp)){
				//if (clean)
				//	continue;
				//if (!keepdup)
				//	continue;
				if (info.isEmpty()){	info =  mes7;}		else{	info =  info + " | "+mes7;}

			}else
				segs.add(temp);

			ILSPAlignment alignment = new ILSPAlignment();
			alignment.addSourceSegment(segpair.seg1);
			alignment.addTargetSegment(segpair.seg2);
			alignment.setScore((float)segpair.score);
			alignment.setL1url(segpair.l1url);
			alignment.setL2url(segpair.l2url);

			//alignment.setSite(segpair.site);
			alignment.setMethod(segpair.method);
			alignment.setLicense(segpair.license);
			alignment.setType(segpair.type);
			alignment.setLengthRatio(Float.toString(ratio));
			alignment.setInfo(info);
			alignmentList.add(alignment);
		}
		return alignmentList;
	}


	private static BilingualCorpusInformation  bilinfo(List<ILSPAlignment> alignmentList, String[] langs, String domain, File outTMX, String creationModeDescription, String organization, String lic){
		if (!alignmentList.isEmpty()){
			TUsScoreStats scores = TMXHandlerUtils.scorestats(alignmentList, true);
			TUsRatioStats ratios = TMXHandlerUtils.ratiostats(alignmentList, true);
			TUsNumStats stats1 = TMXHandlerUtils.numstats(alignmentList, 1, true);
			TUsNumStats stats2 = TMXHandlerUtils.numstats(alignmentList, 2, true);
			List<String> sites_all = TMXHandlerUtils.getSites(alignmentList, false);
			List<String> sites_noannot = TMXHandlerUtils.getSites(alignmentList, true);
			creationModeDescription = creationModeDescription + 
					".\nThe mean value of aligner's scores is "+ scores.meanscore_all+ ", the std value is "+ scores.stdscore_all +
					". The mean value of length (in terms of characters) ratios is "+ ratios.meanratio_all+ " and the std value is "+ ratios.stdratio_all + "." +
					"\nThere are "+ stats1.tus_noan +" TUs with no annotation,"+
					" containing "+ stats1.tokens_noan +" words and "+ stats1.words_noan +" lexical types in "+ langs[0] + 
					" and "+ stats2.tokens_noan +" words and "+ stats2.words_noan+" lexical types in "+ langs[1] +
					". The mean value of aligner's scores is "+ scores.meanscore_noan+ ", the std value is "+ scores.stdscore_noan +
					". The mean value of length (in terms of characters) ratios is "+ ratios.meanratio_noan + " and the std value is "+ ratios.stdratio_noan + "." ;

			String creationDescription = "Parallel ("+ langs[0] + Constants.HYPHEN + langs[1] +") corpus of "+
					alignmentList.size()+ " (" +stats1.tus_noan +" not-annotated) translation units";
			if (!StringUtils.isEmpty(domain))
				creationDescription = creationDescription + " in the "+	domain+" domain";
			BilingualCorpusInformation bilingualCorpusInfo = new BilingualCorpusInformation();

			bilingualCorpusInfo.setName(FilenameUtils.getBaseName(outTMX.getAbsolutePath()));
			bilingualCorpusInfo.setL1(langs[0]);
			bilingualCorpusInfo.setL2(langs[1]);
			bilingualCorpusInfo.setAlignmentList(alignmentList);

			bilingualCorpusInfo.setAlignmentsSize(alignmentList.size());
			bilingualCorpusInfo.setLenInWordsL1(stats1.tokens_all);
			bilingualCorpusInfo.setLenInWordsL2(stats2.tokens_all);
			bilingualCorpusInfo.setVocSizeInL1(stats1.words_all);
			bilingualCorpusInfo.setVocSizeInL2(stats2.words_all);

			bilingualCorpusInfo.setCleanLenInTUs(stats1.tus_noan);
			bilingualCorpusInfo.setCleanVocSizeInL1(stats1.words_noan);
			bilingualCorpusInfo.setCleanVocSizeInL2(stats2.words_noan);
			bilingualCorpusInfo.setCleanLenInWordsL1(stats1.words_noan);
			bilingualCorpusInfo.setCleanLenInWordsL2(stats2.words_noan);

			bilingualCorpusInfo.setMeanAlignScore(scores.meanscore_all);
			bilingualCorpusInfo.setMeanAlignScoreNo(scores.meanscore_noan);
			bilingualCorpusInfo.setStdAlignScore(scores.stdscore_all);
			bilingualCorpusInfo.setStdAlignScoreNo(scores.stdscore_noan);

			bilingualCorpusInfo.setMeanRatioScore(ratios.meanratio_all);
			bilingualCorpusInfo.setMeanRatioScoreNo(ratios.meanratio_noan);
			bilingualCorpusInfo.setStdRatioScore(ratios.stdratio_all);
			bilingualCorpusInfo.setStdRatioScoreNo(ratios.stdratio_noan);

			List<String> domains = Arrays.asList(domain.split(";"));
			List<String> domainEurovocIds=TMXHandler.getEurovocId(domains);
			//FIXME shall we integrate JRX Eurovoc Indexer?
			domain = StringUtils.join(domains, ',');
			String domainEurovocId = StringUtils.join(domainEurovocIds, ',');
			bilingualCorpusInfo.setDomain(domain);
			bilingualCorpusInfo.setDomainId(domainEurovocId);

			bilingualCorpusInfo.setCreationDescription(creationModeDescription);
			bilingualCorpusInfo.setDescription(creationDescription);
			bilingualCorpusInfo.setProjectId("ELRC");
			bilingualCorpusInfo.setProjectURL("http://lr-coordination.eu/");
			bilingualCorpusInfo.setOrganization( organization);
			bilingualCorpusInfo.setOrganizationURL("");
			bilingualCorpusInfo.setAvailability(lic);
			return bilingualCorpusInfo;
		}else
			return null;
	}


	private static void evalDonatedSet(List<String> el, List<String> en,
			List<String> ent,List<String> eval) {
		double crawlcorrect=0, crawlwins=0, mtcorrect=0, mtwins=0, nowinswrong=0, nowinscorrect=0, emptycounter=0, onlycrawlwins=0, onlymtwins=0;
		boolean ok=false;
		eval.remove(0);
		if (el.size()==en.size() && el.size()==ent.size() && el.size()==eval.size())
			ok=true;
		if (!ok){
			System.out.println("list have not equal sizes");
			System.exit(0);
		}
		for (int ii=0;ii<eval.size();ii++){
			String[] temp = eval.get(ii).split("\t");
			if (temp.length==0){
				emptycounter++;
				continue;
			}
			if (!(temp[2].contains("0") || temp[2].contains("1") || temp[2].contains("2")))
				break;
			if (!(temp[3].contains("0") || temp[3].contains("1") || temp[3].contains("2")))
				break;
			if (!(temp[4].contains("0") || temp[4].contains("1") || temp[4].contains("2")))
				break;

			if (el.contains(temp[0])){
				if ((temp[4].equals("1") && temp[2].equals("0") && temp[3].equals("1"))
						|| (temp[4].equals("2") && temp[2].equals("1") && temp[3].equals("0"))
						|| (!temp[4].equals("0") && temp[2].equals("0") && temp[3].equals("0"))	)
					continue;
				if (temp[4].equals("0") && temp[2].equals("0") && temp[3].equals("0")){
					nowinswrong++;
					//continue;
				}
				if (temp[4].equals("0") && temp[2].equals("1") && temp[3].equals("1")){
					nowinscorrect++;
					crawlcorrect++;
					mtcorrect++;
					continue;
				}
				if (en.contains(temp[1]) && ent.contains(temp[5])){
					if (temp[2].equals("0"))
						System.out.println(temp[0]+"\t"+temp[1]);
					if (temp[2].equals("1"))
						crawlcorrect++;
					if (temp[3].equals("1"))
						mtcorrect++;
					if (temp[4].equals("1") && temp[2].equals("1") && temp[3].equals("0")){
						onlycrawlwins++;
						continue;
					}
					if (temp[4].equals("1") && temp[2].equals("1") && temp[3].equals("1")){
						crawlwins++;
						continue;
					}
					if (temp[4].equals("2") && temp[2].equals("1") && temp[3].equals("1")){
						mtwins++;
						continue;
					}
					if (temp[4].equals("2") && temp[2].equals("0") && temp[3].equals("1")){
						onlymtwins++;
						continue;
					}
					continue;
				}
				if (ent.contains(temp[1]) && en.contains(temp[5])){
					if (temp[3].equals("0"))
						System.out.println(temp[0]+"\t"+temp[5]);
					if (temp[3].equals("1"))
						crawlcorrect++;
					if (temp[2].equals("1"))
						mtcorrect++;
					if (temp[4].equals("2")  && temp[2].equals("1") && temp[3].equals("1")){
						crawlwins++;
						continue;
					}
					if (temp[4].equals("2")  && temp[2].equals("0") && temp[3].equals("1")){
						onlycrawlwins++;
						continue;
					}
					if (temp[4].equals("1")  && temp[2].equals("1") && temp[3].equals("1")){
						mtwins++;
						continue;
					}
					if (temp[4].equals("1")  && temp[2].equals("1") && temp[3].equals("0")){
						onlymtwins++;
						continue;
					}

					continue;
				}
				System.out.println(temp[1]);
				System.out.println(temp[5]);
				System.out.println(temp[0]);
				System.out.println("eeeeep");
			}else{
				System.out.println("oops");
			}
		}
		double total = eval.size()-emptycounter;
		System.out.println("total pairs="+total);

		System.out.println("both wrong="+nowinswrong);
		System.out.println("both correct and equivalent="+nowinscorrect);
		System.out.println("only crawler's correct="+ onlycrawlwins);
		System.out.println("only mt's correct="+ onlymtwins);
		System.out.println("both correct but crawler's better="+ crawlwins);
		System.out.println("both correct but mt's better="+ mtwins);

		System.out.println("crawlcorrect="+crawlcorrect+"\nmtcorrect="+mtcorrect);
		System.out.println("crawlprecision="+(crawlcorrect/total));
		System.out.println("mtprecision="+(mtcorrect/total));
		System.out.println();
	}


	public class StrinArrayComparator implements Comparator<String[]> {
		@Override
		public int compare(final String[] first, final String[] second){
			// here you should usually check that first and second
			// a) are not null and b) have at least two items
			// updated after comments: comparing Double, not Strings
			// makes more sense, thanks Bart Kiers
			return Double.valueOf(second[1]).compareTo(
					Double.valueOf(first[1])
					);
		}
	};

	/**
	 * parses a tmxfile, get segments in l1 and l2, and writes these segments in two text files (1 for each language)
	 * @param tmxFile
	 * @param l1
	 * @param l2
	 * @throws IOException
	 *//*
	private static void tmx2TXTs(File tmxFile, String l1, String l2) throws IOException {
		List<Tu> tus = TMXHandlerUtils.getTUs(tmxFile);
		List<String> l1segs = new ArrayList<String>();
		List<String> l2segs = new ArrayList<String>();
		for (Tu tu : tus) {
			l1segs.add(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l1), SPACE_STR));
			l2segs.add(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l2), SPACE_STR));
		}
		File l1file= new File(tmxFile.getAbsolutePath()+PUNCT_STR+l1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for segments in "+l1);
		FileUtils.writeLines(l1file, l1segs);
		File l2file= new File(tmxFile.getAbsolutePath()+PUNCT_STR+l2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for segments in "+l2);
		FileUtils.writeLines(l2file, l2segs);
	}*/


	/*	public static void tmx2Tsv(File tmxFile, File evalFile, String l1, String l2) throws IOException  {
		List<Tu> tus = TMXHandlerUtils.getTUs(tmxFile);
		List<String> outLines = new ArrayList<String>();
		int i = 1;
		for (Tu tu : tus) {
			String l1Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l1), SPACE_STR);
			String l2Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l2), SPACE_STR);
			List<Object> tuProps = tu.getNoteOrProp();
			String type="NULL";
			String score = "NULL";
			String lengthratio = "NULL";
			String info = "NULL";
			for (Object obProp : tuProps) {
				Prop prop = (Prop) obProp;
				if (prop.getType().equals(SCORE)) {
					score = prop.getContent().get(0);
				} else if (prop.getType().equals(SEGMENTTYPE)) {
					type = prop.getContent().get(0);
				} else if (prop.getType().equals(LENGTHRATIO)) {
					lengthratio = prop.getContent().get(0);
				} else if (prop.getType().equals(INFO) && (!prop.getContent().isEmpty())) {
					info = prop.getContent().get(0);
				}
			}
			outLines.add(StringUtils.join(new String[] {String.valueOf(i), l1Text, l2Text, String.valueOf(score), lengthratio, info }, TAB_STR));
			i++;
		}
		LOGGER.info("Writing file "+ evalFile.getAbsolutePath() + " with all results and information.");
		FileUtils.write (evalFile, StringUtils.join(new String[] {"id", l1, l2, "alignerScore", "lengthRatio", "info" }, TAB_STR)+NEWLINE_STR, false);
		FileUtils.writeLines(evalFile, outLines, true);
	}*/


	/*	*//**
	 * parses a tmxfile, get segments in l1 and l2, and writes these segments in two text files (1 for each language)
	 * @param tmxfile
	 * @param l1
	 * @param l2
	 *//*

	public static void tmx2txts(File tmxfile, String l1, String l2){
		File l1File = new File(FilenameUtils.concat(tmxfile.getParent(),l1+textext));
		File l2File = new File(FilenameUtils.concat(tmxfile.getParent(),l2+textext));
		List<String> sourcesegs = new ArrayList<String>();
		List<String> l1segs =  new ArrayList<String>();
		List<String> targetsegs = new ArrayList<String>();
		List<String> l2segs =  new ArrayList<String>();

		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(tmxfile.getAbsolutePath()), UTF_8);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("problem in reading due to encoding issue");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			LOGGER.error(tmxfile.getAbsolutePath()+" file not found");
			e.printStackTrace();
		}
		BilingualScoredTmxParser parser = new BilingualScoredTmxParser(reader, l1,l2);
		List<Alignment> listalign= parser.parse();
		LOGGER.info("Alignments : " + listalign.size());

		int coun=0, ind=-1;
		for (Alignment alignment:listalign){
			ind=ind+1;
			if ((ind/1000)>coun){
				LOGGER.info(Integer.toString(coun));
				coun++;
			}
			sourcesegs = alignment.getSourceSegmentList();
			if (sourcesegs.size()>0)
				l1segs.add(sourcesegs.get(0));
			targetsegs = alignment.getTargetSegmentList();
			if (targetsegs.size()>0)
				l1segs.add(targetsegs.get(0));
		}
		try {
			FileUtils.writeLines(l1File, l1segs);
			FileUtils.writeLines(l2File, l2segs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/


}
