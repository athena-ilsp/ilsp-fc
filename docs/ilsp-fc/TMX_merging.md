# TMX merging

It merges generated TMX and creates the final TMX which is considered as the final output (i.e. the bilingual corpus). Filtering of segment pairs is supported since targeted types of document pairs and segment can be selected. It also extracts metadata of the final corpus.  

```console
java -Dlog4j.configuration=file:/opt/ilsp-fc/log4j.xml -jar /opt/ilsp-fc/ilsp-fc-2.2.4-SNAPSHOT-jar-with-dependencies.jar -tmxmerge -lang "L1;L2" \ 
-i (input) -oxslt -pdm "aupdih" -segtypes "1:1" -bs (baseName for output files) &>"log-tmxmerge"
```

## Options


```console
-tmxmerge   :     for merging generated TMX files (i.e. construct a bilingual corpus).

-i          :     fullpath of input file/directory. It could be either a directory which contains the TMX files to be merged,
                  or a text file with fullpaths of such directories (one directory per textline)

-pdm        :     Defines the types of the document pairs from which the segment pairs will be selected.
                  The proposed value is "aupidh" since pairs of type "m" and "l" (e.g. eng-1_lav-3_m.xml or eng-2_lav-8_l.xml)
                  are only used for testing or examining the tool.

-thres      :     thresholds for 0:1 alignments per type. It should be of the same length with the types parameter. If a TMX of type X contains
                  more 0:1 segment pairs than the corresponding threshold, it will not be selected

-segtypes   :     Types of segment alignments that will be selected for the final output. A suggested value is "1:1".
                  Multiple segment types can be separated by ";" (e.g. 1:1;1:2;2:1).

-oxslt      :     Apply an xsl transformation to generate html file during exporting.

-cc         :     If exists, only document pairs for which a license has been detected will be selected in merged TMX.

-cfg        :     The full path to a configuration file that can be used to override default parameters.

-keepdup    :     keeps duplicate TUs, and annotates them

-keepem     :     keeps TUs, even if one of its TUV does not contain any letter, and annotates them

-keepiden   :     keeps TUs, even if its TUVs are identical after removing non-letters, and annotates them

-ksn        :     keeps only TUs with same digits

-maxlr      :     maximum ratio of length (in chars) in a TU

-minlr      :     minimum ratio of length (in chars) in a TU

-mpa        :     minimum percentage of 0:1 alignments in a TMX, to be accepted

-mtuvl      :     minimum length in tokens of an acceptable TUV

-iso6393    :     if exists three language codes are used. Otherwise, two-letter language codes are used in the generated TMX files.

```
