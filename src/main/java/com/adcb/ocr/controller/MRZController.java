package com.adcb.ocr.controller;

import com.adcb.ocr.engine.OCREngine;
import com.adcb.ocr.pdf.ProcessPdf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mrz")
public class MRZController {

    @Autowired
    private OCREngine ocrEngine;
    @Autowired
    private ProcessPdf processPdf;

    @RequestMapping("/read")
    @ResponseBody
    public String readMRZ(@RequestParam("imagePath") String imagePath, @RequestParam("imageName") String imageName,
                          @RequestParam("docType") String docType){
        return ocrEngine.startEngine(imagePath, imageName, docType);
    }

    @RequestMapping("/readpdf")
    @ResponseBody
    public String readPdf(@RequestParam("imagePath") String imagePath, @RequestParam("imageName") String imageName,
                          @RequestParam("docType") String docType) throws Exception{
        return processPdf.process(imagePath, imageName, docType);
    }
}
