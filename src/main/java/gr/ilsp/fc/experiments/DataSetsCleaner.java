package gr.ilsp.fc.experiments;

import gr.ilsp.fc.utils.ContentNormalizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.apache.log4j.Logger;

public class DataSetsCleaner {
	private static final Logger LOGGER = Logger.getLogger(DataSetsCleaner.class);
	private static DataSetsCleanerOptions options = null;

	private static String process;
	private static String cols1, cols2;
	private static File infile1, infile2, outfile1;

	private static final String TAB_STR = "\t";
	private static final String SEMICOLON_STR = ";";
	private static final String SPACE_STR = " ";


	public static void main(String[] args) throws IOException {

		DataSetsCleaner dsc = new DataSetsCleaner();
		options = new DataSetsCleanerOptions();
		options.parseOptions(args);
		dsc.setProcess(options.getProcess());
		dsc.setCols1(options.getCols1());
		dsc.setCols2(options.getCols2());
		dsc.setInFile1(options.getInFile1());
		dsc.setInFile2(options.getInFile2());
		dsc.setOutFile1(options.getOutFile1());
		dsc.createSets();
	}

	private void createSets() throws IOException {
		List<String> newdataset = new ArrayList<String>();
		List<String> list1 = FileUtils.readLines(infile1);
		if (process.equals("1")){
			LOGGER.info("Parts deduplication. The parts are defined by columns. It is expected a tab separated text file as input");
			newdataset=removeIdenticalPartsPerDataSet(list1,cols1);
			FileUtils.writeLines(outfile1, newdataset);
		}
		if (process.equals("2")){
			LOGGER.info("Comparison of two files. Common parts are excluded from the first file");
			List<String> list2 = FileUtils.readLines(infile2);
			newdataset=removeCommonPartsFromDataSets(list1,cols1,list2,cols2);
			FileUtils.writeLines(outfile1, newdataset);
		}
	}

	private List<String> removeCommonPartsFromDataSets(List<String> list1,
			String cols1, List<String> list2, String cols2) {
		LOGGER.info("Starting from "+ list1.size()+"records");
		List<String> newlist = new ArrayList<String>();
		List<Integer> col1 = definecols(cols1);
		List<Integer> col2 = definecols(cols2);
		
		HashMap<String, String> map2 = new HashMap<String, String>();
		for (String t2:list2){	
			String[] temp2 = t2.split("\t");
			String temp22 = "";
			for (int ii=0;ii<temp2.length;ii++){
				if  (col2.contains(ii))
					temp22 = temp22 + SPACE_STR + ContentNormalizer.normtext(temp2[ii]);
			}
			temp22 = temp22.trim();
			if (!map2.containsKey(temp22))
				map2.put(temp22, t2);
		}
		int counter=0;
		for (String t1:list1){
			String[] temp1 = t1.split("\t");
			String temp11 = "";
			for (int ii=0;ii<temp1.length;ii++){
				if  (col1.contains(ii))
					temp11 = temp11 + SPACE_STR + ContentNormalizer.normtext(temp1[ii]);
			}
			temp11 = temp11.trim();
			if (!map2.containsKey(temp11))
				newlist.add( t1);
			else{
				LOGGER.info(temp11);
				LOGGER.info(t1);
				LOGGER.info(map2.get(temp11));
				counter++;
			}
		}
		LOGGER.info("dups found: " +counter);
		LOGGER.info("Ending to "+ newlist.size()+"records");
		return newlist;
	}

	private void setProcess(String process) {
		DataSetsCleaner.process = process;
	}
	private void setCols1(String cols1) {
		DataSetsCleaner.cols1 = cols1;
	}
	private void setCols2(String cols2) {
		DataSetsCleaner.cols2 = cols2;
	}
	public void setInFile1(File infile1 ){
		DataSetsCleaner.infile1 = infile1;
	}
	public void setInFile2(File infile2 ){
		DataSetsCleaner.infile2 = infile2;
	}
	public void setOutFile1(File outfile1 ){
		DataSetsCleaner.outfile1 = outfile1;
	}

	/**
	 * Concatenates content of each record of a list in columns 'cols' and discards duplicates     
	 * @param set
	 * @param cols
	 * @return
	 */

	private static List<String> removeIdenticalPartsPerDataSet(List<String> list, String cols) {
		LOGGER.info("Starting from "+ list.size()+" records");
		List<Integer> col = definecols(cols);
		List<String> newlist = new ArrayList<String>();
		HashMap<String, String> parts = new HashMap<String, String>();
		int counter=0;
		for (String t:list){
			String[] temp1 = t.split(TAB_STR);
			String temp11 = "";
			for (int ii=0;ii<temp1.length;ii++){
				if  (col.contains(ii))
					temp11 = temp11 + SPACE_STR + ContentNormalizer.normtext(temp1[ii]);
			}
			temp11 = temp11.trim();

			if (!parts.containsKey(temp11)){
				parts.put(temp11, t);
				newlist.add(t);
			}else{
				LOGGER.info(t);
				LOGGER.info(parts.get(temp11));
				counter++;
			}
		}
		LOGGER.info(counter);
		LOGGER.info("Ending to "+ newlist.size()+" records");
		return newlist;
	}


	/**
	 * gets columns ids from a string in which columns ids separated by ;  
	 * @param cols3
	 * @return
	 */
	
	private static List<Integer> definecols(String cols3) {
		String[] t=cols3.split(SEMICOLON_STR);
		List<Integer> col = new ArrayList<Integer>();
		for (int ii=0;ii<t.length;ii++){
			col.add(Integer.parseInt(t[ii]));
		}
		return col;
	}


}
