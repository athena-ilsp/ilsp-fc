package gr.ilsp.fc.sandbox;

import java.io.File;
import java.io.IOException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

public abstract class AbstractXOMXMLScanner extends AbstractScanner {

	Builder builder = new Builder();

	@Override
	protected void process(File file) throws IOException {
		try {
			Document doc = builder.build(file);
			processDoc(doc);
		} // indicates a well-formedness error
		catch (ParsingException ex) { 
			System.out.println(file.getAbsolutePath() + " is not well-formed.");
			System.out.println(ex.getMessage());
		} catch (IOException ex) { 
			System.out.println(ex);
		}  

	}

	protected abstract void processDoc(Document doc) ;

}
