package com.mapzen.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import org.scribe.model.Token;


public class InitActivity extends Activity {
    @InjectView(R.id.sign_up_button) Button signUp;
    @InjectView(R.id.log_in_button) Button logIn;

    MapzenApplication app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);

        View rootView = getWindow().getDecorView().getRootView();
        ButterKnife.inject(this, rootView);
        app = (MapzenApplication) getApplication();
        app.onCreate();
        getActionBar().hide();

        if(app.isLoggedIn()) {
            Intent baseActivity = new Intent(this, BaseActivity.class);
            startActivity(baseActivity);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(app.isLoggedIn()) {
            Intent baseActivity = new Intent(this, BaseActivity.class);
            startActivity(baseActivity);
            this.finish();

        }
        else {
            Token userAuthenticationToken;
            try {
                String[] token = getVerifier();
                if (token[0] != null) {
                    userAuthenticationToken = new Token(token[0], token[1]);
                    app.setAccessToken(userAuthenticationToken);
                    Intent baseActivity = new Intent(this, BaseActivity.class);
                    startActivity(baseActivity);
                    this.finish();
                }
            } catch (Exception e) {

            }
        }
    }


    @OnClick(R.id.sign_up_button)
    @SuppressWarnings("unused")
    public void onClickSignUp() {
        openSignUpPage();
    }

    @OnClick(R.id.log_in_button)
    @SuppressWarnings("unused")
    public void onClickLogIn() {
        loginRoutine();
    }

    public void openSignUpPage() {
        String signUpURL = getString(R.string.osm_sign_up_url);
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(signUpURL));
        startActivity(signUpIntent);
    }

    public void loginRoutine() {
        (new AsyncTask<Void, Void, Token>() {
            @Override
            protected Token doInBackground(Void... params) {
                    return app.getOsmOauthService().getRequestToken();
            }

            @Override
            protected void onPostExecute(Token url) {
                String authenticationUrl = app.getOsmOauthService().getAuthorizationUrl(url);
                startActivity(new Intent("android.intent.action.VIEW",
                               Uri.parse(authenticationUrl)));
            }
        }).execute();
    }

    private String[] getVerifier() {
        // extract the token if it exists
        Uri uri = this.getIntent().getData();
        if (uri == null) {
            return null;
        }
        String token = uri.getQueryParameter("oauth_token");
        String verifier = uri.getQueryParameter("oauth_verifier");
        return new String[] { token, verifier };
    }
}
