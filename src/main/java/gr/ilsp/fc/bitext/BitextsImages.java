package gr.ilsp.fc.bitext;

import gr.ilsp.fc.bitext.Bitexts.DocVector;
import gr.ilsp.fc.utils.Statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class BitextsImages {
	private final static double jac_thr=0.6;
	private final static double toks_thr=0.4;
	private final static double pars_thr=0.6;
	//private final static int im_dif_thr=3;
	private final static String im_pair_method = "i";
	private final static String imdi_pair_method = "p";
	private final static double digitdist_thr = 0.15;
	private static final int URL_LEVEL=1;
	private static final Logger LOGGER = Logger.getLogger(BitextsImages.class);


	/**
	 * Detects pairs of docs based on common filenames of images and digits. It also checks # of pars, #of toks, urlslevel 
	 * @param imagesInHTML : key is the filename and value is an array with imagenames
	 * @param features holds filename as key and its features in DocVector as value
	 * @param targetlanguages 
	 * @param targetedlang1 
	 * @return
	 * @throws IOException 
	 */
	public static ArrayList<String[]> findpairsIMDI(HashMap<String, String[]> imagesInHTML,HashMap<String, DocVector> features, List<String> targetlanguages)  {
		LOGGER.info("Examining pages for pairing based on common images and digits");
		ArrayList<String[]> pairsIM=new ArrayList<String[]>();
		HashMap<String,Double> temppairs=new HashMap<String,Double>();
		Set<String> files=imagesInHTML.keySet();
		String key1, key2, digits1, digits2, temp_pair="", lang1, lang2 ;
		Set<String> paired = new HashSet<String>();
		int counter=0, thous=0, level1, level2;
		double cp1, cp2, distcleanpar,  p1, p2, tok1, tok2, disttok, dist;
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
			if (imagesInHTML.get(key1)==null){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			if (imagesInHTML.get(key1).length==0){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			lang1  = features.get(key1).codeLang;
			if (!targetlanguages.contains(lang1)){
				paired.add(key1);//even it is not paired, we do not need to examine it
				continue;
			}
			digits1=features.get(key1).digitList;
			Set<String> mySet1 = new HashSet<String>();
			Collections.addAll(mySet1, imagesInHTML.get(key1));
			if (mySet1.isEmpty()){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			level1=features.get(key1).urlLevel;
			p1=features.get(key1).numPars;
			cp1 = features.get(key1).numCleanPars;
			tok1= (double)features.get(key1).numToksnoBoil;
			temp_pair = "";
			double temp_jacdist=0.0;
			String temp_lang=null;
			Iterator<String> files2_it = files.iterator();
			while (files2_it.hasNext()){
				key2 = files2_it.next();
				if (paired.contains(key2))
					continue;
				if (features.get(key2)==null){
					paired.add(key2);//even it is not paired, we do not need to examine it 
					continue;
				}
				lang2=features.get(key2).codeLang;
				if (lang1.equals(lang2))
					continue;
				if (!targetlanguages.contains(lang2)){
					paired.add(key2);//even it is not paired, we do not need to examine it
					continue;
				}
				if (paired.contains(key2))
					continue;
				if (features.get(key2)==null){
					paired.add(key2);//even it is not paired, we do not need to examine it 
					continue;
				}
				
				level2=features.get(key2).urlLevel;
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
				digits2=features.get(key2).digitList;
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
				p2 = features.get(key2).numPars;
				cp2 = features.get(key2).numCleanPars;
				tok2 = (double)features.get(key2).numToksnoBoil;
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
						//LOGGER.info("Matched:\t"+key1+"\t"+temp_pair);
					}else{
						if (t==temp_jacdist & !temp_pair.isEmpty()){
							temp_pair="";
							break;
						}
					}
					temp_lang=lang2;
				}
			}
			if (!temp_pair.isEmpty()){
				String ta= key1 +"&"+temp_pair+"&"+lang1+"&"+temp_lang;
				String ta1= temp_pair+"&"+key1 +"&"+temp_lang+"&"+lang1;
				if (temppairs.containsKey(ta1)){
					if (temppairs.get(ta1)==temp_jacdist){
						if (key1.compareTo(temp_pair)<0){
							pairsIM.add(new String[] {key1,temp_pair,lang1, temp_lang, imdi_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
							//LOGGER.info("Selected:\t"+key1+"\t"+temp_pair);
						}
						if (key1.compareTo(temp_pair)>0){
							pairsIM.add(new String[] {temp_pair, key1, temp_lang, lang1, imdi_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
							//LOGGER.info("Selected:\t"+temp_pair+"\t"+key1);
						}
						paired.add(key1);
						paired.add(temp_pair);
						continue;
					}
				}else{
					temppairs.put(ta, temp_jacdist);
				}
			}
		}
		return pairsIM;
	}


	/**
	 * Detects pairs of docs based on common filenames of images. It also checks # of pars, #of toks, urlslevel 
	 * @param imagesInHTML : key is the filename and value is an array with imagenames
	 * @param features holds filename as key and its features in DocVector as value
	 * @param targetlanguages 
	 * @param targetedlang1 
	 * @return
	 * @throws IOException 
	 */
	public static ArrayList<String[]> findpairsIM(HashMap<String, String[]> imagesInHTML, HashMap<String, DocVector> features, List<String> targetlanguages){
		LOGGER.info("Examining pages for pairing based on common images");
		ArrayList<String[]> pairsIM=new ArrayList<String[]>();
		HashMap<String,Double> temppairs=new HashMap<String,Double>();
		String key1, key2, temp_pair="", lang1, lang2, temp_lang;
		Set<String> paired = new HashSet<String>();
		int counter=0, thous=0, level1, level2;
		double cp1, cp2, distcleanpar,  p1, p2, tok1, tok2, disttok, dist;
		Set<String> files=imagesInHTML.keySet();
		Iterator<String> files1_it = files.iterator();
		while (files1_it.hasNext()){									
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
			}
			key1 = files1_it.next();									//current file in lang ii
			if (paired.contains(key1)) 
				continue;
			if (features.get(key1)==null){
				paired.add(key1); // even it is not paired, we do not need to examine it
				continue;
			}
			if (imagesInHTML.get(key1)==null){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			if (imagesInHTML.get(key1).length==0){
				paired.add(key1);//even it is not paired, we do not need to examine it 
				continue;
			}
			lang1=features.get(key1).codeLang;
			if (!targetlanguages.contains(lang1)){
				paired.add(key1); // even it is not paired, we do not need to examine it
				continue;
			}
			Set<String> mySet1 = new HashSet<String>();
			Collections.addAll(mySet1, imagesInHTML.get(key1));
			if (mySet1.isEmpty()){
				paired.add(key1);//even it is not paired, we do not need to examine it
				continue;
			}
			level1=features.get(key1).urlLevel;
			p1 = features.get(key1).numPars;
			cp1 = features.get(key1).numCleanPars;
			tok1 = (double)features.get(key1).numToksnoBoil;

			temp_pair = "";
			temp_lang ="";
			double temp_jacdist=0.0;
			Iterator<String> files2_it = files.iterator();
			while (files2_it.hasNext()){
				key2 = files2_it.next();
				if (paired.contains(key2))
					continue;
				if (features.get(key2)==null){
					paired.add(key2);//even it is not paired, we do not need to examine it
					continue;
				}
				lang2=features.get(key2).codeLang;
				if (lang1.equals(lang2))
					continue;
				if (!targetlanguages.contains(lang2)){
					paired.add(key2); // even it is not paired, we do not need to examine it
					continue;
				}
				Set<String> mySet2 = new HashSet<String>();
				if (imagesInHTML.get(key2)==null){
					paired.add(key2);//even it is not paired, we do not need to examine it
					continue;
				}
				if (imagesInHTML.get(key2).length==0){
					paired.add(key2);//even it is not paired, we do not need to examine it
					continue;
				}
				Collections.addAll(mySet2, imagesInHTML.get(key2));
				if (mySet2.isEmpty()) {
					paired.add(key2);//even it is not paired, we do not need to examine it
					continue;
				}
				level2=features.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;
				Set intersection = new HashSet(mySet1);
				intersection.retainAll(mySet2);
				double t1 = Double.parseDouble(Integer.toString(intersection.size()));
				double t2 = Double.parseDouble(Integer.toString((mySet1.size()+mySet2.size()-intersection.size())));
				double jac = t1/t2;
				if (jac<jac_thr)
					continue;
				tok2 = (double)features.get(key2).numToksnoBoil;
				disttok=0.0;
				if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
				if (disttok <= toks_thr)
					continue;
				dist=0.0;
				distcleanpar=0.0;
				p2 = features.get(key2).numPars;
				cp2 = features.get(key2).numCleanPars;
				if (p1>p2) dist=p2/p1; else dist=p1/p2;
				if (cp1>cp2) distcleanpar=cp2/cp1; else distcleanpar=cp1/cp2;
				if ( dist>=pars_thr || distcleanpar>=pars_thr ){ 
					double t=0.6*jac+0.2*dist+0.2*disttok; 
					//double t=jac*dist*disttok;
					if (t>temp_jacdist){
						temp_pair=key2;
						temp_jacdist=t;
					}else{
						if (t==temp_jacdist){
							temp_pair="";
							break;
						}
					}
					temp_lang=lang2;
				}
			}
			if (!temp_pair.isEmpty()){
				String ta= key1 +"&"+temp_pair+"&"+lang1+"&"+temp_lang;
				String ta1= temp_pair+"&"+key1 +"&"+temp_lang+"&"+lang1;
				if (temppairs.containsKey(ta1)){
					if (temppairs.get(ta1)==temp_jacdist){
						if (key1.compareTo(temp_pair)<0){
							pairsIM.add(new String[] {key1,temp_pair,lang1, temp_lang, im_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
						}
						if (key1.compareTo(temp_pair)>0){
							pairsIM.add(new String[] {temp_pair, key1, temp_lang, lang1, im_pair_method, Double.toString(features.get(key1).numToksnoOOI+features.get(temp_pair).numToksnoOOI)});
						}
						paired.add(key1);
						paired.add(temp_pair);
						continue;
					}
				}else{
					temppairs.put(ta, temp_jacdist);
				}
			}

		}
		return pairsIM;
	}

}
