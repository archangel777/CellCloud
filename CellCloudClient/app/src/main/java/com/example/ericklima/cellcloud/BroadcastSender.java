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
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

/**
 * Created by ErickLima on 01/07/2017.
 */

public class BroadcastSender extends TargetSender{

    private Context c;

    public BroadcastSender(Context c) {
        super(c, getBroadcastAddress(c));
    }

    private static InetAddress getBroadcastAddress(Context c) {
        InetAddress address = null;
        WifiManager wifi = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            address = InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address;
    }
}
