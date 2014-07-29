package gr.ilsp.fmc.main;

import gr.ilsp.fmc.exporter.XSLTransformer;
import gr.ilsp.fmc.utils.XMLTextCharsCleaner;

//import java.io.BufferedInputStream;
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
//import java.net.URI;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
//import org.apache.hadoop.fs.Path;
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
	private static final String resultDir = "xml";
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
		//Draft methods
		several_specific_helpful_tasks();
	}

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
			LOGGER.debug(key1);
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
		while (files_im_it.hasNext()){
			key_im = files_im_it.next();
			if (props.get(key_im)==null) continue;
			String lang1=props.get(key_im)[1];
			Set<String> mySet1 = new HashSet<String>();
			if (imagesInHTML.get(key_im)==null)
				continue;
			Collections.addAll(mySet1, imagesInHTML.get(key_im));
			if (mySet1.isEmpty()) continue;
			Set<String> all_files_keys=props.keySet();
			Iterator<String> all_files_it = all_files_keys.iterator();
			temp_pair="";
			double temp_pair_score=0.0;
			String temp_lang=null;
			while (all_files_it.hasNext()){
				key = all_files_it.next();
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
						if (jac>=jac_thr && dist>=0.6 && disttok > 0.3 && Math.abs(l1-l2)<2.0){ 
							if (jac>temp_pair_score){
								temp_pair=key;
								temp_pair_score=jac*dist;
							}
							temp_lang=lang2;
						}
					}
				}
			}

			if (!temp_pair.isEmpty())
				pairsIM.add(new String[] {key_im,temp_pair,lang1, temp_lang});
		}
		ArrayList<String[]> result=new ArrayList<String[]>();
		for (int ii=0;ii<pairsIM.size();ii++){
			String temp1=pairsIM.get(ii)[0];
			String temp2=pairsIM.get(ii)[1];
			for (int jj=ii+1;jj<pairsIM.size();jj++){
				if (pairsIM.get(jj)[1].equals(temp1) && pairsIM.get(jj)[0].equals(temp2)){
					result.add(new String[] {temp1,temp2,pairsIM.get(ii)[2], pairsIM.get(ii)[3],"im",
							Integer.toString(Integer.parseInt(props.get(temp1)[4])+Integer.parseInt(props.get(temp2)[4]))});
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
				xslTransformer.setBaseDir(outdir+fs+resultDir);
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
			String curXMLName= outdir+fs+resultDir+fs+f1+"_"+f2+"_"+confid.substring(0, 1)+".xml";

			writeCesAling(curXMLName,f1+".xml",f2+".xml",l1,l2,confid,cesAlign);
			if (oxslt) {
				try {
					inFile = new File(curXMLName);
					outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".xml.html");			
					xslTransformer.transform(inFile, outFile);
					String cesAlignText = ReadResources.readFileAsString(outFile.getAbsolutePath());
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

		//File temp = new File(curXMLName);
		//f1=temp.getParent()+fs+f1;
		//f2=temp.getParent()+fs+f2;
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

	private static double editDist(String s, String t) {
		int cost;

		if (s==t)	return 0;
		if (s.length() == 0) return t.length();
		if (t.length() == 0) return s.length();

		int[] v0 = new int[t.length() + 1];
		int[] v1 = new int[t.length() + 1];

		for (int i = 0; i < v0.length; i++)
			v0[i] = i;

		for (int i = 0; i < s.length(); i++){
			// calculate v1 (current row distances) from the previous row v0
			// first element of v1 is A[i+1][0]
			//   edit distance is delete (i+1) chars from s to match empty t
			v1[0] = i + 1;
			// use formula to fill in the rest of the row
			for (int j = 0; j < t.length(); j++){
				//System.out.println(s.substring(i, i+1));
				//System.out.println(t.substring(j, j+1));
				//System.out.println("----------");
				if (s.substring(i, i+1).equals(t.substring(j, j+1))) 
					/*if (s.substring(i, i+1).equals(t.substring(j, j+1))
	        			| s.substring(i, i+1).equals(t.substring(Math.max(1,j-1), j))
	        			| s.substring(i, i+1).equals(t.substring(j, Math.min(t.length(), j+1)))) */
					cost=0;
				else
					cost=1;
				v1[j + 1] = min3(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost);
			}
			// copy v1 (current row) to v0 (previous row) for next iteration
			for (int j = 0; j < v0.length; j++)
				v0[j] = v1[j];
		}
		return v1[t.length()];
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
			String outputFileHTML, ArrayList<String[]> bitexts, String definedDest) {
		String filename;
		//String ttt; //File ttt;
		//String outputDirName1= new File(outputDirName).toURI().toString(); //Path outputDirName1=new Path(outputDirName);
		//String tobeReplaced="", outputDirName2="";

		//tobeReplaced= new File(new File(outputFile).getParent()).toURI().toString();

		//outputDirName2 = new Path(outputDirName1.replace(tobeReplaced, "")).toUri().toString();
		//if (outputDirName2.equals(outputDirName1))
		//	ttt = outputDirName;
		//else
		//	ttt = outputDirName2;
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
			for (int ii=bitexts.size()-1;ii>-1;ii--){
				filename=bitexts.get(ii)[0]+"_"+bitexts.get(ii)[1]+"_"+bitexts.get(ii)[4].substring(0, 1)+".xml";
				//out.write(ttt+fs+resultDir+fs+filename+"\n");
				out.write(outputDirName+fs+resultDir+fs+filename+"\n");
			}
			out.close();
		} catch (IOException e){
			System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
			e.printStackTrace();
		}
		if (outputFileHTML!=null){
			try {
				Writer out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileHTML),"UTF-8"));
				if (bitexts.size()>0){
					out1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
					for (int ii=bitexts.size()-1;ii>-1;ii--){
						filename=bitexts.get(ii)[0]+"_"+bitexts.get(ii)[1]+"_"+bitexts.get(ii)[4].substring(0, 1)+".xml.html";
						//out1.write("<br />"+"<a href=\""+ttt+fs+resultDir+fs+filename+"\">\n"+ttt+fs+resultDir+fs+filename+"</a>"+"\n");
						out1.write("<br />"+"<a href=\""+outputDirName+fs+resultDir+fs+filename+"\">\n"+outputDirName+fs+resultDir+fs+filename+"</a>"+"\n");
					}
					out1.write("</html>");
				}
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
		text = contents.toString();
		return text;
	}




	public static String[] calcStats(HashMap<String, String[]> props, HashMap<String, String[]> props_short,
			ArrayList<String[]> bitextsIM, ArrayList<String[]> bitexts, ArrayList<String[]> bitextsURLs) {
		String[] stats=new String[4];

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
			HashMap<String, String> filesURLS, HashMap<String, String[]> props, String[][] urls_repls) {
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
			boolean match_found;
			while (files_it1.hasNext()){
				key1 = files_it1.next();
				if (paired.contains(key1)) continue;
				if (props.get(key1)==null) continue;
				String lang2=props.get(key1)[1];
				
				if (!lang1.equals(lang2)){
					file_url1=filesURLS.get(key1);
					match_found=false;
					if (urls_repls!=null){
						for (int ii=0;ii<urls_repls.length;ii++){
							if (file_url.replace(urls_repls[ii][0],urls_repls[ii][1]).equals(file_url1)){
								match_found=true;
								break;
							}
						}
					}
					if (match_found | file_url.replace("_"+lang1, "_"+lang2).equals(file_url1)
							| file_url.replace("/"+lang1+"/", "/"+lang2+"/").equals(file_url1)
							| file_url.replace("/"+lang1+"/", "").equals(file_url1) | file_url1.replace("/"+lang2+"/", "").equals(file_url)
							| file_url.replace("lang=1", "lang=2").equals(file_url1)
							| file_url.replace("lang,1", "lang,2").equals(file_url1)
							| file_url.replace("lang="+lang1, "lang="+lang2).equals(file_url1)
							| file_url.toLowerCase().replace("langid=1", "langid=2").equals(file_url1.toLowerCase())
							| file_url.replace("lang,1","lang,2").equals(file_url1) | file_url.replace("lang,2","lang,1").equals(file_url1)	
							| file_url.replace("/english/","/German/").equals(file_url1)
							|(file_url.substring(0, file_url.length()-4).equals(file_url1.substring(0, file_url1.length()-4)) &
									file_url.endsWith("="+lang1) & file_url1.endsWith("="+lang2)))
					{
						LOGGER.debug(file_url1);
						LOGGER.debug(file_url);
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

	public static String[] calcStats2(HashMap<String, String[]> props,
			ArrayList<String[]> bitexts, String type) {
		String[] stats=new String[4];

		if (!bitexts.isEmpty()){
			int jj=0;
			for (jj=0;jj<bitexts.size();jj++){
				if (bitexts.get(jj)[4].equals(type)){
					stats[0]=bitexts.get(jj)[2];
					stats[1]=Integer.toString(0);
					stats[2]=bitexts.get(jj)[3];
					stats[3]=Integer.toString(0);
					break;
				}
			}
			for (int ii=jj;ii<bitexts.size();ii++){
				if (bitexts.get(ii)[4].equals(type)){
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
			}
		}else
			return null;
		return stats;
	}

	public static ArrayList<String[]> findpairsURLs_dist(
			HashMap<String, String> filesURLS, HashMap<String, String[]> props) {
		ArrayList<String[]> result=new ArrayList<String[]>();
		ArrayList<String> paired=new ArrayList<String>();

		ArrayList<String> cand_paired=new ArrayList<String>();
		ArrayList<Double> dist_paired=new ArrayList<Double>();

		Set<String> files_keys=filesURLS.keySet();
		Iterator<String> files_it = files_keys.iterator();
		String key, key1, file_url, file_url1;
		while (files_it.hasNext()){
			key = files_it.next();
			//String[] pair=new String[6];
			if (props.get(key)==null) 
				continue;
			String lang1=props.get(key)[1];
			file_url=filesURLS.get(key);              
			int ind = file_url.indexOf(".");
			int ind1 = file_url.indexOf("/",ind);
			String file_url_file_url="";
			if (ind1<0)
				file_url_file_url=file_url.substring(ind, file_url.length());
			else
				file_url_file_url=file_url.substring(ind1, file_url.length());
			Iterator<String> files_it1 = files_keys.iterator();
			//System.out.println(file_url);
			while (files_it1.hasNext()){
				key1 = files_it1.next();
				if (props.get(key1)==null) continue;
				String lang2=props.get(key1)[1];
				if (!lang1.equals(lang2)){
					file_url1=filesURLS.get(key1);
					ind = file_url1.indexOf(".");
					ind1 = file_url1.indexOf("/",ind);
					String file_url1_file_url1="";
					if (ind<0)
						file_url1_file_url1=file_url1.substring(ind, file_url1.length());
					else
						file_url1_file_url1=file_url1.substring(ind1, file_url1.length());;

						double  temp_dist=editDist(file_url_file_url,file_url1_file_url1)
								/Math.max(file_url_file_url.length()-ind, file_url1_file_url1.length()-ind1);
						//System.out.println(file_url1);
						dist_paired.add(temp_dist);
						cand_paired.add(key+"_"+key1);
						//System.out.println(dist_paired.get(dist_paired.size()-1));
						//System.out.println("------------------");
						System.out.println(key+"\t"+key1+"\t"+file_url.length()+"\t"+file_url1.length()+"\t"+temp_dist);

				}
			}
		}
		System.out.println("================");

		int ind=-1, count_files=0, stop=1;
		double val=1000000; 
		while (count_files<files_keys.size() & stop==1){
			for (int ii=0;ii<cand_paired.size();ii++){
				if (dist_paired.get(ii)<val){
					ind=ii;
					val=dist_paired.get(ii);
				}
			}
			if (val>=0.25){
				stop=0;
				break;
			}
			String  temp_pair=cand_paired.get(ind);
			String[] temp_files=temp_pair.split("_");
			if (!paired.contains(temp_files[0]) && !paired.contains(temp_files[1])){
				String[] pair=new String[6];
				pair[0]=temp_files[0];
				pair[1]=temp_files[1];
				pair[2]=props.get(pair[0])[1];
				pair[3]=props.get(pair[1])[1];
				pair[4]="u";
				pair[5]=Integer.toString(Integer.parseInt(props.get(pair[0])[4])+Integer.parseInt(props.get(pair[1])[4]));
				result.add(pair);
				paired.add(pair[0]);
				paired.add(pair[1]);
				count_files=count_files+2;
				System.out.println(temp_files[0]+"_"+temp_files[1]+ " : "+ val);
				for (int ii=0;ii<cand_paired.size();ii++){
					temp_pair=cand_paired.get(ii);
					temp_files=temp_pair.split("_");
					if (paired.contains(temp_files[0]) | paired.contains(temp_files[1])){
						dist_paired.set(ii,1000000.0); 
						val=1000000;
					}
				}
			}/*else{
				paired.	
			}*/
			//dist_paired.set(ind,1000000.0);
			//val=1000000;
		}


		return result;
	}
	////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	///////temp_methods to be removed///////////////////////////////// 
	
	

	private static void several_specific_helpful_tasks() {
		
		sample_selection("C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\delivered\\alignOutput2014-05-26_024952.log");
		
		count_tmx_scores("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih", "alignOutput2014-05-23_105241.txt", "scores.txt");
		
		correct_select_count_tmx("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih", "alignOutput2014-05-23_105241.txt", 0.4);
		
		select_copy_docpairs("C:\\QTLaunchPad\\MEDICAL\\EN-PT", "C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih",
				"C:\\QTLaunchPad\\MEDICAL\\EN-PT\\outputs\\output.txt" ,"u i h");
		
		check_bilingual_collection("C:\\QTLaunchPad\\AUTOMOTIVE\\EN-PT\\", "en;pt","output_pdfs.txt" );
		
		merge_outlist_files("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih\\outputs","output.txt");
		
		//task1//
		String parent_dir  ="C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\"; 
		String pair_list ="C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\total_pairs.txt";
		//task2//
		String list_with_attricutes_of_files =
				"C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\EN_AUTOMOTIVE_results_nonCC.txt";
		//task3/
		String xml_dir="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		String xmlCC_dir="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml_CC";
		String nonCC_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\output_AUTOMOTIVE_EN_nonCC.txt";
		String CC_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\output_AUTOMOTIVE_EN_CC.txt";
		String licensed_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\AUTOMOTIVE_EN_licensed.txt";
		String pathstring="qtlp_20131227_132408/e401c679-d4d1-4099-8711-a3b97d634614/xml/";
		//task4/
		String target_dir =
				"C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		String exten="xml";
		String exclude_str="crawlinfo=";
		//task5//
		String removefilelist="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\to_remove.txt";
		String path_file_t="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		int task=4;

		if (task==1){
			pdf_pairs(pair_list, parent_dir);
			System.out.println("existance of pdf files in list of pairs is checked");
		}
		if (task==2){
			//list of attributes are extracted by QueryXMLwithXpath class
			print_unique_sites_in_collection(list_with_attricutes_of_files);
			System.out.println("unique sites in collection is printed");
		}
		if (task==3){
			discriminate_CC_from_nonCC(xml_dir, xmlCC_dir, licensed_list, nonCC_list, CC_list, pathstring);
			System.out.println("files with CC licensed are moved from XML to XML_CC");
		}
		if (task==4){
			counttokens(target_dir,exten,exclude_str);
			System.out.println("tokens calculated.");
		}
		if (task==5){
			//remove files from collection
			try {
				String tmp1 = ReadResources.readFileAsString(removefilelist);
				String[] filestoremove = tmp1.split("\n");	
				ArrayList<String> filestoremoveList=new ArrayList<String>();
				for (int ii=0;ii<filestoremove.length;ii++){
					filestoremoveList.add(filestoremove[ii]);
				}
				File xmldir = new File(path_file_t);
				String[] filesinXML= xmldir.list();
				for (int ii=0;ii<filesinXML.length;ii++){
					if (filesinXML[ii].endsWith("xml")){
						if (filestoremoveList.contains(filesinXML[ii])){
							File tempfile=new File(path_file_t+"\\"+filesinXML[ii]);
							System.out.println(tempfile.getName());
							tempfile.delete();
							tempfile=new File(path_file_t+"\\"+filesinXML[ii]+".html");
							System.out.println(tempfile.getName());
							tempfile.delete();
							String tt = filesinXML[ii].substring(0, filesinXML[ii].indexOf("."));
							tempfile=new File(path_file_t+"\\"+tt+".html");
							System.out.println(tempfile.getName());
							tempfile.delete();
						}
					}
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void sample_selection(String tmx_listfile) {
		String[] types =new String[ ] {"u","i","h", "l", "m", "pdf"};
		int[] tmxfile_counts = new int[types.length], selected_tmxfile_counts = new int[types.length];
		int[] tmx_counts = new int[types.length], selected_tmx_counts = new int[types.length];
		boolean found=false;
		int total_tmx=0, totaltmxfile=0; 
		double sample_factor = 0.05, sample_factor1 = 0.05; 
		try {
			ArrayList<String> tmx_files=ReadResources.readFileLineByLine(tmx_listfile);
			for (int ii=0;ii<tmx_files.size();ii++){
				if (!tmx_files.get(ii).startsWith("align"))
					continue;
				
				String[] temp = tmx_files.get(ii).split(" ");
				found = false;
				for (int jj=0;jj<types.length;jj++){
					if (temp[0].endsWith(types[jj]+".tmx")){
						found=true;
						tmx_counts[jj]=tmx_counts[jj]+Integer.parseInt(temp[2]);
						tmxfile_counts[jj]=tmxfile_counts[jj]+1;
						break;
					}
				}
				if (!found){
					System.out.println("EP. Somethng goes wrong, since the type is not recognised.");
					System.exit(0);
				}
			}
			for (int jj=0;jj<tmx_counts.length;jj++){
				total_tmx = total_tmx + tmx_counts[jj];
				totaltmxfile = totaltmxfile + tmxfile_counts[jj];
			}
			System.out.println("total tmxfiles: "+ totaltmxfile);
			System.out.println("total tmx sentence pairs: "+ total_tmx);
						
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("Enter sample size of files (Integer):");
	        int sample_files=0;
	        try{
	        	sample_files = Integer.parseInt(br.readLine());
	        }catch(NumberFormatException nfe){
	            System.err.println("Invalid Format!");
	        }
	        System.out.print("Enter sample size of sentence pairs (Integer):");
	        int sample_sentencepairs=0;
	        try{
	            sample_sentencepairs = Integer.parseInt(br.readLine());
	        }catch(NumberFormatException nfe){
	            System.err.println("Invalid Format!");
	        }
			System.out.println("files to be selected: " + sample_files);
			System.out.println("files to be selected: " + sample_sentencepairs);
			sample_factor = (double) sample_files / (double) totaltmxfile;
			sample_factor1 = (double) sample_sentencepairs / (double) total_tmx;
			for (int jj=0;jj<tmx_counts.length;jj++){
				selected_tmxfile_counts[jj] = (int) Math.round(sample_factor*tmxfile_counts[jj]);
				selected_tmx_counts[jj] = (int) Math.round(sample_factor1*tmx_counts[jj]);
			}
//			System.out.println("aaa");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void count_tmx_scores(String location, String tmx_list_file, String score_text) {
		String score="";
		try {
			String tmx_list = ReadResources.readFileAsString(location+fs+tmx_list_file);
			String[] pairlist = tmx_list.split("\r\n");
			
			for (int ii=0;ii<pairlist.length;ii++){
				LOGGER.debug("ID:\t"+ii);
				LOGGER.debug(location+fs+pairlist[ii]);
				score = score+ 
						ReadResources.extractAttrfromXML(location+fs+pairlist[ii],"tu", "score", true);
				if (score.isEmpty())
					continue;
				
			}
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		ReadResources.writetextfile(location+fs+score_text, score);
	}

	private static void merge_outlist_files(String sourcedir, String newfile) {
		File curdir = new File(sourcedir);
		File[] files= curdir.listFiles();

		String whole_text = "";
		String filename = curdir.getAbsolutePath()+fs+newfile;
		for (File file:files){
			if (file.getName().endsWith(".txt") & file.getName().startsWith("out")){
				try {
					String text = ReadResources.readFileAsString(file.getAbsolutePath());
					whole_text=whole_text+"\n"+text;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		ReadResources.writetextfile(filename, whole_text);
	}

	private static void check_bilingual_collection(String source_path,	String langs1, String output_list_file) {
		int p_xml=0, p_xmlhtml=0, xml1=0, xml2=0, html1=0, html2=0, xmlhtml1=0, xmlhtml2=0;
		String[] langs = langs1.split(";");
		int[] tokens = new int[2];
		ArrayList<String> fff = new ArrayList<String>();
		try {
			String text_lists = ReadResources.readFileAsString(source_path+fs+output_list_file);
			String[] pairs = text_lists.split("\n");

			for (int ii=0; ii<pairs.length;ii++){
				pairs[ii] = pairs[ii].replace("/", "\\");
				if (pairs[ii].startsWith("pdfs") )
					continue;
				File pairfile = new File(source_path+"\\"+pairs[ii]);
				String pparent = pairfile.getParent();
				String pname =  pairfile.getName();
				String[] items = pname.split("_");

				//xml pair
				if (pairfile.exists())
					p_xml++;
				else
					LOGGER.info(pairfile.getAbsolutePath() + " does not exist.");
				//xmlhtml pair
				pairfile = new File(source_path+fs+pairs[ii]+".html");
				if (pairfile.exists())
					p_xmlhtml++;
				else
					LOGGER.info(pairfile.getAbsolutePath() + " does not exist.");

				File file1 = new File(pparent+fs+items[0]+".xml");
				File file2 = new File(pparent+fs+items[1]+".xml");
				//xml 1
				if (file1.exists()){
					String langIdentified = ReadResources.extractLangfromXML(file1.getAbsolutePath(), "language", "iso639");
					String text_temp = ReadResources.extractTextfromXML_clean(file1.getAbsolutePath(),"p","crawlinfo", false);
					StringTokenizer st = new StringTokenizer(text_temp);	
					if (langIdentified.equals(langs[0]))
						tokens[0]=tokens[0]+st.countTokens();
					else
						tokens[1]=tokens[1]+st.countTokens();
					xml1++;

					//ReadResources.writetextfile(source_path+"\\"+langs[0]+"\\"+xml1+".txt", text_temp);
					//FileUtils.copyFile(file1, new File(source_path+"\\"+langs[0]+"\\"+xml1+".xml"));
				}else
					LOGGER.info(file1.getAbsolutePath() + " does not exist.");
				//xml2
				if (file2.exists()){
					String langIdentified = ReadResources.extractLangfromXML(file2.getAbsolutePath(), "language", "iso639");
					String text_temp = ReadResources.extractTextfromXML_clean(file2.getAbsolutePath(),"p","crawlinfo", false);
					StringTokenizer st = new StringTokenizer(text_temp);	
					if (langIdentified.equals(langs[0]))
						tokens[0]=tokens[0]+st.countTokens();
					else
						tokens[1]=tokens[1]+st.countTokens();
					xml2++;
					//ReadResources.writetextfile(source_path+"\\"+langs[1]+"\\"+xml1+".txt", text_temp);
					//FileUtils.copyFile(file2, new File(source_path+"\\"+langs[1]+"\\"+xml2+".xml"));
				}else
					LOGGER.info(file2.getAbsolutePath() + " does not exist.");

				if (fff.contains(file1.getAbsolutePath()))
					LOGGER.info("EEEP	" +  file1.getAbsolutePath());
				else
					fff.add(file1.getAbsolutePath());
				if (fff.contains(file2.getAbsolutePath()))
					LOGGER.info("EEEP	" +  file2.getAbsolutePath());
				else
					fff.add(file2.getAbsolutePath());

				file1 = new File(pparent+"\\"+items[0]+".html");
				file2 = new File(pparent+"\\"+items[1]+".html");
				//html 1
				if (file1.exists())
					html1++;
				else
					LOGGER.info(file1.getAbsolutePath() + " does not exist.");
				//html2
				if (file2.exists())
					html2++;
				else
					LOGGER.info(file2.getAbsolutePath() + " does not exist.");

				file1 = new File(pparent+"\\"+items[0]+".xml.html");
				file2 = new File(pparent+"\\"+items[1]+".xml.html");
				//xmlhtml 1
				if (file1.exists())
					xmlhtml1++;
				else
					LOGGER.info(file1.getAbsolutePath() + " does not exist.");
				//xmlhtml2
				if (file2.exists())
					xmlhtml2++;
				else
					LOGGER.info(file2.getAbsolutePath() + " does not exist.");
			}
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		LOGGER.info(p_xml +"\t"+ p_xmlhtml+"\t"+ xml1+"\t"+xml2+"\t"+html1+"\t"+html2+"\t"+xmlhtml1+"\t"+xmlhtml2);
		System.out.println("DONE");
	}

	private static void select_copy_docpairs(String sourcedir1, String targetdir1, String cesAling_listfile, String type) {
		try {
			File sourcedir = new File(sourcedir1);
			File targetdir = new File(targetdir1);
			String[] pairlist = ReadResources.readFileAsString(cesAling_listfile).replace("/", "\\").split("\n");
			String[] types = type.split(" ");
			for (int ii=0;ii<pairlist.length;ii++){	
				if (pairlist[ii].startsWith("pdfs"))
					continue;
				if (!pairlist[ii].endsWith("_"+types[0]+".xml") 
						& !pairlist[ii].endsWith("_"+types[1]+".xml") 
						& !pairlist[ii].endsWith("_"+types[2]+".xml"))
					continue;
				LOGGER.info(ii);
				//File temppair = new File(sourcedir.getAbsolutePath()+fs+pairlist[ii]);
				//File temppairNEW = new File(targetdir.getAbsolutePath()+fs+pairlist[ii]);
				//FcFileUtils.copyFile(temppair, temppairNEW);
				ReadResources.copy(sourcedir.getAbsolutePath()+fs+pairlist[ii], targetdir.getAbsolutePath()+fs+pairlist[ii]);

				//temppair = new File(sourcedir.getAbsolutePath()+"\\"+pairlist[ii]+".html");
				//temppairNEW = new File(targetdir.getAbsolutePath()+"\\"+pairlist[ii]+".html");
				//FcFileUtils.copyFile(temppair, temppairNEW);
				ReadResources.copy(sourcedir.getAbsolutePath()+fs+pairlist[ii]+".html",
						targetdir.getAbsolutePath()+fs+pairlist[ii]+".html");

				File temppair = new File(sourcedir.getAbsolutePath()+fs+pairlist[ii]);
				File temppairNEW = new File(targetdir.getAbsolutePath()+fs+pairlist[ii]+".html");
				String[] tempitems = temppair.getName().split("_");

				//File tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+".xml");
				//File tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[0]+".xml");
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				ReadResources.copy(temppair.getParent()+fs+tempitems[0]+".xml",
						temppairNEW.getParent()+fs+tempitems[0]+".xml");

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+".xml.html");
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[0]+".xml.html");
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				ReadResources.copy(temppair.getParent()+fs+tempitems[0]+".xml.html",
						temppairNEW.getParent()+fs+tempitems[0]+".xml.html");

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+".html");
				//tempitemNEW = new File(tempitemNEW.getParent()+"\\"+tempitems[0]+".html");
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				ReadResources.copy(temppair.getParent()+fs+tempitems[0]+".html",
						temppairNEW.getParent()+fs+tempitems[0]+".html");

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+".xml");
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[1]+".xml");
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				ReadResources.copy(temppair.getParent()+fs+tempitems[1]+".xml",
						temppairNEW.getParent()+fs+tempitems[1]+".xml");

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+".xml.html");
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[1]+".xml.html");
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				ReadResources.copy(temppair.getParent()+fs+tempitems[1]+".xml.html",
						temppairNEW.getParent()+fs+tempitems[1]+".xml.html");

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+".html");
				//tempitemNEW = new File(tempitemNEW.getParent()+"\\"+tempitems[1]+".html");
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				ReadResources.copy(temppair.getParent()+fs+tempitems[1]+".html",
						temppairNEW.getParent()+fs+tempitems[1]+".html");
			}
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}

	private static void correct_select_count_tmx(String location, String tmx_list_file,	double aling_thr) {

		int counter_tmx=0;
		try {
			String tmx_list = ReadResources.readFileAsString(location+fs+tmx_list_file);
			String[] pairlist = tmx_list.split("\r\n");
			for (int ii=0;ii<pairlist.length;ii++){
				XMLTextCharsCleaner.clean(location+fs+pairlist[ii], location+fs+pairlist[ii]+"1");
				ReadResources.copy(location+fs+pairlist[ii]+"1",location+fs+pairlist[ii]);
				File temp = new File(location+fs+pairlist[ii]+"1");
				temp.delete();
			}
			for (int ii=0;ii<pairlist.length;ii++){
				LOGGER.info("ID:\t"+ii);
				LOGGER.info(location+fs+pairlist[ii]);
				String score = 
						ReadResources.extractAttrfromXML(location+fs+pairlist[ii],"tu", "score", true);
				if (score.isEmpty())
					continue;
				String scores[] =score.split("\n");
				LOGGER.info(scores.length);
				for (int jj=0; jj<scores.length;jj++){
					if (Double.parseDouble(scores[jj])>=aling_thr){
						counter_tmx++;
					}
				}
			}
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		System.out.println(counter_tmx);

	}

	public static void counttokens(String target_dir, String exten, String exclude_str) {
		File xmldir = new File(target_dir);
		String[] filesinXML= xmldir.list();
		String tmp1;
		int tokens_num=0;
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith(exten)){
				//System.out.println(filesinXML[ii]);
				try {
					tmp1 = ReadResources.readFileAsString(target_dir+fs+filesinXML[ii]);
					String[] lines = tmp1.split("\n");
					for (int jj=0;jj<lines.length;jj++){
						if (lines[jj].contains("<p id=") & !lines[jj].contains(exclude_str)){
							//System.out.println(lines[jj]);
							int s=lines[jj].indexOf(">")+1;
							int e=lines[jj].length()-4;
							String temp=lines[jj].substring(s, e);
							StringTokenizer st = new StringTokenizer(temp);	
							//double words_num=st.countTokens();
							tokens_num=tokens_num+st.countTokens();
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		System.out.println(tokens_num);

	}

		
	private static void pdf_pairs(String pair_list, String parent_dir) {

		File tempfile = new File(pair_list);
		try {
			BufferedReader in = new BufferedReader(new FileReader(tempfile));
			String str; //, file1, file2;
			int counter=1;
			//HashMap<String, Integer> sites = new  HashMap<String, Integer>();
			while ((str = in.readLine()) != null) {
				String[] linedata = str.split("\t");			
				if (linedata.length!=2){
					System.out.println("problem in line "+counter+ " of " + pair_list);
					System.exit(0);
				}
				//file1=linedata[0].replaceAll("/", fs);		file2=linedata[1].replaceAll("/", fs);
				File pdf1=new File(parent_dir+fs+linedata[0]);
				File pdf2=new File(parent_dir+fs+linedata[1]);
				if (!pdf1.exists()){
					System.out.println("DOES NOT EXIST file "+ pdf1.getAbsolutePath() + " mentioned in line "+counter+ " of " + pair_list);
					//System.exit(0);
				}
				if (!pdf2.exists()){
					System.out.println("DOES NOT EXIST file "+ pdf2.getAbsolutePath() + " mentioned in line "+counter+ " of " + pair_list);
					//System.exit(0);
				}
				counter++;
			}
			in.close();
		} catch (IOException e) {
		}
	}


	private static void print_unique_sites_in_collection(
			String list_with_attricutes_of_files) {

		File tempfile = new File(list_with_attricutes_of_files);
		try {
			BufferedReader in = new BufferedReader(new FileReader(tempfile));
			String str, site, tempstr; //, tempsite;
			int ind1, ind2, counter; //ind3, ind4 
			HashMap<String, Integer> sites = new  HashMap<String, Integer>();
			while ((str = in.readLine()) != null) {
				String[] linedata = str.split("\t");				
				site=linedata[2];				ind1=site.indexOf("//");
				tempstr=site.substring(ind1+2);	ind2=tempstr.indexOf("/")+ind1+2;
				if (ind2>=site.length()| ind2<0)
					ind2=site.length()-1;
				site = site.substring(ind1+2, ind2);	//	ind3 = site.lastIndexOf(".");
				//tempsite = site.substring(0, ind3);			ind4 = tempsite.lastIndexOf(".");
				//site = site.substring(ind4+1);
				if (sites.containsKey(site)){
					counter = sites.get(site)+1;
					sites.put(site, counter);
				}
				else
					sites.put(site, 1);
			}
			in.close();
			Set<String> site_keys=sites.keySet();
			Iterator<String> key = site_keys.iterator();
			String key_im = "";
			while (key.hasNext()){
				key_im = key.next();
				//LOGGER.info(key_im+"\t"+sites.get(key_im));
				System.out.println(key_im+"\t"+sites.get(key_im));
			}
		} catch (IOException e) {
		}
	}


	private static void discriminate_CC_from_nonCC(String xml_dir,
			String xmlCC_dir, String licensed_list, String nonCC_list, String CC_list, String pathstring) {

		String tmp1;
		try {
			tmp1 = ReadResources.readFileAsString(licensed_list);
			String[] filestoremove = tmp1.split("\n");	
			ArrayList<String> filestoremoveList=new ArrayList<String>();
			for (int ii=0;ii<filestoremove.length;ii++)
				filestoremoveList.add(filestoremove[ii]);
			File xmldir = new File(xml_dir);
			String[] filesinXML= xmldir.list();
			int counter=0;
			for (int ii=0;ii<filesinXML.length;ii++){
				if (filesinXML[ii].endsWith("xml")){
					if (filestoremoveList.contains(filesinXML[ii])){
						String tt = filesinXML[ii].substring(0,filesinXML[ii].indexOf("."));
						//".html"
						File tempfile=new File(xml_dir+"\\"+tt+".html");
						File destfile=new File(xmlCC_dir+"\\"+tt+".html");
						FileUtils.moveFile(tempfile, destfile);
						//".xml"
						tempfile=new File(xml_dir+"\\"+filesinXML[ii]);
						destfile=new File(xmlCC_dir+"\\"+tt+".xml");
						FileUtils.moveFile(tempfile, destfile);
						//".xml.html"
						tt=filesinXML[ii].substring(0, filesinXML[ii].lastIndexOf("."))+".xml.html";
						tempfile=new File(xml_dir+"\\"+tt);
						destfile=new File(xmlCC_dir+"\\"+tt);
						FileUtils.moveFile(tempfile, destfile);

						counter++;
						System.out.println(counter);
					}
				}
			}

		}catch (IOException e1) {
			e1.printStackTrace();
		}

		File xmldir = new File(xml_dir);
		String[] filesinXML= xmldir.list();
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("html") & !filesinXML[ii].contains("xml.html")){
				File tempfile=new File(xml_dir+"\\"+filesinXML[ii]);
				System.out.println(tempfile.getName());
				tempfile.delete();
			}
		}

		String temp_dir = xmlCC_dir;
		xmldir = new File(temp_dir);
		filesinXML= xmldir.list();
		String urlList="";
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("xml"))
				urlList=urlList + pathstring+filesinXML[ii]+"\n";
		}
		ReadResources.writetextfile(CC_list,urlList);
		DedupMD5.writeHTMLfile(CC_list+".html",urlList,true);

		temp_dir = xml_dir;
		xmldir = new File(temp_dir);
		filesinXML= xmldir.list();
		urlList="";
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("xml"))
				urlList=urlList + pathstring+filesinXML[ii]+"\n";
		}
		ReadResources.writetextfile(nonCC_list,urlList);
		DedupMD5.writeHTMLfile(nonCC_list+".html",urlList,true);
	}
}
