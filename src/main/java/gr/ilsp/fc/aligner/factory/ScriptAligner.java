/*package gr.ilsp.fc.aligner.factory;

import gr.ilsp.fc.bitext.BitextUtils;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitter;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;


public  class ScriptAligner {
	//String with the source language
	protected String sLang;
	//String with the target language
	protected String tLang;
	//String with default file types to read from the output of the crawler
	//protected String filetypes="[auidhml]";
	protected String filetypes="[abcdefghijklmnopqrstuvwxyz]";
	//Default url of the europarl sentence splitter service
	protected String sentSplitUrl="http://www.cngl.ie/panacea-soaplab2-axis/services/panacea.europarl_sentence_splitter";
	//Default url of the service to transform the sentence splitter output to the TO format
	protected String sSplit2TOUrl="http://www.cngl.ie/panacea-soaplab2-axis/services/panacea.sentsplit_tok2to";
	//Default url of the sentence aligner service
	protected String sUrl="http://www.cngl.ie/panacea-soaplab2-axis/services/panacea.hunalign";
	//Default url of the service to transform the aligner output to the TO format
	protected String sToUrl="http://www.cngl.ie/panacea-soaplab2-axis/services/panacea.aligner2to";
	//name of output folder and log file
	protected String outputName="";
	protected HashMap<String, Integer> alignsPerType;
	protected HashMap<String, Integer> slSentsPerType;
	protected HashMap<String, Integer> tlSentsPerType;
	protected String dictionary=null;
	protected static String fs1 = System.getProperty("file.separator");
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ScriptAligner.class);

	private File aligner_runnable;
	private File dictalign_path;
	
	
	*//**
	 * ScriptAligner Constructor
	 * @param sLang The source Language
	 * @param tLang The target Language
	 * @param fileTypes the type of crawled files that we wish to run
	 * the aligner over
	 *//*
	public ScriptAligner(String sLang, String tLang, String fileTypes){
		this.sLang=sLang;
		this.tLang=tLang;
		if(fileTypes!=null)
			this.filetypes="["+fileTypes+"]";
		alignsPerType=new HashMap<String, Integer>();
		slSentsPerType=new HashMap<String, Integer>();
		tlSentsPerType=new HashMap<String, Integer>();
	}
	*//**
	 * ScriptAligner Constructor
	 * @param hunalign_path The path to the hunalign executable
	 * @param dict_path The path to the hunalign dictionary
	 * @param sLang The source Language
	 * @param tLang The target Language
	 * @param fileTypes the type of crawled files that we wish to run
	 * the aligner over
	 *//*
	public ScriptAligner(File hunalign_path, File dict_path, String sLang, String tLang, String fileTypes){
		this.sLang=sLang;
		this.tLang=tLang;
		this.aligner_runnable=hunalign_path;
		this.dictalign_path=dict_path;
		if(fileTypes!=null)
			this.filetypes="["+fileTypes+"]";
		alignsPerType=new HashMap<String, Integer>();
		slSentsPerType=new HashMap<String, Integer>();
		tlSentsPerType=new HashMap<String, Integer>();
	}
	*//**
	 * Method to read the list of files from a file stored locally or from a URL 
	 * @param filepath String with the path to the file containing the file list
	 * @return
	 *//*
	private ArrayList<String> getFileList(String filepath){
		if(filepath.startsWith("http"))
			return IOtools.readURLToArray(filepath);
		return IOtools.readFileToArrayForDcuService(filepath);
	}
	*//**
	 * Method to process all files containing document pairs
	 * @param filepath String with the path to the file containing the file list
	 *//*
	public void processsFiles(String runaligner_path, String dictalign_path, 
			String filePath, String dictPath, String tmxlist, String htmltmxlist){
		int pairNumber=0;
		int skippedNumber=0;
		StringBuffer log=new StringBuffer();
		StringBuffer log2=new StringBuffer();
		ArrayList<String> fileList=getFileList(filePath);

		for(String file:fileList) {
			File cesAlignFile = new File(file).getAbsoluteFile();
			String basename = FilenameUtils.getBaseName(cesAlignFile.getName());
			
			String documentAlignmentMethod =BitextUtils.getDocumentAlignmentMethod(cesAlignFile);
			
			if (documentAlignmentMethod .matches(filetypes)){
				pairNumber++;
				int alignments=0;
				
				File[] files=IOtools.parseXmlFile(file, sLang, tLang);
				File sFile=files[0];
				File tFile=files[1];
				String outputDir = cesAlignFile.getParent();

				File tmxFile = new File(FilenameUtils.concat(outputDir,  basename+".tmx"));
				logger.debug(tmxFile.getAbsolutePath());

				try{
					alignments=processDocPair(runaligner_path, dictalign_path, cesAlignFile.getAbsolutePath(),
							sFile, tFile, outputDir, documentAlignmentMethod , dictPath);
					log.append(tmxFile+" :: "+alignments+" alignments"+System.getProperty("line.separator"));
					log2.append(tmxFile+System.getProperty("line.separator"));
					
				}catch(java.lang.NullPointerException e){
					log.append("ERROR when running aligner for pair sl="+sFile.getAbsolutePath().replace(".xml", "")+" tl="+tFile.getAbsolutePath().replace(".xml", "")+System.getProperty("line.separator"));
				}

				if(alignsPerType.containsKey(documentAlignmentMethod )){
					int i=alignsPerType.get(documentAlignmentMethod );
					i+=alignments;
					alignsPerType.put(documentAlignmentMethod , i);
				}
				else
					alignsPerType.put(documentAlignmentMethod , alignments);
			}
			else
				skippedNumber++;
		}
		log.append(System.getProperty("line.separator")+"Document pairs processed by aligner: "+pairNumber+" out of "+fileList.size()+System.getProperty("line.separator"));
		//LOGGER.info("Document pairs processed by the aligner: "+pairNumber+" out of "+fileList.size()+System.getProperty("line.separator"));
		logger.debug(System.getProperty("line.separator")+"Document pairs of unwanted type skipped:"+skippedNumber+System.getProperty("line.separator"));
		log.append(System.getProperty("line.separator")+"Alignments per type:"+System.getProperty("line.separator"));
		Set<String> keys = alignsPerType.keySet();
		for(String key : keys){
			int value=alignsPerType.get(key);
			log.append("\t"+key+": "+value+System.getProperty("line.separator"));
		}
		log.append(System.getProperty("line.separator")+"SL sentences per type:"+System.getProperty("line.separator"));
		Set<String> keys2 = slSentsPerType.keySet();
		for(String key : keys2){
			int value=slSentsPerType.get(key);
			log.append("\t"+key+": "+value+System.getProperty("line.separator"));
		}
		log.append(System.getProperty("line.separator")+"TL sentences per type:"+System.getProperty("line.separator"));
		Set<String> keys3 = tlSentsPerType.keySet();
		for(String key : keys3){
			int value=tlSentsPerType.get(key);
			log.append("\t"+key+": "+value+System.getProperty("line.separator"));
		}

		//Store the log
		//String logName=filePath+this.outputName.replace(".txt", ".alignLog.txt");
		//String listName=logName.replace(".txt", ".alignFileList.txt");
		String logName = tmxlist;
		String listName = htmltmxlist; 
		logger.info("Aligner log file stored at "+logName);
		IOtools.writeToFile(logName, log);
		if (listName!=null){
			logger.info("Aligner file list file stored at "+listName);
			log2=IOtools.convertlistTMX_HTML(log2);
			IOtools.writeToFile(listName, log2);
		}
	}

	*//**
	 * Method for running the hunalign
	 * @param sourceSentences ArrayList with all the source sentences
	 * @param targetSentences ArrayList with all the target sentences
	 * @param tmxFile The File to store the produced tmx file
	 * @throws IOException 
	 *//*
	public AlignmentStats process(ArrayList<String> sourceSentences, ArrayList<String> targetSentences, File tmxFile) throws IOException{
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
		int alignments=IOtools.createTMXfileFromHunalign(outFile.getAbsolutePath(), this.sLang, this.tLang, sourceSentences, targetSentences, tmxFile.getAbsolutePath());
		//Delete the hunalign output file
		outFile.delete();
		return new AlignmentStats(alignments, sourceSentences.size(), targetSentences.size()) ;
	}

	private static String fs = System.getProperty("file.separator");
	
	 * Enum of available platforms for hunalign
	 
	public enum Platform{		
		Linux("/linux/src/hunalign", "hunalign"), 
		Win("/win", "hunalign.exe");

		public String hunPath;
		public String command;

		Platform(String path, String cmd) {
			this.hunPath = path;
			this.command = cmd;
		}
	}

	*//**
	 * @return The platform, null if not recognized
	 *//*
	public static Platform getPlatform(){
		String prop=System.getProperty("os.name").toLowerCase();
		if(prop.equals("linux"))
			return Platform.Linux;
		else if(prop.startsWith("windows"))
			return Platform.Linux; Platform.Win;
		return null;
	}
	*//**
	 * Get the path to the hunalign dir
	 * @return String with the absolute path to the hunalign dir
	 *//*
	//public static String getHunalignPath(){
	//    File file = new File("hunalign-1.1");
	//	return file.getAbsolutePath();
	//}
	*//**
	 * Get the path to the hunalign dir
	 * @return String with the absolute path to the hunalign dir
	 *//*
	public static String getHunalignPlatformPath(){
		String hunpath="";
		String path = ScriptAligner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			File decodedPath = new File(URLDecoder.decode(path, "UTF-8"));
			//File file = new File(decodedPath.getParent()+fs+"hunalign-1.1");
			//hunpath= decodedPath.getAbsolutePath()+getPlatform().hunPath;
			hunpath= decodedPath.getAbsolutePath();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("1");
	    //File file = new File("hunalign-1.1"+getPlatform().hunPath);
	    //return file.getAbsolutePath();
		return hunpath;
	}
	*//**
	 * Get the path to the hunalign executable script
	 * @return String with the absolute path to the hunalign executable
	 *//*
	public static String getHunalignExec(){
	    File file = new File(getHunalignPlatformPath()+fs+getPlatform().command);
		return file.getAbsolutePath();
	}
	*//**
	 * Returns the path to a hunalign sample dictionary for a language pair
	 * @param sl The source language
	 * @param tl The target language
	 * @return String to the absolute path of the language pair dictionary
	 *//*
	public static String getHunalignDict(String sl, String tl){
		//String pathPfx=getHunalignPath()+"/dict/";

		File file =new File(new File(getHunalignPlatformPath()).getParent()+fs+"dict");
		String pathPfx=file.getAbsolutePath()+fs;

		String pathSfx=sl.toLowerCase()+"-"+tl.toLowerCase()+".dic";
		File dictFile = new File(pathPfx+pathSfx);
		if(dictFile.exists())
			return dictFile.getAbsolutePath();
		else{
			pathSfx=tl.toLowerCase()+"-"+sl.toLowerCase()+".dic";
			dictFile = new File(pathPfx+pathSfx);
			if(dictFile.exists())
				return dictFile.getAbsolutePath();
		}
		return new File(pathPfx+"null.dic").getAbsolutePath();
	}	

	public static String getHunalignDict(String dict_path, String sl,String tl) {
		//File file =new File(getHunalignPlatformPath()+fs+runalign_path);
		//String pathPfx=file.getAbsolutePath()+fs;
		if (!dict_path.isEmpty()){
			String pathSfx=sl.toLowerCase()+"-"+tl.toLowerCase()+".dic";
			File dictFile = new File(dict_path+fs+pathSfx);
			if(dictFile.exists())
				return dictFile.getAbsolutePath();
			else{
				pathSfx=tl.toLowerCase()+"-"+sl.toLowerCase()+".dic";
				dictFile = new File(dict_path+fs+pathSfx);
				if(dictFile.exists())
					return dictFile.getAbsolutePath();
			}
			return new File(dict_path+fs+"null.dic").getAbsolutePath();
		}
		return new File(dict_path+fs+"null.dic").getAbsolutePath();
	}

		*//**
		 * Runs the command for hunalign 
		 * @param cmd The command 
		 * @param outFile
		 *//*
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
		*//**
		 * Run the aligner over the document pair
		 * @param cesAlignPath Main part of the URL for the two files
		 * @param sFile The name of the source language document
		 * @param tFile The name of the target language document
		 * @param outputPath The output path
		 * @param type The type of the crawled document
		 * @return String with an output message regarding the alignment process
		 *//*
		protected int processDocPair(String runalign_path, String dictalign_path, String cesAlignPath, File sFile, File tFile, String outputPath, String type, String dict){
			File outputDir = new File(cesAlignPath).getParentFile();
			String basename = FilenameUtils.getBaseName(cesAlignPath);
			//File aa = new File(sFile.getParent()+fs+sFile.getName().replace(".xml", ""));
			sFile= new File(sFile.getParent()+fs+sFile.getName().replace(".xml", ""));
			tFile= new File(tFile.getParent()+fs+tFile.getName().replace(".xml", ""));
			File slF=null;
			File tlF=null;
			
			String outFilePrfx=new File(cesAlignPath).getParent();
			String outFileSfx=cesAlignPath.substring(cesAlignPath.lastIndexOf(fs1), cesAlignPath.length());
			outFilePrfx=outFilePrfx+fs1;
			
			//String slFile = cesAlignPath.substring(0, cesAlignPath.lastIndexOf(fs1)+1)+sFile+".xml";
			//String tlFile = cesAlignPath.substring(0, cesAlignPath.lastIndexOf(fs1)+1)+tFile+".xml";
			//String slFile = sFile+".xml";
			//String tlFile = tFile+".xml";
			SentenceSplitterFactory sentenceSplitterFactory = new SentenceSplitterFactory();
			SentenceSplitter sourceLangSentenceSplitter = sentenceSplitterFactory.getSentenceSplitter(this.sLang);
			SentenceSplitter targetLangSentenceSplitter = sentenceSplitterFactory.getSentenceSplitter(this.tLang);
			
			String slText=IOtools.stripXcesDocument(sourceLangSentenceSplitter, new File(sFile.getParent()+fs+sFile.getName()+".xml"));
			String tlText=IOtools.stripXcesDocument(targetLangSentenceSplitter, new File(tFile.getParent()+fs+tFile.getName()+".xml"));
			ArrayList<String> slSents = new ArrayList<String>(Arrays.asList(slText.split(System.getProperty("line.separator"))));
			ArrayList<String> tlSents = new ArrayList<String>(Arrays.asList(tlText.split(System.getProperty("line.separator"))));
			//ArrayList<String> slSents = new ArrayList<String>(Arrays.asList(slText.split(fs1)));
			//ArrayList<String> tlSents = new ArrayList<String>(Arrays.asList(tlText.split(fs1)));

			try{
				slF=IOtools.createRandomTmpFile();
				BufferedWriter slW=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(slF),Charset.forName("UTF8")));
				slW.write(slText);
				slW.close();

				tlF=IOtools.createRandomTmpFile();
				BufferedWriter tlW=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tlF),Charset.forName("UTF8")));
				tlW.write(tlText);
				tlW.close();
			}catch(IOException e){
				e.printStackTrace();
			}

			if(slSentsPerType.containsKey(type)){
				int i=slSentsPerType.get(type);
				i+=slSents.size();
				slSentsPerType.put(type, i);

				int j=tlSentsPerType.get(type);
				j+=tlSents.size();
				tlSentsPerType.put(type, j);
			}
			else{
				slSentsPerType.put(type, slSents.size());

				tlSentsPerType.put(type, tlSents.size());
			}

			String dictF="";
			if(dict==null)
				//dictF=getHunalignDict(this.sLang, this.sLang);
				dictF=getHunalignDict(dictalign_path, this.sLang, this.sLang);
			else{
				if(dict.compareTo("default")==0)
					dictF=getHunalignDict(dictalign_path,this.sLang, this.tLang);
				else
					dictF=dict;
			}
			//outFilePrfx=outputPath+"/"+sFile+"_"+tFile+"_"+type;
			String outFile=outFilePrfx+outFileSfx+".out";
			String tmxFile =  FilenameUtils.concat (outputDir.getAbsolutePath(), 
					basename + ".tmx");
			
			//Run the hunalign sentence split program using a system call
			String[] cmd={runalign_path,
					//"-realign"
					dictF,
					slF.getAbsolutePath(),
					tlF.getAbsolutePath()};
			//"-text"};
			//" > ",
			//outFile};
			this.runCommand(cmd, outFile);
			int alignments=IOtools.createTMXfileFromHunalign(outFile, this.sLang, this.tLang, slSents, tlSents, tmxFile);
			//Delete the hunalign output file
			new File(outFile).delete();
			return alignments;
		}
}*/