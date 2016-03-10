package gr.ilsp.fc.aligner.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.loomchild.maligna.coretypes.Alignment;
import net.loomchild.maligna.formatter.Formatter;

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
		Object[] alOb=align(sourceSentences, targetSentences, tmxFile);
		@SuppressWarnings("unchecked")
		List<Alignment> alignmentList=(List<Alignment>) alOb[0];
		AlignmentStats as=(AlignmentStats) alOb[1];
		//Create TMX file
		Writer writer = getSingleWriter(tmxFile);
		Formatter formatter = new BilingualScoredTmxFormatter(writer, getSourceLang(), getTargetLang(), sourceFile, targetFile);
		formatter.format(alignmentList);
		writer.close();
		return as;
	}

	/**
	 * Method for running the hunalign
	 * @param sourceSentences List with all the source sentences
	 * @param targetSentences List with all the target sentences
	 * @param tmxFile The File to store the produced tmx file
	 * @return An Object[] containing an instance of List<Alignment> with all the alignments
	 * @throws IOException 
	 */
	public Object[] align(List<String> sourceSentences, List<String> targetSentences, File tmxFile) throws IOException{
		File slF=IOtools.createRandomTmpFile(); //Source sentences
		File tlF=IOtools.createRandomTmpFile(); //Target sentences
		File outFile=IOtools.createRandomTmpFile(); //For storing the hunalign output

		IOtools.writeToFile(slF.getAbsolutePath(), sourceSentences);
		IOtools.writeToFile(tlF.getAbsolutePath(), targetSentences);

		//Run the hunalign sentence split program using a system call
		String[] cmd={this.aligner_runnable.getAbsolutePath(),
				//"-realign",
				"-text",
				this.dictalign_path.getAbsolutePath(),
				slF.getAbsolutePath(),
				tlF.getAbsolutePath()};
		this.runCommand(cmd, outFile);
		//this.runCommand2(cmd, outFile.getAbsolutePath());
		
		Object[] alOb=hunalignOutputToAlignmentList(outFile, sourceSentences, targetSentences);
		//Delete temporary files
		slF.delete();
		tlF.delete();
		outFile.delete();
		return alOb;
	}
	/**
	 * Stores the output of hunalign as an instance of List<Alignment> 
	 * @param file The local location of the hunalign output
	 * @param slSents List with all source sentences
	 * @param tlSents List with all target sentences
	 * @throws IOException 
	 * @ret An Object[] containing an instance of List<Alignment> with all the alignments
	 * and an AlignmentStats instance
	 */
	public Object[] hunalignOutputToAlignmentList(File hunalignfile, List<String> slSents, List<String> tlSents) throws IOException{
		List<String> content=FileUtils.readLines(hunalignfile);

		//logger.info(hunalignfile.getAbsolutePath());
		//logger.info(content.toString());
		List<Alignment> alignmentList=new ArrayList<Alignment>();
		
		int zeroToOneAlignments=0;
		int alignments=0;
		float totalScore=0;
		for(String line:content){
			List<String> sourceSentences=new ArrayList<String>();
			List<String> targetSentences=new ArrayList<String>();
			String[] lineArray=line.split("\t");
			String slText=lineArray[0];
			String tlText=lineArray[1];

			String[] slA=slText.split(" ~~~ ");
			String[] tlA=tlText.split(" ~~~ ");
			if(slText.compareTo("")!=0)
				sourceSentences.addAll(Arrays.asList(slA));
			if(tlText.compareTo("")!=0)
				targetSentences.addAll(Arrays.asList(tlA));
			alignments++;
			if(slText.compareTo("")==0||slText.compareTo("")==0)
				zeroToOneAlignments++;

			float score=0;
			try{
				score=Float.parseFloat(lineArray[2]);
			}catch(java.lang.ArrayIndexOutOfBoundsException e){
				score=0;
			}
			totalScore+=score;
			Alignment al=new Alignment(sourceSentences, targetSentences);
			al.setScore(score);
			alignmentList.add(al);
		}
		//Alignment alignment = new Alignment(sourceSentences, targetSentences);
		AlignmentStats as=new AlignmentStats(alignments, slSents.size(), tlSents.size());
		as.setZeroToOneAlignmentsSize(zeroToOneAlignments);
		as.setMeanScore((double) (totalScore/alignments));
		
		Object[] ret=new Object[2];
		ret[0] = alignmentList;//Collections.singletonList(alignment);
		ret[1] = as;
		return ret;
	}
	/**
	 * Runs the command for hunalign 
	 * @param cmd The command 
	 * @param outFile
	 */
	protected void runCommand(String[] cmd, File outFile){
		 
        try {
        	String s = null;
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
            	FileUtils.write(outFile, s+"\n", true);
            }
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
              //  System.err.println(s);
            }
        } catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
	}

	/**
	 * Runs the command for hunalign 
	 * @param cmd The command 
	 * @param outFile
	 */
	protected void runCommand2(String[] cmd, String outFile){
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
		}catch (Exception t){
			t.printStackTrace();
		}

	}
	
	
	
	
	private Writer getSingleWriter(File outFile) throws UnsupportedEncodingException, FileNotFoundException {
		Writer writer;
		if (outFile!=null) {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"), true);
		} else {
			writer = new PrintWriter(new OutputStreamWriter((System.out),"UTF-8"), true);
		}
		return writer;
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
