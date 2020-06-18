package com.adcb.ocr.endoint;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.adcb.ocr.db.service.OcrRequestResponseService;
import com.adcb.ocr.model.ExtractMRZRequest;
import com.adcb.ocr.model.ExtractMRZResponse;
import com.adcb.ocr.service.MrzOcrService;
import com.adcb.ocr.util.Utilities;

@Endpoint
public class WebServiceEndpoint {
	public static final String NAMESPACE_URI_EXTRACTMRZ = "http://adcb.com/ocr/providers/ExtractMRZ";
	public static final String DEFAULT_RESPONSE_MESSAGE = "API Zone Internal Error";

	@Autowired 
	private MrzOcrService mrzOcrService;
	
	@Autowired
	private OcrRequestResponseService ocrRequestService;

	@PayloadRoot(namespace = NAMESPACE_URI_EXTRACTMRZ, localPart = "ExtractMRZRequest")
	@ResponsePayload
	public ExtractMRZResponse extractMRZ(@RequestPayload ExtractMRZRequest extractMRZRequest) {
		String requestString = Utilities.getXml(extractMRZRequest);
		Date requestTime = new Date();
		ExtractMRZResponse response =  mrzOcrService.getTextFromImage(extractMRZRequest.getCid(),extractMRZRequest.getDocType(),extractMRZRequest.getImgEncodedFP(),extractMRZRequest.getImgEncodedBP());
		String resposneString = Utilities.getXml(response);
		try{
			ocrRequestService.saveLog(requestString, resposneString, requestTime, new Date());
		}catch(Exception e){
			e.printStackTrace();
		}
		return response;
	}
}
