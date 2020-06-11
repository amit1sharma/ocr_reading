package com.amt.ocr.controller;

import com.amt.ocr.rules.Rule;
import com.amt.ocr.rules.impl.*;
import org.opencv.core.Core;

import java.util.ArrayList;
import java.util.List;

public class MainClass {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static String SRC_PATH = "/home/yamraaj/Pictures/samples/remaining/";
    public static String imageName = "passport_02.jpg";
    public static String docType = "pp";

    List<Rule> rules = new ArrayList<>();

    {
        Rule r1 = new Rule1(SRC_PATH,imageName, docType);
        Rule r2 = new Rule2(SRC_PATH,imageName, docType);
        Rule r3 = new Rule3(SRC_PATH,imageName, docType);
        Rule r4 = new Rule4(SRC_PATH,imageName, docType);
        Rule r5 = new Rule5(SRC_PATH,imageName, docType);
//        rules.add(r1);
//        rules.add(r2);
//        rules.add(r3);
//        rules.add(r4);
        rules.add(r5);
    }

    public static void main(String[] args) throws Exception {
        MainClass mainClass = new MainClass();
        for(Rule r : mainClass.rules){
            try {
                r.applyRule();
                System.out.println("hi");
            }catch(Exception e){
                System.out.println("unable to find in "+r.getClass().getDeclaredClasses());
            }
        }
    }
}
