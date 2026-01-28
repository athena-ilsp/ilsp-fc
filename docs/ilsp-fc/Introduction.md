# Introduction

ILSP Focused Crawler (ILSP-FC) is a research prototype for acquiring domain-specific monolingual and bilingual corpora. ILSP-FC integrates modules for text normalization, language identification, document clean-up, metadata extraction, text classification, identification of bitexts (documents that are translations of each other), alignment of segments, and filtering of segment pairs. A detailed description of each module is available at [http://aclweb.org/anthology/W/W13/W13-2506.pdf](http://aclweb.org/anthology/W/W13/W13-2506.pdf) . 

# Workflows

The current version of ILSP-FC offers the user the option to [run all relevant processes](Getting_Started.md) in a pipeline or to select a specific process (e.g. export or deduplication, or pair detection, etc.).

In a configuration for acquiring monolingual data, ILSP-FC applies the following processes (one after the other):

- crawls the web until an expiration criterion is met (i.e. harvests webpages and stores the ones that are in the targeted language, and relevant to a targeted topic if required)
- exports the stored data (i.e. stores downloaded web pages/documents and for each page generates a CesDoc files with its content and metadata). 
- discards (near) duplicate documents


In a configuration for acquiring parallel data, it applies the following processes (one after the other):

- [crawls](Crawl.md) a website with content in the targeted languages (i.e. harvests the website and stores pages that are in the targeted languages, and relevant to a targeted topic if required)
- [exports](Export.md) the stored data (i.e. stores downloaded web page/document and generates a CesDoc file with its content and metadata). 
- discards (near) [duplicate](Near_Deduplication.md) documents
- identifies [pairs](Pair_Detection.md) of (candidate) parallel documents and generates a cesAlign file for each detected pair.
- [aligns](Segment_Alignment.md) the segments in each detected document pair and generates a TMX for each document pair 
- [merges](TMX_merging.md) TMX files corresponding to each document pair in order to create the final output, i.e. a TMX that includes all (or a selection of) segment pairs


# Input

In case of general monolingual crawls, the ILSP-FC travels across the web and stores web pages/documents with content in the targeted language. The required input from the user is:

- a list of seed URLs (i.e. a text file with one URL per text line). 

In case of focused monolingual crawls (i.e. when the crawler visits/processes/stores web pages that are in the targeted language and related to a targeted domain), the input should include:

- a list of seed URLs pointing to relevant web pages. An example seed URL list for _Environment_ in English can be found at [ENV_EN_seeds.txt](ENV_EN_seeds.md).
- a list of term triplets (_\<relevance,term,subtopic\>_) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain. An example domain definition can be found at [ENV_EN_topic.txt](ENV_EN_topic.md) for the _Environment_ domain in English.


In case of general bilingual crawling, the input from the user includes:
- a seed URL list which should contain URL(s) from only one web site with content in both of the targeted languages(e.g. [ENV_EN_ES_seed](ENV_EN_ES_seed.md). The crawler will follow only links pointing to pages inside this web site. Examples of seed URLs can be found at [seed_examples](Seed_examples.md).

In case of focused bilingual crawls, the input should also include: 

- a list of term triplets (_\<relevance,term,subtopic\>_) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain in both the targeted languages (i.e. the union of the domain definition in each language). An example domain definition of  _Environment_ for the English-Spanish pair can be found at [ENV_EN_ES_topic](ENV_EN_ES_topic.md). 
Note that in case a thematic website is targeted, it is very likely that examination of domainnesses could  be avoid (i.e. construction and use of a list of terms that define the targeted topic might be redundant).

# Output

Each module of the tool provides its own output which feeds the next module in the pipeline: 

[Crawl](Crawl.md): Creates the "run" directories (i.e. directories containing all resources fetched/extracted/used/required for each cycle of this crawl). (See setting _dest_)

[Export](Export.md)


- a list of links pointing to XML files following the cesDOC Corpus Encoding Standard (http://www.xces.org/). See this [cesDoc](http://nlp.ilsp.gr/xslt/ilsp-fc/1.xml) file for an example in English for the _Environment_ domain. 
- a list of links pointing to HTML files (by XSL transformation of each XML) for easier browsing of the collection. As an example, see this [rendered cesDoc](http://nlp.ilsp.gr/xslt/ilsp-fc/1.xml.html) file.

[Pair_detection](Pair_Detection.md) : 

- a list of links to XML files following the cesAlign Corpus Encoding Standard for linking cesDoc documents. This example [cesAlign](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.xml) file serves as a link between a detected pair of cesDoc documents in [English](http://nlp.ilsp.gr/xslt/ilsp-fc/98.xml) and [Spanish](http://nlp.ilsp.gr/xslt/ilsp-fc/44.xml).
- a list of links pointing to HTML files (by XSL transformation of each cesAlign XML) for easier browsing of the collection. As an example, see this [rendered cesAlign](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.xml.html) file.

[Segment Alignment](Segment_Alignment.md):

- a list of links to TMX files containing sentence alignments that have been extracted from the detected document pairs. As an example, see this [TMX](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.tmx) file.
- a list of links pointing to HTML files (by XSL transformation of each TMX) for easier browsing of the collection. As an example, see this [rendered TMX](http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.html) file.

[TMX merging](TMX_merging.md):

- a TMX file that includes filtered segment pairs of the generated TMX files. This is the final output of the process (i.e. the parallel corpus). As an example, see this [TMX](http://nlp.ilsp.gr/ilsp-fc/merged_tmx/sample_merged_eng-spa_resource.tmx) file.
- an HTML file (by XSL transformation of the TMX file). As an example, see this [rendered TMX](http://nlp.ilsp.gr/ilsp-fc/merged_tmx/sample_merged_eng-spa_resource.html) file.
- an XML file which contains metadata of the generated corpus. As an example, see this [XML](http://nlp.ilsp.gr/ilsp-fc/merged_tmx/sample_merged_eng-spa_resource.md.xml) file.

# Documentation

- [How to get ILSP-FC](How_To_Get.md): Learn about the different ways to get ILSP-FC
- [Developer Setup](Developer_Setup.md): Learn how to build ILSP-FC 
- [Getting Started](Getting_Started.md): Learn how to run ILSP-FC
- [Languages Supported](Languages-supported.md): Learn about supported languages
- [Resources](Resources.md) acquired with ILSP-FC