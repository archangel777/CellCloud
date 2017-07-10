package com.example.ericklima.cellcloud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.widget.Button;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ericklima.cellcloud.image_list.AddedImagesActivity;
import com.example.ericklima.cellcloud.network.BroadcastListener;
import com.example.ericklima.cellcloud.network.BroadcastSender;
import com.example.ericklima.cellcloud.network.DirectListener;
import com.example.ericklima.cellcloud.results.ResultsFragment;

import java.io.IOException;

public class MainScreen extends AppCompatActivity {

    private static final String LOG_TAG = "WIFI LOG";
    private static final int REQUEST_PERMISSION_FIND = 6473;
    private static final int REQUEST_PERMISSION_ADD = 6474;
    private static final int REQUEST_PERMISSION_LIST = 6475;

    //private Button mButton;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Button mButton;
    private FrameLayout fragmentContainer;

    FragmentManager fm;
    private ResultsFragment resultsFragment;
    private boolean canCloseFragment = false;

    private boolean isImageSelected = false;

    static{ System.loadLibrary("opencv_java3"); }

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

        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_place);
        mButton = (Button) findViewById(R.id.main_btn);
        mButton.setEnabled(false);
        mImageView = (ImageView) findViewById(R.id.upload_photo);
        mProgressBar = (ProgressBar) findViewById(R.id.loadingPanel);
        mProgressBar.setVisibility(View.GONE);

        fm = getSupportFragmentManager();
        resultsFragment = new ResultsFragment();

        new Thread(new BroadcastListener(this)).start();

        new Thread(new DirectListener(this, mImageView)).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_btn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_ADD);
            } else {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_PERMISSION_ADD);
            }
        } else if (id == R.id.rmv_btn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_LIST);
            } else {
                Intent it = new Intent(this, AddedImagesActivity.class);
                startActivity(it);
            }

        }
        return super.onOptionsItemSelected(item);
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
            new BroadcastSender(this).execute(drawable.getBitmap());
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_place, resultsFragment);
            fragmentTransaction.addToBackStack("result");
            fragmentTransaction.commit();
            canCloseFragment = false;
            fragmentContainer.setClickable(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    canCloseFragment = true;
                }
            }, 3000);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_FIND || requestCode == REQUEST_PERMISSION_ADD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, requestCode);
            }
        }
        else if (requestCode == REQUEST_PERMISSION_LIST) {
            Intent it = new Intent(this, AddedImagesActivity.class);
            startActivity(it);
        }
    }

    public void choosePhoto(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_FIND);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_PERMISSION_FIND);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == REQUEST_PERMISSION_FIND || requestCode == REQUEST_PERMISSION_ADD) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Bitmap imageBitmap = getFixedImage(picturePath);
                if (requestCode == REQUEST_PERMISSION_FIND) {
                    mImageView.setImageBitmap(imageBitmap);
                    new FaceCropper(mImageView, mProgressBar, mButton).cropImage();
                    isImageSelected = true;
                } else {
                    new Thread(new FaceSaver(imageBitmap, this)).start();
                }
            }
        }
    }

    private Bitmap getFixedImage(String imagePath) {
        Bitmap rawImage = BitmapFactory.decodeFile(imagePath);
        float aspectRatio = rawImage.getWidth() / (float) rawImage.getHeight();
        int width = 480;
        int height = Math.round(width / aspectRatio);
        Bitmap rescaled = Bitmap.createScaledBitmap(rawImage, width, height, false);
        rawImage.recycle();
        Matrix matrix = new Matrix();
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch(orientation) {
                case 3: matrix.postRotate(180); break;
                case 6: matrix.postRotate(90); break;
                case 8: matrix.postRotate(270); break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap rotated = Bitmap.createBitmap(rescaled, 0, 0, rescaled.getWidth(), rescaled.getHeight(), matrix, true);
        rescaled.recycle();
        return rotated;
    }

    public void receiveResult(Bitmap b) {
        if (resultsFragment.isVisible())
            resultsFragment.addBitmap(b);
    }

    @Override
    public void onBackPressed() {
        if (canCloseFragment) {
            if (fm.getBackStackEntryCount() != 0) {
                fm.popBackStack();
                fragmentContainer.setClickable(false);
            } else {
                super.onBackPressed();
            }
        }
    }

}
