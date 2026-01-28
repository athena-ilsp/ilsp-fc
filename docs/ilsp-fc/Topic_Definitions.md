# Topic Definitions

A topic definition in the context of ILSP-FC is a list of terms that "define" a topic. This list is provided at runtime in a text file where each line contains a term with the following fields:

* weight (an integer, see below) for the term
* a `:` separator
* a (multi-word) term to be searched for in a web document fetched by the crawler (in its main, non-boilerplate content, but also in its title and keywords)
* a `=` separator
* a string for one or more topics that the term corresponds to, separated by ";"

If the targetted languages are more than 1, i.e. if you run a multilingual crawl, the following fields are also needed

* a ">" separator
* a 2-digit iso language code for the language of the term

This list is an external resource that must be created before running the crawl. 

One way to create it is to search for an already available term list (in, for example, the [Eurovoc](http://eurovoc.europa.eu/) multilingual thesaurus) and manually modify it according to the format above. One issue with this approach is that  appropriate weights  should be manually assigned to each term. A simple heuristic is to assign the same weight (e.g. 100) to each term and assign lower weights to terms that may be ambiguous, i.e. terms that may correspond to multiple topics.

Here's an extract from a definition for the environment topic that can be used for a Greek monolingual crawl.

```
70:"πράσινο" σήμα=περιβάλλον
70:άγρια ζώα=περιβάλλον
100:άγρια φυτά και ζώα=περιβάλλον
70:άγριο θηλαστικό=περιβάλλον
50:αγροτική καταστροφή=περιβάλλον
50:άδεια για κυνήγι=περιβάλλον
50:άδεια θήρας=περιβάλλον
50:άδεια κυνηγίου=περιβάλλον
50:άδεια μπαταρία=περιβάλλον
100:άδεια ρύπανσης=περιβάλλον
25:αειφόρος ανάπτυξη=περιβάλλον
25:αέρια εξάτμισης αυτοκινήτων=περιβάλλον
100:αέριο που προκαλεί το φαινόμενο του θερμοκηπίου=περιβάλλον
100:αέριο που φθείρει το στρώμα του όζοντος=περιβάλλον
```

At runtime, the crawler's classifier will be initialized with a stemmed and normalized version of this topic definition: 


```
70     πρασιν σημ    περιβάλλον    ell    "πράσινο" σήμα
70     αγρ ζωα       περιβάλλον    ell    άγρια ζώα
100    αγρ φυτ ζωα   περιβάλλον    ell    άγρια φυτά και ζώα
70     αγρι θηλαστ   περιβάλλον    ell    άγριο θηλαστικό
50     αγροτικ καταστροφ    περιβάλλον    ell    αγροτική καταστροφή
50     αδει θηρ      περιβάλλον    ell    άδεια θήρας
50     αδει κυνηγ    περιβάλλον    ell    άδεια για κυνήγι
50     αδει μπαταρ   περιβάλλον    ell    άδεια μπαταρία
100    αδει ρυπανσ   περιβάλλον    ell    άδεια ρύπανσης
25     αειφορ αναπτυξ       περιβάλλον    ell    αειφόρος ανάπτυξη
25     αερ εξατμισ αυτοκινητ      περιβάλλον    ell    αέρια εξάτμισης αυτοκινήτων
100    αερι προκαλ φαινομεν θερμοκηπ     περιβάλλον    ell    αέριο που προκαλεί το φαινόμενο του θερμοκηπίου
100    αερι φθειρ στρωμ οζοντ     περιβάλλον    ell    αέριο που φθείρει το στρώμα του όζοντος
```

The main content of each fetched web document in Greek, will also be stemmed and normalized. Stemmed terms will be searched for in the normalized content and a score will be calculated as in section 3.5, Text Classifier of this [paper](http://aclweb.org/anthology/W/W13/W13-2506.pdf). A number of thresholds that affect the way this score is calculated can be defined in the crawl's configuration file:

```xml
<classifier>
  <min_content_terms>
    <value>4</value>
    <description>Minimum number of terms that must exist in clean
  content of each web page in order to be stored.This number
  is multiplied with the median value of the terms'weights and
  the result is the threshold for the absolute relevance score.</description>
  </min_content_terms>
  <min_unique_content_terms>
    <value>4</value>
    <description>Minimum unique terms that must exist in clean content</description>
  </min_unique_content_terms>
  <relative_relevance_threshold>
    <value>0.2</value>
    <description>The absolute relevance score is divided by the length
  (in terms of tokens) of the clean content of a document and the
  calculated relative relevance score is compared with this value</description>
  </relative_relevance_threshold>
</classifier>
```