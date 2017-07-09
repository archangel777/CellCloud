package com.example.ericklima.cellcloud.image_list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by ErickLima on 09/07/2017.
 */

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    // references to our images
    private ArrayList<String> imagePaths = new ArrayList<>();

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public void addImagePath(String path) {
        imagePaths.add(path);
    }

    public int getCount() {
        return imagePaths.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new SquareImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePaths.get(position));
        imageView.setImageBitmap(bitmap);
        return imageView;
    }


}
