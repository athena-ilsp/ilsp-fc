package gr.ilsp.fc.langdetect;

import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.langdetect.LangDetectorFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LangDetectorFactoryTest {
	
	private static final Logger logger = LoggerFactory.getLogger(LangDetectorFactoryTest.class);
	private static HashSet<String> langDetectorIds;

	static String langDetectorStr = "langdetect";
	static LangDetectorFactory langDetectorFactory ;
	static LangDetector langDetector;
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		// for testGetLangDetector
		langDetectorIds = new HashSet<String>();
		langDetectorIds.add("langdetect");
		langDetectorIds.add("bs-hr-sr-nb");
//		langDetectorIds.add("tika");
//		langDetectorIds.add("langid");
		
		
		// for testLangDetector
		langDetectorFactory = new LangDetectorFactory();
		langDetector = langDetectorFactory.getLangDetector(langDetectorStr);
		langDetector.initialize();
	}

	@Test
	/**
	 * Actually not a test, but a method to illustrate use.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public void testGetLangDetector() throws Exception {
		
		HashMap<String, String> fileNamesHM = new HashMap<String, String>();
		fileNamesHM.put("politika.srp.txt", "srp");
		fileNamesHM.put("vecernji.hrv.txt", "hrv");
		fileNamesHM.put("dnevniavaz.bos.txt", "bos");
		Set<String> fileNames = fileNamesHM.keySet();

		LangDetectorFactory langDetectorFactory = new LangDetectorFactory();
		for (String langDetectorId : langDetectorIds) {
			logger.info("Loading LangDetector " + langDetectorId );
			LangDetector loopLangDetector;
			if (langDetectorId.contains("langdetect")) {
				loopLangDetector = langDetector; 
			} else {
				loopLangDetector = langDetectorFactory.getLangDetector(langDetectorId);
				loopLangDetector.initialize();
			}
			for (String fileName : fileNames) {
				String goldLang = fileNamesHM.get(fileName);
				int hits = 0;
				InputStream in  =  LangDetectorFactory.class.getResource("/rs_ba_hr_corpus/"+fileName).openStream();
				try {
					List<String> lines =  IOUtils.readLines(in, "UTF-8");
					HashMap<String, Integer> misses = new HashMap<String, Integer>();
					for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
						String line =  iterator.next();
						String autoLang = loopLangDetector.detect(line);
						if ( autoLang.equalsIgnoreCase(goldLang) ) {
							hits++;
						} else if (misses.containsKey(autoLang)) {
							misses.put(autoLang, misses.get(autoLang) + 1);
						} else {
							misses.put(autoLang, 1);
						}
						//langDetector.detectLangs(line);
					}
					logger.info(langDetectorId + ":" + fileName + ":" + goldLang + ":" + ((float) hits*100/lines.size()));
					logger.info("Misses are: "+misses);
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
			System.out.println();
		}
	}

	private void testLangDetector(String input, String langcode, String testName) {
		try {
			logger.info("Language detection for " + langcode  + " on " + input);
			Assert.assertEquals(testName + ": ", langcode, langDetector.detect(input));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testLangDetectorOnIberianInput() {
		testLangDetector("El Gobierno de España es el órgano constitucional que encabeza el poder ejecutivo estatal y dirige la Administración General del Estado. "
				+ "El Gobierno depende políticamente del Congreso de los Diputados a través de la investidura o censura de su presidente, "
				+ "conforme a la forma de Gobierno parlamentarista establecida por la Constitución española de 1978.", 
				"spa",
				"spa test");
		testLangDetector("O Governo da Espanha tem seu regulamento principal no Título IV da Constituição Espanhola de 1978 e no Título V na lei do governo. "
				+ "A Espanha é uma Monarquia Constitucional, onde há o Rei: Don Felipe VI, e o Primeiro-Ministro: Mariano Rajoy Brey.", 
				"por",
				"por test");
		testLangDetector("El govern d'Espanya és la representació de la prefectura de govern del cos executiu d'estat espanyol. "
				+ "És regulat per les disposicions de la Constitució Espanyola de 1978, en els Títols IV i V. "
				+ "El govern dirigeix la política exterior i interior, l'administració civil i militar i la defensa de l'Estat. "
				+ "Exerceix la funció executiva i la potestat reglamentària d'acord amb la Constitució i les lleis de l'Estat.", 
				"cat",
				"cat test");
		testLangDetector("Espainiako botere betearazlearen ordezkaritza da. Hurrengoek osatzen dute Espainiako Gobernua:  Gobernuko presidentea; Gobernuko presidenteordeak. "
				+ "Kargu hauek egotea ez da beharrezkoa eta hauen funtzioak eta zereginak presidenteak aukeratzen eta banatzen ditu.; Ministroak. "
				+ "Presidenteak proposatzen ditu eta Erregeak izendatzen ditu. Bakoitzak alor bat edo gehiagoko ministerio bateko arduradunak dira. "
				+ "Hauek, Ministroen Kontseiluan batzen dira erabakiak hartzeko.", 
				"eus",
				"eus test");
		testLangDetector("O Goberno Español ten o seu principal regulación na Constitución española de 1978, no seu Título IV (e o V respecto da súa relación coas Cortes Xerais). "
				+ "O Goberno dirixe a interior e exterior, a Administración civil e militar e a defensa do Estado. "
				+ "Exerce a función executiva e a potestade regulamentaria de acordo coa Constitución e as leis. O Goberno componse do Presidente, dun ou máis Vicepresidentes, dos Ministros e dos demais membros que estableza a lei."
				+ "O Presidente dirixe a acción do Goberno e coordina as funcións dos demais membros do mesmo, sen prexuízo da competencia e responsabilidade directa destes na súa xestión. "
				+ "Os membros do Goberno non poderán exercer outras funcións representativas que as propias do mandato parlamentario, nin calquera outra función pública que non derive do seu cargo, nin actividade profesional ou mercantil algunha. "
				+ "O Presidente é elixido polo Congreso, a proposta do Rei. O resto de membros do Goberno, elíxeos o propio Presidente, nomeándoos o Rei. "
				+ "A actuación do Rei é simbólica.", 
				"glg",
				"glg test");
	
	}
	
	
	@Test
	public void testLangDetectorOnGaelicInput() {
		testLangDetector("Mae Cymru (hefyd Saesneg: Wales) yn wlad Geltaidd. Gyda'r Alban, Cernyw, Gogledd Iwerddon a Lloegr,[1] mae Cymru'n rhan o'r Deyrnas Unedig. "
				+ "Lleolir y wlad yn ne-orllewin gwledydd Prydain gan ffinio â Lloegr i'r dwyrain, Môr Hafren a Môr Iwerydd i'r gogledd a'r gorllewin. "
				+ "Cymru yw tywysogaeth fwyaf y byd, ond bellach nid yw'r term \"tywysogaeth\" yn cael ei defnyddio'n aml i'w disgrifio. "
				+ "Cymraeg yw iaith frodorol y wlad ond siaredir Saesneg gan y cyfan o'i dinasyddion erbyn heddiw; gall oddeutu 19% o'i phoblogaeth siarad Cymraeg.", 
				"cym",
				"cym test");
	}

	@Test
	public void testLangDetectorOnMalteseInput() {
		testLangDetector("Il-Prim Ministru ta' Malta imexxi l-Gvern ta' Malta, u fil-protokoll jiġi eżatt wara l-President ta' Malta li għandu l-ogħla kariga statali minkejja li dan tal-aħħar għandu "
				+ "rwol kważi ċerimonjali. Il-Prim Ministru jiġi innominat mill-President, bid-deċiżjoni tiegħu jew tagħha tkun ibbażata skont is-sitwazzjoni fil-Parlament Malti. "
				+ "Il-Prim Ministru jrid ikollu maġġoranza ta' voti fil-Parlament biex jaqdi l-mandat. Ħafna drabi l-Prim Ministru jkun il-mexxej tal-iktar partit li jkun ġab voti f'elezzjoni ġenerali."
				+ "Sa mill-1921 Malta kellha b'kollox 13-il Prim Ministru. Il-kariga ta' Prim Ministru Malti ma kinitx teżisti bejn l-1933 u l-1947, kif ukoll bejn l-1958 u l-1962.", 
				"mlt",
				"mlt test");
	}
	
}
