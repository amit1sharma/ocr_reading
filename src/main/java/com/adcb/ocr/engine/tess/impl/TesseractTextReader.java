package com.adcb.ocr.engine.tess.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.adcb.ocr.engine.tess.TextReader;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;


@Service
@Profile("!dev")
public class TesseractTextReader implements TextReader{
	

	/*@Value("${tesseract.tessdata.path}")
	private String tessDataPath;
	
	@Value("${tesseract.tessdata.lang}")
	private String tessDataLang;*/

	Tesseract tesseract1 = new Tesseract();
	@Autowired
	public TesseractTextReader(@Value("${tesseract.tessdata.path}")String tessDataPath, @Value("${tesseract.tessdata.lang}")String tessDataLang){
		tesseract1.setDatapath(tessDataPath);
        tesseract1.setLanguage(tessDataLang);
	};

	@Override
	public String readText(String filePath) throws TesseractException {        
		return tesseract1.doOCR(new File(filePath));
	}

}
