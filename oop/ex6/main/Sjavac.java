package oop.ex6.main;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Sjavac {
    static final String PATH_REGEX = "..sjava";
    static final String VARS_TYPES = "int|double|boolean|String|char";
    static final String METHOD_TYPES = "void";
    static final String NAME_REGEX = "(?:[a-zA-Z][0-9a-zA-Z_]*)|_[0-9a-zA-Z_]+";
    static final String METHOD_NAME_REGEX = "[a-zA-z][0-9a-zA-Z_]*";
    static final String VAR_REGEX = String.format("//s%s//s", VARS_TYPES);
    public static final String IO_ERROR_NUMBER = "2";
    public static final String ILLEGAL_CODE_NUMBER = "1";
    public static final String LEGAL_NUMBER = "0";
    public static final String FILE_ENDING_ERROR = "file should end with .sjava";

    public static final int NUMBER_OF_FILE_READING = 2;
    private static ReadingState firstReading;
    private static ReadingState secondReading;
    private static final Map<String, Method> methodsMap = new HashMap<>();
    private static final Map<String, Variable> globalVariablesMap = new HashMap<>();


    public static void readFile(String pathName) {
        try {
            if(!pathName.matches(pathName)){
                throw new IOException(FILE_ENDING_ERROR);
            }
        } catch (IOException e){
            System.out.println(IO_ERROR_NUMBER);
            System.err.println(e.getMessage());
        }
        try (FileReader fileReader = new FileReader(pathName);
             BufferedReader bufferedReader = new BufferedReader(fileReader);) {
            ReadingState readingState;
            int readingCounter = 0;
            firstReading = new FirstState(bufferedReader, methodsMap, globalVariablesMap);
            secondReading = new SecondState(bufferedReader);
            ReadingState[] readingStates = {firstReading, secondReading};
            while (readingCounter < NUMBER_OF_FILE_READING) {
                readingState = readingStates[readingCounter];
                readingState.pass();
                bufferedReader.reset();
                ++readingCounter;
            }
        } catch (IOException e) {
            System.out.println(IO_ERROR_NUMBER);
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String path = "supplied_material/tests/test501.sjava";
        readFile(path);
        System.out.println("");
    }
}
