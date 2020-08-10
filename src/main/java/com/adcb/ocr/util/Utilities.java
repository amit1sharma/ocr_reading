package com.adcb.ocr.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.adcb.ocr.constants.OcrConstants;
import com.adcb.ocr.decode.MrzParseException;
import com.adcb.ocr.decode.MrzParser;
import com.adcb.ocr.decode.types.MrzDocumentCode;



public final class Utilities {

	private static String ppMatcherRegex = "([A-Z])([A-Z0-9<])([A-Z]{3})([A-Z<]{39})\n([A-Z0-9<]{9})([0-9])([A-Z]{3})([0-9]{6})([0-9])([MF<])([0-9]{6})([0-9])([A-Z0-9<]{14})([0-9])([0-9])";
	static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	private static final Pattern pp = Pattern.compile("[A-Z0-9<]{44}[\n][A-Z0-9<]{44}");
	private static final Pattern eid = Pattern.compile("[A-Z0-9<]{30}[\n][A-Z0-9<]{30}[\n][A-Z0-9<]{30}");

	
	
    private static long oneSqInchPxl = 9216;

    public static int eidAllowedAspectRatio = 5;
    public static int ppAllowedAspectRatio = 5;
//    public static int ppAllowedAspectRatio = 10;
//    public static float eidAllowedCoverageRatio = 0.85f;
//    public static float ppAllowedCoverageRatio = 0.75f;

    public static float calculateDPI(int height, int width){
        long totalArea = height * width;
        float ppi = totalArea/oneSqInchPxl;
        System.out.println(ppi);
        return ppi;
    }
    public static int allowedAspectRatio(String docType) throws Exception {
        if(docType.equalsIgnoreCase("eid")){
            return eidAllowedAspectRatio;
        } else if(docType.equalsIgnoreCase("pp")){
            return ppAllowedAspectRatio;
        } else{
            throw new Exception("Unexpected Document type");
        }
    }

/*    public static float allowedCoverageRatio(String docType) throws Exception {
        if(docType.equals("eid")){
            return eidAllowedCoverageRatio;
        } else if(docType.equals("pp")){
            return ppAllowedCoverageRatio;
        } else{
            throw new Exception("Unexpected Document type");
        }
    }*/
    public static void rotate(Mat src, Double angle) {
        if(angle!=null) {
            Point center = new Point(src.width() / 2, src.height() / 2);
            Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
            Size size = new Size(src.width(), src.height());
            Imgproc.warpAffine(src, src, rotImage, size, Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);
        }
    }
    public static void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

	
	public static <T> void printXml(Class<T> classFile, T valueObject){
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(classFile);
	    	Marshaller jaxbMarshaller = jaxbContext.createMarshaller();	
	    	jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(valueObject, sw);
			String xmlContent = sw.toString();
			System.out.println( "xml content: ");
			System.out.println(xmlContent );
		}catch (Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static <T> String getXml(T valueObject){
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(valueObject.getClass());
	    	Marshaller jaxbMarshaller = jaxbContext.createMarshaller();	
	    	jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(valueObject, sw);
			String xmlContent = sw.toString();
			return xmlContent;
		}catch (Exception e ) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String removeSpace(String mrz){
    	return mrz.replaceAll(" ", "");
    }
    public static boolean validateMRZString(String mrz, String docType){
    	boolean result = false;
    	if("pp".equalsIgnoreCase(docType)){
    		String[] lines = mrz.split("\\n");
    		boolean subResult = true;
    		if(lines.length == 2){
	    		for(String line :  lines){
	    			if(line.length() != 44){
	    				subResult = false;
	    				break;
	    			}
	    		}
	    		result = subResult;
    		}
    	} else if("eid".equalsIgnoreCase(docType)){
    		String[] lines = mrz.split("\\n");
    		boolean subResult = true;
    		if(lines.length == 3){
	    		for(String line :  lines){
	    			if(line.length() != 30){
	    				subResult = false;
	    				break;
	    			}
	    		}
	    		result = subResult;
    		}
    	}
    	return result;
    }
    public static MatOfPoint2f orderPointsClockwise(MatOfPoint2f screenCnt2f) {
		System.out.println(screenCnt2f.dump());

		List<Point> points = screenCnt2f.toList();
		// # initialize a list of coordinates that will be ordered
		// # such that the first entry in the list is the top-left,
		// # the second entry is the top-right, the third is the
		// # bottom-right, and the fourth is the bottom-left
		Collections.sort(points, new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				double s1 = p1.x + p1.y;
				double s2 = p2.x + p2.y;
				return Double.compare(s1, s2);
			}
		});
		Point topLeft = points.get(0);
		Point bottomRight = points.get(3);


		// # now, compute the difference between the points, the
		// # top-right point will have the smallest difference,
		// # whereas the bottom-left will have the largest difference
		Collections.sort(points, new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				double s1 = p1.y - p1.x  ;
				double s2 = p2.y - p2.x;
				return Double.compare(s1, s2);
			}
		});
		Point topRight = points.get(0);
		Point bottomLeft = points.get(3);

		Point[] pts = new Point[]{topLeft,topRight, bottomRight, bottomLeft};

		screenCnt2f = new MatOfPoint2f(pts);
		// System.out.println(screenCnt2f.dump());
		return screenCnt2f;
	}
	
	public static String getEidFP(String data){
		
	//	String[] lines = data.split("\\n");
		Pattern p = Pattern.compile("[0-9]{15}");
		Matcher m = p.matcher(data);
		String result = "";
		while (m.find()) {
		    result = m.group();
			if (!MrzParser.checkDigitEida(result)){
				result = result + "FAIL";
			}
		}
		

		return result;
	}
	
	public static String extractMrzString(String fullPageData, String docType){
		String result = "";
		if(docType.equalsIgnoreCase("PP")){
			//Pattern p = Pattern.compile("[A-Z0-9<]{44}[\n][A-Z0-9<]{44}");
			Matcher m = pp.matcher(fullPageData);

			while (m.find()) {
			    result = m.group();
			}
		}
		else if(docType.equalsIgnoreCase("EID")){
			//Pattern p = Pattern.compile("[A-Z0-9<]{30}[\n][A-Z0-9<]{30}[\n][A-Z0-9<]{30}");
			Matcher m = eid.matcher(fullPageData);

			while (m.find()) {
			    result = m.group();
			}
			
		}
		if(!result.equals("")){
			try{
				MrzDocumentCode.parse(result);
			} catch (MrzParseException e){
				result ="";
			}
		}
		return result;
		
	}
}
