package com.example.ericklima.cellcloud;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ErickLima on 01/07/2017.
 */

public class BroadcastSender extends DirectSender {

    private Context c;

    public BroadcastSender(Context c) {
        super(c, getBroadcastAddress(c));
    }

    private static InetAddress getBroadcastAddress(Context c) {
        try {
            WifiManager wifi = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

            //return InetAddress.getByName("255.255.255.255");
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
