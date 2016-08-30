package gr.ilsp.fc.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Seg;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;

public class ValidateUtils {
	private static final Logger LOGGER = Logger.getLogger(ValidateUtils.class);
	private final static String INFO = "info";
	private final static String SITE = "site";
	
	public static void main(String[] args) throws IOException {
		File tmxfile = new File(args[0]);
		//String[] langs = args[1].split(";");
		String site;
		Tmx tmx;
		tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxfile.getAbsolutePath()));
		List<Tu> tus = tmx.getBody().getTu();
		List<Tu> notAnntus = new ArrayList<Tu>();
		for (Tu tu: tus) {
			List<Object> tuProps = tu.getNoteOrProp();
			for (Object obProp : tuProps) {
				Prop prop = (Prop) obProp;
				if (prop.getType().equals(INFO)) {
					if (prop.getContent().get(0).isEmpty()){
						notAnntus.add(tu);
												
						String segment = getSegment(tu.getTuv().get(0).getSeg());
						
					}
				}
			}
		}
		LOGGER.info("Not annotated TUs are: "+ notAnntus.size());
		Random ran = new Random();
		
		Set<Integer> selectedIds = new HashSet<Integer>();
		List<Tu> selectedtus = new ArrayList<Tu>();
		int x = 0;
		float cent = 0;
		while (cent<0.03){
			x = ran.nextInt(notAnntus.size());
			if (selectedIds.contains(x))
				continue;
			selectedtus.get(x);
		}
	}
	
	private static String getSegment(Seg seg) {
		StringBuilder builder = new StringBuilder();
		for (Object object : seg.getContent()) {
			builder.append(object.toString());
		}
		return builder.toString();
	}
}
