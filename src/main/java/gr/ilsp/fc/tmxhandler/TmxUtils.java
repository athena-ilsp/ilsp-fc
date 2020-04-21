package gr.ilsp.fc.tmxhandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.loomchild.maligna.coretypes.Alignment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.aligner.factory.BilingualScoredTmxParser;
import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.corpora.BilingualTmxMetashareDescriptor;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsNumStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsRatioStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsScoreStats;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.nlp.commons.Constants;

public class TmxUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmxUtils.class);
	private static LangDetector langDetector;
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

	//private static int MIN_TOKEN_LEN=3;	//tokens with less than MIN_TOKEN_LEN are excluded
	//private static float QUANT_RATE= (float) 0.01;
	//private static int QAUNT_DEFAULT=1; // quantization interval 


	public static void main(String[] args) throws Exception{  //throws Exception
		File innfile  = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/2606/source");
		File[] xlifs = innfile.listFiles();
		List<String> l21 = new ArrayList<String>();
		List<String> l22 = new ArrayList<String>();
		for (int ii=0;ii<xlifs.length;ii++){
			List<String> lines = FileUtils.readLines(xlifs[ii]);
			for (String line:lines){
				line = line.trim();
				if (line.startsWith("<source>"))
					l21.add(line.substring(8, line.length()-9));
				if (line.startsWith("<target>"))
					l22.add(line.substring(8, line.length()-9));
			}
		}
		System.out.println(l21.size());
		System.out.println(l22.size());
		List<String> l12 = new ArrayList<String>();
		for (int ii=0;ii<l21.size();ii++){
			l12.add(l21.get(ii)+Constants.TAB+l22.get(ii));
		}
		File result = new File(innfile.getAbsolutePath()+".en-pl");
		FileUtils.writeLines(result, Constants.UTF8, l12, "\n");
		List<SegPair> fpairs1 = txt2tmx(result, Constants.TAB);
		List<ILSPAlignment> faalignments1 = segpair2ILSPAlignment(fpairs1);
		String[] fllangs11 = new String[2]; fllangs11[0] = "en"; fllangs11[1] = "pl";
		generateCorpus(faalignments1, fllangs11, "", new File(result.getAbsolutePath()+ "_u.tmx"),"", "", "");
		
		System.exit(0);
		
		
		File fffile1 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_1801-1819/source/Al Nashiri impotriva Romaniei.tmx.en");
		File fffile2 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_1801-1819/source/Al Nashiri impotriva Romaniei.tmx.ro");
		File fffile3 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_1801-1819/source/Al Nashiri impotriva Romaniei.tmx.en-ro");
		List<SegPair> fpairs = txts2tmx(fffile1, fffile2);
		List<ILSPAlignment> faalignments = segpair2ILSPAlignment(fpairs);
		String[] fllangs1 = new String[2]; fllangs1[0] = "en"; fllangs1[1] = "ro";
		generateCorpus(faalignments, fllangs1, "", new File(fffile3.getAbsolutePath()+ "_u.tmx"),"", "", "");
		System.exit(0);
		
		
		List<String> lines = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_2510/validated-1.txt"));
		List<String> newlines = new ArrayList<String>();
		for (String line:lines){
			if (line.isEmpty())
				continue;
			if (line.startsWith("\"") | line.startsWith(","))
				continue;
			if (line.startsWith("1") |line.startsWith("2") |line.startsWith("3") |line.startsWith("4")| line.startsWith("5")
					| line.startsWith("6")| line.startsWith("7") | line.startsWith("8") | line.startsWith("9") 
					|line.startsWith("0"))
				continue;
			newlines.add(line);
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_2510/validated-1.txt"), newlines);
		System.exit(0);
		
		/*List<String> a23781 = FileUtils.readLines(new File("C:/Users/vpapa/Downloads/archive_2378/result/eng-hrv_corpus.en"));
		List<String> a23782 = FileUtils.readLines(new File("C:/Users/vpapa/Downloads/archive_2378/result/eng-hrv_corpus.hr"));
		
		List<String> a23761 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/txts/Ministarstvo_part1/eng-hrv_corpus.en"));
		List<String> a23762 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/txts/Ministarstvo_part1/eng-hrv_corpus.hr"));
		List<String> a23791 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/txts/Ministarstvo_part2/eng-hrv_corpus.en"));
		List<String> a23792 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/txts/Ministarstvo_part2/eng-hrv_corpus.hr"));		
		
		int countq=0;
		for (int ii=0;ii<a23781.size();ii++){
			String a = a23781.get(ii);
			String b = a23782.get(ii);
			for (int jj=0;jj<a23761.size();jj++){
				String c = a23761.get(jj);
				String d = a23762.get(jj);
				if (a.equals(c) && b.equals(d)){
					System.out.println(a +b + c+ d);
					countq++;
				}
			}
			for (int jj=0;jj<a23791.size();jj++){
				String e = a23791.get(jj);
				String f = a23792.get(jj);
				if (a.equals(e) && b.equals(f)){
					System.out.println(a +b + e+ f);
					countq++;
				}
			}
			
			
		}
		System.out.println(countq);
		System.exit(0);*/
		
		/*tmx2txts2(new File("C:/Users/vpapa/Downloads/archive_2377/Regionalno EN-HR.tmx"), "en", "hr");
		tmx2txts2(new File("C:/Users/vpapa/Downloads/archive_2378/Ministarstvo poljoprivrede EN-HR.tmx"), "hr", "en");
		System.exit(0);*/
		
		/*cleanTXTs(new File("C:/Users/vpapa/Downloads/archive_2377/txts"));
		cleanTXTs(new File("C:/Users/vpapa/Downloads/archive_2378/txts"));
		System.exit(0);*/
		
		/*tmx2txts2(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/Ministarstvo poljoprivrede HR-EN_final.tmx.UTF-8"), "en", "hr");
		tmx2txts2(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/Regionalno HR-EN_final.tmx.UTF-8"), "hr", "en");
		System.exit(0);*/
		
		/*cleanTXTs(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/new_version/Ciklopea_HR-EN_TMs/txts"));
		System.exit(0);*/
		
		fffile1 = new File("C:/Users/vpapa/Downloads/archive_2377/txts/Regionalno EN-HR.tmx.en.cl");
		fffile2 = new File("C:/Users/vpapa/Downloads/archive_2377/txts/Regionalno EN-HR.tmx.hr.cl");
		fffile3 = new File("C:/Users/vpapa/Downloads/archive_2377/txts/Regionalno EN-HR.tmx.en-hr.cl");
		fpairs = txts2tmx(fffile1, fffile2);
		faalignments = segpair2ILSPAlignment(fpairs);
		fllangs1 = new String[2]; fllangs1[0] = "en"; fllangs1[1] = "hr";
		generateCorpus(faalignments, fllangs1, "", new File(fffile3.getAbsolutePath()+ "_u.tmx"),"", "", "");
		System.exit(0);
		
		
	
		
		

		File ffile = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/si.program-podezelja/en-sl.clean1.15.tsv");
		String llang1 = "en", llang2 = "sl";
		File file1 = new File(ffile.getAbsolutePath()+"."+llang1);
		File file2 = new File(ffile.getAbsolutePath()+"."+llang2);
		List<SegPair> pairs = txts2tmx(file1, file2);
		List<ILSPAlignment> aalignments = segpair2ILSPAlignment(pairs);
		String[] llangs1 = new String[2]; llangs1[0] = llang1; llangs1[1] = llang2;
		generateCorpus(aalignments, llangs1, "", new File(ffile.getAbsolutePath()+ "_u.tmx"),"", "", "");
		System.exit(0);

		String[] targetedLangs = {"en", "nb", "et", "cs", "da", "fi", "hu", "nl", "sv", "el", "bg", "ro", "sl", "pl", "pt", "de", "es", "fr", "it" };

		langDetector = LangDetectUtils.loadLangDetectors(targetedLangs,"langdetect");

		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2nb.text.tmx"), "en", "nb");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2et.text.tmx"), "en", "et");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2cs.text.tmx"), "en", "cs");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2da.text.tmx"), "en", "da");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2fi.text.tmx"), "en", "fi");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2hu.text.tmx"), "en", "hu");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2nl.text.tmx"), "en", "nl");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2sv.text.tmx"), "en", "sv");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2el.text.tmx"), "en", "el");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2bg.text.tmx"), "en", "bg");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2ro.text.tmx"), "en", "ro");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2sl.text.tmx"), "en", "sl");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2pl.text.tmx"), "en", "pl");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2pt.text.tmx"), "en", "pt");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.de2en.text.tmx"), "de", "en");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.es2en.text.tmx"), "es", "en");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.fr2en.text.tmx"), "fr", "en");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.it2en.text.tmx"), "it", "en");

		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2de.text.tmx"), "en", "de");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2es.text.tmx"), "en", "es");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2fr.text.tmx"), "en", "fr");
		parseWikimedia(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/task3/wikimedia/sources/cx-corpora.en2it.text.tmx"), "en", "it");
		System.exit(0);


		File f1 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_1775/en_terms.txt");
		File f2 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_1775/sl_terms.txt");
		File f3 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_1775/en-sl_terms.txt");
		List<String> l1 = FileUtils.readLines(f1, Constants.UTF8);
		List<String> l2 = FileUtils.readLines(f2, Constants.UTF8);
		List<String> l3 = new ArrayList<String>();
		if (l1.size()!=l2.size()){
			System.out.println("check list size");
			System.exit(0);
		}
		for (int ii=0;ii<l1.size();ii++){
			//String[] t1 = l1.get(ii).split(",");
			//String[] t2 = l2.get(ii).split(",");
			//for (int jj=0;jj<t1.length;jj++){
			//	String temp= t1[jj].trim();
			//	for (int kk=0;kk<t2.length;kk++){
			//		l3.add(temp+"\t"+t2[kk].trim());
			//	}
			//}
			String t1 = l1.get(ii).trim();
			String t2 = l2.get(ii).trim();
			if (t1.equals(t2)){
				System.out.println(t1);
				continue;
			}
			if (t1.isEmpty() || t2.isEmpty()){
				System.out.println(t1);
				System.out.println(t2);
				continue;
			}
			String t = t1+"\t"+t2;
			if (l3.contains(t))
				continue;
			l3.add(t);
		}

		FileUtils.writeLines(f3, Constants.UTF8, l3,"\n");
		System.exit(0);

		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/_1997-en-fr/eng-fra.tmx"), "eng", "fra");
		System.exit(0);

		File infile = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/_1999-en-it/txt");
		String[] langs1 = new String[2];
		langs1[0] = "it"; langs1[1] = "en";
		File newfile1 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/_1999-en-it/txt/eng-ita.tmx.ita.txt");
		File newfile2 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/_1999-en-it/txt/eng-ita.tmx.eng.txt");
		List<SegPair> segpairslist1 = txts2tmx(newfile1, newfile2);
		List<ILSPAlignment> alignments = segpair2ILSPAlignment(segpairslist1);
		generateCorpus(alignments, langs1, "", new File(FilenameUtils.concat(infile.getAbsolutePath(), "_g.tmx")),"", "", "");
		System.exit(0);

		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/dan-eng.tmx"), "eng", "dan");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/deu-eng.tmx"), "eng", "deu");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/ces-eng.tmx"), "eng", "ces");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-nld.tmx"), "eng", "nld");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-spa.tmx"), "eng", "spa");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-fra.tmx"), "eng", "fra");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-ita.tmx"), "eng", "ita");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-por.tmx"), "eng", "por");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-hun.tmx"), "eng", "hun");
		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/pgv-non-ilsp-region/pgv/eng-swe.tmx"), "eng", "swe");
		System.exit(0);


		String[] langs11 = new String[2];
		langs11[0] = "en"; langs11[1] = "el";
		File infile11 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/el-sub/1968/final");
		File newfile11 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/el-sub/1968/final/txt.en");
		File newfile21 = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/el-sub/1968/final/txt.el");
		List<SegPair> segpairslist11 = txts2tmx(newfile11, newfile21);
		List<ILSPAlignment> alignments1 = segpair2ILSPAlignment(segpairslist11);
		generateCorpus(alignments1, langs11, "", new File(FilenameUtils.concat(infile11.getAbsolutePath(), "_g.tmx")),"", "", "");
		System.exit(0);

		tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_1160-1166/eng-ron_corpus.tmx"), "en", "ro");
		System.exit(0);

		File infilea = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_790-834/source/eng-pol.tmx");
		List<SegPair> segpairs1a = tmx2tmx(infilea);
		List<ILSPAlignment> alignmentsa = segpair2ILSPAlignment(segpairs1a);
		String[] langs1a = new String[2]; langs1a[0]="en"; langs1a[1] = "hr";
		generateCorpus(alignmentsa, langs1a, "", new File(FilenameUtils.concat(infilea.getAbsolutePath(), "_g.tmx")),"", "", "");
		System.exit(0);

		List<String> el1 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_462-954/processed/results-attributes/ceval/ell-eng_corpus.el"), Constants.UTF8);
		List<String> en1 =FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_462-954/processed/results-attributes/ceval/ell-eng_corpus.en"), Constants.UTF8);

		List<String> el2 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_719-941/processed/results-attributes/ceval/_g.tmx.el"), Constants.UTF8);
		List<String> en2 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_719-941/processed/results-attributes/ceval/_g.tmx.en"), Constants.UTF8);
		int count=0;
		for (int ii=0;ii<el2.size();ii++){
			String el = el2.get(ii);
			String en = en2.get(ii);
			for (int jj=0;jj<el1.size();jj++){
				if (el1.get(jj).equals(el) && en1.get(jj).equals(en)){
					count++;
					break;
				}
			}
		}
		System.out.println(count+ " \tfrom\t" + el2.size());

		System.exit(0);

		tmx2txts2(new File("C:/Users/vpapa/ELRC/subcontracts/691__en-pl_cleared/source/NFZ_tm.tmx"), "pl", "en");
		System.exit(0);


		File filet = new File("C:/Users/vpapa/ELRC/tld/pl/crawls/www-paih-gov-pl/new-tmxmerge_new_www-paih-gov-pl_eng-pol_sample_en.txt");
		List<String> samples3 = FileUtils.readLines(filet, Constants.UTF8);
		List<String> newsamples = new ArrayList<String>();
		int countert=1;
		for (String sample:samples3){
			sample = "<seg id=\""+countert+"\">"+sample+"</seg>";
			countert++;
			newsamples.add(sample);
		}
		FileUtils.writeLines(filet, Constants.UTF8, newsamples,"\n");
		System.exit(0);

		String[] pllangs = new String[1];
		pllangs[0]="pl";
		int counterfalselang=0;
		LangDetector langDetector = LangDetectUtils.loadLangDetectors(pllangs,"langdetect");
		List<String> enlines = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/www-paih-gov-pl/new-tmxmerge_new_www-paih-gov-pl_eng-pol.en"), Constants.UTF8);
		//List<String> enlines = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/pl/tld_en-pl_tld_en-pl_eng-pol.en"), Constants.UTF8);
		List<String> langfalses = new ArrayList<String>();
		for (int ii=0;ii<enlines.size();ii++){
			/*String lang = langDetector.detect(pllines.get(ii));
			if (!lang.equals("pol")){
				System.out.println(lang+"\t"+pllines.get(ii));
				langfalses.add(lang+"\t"+pllines.get(ii));
				counterfalselang++;
				//continue;
			}*/
			String lang = langDetector.detect(enlines.get(ii));
			if (!lang.equals("eng")){
				System.out.println(lang+" :\t\t"+enlines.get(ii));
				langfalses.add(lang+"\t"+enlines.get(ii));
				counterfalselang++;
			}
		}
		System.out.println(counterfalselang);
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/www-paih-gov-pl/en.langfalses"), Constants.UTF8, langfalses,"\n");
		System.exit(0);

		String line2;
		int countergov=0;
		BufferedReader bufferreader = new BufferedReader(new FileReader("C:/Users/vpapa/ELRC/tld/pl/tld_en-pl_tld_en-pl_eng-pol.tmx"));
		line2 = bufferreader.readLine();
		while (line2 != null) {     
			if (line2.contains("l1-url") && line2.contains(".gov.")){
				System.out.println(line2);
				countergov++;
			}
			line2 = bufferreader.readLine();
		}
		System.out.println(countergov);
		System.exit(0);

		List<SegPair> tus = TMXHandlerUtils.getTUsFromTMX(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/152/source/law_ell-eng.tmx"), new ArrayList<String>(), "en", "el");
		List<String> sites = new ArrayList<String>();
		for (SegPair tu:tus){
			String temphost1="", temphost2="";
			try {
				temphost1 = new URL(tu.l1url).getHost();
				temphost2 = new URL(tu.l2url).getHost();
			} catch (MalformedURLException e) {
				continue;
				//LOGGER.debug("no protocol of acquired files " +temphost1 + " or " + temphost2);			//e.printStackTrace();
			}
			//if (noannot && !align.getInfo().isEmpty())
			//	continue;
			if (!sites.contains(temphost1))
				sites.add(temphost1);
			if (!sites.contains(temphost2))
				sites.add(temphost2);
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/152/source/sites.txt"), Constants.UTF8, sites,"\n");
		System.exit(0);


		String[] ext = new String[1];
		ext[0] = "tmx";
		File indir = new File("C:/Users/vpapa/ELRC/tld/pl/crawls");
		List<File> files = (List<File>) FileUtils.listFiles(indir, ext, false) ;
		String lang1 = "eng";
		String lang2 = "pl";
		for (File file:files){
			List<SegPair> segpairs =  TMXHandlerUtils.getTUsFromTMX(file, false, lang1,  lang2);
			double t = 0;
			if (segpairs.size()<10)
				t = segpairs.size();
			else
				t = Math.round(segpairs.size()*0.1);
			int sampleSize = (int)Math.min(t,300);
			System.out.println(sampleSize+"\t"+segpairs.size());
			File temp = new File(FilenameUtils.concat(indir.getAbsolutePath(),"clean_samples"));
			File samplefile = new File(FilenameUtils.concat(temp.getAbsolutePath(),file.getName()+".csv"));
			TMXHandlerUtils.generateSampleSegs(segpairs, sampleSize, samplefile);
		}

		System.exit(0); 





		File tmxFile = new File("C:/Users/vpapa/ELRC/tld/hr/crawls/webcorpus_v1_ILSP-FC_eng-hrv.tmx");
		List<SegPair>  totalTUs = GetTMXsubset.getTUsFromTMX(tmxFile, "eng", "hrv"); 
		List<SegPair>  newTUs = new ArrayList<SegPair>();
		for (SegPair segpair:totalTUs){
			if (segpair.annot.isEmpty()){
				newTUs.add(segpair);
			}
		}



		int total_counter=0, clean_counter=0;
		int dups_counter=0, neardups_counter=0, iden_counter=0, empty_counter=0, len_counter=0, ratio_counter = 0, num_counter=0;
		int email_counter=0, url_counter=0;
		int mix_counter=0;


		BufferedReader br = new BufferedReader(new FileReader(tmxFile));
		String line1;
		while ((line1 = br.readLine()) != null) {
			if (line1.contains("type=\"info\">")){
				total_counter++;
				if (line1.contains("type=\"info\"><")){
					clean_counter++;
					continue;
				}
				if (line1.contains("type=\"info\">duplicate</prop>"))
					dups_counter++;
				if (line1.contains("type=\"info\">near-duplicate</prop>"))
					neardups_counter++;
				if (line1.contains("type=\"info\">non-letters</prop>"))
					empty_counter++;
				if (line1.contains("type=\"info\">equal TUVs</prop>"))
					iden_counter++;
				if (line1.contains("type=\"info\">different numbers in TUVs</prop>"))
					num_counter++;
				if (line1.contains("type=\"info\">e-mail address</prop>"))
					email_counter++;
				if (line1.contains("type=\"info\">url</prop>"))
					url_counter++;
				if (line1.contains("type=\"info\">very short segments, shorter than ") && !line1.contains("|"))
					len_counter++;
				if (line1.contains("type=\"info\">charlength ratio of TUVs is lower than ") && !line1.contains("|"))
					ratio_counter++;
				if (line1.contains("|"))
					mix_counter++;
			}
		}
		br.close();
		System.out.println("total:\t"+ total_counter);
		System.out.println("clean:\t"+ clean_counter);
		System.out.println("duplicate:\t"+ dups_counter);
		System.out.println("near-duplicate:\t"+ neardups_counter);
		System.out.println("non-letters:\t"+ empty_counter);
		System.out.println("equal TUVs:\t"+ iden_counter);
		System.out.println("different numbers in TUVs:\t"+ num_counter);
		System.out.println("e-mail address:\t"+ email_counter);
		System.out.println("url:\t"+ url_counter);
		System.out.println("very short segments:\t"+ len_counter);
		System.out.println("charlength ratio of TUVs is lower than:\t"+ ratio_counter);
		System.out.println("more than 1 types of annotation:\t"+ mix_counter);
		System.exit(0);


		List<String> wholetmx = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/webcorpus_v1_ILSP-FC_eng-hrv.tmx"), Constants.UTF8);
		int counter=0;
		for (String line:wholetmx){
			if (line.contains("<info><"))
				counter++;
		}
		System.out.println(counter);

		System.exit(0);

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

		List<String> res1_el =   FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt1_ILSP-FC_ell-eng.el"), Constants.UTF8);
		List<String> res1_en =   FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt1_ILSP-FC_ell-eng.en"), Constants.UTF8);
		List<String> res04_el =  FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt04_ILSP-FC_ell-eng.el"), Constants.UTF8);
		List<String> res04_en =  FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt04_ILSP-FC_ell-eng.en"), Constants.UTF8);
		//List<String> res1_all =  FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-EL/ejustice/resfilt04_ILSP-FC_ell-eng.en"), Constants.UTF8);


		for (int ii=0;ii<res1_el.size();ii++){
			if (res04_el.contains(res1_el.get(ii)) && res04_en.contains(res1_en.get(ii)) )
				continue;
			System.out.println(res1_el.get(ii)+"\t"+res1_en.get(ii));
		}

		System.exit(0);

		List<String> websites = null;
		try {
			websites = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-HR/total_run/output_NEWALIGN_ILSP-FC_eng-hrv.sites.txt"), Constants.UTF8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//List<SegPair> tus = TMXHandlerUtils.getTUsFromTMX(new File("C:/Users/vpapa/ELRC/EN-HR/total_run/output_NEWALIGN_ILSP-FC_eng-hrv.tmx"), new ArrayList<String>(), "en", "hr");


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


		/*List<String> lines = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/merged.txt"), Constants.UTF8);
		List<String> bg_lines = new ArrayList<String>();
		List<String> en_lines = new ArrayList<String>();

		for (String line:lines){
			String[] temp = line.split("\t");
			if (temp.length<2)
				continue;
			bg_lines.add(ContentNormalizer.normalizeText(temp[0]));
			en_lines.add(ContentNormalizer.normalizeText(temp[1]));
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/merged_bg.txt"), Constants.UTF8, bg_lines,"\n");
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/190/TELECOMMUNICATIONS AND BROADBAND/merged_en.txt"), Constants.UTF8, en_lines,"\n");
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


		//tmx2txts(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_710-821/821.en-hr.tmx"), "en", "hr");
		processTatoeba(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/tatoeba/_1028/tatoeba.org.2018.en-fr.tmx"), "en", "fr");
		System.exit(0);
		process988(); //from "dirty" ILSP-like tmx to clean ILSP-like tmx
		System.exit(0);

		process2333( new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/2333-/2333_.txt"));
		System.exit(0);

		process1803(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/_1803-1957-1958/processed/IER-Banking.tbx"), "en", "ro");
		System.exit(0);

		process2305(new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/toD3-4-2/2305-2315/source/aligned/culture-C-all-sk-en-aligned.txt"), "sk", "en");
		System.exit(0);
	}


	private static void cleanTXTs(File indir) throws IOException {

		String a = "<bpt", b = "</bpt>", c = "<ept", d = "</ept>", e="&lt;JUMP&gt;", f ="<ph", g = "</ph>" ;
		File[] files = indir.listFiles();
		for (File file:files){
			List<String> lines = FileUtils.readLines(file);
			
			List<String> newlines = new ArrayList<String>();
			String text="";
			int ind1, ind2, jj=-1;
			for (String line:lines){
				jj++;
				line = line.replaceAll(e, " ");
				String temp = line;
				for (int ii=1;ii<1000;ii++){
					ind1 = temp.indexOf(a);
					ind2 = temp.indexOf(b);
					if (ind1<ind2 && ind1>=0 &&ind2>=0){
						temp = temp.substring(0, ind1)+ temp.substring(ind2+b.length());
						temp = temp.trim();
					
					}else{
						break;
					}
				}
				for (int ii=1;ii<1000;ii++){
					ind1 = temp.indexOf(c);
					ind2 = temp.indexOf(d);
					if (ind1<ind2 && ind1>=0 &&ind2>=0){
						temp = temp.substring(0, ind1)+ temp.substring(ind2+d.length());
						temp = temp.trim();
					}else{
						break;
					}
				}
				for (int ii=1;ii<1000;ii++){
					ind1 = temp.indexOf(f);
					ind2 = temp.indexOf(g);
					if (ind1<ind2 && ind1>=0 &&ind2>=0){
						temp = temp.substring(0, ind1)+ temp.substring(ind2+g.length());
						temp = temp.trim();
					}else{
						break;
					}
				}
				text = temp.replaceAll("\t", Constants.SPACE);
				text = text.replaceAll("(\\s){2,}", Constants.SPACE);
				System.out.println(line);
				System.out.println(text);
				newlines.add(text);
			}
			System.out.println(lines.size());
			System.out.println(newlines.size());
			FileUtils.writeLines(new File(file.getAbsolutePath()+".cl"), newlines, "\n");
		}
	}


	private static void process2333(File infile1) throws IOException {
		List<String> entries = FileUtils.readLines(infile1, Constants.UTF8);
		List<String> res=  new ArrayList<String>();
		//int count11=0;
		for (String entry:entries){
			if (entry.contains("̶") )
				continue;
			String[] temp = entry.split("\t");
			temp[0] = temp[0].replaceAll("\\(.*\\)", "").trim();
			temp[0]  = temp[0] .replaceAll("(\\s){2,}", Constants.SPACE);
			temp[1] = temp[1].replaceAll("\\(.*\\)", "").trim();
			temp[1]  = temp[1] .replaceAll("(\\s){2,}", Constants.SPACE);
			String hr = temp[0].trim();
			String en = temp[1].trim();
			String[] hrl = hr.split(";");
			String[] enl = en.split(";");
			List<String> hrs = Arrays.asList(hrl);
			List<String> ens = Arrays.asList(enl);

			for (int ii=0;ii<hrs.size();ii++){
				String t =hrs.get(ii).trim(); 
				if (t.length()>3){
					if (ens.contains(t))
						continue;
					for (int jj=0;jj<ens.size();jj++){
						String tt = ens.get(jj).trim();
						if (tt.length()>3){
							if (!t.equals(tt)){
								String aa = t+"\t"+tt;
								if (!res.contains(aa)){
									res.add(aa);
								}
							}
						}
					}
				}
			}

		}
		//System.out.println(count11);
		FileUtils.writeLines(new File(infile1.getAbsolutePath()+".ready"), Constants.UTF8, res, "\n");
		System.exit(0);


	}


	private static void laserout2txts(File file, String lang1, String lang2) throws IOException {
		List<String> lines = FileUtils.readLines(file, Constants.UTF8);
		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		for (String line:lines){
			String[] temp = line.split("\t");
			l1.add(temp[1].trim());
			l2.add(temp[2].trim());
		}
		FileUtils.writeLines(new File(file.getAbsolutePath()+"."+lang1), Constants.UTF8, l1, "\n");
		FileUtils.writeLines(new File(file.getAbsolutePath()+"."+lang2), Constants.UTF8, l2, "\n");
	}


	private static void process1803(File file, String l1, String l2) throws IOException {
		List<String> lines = FileUtils.readLines(file, Constants.UTF8);
		List<String> res = new ArrayList<String>(); 
		String t1 = "<langSet xml:lang=\""+l1+"\">";
		String t2 = "<langSet xml:lang=\""+l2+"\">";
		String start = "<langSet xml:lang="; 
		boolean found = true;
		for (String line:lines){

			if (line.contains(start)){
				if (!line.contains(t1) && !line.contains(t2)){
					found =false;
				}else{
					found = true;
					res.add(line);
				}
			}else{
				if (found){
					res.add(line);
				}
			}
		}
		FileUtils.writeLines(new File(file.getAbsolutePath()+".bil"), Constants.UTF8, res, "\n");
	}


	private static void process2305(File file, String lang1, String lang2) throws Exception {
		List<String> lines = FileUtils.readLines(file, Constants.UTF8);
		List<String> lines1 = new ArrayList<String>();
		List<String> lines2 = new ArrayList<String>();
		for (String line:lines){
			String[] temp = line.split("\t");
			lines1.add(temp[0].trim());
			lines2.add(temp[1].trim());
		}
		File file1 = new File(file.getAbsolutePath()+"."+lang1);
		File file2 = new File(file.getAbsolutePath()+"."+lang2);
		FileUtils.writeLines(file1, Constants.UTF8, lines1, "\n");
		FileUtils.writeLines(file2, Constants.UTF8, lines2, "\n");

		List<SegPair> segpairslist = txts2tmx(file1, file2);
		List<ILSPAlignment> als = segpair2ILSPAlignment(segpairslist);

		String[] langs1a = new String[2]; langs1a[0]=lang1; langs1a[1] = lang2;
		generateCorpus(als, langs1a, "", new File(file.getAbsolutePath()+".tmx"),"", "", "");

	}


	private static void parseWikimedia(File infile, String l1, String l2) throws IOException {
		//BufferedWriter out1= new BufferedWriter(new FileWriter(new File(infile.getAbsolutePath()+".log"))); 
		BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".log"),Constants.UTF8));

		LOGGER.info("-------\t"+infile.getName()+"\t----------");
		List<String> lines=  FileUtils.readLines(infile, Constants.UTF8);
		List<String> lines1= new ArrayList<String>();
		List<String> lines2= new ArrayList<String>();

		int id1 = 0, id2 = 0;
		int totalcounter = 0, paircounter=0;
		boolean found1 = false, found2 = false, tufound=false;
		String ntext1="", ntext2="", l11="", l22="";
		String ntext3 = "";
		//String string_key3="", string_key2="";
		for (int ii=0;ii<lines.size()-1;ii++){
			String line= lines.get(ii).trim();
			if (line.startsWith("<tu srclang=") && !tufound){
				id1=ii;
				tufound=true;
				continue;
			}
			if (line.equals("</tu>")){
				id2=ii;
				tufound=false;
				totalcounter++;
			}
			if (id1>0 && id2>0){
				tufound=false;
				found1 = false;
				found2 = false;
				String text1="", text2 = "", text3 = "";
				for (int jj=id1+1;jj<id2;jj++){
					String line1 = lines.get(jj).trim();
					String line11 = lines.get(jj+1).trim();
					String line12 = lines.get(jj+2).trim();
					if (line1.contains("<tuv xml:lang=\""+l1+"\">") && line11.contains("<prop type=\"origin\">source</prop>")){
						found1 = true;
						text1 = line12.substring(line.indexOf(">")+1, line12.lastIndexOf("<"));
						text1 = ContentNormalizer.unescapedhtml(text1);
						text1 = ContentNormalizer.normalizeText(text1);
						text1 = text1.replaceAll("\\[\\d\\]", Constants.SPACE);
						text1 = text1.replaceAll("\\[\\d\\d\\]", Constants.SPACE);
						text1 = text1.replaceAll("\\[\\d\\d\\d\\]", Constants.SPACE);
						text1 = text1.replaceAll("(\\s){2,}", Constants.SPACE).trim();
						l11 = ISOLangCodes.get2LetterCode(langDetector.detect(text1));
						ntext1 =  ContentNormalizer.normtext(text1);
						continue;
					}

					if (line1.contains("<tuv xml:lang=\""+l2+"\">") && line11.contains("<prop type=\"origin\">mt</prop>")){
						text3 = line12.substring(line.indexOf(">")+1, line12.lastIndexOf("<"));
						text3 = ContentNormalizer.unescapedhtml(text3);
						text3 = ContentNormalizer.normalizeText(text3);
						text3 = text3.replaceAll("\\[\\d\\]", Constants.SPACE);
						text3 = text3.replaceAll("\\[\\d\\d\\]", Constants.SPACE);
						text3 = text3.replaceAll("\\[\\d\\d\\d\\]", Constants.SPACE);
						text3 = text3.replaceAll("(\\s){2,}", Constants.SPACE).trim();
						ntext3 =  ContentNormalizer.normtext(text3);
						/*byte[] texthashkey3 = DedupUtils.calculateMD5(ntext3, MIN_TOKEN_LEN); //TODO should text be stemmed?
						string_key3="";
						for (int kk=0;kk<texthashkey3.length;kk++) {
							string_key3 += texthashkey3[kk];
						}*/
						continue;
					}

					if (line1.contains("<tuv xml:lang=\""+l2+"\">") && line11.contains("<prop type=\"origin\">user</prop>")){
						text2 = line12.substring(line.indexOf(">")+1, line12.lastIndexOf("<"));
						text2 = ContentNormalizer.unescapedhtml(text2);
						text2 = ContentNormalizer.normalizeText(text2);
						text2 = text2.replaceAll("\\[\\d\\]", Constants.SPACE);
						text2 = text2.replaceAll("\\[\\d\\d\\]", Constants.SPACE);
						text2 = text2.replaceAll("\\[\\d\\d\\d\\]", Constants.SPACE);
						text2 = text2.replaceAll("(\\s){2,}", Constants.SPACE).trim();

						l22 = ISOLangCodes.get2LetterCode(langDetector.detect(text2));
						ntext2 =  ContentNormalizer.normtext(text2);

						/*byte[] texthashkey2 = DedupUtils.calculateMD5(ntext2, MIN_TOKEN_LEN); //TODO should text be stemmed?
						string_key2="";
						for (int kk=0;kk<texthashkey2.length;kk++) {
							string_key2 += texthashkey2[kk];
						}
						 */
						found2 = true;
						break;
					}
				}
				if (found1 && found2 ){
					paircounter++;
					if ( ntext1.isEmpty() || ntext2.isEmpty()){
						//LOGGER.info(ii+"\t"+text1+"\t"+text2+"\tEMPTY");
						out1.write(ii+"\t"+text1+"\t"+text2+"\tEMPTY\n");
					}else{
						if (ntext1.equals(ntext2)){
							//LOGGER.info(ii+"\t"+text1+"\t"+text2+"\t EQUAL");
							out1.write(ii+"\t"+text1+"\t"+text2+"\tEQUAL\n");	
						}else{
							if (ntext3.equals(ntext2)){
								//LOGGER.info(ii+"\t"+text3+"\t"+text2+"\tMT");
								out1.write(ii+"\t"+text3+"\t"+text2+"\tMT\n");
							}else{
								if (TMXHandlerUtils.checkemail(text1, 0.4) || TMXHandlerUtils.checkemail(text2, 0.4)
										||	TMXHandlerUtils.checkurl(text1, 0.4) || TMXHandlerUtils.checkurl(text2, 0.4)
										|| ntext1.contains("wikimedia") || ntext2.contains("wikimedia")){
									//LOGGER.info(ii+"\t"+text1+"\t"+text2+"\tE-MAIL/URL");
									out1.write(ii+"\t"+text1+"\t"+text2+"\tE-MAIL/URL\n");
								}else{
									if ((double)ntext1.length()<=(double)text1.length()*0.7 || (double)ntext2.length()<=(double)text2.length()*0.7){
										//LOGGER.info(ii+"\t"+text1+"\t"+text2+"\tMANY NUMBERS/SYMBOLS");
										out1.write(ii+"\t"+text1+"\t"+text2+"\tMANY NUMBERS/SYMBOLS\n");
									}else{
										if (!l11.equals(l1) || !l22.equals(l2)){
											//LOGGER.info(ii+"\t"+text1+"\t"+text2+"\tNOT IN TARGETED LANGS");
											out1.write(ii+"\t"+text1+"\t"+text2+"\tNOT IN TARGETED LANGS\n");
										}
										else{
											lines1.add(text1);
											lines2.add(text2);
											id1=0;
											id2=0;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		out1.close();
		if (lines1.size()==lines2.size()){
			List<String> lines3 = new ArrayList<String>();
			for (int ii=0;ii<lines1.size();ii++){
				lines3.add(lines1.get(ii)+Constants.TAB+lines2.get(ii));
			}
			FileUtils.writeLines(new File(infile.getAbsolutePath()+"."+l1+"-"+l2), Constants.UTF8, lines3,"\n");
		}
		FileUtils.writeLines(new File(infile.getAbsolutePath()+"."+l1), Constants.UTF8, lines1,"\n");	
		FileUtils.writeLines(new File(infile.getAbsolutePath()+"."+l2), Constants.UTF8, lines2,"\n");
		LOGGER.info("total TUs:\t"+ totalcounter);
		LOGGER.info("valid TUs:\t"+ paircounter);
		LOGGER.info("passed TUs:\t"+ lines2.size());
	}


	private static void processTatoeba(File file, String lang1, String lang2) throws Exception {
		List<String> lines = FileUtils.readLines(file, Constants.UTF8);
		List<String> l1s = new ArrayList<String>();
		List<String> l2s = new ArrayList<String>();
		List<String> totalls = new ArrayList<String>();

		boolean tu=false, l1=false, l2 = false;
		for (String line:lines){
			line = line.trim();
			//System.out.println(line);
			if (line.equals("<tu>")){
				tu=true;
			}
			if (line.equals("</tu>")){
				tu=false;
			}
			if (tu){
				if (line.equals("<tuv xml:lang=\""+lang1+"\">")){
					l1 = true;
					l2 = false;
				}
				if (line.equals("<tuv xml:lang=\""+lang2+"\">")){
					l2 = true;
					l1 = false;
				}
				if (line.startsWith("<seg>")){
					if (l1)
						l1s.add(line.substring(5, line.length()-6));
					if(l2)	
						l2s.add(line.substring(5, line.length()-6));
				}
			}
			if (!tu){
				for (String l1p:l1s){
					for (String l2p:l2s){
						String tmp = l1p+"\t"+l2p;
						if (!totalls.contains(tmp))
							totalls.add(tmp);
						else{
							System.out.println(tmp);
						}
					}
				}
				l1s.clear();
				l2s.clear();
			}

		}
		File out=new File(file.getAbsolutePath()+".list");
		File out1=new File(out.getAbsolutePath()+".1");
		File out2=new File(out.getAbsolutePath()+".2");
		FileUtils.writeLines(out, Constants.UTF8, totalls,"\n");
		lines = FileUtils.readLines(out, Constants.UTF8);
		List<String> l1list = new ArrayList<String>();
		List<String> l2list = new ArrayList<String>();
		for (String line:lines){
			String[] parts = line.split("\t");
			l1list.add(parts[0]);
			l2list.add(parts[1]);
		}
		FileUtils.writeLines(out1, Constants.UTF8, l1list,"\n");
		FileUtils.writeLines(out2, Constants.UTF8, l2list,"\n");
		List<SegPair> segpairslist = txts2tmx(out1, out2);
		List<ILSPAlignment> als = segpair2ILSPAlignment(segpairslist);

		String[] langs1a = new String[2]; langs1a[0]=lang1; langs1a[1] = lang2;
		generateCorpus(als, langs1a, "", new File(out.getAbsolutePath()+".tmx"),"", "", "");
	}


	private static void tmx2txts2(File file, String l1, String l2) throws IOException {
		List<String> lines = FileUtils.readLines(file, Constants.UTF8);
		List<String> l1s = new ArrayList<String>();
		List<String> l2s = new ArrayList<String>();
		boolean t1=false;
		int l1c = 0, l2c = 0;
		String temp="";
		boolean rejected  = false;
		int counter = 0;
		for (String line:lines){
			line = line.trim();
			System.out.println(line);
			if (line.startsWith("<seg>"))
				counter++;
			else
				continue;
			if (counter % 2 == 0)
				l2s.add(line.substring(5, line.length()-6));
			else
				l1s.add(line.substring(5, line.length()-6));
		}
		FileUtils.writeLines(new File(file.getAbsolutePath()+"."+l1), Constants.UTF8, l1s,"\n");
		FileUtils.writeLines(new File(file.getAbsolutePath()+"."+l2), Constants.UTF8, l2s,"\n");
	}


	private static void generateCorpus(List<ILSPAlignment> alignmentList,  String[] langs,  String domain,  File outTMX,  String creationModeDescription, String organization, String lic ){
		BilingualCorpusInformation bilingualCorpusInfo= bilinfo( alignmentList,  langs,  domain,  outTMX,  creationModeDescription,organization,lic);
		String baseName = outTMX.getAbsolutePath();
		File baseNamef = new File(outTMX.getAbsolutePath());
		if (bilingualCorpusInfo!=null){

			TMXHandler.generateMergedTMX(outTMX, langs, bilingualCorpusInfo, new File(outTMX.getAbsolutePath()+".html"));

			LOGGER.info("Generating language files");
			TMXHandlerUtils.splitIntolangFiles(alignmentList, langs, baseNamef);

			int sampleSizeCe = (int)((double)alignmentList.size()*0.1);
			int sampleSize = Math.min(1500, sampleSizeCe);
			File samplefile = new File(baseName + SAMPLE);
			LOGGER.info("Generating sample file " + samplefile.getAbsolutePath());
			TMXHandlerUtils.generateSample(alignmentList, sampleSize, samplefile);

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName+ ".md.xml");
			LOGGER.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			bilingualTmxMetashareDescriptor.setMetadataLang("en");
			bilingualTmxMetashareDescriptor.run();	
		}else{
			LOGGER.info("No proper TUs found.");
		}
	}



	private static List<ILSPAlignment> segpair2ILSPAlignment(List<SegPair> segpairs){
		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		for (SegPair segpair:segpairs){
			ILSPAlignment alignment = new ILSPAlignment();
			alignment.addSourceSegment(segpair.seg1);
			alignment.addTargetSegment(segpair.seg2);
			alignment.setScore((float)segpair.score);
			alignment.setType(segpair.type);
			alignment.setLengthRatio(Double.toString(segpair.ratio));
			alignment.setInfo(segpair.annot);
			alignmentList.add(alignment);
		}
		return alignmentList;
	}


	private static void process988() {
		//List<String> info = new ArrayList<String>();
		String lang1 = "en", lang2 = "hr";
		File tmxFile = new File("C:/Users/vpapa/ELRC/ELRC-1_to_LOT3/_988-989/processed/output_www-mvep-hr_eng-hr_eng-hrv.tmx");
		//List<SegPair> segs = TMXHandlerUtils.getTUsFromTMX(tmxFile, info, lang1, lang2);
		List<SegPair> segs = TMXHandlerUtils.getTUsFromTMX(tmxFile, false, lang1, lang2);
		List<String> segsl1 = new ArrayList<String>();
		List<String> segsl2 = new ArrayList<String>();
		for (SegPair seg:segs){
			segsl1.add(seg.seg1);
			segsl2.add(seg.seg2);
		}
		List<ILSPAlignment> alignmentsa = segpair2ILSPAlignment(segs);
		String[] langs1a = new String[2]; langs1a[0]="en"; langs1a[1] = "hr";
		generateCorpus(alignmentsa, langs1a, "", new File(FilenameUtils.concat(tmxFile.getParent(), "_g.tmx")),"", "", "");

	}


	private static void process514(File infile, String[] langs, String domain, String creationModeDescription, String organization, String lic) {
		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		File outTMX = new File(FilenameUtils.concat(infile.getParent(),"output_"+langs[0]+"-"+langs[1]+".tmx"));
		File  baseNamef = new File(outTMX.getAbsolutePath());
		String baseName = baseNamef.getName();
		File outHTML = new File(FilenameUtils.concat(infile.getParent(),"output_"+langs[0]+"-"+langs[1]+".tmx.html"));

		List<SegPair> tus1 = TMXHandlerUtils.getTUsFromTMX(infile, langs[0], langs[1]);
		for (SegPair segpair:tus1){
			ILSPAlignment alignment = new ILSPAlignment();
			alignment.addSourceSegment(segpair.seg1);
			alignment.addTargetSegment(segpair.seg2);
			alignment.setScore((float)segpair.score);
			alignment.setType(segpair.type);
			alignment.setLengthRatio(Double.toString(segpair.ratio));
			alignment.setInfo(segpair.annot);
			alignmentList.add(alignment);
		}

		BilingualCorpusInformation bilingualCorpusInfo= bilinfo( alignmentList,  langs,  domain,  outTMX,  creationModeDescription,organization,lic);
		if (bilingualCorpusInfo!=null){

			TMXHandler.generateMergedTMX(outTMX, langs, bilingualCorpusInfo, outHTML);

			LOGGER.info("Generating language files");
			TMXHandlerUtils.splitIntolangFiles(alignmentList, langs, baseNamef);

			int sampleSizeCe = (int)((double)alignmentList.size()*0.1);
			int sampleSize = Math.min(1500, sampleSizeCe);
			File samplefile = new File(baseName + SAMPLE);
			LOGGER.info("Generating sample file " + samplefile.getAbsolutePath());
			TMXHandlerUtils.generateSample(alignmentList, sampleSize, samplefile);

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName+ ".md.xml");
			LOGGER.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			bilingualTmxMetashareDescriptor.setMetadataLang("en");
			bilingualTmxMetashareDescriptor.run();	
		}else{
			LOGGER.info("No proper TUs found.");
		}


	}

	private static void tmx2txts1(File file, String l1, String l2) throws IOException {
		List<String> lines = FileUtils.readLines(file, Constants.UTF8);
		List<String> l1s = new ArrayList<String>();
		List<String> l2s = new ArrayList<String>();
		boolean t1=false;
		int l1c = 0, l2c = 0;
		String temp="";
		boolean rejected  = false;
		for (String line:lines){
			line = line.trim();
			if (line.contains("type=\"info\">")){
				if (line.contains("duplicate") | line.contains("non-letters") | line.contains("equal TUVs")){
					rejected  = true;
				}else
					rejected  = false;
			}
			if (rejected)
				continue;
			if (line.startsWith("<seg>")){
				temp = line.substring(5, line.length()-6);
				if (!t1){
					t1=true;
					l1s.add(temp);
					l1c = l1c + temp.split(" ").length;
				}else{
					t1=false;
					l2s.add(temp);
					l2c = l2c + temp.split(" ").length;
				}
			}
		}
		FileUtils.writeLines(new File(file.getAbsolutePath()+"."+l1), Constants.UTF8, l1s,"\n");
		FileUtils.writeLines(new File(file.getAbsolutePath()+"."+l2), Constants.UTF8, l2s,"\n");
		System.out.println(l1+":\t"+l1c);
		System.out.println(l2+":\t"+l2c);
		System.out.println(l1c+l2c);
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
				ts = FileUtils.readLines(tmx, Constants.UTF8);
			} catch (IOException e) {
				e.printStackTrace();
			}
			boolean t1=false, t2=false; 
			for (String t:ts){
				t=t.trim();
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
		//System.out.println("000");
		return segpairslist;
	}

	private static List<SegPair> txts2tmx(File inFile1, File inFile2) throws Exception{
		List<String> ls1 = FileUtils.readLines(inFile1, Constants.UTF8);
		List<String> ls2 = FileUtils.readLines(inFile2, Constants.UTF8);
		if (ls1.size()!=ls2.size()){
			LOGGER.error("now equal nums of sens");
			return null;
		}
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
		List<String> segpairs = FileUtils.readLines(infile, Constants.UTF8);
		List<SegPair> segpairslist = new ArrayList<SegPair>();
		for (String segpair:segpairs){
			String[] temp = segpair.split(sep);
			String seg1 = ContentNormalizer.normalizeText(temp[0]);
			String seg2 = ContentNormalizer.normalizeText(temp[1]);
			if (seg1.equals("∅") || seg2.equals("∅") || seg1.isEmpty() || seg2.isEmpty()) 
				continue;
			double ratio = (double)seg1.length() / (double)seg2.length();
			//SegPair s = new SegPair(seg1, seg2, Double.parseDouble(temp[2].trim()),
			//		"", "","", "", "", "", ratio,"");
			SegPair s = new SegPair(seg1, seg2, 0.0,
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
		FileUtils.writeLines(l1file, Constants.UTF8, tokedsegs.get(0),"\n");
		File l2file= new File(tmxFile.getAbsolutePath()+TOK_EXT+lang2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for tokenized segments in "+lang2);
		FileUtils.writeLines(l2file, Constants.UTF8, tokedsegs.get(1),"\n");

		//--------------segments in 2 two text files (1 for each language)
		l1file= new File(tmxFile.getAbsolutePath()+Constants.DOT+lang1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for segments in "+lang1);
		FileUtils.writeLines(l1file, Constants.UTF8, segsl1,"\n");
		l2file= new File(tmxFile.getAbsolutePath()+Constants.DOT+lang2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for segments in "+lang2);
		FileUtils.writeLines(l2file, Constants.UTF8, segsl2,"\n");

		//----------segments and props in a text file (tab separated)
		File tsvFile = new File(tmxFile.getAbsolutePath()+Constants.DOT+LIST);
		List<String> outLines = TMXHandlerUtils.getSegsAndProps(segs, lang1, lang2);
		FileUtils.writeLines(tsvFile, Constants.UTF8, outLines,"\n");

		//------------Sample---------------------------
		List<String> sampleSegs = TMXHandlerUtils.getSampleSegs(segs, samplesize);
		File sampleFile = new File(tmxFile.getAbsolutePath()+SAMPLE+samplesize+TXT_EXT);
		FileUtils.writeLines(sampleFile, Constants.UTF8, sampleSegs,"\n");

		//---------------- Sites
		List<String> sites = TMXHandlerUtils.getSitesFromSegPairs(segs, false);
		File sitesFile = new File(tmxFile.getAbsolutePath()+SITES+samplesize+TXT_EXT);
		FileUtils.writeLines(sitesFile, Constants.UTF8, sites,"\n");
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
			if (TMXHandlerUtils.checkemail(segpair.seg1, 0.5) || TMXHandlerUtils.checkemail(segpair.seg2, 0.5)){
				//if (clean)
				//	continue;
				//if (!keepem)
				//	continue;
				if (info.isEmpty()){	info =  mes8;}		else{	info =  info + " | "+mes8;}	
			}
			if (TMXHandlerUtils.checkurl(segpair.seg1, 0.5) || TMXHandlerUtils.checkurl(segpair.seg2, 0.5)){
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
		FileUtils.writeLines(l1file, Constants.UTF8, l1segs,"\n");
		File l2file= new File(tmxFile.getAbsolutePath()+PUNCT_STR+l2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for segments in "+l2);
		FileUtils.writeLines(l2file, Constants.UTF8, l2segs,"\n");
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
		FileUtils.writeLines(evalFile, Constants.UTF8, outLines, "\n",true);
	}*/


	/*	*//**
	 * parses a tmxfile, get segments in l1 and l2, and writes these segments in two text files (1 for each language)
	 * @param tmxfile
	 * @param l1
	 * @param l2
	 */

	public static void tmx2txts(File tmxfile, String l1, String l2){
		File l1File = new File(FilenameUtils.concat(tmxfile.getParent(),tmxfile.getName()+"."+l1+TXT_EXT));
		File l2File = new File(FilenameUtils.concat(tmxfile.getParent(),tmxfile.getName()+"."+l2+TXT_EXT));
		List<String> sourcesegs = new ArrayList<String>();
		List<String> l1segs =  new ArrayList<String>();
		List<String> targetsegs = new ArrayList<String>();
		List<String> l2segs =  new ArrayList<String>();

		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(tmxfile.getAbsolutePath()), Constants.UTF8);
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
				l2segs.add(targetsegs.get(0));
		}
		try {
			FileUtils.writeLines(l1File, Constants.UTF8, l1segs,"\n");
			FileUtils.writeLines(l2File, Constants.UTF8, l2segs,"\n");
			String res = FileUtils.readFileToString(l1File, Constants.UTF8);
			res = res.replaceAll("\r\n", "\n");
			FileUtils.write(l1File, res, Constants.UTF8);
			res = FileUtils.readFileToString(l2File, Constants.UTF8);
			res = res.replaceAll("\r\n", "\n");
			FileUtils.write(l2File, res, Constants.UTF8);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


}
