package gr.ilsp.fc.aligner.factory;

import static net.loomchild.maligna.util.Util.getFileInputStream;
import static net.loomchild.maligna.util.Util.getReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.loomchild.maligna.calculator.Calculator;
import net.loomchild.maligna.calculator.content.OracleCalculator;
import net.loomchild.maligna.calculator.content.TranslationCalculator;
import net.loomchild.maligna.calculator.length.NormalDistributionCalculator;
import net.loomchild.maligna.calculator.length.PoissonDistributionCalculator;
import net.loomchild.maligna.calculator.length.counter.CharCounter;
import net.loomchild.maligna.calculator.length.counter.Counter;
import net.loomchild.maligna.calculator.length.counter.SplitCounter;
import net.loomchild.maligna.calculator.meta.CompositeCalculator;
import net.loomchild.maligna.calculator.meta.MinimumCalculator;
import net.loomchild.maligna.coretypes.Alignment;
import net.loomchild.maligna.coretypes.Category;
import net.loomchild.maligna.coretypes.CategoryDefaults;
import net.loomchild.maligna.filter.aligner.align.AlignAlgorithm;
import net.loomchild.maligna.filter.aligner.align.hmm.HmmAlignAlgorithmFactory;
import net.loomchild.maligna.filter.aligner.align.hmm.adaptive.AdaptiveBandAlgorithm;
import net.loomchild.maligna.filter.aligner.align.hmm.fb.ForwardBackwardAlgorithm;
import net.loomchild.maligna.filter.aligner.align.hmm.fb.ForwardBackwardAlgorithmFactory;
import net.loomchild.maligna.filter.aligner.align.hmm.viterbi.ViterbiAlgorithm;
import net.loomchild.maligna.filter.aligner.align.hmm.viterbi.ViterbiAlgorithmFactory;
import net.loomchild.maligna.filter.aligner.align.onetoone.OneToOneAlgorithm;
import net.loomchild.maligna.formatter.Formatter;
import net.loomchild.maligna.matrix.BandMatrixFactory;
import net.loomchild.maligna.matrix.FullMatrixFactory;
import net.loomchild.maligna.matrix.MatrixFactory;
import net.loomchild.maligna.model.language.LanguageModel;
import net.loomchild.maligna.model.language.LanguageModelUtil;
import net.loomchild.maligna.model.translation.TranslationModel;
import net.loomchild.maligna.model.translation.TranslationModelUtil;
import net.loomchild.maligna.model.vocabulary.Vocabulary;
import net.loomchild.maligna.model.vocabulary.VocabularyUtil;
import net.loomchild.maligna.parser.AlParser;
import net.loomchild.maligna.parser.Parser;

// FIXME : import correct exception classes when on maven
//import net.loomchild.maligna.ui.console.command.exception.MissingParameterException;
//import net.loomchild.maligna.ui.console.command.exception.ParameterFormatException;
//import net.loomchild.maligna.ui.console.command.exception.UnknownParameterException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MalignaAligner extends Aligner {
	

	private static final Logger logger = LoggerFactory.getLogger(MalignaAligner.class);

//	options.addOption("c", "class", true, "Algorithm class. Valid values are: viterbi, fb, one-to-one, unify.");
//	options.addOption("o", "one", false, "Strict one to one alignment.");
//	options.addOption("s", "search", true, "Search method. Valid values are: exhaustive, band, iterative-band. Required by viterbi and fb algorithms.");
//	options.addOption("r", "radius", true, "Band radius in segments. Optional for band, iterband search method, default " + BandMatrixFactory.DEFAULT_BAND_RADIUS + ".");
//	options.addOption("e", "increment", true, "Band increment ratio in each pass. Optional for iterband search method, default " + AdaptiveBandAlgorithm.DEFAULT_BAND_INCREMENT_RATIO + ".");
//	options.addOption("m", "margin", true, "Band minimum acceptable margin. Optional for iterband search method, default " + AdaptiveBandAlgorithm.DEFAULT_MIN_BAND_MARGIN + ".");
//	options.addOption("a", "calculator", true, "Calculator classes separated by commas. Valid values are: normal, poisson, translation, oracle. Required by viterbi and fb algorithms.");
//	options.addOption("n", "counter", true, "Length counter, Valid values are: char, word. Required by normal and poisson calculators.");
//	options.addOption("l", "length-corpus", true, "Length model training corpus. Optional for poisson calculator.");
//	options.addOption("t", "translation-corpus", true, "Translation model training corpus. Optional for translation calculator.");
//	options.addOption("d", "oracle-corpus", true, "Oracle calculator corpus. Required by oracle calculator.");
//	options.addOption("x", "language-models", true, "Source and target language model separated by comma. Optional for translation calculator.");
//	options.addOption("y", "translation-model", true, "Translation model. Optional for translation calculator.");
//	options.addOption("i", "iterations", true, "Translation model train iteration count. Optional for translation calculator, default " + TranslationModelUtil.DEFAULT_TRAIN_ITERATION_COUNT + ".");
//	options.addOption("u", "unification-corpus", true, "Unification reference corpus. Required by unify algorithm.");
	
	private String cls = "viterbi"; 
	private String search = "iterative-band";
	private String counter = "word";	
	private Boolean oneToOne = false; 
	private String calculator = "poisson";
	private String lengthCorpus = null;
	private String oracleCorpus = null;
	private String translationCorpus = null;
	private String languageModels = null;
	private String transModel = null;	
	private int iterations = TranslationModelUtil.DEFAULT_TRAIN_ITERATION_COUNT;
	private int radius = BandMatrixFactory.DEFAULT_BAND_RADIUS;
	private int margin = AdaptiveBandAlgorithm.DEFAULT_MIN_BAND_MARGIN;
	private float increment = AdaptiveBandAlgorithm.DEFAULT_BAND_INCREMENT_RATIO;

	@Override
	public void initialize(String sourceLang, String targetLang) {		
		super.initialize(sourceLang, targetLang);
		logger.debug(this.toString());


	}

	@Override
	public AlignmentStats process(File sourceFile, File targetFile, File tmxFile) throws Exception {
		
		List<String> sourceSentences = new ArrayList<String>();
		List<String> targetSentences = new ArrayList<String>();
		
		
		if (sentenceSplitParagraphs) {
			for (String sourceParagraph:  getParagraphs(sourceFile, isUseBoilerplateParagraphs(), isUseOoiLang(), isPreprocessSentences() )) {
				sourceSentences.addAll(sourceLangSentenceSplitter.getSentences(sourceParagraph, 1));
			}
			for (String targetParagraph:  getParagraphs(targetFile, isUseBoilerplateParagraphs(), isUseOoiLang(), isPreprocessSentences())) {
				targetSentences.addAll(targetLangSentenceSplitter.getSentences(targetParagraph, 1));
			}

			if (isPreprocessSentences()) {
				PreAlignmentNormalizer.unMaskSentences(sourceSentences);
				PreAlignmentNormalizer.unMaskSentences(targetSentences);
				PreAlignmentNormalizer.mergeUpPunctutionOnlySentences(sourceSentences);
				PreAlignmentNormalizer.mergeUpPunctutionOnlySentences(targetSentences);
			}
		
		} else {
			for (String sourceParagraph:  getParagraphs(sourceFile, isUseBoilerplateParagraphs(), isUseOoiLang(), false)) {
				sourceSentences.add(sourceParagraph);
			}
			for (String targetParagraph:  getParagraphs(targetFile, isUseBoilerplateParagraphs(), isUseOoiLang(), false)) {
				targetSentences.add(targetParagraph);
			}
		}
		
		
		Alignment alignment = new Alignment(sourceSentences, targetSentences);
		List<Alignment> alignmentList = Collections.singletonList(alignment);
		AlignAlgorithm alignAlgorithm = createAlgorithm(alignmentList);		
		
		// UGLY HACK to avoid 
//		INFO  12:06:41 - Source sentences 67 target sentences 1 (MalignaAligner.java:116)
//		ERROR 12:06:41 - Could not align files: /opt/gv-20150731/data/gv-es-20080726-2373.xml - /opt/gv-20150731/data/gv-fr-20080719-575.xml (gr.ilsp.web.scripts.GlobalVoicesDataCollector [main] GlobalVoicesDataCollector.java:238)
//		ERROR 12:06:41 -  (gr.ilsp.web.scripts.GlobalVoicesDataCollector [main] GlobalVoicesDataCollector.java:239)
		//		java.util.NoSuchElementException
//		at net.loomchild.maligna.matrix.BandMatrixIterator.next(BandMatrixIterator.java:47)
//		at net.loomchild.maligna.filter.aligner.align.hmm.viterbi.ViterbiAlgorithm.align(ViterbiAlgorithm.java:82)
//		at net.loomchild.maligna.filter.aligner.align.hmm.adaptive.AdaptiveBandAlgorithm.align(AdaptiveBandAlgorithm.java:137)
//		at net.loomchild.maligna.filter.aligner.Aligner.apply(Aligner.java:37)
//		at gr.ilsp.fc.aligner.factory.MalignaAligner.process(MalignaAligner.java:125)
//		at gr.ilsp.web.scripts.GlobalVoicesDataCollector.sentenceAlign(GlobalVoicesDataCollector.java:236)
//		at gr.ilsp.web.scripts.GlobalVoicesDataCollector.main(GlobalVoicesDataCollector.java:191)
// 		FIXME: Consult maligna developer.
		if (Math.abs(sourceSentences.size()-targetSentences.size()) > HARD_SENTENCE_DIFF_THRESHOLD && (sourceSentences.size()==1 || targetSentences.size()==1)) {
			logger.warn("Skipping actual aligning: " + sourceFile + " " + targetFile + " " + tmxFile + ":"  + sourceSentences.size() + " " + targetSentences.size());
		} else {
			if (Math.abs(sourceSentences.size()-targetSentences.size()) > SOFT_SENTENCE_DIFF_THRESHOLD) {
				logger.warn("Difference in L1-L2 sentence size > " + SOFT_SENTENCE_DIFF_THRESHOLD
						+ ": " + sourceFile + " " + targetFile + " " + tmxFile + ":"  + sourceSentences.size() + " " + targetSentences.size());
			}
		}
		net.loomchild.maligna.filter.aligner.Aligner aligner = new net.loomchild.maligna.filter.aligner.Aligner(alignAlgorithm);
		alignmentList = aligner.apply(alignmentList);
		
		Writer writer = getSingleWriter(tmxFile);
		Formatter formatter = new BilingualScoredTmxFormatter(writer, getSourceLang(), getTargetLang(), sourceFile, targetFile);
		formatter.format(alignmentList);
		writer.close();
		return new AlignmentStats(alignmentList.size(), 
				alignment.getSourceSegmentList().size(), alignment.getTargetSegmentList().size()) ;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	private Writer getSingleWriter(File outFile) throws UnsupportedEncodingException, FileNotFoundException {
		Writer writer;
		if (outFile!=null) {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"), true);
		} else {
			writer = new PrintWriter(new OutputStreamWriter((System.out),"UTF-8"), true);
		}
		return writer;
	}
	
	private AlignAlgorithm createAlgorithm(List<Alignment> alignmentList) throws Exception {
		String cls = this.getCls();
		AlignAlgorithm algorithm;
		if (cls.equals("fb") || cls.equals("viterbi")) {
			Calculator calculator = createCalculator(alignmentList);
			Map<Category, Float> categoryMap = 
				CategoryDefaults.BEST_CATEGORY_MAP;
			String search = this.getSearch();
			if (search == null) {
				throw new Exception("MissingParameter search");
			}
			if (search.equals("exhaustive") || search.equals("band")) {
				MatrixFactory matrixFactory;
				if (search.equals("exhaustive")) {
					matrixFactory = new FullMatrixFactory();
				} else if (search.equals("band")) {
					matrixFactory = new BandMatrixFactory(this.getRadius());
				} else {
					throw new Exception("UnknownParameter search");
				}
				if (cls.equals("viterbi")) {
					algorithm = new ViterbiAlgorithm(calculator, 
							categoryMap, matrixFactory);
				} else if (cls.equals("fb")) {
					algorithm = new ForwardBackwardAlgorithm(calculator, 
							categoryMap, matrixFactory);
				} else {
					throw new Exception("UnknownParameter class");
				}			
			} else if (search.equals("iterative-band")) {
				HmmAlignAlgorithmFactory algorithmFactory;
				if (cls.equals("viterbi")) {
					algorithmFactory = new ViterbiAlgorithmFactory();
				} else if (cls.equals("fb")) {
					algorithmFactory = new ForwardBackwardAlgorithmFactory();
				} else {
					throw new Exception("UnknownParameter class");
				}
				algorithm = new AdaptiveBandAlgorithm(algorithmFactory, 
						calculator, this.getRadius(), this.getIncrement(), this.getMargin(), 
						categoryMap);
			} else {
				throw new Exception("UnknownParameter search");
			}
		} else if (cls.equals("one-to-one")) {
			boolean one = this.getOneToOne();
			algorithm = new OneToOneAlgorithm(one);
		} else {
			throw new Exception("UnknownParameter class");
		}
		return algorithm;
	}
	
	private Counter createCounter() throws Exception {
		String ctr = this.getCounter();
		Counter counter;
		if (ctr == null) {
			counter = null;
		} else if (ctr.equals("word")) {
			counter = new SplitCounter();
		} else if (ctr.equals("char")) {
			counter = new CharCounter();
		} else {
			throw new Exception("UnknownParameter counter");
		}
		return counter;
	}
	
	private Calculator createCalculator(List<Alignment> alignmentList) throws Exception {
		String calculatorString = this.getCalculator();
		if (calculatorString == null) {
			throw new Exception("MissingParameter calculator");
		}
		List<String> calculatorStringList = Arrays.asList(calculatorString.split(","));
		return createCalculator(alignmentList, calculatorStringList);
	}
	
	private Calculator createCalculator(List<Alignment> alignmentList, List<String> calculatorStringList) throws Exception {

		List<Calculator> calculatorList = new ArrayList<Calculator>();
		Iterator<String> calculatorStringIterator = calculatorStringList.iterator();
		while (calculatorStringIterator.hasNext()) {
			String calculatorString = calculatorStringIterator.next();
			Calculator calculator;
			if (calculatorString.equals("normal")) {
				calculator = createNormalCalculator();
			} else if(calculatorString.equals("poisson")) {
				calculator = createPoissonCalculator(alignmentList);
			} else if(calculatorString.equals("translation")) {
				calculator = createTranslationCalculator();
			} else if(calculatorString.equals("oracle")) {
				List<String> remainingCalculatorStringList = 
					new ArrayList<String>();
				while (calculatorStringIterator.hasNext()) {
					remainingCalculatorStringList.add(calculatorStringIterator.next());
				}
				Calculator remainingCalculator = createCalculator(alignmentList, remainingCalculatorStringList);
				calculator = createOracleCalculator(remainingCalculator);
			} else {
				throw new Exception("UnknownParameter calculator");
			}
			calculatorList.add(calculator);
		}
		
		Calculator calculator;
		if (calculatorList.size() == 1) {
			calculator = calculatorList.get(0);
		} else {
			calculator = new CompositeCalculator(calculatorList);
		}
		return calculator;
	}
	
	
	private Calculator createNormalCalculator() throws Exception {
		Counter counter = createCounter();
		if (counter == null) {
			throw new Exception("MissingParameter counter");
		}
		Calculator calculator = new NormalDistributionCalculator(counter);
		return calculator;
	}

	private Calculator createPoissonCalculator(List<Alignment> alignmentList) throws Exception {
		Counter counter = createCounter();
		if (counter == null) {
			throw new Exception("MissingParameter counter");
		}
		String lengthCorpus = this.getLengthCorpus();
		List<Alignment> lengthAlignmentList; 
		if (lengthCorpus != null) {
			lengthAlignmentList = loadAlignmentList(lengthCorpus);
		} else {
			lengthAlignmentList = alignmentList;
		}
		Calculator calculator = new PoissonDistributionCalculator(counter, lengthAlignmentList);
		return calculator;
	}

	private Calculator createTranslationCalculator() throws Exception {
		Calculator calculator;
		iterations = this.getIterations();
		String translationCorpus = this.getTranslationCorpus();
		String languageModels = this.getLanguageModels();
		String transModel = this.getTransModel();
		
		Vocabulary sourceVocabulary = new Vocabulary();
		Vocabulary targetVocabulary = new Vocabulary();
		List<List<Integer>> sourceWidList = new ArrayList<List<Integer>>();
		List<List<Integer>> targetWidList = new ArrayList<List<Integer>>();

		if (translationCorpus != null) {
			List<Alignment> translationAlignmentList = loadAlignmentList(translationCorpus); 
			VocabularyUtil.tokenize(VocabularyUtil.DEFAULT_TOKENIZE_ALGORITHM, 
					translationAlignmentList, sourceVocabulary, 
					targetVocabulary, sourceWidList, targetWidList);
		}

		LanguageModel sourceLanguageModel = null;
		LanguageModel targetLanguageModel = null; 
		if (languageModels != null) {
			String[] languageModelArray = languageModels.split(",");
			if (languageModelArray.length != 2) {
				throw new Exception("ParameterFormat language-models");
			}
			sourceLanguageModel = loadLanguageModel(languageModelArray[0]);
			targetLanguageModel = loadLanguageModel(languageModelArray[1]);			
		} else {
			sourceLanguageModel = LanguageModelUtil.train(sourceWidList);
			targetLanguageModel = LanguageModelUtil.train(targetWidList);
		}

		TranslationModel translationModel = null;
		if (transModel != null) {
			translationModel = loadTranslationModel(transModel, sourceVocabulary, targetVocabulary);
		} else {
			translationModel = TranslationModelUtil.train(iterations, sourceWidList, targetWidList);
		}

		calculator = new TranslationCalculator(sourceVocabulary, 
					targetVocabulary, sourceLanguageModel, targetLanguageModel,
					translationModel, VocabularyUtil.DEFAULT_TOKENIZE_ALGORITHM);
		
		return calculator;
	}

	private Calculator createOracleCalculator(Calculator calculator) throws Exception {
		Calculator resultCalculator;
		String oracleCorpus = this.getOracleCorpus();
		if (oracleCorpus == null) {
			throw new Exception("MissingParameter oracle-corpus");
		}
		List<Alignment> oracleAlignmentList = loadAlignmentList(oracleCorpus);
		resultCalculator = new MinimumCalculator(new OracleCalculator(oracleAlignmentList), 
				calculator, OracleCalculator.DEFAULT_SUCCESS_SCORE);
		return resultCalculator;
		
	}

	private LanguageModel loadLanguageModel(String fileName) {
		Reader reader = getReader(getFileInputStream(fileName));
		return LanguageModelUtil.parse(reader);
	}

	private TranslationModel loadTranslationModel(String fileName, 
			Vocabulary sourceVocabulary, Vocabulary targetVocabulary) {
		Reader reader = getReader(getFileInputStream(fileName));
		return TranslationModelUtil.parse(reader, sourceVocabulary, 
				targetVocabulary);
	}
	
	private List<Alignment> loadAlignmentList(String fileName) {
		Reader reader = getReader(getFileInputStream(fileName));
		Parser parser = new AlParser(reader);
		return parser.parse();
	}
	

	/**
	 * @return the cls
	 */
	public String getCls() {
		return cls;
	}

	/**
	 * @param cls the cls to set
	 */
	public void setCls(String cls) {
		this.cls = cls;
	}

	/**
	 * @return the search
	 */
	public String getSearch() {
		return search;
	}

	/**
	 * @param search the search to set
	 */
	public void setSearch(String search) {
		this.search = search;
	}

	/**
	 * @return the oneToOne
	 */
	public Boolean getOneToOne() {
		return oneToOne;
	}

	/**
	 * @param oneToOne the oneToOne to set
	 */
	public void setOneToOne(Boolean oneToOne) {
		this.oneToOne = oneToOne;
	}

	/**
	 * @return the counter
	 */
	public String getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(String counter) {
		this.counter = counter;
	}

	/**
	 * @return the calculator
	 */
	public String getCalculator() {
		return calculator;
	}

	/**
	 * @param calculator the calculator to set
	 */
	public void setCalculator(String calculator) {
		this.calculator = calculator;
	}

	/**
	 * @return the lengthCorpus
	 */
	public String getLengthCorpus() {
		return lengthCorpus;
	}

	/**
	 * @param lengthCorpus the lengthCorpus to set
	 */
	public void setLengthCorpus(String lengthCorpus) {
		this.lengthCorpus = lengthCorpus;
	}

	/**
	 * @return the oracleCorpus
	 */
	public String getOracleCorpus() {
		return oracleCorpus;
	}

	/**
	 * @param oracleCorpus the oracleCorpus to set
	 */
	public void setOracleCorpus(String oracleCorpus) {
		this.oracleCorpus = oracleCorpus;
	}

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * @return the radius
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * @return the margin
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * @param margin the margin to set
	 */
	public void setMargin(int margin) {
		this.margin = margin;
	}

	/**
	 * @return the increment
	 */
	public float getIncrement() {
		return increment;
	}

	/**
	 * @param increment the increment to set
	 */
	public void setIncrement(float increment) {
		this.increment = increment;
	}

	/**
	 * @return the translationCorpus
	 */
	public String getTranslationCorpus() {
		return translationCorpus;
	}

	/**
	 * @param translationCorpus the translationCorpus to set
	 */
	public void setTranslationCorpus(String translationCorpus) {
		this.translationCorpus = translationCorpus;
	}

	/**
	 * @return the languageModels
	 */
	public String getLanguageModels() {
		return languageModels;
	}

	/**
	 * @param languageModels the languageModels to set
	 */
	public void setLanguageModels(String languageModels) {
		this.languageModels = languageModels;
	}

	/**
	 * @return the transModel
	 */
	public String getTransModel() {
		return transModel;
	}

	/**
	 * @param transModel the transModel to set
	 */
	public void setTransModel(String transModel) {
		this.transModel = transModel;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MalignaAligner ["
				+ (cls != null ? "cls=" + cls + ", " : "")
				+ (search != null ? "search=" + search + ", " : "")
				+ (counter != null ? "counter=" + counter + ", " : "")
				+ (oneToOne != null ? "oneToOne=" + oneToOne + ", " : "")
				+ (calculator != null ? "calculator=" + calculator + ", " : "")
				+ (lengthCorpus != null ? "lengthCorpus=" + lengthCorpus + ", "
						: "")
				+ (oracleCorpus != null ? "oracleCorpus=" + oracleCorpus + ", "
						: "")
				+ (translationCorpus != null ? "translationCorpus="
						+ translationCorpus + ", " : "")
				+ (languageModels != null ? "languageModels=" + languageModels
						+ ", " : "")
				+ (transModel != null ? "transModel=" + transModel + ", " : "")
				+ "iterations=" + iterations + ", radius=" + radius
				+ ", margin=" + margin + ", increment=" + increment + ", "
				+ (sourceLang != null ? "sourceLang=" + sourceLang + ", " : "")
				+ (targetLang != null ? "targetLang=" + targetLang + ", " : "")
				+ "sentenceSplitParagraphs=" + sentenceSplitParagraphs
				+ ", useBoilerplateParagraphs=" + useBoilerplateParagraphs
				+ ", useOoiLang=" + useOoiLang + "]";
	}

	
	
}
