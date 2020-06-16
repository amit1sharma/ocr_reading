package com.adcb.ocr.image;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_GRADIENT;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.Sobel;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.threshold;

import static org.opencv.imgproc.Imgproc.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.adcb.ocr.util.Utilities;

public class Image {

    List<String> operationPerformed = new ArrayList<>();

    Mat _original_image = new Mat();
    Mat _gray = new Mat();
    Mat _mat = new Mat();
    String docType;
    String imageAbsoluteSourcePath;
    String imageAbsoluteTargetPath;
    String imageName;


    public Image() {
    }

    public Image(Mat _mat, String docType) {
        _mat.copyTo(this._original_image);
        this.docType = docType;
    }

    public Image(String docType, String imageAbsoluteSourcePath, String imageName) throws IOException {
        this._mat = imread(imageAbsoluteSourcePath+File.separator+imageName);
        _mat.copyTo(this._original_image);
        this.docType = docType;
        this.imageAbsoluteSourcePath = imageAbsoluteSourcePath;
        String imageNameWithoutExt = imageName.split("\\.")[0];

        String tgtPath = this.imageAbsoluteSourcePath+File.separator+imageNameWithoutExt;
        File f = new File(tgtPath);
        if(f.exists()) {
            f.deleteOnExit();
            Utilities.deleteDirectoryStream(Paths.get(f.toURI()));
        }
        f.mkdirs();
        this.imageAbsoluteTargetPath = tgtPath;
        this.imageName = imageName;
        selectKernels();
    }

    Mat rectangleKernel, squareKernel;
    public void selectKernels(){
        int rectKWidth , rectKHeight, sqKWidth, sqKHeight;

        if(_original_image.width()>=900){
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
        rectangleKernel = getStructuringElement(MORPH_RECT, new Size(rectKWidth, rectKHeight));
        squareKernel = getStructuringElement(MORPH_RECT, new Size(sqKWidth, sqKHeight));
    }
    public Image gaussianBlur(Size... size){
        Size s = size.length>0?size[0]:new Size(11,11);
        GaussianBlur(this._mat, this._mat, new Size(11,11),0);
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public float calculatePPI(){
        return Utilities.calculateDPI(this._mat.height(), this._mat.width());
    }
    public Image resize(int width) throws Exception {
        if(_mat!=null){
            if(_mat.width() >= width){
                Imgproc.resize(_mat, _mat, new Size(width, _mat.height()));
            }
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image greyScale(){
        if(_mat!=null){
            cvtColor(_mat, _mat, COLOR_BGR2GRAY);
            _mat.copyTo(_gray);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }
    public Rect findBorder(int size){

        Mat src = new Mat();
        _mat.copyTo(src);
        Mat sbl_x = new Mat(), sbl_y = new Mat();
        int ksize = 2 * size + 1;
        Sobel(src, sbl_x, CvType.CV_32FC1, 2, 0, ksize);
        Sobel(src, sbl_y, CvType.CV_32FC1, 0, 2, ksize);
        Mat sum_img = new Mat();
        Core.add(sbl_x, sbl_y, sum_img);

        Mat gray = new Mat();
        Core.normalize(sum_img, gray, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        Mat row_proj = new Mat(), col_proj = new Mat();
        Core.reduce(gray, row_proj, 1, Core.REDUCE_AVG, CvType.CV_8UC1);

        Core.reduce(gray, col_proj, 0, Core.REDUCE_AVG, CvType.CV_8UC1);

        Sobel(row_proj, row_proj, CvType.CV_8UC1, 0, 2, 3);

        Sobel(col_proj, col_proj, CvType.CV_8UC1, 2, 0, 3);


        Long half_pos = row_proj.total() / 2;

        Mat minmaxloc = new Mat(row_proj,new Range(0,half_pos.intValue()), new Range(0,1));
        Core.MinMaxLocResult mmr = Core.minMaxLoc(minmaxloc);

        Rect result = new Rect();
        result.y = ((Double)mmr.maxLoc.y).intValue();

        Mat minmax2 = new Mat(row_proj, new Range(half_pos.intValue(), ((Long)row_proj.total()).intValue()), new Range(0,1));
        Core.MinMaxLocResult mmr2 = Core.minMaxLoc(minmax2);

        result.height = ((Double)mmr2.maxLoc.y).intValue() + half_pos.intValue() - result.y;

        half_pos = col_proj.total() / 2;

        Mat minmax3 = new Mat(col_proj, new Range(0,1), new Range(0, half_pos.intValue()));
        Core.MinMaxLocResult mmr3 = Core.minMaxLoc(minmax3);
        result.x = ((Double)mmr3.maxLoc.x).intValue();

        Mat minmax4 = new Mat(col_proj, new Range(0,1), new Range( half_pos.intValue(), ((Long)col_proj.total()).intValue()));
        Core.MinMaxLocResult mmr4 = Core.minMaxLoc(minmax4);

        result.width = ((Double)mmr4.maxLoc.x).intValue() + half_pos.intValue() - result.x;

        return result;
    }

    public Image removeBorder(){
        Rect r = findBorder(2);
        Mat borderRemoval = new Mat(_mat, r);
        borderRemoval.copyTo(_mat);
        return this;
    }

    public Image withBlackBoarder(int borderWidth, Scalar scalar){
        if(_mat!=null){
            if(borderWidth>0){
                Core.copyMakeBorder(_mat, _mat, borderWidth, borderWidth, borderWidth, borderWidth,
                        Core.BORDER_CONSTANT, scalar);
            }
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }
    public Image setThreshold(double threshold, double maxValue, int thresholdType){
        if(_mat!=null){
            threshold(_mat, _mat, threshold, maxValue, thresholdType);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image setAdaptiveThreshold(double maxValue, int adaptiveThresholdType, int thresholdType, int blockType, double param){
        if(_mat!=null){
            adaptiveThreshold(_mat, _mat, maxValue, adaptiveThresholdType, thresholdType, blockType, param);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image gradient(){
        if(_mat!=null){
            Mat sqKernel = getStructuringElement(MORPH_RECT, new Size(3, 3), new Point(-1,-1));
            morphologyEx(_mat, _mat, MORPH_GRADIENT, sqKernel, new Point(-1,-1), 1);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }
    public Image save(){
        if(_mat!=null){
            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
            imwrite(imageAbsoluteTargetPath+File.separator+System.currentTimeMillis()+imageName, _mat);
        }
        return this;
    }
    public Image save(String imageAbsoluteTargetPath){
        if(_mat!=null){
            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
            imwrite(imageAbsoluteTargetPath+File.separator+imageName, _mat);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image save(Mat mat, String imageAbsoluteTargetPath){
        if(_mat!=null){
            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
            imwrite(imageAbsoluteTargetPath+File.separator+imageName, mat);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image save(Mat mat){
        if(_mat!=null){
            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
            imwrite(imageAbsoluteTargetPath+File.separator+System.currentTimeMillis()+imageName, mat);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image horizontalConnect(int horizontalMoveSize, int verticalMoveSize){
        if(_mat!=null){
            Mat sqKernel = getStructuringElement(MORPH_RECT, new Size(horizontalMoveSize, verticalMoveSize), new Point(-1,-1));
            morphologyEx(_mat, _mat, MORPH_CLOSE, sqKernel, new Point(-1, -1), Core.BORDER_DEFAULT);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    List<MatOfPoint> allContours = new ArrayList<>();
    /**
     * this will find contours
     * @return
     */
    public List<MatOfPoint> getRectangles(){
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(_mat, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                int result = Double.compare(contourArea(o2), contourArea(o1));
                return result;
            }
        } );
        return contours;
    }

    public Image findContourRectangles(){
        allContours.addAll(getRectangles());
        return this;
    }

    public String getRoiImagePath() throws Exception {
        String roiImagePath = imageAbsoluteTargetPath;
        if(getSaveROI(allContours, roiImagePath)){
            return roiImagePath;
        } else{
            throw new Exception ("Unable to find and ROI out of contours");
        }
    }

    public boolean getSaveROI( List<MatOfPoint> contours, String path) throws Exception {
        boolean found = false;
//        markAreaOfInterest(contours);
//        save(_original_image, path);
        for(MatOfPoint m : contours){
            Rect rect = boundingRect(m);
            Mat currentRoi = new Mat(_gray,rect);
            save(currentRoi, path);
            float aspectRatio = (float)rect.width / rect.height;
            float coverageRatioH = (float)rect.width / _gray.width();


            if(aspectRatio>=Utilities.allowedAspectRatio(docType)  && rect.width>(_gray.width())/2){
                /*if(_gray.height()>_gray.width()) {
                    double increaseFactorH = 1 - coverageRatioH;
//                double pendingPerc = (increaseFactor*100);// this is percentage shorter than original  so we can expand rect within this percentage
                    double remaining_pixLR = (_gray.width() * increaseFactorH) / 2; // remaining pixels tomwards left or right
                    double remaining_pixB = (_gray.height() - (rect.y + rect.height)) / 2; // remaining pixels todys
                    *//**
                     * for handling case when two images in single image
                     *//*

                    if ((remaining_pixB * 2) > (_gray.height() / 2)) {
                        remaining_pixB = remaining_pixB * 2 - _gray.height() / 2;
                    }
                    // expanding upto 90% of remaing pix
                    Double newX = rect.x - (remaining_pixLR * 0.90); // this can be assumed as rect.x exapnding to max initial 0.10
                    Double newY = rect.y - (remaining_pixB * 0.90); // initial 0.90
                    Double finalWidth = rect.width + ((rect.x - newX) * 2);
                    Double finalHeight = rect.height + ((rect.y - newY) * 2);
                    rect.x = newX.intValue();
                    rect.y = newY.intValue();
                    rect.x = rect.x < 0 ? 0 : rect.x;
                    rect.y = rect.y < 0 ? 0 : rect.y;
                    rect.width = finalWidth.intValue();
                    rect.height = finalHeight.intValue();
                } else {*/
                    Double px = (rect.x + rect.width) * 0.05;
                    Double py = (rect.y + rect.height) * 0.05;

                    rect.x = rect.x - px.intValue();
                    rect.y = rect.y - py.intValue();
                    rect.x = rect.x < 0 ? 0 : rect.x;

                    rect.width = rect.width + (px.intValue() * 2);
                    rect.height = rect.height + (py.intValue() * 2);
//                }
                rect.width = (rect.x + rect.width) > _gray.width() ? (rect.width - ((rect.x + rect.width) - _gray.width())) : rect.width;
                rect.height = (rect.y + rect.height) > _gray.height() ? (rect.height - ((rect.y + rect.height) - _gray.height())) : rect.height;
//                rect.width = rect.width>_gray.size(1)?_gray.size(1):rect.width;
//                rect.height = rect.height>_gray.size(0)?_gray.size(0):rect.height;
                Mat finalRoi = new Mat(_gray,rect);
                save(finalRoi, path);
                preprocessROI(rectangleKernel, finalRoi);
                save(finalRoi, path);
                found = true;
                break;
            }
        }
        return found;
    }
    public void preprocessROI(Mat rectangleKernel, Mat finalRoi){
        try {
            if(rectangleKernel!=null) {
                deskewThis(finalRoi);
//                Mat adap = new Mat();
//                threshold(finalRoi, adap, 50, 255, THRESH_OTSU);//(finalRoi, adap, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 10);
//                adaptiveThreshold(finalRoi, finalRoi, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 10);
//                save(finalRoi);
//                morphologyEx(finalRoi, finalRoi, MORPH_BLACKHAT, rectangleKernel);
                Mat squareKernel = getStructuringElement(MORPH_RECT, new Size(1, 1));
                erode(finalRoi, finalRoi, squareKernel);
//                Core.bitwise_not(finalRoi, finalRoi);
//                save(finalRoi);
            } else{
                System.err.println("rectangleKernel not initialized");
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    /**
     * run this method after calling #greyScale# method.
     */
    public Image removeBackGround(){
        if(_mat!=null){
            Mat edges = new Mat();
            Canny(_mat, edges, 10, 150);
            Mat points = new Mat();
            Core.findNonZero(_mat, points);
            Rect rect = boundingRect(points);
            Mat newMat = new Mat(_mat, rect);
            newMat.copyTo(_mat);
        }
        return this;
    }

    public void deskewThis(Mat mat){
        Size size = mat.size();
//        Core.bitwise_not(source, source);
        Mat edges = new Mat();
        Canny(mat, edges,10,150);
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 100, size.width / 2.f, 20);
        Mat channel0 = new Mat();
        Core.extractChannel(lines, channel0, 0);
        double angle = 0.;
        for(int i = 0; i<lines.height(); i++){
            for(int j = 0; j<lines.width();j++){
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }
        if(angle>0) {
            angle /= lines.size().area();
            angle = angle * 180 / Math.PI;
            Utilities.rotate(mat, angle);
        }
    }

    public Image deskew(){
        Size size = _mat.size();
//        Core.bitwise_not(source, source);
        Mat edges = new Mat();
        Canny(_mat, edges,10,150);
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 100, size.width / 2.f, 20);
        double angle = 0.;
        for(int i = 0; i<lines.height(); i++){
            for(int j = 0; j<lines.width();j++){
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }
        angle /= lines.size().area();
        angle = angle * 180 / Math.PI;
        Utilities.rotate(_mat, angle);
        return this;
    }
    public void deskewThis_dotnet(Mat mat){
        Mat edges = new Mat();
        Canny(mat, edges,180,120);
        Mat lines = new Mat();
        HoughLinesP(edges, lines, 1, // distance resolution measure in pixel-related unit
                Math.PI/45, // angle resolution in radians
                100, // threshold
                30, // min line width
                10); // gap between lines
        double angle = 0.;
        int j =0;
        for(int i = 0; i<lines.height(); i++){
            double result =  (lines.get(i, j)[1] - lines.get(i, j)[0])/lines.get(i, j)[1] - lines.get(i, j)[0];
            angle+=Math.atan(result)*57.2957795;
        }
        Double avgAngle = angle/lines.height();
        Utilities.rotate(mat, -avgAngle);
    }
    public Image deskew_dotnet(){
        Mat edges = new Mat();
        Canny(_mat, edges,180,120);
        Mat lines = new Mat();
        HoughLinesP(edges, lines, 1, // distance resolution measure in pixel-related unit
                Math.PI/45, // angle resolution in radians
                100, // threshold
                30, // min line width
                10); // gap between lines
        double angle = 0.;
        int j =0;
        for(int i = 0; i<lines.height(); i++){
            double result =  (lines.get(i, j)[1] - lines.get(i, j)[0])/lines.get(i, j)[1] - lines.get(i, j)[0];
            angle+=Math.atan(result)*57.2957795;
        }
        Double avgAngle = angle/lines.height();
        Utilities.rotate(_mat, -avgAngle);
        return this;
    }

    public Image markAreaOfInterest( List<MatOfPoint> contours) {
        for (int i=0; i< contours.size(); i++){
            drawContours(_gray, contours, i, new Scalar(255, 0, 0),1);
        }
        return this;
    }
    public Image removeNoise(Size size){
        if(_mat!=null) {
            size = size!=null?size:new Size(1,1);
            Mat element = getStructuringElement(MORPH_RECT, size);
            erode(_mat, _mat, element, new Point(-1, -1), 1, Core.BORDER_REFLECT);
        }
        return this;
    }
    public Image morphology(int morphOperation, int horizontalMoveSize, int verticalMoveSize){
        return this.morphology(morphOperation, new Size(horizontalMoveSize, verticalMoveSize));
    }
    public Image morphology(int morphOperation, Size size){
        if(_mat!=null){
            Mat sqKernel = getStructuringElement(MORPH_RECT, size);
            morphologyEx(_mat, _mat, morphOperation,sqKernel);
        }
        return this;
    }
    public Image highlightEveryThing(){
        if(_mat!=null) {
            Mat grad_x = new Mat();
            Mat abs_grad_x = new Mat();
            Sobel(_mat, grad_x, CvType.CV_32F, 1, 0, 3);
            Core.convertScaleAbs(grad_x, abs_grad_x);
            Core.MinMaxLocResult mmlr = Core.minMaxLoc(abs_grad_x);
            Mat absMinDiff = new Mat();
            Core.subtract(abs_grad_x, Scalar.all(mmlr.minVal), absMinDiff);
            double minMaxDiff = mmlr.maxVal - mmlr.minVal;
            Core.divide(absMinDiff, Scalar.all(minMaxDiff), abs_grad_x);
            Core.multiply(abs_grad_x, Scalar.all(255), abs_grad_x);
            abs_grad_x.copyTo(_mat);
        }
        return this;
    }
    public Image erodeImage(Size size){
        Mat rectangleKernel = getStructuringElement(MORPH_RECT, size);
        erode(_mat, _mat, rectangleKernel);
        return this;
    }

}
