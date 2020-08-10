package com.adcb.ocr.db.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adcb.ocr.db.model.TibcoErrorMappingEntity;

@Repository
public interface TibcoErrorMappingRepository extends JpaRepository<TibcoErrorMappingEntity, String>{
	
}
