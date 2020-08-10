package com.adcb.ocr.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adcb.ocr.engine.OCREngine;
import com.adcb.ocr.pdf.ProcessPdf;

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

	@RequestMapping("/readAll")
	@ResponseBody
	public String readMRZPP(@RequestParam("imagePath") String imagePath, @RequestParam("imageName") String imageName,
			@RequestParam("docType") String docType){
		File folder = new File (imagePath);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory())
				continue;
			imageName = fileEntry.getName();
			long startTime = System.currentTimeMillis();
			System.out.println( ocrEngine.startEngine(imagePath, imageName, docType));
			long endTime = System.currentTimeMillis();
			System.out.println(" Time taken for image  :: " + imageName + " :: "+((endTime-startTime)/1000));
		}
		return "Success";
	}
}