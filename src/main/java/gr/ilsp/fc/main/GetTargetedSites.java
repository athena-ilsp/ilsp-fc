package gr.ilsp.fc.main;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


public class GetTargetedSites {
	private static final String curly_brackets_o="{";
	private static final String curly_brackets_c="}";
	private static final String KOMMA=",";
	private static final String QUEST=";";
	private static final String EQUALS="=";
	private static final String SEEDS="seeds";
	private static final String SEP="-";
	private static final String L=" -lang ";
	private static final String AGENT=" -a ";
	private static final String FILTER=" -filter ";
	private static final String U=" -u ";
	private static final String COMMANDS= "commands";
	private static final String QUOTE= "\"";
	private static final String FORW=" &> ";
	private static String destpath="\"/var/www/html/elrc4/culture/eng-fra/";
	
		
	private static final String JAR_ALL = "java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.3-jar-with-dependencies.jar "
			+ "-crawl -export -dedup -pairdetect -align -tmxmerge -f -k "
			+ "-type p -n 100 -t 20 -len 0 -mtlen 100 -p_r \"http://nlp.ilsp.gr/elrc\" -doctypes \"aupdih\" -segtypes \"1:1\"";

	private static final String JAR_CRAWL = "java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar "
			+ "-crawl -f -k -type p -n 100 -t 20 -len 0 -mtlen 100 ";
	
	private static final String JAR_EXPORT = "java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar "
			+ "-export "
			+ " -i \"XXX\" -lang \"L1;L2\" -of "+destpath+ "output_YYY.txt\"  -dom ZZZ  &>"+ destpath +"log-export_YYY\"";

	private static final String JAR_DEDUP = "java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar "
			+ "-dedup "
			+ " -o \"XXX\"  -lang \"L1;L2\"  -of "+destpath+ "output_YYY.txt\"  &>"+ destpath +"log-dedup_YYY\"";
		
	private static final String JAR_PAIR = "java -cp /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar gr.ilsp.fc.bitext.PairDetector -meth \"aupids\""
			+ " -i \"XXX\" -lang \"L1;L2\" -o \"XXX\" -of " + destpath + "output_YYY.txt\" &>"+destpath+"log-pairdetection_YYY\"";
		
	private static final String JAR_ALIGN = "java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar "
			+ "-align -oxslt "
			+ " -i \"XXX\" -lang \"L1;L2\" -o \"XXX\" -oft "+destpath+ "output_YYY.tmx.txt\" "
			+ " -ofth "+destpath+ "output_YYY.tmx.html\" &> "+destpath+ "log-align_YYY\"";
	
	private static final String JAR_TMXMERGE = "java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar "
			+ " -tmxmerge "
			+ " -xslt -oxslt -doctypes \"ZZZ\" -segtypes \"1:1\" -i \"XXX\" -lang \"L1;L2\" -o \"XXX\" -tmx  "+destpath+ "output_YYY.tmx\" "
			+ " &> "+destpath+ "log-tmxmerge_YYY\"";
	
	private static String lang1="";
	private static String lang2="";

	public static void main(String[] args)  {
		//getSeeds(args);
		//input4SeedsAndAllTasks(args);
		input4AllTasks(args);
		//input4Crawl(args);
		//input4Export(args);
		//input4Dedup(args);
		//input4PairDetection(args);
		//input4Align(args);
		//input4TmxMerge(args);
	}

	
	private static void input4Dedup(String[] args) {
		File indirsFile = new File(args[0]);
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(indirsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		for (String line:lines){
			String temp_command = JAR_DEDUP;
			temp_command=temp_command.replaceAll("XXX", line);
			temp_command=temp_command.replaceAll("L1", lang1);
			temp_command=temp_command.replaceAll("L2", lang2);
			String temp1 = new File(line).getParentFile().getParentFile().getName();
			temp1 = temp1.substring(0, temp1.indexOf("_20"));
			temp_command=temp_command.replaceAll("YYY", temp1);
			System.out.println(temp_command);
		}
		
	}


	private static void input4Export(String[] args) {
		File indirsFile = new File(args[0]);
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(indirsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		String domain = args[3];
		for (String line:lines){
			String temp_command = JAR_EXPORT;
			temp_command=temp_command.replaceAll("XXX", line.replaceAll("xml",""));
			temp_command=temp_command.replaceAll("L1", lang1);
			temp_command=temp_command.replaceAll("L2", lang2);
			String temp1 = new File(line).getParentFile().getParentFile().getName();
			temp1 = temp1.substring(0, temp1.indexOf("_20"));
			temp_command=temp_command.replaceAll("YYY", temp1);
			temp_command=temp_command.replaceAll("ZZZ", domain);
			System.out.println(temp_command);
		}
	}


	private static void input4TmxMerge(String[] args) {
		File indirsFile = new File(args[0]);
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(indirsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		String methods = args[3];
		for (String line:lines){
			String temp_command = JAR_TMXMERGE;
			temp_command=temp_command.replaceAll("XXX", line);
			temp_command=temp_command.replaceAll("L1", lang1);
			temp_command=temp_command.replaceAll("L2", lang2);
			String temp = new File(line).getParentFile().getParentFile().getName();
			temp = temp.substring(0, temp.indexOf("_20"));
			temp_command=temp_command.replaceAll("YYY", temp+"_"+methods);
			temp_command=temp_command.replaceAll("ZZZ", methods);
			System.out.println(temp_command);
		}
	}


	private static void input4Align(String[] args) {
		File indirsFile = new File(args[0]);
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(indirsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		for (String line:lines){
			String temp_command = JAR_ALIGN;
			temp_command=temp_command.replaceAll("XXX", line);
			temp_command=temp_command.replaceAll("L1", lang1);
			temp_command=temp_command.replaceAll("L2", lang2);
			String temp = new File(line).getParentFile().getParentFile().getName();
			temp = temp.substring(0, temp.indexOf("_20"));
			temp_command=temp_command.replaceAll("YYY", temp);
			System.out.println(temp_command);
		}
	}


	private static void input4PairDetection(String[] args) {
		File indirsFile = new File(args[0]);
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(indirsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		for (String line:lines){
			String temp_command = JAR_PAIR;
			temp_command=temp_command.replaceAll("XXX", line);
			temp_command=temp_command.replaceAll("L1", lang1);
			temp_command=temp_command.replaceAll("L2", lang2);
			String temp = new File(line).getParentFile().getParentFile().getName();
			temp = temp.substring(0, temp.indexOf("_20"));
			temp_command=temp_command.replaceAll("YYY", temp);
			System.out.println(temp_command);
		}
	}

	
	private static void input4AllTasks(String[] args) {
		File sitesFile =new File(args[0]);
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(sitesFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		File urlSeedFile = new File(FilenameUtils.concat(sitesFile.getParent(), lang1+SEP+lang2+SEP+SEEDS));
		File shellSiteFile = new File(FilenameUtils.concat(sitesFile.getParent(), lang1+SEP+lang2+SEP+COMMANDS));
				
		String commands = "";
		Set<String> hosts=new HashSet<String>();
		for (String line:lines){
			URL url;
			try {
				url = new URL(line);
				String host = url.getHost();
				if (!hosts.contains(host)){
					commands = commands+generateAllTasksCommand(host,urlSeedFile)+"\n";
					hosts.add(host);
				}
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//	System.out.println(sitet.site+"\t"+sitet.lang1+"\t"+sitet.l1pages+"\t"+sitet.lang2+"\t"+sitet.l2pages+"\t"+String.valueOf(sitet.factor));
		}
		WriteResources.writetextfile(shellSiteFile.getAbsolutePath(), commands);
		
	}

	private static String generateAllTasksCommand(String host, File urlSeedFile) {
		String t = host.replaceAll("\\.", "-");
		String t1 = host.replaceAll("\\.", "\\\\.");
		String agent = t+"_"+lang1+SEP+lang2;
		String filter = "\".*"+t1+".*\"";
		String dest = " -dest ";
		String dest1 = "/var/www/html/elrc1/culture/"+lang1+SEP+lang2+"/";
		String dest2 =  dest1+"output_"+agent;
		String command = JAR_ALL + L + QUOTE+lang1 + QUEST+ lang2 + QUOTE+ AGENT + agent +FILTER + filter + U +QUOTE+urlSeedFile.getAbsolutePath()+QUOTE +
				dest+QUOTE+dest1+QUOTE + " -of " + QUOTE+dest2+".txt"+QUOTE + FORW +QUOTE+dest1+ "log_"+ agent + QUOTE; 
						
		return command;
	}
	
	
	private static void getSeeds(String[] args){
		File candidateSitesFile =new File(args[0]);
		//String[] lines = ReadResources.readFileLines(candidateSitesFile.getAbsolutePath());		//"C:/Users/vpapa/ELRC/EUROPEANA/museums1.csv"
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(candidateSitesFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		File urlSeedFile = new File(FilenameUtils.concat(candidateSitesFile.getParent(), lang1+SEP+lang2+SEP+SEEDS));
		//File shellSiteFile = new File(FilenameUtils.concat(candidateSitesFile.getParent(), lang1+SEP+lang2+SEP+COMMANDS));
		List<Site> sites = new ArrayList<Site>();
		List<String> sitenames = new ArrayList<String>();
		String seedurls = "";
		for (String line:lines){
			if (line.contains(curly_brackets_o)){ //languages are in {}
				Site tempsite = parseLine2Site(line,sitenames);
				if (tempsite!=null){
					sitenames.add(tempsite.site);
					sites.add(tempsite);
					seedurls=seedurls+tempsite.site+"\n";
				}
			}
		}
		WriteResources.writetextfile(urlSeedFile.getAbsolutePath(), seedurls);
	}
	
	

	private static Site parseLine2Site(String line, List<String> sitenames) {
		line=line.substring(line.indexOf(KOMMA)+1);
		String site = line.substring(0, line.indexOf(KOMMA));
		if (sitenames.contains(site)){
			return null;
		}
		line=line.substring(line.indexOf(KOMMA)+1);
		String lang = line.substring(line.indexOf(curly_brackets_o)+1, line.indexOf(curly_brackets_c));
		if (!lang.contains(lang1) || !lang.contains(lang2)){
			return null;
		}
		String[] langs = lang.split(KOMMA);
		if (langs.length<2){
			return null;
		}
		String temp = line.substring(line.indexOf(lang1));
		String l1 = temp.substring(0, temp.indexOf(KOMMA));
		temp = line.substring(line.indexOf(lang2));
		String l2 = temp.substring(0, temp.indexOf(KOMMA));
		int ind = l1.indexOf(EQUALS);
		double l1pages = Double.parseDouble(removeNonDigits(l1.substring(ind)));
		l1 = l1.substring(0, ind);
		ind = l2.indexOf(EQUALS);	
		double l2pages = Double.parseDouble(removeNonDigits(l2.substring(ind)));
		l2 = l2.substring(0, ind);	
		double minp = Math.min(l1pages, l2pages);
		double factor = minp*minp/(l1pages+l2pages);
		//System.out.println(site+"\t"+String.valueOf(factor));
		return new Site(site,l1,l1pages,l2,l2pages,factor);
	}

	/**
	 * holds site, language1, language2, pages in language1, pages in language2, factor that indicates "wealth" of site
	 * @author 
	 *
	 */
	public static class Site {
		public double factor;
		public double l1pages;
		public double l2pages;
		public String site;
		public String lang1;
		public String lang2;

		public Site(String site, String lang1, double l1pages, String lang2, double l2pages, double factor) {
			this.site  = site;
			this.lang1 = lang1;
			this.lang2 = lang2;
			this.l1pages = l1pages;
			this.l2pages = l2pages;
			this.factor = factor;
		}
	}

	private static String removeNonDigits(String str) {
		str=str.replaceAll("[^\\p{N}]", " ").trim();
		return str;
	}

	private static class SiteComparator implements Comparator<Site> {
		public int compare(Site t1, Site t2) {
			return (int) ((t2.factor - t1.factor)*1000);
		}
	}

	
	private static void input4SeedsAndAllTasks(String[] args) {
		File candidateSitesFile =new File(args[0]);
		//String[] lines = ReadResources.readFileLines(candidateSitesFile.getAbsolutePath());		//"C:/Users/vpapa/ELRC/EUROPEANA/museums1.csv"
		List<String> lines=new ArrayList<String>();
		try {
			lines = FileUtils.readLines(candidateSitesFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lang1 = args[1];
		lang2 = args[2];
		File urlSeedFile = new File(FilenameUtils.concat(candidateSitesFile.getParent(), lang1+SEP+lang2+SEP+SEEDS));
		File shellSiteFile = new File(FilenameUtils.concat(candidateSitesFile.getParent(), lang1+SEP+lang2+SEP+COMMANDS));
		List<Site> sites = new ArrayList<Site>();
		List<String> sitenames = new ArrayList<String>();
		String seedurls = "";
		for (String line:lines){
			if (line.contains(curly_brackets_o)){ //languages are in {}
				Site tempsite = parseLine2Site(line,sitenames);
				if (tempsite!=null){
					sitenames.add(tempsite.site);
					sites.add(tempsite);
					seedurls=seedurls+tempsite.site+"\n";
				}
			}
		}
		WriteResources.writetextfile(urlSeedFile.getAbsolutePath(), seedurls);
				
		Collections.sort(sites, new SiteComparator());
		String commands = "";
		for (Site sitet:sites){
			URL url;
			try {
				url = new URL(sitet.site);
				String host = url.getHost();
				commands = commands+generateAllTasksCommand(host,urlSeedFile)+"\n";
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//	System.out.println(sitet.site+"\t"+sitet.lang1+"\t"+sitet.l1pages+"\t"+sitet.lang2+"\t"+sitet.l2pages+"\t"+String.valueOf(sitet.factor));
		}
		WriteResources.writetextfile(shellSiteFile.getAbsolutePath(), commands);
		
	}
	
	
}
