package com.amt.ocr.rules.impl;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.image.Image;
import com.amt.ocr.rules.Rule;
import com.amt.ocr.utils.Utils;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.opencv.imgproc.Imgproc.*;

/**
 * this rule is for standard pictures that are scanned or click and cropped properly.
 * including 2 page height and single page height
 * this should cover double image aligned vertically
 */
public class Rule1 implements Rule {
    public Rule1() {
    }

    public static String TESS_DATA = "/home/yamraaj/Pictures/tessdata/tessdata-master/";
    // Create tess obj
    public static Tesseract tesseract = new Tesseract();

    // Load OPENCV
    static {
        tesseract.setDatapath(TESS_DATA);
//        tesseract.setLanguage("ocrb_int");
        tesseract.setLanguage("ocrb");
    }

    public Rule1(String imagePath, String imageName, String docType) {
        this.imagePath = imagePath;
        this.imageName = imageName;
        this.docType = docType;
    }

    private String imagePath;
    private String imageName;
    private String docType;

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
    public void applyRule() throws Exception {

        File f = new File(imagePath);
        if(f.isDirectory()){
            File f1 = new File(imagePath+File.separator+"rule1");
            if(f1.exists()){
                Utils.deleteDirectoryStream(Paths.get(imagePath+File.separator+"rule1"));
            }
            f1.mkdirs();
            for(String imageName : f.list()){
                if(new File(imagePath + File.separator + imageName).isDirectory()){
                    continue;
                }
                try {
                    Mat mat = Imgcodecs.imread(imagePath + File.separator + imageName);
                    selectKernels(mat);
                    Image i = new Image(docType, imagePath, imageName);
                    String roiImagePath = i
                            .greyScale().save()
                            .gaussianBlur(new Size(11, 11)).save()
                            .morphology(MORPH_BLACKHAT, rectSize).save()
                            .highlightEveryThing().save() // sobel
                            .morphology(MORPH_CLOSE, rectSize).save() // done
                            .morphology(MORPH_CLOSE, sqSize).save()
                            .erodeImage(rectSize).save()
                            .findContourRectangles()
                            .getRoiImagePath();

                    String str = tesseract.doOCR(new File(roiImagePath + File.separator + imageName));
                    System.out.println(str);
                    final MrzRecord record = MrzParser.parse(str);
                    System.out.println(record.toString());
                    Files.move(Paths.get(imagePath + imageName), Paths.get(imagePath + File.separator + "rule1" + File.separator + imageName));
                } catch ( Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
