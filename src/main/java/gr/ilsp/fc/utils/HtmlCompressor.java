package gr.ilsp.fc.utils;

public class HtmlCompressor extends com.googlecode.htmlcompressor.compressor.HtmlCompressor {

	public HtmlCompressor() {
		super();

		this.setEnabled(true);                   //if false all compression is off (default is true)
		this.setRemoveComments(true);            //if false keeps HTML comments (default is true)
		this.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
		this.setRemoveIntertagSpaces(true);      //removes inter-tag whitespace characters
		this.setPreserveLineBreaks(false);        //preserves original line breaks
		this.setCompressCss(true);               //compress inline css 
		this.setCompressJavaScript(false);        //compress inline javascript

		//this.setRemoveSurroundingSpaces("br,p"); //remove spaces around provided tags
		this.setRemoveQuotes(false);              //removes unnecessary tag attribute quotes
		this.setSimpleDoctype(false);             //simplify existing doctype
		this.setRemoveScriptAttributes(false);    //remove optional attributes from script tags
		this.setRemoveStyleAttributes(false);     //remove optional attributes from style tags
		this.setRemoveLinkAttributes(false);      //remove optional attributes from link tags
		this.setRemoveFormAttributes(false);      //remove optional attributes from form tags
		this.setRemoveInputAttributes(false);     //remove optional attributes from input tags
		this.setSimpleBooleanAttributes(false);   //remove values from boolean tag attributes
		this.setRemoveJavaScriptProtocol(false);  //remove "javascript:" from inline event handlers
		this.setRemoveHttpProtocol(false);        //replace "http://" with "//" inside tag attributes
		this.setRemoveHttpsProtocol(false);       //replace "https://" with "//" inside tag attributes

//		this.setYuiCssLineBreak(80);             //--line-break param for Yahoo YUI Compressor 
//		this.setYuiJsDisableOptimizations(true); //--disable-optimizations param for Yahoo YUI Compressor 
//		this.setYuiJsLineBreak(-1);              //--line-break param for Yahoo YUI Compressor 
//		this.setYuiJsNoMunge(true);              //--nomunge param for Yahoo YUI Compressor 
//		this.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor 

		//use Google Closure Compiler for javascript compression
		//this.setJavaScriptCompressor(new ClosureJavaScriptCompressor(CompilationLevel.SIMPLE_OPTIMIZATIONS));

		//use your own implementation of css comressor
		//this.setCssCompressor(new MyOwnCssCompressor());
	}


}
