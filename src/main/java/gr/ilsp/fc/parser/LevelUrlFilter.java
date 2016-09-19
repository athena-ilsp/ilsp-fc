package gr.ilsp.fc.parser;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import bixo.datum.UrlDatum;
import bixo.urls.BaseUrlFilter;

// Filter URLs that fall deeper than the targeted depth
@SuppressWarnings({ "serial", "deprecation" })
public class LevelUrlFilter extends BaseUrlFilter {
	private static final Logger LOGGER = Logger.getLogger(LevelUrlFilter.class);

	private int _depth = 100;
		
	public LevelUrlFilter(int depth) {
		_depth = depth;
	}
		
	@Override
	public boolean isRemove(UrlDatum datum) {
		String urlAsString = datum.getUrl();
		try {
			URL url = new URL(urlAsString);
			int d= StringUtils.countMatches(url.getPath(), "/");
			if (d>_depth)
				return true;
			else
				return false;
		} catch (MalformedURLException e) {
			LOGGER.warn("Invalid URL: " + urlAsString);
			return true;
		}
	}
}
