package com.adcb.ocr.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.adcb.ocr.engine.rules.impl.*;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.rules.Rule;
import com.adcb.ocr.engine.tess.TextReader;
import com.adcb.ocr.util.Utilities;

@Component
public class OCREngine {
	
	
	@Autowired
	private TextReader textReader;

    @Autowired
    private List<Rule> rules;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * this is for local testing only
     */
    private void loadRules(){

        rules = new ArrayList<>();
        Rule r1 = new Rule1();
        Rule r2 = new Rule2();
        Rule r3 = new Rule3();
        Rule r4 = new Rule4();
        Rule r5 = new Rule5();
        Rule r20 = new Rule20();
//        rules.add(r1);
//        rules.add(r2);
//        rules.add(r3);
//        rules.add(r4);
//        rules.add(r5);
        rules.add(r20);
    }
    /**
     * this is for local testing only
     */
    public static void main(String[] args) throws Exception {
        String SRC_PATH = "/home/yamraaj/Pictures/";
        String imageName = "abc.jpg";
        String docType = "eid";
        OCREngine mainClass = new OCREngine();
        mainClass.loadRules();
        mainClass.startEngine(SRC_PATH, imageName, docType);
    }
    public String startEngine(String srcPath, String imageName, String docType){
    	String text = "";
    	for(Rule r : rules){
    		text="";
            try {
                String roiImagePath = r.applyRule(srcPath, imageName, docType);
                text = textReader.readText(roiImagePath + File.separator + imageName);
                text = Utilities.removeSpace(text);
                if(!Utilities.validateMRZString(text, docType)){
                	text="";
                    System.out.println("unable to find in "+r.getClass().getName());
                	continue;
                }
                System.out.println("Found in rule : "+r.getClass().getName());
                break;
            }catch(Exception e){
            	text="";
//            	e.printStackTrace();
                System.out.println("unable to find in "+r.getClass().getName());
            }
//            break;
        }
    	System.out.println(text);
    	return text;
    }
    
}