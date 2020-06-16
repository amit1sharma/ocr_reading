package com.adcb.ocr.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opencv.core.Size;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.image.Image;
import com.adcb.ocr.rules.Rule;
import com.adcb.ocr.util.Utilities;

import net.sourceforge.tess4j.Tesseract;

/**
 * this rule is for pictures that are scanned or click and cropped properly but with not proper quality.
 * including 2 page height and single page height
 */
@Component
@Order(3)
public class Rule3 implements Rule {
    public Rule3() {
    }


    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
                .greyScale().save()
                .gaussianBlur(new Size(11, 11)).save()
                .gradient().save()
                .setThreshold(0,255, THRESH_BINARY|THRESH_OTSU).save()
                .horizontalConnect(3, 6).save()
                .erodeImage(new Size(5, 10)).save()
                .findContourRectangles()
                .getRoiImagePath();
        return result;
    }

}
