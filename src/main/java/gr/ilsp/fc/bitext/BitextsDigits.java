package gr.ilsp.fc.bitext;

import gr.ilsp.fc.bitext.Bitexts.DocVector;
import gr.ilsp.fc.utils.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

public class BitextsDigits {
	private static final Logger LOGGER = Logger.getLogger(BitextsDigits.class);
	private final static int len_thr=30;
	private final static int len_thr2=15;
	private final static int len_thr1=90;
	private final static int len_thr3=500;
	//private final static int step=20;
	private final static double max_dig_dif=0.3;
	private final static double max_par_dif=0.6;
	private final static double max_tok_dif=0.4;
	private final static String dig_pair_method = "d";
	private static final int URL_LEVEL=1;
	/**
	 * identifies pairs based on common digits
	 * @param features holds filename as key and its features in DocVector as value
	 * @param targetlanguages 
	 * @return
	 */
	//FIXME thresholds should be checked and selected more carefully (if we keep using thresholds)
	public static ArrayList<String[]> findpairsDig(HashMap<String,DocVector> features, List<String> targetlanguages) {
		LOGGER.info("Examining pages based on common digits");
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		String  key1, key2, digits1, digits2, lang1, lang2;
		String ind="", det_lang="";
		int level1, level2, digits1length, digits2length, counter = 0, thous=0;
		double mindist, p1, p2, tok1, tok2, disttok, dist;
		Set<String> paired=new HashSet<String>();
		Set<String> files=features.keySet();
		Iterator<String> files1_it = files.iterator();
		while (files1_it.hasNext()){									
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
			}
			key1 = files1_it.next();			
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
			digits1=features.get(key1).digitList;
			if (StringUtils.isBlank(digits1)){
				paired.add(key1); // even it is not paired, we do not need to examine it 
				continue;			
			}
			level1=features.get(key1).urlLevel;
			p1=features.get(key1).numPars;
			tok1=features.get(key1).numToksnoBoil;
			mindist =max_dig_dif;
			digits1length = digits1.length();
			Iterator<String> files2_it = files.iterator();
			ind="";
			while (files2_it.hasNext()){
				key2 = files2_it.next();
				//System.out.println(key1+"\t"+key2);
				lang2=features.get(key2).codeLang;
				if (lang1.equals(lang2))
					continue;
				if (!targetlanguages.contains(lang2)){
					paired.add(key2); // even it is not paired, we do not need to examine it
					continue;
				}
				if (paired.contains(key2))
					continue;
				if (features.get(key2)==null){
					paired.add(key2); // even it is not paired, we do not need to examine it 
					continue;
				}
				digits2=features.get(key2).digitList;
				digits2length = digits2.length();
				if (StringUtils.isBlank(digits2)){
					paired.add(key2); // even it is not paired, we do not need to examine it
					continue;	
				}
				
				level2=features.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;
				p2=features.get(key2).numPars;
				dist=0.0;
				if (p1>p2) dist=p2/p1; else dist=p1/p2;
				if (dist<max_par_dif)
					continue;
				tok2=features.get(key2).numToksnoBoil;
				disttok=0.0;
				if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
				if (disttok <= max_tok_dif) 
					continue;
				double temp = Statistics.editDist(digits1, digits2) / (double) Math.min(digits1length,digits2length);
				if (temp>0.2 && (digits2length<len_thr1 || digits1length<len_thr1) 
						&& (digits2length>len_thr || digits1length>len_thr))
					continue;
				if (temp>0.1 && (digits2length<len_thr || digits1length<len_thr) 
						&& (digits2length>=len_thr2 || digits1length>=len_thr2))
					continue;
				if (digits2length<len_thr2 && digits1length<len_thr2 && temp>0) 
					continue;
				int minlen = Math.min(digits1length,digits2length); 
				if (minlen>len_thr3){
					temp = Statistics.editDist(digits1.substring(0, minlen), digits2.substring(0, minlen)) / (double)minlen;	
				}
				if (Precision.round(temp, 2)<=mindist){
					mindist=temp;
					ind = key2;
					det_lang = lang2;
				}else{
					if (temp==mindist & !ind.isEmpty()){
						ind="";
						break;
					}
				}
			}
			if (ind.isEmpty())	
				continue;
			else{
				if (key1.compareTo(ind)<0){
					pairs.add(new String[] {key1,ind,lang1,det_lang,dig_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(ind).numToksnoOOI)});
				}else{
					pairs.add(new String[] {ind,key1,det_lang, lang1,dig_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(ind).numToksnoOOI)});
				}
				paired.add(key1);
				paired.add(ind); 
			}
			//System.out.println("paired:"+"\t"+key1+"\t"+ind);
		}
		return pairs;
	}

	//FIXME: which symbols to keep?
	public static ArrayList<String[]> findpairsSym(	HashMap<String, DocVector> features) {
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		ArrayList<String> already_selected = new ArrayList<String>();
		String  key1, key2, lang1, lang2="", digits1, digits2;
		String ind="", det_lang="";
		int level1, level2;
		double mindist;
		Set<String> files_keys=features.keySet();
		Iterator<String> files_it1 = files_keys.iterator();
		while (files_it1.hasNext()){
			key1 = files_it1.next();
			if (already_selected.contains(key1))
				continue;
			if (features.get(key1)==null)
				continue;
			digits1=features.get(key1).symbolList;
			if (StringUtils.isBlank(digits1))
				continue;			
			lang1=features.get(key1).codeLang;
			level1=features.get(key1).urlLevel;
			//double p1=features.get(key1).numPars;
			//double tok1=features.get(key1).numToksnoBoil;
			mindist =10000000;
			Iterator<String> files_it2 = files_keys.iterator();
			while (files_it2.hasNext()){
				key2 = files_it2.next();
				if (already_selected.contains(key2))
					continue;
				if (features.get(key2)==null)
					continue;
				lang2=features.get(key2).codeLang;
				if (lang1.equals(lang2))
					continue;
				digits2=features.get(key2).symbolList;
				if (StringUtils.isBlank(digits2))
					continue;				
				level2=features.get(key2).urlLevel;
				if (Math.abs(level1-level2)>1)
					continue;
				double temp = 100;
				//System.out.println(key1+"\t"+key2);
				//if ((key1.equals("de-86") && key2.equals("en-109")) || (key1.equals("en-1") && key2.equals("de-79")) || (key1.equals("de-85") && key2.equals("en-106"))
				//		|| (key1.equals("de-87") && key2.equals("en-110")) || (key1.equals("en-104") && key2.equals("de-83"))      ){
				//	temp=0;
				//}
				if (mindist>temp){
					mindist=temp;
					ind = key2;
					det_lang = lang2;
				}
			}
			if (mindist>0.6){
				continue;
			}else{
				if (key1.compareTo(ind)>0){
					pairs.add(new String[] {ind,key1,det_lang, lang1,dig_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(ind).numToksnoOOI)});
				}else{
					pairs.add(new String[] {key1,ind,lang1,det_lang,dig_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(ind).numToksnoOOI)});
				}
				already_selected.add(key1);
				already_selected.add(ind); 
			}
		}
		return pairs;
	}

}
