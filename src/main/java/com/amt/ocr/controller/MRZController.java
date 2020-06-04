package com.amt.ocr.controller;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.processor.RecognizeText;
//import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imread;

@Controller
@RequestMapping("/mrz")
public class MRZController {

    @RequestMapping("/read")
    @ResponseBody
//    public String readMRZ(@RequestParam("imagePath") String imagePath){
    public String readMRZ(){
        Tesseract tesseract = new Tesseract();
        try {
            tesseract.setDatapath("/home/yamraaj/Pictures/tessdata/tessdata-master/");
//            tesseract.setConfigs();
            tesseract.setLanguage("ocrb");
//            tesseract.setPageSegMode();
            try {
                String text = tesseract.doOCR(new File("/home/yamraaj/Pictures/image.jpg"));
                System.out.print(text);
                final MrzRecord record = MrzParser.parse(text);
                System.out.println(record.toMrz());
            }catch(Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }
    @RequestMapping("/loadImage")
    @ResponseBody
    public String loadImage(){
        System.out.println(System.getProperty("java.library.path"));
        long start = System.currentTimeMillis();
        // Read image
        Mat origin = imread(RecognizeText.SRC_PATH + "image.png");
//        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));

//        String result = new RecognizeText().extractTextFromImage(origin);
//        System.out.println(result);imread_1

        System.out.println("Time");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("Done");
        return "time taken is : "+ (System.currentTimeMillis() - start);
    }

    public static void main(String[] args){
        MRZController o = new MRZController();
        o.loadImage();
    }
}
