# Getting Started

Once you [build](Developer_Setup.md) or [download](How_To_Get.md) an ilsp-fc runnable jar, you can run it like this

```shell
java -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar
```

## Examples of running monolingual crawls

* Given a seed URL list [ENV_EN_seeds.txt](ENV_EN_ES_seed.md), the following example crawls the web for 5 minutes and constructs a collection containing English web pages.

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar \
-crawl -export -dedup -a test -f -type m -cdm 5 -lang en -k -u ENV_EN_seeds.txt -oxslt\
-dest crawlResults -bs "output_test" 
```

In this and other example commands in this documentation, a `log4j.xml` file is being used to set logging configuration details. An example `log4j.xml` file can be downloaded from [here](./Log4j_xml.md). 

*  Given a seed URL list [ENV_EN_seeds.txt](ENV_EN_ES_seed.md) and a topic definition for the _Environment_ domain in Engish [ENV_EN_topic.txt](ENV_EN_topic.md), the following example crawls the web for 10 cycles and constructs a collection containing English web pages related to this domain.

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar \
-crawl -export -dedup -a test1 -f -type m -cdl 10 -lang en -k -u seed-examples.txt -oxslt \
-tc ENV-EN-topic.txt -dom Environment -dest crawlResults -bs "output-test"
```

## Example of running bilingual crawls

This is a test example to verify that the whole workflow (crawl, export, deuplication, pair detection, alingment) works successfully.

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-SNAPSHOT-jar-with-dependencies.jar \
-crawl -export -dedup -pairdetect -align -tmxmerge -f -k -oxslt -type p -cdl 1 -t 20 -len 0 -mtlen 100  \
-pdm "aupdih" -segtypes "1:1" -lang "eng;lt;deu;lv" -a test -filter ".*www\.airbaltic\.com.*" \
-u "/var/www/html/elrc/test-seeds" -dest "/var/www/html/elrc/test" \
-bs "/var/www/html/elrc/test/output_test" &> "/var/www/html/elrc/test/log_test"
```

Seed URLs :

```console
https://www.airbaltic.com/lv/bernu-atlaide
https://www.airbaltic.com/lv/profila-registracija
https://www.airbaltic.com/de/ermaessigung-kinder
https://www.airbaltic.com/de/profil-erstellen
https://www.airbaltic.com/en/child-discount
https://www.airbaltic.com/en/create-account
https://www.airbaltic.com/lt/child-discount
https://www.airbaltic.com/lt/sukurti-paskira
```


## Options
There are several options concerning the applied processes. Besides the following comprehensive list, you could see the options that are supported for each module.  

```console
 -a,--agentname <arg>                  Agent name to identify the person or the organization
                                       responsible for the crawl
 -align,--align_sentences <arg>      Sentence align document pairs using this aligner (default is
                                       maligna)
 -bs,--basename <arg>                  Basename to be used in generating all files for easier
                                       content navigation
 -cdm,--crawlduration <arg>            Maximum crawl duration in minutes
 -cc,--creative_commons                Force the alignment process to generate a merged TMX with
                                       sentence alignments only from document pairs for which an
                                       open content license has been detected.
 -cfg,--config <arg>                   Path to the XML configuration file
 -crawl,--crawl                        Start a crawl
 -d,--stay_in_webdomain                Force the monolingual crawler to stay in a specific web
                                       domain
 -dbg,--debug                          Use debug level for logging
 -dedup,--deduplicate                  Deduplicate and discard (near) duplicate documents
 -del,--delete_redundant_files       Delete redundant crawled documents that have not been
                                       detected as members of a document pair
 -dest,--destination <arg>            Path to a directory where the acquired/generated resources
                                       will be stored
 -pdm,--pairDetectMethods <arg>        When creating a merged TMX file, only use sentence alignments
                                       from document pairs that have been identified by specific
                                       methods, e.g. auidh. See the pdm option.
 -dom,--domain <arg>                   A descriptive title for the targeted domain
 -export,--export                      Export crawled documents to cesDoc XML files
 -f,--force                            Force a new crawl. Caution: This will remove any previously
                                       crawled data
 -filter,--fetchfilter <arg>           Use this regex to force the crawler to crawl only in specific
                                       sub webdomains. Webpages with urls that do not match this
                                       regex will not be fetched.
 -h,--help                             This message
 -i,--inputdir <arg>                   Input directory for deduplication, pairdetection, or
                                       alignment
 -ifp,--image_urls                     Full image URLs (and not only their basenames) will be used
                                       in pair detection with common images
 -k,--keepboiler                       Keep and annotate boilerplate content in parsed text
 -l,--loggingAppender <arg>            Logging appender (console, DRFA) to use
 -lang,--languages <arg>               Two or three letter ISO code(s) of target language(s), e.g.
                                       el (for a monolingual crawl for Greek content) or eng;el (for
                                       a bilingual crawl)
 -len,--length <arg>                   Μinimum number of tokens per text block. Shorter text blocks
                                       will be annoteted as "ooi-length"
 -mtlen,--minlength <arg>              Minimum number of tokens in crawled documents (after
                                       boilerplate detection). Shorter documents will be discarded.
 -cdl,--numloops <arg>            Maximum number of fetch/update loops
 -oxslt,--offline_xslt                 Apply an xsl transformation to generate html files during
                                       exporting.
 -p_r,--path_replacements <arg>        Put the strings to be replaced, separated by ';'. This might
                                       be useful for crawling via the web service
 -pairdetect,--pair_detection          Detect document pairs in crawled documents
 -pdm,--pair_detection_methods <arg>   Α string forcing the crawler to detect pairs using one or
                                       more specific methods: a (links between documents), u
                                       (patterns in urls), p (common images and similar digit
                                       sequences),i (common images), d (similar digit sequences), h, or m, or l
                                       (high/medium/low similarity of html structure)
 -segtypes,--segtypes <arg>            When creating a merged TMX file, only use sentence alignments
                                       of specific types, ie. 1:1
 -storefilter,--storefilter <arg>      Use this regex to force the crawler to store only webpages
                                       with urls that match this regex.
 -t,--threads <arg>                    Maximum number of fetcher threads to use
 -tc,--topic <arg>                     Path to a file with the topic definition
 -tmxmerge,--tmxmerge                  Merge aligned segments from each document pair into one tmx
                                       file
 -type,--type <arg>                    Crawl type: m (monolingual) or  p (parallel)
 -u,--urls <arg>                       File with seed urls used to initialize the crawl
 -u_r,--url_replacements <arg>         A string to be replaced, separated by ';'.
```

<!--
[//]: ## (Other settings)

[//]: # (There are several settings that influence the crawling process and can be defined in a configuration file before the crawling process. The default configuration files for monolingual and bilingual crawls are [[FMC_config.xml]]  and [[FBC_config.xml]] respectively. They are included in the ilsp-fc runnable jar.)

[//]: # ( -doctypes	:	Defines the types of the document pairs from which the segment pairs will be selected. The proposed value is "aupidh"	since pairs of type "m" and "l" (e.g. eng-1_lav-3_m.xml or eng-2_lav-8_l.xml) are only used for testing or examining the tool.)


[//]: # ( ## Input )

[//]: # (In case of general monolingual crawls the required input from the user is: )
[//]: # (* a list of seed URLs (i.e. a text file with one URL per text line). )

[//]: # (In case of focused monolingual crawls (i.e. when the crawler visits/processes/stores web pages that are related to a targeted domain), the input should include: ) 
[//]: # (* a list of seed URLs pointing to relevant web pages. An example seed URL list for _Environment_ in English can be found at [[ENV_EN_seeds.txt]]. )
[//]: # (* a list of term triplets (_<relevance,term,subtopic>_) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain. An example domain definition can be found at [[ENV_EN_topic.txt]] for the _Environment_ domain in English. Details on how to construct/bootstrap such lists and how they are used in text to topic classification could be found at this paper http://www.aclweb.org/anthology/W13-2506.pdf )

[//]: # (In case of general bilingual crawling, the input from the user includes:)
[//]: # (* a seed URL list which should contain URL(s) from only one web site (e.g. [[ENV_EN_ES_seed.txt]]). The crawler will follow only links pointing to pages inside this web site. However, the user could use the <code> filter </code> parameter (see below) to allow visiting only links pointing to pages either inside versions of the top domain of the URL (e.g. http://www.fifa.com/,  http://es.fifa.com/ , etc.) or in different web sites (i.e. in cases the translations are in two web sites e.g. http://www.nrcan.gc.ca and http://www.rncan.gc.ca). Examples of seed URLs can be found at [[seed_examples.txt]]. )

[//]: # (In case of focused bilingual crawls, the input should also include: )
[//]: # (* a list of term triplets (_<relevance,term,subtopic>_) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain in both the targeted languages (i.e. the union of the domain definition in each language). An example domain definition of  _Environment_ for the English-Spanish pair can be found at [[ENV_EN_ES_topic.txt]].)

[//]: # (## Language support )

[//]: # (For both monolingual and bilingual crawling, the set of currently supported languages comprises de, el, en, es, fr, ga, hr, it, ja, and pt. )

[//]: # (In order to add another language, a developer/user should: )
[//]: # (* verify that the targeted language is supported by the default language identifier (https://code.google.com/p/language-detection/) integrated in the ILSP-FC, )
[//]: # (* add a textline with proper content in the [[langKeys.txt]] file which is included in the ilsp-fc runnable jar, and)
[//]: # (* add a proper analyser in the <code>gr.ilsp.fmc.utils.AnalyserFactory</code> class of the ilsp-fc source.  )



[//]: # (## Run a monolingual crawl )

[//]: # ( <pre><code>java -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar crawlandexport -a vpapa@ilsp.gr -cfg FMC_config.xml -type m -c 10 -lang en -of output_test1_list.txt -ofh output_test1_list.txt.html -tc ENV_EN_topic.txt  -u ENV_EN_seeds.txt -f -k -dom Environment</code></pre> )

[//]: # ( <pre><code>java -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar crawlandexport -a test2 -f -k -type m -c 5 -lang es -of output_test2_list.txt -ofh output_test2_list.txt.html -u seed_examples.txt  </code></pre> )

[//]: # (## Run a bilingual crawl )

[//]: # ( <pre><code>java -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar crawlandexport -a test3 -c 10 -f -k -l1 de -l2 it -of test_HS_DE-IT_output.txt -ofh test_HS_DE-IT_output.txt.html -tc HS_DE-IT_topic.txt -type p -u seed_examples.txt -cfg FBC_config.xml -dom HS -len 0 -mtlen 100 -xslt -oxslt</code></pre> )

[//]: # ( <pre><code>java -jar ilsp-fc-X.Y.Z-jar-with-dependencies.jar crawlandexport -a test4 -c 10 -f -k -l1 es -l2 en -type p -u seed_examples.txt -filter ".*uefa.com.*" -len 0 -mtlen 80 -xslt -oxslt -dest "/var/crawl_results/" -of test_U_ES-EN_output.txt -ofh test_U_ES-EN_output.txt.html -oft test_U_ES-EN_output.tmx.txt -ofth test_U_ES-EN_output.tmx.html -align  hunalign -dict </code></pre> )

[//]: # ( <pre><code>java -jar ilsp-fc-2.2-jar-with-dependencies.jar crawlandexport -f -a abumatran -type p -align maligna -l1 en -l2 fr -u seed_examples.txt -filter ".*(nrcan|rncan).*" -n 2 -xslt -oxslt -of output_demo_EN-FR.txt -ofh output_demo_EN-FR.txt.html -oft output_demo_EN-FR.tmx.txt -ofth output_demo_EN-FR.tmx.html </code></pre>)

[//]: # ( ## Output )

[//]: # (The output of the ilsp-fc in the case of a monolingual crawl consists of: )
[//]: # (* a list of links pointing to XML files following the cesDOC Corpus Encoding Standard (http://www.xces.org/). See this "cesDoc":http://nlp.ilsp.gr/xslt/ilsp-fc/1.xml file for an example in English for the _Environment_ domain. )
[//]: # (* a list of links pointing to HTML files (by XSL transformation of each XML) for easier browsing of the collection. As an example, see this "rendered cesDoc":http://nlp.ilsp.gr/xslt/ilsp-fc/1.xml.html file. )

[//]: # (The output of the ilsp-fc in the case of a bilingual crawl consists of: )
[//]: # (* a list of links to XML files following the cesAlign Corpus Encoding Standard for linking cesDoc documents. This example "cesAlign":http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.xml file serves as a link between a detected pair of cesDoc documents in "English":http://nlp.ilsp.gr/xslt/ilsp-fc/98.xml and "Spanish":http://nlp.ilsp.gr/xslt/ilsp-fc/44.xml.)
[//]: # (* a list of links pointing to HTML files (by XSL transformation of each cesAlign XML) for easier browsing of the collection. As an example, see this "rendered cesAlign":http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.xml.html file.)
[//]: # (* a list of links to TMX files containing sentence alignments that have been extracted from the detected document pairs. As an example, see this "TMX":http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.tmx file.)
[//]: # (* a list of links pointing to HTML files (by XSL transformation of each TMX) for easier browsing of the collection. As an example, see this "rendered TMX":http://nlp.ilsp.gr/xslt/ilsp-fc/44_98_i.html file.)
-->

## Running modules of the ILSP-FC

The ILSP-FC, in a configuration for acquiring parallel data,  applies the following processes (one after the other):
* [[Crawl|Crawl]]
* [[Export|Export]] 
* [[NearDeduplication|Near Deduplication]]
* [[PairDetection|Pair Detection]]
* [[SegmentAlignment|Segment Alignment]]
* [[TMXmerging|TMX Merging]] 
