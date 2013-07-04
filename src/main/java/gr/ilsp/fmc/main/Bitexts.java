package gr.ilsp.fmc.main;

import gr.ilsp.fmc.exporter.XSLTransformer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.List;
//import java.util.List;
//import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import org.apache.log4j.Logger;
//import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

//import org.apache.hadoop.fs.FileSystem;
//import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.mapred.JobConf;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.evt.XMLEvent2;
//import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Bitexts {
	private static final Logger LOGGER = Logger.getLogger(Bitexts.class);
	private static final String cesDocVersion = "1.0";
	private static final String LANGUAGE_ELE = "language";
	//private static final String EADDRESS = "eAddress";
	//private static final String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://nlp.ilsp.gr/soaplab2-results/";	
	private static String cesNameSpace = "http://www.w3.org/1999/xlink";
	private static String cesNameSpace1 = "http://www.xces.org/schema/2003";
	private static String cesNameSpace2 = "http://www.w3.org/2001/XMLSchema-instance";
	private static String fs = System.getProperty("file.separator");
	//private final static String appendXmlExt = ".xml";
	//private final static double term_thresh=0.5;
	//private static int diagonal_beam=5;
	private static final String URL_ELE = "eAddress";
	private static double text_thres=0.4;
	private static double length_thres=0.4;
	private static double pars_thres=0.4;
	private static int level_thres=2;
	private static double jac_thr=0.6;
	private static int im_dif_thr=3;
	private static int minnumofpars=3;
	private static XSLTransformer xslTransformer = null;
	private static String cesAlingURL="http://nlp.ilsp.gr/xslt/ilsp-fc/cesAlign.xsl";
	public static void main(String[] args) {

		File xmldir = new File("C:\\QTLaunchPad\\Medicine\\EN-DE\\b50977c5-3596-410d-989b-8b7132e404f0\\xml");
		ArrayList<String[]> bitextsURLs=new ArrayList<String[]>();
		HashMap<String, String> filesURLS = findURLs(xmldir);
		HashMap<String, String[]> props;
		try {
			props = representXML_NEW(xmldir);
			bitextsURLs=Bitexts.findpairsURLs(filesURLS,props);
			for (int ii=0;ii<bitextsURLs.size();ii++){
				System.out.println(bitextsURLs.get(ii)[0]);
				System.out.println(bitextsURLs.get(ii)[1]);
				System.out.println(bitextsURLs.get(ii)[2]);
				System.out.println(bitextsURLs.get(ii)[3]);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}




	}

	/*	private static void counttoks(File xmldir, String outfile) {
		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] files= xmldir.list();
		int tokens=0; 
		int counter=0;
		for (int ii=0; ii<files.length ; ii++){
			File fileAlign=new File(files[ii]);
			String f1 = fileAlign.getName();
			if (f1.endsWith(".html")) continue;
			counter++;
			int temp=0;
			try {
				int eventType=0;
				String lang="", curElement="";
				XMLInputFactory2 xmlif = null;
				xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
				xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
				xmlif.configureForSpeed();
				XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+f1),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) {
							lang=xmlr.getAttributeValue(0);
						}else{
							if (curElement.equals("p")){
								int attrs=xmlr.getAttributeCount();
								int t=-1;
								for (int m=1;m<attrs;m++){
									if (xmlr.getAttributeValue(m).equals("boilerplate")
											|| xmlr.getAttributeValue(m).equals("ooi-lang")
											|| xmlr.getAttributeValue(m).equals("ooi-length")){
										t=0; 
										break;
									}
								}
								if (t<0) {
									String tempstr1 = xmlr.getElementText();
									StringTokenizer st = new StringTokenizer(tempstr1);	
									//double words_num=st.countTokens();
									temp = temp+st.countTokens();
								}
							}
						}
					}else
						curElement = "";
				}
				tokens=tokens+temp;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		System.out.println(counter+ " files");
		System.out.println(tokens);
	}*/

	/*	private static void moveoutputfiles(File xmldir, File xmldirnew,	String outfile) {
		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int ii=0;ii<outlist.size();ii++){
			String f1 = outlist.get(ii);
			String f1h=f1.substring(0, f1.indexOf("."))+".html";
			copyfile(xmldir.toString()+fs+f1,xmldirnew.toString()+fs+f1);
			copyfile(xmldir.toString()+fs+f1h,xmldirnew.toString()+fs+f1h);
		}
	}*/

	/*	private static void checklistfiles(File xmldir, String outfile) {
		String[] files= xmldir.list();
		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int ii=0; ii<files.length ; ii++){
			File fileAlign=new File(files[ii]);
			String f1 = fileAlign.getName();
			if (f1.endsWith(".html")) continue;
			if (!outlist.contains(f1))
				System.out.println(f1);
		}
		System.out.println("-----");
		for (int ii=0; ii<outlist.size() ; ii++){
			boolean found =false;
			for (int jj=0; jj<files.length; jj++){
				if (outlist.get(ii).equals(files[jj]))
					found=true;
			}
			if (!found)
				System.out.println(outlist.get(ii));
		}
	}*/

	/*	private static void removepages(String outfile, String outfile_new, String removefile) {
		ArrayList<String> outlist =new ArrayList<String>();
		//ArrayList<String> outlist_new =new ArrayList<String>();
		ArrayList<String> removelist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader in1;
		String str1="";
		try {
			in1 = new BufferedReader(new FileReader(removefile));
			while ((str1 = in1.readLine()) != null) {
				removelist.add(str1.substring(str1.lastIndexOf("/")+1));
			}
			in1.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int ii=0;ii<removelist.size();ii++){
			if (outlist.contains(removelist.get(ii))){
				System.out.println(removelist.get(ii)+" found");
				outlist.remove(removelist.get(ii));
			}
			//else
			//	System.out.println(removelist.get(ii)+"aa");
		}
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile_new),"UTF-8"));
			for (int ii=0;ii<outlist.size();ii++){
				out.write(outlist.get(ii)+"\n");
			}
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	/*	private static void extractURL(File xmldir, String outfile) {
		System.out.println("AAA");
		String[] files= xmldir.list();
		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("urls.txt"),"UTF-8"));
			for (int ii=0; ii<outlist.size() ; ii++){
				File fileCesDoc=new File(outlist.get(ii));
				String f1 = fileCesDoc.getName();
				int temp=0;
				int tokens=0;
				try {
					int eventType=0;
					String eaddress="", curElement="";
					XMLInputFactory2 xmlif = null;
					xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
					xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
					xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
					xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
					xmlif.configureForSpeed();
					XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmldir.getPath()+fs+f1),"UTF-8");
					while (xmlr.hasNext()) {
						eventType = xmlr.next();
						if (eventType == XMLEvent2.START_ELEMENT){
							curElement = xmlr.getLocalName().toString();
							if (curElement.equals(EADDRESS)) {
								eaddress=xmlr.getElementText();
							}else{
								if (curElement.equals("p")){
									int attrs=xmlr.getAttributeCount();
									int t=-1;
									for (int m=1;m<attrs;m++){
										if (xmlr.getAttributeValue(m).equals("boilerplate")
												|| xmlr.getAttributeValue(m).equals("ooi-lang")
												|| xmlr.getAttributeValue(m).equals("ooi-length")){
											t=0; 
											break;
										}
									}
									if (t<0) {
										String tempstr1 = xmlr.getElementText();
										StringTokenizer st = new StringTokenizer(tempstr1);	
										//double words_num=st.countTokens();
										temp = temp+st.countTokens();
									}
								}
							}
						}else
							curElement = "";
					}
					tokens=tokens+temp;
					out.write(f1+":\t"+eaddress+"\ttokens:\t"+temp+"\n");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	/*	private static void moveoutputfiles(File xmldir, String type,	File newxmldir, String outfile) {

		String[] files= xmldir.list();
		int pairscounter=0;
		System.out.println("checking "+type);
		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//File newxmldir=new File(xmldir.getParent()+fs+"selectedxml");
		for (int ii=0; ii<files.length ; ii++){
			File fileAlign=new File(files[ii]);
			String fileAlignName = fileAlign.getName();
			if (fileAlignName.length()<9 || !fileAlignName.contains(type)) continue;
			String f1 = fileAlignName.substring(0, fileAlignName.indexOf("_"))+".xml";
			String f2 = fileAlignName.substring(fileAlignName.indexOf("_")+1, fileAlignName.lastIndexOf("_"))+".xml";
			//System.out.println(f1.substring(0,f1.length()-4)+"_"+f2.substring(0,f2.length()-4)+type+".xml");
			if (!outlist.contains(f1.substring(0,f1.length()-4)+"_"+f2.substring(0,f2.length()-4)+type+".xml"))
				continue;
			pairscounter++;
			String f1h=f1.substring(0, f1.indexOf("."))+".html";
			String f2h=f2.substring(0, f2.indexOf("."))+".html";
			copyfile(xmldir.toString()+fs+f1,newxmldir.toString()+fs+f1);
			copyfile(xmldir.toString()+fs+f2,newxmldir.toString()+fs+f2);
			copyfile(xmldir.toString()+fs+f1h,newxmldir.toString()+fs+f1h);
			copyfile(xmldir.toString()+fs+f2h,newxmldir.toString()+fs+f2h);
			copyfile(xmldir.toString()+fs+fileAlign.getName(),newxmldir.toString()+fs+fileAlign.getName());
		}
		System.out.println(pairscounter+" files:"+ pairscounter*5);
	}*/

	/*	private static void copyfile(String f1, String f2) {
		InputStream inStream = null;
		OutputStream outStream = null;
		try{
			File afile =new File(f1);
			File bfile =new File(f2);
			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);
			byte[] buffer = new byte[1024];
			int length;
			//copy the file content in bytes 
			while ((length = inStream.read(buffer)) > 0)
				outStream.write(buffer, 0, length);
			inStream.close();
			outStream.close();
			System.out.println("File is copied successful!");
		}catch(IOException e){
			e.printStackTrace();
		}
	}*/

	/*	private static void check_domainess(String txtfile, String langs, double thr) {
		String str1=null, str2=null;

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(txtfile));
			String key1, key2;
			String lang1, lang2;
			int len1, len2;
			String[] languages=langs.split(";");
			int[] lengths=new int[2];
			lengths[0]=0; lengths[1]=0;
			double score1, score2;
			int counter=0;
			while ((str1 = in.readLine()) != null) {
				key1="";
				key2="";
				lang1="";
				lang2="";
				String[] temp1=str1.split("\t");
				str2 = in.readLine();
				String[] temp2=str2.split("\t");
				key1 = temp1[0]; 				key2 = temp2[0];
				score1=Double.parseDouble(temp1[1]);
				score2=Double.parseDouble(temp2[1]);
				if (score1>thr || score2>thr){
					counter++;
					lang1 = temp1[7]; 
					lang2 = temp2[7];
					len1=(int)Double.parseDouble(temp1[5]);
					len2=(int)Double.parseDouble(temp2[5]);
					if (lang1.equals(languages[0])){
						lengths[0]=lengths[0]+len1;
						lengths[1]=lengths[1]+len2;
					}
					else{
						lengths[0]=lengths[0]+len2;
						lengths[1]=lengths[1]+len1;
					}
				}else{
					System.out.println(key1+"_"+key2);
				}
			}
			in.close();
			System.out.println("THR:"+ thr +" pairs:"+ counter+ " in "+ languages[0]+": "+ lengths[0]+ " in "+languages[1]+":"+ lengths[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	/*	private static void countdomainess(File xmldir, String type, String langs, String otufile) {
		String[] files= xmldir.list();
		int pairscounter=0;
		String topicdef="C:\\PANACEA\\AUTOMOTIVE\\Automotive-seed-terms-de_en.txt";
		System.out.println("checking "+type);
		ArrayList<String[]> topicterms=tttTopic(topicdef, langs); 
		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(otufile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int ii=0; ii<files.length ; ii++){
			double rank=0;
			//File fileAlign=new File(files[ii]);
			//String fileAlignName = fileAlign.getName();
			//if (fileAlignName.length()<9 || !fileAlignName.contains(type)) continue;
			//pairscounter++;
			//String f1 = fileAlignName.substring(0, fileAlignName.indexOf("_"))+".xml";
			//String f2 = fileAlignName.substring(fileAlignName.indexOf("_")+1, fileAlignName.lastIndexOf("_"))+".xml";
			//if (!outlist.contains(f1.substring(0,f1.length()-4)+"_"+f2.substring(0,f2.length()-4)+type+".xml"))
			//	continue;
			String f1 =files[ii];
			String f2="";
			double temp=0;
			String lang="", pageurl="";
			try {
				int eventType=0;
				String curElement="";
				XMLInputFactory2 xmlif = null;
				xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
				xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
				xmlif.configureForSpeed();
				XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+f1),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE))
							lang=xmlr.getAttributeValue(0);
						else{
							if (curElement.equals(EADDRESS))
								pageurl=xmlr.getElementText();
							else{
								if (curElement.equals("p")){
									int attrs=xmlr.getAttributeCount();
									int t=-1;
									for (int m=1;m<attrs;m++){
										if (xmlr.getAttributeLocalName(m).toString().equals("topic")){
											String tempstr2 = xmlr.getAttributeValue(m);
											String terms[]=tempstr2.split(";");
											for (int ll=0;ll<terms.length;ll++){
												for (int mm=0;mm<topicterms.size();mm++){
													if (topicterms.get(mm)[1].equals(terms[ll]))
														rank = rank+Integer.parseInt(topicterms.get(mm)[0]);
												}
											}
										}
										if (xmlr.getAttributeValue(m).equals("boilerplate")
												|| xmlr.getAttributeValue(m).equals("ooi-lang")
												|| xmlr.getAttributeValue(m).equals("ooi-length")){
											t=0;
											break;
										}
									}
									if (t<0){
										String tempstr1 = xmlr.getElementText();
										StringTokenizer st = new StringTokenizer(tempstr1);	
										temp = temp+st.countTokens();	
									}
								}
							}
						}
					}else
						curElement = "";
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			//System.out.println(f1+" url:"+ pageurl);
			//System.out.println(f1+":\t"+rank/temp + "\trank:\t"+ rank+ "\ttoks:\t"+ temp+"\tlang:\t"+lang);
			if (f2.isEmpty()) {
				if ((rank/temp)<0.5)
					System.out.println(f1+":\t"+rank/temp + "\trank:\t"+ rank+ "\ttoks:\t"+ temp+"\tlang:\t"+lang);
				continue;
			}
			double temp1=0;
			double rank1=0;
			try {
				int eventType=0;
				String curElement="";
				XMLInputFactory2 xmlif = null;
				xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
				xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
				xmlif.configureForSpeed();
				XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+f2),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) 
							lang=xmlr.getAttributeValue(0);
						else{
							if (curElement.equals(EADDRESS))
								pageurl=xmlr.getElementText();
							else{
								if (curElement.equals("p")){
									int attrs=xmlr.getAttributeCount();
									int t=-1;
									for (int m=1;m<attrs;m++){
										if (xmlr.getAttributeLocalName(m).toString().equals("topic")){
											String tempstr2 = xmlr.getAttributeValue(m);
											String terms[]=tempstr2.split(";");
											for (int ll=0;ll<terms.length;ll++){
												for (int mm=0;mm<topicterms.size();mm++){
													if (topicterms.get(mm)[1].equals(terms[ll]))
														rank1 = rank1+Integer.parseInt(topicterms.get(mm)[0]);
												}
											}
											//String tempstr1 = xmlr.getElementText();
											//StringTokenizer st = new StringTokenizer(tempstr1);	
											//temp = temp+st.countTokens();	
											//break;
										}
										if (xmlr.getAttributeValue(m).equals("boilerplate")
												|| xmlr.getAttributeValue(m).equals("ooi-lang")
												|| xmlr.getAttributeValue(m).equals("ooi-length")){
											t=0;
											break;
										}
									}
									if (t<0){
										String tempstr1 = xmlr.getElementText();
										StringTokenizer st = new StringTokenizer(tempstr1);	
										temp1 = temp1+st.countTokens();	
									}
								}
							}
						}
					}else
						curElement = "";
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			//System.out.println(f2+" url:"+ pageurl);
			System.out.println(f2+":\t"+rank1/temp1 + "\trank:\t"+ rank1+ "\ttoks:\t"+ temp1+"\tlang:\t"+lang);
			if (rank/temp<0.5 && rank1/temp1<0.5){
				System.out.println("PROBLEM: "+ f1+"_"+f2);
			}
		}
	}*/


	/*	private static void counttoks(File xmldir, String type, String[] langs, String outfile) {

		ArrayList<String> outlist =new ArrayList<String>();
		BufferedReader in;
		String str="";
		try {
			in = new BufferedReader(new FileReader(outfile));
			while ((str = in.readLine()) != null) {
				outlist.add(str.substring(str.lastIndexOf("/")+1));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String[] files= xmldir.list();
		int pairscounter=0;
		int[] tokens = new int[2];

		for (int ii=0; ii<files.length ; ii++){
			File fileAlign=new File(files[ii]);
			String fileAlignName = fileAlign.getName();
			if (fileAlignName.length()<9 || !fileAlignName.contains(type)) continue;
			String f1 = fileAlignName.substring(0, fileAlignName.indexOf("_"))+".xml";
			String f2 = fileAlignName.substring(fileAlignName.indexOf("_")+1, fileAlignName.lastIndexOf("_"))+".xml";
			if (!outlist.contains(f1.substring(0,f1.length()-4)+"_"+f2.substring(0,f2.length()-4)+type+".xml"))
				continue;
			pairscounter++;
			int temp=0;
			try {
				int eventType=0;
				String lang="", curElement="";
				XMLInputFactory2 xmlif = null;
				xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
				xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
				xmlif.configureForSpeed();
				XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+f1),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) {
							lang=xmlr.getAttributeValue(0);
						}else{
							if (curElement.equals("p")){
								int attrs=xmlr.getAttributeCount();
								int t=-1;
								for (int m=1;m<attrs;m++){
									if (xmlr.getAttributeValue(m).equals("boilerplate")
											|| xmlr.getAttributeValue(m).equals("ooi-lang")
											|| xmlr.getAttributeValue(m).equals("ooi-length")){
										t=0; 
										break;
									}
								}
								if (t<0) {
									String tempstr1 = xmlr.getElementText();
									StringTokenizer st = new StringTokenizer(tempstr1);	
									//double words_num=st.countTokens();
									temp = temp+st.countTokens();
								}
							}
						}
					}else
						curElement = "";
				}
				if (lang.equals(langs[0]))
					tokens[0]=tokens[0]+temp;
				else
					tokens[1]=tokens[1]+temp;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			System.out.println(f1+":"+temp);
			temp=0;
			try {
				int eventType=0;
				String lang="", curElement="";
				XMLInputFactory2 xmlif = null;
				xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
				xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
				xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
				xmlif.configureForSpeed();
				XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+f2),"UTF-8");

				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) {
							lang=xmlr.getAttributeValue(0);
						}else{
							if (curElement.equals("p")){
								int attrs=xmlr.getAttributeCount();
								int t=-1;
								for (int m=1;m<attrs;m++){
									if (xmlr.getAttributeValue(m).equals("boilerplate")
											|| xmlr.getAttributeValue(m).equals("ooi-lang")
											|| xmlr.getAttributeValue(m).equals("ooi-length")){
										t=0; 
										break;
									}
								}
								if (t<0) {
									String tempstr1 = xmlr.getElementText();
									StringTokenizer st = new StringTokenizer(tempstr1);	
									temp = temp+st.countTokens();
								}
							}
						}
					}else
						curElement = "";
				}
				if (lang.equals(langs[0]))
					tokens[0]=tokens[0]+temp;
				else
					tokens[1]=tokens[1]+temp;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			System.out.println(f2+":"+temp);
		}
		System.out.println(type+" > PAIRS:\t"+pairscounter+"\tLANGS/TOKENS:\t"+ langs[0]+ "\twith\t"+ tokens[0]+ "\tAND\t" + langs[1]+ " with "+ tokens[1]);
	}*/

	public static HashMap<String, String[]> excludepairsIM(ArrayList<String[]> pairsIM, HashMap<String, String[]> props) {
		HashMap<String, String[]> props_short=new HashMap<String, String[]>();
		Set<String> files_im_keys=props.keySet();
		Iterator<String> files_im_it = files_im_keys.iterator();
		String key_im = "";
		while (files_im_it.hasNext()){
			key_im = files_im_it.next();
			props_short.put(key_im, props.get(key_im));
		}
		for (int ii=0;ii<pairsIM.size();ii++){
			String[] temp = pairsIM.get(ii);
			props_short.remove(temp[0]);
			props_short.remove(temp[1]);
		}
		return props_short;
	}



	public static ArrayList<String[]> findpairsXML_SVM_NEW(File xmldir,HashMap<String, String[]> props, double[][] sv, double[][] w,
			double[][] b) {
		ArrayList<String[]> pairs = new ArrayList<String[]>();

		Set<String> files_keys=props.keySet();
		Iterator<String> files_it = files_keys.iterator();
		String key1, key2;
		while (files_it.hasNext()){
			key1 = files_it.next();
			if (Bitexts.readlist(xmldir.getPath()+ fs+key1+".xml.txt")==null){
				//AAA[ii][0]="";
				String[] fileprops = props.get(key1);
				fileprops[0]="";
				props.put(key1, fileprops);
			}
		}
		files_it = files_keys.iterator();
		ArrayList<String> already_tested=new ArrayList<String>();
		while (files_it.hasNext()){
			key1 = files_it.next();
			//System.out.println(key1);
			String[] fileprops = props.get(key1);
			if (fileprops[0].isEmpty()) continue;
			String sf = key1;
			int sf_level = Integer.parseInt(fileprops[0]);
			int[] sl =readlist(xmldir.getPath()+ fs+sf+".xml.txt");
			int sflength =0;
			for (int mm=0;mm<sl.length;mm++){
				if (sl[mm]>0)
					sflength = sflength+sl[mm];
			}
			double sl_length = Double.parseDouble(Integer.toString(sl.length));
			double sl_par = Double.parseDouble(fileprops[2]);
			double dist;
			double res=0.0;
			already_tested.add(key1);
			Iterator<String> files_it2 = files_keys.iterator();

			while (files_it2.hasNext()){
				key2 = files_it2.next();
				if (already_tested.contains(key2)) continue;
				String[] fileprops2 = props.get(key2);
				if (fileprops2[0].isEmpty()) continue;
				String tf = key2;
				double tl_par = Double.parseDouble(fileprops2[2]);
				if (!fileprops[1].equals(fileprops2[1]) & Math.abs(sf_level-Integer.parseInt(fileprops2[0]))<level_thres 
						& (Math.abs(sl_par-tl_par)/Math.max(sl_par, tl_par))<pars_thres) {

					int[] tl =readlist(xmldir.getPath()+ fs+tf+".xml.txt");
					double tl_length = Double.parseDouble(Integer.toString(tl.length));
					if (Math.abs(sl_length-tl_length)/Math.min(sl_length,tl_length)<=length_thres
							|| (Math.abs(sl_length-tl_length)<10)){
						dist= Double.parseDouble(Integer.toString(editDist(sl,tl)));
						double f1=0.0, f2=0.0, f3=0.0;
						if (tl_length>=sl_length){
							f1 = sl_length/tl_length;
							f3=dist/tl_length;
							//if (f1>0.9)
							//	f3=0.6*dist/tl_length;
						}
						else{
							f1 = tl_length/sl_length;
							f3=dist/tl_length;
							//if (f1>0.9)
							//	f3=0.6*dist/tl_length;
						}
						if (tl_par>=sl_par){
							f2=sl_par/tl_par;
						}else{
							f2=tl_par/sl_par;
						}
						if (f3>=0.38 || f1<=0.7 || f2<=0.7)
							res=-1;
						else
							res=SVM_test(f1,f2,f3,sv,w,b,19.0);
						if (res>0){
							res=Math.abs(res);
							double inv_res=1/res;
							int tflength =0;
							for (int mm=0;mm<tl.length;mm++){
								if (tl[mm]>0)
									tflength = tflength+tl[mm];
							}
							String pairlength = Integer.toString(Integer.parseInt(props.get(sf)[4])+
									Integer.parseInt(props.get(tf)[4]));
							pairs.add(new String[] {sf,tf,fileprops[1],fileprops2[1],Double.toString(inv_res),pairlength});
						}
					}
				}
			}
		}
		return pairs;
	}

	public static ArrayList<String[]> findpairsIM(HashMap<String, String[]> imagesInHTML,HashMap<String, String[]> props) {
		ArrayList<String[]> pairsIM=new ArrayList<String[]>();

		Set<String> files_im_keys=imagesInHTML.keySet();
		Iterator<String> files_im_it = files_im_keys.iterator();
		String key_im, key;
		String temp_pair="";
		//ArrayList<String> paired=new ArrayList<String>();
		while (files_im_it.hasNext()){
			key_im = files_im_it.next();
			//if (paired.contains(key_im)) continue;
			if (props.get(key_im)==null) continue;
			String lang1=props.get(key_im)[1];
			Set<String> mySet1 = new HashSet<String>();
			if (imagesInHTML.get(key_im)==null)
				continue;
			Collections.addAll(mySet1, imagesInHTML.get(key_im));
			if (mySet1.isEmpty()) continue;
			Set<String> all_files_keys=props.keySet();
			Iterator<String> all_files_it = all_files_keys.iterator();
			//ArrayList<String> temp_paired=new ArrayList<String>();
			//ArrayList<Double> temp_paired_scores=new ArrayList<Double>();
			temp_pair="";
			double temp_pair_score=0.0;
			String temp_lang=null;
			while (all_files_it.hasNext()){
				key = all_files_it.next();
				//if (paired.contains(key)) continue;
				if (props.get(key)==null) continue;
				String lang2=props.get(key)[1];
				if (!lang1.equals(lang2)){
					Set<String> mySet2 = new HashSet<String>();
					if (imagesInHTML.get(key)==null)
						continue;
					Collections.addAll(mySet2, imagesInHTML.get(key));
					if (mySet2.isEmpty()) continue;
					Set intersection = new HashSet(mySet1);
					intersection.retainAll(mySet2);
					if (Math.abs(mySet2.size()-mySet1.size())<im_dif_thr){
						double t1=Double.parseDouble(Integer.toString(intersection.size()));
						double t2=Double.parseDouble(Integer.toString((mySet1.size()+mySet2.size()-intersection.size())));
						double jac=t1/t2;
						double p1=Double.parseDouble(props.get(key_im)[2]);
						double p2=Double.parseDouble(props.get(key)[2]);
						double l1=Double.parseDouble(props.get(key_im)[0]);
						double l2=Double.parseDouble(props.get(key)[0]);
						double tok1=Double.parseDouble(props.get(key_im)[3]);
						double tok2=Double.parseDouble(props.get(key)[3]);
						double dist=0.0;
						double disttok=0.0;
						if (p1>p2) dist=p2/p1; else dist=p1/p2;
						if (tok1>tok2) disttok=tok2/tok1; else disttok=tok1/tok2;
						if (jac>=jac_thr && dist>=0.6 && disttok > 0.3 && Math.abs(l1-l2)<2.0){ //
							//if (jac>=jac_thr && dist>=0.6 && disttok > 0.3){ //
							//System.out.println(key_im +"_im:"+mySet1.size()+"-----"+key+"_im:"+mySet2.size()+"_"+jac);
							//if (jac*dist>temp_pair_score){
							if (jac>temp_pair_score){
								temp_pair=key;
								temp_pair_score=jac*dist;
								//System.out.println(key_im+"->"+key+" with "+jac);
							}
							temp_lang=lang2;
						}
					}
				}
			}
			//if (temp_paired.size()==1){
			if (!temp_pair.isEmpty()){
				//	paired.add(key_im);
				//	paired.add(temp_paired.get(0));
				pairsIM.add(new String[] {key_im,temp_pair,lang1, temp_lang});
				//System.out.println(key_im+"_"+temp_pair);
			}
		}
		ArrayList<String[]> result=new ArrayList<String[]>();
		for (int ii=0;ii<pairsIM.size();ii++){
			String temp1=pairsIM.get(ii)[0];
			String temp2=pairsIM.get(ii)[1];
			for (int jj=ii+1;jj<pairsIM.size();jj++){
				if (pairsIM.get(jj)[1].equals(temp1) && pairsIM.get(jj)[0].equals(temp2)){
					result.add(new String[] {temp1,temp2,pairsIM.get(ii)[2], pairsIM.get(ii)[3],"im",
							Integer.toString(Integer.parseInt(props.get(temp1)[4])+Integer.parseInt(props.get(temp2)[4]))});
					//System.out.println(temp1+"_"+temp2);
				}
			}
		}
		return result;
	}


	public static HashMap<String, String[]> representXML_NEW(File xmldir) throws FileNotFoundException, XMLStreamException {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		HashMap<String, String[]> res=new HashMap<String, String[]>();
		String url="", lang="", curElement="";
		for (int ii=0; ii<files.length ; ii++){
			String[] temp_res = new String[5];
			String tempstr="";
			String newtempstr="";
			int pcounter=0;
			OutputStreamWriter xmlFileListWrt = null;
			try {
				xmlFileListWrt = new OutputStreamWriter(new FileOutputStream
						(xmldir.getPath()+fs+files[ii]+".txt"),"UTF-8");
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
						createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+files[ii]),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) {
							lang=xmlr.getAttributeValue(0);
							temp_res[1]=lang;
						}else{
							if (curElement.equals(URL_ELE)){
								if (xmlr.getAttributeCount()<1){
									url=xmlr.getElementText();
									int k=0, level=0, ind=0;
									while (k<url.length()){
										ind=url.indexOf("/", k);
										if (ind>0){
											k = ind+1;
											level=level+1;
										}else
											k=url.length();
									}
									if (url.endsWith("/"))
										level=level-1;
									level=level-2; 
									temp_res[0]=Integer.toString(level);
								}
							}else{
								if (curElement.equals("p")){
									pcounter=pcounter+1;
									int attrs=xmlr.getAttributeCount();

									int t=-1, t1=0, t10=0;
									for (int m=1;m<attrs;m++){
										//System.out.println(xmlr.getAttributeValue(m));
										if (xmlr.getAttributeValue(m).equals("boilerplate")){
											t=0; 
											break;
										}
										if (xmlr.getAttributeValue(m).equals("title"))
											t=-2; 
										if (xmlr.getAttributeValue(m).equals("heading"))
											t=-3; 
										if (xmlr.getAttributeValue(m).equals("listitem"))
											t=-4;
										if (xmlr.getAttributeLocalName(m).equals("topic")){
											//topic=true;
											t1=-5;
										}
										if (m==1 && !xmlr.getAttributeValue(m).equals("ooi-length") && !xmlr.getAttributeValue(m).equals("ooi-lang")){
											t10=1; 
										}
									}
									if (t<0){
										if (t==-2)
											xmlFileListWrt.write("-2"+"\n");
										if (t==-3)
											xmlFileListWrt.write("-3"+"\n");
										if (t==-4)
											xmlFileListWrt.write("-4"+"\n");
									}
									if (t1<0)
										xmlFileListWrt.write("-5"+"\n");	
									String tempstr1 = xmlr.getElementText();
									if (t<0 | t1<0) {
										//int temp = xmlr.getElementText().length();
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
				temp_res[2]=Integer.toString(pcounter);
				StringTokenizer st = new StringTokenizer(tempstr);
				temp_res[3]=Integer.toString(st.countTokens());
				StringTokenizer st1 = new StringTokenizer(newtempstr);
				temp_res[4]=Integer.toString(st1.countTokens());
				if (res.containsKey(files[ii].substring(0, files[ii].indexOf("."))))
					System.out.println("OOPS");
				res.put(files[ii].substring(0, files[ii].indexOf(".")), temp_res);

				//System.out.println(files[ii].substring(0, files[ii].indexOf("."))+":"+temp_res[0]+">"+temp_res[1]+">"+temp_res[2]+">"+temp_res[3]);

				xmlFileListWrt.close();
				xmlr.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Problem in respesenting the "+ files[ii]+" cesDoc file.");
			}
		}
		return res;
	}


	public static String[][] representXML(File xmldir) throws FileNotFoundException, XMLStreamException {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		String[][] res = new String[files.length][3];
		String url="", lang="", curElement="";
		for (int ii=0; ii<files.length ; ii++){
			//System.out.println(ii);
			int pcounter=0;
			OutputStreamWriter xmlFileListWrt = null;
			//boolean topic=false;
			try {
				xmlFileListWrt = new OutputStreamWriter(new FileOutputStream
						(xmldir.getPath()+fs+files[ii]+".txt"),"UTF-8");
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
						createXMLStreamReader(new FileInputStream(xmldir.getPath()+"/"+files[ii]),"UTF-8");
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						if (curElement.equals(LANGUAGE_ELE)) {
							lang=xmlr.getAttributeValue(0);
							res[ii][1]=lang;
						}else{
							if (curElement.equals(URL_ELE)){
								if (xmlr.getAttributeCount()<1){
									url=xmlr.getElementText();
									int k=0, level=0, ind=0;
									while (k<url.length()){
										ind=url.indexOf("/", k);
										if (ind>0){
											k = ind+1;
											level=level+1;
										}else
											k=url.length();
									}
									if (url.endsWith("/"))
										level=level-1;
									level=level-2; 
									res[ii][0]=Integer.toString(level);
								}
							}else{
								if (curElement.equals("p")){
									pcounter=pcounter+1;
									int attrs=xmlr.getAttributeCount();
									int t=-1, t1=0;
									for (int m=1;m<attrs;m++){
										if (xmlr.getAttributeValue(m).equals("boilerplate")){
											t=0; 
											break;
										}
										if (xmlr.getAttributeValue(m).equals("title"))
											t=-2; 
										if (xmlr.getAttributeValue(m).equals("heading"))
											t=-3; 
										if (xmlr.getAttributeValue(m).equals("listitem"))
											t=-4;
										if (xmlr.getAttributeLocalName(m).equals("topic")){
											//topic=true;
											t1=-5;
										}
									}
									if (t<0){
										if (t==-2)
											xmlFileListWrt.write("-2"+"\n");
										if (t==-3)
											xmlFileListWrt.write("-3"+"\n");
										if (t==-4)
											xmlFileListWrt.write("-4"+"\n");
									}
									if (t1<0)
										xmlFileListWrt.write("-5"+"\n");	
									if (t<0 | t1<0) {
										int temp = xmlr.getElementText().length();
										xmlFileListWrt.write(Integer.toString(temp)+"\n");
									}
								}
							}
						}
					}else{
						curElement = "";
					}
				}
				res[ii][2]=Integer.toString(pcounter);
				xmlFileListWrt.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();

			}catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Problem in respesenting the "+ files[ii]+" cesDoc file.");
			}
		}
		return res;
	}


	public static double[][] readRes(String filename) throws IOException {
		ArrayList<double[]> tempparam=new ArrayList<double[]>();
		URL svURL = ReadResources.class.getClassLoader().getResource(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			String[] temp=inputLine.split("\t");
			double[] tempd=new double[temp.length];
			for (int j=0;j<temp.length;j++){
				tempd[j]=Double.parseDouble(temp[j]);  
			}
			if (temp.length>1){
				tempparam.add(new double[] {tempd[0], tempd[1],tempd[2]});
			}else{
				tempparam.add(new double[] {tempd[0]});
			}
		}
		in.close();
		int x= tempparam.size();
		int y = tempparam.get(0).length;
		double[][] param=new double[x][y];
		for (int j=0;j<x;j++){
			for (int k=0;k<y;k++)
				param[j][k]=tempparam.get(j)[k];
		}
		return param;
	}


	public static ArrayList<String[]> findpairsXML_SVM(File xmldir, String[][] AAA, double[][] sv, double[][] w, double[][] b) {
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		for (int ii=0; ii<files.length ; ii++){
			if (Bitexts.readlist(xmldir.getPath()+ "/"+files[ii]+".txt")==null){
				AAA[ii][0]="";
			}
		}
		for (int ii=0; ii<files.length ; ii++){
			if (AAA[ii][0].isEmpty())
				continue;
			String sf = files[ii].substring(0, files[ii].indexOf("."));
			int sf_level = Integer.parseInt(AAA[ii][0]);
			int[] sl =readlist(xmldir.getPath()+ fs+sf+".xml.txt");
			int sflength =0;
			for (int mm=0;mm<sl.length;mm++){
				if (sl[mm]>0)
					sflength = sflength+sl[mm];
			}
			double sl_length = Double.parseDouble(Integer.toString(sl.length));
			double sl_par = Double.parseDouble(AAA[ii][2]);
			double dist;
			double res=0.0;
			for (int jj=ii+1; jj<files.length ; jj++){
				if (AAA[jj][0].isEmpty())
					continue;
				String tf = files[jj].substring(0, files[jj].indexOf("."));
				double tl_par = Double.parseDouble(AAA[jj][2]);
				if (!AAA[ii][1].equals(AAA[jj][1]) & Math.abs(sf_level-Integer.parseInt(AAA[jj][0]))<level_thres 
						& (Math.abs(sl_par-tl_par)/Math.max(sl_par, tl_par))<pars_thres) {
					int[] tl =readlist(xmldir.getPath()+ fs+tf+".xml.txt");
					double tl_length = Double.parseDouble(Integer.toString(tl.length));
					if (Math.abs(sl_length-tl_length)/Math.min(sl_length,tl_length)<=length_thres
							|| (Math.abs(sl_length-tl_length)<10)){
						dist= Double.parseDouble(Integer.toString(editDist(sl,tl)));
						double f1=0.0, f2=0.0, f3=0.0;
						if (tl_length>=sl_length){
							f1 = sl_length/tl_length;
							f3=dist/tl_length;
							//if (sl_length<15)
							//	f3=0.8*dist/tl_length;
						}
						else{
							f1 = tl_length/sl_length;
							f3=dist/tl_length;
							//if (sl_length<15)
							//	f3=0.8*dist/tl_length;
						}
						if (tl_par>=sl_par){
							f2=sl_par/tl_par;
						}else{
							f2=tl_par/sl_par;
						}
						if (f3>=0.3 || f1<=0.7 || f2<=0.7)
							res=-1;
						else
							res=SVM_test(f1,f2,f3,sv,w,b,19.0);
						//res=SVM_test(f1,f2,f3,sv,w,b,19.0);
						if (res>0){
							//filepair=sf+"_"+tf;
							double inv_res=1/res;
							//langpair = AAA[ii][1]+"_"+AAA[jj][1];
							//pairs.add(new String[] {filepair,langpair,Double.toString(inv_res)});
							//pairs.add(new String[] {filepair,langpair,Double.toString(res)});
							/*File sftemp =new File(xmldir+fs+files[ii]);
							File tftemp =new File(xmldir+fs+files[jj]);
							String pairlength = Double.toString(sftemp.length()+tftemp.length());*/
							int tflength =0;
							for (int mm=0;mm<tl.length;mm++){
								if (tl[mm]>0)
									tflength = tflength+tl[mm];
							}
							String pairlength = Integer.toString(tflength+sflength);
							pairs.add(new String[] {sf,tf,AAA[ii][1],AAA[jj][1],Double.toString(inv_res),pairlength});
						}
					}
				}
			}
		}
		return pairs;
	}


	private static double SVM_test(double f1, double f2, double f3,
			double[][] sv, double[][] w, double[][] b, double degree) {
		Double res=0.0;
		double temp2, temp1;
		double par1, par2, par3, par123;
		for (int j=0;j<sv.length;j++){
			par1 = f1*sv[j][0];
			par2 = f2*sv[j][1];
			par3 = f3*sv[j][2];
			par123 = par1 + par2 + par3+1;
			temp1=Math.pow(par123,degree);
			temp2=temp1*w[j][0];
			res=res+temp2; 
		}
		res=res+b[0][0];
		return res;
	}


	public static ArrayList<String[]> findBestPairs_SVM(ArrayList<String[]> pairs) {
		ArrayList<String[]> bitexts=new ArrayList<String[]>();
		Double[] new_temp1 = new Double[pairs.size()];
		String[][] pairlist = new String[pairs.size()][5];
		Double[] editdist =  new Double[pairs.size()];
		for (int i = 0; i < new_temp1.length; i++){
			new_temp1[i] =Double.parseDouble(pairs.get(i)[2]);
			editdist[i] =Double.parseDouble(pairs.get(i)[2]);
			int ind = pairs.get(i)[0].indexOf("_");
			pairlist[i][0]=pairs.get(i)[0].substring(0, ind);
			pairlist[i][1]=pairs.get(i)[0].substring(ind+1);
			int ind1 = pairs.get(i)[1].indexOf("_");
			pairlist[i][2]=pairs.get(i)[1].substring(0, ind1);
			pairlist[i][3]=pairs.get(i)[1].substring(ind1+1);
			pairlist[i][4]=pairs.get(i)[3];

		}
		Arrays.sort(new_temp1);
		int kk = 0;
		for (int i = 0; i < new_temp1.length; i++){
			if (i > 0 && new_temp1[i].equals(new_temp1[i -1]))
				continue;
			new_temp1[kk++] = new_temp1[i];
		}
		Double[] new_temp = new Double[kk];
		System.arraycopy(new_temp1, 0, new_temp, 0, kk);
		for (int i = 0; i < new_temp.length; i++){
			//System.out.print(new_temp[i]+">");
			for (int j = 0; j < pairlist.length; j++){
				if (!pairlist[j][0].isEmpty() & !pairlist[j][1].isEmpty()){
					if (new_temp[i].equals(editdist[j])){
						String f1 = pairlist[j][0];
						String f2 = pairlist[j][1];
						String l1 = pairlist[j][2];
						String l2 = pairlist[j][3];
						//System.out.println("FOUND: "+f1 +" and "+ f2+"___"+pairlist[j][4]);
						bitexts.add(new String[] {f1, f2,l1,l2,"",pairlist[j][4]});
						for(int k=0; k<pairlist.length;k++){
							if (pairlist[k][0].equals(f1) | pairlist[k][0].equals(f2))
								pairlist[k][0]="";		
							if (pairlist[k][1].equals(f1) | pairlist[k][1].equals(f2))
								pairlist[k][1]="";
						}
					}
				}
			}
			//System.out.println();	
		}
		//System.out.println("END");
		return bitexts;
	}



	public static ArrayList<String[]> findBestPairs_SVM_NEW(ArrayList<String[]> pairs) {
		ArrayList<String[]> bitexts=new ArrayList<String[]>();
		int[][] counts=new int[pairs.size()][2]; 
		for (int ii=0;ii<pairs.size();ii++){
			for (int jj=0;jj<pairs.size();jj++){
				if (pairs.get(ii)[0].equals(pairs.get(jj)[0]) | pairs.get(ii)[0].equals(pairs.get(jj)[1]) ){
					counts[ii][0] = counts[ii][0]+1;
				}
				if (pairs.get(ii)[1].equals(pairs.get(jj)[0]) | pairs.get(ii)[1].equals(pairs.get(jj)[1]) ){
					counts[ii][1] = counts[ii][1]+1;
				}
			}
		}
		int[] flags=new int[counts.length];
		//int limit = maxArray(counts)/2;
		//String temp="";
		double dist, dist1;
		//for (int ii=0;ii<limit;ii++){
		//for (int ii=0;ii<2;ii++){
		for (int jj=0;jj<pairs.size();jj++){
			if (counts[jj][0]==1 & counts[jj][1]==1 & flags[jj]==0){
				//System.out.println(pairs.get(jj)[0]+"_"+ pairs.get(jj)[1]+"___"+pairs.get(jj)[5]);
				bitexts.add(new String[] {pairs.get(jj)[0], pairs.get(jj)[1],pairs.get(jj)[2],pairs.get(jj)[3],"high",pairs.get(jj)[5]});
				flags[jj]=1;
				//counts[jj][0]=counts[jj][0]-1;
				//counts[jj][1]=counts[jj][1]-1;
			}
		}
		//System.out.println("END OF pairs of type1");
		for (int jj=0;jj<pairs.size();jj++){
			if (counts[jj][0]==1 & counts[jj][1]==2 & flags[jj]==0){
				//temp =pairs.get(jj)[1]; 
				dist = Double.parseDouble(pairs.get(jj)[4]);
				dist1=0.0;
				int ind=-1;
				for (int kk=0;kk<pairs.size();kk++){
					if (pairs.get(kk)[1].equals(pairs.get(jj)[1]) & kk!=jj)
						ind=1;
					if (pairs.get(kk)[0].equals(pairs.get(jj)[1]) & kk!=jj)
						ind=0;
					if (ind>-1){
						dist1 = Double.parseDouble(pairs.get(kk)[4]);
						if (dist<dist1){
							//System.out.println(pairs.get(jj)[0]+"_"+ pairs.get(jj)[1]+"___"+pairs.get(jj)[5]);
							bitexts.add(new String[] {pairs.get(jj)[0], pairs.get(jj)[1],pairs.get(jj)[2],pairs.get(jj)[3],"medium",pairs.get(jj)[5]});
							//counts[jj][0]=0;
							//counts[jj][1]=0;
							counts[kk][ind]=0;
							flags[jj]=2;
							flags[kk]=-1;
						}else{
							flags[jj]=-1;
							counts[kk][ind]=counts[kk][ind]-1;
						}
						break;
					}
				}
			}
		}
		for (int jj=0;jj<pairs.size();jj++){
			if (counts[jj][0]==2 & counts[jj][1]==1 & flags[jj]==0){
				//temp =pairs.get(jj)[0]; 
				dist = Double.parseDouble(pairs.get(jj)[4]);
				dist1=0.0;
				for (int kk=0;kk<pairs.size();kk++){
					if (pairs.get(kk)[0].equals(pairs.get(jj)[0]) & kk!=jj){
						dist1 = Double.parseDouble(pairs.get(kk)[4]);
						if (dist<dist1){
							//System.out.println(pairs.get(jj)[0]+"_"+ pairs.get(jj)[1]+"___"+pairs.get(jj)[5]);
							bitexts.add(new String[] {pairs.get(jj)[0], pairs.get(jj)[1],pairs.get(jj)[2],pairs.get(jj)[3],"medium",pairs.get(jj)[5]});
							//counts[jj][0]=0;
							//counts[jj][1]=0;
							counts[kk][0]=0;
							flags[jj]=2;
							flags[kk]=-1;
						}else{
							flags[jj]=-1;
							counts[kk][0]=counts[kk][0]-1;
						}
						break;
					}
				}
			}
		}
		//}
		//System.out.println("END OF pairs of type 2");
		ArrayList<String[]> new_pairs=new ArrayList<String[]>();
		for (int ii=0;ii<pairs.size();ii++){
			if (flags[ii]==0){
				new_pairs.add(new String[] {pairs.get(ii)[0]+"_"+pairs.get(ii)[1],pairs.get(ii)[2]+"_"+pairs.get(ii)[3],pairs.get(ii)[4],pairs.get(ii)[5]});
			}
		}
		ArrayList<String[]> new_bitexts=new ArrayList<String[]>();
		new_bitexts=findBestPairs_SVM(new_pairs);
		for (int ii=0;ii<new_bitexts.size();ii++){
			bitexts.add(new String[] {new_bitexts.get(ii)[0], 
					new_bitexts.get(ii)[1],new_bitexts.get(ii)[2],new_bitexts.get(ii)[3],"low",new_bitexts.get(ii)[5]});
		}
		return bitexts;
	}


	public static void writeXMLs(String outdir,ArrayList<String[]> bitexts, boolean cesAlign, boolean oxslt) throws XMLStreamException, IOException{

		if (oxslt) {
			try {
				xslTransformer = new XSLTransformer(cesAlingURL);
				xslTransformer.setBaseDir(outdir+fs+"xml");
			} catch (TransformerConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		File inFile = null, outFile = null;
		for (int ii=0;ii<bitexts.size();ii++){
			String f1=bitexts.get(ii)[0];
			String f2=bitexts.get(ii)[1];
			String l1=bitexts.get(ii)[2];
			String l2=bitexts.get(ii)[3];
			String confid=bitexts.get(ii)[4];
			//Path outdir1 = new Path(outdir);
			String curXMLName= outdir+fs+"xml"+fs+f1+"_"+f2+"_"+confid.substring(0, 1)+".xml";
			
			writeCesAling(curXMLName,f1+".xml",f2+".xml",l1,l2,confid,cesAlign);
			if (oxslt) {
				try {
					inFile = new File(curXMLName);
					outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".xml.html");			
					xslTransformer.transform(inFile, outFile);
										
					//f1=f1+".xml.html";
					//f2=f2+".xml.html";
					//String curXMLName_temp= outdir+fs+"xml"+fs+f1+"_"+f2+"_"+confid.substring(0, 1)+"temp.xml";
					//writeCesAling(curXMLName_temp,f1+".xml.html",f2+".xml.html",l1,l2,confid,cesAlign);
					//inFile = new File(curXMLName_temp);
					//outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".xml.html");			
					//xslTransformer.transform(inFile, outFile);
					//File destFile=new File(outFile.getAbsolutePath().replace("temp.xml.html", ".xml.html"));
					//FileUtils.copyFile(outFile, destFile);
					//DedupMD5.delete(outFile.getAbsolutePath());
					//String curXMLName_temp= outdir+fs+"xml"+fs+f1+"_"+f2+"_"+confid.substring(0, 1)+".xml.html";
					String cesAlignText = readFileAsString(outFile.getAbsolutePath());
					cesAlignText = cesAlignText.replace(f1+".xml", f1+".xml.html");
					cesAlignText = cesAlignText.replace(f2+".xml", f2+".xml.html");
					OutputStreamWriter tmpwrt;
					try {
						tmpwrt = new OutputStreamWriter(new FileOutputStream(outFile.getAbsolutePath()),"UTF-8");
						tmpwrt.write(cesAlignText);
						tmpwrt.close();
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (TransformerException e) {
					e.printStackTrace();
					LOGGER.warn("Could not transform " + inFile.getAbsolutePath() + " to " + outFile.getAbsolutePath());
				}
			}
		}
	}


	private static void writeCesAling(String curXMLName, String f1, String f2,
			String l1, String l2, String confid, boolean cesAlign) {

		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		OutputStreamWriter wrt;
		try {
			wrt = new OutputStreamWriter(new FileOutputStream(curXMLName),"UTF-8");
			XMLStreamWriter2 xtw = (XMLStreamWriter2) xof.createXMLStreamWriter(wrt);
			//System.err.println("Attempting to write "+curXMLName);
			//String v1 =outdir.replace(VAR_RES_CACHE,HTTP_PATH);
			//f1 = f1.replace(VAR_RES_CACHE,HTTP_PATH);
			//f2 = f2.replace(VAR_RES_CACHE,HTTP_PATH);
			//vpapa added this just for development on windows
			//f1 = f1.substring(f1.indexOf("http:"));
			//f2 = f2.substring(f2.indexOf("http:"));
			xtw.writeStartDocument();
			//if (cesAlign){
			//	xtw.writeProcessingInstruction("xml-stylesheet href='http://nlp.ilsp.gr/panacea/xces-xslt/cesAlign.xsl' type='text/xsl'");
			//xtw.writeProcessingInstruction("xml-stylesheet href='http://nlp.ilsp.gr/panacea/xces-xslt/cesAlign.xsl' type='text/xsl'");
			//xtw.writeProcessingInstruction("xml-stylesheet", "href='http://nlp.ilsp.gr/panacea/xces-xslt/cesAlign.xsl' type='text/xsl'");
			//}
			xtw.writeStartElement("cesAlign");
			xtw.writeAttribute("version", "1.0");
			xtw.writeAttribute("xmlns:xlink", cesNameSpace );
			xtw.writeAttribute("xmlns", cesNameSpace1 );
			xtw.writeAttribute("xmlns:xsi", cesNameSpace2 );
			//xtw.writeAttribute("xmlns", "http://www.xces.org/schema/2003");
			createHeader(xtw, f1, f2, l1, l2,confid,cesAlign);
			xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
			wrt.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int editDist(int[] sl, int[] tl) {
		int n=sl.length, m=tl.length, cost, sl_i, tl_j; 
		int d[][] = new int[n+1][m+1]; // matrix
		for (int i=0; i<=n; i++) {d[i][0]=i;}
		for (int j=0; j<=m; j++) {d[0][j]=j;}
		for (int i=1; i<= n; i++) {
			sl_i = sl[i-1];
			//for (int j=Math.max(0, i-2); j<=Math.min(i+2, m); j++) {
			for (int j=1; j<=m; j++) {
				tl_j = tl[j-1];
				if (sl_i<0 & tl_j<0) {
					if (sl_i==tl_j)
						cost = 0;
					else
						cost = 1;
				}
				else{
					if (sl_i*tl_j<0)
						cost = 1000000000;
					else{
						if (Double.parseDouble(Integer.toString(Math.abs(sl_i-tl_j)))/
								Double.parseDouble(Integer.toString(Math.max(sl_i,tl_j)))>=text_thres)
							cost=1;
						else
							cost=0;
					}
				}
				//d[i][j] = min3(d[Math.max(0, i-1)][j], d[i][j], d[i][Math.max(0,j-1)])+ cost;
				//d[i][j] = min3(d[i-1][j], d[i-1][j-1], d[i][j-1])+cost;
				d[i][j] = min3(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1]+cost);
				//System.out.println("i="+i+"	j="+j+"	si="+sl_i+"	tj="+tl_j+"	dij="+d[i][j]);
			}
		}
		//int dist=1000000000;
		//for (int i=1; i<= m; i++){
		//	dist=Math.min(dist, d[n][i]);
		//}
		return d[n][m];
		//return dist;
	}


	private static int min3(int i, int j, int k) {
		int l = Math.min(i, j);
		int res = Math.min(l, k);
		return res;
	}


	private static int[] readlist(String fn) {
		File f=new File(fn);
		String str=null;
		ArrayList<String> patterns1 = new ArrayList<String>();
		int kk=0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			while ((str = in.readLine()) != null) {
				patterns1.add(str);
				kk++;
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Problem in reading file: " + f.getName());
		}
		String[] patterns = new String[kk];
		System.arraycopy(patterns1.toArray(), 0, patterns, 0, kk);
		int[] patt_int = new int[kk];
		//for (int i=0; i<kk; i++)
		//	patt_int[i]=Integer.parseInt(patterns[i]);
		//return patt_int;

		int num_of_text_par=0;
		for (int i=0; i<kk; i++){
			patt_int[i]=Integer.parseInt(patterns[i]);
			if (Integer.parseInt(patterns[i])>0)
				num_of_text_par++;
		}
		if (num_of_text_par>minnumofpars)
			return patt_int;
		else
			return null;


	}


	private static void createHeader(XMLStreamWriter2 xtw, String f1, 
			String f2,String l1, String l2, String confid, boolean cesAlign) throws XMLStreamException {
		xtw.writeStartElement("cesHeader");
		xtw.writeAttribute("version", cesDocVersion);
		xtw.writeStartElement("profileDesc");
		xtw.writeStartElement("translations");
		//xtw.writeAttribute("confidence",confid );
		xtw.writeStartElement("translation");
		xtw.writeAttribute("trans.loc", f1);
		xtw.writeAttribute("xml:lang", l1);
		xtw.writeAttribute("wsd", "UTF-8");
		xtw.writeAttribute("n", "1");
		xtw.writeEndElement();//translation
		xtw.writeStartElement("translation");
		xtw.writeAttribute("trans.loc", f2);
		xtw.writeAttribute("xml:lang", l2);
		xtw.writeAttribute("wsd", "UTF-8");
		xtw.writeAttribute("n", "2");
		xtw.writeEndElement(); //translations
		xtw.writeEndElement(); //profileDesc
		xtw.writeEndElement(); //cesHeader
	}

	/*public static void writeOutList(String outputDirName, String outputFile,
			String outputHFile) {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.contains("_") & !arg1.contains("xml.html"));
			}
		};
		File xmldir=new File(outputDirName+fs+"xml");
		String[] files= xmldir.list(filter);
		//String temp="";
		try {
			//FileWriter outFile = new FileWriter(outputFile);
			//PrintWriter out = new PrintWriter(outFile);
			Path outputDirName1=new Path(outputDirName);
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
			for (int ii=0; ii<files.length ; ii++){
				////temp=xmldir.getAbsolutePath().replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "");
				File ttt = new File(outputDirName1.toString()+fs+"xml"+fs+files[ii]);
				out.write(ttt.getAbsolutePath()+"\n");
			}
			out.close();
		} catch (IOException e){
			System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
			e.printStackTrace();
		}
		if (outputHFile!=null){
			FilenameFilter filterH = new FilenameFilter() {			
				public boolean accept(File arg0, String arg1) {
					return (arg1.contains("_") & arg1.contains("xml.html"));
				}
			};
			try {
				Path outputDirName1=new Path(outputDirName);
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile+".html"),"UTF-8"));
				if (files.length>0){
					out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");	
				}
				for (int ii=0; ii<files.length ; ii++){
					////temp=xmldir.getAbsolutePath().replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "");
					File ttt = new File(outputDirName1.toString()+fs+"xml"+fs+files[ii]);
					out.write("<br />"+"<a href=\""+ttt.getAbsolutePath()+"\">\n"+ttt.getAbsolutePath()+"</a>"+"\n");
				}
				if (files.length>0){
					out.write("</html>");
				}
				out.close();
			} catch (IOException e){
				System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
				e.printStackTrace();
			}
		}
	}
	 */

	public static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
		f.read(buffer);
		f.close();
		return new String(buffer);
	}


	public static ArrayList<String[]> sortbyLength(ArrayList<String[]> bitexts) {
		ArrayList<String[]> new_bitexts=new ArrayList<String[]>();
		int[] new_temp1 = new int[bitexts.size()];

		for (int i = 0; i < new_temp1.length; i++){
			new_temp1[i] =Integer.parseInt(bitexts.get(i)[5]);
		}
		Arrays.sort(new_temp1);
		int kk = 0;
		for (int i = 0; i < new_temp1.length; i++){
			if (i > 0 && new_temp1[i]==new_temp1[i -1])
				continue;
			new_temp1[kk++] = new_temp1[i];
		}
		int[] new_temp = new int[kk];
		System.arraycopy(new_temp1, 0, new_temp, 0, kk);
		for (int i=0; i<new_temp.length; i++){
			for (int jj=0;jj<bitexts.size();jj++){
				if (Integer.parseInt(bitexts.get(jj)[5])==new_temp[i]){
					new_bitexts.add(new String[] {bitexts.get(jj)[0], 
							bitexts.get(jj)[1],bitexts.get(jj)[2],bitexts.get(jj)[3],bitexts.get(jj)[4]});
				}
			}
		}
		return new_bitexts;
	}




	public static void writeOutList(String outputDirName, String outputFile, 
			String outputFileHTML, ArrayList<String[]> bitexts) {
		String filename;
		File ttt;
		try {
			Path outputDirName1=new Path(outputDirName);
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
			//=new String[bitexts.size()];
			for (int ii=bitexts.size()-1;ii>-1;ii--){
				filename=bitexts.get(ii)[0]+"_"+bitexts.get(ii)[1]+"_"+bitexts.get(ii)[4].substring(0, 1)+".xml";
				//ttt = new File(outputDirName1.toString()+fs+"xml"+fs+filename);
				out.write(outputDirName1.toString()+fs+"xml"+fs+filename+"\n");
			}
			out.close();
		} catch (IOException e){
			System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
			e.printStackTrace();
		}
		if (outputFileHTML!=null){
			try {
				Path outputDirName1=new Path(outputDirName);
				Writer out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileHTML),"UTF-8"));
				out1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
				for (int ii=bitexts.size()-1;ii>-1;ii--){
					filename=bitexts.get(ii)[0]+"_"+bitexts.get(ii)[1]+"_"+bitexts.get(ii)[4].substring(0, 1)+".xml.html";
					ttt = new File(outputDirName1.toString()+fs+"xml"+fs+filename);
					out1.write("<br />"+"<a href=\""+ttt.toURI().toString()+"\">\n"+ttt.toURI().toString()+"</a>"+"\n");
				}
				out1.write("</html>");
				out1.close();
			} catch (IOException e){
				System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
				e.printStackTrace();
			}	
		}
	}



	public static String extractURLfromXML(File xmldir, String inputString) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(xmldir.getAbsolutePath()+fs+inputString);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName("eAddress");
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (!NameElement.hasAttribute("type")){
					result+=NameElement.getTextContent();
					break;
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}



	public static String readfile(File txtfile) {
		String str=null, text=null;
		StringBuffer contents = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(txtfile));
			while ((str = in.readLine()) != null) {
				contents.append(str).append(System.getProperty("line.separator"));
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Problem in reading file: " + txtfile.getName());
		}
		//System.out.println(contents.toString());
		text = contents.toString();
		return text;
	}




	public static String[] calcStats(HashMap<String, String[]> props, HashMap<String, String[]> props_short,
			ArrayList<String[]> bitextsIM, ArrayList<String[]> bitexts, ArrayList<String[]> bitextsURLs) {
		String[] stats=new String[4];

		//if (bitextsIM!=null){
		if (!bitextsIM.isEmpty()){
			stats[0]=bitextsIM.get(0)[2];
			stats[1]=Integer.toString(0);
			stats[2]=bitextsIM.get(0)[3];
			stats[3]=Integer.toString(0);
			for (int ii=0;ii<bitextsIM.size();ii++){
				String p1 = bitextsIM.get(ii)[0];
				String p2 = bitextsIM.get(ii)[1];
				//String l1 = bitextsIM.get(ii)[2];
				//String l2 = bitextsIM.get(ii)[3];
				String[] attr1=props.get(p1);
				String[] attr2=props.get(p2);
				//System.out.println(p1+" in "+attr1[1]+":"+attr1[3]+"\t"+p2+" in "+attr2[1]+":"+attr2[3]);
				if (attr1[1].equals(stats[0]))
					stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr1[4]));
				if (attr1[1].equals(stats[2]))
					stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr1[4])); 
				if (attr2[1].equals(stats[0]))
					stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr2[4])); 
				if (attr2[1].equals(stats[2]))
					stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr2[4]));
			}
			if (!bitexts.isEmpty()){
				for (int ii=0;ii<bitexts.size();ii++){
					String p1 = bitexts.get(ii)[0];
					String p2 = bitexts.get(ii)[1];
					String[] attr1=props_short.get(p1);
					String[] attr2=props_short.get(p2);
					if (attr1[1].equals(stats[0]))
						stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr1[4]));
					if (attr1[1].equals(stats[2]))
						stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr1[4])); 
					if (attr2[1].equals(stats[0]))
						stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr2[4])); 
					if (attr2[1].equals(stats[2]))
						stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr2[4]));
				}
			}
		}else{
			//if (bitexts!=null){
			if (!bitexts.isEmpty()){
				stats[0]=bitexts.get(0)[2];
				stats[1]=Integer.toString(0);
				stats[2]=bitexts.get(0)[3];
				stats[3]=Integer.toString(0);
				for (int ii=0;ii<bitexts.size();ii++){
					String p1 = bitexts.get(ii)[0];
					String p2 = bitexts.get(ii)[1];
					String[] attr1=props_short.get(p1);
					String[] attr2=props_short.get(p2);
					if (attr1[1].equals(stats[0]))
						stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr1[4]));
					if (attr1[1].equals(stats[2]))
						stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr1[4])); 
					if (attr2[1].equals(stats[0]))
						stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr2[4])); 
					if (attr2[1].equals(stats[2]))
						stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr2[4]));
				}
			}
		}
		if (stats[0]==null)
			return null;
		return stats;
	}


	public static HashMap<String, String> findURLs(File xmldir) {

		HashMap<String, String> result = new HashMap<String, String>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		for (int ii=0; ii<files.length ; ii++){
			String key=files[ii].substring(0, files[ii].indexOf("."));
			String keyurl= extractURLfromXML(xmldir,files[ii]);
			result.put(key,keyurl);
		}
		return result;
	}

	public static ArrayList<String[]> findpairsURLs(
			HashMap<String, String> filesURLS, HashMap<String, String[]> props) {
		ArrayList<String[]> result=new ArrayList<String[]>();
		ArrayList<String> paired=new ArrayList<String>();
		Set<String> files_keys=filesURLS.keySet();
		Iterator<String> files_it = files_keys.iterator();
		String key, key1, file_url, file_url1;
		while (files_it.hasNext()){
			key = files_it.next();
			String[] pair=new String[6];
			if (paired.contains(key)) continue;
			if (props.get(key)==null) 
				continue;
			String lang1=props.get(key)[1];
			file_url=filesURLS.get(key);              
			Iterator<String> files_it1 = files_keys.iterator();
			while (files_it1.hasNext()){
				key1 = files_it1.next();
				if (paired.contains(key1)) continue;
				if (props.get(key1)==null) continue;
				String lang2=props.get(key1)[1];
				if (!lang1.equals(lang2)){
					file_url1=filesURLS.get(key1);
					if (file_url.replace("_"+lang1, "_"+lang2).equals(file_url1)
							| file_url.replace("/"+lang1+"/", "/"+lang2+"/").equals(file_url1)
							| file_url.replace("lang=1", "lang=2").equals(file_url1)
							| file_url.replace("lang,1", "lang,2").equals(file_url1)){
						pair[0]=key;
						pair[1]=key1;
						pair[2]=lang1;
						pair[3]=lang2;
						pair[4]="u";
						pair[5]=Integer.toString(Integer.parseInt(props.get(key)[4])+Integer.parseInt(props.get(key1)[4]));
						result.add(pair);
						paired.add(key);
						paired.add(key1);
						break;
					}
				}
			}
		}
		return result;
	}


	public static String[] calcStats1(HashMap<String, String[]> props,
			ArrayList<String[]> bitexts) {
		String[] stats=new String[4];

		if (!bitexts.isEmpty()){
			stats[0]=bitexts.get(0)[2];
			stats[1]=Integer.toString(0);
			stats[2]=bitexts.get(0)[3];
			stats[3]=Integer.toString(0);
			for (int ii=0;ii<bitexts.size();ii++){
				String p1 = bitexts.get(ii)[0];
				String p2 = bitexts.get(ii)[1];
				String[] attr1=props.get(p1);
				String[] attr2=props.get(p2);
				//System.out.println(p1+" in "+attr1[1]+":"+attr1[3]+"\t"+p2+" in "+attr2[1]+":"+attr2[3]);
				if (attr1[1].equals(stats[0]))
					stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr1[4]));
				if (attr1[1].equals(stats[2]))
					stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr1[4])); 
				if (attr2[1].equals(stats[0]))
					stats[1]=Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(attr2[4])); 
				if (attr2[1].equals(stats[2]))
					stats[3]=Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(attr2[4]));
			}
		}else
			return null;
		return stats;
	}


	public static String[] totalStats(String[] statsURLS, String[] statsIM,
			String[] statsSTRUCT) {
		String[] stats=new String[4];
		if (statsURLS!=null){
			stats=statsURLS;
			if (statsIM!=null){
				if (stats[0].equals(statsIM[0])){
					stats[1] = Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(statsIM[1]));
					stats[3] = Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(statsIM[3]));
				}else{
					stats[1] = Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(statsIM[3]));
					stats[3] = Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(statsIM[1]));
				}	
			}
			if (statsSTRUCT!=null){
				if (stats[0].equals(statsSTRUCT[0])){
					stats[1] = Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(statsSTRUCT[1]));
					stats[3] = Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(statsSTRUCT[3]));
				}else{
					stats[1] = Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(statsSTRUCT[3]));
					stats[3] = Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(statsSTRUCT[1]));
				}	
			}
		}else{
			if (statsIM!=null){
				stats=statsIM;
				if (statsSTRUCT!=null){
					if (stats[0].equals(statsSTRUCT[0])){
						stats[1] = Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(statsSTRUCT[1]));
						stats[3] = Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(statsSTRUCT[3]));
					}else{
						stats[1] = Integer.toString(Integer.parseInt(stats[1])+Integer.parseInt(statsSTRUCT[3]));
						stats[3] = Integer.toString(Integer.parseInt(stats[3])+Integer.parseInt(statsSTRUCT[1]));
					}	
				}
			}else{
				if (statsSTRUCT!=null){
					stats=statsSTRUCT;
				}else
					return null;
			}
		}
		return stats;

	}

}
