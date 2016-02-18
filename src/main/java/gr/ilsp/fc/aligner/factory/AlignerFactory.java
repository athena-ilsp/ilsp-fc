package gr.ilsp.fc.aligner.factory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignerFactory {

	private String[] alignerIds = {"hunalign","maligna"};
	private static final Logger logger = LoggerFactory.getLogger(AlignerFactory.class);
	private static boolean xslt = true;
	
	public Aligner getAligner(String aligner) {
		if (aligner.equalsIgnoreCase("hunalign")) {
			return new HunalignAligner();
		} else if (aligner.equalsIgnoreCase("maligna")) {
			return new MalignaAligner();
		} else if (aligner.equalsIgnoreCase("default")) {
			return new MalignaAligner();
		} else {
			logger.warn("Aligner " + aligner + " not among known aligners: " + Arrays.toString(alignerIds));
			logger.warn("Using default aligner: " + MalignaAligner.class.getName());
			return new MalignaAligner();
		}
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException {
		String alignerStr = args[0];
		String sourceLang = args[1];		
		String targetLang = args[2];		
		File cesAlignList = new File(args[3]);		
		File tmxList = new File(args[4]);		
		File tmxHtmlList = new File(args[5]);
		try {
			AlignerFactory alignerFactory = new AlignerFactory();
			Aligner aligner = alignerFactory.getAligner(alignerStr);
			Properties properties = new Properties();
			properties.setProperty("sourceLang", sourceLang);			
			properties.setProperty("targetLang", targetLang);			
			if (alignerStr.equalsIgnoreCase("hunalign")){
				properties.setProperty("aligner_runnable_path", args[6]);
				properties.setProperty("dictalign_path", args[7]);
			}
			aligner.initialize(properties);
			aligner.processCesAlignList(cesAlignList, tmxList, tmxHtmlList,xslt);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

}
