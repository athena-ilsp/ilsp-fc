package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.utils.FCStringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.loomchild.maligna.parser.TmxParseException;
import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Seg;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;
import net.loomchild.maligna.util.bind.tmx.Tuv;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TMXHandlerUtils {
	private static final Logger LOGGER = Logger.getLogger(TMXHandlerUtils.class);

	private static final String SPACE_SEPARATOR = " ";
	private static final String SEMI_SEPAR = ";";
	private final static String PUNCT = ".";
	private static final String UNDERSCORE = "_";
	private static final String XML_EXTENSION = ".xml";
	private final static String eAddressNode ="eAddress"; 
	private final static String licenseNode = "license";
	private final static String TARGET_STR = "target";
	private final static String SCORE = "score";
	private final static String SEGMENTTYPE = "segmentType";
	private static boolean doNotCountZeroToOneAlignments = true;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	private static final double percent_thr=0.15;
	//private static final int length_THR = 6;

	/**
	 * gets the tus of a tmx if the 0:1 alignments are less than thr
	 * @param tmxFile
	 * @param thr
	 */
	public static List<SegPair>  getTUsFromTMX(File tmxFile, int thr, String lang1, String lang2, boolean lic) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxFile));
			int alignments = 0;
			int sourceSegments = 0;
			int targetSegments = 0;
			int zeroToOneAlignments = 0;
			//double meanAlignmentScore = 0.0;
			TmxInfo tmxinfo = TMXHandlerUtils.getInfo(tmxFile);
			if (lic && tmxinfo.license.isEmpty())
				return segpairs;
			List<Tu> tus = tmx.getBody().getTu();
			for (Tu tu: tus) {
				alignments = alignments+1;
				List<Object> tuProps = tu.getNoteOrProp();
				String type="";
				double score = 0;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(SCORE)) {
						//meanAlignmentScore = meanAlignmentScore + Double.parseDouble(prop.getContent().get(0));
						score = Double.parseDouble(prop.getContent().get(0));
					} else if (prop.getType().equals(SEGMENTTYPE)) {
						type = prop.getContent().get(0);
						String[] segs = StringUtils.split(prop.getContent().get(0), ":");
						sourceSegments = sourceSegments + Integer.parseInt(segs[0]);
						targetSegments = targetSegments + Integer.parseInt(segs[1]);
						if (doNotCountZeroToOneAlignments && (Integer.parseInt(segs[0])==0 ||Integer.parseInt(segs[1])==0 )) {
							alignments--;
							zeroToOneAlignments++;
						}
					}
				}
				segpairs.add(new SegPair(StringUtils.join(createSegmentList(tu, lang1), SPACE_SEPARATOR), 
						StringUtils.join(createSegmentList(tu, lang2), SPACE_SEPARATOR),
						score, type, tmxinfo.method, tmxinfo.site, tmxinfo.license, tmxinfo.other));
			}
			LOGGER.debug("Examining " + tmxFile.getAbsolutePath() + SPACE_SEPARATOR + tus.size());
			double percent = (double)zeroToOneAlignments / (double)tus.size();
			if (zeroToOneAlignments>thr || percent>percent_thr){
				return null;
			}
		} catch (FileNotFoundException e) {
			LOGGER.warn("Problem in reading "+ tmxFile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}


	private static TmxInfo getInfo(File tmxFile) {
		String method = tmxFile.getName().substring(tmxFile.getName().lastIndexOf(UNDERSCORE)+1, tmxFile.getName().lastIndexOf(PUNCT));
		File f1 = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), UNDERSCORE)[0])+XML_EXTENSION);
		File f2 = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), UNDERSCORE)[1])+XML_EXTENSION);
		String site="", license="";
		URL url = null;
		String webpage1=ReadResources.extractNodefromXML(f1.getAbsolutePath(), eAddressNode, false);
		String webpage2=ReadResources.extractNodefromXML(f2.getAbsolutePath(), eAddressNode, false);
		try {
			url = new URL(webpage1);
			webpage1 = url.getProtocol()+"://"+ url.getHost();
		} catch (MalformedURLException e) {
			webpage1 = webpage1.substring(0, webpage1.indexOf("/"));
			LOGGER.warn("no protocol "+ webpage1);
		}
		try {
			url = new URL(webpage2);
			webpage2 = url.getProtocol()+"://"+ url.getHost();
		} catch (MalformedURLException e) {
			webpage2 = webpage2.substring(0, webpage2.indexOf("/"));
			LOGGER.warn("no protocol "+ webpage2);
		}
		
		if (!webpage1.equals(webpage2)){
			if (webpage1.contains(webpage2))
				site = webpage1;
			else if (webpage2.contains(webpage1))
				site = webpage2;
			else
				site = webpage1+SEMI_SEPAR+webpage2;
		}else
			site = webpage1;

		String license1 = ReadResources.extractAttrfromXML(f1.getAbsolutePath(), licenseNode, TARGET_STR,true, true);
		String license2 = ReadResources.extractAttrfromXML(f2.getAbsolutePath(), licenseNode, TARGET_STR,true, true);
		if (!license1.isEmpty())
			license = license1;
		else{
			if (!license2.isEmpty())
				license = license2;
			else
				license="";
		}

		return new TmxInfo(method, site, license, "");
	}

	public static List<String> createSegmentList(Tu tu, String languageCode) {
		List<String> segmentList = new ArrayList<String>();
		for (Tuv tuv : tu.getTuv()) {
			if (languageCode.equals(tuv.getLang())) {
				String segment = getSegment(tuv.getSeg());
				segmentList.add(segment);
			}
		}
		if (segmentList.size() > 1) {
			throw new TmxParseException(languageCode + " variant count greater than 1");
		}
		return segmentList;
	}


	private static String getSegment(Seg seg) {
		StringBuilder builder = new StringBuilder();
		for (Object object : seg.getContent()) {
			builder.append(object.toString());
		}
		return builder.toString();
	}


	/**
	 * holds seg in lang1, seg in lang2, score, type of aligmnement, method of pair detection 
	 * @author 
	 *
	 */
	public static class SegPair {
		public String seg1;
		public double score;
		public String seg2;
		public String type;
		public String method;
		public String site;
		public String license;
		public String other;

		public SegPair(String seg1, String seg2, double score, String type, String method, String site, String license, String other) {
			this.seg1 = seg1;
			this.seg2 = seg2;
			this.type = type;
			this.score = score;
			this.method = method;
			this.site = site;
			this.license = license;
			this.other = other;
		}
	}

	/**
	 * Selects files that end with one of elements of types 
	 * @param tfs
	 * @param types
	 * @return
	 */
	public static  List<File> selectTypes(List<File> tfs, String[] types) {
		List<File> tmxfiles = new ArrayList<File>();
		for (File tf:tfs){
			for (int ii=0;ii<types.length;ii++){
				if (tf.getName().endsWith(types[ii])){
					tmxfiles.add(tf);
					break;
				}
			}
		}
		return tmxfiles;
	}

	/**
	 * holds site, license, and method of pair detection 
	 * @author 
	 *
	 */
	public static class TmxInfo {
		public String method;
		public String site;
		public String license;
		public String other;

		public TmxInfo(String method, String site, String license, String other) {
			this.method = method;
			this.site = site;
			this.license = license;
			this.other = other;
		}
	}
	/**
	 * counts words of i-th TUV of each TU in  the alignmentList
	 * @param alignmentList
	 * @param i
	 * @return
	 */
	public static int[] countWordsInTMX(List<ILSPAlignment> alignmentList, int i) {
		int len=0;
		Set<String> words = new HashSet<String>();
		for (ILSPAlignment align:alignmentList){
			List<String> temp=new ArrayList<String>();
			if (i==1)
				temp =align.getSourceSegmentList();
			if (i==2)
				temp =align.getTargetSegmentList();
			for (String str:temp){
				List<String> toks = FCStringUtils.getWords(str); 
				len =len +toks.size();
				for (String tok:toks){
					if (!words.contains(tok)){
						words.add(tok);
					}
				}
			}
		}
		int[] res=new int[2]; res[0]=len; res[1] = words.size();
		return res;
	}
}
