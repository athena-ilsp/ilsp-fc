package gr.ilsp.fc.corpusfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.io.MD5Hash;
//import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.dedup.DedupUtils;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitter;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;
import gr.ilsp.nlp.commons.Constants;

public class RunFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunFilter.class);
	//private static final String[] exclude_symbols={"{", "}", "[", "]","--", "ÿ", "Î", "Ï", "Å", "Â", "Ã", "Ё", "Ð", "☭", "☁", "✚",
	//	"¿", "î", "æ", "û", "ï","è", "ç","ò", "ð","å","à","ë","í","ñ","ý","ã","î", "åã", "åå" };	 
	private static final String[] exclude_symbols={"{", "}", "--", "ÿ", "Î", "Ï", "Å", "Â", "Ã", "Ё", "Ð", "☭", "☁", "✚",
		"¿", "î", "æ", "û", "ï", "ç","ò", "ð","å","à","ë","í","ñ","ý","ã","î", "åã", "åå" };	 

	private static final String[] exclude_grams ={"Çá", "ãÚ", "áÍÙÇÊ", "äÚã", "ãÜÇÐÇ", "ãÚ", "ÇÓ", "ãæÇÞ"};
	//û í ñ å ã

	private static final String[] connect={"janfeb", "febmar", "marapr", "aprmay","mayjun", "junjul", "julaug", "augsep", "sepoct", "sepokt", "octnov", "oktnov","novdec", "novdez", "decjan",
		"dezjan", "januaryfebruary", "marchapril", "aprilma", "julyaug", "septemberoct", "septemberokt", "octoberno", "oktoberno", "novemberde", "decemberja", "märzap", "junij", "juliau",
		"augustse", "junej"};

	private static List<Integer> getInd(File indfile1) {
		BufferedReader in;
		String inputLine, temp;
		List<Integer> ind = new ArrayList<Integer>();
		try {
			in = new BufferedReader(new FileReader(indfile1));
			while ((inputLine = in.readLine()) != null) {
				temp = inputLine.trim();
				ind.add(Integer.parseInt(temp));
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ind;
	}	

	private static List<String> checkInd(List<Integer> ind1, List<Integer> ind2, File filedata) {
		BufferedReader in;
		String inputLine, temp;
		int linecounter = -1;
		List<String> ind = new ArrayList<String>();
		int counter=0;
		try {
			in = new BufferedReader(new FileReader(new File("C:/Users/vpapa/ELRC/wmt18/data")));
			String[] tt = new String[3];
			String en, de, num1, num2;
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (ind1.contains(linecounter) || ind2.contains(linecounter)){
					temp = inputLine.trim();
					tt=temp.split("\t");
					en = tt[0].trim();				//nen = ContentNormalizer.normtext(en);
					de = tt[1].trim();				//nde = ContentNormalizer.normtext(de);
					num1=en.replaceAll("\\D+","");
					num2=de.replaceAll("\\D+","");
					if (num1.equals(num2)){
						if (!num1.isEmpty()){
							ind.add(Integer.toString(linecounter));
							LOGGER.info(linecounter+"\t"+inputLine);
							counter++;
						}
					}
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(counter);
		return ind;
	}

	public static void main(String[] args) throws Exception{  

		/*		File filedata = new File(("C:/Users/vpapa/ELRC/wmt18/data"));
		File indfile1 = new File(("C:/Users/vpapa/ELRC/wmt18/less2more.ind.num"));
		File indfile2 = new File(("C:/Users/vpapa/ELRC/wmt18/more2less.ind.num"));
		File indfile =  new File(("C:/Users/vpapa/ELRC/wmt18/inds"));
		List<Integer> ind1 = getInd(indfile1);
		List<Integer> ind2 = getInd(indfile2);
		List<String> indcorrect = checkInd(ind1, ind2, filedata);

		FileUtils.writeLines(indfile, Constants.UTF8, indcorrect,"\n");
		System.exit(0);




		BufferedReader in;
		String inputLine, temp;
		int linecounter = -1;
		int counter=0;
		try {
			in = new BufferedReader(new FileReader(new File("C:/Users/vpapa/ELRC/wmt18/data")));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter>59800000){
					temp = inputLine.trim();
					String[] tt = temp.split("\t");
					//if (temp.contains("ÿÿÿÿÿÿÿÿÿÿÿÿ")){
					//	System.out.println(linecounter+":\t"+tt[0].length()+"\t"+inputLine);
					//	counter++;
					//}
					if (temp.startsWith("ð")){
						//LOGGER.info(linecounter+":\t"+tt[0].length()+"\t"+inputLine);
						counter++;
					}
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(counter);
		System.exit(0);





		File datafile = new File(("C:/Users/vpapa/ELRC/wmt18/data"));
		File datafilescores = new File(("C:/Users/vpapa/ELRC/wmt18/data104m.row.filt.scores"));
		File datafilesnewcores = new File(("C:/Users/vpapa/ELRC/wmt18/data104m.row.filt.scores.hun"));

		List<Double> scores = getScores(datafilescores);
		//addZeroScores(datafile, scores, datafilesnewcores);
		getNewScoresHun(datafile, scores, datafilesnewcores);
		//getNewScores(datafile, scores, datafilesnewcores);

		System.exit(0);

		System.out.println("START");
		if (args.length==3){

			File datafile = new File(args[0]);
			String inputLine;
			BufferedReader in;
			Set<String> chars = new HashSet<String>();
			int linecounter=-1;
			try {
				in = new BufferedReader(new FileReader(datafile));
				String a = "", temp="";
				while ((inputLine = in.readLine()) != null) {
					linecounter++;
					temp= inputLine.toLowerCase();
					for (int ii=0;ii<temp.length();ii++){
						 a = Character.toString(temp.charAt(ii));
						 if (a.matches(".*[\\u0020-\\u007F].*") || a.equals("ä") || a.equals("ö") || a.equals("ü") || a.equals("ß"))  
							 continue;

						if (!chars.contains(a)){
							chars.add(a);
							System.out.println(a);
						}
					}
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] ch = new String[chars.size()];
			Iterator<String> it =chars.iterator();
			int jj=0;
			while (it.hasNext()){
				ch[jj]=it.next();
				jj++;
			}
			List<String> allchars = new ArrayList<String>();
			Arrays.sort(ch);
			for (int kk=0;kk<ch.length;kk++){
				allchars.add(ch[kk]);
			}
			FileUtils.writeLines(new File(args[0]+".chars"), Constants.UTF8, allchars,"\n");
			System.exit(0);

			//clusterbysite(args);




			//LOGGER.info("------------scoring by filtering------------");
			scoring_filter(args);
			System.exit(0);

			//LOGGER.info("------------REscoring by filtering------------");
			//checkmonths1(args);
			rescoring(args);
			System.exit(0);

			//LOGGER.info("------------normalizing------------");
			//normalizing(args);
			//System.exit(0);

			//LOGGER.info("------------scoring by deduplicating------------");
			//scoring_dedup(args);
			//scoring_dedup1(args);
			//System.exit(0);
		}
		System.exit(0);*/


		LOGGER.info("\nargs:\n 0=fullpath of infile\n 1=languages separated by ;\n 2=minTUVlen\n 3=maxTUVlen\n 4=minTUVratio\n 5=maxTUVratio\n 6=version");
		File infile = new File(args[0]); 
		String[] langs = args[1].split(Constants.SEMICOLON);
		langs[0]=ISOLangCodes.get3LetterCode(langs[0]);
		langs[1]=ISOLangCodes.get3LetterCode(langs[1]);	
		int minTuvLen = Integer.parseInt(args[2]);
		int maxTuvLen = Integer.parseInt(args[3]);
		float minratio = Float.parseFloat(args[4]);
		float maxratio = Float.parseFloat(args[5]);
		String ext = ".filt."+args[2]+"minlen_"+args[3]+"maxlen"+args[4]+"minra_"+args[5]+"maxra_"+"dupTUV_"+"incoh";
		String version = args[6];
		//STEP 1.
		new File(infile.getAbsolutePath()+ext+"."+version);
		List<File> outs = filterCorpus(infile, langs, minTuvLen, maxTuvLen, minratio, maxratio, ext, version);
		System.exit(0);

		//STEP 2.
		File outdir = clusterbylength(outs.get(0), 2);
		//System.exit(0);

		//STEP 3
		File outfile = dedupWMT18_TUV(outdir, ".dedup", 2 , 6);
		//File outfile = dedupWMT18(outs.get(0), ".dedup", 2 );
		System.exit(0);

		//STEP 3
		buildcorpus(args);
		System.exit(0);

		//subsetWMT18(args);
		//System.exit(0);

		//test_enpl_paracrawl();
		//System.exit(0);
	}

	private static void getNewScoresHun(File datafile, List<Double> scores, File datafilesnewcores) throws IOException {
		//BufferedWriter out = new BufferedWriter(new FileWriter(datafilesnewcores));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datafilesnewcores.getAbsolutePath()),Constants.UTF8));

		BufferedReader in;
		String inputLine, temp;
		int linecounter = -1;
		int limit = scores.size()-1;
		try {
			in = new BufferedReader(new FileReader(datafile));
			double score = 0;
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
					score = 15000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.4){
					score = 12500+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.3){
					score = 10000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.2){
					score = 7500+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.19){
					score = 5000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.1){
					score = 2500+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==2.0){
					score = 1000+Double.parseDouble(tt[2]);
					out.write(Double.toString(score)+"\n");
					continue;
				}
				if (scores.get(linecounter)==1.0){
					score = tt[0].length()/3;
					out.write(Double.toString(score)+"\n");
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

	private static void getNewScores(File datafile, List<Double> scores, File datafilesnewcores) throws IOException {
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

	private static void addZeroScores(File datafile, List<Double> scores, File datafilesnewcores) throws IOException {
		//BufferedWriter out = new BufferedWriter(new FileWriter(datafilesnewcores));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datafilesnewcores.getAbsolutePath()),Constants.UTF8));
		BufferedReader in;
		String inputLine;//, temp;
		int linecounter = -1;
		int limit = scores.size()-1;
		try {
			in = new BufferedReader(new FileReader(datafile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (linecounter>limit){
					out.write("0\n");
					continue;
				}
				//temp = inputLine.trim();
				out.write(Double.toString(scores.get(linecounter))+"\n");
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static List<Double> getScores(File datafilescores) {
		BufferedReader in;
		List<Double> scores = new ArrayList<Double>();
		String inputLine, temp;
		int linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(datafilescores));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				scores.add(Double.parseDouble(temp));
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scores;
	}

	private static Set<String> getVoc(File filedata, File filemerge, int col, String score, Set<String> voc) {
		List<Integer> indeces = new ArrayList<Integer>();
		String inputLine, temp;
		int linecounter = -1;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(filemerge));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals(score))
					indeces.add(linecounter);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		int wordcounter = 0;
		String ntemp;
		try {
			in = new BufferedReader(new FileReader(filedata));
			List<String> toks = new ArrayList<String>();
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (indeces.contains(linecounter)){
					temp = inputLine.split("\t")[col];
					ntemp = ContentNormalizer.normtext(temp);
					toks = FCStringUtils.getTokens(ntemp);
					wordcounter = wordcounter+ toks.size();
					for (String tok:toks){
						if (voc.contains(tok))
							continue;
						voc.add(tok);
					}
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("# of words in sentences with score "+ score + " = "+ wordcounter);
		System.out.println("# of unique words in sentences with score "+ score + " = "+ voc.size());
		return voc;
	}

	private static void getSubCorpus(File filedata, File filetok, File filerow,		File filemerge, File filesubcorpus) throws IOException {

		//File filemerge = new File("C:/tmp/merge");
		mergeFilts( filetok,  filerow, filemerge);
		BufferedReader in;
		List<Integer> indeces = new ArrayList<Integer>();
		String inputLine, temp;
		int linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(filemerge));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals("3"))
					indeces.add(linecounter);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		//BufferedWriter out = new BufferedWriter(new FileWriter(filesubcorpus));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filesubcorpus.getAbsolutePath()),Constants.UTF8));
		try {
			in = new BufferedReader(new FileReader(filedata));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (linecounter>indeces.size())
					break;
				if (indeces.contains(linecounter))
					out.write(inputLine+"\n");
			}
			in.close();	
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static File mergeFilts(File filetok, File filerow, File filemerge) throws IOException {
		BufferedReader in;
		List<Integer> indeces = new ArrayList<Integer>();
		String inputLine, temp;
		int linecounter = -1, cut=0;
		//BufferedWriter out = new BufferedWriter(new FileWriter(filemerge));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filemerge.getAbsolutePath()),Constants.UTF8));
		try {
			in = new BufferedReader(new FileReader(filetok));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals("0"))
					indeces.add(linecounter);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("moses:\t"+indeces.size());
		linecounter = -1;
		try {
			in = new BufferedReader(new FileReader(filerow));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals("0") || temp.equals("1") || temp.contains("2")){
					out.write(temp+"\n");
					continue;
				}
				/*if (temp.contains("2")){
					if (indeces.contains(linecounter)){
						out.write("0\n");
						LOGGER.info(linecounter+"\tMoses cuts \t"+ inputLine);
						continue;
					}
					out.write(temp+"\n");
					continue;
				}*/
				if (temp.contains("3")){
					if (indeces.contains(linecounter)){
						out.write("0\n");
						LOGGER.info(linecounter+"\tMoses cuts \t"+ inputLine);
						cut++;
						continue;
					}
					out.write("3\n");
					continue;
				}
				/*if (!temp.equals("3") && !temp.equals("2.1") ){
					out.write(temp+"\n");
					continue;
				}
				if (indeces.contains(linecounter)){
					out.write("0\n");
					//indeces.remove(linecounter);
					if (!temp.equals("0")){
						LOGGER.info(linecounter+"\tcut due to Moses\t"+ inputLine);
						cut++;
					}
				}else{
					out.write(temp+"\n");
				}*/
			}
			in.close();	
			out.close();
			System.out.println("cut:\t"+cut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filemerge;
	}

	private static void filterMosesRules(File file) throws IOException {
		BufferedReader in;
		//BufferedWriter out = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath()+".scores")));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()+".scores"),Constants.UTF8));
		String inputLine="", en, de;
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(file));
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

	private static void counting(File file) throws IOException {
		BufferedReader in;
		//BufferedWriter out = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath()+".len")));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()+".len"),Constants.UTF8));

		String inputLine="", en, de, nen, nde, temp;
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(file));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				temp = inputLine.trim();
				if (temp.equals("0") || temp.equals("1")){
					out.write(linecounter+"\t0\t0\t0");
					continue;
				}
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();	nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();	nde = ContentNormalizer.normtext(de);
				int enlen= FCStringUtils.countTokens(nen);  
				int delen = FCStringUtils.countTokens(nde);  
				int t = enlen+delen;
				out.write(linecounter+"\t"+Integer.toString(enlen)+"\t"+Integer.toString(delen)+ Integer.toString(t));				
			}	
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void rescoring(String[] args) throws IOException {
		File datafilt = new File(args[0]+".filt");
		File datafiltscores = new File(args[0]+".filt.scores");
		File datafiltscores1 = new File(args[0]+".filt.scores.1");

		String inputLine, en, de, nen, nde, nen1, nde1;
		BufferedReader in;
		int  linecounter=-1;
		double score;
		List<Double> scores = new ArrayList<Double>();
		try {
			in = new BufferedReader(new FileReader(datafiltscores));
			while ((inputLine = in.readLine()) != null) {
				//linecounter++;
				score = Double.parseDouble(inputLine.trim());
				scores.add(score);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			in = new BufferedReader(new FileReader(datafilt));
			int encount = 0, decount=0;

			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				score = scores.get(linecounter); 
				if (score==2){
					String[] tt = inputLine.split("\t");
					en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
					de = tt[1].trim();				nde = ContentNormalizer.normtext(de);

					if (((float)nen.length()/(float)en.length())   <0.5 
							|| ((float)nde.length()/(float)de.length())<0.5){
						//LOGGER.info(linecounter+"\t"+inputLine);
						LOGGER.info(linecounter+"\tfew-letters05:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}

					if (nen.equals("customer ratings") || nde.endsWith("customer ratings")
							|| nen.equals("cut")
							|| nen.startsWith("gebote") || nen.endsWith("gebote")
							|| nen.startsWith("folg")
							|| nen.startsWith("grannkillar")
							|| nen.startsWith("guy next door")
							|| nen.startsWith("guy next door")
							|| inputLine.contains("0 (beginner)")
							|| inputLine.contains("0 Be the first!")
							|| nen.equals("bids")
							|| nen.equals("bids eur")
							|| nen.startsWith("0 article")
							|| nen.startsWith("0 artículo")
							|| nen.equals("comment")
							|| (nen.startsWith("comment") && !en.startsWith("Comment:"))
							|| nen.equals("comments from users comments listed")
							|| nen.equals("antworten") || nde.equals("antworten")
							|| nen.contains("anfänger") || nde.equals("anfänger")
							|| (inputLine.contains("Coins/min") && !nen.equals("LadySarah") && !nde.equals("LadySarah"))
							|| inputLine.contains("ArtikelGesamt")	
							//|| inputLine.contains("pdf")
							|| (nen.contains("pdf") && !nde.contains("pdf"))
							|| (!nen.contains("pdf") && nde.contains("pdf"))
							){
						LOGGER.info(linecounter+"\tspecial-cases:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}
					if (inputLine.contains("pdf")){
						String num1=en.replaceAll("\\D+","");
						String num2=de.replaceAll("\\D+","");
						if (num1.isEmpty() || num2.isEmpty()){
							LOGGER.info(linecounter+"\tspecial-cases:\t"+inputLine);
							scores.set(linecounter, 0.0);
							continue;
						}
						if (!num1.equals(num2)){
							LOGGER.info(linecounter+"\tspecial-cases:\t"+inputLine);
							scores.set(linecounter, 0.0);
							continue;
						}
					}

					encount = symbolInString(en, "|");
					decount = symbolInString(de, "|");
					if (Math.abs(encount-decount)>1){
						LOGGER.info(linecounter+"\tnumof|:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}
					if (  (en.startsWith("00") && !de.startsWith("00"))
							|| (!en.startsWith("00") && de.startsWith("00"))
							|| (en.startsWith("0%") && !de.startsWith("0%"))
							|| (!en.startsWith("0%") && de.startsWith("0%"))
							|| (en.startsWith("0.") && !de.startsWith("0."))
							|| (!en.startsWith("0.") && de.startsWith("0."))
							|| (en.startsWith("€0") && !de.contains("€"))
							//|| (en.startsWith("["))
							|| (!en.startsWith("+0") && de.startsWith("+0"))
							|| (!en.startsWith("0 0") && de.startsWith("0 0"))
							|| (!en.startsWith("0-100") && de.startsWith("0-100"))
							|| (!en.startsWith("- 0-") && de.startsWith("- 0-"))){
						LOGGER.info(linecounter+"\twrong-heur:\t"+inputLine);
						scores.set(linecounter, 0.0);
						continue;
					}

					/*nen = ContentNormalizer.normtext(en);
					nde = ContentNormalizer.normtext(de);
					if (checkmonths(nen,nde)){
						LOGGER.info(linecounter+"\t"+inputLine);
						scores.set(linecounter, score-0.1);
					}*/
				}
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileUtils.writeLines(datafiltscores1, Constants.UTF8, scores,"\n");
	}

	private static int symbolInString(String str, String symbol) {
		int counter = 0;
		for (int ii=0;ii<str.length();ii++){
			String a =Character.toString(str.charAt(ii));
			if (a.equals(symbol)){
				counter++;
			}
		}	
		return counter;
	}

	private static void checkmonths1(String[] args) {
		File datafile = new File(args[0]);
		String inputLine, en, de, nen, nde, nen1, nde1;
		BufferedReader in;
		int  linecounter=-1;
		try {
			in = new BufferedReader(new FileReader(datafile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
				if (checkmonths(nen,nde))
					LOGGER.info(linecounter+"\t"+inputLine);
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkmonths(String nen, String nde) {
		if ((nen.contains("january") && !nde.contains("januar")) ||  (!nen.contains("january") && nde.contains("januar")))
			return true;
		if ((nen.contains("february") && !nde.contains("februar")) || (!nen.contains("february") && nde.contains("februar")))
			return true;
		if ((nen.contains("march") && !nde.contains("märz")) ||(!nen.contains("march") && nde.contains("märz")))
			return true;
		if ((nen.contains("april") && !nde.contains("april")) ||(!nen.contains("april") && nde.contains("april")) ) 
			return true;
		if ((nen.equals("may") && !nde.contains("mai")) )//|| (!nen.equals("may") && nde.contains("mai"))) 
			return true;
		if ((nen.contains("june") && !nde.contains("juni")) || (!nen.contains("june") && nde.contains("juni"))) 
			return true;
		if ((nen.contains("july") && !nde.contains("juli")) || (!nen.contains("july") && nde.contains("juli"))) 
			return true;
		if ((nen.contains("august") && !nde.contains("august")) || (!nen.contains("august") && nde.contains("august"))) 
			return true;
		if ((nen.contains("september") && !nde.contains("september")) || (!nen.contains("september") && nde.contains("september"))) 
			return true;
		if ((nen.contains("october") && !nde.contains("oktober")) || (!nen.contains("october") && nde.contains("oktober"))) 
			return true;
		if ((nen.contains("november") && !nde.contains("november")) || (!nen.contains("november") && nde.contains("november"))) 
			return true;
		if ((nen.contains("december") && !nde.contains("dezember")) || (!nen.contains("december") && nde.contains("dezember")))
			return true;

		return false;
	}

	private static void normalizing(String[] args) throws IOException {
		File newdata = new File(args[0]+".filt.norm");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()),Constants.UTF8));

		String inputLine, en, de, nen, nde ;
		int linecounter=-1;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(args[0]+".filt"));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (inputLine.equals("0")){
					outnewdata.write("0\n");
					continue;
				}
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
				String temp = nen+nde;
				outnewdata.write(linecounter+"\t"+temp+"\t"+en.length()+"\t"+tt[2].trim()+"\n");
			}
			in.close();
			outnewdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static boolean drop(String inputLine, String en, String de, int linecounter){
		boolean found;
		boolean res=true;

		if (en.matches(".*[\\u0530-\\u058F].*") || de.matches(".*[\\u0530-\\u058F].*")){
			LOGGER.info(linecounter +"\tarmenian:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0900-\\u097F].*") || de.matches(".*[\\u0900-\\u097F].*")){
			LOGGER.info(linecounter +"\thindi:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0400-\\u04FF].*") || de.matches(".*[\\u0400-\\u04FF].*")){
			LOGGER.info(linecounter +"\tcyrillic:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u10A0-\\u10FF].*") || de.matches(".*[\\u10A0-\\u10FF].*")){
			LOGGER.info(linecounter +"\tgeorgian:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
			LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
			LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
			LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
			LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0590-\\u05FF].*") || de.matches(".*[\\u0590-\\u05FF].*")){
			LOGGER.info(linecounter +"\tHebrew:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u1100-\\u11FF].*") || de.matches(".*[\\u1100-\\u11FF].*")
				|| en.matches(".*[\\uAC00-\\uD7AF].*") || de.matches(".*[\\uAC00-\\uD7AF].*")){
			LOGGER.info(linecounter +"\tHangul Jamo:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u3040-\\u309F].*") || de.matches(".*[\\u3040-\\u309F].*")){
			LOGGER.info(linecounter +"\tHiragana:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u30A0-\\u30FF].*") || de.matches(".*[\\u30A0-\\u30FF].*")){
			LOGGER.info(linecounter +"\tKatakana:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0370-\\u03FF].*") || de.matches(".*[\\u0370-\\u03FF].*")){
			LOGGER.info(linecounter +"\tGreek:\t"+ inputLine);
			return false;
		}
		if (en.matches(".*[\\u0E00-\\u0E7F].*") || de.matches(".*[\\u0E00-\\u0E7F].*")){
			LOGGER.info(linecounter +"\tThai:\t"+ inputLine);
			return false;
		}
		for (int ii=0;ii<exclude_grams.length;ii++){
			if (inputLine.contains(exclude_grams[ii])){
				LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
				return false;
			}
		}

		found = false;
		boolean foundsame= false;
		int counter = 0, countersame=0;
		//Set<String> nenchars = new HashSet<String>();
		String b = "";

		for (int ii=0;ii<en.length();ii++){
			String a =Character.toString(en.charAt(ii));
			for (int jj=0;jj<exclude_symbols.length;jj++){
				if (a.equals(exclude_symbols[jj]))
					counter++;
				if (counter>1){
					found = true;
					break;
				}
			}
			//if (!nenchars.contains(a))
			//	nenchars.add(a);
			/*if (a.equals(b)){
				countersame++;
				if (countersame>4){
					foundsame=true;
					break;
				}
			}else{
				b=a;
				countersame=1;
			}*/
		}
		//if (nenchars.size()<3){
		//	LOGGER.info(linecounter +"\tsame-letters5:\t"+inputLine);
		//	return false;
		//}
		if (foundsame){
			LOGGER.info(linecounter +"\tsame-letters5:\t"+inputLine);
			return false;
		}

		/*
		for (int ii=0;ii<exclude_symbols.length;ii++){
			if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
				counter++;
				if (counter>2){
					found = true;
					break;
				}
			}
		}*/

		if (found){
			LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
			return false;
		}
		counter = 0;
		countersame=0;
		for (int ii=0;ii<de.length();ii++){
			String a =Character.toString(de.charAt(ii));
			for (int jj=0;jj<exclude_symbols.length;jj++){
				if (a.equals(exclude_symbols[jj]))
					counter++;
				if (counter>1){
					found = true;
					break;
				}
				/*if (a.equals(b)){
					countersame++;
					if (countersame>4){
						foundsame=true;
						break;
					}
				}else{
					b=a;
					countersame=1;
				}*/
			}
		}
		if (foundsame){
			LOGGER.info(linecounter +"\tsame-letters5:\t"+inputLine);
			return false;
		}

		if (found){
			LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
			return false;
		}

		if ((en.contains("??") && !de.contains("??")) || (!en.contains("??") && de.contains("??"))) {
			LOGGER.info(linecounter +"\tspecial symbols??:\t"+inputLine);
			return false;
		}

		return res;
	}

	private static void scoring_filter(String[] args) throws IOException {
		String[] langs = new String[2];
		langs[0]= "eng";
		langs[1]= "deu";

		File datafile = new File(args[0]);
		File newdata = new File(datafile.getAbsolutePath()+"."+args[1]+".filt.words2");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()),Constants.UTF8));

		String inputLine, en, de, nen, nde;

		BufferedReader in;
		//List<String> scores= new ArrayList<String>();
		List<Double> scores= new ArrayList<Double>();
		int  linecounter=-1;
		int milcounter=1;
		boolean found = false;
		HashMap<String, int[]> map = new  HashMap<String, int[]>();
		//HashMap<String, Integer> enpart = new HashMap<String, Integer>();
		HashMap<String, double[]> enpart = new HashMap<String, double[]>();
		try {
			in = new BufferedReader(new FileReader(datafile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				//if (linecounter!=(scores.size())){
				//	System.out.println("ooops");
				//}

				//if ((linecounter / 5000000)>milcounter){
				//	FileUtils.writeLines(new File(args[0]+"."+args[1]+".filt.scores"), Constants.UTF8, scores, "\n");
				//	milcounter++;
				//}
				//if (linecounter==176235 || linecounter==157059){
				//	System.out.println("dsddssd");
				//}
				String[] tt = inputLine.split("\t");
				en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
				de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
				//String num1=en.replaceAll("\\D+","");
				//String num2=de.replaceAll("\\D+","");

				int enlen= FCStringUtils.countTokens(en);  //FCStringUtils.countTokens(en);
				int delen = FCStringUtils.countTokens(de);  //FCStringUtils.countTokens(de);
				if (enlen>80 || delen>80){
					scores.add(0.0); //scores.add(" 0 ");
					//outnewdata.write("0\n");
					LOGGER.info(linecounter+"\tMosesLen80:\t"+inputLine);
					continue;
				}

				if (enlen>9*delen || delen>9*enlen){
					scores.add(0.0); //scores.add(" 0 ");
					//outnewdata.write("0\n");
					LOGGER.info(linecounter+"\tMosesRa:\t"+inputLine);
					continue;
				}

				if (nen.isEmpty() || nde.isEmpty()){
					scores.add(0.0); //scores.add(" 0 ");
					LOGGER.info(linecounter+"\tno-letters:\t"+inputLine);
					continue;
				}

				//	if (nen1.isEmpty() || nde1.isEmpty()){
				//		scores.add(0.0); //scores.add(" 0 ");
				//		outnewdata.write("0\n");	LOGGER.info(linecounter+"\tno-letters:\t"+inputLine);
				//		continue;
				//	}

				if (nen.equals(nde)){
					scores.add(0.0); //scores.add(" 0 ");
					LOGGER.info(linecounter+"\tequal-TUVs:\t"+inputLine);
					continue;
				}
				if (nen.length()<4 || nde.length()<4){
					scores.add(0.0); //scores.add(" 0 ");
					LOGGER.info(linecounter+"\tno-letters3:\t"+inputLine);
					continue;
				}


				if (enlen<3 || delen<3){
					scores.add(0.0); //scores.add(" 0 ");
					outnewdata.write(inputLine+"\n"); 	
					continue;
				}
				if (TMXHandlerUtils.checkemail(en, 0.5) || TMXHandlerUtils.checkemail(de, 0.5)){
					scores.add(0.0); //scores.add(" 0 ");
					LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					continue;
				}
				if (TMXHandlerUtils.checkurl(en, 0.5) || TMXHandlerUtils.checkurl(de, 0.5)){
					scores.add(0.0); //scores.add(" 0 ");
					LOGGER.info(linecounter +"\turl:\t"+ inputLine);
					continue;
				}



				List<String> detoks = FCStringUtils.getTokens(nde);
				Double[] detokenslen = FCStringUtils.getTokensLength(detoks);
				if (Statistics.getMax(detokenslen)>45){
					scores.add(0.0); //scores.add(" 0 ");
					//outnewdata.write("0\n");
					LOGGER.info(linecounter+"\tlong-words in norm:\t"+inputLine);
					continue;
				}
				List<String> entoks = FCStringUtils.getTokens(nen);
				Double[] entokenslen = FCStringUtils.getTokensLength(entoks);
				if (Statistics.getMax(entokenslen)>45){
					scores.add(0.0); //scores.add(" 0 ");
					//outnewdata.write("0\n");
					LOGGER.info(linecounter+"\tlong-words in norm:\t"+inputLine);
					continue;
				}

			}
			in.close();
			outnewdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(scores.size());
	}

	private static boolean wrongtrans(String inputLine, String en, String de, String nen, String nde) {
		boolean found =false;


		if (en.contains("incScript") || de.contains("incScript")
				|| en.contains("includeLibs") || de.contains("includeLibs")	
				|| en.contains("RFUNC") || de.contains("RFUNC")
				|| en.contains(" but =")
				|| (en.contains("fputs(") && !de.contains("fputs("))
				|| (en.contains("fwrite(") && !de.contains("fwrite("))
				|| (en.contains("fclose(") && !de.contains("fclose("))
				|| (en.contains("fopen(") && !de.contains("fopen("))
				){
			return true;
		}

		if (nen.equals("customer ratings") || nde.endsWith("customer ratings")
				|| nen.equals("cut")
				|| nen.startsWith("gebote") || nen.endsWith("gebote")
				|| nen.startsWith("folg")
				|| nen.startsWith("grannkillar")
				|| nen.startsWith("guy next door")
				|| nen.startsWith("guy next door")
				|| inputLine.contains("0 (beginner)")
				|| inputLine.contains("0 Be the first!")
				|| nen.equals("bids")
				|| nen.equals("bids eur")
				|| nen.startsWith("0 article")
				|| nen.startsWith("0 artículo")
				|| nen.equals("comment")
				|| (nen.startsWith("comment ") && !en.startsWith("Comment:"))
				|| nen.equals("comments from users comments listed")
				|| nen.equals("antworten") || nde.equals("antworten")
				|| nen.contains("anfänger") || nde.equals("anfänger")
				|| (inputLine.contains("Coins/min") && !nen.equals("LadySarah") && !nde.equals("LadySarah"))
				|| inputLine.contains("ArtikelGesamt")	
				//|| inputLine.contains("pdf")
				//|| (nen.contains("pdf") && !nde.contains("pdf"))
				//|| (!nen.contains("pdf") && nde.contains("pdf"))
				|| nen.startsWith("byjoern")
				|| nen.contains("jquery")
				|| nde.contains("jquery")
				|| nen.startsWith("pinyin")
				|| nde.startsWith("pinyin")			
				|| nen.startsWith("bymanniLo")
				|| nen.startsWith("byThym1959")
				|| nen.startsWith("premier")
				){
			return true;
		}

		int encount = symbolInString(en, "|");
		int decount = symbolInString(de, "|");
		if (Math.abs(encount-decount)>2){
			return true;
		}


		if (  (en.startsWith("00") && !de.startsWith("00"))
				|| (!en.startsWith("00") && de.startsWith("00"))
				|| (en.startsWith("0%") && !de.startsWith("0%"))
				|| (!en.startsWith("0%") && de.startsWith("0%"))
				|| (en.startsWith("0.") && !de.startsWith("0."))
				|| (!en.startsWith("0.") && de.startsWith("0."))
				|| (en.startsWith("€0") && !de.contains("€"))
				//|| (en.startsWith("["))
				|| (!en.startsWith("+0") && de.startsWith("+0"))
				|| (!en.startsWith("0 0") && de.startsWith("0 0"))
				|| (!en.startsWith("0-100") && de.startsWith("0-100"))
				|| (!en.startsWith("- 0-") && de.startsWith("- 0-"))){
			return true;
		}
		return found;
	}

	private static boolean wrongmonths(String nen, String nde) {
		if (nen.equals("january") && !nde.equals("januar"))
			return true;
		if (nen.equals("february") && !nde.equals("februar"))
			return true;
		if (nen.equals("march") && !nde.equals("märz"))
			return true;
		if (nen.equals("april") && !nde.equals("april"))
			return true;
		if (nen.equals("may") && !nde.equals("mai"))
			return true;
		if (nen.equals("june") && !nde.equals("juni"))
			return true;
		if (nen.equals("july") && !nde.equals("juli"))
			return true;
		if (nen.equals("august") && !nde.equals("august"))
			return true;
		if (nen.equals("september") && !nde.equals("september"))
			return true;
		if (nen.equals("october") && !nde.equals("oktober"))
			return true;
		if (nen.equals("november") && !nde.equals("november"))
			return true;
		if (nen.equals("december") && !nde.equals("dezember"))
			return true;

		return false;
	}

	private static boolean connected(String nen) {
		boolean found =false;
		for (int ii=0;ii<connect.length;ii++){
			if (nen.contains(connect[ii]))
				return true;
		}
		return found;
	}

	private static void scoring_dedup(String[] args) throws IOException {
		File newdata = new File(args[0]+".filt.dedup");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()),Constants.UTF8));
		String inputLine, inputLine1, text, text1;
		int len, len1;
		//double hunscore, hunscore1;
		BufferedReader in, in1;
		List<String> scores= new ArrayList<String>();
		Set<Integer> dups = new HashSet<Integer>();
		int  linecounter=-1, linecounter1;
		String index, index1;
		boolean found;
		try {
			in = new BufferedReader(new FileReader(args[0]+".filt.norm"));

			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				if (dups.contains(linecounter)){
					scores.add(" 0 ");
					outnewdata.write("0\n");
					LOGGER.info(linecounter+"\tdups:\t"+inputLine);
					continue;
				}
				if (inputLine.equals("0")){
					scores.add(" 0 ");
					outnewdata.write("0\n");
					continue;
				}
				String[] tt = inputLine.split("\t");
				index = tt[0];
				text = tt[1].trim();				//nen = ContentNormalizer.normtext(en);				//de = tt[1].trim();				//nde = ContentNormalizer.normtext(de);
				len = Integer.parseInt(tt[2].trim());
				//hunscore = Double.parseDouble(tt[2].trim());
				//String temp = nen+nde;
				in1 = new BufferedReader(new FileReader(args[0]+".filt.norm"));
				linecounter1=-1;
				found=false;
				while ((inputLine1 = in1.readLine()) != null) {
					linecounter1++;
					if (dups.contains(linecounter1))
						continue;
					if (linecounter1<=linecounter)
						continue;
					if (inputLine1.equals("0"))
						continue;
					//System.out.println(linecounter+"   >>>> "+linecounter1);
					String[] tt1 = inputLine1.split("\t");
					index1 = tt1[0];
					text1 = tt1[10].trim();				//nen1 = ContentNormalizer.normtext(en1);				//de1 = tt1[1].trim();				//nde1 = ContentNormalizer.normtext(de1);
					len1 = Integer.parseInt(tt1[2].trim());
					//hunscore1 = Double.parseDouble(tt1[2].trim());
					//String temp1 = nen1+nde1;
					//System.out.println("-------------------------------------------------------------------------");
					//System.out.println(linecounter+"   :  "+temp);
					//System.out.println(linecounter1+"   :  "+temp1);
					if (len1!=len)
						continue;
					if (text.equals(text1)){
						//if (hunscore<hunscore1){
						if (len<=len1){	
							//found = true;
							dups.add(linecounter1);
							//break;
						}else{
							found = true;
							break;
						}
					}
				}
				in1.close();
				if (!found){
					scores.add(" 1 ");
					outnewdata.write(inputLine+"\n");
					LOGGER.info("passed:\t"+ linecounter);
				}else{
					scores.add(" 0 ");
					outnewdata.write("0\n");
					LOGGER.info(linecounter+" >> "+linecounter1+"\tdups:\t"+inputLine+ " >> "+inputLine1);
				}
			}
			in.close();
			outnewdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileUtils.writeLines(new File(args[0]+".filt.dedup.scores"), Constants.UTF8, scores, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void scoring_dedup1(String[] args) throws IOException {
		File newdata = new File(args[0]+".filt.dedup");
		//BufferedWriter outnewdata = new BufferedWriter(new FileWriter(newdata));
		BufferedWriter outnewdata = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata.getAbsolutePath()),Constants.UTF8));
		String inputLine, inputLine1, en, de, nen, nde, en1, de1, nen1, nde1, indt="";
		//double hunscore, hunscore1;
		//BufferedReader in, in1;
		List<String> scores= new ArrayList<String>();
		Set<Integer> dups = new HashSet<Integer>();
		int  indi=0;
		boolean found=false;

		List<String> lines = FileUtils.readLines(new File(args[0]+".filt"), Constants.UTF8);
		for (int linecounter=0;linecounter<lines.size()-1;linecounter++){
			inputLine = lines.get(linecounter);
			if (dups.contains(linecounter)){
				scores.add(" 0 ");
				outnewdata.write("0\n");
				LOGGER.info(linecounter+"\tdups:\t"+inputLine);
				continue;
			}
			if (inputLine.equals("0")){
				scores.add(" 0 ");
				outnewdata.write("0\n");
				continue;
			}
			String[] tt = inputLine.split("\t");
			en = tt[0].trim();				nen = ContentNormalizer.normtext(en);
			de = tt[1].trim();				nde = ContentNormalizer.normtext(de);
			//hunscore = Double.parseDouble(tt[2].trim());
			String temp = nen+nde;
			found = false;
			for (int linecounter1=linecounter+1;linecounter1<lines.size();linecounter1++){
				inputLine1 = lines.get(linecounter1);
				if (dups.contains(linecounter1))
					continue;
				//if (linecounter1<=linecounter)
				//	continue;
				if (inputLine1.equals("0"))
					continue;
				//System.out.println(linecounter+"   >>>> "+linecounter1);
				String[] tt1 = inputLine1.split("\t");
				en1 = tt1[0].trim();				nen1 = ContentNormalizer.normtext(en1);
				de1 = tt1[1].trim();				nde1 = ContentNormalizer.normtext(de1);
				//hunscore1 = Double.parseDouble(tt1[2].trim());
				String temp1 = nen1+nde1;
				if (temp.equals(temp1)){
					//if (hunscore<hunscore1){
					if (en.length()<=en1.length()){	
						//found = true;
						dups.add(linecounter1);
						//break;
					}else{
						found = true;
						indi = linecounter1;
						indt = inputLine1;
						break;
					}
				}
			}
			if (!found){
				scores.add(" 1 ");
				outnewdata.write(inputLine+"\n");
				LOGGER.info("passed:\t"+ linecounter);
			}else{
				scores.add(" 0 ");
				outnewdata.write("0\n");
				LOGGER.info(linecounter+" >> "+indi+"\tdups:\t"+inputLine+ " >> "+indt);
			}
		}
		outnewdata.close();
		try {
			FileUtils.writeLines(new File(args[0]+".filt.dedup.scores"), Constants.UTF8, scores, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void clusterbysite(String[] args) {
		File infile = new File(args[0]); 
		File outdir = new File(FilenameUtils.concat(infile.getParent(),"sites"));
		BufferedReader in;

		int  linecounter=-1 ;//, comma=0;
		String inputLine, site1, site2, en,  de, score, current_page="", url, nen, nde; 
		URL url1, url2;
		boolean found = false;
		int pagecounter=0;
		Set<String> sites = new HashSet<String>();
		float ratio;
		List<String> lines = new ArrayList<String>();
		File tempfile = new File(FilenameUtils.concat(outdir.getAbsolutePath(), "mixed"));
		File currentfile=null;
		try {
			FileUtils.writeLines(tempfile, Constants.UTF8, lines, "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			in = new BufferedReader(new FileReader(infile));
			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.contains(Constants.TAB)){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length!=5){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				en = attrs[0].trim();
				de = attrs[1].trim();
				nen = ContentNormalizer.normtext(en);
				nde = ContentNormalizer.normtext(de);
				if ( nen.isEmpty() || nde.isEmpty()){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					continue;
				}
				if (nen.equals(nde)){
					LOGGER.info(linecounter +"\texact identical TUVs:\t"+inputLine);
					continue;
				}
				if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					continue;
				}
				if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					continue;
				}
				if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					continue;
				}
				if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					continue;
				}
				found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
					continue;
				}
				if ((en.contains("??") && !de.contains("??")) || (!en.contains("??") && de.contains("??"))) {
					LOGGER.info(linecounter +"\tspecial symbols??:\t"+inputLine);
					continue;
				}
				//ratio = (float)en.length()/(float)de.length();
				//if (ratio<0.3 || ratio >2){
				//	LOGGER.info(linecounter +"\tratio:\t"+ inputLine);
				//	continue;
				//}

				url = attrs[2].trim();
				url1 = new URL(url);
				url2 = new URL(attrs[3].trim());
				//score = Double.parseDouble(attrs[4].trim());
				score = attrs[4].trim();
				site1 = url1.getAuthority();
				site2 = url2.getAuthority();

				if (site1.equals(site2)){
					tempfile = 	new File(FilenameUtils.concat(outdir.getAbsolutePath(), site1));
					if (sites.contains(site1)){
						if (current_page.equals(url)) {
							lines = FileUtils.readLines(currentfile, Constants.UTF8);
						}else{
							lines.clear();
							pagecounter++;
							currentfile = new File(FilenameUtils.concat(tempfile.getAbsolutePath(), Integer.toString(pagecounter)));
						}
					}else{
						lines.clear();
						sites.add(site1);
						LOGGER.info(site1);
						pagecounter=1;
						currentfile = new File(FilenameUtils.concat(tempfile.getAbsolutePath(), Integer.toString(pagecounter)));
					}
					lines.add(linecounter+"\t"+en+"\t"+de+"\t"+score);
					FileUtils.writeLines(currentfile, Constants.UTF8, lines, "\n");

					//if (sites.contains(site1))
					//	lines = FileUtils.readLines(tempfile, Constants.UTF8);
					//else{
					//	lines.clear();
					//	sites.add(site1);
					//	LOGGER.info(site1);
					//}
				}else{
					tempfile = new File(FilenameUtils.concat(outdir.getAbsolutePath(), "mixed"), Constants.UTF8);
					lines = FileUtils.readLines(tempfile, Constants.UTF8);
					LOGGER.info(site1+"\t"+site2);
					lines.add(linecounter+"\t"+inputLine);
				}
				//lines.add(linecounter+"\t"+en+"\t"+de+"\t"+score);
				//if (current_page.equals(url)) 
				//FileUtils.writeLines(tempfile, Constants.UTF8, lines, "\n");
				//currentfile = tempfile;
				current_page = url;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static File dedupWMT18_TUV(File indir, String ext, int tupart, int THR) throws IOException {
		File outfile = new File(indir.getAbsolutePath()+ext);
		//BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));

		File dedupdir = new File(FilenameUtils.concat(indir.getParent(), "dupTUV"+Integer.toString(tupart)));
		dedupdir.mkdir();
		File[] lenfiles = indir.listFiles();
		int[] lens = new int[lenfiles.length];
		for (int ii=0;ii<lens.length;ii++){
			lens[ii] = Integer.parseInt(lenfiles[ii].getName());
		}
		Arrays.sort(lens);
		int counter=0;
		Set<String> seen = new HashSet<String>();
		for (int ii=lens.length-1;ii>-1;ii--){
			BufferedReader in1, in2;
			File tempfile1 = new File(FilenameUtils.concat(indir.getAbsolutePath(), Integer.toString(lens[ii])));
			in1 = new BufferedReader(new FileReader(tempfile1));
			//String inputLine1, tuv1, ntuv1,  inputLine2, tuv2, ntuv2;
			String inputLine1, inputLine2;
			int linecounter1=0;
			while ((inputLine1 = in1.readLine()) != null) {
				linecounter1++;
				String[] attrs1 = inputLine1.split(Constants.TAB);
				if (seen.contains(attrs1[0]))
					continue;
				//tuv1 = attrs1[tupart];
				//ntuv1 = ContentNormalizer.normtext(tuv1);
				//List<String> stokens1 = FCStringUtils.getTokens(ntuv1);
				//Set<String> grams1 = getGrams(stokens1);
				Set<String> grams1 = getGrams(FCStringUtils.getTokens(ContentNormalizer.normtext(attrs1[tupart])));

				List<String> dups = new ArrayList<String>();
				dups.add(inputLine1);
				for (int kk=lens.length-1;kk>-1;kk--){
					if (kk>ii)
						continue;
					File tempfile2 = new File(FilenameUtils.concat(indir.getAbsolutePath(), Integer.toString(lens[kk])));
					System.out.println(tempfile1.getName()+" --vs-- "+tempfile2.getName());
					in2 = new BufferedReader(new FileReader(tempfile2));
					int linecounter2=0;
					while ((inputLine2 = in2.readLine()) != null) {
						if (kk==ii){
							if (linecounter2<linecounter1){
								linecounter2++;
								continue;
							}
						}
						linecounter2++;
						String[] attrs2 = inputLine2.split(Constants.TAB);
						if (seen.contains(attrs2[0]))
							continue;
						//tuv2 = attrs2[tupart];
						//ntuv2 = ContentNormalizer.normtext(tuv2);
						//List<String> stokens2 = FCStringUtils.getTokens(ntuv2);
						//Set<String> grams2 =getGrams(stokens2);

						Set<String> grams2 = getGrams(FCStringUtils.getTokens(ContentNormalizer.normtext(attrs2[tupart])));

						Set intersection = new HashSet(grams1);
						intersection.retainAll(grams2);
						if ((Math.min(grams2.size(),grams1.size()) -  intersection.size()  ) < THR){
							System.out.println(inputLine2);
							dups.add(inputLine2);
							seen.add(attrs2[0]);
						}
					}
					in2.close();
				}
				if (dups.size()>1){
					File temp = new File(FilenameUtils.concat(dedupdir.getAbsolutePath(),"_dup-"+counter));
					FileUtils.writeLines(temp, Constants.UTF8, dups, "\n");
					counter++;
				}
				if (dups.size()==1){
					bw.write(inputLine1+"\n");
				}

			}
			in1.close();
		}
		bw.close();
		return outfile;
	}

	private static Set<String> getGrams(List<String> tokens) {
		Set<String> grams = new HashSet<String>();
		for (int jj=0;jj<tokens.size()-1;jj++){
			grams.add(tokens.get(jj));  												//1-gram
			grams.add(tokens.get(jj)+" "+tokens.get(jj+1));								//2-gram
			if (jj>0){
				grams.add(tokens.get(jj-1)+" "+tokens.get(jj)+" "+tokens.get(jj+1));	//3-gram
			}
		}
		return grams;
	}

	private static List<File> filterCorpus(File infile, String[] langs, int minTuvLen, int maxTuvLen, float minratio, float maxratio, String ext, String version) {
		LOGGER.info("\nargs:\n 0=fullpath of infile\n 1=languages separated by ;\n 2=minTUVlen\n 3=maxTUVlen\n 4=minTUVratio\n 5=maxTUVratio\n 6=version");
		List<File> outs = new ArrayList<File>();
		//LangDetector langDetector = LangDetectUtils.loadLangDetectors(langs,"langdetect");

		File outfile0 = new File(infile.getAbsolutePath()+ext+"."+version);
		File outfile1 = new File(infile.getAbsolutePath()+ext+".samenum"+"."+version);
		File outfile1a = new File(infile.getAbsolutePath()+ext+".nonum"+"."+version);
		File outfile2 = new File(infile.getAbsolutePath()+ext+".samesym"+"."+version);
		File outfile3 = new File(infile.getAbsolutePath()+ext+".samenumsym"+"."+version);
		File outfile3a = new File(infile.getAbsolutePath()+ext+".nonumsamesym"+"."+version);
		File l1file = new File(infile.getAbsolutePath()+".l1");
		File l2file = new File(infile.getAbsolutePath()+".l2");
		outs.add(outfile0); outs.add(outfile1); outs.add(outfile1a); outs.add(outfile2); outs.add(outfile3);	outs.add(outfile3a);  
		String inputLine;
		BufferedReader in;
		BufferedWriter  bw0 ,bw1, bw2, bw3, bwl1, bwl2, bw1a, bw3a;

		BufferedWriter  bwcharsets ,bwexclsymbols, bwidenTUVs, bwsymbols1, bwsymbols2, bwshort, bwlong, bwlang, bwratio;
		BufferedWriter bwemail, bwurl, bwcase, bwmaxlen, bwminlen, bwhunscore, bwincoh, bwnearTUVs, bwrepeats;

		int  counter0 = 0, counter1= 0, counter1a= 0, counter2= 0, counter3 = 0, counter3a = 0, tempcounter = 0, linecounter=-1 ;//, comma=0;
		String lang1, lang2, nl1, nl2, num1, num2, l1, l2;
		float ratio;
		boolean found = false;
		//Set<String> nenlist1 = new HashSet<String>();
		//Set<String> ndelist1 = new HashSet<String>();

		try {
			in = new BufferedReader(new FileReader(infile));
			/*bw0 = new BufferedWriter(new FileWriter(outfile0));
			bw1 = new BufferedWriter(new FileWriter(outfile1));
			bw1a = new BufferedWriter(new FileWriter(outfile1a));
			bw2 = new BufferedWriter(new FileWriter(outfile2));
			bw3 = new BufferedWriter(new FileWriter(outfile3));
			bw3a = new BufferedWriter(new FileWriter(outfile3a));
			bwen = new BufferedWriter(new FileWriter(enfile));
			bwde = new BufferedWriter(new FileWriter(defile));*/
			bw0 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile0.getAbsolutePath()),Constants.UTF8));
			bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1.getAbsolutePath()),Constants.UTF8));
			bw1a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1a.getAbsolutePath()),Constants.UTF8));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile2.getAbsolutePath()),Constants.UTF8));
			bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3.getAbsolutePath()),Constants.UTF8));
			bw3a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3a.getAbsolutePath()),Constants.UTF8));
			bwl1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(l1file.getAbsolutePath()),Constants.UTF8));
			bwl2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(l2file.getAbsolutePath()),Constants.UTF8));

			/*bwcharsets = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".charsets")));
			bwexclsymbols = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".exclsymbols")));
			bwidenTUVs = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".idenTUVs")));
			bwsymbols = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".mostly-non-letters")));
			bwshort = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".short")));
			bwlong = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".long")));
			bwlang = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".lang")));
			bwratio = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".ratio")));
			bwcase = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".case")));
			bwemail = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".email")));
			bwurl = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".url")));
			bwmaxlen = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".max-len")));
			bwminlen = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".min-len")));
			bwhunscore = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".hun-score")));
			bwincoh = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".incoh")));
			bwnearTUVs = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".nearTUVs")));
			bwrepeats = new BufferedWriter(new FileWriter( new File(infile.getAbsolutePath()+".repeats")));*/

			bwcharsets =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".charsets"),Constants.UTF8));
			bwsymbols1 = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".non-letters"),Constants.UTF8));
			
			
			bwexclsymbols =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".exclsymbols"),Constants.UTF8));
			
			bwidenTUVs =	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".idenTUVs"),Constants.UTF8));
			bwsymbols2 = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".mostly-non-letters"),Constants.UTF8));
			bwshort = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".short"),Constants.UTF8));
			bwlong = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".long"),Constants.UTF8));
			bwlang = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".lang"),Constants.UTF8));
			bwratio = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".ratio"),Constants.UTF8));
			bwcase =		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".case"),Constants.UTF8));
			bwemail = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".email"),Constants.UTF8));
			bwurl = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".url"),Constants.UTF8));
			bwmaxlen = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".max-len"),Constants.UTF8));
			bwminlen = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".min-len"),Constants.UTF8));
			bwhunscore = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".hun-score"),Constants.UTF8));
			bwincoh = 		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".incoh"),Constants.UTF8));
			bwnearTUVs = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".nearTUVs"),Constants.UTF8));
			bwrepeats = 	new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infile.getAbsolutePath()+".repeats"),Constants.UTF8));

			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.contains(Constants.TAB)){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length<2){
					LOGGER.info("not valid line:\t"+ inputLine);
					continue;
				}
				l1 = attrs[0].trim();
				l2 = attrs[1].trim();
				bwl1.write(l1+"\n");
				bwl2.write(l2+"\n");

				if (l1.matches(".*[\\u4E00-\\u9FFF].*") || l2.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.matches(".*[\\u0600-\\u06FF].*") || l2.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.matches(".*[\\u0980-\\u09FF].*") || l2.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.matches(".*[\\u0C80-\\u0CFF].*") || l2.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					bwcharsets.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				nl1 = ContentNormalizer.normtext(l1);
				nl2 = ContentNormalizer.normtext(l2);
				if ( nl1.isEmpty() || nl2.isEmpty()){
					LOGGER.info(linecounter +"\tnon-letters:\t"+ inputLine);
					bwsymbols1.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (l1.equals(l2)){
					LOGGER.info(linecounter +"\texact identical TUVs:\t"+inputLine);
					bwidenTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (nl1.equals(nl2)){
					LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					bwidenTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				if (TMXHandlerUtils.checkemail(l1, 0.5) || TMXHandlerUtils.checkemail(l2, 0.5)){
					LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					bwemail.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (TMXHandlerUtils.checkurl(l1, 0.5) || TMXHandlerUtils.checkurl(l2, 0.5)){
					LOGGER.info(linecounter +"\turl:\t"+ inputLine);
					bwurl.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
								
				if (FCStringUtils.isAllUpperCase(l1) * FCStringUtils.isAllUpperCase(l2)<0){
					LOGGER.info(linecounter +"\tcase:\t"+ inputLine);
					bwcase.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				
				ratio = (float)l1.length()/(float)l2.length();
				if ( ratio>maxratio || ratio < minratio){
					LOGGER.info(linecounter +"\tratio:\t"+ratio+"\t"+ inputLine);
					bwratio.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				
				/*lang1 = langDetector.detect(l1);
				lang2 = langDetector.detect(l2);
				if (!(lang1.equals(langs[0]) && lang2.equals(langs[1]))){
					LOGGER.info(linecounter +"\tlangs:\t"+ inputLine);
					bwlang.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}*/
				
				int nl1count = FCStringUtils.countTokens(nl1);
				int nl2count = FCStringUtils.countTokens(nl2);
				if (nl1count<minTuvLen || nl2count<minTuvLen){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);
					bwshort.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				
				if (nl1count>maxTuvLen || nl2count>maxTuvLen){
					LOGGER.info(linecounter +"\tlong:\t"+ inputLine);
					bwlong.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				
				
				if (((float)nl1.length()/(float)l1.length())<0.6){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols2.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (((float)nl2.length()/(float)l2.length())<0.6){
					LOGGER.info(linecounter +"\tmostly non-letters:\t"+ inputLine);
					bwsymbols2.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				
				List<String> stokens = FCStringUtils.getTokens(l1);
				List<String> ttokens = FCStringUtils.getTokens(l2); 
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);
				if (Statistics.getMax(stokenslen)>40 || Statistics.getMax(ttokenslen)>40){
					LOGGER.info(linecounter + "\tmax word:\t"+ inputLine);
					bwmaxlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (Statistics.getMedian(stokenslen)>=25 || Statistics.getMedian(ttokenslen)>=25){
					LOGGER.info(linecounter + "\tlong median:\t"+ inputLine);
					bwmaxlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (Statistics.getMedian(stokenslen)<2 || Statistics.getMedian(ttokenslen)<2){
					LOGGER.info(linecounter + "\tshort median:\t"+ inputLine);
					bwminlen.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				int a = stokens.size();
				int b = getGrams(stokens).size();
				if (b<a){
					LOGGER.info(linecounter + "\trepeats:\t"+ inputLine);
					bwrepeats.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				a = ttokens.size();
				b = getGrams(ttokens).size();
				if (b<a){
					LOGGER.info(linecounter + "\trepeats:\t"+ inputLine);
					bwrepeats.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (incoherent(stokens)){
					LOGGER.info(linecounter + "\tincoherent TUV1:\t"+ inputLine);
					bwincoh.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if (incoherent(ttokens)){
					LOGGER.info(linecounter + "\tincoherent TUV2:\t"+ inputLine);
					bwincoh.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				/*found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (l1.contains(exclude_symbols[ii]) || l2.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbolsEXC:\t"+inputLine);
					bwexclsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}
				if ((l1.contains("??") && !l2.contains("??")) || (!l1.contains("??") && l2.contains("??"))) {
					LOGGER.info(linecounter +"\tspecial symbols??:\t"+inputLine);
					bwexclsymbols.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}*/
				
				
				//if (nenlist1.contains(nen)){
				//	LOGGER.info(linecounter + "\tdup TUV1:\t"+ inputLine);	
				//	continue;
				//}
				//if (ndelist1.contains(nde)){
				//	LOGGER.info(linecounter + "\tdup TUV2:\t"+ inputLine);	
				//	continue;
				//}
				num1=l1.replaceAll("\\D+","");
				num2=l2.replaceAll("\\D+","");
				if (num1.isEmpty() && num2.isEmpty()){
					if (attrs.length>2){
						double hunscore = Double.parseDouble(attrs[2]);
						if (hunscore<-0.3){
							LOGGER.info(linecounter + "\tlow hunscore:\t"+ inputLine);
							bwhunscore.write(linecounter+"\t"+inputLine+"\n");
							continue;
						}
					}
				}

				List<String> nstokens = FCStringUtils.getTokens(nl1); 
				List<String> nttokens = FCStringUtils.getTokens(nl2); 
				if (nearTUVs(nstokens, nttokens, 0.7) || nearTUVs(nttokens, nstokens, 0.7)){ //on TU level
					LOGGER.info(linecounter + "\tnear TUVs:\t"+ inputLine);
					bwnearTUVs.write(linecounter+"\t"+inputLine+"\n");
					continue;
				}

				tempcounter = FCStringUtils.countTokens(l1);
				counter0 = counter0 + tempcounter;
				bw0.write(linecounter + "\t"+inputLine+"\n");

				//nenlist1.add(nen);
				//ndelist1.add(nde);

				if (num1.equals(num2) ){
					if (!num1.isEmpty()){
						counter1 = counter1+tempcounter;
						bw1.write(linecounter + "\t"+inputLine+"\n");
					}else{
						counter1a = counter1a+tempcounter;
						bw1a.write(linecounter + "\t"+inputLine+"\n");
					}
				}
				String ensym = ContentNormalizer.leaveSymbols(l1); 
				String desym = ContentNormalizer.leaveSymbols(l2);
				if (ensym.equals(desym)){
					counter2 = counter2+tempcounter;
					bw2.write(linecounter + "\t"+inputLine+"\n");
				}
				if (num1.equals(num2) && ensym.equals(desym) ){
					if (!num1.isEmpty()){
						counter3 = counter3+tempcounter;
						bw3.write(linecounter + "\t"+inputLine+"\n");
					}else{
						counter3a = counter3a+tempcounter;
						bw3a.write(linecounter + "\t"+inputLine+"\n");
					}
				}
			}
			in.close();
			bw0.close();			bw1.close();			bw1a.close();			bw2.close();			bw3.close();			bw3a.close();
			bwl1.close();			bwl2.close();
			bwcharsets.close();		bwexclsymbols.close();	bwidenTUVs.close();		bwsymbols1.close();		bwsymbols1.close();		bwshort.close();		bwlong.close();
			bwlang.close();			bwratio.close();		bwemail.close();		bwurl.close();			bwcase.close();			bwmaxlen.close();
			bwminlen.close();		bwhunscore.close();		bwincoh.close();		bwnearTUVs.close();		bwrepeats.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("filtered corpus contains "+ counter0 + " tokens in "+ langs[0]);
		LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in "+ langs[0]);
		LOGGER.info("no number filtered corpus contains "+ counter1a + " tokens in "+ langs[0]);
		LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in "+ langs[0]);
		LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in "+ langs[0]);
		LOGGER.info("no number and same symbols filtered corpus contains "+ counter3a + " tokens in "+ langs[0]);
		return outs;
	}

	private static void buildcorpus(String[] args) throws IOException {
		File infile = new File(args[0]); 
		File outfile = new File(infile.getAbsolutePath()+".tmp");
		Set<String> envoc = new HashSet<String>();
		//Set<String> devoc = new HashSet<String>();
		String inputLine, nen;
		BufferedReader in;
		//BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));
		int linecounter=0, words=0;
		int thr=6;
		try {
			in = new BufferedReader(new FileReader(infile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;

				String[] attrs = inputLine.split(Constants.TAB);
				nen = ContentNormalizer.normtext(attrs[0]);

				List<String> stokens = FCStringUtils.getTokens(nen);
				List<String> grams = new ArrayList<String>();
				for (int ii=0;ii<stokens.size()-1;ii++){
					grams.add(stokens.get(ii));  												//1-gram
					grams.add(stokens.get(ii)+" "+stokens.get(ii+1));							//2-gram
					if (ii>0){
						grams.add(stokens.get(ii-1)+" "+stokens.get(ii)+" "+stokens.get(ii+1));	//3-gram
					}
				}
				int counter = grams.size();
				for (int ii=0;ii<grams.size();ii++){
					if (envoc.contains(grams.get(ii)))
						counter--;
				}
				if (counter<thr){
					System.out.println(inputLine);
					continue;
				}
				for (int ii=0;ii<grams.size();ii++){
					envoc.add(grams.get(ii));
				}
				bw.write(inputLine+"\n");
				words = words + stokens.size();
			}
			in.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	

	}

	private static File clusterbylength(File infile, int tuvid) throws IOException {

		File outdir = new File(FilenameUtils.concat(infile.getParent(),"bylen"));
		outdir.mkdir();

		Set<Integer> lenslist = new HashSet<Integer>();
		String inputLine;
		BufferedReader in;
		int linecounter=0, len;	
		try {
			in = new BufferedReader(new FileReader(infile));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				len = FCStringUtils.countTokens(ContentNormalizer.normtext(attrs[tuvid]));
				lenslist.add(len);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		Iterator<Integer> it = lenslist.iterator();
		int len1;
		while (it.hasNext()){
			len = it.next();
			System.out.println("len="+len);
			in = new BufferedReader(new FileReader(infile));
			File lenfile = new File(FilenameUtils.concat(outdir.getAbsolutePath(),Integer.toString(len)));
			//BufferedWriter  bw = new BufferedWriter(new FileWriter(lenfile));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lenfile.getAbsolutePath()),Constants.UTF8));
			while ((inputLine = in.readLine()) != null) {
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				len1 = FCStringUtils.countTokens(ContentNormalizer.normtext(attrs[tuvid]));
				if (len1==len){
					bw.write(inputLine+"\n");
				}
				lenslist.add(len);
			}
			in.close();
			bw.close();
		}
		return outdir;
	}

	/**
	 * It assumes that infile is a text file containing a parallel corpus
	 * "line id", "TUV1","TUV2", "hunscore" (TAB separated) 
	 * for each text line (segment pair), it generates the 1-3grams of the tupart TUV
	 * examines the list of n-grams with the list of all other segment pairs and identifies near-duplicates in case the   
	 * @param infile
	 * @param ext
	 * @param tupart : 1 for source TUV, 2 for target TUV
	 * @return
	 */

	private static File dedupWMT18(File infile, String ext, int tupart ) {
		File outfile = new File(infile.getAbsolutePath()+ext);
		File dedupdir = new File(FilenameUtils.concat(infile.getParent(), "dupTUV"+Integer.toString(tupart)));
		dedupdir.mkdir();
		String inputLine1, inputLine2;
		BufferedReader in1, in2;
		BufferedWriter bw;

		int  counter = 0, linecounter1=0, linecounter2=0 ;//, comma=0;
		String nde1, de1, nde2, de2;
		int THR = 6;
		try {
			in1 = new BufferedReader(new FileReader(infile));
			//bw = new BufferedWriter(new FileWriter(outfile));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));
			List<String> seen = new ArrayList<String>();
			while ((inputLine1 = in1.readLine()) != null) {
				linecounter1++;
				String[] attrs1 = inputLine1.split(Constants.TAB);
				String temp1 = attrs1[1]+"\t"+attrs1[2]+"\t"+attrs1[3];
				if (seen.contains(md5hash(temp1,3)))
					continue;

				de1 = attrs1[tupart];
				//score1 = Double.parseDouble(attrs1[2]);
				nde1 = ContentNormalizer.normtext(de1);
				List<String> stokens1 = FCStringUtils.getTokens(nde1);
				Set<String> grams1 = new HashSet<String>();
				for (int ii=0;ii<stokens1.size()-1;ii++){
					grams1.add(stokens1.get(ii));  												//1-gram
					grams1.add(stokens1.get(ii)+" "+stokens1.get(ii+1));						//2-gram
					if (ii>0){
						grams1.add(stokens1.get(ii-1)+" "+stokens1.get(ii)+" "+stokens1.get(ii+1));	//3-gram
					}
				}

				List<String> dups = new ArrayList<String>();
				dups.add(inputLine1);
				in2 = new BufferedReader(new FileReader(infile));
				linecounter2 = 0;
				while ((inputLine2 = in2.readLine()) != null) {
					if (linecounter2<linecounter1){
						linecounter2++;
						continue;
					}
					linecounter2++;
					//System.out.println(linecounter1 +"\t"+linecounter2);
					String[] attrs2 = inputLine2.split(Constants.TAB);
					de2 = attrs2[tupart];
					//score2 = Double.parseDouble(attrs2[2]);
					nde2 = ContentNormalizer.normtext(de2);
					List<String> stokens2 = FCStringUtils.getTokens(nde2);
					Set<String> grams2 = new HashSet<String>();
					for (int ii=0;ii<stokens2.size()-1;ii++){
						grams2.add(stokens2.get(ii));  												//1-gram
						grams2.add(stokens2.get(ii)+" "+stokens2.get(ii+1));						//2-gram
						if (ii>0){
							grams2.add(stokens2.get(ii-1)+" "+stokens2.get(ii)+" "+stokens2.get(ii+1));	//3-gram
						}
					}

					Set intersection = new HashSet(grams1);
					intersection.retainAll(grams2);
					int t1 = intersection.size();
					if ((Math.min(grams2.size(),grams1.size()) -  intersection.size()  ) < THR){
						//System.out.println(inputLine2);
						dups.add(inputLine2);
						String temp2 = attrs2[1]+"\t"+attrs2[2]+"\t"+attrs2[3];
						seen.add(md5hash(temp2,3));
					}
				}
				in2.close();
				if (dups.size()>1){
					File temp = new File(FilenameUtils.concat(dedupdir.getAbsolutePath(),"_dup-"+counter));
					FileUtils.writeLines(temp, Constants.UTF8, dups, "\n");
					counter++;
				}
				if (dups.size()==1){
					bw.write(inputLine1);
				}
			}
			in1.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//LOGGER.info("filtered corpus contains "+ counter + " tokens in lang1");
		//LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in lang1");
		//LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in lang1");
		//LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in lang1");
		return outfile;
	}

	private static void subsetWMT18(String[] args) {
		LOGGER.info("\nargs:\n 0=fullpath of infile\n 1=languages separated by ;\n 2=minTUVlen\n 3=minTUVratio\n 4=maxTUVratio\n 5=version");
		File infile = new File(args[0]); 

		String[] langs = args[1].split(Constants.SEMICOLON);
		langs[0]=ISOLangCodes.get3LetterCode(langs[0]);
		langs[1]=ISOLangCodes.get3LetterCode(langs[1]);	
		LangDetector langDetector = LangDetectUtils.loadLangDetectors(langs,"langdetect");

		int minTuvLen = Integer.parseInt(args[2]);
		float minratio = Float.parseFloat(args[3]);
		float maxratio = Float.parseFloat(args[4]);
		String ext = ".filt"+args[2]+"len_"+args[3]+"ra_"+args[4]+"ra_"+"dupTU_"+"dupTUV_"+"incoh";
		File outfile = new File(infile.getAbsolutePath()+ext);
		File outfile1 = new File(infile.getAbsolutePath()+ext+".samenum");
		File outfile2 = new File(infile.getAbsolutePath()+ext+".samesym");
		File outfile3 = new File(infile.getAbsolutePath()+ext+".samenumsym");
		File outfile4 = new File(infile.getAbsolutePath()+ext+"."+args[5]);
		File enfile = new File(infile.getAbsolutePath()+".en");
		File defile = new File(infile.getAbsolutePath()+".de");
		File scorefile = new File(infile.getAbsolutePath()+".score."+args[5]);
		File scoresamenumfile = new File(infile.getAbsolutePath()+".samenum.score."+args[5]);
		File scoresamesymfile = new File(infile.getAbsolutePath()+".samesym.score."+args[5]);
		File scoresamenumsymfile = new File(infile.getAbsolutePath()+".samenumsym.score."+args[5]);

		String inputLine;
		BufferedReader in;
		BufferedWriter  bw ,bw1, bw2, bw3, bw4, bwen, bwde, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym;

		int  counter = 0, counter1= 0, counter2= 0, counter3 = 0, tempcounter = 0, linecounter=0 ;//, comma=0;
		String lang1, lang2, nen, nde, num1, num2, en, de;
		float ratio;
		boolean found = false;
		Set<String> nenlist1 = new HashSet<String>();
		Set<String> ndelist1 = new HashSet<String>();

		Set<List<String>> nenlist = new HashSet<List<String>>();
		Set<List<String>> ndelist = new HashSet<List<String>>();

		try {
			in = new BufferedReader(new FileReader(infile));
			/*bw = new BufferedWriter(new FileWriter(outfile));
			bw1 = new BufferedWriter(new FileWriter(outfile1));
			bw2 = new BufferedWriter(new FileWriter(outfile2));
			bw3 = new BufferedWriter(new FileWriter(outfile3));
			bw4 = new BufferedWriter(new FileWriter(outfile4));
			bwen = new BufferedWriter(new FileWriter(enfile));
			bwde = new BufferedWriter(new FileWriter(defile));
			bwscore = new BufferedWriter(new FileWriter(scorefile));
			bwscoresamenum = new BufferedWriter(new FileWriter(scoresamenumfile));
			bwscoresamesym = new BufferedWriter(new FileWriter(scoresamesymfile));
			bwscoresamenumsym = new BufferedWriter(new FileWriter(scoresamenumsymfile));*/
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));
			bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile1.getAbsolutePath()),Constants.UTF8));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile2.getAbsolutePath()),Constants.UTF8));
			bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile3.getAbsolutePath()),Constants.UTF8));
			bw4 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile4.getAbsolutePath()),Constants.UTF8));
			bwen = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(enfile.getAbsolutePath()),Constants.UTF8));
			bwde = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defile.getAbsolutePath()),Constants.UTF8));
			bwscore = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scorefile.getAbsolutePath()),Constants.UTF8));
			bwscoresamenum = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scoresamenumfile.getAbsolutePath()),Constants.UTF8));
			bwscoresamesym = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scoresamesymfile.getAbsolutePath()),Constants.UTF8));
			bwscoresamenumsym = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scoresamenumsymfile.getAbsolutePath()),Constants.UTF8));

			while ((inputLine = in.readLine()) != null) {
				if (!inputLine.contains(Constants.TAB)){
					LOGGER.info("not valid line:\t"+ inputLine);
					bw4.write(inputLine+"\n");
					continue;
				}
				linecounter++;
				String[] attrs = inputLine.split(Constants.TAB);
				if (attrs.length!=3){
					LOGGER.info("not valid line:\t"+ inputLine);
					bw4.write(inputLine+"\n");
					continue;
				}
				en = attrs[0];
				de = attrs[1];
				bwen.write(en+"\n");
				bwde.write(de+"\n");
				if (en.matches(".*[\\u4E00-\\u9FFF].*") || de.matches(".*[\\u4E00-\\u9FFF].*")){
					LOGGER.info(linecounter +"\tchinese:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.matches(".*[\\u0600-\\u06FF].*") || de.matches(".*[\\u0600-\\u06FF].*")){ 
					LOGGER.info(linecounter +"\tarabic:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.matches(".*[\\u0980-\\u09FF].*") || de.matches(".*[\\u0980-\\u09FF].*")){ 
					LOGGER.info(linecounter +"\tbengali:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.matches(".*[\\u0C80-\\u0CFF].*") || de.matches(".*[\\u0C80-\\u0CFF].*")){
					LOGGER.info(linecounter +"\tKannada:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				found = false;
				for (int ii=0;ii<exclude_symbols.length;ii++){
					if (en.contains(exclude_symbols[ii]) || de.contains(exclude_symbols[ii]) ){
						found = true;
						break;
					}
				}
				if (found){
					LOGGER.info(linecounter +"\tspecial symbols:\t"+inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (en.equals(de)){
					LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				nen = ContentNormalizer.normtext(en);
				if (((float)nen.length()/(float)en.length())<0.6){
					LOGGER.info(linecounter +"\tmany non-letters:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				nde = ContentNormalizer.normtext(de);
				if (((float)nde.length()/(float)de.length())<0.6){
					LOGGER.info(linecounter +"\tmany non-letters:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if ( nen.isEmpty() || nde.isEmpty()){
					LOGGER.info(linecounter +"\tsymbols:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (nen.equals(nde)){
					LOGGER.info(linecounter +"\tidentical TUVs:\t"+inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				int nencount = FCStringUtils.countTokens(nen);
				int ndecount = FCStringUtils.countTokens(nde);
				if (nencount<minTuvLen || ndecount<minTuvLen){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (nencount>70 || ndecount>70){
					LOGGER.info(linecounter +"\tshort:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");	
					continue;
				}
				lang1 = langDetector.detect(en);
				lang2 = langDetector.detect(de);
				if (!(lang1.equals(langs[0]) && lang2.equals(langs[1]))){
					LOGGER.info(linecounter +"\tlangs:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}

				ratio = (float)en.length()/(float)de.length();
				if ( ratio>maxratio || ratio < minratio){
					LOGGER.info(linecounter +"\tratio:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (FCStringUtils.isAllUpperCase(en) * FCStringUtils.isAllUpperCase(de)<0){
					LOGGER.info(linecounter +"\tcase:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (TMXHandlerUtils.checkemail(en, 0.5) || TMXHandlerUtils.checkemail(de, 0.5)){
					LOGGER.info(linecounter +"\te-mail:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (TMXHandlerUtils.checkurl(en, 0.5) || TMXHandlerUtils.checkurl(de, 0.5)){
					LOGGER.info(linecounter +"\turl:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}

				//temp_ende = md5hash(nen+" "+nde,3);
				//if (nennde.contains(temp_ende)){
				//	LOGGER.info(linecounter +"\tdup TU:\t"+inputLine);
				//	continue;
				//}//else{
				//	nennde.add(temp_ende);
				//}
				//temp_en = md5hash(nen,3);
				//if (nenlist.contains(temp_en)){
				//	LOGGER.info(linecounter +"\tdup TUV1:\t"+ inputLine);
				//	continue;
				//}//else{
				//	nenlist.add(temp_en);
				//}
				//temp_de = md5hash(nde,3);
				//if (ndelist.contains(temp_de)){
				//	LOGGER.info(linecounter +"\tdup TUV2:\t"+ inputLine);
				//	continue;
				//}//else{
				//	ndelist.add(temp_de);
				//}

				List<String> stokens = FCStringUtils.getTokens(en); 
				List<String> ttokens = FCStringUtils.getTokens(de); 
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);
				if (Statistics.getMax(stokenslen)>40 || Statistics.getMax(ttokenslen)>40){
					LOGGER.info(linecounter + "\tmax word:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (Statistics.getMedian(stokenslen)>=25 || Statistics.getMedian(ttokenslen)>=25){
					LOGGER.info(linecounter + "\tlong median:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (Statistics.getMedian(stokenslen)<3 || Statistics.getMedian(ttokenslen)<3){
					LOGGER.info(linecounter + "\tshort median:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (nenlist1.contains(nen)){
					LOGGER.info(linecounter + "\tdup TUV1:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				if (ndelist1.contains(nde)){
					LOGGER.info(linecounter + "\tdup TUV2:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0");
					continue;
				}
				num1=en.replaceAll("\\D+","");
				num2=de.replaceAll("\\D+","");
				if (num1.isEmpty() && num2.isEmpty()){
					double hunscore = Double.parseDouble(attrs[2]);
					if (hunscore<-0.3){
						LOGGER.info(linecounter + "\tlow hunscore:\t"+ inputLine);
						addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.3");
						continue;
					}
				}

				if (incoherent(stokens)){
					LOGGER.info(linecounter + "\tincoherent TUV1:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.5");
					continue;
				}
				if (incoherent(ttokens)){
					LOGGER.info(linecounter + "\tincoherent TUV2:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.5");	
					continue;
				}

				List<String> nstokens = FCStringUtils.getTokens(nen); 
				List<String> nttokens = FCStringUtils.getTokens(nde); 

				if (nearTUVs(nstokens, nttokens, 0.7) || nearTUVs(nttokens, nstokens, 0.7)){ //on TU level
					LOGGER.info(linecounter + "\tnear TUVs:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.6");
					continue;
				}

				if (nearTUVsList(nenlist, nstokens, 0.7)){ //across TUV1
					LOGGER.info(linecounter + "\tnear TUV1s:\t"+ inputLine);	
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.6");	
					continue;
				}
				if (nearTUVsList(ndelist, nttokens, 0.7)){ //across TUV2
					LOGGER.info(linecounter + "\tnear TUV2s:\t"+ inputLine);
					addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, "0.6");	
					continue;
				}

				tempcounter = FCStringUtils.countTokens(en);
				counter = counter + tempcounter;
				bw.write(inputLine+"\n");
				String ss =  Integer.toString(nencount + ndecount);
				//addlines(bw4, bwscore, bwscoresamenum, bwscoresamesym, bwscoresamenumsym, en , de, ss);					
				bw4.write(en+"\t"+de+"\t"+ss+"\n");
				bwscore.write(ss+"\n");

				//nennde.add(temp_ende);
				nenlist1.add(nen);
				ndelist1.add(nde);
				nenlist.add(nstokens);
				ndelist.add(nttokens);

				if (num1.equals(num2) ){
					counter1 = counter1+tempcounter;
					bw1.write(inputLine+"\n");
					bwscoresamenum.write(ss+"\n");
				}else{
					bwscoresamenum.write("0\n");
				}
				String ensym = ContentNormalizer.leaveSymbols(en); 
				String desym = ContentNormalizer.leaveSymbols(de);
				if (ensym.equals(desym)){
					counter2 = counter2+tempcounter;
					bw2.write(inputLine+"\n");
					bwscoresamesym.write(ss+"\n");
				}else{
					bwscoresamesym.write("0\n");
				}
				if (num1.equals(num2) && ensym.equals(desym) ){
					counter3 = counter3+tempcounter;
					bw3.write(inputLine+"\n");
					bwscoresamenumsym.write(ss+"\n");
				}else{
					bwscoresamenumsym.write("0\n");
				}
			}
			in.close();
			bw.close();
			bw1.close();
			bw2.close();
			bw3.close();
			bw4.close();
			bwen.close();
			bwde.close();
			bwscore.close();
			bwscoresamenum.close();
			bwscoresamesym.close();
			bwscoresamenumsym.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("filtered corpus contains "+ counter + " tokens in "+ langs[0]);
		LOGGER.info("same number filtered corpus contains "+ counter1 + " tokens in "+ langs[0]);
		LOGGER.info("same symbols filtered corpus contains "+ counter2 + " tokens in "+ langs[0]);
		LOGGER.info("same number and symbols filtered corpus contains "+ counter3 + " tokens in "+ langs[0]);

	}

	private static void test_enpl_paracrawl() throws IOException {

		File enfile = new File("C:/Users/vpapa/ELRC/paracrawl/paracrawl-release1.en-pl.zipporah0-dedup-clean/paracrawl-release1.en-pl.zipporah0-dedup-clean.en");
		File plfile = new File("C:/Users/vpapa/ELRC/paracrawl/paracrawl-release1.en-pl.zipporah0-dedup-clean/paracrawl-release1.en-pl.zipporah0-dedup-clean.pl");
		int ind = enfile.getAbsolutePath().indexOf(".");
		File cutsfile = new File(enfile.getAbsolutePath().substring(0, ind)+".cut");
		File outfile = new File(enfile.getAbsolutePath().substring(0, ind)+".filt");
		//BufferedWriter bwcut = new BufferedWriter(new FileWriter(cutsfile));
		//BufferedWriter bwout = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter bwcut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cutsfile.getAbsolutePath()),Constants.UTF8));
		BufferedWriter bwout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),Constants.UTF8));

		List<String> ens = FileUtils.readLines(enfile, Constants.UTF8);
		List<String> pls = FileUtils.readLines(plfile, Constants.UTF8);
		String en1="", pl1="", nen1="", npl1="", nennpl="";
		List<String> list = new ArrayList<String>();
		boolean found;
		for (int ii=0;ii<ens.size();ii++){
			en1=ens.get(ii);
			pl1=pls.get(ii);
			if (en1.matches(".*[\\u4E00-\\u9FFF].*") || pl1.matches(".*[\\u4E00-\\u9FFF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (en1.matches(".*[\\u0600-\\u06FF].*") || pl1.matches(".*[\\u0600-\\u06FF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (en1.matches(".*[\\u0980-\\u09FF].*") || pl1.matches(".*[\\u0980-\\u09FF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (en1.matches(".*[\\u0C80-\\u0CFF].*") || pl1.matches(".*[\\u0C80-\\u0CFF].*")){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			found = false;
			for (int jj=0;jj<exclude_symbols.length;jj++){
				if (en1.contains(exclude_symbols[jj]) || pl1.contains(exclude_symbols[jj]) ){
					found = true;
					break;
				}
			}
			if (found){
				bwcut.write("symbols\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			nen1 = ContentNormalizer.normtext(en1);
			npl1 = ContentNormalizer.normtext(pl1);
			if (nen1.isEmpty() || npl1.isEmpty()){
				bwcut.write("no letters\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (nen1.equals(npl1)){
				bwcut.write("identical\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (TMXHandlerUtils.checkemail(en1, 0.5) || TMXHandlerUtils.checkemail(pl1, 0.5)){
				bwcut.write("email\t"+en1+"\t"+pl1+"\n");
				continue;
			}
			if (TMXHandlerUtils.checkurl(en1, 0.5) || TMXHandlerUtils.checkurl(pl1, 0.5)){
				bwcut.write("url\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			List<String> stokens = FCStringUtils.getTokens(nen1); 
			List<String> ttokens = FCStringUtils.getTokens(npl1); 
			if (stokens.size()<2 || ttokens.size()<2){
				bwcut.write("short\t"+en1+"\t"+pl1+"\n");
				continue;
			}

			nennpl = nen1+"\t"+npl1;
			if (list.contains(nennpl)){
				bwcut.write("dups\t"+en1+"\t"+pl1+"\n");
				continue;
			}else{
				list.add(nennpl);
				bwout.write(en1+"\t"+pl1+"\n");
			}
		}
		bwcut.close();
		bwout.close();
	}

	private static void addlines(BufferedWriter bw4, BufferedWriter bwscore,	BufferedWriter bwscoresamenum, 	BufferedWriter bwscoresamesym, BufferedWriter bwscoresamenumsym,
			String en, String de, String score) throws IOException {

		bw4.write(en+"\t"+de+"\t"+score+"\n");
		bwscore.write(score+"\n");
		bwscoresamenum.write(score+"\n");
		bwscoresamesym.write(score+"\n");
		bwscoresamenumsym.write(score+"\n");
	}

	private static boolean nearTUVsList(Set<List<String>> nenlist, 	List<String> nstokens, double d) {

		Iterator<List<String>> it = nenlist.iterator();
		List<String> t = new ArrayList<String>();
		boolean found  = false;
		while (it.hasNext()){
			t = it.next();
			if (nearTUVs(t, nstokens, 0.7) || nearTUVs(nstokens, t, 0.7)){
				found = true;
				break;
			}	
		}
		return found;
	}

	private static boolean nearTUVs(List<String> nttokens, List<String> nstokens, double thr) {
		double count=0;
		for (int ii=0;ii<nstokens.size();ii++){
			if (nttokens.contains(nstokens.get(ii)))
				count++;
		}
		if (count/(double)(nstokens.size())>=thr)
			return true;
		return false;
	}

	private static boolean incoherent(List<String> stokens) {
		int comma = 0;
		for (int kk=0;kk<stokens.size();kk++){
			if (stokens.get(kk).endsWith(","))
				comma++;
		}
		if (((float)comma/(float)stokens.size())>0.4){
			return true;
		}
		return false;
	}

	private static String md5hash(String text, int MIN_TOKEN_LEN) {
		String string_key="";
		byte[] texthashkey = DedupUtils.calculateMD5(text, MIN_TOKEN_LEN); //TODO should text be stemmed?
		for (int jj=0;jj<texthashkey.length;jj++) {
			string_key += texthashkey[jj];
		}
		return string_key;
	}
}
