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
    private static final String SUB_ROUTINE_BODY="subroutineBody";
    private static final String PARAMETER_LIST= "parameterList";
    private static final String VAR_DEC= "varDec";
    private static final String STATEMENT = "statement";
    private static final String LET_STATEMENT= "letStatement";
    private static final String WHILE_STATEMENT= "whileStatement";
    private static final String IF_STATEMENT= "ifStatement";
    private static final String DO_STATEMENT= "doStatement";
    private static final String RETURN_STATEMENT= "returnStatement";
    private static final String EXPRESSION= "expression";
    private static final String TERM ="term";

    private static final String FIELD_OR_STATIC="\\s++(field|static)\\s++";
    private static final String FUNCTIONS_DEC="\\s++(method|function|constructor)\\s++";
    private static final String FUNCTIONS_TYPE= "\\s++(void|int|char|boolean)\\s++";
    private static final String TYPE= "\\s++(int|char|boolean)\\s++";
    private static final String STATEMENT_TYPE= "\\s++(let|while|if|do|return)\\s++";
    private static final String VAR= "\\s++var\\s++";
    private static final String LET ="\\s++let\\s++";
    private static final String WHILE = "\\s++while\\s++";
    private static final String IF = "\\s++if\\s++";
    private static final String DO = "\\s++do\\s++";
    private static final String RETURN = "\\s++return\\s++";
    private static final String COMMA=",";
    private static final String OPEN_SQUARE_BRACKET="\\[";
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
            checkAVarType(parameterList);
            addIdentifier(parameterList); //add the name of the parameter
        }
    }

    /**
     * compile the subroutine body
     * @param subRoutineDec an element representing the subroutine declaration
     */
    private void compileSubroutineBody(Element subRoutineDec){
        //add a subroutine body element
        Element subroutineBody= xmlDoc.createElement(SUB_ROUTINE_BODY);
        subRoutineDec.appendChild(subroutineBody);

        addSymbol(subroutineBody); //add the symbol "{"
        while(this.currentElement.getTextContent().matches(VAR)){
            compileVarDec(subroutineBody);
        }
        compileStatement(subroutineBody);
    }

    /**
     * compile local variable declarations
     * @param subroutineBody the subroutine body element
     */
    private void compileVarDec(Element subroutineBody){
        //add a local variable declaration
        Element varDec= xmlDoc.createElement(VAR_DEC);
        subroutineBody.appendChild(varDec);

        addKeyword(varDec);// add the var keyword
        //check if the parameter is an int , a char or a boolean or an object based class
        checkAVarType(varDec);
        addIdentifier(varDec); //add the name of the variable

        // check if another variable are declared of the same type
        checkForAnotherVariable(varDec);
        addSymbol(varDec); //add the symbol ";"
    }

    /**
     * compile the statement such as while, do, if,else and let
     * @param subroutineBody the subroutine body element
     */
    private void compileStatement(Element subroutineBody){

        //add a statement element
        Element statement = xmlDoc.createElement(STATEMENT);
        subroutineBody.appendChild(statement);
        // check if there is another statement
        while (this.currentElement.getTextContent().matches(STATEMENT_TYPE)) {
            //check for each statement
            if(this.currentElement.getTextContent().matches(LET)) {
                compileLet(statement);
            } else if (this.currentElement.getTextContent().matches(WHILE)){
                compileWhile(statement);
            }else if (this.currentElement.getTextContent().matches(IF)){
                compileIf(statement);
            }else if (this.currentElement.getTextContent().matches(DO)){
                compileDo(statement);
            }else if (this.currentElement.getTextContent().matches(RETURN)){
                compileReturn(statement);
            }
        }
    }

    /**
     * compile a let statement
     * @param statement the statement element
     */
    private void compileLet(Element statement){
        // begin a let statement
        Element let= xmlDoc.createElement(LET_STATEMENT);
        statement.appendChild(let);

        addKeyword(let); //add the let keyword
        addIdentifier(let); //add the name of the variable

        if(this.currentElement.getTextContent().matches(OPEN_SQUARE_BRACKET)){
            addSymbol(let); //add the symbol "["
            compileExpression(let);
            addSymbol(let); //add the symbol "]"
        }
        //todo maybe add calling for a method
        addSymbol(let); // add the symbol "="
        compileExpression(let);
        addSymbol(let); //add the symbol ";"

    }

    /**
     * compile a while statement
     * @param statement the statement element
     */
    private void compileWhile(Element statement){
        // begin a while statement
        Element whileStatement= xmlDoc.createElement(WHILE_STATEMENT);
        statement.appendChild(whileStatement);

        addKeyword(whileStatement); //add the while keyword
        addSymbol(whileStatement); //add the symbol "("
        //compile the while condition
        compileExpression(whileStatement);

        addSymbol(whileStatement); //add the symbol ")"
        addSymbol(whileStatement); //add the symbol "{"

        //compile the statement inside the while
        compileStatement(whileStatement);
    }

    
    /**
     * compile a expression
     * @param rootElement the root element
     */
    private void compileExpression(Element rootElement){
        // add a expression element
        Element expression= xmlDoc.createElement(EXPRESSION);
        rootElement.appendChild(expression);

        compileTerm(expression);
    }

    /**
     * compile a term
     * @param rootElement the root element
     */
    private void compileTerm(Element rootElement){
        // add a term element
        Element term= xmlDoc.createElement(TERM);
        rootElement.appendChild(term);

        addIdentifier(term);
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
     * check if the type of the variable is int,char or boolean else it's an object base class
     * @param rootElement the root element
     */
    private void checkAVarType(Element rootElement){
        if(this.currentElement.getTextContent().matches(TYPE)){
            addKeyword(rootElement);
        }
        else{
            addIdentifier(rootElement); // add a object base parameter type
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
