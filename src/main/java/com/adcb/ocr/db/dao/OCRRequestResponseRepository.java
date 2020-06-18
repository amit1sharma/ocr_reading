package com.adcb.ocr.db.dao;

import java.math.BigDecimal;

import org.springframework.data.repository.CrudRepository;

import com.adcb.ocr.db.model.OcrRequestResponseModel;

public interface OCRRequestResponseRepository extends CrudRepository<OcrRequestResponseModel, BigDecimal>{

}
