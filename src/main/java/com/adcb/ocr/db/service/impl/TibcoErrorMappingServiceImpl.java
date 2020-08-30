package com.adcb.ocr.db.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adcb.ocr.db.dao.TibcoErrorMappingRepository;
import com.adcb.ocr.db.model.TibcoErrorMappingEntity;
import com.adcb.ocr.db.service.TibcoErrorMappingService;
@Service
public class TibcoErrorMappingServiceImpl implements TibcoErrorMappingService{

	@Autowired
	private TibcoErrorMappingRepository tibcoErrorMappingRepository;
	@Override
	public Optional<TibcoErrorMappingEntity> findById(String errorCode) {
		return tibcoErrorMappingRepository.findById(errorCode);
	}
}
