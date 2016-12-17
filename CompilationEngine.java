import javax.print.Doc;
import org.w3c.dom.Document;
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

    private static final String KEYWORD= "<keyword>";

    /** the tokens input*/
    private ArrayList<String> tokens;
    /** a stack that entails the current block the compilation engine is processing.*/
    private Stack<String> blocks; //todo find a better name
    /** the current line being compiled*/
    private String currentLine;

    /** the current matcher*/
    private Matcher curMatcher;

    private Document xmlDoc;

    /** constructor*/
    public CompilationEngine(ArrayList<String> tokens, Document xmlDoc){
        this.tokens=tokens;
        this.blocks= new Stack<>();
        this.currentLine=tokens.get(1); //skipping the token at the beginning of the input
        this.xmlDoc=xmlDoc;
    }

    /**
     * get the xml document
     * @return the xml document
     */
    public Document getXmlDoc(){
        return this.xmlDoc;
    }


    /**
     * advance the current line if possible
     * @return the current line
     */
    private String advance(){
        tokens.remove(0); // delete the current line
        if(!tokens.isEmpty()){
            this.currentLine=tokens.get(0); //advance
        }
        else{
            this.currentLine=null; // in case the array list is empty
        }
        return this.currentLine;
    }

    /**
     * initiate compilation by starting on compiling the class keyword
     */
    public void compileClass(){
        xmlLines.add("<class>");
        xmlLines.add(this.currentLine); //add the class keyword
        advance();
        xmlLines.add(this.currentLine); // add the identifier
        advance();
        xmlLines.add(this.currentLine); // add the curly brackets
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
