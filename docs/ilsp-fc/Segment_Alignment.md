# Segment Alignment


It uses maligna aligner for identifying segment pairs from each detect document pair. It generates a TMX file for each cesAlign file (e.g. eng-12_ell-18_x.tmx for eng-12_ell-18_x.xml).

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-jar-with-dependencies.jar \
-align -lang "L1;L2" -i (fullpath of dir with the generated cesAlign) \
-bs (fullpath and basename on which all files for easier content navigation will be generated) \
-oxslt &>"/var/www/tests/log-align"
```

## Options

```console
-align	: for segment alignment

-i      : crawlpath up to the auto-generated dir by the crawl module

-lang   : two or three letter ISO code(s) of target language(s), 
          e.g.  el (for a monolingual crawl for Greek content) or en;el (for a bilingual crawl)
          CesDoc files will be generated only for crawled web documents that are in the targeted language(s)

-bs     : Basename to be used in generating all files for easier content navigation

-oxslt  : Export crawl results with the help of an xslt file for better examination of results.
```