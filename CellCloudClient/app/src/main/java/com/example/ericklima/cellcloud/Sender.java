package com.example.ericklima.cellcloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
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

public class Sender extends AsyncTask<Bitmap, Void, Void>{

    private Context c;

    public Sender(Context c) {
        this.c = c;
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        sendBroadcast(getBytesFromBitmap(bitmaps[0]));
        return null;
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    private void sendBroadcast(byte[] sendData) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), Constants.PORT);
            socket.send(sendPacket);
            Log.d("SENDING", getClass().getName() + "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
            Log.d("N Bytes", "" + sendPacket.getLength());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}
