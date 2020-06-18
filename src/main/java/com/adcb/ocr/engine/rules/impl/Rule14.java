package com.adcb.ocr.engine.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

import org.opencv.core.Size;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;

/**
 * from this rule onwards all .net rules are implemented
 * 
 */
@Component
@Order(14)
public class Rule14 implements Rule {
    public Rule14() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
                .greyScale().save()
                .gaussianBlur(new Size(11, 11)).save()
                .gradient().save()
                .setThreshold(80, 255, THRESH_BINARY)
                .horizontalConnect(19, 1)
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
