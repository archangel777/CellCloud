package com.example.ericklima.cellcloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static android.content.ContentValues.TAG;

/**
 * Created by ErickLima on 01/07/2017.
 */

public class DirectSender extends AsyncTask<Bitmap, Void, Void>{

    private Context c;
    private InetAddress address;

    public DirectSender(Context c, InetAddress address) {
        this.c = c;
        this.address = address;
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        send(getBytesFromBitmap(bitmaps[0]));
        return null;
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void send(byte[] sendData) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            int port = (address.toString().endsWith(".255"))? Constants.BROADCAST_PORT : Constants.RESULT_PORT;
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);
            Log.d("SENDING", getClass().getName() + "Broadcast packet sent to: " + address);
            Log.d("SENDING", "Size: " + sendPacket.getLength());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }
}
