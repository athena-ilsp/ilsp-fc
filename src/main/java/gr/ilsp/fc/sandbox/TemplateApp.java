package gr.ilsp.fc.sandbox;

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
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

/**
 * @author prokopis
 * 
 */
public class TemplateApp {

	private final static String APPNAME = TemplateApp.class.getSimpleName();
	static Logger logger = Logger.getLogger(APPNAME);

	private Options options;

	private String inputEnc = "UTF-8";
	private String outputEnc= "UTF-8";

	private File inputDir = null;
	private File outputDir = null;

	private String fileFilterRegex = "*";

	private String stripExt = null;
	private String appendExt = null;

	private boolean processRecursively = false;
	
	public static void main(String[] args) throws IOException  {
		TemplateApp app = new TemplateApp();
		app.createOptions();
		app.parseOptions(args);		
		List<File> inputFiles = app.getFilesFromInputDirectory(app.getInputDir(), app.getFileFilterRegex(), app.processRecursively);
		app.process(inputFiles);
	}

	private void process(List<File> inputFiles) {
		for (File inputFile : inputFiles) {
			logger.info("Processing "+inputFile);
		}
	}
	  
	private void createOptions() {
		options = new Options();		

		options.addOption(new Option("h", "help", false, "help"));
		options.addOption(new Option("ie", "inputEnc", true, "Input encoding (default is " + inputEnc + ")"));
		options.addOption(new Option("oe", "outputEnc", true, "Output encoding (default is " + outputEnc + ")"));
		
		Option inputDirOp = new Option("id", "inputDir", true, "Input dir (default is " + inputDir + ")");
		inputDirOp.setRequired(true);		
		options.addOption(inputDirOp);
		options.addOption(new Option("od", "outputDir", true, "Output dir (default is " + outputDir + ")"));

		options.addOption(new Option("s", "strip", true, "File extension to be stripped from the filenames (default is "+ stripExt + ")"));
		options.addOption(new Option("a", "append", true, "File extension to be appended to filenames (default is "+ appendExt + ")"));
		
		options.addOption(new Option("r", "fileFilterRegex", true, "Regex for files to be processed (default is "+ fileFilterRegex + ")"));
		options.addOption(new Option("rec", "recursive", false, "Scan input dir recursively"));

	} 
	
	private void parseOptions(String[] args) {
	
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
			if (line.hasOption("oe")) {
				outputEnc = line.getOptionValue("oe");
			}

			if (line.hasOption("rec")) {
				processRecursively = true;
			}

			if (line.hasOption("r")) {
				fileFilterRegex = line.getOptionValue("r");
			}
			
			if (line.hasOption("s")) {
				stripExt = line.getOptionValue("s");
			}

			if (line.hasOption("a")) {
				appendExt = line.getOptionValue("a");
			}

			if (line.hasOption("id")) {
				inputDir = new File(line.getOptionValue("id"))
						.getAbsoluteFile();
			}

			if (line.hasOption("od")) {
				outputDir = new File(line.getOptionValue("od"))
						.getAbsoluteFile();
			} else {
				outputDir = inputDir;
			}

			if (!inputDir.exists()) {
				logger.error("Dir not specified or doesnot exist ");
				printHelp(APPNAME, options);
				System.exit(64);
			} else if (!outputDir.exists()) {
				try {
					outputDir.mkdirs();
				} catch (Exception e) {
					logger.error("Could not write to output dir "
							+ outputDir.getAbsolutePath());
					printHelp(APPNAME, options);
					System.exit(64);
				}
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
			Iterator<?> iterateFiles =  FileUtils.iterateFiles(inputDir,
					new WildcardFileFilter(getFileFilterRegex()), TrueFileFilter.INSTANCE);
			while (iterateFiles.hasNext()) {				
				inputFiles.add((File) iterateFiles.next());
			}
		} else {
			 FileFilter fileFilter = new WildcardFileFilter(getFileFilterRegex());
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
	 * @return the outputEnc
	 */
	public String getOutputEnc() {
		return outputEnc;
	}

	/**
	 * @param outputEnc the outputEnc to set
	 */
	public void setOutputEnc(String outputEnc) {
		this.outputEnc = outputEnc;
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
	 * @return the outputDir
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * @param outputDir the outputDir to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
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
	 * @return the stripExt
	 */
	public String getStripExt() {
		return stripExt;
	}

	/**
	 * @param stripExt the stripExt to set
	 */
	public void setStripExt(String stripExt) {
		this.stripExt = stripExt;
	}

	/**
	 * @return the appendExt
	 */
	public String getAppendExt() {
		return appendExt;
	}

	/**
	 * @param appendExt the appendExt to set
	 */
	public void setAppendExt(String appendExt) {
		this.appendExt = appendExt;
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