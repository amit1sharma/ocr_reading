package com.amt.ocr.rules.impl;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.image.Image;
import com.amt.ocr.rules.Rule;
import com.amt.ocr.utils.Utils;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Size;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

/**
 * this rule is for pictures that are scanned or click and cropped properly but with not proper quality.
 * including 2 page height and single page height
 */
public class Rule3 implements Rule {
    public Rule3() {
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

    public Rule3(String imagePath, String imageName, String docType) {
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
            File f1 = new File(imagePath + File.separator + "rule3");
            if (f1.exists()) {
                Utils.deleteDirectoryStream(Paths.get(imagePath + File.separator + "rule3"));
            }
            f1.mkdirs();
            for (String imageName : f.list()) {
                if (new File(imagePath + File.separator + imageName).isDirectory()) {
                    continue;
                }
                try {
                    Image i = new Image(docType, imagePath, imageName);
                    String roiImagePath = i
                            .greyScale().save()
                            .gaussianBlur(new Size(11, 11)).save()
                            .gradient().save()
                            .setThreshold(0,255, THRESH_BINARY|THRESH_OTSU).save()
                            .horizontalConnect(3, 6).save()
                            .erodeImage(new Size(5, 10)).save()
                            .findContourRectangles()
                            .getRoiImagePath();

                    String str = "";
                    long stime = System.currentTimeMillis();
                    str = tesseract.doOCR(new File(roiImagePath + File.separator + imageName));
                    long etime = System.currentTimeMillis();
                    System.out.println(etime - stime);
                    System.out.println(str);
                    final MrzRecord record = MrzParser.parse(str);
                    System.out.println(record.toString());
                    Files.move(Paths.get(imagePath + imageName), Paths.get(imagePath + File.separator + "rule3" + File.separator + imageName));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
