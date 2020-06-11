package com.amt.ocr.processor;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.amt.ocr.decode.MrzParser;
import com.amt.ocr.decode.MrzRecord;
import com.amt.ocr.utils.DeskewImage;
import com.recognition.software.jdeskew.ImageDeskew;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import org.opencv.core.*;

import javax.imageio.ImageIO;

//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;


/**
 * Recognize text from image
 * 
 * @author tramvm@gmail.com
 *
 */
public class RecognizeText {


//	public static String imageName = "image2.jpg";
	public static String imageName = "passport_02.jpg";
	public static String docType = "pp";

//	public static String imageName = "eid1.jpg";
//	public static String docType = "eid";
	public static int eidAllowedAspectRatio = 5;
	public static int ppAllowedAspectRatio = 10;
	public static float eidAllowedCoverageRatio = 0.85f;
	public static float ppAllowedCoverageRatio = 0.75f;


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
//		tesseract.setPageSegMode(8);
	}
	
	
	public String extractTextFromImage(Mat inputMat) throws Exception {
		String result = "";
		extractMRZ(inputMat);
		try {
			// Recognize text with OCR

			result = tesseract.doOCR(new File(TARGET_PATH + "roi"+ imageName));
		} catch (TesseractException e) {
			e.printStackTrace();
		}

		return result;
	}

	private void saveImage(Mat image){
		imwrite(TARGET_PATH + System.nanoTime()+imageName, image);
	}

	public String extractMRZ(Mat originalImage) throws Exception {

		Mat superOriginalImage = new Mat();
		originalImage.copyTo(superOriginalImage);

		int rectKWidth , rectKHeight, sqKWidth, sqKHeight;

		if(originalImage.width()>=900){
			rectKWidth=50;
			rectKHeight = 30;
			sqKWidth=50;
			sqKHeight = 50;
		} else {
			rectKWidth=30;
			rectKHeight = 20;
			sqKWidth=30;
			sqKHeight = 30;
		}

		Mat rectangleKernel = getStructuringElement(MORPH_RECT, new Size(rectKWidth, rectKHeight));
		Mat squareKernel = getStructuringElement(MORPH_RECT, new Size(sqKWidth, sqKHeight));

//		resize(originalImage,originalImage,new Size(originalImage.width(),600));

		saveImage(originalImage);
		Mat gray = new Mat();
		cvtColor(originalImage, originalImage, COLOR_BGR2GRAY);
		originalImage.copyTo(gray);
		saveImage(gray);


//		medianBlur(originalImage, originalImage, 5);
//		saveImage(originalImage);

		GaussianBlur(originalImage, originalImage, new Size(11,11),0);
		saveImage(originalImage);

		morphologyEx(originalImage,originalImage, MORPH_BLACKHAT,rectangleKernel);
		saveImage(originalImage);


		Mat grad_x = new Mat();
		Mat abs_grad_x = new Mat();
		Sobel(originalImage, grad_x, CvType.CV_32F,1,0,3);
//		saveImage(grad_x);
		Core.convertScaleAbs( grad_x, abs_grad_x );
		Core.MinMaxLocResult mmlr = Core.minMaxLoc(abs_grad_x);

		Mat absMinDiff = new Mat();
		Core.subtract(abs_grad_x,Scalar.all(mmlr.minVal),absMinDiff);
		double minMaxDiff = mmlr.maxVal - mmlr.minVal;

		Core.divide(absMinDiff,Scalar.all(minMaxDiff),abs_grad_x);

		Core.multiply(abs_grad_x, Scalar.all(255),abs_grad_x);

		saveImage(abs_grad_x);

//		dilate(abs_grad_x,abs_grad_x, squareKernel);
//		saveImage(abs_grad_x);

		morphologyEx(abs_grad_x,abs_grad_x, MORPH_CLOSE, rectangleKernel);
		saveImage(abs_grad_x);
		threshold(abs_grad_x, abs_grad_x,0,255, THRESH_BINARY|THRESH_OTSU);
		saveImage(abs_grad_x);

		try {
			if(checkIfMRZFound(gray, abs_grad_x, rectangleKernel)){
				return "true";
			}
		} catch(Exception e){
			System.out.println("not detected after 7");
		}
		morphologyEx(abs_grad_x, abs_grad_x,MORPH_CLOSE, squareKernel);
		saveImage(abs_grad_x);
		try {
			if( checkIfMRZFound(gray, abs_grad_x, rectangleKernel)){
				return "true";
			}
		} catch(Exception e){
			System.out.println("not detected after 8");
		}
		erode(abs_grad_x, abs_grad_x, rectangleKernel);
		saveImage(abs_grad_x);

		Mat originalCopy = new Mat();
		abs_grad_x.copyTo(originalCopy);

		if( checkIfMRZFound(gray, originalCopy, rectangleKernel)){
			return "true";
		} else{
			return "false";
		}

	}

	public boolean checkIfMRZFound(Mat gray, Mat originalCopy, Mat rectangleKernel) throws Exception {
		boolean result = false;
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		findContours(originalCopy, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
		hierarchy.release();

		Collections.sort(contours, new Comparator<MatOfPoint>() {
			@Override
			public int compare(MatOfPoint o1, MatOfPoint o2) {
				int result = Double.compare(contourArea(o2), contourArea(o1));
				return result;
			}
		} );
		boolean found = false;
		for(MatOfPoint m : contours){
			Rect rect = boundingRect(m);
			int aspectRatio = rect.width / rect.height;
			float coverageRatio = (float)rect.width / gray.size(1);

			if(aspectRatio>=allowedAspectRatio() && coverageRatio > allowedCoverageRatio()){
				Double px = (rect.x + rect.width) * 0.03;
				Double py = (rect.y + rect.height) * 0.03;

				rect.x = rect.x - px.intValue();
				rect.y = rect.y - py.intValue();
				rect.x = rect.x < 0?0:rect.x;

 				rect.width = rect.width + (px.intValue()*2);
				rect.height = rect.height + (py.intValue()*2);
				rect.width = rect.width>gray.size(1)?gray.size(1):rect.width;
				rect.height = rect.height>gray.size(0)?gray.size(0):rect.height;

				Mat finalRoi = new Mat(gray,rect);
				preprocessROI(rectangleKernel, finalRoi);
				imwrite(TARGET_PATH + "roi"+ imageName, finalRoi);
				found = true;
				break;

			}
		}
		if(found) {
			String str = "";
			try {

				str = tesseract.doOCR(new File(TARGET_PATH + "roi" + imageName));
				System.out.println(str);
				final MrzRecord record = MrzParser.parse(str);
				System.out.println(record.toString());
				result = true;
			} catch (TesseractException e) {
				e.printStackTrace();
			}
		}
		return result;
	}


	private void preprocessROI(Mat rectangleKernel, Mat finalRoi){
		morphologyEx(finalRoi, finalRoi, MORPH_BLACKHAT,rectangleKernel);
//		saveImage(finalRoi);
		Mat squareKernel = getStructuringElement(MORPH_RECT, new Size(2, 2));
		dilate(finalRoi, finalRoi, squareKernel);
//		saveImage(finalRoi);
		Core.bitwise_not(finalRoi, finalRoi);
//		saveImage(finalRoi);
	}


	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		System.out.println(System.getProperty("java.library.path"));

/*		File f = new File(SRC_PATH + imageName);
		BufferedImage image = ImageIO.read(f);
		ImageDeskew deskew = new ImageDeskew(image);
		BufferedImage bim = ImageHelper.rotateImage(image, deskew.getSkewAngle());
		ImageIO.write(bim,"png",f);*/

		// Read image
		Mat origin = imread(SRC_PATH + imageName);

		String result = new RecognizeText().extractMRZ(origin);
		System.out.println("Time");
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("Done");

	}

	private int allowedAspectRatio() throws Exception {
		if(docType.equals("eid")){
			return eidAllowedAspectRatio;
		} else if(docType.equals("pp")){
			return ppAllowedAspectRatio;
		} else{
			throw new Exception("Unexpected Document type");
		}
	}

	private float allowedCoverageRatio() throws Exception {
		if(docType.equals("eid")){
			return eidAllowedCoverageRatio;
		} else if(docType.equals("pp")){
			return ppAllowedCoverageRatio;
		} else{
			throw new Exception("Unexpected Document type");
		}
	}
	
	
}