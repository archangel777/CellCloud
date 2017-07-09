package com.example.ericklima.cellcloud.image_list;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import com.example.ericklima.cellcloud.R;

import java.io.File;

public class AddedImagesActivity extends AppCompatActivity {

    String SCAN_PATH = Environment.getExternalStorageDirectory().getPath()+"/faces/";
    File[] allFiles ;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_added_images);

        GridView gridview = (GridView) findViewById(R.id.grid_view);
        ImageAdapter adapter = new ImageAdapter(this);

        File folder = new File(SCAN_PATH);
        allFiles = folder.listFiles();
        if (allFiles != null) {
            for (File f : allFiles)
                adapter.addImagePath(f.getAbsolutePath());
        }

        gridview.setAdapter(adapter);

//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v,
//                                    int position, long id) {
//                new SingleMediaScanner(AddedImagesActivity.this, allFiles[position]);
//            }
//        });


    }

    public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mMs;
        private File mFile;

        public SingleMediaScanner(Context context, File f) {
            mFile = f;
            mMs = new MediaScannerConnection(context, this);
            mMs.connect();
        }

        public void onMediaScannerConnected() {
            mMs.scanFile(mFile.getAbsolutePath(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
            mMs.disconnect();
        }

    }
}
