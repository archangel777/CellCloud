package com.example.ericklima.cellcloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FaceSaver implements Runnable {

    private static int counter = 0;
    private Context c;
    private Bitmap bitmap;

    public FaceSaver(Bitmap bitmap, Context c) {
        this.c = c;
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        try {
            bitmap = FaceCropper.crop(bitmap, 180, 180);
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, "face" + counter + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
            OutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream

            MediaStore.Images.Media.insertImage(c.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            counter++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
