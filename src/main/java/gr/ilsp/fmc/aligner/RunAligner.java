package gr.ilsp.fmc.aligner;


import org.apache.log4j.Logger;

public class RunAligner {
	private static final Logger LOGGER = Logger.getLogger(RunAligner.class);
	
	
	public static void align(String alignerName, String l1, String l2, String runalign_path,
			String dictalign_path, String dict,
			String cesAlignList, String tmxlist, String htmltmxlist){
		
		LOGGER.info("Alinger: "+ alignerName);
		LOGGER.info("Run_aligner: "+ runalign_path);
		//LOGGER.info("Use_dict: "+ dictalign_path);
		LOGGER.info("Languages: " + l1 + " "+ l2);
		LOGGER.info("Dictionary: " + dict);
		LOGGER.info("Dictionary_path: " + dictalign_path);
		//LOGGER.info("cesAlignList: " + cesAlignList);

		if (alignerName.equals("default")){
			ScriptAligner ra=new ScriptAligner( l1, l2, null);
			ra.processFiles( runalign_path, dictalign_path, cesAlignList, dict, tmxlist,htmltmxlist  );
		}else{
			LOGGER.info("only default hunalign is supported in the current version");
		}
	}
}
