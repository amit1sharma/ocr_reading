package com.amt.ocr.controller;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.processor.RecognizeText;
//import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
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

/*    @RequestMapping("/read")
    @ResponseBody
//    public String readMRZ(@RequestParam("imagePath") String imagePath){
    public String readMRZ(){
        Tesseract tesseract = new Tesseract();
        try {
            tesseract.setDatapath("/home/yamraaj/Pictures/tessdata/tessdata-master/");
//            tesseract.setConfigs();
            tesseract.setLanguage("eng");
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
    }*/

    @RequestMapping("/loadImage")
    @ResponseBody
    public String loadImage(){
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("Start recognize text from image");
        long start = System.currentTimeMillis();
        // Read image
        Mat origin = imread(RecognizeText.SRC_PATH + "image.png");

        String result = new RecognizeText().extractTextFromImage(origin);
        System.out.println(result);

        System.out.println("Time");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("Done");
        return "time taken is : "+ (System.currentTimeMillis() - start);
    }
}
