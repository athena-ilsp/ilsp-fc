package gr.ilsp.fc.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;


public class InvalidTMXFilter extends AbstractXOMXMLScanner {

	public class Cand {
		private List<String> ngramLemmas = new ArrayList<String>();
		private List<String> ngramPos = new ArrayList<String>();
		private int ngramFreq = 0;
		private int candId = 0;
		private double mle = 0.0;
		private double pmi = 0.0;
		private double t = 0.0;
		private double dice = 0.0;
		private double ll = 0.0;

		public double getMle() {
			return mle;
		}

		public void setMle(double mle) {
			this.mle = mle;
		}

		public double getPmi() {
			return pmi;
		}

		public void setPmi(double pmi) {
			this.pmi = pmi;
		}

		public double getT() {
			return t;
		}

		public void setT(double t) {
			this.t = t;
		}

		public double getDice() {
			return dice;
		}

		public void setDice(double dice) {
			this.dice = dice;
		}

		public double getLl() {
			return ll;
		}

		public void setLl(double ll) {
			this.ll = ll;
		}

		public Cand(int candId) {
			this.candId = candId;
		}

		public List<String> getNgramLemmas() {
			return ngramLemmas;
		}
		
		public void addNgramLemma(String ngramLemma) {
			this.ngramLemmas.add(ngramLemma);
		}
		
		public List<String> getNgramPos() {
			return ngramPos;
		}
		
		public void addNgramPos(String ngramPos) {
			this.ngramPos.add(ngramPos);
		}
		
		public int getNgramFreq() {
			return ngramFreq;
		}
		
		public void setNgramFreq(int ngramFreq) {
			this.ngramFreq = ngramFreq;
		}

		public int getCandId() {
			return candId;
		}

		public void setCandId(int candId) {
			this.candId = candId;
		}

		@Override
		public String toString() {
			return "Cand [ngramLemmas=" + ngramLemmas + ", ngramPos="
					+ ngramPos + ", ngramFreq=" + ngramFreq + ", candId="
					+ candId + ", mle=" + mle + ", pmi=" + pmi + ", t=" + t
					+ ", dice=" + dice + ", ll=" + ll + "]";
		}
		
		public String toCsv() {
			return ngramLemmas.toString() + "\t" + ngramPos.toString() + "\t" + ngramFreq + "\t"
					+ "\t" + mle + "\t" + pmi + "\t" + t
					+ "\t" + dice + "\t" + ll ;
		}
	}

	private static final String DICE = "dice";
	private static final String MLE = "mle";
	private static final String PMI = "pmi";
	private static final String LL = "ll";
	private static final String T = "t";
	
	public static void main(String[] args) throws IOException  {
		InvalidTMXFilter script = new InvalidTMXFilter();
		script.createOptions();
		script.parseOptions(args);
		List<File> inputFiles =  script.getFilesFromInputDirectory(script.getInputDir(), script.getFileFilterRegex(), script.isProcessRecursively());
		logger.info(script.getInputDir().getAbsolutePath());
		logger.info(inputFiles.toString());
		script.process(inputFiles);
	}

	@Override
	protected void processDoc(Document doc) {
		Element tmx = doc.getRootElement();

		for (int i = 0; i < tmx.getChildCount(); i++) {
			getTus(tmx.getChild(i));
		}
	}
	

	public  void getTus(Node current) {
		if (current instanceof Element) {
			Element candEle = (Element) current;
			if (candEle.getLocalName().equals("cand")) {
				Cand cand = new Cand(Integer.parseInt(candEle.getAttributeValue("candid")));
				getNgrams(candEle, cand);
				//getOccurs(candEle, cand);
				getFeatures(candEle, cand);
				System.out.println(cand.toCsv());
			}
		}
	}

	private void getFeatures(Element candEle, Cand cand) {
		Element features = candEle.getChildElements("features").get(0);
		if (features != null) {
			Elements feats = features.getChildElements("feat");
			for (int i = 0; i < feats.size(); i++) {
				Element feat = feats.get(i);
				String featName = feat.getAttributeValue("name");
				featName = featName.substring(0, featName.indexOf("_"));
				Double value = Double.parseDouble(feat.getAttributeValue("value"));
				switch (featName) {
				case DICE:
					cand.setDice(value);
					break;
				case MLE:
					cand.setMle(value);
					break;
				case PMI:
					cand.setPmi(value);
					break;
				case LL:
					cand.setLl(value);
					break;
				case T:
					cand.setT(value);
					break;
				}
			}
		}
		
	}

	private void getNgrams(Element candEle, Cand cand) {
		Element ngram = candEle.getChildElements("ngram").get(0);
		if (ngram != null) {
			Elements lemmas = ngram.getChildElements("w");
			for (int i = 0; i < lemmas.size(); i++) {
				cand.addNgramLemma(lemmas.get(i).getAttributeValue("lemma"));
				cand.addNgramPos(lemmas.get(i).getAttributeValue("pos"));
			}
			cand.setNgramFreq(Integer.parseInt(ngram.getChildElements("freq").get(0).getAttributeValue("value")));
		}
	}

}