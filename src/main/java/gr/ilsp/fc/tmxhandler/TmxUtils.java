package gr.ilsp.fc.tmxhandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.nlp.commons.Constants;

public class TmxUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmxUtils.class);
	private static final String TOK_EXT = ".tok";
	private static final String TXT_EXT = ".txt";
	private static final String LIST = "list";
	private final static String SAMPLE = ".sample";
	private final static String SITES = ".sites";

	public static void main(String[] args) throws IOException {
		LOGGER.info("\n1st argument: the targeted TMX file\n"
				+ "2nd argument: the \"source\" language\n"
				+ "3rd argument: the \"target\" language\n"
				+ "4th argument: the annotation type to exclude (i.e. some of \"short;ratio;equal;duplicate;address;numbers;letters\" or \"\" for getting all)\n"
				+ "5th argument: the absolute sample size\n");
		File tmxFile = new File(args[0]).getAbsoluteFile();
		LOGGER.info("Reading " + tmxFile.getAbsolutePath());
		String lang1=args[1];
		String lang2=args[2];
		LOGGER.info("Languages: " + lang1 + Constants.SPACE + lang2 + Constants.SPACE);
		List<String> info = new ArrayList<String>(); 	//List<String> info = Arrays.asList(new String[] {"short", "ratio", "equal", "duplicate", "address", "numbers", "letters" });
		if (!args[3].equals("\"\"")){
			String[] temp= args[3].split(";");
			info =Arrays.asList(temp);
		}
		int samplesize = Integer.parseInt(args[4]);

		//---------------list of Segment Pairs
		List<SegPair> segs = TMXHandlerUtils.getTUsFromTMX(tmxFile, info, lang1, lang2);
		List<String> segsl1 = new ArrayList<String>();
		List<String> segsl2 = new ArrayList<String>();
		for (SegPair seg:segs){
			segsl1.add(seg.seg1);
			segsl2.add(seg.seg2);
		}
		
		//---------------tokenized segments in 2 two text files (1 for each language)
		List<List<String>> tokedsegs = TMXHandlerUtils.getTokedSegs(segsl1, segsl2, lang1,lang2);
		File l1file= new File(tmxFile.getAbsolutePath()+TOK_EXT+lang1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for tokenized segments in "+lang1);
		FileUtils.writeLines(l1file, tokedsegs.get(0));
		File l2file= new File(tmxFile.getAbsolutePath()+TOK_EXT+lang2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for tokenized segments in "+lang2);
		FileUtils.writeLines(l2file, tokedsegs.get(1));

		//--------------segments in 2 two text files (1 for each language)
		l1file= new File(tmxFile.getAbsolutePath()+Constants.DOT+lang1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for segments in "+lang1);
		FileUtils.writeLines(l1file, segsl1);
		l2file= new File(tmxFile.getAbsolutePath()+Constants.DOT+lang2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for segments in "+lang2);
		FileUtils.writeLines(l2file, segsl2);

		//----------segments and props in a text file (tab separated)
		File tsvFile = new File(tmxFile.getAbsolutePath()+Constants.DOT+LIST);
		List<String> outLines = TMXHandlerUtils.getSegsAndProps(segs, lang1, lang2);
		FileUtils.writeLines(tsvFile, outLines);

		//------------Sample---------------------------
		List<String> sampleSegs = TMXHandlerUtils.getSampleSegs(segs, samplesize);
		File sampleFile = new File(tmxFile.getAbsolutePath()+SAMPLE+samplesize+TXT_EXT);
		FileUtils.writeLines(sampleFile, sampleSegs);

		//---------------- Sites
		List<String> sites = TMXHandlerUtils.getSitesFromSegPairs(segs, false);
		File sitesFile = new File(tmxFile.getAbsolutePath()+SITES+samplesize+TXT_EXT);
		FileUtils.writeLines(sitesFile, sites);
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
	
	/**
	 * parses a tmxfile, get segments in l1 and l2, and writes these segments in two text files (1 for each language)
	 * @param tmxFile
	 * @param l1
	 * @param l2
	 * @throws IOException
	 *//*
	private static void tmx2TXTs(File tmxFile, String l1, String l2) throws IOException {
		List<Tu> tus = TMXHandlerUtils.getTUs(tmxFile);
		List<String> l1segs = new ArrayList<String>();
		List<String> l2segs = new ArrayList<String>();
		for (Tu tu : tus) {
			l1segs.add(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l1), SPACE_STR));
			l2segs.add(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l2), SPACE_STR));
		}
		File l1file= new File(tmxFile.getAbsolutePath()+PUNCT_STR+l1);
		LOGGER.info("Writing file "+ l1file.getAbsolutePath() + " for segments in "+l1);
		FileUtils.writeLines(l1file, l1segs);
		File l2file= new File(tmxFile.getAbsolutePath()+PUNCT_STR+l2);
		LOGGER.info("Writing file "+ l2file.getAbsolutePath() + " for segments in "+l2);
		FileUtils.writeLines(l2file, l2segs);
	}*/


/*	public static void tmx2Tsv(File tmxFile, File evalFile, String l1, String l2) throws IOException  {
		List<Tu> tus = TMXHandlerUtils.getTUs(tmxFile);
		List<String> outLines = new ArrayList<String>();
		int i = 1;
		for (Tu tu : tus) {
			String l1Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l1), SPACE_STR);
			String l2Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l2), SPACE_STR);
			List<Object> tuProps = tu.getNoteOrProp();
			String type="NULL";
			String score = "NULL";
			String lengthratio = "NULL";
			String info = "NULL";
			for (Object obProp : tuProps) {
				Prop prop = (Prop) obProp;
				if (prop.getType().equals(SCORE)) {
					score = prop.getContent().get(0);
				} else if (prop.getType().equals(SEGMENTTYPE)) {
					type = prop.getContent().get(0);
				} else if (prop.getType().equals(LENGTHRATIO)) {
					lengthratio = prop.getContent().get(0);
				} else if (prop.getType().equals(INFO) && (!prop.getContent().isEmpty())) {
					info = prop.getContent().get(0);
				}
			}
			outLines.add(StringUtils.join(new String[] {String.valueOf(i), l1Text, l2Text, String.valueOf(score), lengthratio, info }, TAB_STR));
			i++;
		}
		LOGGER.info("Writing file "+ evalFile.getAbsolutePath() + " with all results and information.");
		FileUtils.write (evalFile, StringUtils.join(new String[] {"id", l1, l2, "alignerScore", "lengthRatio", "info" }, TAB_STR)+NEWLINE_STR, false);
		FileUtils.writeLines(evalFile, outLines, true);
	}*/


	/*	*//**
	 * parses a tmxfile, get segments in l1 and l2, and writes these segments in two text files (1 for each language)
	 * @param tmxfile
	 * @param l1
	 * @param l2
	 *//*

	public static void tmx2txts(File tmxfile, String l1, String l2){
		File l1File = new File(FilenameUtils.concat(tmxfile.getParent(),l1+textext));
		File l2File = new File(FilenameUtils.concat(tmxfile.getParent(),l2+textext));
		List<String> sourcesegs = new ArrayList<String>();
		List<String> l1segs =  new ArrayList<String>();
		List<String> targetsegs = new ArrayList<String>();
		List<String> l2segs =  new ArrayList<String>();

		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(tmxfile.getAbsolutePath()), UTF_8);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("problem in reading due to encoding issue");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			LOGGER.error(tmxfile.getAbsolutePath()+" file not found");
			e.printStackTrace();
		}
		BilingualScoredTmxParser parser = new BilingualScoredTmxParser(reader, l1,l2);
		List<Alignment> listalign= parser.parse();
		LOGGER.info("Alignments : " + listalign.size());

		int coun=0, ind=-1;
		for (Alignment alignment:listalign){
			ind=ind+1;
			if ((ind/1000)>coun){
				LOGGER.info(Integer.toString(coun));
				coun++;
			}
			sourcesegs = alignment.getSourceSegmentList();
			if (sourcesegs.size()>0)
				l1segs.add(sourcesegs.get(0));
			targetsegs = alignment.getTargetSegmentList();
			if (targetsegs.size()>0)
				l1segs.add(targetsegs.get(0));
		}
		try {
			FileUtils.writeLines(l1File, l1segs);
			FileUtils.writeLines(l2File, l2segs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/


}
