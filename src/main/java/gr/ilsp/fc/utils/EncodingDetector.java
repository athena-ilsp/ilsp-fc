/**
 * 
 */
package gr.ilsp.fc.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 */
public class EncodingDetector {
    /**
     * Logger for this class
     */
    static Logger logger = LoggerFactory.getLogger(EncodingDetector.class.getCanonicalName());
	CharsetDetector charsetDetector = null;

	public static void main(String[] args) {
		EncodingDetector encodingDetector = new EncodingDetector();
		encodingDetector.initialize();
		try {
			Charset charset = encodingDetector.detect(new File(args[0]));
			if (charset != null) {
				logger.info(charset.displayName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initialize() {
		charsetDetector = new CharsetDetector();
	}
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Charset detect(File file) throws IOException   {
		InputStream is = FileUtils.openInputStream(file);
		return detect(is);
	}

	/**
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public Charset detect(InputStream is) throws IOException {
		byte[] bytes = IOUtils.toByteArray(is);
	    return detect(bytes);
	}

	/**
	 * @param bytes
	 * @return
	 */
	public Charset detect(byte[] bytes) {
		charsetDetector.setText(bytes);
	    CharsetMatch match = charsetDetector.detect();
        if (match != null) {
            try {
            	return Charset.forName(match.getName());
            } catch(UnsupportedCharsetException e)  {
                logger.warn("Charset detected as " + match.getName() + " but the JVM does not support this, detection skipped");
            }
        }
        return null;
	}

}
