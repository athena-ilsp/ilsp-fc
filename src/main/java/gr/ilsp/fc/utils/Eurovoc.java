package gr.ilsp.fc.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * Enum class to load and query eurovoc domain/thesaurus-name/concept <-> id pairs from the eng eurovoc
 * 
 * @author prokopis
 *
 */
public enum Eurovoc {
	INSTANCE;

	private static Map<String, String> termIdEntries;
	private static Map<String, String> idTermEntries;

	private Eurovoc() {
		loadCSVTerms();
	}

	private void loadCSVTerms() {
		termIdEntries = new HashMap<String, String>();
		idTermEntries = new HashMap<String, String>();
		try {
			//System.out.println("Loading terms...");
			List<String> lines = IOUtils.readLines(this.getClass().getResourceAsStream("/eurovoc/eurovoc-eng-4.4.tsv"));
			for (String line : lines) {
				String[] fields = StringUtils.split(line, "\t");
				String id = fields[0];
				String term = fields[1];
				String lcTerm = fields[1].toLowerCase();

				if (term.startsWith("Domain-")) {
					termIdEntries.put(term, id);
					termIdEntries.put(lcTerm, id);
					idTermEntries.put(id, term);
					continue;
				}
				
				if (termIdEntries.containsKey(lcTerm) && (termIdEntries.get(lcTerm).startsWith("MicroThesaurus-") || termIdEntries.get(lcTerm).startsWith("Domain-"))) {
					continue;
				} else {
					termIdEntries.put(term, id);
					termIdEntries.put(lcTerm, id);
					idTermEntries.put(id, term);
				}
			}
		} catch (IOException e) {
			System.err.println("Cannot load eurovoc resource...");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the eurovoc identifier for the term, e.g. 317 for culture or CultUre or null
	 * @param term
	 * @return
	 */
	public String getIdentifier(String term){
		return termIdEntries.get(term.toLowerCase().trim());
	}

	/**
	 * Returns the canonical term for an identifier,  e.g. culture for 317, or null
	 * @param identifier
	 * @return
	 */
	public String getConceptLabel(String identifier){
		return idTermEntries.get(identifier);
	}

	/**
	 * Returns the canonical term, e.g. culture for CultUre
	 * 
	 * @param term
	 * @return 
	 */
	public String getCanonicalLabel(String term){
		return idTermEntries.get(termIdEntries.get(term.toLowerCase()));
	}

	
}