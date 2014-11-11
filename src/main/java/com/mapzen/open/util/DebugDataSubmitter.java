package com.mapzen.open.util;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.activity.BaseActivity;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.squareup.okhttp.OkHttpClient;

import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

public class DebugDataSubmitter implements Runnable {
    OkHttpClient client = new OkHttpClient();
    BaseActivity activity;
    String endpoint;
    File file;
    OutputStream out;
    InputStream in;

    @Inject SQLiteDatabase db;

    public DebugDataSubmitter(BaseActivity activity) {
        this.activity = activity;
        ((MapzenApplication) activity.getApplication()).inject(this);
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

    public void run() {
        try {
            submit();
        } catch (IOException e) {
            Logger.e("Unable to submit GPS data.", e);
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
            connection.setRequestProperty("Content-Type", "application/octet-stream");
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
            truncateDatabase();
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

    public void truncateDatabase() {
        db.beginTransaction();
        db.delete(DatabaseHelper.TABLE_ROUTES, null, null);
        db.delete(DatabaseHelper.TABLE_LOCATIONS, null, null);
        db.delete(DatabaseHelper.TABLE_ROUTE_GEOMETRY, null, null);
        db.delete(DatabaseHelper.TABLE_LOG_ENTRIES, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
