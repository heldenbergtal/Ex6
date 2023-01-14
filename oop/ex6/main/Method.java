package oop.ex6.main;

import java.util.HashMap;
import java.util.Map;

public class Method {
    Map<String, Variable> parameters = new HashMap<>();
    String returnType;


    public Method(Map<String, Variable> parameters, String returnType) {
        this.returnType = returnType;
        this.parameters.putAll(parameters);
    }
}
