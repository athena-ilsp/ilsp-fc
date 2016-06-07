package gr.ilsp.fc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Tu;

public class Tmx2Tsv {

	private static final Logger logger = LoggerFactory.getLogger(Tmx2Tsv.class);
	private static final String NEW_LINE = "\n";
	private static final String TAB_SEPARATOR = "\t";
	private static final String SPACE_SEPARATOR = " ";
	private final static String SCORE = "score";
	private final static String SEGMENTTYPE = "type";
	private final static String LENGTHRATIO = "lengthRatio";
	private final static String INFO = "info";

	public static void tmx2Tsv(File tmxFile, File evalFile, String l1, String l2) throws IOException  {
		List<Tu> tus = TMXHandlerUtils.getTUs(tmxFile);
		List<String> outLines = new ArrayList<String>();
		for (Tu tu : tus) {
			String l1Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l1), SPACE_SEPARATOR);
			String l2Text = StringUtils.join(TMXHandlerUtils.createSegmentList(tu, l2), SPACE_SEPARATOR);
			List<Object> tuProps = tu.getNoteOrProp();
			String type="";
			String score = "";
			String lengthratio = "";
			String info = "";
			for (Object obProp : tuProps) {
				Prop prop = (Prop) obProp;
				if (prop.getType().equals(SCORE)) {
					//meanAlignmentScore = meanAlignmentScore + Double.parseDouble(prop.getContent().get(0));
					score = prop.getContent().get(0);
				} else if (prop.getType().equals(SEGMENTTYPE)) {
					type = prop.getContent().get(0);
				} else if (prop.getType().equals(LENGTHRATIO)) {
					lengthratio = prop.getContent().get(0);
				} else if (prop.getType().equals(INFO) && (!prop.getContent().isEmpty())) {
					info = prop.getContent().get(0);
				}
			}
			outLines.add(StringUtils.join(new String[] {l1Text, l2Text, String.valueOf(score), type, lengthratio, info }, TAB_SEPARATOR));
			break;
		}
		FileUtils.write (evalFile, StringUtils.join(new String[] {l1, l2, "aligner-score", "type", "lengthratio", "info" }, TAB_SEPARATOR)+NEW_LINE, false);
		FileUtils.writeLines(evalFile, outLines, true);
	}
	
	public static void main(String[] args) throws IOException {
		File tmxFile = new File(args[0]);
		File evalFile = new File(args[1]);
		String l1=args[2];
		String l2=args[3];
		logger.info("Reading " + tmxFile.getAbsolutePath());
		logger.info("Languages: " + l1 + " " + l2 + " ");
		tmx2Tsv(tmxFile, evalFile, l1, l2);
		logger.info("Done writing to " + evalFile.getAbsolutePath());
		
	}
	
}
