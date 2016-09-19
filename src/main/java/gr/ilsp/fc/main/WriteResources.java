package gr.ilsp.fc.main;

import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class WriteResources {
	private static final String appHTMLext = ".html";
	
	/**
	 * Generates Text file with list of paths of cesDocFiles 
	 */
	public static void WriteTextList(List<File> remFiles, File outputFile) {
		/*OutputStreamWriter xmlFileListWrt;
		try {
			xmlFileListWrt = new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8");
			for (File xmlFile: remFiles) {
				xmlFileListWrt.write(xmlFile.getAbsolutePath().replace("\\","/")+"\n");
			}
			xmlFileListWrt.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		List<String> lines = new ArrayList<String>();
		for (File xmlFile: remFiles) {
			lines.add(xmlFile.getAbsolutePath().replace("\\","/"));
		}
		try {
			FileUtils.writeLines(outputFile, lines);
		} catch (IOException e) {
			System.err.println("problem in writing file "+ outputFile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	/**
	 * Generates HTML file with list of links pointing to the cesDocFiles or the transformed cesDocFiles
	 */
	public static void WriteHTMLList(List<File> htmlTmxFiles, File outputFileHTML) {
		/*if (htmlTmxFiles.size()>0){
			OutputStreamWriter xmlFileListWrt1;
			try {
				xmlFileListWrt1 = new OutputStreamWriter(new FileOutputStream(outputFileHTML),"UTF-8");
				xmlFileListWrt1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
				String ttt;
				for (File file: htmlTmxFiles) {
					if (!file.getAbsolutePath().endsWith(appHTMLext)){
						file = new File(file.getAbsolutePath()+appHTMLext);
					}
					ttt= "<a href=\""+file.getAbsolutePath()+"\">\n"+file.getName()+"</a>";
					xmlFileListWrt1.write("<br />"+ttt.replace("\\","/")+"\n");
				}
				xmlFileListWrt1.write("</html>");
				xmlFileListWrt1.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		if (htmlTmxFiles.size()>0){
			List<String> lines = new ArrayList<String>();
			lines.add("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			String ttt;
			for (File file: htmlTmxFiles) {
				if (!file.getAbsolutePath().endsWith(appHTMLext)){
					file = new File(file.getAbsolutePath()+appHTMLext);
				}
				ttt= "<a href=\""+file.getAbsolutePath()+"\">\n"+file.getName()+"</a>";
				lines.add("<br />"+ttt.replace("\\","/"));
			}
			lines.add("</html>");
			try {
				FileUtils.writeLines(outputFileHTML, lines);
			} catch (IOException e) {
				System.err.println("problem in writing file "+ outputFileHTML.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
}
