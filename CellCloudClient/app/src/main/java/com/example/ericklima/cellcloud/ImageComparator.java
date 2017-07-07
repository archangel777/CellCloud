package com.example.ericklima.cellcloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ImageComparator {

    private MainScreen context;
    private InetAddress address;
    public static final int min_matches = 70;
    public static final int min_dist = 30;

    private Bitmap b1;
    private Mat hist1;

    public ImageComparator(MainScreen c, Bitmap b1, InetAddress address) {
        context = c;
        this.address = address;

        this.b1 = Bitmap.createScaledBitmap(b1, 100, 100, true);
        Mat img1 = new Mat();
        Utils.bitmapToMat(b1, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
        img1.convertTo(img1, CvType.CV_32F);
        hist1 = new Mat();
        MatOfInt histSize = new MatOfInt(180);
        MatOfInt channels = new MatOfInt(0);
        ArrayList<Mat> bgr_planes1 = new ArrayList<>();
        Core.split(img1, bgr_planes1);
        MatOfFloat histRanges = new MatOfFloat(0f, 180f);
        Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, false);
        Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
        img1.convertTo(img1, CvType.CV_32F);
        hist1.convertTo(hist1, CvType.CV_32F);
    }

    public void run(Bitmap b2) {
        if (b2 != null) {
            b2 = Bitmap.createScaledBitmap(b2, 100, 100, true);
            Mat img2 = new Mat();
            Utils.bitmapToMat(b2, img2);
            Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
            img2.convertTo(img2, CvType.CV_32F);
            //Log.d("ImageComparator", "img1:"+img1.rows()+"x"+img1.cols()+" img2:"+img2.rows()+"x"+img2.cols());
            Mat hist2 = new Mat();
            MatOfInt histSize = new MatOfInt(180);
            MatOfInt channels = new MatOfInt(0);
            ArrayList<Mat> bgr_planes2 = new ArrayList<Mat>();
            Core.split(img2, bgr_planes2);
            MatOfFloat histRanges = new MatOfFloat(0f, 180f);
            Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, false);
            Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
            img2.convertTo(img2, CvType.CV_32F);
            hist2.convertTo(hist2, CvType.CV_32F);

            double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
            Log.d("ImageComparator", "compare: " + compare);

            if (compare < 4000) {
                Log.d("DUPLICATE", "Found duplicate!!");
                new TargetSender(context, address).execute(b2);
            }
            //new asyncTask(context, b1, b2, address).execute();

        }
    }

    private static class asyncTask extends AsyncTask<Void, Void, Void> {
        private static Mat img1, img2, descriptors, dupDescriptors;
        private static FeatureDetector detector;
        private static DescriptorExtractor DescExtractor;
        private static DescriptorMatcher matcher;
        private static MatOfKeyPoint keypoints, dupKeypoints;
        private static MatOfDMatch matches, matches_final_mat;
        private static boolean foundDuplicate = false;
        private MainScreen asyncTaskContext=null;
        private Bitmap bmpimg1, bmpimg2;
        private InetAddress address;

        private static Scalar RED = new Scalar(255,0,0);
        private static Scalar GREEN = new Scalar(0,255,0);

        public asyncTask(MainScreen context, Bitmap b1, Bitmap b2, InetAddress address) {
            asyncTaskContext=context;
            bmpimg1 = b1;
            bmpimg2 = b2;
            this.address = address;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            compare();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                List<DMatch> finalMatchesList = matches_final_mat.toList();

                if (!foundDuplicate && finalMatchesList.size() > min_matches) {
                    foundDuplicate = true;
                    Log.d("DUPLICATE", "Found duplicate!!");
                    new TargetSender(asyncTaskContext, address).execute(bmpimg2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(asyncTaskContext, e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        void compare() {
            try {
                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
                img1 = new Mat();
                img2 = new Mat();
                Utils.bitmapToMat(bmpimg1, img1);
                Utils.bitmapToMat(bmpimg2, img2);
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2RGB);
                detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
                DescExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
                matcher = DescriptorMatcher
                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                keypoints = new MatOfKeyPoint();
                dupKeypoints = new MatOfKeyPoint();
                descriptors = new Mat();
                dupDescriptors = new Mat();
                matches = new MatOfDMatch();
                detector.detect(img1, keypoints);
                Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
                detector.detect(img2, dupKeypoints);
                Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
                // Descript keypoints
                DescExtractor.compute(img1, keypoints, descriptors);
                DescExtractor.compute(img2, dupKeypoints, dupDescriptors);
                Log.d("LOG!", "number of descriptors= " + descriptors.size());
                Log.d("LOG!",
                        "number of dupDescriptors= " + dupDescriptors.size());
                // matching descriptors
                matcher.match(descriptors, dupDescriptors, matches);
                Log.d("LOG!", "Matches Size " + matches.size());
                // New method of finding best matches
                List<DMatch> matchesList = matches.toList();
                List<DMatch> matches_final = new ArrayList<DMatch>();
                for (int i = 0; i < matchesList.size(); i++) {
                    if (matchesList.get(i).distance <= min_dist) {
                        matches_final.add(matches.toList().get(i));
                    }
                }

                matches_final_mat = new MatOfDMatch();
                matches_final_mat.fromList(matches_final);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
