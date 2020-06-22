package com.adcb.ocr.engine.rules.impl;

import com.adcb.ocr.engine.image.Image;
import com.adcb.ocr.engine.rules.Rule;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;

/**
 ** rule for rotation
 */
@Component
@Order(20)
public class Rule20 extends Rule1 implements Rule {
    public Rule20() {
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
        Image i = new Image(docType, imagePath, imageName);
        i
                .getPerspectiveImage().save();
        Mat originalMat = i.get_original_image();
        selectKernels(originalMat);
        result = i
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
