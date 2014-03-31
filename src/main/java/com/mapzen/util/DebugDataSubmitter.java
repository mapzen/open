package com.mapzen.util;

import com.mapzen.activity.BaseActivity;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.squareup.okhttp.OkHttpClient;

import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DebugDataSubmitter {
    OkHttpClient client = new OkHttpClient();
    BaseActivity activity;
    String endpoint;
    File file;
    OutputStream out;
    InputStream in;
    boolean runInThread = true;

    public DebugDataSubmitter(BaseActivity activity) {
        this.activity = activity;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setRunInThread(boolean runInThread) {
        this.runInThread = runInThread;
    }

    public void run() {
        if (runInThread) {
            executeInThread();
        } else {
            execute();
        }
    }

    public String readInputStream(InputStream in) throws IOException {
        return CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
    }

    private void submit() throws IOException {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = client.open(url);
            connection.setRequestMethod("POST");
            out = connection.getOutputStream();
            out.write(Files.toByteArray(file));
            out.close();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity,
                                "Upload failed, please try again later!", Toast.LENGTH_LONG).show();
                    }
                });
                throw new IOException("Unexpected HTTP response: "
                        + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
            final String responseText = readInputStream(connection.getInputStream());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, responseText, Toast.LENGTH_LONG).show();
                }
            });
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    private void execute() {
        try {
            submit();
        } catch (IOException e) {
            Logger.e("Error: " + e.toString());
        }
    }

    private void executeInThread() {
        new Thread(new Runnable() {
            public void run() {
                execute();
            }
        }).start();
    }
}


