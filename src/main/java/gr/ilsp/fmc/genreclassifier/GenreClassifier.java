package gr.ilsp.fmc.genreclassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fmc.main.ReadResources;

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
		String tmp1;
		try {
			//ReadResources.class.getClassLoader().getResource(genres_file);
			tmp1 = ReadResources.readFileAsString(genres_file);

			//URL default_genre = SimpleCrawlHFS.class.getClassLoader().getResource(genres_file);			
			//System.out.println( default_genre.getFile());	
			//tmp1 = ReadResources.readFileAsString(default_genre.getFile());

			tmp1=tmp1.replaceAll("\r\n", "\n");
			String[] genres_features = tmp1.split("\n");
			String[] genres = new String[genres_features.length];
			String[] features = new String[genres_features.length];
			for (int ii=0;ii<genres_features.length;ii++){
				String[] temp= genres_features[ii].split("\t");
				genres[ii]=temp[0]; 	
				if (temp.length>1)
					features[ii]=temp[1].toLowerCase();
				else
					features[ii]=" ";
			}
			int found=genres_features.length-1;
			for (int jj=0; jj<genres.length;jj++){
				if (matches(url,features[jj],genregkeys_separator)){
					found=jj;
					break;
				}
			}
			if (found==genres_features.length-1){
				for (int jj=0; jj<genres.length;jj++){
					if (matches(title,features[jj],genregkeys_separator)){
						found=jj;
						break;
					}
				}
			}
			doc_genre=genres[found];
		} catch (IOException e) {
			logger.error("File with genre types and genre keywords cannot be read.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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



	public static HashMap<String, String> Genres_keywords(String genres_file) {
		HashMap<String, String> genres_keys = new HashMap<String, String>();
		try {
			ArrayList<String> tmp1 = ReadResources.readFileLineByLine(genres_file);
			for (int ii=0;ii<tmp1.size();ii++){
				String[] tmp2 = tmp1.get(ii).split(genre_separator);
				//String[] tmp3 = tmp2[1].split(separator);
				//ArrayList<String> tmp4 = new ArrayList<String>();
				//for (int jj=0;jj<tmp3.length;jj++){
				//	tmp4.add(tmp3[jj].toLowerCase());
				//}
				if (tmp2.length==2)
					genres_keys.put(tmp2[0], tmp2[1].toLowerCase());
				else{
					if (tmp2.length>2){
						logger.info("Genre file should be checked. A textline includes more than 1 tabs.");
					}
				}
			}
		} catch (IOException e) {
			logger.error("File with genre types and genre keywords cannot be read.");
			e.printStackTrace();
		}
		return genres_keys;
	}
}
