package gr.ilsp.fc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.nlp.commons.Constants;
import net.loomchild.maligna.util.bind.tmx.Tu;

public class EvalSetExtractor {

	private static final Logger logger = LoggerFactory.getLogger(EvalSetExtractor.class);
	private static final String NEW_LINE = "\n";
		
	public static void extractTUsForEvaluation(File tmxFile, File evalFile, String l1, String l2, int n ) throws IOException  {
		List<Tu> tus = TMXHandlerUtils.getTUs(tmxFile);
		Random rand = new Random();
		Collections.shuffle(tus, rand);
		List<String> outLines = new ArrayList<String>();
		int tmx_i = 1;
		for (Tu tu : tus) {
			String l1Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l1), Constants.SPACE);
			String l2Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l2), Constants.SPACE);
			outLines.add(StringUtils.join(new String[] {l1Text, l2Text, Constants.EMPTY_STRING }, Constants.TAB));
			if (tmx_i++ >= n) {
				break;
			}
		}
		FileUtils.write (evalFile, StringUtils.join(new String[] {l1, l2, "Score" }, Constants.TAB)+NEW_LINE, false);
		FileUtils.writeLines(evalFile, outLines, "\n",true);
	}
	
	public static void main(String[] args) throws IOException {
		File tmxFile = new File(args[0]);
		File evalFile = new File(args[1]);
		String l1=args[2];
		String l2=args[3];
		int n = Integer.parseInt(args[4]); 
		logger.info(tmxFile.getAbsolutePath());
		logger.info(l1 + Constants.SPACE + l2 + Constants.SPACE + n);
		logger.info(evalFile.getAbsolutePath());
		extractTUsForEvaluation(tmxFile, evalFile, l1, l2, n);
	}
	
}
