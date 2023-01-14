package oop.ex6.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class SecondState implements ReadingState{

    private BufferedReader bufferedReader;
    private Map<String, Method> methodsMap;
    private Map<String, Variable> globalVariablesMap;
    private Map<Integer, String> methodsLines;
    private int lineCounter = 1;

    public SecondState(BufferedReader bufferedReader, Map<String, Method> methodsMap, 
                       Map<String, Variable> globalVariablesMap, Map<Integer, String> methodsLines) {
        this.bufferedReader = bufferedReader;
        this.methodsMap = methodsMap;
        this.globalVariablesMap = globalVariablesMap;
        this.methodsLines = methodsLines;
    }

    @Override
    public void pass() throws IOException {
        String line;
//        while ((line = bufferedReader.readLine()) != null) {
//            for (:
//                 ) {
//
//            }
        }

    }

