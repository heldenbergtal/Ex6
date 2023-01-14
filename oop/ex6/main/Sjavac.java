package oop.ex6.main;

import java.io.*;
import java.util.*;

public class Sjavac {
    static final String PATH_REGEX = ".*\\.sjava";
    static final String VARS_TYPES = "int|double|boolean|String|char";
    static final String METHOD_TYPES = "void";
    static final String NAME_REGEX = "(?:[a-zA-Z][0-9a-zA-Z_]*)|_[0-9a-zA-Z_]+";
    static final String METHOD_NAME_REGEX = "[a-zA-z][0-9a-zA-Z_]*";
    static final String VAR_REGEX = String.format("//s%s//s", VARS_TYPES);
    public static final String IO_ERROR_NUMBER = "2";
    public static final String ILLEGAL_CODE_NUMBER = "1";
    public static final String LEGAL_CODE_NUMBER = "0";
    public static final String FILE_ENDING_ERROR = "file should end with .sjava";

    public static final int NUMBER_OF_FILE_READING = 2;
    private static ReadingState firstReading;
    private static ReadingState secondReading;
    private static final Map<String, Method> methodsMap = new HashMap<>();
    private static final Map<String, Variable> globalVariablesMap = new HashMap<>();
    private static final Map<Integer, String> methodsLines = new TreeMap<>();


    public static void readFile(String pathName) {
        if (!checkPathNameEndsWithsjava(pathName))
            return;
        try (FileReader fileReader = new FileReader(pathName);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            bufferedReader.mark(1000);  // TODO think of a better option
            ReadingState readingState;
            int readingCounter = 0;
            firstReading = new FirstState(bufferedReader, methodsMap, globalVariablesMap, methodsLines);
            secondReading = new SecondState(bufferedReader, methodsMap, globalVariablesMap, methodsLines);
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
            return;
        } catch (IllegalCodeException e) {
            System.out.println(ILLEGAL_CODE_NUMBER);
            System.err.println(e.getMessage());
            return;
        }
        System.out.println(LEGAL_CODE_NUMBER);
    }

    private static boolean checkPathNameEndsWithsjava(String pathName) {
        try {
            if(!pathName.matches(PATH_REGEX)){
                throw new IOException(FILE_ENDING_ERROR);
            }
            return true;
        } catch (IOException e){
            System.out.println(IO_ERROR_NUMBER);
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        String path = "supplied_material/tests/test501.sjava";
        readFile(path);
        System.out.println("");
    }
}
