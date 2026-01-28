# ILSP Focused Crawler

ILSP Focused Crawler (ILSP-FC) is a research prototype for acquiring domain-specific monolingual and bilingual [corpora](http://en.wikipedia.org/wiki/Text_corpus). The required input from the user consists of a list of seed URLs pointing to relevant web pages and a list of terms that describe a topic. ILSP-FC integrates modules for text normalization, language identification, document clean-up, text classification, bilingual document alignment (i.e. identification of pairs of documents that are translations of each other) and sentence alignment. If the user does not provide a list of terms, the software can be used as a general crawler.

ILSP-FC was developed by researchers of the [ILSP/Athena RIC](http://www.ilsp.gr/en/) and was used in the [European Language Resource Coordination](http://www.lr-coordination.eu/home) Data effort. ELRC Data implemented the acquisition of language resources and language processing services, as well as their provision to the language resource repository of the [Connecting Europe Facility](http://ec.europa.eu/digital-agenda/en/connecting-europe-facility) (CEF) [eTranslation platform](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eTranslation), which helped European and national public administrations exchange information across language barriers in EU.

An initial version of the crawler was produced during [PANACEA](http://panacea-lr.eu), an [EU FP7](http://cordis.europa.eu/fp7/ict/language-technologies/home_en.html) project for the acquisition and production of Language Resources. It was then extended during the [QTLaunchPad](http://www.qt21.eu/launchpad) project, a European Commission-funded collaborative research initiative dedicated to overcoming quality barriers in machine and human translation and in language technologies; and the [FP7-PEOPLE](http://cordis.europa.eu/fp7/people/home_en.html) [Abu-MaTran] (http://www.abumatran.eu/) project for enhancing industry-academia cooperation in the adoption of machine translation technologies.

ILSP-FC is a Java project released under the [GNU GPL, v. 3.0](http://www.gnu.org/licenses/gpl-3.0.en.html) license. It depends on open-source libraries for [web mining](https://github.com/bixo/bixo) and [building data-processing workflows](http://cascading.org).  

If you use ILSP-FC in scientific work, please cite: Papavassiliou, V., Prokopidis, P. & G. Thurmair. (2013). [A modular open-source focused crawler for mining monolingual and bilingual corpora from the web.](https://aclweb.org/anthology/W/W13/W13-2506.pdf) In Proceedings of the Sixth Workshop on Building and Using Comparable Corpora, pages 43-51. Sofia, Bulgaria : Association for Computational Linguistics ([BibTeX](https://aclweb.org/anthology/W/W13/W13-2506.bib))

The pair detection module of ILSP-FC was used for aligning documents in the WMT16 Bilingual Document Alignment Shared Task. The system reached a recall of 91% in the soft scoring setting prepared by the organizers. More details are presented in the system paper: Papavassiliou, V., Prokopidis, P., and Piperidis, S. (2016). [The ILSP/ARC submission to the WMT 2016 bilingual document alignment shared task](https://www.aclweb.org/anthology/W16-2375.pdf). In Proceedings of the First Conference on Machine Translation. Berlin, Germany: Association for Computational Linguistics ([BibTeX](https://www.aclweb.org/anthology/W16-2375.bib))

Please send any questions for ilsp-fc to ilsp-dot-gr.
 
## Workflows

The current version of ILSP-FC offers the user the option to [run all relevant processes](ilsp-fc/Getting_Started.md) in a pipeline or to select a specific process (e.g. export or deduplication, or pair detection, etc.).

In a configuration for acquiring monolingual data, ILSP-FC applies the following processes (one after the other):

- crawls the web until an expiration criterion is met (i.e. harvests webpages and stores the ones that are in the targeted language, and relevant to a targeted topic if required)
- exports the stored data (i.e. stores downloaded web pages/documents and for each page generates a CesDoc files with its content and metadata). 
- discards (near) duplicate documents


In a configuration for acquiring parallel data, it applies the following processes (one after the other):

- [crawls](ilsp-fc/Crawl.md) a website with content in the targeted languages (i.e. harvests the website and stores pages that are in the targeted languages, and relevant to a targeted topic if required)
- [exports](ilsp-fc/Export.md) the stored data (i.e. stores downloaded web page/document and generates a CesDoc file with its content and metadata). 
- discards (near) [duplicate](ilsp-fc/Near_Deduplication.md) documents
- identifies [pairs](ilsp-fc/Pair_Detection.md) of (candidate) parallel documents and generates a cesAlign file for each detected pair.
- [aligns](ilsp-fc/Segment_Alignment.md) the segments in each detected document pair and generates a TMX for each document pair 
- [merges](ilsp-fc/TMX_merging.md) TMX files corresponding to each document pair in order to create the final output, i.e. a TMX that includes all (or a selection of) segment pairs


## Input

In case of general monolingual crawls, the ILSP-FC travels across the web and stores web pages/documents with content in the targeted language. The required input from the user is:

- a list of seed URLs (i.e. a text file with one URL per text line). 

In case of focused monolingual crawls (i.e. when the crawler visits/processes/stores web pages that are in the targeted language and related to a targeted domain), the input should include:

- a list of seed URLs pointing to relevant web pages. An example seed URL list for _Environment_ in English can be found at [ENV_EN_seeds.txt](ilsp-fc/ENV_EN_seeds.md).
- a list of term triplets (_\<relevance,term,subtopic\>_) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain. An example domain definition can be found at [ENV_EN_topic.txt](ilsp-fc/ENV_EN_topic.md) for the _Environment_ domain in English.


In case of general bilingual crawling, the input from the user includes:
- a seed URL list which should contain URL(s) from only one web site with content in both of the targeted languages(e.g. [ENV_EN_ES_seed](ilsp-fc/ENV_EN_ES_seed.md). The crawler will follow only links pointing to pages inside this web site. Examples of seed URLs can be found at [seed_examples](ilsp-fc/Seed_examples.md).

In case of focused bilingual crawls, the input should also include: 

- a list of term triplets (_\<relevance,term,subtopic\>_) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain in both the targeted languages (i.e. the union of the domain definition in each language). An example domain definition of  _Environment_ for the English-Spanish pair can be found at [ENV_EN_ES_topic](ilsp-fc/ENV_EN_ES_topic.md). 
Note that in case a thematic website is targeted, it is very likely that examination of domainnesses could  be avoid (i.e. construction and use of a list of terms that define the targeted topic might be redundant).

## Output

Each module of the tool provides its own output which feeds the next module in the pipeline: 

[Crawl](ilsp-fc/Crawl.md): Creates the "run" directories (i.e. directories containing all resources fetched/extracted/used/required for each cycle of this crawl). (See setting _dest_)

[Export](ilsp-fc/Export.md)


- a list of links pointing to XML files following the cesDOC Corpus Encoding Standard (http://www.xces.org/). See this [cesDoc](http://nlp.ilsp.gr/xslt/ilsp-fc/1.xml) file for an example in English for the _Environment_ domain. 
- a list of links pointing to HTML files (by XSL transformation of each XML) for easier browsing of the collection. As an example, see this [rendered cesDoc](http://nlp.ilsp.gr/xslt/ilsp-fc/1.xml.html) file.

[Pair_detection](ilsp-fc/Pair_Detection.md) : 

- a list of links to XML files following the cesAlign Corpus Encoding Standard for linking cesDoc documents. This example [cesAlign](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.xml) file serves as a link between a detected pair of cesDoc documents in [English](http://nlp.ilsp.gr/xslt/ilsp-fc/98.xml) and [Spanish](http://nlp.ilsp.gr/xslt/ilsp-fc/44.xml).
- a list of links pointing to HTML files (by XSL transformation of each cesAlign XML) for easier browsing of the collection. As an example, see this [rendered cesAlign](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.xml.html) file.

[Segment Alignment](ilsp-fc/Segment_Alignment.md):

- a list of links to TMX files containing sentence alignments that have been extracted from the detected document pairs. As an example, see this [TMX](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.tmx) file.
- a list of links pointing to HTML files (by XSL transformation of each TMX) for easier browsing of the collection. As an example, see this [rendered TMX](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.html) file.

[TMX merging](ilsp-fc/TMX_merging.md):

- a TMX file that includes filtered segment pairs of the generated TMX files. This is the final output of the process (i.e. the parallel corpus). As an example, see this [TMX](http://nlp.ilsp.gr/ilsp-fc/merged_tmx/sample_merged_eng-spa_resource.tmx) file.
- an HTML file (by XSL transformation of the TMX file). As an example, see this [rendered TMX](http://nlp.ilsp.gr/ilsp-fc/merged_tmx/sample_merged_eng-spa_resource.html) file.
- an XML file which contains metadata of the generated corpus. As an example, see this [XML](http://nlp.ilsp.gr/ilsp-fc/merged_tmx/sample_merged_eng-spa_resource.md.xml) file.

# More information

- [How to get ILSP-FC](ilsp-fc/How_To_Get.md): Learn about the different ways to get ILSP-FC
- [Developer Setup](ilsp-fc/Developer_Setup.md): Learn how to build ILSP-FC 
- [Getting Started](ilsp-fc/Getting_Started.md): Learn how to run ILSP-FC
- [Languages Supported](ilsp-fc/Languages-supported.md): Learn about supported languages
- [Resources](ilsp-fc/Resources.md) acquired with ILSP-FC

