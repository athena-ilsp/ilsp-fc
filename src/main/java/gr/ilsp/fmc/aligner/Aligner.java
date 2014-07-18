package gr.ilsp.fmc.aligner;

import gr.ilsp.fmc.main.SimpleCrawlHFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class Aligner{
	//String with the source language
	protected String sLang;
	//String with the target language
	protected String tLang;
	//String with default file types to read from the output of the crawler
	protected String filetypes="[uihml]";
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
	private static final Logger LOGGER = Logger.getLogger(SimpleCrawlHFS.class);

	/**
	 * RunAligner Constructor
	 * @param sLang The source Language
	 * @param tLang The target Language
	 * @param fileTypes the type of crawled files that we wish to run
	 * the aligner over
	 */
	public Aligner(String sLang, String tLang, String fileTypes){
		this.sLang=sLang;
		this.tLang=tLang;
		if(fileTypes!=null)
			this.filetypes="["+fileTypes+"]";
	}
	/**
	 * Method to read the list of files from a file stored locally or from a URL 
	 * @param filepath String with the path to the file containing the file list
	 * @return
	 */
	private ArrayList<String> getFileList(String filepath){
		if(filepath.startsWith("http"))
			return IOtools.readURLToArray(filepath);
		return IOtools.readFileToArrayForDcuService(filepath);
	}
	/**
	 * Method to process all files containing document pairs
	 * @param filepath String with the path to the file containing the file list
	 */
	protected void processFiles(String runaligner_path, String dictalign_path, String filePath, String dictPath){
		int pairNumber=0;
		int skippedNumber=0;
		StringBuffer log=new StringBuffer();
		StringBuffer log2=new StringBuffer();
		ArrayList<String> fileList=getFileList(filePath);
		//this.outputName=fs1+"alignOutput"+getDateTime();

		for(String file:fileList){
			String filePair=file.substring(file.lastIndexOf(fs1)+1, file.lastIndexOf(".xml"));
			String type=filePair.substring(filePair.length() - 1);
			if(file.startsWith("pdfs"))
				type="pdf";
			if(type.matches(filetypes)||type.compareTo("pdf")==0){
				pairNumber++;
				int alignments=0;
				//Parse the filename to get the document pair
				if(file.startsWith("../")){
					String newFilePath=filePath.substring(0, filePath.lastIndexOf(fs1));
					newFilePath=newFilePath.substring(0, newFilePath.lastIndexOf(fs1))+file.replace("..", "").replace(filePair+".xml", "");
					String files[]=IOtools.parseXmlFile(newFilePath, sLang, tLang);
					//String sFile=filePair.substring(0, filePair.indexOf("_"));
					//String tFile=filePair.substring(filePair.indexOf("_")+1, filePair.lastIndexOf("_"));
					String sFile=files[0];
					String tFile=files[1];
					//String prefix=file.replace("../", "");
					//String outputPath=filePath.substring(0, filePath.lastIndexOf(fs1)+1)+prefix.substring(0, prefix.indexOf(fs1))+this.outputName;
					String outputPath=filePath.substring(0, filePath.lastIndexOf(fs1))+this.outputName+fs1+file.substring(0, file.lastIndexOf(fs1));
					if(outputPath.endsWith(fs1+"xml"))
						outputPath=outputPath.replace(fs1+"xml", fs1+"tmx");
					else
						outputPath+=fs1+"tmx";
					IOtools.createDir(outputPath);

					try{
						alignments=processDocPair(runaligner_path, dictalign_path, newFilePath, sFile, tFile, outputPath, type, dictPath);
						String tmxFile=outputPath+fs1+"algn_"+sFile.replace(".xml", "")+"_"+tFile.replace(".xml", "")+"_"+type+".tmx";
						log.append(tmxFile+" :: "+alignments+System.getProperty("line.separator"));
						log2.append(tmxFile+System.getProperty("line.separator"));
					}catch(java.lang.NullPointerException e){
						log.append("ERROR when running aligner for pair sl="+sFile.replace(".xml", "")+" tl="+tFile.replace(".xml", "")+System.getProperty("line.separator"));
					}
				}
				else{
					//String newFilePath=filePath.substring(0, filePath.lastIndexOf(fs1)+1)+file;
					//String files[]=IOtools.parseXmlFile(newFilePath, sLang, tLang);
					String files[]=IOtools.parseXmlFile(file, sLang, tLang);
					String sFile=files[0];
					String tFile=files[1];
					//String outputPath=filePath.substring(0, filePath.lastIndexOf(fs1))+this.outputName+fs1+file.substring(0, file.lastIndexOf(fs1));
					String outputPath=file.substring(0, file.lastIndexOf(fs1));
					if(outputPath.endsWith(fs1+"xml"))
						outputPath=outputPath.replace(fs1+"xml", fs1+"tmx");
					else
						outputPath+=fs1+"tmx";
					IOtools.createDir(outputPath);

					try{
						//alignments=processDocPair(runaligner_path, dictalign_path, newFilePath, sFile, tFile, outputPath, type, dictPath);
						alignments=processDocPair(runaligner_path, dictalign_path, file, sFile, tFile, outputPath, type, dictPath);
						//String tmxFile=outputPath+"/"+sFile.replace(".xml", "")+"_"+tFile.replace(".xml", "")+"_"+type+".tmx";
						//String tmxFile=filePair.replace(fs1+"xml", fs1+"tmx")+".tmx";
						String tmxFile=file.replace("xml", "tmx");
						log.append(tmxFile+" :: "+alignments+" alignments"+System.getProperty("line.separator"));
						log2.append(tmxFile+System.getProperty("line.separator"));
					}catch(java.lang.NullPointerException e){
						log.append("ERROR when running aligner for pair sl="+sFile.replace(".xml", "")+" tl="+tFile.replace(".xml", "")+System.getProperty("line.separator"));
					}
				}
				if(alignsPerType.containsKey(type)){
					int i=alignsPerType.get(type);
					i+=alignments;
					alignsPerType.put(type, i);
				}
				else
					alignsPerType.put(type, alignments);
			}
			else
				skippedNumber++;
		}
		log.append(System.getProperty("line.separator")+"Document pairs processed by aligner: "+pairNumber+" out of "+fileList.size()+System.getProperty("line.separator"));
		LOGGER.info("Document pairs processed by the aligner: "+pairNumber+" out of "+fileList.size()+System.getProperty("line.separator"));
		//log.append(System.getProperty("line.separator")+"Document pairs of unwanted type skipped:"+skippedNumber+System.getProperty("line.separator"));
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
		String logName=filePath+this.outputName.replace(".txt", ".alignLog.txt");
		String listName=logName.replace(".txt", ".alignFileList.txt");
		LOGGER.info("Aligner log file stored at "+logName);
		LOGGER.info("Aligner file list file stored at "+listName);
		logName=logName.replace(".txt", ".alignLog.txt");
		IOtools.writeToFile(logName, log);
		IOtools.writeToFile(listName, log2);
	}

	protected abstract int processDocPair(String runaligner_path, String dictalign_path,String mainPath, String sFile, String tFile, String outputPath, String type, String dict);
}