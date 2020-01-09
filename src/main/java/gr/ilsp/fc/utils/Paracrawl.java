package gr.ilsp.fc.utils;

import gr.ilsp.nlp.commons.Constants;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.utils.ContentNormalizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class Paracrawl {
	private static final Logger LOGGER = Logger.getLogger(Paracrawl.class);
	//private static final String header = "en\tbg\tcs\tda\tde\tel\tet\tes\tfi\tfr\tga\thr\thu\tit\tis\tlt\tlv\tmt\tnl\tno\tpl\tpt\tro\tsk\tsl\tsv";
	private static final String header = "bg\tcs\tda\tde\tel\tes\tet\tfi\tfr\tga\thr\thu\tit\tis\tlt\tlv\tmt\tnl\tno\tpl\tpt\tro\tsk\tsl\tsv";



	public static void main(String[] args) throws IOException {

		System.out.println("---------------------ADD--------------------------------");
		System.out.println("1st argument could be : \"add\" for adding column with lang");
		System.out.println("2nd argument is the fullpath of infile (downloaded from paracrawl)");
		System.out.println("3rd argument is the language to be added");

		System.out.println("---------------------NORM--------------------------------");
		System.out.println("1st argument could be : \"norm\" for normalize the 1st column (i.e. the EN column) ");
		System.out.println("2nd argument is the fullpath of infile (downloaded from paracrawl AND 3rd column with LANG added)");

		System.out.println("---------------------KEEPMULTI--------------------------------");
		System.out.println("1st argument could be : \"keepmulti\" for keeping only multilingual pairs");
		System.out.println("2nd argument is the fullpath of infile, (file after CAT all normalized datasets and SORT");
		System.out.println("returns *.multi");

		System.out.println("---------------------PROCESSMULTI--------------------------------");
		System.out.println("1st argument could be : \"processmulti\" for generating the sparse table");
		System.out.println("2nd argument is the fullpath of infile");
		System.out.println("returns *.idmulti");

		System.out.println("---------------------RESULTMULTI--------------------------------");
		System.out.println("1st argument could be : \"resultmulti\" for multilingual pairs in specific languages");
		System.out.println("2nd argument is the fullpath of infile");
		System.out.println("3rd argument could be : targeted languages separated by ;");
		System.out.println("returns *.(targeted languages)");

		System.out.println("---------------------GETMULTI--------------------------------");
		System.out.println("1st argument could be : \"getmulti\" for generating multilingual corpus based on idmulti file");
		System.out.println("2nd argument is the fullpath of infile (the idmulti file)");
		System.out.println("returns *.corpus");

		System.out.println("---------------------FILTER--------------------------------");
		System.out.println("1st argument could be : \"filter\" for cleaning a paracrawl corpus");
		System.out.println("2nd argument is the fullpath of infile (it should be like en-X.)");
		System.out.println("returns many files .(clean, noletters, identicalTUVs, ratioTUVs, shortTUVs, addrs, cases, dupTUs, wronglang)");
		
		/*System.out.println("-------------------DENORMALIZE----------------------------------");
		System.out.println("1st argument could be : \"denorm\" for finding common EN parts");
		System.out.println("2nd argument is the fullpath of normalized infile");
		System.out.println("3rd argument is the fullpath of original infile");*/

		String mode = args[0];
		File f1, f2=null, f3, f4;

		//Step 1. ADD LANG AT THE END OF EACH LINE
		//File f1 = new File("C:/Users/vpapa/ELRC/paracrawl_v5/en-bg.bicleaner07");
		if (mode.equals("add")){
			f1 = new File(args[1]);
			String lang = args[2];
			f2 = addlang(f1, lang);
		}

		//Step 2. CAT FILE ON LINUX

		//Step 3. NORMALIZE MERGED FILE
		if (mode.equals("norm")){
			f1 = new File(args[1]);
			f2 = normalizeContent(f1);
		}

		if (mode.equals("keepmulti")){
			f1 = new File(args[1]);
			f2 = keepMultilinguality(f1);
		}
		if (mode.equals("processmulti")){
			f1 = new File(args[1]);
			f2 = processMultilinguality(f1);
		}
		if (mode.equals("resultmulti")){
			f1 = new File(args[1]);
			String[] langs = args[2].split(Constants.SEMICOLON);
			f2 = resultMultilinguality(f1, langs);
		}
		if (mode.equals("getmulti")){
			f1 = new File(args[1]);
			f2 = getMultilinguality(f1);
		}
		
		if (mode.equals("filter")){
			File f11 = new File(args[1]);
			f1 = f11.getAbsoluteFile();
			filterparacrawlTest(f1);
		}
		/*if (mode.equals("getmulti_")){
			f1 = new File(args[1]);
			f2 = getMultilinguality(f1);
		}*/
		
		
		LOGGER.info("Result in "+ f2.getAbsolutePath());


		//Step 4. SORT NORMALIZED MERGED FILE ON LINUX

		//Step 5. FIND MULTI-LINGUAL
		if (mode.equals("comm")){
			f1 = new File(args[1]);
			int range = 2;
			f2 = findCommInSorted(f1, range);
		}
		//Step 6. BACK TO THE ORIGINAL EN PART
		if (mode.equals("denorm")){
			f1 = new File(args[1]);
			f2 = new File(args[2]);
			f3 = new File(args[3]);
			f4 = denormalize(f1, f2, f3);
			LOGGER.info("Result in "+ f4.getAbsolutePath());
		}
	}

	private static File getMultilinguality(File f11) throws IOException {
		File f1 = f11.getAbsoluteFile();
		LOGGER.info(f1.getAbsolutePath());
		String basePath = f1.getParent();
		LOGGER.info(basePath);
		
		List<String> ids = FileUtils.readLines(f1, Constants.UTF8);
		LOGGER.info(ids.get(0));
		String[] heads = ids.get(0).split(Constants.TAB);
		for (int ii=0;ii<heads.length;ii++)
			LOGGER.info(heads[ii]);
		String temp = "";
				
		Map<String, List<String>> tuples = new HashMap<String, List<String>>();
		for (int ii=0;ii<heads.length;ii++){
			LOGGER.info("Getting en-" +heads[ii] + " ...");
			temp = FilenameUtils.concat(basePath, "en"+Constants.HYPHEN+heads[ii]+".bicleaner07.lang");
			
			LOGGER.info("Reading "+ temp);
			List<String> templist = FileUtils.readLines(new File(temp));
			List<String> newtemplist = new ArrayList<String>();
			for (int jj=1;jj<ids.size();jj++){
				int id = Integer.parseInt(ids.get(jj).split(Constants.TAB)[ii]);
				//System.out.println(id +"\t:\t"+templist.get(id-1));
				newtemplist.add(templist.get(id-1));
			}
			templist = null;
			List<String> line= new ArrayList<String>();
			List<String> en= new ArrayList<String>();
			List<String> cur= new ArrayList<String>();
			for (int jj=0;jj<newtemplist.size();jj++){
				line = Arrays.asList(newtemplist.get(jj).split(Constants.TAB));
				en.add(line.get(0));
				cur.add(line.get(1));
			}
			tuples.put("en-"+heads[ii], en);
			LOGGER.info("en-"+heads[ii]+":\t"+en.size());
			tuples.put(heads[ii], cur);
			LOGGER.info(heads[ii]+":\t"+cur.size());
			newtemplist = null;
		}
		File f2 = new File(f1.getAbsolutePath()+".corpus");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String headline = "";
		for (String head:heads)
			headline = headline+Constants.TAB+head;
		out.write(headline.trim()+"\n");
		
		//Set<String> keys = tuples.keySet();
		int tuplen = tuples.get(heads[0]).size();
		for (int ii=0;ii<heads.length;ii++){
			if (tuples.get(heads[ii]).size()!=tuplen)
				LOGGER.error("Some sentences missins");
			if (tuples.get("en-"+heads[ii]).size()!=tuplen)
				LOGGER.error("Some sentences missins");
		}
		String currentEN="", currentline="";
		int counter=0;
		for (int jj=0;jj<tuplen;jj++){
			for (int ii=0;ii<heads.length;ii++){
				temp = tuples.get("en-"+heads[ii]).get(jj);
				if (currentEN.isEmpty())
					currentEN=temp;
				else{
					if (!currentEN.equals(temp)){
						LOGGER.info("en-"+heads[0]+":\t"+currentEN);
						LOGGER.info("en-"+heads[ii]+":\t"+temp);
					}
				}
				currentline =  currentline+Constants.TAB + tuples.get(heads[ii]).get(jj);
			}
			counter++;
			currentline = currentEN+currentline;
			out.write(currentline.trim()+"\n");
			currentline="";
			currentEN="";
		}
		out.close();
		LOGGER.info("Total multiples IDs = "+ (ids.size()-1));
		LOGGER.info("Total multiples segments = "+ counter);
		return f2;
	}

/*	private static File getMultilinguality_(File f1) throws IOException {
		List<String> ids = FileUtils.readLines(f1, Constants.UTF8);
		String[] heads = ids.get(0).split(Constants.TAB);
		File[] datasets = new File[heads.length];
		String temp = "";
		String basePath = f1.getParent();
		for (int ii=0;ii<heads.length;ii++){
			temp = FilenameUtils.concat(basePath, "en-"+heads[ii]+".bicleaner07.lang");
			datasets[ii] = new File(temp);
		}
		File f2 = new File(f1.getAbsolutePath()+".corpus");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String headline = "";
		for (String head:heads){
			headline = headline+Constants.TAB+head;
		}
		out.write(headline.trim()+"\n");

		String corpusline="", currentEn="";
		for (int ii=1;ii<ids.size();ii++){
			String[] lids = ids.get(ii).split(Constants.TAB);
			temp="";
			corpusline="";
			for (int jj=0;jj<lids.length;jj++){
				temp = getLine(datasets[jj], Integer.parseInt(lids[jj]));
				System.out.println(datasets[jj].getName()+ Constants.TAB+lids[jj]+ Constants.TAB+temp);
				String[] temp1 =temp.split(Constants.TAB);
				if (corpusline.isEmpty()){
					corpusline = temp1[0]+ Constants.TAB+temp1[1];
					currentEn = temp1[0];
				}else{
					if (currentEn.equals(temp1[0]))
						corpusline = corpusline+Constants.TAB+temp1[1];
					else{
						LOGGER.info("ERROR");
						corpusline = corpusline+Constants.TAB+temp1[1];
					}
				}
			}
			System.out.println(corpusline);
			out.write(corpusline+"\n");
		}
		out.close();
		return f2;
	}*/


	/*private static String getLine(File file, int lineid) throws IOException {
		// TODO Auto-generated method stub
		String result = "";
		BufferedReader in = new BufferedReader(new FileReader(file));
		String inputLine;
		int counter = 0;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			if (counter==lineid){ 
				result = inputLine;
				break;
			}
		}
		in.close();
		return result;
	}
*/

	private static File resultMultilinguality(File f1, String[] langs) throws IOException{
		Arrays.sort(langs);
		String foundpair = "";
		for (String lang:langs){
			foundpair = foundpair +lang+Constants.HYPHEN;
		}

		File f2 = new File(f1.getAbsolutePath()+"." + foundpair.substring(0, foundpair.length()-1));
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine;
		int counter = 0;
		List<String> headlangs = new ArrayList<String>();

		List<String> targetlangs = Arrays.asList(langs);
		while ((inputLine = in.readLine()) != null) {
			if (counter==0){ 
				headlangs = Arrays.asList(inputLine.split(Constants.TAB));
				break;
			}
		}
		in.close();
		List<Integer> cols = new ArrayList<Integer>();
		for (int ii=0;ii<targetlangs.size();ii++){
			for (int jj=0;jj<headlangs.size();jj++){
				if (targetlangs.get(ii).equals(headlangs.get(jj))){
					cols.add(jj);
					break;
				}
			}
		}
		in = new BufferedReader(new FileReader(f1));
		foundpair="";
		boolean found = true;
		int thr = cols.get(cols.size()-1)+1;
		counter = 0;
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length<thr)
				continue;
			foundpair = "";
			found = true;
			for (int ii=0;ii<cols.size();ii++){
				if (parts[cols.get(ii)].isEmpty()){
					found = false;
					break;
				}else{
					foundpair = foundpair +parts[cols.get(ii)]+Constants.TAB;
				}
			}
			if (found){
				out.write(foundpair.trim()+"\n");
				counter++;
			}
		}
		in.close();
		out.close();
		LOGGER.info("# TUs found:\t"+ (counter-1));
		return f2;
	}

	private static File keepMultilinguality(File f1)  throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".multi");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, key="", previouskey="";
		List<String> list = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length!=4){
				LOGGER.warn("CUT\t" +inputLine);
				continue;
			}
			key = parts[0];
			if (!key.equals(previouskey)){
				if (list.size()>1){
					for (int ii=0;ii<list.size();ii++){
						out.write(list.get(ii)+"\n");
					}
				}
				list.clear();
				list.add(key+"\t"+parts[2]+"\t"+parts[3]);
				previouskey=key;
			}else{
				list.add(key+"\t"+parts[2]+"\t"+parts[3]);
			}
		}
		in.close();
		out.close();
		return f2;
	}

	private static File processMultilinguality(File f1) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".idmulti");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line="";
		LOGGER.info("reading file");
		Map<Integer,LangObject> obj= new HashMap<Integer,LangObject>();
		String key="", curlang="", previouskey="";
		LangObject templangobj = initLangObject();
		int idkey=0;
		int counter = 0,  thous = 1, level=100000, thr = level;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			if (counter==thr){
				thous++;
				thr = level*thous;
				LOGGER.info(counter);
			}		
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length!=3){
				LOGGER.warn("CUT\t" +inputLine);
				continue;
			}
			key = parts[0];
			if (!key.equals(previouskey)){
				idkey++;
				previouskey=key;
			}
			if (obj.containsKey(idkey))
				templangobj = obj.get(idkey);
			else
				templangobj = initLangObject();
			curlang = parts[1];
			line = parts[2];
			Field languageField =  null;
			try {
				languageField = LangObject.class.getDeclaredField(curlang);
				//boolean accessible = languageField.isAccessible();
				//templangobj.el = parts[3];
				languageField.set(templangobj, line);
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//templangobj = update(templangobj, curlang, parts[3]);
			obj.put(idkey, templangobj);
		}
		in.close();
		Set<Integer> keys = obj.keySet();
		LOGGER.info("Number of multilingual TUs:\t"+keys.size());
		Iterator<Integer> keys_it = keys.iterator();
		thous=0;
		int key_temp;
		LOGGER.info("writing file");
		out.write(header+"\n");
		boolean multi=false;
		while (keys_it.hasNext()){									
			counter++;
			if (counter/100000>thous){
				thous++;
				LOGGER.info((thous*100000)+ " files have been examined");
			}
			key_temp = keys_it.next();
			templangobj = obj.get(key_temp);
			multi = checkmutli(templangobj);
			if (multi)
				writeline(out, key_temp, templangobj);
			//writeline(out, key_temp, templangobj);
		}
		out.close();
		return f2;
	}

	private static File denormalize(File f1, File f2, File f3) throws IOException {
		File f4 = new File(f1.getAbsolutePath()+"_1");
		List<String> comm = FileUtils.readLines(f1, Constants.UTF8);
		BufferedReader in = new BufferedReader(new FileReader(f2));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line="";
		int counter = 0;
		boolean found = false;
		while ((inputLine = in.readLine()) != null) {
			found = false;
			String[] parts = inputLine.split(Constants.TAB);
			String temp = ContentNormalizer.normtext(parts[0]);
			for (int jj=0;jj<comm.size();jj++){
				String[] t =comm.get(jj).split(Constants.TAB); 
				if (t[0].equals(temp)){
					line = parts[0] + Constants.TAB+t[1] + Constants.TAB+t[2];
					found = true;
					break;
				}	
			}
			if (found)
				out.write(line.trim()+"\n");

		}
		System.out.println(counter);
		in.close();
		out.close();

		return f4;
	}

	private static File addlang(File f1, String lang) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".lang");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line;
		int counter = 0;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			//LOGGER.info(inputLine);
			line = inputLine + Constants.TAB+ lang;
			out.write(line.trim()+"\n");
		}
		System.out.println(counter);
		in.close();
		out.close();
		return f2;
	}

	private static File normalizeContent(File f1) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".norm");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line;
		int counter = 0;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			//LOGGER.info(inputLine);
			String[] parts = inputLine.split(Constants.TAB);
			parts[0] = ContentNormalizer.normtext(parts[0]);

			line = "";
			for (int ii=0;ii<parts.length;ii++){
				line = line + Constants.TAB + parts[ii];
			}
			line = line.trim() + Constants.TAB + counter;
			out.write(line.trim()+"\n");
		}
		LOGGER.info(counter + " TUs");
		in.close();
		out.close();
		return f2;
	}

	private static File findCommInSorted(File f1, int range1) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".comm");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine;
		boolean found = false;
		String preen = "", prexx = "", prela="", text = "";
		int  tus = 0;
		String en, xx, la;
		while ((inputLine = in.readLine()) != null) {
			found = false;
			String[] parts = inputLine.split(Constants.TAB);
			en = parts[0];
			xx = parts[1];
			la = parts[2];
			if (!en.equals(preen)){
				preen = en;
				prexx = xx;
				prela = la;
			}else{
				if (prela.equals(la)){
					preen = en;
					prexx = xx;
					prela = la;
				}else{
					found = true;
				}
			}
			if (found){
				text = en+Constants.TAB;
				if (prela.compareTo(la) < 0)
					text = text + prexx +Constants.TAB+ xx;
				else
					text = text + xx +Constants.TAB+ prexx;
				tus++;
				out.write(text.trim()+"\n");
			}
		}
		LOGGER.info(tus + "common TUs");
		in.close();
		out.close();
		return f2;
	}

	public static class LangObject {
		public String bg, cs, da, de, el, et, es, fi, fr, ga;
		public String hr, hu, it, is, lt, lv, mt, nl, no, pl, pt;
		public String ro, sk, sl, sv;

		public LangObject(String bg, String cs, String da, String de, String el,
				String et, String es, String fi, String fr, String ga, String hr,
				String hu, String it, String is, String lt, String lv, String mt,
				String nl, String no, String pl, String pt, String ro, String sk,
				String sl, String sv) {
			super();
			this.bg = bg;
			this.cs = cs;
			this.da = da;
			this.de = de;
			this.el = el;
			this.et = et;
			this.es = es;
			this.fi = fi;
			this.fr = fr;
			this.ga = ga;
			this.hr = hr;
			this.hu = hu;
			this.it = it;
			this.is = is;
			this.lt = lt;
			this.lv = lv;
			this.mt = mt;
			this.nl = nl;
			this.no = no;
			this.pl = pl;
			this.pt = pt;
			this.ro = ro;
			this.sk = sk;
			this.sl = sl;
			this.sv = sv;
		}
	}
	public static LangObject initLangObject(){
		LangObject docdata = new LangObject("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" );
		return docdata;
	}

	private static void writeline(BufferedWriter out, int key_temp, 	LangObject templangobj) throws IOException {

		out.write( templangobj.bg+"\t"+templangobj.cs+"\t"+templangobj.da+"\t"+templangobj.de+"\t"+templangobj.el+"\t"+templangobj.es+"\t"
				+templangobj.et+"\t"+templangobj.fi+"\t"+templangobj.fr+"\t"+templangobj.ga+"\t"+templangobj.hr+"\t"+templangobj.hu+"\t"+templangobj.is+"\t"
				+templangobj.it+"\t"+templangobj.lt+"\t"+templangobj.lv+"\t"+templangobj.mt+"\t"+templangobj.nl+"\t"+templangobj.no+"\t"+templangobj.pl+"\t"
				+templangobj.pt+"\t"+templangobj.ro+"\t"+templangobj.sk+"\t"+templangobj.sl+"\t"+templangobj.sv+"\n");

		//	out.write(key_temp+"\t"+ templangobj.bg+"\t"+templangobj.cs+"\t"+templangobj.da+"\t"+templangobj.de+"\t"+templangobj.el+"\t"+templangobj.es+"\t"
		//			+templangobj.et+"\t"+templangobj.fi+"\t"+templangobj.fr+"\t"+templangobj.ga+"\t"+templangobj.hr+"\t"+templangobj.hu+"\t"+templangobj.is+"\t"
		//			+templangobj.it+"\t"+templangobj.lt+"\t"+templangobj.lv+"\t"+templangobj.mt+"\t"+templangobj.nl+"\t"+templangobj.no+"\t"+templangobj.pl+"\t"
		//			+templangobj.pt+"\t"+templangobj.ro+"\t"+templangobj.sk+"\t"+templangobj.sl+"\t"+templangobj.sv+"\n");
	}

	private static boolean checkmutli(LangObject templangobj) {

		int counter = 0;
		if (!templangobj.bg.isEmpty())
			counter++;
		if (!templangobj.cs.isEmpty())
			counter++;
		if (!templangobj.da.isEmpty())
			counter++;
		if (!templangobj.de.isEmpty())
			counter++;
		if (!templangobj.el.isEmpty())
			counter++;
		if (!templangobj.es.isEmpty())
			counter++;
		if (!templangobj.et.isEmpty())
			counter++;
		if (!templangobj.fi.isEmpty())
			counter++;
		if (!templangobj.fr.isEmpty())
			counter++;
		if (!templangobj.ga.isEmpty())
			counter++;
		if (!templangobj.hr.isEmpty())
			counter++;
		if (!templangobj.hu.isEmpty())
			counter++;
		if (!templangobj.is.isEmpty())
			counter++;
		if (!templangobj.it.isEmpty())
			counter++;
		if (!templangobj.lt.isEmpty())
			counter++;
		if (!templangobj.lv.isEmpty())
			counter++;
		if (!templangobj.mt.isEmpty())
			counter++;
		if (!templangobj.nl.isEmpty())
			counter++;
		if (!templangobj.no.isEmpty())
			counter++;
		if (!templangobj.pl.isEmpty())
			counter++;
		if (!templangobj.pt.isEmpty())
			counter++;
		if (!templangobj.ro.isEmpty())
			counter++;
		if (!templangobj.sk.isEmpty())
			counter++;
		if (!templangobj.sl.isEmpty())
			counter++;
		if (!templangobj.sv.isEmpty())
			counter++;
		if (counter==0)
			System.out.println("problem");
		if (counter>1)
			return true;

		return false;
	}


	private static void filterparacrawlTest(File f1) throws IOException {
		File filteredfile = new File(f1.getAbsolutePath()+".clean");
		
		FileOutputStream fos = new FileOutputStream(filteredfile);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		String[] langs = f1.getName().split("\\.")[0].split(Constants.HYPHEN);
		
		LangDetector langDetector = LangDetectUtils.loadLangDetectors(langs,"langdetect");
		
		List<String> noletters = new ArrayList<String>();
		List<String> cases = new ArrayList<String>();
		List<String> identicalTUVs = new ArrayList<String>();
		List<String> shortTUs = new ArrayList<String>();
		List<String> segs = new ArrayList<String>();
		List<String> ratios = new ArrayList<String>();
		List<String> addr = new ArrayList<String>();
		List<String> dupTUs = new ArrayList<String>();
		List<String> wronglangs = new ArrayList<String>();
		
		int counter = 0, counterTUs=0;
		int counternoletters=0, counteriden=0, counterdup=0, counteremail = 0, counterratio=0, countershort = 0, countercase = 0, counterlang=0;
			
		BufferedReader in = new BufferedReader(new FileReader(f1));
		String inputLine, lang;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			//LOGGER.info(inputLine);
			String[] parts = inputLine.split(Constants.TAB);
			String ensen = parts[0].trim();		String normS = ContentNormalizer.normtext(ensen); 
			String tasen = parts[1].trim();		String normT = ContentNormalizer.normtext(tasen);
			if ( normS.isEmpty() || normT.isEmpty()){
				LOGGER.info("CUT noletters:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				noletters.add("CUT noletters:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				counternoletters++;
				continue;
			}
			if (normS.equals(normT)){
				LOGGER.info("CUT identical TUs:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				identicalTUVs.add("CUT identical TUs:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				counteriden++;
				continue;
			}
			if (TMXHandlerUtils.checkemail(ensen, 0.5) || TMXHandlerUtils.checkemail(tasen, 0.5) || TMXHandlerUtils.checkurl(ensen, 0.5) || TMXHandlerUtils.checkurl(tasen, 0.5)){
				LOGGER.info("CUT email,url:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				addr.add("CUT email,url:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				counteremail++;
				continue;	
			}
			String temp = normS+Constants.TAB+normT;
			boolean dup=false;
			for (int jj=0;jj<segs.size();jj++){
				String segtemp = segs.get(jj);
				if (segtemp.equals(temp)){
					dupTUs.add("CUT dup:(id="+counter+")\t"+temp +"\t"+ ensen+ "\t"+tasen);
					dup = true;
				}
				if (dup)
					break;
			}
			if (dup){
				counterdup++;
				continue;
			}
			if (FCStringUtils.countTokens(normS)<3 || FCStringUtils.countTokens(normT)<3){
				LOGGER.info("CUT very short TUs:"+counter+"\t"+ ensen+ "\t"+tasen);
				shortTUs.add("CUT very short TUs:"+ counter+"\t"+ ensen+ "\t"+tasen);
				countershort++;
				continue;
			}

			float ratio = (float)ensen.length()/(float)tasen.length();
			if (ratio>2.0 || ratio < 0.5){
				LOGGER.info("CUT ratio:(id="+counter+")\t"+ ensen+ "\t"+tasen+"\t"+ ratio);
				ratios.add("CUT ratio:(id="+counter+")\t"+ ensen+ "\t"+tasen+"\t"+ ratio);
				counterratio++;
				continue;
			}

			if (FCStringUtils.isAllUpperCase(ensen) * FCStringUtils.isAllUpperCase(tasen)<0){
				LOGGER.info("CUT case:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				cases.add("CUT case:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				countercase++;
				continue;
			}
			lang = ISOLangCodes.get2LetterCode(langDetector.detect(ensen));
			if (!lang.equals(langs[0])){
				LOGGER.info("CUT lang1:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				wronglangs.add("CUT lang1:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				counterlang++;
				continue;
			}
			lang = ISOLangCodes.get2LetterCode(langDetector.detect(tasen));
			if (!lang.equals(langs[1])){
				LOGGER.info("CUT lang2:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				wronglangs.add("CUT lang2:(id="+counter+")\t"+ ensen+ "\t"+tasen);
				counterlang++;
				continue;
			}
			segs.add(temp);
			counterTUs++;			
			bw.write(ensen+"\t"+tasen+"\n");
		}
		LOGGER.info(counterTUs + " TUs");
		in.close();
		bw.close();
		LOGGER.info("remain:\t"+counterTUs);
		LOGGER.info("counternoletters:\t" + counternoletters);
		LOGGER.info("counteriden:\t" + counteriden);
		LOGGER.info("counteremail:\t" + counteremail);
		LOGGER.info("counterdup:\t" + counterdup);
		LOGGER.info("countershort:\t" + countershort);
		LOGGER.info("counterratio:\t" + counterratio);
		LOGGER.info("countercase:\t" + countercase);
		LOGGER.info("wronglang2:\t" + counterlang);
		
		File nolettersfile = new File(f1.getAbsolutePath()+".noletters");
		FileUtils.writeLines(nolettersfile, noletters);

		File identicalTUVsfile = new File(f1.getAbsolutePath()+".identicalTUVs");
		FileUtils.writeLines(identicalTUVsfile, identicalTUVs);

		File ratiosfile = new File(f1.getAbsolutePath()+".ratioTUVs");
		FileUtils.writeLines(ratiosfile, ratios);

		File shortTUsfile = new File(f1.getAbsolutePath()+".shortTUVs");
		FileUtils.writeLines(shortTUsfile, shortTUs);

		File addrsfile = new File(f1.getAbsolutePath()+".addrs");
		FileUtils.writeLines(addrsfile, addr);

		File casesfile = new File(f1.getAbsolutePath()+".cases");
		FileUtils.writeLines(casesfile, cases);

		File dupTUsfile = new File(f1.getAbsolutePath()+".dupTUs");
		FileUtils.writeLines(dupTUsfile, dupTUs);
		
		File wronglangsfile = new File(f1.getAbsolutePath()+".wronglang");
		FileUtils.writeLines(wronglangsfile, wronglangs);
	}
}
