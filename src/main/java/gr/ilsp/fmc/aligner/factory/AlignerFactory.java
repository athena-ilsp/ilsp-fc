package gr.ilsp.fmc.aligner.factory;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignerFactory {

	private String[] alignerIds = {"hunalign","maligna"};
	private static final Logger logger = LoggerFactory.getLogger(AlignerFactory.class);

	public Aligner getAligner(String aligner) {
		if (aligner.equalsIgnoreCase("maligna")) {
			return new MalignaAligner();
		} else {
			logger.warn("Aligner " + aligner + " not among known aligners: " + Arrays.toString(alignerIds));
			logger.warn("Using default aligner: " + MalignaAligner.class.getName());
			return new MalignaAligner();
		}
	}
}
