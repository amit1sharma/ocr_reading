package com.adcb.ocr.engine.rules.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;

/**
 ** rule for rotation
 */
@Component
@Order(25)
public class Rule25 implements Rule {
    public Rule25() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
      //  .denoise().save()
      //  .greyScale().save()
      //  .setAdaptiveThreshold(255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10).save()
        .getImagePath();

        return result;
    }

}