package com.example.ericklima.cellcloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;

/**
 * Created by ErickLima on 01/07/2017.
 */

public class FaceCropper {
    private ImageView imageView;
    private ProgressBar progressBar;
    private Button button;
    private int viewHeight, viewWidth;
    private BitmapDrawable drawable;
    private FaceDetector myFaceDetect;
    private FaceDetector.Face[] myFace;
    private float myEyesDistance;

    public FaceCropper(ImageView imageView, ProgressBar progressBar, Button button) {
        this.imageView = imageView;
        this.progressBar = progressBar;
        this.button = button;
    }

    public void cropImage() {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                viewHeight = imageView.getMeasuredHeight();
                viewWidth = imageView.getMeasuredWidth();
                drawable = (BitmapDrawable) imageView.getDrawable();
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setClickable(true);
                button.setEnabled(false);
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap resizedBitmap = null;
                try {
                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);

                    Bitmap bitmapOrg = drawable.getBitmap();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    byte[] bytes = stream.toByteArray();

                    int targetWidth = bitmapOrg.getWidth();
                    int targetHeight = bitmapOrg.getHeight();

                    Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                            targetHeight, Bitmap.Config.ARGB_8888);

                    RectF rectf = new RectF(0, 0, viewWidth, viewHeight);

                    Canvas canvas = new Canvas(targetBitmap);
                    Path path = new Path();

                    path.addRect(rectf, Path.Direction.CW);
                    canvas.clipPath(path);

                    canvas.drawBitmap(
                            bitmapOrg,
                            new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg
                                    .getHeight()), new Rect(0, 0, targetWidth,
                                    targetHeight), paint);

                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);

                    BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                    bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                    bitmapOrg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapFactoryOptions);

                    myFace = new FaceDetector.Face[5];
                    myFaceDetect = new FaceDetector(targetWidth, targetHeight,
                            5);
                    int numberOfFaceDetected = myFaceDetect.findFaces(
                            bitmapOrg, myFace);

                    Log.d("Faces Detected", "" + numberOfFaceDetected);

                    if (numberOfFaceDetected > 0) {
                        PointF myMidPoint;
                        FaceDetector.Face face = myFace[0];
                        myMidPoint = new PointF();
                        face.getMidPoint(myMidPoint);
                        myEyesDistance = face.eyesDistance();

                        if (myMidPoint.x + viewWidth > targetWidth) {
                            while (myMidPoint.x + viewWidth > targetWidth) {
                                myMidPoint.x--;
                            }
                        }
                        if (myMidPoint.y + viewHeight > targetHeight) {
                            while (myMidPoint.y + viewHeight > targetHeight) {
                                myMidPoint.y--;
                            }
                        }

                        int x = contain((int) (myMidPoint.x - 2*myEyesDistance), 0, bitmapOrg.getWidth());
                        int y = contain((int) (myMidPoint.y - 1.5*myEyesDistance), 0, bitmapOrg.getHeight());
                        int sizeX = Math.min(Math.min((int) (4* myEyesDistance), bitmapOrg.getWidth()-x), bitmapOrg.getHeight()-y);
                        int sizeY = Math.min(Math.min((int) (4* myEyesDistance), bitmapOrg.getWidth()-x), bitmapOrg.getHeight()-y);
                        Log.d("Sizes", x + " | " + y + " | " + sizeX + " | " + sizeY);
                        resizedBitmap = Bitmap.createBitmap(
                                bitmapOrg, x, y, sizeX, sizeY, matrix, true);
                    } else {
                        resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                                viewWidth, viewHeight, matrix, true);
                    }

                } catch (Exception e) {
                    System.out.println("Error1 : " + e.getMessage()
                            + e.toString());
                }
                return resizedBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                imageView.setImageBitmap(bitmap);
                progressBar.setVisibility(View.GONE);
                progressBar.setClickable(false);
                button.setEnabled(true);
            }
        }.execute();

    }

    private int contain(int n, int min, int max) {
        return Math.min(Math.max(n, min), max);
    }

}
