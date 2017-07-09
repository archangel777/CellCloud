package com.example.ericklima.cellcloud.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.ericklima.cellcloud.MainScreen;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ErickLima on 01/07/2017.
 */

public abstract class Listener implements Runnable {

    protected static final String TAG = "LISTENER";
    protected static String localHost = null;
    protected MainScreen context;
    protected Handler handler = new Handler();

    protected Listener(MainScreen context) {
        this.context = context;
    }

    protected boolean isFromPC(InetAddress address) {
        return address.getHostAddress().equals(localHost);
    }

    protected String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Socket Problem", ex.toString());
        }
        return null;
    }
}
