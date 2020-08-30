package com.adcb.ocr.engine.rules.impl;
import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.opencv.imgproc.Imgproc.*;

/**
 * this rule is for standard pictures that are scanned or click and cropped properly.
 * including 2 page height and single page height
 * this should cover double image aligned vertically
 */
@Component
@Order(8)
public class Rule8 implements Rule {
    public Rule8() {
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
//        Mat mat = Imgcodecs.imread(imagePath + File.separator + imageName);
        Image i = new Image(docType, imagePath, imageName);
        result = i
                .greyScale().save()

//                .setAdaptiveThreshold(255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10).save()
                .removeWaterMark(false).save()
                .gaussianBlur(new Size(11, 11)).save()
                .gradient().save()
                .setThreshold(0,255, THRESH_BINARY|THRESH_OTSU).save()
                .horizontalConnect(4, 8).save()
                .erodeImage(new Size(5, 5)).save()
                .findContourRectangles()
                .getRoiImagePath(false);

        return result;
    }
}