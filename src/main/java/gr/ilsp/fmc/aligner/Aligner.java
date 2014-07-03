package gr.ilsp.fmc.aligner;


import org.apache.log4j.Logger;

public class Aligner {
	private static final Logger LOGGER = Logger.getLogger(Aligner.class);
	//private String _alingerName;
	//private String _lang1;
	//private String _lang2;
	//private String _cesAlignList;
	
	
	public static void aling(String alingerName, String l1, String l2, String dict, String cesAlignList){
		
		LOGGER.info("Alinger: "+ alingerName);
		LOGGER.info("Languages: " + l1 + " "+ l2);
		LOGGER.info("Dictionary: " + dict);
		LOGGER.info("cesAlignList: " + cesAlignList);
		
	}
}
