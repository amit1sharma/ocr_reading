package com.amt.ocr.rules.impl;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.image.Image;
import com.amt.ocr.rules.Rule;
import com.amt.ocr.utils.Utils;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.opencv.imgproc.Imgproc.*;

/**
 * this rule is for pictures that are scanned or click and cropped properly but with not proper quality.
 * including 2 page height and single page height
 */
public class Rule2 implements Rule {
    public Rule2() {
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

    public Rule2(String imagePath, String imageName, String docType) {
        this.imagePath = imagePath;
        this.imageName = imageName;
        this.docType = docType;
    }

    private String imagePath;
    private String imageName;
    private String docType;

    @Override
    public void applyRule() throws Exception {
        File f = new File(imagePath);
        if(f.isDirectory()) {
            File f1 = new File(imagePath+File.separator+"rule2");
            if(f1.exists()){
                Utils.deleteDirectoryStream(Paths.get(imagePath+File.separator+"rule2"));
            }
            f1.mkdirs();
            for (String imageName : f.list()) {
                if(new File(imagePath + File.separator + imageName).isDirectory()){
                    continue;
                }
                try {
                    Image i = new Image(docType, imagePath, imageName);
                    String roiImagePath = i
                            .greyScale().save()
                            .gaussianBlur(new Size(11, 11)).save()
                            .gradient().save()
                            .setThreshold(0,255, THRESH_BINARY|THRESH_OTSU).save()
                            .horizontalConnect(5, 10).save()
                            .erodeImage(new Size(5, 5)).save()
                            .findContourRectangles()
                            .getRoiImagePath();

                    String str = tesseract.doOCR(new File(roiImagePath + File.separator + imageName));
                    System.out.println(str);
                    final MrzRecord record = MrzParser.parse(str);
                    System.out.println(record.toString());
                    Files.move(Paths.get(imagePath + imageName), Paths.get(imagePath + File.separator + "rule2" + File.separator + imageName));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
