package gr.ilsp.fc.aligner.factory;

import java.util.List;

import net.loomchild.maligna.coretypes.Alignment;

public class ILSPAlignment extends Alignment {

	public static final float DEFAULT_SCORE = 0.0f;
	public static final String EMPTY_STR = "";
	public static final String UNDER_REVIEW_STR = "Under review";
	//private List<String> sourceSegmentList;
	//private List<String> targetSegmentList;
	//private float score;
	private String site;
	private String license;
	private String method;
	private String lengthRatio;
	private String type;
	private String info;
	
	public ILSPAlignment() {
		//this.sourceSegmentList = new ArrayList<String>();
		//this.targetSegmentList = new ArrayList<String>();
		//this.score = DEFAULT_SCORE;
		this.site = EMPTY_STR;
		this.license = UNDER_REVIEW_STR;
		this.method = EMPTY_STR;
		this.lengthRatio = EMPTY_STR;
		this.type = EMPTY_STR;
		this.info = EMPTY_STR;
	}

	public ILSPAlignment(List<String> sourceSegmentList, List<String> targetSegmentList) {
		super(sourceSegmentList, targetSegmentList);
		// TODO Auto-generated constructor stub
		this.site = EMPTY_STR;
		this.license = UNDER_REVIEW_STR;
		this.method = EMPTY_STR;
		this.lengthRatio = EMPTY_STR;
		this.type = EMPTY_STR;
		this.info = EMPTY_STR;
	}

	public ILSPAlignment(String[] sourceSegmentArray, String[] targetSegmentArray) {
		super(sourceSegmentArray, targetSegmentArray);
		// TODO Auto-generated constructor stub
		this.site = EMPTY_STR;
		this.license = UNDER_REVIEW_STR;
		this.method = EMPTY_STR;
		this.lengthRatio = EMPTY_STR;
		this.type = EMPTY_STR;
		this.info = EMPTY_STR;
	}

	public ILSPAlignment(List<String> sourceSegmentList, List<String> targetSegmentList, float score) {
		super(sourceSegmentList, targetSegmentList, score);
		this.site = EMPTY_STR;
		this.license = UNDER_REVIEW_STR;
		this.method = EMPTY_STR;
		this.lengthRatio = EMPTY_STR;
		this.type = EMPTY_STR;
		this.info = EMPTY_STR;
	}

	public ILSPAlignment(String[] sourceSegmentArray, String[] targetSegmentArray, float score) {
		super(sourceSegmentArray, targetSegmentArray, score);
		this.site = EMPTY_STR;
		this.license = UNDER_REVIEW_STR;
		this.method = EMPTY_STR;
		this.lengthRatio = EMPTY_STR;
		this.type = EMPTY_STR;
		this.info = EMPTY_STR;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getLengthRatio() {
		return lengthRatio;
	}

	public void setLengthRatio(String lengthRatio) {
		this.lengthRatio = lengthRatio;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
