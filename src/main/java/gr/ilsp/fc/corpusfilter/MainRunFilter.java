package gr.ilsp.fc.corpusfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
//import gr.ilsp.fc.utils.ISOLangCodes;
//import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitter;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;
import gr.ilsp.nlp.commons.Constants;

public class MainRunFilter {
	private static MainRunFilterOptions options = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(MainRunFilter.class);
	//private static final String[] exclude_symbols={"{", "}", "[", "]","--", "ÿ", "Î", "Ï", "Å", "Â", "Ã", "Ё", "Ð", "☭", "☁", "✚",
	//	"¿", "î", "æ", "û", "ï","è", "ç","ò", "ð","å","à","ë","í","ñ","ý","ã","î", "åã", "åå" };	 
	private static final String[] exclude_symbols={"{", "}", "--", "ÿ", "Î", "Ï", "Å", "Â", "Ã", "Ё", "Ð", "☭", "☁", "✚",
		"¿", "î", "æ", "û", "ï", "ç","ò", "ð","å","à","ë","í","ñ","ý","ã","î", "åã", "åå" };	 

	private static final String[] exclude_grams={"Çá", "ãÚ", "áÍÙÇÊ", "äÚã", "ãÜÇÐÇ", "ãÚ", "ÇÓ", "ãæÇÞ"};
	//û í ñ å ã

	private static File inFile;
	private static int minTuvLen;
	private static int maxTuvLen;
	private static double minlr;
	private static double maxlr;
	private static String[] langs;
	private static LangDetector langDetector;
	private static String baseName;
	private static File baseFile;

	public static void main(String[] args) throws Exception{  
		MainRunFilter rf = new MainRunFilter();
		options = new MainRunFilterOptions();
		options.parseOptions(args);
		rf.setInFile(options.getInfile());
		rf.setMinTuvLen(options.getMinTuvLen());
		rf.setMaxTuvLen(options.getMaxTuvLen());
		rf.setMinTuLenRatio(options.getMinTuLenRatio());
		rf.setMaxTuLenRatio(options.getMaxTuLenRatio());

		rf.setLanguage(options.getLanguage());
		rf.setLangDetector(options.getLangDetector());
		String ext = ".filt."+minTuvLen+"minlen_"+maxTuvLen+"maxlen"+minlr+"minra_"+maxlr+"maxra_"+"dupTUV_dupTU_incoh";
		rf.setBaseName(ext);
		rf.setBaseFile(inFile.getAbsolutePath()+ext);
		List<File> outs = rf.filterCorpus();

	}

	private static List<File> filterCorpus() {
		List<File> outs = new ArrayList<File>();

		File outfile0 = baseFile;
		File outfile1 = new File(baseFile.getAbsolutePath()+".samenum");
		File outfile1a = new File(baseFile.getAbsolutePath()+".nonum");
		File outfile2 = new File(baseFile.getAbsolutePath()+".samesym");
		File outfile3 = new File(baseFile.getAbsolutePath()+".samenumsym");
		File outfile3a = new File(baseFile.getAbsolutePath()+".nonumsamesym");

		outs.add(outfile0); outs.add(outfile1); outs.add(outfile1a); outs.add(outfile2); outs.add(outfile3);	outs.add(outfile3a);  
		String inputLine;
		BufferedReader in;
		BufferedWriter  bw0; // ,bw1, bw2, bw3, bw1a, bw3a;

		BufferedWriter bwinvalid, bwidenTUVs, bwidenTUs, bwsymbols1, bwsent;
		BufferedWriter bwshort, bwlong, bwlang, bwratio, bwemail, bwURL, bwcase, bwrepeats, bwdiffnum;
		//BufferedWriter  bwmaxlen, bwminlen, bwhunscore, bwincoh,  bwcharsets ,bwexclsymbols, bwidenTUV1s, bwidenTUV2s;

		//int  counter0 = 0, counter1= 0, counter1a= 0, counter2= 0, counter3 = 0, counter3a = 0, tempcounter = 0 ;//, comma=0;
		String lang1, lang2, nl1, nl2, l1, l2, temp;
		//String num1, num2;
		double ratio;
		int linecounter=-1 ;
		//boolean found = false;
		Set<String> passed = new HashSet<String>();
		Set<Integer> ids = new HashSet<Integer>();
		//Set<String> nl1list = new HashSet<String>();
		//Set<String> nl2list = new HashSet<String>();

		SentenceSplitterFactory sentencesplitterFactory = new SentenceSplitterFactory();
		SentenceSplitter l1s = sentencesplitterFactory.getSentenceSplitter("eng");
		SentenceSplitter l2s = sentencesplitterFactory.getSentenceSplitter("ell");

		try {
			in = new BufferedReader(new FileReader(inFile));

			bw0 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".pass"),Constants.UTF8));
			/*bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1.getAbsolutePath()),Constants.UTF8));
			bw1a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1a.getAbsolutePath()),Constants.UTF8));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile2.getAbsolutePath()),Constants.UTF8));
			bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3.getAbsolutePath()),Constants.UTF8));
			bw3a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3a.getAbsolutePath()),Constants.UTF8));*/

			bwinvalid = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".invalid"),Constants.UTF8));
			bwsymbols1 = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".non-letters"),Constants.UTF8));
			bwidenTUVs =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".idenTUVs"),Constants.UTF8));
			bwidenTUs =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".idenTUs"),Constants.UTF8));
			bwlang = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".lang"),Constants.UTF8));
			bwsent = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".sents"),Constants.UTF8));
			bwemail = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".email"),Constants.UTF8));
			bwURL = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".URL"),Constants.UTF8));
			bwratio = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".ratio"),Constants.UTF8));
			bwshort = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".short"+minTuvLen),Constants.UTF8));
			bwlong = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".long"+maxTuvLen),Constants.UTF8));
			bwrepeats = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".repeats"),Constants.UTF8));
			bwcase =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".case"),Constants.UTF8));
			bwdiffnum = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".diffnum"),Constants.UTF8));
			/*	bwcharsets =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".charsets"),Constants.UTF8));
			bwexclsymbols =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".exclsymbols"),Constants.UTF8));

			//bwidenTUV1s =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".idenTUV1s"),Constants.UTF8));
			//bwidenTUV2s =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFile.getAbsolutePath()+".idenTUV2s"),Constants.UTF8));	

			bwmaxlen = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".max-len"),Constants.UTF8));
			bwminlen = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".min-len"),Constants.UTF8));
			bwhunscore = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".hun-score"),Constants.UTF8));
			bwincoh = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".incoh"),Constants.UTF8));
			bwnearTUVs = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile.getAbsolutePath()+".nearTUVs"),Constants.UTF8));
			 */

			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//--------------INVALID -------------------------------------------
				if (!inputLine.contains(Constants.TAB)){	//LOGGER.info("not valid line:\t"+ inputLine);
					bwinvalid.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length<2){ //		LOGGER.info("not valid line:\t"+ inputLine);
					bwinvalid.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				//--------------NO-LETTERS -------------------------------------------
				l1 = attrs[0];
				l2 = attrs[1];
				if (l1.isEmpty() || l2.isEmpty()){					//LOGGER.info(linecounter +"\tnon-letters:\t"+ inputLine);
					bwsymbols1.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				nl1 = ContentNormalizer.normtext(l1);
				nl2 = ContentNormalizer.normtext(l2);
				//if ( nl1.isEmpty() || nl2.isEmpty()){ 				//LOGGER.info(linecounter +"\tnon-letters:\t"+ inputLine);
				if ( nl1.length()<3 || nl2.length()<3){ 					
					bwsymbols1.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				//-------------------------IDENTICAL TUVs-----------------------------------
				if (l1.equals(l2)){									//LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					bwidenTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (nl1.equals(nl2)){								//LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					bwidenTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				//---------------------------REPEATS ---------------------------------
				List<String> nl1tokens = FCStringUtils.getTokens(nl1);
				if (nl1tokens.size()>3){
					Set<String> nl1grams = get12Grams(nl1tokens);
					if (nl1grams.size()<nl1tokens.size()){					//LOGGER.info(linecounter + "\trepeats:\t"+ inputLine);
						bwrepeats.write(linecounter+"\t"+inputLine+"\n");
						continue;
					}
				}
				List<String> nl2tokens = FCStringUtils.getTokens(nl2);
				Set<String> nl2grams = get12Grams(nl2tokens);
				if (nl2tokens.size()>3){
					if (nl2grams.size()<nl2tokens.size()){					//LOGGER.info(linecounter + "\trepeats:\t"+ inputLine);
						bwrepeats.write(linecounter+"\t"+inputLine+"\n");
						continue;
					}
				}
				//--------------NEAR DUBLICATE TUs ------------------------------------
				temp = nl1+Constants.TAB+nl2;
				if (passed.contains(temp)){							//LOGGER.info(linecounter +"\tidentical TUs:\t"+inputLine);
					bwidenTUs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}else{
					passed.add(temp);
					ids.add(linecounter);
				}
				//----------------NOT IN TARGET LANGUAGES ----------------------------------
				if (langs!=null){
					lang1 = langDetector.detect(l1);
					lang2 = langDetector.detect(l2);
					if (!(lang1.equals(langs[0]) && lang2.equals(langs[1]))){ 	//LOGGER.info(linecounter +"\tlangs:\t"+ inputLine);
						bwlang.write(linecounter+"\t"+inputLine+"\n");
						continue;
					}
				}
				//--------------------------EMAILs or URLs or CASE---------------------------
				if (TMXHandlerUtils.checkemail(l1, 0.5) || TMXHandlerUtils.checkemail(l2, 0.5)){			//LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					bwemail.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (TMXHandlerUtils.checkurl(l1, 0.5) || TMXHandlerUtils.checkurl(l2, 0.5)){				//LOGGER.info(linecounter +"\turl:\t"+ inputLine);
					bwURL.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (FCStringUtils.isAllUpperCase(l1) * FCStringUtils.isAllUpperCase(l2)<0){					//LOGGER.info(linecounter +"\tcase:\t"+ inputLine);
					bwcase.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				//-----------------------RATIO ------------------------------------
				ratio = (double)l1.length()/(double)l2.length();
				if ( ratio>maxlr || ratio < minlr){					//LOGGER.info(linecounter +"\tratio:\t"+ratio+"\t"+ inputLine);
					bwratio.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				//-----------------------# of sentences ------------------------------------
				List<String> l1sens = l1s.getSentences(l1, 1);
				List<String> l2sens = l2s.getSentences(l2, 1);
				if (l1sens.size()!=l2sens.size()){								//LOGGER.info(linecounter +"\t# sentences:\t"+ratio+"\t"+ inputLine);
					bwsent.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				//-------------------SIZE -----------------------------------------
				List<String> l1tokens = FCStringUtils.getTokens(l1);
				List<String> l2tokens = FCStringUtils.getTokens(l2);
				int l1count = l1tokens.size();
				int l2count = l2tokens.size();
				if (l1count<minTuvLen || l2count<minTuvLen){ 					//LOGGER.info(linecounter +"\tshort:\t"+ inputLine);
					bwshort.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1count>maxTuvLen || l2count>maxTuvLen){					//LOGGER.info(linecounter +"\tlong:\t"+ inputLine);
					bwlong.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				//-------------------NUMBERS -----------------------------------------
				String num1=l1.replaceAll("\\D+","").trim();
				String num2=l2.replaceAll("\\D+","").trim();
				if (!num1.equals(num2)){										//LOGGER.info(linecounter +"\tdiff numbers:\t"+ inputLine);
					bwdiffnum.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				bw0.write(linecounter+"\t"+inputLine+"\n");

				/*if (nl1list.contains(nl1)){ 					//LOGGER.info(linecounter + "\tdup TUV1:\t"+ inputLine);
					bwidenTUV1s.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}else
					nl1list.add(nl1);

				if (nl2list.contains(nl2)){						//LOGGER.info(linecounter + "\tdup TUV2:\t"+ inputLine);	
					bwidenTUV2s.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}else
					nl2list.add(nl2);*/
			}
			in.close();
			bw0.close();
			bwinvalid.close();
			bwidenTUs.close();	bwidenTUVs.close(); bwrepeats.close();
			bwsymbols1.close();
			bwlang.close();	bwsent.close();
			bwemail.close();	bwURL.close(); bwcase.close();
			bwratio.close();
			bwshort.close(); bwlong.close(); bwdiffnum.close();
			/*bwidenTUV1s.close();
			bwidenTUV2s.close();*/

			/*			linecounter=-1;
			in = new BufferedReader(new FileReader(inFile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (ids.contains(linecounter)){

				}

				if (l1.matches(".*[\\u4E00-\\u9FFF].*") || l2.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.matches(".*[\\u0600-\\u06FF].*") || l2.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.matches(".*[\\u0980-\\u09FF].*") || l2.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.matches(".*[\\u0C80-\\u0CFF].*") || l2.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}




				int nl1count = FCStringUtils.countTokens(nl1);
				int nl2count = FCStringUtils.countTokens(nl2);
				if (nl1count<minTuvLen || nl2count<minTuvLen){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);
					bwshort.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				if (nl1count>maxTuvLen || nl2count>maxTuvLen){		//LOGGER.info(linecounter +"\tlong:\t"+ inputLine);
					bwlong.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				if (((float)nl1.length()/(float)l1.length())<0.6){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (((float)nl2.length()/(float)l2.length())<0.6){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}


				List<String> stokens = FCStringUtils.getTokens(l1);
				List<String> ttokens = FCStringUtils.getTokens(l2); 
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);
				if (Statistics.getMax(stokenslen)>40 || Statistics.getMax(ttokenslen)>40){
					LOGGER.info(linecounter + "\tmax word:\t"+ inputLine);
					bwmaxlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (Statistics.getMedian(stokenslen)>=25 || Statistics.getMedian(ttokenslen)>=25){
					LOGGER.info(linecounter + "\tlong median:\t"+ inputLine);
					bwmaxlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (Statistics.getMedian(stokenslen)<2 || Statistics.getMedian(ttokenslen)<2){
					LOGGER.info(linecounter + "\tshort median:\t"+ inputLine);
					bwminlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}


				if (incoherent(stokens)){
					LOGGER.info(linecounter + "\tincoherent TUV1:\t"+ inputLine);
					bwincoh.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (incoherent(ttokens)){
					LOGGER.info(linecounter + "\tincoherent TUV2:\t"+ inputLine);
					bwincoh.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (l1.contains(exclude_symbols[ii]) || l2.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
					bwexclsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				num1=l1.replaceAll("\\D+","");
				num2=l2.replaceAll("\\D+","");
				if (num1.isEmpty() && num2.isEmpty()){
					if (attrs.length>2){
						double hunscore = Double.parseDouble(attrs[2]);
						if (hunscore<-0.3){
							LOGGER.info(linecounter + "\tlow hunscore:\t"+ inputLine);
							bwhunscore.write(linecounter+"\t"+inputLine+"\n");
							continue;
						}
					}
				}


				tempcounter = FCStringUtils.countTokens(l1);
				counter0 = counter0 + tempcounter;
				bw0.write(linecounter + "\t"+inputLine+"\n");

				//nenlist1.add(nen);
				//ndelist1.add(nde);

				if (num1.equals(num2) ){
					if (!num1.isEmpty()){
						counter1 = counter1+tempcounter;
						bw1.write(linecounter + "\t"+inputLine+"\n");
					}else{
						counter1a = counter1a+tempcounter;
						bw1a.write(linecounter + "\t"+inputLine+"\n");
					}
				}
				String ensym = ContentNormalizer.leaveSymbols(l1); 
				String desym = ContentNormalizer.leaveSymbols(l2);
				if (ensym.equals(desym)){
					counter2 = counter2+tempcounter;
					bw2.write(linecounter + "\t"+inputLine+"\n");
				}
				if (num1.equals(num2) && ensym.equals(desym) ){
					if (!num1.isEmpty()){
						counter3 = counter3+tempcounter;
						bw3.write(linecounter + "\t"+inputLine+"\n");
					}else{
						counter3a = counter3a+tempcounter;
						bw3a.write(linecounter + "\t"+inputLine+"\n");
					}
				}
			}
			in.close();
			bw0.close();			bw1.close();			bw1a.close();			bw2.close();			bw3.close();			bw3a.close();
			bwcharsets.close();		bwexclsymbols.close();	bwshort.close();		bwlong.close();
			bwlang.close();			bwratio.close();		bwemail.close();		bwurl.close();			bwcase.close();			bwmaxlen.close();
			bwminlen.close();		bwhunscore.close();		bwincoh.close();		bwnearTUVs.close();		bwrepeats.close();

			bwsymbols1.close();	bwidenTUVs.close();	bwidenTUs.close();	*/


		} catch (IOException e) {
			e.printStackTrace();
		}
		/*LOGGER.info("filtered corpus contains "+ counter0 + " tokens in "+ langs[0]);
		LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in "+ langs[0]);
		LOGGER.info("no number filtered corpus contains "+ counter1a + " tokens in "+ langs[0]);
		LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in "+ langs[0]);
		LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in "+ langs[0]);
		LOGGER.info("no number and same symbols filtered corpus contains "+ counter3a + " tokens in "+ langs[0]);*/
		return outs;
	}

	private static Set<String> get12Grams(List<String> tokens) {
		Set<String> grams = new HashSet<String>();
		for (int jj=0;jj<tokens.size()-1;jj++){
			grams.add(tokens.get(jj));  												//1-gram
			grams.add(tokens.get(jj)+" "+tokens.get(jj+1));								//2-gram
			//if (jj>0){
			//	grams.add(tokens.get(jj-1)+" "+tokens.get(jj)+" "+tokens.get(jj+1));	//3-gram
			//}
		}
		return grams;
	}

	private static Set<String> get123Grams(List<String> tokens) {
		Set<String> grams = new HashSet<String>();
		for (int jj=0;jj<tokens.size()-1;jj++){
			grams.add(tokens.get(jj));  												//1-gram
			grams.add(tokens.get(jj)+" "+tokens.get(jj+1));								//2-gram
			if (jj>0){
				grams.add(tokens.get(jj-1)+" "+tokens.get(jj)+" "+tokens.get(jj+1));	//3-gram
			}
		}
		return grams;
	}

	private void setBaseFile(String string) {
		MainRunFilter.baseFile = new File(string);
	}
	private void setBaseName(String string) {
		MainRunFilter.baseName = string;
	}

	private void setLangDetector(LangDetector langDetector) {
		MainRunFilter.langDetector  = langDetector;

	}

	private void setLanguage(String[] langs) {
		MainRunFilter.langs  = langs;
	}

	private void setMaxTuLenRatio(double maxTuLenRatio) {
		MainRunFilter.maxlr  = maxTuLenRatio;	
	}

	private void setMinTuLenRatio(double minTuLenRatio) {
		MainRunFilter.minlr  = minTuLenRatio;		
	}

	private void setMaxTuvLen(int maxTuvLen) {
		MainRunFilter.maxTuvLen  = maxTuvLen;
	}

	private void setMinTuvLen(int minTuvLen) {
		MainRunFilter.minTuvLen  = minTuvLen;
	}

	private void setInFile(File infile) {
		MainRunFilter.inFile  = infile;
	}




}
