package com.adcb.ocr.engine.rules.impl;

import static org.opencv.imgproc.Imgproc.MORPH_BLACKHAT;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;

/**
 * this rule is for standard pictures that are scanned or click and cropped properly.
 * including 2 page height and single page height
 * this should cover double image aligned vertically
 */
@Component
@Order(1)
public class Rule1 implements Rule {
    public Rule1() {
    }

    private Mat rectangleKernel, squareKernel;
    private Size rectSize;
    private Size sqSize;
    public void selectKernels(Mat mat){
        int rectKWidth , rectKHeight, sqKWidth, sqKHeight;

        if(mat.width()>=900){
            rectKWidth=50;
            rectKHeight = 30;
            sqKWidth=50;
            sqKHeight = 50;
        } else {
            rectKWidth=30;
            rectKHeight = 20;
            sqKWidth=30;
            sqKHeight = 30;
        }
        rectSize = new Size(rectKWidth, rectKHeight);
        sqSize = new Size(sqKWidth, sqKHeight);
        rectangleKernel = getStructuringElement(MORPH_RECT, new Size(rectKWidth, rectKHeight));
        squareKernel = getStructuringElement(MORPH_RECT, new Size(sqKWidth, sqKHeight));
    }

    @Override
    public String applyRule(String imagePath, String imageName, String docType) throws Exception {
    	String result = "";
        Mat mat = Imgcodecs.imread(imagePath + File.separator + imageName);
        selectKernels(mat);
        Image i = new Image(docType, imagePath, imageName);
        result = i
//        		.denoise().save()
                .greyScale().save()
//                .removeWaterMark().save()
                .gaussianBlur(new Size(11, 11)).save()
                .morphology(MORPH_BLACKHAT, rectSize).save()
                .highlightEveryThing().save() // sobel
                .morphology(MORPH_CLOSE, rectSize).save() // done
                .morphology(MORPH_CLOSE, sqSize).save()
                .erodeImage(rectSize).save()
                .findContourRectangles()
                .getRoiImagePath();

        return result;
    }

}
