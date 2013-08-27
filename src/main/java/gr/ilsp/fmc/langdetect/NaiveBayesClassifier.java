//#    Java port of the following python by Nikola Ljubešić:

//#    Naive Bayes classifier for language identification v0.1
//#    Copyright 2012 Nikola Ljubešić http://nlp.ffzg.hr/resources/tools/bs-hr-sr-lid/
//#
//#    This program is free software: you can redistribute it and/or modify
//#    it under the terms of the GNU Lesser General Public License as published
//#    by the Free Software Foundation, either version 3 of the License, or
//#    (at your option) any later version.
//#
//#    This program is distributed in the hope that it will be useful,
//#    but WITHOUT ANY WARRANTY; without even the implied warranty of
//#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//#    GNU Lesser General Public License for more details.
//#
//#    You should have received a copy of the GNU Lesser General Public License
//#    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package gr.ilsp.fmc.langdetect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class NaiveBayesClassifier extends LangDetector {

	private static final Logger logger = LoggerFactory.getLogger(NaiveBayesClassifier.class);

	private String jsonModel = "bs-hr-sr.classifier.json.gz";
	private HashMap<String, HashMap<String, Double>> parms;

	@Override
	protected void initialize()  {
		parms = deSerializeJsonClassifier(getClass().getClassLoader().getResource(jsonModel));
	}

	@Override
	protected String detect(String text) {
		return guessCategory(parms, text);
	}

	@Override
	protected HashMap<String, Double> detectLangs(String text) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void createNewLanguageProfile(String lang, File trainFile,
			File profileFile) throws Exception {
		// TODO Auto-generated method stub
	}
	
	private static HashMap<String, HashMap<String, Double>> deSerializeJsonClassifier(URL jsonModel) {
		HashMap<String, HashMap<String, Double>> parms = null;
		try {
			logger.debug("Reading model from json file.", jsonModel);
			GZIPInputStream gzip = new GZIPInputStream(jsonModel.openConnection().getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
			Gson gson = new Gson();
			Type stringStringMap = new TypeToken<HashMap<String, HashMap<String, Double>>>(){}.getType();
			parms =  gson.fromJson(br, stringStringMap);		
			br.close();
			gzip.close();	
		} catch (IOException e) {	
			logger.error("Cannot read model from json file.", e.getMessage());
		}
		return parms;
	}

	private String guessCategory(HashMap<String, HashMap<String, Double>> parms, String text) {
		HashMap<String, Double> distr = new HashMap<String, Double>(); 

		calculateDistr(text, distr);	

		Set<String> categories = parms.keySet();
		HashMap<String, Double> probabilities = new HashMap<String, Double>();
		for (String category : categories) {
			probabilities.put(category, 0.0);
		}		

		for (String feature : distr.keySet()) {  
			// if (!parms.get("hr").containsKey(feature)) {
				//TODO: TBFI
		        //continue;
			//}
			for (String category : categories) {
				double prev = probabilities.get(category);
				if (parms.get(category).containsKey(feature)) {
					probabilities.put(category, prev + (parms.get(category).get(feature)* distr.get(feature)));
				}
			}
		}

		Entry<String, Double> max = null;
		for (Entry<String, Double> entry : probabilities.entrySet()) {
			if (max == null || max.getValue() < entry.getValue()) {
				max = entry;
			}
		}
		return max.getKey();
	}
	

	private void calculateDistr(List<String> lines, HashMap<String, Double> distr) {
		for (String line : lines) {
			calculateDistr(line, distr);
		}
	}

	
	private static final String replaceAbles = "0123456789!@#$%^&*()-_+={}[]|\\:;'<>?,./\"`'“";

	private void calculateDistr(String line, HashMap<String, Double> distr) {
		line = StringUtils.replaceChars(line, replaceAbles, "");
		StringTokenizer st = new StringTokenizer(line);
		while (st.hasMoreElements()) {
			String word = (String) st.nextElement();
			if (word.toLowerCase().equals(word)) {
				if (distr.containsKey(word)) {
					distr.put(word, distr.get(word)+1);
				} else {
					distr.put(word, 1.0);
				}
			}
		}
	}	
	
	
	// Rest of methods for training, testing, etc...
	public static void main(String[] args) throws Exception {
		NaiveBayesClassifier naiveBayesClassifier = new NaiveBayesClassifier();
		naiveBayesClassifier.initialize();

		naiveBayesClassifier.printNStrongestFeatures(10);
		logger.info(naiveBayesClassifier.detect("Unatoč ulasku u EU i dostupnosti novih fondova, " +
				"Hrvate brinu gospodarstvo i kvaliteta života."));

//		HashMap<String, File> testFilesHM = new HashMap<String, File>();
//		testFilesHM.put("bs", new File("/tmp/data/dnevniavaz.ba.txt"));
//		testFilesHM.put("sr", new File("/tmp/data/politika.rs.txt"));
//		testFilesHM.put("hr", new File("/tmp/data/vecernji.hr.txt"));
//		naiveBayesClassifier.test(testFilesHM, parms);
//
	
		// Following is for retraining on new data 
//		HashMap<String, File> langFilesHM = new HashMap<String, File>();
//		langFilesHM.put("bs", new File("/tmp/data/setimes.bs.txt"));
//		langFilesHM.put("hr", new File("/tmp/data/setimes.hr.txt"));
//		langFilesHM.put("sr", new File("/tmp/data/setimes.sr.txt"));
//		HashMap<String, HashMap<String, Double>> distr = new HashMap<String, HashMap<String,Double>>();
//		naiveBayesClassifier.train(langFilesHM, distr, new File(modelFile));
//		System.out.println();
	}



	private void printNStrongestFeatures(int n) {
		Set<String> categories = parms.keySet();		
		for (String category : categories) {
			logger.info(category);
			HashMap<String, Double> features = parms.get(category);
			TreeMap<Double, String> reversedMap = new TreeMap<Double, String>();
			for (Entry<String, Double> entry : features.entrySet()) {
			    reversedMap.put(entry.getValue(), entry.getKey());
			}
			Iterator<Double> iterator =  reversedMap.descendingKeySet().iterator();
			int j=0;
			while (iterator.hasNext() &&  n>j++){
				Double d = iterator.next() ;
				logger.info(reversedMap.get(d)  +  ":" + d);
			}
		}
	}

	@SuppressWarnings("unused")
	private void test(HashMap<String, File> testFilesHM, HashMap<String, HashMap<String, Double>> parms) throws IOException {

		for (String goldLang : testFilesHM.keySet()) {
			File testFile = testFilesHM.get(goldLang);

			int hits = 0;	
			InputStream in  =  new FileInputStream(testFile);
			try {
				List<String> lines =  IOUtils.readLines(in, "UTF-8");
				HashMap<String, Integer> misses = new HashMap<String, Integer>();
				for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
					String line =  iterator.next();
					String autoLang = guessCategory(parms, line);

					if ( autoLang.equalsIgnoreCase(goldLang) ) {
						hits++;
					} else if (misses.containsKey(autoLang)) {
						misses.put(autoLang, misses.get(autoLang) + 1);
					} else {
						misses.put(autoLang, 1);
					}
				}
				logger.info("NaiveBayes" + ":" + testFile + ":" + goldLang + ":" + ((float) hits*100/lines.size()));
				logger.info("Misses are: "+misses);
			} finally {
				IOUtils.closeQuietly(in);
			}
			System.out.println();
		}
	}

	@SuppressWarnings("unused")
	private void train (HashMap<String, File> langFilesHM, HashMap<String, HashMap<String, Double>> distr, File modelFile) throws IOException {
		logger.debug("Saving model to " + langFilesHM.toString());
		for (String lang: langFilesHM.keySet()) {
			addCategory(lang, FileUtils.readLines(langFilesHM.get(lang), "UTF-8"), distr);
		}
		HashMap<String, HashMap<String, Double>> parms = calculateParms(distr);
		serializeClassifier(parms, modelFile);
	}

	private void serializeClassifier(HashMap<String, HashMap<String, Double>> parms, File modelFile) {
		try{		
			logger.debug("Serializing to " + modelFile);
			//use buffering
			OutputStream fos = new FileOutputStream( modelFile );
			OutputStream bos = new BufferedOutputStream( fos );
			ObjectOutput output = new ObjectOutputStream( bos );
			try {
				output.writeObject(parms);
			} finally{
				output.close();
			}
		} catch(IOException ex){
			logger.error("Cannot serialize model.", ex);
		}
	}

	private void addCategory(String category, List<String> lines, HashMap<String, HashMap<String, Double>> distr) {
		distr.put(category, new HashMap<String, Double>());
		calculateDistr(lines, distr.get(category));	
	}


	private HashMap<String, HashMap<String, Double>> calculateParms(HashMap<String, HashMap<String, Double>> distr) {
		HashMap<String, HashMap<String, Double>> parms = new HashMap<String, HashMap<String,Double>>();

		Set<String> features = new HashSet<String>();
		Set<String> categories = distr.keySet();
		for (String category : categories) {
			features.addAll(distr.get(category).keySet());
			parms.put(category, new HashMap<String, Double>());
		}

		for (String feature : features) {
			double featureSum = 0.0;
			for (String category : categories) {
				//logger.info(category);
				if (distr.get(category).containsKey(feature)) {
					featureSum+=distr.get(category).get(feature);
				}
				featureSum+=0.1;
				if (feature.equals("sedmice") ) {
					logger.info("sedmice: " + category + " fs: "+featureSum);
				}

				double val=0.0;
				if (distr.get(category).containsKey(feature)) {
					val = distr.get(category).get(feature);
				}
				parms.get(category).put(feature, val+0.1);	
				if (feature.equals("sedmice") ) {
					logger.info( "Init param ->" + feature + " " + category + " ->" + parms.get(category).get(feature));
				}
			}

			for (String category : categories) {
				if (feature.equals("sedmice") && category.equals("bs")) {
					System.out.println("Feat " + parms.get(category).get(feature));
					System.out.println("FS " + featureSum);     
					System.out.println("Feat/FS " + parms.get(category).get(feature)/ featureSum);     
					System.out.println("log_e(Feat/FS) " + Math.log(parms.get(category).get(feature)/ featureSum));     
					System.out.println("log_2(Feat/FS) " + ( Math.log( parms.get(category).get(feature)/ featureSum)) / Math.log(2));     
					System.out.println("Mylog_2(Feat/FS) " +  Math.log(0.99988104) / Math.log(2));     

					//logger.info("Prefinal param " + category + "-> (-0.0001707129384182576) " + feature + "->" + Math.log(parms.get(category).get(feature) / featureSum ));
				}
				
				parms.get(category).put(feature, Math.log(parms.get(category).get(feature) / featureSum ) / Math.log(2) );
				if (feature.equals("sedmice") && category.equals("bs")) {
					logger.info("Final param " + category + "-> (-0.0001707129384182576) " + feature + "->" + parms.get(category).get(feature));
				}
			}	
			
		}
		distr=null;
		return parms;
	}

	
	

	@SuppressWarnings({ "unused", "unchecked" })
	private static HashMap<String, HashMap<String, Double>> deSerializeClassifier(File modelFile) {
		HashMap<String, HashMap<String, Double>> parms = null;
		try{
			logger.debug("Reading model from " + modelFile);
			InputStream fis = new FileInputStream( modelFile);
			InputStream bis = new BufferedInputStream( fis );
			ObjectInput input = new ObjectInputStream ( bis );
			try {
				parms = (HashMap<String, HashMap<String, Double>>) input.readObject();
			} finally{
				input.close();
			}
		} catch(ClassNotFoundException ex){
			logger.error("Cannot perform input. Class not found.", ex);
		} catch (IOException ex){
			logger.error("Cannot serialize model.", ex);
		}	

		return parms;
	}


}
