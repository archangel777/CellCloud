package com.example.ericklima.cellcloud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
//import android.widget.Button;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainScreen extends AppCompatActivity {

    private static final String LOG_TAG = "WIFI LOG";
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_PERMISSION = 6473;

    //private Button mButton;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Button mButton;

    private boolean isImageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG); // PARTIAL_WAKE_LOCK Only keeps CPU on
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock = wifiManager.createWifiLock(3, LOG_TAG);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock(LOG_TAG);

        wakeLock.acquire();
        multicastLock.acquire();
        wifiLock.acquire();

        mButton = (Button) findViewById(R.id.main_btn);
        mButton.setEnabled(false);
        mImageView = (ImageView) findViewById(R.id.upload_photo);
        mProgressBar = (ProgressBar) findViewById(R.id.loadingPanel);
        mProgressBar.setVisibility(View.GONE);

        new Thread(new Listener()).start();

    }

    public void onClick(View v) {
        if (!isImageSelected) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setMessage("You must select a photo to send.").show();
        } else {
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
            Bitmap[] bitmap = {drawable.getBitmap()};
            //mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap[0], 60, 60, false));
            new Sender(this).execute(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void choosePhoto(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            mImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            new FaceCropper(mImageView, mProgressBar, mButton).cropImage();
            isImageSelected = true;
        }
    }

}
