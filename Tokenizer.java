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
		for(int k=0;k<jacklines.size();k++){
			reader.newLine(jacklines.get(k));
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

			
			String keywordRegex = "\\bclass\\b|\\bconstructor\\b|\\bfunction\\b|\\bmethod\\b|\\bfield\\b|\\bstatic\\b|\\bvar\\b|\\bint\\b|\\bchar\\b|\\bboolean\\b|\\bvoid\\b|\\btrue\\b|\\bfalse\\b|\\bnull\\b|\\bthis\\b|\\blet\\b|\\bdo\\b|\\bif\\b|\\belse\\b|\\bwhile\\b|\\breturn\\b";
			String symbolRegex = "(\\.|\\{|\\}|\\(|\\)|;|\\,|\\[|\\]|\\*|\\-|\\+|/|&|<|>|=|~|\\|)";//\\.|\\{|\\}";//|(|)|[|]|*|-|/|&|<|>|=|~";//TODO add '|'
			String integerConstantRegex = "[0-9]+";
			String stringConstantRegex = "\"[^\"\n]+\""; // TODO
			String identifierRegex = "\\b\\w+\\b";//"[A-Za-z]+";//|_][A-Z|a-z|_|0-9]*";
			String oneLineCommentRegex = "//.*";
			
			Pattern keyWordPattern = Pattern.compile(keywordRegex);
			Pattern symbolPattern = Pattern.compile(symbolRegex);
			Pattern integerConstantPattern = Pattern.compile(integerConstantRegex);
			Pattern stringConstantPattern = Pattern.compile(stringConstantRegex);
			Pattern identifierPattern = Pattern.compile(identifierRegex);
			Pattern oneLineCommentPattern = Pattern.compile(oneLineCommentRegex);
			
			this.patterns = new Pattern[6];
			patterns[1] = keyWordPattern;
			patterns[2] = symbolPattern;
			patterns[3] = integerConstantPattern;
			patterns[4] = stringConstantPattern;
			patterns[5] = identifierPattern;
			patterns[0] = oneLineCommentPattern;
			
			
			elementsTypes = new String[6];
			elementsTypes[1] = KEYWORD;
			elementsTypes[2] = SYMBOL;
			elementsTypes[3] = INTEGER_CONSTANT;
			elementsTypes[4] = STRING_CONSTANT;
			elementsTypes[5] = IDENTIFIER;
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
			for (int i = 0; i < 6; i++) {

				Matcher m = patterns[i].matcher(codeLine);

				if (m.find() && m.start() == 0) {
					
					if(i == 0){
						// comment to the end of the line
						this.codeLine = "";
						return null;
					}

					String strToken = codeLine.substring(0, m.end());
					if(this.codeLine.indexOf('\"')==0&& i==4){
						strToken=this.codeLine.substring(1,strToken.length()-1);
					}
					strToken=" "+ strToken+" ";
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
			line= line.trim();
			if(openMultilineComment){
				return closeComment(line); // close a comment and returns a shorter string
			}
			return openComment(line); // open a comment and returns a shorter string
		}

		/**
		 * delete the comment start
		 * @param line the line being processed
		 * @param length the length of the comments symbols
		 * @param location the location of the initiation of the comment
		 * @param flag a boolean variable to signify the openMultilineComment status
		 * @return a string without these comments symbols
		 */
		private String dealWithComment(String line,int length,int location, boolean flag){
			openMultilineComment = flag;
			if(location==0){ // if the line start with a comment symbol
				return line.substring(length);
			}else if(!flag){ // if a "*/" char was seen at a new line
				return line.substring(location+length);
			}
			else{ //if the line contains the comment symbol "/*"
				if(line.contains("*/")){ //if a "*/" was seen at the same line
					openMultilineComment=false;
					return line.substring(0,location)+" "+ line.substring(line.indexOf("*/")+2);
				}
				return line.substring(0,location-1)+ line.substring(location+length);
			}
		}

		private String closeComment(String line){
			if(line.startsWith("*/")){
				return preProcess(dealWithComment(line,2,0,false));
			}
			if(line.contains("*/")){
				return preProcess(dealWithComment(line,2,line.indexOf("*/"),false));
			} else {
				return "";
			}
		}

		private String openComment(String line){
			if( line.startsWith("/**") ){
				return this.preProcess(dealWithComment(line,3 ,0,true));
			}else if (line.startsWith("/*")){
				return this.preProcess(dealWithComment(line,2 ,0,true));
			}
			else if( line.contains("/**")){
				return this.preProcess(dealWithComment(line,3,line.indexOf("/**"),true));
			}else if(line.contains("/*")){
				return this.preProcess(dealWithComment(line,2,line.indexOf("/*"),true));
			}
			return line;
		}
		
	}

	
}
