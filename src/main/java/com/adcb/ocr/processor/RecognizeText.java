package com.adcb.ocr.processor;

//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;


/**
 * Recognize text from image
 *
 */
public class RecognizeText {/*

	public static String image = "picture3.jpg";
	// Source path content images
	public static String SRC_PATH = "/home/yamraaj/Pictures/";
	public static String TARGET_PATH = "/home/yamraaj/Pictures/bgr2gray/";
	public static String TESS_DATA = "/home/yamraaj/Pictures/tessdata/tessdata-master/";
	
	// Create tess obj
	public static Tesseract tesseract = new Tesseract();
	
	// Load OPENCV
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		tesseract.setDatapath(TESS_DATA);
		tesseract.setLanguage("ocrb");
	}
	
	
	public String extractTextFromImage(Mat inputMat) {
		String result = "";
		extractMRZ(inputMat);
		try {
			// Recognize text with OCR

			result = tesseract.doOCR(new File(TARGET_PATH + "roi"+image));
		} catch (TesseractException e) {
			e.printStackTrace();
		}

		return result;
	}

	public String extractMRZ(Mat originalImage) {

		Mat superOriginalImage = new Mat();
		originalImage.copyTo(superOriginalImage);
		Mat gray = new Mat();

		Mat rectangleKernal = getStructuringElement(MORPH_RECT, new Size(50, 30));
		Mat squareKernal = getStructuringElement(MORPH_RECT, new Size(50, 50));

//		resize(originalImage,originalImage,new Size(originalImage.width(),600));

		imwrite(TARGET_PATH + "1"+image, originalImage);

		cvtColor(originalImage, originalImage, COLOR_BGR2GRAY);
		originalImage.copyTo(gray);
		imwrite(TARGET_PATH + "2"+image, originalImage);

		GaussianBlur(originalImage, originalImage, new Size(11,11),0);

		imwrite(TARGET_PATH + "3"+image, originalImage);

		morphologyEx(originalImage,originalImage, MORPH_BLACKHAT,rectangleKernal);

		imwrite(TARGET_PATH + "4"+image, originalImage);


		Mat grad_x = new Mat(), grad_y = new Mat();
		Mat abs_grad_x = new Mat();
	//	Sobel(originalImage, grad_x, CvType.CV_32F,1,0,3); // ddepth has to be cv_32f but it is not there so using 5

		Core.convertScaleAbs( grad_x, abs_grad_x );
		Core.MinMaxLocResult mmlr = Core.minMaxLoc(abs_grad_x);

		Mat absMinDiff = new Mat();
		Core.subtract(abs_grad_x,Scalar.all(mmlr.minVal),absMinDiff);
		double minMaxDiff = mmlr.maxVal - mmlr.minVal;

		Core.divide(absMinDiff,Scalar.all(minMaxDiff),abs_grad_x);

		Core.multiply(abs_grad_x, Scalar.all(255),abs_grad_x);

		imwrite(TARGET_PATH + "5"+image, abs_grad_x);

		morphologyEx(abs_grad_x,abs_grad_x, MORPH_CLOSE, rectangleKernal);
		imwrite(TARGET_PATH + "6"+image, abs_grad_x);
		threshold(abs_grad_x, abs_grad_x,0,255, THRESH_BINARY|THRESH_OTSU);
		imwrite(TARGET_PATH + "7"+image, abs_grad_x);

		try {
			return checIfMRZFound(gray, abs_grad_x, superOriginalImage);
		} catch(Exception e){
			System.out.println("not detected after 7");
		}
		morphologyEx(abs_grad_x, abs_grad_x,MORPH_CLOSE, squareKernal);
		imwrite(TARGET_PATH + "8"+image, abs_grad_x);
		try {
			return checIfMRZFound(gray, abs_grad_x, superOriginalImage);
		} catch(Exception e){
			System.out.println("not detected after 8");
		}
		erode(abs_grad_x, abs_grad_x, rectangleKernal);
		imwrite(TARGET_PATH + "9"+image, abs_grad_x);

		Mat originalCopy = new Mat();
		abs_grad_x.copyTo(originalCopy);

		return checIfMRZFound(gray, originalCopy, superOriginalImage);

	}

	public String checIfMRZFound(Mat gray, Mat originalCopy, Mat superOriginalImage){
		return null;
	}


	
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println(System.getProperty("java.library.path"));
	
		// Read image
		Mat origin = imread(SRC_PATH + image);
		
//		String result = new RecognizeText().extractTextFromImage(origin);
		String result = new RecognizeText().extractMRZ(origin);
		final MrzRecord record = MrzParser.parse(result);
//		System.out.println(record.toMrz());
		System.out.println(result);
		
		System.out.println("Time");
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("Done");

	}
	
	
*/}