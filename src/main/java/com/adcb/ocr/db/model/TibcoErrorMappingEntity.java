package com.adcb.ocr.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "OCR_TIBCO_ERROR_CODES")
public class TibcoErrorMappingEntity {

	@Id
	@Column(name = "TIBCO_ERROR_CODE")
	private String errorcode;
	@Column(name = "API_ZONE_RESPONSE")
	private String apizoneresponse;
	@Column(name = "WEBSERVICE_ERROR_DESCRIPTION")
	private String errordescription;

	public String getErrorcode() {
		return errorcode;
	}
	public void setErrorcode(String errorcode) {
		this.errorcode = errorcode;
	}
	public String getApizoneresponse() {
		return apizoneresponse;
	}
	public void setApizoneresponse(String apizoneresponse) {
		this.apizoneresponse = apizoneresponse;
	}

	public String getErrordescription() {
		return errordescription;
	}

	public void setErrordescription(String errordescription) {
		this.errordescription = errordescription;
	}

}
