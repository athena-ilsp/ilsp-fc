package gr.ilsp.fc.classifier;

import gr.ilsp.fc.datums.ClassifierDatum;
import gr.ilsp.fc.datums.ExtendedParsedDatum;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.TopicTools;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class Classifier implements Serializable{
	private static final Logger LOGGER = Logger.getLogger(Classifier.class);
	private static final String keyword_loc="keywords";
	private static final String description_loc= "description";
	private static final String PDFcontent = "pdfcontent";
	private static double TITLE_WEIGHT = 10;
	private static double KEYWORDS_WEIGHT = 4;
	private static double META_WEIGHT = 2;
	private static double CONTENT_WEIGHT = 1;
	//private double TOTABSCORE_TH = 0.0;
	//private double TOTRELSCORE_TH = 0.2;
	//private double SUBCLASSSCORE_TH = 0.2;
	//private double relcontentthr = 0.1;
	private String[] _targetLanguages;
	private String _storeFilter;
	//private int _minTokensNumber = 200;		
	private int _minTokensNumber;
	private static String[] _classes;
	private String[] _targetlangKeys;

	private static ArrayList<String[]> _topic;

	private static double _absthres;
	private static double _relthres;
	private static int _min_uniq_terms;
	private boolean _keepBoiler = false;

	private int _max_depth;

	public Classifier(String[] langKeys, String[] targetlanguages, String[] classes, 
			ArrayList<String[]> topic, double abs_thres, double rel_thres, boolean keepBoiler, 
			int min_uniq_terms, int max_depth, int minTokensNumber, String storeFilter){
		_targetLanguages = targetlanguages;
		_classes = classes;
		_topic = topic;
		_absthres = abs_thres;
		_relthres = rel_thres;
		_keepBoiler  = keepBoiler;
		_min_uniq_terms = min_uniq_terms;
		_max_depth = max_depth;
		_targetlangKeys=langKeys;
		_minTokensNumber=minTokensNumber;
		_storeFilter = storeFilter;
	}

	
	/**
	 * Gets info from parsedDatum and checks:
	 * if content is long enough,
	 * if language of content is a targeted language,
	 * if content is relevant to the user-provided topic (if exists)
	 * if fetched url matches filter for storing (if exists)
	 * It is applied only on fetched html documents, in other cases (i.e. pdf) it is not applied   
	 * @param parsedDatum
	 * @return
	 */
	public ClassifierDatum classify(ExtendedParsedDatum parsedDatum) {
		String url = parsedDatum.getUrl();
		if (_storeFilter!=null){
			if (!url.matches(_storeFilter)){
				return null;
			}
		}
		
		String content = parsedDatum.getParsedText().toLowerCase();
		
		if (content.equals(PDFcontent)){
			return new ClassifierDatum(url, new String[0],new Double[0][0], 0.0, 0.0,0);
		}
		if (_keepBoiler)
			content = ContentNormalizer.cleanContent(content);
		int length_in_tok=FCStringUtils.countTokens(content);
		if (length_in_tok<_minTokensNumber){
			LOGGER.debug(parsedDatum.getUrl()+"\nCUT due to its small clean content length:"+length_in_tok+ "/"+_minTokensNumber);
			return null;
		}
		String identifiedlanguage = parsedDatum.getLanguage();
		if (!LangDetectUtils.istargetedlang(identifiedlanguage,_targetLanguages))
			return null;
		String title = parsedDatum.getTitle();
		String keywords = "", meta= "";
				
		//if (url.contains("wikipedia"))
		//	return null;
		//		for (String s:metaMap.keySet()){
		//			if (s.equals("keywords"))
		//				keywords = metaMap.get(s);
		//			if (s.equals("description"))
		//				meta = metaMap.get(s);			
		//		}

		Map<String,String> metaMap = parsedDatum.getParsedMeta();
		for (Entry<String, String> s:metaMap.entrySet()){
			if (s.getKey().equals(keyword_loc))
				keywords = s.getValue();
			if (s.getKey().equals(description_loc))
				meta = s.getValue();			
		}
		if (_topic==null){	
			return new ClassifierDatum(url, new String[0],new Double[0][0], 0.0, 0.0,length_in_tok);
		}		
		if (title==null) title = "";

		ClassifierDatum result=classifyText(title,keywords,meta,content,identifiedlanguage, url, length_in_tok);
		return result;
	}
	

	/**
	 * Checks if title, keywords, meta and content contains terms of the topic definition and classifies the documents as relevant to the topic or not  
	 * @param title
	 * @param keywords
	 * @param meta
	 * @param content
	 * @param identifiedlanguage
	 * @param url
	 * @param length_in_tok
	 * @return
	 */
	public static ClassifierDatum classifyText(String title, String keywords, String meta, String content, String identifiedlanguage, String url, int length_in_tok) {
		Double[][] titleScores = rankText(title,identifiedlanguage, TITLE_WEIGHT ,_topic,_classes,false);
		Double[][] keywordsScores=rankText(keywords,identifiedlanguage,KEYWORDS_WEIGHT ,_topic,_classes,false);
		Double[][] metaScores=rankText(meta,identifiedlanguage,META_WEIGHT ,_topic,_classes,false);		
		Double[][] contentScores=rankText(content,identifiedlanguage,CONTENT_WEIGHT ,_topic,_classes, true);
		if (contentScores==null)
			return null;
		
		double norm_score=0; 
		Double total_score=0.0;
		Double total_relscore = 0.0;
		//add the scores
		Double[][] sums= titleScores;
		for (int ii=0;ii<titleScores.length;ii++){
			sums[ii][0] += keywordsScores[ii][0]+ metaScores[ii][0]+contentScores[ii][0];
			sums[ii][1] += keywordsScores[ii][1]+ metaScores[ii][1]+contentScores[ii][1];
		}

		//if the total absolute and relative scores are over the corresponding thresholds
		//calculate a normalized score per subclass and compare it with another threshold
		ArrayList<String> subclasses = new ArrayList<String>();
		ArrayList<Double[][]> subscores = new ArrayList<Double[][]>(); 
		//if (sums[sums.length-1][0]>thr1 && sums[sums.length-1][1]>thr2){
		if (sums[sums.length-1][0]>=_absthres && sums[sums.length-1][1]>=_relthres){
			//pass=true;
			for (int ii=0;ii<titleScores.length-1;ii++){
				//norm_score =100 *sums[ii][1]/sums[scores1.length-1][1];
				norm_score = sums[ii][1]/sums[titleScores.length-1][1];
				if (norm_score>=_relthres){	
					subclasses.add(_classes[ii]);
					//System.out.println("Text is in the subclass " + classes[ii]+".");
					total_score += sums[ii][0];
					total_relscore += sums[ii][1];
					subscores.add(new Double[][]{new Double[] {sums[ii][0],sums[ii][1]}});
				}
			}
		}else{
			return null;
		}
		String[] subclasses1=new String[subclasses.size()];
		for (int i=0;i<subclasses.size();i++){
			subclasses1[i]= subclasses.get(i);
		}
		Double[][] subscores1 = new Double[subscores.size()][2];
		for (int i=0;i<subscores.size();i++){
			Double[][] temp=subscores.get(i);
			subscores1[i][0] = temp[0][0];
			subscores1[i][1] = temp[0][1];
		}
		//System.out.println("The total score is "+total_score);
		total_relscore=contentScores[contentScores.length-1][1]/2; //rel score is based on content only.
		total_relscore=(double) (Math.round(total_relscore*1000))/1000;
		
		double contentscore = contentScores[contentScores.length-1][0];
		double relcontentscore = contentScores[contentScores.length-1][1];
		if (_absthres==0.0){
			_relthres=-0.1;
		}
		if (contentscore>=_absthres && relcontentscore>_relthres){//TODO relthres value????
			ClassifierDatum result = new ClassifierDatum(url, subclasses1,subscores1, total_score, total_relscore,length_in_tok);
			return result;
		}else
			return null;
	}

	/**
	 * Calculates scores. Returns an array of doubles with 2 columns.
	 * The number of rows are as many as the subclasses plus one (the "total").
	 * The first contains the absolute score per subclass, while the second relative scores,
	 * i.e. the absolute scores divided by the number of words.
	 * @param str: text to classify
	 * @param w: weight for the location of the examined text
	 * @param topic: List of triplets
	 * @param classes: List of classes
	 * @param isContent
	 * @return
	 */
	public static Double[][] rankText(String str, String language, double w,ArrayList<String[]> topic, String[] classes, boolean isContent){
		int uniqueTermsFound = 0;
		//stem the input str
		ArrayList<String> stems =new ArrayList<String>();
		try {
			stems = TopicTools.getStems(str, language);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
			return null;
		}
		//concatenate stems 
		str="";
		for (String s:stems){ str+=" "+s;}
		str = str.trim();
		double words_num= FCStringUtils.countTokens(str);
		//initialization of scores array
		Double[][] scores = new Double[classes.length+1][2];
		for (int ii=0;ii<classes.length;ii++){
			scores[ii][0] = 0.0;	scores[ii][1] = 0.0;
		}		
		scores[classes.length][0]=0.0;  scores[classes.length][1]=0.0;
		if (words_num==0.0) return scores;

		String[] tempstr = new String[1];		String term, term_class, term_lang;
		double weight=0, matches, term_score; 
		int index;
		for (int ii=0;ii<topic.size();ii++){ //for each row of the topic
			//get the term
			tempstr=topic.get(ii);
			term = tempstr[1];
			term_lang=tempstr[3];
			matches=0;
			term_score=0;
			weight=Double.parseDouble(tempstr[0]);
			//find term in text
			if (!term_lang.equals(language))
				continue;
			Pattern pattern = Pattern.compile(" "+term+" ");	
			Matcher matcher = pattern.matcher(" "+str+" ");
			//list with positions of a found term
			ArrayList<String> termpos=new ArrayList<String>(); 
			while (matcher.find()) {
				//get the position of a found term
				termpos.add(Integer.toString(matcher.start()));
				matches++;
			}

			if (matches>0){
				if (weight>0.0){
					uniqueTermsFound++;
				}
				//add found term
				//termfound.add(term);
				//add id of the found term
				//termfoundid.add(Integer.toString(ii+1));
				//get the weight of the term
				//weight=Double.parseDouble(tempstr[0]);
				term_score = weight*matches;
				//get the subclass of the term
				term_class = tempstr[2];
				if (term_class.contains(";")){
					String[] term_classes = term_class.split(";");
					int term_classes_amount = term_classes.length;
					for (int mm=0;mm<term_classes_amount;mm++){
						index = Arrays.binarySearch(classes, term_classes[mm]);
						scores[index][0] =scores[index][0]+term_score/term_classes_amount;
					}
				}
				else{
					index = Arrays.binarySearch(classes, term_class);
					scores[index][0] =scores[index][0]+term_score;
				}
			}
		}
		//Check if found terms in the content are equal or above the predefined threshold
		if (isContent) {
			if (uniqueTermsFound<_min_uniq_terms && _min_uniq_terms>0) {
				return null;
			}
		}
		//calculate scores with the weight of the text (the location weight) 
		//and the relative scores
		for (int ii=0;ii<classes.length;ii++){
			scores[ii][0] = scores[ii][0]*w;
			if (isContent)
				scores[ii][1] = scores[ii][0]/words_num;
			scores[classes.length][0] =scores[ii][0]+scores[classes.length][0];
			scores[classes.length][1] =scores[ii][1]+scores[classes.length][1];			
		}		
		return scores;
	}


	public double rankLink(String text1, String anchortext, String pagelang, double vv) {
		double score = 0, weight=0; int matches=0;
		String[] tempstr = null;		
		String term;
		String text = text1.trim();
		ArrayList<String> stems =new ArrayList<String>();
		try {
			
			boolean matchT=false, matchL=false;
			for (String lang:_targetLanguages){
				if (pagelang.equals(lang)){
					matchT=true; //current page is in one of the targeted languages 
					break;
				}
			}
			String langidentified_link="";
			//System.out.println("langidentified_page:"+langIdentified);

			if (!text.isEmpty()){
				langidentified_link= LangDetectUtils.detectLanguage(text.toLowerCase());
				//System.out.print("langidentified_link:"+langidentified_link+"\t");
				for (String lang:_targetLanguages){
					if (langidentified_link.equals(lang)){
						matchL=true; //text of current link is in one of the targeted languages 
						break;
					}
				}
			}else
				return score;
			int type=0;
			if (_targetLanguages.length>1){
				int m=0;
				for (String lang:_targetLanguages){
					//if (containLangKeys(text,m) & !lang.equals(pagelang)){
					if (containLangKeys(anchortext,m) & !lang.equals(pagelang)){
						//System.out.println("BINGO!");
						//link's text implies that the link points to candidate translation
						type=1;
						break;
					}
					m++;
				}
				if (type==1 & vv>=_absthres){
					//link's text implies that the link points to candidate translation
					score +=10000*_absthres;
					return score;
				}
			}
			if (matchT & matchL){
				if (!langidentified_link.equals(pagelang) & FCStringUtils.countTokens(text)>3 & vv>=_absthres){
					score+=10*_absthres; //text and link are in dif. langs but both in targ. langs
				}else{
					score+=_absthres;  //text and link are in same langs and in targ. langs
				}
				stems = TopicTools.getStems(text, langidentified_link);
			}
			else
				return score;
		} catch (IOException e) {
			e.printStackTrace();
		}
		text="";
		for (String s:stems){ text=text.concat(" "+s);}
		text = text.trim();
		for (int ii=0;ii<_topic.size();ii++){ 
			tempstr=_topic.get(ii);
			term = tempstr[1];
			matches=0;
			Pattern pattern = Pattern.compile(" "+term+" ");
			Matcher matcher = pattern.matcher(" "+text+" ");
			while (matcher.find()) {
				matches++;
			}
			if (matches>0){
				weight=Double.parseDouble(tempstr[0]);
				score += weight*matches;
				//System.out.println("FoundTerm :"+ term );
			}
		}
		return score;
	}


	private boolean containLangKeys(String text, int m) {
		boolean result = false;
		if  (_targetlangKeys[m].isEmpty())
			return result;
		String[] keys= _targetlangKeys[m].split(",");
		String[] words= text.toLowerCase().split(" ");
		if (words.length>10)
			return result;
		for (int ii=0;ii<keys.length;ii++){
			for (int jj=0;jj<words.length;jj++){
				if (keys[ii].toLowerCase().equals(words[jj].toLowerCase())){
					result=true;
					break;
				}
			}
		}
		return result;
	}


	public ArrayList<String[]> getTopic(){
		return _topic;
	}

	public int getMaxDepth() {
		return _max_depth;
	}

	/**
	 * calculates a score per link
	 * @param linktext : text around the link and check its language
	 * @param anchortext : to be checked for special langKeys
	 * @param pagelang : language for the webpage contains the examined link
	 * @param vv : score of the webpage
	 * @return
	 */
	public double rankLinkNotopic(String linktext, String anchortext, String pagelang, double vv) {
		double score = 0;
		String text = linktext.trim();
		boolean matchT=false, matchL=false;
		for (String lang:_targetLanguages){
			if (pagelang.equals(lang)){
				matchT=true; //current page is in one of the targeted languages 
				break;
			}
		}
		String langidentified_link="";
		//System.out.println("langidentified_page:"+langIdentified);

		if (!text.isEmpty()){
			langidentified_link= LangDetectUtils.detectLanguage(text.toLowerCase());
			//System.out.print("langidentified_link:"+langidentified_link+"\t");
			for (String lang:_targetLanguages){
				if (langidentified_link.equals(lang)){
					matchL=true; //text of current link is in one of the targeted languages 
					break;
				}
			}
		}else
			return score;
		int type=0;
		if (_targetLanguages.length>1){
			int m=0;
			for (String lang:_targetLanguages){
				//if (containLangKeys(text,m) & !lang.equals(pagelang)){
				if (containLangKeys(anchortext,m) & !lang.equals(pagelang)){
					//System.out.println("BINGO!");
					//link's text implies that the link points to candidate translation
					type=1;
					break;
				}
				m++;
			}
			if (type==1 & vv>=_absthres){
				//link's text implies that the link points to candidate translation
				score +=2;
				return score;
			}
		}
		if (matchT & matchL){
			if (!langidentified_link.equals(pagelang) & FCStringUtils.countTokens(text)>3 & vv>=_absthres){
				score+=1; //text and link are in dif. langs but both in targ. langs
			}else{
				score+=1;  //text and link are in same langs and in targ. langs
			}
		}
		else
			return score;
		return score;
	}
}
