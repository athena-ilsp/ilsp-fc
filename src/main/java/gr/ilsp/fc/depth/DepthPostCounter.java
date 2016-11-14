package gr.ilsp.fc.depth;

import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class DepthPostCounter {

	private static final Logger LOGGER = Logger.getLogger(DepthPostCounter.class);
	private static DepthPostCounterOptions options = null;
	private static boolean merge;
	private static File input1, input2;
	private static final String PUNCT = ".";
	private static final String SPACE = " ";
	private static final String TAB = "\t";
	private static final String CSV = "csv";
	private static final String LOG = "log";
	private static final String SEP = "-";
	private static final String FC = "fc";
	private static final String RES = "res";
	private static final String cycle = "Starting cycle";
	private static final String discovered = " - PASSED\t";
	//private static final String fetched = " - PARSED URL:\t";
	private static final String stored = " - EXPORTED:\t";

	public static void main(String[] args) {
		DepthPostCounter dep = new DepthPostCounter();
		options = new DepthPostCounterOptions();
		options.parseOptions(args);
		dep.setInput1(options.getInput1());
		dep.toMerge(options.toMerge());
		dep.countDepth();
		dep.setInput2(options.getInput2());
		try {
			dep.getStats();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * parses a log file (its name should start with log) which contains the number of crawl cycle, the parsed URLs,
	 * the URLs extracted from each parsed URL(i.e. the discovered URLs) , the exported (stored) URLs
	 * writes a file (it ends in csv) in which each line corresponds to a stored URL. The line holds the host name, the lang, the depth, and the URL
	 * in case, the input file is a directory, it is applied in each logfile in this directory
	 * If merge is asked, it also merges the generated csv files       
	 * @param infile
	 * @param outfile
	 */
	private void countDepth() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		File csvfile;
		if (input1.isDirectory()){
			File[] logfiles = input1.listFiles();
			for (File logfile:logfiles){
				if (!logfile.getName().startsWith(LOG) || logfile.getName().endsWith(PUNCT+CSV))
					continue;
				csvfile = new File(FilenameUtils.concat(logfile.getParent(),timeStamp+SEP+FC+SEP+logfile.getName()+PUNCT+CSV));
				countCrawlDepth(logfile, csvfile);
			}
			if (merge){
				csvfile = new File(FilenameUtils.concat(input1.getParent(),timeStamp+SEP+FC+SEP+input1.getName()+PUNCT+CSV));
				mergeCSV(input1,csvfile);
			}
		}
		if (input1.isFile()){
			csvfile = new File(FilenameUtils.concat(input1.getParent(),timeStamp+SEP+FC+SEP+input1.getName()+PUNCT+CSV));
			countCrawlDepth(input1,csvfile);
		}
	}

	/**
	 * parses a log file (its name shopuld start with log) which contains the number of crawl cycle, the parsed URLs,
	 * the URLs extracted from each parsed URL(i.e. the discovered URLs) , the exported (stored) URLs
	 * writes a file (it ends in csv) in which each line corresponds to a stored URL. The line holds the host name, the lang, the depth, and the URL    
	 * @param infile
	 * @param outfile
	 */
	public void countCrawlDepth(File infile, File outfile) {
		Map<String, List<String>> depth = new HashMap<String,List<String>>();
		Map<String, String> url_lang = new HashMap<String,String>();
		List<String> res= new ArrayList<String>();
		try {
			List<String> lines = FileUtils.readLines(infile);
			List<String> urls = new ArrayList<String>();
			String cycle_key="";
			for (String line:lines){
				if (line.contains(stored)){
					String[] temp = line.split(TAB);
					String lang = temp[temp.length-1].split(SPACE)[0].trim();
					String link = temp[temp.length-2].trim();
					url_lang.put(link, lang);
				}
				if (line.contains(cycle)){
					String[] temp = line.split(SPACE);
					if (!cycle_key.isEmpty()){
						depth.put(cycle_key, urls);
						urls = new ArrayList<String>();
					}
					cycle_key = temp[temp.length-2].trim();
				}
				if (line.contains(discovered)){
					String temp = line.split(TAB)[1].split(SPACE)[0].trim();
					if (!urls.contains(temp))
						urls.add(temp);
				}	
			}
			Set<String> stored = url_lang.keySet();
			int cy=depth.size();
			String[] cycles = new String[cy];
			for (int ii=0;ii<cy;ii++){
				cycles[ii] = Integer.toString(ii+1);
			}

			Iterator<String> stored_it = stored.iterator();
			String key_st,  d, h, l; //key_cy,

			while (stored_it.hasNext()){
				d=""; h="";
				key_st = stored_it.next();
				l = url_lang.get(key_st);
				for (int ii=0;ii<cycles.length;ii++){
					if (depth.get(cycles[ii]).contains(key_st)){
						d=cycles[ii];
						break;
					}
				}
				if (d.isEmpty()){
					LOGGER.error("Something went wrong. It is not found when "+key_st+ " was discovered!");
					continue;
				}
				h = new URL(key_st).getHost();
				System.out.println(h+TAB+l+TAB+d+TAB+key_st);
				res.add(h+TAB+l+TAB+d+TAB+key_st);	
			}
		} catch (IOException e) {
			LOGGER.error("problem in reading "+infile.getAbsolutePath());
			e.printStackTrace();
		}
		try {
			FileUtils.writeLines(outfile, res);
		} catch (IOException e) {
			LOGGER.error("problem in writing "+outfile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public void mergeCSV(File input, File output) {
		File[] csvfiles = input.listFiles();
		List<String> res = new ArrayList<String>();
		for (File csvfile:csvfiles){
			if (!csvfile.getName().endsWith(PUNCT+CSV))
				continue;
			try {
				List<String> lines = FileUtils.readLines(csvfile);
				res.addAll(lines);
			} catch (IOException e) {
				LOGGER.error("problem in reading "+csvfile.getAbsolutePath());
				e.printStackTrace();
			}
		}
		try {
			FileUtils.writeLines(output, res);
		} catch (IOException e) {
			LOGGER.error("problem in writing "+output.getAbsolutePath());
			e.printStackTrace();
		}
	}


	private void setInput1(File infile) {
		DepthPostCounter.input1=infile;
	}
	private void setInput2(File infile) {
		DepthPostCounter.input2=infile;
	}
	
	private void toMerge(boolean merge) {
		DepthPostCounter.merge=merge;
	}


	public void getStats() throws IOException{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		File inn= input2;
		String langs = "af;ar;bg;bn;ca;cs;cy;da;de;el;en;es;et;eu;fa;fi;fr;ga;gl;gu;he;hi;hr;hu;id;it;ja;kn;ko;lt;lv;mk;ml;mr;mt;ne;nl;no;pa;pl;pt;ro;ru;sk;sl;so;sq;sv;sw;ta;te;th;tl;tr;uk;ur;vi;zh-cn;zh-tw";
		String[] ls = LangDetectUtils.updateLanguages(langs,true).split(";");
		Map<String, String> resu = new HashMap<String, String>();
		resu.put("site", "");		resu.put("depth", "");		resu.put("minlen", "");		for (int ii=0;ii<ls.length;ii++){resu.put(ls[ii], "");}
		File[] outputs= inn.listFiles();
		File resi = new File(FilenameUtils.concat(inn.getParent(), timeStamp+SEP+FC+SEP+inn.getName()+SEP+RES+PUNCT+CSV));

		for (File file:outputs){
			List<String> lines= FileUtils.readLines(file) ;
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
					String l = t[0].split(" ")[4];
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
		FileUtils.writeLines(resi, total);
	}

}







