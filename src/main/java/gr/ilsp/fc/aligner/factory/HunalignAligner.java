package gr.ilsp.fc.aligner.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
		super.initialize(properties);
		setAligner_runnable(new File(properties.getProperty("aligner_runnable_path")));
		if (!getAligner_runnable().exists()) {
			throw new Exception("Sentence alignments are asked as output but the aligner's runnable does not exist.");
		}

		setDictalign_path(new File(properties.getProperty("dictalign_path")));
		if (!getDictalign_path().exists()) {
			throw new Exception("Aligner dictionary path set but the dictionary path does not exist.");
		}
		setDict(properties.getProperty("dict"));
		logger.debug(this.toString());
	}

	/*@Override
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

	}*/

	@Override
	public AlignmentStats process(File sourceFile, File targetFile, File tmxFile) throws Exception {

		List<String> sourceSentences = new ArrayList<String>();
		List<String> targetSentences = new ArrayList<String>();

		if (sentenceSplitParagraphs) {
			for (String sourceParagraph:  getParagraphs(sourceFile, isUseBoilerplateParagraphs(), isUseOoiLang(), isPreprocessSentences() )) {
				sourceSentences.addAll(sourceLangSentenceSplitter.getSentences(sourceParagraph, 1));
			}
			for (String targetParagraph:  getParagraphs(targetFile, isUseBoilerplateParagraphs(), isUseOoiLang(), isPreprocessSentences())) {
				targetSentences.addAll(targetLangSentenceSplitter.getSentences(targetParagraph, 1));
			}

			if (isPreprocessSentences()) {
				PreAlignmentNormalizer.unMaskSentences(sourceSentences);
				PreAlignmentNormalizer.unMaskSentences(targetSentences);
				PreAlignmentNormalizer.mergeUpPunctutionOnlySentences(sourceSentences);
				PreAlignmentNormalizer.mergeUpPunctutionOnlySentences(targetSentences);
			}
		
		} else {
			for (String sourceParagraph:  getParagraphs(sourceFile, isUseBoilerplateParagraphs(), isUseOoiLang(), false)) {
				sourceSentences.add(sourceParagraph);
			}
			for (String targetParagraph:  getParagraphs(targetFile, isUseBoilerplateParagraphs(), isUseOoiLang(), false)) {
				targetSentences.add(targetParagraph);
			}
		}
		//Run aligner using List<String> sourceSentences and List<String> targetSentences
		AlignmentStats as=align(sourceSentences, targetSentences, tmxFile);
		return as;
	}

	/**
	 * Method for running the hunalign
	 * @param sourceSentences List with all the source sentences
	 * @param targetSentences List with all the target sentences
	 * @param tmxFile The File to store the produced tmx file
	 * @throws IOException 
	 */
	public AlignmentStats align(List<String> sourceSentences, List<String> targetSentences, File tmxFile) throws IOException{
		File slF=IOtools.createRandomTmpFile(); //Source sentences
		File tlF=IOtools.createRandomTmpFile(); //Target sentences
		File outFile=IOtools.createRandomTmpFile(); //For storing the hunalign output

		IOtools.writeToFile(slF.getAbsolutePath(), sourceSentences);
		IOtools.writeToFile(tlF.getAbsolutePath(), targetSentences);

		//Run the hunalign sentence split program using a system call
		String[] cmd={this.aligner_runnable.getAbsolutePath(),
				//"-realign",
				this.dictalign_path.getAbsolutePath(),
				slF.getAbsolutePath(),
				tlF.getAbsolutePath()};
		this.runCommand(cmd, outFile.getAbsolutePath());
		int alignments=IOtools.createTMXfileFromHunalign(outFile.getAbsolutePath(), this.sourceLang, this.targetLang, sourceSentences, targetSentences, tmxFile.getAbsolutePath());
		//Delete the hunalign output file
		outFile.delete();
		return new AlignmentStats(alignments, sourceSentences.size(), targetSentences.size()) ;
	}
	/**
	 * Runs the command for hunalign 
	 * @param cmd The command 
	 * @param outFile
	 */
	protected void runCommand(String[] cmd, String outFile){
		try{  
			FileOutputStream fos = new FileOutputStream(outFile);
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmd);

			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fos);
			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();
			if(exitVal!=0)
				logger.info("ERROR while running the aligner script!");
			//if(exitVal==0)
			//System.out.println("Completed file: " + outFile.replace(".out", ""));
			fos.flush();
			fos.close();  
		}catch (Throwable t){
			t.printStackTrace();
		}

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
