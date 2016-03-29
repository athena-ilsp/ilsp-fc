package gr.ilsp.fc.aligner.factory;

import gr.ilsp.fc.bitext.BitextUtils;
import gr.ilsp.fc.exporter.XSLTransformer;
import gr.ilsp.fc.main.WriteResources;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitter;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

/**
 * @author prokopis
 *
 */
public abstract class Aligner {

	private static final String OOI_LANG = "ooi-lang";
	private static final String TMXlist = ".tmxlist.txt";
	private static final String TMXHTMLlist = ".tmxlist.html";
	//private static final String TYPE = "type";
	private static final Logger logger = LoggerFactory.getLogger(Aligner.class);
	protected Properties properties ;
	protected String sourceLang ;
	protected String targetLang ;
	private static final String BOILERPLATE = "boilerplate";
	private static final String CRAWLINFO = "crawlinfo";
	protected static final String P_ELE = "p";
	private static final String appXMLext = ".xml";
	private static final String appTMXext = ".tmx";
	private static final String appTMXHTMLext = ".tmx.html";
	private static final String UNDERSCORE_STR = "_";
	private static final String tmx_xsl= "http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html.xsl";
	protected boolean useBoilerplateParagraphs = false;
	protected boolean useOoiLang = true;
	protected boolean sentenceSplitParagraphs = true;
	private List<Pair<String, String>> attributeValuesToIgnore = new ArrayList<Pair<String, String>>();

	protected SentenceSplitterFactory sentenceSplitterFactory = new SentenceSplitterFactory();
	protected SentenceSplitter sourceLangSentenceSplitter;
	protected SentenceSplitter targetLangSentenceSplitter;

	protected boolean preprocessSentences = false;

	protected static final int SOFT_SENTENCE_DIFF_THRESHOLD = 10;
	protected static final int HARD_SENTENCE_DIFF_THRESHOLD = 50;

	public abstract AlignmentStats process(File sourceFile, File targetFile, File tmxFile) throws IOException, Exception;

	public abstract void destroy ();


	/**
	 * Processes a list of l1File l2File pairs  
	 * 
	 * @param An org.apache.commons.lang3.tuple.Pair with l1 and l2 files
	 * @param l1
	 * @param l2
	 * @return a list of tmxfiles
	 */
	public List<File> processl1L2Files(List<Pair<File, File>> l1L2Files, String l1, String l2)  {
		int id = 1;
		List<File> tmxFiles = new ArrayList<File>();
		for (Pair<File, File> filePair : l1L2Files) {
			File l1File = filePair.getLeft(); 
			File l2File = filePair.getRight();
			File tmxFile = new File (l1File.getParentFile() + File.separator + StringUtils.join(new String[]{l1, l2, String.valueOf(id)}, "_") + ".tmx");
			String outFile = l1File.getParentFile() + File.separator + StringUtils.join(new String[]{l1, l2, String.valueOf(id)}, "_") + ".out";
			try {
				AlignmentStats as=this.process(l1File, l2File, tmxFile);
				IOtools.writeToFile(outFile, new StringBuffer(as.toString()));
			} catch (Exception e) {
				logger.warn("Cannot align: " + l1File  +  " - " + l2File);
				e.printStackTrace();
			}
			tmxFiles.add(tmxFile);
			id++;
		}
		return tmxFiles;
	}


	public void processCesAlignList(File cesAlignList, boolean oxslt, boolean iso6393)  {
		logger.info("------------Alignment of segments in the detected document pairs.------------");
		List<String> lines=new ArrayList<String>();
		if (cesAlignList.isDirectory()){
			File[] docpairs= cesAlignList.listFiles();
			for (File file:docpairs){
				if (file.getName().contains(UNDERSCORE_STR) && file.getName().endsWith(appXMLext))
					lines.add(file.getAbsolutePath());
			}
		}else{
			try {
				lines = FileUtils.readLines(cesAlignList);
			} catch (IOException e) {
				logger.error("problem in reading the file " +cesAlignList.getAbsolutePath() +" list of detected pairs");
				e.printStackTrace();
			}
		}
		logger.info("Aligning sentences in document pairs using "+ this.getClass().getSimpleName());
		logger.debug("... from cesAlign list " + cesAlignList);
		List<File> tmxFiles = new ArrayList<File>();
		List<File> htmlTmxFiles = new ArrayList<File>();
		List<Integer> alignmentsPerFile = new ArrayList<Integer>();
		Map<String, Integer> alignmentsMap = new HashMap<String, Integer>();
		Map<String, Integer> sourceSentsMap = new HashMap<String, Integer>();
		Map<String, Integer> targetSentsMap = new HashMap<String, Integer>();
		XSLTransformer xslTransformer=null;
		if (oxslt){
			try {
				xslTransformer = new XSLTransformer(tmx_xsl);
			} catch (MalformedURLException e) {
				logger.warn("problem1 in initiating xslTransformer for TMX files");
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				logger.warn("problem1 in initiating xslTransformer for TMX files");
				e.printStackTrace();
			} catch (IOException e) {
				logger.warn("problem1 in initiating xslTransformer for TMX files");
				e.printStackTrace();
			}
		}
		for (String line : lines) {
			logger.debug("Examining document pair: " + line);
			File cesAlignFile = new File(line.trim());
			logger.debug("cesAlign file " + cesAlignFile.getAbsolutePath());
			String cesAlignBasename = FilenameUtils.getBaseName(cesAlignFile.getAbsolutePath());
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Map<String, String> basenameMap = new HashMap<String, String>();
				basenameMap.clear();
				Document doc = dBuilder.parse(cesAlignFile);
				NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("translation");
				for(int i=0; i< translationNodes.getLength(); i++){
					Element translationElement = (Element)translationNodes.item(i);
					File transFile = new File(translationElement.getAttribute("trans.loc"));
					basenameMap.put(translationElement.getAttribute("xml:lang"), FilenameUtils.getBaseName(transFile.getName()));
				}
				//FIXME ugly fix to keep 3-letter for everything but the language code in TMX 
				this.sourceLang= ISOLangCodes.get3LetterCode(this.sourceLang);
				this.targetLang= ISOLangCodes.get3LetterCode(this.targetLang);
				File sourceFile = new File(FilenameUtils.concat(cesAlignFile.getParent(), basenameMap.get(sourceLang) + appXMLext));
				File targetFile = new File(FilenameUtils.concat(cesAlignFile.getParent(), basenameMap.get(targetLang) + appXMLext));
				File tmxFile = 	new File(FilenameUtils.concat(cesAlignFile.getParent(), cesAlignBasename + appTMXext)); 
				if (!iso6393){
					this.sourceLang= ISOLangCodes.get2LetterCode(this.sourceLang);
					this.targetLang= ISOLangCodes.get2LetterCode(this.targetLang);
				}
				AlignmentStats alignmentStats = this.process(sourceFile, targetFile, tmxFile);
				tmxFiles.add(tmxFile);

				File htmlTmxFile = null;
				if (oxslt && xslTransformer!=null){
					htmlTmxFile =   new File(FilenameUtils.removeExtension(tmxFile.getAbsolutePath()) + appTMXHTMLext);
					xslTransformer.setBaseDir(tmxFile.getParent());
					xslTransformer.transform(tmxFile, htmlTmxFile);
					htmlTmxFiles.add(htmlTmxFile);
				}
				alignmentsPerFile.add(alignmentStats.getAlignmentsSize());
				String documentAlignmentMethod = BitextUtils.getDocumentAlignmentMethod(cesAlignFile);
				int count;

				count = sourceSentsMap.containsKey(documentAlignmentMethod) ? sourceSentsMap.get(documentAlignmentMethod) : 0;
				sourceSentsMap.put(documentAlignmentMethod, count + alignmentStats.getSourceSentencesSize());

				count = targetSentsMap.containsKey(documentAlignmentMethod) ? targetSentsMap.get(documentAlignmentMethod) : 0;
				targetSentsMap.put(documentAlignmentMethod, count + alignmentStats.getTargetSentencesSize());

				count = alignmentsMap.containsKey(documentAlignmentMethod) ? alignmentsMap.get(documentAlignmentMethod) : 0;
				alignmentsMap.put(documentAlignmentMethod, count + alignmentStats.getAlignmentsSize());

				logger.debug("Exported results to tmx file " + tmxFile);
				if (oxslt){
					logger.debug("Exported results to html tmx file " + htmlTmxFile);
				}
			} catch (Exception ex) {
				logger.warn( "Problem in generating TMX files: \n"+ ex.getMessage());
			}
		}
		String tempparent = cesAlignList.getParent();
		String tempname =  FilenameUtils.getBaseName(FilenameUtils.getBaseName(cesAlignList.getName()));
		File outputTMXList = new File(FilenameUtils.concat(tempparent, tempname)+TMXlist);
		File outputHTMLTMXList = null;
		if (oxslt)
		 outputHTMLTMXList = new File(FilenameUtils.concat(tempparent, tempname)+TMXHTMLlist);
		
		generateTmxListFiles(outputTMXList, outputHTMLTMXList, lines, tmxFiles,
				htmlTmxFiles, alignmentsPerFile, alignmentsMap, sourceSentsMap,
				targetSentsMap);
	}

	private void generateTmxListFiles(File outputTMXList, File outputHTMLTMXList, List<String> lines, List<File> tmxFiles,
			List<File> htmlTmxFiles, List<Integer> alignmentsPerFile, 	Map<String, Integer> alignmentsMap,
			Map<String, Integer> sourceSentsMap, Map<String, Integer> targetSentsMap) {

		if (outputHTMLTMXList!=null){
			WriteResources.WriteHTMLList(htmlTmxFiles, outputHTMLTMXList);
			logger.info("Created list of tmx in " + outputHTMLTMXList.getAbsolutePath());
		}
		StringBuffer tmxContent = new StringBuffer();
		int tmxI = 0;
		for (File tmxFile : tmxFiles){
			//tmxContent.append(tmxFile.getAbsoluteFile());
			tmxContent.append(tmxFile.getAbsolutePath().replace("\\","/"));
			tmxContent.append(" :: " + alignmentsPerFile.get(tmxI++) + " alignments");
			tmxContent.append("\n");	
		}
		tmxContent.append("\n"
				+ "Document pairs processed by aligner: " + tmxFiles.size()
				+ " out of " + lines.size());
		tmxContent.append("\n");

		tmxContent.append("\n"
				+ "Alignments per type:\n" ) ;
		for (String key: Lists.newArrayList(alignmentsMap.keySet())) {
			tmxContent.append("\t" + key + ":" + alignmentsMap.get(key) +"\n");        	
		}
		tmxContent.append("\n");
		tmxContent.append("\n"
				+ "SL sentences per type:\n" ) ;
		for (String key: Lists.newArrayList(sourceSentsMap.keySet())) {
			tmxContent.append("\t" + key + ":" + sourceSentsMap.get(key)+"\n");        	
		}
		tmxContent.append("\n");
		tmxContent.append("\n"
				+ "TL sentences per type:\n" ) ;
		for (String key: Lists.newArrayList(targetSentsMap.keySet())) {
			tmxContent.append("\t" + key + ":" + targetSentsMap.get(key)+"\n");        	
		}
		tmxContent.append("\n");

		PrintWriter tmxWriter;
		try {
			tmxWriter = new PrintWriter(outputTMXList, "UTF-8");
			tmxWriter.print(tmxContent.toString());
			tmxWriter.close();
		} catch (FileNotFoundException e) {
			logger.warn("Problem in writing the list of the TMX files");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			logger.warn("Encoding problem in writing the list of the TMX files");
			e.printStackTrace();
		}
		logger.info("Created list of tmx in " + outputTMXList.getAbsolutePath());
	}

	public void initialize (String sourceLang, String targetLang) {
		logger.debug("Inititalizing aligner with " + sourceLang +  " " + targetLang + " " + this.toString());
		setSourceLang(sourceLang);
		setTargetLang(targetLang);
	}

	public void initialize(Properties properties) throws Exception {
		this.properties = properties;
		setSourceLang(properties.getProperty("sourceLang"));
		setTargetLang(properties.getProperty("targetLang"));

		sourceLangSentenceSplitter = sentenceSplitterFactory.getSentenceSplitter(getSourceLang());
		targetLangSentenceSplitter = sentenceSplitterFactory.getSentenceSplitter(getTargetLang());

		if (!properties.isEmpty()) {
			logger.debug("Inititalizing aligner with properties ");
			logger.debug(properties.toString());
		}	
	}

	/**
	 * @param file
	 * @param useBoilerplateParagraphs
	 * @return
	 * @throws Exception
	 */
	protected List<String> getParagraphs(File file, boolean useBoilerplateParagraphs) throws Exception {
		return getParagraphs(file, useBoilerplateParagraphs, true,  false);
	}

	/**
	 * @param file
	 * @param useOoiLang
	 * @param useBoilerplateParagraphs
	 * @param preProcessParagraphs
	 * @return
	 * @throws Exception
	 */
	protected List<String> getParagraphs(File file, boolean useBoilerplateParagraphs, 
			boolean useOoilang, boolean preProcessParagraphs) throws Exception {
		List<String> paragraphs = new ArrayList<String>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			NodeList pNodes = doc.getDocumentElement().getElementsByTagName(P_ELE);


			for(int i=0; i< pNodes.getLength(); i++){
				Element pElement = (Element)pNodes.item(i);

				if (isElementToIgnore(pElement)) {
					continue;
				}

				if (pElement.hasAttribute(CRAWLINFO) && pElement.getAttribute(CRAWLINFO).equals(BOILERPLATE) ) {
					continue;
				} else if (useOoilang==false && pElement.hasAttribute(CRAWLINFO) && pElement.getAttribute(CRAWLINFO).equals(OOI_LANG) ) {
					continue;	
				} else {
					if (isPreprocessSentences()) {
						paragraphs.add(PreAlignmentNormalizer.normalizeSentenceForAlignment(
								PreAlignmentNormalizer.unMaskSentence(
										pElement.getTextContent().trim())));
					} else {
						paragraphs.add(pElement.getTextContent().trim());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return paragraphs;
	}

	/**
	 * @param pElement
	 * @return 
	 */
	private boolean isElementToIgnore(Element pElement) {
		if (!attributeValuesToIgnore.isEmpty()) {
			NamedNodeMap attrs = pElement.getAttributes();
			for (int j = 0; j<attrs.getLength(); j++) {
				Attr attribute = (Attr) attrs.item(j);
				if (attributeValuesToIgnore.contains(Pair.of(attribute.getName(), attribute.getValue()))) {
					return true; 
				}
			}
		}
		return false;
	}






	/**
	 * @return the sourceLang
	 */
	public String getSourceLang() {
		return sourceLang;
	}
	/**
	 * @param sourceLang the sourceLang to set
	 */
	public void setSourceLang(String sourceLang) {
		this.sourceLang = sourceLang;
	}
	/**
	 * @return the targetLang
	 */
	public String getTargetLang() {
		return targetLang;
	}
	/**
	 * @param targetLang the targetLang to set
	 */
	public void setTargetLang(String targetLang) {
		this.targetLang = targetLang;
	}

	/**
	 * @return the useBoilerplateParagraphs
	 */
	public boolean isUseBoilerplateParagraphs() {
		return useBoilerplateParagraphs;
	}
	/**
	 * @param useBoilerplateParagraphs the useBoilerplateParagraphs to set
	 */
	public void setUseBoilerplateParagraphs(boolean useBoilerplateParagraphs) {
		this.useBoilerplateParagraphs = useBoilerplateParagraphs;
	}


	public void extractSegments(File outputTMXList, File allL1Segments,
			File allL2Segments) throws IOException {
	}
	/**
	 * @return the useOoiLang
	 */
	public boolean isUseOoiLang() {
		return useOoiLang;
	}
	/**
	 * @param useOoiLang the useOoiLang to set
	 */
	public void setUseOoiLang(boolean useOoiLang) {
		this.useOoiLang = useOoiLang;
	}

	/**
	 * @return the sentenceSplitParagraphs
	 */
	public boolean isSentenceSplitParagraphs() {
		return sentenceSplitParagraphs;
	}

	/**
	 * @param sentenceSplitParagraphs the sentenceSplitParagraphs to set
	 */
	public void setSentenceSplitParagraphs(boolean sentenceSplitParagraphs) {
		this.sentenceSplitParagraphs = sentenceSplitParagraphs;
	}

	/**
	 * @return the attributeValuesToIgnore
	 */
	public List<Pair<String, String>> getAttributeValuesToIgnore() {
		return attributeValuesToIgnore;
	}

	/**
	 * @param attributeValuesToIgnore the attributeValuesToIgnore to set
	 */
	public void setAttributeValuesToIgnore(List<Pair<String, String>> attributeValuesToIgnore) {
		this.attributeValuesToIgnore = attributeValuesToIgnore;
	}

	/**
	 * @return the preprocessSentences
	 */
	public boolean isPreprocessSentences() {
		return preprocessSentences;
	}

	/**
	 * @param preprocessSentences the preprocessSentences to set
	 */
	public void setPreprocessSentences(boolean preprocessSentences) {
		this.preprocessSentences = preprocessSentences;
	}

}
