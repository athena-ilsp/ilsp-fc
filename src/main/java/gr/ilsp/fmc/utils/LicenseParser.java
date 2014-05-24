/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.ilsp.fmc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


import org.apache.tika.metadata.CreativeCommons;
import org.apache.tika.metadata.Metadata;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.Purifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * 
 * Adds metadata identifying the Creative Commons license used, if any. 
 * 
 * <p>Modified code from nutch/trunk/src/plugin/creativecommons/src/java/org/creativecommons/nutch/CCParseFilter.java
 * 
 */
public class LicenseParser {

	public static final Logger LOGGER = LoggerFactory.getLogger(LicenseParser.class);

	private static final HashMap<String, String> WORK_TYPE_NAMES = new HashMap<String, String>();
	static {
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/MovingImage", "video");
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/StillImage", "image");
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/Sound", "audio");
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/Text", "text");
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/Interactive", "interactive");
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/Software", "software");
		WORK_TYPE_NAMES.put("http://purl.org/dc/dcmitype/Image", "image");
	}

	/** Walks DOM tree, looking for RDF in comments and licenses in anchors.*/
	public static class Walker {
		private static final String LICENSES_STR = "/licenses/";
		private static final String HTTP_PROTOCOL = "http";
		private static final String CREATIVECOMMONS_ORG_STR = "creativecommons.org";
		private static final String A_LICENSE_LOCATION = "a";
		private static final String REL_LICENSE_LOCATION = "rel";
		private URL base;                             // base url of page
		//private String rdfLicense;                    // subject url found, if any
		private URL relLicense;                       // license url found, if any
		private URL anchorLicense;                    // anchor url found, if any
		private String workType;                      // work type URI

		/** Scan the document adding attributes to metadata.
		 * @throws Exception */
		public static void walk(Node doc,  Metadata metadata) throws Exception {

			// walk the DOM tree, scanning for license data
			Walker walker = new Walker();
			walker.walk(doc);

			// interpret results of walk
			String licenseUrl = null;
			String licenseLocation = null;
			if (walker.relLicense != null) {     // 2nd: anchor w/ rel=license
				licenseLocation = REL_LICENSE_LOCATION;
				licenseUrl = walker.relLicense.toString();
			} else if (walker.anchorLicense != null) {  // 3rd: anchor w/ CC license
				licenseLocation = A_LICENSE_LOCATION;
				licenseUrl = walker.anchorLicense.toString();
//			} else if (walker.rdfLicense != null) {            // 1st choice: subject in RDF. Not used right now. Should we use it?
//					licenseLocation = "rdf";
//					licenseUrl = walker.rdfLicense;
			} else  {
				throw new Exception("No CC license found.");
			}

			// add license to metadata
			if (licenseUrl != null) {
				LOGGER.debug("CC: found "+licenseUrl+" in "+licenseLocation);
				
				metadata.add(CreativeCommons.LICENSE_URL, licenseUrl);
				metadata.add(CreativeCommons.LICENSE_LOCATION, licenseLocation);
			}

			if (walker.workType != null) {
				LOGGER.debug("CC: found "+walker.workType);
				metadata.add(CreativeCommons.WORK_TYPE, walker.workType);
			}
		}

		/** Scan the document looking for RDF in comments and license elements.*/
		private void walk(Node node) {

			// check element nodes for license URL
			if (node instanceof Element) {
				findLicenseUrl((Element)node);
			}

//			// check comment nodes for license RDF
//			if (node instanceof Comment) {
//				findRdf(((Comment)node).getData());
//			}

			// recursively walk child nodes
			NodeList children = node.getChildNodes();
			for (int i = 0; children != null && i < children.getLength(); i++ ) {
				walk(children.item(i));
			}
		}

		/** Extract license url from element, if any.  Thse are the href attribute
		 * of anchor elements with rel="license".  These must also point to
		 * http://creativecommons.org/licenses/. */
		private void findLicenseUrl(Element element) {
			// only look in Anchor elements
			if (!A_LICENSE_LOCATION.equalsIgnoreCase(element.getTagName()))
				return;

			// require an href
			String href = element.getAttribute("href");
			if (href == null)
				return;

			try {
				URL url = new URL(base, href);            // resolve the url

				// check that it's a CC license URL
				if (HTTP_PROTOCOL.equalsIgnoreCase(url.getProtocol()) &&
						CREATIVECOMMONS_ORG_STR.equalsIgnoreCase(url.getHost()) &&
						url.getPath() != null &&
						url.getPath().startsWith(LICENSES_STR) &&
						url.getPath().length() > LICENSES_STR.length()) {

					// check rel="license"
					String rel = element.getAttribute(REL_LICENSE_LOCATION);
					if (rel != null && "license".equals(rel) && this.relLicense == null) {
						this.relLicense = url;                   // found rel license
					} else if (this.anchorLicense == null) {
						this.anchorLicense = url;             // found anchor license
					}
				}
			} catch (MalformedURLException e) {         // ignore malformed urls
			}
		}

//		/** Creative Commons' namespace URI. */
//		private static final String CC_NS = "http://web.resource.org/cc/";
//
//		/** Dublin Core namespace URI. */
//		private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
//
//		/** RDF syntax namespace URI. */
//		private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

//		private void findRdf(String comment) {
//			// first check for likely RDF in comment
//			int rdfPosition = comment.indexOf("RDF");
//			if (rdfPosition < 0)
//				return;                                   // no RDF, abort
//			int nsPosition = comment.indexOf(CC_NS);
//			if (nsPosition < 0)
//				return;                                   // no RDF, abort
//
//			// try to parse the XML
//			Document doc;
//			try {
//				DocumentBuilder parser = FACTORY.newDocumentBuilder();
//				doc = parser.parse(new InputSource(new StringReader(comment)));
//			} catch (Exception e) {
//				if (LOG.isWarnEnabled()) {
//					LOG.warn("CC: Failed to parse RDF in "+base+": "+e);
//				}
//				//e.printStackTrace();
//				return;
//			}
//
//			// check that root is rdf:RDF
//			NodeList roots = doc.getElementsByTagNameNS(RDF_NS, "RDF");
//			if (roots.getLength() != 1) {
//				if (LOG.isWarnEnabled()) { LOG.warn("CC: No RDF root in "+base); }
//				return;
//			}
//			Element rdf = (Element)roots.item(0);
//
//			// get cc:License nodes inside rdf:RDF
//			NodeList licenses = rdf.getElementsByTagNameNS(CC_NS, "License");
//			for (int i = 0; i < licenses.getLength(); i++) {
//
//				Element l = (Element)licenses.item(i);
//
//				// license is rdf:about= attribute from cc:License
//				this.rdfLicense = l.getAttributeNodeNS(RDF_NS,"about").getValue();
//
//				// walk predicates of cc:License
//				NodeList predicates = l.getChildNodes();
//				for (int j = 0; j < predicates.getLength(); j++) {
//					Node predicateNode = predicates.item(j);
//					if (!(predicateNode instanceof Element))
//						continue;
//					Element predicateElement = (Element)predicateNode;
//
//					// extract predicates of cc:xxx predicates
//					if (!CC_NS.equals(predicateElement.getNamespaceURI())) {
//						continue;
//					}
//					String predicate = predicateElement.getLocalName();
//
//					// object is rdf:resource from cc:xxx predicates
//					String object =
//							predicateElement.getAttributeNodeNS(RDF_NS, "resource").getValue();
//
//					// add object and predicate to metadata
//					// metadata.put(object, predicate);
//					// if (LOG.isInfoEnabled()) {
//					//   LOG.info("CC: found: "+predicate+"="+object);
//					// }
//				}
//			}
//
//			// get cc:Work nodes from rdf:RDF
//			NodeList works = rdf.getElementsByTagNameNS(CC_NS, "Work");
//			for (int i = 0; i < works.getLength(); i++) {
//				Element l = (Element)works.item(i);
//
//				// get dc:type nodes from cc:Work
//				NodeList types = rdf.getElementsByTagNameNS(DC_NS, "type");
//				for (int j = 0; j < types.getLength(); j++) {
//					Element type = (Element)types.item(j);
//					String workUri = 
//							type.getAttributeNodeNS(RDF_NS, "resource").getValue();
//					this.workType = (String)WORK_TYPE_NAMES.get(workUri);
//					break;
//				}
//			}
//		}
	}

	/** Adds metadata or otherwise modifies a parse of an HTML document, given
	 *  a URL of page on disk or on the web. 
	 * @throws IOException 
	 * @throws SAXException */
	public Metadata getLicense(Document doc, Metadata metadata)  {
		Node rootNode = doc.getDocumentElement();
		// extract license metadata
		try {
			Walker.walk(rootNode, metadata);
		} catch (Exception e) {
			LOGGER.warn("Could not parse document");
		}
		return metadata;
	}



	
	public static void main(String[] args) throws IOException, SAXException  {
		
		// URL can be a page on disk as well
		String baseTestUrl = "http://code.creativecommons.org/svnroot/ccnutch/trunk/data/";
		for (String page : new String[]{"anchor.html", "rdf.html", "rel.html"}) {
			
			// You most probably already have all these
			Metadata metadata = new Metadata();
			URL url = new URL(baseTestUrl + page);
			LOGGER.info (url.toString());
			HttpURLConnection hcon = (HttpURLConnection)url.openConnection();
			hcon.setConnectTimeout(5000);
			hcon.setReadTimeout(5000);
			hcon.setUseCaches(false);
			hcon.setRequestProperty("User-Agent","ISPL");
			hcon.setRequestProperty("Accept-Charset","utf-8");
			hcon.setRequestProperty("Keep-Alive","300");
			//
			
			
			InputStream instr = hcon.getInputStream();
			HTMLConfiguration config = new HTMLConfiguration();
			XMLDocumentFilter[] filters = { new Purifier() }; // This is important, otherwise you cannot domify below.
			config.setProperty("http://cyberneko.org/html/properties/filters",filters);
			DOMParser dp = new DOMParser(config);
			dp.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace",false);
			dp.parse(new org.xml.sax.InputSource(instr));
			Document doc = dp.getDocument();
	
			LicenseParser lp = new LicenseParser();
			//System.out.println(lp.getLicense(doc, metadata));
		}
	
	}



	

}
