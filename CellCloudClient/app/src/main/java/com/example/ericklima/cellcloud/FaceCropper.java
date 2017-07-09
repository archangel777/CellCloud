package com.example.ericklima.cellcloud;

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

public class FaceCropper {
    private ImageView imageView;
    private ProgressBar progressBar;
    private Button button;
    private int viewHeight, viewWidth;
    private BitmapDrawable drawable;

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
                return crop(drawable.getBitmap(), 180, 180);
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

    private static int contain(int n, int min, int max) {
        return Math.min(Math.max(n, min), max);
    }

    public static Bitmap crop(Bitmap b, int viewHeight, int viewWidth) {
        Bitmap result = null;
        try {
            Paint paint = new Paint();
            paint.setFilterBitmap(true);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] bytes = stream.toByteArray();

            int targetWidth = b.getWidth();
            int targetHeight = b.getHeight();

            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                    targetHeight, Bitmap.Config.ARGB_8888);

            RectF rectf = new RectF(0, 0, viewWidth, viewHeight);

            Canvas canvas = new Canvas(targetBitmap);
            Path path = new Path();

            path.addRect(rectf, Path.Direction.CW);
            canvas.clipPath(path);

            canvas.drawBitmap(b,
                    new Rect(0, 0, b.getWidth(), b.getHeight()),
                    new Rect(0, 0, targetWidth, targetHeight), paint);

            Matrix matrix = new Matrix();
            matrix.postScale(1f, 1f);

            BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
            bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

            b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapFactoryOptions);

            FaceDetector.Face[] myFace = new FaceDetector.Face[5];
            FaceDetector myFaceDetect = new FaceDetector(targetWidth, targetHeight, 5);
            int numberOfFaceDetected = myFaceDetect.findFaces(b, myFace);

            Log.d("Faces Detected", "" + numberOfFaceDetected);

            if (numberOfFaceDetected > 0) {
                PointF myMidPoint;
                FaceDetector.Face face = myFace[0];
                myMidPoint = new PointF();
                face.getMidPoint(myMidPoint);
                float myEyesDistance = face.eyesDistance();

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

                int x = contain((int) (myMidPoint.x - 1.5 * myEyesDistance), 0, b.getWidth());
                int y = contain((int) (myMidPoint.y - 1.2 * myEyesDistance), 0, b.getHeight());
                int sizeX = Math.min(Math.min((int) (3 * myEyesDistance), b.getWidth() - x), b.getHeight() - y);
                int sizeY = Math.min(Math.min((int) (3 * myEyesDistance), b.getWidth() - x), b.getHeight() - y);

                Log.d("Sizes", x + " | " + y + " | " + sizeX + " | " + sizeY);
                result = Bitmap.createBitmap(b, x, y, sizeX, sizeY, matrix, true);
            } else {
                result = Bitmap.createBitmap(b, 0, 0, viewWidth, viewHeight, matrix, true);
            }
        } catch (Exception e) {
            System.out.println("Error1 : " + e.getMessage() + e.toString());
        }
        return result;
    }

}
