package com.adcb.ocr.endoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.adcb.ocr.model.ExtractMRZRequest;
import com.adcb.ocr.model.ExtractMRZResponse;
import com.adcb.ocr.service.MrzOcrService;

@Endpoint
public class WebServiceEndpoint {
	public static final String NAMESPACE_URI_EXTRACTMRZ = "http://adcb.com/ocr/providers/ExtractMRZ";
	public static final String DEFAULT_RESPONSE_MESSAGE = "API Zone Internal Error";

	@Autowired 
	private MrzOcrService mrzOcrService;

	@PayloadRoot(namespace = NAMESPACE_URI_EXTRACTMRZ, localPart = "ExtractMRZRequest")
	@ResponsePayload
	public ExtractMRZResponse extractMRZ(@RequestPayload ExtractMRZRequest extractMRZRequest) {
		return mrzOcrService.getTextFromImage(extractMRZRequest.getCid(),extractMRZRequest.getDocType(),extractMRZRequest.getImgEncodedFP(),extractMRZRequest.getImgEncodedBP());
		/*
		ExtractMRZResponse er = new ExtractMRZResponse();
		er.setPassportName("Test User");
		er.setPassportNumber("1234567");
		er.setExpirydate("220413");
		er.setIssueDate("");
		er.setCountryCode("IND");
		er.setPlaceOfIssue("");
		er.setSurname("ADCB");
		er.setGivenName("Test");
		er.setSex("M");
		er.setPersonalNo("123456789");
		er.setCity("");
		er.setDateOFBirth("870101");
		er.setEidaNumber("12345678");
		er.setEidaExpiry("220101");
		er.setEidaName("Test User");
		er.setEidaCardNumber("12345678");
		er.setNationality("IND");
		er.setPassportType("P");
		er.setErrorCode("0");
		er.setErrorDescription("Success");
		return er;
	*/}
}
