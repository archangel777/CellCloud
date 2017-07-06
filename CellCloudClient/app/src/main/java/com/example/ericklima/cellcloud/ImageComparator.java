package com.example.ericklima.cellcloud;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.android.Utils;

public class ImageComparator {

    private Mat img;
    private MatOfKeyPoint keyPoints;

    FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
    DescriptorExtractor SurfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);

    static{ System.loadLibrary("opencv_java3"); }

    public ImageComparator(Bitmap mainImage) {
        img = new Mat();
        keyPoints = new MatOfKeyPoint();
        Bitmap bmp32 = mainImage.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, img);
        detector.detect(img, keyPoints);
        Log.d("LOG!", "number of query KeyPoints= " + keyPoints.size());
    }

    public void compareImage(Bitmap toCompare) {
        Mat imgToComp = new Mat();
        MatOfKeyPoint logoKeyPoints = new MatOfKeyPoint();
        Bitmap bmp32 = toCompare.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, imgToComp);

        //extract keypoints
        detector.detect(imgToComp, logoKeyPoints);
        Log.d("LOG!", "number of logo KeyPoints= " + logoKeyPoints.size());

        //Descript keypoints
        long time2 = System.currentTimeMillis();
        Mat descriptors = new Mat();
        Mat logoDescriptors = new Mat();
        Log.d("LOG!", "logo type" + img.type() + "  inType" + imgToComp.type());
        SurfExtractor.compute(img, keyPoints, descriptors);
        SurfExtractor.compute(imgToComp, logoKeyPoints, logoDescriptors);
        Log.d("LOG!", "Description time elapsed" + (System.currentTimeMillis()- time2));
    }
}
