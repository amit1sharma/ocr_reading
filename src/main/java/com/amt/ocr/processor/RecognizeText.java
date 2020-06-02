package com.amt.ocr.processor;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;


/**
 * Recognize text from image
 * 
 * @author tramvm@gmail.com
 *
 */
public class RecognizeText {

	String image = "image.png";
	// Source path content images
	public static String SRC_PATH = "/home/yamraaj/Pictures/";
	public static String BGR_SRC_PATH = "/home/yamraaj/Pictures/bgr2gray/";
	public static String TESS_DATA = "/home/yamraaj/Pictures/tessdata/tessdata-master/";
	
	// Create tess obj
//	public static Tesseract tesseract = new Tesseract();
	
	// Load OPENCV
	static {
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		tesseract.setDatapath(TESS_DATA);
	}
	
	
	public String extractTextFromImage(Mat inputMat) {
		String result = "";
		Mat gray = new Mat();
		
		// Convert to gray scale
		cvtColor(inputMat, gray, COLOR_BGR2GRAY);
		imwrite(SRC_PATH + image, gray);
		
		//  Apply closing, opening
		//Mat element = getStructuringElement(MORPH_RECT, new Size(2, 2), new Point(1, 1));
		//dilate(gray, gray, element);
		//erode(gray, gray, element);

		//imwrite(SRC_PATH + "closeopen.png", gray);

		/*try {
			// Recognize text with OCR
			result = tesseract.doOCR(new File(SRC_PATH + image));
		} catch (TesseractException e) {
			e.printStackTrace();
		}*/

		return result;
	}
	
	
	public static void main(String[] args) {
		System.out.println("Start recognize text from image");
		long start = System.currentTimeMillis();
	
		// Read image
		Mat origin = imread(SRC_PATH + "2.png");
		
		String result = new RecognizeText().extractTextFromImage(origin);
		System.out.println(result);
		
		System.out.println("Time");
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("Done");

	}
	
	
}