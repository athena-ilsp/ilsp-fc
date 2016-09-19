package gr.ilsp.fc.parser;

//import org.apache.commons.validator.UrlValidator;
import org.apache.log4j.Logger;

import bixo.datum.UrlDatum;
import bixo.hadoop.ImportCounters;
import bixo.urls.BaseUrlFilter;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop.HadoopFlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.operation.OperationCall;

import com.bixolabs.cascading.LoggingFlowProcess;
import com.bixolabs.cascading.LoggingFlowReporter;
import com.bixolabs.cascading.NullContext;


public class ExtendedUrlFilter extends BaseOperation<NullContext> implements Filter<NullContext> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5138525444745041509L;
	private BaseUrlFilter _filter;
	private BaseUrlFilter _filterl;
	private String _mainhost;
	private int _level;
	private transient LoggingFlowProcess _flowProcess;
	private static final Logger LOGGER = Logger.getLogger(ExtendedUrlFilter.class);


	public ExtendedUrlFilter(BaseUrlFilter urlDomainFilter, BaseUrlFilter urlLevelFilter) {
		_filter = urlDomainFilter;
		_filterl = urlLevelFilter;
	}

	public ExtendedUrlFilter(BaseUrlFilter urlDomainFilter, BaseUrlFilter urlLevelFilter, String mainhost, int level) {
		// TODO Auto-generated constructor stub
		_filter = urlDomainFilter;
		_filterl = urlLevelFilter;
		_mainhost = mainhost;
		_level = level;
	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {
		super.prepare(flowProcess, operationCall);
		_flowProcess = new LoggingFlowProcess((HadoopFlowProcess)flowProcess);
		_flowProcess.addReporter(new LoggingFlowReporter());
	}

	@Override
	public boolean isRemove(FlowProcess process, FilterCall<NullContext> filterCall) {
		UrlDatum datum = new UrlDatum(filterCall.getArguments());
		String aaa=datum.getUrl();
		if (aaa.startsWith("ftp") || aaa.contains("mailto:")|| aaa.equals("http:/") || aaa.isEmpty() || aaa.length()<7 ) 
			return false;
		
		String mainhost;
		if (_mainhost==null)
			mainhost=aaa;
		else
			mainhost=_mainhost;

		if ((_filter.isRemove(datum) && !aaa.contains(mainhost)) || (_filterl.isRemove(datum)) ) {
			//process.increment(ImportCounters.URLS_FILTERED, 1);
			//_numFiltered += 1;
			LOGGER.debug("filtered: "+aaa);
			_flowProcess.increment(ImportCounters.URLS_FILTERED, 1);
			return true;
		} else {
			//System.out.println("passed: "+aaa);
			_flowProcess.increment(ImportCounters.URLS_ACCEPTED, 1);
			return false;
		}
	}

	@Override
	public void cleanup(FlowProcess process, OperationCall<NullContext> operationCall) {
		_flowProcess.dumpCounters();
		super.cleanup(process, operationCall);
	}

}
