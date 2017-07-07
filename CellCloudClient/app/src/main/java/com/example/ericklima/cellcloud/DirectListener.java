package com.example.ericklima.cellcloud;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ErickLima on 07/07/2017.
 */

public class DirectListener extends Listener {

    private ImageView imageView;
    private byte[] recvBuf;

    public DirectListener(MainScreen context, ImageView view) {
        super(context);
        imageView = view;
    }

    @Override
    public void run() {
        try {
            localHost = getLocalIpAddress();
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            DatagramSocket socket = new DatagramSocket(Constants.RESULT_PORT, InetAddress.getByName("0.0.0.0"));

            while (true) {
                Log.i(TAG,"Ready to receive broadcast packets!");

                //Receive a packet
                recvBuf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                Log.d(TAG, "Address: " + packet.getAddress().getHostAddress() + ", Port: " + String.valueOf(packet.getPort()));

                Log.d("FOUND", "FOUND THE FACE !!!!! NICEEEEEEEEEE");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Found the face!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (IOException ex) {
            Log.i(TAG, "Oops!! " + ex.getMessage());
        }
    }

}
