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

    private Context c;
    private Bitmap bitmap;

    public FaceSaver(Bitmap bitmap, Context c) {
        this.c = c;
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        try {
            bitmap = FaceCropper.crop(bitmap, 200, 200);
            String path = Environment.getExternalStorageDirectory().toString() + "/faces";
            int counter = 0;
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();
            File file;
            // the File to save , append increasing numeric counter to prevent files from getting overwritten.
            while((file = new File(path, "face" + counter + ".jpg")).exists()) counter ++;
            OutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream

            //MediaStore.Images.Media.insertImage(c.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
