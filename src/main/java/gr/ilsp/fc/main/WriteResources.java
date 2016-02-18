package gr.ilsp.fc.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

public class WriteResources {
	private static final String appHTMLext = ".html";
	
	/**
	 * Writes a String in a text file
	 * @param filename
	 * @param text
	 */
	public static void writetextfile(String filename,String text) {
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
			out.write(text.trim());
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file. The encoding is not supported.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file. The file does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file.");
		}
	}

	/**
	 * Generates Text file with list of paths of cesDocFiles 
	 */
	public static void WriteTextList(List<File> remFiles, File outputFile) {
		OutputStreamWriter xmlFileListWrt;
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
		}
	}

	/**
	 * Generates HTML file with list of links pointing to the cesDocFiles or the transformed cesDocFiles
	 */
	public static void WriteHTMLList(List<File> htmlTmxFiles, File outputFileHTML, boolean applyOfflineXSLT) {
		if (htmlTmxFiles.size()>0){
			OutputStreamWriter xmlFileListWrt1;
			try {
				xmlFileListWrt1 = new OutputStreamWriter(new FileOutputStream(outputFileHTML),"UTF-8");
				xmlFileListWrt1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
				String ttt, fileURL;
				for (File file: htmlTmxFiles) {
					if (!file.getAbsolutePath().endsWith(appHTMLext)){
						file = new File(file.getAbsolutePath()+appHTMLext);
					}
					fileURL = file.getAbsolutePath().replace("\\","/");
					if (applyOfflineXSLT){
						ttt= "<a href=\""+fileURL+"\">\n"+file.getName()+"</a>";
					}else{
						ttt= "<a href=\""+file.getAbsolutePath()+"\">\n"+file.getName()+"</a>";
					}
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
		}
	}
}
