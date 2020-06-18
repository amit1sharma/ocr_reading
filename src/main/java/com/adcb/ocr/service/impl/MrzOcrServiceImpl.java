package com.adcb.ocr.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adcb.ocr.decode.MrzParser;
import com.adcb.ocr.decode.records.MRP;
import com.adcb.ocr.decode.records.MrtdTd1;
import com.adcb.ocr.engine.OCREngine;
import com.adcb.ocr.model.ExtractMRZResponse;
import com.adcb.ocr.service.MrzOcrService;



@Service
public class MrzOcrServiceImpl implements MrzOcrService{

	@Value("${passport.image.storage.path}")
	private String passportImageStoragePath;
	@Value("${eid.image.storage.path}")
	private String eidImageStoragePath;
	@Value("${image.default.extension}")
	private String imageDefaultExtension;

	@Autowired
	private OCREngine ocrEngine;

	/*	private static String tesseractTessdataPath;

    @Value("${tesseract.tessdata.path}")
    public void setTessDataPath(String dataPath) {
    	tesseractTessdataPath = dataPath;
    }*/

	private static String passportType = "PP";
	private static String eidType = "EID";
	private static final Logger APPLOGGER = LoggerFactory.getLogger(MrzOcrServiceImpl.class);
	// Create tess obj

	/*	private static Tesseract getTesseract() {
		Tesseract instance = new Tesseract();
		instance.setDatapath(tesseractTessdataPath);
		instance.setLanguage("ocrb");
		return instance;
	}*/

	@Override
	public ExtractMRZResponse getTextFromImage(String cid, String docType, String binaryImageFP, String binaryImageBP) {
		String imageStoragePathPP = "";
		String imageStoragePathEidFP = "";
		String imageStoragePathEidBP = "";
		ExtractMRZResponse extractMRZResponse = new ExtractMRZResponse();
		String result = "", resultFP = "";
		if(docType == null || docType.trim().equals("") || (! docType.equals(passportType) && !docType.equals(eidType) 
				|| cid == null || cid.trim().equals("") )){
			return extractMRZResponse;
		}
		if (docType.equals(passportType) && ( binaryImageFP == null || binaryImageFP.trim().length() == 0)){
			return extractMRZResponse;
		}
		if (docType.equals(eidType) && (binaryImageFP == null || binaryImageBP == null || binaryImageFP.trim().length()==0
				||binaryImageBP.trim().length()==0)){
			return extractMRZResponse;
		}
		if(docType.equals(passportType)){
			String imageName = "";
			imageStoragePathPP = passportImageStoragePath+cid;
			File file = new File(imageStoragePathPP);
			file.mkdirs();
			//	String imageNameWithPath = imageStoragePathPP+File.separator+imageName;
			imageName =	saveImage(cid, docType, binaryImageFP, imageStoragePathPP);
			if(imageName.equals("")){
				extractMRZResponse.setErrorDescription("Failure : Image could not be saved !!");
				return extractMRZResponse;
			}
			result = extractMRZ(imageStoragePathPP, imageName, docType);
		}
		if(docType.equals(eidType)){
			String imageNameFP = "";
			String imageNameBP = "";


			//Code is temporarily added until front page validation is not required- START 
			imageStoragePathEidBP = eidImageStoragePath+cid;
			File file = new File(imageStoragePathEidBP);
			file.mkdirs();
			imageNameBP =	saveImage(cid, "EIDBP", binaryImageBP, imageStoragePathEidBP);
			if(imageNameBP.equals("")){
				extractMRZResponse.setErrorDescription("Failure : Image could not be saved !!");
				return extractMRZResponse;
			}
			// Code is temporarily added until front page validation is not required- END 


			/*			
			 * Below code is commented for time being, until front page 
			 * validation is not required. Uncomment below code to Enable front page validation //
			 * 
			 * 
			 *
			imageStoragePathEidFP = eidImageStoragePath+cid;
			imageNameFP  = saveImage(cid , "EIDFP", binaryImageFP, imageStoragePathEidFP);
			if (!imageNameFP.equals("")){
				imageStoragePathEidBP = eidImageStoragePath+cid;
				imageNameBP =	saveImage(cid, "EIDBP", binaryImageBP, imageStoragePathEidBP);
			}
			if(imageNameFP.equals("") || imageNameBP.equals("")){
				extractMRZResponse.setErrorDescription("Failure : Image could not be saved !!");
				return extractMRZResponse;
			}
			resultFP = extractMRZ(imageStoragePathEidFP, imageNameFP, "EIDFP");
			 *
			 */
			result = extractMRZ(imageStoragePathEidBP, imageNameBP, "EID");
		}
		if(!result.equals("") && docType.equals(passportType)){
			MRP record = (MRP) MrzParser.parse(result);
			extractMRZResponse.setPassportName(record.givenNames + " " + record.surname);
			extractMRZResponse.setPassportNumber(record.documentNumber);
			extractMRZResponse.setExpirydate(record.expirationDate.toMrz());
			extractMRZResponse.setIssueDate("");
			extractMRZResponse.setCountryCode(record.issuingCountry);
			extractMRZResponse.setPlaceOfIssue("");
			extractMRZResponse.setSurname(record.surname);
			extractMRZResponse.setGivenName(record.givenNames);
			extractMRZResponse.setSex(record.sex.toString());
			extractMRZResponse.setPersonalNo(record.personalNumber);
			extractMRZResponse.setCity("");
			extractMRZResponse.setDateOFBirth(record.dateOfBirth.toMrz());
			extractMRZResponse.setEidaNumber("");
			extractMRZResponse.setEidaExpiry("");
			extractMRZResponse.setEidaName("");
			extractMRZResponse.setEidaCardNumber("");
			extractMRZResponse.setNationality(record.nationality);
			extractMRZResponse.setPassportType(String.valueOf(record.code1)+String.valueOf(record.code2));
			extractMRZResponse.setErrorCode("0");
			extractMRZResponse.setErrorDescription("Success");
			return extractMRZResponse;
		}
		else if(!result.equals("") && docType.equals(eidType)){
			MrtdTd1 record = (MrtdTd1)MrzParser.parse(result);
			extractMRZResponse.setPassportName("");
			extractMRZResponse.setPassportNumber("");
			extractMRZResponse.setExpirydate("");
			extractMRZResponse.setIssueDate("");
			extractMRZResponse.setCountryCode(record.issuingCountry);
			extractMRZResponse.setPlaceOfIssue("");
			extractMRZResponse.setSurname(record.surname);
			extractMRZResponse.setGivenName(record.givenNames);
			extractMRZResponse.setSex(record.sex.toString());
			extractMRZResponse.setPersonalNo("");
			extractMRZResponse.setCity("");
			extractMRZResponse.setDateOFBirth(record.dateOfBirth.toMrz());
			extractMRZResponse.setEidaNumber(record.optional);
			extractMRZResponse.setEidaExpiry(record.expirationDate.toMrz());
			extractMRZResponse.setEidaName(record.givenNames + " " + record.surname);
			extractMRZResponse.setEidaCardNumber(record.documentNumber);
			extractMRZResponse.setNationality(record.nationality);
			extractMRZResponse.setPassportType("");
			extractMRZResponse.setErrorCode("0");
			extractMRZResponse.setErrorDescription("Success");
			return extractMRZResponse;
		}
		if(result==null || "".equals(result.trim())){
			extractMRZResponse.setErrorCode("1");
			extractMRZResponse.setErrorDescription("Failure : MRZ could not be read !!");
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
		case 'R':
			fileExtension = ".gif";
			break;
		default :
			fileExtension = ".jpg";
		}
		try {
			if(docType.equals( passportType)){
				imageName =File.createTempFile(cid, fileExtension).getName();
				//	imageName = cid+fileExtension;
			}
			else if (docType.equals("EIDFP") ) {
				imageName =File.createTempFile(cid+"_"+eidType+"_FrontPage", fileExtension).getName();
				//	imageName = cid+"_"+eidType+"_FrontPage"+fileExtension;
			}
			else if (docType.equals("EIDBP") ) {
				imageName =File.createTempFile(cid+"_"+eidType+"_BackPage", fileExtension).getName();
				//	imageName = cid+"_"+eidType+"_BackPage"+fileExtension;
			}
		} catch (IOException e) {
			APPLOGGER.error("Error while creating temp file name with file extension {}" , e);
			return "";
		}
		try (FileOutputStream imageOutFile = new FileOutputStream(imageStoragePath+File.separator+imageName)) {
			// Converting Base64 String into Image byte array
			byte[] imageByteArray = Base64.getDecoder().decode(binaryImage);
			imageOutFile.write(imageByteArray);
		} catch (FileNotFoundException e) {
			APPLOGGER.error("Image not found {}" , e);
			return "";
		} catch (IOException ioe) {
			APPLOGGER.error("Exception while reading the Image {}  " , ioe);
			return ""; 
		}
		return imageName;
	}


	public String extractMRZ(String srcPath, String imageName, String docType) {
		return ocrEngine.startEngine(srcPath, imageName, docType);
	}
}
