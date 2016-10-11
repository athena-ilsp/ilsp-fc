package gr.ilsp.fc.bitext;

import gr.ilsp.fc.bitext.Bitexts.DocVector;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class BitextsURLs {
	private static final Logger LOGGER = Logger.getLogger(BitextsURLs.class);
	private static final String UNDERSCORE_STR = "_";
	private static final String HYPHEN = "-";
	private static final String pair_type_link = "a";
	private static final String pair_type_URL = "u";
	private static final int URL_LEVEL=1;
	private final static double max_par_dif=0.6;
	private final static double max_tok_dif=0.4;
	/**
	 * get pairs have been detected as pairs during crawl
	 * @param hreflangIDPairs holds id of file and its lang as key and id of paired file and its lang as value (ids and langs are seperated by "-") 
	 * @param features holds filename as key and its features in DocVector as value
	 * @return list of pairs (id1, id2, lang1, lang2, type="a", total num of tokens)
	 */	
	//FIXME should we add a check on #of paragraphs and #of tokens? If yes, we need features variable
	public static ArrayList<String[]> findpairsHRefLang(Map<String, String> hreflangIDPairs, HashMap<String, DocVector> features, List<String> targetlanguages) {
		LOGGER.info("Examining pages based on links");
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		Set<String> paired = new HashSet<>();
		for (Map.Entry<String, String> entry : hreflangIDPairs.entrySet()) {
			String id1_lang = entry.getKey();
			int p1 = id1_lang.lastIndexOf(HYPHEN);
			String lang1 = id1_lang.substring(0, p1);
			if (!targetlanguages.contains(lang1))
				continue;
			String id1 = id1_lang.substring(p1+1);

			String id2_lang = entry.getValue();
			int p2 = id2_lang.lastIndexOf(HYPHEN);
			String lang2 = id2_lang.substring(0, p2);
			if (!targetlanguages.contains(lang2))
				continue;
			String id2 = id2_lang.substring(p2+1);
			if (lang1.equals(lang2))
				continue;
			if (paired.contains(id1) && paired.contains(id2)) // Do not add the other pair direction. 
				continue;
			//FIXME multiple matches should be examined
			if (paired.contains(id1_lang)){ // if multiple matches are found, keep only the first  
				System.out.println("Multiple matches :" + id1_lang);
				continue;
			}
			if (paired.contains(id2_lang)){// if multiple matches are found, keep only the first. 
				System.out.println("Multiple matches :" + id2_lang);
				continue;
			}
			String temp[] = {id1_lang, id2_lang, lang1,lang2, pair_type_link, Double.toString(features.get(id1_lang).numToksnoOOI+features.get(id2_lang).numToksnoOOI)};
			if (id1_lang.compareTo(id2_lang)>0){
				temp[0] = id2_lang;
				temp[1] = id1_lang;
				temp[2] = lang2;
				temp[3] = lang1;
			}
			pairs.add(temp);
			paired.add(id1_lang);
			paired.add(id2_lang);
		}
		return pairs;
	}

	/**
	 * Detect pairs of documents based on special patterns detected in URLs. 
	 * Returns detected pairs: pair[0]=filename1, pair[1]=filename2, pair[2]=lang1, pair[3]=lang2, pair[4]="u" (method)
	 * @param filesURLS : HashMap (filenames as keys) with URLs
	 * @param features	: HashMap (filenames as keys) with DocVectors
	 * @param urls_repls: array with replacements to be applied
	 * @return list of pairs (id1, id2, lang1, lang2, type="u", total num of tokens)
	 */
	//FIXME should we add a check on # of paragraphs and #of tokens?
	public static ArrayList<String[]> findpairsURLs(HashMap<String, DocVector> features, String[][] urls_repls, List<String> targetlanguages) {
		LOGGER.info("Examining pages for pairing based on URLs");
		ArrayList<String[]> result=new ArrayList<String[]>();
		Set<String> paired=new HashSet<String>();
		Set<String> files=features.keySet();
		String key1, key2, file_url1, file_url2, lang1, lang2;
		int counter = 0, thous=0, level1, level2;
		double p1, p2, tok1, tok2, disttok, dist;
		Iterator<String> files1_it = files.iterator();
		while (files1_it.hasNext()){									
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
			}
			key1 = files1_it.next();									//current file in lang ii
			String[] pair=new String[6];
			if (paired.contains(key1)) 
				continue;
			if (features.get(key1)==null){
				paired.add(key1); // even it is not paired, we do not need to examine it
				continue;
			}
			lang1=features.get(key1).codeLang;
			if (!targetlanguages.contains(lang1)){
				paired.add(key1); // even it is not paired, we do not need to examine it
				continue;
			}
			level1=features.get(key1).urlLevel;
			p1=features.get(key1).numPars;
			tok1=features.get(key1).numToksnoBoil;
			file_url1=features.get(key1).url;
			Iterator<String> files2_it = files.iterator();				//iterator of files in lang jj
			boolean match_found;
			while (files2_it.hasNext()){
				key2 = files2_it.next();
				if (paired.contains(key2)) 
					continue;
				if (features.get(key2)==null){
					paired.add(key2); // even it is not paired, we do not need to examine it
					continue;
				}
				lang2=features.get(key2).codeLang;
				if (!targetlanguages.contains(lang2)){
					paired.add(key2); // even it is not paired, we do not need to examine it
					continue;
				}
				if (lang1.equals(lang2))
					continue;
				level2=features.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;
				p2=features.get(key2).numPars;
				tok2=features.get(key2).numToksnoBoil;
				dist=0.0;
				if (p1>p2) dist=p2/p1; else dist=p1/p2;
				if (dist<max_par_dif)
					continue;
				disttok=0.0;
				if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
				if (disttok <= max_tok_dif) 
					continue;
				//file_url1=filesURLS.get(key1);
				file_url2=features.get(key2).url;
				match_found=false;
				String updatedfile_url = file_url1; 
				if (urls_repls!=null){
					for (int ii=0;ii<urls_repls.length;ii++){
						updatedfile_url = updatedfile_url.replaceAll(urls_repls[ii][1],urls_repls[ii][0]);
						if (file_url1.replaceAll(urls_repls[ii][0],urls_repls[ii][1]).equals(file_url2)){
							match_found=true;
							break;
						}
						if (updatedfile_url.equals(file_url2)){
							match_found=true;
							break;
						}
					}
				}
				if (match_found || checkUrsPatterns(file_url1.toLowerCase(), file_url2.toLowerCase(), lang1, lang2)){
					LOGGER.debug(file_url1);
					LOGGER.debug(file_url2);
					pair[0]=key1;					pair[1]=key2;
					pair[2]=lang1;					pair[3]=lang2;
					pair[4]=pair_type_URL;
					pair[5]=Double.toString(features.get(key1).numToksnoOOI+features.get(key2).numToksnoOOI);
					if (key1.compareTo(key2)>0){
						pair[0]=key2;			pair[1]=key1;
						pair[2]=lang2;			pair[3]=lang1;
					}
					result.add(pair);
					paired.add(key1);
					paired.add(key2);
					break;
				}
			}
		}
		return result;
	}


	/**
	 * instead of using special pattern matches in URLs, we could compare URLs, and calculate edit-distance 
	 * @param filesURLS
	 * @param props
	 * @return
	 */
	//FIXME This method is not used!
	public static ArrayList<String[]> findpairsURLs_dist(HashMap<String, String> filesURLS, HashMap<String, String[]> props) {
		ArrayList<String[]> result=new ArrayList<String[]>();
		ArrayList<String> paired=new ArrayList<String>();

		ArrayList<String> cand_paired=new ArrayList<String>();
		ArrayList<Double> dist_paired=new ArrayList<Double>();

		Set<String> files_keys=filesURLS.keySet();
		Iterator<String> files_it = files_keys.iterator();
		String key, key1, file_url, file_url1;
		while (files_it.hasNext()){
			key = files_it.next();
			if (props.get(key)==null) 
				continue;
			String lang1=props.get(key)[1];
			file_url=filesURLS.get(key);              
			int ind = file_url.indexOf(".");
			int ind1 = file_url.indexOf("/",ind);
			String file_url_file_url="";
			if (ind1<0)
				file_url_file_url=file_url.substring(ind, file_url.length());
			else
				file_url_file_url=file_url.substring(ind1, file_url.length());
			Iterator<String> files_it1 = files_keys.iterator();
			LOGGER.debug(file_url);
			while (files_it1.hasNext()){
				key1 = files_it1.next();
				if (props.get(key1)==null) continue;
				String lang2=props.get(key1)[1];
				if (!lang1.equals(lang2)){
					file_url1=filesURLS.get(key1);
					ind = file_url1.indexOf(".");
					ind1 = file_url1.indexOf("/",ind);
					String file_url1_file_url1="";
					if (ind<0)
						file_url1_file_url1=file_url1.substring(ind, file_url1.length());
					else
						file_url1_file_url1=file_url1.substring(ind1, file_url1.length());

					double  temp_dist=Statistics.editDist(file_url_file_url,file_url1_file_url1)
							/Math.max(file_url_file_url.length()-ind, file_url1_file_url1.length()-ind1);
					LOGGER.debug(file_url1);
					dist_paired.add(temp_dist);
					cand_paired.add(key+UNDERSCORE_STR+key1);
					LOGGER.debug(dist_paired.get(dist_paired.size()-1));
					LOGGER.debug(key+"\t"+key1+"\t"+file_url.length()+"\t"+file_url1.length()+"\t"+temp_dist);

				}
			}
		}
		int ind=-1, count_files=0, stop=1;
		double val=1000000; 
		while (count_files<files_keys.size() & stop==1){
			for (int ii=0;ii<cand_paired.size();ii++){
				if (dist_paired.get(ii)<val){
					ind=ii;
					val=dist_paired.get(ii);
				}
			}
			if (val>=0.25){
				stop=0;
				break;
			}
			String  temp_pair=cand_paired.get(ind);
			String[] temp_files=temp_pair.split(UNDERSCORE_STR);
			if (!paired.contains(temp_files[0]) && !paired.contains(temp_files[1])){
				String[] pair=new String[6];
				pair[0]=temp_files[0];
				pair[1]=temp_files[1];
				pair[2]=props.get(pair[0])[1];
				pair[3]=props.get(pair[1])[1];
				pair[4]=pair_type_URL;
				pair[5]=Integer.toString(Integer.parseInt(props.get(pair[0])[4])+Integer.parseInt(props.get(pair[1])[4]));
				result.add(pair);
				paired.add(pair[0]);
				paired.add(pair[1]);
				count_files=count_files+2;
				System.out.println(temp_files[0]+"_"+temp_files[1]+ " : "+ val);
				for (int ii=0;ii<cand_paired.size();ii++){
					temp_pair=cand_paired.get(ii);
					temp_files=temp_pair.split(UNDERSCORE_STR);
					if (paired.contains(temp_files[0]) | paired.contains(temp_files[1])){
						dist_paired.set(ii,1000000.0); 
						val=1000000;
					}
				}
			}
		}
		return result;
	}


	/**
	 * check some common patterns in URLs of multilingual websites 
	 * @param url1
	 * @param url2
	 * @param lang1
	 * @param lang2
	 * @return
	 */
	private static boolean checkUrsPatterns(String url1, String url2, String lang1, String lang2) {
		boolean found = false;
		if (url1.equals(url2))
			return false;

		if (checkUrsCommonPatterns(lang1, lang2,url1,url2))
			return true;

		if (checkUrslangPatterns(url1,url2))
			return true;

		/*if (url1.replace("/"+lang1+"/", "/"+lang2+"/").equals(url2) || url1.replace("/"+lang2+"/", "/"+lang1+"/").equals(url2)
				|| url1.replace("_"+lang1, "_"+lang2).equals(url2)	|| url1.replace("_"+lang2, "_"+lang1).equals(url2)
				|| url1.replace("/"+lang1+"/", "/").equals(url2)	|| url2.replace("/"+lang2+"/", "/").equals(url1)
				|| url1.replace("-"+lang1,"-"+lang2).equals(url2)   || url1.replace("-"+lang2,"-"+lang1).equals(url2) 
				|| url1.replace("lang="+lang1, "lang="+lang2).equals(url2) || url1.replace("lang="+lang2, "lang="+lang1).equals(url2) 
				|| url1.replace(lang1,lang2).equals(url2) || url1.replace(lang2,lang1).equals(url2)  
				|| (url1.substring(0, url1.length()-4).equals(url2.substring(0, url2.length()-4)) &
						url1.endsWith("="+lang1) & url2.endsWith("="+lang2)))
			return true;*/

		/*if (url1.replace("_"+lang1, "_"+lang2).equals(url2)
				| url1.replace("/"+lang1+"/", "/"+lang2+"/").equals(url2)
				| url1.replace("/"+lang1+"/", "/").equals(url2) | url2.replace("/"+lang2+"/", "/").equals(url1)
				| url1.replace("lang=1", "lang=2").equals(url2)
				| url1.replace("lang,1", "lang,2").equals(url2)
				| url1.replace("lang="+lang1, "lang="+lang2).equals(url2)
				| url1.replace("lingua="+lang1, "lingua="+lang2).equals(url2)
				| url1.toLowerCase().replace("langid=1", "langid=2").equals(url2.toLowerCase())
				| url1.replace("lang,1","lang,2").equals(url2) | url1.replace("lang,2","lang,1").equals(url2)
				| url1.replace(lang1,lang2).equals(url2)
				| (url1.substring(0, url1.length()-4).equals(url2.substring(0, url2.length()-4)) &
						url1.endsWith("="+lang1) & url2.endsWith("="+lang2)))
			return true;*/

		lang1 =  ISOLangCodes.get2LetterCode(lang1);
		lang2 =  ISOLangCodes.get2LetterCode(lang2);

		if (checkUrsCommonPatterns(lang1, lang2,url1,url2))
			return true;

		if (checkUrslangPatterns(url1,url2))
			return true;

		return found;
	}

	private static boolean checkUrsCommonPatterns(String lang1, String lang2, String url1, String url2) {
		boolean found=false;
		if ( url1.replace("/"+lang1+"/", "/").equals(url2)	|| url2.replace("/"+lang2+"/", "/").equals(url1)
				|| url1.replace(lang1,lang2).equals(url2) || url1.replace(lang2,lang1).equals(url2))  
			return true;
		if ( url1.replace("/"+lang1+"/", "/"+lang2+"/").equals(url2) || url2.replace("/"+lang2+"/", "/"+lang1+"/").equals(url1))
			return true;
		if ( url1.replace("."+lang1+".", "."+lang2+".").equals(url2) || url2.replace("."+lang2+".", "."+lang1+".").equals(url1))
			return true;
		if ( url1.replace("_"+lang1, "_"+lang2).equals(url2) || url2.replace("_"+lang2, "_"+lang1).equals(url1))
			return true;
		if ( url1.replace("_"+lang1, "").equals(url2) || url2.replace("_"+lang2, "").equals(url1))
			return true;

		if ( url1.replace("-"+lang1, "-"+lang2).equals(url2) || url2.replace("-"+lang2, "-"+lang1).equals(url1))
			return true;
		return found;
	}

	private static boolean checkUrslangPatterns(String url1, String url2) {
		boolean found=false;
		String[] patts = {"0", "1", "2", "3", "4"};
		if (url1.contains("?&l") && url2.contains("?&l")){
			for (int ii=0;ii<patts.length;ii++){
				found =false;
				for (int jj=0;jj<patts.length;jj++){
					if (ii!=jj){
						if (url1.replace("?&l="+ii, "?&l="+jj).equals(url2)){
							found=true;
							break;
						}
					}
				}
				if (found)
					break;
			}
		}
		if (found)
			return true;
		if (url1.contains("lingua") && url2.contains("lingua")){
			for (int ii=0;ii<patts.length;ii++){
				found =false;
				for (int jj=0;jj<patts.length;jj++){
					if (ii!=jj){
						if (url1.replace("lingua="+ii, "lingua="+jj).equals(url2)){
							found=true;
							break;
						}
					}
				}
				if (found)
					break;
			}
		}
		if (found)
			return true;

		if (url1.contains("lang") && url2.contains("lang")){
			for (int ii=0;ii<patts.length;ii++){
				found =false;
				for (int jj=0;jj<patts.length;jj++){
					if (ii!=jj){
						if (url1.replace("lang="+ii, "lang="+jj).equals(url2)
								|| url1.replace("lang,"+ii, "lang,"+jj).equals(url2)
								|| url1.replace("langid=,"+ii, "langid=,"+jj).equals(url2)
								|| url1.replace("_,"+ii, "_"+jj).equals(url2)){
							found=true;
							break;
						}
					}
				}
				if (found)
					break;
			}
		}
		return found;
	}

}
