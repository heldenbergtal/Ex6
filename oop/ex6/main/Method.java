package oop.ex6.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Method {
    private final Map<String, Variable> parameters = new HashMap<>();
    private final String returnType;
    private final int startMethodLine;
    private final int endMethodLine;


    public Method(Map<String, Variable> parameters, String returnType, int startMethodLine, int endMethodLine) {
        this.returnType = returnType;
        this.parameters.putAll(parameters);
        this.startMethodLine = startMethodLine;
        this.endMethodLine = endMethodLine;
    }

    public void addLocalVariable(String varName, Variable variable){
        parameters.put(varName, variable);
    }

    public Variable getLocalVariable(String name){
        if (!parameters.containsKey(name)) {
            return null; // TODO - "throw new Exception "cannot find symbol"
        }
        return parameters.get(name);
    }



}
