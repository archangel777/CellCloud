package com.example.ericklima.cellcloud;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ErickLima on 01/07/2017.
 */

public class Listener implements Runnable {

    private static final String TAG = "LISTENER";

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            DatagramSocket socket = new DatagramSocket(Constants.PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                Log.i(TAG,"Ready to receive broadcast packets!");

                //Receive a packet
                byte[] recvBuf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Packet received
                Log.i(TAG, "Packet received from: " + packet.getAddress().getHostAddress());
                Bitmap bitmap = BitmapFactory.decodeByteArray(recvBuf, 0, recvBuf.length);
            }
        } catch (IOException ex) {
            Log.i(TAG, "Oops!! " + ex.getMessage());
        }
    }
}
