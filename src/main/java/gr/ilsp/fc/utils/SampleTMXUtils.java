package gr.ilsp.fc.utils;

import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Seg;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;
import net.loomchild.maligna.util.bind.tmx.Tuv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SampleTMXUtils {
	private static final Logger LOGGER = Logger.getLogger(SampleTMXUtils.class);
	//private static final String appXMLext = ".xml";
	//private static final String appHTMLext = ".html";
	//private static final String appXMLHTMLext = ".xml.html";
	//private static final String type_p ="p";
	//private static final String SITE = "site";
	private final static String SCORE = "score";
	private final static String INFO = "info";
	private static final String SPACE_SEPARATOR = " ";

	enum Sort { ASCENDING, DESCENDING; }



	public static void main(String[] args) throws IOException {
		File tmxfile = new File(args[0]);
		String[] temp= args[1].split(";");
		List<String> info = new ArrayList<String>();
		if (!temp[0].equals("\"\"")){
			for (String t:temp)
				info.add(t);
		}
		int samplesize = Integer.parseInt(args[2]);
		String lang1 = args[3];
		String lang2 = args[4];
		//String type = args[5];
		List<SegPair> segpairs = getTUsFromTMX(tmxfile,info, lang1, lang2);
		Set<SegPair> selpairs = Statistics.distinctRandomIntegers(segpairs, samplesize);
		//if (type.contains("txt")){
		String pairs="";
		File txtfile = new File(tmxfile.getAbsolutePath()+".txt");
		for (SegPair sg:selpairs)
			pairs = pairs+sg.seg1+"\t"+ sg.seg2+"\n";
		FileUtils.writeStringToFile(txtfile, pairs);
	}

	private static List<SegPair> getTUsFromTMX(File tmxfile, List<String> info, String lang1, String lang2) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxfile));
			//TmxInfo tmxinfo = TMXHandlerUtils.getInfo(tmxfile);
			List<Tu> tus = tmx.getBody().getTu();
			boolean found = false;
			for (Tu tu: tus) {
				List<String> segmentList1 = new ArrayList<String>();
				List<String> segmentList2 = new ArrayList<String>();
				List<Object> tuProps = tu.getNoteOrProp();
				String type="", localinfo="";
				double score = 0;
				found=false;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(SCORE)) 
						score = Double.parseDouble(prop.getContent().get(0));
					if (prop.getType().equals(INFO)){
						if (prop.getContent().isEmpty())
							localinfo="";
						else
							localinfo = prop.getContent().get(0);
						if (info.contains(localinfo) || (info.isEmpty() && localinfo.isEmpty()))
							found = true;
					}
				}
				if (!found)
					continue;
				for (Tuv tuv : tu.getTuv()) {
					if (lang1.equals(tuv.getLang())) {
						String segment = getSegment(tuv.getSeg());
						segmentList1.add(segment);
					}
					if (lang2.equals(tuv.getLang())) {
						String segment = getSegment(tuv.getSeg());
						segmentList2.add(segment);
					}
				}

				segpairs.add(new SegPair(StringUtils.join(segmentList1, SPACE_SEPARATOR), 
						StringUtils.join(segmentList2, SPACE_SEPARATOR),
						score, type, "", "", "", "", ""));
			}
			LOGGER.debug("Examining " + tmxfile.getAbsolutePath() + SPACE_SEPARATOR + tus.size());

		} catch (FileNotFoundException e) {
			LOGGER.warn("Problem in reading "+ tmxfile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}



	private static String getSegment(Seg seg) {
		StringBuilder builder = new StringBuilder();
		for (Object object : seg.getContent()) {
			builder.append(object.toString());
		}
		return builder.toString();
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


	private static void sample_selection(String tmx_listfile) {
		String[] types =new String[ ] {"u","i","h", "l", "m", "pdf"};
		int[] tmxfile_counts = new int[types.length], selected_tmxfile_counts = new int[types.length];
		int[] tmx_counts = new int[types.length], selected_tmx_counts = new int[types.length];
		boolean found=false;
		int total_tmx=0, totaltmxfile=0; 
		double sample_factor = 0.05, sample_factor1 = 0.05; 
		try {
			List<String> tmx_files=FileUtils.readLines(new File(tmx_listfile));
			for (int ii=0;ii<tmx_files.size();ii++){
				if (!tmx_files.get(ii).startsWith("align"))
					continue;

				String[] temp = tmx_files.get(ii).split(" ");
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

	private static void print_unique_sites_in_collection(String list_with_attricutes_of_files) {

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

}
