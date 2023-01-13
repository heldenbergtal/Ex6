package oop.ex6.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstState implements ReadingState {
    private static final String ILLEGAL_CODE_MSG = "invalid syntax in line %d";
    private static final String VARIABLE_DEFINED_ERROR_MSG = "variable name is already defined in line %d";
    private static final String UNINITIALIZED_VAR_ERROR_MSG = "variable has not have been initialized in " +
            "line %d";
    private static final String INCOMPATIBLE_TYPES_MSG = "incompatible types in line %d";
    private static final String ASSIGNMENT_BEFORE_DECLARATION_MSG = "cannot find symbol in line %d";
    private static final String VALUE_TO_FINAL_VARIABLE_MSG = "cannot assign a value to final variable in " +
            "line %d";
    private static final int INITIALIZATION_VAR_FINAL_INDEX = 1;
    private static final int INITIALIZATION_VAR_TYPE_INDEX = 2;
    private static final int INITIALIZATION_VAR_NAME_INDEX = 3;
    private static final int INITIALIZATION_VAR_ASSIGNMENT_INDEX = 4;
    private static final int DECLARATION_VAR_TYPE_INDEX = 1;
    private static final int DECLARATION_VAR_NAME_INDEX = 2;
    private static final int ASSIGNMENT_VAR_NAME_INDEX = 1;
    private static final int ASSIGNMENT_VAR_ASSIGNMENT_INDEX = 2;
    private static final int GLOBAL_VAR_TYPE = 0;
    private static final int GLOBAL_VAR_ASSIGNED = 1;
    private static final int GLOBAL_VAR_FINAL = 2;
    public static final String TRUE_KEY = "true";
    public static final String FALSE_KEY = "false";
    public static final char END_LINE_MARK = ';';
    public static final String VARIABLES_SEPERATOR = ",";
    public static final String SPACE = " ";

    public static final String INT_KEY = "int";
    public static final String CHAR_KEY = "char";
    public static final String STRING_KEY = "String";
    public static final String DOUBLE_KEY = "double";
    public static final String BOOLEAN_KEY = "boolean";
    static final String INT_ASSIGNMENT = "[+-]?[0-9]+";
    static final String CHAR_ASSIGNMENT = "'.'";
    static final String STRING_ASSIGNMENT = "\".*\"";
    static final String DOUBLE_ASSIGNMENT = "[+-]?\\d*.\\d+|\\d+.\\d*|" + INT_ASSIGNMENT;
    static final String BOOLEAN_ASSIGNMENT = DOUBLE_ASSIGNMENT + "|true|false";
    public static final String LEGAL_ASSIGNMENT = String.format("%s|%s|%s|%s|%s", INT_ASSIGNMENT,
            CHAR_ASSIGNMENT, STRING_ASSIGNMENT, DOUBLE_ASSIGNMENT, BOOLEAN_ASSIGNMENT);
    private static final String VARIABLE_DECLARATION_REGEX = String.format("\\s*(%s)\\s*(%s)\\s*",
            Sjavac.VARS_TYPES, Sjavac.NAME_REGEX);
    private static final String METHOD_REGEX = String.format("\\s*(void)\\s*(%s)\\s*\\(([^)]*)\\)\\s*\\{\\s*",
            Sjavac.METHOD_NAME_REGEX);
    private static final String VARIABLE_ASSIGNMENT_REGEX = String.format("\\s*(%s)\\s*=\\s*(%s|%s)\\s*(," +
                    "\\s*(%s)\\s*=\\s*(%s|%s)\\s*)*;",
            Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT, Sjavac.NAME_REGEX, Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT, Sjavac.NAME_REGEX);
    private static final String VARIABLE_INITIALIZATION_REGEX = String.format("\\s*(final)?\\s*(%s)\\s*(%s)" +
            "\\s*=\\s*(%s|%s)\\s*", Sjavac.VARS_TYPES, Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT, Sjavac.NAME_REGEX);

    private static final String VARIABLE_REGEX = String.format("(?:%s)|(?:%s)(,\\s*(?:%s)|(?:%s)\\s*)*;",
            VARIABLE_DECLARATION_REGEX, VARIABLE_INITIALIZATION_REGEX, Sjavac.NAME_REGEX, VARIABLE_ASSIGNMENT_REGEX);
    private final Pattern variableInitializationPattern = Pattern.compile(VARIABLE_INITIALIZATION_REGEX);
    private final Pattern variableAssignmentPattern = Pattern.compile(VARIABLE_ASSIGNMENT_REGEX);
    private final Pattern variablePattern = Pattern.compile(VARIABLE_REGEX);
    private final Pattern variableDeclarationPattern = Pattern.compile(VARIABLE_DECLARATION_REGEX);
    private final Pattern methodPrefixPattern = Pattern.compile(METHOD_REGEX);


    public static final Map<String, String> TYPE_REGEX_MAP = new HashMap<>() {{
        put(INT_KEY, INT_ASSIGNMENT);
        put(CHAR_KEY, CHAR_ASSIGNMENT);
        put(STRING_KEY, STRING_ASSIGNMENT);
        put(BOOLEAN_KEY, BOOLEAN_ASSIGNMENT);
        put(DOUBLE_KEY, DOUBLE_ASSIGNMENT);
    }};
    private final BufferedReader bufferedReader;
    private final Map<String, String[]> methodsMap;
    private final Map<String, String[]> globalVariablesMap;

    private int lineCounter = 0;


    public FirstState(BufferedReader bufferedReader, Map<String, String[]> methodsMap,
                      Map<String, String[]> variablesMap) {
        this.bufferedReader = bufferedReader;
        this.methodsMap = methodsMap;
        this.globalVariablesMap = variablesMap;
    }

    @Override
    public void pass() throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcherVariableAssignment = variableAssignmentPattern.matcher(line);
            Matcher matcherVariable = variablePattern.matcher(line);
            Matcher matcherMethod = methodPrefixPattern.matcher(line);

            if (matcherVariable.matches()) {
                handleDeclarationOrAssignment(line);
            } else if (matcherVariableAssignment.matches()) {
                handleAssignments(line);
            } else if (matcherMethod.matches()) {
                readMethodDeclaration(matcherMethod);
            } else {
                throw new IOException(String.format(ILLEGAL_CODE_MSG, lineCounter));
            }
            ++lineCounter;
        }
    }

    private void handleAssignments(String line) throws IOException {
        line = line.substring(0, line.lastIndexOf(END_LINE_MARK));
        String[] arr = line.split(VARIABLES_SEPERATOR);
        for (String s : arr) {
            Matcher matcher = variableAssignmentPattern.matcher(s + END_LINE_MARK);
            if (matcher.matches()) {
                readVariableAssignment(matcher);
            }
        }
    }

    private void handleDeclarationOrAssignment(String line) throws IOException {
        line = line.substring(0, line.lastIndexOf(END_LINE_MARK));
        String[] arr = line.split(VARIABLES_SEPERATOR);
        String[] firstCommand = arr[0].split(SPACE);
        String type = firstCommand[0].equals("final") ? firstCommand[1] : firstCommand[0];
        for (int i = 0; i < arr.length; ++i) {
            String s = i > 0 ? type + arr[i] : arr[i];
            Matcher matcherVariableDeclaration = variableDeclarationPattern.matcher(s);
            Matcher matcherVariableInitialization = variableInitializationPattern.matcher(s);
            if (matcherVariableDeclaration.matches()) {
                readVariableDeclaration(matcherVariableDeclaration);
            }
            if (matcherVariableInitialization.matches()) {
                readVariableInitialization(matcherVariableInitialization);
            }
        }
    }


    private void readVariableDeclaration(Matcher matcher) throws IOException {
        checkIfVariableWasDeclared(matcher, DECLARATION_VAR_NAME_INDEX);
        String[] characteristics = new String[]{matcher.group(DECLARATION_VAR_TYPE_INDEX), FALSE_KEY, FALSE_KEY};
        globalVariablesMap.put(matcher.group(DECLARATION_VAR_NAME_INDEX), characteristics);
    }

    private void readVariableAssignment(Matcher matcher) throws IOException {
        checkIfDeclaredBeforeAssignment(matcher, ASSIGNMENT_VAR_NAME_INDEX);
        String varName = matcher.group(ASSIGNMENT_VAR_NAME_INDEX);
        String type = globalVariablesMap.get(varName)[GLOBAL_VAR_TYPE];
        checkAssignmentToFinal(matcher, ASSIGNMENT_VAR_NAME_INDEX);
        checkIfTypeMatches(type, matcher.group(ASSIGNMENT_VAR_ASSIGNMENT_INDEX));
        globalVariablesMap.get(matcher.group(ASSIGNMENT_VAR_NAME_INDEX))[GLOBAL_VAR_ASSIGNED] = TRUE_KEY;
    }

    private void readVariableInitialization(Matcher matcher) throws IOException {
        checkIfVariableWasDeclared(matcher, INITIALIZATION_VAR_NAME_INDEX);
        String varAssignment = matcher.group(INITIALIZATION_VAR_ASSIGNMENT_INDEX);
        checkIfTypeMatches(matcher.group(INITIALIZATION_VAR_TYPE_INDEX), varAssignment);
        String isFinal = matcher.group(INITIALIZATION_VAR_FINAL_INDEX) != null ? TRUE_KEY : FALSE_KEY;
        String[] characteristics = new String[]{matcher.group(INITIALIZATION_VAR_TYPE_INDEX), TRUE_KEY, isFinal};
        globalVariablesMap.put(matcher.group(INITIALIZATION_VAR_NAME_INDEX), characteristics);
    }

    private void readMethodDeclaration(Matcher matcher) {


    }

    private void checkIfVariableWasDeclared(Matcher matcher, int nameIndex) throws IOException {
        if (globalVariablesMap.containsKey(matcher.group(nameIndex))) {
            throw new IOException(String.format(VARIABLE_DEFINED_ERROR_MSG, lineCounter));
        }
    }

    private void checkIfTypeMatches(String varType, String assignedVar) throws IOException {
        if (assignedVar.matches(TYPE_REGEX_MAP.get(varType))) {
            return;
        }
        checkIfAssigned(assignedVar);
        if (globalVariablesMap.get(assignedVar)[GLOBAL_VAR_TYPE].matches(varType)) {
            return;
        }
        throw new IOException(String.format(INCOMPATIBLE_TYPES_MSG, lineCounter));
    }

    private void checkIfAssigned(String assignedVar) throws IOException {
        if (!globalVariablesMap.containsKey(assignedVar)) {
            throw new IOException(String.format(ASSIGNMENT_BEFORE_DECLARATION_MSG, lineCounter));
        }
        if (!globalVariablesMap.get(assignedVar)[GLOBAL_VAR_ASSIGNED].equals(TRUE_KEY)) {
            throw new IOException(String.format(UNINITIALIZED_VAR_ERROR_MSG, lineCounter));
        }
    }

    private void checkIfDeclaredBeforeAssignment(Matcher matcher, int nameIndex) throws IOException {
        if (!globalVariablesMap.containsKey(matcher.group(nameIndex))) {
            throw new IOException(String.format(ASSIGNMENT_BEFORE_DECLARATION_MSG, lineCounter));
        }
    }

    private void checkAssignmentToFinal(Matcher matcher, int nameIndex) throws IOException {
        if (!globalVariablesMap.get(matcher.group(nameIndex))[GLOBAL_VAR_FINAL].equals(TRUE_KEY)) {
            throw new IOException(String.format(VALUE_TO_FINAL_VARIABLE_MSG, lineCounter));
        }
    }

}


//        if (line.matches(String.format("\\s*" + INT_KEY + "\\s*" + NAME_REGEX + "\\s*=\\s*" + "\\s*" +
//        TYPE_REGEX_MAP.get(INT_KEY) + "\\s*;"))) {
//        System.out.println("hello");
