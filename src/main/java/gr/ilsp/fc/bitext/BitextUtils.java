package gr.ilsp.fc.bitext;

import gr.ilsp.fc.bitext.Bitexts.DocVector;
import gr.ilsp.fc.main.ReadResources;

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
	private static final String UNDERSCORE_STR = "_";
	private static final String PUNCT = ".";
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
			if (method.isEmpty()){
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
			String file1 = key.split(UNDERSCORE_STR)[0];
			String file2 = value.split(UNDERSCORE_STR)[0];
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
			paired.add(file1+PUNCT+input_type);
			paired.add(file2+PUNCT+input_type);
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
				if (file.isDirectory() || file.getName().contains(UNDERSCORE_STR)) { // Skip directories and cesAlignFiles
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
			a = FileUtils.readLines(new File(docpairsfile));
			for (String t:a){
				String[] l=t.split(" ");
				res.put(l[0], l[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return res;
	}

}
