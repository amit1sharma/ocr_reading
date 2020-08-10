package com.adcb.ocr.engine.rules.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.RuleEidaFP;

/**
 * this rule is for standard pictures that are scanned or click and cropped properly.
 * including 2 page height and single page height
 * this should cover double image aligned vertically
 */
@Component
@Order(1)
public class RuleEidaFP1 implements RuleEidaFP {
    public RuleEidaFP1() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
                .greyScale().save()
                .getImagePath();

        return result;
    }

}
