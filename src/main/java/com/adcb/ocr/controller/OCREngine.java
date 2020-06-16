package com.adcb.ocr.controller;

import com.adcb.ocr.rules.Rule;
import com.adcb.ocr.rules.impl.*;
import com.adcb.ocr.tess.TextReader;
import com.adcb.ocr.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class OCREngine {
	
	
	@Autowired
	private TextReader textReader;

    @Autowired
    private List<Rule> rules;

    /**
     * this is for local testing only
     */
    private void loadRules(){

        Rule r1 = new Rule1();
        Rule r2 = new Rule2();
        Rule r3 = new Rule3();
        Rule r4 = new Rule4();
        Rule r5 = new Rule5();
        rules.add(r1);
        rules.add(r2);
        rules.add(r3);
        rules.add(r4);
        rules.add(r5);
    }
    /**
     * this is for local testing only
     */
    public static void main(String[] args) throws Exception {
        String SRC_PATH = "/home/yamraaj/Pictures/samples/remaining/";
        String imageName = "passport_02.jpg";
        String docType = "pp";
        OCREngine mainClass = new OCREngine();
        mainClass.loadRules();
        mainClass.startEngine(SRC_PATH, imageName, docType);
    }
    public String startEngine(String srcPath, String imageName, String docType){
    	String result = "";
    	for(Rule r : rules){
            try {
                String roiImagePath = r.applyRule(srcPath, imageName, docType);
                result = textReader.readText(roiImagePath + File.separator + imageName);
                result = Utilities.removeSpace(result);
                if(!Utilities.validateMRZString(result, docType)){
                	result="";
                	continue;
                }
                System.out.println("Found in rule : "+r.getClass().getName());
                break;
            }catch(Exception e){
//            	e.printStackTrace();
                System.out.println("unable to find in "+r.getClass().getName());
            }
//            break;
        }
    	System.out.println(result);
    	return result;
    }
    
}
