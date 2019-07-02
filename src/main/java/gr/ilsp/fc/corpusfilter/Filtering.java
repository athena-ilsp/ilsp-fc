package gr.ilsp.fc.corpusfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.io.MD5Hash;
//import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.dedup.DedupUtils;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitter;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;
import gr.ilsp.nlp.commons.Constants;

public class Filtering {

	private static final Logger LOGGER = LoggerFactory.getLogger(Filtering.class);
	private static final String[] exclude_symbols={"{", "}", "[", "]","--", "ÿ", "Î", "Ï", "Å", "Â", "Ã", "Ё", "Ð", "☭", "☁", "✚",
		"¿", "î", "æ", "û", "ï","è", "ç","ò", "ð","å","à","ë","í","ñ","ý","ã","î", "åã", "åå" };

	private static final String[] exclude_symbols1={ "ÿ", "Î", "Ï", "Å", "Â", "Ã", "Ё", "Ð", "¿", "î", "æ", "û", "ï","è", "ç","ò", "ð","å","à","ë","í","ñ","ý","ã","î" };
	private static final String[] exclude_grams1={ "Çá", "ãÚ", "áÍÙÇÊ", "äÚã", "ãÜÇÐÇ", "ãÚ", "ÇÓ", "ãæÇÞ", "åã", "åå" ,"--", "☭", "☁", "✚", "{", "}", "[", "]" };

	private static final String[] exclude_grams={"Çá", "ãÚ", "áÍÙÇÊ", "äÚã", "ãÜÇÐÇ", "ãÚ", "ÇÓ", "ãæÇÞ"};
	//û í ñ å ã

	private static final String[] connect={"janfeb", "febmar", "marapr", "aprmay","mayjun", "junjul", "julaug", "augsep", "sepoct", "sepokt", "octnov", "oktnov","novdec", "novdez", "decjan",
		"dezjan", "januaryfebruary", "marchapril", "aprilma", "julyaug", "septemberoct", "septemberokt", "octoberno", "oktoberno", "novemberde", "decemberja", "märzap", "junij", "juliau",
		"augustse", "junej"};

	
	public static void main(String[] args) throws Exception{


		//filterWIiki(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/el-en.csv"), "el", "en");
		//System.exit(0);

		processEN_EL_paracrawl(new File("C:/Users/vpapa/Downloads/en-el.zipporah0.tmx/en-el.zipporah0.tmx"));
		System.exit(0);

		//String a = "&lt; 3823 JulAugSepOctNovDec 3824 JanFebMar &gt;";//	&lt; 3823 SepOktNovDez 3824 JanFebMäAprMai &gt;	6.3";
		//int res1 = checkStrangeStr(ContentNormalizer.basiclatin(a),a, 3,3,3);

		BufferedReader in;
		String[] inlangs = new String[2];		inlangs[0] = "eng"; inlangs[1] = "deu";
		LangDetector langDetector = LangDetectUtils.loadLangDetectors(inlangs,"langdetect");

		int linecounter = 0;
		String inputLine="", en, nen, nde, nen1, nde1, num1 , num2, sym1, sym2, lang1, lang2, nennde, en1, en2, de1, de2, nen2, nde2;
		double ratio, THR=0.8;


		//STEP 1. CLUSTERING BASED ON RULES/HEURISTICS
		//File f1 = new File("/nfs-elrc/wmt18-pcf/last/data.tok");
		//File f2 = new File("/nfs-elrc/wmt18-pcf/last/data");
		//cluster_filter_corpus(f1,f2, inlangs, langDetector);
		//System.exit(0);

		//STEP 2. IN-OUT LANG in EACH CLUSTER (only in the first 5 clusters)
		//File fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumNotEmptySameSym");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumEmptySym");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumDifSym");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymNoPunct");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymPunct");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumEmptySym");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumDifSym");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumNotEmptySameSym");
		//split_cluster_langs(fcat, inlangs, langDetector);
		//fcat = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumEmptySym");
		//split_cluster_langs(fcat, inlangs, langDetector);


		//STEP 3. DEDUP TUs in IN-LANG in EACH CLUSTER 
		//File fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumNotEmptySameSym.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumEmptySym.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumDifSym.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymNoPunct.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymPunct.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumEmptySym.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumDifSym.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumNotEmptySameSym.inlang");
		//dedup_cluster_norm(fdup);
		//fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumEmptySym.inlang");
		//dedup_cluster_norm(fdup);
		//System.exit(0);


		//STEP 4. NEAR DEDUP TUs in each cluster (only in the first 5 clusters)
		//A. generate normalized sentence pairs
		//File fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumNotEmptySameSym.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumEmptySym.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumDifSym.inlang.dedup");
		//normdup(fnormdup);
		//File fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymNoPunct.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymPunct.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumEmptySym.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumDifSym.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumNotEmptySameSym.inlang.dedup");
		//normdup(fnormdup);
		//fnormdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumEmptySym.inlang.dedup");
		//normdup(fnormdup);
		//System.exit(0);

		//*******************************************************
		//B. SORT THEM OFF-LINE
		//*******************************************************

		//C. generate Vectors and Grams for each sent pair
		//File fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumNotEmptySameSym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumEmptySym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumDifSym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymNoPunct.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymPunct.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumEmptySym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumDifSym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumNotEmptySameSym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);
		//fneardup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumEmptySym.inlang.dedup.norm.sort");
		//senpairs2vectors(fneardup);

		//D.dedup based on intersection of n-grams, n=1,2,3,
		File fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumNotEmptySameSym.inlang.dedup.norm.sort.gram");
		File  fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumNotEmptySameSym.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumEmptySym.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumEmptySym.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumDifSym.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.NotEmptySameNumDifSym.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymNoPunct.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymNoPunct.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymPunct.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumNotEmptySameSymPunct.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumEmptySym.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumEmptySym.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		//fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumDifSym.inlang.dedup.norm.sort.gram");
		// fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.EmptyNumDifSym.inlang.dedup");
		//dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumNotEmptySameSym.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumNotEmptySameSym.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);
		fgram = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumEmptySym.inlang.dedup.norm.sort.gram");
		fdup = new File("/nfs-elrc/wmt18-pcf/last/data.tok.DifNumEmptySym.inlang.dedup");
		dedup_cluster_gram(fgram, THR, fdup);

		System.exit(0);

		File datafile = new File(args[0]);
		File scores = new File(args[1]);
		Set<Integer> ss = getIds(new File(args[2]));
		Set<Integer> se = getIds(new File(args[3]));
		Set<Integer> sd = getIds(new File(args[4]));
		Set<Integer> esnp = getIds(new File(args[5]));
		Set<Integer> esp = getIds(new File(args[6]));
		Set<Integer> ee = getIds(new File(args[7]));
		Set<Integer> ed = getIds(new File(args[8]));
		Set<Integer> ds = getIds(new File(args[9]));
		Set<Integer> de = getIds(new File(args[10]));
		Set<Integer> cut = getIds(new File(args[11]));
		Set<Integer> dd = getIds(new File(args[12]));

		linecounter = 0;
		try {
			in = new BufferedReader(new FileReader(datafile));
			//BufferedWriter out = new BufferedWriter(new FileWriter(scores));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scores.getAbsolutePath()),Constants.UTF8));
			double score = 0;
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				score = Double.parseDouble(tt[2]);
				if (ss.contains(linecounter)){
					score = score+15000;
					out.write(score+"\n");
					System.out.println(inputLine);
					continue;
				}
				if (se.contains(linecounter)){
					score = score+12500;
					out.write(score+"\n");
					System.out.println(inputLine);
					continue;
				}
				if (sd.contains(linecounter)){
					score = score+7500;
					out.write(score+"\n");
					continue;
				}
				if (esnp.contains(linecounter)){
					score = score+5000;
					out.write(score+"\n");
					continue;
				}
				if (esp.contains(linecounter) || ee.contains(linecounter)){
					score = score+2500;
					out.write(score+"\n");
					continue;
				}
				if (ed.contains(linecounter) || ds.contains(linecounter) ){
					score = score+1000;
					out.write(score+"\n");
					continue;
				}
				if (de.contains(linecounter) ){
					score = score+100;
					out.write(score+"\n");
					continue;
				}
				if (cut.contains(linecounter)|| dd.contains(linecounter)){
					//score = score+2500;
					out.write("0"+"\n");
					continue;
				}

				out.write(score+"\n");
			}	
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);

		/*Set<Integer> inds= new HashSet<Integer>();
		File filewronghand = new File("C:/Users/vpapa/ELRC/wmt18/in/wrong-inds.hand"); 
		BufferedReader in;
		String inputLine="";
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(filewronghand));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//scores.add(Integer.parseInt(inputLine));
				inds.add((int)Double.parseDouble(inputLine));
			}	
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		File f1 = new File("C:/Users/vpapa/ELRC/wmt18/in/data");
		//BufferedReader in;
		//BufferedWriter out = new BufferedWriter(new FileWriter(new File(f2.getAbsolutePath())));

		int  counter=0, counter1=0;
		linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(f1));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//if (linecounter<=70000000)
				//	continue;
				if (inds.contains(linecounter)){
					String[] tt = inputLine.split("\t");
					double en = (double)tt[0].length();
					double de = (double)tt[1].length();
					double ratio = en/de;
					System.out.println(linecounter + "\t"+ en +"\t"+inputLine);
					//System.out.println(tt[1]+"\t"+ratio);
					if (ratio<=0.5 || ratio>=2){
						counter++;
						System.out.println(linecounter);
						//LOGGER.info("\t"+linecounter+"\t"+tt[0].length()+"\t"+inputLine);
					}
					//LOGGER.info("\t"+linecounter+"\t"+tt[0].length()+"\t"+inputLine);
					//counter++;
				}
			}	
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}*/



		/*		List<Integer> scores= new ArrayList<Integer>();
		File f0 = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len");
		BufferedReader in;
		String inputLine="";
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(f0));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//scores.add(Integer.parseInt(inputLine));
				scores.add((int)Double.parseDouble(inputLine));
			}	
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}


		File f1 = new File("C:/Users/vpapa/ELRC/wmt18/in/data");
		//BufferedReader in;
		//BufferedWriter out = new BufferedWriter(new FileWriter(new File(f2.getAbsolutePath())));

		int  counter=0, counter1=0;
		linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(f1));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//if (linecounter<=70000000)
				//	continue;
				if (scores.get(linecounter)>=15000){
					counter1++;
					String[] tt = inputLine.split("\t");
					double en = (double)tt[0].length();
					double de = (double)tt[1].length();
					double ratio = en/de;
					//System.out.println(tt[1]+"\t"+ratio);
					if (ratio<=0.5 || ratio>=2){
						counter++;
						System.out.println(linecounter);
						//LOGGER.info("\t"+linecounter+"\t"+tt[0].length()+"\t"+inputLine);
					}
					//LOGGER.info("\t"+linecounter+"\t"+tt[0].length()+"\t"+inputLine);
					//counter++;
				}
			}	
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Total="+Integer.toString(counter1) + "\t"+"RA="+Integer.toString(counter));
		System.exit(0);*/

		/*File f2 = new File("C:/Users/vpapa/ELRC/wmt18/res/log_morethan70m");
		String inputLine="";
		int linecounter=-1, counter=0;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f2));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				double en = (double)tt[3].length();
				double de = (double)tt[4].length();
				double ratio = en/de;
				//System.out.println(tt[1]+"\t"+ratio);
				if (ratio<=0.5 || ratio>=2){
					counter++;
					System.out.println(linecounter);
				}
			}	
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);

File f1 = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len.new123");
File f2 = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len.new123_");
		BufferedReader in;
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(f2.getAbsolutePath())));
		String inputLine="";
		int score;
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(f1));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//if (linecounter>100)
				//	break;
				score = (int)Double.parseDouble(inputLine);
				out.write(score+"\n");
			}	
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
System.exit(0);*/


		/*File filedata = new File("C:/Users/vpapa/ELRC/wmt18/in/data");
		File fileforInds  = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len.new12");
		File filenormdata = new File("C:/Users/vpapa/ELRC/wmt18/res/data.12500.norm");
		int thrlow = 12500; 
		int thrhigh = 15000;
		getLevelCorpus(filedata, fileforInds, filenormdata, thrlow, thrhigh);

		filenormdata = new File("C:/Users/vpapa/ELRC/wmt18/res/data.10000.norm");
		thrlow = 10000; 
		thrhigh = 12500;
		getLevelCorpus(filedata, fileforInds, filenormdata, thrlow, thrhigh);

		filenormdata = new File("C:/Users/vpapa/ELRC/wmt18/res/data.15000.norm");
		thrlow = 15000; 
		thrhigh = 20000;
		getLevelCorpus(filedata, fileforInds, filenormdata, thrlow, thrhigh);

		System.exit(0);*/
		/*File fileRemIndsdup= new File("C:/Users/vpapa/ELRC/wmt18/res/todelowered12500");
		File filenormdatasort = new File("C:/Users/vpapa/ELRC/wmt18/res/data.12500.norm.sort");
		dedupDEtoplevelsort(filenormdatasort,fileRemIndsdup, 0, 200000000);

		fileRemIndsdup= new File("C:/Users/vpapa/ELRC/wmt18/res/todelowered10000");
		filenormdatasort = new File("C:/Users/vpapa/ELRC/wmt18/res/data.10000.norm.sort");
		dedupDEtoplevelsort(filenormdatasort,fileRemIndsdup, 0, 200000000);

		fileRemIndsdup= new File("C:/Users/vpapa/ELRC/wmt18/res/todelowered15000");
		filenormdatasort = new File("C:/Users/vpapa/ELRC/wmt18/res/data.15000.norm.sort");
		dedupDEtoplevelsort(filenormdatasort,fileRemIndsdup, 0, 200000000);

		//FilteringUtils.corrections(new File("C:/Users/vpapa/ELRC/wmt18/in/wrong-inds-old.hand"), new File("C:/Users/vpapa/ELRC/wmt18/in/wrong-inds.hand"));
		System.exit(0);*/
		//--------------------------------from clusters to scales based on hun or len ---------------------------------------------------------------		
		/*File filedata = new File("C:/Users/vpapa/ELRC/wmt18/in/data");
		File datafilescores = new File(("C:/Users/vpapa/ELRC/wmt18/in/data104m.row.filt.scores"));
		List<Double> scores = FilteringUtils.getScores(datafilescores);
		File datafilesnewcores = new File(("C:/Users/vpapa/ELRC/wmt18/res/data104m.row.filt.scores.hun"));
		FilteringUtils.getNewScoresHun(filedata, scores, datafilesnewcores);
		datafilesnewcores = new File(("C:/Users/vpapa/ELRC/wmt18/res/data104m.row.filt.scores.len"));
		FilteringUtils.getNewScoresLen(filedata, scores, datafilesnewcores);
		System.exit(0);*/
		//scores.clear();
		//scores=null;
		//**RUN ONCE*****************************************************
		//------------------------------apply moses rule -------------------------------------------------------------------		
		//File filedatatok = new File("C:/Users/vpapa/ELRC/wmt18/in/data.tok");
		//File filetok = new File("C:/Users/vpapa/ELRC/wmt18/in/data.tok.filt.scores");  //0s for cuts, 1s for passes 
		//FilteringUtils.applyMosesRule(filedatatok, 80, 9, filetok); 
		//-------------------------------merge mewscores with correct/wrong/moses-------------------------------------------------------------------
		//File filetok = new File("C:/Users/vpapa/ELRC/wmt18/in/data.tok.filt.scores"); // from Moses rule
		//File filecorrecthand = new File("C:/Users/vpapa/ELRC/wmt18/in/correct-inds.hand"); //manually detected
		//File filewronghand = new File("C:/Users/vpapa/ELRC/wmt18/in/wrong-inds.hand"); //manually detected
		//File datafilesnewcores = new File(("C:/Users/vpapa/ELRC/wmt18/res/data104m.row.filt.scores.len"));
		//File filemerge = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len"); //new file with scores
		//FilteringUtils.mergeFilts(filetok, datafilesnewcores, filecorrecthand, filewronghand, filemerge);
		//System.exit(0);
		//datafilesnewcores = new File(("C:/Users/vpapa/ELRC/wmt18/res/data104m.row.filt.scores.hun"));
		//filemerge = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.hun"); //new file with scores
		//FilteringUtils.mergeFilts(filetok, datafilesnewcores, filecorrecthand, filewronghand, filemerge);
		//System.exit(0);
		//----------------------------------get indeces of lines to be lowered----------------------------------------------------------------		
		/*		File filedata = new File("C:/Users/vpapa/ELRC/wmt18/in/data");
		File filemerge = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.hun");
		File fileRemInds1 = new File("C:/Users/vpapa/ELRC/wmt18/res/tobelowered1");

		List<String> toberemovedstrs = new ArrayList<String>();
		toberemovedstrs.add("Weather and meteo conditions on ");
		toberemovedstrs.add("Holiday homes less than ");
		toberemovedstrs.add("Conversion base : ");
		toberemovedstrs.add("W / Westf / ");
		toberemovedstrs.add("S / Westf / ");
		toberemovedstrs.add("There are only 2 agriturismi");
		toberemovedstrs.add("Show all apartments");
		toberemovedstrs.add("How to convert from");
		toberemovedstrs.add("--> see other products of ");
		toberemovedstrs.add("nationality: Bosnia and");
		toberemovedstrs.add("Recommendation: If you");
		toberemovedstrs.add("W / Holst / B /");
		toberemovedstrs.add("> MA'AT MAGAZINE >");
		toberemovedstrs.add("Stamps » Detailed view:"); 
		toberemovedstrs.add("If you know the name of wrong");
		toberemovedstrs.add("Register and present for free your Accommodations");
		toberemovedstrs.add("Uninstallation: As DLL files are ");
		toberemovedstrs.add("Follow the on-screen directions to complete the uninstallation");
		toberemovedstrs.add("Virus and Trojan Factor:");
		toberemovedstrs.add("1:Indicated search options have produced no results"); 
		toberemovedstrs.add("Start » Vespa PK");
		toberemovedstrs.add("Start » sort by car brand");
		toberemovedstrs.add("BVV Trade Fairs Brno ");
		toberemovedstrs.add("You may run into the same problem");
		toberemovedstrs.add("And in order to thank all the uses' support ");
		toberemovedstrs.add("There are currently no product ");
		toberemovedstrs.add("In seconds, you will learn ");
		toberemovedstrs.add("WaxTime:");
		toberemovedstrs.add("mixed lot of");
		toberemovedstrs.add("Software Downloads Associated with File Extension");
		toberemovedstrs.add("To begin your free file ");
		toberemovedstrs.add("Additional types of files may ");
		toberemovedstrs.add("Share your experiences and views on");
		toberemovedstrs.add("If you know the name of ");
		toberemovedstrs.add("You need to install Adobe ");
		toberemovedstrs.add("At the moment we have no availabilities");
		toberemovedstrs.add("Holiday homes, holiday apartments and self-catering");
		toberemovedstrs.add("There is only one agriturismo to");
		toberemovedstrs.add("Pork side :: Pork side ");
		toberemovedstrs.add("The guest reviews are submitted ");
		toberemovedstrs.add("The Rolex collection, presented to ");
		toberemovedstrs.add("These are general hotel policies for");
		toberemovedstrs.add("Program Modification: Sometimes, not only ");
		toberemovedstrs.add("24-hr front desk");
		toberemovedstrs.add("This entry was posted in");
		toberemovedstrs.add("Browse these categories as well:");
		toberemovedstrs.add("Did someone email you a");
		toberemovedstrs.add("Looking for a B&B holiday");
		toberemovedstrs.add("Surface area : about ");
		toberemovedstrs.add("A: iDate2010 (Dating Business Conference)");
		toberemovedstrs.add("flat 25 AMAREINS - deleted100,000");
		toberemovedstrs.add("Software Downloads Associated with File");
		toberemovedstrs.add("Share your experiences and views");
		toberemovedstrs.add("Did someone email you a");
		toberemovedstrs.add("Before you can open a");
		toberemovedstrs.add("Finally, you can now discover");
		toberemovedstrs.add("Download FileViewPro to Open Your");
		toberemovedstrs.add("Unsure about which type of");
		toberemovedstrs.add("If your PC opens the");
		toberemovedstrs.add("In the case that your");
		toberemovedstrs.add("This allows the intelligence of");
		toberemovedstrs.add("* I confirm having stayed");
		toberemovedstrs.add("I wish to make a");
		toberemovedstrs.add("Spending your vacation in an");
		toberemovedstrs.add("You are currently converting ");
		toberemovedstrs.add("BVV Trade Fairs Brno →");
		toberemovedstrs.add("Register and present for free");
		toberemovedstrs.add("Looking for a B&B ");
		toberemovedstrs.add("Start » sort by car");
		toberemovedstrs.add("Other bed & breakfasts in");
		toberemovedstrs.add("home / agates worldwide /");
		toberemovedstrs.add("Home::Arrow::Fletches and Feathers::natural fletching::Bearpaw");
		toberemovedstrs.add("It supports dBase, Clipper,");
		toberemovedstrs.add("Customer opinions & ratings");
		toberemovedstrs.add("Saturday, July 16, 2016");
		toberemovedstrs.add("Wednesday, July 6, 2016");
		toberemovedstrs.add("Tuesday, July 5, 2016");
		toberemovedstrs.add("Friday, July 15, 2016");
		toberemovedstrs.add("end of stock sales");
		toberemovedstrs.add("The real estate site");
		toberemovedstrs.add("How To Open Your");
		toberemovedstrs.add("What is File Extension");
		toberemovedstrs.add("Before you can open");
		toberemovedstrs.add("Holiday House for rent");
		toberemovedstrs.add("Price and availability RESIDENCE");
		toberemovedstrs.add("Holiday Apartment for rent");
		toberemovedstrs.add("No experiences yet with");
		toberemovedstrs.add("Categories Germany items ");
		toberemovedstrs.add("Drag & Drop Your");
		toberemovedstrs.add("Virtual tour around the");
		toberemovedstrs.add("Did you stay in");
		toberemovedstrs.add("Main page » Catalogue");
		toberemovedstrs.add("You are visiting :");
		toberemovedstrs.add("You are here: Home");
		toberemovedstrs.add("Home Rental Northern Alps");
		toberemovedstrs.add("The guest reviews are");
		toberemovedstrs.add("Newspapers > United States");
		toberemovedstrs.add("You are viewing the weather");
		toberemovedstrs.add("Fill in the form to request");
		toberemovedstrs.add("Fill out the form to contact");
		toberemovedstrs.add("Your search criteria: DatabaseSheet");
		toberemovedstrs.add("More from the press review");
		toberemovedstrs.add("Free Javascript Slideshow is free");
		toberemovedstrs.add("(29/10/2013) Released for BAE ");
		toberemovedstrs.add("Reviews of the apartment");
		toberemovedstrs.add("The Spirit of Ma'at");
		toberemovedstrs.add("Start » PLAYMOBIL »");
		toberemovedstrs.add("Yacht charter on the");
		toberemovedstrs.add("Ink & Paper -");
		toberemovedstrs.add("Software: Desktop Enhancements:Screensavers >");
		toberemovedstrs.add("Spray Can Lechler Paint");
		toberemovedstrs.add("Customer ratings for this");
		toberemovedstrs.add("distance in Km from");
		toberemovedstrs.add("Studio sleeping corner 4");
		double score  = 10000;
		getIndsManDetect(filedata, filemerge, score, toberemovedstrs, fileRemInds1);
		System.exit(0);*/

		File filemerge  = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.hun");
		File filenewmerge = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.hun.MANDUP");
		File fileRemInds1 = new File("C:/Users/vpapa/ELRC/wmt18/res/tobeloweredMANDUP");
		//List<String> lowered1= FileUtils.readLines(fileRemInds1, Constants.UTF8);
		int newscale = 7500;
		updateScoresFromIndeces(filemerge, fileRemInds1, filenewmerge, newscale);
		System.exit(0);

		///--------------------------get indeces of lines to be lowered due to deduplcation  (ngrams) SORTED VERSION--------------------------------------------
		//filedata = new File("C:/Users/vpapa/ELRC/wmt18/data");
		//fileforInds  = new File("C:/Users/vpapa/ELRC/wmt18/data104m.merge.filt.scores.len");
		//File filenormdata = new File("C:/Users/vpapa/ELRC/wmt18/data.top.norm");
		//int thr = 15000; 
		//getLevelCorpus(filedata, fileforInds, filenormdata, thr);
		//**THE output should sorted *****************************************************
		//System.exit(0);
		File fileRemInds2 = new File("C:/Users/vpapa/ELRC/wmt18/res/tobelowered2");
		//dedupDEtoplevelsort(filenormdata,fileRemInds2, 0, 200000000);
		//System.exit(0);		 
		fileRemInds1 = new File("C:/Users/vpapa/ELRC/wmt18/res/tobelowered1");
		//--------------------------merges indeces of lines to be lowered due to upper methods--------------------------------------------		
		//File fileRemInds = new File(("C:/Users/vpapa/ELRC/wmt18/res/tobelowered1-2"));
		//lowered1= FileUtils.readLines(fileRemInds1, Constants.UTF8);
		//List<String> lowered2= FileUtils.readLines(fileRemInds2, Constants.UTF8);
		//lowered1.addAll(lowered2);
		//FileUtils.writeLines(fileRemInds, Constants.UTF8, lowered1, "\n");
		//File fileinds = new File(("C:/Users/vpapa/ELRC/wmt18/res/tobelowered1-2"));

		//--------------------------update file with scores FINAL STEP--------------------------------------------			
		File filescores = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len");
		File filenewscores = new File("C:/Users/vpapa/ELRC/wmt18/res/data104m.merge.filt.scores.len.new12");
		//double newscale = 7500;
		//File filescores = new File(args[0]);
		//File fileinds = new File(args[1]);
		//File filenewscores = new File(args[2]);
		//double oldscale = Double.parseDouble(args[3]);
		//double newscale = Double.parseDouble(args[3]);
		//updateScoresFromIndeces(filescores, fileRemInds, filenewscores, newscale);
		System.exit(0);
		//-----------------------------------------------------------------------------------------------

		/*File filetok = new File("C:/Users/vpapa/ELRC/wmt18/data.tok.filt.scores");
		File filerow = new File("C:/Users/vpapa/ELRC/wmt18/data.row.filt.scores");
		File filedata = new File("C:/Users/vpapa/ELRC/wmt18/data");
		File filesubcorpus = new File("C:/Users/vpapa/ELRC/wmt18/data.subcorpus");
		File filemerge = new File("C:/Users/vpapa/ELRC/wmt18/data.merge.filt.scores");
		getSubCorpus(filedata, filetok, filerow, filemerge, filesubcorpus);

		//filemerge =  mergeFilts( filetok,  filerow, filemerge);
		Set<String> voc = new HashSet<String>();
		voc = getVoc(filedata, filemerge, 0, "3", voc);
		//TO DO ADD LIST OF LINES

		System.exit(0);
		 */
		//counting(new File("C:/Users/vpapa/ELRC/wmt18/data.filt"));
		//System.exit(0);

		//filterMosesRules(new File("C:/Users/vpapa/ELRC/wmt18/data.tok"));
		//System.exit(0);

		//List<String> lines = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/wmt18/data.filt"), Constants.UTF8);
		//File tempfile = new File("C:/Users/vpapa/ELRC/wmt18/data.filt.5len_0.3ra_2ra_dupTU_dupTUV_incoh.v1");
		//File outdir1 = clusterbylength(tempfile, 2);
		//File outdir1 = new File("C:/Users/vpapa/ELRC/wmt18/bylen");
		//dedupWMT18_TUV(outdir1, ".dedup", 2, 6);
		//System.exit(0);

		System.out.println("START");
		if (args.length==3){

			//clusterbysite(args);

			//LOGGER.info("------------scoring by filtering------------");
			scoring_filter(args);
			System.exit(0);

			//LOGGER.info("------------REscoring by filtering------------");
			//checkmonths1(args);
			//rescoring(args);
			//System.exit(0);

			//LOGGER.info("------------normalizing------------");
			//normalizing(args);
			//System.exit(0);

			//LOGGER.info("------------scoring by deduplicating------------");
			scoring_dedup(args);
			//scoring_dedup1(args);
			//System.exit(0);
		}
		System.exit(0);


		LOGGER.info("\nargs:\n 0=fullpath of infile\n 1=languages separated by ;\n 2=minTUVlen\n 3=maxTUVlen\n 4=minTUVratio\n 5=maxTUVratio\n 6=version");
		File infile = new File(args[0]); 
		String[] langs = args[1].split(Constants.SEMICOLON);
		langs[0]=ISOLangCodes.get3LetterCode(langs[0]);
		langs[1]=ISOLangCodes.get3LetterCode(langs[1]);	
		int minTuvLen = Integer.parseInt(args[2]);
		int maxTuvLen = Integer.parseInt(args[3]);
		float minratio = Float.parseFloat(args[4]);
		float maxratio = Float.parseFloat(args[5]);
		String ext = ".filt."+args[2]+"minlen_"+args[3]+"maxlen"+args[4]+"minra_"+args[5]+"maxra_"+"dupTUV_"+"incoh";
		String version = args[6];
		//STEP 1.
		new File(infile.getAbsolutePath()+ext+"."+version);
		List<File> outs = filterCorpus(infile, langs, minTuvLen, maxTuvLen, minratio, maxratio, ext, version);
		//System.exit(0);

		//STEP 2.
		File outdir = clusterbylength(outs.get(0), 2);
		//System.exit(0);

		//STEP 3
		File outfile = dedupWMT18_TUV(outdir, ".dedup", 2 , 6);
		//File outfile = dedupWMT18(outs.get(0), ".dedup", 2 );
		System.exit(0);

		//STEP 3
		buildcorpus(args);
		System.exit(0);

		//subsetWMT18(args);
		//System.exit(0);

		//test_enpl_paracrawl();
		//System.exit(0);
	}


	private static void processEN_EL_paracrawl(File file) {
		BufferedReader in;
		String line;
		String en_seg="", el_seg = "", site="";
		boolean en =false, el = false;
		List<String> en_source = new ArrayList<String>();
		List<String> el_source = new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		List<String> newlist = new ArrayList<String>();
		Map<String, Integer> en_site_coun = new HashMap<String, Integer>();
		Map<String, Integer> el_site_coun = new HashMap<String, Integer>();
		int counter=0;
		try {
			in = new BufferedReader(new FileReader(file));
			while ((line = in.readLine()) != null) {
				line=line.trim();
				//System.out.println(line);
				if (line.equals("</tu>")){
					counter++;
					en = false;			el = false;
					en_seg = "";		el_seg = "";
					en_source.clear();	el_source.clear();
					continue;
				}
				if (line.equals("<tuv xml:lang=\"en\">")){
					en=true;
					continue;
				}
				if (line.equals("<tuv xml:lang=\"el\">")){
					el=true;
					continue;
				}
				if (line.startsWith("<prop type=\"source-document\">")){
					line = line.substring(line.indexOf(">")+1, line.lastIndexOf("<"));
					//System.out.println(line);
					URL url;
					try {
						url = new URL(line);
						site = url.getAuthority();
					} catch (MalformedURLException e) {
						site = "unknown";
					}
					if (en){
						if (!en_source.contains(site))
							en_source.add(site);
					}
					if (el){
						if (!el_source.contains(site))
							el_source.add(site);
					}
					continue;
				}
				if (line.startsWith("<seg>")){
					if (en){
						if (en_seg.isEmpty())
							en_seg = line.substring(line.indexOf(">")+1, line.lastIndexOf("<"));	
					}
					if (el){
						if (el_seg.isEmpty())
							el_seg = line.substring(line.indexOf(">")+1, line.lastIndexOf("<"));
					}

				}
				if (en && el){
					if (!en_seg.isEmpty() && !el_seg.isEmpty()){
						String normS = ContentNormalizer.normtext(en_seg);
						String normT = ContentNormalizer.normtext(el_seg);
						if (normS.isEmpty() || normT.isEmpty() || normS.equals(normT)){
							en = false;			el = false;
							en_seg = "";		el_seg = "";
							en_source.clear();	el_source.clear();
							continue;
						}
						String normtemp = normS+Constants.TAB+normT;
						if (list.contains(normtemp))
							continue;
						list.add(normtemp);
						newlist.add(en_seg+Constants.TAB+el_seg);
						en = false;			el = false;
						int count=0;
						for (String en_s:en_source){
							if (en_site_coun.containsKey(en_s))
								count = en_site_coun.get(en_s)+1;
							else
								count=1;
							en_site_coun.put(en_s, count);
						}
						for (String el_s:el_source){
							if (el_site_coun.containsKey(el_s))
								count = el_site_coun.get(el_s)+1;
							else
								count=1;
							el_site_coun.put(el_s, count);
						}
					}
				}
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		list.clear();
		System.out.println(newlist.size());
		try {
			FileUtils.writeLines(new File(file.getAbsolutePath()+".txt"), Constants.UTF8, newlist, "\n");
			//newlist.clear();
			en_source = new ArrayList<String>();
			el_source = new ArrayList<String>();
			Set<String> sites=en_site_coun.keySet();
			Iterator<String> sites_it = sites.iterator();
			String key="";
			while (sites_it.hasNext()){									
				key = sites_it.next();					
				en_source.add(key+Constants.TAB+en_site_coun.get(key));
			}
			FileUtils.writeLines(new File(file.getAbsolutePath()+".en_sites"), Constants.UTF8, en_source, "\n");

			sites=el_site_coun.keySet();
			sites_it = sites.iterator();
			while (sites_it.hasNext()){									
				key = sites_it.next();					
				el_source.add(key+Constants.TAB+el_site_coun.get(key));
			}
			FileUtils.writeLines(new File(file.getAbsolutePath()+".el_sites"), Constants.UTF8, el_source, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Set<Integer> getIds(File file) {
		Set<Integer> res = new HashSet<Integer>();
		BufferedReader in;
		String inputLine;
		try {
			in = new BufferedReader(new FileReader(file));
			while ((inputLine = in.readLine()) != null) {
				String[] tt = inputLine.split("\t");
				res.add(Integer.parseInt(tt[0]));
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	private static void sepSymSets(File f) throws IOException {
		BufferedReader in;
		//BufferedWriter outall = new BufferedWriter(new FileWriter(new File(f.getAbsolutePath()+".symall")));
		//BufferedWriter outpart = new BufferedWriter(new FileWriter(new File(f.getAbsolutePath()+".sympart")));
		BufferedWriter outall = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath()+".symall"),Constants.UTF8));
		BufferedWriter outpart = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath()+".sympart"),Constants.UTF8));
		
		int  linecounter=0;
		String inputLine, en, de , sym1, sym2;
		try {
			in = new BufferedReader(new FileReader(f));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[1].trim();		de = tt[2].trim();	//nen = ContentNormalizer.normtext(en);		nde = ContentNormalizer.normtext(de);
				sym1 = 	ContentNormalizer.leaveSymbols(en); 
				sym2 = ContentNormalizer.leaveSymbols(de);
				if (sym1.equals("."))
					outall.write(inputLine+"\n");
				else
					outpart.write(inputLine+"\n");
			}
			in.close();
			outall.close();
			outpart.close();
		}catch (IOException e) {
			e.printStackTrace();
		}


	}
	private static void normdup(File fnormdup) throws IOException {
		BufferedReader in;
		//BufferedWriter outfnormdup = new BufferedWriter(new FileWriter(new File(fnormdup.getAbsolutePath()+".norm")));
		BufferedWriter outfnormdup = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fnormdup.getAbsolutePath()+".norm"),Constants.UTF8));
		int  linecounter=0;
		String inputLine, en, de , nen, nde;
		try {
			in = new BufferedReader(new FileReader(fnormdup));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[1].trim();		de = tt[2].trim();	nen = ContentNormalizer.normtext(en);		nde = ContentNormalizer.normtext(de);
				/*String[] en1 = nen.split(" "); 		String en2="";
				for (int ii=0;ii<en1.length;ii++){
					if (en1[ii].length()>1)
						en2 = en2 + " "+ en1[ii];
				}
				en2 = en2.trim();
				String[] de1 = nde.split(" "); 		String de2="";
				for (int ii=0;ii<de1.length;ii++){
					if (de1[ii].length()>1)
						de2 = de2 + " "+ de1[ii];
				}
				de2 = de2.trim();
				outfnormdup.write(tt[0]+"\t"+en2 +"\t"+de2+"\t"+tt[3]+"\n");*/
				outfnormdup.write(tt[0]+"\t"+nen +"\t"+nde+"\t"+tt[3]+"\n");
			}
			in.close();
			outfnormdup.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

	}



	private static void dedup_cluster_gram(File fgram, double THR, File fdup) throws IOException {
		System.out.println(fgram.getAbsolutePath());
		BufferedReader in;
		//BufferedWriter outfneardedup = new BufferedWriter(new FileWriter(new File(fgram.getAbsolutePath()+"."+Double.toString(THR)+".neardedup")));
		BufferedWriter outfneardedup = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fgram.getAbsolutePath()+"."+Double.toString(THR)+".neardedup"),Constants.UTF8));
		
		int  linecounter=0;
		String inputLine, gram;

		List<Set<String>> listset = new ArrayList<Set<String>>();
		List<String> listid = new ArrayList<String>();
		Set<String> gramset = new HashSet<String>();

		try {
			in = new BufferedReader(new FileReader(fgram));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				gramset.clear();
				String[] tt = inputLine.split("\t");
				gram = tt[1].trim();
				if (gram.startsWith(","))
					gram=gram.substring(1);
				for (String t: gram.split(",")){
					gramset.add(t);
				}
				System.out.println(linecounter+"\t:"+ gramset.size());
				//listset.add(gramset);
				listset.add(new HashSet(gramset));
				listid.add(tt[0]);
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> gramset1 = new HashSet<String>();
		Set<String> gramset2 = new HashSet<String>();
		Set<Integer> checked = new HashSet<Integer>();
		Set<String> remained = new HashSet<String>();
		double ratio;
		boolean found = false;
		Set intersection = new HashSet(gramset1);
		for (int ii=0;ii<listid.size()-1;ii++){
			System.out.println(listid.get(ii));
			if (checked.contains(ii)){
				System.out.println("\tchecked");
				continue;
			}

			gramset1 = listset.get(ii);
			found = false;
			//for (int jj=ii+1;jj<listid.size();jj++){
			for (int jj=ii+1;jj<ii+1000;jj++){
				if (jj>=listid.size())
					break;
				if (checked.contains(jj)){
					System.out.println("\tchecked");
					continue;
				}
				intersection = new HashSet(gramset1);
				gramset2 = listset.get(jj);
				intersection.retainAll(gramset2);
				ratio = (double)intersection.size()  / (double)Math.min(gramset2.size(),gramset1.size()) ;
				if (ratio>=THR){
					if (gramset2.size()> gramset1.size()){
						found = true;
						break;
					}else{
						System.out.println("\t"+listid.get(jj));
						checked.add(jj);
					}
				}
			}
			if (!found){
				System.out.println(">>>>\t"+ listid.get(ii));
				//outfneardedup.write(listid.get(ii)+"\n");
				remained.add(listid.get(ii));
			}
		}
		//outfneardedup.close();
		linecounter=0;
		try {
			in = new BufferedReader(new FileReader(fdup));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				if (remained.contains(tt[0])){
					outfneardedup.write(inputLine+"\n");
				}
			}
			in.close();
			outfneardedup.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void senpairs2vectors(File fneardup) throws IOException {

		BufferedReader in;
		int linecounter = 0;
		String inputLine="", en, de, nen, nde;
		double ratio, THR=0.7;

		//BufferedWriter outvector = new BufferedWriter(new FileWriter(new File(fneardup.getAbsolutePath()+".vector")));
		//BufferedWriter outgram = new BufferedWriter(new FileWriter(new File(fneardup.getAbsolutePath()+".gram")));
		BufferedWriter outvector = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fneardup.getAbsolutePath()+".vector"),Constants.UTF8));
		BufferedWriter outgram = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fneardup.getAbsolutePath()+".gram"),Constants.UTF8));
		
		//Map<String, String> list = new HashMap<String, String>();
		Map<String, Integer> wordlist = new HashMap<String, Integer>();
		int wordcounter=0;
		String temp;
		try {
			in = new BufferedReader(new FileReader(fneardup));
			String linev="", lineg="";
			Set<String> grams = new HashSet<String>();
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt1 = inputLine.split("\t");
				en = tt1[1].trim();		de = tt1[2].trim();	nen = ContentNormalizer.normtext(en);		nde = ContentNormalizer.normtext(de);
				List<String> tl = FCStringUtils.getTokens(nen);
				List<String> nl = new ArrayList<String>();
				linev = tt1[0]+Constants.TAB;
				lineg = tt1[0]+Constants.TAB;
				for (String word:tl){
					if (!wordlist.containsKey(word)){
						wordcounter++;
						wordlist.put(word, wordcounter);
					}
					temp = Integer.toString(wordlist.get(word));
					linev = linev+wordlist.get(word)+" ";
					nl.add(temp);
				}
				grams = getGrams(nl);				
				tl = FCStringUtils.getTokens(nde);
				nl = new ArrayList<String>();
				linev = linev.trim()+Constants.TAB;
				for (String word:tl){
					if (!wordlist.containsKey(word)){
						wordcounter++;
						wordlist.put(word, wordcounter);
					}
					temp = Integer.toString(wordlist.get(word));
					linev = linev + wordlist.get(word)+" ";
					nl.add(temp);
				}
				outvector.write(linev.trim()+"\n");

				grams.addAll(getGrams(nl));
				Iterator<String> key = grams.iterator();
				while (key.hasNext()){
					lineg= lineg+","+key.next();
				}	
				outgram.write(lineg+"\n");	
			}
			in.close();
			outvector.close();
			outgram.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void dedup_cluster_norm(File fdup) throws IOException {
		//BufferedWriter outfdedup = new BufferedWriter(new FileWriter(new File(fdup.getAbsolutePath()+".dedup")));
		BufferedWriter outfdedup = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fdup.getAbsolutePath()+".dedup"),Constants.UTF8));
		BufferedReader in;
		int  linecounter=0;
		String inputLine, en, de, nen, nde, nennde;

		Map<String, String> list = new HashMap<String, String>();
		try {
			in = new BufferedReader(new FileReader(fdup));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[1].trim();		de = tt[2].trim();
				nen = ContentNormalizer.normtext(en);
				nde = ContentNormalizer.normtext(de);
				nennde = nen+Constants.TAB+nde;
				if (list.containsKey(nennde)){
					continue;
				}
				list.put(nennde, inputLine);
			}
			in.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

		Set<String> t = list.keySet();
		Iterator<String> it = t.iterator();
		String  line="", nline="";
		while (it.hasNext()){
			nline = it.next();
			line= list.get(nline);
			outfdedup.write(line+"\n");
		}
		outfdedup.close();	
	}

	private static void split_cluster_langs(File fcat, String[] inlangs, LangDetector langDetector) throws IOException {
		//BufferedWriter outfcat1 = new BufferedWriter(new FileWriter(new File(fcat.getAbsolutePath()+".inlang")));
		//BufferedWriter outfcat2 = new BufferedWriter(new FileWriter(new File(fcat.getAbsolutePath()+".outlang")));
		BufferedWriter outfcat1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fcat.getAbsolutePath()+".inlang"),Constants.UTF8));
		BufferedWriter outfcat2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fcat.getAbsolutePath()+".outlang"),Constants.UTF8));
		
		BufferedReader in;

		//LangDetector langDetector1 = LangDetectUtils.loadLangDetectors(inlangs,"tika");
		int  linecounter=0;
		String inputLine, en, de, lang1;
		try {
			in = new BufferedReader(new FileReader(fcat));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[1].trim();		de = tt[2].trim();

				lang1 = langDetector.detect(en);
				if (!lang1.equals(inlangs[0])){
					outfcat2.write(inputLine+"\n");	
					continue;
				}
				lang1 = langDetector.detect(de);
				if (!lang1.equals(inlangs[1])){
					outfcat2.write(inputLine+"\n");	
					continue;
				}			
				outfcat1.write(inputLine+"\n");
			}
			in.close();
			outfcat1.close();
			outfcat2.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void cluster_filter_corpus(File f1, File f2, String[] inlangs, LangDetector langDetector ) throws IOException {
		//SentenceSplitterFactory sentencesplitterFactory = new SentenceSplitterFactory();
		//SentenceSplitter enss = sentencesplitterFactory.getSentenceSplitter(inlangs[0]);
		//SentenceSplitter dess = sentencesplitterFactory.getSentenceSplitter(inlangs[1]);

		BufferedWriter out1 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".moses"),Constants.UTF8));
		BufferedWriter out2 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".empty"),Constants.UTF8));
		BufferedWriter out3 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".equal"),Constants.UTF8));
		BufferedWriter out4 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".empty3"),Constants.UTF8));
		BufferedWriter out5 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".uniqLetsLess3"),Constants.UTF8));
		BufferedWriter out6 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".sameLetInRowMore3"),Constants.UTF8));
		BufferedWriter out8a =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".emailLike"),Constants.UTF8));
		BufferedWriter out8b =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".urlLike"),Constants.UTF8));
		BufferedWriter out7 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".strangeWords"),Constants.UTF8));
		BufferedWriter out9 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".lessLatinLets"),Constants.UTF8));
		BufferedWriter out10 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".notInEN"),Constants.UTF8));
		BufferedWriter out11 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".notInDE"),Constants.UTF8));
		BufferedWriter out12 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".hun001"),Constants.UTF8));
		BufferedWriter out13 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".ratio4"),Constants.UTF8));
		//BufferedWriter out14 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".nearTUs",Constants.UTF8));

		BufferedWriter out_22 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".NotEmptySameNumNotEmptySameSym"),Constants.UTF8));
		BufferedWriter out_21 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".NotEmptySameNumEmptySym"),Constants.UTF8));
		BufferedWriter out_20 =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".NotEmptySameNumDifSym"),Constants.UTF8));

		BufferedWriter out_12a =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".EmptyNumNotEmptySameSymNoPunct"),Constants.UTF8));
		BufferedWriter out_12b =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".EmptyNumNotEmptySameSymPunct"),Constants.UTF8));
		BufferedWriter out_11 =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".EmptyNumEmptySym"),Constants.UTF8)); 
		BufferedWriter out_10 =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".EmptyNumDifSym"),Constants.UTF8));

		BufferedWriter out_02 =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".DifNumNotEmptySameSym"),Constants.UTF8)); 
		BufferedWriter out_01 =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".DifNumEmptySym"),Constants.UTF8)); 
		BufferedWriter out_00 =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1.getAbsolutePath()+".DifNumDifSym"),Constants.UTF8));
		
		/*BufferedWriter out1 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".moses")));
		BufferedWriter out2 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".empty")));
		BufferedWriter out3 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".equal")));
		BufferedWriter out4 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".empty3")));
		BufferedWriter out5 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".uniqLetsLess3")));
		BufferedWriter out6 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".sameLetInRowMore3")));
		BufferedWriter out8a = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".emailLike")));
		BufferedWriter out8b = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".urlLike")));
		BufferedWriter out7 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".strangeWords")));
		BufferedWriter out9 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".lessLatinLets")));
		BufferedWriter out10 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".notInEN")));
		BufferedWriter out11 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".notInDE")));
		BufferedWriter out12 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".hun001")));
		BufferedWriter out13 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".ratio4")));
		//BufferedWriter out14 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".nearTUs")));

		BufferedWriter out_22 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".NotEmptySameNumNotEmptySameSym")));
		BufferedWriter out_21 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".NotEmptySameNumEmptySym")));
		BufferedWriter out_20 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".NotEmptySameNumDifSym")));

		BufferedWriter out_12a = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".EmptyNumNotEmptySameSymNoPunct")));
		BufferedWriter out_12b = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".EmptyNumNotEmptySameSymPunct")));
		BufferedWriter out_11 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".EmptyNumEmptySym"))); 
		BufferedWriter out_10 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".EmptyNumDifSym")));

		BufferedWriter out_02 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".DifNumNotEmptySameSym"))); 
		BufferedWriter out_01 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".DifNumEmptySym"))); 
		BufferedWriter out_00 = new BufferedWriter(new FileWriter(new File(f1.getAbsolutePath()+".DifNumDifSym")));*/
		
		BufferedReader in;
		int  linecounter=0,  entoks, detoks;
		String inputLine, en, de, nen, nde, nen1, nde1, num1, num2, sym1, sym2, lang1, lang2;
		double hunscore=0;
		Set<Integer> mosescut = new HashSet<Integer>();
		try {
			in = new BufferedReader(new FileReader(f1));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();		de = tt[1].trim();
				entoks = en.split(" ").length;
				detoks = de.split(" ").length;
				if (entoks>80 || detoks>80 || entoks>9*detoks || detoks>9*entoks){
					out1.write(linecounter +"\t"+inputLine+"\n");
					mosescut.add(linecounter);
					continue;
				}
			}
			in.close();
			out1.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

		linecounter = 0;
		try {
			in = new BufferedReader(new FileReader(f2));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (mosescut.contains(linecounter))
					continue;
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en); nen1 = ContentNormalizer.basiclatin(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de); nde1 = ContentNormalizer.basiclatin(de);
				hunscore = Double.parseDouble(tt[2].replaceAll(" ", ""));

				List<String> entokens = Arrays.asList(en.split(" "));
				List<String> detokens = Arrays.asList(de.split(" "));
				entoks = entokens.size();
				detoks = detokens.size();
				//List<String> nentokens = Arrays.asList(nen.split(" "));
				//List<String> ndetokens = Arrays.asList(nde.split(" "));

				if (nen.isEmpty() || nde.isEmpty()){
					out2.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				if (nen.equals(nde)){
					out3.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				if (nen1.isEmpty() || nde1.isEmpty()){
					out2.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				if (nen1.equals(nde1)){
					out3.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}

				if (((double)nen1.length()/(double)en.length()<0.5) || ((double)nde1.length()/(double)de.length()<0.5)){
					out9.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}

				if (nen.length()<3 || nen1.length()<3 || nde.length()<3 || nde1.length()<3){
					out4.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}	

				//less than 3 unique basic letters or more than 3 times the same letter in row or many transitions from lower to captial to symbol and vice versa
				int res = checkStrangeStr(nen1,en, 3,3,3);
				if (res==1){
					out5.write(linecounter +"\t"+inputLine+"\n");	
					continue;
				}
				if (res==2){
					out6.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				if (res==3){
					out7.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				res = checkStrangeStr(nde1,de, 3,3,3);
				if (res==1){
					out5.write(linecounter +"\t"+inputLine+"\n");	
					continue;
				}
				if (res==2){
					out6.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				if (res==3){
					out7.write(linecounter +"\t"+inputLine+"\n");
					continue;
				}
				//more than 50% of length is covered by e-mail//
				if (TMXHandlerUtils.checkemail(en, 0.5) || TMXHandlerUtils.checkemail(de, 0.5)){
					out8a.write(linecounter +"\t"+inputLine+"\n");		
					continue;
				}
				//more than 50% of length is covered by url//
				if (TMXHandlerUtils.checkurl(en, 0.5) || TMXHandlerUtils.checkurl(de, 0.5)){
					out8b.write(linecounter +"\t"+inputLine+"\n");	
					continue;
				}

				double len = en.length();
				double lde = de.length();
				double ra = len/lde;
				if (ra > 4 || ra<0.25){
					out13.write(linecounter +"\t"+inputLine+"\n");	
					continue;
				}
				if (entoks>8){
					lang1 = langDetector.detect(en);
					if (!lang1.equals(inlangs[0])){
						out10.write(linecounter +"\t"+inputLine+"\n");	
						continue;
					}
				}
				if (detoks>8){
					lang2 = langDetector.detect(de);
					if (!lang2.equals(inlangs[1])){
						out11.write(linecounter +"\t"+inputLine+"\n");	
						continue;
					}
				}

				if (hunscore<0.01 ){
					out12.write(linecounter+"\t"+inputLine+ "\n");
					continue;
				}

				num1=en.replaceAll("\\D+","");
				num2=de.replaceAll("\\D+","");
				sym1 = 	ContentNormalizer.leaveSymbols(en); 
				sym2 = ContentNormalizer.leaveSymbols(de);

				if (num1.equals(num2)){  
					if (!num1.isEmpty()){ 
						if (sym1.equals(sym2)){ 
							if (!sym1.isEmpty()){ 
								//if (ende.containsKey(nen)){
								//	if (!ende.get(nen).equals(nde)){
								//		out_22.write(linecounter+"\t"+inputLine+ "\n"); //not empty and same numbers, not empty and same symbols
								//	}
								//}else{ 
								//	ende.put(nen, nde);
								//	System.out.println(inputLine);
								out_22.write(linecounter+"\t"+inputLine+ "\n");  //not empty and same numbers, not empty and same symbols
								//}
							}else{ 
								out_21.write(linecounter+"\t"+inputLine+ "\n"); //same and not empty numbers, empty symbols
							}
						}else{ 
							out_20.write(linecounter+"\t"+inputLine+ "\n"); //same and not empty numbers but different symbols
						}
					}else{  //empty numbers
						if (sym1.equals(sym2)){ 
							if (!sym1.isEmpty()){
								if (sym1.equals("."))
									out_12b.write(linecounter+"\t"+inputLine+ "\n"); //empty numbers, not empty and same symbols not punct
								else
									out_12a.write(linecounter+"\t"+inputLine+ "\n"); //empty numbers, not empty and same symbols more than punct
							}else{ 
								out_11.write(linecounter+"\t"+inputLine+ "\n"); ////empty numbers and empty symbols
							}
						}else{
							out_10.write(linecounter+"\t"+inputLine+ "\n"); //empty numbers, dif symbols
						}
					}
				}else{ 
					if (sym1.equals(sym2)){ 
						if (!sym1.isEmpty()){ 
							out_02.write(linecounter+"\t"+inputLine+ "\n"); //different numbers, not empty and same symbols
						}else{ 
							out_01.write(linecounter+"\t"+inputLine+ "\n"); //different numbers and empty symbols
						}
					}else{
						out_00.write(linecounter+"\t"+inputLine+ "\n"); //dif numbers, dif symbols
					}
				}
			}	
			in.close();	
			out2.close();
			out3.close();
			out4.close();
			out5.close();
			out6.close();
			out7.close();
			out8a.close();
			out8b.close();
			out9.close();
			out10.close();
			out11.close();
			out12.close();
			out13.close();
			out_22.close(); out_21.close(); out_20.close();
			out_12a.close(); out_12b.close(); out_11.close(); out_10.close();
			out_02.close(); out_01.close(); out_00.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	/**
	 * 
	 * @param nen1
	 * @param en
	 * @param thr1 unique letters
	 * @param thr2 same leteer in row
	 * @param thr3 transitions from lower to upper or number or symbol and vice versa 
	 * @return
	 */
	private static int checkStrangeStr(String nstr, String str, int thr1, int thr2, int thr3) {
		Set<String> chars = new HashSet<String>();
		int counter = 1;
		String b = "";
		boolean found = false;

		for (int ii=0;ii<nstr.length();ii++){
			Character temp = nstr.charAt(ii);
			String a  = Character.toString(temp);
			if (!chars.contains(a))
				chars.add(a);
			if (a.equals(b)){
				counter++;
				if (counter>thr2){
					found = true;
					break;
				}
			}else{
				b=a;
				counter=1;
			}
		}
		if (chars.size()<thr1){
			return 1;
		}
		if (found){
			return 2;
		}

		int counter1=0, counter2=0;
		String cur = "", oldcur="", tok;
		String[] toks = str.split(" ");
		int newthr = nstr.split(" ").length/3;
		for (int jj=0;jj<toks.length;jj++){
			tok = toks[jj];
			counter1=0;
			for (int ii=0;ii<tok.length();ii++){
				Character temp = tok.charAt(ii);
				if (Character.isDigit(temp))
					cur="d";
				if (Character.isUpperCase(temp))
					cur="u";
				if (Character.isLowerCase(temp))	
					cur="l";
				if (!Character.isLetterOrDigit(temp) && !Character.isWhitespace(temp))
					cur="s";
				if (ii>0 && !oldcur.equals(cur))
					counter1++;
				oldcur=cur;
			}
			if (counter1>thr3){
				counter2++;
				if (counter2>newthr){
					return 3;
				}
			}
		}

		return 0;
	}
	
	/**
	 * 
	 * @param filedata: the data file
	 * @param fileInds: the file with indeces of lines with score more than score
	 * @param score: the score lower limit from a line to be checked
	 * @param toberemovedstrs: manually detected strs with which lines of limited use start
	 * @param fileRemInds : the indeces of lines for which the score will be decreased 
	 * @throws IOException
	 */
	private static void getIndsManDetect(File filedata, File filemerge, 	double score, List<String> toberemovedstrs, File fileRemInds) throws IOException {

		int[] counters = new int[toberemovedstrs.size()];
		List<Integer> tobelowered = new ArrayList<Integer>();
		BufferedReader in;
		int  linecounter=-1;
		String inputLine, en;//, de;
		//List<Integer> toplevelinds = FilteringUtils.getIndsFromScore(new File("C:/Users/vpapa/ELRC/wmt18/data104m.merge.filt.scores.hun"), score);
		//List<Integer> toplevelinds = FilteringUtils.getIndsFromScore(filemerge, score);
		in = new BufferedReader(new FileReader(filedata));
		//List<String> entok = new ArrayList<String>();
		//List<String> detok = new ArrayList<String>();
		//boolean found = false;
		while ((inputLine = in.readLine()) != null) {
			linecounter++;
			//if (!toplevelinds.contains(linecounter))
			//	continue;
			String[] tt = inputLine.split("\t");
			en = tt[0].trim();
			//found = false;
			for (int ii=0;ii<toberemovedstrs.size();ii++){
				if (en.startsWith(toberemovedstrs.get(ii))){
					//if (!toplevelinds.contains(linecounter))
					//	continue;
					if (counters[ii]>2){
						LOGGER.info("\t"+linecounter +"\t" +inputLine);
						tobelowered.add(linecounter);
						//found = true;
						break;
					}else
						counters[ii]++;
				}
			}
			//if (found)
			//	continue;
			/*de = tt[1].trim();
			entok = Arrays.asList(ContentNormalizer.normtext(en).split(" "));
			detok = Arrays.asList(ContentNormalizer.normtext(de).split(" "));
			if ((entok.contains("january") && !detok.contains("januar")) ||  (!entok.contains("january") && detok.contains("januar"))){
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				tobelowered.add(linecounter);
				continue;
			}
			if ((entok.contains("february") && !detok.contains("februar")) || (!entok.contains("february") && detok.contains("februar"))){
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("march") && !detok.contains("märz")) ||(!entok.contains("march") && detok.contains("märz"))){
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("april") && !detok.contains("april")) ||(!entok.contains("april") && detok.contains("april")) ) {
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.equals("may") && !detok.contains("mai")) )//|| (!nen.equals("may") && nde.contains("mai"))) 
					return true;
			if ((entok.contains("june") && !detok.contains("juni")) || (!entok.contains("june") && detok.contains("juni"))){
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("july") && !detok.contains("juli")) || (!entok.contains("july") && detok.contains("juli"))) {
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("august") && !detok.contains("august")) || (!entok.contains("august") && detok.contains("august"))) {
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("september") && !detok.contains("september")) || (!entok.contains("september") && detok.contains("september"))){
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("october") && !detok.contains("oktober")) || (!entok.contains("october") && detok.contains("oktober"))) {
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("november") && !detok.contains("november")) || (!entok.contains("november") && detok.contains("november"))) {
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}
			if ((entok.contains("december") && !detok.contains("dezember")) || (!entok.contains("december") && detok.contains("dezember"))){
				tobelowered.add(linecounter);
				LOGGER.info("\t"+linecounter +"\t" +inputLine);
				continue;
			}*/

		}
		in.close();
		List<String> res = new ArrayList<String>();
		for (int ii=0;ii<tobelowered.size();ii++){
			res.add(Integer.toString(tobelowered.get(ii)));
		}
		//FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/wmt18/tobelowered1"), res, "\n");	
		FileUtils.writeLines(fileRemInds, Constants.UTF8, res, "\n");
	}

	/**
	 * decreasse scores from a list of indeces
	 * @param newscale 
	 * @param filescores: a list of scores 104m
	 * @param fileinds: a list of indeces to be decreased
	 * @param filenewscores: the new list of scores 104m
	 * @param oldscale: the scale to which the old scores have been mapped
	 * @param newscale: the scale to which the new scores will be mapped
	 * @throws IOException 
	 */
	private static void updateScoresFromIndeces(File filescores, File fileinds, File filenewscores,  int newscale) throws IOException {
		//List<Integer> inds = new ArrayList<Integer>();
		Set<Integer> indsset = new HashSet<Integer>();
		BufferedReader in;
		String inputLine;
		try {
			in = new BufferedReader(new FileReader(fileinds));
			while ((inputLine = in.readLine()) != null) {
				//inds.add(Integer.parseInt(inputLine.trim()));
				indsset.add(Integer.parseInt(inputLine.trim()));
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(inds.size()+"\t"+indsset.size());
		int linecounter = -1;
		double score;
		int counter = 0;
		double v = 0;
		int newscore=0;
		BufferedWriter out = new BufferedWriter(new FileWriter(filenewscores));
		try {
			in = new BufferedReader(new FileReader(filescores));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				score = Double.parseDouble(inputLine);
				if (score<newscale){
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (indsset.contains(linecounter)){
					//if (inds.contains(linecounter)){	
					if (score>15000)
						v = 15000;
					else{
						if (score>12500){
							v = 12500;
						}else{
							if (score>10000)
								v = 10000;
						}
					}
					score = score-v+newscale; //score = score-(int)scoreoldscale+newscale;
					out.write(Double.toString(score)+"\n");
					counter++;
				}else{
					if (score>15000)
						v = 15000;
					else{
						if (score>12500){
							v = 12500;
						}else{
							if (score>10000)
								v = 10000;
						}
					}
					score = score-v;
					if (score>1){
						newscore = (int)score;
						score = score-newscore;
					}
					score = score+v;
					out.write(Double.toString(score)+"\n");
				}
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(counter);
		Set<Integer> aa = new HashSet<Integer>();
	}
	
	/**
	 * generates a normailzed version of the coprus based on the scores that are over a threshold
	 * @param filedata filedata
	 * @param filescores scores
	 * @param filenormdata output
	 * @param thr threshold
	 * @param thrhigh 
	 * @throws IOException
	 */
	private static void getLevelCorpus(File filedata, File filescores, File filenormdata, int thrlow, int thrhigh) throws IOException {
		//File filedata = new File(args[0]);
		//File filemerge = new File(args[1]);
		//File filetop = new File("C:/Users/vpapa/ELRC/wmt18/data104m.merge.filt.scores.hun.topcorpus.norm");
		//File filetop = new File(args[2]);
		//int thr = Integer.parseInt(args[3]);
		//List<Integer> inds = FilteringUtils.getIndsFromScore(filemerge,15000);
		List<Double> inds = FilteringUtils.getFromScore(filescores,thrlow,thrhigh);
		//BufferedWriter out = new BufferedWriter(new FileWriter(filenormdata));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filenormdata.getAbsolutePath()), Constants.UTF8));
		BufferedReader in1;
		String inputLine1 ; 
		int linecounter1=-1;
		try {
			in1 = new BufferedReader(new FileReader(filedata));
			while ((inputLine1 = in1.readLine()) != null) {
				linecounter1++;
				if (inds.get(linecounter1)==1.0){
					//if (inds.contains(linecounter1)){
					String[] tt = inputLine1.split("\t");
					out.write(linecounter1 +"\t"+ContentNormalizer.normtext(tt[0])+"\t"+ ContentNormalizer.normtext(tt[1])+"\t"+ tt[2]+"\n");
				}
			}
			in1.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
	}

	/**
	 * examines a normalized and sorted version of the corpus and checkfor dups based on intersection of the set of ngrams and  
	 * @param filetop the normalized corpus
	 * @param filetobelowered
	 * @param startlimit
	 * @param endlimit
	 * @throws IOException
	 */
	private static void dedupDEtoplevelsort(File filetop, File filetobelowered, int startlimit, int endlimit) throws IOException {

		//File filetop = new File(args[0]); 
		//File filetobelowered = new File(args[1]);
		//int startlimit = Integer.parseInt(args[2]);
		//int endlimit = Integer.parseInt(args[3]);
		int lang =2, beam =11, limit;
		double THR = 0.7, ratio;
		List<Integer> tobelowered = new ArrayList<Integer>(); 
		String inputLine, sen1, sen2, norm1, norm2 ; //en, de,  nde;
		int linecounter=-1, linecounter2=-1, ind1, ind2, len;
		BufferedReader in, in2;
		List<String> toks1 = new ArrayList<String>();
		Set<String> grams1 = new HashSet<String>();
		List<String> toks2 = new ArrayList<String>();
		Set<String> grams2 = new HashSet<String>();
		boolean found = false, foundold=false;

		List<String> sens = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader(filetop));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter>= startlimit && linecounter<endlimit){
					sens.add(inputLine);
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int ii=0; ii<sens.size(); ii++){
			sen1  = sens.get(ii);
			String[] tt1 = sen1.split("\t");
			ind1 = Integer.parseInt(tt1[0]);
			if (tobelowered.contains(ind1))
				continue;
			norm1 = tt1[lang].trim();
			toks1 = FCStringUtils.getTokens(norm1);
			grams1 = getGrams(toks1);
			limit = Math.min(ii+beam, sens.size());
			foundold = false;
			for (int jj=ii+1; jj<limit; jj++){
				sen2 = sens.get(jj);
				String[] tt2 = sen2.split("\t");
				ind2 = Integer.parseInt(tt2[0]);
				if (tobelowered.contains(ind2))
					continue;
				norm2 = tt2[lang].trim();
				toks2 = FCStringUtils.getTokens(norm2);
				grams2 = getGrams(toks2);
				Set<String> intersection = new HashSet<String>(grams1);
				intersection.retainAll(grams2);
				ratio = (double)intersection.size()  / (double)Math.min(grams2.size(),grams1.size()) ;
				if (ratio<THR)
					continue;
				found = false;
				len = Math.min(toks2.size(), toks1.size());
				if (ratio >=0.95)
					found = true;
				else{
					if ( ratio> THR && len>6)
						found = true;
				}
				if (found){
					foundold = true;
					LOGGER.info(Double.toString(ratio));
					LOGGER.info(ii + "\t" + sen1);
					LOGGER.info(jj + "\t" + sen2);
					LOGGER.info("===============================");
					if (grams1.size()>=grams2.size()){
						LOGGER.info("passed:\t"+sen1);
						tobelowered.add(ind2);
					}else{
						LOGGER.info("passed:\t"+sen2);
						tobelowered.add(ind1);
						LOGGER.info("===============================");
						break;
					}
					LOGGER.info("===============================");
				}
				if (foundold && !found)
					break;
			}
		}
		FileUtils.writeLines(filetobelowered, Constants.UTF8, tobelowered, "\n");
	}

	private static void dedupDEtoplevel(String[] args) throws IOException {

		File filetop = new File(args[0]); 
		File filetobelowered = new File(args[1]);
		int startlimit = Integer.parseInt(args[2]);
		int endlimit = Integer.parseInt(args[3]);
		BufferedReader in;
		String inputLine;
		int linecounter = -1;
		List<String> sens = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader(filetop));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter>= startlimit && linecounter<endlimit){
					sens.add(inputLine);
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		String sen1, sen2;
		int lang =2, beam =10, limit;
		double THR = 0.7, ratio;
		List<Integer> tobelowered = new ArrayList<Integer>(); 
		String inputLine1, inputLine2, norm1, norm2 ; //en, de,  nde;
		int linecounter1=-1, linecounter2=-1, ind1, ind2, len;

		List<String> toks1 = new ArrayList<String>();
		Set<String> grams1 = new HashSet<String>();
		List<String> toks2 = new ArrayList<String>();
		Set<String> grams2 = new HashSet<String>();
		boolean found = false, foundold1=false;
		for (int ii=0; ii<sens.size(); ii++){
			sen1  = sens.get(ii);
			String[] tt1 = sen1.split("\t");
			ind1 = Integer.parseInt(tt1[0]);
			if (tobelowered.contains(ind1))
				continue;
			norm1 = tt1[lang].trim();
			toks1 = FCStringUtils.getTokens(norm1);
			grams1 = getGrams(toks1);
			limit = Math.min(ii+500, sens.size());
			//foundold = false;
			for (int jj=ii+1; jj<limit; jj++){
				//for (int jj=ii+1; jj<sens.size(); jj++){
				sen2 = sens.get(jj);
				String[] tt2 = sen2.split("\t");
				ind2 = Integer.parseInt(tt2[0]);
				if (tobelowered.contains(ind2))
					continue;
				norm2 = tt2[lang].trim();
				toks2 = FCStringUtils.getTokens(norm2);
				grams2 = getGrams(toks2);
				Set<String> intersection = new HashSet<String>(grams1);
				intersection.retainAll(grams2);
				ratio = (double)intersection.size()  / (double)Math.min(grams2.size(),grams1.size()) ;
				if (ratio<THR)
					continue;
				found = false;
				len = Math.min(toks2.size(), toks1.size());
				if (ratio >=0.95)
					found = true;
				else{
					if ( ratio> THR && len>6)
						found = true;
				}
				if (found){
					//foundold = true;
					LOGGER.info(Double.toString(ratio));
					LOGGER.info(ii + "\t" + sen1);
					LOGGER.info(jj + "\t" + sen2);
					LOGGER.info("===============================");
					if (grams1.size()>=grams2.size()){
						LOGGER.info("passed:\t"+sen1);
						tobelowered.add(ind2);
					}else{
						LOGGER.info("passed:\t"+sen2);
						tobelowered.add(ind1);
						LOGGER.info("===============================");
						break;
					}
					LOGGER.info("===============================");
				}
				//if (foundold && !found)
				//	break;
			}
		}
		FileUtils.writeLines(filetobelowered, Constants.UTF8, tobelowered, "\n");
	}

	private static Set<String> getVoc(File filedata, File filemerge, int col, String score, Set<String> voc) {
		List<Integer> indeces = new ArrayList<Integer>();
		String inputLine, temp;
		int linecounter = -1;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(filemerge));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals(score))
					indeces.add(linecounter);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		int wordcounter = 0;
		String ntemp;
		try {
			in = new BufferedReader(new FileReader(filedata));
			List<String> toks = new ArrayList<String>();
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (indeces.contains(linecounter)){
					temp = inputLine.split("\t")[col];
					ntemp = ContentNormalizer.normtext(temp);
					toks = FCStringUtils.getTokens(ntemp);
					wordcounter = wordcounter+ toks.size();
					for (String tok:toks){
						if (voc.contains(tok))
							continue;
						voc.add(tok);
					}
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("# of words in sentences with score "+ score + " = "+ wordcounter);
		System.out.println("# of unique words in sentences with score "+ score + " = "+ voc.size());
		return voc;
	}

	private static void getSubCorpus(File filedata, File filetok, File filerow,		File filemerge, File filesubcorpus) throws IOException {

		//File filemerge = new File("C:/tmp/merge");
		//mergeFilts( filetok,  filerow, findhand, filemerge);
		BufferedReader in;
		List<Integer> indeces = new ArrayList<Integer>();
		String inputLine, temp;
		int linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(filemerge));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals("3"))
					indeces.add(linecounter);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		//BufferedWriter out = new BufferedWriter(new FileWriter(filesubcorpus));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filesubcorpus.getAbsolutePath()), Constants.UTF8));
				
		try {
			in = new BufferedReader(new FileReader(filedata));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (linecounter>indeces.size())
					break;
				if (indeces.contains(linecounter))
					out.write(inputLine+"\n");
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void counting(File file) throws IOException {
		BufferedReader in;
		//BufferedWriter out = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath()+".len")));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()+".len"), Constants.UTF8));
		
		String inputLine="", en, de, nen, nde, temp;
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(file));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals("0") || temp.equals("1")){
					out.write(linecounter+"\t0\t0\t0");
					continue;
				}
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();	nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();	nde = ContentNormalizer.normtext(de);
				int enlen= FCStringUtils.countTokens(nen);  
				int delen = FCStringUtils.countTokens(nde);  
				int t = enlen+delen;
				out.write(linecounter+"\t"+Integer.toString(enlen)+"\t"+Integer.toString(delen)+ Integer.toString(t));				
			}	
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void rescoring(String[] args) throws IOException {
		File datafilt = new File(args[0]+".filt");
		File datafiltscores = new File(args[0]+".filt.scores");
		File datafiltscores1 = new File(args[0]+".filt.scores.1");

		String inputLine, en, de, nen, nde, nen1, nde1;
		BufferedReader in;
		int  linecounter=-1;
		double score;
		List<Double> scores = new ArrayList<Double>();
		try {
			in = new BufferedReader(new FileReader(datafiltscores));
			while ((inputLine = in.readLine()) != null) {
				//linecounter++;
				score = Double.parseDouble(inputLine.trim());
				scores.add(score);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			in = new BufferedReader(new FileReader(datafilt));
			int encount = 0, decount=0;

			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				score = scores.get(linecounter); 
				if (score==2){
					String[] tt = inputLine.split("\t");
					en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
					de = tt[1].trim();				nde = ContentNormalizer.normtext(de);

					if (((float)nen.length()/(float)en.length())   <0.5 
							|| ((float)nde.length()/(float)de.length())<0.5){
						//LOGGER.info(linecounter+"\t"+inputLine);
						LOGGER.info(linecounter+"\tfew-letters05:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}

					if (nen.equals("customer ratings") || nde.endsWith("customer ratings")
							|| nen.equals("cut")
							|| nen.startsWith("gebote") || nen.endsWith("gebote")
							|| nen.startsWith("folg")
							|| nen.startsWith("grannkillar")
							|| nen.startsWith("guy next door")
							|| nen.startsWith("guy next door")
							|| inputLine.contains("0 (beginner)")
							|| inputLine.contains("0 Be the first!")
							|| nen.equals("bids")
							|| nen.equals("bids eur")
							|| nen.startsWith("0 article")
							|| nen.startsWith("0 artículo")
							|| nen.equals("comment")
							|| (nen.startsWith("comment") && !en.startsWith("Comment:"))
							|| nen.equals("comments from users comments listed")
							|| nen.equals("antworten") || nde.equals("antworten")
							|| nen.contains("anfänger") || nde.equals("anfänger")
							|| (inputLine.contains("Coins/min") && !nen.equals("LadySarah") && !nde.equals("LadySarah"))
							|| inputLine.contains("ArtikelGesamt")	
							//|| inputLine.contains("pdf")
							|| (nen.contains("pdf") && !nde.contains("pdf"))
							|| (!nen.contains("pdf") && nde.contains("pdf"))
							){
						LOGGER.info(linecounter+"\tspecial-cases:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}
					if (inputLine.contains("pdf")){
						String num1=en.replaceAll("\\D+","");
						String num2=de.replaceAll("\\D+","");
						if (num1.isEmpty() || num2.isEmpty()){
							LOGGER.info(linecounter+"\tspecial-cases:\t"+inputLine);
							scores.set(linecounter, 0.0);
							continue;
						}
						if (!num1.equals(num2)){
							LOGGER.info(linecounter+"\tspecial-cases:\t"+inputLine);
							scores.set(linecounter, 0.0);
							continue;
						}
					}

					encount = symbolInString(en, "|");
					decount = symbolInString(de, "|");
					if (Math.abs(encount-decount)>1){
						LOGGER.info(linecounter+"\tnumof|:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}
					if (  (en.startsWith("00") && !de.startsWith("00"))
							|| (!en.startsWith("00") && de.startsWith("00"))
							|| (en.startsWith("0%") && !de.startsWith("0%"))
							|| (!en.startsWith("0%") && de.startsWith("0%"))
							|| (en.startsWith("0.") && !de.startsWith("0."))
							|| (!en.startsWith("0.") && de.startsWith("0."))
							|| (en.startsWith("€0") && !de.contains("€"))
							//|| (en.startsWith("["))
							|| (!en.startsWith("+0") && de.startsWith("+0"))
							|| (!en.startsWith("0 0") && de.startsWith("0 0"))
							|| (!en.startsWith("0-100") && de.startsWith("0-100"))
							|| (!en.startsWith("- 0-") && de.startsWith("- 0-"))){
						LOGGER.info(linecounter+"\twrong-heur:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}

					/*nen = ContentNormalizer.normtext(en);
					nde = ContentNormalizer.normtext(de);
					if (checkmonths(nen,nde)){
						LOGGER.info(linecounter+"\t"+inputLine);
						scores.set(linecounter, score-0.1);
					}*/
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileUtils.writeLines(datafiltscores1, Constants.UTF8, scores, "\n");
	}

	private static int symbolInString(String str, String symbol) {
		int counter = 0;
		for (int ii=0;ii<str.length();ii++){
			String a =Character.toString(str.charAt(ii));
			if (a.equals(symbol)){
				counter++;
			}
		}	
		return counter;
	}

	private static void checkmonths1(String[] args) {
		File datafile = new File(args[0]);
		String inputLine, en, de, nen, nde, nen1, nde1;
		BufferedReader in;
		int  linecounter=-1;


		try {
			in = new BufferedReader(new FileReader(datafile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
				if (checkmonths(nen,nde))
					LOGGER.info(linecounter+"\t"+inputLine);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkmonths(String nen, String nde) {
		if ((nen.contains("january") && !nde.contains("januar")) ||  (!nen.contains("january") && nde.contains("januar")))
			return true;
		if ((nen.contains("february") && !nde.contains("februar")) || (!nen.contains("february") && nde.contains("februar")))
			return true;
		if ((nen.contains("march") && !nde.contains("märz")) ||(!nen.contains("march") && nde.contains("märz")))
			return true;
		if ((nen.contains("april") && !nde.contains("april")) ||(!nen.contains("april") && nde.contains("april")) ) 
			return true;
		if ((nen.equals("may") && !nde.contains("mai")) )//|| (!nen.equals("may") && nde.contains("mai"))) 
			return true;
		if ((nen.contains("june") && !nde.contains("juni")) || (!nen.contains("june") && nde.contains("juni"))) 
			return true;
		if ((nen.contains("july") && !nde.contains("juli")) || (!nen.contains("july") && nde.contains("juli"))) 
			return true;
		if ((nen.contains("august") && !nde.contains("august")) || (!nen.contains("august") && nde.contains("august"))) 
			return true;
		if ((nen.contains("september") && !nde.contains("september")) || (!nen.contains("september") && nde.contains("september"))) 
			return true;
		if ((nen.contains("october") && !nde.contains("oktober")) || (!nen.contains("october") && nde.contains("oktober"))) 
			return true;
		if ((nen.contains("november") && !nde.contains("november")) || (!nen.contains("november") && nde.contains("november"))) 
			return true;
		if ((nen.contains("december") && !nde.contains("dezember")) || (!nen.contains("december") && nde.contains("dezember")))
			return true;

		return false;
	}

	private static void normalizing(String[] args) throws IOException {
		File newdata = new File(args[0]+".filt.norm");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()), Constants.UTF8));
		String inputLine, en, de, nen, nde ;
		int linecounter=-1;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(args[0]+".filt"));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (inputLine.equals("0")){
					outnewdata.write("0\n");
					continue;
				}
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
				String temp = nen+nde;
				outnewdata.write(linecounter+"\t"+temp+"\t"+en.length()+"\t"+tt[2].trim()+"\n");
			}
			in.close();
			outnewdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static boolean drop(String inputLine, String en, String de, int linecounter){
		boolean found;
		boolean res=true;

		if (en.matches(".*[\\u0530-\\u058F].*") || de.matches(".*[\\u0530-\\u058F].*")){
			LOGGER.info(linecounter +"\tarmenian:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0900-\\u097F].*") || de.matches(".*[\\u0900-\\u097F].*")){
			LOGGER.info(linecounter +"\thindi:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0400-\\u04FF].*") || de.matches(".*[\\u0400-\\u04FF].*")){
			LOGGER.info(linecounter +"\tcyrillic:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u10A0-\\u10FF].*") || de.matches(".*[\\u10A0-\\u10FF].*")){
			LOGGER.info(linecounter +"\tgeorgian:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
			LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
			LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
			LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
			LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0590-\\u05FF].*") || de.matches(".*[\\u0590-\\u05FF].*")){
			LOGGER.info(linecounter +"\tHebrew:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u1100-\\u11FF].*") || de.matches(".*[\\u1100-\\u11FF].*")
				|| en.matches(".*[\\uAC00-\\uD7AF].*") || de.matches(".*[\\uAC00-\\uD7AF].*")){
			LOGGER.info(linecounter +"\tHangul Jamo:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u3040-\\u309F].*") || de.matches(".*[\\u3040-\\u309F].*")){
			LOGGER.info(linecounter +"\tHiragana:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u30A0-\\u30FF].*") || de.matches(".*[\\u30A0-\\u30FF].*")){
			LOGGER.info(linecounter +"\tKatakana:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0370-\\u03FF].*") || de.matches(".*[\\u0370-\\u03FF].*")){
			LOGGER.info(linecounter +"\tGreek:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0E00-\\u0E7F].*") || de.matches(".*[\\u0E00-\\u0E7F].*")){
			LOGGER.info(linecounter +"\tThai:\t"+ inputLine);
			return false;
		}
		for (int ii=0;ii<exclude_grams.length;ii++){
			if (inputLine.contains(exclude_grams[ii])){
				LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
				return false;
			}
		}

		found = false;
		boolean foundsame= false;
		int counter = 0, countersame=0;
		//Set<String> nenchars = new HashSet<String>();
		String b = "";

		for (int ii=0;ii<en.length();ii++){
			String a =Character.toString(en.charAt(ii));
			for (int jj=0;jj<exclude_symbols.length;jj++){
				if (a.equals(exclude_symbols[jj]))
					counter++;
				if (counter>1){
					found = true;
					break;
				}
			}
			//if (!nenchars.contains(a))
			//	nenchars.add(a);
			/*if (a.equals(b)){
				countersame++;
				if (countersame>4){
					foundsame=true;
					break;
				}
			}else{
				b=a;
				countersame=1;
			}*/
		}
		//if (nenchars.size()<3){
		//	LOGGER.info(linecounter +"\tsame-letters5:\t"+inputLine);
		//	return false;
		//}
		if (foundsame){
			LOGGER.info(linecounter +"\tsame-letters5:\t"+inputLine);
			return false;
		}

		/*
		for (int ii=0;ii<exclude_symbols.length;ii++){
			if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
				counter++;
				if (counter>2){
					found = true;
					break;
				}
			}
		}*/

		if (found){
			LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
			return false;
		}
		counter = 0;
		countersame=0;
		for (int ii=0;ii<de.length();ii++){
			String a =Character.toString(de.charAt(ii));
			for (int jj=0;jj<exclude_symbols.length;jj++){
				if (a.equals(exclude_symbols[jj]))
					counter++;
				if (counter>1){
					found = true;
					break;
				}
				/*if (a.equals(b)){
					countersame++;
					if (countersame>4){
						foundsame=true;
						break;
					}
				}else{
					b=a;
					countersame=1;
				}*/
			}
		}
		if (foundsame){
			LOGGER.info(linecounter +"\tsame-letters5:\t"+inputLine);
			return false;
		}

		if (found){
			LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
			return false;
		}

		if ((en.contains("??") && !de.contains("??")) || (!en.contains("??") && de.contains("??"))) {
			LOGGER.info(linecounter +"\tspecial symbols??:\t"+inputLine);
			return false;
		}

		return res;
	}

	private static void scoring_filter(String[] args) throws IOException {
		String[] langs = new String[2];
		langs[0]= "eng";
		langs[1]= "deu";

		LangDetector langDetector = LangDetectUtils.loadLangDetectors(langs,"langdetect");

		SentenceSplitterFactory sentencesplitterFactory = new SentenceSplitterFactory();
		SentenceSplitter enss = sentencesplitterFactory.getSentenceSplitter(langs[0]);
		SentenceSplitter dess = sentencesplitterFactory.getSentenceSplitter(langs[1]);

		File datafile = new File(args[0]);

		File newdata = new File(datafile.getAbsolutePath()+"."+args[1]+".filt");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()), Constants.UTF8));
		String inputLine, en, de, nen, nde, nen1, nde1;
		double hunscore;
		BufferedReader in;
		//List<String> scores= new ArrayList<String>();
		List<Double> scores= new ArrayList<Double>();
		int  linecounter=-1;
		int milcounter=1;
		boolean found = false;
		HashMap<String, int[]> map = new  HashMap<String, int[]>();
		//HashMap<String, Integer> enpart = new HashMap<String, Integer>();
		HashMap<String, double[]> enpart = new HashMap<String, double[]>();
		try {
			in = new BufferedReader(new FileReader(datafile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter!=(scores.size())){
					System.out.println("ooops");
				}

				if ((linecounter / 10000000)>milcounter){
					FileUtils.writeLines(new File(args[0]+"."+args[1]+".filt.scores"), Constants.UTF8, scores, "\n");
					milcounter++;
				}
				//if (linecounter==176235 || linecounter==157059){
				//	System.out.println("dsddssd");
				//}

				if (inputLine.startsWith("ð")){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n"); 	LOGGER.info(linecounter+"\tcorrupted:\t"+inputLine);
					continue;
				}
				if (inputLine.startsWith("HHH")){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n"); 	LOGGER.info(linecounter+"\tcorrupted:\t"+inputLine);
					continue;
				}

				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
				String num1=en.replaceAll("\\D+","");
				String num2=de.replaceAll("\\D+","");

				int enlen= FCStringUtils.countTokens(en);  //FCStringUtils.countTokens(en);
				int delen = FCStringUtils.countTokens(de);  //FCStringUtils.countTokens(de);
				if (enlen>80 || delen>80){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n"); 	LOGGER.info(linecounter+"\tMosesLen80:\t"+inputLine);
					continue;
				}

				if (enlen>9*delen || delen>9*enlen){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tMosesRa:\t"+inputLine);
					continue;
				}

				if (enlen<3 || delen<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n"); 	LOGGER.info(linecounter+"\twords3:\t"+inputLine);
					continue;
				}

				if (nen.isEmpty() || nde.isEmpty()){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tno-letters:\t"+inputLine);
					continue;
				}

				nen1 = ContentNormalizer.normENDEtext(en);
				nde1 = ContentNormalizer.normENDEtext(de);
				hunscore = Double.parseDouble(tt[2].trim());

				if (nen1.isEmpty() || nde1.isEmpty()){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tno-letters:\t"+inputLine);
					continue;
				}

				if (nen.equals(nde)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tequal-TUVs:\t"+inputLine);
					continue;
				}
				if (nen.length()<4 || nde.length()<4){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tno-letters3:\t"+inputLine);
					continue;
				}
				if (connected(nen)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tconnected:\t"+inputLine);
					continue;
				}
				if (connected(nde)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tconnected:\t"+inputLine);
					continue;
				}
				if (wrongmonths(nen, nde)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\twrong:\t"+inputLine);
					continue;
				}
				if (wrongtrans(inputLine, en, de, nen, nde)){ 
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\twrong-heur:\t"+inputLine);
					continue;
				}		


				/*if (!drop1(inputLine, en, de, nen, nde, linecounter)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");
					continue;
				}*/


				Set<String> nenchars = new HashSet<String>();
				int counter = 1;
				String b = "";
				found = false;
				for (int ii=0;ii<nen.length();ii++){
					String a  = Character.toString(nen.charAt(ii));
					if (!nenchars.contains(a))
						nenchars.add(a);
					if (a.equals(b)){
						counter++;
						if (counter>4){
							found = true;
							break;
						}
					}else{
						b=a;
						counter=1;
					}
				}
				if (nenchars.size()<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tno-letters2:\t"+inputLine);
					continue;
				}
				if (found){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tsame-letters5:\t"+inputLine);
					continue;
				}

				counter = 1;
				b = "";
				//found =false;
				Set<String> ndechars = new HashSet<String>();
				for (int ii=0;ii<nde.length();ii++){
					String a  = Character.toString(nde.charAt(ii));
					if (!ndechars.contains(a))
						ndechars.add(a);
					if (a.equals(b)){
						counter++;
						if (counter>4){
							found = true;
							break;
						}
					}else{
						b=a;
						counter=1;
					}
				}
				if (ndechars.size()<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tno-letters2:\t"+inputLine);
					continue;
				}

				if (found){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tsame-letters5:\t"+inputLine);
					continue;
				}

				if (!drop(inputLine, en, de, linecounter)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");
					continue;
				}

				if (((float)nen.length()/(float)en.length())<0.3){
					if (!num1.isEmpty() && !num1.equals(num2)){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter+"\tfew-letters03:\t"+inputLine);
						continue;
					}
				}
				if (TMXHandlerUtils.checkemail(en, 0.5) || TMXHandlerUtils.checkemail(de, 0.5)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					continue;
				}
				if (TMXHandlerUtils.checkurl(en, 0.5) || TMXHandlerUtils.checkurl(de, 0.5)){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter +"\turl:\t"+ inputLine);
					continue;
				}

				enlen= FCStringUtils.countTokens(nen);  //FCStringUtils.countTokens(en);
				delen = FCStringUtils.countTokens(nde);  //FCStringUtils.countTokens(de);
				if (enlen<3 || delen<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tLen3:\t"+inputLine);
					continue;
				}

				if (enlen>8){
					String lang1 = langDetector.detect(en);
					if (!lang1.equals(langs[0])){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter+"\tnot-English8:\t"+inputLine);
						continue;
					}
				}
				if (delen>8){
					String lang1 = langDetector.detect(de);
					if (!lang1.equals(langs[1])){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter+"\tnot-German8:\t"+inputLine);
						continue;
					}
				}

				List<String> entoks = FCStringUtils.getTokens(nen);
				Double[] entokenslen = FCStringUtils.getTokensLength(entoks);
				if (Statistics.getMax(entokenslen)>40){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");		LOGGER.info(linecounter+"\tlong-words in norm:\t"+inputLine);
					continue;
				}
				if (Statistics.getMedian(entokenslen)<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter + "\tshort median in norm:\t"+ inputLine);
					continue;
				}
				List<String> detoks = FCStringUtils.getTokens(nde);
				Double[] detokenslen = FCStringUtils.getTokensLength(detoks);
				if (Statistics.getMax(detokenslen)>45){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tlong-words in norm:\t"+inputLine);
					continue;
				}
				if (Statistics.getMedian(detokenslen)<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter + "\tshort median in norm:\t"+ inputLine);
					continue;
				}

				if (entoks.size()>1){
					if (Statistics.getMedian(entokenslen)>=25 ){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter + "\tlong median in norm:\t"+ inputLine);
						continue;
					}
				}

				if (detoks.size()>2){
					if (Statistics.getMedian(detokenslen)>=25 ){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter + "\tlong median in norm:\t"+ inputLine);
						continue;
					}
				}

				if (enlen>2){
					String lang1 = langDetector.detect(en);
					if (!lang1.equals(langs[0])){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter+"\tnot-English2:\t"+inputLine);
						continue;
					}
				}
				if (delen>2){
					String lang1 = langDetector.detect(de);
					if (!lang1.equals(langs[1])){
						scores.add(0.0); //scores.add(" 0 ");
						outnewdata.write("0\n");	LOGGER.info(linecounter+"\tnot-German2:\t"+inputLine);
						continue;
					}
				}

				Set<String> toks = new HashSet<String>();
				for (String tok:entoks){
					toks.add(tok);
				}
				if ((double)toks.size()/(double)entoks.size()<0.4){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\trepeats:\t"+inputLine);
					continue;
				}
				toks.clear();
				for (String tok:detoks){
					toks.add(tok);
				}
				if ((double)toks.size()/(double)detoks.size()<0.4){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\trepeats:\t"+inputLine);
					continue;
				}

				if (nearTUVs(detoks, entoks, 0.67) || nearTUVs(entoks, detoks, 0.67)){ //on TU level
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\tnearTUVs:\t"+inputLine);
					continue;
				}

				if (hunscore<0.01 && delen>1 && enlen>1){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write("0\n");	LOGGER.info(linecounter+"\thunaling:\t"+inputLine);
					continue;
				}

				List<String> ensens = enss.getSentences(en, 1);
				List<String> desens = dess.getSentences(de, 1);
				found = false;
				int foundline = 0;
				double oldscore = 0;
				double[] temp = new double[2];
				if (enpart.containsKey(nen)){
					found = true;
					//foundline = enpart.get(nen);
					temp = enpart.get(nen);
					foundline = (int)temp[0];
					oldscore = scores.get(foundline);
					LOGGER.info(linecounter+"\tdupEN:\t"+inputLine);
					System.out.println("---");
				}
				if (ensens.size()>desens.size()){
					if (!(num1.isEmpty() && num2.isEmpty())){
						if (!num1.equals(num2)){
							scores.add(1.0); //scores.add(" 1 ");
							outnewdata.write("1\n");
							LOGGER.info(linecounter+"\tmore2less:\t"+inputLine);
							continue;
						}
					}else{
						if (found){
							if (oldscore>=2.2){
								scores.add(1.0); //scores.add(" 2.05 ");
								continue;
							}else{
								if (oldscore<2.1){
									scores.add(2.1);
									//enpart.put(nen, linecounter);
									scores.set((int)temp[0], 1.0);
									temp[0] = (double)linecounter;
									temp[1] = Double.parseDouble(tt[2]);
									enpart.put(nen, temp);
									System.out.println("passed");
									continue;
								}else{
									scores.add(1.0); //scores.add(" 2.05 ");
									continue;
								}
							}
						}else{
							scores.add(2.1); //scores.add(" 2.1 ");
							//enpart.put(nen, linecounter);
							temp[0] = (double)linecounter;
							temp[1] = Double.parseDouble(tt[2]);
							enpart.put(nen, temp);
						}
						outnewdata.write("1\n");
						LOGGER.info(linecounter+"\tmore2less:\t"+inputLine);
						continue;
					}
				}

				if (ensens.size()<desens.size()){
					if (!(num1.isEmpty() && num2.isEmpty())){
						if (!num1.equals(num2)){
							scores.add(1.0); //scores.add(" 1 ");
							outnewdata.write("1\n");
							LOGGER.info(linecounter+"\tless2more:\t"+inputLine);
							continue;
						}
					}else{
						if (found){
							if (oldscore>=2.2){
								scores.add(1.0); //scores.add(" 2.05 ");
								continue;
							}else{
								if (oldscore<2.1){
									scores.add(2.1);
									//enpart.put(nen, linecounter);
									scores.set((int)temp[0], 1.0);
									temp[0] = (double)linecounter;
									temp[1] = Double.parseDouble(tt[2]);
									enpart.put(nen, temp);
									System.out.println("passed");
									continue;
								}else{
									scores.add(1.0); //scores.add(" 2.05 ");
									continue;
								}
							}
						}else{
							scores.add(2.1); //scores.add(" 2.1 ");
							//enpart.put(nen, linecounter);
							temp[0] = (double)linecounter;
							temp[1] = Double.parseDouble(tt[2]);
							enpart.put(nen, temp);
						}
						outnewdata.write("1\n");
						LOGGER.info(linecounter+"\tless2more:\t"+inputLine);
						continue;
					}
				}

				byte[] texthashkey = MD5Hash.digest(nen+nde).getDigest();
				String key="";
				for (int jj=0;jj<texthashkey.length;jj++) {
					key += texthashkey[jj];
				}
				int[] props = new int[3];
				int nums=0;
				if (num1.equals(num2))
					nums=1;
				int len = (nen+nde).length();
				if (map.containsKey(key)){
					props =map.get(key);
					if (props[2]==nums){
						if (props[0]<=len){
							scores.add(0.0); //scores.add(" 0 ");
							outnewdata.write("0\n");	LOGGER.info(linecounter+"\tdup:\t"+inputLine);
							continue;
						}else{
							scores.set(props[1],0.0); //scores.set(props[1]," 0 ");
						}
					}else{
						if (props[2]==1){
							scores.add(0.0); //scores.add(" 0 ");
							outnewdata.write("0\n");	LOGGER.info(linecounter+"\tdup:\t"+inputLine);
							continue;
						}else{
							scores.set(props[1],0.0); //scores.set(props[1],"0");
						}
					}
				}

				if (num1.equals(num2)){
					if (!num1.isEmpty()){
						if (found){
							if (oldscore==3){
								scores.add(1.0); //scores.add(" 2.05 ");
								continue;
							}else{
								scores.add(3.0); //scores.add(" 3 ");
								//enpart.put(nen, linecounter);
								scores.set((int)temp[0], 1.0);
								temp[0] = (double)linecounter;
								temp[1] = Double.parseDouble(tt[2]);
								enpart.put(nen, temp);
								System.out.println("passed");
								continue;
							}
						}else{
							scores.add(3.0); //scores.add(" 3 ");
							//enpart.put(nen, linecounter);
							temp[0] = (double)linecounter;
							temp[1] = Double.parseDouble(tt[2]);
							enpart.put(nen, temp);
						}
					}else{
						double ra =(double)nen.length() /(double)nde.length(); 
						double curscore = 0;
						if (ra>=0.8 && ra<=1.2)
							curscore=2.4;
						if ((ra>0.55 && ra<0.8) || (ra>1.2 && ra<=1.45))
							curscore=2.3; //scores.add(" 2.2 ");
						if ((ra>0.4 && ra<=0.55) || (ra>1.45 && ra<1.7))			//if (ra<0.55 || ra>=1.45)
							curscore=2.19; 
						if (ra<=0.4 || ra>=1.7)
							curscore=0.0;
						if (found){
							if (oldscore>=curscore){	
								scores.add(1.0); //scores.add(" 2.05 ");
								continue;
							}else{
								//if (Double.parseDouble(tt[2])>0.8){
								temp = enpart.get(nen);
								if (Double.parseDouble(tt[2])>(temp[1]+0.1)){	
									scores.add(curscore);
									scores.set((int)temp[0], 1.0);
									//enpart.put(nen, linecounter);
									temp[0] = (double)linecounter;
									temp[1] = Double.parseDouble(tt[2]);
									enpart.put(nen, temp);
									System.out.println("passed");
									continue;
								}else{
									scores.add(1.0); //scores.add(" 2.05 ");
									continue;
								}
							}
						}else{
							//double ra =(double)nen.length() /(double)nde.length(); 
							if (ra>=0.8 && ra<=1.2)
								scores.add(2.4);
							if ((ra>0.55 && ra<0.8) || (ra>1.2 && ra<=1.45))
								scores.add(2.3); //scores.add(" 2.2 ");
							if ((ra>0.4 && ra<=0.55) || (ra>1.45 && ra<1.7))			//if (ra<0.55 || ra>=1.45)
								scores.add(2.19); 
							if (ra<=0.4 || ra>=1.7){
								scores.add(0.0);
								outnewdata.write("0\n"); LOGGER.info(linecounter+"\tratio:\t"+inputLine);
								continue;
							}
							//enpart.put(nen, linecounter);
							temp[0] = (double)linecounter;
							temp[1] = Double.parseDouble(tt[2]);
							enpart.put(nen, temp);
						}
					}
				}else{
					if (found){
						if (oldscore>2.0){
							scores.add(0.0);
							continue;
						}else{
							temp = enpart.get(nen);
							if (Double.parseDouble(tt[2])>(temp[1]+0.1)){
								if (Double.parseDouble(tt[2])>4)
									scores.add(2.4);
								else{
									scores.add(2.0);
								}
								scores.set((int)temp[0], 1.0);
								//enpart.put(nen, linecounter);
								temp[0] = (double)linecounter;
								temp[1] = Double.parseDouble(tt[2]);
								enpart.put(nen, temp);
								System.out.println("passed");
								continue;
							}else{
								scores.add(1.0); //scores.add(" 2.05 ");
								continue;
							}
						}
					}

					scores.add(2.0); 
					//enpart.put(nen, linecounter);
					temp[0] = (double)linecounter;
					temp[1] = Double.parseDouble(tt[2]);
					enpart.put(nen, temp);
				}
				props[0] = len;
				props[1] = linecounter;
				props[2] = nums;
				map.put(key, props);
				if (linecounter!=(scores.size()-1)){
					System.out.println("ooops");
				}

				//scores.add(" 1 ");
				outnewdata.write(inputLine+"\n");
			}
			in.close();
			outnewdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileUtils.writeLines(new File(args[0]+"."+args[1]+".filt.scores"), Constants.UTF8, scores, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean drop1(String inputLine, String en, String de,String nen, String nde, int linecounter) {
		boolean foundsame= false, foundProb = false, found = false;
		boolean res=true;

		for (int ii=0;ii<exclude_grams1.length;ii++){
			if (inputLine.contains(exclude_grams1[ii])){
				LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
				return true;
			}
		}

		Set<String> nenchars = new HashSet<String>();
		int counter = 1, counterProb=0;
		String b = "";
		for (int ii=0;ii<nen.length();ii++){
			String a  = Character.toString(nen.charAt(ii));
			if (inStrangeLang(a, linecounter, inputLine)){
				return false;
			}
			if (!nenchars.contains(a))
				nenchars.add(a);
			if (a.equals(b)){
				counter++;
				if (counter>4){
					foundsame = true;
					break;
				}
			}else{
				b=a;
				counter=1;
			}
			for (int jj=0;jj<exclude_symbols1.length;jj++){
				if (a.equals(exclude_symbols1[jj]))
					counterProb++;
				if (counterProb>1){
					foundProb = true;
					break;
				}
			}
			if (foundProb){
				found = true;
				break;
			}
		}
		if (foundsame){
			LOGGER.info(linecounter+"\tsame-letters5:\t"+inputLine);			return false;
		}
		if (found){
			LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);		return false;
		}
		if (nenchars.size()<3){
			LOGGER.info(linecounter+"\tno-letters2:\t"+inputLine);				return false;
		}	
		return res;
	}

	private static boolean inStrangeLang(String en, int linecounter, String inputLine) {
		boolean res = false;

		if (en.matches(".*[\\u0530-\\u058F].*")){
			LOGGER.info(linecounter +"\tarmenian:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0900-\\u097F].*")){
			LOGGER.info(linecounter +"\thindi:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0400-\\u04FF].*")){
			LOGGER.info(linecounter +"\tcyrillic:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u10A0-\\u10FF].*")){
			LOGGER.info(linecounter +"\tgeorgian:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u4E00-\\u9FFF].*")){
			LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0600-\\u06FF].*")){ 
			LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0980-\\u09FF].*")){ 
			LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0C80-\\u0CFF].*")){
			LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0590-\\u05FF].*")){
			LOGGER.info(linecounter +"\tHebrew:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u1100-\\u11FF].*")	|| en.matches(".*[\\uAC00-\\uD7AF].*")){
			LOGGER.info(linecounter +"\tHangul Jamo:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u3040-\\u309F].*")){
			LOGGER.info(linecounter +"\tHiragana:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u30A0-\\u30FF].*")){
			LOGGER.info(linecounter +"\tKatakana:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0370-\\u03FF].*")){
			LOGGER.info(linecounter +"\tGreek:\t"+ inputLine);
			return true;
		}
		if (en.matches(".*[\\u0E00-\\u0E7F].*")){
			LOGGER.info(linecounter +"\tThai:\t"+ inputLine);
			return true;
		}
		return res;
	}

	private static boolean wrongtrans(String inputLine, String en, String de, String nen, String nde) {
		boolean found =false;
		if (en.contains("incScript") || de.contains("incScript")
				|| en.contains("includeLibs") || de.contains("includeLibs")	
				|| en.contains("RFUNC") || de.contains("RFUNC")
				|| en.contains(" but =")
				|| (en.contains("fputs(") && !de.contains("fputs("))
				|| (en.contains("fwrite(") && !de.contains("fwrite("))
				|| (en.contains("fclose(") && !de.contains("fclose("))
				|| (en.contains("fopen(") && !de.contains("fopen("))
				){
			return true;
		}

		if (nen.equals("customer ratings") || nde.endsWith("customer ratings")
				|| nen.equals("cut")
				|| nen.startsWith("gebote") || nen.endsWith("gebote")
				|| nen.startsWith("folg")
				|| nen.startsWith("grannkillar")
				|| nen.startsWith("guy next door")
				|| nen.startsWith("guy next door")
				|| inputLine.contains("0 (beginner)")
				|| inputLine.contains("0 Be the first!")
				|| nen.equals("bids")
				|| nen.equals("bids eur")
				|| nen.startsWith("0 article")
				|| nen.startsWith("0 artículo")
				|| nen.equals("comment")
				|| (nen.startsWith("comment ") && !en.startsWith("Comment:"))
				|| nen.equals("comments from users comments listed")
				|| nen.equals("antworten") || nde.equals("antworten")
				|| nen.contains("anfänger") || nde.equals("anfänger")
				|| (inputLine.contains("Coins/min") && !nen.equals("LadySarah") && !nde.equals("LadySarah"))
				|| inputLine.contains("ArtikelGesamt")	
				//|| inputLine.contains("pdf")
				//|| (nen.contains("pdf") && !nde.contains("pdf"))
				//|| (!nen.contains("pdf") && nde.contains("pdf"))
				|| nen.startsWith("byjoern")
				|| nen.contains("jquery")
				|| nde.contains("jquery")
				|| nen.startsWith("pinyin")
				|| nde.startsWith("pinyin")			
				|| nen.startsWith("bymanniLo")
				|| nen.startsWith("byThym1959")
				|| nen.startsWith("premier")
				){
			return true;
		}

		int encount = symbolInString(en, "|");
		int decount = symbolInString(de, "|");
		if (Math.abs(encount-decount)>2){
			return true;
		}


		if (  (en.startsWith("00") && !de.startsWith("00"))
				|| (!en.startsWith("00") && de.startsWith("00"))
				|| (en.startsWith("0%") && !de.startsWith("0%"))
				|| (!en.startsWith("0%") && de.startsWith("0%"))
				|| (en.startsWith("0.") && !de.startsWith("0."))
				|| (!en.startsWith("0.") && de.startsWith("0."))
				|| (en.startsWith("€0") && !de.contains("€"))
				//|| (en.startsWith("["))
				|| (!en.startsWith("+0") && de.startsWith("+0"))
				|| (!en.startsWith("0 0") && de.startsWith("0 0"))
				|| (!en.startsWith("0-100") && de.startsWith("0-100"))
				|| (!en.startsWith("- 0-") && de.startsWith("- 0-"))){
			return true;
		}
		return found;
	}

	private static boolean wrongmonths(String nen, String nde) {
		if (nen.equals("january") && !nde.equals("januar"))
			return true;
		if (nen.equals("february") && !nde.equals("februar"))
			return true;
		if (nen.equals("march") && !nde.equals("märz"))
			return true;
		if (nen.equals("april") && !nde.equals("april"))
			return true;
		if (nen.equals("may") && !nde.equals("mai"))
			return true;
		if (nen.equals("june") && !nde.equals("juni"))
			return true;
		if (nen.equals("july") && !nde.equals("juli"))
			return true;
		if (nen.equals("august") && !nde.equals("august"))
			return true;
		if (nen.equals("september") && !nde.equals("september"))
			return true;
		if (nen.equals("october") && !nde.equals("oktober"))
			return true;
		if (nen.equals("november") && !nde.equals("november"))
			return true;
		if (nen.equals("december") && !nde.equals("dezember"))
			return true;

		return false;
	}

	private static boolean connected(String nen) {
		boolean found =false;
		for (int ii=0;ii<connect.length;ii++){
			if (nen.contains(connect[ii]))
				return true;
		}
		return found;
	}

	private static void scoring_dedup(String[] args) throws IOException {
		File newdata = new File(args[0]+".filt.dedup");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()), Constants.UTF8));
		
		String inputLine, inputLine1, text, text1;
		int len, len1;
		//double hunscore, hunscore1;
		BufferedReader in, in1;
		List<String> scores= new ArrayList<String>();
		Set<Integer> dups = new HashSet<Integer>();
		int  linecounter=-1, linecounter1;
		String index, index1;
		boolean found;
		try {
			in = new BufferedReader(new FileReader(args[0]+".filt.norm"));

			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (dups.contains(linecounter)){
					scores.add(" 0 ");
					outnewdata.write("0\n");
					LOGGER.info(linecounter+"\tdups:\t"+inputLine);
					continue;
				}
				if (inputLine.equals("0")){
					scores.add(" 0 ");
					outnewdata.write("0\n");
					continue;
				}
				String[] tt = inputLine.split("\t");
				index = tt[0];
				text = tt[1].trim();				//nen = ContentNormalizer.normtext(en);				//de = tt[1].trim();				//nde = ContentNormalizer.normtext(de);
				len = Integer.parseInt(tt[2].trim());
				//hunscore = Double.parseDouble(tt[2].trim());
				//String temp = nen+nde;
				in1 = new BufferedReader(new FileReader(args[0]+".filt.norm"));
				linecounter1=-1;
				found=false;
				while ((inputLine1 = in1.readLine()) != null) {
					linecounter1++;
					if (dups.contains(linecounter1))
						continue;
					if (linecounter1<=linecounter)
						continue;
					if (inputLine1.equals("0"))
						continue;
					//System.out.println(linecounter+"   >>>> "+linecounter1);
					String[] tt1 = inputLine1.split("\t");
					index1 = tt1[0];
					text1 = tt1[10].trim();				//nen1 = ContentNormalizer.normtext(en1);				//de1 = tt1[1].trim();				//nde1 = ContentNormalizer.normtext(de1);
					len1 = Integer.parseInt(tt1[2].trim());
					//hunscore1 = Double.parseDouble(tt1[2].trim());
					//String temp1 = nen1+nde1;
					//System.out.println("-------------------------------------------------------------------------");
					//System.out.println(linecounter+"   :  "+temp);
					//System.out.println(linecounter1+"   :  "+temp1);
					if (len1!=len)
						continue;
					if (text.equals(text1)){
						//if (hunscore<hunscore1){
						if (len<=len1){	
							//found = true;
							dups.add(linecounter1);
							//break;
						}else{
							found = true;
							break;
						}
					}
				}
				in1.close();
				if (!found){
					scores.add(" 1 ");
					outnewdata.write(inputLine+"\n");
					LOGGER.info("passed:\t"+ linecounter);
				}else{
					scores.add(" 0 ");
					outnewdata.write("0\n");
					LOGGER.info(linecounter+" >> "+linecounter1+"\tdups:\t"+inputLine+ " >> "+inputLine1);
				}
			}
			in.close();
			outnewdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileUtils.writeLines(new File(args[0]+".filt.dedup.scores"), Constants.UTF8, scores, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private static void scoring_dedup1(String[] args) throws IOException {
		File newdata = new File(args[0]+".filt.dedup");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()), Constants.UTF8));
		
		String inputLine, inputLine1, en, de, nen, nde, en1, de1, nen1, nde1, indt="";
		//double hunscore, hunscore1;
		//BufferedReader in, in1;
		List<String> scores= new ArrayList<String>();
		Set<Integer> dups = new HashSet<Integer>();
		int  indi=0;
		boolean found=false;

		List<String> lines = FileUtils.readLines(new File(args[0]+".filt"), Constants.UTF8);
		for (int linecounter=0;linecounter<lines.size()-1;linecounter++){
			inputLine = lines.get(linecounter);
			if (dups.contains(linecounter)){
				scores.add(" 0 ");
				outnewdata.write("0\n");
				LOGGER.info(linecounter+"\tdups:\t"+inputLine);
				continue;
			}
			if (inputLine.equals("0")){
				scores.add(" 0 ");
				outnewdata.write("0\n");
				continue;
			}
			String[] tt = inputLine.split("\t");
			en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
			de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
			//hunscore = Double.parseDouble(tt[2].trim());
			String temp = nen+nde;
			found = false;
			for (int linecounter1=linecounter+1;linecounter1<lines.size();linecounter1++){
				inputLine1 = lines.get(linecounter1);
				if (dups.contains(linecounter1))
					continue;
				//if (linecounter1<=linecounter)
				//	continue;
				if (inputLine1.equals("0"))
					continue;
				//System.out.println(linecounter+"   >>>> "+linecounter1);
				String[] tt1 = inputLine1.split("\t");
				en1 = tt1[0].trim();				nen1 = ContentNormalizer.normtext(en1);
				de1 = tt1[1].trim();				nde1 = ContentNormalizer.normtext(de1);
				//hunscore1 = Double.parseDouble(tt1[2].trim());
				String temp1 = nen1+nde1;
				if (temp.equals(temp1)){
					//if (hunscore<hunscore1){
					if (en.length()<=en1.length()){	
						//found = true;
						dups.add(linecounter1);
						//break;
					}else{
						found = true;
						indi = linecounter1;
						indt = inputLine1;
						break;
					}
				}
			}
			if (!found){
				scores.add(" 1 ");
				outnewdata.write(inputLine+"\n");
				LOGGER.info("passed:\t"+ linecounter);
			}else{
				scores.add(" 0 ");
				outnewdata.write("0\n");
				LOGGER.info(linecounter+" >> "+indi+"\tdups:\t"+inputLine+ " >> "+indt);
			}
		}
		outnewdata.close();
		try {
			FileUtils.writeLines(new File(args[0]+".filt.dedup.scores"), Constants.UTF8, scores, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void clusterbysite(String[] args) {
		File infile = new File(args[0]); 
		File outdir = new File(FilenameUtils.concat(infile.getParent(),"sites"));
		BufferedReader in;


		int  linecounter=-1 ;//, comma=0;
		String inputLine, site1, site2, en,  de, score, current_page="", url, nen, nde; 
		URL url1, url2;
		boolean found = false;
		int pagecounter=0;
		Set<String> sites = new HashSet<String>();
		float ratio;
		List<String> lines = new ArrayList<String>();
		File tempfile = new File(FilenameUtils.concat(outdir.getAbsolutePath(), "mixed"));
		File currentfile=null;
		try {
			FileUtils.writeLines(tempfile, Constants.UTF8, lines, "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			in = new BufferedReader(new FileReader(infile));
			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.contains(Constants.TAB)){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length!=5){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				en = attrs[0].trim();
				de = attrs[1].trim();
				nen = ContentNormalizer.normtext(en);
				nde = ContentNormalizer.normtext(de);
				if ( nen.isEmpty() || nde.isEmpty()){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					continue;
				}
				if (nen.equals(nde)){
					LOGGER.info(linecounter +"\texact identical TUVs:\t"+inputLine);
					continue;
				}
				if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					continue;
				}
				if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					continue;
				}
				if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					continue;
				}
				if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					continue;
				}
				found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
					continue;
				}
				if ((en.contains("??") && !de.contains("??")) || (!en.contains("??") && de.contains("??"))) {
					LOGGER.info(linecounter +"\tspecial symbols??:\t"+inputLine);
					continue;
				}
				//ratio = (float)en.length()/(float)de.length();
				//if (ratio<0.3 || ratio >2){
				//	LOGGER.info(linecounter +"\tratio:\t"+ inputLine);
				//	continue;
				//}


				url = attrs[2].trim();
				url1 = new URL(url);
				url2 = new URL(attrs[3].trim());
				//score = Double.parseDouble(attrs[4].trim());
				score = attrs[4].trim();
				site1 = url1.getAuthority();
				site2 = url2.getAuthority();

				if (site1.equals(site2)){
					tempfile = 	new File(FilenameUtils.concat(outdir.getAbsolutePath(), site1));
					if (sites.contains(site1)){
						if (current_page.equals(url)) {
							lines = FileUtils.readLines(currentfile, Constants.UTF8);
						}else{
							lines.clear();
							pagecounter++;
							currentfile = new File(FilenameUtils.concat(tempfile.getAbsolutePath(), Integer.toString(pagecounter)));
						}
					}else{
						lines.clear();
						sites.add(site1);
						LOGGER.info(site1);
						pagecounter=1;
						currentfile = new File(FilenameUtils.concat(tempfile.getAbsolutePath(), Integer.toString(pagecounter)));
					}
					lines.add(linecounter+"\t"+en+"\t"+de+"\t"+score);
					FileUtils.writeLines(currentfile, Constants.UTF8, lines, "\n");

					//if (sites.contains(site1))
					//	lines = FileUtils.readLines(tempfile, Constants.UTF8);
					//else{
					//	lines.clear();
					//	sites.add(site1);
					//	LOGGER.info(site1);
					//}
				}else{
					tempfile = 	new File(FilenameUtils.concat(outdir.getAbsolutePath(), "mixed"));
					lines = FileUtils.readLines(tempfile, Constants.UTF8);
					LOGGER.info(site1+"\t"+site2);
					lines.add(linecounter+"\t"+inputLine);
				}
				//lines.add(linecounter+"\t"+en+"\t"+de+"\t"+score);
				//if (current_page.equals(url)) 
				//FileUtils.writeLines(tempfile, Constants.UTF8, lines, "\n");
				//currentfile = tempfile;
				current_page = url;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static File dedupWMT18_TUV(File indir, String ext, int tupart, int THR) throws IOException {
		File outfile = new File(indir.getAbsolutePath()+ext);
		//BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()), Constants.UTF8));
		
		File dedupdir = new File(FilenameUtils.concat(indir.getParent(), "dupTUV"+Integer.toString(tupart)));
		dedupdir.mkdir();
		File[] lenfiles = indir.listFiles();
		int[] lens = new int[lenfiles.length];
		for (int ii=0;ii<lens.length;ii++){
			lens[ii] = Integer.parseInt(lenfiles[ii].getName());
		}
		Arrays.sort(lens);
		int counter=0;
		Set<String> seen = new HashSet<String>();
		for (int ii=lens.length-1;ii>-1;ii--){
			BufferedReader in1, in2;
			File tempfile1 = new File(FilenameUtils.concat(indir.getAbsolutePath(), Integer.toString(lens[ii])));
			in1 = new BufferedReader(new FileReader(tempfile1));
			//String inputLine1, tuv1, ntuv1,  inputLine2, tuv2, ntuv2;
			String inputLine1, inputLine2;
			int linecounter1=0;
			while ((inputLine1 = in1.readLine()) != null) {
				linecounter1++;
				String[] attrs1 = inputLine1.split(Constants.TAB);
				if (seen.contains(attrs1[0]))
					continue;
				//tuv1 = attrs1[tupart];
				//ntuv1 = ContentNormalizer.normtext(tuv1);
				//List<String> stokens1 = FCStringUtils.getTokens(ntuv1);
				//Set<String> grams1 = getGrams(stokens1);
				Set<String> grams1 = getGrams(FCStringUtils.getTokens(ContentNormalizer.normtext(attrs1[tupart])));

				List<String> dups = new ArrayList<String>();
				dups.add(inputLine1);
				for (int kk=lens.length-1;kk>-1;kk--){
					if (kk>ii)
						continue;
					File tempfile2 = new File(FilenameUtils.concat(indir.getAbsolutePath(), Integer.toString(lens[kk])));
					System.out.println(tempfile1.getName()+" --vs-- "+tempfile2.getName());
					in2 = new BufferedReader(new FileReader(tempfile2));
					int linecounter2=0;
					while ((inputLine2 = in2.readLine()) != null) {
						if (kk==ii){
							if (linecounter2<linecounter1){
								linecounter2++;
								continue;
							}
						}
						linecounter2++;
						String[] attrs2 = inputLine2.split(Constants.TAB);
						if (seen.contains(attrs2[0]))
							continue;
						//tuv2 = attrs2[tupart];
						//ntuv2 = ContentNormalizer.normtext(tuv2);
						//List<String> stokens2 = FCStringUtils.getTokens(ntuv2);
						//Set<String> grams2 =getGrams(stokens2);

						Set<String> grams2 = getGrams(FCStringUtils.getTokens(ContentNormalizer.normtext(attrs2[tupart])));

						Set<String> intersection = new HashSet<String>(grams1);
						intersection.retainAll(grams2);
						if ((Math.min(grams2.size(),grams1.size()) -  intersection.size()  ) < THR){
							System.out.println(inputLine2);
							dups.add(inputLine2);
							seen.add(attrs2[0]);
						}
					}
					in2.close();
				}
				if (dups.size()>1){
					File temp = new File(FilenameUtils.concat(dedupdir.getAbsolutePath(),"_dup-"+counter));
					FileUtils.writeLines(temp, Constants.UTF8, dups, "\n");
					counter++;
				}
				if (dups.size()==1){
					bw.write(inputLine1+"\n");
				}

			}
			in1.close();
		}
		bw.close();
		return outfile;
	}

	private static Set<String> getGrams(List<String> tokens) {
		Set<String> grams = new HashSet<String>();
		for (int jj=0;jj<tokens.size()-1;jj++){
			grams.add(tokens.get(jj));  												//1-gram
			grams.add(tokens.get(jj)+" "+tokens.get(jj+1));								//2-gram
			if (jj>0){
				grams.add(tokens.get(jj-1)+" "+tokens.get(jj)+" "+tokens.get(jj+1));	//3-gram
			}
		}
		grams.add(tokens.get(tokens.size()-1));
		return grams;
	}

	private static List<File> filterCorpus(File infile, String[] langs, int minTuvLen, int maxTuvLen, float minratio, float maxratio, String ext, String version) {
		LOGGER.info("\nargs:\n 0=fullpath of infile\n 1=languages separated by ;\n 2=minTUVlen\n 3=maxTUVlen\n 4=minTUVratio\n 5=maxTUVratio\n 6=version");
		List<File> outs = new ArrayList<File>();
		LangDetector langDetector = LangDetectUtils.loadLangDetectors(langs,"langdetect");

		File outfile0 = new File(infile.getAbsolutePath()+ext+"."+version);
		File outfile1 = new File(infile.getAbsolutePath()+ext+".samenum"+"."+version);
		File outfile1a = new File(infile.getAbsolutePath()+ext+".nonum"+"."+version);
		File outfile2 = new File(infile.getAbsolutePath()+ext+".samesym"+"."+version);
		File outfile3 = new File(infile.getAbsolutePath()+ext+".samenumsym"+"."+version);
		File outfile3a = new File(infile.getAbsolutePath()+ext+".nonumsamesym"+"."+version);
		File enfile = new File(infile.getAbsolutePath()+".en");
		File defile = new File(infile.getAbsolutePath()+".de");
		outs.add(outfile0); outs.add(outfile1); outs.add(outfile1a); outs.add(outfile2); outs.add(outfile3);	outs.add(outfile3a);  
		String inputLine;
		BufferedReader in;
		BufferedWriter  bw0 ,bw1, bw2, bw3, bwen, bwde, bw1a, bw3a;

		BufferedWriter  bwcharsets ,bwexclsymbols, bwidenTUVs, bwsymbols, bwshort, bwlong, bwlang, bwratio;
		BufferedWriter bwemail, bwurl, bwcase, bwmaxlen, bwminlen, bwhunscore, bwincoh, bwnearTUVs, bwrepeats;

		int  counter0 = 0, counter1= 0, counter1a= 0, counter2= 0, counter3 = 0, counter3a = 0, tempcounter = 0, linecounter=-1 ;//, comma=0;
		String lang1, lang2, nen, nde, num1, num2, en, de;
		float ratio;
		boolean found = false;
		//Set<String> nenlist1 = new HashSet<String>();
		//Set<String> ndelist1 = new HashSet<String>();

		try {
			in = new BufferedReader(new FileReader(infile));
			
			/*bw0 = new BufferedWriter(new FileWriter(outfile0));
			bw1 = new BufferedWriter(new FileWriter(outfile1));
			bw1a = new BufferedWriter(new FileWriter(outfile1a));
			bw2 = new BufferedWriter(new FileWriter(outfile2));
			bw3 = new BufferedWriter(new FileWriter(outfile3));
			bw3a = new BufferedWriter(new FileWriter(outfile3a));
			bwen = new BufferedWriter(new FileWriter(enfile));
			bwde = new BufferedWriter(new FileWriter(defile));

			bwcharsets = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".charsets")));
			bwexclsymbols = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".exclsymbols")));
			bwidenTUVs = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".idenTUVs")));
			bwsymbols = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".mostly-non-letters")));
			bwshort = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".short")));
			bwlong = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".long")));
			bwlang = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".lang")));
			bwratio = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".ratio")));
			bwcase = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".case")));
			bwemail = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".email")));
			bwurl = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".url")));
			bwmaxlen = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".max-len")));
			bwminlen = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".min-len")));
			bwhunscore = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".hun-score")));
			bwincoh = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".incoh")));
			bwnearTUVs = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".nearTUVs")));
			bwrepeats = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".repeats")));*/

			bw0 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile0), Constants.UTF8));
			bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1), Constants.UTF8));
			bw1a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1a), Constants.UTF8));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile2), Constants.UTF8));
			bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3), Constants.UTF8));
			bw3a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3a), Constants.UTF8));
			bwen = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(enfile), Constants.UTF8));
			bwde = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defile), Constants.UTF8));

			bwcharsets = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".charsets")), Constants.UTF8));
			bwexclsymbols = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".exclsymbols")), Constants.UTF8));
			bwidenTUVs = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".idenTUVs")), Constants.UTF8));
			bwsymbols = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".mostly-non-letters")), Constants.UTF8));
			bwshort = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".short")), Constants.UTF8));
			bwlong = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".long")), Constants.UTF8));
			bwlang = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".lang")), Constants.UTF8));
			bwratio = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".ratio")), Constants.UTF8));
			bwcase = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".case")), Constants.UTF8));
			bwemail = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".email")), Constants.UTF8));
			bwurl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".url")), Constants.UTF8));
			bwmaxlen = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".max-len")), Constants.UTF8));
			bwminlen = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".min-len")), Constants.UTF8));
			bwhunscore = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".hun-score")), Constants.UTF8));
			bwincoh = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".incoh")), Constants.UTF8));
			bwnearTUVs = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".nearTUVs")), Constants.UTF8));
			bwrepeats = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( new File(infile.getAbsolutePath()+".repeats")), Constants.UTF8));
			
			
			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.contains(Constants.TAB)){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length!=3){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				en = attrs[0].trim();
				de = attrs[1].trim();
				bwen.write(en+"\n");
				bwde.write(de+"\n");
				if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
					bwexclsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if ((en.contains("??") && !de.contains("??")) || (!en.contains("??") && de.contains("??"))) {
					LOGGER.info(linecounter +"\tspecial symbols??:\t"+inputLine);
					bwexclsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (en.equals(de)){
					LOGGER.info(linecounter +"\texact identical TUVs:\t"+inputLine);
					bwidenTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				nen = ContentNormalizer.normtext(en);
				nde = ContentNormalizer.normtext(de);
				if ( nen.isEmpty() || nde.isEmpty()){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (nen.equals(nde)){
					LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					bwidenTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				if (((float)nen.length()/(float)en.length())<0.6){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (((float)nde.length()/(float)de.length())<0.6){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				int nencount = FCStringUtils.countTokens(nen);
				int ndecount = FCStringUtils.countTokens(nde);
				if (nencount<minTuvLen || ndecount<minTuvLen){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);
					bwshort.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				lang1 = langDetector.detect(en);
				lang2 = langDetector.detect(de);
				if (!(lang1.equals(langs[0]) && lang2.equals(langs[1]))){
					LOGGER.info(linecounter +"\tlangs:\t"+ inputLine);
					bwlang.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				if (nencount>maxTuvLen || ndecount>maxTuvLen){
					LOGGER.info(linecounter +"\tlong:\t"+ inputLine);
					bwlong.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				ratio = (float)en.length()/(float)de.length();
				if ( ratio>maxratio || ratio < minratio){
					LOGGER.info(linecounter +"\tratio:\t"+ inputLine);
					bwratio.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (FCStringUtils.isAllUpperCase(en) * FCStringUtils.isAllUpperCase(de)<0){
					LOGGER.info(linecounter +"\tcase:\t"+ inputLine);
					bwcase.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (TMXHandlerUtils.checkemail(en, 0.5) || TMXHandlerUtils.checkemail(de, 0.5)){
					LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					bwemail.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (TMXHandlerUtils.checkurl(en, 0.5) || TMXHandlerUtils.checkurl(de, 0.5)){
					LOGGER.info(linecounter +"\turl:\t"+ inputLine);
					bwurl.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				List<String> stokens = FCStringUtils.getTokens(en);
				List<String> ttokens = FCStringUtils.getTokens(de); 
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
				if (Statistics.getMedian(stokenslen)<3 || Statistics.getMedian(ttokenslen)<3){
					LOGGER.info(linecounter + "\tshort median:\t"+ inputLine);
					bwminlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				int a = stokens.size();
				int b = getGrams(stokens).size();
				if (b<a){
					LOGGER.info(linecounter + "\trepeats:\t"+ inputLine);
					bwrepeats.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				a = ttokens.size();
				b = getGrams(ttokens).size();
				if (b<a){
					LOGGER.info(linecounter + "\trepeats:\t"+ inputLine);
					bwrepeats.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				//if (nenlist1.contains(nen)){
				//	LOGGER.info(linecounter + "\tdup TUV1:\t"+ inputLine);	
				//	continue;
				//}
				//if (ndelist1.contains(nde)){
				//	LOGGER.info(linecounter + "\tdup TUV2:\t"+ inputLine);	
				//	continue;
				//}
				num1=en.replaceAll("\\D+","");
				num2=de.replaceAll("\\D+","");
				if (num1.isEmpty() && num2.isEmpty()){
					double hunscore = Double.parseDouble(attrs[2]);
					if (hunscore<-0.3){
						LOGGER.info(linecounter + "\tlow hunscore:\t"+ inputLine);
						bwhunscore.write(linecounter+"\t"+inputLine+"\n");
						continue;
					}
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

				List<String> nstokens = FCStringUtils.getTokens(nen); 
				List<String> nttokens = FCStringUtils.getTokens(nde); 
				if (nearTUVs(nstokens, nttokens, 0.7) || nearTUVs(nttokens, nstokens, 0.7)){ //on TU level
					LOGGER.info(linecounter + "\tnear TUVs:\t"+ inputLine);
					bwnearTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				tempcounter = FCStringUtils.countTokens(en);
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
				String ensym = ContentNormalizer.leaveSymbols(en); 
				String desym = ContentNormalizer.leaveSymbols(de);
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
			bwen.close();			bwde.close();
			bwcharsets.close();		bwexclsymbols.close();	bwidenTUVs.close();		bwsymbols.close();		bwshort.close();		bwlong.close();
			bwlang.close();			bwratio.close();		bwemail.close();		bwurl.close();			bwcase.close();			bwmaxlen.close();
			bwminlen.close();		bwhunscore.close();		bwincoh.close();		bwnearTUVs.close();		bwrepeats.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("filtered corpus contains "+ counter0 + " tokens in "+ langs[0]);
		LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in "+ langs[0]);
		LOGGER.info("no number filtered corpus contains "+ counter1a + " tokens in "+ langs[0]);
		LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in "+ langs[0]);
		LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in "+ langs[0]);
		LOGGER.info("no number and same symbols filtered corpus contains "+ counter3a + " tokens in "+ langs[0]);
		return outs;
	}

	private static void buildcorpus(String[] args) throws IOException {
		File infile = new File(args[0]); 
		File outfile = new File(infile.getAbsolutePath()+".tmp");
		Set<String> envoc = new HashSet<String>();
		//Set<String> devoc = new HashSet<String>();
		String inputLine, nen;
		BufferedReader in;
		//BufferedWriter  bw = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()), Constants.UTF8));
		int linecounter=0, words=0;
		int thr=6;
		try {
			in = new BufferedReader(new FileReader(infile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;

				String[] attrs = inputLine.split(Constants.TAB);
				nen = ContentNormalizer.normtext(attrs[0]);

				List<String> stokens = FCStringUtils.getTokens(nen);
				List<String> grams = new ArrayList<String>();
				for (int ii=0;ii<stokens.size()-1;ii++){
					grams.add(stokens.get(ii));  												//1-gram
					grams.add(stokens.get(ii)+" "+stokens.get(ii+1));							//2-gram
					if (ii>0){
						grams.add(stokens.get(ii-1)+" "+stokens.get(ii)+" "+stokens.get(ii+1));	//3-gram
					}
				}
				int counter = grams.size();
				for (int ii=0;ii<grams.size();ii++){
					if (envoc.contains(grams.get(ii)))
						counter--;
				}
				if (counter<thr){
					System.out.println(inputLine);
					continue;
				}
				for (int ii=0;ii<grams.size();ii++){
					envoc.add(grams.get(ii));
				}
				bw.write(inputLine+"\n");
				words = words + stokens.size();
			}
			in.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	

	}


	private static File clusterbylength(File infile, int tuvid) throws IOException {

		File outdir = new File(FilenameUtils.concat(infile.getParent(),"bylen"));
		outdir.mkdir();

		Set<Integer> lenslist = new HashSet<Integer>();
		String inputLine;
		BufferedReader in;
		int linecounter=0, len;	
		try {
			in = new BufferedReader(new FileReader(infile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				len = FCStringUtils.countTokens(ContentNormalizer.normtext(attrs[tuvid]));
				lenslist.add(len);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		Iterator<Integer> it = lenslist.iterator();
		int len1;
		while (it.hasNext()){
			len = it.next();
			System.out.println("len="+len);
			in = new BufferedReader(new FileReader(infile));
			File lenfile = new File(FilenameUtils.concat(outdir.getAbsolutePath(),Integer.toString(len)));
			//BufferedWriter  bw = new BufferedWriter(new FileWriter(lenfile));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lenfile.getAbsolutePath()), Constants.UTF8));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				len1 = FCStringUtils.countTokens(ContentNormalizer.normtext(attrs[tuvid]));
				if (len1==len){
					bw.write(inputLine+"\n");
				}
				lenslist.add(len);
			}
			in.close();
			bw.close();
		}
		return outdir;
	}

	/**
	 * It assumes that infile is a text file containing a parallel corpus
	 * "line id", "TUV1","TUV2", "hunscore" (TAB separated) 
	 * for each text line (segment pair), it generates the 1-3grams of the tupart TUV
	 * examines the list of n-grams with the list of all other segment pairs and identifies near-duplicates in case the   
	 * @param infile
	 * @param ext
	 * @param tupart : 1 for source TUV, 2 for target TUV
	 * @return
	 */

	private static File dedupWMT18(File infile, String ext, int tupart ) {
		File outfile = new File(infile.getAbsolutePath()+ext);
		File dedupdir = new File(FilenameUtils.concat(infile.getParent(), "dupTUV"+Integer.toString(tupart)));
		dedupdir.mkdir();
		String inputLine1, inputLine2;
		BufferedReader in1, in2;
		BufferedWriter bw;

		int  counter = 0, linecounter1=0, linecounter2=0 ;//, comma=0;
		String nde1, de1, nde2, de2;
		int THR = 6;
		try {
			in1 = new BufferedReader(new FileReader(infile));
			//bw = new BufferedWriter(new FileWriter(outfile));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()), Constants.UTF8));
			List<String> seen = new ArrayList<String>();
			while ((inputLine1 = in1.readLine()) != null) {
				linecounter1++;
				String[] attrs1 = inputLine1.split(Constants.TAB);
				String temp1 = attrs1[1]+"\t"+attrs1[2]+"\t"+attrs1[3];
				if (seen.contains(md5hash(temp1,3)))
					continue;

				de1 = attrs1[tupart];
				//score1 = Double.parseDouble(attrs1[2]);
				nde1 = ContentNormalizer.normtext(de1);
				List<String> stokens1 = FCStringUtils.getTokens(nde1);
				Set<String> grams1 = new HashSet<String>();
				for (int ii=0;ii<stokens1.size()-1;ii++){
					grams1.add(stokens1.get(ii));  												//1-gram
					grams1.add(stokens1.get(ii)+" "+stokens1.get(ii+1));						//2-gram
					if (ii>0){
						grams1.add(stokens1.get(ii-1)+" "+stokens1.get(ii)+" "+stokens1.get(ii+1));	//3-gram
					}
				}

				List<String> dups = new ArrayList<String>();
				dups.add(inputLine1);
				in2 = new BufferedReader(new FileReader(infile));
				linecounter2 = 0;
				while ((inputLine2 = in2.readLine()) != null) {
					if (linecounter2<linecounter1){
						linecounter2++;
						continue;
					}
					linecounter2++;
					//System.out.println(linecounter1 +"\t"+linecounter2);
					String[] attrs2 = inputLine2.split(Constants.TAB);
					de2 = attrs2[tupart];
					//score2 = Double.parseDouble(attrs2[2]);
					nde2 = ContentNormalizer.normtext(de2);
					List<String> stokens2 = FCStringUtils.getTokens(nde2);
					Set<String> grams2 = new HashSet<String>();
					for (int ii=0;ii<stokens2.size()-1;ii++){
						grams2.add(stokens2.get(ii));  												//1-gram
						grams2.add(stokens2.get(ii)+" "+stokens2.get(ii+1));						//2-gram
						if (ii>0){
							grams2.add(stokens2.get(ii-1)+" "+stokens2.get(ii)+" "+stokens2.get(ii+1));	//3-gram
						}
					}

					Set<String> intersection = new HashSet<String>(grams1);
					intersection.retainAll(grams2);
					int t1 = intersection.size();
					if ((Math.min(grams2.size(),grams1.size()) -  intersection.size()  ) < THR){
						//System.out.println(inputLine2);
						dups.add(inputLine2);
						String temp2 = attrs2[1]+"\t"+attrs2[2]+"\t"+attrs2[3];
						seen.add(md5hash(temp2,3));
					}
				}
				in2.close();
				if (dups.size()>1){
					File temp = new File(FilenameUtils.concat(dedupdir.getAbsolutePath(),"_dup-"+counter));
					FileUtils.writeLines(temp, Constants.UTF8, dups, "\n");
					counter++;
				}
				if (dups.size()==1){
					bw.write(inputLine1);
				}
			}
			in1.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//LOGGER.info("filtered corpus contains "+ counter + " tokens in lang1");
		//LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in lang1");
		//LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in lang1");
		//LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in lang1");
		return outfile;
	}

	private static void subsetWMT18(String[] args) {
		LOGGER.info("\nargs:\n 0=fullpath of infile\n 1=languages separated by ;\n 2=minTUVlen\n 3=minTUVratio\n 4=maxTUVratio\n 5=version");
		File infile = new File(args[0]); 

		String[] langs = args[1].split(Constants.SEMICOLON);
		langs[0]=ISOLangCodes.get3LetterCode(langs[0]);
		langs[1]=ISOLangCodes.get3LetterCode(langs[1]);	
		LangDetector langDetector = LangDetectUtils.loadLangDetectors(langs,"langdetect");

		int minTuvLen = Integer.parseInt(args[2]);
		float minratio = Float.parseFloat(args[3]);
		float maxratio = Float.parseFloat(args[4]);
		String ext = ".filt"+args[2]+"len_"+args[3]+"ra_"+args[4]+"ra_"+"dupTU_"+"dupTUV_"+"incoh";
		File outfile = new File(infile.getAbsolutePath()+ext);
		File outfile1 = new File(infile.getAbsolutePath()+ext+".samenum");
		File outfile2 = new File(infile.getAbsolutePath()+ext+".samesym");
		File outfile3 = new File(infile.getAbsolutePath()+ext+".samenumsym");
		File outfile4 = new File(infile.getAbsolutePath()+ext+"."+args[5]);
		File enfile = new File(infile.getAbsolutePath()+".en");
		File defile = new File(infile.getAbsolutePath()+".de");
		File scorefile = new File(infile.getAbsolutePath()+".score."+args[5]);
		File scoresamenumfile = new File(infile.getAbsolutePath()+".samenum.score."+args[5]);
		File scoresamesymfile = new File(infile.getAbsolutePath()+".samesym.score."+args[5]);
		File scoresamenumsymfile = new File(infile.getAbsolutePath()+".samenumsym.score."+args[5]);

		String inputLine;
		BufferedReader in;
		BufferedWriter  bw ,bw1, bw2, bw3, bw4, bwen, bwde, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym;

		int  counter = 0, counter1= 0, counter2= 0, counter3 = 0, tempcounter = 0, linecounter=0 ;//, comma=0;
		String lang1, lang2, nen, nde, num1, num2, en, de;
		float ratio;
		boolean found = false;
		Set<String> nenlist1 = new HashSet<String>();
		Set<String> ndelist1 = new HashSet<String>();

		Set<List<String>> nenlist = new HashSet<List<String>>();
		Set<List<String>> ndelist = new HashSet<List<String>>();

		try {
			in = new BufferedReader(new FileReader(infile));
			/*bw = new BufferedWriter(new FileWriter(outfile));
			bw1 = new BufferedWriter(new FileWriter(outfile1));
			bw2 = new BufferedWriter(new FileWriter(outfile2));
			bw3 = new BufferedWriter(new FileWriter(outfile3));
			bw4 = new BufferedWriter(new FileWriter(outfile4));
			bwen = new BufferedWriter(new FileWriter(enfile));
			bwde = new BufferedWriter(new FileWriter(defile));
			bwscore = new BufferedWriter(new FileWriter(scorefile));
			bwscoresamenum = new BufferedWriter(new FileWriter(scoresamenumfile));
			bwscoresamesym = new BufferedWriter(new FileWriter(scoresamesymfile));
			bwscoresamenumsym = new BufferedWriter(new FileWriter(scoresamenumsymfile));*/
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), Constants.UTF8));
			bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1), Constants.UTF8));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile2), Constants.UTF8));
			bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3), Constants.UTF8));
			bw4 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile4), Constants.UTF8));
			bwen = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(enfile), Constants.UTF8));
			bwde = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defile), Constants.UTF8));
			bwscore = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scorefile), Constants.UTF8));
			bwscoresamenum = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scoresamenumfile), Constants.UTF8));
			bwscoresamesym = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scoresamesymfile), Constants.UTF8));
			bwscoresamenumsym = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scoresamenumsymfile), Constants.UTF8));
			
			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.contains(Constants.TAB)){
					LOGGER.info("not valid line:\t"+ inputLine);
					bw4.write(inputLine+"\n");
					continue;
				}
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length!=3){
					LOGGER.info("not valid line:\t"+ inputLine);
					bw4.write(inputLine+"\n");
					continue;
				}
				en = attrs[0];
				de = attrs[1];
				bwen.write(en+"\n");
				bwde.write(de+"\n");
				if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbols:\t"+inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.equals(de)){
					LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				nen = ContentNormalizer.normtext(en);
				if (((float)nen.length()/(float)en.length())<0.6){
					LOGGER.info(linecounter +"\tmany non-letters:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				nde = ContentNormalizer.normtext(de);
				if (((float)nde.length()/(float)de.length())<0.6){
					LOGGER.info(linecounter +"\tmany non-letters:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if ( nen.isEmpty() || nde.isEmpty()){
					LOGGER.info(linecounter +"\tsymbols:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (nen.equals(nde)){
					LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				int nencount = FCStringUtils.countTokens(nen);
				int ndecount = FCStringUtils.countTokens(nde);
				if (nencount<minTuvLen || ndecount<minTuvLen){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (nencount>70 || ndecount>70){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");	
					continue;
				}
				lang1 = langDetector.detect(en);
				lang2 = langDetector.detect(de);
				if (!(lang1.equals(langs[0]) && lang2.equals(langs[1]))){
					LOGGER.info(linecounter +"\tlangs:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}

				ratio = (float)en.length()/(float)de.length();
				if ( ratio>maxratio || ratio < minratio){
					LOGGER.info(linecounter +"\tratio:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (FCStringUtils.isAllUpperCase(en) * FCStringUtils.isAllUpperCase(de)<0){
					LOGGER.info(linecounter +"\tcase:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (TMXHandlerUtils.checkemail(en, 0.5) || TMXHandlerUtils.checkemail(de, 0.5)){
					LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (TMXHandlerUtils.checkurl(en, 0.5) || TMXHandlerUtils.checkurl(de, 0.5)){
					LOGGER.info(linecounter +"\turl:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}

				//temp_ende = md5hash(nen+" "+nde,3);
				//if (nennde.contains(temp_ende)){
				//	LOGGER.info(linecounter +"\tdup TU:\t"+inputLine);
				//	continue;
				//}//else{
				//	nennde.add(temp_ende);
				//}
				//temp_en = md5hash(nen,3);
				//if (nenlist.contains(temp_en)){
				//	LOGGER.info(linecounter +"\tdup TUV1:\t"+ inputLine);
				//	continue;
				//}//else{
				//	nenlist.add(temp_en);
				//}
				//temp_de = md5hash(nde,3);
				//if (ndelist.contains(temp_de)){
				//	LOGGER.info(linecounter +"\tdup TUV2:\t"+ inputLine);
				//	continue;
				//}//else{
				//	ndelist.add(temp_de);
				//}

				List<String> stokens = FCStringUtils.getTokens(en); 
				List<String> ttokens = FCStringUtils.getTokens(de); 
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);
				if (Statistics.getMax(stokenslen)>40 || Statistics.getMax(ttokenslen)>40){
					LOGGER.info(linecounter + "\tmax word:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (Statistics.getMedian(stokenslen)>=25 || Statistics.getMedian(ttokenslen)>=25){
					LOGGER.info(linecounter + "\tlong median:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (Statistics.getMedian(stokenslen)<3 || Statistics.getMedian(ttokenslen)<3){
					LOGGER.info(linecounter + "\tshort median:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (nenlist1.contains(nen)){
					LOGGER.info(linecounter + "\tdup TUV1:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (ndelist1.contains(nde)){
					LOGGER.info(linecounter + "\tdup TUV2:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				num1=en.replaceAll("\\D+","");
				num2=de.replaceAll("\\D+","");
				if (num1.isEmpty() && num2.isEmpty()){
					double hunscore = Double.parseDouble(attrs[2]);
					if (hunscore<-0.3){
						LOGGER.info(linecounter + "\tlow hunscore:\t"+ inputLine);
						addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.3");
						continue;
					}
				}

				if (incoherent(stokens)){
					LOGGER.info(linecounter + "\tincoherent TUV1:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.5");
					continue;
				}
				if (incoherent(ttokens)){
					LOGGER.info(linecounter + "\tincoherent TUV2:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.5");	
					continue;
				}

				List<String> nstokens = FCStringUtils.getTokens(nen); 
				List<String> nttokens = FCStringUtils.getTokens(nde); 

				if (nearTUVs(nstokens, nttokens, 0.7) || nearTUVs(nttokens, nstokens, 0.7)){ //on TU level
					LOGGER.info(linecounter + "\tnear TUVs:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.6");
					continue;
				}

				if (nearTUVsList(nenlist, nstokens, 0.7)){ //across TUV1
					LOGGER.info(linecounter + "\tnear TUV1s:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.6");	
					continue;
				}
				if (nearTUVsList(ndelist, nttokens, 0.7)){ //across TUV2
					LOGGER.info(linecounter + "\tnear TUV2s:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.6");	
					continue;
				}

				tempcounter = FCStringUtils.countTokens(en);
				counter = counter + tempcounter;
				bw.write(inputLine+"\n");
				String ss =  Integer.toString(nencount + ndecount);
				//addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, ss);					
				bw4.write(en+"\t"+de+"\t"+ss+"\n");
				bwscore.write(ss+"\n");

				//nennde.add(temp_ende);
				nenlist1.add(nen);
				ndelist1.add(nde);
				nenlist.add(nstokens);
				ndelist.add(nttokens);

				if (num1.equals(num2) ){
					counter1 = counter1+tempcounter;
					bw1.write(inputLine+"\n");
					bwscoresamenum.write(ss+"\n");
				}else{
					bwscoresamenum.write("0\n");
				}
				String ensym = ContentNormalizer.leaveSymbols(en); 
				String desym = ContentNormalizer.leaveSymbols(de);
				if (ensym.equals(desym)){
					counter2 = counter2+tempcounter;
					bw2.write(inputLine+"\n");
					bwscoresamesym.write(ss+"\n");
				}else{
					bwscoresamesym.write("0\n");
				}
				if (num1.equals(num2) && ensym.equals(desym) ){
					counter3 = counter3+tempcounter;
					bw3.write(inputLine+"\n");
					bwscoresamenumsym.write(ss+"\n");
				}else{
					bwscoresamenumsym.write("0\n");
				}
			}
			in.close();
			bw.close();
			bw1.close();
			bw2.close();
			bw3.close();
			bw4.close();
			bwen.close();
			bwde.close();
			bwscore.close();
			bwscoresamenum.close();
			bwscoresamesym.close();
			bwscoresamenumsym.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("filtered corpus contains "+ counter + " tokens in "+ langs[0]);
		LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in "+ langs[0]);
		LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in "+ langs[0]);
		LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in "+ langs[0]);

	}

	private static void test_enpl_paracrawl() throws IOException {

		File enfile = new File("C:/Users/vpapa/ELRC/paracrawl/paracrawl-release1.en-pl.zipporah0-dedup-clean/paracrawl-release1.en-pl.zipporah0-dedup-clean.en");
		File plfile = new File("C:/Users/vpapa/ELRC/paracrawl/paracrawl-release1.en-pl.zipporah0-dedup-clean/paracrawl-release1.en-pl.zipporah0-dedup-clean.pl");
		int ind = enfile.getAbsolutePath().indexOf(".");
		File cutsfile = new File(enfile.getAbsolutePath().substring(0, ind)+".cut");
		File outfile = new File(enfile.getAbsolutePath().substring(0, ind)+".filt");
		/*BufferedWriter bwcut = new BufferedWriter(new FileWriter(cutsfile));
		BufferedWriter bwout = new BufferedWriter(new FileWriter(outfile));*/
		BufferedWriter bwcut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cutsfile), Constants.UTF8));
		BufferedWriter bwout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), Constants.UTF8));

		List<String> ens = FileUtils.readLines(enfile, Constants.UTF8);
		List<String> pls = FileUtils.readLines(plfile, Constants.UTF8);
		String en1="", pl1="", nen1="", npl1="", nennpl="";
		List<String> list = new ArrayList<String>();
		boolean found;
		for (int ii=0;ii<ens.size();ii++){
			en1=ens.get(ii);
			pl1=pls.get(ii);
			if (en1.matches(".*[\\u4E00-\\u9FFF].*") || pl1.matches(".*[\\u4E00-\\u9FFF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (en1.matches(".*[\\u0600-\\u06FF].*") || pl1.matches(".*[\\u0600-\\u06FF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (en1.matches(".*[\\u0980-\\u09FF].*") || pl1.matches(".*[\\u0980-\\u09FF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (en1.matches(".*[\\u0C80-\\u0CFF].*") || pl1.matches(".*[\\u0C80-\\u0CFF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			found = false;
			for (int jj=0;jj<exclude_symbols.length;jj++){
				if (en1.contains(exclude_symbols[jj]) || pl1.contains(exclude_symbols[jj]) ){
					found = true;
					break;
				}
			}
			if (found){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			nen1 = ContentNormalizer.normtext(en1);
			npl1 = ContentNormalizer.normtext(pl1);
			if (nen1.isEmpty() || npl1.isEmpty()){
				bwcut.write("no letters\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (nen1.equals(npl1)){
				bwcut.write("identical\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (TMXHandlerUtils.checkemail(en1, 0.5) || TMXHandlerUtils.checkemail(pl1, 0.5)){
				bwcut.write("email\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (TMXHandlerUtils.checkurl(en1, 0.5) || TMXHandlerUtils.checkurl(pl1, 0.5)){
				bwcut.write("url\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			List<String> stokens = FCStringUtils.getTokens(nen1); 
			List<String> ttokens = FCStringUtils.getTokens(npl1); 
			if (stokens.size()<2 || ttokens.size()<2){
				bwcut.write("short\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			nennpl = nen1+"\t"+npl1;
			if (list.contains(nennpl)){
				bwcut.write("dups\t"+en1+"\t"+pl1+"\n");
				continue;
			}else{
				list.add(nennpl);
				bwout.write(en1+"\t"+pl1+"\n");
			}
		}
		bwcut.close();
		bwout.close();
	}

	private static void addlines(BufferedWriter bw4, BufferedWriter bwscore,	BufferedWriter bwscoresamenum, 	BufferedWriter bwscoresamesym, BufferedWriter bwscoresamenumsym,
			String en, String de, String score) throws IOException {

		bw4.write(en+"\t"+de+"\t"+score+"\n");
		bwscore.write(score+"\n");
		bwscoresamenum.write(score+"\n");
		bwscoresamesym.write(score+"\n");
		bwscoresamenumsym.write(score+"\n");
	}

	private static boolean nearTUVsList(Set<List<String>> nenlist, 	List<String> nstokens, double d) {

		Iterator<List<String>> it = nenlist.iterator();
		List<String> t = new ArrayList<String>();
		boolean found  = false;
		while (it.hasNext()){
			t = it.next();
			if (nearTUVs(t, nstokens, 0.7) || nearTUVs(nstokens, t, 0.7)){
				found = true;
				break;
			}	
		}
		return found;
	}

	private static boolean nearTUVs(List<String> nttokens, List<String> nstokens, double thr) {
		double count=0;
		for (int ii=0;ii<nstokens.size();ii++){
			if (nttokens.contains(nstokens.get(ii)))
				count++;
		}
		if (count/(double)(nstokens.size())>=thr)
			return true;
		return false;
	}

	private static boolean incoherent(List<String> stokens) {
		int comma = 0;
		for (int kk=0;kk<stokens.size();kk++){
			if (stokens.get(kk).endsWith(","))
				comma++;
		}
		if (((float)comma/(float)stokens.size())>0.4){
			return true;
		}
		return false;
	}

	private static String md5hash(String text, int MIN_TOKEN_LEN) {
		String string_key="";
		byte[] texthashkey = DedupUtils.calculateMD5(text, MIN_TOKEN_LEN); //TODO should text be stemmed?
		for (int jj=0;jj<texthashkey.length;jj++) {
			string_key += texthashkey[jj];
		}
		return string_key;
	}




	/*	

	try {
		in1 = new BufferedReader(new FileReader(filetop));
		while ((inputLine1 = in1.readLine()) != null) {
			linecounter1++;
			String[] tt1 = inputLine1.split("\t");
			ind1 = Integer.parseInt(tt1[0]);
			if (tobelowered.contains(ind1))
				continue;
			norm1 = tt1[lang].trim();
			toks1 = FCStringUtils.getTokens(norm1);
			//if (entoks1.size()<8)
			//	continue;
			grams1 = getGrams(toks1);
			in2 = new BufferedReader(new FileReader(filetop));
			linecounter2=-1;
			limit = linecounter1+beam;
			//foundold = false;
			while ((inputLine2 = in2.readLine()) != null) {
				linecounter2++;
				if (linecounter2>limit)
					break;
				if (linecounter2>linecounter1 ){
					String[] tt2 = inputLine2.split("\t");
					ind2 = Integer.parseInt(tt2[0]);
					if (tobelowered.contains(ind2))
						continue;
					norm2 = tt2[lang].trim();
					toks2 = FCStringUtils.getTokens(norm2);
					//if (entoks2.size()<8)
					//	continue;
					grams2 = getGrams(toks2);
					Set intersection = new HashSet(grams1);
					intersection.retainAll(grams2);
					ratio = (double)intersection.size()  / (double)Math.min(grams2.size(),grams1.size()) ;
					found = false;
					len = Math.min(toks2.size(), toks1.size());
					if (ratio >=0.95)
						found = true;
					else{
						if ( ratio> THR && len>6)
							found = true;
					}
					if ( found){
						//foundold = true;
						LOGGER.info(Double.toString(ratio));
						LOGGER.info(linecounter1 + "\t" + inputLine1);
						LOGGER.info(linecounter2 + "\t" + inputLine2);
						LOGGER.info("===============================");
						if (grams1.size()>=grams2.size()){
							LOGGER.info("passed:\t"+inputLine1);
							tobelowered.add(ind2);
						}else{
							LOGGER.info("passed:\t"+inputLine2);
							tobelowered.add(ind1);
							break;
						}
						LOGGER.info("===============================");
					}
					if (foundold && !found)
						break;
				}
			}
			in2.close();
		}
		in1.close();	
	} catch (IOException e) {
		e.printStackTrace();
	}
	List<String> res = new ArrayList<String>();
	for (int ii=0;ii<tobelowered.size();ii++){
		res.add(Integer.toString(tobelowered.get(ii)));
	}
	FileUtils.writeLines(filetobelowered, Constants.UTF8, res, "\n");
	System.exit(0);*/


}
