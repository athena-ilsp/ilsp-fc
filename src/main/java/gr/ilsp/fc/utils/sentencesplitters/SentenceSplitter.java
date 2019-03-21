package gr.ilsp.fc.utils.sentencesplitters;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public abstract class SentenceSplitter {
	
	URL abbreviationsURL;

	String lang ;

	/**
     * All newlines will be used to split the text into paragraphs
     */
    public static final int ALL_NEWLINES = 1;
    
    /**
     * Only blocks of two or more consecutive newlines will be used to split the text into paragraphs.
     */
    public static final int MULTIPLE_NEWLINES = 2;
    
    /**
     * The text will not be split into paragraphs before splitting into sentences. 
     */
    public static final int IGNORE_NEWLINES = 3 ;

    protected Boolean splitOnColon = true ;
	
	public abstract List<String> getSentences(String text, int paragraphMode) throws IOException;


	/**
	 * @param abbreviationsURL the abbreviationsURL to set
	 */
	public void setAbbreviationsURL(URL abbreviationsURL) {
		this.abbreviationsURL = abbreviationsURL;
	}

	/**
	 * @return the splitOnColon
	 */
	protected  Boolean getSplitOnColon() {
		return splitOnColon;
	}


	/**
	 * @param splitOnColon the splitOnColon to set
	 */
	protected void setSplitOnColon(Boolean splitOnColon) {
		this.splitOnColon = splitOnColon;
	}


	
	

}
