package com.mapzen.open.core;

import com.mapzen.open.util.Logger;

import com.google.common.io.Files;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class StyleDownLoader {
    private OkHttpClient client;
    private String host;
    private HttpResponseCache cache;
    private Context context;

    public StyleDownLoader(Context context) {
        this.context = context;
        if (context.getExternalCacheDir() != null) {
            init();
        }
    }

    private void init() {
        final File cacheDir = new File(context.getExternalCacheDir().getAbsolutePath() + "/assets");
        final int cacheSize = 1024 * 1024;
        try {
            cache = new HttpResponseCache(cacheDir, cacheSize);
            client = new OkHttpClient();
            client.setOkResponseCache(cache);
        } catch (IOException ioe) {
            Logger.e("network failed: " + ioe.toString());
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void download() {
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (client == null) {
                    return null;
                }

                URL url = null;
                try {
                    url = new URL(host + "manifest");
                } catch (MalformedURLException e) {
                    Logger.e("bad url: " + host + "manifest");
                }
                HttpURLConnection connection = client.open(url);
                try {
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    String[] assets = IOUtils.toString(in).split("\n");
                    for (String asset: assets) {
                        if (!asset.isEmpty()) {
                            requestAndWriteFile(asset);
                        }
                    }
                } catch (IOException ioe) {
                    Logger.e("network failed: " + ioe.toString());
                } finally {
                    connection.disconnect();
                }
                return null;
            }
        }).execute();
    }

    private void requestAndWriteFile(String path) {
        URL url = null;
        try {
            url = new URL(host + path);
        } catch (MalformedURLException e) {
            Logger.e("bad url: " + host + path);
        }
        HttpURLConnection connection = client.open(url);
        try {
            Logger.d("Response for: " + path + " status: "
                    + connection.getResponseCode() + "\n"
                    + "cache stats: req count: " + cache.getRequestCount()
                    + "\nnetwork count: " + cache.getNetworkCount()
                    + "\ncache hitcount: " + cache.getHitCount());
            InputStream in = new BufferedInputStream(connection.getInputStream());
            String pathToFile = context.getExternalFilesDir(null).getAbsolutePath() + "/" + path;
            File file = new File(pathToFile);
            Files.createParentDirs(file);
            Files.write(IOUtils.toByteArray(in), file);
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        } finally {
            connection.disconnect();
        }
    }
}
