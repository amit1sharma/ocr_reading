package com.adcb.ocr.engine.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

import org.opencv.core.Size;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;

/**
 * this rule is for pictures that are scanned or click and cropped properly but with not proper quality.
 * including 2 page height and single page height
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
//        		.denoise().save()
                .greyScale().save()
                .removeWaterMark(false).save()
                .gaussianBlur(new Size(11, 11)).save()
                .gradient().save()
                .setThreshold(0,255, THRESH_BINARY|THRESH_OTSU).save()
                .horizontalConnect(4, 10).save()
//                .erodeImage(new Size(5, 5)).save()
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
