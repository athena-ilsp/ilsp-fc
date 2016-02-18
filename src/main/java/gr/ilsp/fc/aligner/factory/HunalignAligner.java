package gr.ilsp.fc.aligner.factory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HunalignAligner extends Aligner {
	private static final Logger logger = LoggerFactory.getLogger(HunalignAligner.class);
	private File aligner_runnable;
	private File dictalign_path;
	private String dict = "default";
	//private boolean xslt = true;

	@Override
	public void initialize(Properties properties) throws Exception {
		this.properties = properties;
		setSourceLang(properties.getProperty("sourceLang"));
		setTargetLang(properties.getProperty("targetLang"));
		if (!properties.isEmpty()) {
			logger.info("Inititalizing aligner with properties ");
			logger.info(properties.toString());
		}
		setAligner_runnable(new File(properties.getProperty("aligner_runnable_path")));
		if (!getAligner_runnable().exists()) {
			throw new Exception("Sentence alignments are asked as output but the aligner's runnable does not exist.");
		}

		setDictalign_path(new File(properties.getProperty("dictalign_path")));
		if (!getDictalign_path().exists()) {
			throw new Exception("Aligner dictionary path set but the dictionary path does not exist.");
		}

		setDict(properties.getProperty("dict"));

	}




	@Override
	public void processCesAlignList(File cesAlignList, File outputTMXList,
			File outputHTMLTMXList, boolean xslt){
		ScriptAligner ra=new ScriptAligner( sourceLang, targetLang, null);

		if (outputHTMLTMXList!=null)
			ra.processFiles(getAligner_runnable().getAbsolutePath(), getDictalign_path().getAbsolutePath(), 
					cesAlignList.getAbsolutePath(), getDict(), 
					outputTMXList.getAbsolutePath(), outputHTMLTMXList.getAbsolutePath()  );
		else
			ra.processFiles(getAligner_runnable().getAbsolutePath(), getDictalign_path().getAbsolutePath(), 
					cesAlignList.getAbsolutePath(), getDict(), 
					outputTMXList.getAbsolutePath(), null);

	}


	@Override
	public AlignmentStats process(File sourceFile, File targetFile, File tmxFile)
			throws IOException, Exception {
		logger.warn("Not implemented yet! Returning null!");
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	private File getAligner_runnable() {
		return aligner_runnable;
	}

	private void setAligner_runnable(File aligner_runnable) {
		this.aligner_runnable = aligner_runnable;
	}

	private File getDictalign_path() {
		return dictalign_path;
	}

	private void setDictalign_path(File dictalign_path) {
		this.dictalign_path = dictalign_path;
	}




	public String getDict() {
		return dict;
	}




	public void setDict(String dict) {
		this.dict = dict;
	}


}
