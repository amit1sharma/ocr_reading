package com.adcb.ocr.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adcb.ocr.constants.OcrConstants;
import com.adcb.ocr.db.model.TibcoErrorMappingEntity;
import com.adcb.ocr.db.service.TibcoErrorMappingService;
import com.adcb.ocr.decode.MrzParser;
import com.adcb.ocr.decode.records.MRP;
import com.adcb.ocr.decode.records.MrtdTd1;
import com.adcb.ocr.engine.OCREngine;
import com.adcb.ocr.model.ExtractMRZResponse;
import com.adcb.ocr.pdf.ProcessPdf;
import com.adcb.ocr.service.MrzOcrService;
import com.adcb.ocr.util.Utilities;



@Service
public class MrzOcrServiceImpl implements MrzOcrService{

	@Value("${passport.image.storage.path}")
	private String passportImageStoragePath;
	@Value("${eid.image.storage.path}")
	private String eidImageStoragePath;
	@Value("${image.default.extension}")
	private String imageDefaultExtension;
	@Value("${fp.mismatch.acceptable.count}")
	private String acceptableCount;

	@Autowired
	private OCREngine ocrEngine;

	@Autowired
	private TibcoErrorMappingService tibcoErrorMappingService;

	@Autowired
	private ProcessPdf processPdf;

	private static String passportType = "PP";
	private static String eidType = "EID";
	private static final Logger APPLOGGER = LoggerFactory.getLogger(MrzOcrServiceImpl.class);

	@Override
	public ExtractMRZResponse getTextFromImage(String cid, String docType, String binaryImageFP, String binaryImageBP) {
		String imageStoragePathPP = "", imageStoragePathEidFP = "", imageStoragePathEidBP = "", result = "", resultFP = "";
		boolean isRequestValid = true;
		ExtractMRZResponse extractMRZResponse = new ExtractMRZResponse();

		if(docType == null || docType.trim().equals("") || (!docType.equalsIgnoreCase(passportType) && !docType.equalsIgnoreCase(eidType)) 
				|| cid == null || cid.trim().equals("")){
			isRequestValid = false;
		}
		if (docType.equalsIgnoreCase(passportType) && ( binaryImageFP == null || binaryImageFP.trim().length() == 0)){
			isRequestValid = false;
		}
		else if (docType.equalsIgnoreCase(eidType) && (binaryImageFP == null || binaryImageBP == null || binaryImageFP.trim().length()==0
				||binaryImageBP.trim().length()==0)){
			isRequestValid = false;
		}
		try{
			if(isRequestValid && docType.equalsIgnoreCase(passportType)){
				String imageName = "";
				imageStoragePathPP = passportImageStoragePath+cid;
				File file = new File(imageStoragePathPP);
				file.mkdirs();
				//	String imageNameWithPath = imageStoragePathPP+File.separator+imageName;
				imageName =	saveImage(cid, docType, binaryImageFP, imageStoragePathPP);
				if(imageName.equals("")){
					result = "";
					isRequestValid = false;
				}
				else {
					result = extractMRZ(imageStoragePathPP, imageName, docType);
					if(null == result || result.trim().equals("")){
						isRequestValid = false;
					}
				}
			}
			else if(isRequestValid && docType.equalsIgnoreCase(eidType)){
				String imageNameFP = "", imageNameBP = "";
				imageStoragePathEidFP = eidImageStoragePath+cid;
				File file = new File(imageStoragePathEidFP);
				file.mkdirs();
				imageNameFP  = saveImage(cid ,  OcrConstants.EIDFP, binaryImageFP, imageStoragePathEidFP);
				if (!imageNameFP.equals("")){
					imageStoragePathEidBP = eidImageStoragePath+cid;
					imageNameBP =	saveImage(cid,  OcrConstants.EIDBP, binaryImageBP, imageStoragePathEidBP);
				}
				if(!imageNameFP.equals("") || !imageNameBP.equals("")){
					resultFP = extractMRZ(imageStoragePathEidFP, imageNameFP, OcrConstants.EIDFP);
					result = extractMRZ(imageStoragePathEidBP, imageNameBP, eidType);
					if(null == result || result.trim().equals("") ){ //|| resultFP == null || resultFP.trim().equals("")
						isRequestValid = false;
					}
				} else {
					isRequestValid = false;
				}
			} 
			if(isRequestValid && docType.equalsIgnoreCase(passportType) ){
				MRP record = (MRP) MrzParser.parse(result);
				extractMRZResponse.setPassportName(record.givenNames + " " + record.surname);
				extractMRZResponse.setPassportNumber(record.documentNumber);
				extractMRZResponse.setExpirydate(record.expirationDate.toMrz());
				extractMRZResponse.setCountryCode(record.issuingCountry);
				extractMRZResponse.setSurname(record.surname);
				extractMRZResponse.setGivenName(record.givenNames);
				extractMRZResponse.setSex(record.sex.toString());
				extractMRZResponse.setPersonalNo(record.personalNumber);
				extractMRZResponse.setDateOFBirth(record.dateOfBirth.toMrz());
				extractMRZResponse.setNationality(record.nationality);
				extractMRZResponse.setPassportType(String.valueOf(record.code1)+(String.valueOf(record.code2).equals("<") ? "" : String.valueOf(record.code2) ));
				extractMRZResponse.setErrorCode(OcrConstants.ERROR_CODE_ZERO);
				extractMRZResponse.setErrorDescription(OcrConstants.SUCCESS);
				if(!record.validDocumentNumber){
					extractMRZResponse.setErrorCode(OcrConstants.CHECK_DIGIT_VALIDATION_ERROR_PASSPORT_NUMBER);
					TibcoErrorMappingEntity  tibcoErrorMappingEntity  =getEntityFromDB(OcrConstants.CHECK_DIGIT_VALIDATION_ERROR_PASSPORT_NUMBER) ;
					extractMRZResponse.setErrorDescription(tibcoErrorMappingEntity !=null ? 
							tibcoErrorMappingEntity.getApizoneresponse() : OcrConstants.CHECK_DIGIT_FAILURE_MSG_PASSPORT_NUMBER);
				}
				return extractMRZResponse;
			}
			else if(isRequestValid && docType.equals(eidType) ){
				MrtdTd1 record = (MrtdTd1)MrzParser.parse(result);

				extractMRZResponse.setCountryCode(record.issuingCountry);
				extractMRZResponse.setSurname(record.surname);
				extractMRZResponse.setGivenName(record.givenNames);
				extractMRZResponse.setSex(record.sex.toString());
				extractMRZResponse.setDateOFBirth(record.dateOfBirth.toMrz());
				extractMRZResponse.setEidaNumber(record.optional);
				extractMRZResponse.setEidaExpiry(record.expirationDate.toMrz());
				extractMRZResponse.setEidaName(record.givenNames + " " + record.surname);
				extractMRZResponse.setEidaCardNumber(record.documentNumber);
				extractMRZResponse.setNationality(record.nationality);
				extractMRZResponse.setErrorCode(OcrConstants.ERROR_CODE_ZERO);
				extractMRZResponse.setErrorDescription(OcrConstants.SUCCESS);
				resultFP = MrzParser.fixTesseractReadingErrors(resultFP);
				boolean bpFpmismatch = Utilities.checkMismatchCount(resultFP, record.optional,acceptableCount);
				if(bpFpmismatch || !record.validDocumentNumber || !record.validEidaNumber){
					String errorId = "", statusDesc = "";
					if(!record.validDocumentNumber){
						errorId = OcrConstants.CHECK_DIGIT_VALIDATION_ERROR_EIDA_CARD_NUMBER;
						statusDesc = OcrConstants.CHECK_DIGIT_FAILURE_MSG_EIDA_CARD_NUMBER;
					} else if (!record.validEidaNumber){
						errorId = OcrConstants.CHECK_DIGIT_VALIDATION_ERROR_EIDA_NUMBER;
						statusDesc = OcrConstants.CHECK_DIGIT_FAILURE_MSG_EIDA_NUMBER;
					} else if (!resultFP.equals(record.optional) ){
						errorId = OcrConstants.EID_IN_FRONT_AND_LAST_PAGE_DO_NOT_MATCH;
						statusDesc = OcrConstants.FRONT_AND_BACK_EID_FAILURE_MSG;
					}
					TibcoErrorMappingEntity  tibcoErrorMappingEntity =	getEntityFromDB(errorId);
					extractMRZResponse.setErrorCode(errorId);
					extractMRZResponse.setErrorDescription(statusDesc);
					if (null!=tibcoErrorMappingEntity){
						statusDesc = tibcoErrorMappingEntity.getApizoneresponse();
						if(errorId.equals(OcrConstants.EID_IN_FRONT_AND_LAST_PAGE_DO_NOT_MATCH)){
							statusDesc = statusDesc.replaceAll(OcrConstants.EIDFP, resultFP);
							statusDesc = statusDesc.replaceAll(OcrConstants.EIDBP, record.optional);
						}
						extractMRZResponse.setErrorDescription(statusDesc);
						return extractMRZResponse;
					}
				}
				return extractMRZResponse;
			}
		}catch(Exception e){
			APPLOGGER.error("Issue with the image. Please try a different image or pdf and verify the content of the request." , e);
			isRequestValid = false;
			//result = null;
		}
		if(!isRequestValid){
			APPLOGGER.error("MRZ couldn't be detected for CID {}.Please try a different image or pdf and verify the content of the request." ,cid);
			extractMRZResponse.setErrorCode(OcrConstants.ISSUE_WITH_THE_IMAGE);
			TibcoErrorMappingEntity  tibcoErrorMappingEntity =	getEntityFromDB(OcrConstants.ISSUE_WITH_THE_IMAGE);
			extractMRZResponse.setErrorDescription(tibcoErrorMappingEntity !=null ? 
					tibcoErrorMappingEntity.getApizoneresponse() : "Please try a different image or pdf and verify the content of the request.");
			return extractMRZResponse;
		}
		return extractMRZResponse;
	}

	private String saveImage(String cid, String docType, String binaryImage, String imageStoragePath){
		String imageName = "";
		String fileExtension = "";
		switch (binaryImage.charAt(0)){
		case '/': 
			fileExtension = ".jpeg";
			break;
		case 'i':
			fileExtension = ".png";
			break;
		case 'J':
			fileExtension = ".pdf";
			break;
		default :
			fileExtension = "not defined";
		}
		if(fileExtension.equals("not defined")){
			return imageName;
		}
		try {
			if(docType.equals( passportType)){
				imageName =File.createTempFile(cid, fileExtension).getName();
			}
			else if (docType.equals("EIDFP") ) {
				imageName =File.createTempFile(cid+"_"+eidType+"_FrontPage", fileExtension).getName();
			}
			else if (docType.equals("EIDBP") ) {
				imageName =File.createTempFile(cid+"_"+eidType+"_BackPage", fileExtension).getName();
			}
		} catch (IOException e) {
			APPLOGGER.error("Error while creating temp file name with file extension.. " , e);
			return "";
		}
		try (FileOutputStream imageOutFile = new FileOutputStream(imageStoragePath+File.separator+imageName)) {
			// Converting Base64 String into Image byte array
			byte[] imageByteArray = Base64.getDecoder().decode(binaryImage);
			imageOutFile.write(imageByteArray);
		} catch (FileNotFoundException e) {
			APPLOGGER.error("Image not found.. " , e);
			return "";
		} catch (IOException ioe) {
			APPLOGGER.error("Exception while reading the Image.. " , ioe);
			return ""; 
		}
		return imageName;
	}


	private String extractMRZ(String srcPath, String imageName, String docType) throws Exception {
		if(imageName.endsWith(".pdf")){
			imageName = processPdf.process(srcPath, imageName, docType);
		}
		return ocrEngine.startEngine(srcPath, imageName, docType);
	}

	private TibcoErrorMappingEntity getEntityFromDB(String errorId){
		Optional<TibcoErrorMappingEntity>  tibcoErrorMappingEntity = tibcoErrorMappingService.findById(errorId);
		if (tibcoErrorMappingEntity.isPresent()){
			return tibcoErrorMappingEntity.get();
		}
		APPLOGGER.error("ErrorId  not present in DB for ID {}",errorId);
		return null;
	}
}
