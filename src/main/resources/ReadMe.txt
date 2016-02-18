

///////////////////////////////////////////////////////////////
Regarding TILDE experiments with ILSP-FC:
Thank you very much for your feedback. It is always nice to have comments for this tool, especially by teams that have worked in data acquisition.
First of all, we would like to mention that the version of the ILSP-FC which is available at http://redmine.ilsp.gr/redmine/projects/ilsp-fc/files is "far behind" of the current version of the tool (in many tasks such as content extraction, identification of pairs of parallel documents, segment alignment and filtering/selecting document pairs or segment pairs).
Even the version that we used for the initial ELRC experiments in EN-LV has been changed.
For example, some of the integrated pair detection methods have been judged reliable (based on previous experiments and evaluation tasks), while others are used for testing/enhancing the tool. The filenames of the pairs that were identified by "good" methods end with one of the letters a,u,p,d,i,h (e.g. eng-1_lav-5_i.xml for such a document pair and eng-1_lav-5_i.tmx for the resulted TMX), while the pairs detected by "under review" methods have the letter m or l. To this end, we recently integrated an option for selecting the "types" of document pairs from which  the final output will be constructed.
#Although the current version is being constantly changed and tested, we aim to make a release very soon.
About the results:
The results of eu2015.lv and makroekomika.lv are absolutely explainable:
Regarding eu2015, all document pairs were identified by methods which are considered reliable. So having found correct pairs of document pairs, the segment pairs are expected to be also correct.
Regarding makroekomika, 236 out of 391 document pairs were identified by "l" or "m" methods. We know that these methods result many errors and so it is proposed not to include their TUs in the final output. 

Note that www.makroekonomika.lv is the latvian part, while www.macroeconomics.lv includes the English part. So you should -filter ".*www\.(makroekonomika|macroeconomics)\.lv.*" to let crawler visit/store web pages from both websites. This parameter could be also used to crawl branches of a website (e.g. (i.e. use ".*/legislation_summaries/environment.*" to stay on a part of the ".*ec.europa.eu.*" web site).
We crawled www.riga.lv (20 cycles) and ...

////////////////////////////////////////////////////////////////
Regarding acquisition of parallel data in ELRC:
- domainness
Can we find many websites that are relevant to DSIs?
Could we consider that data from such websites are relevant to DSIs without applying domain classification?
--If YES in both questions, we could use ILSP-FC with no need of other resources (i.e. only the thematic websites).
By using -dom parameter we can "pass" the its argument to "domain" tag of the cesDoc (Απαπαπαπα) 
--If NO, ILSP-FC requires a list of term triplets (<relevance,term,subtopic>) that describe a domain (i.e. this list is required in case the user aims to acquire domain-specific documents) and, optionally, subcategories of this domain in both the targeted languages. By using EUROVOC identifiers we might be able to create such a list, but we have to check (i.e. terms for OPEN DATA, or EUROPEANA are available?) 
- License
We detect CC license(s) in web pages and annotate them properly.

///////////////////////////////////////////////////////////////
The ILSP-FC, in a configuration for acquiring parallel data, applies the following processes (one after the other):
- crawls a website with content in the targeted languages (i.e. harvests the website and stores pages that are in the targeted languages)
- exports the stored data (i.e. stores downloaded webpage/document and generates a CesDoc file with its content and metadata). Each file is named by the language iso code (i.e. eng for an English document) followed by a unique id (e.g. eng-1.xml and eng-1.html, eng-2.xml and eng-2.html, ..., lav-1.xml and lav-1.html, etc.).
- discards (near) duplicate documents
- identifies pairs of (candidate) parallel documents by using several methods (Note that some of these methods have been judged reliable, while others are used for testing/enhancing the tool.) For each detected pair, a cesAling file is generated. The filename of this file consists of the filenames of the paired documents and the identifier of the method which provided this pair (e.g. eng-12_lav-24_x.xml, where x stands for a, u, p, i, d, h, m and l). (This file holds references to the paired documents and the method which provided this pair.)
- aligns segments in each detected document pair and generates a TMX for each document pair (e.g. eng-12_lav-24_x.tmx)
- merges the generated TMX files in order to create the final output (i.e. a TMX that includes all the segment pairs)
With the current version of ILSP-FC a user could ask for executing all mentioned processes in a row, or some of them , or just one of them.

This is an example for running the whole pipeline (domainness is not examined):
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar -crawl -export -dedup -pairdetect -align -tmxmerge -f -a riga_lv_eng-lav -u "/home/ELRC/tests/eng-lav-seeds" -type p -lang "eng;lv" -filter ".*riga\.lv.*" -n 100 -t 20 -k -len 0 -mtlen 100 -dest "/var/www/html/elrc4/tests/eng-lav/" -xslt -oxslt -doctypes "auidh" -segtypes "1:1" -of "/var/www/html/elrc4/tests/eng-lav/output_riga_lv_eng-lav.txt" -ofh "/var/www/html/elrc4/tests/eng-lav/output_riga_lv_eng-lav.html" -oft "/var/www/html/elrc4/tests/eng-lav/output_riga_lv_eng-lav.tmx.txt" -ofth "/var/www/html/elrc4/tests/eng-lav/output_riga_lv_eng-lav.tmx.html" -tmx "/var/www/html/elrc4/tests/eng-lav/output_riga_lv_eng-lav.tmx" &> "/var/www/html/elrc4/tests/eng-lav/log_riga_lv_eng-lav"

-crawl		:	for crawling process. No argument is required
-export		:	for exporting process. No argument is required
-dedup		:	for (near) deduplication.  No argument is required
-pairdetect	:	for identification of pairs of parallel documents. No argument is required
-align		:	for segment alignment.  No argument is required
-tmxmerge	:	for merging generated TMX
-f			:	Forces the crawler to start a new job.
-a			:	user agent name (required)
-u			:	the text file that contains the seed URLs that will initialize the crawler. In case of bilingual crawling
				the list should contain the URL of the main page of the targeted website, or (of course) other URLs of this website.
-type		:	the type of crawling. Crawling for monolingual (m) or parallel (p).
-lang		:	the language iso codes of the targeted languages separated by ";" (required).
-filter		:	A regular expression to filter out URLs which do NOT match this regex.
				The use of this filter forces the crawler to either focus on a specific web domain (i.e. ".*ec.europa.eu.*"), or on a part of a web domain (e.g.".*/legislation_summaries/environment.*"). Note that if this filter is used, only the seed URLs that match this regex will be fetched.
-n			:	the crawl duration in cycles. Since the crawler runs in cycles (during which links stored at the top of 
				the crawler’s frontier are extracted and new links are examined) it is proposed to use this parameter either for testing purposes or selecting a large number (i.e. 100) to "verify" that the crawler will visit the entire website.
-t			:	the number of threads that will be used to fetch web pages in parallel.
-k			:	Forces the crawler to annotate boilerplate content in parsed text.
-len		:	Minimum number of tokens per paragraph. If the length (in terms of tokens) of a paragraph is 
				less than this value the paragraph will be annotated as "out of interest" and will not be included into the clean text of the web page.
-mtlen		:	Minimum number of tokens in cleaned document. If the length (in terms of tokens) of the cleaned 
				text is less than this value, the document will not be stored.
-dest		:	The directory where the results (i.e. the crawled data, exported data, etc.) will be stored.
-xslt		:	Insert a stylesheet for rendering xml results as html.
-oxslt		:	Export crawl results with the help of an xslt file for better examination of results.
-doctypes	:	Defines the types of the document pairs from which the segment pairs will be selected. The proposed value is "aupidh"
				since pairs of type "m" and "l" (e.g. eng-1_lav-3_m.xml or eng-2_lav-8_l.xml) are only used for testing or examining the tool.
-segtypes	:	Types of segment alignments that will be selected for the final output. The value "1:1" is proposed.
-of			:	A text file containing a list with the exported XML files (cesDoc files after exporting, cesAlign files after
				pairdetection).
-ofh		:	An HTML file containing a list with the generated XML files.
-oft		:	A text file containing a list with the exported TMX files.
-ofth		:	An HTML file containing a list with the generated TMX files.
-tmx		:	A TMX files that includes filtered segment pairs of the generated TMX. This is the final output of the process
				(i.e. the parallel corpus) 

				
				
				
//////////////////////////////////////////////
Crawling

In order to run a crawl the user should create a text file with the seed URLs (i.e. the URL of the main page of the targeted website, or (of course) more URLs of this website).
This is an example of running a crawl (see section Settings for the explanation of each parameter):
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar -crawl -f -k -type p -n 100 -t 20 -len 0 -mtlen 100 -lang "eng;lv" -a riga_lv_eng-lav -filter ".*riga\.lv.*" -u "/home/vpapa/ELRC/tests/eng-lav-seeds" -dest "/var/www/html/elrc4/tests/eng-lav/" &> "/var/www/html/elrc4/tests/eng-lav/log_riga_lv_eng-lav"

The tool will create the file structure dest/agent/crawl-id (where dest and agent stand for the arguments of parameters dest and agent respectively and crawl-id is generated automatically). In this directory, the tool will create the "run" directories (i.e. directories containing all resources fetched/extracted/used/required for each cycle of this crawl.
////////////////////////////////////////////////////
Export

Given that a crawl has finished, you can ask for exporting the acquired data 
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar -export  -i "/var/www/html/elrc4/tests/eng-lav/riga_lv_eng-lav_20160204_152734/d85041bb-9cb2-443e-8e5b-e43b9e168589/" -lang "eng;lav" -of "/var/www/html/elrc4/tests/eng-lav/output_riga_lv_eng-lav.txt"  &>"/var/www/html/elrc4/culture/eng-fra/log-export_riga_lv_eng-lav"

The tool will create the directory "xml" next to the "run" directories. In this directory the downloaded documents (html, pdf) will be stored. For each stored file,  a cesDoc file containing its content and metadata will be generated. Each file is named by the language iso code (i.e. eng for an English document) followed by a unique id (e.g. eng-1.xml, eng-2.xml, etc.)
We could also add parameters -len and -mtlen for "filtering" very short paragraphs or documents.
By using -dom parameter we can "pass" the its argument to "domain" tag of the cesDoc (Απαπαπαπα).
A text file with paths of the generated cesDoc files is also created ( see -of). We could also add parameter -xslt for creating the xml.html files (rendered xml results as html) and -oxslt for generating a list of links pointing to the xml.html files. Then we need the -ofh parameter to define the html file which will include the list of links.

///////////////////////////////////////////////////
(near) Deduplication
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar -dedup -o "./chateaudesreaux-fr_eng-fra_20160117_035615/a78cfeb5-e044-44f8-b531-c0aacf7a3ad8/xml" -lang "eng;fra" -of XXX 
Examines the cesDoc in a directory and removes (near)duplicates. For -of, -ofh, oxslt see above. 

//////////////////////////////////////////////////
Pair detection
Based on website graph, specific patterns in URLs, occurrences of common images, similarity of sequences of digits and similarity of structure, it detects pairs of parallel documents. (Note that some of these methods have been judged reliable, while others are used for testing/enhancing the tool.) For each detected pair, a cesAling file is generated. The proposed value is "aupidh" since pairs of type "m" and "l" (e.g. eng-1_lav-3_m.xml or eng-2_lav-8_l.xml) are only used for testing or examining the tool.
java -cp /opt/ilsp-fc/ilsp-fc-2.2.2-jar-with-dependencies.jar gr.ilsp.fc.bitext.PairDetector -meth "aupids" -i "/var/www/html/elrc3/culture/eng-fra/chateau-goulaine-online-fr_eng-fra_20160114_021625/ef71361a-9f13-4764-83c6-ad7fad8f3cb2/xml" -lang "eng;fra" -o "/var/www/html/elrc3/culture/eng-fra/chateau-goulaine-online-fr_eng-fra_20160114_021625/ef71361a-9f13-4764-83c6-ad7fad8f3cb2/xml" -of "/var/www/html/elrc3/culture/eng-fra/output_chateau-goulaine-online-fr_eng-fra.txt" &>"/var/www/html/elrc3/culture/eng-fra/log-pairdetection_chateau-goulaine-online-fr_eng-fra"

/////////////////////////////////////////////////////
Alignment

/////////////////////////////////////////////////////
TMX merging

/////////////////////////////////////////////////////
Settings

-crawl : crawling will be run. No argument is required
-f		:	Forces the crawler to start a new job (required).
-a		:	user agent name (required)
-type	:	the type of crawling. Crawling for monolingual (m) or parallel (p).
-lang	:	the language iso codes of the targeted languages separated by ";" (required).
-u		:	the text file that contains the seed URLs that will initialize the crawler. In case of bilingual crawling
			the list should contain only 1 or 2 URLs from the same web doamin.  
-filter	:	A regular expression to filter out URLs which do NOT match this regex.
			The use of this filter forces the crawler to either focus on a specific 
			web domain (i.e. ".*ec.europa.eu.*"), or on a part of a web domain 
			(e.g.".*/legislation_summaries/environment.*"). Note that if this filter
			is used, only the seed URLs that match this regex will be fetched.
-c		:	the crawl duration in minutes. Since the crawler runs in cycles (during which links stored at the top of 
			the crawler’s frontier are extracted and new links are examined) it is very likely that the defined time
			will expire during a cycle run. Then, the crawler will stop only after the end of the running cycle.
-n		:	the crawl duration in cycles. It is proposed to use this parameter either for testing purposes or 
     selecting a large number (i.e. 100) to verify that the crawler will visit the entire website.
-tc		:	domain definition (a text file that contains a list of term triplets that describe the targeted
			domain). If omitted, the crawl will be a "general" one (i.e. module for text-to-domain
			classification will not be used). 
-dom	:	the name of targeted domain/topic
-k		:	Forces the crawler to annotate boilerplate content in parsed text.
-len	:	Minimum number of tokens per paragraph. If the length (in terms of tokens) of a paragraph is 
			less than this value (default is 3) the paragraph will be annotated as "out of interest" and
			will not be included into the clean text of the web page.
-mtlen	:	Minimum number of tokens in cleaned document. If the length (in terms of tokens) of the cleaned 
			text is less than this value (default is 200), the document will not be stored.







-cfg : the configuration file that will be used instead of the default (see crawler_config.xml above). 






-u_r : This parameter should be used for bilingual crawling when there is an already known pattern in URLs
       which implies that one page is the candidate translation the other. It includes the two strings
       to be replaced separated by ';'.
-d : Forces the crawler to stay in a web site (i.e. starts from a web site and extracts only links to pages
     inside the same web site). It should be used only for monolingual crawling.

-align : Name of aligner to be used for sentence alignment (default is maligna).
-dict :  A dictionary for sentence alignment if hunalign is used. The default L1-L2 dictionary of hunalign will be used if it 
        exists.

-dom : Title of the targeted domain (required when domain definition, i.e. tc parameter, is used).

-of : A text file containing a list with the exported XML files (see section Output below).
-ofh : An HTML file containing a list with the generated XML files (see section Output below).
-oft : A text file containing a list with the exported TMX files (see section Output below).
-ofth : An HTML file containing a list with the generated TMX files (see section Output below).
 