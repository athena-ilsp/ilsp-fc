# Export

After crawling, the export process comes. The Exporter module generates a cesDoc file for each stored web document. Each file contains metadata (e.g. language, domain, URL, etc.) about the corresponding document inside a header element.  Moreover, a `<body>` element contains the content of the document segmented in paragraphs. Apart from normalized text, each paragraph element `<p>` is enriched with attributes providing more information about the process outcome. Once a crawl has finished, the acquired data can be exported with:

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-jar-with-dependencies.jar \
-export -lang "en;it" -len 0 -mtlen 100 \
-i (crawlpath up to the auto-generated dir) -tc (full path of topic file) \
-dom (title of targeted topic) -bs (fullpath and basename on which all files for easier content navigation will be generated) \
-oxslt  &>"/var/www/html/tests/eng-ita/log-export_www_esteri_it_eng-ita"
```

## Options

```console
-export :     for exporting process

-i      :     crawlpath up to the auto-generated dir by the crawl module

-lang   :     two or three letter ISO code(s) of target language(s), 
              e.g.  el (for a monolingual crawl for Greek content) or en;el (for a bilingual crawl)
              CesDoc files will be generated only for crawled web documents that are in the targeted language(s)

-tc     :     fullpath of topic file (a text file that contains a list of term triplets that describe the targeted topic).

-dom    :     title of the targeted domain (required when domain definition, i.e. tc parameter, is used).

-bs     :     Basename to be used in generating all files for easier content navigation

-oxslt  :     Export crawl results with the help of an xslt file for better examination of results.

-len    :     Minimum number of tokens per paragraph. If the length (in terms of tokens) of a paragraph is
              less than this value the paragraph will be annotated as "out of interest" 
              and will not be included into the clean text of the web page.

-mtlen  :     Minimum number of tokens in cleaned document. If the length (in terms of tokens) of the cleaned text
              is less than this value, the document will not be stored.

```

The tool will create the directory "xml" next to the "run" directories. In this directory the downloaded documents (html, pdf) and the generated XML files will be stored. Each file is named by the language iso code (i.e. eng for an English document) followed by a unique id (e.g. eng-1.xml and eng-1.html, eng-2.xml and eng-2.html, etc.)