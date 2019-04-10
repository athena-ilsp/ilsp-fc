package gr.ilsp.fc.DGThandler;

import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.ValidateUtils;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DGTHandler {
	private static DGTHandlerOptions options = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(DGTHandler.class);
	private static final String TAB_SEPARATOR = "\t";
	private static final String MAT = ".mat";
	private static final String PUNCT = ".";
	//private static final String TOK = "tok";
	private static final String HYPHEN = "-";
	private static final String ILSP = ".ilsp";
	private static final String FILTERED = ".filtered";
	
	private static File infile;
	private static String l1 , l2;
	private static double minTULenRatio, maxTULenRatio;	

	private static double minTULenRatioD=0.25, maxTULenRatioD=4;

	private static double minTuvLen, max_word_length, median_word_length;

	private static Set<String> segs = new HashSet<String>() ;

	public void tuStats(File tmxFile) throws IOException  {
		LOGGER.info("Default Filtering of TUs from " + tmxFile.getAbsolutePath() + " :");
		List<String> tus = new ArrayList<String>();

		LOGGER.info("seg pairs with length ratio >" + maxTULenRatioD + TAB_SEPARATOR);
		tus.add("seg pairs with length ratio >" + maxTULenRatioD + TAB_SEPARATOR);

		LOGGER.info("seg pairs with length ratio <" + minTULenRatioD + TAB_SEPARATOR);
		tus.add("seg pairs with length ratio <" + minTULenRatioD + TAB_SEPARATOR);

		LOGGER.info("Reading " + tmxFile.getAbsolutePath()+" file ...... ");
		File ratiosfile = new File(tmxFile.getAbsolutePath()+MAT); 
		File tusfile = new File(tmxFile.getAbsolutePath()+FILTERED);
		BufferedReader br=null;
		String line = "";
		boolean tu=false, tu1 = false;
		String seg1="", seg2="";
		int counter=0, counter1=0;
		List<Double> ratios = new ArrayList<Double>();
		List<String> l1tuvs = new ArrayList<String>();
		File li1tuvsfile = new File(tmxFile.getAbsolutePath()+PUNCT+l1+HYPHEN+l2+PUNCT+l1);
		List<String> l2tuvs = new ArrayList<String>();
		File li2tuvsfile = new File(tmxFile.getAbsolutePath()+PUNCT+l1+HYPHEN+l2+PUNCT+l2);
		try{
			br=new BufferedReader(new InputStreamReader(new FileInputStream(tmxFile),"UTF-16LE"));
			while((line = br.readLine())!=null){
				if (line.startsWith("<tuv xml:lang=") && !tu1){
					tu=true;
					continue;
				}
				if (tu && !tu1 && line.startsWith("<seg>")){
					tu1=true;
					seg1 = line.substring(5, line.length()-6);
					continue;
				}
				if (tu && tu1 && line.startsWith("<seg>")){
					seg2 = line.substring(5, line.length()-6);
					tu=false; tu1=false;
					double lenratio =((double) seg1.length() /  (double) seg2.length());
					counter++;
					if (lenratio >maxTULenRatioD || lenratio<minTULenRatioD){  
						//LOGGER.info(seg1+TAB_SEPARATOR+"------------------"+TAB_SEPARATOR+seg2);
						tus.add(seg1+TAB_SEPARATOR+seg2);
						counter1++;
					}else{
						l1tuvs.add(seg1);
						l2tuvs.add(seg2);
						ratios.add(lenratio);
					}
					seg1="";
					seg2="";
				}
			}
			br.close(); //CLOSE THE BufferedReader
		}catch(Exception e){
			System.out.println(e);
		}
		double per= ((double)counter1 / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+counter1 +TAB_SEPARATOR+counter+TAB_SEPARATOR+per );
		Double[] ra = new Double[ratios.size()];
		for (int ii=0;ii<ratios.size();ii++)
			ra[ii] = ratios.get(ii);
		LOGGER.info(TAB_SEPARATOR+"MEAN value:"+TAB_SEPARATOR+ Statistics.getMean(ra)+TAB_SEPARATOR);
		LOGGER.info(TAB_SEPARATOR+"STD value:"+TAB_SEPARATOR+ Statistics.getStdDev(ra)+TAB_SEPARATOR);
		FileUtils.writeLines(tusfile, "UTF-8", tus, "\n");
		FileUtils.writeLines(ratiosfile, "UTF-8",ratios, "\n");
		FileUtils.writeLines(li1tuvsfile,"UTF-8", l1tuvs, "\n");
		FileUtils.writeLines(li2tuvsfile, "UTF-8",l2tuvs,"\n");
	}

	private void tuFilteredStats(File tmxFile) throws IOException  {
		LOGGER.info("ILSP Filtering of TUs from " + tmxFile.getAbsolutePath() + " :");
		List<String> tus = new ArrayList<String>();
		List<String> l1tuvs = new ArrayList<String>();
		File li1tuvsfile = new File(tmxFile.getAbsolutePath()+ILSP+PUNCT+l1+HYPHEN+l2+PUNCT+l1);
		List<String> l2tuvs = new ArrayList<String>();
		File li2tuvsfile = new File(tmxFile.getAbsolutePath()+ILSP+PUNCT+l1+HYPHEN+l2+PUNCT+l2);
		//List<String> l1toktuvs = new ArrayList<String>();
		//File li1toktuvsfile = new File(tmxFile.getAbsolutePath()+ILSP+PUNCT+l1+HYPHEN+l2+PUNCT+TOK+PUNCT+l1);
		//List<String> l2toktuvs = new ArrayList<String>();
		//File li2toktuvsfile = new File(tmxFile.getAbsolutePath()+ILSP+PUNCT+l1+HYPHEN+l2+PUNCT+TOK+PUNCT+l2);
		
		
		LOGGER.info("seg pairs containing  a TUV with no letters are excluded");
		tus.add("seg pairs containing  a TUV with no letters are excluded");

		LOGGER.info("seg pairs with equal TUVs are excluded");
		tus.add("seg pairs with equal TUVs are excluded");

		LOGGER.info("seg pairs with a TUV containing only an e-mail are excluded");
		tus.add("seg pairs with a TUV containing only an e-mail are excluded");

		LOGGER.info("seg pairs with different numbers in TUVs are excluded");
		tus.add("seg pairs with different numbers in TUVs are excluded");

		LOGGER.info("seg pairs with length ratio >" + maxTULenRatio + " are excluded");
		tus.add("seg pairs with length ratio >" + maxTULenRatio + " are excluded");

		LOGGER.info("seg pairs with length ratio <" + minTULenRatio + " are excluded");
		tus.add("seg pairs with length ratio <" + minTULenRatio + " are excluded");

		LOGGER.info("seg pairs with a token longer than " + max_word_length + "characters are excluded");
		tus.add("seg pairs with a token longer than " + max_word_length + "characters are excluded");

		LOGGER.info("seg pairs containing  a TUV with median length longer than " + median_word_length + "characters are excluded");
		tus.add("seg pairs containing  a TUV with median length longer than " + median_word_length + "characters are excluded");

		LOGGER.info("Reading " + tmxFile.getAbsolutePath()+" file ...... ");
		File ratiosfile = new File(tmxFile.getAbsolutePath()+ILSP+MAT); 
		File tusfile = new File(tmxFile.getAbsolutePath()+ILSP+FILTERED);
		BufferedReader br=null;
		String line = "";
		boolean tu=false, tu1 = false;
		String seg1="", seg2="";
		int counter=0, counter1=0, counterempty=0, counterduple=0, counterequal=0, counteremail=0, counterlr=0, counterdifnum=0, countershort=0, counterlongw=0;

		List<Double> ratios = new ArrayList<Double>();
		try{
			br=new BufferedReader(new InputStreamReader(new FileInputStream(tmxFile),"UTF-16LE"));
			while((line = br.readLine())!=null){
				if (line.startsWith("<tuv xml:lang=") && !tu1){
					tu=true;
					continue;
				}
				if (tu && !tu1 && line.startsWith("<seg>")){
					tu1=true;
					seg1 = line.substring(5, line.length()-6);
					continue;
				}
				if (tu && tu1 && line.startsWith("<seg>")){
					seg2 = line.substring(5, line.length()-6);
					tu=false; tu1=false;
					counter++;
					String normS = ContentNormalizer.normtext(seg1);
					String normT = ContentNormalizer.normtext(seg2);
					if ( normS.isEmpty() || normT.isEmpty()){
						//LOGGER.info("empty TUV after normalization:"+ seg1 +TAB_SEPARATOR+ seg2);
						tus.add("Empty: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counterempty++;
						continue;
					}
					if (normS.equals(normT)){
						//LOGGER.info("equal TUVs after normalization:"+ seg1 + TAB_SEPARATOR + seg2);
						tus.add("EqualTUVs: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counterequal++;
						continue;	
					}
					if (ValidateUtils.isValidEmailAddress(seg1) || ValidateUtils.isValidEmailAddress(seg2)){
						//LOGGER.info("e-mail address in a TUV:"+ seg1 + TAB_SEPARATOR + seg2);
						tus.add("Emails: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counteremail++;
						continue;	
					}
					double lenratio =((double) seg1.length() /  (double) seg2.length());
					if (lenratio>maxTULenRatio || lenratio < minTULenRatio){
						//LOGGER.info("very high/low ratio of lengths of TUVs:"+ seg1 +TAB_SEPARATOR+ seg2);
						tus.add("lenRatio: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counterlr++;
						continue;
					}
					String num1=seg1.replaceAll("\\D+","");
					String num2=seg2.replaceAll("\\D+","");
					if (!num1.equals(num2)){
						//LOGGER.info("different numbers in TUVs:"+ seg1 +TAB_SEPARATOR+ seg2);
						tus.add("DifNum: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counterdifnum++;
						continue;	
					}
					if (FCStringUtils.countTokens(normS)<minTuvLen || FCStringUtils.countTokens(normT)<minTuvLen){
						//LOGGER.info("discarded TU, very short TUV:"+ seg1 +TAB_SEPARATOR+ seg2);
						tus.add("Short: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						countershort++;
						continue;
					}
					List<String> stokens = FCStringUtils.getTokens(normS);
					List<String> ttokens = FCStringUtils.getTokens(normT);
					Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
					Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);
					if (Statistics.getMax(stokenslen)>max_word_length || Statistics.getMax(ttokenslen)>max_word_length){
						//LOGGER.info("discarded TU, very large word (due to bad text extraction from pdf):"+ seg1 +TAB_SEPARATOR+ seg2);
						tus.add("LongWords: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counterlongw++;
						continue;
					}else{
						if (Statistics.getMedian(stokenslen)>=median_word_length || Statistics.getMedian(ttokenslen)>=median_word_length){
							//LOGGER.info("discarded TU, very large words (due to bad text extraction from pdf):"+ seg1 +TAB_SEPARATOR+ seg2);
							tus.add("LongWords: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
							counter1++;
							counterlongw++;
							continue;
						}
					}
					String temp = normS+TAB_SEPARATOR+normT;
					if (segs.contains(temp)){
						//LOGGER.info("duplicate TU after normalization:"+ seg1 +TAB_SEPARATOR+ seg2);
						tus.add("Dups: "+TAB_SEPARATOR+seg1+TAB_SEPARATOR+seg2);
						counter1++;
						counterduple++;
						continue;
					}else{
						segs.add(temp);
					}
					l1tuvs.add(seg1);
					l2tuvs.add(seg2);
					//l1toktuvs.add(FCStringUtils.getTokensStr(seg1).trim());
					//l2toktuvs.add(FCStringUtils.getTokensStr(seg2).trim());
					
					ratios.add(lenratio);
					seg1="";
					seg2="";
				}
			}
			br.close(); //CLOSE THE BufferedReader
		}catch(Exception e){
			System.out.println(e);
		}
		double per= ((double)counter1 / (double) counter)*100;	LOGGER.info(TAB_SEPARATOR+"TOTAL:"+TAB_SEPARATOR+per+TAB_SEPARATOR+ counter1 +TAB_SEPARATOR+counter+TAB_SEPARATOR);
		per= ((double)counterduple / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"DUPS:"+TAB_SEPARATOR +per+TAB_SEPARATOR+counterduple +TAB_SEPARATOR+counter+TAB_SEPARATOR);
		per= ((double)counterempty / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"EMPTY:"+TAB_SEPARATOR+per+TAB_SEPARATOR+counterempty +TAB_SEPARATOR+counter+TAB_SEPARATOR);		
		per= ((double)counterequal / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"EQUALTUVs:"+TAB_SEPARATOR+per+TAB_SEPARATOR+counterequal +TAB_SEPARATOR+counter+TAB_SEPARATOR);	
		per= ((double)countershort / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"ShortTUV:"+TAB_SEPARATOR+per+TAB_SEPARATOR+countershort +TAB_SEPARATOR+counter+TAB_SEPARATOR);
		per= ((double)counterlongw / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"LongWTUVs:"+TAB_SEPARATOR+per+TAB_SEPARATOR+counterlongw +TAB_SEPARATOR+counter+TAB_SEPARATOR);
		per= ((double)counteremail / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"Emails:"+TAB_SEPARATOR+per+TAB_SEPARATOR+counteremail +TAB_SEPARATOR+counter+TAB_SEPARATOR);
		per= ((double)counterlr / (double) counter)*100;		LOGGER.info(TAB_SEPARATOR+"LenRatio:"+TAB_SEPARATOR+per+TAB_SEPARATOR+counterlr +TAB_SEPARATOR+counter+TAB_SEPARATOR);
		per= ((double)counterdifnum / (double) counter)*100;	LOGGER.info(TAB_SEPARATOR+"DifNum:"+TAB_SEPARATOR+per+TAB_SEPARATOR+counterdifnum +TAB_SEPARATOR+counter+TAB_SEPARATOR);

		Double[] ra = new Double[ratios.size()];
		for (int ii=0;ii<ratios.size();ii++)
			ra[ii] = ratios.get(ii);
		LOGGER.info(TAB_SEPARATOR+"MEAN value:"+TAB_SEPARATOR+ Statistics.getMean(ra)+TAB_SEPARATOR);
		LOGGER.info(TAB_SEPARATOR+"STD value:"+TAB_SEPARATOR+ Statistics.getStdDev(ra)+TAB_SEPARATOR);
		FileUtils.writeLines(ratiosfile, "UTF-8",ratios, "\n");
		FileUtils.writeLines(tusfile, "UTF-8",tus,"\n");
		FileUtils.writeLines(li1tuvsfile, "UTF-8",l1tuvs,"\n");
		FileUtils.writeLines(li2tuvsfile, "UTF-8",l2tuvs,"\n");
		//FileUtils.writeLines(li1toktuvsfile, "UTF-8",l1toktuvs,"\n");
		//FileUtils.writeLines(li2toktuvsfile, "UTF-8",l2toktuvs,"\n");
	}

	public static void main(String[] args) throws IOException {

		DGTHandler dgt = new DGTHandler();
		options = new DGTHandlerOptions();
		options.parseOptions(args);
		dgt.setInFile(options.getInFile());
		dgt.setL1(options.getL1());
		dgt.setL2(options.getL2());
		dgt.setMinTuvLen(options.getMinTuvLen());
		dgt.setMinTuLenRatio(options.getMinTuLenRatio());
		dgt.setMaxTuLenRatio(options.getMaxTuLenRatio());
		dgt.setMaxWordLen(options.getMaxWordLen());
		dgt.setMedWordLen(options.getMedWordLen());
		LOGGER.info("Input file is " + options.getInFile().getAbsolutePath());
		LOGGER.info("Languages: " + l1 + TAB_SEPARATOR + l2);
		dgt.tuStats(options.getInFile()); 
		dgt.tuFilteredStats(options.getInFile());
		//dgt.tmx2txt(options.getInFile());
	}

/*	private void tmx2txt(File tmxFile) throws IOException {
		BufferedReader br=null;
		String line = "";
		boolean tu=false, tu1 = false;
		String seg1="", seg2="";
		List<String> l1tuvs = new ArrayList<String>();
		File li1tuvsfile = new File(tmxFile.getAbsolutePath()+PUNCT+l1+HYPHEN+l2+PUNCT+l1);
		List<String> l2tuvs = new ArrayList<String>();
		File li2tuvsfile = new File(tmxFile.getAbsolutePath()+PUNCT+l1+HYPHEN+l2+PUNCT+l2);
		try{
			br=new BufferedReader(new InputStreamReader(new FileInputStream(tmxFile),"UTF-16LE"));
			while((line = br.readLine())!=null){
				if (line.startsWith("<tuv xml:lang=") && !tu1){
					tu=true;
					continue;
				}
				if (tu && !tu1 && line.startsWith("<seg>")){
					tu1=true;
					seg1 = line.substring(5, line.length()-6);
					continue;
				}
				if (tu && tu1 && line.startsWith("<seg>")){
					seg2 = line.substring(5, line.length()-6);
					tu=false; tu1=false;
					double lenratio =((double) seg1.length() /  (double) seg2.length());
					if (lenratio >maxTULenRatioD || lenratio<minTULenRatioD){  

					}else{
						l1tuvs.add(seg1);
						l2tuvs.add(seg2);
					}
					seg1="";
					seg2="";
				}
			}
			br.close(); //CLOSE THE BufferedReader
		}catch(Exception e){
			System.out.println(e);
		}
		FileUtils.writeLines(li1tuvsfile, l1tuvs,"\n");
		FileUtils.writeLines(li2tuvsfile, l2tuvs,"\n");
	}*/

	private void setInFile(File inFile) {
		DGTHandler.infile = inFile;
	}
	private void setL1(String l1) {
		DGTHandler.l1 = l1;
	}
	private void setL2(String l2) {
		DGTHandler.l2 = l2;
	}
	public void setMinTuvLen(int minTuvLen) {
		DGTHandler.minTuvLen = minTuvLen;
	}
	public void setMinTuLenRatio(double minTuLenRatio) {
		DGTHandler.minTULenRatio = minTuLenRatio;
	}
	public void setMaxTuLenRatio(double maxTuLenRatio) {
		DGTHandler.maxTULenRatio = maxTuLenRatio;
	}
	public void setMaxWordLen(double max_word_length) {
		DGTHandler.max_word_length = max_word_length;
	}
	public void setMedWordLen(double median_word_length) {
		DGTHandler.median_word_length = median_word_length;
	}

}
