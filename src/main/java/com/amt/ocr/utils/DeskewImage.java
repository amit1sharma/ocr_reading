package com.amt.ocr.utils;

import com.amt.ocr.image.Image;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class DeskewImage {

    public static Tesseract tesseract = new Tesseract();
    public static String imageName = "wha.jpeg";
    public static String SRC_PATH = "/home/yamraaj/Pictures/";
    public static String TARGET_PATH = "/home/yamraaj/Pictures/bgr2gray/";
    public static String TESS_DATA = "/home/yamraaj/Pictures/tessdata/tessdata-master/";

    // Load OPENCV
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        tesseract.setDatapath(TESS_DATA);
        tesseract.setLanguage("ocrb");
        tesseract.setPageSegMode(8);
    }

    public static void main(String[] args){
        Mat source = imread(SRC_PATH+imageName);

        Image i = new Image(source,"pp");
        i
//        i.deskew()
//        i.greyScale()
//                .save(TARGET_PATH+"1"+imageName)
//                .gradient()
//                .save(TARGET_PATH+"2"+imageName)
//                .setThreshold(0, 255, THRESH_BINARY|THRESH_OTSU)
//                .save(TARGET_PATH+"3"+imageName)
//                .horizontalConnect(19,1)
                .deskew()
        .save(TARGET_PATH+imageName);
    }

    public static void removeBackGround(Mat _mat){
        if(_mat!=null){
            cvtColor(_mat, _mat, COLOR_RGBA2GRAY);
            saveImage(_mat);
            threshold(_mat, _mat, 0, 255, THRESH_BINARY_INV|THRESH_OTSU);
            saveImage(_mat);
            // get background
            Mat ones = Mat.ones(3,3, CvType.CV_8U);
            erode(_mat, _mat, ones);
            saveImage(_mat);

            Mat opening = new Mat();
            dilate(_mat,opening, ones);
            saveImage(opening);
            Mat cardBg = new Mat();
            dilate(opening, cardBg, ones, new Point(-1,-1),3);
            saveImage(cardBg);
            // distance transform
            Mat distancetrans = new Mat();
            distanceTransform(opening, distancetrans, DIST_L2, 5);
            Core.normalize(distancetrans, distancetrans,1,0,Core.NORM_INF);
            //get foreground
            Mat cardFg = new Mat();
            threshold(distancetrans, cardFg, 0.7 * 1, 255, THRESH_BINARY);
            cardFg.convertTo(cardFg, CvType.CV_8U, 1,0);
            Mat unknown = new Mat();
            Core.subtract(cardBg, cardFg, unknown);
            saveImage(unknown);

//            connectedComponents(cardFg)







        }
    }
    private static void saveImage(Mat image){
        imwrite(TARGET_PATH + System.currentTimeMillis()+imageName, image);
    }


}
