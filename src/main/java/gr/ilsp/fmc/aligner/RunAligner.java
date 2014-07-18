package gr.ilsp.fmc.aligner;


import org.apache.log4j.Logger;

public class RunAligner {
	private static final Logger LOGGER = Logger.getLogger(RunAligner.class);
	//private String _alingerName;
	//private String _lang1;
	//private String _lang2;
	//private String _cesAlignList;
	
	
	public static void align(String alingerName, String l1, String l2, String runaling_path, String dictalign_path, String dict, String cesAlignList){
		
		LOGGER.info("Alinger: "+ alingerName);
		LOGGER.info("Run_aligner: "+ runaling_path);
		//LOGGER.info("Use_dict: "+ dictalign_path);
		LOGGER.info("Languages: " + l1 + " "+ l2);
		LOGGER.info("Dictionary: " + dict);
		LOGGER.info("Dictionary_path: " + dictalign_path);
		//LOGGER.info("cesAlignList: " + cesAlignList);

		if (alingerName.equals("default")){
			ScriptAligner ra=new ScriptAligner( l1, l2, null);
			ra.processFiles( runaling_path, dictalign_path, cesAlignList, dict );
		}else{
			LOGGER.info("only default hunalign is supported in the current version");
		}
	}
}
