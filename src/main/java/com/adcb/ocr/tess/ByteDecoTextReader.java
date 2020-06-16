package com.adcb.ocr.tess;

import static org.bytedeco.leptonica.global.lept.pixDestroy;
import static org.bytedeco.leptonica.global.lept.pixRead;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class ByteDecoTextReader implements TextReader{

	@Value("${tesseract.tessdata.path}")
	private String tessDataPath;
	
	@Value("${tesseract.tessdata.lang}")
	private String tessDataLang;

	@Override
	public String readText(String filePath) throws Exception {
		TessBaseAPI tessBaseAPI = new TessBaseAPI();
		if(tessBaseAPI.Init(tessDataPath,tessDataLang)!=0){
			System.err.println("could not initiate bytedeco text reader");
			System.exit(1);
		}
		PIX pix = pixRead(filePath);
		tessBaseAPI.SetImage(pix);
		BytePointer outText = tessBaseAPI.GetUTF8Text();
		String result =  outText.getString();
		
		tessBaseAPI.End();
        outText.deallocate();
        pixDestroy(pix);
        return result;
	}

}
