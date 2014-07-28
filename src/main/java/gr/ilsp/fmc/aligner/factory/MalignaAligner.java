package gr.ilsp.fmc.aligner.factory;

import static net.sourceforge.align.util.Util.getFileInputStream;
import static net.sourceforge.align.util.Util.getReader;

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

import net.sourceforge.align.calculator.Calculator;
import net.sourceforge.align.calculator.content.OracleCalculator;
import net.sourceforge.align.calculator.content.TranslationCalculator;
import net.sourceforge.align.calculator.length.NormalDistributionCalculator;
import net.sourceforge.align.calculator.length.PoissonDistributionCalculator;
import net.sourceforge.align.calculator.length.counter.CharCounter;
import net.sourceforge.align.calculator.length.counter.Counter;
import net.sourceforge.align.calculator.length.counter.SplitCounter;
import net.sourceforge.align.calculator.meta.CompositeCalculator;
import net.sourceforge.align.calculator.meta.MinimumCalculator;
import net.sourceforge.align.coretypes.Alignment;
import net.sourceforge.align.coretypes.Category;
import net.sourceforge.align.coretypes.CategoryDefaults;
import net.sourceforge.align.filter.aligner.align.AlignAlgorithm;
import net.sourceforge.align.filter.aligner.align.hmm.HmmAlignAlgorithmFactory;
import net.sourceforge.align.filter.aligner.align.hmm.adaptive.AdaptiveBandAlgorithm;
import net.sourceforge.align.filter.aligner.align.hmm.fb.ForwardBackwardAlgorithm;
import net.sourceforge.align.filter.aligner.align.hmm.fb.ForwardBackwardAlgorithmFactory;
import net.sourceforge.align.filter.aligner.align.hmm.viterbi.ViterbiAlgorithm;
import net.sourceforge.align.filter.aligner.align.hmm.viterbi.ViterbiAlgorithmFactory;
import net.sourceforge.align.filter.aligner.align.onetoone.OneToOneAlgorithm;
import net.sourceforge.align.formatter.Formatter;
import net.sourceforge.align.formatter.TmxFormatter;
import net.sourceforge.align.matrix.BandMatrixFactory;
import net.sourceforge.align.matrix.FullMatrixFactory;
import net.sourceforge.align.matrix.MatrixFactory;
import net.sourceforge.align.model.language.LanguageModel;
import net.sourceforge.align.model.language.LanguageModelUtil;
import net.sourceforge.align.model.translation.TranslationModel;
import net.sourceforge.align.model.translation.TranslationModelUtil;
import net.sourceforge.align.model.vocabulary.Vocabulary;
import net.sourceforge.align.model.vocabulary.VocabularyUtil;
import net.sourceforge.align.parser.AlParser;
import net.sourceforge.align.parser.Parser;
import net.sourceforge.align.ui.console.command.exception.MissingParameterException;
import net.sourceforge.align.ui.console.command.exception.ParameterFormatException;
import net.sourceforge.align.ui.console.command.exception.UnknownParameterException;

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
	}

	@Override
	public void process(File sourceFile, File targetFile, File tmxFile) throws Exception {
		logger.debug(this.toString());

		List<String> sourceSentences = new ArrayList<String>();
		List<String> targetSentences = new ArrayList<String>();
		for (String sourceParagraph:  getParagraphs(sourceFile, isUseBoilerplateParagraphs())) {
			sourceSentences.addAll(sentenceSplitter.getSentences(sourceParagraph, 1));
		}
		for (String targetParagraph:  getParagraphs(targetFile, isUseBoilerplateParagraphs())) {
			targetSentences.addAll(sentenceSplitter.getSentences(targetParagraph, 1));
		}

		Alignment alignment = new Alignment(sourceSentences, targetSentences);
		List<Alignment> alignmentList = Collections.singletonList(alignment);
		AlignAlgorithm alignAlgorithm = createAlgorithm(alignmentList);
		net.sourceforge.align.filter.aligner.Aligner aligner = new net.sourceforge.align.filter.aligner.Aligner(alignAlgorithm);
		alignmentList = aligner.apply(alignmentList);

		Writer writer = getSingleWriter(tmxFile);
		Formatter formatter = new TmxFormatter(writer, getSourceLang(), getTargetLang());
		formatter.format(alignmentList);

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	private Writer getSingleWriter(File outFile) throws UnsupportedEncodingException, FileNotFoundException {
		Writer writer;
		if (outFile!=null) {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile),"utf-8"), true);
		} else {
			writer = new PrintWriter(new OutputStreamWriter((System.out),"utf-8"), true);
		}
		return writer;
	}

	
	private AlignAlgorithm createAlgorithm(List<Alignment> alignmentList) {
		String cls = this.getCls();
		AlignAlgorithm algorithm;
		if (cls.equals("fb") || cls.equals("viterbi")) {
			Calculator calculator = createCalculator(alignmentList);
			Map<Category, Float> categoryMap = 
				CategoryDefaults.BEST_CATEGORY_MAP;
			String search = this.getSearch();
			if (search == null) {
				throw new MissingParameterException("search");
			}
			if (search.equals("exhaustive") || search.equals("band")) {
				MatrixFactory matrixFactory;
				if (search.equals("exhaustive")) {
					matrixFactory = new FullMatrixFactory();
				} else if (search.equals("band")) {
					matrixFactory = new BandMatrixFactory(this.getRadius());
				} else {
					throw new UnknownParameterException("search");
				}
				if (cls.equals("viterbi")) {
					algorithm = new ViterbiAlgorithm(calculator, 
							categoryMap, matrixFactory);
				} else if (cls.equals("fb")) {
					algorithm = new ForwardBackwardAlgorithm(calculator, 
							categoryMap, matrixFactory);
				} else {
					throw new UnknownParameterException("class");
				}			
			} else if (search.equals("iterative-band")) {
				HmmAlignAlgorithmFactory algorithmFactory;
				if (cls.equals("viterbi")) {
					algorithmFactory = new ViterbiAlgorithmFactory();
				} else if (cls.equals("fb")) {
					algorithmFactory = new ForwardBackwardAlgorithmFactory();
				} else {
					throw new UnknownParameterException("class");
				}
				algorithm = new AdaptiveBandAlgorithm(algorithmFactory, 
						calculator, this.getRadius(), this.getIncrement(), this.getMargin(), 
						categoryMap);
			} else {
				throw new UnknownParameterException("search");
			}
		} else if (cls.equals("one-to-one")) {
			boolean one = this.getOneToOne();
			algorithm = new OneToOneAlgorithm(one);
		} else {
			throw new UnknownParameterException("class");
		}
		return algorithm;
	}
	
	private Counter createCounter() {
		String ctr = this.getCounter();
		Counter counter;
		if (ctr == null) {
			counter = null;
		} else if (ctr.equals("word")) {
			counter = new SplitCounter();
		} else if (ctr.equals("char")) {
			counter = new CharCounter();
		} else {
			throw new UnknownParameterException("counter");
		}
		return counter;
	}
	
	private Calculator createCalculator(List<Alignment> alignmentList) {
		String calculatorString = this.getCalculator();
		if (calculatorString == null) {
			throw new MissingParameterException("calculator");
		}
		List<String> calculatorStringList = Arrays.asList(calculatorString.split(","));
		return createCalculator(alignmentList, calculatorStringList);
	}
	
	private Calculator createCalculator(List<Alignment> alignmentList, List<String> calculatorStringList) {

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
				throw new UnknownParameterException("calculator");
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
	
	
	private Calculator createNormalCalculator() {
		Counter counter = createCounter();
		if (counter == null) {
			throw new MissingParameterException("counter");
		}
		Calculator calculator = new NormalDistributionCalculator(counter);
		return calculator;
	}

	private Calculator createPoissonCalculator(List<Alignment> alignmentList) {
		Counter counter = createCounter();
		if (counter == null) {
			throw new MissingParameterException("counter");
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

	private Calculator createTranslationCalculator() {
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
				throw new ParameterFormatException("language-models");
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

	private Calculator createOracleCalculator(Calculator calculator) {
		Calculator resultCalculator;
		String oracleCorpus = this.getOracleCorpus();
		if (oracleCorpus == null) {
			throw new MissingParameterException("oracle-corpus");
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
				+ ", margin=" + margin + ", increment=" + increment + "]";
	}
	
}
