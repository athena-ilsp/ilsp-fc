package gr.ilsp.fc.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;

/**
 * Utils class for reading hreflang attributes from already crawled files.
 * 
 * @author prokopis
 *
 */
public class HrefLangUtils {

	private static ILSPFCUrlNormalizer ilspfcUrlNormalizer = new ILSPFCUrlNormalizer();
	private static final Logger logger = LoggerFactory.getLogger(HrefLangUtils.class);
	
	public static void main(String[] args) throws IOException {
		File file2UrlLangFile = new File(args[0]);
		File htmlDir = new File(args[1]).getAbsoluteFile();
		File hreflangList = new File(args[2]);
		String baseURI = "http://" + htmlDir.getName().replace(".lett/", "/").replace(".lett", "/");

		logger.info("Reading url2file map");
		Map<String, String> url2FileMap = getUrl2FileMap(file2UrlLangFile);
		logger.info("Creating l12l map from " + htmlDir);
		Map<String, String> l1l2FileMap = getl1l2FileMap(htmlDir, baseURI, url2FileMap,  "en", "fr");
		logger.info("Writing hreflang list to " + hreflangList);
		writeL1L2FileMap2File(l1l2FileMap, hreflangList);
		logger.info("Done");
	}

	public static Map<String, String> getUrl2FileMap(File fileUrlLang) throws IOException {
		Map<String, String> url2FileMap =  new HashMap<String, String>();
		List<String> fileUrlLangLines = FileUtils.readLines(fileUrlLang);
		for (String line: fileUrlLangLines) {
			String[] fields = StringUtils.split(line);
			logger.debug(fields[0] + " " + fields[1] + " " + fields[2]);
			populateUrlFileMap(url2FileMap, fields[0], fields[1]);
		}
		return url2FileMap;
	}

	public static Map<String, String> getl1l2FileMap(File htmlDir, String baseURI, Map<String, String> url2FileMap, String l1, String l2) throws IOException {
		Map<String, String> tempL1l2FileMap = getl1l2FileMap(htmlDir, baseURI, url2FileMap) ;
		Map<String, String> l1l2FileMap =  new HashMap<String, String>();
		TreeSet<String> treeSet = new TreeSet<String>( tempL1l2FileMap.keySet());
		for (String key : treeSet) {
			String value = tempL1l2FileMap.get(key);
			if (key.startsWith(l1) && value.startsWith(l2)) {
				l1l2FileMap.put(key, value);
			} else if (key.startsWith(l2) && value.startsWith(l1)) {
				l1l2FileMap.put(key, value);
			}
		}
		return l1l2FileMap;
	}
	
	
	public static Map<String, String> getl1l2FileMap(File htmlDir, String baseURI, Map<String, String> url2FileMap) throws IOException {
		Map<String, String> l1l2FileMap =  new HashMap<String, String>();
		Collection<File> htmlFiles = FileUtils.listFiles(htmlDir, new WildcardFileFilter("*html"), null);
		for (File htmlFile : htmlFiles) {
			//slogger.info(htmlFile.getAbsolutePath());
			getHrefLangs(url2FileMap, l1l2FileMap, htmlFile, baseURI);
		}
		return l1l2FileMap;
	}

	public static void populateUrlFileMap(Map<String, String> urlFileMap,  String file, String url) {
		//logger.info(file);
		//System.exit(0);
		url = ilspfcUrlNormalizer.normalize(url);
		urlFileMap.put(url, file); 
		urlFileMap.put(StringUtils.replacePattern(url, "\\.php$", "\\.html"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.php\\?", "\\.html\\?"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.html$", "\\.php"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.html\\?", "\\.php\\?"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.aspx$", "\\.html"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.aspx\\?", "\\.html\\?"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.html$", "\\.aspx"), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.html\\?", "\\.aspx\\?"), file);

	}

	public static void getHrefLangs(Map<String, String> url2FileMap, Map<String, String> l12l2FileMap, File htmlFile, String baseUri) throws IOException {
        Document doc = Jsoup.parse(htmlFile, "UTF-8", baseUri);
        Elements links = doc.select("link[hreflang]");
        
        String myLang = "en";
        String otherLang = "fr";
		if (htmlFile.getName().startsWith("fr") ) {
	        myLang = "fr";
	        otherLang = "en";
		}
        
		if (baseUri.contains("www.molior.ca"))  {
			if (doc.select("li[class=lg] > a[href]")!=null) {
				links.add(doc.select("li[class=lg] > a[href]").first());
			}
		} else if (baseUri.contains("www.axa.com") && links.isEmpty() ) {
			// <a href="/en/newsroom/news/philanthropy-in-action-at-AXA" target="_self" class="navigation__language-button-mobile__lang navigation__language-button-mobile__lang--active">en</a>
            // <a href="/fr/newsroom/actualites/AXA-une-philanthropie-en-action" target="_self" class="navigation__language-button-mobile__lang ">fr</a>
			if (doc.select("a[class=navigation__language-button-mobile__lang] ")!=null) {
				Elements addOnLinks = doc.select("a[class=navigation__language-button-mobile__lang] ");
				for (Element link:addOnLinks) {
					if (link.text().startsWith(otherLang) ) {
						links.add(link);break;
					}
				}
			}
		}
        
        for (Element link: links) {
			if (link==null) {
				continue;
			}

        	String href = link.attr("abs:href");
        	//String hreflang = StringUtils.lowerCase(link.attr("hreflang"));
        	href = StringUtils.replacePattern(href, baseUri+"(\\.\\./)+", baseUri+"");
        	href = ilspfcUrlNormalizer.normalize(href);
        	if (url2FileMap.containsKey(href)) {
        		String l1FileName = htmlFile.getName();
        		String l2FileName = new File(url2FileMap.get(href)).getName();
        		int res = l1FileName.compareTo( l2FileName) ;
        		if( res < 1 ) {
        			l12l2FileMap.put(l1FileName, l2FileName);
        		} else {
        			l12l2FileMap.put(l2FileName, l1FileName);
        		}
        	}
        }
	}

	
	public static void writeL1L2FileMap2File( Map<String, String> l1l2FileMap, File hreflangList) throws IOException {
		FileUtils.write(hreflangList, (""), "UTF-8", false);
		TreeSet<String> treeSet = new TreeSet<String>( l1l2FileMap.keySet());
		for (String key : treeSet) {
			String value = l1l2FileMap.get(key);
			//logger.info(key + value);
			FileUtils.write(hreflangList, (key + "\t" + value + "\n"), "UTF-8", true);
		}
	}

}
