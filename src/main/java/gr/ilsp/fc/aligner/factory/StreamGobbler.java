package gr.ilsp.fc.aligner.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * taken from 
 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
 *
 */
public class StreamGobbler extends Thread{
    InputStream is;
    String type;
    OutputStream os;
    
    public StreamGobbler(InputStream is, String type){
        this(is, type, null);
    }
    public StreamGobbler(InputStream is, String type, OutputStream redirect){
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
    
    public void run(){
        try{
            PrintWriter pw = null;
            if(os != null)
                pw = new PrintWriter(os);
                
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null){
                if (pw != null)
                    pw.println(line);
            }
            if (pw != null)
                pw.flush();
        }catch (IOException ioe){
            ioe.printStackTrace();  
        }
    }
}