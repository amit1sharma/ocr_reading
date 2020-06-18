package com.adcb.ocr.engine.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;

/**
 * from this rule onwards all .net rules are implemented
 * 
 */
@Component
@Order(16)
public class Rule16 implements Rule {
    public Rule16() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
        		.horizontalConnect(10, 1)
                .greyScale()
                .setThreshold(80, 255, THRESH_BINARY)
                .gradient()
                .horizontalConnect(10, 1)
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
