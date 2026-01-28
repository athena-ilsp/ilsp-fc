# Crawl

In general, the crawler initializes its frontier (i.e. the list of pages to be visited) from a seed URL list, fetches the web pages, extracts links from fetched web pages, adds the links to the list of pages to be visited and so on. During this process, modules for page fetching, content normalization, boilerplate removal, metadata extraction, text classification,  and link extraction and prioritization are used. Users can configure several settings that determine the fetching process or the strictness of the text classifier by modifying the default configuration file [FBC_config.xml](FBC_config.md), when crawl for multilingual data, or [FMC_config.xml](FBC_config.md) for acquiring monolingual data. 

The required input from the user consists of a list of seed URLs to initiate the crawler's frontier, and a list of terms that describe a targeted topic. If the user does not provide a list of terms, the software can be used as a general crawler.

The following example starts a new crawl for acquiring multilingual data. In the defined destination (argument of option -dest), a directory (the crawl directory) is created (its name is based on the argument of option -a and date). 
In this crawl directory a child directory (auto-generated directory) denoted the crawl id. Next in this auto-generated directory, the data acquired in each crawl cycle is stored in run directories.

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.3-SNAPSHOT-jar-with-dependencies.jar -crawl -f -k -a test -type p \
-lang "L1;L2;L3;L4" -cdl 2 -t 20 -len 0 -mtlen 100 -dest (fullpath of the destination to store crawl results)  -u (fullpath of the text file containing the seed URLs) \
-filter (regex to control URLs to be visited) -tc (full path of topic file) -dom (title of targeted topic) &>"log_crawl"
```

By changing arguments of options -type and -lang to "m" and "L1" respectively, the command could be used for acquiring monolingual data.

This is an example of running a crawl: 

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-jar-with-dependencies.jar \
-crawl -f -type p -lang "eng;ita" -a www_esteri_it -u "/home/user/seeds/eng-ita-seeds" -filter ".*www\.esteri\.it\/.*" \
-cdl 100 -t 20 -len 0 -mtlen 100 -k -dest "/var/www/html/tests/eng-ita/" &> "/var/www/html/tests/eng-ita/log-crawl_www_esteri_it_eng-ita" 

```

## Options

```console
-crawl     :     For applying crawling process.

-f         :     Forces the crawler to start a new job.

-type      :     The type of crawling. Crawling for monolingual (m) or parallel (p).

-lang      :     The language iso codes of the targeted languages separated by ";".

-cfg       :     The full path to a configuration file that can be used to override default parameters.

-a         :     User agent name. It is proposed to use a name similar to the targeted site.

-u         :     fullpath of text file that contains the seed URLs that will initialize the crawler.
                 In case of bilingual crawling	the list should contain the URL of the main page of the targeted website,
                 or (of course) other URLs of this website.

-filter    :     A regular expression to filter out URLs which do NOT match this regex.
                 The use of this filter forces the crawler to either focus on a specific web domain (i.e. ".*ec.europa.eu.*"),
                 or on a part of a web domain (e.g.".*/legislation_summaries/environment.*") or in different web sites (i.e. 
                 in cases the translations are in two web sites e.g. http://www.nrcan.gc.ca and http://www.rncan.gc.ca).
                 Note that if this filter is used, only the seed URLs that match this regex will be fetched.

-cdl         :     The crawl duration in cycles. Since the crawler runs in cycles (during which links stored at the top
                 of the crawler’s frontier are extracted and new links are examined) it is proposed to use this parameter either
                 for testing purposes or selecting a large number (i.e. 100) to "verify" that the crawler will visit the entire website.

-dest      :     The directory where the results (i.e. the crawled data) will be stored.
                 The tool will create the file structure dest/agent/crawl-id (where dest and agent stand for the arguments
                 of parameters dest and agent respectively and crawl-id is generated automatically). In this directory,
                 the tool will create the "run" directories (i.e. directories containing all resources
                 fetched/extracted/used/required for each cycle of this crawl.
                 In addition a pdf directory for storing acquired pdf files will be created.

-t         :     The number of threads that will be used to fetch web pages in parallel.

-k         :     Forces the crawler to annotate boilerplate content in parsed text.

-len       :     Minimum number of tokens per paragraph. If the length (in terms of tokens) of a paragraph
                 is less than this value the paragraph will be annotated as "out of interest"
                 and will not be included into the clean text of the web page.

-mtlen     :     Minimum number of tokens in cleaned document. If the length (in terms of tokens)
                 of the cleaned text is less than this value, the document will not be stored.

-tc        :     fullpath of topic file (a text file that contains a list of term triplets that describe the targeted topic).
                 An example domain definition of "Environment" for the English-Spanish pair can be found at
                 http://nlp.ilsp.gr/redmine/projects/ilsp-fc/wiki/ENV_EN_ES_topic. If omitted,
                 the crawl will be a "general" one (i.e. module for text-to-domain classification will not be used).

-dom       :     Title of the targeted domain (required when domain definition, i.e. tc parameter, is used).

-storefilter  :  A regular expression to discard (i.e. visit/fetch/process but do not store) webpages with URLs which do NOT match this regex.

```
