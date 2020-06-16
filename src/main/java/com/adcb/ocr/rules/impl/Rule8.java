package com.adcb.ocr.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.image.Image;
import com.adcb.ocr.rules.Rule;

/**
 * from this rule onwards all .net rules are implemented
 * 
 */
@Component
@Order(8)
public class Rule8 implements Rule {
    public Rule8() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
        		.greyScale()
        		.gradient()
        		.setThreshold(0,  255, THRESH_BINARY|THRESH_OTSU)
        		.horizontalConnect(19, 1)
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
