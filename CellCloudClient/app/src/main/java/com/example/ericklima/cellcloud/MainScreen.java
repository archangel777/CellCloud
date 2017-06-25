package com.example.ericklima.cellcloud;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ericklima.cellcloud.strategies.MinReduceStrategy;
import com.example.ericklima.cellcloud.strategies.ReduceStrategy;
import com.example.ericklima.cellcloud.strategies.SumReduceStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends AppCompatActivity {

    private boolean connected = false;

    public static final String host = "192.168.0.105";
    public static final int port = 3000;
    public static final int timeout = 3000;

    Socket socket = null;
    Listener listener = null;

    private ProgressDialog progress;
    private Button btn;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        btn = (Button) findViewById(R.id.main_btn);
        textView = (TextView) findViewById(R.id.messenger);

        progress = new ProgressDialog(this);
        progress.setMessage("Trying to connect to server on port " + port + "...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
    }

    public void onClick(View v) {
        try {
            if (!connected)
                new ServerConnector(host, port).execute();
            else {
                listener.cancel(true);
                connected = false;
                btn.setText("Connect");
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ServerConnector extends AsyncTask<Void, Void, Socket> {

        private String host;
        private int port;

        ServerConnector(String host, int port) {
            socket = new Socket();
            this.host = host;
            this.port = port;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        @Override
        protected Socket doInBackground(Void[] objects) {
            try {
                socket.connect(new InetSocketAddress(host, port), timeout);
            } catch (IOException e) {
                Log.e("Failed Connection", "Failed to connect after " + timeout + "ms.");
            }

            return socket;
        }

        @Override
        protected void onPostExecute(Socket socket) {
            super.onPostExecute(socket);
            progress.dismiss();
            if (socket.isConnected()) {
                textView.setText("");
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    connected = true;
                    btn.setText("Disconnect");
                    listener = new Listener(in, out);
                    listener.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                textView.setText("Failed to connect.");
            }
        }
    }

    private class Listener extends AsyncTask<Void, Void, Void> {

        private PrintWriter out = null;
        private BufferedReader in = null;

        Listener(BufferedReader in, PrintWriter out) {
            this.in = in;
            this.out = out;
        }

        @Override
        protected Void doInBackground(Void[] objects) {
            try {
                String line;

                while((line = in.readLine()) != null) {
                    processAndSendResult(line, out);
                }
            } catch (IOException e) {
                Log.e("Disconnection", "Socket disconnected.");
            }
            return null;
        }
    }

    public void processAndSendResult(String toProcess, PrintWriter out) {
        Gson gson = new Gson();
        DataPacket pkt = gson.fromJson(toProcess, DataPacket.class);
        ReduceStrategy strategy;

        switch(pkt.getProcessType()) {
            default:
            case "min":
                strategy = new MinReduceStrategy();
                break;
            case "sum":
                strategy = new SumReduceStrategy();
                break;
        }
        Long result = strategy.reduce(pkt.getData());
        //Log.d("result", result.toString());
        out.println(gson.toJson(result));
        out.flush();
    }

}
