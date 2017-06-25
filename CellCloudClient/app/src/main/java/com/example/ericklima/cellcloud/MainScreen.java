package com.example.ericklima.cellcloud;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    public static String host;
    public static int port;
    public static final int timeout = 3000;

    private SharedPreferences mPreferences;

    Socket socket = null;
    Listener listener = null;

    private ProgressDialog progress;
    private Button btn;
    private TextView textView;
    private EditText mIP0, mIP1, mIP2, mIP3, mPort;

    TextChanger textChangerListener = new TextChanger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);



        mIP0 = (EditText) findViewById(R.id.ip0);
        mIP1 = (EditText) findViewById(R.id.ip1);
        mIP2 = (EditText) findViewById(R.id.ip2);
        mIP3 = (EditText) findViewById(R.id.ip3);
        mPort = (EditText) findViewById(R.id.port);

        mIP0.addTextChangedListener(textChangerListener);
        mIP1.addTextChangedListener(textChangerListener);
        mIP2.addTextChangedListener(textChangerListener);
        mIP3.addTextChangedListener(textChangerListener);
        mPort.addTextChangedListener(textChangerListener);

        mPreferences = getSharedPreferences("Connection", MODE_PRIVATE);
        if (mPreferences.contains("ip") && mPreferences.contains("port")) {
            host = mPreferences.getString("ip", "192.168.0.1");
            port = mPreferences.getInt("port", 3000);
            String[] split = host.split("\\.");
            mIP0.setText(split[0]);
            mIP1.setText(split[1]);
            mIP2.setText(split[2]);
            mIP3.setText(split[3]);
            mPort.setText(String.valueOf(port));
        }

        btn = (Button) findViewById(R.id.main_btn);
        textView = (TextView) findViewById(R.id.messenger);

        progress = new ProgressDialog(this);
        progress.setMessage("Trying to connect to server on port " + port + "...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIP0.removeTextChangedListener(textChangerListener);
        mIP1.removeTextChangedListener(textChangerListener);
        mIP2.removeTextChangedListener(textChangerListener);
        mIP3.removeTextChangedListener(textChangerListener);
        mPort.removeTextChangedListener(textChangerListener);
    }

    private class TextChanger implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            StringBuilder builder = new StringBuilder();
            builder.append(mIP0.getText().toString());
            builder.append(".");
            builder.append(mIP1.getText().toString());
            builder.append(".");
            builder.append(mIP2.getText().toString());
            builder.append(".");
            builder.append(mIP3.getText().toString());
            host = builder.toString();
            port = Integer.valueOf(mPort.getText().toString());
            mPreferences.edit()
                    .putString("ip", host)
                    .putInt("port", port)
                    .apply();;
        }

        @Override
        public void afterTextChanged(Editable editable) {}
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
                Log.e("Failed Connection", "Failed to connect to " + host + ":" + port + " after " + timeout + "ms.");
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
