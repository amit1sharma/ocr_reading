package com.adcb.ocr.rules.impl;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opencv.core.Size;
import org.springframework.stereotype.Component;

import com.adcb.ocr.image.Image;
import com.adcb.ocr.rules.Rule;
import com.adcb.ocr.util.Utilities;

import net.sourceforge.tess4j.Tesseract;

/**
 * from this rule onwards all .net rules are implemented
 * 
 */
@Component
public class Rule21 implements Rule {
    public Rule21() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Image i = new Image(docType, imagePath, imageName);
        result = i
        		.horizontalConnect(19, 1)
                .greyScale().save()
                .setThreshold(80, 255, THRESH_BINARY)
                .gradient().save()
                .horizontalConnect(19, 1)
                .findContourRectangles()
                .getRoiImagePath();
        return result;
    }

}
