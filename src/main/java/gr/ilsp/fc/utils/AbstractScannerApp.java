package gr.ilsp.fc.utils;

/**
 * 
 */

import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prokopis
 * 
 */
public abstract class AbstractScannerApp {

	private final static String APPNAME = AbstractScannerApp.class.getSimpleName();
	protected static final String TAB = "\t";
	static Logger logger = LoggerFactory.getLogger(APPNAME);

	private Options options;

	private String inputEnc = Constants.UTF8;

	private File inputDir = null;

	private String fileFilterRegex = "*";

	private boolean processRecursively = false;
	
	protected void process(List<File> inputFiles) throws IOException {
		for (File file : inputFiles) {
			process(file);
		}
	}
	
	protected abstract void process(File file) throws IOException ;
	  
	protected void createOptions() {
		options = new Options();		

		options.addOption(new Option("h", "help", false, "help"));
		options.addOption(new Option("ie", "inputEnc", true, "Input encoding (default is " + inputEnc + ")"));
		
		Option inputDirOp = new Option("id", "inputDir", true, "Input dir (default is " + inputDir + ")");
		inputDirOp.setRequired(true);		
		options.addOption(inputDirOp);
		
		options.addOption(new Option("r", "fileFilterRegex", true, "Regex for files to be processed (default is "+ fileFilterRegex + ")"));
		options.addOption(new Option("rec", "recursive", false, "Scan input dir recursively"));

	} 
	
	protected void parseOptions(String[] args) {
	
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {

			CommandLine line = clParser.parse(options, args);

			if (line.hasOption("h")) {
				printHelp("Options", options);
				System.exit(0);
			}

			if (line.hasOption("ie")) {
				inputEnc = line.getOptionValue("ie");
			}

			if (line.hasOption("rec")) {
				this.processRecursively = true;
				logger.info("Scanning dir recursively");
			}

			if (line.hasOption("r")) {
				fileFilterRegex = line.getOptionValue("r");
			}
			
			if (line.hasOption("id")) {
				inputDir = new File(line.getOptionValue("id"))
						.getAbsoluteFile();
			}

			if (!inputDir.exists()) {
				logger.error("Dir not specified or doesnot exist ");
				printHelp(APPNAME, options);
				System.exit(64);
			}
		} catch (ParseException exp) {
			for (String arg : args) {
				if (arg.equals("-h") || arg.contains("help")) {
					printHelp(APPNAME, options);
					System.exit(64);
				}
			}
			// oops, something went wrong
			logger.error("Parsing options failed.  Reason: " + exp.getMessage());
			printHelp(APPNAME, options);
			System.exit(64);
		}
	}

	  
	public List<File> getFilesFromInputDirectory(File inputDir,
			String fileFilterRegex, boolean processRecursively) throws IOException {
		List<File> inputFiles = new ArrayList<File>();

		if (!inputDir.exists() || !inputDir.isDirectory()) {
			throw new IOException(inputDir + " does not exist or is not a directory.");
		}

		if (processRecursively) {
			logger.debug(inputDir.toString() + "/**/" + getFileFilterRegex());
			Iterator<?> iterateFiles =  FileUtils.iterateFiles(inputDir,
					new RegexFileFilter(getFileFilterRegex()), TrueFileFilter.TRUE);
			while (iterateFiles.hasNext()) {				
				inputFiles.add((File) iterateFiles.next());
			}
		} else {
			 FileFilter fileFilter = new RegexFileFilter(getFileFilterRegex());
			 File[] files = inputDir.listFiles(fileFilter);
			 for (int i = 0; i < files.length; i++) {				 
				 inputFiles.add(files[i]);	
			 }
		}
		return inputFiles;
	}
	
	/**
	 * @return the inputEnc
	 */
	public String getInputEnc() {
		return inputEnc;
	}

	/**
	 * @param inputEnc the inputEnc to set
	 */
	public void setInputEnc(String inputEnc) {
		this.inputEnc = inputEnc;
	}


	/**
	 * @return the inputDir
	 */
	public File getInputDir() {
		return inputDir;
	}

	/**
	 * @param inputDir the inputDir to set
	 */
	public void setInputDir(File inputDir) {
		this.inputDir = inputDir;
	}


	/**
	 * @return the fileFilterRegex
	 */
	public String getFileFilterRegex() {
		return fileFilterRegex;
	}

	/**
	 * @param fileFilterRegex the fileFilterRegex to set
	 */
	public void setFileFilterRegex(String fileFilterRegex) {
		this.fileFilterRegex = fileFilterRegex;
	}

	/**
	 * @return the processRecursively
	 */
	public boolean isProcessRecursively() {
		return processRecursively;
	}

	/**
	 * @param processRecursively the processRecursively to set
	 */
	public void setProcessRecursively(boolean processRecursively) {
		this.processRecursively = processRecursively;
	}

	
	public static void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( program, options );
	}

	
}
