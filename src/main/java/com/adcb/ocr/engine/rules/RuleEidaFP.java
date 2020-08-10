package com.adcb.ocr.engine.rules;

public interface RuleEidaFP {
    String applyRule(String imagePath, String imageName, String docType) throws Exception;
}