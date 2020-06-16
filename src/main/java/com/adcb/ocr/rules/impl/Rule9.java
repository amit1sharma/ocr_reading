package com.adcb.ocr.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.image.Image;
import com.adcb.ocr.rules.Rule;

/**
 * from this rule onwards all .net rules are implemented
 * 
 */
@Component
@Order(9)
public class Rule9 implements Rule {
    public Rule9() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
        		.greyScale()
        		.setThreshold(80,  255, THRESH_BINARY)
        		.gradient()
        		.horizontalConnect(19, 1)
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
