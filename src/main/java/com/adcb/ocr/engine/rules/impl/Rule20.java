package com.adcb.ocr.engine.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

import org.opencv.core.Size;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;

/**
 ** rule for perspective image
 */
@Component
@Order(20)
public class Rule20 implements Rule {
    public Rule20() {
    }


    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        i
                .getPerspectiveImage().save()
                .greyScale().save()
                .gaussianBlur(new Size(11, 11)).save()
                .gradient().save()
                .setThreshold(0, 255, THRESH_BINARY|THRESH_OTSU).save()
                .horizontalConnect(3, 3).save()
                .erodeImage(new Size(5,5)).save()
                .findContourRectangles()
                .getRoiImagePath();


        return result;
    }

}
