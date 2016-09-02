package gr.ilsp.fc.utils;

import gr.ilsp.fc.aligner.factory.BilingualScoredTmxParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import java.util.ArrayList;
import java.util.List;

import net.loomchild.maligna.coretypes.Alignment;

import org.apache.commons.io.FilenameUtils;

public class CleanTMX {
	private static final String UTF_8 = "UTF-8";
	private static final String textext=".txt";


	public static void tmx2txts(String tmxfile, String l1, String l2){
		File tmxFile = new File(tmxfile);
		File l1File = new File(FilenameUtils.concat(tmxFile.getParent(),l1+textext));
		File l2File = new File(FilenameUtils.concat(tmxFile.getParent(),l2+textext));
		Reader reader;
		Writer out1, out2;
		try {
			reader = new InputStreamReader(new FileInputStream(tmxFile.getAbsolutePath()), UTF_8);

			out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(l1File.getAbsolutePath()),"UTF-8"));
			out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(l2File.getAbsolutePath()),"UTF-8"));

			BilingualScoredTmxParser parser = new BilingualScoredTmxParser(reader, l1,l2);
			List<Alignment> listalign= parser.parse();
			System.out.println("Alignments : " + listalign.size());
			List<String> sourcesegs = new ArrayList<String>();
			List<String> targetsegs = new ArrayList<String>();
			int coun=0, ind=-1;
			for (Alignment alignment:listalign){
				ind=ind+1;
				if ((ind/1000)>coun){
					System.out.println(coun);
					coun++;
				}
				sourcesegs = alignment.getSourceSegmentList();
				if (sourcesegs.size()>0){
					out1.write(sourcesegs.get(0));
				}
				out1.write("\n");
				targetsegs = alignment.getTargetSegmentList();
				if (targetsegs.size()>0){
					//l2text=l2text+targetsegs.get(0);
					out2.write(targetsegs.get(0));
				}
				out2.write("\n");
			}
			out1.close();
			out2.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



//
//	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException{
//
//		String tmxfile ="C:\\Users\\vpapa\\ELRC\\gv-20150731\\TMX\\ell-eng.tmx";
//		String l1="ell";
//		String l2="eng";
//		tmx2txts(tmxfile,l1,l2);
//
//
//		//File infile = new File(args[0]);
//		LOGGER.info("It is supposed that the TMX file is named like l1-l2.tmx");
//		File infile = new File("C:/Users/vpapa/ELRC/gv-20150731/TMX/eng-spa.tmx"); 
//		//File outfile = new File(infile.getParent()+fs+FcFileUtils.removeExtension(infile.getName())+".txt"); 
//
//		//LangDetectUtils.loadCybozuLangIdentifier();
//
//		String[] langs = infile.getName().split("\\.")[0].split("-");
//
//		//String targetlang = "";
//		//String sourcelang = "";
//		//if (langs[0].length()>2){ sourcelang = langs[0].substring(0,2);}
//		//if (langs[1].length()>2){ targetlang = langs[1].substring(0,2);}
//		//if (langs[1].equals("spa")){targetlang="es";} 		 
//
//		Reader reader = new InputStreamReader(new FileInputStream(infile.getAbsolutePath()), UTF_8);
//		BilingualScoredTmxParser parser = new BilingualScoredTmxParser(reader, langs[0],langs[1]);
//		List<Alignment> listalign= parser.parse();
//		LOGGER.info("Num of TUs:"+listalign.size() + "in "+ infile.getName());
//		int ind=-1,counter_red=0, counter_intrasame=0, counter_intersame=0, counter_lang=0; 
//		//ArrayList<Integer> indeces = new ArrayList<Integer>(); 
//		HashMap<String, String> source = new HashMap<String, String>();
//		//HashMap<String, String> target = new HashMap<String, String>();
//		//HashMap<String, String> pairs = new HashMap<String, String>();
//		//ArrayList<String[]> segpair = new ArrayList<String[]>(); 
//		//boolean founds=false, foundt=false;
//		//String idlang ="", idlang1="";
//		//Writer out;
//		//try {
//		//out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile.getAbsolutePath()),"UTF-8"));
//		int coun=0;
//		for (Alignment alignment:listalign){
//			ind=ind+1;
//			if ((ind/5000)>coun){
//				System.out.println(coun);
//				coun++;
//			}
//			//System.out.println(ind);//founds=false;
//			//foundt=false;
//			List<String> sourcesegs = alignment.getSourceSegmentList();
//			List<String> targetsegs = alignment.getTargetSegmentList();
//
//			if (sourcesegs.size()!=targetsegs.size()){
//				LOGGER.info("Different number of segs in a TU");
//				//indeces.add(ind);
//				continue;
//			}
//			if (sourcesegs.size()>1){
//				LOGGER.info("More than one segs in a TUV");
//				//indeces.add(ind);
//				continue;	
//			}
//			String normS = ContentNormalizer.normtext(sourcesegs.get(0));
//			String normT = ContentNormalizer.normtext(targetsegs.get(0));
//			if ( normS.isEmpty() || normT.isEmpty()){
//				LOGGER.info("obviously not useful pair");LOGGER.info(sourcesegs.get(0));	LOGGER.info(targetsegs.get(0));	LOGGER.info("-------------");
//				counter_red++;
//				//indeces.add(ind);
//				continue;
//			}
//			if (normS.equals(normT)){
//				LOGGER.info("Same segs in a TU");		LOGGER.info(sourcesegs.get(0));		LOGGER.info(targetsegs.get(0));	LOGGER.info("-------------");
//				counter_intrasame++;
//				//indeces.add(ind);
//				continue;
//			}
//			if (source.containsKey(normS)){
//				if (source.get(normS).equals(normT)){
//					LOGGER.info("same TUs");	LOGGER.info(sourcesegs.get(0));		LOGGER.info(targetsegs.get(0));
//					//indeces.add(ind);
//					counter_intersame++;
//					continue;
//					//founds=true;
//				}//else{
//				//	LOGGER.info("same source, different target");	LOGGER.info(sourcesegs.get(0));		LOGGER.info(targetsegs.get(0));
//				//}
//				//founds=true;
//			}
//			source.put(normS, normT);
//			//else{
//			//	source.put(normS, normT);
//			//}
//			//if (target.containsKey(normT)){
//			//	if (target.get(normT).equals(normS)){
//			//LOGGER.info("same TUs");	LOGGER.info(sourcesegs.get(0));		LOGGER.info(targetsegs.get(0));
//			//		foundt=true;
//			//}//else{
//			//	LOGGER.info("same target, different source");	LOGGER.info(sourcesegs.get(0));		LOGGER.info(targetsegs.get(0));	
//			//}
//			//foundt=true;
//			//}else{
//			//	target.put(normT, normS);
//			//}
//			//if (!founds && !foundt){
//			//pairs.put(sourcesegs.get(0), targetsegs.get(0));
//			//String t[] = new String[2];	t[0]=sourcesegs.get(0); t[1]=targetsegs.get(0);
//			//segpair.add(t);
//
//			//if (StringUtils.split(sourcesegs.get(0)).length>8){
//
//			/*	idlang = LangDetectUtils.detectLanguage(sourcesegs.get(0));
//						if (!idlang.equals(sourcelang) ){
//							LanguageIdentifier LangI=new LanguageIdentifier(sourcesegs.get(0)); 
//							idlang1 = LangI.getLanguage();
//							if (!idlang1.equals(sourcelang) ){
//								counter_lang++;
//								continue;
//							}
//						}*/
//			//}
//			//if (StringUtils.split(targetsegs.get(0)).length>8){
//
//			/*idlang = LangDetectUtils.detectLanguage(targetsegs.get(0));
//						if (!idlang.equals(targetlang) ){
//							LanguageIdentifier LangI=new LanguageIdentifier(targetsegs.get(0)); 
//							idlang1 = LangI.getLanguage();
//							if (!idlang1.equals(targetlang) ){
//								counter_lang++;
//								continue;
//							}
//						}*/
//			//}
//			//out.write(sourcesegs.get(0)+"\t"+ targetsegs.get(0)+"\n");
//			//}
//		}
//		System.out.println("counter_red: " + counter_red);
//		System.out.println("counter_intrasame: " + counter_intrasame);
//		System.out.println("counter_intersame: " + counter_intersame);
//		System.out.println("counter_lang: " + counter_lang);
//		//LOGGER.info("seg pairs:" + pairs.size());
//		//LOGGER.info("seg pairs:" + segpair.size());
//		System.out.println();
//
//		//out.close();
//		//} catch (UnsupportedEncodingException e) {
//		//	e.printStackTrace();
//		//	System.err.println("Error in writing the output text file. The encoding is not supported.");
//		//} catch (FileNotFoundException e) {
//		//	e.printStackTrace();
//		//	System.err.println("Error in writing the output text file. The file does not exist.");
//		//} catch (IOException e) {
//		//	e.printStackTrace();
//		//	System.err.println("Error in writing the output text file.");
//		//}
//	}
}
