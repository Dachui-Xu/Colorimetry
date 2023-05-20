package com.example.colorimetry;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageMethod {

    public static double[][] HSV_VALUE_HIGH = {
            new double[]{10.0d, 255.0d, 255.0d},
            new double[]{25.0d, 255.0d, 255.0d},
            new double[]{34.0d, 255.0d, 255.0d},
            new double[]{77.0d, 255.0d, 255.0d},
            new double[]{99.0d, 255.0d, 255.0d},
            new double[]{124.0d, 255.0d, 255.0d},
            new double[]{155.0d, 255.0d, 255.0d},
            new double[]{180.0d, 255.0d, 46.0d},
            new double[]{180.0d, 43.0d, 220.0d},
            new double[]{180.0d, 30.0d, 255.0d},
            new double[]{180.0d, 255.0d, 255.0d}};
    public static double[][] HSV_VALUE_LOW = {
            new double[]{0.0d, 43, 46},
            new double[]{11.0d, 43, 46},
            new double[]{26.0d, 43, 46},
            new double[]{35.0d, 43, 46},
            new double[]{78.0d, 43, 46},
            new double[]{100.0d,43, 46},
            new double[]{125.0d, 43, 46},//6
            new double[]{0.0d, 0.0d, 0.0d},
            new double[]{0.0d, 0.0d, 46.0d},
            new double[]{0.0d, 0.0d, 221.0d},
            new double[]{156.0d, 43d, 0d}};
   public static void HSV_S_V(int s, int v){
       HSV_VALUE_LOW = new double[][]{
               new double[]{0.0d, s, v},
               new double[]{11.0d, s, v},
               new double[]{26.0d, s, v},
               new double[]{35.0d, s, v},
               new double[]{78.0d, s, v},
               new double[]{100.0d, s, v},
               new double[]{125.0d, s, v},//6
               new double[]{0.0d, 0.0d, 0.0d},
               new double[]{0.0d, 0.0d, 46.0d},
               new double[]{0.0d, 0.0d, 221.0d},
               new double[]{156.0d, 43d, 0d}};
   }

    public static Mat BitmapToMat(Bitmap bmp) {
        Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp, mat);
        return mat;
    }

    public static void getGrayImage(Bitmap bmp) {
        Mat mat_bmp = BitmapToMat(bmp);
        Mat mat_gray = new Mat();
        Imgproc.cvtColor(mat_bmp, mat_gray, 10, 1);
        Utils.matToBitmap(mat_gray, bmp);
    }

    public static Bitmap detectColor(Bitmap bmp, int color) {
        Mat m = BitmapToMat(bmp);
        Mat hsv = new Mat();
        Imgproc.cvtColor(m, hsv, Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_BGR2HSV);
        Mat img = new Mat();
        Core.inRange(hsv, new Scalar(HSV_VALUE_LOW[color]), new Scalar(HSV_VALUE_HIGH[color]), img);
        Mat kernel = Imgproc.getStructuringElement(0, new Size(3.0d, 3.0d));
        Imgproc.erode(img, img, kernel);
        Imgproc.erode(img, img, kernel);
        Utils.matToBitmap(img, bmp);
        return bmp;
    }

    public  double detectVariousColor(Bitmap bmp, int colorMin, int colorMax) {
        //按颜色提取部分
        Mat m = BitmapToMat(bmp);
        Mat hsv = new Mat();
        Mat bgr = new Mat();
        Mat filer = new Mat();
        Mat hsv2 = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        if(!MainActivity.filerFlag){
            Imgproc.cvtColor(m, filer, Imgproc.COLOR_RGBA2BGR);
        }else{
            Imgproc.cvtColor(m, bgr, Imgproc.COLOR_RGBA2BGR);
            //Imgproc.erode(bgr,filer,kernel);//腐蚀，最小值滤波，用最小值替换中心像素
            Imgproc.bilateralFilter(bgr,filer,0,150,15);//高斯双边滤波
        }
        Imgproc.cvtColor(filer, hsv2, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv2, new Scalar(HSV_VALUE_LOW[colorMin]), new Scalar(HSV_VALUE_HIGH[colorMax]), hsv);

        MainActivity.progressCount = 8;

        Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_CLOSE, kernel);
        //给轮廓绘制曲线
        Mat outMat = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(hsv, contours, outMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.cvtColor(filer, filer, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(filer, bmp);
        Scalar scalar = new Scalar(255, 215, 0);
        Imgproc.drawContours(filer, contours, -1, scalar, 2);
        Imgproc.cvtColor(filer, filer, Imgproc.COLOR_RGBA2BGR);//后续还需添加文字，所以转回BGR

        MainActivity.progressCount = 17;

        Mat rawDist = new Mat(hsv.size(), CvType.CV_32FC1);
        int n =contours.size();//轮廓数量
        double resultValue = 0; //RGB处理结果
        ArrayList<Double> resultAll = new ArrayList<>();
        ArrayList<Double> resultTemp = new ArrayList<>();
        MatOfPoint2f matOfPoint2fNew = new MatOfPoint2f();
        int row = hsv.rows();
        int col = hsv.cols();
        int[] pixels = new int[row*col];
        bmp.getPixels(pixels,0,col,0,0,col,row);
        int index = 0;
        int red = 0,green =0,blue = 0;
        double sum = 0.0;
        double eAverage = 0.0;
        int z4 = 0;

        MainActivity.progressCount = 25;
        int q =n;
        while (n>0){
            resultTemp.clear();

            //矩形
            Rect rect = Imgproc.boundingRect(contours.get(n-1));
            int xL = rect.x ;
            int yL = rect.y ;
            int xR = xL + rect.width ;
            int yR = yL + rect.height ;

        for (int j = yL; j < yR; j++) {
            for (int i = xL; i < xR; i++) {
                matOfPoint2fNew.fromList(contours.get(n-1).toList());
                rawDist.put(j, i, Imgproc.pointPolygonTest(matOfPoint2fNew, new Point(i, j), false));
                if (rawDist.get(j, i)[0] == 1) { //内部
                    index = col*j+i;
                    red = (pixels[index]>>16)&0xff;
                    green = (pixels[index]>>8)&0xff;
                    blue  = pixels[index]&0xff;
                   /* int rgbPixel = bitmap.getPixel(j, i);
                    int red = Color.red(rgbPixel);
                    int green = Color.green(rgbPixel);
                    int blue = Color.blue(rgbPixel);*/
                    if ((red !=255 || green != 255 || blue != 255) && ((red !=0 || green != 0 || blue != 0))) {
                        //System.out.println(red + " , " + green + " , " + blue);
                        resultValue = Math.sqrt((double) ((blue * blue) + (green * green) + (red * red)));
                        //resultAll.add(resultValue);
                        resultTemp.add(resultValue);
                    }
                }
            }
        }

        MainActivity.progressCount += 40/q;
            eAverage =0.0;
            sum = 0.0;
            z4 =0;
            for (Double aDouble : resultTemp) {
                z4++;
                sum += aDouble;
                eAverage = sum / z4;
            }
            resultAll.add(eAverage);
            Imgproc.putText(filer,n+". "+(float)eAverage,new Point(0,25*n),Imgproc.FONT_HERSHEY_COMPLEX_SMALL,1,new Scalar(0,255,0),2,4,false);
            Imgproc.putText(filer,""+(float)eAverage,new Point(xL+rect.width/2-50,yL+rect.height/2),Imgproc.FONT_HERSHEY_COMPLEX_SMALL, 1,new Scalar(0,0,255),1,4,false);
            System.out.println(rect.height * rect.width);
            MainActivity.progressCount += 15/q;
            n--;
        }
            Imgproc.cvtColor(filer, filer, Imgproc.COLOR_BGR2RGBA);
            Utils.matToBitmap(filer, bmp);
           sum = 0.0;
            eAverage = 0.0;
            z4 = 0;
            for (Double aDouble : resultAll) {
                z4++;
                sum += aDouble;
                eAverage = sum / z4;
            }
        MainActivity.progressCount =88;
        m.release();
        bgr.release();
        filer.release();
        hsv.release();
        hsv2.release();
        rawDist.release();
        return eAverage;
    }
    //按照颜色绘制轮廓
    public void detectColorRange(Bitmap bmp, int colorMin, int colorMax){
        //按颜色提取部分
        Mat m = BitmapToMat(bmp);
        Mat hsv = new Mat();
        Mat bgr = new Mat();
        Mat filer = new Mat();
        Mat hsv2 = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        if(!MainActivity.filerFlag){
            Imgproc.cvtColor(m, filer, Imgproc.COLOR_RGBA2BGR);
        }else{
            Imgproc.cvtColor(m, bgr, Imgproc.COLOR_RGBA2BGR);
            //Imgproc.erode(bgr,filer,kernel);//腐蚀，最小值滤波，用最小值替换中心像素
            Imgproc.bilateralFilter(bgr,filer,0,150,15);//高斯双边滤波
        }

        Imgproc.cvtColor(filer, hsv2, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv2, new Scalar(HSV_VALUE_LOW[colorMin]), new Scalar(HSV_VALUE_HIGH[colorMax]), hsv);

        Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_CLOSE, kernel);
        Mat outMat = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(hsv, contours, outMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //绘制矩形框
/*        for(int i =0;i<contours.size();i++){
        Rect rect = Imgproc.boundingRect(contours.get(i));
        Imgproc.rectangle(filer,rect.tl(),rect.br(),new Scalar(0, 0, 255),1,4);}*/


        Scalar scalar = new Scalar(0, 0, 255);
        Imgproc.drawContours(filer, contours, -1, scalar, 2);
        Imgproc.cvtColor(filer, filer, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(filer, bmp);
        //Utils.matToBitmap(hsv, bmp);
        m.release();
        bgr.release();
        filer.release();
        hsv.release();
        hsv2.release();
    }


    public  Bitmap detectContours(Bitmap bmp, Bitmap bmpOriginal) {
        Bitmap bitmap = bmpOriginal;
        Mat markerMask = BitmapToMat(bmp);
        Mat markerMask1 = BitmapToMat(bmpOriginal);
        Imgproc.cvtColor(markerMask, markerMask, 6);
        Imgproc.threshold(markerMask, markerMask, 0.0d, 255.0d, 8);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(markerMask, contours, hierarchy, 2, 2);
        Imgproc.drawContours(markerMask1, contours, -1, new Scalar(156.0d, 43.0d, 46.0d), 4);
        System.out.println("轮廓数量：" + contours.size());
        System.out.println("hierarchy类型：" + hierarchy);
        Utils.matToBitmap(markerMask1, bitmap);
        return bitmap;
    }
    //边缘提取
    private void edge2Demo(Mat src,Mat dst){
        //x方向梯度
        Mat gradX = new Mat();
        Imgproc.Sobel(src,gradX,CvType.CV_16S,1,0);
        //y方向梯度
        Mat gradY = new Mat();
        Imgproc.Sobel(src, gradY, CvType.CV_16S, 0, 1);
        //边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(gradX,gradY,edges,50,150);
        Core.bitwise_and(src, src, dst, edges);
        //释放内存
        edges.release();
        gradX.release();
        gradY.release();
    }
}

