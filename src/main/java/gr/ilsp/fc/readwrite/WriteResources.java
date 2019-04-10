package gr.ilsp.fc.readwrite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class WriteResources {
	private static final String appHTMLext = ".html";
	//private static final String VAR_RES_CACHE = "/var/www/html/20171107-ILSP-FC_demo/";
	/**
	 * Generates Text file with list of paths of cesDocFiles 
	 */
	public static void WriteTextList(List<File> remFiles, File outputFile) {
		List<String> lines = new ArrayList<String>();
		for (File xmlFile: remFiles) {
			lines.add(xmlFile.getAbsolutePath().replace("\\","/"));
		}
		try {
			FileUtils.writeLines(outputFile, lines,"\n");
		} catch (IOException e) {
			System.err.println("problem in writing file "+ outputFile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	/**
	 * Generates HTML file with list of links pointing to the cesDocFiles or the transformed cesDocFiles
	 */
	public static void WriteHTMLList(List<File> htmlTmxFiles, File outputFileHTML) {
		if (htmlTmxFiles.size()>0){
			List<String> lines = new ArrayList<String>();
			lines.add("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			String ttt;
			for (File file: htmlTmxFiles) {
				if (!file.getAbsolutePath().endsWith(appHTMLext)){
					file = new File(file.getAbsolutePath()+appHTMLext);
				}
				//String prot = file.getAbsolutePath().replaceAll(VAR_RES_CACHE, "");
				//ttt= "<a href=\""+prot+"\">\n"+file.getName()+"</a>";
				ttt= "<a href=\""+file.getAbsolutePath()+"\">\n"+file.getName()+"</a>";
				lines.add("<br />"+ttt.replace("\\","/"));
			}
			lines.add("</html>");
			try {
				FileUtils.writeLines(outputFileHTML, lines,"\n");
			} catch (IOException e) {
				System.err.println("problem in writing file "+ outputFileHTML.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
}
