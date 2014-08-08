package com.mapzen.activity;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.android.lost.LocationClient;

import org.scribe.model.Token;
import org.scribe.model.Verifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mapzen.core.OSMOauthFragment.OSM_VERIFIER_KEY;

public class LoginActivity extends Activity {
    @InjectView(R.id.sign_up_button) Button signUp;
    @InjectView(R.id.log_in_button) Button logIn;
    private MapzenApplication app;
    private Token requestToken = null;
    private Handler delayButtonHandler;
    private Animation fadeIn, fadeInSlow, fadeOut;
    private Verifier verifier;
    private int clickCount;
    @Inject LocationClient locationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MapzenApplication) getApplication();
        app.inject(this);
        setContentView(R.layout.init);
        View rootView = getWindow().getDecorView().getRootView();
        clickCount = 0;
        ButterKnife.inject(this, rootView);
        loadAnimations();
        animateViewTransitions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationClient.disconnect();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) {
            setAccessToken(intent);
            startBaseActivity();
        }
    }

    @OnClick(R.id.sign_up_button)
    @SuppressWarnings("unused")
    protected void onClickSignUp() {
        openSignUpPage();
    }

    @OnClick(R.id.log_in_button)
    @SuppressWarnings("unused")
    protected void onClickLogIn() {
        loginRoutine();
    }

    @OnClick(R.id.logo)
    protected void onClickLogo() {
        clickCount++;
        if (clickCount == 3) {
            forceLogin();
        }
    }

    private void forceLogin() {
        SharedPreferences prefs = getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("forced_login", true);
        editor.commit();
        startBaseActivity();
    }

    private void openSignUpPage() {
        String signUpURL = getString(R.string.osm_sign_up_url);
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(signUpURL));
        startActivity(signUpIntent);
    }

    public void loginRoutine() {
        (new AsyncTask<Void, Void, Token>() {
            @Override
            protected Token doInBackground(Void... params) {
                try {
                    setRequestToken(app.getOsmOauthService().getRequestToken());
                    return requestToken;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Token url) {
                if (url != null) {
                    openLoginPage(url);
                } else {
                    unableToLogInAction();
                }
            }
        }).execute();
    }

    protected void openLoginPage(Token url) {
        String authenticationUrl = app.getOsmOauthService().getAuthorizationUrl(url);
        Intent oauthIntent = new Intent(Intent.ACTION_VIEW);
        oauthIntent.setData(Uri.parse(authenticationUrl));
        oauthIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(oauthIntent);
    }

    private void startBaseActivity() {
        Intent baseActivity = new Intent(this, BaseActivity.class);
        startActivity(baseActivity);
        finish();
    }

    private void setAccessToken(Intent intent) {
        Uri uri = intent.getData();
        verifier = new Verifier(uri.getQueryParameter(OSM_VERIFIER_KEY));
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    app.setAccessToken(app.getOsmOauthService()
                            .getAccessToken(requestToken, verifier));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Unable to log in", Toast.LENGTH_LONG).show();
                }

                return null;
            }
        }).execute();
    }

    public void setRequestToken(Token token) {
        requestToken = token;
    }

    private void animateViewTransitions() {
        fadeOutMotto();
        delayButtonHandler = new Handler();
        delayButtonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeInLoginView();
            }
        }, 2000);
    }

    private void fadeOutMotto() {
        findViewById(R.id.motto).startAnimation(fadeOut);
        findViewById(R.id.motto).setVisibility(LinearLayout.INVISIBLE);
    }

    private void fadeInLoginView() {
        findViewById(R.id.login_explanation).startAnimation(fadeIn);
        findViewById(R.id.log_in_button).startAnimation(fadeInSlow);
        findViewById(R.id.sign_up_button).startAnimation(fadeInSlow);
        findViewById(R.id.login_layout).setVisibility(LinearLayout.VISIBLE);
    }

    private void loadAnimations() {
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fadeinslow);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
    }

    protected void unableToLogInAction() {
        Toast.makeText(getApplicationContext(), getString(R.string.login_error),
                Toast.LENGTH_LONG).show();
        startBaseActivity();
    }

    public String getOSMVerifierKey() {
        return OSM_VERIFIER_KEY;
    }
}
