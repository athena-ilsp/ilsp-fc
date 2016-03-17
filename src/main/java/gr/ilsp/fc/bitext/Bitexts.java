package gr.ilsp.fc.bitext;

import gr.ilsp.fc.main.ImageExtractor;
import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.evt.XMLEvent2;


public class Bitexts {
	private static final Logger LOGGER = Logger.getLogger(Bitexts.class);
	private static final String LANGUAGE_ELE = "language";
	private static final String P_ELE = "p";
	private static final String PUNCT = ".";
	//private final static double term_thresh=0.5;
	//private static int diagonal_beam=5;
	private static final String URL_ELE = "eAddress";
	private static final String appXMLext = ".xml";
	private static final String appTXText = ".txt";

	private static final String ooi_crawlinfo = "crawlinfo";
	private static final String ooi_boilerplate="boilerplate";
	private static final String ooi_lang = "ooi-lang";
	private static final String ooi_length = "ooi-length";
	private static final String type_title = "title";
	private static final String type_heading = "heading";
	private static final String type_listitem = "listitem";
	private static final String type_topic = "topic";
	private static final int title_code = -2;
	private static final int heading_code = -3;
	private static final int listitem_code =  -4;
	private static final int topic_code =  -5 ;
	private static final String h_struct_simil = "high";
	private static final String m_struct_simil = "medium";
	private static final String l_struct_simil = "low";
	private static final String u_type = "u";
	private static final String d_type = "d";
	private static final String a_type = "a";
	private static final String im_type = "i";
	private static final String h_type = "s";
	private static final String m_type = "m";
	private static final String l_type = "l";
	private static final String imdi_type = "p";
	private static final String UNDERSCORE="_";
	private static HashMap<String, DocVector> features = new HashMap<String, DocVector>();		//keeps features of docs 
	private static HashMap<String, DocVector> features_paired = new HashMap<String, DocVector>(); //keeps features of docs that have been considered pairs
	private static HashMap<String, String[]> imagesInHTML=new HashMap<String,String[]>();

	public static void main(String[] args) {
		//String ttt = ReadResources.extractAttrfromXML("C:/Users/vpapa/OLDocPro/Systems/HMM/test_images/im1.xml", "TextLine", "id", true);
		String outputDir = args[0];
		String resultXMLDir="xml";
		String methods =args[1];
		String[] langs = args[2].split(";");
		File outputfile = new File(FilenameUtils.concat(outputDir,"test.txt"));
		File gt = new File(args[3]);
		File inxmldir = new File(FilenameUtils.concat(outputDir,resultXMLDir));
		ArrayList<String[]> bitextsALL = findPairsUIDS(inxmldir,methods, langs,null, outputDir,null, true, false, gt);
		for (int ii=0;ii<bitextsALL.size();ii++){
			LOGGER.info(bitextsALL.get(ii)[0]+"_"+ bitextsALL.get(ii)[1]);
		}
		WriteBitexts.writeOutList(inxmldir,outputfile,new File(outputfile.getAbsolutePath()+".html"),bitextsALL);
	}


	/**
	 * Parses the cesDoc files in the xmldir directory and for each file (which ends with appXMLext) extracts features
	 * and stores them in a DocVector.
	 * Also generates a text file with the fingerprint of the cesDoc. 
	 * @param xmldir input directory
	 * @param langs 
	 * @return HashMap with features of the cesDoc files. Key is the filename of the cesDoc (with no extension) 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * 
	 */
	public static HashMap<String, DocVector> extractXML_Features(File xmldir, List<String> langs) {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(appXMLext) && !arg1.contains(UNDERSCORE));
			}
		};
		String[] files= xmldir.list(filter);
		HashMap<String, DocVector> res=new HashMap<String, DocVector>();
		String url="", curElement="";
		String codelang="", listofDigits, fileFinger, listofSymbols ;
		int urllevel=0, numofpars, numofToksb, numofTokso, numofcleanpars  ;
		int counter=1;
		boolean skip;
		for (int ii=0; ii<files.length ; ii++){
			skip=false;
			fileFinger =  files[ii]+appTXText;
			numofpars=0; numofToksb=0; numofTokso=0; numofcleanpars=0;
			String tempstr="", newtempstr="";
			OutputStreamWriter xmlFileListWrt = null;
			try {
				xmlFileListWrt = new OutputStreamWriter(new FileOutputStream
						(FilenameUtils.concat(xmldir.getPath(),fileFinger)),"UTF-8");
				int eventType=0;
				XMLInputFactory2 xmlif = null;
				xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
				xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,
						Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,
						Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
				xmlif.configureForSpeed();
				XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.
						createXMLStreamReader(new FileInputStream(FilenameUtils.concat(xmldir.getPath(),files[ii])),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) {
							codelang=ISOLangCodes.get3LetterCode(xmlr.getAttributeValue(0));
							if (!langs.contains(codelang)){
								skip=true;
								break;
							}
						}else{
							if (curElement.equals(URL_ELE)){
								if (xmlr.getAttributeCount()<1){
									url=xmlr.getElementText();
									urllevel = BitextUtils.getURLlevel(url);
								}
							}else{
								if (curElement.equals(P_ELE)){
									numofpars=numofpars+1;
									int attrs=xmlr.getAttributeCount();
									int t=-1, t1=0, t10=0;
									for (int m=1;m<attrs;m++){
										if (xmlr.getAttributeValue(m).equals(ooi_boilerplate)){
											t=0; 
											break;
										}
										if (xmlr.getAttributeValue(m).equals(type_title))
											t= title_code;
										if (xmlr.getAttributeValue(m).equals(type_heading))
											t= heading_code;
										if (xmlr.getAttributeValue(m).equals(type_listitem))
											t= listitem_code;
										if (xmlr.getAttributeLocalName(m).equals(type_topic))
											t1= topic_code;
										if (m==1 && !xmlr.getAttributeValue(m).equals(ooi_length) && !xmlr.getAttributeValue(m).equals(ooi_lang))
											t10=1; 
									}
									if (t!=0){
										numofcleanpars = numofcleanpars+1;
									}
									if (t<0){
										if (t==title_code)
											xmlFileListWrt.write(Integer.toString(title_code)+"\n");
										if (t==heading_code)
											xmlFileListWrt.write(Integer.toString(heading_code)+"\n");
										if (t==listitem_code)
											xmlFileListWrt.write(Integer.toString(listitem_code)+"\n");
									}
									if (t1<0)
										xmlFileListWrt.write(Integer.toString(topic_code)+"\n");	
									String tempstr1 = xmlr.getElementText();
									if (t<0 | t1<0) {
										int temp = tempstr1.length();
										xmlFileListWrt.write(Integer.toString(temp)+"\n");
										tempstr = tempstr+ " "+tempstr1;
									}
									if (t10==1 | attrs==1)
										newtempstr = newtempstr + " "+ tempstr1;
								}
							}
						}
					}else
						curElement = "";
				}
				if (!skip){
					numofToksb = FCStringUtils.countTokens(tempstr);
					numofTokso = FCStringUtils.countTokens(newtempstr);

					listofDigits=ReadResources.extractNumsfromXML(FilenameUtils.concat(xmldir.getPath(),files[ii]), P_ELE,ooi_crawlinfo, ooi_boilerplate, false);
					//listofSymbols = ReadResources.extractSymbolsfromXML(FilenameUtils.concat(xmldir.getPath(),files[ii]), P_ELE,ooi_crawlinfo, ooi_boilerplate, false);
					listofSymbols ="";
					DocVector filevector= new DocVector(url, urllevel, codelang, numofpars, numofcleanpars, numofToksb, numofTokso, listofDigits, fileFinger, listofSymbols); 

					if (res.containsKey(files[ii].substring(0, files[ii].indexOf(PUNCT))))
						System.out.println("OOPS");
					res.put(files[ii].substring(0, files[ii].indexOf(PUNCT)), filevector);
				}
				xmlFileListWrt.close();
				xmlr.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Problem in respesenting the "+ files[ii]+" cesDoc file.");
			}
			if (ii/1000 >=counter){
				LOGGER.info("features for "+ (counter*1000)+ " files have been extracted.");
				counter++;
			}
		}
		return res;
	}

	/**
	 * Extracts features of each cesDoc and identifies pairs by several methods (links, URLS, images, digits, structure). 
	 * It generates cesAlign files for each detected pair, their xslt transformation if asked, and text file(s) with a list of paths of generated cesAlign ( with a list of links pointing to transformed cesAlign)  
	 * @param xmldir	: the directory with the cesDoc files
	 * @param methods	: the methods to be applied for pair detection
	 * @param langs		: targeted languages
	 * @param hreflangIDPairs
	 * @param outputDirName
	 * @param urlRepls		: url patterns to be checked.
	 * @param offlineXSLTT	: if true creates transformed cesAlign files.
	 * @param keepimpath	: Used for image filename detection/storing. If true, uses the whole image path, else only the image filename.
	 * @param groundTruth 
	 * @return
	 */
	public static ArrayList<String[]> findPairsUIDS(File xmldir, String methods, String[] langs,  Map<String, String> hreflangIDPairs,
			String outputDirName, String[][] urlRepls, boolean offlineXSLT, boolean keepimpath, File groundTruth) {

		ArrayList<String[]> bitextsALL=new ArrayList<String[]>();
		LOGGER.info("Feature extraction from generated cesDoc and corresponding HTML files");
		//extracts features and images of docs
		features = Bitexts.extractXML_Features(xmldir, Arrays.asList(langs));
		//Are there enough docs for identification of pairs?
		int[] langfiles=BitextUtils.check_crawl_stats(langs, features);
		if (langfiles==null)
			return bitextsALL;
		int minlangfiles=Collections.min(Arrays.asList((ArrayUtils.toObject(langfiles))));
		LOGGER.info("document pairing");
		if (methods.contains(im_type)|| methods.contains(imdi_type)){
			imagesInHTML=ImageExtractor.findImages(xmldir,keepimpath, minlangfiles*2);
			LOGGER.info( imagesInHTML.size() + " files contain at least one image");
		}
		//keeps features of docs that have been considered pairs
		features_paired = new HashMap<String, DocVector>();

		ArrayList<String[]> bitexts= new ArrayList<String[]>();

		// Store pairs based on hreflangs (have been detected during crawl)
		//generates cesAlign based on this method, updates list with bitexts, calculates tokens for this method
		if (methods.contains(a_type)){
			if (hreflangIDPairs!=null){
				bitexts=BitextsURLs.findpairsHRefLang(hreflangIDPairs, features);
				if (bitexts.size()>0){
					LOGGER.info(bitexts.size()+ " pairs found (based on links).");
					WriteBitexts.writeXMLs(outputDirName,bitexts,offlineXSLT);
					moveFilesinPairs(bitexts,features, features_paired,imagesInHTML);
					for (int ii=0;ii<bitexts.size();ii++)
						bitextsALL.add(bitexts.get(ii));
					BitextUtils.calcToksperLang(features_paired,bitexts,a_type);
					LOGGER.info(features.size()+ " files still remained for pair detection.");
				}else{
					LOGGER.info("No pairs found (based on links)");
				}
			}else{
				LOGGER.info("No pairs found (based on links)");
			}
		}
		//Find URLs of docs and pairs based on URLs
		if (methods.contains(u_type)){
			if (features.size()>1){
				//Find pairs based on URLs
				bitexts=BitextsURLs.findpairsURLs(features,urlRepls);
				if (bitexts.size()>0){
					LOGGER.info(bitexts.size()+ " pairs found (based on URLs).");
					WriteBitexts.writeXMLs(outputDirName,bitexts,offlineXSLT);
					moveFilesinPairs(bitexts,features, features_paired,imagesInHTML);
					for (int ii=0;ii<bitexts.size();ii++)
						bitextsALL.add(bitexts.get(ii));
					BitextUtils.calcToksperLang(features_paired,bitexts,u_type);
					LOGGER.info(features.size()+ " files still remained for pair detection.");
				}else{
					LOGGER.info("No pairs found (based on URLs)");
				}
			}
		}
		//Find pairs based on common images and digits

		if (methods.contains(imdi_type)){
			if (features.size()>1){
				bitexts=new ArrayList<String[]>();
				if (imagesInHTML.size()>1){
					bitexts=BitextsImages.findpairsIMDI(imagesInHTML,features);
					if (bitexts.size()>0){
						LOGGER.info(bitexts.size()+ " pairs found (based on images and digits).");
						WriteBitexts.writeXMLs(outputDirName,bitexts,offlineXSLT);
						moveFilesinPairs(bitexts,features, features_paired,imagesInHTML);
						for (int ii=0;ii<bitexts.size();ii++)
							bitextsALL.add(bitexts.get(ii));
						BitextUtils.calcToksperLang(features_paired,bitexts,imdi_type);
						LOGGER.info(features.size()+ " files still remained for pair detection.");
					}else
						LOGGER.info("No pairs found (based on combination of images and digits)");
				}else{
					LOGGER.info("No pairs found (based on combination of images and digits)");
				}
			}
		}
		//Find pairs based on common digits
		if (methods.contains(d_type)){
			if (features.size()>1){
				bitexts = BitextsDigits.findpairsDig(features);
				if (bitexts.size()>0){
					LOGGER.info(bitexts.size()+ " pairs found (based on digits).");
					WriteBitexts.writeXMLs(outputDirName,bitexts,offlineXSLT);
					moveFilesinPairs(bitexts,features, features_paired,imagesInHTML);
					for (int ii=0;ii<bitexts.size();ii++)
						bitextsALL.add(bitexts.get(ii));
					BitextUtils.calcToksperLang(features_paired,bitexts,d_type);
					LOGGER.info(features.size()+ " files still remained for pair detection.");
				}else
					LOGGER.info("No pairs found (based on common digits)");
			}
		}
		//Find pairs based on common images
		if (methods.contains(im_type)){
			if (features.size()>1){
				bitexts=new ArrayList<String[]>();
				if (imagesInHTML.size()>1){
					bitexts=BitextsImages.findpairsIM(imagesInHTML,features);
					if (bitexts.size()>0){
						LOGGER.info(bitexts.size()+ " pairs found (based on images).");
						WriteBitexts.writeXMLs(outputDirName,bitexts,offlineXSLT);
						moveFilesinPairs(bitexts,features, features_paired,imagesInHTML);
						for (int ii=0;ii<bitexts.size();ii++)
							bitextsALL.add(bitexts.get(ii));
						BitextUtils.calcToksperLang(features_paired,bitexts,im_type);
						LOGGER.info(features.size()+ " files still remained for pair detection.");
					}else
						LOGGER.info("No pairs found (based on images)");
				}
			}
		}

		//Find pairs based on common symbols
		/*if (methods.contains(s_type)){	
				if (features.size()>1){
					bitexts = BitextsDigits.findpairsSym(features);
					if (bitexts.size()>0){
						LOGGER.info(bitexts.size()+ " pairs found (based on symbols).");
						WriteBitexts.writeXMLs(outputDirName,bitexts,offlineXSLT);
						moveFilesinPairs(bitexts,features, features_paired);
						for (int ii=0;ii<bitexts.size();ii++)
							bitextsALL.add(bitexts.get(ii));
						BitextUtils.calcToksperLang(features_paired,bitexts,d_type);
						LOGGER.info(features.size()+ " files still remained for pair detection.");
					}else
						LOGGER.info("No pairs found (based on common digits)");
				}
			}*/
		//Find pairs based on similar structures
		if (methods.contains(h_type) || methods.contains(m_type) || methods.contains(l_type)){
			if (features.size()>1){
				bitexts  = BitextsStruct.findpairsXML_SVM_NEW(xmldir,features);
				ArrayList<String[]> bitextsSTRUCT = BitextsStruct.findBestPairs_SVM_NEW(bitexts);
				if (bitextsSTRUCT.size()>0){
					int[] counters = BitextsStruct.getPairProps(bitextsSTRUCT);
					WriteBitexts.writeXMLs(outputDirName,bitextsSTRUCT,offlineXSLT);
					LOGGER.info("Pairs found (based on structure) : "+bitextsSTRUCT.size());
					LOGGER.info("(with high similarity) : " + counters[0]);
					if (counters[0]>0){
						BitextUtils.calcToksperLang(features,bitextsSTRUCT,h_struct_simil);
					}
					LOGGER.info("(with medium similarity) : " + counters[1]);
					if (counters[1]>0){
						BitextUtils.calcToksperLang(features,bitextsSTRUCT,m_struct_simil);
					}
					LOGGER.info("(with low similarity) :  "+ counters[2]);
					if (counters[2]>0){
						BitextUtils.calcToksperLang(features,bitextsSTRUCT,l_struct_simil);
					}
					moveFilesinPairs(bitextsSTRUCT,features, features_paired,imagesInHTML);
					for (int ii=0;ii<bitextsSTRUCT.size();ii++)
						bitextsALL.add(bitextsSTRUCT.get(ii));
				}else
					LOGGER.info("No pairs found (based on structure)");
			}
		}
		//Total results on document level
		if (bitextsALL.size()>0){
			bitextsALL = BitextUtils.sortbyLength(bitextsALL);
			BitextUtils.calcToksperLang(features_paired,bitextsALL,"");
			if (groundTruth!=null){
				evalPairingMethod(bitextsALL, groundTruth);
			}
		}else{
			LOGGER.info("No pairs found");
		}
		return bitextsALL;
	}

	private static void evalPairingMethod(ArrayList<String[]> bitexts,	File groundTruth) {
		HashMap<String,String> truepairs = BitextUtils.getTruePairs( groundTruth.getAbsolutePath());
		HashMap<String,int[]> results = new HashMap<String,int[]>();
		LOGGER.info("Found pairs: "+ bitexts.size());
		for (int ii=0;ii<bitexts.size();ii++){
			String key1 = bitexts.get(ii)[0];
			String key2 = bitexts.get(ii)[1];
			String method =bitexts.get(ii)[4];
			boolean correct=false;
			if ((truepairs.containsKey(key1) && truepairs.get(key1).equals(key2)) || (truepairs.containsKey(key2) && truepairs.get(key2).equals(key1))){
				System.out.println("http://nlp.ilsp.gr/pgv/data/"+key1+".html\t"+"http://nlp.ilsp.gr/pgv/data/"+key2+".html\t"+method+"\t"+bitexts.get(ii)[5]+"\t1");
				correct=true;
			}else{
				System.out.println("http://nlp.ilsp.gr/pgv/data/"+key1+".html\t"+"http://nlp.ilsp.gr/pgv/data/"+key2+".html\t"+method+"\t"+bitexts.get(ii)[5]+"\t0\thttp://nlp.ilsp.gr/pgv/data/"+truepairs.get(key1)+".html");
			}
			int[] res=new int[2];
			if (results.containsKey(method)){
				res= results.get(method);
				if (correct)
					res[0] = res[0] + 1;
				else
					res[1] = res[1] + 1;
			}else{
				if (correct){
					res[0]=1; res[1]=0;
				}else{
					res[0]=0; res[1]=1;
				}			
			}
			results.put(method, res);
		}
		Set<String> methods = results.keySet();
		Iterator<String> method = methods.iterator();
		String method_it;
		int pos=0, neg=0;
		while (method.hasNext()){
			method_it = method.next();
			int[] res=results.get(method_it);
			LOGGER.info("Method\t"+ method_it +"\tCorrect =\t"+ res[0]+"\tWrong =\t"+ res[1]);
			pos = pos+res[0];
			neg=neg+res[1];
		}
		LOGGER.info("Total results\t\tCorrect =\t"+ pos+"\tWrong =\t"+ neg);
	}



	/**
	 * Removes DocVectors of files that have been already paired (i.e. are in pairs ) from features and puts them in features_paired
	 * Note that each element of pairs holds filename1, filename2, lang1, lang2, total num of tokens 
	 * @param pairs
	 * @param features
	 * @param images 
	 * @return
	 */
	public static void moveFilesinPairs(ArrayList<String[]> pairs, HashMap<String,DocVector> features, HashMap<String,DocVector> features_paired, HashMap<String, String[]> imagesInHTML) {
		for (int ii=0;ii<pairs.size();ii++){
			String[] temp = pairs.get(ii);
			features_paired.put(temp[0], features.get(temp[0]));
			features_paired.put(temp[1], features.get(temp[1]));
			features.remove(temp[0]);
			features.remove(temp[1]);
			imagesInHTML.remove(temp[0]);
			imagesInHTML.remove(temp[1]);
		}
	}

	/**
	 * urlLevel: the level in the website, codeLang: the iso code of the detected language, numPars:  the total # of paragraphs,
	 *  numToksnoBoil and numToksnoOOI:  the # of tokens in the clean text (no boilerplate) and (no boilerplate and ooi-lang) respectively
	 *  digitList: the list of digits, fileFinger: the text file with the fingerprint.
	 * 
	 *
	 */
	public static class DocVector {
		public String url;
		public int urlLevel;
		public String codeLang;
		public double numPars;
		public double numCleanPars;
		public double numToksnoBoil;
		public double numToksnoOOI;
		public String digitList;
		public String fileFinger;
		public String symbolList;

		public DocVector(String url, int urlLevel, String codeLang, double numPars, double numCleanPars, double numToksnoBoil, double numToksnoOOI, String digitList, String fileFinger, String listofSymbols) {
			this.url = url;
			this.urlLevel = urlLevel;
			this.codeLang = codeLang;
			this.numPars = numPars;
			this.numCleanPars = numCleanPars;
			this.numToksnoBoil = numToksnoBoil;
			this.numToksnoOOI = numToksnoOOI;
			this.digitList = digitList;
			this.symbolList = listofSymbols;
			this.fileFinger = fileFinger;
		}
	}
}
