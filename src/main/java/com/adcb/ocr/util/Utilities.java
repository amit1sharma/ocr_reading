package com.adcb.ocr.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;



public final class Utilities {

	private static String ppMatcherRegex = "([A-Z])([A-Z0-9<])([A-Z]{3})([A-Z<]{39})\n([A-Z0-9<]{9})([0-9])([A-Z]{3})([0-9]{6})([0-9])([MF<])([0-9]{6})([0-9])([A-Z0-9<]{14})([0-9])([0-9])";
	static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
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
	
	
}
