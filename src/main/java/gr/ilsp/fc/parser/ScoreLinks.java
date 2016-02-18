package gr.ilsp.fc.parser;


import org.apache.log4j.Logger;

import gr.ilsp.fc.classifier.Classifier;
import gr.ilsp.fc.datums.ClassifierDatum;
import gr.ilsp.fc.datums.CrawlDbDatum;
import gr.ilsp.fc.datums.ExtendedParsedDatum;
import gr.ilsp.fc.datums.ExtendedUrlDatum;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.utils.ContentNormalizer;
//import gr.ilsp.fc.utils.ContentNormalizer;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop.HadoopFlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.TupleEntryCollector;

import com.bixolabs.cascading.LoggingFlowProcess;
import com.bixolabs.cascading.LoggingFlowReporter;
import com.bixolabs.cascading.NullContext;


public class ScoreLinks extends BaseOperation<NullContext> implements Function<NullContext> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 864053504476981356L;
	private static final Logger LOGGER = Logger.getLogger(ScoreLinks.class);
	private Classifier _classifier = null;
	private transient LoggingFlowProcess _flowProcess;
	private enum ScoreLinksCounters {
		SCORING_LINKS_NUMBER, SCORING_LINKS_TIME, SCORING_LINKS_TUNNEL_REJECTED
	}
	public ScoreLinks(Classifier classifier) {
		super(ExtendedUrlDatum.FIELDS);
		_classifier = classifier;        
	}

	@Override
	public void prepare(FlowProcess process, OperationCall<NullContext> operationCall) {
		super.prepare(process, operationCall);
		_flowProcess = new LoggingFlowProcess((HadoopFlowProcess)process);
		_flowProcess.addReporter(new LoggingFlowReporter());
	}

	@Override
	public void cleanup(FlowProcess process, OperationCall<NullContext> operationCall) {
		LOGGER.debug("Ending outlink scoring");
		_flowProcess.dumpCounters();
		super.cleanup(process, operationCall);
	}

	@Override
	public void operate(FlowProcess process, FunctionCall<NullContext> funcCall) {
		long time = System.currentTimeMillis();
		ExtendedUrlDatum resultDatum = null;
		ExtendedParsedDatum datum = null;
		
		datum = new ExtendedParsedDatum(funcCall.getArguments());
		
		ExtendedOutlink[] outlinks = datum.getOutlinks();
		double score = datum.getTupleEntry().getDouble(ClassifierDatum.TOTABSCORE) / outlinks.length;
		int crawlDepth = (Integer) datum.getPayloadValue(CrawlDbDatum.CRAWL_DEPTH);
		double vv=datum.getTupleEntry().getDouble(ClassifierDatum.TOTABSCORE);
				
		//added this for tunneling reasons when crawling without topic
		if (_classifier.getTopic()!=null){ 
			if (score>0.0) 
				crawlDepth = -1;
		}else
			crawlDepth = -1;
				
		if (crawlDepth<_classifier.getMaxDepth()) {
			datum.setPayloadValue(CrawlDbDatum.CRAWL_DEPTH, crawlDepth + 1);        
			TupleEntryCollector collector = funcCall.getOutputCollector();
			//ExtendedUrlDatum[] datums = new ExtendedUrlDatum[outlinks.length];
			//String normtext1 = ContentNormalizer.normalizeText(datum.getParsedText()).toLowerCase();
			String normtext = ContentNormalizer.cleanContent(datum.getParsedText()).toLowerCase();
			//String alltext = datum.getParsedText();
			String pagelang= LangDetectUtils.detectLanguage(normtext);
			for (ExtendedOutlink outlink : outlinks) {   
				String linktext = outlink.getAnchor() + " " + outlink.getSurroundText();
				String linktext1 = outlink.getAnchor();
				LOGGER.debug(outlink.getSurroundText());
				String url = outlink.getToUrl();
				double linkScore = 0.0;
				LOGGER.debug("\t\t\t"+outlink.getToUrl());
				LOGGER.debug(outlink.getAnchor());
				//if (_filter!=null)
				if (_classifier.getTopic()!=null){ 
					linkScore = _classifier.rankLink(linktext, linktext1,pagelang,vv);
					//linkScore += _classifier.rankLink(linktext1,pagelang,vv);
					//linkScore = linkScore+_classifier.rankLink1(url);
				}else{
					linkScore = _classifier.rankLinkNotopic(linktext, linktext1,pagelang,vv);
					//FIXME TO PROMOTE URLS with special strings/patterns
					//if (url.contains("/cikk/16") || url.contains("/cikk/15") || url.contains("english.")){
					//if (url.contains("/en/")){
					//	linkScore = linkScore+100000;
					//}
				}
				LOGGER.debug(linkScore);
				if (url==null) continue;
				url = url.replaceAll("[\n\r]", "");         
				resultDatum = new ExtendedUrlDatum(url);                    
				resultDatum.setPayload(datum.getPayload());           
				resultDatum.setScore(score + linkScore);    
				collector.add(resultDatum.getTuple());
				_flowProcess.increment(ScoreLinksCounters.SCORING_LINKS_NUMBER, 1);
			}
		} else{
			LOGGER.debug("SCORING_LINKS_TUNNEL_REJECTED:"+ScoreLinksCounters.SCORING_LINKS_TUNNEL_REJECTED.toString());
			_flowProcess.increment(ScoreLinksCounters.SCORING_LINKS_TUNNEL_REJECTED,outlinks.length);
		}
		_flowProcess.increment(ScoreLinksCounters.SCORING_LINKS_TIME, (int)(System.currentTimeMillis()-time));
		LOGGER.debug("EXTRACTION FINISHED");
	}




}
