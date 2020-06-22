package com.adcb.ocr.engine.image;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GetPerspective {
    public void findPerspective(){
//        order
    }

    MatOfPoint2f orderPointsClockwise(MatOfPoint2f screenCnt2f) {
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

//    public void orderPoints(Mat approx){
//        //calculate the center of mass of our contour image using moments
//        Moments moment = Imgproc.moments(approx.get(0));
//        int x = (int) (moment.get_m10() / moment.get_m00());
//        int y = (int) (moment.get_m01() / moment.get_m00());
//
//        //SORT POINTS RELATIVE TO CENTER OF MASS
//        Point[] sortedPoints = new Point[4];
//
//        double[] data;
//        int count = 0;
//        for(int i=0; i<approx.get(0).rows(); i++){
//            data = approx.get(0).get(i, 0);
//            double datax = data[0];
//            double datay = data[1];
//            if(datax < x && datay < y){
//                sortedPoints[0]=new Point(datax,datay);
//                count++;
//            }else if(datax > x && datay < y){
//                sortedPoints[1]=new Point(datax,datay);
//                count++;
//            }else if (datax < x && datay > y){
//                sortedPoints[2]=new Point(datax,datay);
//                count++;
//            }else if (datax > x && datay > y){
//                sortedPoints[3]=new Point(datax,datay);
//                count++;
//            }
//        }
//    }

}
