package gr.ilsp.fc.bitext;

import gr.ilsp.fc.bitext.Bitexts.DocVector;
import gr.ilsp.fc.utils.Statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class BitextsStruct {
	private static final Logger LOGGER = Logger.getLogger(BitextsStruct.class);
	private static final String UNDERSCORE_STR = "_";
	private static final String appXMLTXText = ".xml.txt";
	private static final double length_thres=0.4;
	private static final double pars_thres=0.4;
	private static final String h_struct_simil = "high";
	private static final String m_struct_simil = "medium";
	private static final String l_struct_simil = "low";
	private static final String SupportVectorsFile = "SVs19_last.txt";
	private static final String WeightsFile = "Ws19_last.txt";
	private static final String ConstFile = "B19_last.txt";
	private static final int URL_LEVEL=10;
	private static final String h_type = "h";
	private static final String m_type = "m";
	private static final String l_type = "l";


	public static ArrayList<String[]> findpairsXML_SVM_NEW(File xmldir,HashMap<String, DocVector> features) {
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
		Set<String> files=features.keySet();
		String  key1, key2, lang1, lang2;
		int level1, level2,  counter = 0, thous=0;
		double dist, sl_length , p2, p1;
		Set<String> paired=new HashSet<String>();
		HashMap<String, Integer> multipairs = new HashMap<String, Integer>();
		Iterator<String> files1_it = files.iterator();
		while (files1_it.hasNext()){								
			counter++;
			if (counter/1000>thous){
				thous++;
				LOGGER.info((thous*1000)+ " files have been examined");
				LOGGER.info("multi-pairs:"+ pairs.size());
			}
			key1 = files1_it.next();
			lang1 = features.get(key1).codeLang;
			level1 = features.get(key1).urlLevel;
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
			p1 = features.get(key1).numPars; //Double.parseDouble(fileprops[2]);
			double res=0.0;
			paired.add(key1);
			Iterator<String> files2_it = files.iterator();
			while (files2_it.hasNext()){
				key2 = files2_it.next();
				if (paired.contains(key2))
					continue;
				if (features.get(key2)==null){
					paired.add(key2); // even it is not paired, we do not need to examine it 
					continue;
				}
				lang2 = features.get(key2).codeLang;
				if (lang2.equals(lang1))
					continue;
				level2 = features.get(key2).urlLevel;
				if (Math.abs(level1-level2)>URL_LEVEL)
					continue;		
				p2 = features.get(key2).numPars; 
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
						res=Math.abs(res);
						double inv_res=1/res;
						int tflength =0;
						for (int mm=0;mm<tl.length;mm++){
							if (tl[mm]>0)
								tflength = tflength+tl[mm];
						}
						String pairlength = Double.toString(features.get(key2).numToksnoOOI+features.get(key1).numToksnoOOI);
						int t1=0, t2=0;
						if (multipairs.containsKey(key1))
							t1 = multipairs.get(key1);
						t1=t1+1;
						multipairs.put(key1, t1);
						if (multipairs.containsKey(key2))
							t2 = multipairs.get(key2);
						t2=t2+1;
						multipairs.put(key2, t2);
						if (t1>2 && t2>2) //we do not care for docs which participate in more than 2 docs 
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
	}

	public static ArrayList<String[]> findBestPairs_SVM(ArrayList<String[]> pairs) {
		ArrayList<String[]> bitexts=new ArrayList<String[]>();
		Double[] new_temp1 = new Double[pairs.size()];
		String[][] pairlist = new String[pairs.size()][5];
		Double[] editdist =  new Double[pairs.size()];
		for (int i = 0; i < new_temp1.length; i++){
			new_temp1[i] =Double.parseDouble(pairs.get(i)[2]);
			editdist[i] =Double.parseDouble(pairs.get(i)[2]);
			int ind = pairs.get(i)[0].indexOf(UNDERSCORE_STR);
			pairlist[i][0]=pairs.get(i)[0].substring(0, ind);
			pairlist[i][1]=pairs.get(i)[0].substring(ind+1);
			int ind1 = pairs.get(i)[1].indexOf(UNDERSCORE_STR);
			pairlist[i][2]=pairs.get(i)[1].substring(0, ind1);
			pairlist[i][3]=pairs.get(i)[1].substring(ind1+1);
			pairlist[i][4]=pairs.get(i)[3];
		}
		Arrays.sort(new_temp1);
		int kk = 0;
		for (int i = 0; i < new_temp1.length; i++){
			if (i > 0 && new_temp1[i].equals(new_temp1[i -1]))
				continue;
			new_temp1[kk++] = new_temp1[i];
		}
		Double[] new_temp = new Double[kk];
		System.arraycopy(new_temp1, 0, new_temp, 0, kk);
		for (int i = 0; i < new_temp.length; i++){
			for (int j = 0; j < pairlist.length; j++){
				if (!pairlist[j][0].isEmpty() & !pairlist[j][1].isEmpty()){
					if (new_temp[i].equals(editdist[j])){
						String f1 = pairlist[j][0];
						String f2 = pairlist[j][1];
						String l1 = pairlist[j][2];
						String l2 = pairlist[j][3];
						bitexts.add(new String[] {f1, f2,l1,l2,"",pairlist[j][4]});
						for(int k=0; k<pairlist.length;k++){
							if (pairlist[k][0].equals(f1) || pairlist[k][0].equals(f2))
								pairlist[k][0]="";		
							if (pairlist[k][1].equals(f1) || pairlist[k][1].equals(f2))
								pairlist[k][1]="";
						}
					}
				}
			}
		}
		return bitexts;
	}

	public static ArrayList<String[]> findBestPairs_SVM_NEW(ArrayList<String[]> pairs, String methods) {
		ArrayList<String[]> bitexts=new ArrayList<String[]>();
		int[][] counts=new int[pairs.size()][2];
		int counter = 0, thous=0;
		int step=1000; //int step = Math.min(pairs.size()/20,20000);
		for (int ii=0;ii<pairs.size();ii++){
			counter++;
			if (counter/step>thous){
				thous++;
				LOGGER.info((thous*step)+ " pairs have been examined");
			}
			//if (counts[ii][0] >1 && counts[ii][1] >1)
			//	continue;
			for (int jj=0;jj<pairs.size();jj++){
				if (pairs.get(ii)[0].equals(pairs.get(jj)[0]) || pairs.get(ii)[0].equals(pairs.get(jj)[1]) )
					counts[ii][0] = counts[ii][0]+1;
				if (pairs.get(ii)[1].equals(pairs.get(jj)[0]) || pairs.get(ii)[1].equals(pairs.get(jj)[1]) )
					counts[ii][1] = counts[ii][1]+1;
			}
		}
		LOGGER.info("Selection finished.");
		int[] flags=new int[counts.length];
		double dist, dist1;
		if (methods.contains(h_type)){
			for (int jj=0;jj<pairs.size();jj++){
				if (counts[jj][0]==1 & counts[jj][1]==1 & flags[jj]==0){
					LOGGER.debug(pairs.get(jj)[0]+UNDERSCORE_STR+ pairs.get(jj)[1]+"___"+pairs.get(jj)[5]);
					bitexts.add(new String[] {pairs.get(jj)[0], pairs.get(jj)[1],pairs.get(jj)[2],pairs.get(jj)[3],h_struct_simil,pairs.get(jj)[5]});
					flags[jj]=1;
				}
			}
			LOGGER.info("pairs with "+ h_struct_simil+" similarity found.");
		}
		if (methods.contains(m_type)){
			for (int jj=0;jj<pairs.size();jj++){
				if (counts[jj][0]==1 & counts[jj][1]==2 & flags[jj]==0){
					dist = Double.parseDouble(pairs.get(jj)[4]);
					dist1=0.0;
					int ind=-1;
					for (int kk=0;kk<pairs.size();kk++){
						if (pairs.get(kk)[1].equals(pairs.get(jj)[1]) & kk!=jj)
							ind=1;
						if (pairs.get(kk)[0].equals(pairs.get(jj)[1]) & kk!=jj)
							ind=0;
						if (ind>-1){
							dist1 = Double.parseDouble(pairs.get(kk)[4]);
							if (dist<dist1){
								LOGGER.debug(pairs.get(jj)[0]+UNDERSCORE_STR+ pairs.get(jj)[1]+"___"+pairs.get(jj)[5]);
								bitexts.add(new String[] {pairs.get(jj)[0], pairs.get(jj)[1],pairs.get(jj)[2],pairs.get(jj)[3],m_struct_simil,pairs.get(jj)[5]});
								counts[kk][ind]=0;
								flags[jj]=2;
								flags[kk]=-1;
							}else{
								flags[jj]=-1;
								counts[kk][ind]=counts[kk][ind]-1;
							}
							break;
						}
					}
				}
			}
			for (int jj=0;jj<pairs.size();jj++){
				if (counts[jj][0]==2 & counts[jj][1]==1 & flags[jj]==0){
					dist = Double.parseDouble(pairs.get(jj)[4]);
					dist1=0.0;
					for (int kk=0;kk<pairs.size();kk++){
						if (pairs.get(kk)[0].equals(pairs.get(jj)[0]) & kk!=jj){
							dist1 = Double.parseDouble(pairs.get(kk)[4]);
							if (dist<dist1){
								LOGGER.debug(pairs.get(jj)[0]+UNDERSCORE_STR+ pairs.get(jj)[1]+"___"+pairs.get(jj)[5]);
								bitexts.add(new String[] {pairs.get(jj)[0], pairs.get(jj)[1],pairs.get(jj)[2],pairs.get(jj)[3],m_struct_simil,pairs.get(jj)[5]});
								counts[kk][0]=0;
								flags[jj]=2;
								flags[kk]=-1;
							}else{
								flags[jj]=-1;
								counts[kk][0]=counts[kk][0]-1;
							}
							break;
						}
					}
				}
			}
			LOGGER.info("pairs with "+ m_struct_simil+" similarity found.");
		}
		if (methods.contains(l_type)){
			ArrayList<String[]> new_pairs=new ArrayList<String[]>();
			for (int ii=0;ii<pairs.size();ii++){
				if (flags[ii]==0){
					new_pairs.add(new String[] {pairs.get(ii)[0]+UNDERSCORE_STR+pairs.get(ii)[1],pairs.get(ii)[2]
							+UNDERSCORE_STR+pairs.get(ii)[3],pairs.get(ii)[4],pairs.get(ii)[5]});
				}
			}
			ArrayList<String[]> new_bitexts=new ArrayList<String[]>();
			new_bitexts=findBestPairs_SVM(new_pairs);
			for (int ii=0;ii<new_bitexts.size();ii++){
				bitexts.add(new String[] {new_bitexts.get(ii)[0], 
						new_bitexts.get(ii)[1],new_bitexts.get(ii)[2],new_bitexts.get(ii)[3],l_struct_simil,new_bitexts.get(ii)[5]});
			}
			LOGGER.info("pairs with "+ l_struct_simil+" similarity found.");
		}
		return bitexts;
	}

	public static int[] getPairProps(ArrayList<String[]> bitextsSTRUCT) {
		int[] counters = new int[3];
		for (int kk=0;kk<bitextsSTRUCT.size();kk++){
			if (bitextsSTRUCT.get(kk)[4].equals(h_struct_simil)) {
				counters[0]++;
				continue;
			}
			if (bitextsSTRUCT.get(kk)[4].equals(m_struct_simil)){
				counters[1]++;
				continue;
			}
			if (bitextsSTRUCT.get(kk)[4].equals(l_struct_simil))
				counters[2]++;
		}
		return counters;
	}

}
