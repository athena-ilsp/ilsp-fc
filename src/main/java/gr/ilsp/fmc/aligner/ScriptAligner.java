package gr.ilsp.fmc.aligner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class ScriptAligner extends Aligner{
	private static String fs = System.getProperty("file.separator");
	/*
	 * Enum of available platforms for hunalign
	 */
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
	private static final Logger LOGGER = Logger.getLogger(ScriptAligner.class);
	/**
	 * @return The platform, null if not recognized
	 */
	public static Platform getPlatform(){
		String prop=System.getProperty("os.name").toLowerCase();
		if(prop.equals("linux"))
			return Platform.Linux;
		else if(prop.startsWith("windows"))
			return Platform.Win;
		return null;
	}
	/**
	 * Get the path to the hunalign dir
	 * @return String with the absolute path to the hunalign dir
	 */
	public static String getHunalignPath(){
	    File file = new File("hunalign-1.1");
		return file.getAbsolutePath();
	}
	/**
	 * Get the path to the hunalign dir
	 * @return String with the absolute path to the hunalign dir
	 */
	public static String getHunalignPlatformPath(){
		String hunpath="";
		String path = ScriptAligner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		try {
			File decodedPath = new File(URLDecoder.decode(path, "UTF-8"));
			File file = new File(decodedPath.getParent()+fs+"hunalign-1.1");
			hunpath= file.getAbsolutePath()+getPlatform().hunPath;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("1");
	    //File file = new File("hunalign-1.1"+getPlatform().hunPath);
	    //return file.getAbsolutePath();
		return hunpath;
	}
	/**
	 * Get the path to the hunalign executable script
	 * @return String with the absolute path to the hunalign executable
	 */
	public static String getHunalignExec(){
	    File file = new File(getHunalignPlatformPath()+fs+getPlatform().command);
		return file.getAbsolutePath();
	}
	/**
	 * Returns the path to a hunalign sample dictionary for a language pair
	 * @param sl The source language
	 * @param tl The target language
	 * @return String to the absolute path of the language pair dictionary
	 */
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
	/**
	 * RunAligner Constructor
	 * @param sLang The source Language
	 * @param tLang The target Language
	 * @param fileTypes the type of crawled files that we wish to run
	 * the aligner over
	 */
	public ScriptAligner(String sLang, String tLang, String fileTypes){
		super(sLang, tLang, fileTypes);
		alignsPerType=new HashMap<String, Integer>();
		slSentsPerType=new HashMap<String, Integer>();
		tlSentsPerType=new HashMap<String, Integer>();
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
            	LOGGER.info("ERROR while running the aligner script!");
            //if(exitVal==0)
            	//System.out.println("Completed file: " + outFile.replace(".out", ""));
            fos.flush();
            fos.close();  
        }catch (Throwable t){
          t.printStackTrace();
        }

	}
	/**
	 * Run the aligner over the document pair
	 * @param mainPath Main part of the URL for the two files
	 * @param slFile The name of the source language document
	 * @param tlFile The name of the target language document
	 * @param outputPath The output path
	 * @param type The type of the crawled document
	 * @return String with an output message regarding the alignment process
	 */
	protected int processDocPair(String mainPath, String sFile, String tFile, String outputPath, String type, String dict){
		sFile=sFile.replace(".xml", "");
		tFile=tFile.replace(".xml", "");
		File slF=null;
		File tlF=null;
		
		String outFilePrfx=mainPath.substring(0, mainPath.lastIndexOf(fs1+"xml"));
		String outFileSfx=mainPath.substring(mainPath.lastIndexOf(fs1), mainPath.length()).replace(".xml", "");
		outFilePrfx=outFilePrfx+fs1+"tmx";
		
		String slFile=mainPath.substring(0, mainPath.lastIndexOf(fs1)+1)+sFile+".xml";
		String tlFile=mainPath.substring(0, mainPath.lastIndexOf(fs1)+1)+tFile+".xml";
		String slText=IOtools.stripXcesDocument(new File(slFile));
		String tlText=IOtools.stripXcesDocument(new File(tlFile));
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
			dictF=getHunalignDict(this.sLang, this.sLang);
		else{
			if(dict.compareTo("default")==0)
				dictF=getHunalignDict(this.sLang, this.tLang);
			else
				dictF=dict;
		}
		//outFilePrfx=outputPath+"/"+sFile+"_"+tFile+"_"+type;
		String outFile=outFilePrfx+outFileSfx+".out";
		String tmxFile=outFilePrfx+outFileSfx+".tmx";
		
		//Run the hunalign sentence split program using a system call
		String[] cmd={getHunalignExec(),
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
    /**
     * Main method
     * @param arg
     */
    public static void main(String[] arg){
    	String slang=arg[0].toLowerCase();
    	String tlang=arg[1].toLowerCase();
    	String regexp=arg[2];
    	String str=arg[3];
    	ScriptAligner ra=new ScriptAligner(slang, tlang, regexp);
    	ra.processFiles(str, arg[4]);
    }
}