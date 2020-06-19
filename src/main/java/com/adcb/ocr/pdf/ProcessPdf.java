package com.adcb.ocr.pdf;

import com.adcb.ocr.engine.OCREngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessPdf {

    @Autowired
    private OCREngine ocrEngine;

    public String process(String path, String fileName, String docType) throws Exception{
        SaveImagesFromPdf saveImagesFromPdf = new SaveImagesFromPdf();
        saveImagesFromPdf.extractImage(path, fileName);
        String imageNameWithoutExt = fileName.split("\\.")[0];
        String imageName = imageNameWithoutExt+".png";
        return ocrEngine.startEngine(path, imageName, docType);
    }
}
