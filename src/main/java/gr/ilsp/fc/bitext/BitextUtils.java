package gr.ilsp.fc.bitext;

import gr.ilsp.fc.bitext.Bitexts.DocVector;
//import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class BitextUtils {
	private static final Logger LOGGER = Logger.getLogger(BitextUtils.class);
	private static int minnumofpars=3;
	private static final String SLASH = "/";
	private static String input_type="xml";
	
	
	/**
	 * Reads specific to the project resources with the SVM parameters for identification of pairs based on structure.
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static double[][] readSVMparams(String filename) throws IOException {
		ArrayList<double[]> tempparam=new ArrayList<double[]>();
		URL svURL = ReadResources.class.getClassLoader().getResource(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			String[] temp=inputLine.split("\t");
			double[] tempd=new double[temp.length];
			for (int j=0;j<temp.length;j++){
				tempd[j]=Double.parseDouble(temp[j]);  
			}
			if (temp.length>1){
				tempparam.add(new double[] {tempd[0], tempd[1],tempd[2]});
			}else{
				tempparam.add(new double[] {tempd[0]});
			}
		}
		in.close();
		int x= tempparam.size();
		int y = tempparam.get(0).length;
		double[][] param=new double[x][y];
		for (int j=0;j<x;j++){
			for (int k=0;k<y;k++)
				param[j][k]=tempparam.get(j)[k];
		}
		return param;
	}

	/**
	 * Reads a text file with the fingerprint 
	 * @param fn
	 * @return
	 */
	public static int[] readFingerprint(String fn) {
		File f=new File(fn);
		String str=null;
		ArrayList<String> patterns1 = new ArrayList<String>();
		int kk=0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			while ((str = in.readLine()) != null) {
				patterns1.add(str);
				kk++;
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Problem in reading file: " + f.getName());
		}
		String[] patterns = new String[kk];
		System.arraycopy(patterns1.toArray(), 0, patterns, 0, kk);
		int[] patt_int = new int[kk];

		int num_of_text_par=0;
		for (int i=0; i<kk; i++){
			patt_int[i]=Integer.parseInt(patterns[i]);
			if (Integer.parseInt(patterns[i])>0)
				num_of_text_par++;
		}
		if (num_of_text_par>minnumofpars)
			return patt_int;
		else
			return null;
	}

	/**
	 *  Gets the ArrayList of bitexts and returns a new list sorted by the total length in tokens of each bitext
	 * @param bitexts
	 * @return
	 */
	public static ArrayList<String[]> sortbyLength(ArrayList<String[]> bitexts) {
		ArrayList<String[]> new_bitexts=new ArrayList<String[]>();
		double[] new_temp1 = new double[bitexts.size()];

		for (int i = 0; i < new_temp1.length; i++){
			new_temp1[i] =Double.parseDouble(bitexts.get(i)[5]);
		}
		Arrays.sort(new_temp1);
		int kk = 0;
		for (int i = 0; i < new_temp1.length; i++){
			if (i > 0 && new_temp1[i]==new_temp1[i -1])
				continue;
			new_temp1[kk++] = new_temp1[i];
		}
		double[] new_temp = new double[kk];
		System.arraycopy(new_temp1, 0, new_temp, 0, kk);

		for (int i=0; i<new_temp.length; i++){
			for (int jj=0;jj<bitexts.size();jj++){
				if (Double.parseDouble(bitexts.get(jj)[5])==new_temp[i]){
					new_bitexts.add(new String[] {bitexts.get(jj)[0], bitexts.get(jj)[1],bitexts.get(jj)[2],bitexts.get(jj)[3],bitexts.get(jj)[4], bitexts.get(jj)[5]});
				}
			}
		}
		return new_bitexts;
	}

	/**
	 * Calculates the tokens in the paired documents, per language and per method 
	 * @param features
	 * @param bitexts
	 * @param method
	 * @return
	 */
	public static void calcToksperLang(HashMap<String, DocVector> features,	ArrayList<String[]> bitexts, String method) {
		String[] stats=new String[4];
		if (!bitexts.isEmpty()){
			if (method.isEmpty()){ //i.e. all types
				stats[0]=bitexts.get(0)[2];				stats[1]=Integer.toString(0);
				stats[2]=bitexts.get(0)[3];				stats[3]=Integer.toString(0);
				String p1, p2, lang1, lang2;
				for (int ii=0;ii<bitexts.size();ii++){
					p1 = bitexts.get(ii)[0];		lang1 = bitexts.get(ii)[2];
					p2 = bitexts.get(ii)[1];		lang2 = bitexts.get(ii)[3];
					if (lang1.equals(stats[0]))
						stats[1]=Integer.toString(Integer.parseInt(stats[1])+(int)features.get(p1).numToksnoOOI);
					if (lang1.equals(stats[2]))
						stats[3]=Integer.toString(Integer.parseInt(stats[3])+(int)features.get(p1).numToksnoOOI); 
					if (lang2.equals(stats[0]))
						stats[1]=Integer.toString(Integer.parseInt(stats[1])+(int)features.get(p2).numToksnoOOI); 
					if (lang2.equals(stats[2]))
						stats[3]=Integer.toString(Integer.parseInt(stats[3])+(int)features.get(p2).numToksnoOOI);
				}
			}else{
				int jj=0;
				//run up to the first of such type, just to get the langs 
				for (jj=0;jj<bitexts.size();jj++){
					if (bitexts.get(jj)[4].equals(method)){
						stats[0]=bitexts.get(jj)[2];		stats[1]=Integer.toString(0);
						stats[2]=bitexts.get(jj)[3];		stats[3]=Integer.toString(0);
						break;
					}
				}//continue for the rest
				for (int ii=jj;ii<bitexts.size();ii++){
					if (bitexts.get(ii)[4].equals(method)){
						String p1 = bitexts.get(ii)[0];		String lang1=features.get(p1).codeLang;
						String p2 = bitexts.get(ii)[1];		String lang2=features.get(p2).codeLang;
						if (lang1.equals(stats[0]))
							stats[1]=Integer.toString(Integer.parseInt(stats[1])+(int)features.get(p1).numToksnoOOI);
						if (lang1.equals(stats[2]))
							stats[3]=Integer.toString(Integer.parseInt(stats[3])+(int)features.get(p1).numToksnoOOI); 
						if (lang2.equals(stats[0]))
							stats[1]=Integer.toString(Integer.parseInt(stats[1])+(int)features.get(p2).numToksnoOOI); 
						if (lang2.equals(stats[2]))
							stats[3]=Integer.toString(Integer.parseInt(stats[3])+(int)features.get(p2).numToksnoOOI);
					}
				}
			}
		}
		LOGGER.info("Tokens in "+stats[0] +" : "+ stats[1]);
		LOGGER.info("Tokens in "+stats[2] +" : "+ stats[3]);
	}

	/**
	 * Gets a URL and returns its level in the web site 
	 * @param url
	 * @return
	 */
	public static int getURLlevel(String url) {
		int k=0, level=0, ind=0;
		while (k<url.length()){
			ind=url.indexOf(SLASH, k);
			if (ind>0){
				k = ind+1;
				level=level+1;
			}else
				k=url.length();
		}
		if (url.endsWith(SLASH))
			level=level-1;
		level=level-2; 
		return level;
	}

	/**
	 * Checks if the generated CesDoc (keys in DocVectors) concern documents in both targeted languages.  
	 * @param lang
	 * @param props
	 * @return
	 */
	public static int[] check_crawl_stats(String[] lang, HashMap<String, DocVector> features ) {
		if (features.size()<2){
			LOGGER.info("Less than 2 files found. Detection of pairs is stopped.");
			return null;
		}
		int[] langfiles = new int[lang.length];
		Set<String> files_keys=features.keySet();
		Iterator<String> files_it = files_keys.iterator();
		String key;
		while (files_it.hasNext()){
			key = files_it.next();
			DocVector file_props=features.get(key);
			for (int ii=0;ii<lang.length;ii++){
				if (file_props.codeLang.equals(lang[ii])){
					langfiles[ii]++;
					break;
				}
			}
		}
		for (int ii=0;ii<langfiles.length;ii++){
			if (langfiles[ii]==0){
				LOGGER.info("No file found in " + lang[ii]+"; Detection of pairs is stopped.");
				return null;
			}else{
				LOGGER.info(langfiles[ii] + " documents in "+lang[ii]);
			}
		}
		return langfiles;
	}

	/**
	 * returns list of Docs that participate in "birectional pairs" (based on hreflang) or all docs in pairs
	 * @param hreflangIDPairs
	 * @return
	 */
	public static Set<String> getDocsinPairs (Map<String, String> hreflangIDPairs) {
		Set<String> paired = new HashSet<String>();
		for (Map.Entry<String, String> entry : hreflangIDPairs.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String file1 = key.split(Constants.UNDERSCORE)[0];
			String file2 = value.split(Constants.UNDERSCORE)[0];
			//System.out.println(file1+" --> "+file2);
			if (paired.contains(file1)){
				LOGGER.debug("Skipping already seen file " + file1 +". The docs in its pairs are strong candidates for deduplication" );
				continue;
			}
			if (paired.contains(file2)) {	
				LOGGER.debug("Skipping already seen file " + file2 +". The docs in its pairs are strong candidates for deduplication" );
				continue;
			} 
			//if ((hreflangIDPairs.containsKey(value) &&  hreflangIDPairs.get(value).equals(key) )) {
			paired.add(file1+Constants.DOT+input_type);
			paired.add(file2+Constants.DOT+input_type);
			//}
		}
		return paired;
	}

	/**
	 * 
	 * <B>Use carefully.</B>
	 * 
	 * Deletes all redundant files of a bilingual crawl. All files whose id is not
	 * contained in one of the first two entries in a bitextStructure entry are
	 * deleted.
	 * 
	 * Usage:
	 * 
	 * <pre>
	 * {@code
	 * ArrayList<String[]> bitextStruct= new ArrayList<String[]>();
	 * bitextStruct.add(new String[] {"127", "75", "", "", ""});
	 * bitextStruct.add(new String[] {"128", "76", "", "", ""});
	 * ...
	 * removeRedundantFiles(new File("/data/dirWithCrawlFiles"), bitextStruct);
	 * </pre>
	 *
	 * @param parentDir
	 * @param bitextStructure
	 * @throws IOException
	 */
	public static void removeRedundantFiles(File parentDir, ArrayList<String[]> bitextStructure) {
		if (parentDir.isDirectory()) {			
			// First collect all ids we want to keep.
			List<String> ids=  new ArrayList<String>();
			for (String[] bitext : bitextStructure) {
				ids.add(bitext[0]); ids.add(bitext[1]);
			}			
			List<File> files = Arrays.asList(parentDir.listFiles());
			for (File file: files) {
				if (file.isDirectory() || file.getName().contains(Constants.UNDERSCORE)) { // Skip directories and cesAlignFiles
					continue;
				}
				String basename = FilenameUtils.getBaseName(file.getName()); // Get the basename
				while (!FilenameUtils.getBaseName(basename).equals(basename)) { // Get the real basename
					basename = FilenameUtils.getBaseName(basename);
				}

				if (!ids.contains(basename)) { // Finally delete all redundant files
					LOGGER.debug("Deleting redundant " +file);
					file.delete();					
				}
			}
		} else {
			LOGGER.warn(parentDir + " is not a directory.");
		}
	}

	/**
	 * Returns the pairing method with which the cesAlignFile detected 
	 * @param cesAlignFile
	 * @return
	 */
	public static String getDocumentAlignmentMethod (File cesAlignFile) {
		String documentAlignmentMethod = "null";
		String filename = FilenameUtils.getBaseName(cesAlignFile.getName());
		if (filename.matches(".*_\\p{L}")) {
			documentAlignmentMethod = filename.substring(filename.length() - 1); 
		}
		return documentAlignmentMethod;
	}

	public static HashMap<String, String> getTruePairs(String docpairsfile) {
		HashMap<String, String> res=new HashMap<String,String>();
		List<String> a;
		try {
			a = FileUtils.readLines(new File(docpairsfile),Constants.UTF8);
			for (String t:a){
				String[] l=t.split(Constants.SPACE);
				res.put(l[0], l[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return res;
	}

	public static Set<String> getDocsinPairsList(	List<String> idPairsFromTranslationLinksList) {
		
		Set<String> paired = new HashSet<String>();
		
		for (String pair:idPairsFromTranslationLinksList){
			String[] pairs = pair.split(Constants.TAB);
			paired.add(pairs[0]+Constants.DOT+input_type);
			paired.add(pairs[1]+Constants.DOT+input_type);
		}
		
		return paired;
	}
	
	/**
	 * identifies pairs based on common digits
	 * @param features holds filename as key and its features in DocVector as value
	 * @return
	 *//*
	//FIXME thresholds should be checked and selected more carefully (if we keep using thresholds)
	public static ArrayList<String[]> findpairsDig(HashMap<String,DocVector> features, String[] langs) {
		LOGGER.info("Examining pages based on common digits");
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		Set<String> paired=new HashSet<String>();
		String  key, key1, key2, digits1, digits2;//, lang1, lang2;
		String ind="";//, det_lang="";
		int level1, level2, digits1length, digits2length, counter = 0, thous=0;
		double mindist, p1, p2, tok1, tok2, disttok, dist;
		
		HashMap<String, DocVector> features1 = new HashMap<String, DocVector>();
		HashMap<String, DocVector> features2 = new HashMap<String, DocVector>();
		
		Set<String> files_keys = features.keySet();
		Iterator<String> files_it = files_keys.iterator();
		//Split features in two groups: langs[0] , langs[1] 
		while (files_it.hasNext()){
			key = files_it.next();
			if (features.get(key)==null)
				continue;
			DocVector t = features.get(key);
			if (features.get(key).codeLang.equals(langs[0]))
				features1.put(key, t);
			else{
				if (features.get(key).codeLang.equals(langs[1]))
					features2.put(key, t);
			}
		}
		Set<String> files_keys1 = features1.keySet();
		Set<String> files_keys2 = features2.keySet();
		Iterator<String> files_it1 = files_keys1.iterator();
		LOGGER.info(files_keys1.size()+" files in "+ langs[0]);
		LOGGER.info(files_keys2.size()+" files in "+ langs[1]);
		//for each file of group 1 checks all files of group 2
		//and keep the nearest if exists
		while (files_it1.hasNext()){								
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
			}
			key1 = files_it1.next();			
			if (paired.contains(key1))
				continue;
			if (features1.get(key1)==null){
				paired.add(key1); // even it is not paired, we do not need to examine it 
				continue;
			}
			digits1=features1.get(key1).digitList;
			if (StringUtils.isBlank(digits1)){
				paired.add(key1); // even it is not paired, we do not need to examine it 
				continue;			
			}
			//lang1=features1.get(key1).codeLang;
			level1=features1.get(key1).urlLevel;
			p1=features1.get(key1).numPars;
			tok1=features1.get(key1).numToksnoBoil;
			digits1length = digits1.length();
			mindist =max_dig_dif;
			Iterator<String> files_it2 = files_keys2.iterator();
			ind="";
			while (files_it2.hasNext()){
				key2 = files_it2.next();
				if (paired.contains(key2))
					continue;
				if (features2.get(key2)==null){
					paired.add(key2); // even it is not paired, we do not need to examine it 
					continue;
				}
				digits2=features2.get(key2).digitList;
				if (StringUtils.isBlank(digits2)){
					paired.add(key2); // even it is not paired, we do not need to examine it
					continue;	
				}
				//lang2=features2.get(key2).codeLang;
				level2=features2.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;
				p2=features2.get(key2).numPars;
				//dist=0.0;
				if (p1>p2) dist=p2/p1; else dist=p1/p2;
				if (dist<max_par_dif)
					continue;
				tok2=features2.get(key2).numToksnoBoil;
				//disttok=0.0;
				if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
				if (disttok <= max_tok_dif) 
					continue;
				digits2length=digits2.length();
				double temp = Statistics.editDist(digits1, digits2) / (double) Math.min(digits1length,digits2length);
				if (temp>0.2 && (digits2length<len_thr1 || digits1length<len_thr1) 
						&& (digits2length>len_thr || digits1length>len_thr))
					continue;
				if (temp>0.1 && (digits2length<len_thr || digits1length<len_thr) 
						&& (digits2length>=len_thr2 || digits1length>=len_thr2))
					continue;
				if (digits2length<len_thr2 && digits1length<len_thr2 && temp>0) 
					continue;
				//if (digits2.length()<len_thr3 && digits1.length()<len_thr3 && tok1<toknum_thr && tok2<toknum_thr ) 
				//	continue;
				if (temp<mindist){
					mindist=temp;
					//mindisttok=disttok;
					ind = key2;
					//det_lang = lang2;
				}else{
					if (temp==mindist & !ind.isEmpty()){
						if (disttok>mindisttok){
									ind = key2;
									det_lang = lang2;
									mindisttok=disttok;
								}else{
									if (disttok==mindisttok){
						ind="";
						break;
						//}
						//}
					}
				}
			}
			if (ind.isEmpty())	
				continue;
			else{
				LOGGER.info("Matched:\t"+ key1 +"\t"+ind);
				if (key1.compareTo(ind)<0){
					pairs.add(new String[] {key1,ind,langs[0],langs[1],dig_pair_method, Double.toString(features1.get(key1).numToksnoOOI+features2.get(ind).numToksnoOOI)});
				}else{
					pairs.add(new String[] {ind,key1,langs[1], langs[0],dig_pair_method, Double.toString(features1.get(key1).numToksnoOOI+features2.get(ind).numToksnoOOI)});
				}
				paired.add(key1);
				paired.add(ind); 
			}
		}
		return pairs;
	}*/
	
	

/*
	*//**
	 * Detects pairs of docs based on common filenames of images and digits. It also checks # of pars, #of toks, urlslevel 
	 * @param imagesInHTML : key is the filename and value is an array with imagenames
	 * @param features holds filename as key and its features in DocVector as value
	 * @param targetedlang1 
	 * @return
	 * @throws IOException 
	 *//*
	public static ArrayList<String[]> findpairsIMDI__(HashMap<String, String[]> imagesInHTML,HashMap<String, DocVector> features,String[] langs)  {
		LOGGER.info("Examining pages for pairing based on common images and digits");
		ArrayList<String[]> pairsIM=new ArrayList<String[]>();
		HashMap<String,Double> temppairs=new HashMap<String,Double>();
		//Set<String> files=imagesInHTML.keySet();
		String key, key1, key2, digits1, digits2, temp_pair="";
		Set<String> paired = new HashSet<String>();
		int counter=0, thous=0, level1, level2;
		double cp1, cp2, distcleanpar,  p1, p2, tok1, tok2, disttok, dist;

		HashMap<String, DocVector> features1 = new HashMap<String, DocVector>();
		HashMap<String, DocVector> features2 = new HashMap<String, DocVector>();

		Set<String> files_keys = features.keySet();
		Iterator<String> files_it = files_keys.iterator();
		//Split features in two groups: langs[0] , langs[1] 
		while (files_it.hasNext()){
			key = files_it.next();
			if (features.get(key)==null)
				continue;
			DocVector t = features.get(key);
			if (features.get(key).codeLang.equals(langs[0]))
				features1.put(key, t);
			else{
				if (features.get(key).codeLang.equals(langs[1]))
					features2.put(key, t);
			}
		}
		Set<String> files_keys1 = features1.keySet();
		Set<String> files_keys2 = features2.keySet();
		LOGGER.info(files_keys1.size()+" files in "+ langs[0]);
		LOGGER.info(files_keys2.size()+" files in "+ langs[1]);
		//for each file of group 1 checks all files of group 2
		//and keep the nearest if exists

		Iterator<String> files_it1 = files_keys1.iterator();
		while (files_it1.hasNext()){									
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
			}
			key1 = files_it1.next();
			if (paired.contains(key1)) 
				continue;
			if (features1.get(key1)==null){
				paired.add(key1); // even it is not paired, we do not need to examine it
				continue;
			}
			if (imagesInHTML.get(key1)==null || imagesInHTML.get(key1).length==0){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			if (imagesInHTML.get(key1).length==0){
			paired.add(key1);//even it is not paired, we do not need to examine it 
			continue;
		}

			digits1=features1.get(key1).digitList;
			Set<String> mySet1 = new HashSet<String>();
			Collections.addAll(mySet1, imagesInHTML.get(key1));
			if (mySet1.isEmpty()){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			//lang1  = features.get(key1).codeLang;
			level1=features1.get(key1).urlLevel;
			p1=features1.get(key1).numPars;
			cp1 = features1.get(key1).numCleanPars;
			tok1= (double)features1.get(key1).numToksnoBoil;
			temp_pair = "";
			double temp_jacdist=0.0;
			//String temp_lang=null;
			Iterator<String> files_it2 = files_keys2.iterator();
			while (files_it2.hasNext()){
				key2 = files_it2.next();
				if (paired.contains(key2))
					continue;
				if (features.get(key2)==null){
					paired.add(key2);//even it is not paired, we do not need to examine it 
					continue;
				}
				level2=features2.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;
				if (imagesInHTML.get(key2)==null){
					paired.add(key2);//even it is not paired, we do not need to examine it 
					continue;
				}
				if (imagesInHTML.get(key2).length==0){
					paired.add(key2);//even it is not paired, we do not need to examine it 
					continue;
				}
				digits2=features2.get(key2).digitList;
				Set<String> mySet2 = new HashSet<String>();
				Collections.addAll(mySet2, imagesInHTML.get(key2));
				if (mySet2.isEmpty()){
					paired.add(key2);//even it is not paired, we do not need to examine it 
					continue;
				}
				Set intersection = new HashSet(mySet1);
				intersection.retainAll(mySet2);
				double t1 = Double.parseDouble(Integer.toString(intersection.size()));
				double t2 = Double.parseDouble(Integer.toString((mySet1.size()+mySet2.size()-intersection.size())));
				double jac = t1/t2;
				if (jac<jac_thr)
					continue;
				p2 = features2.get(key2).numPars;
				cp2 = features2.get(key2).numCleanPars;
				tok2 = (double)features2.get(key2).numToksnoBoil;
				dist=0.0;
				distcleanpar=0.0;
				disttok=0.0;
				if (p1>p2) dist=p2/p1; else dist=p1/p2;
				if (cp1>cp2) distcleanpar=cp2/cp1; else distcleanpar=cp1/cp2;
				if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
				if (disttok <= toks_thr)
					continue;
				double digitdist = Statistics.editDist(digits1, digits2) / (double) Math.min(digits1.length(),digits2.length());
				if (digitdist > digitdist_thr)
					continue;
				if ( dist>=pars_thr || distcleanpar>=pars_thr){ 
					double t=jac*dist*disttok*(1-digitdist); 
					if (t>temp_jacdist){
						temp_pair=key2;
						temp_jacdist=t;
						LOGGER.info("Matched:\t"+key1+"\t"+temp_pair);
					}else{
						if (t==temp_jacdist & !temp_pair.isEmpty()){
							temp_pair="";
							break;
						}
					}
					//temp_lang=lang2;
				}
			}
			if (!temp_pair.isEmpty()){
				String ta= key1 +"&"+temp_pair;
				//String ta1= temp_pair+"&"+key1 +"&"+temp_lang+"&"+lang1;
				if (temppairs.containsKey(ta)){
					if (temppairs.get(ta)==temp_jacdist){
						//if (key1.compareTo(temp_pair)<0){
						//	pairsIM.add(new String[] {key1,temp_pair,langs[0], langs[1], imdi_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
						//}
						//if (key1.compareTo(temp_pair)>0){
						//	pairsIM.add(new String[] {temp_pair, key1, langs[1], langs[0], imdi_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
						//}
						pairsIM.add(new String[] {key1,temp_pair,langs[0], langs[1], imdi_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
						paired.add(key1);
						paired.add(temp_pair);
						LOGGER.info("Selected:\t"+key1+"\t"+temp_pair);
						continue;
					}
				}else{
					temppairs.put(ta, temp_jacdist);
					pairsIM.add(new String[] {key1,temp_pair,langs[0], langs[1], imdi_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
					paired.add(key1);
					paired.add(temp_pair);
					LOGGER.info("Selected:\t"+key1+"\t"+temp_pair);
				}
			}
		}
		return pairsIM;
	}*/

/*	//fixme: Do the following and the next methods provide the same results?
	public static ArrayList<String[]> findpairsXML_SVM_NEW__(File xmldir,HashMap<String, DocVector> features, String[] langs) {
		LOGGER.info("Examining pages based on structure");
		double[][] sv=null, b = null, w=null;
		try {
			sv = BitextUtils.readSVMparams(SupportVectorsFile);
			b = BitextUtils.readSVMparams(ConstFile);
			w = BitextUtils.readSVMparams(WeightsFile);
		} catch (IOException e) {
			LOGGER.error("Problem in reading parameters of  SVM"); 
			e.printStackTrace();
		}
		ArrayList<String[]> pairs = new ArrayList<String[]>();

		HashMap<String, DocVector> features1 = new HashMap<String, DocVector>();
		HashMap<String, DocVector> features2 = new HashMap<String, DocVector>();

		Set<String> files_keys = features.keySet();
		Iterator<String> files_it1 = files_keys.iterator();

		String key1, key2, lang1, lang2;
		int level1, level2,  counter = 0, thous=0;
		//split features into two groups: lang1 and lang2
		while (files_it1.hasNext()){
			key1 = files_it1.next();
			if (features.get(key1)==null){
				continue;
			}
			DocVector t = features.get(key1);
			if (features.get(key1).codeLang.equals(langs[0]))
				features1.put(key1, t);
			else{
				if (features.get(key1).codeLang.equals(langs[1]))
					features2.put(key1, t);
			}
		}
		Set<String> files_keys1 = features1.keySet();
		Set<String> files_keys2 = features2.keySet();
		files_it1 = files_keys1.iterator();
		double dist, sl_length , p2, p1;
		Set<String> paired=new HashSet<String>();
		HashMap<String, Integer> multipairs = new HashMap<String, Integer>();
		while (files_it1.hasNext()){								
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
			}
			key1 = files_it1.next();
			lang1 = features1.get(key1).codeLang;
			level1 = features1.get(key1).urlLevel;
			int[] sl =BitextUtils.readFingerprint(FilenameUtils.concat(xmldir.getAbsolutePath(),key1+appXMLTXText));
			if (sl==null){
				paired.add(key1);
				continue;
			}
			int sflength =0;
			for (int mm=0;mm<sl.length;mm++){
				if (sl[mm]>0)
					sflength = sflength+sl[mm];
			}
			sl_length = Double.parseDouble(Integer.toString(sl.length));
			p1 = features1.get(key1).numPars; //Double.parseDouble(fileprops[2]);
			double res=0.0;
			paired.add(key1);
			Iterator<String> files_it2 = files_keys2.iterator();
			while (files_it2.hasNext()){
				key2 = files_it2.next();
				if (paired.contains(key2))
					continue;
				if (features2.get(key2)==null){
					paired.add(key2); // even it is not paired, we do not need to examine it 
					continue;
				}
				lang2 = features2.get(key2).codeLang;
				level2 = features2.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;		
				p2 = features2.get(key2).numPars; 
				if (Math.abs(p1-p2)/Math.max(p1, p2)>=pars_thres)
					continue;
				//if ((Math.abs(sl_par-tl_par)/Math.max(sl_par, tl_par))<pars_thres) {
				int[] tl =BitextUtils.readFingerprint(FilenameUtils.concat(xmldir.getAbsolutePath(),key2+appXMLTXText));
				if (tl==null){
					paired.add(key2);
					continue;
				}
				double tl_length = Double.parseDouble(Integer.toString(tl.length));
				if (Math.abs(sl_length-tl_length)/Math.min(sl_length,tl_length)<=length_thres
						|| (Math.abs(sl_length-tl_length)<10)){
					dist= Double.parseDouble(Integer.toString(Statistics.editDist(sl,tl)));
					double f1=0.0, f2=0.0, f3=0.0;
					if (tl_length>=sl_length){
						f1 = sl_length/tl_length;
						f3=dist/tl_length;
					}else{
						f1 = tl_length/sl_length;
						f3=dist/tl_length;
					}
					if (p2>=p1){
						f2=p1/p2;
					}else{
						f2=p2/p1;
					}
					if (f3>=0.38 || f1<=0.7 || f2<=0.7)
						res=-1;
					else
						res=Statistics.SVM_test(f1,f2,f3,sv,w,b,19.0);
					if (res>0){
						LOGGER.info("Matched:\t"+ key1 + "\twith\t"+ key2);
						res=Math.abs(res);
						double inv_res=1/res;
						int tflength =0;
						for (int mm=0;mm<tl.length;mm++){
							if (tl[mm]>0)
								tflength = tflength+tl[mm];
						}
						String pairlength = Double.toString(features2.get(key2).numToksnoOOI+features1.get(key1).numToksnoOOI);
						int t1=0, t2=0;
						if (multipairs.containsKey(key1))
							t1 = multipairs.get(key1);
						t1=t1+1;
						if (t1>2)
							continue;
						multipairs.put(key1, t1);
						if (multipairs.containsKey(key2))
							t2 = multipairs.get(key2);
						t2=t2+1;
						if (t2>2)
							continue;
						multipairs.put(key2, t2);
						if (t1>2 || t2>2) //we do not care for docs which participate in more than 2 docs
							continue;
						if (key1.compareTo(key2)>0){
							pairs.add(new String[] {key2,key1,lang2,lang1,Double.toString(inv_res),pairlength});
						}else{
							pairs.add(new String[] {key1,key2,lang1,lang2,Double.toString(inv_res),pairlength});
						}
					}
				}
			}
		}
		return pairs;
	}*/


	/*	*//**
	 * Detects pairs of docs based on common filenames of images. It also checks # of pars, #of toks, urlslevel 
	 * @param imagesInHTML : key is the filename and value is an array with imagenames
	 * @param features holds filename as key and its features in DocVector as value
	 * @param targetedlang1 
	 * @return
	 *//*
	public static ArrayList<String[]> findpairsIM(HashMap<String, String[]> imagesInHTML,HashMap<String, DocVector> features) {
		ArrayList<String[]> pairsIM=new ArrayList<String[]>();
		Set<String> files_im_keys=imagesInHTML.keySet();
		Iterator<String> files_im_it = files_im_keys.iterator();
		String key_im, key;
		String temp_pair="";
		ArrayList<String> paired = new ArrayList<String>();
		int counter=0, thous=0;

		while (files_im_it.hasNext()){
			key_im = files_im_it.next();
			if (paired.contains(key_im)){
				continue;
			}
			if (features.get(key_im)==null) continue;
			String lang1=features.get(key_im).codeLang;
			Set<String> mySet1 = new HashSet<String>();
			if (imagesInHTML.get(key_im)==null)
				continue;
			Collections.addAll(mySet1, imagesInHTML.get(key_im));
			if (mySet1.isEmpty()) continue;
			Set<String> all_files_keys=features.keySet();
			Iterator<String> all_files_it = all_files_keys.iterator();
			temp_pair="";
			double temp_pair_score=0.0;
			String temp_lang=null;
			while (all_files_it.hasNext()){
				key = all_files_it.next();
				if (paired.contains(key)){
					continue;
				}
				if (features.get(key)==null) continue;
				String lang2=features.get(key).codeLang;
				if (!lang1.equals(lang2)){
					Set<String> mySet2 = new HashSet<String>();
					if (imagesInHTML.get(key)==null)
						continue;
					Collections.addAll(mySet2, imagesInHTML.get(key));
					if (mySet2.isEmpty()) continue;
					Set intersection = new HashSet(mySet1);
					intersection.retainAll(mySet2);
					if (Math.abs(mySet2.size()-mySet1.size())<im_dif_thr){
						double t1 = Double.parseDouble(Integer.toString(intersection.size()));
						double t2 = Double.parseDouble(Integer.toString((mySet1.size()+mySet2.size()-intersection.size())));
						double jac = t1/t2;
						if (jac<jac_thr){
							continue;
						}
						double p1 = features.get(key_im).numPars;
						double p2 = features.get(key).numPars;
						double cp1 = features.get(key_im).numCleanPars;
						double cp2 = features.get(key).numCleanPars;
						double l1 = (double)features.get(key_im).urlLevel;
						double l2 = (double)features.get(key).urlLevel;
						double tok1 = (double)features.get(key_im).numToksnoBoil;
						double tok2 = (double)features.get(key).numToksnoBoil;
						double dist=0.0;
						double distcleanpar=0.0;
						double disttok=0.0;
						if (p1>p2) dist=p2/p1; else dist=p1/p2;
						if (cp1>cp2) distcleanpar=cp2/cp1; else distcleanpar=cp1/cp2;

						if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
						//System.out.println(key_im+"\t"+key);
						if ( (dist>=pars_thr || distcleanpar>=pars_thr) && disttok > toks_thr && Math.abs(l1-l2)<urllevel_thr){  
							if (jac>temp_pair_score){
								temp_pair=key;
								temp_pair_score=jac*dist;
							}
							temp_lang=lang2;
						}
					}
				}
			}
			if (!temp_pair.isEmpty()){
				if (key_im.compareTo(temp_pair)<0){
					pairsIM.add(new String[] {key_im,temp_pair,lang1, temp_lang, im_pair_method, Double.toString(features.get(key_im).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
					System.out.println("\t\t"+lang1+Constants.SPACE+ temp_lang+Constants.SPACE+key_im+Constants.SPACE+temp_pair);
				}	
				if (key_im.compareTo(temp_pair)>0){
					pairsIM.add(new String[] {temp_pair, key_im, temp_lang, lang1, im_pair_method, Double.toString(features.get(key_im).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
					System.out.println("\t\t"+temp_lang+Constants.SPACE+ lang1+Constants.SPACE+temp_pair+Constants.SPACE+key_im);
				}
			}
			paired.add(key_im);
			paired.add(temp_pair);
			counter++;
			if ((counter/1000)>thous){
				thous++;
				LOGGER.info((thous*1000) + " files have been examined");
			}
		}
		return pairsIM;
	}*/
	
}
