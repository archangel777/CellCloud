package com.example.ericklima.cellcloud;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.ericklima.cellcloud.network.DirectSender;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ImageComparator {

    private MainScreen context;
    private InetAddress address;
    public static final int min_matches = 60;
    public static final int min_dist = 120;
    public static final int DUPLICATE_LIMIT = 10000;

    private Bitmap b1;
    private Mat hist1;

    public ImageComparator(MainScreen c, Bitmap b1, InetAddress address) {
        context = c;
        this.address = address;
        this.b1 = Bitmap.createScaledBitmap(b1, 100, 100, true);
        hist1 = getHistogram(this.b1);
    }

    public void run(Bitmap b2) {
        b2 = Bitmap.createScaledBitmap(b2, 100, 100, true);
        if (b2 != null) {
            Mat hist2 = getHistogram(b2);

            double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
            Log.d("ImageComparator", "compare: " + compare);

            if (compare < DUPLICATE_LIMIT || newCompare(b1, b2)) {
                Log.d("DUPLICATE", "Found duplicate!!");
                new DirectSender(context, address).execute(b2);
            }
            //new asyncTask(context, b1, b2, address).execute();

        }
    }

    private Mat getHistogram(Bitmap b) {
        Mat img = new Mat();
        Utils.bitmapToMat(b, img);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2GRAY);
        img.convertTo(img, CvType.CV_32F);

        /* Test
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        img = Mat.zeros(img.size(), CvType.CV_32F);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(img, contours, contourIdx, new Scalar(0, 0, 255), -1);
        }
        /* ----
        //Log.d("ImageComparator", "img1:"+img1.rows()+"x"+img1.cols()+" img2:"+img2.rows()+"x"+img2.cols());
        */
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(1000);
        MatOfInt channels = new MatOfInt(0);
        ArrayList<Mat> bgr_planes2 = new ArrayList<>();
        Core.split(img, bgr_planes2);
        MatOfFloat histRanges = new MatOfFloat(0f, 1000f);
        Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist, histSize, histRanges, false);
        Core.normalize(hist, hist, 0, hist.rows(), Core.NORM_MINMAX, -1, new Mat());
        img.convertTo(img, CvType.CV_32F);
        hist.convertTo(hist, CvType.CV_32F);
        return hist;
    }

    private boolean newCompare(Bitmap b1, Bitmap b2) {
        try {
            b1 = b1.copy(Bitmap.Config.ARGB_8888, true);
            b2 = b2.copy(Bitmap.Config.ARGB_8888, true);
            Mat img1 = new Mat();
            Mat img2 = new Mat();
            Utils.bitmapToMat(b1, img1);
            Utils.bitmapToMat(b2, img2);
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
            DescriptorExtractor descExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
            DescriptorMatcher matcher = DescriptorMatcher
                    .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            MatOfKeyPoint dupKeypoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();
            Mat dupDescriptors = new Mat();
            MatOfDMatch matches = new MatOfDMatch();
            detector.detect(img1, keypoints);
            //Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
            detector.detect(img2, dupKeypoints);
            //Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
            // Descript keypoints
            descExtractor.compute(img1, keypoints, descriptors);
            descExtractor.compute(img2, dupKeypoints, dupDescriptors);
            //Log.d("LOG!", "number of descriptors= " + descriptors.size());
            //Log.d("LOG!", "number of dupDescriptors= " + dupDescriptors.size());
            // matching descriptors
            matcher.match(descriptors, dupDescriptors, matches);
            //Log.d("LOG!", "Matches Size " + matches.size());
            // New method of finding best matches
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final = new ArrayList<>();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= min_dist) {
                    matches_final.add(matches.toList().get(i));
                }
            }
            Log.d("LOG!", "Final Size " + matches_final.size());
            return matches_final.size() >= min_matches;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    private static class asyncTask extends AsyncTask<Void, Void, Void> {
//        private static Mat img1, img2, descriptors, dupDescriptors;
//        private static FeatureDetector detector;
//        private static DescriptorExtractor DescExtractor;
//        private static DescriptorMatcher matcher;
//        private static MatOfKeyPoint keypoints, dupKeypoints;
//        private static MatOfDMatch matches, matches_final_mat;
//        private static boolean foundDuplicate = false;
//        private MainScreen asyncTaskContext=null;
//        private Bitmap bmpimg1, bmpimg2;
//        private InetAddress address;
//
//        private static Scalar RED = new Scalar(255,0,0);
//        private static Scalar GREEN = new Scalar(0,255,0);
//
//        public asyncTask(MainScreen context, Bitmap b1, Bitmap b2, InetAddress address) {
//            asyncTaskContext=context;
//            bmpimg1 = b1;
//            bmpimg2 = b2;
//            this.address = address;
//        }
//
//        @Override
//        protected Void doInBackground(Void... arg0) {
//            compare();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            try {
//                List<DMatch> finalMatchesList = matches_final_mat.toList();
//
//                if (!foundDuplicate && finalMatchesList.size() > min_matches) {
//                    foundDuplicate = true;
//                    Log.d("DUPLICATE", "Found duplicate!!");
//                    new DirectSender(asyncTaskContext, address).execute(bmpimg2);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(asyncTaskContext, e.toString(), Toast.LENGTH_LONG).show();
//            }
//        }
//
//        void compare() {
//            try {
//                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
//                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
//                img1 = new Mat();
//                img2 = new Mat();
//                Utils.bitmapToMat(bmpimg1, img1);
//                Utils.bitmapToMat(bmpimg2, img2);
//                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
//                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2RGB);
//                detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
//                DescExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
//                matcher = DescriptorMatcher
//                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//
//                keypoints = new MatOfKeyPoint();
//                dupKeypoints = new MatOfKeyPoint();
//                descriptors = new Mat();
//                dupDescriptors = new Mat();
//                matches = new MatOfDMatch();
//                detector.detect(img1, keypoints);
//                Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
//                detector.detect(img2, dupKeypoints);
//                Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
//                // Descript keypoints
//                DescExtractor.compute(img1, keypoints, descriptors);
//                DescExtractor.compute(img2, dupKeypoints, dupDescriptors);
//                Log.d("LOG!", "number of descriptors= " + descriptors.size());
//                Log.d("LOG!",
//                        "number of dupDescriptors= " + dupDescriptors.size());
//                // matching descriptors
//                matcher.match(descriptors, dupDescriptors, matches);
//                Log.d("LOG!", "Matches Size " + matches.size());
//                // New method of finding best matches
//                List<DMatch> matchesList = matches.toList();
//                List<DMatch> matches_final = new ArrayList<DMatch>();
//                for (int i = 0; i < matchesList.size(); i++) {
//                    if (matchesList.get(i).distance <= min_dist) {
//                        matches_final.add(matches.toList().get(i));
//                    }
//                }
//
//                matches_final_mat = new MatOfDMatch();
//                matches_final_mat.fromList(matches_final);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
}
