package gr.ilsp.fc.parser;

import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedLinksExtractor {

	private static final Logger logger = LoggerFactory.getLogger(ExtendedLinksExtractor.class);

	private static final String A_HREF = "a[href]";
	private static final String LINK_HREFLANG = "link[hreflang]";
	private static final String LINK_CANONICAL = "link[canonical]";

	private static final String CLASS_ORG_JSOUP_NODES_ELEMENT = "class org.jsoup.nodes.Element";
	private static final String CLASS_ORG_JSOUP_NODES_TEXT_NODE = "class org.jsoup.nodes.TextNode";

	private static final String ABS_HREF_ATTR = "abs:href";
	private static final String HREFLANG_ATTR = "hreflang";
	//private static final String REL_ATTR = "rel";

	private static final String SPACE_STR = " ";

	private static ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();

	private static final int context_thresh=40;

	public static ExtendedOutlink[] getLinks(InputStream _input, Metadata metadata) {
		//ArrayList<String[]> rankedlinks = new ArrayList<String[]>();
		org.jsoup.nodes.Document doc;
		ExtendedOutlink[] rankedLinks = null;
		try {
			_input.reset();
			BufferedReader reader = new BufferedReader(new InputStreamReader(_input,metadata.get(Metadata.CONTENT_ENCODING)));

			String htmltext = "", linktext = "", anchortext = "";
			String line = "", temp = "";
			String pre_extendedtext ="", post_extendedtext ="", wholetext="";
			int countpre=0, countpost=0, count=0;
			int prelimit=0, postlimit=0;
			int index = 0;
			List<org.jsoup.nodes.Node> aa = null;
			org.jsoup.nodes.Element node = null;
			String[] preWORDS=null, postWORDS=null;

			while ((line=reader.readLine())!=null) htmltext=htmltext.concat(line);
			reader.close();
			String baseUrl = metadata.get(Metadata.CONTENT_LOCATION);			
			doc = baseUrl!=null ? Jsoup.parse(htmltext,baseUrl) : Jsoup.parse(htmltext);	
			Elements canonicalLinks = doc.select(LINK_CANONICAL);			
			if (canonicalLinks.size()>0) {
				//<link rel="canonical" href="https://blog.example.com/dresses/green-dresses-are-awesome" />
				ExtendedOutlink extendedOutLink = new ExtendedOutlink(canonicalLinks.get(0).attr(ABS_HREF_ATTR), LINK_CANONICAL, null);	
				logger.info("Found canonical URL: " + extendedOutLink.getToUrl());
				rankedLinks = new ExtendedOutlink[1];	
				rankedLinks[0] = extendedOutLink;
				return rankedLinks;
			}

			Elements links = doc.select(A_HREF);			
			//FIXME added to find all links (is this way OK? Do we need both ways?)
			if (links.size()==0){
				doc = Jsoup.connect(baseUrl).get();
				links = doc.select(A_HREF);
			}

			// Add all <link> links that contain the hreflang
			// <link rel="alternate" hreflang="lv" href="http://www.fold.lv/2015/07/one-wolf-entomologija-un-celotaja-gars/" />
			links.addAll(doc.select(LINK_HREFLANG));

			rankedLinks = new ExtendedOutlink[links.size()];	
			int linksIndex = 0;
			for (org.jsoup.nodes.Element link : links) {
				linktext = link.attr(ABS_HREF_ATTR);
				anchortext = link.text().trim();
				aa=link.parent().childNodes();
				index = aa.indexOf(link);
				pre_extendedtext ="";
				post_extendedtext ="";
				wholetext="";
				countpre=0;
				countpost=0;
				count=0;
				for (int bb=1;bb<aa.size();bb++){
					if (index-bb>-1){
						temp = aa.get(index-bb).getClass().toString();
						if (temp.equals(CLASS_ORG_JSOUP_NODES_TEXT_NODE)){
							pre_extendedtext= aa.get(index-bb)+SPACE_STR+pre_extendedtext;
						}else if (temp.equals(CLASS_ORG_JSOUP_NODES_ELEMENT)){
							node = (org.jsoup.nodes.Element) aa.get(index-bb);
							pre_extendedtext= node.text()+SPACE_STR+pre_extendedtext;
						}
					}
					if (index+bb<aa.size()){
						temp = aa.get(index+bb).getClass().toString();
						if (temp.equals(CLASS_ORG_JSOUP_NODES_TEXT_NODE)){
							post_extendedtext+= SPACE_STR+aa.get(index+bb);
						} else if (temp.equals(CLASS_ORG_JSOUP_NODES_ELEMENT)){
							node = (org.jsoup.nodes.Element) aa.get(index+bb);
							post_extendedtext+= SPACE_STR+node.text();
						}
					}
					pre_extendedtext=pre_extendedtext.trim();
					countpre = ReadResources.countTokens(pre_extendedtext);
					post_extendedtext=post_extendedtext.trim();
					countpost = ReadResources.countTokens(post_extendedtext);
					count = countpre+countpost;
					count = countpre+countpost;
					if (count>=context_thresh){
						break;
					}
				}
				if (count==0){count=1;};
				if (countpre>=context_thresh & countpost>=context_thresh){
					prelimit= Math.min(countpre, (int) Math.rint(0.5*context_thresh));
					postlimit= Math.min(countpost,(int) Math.rint(0.5*context_thresh));
				}else{
					prelimit= Math.min(countpre,(int) Math.rint(countpre*context_thresh/count));
					postlimit= Math.min(countpost,(int) Math.rint(countpost*context_thresh/count));
				}
				preWORDS  = pre_extendedtext.split(SPACE_STR);
				postWORDS = post_extendedtext.split(SPACE_STR);
				for (int bb=preWORDS.length-prelimit-1;bb<preWORDS.length;bb++){
					if (bb>=0){
						wholetext = wholetext.concat(SPACE_STR+ preWORDS[bb]);
					}
				}
				for (int bb=0;bb<postlimit;bb++){
					wholetext += SPACE_STR+ postWORDS[bb];
				}

				ExtendedOutlink extendedOutLink = new ExtendedOutlink(linktext,anchortext,wholetext);

				// A check for links with hreflang () attributes.
				if (link.hasAttr(HREFLANG_ATTR)   ) {// && link.hasAttr(REL_ATTR)) {
					//if (!linktext.contains("http://www.bundesregierung.de/Webs/Breg/")) {
						extendedOutLink.setToUrl(normalizer.normalize(extendedOutLink.getToUrl()));
						extendedOutLink.setHrefLang(link.attr(HREFLANG_ATTR));
						logger.debug("Found hreflang link (" + extendedOutLink.getHrefLang() + "): " + extendedOutLink.getToUrl());
					//}
				}		

				rankedLinks[linksIndex] = extendedOutLink;
				linksIndex++;
				//rankedlinks.add(new String[] {linktext, anchortext, wholetext});
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rankedLinks;		
	}

}
