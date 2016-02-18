package gr.ilsp.fc.aligner.factory;

import java.io.File;

public class AlignmentStats {
	private int alignmentsSize;
	private int zeroToOneAlignmentsSize;
	private int twitterAlignmentsSize;
	private int sourceSentencesSize;
	private int targetSentencesSize;
	private File l1File;
	private File l2File;
	private String l1;
	private String l2;
	private File tmxFile;
	private File tmxHtmlFile;
	private Double meanScore = 0.0;
	
	public AlignmentStats(int alignmentsSize, int sourceSegmentsSize,
			int targetSegmentsSize) {
		super();
		this.alignmentsSize = alignmentsSize;
		this.sourceSentencesSize = sourceSegmentsSize;
		this.targetSentencesSize = targetSegmentsSize;
	}
	
	public int getAlignmentsSize() {
		return alignmentsSize;
	}
	public void setAlignmentsSize(int alignments) {
		this.alignmentsSize = alignments;
	}
	public int getSourceSentencesSize() {
		return sourceSentencesSize;
	}
	public void setSourceSentencesSize(int sourceSegments) {
		this.sourceSentencesSize = sourceSegments;
	}
	public int getTargetSentencesSize() {
		return targetSentencesSize;
	}
	public void setTargetSentencesSize(int targetSegments) {
		this.targetSentencesSize = targetSegments;
	}

	/**
	 * @return the l1File
	 */
	public File getL1File() {
		return l1File;
	}

	/**
	 * @param l1File the l1File to set
	 */
	public void setL1File(File l1File) {
		this.l1File = l1File;
	}

	/**
	 * @return the l2File
	 */
	public File getL2File() {
		return l2File;
	}

	/**
	 * @param l2File the l2File to set
	 */
	public void setL2File(File l2File) {
		this.l2File = l2File;
	}

	/**
	 * @return the l1
	 */
	public String getL1() {
		return l1;
	}

	/**
	 * @param l1 the l1 to set
	 */
	public void setL1(String l1) {
		this.l1 = l1;
	}

	/**
	 * @return the l2
	 */
	public String getL2() {
		return l2;
	}

	/**
	 * @param l2 the l2 to set
	 */
	public void setL2(String l2) {
		this.l2 = l2;
	}

	/**
	 * @return the tmxFile
	 */
	public File getTmxFile() {
		return tmxFile;
	}

	/**
	 * @param tmxFile the tmxFile to set
	 */
	public void setTmxFile(File tmxFile) {
		this.tmxFile = tmxFile;
	}

	/**
	 * @return the tmxHtmlFile
	 */
	public File getTmxHtmlFile() {
		return tmxHtmlFile;
	}

	/**
	 * @param tmxHtmlFile the tmxHtmlFile to set
	 */
	public void setTmxHtmlFile(File tmxHtmlFile) {
		this.tmxHtmlFile = tmxHtmlFile;
	}

	/**
	 * @return the meanScore
	 */
	public Double getMeanScore() {
		return meanScore;
	}

	/**
	 * @param meanScore the meanScore to set
	 */
	public void setMeanScore(Double meanScore) {
		this.meanScore = meanScore;
	}

	/**
	 * @return the zeroToOneAlignmentsSize
	 */
	public int getZeroToOneAlignmentsSize() {
		return zeroToOneAlignmentsSize;
	}

	/**
	 * @param zeroToOneAlignmentsSize the zeroToOneAlignmentsSize to set
	 */
	public void setZeroToOneAlignmentsSize(int zeroToOneAlignmentsSize) {
		this.zeroToOneAlignmentsSize = zeroToOneAlignmentsSize;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AlignmentStats [alignmentsSize="
				+ alignmentsSize
				+ ", zeroToOneAlignmentsSize="
				+ zeroToOneAlignmentsSize
				+ ", sourceSentencesSize="
				+ sourceSentencesSize
				+ ", targetSentencesSize="
				+ targetSentencesSize
				+ ", "
				+ (l1File != null ? "l1File=" + l1File + ", " : "")
				+ (l2File != null ? "l2File=" + l2File + ", " : "")
				+ (l1 != null ? "l1=" + l1 + ", " : "")
				+ (l2 != null ? "l2=" + l2 + ", " : "")
				+ (tmxFile != null ? "tmxFile=" + tmxFile + ", " : "")
				+ (tmxHtmlFile != null ? "tmxHtmlFile=" + tmxHtmlFile + ", "
						: "")
				+ (meanScore != null ? "meanScore=" + meanScore : "") + "]";
	}

	/**
	 * @return the twitterAlignmentsSize
	 */
	public int getTwitterAlignmentsSize() {
		return twitterAlignmentsSize;
	}

	/**
	 * @param twitterAlignmentsSize the twitterAlignmentsSize to set
	 */
	public void setTwitterAlignmentsSize(int twitterAlignmentsSize) {
		this.twitterAlignmentsSize = twitterAlignmentsSize;
	}
	
	
	
}
