package gr.ilsp.fmc.main;
//vpapa

//import java.io.BufferedInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.hadoop.fs.Path;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.evt.XMLEvent2;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;

public class Bitexts {
	private static final String cesDocVersion = "1.0";
	private static final String LANGUAGE_ELE = "language";
	private static final String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	private static final String HTTP_PATH = "http://nlp.ilsp.gr/soaplab2-results/";	
	private static String cesNameSpace = "http://www.w3.org/1999/xlink";
	private static String cesNameSpace1 = "http://www.xces.org/schema/2003";
	private static String cesNameSpace2 = "http://www.w3.org/2001/XMLSchema-instance";
	private static String fs = System.getProperty("file.separator");
	//private final static String appendXmlExt = ".xml";
	//private final static double term_thresh=0.5;
	private static final String URL_ELE = "eAddress";
	//private static int diagonal_beam=5;
	private static double text_thres=0.4;
	private static double length_thres=0.4;
	private static double pars_thres=0.4;
	private static int level_thres=2;

	@SuppressWarnings("restriction")
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
			//String filepair="";
			//String langpair="";
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
						}
						else{
							f1 = tl_length/sl_length;
							f3=dist/tl_length;
						}
						if (tl_par>=sl_par){
							f2=sl_par/tl_par;
						}else{
							f2=tl_par/sl_par;
						}
						if (f3>=0.30 || f1<=0.7 || f2<=0.7)
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
		String temp="";
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
				temp =pairs.get(jj)[1]; 
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
				temp =pairs.get(jj)[0]; 
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

	/*private static int maxArray(int[][] num) {
		int res=0;
		for (int ii=0;ii<num.length;ii++){
			res=Math.max(res, num[ii][0]);
		}
		for (int ii=0;ii<num.length;ii++){
			res=Math.max(res, num[ii][1]);
		}
		return res;
	}*/


	@SuppressWarnings("restriction")
	public static void writeXMLs(String outdir,ArrayList<String[]> bitexts, boolean cesAlign) throws UnsupportedEncodingException, FileNotFoundException, XMLStreamException{
		for (int ii=0;ii<bitexts.size();ii++){
			String f1=bitexts.get(ii)[0];
			String f2=bitexts.get(ii)[1];
			String l1=bitexts.get(ii)[2];
			String l2=bitexts.get(ii)[3];
			String confid=bitexts.get(ii)[4];
			Path outdir1 = new Path(outdir);
			//String f11="", f22="";
			//String ff1 = f1.substring(f1.lastIndexOf("/")+1, f1.lastIndexOf("."));
			//String ff2 = f2.substring(f2.lastIndexOf("/")+1, f2.lastIndexOf("."));
			//String curXMLName=f1.substring(0, f1.lastIndexOf("/"))+"/"+ff1+"_"+ff2+".xml";
			String curXMLName= outdir+fs+"xml"+fs+f1+"_"+f2+"_"+confid.substring(0, 1)+".xml";

			XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
			OutputStreamWriter wrt = new OutputStreamWriter(new FileOutputStream(curXMLName),"UTF-8");
			XMLStreamWriter2 xtw = (XMLStreamWriter2) xof.createXMLStreamWriter(wrt);
			//System.err.println("Attempting to write "+curXMLName);
			f1 = outdir1.toString()+fs+"xml"+fs+f1+".xml";
			f2 = outdir1.toString()+fs+"xml"+fs+f2+".xml";
			//String v1 =outdir.replace(VAR_RES_CACHE,HTTP_PATH);
			f1 = f1.replace(VAR_RES_CACHE,HTTP_PATH);
			f2 = f2.replace(VAR_RES_CACHE,HTTP_PATH);
			//vpapa added this just for development on windows
			f1 = f1.substring(f1.indexOf("http:"));
			f2 = f2.substring(f2.indexOf("http:"));
			xtw.writeStartDocument();
			if (cesAlign){
				xtw.writeProcessingInstruction("xml-stylesheet href='http://nlp.ilsp.gr/panacea/xces-xslt/cesAlign.xsl' type='text/xsl'");
				//xtw.writeProcessingInstruction("xml-stylesheet href='http://nlp.ilsp.gr/panacea/xces-xslt/cesAlign.xsl' type='text/xsl'");
				//xtw.writeProcessingInstruction("xml-stylesheet", "href='http://nlp.ilsp.gr/panacea/xces-xslt/cesAlign.xsl' type='text/xsl'");
			}
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
		if (num_of_text_par>5)
			return patt_int;
		else
			return null;


	}

	@SuppressWarnings("restriction")
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

	public static void writeOutList(String outputDirName, String outputFile) {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.contains("_"));
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
				//temp=xmldir.getAbsolutePath().replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "");
				//out.write(temp+fs+files[ii]+"\n");

				String ttt = (outputDirName1.toString()+fs+"xml"+fs+files[ii]).replace(VAR_RES_CACHE,HTTP_PATH);
				ttt=ttt.substring(ttt.indexOf("http:"));
				out.write(ttt+"\n");


			}
			out.close();
		} catch (IOException e){
			System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
			e.printStackTrace();
		}
		try {
			//FileWriter outFile = new FileWriter(outputFile);
			//PrintWriter out = new PrintWriter(outFile);
			Path outputDirName1=new Path(outputDirName);
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile+".html"),"UTF-8"));
			out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");

			for (int ii=0; ii<files.length ; ii++){
				//temp=xmldir.getAbsolutePath().replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "");
				//out.write(temp+fs+files[ii]+"\n");
				String ttt = (outputDirName1.toString()+fs+"xml"+fs+files[ii]).replace(VAR_RES_CACHE,HTTP_PATH);
				ttt=ttt.substring(ttt.indexOf("http:"));
				ttt = "<a href=\""+ttt+"\">"+ttt+"</a>";
				out.write("<br />"+ttt);
			}
			out.write("</html>");
			out.close();
		} catch (IOException e){
			System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
			e.printStackTrace();
		}
	}

	public static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
		f.read(buffer);
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

	public static void writeOutList(String outputDirName, String outputFile, String outputFileHTML, ArrayList<String[]> bitexts) {
		//File xmldir=new File(outputDirName+fs+"xml");
		//String[] files= xmldir.list(filter);
		//String temp="";
		try {
			//FileWriter outFile = new FileWriter(outputFile);
			//PrintWriter out = new PrintWriter(outFile);
			Path outputDirName1=new Path(outputDirName);
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
			String[] files=new String[bitexts.size()];
			for (int ii=bitexts.size()-1;ii>-1;ii--){
				files[ii]=bitexts.get(ii)[0]+"_"+bitexts.get(ii)[1]+"_"+bitexts.get(ii)[4].substring(0, 1)+".xml";
				//}
				//for (int ii=0; ii<files.length ; ii++){
				//temp=xmldir.getAbsolutePath().replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "");
				//out.write(temp+fs+files[ii]+"\n");
				String ttt = (outputDirName1.toString()+fs+"xml"+fs+files[ii]).replace(VAR_RES_CACHE,HTTP_PATH);
				ttt=ttt.substring(ttt.indexOf("http:"));
				out.write(ttt+"\n");
			}
			out.close();
		} catch (IOException e){
			System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
			e.printStackTrace();
		}
		if (outputFileHTML!=null){
			try {
				//FileWriter outFile = new FileWriter(outputFile);
				//PrintWriter out = new PrintWriter(outFile);
				Path outputDirName1=new Path(outputDirName);
				Writer out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileHTML),"UTF-8"));
				out1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
				String[] files=new String[bitexts.size()];
				for (int ii=bitexts.size()-1;ii>-1;ii--){
					//temp=xmldir.getAbsolutePath().replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "");
					//out.write(temp+fs+files[ii]+"\n");
					files[ii]=bitexts.get(ii)[0]+"_"+bitexts.get(ii)[1]+"_"+bitexts.get(ii)[4].substring(0, 1)+".xml";

					String ttt = (outputDirName1.toString()+fs+"xml"+fs+files[ii]).replace(VAR_RES_CACHE,HTTP_PATH);
					ttt=ttt.substring(ttt.indexOf("http:"));
					ttt = "<a href=\""+ttt+"\">"+ttt+"</a>";
					out1.write("<br />"+ttt);
				}
				out1.write("</html>");
				out1.close();
			} catch (IOException e){
				System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
				e.printStackTrace();
			}	
		}
	}



	/*@SuppressWarnings("restriction")
	public static String[][] representXML_old(File xmldir) throws FileNotFoundException, XMLStreamException {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		String[][] res = new String[files.length][3];
		String url="", lang="", curElement="";//previous_element="";

		for (int ii=0; ii<files.length ; ii++){
			//System.out.println(ii);
			int pcounter=0;
			OutputStreamWriter xmlFileListWrt = null;
			boolean topic=false;
			try {
				xmlFileListWrt = new OutputStreamWriter(new FileOutputStream
						(xmldir.getPath()+"/"+files[ii]+".txt"),"UTF-8");
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
				//System.err.println("Parsing :"+files[ii]);
				while (xmlr.hasNext()) {
					eventType = xmlr.next();
					if (eventType == XMLEvent2.START_ELEMENT){
						curElement = xmlr.getLocalName().toString();
						//previous_element="";
						if (curElement.equals(LANGUAGE_ELE)) {
							//System.out.println(xmlr.isStartElement());
							lang=xmlr.getAttributeValue(0);
							res[ii][1]=lang;
						}else{
							if (curElement.equals(URL_ELE)){
								//System.out.println(xmlr.isStartElement());
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
									//System.out.println(xmlr.isStartElement());
									int attrs=xmlr.getAttributeCount();
									//previous_element=curElement;
									//System.out.println(xmlr.getElementText());
									int t=-1, t1=0;
									for (int m=1;m<attrs;m++){
										//System.out.println(xmlr.getAttributeLocalName(m));
										//System.out.println(xmlr.getAttributeValue(m));
										//System.out.println(xmlr.getAttributeAsQName(m));
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
											topic=true;
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
										//if (t<0) {
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
			}
			//if (!topic){
			//	res[ii][1]="";
			//	res[ii][0]="";
			//	res[ii][2]="";
			//}
		}
		return res;
	}	*/

	/*public static ArrayList<String[]> findpairsXML_old(File xmldir, String[][] AAA) {
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		//int[] tl=new int[9];
		//tl[0]=-1; tl[1]=-2; tl[2]=27; tl[3]=-2; tl[4]=39; tl[5]=-4; tl[6]=-5; tl[7]=-4; tl[8]=-1;
		//int[] sl=new int[10];
		//sl[0]=-1; sl[1]=-2; sl[2]=27; sl[3]=-2; sl[4]=38; sl[5]=-4; sl[6]=-5; sl[7]=-4; sl[8]=-6; sl[9]=-1;
		for (int ii=0; ii<files.length ; ii++){
			if (AAA[ii][0].isEmpty())
				continue;
			String sf = files[ii].substring(0, files[ii].indexOf("."));
			int sf_level = Integer.parseInt(AAA[ii][0]);
			int[] sl =readlist(xmldir.getPath()+ "/"+sf+".xml.txt");
			int sl_length = sl.length;
			double sl_par = Double.parseDouble(AAA[ii][2]);
			String filepair="";
			String langpair="";
			int dist_old=1000000, dist_new;
			for (int jj=ii+1; jj<files.length ; jj++){
				if (AAA[jj][0].isEmpty())
					continue;
				double tl_par = Double.parseDouble(AAA[jj][2]);
				if (!AAA[ii][1].equals(AAA[jj][1]) & Math.abs(sf_level-Integer.parseInt(AAA[jj][0]))<2 
						& (Math.abs(sl_par-tl_par)/Math.max(sl_par, tl_par))<pars_thres) {
					String tf = files[jj].substring(0, files[jj].indexOf("."));
					int[] tl =readlist(xmldir.getPath()+ "/"+tf+".xml.txt");
					int tl_length = tl.length;
					if ((Double.parseDouble(Integer.toString(Math.abs(sl_length-tl_length)))/
							Double.parseDouble(Integer.toString(Math.min(sl_length,tl_length)))<=length_thres)
							|| (Math.abs(sl_length-tl_length)<10)){
						//System.out.println(sf+ " with "+ tf);
						dist_new = editDist(sl,tl);
						if (dist_new<dist_old & dist_new<=Math.min(sl_length,tl_length)/2){
							filepair=sf+"_"+tf;
							langpair = AAA[ii][1]+"_"+AAA[jj][1];
							dist_old=dist_new;
							//System.out.println(sf+"_"+tf+" l1:"+sl_length+" l2:"+tl_length+
							//		" dist: "+dist_old+ " p1:"+sl_par+" p2:"+tl_par);
						}
					}
				}
			}
			if (!filepair.isEmpty()){
				//System.out.println(pair+"_"+dist_old);
				pairs.add(new String[] {filepair,langpair,Integer.toString(dist_old)});
			}
		}
		return pairs;
	}*/

	/*public static ArrayList<String[]> findBestPairs_old(ArrayList<String[]> pairs) {
		ArrayList<String[]> bitexts=new ArrayList<String[]>();
		Double[] new_temp1 = new Double[pairs.size()];
		String[][] pairlist = new String[pairs.size()][4];
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
		//
		//ArrayList<String[]> bitexts_temp=new ArrayList<String[]>();
		for (int i = 0; i < new_temp.length; i++){
			ArrayList<String> filesToRem=new ArrayList<String>();
			//System.out.print(new_temp[i]+">");
			for (int j = 0; j < pairlist.length; j++){
				if (!pairlist[j][0].isEmpty() & !pairlist[j][1].isEmpty()){
					if (new_temp[i].equals(editdist[j])){
						String f1 = pairlist[j][0];
						String f2 = pairlist[j][1];
						String l1 = pairlist[j][2];
						String l2 = pairlist[j][3];
						//System.out.print("FOUND: "+f1 +" and "+ f2+" | ");
						boolean found=false;
						for(int k=0; k<filesToRem.size();k++){
							if (f1.equals(filesToRem.get(k)) | f2.equals(filesToRem.get(k))){
								found=true;
								filesToRem.add(f1);
								filesToRem.add(f2);
								//System.out.print("REMOVE: "+f1 +" and "+ f2+" | ");
								break;
							}
						}
						if (!found){
							bitexts.add(new String[] {f1, f2,l1,l2});
							//System.out.print(f1+"_"+f2+ " | ");
							//System.out.print("REMOVE: "+f1 +" and "+ f2+" | ");
							filesToRem.add(f1);
							filesToRem.add(f2);
						}
					}
				}
			}
			for (int j = 0; j < pairlist.length; j++){
				for(int k=0; k<filesToRem.size();k++){
					if (pairlist[j][0].equals(filesToRem.get(k)) | pairlist[j][1].equals(filesToRem.get(k))){
						pairlist[j][0]="";
						pairlist[j][1]="";
					}
				}
			}
			//System.out.println();	
		}
		//System.out.println("END");
		return bitexts;
	}*/


	/*public static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
		f.read(buffer);
		return new String(buffer);
	}*/

	/*public static void runbitextor(File newdir) {
		try {
			copy( "config.xml", newdir.getParent()+"/config.xml");
			Process proc;
			String command = "/usr/local/bin/bitextor ";
			command += String.format("-d %s ",newdir.getPath());
			command += String.format("-l %s ",newdir.getParent()+"/logfile.log");
			command += String.format("-c %s ",newdir.getParent()+"/config.xml");
			System.out.println(command);
			//Run Bitextor
			try {
				proc = Runtime.getRuntime().exec(command);
				parseStreams(proc, false);
				proc.waitFor();

			} catch (IOException ex){
				System.err.println("Error while running bitextor, aborting...");
				System.exit(64);
			} catch (InterruptedException e) {
				System.err.println("Error while running bitextor, Interruption exception occured, aborting...");
				System.exit(64);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}*/

	/*public static void parseStreams(Process proc,Boolean display) throws IOException{
		String s;
		BufferedReader stdInput = new BufferedReader(new 
				InputStreamReader(proc.getInputStream()));
		BufferedReader stdError = new BufferedReader(new 
				InputStreamReader(proc.getErrorStream()));	     	    
		while ((s = stdInput.readLine()) != null) {
			if (display)
				System.err.println(s);
		}
		while ((s = stdError.readLine()) != null) {
			if (display)
				System.err.println(s);
		}
	} */

	/*public static File mirrorsite(String outputdir) {
		//File sitedir = new File(outputdir+"/xml");
		File sitedir = new File(outputdir);
		//FilenameFilter filter = new XMLFilter();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".xml"));
			}
		};
		String[] files= sitedir.list(filter);
		int numoffiles=files.length;		
		System.out.println("num of files: "+ numoffiles);
		File newdir= new File(sitedir.getPath()+ "/downloadedsite");
		if (numoffiles<1){
			System.err.println("Only " + numoffiles + "file has been downloaded");
			System.err.println("Abort");
			return null;
			//System.exit(0);
		}else {
			//Create a new directory called "downloadedsite" to put downloaded HTML
			//The directory should be in the temp directory
			newdir.mkdir();
		}
		int filecount=0;
		String url="", fileid="";
		for (int ii=0; ii<numoffiles ; ii++){
			filecount++;
			fileid=files[ii].substring(0, files[ii].lastIndexOf("."));
			url = extractURLfromXML(sitedir.getPath()+"/"+files[ii]);
			File struct=new File(newdir.getPath() + "/" + url.substring(url.indexOf("/"), url.lastIndexOf("/")));
			struct.mkdirs();
			//String sourcefile = sitedir.getPath()+"/"+fileid+".html";
			//String targetfile = struct.getPath()+"/"+fileid+".html";
			String sourcefile = sitedir.getPath()+"/"+fileid+".html";
			String targetfile = struct.getPath()+"/"+fileid+".html";
			try {
				copy(sourcefile, targetfile);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		return newdir;
	}*/

	/*public static String extractURLfromXML(String inputString) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputString);
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
	}*/

	/*public static void copy(String fromFileName, String toFileName)	throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			System.out.print("Overwrite existing file " + toFile.getName()
					+ "? (Y/N): ");
			System.out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String response = in.readLine();
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
				if (to != null)
					try {
						to.close();
					} catch (IOException e) {
						;
					}
		}
	}*/

	/*@SuppressWarnings("restriction")
	public static void writeXMLs(String f1,String f2, String l1, String l2) throws UnsupportedEncodingException, FileNotFoundException, XMLStreamException{
		String ff1 = f1.substring(f1.lastIndexOf("/")+1, f1.lastIndexOf("."));
		String ff2 = f2.substring(f2.lastIndexOf("/")+1, f2.lastIndexOf("."));
		String curXMLName=f1.substring(0, f1.lastIndexOf("/"))+"/"+ff1+"_"+ff2+".xml";

		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		OutputStreamWriter wrt = new OutputStreamWriter(new FileOutputStream(curXMLName),"UTF-8");
		XMLStreamWriter2 xtw = (XMLStreamWriter2) xof.createXMLStreamWriter(wrt);
		System.err.println("Attempting to write "+curXMLName);
		f1 = f1.replace(VAR_RES_CACHE,HTTP_PATH);
		f2 = f2.replace(VAR_RES_CACHE,HTTP_PATH);
		xtw.writeStartDocument();
		xtw.writeStartElement("cesAlign");
		xtw.writeAttribute("version", "1.0");
		//xtw.writeAttribute("xmlns", "http://www.xces.org/schema/2003");
		createHeader(xtw, f1, f2, l1, l2);
		xtw.writeEndDocument();
		xtw.flush();
		xtw.close();
	}*/



	/*@SuppressWarnings("restriction")
	public static String readElemInXML(String fn, String element) throws FileNotFoundException, XMLStreamException {
		String lang="", curElement="";
		int eventType=0;
		XMLInputFactory2 xmlif = null;
		try {
			xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
			xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,
					Boolean.FALSE);
			xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,
					Boolean.FALSE);
			xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
			xmlif.configureForSpeed();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.
		createXMLStreamReader(new FileInputStream(fn),"UTF-8");
		//System.err.println("Parsing :"+fn);
		System.out.println("Parsing :"+fn);
		//fn = fn.replace(VAR_RES_CACHE,HTTP_PATH);
		while (xmlr.hasNext()) {
			eventType = xmlr.next();
			if (eventType == XMLEvent2.START_ELEMENT){
				curElement = xmlr.getLocalName().toString();
				if (curElement.equals(LANGUAGE_ELE)) {
					lang=xmlr.getAttributeValue(0);
					break;
				}
			}
		}
		//vpapa
		xmlr.close();
		return lang;
	}*/




	/*public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	} */

	/*class XMLFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".xml"));
		}
	}*/

	//end of vpapa

	/*public static void findbitexts(File newdir, File outFile) {
		String logData="";
		try {
			logData = readFileAsString(newdir.getParent()+"/logfile.log");
		} catch (IOException e) {
			System.err.println("Error while reading log file, aborting...");
			System.exit(64);
		}
		String searchtext1 = "The bitext between ";
		String searchtext2a = " has been created>> Edit distance: ";
		String searchtext2 = "%.";
		String searchtext3 = " and ";
		String f1="", f2="";
		ArrayList<Double> editdist = new ArrayList<Double>();
		ArrayList<String[]> pairlist = new ArrayList<String[]>();
		int qnew1=0, qold1=0, qnew2=0, qold2=0, id=0, q3;
		String sent="", listoffiles="", l1, l2;
		double s1, s2, s;
		//parse Bitextor's logdata to find pairs and distances  
		while (qnew1>=qold1) {
			qold1=qnew1+1;
			qnew1=logData.substring(qold1+1, logData.length()).indexOf(searchtext1)+qold1;
			qold2=qnew2+1;
			qnew2=logData.substring(qold2, logData.length()).indexOf(searchtext2)+qold2;
			if (qnew1>qold1) {
				//If a pair has been found (e.g. consisting of <f1> and <f2>),get the language from the corresponding XMLs
				//create an XCES XML called <f1_f2>, add a line with that file to the output
				sent=logData.substring(qnew1, qnew2);
				sent=sent.replace(searchtext2a, "");
				q3=sent.indexOf(searchtext3);

				f1=sent.substring(searchtext1.length()+1, q3);
				//System.out.println(f1);
				f2=sent.substring(q3+searchtext3.length(), sent.lastIndexOf("l")+1);
				//System.out.println(f2);
				f1= f1.substring(f1.lastIndexOf("/")+1, f1.lastIndexOf("."));
				f2= f2.substring(f2.lastIndexOf("/")+1, f2.lastIndexOf("."));
				try {
					l1 = readElemInXML(newdir.getParent()+"/"+f1+appendXmlExt,LANGUAGE_ELE);
					l2 = readElemInXML(newdir.getParent()+"/"+f2+appendXmlExt, LANGUAGE_ELE);
					s1 = extractTermsfromXML(newdir.getParent()+"/"+f1+appendXmlExt).split(";").length;
					s2 = extractTermsfromXML(newdir.getParent()+"/"+f2+appendXmlExt).split(";").length;
					//double s=Math.abs(s1-s2)/Math.max(s1, s2);

					s=Math.abs(s1-s2)/Math.max(s1, s2);
					System.out.println("f1:"+f1 +" in "+ l1 + " AND f2:"+f2+" in "+l2+" dif: "+ s);
					if (!l1.equals(l2) & s<=term_thresh){
						pairlist.add( new String[] {f1, f2, l1 ,l2});
						editdist.add(Double.parseDouble(sent.substring(sent.lastIndexOf("l")+1)));
					}
				}catch (FileNotFoundException e) {
					System.err.println("Error while finding bitexts, Filenotfound exception, aborting...");
					System.exit(64);
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
		}
		//unique and sort distances
		Double[] new_temp1 = new Double[editdist.size()];
		for (int i = 0; i < new_temp1.length; i++)
			new_temp1[i] = editdist.get(i);
		Arrays.sort(new_temp1);
		int kk = 0;
		for (int i = 0; i < new_temp1.length; i++){
			if (i > 0 && new_temp1[i].equals(new_temp1[i -1]))
				continue;
			new_temp1[kk++] = new_temp1[i];
		}
		Double[] new_temp = new Double[kk];
		System.arraycopy(new_temp1, 0, new_temp, 0, kk);
		//find the best pairs
		ArrayList<String[]> pairs=new ArrayList<String[]>();
		for (int i = 0; i < new_temp.length; i++){
			ArrayList<String> filesToRem=new ArrayList<String>();
			//System.out.print(new_temp[i]+">");
			for (int j = 0; j < editdist.size(); j++){
				if (!pairlist.get(j)[0].isEmpty() & !pairlist.get(j)[1].isEmpty()){
					if (new_temp[i].equals(editdist.get(j))){
						f1 = pairlist.get(j)[0];
						f2 = pairlist.get(j)[1];
						l1 = pairlist.get(j)[2];
						l2 = pairlist.get(j)[3];
						//System.out.print("FOUND: "+f1 +" and "+ f2+" | ");
						boolean found=false;
						for(int k=0; k<filesToRem.size();k++){
							if (f1.equals(filesToRem.get(k)) | f2.equals(filesToRem.get(k))){
								found=true;
								filesToRem.add(f1);
								filesToRem.add(f2);
								//System.out.print("REMOVE: "+f1 +" and "+ f2+" | ");
								break;
							}
						}
						if (!found){
							pairs.add(new String[] {f1, f2, l1, l2});
							System.out.print(f1+"_"+f2+ " | ");
							//System.out.print("REMOVE: "+f1 +" and "+ f2+" | ");
							filesToRem.add(f1);
							filesToRem.add(f2);
						}
					}
				}
			}
			for (int j = 0; j < pairlist.size(); j++){
				for(int k=0; k<filesToRem.size();k++){
					if (pairlist.get(j)[0].equals(filesToRem.get(k)) | pairlist.get(j)[1].equals(filesToRem.get(k))){
						pairlist.get(j)[0]="";
						pairlist.get(j)[1]="";
					}
				}
			}
			//System.out.println();	
		}
		//create cesAligns
		String ff1, ff2;
		for (int i = 0; i < pairs.size(); i++){
			ff1= newdir.getParent()+"/"+pairs.get(i)[0]+appendXmlExt;
			ff2 = newdir.getParent()+"/"+pairs.get(i)[1]+appendXmlExt;
			l1 = pairs.get(i)[2];
			l2 = pairs.get(i)[3];
			try {
				writeXMLs(ff1, ff2, l1, l2);
			} catch (UnsupportedEncodingException e) {
				System.err.println("Error while creating cesAlign XMLs, UnsupportedEncoding exception, aborting...");
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				System.err.println("Error while creating cesAlign XMLs, Filenotfound exception, aborting...");
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			id++;
			listoffiles+=newdir.getParent()+"/" + pairs.get(i)[0]+"_"+pairs.get(i)[1]+appendXmlExt +"\n";
		}

		try {
			if (!listoffiles.isEmpty()) {
				listoffiles=listoffiles.replace(VAR_RES_CACHE, HTTP_PATH);
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile.getPath()),"UTF-8"));
				out.write(listoffiles);
				out.close();
				System.err.println(id + " pairs have been created.");
			}
			else {
				System.err.println("No pairs found!");
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile.getPath()),"UTF-8"));
				out.write(listoffiles);
				out.close();
			}
		}catch (IOException ex){
			System.err.println("Error while creating final output, aborting...");
			System.exit(64);
		}
	} */

	/*private static String extractTermsfromXML(String fn) {
		String res="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(fn);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName("p");

			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (NameElement.hasAttribute("topic")){
					//result+=NameElement.getTextContent();
					res=res+NameElement.getAttribute("topic")+";";
				//	break;
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}*/


	/*public static void findbitexts_old(File newdir, File outFile) {
		String logData="";
		try {
			logData = readFileAsString(newdir.getParent()+"/logfile.log");
		} catch (IOException e) {
			System.err.println("Error while reading log file, aborting...");
			System.exit(64);
		}
		System.out.println("Parse Bitextor's logdata");
		String searchtext1 = "The bitext between ";
		String searchtext2 = " has been created";
		String searchtext3 = " and ";
		String f1="", f2="";
		int qnew1=0, qold1=0, qnew2=0, qold2=0, id=0, q3;
		String sent="", listoffiles="";
		while (qnew1>=qold1) {
			qold1=qnew1+1;
			qnew1=logData.substring(qold1+1, logData.length()).indexOf(searchtext1)+qold1;
			qold2=qnew2+1;
			qnew2=logData.substring(qold2, logData.length()).indexOf(searchtext2)+qold2;
			if (qnew1>qold1) {
				//If a pair has been found (e.g. consisting of <f1> and <f2>),get the language from the corresponding XMLs
				//create an XCES XML called <f1_f2>, add a line with that file to the output
				id++;
				sent=logData.substring(qnew1, qnew2);
				q3=sent.indexOf(searchtext3);
				f1=sent.substring(searchtext1.length()+1, q3);
				//System.out.println(f1);
				f2=sent.substring(q3+searchtext3.length(), sent.length());
				//System.out.println(f2);
				f1= f1.substring(f1.lastIndexOf("/")+1, f1.lastIndexOf("."));
				f2= f2.substring(f2.lastIndexOf("/")+1, f2.lastIndexOf("."));
				//System.out.println(f1);
				//System.out.println(f2);
				//System.out.println(newdir.getParentFile().getParent()+"/" + f1+"_"+f2+appendXmlExt);
				listoffiles+=newdir.getParent()+"/" + f1+"_"+f2+appendXmlExt +"\n";

				String ff1 = newdir.getParent()+"/"+f1+appendXmlExt;
				String ff2 = newdir.getParent()+"/"+f2+appendXmlExt;
				System.out.println(ff1);
				System.out.println(ff2);			 
				try{
					String l1= readElemInXML(ff1,LANGUAGE_ELE);
					String l2=readElemInXML(ff2, LANGUAGE_ELE);
					if (!l1.equals(l2)){
						System.out.println(ff1+ " in " + l1 +" WITH "+ ff2 + " in "+l2);
						writeXMLs(ff1, ff2, l1, l2);
					}
				} catch (FileNotFoundException e) {
					System.err.println("Error while creating cesAlign XMLs, Filenotfound exception, aborting...");
					System.exit(64);
				} catch (UnsupportedEncodingException e) {
					System.err.println("Error while creating cesAlign XMLs, UnsupportedEncodingException aborting...");
					System.exit(64);
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			if (!listoffiles.isEmpty()) {
				listoffiles=listoffiles.replace(VAR_RES_CACHE, HTTP_PATH);
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile.getPath()),"UTF-8"));
				out.write(listoffiles);
				out.close();
				System.err.println(id + " pairs have been created.");
			}
			else {
				System.err.println("No pairs found!");
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile.getPath()),"UTF-8"));
				out.write(listoffiles);
				out.close();
			}
		}catch (IOException ex){
			System.err.println("Error while creating final output, aborting...");
			System.exit(64);
		}
	}	 
	 */

}
