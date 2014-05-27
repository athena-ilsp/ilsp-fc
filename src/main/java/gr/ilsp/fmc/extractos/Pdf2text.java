package gr.ilsp.fmc.extractos;


import gr.ilsp.fmc.utils.ContentNormalizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;


public class Pdf2text {
	private static final Logger LOGGER = Logger.getLogger(Pdf2text.class);
	//private static String fs = System.getProperty("file.separator");
	private static ArrayList<PrintTextLocations.CharAttr> chardata=new ArrayList<PrintTextLocations.CharAttr>();

	private static ArrayList<LineAttr> linedata = new ArrayList<LineAttr>();
	private static ArrayList<SectionAttr> sectiondata = new ArrayList<SectionAttr>();

	private static HashMap<String,ArrayList<LineAttr>> docprops=new HashMap<String,ArrayList<LineAttr>>();

	//private static double minimum_space_width = 1;
	//private static double minimum_space_height = 6; //two successive chars in the same text line
	//should differ less than this threshold in vertical coordinates 
	private static double fontsize_thr = 0.6;	 		//threshold for Chain Map Representation It should be at least 0.5
	//private static double std_thr=0.1;
	//private static double align_thr=0.3;
	private static double align_thr_fully = 0.6;
	private static double caps_thr = 0.75;
	//private static int window=15;
	//private static double w1=2* (double) window;
	//private static double w2= (double) window / 2;
	//private static ArrayList<String> lastchars=new ArrayList<String>(Arrays.asList(".", "!", "?", ";")); 

	public static void main( String[] args ) throws IOException	{
		String path=args[0];
		String files;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 
		for (int i = 0; i < listOfFiles.length; i++){
			if (listOfFiles[i].isFile()) {
				files = listOfFiles[i].getAbsolutePath();
				if (files.endsWith(".pdf") || files.endsWith(".PDF")){
					File input = new File(files);
					String filename=input.getName();
					LOGGER.info("---------------------------------------FILE:"+ filename);
					docprops.clear();
				}
			}
		}		
	}

	@SuppressWarnings("unchecked")
	public static String run1(File input) { //throws IOException {
		String content="";
		PDDocument document = null;
		try {
			document = PDDocument.load(input);
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (InvalidPasswordException e) {
					System.err.println("Error: Document is encrypted with a password.");
				} catch (CryptographyException e) {
					System.err.println("Error: CryptographyException.");
				}
			}
			PrintTextLocations printer = new PrintTextLocations();
			List<PDPage> allPages = document.getDocumentCatalog().getAllPages();
			float pageheight=0, pagewidth=0;
			for (int i = 0; i < allPages.size(); i++) {
				PDPage page =  allPages.get(i);
				if (page.getMediaBox()!=null){
					pageheight = page.getMediaBox().getHeight();
					pagewidth = page.getMediaBox().getWidth();
					LOGGER.debug("Processing page: " + i+" with height " +pageheight 
							+ " and width "+ pagewidth);
				}
				else {/*FIXME since pageheight is unknown, it cannot be used for footer/header detection*/
					LOGGER.error("PROBLEM in getMediaBox");
				}
				chardata.clear();
				linedata.clear();
				sectiondata.clear();
				PDStream contents = page.getContents(); 
				ArrayList<LineAttr> current_linedata = new ArrayList<LineAttr>();
				if (contents != null) {
					printer.processStream(page, page.findResources(), page.getContents().getStream());
					chardata=printer.getchardata();
					if (chardata.size()<1){
						docprops.put("page"+i, current_linedata);
						continue;
					}
					layout_analysis(pageheight);
				}
				for (int jj=0;jj<linedata.size();jj++){
					current_linedata.add(linedata.get(jj));
					current_linedata.get(jj).chars=Utils.normalizeContent(current_linedata.get(jj).chars);
					LOGGER.debug(current_linedata.get(jj).p+"\t"+
							current_linedata.get(jj).t+"\t"+
							current_linedata.get(jj).chars);
				}
				docprops.put("page"+i, current_linedata);
				content = content+getAllText(docprops);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return content;
	}


	public static String getText(HashMap<String, ArrayList<LineAttr>> docprops2) {
		String temp_int, temp, content="";
		boolean found, found1;

		for (int ii=0;ii<docprops.size();ii++){	
			temp_int="page"+ii;
			if  (!docprops.containsKey(temp_int))
				continue;
			if (docprops.get(temp_int).size()==1){
				if (docprops.get(temp_int).get(0).t!=-2 & docprops.get(temp_int).get(0).t!=-3 
						& docprops.get(temp_int).get(0).t!=-1){ 
					content = content+docprops.get(temp_int).get(0).chars+"</text>\n<text>";
				}
				else{
					if (docprops.get(temp_int).get(0).t!=-2)
						content = content+"<boiler>"+docprops.get(temp_int).get(0).chars+"</boiler>\n";
					if (docprops.get(temp_int).get(0).t!=-3)
						content = content+"<boiler>"+docprops.get(temp_int).get(0).chars+"</boiler>\n";
					continue;
				}
				continue;
			}	
			found1=false;
			for (int jj=1;jj<docprops.get(temp_int).size();jj++){	
				found=false ;
				if ((docprops.get(temp_int).get(jj).p).equals(docprops.get(temp_int).get(jj-1).p)){
					temp=docprops.get(temp_int).get(jj-1).chars.trim();
					if (temp.endsWith("-")){
						temp=temp.substring(0, temp.lastIndexOf("-"));
						found = true;
					}
					if (docprops.get(temp_int).get(jj-1).t!=-2 & docprops.get(temp_int).get(jj-1).t!=-3){
						if (found1)
							content = content + "<bolier>"+temp; //out.write(temp);
						else
							if (content.isEmpty()){
								content = content +""+ temp; //out.write(" "+temp);
							}else{
								content = content +" "+ temp; //out.write(" "+temp);
							}
						if (found)
							found1 = true;
						else
							found1 = false;
					}else
						continue;
				}
				else{
					temp=docprops.get(temp_int).get(jj-1).chars.trim();
					if (temp.endsWith("-")){
						temp=temp.substring(0, temp.lastIndexOf("-"));
						found=true;
					}
					if (docprops.get(temp_int).get(jj-1).t!=-2 & docprops.get(temp_int).get(jj-1).t!=-3){
						if (found1)
							content = content + temp+"</text>\n<text>";  //out.write(temp+"\n");
						else
							content = content + " " + temp+"</text>\n<text>";  //out.write(" "+temp+"\n");
						if (found)
							found1=true;
						else
							found1=false;
					}else
						continue;
				}
				if (jj==docprops.get(temp_int).size()-1 
						& docprops.get(temp_int).get(jj).t!=-2 & docprops.get(temp_int).get(jj).t!=-3){
					temp= docprops.get(temp_int).get(jj).chars.trim();
					if (temp.endsWith("-")){
						temp=temp.substring(0, temp.lastIndexOf("-"));
						found=true;
					}
					if (found1)
						content = content + temp+"</text>\n<text>";   //out.write(temp+"\n");
					else
						content = content + " "+temp+"</text>\n<text>";  //out.write(" "+temp+"\n");
					if (found)
						found1=true;
					else
						found1=false;
				}
			}
		}
		return content;
	}


	public static String getAllText(HashMap<String, ArrayList<LineAttr>> docprops2) {
		String temp_int, temp;
		String content="";
		//boolean found=true; //true denotes that i a <text> element is opened.
		boolean found1=false; //true denotes that i the textline ends with "-".

		for (int ii=0;ii<docprops.size();ii++){	
			temp_int="page"+ii;
			if  (!docprops.containsKey(temp_int))
				continue;
			if (docprops.get(temp_int).size()==1){
				content = "<text>"+docprops.get(temp_int).get(0).chars+"</text>";
				continue;
			}else{
				temp = docprops.get(temp_int).get(0).chars.trim();
				if (temp.endsWith("-")){
					temp=temp.substring(0, temp.lastIndexOf("-")).trim();
					found1=true;
				}
				//content="<text>"+ temp;
				content="<text>";
			}
			for (int jj=1;jj<docprops.get(temp_int).size();jj++){
				//System.out.println("1:\t"+docprops.get(temp_int).get(jj-1).p + "\t" +docprops.get(temp_int).get(jj-1).chars.trim());
				//System.out.println("2:\t"+docprops.get(temp_int).get(jj).p + "\t" +docprops.get(temp_int).get(jj).chars.trim());
				//System.out.println(content);
				if ((docprops.get(temp_int).get(jj).p).equals(docprops.get(temp_int).get(jj-1).p)){
					if (found1){
						content = content + temp;
						//found1=false;
					}else{
						content = content + temp + " " ;
					}
				}else{
					content=content+temp+"</text>\n<text>";
					/*if (found1){
						content=content+temp+"</text>\n<text>";
						//found1=false;
					}else{
						content=content+temp+"</text>\n<text>";
					}*/
				}		
				temp=docprops.get(temp_int).get(jj).chars.trim();
				if (temp.endsWith("-")){
					temp=temp.substring(0, temp.lastIndexOf("-")).trim();
					found1=true;
				}else{
					found1=false;
				}
				//System.out.println(temp);
		
				if (jj==docprops.get(temp_int).size()-1){
					
					temp= docprops.get(temp_int).get(jj).chars.trim();
					if (temp.endsWith("-")){
						temp=temp.substring(0, temp.lastIndexOf("-"));
						//found=true;
					}
					content = content + temp+"</text>\n";   //out.write(temp+"\n");
					//content = content + " "+temp+"</text>\n<text>";  //out.write(" "+temp+"\n");
				}
			}
		}
		return content;
	}



	private static int layout_analysis(float pageheight) {
		LOGGER.debug("PUT CHARACTERS INTO TEXT-LINES. Just checks the y-coordinates of successive characters.");
		boolean sort_sections=false;
		int linecounter =put_chars_in_textlines(pageheight);
		if (linecounter==0)
			return 0;	

		ArrayList<LineAttr> linedata_temp= new ArrayList<LineAttr>();	

		ArrayList<Double> uniquefontsizes=findFontSizes();
		//FIXME REMOVE TEXTLINES WITH FONTSIZES <0.5
		boolean usefonts=true;
		if (uniquefontsizes.size()<2){
			usefonts=false;
			for (int ii=0;ii<chardata.size();ii++){
				chardata.get(ii).fs=Math.round(chardata.get(ii).h);
				chardata.get(ii).h=Math.round(chardata.get(ii).h);
			}
			LOGGER.debug("Analysis will be based on estimated font sizes (i.e. categorization based on text-line heights)");
		}else
			LOGGER.debug("Analysis will be based on provided font sizes");

		estimate_space_thr_per_line(linecounter,usefonts);

		if (usefonts)
			represent_textline_fonts();
		else
			represent_textline_heights();

		float x1, x2, y1, y2, x11, x22, y11, y22;
		ArrayList<Integer> indeces= new ArrayList<Integer>();
		for (int ii=0;ii<linedata.size()-1;ii++){
			x1=linedata.get(ii).x; y1=linedata.get(ii).y;
			x2 = x1+linedata.get(ii).w;
			y2=  y1+linedata.get(ii).h;
			for (int jj=ii+1; jj<linedata.size();jj++){
				x11 = linedata.get(jj).x; y11=linedata.get(jj).y;
				x22 = x11+linedata.get(jj).w;
				y22 = y11+linedata.get(jj).h;
				if (!indeces.contains(ii) & !indeces.contains(jj))
					if (x11>=x1 & y11>=y1 & x22<=x2 & y22<=y2){
						if (linedata.get(jj).chars.length()<=linedata.get(ii).chars.length()){
							indeces.add(jj); 
						}
					}
				if (x1>=x11 & y1>=y11 & x2<=x22 & y2<=y22){
					if (linedata.get(ii).chars.length()<=linedata.get(jj).chars.length()){
						indeces.add(ii);
					}
				}
			}
		}
		//normalization of each text line and removal of lines that contains only digits
		for (int ii=0;ii<linedata.size();ii++){
			linedata.get(ii).chars=ContentNormalizer.normalizeText(linedata.get(ii).chars);
		}
		for (int ii=0;ii<linedata.size();ii++){
			String temp=linedata.get(ii).chars.replace(" ", "");
			if (linedata.get(ii).fs>fontsize_thr & linedata.get(ii).h>fontsize_thr 
					& !indeces.contains(ii)
					& !LineTypeGuesser.isDigitsOnlyLine(temp)){
				linedata_temp.add(linedata.get(ii));
			}
		}

		linedata=linedata_temp;
		LOGGER.debug("LINES: "+linedata.size());

		//System.out.println("CATEGORIZE TEXTLINES By FontSize, based on a simplification of Chain Map Representation");
		//maximin();//update type of text-lines in linedata based on fontsize 

		ArrayList<Double> uniquefontheights=findFontHeights();
		if (uniquefontheights.size()==1)
			return 0;
		if (usefonts)
			categorizePerFontsize(uniquefontsizes);
		else
			categorizePerFontsize(uniquefontheights);
		//categorizePerFontheight(uniquefontheights);

		linedata_temp = linedata;

		createSections(); //based on the height of each text-line 

		ArrayList<SectionAttr> sectiondata1 = new ArrayList<SectionAttr>();
		int par_count;
		for (int ii=0;ii<sectiondata.size();ii++){
			if (sectiondata.get(ii).num>1){
				segmentSectionsByDistance(sectiondata.get(ii),ii, usefonts);
				par_count=Integer.parseInt(linedata.get(sectiondata.get(ii).el).p.split("_")[1]);
				if (par_count==0)
					sectiondata1.add(sectiondata.get(ii));
				else{
					for (int jj=0; jj<par_count+1; jj++){
						SectionAttr new_temp_section=new SectionAttr(sectiondata.get(ii).chars,sectiondata.get(ii).x, sectiondata.get(ii).y,
								sectiondata.get(ii).fs, sectiondata.get(ii).h, sectiondata.get(ii).w,
								sectiondata.get(ii).p,sectiondata.get(ii).t,sectiondata.get(ii).num,
								sectiondata.get(ii).sl,sectiondata.get(ii).el);
						String temp_par="";
						int par_length=0, sl=-1, el=-1;
						for (int kk=new_temp_section.sl;kk<new_temp_section.el+1;kk++){
							if (Integer.parseInt(linedata.get(kk).p.split("_")[1])==jj){
								temp_par=temp_par+"\n"+linedata.get(kk).chars;
								if (sl==-1)
									sl=kk;
								el=kk;
								par_length++;
							}
						}
						new_temp_section.chars=temp_par;
						new_temp_section.sl=sl;
						new_temp_section.el=el;
						new_temp_section.num=par_length;
						sectiondata1.add(new_temp_section);
					}
				}
			}else{
				for (int jj=0;jj<sectiondata.get(ii).num;jj++)
					linedata.get(sectiondata.get(ii).sl+jj).p=ii+"_"+0;

				sectiondata1.add(sectiondata.get(ii));
			}
		}

		//linedata_temp = linedata;	
		sectiondata.clear();
		sectiondata = sectiondata1;

		for (int ii=0;ii<sectiondata.size();ii++){
			if (sectiondata.get(ii).num>2)
				segmentSectionsByAlignement1(sectiondata.get(ii),ii, usefonts);
			else{
				for (int jj=0;jj<sectiondata.get(ii).num;jj++)
					linedata.get(sectiondata.get(ii).sl+jj).p=ii+"_"+0;
			}
		}
		//updates the coordinates of the sections
		for (int ii=0;ii<sectiondata.size();ii++){
			float[] st_topdist = new float[sectiondata.get(ii).num];
			float[] st_leftdist = new float[sectiondata.get(ii).num];
			float[] en_topdist = new float[sectiondata.get(ii).num];
			float[] en_leftdist = new float[sectiondata.get(ii).num];
			for (int jj=0;jj<sectiondata.get(ii).num;jj++){
				st_topdist[jj] = linedata.get(sectiondata.get(ii).sl+jj).y;
				st_leftdist[jj] = linedata.get(sectiondata.get(ii).sl+jj).x;
				en_topdist[jj] = st_topdist[jj] + linedata.get(sectiondata.get(ii).sl+jj).h;
				en_leftdist[jj] = linedata.get(sectiondata.get(ii).sl+jj).w;
			}
			Arrays.sort(st_topdist); Arrays.sort(st_leftdist);
			Arrays.sort(en_topdist); Arrays.sort(en_leftdist);
			sectiondata.get(ii).y = Math.round(st_topdist[0]-1);
			sectiondata.get(ii).x = Math.round(st_leftdist[0]-1);
			sectiondata.get(ii).h = Math.round(en_topdist[en_topdist.length-1]+1) - Math.round(st_topdist[0]-1);
			sectiondata.get(ii).w = Math.round(en_leftdist[en_leftdist.length-1]+1);
		}
		//check_page_template();
		for (int ii=0;ii<sectiondata.size();ii++){
			LOGGER.debug("TYPE:\t"+sectiondata.get(ii).t);
			LOGGER.debug(sectiondata.get(ii).chars+"\n--------");
		}
		if (sort_sections)
			put_sections_in_order(pageheight);
		
		//detect_header_footer(pageheight);
		linedata_temp = linedata;
		return 1;
	}

	private static void put_sections_in_order(float pageheight) {//sort by distance from the top
		ArrayList<SectionAttr> sectiondata_res = new ArrayList<SectionAttr>();
		ArrayList<Integer> selected = new ArrayList<Integer>();
		ArrayList<SectionAttr> sectiondata_temp = new ArrayList<SectionAttr>();

		for (int ii=0;ii<sectiondata.size();ii++){
			if (sectiondata.get(ii).y>0.9*pageheight){
				selected.add(ii); 
				sectiondata.get(ii).t=-2; // footer
				sectiondata_temp.add(sectiondata.get(ii));
				LOGGER.debug("FOOTER: "+sectiondata.get(ii).chars);
			}else{
				if ((sectiondata.get(ii).y+sectiondata.get(ii).h)<0.1*pageheight){
					selected.add(ii); 
					sectiondata.get(ii).t=-1; //header
					sectiondata_temp.add(sectiondata.get(ii));
					LOGGER.debug("HEADER: "+sectiondata.get(ii).chars);
				}else
					LOGGER.debug("TEXT: "+sectiondata.get(ii).chars);
			}
		}
		int factor=10;
		sectiondata_res=analysis(sectiondata, selected, null, factor);
		for (int ii=0;ii<sectiondata_res.size();ii++)
			sectiondata_temp.add(sectiondata_res.get(ii));

		sectiondata_res.clear();
		for (int ii=0;ii<sectiondata.size();ii++){
			if (!sectiondata_temp.contains(sectiondata.get(ii)))
				sectiondata_res.add(sectiondata.get(ii));
		}
		selected.clear();
		for (int ii=1;ii<31;ii++){
			if (sectiondata_temp.size()<1){
				LOGGER.debug("No text extracted from this page");
				break;
			}
			if (sectiondata_res.size()>0){
				SectionAttr lastselected = sectiondata_temp.get(sectiondata_temp.size()-1);
				//ArrayList<SectionAttr> sectiondata_new_res=analysis(sectiondata_res, selected,lastselected, factor);
				sectiondata_res=analysis(sectiondata_res, selected,lastselected, factor);
				if (sectiondata_res.isEmpty())
					factor=2*factor;
				//else
				//	sectiondata_res=sectiondata_new_res;

				for (int jj=0;jj<sectiondata_res.size();jj++)
					sectiondata_temp.add(sectiondata_res.get(jj));

				sectiondata_res.clear();
				for (int jj=0;jj<sectiondata.size();jj++){
					if (!sectiondata_temp.contains(sectiondata.get(jj)))
						sectiondata_res.add(sectiondata.get(jj));
				}
				selected.clear();
			}
			else
				break;
		}

		sectiondata.clear();
		for (int ii=0;ii<sectiondata_temp.size();ii++){
			sectiondata.add(sectiondata_temp.get(ii));
		}
		ArrayList<LineAttr> linedata_temp =new ArrayList<LineAttr>();
		for (int ii=0;ii<sectiondata.size();ii++){
			for (int jj=sectiondata.get(ii).sl;jj<=sectiondata.get(ii).el;jj++){
				if (sectiondata.get(ii).t<0){
					linedata.get(jj).t=sectiondata.get(ii).t;
					//System.out.println(sectiondata.get(ii).chars);
				}
				linedata_temp.add(linedata.get(jj));
			}
		}
		linedata=linedata_temp;
	}

	private static ArrayList<SectionAttr> analysis(	ArrayList<SectionAttr> sectiondata2, ArrayList<Integer> selected2,
			SectionAttr last_selected, int factor) {
		ArrayList<SectionAttr> sectiondata_res = new ArrayList<SectionAttr>();
		ArrayList<Integer> not_selected = new ArrayList<Integer>();
		float[] dist_from_top = new float[sectiondata2.size()];
		for (int ii=0;ii<sectiondata2.size();ii++){
			dist_from_top[ii]=sectiondata2.get(ii).y;
		}
		Arrays.sort(dist_from_top);

		for (int ii=0;ii<dist_from_top.length;ii++){
			for (int jj=0;jj<sectiondata2.size();jj++){
				if (sectiondata2.get(jj).y==dist_from_top[ii] & !selected2.contains(jj)){
					int y_st = (int) (sectiondata2.get(jj).y - 5 * sectiondata2.get(jj).fs);
					/*- sectiondata2.get(jj).h/sectiondata2.get(jj).num);*/
					int y_en = (int) (sectiondata2.get(jj).y + sectiondata2.get(jj).h+ 5 * sectiondata2.get(jj).fs);
					/*+sectiondata2.get(jj).h/sectiondata2.get(jj).num);*/
					int x_st = (int) (sectiondata2.get(jj).x) ;
					int x_en = (int) (sectiondata2.get(jj).w) ;
					boolean found1 = false, found2 = false;
					for (int kk=0;kk<sectiondata2.size();kk++){ 
						if ( kk!=jj 
								& (sectiondata2.get(kk).w-x_st<0)
								& (Math.min(sectiondata2.get(kk).y+sectiondata2.get(kk).h, y_en)
										-Math.max(sectiondata2.get(kk).y, y_st)>=0)
										& sectiondata2.get(kk).x< sectiondata2.get(jj).x  
								) {
							found1=true;
							//System.out.print("HORIZONTAL OVERLAP with : ");
							//System.out.println(sectiondata2.get(kk).chars);
							not_selected.add(jj);
							//break;
						}
					}
					if (found1)
						continue; 
					x_st = (int) (sectiondata2.get(jj).x);
					x_en = (int) (sectiondata2.get(jj).w);
					for (int kk=0;kk<sectiondata2.size();kk++){
						if (kk!=jj 
								/*& sectiondata2.get(kk).x>=x_st & sectiondata2.get(kk).x<=x_en*/
								& (Math.max(x_st, sectiondata2.get(kk).x)-Math.min(x_en, sectiondata2.get(kk).w)<0)
								& not_selected.contains(kk)) {
							found2=true;
							//System.out.print("VERTICAL OVERLAP with : ");
							//System.out.println(sectiondata2.get(kk).chars);
							not_selected.add(jj);
							break;
						}
					}

					if (!found1 & !found2 & !not_selected.contains(jj)){
						float xx;
						if (sectiondata_res.size()>0)
							xx = sectiondata_res.get(sectiondata_res.size()-1).w;
						else{
							if (last_selected!=null)
								xx=last_selected.w;
							else
								xx=sectiondata2.get(jj).x;		
						}
						//System.out.println(xx);
						//System.out.println(sectiondata2.get(jj).x);
						//System.out.println(sectiondata2.get(jj).fs);
						//System.out.println(sectiondata2.get(jj).x - xx);

						//if (sectiondata2.get(jj).x - xx > 5 * sectiondata2.get(jj).fs){
						if (sectiondata2.get(jj).x - xx > factor * sectiondata2.get(jj).fs){


						}else{
							sectiondata_res.add(sectiondata2.get(jj));
							//System.out.print("ADDED : ");
							//System.out.println(sectiondata2.get(jj).chars);
							selected2.add(jj);
							break;
						}
					}
				}
			}
		}
		return sectiondata_res;
	}

	private static void segmentSectionsByAlignement1(SectionAttr sectionAttr, int sectioncounter, boolean usefonts) {
		ArrayList<Integer> pars=new ArrayList<Integer>();
		double[] x_st=new double[sectionAttr.num];
		double[] x_en=new double[sectionAttr.num];
		int caps=0;
		int[] cap=new int[sectionAttr.num];
		for (int ii=sectionAttr.sl;ii<sectionAttr.el+1;ii++){
			x_st[ii-sectionAttr.sl] = Math.floor(linedata.get(ii).x);
			x_en[ii-sectionAttr.sl] = Math.floor(linedata.get(ii).w)-x_st[ii-sectionAttr.sl];
			//System.out.println(linedata.get(ii).chars.trim());
			if (Character.isUpperCase(linedata.get(ii).chars.trim().charAt(0)) 
					& !linedata.get(ii).chars.trim().toUpperCase().equals(linedata.get(ii).chars.trim()))	{
				caps++; cap[ii-sectionAttr.sl]=1;
			}
		}
		double std_x_st = Utils.getStdDev(x_st);
		double std_x_en = Utils.getStdDev(x_en);
		double max_width = Utils.getMax(x_en);
		double[] cl = Utils.find_most_commonValue(x_st);
		double[] cr = Utils.find_most_commonValue(x_en);
		double mr = Utils.getMax(x_en);
		if (cr[1]==-1); cr[0]=max_width;
		int type=0;
		if (((cl[1]/x_st.length)>=align_thr_fully)& ((cr[1]/x_en.length)>=align_thr_fully)){
			//System.out.println("Section is fully justified");
			type=1;
		}else{
			if (((cl[1]/x_st.length)>=align_thr_fully) /*& ((cr[1]/x_en.length)<align_thr)*/){
				//System.out.println("Section is left justified");
				type=2;
			}else{
				if ((/*(cl[1]/x_st.length)<align_thr) & */(cr[1]/x_en.length)>=align_thr_fully)){
					//System.out.println("Section is right justified");
					type=3;
				}else{
					//System.out.println("Section is center justified");
					type=4;
				}
			}
		}
		if (type==1){//fully justified
			for (int ii=sectionAttr.sl;ii<sectionAttr.el+1;ii++){
				if (x_st[ii-sectionAttr.sl]>cl[0]+std_x_st & !pars.contains(ii-sectionAttr.sl)){
					pars.add(ii-sectionAttr.sl);
				}
				if ( x_en[ii-sectionAttr.sl]<cr[0]-2*sectionAttr.fs & !pars.contains(ii-sectionAttr.sl+1)){
					pars.add(ii-sectionAttr.sl+1);
				}
			}
		}
		if (type==2){//left justified
			for (int ii=sectionAttr.sl;ii<sectionAttr.el+1;ii++){
				if (x_st[ii-sectionAttr.sl]>cl[0]+std_x_st){
					pars.add(ii-sectionAttr.sl);
				}
			}
			for (int ii=1;ii<x_en.length;ii++){
				String templine=linedata.get(sectionAttr.sl+ii).chars.trim();
				String templine1=linedata.get(sectionAttr.sl+ii-1).chars.trim();
				String ch=templine.substring(0, 1);
				String ch1 = templine1.substring(templine1.length()-1, templine1.length());

				if ( (Character.isUpperCase(templine.charAt(0)) | ch.matches("\\d.*"))
						& (	(( ch1.matches("\\d.*") | ch1.matches(":") | ch1.matches(";") | ch1.matches("\\."))
								& ( x_en[ii-1] < mr-std_x_en /*| cr[1]==-1*/))
								| ( x_en[ii-1] < mr/2 /*| cr[1]==-1 */))
								& (!pars.contains(ii))){
					pars.add(ii);
				}
				//if ((ch1.matches(":")| ch1.matches(";"))
				//		& (!pars.contains(ii)))	
				//	pars.add(ii);
			}
		}
		if (type==3){//right justified
			for (int ii=sectionAttr.sl;ii<sectionAttr.el+1;ii++){
				if (x_en[ii-sectionAttr.sl]<cr[0]-std_x_en){
					pars.add(ii-sectionAttr.sl+1);
				}
			}
			for (int ii=1;ii<x_en.length;ii++){
				//String templine=linedata.get(sectionAttr.sl+ii).chars.trim();
				String templine1=linedata.get(sectionAttr.sl+ii-1).chars.trim();
				//String ch=templine.substring(0, 1);
				String ch1 = templine1.substring(templine1.length()-1, templine1.length());
				/*if ((Character.isUpperCase(templine.charAt(0)) | ch.matches("\\d.*"))		
						&( (ch1.matches("\\d.*") | ch1.matches(":")| ch1.matches(";")))
						//		| (x_st[ii-1]>=cl[0]-std_x_st)| cl[1]==-1))
						& (!pars.contains(ii)))*/
				if ((ch1.matches(":")| ch1.matches(";"))
						& (!pars.contains(ii)))	
					pars.add(ii);	
			}
		}
		for (int ii=0;ii<x_en.length;ii++){
			String templine=linedata.get(sectionAttr.sl+ii).chars.trim();
			String ch=templine.substring(0, 1);
			if (( ch.matches("●") | ch.matches("•"))
					& !pars.contains(ii))
				pars.add(ii);
		}
		/*for (int ii=1;ii<x_en.length;ii++){
			String templine=linedata.get(sectionAttr.sl+ii).chars.trim();
			String templine1=linedata.get(sectionAttr.sl+ii-1).chars.trim();
			String ch=templine.substring(0, 1);
			String ch1 = templine1.substring(templine1.length()-1, templine1.length());
			if ((Character.isUpperCase(templine.charAt(0)) | ch.matches("\\d.*"))		
					& (ch1.matches("\\d.*") | ch1.matches(":")| ch1.matches(";"))
					& (!pars.contains(ii)))
				pars.add(ii);
		}*/
		if (( (float)caps / (float)sectionAttr.num)>caps_thr){
			for (int ii=1;ii<x_en.length;ii++){
				if (cap[ii]==1 & (!pars.contains(ii)))
					pars.add(ii);
			}
		}
		if (pars.isEmpty()){
			for (int ii=0;ii<x_en.length;ii++)
				linedata.get(ii+sectionAttr.sl).p=sectioncounter+"_"+0;
		}else{
			int[] parsB=Utils.sortArrayList(pars);
			for (int ii=0;ii<x_en.length;ii++){
				for (int jj=0; jj<parsB.length;jj++){
					if (ii+sectionAttr.sl<linedata.size()){
						if (ii<parsB[jj]){
							linedata.get(ii+sectionAttr.sl).p=sectioncounter+"_"+(jj);
							break;
						}else
							linedata.get(ii+sectionAttr.sl).p=sectioncounter+"_"+(jj+1);
					}
				}
			}
		}
	}

	private static void createSections() {
		//Step 1. Grouping based on text line height (t attribute of lines has already be filled)
		int counter=0, counter1=1, start_line=0;
		SectionAttr t=new SectionAttr(linedata.get(0).chars,linedata.get(0).x, linedata.get(0).y,
				linedata.get(0).fs, linedata.get(0).h, linedata.get(0).w,
				"sec_0",linedata.get(0).t,counter1,start_line,start_line);
		sectiondata.add(t);
		ArrayList<LineAttr> linedata_temp =new ArrayList<LineAttr>();
		ArrayList<LineAttr> linedata_preview =new ArrayList<LineAttr>();
		for (int ii=0;ii<linedata.size();ii++){
			linedata_preview.add(linedata.get(ii));
		}
		for (int ii=1;ii<linedata.size();ii++){
			//System.out.println(linedata.get(ii-1).chars);
			//System.out.println(linedata.get(ii).chars);
			if	( (linedata.get(ii).t==linedata.get(ii-1).t) &
					//( (Math.abs(linedata.get(ii).y-linedata.get(ii-1).y-linedata.get(ii-1).h)<2*linedata.get(ii-1).h) |
					// (Math.abs(linedata.get(ii).x-linedata.get(ii-1).x-linedata.get(ii-1).w)<4*linedata.get(ii-1).h) 	
					//)
					(Math.abs(linedata.get(ii).y-linedata.get(ii-1).y-linedata.get(ii-1).h)<4*linedata.get(ii-1).h) &
					(vert_overlap(linedata.get(ii-1),linedata.get(ii)))){
				counter1++;
				t = new SectionAttr(sectiondata.get(counter).chars+"\n"+linedata.get(ii).chars,
						Math.min(sectiondata.get(counter).x, linedata.get(ii).x),
						Math.min(sectiondata.get(counter).y, linedata.get(ii).y),
						linedata.get(ii).fs,
						sectiondata.get(counter).h + linedata.get(ii).h,
						Math.max(linedata.get(ii-1).w, linedata.get(ii).w),
						sectiondata.get(counter).p,linedata.get(ii).t,
						counter1,start_line,ii);
				sectiondata.set(counter,t);
			}else{
				counter1=1;
				start_line=ii;
				counter++;
				t = new SectionAttr(linedata.get(ii).chars,linedata.get(ii).x,linedata.get(ii).y,
						linedata.get(ii).fs, linedata.get(ii).h,linedata.get(ii).w,
						"sec_"+Integer.toString(counter),linedata.get(ii).t,counter1, start_line,start_line);
				sectiondata.add(t);
			}
		}
		ArrayList<SectionAttr> sectiondata_temp = new ArrayList<SectionAttr>();

		//Step 2. In each group the text lines are sorted 
		ArrayList<SectionAttr> sectiondata_preview = new ArrayList<SectionAttr>();
		for (int ii=0;ii<sectiondata.size();ii++){
			sectiondata_preview.add(sectiondata.get(ii));
		}
		for (int ii=0;ii<sectiondata.size();ii++){
			if (sectiondata.get(ii).num==1){
				t = sectiondata.get(ii);
				sectiondata_temp.add(t);
				linedata_temp.add(linedata.get(sectiondata.get(ii).sl));
				continue;
			}
			float[] dist_from_top = new float[sectiondata.get(ii).num];
			float[] dist_from_left = new float[sectiondata.get(ii).num];
			for (int jj=0;jj<sectiondata.get(ii).num;jj++){
				dist_from_top[jj]=linedata.get(sectiondata.get(ii).sl+jj).y;
				dist_from_left[jj]=linedata.get(sectiondata.get(ii).sl+jj).x;
			}
			Arrays.sort(dist_from_top);
			Arrays.sort(dist_from_left);
			ArrayList<Integer> selected= new ArrayList<Integer>();
			String section_chars="";
			for (int kk=0;kk<dist_from_top.length;kk++){
				for (int ll=0;ll<dist_from_left.length;ll++){
					for (int jj=0;jj<sectiondata.get(ii).num;jj++){
						if (!selected.contains(jj)){
							float temp_y=linedata.get(sectiondata.get(ii).sl+jj).y;
							float temp_x=linedata.get(sectiondata.get(ii).sl+jj).x;
							if (temp_y==dist_from_top[kk] & temp_x==dist_from_left[ll] ){
								selected.add(jj);
								section_chars = section_chars+linedata.get(sectiondata.get(ii).sl+jj).chars+"\n";
								linedata_temp.add(linedata.get(sectiondata.get(ii).sl+jj));
								break;
							}
						}
					}
				}
			}
			sectiondata_temp.add(sectiondata.get(ii));
			sectiondata_temp.get(ii).chars=section_chars.substring(0, section_chars.length()-1);
		}
		sectiondata.clear();
		for (int ii=0;ii<sectiondata_temp.size();ii++){
			sectiondata.add(sectiondata_temp.get(ii));
			LOGGER.debug(sectiondata.get(ii).chars);
			LOGGER.debug("-------------");
		}
		linedata.clear();
		for (int ii=0;ii<linedata_temp.size();ii++)
			linedata.add(linedata_temp.get(ii));

		for (int ii=0;ii<sectiondata.size();ii++){
			sectiondata.get(ii).x=Math.round(sectiondata.get(ii).x);
			sectiondata.get(ii).y=Math.round(sectiondata.get(ii).y+1);
		}

	}

	private static boolean vert_overlap(LineAttr lineAttr1, LineAttr lineAttr2) {
		float st1 = lineAttr1.x; float st2 = lineAttr2.x;
		float en1 = lineAttr1.w; float en2 = lineAttr2.w;
		if ((Math.min(en1, en2)-Math.max(st1, st2))>0)
			return true;
		else
			return false;
	}

	private static void segmentSectionsByDistance(SectionAttr sectionAttr,
			int sectioncounter, boolean usefonts) {
		ArrayList<Double> distances = new ArrayList<Double>();
		//int[] heights = new int[sectionAttr.num-1];
		float real_height_1=0;
		for (int jj=sectionAttr.sl;jj<sectionAttr.el;jj++){
			if (usefonts){
				real_height_1 = linedata.get(jj).fs;
			}
			else{
				real_height_1 = linedata.get(jj).fs; //real_height_1 = linedata.get(jj).h;
			}
			//FIXME Most common Y-COORD of chars in a text-line should be taken into account. 
			distances.add((double) Math.round((Math.abs(linedata.get(jj).y +
					real_height_1 -linedata.get(jj+1).y)*10))/10);
		}
		int count=0;
		int[] indeces=new int[sectionAttr.num];

		if (distances.size()>1){
			double com_dist;
			if (distances.size()>3){
				double[] dist=new double[distances.size()];
				for (int ii=0;ii<dist.length;ii++){
					dist[ii] = distances.get(ii);
				}
				com_dist=Utils.find_most_commonValue(dist)[0];
			}else{
				com_dist=-1;
			}

			for (int ii=0;ii<distances.size();ii++){
				if (com_dist>0){
					if (distances.get(ii)>=2.715*com_dist){
						count++;
						indeces[ii]=1;
					}
				}else{
					if (ii>0){
						if (distances.get(ii)>1.5*sectionAttr.fs & distances.get(ii)>1.5*distances.get(ii-1)){	
							count++;
							indeces[ii]=1;
						}
					}
				}
			}
		}
		//if (distances.size()==1){
		//}

		int pars=0;
		if (count!=0 ){//& count!=sectionAttr.el-sectionAttr.sl){
			for (int jj=sectionAttr.sl;jj<sectionAttr.el;jj++){
				//if (linedata.get(jj).p.isEmpty())
				linedata.get(jj).p=sectioncounter+"_"+pars;
				//System.out.println(linedata.get(jj).chars+"\t"+linedata.get(jj).p);
				//if ((distances.get(jj-sectionAttr.sl)-thr)*((distances.get(jj-sectionAttr.sl+1)-thr))<0 & count>0){
				//if ((distances.get(jj-sectionAttr.sl)-thr)>0 & count>0){	
				if (indeces[jj-sectionAttr.sl]==1 & count>0){	
					//linedata.get(jj+1).p=sectioncounter+"_"+pars;
					pars++;
					count=count-1;
				}
			}
			for (int jj=sectionAttr.sl;jj<sectionAttr.el+1;jj++){
				if (linedata.get(jj).p.isEmpty())
					linedata.get(jj).p=sectioncounter+"_"+pars;
			}
		}else{
			for (int jj=sectionAttr.sl;jj<sectionAttr.el+1;jj++)
				linedata.get(jj).p=sectioncounter+"_"+pars;
		}
	}

	private static ArrayList<Double> findFontHeights() {

		ArrayList<Double> uniquefontheights=new ArrayList<Double>();
		double[] fontsizes=new double[linedata.size()+1];
		fontsizes[linedata.size()]=100000;
		for (int ii=0;ii<linedata.size();ii++)
			fontsizes[ii]=linedata.get(ii).fs; //fontsizes[ii]=linedata.get(ii).h; 
		Arrays.sort(fontsizes);
		double temp_font=0;
		for (int ii=0;ii<fontsizes.length;ii++){
			if (Math.abs(fontsizes[ii]-temp_font)>fontsize_thr){
				uniquefontheights.add(fontsizes[ii]);
				temp_font=fontsizes[ii];
			}
		}
		return uniquefontheights;
	}

	private static void represent_textline_heights() {
		String temp="";
		float xst = -1, yst = 1000000000, fsst = 0, xsst = 0,  sst = 0, yen=0;//,width=0; hst = 0,
		//ArrayList<LineAttr> linedata_temp =new ArrayList<LineAttr>();
		ArrayList<Float> fsstList=new ArrayList<Float>();

		for (int ii=1;ii<chardata.size();ii++){
			if (chardata.get(ii).character.length()>1){
				if ( !chardata.get(ii).character.contains("f")){
					chardata.get(ii).character=" ";
				}
			}
			if (chardata.get(ii).p!=chardata.get(ii-1).p | ii==chardata.size()-1  ){	
				temp=temp+chardata.get(ii-1).character;
				fsstList.add(chardata.get(ii-1).h);
				double[] fsstArray=new double[fsstList.size()];
				for (int kk=0;kk<fsstList.size();kk++)
					fsstArray[kk]=fsstList.get(kk);
				Arrays.sort(fsstArray);

				if (temp.trim().length()>0){
					LineAttr t = new LineAttr(temp,
							xst, 
							Math.min(yst, chardata.get(ii-1).y),
							(float) Utils.getMedian(fsstArray),
							(xsst+chardata.get(ii-1).xs)/(temp.length()),
							yen-yst,//(hst+chardata.get(ii-1).h)/(temp.length()) ,
							(sst+chardata.get(ii-1).s)/(temp.length()) ,  
							chardata.get(ii-1).x+chardata.get(ii-1).w,"",-3);
					linedata.add(t);
				}
				temp=""; 
				xst=-1; yst=1000000000; fsst=0; xsst=0;  sst=0; yen=0; //hst=0;
				fsstList.clear();
			}else{
				temp = temp + chardata.get(ii-1).character;
				//yst = Math.min(yst,chardata.get(ii-1).y);
				if (Math.abs(chardata.get(ii-1).h-chardata.get(ii).h)<=fontsize_thr){
					yst = Math.min(yst,chardata.get(ii-1).y);
					yen = Math.max(yen,yst + chardata.get(ii-1).h);
				}
				fsstList.add(chardata.get(ii-1).h);
				fsst = fsst + chardata.get(ii-1).h;
				xsst = xsst + chardata.get(ii-1).xs;
				sst = sst + chardata.get(ii-1).s;
				if (xst<0)
					xst=chardata.get(ii-1).x;
			}
		}
	}

	private static void represent_textline_fonts() {
		String temp="",temp1;
		float xst = -1, yst = 1000000000, fsst = 0, xsst = 0,  sst = 0, yen=0;

		for (int ii=1;ii<chardata.size();ii++){
			if ((chardata.get(ii).p!=chardata.get(ii-1).p | ii==chardata.size()-1 ) ){
				temp=temp+chardata.get(ii-1).character;
				LineAttr t = new LineAttr(temp,
						xst, 
						Math.min(yst, chardata.get(ii-1).y),//(yst+chardata.get(ii-1).y)/(temp.length()),
						fsst, 
						(xsst+chardata.get(ii-1).xs)/(temp.length()),
						yen-yst,//(hst+chardata.get(ii-1).h)/(temp.length()) ,
						(sst+chardata.get(ii-1).s)/(temp.length()) ,  
						chardata.get(ii-1).x+chardata.get(ii-1).w,"",-3);
				temp1=t.chars.trim();
				if (temp1.length()>0){
					linedata.add(t);		//linedata_temp = linedata;
				}	
				temp=""; 
				xst=-1; yst=1000000000; fsst=0; xsst=0; sst=0; yen=0;  //hst=0; 
			}else{
				temp = temp + chardata.get(ii-1).character;
				//yst = Math.min(yst,chardata.get(ii-1).y);
				if (Math.abs(chardata.get(ii-1).fs-chardata.get(ii).fs)<=fontsize_thr){
					yst = Math.min(yst,chardata.get(ii-1).y);
					yen = Math.max(yen,chardata.get(ii-1).y + chardata.get(ii-1).h);
				}
				fsst = Math.max(fsst,chardata.get(ii-1).fs);
				xsst = xsst + chardata.get(ii-1).xs;
				sst = sst + chardata.get(ii-1).s;
				if (xst<0){
					xst=chardata.get(ii-1).x;
				}
			}
		}
	}

	private static int put_chars_in_textlines(float pageheight) {
		float x1, x2, y1, y2, h1, h2, w1, y3, h3;
		int linecounter=0;
		String character;
		ArrayList<PrintTextLocations.CharAttr> chardata_temp=new ArrayList<PrintTextLocations.CharAttr>();
		ArrayList<Float> ycoord= new ArrayList<Float>();
		//if h is negative the text is inverted. If s is negative, the text is written from right to left.
		//In these cases the text is removed.
		for (int ii=0;ii<chardata.size();ii++){
			if (chardata.get(ii).h>0 | chardata.get(ii).s>0){
				chardata_temp.add(chardata.get(ii));
				ycoord.add(chardata.get(ii).y);
			}
			else
				LOGGER.debug("Discard:"+ chardata.get(ii).character);
		}

		float[] ycoord_a = new float[ycoord.size()];
		for (int i = 0; i < ycoord_a.length; i++)
			ycoord_a[i] =ycoord.get(i);
		Arrays.sort(ycoord_a);
		//String orientation="";
		if (ycoord_a[ycoord_a.length-1]>pageheight)
			LOGGER.debug("probably the orientation of text in page is vertical.");			//orientation="v";
		else
			LOGGER.debug("probably the orientation of text in page is horizontal.");	//orientation="h";

		chardata.clear();
		chardata=chardata_temp;
		chardata.add(chardata.get(chardata.size()-1));//repeat the last char
		chardata.get(0).p=linecounter;//0;
		float previous_line_vertical_end=-10000000;

		for (int ii=1;ii<chardata.size();ii++){
			character = chardata.get(ii).character;
			character = ContentNormalizer.normalizeText(character);

			boolean found=false;
			if (character.equals(" ") 
					& (chardata.get(ii-1).character.equals("fi")
							| chardata.get(ii-1).character.equals("ff") 
							| chardata.get(ii-1).character.equals("fl")) ){
				found=true;
			}
			if (!found & (character.equals("") | character.equals(" "))){
				chardata.get(ii).character=character;
				chardata.get(ii).p=chardata.get(ii-1).p;			chardata.get(ii).x=chardata.get(ii-1).x;
				chardata.get(ii).y=chardata.get(ii-1).y;			chardata.get(ii).h=chardata.get(ii-1).h;
				chardata.get(ii).w=chardata.get(ii-1).w;			chardata.get(ii).fs=chardata.get(ii-1).fs;
				chardata.get(ii).xs=chardata.get(ii-1).xs;			chardata.get(ii).s=chardata.get(ii-1).s;
				//continue;
			}

			if (chardata.get(ii).w==0 | found/*|character.equals("") | character.equals(" ")*/)	{	
				//System.out.println("OOPS!!!");
				chardata.get(ii).character="";
				chardata.get(ii).p=chardata.get(ii-1).p;
				chardata.get(ii).x=chardata.get(ii-1).x;
				chardata.get(ii).y=chardata.get(ii-1).y;
				chardata.get(ii).h=chardata.get(ii-1).h;
				chardata.get(ii).w=chardata.get(ii-1).w;
				chardata.get(ii).fs=chardata.get(ii-1).fs;
				chardata.get(ii).xs=chardata.get(ii-1).xs;
				chardata.get(ii).s=chardata.get(ii-1).s;
				continue;
			}
			y1=chardata.get(ii-1).y; 
			y2=chardata.get(ii).y;    
			x1=chardata.get(ii-1).x;  
			x2=chardata.get(ii).x;  
			h1=chardata.get(ii-1).h;
			h2=chardata.get(ii).h;
			w1=chardata.get(ii-1).w;
			if (ii>1){
				y3=chardata.get(ii-2).y;
				h3=chardata.get(ii-2).h;
			}else{
				y3=1000000; h3=10000000;
			}
			if ( ( ( (y2>=y1 & y2<=y1+h1) |   (y2+h2>=y1 & Math.round(y2+h2)<=Math.round(y1+h1))
					| (Math.min(y1+h1, y2+h2)- Math.max(y1, y2)>=0) | ((Math.min(y3+h3, y2+h2)- Math.max(y2, y3)>=0)))
					&
					(x2>x1-fontsize_thr & x2<=x1+7*w1+fontsize_thr)
					) 
					| character.equals("|") | character.equals("©")){	
				//if two successive chars are horizontally overlapped
				if (x2<x1){	//if two successive chars are not in the normal order
					//(probably interpretation inserted an extra character, i.e "copyright-> c & copyright)
					// and so this char should be removed
					//System.out.println("Problem: char "+ chardata.get(ii-1).character+ " should be removed.");
					chardata.get(ii-1).character="";
					chardata.get(ii-1).x=chardata.get(ii).x;
					chardata.get(ii-1).y=chardata.get(ii).y;
					chardata.get(ii-1).h=chardata.get(ii).h;
					chardata.get(ii-1).w=chardata.get(ii).w;
					chardata.get(ii-1).fs=chardata.get(ii).fs;
					chardata.get(ii-1).xs=chardata.get(ii).xs;
					chardata.get(ii-1).s=chardata.get(ii).s;
				}
				chardata.get(ii).p=chardata.get(ii-1).p;
			}else{
				if (y2<y1)
					previous_line_vertical_end=-1000000;
				else
					previous_line_vertical_end=y1+h1;

				linecounter++;
				chardata.get(ii).p=linecounter; //chardata.get(ii-1).p+1;
			}
		}
		return linecounter;
	}


	private static void categorizePerFontsize(ArrayList<Double> uniquefontsizes) {

		for (int ii=0;ii<linedata.size();ii++){
			for (int jj=0;jj<uniquefontsizes.size();jj++){
				if (Math.abs(linedata.get(ii).fs-uniquefontsizes.get(jj))<fontsize_thr){
					linedata.get(ii).t=jj;
					break;
				}
			}
		}
	}


	private static ArrayList<Double> findFontSizes() {
		ArrayList<Double> uniquefontsizes=new ArrayList<Double>();
		HashSet<Double> fonts=new HashSet<Double>();
		for (int ii=0;ii<chardata.size();ii++){
			//System.out.println(chardata.get(ii).character + "\t"+ chardata.get(ii).fs);
			if (!fonts.contains((double) chardata.get(ii).fs))
				fonts.add((double) Math.round(chardata.get(ii).fs));
		}
		Double[] fontsizes=new Double[fonts.size()+1];
		fontsizes[fonts.size()]=(double) 100000;
		Iterator<Double> fontkey=fonts.iterator();
		int counter=0;
		while (fontkey.hasNext()){
			fontsizes[counter]= fontkey.next();
			counter++;
		}
		//for (int ii=0;ii<chardata.size();ii++)
		//	fontsizes[ii]=chardata.get(ii).fs;

		//ArrayList<Double> uniquefontsizes=new ArrayList<Double>();
		//double[] fontsizes=new double[chardata.size()+1];
		//fontsizes[chardata.size()]=100000;
		//for (int ii=0;ii<chardata.size();ii++)
		//	fontsizes[ii]=chardata.get(ii).fs;
		Arrays.sort(fontsizes);
		for (int ii=1;ii<fontsizes.length;ii++){
			if (Math.abs(fontsizes[ii]-fontsizes[ii-1])>fontsize_thr)
				uniquefontsizes.add(fontsizes[ii-1]);
		}
		return uniquefontsizes;
	}


	public static class LineAttr {
		public String chars; //text
		public float x;		 //starting horizontal coordinate
		public float y;		 //starting vertical coordinate
		public float fs;	 //fontsize
		public float xs;	 //xspace
		public float h;		 //height
		public float s;		 //space
		public float w;		 //width
		public String p;	 //numbered id
		public int t;		 //logicalLayoutType based on fontsize


		public LineAttr(String chars, float x, 
				float y, float fs, float xs, float h, float s, float w, String p, int t) {
			this.chars = chars;
			this.x = x;
			this.y = y;
			this.fs = fs;
			this.xs = xs;
			this.h = h;
			this.s = s;
			this.w = w;
			this.p=p;
			this.t=t;
		}
	}


	public static class SectionAttr {
		public String chars; //text
		public float x;		 //starting horizontal coordinate
		public float y;		 //starting vertical coordinate
		public float fs;	 //fontsize
		public float h;		 //height
		public float w;		 //width
		public String p;	 //geometricalLayoutType_id i.e. p1 for the first paragraph or sec1 for the first section
		public int t;		 //llogicalLayoutType based on fontsize (to be: 0 for title, 1 for body etc)
		public int num;		//num of textlines
		public int sl;	 //first textline
		public int el;	 //last textline							 

		public SectionAttr(String chars, float x, 
				float y, float fs, float h, float w, String p, int t, int num , int sl, int el) {
			this.chars = chars;
			this.x = x;
			this.y = y;
			this.fs = fs;
			this.h = h;
			this.w = w;			
			this.p=p;
			this.t=t;
			this.num=num;
			this.sl = sl;
			this.el = el;

		}
	}


	public static class ParagraphAttr {
		public String chars; //text
		public float x;		 //starting horizontal coordinate
		public float y;		 //starting vertical coordinate
		public float fs;	 //fontsize
		//public float xs;	 //xspace
		public float h;		 //height
		//public float s;		 //space
		public float w;		 //width
		public String p;	 //geometricalLayoutType_id i.e. p1 for the first paragraph or sec1 for the first section
		public int t;		 //logicalLayoutType based on fontsize (to be: 0 for title, 1 for body etc)
		public int num;		//num of textlines



		public ParagraphAttr(String chars, float x, 
				float y, float fs, float h,  float w, String p, int t, int num) {
			this.chars = chars;
			this.x = x;
			this.y = y;
			this.fs = fs;
			//this.xs = xs;
			this.h = h;
			//this.s = s;
			this.w = w;
			this.p=p;
			this.t=t;
			this.num=num;
		}
	}

	private static void estimate_space_thr_per_line(int linecounter, boolean usefonts) {
		float x1, x2, w1;
		//collect candidate places for white spaces per text-line
		float[] thr_spaces_per_line=new float[linecounter+1];
		linecounter=0;
		//System.out.println("LINE:"+linecounter);
		ArrayList<Double> candidate_spaces=new ArrayList<Double>();
		String temp="";
		for (int ii=1;ii<chardata.size();ii++){
			x1=chardata.get(ii-1).x;  
			x2=chardata.get(ii).x;   
			w1=chardata.get(ii-1).w; 
			//if (chardata.get(ii).character.equals(" "))
			//	continue;
			if (chardata.get(ii-1).character.equals("") )//removed character
				continue;

			if (chardata.get(ii).p==chardata.get(ii-1).p){//if two successive characters are in the same text-line
				//if (chardata.get(ii).p==16){
				//	System.out.print(chardata.get(ii-1).character+chardata.get(ii).character);
				//}
				temp=temp+chardata.get(ii-1).character;
				if((x2-x1-w1)>0){//if there is a gap between them 
					//candidate_spaces.add(Math.l.log(x2-x1-w1));
					candidate_spaces.add((double) (x2-x1-w1)); //consider this gap as a candidate place for whitespace
				}
			}else{//estimate a threshold that separates candidate spaces to real and not real.
				//System.out.println();
				if (usefonts)
					thr_spaces_per_line[linecounter]=(float) Math.min((float) Utils.otsu(candidate_spaces),
							chardata.get(ii-1).fs/5);
				else
					thr_spaces_per_line[linecounter]=(float) Utils.otsu(candidate_spaces);	
				linecounter++;
				//System.out.println("LINE:"+linecounter);
				candidate_spaces.clear();
				temp="";
			}
		}
		//based on the estimated threshold per text-line, required white spaces are added.
		//System.out.println("BASED ON THE ESTIMATED THRESHOLD PER TEXT-LINE, REQUIRED WHITESPACES ARE ADDED.");
		for (int ii=1;ii<chardata.size();ii++){
			x1=chardata.get(ii-1).x;  
			x2=chardata.get(ii).x;    	
			w1=chardata.get(ii-1).w;  
			if (chardata.get(ii).p==chardata.get(ii-1).p){//if two successive characters are in the same text-line
				if (thr_spaces_per_line[chardata.get(ii-1).p]>0){
					if((x2-x1-w1)>=thr_spaces_per_line[chardata.get(ii-1).p]){
						//System.out.println(chardata.get(ii-1).character+"\t"+chardata.get(ii).character);
						chardata.get(ii-1).character=chardata.get(ii-1).character+" ";
					}
				}
			}
		}
	}

}
