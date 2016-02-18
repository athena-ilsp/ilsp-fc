package gr.ilsp.fc.dedup;

import java.io.File;

import org.apache.log4j.Logger;

import gr.ilsp.fc.dedup.DedupMD5;

public class DedupMain {
	private static final Logger LOGGER = Logger.getLogger(DedupMain.class);
	private static String methodtype;
	private static double inter_thr; //intersection of common paragraphs
	private static int MIN_TOK_LEN; //tokens with less that MIN_TOK_LEN letters are excluded
	private static int MIN_PAR_LEN; //paragraphs with less than min_tok_num are excluded

	public static void main(String[] args) {
		File outputDirName =new File(args[0]);
		File file1=null, file2=null;
		String inputtype= args[1];
	
		if (args.length>2)
			methodtype = args[2];
		else
			methodtype ="0";
		if (args.length>3)
			file1 = new File(args[3]);
		if (args.length>4)
			file2 = new File(args[4]);
		if (args.length>5)
			inter_thr = Double.parseDouble(args[5]);
		else
			inter_thr = 0.7;

		if (args.length>6)
			MIN_TOK_LEN = Integer.parseInt(args[6]);
		else
			MIN_TOK_LEN = 3;

		if (args.length>7)
			MIN_PAR_LEN = Integer.parseInt(args[7]);
		else
			MIN_PAR_LEN = 3;

		//FIXME pass these thresholds to the methods
		if (methodtype.equals("1")){
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(outputDirName, null, file1,file2,true, MIN_TOK_LEN, inputtype);
		}
		if (methodtype.equals("2")){
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupParsMD5.dedup(outputDirName, null, file1,file2,true,MIN_PAR_LEN, inter_thr, inputtype);
		}
		if (methodtype.equals("0")){
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(outputDirName, null, file1,file2,true, MIN_TOK_LEN, inputtype);
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupParsMD5.dedup(outputDirName, null, file1,file2,true, MIN_PAR_LEN, inter_thr, inputtype);
		}
		System.out.println("dedup done.");
	}


}
