import javax.swing.filechooser.FileNameExtensionFilter;
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
    private static void readAndWriteAllJackFiles(ArrayList <File> jackList){
        for (int j=0;j<jackList.size();j++) {
            String outputFileName = setOutputFileName(jackList.get(j));
            try (FileReader jackFile = new FileReader(jackList.get(j));// define the BufferReader and BufferWriter
                 BufferedReader reader = new BufferedReader(jackFile);
                 FileWriter vmFile = new FileWriter(outputFileName);
                 BufferedWriter writer = new BufferedWriter(vmFile))
            {
                //read the file and parse it
                readAndParse(reader,jackList.get(j).getName());
                // todo need to write to xml after the tokenizer and the complication
//     TODO           for (int i = 0; i < Tokenizer.getXmlLines().size(); i++) {
//                    writer.write(Tokenizer.getXmLines().get(i) + NEW_LINE); //write the token output
//                }
//                compileAllFiles(Tokenizer.getXmlLines(),outputFileName);
            } catch (IOException e) {
                System.out.println(BAD_FILE);
            }
        }
    }

    /**
     * compile all files and write a xml file for each and every of them
     * @param listOfTokens the Tokenizer output
     * @param location the location of the output
     */
    private static void compileAllFiles(ArrayList<String> listOfTokens,String location){
            // change the output location
            String outputFileName=location.substring(0,location.length()-FIVE);
            outputFileName+=XML;

            try(FileWriter vmFile = new FileWriter(outputFileName);
                BufferedWriter writer = new BufferedWriter(vmFile)){
                // compile the Tokenizer output
                CompilationEngine compiler = new CompilationEngine(listOfTokens);
                compiler.compileClass();
                //write to a xml file
                
//                for(int i=0;i<compiler.getXmlLines.size();i++){	TODO
//                    writer.write(listOfTokens.get(i) + NEW_LINE);
//                }
            }
            catch (IOException e)
            {
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
     * read and parse a specific file
     * @param reader a bufferReader
     * @param className the current class name
     * @throws IOException
     */
    private static void readAndParse(BufferedReader reader, String className) throws IOException {
        Tokenizer tokenizer = new Tokenizer(); //define a new tokenizer
        
        try{
        	tokenizer.readCodeFile(reader);
        }
        catch(Exception e){
        	System.out.println("Fail...:(");
        }
        
        
//        String text;
//        while ((text = reader.readLine()) != null) // add the lines to the container
//        {
//            tokenizer.getJackLines().add(text);
//        }
//        tokenizer.tokenizeJackFile(className); // parse the vm text
    }




}