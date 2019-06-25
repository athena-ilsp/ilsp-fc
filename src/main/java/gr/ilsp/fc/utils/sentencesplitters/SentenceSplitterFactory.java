package gr.ilsp.fc.utils.sentencesplitters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import gr.ilsp.nlp.commons.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentenceSplitterFactory {

	private static final String SUFFIX = ".txt";
	private static final String PREFIX = "-abbrs-";
	private static final String NBABBR_STR = "=0";
	private static final String DOT_STR = ".";
	private static final Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);

	public SentenceSplitter getSentenceSplitter(String lang) {
		SentenceSplitter sentenceSplitter;
		logger.debug("Creating segmenter for " + lang);
		if (lang.startsWith("zh")) {
			sentenceSplitter = new MorphAdornerSentenceSplitter(lang);
		} else if (lang.startsWith("zh")) { // FIXME: choose between the two
			sentenceSplitter = new ChineseSentenceSplitter();
		} else {
			sentenceSplitter = new  MorphAdornerSentenceSplitter(lang);
		}
		
		try {				
			URL abbreviationsURL = this.getClass().getResource("/abbr-lists/eng.txt");
			URL langSpecificAbbreviationsURL = this.getClass().getResource("/abbr-lists/"+ lang +".txt");
			URL allAbbrsURL = null;
			
			if (langSpecificAbbreviationsURL == null) {
				allAbbrsURL = createAbbreviationList(abbreviationsURL, null, null);
			} else {
				logger.debug(langSpecificAbbreviationsURL.toExternalForm());
				allAbbrsURL = createAbbreviationList(abbreviationsURL, langSpecificAbbreviationsURL, lang);
				for (String line: IOUtils.readLines(allAbbrsURL.openStream())) {
					logger.debug(line);
				}
			}
			sentenceSplitter.setAbbreviationsURL(allAbbrsURL);
		} catch (IOException e) {
			logger.warn("Could not load abbreviations.");
			e.printStackTrace();
		}
		
		return sentenceSplitter;
	}

	private URL createAbbreviationList(URL commonAbbreviationsURL, URL langSpecificAbbreviationsURL, String lang) {
		try {
			List<String> commonAbbrs = IOUtils.readLines(commonAbbreviationsURL.openStream());
			if (langSpecificAbbreviationsURL!=null) {
				List<String> langSpecificAbbrs = IOUtils.readLines(langSpecificAbbreviationsURL.openStream());
				commonAbbrs.addAll(langSpecificAbbrs);
			}
			if (lang==null) {
				lang="common";
			}
			List<String> abbrs = new ArrayList<String>();
			for (String abbr: commonAbbrs) {
				abbr = abbr.trim();
				if (abbr.endsWith(DOT_STR)) {
					logger.debug(abbr);
					abbrs.add(abbr+NBABBR_STR);
				}
			}
			File tempFile = File.createTempFile(lang + PREFIX, SUFFIX);
			FileUtils.writeLines(tempFile, Constants.UTF8, abbrs,"\n"); 
			tempFile.deleteOnExit();
			return tempFile.toURI().toURL();
		} catch (IOException e) {
			return commonAbbreviationsURL;
		}
	}

	public static void main(String[] args) throws IOException {
		SentenceSplitterFactory sentenceSplitterFactory = new SentenceSplitterFactory();
		String langs = "amh ara aym ben bul cat ces dan deu ell eng epo fas fil fra heb hin hun ind ita jpn khm kor mkd mlg mya nld ori pol por rum rus spa sqi srp swa swe tur urd zhs ara aym ben bul cat ces dan deu ell eng epo fas fil fra heb hin hun ind ita jpn khm kor mkd mlg mya nld ori pol por rum rus spa sqi srp swa swe tur urd zhs zht";
		langs = "mlg";
		for (String lang : Arrays.asList(StringUtils.split(langs))) {
			logger.info(lang);
			SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter(lang);
			//logger.info(sentenceSegmenter.toString());
			List<String> sentences = sentenceSplitter.getSentences(
					""
//					+ "Nivoaka voalohany tany Gaza tamin'ny 1 Aogositra ny "
//					+ "<a href=”http://www.reuters.com/article/middleeastCrisis/idUSL3682698″>horonantsary lava</a> "
//					+ "novokarin'i <a href=”http://en.wikipedia.org/wiki/Hamas”>Hamas</a>, ampahany amin'ny tetika "
//					+ "fanangonana fanohanana nataon'ilay hetsika avy amin'ny alalan'ny "
//					+ "<a href=”http://www.nytimes.com/2009/07/24/world/middleeast/24gaza.html”>hetsika ara-kolotsaina</a>, "
//					+ "tamin'ny fananganana "
					+ "“<a href=”http://www.telegraph.co.uk/news/worldnews/middleeast/palestinianauthority/5966842/Hamas-to-make-feature-film.html”>kolontsainan'ny fanoherana</a>“. "
					+ "T"
//					+ "Samy natrehan'ireo mpitoraka blaogy avokoa izy roa ireo, ary omeny antsika ny hevitr'izy ireo."

//					+ "القصة هي كما يلي لمن منكم لم يسمع عنها: كان أول أيام العيد، وافتتاح فيلم جديد بوسط البلد. تجمعت حشود من الرجال محاولين الدخول، لكن بعد أن بيعت كل التذاكر، قرروا تحطيم شباك التذاكر. بعد تحقيق ذلك، ذهبوا إلى ما يمكن وصفه فقط بسعار جنسي ركضوا في الجوار يمسكون بأي وكل فتاة في مرماهم، سواء أكن منقبات أم محجبين أو بلا غطاء، سواء كن مصريات أو أجنبيات، حتى الحوامل. كانوا يمسكون بهن، ويتحرشون بهن، وحاولوا أنتزاع ملابسهن واغتصابهن، كل هذا أمام الشرطة، الذين لم يحركوا ساكناً. حاولت الناس الخيرة بوسط البلد فعل ما في وسعهم لحماية الفتيات. سمح أصحاب المتاجر للفتيات بالدخول إلى متاجرهم وأغلقوا الأبواب، بينما حاولت الحشود الاقتحام. أدخل سائقي سيارات الأجرة الفتيات داخل السيارات بينما تحاول الحشود كسر الزجاج و إنتزاع الفتيات للخارج. كانت حالة مقرفة من الهرع والمرع والاعتداءات الجنسية التي استمرت لمدة 5 ساعات من 7:30 مساءً حتي 12:30 صباحاً، أصاب بالغثيان لمجرد التفكير بالأمر. "
//					+ "Cela fait plus d'un mois qu'un groupe de manifestants, qui appartiennent aux peuples "
//							+ "indigènes du territoire autochtone et du parc national d'Isiboro Sécure (sigle en espagnol: TIPNIS)  "
//							+ "a commencé sa marche de 500km dans le but d'atteindre le siège du gouvernement à La Paz et d'y exprimer son "
//							+ "opposition à la construction prévue d'une autoroute à travers son territoire. Pendant plusieurs jours, "
//							+ "la marche avait été arrêtée par un groupe de colons pro-gouvernement qui avaient bloqué la route à  Yucumo "
//							+ "pour exiger que les manifestants s'arrêtent et renouent le dialogue avec le gouvernement d'Evo Morales."
//							+ "イシボロ・セクレ国立公園先住民居住区（TIPNIS:スペイン語名の頭文字をとった名称）の先住民デモ隊が、彼らの土地を通過する道路の建設計画へ反対を表明すべく、ラ・パスの政府中枢部を目指して500kmに及ぶ行進を始めてから一ヶ月以上が経っていた。 既に数日にわたって、親政府派の移住者（訳注：アンデス地方の先住民族を中心とする）によってデモ行進は止められていた。移住者たちはユクモの道路を封鎖し、デモの停止とエボ・モラレス政権との対話の再開を求めていた。 警察による弾圧"
							,1);
			
			for (String sentence : sentences) {
				logger.info(sentence);
				
			}
			
		}
		logger.info("Done");
	}

}
