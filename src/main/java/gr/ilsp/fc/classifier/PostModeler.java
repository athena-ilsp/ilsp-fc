package gr.ilsp.fc.classifier;

import gr.ilsp.fc.tmxhandler.GetTMXsubset;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class PostModeler implements Serializable{
	private static final Logger LOGGER = Logger.getLogger(PostModeler.class);
	
	private static double thr = 0.3;
		
	
	public static void main(String[] args) throws IOException {
		
		File compositionfile = new File("C:/Users/vpapa/ELRC/tld/hr/crawls/webcorpus_v2_ILSP-FC_eng-hrv.en_txts.mallet_composition.txt");
		int clusters = 10;
		getClusterTextByMax(compositionfile, clusters);
		System.exit(0);
		
		File tmxfile = new File("C:/Users/vpapa/ELRC/tld/hr/crawls/webcorpus_v2_ILSP-FC_eng-hrv.tmx");
		String lang1="eng";
		String lang2 = "hrv";
		List<SegPair> segpairs = GetTMXsubset.getTUsFromTMX(tmxfile, lang1, lang2);
		//for (SegPair segpair:segpairs){
		//	segpair.l1url
		//}
		
		//File compositionfile = new File(args[0]);
		
		//int clusters=Integer.parseInt(args[1]);
		
		int counter_none=0, counter_multi=0;
		int linecounter=0;
		Map<Integer,List<String>> cluster_groups = new HashMap<Integer,List<String>>();
		//BufferedReader br = new BufferedReader(new FileReader(compositionfile));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(compositionfile.getAbsolutePath()), Constants.UTF8));
		String line;
		boolean found = false;
		while ((line = br.readLine()) != null) {
			linecounter++;
			String[] segment = line.split("\t");
			List<String> segment_clusters = new ArrayList<String>();
			for (int ii=2;ii<segment.length;ii++){
				if (Double.parseDouble(segment[ii])>thr){
					segment_clusters.add(Integer.toString(ii-2));
					List<String> temp =  new ArrayList<String>();
					if (cluster_groups.containsKey(ii-2))
						temp = cluster_groups.get(ii-2);
					temp.add(segment[0]);
					cluster_groups.put(ii-2,temp);
					//segment_clusters.add(Integer.toString(ii-2));
				}
			}
			if (segment_clusters.isEmpty())
				counter_none++;
			if (segment_clusters.size()>1){
				System.out.println(segment[0]+segment_clusters);
				counter_multi++;
			}
		}
		br.close();
		System.out.println(linecounter+"\t"+ counter_none+"\t"+counter_multi);
		int counter_inter=0;
		for (int ii=0;ii<clusters;ii++){
			List<String> list1 = cluster_groups.get(ii);
			System.out.println(ii+"\t"+ list1.size());
			for (int jj=0;jj<clusters;jj++){
				List<String> list2 = cluster_groups.get(jj);
				counter_inter=0;
				for (int kk=0;kk<list1.size();kk++){
					if (list2.contains(list1.get(kk)))
						counter_inter++;
				}
				double perce = (double)counter_inter/(double)(list1.size());
				double perce1 = Precision.round(perce,5);
				double perce2 = Precision.round(100*perce1,3);
				System.out.println(ii + "\t" + jj + "\t" + counter_inter + "\t" +perce2+ "%");
			}
		}
	}


	private static void getClusterTextByMax(File compositionfile, int clusters) throws IOException {
		String basePath = compositionfile.getParent();
		Map<Integer,List<String>> cluster_groups = new HashMap<Integer,List<String>>();
		int linecounter=0, counter=0;
		//BufferedReader br = new BufferedReader(new FileReader(compositionfile));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(compositionfile.getAbsolutePath()), Constants.UTF8));
		String line;
		while ((line = br.readLine()) != null) {
			linecounter++;
			String[] segment = line.split("\t");
			double max_value=0, temp_w=0;
			int ind = -1;
			for (int ii=2;ii<segment.length;ii++){
				temp_w = Double.parseDouble(segment[ii]);
				if (temp_w>max_value){
					max_value = temp_w;
					ind = ii-2;
				}
			}
			if (ind<0){
				System.out.println("OOPS\t"+line);
				continue;
			}
			counter++;
			List<String> temp =  new ArrayList<String>();
			String segment_text = FileUtils.readFileToString(new File(FilenameUtils.concat(basePath, segment[1])), Constants.UTF8);
			if (cluster_groups.containsKey(ind))
				temp = cluster_groups.get(ind);
			temp.add(segment_text);
			cluster_groups.put(ind,temp);
		}
		br.close();
		System.out.println(linecounter+"\t"+counter);
		List<String> temp =  new ArrayList<String>();
		for (int ii=0;ii<clusters;ii++){
			temp = cluster_groups.get(ii);
			FileUtils.writeLines(new File(FilenameUtils.concat(basePath, ii+"_cluster.txt")), Constants.UTF8, temp, "\n");
		}
	}
}
