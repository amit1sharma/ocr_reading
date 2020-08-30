package com.adcb.ocr.engine.rules.impl;

import org.opencv.imgproc.Imgproc;
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
@Order(4)
public class RuleEidaFP4 implements RuleEidaFP {
    public RuleEidaFP4() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
        		//.denoise().save()
                .greyScale().save()
                .setAdaptiveThreshold(255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10).save()
                .getImagePath();

        return result;
    }

}
