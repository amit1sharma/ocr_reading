package com.adcb.ocr.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "OCR_REQUESTRESPONSE")
public class OcrRequestResponseModel {

	@Id
	@Column(name = "REQID")
//	@GeneratedValue(strategy=GenerationType.AUTO)
	@SequenceGenerator(name = "OCR_REQUESTRESPONSE_seq", sequenceName = "OCR_REQUESTRESPONSE_seq", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OCR_REQUESTRESPONSE_seq") 
	private Long requestId;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "REQTIMESTAMP")
	private Date requestTimeStamp;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "RESPTIMESTAMP")
	private Date responseTimeStamp;
	
	@Lob
	@Column(name = "REQUESTDATA")
	private String requestData;
	
	@Lob
	@Column(name = "RESPONSEDATA")
	private String responseData;

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public Date getRequestTimeStamp() {
		return requestTimeStamp;
	}

	public void setRequestTimeStamp(Date requestTimeStamp) {
		this.requestTimeStamp = requestTimeStamp;
	}

	public Date getResponseTimeStamp() {
		return responseTimeStamp;
	}

	public void setResponseTimeStamp(Date responseTimeStamp) {
		this.responseTimeStamp = responseTimeStamp;
	}

	public String getRequestData() {
		return requestData;
	}

	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}

	public String getResponseData() {
		return responseData;
	}

	public void setResponsedata(String responseData) {
		this.responseData = responseData;
	}
	
}
