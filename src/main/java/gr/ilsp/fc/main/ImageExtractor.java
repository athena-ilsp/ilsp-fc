package gr.ilsp.fc.main;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
//import org.codehaus.stax2.XMLInputFactory2;
//import org.codehaus.stax2.XMLStreamReader2;
//import org.codehaus.stax2.evt.XMLEvent2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class ImageExtractor {

	private static final String HREF = "href";
	private static final String SLASH = "/";
	private static final String PUNCT = ".";
	private static final String SRC = "src";
	private static final String IMG = "img";
	private static final String IFRAME = "iframe";
	private static final String htmlext = ".html";
	private static final String xmlext = ".xml";
	private static final int im_thr = 5;
	private static final Logger logger = Logger.getLogger(ImageExtractor.class.getName());
	// Matcher for some irrelevant images
	//private Matcher irrelevantImagesM = Pattern.compile(".*(icon|rss|twitter|facebook|share|save|youtube).*").matcher("");
	private Matcher irrelevantImagesM = Pattern.compile(".*(icon|rss|twitter|facebook|share|save).*").matcher("");

	/**
	 * Parses the HTML files of the xmldir directory and returns the imageNames that are in each file
	 * Boilerplate images (i.e. images that are in more than 5 files or occur in more than 10% of files) are excluded
	 * @param xmldir
	 * @param keep if true keeps the whole path of the image, otherwise only the filename
	 * @param minlangfiles 
	 * @return key is the id of the HTML file, and value is a list with imageNames
	 */
	public static HashMap<String, String[]> findImages(File xmldir, boolean keep, int thr){
		HashMap<String, String[]> init_result = new HashMap<String, String[]>();
		HashMap<String, String[]> result = new HashMap<String, String[]>();
		HashMap<String, Integer> all_images = new HashMap<String, Integer>();
		ImageExtractor ie = new ImageExtractor();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.length()>5 && arg1.substring(arg1.length()-5).equals(htmlext) & !arg1.contains(xmlext));
			}
		};
		String[] files= xmldir.list(filter);
		logger.info("image extraction");
		int counter=0, thous=0;
		try {
			String html, key;
			for (int ii=0; ii<files.length ; ii++){
				File htmlfile=new File(FilenameUtils.concat(xmldir.getAbsolutePath(),files[ii]));
				html = FileUtils.readFileToString(htmlfile);
				Document doc = Jsoup.parse(html);
				List<String> images = ie.getImages(doc,keep);
				if (!images.isEmpty()){
					List<String> uni_images = new ArrayList<String>();
					for (String s:images){
						if (!uni_images.contains(s)){
							uni_images.add(s);
						}
					}
					String[] strarray=new String[uni_images.size()];
					for (int jj=0;jj<uni_images.size();jj++){
						strarray[jj]=uni_images.get(jj);
						if (all_images.containsKey(uni_images.get(jj)))
							all_images.put(uni_images.get(jj), all_images.get(uni_images.get(jj))+1);
						else
							all_images.put(uni_images.get(jj), 1);
					}
					key=files[ii].substring(0, files[ii].indexOf(PUNCT));
					init_result.put(key,images.toArray(new String[images.size()]));
				}
				counter++;
				if ((counter/1000)>thous){
					thous++;
					logger.info("images from "+(thous*1000) + " files have been extracted");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> keys1=all_images.keySet();
		Iterator<String> imit = keys1.iterator();
		String temp_im="";
		
		ArrayList<String> boilerimages = new ArrayList<String>();
		//FIXME thr should be calculated/estimated by checking the frequencies
		//Integer thr = Math.max(files.length/10, im_thr);
		thr = Math.max(thr/10, im_thr);
		//if (thr>100) thr=100;
		logger.info( all_images.size()+ " images found.");
		logger.info("images that occur more than " + thr + " times are removed.");
		//Integer thr = Math.min(files.length/10, im_thr);
		while (imit.hasNext()){	
			temp_im = imit.next();
			if (all_images.get(temp_im)>=thr || all_images.get(temp_im)==1 || all_images.get(temp_im)==files.length){	
				boilerimages.add(temp_im);
				//System.out.println("DISCARD_IMAGE: "+ temp_im);
			}
		}
		logger.info( boilerimages.size()+ " images were removed.");
		keys1=init_result.keySet();
		imit = keys1.iterator();
		while (imit.hasNext()){	
			temp_im = imit.next();
			String[] temp=init_result.get(temp_im);
			List<String> new_temp=new ArrayList<String>();
			for (int ii=0;ii<temp.length;ii++){
				if (!boilerimages.contains(temp[ii]))
					new_temp.add(temp[ii]);
			}
			if (new_temp.isEmpty()){
				continue;
			}
			String[] temp1=new String[new_temp.size()];
			
			for (int ii=0;ii<new_temp.size();ii++){
				temp1[ii]=new_temp.get(ii);
			}
			result.put(temp_im, temp1);
		}
		return result;
	}


	// Prokopis: It would be interesting if this could accept document elements,
	// Prokopis: It would be interesting if this could accept document elements,
	// and thus is it could be fed with non-boilerplate chunks of html source
	/**
	 * returns imageNames of a Document
	 * @param doc
	 * @param keep if true keeps the whole path of the image, otherwise only the filename
	 * @return
	 * @throws IOException
	 */
	private  List<String> getImages(Document doc, boolean keep) throws IOException {
		List<String> images = new ArrayList<String>();
		Elements imgElements = doc.getElementsByTag(IMG);
		Elements iframeElements = doc.getElementsByTag(IFRAME);
		//Elements imgElementsHu = doc.select("a.dld");
		imgElements.addAll(iframeElements);
		//imgElements = imgElementsHu;
		String src = "";
		for (Element el : imgElements) {
			// get the src url
			if (el.hasAttr(SRC)) {
				src = el.attr(SRC);
			} else if (el.hasAttr(HREF)) {
				src = el.attr(HREF);
			}
			//if (!el.parent().tagName().equalsIgnoreCase("p")) {
			//	continue;
			//}
			if (src.isEmpty() ) continue;
			if (irrelevantImagesM.reset(src).matches()) {
				continue;
			}
			// Extract the name of the image from the src attribute
			int indexname = src.lastIndexOf(SLASH);
			if (indexname == src.length()) 
				src = src.substring(1, indexname);
			
			if (keep)
				images.add(src);
			else{
				indexname = src.lastIndexOf(SLASH);
				if (indexname>0)
					images.add(src.substring(indexname+1, src.length()));
				else
					images.add(src);
			}
		}
		return images;
	}

	
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
	
}
