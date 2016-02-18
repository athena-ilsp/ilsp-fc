package gr.ilsp.fc.corpora;


public class MonolingualCorpusInformation {
	private int tokensSize;
	private int documentsSize;
	private int paragraphsSize;
	private String lang;
	
	
	/**
	 * @return the tokensSize
	 */
	public int getTokensSize() {
		return tokensSize;
	}
	/**
	 * @param tokensSize the tokensSize to set
	 */
	public void setTokensSize(int tokensSize) {
		this.tokensSize = tokensSize;
	}
	/**
	 * @return the documentsSize
	 */
	public int getDocumentsSize() {
		return documentsSize;
	}
	/**
	 * @param documentsSize the documentsSize to set
	 */
	public void setDocumentsSize(int documentsSize) {
		this.documentsSize = documentsSize;
	}
	/**
	 * @return the paragraphsSize
	 */
	public int getParagraphsSize() {
		return paragraphsSize;
	}
	/**
	 * @param paragraphsSize the paragraphsSize to set
	 */
	public void setParagraphsSize(int paragraphsSize) {
		this.paragraphsSize = paragraphsSize;
	}
	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}
	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MonolingualCorpusInformation [tokensSize=" + tokensSize
				+ ", documentsSize=" + documentsSize + ", paragraphsSize="
				+ paragraphsSize + ", " + (lang != null ? "lang=" + lang : "")
				+ "]";
	}


}
