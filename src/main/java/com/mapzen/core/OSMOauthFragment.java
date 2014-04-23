package com.mapzen.core;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.util.Logger;

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
    public static final String OAUTH_URL = "http://www.openstreetmap.org/oauth/authorize";
    public static final String CLIENT_ID = "LlswDE4hErhnWWIlX0JRASLHQvtsC7lOjUVqg7w3";
    public static final String CLIENT_SECRET = "DWsEIHPrWFELsOnJUWDX1EYZXVBC7EvtjObDhAAl";
    public static final String TAG = OSMOauthFragment.class.getSimpleName();

    private BaseActivity act;

    public void setActivity(BaseActivity act) {
        this.act = act;
    }

    public static OSMOauthFragment newInstance(BaseActivity act) {
        final OSMOauthFragment fragment = new OSMOauthFragment();
        fragment.setActivity(act);
        return fragment;
    }

    public void setTokens(Uri uri) {
        Logger.d("OAUTH: " + uri.toString());
        Logger.d("OAUTH oauth_token: ||" + uri.getQueryParameter("oauth_token") + "||");
        Logger.d("OAUTH oauth_verifier: ||" + uri.getQueryParameter("oauth_verifier") + "||");
        act.setVerifier(new Verifier(uri.getQueryParameter("oauth_verifier")));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle("Please Authorize");

        String url = OAUTH_URL + "?client_id=" + CLIENT_ID;
        final View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button closer = (Button) view.findViewById(R.id.closer);
        closer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final WebView webview = (WebView) view.findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                if (url.contains("mapzen.com")) {
                    Uri uri = Uri.parse(url);
                    setTokens(uri);
                    (new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            if (act.getAccessToken() == null) {
                                act.setAccessToken(act.getService().getAccessToken(
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
         });

        (new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String url = "";
                if (act.getAccessToken() == null) {
                    act.setRequestToken(act.getService().getRequestToken());
                    url = act.getService().getAuthorizationUrl(act.getRequestToken());
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
        return view;
    }
}
