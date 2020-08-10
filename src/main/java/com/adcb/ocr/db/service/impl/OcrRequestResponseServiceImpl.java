package com.adcb.ocr.db.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adcb.ocr.db.dao.OCRRequestResponseRepository;
import com.adcb.ocr.db.model.OcrRequestResponseModel;
import com.adcb.ocr.db.service.OcrRequestResponseService;

@Service
public class OcrRequestResponseServiceImpl implements OcrRequestResponseService{
	
	@Autowired
	private OCRRequestResponseRepository ocrRequestResponseRepository;
	
	@Override
	public void saveLog(String requestData, String responseData, Date requestTime, Date responseTime){
		OcrRequestResponseModel model = new OcrRequestResponseModel();
		model.setRequestData(requestData);
		model.setRequestTimeStamp(requestTime);
		model.setResponsedata(responseData);
		model.setResponseTimeStamp(responseTime);
		ocrRequestResponseRepository.save(model);
	}
}
