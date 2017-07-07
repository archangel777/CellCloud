package com.example.ericklima.cellcloud;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ErickLima on 07/07/2017.
 */

public class BroadcastListener extends Listener {

    public BroadcastListener(MainScreen context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            localHost = getLocalIpAddress();
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            DatagramSocket socket = new DatagramSocket(Constants.BROADCAST_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                Log.i(TAG,"Ready to receive broadcast packets!");

                //Receive a packet
                byte[] recvBuf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                Log.d(TAG, "Address: " + packet.getAddress().getHostAddress() + ", Port: " + String.valueOf(packet.getPort()));

                    //if (!isFromPC(packet.getAddress())) {
                    //Packet received
                    Log.i(TAG, "Packet received from: " + packet.getAddress().getHostAddress());
                    Bitmap bitmap = BitmapFactory.decodeByteArray(recvBuf, 0, recvBuf.length);
                    ImageComparator comparator = new ImageComparator(context, bitmap, packet.getAddress());
                    String path = Environment.getExternalStorageDirectory().toString() + "/faces";
                    File dir = new File(path);
                    if (dir.exists())
                        for (File f : dir.listFiles()) {
                            Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
                            comparator.run(b);
                        }
                    //}
            }
        } catch (IOException ex) {
            Log.i(TAG, "Oops!! " + ex.getMessage());
        }
    }

}
