package gr.ilsp.fmc.parser;





import org.apache.commons.validator.UrlValidator;

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
	private String _mainhost;
	private transient LoggingFlowProcess _flowProcess;
	//private static final Logger LOGGER = Logger.getLogger(ExtendedUrlFilter.class);


	public ExtendedUrlFilter(BaseUrlFilter filter) {
		_filter = filter;
	}

	public ExtendedUrlFilter(BaseUrlFilter urlFilter, String mainhost) {
		// TODO Auto-generated constructor stub
		_filter = urlFilter;
		_mainhost = mainhost;
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
		//BaseUrlFilter filter=_filter;
		String aaa=datum.getUrl();
				
		if (aaa.startsWith("ftp") || aaa.equals("http:/") || aaa.isEmpty() || aaa.length()<7)
			return false;
		/*UrlValidator urlValidator = new UrlValidator(UrlValidator.NO_FRAGMENTS);
		if (!urlValidator.isValid(aaa) && !aaa.contains("#")){
			System.out.println(aaa);
			return false;
		}*/
			

		//filter.
		String mainhost;
		if (_mainhost==null)
			mainhost=aaa;
		else
			mainhost=_mainhost;

		//if (_filter.isRemove(datum)) {
		if (_filter.isRemove(datum) && !aaa.contains(mainhost)) {
			//process.increment(ImportCounters.URLS_FILTERED, 1);
			//_numFiltered += 1;
			//System.out.println("filtered: "+aaa);
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
