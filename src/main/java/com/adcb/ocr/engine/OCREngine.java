package com.adcb.ocr.engine;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adcb.ocr.constants.OcrConstants;
import com.adcb.ocr.engine.rules.Rule;
import com.adcb.ocr.engine.rules.RuleEidaFP;
import com.adcb.ocr.engine.rules.impl.Rule1;
import com.adcb.ocr.engine.rules.impl.Rule2;
import com.adcb.ocr.engine.rules.impl.Rule3;
import com.adcb.ocr.engine.rules.impl.Rule4;
import com.adcb.ocr.engine.rules.impl.Rule5;
import com.adcb.ocr.engine.tess.TextReader;
import com.adcb.ocr.util.Utilities;

@Component
public class OCREngine {
	
	@Value("${delete.after.processing}")
	private boolean deleteAfterProcessing;


	@Autowired
	private TextReader textReader;

	@Autowired
	private List<Rule> rules;
	@Autowired
	private List<RuleEidaFP> rulesEidaFP;
	private static final Logger APPLOGGER = LoggerFactory.getLogger(OCREngine.class);

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
		String SRC_PATH = "/home/tempfolder/Pictures/samples/remaining/";
		String imageName = "passport_02.jpg";
		String docType = "pp";
		OCREngine mainClass = new OCREngine();
		mainClass.loadRules();
		mainClass.startEngine(SRC_PATH, imageName, docType);
	}
	public String startEngine(String srcPath, String imageName, String docType){

		String text = "";
		if(docType.equalsIgnoreCase(OcrConstants.EIDFP)){
			int totalRules = rulesEidaFP.size();
			int counter=0;
			for (RuleEidaFP ruleEidaFP : rulesEidaFP ){
				try {
					counter++;
					String roiImagePath = ruleEidaFP.applyRule(srcPath, imageName, docType);
					text = textReader.readText(roiImagePath + File.separator + imageName);
					text = Utilities.getEidFP(text);
					if(text.contains("FAIL") && counter < totalRules){
						continue;
					} else if (text.contains("FAIL")) {
						text = text.replaceAll("FAIL", "");
					}
					if(text.equals("")){
						APPLOGGER.info("Unable to find in {} ",ruleEidaFP.getClass().getName());
						continue;
					}
					APPLOGGER.info("Found in rule : {} ", ruleEidaFP.getClass().getName());
					break;
				} catch (Exception e) {
					APPLOGGER.error("Error in reading the images by tesseract.");
					return "";
				}
			}
			APPLOGGER.debug(text);
		}
		else 
		{ 
			for(Rule r : rules){
				text="";
				try {
					APPLOGGER.info("processing rule : {} ", r.getClass().getName());
					String roiImagePath = r.applyRule(srcPath, imageName, docType);
					text = textReader.readText(roiImagePath + File.separator + imageName);
					text = Utilities.removeSpace(text);
					if(!Utilities.validateMRZString(text, docType)){
						if(r.getClass().getName().contains("Rule22")
								|| r.getClass().getName().contains("Rule23") 
								|| r.getClass().getName().contains("Rule24")
								|| r.getClass().getName().contains("Rule25")){
							String textTemp = Utilities.extractMrzString(text,docType);
							if(!textTemp.equals("")){
								text = textTemp;
								APPLOGGER.info("Found in rule : {} ", r.getClass().getName());
								break;
							}
						}
						text="";
						APPLOGGER.info("unable to find in {} ",r.getClass().getName());
						continue;
					}
					APPLOGGER.info("Found in rule : {} ", r.getClass().getName());
					break;
				}catch(Exception e){
					text="";
					APPLOGGER.info("unable to find in {} ",r.getClass().getName(),e);
				}
			}
			APPLOGGER.debug(text);
			try {
				if(deleteAfterProcessing){
					APPLOGGER.info("Processing complete deleting folder.");
					Utilities.deleteDirectoryStream(new File(srcPath).toPath());
					APPLOGGER.info("deleted folder : " + srcPath);
				}
			} catch (IOException e) {
				APPLOGGER.error("Error Occured during deletion of images {}" , e.getMessage());
			}
		}

		return text;
	}

}