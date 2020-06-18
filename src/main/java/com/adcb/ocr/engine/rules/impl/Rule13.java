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
@Order(13)
public class Rule13 implements Rule {
    public Rule13() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
                .greyScale()
                .deskew()
                .setThreshold(10, 255, THRESH_BINARY)
                .gradient().save()
                .horizontalConnect(19, 1)
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
