package com.adcb.ocr.engine.rules;

/**
 * Implement classes with names using sequence that is used already ie Rile1, Rule2, Rule3
 * These are the sequence in which rules will be applied
 * @author tp21037220
 *
 */
public interface Rule {
    String applyRule(String imagePath, String imageName, String docType) throws Exception;
}