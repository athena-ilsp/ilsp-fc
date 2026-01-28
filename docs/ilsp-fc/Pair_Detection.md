# Pair Detection

It detects pairs of parallel documents based on website graph, specific patterns in URLs, occurrences of common images, similarity of sequences of digits and similarity of structure. For each detected pair, a cesAlign is generated. The basename of a cesAling file consists of the basenames of the paired documents and the identifier of the method which provided this pair (e.g. eng-12_ell-18_x.xml, where x stands for a, u, p, i, d, h, m and l). This file holds references to the paired documents.

```console
java -cp /opt/ilsp-fc/ilsp-fc-2.2.3-jar-with-dependencies.jar gr.ilsp.fc.bitext.PairDetector \
-pdm "aupidh" -lang "en;it" -oxslt  -i (crawlpath up to the auto-generated xml dir) \
-bs (fullpath and basename on which all files for easier content navigation will be generated)
 &>"/var/www/html/tests/eng-ita/log-pairdetect_www_esteri_it_eng-ita"
```

## Options

```console
-i      : crawlpath up to the auto-generated dir by the crawl module

-lang   : two or three letter ISO code(s) of target language(s), 
          e.g.  el (for a monolingual crawl for Greek content) or en;el (for a bilingual crawl)
          CesDoc files will be generated only for crawled web documents that are in the targeted language(s)

-pdm   :  methods to be used for pair detection. Put a string which contains a for checking links, 
          u for checking urls for patterns, p for combining common images and digits, i for using common images,
          d for examining digit sequences, h for examining structures.

-bs    :  Basename to be used in generating all files for easier content navigation

-oxslt :  Export crawl results with the help of an xslt file for better examination of results.

-ifp   :  image_fullpath. Keep image fullpath for pair detection for representing an image instead of its name only.

-u_r   :  url_replacements. Besides the default patterns, the user could add more patterns separated by ;

-del   :  delete redundant files. Deletes cesDoc files that have not been paired				

```