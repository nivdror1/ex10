import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** the purpose of this class is to read and write the IO*/
public class IO {
    private static final String BAD_FILE= "file could not been read or write into";
    private static final String FILE_NOT_EXISTS = "enter another file, since the path was wrong";
    private static final String XML = ".xml";
    private static final String TOKEN_XML = "T.xml";
    private static final String DOT = "\\w++\\.";
    private static final String JACK= DOT+"jack";
    private static final Pattern JACK_PATTERN =Pattern.compile(JACK);
    private static final int FIVE =5;
    private static final String SLASH="\\";
    private static final String NEW_LINE="\n";


    /**
     * the main method which control the progress of the assembler
     */
    public static void main(String[] args) {
        File file = new File(args[0]);
        if (file.exists()) // check if the file exists
        {
            //check if it is a file or a directory and get all the appropriate files
            ArrayList<File> jackList =fileOrDirectory(file);
            readAndWriteAllJackFiles(jackList);
        } else {
            System.out.println(FILE_NOT_EXISTS);
        }
    }

    /**
     * check if the input file is an actual file or a directory - filter only the files that ends with ".vm"
     * @param file a file
     * @return a array list of the files that were found
     */
    private static ArrayList<File> fileOrDirectory(File file) {
        File[] filesArray;
        ArrayList<File> jackFiles = new ArrayList<>();
        // check if it a file
        if (file.isFile())
        {
            jackFiles.add(file);
        } else if (file.isDirectory()) //check for a directory
        {
            //create a filter that specifies the suffix of the file
            filesArray = file.listFiles(); // create an array of all the files in the directory
            if(filesArray!=null){
                for (int i = 0; i < filesArray.length; i++) {
                    Matcher m = JACK_PATTERN.matcher(filesArray[i].getName());
                    if (m.lookingAt()) { // find the dot char
                        jackFiles.add(filesArray[i]);
                    }
                }
            }else{
                System.out.println(FILE_NOT_EXISTS);
            }
        }
        return jackFiles;
    }

    /**
     * read all of the vm files
     * @param jackList an array list of vm files
     */
    private static void readAndWriteAllJackFiles(ArrayList <File> jackList) {
        for (int j = 0; j < jackList.size(); j++) {
            String outputFileName = setOutputFileName(jackList.get(j));
            try (FileReader jackFile = new FileReader(jackList.get(j));// define the BufferReader and BufferWriter
                 BufferedReader reader = new BufferedReader(jackFile)) {

                //read the file and tokenize it
                readAndTokenize(reader, jackList.get(j).getAbsolutePath());

                //compile
                compileFile(Tokenizer.getXmlLines(), outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }catch (ParserConfigurationException e2){
                e2.printStackTrace();
            }
        }
    }

    /**
     * compile all files and write a xml file for each and every of them
     * @param listOfTokens the Tokenizer output
     * @param location the location of the output
     */
    private static void compileFile(ArrayList<String> listOfTokens,String location){
            // change the output location
            String outputFileName=location.substring(0,location.length()-FIVE);
            outputFileName+=XML;

            try{
                //create the xml document
                Document xmlDoc= createXmlDoc();
                // compile the Tokenizer output
                CompilationEngine compiler = new CompilationEngine(listOfTokens,xmlDoc);
                compiler.compileClass();
                xmlDoc=compiler.getXmlDoc();

                // serialize to xml file
                writeXml(outputFileName,xmlDoc);

            }
            catch (ParserConfigurationException e){
                e.printStackTrace();
            }

    }

    /**
     * get the name of the asm file and exchange it to asm file
     * @param file the input file
     * @return the file name but with a suffix of a asm file
     */
    private static String setOutputFileName( File file ) {
        String location= file.getAbsolutePath();
        if(file.isFile()){ // if the there is only one file
            location=location.substring(0,location.length()-FIVE);
            location+=TOKEN_XML;
        }
        else{ //if it is a directory
            location+=SLASH+file.getName()+TOKEN_XML;
        }
        return location;
    }

    /**
     * read and tokenize a specific file
     * @param reader a bufferReader
     * @param outputFileName the current name of the output file
     * @throws IOException
     */
    private static void readAndTokenize(BufferedReader reader, String outputFileName)
            throws IOException, ParserConfigurationException {
        //create a xml document
        Document xmlDoc = createXmlDoc();

        Tokenizer tokenizer = new Tokenizer(); //define a new tokenizer
        String text;
        while ((text = reader.readLine()) != null) // add the lines to the container
        {
            tokenizer.getJackLines().add(text);
        }
        xmlDoc= tokenizer.tokenizeJackFile(xmlDoc); // tokenize the vm text
        writeXml(outputFileName,xmlDoc);
    }

    /**
     * serialize the xml file
     * @param outputFileName the name and location of the output file
     * @param xmlDoc the document to serialize
     */
    private static void writeXml(String outputFileName, Document xmlDoc){
        //set output format
        OutputFormat outFormat= new OutputFormat(xmlDoc);
        outFormat.setIndenting(true);

        File outputFile= new File(outputFileName);
        try (FileOutputStream outStream = new FileOutputStream(outputFile)){
            //create a xml serializer
            XMLSerializer serializer= new XMLSerializer(outStream,outFormat);
            // serialize the xml file
            serializer.serialize(xmlDoc);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * create the xml document
     * @return the xml document
     * @throws ParserConfigurationException
     */
    private static Document createXmlDoc() throws ParserConfigurationException{

        // create a new document builder factory
        DocumentBuilderFactory docBFactory = DocumentBuilderFactory.newInstance();
        // create a document builder
        DocumentBuilder docBuilder = docBFactory.newDocumentBuilder();
        //create a document
        Document xmlDoc= docBuilder.newDocument();
        return xmlDoc;
    }

}