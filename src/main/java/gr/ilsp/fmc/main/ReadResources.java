package gr.ilsp.fmc.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
}
