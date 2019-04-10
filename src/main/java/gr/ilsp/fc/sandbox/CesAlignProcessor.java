package gr.ilsp.fc.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gr.ilsp.nlp.commons.Constants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CesAlignProcessor extends AbstractScanner {

	private static final String SCORE = "score";
	private static final String TYPE = "type";
	private static final String PROP = "prop";
	private static final String SEG = "seg";
	private static final String XML_LANG = "xml:lang";
	private static final String TU = "tu";
	private static final String TUV = "tuv";
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	File allL1Segments = new File("en.txt");
	File allL2Segments = new File("fi.txt");
	File malignaCorpus = new File("maligna-corpus.txt");

	public class Site {
		int l1Sites = 0;
		int l2Sites = 0;
		private String host="";
		public Site(String host) {
			this.host=host;
		}
		public String getSiteInfo() {
			return (host + TAB + l1Sites + TAB + l2Sites);
		};
		@Override
		public String toString() {
			return getSiteInfo();
		}
	}

	private static Logger logger = LoggerFactory.getLogger(CesAlignProcessor.class);

	private static final String L1 = "en";
	private static final String L2 = "fi";
	private static final String BOILERPLATE = "boilerplate";
	private static final String CRAWLINFO = "crawlinfo";
	protected static final String PARA_NAME = "p";

	protected void process(File cesAlignFile) throws IOException {
		logger.debug("Reading cesAlign file " + cesAlignFile.getAbsolutePath());
		// String cesAlignBasename = FilenameUtils.getBaseName(cesAlignFile.getAbsolutePath());
		DocumentBuilder dBuilder = null;
		Map<String, String> basenameMap = new HashMap<String, String>();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			Document doc = dBuilder.parse(cesAlignFile);
			NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("translation");
			for(int i=0; i< translationNodes.getLength(); i++){
				Element translationElement = (Element)translationNodes.item(i);
				File transFile = new File(translationElement.getAttribute("trans.loc"));
				basenameMap.put(translationElement.getAttribute(XML_LANG), FilenameUtils.getBaseName(transFile.getName()));
			}

			File l1File = new File(FilenameUtils.concat(cesAlignFile.getParent(), basenameMap.get(L1) + ".xml"));
			File l2File = new File(FilenameUtils.concat(cesAlignFile.getParent(), basenameMap.get(L2) + ".xml"));

			File l1TxtFile = new File(FilenameUtils.concat(cesAlignFile.getParent(), (basenameMap.get(L1) + ".txt")));
			File l2TxtFile = new File(FilenameUtils.concat(cesAlignFile.getParent(), (basenameMap.get(L2) + ".txt")));

			logger.debug("Will read " + L1 + " file " + l1File);
			logger.debug("Will read " + L2 + " file " + l2File);

			extractText(l1File, l1TxtFile);
			extractText(l2File, l2TxtFile);

			logger.debug("Exported results to " + L1 + " file " + l1TxtFile);
			logger.debug("Exported results to " + L2 + " file " + l2TxtFile);

//			System.out.print(cesAlignFile.getAbsolutePath() + "\t");
//			System.out.print(l1File.getAbsolutePath() + "\t");
//			System.out.print(l2File.getAbsolutePath() + "\t");
//			System.out.print(l1TxtFile.getAbsolutePath() + "\t");
//			System.out.println(l2TxtFile.getAbsolutePath() );

			basenameMap.clear();

		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	public void extractText(File cesFile, File txtFile) {
		try {
			List<String> paragraphs = new ArrayList<String>();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(cesFile);
			NodeList pNodes = doc.getDocumentElement().getElementsByTagName(PARA_NAME);
			for(int i=0; i< pNodes.getLength(); i++){
				Element pElement = (Element)pNodes.item(i);;
				if (pElement.hasAttribute(CRAWLINFO) && pElement.getAttribute(CRAWLINFO).equals(BOILERPLATE) ) {
					continue;
				} else {
					paragraphs.add(pElement.getTextContent().trim());
				}
			}
			FileUtils.writeLines(txtFile, paragraphs,"\n");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error(e.getMessage());
		}
	}

//	private void runAligner(List<File> inputFiles, File cesAlignList, File outputTMXList, File outputHTMLTMXList, String[] langs) throws IOException {
//		FileUtils.writeLines(cesAlignList, inputFiles,"\n");
//		AlignerFactory alignerFactory = new AlignerFactory();
//		Aligner aligner = alignerFactory.getAligner("maligna");
//		Properties properties = new Properties();
//		properties.setProperty("sourceLang", langs[0]);			
//		properties.setProperty("targetLang", langs[1]);			
//		try {
//			aligner.initialize(properties);
//			aligner.processCesAlignList(cesAlignList, outputTMXList, outputHTMLTMXList);
//			extractSegmentsFromTmxFiles(inputFiles);
//		} catch (Exception ex) {
//			logger.error("Aligner cannot be initialized:");
//			ex.printStackTrace();
//			System.exit(0);
//		}
//	}

	private void extractSegmentsFromTmxFiles(List<File> cesAlignFiles) throws ParserConfigurationException, IOException, SAXException {
		int l1id =1 ;
		int l2id =1 ;
		DocumentBuilder dBuilder;
		dBuilder = dbFactory.newDocumentBuilder();
		FileUtils.write(allL1Segments, null);
		FileUtils.write(allL2Segments, null);
		FileUtils.write(malignaCorpus, null);
		for (File cesAlignFile: cesAlignFiles) {

			Document cesAligndoc = dBuilder.parse(cesAlignFile);
			NodeList translationNodes = cesAligndoc.getDocumentElement().getElementsByTagName("translation");

			Map<String, String> basenameMap = new HashMap<String, String>();
			for(int i=0; i< translationNodes.getLength(); i++){
				Element translationElement = (Element)translationNodes.item(i);
				File transFile = new File(translationElement.getAttribute("trans.loc"));
				basenameMap.put(translationElement.getAttribute(XML_LANG), FilenameUtils.getBaseName(transFile.getName()));
			}

			File l1File = new File(FilenameUtils.concat(cesAlignFile.getParent(), basenameMap.get(L1) + ".xml"));
			String eAddress1 = 
					((Element) ((Element) dBuilder.parse(l1File).getDocumentElement().getElementsByTagName("imprint").item(0)).getElementsByTagName("eAddress").item(0)).getTextContent();

			File l2File = new File(FilenameUtils.concat(cesAlignFile.getParent(), basenameMap.get(L2) + ".xml"));
			String eAddress2 = 
					((Element) ((Element) dBuilder.parse(l2File).getDocumentElement().getElementsByTagName("imprint").item(0)).getElementsByTagName("eAddress").item(0)).getTextContent();

			basenameMap.clear();

			logger.info("Extracting segments from " + cesAlignFile);
			File tmxFile = new File(FilenameUtils.removeExtension(cesAlignFile.getAbsolutePath()) + ".tmx");
			List<String> l1segments = new ArrayList<String>();
			List<String> l2segments = new ArrayList<String>();
			Document doc = dBuilder.parse(tmxFile);

			NodeList tuNodes = doc.getDocumentElement().getElementsByTagName(TU);
			String l1Text = "";
			String l2Text = "";
			String score = "";
			for(int tui=0; tui< tuNodes.getLength(); tui++){
				Element tuElement = (Element) tuNodes.item(tui);
				NodeList tuvNodes = tuElement.getElementsByTagName(TUV);
				int tuvNodesLength =0;
				for(int i=0; i< tuvNodes.getLength(); i++){
					tuvNodesLength++;
					Element tuvElement = (Element) tuvNodes.item(i);
					NodeList segNodes = tuvElement.getElementsByTagName(SEG);
					String segText = "";
					for(int j=0; j< segNodes.getLength(); j++){
						Element segElement = (Element) segNodes.item(j);
						segText = segText + Constants.SPACE + segElement.getTextContent();
					}
					if (tuvElement.hasAttribute(XML_LANG) && tuvElement.getAttribute(XML_LANG).equals(L1) ) {
						l1Text =   escapeXml (segText.trim());
						l1segments.add("<p id=\""+ (l1id) +"\">" + l1Text +"</p>");
						l1id++;
					} else if (tuvElement.hasAttribute(XML_LANG) && tuvElement.getAttribute(XML_LANG).equals(L2) ) {
						l2Text =   escapeXml (segText.trim());
						l2segments.add("<p id=\""+ (l2id) +"\">" + l2Text  +"</p>");
						l2id++;
					}
				
				}

				// C:\tmp\sample-input\tourism-hanko-fi_20150318_172926\2c5a9f72-ec11-47c3-b537-669c6af5eed7\xml\144.xml   
				// C:\tmp\sample-input\tourism-hanko-fi_20150318_172926\2c5a9f72-ec11-47c3-b537-669c6af5eed7\xml\159.xml 
				// Take a walk on the cape where the mainland ends, and you are met by the endless sea and unique nature.        
				// Hangon historia perustuu kaupungin erinomaiseen sijaintiin suojasatamineen.    
				//0.244286

				if (tuvNodesLength==2) {
					NodeList propNodes = tuElement.getElementsByTagName(PROP);
					
					for(int pi=0; pi< propNodes.getLength(); pi++){
						Element propElement = (Element) propNodes.item(pi);
						if (propElement.hasAttribute(TYPE) && propElement.getAttribute(TYPE).equals(SCORE) ) {
							score= propElement.getTextContent();
						}
					}

					FileUtils.write(malignaCorpus, 
							(
									l1File.getAbsolutePath() + "\t" +
									l2File.getAbsolutePath() + "\t" +
									l1Text + "\t" + l2Text + "\t" + 
									score + "\t"+
									cesAlignFile.getAbsolutePath() + "\n"	), true);
				} else {
					logger.debug("Skipping tu of length " + tuvNodesLength+"");
				}
	
			}
			//			<aligFile>/home/vpapa/ABU/EN-FI/tourism-hanko-fi_20150318_172926/2c5a9f72-ec11-47c3-b537-669c6af5eed7/xml/144_159_i.xml</aligFile>
			//			<file>/home/vpapa/ABU/EN-FI/tourism-hanko-fi_20150318_172926/2c5a9f72-ec11-47c3-b537-669c6af5eed7/xml/159.xml</file>
			//			<setting>fc-all</setting>
			//			<eAddress>http://tourism.hanko.fi/kavelyreitit-luonnossa/</eAddress>
			//			<language iso639="fi"/>

			FileUtils.write(allL1Segments, "<aligFile>" + cesAlignFile.getAbsolutePath() + "</aligFile>\n", true);
			FileUtils.write(allL1Segments, "<file>" + l1File.getAbsolutePath() + "</file>\n", true);
			FileUtils.write(allL1Segments, "<setting>fc-all</setting>\n", true);
			FileUtils.write(allL1Segments, "<eAddress>"+ eAddress1+"</eAddress>\n", true);
			FileUtils.write(allL1Segments, "<language iso639=\""+ L1 + "\"/>\n", true);
			FileUtils.writeLines(allL1Segments, l1segments, "\n", true);

			FileUtils.write(allL2Segments, "<aligFile>" + cesAlignFile.getAbsolutePath()+ "</aligFile>\n", true);
			FileUtils.write(allL2Segments, "<file>" + l2File.getAbsolutePath() + "</file>\n", true);
			FileUtils.write(allL2Segments, "<setting>fc-all</setting>\n", true);
			FileUtils.write(allL2Segments, "<eAddress>"+ eAddress2+"</eAddress>\n", true);
			FileUtils.write(allL2Segments, "<language iso639=\""+ L2 + "\"/>\n", true);
			FileUtils.writeLines(allL2Segments, l2segments, "\n", true);
			
		}
	}

	public String escapeXml(String s) {
		return s.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException  {
		CesAlignProcessor app = new CesAlignProcessor();
		
		app.createOptions();
		app.parseOptions(args);
		List<File> inputFiles =  app.getFilesFromInputDirectory(app.getInputDir(), app.getFileFilterRegex(), app.isProcessRecursively());
		//app.process(inputFiles);
		//app.extractCesDocs(inputFiles);

//		File cesAlignList = new File("ces-align-list.txt");
//		File outputTMXList = new File("tmx-list.txt");
//		File outputHTMLTMXList = new File("tmx-html-list.html");
//		String[] langs = {"en", "fi"};
		
		//app.runAligner(inputFiles, cesAlignList, outputTMXList, outputHTMLTMXList, langs);
		//app.printResults();
		app.extractSegmentsFromTmxFiles(inputFiles);
		
		logger.info("Processed " + inputFiles.size() + " files.");
	}

}
