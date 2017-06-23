package com.example.ericklima.cellcloud;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        btn = (Button) findViewById(R.id.main_btn);

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
                System.err.println("Failed to connect after " + timeout + "ms.");
                //e.printStackTrace();
            }

            return socket;
        }

        @Override
        protected void onPostExecute(Socket socket) {
            super.onPostExecute(socket);
            progress.dismiss();
            if (socket.isConnected()) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void processAndSendResult(String toProcess, PrintWriter out) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Tuple<Long>>>(){}.getType();
        ArrayList<Tuple<Long>> list = gson.fromJson(toProcess, type);

        Long result = reduce(list);
        out.println(gson.toJson(result));
    }

    public Long reduce(ArrayList<Tuple<Long>> t) {
        if (t.size() == 1) return Math.min(t.get(0).getT1(), t.get(0).getT2());
        Tuple<Long> t1 = t.remove(0), t2 = t.remove(0);
        t.add(new Tuple<>(Math.min(t1.getT1(), t1.getT2()), Math.min(t2.getT1(), t2.getT2())));
        return reduce(t);
    }
}
