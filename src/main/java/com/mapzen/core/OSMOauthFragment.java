package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

import org.scribe.model.Verifier;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class OSMOauthFragment extends DialogFragment {
    public static final String TAG = OSMOauthFragment.class.getSimpleName();
    public static final String OSM_VERIFIER_KEY = "oauth_verifier";
    public static final String OSM_MAPZEN_URL_MATCH = "mapzen.com";

    private BaseActivity act;

    public void setActivity(BaseActivity act) {
        this.act = act;
    }

    public static OSMOauthFragment newInstance(BaseActivity act) {
        final OSMOauthFragment fragment = new OSMOauthFragment();
        fragment.setActivity(act);
        return fragment;
    }

    public void setVerifier(Uri uri) {
        act.setVerifier(new Verifier(uri.getQueryParameter(OSM_VERIFIER_KEY)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle(act.getString(R.string.osm_login_title));

        final View view = inflater.inflate(R.layout.fragment_osm_oauth_login, container, false);

        final WebView webview = (WebView) view.findViewById(R.id.oauth_login_screen);
        webview.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                if (url.contains(OSM_MAPZEN_URL_MATCH)) {
                    Uri uri = Uri.parse(url);
                    setVerifier(uri);
                    setAccessToken();
                }
            }
        });

        loadAuthorizationUrl(webview);
        return view;
    }

    private void loadAuthorizationUrl(final WebView webview) {
        (new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String url = "";
                if (act.getAccessToken() == null) {
                    act.setRequestToken(act.getOsmOauthService().getRequestToken());
                    url = act.getOsmOauthService().getAuthorizationUrl(act.getRequestToken());
                } else {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webview.setVisibility(View.GONE);
                        }
                    });
                }
                return url;
            }

            @Override
            protected void onPostExecute(String url) {
                webview.loadUrl(url);
            }
        }).execute();
    }

    private void setAccessToken() {
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (act.getAccessToken() == null) {
                    act.setAccessToken(act.getOsmOauthService().getAccessToken(
                            act.getRequestToken(), act.getVerifier()));
                }
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
                return null;
            }
        }).execute();
    }
}
