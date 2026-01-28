# Languages-supported

## Support for [EU languages](https://en.wikipedia.org/wiki/European_Union#Languages)

| Language        | Analyzer | Lang. detection | Abbrev. list |
|:----------------|:---------|:----------------|:-------------|
| Bulgarian       | Y        | Y               |              |
| Croatian        | Y        | Y               | Y            |
| Czech           | Y        | Y               | Y            |
| Danish          | Y        | Y               | Y            |
| Dutch           | Y        | Y               | Y            |
| English         | Y        | Y               | Y            |
| Estonian        |          | Y               |              |
| Finnish         | Y        | Y               | Y            |
| French          | Y        | Y               | Y            |
| German          | Y        | Y               | Y            |
| Greek           | Y        | Y               | Y            |
| Hungarian       | Y        | Y               | Y            |
| Italian         | Y        | Y               | Y            |
| Irish           | Y        | Y               |              |
| Latvian         | Y        | Y               |              |
| Lithuanian      | Y        | Y               |              |
| Maltese         |          | Y               | Y            |
| Polish          | Y        | Y               |              |
| Portuguese      | Y        | Y               | Y            |
| Romanian        | Y        | Y               |              |
| Slovak          |          | Y               |              |
| Slovene         | Y        | Y               |              |
| Spanish         | Y        | Y               | Y            |
| Swedish         | Y        | Y               | Y            |
| Catalan         | Y        | Y               |              |
| Galician        | Y        | Y               |              |
| Basque          | Y        | Y               |              |
| Scottish Gaelic |          |                 |              |
| Welsh           |          | Y               | |            |


To add another language:

* verify that the targeted language is supported by the default language identifier (https://code.google.com/p/language-detection/) integrated in ILSP-FC (or other identifiers available in the gr.ilsp.fc.langdetect package),
* add a textline with proper content in the [[langKeys.txt]] file included in the ilsp-fc runnable jar, and
* add a proper analyser in the gr.ilsp.fc.utils.AnalyserFactory class of the ilsp-fc source.

