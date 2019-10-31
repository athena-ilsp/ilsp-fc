package gr.ilsp.fc.utils;

import gr.ilsp.fc.bitext.BitextUtils;
import gr.ilsp.fc.bitext.Bitexts;
import gr.ilsp.fc.bitext.Bitexts.DocVector;
import gr.ilsp.fc.cleaner.CleanerUtils;
import gr.ilsp.fc.cleaner.CleanerUtils.ParsAttr;
import gr.ilsp.fc.dedup.DedupUtils;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Seg;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;
import net.loomchild.maligna.util.bind.tmx.Tuv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;


public class TempUtils {
	private static final Logger LOGGER = Logger.getLogger(TempUtils.class);
	private static final String appXMLext = ".xml";
	private static final String appHTMLext = ".html";
	private static final String appXMLHTMLext = ".xml.html";
	private static final String type_p ="p";
	private static final String HTTRACK1 = "<!-- Mirrored from"; 
	private static final String HTTRACK2 = "by HTTrack Website Copier";
	private static final String SITE = "site";
	private static final String INFO = "info";
	private static final String UNDERSCORE_STR ="_";
	private static final String COPYRIGHT_STR ="©";
	private static final String RIGHTS_STR ="all rights reserved";
	private static final List<String> rights_links= Arrays.asList("privacy statement", "οροι χρησης", "προστασία προσωπικών δεδομένων",
			"terms of use", "privacy policy" , "disclaimer");


	private static final String en_line = "<tuv xml:lang=\"EN-GB\">";
	private static final String sl_line = "<tuv xml:lang=\"SL\">";

	enum Sort { ASCENDING, DESCENDING; }

	enum Field {

		S(String.class), D(Double.class);

		private Class type;

		Field(Class<? extends Comparable> type) {
			this.type = type;
		}

		public Class getType() {
			return type;
		}
	}

	private static String replaceLanguages(String langstring) {
		String newlangStrinb = langstring.replaceAll("Croatian", "hr");
		newlangStrinb = newlangStrinb.replaceAll("English", "en");
		newlangStrinb = newlangStrinb.replaceAll("French", "fr");
		newlangStrinb = newlangStrinb.replaceAll("German", "de");
		newlangStrinb = newlangStrinb.replaceAll("Slovenian", "sl");
		newlangStrinb = newlangStrinb.replaceAll("Italian", "it");
		newlangStrinb = newlangStrinb.replaceAll("Hungarian", "hu");
		newlangStrinb = newlangStrinb.replaceAll("Dutch; Flemish", "nl");
		newlangStrinb = newlangStrinb.replaceAll("Bulgarian", "bg");
		newlangStrinb = newlangStrinb.replaceAll("Italian", "it");
		newlangStrinb = newlangStrinb.replaceAll("Latvian","lv");
		newlangStrinb = newlangStrinb.replaceAll("Modern Greek \\(1453-\\)","el");
		newlangStrinb = newlangStrinb.replaceAll("Polish","pl");
		newlangStrinb = newlangStrinb.replaceAll("Romanian; Moldavian; Moldovan","ro");
		newlangStrinb = newlangStrinb.replaceAll("Spanish; Castilian", "es");
		newlangStrinb = newlangStrinb.replaceAll("Czech", "cs");
		newlangStrinb = newlangStrinb.replaceAll("Estonian", "et");
		newlangStrinb = newlangStrinb.replaceAll("Finnish", "fi");
		newlangStrinb = newlangStrinb.replaceAll("Maltese","mt");
		newlangStrinb = newlangStrinb.replaceAll("Portuguese", "pt");
		newlangStrinb = newlangStrinb.replaceAll("Swedish","sw");
		newlangStrinb = newlangStrinb.replaceAll("Icelandic", "is");
		newlangStrinb = newlangStrinb.replaceAll("Irish", "ga");
		newlangStrinb = newlangStrinb.replaceAll("Lithuanian","lt");
		newlangStrinb = newlangStrinb.replaceAll("Norwegian","no");
		newlangStrinb = newlangStrinb.replaceAll("Danish","da");
		newlangStrinb = newlangStrinb.replaceAll("Slovak", "sk");
		newlangStrinb = newlangStrinb.replaceAll("Slovenian", "sl");
		//newlangStrinb = newlangStrinb.replaceAll(Constants.SPACE, "");
		newlangStrinb = newlangStrinb.replaceAll("\\|", "-");
		newlangStrinb = newlangStrinb.replaceAll(" - ", "-");
		return newlangStrinb.trim();
	}

	public static void main(String[] args) throws Exception {

		File[] files = new File("C:/Users/vpapa/ELRC/tld/el/crawls/commands/crawl").listFiles();
		String p = "C:/Users/vpapa/ELRC/tld/el/crawls/commands";
		for (File file:files){
			File destFile = new File(FilenameUtils.concat(p, "crawl_"+file.getName()+".sh"));
			FileUtils.copyFile(file, destFile);
		}
		System.exit(0);
		
		List<String> hots = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/el/crawls/hot_spots_sites.txt"), Constants.UTF8) ;
		List<String> hits = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/el/crawls/tld_filelist"), Constants.UTF8) ;
		List<String> hitsites = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/el/seeds_used.txt"), Constants.UTF8) ;
		
		String hot, hotsite;
		boolean hotfound1 = false, hotfound2 = false;
		for (String line:hots){
			if (line.contains("ibooked") || line.contains("carwall") || line.contains("blog")  || line.contains("webemail"))
				continue;
			String[] temp = line.split("\t");
			hot = temp[0];
			String[] parts  =hot.split("\\.");
			if (parts[0].equals("www"))
				hotsite = parts[1];
			else
				hotsite = parts[0];
			hotfound1 =false;
			for (String hit:hits){
				if (hit.contains(hotsite)){
					hotfound1 = true;
					break;
				}
			}
			hotfound2 =false;
			for (String hitsite:hitsites){
				if (hitsite.contains(hotsite)){
					hotfound2 = true;
					break;
				}
			}
			if (!hotfound1 && !hotfound2)
				System.out.println(line);
			
		}
		
		System.exit(0);
		
		
		//processTLDlist(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1912run_0-01_sites.txt"));
		List<String> websites1 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1912run_0-01_sites.txt_f.txt"), Constants.UTF8) ; 
		List<String> oldwebsites1 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_f.txt"), Constants.UTF8) ;
		boolean found1 = false;
		for (String website:websites1){
			found1 = false;
			for (String oldwebsite:oldwebsites1){
				if (oldwebsite.contains(website)){
					found1 =true;
					break;
				}
			}
			if (!found1)
				System.out.println(website);
		}
		System.exit(0);
		
		
		
		
		List<String> bucksites = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_b.txt"), Constants.UTF8);
		double thr_l = 0.9, thr_h = 1.2, thr_size_l = 0, thr_size_h = 1000; 
		String thr1 = Double.toString(thr_l);
		String thr2 = Double.toString(thr_h);
		String thr3 = Double.toString(thr_size_l);
		String thr4 = Double.toString(thr_size_h);
		String tld_sites = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_f.txt"), Constants.UTF8);
		String bucksele_sites = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_b_selected.txt"), Constants.UTF8);
		
		File filteredfile = new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_b_"+thr1+"-"+thr2+"_"+thr3+"-"+thr4+".txt");
		FileOutputStream fos = new FileOutputStream(filteredfile);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, Constants.UTF8));
		
		int counter=0;
		for (String bucksite:bucksites){
			if (bucksite.contains("wrzuta") || bucksite.contains("sex"))
				continue;
			String[] t = bucksite.split("\t");
			if (tld_sites.contains(t[0]) || bucksele_sites.contains(t[0]))
				continue;
			double n1 = Double.parseDouble(t[1].trim().split(Constants.SPACE)[1]);
			double n2 = Double.parseDouble(t[2].trim().split(Constants.SPACE)[1]);
			if (n1<thr_size_h && n2<thr_size_h && n1>thr_size_l && n2>thr_size_l){
				double n3 = n1/n2;
				if (n3<=thr_h && n3>=thr_l){
					counter++;
					bw.write(t[0]+"\n");
					//System.out.println(bucksite);
				}
			}
		}
		bw.close();
		System.exit(0);
		
		
		List<String> sites1 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/tmxs/sites.txt"), Constants.UTF8);
		File[] tmxs = new File("C:/Users/vpapa/ELRC/tld/pl/crawls/tmxs").listFiles();
		for (String site:sites1){
			boolean found = false;
			for (File tmx:tmxs){
				if (tmx.getName().contains(site)){
					found=true;
					String t = FileUtils.readFileToString(tmx, Constants.UTF8);
					String t1 = t.substring(0, t.indexOf(" TUs with no annotation,"));
					String res = t1.substring(t1.lastIndexOf(Constants.SPACE));
					System.out.println(site +"\t"+res );
				}
			}
			if (!found)
				System.out.println("eeeee "+ site);
		}
		
		System.exit(0);
		
		File indir = new File("C:/Users/vpapa/Downloads/archive (13)/culture_elrc-corpus_doc_ell_v2");
		String[] ext = new String[1];
		Map<String, Integer> sites = new HashMap<String, Integer>();
		ext[0]= "xml";
		List<File> xmlfiles = (List<File>) FileUtils.listFiles(indir, ext, false);
		for (File file:xmlfiles){
			System.out.println(file.getName());
			String eaddress = ReadResources.extractNodefromXML(file.getAbsolutePath(), "eAddress", false);
			URL url = null;
			
			 try {
				url = new URL(eaddress);
			} catch (MalformedURLException e) {
				continue;
				//url="";
			}
			
			String site = url.getAuthority();
			//System.out.println(site);
			int num=1;
			if (sites.containsKey(site))
				num = sites.get(site)+1;
			sites.put(site, num);
		}
		Set<String> si=sites.keySet();
		Iterator<String> si1 = si.iterator();
		String s;
		System.out.println("site\t# of webpages");
		while (si1.hasNext()){									
			 s= si1.next();			
			 System.out.println(s +"\t"+sites.get(s));
		}
		System.exit(0);
		
		//fromTMXtoTXTs_ENpart(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/webcorpus_v2_ILSP-FC_eng-hrv.tmx"));
		//System.exit(0);

		processTLDlist(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt"));
		//System.exit(0);
		List<String> res = processBuckList(new File("C://Users//vpapa//ELRC//2015_22.stats.joined"), new File("C://Users//vpapa//ELRC//2015_40.stats.joined"), ".pl", " pl ", " en ");
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_b.txt"), Constants.UTF8, res,"\n");
		System.exit(0);

		List<String> websites = getUniqueSites(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt"));
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/tld/pl/crawls/1655run_sites.txt_f.txt"), Constants.UTF8, websites,"\n");
		System.exit(0);

		fromTXTtoTXTs_ENpart(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/webcorpus_v2_ILSP-FC_eng-hrv.en"));
		System.exit(0);

		processTLDlist(new File("C:/Users/vpapa/ELRC/tld/el/crawls/1486runs_sites.txt"));
		System.exit(0);
		processBuckList(new File("C://Users//vpapa//ELRC//2015_22.stats.joined"), new File("C://Users//vpapa//ELRC//2015_40.stats.joined"), ".gr", " el ", " en ");
		System.exit(0);

		/*championlist(new File("C:/Users/vpapa/vpapa/ntinos_group.txt"));
		System.exit(0);*/
		checkparacrawl(new File("C:/Users/vpapa/ELRC/paracrawl/paracrawl.2016.es.clean"));
		System.exit(0);		
		getURLsfromTMX(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/merge2_default_hr/total_tld_en-hr_clean_default_ILSP-FC_eng-hrv.tmx"));
		System.exit(0);

		getURLsfromPairedCesDoc(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/merge2_default_hr/total_tld_en-hr_clean_default_ILSP-FC_eng-hrv.tmx"));
		System.exit(0);



		diffwebsites();
		System.exit(0);

		parselogfiles(new File("C:/Users/vpapa/ELRC/EN-HR/total_run/crawled/logs"));
		System.exit(0);

		fromCesDoc2xml(new File("C:/Users/vpapa/test/ILSP_20170203_113925/5beb49a6-30b8-484a-bc6b-8d6b581fd2e9/xml"));
		System.exit(0);



		getStatsOnELRCDatasets(new File("C://Users//vpapa//ELRC//20170406_ELRC_SHARE_OVERVIEW.txt"));
		System.exit(0);

		List<String> newcommands = getNewCommands(new File("C:/Users/vpapa/test/test_pdf/crawldirs_culture_el.txt"));
		System.exit(0);

		processSL_NAP();
		System.exit(0);

		getStatsFromDepthOutput(new File("C:/Users/vpapa/test/depth/audi/outputs"));
		System.exit(0);

		List<String> el = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/donated/greek_anti-corruption/non-annot_GR.txt"), Constants.UTF8);
		List<String> en = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/donated/greek_anti-corruption/non-annot_EN.txt"), Constants.UTF8);
		List<String> ent = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/donated/greek_anti-corruption/non-annot_TransEN.txt"), Constants.UTF8);
		List<String> eval = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/donated/greek_anti-corruption/testEL-EN.tsv"), Constants.UTF8);
		evalDonatedSet(el, en, ent, eval);
		System.exit(0);

		checkRightsInCesDoc(new File(args[0]));
		System.exit(0);

		fetchURL("http://www.ilsp.gr/el/infoprojects/meta");
		System.exit(0);

		cleanABUSets();
		System.exit(0);

		generateABUsets( new File(args[0]), new File(args[1]));
		System.exit(0);

		renameCESfiles(new File(args[0]),   new File(args[1]), args[2], "_");
		System.exit(0);

		processABUsets("C:/Users/vpapa/ABU/tourism/datasets/");
		System.exit(0);

		processABUsets1("C:/Users/vpapa/ABU/tourism/datasets/");
		System.exit(0);

		checkLvEn();
		System.exit(0);

		iroEval();
		System.exit(0);

		globalVoicesCheck(args);
		System.exit(0);

		iroSets();
		System.exit(0);

		try {
			several_specific_helpful_tasks();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@SuppressWarnings("resource")
	private static void fromTXTtoTXTs_ENpart(File txtfile) throws IOException {

		File dirtxtfile = new File(txtfile.getAbsolutePath()+"_txts");
		dirtxtfile.mkdir();
		int counter=0;
		BufferedReader br = new BufferedReader(new FileReader(txtfile));
		String line;
		File txtf ;
		while ((line = br.readLine()) != null) {
			line=line.trim();
			txtf = new File(FilenameUtils.concat(dirtxtfile.getAbsolutePath(),Integer.toString(counter)));
			FileUtils.writeStringToFile(txtf, line, Constants.UTF8);
			counter++;
		}
	}

	private static void fromTMXtoTXTs_ENpart(File file) {
		// TODO Auto-generated method stub

	}


	private static void processTLDlist(File file) throws IOException {
		List<String> sites = FileUtils.readLines(file, Constants.UTF8);
		List<String> finsites = new ArrayList<String>();
		List<String> nakedsites = new ArrayList<String>();
		String qq="";
		for (String site:sites){
			if (finsites.contains(site))
				continue;
			qq=site;
			if (qq.startsWith("www."))
				qq=qq.substring(4);
			if (nakedsites.contains(qq))
				continue;
			nakedsites.add(qq);
			finsites.add(site);
		}
		File res = new File(file.getAbsolutePath()+"_f.txt");
		FileUtils.writeLines(res, Constants.UTF8, finsites,"\n");
	}

	private static void iroSets() {
		List<String> res=new ArrayList<String>();	
		int group=4;
		try {
			List<String> tusinfo = FileUtils.readLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/eng-fra_culture_aupdih.csv"), Constants.UTF8);
			String[][] tus=new String[tusinfo.size()][2];
			//String[] itus=new String[tusinfo.size()];
			int counter=0;
			for (String tu:tusinfo){
				if (counter==0){
					counter++;
					continue;
				}
				String[] temp7 = tu.split("\t");
				tus[counter][0] = temp7[3];
				tus[counter][1] = temp7[4];
				//itus[counter] = temp[6];
				counter++;
			}
			//FileUtils.writeLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/sampling_eng-fra_culture_charRatios.csv"), Constants.UTF8, Arrays.asList(itus),"\n");

			List<String> lines = FileUtils.readLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/sampling_eng-fra_culture.txt"), Constants.UTF8);
			counter=0;
			for (String line:lines){
				if (counter==0){
					counter++;
					continue;
				}
				String[] temp8 = line.split("\t");
				if (!temp8[group].trim().isEmpty()){
					//System.out.println(temp[group]);
					int ind = Integer.parseInt(temp8[group].trim()) ;
					res.add(temp8[group].trim()+"\t"+tus[ind][0]+"\t"+tus[ind][1]);
				}	
			}
			FileUtils.writeLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/sampling_eng-fra_culture_g"+Integer.toString(group+1)+".csv"), Constants.UTF8, res,"\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void globalVoicesCheck(String[] args) {
		//HashMap<String,String> truepairs = BitextUtils.getTruePairs( "C:\\Users\\vpapa\\ELRC\\gv-data\\last_version\\docpairs_true_ell-eng.txt");
		//HashMap<String,String> foundpairs = BitextUtils.getTruePairs( "C:\\Users\\vpapa\\ELRC\\gv-data\\last_version\\docpairs_found_noURL_ell-eng.txt");
		//HashMap<String, DocVector> features = Bitexts.extractXML_Features(new File("C:\\Users\\vpapa\\ELRC\\gv-data\\last_version\\pairs"));
		HashMap<String,String> truepairs = BitextUtils.getTruePairs( args[0]);
		HashMap<String,String> foundpairs = BitextUtils.getTruePairs( args[1]);
		HashMap<String, DocVector> features = Bitexts.extractXML_Features(new File(args[2]), null);
		Set<String> ts=truepairs.keySet();
		Iterator<String> itt = ts.iterator();
		while (itt.hasNext()){
			String td1 = itt.next();
			String td2 = truepairs.get(td1);
			double len = features.get(td1).numToksnoOOI + features.get(td2).numToksnoOOI;
			Set<String> fs=foundpairs.keySet();
			Iterator<String> itf = fs.iterator();
			boolean found = false;
			while (itf.hasNext()){
				String fd1 = itf.next();
				String fd2 = foundpairs.get(fd1);
				if ((td1.equals(fd1) && td2.equals(fd2)) || (td1.equals(fd2) && td2.equals(fd1))){
					found =true;
					break;
				}
			}
			if (found){
				System.out.println("1\t"+td1+"\t"+td2+"\t"+len);	
			}else{
				System.out.println("0\t"+td1+"\t"+td2+"\t"+len);
			}
		}
	}

	private static void iroEval() throws IOException {
		File evalfile = new File("C:/Users/vpapa/ELRC/eval_tests/eng-fra_culture_aupdih_evalset_iro");
		File pairsfile = new File("C:/Users/vpapa/ELRC/eval_tests/eng-fra_culture_aupdih.csv");
		File resultfile = new File("C:/Users/vpapa/ELRC/eval_tests/eng-fra_culture_aupdih_final.csv");
		List<String> eval_lines = FileUtils.readLines(evalfile, Constants.UTF8);
		List<String> all_lines = FileUtils.readLines(pairsfile, Constants.UTF8);
		String[] id=null;
		String evalres="id\tl1\tl2\tseg1\tseg2\ttype\tcharLengthRatio\twordLengthRatio\talignerScore\tevalscore\n";
		for (String t:eval_lines){
			id=t.split("\t");
			for (String tt:all_lines){
				if (tt.startsWith(id[0]+"\t")){
					evalres = evalres+tt+"\t"+id[3]+"\n";
					continue;
				}
			}
		}
		FileUtils.writeStringToFile(resultfile, evalres, Constants.UTF8);
	}

	private static void checkLvEn() throws IOException {
		List<String> l1 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/TILDE/archive/State_related_content_from_Latvian_Web.en-lv.en"), Constants.UTF8);
		List<String> l2 = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/TILDE/archive/State_related_content_from_Latvian_Web.en-lv.lv"), Constants.UTF8);
		System.out.println(l1.size()+"\t"+l2.size());
		List<String> l1_l2 = new ArrayList<String>();
		Set<String> segs = new HashSet<String>();
		for (int ii=0; ii<l1.size();ii++){
			String normS = ContentNormalizer.normtext(l1.get(ii));
			String normT = ContentNormalizer.normtext(l2.get(ii));
			if ( normS.isEmpty() || normT.isEmpty())
				continue;			
			if (normS.equals(normT))
				continue;
			/*if (Statistics.editDist(normS,normT)<5){ //FIXME add as parameter, check its influence
				LOGGER.warn("Discard due to high similarity of TUVs ");
				LOGGER.warn("\t"+ l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
			}*/

			if (Statistics.getMedian(FCStringUtils.getTokensLength(FCStringUtils.getTokens(normS)))>15){
				LOGGER.warn("Discard due to long tokens in a TUV ");
				LOGGER.warn("\t"+l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
			}
			if (Statistics.getMedian(FCStringUtils.getTokensLength(FCStringUtils.getTokens(normT)))>15){
				LOGGER.warn("Discard due to long tokens in a TUV ");
				LOGGER.warn("\t"+l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
			}
			/*if (FCStringUtils.countTokens(normS)<2){
				LOGGER.warn("Discard due to length (in tokens) of a TUV ");
				LOGGER.warn("\t"+l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
			}
			if (FCStringUtils.countTokens(normT)<2){
				LOGGER.warn("Discard due to length (in tokens) of a TUV ");
				LOGGER.warn("\t"+l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
			}*/
			float ratio = (float)normS.length()/(float)normT.length();
			if (ratio>3 || ratio < 0.3){
				LOGGER.warn("Discard due to charlength ratio of TUVs ");
				LOGGER.warn("\t"+l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
			}
			/*String num1=l1.get(ii).replaceAll("\\D+","");
			String num2=l2.get(ii).replaceAll("\\D+","");
			if (!num1.equals(num2)){
				//double temp=Statistics.editDist(num1, num2) / (double) Math.min(num1.length(),num2.length());
				//if (temp>0.35){
				LOGGER.warn("Discard due to different numbers in TUVs ");
				LOGGER.warn("\t"+l1.get(ii));
				LOGGER.warn("\t"+ l2.get(ii));
				continue;
				//}
			}*/

			String temp6 = normS+"\t"+normT;
			if (!segs.contains(temp6)){
				segs.add(temp6);
				l1_l2.add(l1.get(ii).trim()+"\t"+l2.get(ii).trim());
			}
		}
		System.out.println(segs.size());
	}

	private static void processABUsets1(String path) throws IOException {
		List<String> trainset = FileUtils.readLines(new File(path+"trainset.tcv"), Constants.UTF8);
		List<String> engtrains = new ArrayList<String>();
		List<String> elltrains = new ArrayList<String>();
		List<String> trains = new ArrayList<String>();
		//String path = "C:/Users/vpapa/ABU/tourism/datasets/";
		List<String> engset = FileUtils.readLines(new File(path+"tourism_eng.txt"), Constants.UTF8);
		List<String> newengset = new ArrayList<String>();
		List<String> ellset = FileUtils.readLines(new File(path+"tourism_ell.txt"), Constants.UTF8);
		List<String> newellgset = new ArrayList<String>();
		List<String> devset = FileUtils.readLines(new File(path+"devset.tcv"), Constants.UTF8);
		List<String> engdevs = new ArrayList<String>();
		List<String> elldevs = new ArrayList<String>();
		List<String> devs = new ArrayList<String>();
		List<String> testset1 =new ArrayList<String>();
		List<String> testset =new ArrayList<String>();

		Integer[] temp= new Integer[6];
		List<String> l33 = new ArrayList<String>();
		List<String> l22 = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/culture_en-el.processed.tsv"), Constants.UTF8);
		for (String line:l22){
			String[] temp3 = line.split("\t");
			if (temp[5].equals("NULL")){
				l33.add(line); //System.out.println(temp[0]+"\t"+temp[3]+"\t"+temp[4]);
			}
		}
		l22=null;
		List<String> l11 = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/tourism_en-el.processed.tsv"), Constants.UTF8);
		for (String line:l11){
			String[] temp4 = line.split("\t");
			if (temp[5].equals("NULL")){
				l33.add(line); //System.out.println(temp[0]+"\t"+temp[3]+"\t"+temp[4]);
			}
		}
		List<String> dev1 = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/devset.txt"), Constants.UTF8);
		List<String> devset1 =new ArrayList<String>();
		for (String line:dev1){
			int ind=Integer.parseInt(line);
			devset1.add(l33.get(ind));

		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ABU/tourism/devset.tcv"), Constants.UTF8, devset,"\n");

		List<String> test1 = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/testset.txt"), Constants.UTF8);

		for (String line:test1){
			int ind=Integer.parseInt(line);
			testset.add(l33.get(ind));
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ABU/tourism/testset.tcv"), Constants.UTF8, testset,"\n");

		List<String> trainset1 = new ArrayList<String>();
		for (String line:l33){
			if (testset.contains(line) || devset.contains(line))
				continue;
			trainset.add(line);
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ABU/tourism/trainset.tcv"), Constants.UTF8, trainset,"\n");

		for (String line1:dev1){
			String id1 = line1.split("\t")[0];
			for (String line2:test1){
				String id2 = line2.split("\t")[0];
				if (id1.equals(id2)){
					System.out.println("OOOOPS");
				}
			}
		}		
	}

	private static void processABUsets(String path) throws IOException {
		//String path = "C:/Users/vpapa/ABU/tourism/datasets/";
		List<String> trainset = FileUtils.readLines(new File(path+"trainset.tcv"), Constants.UTF8);
		List<String> engtrains = new ArrayList<String>();
		List<String> elltrains = new ArrayList<String>();
		List<String> trains = new ArrayList<String>();
		for (String train:trainset){
			engtrains.add(train.split("\t")[1]);
			elltrains.add(train.split("\t")[2]);
			trains.add(train.split("\t")[1]+Constants.SPACE+train.split("\t")[2]);
		}
		List<String> devset = FileUtils.readLines(new File(path+"devset.tcv"), Constants.UTF8);
		List<String> engdevs = new ArrayList<String>();
		List<String> elldevs = new ArrayList<String>();
		List<String> devs = new ArrayList<String>();
		for (String dev:devset){
			engdevs.add(dev.split("\t")[1]);
			elldevs.add(dev.split("\t")[2]);
			devs.add(dev.split("\t")[1]+Constants.SPACE+dev.split("\t")[2]);
		}
		List<String> testset = FileUtils.readLines(new File(path+"testset.tcv"), Constants.UTF8);
		List<String> engtests = new ArrayList<String>();
		List<String> elltests = new ArrayList<String>();
		List<String> tests = new ArrayList<String>();
		for (String test:testset){
			engtests.add(test.split("\t")[1]);
			elltests.add(test.split("\t")[2]);
			tests.add(test.split("\t")[1]+Constants.SPACE+test.split("\t")[2]);
		}
		for (String train:trains){
			String temp2 = ContentNormalizer.normtext(train);
			for (String test:tests){
				String temp1 = ContentNormalizer.normtext(test);
				if (temp2.equals(temp1)){
					System.out.println(train+"\t"+test);
				}
			}
		}
	}

	private static void cleanABUSets() throws IOException {
		List<String> templines = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/datasets/abu/last_testset.tcv"), Constants.UTF8);
		List<String> tep = new ArrayList<String>();		for (String line:templines)		{			tep.add(line.split("\t")[1]); 		}	
		templines = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/datasets/abu/last_devset.tcv"), Constants.UTF8);
		List<String> dep = new ArrayList<String>();		for (String line:templines)		{			dep.add(line.split("\t")[1]); 		}
		List<String> mono = FileUtils.readLines(new File("C:/Users/vpapa/ABU/tourism/datasets/abu/last_tourism_eng.txt"), Constants.UTF8);
		System.out.println(mono.size());
		mono = cleanABUMonoSets(mono, tep,  dep);
		FileUtils.writeLines(new File("C:/Users/vpapa/ABU/tourism/datasets/abu/last_tourism-culture_eng.txt"), Constants.UTF8, mono,"\n");		
	}

	private static void fetchURL(String string) throws IOException {
		//URL url = new URL("https://github.com/bixo/bixo/blob/master/examples/src/test/java/bixo/examples/crawl/DemoCrawlWorkflowLRTest.java");
		//System.out.println(url.getPath());
		//url = new URL("http://www.antagonistikotita.gr/epanek/proskliseis.asp?id=49&cs=");
		//System.out.println(url.getPath());
		//url = new URL("http://www.ilsp.gr/el/infoprojects/meta");
		//System.out.println(url.getPath());
		URL url = new URL(string);
		URLConnection conn = url.openConnection();
		InputStream ins = conn.getInputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int r;
		while ((r = ins.read(buf)) != -1) {
			bos.write(buf, 0, r);
		}
		ins.close();
		final byte[] data = bos.toByteArray();

		InputStream is = new ByteArrayInputStream(data, 0, data.length);

		String content = CleanerUtils.getContent(is, true);
		System.out.println(content);
	}

	private static void checkRightsInCesDoc(File file) throws IOException {
		List<String> dirs= FileUtils.readLines(file, Constants.UTF8);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(appXMLext) & !arg1.contains(UNDERSCORE_STR));
			}
		};
		for (String dir:dirs){
			List<File> cesDocFiles = FcFileUtils.listFiles(new File(dir), filter,true);
			HashMap<String, ParsAttr> boilerpars_attrs = CleanerUtils.getParsAttrs((ArrayList<File>) cesDocFiles, "crawlinfo", "boilerplate", true);
			Set<String> pars = boilerpars_attrs.keySet();
			Iterator<String> it_pars = pars.iterator();
			String key, norm_key;
			double thr = cesDocFiles.size()/10;
			System.out.println(dir);
			System.out.println("-------------------------------------------------");
			while (it_pars.hasNext()){
				key = it_pars.next();
				if (boilerpars_attrs.get(key).filenames.size()<thr)
					continue;
				norm_key = ContentNormalizer.normalizeText(key);
				norm_key =  ContentNormalizer.normtext1(norm_key);
				if (norm_key.isEmpty())
					continue;
				String[] words = norm_key.split(Constants.SPACE); 
				if (words.length<2)
					continue;

				Double[] temp1 = new Double[boilerpars_attrs.get(key).par_endids.size()];
				for (int ii=0;ii<boilerpars_attrs.get(key).par_endids.size();ii++)
					temp1[ii] = boilerpars_attrs.get(key).par_startids.get(ii)/(boilerpars_attrs.get(key).par_endids.get(ii)+boilerpars_attrs.get(key).par_startids.get(ii));

				if (Statistics.getMean(temp1)<0.8)
					continue;
				if (norm_key.contains(COPYRIGHT_STR) || norm_key.contains(RIGHTS_STR)){
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!"+key+"!!!!!!!!!!!!!!!!!!!!!!!!");
					break;
				}
				System.out.println(key);
			}	
		}
	}

	private static void getStatsFromDepthOutput(File inn) throws IOException {
		//File inn= new File("C:/Users/vpapa/test/depth/audi/outputs");
		String langs = "af;ar;bg;bn;ca;cs;cy;da;de;el;en;es;et;eu;fa;fi;fr;ga;gl;gu;he;hi;hr;hu;id;it;ja;kn;ko;lt;lv;mk;ml;mr;mt;ne;nl;no;pa;pl;pt;ro;ru;sk;sl;so;sq;sv;sw;ta;te;th;tl;tr;uk;ur;vi;zh-cn;zh-tw";
		String[] ls = LangDetectUtils.updateLanguages(langs,true).split(";");
		Map<String, String> resu = new HashMap<String, String>();
		resu.put("site", "");		resu.put("depth", "");		resu.put("minlen", "");		for (int ii=0;ii<ls.length;ii++){resu.put(ls[ii], "");}
		File[] outputs= inn.listFiles();
		File resi = new File(inn.getAbsolutePath()+"_res");

		for (File file:outputs){
			List<String> lines= FileUtils.readLines(file, Constants.UTF8) ;
			List<String> found = new ArrayList<String>();
			for (String line:lines){
				if (line.startsWith("\t") || line.startsWith("staring"))
					continue;
				if (line.startsWith("target"))	{			//newlines.add(line.split("\t")[1]);
					resu.put("site", resu.get("site")+line.split("\t")[1]+"\t");
					continue;
				}
				if (line.startsWith("crawled"))	{			//newlines.add(line.split("\t")[1]);
					//total[1] = total[1]+line.split("\t")[1]+"\t";
					resu.put("depth", resu.get("depth")+line.split("\t")[1]+"\t");
					continue;
				}
				if (line.startsWith("minimum"))	{			//newlines.add(line.split("\t")[1]);
					//total[2] = total[2]+line.split("\t")[1]+"\t";
					resu.put("minlen", resu.get("minlen")+line.split("\t")[1]+"\t");
					continue;
				}
				if (line.startsWith("number")){
					System.out.println(line);
					String[] t = line.split("\t");
					String l = t[0].split(Constants.SPACE)[4];
					resu.put(l, resu.get(l)+t[1]+"\t");
					found.add(l);
				}
			}
			for (int ii=0;ii<ls.length;ii++){
				if (found.contains(ls[ii]))
					continue;
				resu.put(ls[ii], resu.get(ls[ii])+"0"+"\t");
			}
		}
		List<String> total = new ArrayList<String>();
		total.add("site"+"\t"+resu.get("site"));
		total.add("depth"+"\t"+resu.get("depth"));
		total.add("minlen"+"\t"+resu.get("minlen"));
		for (int ii=0;ii<ls.length;ii++){
			total.add(ls[ii]+"\t"+resu.get(ls[ii]));
		}
		FileUtils.writeLines(resi, Constants.UTF8, total,"\n");		
	}

	private static void getURLsfromPairedCesDoc(File in10) {
		//File in10 = new File ("C:/Users/vpapa/test/depth/polinst/polinst-hu_20161122_202939/6227940e-3987-4fe4-abad-75b13faa45d3/xml");
		String lang11 = "eng";
		String lang22 = "hun";
		File[] files10 = in10.listFiles();
		System.out.println(lang11+"\t"+lang22);
		for (File file:files10){
			String filename =file.getName(); 
			if (filename.endsWith("xml") && filename.contains(UNDERSCORE_STR) && filename.contains(lang11) && filename.contains(lang22)){
				String[] parts = filename.split(UNDERSCORE_STR);
				File newfile = new File(FilenameUtils.concat(file.getParent(),parts[0]+".xml"));
				String url1 = ReadResources.extractNodefromXML(newfile.getAbsolutePath(), "eAddress", false);
				newfile = new File(FilenameUtils.concat(file.getParent(),parts[1]+".xml"));
				url1 = url1+"\t"+ReadResources.extractNodefromXML(newfile.getAbsolutePath(), "eAddress",false);
				System.out.println(url1);
			}
		}
	}

	private static List<String> getNewCommands(File file) throws IOException {
		List<String> commands = FileUtils.readLines(file, Constants.UTF8);
		List<String> newcommands = new ArrayList<String>();
		for (String command:commands){
			File t1 = new File(command);
			String t2 = t1.getParentFile().getName();
			t2 = t2.substring(0, t2.indexOf("_"));
			String t3 = t1.getParentFile().getParent();
			t3=t3.replace("\\", "/");
			//.replaceAll("\\", "/");
			String a = " java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-SNAPSHOT-jar-with-dependencies.jar ";  
			a = a + " -export -i \""+command + "\" -lang \"eng;ell\" -len 0 -mtlen 100 -dom \"culture\" -bs \"" + t3 + "/output_"+ t2+"\"" ;
			a = a + " >\"" + t3+ "/log_export_"+t2+ "\"";
			newcommands.add(a);

			a = " java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-SNAPSHOT-jar-with-dependencies.jar ";  
			a = a + " -export -offline -i \""+command +"/pdf"+ "\" -lang \"eng;ell\" -len 0 -mtlen 100 -dom \"culture\" -bs \"" + t3 + "/output_"+ t2+"\"" ;
			a = a + " -o \""+command +"/xml\"" ; 
			a = a + " >\"" + t3+ "/log_export_offline_"+t2+ "\"";
			newcommands.add(a);

			a = " java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-SNAPSHOT-jar-with-dependencies.jar ";  
			a = a + " -dedup -i \""+command +"/xml"+ "\" -lang \"eng;ell\" -bs \"" + t3 + "/output_"+ t2+"\"" ;
			a = a + " >\"" + t3+ "/log_dedup_"+t2+ "\"";
			newcommands.add(a);
		}
		FileUtils.writeLines(new File(file.getAbsolutePath()+".txt"), Constants.UTF8, newcommands,"\n");
		return null;
	}

	private static void getStatsOnELRCDatasets(File infile) throws IOException {
		String cri1 = "Corpus";
		String cri2 = "Translation Units";
		List<String> ilsp = new ArrayList();	ilsp.add("Greece");		ilsp.add("Poland");			ilsp.add("Croatia");	ilsp.add("Slovenia");		ilsp.add("Slovakia");	ilsp.add("Bulragia");	ilsp.add("Cyprus");		ilsp.add("Romania");
		List<String> tilde = new ArrayList();	tilde.add("Estonia"); 	tilde.add("Finland"); 		tilde.add("Iceland");	tilde.add("Latvia");		tilde.add("Lithuania");	tilde.add("Norway");	tilde.add("Sweden");
		List<String> elda = new ArrayList(); 	elda.add("France");		elda.add("Belgium");		elda.add("Spain");		elda.add("France, Spain");	elda.add("France, Malta"); elda.add("Ireland");	elda.add("Italy"); 		elda.add("Malta");		elda.add("Portugal");
		List<String> dfki = new ArrayList();	dfki.add("Austria");	dfki.add("Czech Republic"); dfki.add("Germany");	dfki.add("Hungary");		dfki.add("Luxembourg");	dfki.add("Netherlands"); 

		//File infile = new File("C://Users//vpapa//ELRC//20170406_ELRC_SHARE_OVERVIEW.txt");
		List<String>  datalines = FileUtils.readLines(infile, Constants.UTF8);
		List<String>  selectedlines = new ArrayList<String>(); 
		List<String>  selectedlines1 = new ArrayList<String>();
		for (String line:datalines){
			//if (line.contains("INGESTED") || line.contains("INTERNAL") || line.contains("PUBLISHED")){
			if (line.contains("INTERNAL")){
				continue;
			}
			String[] templine = line.split("\t");
			if (templine[1].equals(cri1) && templine[5].contains(cri2)){
				String langs = replaceLanguages(templine[2]);
				String num =templine[4]; 
				if (num.contains("|")){
					String[] a= num.split("\\|");
					num = a[a.length-1].trim();
				}
				num=num.replaceAll(",", "");
				selectedlines.add(langs+"\t"+num+"\t"+templine[6].trim()+"\t"+templine[7].trim()+"\t"+templine[11].trim()+"\t"+templine[12].trim()+"\t"+templine[14].trim());
			}
			if (templine[1].equals(cri1) && !templine[2].contains("|")){
				String langs = replaceLanguages(templine[2]);
				String[] meanum =templine[4].split("\\|"); 
				String[] mea =templine[5].split("\\|");
				int index=-1;
				for (int ii=0;ii<mea.length;ii++){
					if (mea[ii].contains("Words") || mea[ii].contains("Tokens")){
						index=ii;
						break;
					}
				}
				if (index<0)
					continue;
				String num = meanum[index].trim().replaceAll(",", "");
				selectedlines1.add(langs+"\t"+num+"\t"+templine[6].trim()+"\t"+templine[7].trim()+"\t"+templine[11].trim()+"\t"+templine[12].trim()+"\t"+templine[14].trim());

			}
		}
		FileUtils.writeLines(new File(infile.getAbsolutePath()+"_"+cri1+"_"+cri2), Constants.UTF8, selectedlines,"\n");
		FileUtils.writeLines(new File(infile.getAbsolutePath()+"_"+cri1+"_mono"), Constants.UTF8, selectedlines1,"\n");

		Map<String, Integer[]> langmono_num = new HashMap<String,Integer[]>();
		List<String> tulines1 = FileUtils.readLines(new File(infile.getAbsolutePath()+"_"+cri1+"_mono"), Constants.UTF8);

		for (String tus:tulines1){
			Integer[] temp_langmono= new Integer[3];
			for (int ii=0;ii<temp_langmono.length;ii++){
				temp_langmono[ii] = 0; 
			}
			String[] a = tus.split("\t");
			if (langmono_num.containsKey(a[0]))
				temp_langmono = langmono_num.get(a[0]);
			if (a[6].equals("NO"))
				temp_langmono[0] = temp_langmono[0]+Integer.parseInt(a[1]);
			if (a[6].equals("YES"))
				temp_langmono[1] = temp_langmono[1]+Integer.parseInt(a[1]);
			temp_langmono[2] = temp_langmono[2] + Integer.parseInt(a[1]);
			langmono_num.put(a[0], temp_langmono);
		}
		System.out.println("--------------------monolingual ------------------------");
		Set<String> langmonoset = langmono_num.keySet();
		Iterator<String> langmono = langmonoset.iterator();
		String ll="";
		while (langmono.hasNext()){
			ll = langmono.next();
			Integer[] temp = langmono_num.get(ll);
			System.out.println(ll+"\t"+temp[0]+ "\t"+temp[1]+"\t"+temp[2]);
		}	

		Map<String, Integer[]> lang_num = new HashMap<String,Integer[]>();
		Map<String, Integer[]> country_num = new HashMap<String,Integer[]>();
		Map<String, Integer[]> lic_num = new HashMap<String,Integer[]>();

		//Map<String, Integer> serv_num = new HashMap<String,Integer>();
		List<String> tulines = FileUtils.readLines(new File(infile.getAbsolutePath()+"_"+cri1+"_"+cri2), Constants.UTF8);
		for (String tus:tulines){
			Integer[] temp_lang=new Integer[3]; Integer[] temp_country=new Integer[3]; Integer[] temp_lic= new Integer[3]; 
			for (int ii=0;ii<temp_lang.length;ii++){
				temp_lang[ii] = 0; 	temp_country[ii] = 0; temp_lic[ii] = 0; 
			}
			String[] a = tus.split("\t");
			if (lang_num.containsKey(a[0]))
				temp_lang = lang_num.get(a[0]);
			if (a[6].equals("NO"))
				temp_lang[0] = temp_lang[0]+Integer.parseInt(a[1]);
			if (a[6].equals("YES"))
				temp_lang[1] = temp_lang[1]+Integer.parseInt(a[1]);
			temp_lang[2] = temp_lang[2] + Integer.parseInt(a[1]);
			lang_num.put(a[0], temp_lang);

			if (country_num.containsKey(a[4]))
				temp_country = country_num.get(a[4]);
			if (a[6].equals("NO"))
				temp_country[0] = temp_country[0]+Integer.parseInt(a[1]);
			if (a[6].equals("YES"))
				temp_country[1] = temp_country[1]+Integer.parseInt(a[1]);
			temp_country[2] = temp_country[2] + Integer.parseInt(a[1]);
			country_num.put(a[4], temp_country);

			if (lic_num.containsKey(a[5]))
				temp_lic = lic_num.get(a[5]);
			if (a[6].equals("NO"))
				temp_lic[0] = temp_lic[0]+Integer.parseInt(a[1]);
			if (a[6].equals("YES"))
				temp_lic[1] = temp_lic[1]+Integer.parseInt(a[1]);
			temp_lic[2] = temp_lic[2] + Integer.parseInt(a[1]);
			lic_num.put(a[5], temp_lic);
		}
		Set<String> langsset = lang_num.keySet();
		Set<String> countriesset = country_num.keySet();
		Set<String> licsset = lic_num.keySet();
		Iterator<String> lang = langsset.iterator();
		Iterator<String> country = countriesset.iterator();
		Iterator<String> lic = licsset.iterator();
		System.out.println("--------------------langs------------------------");
		ll="";
		while (lang.hasNext()){
			ll = lang.next();
			Integer[] temp = lang_num.get(ll);
			System.out.println(ll+"\t"+temp[0]+ "\t"+temp[1]+"\t"+temp[2]);
		}
		System.out.println("--------------------counties------------------------");
		ll="";
		while (country.hasNext()){
			ll = country.next();
			Integer[] temp = country_num.get(ll);
			System.out.println(ll+"\t"+temp[0]+ "\t"+temp[1]+"\t"+temp[2]);
		}
		System.out.println("--------------------lics------------------------");
		ll="";
		while (lic.hasNext()){
			ll = lic.next();
			Integer[] temp = lic_num.get(ll);
			System.out.println(ll+"\t"+temp[0]+ "\t"+temp[1]+"\t"+temp[2]);
		}
		System.out.println("--------------------partners------------------------");
		Integer[] temp_init1 = new Integer[3]; Integer[] temp_init2 = new Integer[3]; Integer[] temp_init3 = new Integer[3]; Integer[] temp_init4 = new Integer[3];
		for (int ii=0;ii<temp_init1.length;ii++){
			temp_init1[ii] = 0; temp_init2[ii] = 0; temp_init3[ii] = 0; temp_init4[ii] = 0; 
		}
		Map<String, Integer[]>  partners = new HashMap<String,Integer[]>(); partners.put("ilsp", temp_init1); partners.put("tilde", temp_init2); partners.put("elda", temp_init3);partners.put("dfki", temp_init4);
		country = countriesset.iterator();
		while (country.hasNext()){
			ll = country.next();
			if (ilsp.contains(ll)) {
				Integer[] temp = partners.get("ilsp");
				for (int ii=0;ii<3;ii++)
					temp[ii] = temp[ii]+country_num.get(ll)[ii];
				partners.put("ilsp",  temp);
				continue;
			}
			if (elda.contains(ll)) {
				Integer[] temp = partners.get("elda");
				for (int ii=0;ii<3;ii++)
					temp[ii] = temp[ii]+country_num.get(ll)[ii];
				partners.put("elda",  temp);
				continue;
			}
			if (dfki.contains(ll)) {
				Integer[] temp = partners.get("dfki");
				for (int ii=0;ii<3;ii++)
					temp[ii] = temp[ii]+country_num.get(ll)[ii];
				partners.put("dfki",  temp);
				continue;
			}
			if (tilde.contains(ll)) {
				Integer[] temp = partners.get("tilde");
				for (int ii=0;ii<3;ii++)
					temp[ii] = temp[ii]+country_num.get(ll)[ii];
				partners.put("tilde",  temp);
				continue;
			}
			System.out.println("oops\t"+ll);
		}
		System.out.println("ilsp"+"\t" + partners.get("ilsp")[0]+"\t"+partners.get("ilsp")[1]+"\t"+partners.get("ilsp")[2]);
		System.out.println("elda"+"\t" + partners.get("elda")[0]+"\t"+partners.get("elda")[1]+"\t"+partners.get("elda")[2]);
		System.out.println("dfki"+"\t" + partners.get("dfki")[0]+"\t"+partners.get("dfki")[1]+"\t"+partners.get("dfki")[2]);
		System.out.println("tilde"+"\t" + partners.get("tilde")[0]+"\t"+partners.get("tilde")[1]+"\t"+partners.get("tilde")[2]);

	}

	private static List<String> processBuckList(File file1, File file2, String domain, String tlang1, String tlang2) throws IOException {
		//String domain = ".hr ";
		//String tlang1 = " hr ";
		//String tlang2 = " en ";
		List<String> cb = FileUtils.readLines(file1, Constants.UTF8);
		List<String> cb1 = FileUtils.readLines(file2, Constants.UTF8);
		List<String> hren_list = new ArrayList<String>();
		List<String> hren_list1 = new ArrayList<String>();
		List<String> res_list = new ArrayList<String>();
		String naked="";
		for (String line:cb){
			if (line.contains(domain) && line.contains(tlang1) && line.contains(tlang2)){
				//System.out.println(line);
				if (line.contains("blogspot") || line.contains("webstatsdomain") || line.contains("forum") || line.contains("blog."))
					continue;
				String[] t = line.split(Constants.SPACE);
				if (t[1].length()>30){
					System.out.println("eeee      "+line);
					continue;
				}
				if (hren_list.contains(t[1]))
					continue;
				naked=t[1];
				if (t[1].startsWith("www."))
					naked = t[1].substring(4);
				if (hren_list1.contains(naked))
					continue;
				hren_list.add(t[1]);
				hren_list1.add(naked);

				int index1 = line.indexOf(tlang1);
				String subline1 = line.substring(index1+4);
				int index11 = subline1.indexOf(Constants.SPACE);
				if (index11<0)
					index11 = subline1.length();
				int index2 = line.indexOf(tlang2);
				String subline2 = line.substring(index2+4);
				int index22 = subline2.indexOf(Constants.SPACE);
				if (index22<0)
					index22 = subline2.length();
				subline1 = t[1]+"\t"+tlang1+subline1.substring(0, index11)+"\t"+tlang2+subline2.substring(0,index22);
				subline1.replaceAll(Constants.SPACE, "\t");
				System.out.println(subline1);	
				res_list.add(subline1);
			}
		}
		for (String line:cb1){
			if (line.contains(domain) && line.contains(tlang1) && line.contains(tlang2)){
				//System.out.println(line);
				if (line.contains("blogspot") || line.contains("webstatsdomain") || line.contains("forum") || line.contains("blog."))
					continue;
				String[] t = line.split(Constants.SPACE);

				if (t[1].length()>30){
					//System.out.println("eeee      "+line);
					continue;
				}
				if (hren_list.contains(t[1]))
					continue;
				naked=t[1];
				if (t[1].startsWith("www."))
					naked = t[1].substring(4);
				if (hren_list1.contains(naked))
					continue;
				hren_list.add(t[1]);
				hren_list1.add(naked);

				int index1 = line.indexOf(tlang1);
				String subline1 = line.substring(index1+4);
				int index11 = subline1.indexOf(Constants.SPACE);
				if (index11<0)
					index11 = subline1.length();
				int index2 = line.indexOf(tlang2);
				String subline2 = line.substring(index2+4);
				int index22 = subline2.indexOf(Constants.SPACE);
				if (index22<0)
					index22 = subline2.length();
				subline1 = t[1]+"\t"+tlang1+subline1.substring(0, index11)+"\t"+tlang2+subline2.substring(0,index22);
				subline1.replaceAll(Constants.SPACE, "\t");
				System.out.println(subline1);	
				res_list.add(subline1);
			}
		}
		return res_list;
	}

	private static void fromCesDoc2xml(File file) throws IOException {
		String ooi_crawlinfo = "crawlinfo";
		String P_ELE = "p";
		File indir = new File("C:/Users/vpapa/test/ILSP_20170203_113925/5beb49a6-30b8-484a-bc6b-8d6b581fd2e9/xml");
		File[] xmlfiles = indir.listFiles();
		for (int ii=0;ii<xmlfiles.length;ii++){
			String basename = FilenameUtils.getBaseName(xmlfiles[ii].getAbsolutePath());
			if (basename.contains(Constants.UNDERSCORE))
				continue;
			String text= "";
			if (xmlfiles[ii].getName().endsWith("xml")){
				text = ReadResources.extractTextfromXML_clean(xmlfiles[ii].getAbsolutePath(),P_ELE,ooi_crawlinfo, false);
				File txtfile = new File(FilenameUtils.concat(xmlfiles[ii].getParent(),basename+".txt"));
				FileUtils.writeStringToFile(txtfile, text, Constants.UTF8);
			}
		}
	}

	private static void parselogfiles(File file) throws IOException {
		File[] logs = file.listFiles();
		for (int ii=0;ii<logs.length;ii++){
			String temp = FileUtils.readFileToString(logs[ii], Constants.UTF8);
			if (temp.contains("Exception in thread")){
				System.out.println("Problem from "+ logs[ii].getName());
				continue;
			}
			if (temp.contains("Merged TMX at")){
				System.out.println("TUs from "+ logs[ii].getName());
				continue;
			}
			if (temp.contains("CesDoc files generated: 0")){
				System.out.println("noDoc from "+ logs[ii].getName());
				continue;
			}	
			if (temp.contains("No pairs in")){
				System.out.println("noPair from "+ logs[ii].getName());
				continue;
			}	
			if (temp.contains("No proper TUs found.")){
				System.out.println("noTUs from "+ logs[ii].getName());
				continue;
			}
			System.out.println("What from "+ logs[ii].getName());
		}
	}

	private static void diffwebsites() throws IOException {
		List<String> websites = getUniqueSites(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/test_websites.txt"));
		String a1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds1-100.txt"), Constants.UTF8);
		String b1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds101-300.txt"), Constants.UTF8);
		String c1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds101-300_part1.txt"), Constants.UTF8);
		String d1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds300plus.txt"), Constants.UTF8);

		String d2 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds_prelast.txt"), Constants.UTF8);
		String d3 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds_prelast_v1.txt"), Constants.UTF8);

		String i1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_hr-en_extra_seeds.txt"), Constants.UTF8);
		String i2 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds_part2.txt"), Constants.UTF8);
		String i3 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_seeds_part3.txt"), Constants.UTF8);
		String i31 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_hr-en_seeds_part3.1.txt"), Constants.UTF8);
		String i32 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/tld_hr-en_seeds_part3.2.txt"), Constants.UTF8);

		String to1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/seeds/total_en-hr-seeds"), Constants.UTF8);
		String to2 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/seeds/new_selections_seeds.txt"), Constants.UTF8);
		String to3 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/seeds/new_selections_seeds1.txt"), Constants.UTF8);
		String to4 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/tld/hr/crawls/seeds/new_selections_seeds2.txt"), Constants.UTF8);

		String f1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/created_datasets/datasets_v2/culture_elrc_eng-hrv.tmx"), Constants.UTF8);
		String g1 = FileUtils.readFileToString(new File("C:/Users/vpapa/ELRC/created_datasets/datasets_v2/pub_admin_v2_elrc_eng-hrv.tmx"), Constants.UTF8);
		List<String> h1= FileUtils.readLines(new File("C:/Users/vpapa/ELRC/EN-HR/total_run/output_NEWALIGN_ILSP-FC_eng-hrv.tmx"), Constants.UTF8);
		List<String> h2 = new ArrayList<String>();
		for (String l:h1){
			if (l.contains("-url\""))
				h2.add(l);
		}
		h1=null;
		List<String> newwebsites =new ArrayList<String>();
		int cou=0;
		for (String website:websites){
			cou++;
			if (website.contains("blog") || website.contains("web."))
				continue;
			if (to1.contains(website))
				continue;
			if (to2.contains(website))
				continue;
			if (to3.contains(website))
				continue;
			if (to4.contains(website))
				continue;

			if (a1.contains(website))
				continue;
			if (b1.contains(website))
				continue;
			if (c1.contains(website))
				continue;
			if (d1.contains(website))
				continue;
			if (d2.contains(website))
				continue;
			if (d3.contains(website))
				continue;
			if (i1.contains(website))
				continue;
			if (i2.contains(website))
				continue;
			if (i3.contains(website))
				continue;
			if (i31.contains(website))
				continue;
			if (i32.contains(website))
				continue;
			if (f1.contains(website))
				continue;
			if (g1.contains(website))
				continue;

			boolean found=false;
			for (String hh2:h2){
				if (hh2.contains(website)){
					found=true;
					break;
				}
			}
			if (newwebsites.contains(website))
				continue;
			if (found)
				continue;
			newwebsites.add(website);
			System.out.println(website);
		}		
	}

	private static List<String> getUniqueSites(File file) throws IOException {
		List<String> initwebsites = FileUtils.readLines(file, Constants.UTF8);
		List<String> websites = new ArrayList<String>();
		for (int ii=0;ii<initwebsites.size();ii++){
			String website=initwebsites.get(ii);
			if (website.startsWith("#"))
				continue;
			String t = website.replaceAll("www.", "");
			int int1 = t.indexOf(".");
			int int2 = t.lastIndexOf(".");
			if (int1<int2)
				continue;
			if (websites.contains(t))
				continue;

			websites.add(t);
		}
		return websites;
	}

	private static void getURLsfromTMX(File tmxfile) throws IOException {
		List<String> urls = FileUtils.readLines(tmxfile, Constants.UTF8);
		Set<String> uniqueurls = new HashSet<String>();
		List<String> targeturls=new ArrayList<String>();

		for (String url:urls){
			if (url.contains("<prop type=\"l1-url\">") || url.contains("<prop type=\"l2-url\">")){
				if (!uniqueurls.contains(url)){
					uniqueurls.add(url);
					//url=url.replaceAll("<prop type=\"l1-url\">", "");
					//url= url.replaceAll("</prop>", "").trim();
					targeturls.add(url);

				}
			}

		}
		File outfile = new File(tmxfile.getAbsolutePath()+".urls");
		FileUtils.writeLines(outfile, Constants.UTF8, targeturls,"\n");

	}

	private static void checkparacrawl(File parcrawlfile) throws FileNotFoundException, IOException {
		int counterpara = -1, counter_iden=0, counter_empty=0, counter_probl=0, counter_email=0, counter_url=0, counter_case=0, counter_ratio=0;
		int counter_norm_empty=0, counter_norm_short=0, counter_norm_iden=0;
		int counter_dups=0;
		Set<String> passed = new HashSet<String>();
		int min_tok_len=3;
		try (BufferedReader br = new BufferedReader(new FileReader(parcrawlfile))) {
			String line;
			while ((line = br.readLine()) != null) {
				counterpara++; 
				line=line.trim();
				if (line.isEmpty()){
					counter_empty++;
					continue;

				}
				String[] temp = line.split("\t");
				if (temp.length!=2){
					counter_probl++;
					continue;
				}
				String temp1 = temp[0].trim();
				String temp2 = temp[1].trim();
				if (temp1.isEmpty() || temp2.isEmpty()){
					System.out.println("EMPTY \t\t:\t\t"+ temp1+"\t"+temp2);
					counter_empty++;
					continue;
				}
				if (temp1.equals(temp2)){
					System.out.println("IDENTICAL TUVs \t\t:\t\t"+ temp1+"\t"+temp2);
					counter_iden++;
					continue;
				}

				if (TMXHandlerUtils.checkemail(temp1, 0.5) || TMXHandlerUtils.checkemail(temp2, 0.5)){
					System.out.println("MOSTLY EMAIL\t\t:\t\t"+ temp1+"\t"+temp2);
					counter_email++;
					continue;
				}
				if (TMXHandlerUtils.checkurl(temp1, 0.5) || TMXHandlerUtils.checkurl(temp2, 0.5)){
					System.out.println("MOSTLY URL\t\t:\t\t" + temp1+"\t"+temp2);
					counter_url++;
					continue;
				}
				if (FCStringUtils.isAllUpperCase(temp1) * FCStringUtils.isAllUpperCase(temp2)<0){
					System.out.println("CASE DIFF\t\t:\t\t" + temp1+"\t"+temp2);
					counter_case++;
					continue;
				}

				float ratio = (float)temp1.length()/(float)temp2.length();
				if (ratio>=2 || ratio<= 0.5){
					System.out.println("CHAR_RATIO > 2 \t\t:\t\t" + temp1+"\t"+temp2);
					counter_ratio++;
					continue;
				}
				String normS = ContentNormalizer.normtext(temp1);
				String normT = ContentNormalizer.normtext(temp2);
				if (normS.isEmpty() || normT.isEmpty()){
					System.out.println("EMPTY_NORM_TUVS \t\t:\t\t" + temp1+"\t"+temp2);
					counter_norm_empty++;
					continue;
				}
				if (normS.equals(normT)){
					System.out.println("IDENTICAL_NORM_TUVS \t\t:\t\t" + temp1+"\t"+temp2);
					counter_norm_iden++;
					continue;
				}

				int len1 = FCStringUtils.countTokens(normS);
				int len2 = FCStringUtils.countTokens(normT);
				if (len1<min_tok_len || len2<min_tok_len){
					System.out.println("SHORT_NORM_TUVS "+min_tok_len+"\t\t:\t\t" + temp1+"\t"+temp2);
					counter_norm_short++;
					continue;
				}

				byte[] texthashkey = DedupUtils.calculateMD5(normT+"\t"+normS, min_tok_len-1); //TODO should text be stemmed?
				String key="";
				for (int jj=0;jj<texthashkey.length;jj++) {
					key += texthashkey[jj];
				}
				if (passed.contains(key)){
					System.out.println("DUPS\t\t:\t\t" + temp1+"\t"+temp2);
					counter_dups++;
					continue;
				}
				passed.add(key);
			}
		}

		System.out.println("total: " + counterpara);
		System.out.println("empty: " + counter_empty);
		System.out.println("identical: " + counter_iden);
		System.out.println("mostly URL: " + counter_url);
		System.out.println("mostly EMAIL: " + counter_email);
		System.out.println("case: " + counter_case);
		System.out.println("empty_norm: " + counter_norm_empty);
		System.out.println("identical_norm: " + counter_norm_iden);
		System.out.println("short_norm: " + counter_norm_short);
		System.out.println("dups_norm: " + counter_dups);

		double filtered = counter_empty + counter_iden + counter_url + counter_email + counter_dups + counter_case + counter_norm_empty + counter_norm_iden + counter_norm_short;
		double remained = counterpara - filtered;
		System.out.println("remain: "+ remained);		
	}




	private static void championlist(File file) throws IOException {
		List<String> teams = FileUtils.readLines(file, Constants.UTF8);
		List<String> selected = new ArrayList<String>();
		HashMap<Integer, List<String>> rounds = new HashMap<Integer, List<String>>();
		int totalrounds = teams.size()-1;
		for  (int ii=1;ii<=totalrounds;ii++)
			rounds.put(ii, new ArrayList<String>());

		List<String> matches = new ArrayList<String>();
		List<String> selectedmatches = new ArrayList<String>();

		boolean in=true;
		int counter=1;
		for (int team1=0;team1<teams.size();team1++){
			for (int team2=0;team2<teams.size();team2++){
				if (team1==team2)
					continue;
				if (selected.contains(teams.get(team1)) || selected.contains(teams.get(team2)))
					continue;
				if (in){
					System.out.println(counter+". " + teams.get(team1) + " - " + teams.get(team2));
					matches.add(teams.get(team1) + " - " + teams.get(team2));
					counter++;
				}else{
					System.out.println(counter+". " + teams.get(team2) + " - " + teams.get(team1));
					matches.add(teams.get(team2) + " - " + teams.get(team1));
					counter++;
				}
				if (in)
					in=false;
				else
					in=true;
			}
			selected.add(teams.get(team1));
		}

		for (int jj=1;jj<=totalrounds;jj++){
			List<String> temp = rounds.get(jj);
			List<String> tempteams = new ArrayList<String>();
			for (int ii=0;ii<temp.size();ii++){
				String[] t = temp.get(ii).split("-");
				tempteams.add(t[0].trim());
				tempteams.add(t[1].trim());
			}
			if (temp.size()>=8)
				continue;
			for (int ii=0;ii<matches.size();ii++){
				if (selectedmatches.contains( matches.get(ii)))
					continue;
				String[] t = matches.get(ii).split("-");
				if (tempteams.contains(t[0].trim()) || tempteams.contains(t[1].trim()))
					continue;
				if (temp.size()>=8)
					break;
				temp.add(matches.get(ii));
				rounds.put(jj,temp);
				selectedmatches.add(matches.get(ii));
				tempteams.add(t[0].trim());
				tempteams.add(t[1].trim());
			}
		}

		for (int jj=1;jj<=totalrounds;jj++){
			List<String> temp = rounds.get(jj);
			System.out.println("--------------\t"+jj + "η ΑΓΩΝΙΣΤΙΚΗ\t-------------------------");
			for (int ii=0;ii<temp.size();ii++)
				System.out.println(temp.get(ii));
		}

		//check
		for (int ii=0;ii<teams.size();ii++){
			String team = teams.get(ii);
			List<String> opps = new ArrayList<String>();
			System.out.println(team);
			opps.add(team);
			for (int jj=1;jj<=totalrounds;jj++){
				List<String> m = rounds.get(jj);
				for (int kk=0;kk<m.size();kk++){
					String[] a = m.get(kk).split("-");
					if (a[0].trim().equals(team) || a[1].trim().equals(team)){
						if (a[0].trim().equals(team))
							opps.add(a[1].trim());
						else
							opps.add(a[0].trim());
						Collections.sort(opps);
					}
				}
			}
			System.out.println(opps);
		}

	}

	/**
	 * process two TMX files EN-SL and SL-EN given by the SL NAP
	 * checks for duplicates between files
	 * generates two files containing only the unique TUs
	 * @throws IOException
	 */
	private static void processSL_NAP() throws IOException {
		File en_sl = new File("C:/Users/vpapa/ELRC/naps/SL/EN-SL.tmx");
		File sl_en = new File("C:/Users/vpapa/ELRC/naps/SL/SL-EN.tmx");
		List<String> en_sl_pairs = pseudoparseTMX(en_sl, "UTF-16LE");
		LOGGER.info("number of EN-SL TUs in "+ en_sl.getName() + " is : " + en_sl_pairs.size());
		List<String> sl_en_pairs = pseudoparseTMX(sl_en, "UTF-16LE");
		LOGGER.info("number of SL-EN TUs in "+ sl_en.getName() + " is : " + sl_en_pairs.size());

		List<String> all_pairs = new ArrayList<String>();
		all_pairs.addAll(en_sl_pairs);
		all_pairs.addAll(sl_en_pairs);
		LOGGER.info("number of all TUs is "+ all_pairs.size());

		List<String> uniquepairs = new ArrayList<String>();
		for (String pair:all_pairs){
			if (!uniquepairs.contains(pair))
				uniquepairs.add(pair);
		}
		LOGGER.info("number of unique TUs is "+ uniquepairs.size());
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/naps/SL/unique_EN-SL_pairs.txt"), Constants.UTF8, uniquepairs,"\n");

		int newsize1 =  7*uniquepairs.size()/15;
		int newsize2 =  8*uniquepairs.size()/15;
		LOGGER.info("Two files will be generated.");
		LOGGER.info("The first will include	about "+ newsize1 + " TUs.");
		LOGGER.info("The second will include about "+ newsize2 + " TUs.");
		uniquepairs.clear();
		all_pairs.clear();

		int comcounter_en=0, comcounter_sl=0;
		for (String pair:en_sl_pairs){
			if (sl_en_pairs.contains(pair))
				comcounter_en++;
			//else
			//	LOGGER.info(pair);
		}
		for (String pair:sl_en_pairs){
			if (en_sl_pairs.contains(pair))
				comcounter_sl++;
			//else
			//	LOGGER.info(pair);
		}

		LOGGER.info("number of TUS from EN-SL which are in SL-EN : " + comcounter_en);
		double inters1 = 100 *(double) comcounter_en/ (double) en_sl_pairs.size();
		LOGGER.info(inters1 + "% of EN_SL");
		double inters2 = 100 * (double) comcounter_en/ (double) sl_en_pairs.size();
		LOGGER.info(inters2 + "% of SL-EN");

		LOGGER.info("number of TUS from SL-EN which are in EN-SL : " + comcounter_sl);
		inters1 = 100 *(double) comcounter_sl/ (double) en_sl_pairs.size();
		LOGGER.info(inters1 + "% of EN_SL");
		inters2 = 100 * (double) comcounter_sl/ (double) sl_en_pairs.size();
		LOGGER.info(inters2 + "% of SL-EN");

		sl_en_pairs.clear();

		List<String> en_sl_lines = FileUtils.readLines(en_sl, "UTF-16LE");
		List<String> en_sl_lines_new = new ArrayList<String>();
		List<String> en_sl_lines_newRest = new ArrayList<String>();

		int paircounter = 0;
		for (String line:en_sl_lines){
			if (paircounter<newsize1)
				en_sl_lines_new.add(line);
			else{
				en_sl_lines_newRest.add(line);
				continue;
			}
			if (line.equals("</tu>"))
				paircounter++;
		}
		en_sl_lines_new.add("");
		en_sl_lines_new.add("</body>");
		en_sl_lines_new.add("</tmx>");
		File en_sl_new1 = new File("C:/Users/vpapa/ELRC/naps/SL/EN-SL_new1.tmx");
		FileUtils.writeLines(en_sl_new1, "UTF-16LE", en_sl_lines_new,"\n");
		List<String> en_sl_pairs_new = pseudoparseTMX(en_sl_new1, "UTF-16LE");
		int words=0;
		for (String pair:en_sl_pairs_new){
			pair = pair.replaceAll("<seg>", "");
			pair = pair.replaceAll("</seg>", "");
			pair = pair.replaceAll("\t", Constants.SPACE);
			words = words + FCStringUtils.getTokens(pair).size();
		}
		LOGGER.info("Number of words (space separated) in "+ en_sl_new1.getName()+ " is: "+ words);

		System.out.println(en_sl_pairs_new.size());
		System.out.println(en_sl_pairs_new.get(0));
		en_sl_lines.clear();
		en_sl_lines_new.clear();
		//FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/naps/SL/EN-SL_new1.tmx"), "UTF-16LE", en_sl_lines_newRest,"\n");		

		List<String> sl_en_lines = FileUtils.readLines(sl_en, "UTF-16LE");
		System.out.println(sl_en_lines.size());
		sl_en_lines.addAll(en_sl_lines_newRest);
		System.out.println(sl_en_lines.size());

		List<String> sl_en_lines_new =new ArrayList<String>();
		List<String> temp = new ArrayList<String>();
		boolean intu = false;
		String ttt = "";
		paircounter = 0;
		for (String line:sl_en_lines){
			if (line.startsWith("<tu creationdate=")){
				temp.add(line);
				intu= true;
				continue;
			}
			if (!intu){
				sl_en_lines_new.add(line);
				continue;
			}
			temp.add(line);
			if (line.startsWith("<seg>")){
				if (ttt.isEmpty())
					ttt = ttt+line;
				else
					ttt = line+"\t"+ttt;
				continue;
			}
			if (line.startsWith("</tu>") && ttt.contains("\t")){
				temp = changeOrder(temp);
				ttt = getSegs(temp);
				if (en_sl_pairs_new.contains(ttt)){
					//System.out.println(ttt);
					intu=false;
					temp.clear();
					ttt="";
					continue;
				}else{
					sl_en_lines_new.addAll(temp);
					paircounter++;
					en_sl_pairs_new.add(ttt);
					temp.clear();
					ttt="";
					//System.out.println(paircounter);
				}
			}	
		}
		File en_sl_new2 = new File("C:/Users/vpapa/ELRC/naps/SL/EN-SL_new2.tmx");
		FileUtils.writeLines(en_sl_new2, "UTF-16LE", sl_en_lines_new,"\n");	
		List<String> en_sl_pairs_new2 = pseudoparseTMX(en_sl_new2, "UTF-16LE");

		words=0;
		for (String pair:en_sl_pairs_new2){
			words = words + FCStringUtils.getTokens(pair).size();
		}
		LOGGER.info("Number of words (space separated) in "+ en_sl_new2.getName()+ " is: "+ words);

	}


	private static String getSegs(List<String> list) {
		String res="";
		for (String a:list){
			if (a.startsWith("<seg>"))
				res = res+"\t"+a;
		}
		return res.trim();
	}


	private static List<String> changeOrder(List<String> list) {
		List<String> new_list = new ArrayList<String>();
		boolean found1 = false, found2 = false;
		String text1 = "", text2 = "";
		for (String a:list){
			if (a.startsWith("<tuv xml:lang=\"SL\">")){
				found1 = true;
				continue;
			}
			if (found1){
				text1 = a;
				found1=false;
				continue;
			}
			if (a.startsWith("<tuv xml:lang=\"EN-GB\">")){
				found2 = true;
				continue;
			}
			if (found2){
				text2 = a;
				found2=false;
				continue;
			}
			if (!text1.isEmpty() && !text2.isEmpty()){
				new_list.add("<tuv xml:lang=\"EN-GB\">");
				new_list.add(text2);
				new_list.add("</tuv>");
				new_list.add("<tuv xml:lang=\"SL\">");
				new_list.add(text1);
				new_list.add("</tuv>");
				new_list.add("</tu>");
				return new_list;
			}
			if (!text1.isEmpty() || !text2.isEmpty())
				continue;
			new_list.add(a);
		}

		return new_list;
	}


	private static List<String> pseudoparseTMX(File file, String encoding) throws IOException {
		List<String> en_sl_lines = FileUtils.readLines(file, encoding);
		//System.out.println(en_sl_lines.size());
		String en_text = "";
		String sl_text = "";
		boolean found_en = false;
		boolean found_sl = false;
		List<String> en_sl_pairs = new ArrayList<String>();
		for (String line:en_sl_lines){
			if (line.startsWith(en_line)){
				found_en=true;
				continue;
			}
			if (found_en){
				en_text= line;
				found_en=false;
				continue;
			}
			if (line.startsWith(sl_line)){
				found_sl=true;
				continue;
			}
			if (found_sl){
				sl_text= line;
				found_sl=false;
				continue;
			}
			if (!en_text.isEmpty() && !sl_text.isEmpty()){
				en_sl_pairs.add(en_text+"\t"+sl_text);
				en_text="";
				sl_text="";
			}
		}
		return en_sl_pairs;
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


	private static List<String> cleanABUMonoSets(List<String> mono, List<String> tep, List<String> dep) {
		List<String> res = new ArrayList<String>();
		Set<String> step = new HashSet<String>();
		Set<String> sdep = new HashSet<String>();
		Set<String> dup = new HashSet<String>();
		System.out.println(mono.size());
		for (String str:tep){
			step.add(ContentNormalizer.normtext(str));
		}
		for (String str:dep){
			sdep.add(ContentNormalizer.normtext(str));
		}
		for (String str:mono){
			String t = ContentNormalizer.normtext(str);
			if (step.contains(t) || sdep.contains(t) || dup.contains(t)){
				continue;
			}
			dup.add(t);
			res.add(str);
		}
		System.out.println(dup.size());
		System.out.println(res.size());
		return res;
	}


	private static void generateABUsets(File tf, File cf) throws FileNotFoundException {
		List<String> sitesforTRAIN = new ArrayList<String>();
		sitesforTRAIN.add("http://www.discovergreece.com");
		sitesforTRAIN.add("http://www.visitgreece.gr");
		sitesforTRAIN.add("http://www.ancient-greek-history.com");
		sitesforTRAIN.add("http://www.skiathos-tours.gr");
		sitesforTRAIN.add("https://www.greekhotels.gr");
		sitesforTRAIN.add("http://www.greekhotels.gr;https://www.greekhotels.gr");
		sitesforTRAIN.add("https://www.greekhotels.gr;http://www.greekhotels.gr");		
		sitesforTRAIN.add("http://www.greekhotels.gr");
		sitesforTRAIN.add("http://www.rockwavefestival.gr");
		sitesforTRAIN.add("http://www.gnhm.gr");
		sitesforTRAIN.add("http://www.ariahotels.gr");
		sitesforTRAIN.add("http://www.visitnorthgreece.com");
		sitesforTRAIN.add("http://www.sifnos.gr");
		sitesforTRAIN.add("http://www.lefkadaslowguide.gr");
		sitesforTRAIN.add("http://thisisnaxos.gr");
		sitesforTRAIN.add("http://www.koufonisia.gr");
		sitesforTRAIN.add("http://www.milos-tours.gr");
		sitesforTRAIN.add("http://www.naxos-tours.gr");
		sitesforTRAIN.add("http://www.santorini.gr");
		sitesforTRAIN.add("http://www.pakanellopoulosfoundation.org");
		sitesforTRAIN.add("http://www.e-kyklades.gr");
		sitesforTRAIN.add("http://www.discovernafplio.gr");
		sitesforTRAIN.add("http://www.patmos-island.com");
		sitesforTRAIN.add("http://www.kefalonia-tours.gr");
		sitesforTRAIN.add("http://skopelosweb.gr");
		sitesforTRAIN.add("http://www.e-thasos.gr");
		sitesforTRAIN.add("http://www.tinosecret.gr");
		sitesforTRAIN.add("http://travelphoto.gr");
		sitesforTRAIN.add("http://alonissos.gr");
		sitesforTRAIN.add("http://visit.samos.gr");
		sitesforTRAIN.add("http://www.kythnos.gr");
		sitesforTRAIN.add("http://www.theacropolismuseum.gr");
		sitesforTRAIN.add("http://www.gozakynthos.gr");
		sitesforTRAIN.add("http://www.e-zakynthos.com");			
		sitesforTRAIN.add("http://www.jewishmuseum.gr");
		sitesforTRAIN.add("http://www.go-thassos.gr");
		sitesforTRAIN.add("http://www.tinos-about.gr");
		sitesforTRAIN.add("http://www.chios-greece.gr");
		sitesforTRAIN.add("http://www.benaki.gr");
		sitesforTRAIN.add("http://www.nisyros.gr");
		sitesforTRAIN.add("http://www.kimolos.gr");
		sitesforTRAIN.add("http://greekfestival.gr");
		sitesforTRAIN.add("http://www.nestoriohotel.gr");
		sitesforTRAIN.add("http://www.kosinfo.gr");
		sitesforTRAIN.add("http://www.patmos.gr");
		sitesforTRAIN.add("http://astypalaia-island.gr");
		sitesforTRAIN.add("http://www.kithera.gr");
		sitesforTRAIN.add("http://moca-andros.gr;http://www.moca-andros.gr");
		sitesforTRAIN.add("http://www.moca-andros.gr;http://moca-andros.gr");
		sitesforTRAIN.add("http://moca-andros.gr");
		sitesforTRAIN.add("http://www.nationalgallery.gr");
		sitesforTRAIN.add("http://tinos360.gr");
		sitesforTRAIN.add("http://www.moca-andros.gr");
		sitesforTRAIN.add("http://www.andros.gr");
		sitesforTRAIN.add("http://www.chiosonline.gr");
		sitesforTRAIN.add("http://www.dodecanese-islands.com");
		sitesforTRAIN.add("http://www.paros.gr");
		sitesforTRAIN.add("http://www.rodosisland.gr");
		sitesforTRAIN.add("http://www.visitdelphi.gr");		
		sitesforTRAIN.add("http://www.united-hellas.com");
		sitesforTRAIN.add("http://www.nhmuseum.gr");
		sitesforTRAIN.add("http://www.namuseum.gr");
		sitesforTRAIN.add("http://www.parosweb.com");
		sitesforTRAIN.add("http://skyros-island.gr");
		sitesforTRAIN.add("http://www.astipalea.org");
		sitesforTRAIN.add("http://www.porto-klaras.gr");
		sitesforTRAIN.add("http://lesvosgreece.gr");
		sitesforTRAIN.add("http://www.amorgos-island.gr");
		sitesforTRAIN.add("http://www.thrakipalace.gr");
		sitesforTRAIN.add("http://www.naxosisland.eu");
		sitesforTRAIN.add("http://www.cycladic.gr");	
		sitesforTRAIN.add("http://www.antiparos.gr");
		sitesforTRAIN.add("http://www.zanteweb.gr");
		sitesforTRAIN.add("http://www.mykonos.gr");
		sitesforTRAIN.add("http://europeanyouthcard.gr");
		sitesforTRAIN.add("http://www.visitmarathon.gr");
		sitesforTRAIN.add("http://www.ime.gr");
		sitesforTRAIN.add("http://www.byzantinemuseum.gr");
		sitesforTRAIN.add("http://www.mmca.org.gr");
		sitesforTRAIN.add("http://www.emst.gr");
		sitesforTRAIN.add("http://www.aigai.gr");
		sitesforTRAIN.add("http://www.gtp.gr");
		sitesforTRAIN.add("http://www.n-t.gr");
		sitesforTRAIN.add("http://odysseus.culture.gr");
		sitesforTRAIN.add("http://www.historical-museum.gr");
		sitesforTRAIN.add("http://www.athensmuseums.net");
		sitesforTRAIN.add("http://www.averoffmuseum.gr");
		sitesforTRAIN.add("http://www.art-athina.gr");
		sitesforTRAIN.add("http://www.tactualmuseum.gr");
		sitesforTRAIN.add("http://www.vrellis.gr");
		sitesforTRAIN.add("http://www.cact.gr");
		sitesforTRAIN.add("http://www.adgallery.gr;http://adgallery.gr");
		sitesforTRAIN.add("http://adgallery.gr");
		sitesforTRAIN.add("http://www.adgallery.gr");
		sitesforTRAIN.add("http://adgallery.gr;http://www.adgallery.gr");
		sitesforTRAIN.add("http://www.artcorfu.com");
		sitesforTRAIN.add("http://www.mbp.gr");
		sitesforTRAIN.add("http://www.greecevirtual.gr");
		sitesforTRAIN.add("http://elinepa.org");
		sitesforTRAIN.add("http://www.aquaworld-crete.com");
		sitesforTRAIN.add("http://www.spyropoulosfoundation.org");
		sitesforTRAIN.add("http://www.yppo.gr");
		sitesforTRAIN.add("http://www.boccf.org");
		sitesforTRAIN.add("http://www.lesvosmuseum.gr");
		sitesforTRAIN.add("http://www.thmphoto.gr");
		sitesforTRAIN.add("http://www.cretaquarium.gr");
		sitesforTRAIN.add("http://nimac.org.cy");
		sitesforTRAIN.add("http://www.mcw.gov.cy");
		sitesforTRAIN.add("http://www.felioscollection.gr");
		sitesforTRAIN.add("http://www.cca.gr");
		sitesforTRAIN.add("http://www.biol.uoa.gr");
		sitesforTRAIN.add("http://andros.gr");
		sitesforTRAIN.add("http://andros.gr;http://www.andros.gr");
		sitesforTRAIN.add("http://www.andros.gr;http://andros.gr");
		sitesforTRAIN.add("http://www.gtp.gr;https://www.gtp.gr");
		sitesforTRAIN.add("http://www.skopelosweb.gr;http://skopelosweb.gr");
		sitesforTRAIN.add("http://skopeloshotels.eu");
		sitesforTRAIN.add("http://skopeloshotels.eu;http://www.skopeloshotels.eu");
		sitesforTRAIN.add("http://www.skopeloshotels.eu;http://skopeloshotels.eu");
		sitesforTRAIN.add("http://www.mouseiodimokratias.gr");
		sitesforTRAIN.add("http://www.mar-mus-crete.gr");
		sitesforTRAIN.add("http://www.paros-tours.gr");
		sitesforTRAIN.add("http://www.miet.gr");
		sitesforTRAIN.add("http://www.zampelasart.com.cy");
		sitesforTRAIN.add("http://www.katakouzenos.gr");
		sitesforTRAIN.add("http://olympicmuseum-thessaloniki.org");
		sitesforTRAIN.add("http://www.portokea-suites.com");
		sitesforTRAIN.add("http://www.santamarinabeach.gr");
		sitesforTRAIN.add("http://www.samos.net");
		sitesforTRAIN.add("http://www.zampelasart.com");

		sitesforTRAIN.add("http://anema.gr");
		sitesforTRAIN.add("http://www.parknafplio.gr");
		sitesforTRAIN.add("http://paleo-museum.uoa.gr");
		sitesforTRAIN.add("http://www.brasilhotel.gr");
		sitesforTRAIN.add("http://www.petrino-alonissos.gr");
		sitesforTRAIN.add("http://ios.gr");
		sitesforTRAIN.add("http://tickets.greekfestival.gr");
		sitesforTRAIN.add("http://www.coralikos.com");
		sitesforTRAIN.add("http://www.hellenicmotormuseum.gr");
		sitesforTRAIN.add("http://www.ammosandros.gr");
		sitesforTRAIN.add("http://www.kentrolaografias.gr");
		sitesforTRAIN.add("http://www.noesis.edu.gr");
		sitesforTRAIN.add("http://m.milos-nefelistudios.gr");
		sitesforTRAIN.add("http://www.avis.gr");
		sitesforTRAIN.add("http://greekvillaislands.com");
		sitesforTRAIN.add("http://www.kos-island.gr;http://kos-island.gr");
		sitesforTRAIN.add("http://www.frissirasmuseum.com");
		sitesforTRAIN.add("http://www.hellenicmotormuseum.gr");
		sitesforTRAIN.add("http://www.halepa.com");
		sitesforTRAIN.add("http://www.aithra-andros.gr");
		sitesforTRAIN.add("http://www.hotelakrotiri.com");
		sitesforTRAIN.add("http://www.enetikoresort.com");
		sitesforTRAIN.add("http://www.ostria-andros.gr");
		sitesforTRAIN.add("http://paleo.cnhotelgroup.com");
		sitesforTRAIN.add("http://www.villagiasemi-andros.gr;http://villagiasemi-andros.gr");
		sitesforTRAIN.add("http://www.hotelastron.com");
		sitesforTRAIN.add("https://applications.europeanyouthcard.gr");
		sitesforTRAIN.add("http://www.seaviewvillage.gr");
		sitesforTRAIN.add("https://www.visitmeteora.travel");
		//sitesforTRAIN.add("http://www.eloundapalm-crete.com");

		HashMap<String, Integer> devsitesT = new HashMap<String, Integer>();
		HashMap<String, Integer> testsitesT = new HashMap<String, Integer>();
		HashMap<String, Integer> trainsitesT = new HashMap<String, Integer>();

		List<Tu> de = new ArrayList<Tu>();
		List<Tu> te = new ArrayList<Tu>();
		List<Tu> tr = new ArrayList<Tu>();

		Tmx ttmx, ctmx;
		ttmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tf.getAbsolutePath()));
		ctmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(cf.getAbsolutePath()));
		List<Tu> ttus = ttmx.getBody().getTu();
		String site ="";
		for (Tu ttu: ttus) {
			List<Object> tuProps = ttu.getNoteOrProp(); //System.out.print(ttu.getTuid()+"\t");
			for (Object obProp : tuProps) {
				Prop prop = (Prop) obProp;
				if (prop.getType().equals(SITE)){ 	site = prop.getContent().get(0);}	//System.out.println(site);		
				if (prop.getType().equals(INFO)){
					if (!prop.getContent().isEmpty()){ 	break;}//System.out.println(prop.getContent().get(0));
					if (!sitesforTRAIN.contains(site)){
						if (testsitesT.containsKey(site)){	te.add(ttu);	int num = testsitesT.get(site)+1;	testsitesT.put(site, num);		break;
						}else{
							if (te.size()<1100){	testsitesT.put(site,1);		te.add(ttu);		break;			}
						}	//System.out.println("test & dev selected");

						if (devsitesT.containsKey(site) ){
							de.add(ttu);		int num = devsitesT.get(site)+1;							devsitesT.put(site, num);							break;
						}else{
							if (de.size()<1400){	devsitesT.put(site,1);			de.add(ttu);	break;			}
						}
					}
					if (!trainsitesT.containsKey(site)){	trainsitesT.put(site,1);	tr.add(ttu);	break;
					}else{		int num = trainsitesT.get(site)+1;	tr.add(ttu);	trainsitesT.put(site, num);		break;	}
				}
			}
		}
		System.out.println("TEST:\t"+ te.size());
		System.out.println("DEV:\t"+ de.size());
		System.out.println("TRAIN:\t"+ tr.size());
		System.out.println("---------TOURISM----------------------");

		List<Tu> ctus = ctmx.getBody().getTu();
		site ="";
		for (Tu ttu: ctus) {
			List<Object> tuProps = ttu.getNoteOrProp(); //System.out.print(ttu.getTuid()+"\t");
			for (Object obProp : tuProps) {
				Prop prop = (Prop) obProp;
				if (prop.getType().equals(SITE)){ 	site = prop.getContent().get(0);}	//System.out.println(site);		
				if (prop.getType().equals(INFO)){
					if (!prop.getContent().isEmpty()){ 	break;}//System.out.println(prop.getContent().get(0));
					if (!sitesforTRAIN.contains(site)){
						if (devsitesT.containsKey("C"+site)){
							de.add(ttu);		int num = devsitesT.get("C"+site)+1;							devsitesT.put("C"+site, num);							break;
						}else{
							if (de.size()<1900){	devsitesT.put("C"+site,1);			de.add(ttu);	break;			}
						}
						if (testsitesT.containsKey("C"+site)){	te.add(ttu);	int num = testsitesT.get("C"+site)+1;	testsitesT.put("C"+site, num);		break;
						}else{
							if (te.size()<2000){	testsitesT.put("C"+site,1);		te.add(ttu);		break;			}
						}	//System.out.println("test & dev selected");
					}
					if (!trainsitesT.containsKey("C"+site)){	trainsitesT.put("C"+site,1);	tr.add(ttu);	break;
					}else{		int num = trainsitesT.get("C"+site)+1;	tr.add(ttu);	trainsitesT.put("C"+site, num);		break;	}
				}
			}
		}
		System.out.println("TEST:\t"+ te.size());
		System.out.println("DEV:\t"+ de.size());
		System.out.println("TRAIN:\t"+ tr.size());

		System.out.println("--------CULTURE-----------------------");		

		List<String> testlines = tu2line(te);
		List<String> newtestlines = dedup(testlines);

		List<String> devlines = tu2line(de);
		List<String> newdevlines = dedup(devlines);
		//FileUtils.writeLines(new File(FilenameUtils.concat(tf.getParent(), "last_devset.tcv")), Constants.UTF8, devlines,"\n");

		List<String> trainlines = tu2line(tr);
		List<String> newtrainlines = dedup(trainlines);
		//FileUtils.writeLines(new File(FilenameUtils.concat(tf.getParent(), "last_devset.tcv")), Constants.UTF8, trainlines,"\n");

		System.out.println("TEST:\t"+ newtestlines.size());
		System.out.println("DEV:\t"+ newdevlines.size());
		System.out.println("TRAIN:\t"+ newtrainlines.size());

		System.out.println("--------------INTER-DEDUP-----------------");		

		newtestlines = dedup(newtestlines, newtrainlines);
		newdevlines = dedup(newdevlines, newtrainlines);
		newdevlines = dedup(newdevlines , newtestlines);

		System.out.println("TEST:\t"+ newtestlines.size());
		System.out.println("DEV:\t"+ newdevlines.size());
		System.out.println("TRAIN:\t"+ newtrainlines.size());

		System.out.println("--------------INTRA-DEDUP-----------------");

		try {
			FileUtils.writeLines(new File(FilenameUtils.concat(tf.getParent(), "last_testset.tcv")), Constants.UTF8, newtestlines,"\n");
			FileUtils.writeLines(new File(FilenameUtils.concat(tf.getParent(), "last_devset.tcv")), Constants.UTF8, newdevlines,"\n");
			FileUtils.writeLines(new File(FilenameUtils.concat(tf.getParent(), "last_trainset.tcv")), Constants.UTF8, newtrainlines,"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println("TEST:\t"+ testsitesT);
		//		System.out.println("TEST:\t"+ devsitesT);
		//		System.out.println("TEST:\t"+ trainsitesT);

	}


	private static List<String> dedup(List<String> lines1,List<String> lines2) {

		List<String> res = new ArrayList<String>();
		Set<String> lines2s = new HashSet<String>();
		for (String line2:lines2){
			String[] tt = line2.split("\t");
			lines2s.add(ContentNormalizer.normtext(tt[1] + Constants.SPACE + tt[2]));
		}
		for (String line1:lines1){
			String[] t = line1.split("\t");
			String temp = ContentNormalizer.normtext(t[1]+ Constants.SPACE +t[2]);
			if (!lines2s.contains(temp)){
				res.add(line1);
			}//else{
			//System.out.println(line1);
			//}
		}
		return res;
	}


	private static List<String> dedup(List<String> lines) {
		Set<String> tests = new HashSet<String>();
		List<String> newlines = new ArrayList<String>();
		for (String line:lines){
			String temp = ContentNormalizer.normtext(line.split("\t")[1]+Constants.SPACE+line.split("\t")[2]);
			if (!tests.contains(temp)){
				tests.add(temp);
				newlines.add(line);
			}else{
				//System.out.println(temp);
			}
		}
		return newlines;
	}


	private static List<String> tu2line(List<Tu> te) {
		List<String> res= new ArrayList<String>();
		for (Tu tu:te){
			List<Tuv> tuvs = tu.getTuv();
			String id = tu.getTuid();
			String seg1 = getSegment(tuvs.get(0).getSeg());
			String seg2 = getSegment(tuvs.get(1).getSeg());
			String score = getProp(tu, "score");
			String ratio = getProp(tu, "lengthRatio");
			String info = getProp(tu, "info");
			res.add(id+"\t"+seg2+"\t"+seg1+"\t"+score+"\t"+ratio+"\t"+info);
		}
		return res;
	}


	private static String getProp(Tu tu, String propName) {
		List<Object> tuProps = tu.getNoteOrProp();
		String propValue="NULL";
		for (Object obProp : tuProps) {
			Prop prop = (Prop) obProp;
			if (prop.getType().equals(propName)){ 
				if ( !prop.getContent().isEmpty())
					propValue = prop.getContent().get(0);
				break;
			}
		}
		return propValue;
	}


	private static void renameCESfiles(File indir, File outdir, String suffix, String sep) {
		File[] files=indir.listFiles();
		if (!outdir.exists())
			outdir.mkdirs();
		int counter=0;
		for (File file:files){
			if (file.getName().endsWith(suffix)){
				//String[] parts = file.getName().split(sep);
				//if (parts.length<2)
				//	continue;
				File newfile =  new File(FilenameUtils.concat(outdir.getAbsolutePath(), counter+"."+suffix));
				try {
					FileUtils.copyFile(file, newfile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				counter++;
			}
		}
	}

	private static void several_specific_helpful_tasks() throws IOException {

		sample_selection("C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\delivered\\alignOutput2014-05-26_024952.log");

		count_tmx_scores("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih", "alignOutput2014-05-23_105241.txt", "scores.txt");

		correct_select_count_tmx("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih", "alignOutput2014-05-23_105241.txt", 0.4);

		select_copy_docpairs("C:\\QTLaunchPad\\MEDICAL\\EN-PT", "C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih",
				"C:\\QTLaunchPad\\MEDICAL\\EN-PT\\outputs\\output.txt" ,"u i h");

		check_bilingual_collection("C:\\QTLaunchPad\\AUTOMOTIVE\\EN-PT\\", "en;pt","output_pdfs.txt" );

		merge_outlist_files("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih\\outputs","output.txt");

		//task1//
		String parent_dir  ="C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\"; 
		String pair_list ="C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\total_pairs.txt";
		//task2//
		String list_with_attricutes_of_files =
				"C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\EN_AUTOMOTIVE_results_nonCC.txt";
		//task3/
		String xml_dir="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		String xmlCC_dir="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml_CC";
		String nonCC_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\output_AUTOMOTIVE_EN_nonCC.txt";
		String CC_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\output_AUTOMOTIVE_EN_CC.txt";
		String licensed_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\AUTOMOTIVE_EN_licensed.txt";
		String pathstring="qtlp_20131227_132408/e401c679-d4d1-4099-8711-a3b97d634614/xml/";
		//task4/
		String target_dir =
				"C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		String exten="xml";
		String exclude_str="crawlinfo=";
		//task5//
		String removefilelist="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\to_remove.txt";
		String path_file_t="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		int task=4;

		if (task==1){
			pdf_pairs(pair_list, parent_dir);
			System.out.println("existance of pdf files in list of pairs is checked");
		}
		if (task==2){
			//list of attributes are extracted by QueryXMLwithXpath class
			print_unique_sites_in_collection(list_with_attricutes_of_files);
			System.out.println("unique sites in collection is printed");
		}
		if (task==3){
			discriminate_CC_from_nonCC(xml_dir, xmlCC_dir, licensed_list, nonCC_list, CC_list, pathstring);
			System.out.println("files with CC licensed are moved from XML to XML_CC");
		}
		if (task==4){
			counttokens(target_dir,exten,exclude_str);
			System.out.println("tokens calculated.");
		}
		if (task==5){
			String tmp1 = FileUtils.readFileToString(new File(removefilelist), Constants.UTF8);
			String[] filestoremove = tmp1.split("\n");	
			ArrayList<String> filestoremoveList=new ArrayList<String>();
			for (int ii=0;ii<filestoremove.length;ii++){
				filestoremoveList.add(filestoremove[ii]);
			}
			File xmldir = new File(path_file_t);
			String[] filesinXML= xmldir.list();
			for (int ii=0;ii<filesinXML.length;ii++){
				if (filesinXML[ii].endsWith("xml")){
					if (filestoremoveList.contains(filesinXML[ii])){
						File tempfile=new File(path_file_t+"\\"+filesinXML[ii]);
						System.out.println(tempfile.getName());
						tempfile.delete();
						tempfile=new File(path_file_t+"\\"+filesinXML[ii]+appHTMLext);
						System.out.println(tempfile.getName());
						tempfile.delete();
						String tt = filesinXML[ii].substring(0, filesinXML[ii].indexOf("."));
						tempfile=new File(path_file_t+"\\"+tt+appHTMLext);
						System.out.println(tempfile.getName());
						tempfile.delete();
					}
				}
			}
		}
	}

	private static void sample_selection(String tmx_listfile) {
		String[] types =new String[ ] {"u","i","h", "l", "m", "pdf"};
		int[] tmxfile_counts = new int[types.length], selected_tmxfile_counts = new int[types.length];
		int[] tmx_counts = new int[types.length], selected_tmx_counts = new int[types.length];
		boolean found=false;
		int total_tmx=0, totaltmxfile=0; 
		double sample_factor = 0.05, sample_factor1 = 0.05; 
		try {
			List<String> tmx_files=FileUtils.readLines(new File(tmx_listfile), Constants.UTF8);
			for (int ii=0;ii<tmx_files.size();ii++){
				if (!tmx_files.get(ii).startsWith("align"))
					continue;

				String[] temp = tmx_files.get(ii).split(Constants.SPACE);
				found = false;
				for (int jj=0;jj<types.length;jj++){
					if (temp[0].endsWith(types[jj]+".tmx")){
						found=true;
						tmx_counts[jj]=tmx_counts[jj]+Integer.parseInt(temp[2]);
						tmxfile_counts[jj]=tmxfile_counts[jj]+1;
						break;
					}
				}
				if (!found){
					System.out.println("EP. Somethng goes wrong, since the type is not recognised.");
					System.exit(0);
				}
			}
			for (int jj=0;jj<tmx_counts.length;jj++){
				total_tmx = total_tmx + tmx_counts[jj];
				totaltmxfile = totaltmxfile + tmxfile_counts[jj];
			}
			System.out.println("total tmxfiles: "+ totaltmxfile);
			System.out.println("total tmx sentence pairs: "+ total_tmx);

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter sample size of files (Integer):");
			int sample_files=0;
			try{
				sample_files = Integer.parseInt(br.readLine());
			}catch(NumberFormatException nfe){
				System.err.println("Invalid Format!");
			}
			System.out.print("Enter sample size of sentence pairs (Integer):");
			int sample_sentencepairs=0;
			try{
				sample_sentencepairs = Integer.parseInt(br.readLine());
			}catch(NumberFormatException nfe){
				System.err.println("Invalid Format!");
			}
			System.out.println("files to be selected: " + sample_files);
			System.out.println("files to be selected: " + sample_sentencepairs);
			sample_factor = (double) sample_files / (double) totaltmxfile;
			sample_factor1 = (double) sample_sentencepairs / (double) total_tmx;
			for (int jj=0;jj<tmx_counts.length;jj++){
				selected_tmxfile_counts[jj] = (int) Math.round(sample_factor*tmxfile_counts[jj]);
				selected_tmx_counts[jj] = (int) Math.round(sample_factor1*tmx_counts[jj]);
			}
			//			System.out.println("aaa");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void count_tmx_scores(String location, String tmx_list_file, String score_text) throws IOException {
		String score="";
		List<String> pairlist =  FileUtils.readLines(new File(FilenameUtils.concat(location,tmx_list_file)), Constants.UTF8);
		for (int ii=0;ii<pairlist.size();ii++){
			LOGGER.debug("ID:\t"+ii);
			LOGGER.debug(FilenameUtils.concat(location,pairlist.get(ii)));
			score = score+ 
					ReadResources.extractAttrfromXML(FilenameUtils.concat(location,pairlist.get(ii)),"tu", "score", true,false);
			if (score.isEmpty())
				continue;

		}
		//WriteResources.writetextfile(FilenameUtils.concat(location,score_text), score);
		FileUtils.writeStringToFile(new File(FilenameUtils.concat(location,score_text)), score, Constants.UTF8);
	}

	private static void merge_outlist_files(String sourcedir, String newfile) throws IOException {
		File curdir = new File(sourcedir);
		File[] files= curdir.listFiles();

		String whole_text = "";
		String filename = FilenameUtils.concat(curdir.getAbsolutePath(),newfile);
		for (File file:files){
			if (file.getName().endsWith(".txt") & file.getName().startsWith("out")){
				String text =FileUtils.readFileToString(file, Constants.UTF8);
				whole_text=whole_text+"\n"+text;
			}
		}
		//WriteResources.writetextfile(filename, whole_text);
		FileUtils.writeStringToFile(new File(filename), whole_text, Constants.UTF8);
	}

	private static void check_bilingual_collection(String source_path,	String langs1, String output_list_file) throws IOException {
		int p_xml=0, p_xmlhtml=0, xml1=0, xml2=0, html1=0, html2=0, xmlhtml1=0, xmlhtml2=0;
		String[] langs = langs1.split(";");
		int[] tokens = new int[2];
		ArrayList<String> fff = new ArrayList<String>();
		List<String> pairs =FileUtils.readLines(new File(FilenameUtils.concat(source_path,output_list_file)), Constants.UTF8);

		for (int ii=0; ii<pairs.size();ii++){
			pairs.set(ii, pairs.get(ii).replace("/", "\\"));
			if (pairs.get(ii).startsWith("pdfs") )
				continue;
			File pairfile = new File(source_path+"\\"+pairs.get(ii));
			String pparent = pairfile.getParent();
			String pname =  pairfile.getName();
			String[] items = pname.split("_");

			//xml pair
			if (pairfile.exists())
				p_xml++;
			else
				LOGGER.info(pairfile.getAbsolutePath() + " does not exist.");
			//xmlhtml pair
			pairfile = new File(FilenameUtils.concat(source_path,pairs.get(ii)+appHTMLext));
			if (pairfile.exists())
				p_xmlhtml++;
			else
				LOGGER.info(pairfile.getAbsolutePath() + " does not exist.");

			File file1 = new File(FilenameUtils.concat(pparent,items[0]+appXMLext));
			File file2 = new File(FilenameUtils.concat(pparent,items[1]+appXMLext));
			//xml 1
			if (file1.exists()){
				//String langIdentified = ReadResources.extractLangfromXML(file1.getAbsolutePath(), "language", "iso639");

				String langIdentified = ReadResources.extractAttrfromXML(file1.getAbsolutePath(), "language", "iso639", true,false);
				String text_temp = ReadResources.extractTextfromXML_clean(file1.getAbsolutePath(),type_p,"crawlinfo", false);

				if (langIdentified.equals(langs[0]))
					tokens[0]=tokens[0]+FCStringUtils.countTokens(text_temp,langIdentified);
				else
					tokens[1]=tokens[1]+FCStringUtils.countTokens(text_temp,langIdentified);
				xml1++;

				//ReadResources.writetextfile(source_path+"\\"+langs[0]+"\\"+xml1+".txt", text_temp);
				//FileUtils.copyFile(file1, new File(source_path+"\\"+langs[0]+"\\"+xml1+appXMLext));
			}else
				LOGGER.info(file1.getAbsolutePath() + " does not exist.");
			//xml2
			if (file2.exists()){
				//String langIdentified = ReadResources.extractLangfromXML(file2.getAbsolutePath(), "language", "iso639");
				String langIdentified = ReadResources.extractAttrfromXML(file1.getAbsolutePath(), "language", "iso639", true, false);
				String text_temp = ReadResources.extractTextfromXML_clean(file2.getAbsolutePath(),type_p,"crawlinfo", false);
				if (langIdentified.equals(langs[0]))
					tokens[0]=tokens[0]+FCStringUtils.countTokens(text_temp,langIdentified);
				else
					tokens[1]=tokens[1]+FCStringUtils.countTokens(text_temp,langIdentified);
				xml2++;
				//ReadResources.writetextfile(source_path+"\\"+langs[1]+"\\"+xml1+".txt", text_temp);
				//FileUtils.copyFile(file2, new File(source_path+"\\"+langs[1]+"\\"+xml2+appXMLext));
			}else
				LOGGER.info(file2.getAbsolutePath() + " does not exist.");

			if (fff.contains(file1.getAbsolutePath()))
				LOGGER.info("EEEP	" +  file1.getAbsolutePath());
			else
				fff.add(file1.getAbsolutePath());
			if (fff.contains(file2.getAbsolutePath()))
				LOGGER.info("EEEP	" +  file2.getAbsolutePath());
			else
				fff.add(file2.getAbsolutePath());

			file1 = new File(pparent+"\\"+items[0]+appHTMLext);
			file2 = new File(pparent+"\\"+items[1]+appHTMLext);
			//html 1
			if (file1.exists())
				html1++;
			else
				LOGGER.info(file1.getAbsolutePath() + " does not exist.");
			//html2
			if (file2.exists())
				html2++;
			else
				LOGGER.info(file2.getAbsolutePath() + " does not exist.");

			file1 = new File(pparent+"\\"+items[0]+appXMLHTMLext);
			file2 = new File(pparent+"\\"+items[1]+appXMLHTMLext);
			//xmlhtml 1
			if (file1.exists())
				xmlhtml1++;
			else
				LOGGER.info(file1.getAbsolutePath() + " does not exist.");
			//xmlhtml2
			if (file2.exists())
				xmlhtml2++;
			else
				LOGGER.info(file2.getAbsolutePath() + " does not exist.");
		}
		LOGGER.info(p_xml +"\t"+ p_xmlhtml+"\t"+ xml1+"\t"+xml2+"\t"+html1+"\t"+html2+"\t"+xmlhtml1+"\t"+xmlhtml2);
		System.out.println("DONE");
	}

	private static void select_copy_docpairs(String sourcedir1, String targetdir1, String cesAling_listfile, String type) {
		try {
			File sourcedir = new File(sourcedir1);
			File targetdir = new File(targetdir1);
			String[] pairlist =FileUtils.readFileToString(new File(cesAling_listfile), Constants.UTF8).replace("/", "\\").split("\n");
			String[] types = type.split(Constants.SPACE);
			for (int ii=0;ii<pairlist.length;ii++){	
				if (pairlist[ii].startsWith("pdfs"))
					continue;
				if (!pairlist[ii].endsWith("_"+types[0]+appXMLext) 
						& !pairlist[ii].endsWith("_"+types[1]+appXMLext) 
						& !pairlist[ii].endsWith("_"+types[2]+appXMLext))
					continue;
				LOGGER.info(ii);
				//File temppair = new File(sourcedir.getAbsolutePath()+fs+pairlist[ii]);
				//File temppairNEW = new File(targetdir.getAbsolutePath()+fs+pairlist[ii]);
				//FcFileUtils.copyFile(temppair, temppairNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii])),
						new File(FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii])));
				//FcFileUtils.copy(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]), FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]));

				//temppair = new File(sourcedir.getAbsolutePath()+"\\"+pairlist[ii]+appHTMLext);
				//temppairNEW = new File(targetdir.getAbsolutePath()+"\\"+pairlist[ii]+appHTMLext);
				//FcFileUtils.copyFile(temppair, temppairNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]+appHTMLext)),
						new File(FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]+appHTMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]+appHTMLext),
				//		FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]+appHTMLext));

				File temppair = new File(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]));
				File temppairNEW = new File(FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]+appHTMLext));
				String[] tempitems = temppair.getName().split("_");

				//File tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+appXMLext);
				//File tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[0]+appXMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appXMLext)),
						new File(FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appXMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appXMLext),
				//		FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appXMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+appXMLHTMLext);
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[0]+appXMLHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appXMLHTMLext)),
						new File(FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appXMLHTMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appXMLHTMLext),
				//		FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appXMLHTMLext));


				//tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+appHTMLext);
				//tempitemNEW = new File(tempitemNEW.getParent()+"\\"+tempitems[0]+appHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appHTMLext)),
						new File(FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appHTMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appHTMLext),
				//		FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appHTMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+appXMLext);
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[1]+appXMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appXMLext)),
						new File(FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appXMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appXMLext),
				//		FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appXMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+appXMLHTMLext);
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[1]+appXMLHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appXMLHTMLext)),
						new File(FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appXMLHTMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appXMLHTMLext),
				//		FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appXMLHTMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+appHTMLext);
				//tempitemNEW = new File(tempitemNEW.getParent()+"\\"+tempitems[1]+appHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FileUtils.copyFile(new File(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appHTMLext)),
						new File(FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appHTMLext)));
				//FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appHTMLext),
				//		FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appHTMLext));
			}
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}

	private static void correct_select_count_tmx(String location, String tmx_list_file,	double align_thr) {

		int counter_tmx=0;
		try {
			String tmx_list = FileUtils.readFileToString(new File(FilenameUtils.concat(location,tmx_list_file)), Constants.UTF8);
			String[] pairlist = tmx_list.split("\r\n");
			for (int ii=0;ii<pairlist.length;ii++){
				XMLTextCharsCleaner.clean(FilenameUtils.concat(location,pairlist[ii]), FilenameUtils.concat(location,pairlist[ii]+"1"));
				FileUtils.copyFile(new File(FilenameUtils.concat(location,pairlist[ii]+"1")),
						new File(FilenameUtils.concat(location,pairlist[ii])));
				//FcFileUtils.copy(FilenameUtils.concat(location,pairlist[ii]+"1"),FilenameUtils.concat(location,pairlist[ii]));
				File temp = new File(FilenameUtils.concat(location,pairlist[ii]+"1"));
				temp.delete();
			}
			for (int ii=0;ii<pairlist.length;ii++){
				LOGGER.info("ID:\t"+ii);
				LOGGER.info(FilenameUtils.concat(location,pairlist[ii]));
				String score = 
						ReadResources.extractAttrfromXML(FilenameUtils.concat(location,pairlist[ii]),"tu", "score", true, false);
				if (score.isEmpty())
					continue;
				String scores[] =score.split("\n");
				LOGGER.info(scores.length);
				for (int jj=0; jj<scores.length;jj++){
					if (Double.parseDouble(scores[jj])>=align_thr){
						counter_tmx++;
					}
				}
			}
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		System.out.println(counter_tmx);

	}

	private static void pdf_pairs(String pair_list, String parent_dir) {

		File tempfile = new File(pair_list);
		try {
			BufferedReader in = new BufferedReader(new FileReader(tempfile));
			String str; //, file1, file2;
			int counter=1;
			//HashMap<String, Integer> sites = new  HashMap<String, Integer>();
			while ((str = in.readLine()) != null) {
				String[] linedata = str.split("\t");			
				if (linedata.length!=2){
					System.out.println("problem in line "+counter+ " of " + pair_list);
					System.exit(0);
				}
				//file1=linedata[0].replaceAll("/", fs);		file2=linedata[1].replaceAll("/", fs);
				File pdf1=new File(FilenameUtils.concat(parent_dir,linedata[0]));
				File pdf2=new File(FilenameUtils.concat(parent_dir,linedata[1]));
				if (!pdf1.exists()){
					System.out.println("DOES NOT EXIST file "+ pdf1.getAbsolutePath() + " mentioned in line "+counter+ " of " + pair_list);
					//System.exit(0);
				}
				if (!pdf2.exists()){
					System.out.println("DOES NOT EXIST file "+ pdf2.getAbsolutePath() + " mentioned in line "+counter+ " of " + pair_list);
					//System.exit(0);
				}
				counter++;
			}
			in.close();
		} catch (IOException e) {
		}
	}


	private static void print_unique_sites_in_collection(
			String list_with_attricutes_of_files) {

		File tempfile = new File(list_with_attricutes_of_files);
		try {
			BufferedReader in = new BufferedReader(new FileReader(tempfile));
			String str, site, tempstr; //, tempsite;
			int ind1, ind2, counter; //ind3, ind4 
			HashMap<String, Integer> sites = new  HashMap<String, Integer>();
			while ((str = in.readLine()) != null) {
				String[] linedata = str.split("\t");				
				site=linedata[2];				ind1=site.indexOf("//");
				tempstr=site.substring(ind1+2);	ind2=tempstr.indexOf("/")+ind1+2;
				if (ind2>=site.length()| ind2<0)
					ind2=site.length()-1;
				site = site.substring(ind1+2, ind2);	//	ind3 = site.lastIndexOf(".");
				//tempsite = site.substring(0, ind3);			ind4 = tempsite.lastIndexOf(".");
				//site = site.substring(ind4+1);
				if (sites.containsKey(site)){
					counter = sites.get(site)+1;
					sites.put(site, counter);
				}
				else
					sites.put(site, 1);
			}
			in.close();
			Set<String> site_keys=sites.keySet();
			Iterator<String> key = site_keys.iterator();
			String key_im = "";
			while (key.hasNext()){
				key_im = key.next();
				//LOGGER.info(key_im+"\t"+sites.get(key_im));
				System.out.println(key_im+"\t"+sites.get(key_im));
			}
		} catch (IOException e) {
		}
	}


	private static void discriminate_CC_from_nonCC(String xml_dir,
			String xmlCC_dir, String licensed_list, String nonCC_list, String CC_list, String pathstring) throws IOException {

		String tmp1;
		try {
			tmp1 = FileUtils.readFileToString(new File(licensed_list), Constants.UTF8);
			String[] filestoremove = tmp1.split("\n");	
			ArrayList<String> filestoremoveList=new ArrayList<String>();
			for (int ii=0;ii<filestoremove.length;ii++)
				filestoremoveList.add(filestoremove[ii]);
			File xmldir = new File(xml_dir);
			String[] filesinXML= xmldir.list();
			int counter=0;
			for (int ii=0;ii<filesinXML.length;ii++){
				if (filesinXML[ii].endsWith("xml")){
					if (filestoremoveList.contains(filesinXML[ii])){
						String tt = filesinXML[ii].substring(0,filesinXML[ii].indexOf("."));
						//appHTMLext
						File tempfile=new File(xml_dir+"\\"+tt+appHTMLext);
						File destfile=new File(xmlCC_dir+"\\"+tt+appHTMLext);
						FileUtils.moveFile(tempfile, destfile);
						//appXMLext
						tempfile=new File(xml_dir+"\\"+filesinXML[ii]);
						destfile=new File(xmlCC_dir+"\\"+tt+appXMLext);
						FileUtils.moveFile(tempfile, destfile);
						//appXMLHTMLext
						tt=filesinXML[ii].substring(0, filesinXML[ii].lastIndexOf("."))+appXMLHTMLext;
						tempfile=new File(xml_dir+"\\"+tt);
						destfile=new File(xmlCC_dir+"\\"+tt);
						FileUtils.moveFile(tempfile, destfile);

						counter++;
						System.out.println(counter);
					}
				}
			}

		}catch (IOException e1) {
			e1.printStackTrace();
		}

		File xmldir = new File(xml_dir);
		String[] filesinXML= xmldir.list();
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("html") & !filesinXML[ii].contains("xml.html")){
				File tempfile=new File(xml_dir+"\\"+filesinXML[ii]);
				System.out.println(tempfile.getName());
				tempfile.delete();
			}
		}

		String temp_dir = xmlCC_dir;
		xmldir = new File(temp_dir);
		filesinXML= xmldir.list();
		String urlList="";
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("xml"))
				urlList=urlList + pathstring+filesinXML[ii]+"\n";
		}
		//WriteResources.writetextfile(CC_list,urlList);
		FileUtils.writeStringToFile(new File(nonCC_list), urlList, Constants.UTF8);
		writeHTMLfile(CC_list+appHTMLext,urlList,true);

		temp_dir = xml_dir;
		xmldir = new File(temp_dir);
		filesinXML= xmldir.list();
		urlList="";
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("xml"))
				urlList=urlList + pathstring+filesinXML[ii]+"\n";
		}
		//WriteResources.writetextfile(nonCC_list,urlList);

		FileUtils.writeStringToFile(new File(nonCC_list), urlList, Constants.UTF8);

		writeHTMLfile(nonCC_list+appHTMLext,urlList,true);
	}


	static void writeHTMLfile(String filename, String urlList,
			boolean applyOfflineXSLT2) {
		String outputfile1 =filename;
		String[] urls=urlList.split("\n");
		OutputStreamWriter xmlFileListWrt1;
		try {
			xmlFileListWrt1 = new OutputStreamWriter(new FileOutputStream(outputfile1),Constants.UTF8);
			xmlFileListWrt1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			for (int ii=0; ii<urls.length;ii++) {
				String ttt;// = urls[ii];//.toString();
				File xmlFile=new File(urls[ii]);
				String fileURL = new File(xmlFile.getAbsolutePath()+appHTMLext)
				.getAbsolutePath().replace("\\", "/");
				//String ttt=qq.toURI().toString();
				//URL fileURL = xmlFile.toURI().toURL();
				if (applyOfflineXSLT2)
					//ttt= "<a href=\""+fileURL+".html\">\n"+xmlFile.getAbsolutePath()+".html</a>";
					ttt= "<a href=\""+fileURL+"\">\n"+fileURL+"</a>";
				else
					//ttt= "<a href=\""+fileURL+"\">\n"+xmlFile.getAbsolutePath()+"</a>";
					ttt= "<a href=\""+xmlFile.getAbsolutePath().replace("\\", "/")+"\">\n"+
							xmlFile.getAbsolutePath().replace("\\", "/")+"</a>";

				//<a href="https://issues.apache.org/jira/browse/NUTCH-721" target="_blank">NUTCH-721</a>
				xmlFileListWrt1.write("<br />"+ttt.replace("\\","/")+"\n");
				//xmlFileListWrt.write(xmlFile.replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "")   +"\n");
			}
			xmlFileListWrt1.write("</html>");
			xmlFileListWrt1.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static String getSegment(Seg seg) {
		StringBuilder builder = new StringBuilder();
		for (Object object : seg.getContent()) {
			builder.append(object.toString());
		}
		return builder.toString();
	}


	public static void counttokens(String target_dir, String exten, String exclude_str) throws IOException {
		File xmldir = new File(target_dir);
		String[] filesinXML= xmldir.list();
		String tmp1;
		int tokens_num=0;
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith(exten)){
				tmp1 = FileUtils.readFileToString(new File(FilenameUtils.concat(target_dir,filesinXML[ii])), Constants.UTF8);
				String[] lines = tmp1.split("\n");
				for (int jj=0;jj<lines.length;jj++){
					if (lines[jj].contains("<p id=") & !lines[jj].contains(exclude_str)){
						int s=lines[jj].indexOf(">")+1;
						int e=lines[jj].length()-4;
						String temp=lines[jj].substring(s, e);
						tokens_num=tokens_num+FCStringUtils.countTokens(temp);
					}
				}
			}
		}
	}


	public static String handleCopiedSite(InputStream input) {
		String url="";
		int len = +HTTRACK1.length();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(input,Constants.UTF8));
			String nextLine="";
			while((nextLine = reader.readLine())!=null){
				int i1 = nextLine.indexOf(HTTRACK1);
				int i2 = nextLine.indexOf(HTTRACK2);
				if (i1<i2){
					url=nextLine.substring(i1+len, i2-1).trim();
					break;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	/*public static String convertStreamToString(InputStream is)
			throws IOException {

	 * To convert the InputStream to String we use the
	 * Reader.read(char[] buffer) method. We iterate until the
	 * Reader return -1 which means there's no more data to
	 * read. We use the StringWriter class to produce the string.

				if (is != null) {
					Writer writer = new StringWriter();

					char[] buffer = new char[1024];
					try {
						Reader reader = new BufferedReader(
								new InputStreamReader(is, Constants.UTF8));
						int n;
						while ((n = reader.read(buffer)) != -1) {
							writer.write(buffer, 0, n);
						}
					} finally {
						is.close();
					}
					return writer.toString();
				} else {       
					return "";
				}
	}*/



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


	public static class Record {
		public String url1;
		public String url2;
		public Double dist;

		public Record(String url1, String url2, double dist) {
			this.url1 = url1;
			this.url2 = url2;
			this.dist = dist;
		}

		@Override
		public String toString() {
			return url1 + Constants.SPACE + dist;
		}

		public int compareTo(Field field, Record record) {
			switch (field) {
			case S: return this.url1.compareTo(record.url1);
			case D: return this.dist.compareTo(record.dist);
			default: throw new IllegalArgumentException(
					"Unable to sort Records by " + field.getType());
			}
		}

	}


	class RecordComparator implements Comparator<Record> {

		private Field field;
		private Sort sort;

		public RecordComparator(Sort sort, Field field) {
			this.sort = sort;
			this.field = field;
		}

		@Override
		public final int compare(Record a, Record b) {
			int result = a.compareTo(field, b);
			if (sort == Sort.ASCENDING) return result;
			else return -result;
		}
	}


}
