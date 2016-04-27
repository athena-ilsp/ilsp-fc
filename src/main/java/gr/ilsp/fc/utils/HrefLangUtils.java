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
		logger.info("Writing hreflang list of " + l1l2FileMap.keySet().size() + " pairs to " + hreflangList);
		writeL1L2FileMap2File(l1l2FileMap, hreflangList);
		logger.info("Done");
	}

	public static Map<String, String> getUrl2FileMap(File fileUrlLang) throws IOException {
		Map<String, String> url2FileMap =  new HashMap<String, String>();
		List<String> fileUrlLangLines = FileUtils.readLines(fileUrlLang);
		int i = 1;
		for (String line: fileUrlLangLines) {
			String[] fields = StringUtils.split(line);
			//logger.info(fields[0] + " " + fields[1] + " " + fields[2]);
			populateUrlFileMap(url2FileMap, fields[0], fields[1]);
			i++;
			if (i%100000==0) {
				logger.info("Processed " + i + " lines.");
			}
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
		urlFileMap.put(StringUtils.replacePattern(url, "\\.html", ""), file);
		urlFileMap.put(StringUtils.replacePattern(url, "\\.html", "/"), file);
		if (!url.contains(".html")) {
			urlFileMap.put((url + ".html"), file);
		}
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


		if (baseUri.contains("www.molior.ca") && links.isEmpty())  {
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
		} else if (baseUri.contains("histalu.org") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[href]");
				for (Element link:addOnLinks) {
					if (link.attr("href").contains("english") ) {
						//logger.debug(link.toString());
						links.add(link);break;
					}
				}
			}
		} else if (baseUri.contains("pc.gc.ca") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				//<a id="content_0_topmenu_0_langsw" title="English - English version of the Web page" xml:lang="en" href="http://www.pc.gc.ca/eng/voyage-travel/reserve.aspx" lang="en">English</a>
				Elements addOnLinks = doc.select("a[id=content_0_topmenu_0_langsw]");
				for (Element link:addOnLinks) {
					if (link.attr("lang").contains("en") ) {
						//logger.debug(link.toString());
						links.add(link);break;
					}
				}
			}
		} else if (baseUri.contains("www.technip.com") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				//<a href="../../../en/our-business/services/process-technologies.html" class="language-link" 
				// xml:lang="en" title="Process Technologies">English</a></li>
				Elements addOnLinks = doc.select("a[class=language-link]");
				for (Element link:addOnLinks) {
					if (link.attr("xml:lang").contains("en") ) {
						//logger.debug(link.toString());
						links.add(link);break;
					}
				}
			}
		} else if (baseUri.contains("www.juratourisme.ch") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				//				<nav class="language column small4 medium3 large3 count3">
				//                 <a href="http://www.juratourisme.ch/fr/decouvertes/visites-de-villes-guidees.3540/visite-guidee-porrentruy.1513.html">FR</a>
				//                 <a href="http://www.juratourisme.ch/de/entdeckungen/stadtfuhrungen.3540/fuhrungen-durch-porrentruy.1513.html">DE</a>
				//                 <a href="http://www.juratourisme.ch/en/discoveries/guided-city-tours.3540/guided-tour-porrentruy.1513.html" class="on">EN</a>
				//                 </nav>
				Elements addOnLinks = doc.select("nav > a[href]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).equals("en") ) {
						//logger.info(link.toString());
						links.add(link);break;
					}
				}
			}			
			//		// NOT RELIABLE
			//		} else if (baseUri.contains("cmhg-phmc.gc.ca") && links.isEmpty() ) {
			//			if (otherLang.equals("en")) {
			//				Elements addOnLinks = doc.select("li > div > a[href]");
			//				for (Element link:addOnLinks) {
			//					if (StringUtils.lowerCase(link.text()).equals("english") ) {
			//						logger.info(link.toString());
			//						links.add(link);break;
			//					}
			//				}
			//			}			
		} else if (baseUri.contains("www.hydrel.ch") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[href]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).equals("english")  || StringUtils.lowerCase(link.attr("href")).contains("/en/")) {
						logger.info(link.toString());
						links.add(link);
						Element link2 = link.clone();
						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?", "html"));
						links.add(link2);
						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?", "jsp"));
						links.add(link2);
						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?.*$", "jsp"));
						links.add(link2);
						break;
					}
				}
			}			
		} else if (baseUri.contains("www.world-governance.org") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[rel=alternate]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).equals("en")) {
						Element link2 = link.clone();
						link2.attr("href", StringUtils.substring(link.attr("href"), link.attr("href").lastIndexOf("article") ) + "?lang=en" );
						links.add(link2);
						//						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?", "jsp"));
						//						links.add(link2);
						//						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?.*$", "jsp"));
						//						links.add(link2);
						break;
					}
				}
			}			
		} else if (baseUri.contains("taize.fr") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("option[lang]");

				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).equals("english")) {
						if (link.attr("value").contains("article167.html")) {
							continue;
						}
						links.add(link);
						link.attr("href", link.attr("value"));
						Element link2 = link.clone();
						link2.attr("href", link.attr("value")+"?chooselang=1");	
						links.add(link2);
						//						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?", "jsp"));
						//						links.add(link2);
						//						link2.attr("href", StringUtils.replacePattern(link.attr("href"), "html\\?.*$", "jsp"));
						//						links.add(link2);
						break;
					}
				}
			}
		} else if (baseUri.contains("lagardere")  ) {

			if (otherLang.equals("en")) {
				// <a href="http://www.lagardere.com/human-capital/our-approach/presentation-1009.html" 
				// id="linkTranslateVersion" >English Version</a></li>
				Elements addOnLinks = doc.select("a[id=linkTranslateVersion]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).trim().contains("english version")) {
						links.add(link);
						break;
					}
				}
			}
		} else if (baseUri.contains("raison-publique.fr") && links.isEmpty() ) {
			if (otherLang.equals("fr")) {
				// <a href="article443.html" rel="alternate" hreflang="en" title="The Resistance">English</a>
				Elements addOnLinks = doc.select("a[hreflang]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.attr("hreflang")).trim().equals("fr")) {
						links.add(link);
						//logger.info(link.attr("href"));
						break;
					}
				}
			}
		} else if (baseUri.contains("www.international.icomos.org") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[href]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).trim().contains("en")) {
						if (link.attr("href").contains("index.html")) {
							continue;
						}
						links.add(link);
						//logger.info(link.attr("href"));
						break;
					}
				}
				
			}
		} else if (baseUri.contains("www.eufic.org") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[href]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).trim().contains("en-english")) {
						links.add(link);
						Element link2 = link.clone();
						link2.attr("href", link.attr("href").replaceFirst("/en/", "/article/en/"));	
						links.add(link2);
						break;
					}
				}
			}
		} else if (baseUri.contains("egodesign.ca") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[class=lang_option]");
				// <a href="../en/article3216.html?article_id=143&amp;page=7&amp;switchLang=en" class="lang_option">english</a><
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).trim().contains("english")) {
						links.add(link);
						break;
					}
				}
			}
		} else if (baseUri.contains("www.unv.org") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[href]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).trim().contains("english")) {
						logger.info(link.toString());
						links.add(link);
						break;
					}
				}
			}
		} else if (baseUri.contains("www.oag-bvg.gc.ca") && links.isEmpty() ) {
			if (otherLang.equals("en")) {
				Elements addOnLinks = doc.select("a[href]");
				for (Element link:addOnLinks) {
					if (StringUtils.lowerCase(link.text()).trim().contains("english")) {
						logger.info(link.toString());
						links.add(link);
						Element link2 = link.clone();
						link2.attr("href", link.attr("href").replaceFirst("/English/", "/internet/English/"));	
						links.add(link2);
						break;
					}
				}
			}
//		} else if (baseUri.contains("www.iisd.ca") && links.isEmpty() ) {
//			if (otherLang.equals("en")) {
//				Elements addOnLinks = doc.select("a[href]");
//				for (Element link:addOnLinks) {
//
//					if (link.absUrl("href").contains("index.html")  || link.absUrl("href").contains("mailto")) {
//						continue;
//					}
//					
//					String linkText = StringUtils.lowerCase(link.text()).trim();
//
//					if ((linkText.contains("english") || linkText.matches("^enb.*") || linkText.matches("/enb.*")  || linkText.contains("version anglaise") )  ) {
//						logger.info(link.toString());
//						links.add(link);
//						break;
//					}
//				}
//			}

		} else  {
			logger.info("Baseuri not matched " + baseUri);
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
			} else {
				logger.warn(href);
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
