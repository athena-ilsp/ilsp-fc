package gr.ilsp.fmc.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;

public class ReadResources {
	
    public static ArrayList<Double> readFile(String filename) throws IOException {
    	ArrayList<Double> param=new ArrayList<Double>();
        URL svURL = ReadResources.class.getClassLoader().getResource(filename);
        BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
              System.out.println(inputLine);
        }
        in.close();
        return param;
    }
    
    
    public static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
		f.read(buffer);
		f.close();
		return new String(buffer);
	}
    
    
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
    
    
}
