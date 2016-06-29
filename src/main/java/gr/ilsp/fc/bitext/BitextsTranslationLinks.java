package gr.ilsp.fc.bitext;

import gr.ilsp.fc.datums.ExtendedParsedDatum;
import gr.ilsp.fc.parser.ExtendedOutlink;
import gr.ilsp.fc.utils.CrawlConfig;
import gr.ilsp.fc.utils.ISOLangCodes;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bixo.utils.CrawlDirUtils;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryIterator;

public class BitextsTranslationLinks {

	private static final Logger LOGGER = LoggerFactory.getLogger(BitextsTranslationLinks.class);
	private static final String UNDERSCORE_STR = "_";

	/**
	 * Scans a directory with results from a crawl and returns a map of
	 * URL1_L1->URL2_L2, based on translation links from each datum's
	 * datum.getTranslationOutlinks(). These translation links were collected
	 * during the crawl by checking HTML pages a) for links with the hreflang
	 * attribute in links; and b) for other translation links, discovered with
	 * heuristics.
	 * 
	 * @param conf
	 * @param langs
	 * @param crawlDirName
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> getURLPairsFromTranslationLinks(JobConf conf,
			List<String> langs, String crawlDirName) throws IOException {

		Map<String, String> urlPairsFromTranslationLinks = new HashMap<String, String>();
		Path crawlDirPath = new Path(crawlDirName);
		FileSystem fs = crawlDirPath.getFileSystem(conf);

		if (!fs.exists(crawlDirPath)) {
			LOGGER.error("Prior crawl output directory does not exist: " + crawlDirName);
			LOGGER.error("Returning an empty map of URL pairs");
			return urlPairsFromTranslationLinks;
		}

		// FIXME: Should the following be -1 as in the exportXML step or 0?
		int prevLoop = -1;
		Path curDirPath = null;

		while ((curDirPath = CrawlDirUtils.findNextLoopDir(fs, crawlDirPath,
				prevLoop)) != null) {

			Path parseDbPath = new Path(curDirPath,
					CrawlConfig.PARSE_SUBDIR_NAME);
			Tap parseDbTap = new Hfs(new SequenceFile(
					ExtendedParsedDatum.FIELDS), parseDbPath.toUri().toString());
			LOGGER.debug("Examining " + parseDbPath.toUri().toString());
			TupleEntryIterator iter = parseDbTap.openForRead(conf);
			iter = parseDbTap.openForRead(conf);

			while (iter.hasNext()) {
				TupleEntry entry = iter.next();
				ExtendedParsedDatum datum = new ExtendedParsedDatum(entry);
				String myUrl = datum.getUrl();
				String myLang = datum.getLanguage();
				ExtendedOutlink[] translationOutLinks = datum
						.getTranslationOutlinks();
				for (ExtendedOutlink translationOutLink : translationOutLinks) {
					String outLinkLang = ISOLangCodes.get3LetterCode(translationOutLink.getHrefLang());
					if (langs.contains(outLinkLang)) {			
						// Check for pages wrongly pointing to themselves. Seems to be an issue for certain sites .
						if (myUrl.equals(translationOutLink.getToUrl()) || myLang.equals(outLinkLang)) {
							continue;
						}
						urlPairsFromTranslationLinks.put(myUrl + UNDERSCORE_STR + myLang,
								translationOutLink.getToUrl() + UNDERSCORE_STR
										+ outLinkLang);
						LOGGER.debug("Adding " + translationOutLink.toString());
					}
				}
			}

			int curLoop = CrawlDirUtils.extractLoopNumber(curDirPath);
			if (curLoop != prevLoop + 1) {
				LOGGER.warn(String.format(
						"Missing directories between %d and %d", prevLoop,
						curLoop));
			}
			prevLoop = curLoop;
		}
		// all pairs based on links (birectional or not) are considered
		return urlPairsFromTranslationLinks;
	}

	/**
	 * Converts a map of urlPairsFromTranslationLinks to a map of
	 * idPairsFromTranslationLinks, using a map of urlsToIds, generated during
	 * exporting.
	 * 
	 * @param urlPairsFromTranslationLinks URL1-->URL2
	 * @param urlsToIds URL1-->File1
	 * @return Map File1-->File2
	 */
	public static Map<String, String> getIdPairsFromTranslationLinks (
			Map<String, String> urlPairsFromTranslationLinks,
			Map<String, String> urlsToIds) {

	//	for (Map.Entry<String, String> entry : urlsToIds.entrySet()) {
	//		LOGGER.info(entry.getKey()+"\t"+entry.getValue());
	//	}
		
		Map<String, String> idPairsFromTranslationLinks = new HashMap<>();
		Set<String> paired = new HashSet<>();

		for (Map.Entry<String, String> entry : urlPairsFromTranslationLinks.entrySet()) {
			String url1_lang = entry.getKey();
			int p1 = url1_lang.lastIndexOf(UNDERSCORE_STR);
			String url1 = url1_lang.substring(0, p1);
			//String lang1 = url1_lang.substring(p1 + 1);
			String url2_lang = entry.getValue();
			int p2 = url2_lang.lastIndexOf(UNDERSCORE_STR);
			String urlVal = url2_lang.substring(0, p2);
			//String lang2 = url2_lang.substring(p2 + 1);
//LOGGER.info(url1_lang+"\t"+url2_lang+"\t");


			// Do not add  any  other  pairs.
			if (paired.contains(url1_lang) || paired.contains(url2_lang)) { 
				continue;
			}
			
			if (urlsToIds.containsKey(url1) && urlsToIds.containsKey(urlVal) && !urlsToIds.get(url1).equals(urlsToIds.get(urlVal))) { 
				//idPairsFromTranslationLinks.put( urlsToIds.get(url1) + UNDERSCORE_STR + lang1, urlsToIds.get(urlVal) + UNDERSCORE_STR + lang2);
				idPairsFromTranslationLinks.put( urlsToIds.get(url1) , urlsToIds.get(urlVal));
				LOGGER.debug(urlsToIds.get(url1) + " -> " + urlsToIds.get(urlVal));
			} else {
				// FIXME when urlsToIDs do not contain these urls? when the
				// webpage was not visited or the cesDoc has not been generated
				LOGGER.debug(urlsToIds.get(url1) + " or "
						+ urlsToIds.get(urlVal)
						+ " are not in the list of files.");
			}
			if (urlsToIds.containsKey(url1)) {
				paired.add(url1_lang);
			}
			if (urlsToIds.containsKey(urlVal)) {
				paired.add(url2_lang);
			}
		}
		return idPairsFromTranslationLinks;
	}

}
