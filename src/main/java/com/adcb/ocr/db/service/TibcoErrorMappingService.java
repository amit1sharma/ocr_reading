package com.adcb.ocr.db.service;

import java.util.Optional;

import com.adcb.ocr.db.model.TibcoErrorMappingEntity;

public interface TibcoErrorMappingService {

	public Optional<TibcoErrorMappingEntity> findById(String errorCode);
}
