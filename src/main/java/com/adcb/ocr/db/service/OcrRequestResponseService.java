package com.adcb.ocr.db.service;

import java.util.Date;

public interface OcrRequestResponseService {

	void saveLog(String requestData, String responseData, Date requestTime, Date responseTime);

}
