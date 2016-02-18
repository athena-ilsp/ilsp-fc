package gr.ilsp.fc.datums;

import gr.ilsp.fc.parser.ExtendedOutlink;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


import bixo.datum.UrlDatum;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

@SuppressWarnings("serial")
public class ExtendedParsedDatum extends UrlDatum {

	//private static final Logger logger = LoggerFactory.getLogger(ExtendedParsedDatum.class);
	
	private static final String HYPHEN_STR = "-";
	public static final String HOST_ADDRESS_FN = fieldName(ExtendedParsedDatum.class, "hostAddress");
    public static final String PARSED_TEXT_FN = fieldName(ExtendedParsedDatum.class, "parsedText");
    public static final String LANGUAGE_FN = fieldName(ExtendedParsedDatum.class, "language");
    public static final String TITLE_FN = fieldName(ExtendedParsedDatum.class, "title");
    public static final String OUTLINKS_FN = fieldName(ExtendedParsedDatum.class, "outLinks");
    public static final String TRANSLATIONOUTLINKS_FN = fieldName(ExtendedParsedDatum.class, "translationOutLinks");
    public static final String PARSED_META_FN = fieldName(ExtendedParsedDatum.class, "parsedMeta");

    public static final Fields FIELDS = new Fields(HOST_ADDRESS_FN, PARSED_TEXT_FN, LANGUAGE_FN, 
                    TITLE_FN, OUTLINKS_FN, TRANSLATIONOUTLINKS_FN, PARSED_META_FN).append(getSuperFields(ExtendedParsedDatum.class));

    /**
     * No argument constructor for use with FutureTask
     */
    public ExtendedParsedDatum() {
        super(FIELDS);
    }
    
    public ExtendedParsedDatum(TupleEntry tupleEntry) {
        super(tupleEntry);
        validateFields(tupleEntry, FIELDS);
    }
    
    public ExtendedParsedDatum(String url, String hostAddress, String parsedText, String language, String title, 
    		ExtendedOutlink[] outlinks, Map<String, String> parsedMeta) {
        super(FIELDS);
        
        setUrl(url);
        setHostAddress(hostAddress);
        setParsedText(parsedText);
        setLanguage(language);
        setTitle(title);
       	setOutlinks(outlinks);
       	setTranslationOutlinks(outlinks);
        setParsedMeta(parsedMeta);
    }

    public String getHostAddress() {
        return _tupleEntry.getString(HOST_ADDRESS_FN);
    }

    public void setHostAddress(String hostAddress) {
        _tupleEntry.set(HOST_ADDRESS_FN, hostAddress);
    }

    public String getParsedText() {
        return _tupleEntry.getString(PARSED_TEXT_FN);
    }

    public void setParsedText(String parsedText) {
        _tupleEntry.set(PARSED_TEXT_FN, parsedText);
    }

    public String getLanguage() {
        return _tupleEntry.getString(LANGUAGE_FN);
    }

    public void setLanguage(String language) {
        _tupleEntry.set(LANGUAGE_FN, language);
    }

    public String getTitle() {
        return _tupleEntry.getString(TITLE_FN);
    }

    public void setTitle(String title) {
        _tupleEntry.set(TITLE_FN, title);
    }

    public ExtendedOutlink[] getOutlinks() {
        return convertTupleToOutlinks((Tuple)_tupleEntry.get(OUTLINKS_FN));
    }

    public ExtendedOutlink[] getTranslationOutlinks() {
        return convertTupleToOutlinks((Tuple)_tupleEntry.get(TRANSLATIONOUTLINKS_FN));
    }
    
    public void setOutlinks(ExtendedOutlink[] outlinks) {
        _tupleEntry.set(OUTLINKS_FN, convertOutlinksToTuple(outlinks));
    }

    public void setTranslationOutlinks(ExtendedOutlink[] outlinks) {
        _tupleEntry.set(TRANSLATIONOUTLINKS_FN, extractTranslationOutlinksToTuple(outlinks));
    }
    
	public Map<String, String> getParsedMeta() {
        return convertTupleToMap((Tuple)_tupleEntry.get(PARSED_META_FN));
    }

    public void setParsedMeta(Map<String, String> parsedMeta) {
        _tupleEntry.set(PARSED_META_FN, convertMapToTuple(parsedMeta));
    }

    private Tuple convertOutlinksToTuple(ExtendedOutlink[] outLinks) {
        Tuple tuple = new Tuple();
        for (ExtendedOutlink outlink : outLinks) {
            tuple.add(outlink.getToUrl());
            tuple.add(outlink.getAnchor());
            tuple.add(outlink.getSurroundText());
            tuple.add(outlink.getRelAttributes());    
            tuple.add(outlink.getHrefLang());
        }
        
        return tuple;
    }

    private Object extractTranslationOutlinksToTuple(ExtendedOutlink[] outLinks) {
        Tuple tuple = new Tuple();
        for (ExtendedOutlink outLink : outLinks) {
        	if ( ! outLink.getHrefLang().equals(HYPHEN_STR)) { // Has, for example, an "el" value
        		//FIXME, should we rank it?
        		tuple.add(outLink.getToUrl());
        		tuple.add(outLink.getAnchor());
        		tuple.add(outLink.getSurroundText());
        		tuple.add(outLink.getRelAttributes());    
        		tuple.add(outLink.getHrefLang());
        	}
        }
        
        return tuple;
	}

    
    private ExtendedOutlink[] convertTupleToOutlinks(Tuple tuple) {
        int numOutlinks = tuple.size() / 5;
        ExtendedOutlink[] result = new ExtendedOutlink[numOutlinks];
        for (int i = 0; i < numOutlinks; i++) {
            int tupleOffset = i * 5;
            result[i] = new ExtendedOutlink(tuple.getString(tupleOffset), tuple.getString(tupleOffset + 1), tuple.getString(tupleOffset + 2),tuple.getString(tupleOffset + 3), tuple.getString(tupleOffset + 4));
        }
        return result;
    }

    private Tuple convertMapToTuple(Map<String, String> map) {
        Tuple result = new Tuple();
        if (map != null) {
            for (Entry<String, String> entry : map.entrySet()) {
                result.add(entry.getKey());
                result.add(entry.getValue());
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> convertTupleToMap(Tuple tuple) {
        Map<String, String> result = new HashMap<String, String>();
        @SuppressWarnings("rawtypes")
		Iterator<Comparable> iter = tuple.iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            String value = (String)iter.next();
            result.put(key, value);
        }
        
        return result;
    }

    public static Fields getParsedTextField() {
        return new Fields(ExtendedParsedDatum.PARSED_TEXT_FN);
    }

}
