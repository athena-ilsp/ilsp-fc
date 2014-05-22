package gr.ilsp.fmc.main;

import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
//import java.io.OutputStreamWriter;
import java.util.ArrayList;
//import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
//import org.codehaus.stax2.XMLInputFactory2;
//import org.codehaus.stax2.XMLStreamReader2;
//import org.codehaus.stax2.evt.XMLEvent2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class ImageExtractor {

	private static final String SLASH = "/";
	private static final String SRC = "src";
	private static final String IMG = "img";
	private static final Logger logger = Logger.getLogger(ImageExtractor.class.getName());
	private static String fs = System.getProperty("file.separator");
	//private static final String URL_ELE = "eAddress";
	// Matcher for some irrelevant images
	private Matcher irrelevantImagesM = Pattern.compile(".*(icon|rss|twitter|facebook|share|save|youtube).*").matcher("");

	public static HashMap<String, String[]> findImages(File xmldir, boolean keep){

		HashMap<String, String[]> result = new HashMap<String, String[]>();
		HashMap<String, Integer> all_images = new HashMap<String, Integer>();
		ImageExtractor ie = new ImageExtractor();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-5).equals(".html") & !arg1.contains(".xml"));
			}
		};
		String[] files= xmldir.list(filter);
		try {
			for (int ii=0; ii<files.length ; ii++){
				
				File htmlfile=new File(xmldir.toString()+fs+files[ii]);
				//System.out.println(ii+">"+htmlfile.getName());
				String html = ReadResources.readFileAsString(htmlfile.getAbsolutePath());
				Document doc = Jsoup.parse(html);
				List<String> images = ie.getImages(doc,keep);
				if (!images.isEmpty()){
					String[] strarray=new String[images.size()];
					for (int jj=0;jj<images.size();jj++){
						strarray[jj]=images.get(jj);
						if (all_images.containsKey(images.get(jj)))
							all_images.put(images.get(jj), all_images.get(images.get(jj))+1);
						else
							all_images.put(images.get(jj), 1);
					}
					String key=files[ii].substring(0, files[ii].indexOf("."));
					result.put(key,strarray);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> keys1=all_images.keySet();
		Iterator<String> imit = keys1.iterator();
		String temp_im="";
		//while (imit.hasNext()){	
		//	temp_im = imit.next();
			//System.out.println(temp_im+"\t"+ all_images.get(temp_im));
		//}
		//imit = keys1.iterator();
		ArrayList<String> boilerimages = new ArrayList<String>();
		Integer thr = files.length/10;
		while (imit.hasNext()){	
			temp_im = imit.next();
			//System.out.println(temp_im+"_"+all_images.get(temp_im) );
			if ((all_images.get(temp_im)>=thr || all_images.get(temp_im)>=10 || all_images.get(temp_im)==1) & thr>0){
			//if (all_images.get(temp_im)>=10 || all_images.get(temp_im)==1){
				boilerimages.add(temp_im);
				//System.out.println("DISCARD_IMAGE: "+ temp_im);
			}
				
		}
		keys1=result.keySet();
		imit = keys1.iterator();
		while (imit.hasNext()){	
			temp_im = imit.next();
			String temp[]=result.get(temp_im);
			ArrayList<String> new_temp=new ArrayList<String>();
			for (int ii=0;ii<temp.length;ii++){
				if (!boilerimages.contains(temp[ii]))
					new_temp.add(temp[ii]);
			}
			String final_temp[]=new String[new_temp.size()];
			//System.out.println(temp_im);
			for (int ii=0;ii<new_temp.size();ii++){
				final_temp[ii]=new_temp.get(ii);
				//System.out.println(final_temp[ii]);
			}
				
			result.put(temp_im, final_temp);
		}
		return result;
	}


	/*private static String getURLfromXML(String xmlfile) {
		String url="", curElement="";
		try {
			int eventType=0;
			XMLInputFactory2 xmlif = null;
			xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
			xmlif.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES,
					Boolean.FALSE);
			xmlif.setProperty(XMLInputFactory2.IS_SUPPORTING_EXTERNAL_ENTITIES,
					Boolean.FALSE);
			xmlif.setProperty(XMLInputFactory2.IS_COALESCING, Boolean.FALSE);
			xmlif.configureForSpeed();
			XMLStreamReader2 xmlr;
			xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(new FileInputStream(xmlfile),"UTF-8");
			while (xmlr.hasNext()) {
				eventType = xmlr.next();
				if (eventType == XMLEvent2.START_ELEMENT){
					curElement = xmlr.getLocalName().toString();
					if (curElement.equals(URL_ELE)){
						if (xmlr.getAttributeCount()<1){
							url=xmlr.getElementText();
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return url;
	}
	 */









	public static void main(String[] args) {
		try {
			ImageExtractor ie = new ImageExtractor();

			String webSiteURL1 = 
					"http://setimes.com/cocoon/setimes/xhtml/el/features/setimes/features/2012/06/28/feature-01";
			String webSiteURL2 = 
					"http://setimes.com/cocoon/setimes/xhtml/en_GB/features/setimes/features/2012/06/28/feature-01";

			for (String webSiteURL: new String[] {webSiteURL1, 
					webSiteURL2}) {
				// Connect to the website and get the html
				Document doc = Jsoup.connect(webSiteURL).get();

				logger.info("Images for " + webSiteURL);

				List<String> images = ie.getImages(doc,true);
				for (String image : images) {
					logger.info(image);
				}
				System.out.println();
			}
		} catch (IOException ex) {
			logger.warn("There was an error");
			logger.warn(ex.getMessage());
		}
	}

	// Prokopis: It would be interesting if this could accept document elements,
	// and thus is it could be fed with non-boilerplate chunks of html source

	private  List<String> getImages(Document doc, boolean keep) throws IOException {
		List<String> images = new ArrayList<String>();
		Elements imgElements = doc.getElementsByTag(IMG);
		for (Element el : imgElements) {
			// get the src url
			//String src = el.absUrl(SRC);
			String src = el.attr(SRC);
			//System.out.println(src);
			if (src.isEmpty()) continue;
			if (irrelevantImagesM.reset(src).matches()) {
				continue;
			}
			// Extract the name of the image from the src attribute
			int indexname = src.lastIndexOf(SLASH);
			if (indexname == src.length()) {
				src = src.substring(1, indexname);
				//images.add(src);
				//continue;
			}
			//if (!images.contains(src)){
				if (keep)
					images.add(src);
				else{
					indexname = src.lastIndexOf(SLASH);
					if (indexname>0)
						images.add(src.substring(indexname, src.length()));
					else
						images.add(src);
				}
			//}
		}
		return images;
	}

}





/*for (int ii=0; ii<files.length ; ii++){
	System.out.println(ii);
	//webSiteURL=getURLfromXML(xmldir.getPath()+"/"+files[ii]);
	//Document doc;
	//doc = Jsoup.connect(webSiteURL).get();
	//logger.info("Images for " + webSiteURL);
	//System.out.println(files[ii]);
	File htmlfile = new File(xmldir.toString()+fs+files[ii]);
	String html = Bitexts.readFileAsString(htmlfile.getAbsolutePath());
	Document doc = Jsoup.parse(html);
	html="";
	htmlfile=null;
	//List<String> images = ie.getImages(doc,keep);
	//ArrayList<String> uniqueimages = new ArrayList<String>();
	List<String> uniqueimages = ie.getImages(doc,keep);
	doc=null;
	if (!uniqueimages.isEmpty()){
		//String[] strarray = new String[images.size()];
		//for (int jj=0;jj<images.size();jj++)
		//	strarray[jj]=images.get(jj);
		//Arrays.sort(strarray);
		//images = null;
		//uniqueimages.add(strarray[0]);
		//for (int kk=1;kk<strarray.length;kk++){ 
		//		if (!strarray[kk].equals(strarray[kk-1]))
		//			uniqueimages.add(strarray[kk]);
		//}
		//strarray = null;
		String[] strarray = new String[uniqueimages.size()];
		for (int jj=0;jj<uniqueimages.size();jj++)
			strarray[jj]=uniqueimages.get(jj);
		uniqueimages=null;
		for (int jj=0;jj<strarray.length;jj++){
			if (all_images.containsKey(strarray[jj]))
				all_images.put(strarray[jj], all_images.get(strarray[jj])+1);
			else
				all_images.put(strarray[jj], 1);
		}
		uniqueimages=null;
		String key=files[ii].substring(0, files[ii].indexOf("."));
		result.put(key,strarray);
		strarray=null;
	} 
	//System.out.println();
}



*/