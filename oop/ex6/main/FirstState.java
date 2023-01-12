package oop.ex6.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstState implements ReadingState {
    public static final String ILLEGAL_CODE_MSG = "invalid syntax in line %d";
    public static final String VARIABLE_DEFINED_ERROR_MSG = "variable name is already defined";
    public static final String INCOMPATIBLE_TYPES_MSG = "incompatible types";
    public static final String ASSINGMENT_BEFORE_DECLARATION_MSG = "cannot find symbol";
    public static final String VALUE_TO_FINAL_VARIABLE_MSG = "cannot assign a value to final variable";
    private final BufferedReader bufferedReader;
    private final Map<String, String[]> methodsMap;
    private final Map<String, String[]> globalVariablesMap;
    private final Pattern variableInitializationPattern = Pattern.compile(variableInitialization);
    private final Pattern variableAssignmentPattern = Pattern.compile(variableAssignment);
    private final Pattern variableDeclarationPattern = Pattern.compile(variableDeclaration);
    private final Pattern methodPrefixPattern = Pattern.compile(method);
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
    public static final Map<String, String> TYPE_REGEX_MAP = new HashMap<>() {{
        put(INT_KEY, INT_ASSIGNMENT);
        put(CHAR_KEY, CHAR_ASSIGNMENT);
        put(STRING_KEY, STRING_ASSIGNMENT);
        put(BOOLEAN_KEY, BOOLEAN_ASSIGNMENT);
        put(DOUBLE_KEY, DOUBLE_ASSIGNMENT);
    }};
//    private static final String variableInitialization ="\\s*(final)?\\s*(int)\\s*(a)\\s*=\\s*(-5)\\s*;";
    private static final String variableInitialization = String.format("\\s*(final)?\\s*(%s)\\s*(%s)" +
                "\\s*=\\s*(%s)\\s*;", Sjavac.VARS_TYPES, Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT);
    private static final String variableAssignment = String.format("\\s*(%s)\\s*=\\s*(%s)\\s*;",
            Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT);
    private static final String variableDeclaration = String.format("\\s*(%s)\\s*(%s)\\s*;",
            Sjavac.VARS_TYPES, Sjavac.NAME_REGEX);
    private static final String method = String.format("\\s*(void)\\s*(%s)\\s*\\(([^)]*)\\)\\s*\\{\\s*",
            Sjavac.METHOD_NAME_REGEX);

    public FirstState(BufferedReader bufferedReader, Map<String, String[]> methodsMap,
                      Map<String, String[]> variablesMap) {
        this.bufferedReader = bufferedReader;
        this.methodsMap = methodsMap;
        this.globalVariablesMap = variablesMap;
    }

    @Override
    public void pass() throws IOException {
        String line;
        int lineCounter = 0;
        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcherVariableInitialization = variableInitializationPattern.matcher(line);
            Matcher matcherVariableAssignment = variableAssignmentPattern.matcher(line);
            Matcher matcherVariableDeclaration = variableDeclarationPattern.matcher(line);
            Matcher matcherMethod = methodPrefixPattern.matcher(line);
            if (matcherVariableAssignment.matches()) {
                readVariableAssignment(matcherVariableAssignment);
            } else if (matcherVariableInitialization.matches()) {
                readVariableInitialization(matcherVariableInitialization);
            } else if (matcherVariableDeclaration.matches()) {
                readVariableDeclaration(matcherVariableDeclaration);
            } else if (matcherVariableAssignment.matches()) {
                readMethodDeclaration(matcherMethod);
            } else {
                throw new IOException(String.format(ILLEGAL_CODE_MSG, lineCounter));
            }
            ++lineCounter;
        }
    }

    private void readVariableDeclaration(Matcher matcher) throws IOException {
        if (globalVariablesMap.containsKey(matcher.group(2))) {
            throw new IOException(VARIABLE_DEFINED_ERROR_MSG);
        }
        String[] characteristics = new String [] {matcher.group(1), "false", "false"};
        globalVariablesMap.put(matcher.group(2), characteristics);
    }

    private void readVariableInitialization(Matcher matcher) throws IOException {  //TODO add assignment to
        // exist variable
        if (globalVariablesMap.containsKey(matcher.group(3))) {
            throw new IOException(VARIABLE_DEFINED_ERROR_MSG);
        }
        String variableAssignment = matcher.group(4);
        if (!variableAssignment.matches(TYPE_REGEX_MAP.get(matcher.group(2)))) {
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
            System.out.println(matcher.group(4));
            System.out.println(matcher.group(5));
            throw new IOException(INCOMPATIBLE_TYPES_MSG);
        }
        String isFinal = matcher.group(1) != null ? "true" : "false";
        String[] characteristics = new String [] {matcher.group(1), "true", isFinal};
        globalVariablesMap.put(matcher.group(3), characteristics);
    }

    private void readVariableAssignment(Matcher matcher) throws IOException {   //TODO add assignment to
        // exist variable
        if (!globalVariablesMap.containsKey(matcher.group(1))){
            throw new IOException(ASSINGMENT_BEFORE_DECLARATION_MSG);
        }
        if (!globalVariablesMap.get(matcher.group(1))[2].equals("true"))
            throw new IOException(VALUE_TO_FINAL_VARIABLE_MSG);
        String varName = matcher.group(1);
        String type = globalVariablesMap.get(varName)[0];
        String validAssignment = TYPE_REGEX_MAP.get(type);
        if (!matcher.group(2).matches(TYPE_REGEX_MAP.get(validAssignment))){
            throw new IOException(INCOMPATIBLE_TYPES_MSG);
        }
        globalVariablesMap.get(matcher.group(1))[1] = "true";
    }

    private void readMethodDeclaration(Matcher matcher) {
    }
}


//        if (line.matches(String.format("\\s*" + INT_KEY + "\\s*" + NAME_REGEX + "\\s*=\\s*" + "\\s*" +
//        TYPE_REGEX_MAP.get(INT_KEY) + "\\s*;"))) {
//        System.out.println("hello");
