package gr.ilsp.fmc.extractors;

import java.io.IOException;
import java.util.ArrayList;


import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class PrintTextLocations extends PDFTextStripper
{
	private ArrayList<CharAttr> chardata=new ArrayList<CharAttr>();
	
	public ArrayList<CharAttr> getchardata() {
		return chardata;
	}

	public void setchardata(ArrayList<CharAttr> chardata) {
		this.chardata = chardata;
	}

	public PrintTextLocations() throws IOException {
        super.setSortByPosition(true);
    }
	
	@Override /* is this needed... */
    //protected void processTextPosition(TextPosition text) {
	protected void processTextPosition(TextPosition text) {
        /*System.out.println("String[" + text.getXDirAdj() + ","
                + text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale="
                + text.getXScale() + " height=" + text.getHeightDir() + " space="
                + text.getWidthOfSpace() + " width="
                + text.getWidthDirAdj() + "]" + text.getCharacter());*/
       //System.out.println(chardata.size());
        
        CharAttr t= new CharAttr(text.getCharacter(),text.getXDirAdj(),
        		text.getYDirAdj(), text.getFontSize(), text.getXScale(),
        		text.getHeightDir(), text.getWidthOfSpace(),
        		text.getWidthDirAdj(), 0);
       
        //System.out.println(x);
        //System.out.println(y);
        chardata.add(t);
        //System.out.println(chardata.size());
        //System.out.println("----");
    }	
    
	public static class CharAttr {
		public String character;
		public float x;
		public float y;
		public float fs;
		public float xs;
		public float h;
		public float s;
		public float w;
		public int p;
		

		public CharAttr(String character, float x, 
				float y, float fs, float xs, float h, float s, float w, int p) {
			this.character = character;
			this.x = x;
			this.y = y;
			this.fs = fs;
			this.xs = xs;
			this.h = h;
			this.s = s;
			this.w = w;
			this.p=p;
		}
	}
	
	
}
