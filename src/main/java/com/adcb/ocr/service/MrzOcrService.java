package com.adcb.ocr.service;

import com.adcb.ocr.model.ExtractMRZResponse;

public interface MrzOcrService {

	public ExtractMRZResponse getTextFromImage(String cid, String docType, String binaryImageFP, String binaryImageBP);
}
