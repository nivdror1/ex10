
import org.w3c.dom.*;


/** jack analyzer compilation engine*/
public class CompilationEngine {


    private static final String KEYWORD= "keyword";
    private static final String IDENTIFIER="identifier";
    private static final String SYMBOL= "symbol";

    private static final String CLASS= "class";
    private static final String CLASS_VAR_DEC= "classVarDec";
    private static final String SUB_ROUTINE_DEC="subroutineDec";
    private static final String SUB_ROUTINE_BODY="subroutineBody";
    private static final String PARAMETER_LIST= "parameterList";
    private static final String VAR_DEC= "varDec";
    private static final String STATEMENT = "statements";
    private static final String LET_STATEMENT= "letStatement";
    private static final String WHILE_STATEMENT= "whileStatement";
    private static final String IF_STATEMENT= "ifStatement";
    private static final String DO_STATEMENT= "doStatement";
    private static final String RETURN_STATEMENT= "returnStatement";
    private static final String EXPRESSION= "expression";
    private static final String TERM ="term";
    private static final String EXPRESSION_LIST= "expressionList";
    private static final String INTEGER_CONSTANT ="integerConstant";
    private static final String STRING_CONSTANT= "stringConstant";

    private static final String FIELD_OR_STATIC="\\s*+(field|static)\\s*+";
    private static final String FUNCTIONS_DEC="\\s*+(method|function|constructor)\\s*+";
    private static final String FUNCTIONS_TYPE= "\\s*+(void|int|char|boolean)\\s*+";
    private static final String TYPE= "\\s*+(int|char|boolean)\\s*+";
    private static final String STATEMENT_TYPE= "\\s*+(let|while|if|do|return)\\s*+";
    private static final String VAR= "\\s*+var\\s*+";
    private static final String LET ="\\s*+let\\s*+";
    private static final String WHILE = "\\s*+while\\s*+";
    private static final String IF = "\\s*+if\\s*+";
    private static final String DO = "\\s*+do\\s*+";
    private static final String RETURN = "\\s*+return\\s*+";
    private static final String ELSE = "\\s*+else\\s*+";
    private static final String COMMA="\\s*+,\\s*+";
    private static final String OPEN_SQUARE_BRACKET="\\s*+\\[\\s*+";
    private static final String OPEN_BRACKETS= "\\s*+\\(\\s*+";
    private static final String END_BRACKETS= "\\s*+\\)\\s*+";
    private static final String DOT="\\s*+\\.\\s*+";
    private static final String SEMI_COLON="\\s*+;\\s*+";
    private static final String KEYWORD_CONSTANT="\\s*+(this|true|false|null)\\s*+";
    private static final String OPERATORS= "\\s*+(\\||\\+|-|\\*|/|&|<|>||\"|=|~)\\s*+";
    private static final String UNARY_OP= "\\s*+(~|-)\\s*+";
    private static final String DECIMAL_CONSTANT= "\\s*+[0-9]++\\s*+";


    /** the tokens input xml*/
    private Document tokenXml;

    /** the current element being compiled*/
    private Element currentElement;

    /** the output xml document*/
    private Document xmlDoc;
    /** a node list made by the tokenizer*/
    private NodeList tokenList;

    private int counter=1;

    /** constructor*/
    public CompilationEngine( Document tokenXml, Document xmlDoc){
        this.tokenXml= tokenXml;
        this.xmlDoc=xmlDoc;
        xmlDoc.setXmlStandalone(true);
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
        addElement(rootElement,KEYWORD);

        // add the name of the class
        addElement(rootElement,IDENTIFIER);
        // add the symbol "{"
        addElement(rootElement,SYMBOL);

        // compile the class variable declarations
        compileClassVarDec(rootElement);

        addElement(rootElement,SYMBOL); //add the symbol "}"
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

            addElement(classVarElement,KEYWORD); // add field or static keyword
            if(this.currentElement.getTextContent().matches(TYPE)) {
                addElement(classVarElement,KEYWORD); // add the type of the variable
            }else{
                addElement(classVarElement,IDENTIFIER); //add the object base type of the variable
            }
            addElement(classVarElement,IDENTIFIER); // add the name of the variable
            checkForAnotherVariable(classVarElement);
            addElement(classVarElement,SYMBOL); // add the symbol ";"

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

            addElement(subRoutineDec,KEYWORD); //add the type of the function

            //add the return type of the function
            if(this.currentElement.getTextContent().matches(FUNCTIONS_TYPE)){
                addElement(subRoutineDec,KEYWORD);
            }else{
                addElement(subRoutineDec,IDENTIFIER);
            }

            addElement(subRoutineDec,IDENTIFIER); //add the name of the function

            addElement(subRoutineDec,SYMBOL); //add the symbol "("
            compileParameterList(subRoutineDec); //compile the function's parameters
            addElement(subRoutineDec,SYMBOL); //add the symbol ")"

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
                addElement(parameterList,SYMBOL);
            }
            //check if the parameter is an int , a char or a boolean
            checkAVarType(parameterList);
            addElement(parameterList,IDENTIFIER); //add the name of the parameter
        }
        if(!parameterList.hasChildNodes()){
            Text empty= xmlDoc.createTextNode("\t");
            parameterList.appendChild(empty);
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

        addElement(subroutineBody,SYMBOL); //add the symbol "{"
        while(this.currentElement.getTextContent().matches(VAR)){
            compileVarDec(subroutineBody);
        }
        compileStatement(subroutineBody);
        addElement(subroutineBody,SYMBOL); //add the symbol "}"
    }

    /**
     * compile local variable declarations
     * @param rootElement the current block element
     */
    private void compileVarDec(Element rootElement){
        //add a local variable declaration
        Element varDec= xmlDoc.createElement(VAR_DEC);
        rootElement.appendChild(varDec);

        addElement(varDec,KEYWORD);// add the var keyword
        //check if the parameter is an int , a char or a boolean or an object based class
        checkAVarType(varDec);
        addElement(varDec,IDENTIFIER); //add the name of the variable

        // check if another variable are declared of the same type
        checkForAnotherVariable(varDec);
        addElement(varDec,SYMBOL); //add the symbol ";"
    }

    /**
     * compile the statement such as while, do, if,else and let
     * @param rootElement the root element
     */
    private void compileStatement(Element rootElement){

        //add a statement element
        Element statement = xmlDoc.createElement(STATEMENT);
        rootElement.appendChild(statement);
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

        addElement(let,KEYWORD); //add the let keyword
        addElement(let,IDENTIFIER); //add the name of the variable

        if(this.currentElement.getTextContent().matches(OPEN_SQUARE_BRACKET)){
            addElement(let,SYMBOL); //add the symbol "["
            compileExpression(let);
            addElement(let,SYMBOL); //add the symbol "]"
        }

        addElement(let,SYMBOL); // add the symbol "="
        compileExpression(let);
        addElement(let,SYMBOL); //add the symbol ";"

    }

    /**
     * compile a while statement
     * @param statement the statement element
     */
    private void compileWhile(Element statement){
        // begin a while statement
        Element whileStatement= xmlDoc.createElement(WHILE_STATEMENT);
        statement.appendChild(whileStatement);

        addElement(whileStatement,KEYWORD); //add the while keyword

        //compile the while condition
        compileProgramFlowCondition(whileStatement);

        //compile the statement inside the while
        compileProgramFlowStatement(whileStatement);
    }

    /**
     * compile a if statement
     * @param statement the statement element
     */
    private void compileIf(Element statement){
        // begin a if statement
        Element ifStatement= xmlDoc.createElement(IF_STATEMENT);
        statement.appendChild(ifStatement);

        //compile the if condition
        addElement(ifStatement,KEYWORD); // add the if keyword
        compileProgramFlowCondition(ifStatement);

        //compile the if statement
        compileProgramFlowStatement(ifStatement);

        //compile the else statement if it exists
        if(this.currentElement.getTextContent().matches(ELSE)){
            addElement(ifStatement,KEYWORD); //add the else keyword
            compileProgramFlowStatement(ifStatement);
        }
    }

    /**
     * compile a do statement
     * @param statement the statement element
     */
    private void compileDo(Element statement){
        // begin a do statement
        Element doStatement= xmlDoc.createElement(DO_STATEMENT);
        statement.appendChild(doStatement);

        addElement(doStatement,KEYWORD); // add the do keyword

        addElement(doStatement,IDENTIFIER); //add the name of the class or the name of the subroutine

        //in case the call wasn't made of the class
        if(this.currentElement.getTextContent().matches(DOT)){
            addElement(doStatement,SYMBOL); //add the symbol "."
            addElement(doStatement,IDENTIFIER); //add the name of the subroutine
        }

        // compile the parameter list
        addElement(doStatement,SYMBOL); //add the symbol "("
        compileExpressionList(doStatement);
        addElement(doStatement,SYMBOL); //add the symbol ")"

        addElement(doStatement,SYMBOL); // add the symbol ";"
    }

    /**
     * compile a return statement
     * @param statement the statement element
     */
    private void compileReturn(Element statement){
        // begin a return statement
        Element returnStatement= xmlDoc.createElement(RETURN_STATEMENT);
        statement.appendChild(returnStatement);

        addElement(returnStatement,KEYWORD); //add the return keyword
        if(!this.currentElement.getTextContent().matches(SEMI_COLON)){
            compileExpression(returnStatement);
        }
        addElement(returnStatement,SYMBOL); //add the symbol ";"
    }
    /**
     * compile an expression list
     * @param rootElement the root element
     */
    private void compileExpressionList(Element rootElement){
        // begin an expressionList
        Element expressionList= xmlDoc.createElement(EXPRESSION_LIST);
        rootElement.appendChild(expressionList);

        //check for a expression
        if(!this.currentElement.getTextContent().matches(END_BRACKETS)){
            //compile the first expression
            compileExpression(expressionList);

            //check for other expressions
            while(this.currentElement.getTextContent().matches(COMMA)){
                addElement(expressionList,SYMBOL); //add the symbol ","
                compileExpression(expressionList);
            }
        }
        if(!expressionList.hasChildNodes()){
            Text empty= xmlDoc.createTextNode("\t");
            expressionList.appendChild(empty);
        }


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
        // check for operators if so compile another term
        while(this.currentElement.getTextContent().matches(OPERATORS)){
           addElement(expression,SYMBOL);
           compileTerm(expression);
       }

    }

    /**
     * begin compile a term - start with String constant
     * @param rootElement the root element
     */
    private void compileTerm(Element rootElement){
        // add a term element
        Element term= xmlDoc.createElement(TERM);
        rootElement.appendChild(term);

        if(this.currentElement.getTagName().matches(STRING_CONSTANT)){
            addElement(term,STRING_CONSTANT); //add the string constant
        }
        else {
            compileTermKeywordConstant(term); //continue term compilation
        }
    }

    /**
     * continue term compilation - continue with keyword constant
     * @param term the root element
     */
    private void compileTermKeywordConstant(Element term){
        if(this.currentElement.getTextContent().matches(KEYWORD_CONSTANT)){//todo change soon!!!!!!!
             addElement(term,KEYWORD); //add the keyword this
        }else{
            compileDecimalConstant(term); //continue term compilation
        }
    }

    /**
     * continue term compilation - continue with decimal constant
     * @param term the root element
     */
    private void compileDecimalConstant(Element term){
        if(this.currentElement.getTextContent().matches(DECIMAL_CONSTANT)){ //todo change soon!!!!!!!
            addElement(term,INTEGER_CONSTANT); // add the integer constant
        }else{
            compileTermContinuation(term); //continue term compilation
        }
    }
    /**
     * continue term compilation - continue with array index or a method or new expression list or unary operation
     * @param term the root element
     */
    private void compileTermContinuation(Element term){

        compileTermOfInitExpression(term); // check for initiation of a new expression
        if(this.currentElement.getTextContent().matches(OPEN_SQUARE_BRACKET)){

            addElement(term,SYMBOL); //add the symbol "["
            compileExpression(term);
            addElement(term,SYMBOL); //add the symbol "]"

        }else if(this.currentElement.getTextContent().matches(DOT)){

            addElement(term,SYMBOL); //add the symbol "."
            addElement(term,IDENTIFIER); //add the name of the method
            addElement(term,SYMBOL); //add the symbol "("
            compileExpressionList(term);
            addElement(term,SYMBOL); //add the symbol ")"

        }else if(this.currentElement.getTextContent().matches(OPEN_BRACKETS)){
            addElement(term,SYMBOL); //add the symbol "("
            compileExpressionList(term);
            addElement(term,SYMBOL); //add the symbol ")"
           }
         else if (this.currentElement.getTextContent().matches(UNARY_OP)
                && this.currentElement.getPreviousSibling().
                getPreviousSibling().getTextContent().matches(OPEN_BRACKETS"|\\s*+(=|&)\\s*+")){
            addElement(term,SYMBOL); //add the symbol "~,-"
            compileTerm(term);
        }
    }
    /**
     * continue term compilation - continue with a new expression
     * @param term the root element
     */
    private void compileTermOfInitExpression(Element term){
        if(this.currentElement.getTextContent().matches(OPEN_BRACKETS)){
            addElement(term,SYMBOL); //add the symbol "("
            compileExpression(term);
            addElement(term,SYMBOL); //add the symbol ")"
        }
        if(this.currentElement.getTagName().matches(IDENTIFIER)) {
            addElement(term, IDENTIFIER);
        }
    }
    /**
     * advance the element
     */
    private void advanceElement(){
        counter+=2;
        if(counter<tokenList.getLength()) {
            this.currentElement = (Element) tokenList.item(counter);
        }
    }

    /**
     * add an element whether it is a keyword, identifier, symbol or constant
     * @param rootElement the root element
     * @param tag the tag name of the element
     */
    private void addElement(Element rootElement, String tag){
        //set keywordElement
        Element tagElement =xmlDoc.createElement(tag);
        Text tagText= xmlDoc.createTextNode(this.currentElement.getTextContent());
        tagElement.appendChild(tagText);

        //connect to the father element
        rootElement.appendChild(tagElement);
        advanceElement();
    }


    /**
     * check for another variable separated by comma
     */
    private void checkForAnotherVariable(Element varDecElement){
        while (this.currentElement.getTextContent().matches(COMMA)){
            addElement(varDecElement,SYMBOL); //add the comma
            addElement(varDecElement,IDENTIFIER); //add a variable name
        }
    }

    /**
     * check if the type of the variable is int,char or boolean else it's an object base class
     * @param rootElement the root element
     */
    private void checkAVarType(Element rootElement){
        if(this.currentElement.getTextContent().matches(TYPE)){
            addElement(rootElement,KEYWORD);
        }
        else{
            addElement(rootElement,IDENTIFIER); // add a object base parameter type
        }
    }

    /**
     * compile the statement inside the while, if or else blocks
     * @param rootElement the root element
     */
    private void compileProgramFlowStatement(Element rootElement){

        addElement(rootElement,SYMBOL); //add the symbol "{"
        if(this.currentElement.getTextContent().matches(VAR)){
            compileVarDec(rootElement);
        }
        compileStatement(rootElement);
        addElement(rootElement,SYMBOL); //add the symbol "}"
    }

    /**
     * compile the if or while conditions
     * @param rootElement the root element
     */
    private void compileProgramFlowCondition(Element rootElement){
        addElement(rootElement,SYMBOL); // add the symbol "("
        compileExpression(rootElement);
        addElement(rootElement,SYMBOL); //add the symbol ")"
    }

}
