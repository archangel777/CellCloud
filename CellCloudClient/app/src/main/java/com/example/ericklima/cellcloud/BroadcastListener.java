package com.example.ericklima.cellcloud;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ErickLima on 07/07/2017.
 */

public class BroadcastListener extends Listener {

    private ImageView imageView;

    public BroadcastListener(MainScreen context) {
        super(context);
        imageView = (ImageView) context.findViewById(R.id.upload_photo);
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
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

                if (!isFromPC(packet.getAddress())) {
                    //Packet received
                    Log.i(TAG, "Packet received from: " + packet.getAddress().getHostAddress());
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    ImageComparator comparator = new ImageComparator(context, bitmap, packet.getAddress());
                    String path = Environment.getExternalStorageDirectory().toString() + "/faces";
                    File dir = new File(path);
                    if (dir.exists())
                        for (File f : dir.listFiles()) {
                            Log.d("File Name", f.getName());
                            Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
                            comparator.run(b);
                        }
                }
            }
        } catch (IOException ex) {
            Log.i(TAG, "Oops!! " + ex.getMessage());
        }
    }

}
