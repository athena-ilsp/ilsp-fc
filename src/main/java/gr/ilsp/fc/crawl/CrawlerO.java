package gr.ilsp.fc.crawl;



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;

import bixo.config.FetcherPolicy;
//import bixo.config.FetcherPolicy.FetcherMode;
import bixo.config.UserAgent;
//import bixo.datum.UrlStatus;
import bixo.urls.BaseUrlFilter;


public class CrawlerO{

	protected JobConf jobconf = null;
	protected CompositeConfiguration configuration;
	protected String configFile;
	/**
	 * @return the configFile
	 */
	public String getConfigFile() {
		return configFile;
	}
	/**
	 * @param configFile the configFile to set
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	protected String loggingAppender = null;
	//parameters for domain classification
	protected ArrayList<String[]> topic;
	protected String[] classes;
	protected double abs_thres = 0;
	protected double rel_thres=0;
	protected int min_uniq_terms=0;

	protected int max_requests_per_run = 10000;

	protected boolean force, iso6393, isDomainFile;
	protected int minParLen;
	protected int minDocLen;
	protected int level;
	protected int depth;
	protected int threads=10;

	protected int durationNumLoops, startLoop, endLoop;
	protected int durationMinutes;
	protected long targetEndTime;
	
	protected Path crawlDbPath;
	
	protected String type;
	protected String agentName;
	protected String userTopic;
	protected String filter;
	protected String storefilter;
	protected String webMainDomain;
	protected String webDomain;

	protected String[] targetlangs;
	protected HashMap<String, String> mapLangs;
	protected String[] langKeys;
	protected Map<String, String> uRLPairsFromTranslationLinks;
	
	protected int maxTunnelingDepth;
	protected List<String[]> linkAttrs;
	protected File topicFile;
	protected File outputDir;
	protected File seedsFile;
	//protected File baseName;
	protected Set<String> validMimes;
	protected FetcherPolicy policy; 
	protected Path outputDirPath;
	protected UserAgent userAgent;
	protected BaseUrlFilter urlLevelFilter, urlDomainFilter;
	protected FileSystem fs;
	protected Path inputPath;
	protected boolean hasNumLoops;
	protected boolean hasEndTime;
	protected boolean keepboiler;
	protected boolean isdegub;

	
	/**
	 * @return the urlPairsFromTranslationLinks, the pairs of URLs detected during crawling as translation links
	 */
	public Map<String, String> getURLPairsFromTranslationLinks() {
		return uRLPairsFromTranslationLinks;
	}
	/**
	 * @param urlPairsFromTranslationLinks (the URL pairs found during crawling as translation links) 
	 */
	public void setURLPairsFromTranslationLinks(
			Map<String, String> uRLPairsFromTranslationLinks) {
		this.uRLPairsFromTranslationLinks = uRLPairsFromTranslationLinks;
	}
	/**
	 * @return the jobconf
	 */
	public JobConf getJobconf() {
		return jobconf;
	}
	/**
	 * @param jobconf the jobconf to set
	 */
	public void setJobconf(JobConf jobconf) {
		this.jobconf = jobconf;
	}
	/**
	 * @return the configuration
	 */
	public CompositeConfiguration getConfiguration() {
		return configuration;
	}
	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(CompositeConfiguration configuration) {
		this.configuration = configuration;
	}
	/**
	 * @return the loggingAppender
	 */
	public String getLoggingAppender() {
		return loggingAppender;
	}
	/**
	 * @param loggingAppender the loggingAppender to set
	 */
	public void setLoggingAppender(String loggingAppender) {
		this.loggingAppender = loggingAppender;
	}
	/**
	 * @return the topic
	 */
	public ArrayList<String[]> getTopic() {
		return topic;
	}
	/**
	 * @param topic the topic to set
	 */
	public void setTopic(ArrayList<String[]> topic) {
		this.topic = topic;
	}
	/**
	 * @return the classes
	 */
	public String[] getClasses() {
		return classes;
	}
	/**
	 * @param classes the classes to set
	 */
	public void setClasses(String[] classes) {
		this.classes = classes;
	}
	/**
	 * @return the abs_thres
	 */
	public double getAbs_thres() {
		return abs_thres;
	}
	/**
	 * @param abs_thres the abs_thres to set
	 */
	public void setAbs_thres(double abs_thres) {
		this.abs_thres = abs_thres;
	}
	/**
	 * @return the rel_thres
	 */
	public double getRel_thres() {
		return rel_thres;
	}
	/**
	 * @param rel_thres the rel_thres to set
	 */
	public void setRel_thres(double rel_thres) {
		this.rel_thres = rel_thres;
	}
	/**
	 * @return the min_uniq_terms
	 */
	public int getMin_uniq_terms() {
		return min_uniq_terms;
	}
	/**
	 * @param min_uniq_terms the min_uniq_terms to set
	 */
	public void setMin_uniq_terms(int min_uniq_terms) {
		this.min_uniq_terms = min_uniq_terms;
	}
	/**
	 * @return the max_requests_per_run
	 */
	public int getMax_requests_per_run() {
		return max_requests_per_run;
	}
	/**
	 * @param max_requests_per_run the max_requests_per_run to set
	 */
	public void setMax_requests_per_run(int max_requests_per_run) {
		this.max_requests_per_run = max_requests_per_run;
	}
	/**
	 * @return the force
	 */
	public boolean isForce() {
		return force;
	}
	/**
	 * @param force the force to set
	 */
	public void setForce(boolean force) {
		this.force = force;
	}
	/**
	 * @return the iso6393
	 */
	public boolean isIso6393() {
		return iso6393;
	}
	/**
	 * @param iso6393 the iso6393 to set
	 */
	public void setIso6393(boolean iso6393) {
		this.iso6393 = iso6393;
	}
	/**
	 * @return the isDomainFile
	 */
	public boolean isDomainFile() {
		return isDomainFile;
	}
	/**
	 * @param isDomainFile the isDomainFile to set
	 */
	public void setDomainFile(boolean isDomainFile) {
		this.isDomainFile = isDomainFile;
	}
	/**
	 * @return the minParLen
	 */
	public int getMinParLen() {
		return minParLen;
	}
	/**
	 * @param minParLen the minParLen to set
	 */
	public void setMinParLen(int minParLen) {
		this.minParLen = minParLen;
	}
	/**
	 * @return the minDocLen
	 */
	public int getMinDocLen() {
		return minDocLen;
	}
	/**
	 * @param minDocLen the minDocLen to set
	 */
	public void setMinDocLen(int minDocLen) {
		this.minDocLen = minDocLen;
	}
	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}
	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	/**
	 * @return the threads
	 */
	public int getThreads() {
		return threads;
	}
	/**
	 * @param threads the threads to set
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}
	/**
	 * @return the durationNumLoops
	 */
	public int getDurationNumLoops() {
		return durationNumLoops;
	}
	/**
	 * @param durationNumLoops the durationNumLoops to set
	 */
	public void setDurationNumLoops(int durationNumLoops) {
		this.durationNumLoops = durationNumLoops;
	}
	/**
	 * @return the startLoop
	 */
	public int getStartLoop() {
		return startLoop;
	}
	/**
	 * @param startLoop the startLoop to set
	 */
	public void setStartLoop(int startLoop) {
		this.startLoop = startLoop;
	}
	/**
	 * @return the endLoop
	 */
	public int getEndLoop() {
		return endLoop;
	}
	/**
	 * @param endLoop the endLoop to set
	 */
	public void setEndLoop(int endLoop) {
		this.endLoop = endLoop;
	}
	/**
	 * @return the durationMinutes
	 */
	public int getDurationMinutes() {
		return durationMinutes;
	}
	/**
	 * @param durationMinutes the durationMinutes to set
	 */
	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
	/**
	 * @return the targetEndTime
	 */
	public long getTargetEndTime() {
		return targetEndTime;
	}
	/**
	 * @param targetEndTime the targetEndTime to set
	 */
	public void setTargetEndTime(long targetEndTime) {
		this.targetEndTime = targetEndTime;
	}
	/**
	 * @return the crawlDbPath
	 */
	public Path getCrawlDbPath() {
		return crawlDbPath;
	}
	/**
	 * @param crawlDbPath the crawlDbPath to set
	 */
	public void setCrawlDbPath(Path crawlDbPath) {
		this.crawlDbPath = crawlDbPath;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the agentName
	 */
	public String getAgentName() {
		return agentName;
	}
	/**
	 * @param agentName the agentName to set
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	/**
	 * @return the userTopic
	 */
	public String getUserTopic() {
		return userTopic;
	}
	/**
	 * @param userTopic the userTopic to set
	 */
	public void setUserTopic(String userTopic) {
		this.userTopic = userTopic;
	}
	/**
	 * @return the filter
	 */
	public String getFilter() {
		return filter;
	}
	/**
	 * @param filter the filter to set
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}
	/**
	 * @return the storefilter
	 */
	public String getStorefilter() {
		return storefilter;
	}
	/**
	 * @param storefilter the storefilter to set
	 */
	public void setStorefilter(String storefilter) {
		this.storefilter = storefilter;
	}
	/**
	 * @return the webMainDomain
	 */
	public String getWebMainDomain() {
		return webMainDomain;
	}
	/**
	 * @param webMainDomain the webMainDomain to set
	 */
	public void setWebMainDomain(String webMainDomain) {
		this.webMainDomain = webMainDomain;
	}
	/**
	 * @return the webDomain
	 */
	public String getWebDomain() {
		return webDomain;
	}
	/**
	 * @param webDomain the webDomain to set
	 */
	public void setWebDomain(String webDomain) {
		this.webDomain = webDomain;
	}
	/**
	 * @return the targetlangs
	 */
	public String[] getTargetlangs() {
		return targetlangs;
	}
	/**
	 * @param targetlangs the targetlangs to set
	 */
	public void setTargetlangs(String[] targetlangs) {
		this.targetlangs = targetlangs;
	}
	/**
	 * @return the mapLangs
	 */
	public HashMap<String, String> getMapLangs() {
		return mapLangs;
	}
	/**
	 * @param mapLangs the mapLangs to set
	 */
	public void setMapLangs(HashMap<String, String> mapLangs) {
		this.mapLangs = mapLangs;
	}
	/**
	 * @return the langKeys
	 */
	public String[] getLangKeys() {
		return langKeys;
	}
	/**
	 * @param langKeys the langKeys to set
	 */
	public void setLangKeys(String[] langKeys) {
		this.langKeys = langKeys;
	}
	/**
	 * @return the maxTunnelingDepth
	 */
	public int getMaxTunnelingDepth() {
		return maxTunnelingDepth;
	}
	/**
	 * @param maxTunnelingDepth the maxTunnelingDepth to set
	 */
	public void setMaxTunnelingDepth(int maxTunnelingDepth) {
		this.maxTunnelingDepth = maxTunnelingDepth;
	}
	/**
	 * @return the linkAttrs
	 */
	public List<String[]> getLinkAttrs() {
		return linkAttrs;
	}
	/**
	 * @param linkAttrs the linkAttrs to set
	 */
	public void setLinkAttrs(List<String[]> linkAttrs) {
		this.linkAttrs = linkAttrs;
	}
	/**
	 * @return the topicFile
	 */
	public File getTopicFile() {
		return topicFile;
	}
	/**
	 * @param topicFile the topicFile to set
	 */
	public void setTopicFile(File topicFile) {
		this.topicFile = topicFile;
	}
	/**
	 * @return the outputDir
	 */
	public File getOutputDir() {
		return outputDir;
	}
	/**
	 * @param outputDir the outputDir to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	/**
	 * @return the seedsFile
	 */
	public File getSeedsFile() {
		return seedsFile;
	}
	/**
	 * @param seedsFile the seedsFile to set
	 */
	public void setSeedsFile(File seedsFile) {
		this.seedsFile = seedsFile;
	}
	/**
	 * @return the validMimes
	 */
	public Set<String> getValidMimes() {
		return validMimes;
	}
	/**
	 * @param validMimes the validMimes to set
	 */
	public void setValidMimes(Set<String> validMimes) {
		this.validMimes = validMimes;
	}
	/**
	 * @return the policy
	 */
	public FetcherPolicy getPolicy() {
		return policy;
	}
	/**
	 * @param policy the policy to set
	 */
	public void setPolicy(FetcherPolicy policy) {
		this.policy = policy;
	}
	/**
	 * @return the outputDirPath
	 */
	public Path getOutputDirPath() {
		return outputDirPath;
	}
	/**
	 * @param outputDirPath the outputDirPath to set
	 */
	public void setOutputDirPath(Path outputDirPath) {
		this.outputDirPath = outputDirPath;
	}
	/**
	 * @return the userAgent
	 */
	public UserAgent getUserAgent() {
		return userAgent;
	}
	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(UserAgent userAgent) {
		this.userAgent = userAgent;
	}
	/**
	 * @return the urlLevelFilter
	 */
	public BaseUrlFilter getUrlLevelFilter() {
		return urlLevelFilter;
	}
	/**
	 * @param urlLevelFilter the urlLevelFilter to set
	 */
	public void setUrlLevelFilter(BaseUrlFilter urlLevelFilter) {
		this.urlLevelFilter = urlLevelFilter;
	}
	/**
	 * @return the urlDomainFilter
	 */
	public BaseUrlFilter getUrlDomainFilter() {
		return urlDomainFilter;
	}
	/**
	 * @param urlDomainFilter the urlDomainFilter to set
	 */
	public void setUrlDomainFilter(BaseUrlFilter urlDomainFilter) {
		this.urlDomainFilter = urlDomainFilter;
	}
	/**
	 * @return the fs
	 */
	public FileSystem getFs() {
		return fs;
	}
	/**
	 * @param fs the fs to set
	 */
	public void setFs(FileSystem fs) {
		this.fs = fs;
	}
	/**
	 * @return the inputPath
	 */
	public Path getInputPath() {
		return inputPath;
	}
	/**
	 * @param inputPath the inputPath to set
	 */
	public void setInputPath(Path inputPath) {
		this.inputPath = inputPath;
	}
	/**
	 * @return the hasNumLoops
	 */
	public boolean isHasNumLoops() {
		return hasNumLoops;
	}
	/**
	 * @param hasNumLoops the hasNumLoops to set
	 */
	public void setHasNumLoops(boolean hasNumLoops) {
		this.hasNumLoops = hasNumLoops;
	}
	/**
	 * @return the hasEndTime
	 */
	public boolean isHasEndTime() {
		return hasEndTime;
	}
	/**
	 * @param hasEndTime the hasEndTime to set
	 */
	public void setHasEndTime(boolean hasEndTime) {
		this.hasEndTime = hasEndTime;
	}
	/**
	 * @return the keepboiler
	 */
	public boolean isKeepboiler() {
		return keepboiler;
	}
	/**
	 * @param keepboiler the keepboiler to set
	 */
	public void setKeepboiler(boolean keepboiler) {
		this.keepboiler = keepboiler;
	}
	/**
	 * @return the isdegub
	 */
	public boolean isIsdegub() {
		return isdegub;
	}
	/**
	 * @param isdegub the isdegub to set
	 */
	public void setIsdegub(boolean isdegub) {
		this.isdegub = isdegub;
	}
}


