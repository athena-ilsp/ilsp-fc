package gr.ilsp.fc.genreclassifier;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.main.ReadResources;

public class GenreClassifier {

	private static final String genregkeys_separator=",";
	private static final String genre_separator="\t";
	static Logger logger = LoggerFactory.getLogger(GenreClassifier.class.getCanonicalName());

	public static String GenreClassifier_keywords(HashMap<String, String> genres_keys, String url,
			String title) {
		String doc_genre="Other";

		String features;
		Set<String> keys=genres_keys.keySet();
		Iterator<String> keys_it = keys.iterator();
		String key ;
		while (keys_it.hasNext()){
			key = keys_it.next();
			features = genres_keys.get(key);
			if (matches(url,features,genregkeys_separator)){
				doc_genre = key;
				break;
			}
			if (doc_genre.equals("Other")){
				if (matches(title,features,genregkeys_separator)){
					doc_genre = key;
					break;
				}
			}
		}
		return doc_genre;
	}


	public static String GenreClassifier_keywords_or(String genres_file, String url,
			String title) {
		String doc_genre="Other";
		//ReadResources.class.getClassLoader().getResource(genres_file);
		
		List<String> genres_features=new ArrayList<String>();
		try {
			genres_features = FileUtils.readLines(new File(genres_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (genres_features.isEmpty()){
			return "unknown";
		}
		String[] genres = new String[genres_features.size()];
		String[] features = new String[genres_features.size()];
		for (int ii=0;ii<genres_features.size();ii++){
			String[] temp= genres_features.get(ii).split(genre_separator);
			genres[ii]=temp[0]; 	
			if (temp.length>1)
				features[ii]=temp[1].toLowerCase();
			else
				features[ii]=" ";
		}
		int found=genres_features.size()-1;
		for (int jj=0; jj<genres.length;jj++){
			if (matches(url,features[jj],genregkeys_separator)){
				found=jj;
				break;
			}
		}
		if (found==genres_features.size()-1){
			for (int jj=0; jj<genres.length;jj++){
				if (matches(title,features[jj],genregkeys_separator)){
					found=jj;
					break;
				}
			}
		}
		doc_genre=genres[found];
		return doc_genre;
	}

	private static boolean matches(String text, String list, String separator) {
		String[] temp=list.split(separator);
		for (int ii=0;ii<temp.length;ii++){
			if (text.contains(temp[ii].trim())){
				return true;
			}
		}
		return false;
	}


	public static HashMap<String, String> Genres_keywords(URL genreFile) {
		HashMap<String, String> genres_keys = new HashMap<String, String>();

		logger.info("Reading genre file: " + genreFile);
		List<String> tmp1 = ReadResources.readFileLineByLine(genreFile); 
		//List<String> tmp1=FileUtils.readLines(genreFile);
		
		for (int ii=0;ii<tmp1.size();ii++){
			String[] tmp2 = tmp1.get(ii).split(genre_separator);
			if (tmp2.length==2){
				genres_keys.put(tmp2[0], tmp2[1].toLowerCase());
				logger.info("genre type: "+ tmp2[0]);
			}
			else{
				if (tmp2.length>2){
					logger.info("Genre file should be checked. A textline includes more than 1 tabs.");
				}
			}
		}
		return genres_keys;
	}
}
