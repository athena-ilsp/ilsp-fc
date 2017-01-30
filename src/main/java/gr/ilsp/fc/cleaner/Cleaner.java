package gr.ilsp.fc.cleaner;

import gr.ilsp.fc.cleaner.CleanerUtils.ParsAttr;
import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.Statistics;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Cleaner {
	private static final Logger LOGGER = Logger.getLogger(Cleaner.class);
	private static double thr_persent = 0.2; 
	private static double thr_std = 2;
	private static final String UNDERSCORE_STR="_";
	private static final String XMLEXT = ".xml";
	
	public static void main(String[] args) {
		File xmlDirName = new File(args[0]);	// cesDoc files to be examined are in this directory
		//int limit = 10;
		//if (args.length>1){
		//	limit = Integer.parseInt(args[1]);	//pars that occur in more than (1/limit) files are examined
		//}
		//ArrayList<File> files=FcFileUtils.getCesDocFiles(xmlDirName);  //all CesDocFiles
		
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(XMLEXT) &!arg1.contains(UNDERSCORE_STR) );
			}
		};
		List<File> files = FcFileUtils.listFiles(xmlDirName, filter,true);
				
		
//Set<String> boilerpars = CleanerUtils.getBoilerPars(files); //all boilerplate pars according to BoilerPipe
		HashMap<String, ParsAttr> boilerpars_attrs = CleanerUtils.getParsAttrs(files, "crawlinfo", "boilerplate", true);
		Set<String> temp = boilerpars_attrs.keySet();
		Iterator<String> it_temp = temp.iterator();
		Double[] boiler_freqs = new Double[temp.size()];
		String key_temp;
		int ii=0;
		while (it_temp.hasNext()){
			key_temp = it_temp.next();
			if (boilerpars_attrs.get(key_temp).filenames.size()<3){
				key_temp = it_temp.next();
				ParsAttr tempAttr = boilerpars_attrs.get(key_temp);
				LOGGER.info(key_temp);
				LOGGER.info(tempAttr.filenames.size());
				LOGGER.info(tempAttr.filenames);
				LOGGER.info(tempAttr.par_startids);
				LOGGER.info(tempAttr.par_endids);
				LOGGER.info("-----");
				boiler_freqs[ii]= (double)boilerpars_attrs.get(key_temp).filenames.size();
				ii++;
			}
		//	boiler_freqs[ii]= (double)boilerpars_attrs.get(key_temp).filenames.size();
			//System.out.println(boiler_freqs[ii]);
			//ii++;
		}
		//double med_boilerfreq = Statistics.getMedian(boiler_freqs);
		//HashMap<Double, Double> tt = Statistics.getBins(boiler_freqs);
		//HashMap<String, ArrayList<File>> langs_files = CleanerUtils.getFil;esPerLang(files);		//CesDocFiles per lang
		//double thr=0;
		//Set<String> langkeys = langs_files.keySet();
		//Iterator<String> it_lang = langkeys.iterator();
		//String lang_key;
		//files = new  ArrayList<File>();
		//Set<String> ilspboilerpars = new HashSet<String>();
		//while (it_lang.hasNext()){
		//	lang_key = it_lang.next();
		//	files = langs_files.get(lang_key);
		//thr = Math.max(10,(double)files.size()/(double)limit);
		//	LOGGER.info(files.size()+ " docs in " + lang_key);
		HashMap<String, ParsAttr> noboilerpars_attrs = CleanerUtils.getParsAttrs(files,"crawlinfo", "boilerplate", false);
		//double thr = 
		
		temp = detectBoilerPars(noboilerpars_attrs, 3);
		//ilspboilerpars = CleanerUtils.mergeSets(ilspboilerpars, temp);
		//ilspboilerpars.addAll(temp);
		//}
		//Set<String> intersection = new HashSet<String>(ilspboilerpars); // use the copy constructor
		//intersection.retainAll(boilerpars);
		
		//Set<String> setdif = checkUncommonPars(ilspboilerpars, boilerpars); //pars that are in arg1 nut not in arg2
		Iterator<String> it_par = temp.iterator();
		String par_key;
		while (it_par.hasNext()){
			par_key = it_par.next();
			ParsAttr tempAttr = noboilerpars_attrs.get(par_key);
			LOGGER.info(par_key);
			LOGGER.info(tempAttr.filenames.size());
			LOGGER.info(tempAttr.filenames);
			LOGGER.info(tempAttr.par_startids);
			LOGGER.info(tempAttr.par_endids);
			LOGGER.info("-----");
			//updateParsAttrs(xmlDirName, tempAttr);
		}
		
		//double thr = Math.max(10,(double)files.size()/(double)limit);
		//HashMap<String, ParsAttr> allpars_attrs = CleanerUtils.getParsAttrs(files);
		//Set<String> ilspboilerpars = detectBoilerPars(allpars_attrs, thr);

		//LOGGER.info("Frequent pars that have not been identified as boilerplate\n");
		//checkCommonPars(allpars_attrs, boilerpars,limit);
		//LOGGER.info("Frequent pars\n");
		//checkCommonPars(allpars_attrs, null,limit);


		/*while (it_lang.hasNext()){
			lang_key = it_lang.next();
			files = langs_files.get(lang_key);
			LOGGER.info(files.size()+ " docs in " + lang_key);
			HashMap<String, ParsAttr> pars_freqs = CleanerUtils.getParsAttrs(files);
			LOGGER.info(lang_key + " Frequent pars that have not been identified as boilerplate\n");
			checkCommonPars(pars_freqs, boilerpars,limit);
			LOGGER.info(lang_key + " Frequent pars\n");
			checkCommonPars(pars_freqs, null,limit);
		}*/
	}

	/*private static void updateParsAttrs(File xmlDirName, ParsAttr tempAttr) {
		List<String> filenames = tempAttr.filenames;
		
		
		
	}*/

	/*private static Set<String> checkUncommonPars(Set<String> set1,	Set<String> set2) {
		Set<String> setdif = new HashSet<String>();
		Iterator<String> it_par = set1.iterator();
		String par_key;
		LOGGER.info("---------");
		LOGGER.info("---------");
		LOGGER.info("---------");
		LOGGER.info("---------");
		while (it_par.hasNext()){
			par_key = it_par.next();
			if (!set2.contains(par_key)){
				LOGGER.info(par_key);
				setdif.add(par_key);
			}
		}
		return setdif;
	}*/

	/**
	 * returns paragraphs that occur more than thr times in the collection AND their position (in these documents of the collection) are almost constant
	 * @param allpars_freqs
	 * @param thr
	 * @return
	 */

	private static Set<String> detectBoilerPars(HashMap<String, ParsAttr> pars_attrs, double thr) {
		Set<String> boilerpars =  new HashSet<String>();
		HashMap<Double, Double> bins= new HashMap<Double, Double>();
		Set<String> pars = pars_attrs.keySet();
		Iterator<String> it_par = pars.iterator();
		String par_key;
		while (it_par.hasNext()){
			par_key = it_par.next();
			List<String> filenames= pars_attrs.get(par_key).filenames;
			if (filenames.size()>thr){
				List<Double> parids=pars_attrs.get(par_key).par_startids;
				Double[] ids = (Double[]) parids.toArray(new Double[0]);
				double std1 = Statistics.getStdDev(ids);
				if (std1< thr_std){
					LOGGER.info(par_key + " found "+ ids.length +" times");
					boilerpars.add(par_key);
					continue;
				}
				bins = Statistics.getBins(ids);
				//double mean1 = Statistics.getMean(ids);				//double median1 = Statistics.getMedian(ids);
				//LOGGER.info(mean1);				//LOGGER.info(median1);
				if (Statistics.isMainBin(bins, thr_persent)){
					LOGGER.info(par_key + " found "+ ids.length +" times");
					LOGGER.info(std1);
					boilerpars.add(par_key);
					continue;
				}

				parids=pars_attrs.get(par_key).par_endids;
				ids = (Double[]) parids.toArray(new Double[0]);
				bins = Statistics.getBins(ids);
				double std2 = Statistics.getStdDev(ids);
				if (std2<thr_std){
					LOGGER.info(par_key + " found "+ ids.length +" times");
					boilerpars.add(par_key);
					continue;
				}
				//double mean2 = Statistics.getMean(ids);				//double median2 = Statistics.getMedian(ids);
				//LOGGER.info(mean2);				//LOGGER.info(median2);
				if (Statistics.isMainBin(bins, thr_persent)){
					LOGGER.info(par_key + " found "+ ids.length +" times");
					LOGGER.info(std2);
					boilerpars.add(par_key);
					continue;
				}

				LOGGER.info(par_key);
				LOGGER.info("----------");
			}
		}
		return boilerpars;
	}

	/**
	 * Finds the paragraphs that 1) are non-boilerplate, and 2) occur in more than (1/limit) of the number of docs in the collection 
	 * @param allpars_freqs
	 * @param boilerpars
	 * @param  
	 *//*

	private static void checkCommonPars(HashMap<String, ParsAttr> allpars_freqs, Set<String> boilerpars, int limit) {
		String par_key;
		int par_freq;
		Set<String> pars = allpars_freqs.keySet();
		Iterator<String> it_par = pars.iterator();
		double thr = Math.max(10,(double)pars.size()/(double)limit);
		while (it_par.hasNext()){
			par_key = it_par.next();
			par_freq = allpars_freqs.get(par_key).filenames.size();
			if (par_freq>=thr){
				if (boilerpars==null){
					LOGGER.info("PARAGRAPH: "+ par_key);
					LOGGER.info("FREQUENCY "+par_freq);
					LOGGER.info("------");
				}else{
					if (!boilerpars.contains(par_key)){
						LOGGER.info("PARAGRAPH: "+ par_key);
						LOGGER.info("FREQUENCY "+par_freq);
						LOGGER.info("------");
					}
				}
			}
		}
	}*/

}
