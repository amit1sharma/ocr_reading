package com.adcb.ocr.engine.image;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.photo.Photo.fastNlMeansDenoisingColored;

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
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.adcb.ocr.config.ApplicationStatupConfigurator;
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

    public Mat get_original_image(){
        return this._original_image;
    }

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
    }
    public Image cropLowerHalf(){
        int newHeight = _mat.height()/2;
        Rect rect = new Rect(0,newHeight,_mat.width(),newHeight);
        Mat newMat = new Mat(_mat, rect);
        newMat.copyTo(_mat);
        return this;
    }
    public Image gaussianBlur(Size... size){
        Size s = size.length>0?size[0]:new Size(11,11);
        GaussianBlur(this._mat, this._mat, s,0);
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public float calculatePPI(){
        return Utilities.calculateDPI(this._mat.height(), this._mat.width());
    }
    public Image resizeWidth(int width) throws Exception {
        if(_mat!=null){
            if(_mat.width() >= width){
                Imgproc.resize(_mat, _mat, new Size(width, _mat.height()));
            }
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }
    public Image resizeHeight(int height) throws Exception {
        if(_mat!=null){
            if(_mat.height() >= height){
                Imgproc.resize(_mat, _mat, new Size(_mat.width(), height));
            }
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image greyScale(){
        if(_mat!=null){
            greyScale(_mat);
            _mat.copyTo(_gray);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }
    public Image greyScale(Mat mat){
        if(_mat!=null){
            cvtColor(mat, mat, COLOR_BGR2GRAY);
//            _mat.copyTo(_gray);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }
    public Image rotateImage(Double angle){
    	if(_mat!=null){
    		Utilities.rotate(_mat, angle);
    	}
    	return this;
    }

    public Image rotateMat(Double angle){
	    if (angle == 270 || angle == -90){
	        // Rotate clockwise 270 degrees
	    	Core.transpose(_mat, _mat);
	    	Core.flip(_mat, _mat, 0);
	    }
	    else if (angle == 180 || angle == -180){
	        // Rotate clockwise 180 degrees
	    	Core.flip(_mat, _mat, -1);
	    }
	    else if (angle == 90 || angle == -270){
	        // Rotate clockwise 90 degrees
	    	Core.transpose(_mat, _mat);
	    	Core.flip(_mat, _mat, 1);
	    }
	    else if (angle == 360 || angle == 0 || angle == -360){

	    }
	    else {
	        Utilities.rotate(_mat, angle);
	    }
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
//            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
//            imwrite(imageAbsoluteTargetPath+File.separator+System.currentTimeMillis()+imageName, _mat);
            save(imageAbsoluteTargetPath+File.separator+System.currentTimeMillis()+imageName);
        }
        return this;
    }
    public Image save(String imageAbsoluteTargetPath){
        if(_mat!=null){
//            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
//            imwrite(imageAbsoluteTargetPath+File.separator+imageName, _mat);
            save(_mat, imageAbsoluteTargetPath);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

    public Image save(Mat mat){
        save(mat, null);
        return this;
    }

    public Image save(Mat mat, String imageAbsoluteTargetPathWithImageName){
        if(mat!=null){
            String imageNameWithPath = imageAbsoluteTargetPathWithImageName!=null?imageAbsoluteTargetPathWithImageName:this.imageAbsoluteTargetPath+File.separator+imageName;
            if("true".equalsIgnoreCase(ApplicationStatupConfigurator.saveStageImages)){
            	imwrite(imageNameWithPath, mat);
            } else{
            	System.out.println("not saving image as not saveStageImage is not set in properties");
            }
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }

/*    public Image save1(Mat mat){
        if(_mat!=null){
            imageAbsoluteTargetPath = imageAbsoluteTargetPath!=null?imageAbsoluteTargetPath:this.imageAbsoluteTargetPath;
            imwrite(imageAbsoluteTargetPath+File.separator+System.currentTimeMillis()+imageName, mat);
        }
        //operationPerformed.add(Image.class.getEnclosingMethod().getName());
        return this;
    }*/

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

    /**
     * set applynoise false . default is true
     * @param applyDenoise
     * @return
     * @throws Exception
     */
    public String getRoiImagePath(boolean... applyDenoise) throws Exception {
        String roiImagePath = imageAbsoluteTargetPath;
        if(getSaveROI(allContours, roiImagePath, applyDenoise)){
            return roiImagePath;
        } else{
            throw new Exception ("Unable to find and ROI out of contours");
        }
    }

    public boolean getSaveROI( List<MatOfPoint> contours, String path, boolean[] applyDenoise) throws Exception {
        boolean found = false;
//        markAreaOfInterest(contours);
//        save(_original_image, path);
        for(MatOfPoint m : contours){
            Rect rect = boundingRect(m);
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
                Mat finalRoi = new Mat(_original_image,rect);
//                save(finalRoi, path+File.separator+imageName);
                preprocessROI(finalRoi, applyDenoise);
                save(finalRoi, path+File.separator+imageName);
                found = true;
                break;
            }
        }
        return found;
    }
    public void preprocessROI( Mat finalRoi, boolean[] applyDenoise){
        try {
                deskewThis(finalRoi);

                long stime = System.currentTimeMillis();
                if(applyDenoise.length>0){
                	if(applyDenoise[0]){
                		denoise(finalRoi);
                	}
                } else{
                	denoise(finalRoi);
                }
                System.out.println("time taken : " +(System.currentTimeMillis() - stime));
//                save(finalRoi, null);
                greyScale(finalRoi);
             //   adaptiveThreshold(finalRoi, finalRoi, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);
//                save(finalRoi);
//                morphologyEx(finalRoi, finalRoi, MORPH_BLACKHAT, rectangleKernel);
             //   Mat squareKernel = getStructuringElement(MORPH_RECT, new Size(1, 1));
          //      erode(finalRoi, finalRoi, squareKernel);
//                Core.bitwise_not(finalRoi, finalRoi);
//                save(finalRoi);

        } catch (Exception e){
//            e.printStackTrace();
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
    	deskewThis(_mat);
        /*Size size = _mat.size();
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
        Utilities.rotate(_mat, angle);*/
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


    public Image markAreaOfInterest(Mat mat,  List<MatOfPoint> contours) {
        for (int i=0; i< contours.size(); i++){
            drawContours(mat, contours, i, new Scalar(255, 0, 0),3);
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

    public Image denoise(){
    	return denoise(_mat);
    }
    public Image denoise(Mat mat){
    	fastNlMeansDenoisingColored(mat, mat,10,10,7,21);
    	return this;
    }

    public String getImagePath() throws Exception {
    	save(_mat,imageAbsoluteTargetPath+File.separator+imageName);
        return imageAbsoluteTargetPath;
    }
    public Image getPerspectiveImage(){
        MatOfPoint2f finalPoints = edgeDetection();
        fourPointTransform(finalPoints);
        return this;
    }

    public MatOfPoint2f edgeDetection(){
        this.greyScale()
                .gaussianBlur(new Size(5, 5));
        //save();
        Canny(_mat, _mat, 110, 1);
        //save();
//        Mat squareKernel = getStructuringElement(MORPH_RECT, new Size(1, 1));
//        dilate(_mat, _mat, squareKernel);
//        this.erodeImage(new Size(1, 1));
        horizontalConnect(3,3);
        //save();
        List<MatOfPoint> mop = getRectangles();
//        markAreaOfInterest(_original_image, mop);
//        save(_original_image);
        MatOfPoint2f finalPoints = new MatOfPoint2f();
        for(MatOfPoint m : mop ) {

            MatOfPoint2f thisContour2f = new MatOfPoint2f();
            m.convertTo(thisContour2f, CvType.CV_32FC2);
            MatOfPoint2f mopApprox = new MatOfPoint2f();

            double arc = arcLength(thisContour2f, true);
            approxPolyDP(thisContour2f, mopApprox, 0.02 * arc, true);
            if (mopApprox.size().height == 4) {
                mopApprox.copyTo(finalPoints);
                break;
            }
        }
        Mat mm = new Mat(_original_image, boundingRect(finalPoints));
        //save(mm, null);
        return finalPoints;
    }
    private void fourPointTransform(MatOfPoint2f mopApprox){

        MatOfPoint2f ordered = Utilities.orderPointsClockwise(mopApprox);
        Point[] pts = ordered.toArray();

        Point tl = pts[0];
        Point tr = pts[1];
        Point bl = pts[2];
        Point br = pts[3];

        double widthTop = tr.x - tl.x;
        double widthBottom = br.x - bl.x;
        Double maxWidth = Math.max(widthTop, widthBottom);

        double heightLeft = bl.y - tl.y;
        double heightRight = br.y - tr.y;
        Double maxHeight = Math.max(heightLeft, heightRight);


        Mat destImage = new Mat(maxHeight.intValue(), maxWidth.intValue(), _original_image.type());
        Mat src = new MatOfPoint2f(
                pts[0],
                pts[1],
                pts[2],
                pts[3]);
        Mat dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(destImage.width() - 1, 0),
                new Point(destImage.width() - 1, destImage.height() - 1),
                new Point(0, destImage.height() - 1));

        Mat transform = Imgproc.getPerspectiveTransform(src, dst);
        Imgproc.warpPerspective(_original_image, destImage, transform, destImage.size());
        destImage.copyTo(_mat);
        destImage.copyTo(_original_image);
    }

    public Image removeWaterMark(boolean dilate) {
        // approximate the background
        Mat bg = _gray.clone();
        for (int r = 1; r < 5; r++) {
            Mat kernel2 = getStructuringElement(MORPH_ELLIPSE, new Size(5 * r + 1, 5 * r + 1));
            morphologyEx(bg, bg, MORPH_CLOSE, kernel2);
            morphologyEx(bg, bg, MORPH_OPEN, kernel2);
        }
        
// difference = background - initial
        Mat dif = new Mat();
        Core.subtract(bg, _gray, dif);
        //save(dif);
// threshold the difference image so we get dark letters

        threshold(dif, dif, 0, 255, THRESH_BINARY_INV | THRESH_OTSU);
        if(dilate){
            Mat squareKernel = getStructuringElement(MORPH_RECT, new Size(2, 2));
            dilate(dif, dif, squareKernel);
        }
        //save(dif);
        dif.copyTo(_mat);
        dif.copyTo(_original_image);
// threshold the background image so we get dark region
        /*Mat dark = new Mat();
        threshold(bg, dark, 0, 255, THRESH_BINARY_INV | THRESH_OTSU);
        save(dark);
// extract pixels in the dark region
        double[][] darkpix = new double[Core.countNonZero(dark)][];
        int index = 0;
        for (int r = 0; r < dark.height(); r++) {
            for (int c = 0; c < dark.width(); c++) {
                if (_gray.get(r, c) != null) {
                    darkpix[index++] = _gray.get(r, c);
                }
            }
        }
// threshold the dark region so we get the darker pixels inside it
        Mat darkpixMat = new Mat();
//        darkpixMat.put(0, 0, );
//        darkpixMat.pu
        threshold(darkpixMat, darkpixMat, 0, 255, THRESH_BINARY | THRESH_OTSU);
// paste the extracted darker pixels
        index = 0;
        for (int r = 0; r < dark.height(); r++) {
            for (int c = 0; c < dark.width(); c++) {
                if (darkpix[index++] != null) {
                    bw.put(r, c, darkpix[index++]);
                }
            }
        }*/

        return this;
    }


}