import javax.print.Doc;

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import com.sun.org.apache.xalan.internal.xsltc.runtime.*;
import org.w3c.dom.*;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** jack analyzer compilation engine*/
public class CompilationEngine {


    /** a regex for a comment*/
    private static final String ONE_LINER_COMMENT ="^/{2}";
    private static final Pattern COMMENT_PATTERN= Pattern.compile(ONE_LINER_COMMENT);

    /** a regex for an empty line*/
    private static final String EMPTY_LINE= "^\\s*+$";
    private static final Pattern EMPTY_LINE_PATTERN= Pattern.compile(EMPTY_LINE);

    private static final String KEYWORD= "keyword";
    private static final String IDENTIFIER="identifier";
    private static final String SYMBOL= "symbol";

    private static final String CLASS= "class";
    private static final String CLASS_VAR_DEC= "classVarDec";
    private static final String SUB_ROUTINE_DEC="subroutineDec";
    private static final String PARAMETER_LIST= "parameterList";

    private static final String FIELD_OR_STATIC="\\s++(field|static)\\s++";
    private static final String FUNCTIONS_DEC="\\s++(method|function|constructor)\\s++";
    private static final String FUNCTIONS_TYPE= "\\s++(void|int|char|boolean)\\s++";
    private static final String PARAMETER_TYPE= "\\s++(int|char|boolean)\\s++";
    private static final String COMMA=",";
    private static final String END_BRACKETS= "\\)";


    /** the tokens input xml*/
    private Document tokenXml;
    /** a stack that entails the current block the compilation engine is processing.*/
    private Stack<String> blocks; //todo to think i don't really need it
    /** the current element being compiled*/
    private Element currentElement;

    /** the current matcher*/
    private Matcher curMatcher;
    /** the output xml document*/
    private Document xmlDoc;
    /** a node list made by the tokenizer*/
    private NodeList tokenList;

    private int counter=1;

    /** constructor*/
    public CompilationEngine( Document tokenXml, Document xmlDoc){
        this.tokenXml= tokenXml;
        this.blocks= new Stack<>();
        this.xmlDoc=xmlDoc;
        Element rootElement= tokenXml.getDocumentElement();
        this.tokenList=rootElement.getChildNodes();
        this.currentElement= (Element)this.tokenList.item(1);
    }

    /**
     * get the xml document
     * @return the xml document
     */
    public Document getXmlDoc(){
        return this.xmlDoc;
    }




    /**
     * initiate compilation by starting on compiling the class keyword
     */
    public void compileClass(){
        Element rootElement =xmlDoc.createElement(CLASS); // add the root of the xml as class
        this.xmlDoc.appendChild(rootElement);
        // add the class keyword
        addKeyword(rootElement);
        // add the name of the class
        addIdentifier(rootElement);
        // add the symbol "{"
        addSymbol(rootElement);

        // compile the class variable declarations
        compileClassVarDec(rootElement);
    }

    /**
     * compile field or static variable declarations
     * @param rootElement the class element
     */
    private void compileClassVarDec(Element rootElement){

        while(this.currentElement.getTextContent().matches(FIELD_OR_STATIC)) {

            // add a field or static variable
            Element classVarElement= xmlDoc.createElement(CLASS_VAR_DEC);
            rootElement.appendChild(classVarElement);

            addKeyword(classVarElement); // add field or static keyword
            addKeyword(classVarElement); // add the type of the variable
            addIdentifier(classVarElement); // add the name of the variable
            checkForAnotherVariable(classVarElement);
            addSymbol(classVarElement); // add the symbol ";"

        }
        compileSubroutine(rootElement);

    }

    /**
     * compile subroutines such as constructor, method and function
     * @param rootElement the class element
     */
    private void compileSubroutine(Element rootElement){

        while(this.currentElement.getTextContent().matches(FUNCTIONS_DEC)) {
            // add a kind of method
            Element subRoutineDec= xmlDoc.createElement(SUB_ROUTINE_DEC);
            rootElement.appendChild(subRoutineDec);

            addKeyword(subRoutineDec); //add the type of the function

            //add the return type of the function
            if(this.currentElement.getTextContent().matches(FUNCTIONS_TYPE)){
                addKeyword(subRoutineDec);
            }else{
                addIdentifier(subRoutineDec);
            }
            addIdentifier(subRoutineDec); //add the name of the function

            addSymbol(subRoutineDec); //add the symbol "("
            compileParameterList(subRoutineDec); //compile the function's parameters
            addSymbol(subRoutineDec); //add the symbol ")"

            compileSubroutineBody(subRoutineDec); //compile the body of the subroutine
        }
    }

    /**
     * compile the function, method and constructor parameters
     * @param subRoutineDec the root element
     */
    private void compileParameterList(Element subRoutineDec){
        // add the parameters
        Element parameterList= xmlDoc.createElement(PARAMETER_LIST);
        subRoutineDec.appendChild(parameterList);
        //check if there is parameters
        while(!this.currentElement.getTextContent().matches(END_BRACKETS)){
            //check for a comma if it exists add it
            if(this.currentElement.getTextContent().matches(COMMA)){
                addSymbol(parameterList);
            }
            //check if the parameter is an int , a char or a boolean
            if(this.currentElement.getTextContent().matches(PARAMETER_TYPE)){
                addKeyword(parameterList);
            }
            else{
                addIdentifier(parameterList); // add a object base parameter type
            }
            addIdentifier(parameterList); //add the name of the parameter
        }
    }

    /**
     * compile the subroutine body
     * @param subRoutineDec an element representing the subroutine declaration
     */
    private void compileSubroutineBody(Element subRoutineDec){

    }
    /**
     * advance the element
     */
    private void advanceElement(){ //todo decide what to do when the counter is bigger than tokenlist length
        counter+=2;
        if(counter<tokenList.getLength()) {
            this.currentElement = (Element) tokenList.item(counter);
        }
    }

    /**
     * add a keyword element to the xml doc
     * @param mainElement the father element
     */
    private void addKeyword(Element mainElement){
        //set keywordElement
        Element keywordElement =xmlDoc.createElement(KEYWORD);
        Text keywordText= xmlDoc.createTextNode(this.currentElement.getTextContent());
        keywordElement.appendChild(keywordText);

        //connect to the father element
        mainElement.appendChild(keywordElement);
        advanceElement();
    }

    /**
     * add an identifier to the xml doc
     * @param mainElement the father element
     */
    private void addIdentifier(Element mainElement){
        //set identifierElement
        Element identifierElement =xmlDoc.createElement(IDENTIFIER);
        Text name= xmlDoc.createTextNode(this.currentElement.getTextContent());
        identifierElement.appendChild(name);

        //connect to the father element
        mainElement.appendChild(identifierElement);
        advanceElement();
    }

    /**
     * add a symbol to the xml doc
     * @param  mainElement the father element
     */
    private void addSymbol(Element mainElement){
        //set symbolElement
        Element symbolElement= xmlDoc.createElement(SYMBOL);
        Text symbolText= xmlDoc.createTextNode(this.currentElement.getTextContent());
        symbolElement.appendChild(symbolText);

        //connect to the father element
        mainElement.appendChild(symbolElement);

        advanceElement();
    }

    /**
     * check for another variable separated by comma
     */
    private void checkForAnotherVariable(Element varDecElement){
        while (this.currentElement.getTextContent().matches(COMMA)){
            addSymbol(varDecElement); //add the comma
            addIdentifier(varDecElement); //add a variable name
        }
    }



















    /**
     * check if the line is blank
     * @param line a string that represent the specific line in the asm file
     * @return return true if it was a blank line, false otherwise
     */
    private boolean deleteBlankLines(String line){
        Matcher m=EMPTY_LINE_PATTERN.matcher(line);
        return m.find(); // check for an empty line

    }

    /**
     * check if the line is a comment
     * @param line a string that represent the specific line in the asm file
     * @return return true if it was a comment, false otherwise
     */
    private boolean deleteOneLinerComment(String line)
    {
        Matcher m= COMMENT_PATTERN.matcher(line);
        return m.lookingAt();
    }

}
