import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	

	/*
	 * some constants
	 */
	private static String TOKENS = "tokens";
	private static String KEYWORD = "keyword";
	private static String SYMBOL = "symbol";
	private static String IDENTIFIER = "identifier";
	private static String INTEGER_CONSTANT = "integerConstant";
	private static String STRING_CONSTANT = "stringConstant";

	//	the xml document
	private Document doc;
	
	private TokenReader reader;
	private PreProcessor preProcessor;
	private ArrayList<String> jacklines;
	
	/**
	 * the tokenizer constructor.
	 */
	public Tokenizer(Document doc) {
		this.jacklines=new ArrayList<>();
		preProcessor = new PreProcessor();
		
		try{
        
        this.doc = doc;
        // create and insert the root element
        Element rootElement = this.doc.createElement(TOKENS);
        this.doc.appendChild(rootElement);
        
        reader = new TokenReader(rootElement, doc);

	    } catch (Exception e) {
	          e.printStackTrace();
	    }
	}

	/**
	 * get the jack code line
	 * @return return JackLines
	 */
	public ArrayList<String> getJackLines(){
		return this.jacklines;
	}
	
/**
 * get buffered reader, pharse the file to tokenes and return the XML documents.
 * @return return the xml document
 * @throws Exception
 */
	public Document readCodeFile(){
		// read line
		for(int i=0;i<jacklines.size();i++){
			reader.newLine(jacklines.get(i));
			while(reader.getNextElement() != null){}
		}

		return this.doc;
	}



	/**
	 * the main pharsing class
	 * @author omri
	 *
	 */
	private class TokenReader{
		private Document doc;
		private Element rootElement;
		
		private String elementsTypes[];
		private Pattern patterns[];
		
		private String codeLine;
		
		public TokenReader(Element root, Document doc) {
			
			this.doc = doc;
			
			this.rootElement = root;

			
			String keywordRegex = "class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return";
			String symbolRegex = "\\.|\\{|\\}|\\(|\\)|;|\\,|\\[|\\]|\\*|\\-|\\+|/|&|<|>|=|~";//\\.|\\{|\\}";//|(|)|[|]|*|-|/|&|<|>|=|~";//TODO add '|'
			String integerConstantRegex = "[0-9]+";
			String stringConstantRegex = "\"[^\"\n]+\""; // TODO
			String identifierRegex = "\\b\\w+\\b";//"[A-Za-z]+";//|_][A-Z|a-z|_|0-9]*";
			
			Pattern keyWordPattern = Pattern.compile(keywordRegex);
			Pattern symbolPattern = Pattern.compile(symbolRegex);
			Pattern integerConstantPattern = Pattern.compile(integerConstantRegex);
			Pattern stringConstantPattern = Pattern.compile(stringConstantRegex);
			Pattern identifierPattern = Pattern.compile(identifierRegex);
			
			this.patterns = new Pattern[5];
			patterns[0] = keyWordPattern;
			patterns[1] = symbolPattern;
			patterns[2] = integerConstantPattern;
			patterns[3] = stringConstantPattern;
			patterns[4] = identifierPattern;
			
			elementsTypes = new String[5];
			elementsTypes[0] = KEYWORD;
			elementsTypes[1] = SYMBOL;
			elementsTypes[2] = INTEGER_CONSTANT;
			elementsTypes[3] = STRING_CONSTANT;
			elementsTypes[4] = IDENTIFIER;
		}
		
		public void newLine(String codeLine){
			
			this.codeLine = preProcessor.preProcess(codeLine);
		}
		
		/**
		 * TODO throw exceptions
		 * @return the next element if exists. if EOL, return null.
		 */
		public Element getNextElement() {
			if ((codeLine == null) || (codeLine.length() == 0)) {
				return null;
			}
			for (int i = 0; i < 5; i++) {

				Matcher m = patterns[i].matcher(codeLine);

				if (m.find() && m.start() == 0) {

					String strToken = codeLine.substring(0, m.end());
					Element element = doc.createElement(elementsTypes[i]);
					element.appendChild(doc.createTextNode(strToken));
					rootElement.appendChild(element);


					int a = m.end();
					String cutLine = "";
					if (a < codeLine.length())
						cutLine = codeLine.substring(a);
					cutLine = cutLine.trim();
					this.codeLine = cutLine;

					return element;
				}
			}
			return null;
			//todo when will you have exception?
//			System.out.println("mismatch");
//			throw new Exception();
		}
	}
	
	/**
	 * this class used for deleting comments.
	 * it get 1 line at a time and return the line after the non-code parts has deleted.
	 */
	private class PreProcessor{
		boolean openMultilineComment = false;
		
		/**
		 * the main preprocessing method.
		 * is called from the newLine method of the tokenReader.
		 */
		public String preProcess(String line){
			
			line = line.trim();
			
			if(line.length() == 0)
				return line;
			
			if(openMultilineComment){
				if(line.contains("*/")){
					openMultilineComment = false;
					return preProcess(line.substring(line.indexOf("*/")+2));

				} else {
					return "";
				}
			}
			
			//	comment to the end of the line
			if(line.contains("//")){
				line = line.substring(0, line.indexOf("//")).trim();
				return line;
			}
			
			if( line.contains("/*") ){
				openMultilineComment = true;
				return this.preProcess(line.substring(line.indexOf("/*")));
			}
			return line;
		}
		
		
		
	}

	
}
