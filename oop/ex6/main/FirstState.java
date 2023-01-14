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
    static final char END_LINE_MARK = ';';
    static final String VARIABLES_SEPARATOR = ",";
    static final String SPACE = " ";
    static final String INT_KEY = "int";
    static final String CHAR_KEY = "char";
    static final String STRING_KEY = "String";
    static final String DOUBLE_KEY = "double";
    static final String BOOLEAN_KEY = "boolean";
    static final String INT_ASSIGNMENT = "[+-]?[0-9]+";
    static final String CHAR_ASSIGNMENT = "'.'";
    static final String STRING_ASSIGNMENT = "\".*\"";
    static final String DOUBLE_ASSIGNMENT = "[+-]?\\d*.\\d+|\\d+.\\d*|" + INT_ASSIGNMENT;
    static final String BOOLEAN_ASSIGNMENT = DOUBLE_ASSIGNMENT + "|true|false";
    public static final String LEGAL_ASSIGNMENT = String.format("%s|%s|%s|%s|%s", INT_ASSIGNMENT,
            CHAR_ASSIGNMENT, STRING_ASSIGNMENT, DOUBLE_ASSIGNMENT, BOOLEAN_ASSIGNMENT);
    static final String COMMENT_EMPTY_LINE_REGEX = "//.*|\\s*";
    private static final String VARIABLE_DECLARATION_REGEX = String.format("\\s*(%s)\\s*(%s)\\s*",
            Sjavac.VARS_TYPES, Sjavac.NAME_REGEX);
    private static final String METHOD_REGEX = String.format("\\s*(void)\\s*(%s)\\s*\\(([^)]*)\\)\\s*\\{\\s*",
            Sjavac.METHOD_NAME_REGEX);
    private static final String VARIABLE_ASSIGNMENT_REGEX = String.format("\\s*(%s)\\s*=\\s*(%s|%s)\\s*(," +
                    "\\s*(%s)\\s*=\\s*(%s|%s)\\s*)*;",
            Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT, Sjavac.NAME_REGEX, Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT, Sjavac.NAME_REGEX);
    private static final String VARIABLE_INITIALIZATION_REGEX = String.format("\\s*(final)?\\s*(%s)\\s*(%s)" +
            "\\s*=\\s*(%s|%s)\\s*", Sjavac.VARS_TYPES, Sjavac.NAME_REGEX, LEGAL_ASSIGNMENT, Sjavac.NAME_REGEX);
    private static final String PARAMETERS_REGEX = String.format("(?:\\s*(final)?%s)", VARIABLE_DECLARATION_REGEX);
    private static final String VARIABLE_REGEX = String.format("((?:%s)|(?:%s))(,\\s*(?:%s)|(?:%s)\\s*)*;",
            VARIABLE_DECLARATION_REGEX, VARIABLE_INITIALIZATION_REGEX, Sjavac.NAME_REGEX, VARIABLE_ASSIGNMENT_REGEX);
    public static final int METHOD_NAME_INDEX = 2;
    public static final String OVERLOAD_METHOD_MSG = "overload is not allowed in line %d";
    public static final int METHOD_PARAMETERS_INDEX = 3;
    public static final int METHOD_RETURN_TYPE_INDEX = 1;
    public static final int PARAMETERS_FINAL_INDEX = 1;
    public static final int PARAMETERS_TYPE_INDEX = 2;
    public static final String INCORRECT_PARAMETER_MSG = "incorrect parameter in line %d";
    private static final int PARAMETERS_NAME_INDEX = 3;
    public static final String PARAMETER_ALREADY_DEFINED_MSG = "variable a is already defined in method " +
            "name, in line %d";
    public static final String FINAL_KEY = "final";
    public static final int FIRST_SLOT_INDEX = 0;
    public static final String END_OF_FILE_MSG = "reached end of file";
    public static final char OPEN_CURLY_BRACKETS = '{';
    public static final char CLOSE_CURLY_BRACKETS = '}';
    public static final int NOT_FOUND = -1;
    public static final int INIT_NUMBER_OPEN_BRACKETS_IN_METHOD = 1;
    public static final int INIT_NUMBER_CLOSE_BRACKETS_IN_METHOD = 0;
    private final Pattern variableInitializationPattern = Pattern.compile(VARIABLE_INITIALIZATION_REGEX);
    private final Pattern variableAssignmentPattern = Pattern.compile(VARIABLE_ASSIGNMENT_REGEX);
    private final Pattern variablePattern = Pattern.compile(VARIABLE_REGEX);
    private final Pattern variableDeclarationPattern = Pattern.compile(VARIABLE_DECLARATION_REGEX);
    private final Pattern methodPrefixPattern = Pattern.compile(METHOD_REGEX);
    private final Pattern methodParametersPattern = Pattern.compile(PARAMETERS_REGEX);
    private final Pattern CommentOrEmptyLinePattern = Pattern.compile(COMMENT_EMPTY_LINE_REGEX);


    public static final Map<String, String> TYPE_REGEX_MAP = new HashMap<>() {{
        put(INT_KEY, INT_ASSIGNMENT);
        put(CHAR_KEY, CHAR_ASSIGNMENT);
        put(STRING_KEY, STRING_ASSIGNMENT);
        put(BOOLEAN_KEY, BOOLEAN_ASSIGNMENT);
        put(DOUBLE_KEY, DOUBLE_ASSIGNMENT);
    }};
    private final BufferedReader bufferedReader;
    private final Map<String, Method> methodsMap;
    private final Map<String, Variable> globalVariablesMap;
    private int lineCounter = 1;


    public FirstState(BufferedReader bufferedReader, Map<String, Method> methodsMap,
                      Map<String, Variable> variablesMap) {
        this.bufferedReader = bufferedReader;
        this.methodsMap = methodsMap;
        this.globalVariablesMap = variablesMap;
    }

    @Override
    public void pass() throws IllegalCodeException, IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcherVariableAssignment = variableAssignmentPattern.matcher(line);
            Matcher matcherVariable = variablePattern.matcher(line);
            Matcher matcherMethod = methodPrefixPattern.matcher(line);
            Matcher matcherCommentOrEmptyLine = CommentOrEmptyLinePattern.matcher(line);
            if (matcherVariable.matches()) {
                handleDeclarationOrAssignment(line);
            } else if (matcherVariableAssignment.matches()) {
                handleAssignments(line);
            } else if (matcherMethod.matches()) {
                readMethodDeclaration(matcherMethod);
            } else if (matcherCommentOrEmptyLine.matches()) {
                ++lineCounter;
                continue;
            } else {
                throw new IllegalCodeException(String.format(ILLEGAL_CODE_MSG, lineCounter));
            }
            ++lineCounter;
        }
    }

    private void getToEndOfMethod() throws IllegalCodeException {
        int openBracketsCounter = INIT_NUMBER_OPEN_BRACKETS_IN_METHOD,
                closeBracketsCounter = INIT_NUMBER_CLOSE_BRACKETS_IN_METHOD;
        String currLine;
        while ((currLine = bufferedReader.readLine()) != null){
            lineCounter++;
            int openBracketsIndex = currLine.lastIndexOf(OPEN_CURLY_BRACKETS),
                    closeBracketsIndex = currLine.lastIndexOf(CLOSE_CURLY_BRACKETS);
            if (openBracketsIndex != NOT_FOUND && openBracketsIndex == currLine.length()- 1) {
                openBracketsCounter+= 1;
            } else if (closeBracketsIndex != NOT_FOUND && closeBracketsIndex == currLine.length()- 1) {
                closeBracketsCounter += 1;
            }
            if (openBracketsCounter == closeBracketsCounter) {
                break;
            }
        }
        if (currLine == null) {
            throw new IllegalCodeException(END_OF_FILE_MSG);
        }
    }// TODO - check return in second pass?


    private void handleAssignments(String line) throws IllegalCodeException {
        line = line.substring(0, line.lastIndexOf(END_LINE_MARK));
        String[] arr = line.split(VARIABLES_SEPARATOR);
        for (String s : arr) {
            Matcher matcher = variableAssignmentPattern.matcher(s + END_LINE_MARK);
            if (matcher.matches()) {
                readVariableAssignment(matcher);
            }
        }
    }

    private void handleDeclarationOrAssignment(String line) throws IllegalCodeException {
        line = line.substring(0, line.lastIndexOf(END_LINE_MARK));
        String[] arr = line.split(VARIABLES_SEPARATOR);
        String[] firstCommand = arr[FIRST_SLOT_INDEX].split(SPACE);
        String type = firstCommand[FIRST_SLOT_INDEX].equals(FINAL_KEY) ? firstCommand[1] : firstCommand[FIRST_SLOT_INDEX];
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


    private void readVariableDeclaration(Matcher matcher) throws IllegalCodeException {
        checkIfVariableWasDeclared(matcher, DECLARATION_VAR_NAME_INDEX);
        Variable variable = new Variable(matcher.group(DECLARATION_VAR_TYPE_INDEX), false, false);
        globalVariablesMap.put(matcher.group(DECLARATION_VAR_NAME_INDEX), variable);
    }

    private void readVariableAssignment(Matcher matcher) throws IllegalCodeException {
        checkIfDeclaredBeforeAssignment(matcher, ASSIGNMENT_VAR_NAME_INDEX);
        String varName = matcher.group(ASSIGNMENT_VAR_NAME_INDEX);
        String type = globalVariablesMap.get(varName).getType();
        checkAssignmentToFinal(matcher, ASSIGNMENT_VAR_NAME_INDEX);
        checkIfTypeMatches(type, matcher.group(ASSIGNMENT_VAR_ASSIGNMENT_INDEX));
        globalVariablesMap.get(matcher.group(ASSIGNMENT_VAR_NAME_INDEX)).setIsAssigned(true);

    }

    private void readVariableInitialization(Matcher matcher) throws IllegalCodeException {
        checkIfVariableWasDeclared(matcher, INITIALIZATION_VAR_NAME_INDEX);
        String varAssignment = matcher.group(INITIALIZATION_VAR_ASSIGNMENT_INDEX);
        checkIfTypeMatches(matcher.group(INITIALIZATION_VAR_TYPE_INDEX), varAssignment);
        boolean isFinal = matcher.group(INITIALIZATION_VAR_FINAL_INDEX) != null;
        Variable variable = new Variable(matcher.group(INITIALIZATION_VAR_TYPE_INDEX), true, isFinal);
        globalVariablesMap.put(matcher.group(INITIALIZATION_VAR_NAME_INDEX), variable);
    }

    private void readMethodDeclaration(Matcher matcher) throws IllegalCodeException {
        if (methodsMap.containsKey(matcher.group(METHOD_NAME_INDEX))) {
            throw new IllegalCodeException(String.format(OVERLOAD_METHOD_MSG, lineCounter));
        }
        int startMethodLine = lineCounter;
        getToEndOfMethod();
        int endMethodLine = lineCounter;
        Map<String, Variable> variables = ReadVariables(matcher.group(METHOD_PARAMETERS_INDEX));
        Method method = new Method(variables, matcher.group(METHOD_RETURN_TYPE_INDEX), startMethodLine, endMethodLine);
        methodsMap.put(matcher.group(METHOD_NAME_INDEX), method);
    }

    private Map<String, Variable> ReadVariables(String initVariables) throws IllegalCodeException {
        String[] arr = initVariables.isEmpty() ? new String[]{} : initVariables.split(VARIABLES_SEPARATOR);
        Map<String, Variable> variables = new HashMap<>();
        for (var param : arr) {
            Matcher matcher = methodParametersPattern.matcher(param);
            if (matcher.matches()) {
                checkParameterAlreadyDefined(variables, matcher.group(PARAMETERS_NAME_INDEX));
                boolean isFinal = matcher.group(PARAMETERS_FINAL_INDEX) != null;
                String type = matcher.group(PARAMETERS_TYPE_INDEX);
                Variable variable = new Variable(type, true, isFinal);
                variables.put(matcher.group(PARAMETERS_NAME_INDEX), variable);
            } else {
                throw new IllegalCodeException(String.format(INCORRECT_PARAMETER_MSG, lineCounter));
            }
        }
        return variables;
    }

    private void checkParameterAlreadyDefined(Map<String, Variable> variables, String paramName) throws IllegalCodeException {
        if (variables.containsKey(paramName)) {
            throw new IllegalCodeException(String.format(PARAMETER_ALREADY_DEFINED_MSG, lineCounter));
        }
    }

    private void checkIfVariableWasDeclared(Matcher matcher, int nameIndex) throws IllegalCodeException {
        if (globalVariablesMap.containsKey(matcher.group(nameIndex))) {
            throw new IllegalCodeException(String.format(VARIABLE_DEFINED_ERROR_MSG, lineCounter));
        }
    }

    private void checkIfTypeMatches(String varType, String assignedVar) throws IllegalCodeException {
        if (assignedVar.matches(TYPE_REGEX_MAP.get(varType))) {
            return;
        }
        checkIfAssigned(assignedVar);
        if (globalVariablesMap.get(assignedVar).getType().matches(varType)) {
            return;
        }
        throw new IllegalCodeException(String.format(INCOMPATIBLE_TYPES_MSG, lineCounter));
    }

    private void checkIfAssigned(String assignedVar) throws IllegalCodeException {
        if (!globalVariablesMap.containsKey(assignedVar)) {
            throw new IllegalCodeException(String.format(ASSIGNMENT_BEFORE_DECLARATION_MSG, lineCounter));
        }
        if (!globalVariablesMap.get(assignedVar).getIsAssigned()) {
            throw new IllegalCodeException(String.format(UNINITIALIZED_VAR_ERROR_MSG, lineCounter));
        }
    }

    private void checkIfDeclaredBeforeAssignment(Matcher matcher, int nameIndex) throws IllegalCodeException {
        if (!globalVariablesMap.containsKey(matcher.group(nameIndex))) {
            throw new IllegalCodeException(String.format(ASSIGNMENT_BEFORE_DECLARATION_MSG, lineCounter));
        }
    }

    private void checkAssignmentToFinal(Matcher matcher, int nameIndex) throws IllegalCodeException {
        if (!globalVariablesMap.get(matcher.group(nameIndex)).getIsFinal()) {
            throw new IllegalCodeException(String.format(VALUE_TO_FINAL_VARIABLE_MSG, lineCounter));
        }
    }

}


