package com.mapzen.util;

import com.google.common.io.Files;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class SubmitDebugData {
    OkHttpClient client = new OkHttpClient();
    String endpoint;
    File file;
    OutputStream out;
    InputStream in;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private String submit() throws IOException {
        try {
            URL url = new URL(endpoint + "?filename=" + UUID.randomUUID().toString());
            HttpURLConnection connection = client.open(url);
            connection.setRequestMethod("POST");
            out = connection.getOutputStream();
            out.write(Files.toByteArray(file));
            out.close();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP response: "
                        + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
            in = connection.getInputStream();
            return in.toString();
        } finally {
            if (out != null) { out.close(); }
            if (in != null) { in.close(); }
        }
    }

    public void run() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    submit();
                } catch (IOException error) {
                    Logger.d("error: " + error.toString());
                }
            }
        }).start();
    }
}


