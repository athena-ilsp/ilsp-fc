package gr.ilsp.fc.monomerge;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gr.ilsp.fc.corpora.MonolingualCorpusInformation;
import gr.ilsp.fc.corpora.MonolingualMetashareDescriptor;
//import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.Eurovoc;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.ZipUtils;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitter;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;

public class MonoMerger {
	private static final Logger LOGGER = Logger.getLogger(MonoMerger.class);
	private static MonoMergerOptions options = null;
	private static File inputFile = null, baseName = null; //, outXMLFile = null;
	private static final String DEFAULT_M_CONFIG_FILE = "FMC_config.xml";
	private static final String TAB_STR = "\t";
	private static final String TXT_EXT = ".txt";
	private static final String ZIP_EXT = ".zip";
	private static final String XML_EXT = ".xml";
	private final static String Metadata_EXT = ".md.xml";
	private static final String DOCLEVEL = "doc";
	private static final String DOCUMENTS = "files";
	private static final String SENLEVEL = "sen";
	private static final String SENTENCES = "sentences";
	private static final String PARLEVEL = "par";
	private static final String PARAGRAPHS = "paragraphs";
	
	private static CompositeConfiguration configuration;
	private static String language, userTopic, corpuslevel;
	private static boolean cc=false;
	private static List<String> sites=new ArrayList<String>();
	private static final int min_char_num = 5;
	private static final int min_tok_num = 5;
	private static final double max_word_length = 25;
	private static final double max_median_word_length = 18;
	//private static final double min_median_word_length1 = 3;
	
	private static SentenceSplitter sentenceSplitter;
	private static final String UNDERSCORE_STR="_";
	private static final String SEMICOLON_STR=";";
	//private final static String PUNCT = ".";
	private final static String UNKNOWN_STR ="unknown";
	//private static final String HTML =".html";
	//private static final String TXTEXT = ".txt";
	
	private final static String domainNode = "domain";
	private final static String FREE_STR="free";
	//private final static String UTF_8 = "UTF-8";
	//private final static String XSL_TMX2HTML ="http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html-no-segtype.xsl";
	private static final String P_ELE = "p";
	private static final String EADDRESS = "eAddress";
	private static final String ooi_crawlinfo = "crawlinfo";
	private String creationDescription = "The ILSP Focused Crawler was used for the acquisition "
			+ "of monolingual data from websites, and for the normalization, cleaning, (near)deduplication on document level.";
	private static boolean iso6393=false;

	public static void main(String[] args) {
		MonoMerger mm = new MonoMerger();
		options = new MonoMergerOptions();
		options.parseOptions(args);
		configuration = getConfig(options.getConfig());
		mm.setConfiguration(configuration);
		mm.setTargetDir(options.getTargetDir());
		mm.setCC(options.getCC());
		mm.setUserTopic(options.getUserTopic());
		SentenceSplitterFactory sentenceSplitterFactory = new SentenceSplitterFactory();
		mm.setSentenceSplitter(sentenceSplitterFactory.getSentenceSplitter(options.getLanguage()));
		mm.setCorpusLevel(options.getCorpusLevel());
		mm.setSites(options.getSites());
		mm.setLanguage(options.getLanguage());
		String[] languages = mm.getLanguage().split(SEMICOLON_STR);
		for (String lang:languages){
			mm.setLanguage(lang);
			mm.setBaseName(new File(options.getBaseName().getAbsolutePath()+UNDERSCORE_STR+lang));
			mm.merge();
		}
	}

	/**
	 * Gets cesDoc XMLs from a directory and adds selected segments of these XMLs in a new XML file, and a TXT file.
	 */
	public void merge() {
		LOGGER.info("------------Constructing a monolingual corpus in "+language+".------------");
		File corpusdoc = new File(baseName.getAbsolutePath());
		LOGGER.info("Type of corpus:\tMonolingual");
		LOGGER.info("level of corpus:\t"+corpuslevel);
		LOGGER.info("filename of corpus:\t"+corpusdoc.getAbsolutePath());
		if (corpuslevel.equals(DOCLEVEL))
			corpusdoc.mkdirs();
		if (corpuslevel.equals(PARLEVEL))
			creationDescription = creationDescription + " Duplicate paragraphs were discarded.";
		if (corpuslevel.equals(SENLEVEL)){
			creationDescription = creationDescription + " First, paragraphs were split into sentences. "
					+ "Sentences with length less than "+ min_tok_num +" tokens (after removing non-letters) were discarded. "
					+ "Duplicate sentences (after removing non-letters) were discarded.";
		}
		if (corpuslevel.equals(PARLEVEL) || corpuslevel.equals(SENLEVEL))
			corpusdoc = new File(corpusdoc.getAbsolutePath()+TXT_EXT);
		
		List<File> xmlfiles = selectCesDocFiles();
		LOGGER.info("targeted files : "+ xmlfiles.size());

		if (!xmlfiles.isEmpty()){
			MonolingualCorpusInformation monlingualCorpusInfo =new MonolingualCorpusInformation();
			monlingualCorpusInfo.setCreationDescription(creationDescription);
			monlingualCorpusInfo.setName(FilenameUtils.getBaseName(corpusdoc.getAbsolutePath()));
			monlingualCorpusInfo.setLang(language);
			monlingualCorpusInfo.setOrganization(configuration.getString("resourceCreator.organization"));
			monlingualCorpusInfo.setOrganizationURL(configuration.getString("resourceCreator.organizationURL"));
			monlingualCorpusInfo.setProjectId(configuration.getString("fundingProject.projectId"));
			monlingualCorpusInfo.setProjectURL(configuration.getString("fundingProject.projectURL"));
		
			int[] sizes = new int[5]; //docs, pars, sents, tokens, words FIXME
			String level="";
			if (corpuslevel.equals(DOCLEVEL)){
				sizes = generateDocLevelMonoCorpus(xmlfiles, corpusdoc);
				//ZipUtils.zipDir(corpusdoc,new File(corpusdoc.getAbsolutePath()+ZIP_EXT));
				level = DOCUMENTS;
			}
			if (corpuslevel.equals(PARLEVEL)){
				sizes = generateParLevelMonoCorpus(xmlfiles, corpusdoc);
				level = PARAGRAPHS;
			}
			if (corpuslevel.equals(SENLEVEL)){
				sizes =  generateSenLevelMonoCorpus(xmlfiles, corpusdoc);
				level = SENTENCES;
			}
			monlingualCorpusInfo.setLevel(level);
			monlingualCorpusInfo.setFilesNum(sizes[0]);
			monlingualCorpusInfo.setParagraphsNum(sizes[1]);
			monlingualCorpusInfo.setSentenceNum(sizes[2]);
			monlingualCorpusInfo.setTokensNum(sizes[3]);
			monlingualCorpusInfo.setLexTypesNum(sizes[4]);
			
			List<String> domains = ReadResources.extactValueFromCesDoc(xmlfiles, domainNode);
			if (domains.isEmpty())
				domains.add(userTopic); 
			List<String> domainEurovocIds=getEurovocId(domains);
			String domain = StringUtils.join(domains, ',');
			String domainEurovocId = StringUtils.join(domainEurovocIds, ',');
			monlingualCorpusInfo.setDomain(domain);
			monlingualCorpusInfo.setDomainId(domainEurovocId);
			
			String description = "Monolingual ("+ language + ") corpus. It consists of ";
			if (corpuslevel.equals(DOCLEVEL))
				description = description + monlingualCorpusInfo.getFilesNum() + " " + DOCUMENTS ;
			if (corpuslevel.equals(PARLEVEL))
				description = description + monlingualCorpusInfo.getParagraphsNum()+ " " + PARAGRAPHS ;
			if (corpuslevel.equals(SENLEVEL))
				description = description + monlingualCorpusInfo.getSentenceNum()+ " " + SENTENCES ;
			description = description + " containing " + monlingualCorpusInfo.getTokensNum() +" tokens and "+ monlingualCorpusInfo.getLexTypesNum() +" lexical types";
			if (!StringUtils.isBlank(domain)) 
				description = description + " in the " + domain + " domain.";
			else
				description = description + ".";
			monlingualCorpusInfo.setDescription(description);
						
			if (cc) 
				monlingualCorpusInfo.setAvailability(FREE_STR); 
			else 
				monlingualCorpusInfo.setAvailability(UNKNOWN_STR); 
			
			LOGGER.info("size of corpus in documents:\t"+monlingualCorpusInfo.getFilesNum());
			LOGGER.info("size of corpus in tokens:\t"+monlingualCorpusInfo.getTokensNum());
			LOGGER.info("size of corpus in lexical types:\t"+monlingualCorpusInfo.getLexTypesNum());
			LOGGER.info("domain of corpus:\t"+monlingualCorpusInfo.getDomain());
			LOGGER.info("description of corpus:\t"+monlingualCorpusInfo.getDescription());
			LOGGER.info("language of corpus:\t"+monlingualCorpusInfo.getLang());

			MonolingualMetashareDescriptor monolingualMetashareDescriptor = new MonolingualMetashareDescriptor(monlingualCorpusInfo);
			File metadatadaFile = new File(baseName.getAbsolutePath()+ Metadata_EXT);
			LOGGER.info("Generating metadata descriptor " + metadatadaFile);
			monolingualMetashareDescriptor.setOutFile(metadatadaFile);
			if (iso6393) 
				monolingualMetashareDescriptor.setMetadataLang("eng");
			else 
				monolingualMetashareDescriptor.setMetadataLang("en");
			monolingualMetashareDescriptor.run();
		}else{
			LOGGER.info("No CesDoc found.");
		}
	}

	
	/**
	 * Selects the cesDoc files (in the targeted language) in the targeted directory or the list of the targeted directories. 
	 * @return
	 */
	private List<File> selectCesDocFiles() {
		FilenameFilter cesDocLangfilter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(XML_EXT) &!arg1.contains(UNDERSCORE_STR) & arg1.startsWith(ISOLangCodes.get3LetterCode(language)));
			}
		};

		List<File> xmlfiles = new ArrayList<File>();
		if (inputFile.isDirectory()){
			xmlfiles = FcFileUtils.listFiles(inputFile, cesDocLangfilter,true);
			/*if (xmlfiles.isEmpty()){
				FilenameFilter filter1 = new FilenameFilter() {			
					public boolean accept(File arg0, String arg1) {
						return (arg1.endsWith(XMLEXT) &!arg1.contains(UNDERSCORE_STR));
					}
				};
				xmlfiles = FcFileUtils.listFiles(inputFile, filter1,true);
			}*/
		}else{ //it is considered a text file containing a list with full paths of targeted directories (a full path per line)
			List<String> targetdirs;
			try {
				targetdirs = FileUtils.readLines(inputFile);
				for (String targetdir:targetdirs){
					LOGGER.info("finding files from "+ targetdir);
					List<File> tfs = FcFileUtils.listFiles(new File(targetdir), cesDocLangfilter,true);
					xmlfiles.addAll(tfs);
				}
			} catch (IOException e) {
				LOGGER.error("Problem in reading file "+ inputFile.getAbsolutePath() );
				e.printStackTrace();
			} 
		}
		return xmlfiles;
	}

	private int[] generateSenLevelMonoCorpus(List<File> xmlfiles, File corpusdoc) {
		int totalTokens=0;
		List<String> paragraphs = new ArrayList<String>();
		List<String> tempSentences = new ArrayList<String>();
		List<String> sentences = new ArrayList<String>();
		Set<String> cleanSentences = new HashSet<String>();
		Set<String> words = new HashSet<String>();
		String cleanSentence="";
		List<String> stokens = new ArrayList<String>();
		int[] sizes = new int[5]; //docs, paragraphs, sentences, tokens, lexicaltypes
		int counter=0, thous=0;
		for (File xmlfile:xmlfiles){
			String docurl = ReadResources.extractNodefromXML(xmlfile.getAbsolutePath(), EADDRESS, false);
			if (!inSites(docurl,sites))
				continue;
			paragraphs=Arrays.asList(ReadResources.extractTextfromXML_clean(xmlfile.getAbsolutePath(),P_ELE,ooi_crawlinfo, false).split("\n"));
			if (paragraphs.isEmpty())
				continue;
			sizes[0] = sizes[0] + 1;
			sizes[1] = sizes[1] + paragraphs.size();

			tempSentences = pars2sents(paragraphs);
			for (String sentence:tempSentences){
				sentence= ContentNormalizer.normalizeText(sentence);
				sentence = sentence.replaceAll("\t", " ");
				sentence = sentence.replaceAll("\r\n", "");
				sentence = sentence.replaceAll("\n", "");
				sentence = sentence.trim();
				cleanSentence = ContentNormalizer.normtext(sentence);
				
				if (cleanSentence.length()<min_char_num)
					continue;
				stokens = FCStringUtils.getTokens(cleanSentence);
				if (stokens.size()<min_tok_num)
					continue;
				Double[] stokenslen= FCStringUtils.getTokensLength(stokens);
				if (Statistics.getMax(stokenslen)>max_word_length 
						|| Statistics.getMedian(stokenslen)>max_median_word_length )//|| Statistics.getMedian(stokenslen)<min_median_word_length)
					continue;

				if (!cleanSentences.contains(cleanSentence)){
					cleanSentences.add(cleanSentence);
					sentences.add(sentence+TAB_STR+docurl+TAB_STR+xmlfile.getName());
					List<String> toks = FCStringUtils.getWords(sentence); 
					totalTokens =totalTokens +toks.size();
					for (String tok:toks){
						if (!words.contains(tok))
							words.add(tok);
					}
				}
			}
			counter++;
			if (counter/1000>thous){
				LOGGER.info((thous*1000)+" files have been processed.");
				thous++;
			}
		}
		LOGGER.info("Sentences:\t"+sentences.size());
		LOGGER.info("Words:\t"+words.size());
		LOGGER.info("Tokens:\t"+totalTokens);
		//docs, paragraphs, sentences, tokens, words
		sizes[2] = sentences.size();
		sizes[3] = totalTokens;
		sizes[4] = words.size();
		try {
			FileUtils.writeLines(corpusdoc, sentences);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sizes;
	}

	private int[] generateParLevelMonoCorpus(List<File> xmlfiles, File corpuspar) {
		int[] sizes = new int[5]; //docs, paragraphs, sentences, tokens, lexicaltypes
		List<String> paragraphs = new ArrayList<String>();
		List<String> total_paragraphs = new ArrayList<String>();
		int totalTokens = 0 ;
		int filecounter=0;
		Set<String> words = new HashSet<String>();
		for (File xmlfile:xmlfiles){
			String docurl = ReadResources.extractNodefromXML(xmlfile.getAbsolutePath(), EADDRESS, false);
			if (!inSites(docurl,sites))
				continue;
			paragraphs=Arrays.asList(ReadResources.extractTextfromXML_clean(xmlfile.getAbsolutePath(),P_ELE,ooi_crawlinfo, false).split("\n"));
			if (paragraphs.isEmpty())
				continue;
			for (String paragraph:paragraphs){
				if (total_paragraphs.contains(paragraph))
					continue;
				String normP = ContentNormalizer.normtext(paragraph);
				if (normP.length()<min_char_num)
					continue;
				List<String> stokens = FCStringUtils.getTokens(normP);
				if (stokens.size()<min_tok_num)
					continue;
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				if (Statistics.getMax(stokenslen)>max_word_length 
						|| Statistics.getMedian(stokenslen)>max_median_word_length )//|| Statistics.getMedian(stokenslen)<min_median_word_length)
					continue;
				
				total_paragraphs.add(paragraph+TAB_STR+docurl+TAB_STR+xmlfile.getName());
			}
			filecounter++;
		}
		try {
			FileUtils.writeLines(corpuspar, total_paragraphs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String paragraph:total_paragraphs){
			List<String> temptokens = FCStringUtils.getTokens(paragraph);
			totalTokens =totalTokens +temptokens.size();
			for (String tok:temptokens){
				if (!words.contains(tok))
					words.add(tok);
			}
		}

		sizes[0] = filecounter;
		sizes[1] = total_paragraphs.size();
		sizes[3] = totalTokens;
		sizes[4] = words.size();
		return sizes;

	}

	private int[] generateDocLevelMonoCorpus(List<File> xmlfiles, File corpusdoc) {
		int[] sizes = new int[5]; //docs, paragraphs, sentences, tokens, lexicaltypes
		List<String> paragraphs = new ArrayList<String>();
		int totalTokens = 0 ;
		int filecounter=0;
		Set<String> words = new HashSet<String>();
		for (File xmlfile:xmlfiles){
			String dochost = ReadResources.extractNodefromXML(xmlfile.getAbsolutePath(), EADDRESS, false);
			if (!inSites(dochost,sites))
				continue;
			paragraphs=Arrays.asList(ReadResources.extractTextfromXML_clean(xmlfile.getAbsolutePath(),P_ELE,ooi_crawlinfo, false).split("\n"));
			if (paragraphs.isEmpty())
				continue;
			sizes[1] = sizes[1] + paragraphs.size();
			try {
				FileUtils.copyFile(xmlfile, new File(FilenameUtils.concat(corpusdoc.getAbsolutePath(),filecounter+XML_EXT)));
				filecounter++;
			} catch (IOException e) {
				LOGGER.error("problem in writing text file for "+xmlfile.getAbsolutePath());
				e.printStackTrace();
			}
			for (String paragraph:paragraphs){
				List<String> temptokens = FCStringUtils.getTokens(paragraph);
				totalTokens =totalTokens +temptokens.size();
				for (String tok:temptokens){
					if (!words.contains(tok))
						words.add(tok);
				}
			}
		}
		sizes[0] = filecounter;
		sizes[3] = totalTokens;
		sizes[4] = words.size();
		return sizes;
	}

	
	private static boolean inSites(String webpage1, List<String> sites) {
		if (!sites.isEmpty()){
			try {
				if (!sites.contains(new URL(webpage1).getHost()))
					return false;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private List<String> pars2sents(List<String> paragraphs) {
		List<String> sentences = new ArrayList<String>();
		for (String paragraph:paragraphs){
			try {
				sentences.addAll(sentenceSplitter.getSentences(paragraph, 1));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sentences;
	}


	/**
	 * Gets Eurovoc id for each element of domain 
	 * @param domain
	 * @return
	 */
	private List<String> getEurovocId(List<String> domains) {
		List<String> domainEurovocIds=new ArrayList<String>();
		Eurovoc eurovoc = Eurovoc.INSTANCE;
		String did="";
		for (String d:domains){
			did = eurovoc.getIdentifier(d);
			if (did!=null)
				domainEurovocIds.add(did);

		}
		return domainEurovocIds;
	}

	/**
	 * absolute path of the baseName for the outfiles  
	 * @param baseName
	 */
	public void setBaseName(File baseName) {
		MonoMerger.baseName  = baseName;
	}
	public File getBaseName() {
		return baseName;
	}
	
	public void setSentenceSplitter(SentenceSplitter sentenceSplitter) {
		MonoMerger.sentenceSplitter = sentenceSplitter; 
	}


	/**
	 * absolute path of the directory containing the TMXs to be merged
	 * @param targetDir
	 */
	public void setTargetDir(File inputFile) {
		MonoMerger.inputFile  = inputFile;
	}

	/**
	 * language iso codes separated by ";"
	 * @param languages
	 */
	public void setLanguage(String language) {
		MonoMerger.language = language;
	}
	public String getLanguage() { 
		return language;
	}
	/**
	 * apply transformation of the generated TMX to HTML. if exists, an HTML file will be created next to the generated TMX
	 * @param offlineXSLT
	 *//*
	public void setApplyOfflineXSLT(boolean offlineXSLT) {
		MonoMerger.oxslt  = offlineXSLT;
	}*/

	public void setCC(boolean cc) {
		MonoMerger.cc  = cc;
	}

	public void setUserTopic(String domain) {
		MonoMerger.userTopic  = domain;
	}

	public void setSites(List<String> sites) {
		MonoMerger.sites  = sites;
	}
	
	public void setCorpusLevel(String level) {
		MonoMerger.corpuslevel  = level;
	}


	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	public static CompositeConfiguration getConfig( String confFile) {
		configuration = new CompositeConfiguration();
		URL default_config = MonoMerger.class.getClassLoader().getResource(DEFAULT_M_CONFIG_FILE);
		if (confFile!=null){
			String custom_config = confFile;
			try {
				XMLConfiguration xml_custom_config = new XMLConfiguration(custom_config);
				xml_custom_config.setValidating(true);
				configuration.addConfiguration(xml_custom_config);
			} catch (ConfigurationException e) {
				LOGGER.error("Invalid configuration file: " + custom_config);
			}
		}
		try {			
			configuration.addConfiguration(new XMLConfiguration(default_config));				
		} catch (ConfigurationException e1) {
			// Shouldn't happen
			LOGGER.error("Problem with default configuration file.");
		}
		return configuration;
	}


	public void setConfiguration(CompositeConfiguration config) {
		MonoMerger.configuration = config;
	}

	/**
	 * @return the iso6393
	 */
	public static boolean isIso6393() {
		return iso6393;
	}

	/**
	 * @param iso6393 the iso6393 to set
	 */
	public static void setIso6393(boolean iso6393) {
		MonoMerger.iso6393 = iso6393;
	}
}
