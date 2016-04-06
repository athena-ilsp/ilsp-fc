package gr.ilsp.fc.aligner.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignerFactory {

	private String[] alignerIds = {"hunalign","maligna"};
	private static final Logger logger = LoggerFactory.getLogger(AlignerFactory.class);
	//private static boolean xslt = true;
	
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

	/*public static void main(String[] args) throws IOException, ParserConfigurationException {
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
			//aligner.processCesAlignList(cesAlignList, tmxList, tmxHtmlList,xslt);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}*/
	public static void main(String[] args) throws IOException, ParserConfigurationException {
		String alignerStr = args[0];
		String sourceLang = args[1];		
		String targetLang = args[2];		

		//File tmxList = new File(args[4]);		
		//File tmxHtmlList = new File(args[5]);
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
			List<Pair<File, File>> l1L2Files=new ArrayList<Pair<File, File>>();
			ArrayList<String> filePairs=IOtools.readFileToArray(args[3]);
			for(String pair:filePairs){
				String[] files=pair.split(" ");
				Pair<File, File> newPair= Pair.of(new File((new File(args[3]).getParent())+File.separator+files[0]), new File((new File(args[3]).getParent())+File.separator+files[1]));
				l1L2Files.add(newPair);
				
			}
			aligner.processl1L2Files(l1L2Files, sourceLang, targetLang);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
}
