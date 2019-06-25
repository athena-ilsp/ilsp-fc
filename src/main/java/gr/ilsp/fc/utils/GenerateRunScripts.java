package gr.ilsp.fc.utils;

import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class GenerateRunScripts {
	private static final Logger LOGGER = Logger.getLogger(GenerateRunScripts.class);
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String LineSeparator = "\n";
	private static final String win = "win";
	private static final String command_winfile = "command_win.txt";
	private static final String linux = "linux";
	private static final String command_linuxfile = "command_linux.txt";
	private static final String s1 = "sitename1";
	private static final String s2 = "sitename2";
	private static final String pre = "#!/bin/bash\n";  
	private static final String linext = ".sh";
	private static final String winext = ".bat";
	
	public static void main(String[] args) {
		String command = readCommand(args);
		if (command.isEmpty()){
			System.exit(0);
		}
		String os = args[0];
		String outfile = args[2];
		File sitelist1  = new File(args[1]); //file with list of websites to be crawled
		List<String> sites1;
		try {
			sites1 = FileUtils.readLines(sitelist1, Constants.UTF8);
			sites1 = getUniqueSites(sites1);
			String commands = "";
			for (int ii=0;ii<sites1.size();ii++){
				
				if (skipLineM.reset(sites1.get(ii)).matches()) 
					continue;
				
				String temp1=sites1.get(ii).replaceAll("\\.", "-");
				String temp2=sites1.get(ii).replaceAll("\\.", "&&&&");
				String temp=command.replaceAll(s1, temp1);
				temp = temp.replaceAll(s2, temp2);
				//System.out.println(temp);
				commands=commands+temp+"\n";
			}
			if (os.equals(linux)){
				commands = pre+commands;
				if (!outfile.endsWith(linext)){
					outfile = outfile+linext;
				}
			}else{
				if (!outfile.endsWith(winext)){
					outfile = outfile+winext;
				}
			}
			//WriteResources.writetextfile(outfile, commands);
			FileUtils.writeStringToFile(new File(outfile), commands, Constants.UTF8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private static List<String> getUniqueSites(List<String> sites1) {
		Set<String> setsites = new HashSet<String>();
		for (int ii=0;ii<sites1.size();ii++){
			if (!setsites.contains(sites1.get(ii))){
				setsites.add(sites1.get(ii));
			}else{
				System.out.println(sites1.get(ii));
			}
		}
		List<String> uniquesites = new ArrayList<String>();
		Iterator<String> site = setsites.iterator();
		while (site.hasNext()){
			uniquesites.add(site.next());
		}
		return uniquesites;
	}
	

	/**
	 * Reads the pattern for the command to run the ILSP-FC. If no pattern is provided by the user, default patterns (in resources) are used
	 * @param args
	 * @return
	 */

	private static String readCommand(String[] args) {
		String command="";
		if (args.length>3){
			File commandfile = new File(args[3]);
			try {
				command = FileUtils.readFileToString(commandfile, Constants.UTF8).split(LineSeparator)[0];
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			if (args[0].equals(win)){
				try {
					command = readRunCrawlCommand(command_winfile);
				} catch (IOException e) {
					LOGGER.error("Problem in reading Resource "+command_winfile);
					e.printStackTrace();
				}
			}else{
				if (args[0].equals(linux)){
					try {
						command = readRunCrawlCommand(command_linuxfile);
					} catch (IOException e) {
						LOGGER.error("Problem in reading Resource "+command_linuxfile);
						e.printStackTrace();
					}
				}else{
					LOGGER.info("First argument should be \"win\" or \"linux\" ");
				}
			}
		}
		return command;
	}

	private static String readRunCrawlCommand(String resourcefile) throws IOException {
		String res="";
		URL svURL = ReadResources.class.getClassLoader().getResource(resourcefile);
		BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			res=inputLine;
			continue;
		}
		in.close();
		return res;
	}
}
