package oop.ex6.main;

public class Method {
    Variable[] parameters;
    String returnType;


    public Method(Variable[] parameters, String returnType) {
        this.returnType = returnType;
        this.parameters = new Variable[parameters.length];
        System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
    }
}
