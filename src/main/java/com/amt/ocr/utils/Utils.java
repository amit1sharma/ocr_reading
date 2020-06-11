package com.amt.ocr.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Utils {
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
        if(docType.equals("eid")){
            return eidAllowedAspectRatio;
        } else if(docType.equals("pp")){
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
}
