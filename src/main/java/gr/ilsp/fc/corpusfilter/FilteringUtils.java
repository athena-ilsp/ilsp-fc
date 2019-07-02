package gr.ilsp.fc.corpusfilter;

import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteringUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Filtering.class);

	
	
	/*				
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/my-corpus5_100m") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	System.exit(0);

	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/test2018_newest.out.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/newstest2017-deen-src.de.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/newstest2018-deen-src.de.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/newstest2017-deen-ref.en.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/newstest2018-deen-ref.en.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/test2017.out.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/wmt18_10/test2018.out.sgm") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/submitted/my-corpus9_100m") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	FilteringUtils.checkWords(new File("C:/Users/vpapa/ELRC/wmt18/in/data") , new File("C:/Users/vpapa/ELRC/wmt18/strange_words/stelios_wordlist.txt") );
	System.exit(0); */

	
	

	/**
	 * maps old scores (i.e. label of clusters) to new scales plus the en length 
	 * @param datafile : the data file, to get en part 
	 * @param scores	: the label of cluster
	 * @param datafilesnewcores : the file in which the new scores will be stored
	 * @throws IOException
	 */
	public static void getNewScoresLen(File datafile, List<Double> scores, File datafilesnewcores) throws IOException {
		//BufferedWriter out = new BufferedWriter(new FileWriter(datafilesnewcores));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datafilesnewcores.getAbsolutePath()),Constants.UTF8));
		
		BufferedReader in;
		String inputLine, temp;
		int linecounter = -1;
		int limit = scores.size()-1;
		try {
			in = new BufferedReader(new FileReader(datafile));
			int score = 0;
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter>limit){
					out.write("0\n");
					continue;
				}
				temp = inputLine.trim();
				String[] tt = temp.split("\t");
				if (scores.get(linecounter)==0.0){
					out.write("0\n");
					continue;
				}

				if (scores.get(linecounter)==3.0){
					score = 15000+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.4){
					score = 12500+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.3){
					score = 10000+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.2){
					score = 7500+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.19){
					score = 5000+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.1){
					score = 2500+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.0){
					score = 1000+tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==1.0){
					score = tt[0].length();
					out.write(Integer.toString(score)+"\n");
					continue;
				}
				System.out.println(linecounter+"\t:\t"+ scores.get(linecounter));
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void corrections(File infile,  File outfile) throws IOException {
		//BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));
		
		BufferedReader in;
		String inputLine, temp;
		int ind = 0;
		try {
			in = new BufferedReader(new FileReader(infile));
			double score = 0;
			while ((inputLine = in.readLine()) != null) {
				ind = Integer.parseInt(inputLine);
				ind=ind-1;
				out.write(Integer.toString(ind)+"\n");
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	/**
	 * maps old scores (i.e. label of clusters) to new scales plus the hunalign socre
	 * @param datafile : the data file, to get hunalign score
	 * @param scores	: the label of cluster
	 * @param datafilesnewcores : the file in which the new scores will be stored
	 * @throws IOException
	 */

	public static void getNewScoresHun(File datafile, List<Double> scores, File datafilesnewcores) throws IOException {
		//BufferedWriter out = new BufferedWriter(new FileWriter(datafilesnewcores));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datafilesnewcores.getAbsolutePath()),Constants.UTF8));
		BufferedReader in;
		String inputLine, temp;
		int linecounter = -1;
		int limit = scores.size()-1;
		double oldscore=0;
		int counter = 0;
		try {
			in = new BufferedReader(new FileReader(datafile));
			double score = 0;
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter>limit){
					out.write("0\n");
					continue;
				}
				oldscore = scores.get(linecounter);

				if (oldscore==0.0){
					out.write("0\n");
					continue;
				}
				temp = inputLine.trim();
				String[] tt = temp.split("\t");
				if (oldscore==1.0){
					score = tt[0].length()/3;
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (oldscore==2.4){
					score = 12500+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (oldscore==2.0){
					score = 1000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (oldscore==3.0){
					counter++;
					score = 15000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (oldscore==2.3){
					score = 10000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				/*if (oldscore==2.2){
					score = 7500+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}*/
				if (oldscore==2.19){
					score = 5000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (oldscore==2.1){
					score = 2500+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}

				System.out.println(linecounter+"\t:\t"+ oldscore);
			}
			LOGGER.info("sens with 3.0 are " + counter);
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	//get inds from indeces file
	public static 	List<Integer> getIndsFromFile(File file){
		List<Integer> inds = new ArrayList<Integer>();
		String inputLine;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.isEmpty())
					continue;
				inds.add(Integer.parseInt(inputLine.trim()));
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inds;
	}


	public static 	File applyMosesRule(File infile, int len, int mult, File outfile){
		LOGGER.info("#########################################");
		LOGGER.info("getting scores by applying Moses' rules ");
		LOGGER.info("#########################################");
		//List<Integer> inds = new ArrayList<Integer>();
		String inputLine;
		BufferedReader in;
		int linecounter=-1;
		int lenen, lende;
		//double score;
		try {
			in = new BufferedReader(new FileReader(infile));
			//BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));
			
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				lenen = tt[0].split(" ").length;
				lende = tt[1].split(" ").length;
				if (lenen>len || lende>len){
					LOGGER.info("cutMoses\t"+linecounter+"\t"+ inputLine);
					out.write("0\n");
					continue;
				}
				if (lenen> mult*lende || lende>mult*lenen){
					LOGGER.info("cutMoses\t"+linecounter+"\t"+ inputLine);
					out.write("0\n");
					continue;
				}
				out.write("1\n");
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outfile;
	}


	public static List<Double> getScores(File datafilescores) {
		BufferedReader in;
		List<Double> scores = new ArrayList<Double>();
		String inputLine, temp;
		double score = 0;
		int freq = 0;
		Map<Double, Integer> scoresfreqs = new HashMap<Double, Integer>();
		try {
			in = new BufferedReader(new FileReader(datafilescores));
			while ((inputLine = in.readLine()) != null) {
				temp = inputLine.trim();
				score = Double.parseDouble(temp);
				scores.add(score);
				if (scoresfreqs.containsKey(score))
					freq = scoresfreqs.get(score)+1;
				else
					freq = 1;
				scoresfreqs.put(score, freq);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<Double> keys=scoresfreqs.keySet();
		Iterator<Double> it = keys.iterator();
		double key;
		while (it.hasNext()){
			key = it.next();
			LOGGER.info("score:"+key+"\t"+ scoresfreqs.get(key));
		}		
		return scores;
	}


	//get inds of specific score from indeces file
	public static 	List<Integer> getIndsFromScore(File file, double thr){
		LOGGER.info("#########################################");
		LOGGER.info("getting lines with score more than "+ thr);
		LOGGER.info("#########################################");
		List<Integer> inds = new ArrayList<Integer>();
		String inputLine;
		BufferedReader in;
		int linecounter=-1;
		double score;
		try {
			in = new BufferedReader(new FileReader(file));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				score = Double.parseDouble(inputLine.trim());
				if (score>=thr){
					inds.add(linecounter);
					//LOGGER.info(linecounter+"\tmore than 15000\t"+inputLine);
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("sens with score more than "+thr+ " are "+ inds.size());
		return inds;
	}



	public static File mergeFilts(File filetok, File filerow, File filecorrecthand, File filewronghand, File filemerge) throws IOException {
		BufferedReader in;
		List<Double> tokscores = new ArrayList<Double>();
		String inputLine;
		int linecounter = -1, cut=0, cutall=0;
		double filtscore=0, newscore, tokscore;
		//get tokscores
		try {
			in = new BufferedReader(new FileReader(filetok));
			while ((inputLine = in.readLine()) != null) {
				tokscores.add(Double.parseDouble(inputLine.trim()));
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		//get correct inds
		List<Integer> correctinds = FilteringUtils.getIndsFromFile(filecorrecthand);
		//get wrong inds
		List<Integer> wronginds =  FilteringUtils.getIndsFromFile(filewronghand);
		//merge
		//BufferedWriter out = new BufferedWriter(new FileWriter(filemerge));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filemerge.getAbsolutePath()),Constants.UTF8));
		linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(filerow));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (wronginds.contains(linecounter)){
					out.write("0\n");
					continue;
				}
				if (correctinds.contains(linecounter)){
					out.write("15000\n");
					continue;
				}
				filtscore = Double.parseDouble(inputLine.trim());
				if (filtscore==0){
					out.write(inputLine+"\n");
					continue;
				}
				tokscore = tokscores.get(linecounter);
				newscore = tokscore*filtscore;
				out.write(Double.toString(newscore)+"\n");		//out.write(Double.toString(filtscore)+"\n");
				if (filtscore>0 && tokscore==0){
					cutall++;
					if (filtscore>14999)
						cut++;
					LOGGER.info(linecounter+"\tMoses cuts \t"+ inputLine);
				}
			}
			in.close();	
			out.close();
			System.out.println("Total cut:\t"+cutall);
			System.out.println("1st Rank cut:\t"+cut);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return filemerge;
	}




	private static void filterMosesRules(File datatokfile) throws IOException {
		BufferedReader in;
		//BufferedWriter out = new BufferedWriter(new FileWriter(new File(datatokfile.getAbsolutePath()+".scores")));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datatokfile.getAbsolutePath()+".scores"),Constants.UTF8));
		String inputLine="", en, de;
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(datatokfile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();	
				de = tt[1].trim();	
				int enlen= FCStringUtils.countTokens(en);
				if (enlen>80){
					out.write("0\n");
					LOGGER.info(linecounter+"\tMosesLen80:\t"+inputLine);
					continue;
				}
				int delen = FCStringUtils.countTokens(de);  
				if (delen>80){
					out.write("0\n");
					LOGGER.info(linecounter+"\tMosesLen80:\t"+inputLine);
					continue;
				}
				if (delen>9*enlen || enlen>9*delen){
					out.write("0\n");
					LOGGER.info(linecounter+"\tMosesRatio9:\t"+inputLine);
					continue;
				}
				out.write("1\n");
			}	
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static List<Double> getFromScore(File filemerge, int thrlow, int thrhigh) {
		LOGGER.info("#########################################");
		LOGGER.info("getting lines with score more than "+ thrlow+ "less than "+ thrhigh);
		LOGGER.info("#########################################");
		List<Double> inds = new ArrayList<Double>();
		String inputLine;
		BufferedReader in;
		int linecounter=-1;
		double score;
		int counter=0;
		try {
			in = new BufferedReader(new FileReader(filemerge));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				score = Double.parseDouble(inputLine.trim());
				if (score>=thrlow && score<thrhigh){
					inds.add(1.0);
					counter++;
					LOGGER.info(linecounter+"\tmore than "+ thrlow +"\tless than "+thrhigh+"\t"+inputLine);
				}else
					inds.add(0.0);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("sens with score more than "+thrlow+ "and less than "+ thrhigh +" are "+ counter);
		return inds;
	}




	public static void checkWords(File filedata, File filewords) throws IOException {
		String[] words = FileUtils.readFileToString(filewords, Constants.UTF8).split(",");
		BufferedWriter out=null;
		for (int ii=0;ii<words.length;ii++){
			words[ii] = " "+ContentNormalizer.normtext(words[ii])+" ";
		}
		String inputLine, line;
		BufferedReader in;
		int linecounter=0;
		try {
			in = new BufferedReader(new FileReader(filedata));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				line = " "+ContentNormalizer.normtext(inputLine.trim())+" ";
				for (int ii=0;ii<words.length;ii++){
					if (line.contains(words[ii])){
						out = new BufferedWriter(new FileWriter(new File(FilenameUtils.concat(filewords.getParent(),words[ii].trim()+"."+filedata.getName())), true));
						//String aa = FilenameUtils.concat(filewords.getParent(),words[ii].trim()+"."+filedata.getName());
						//out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aa),Constants.UTF8));
						out.append(linecounter+"\t"+words[ii] +"\t:\t" + inputLine+"\n");
						out.close();
					}
				}
			}
			in.close();	
			if (out!=null)
				out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
