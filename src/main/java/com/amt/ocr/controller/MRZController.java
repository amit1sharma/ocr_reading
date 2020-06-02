package com.amt.ocr.controller;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.processor.RecognizeText;
import net.sourceforge.tess4j.Tesseract;
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

    @RequestMapping("/read")
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
    }

    @RequestMapping("/read1")
    @ResponseBody
    public String readMRZOpenCV(){
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("Start recognize text from image");
        long start = System.currentTimeMillis();
//        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        // Read image
        Mat origin = imread(RecognizeText.SRC_PATH + "image.png");

        String result = new RecognizeText().extractTextFromImage(origin);
        System.out.println(result);

        System.out.println("Time");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("Done");
        return "time taken is : "+ (System.currentTimeMillis() - start);
    }


    /*private boolean saveImage(RenderedImage bufferedImage,
                              String formatName,
                              File localOutputFile,
                              int dpi) throws IOException {
        boolean success;

        if (formatName.equalsIgnoreCase("png"))
        {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

            final String pngMetadataFormatName = "javax_imageio_png_1.0";

            // Convert dpi (dots per inch) to dots per meter
            final double metersToInches = 39.3701;
            int dotsPerMeter = (int) Math.round(dpi * metersToInches);

            IIOMetadataNode pHYs_node = new IIOMetadataNode("pHYs");
            pHYs_node.setAttribute("pixelsPerUnitXAxis", Integer.toString(dotsPerMeter));
            pHYs_node.setAttribute("pixelsPerUnitYAxis", Integer.toString(dotsPerMeter));
            pHYs_node.setAttribute("unitSpecifier", "meter");

            IIOMetadataNode root = new IIOMetadataNode(pngMetadataFormatName);
            root.appendChild(pHYs_node);

            metadata.mergeTree(pngMetadataFormatName, root);

            writer.setOutput(ImageIO.createImageOutputStream(localOutputFile));
            writer.write(metadata, new IIOImage(bufferedImage, null, metadata), writeParam);
            writer.dispose();

            success = true;
        }
        else
        {
            success = ImageIO.write(bufferedImage, formatName, localOutputFile);
        }

        return success;
    }*/

}
