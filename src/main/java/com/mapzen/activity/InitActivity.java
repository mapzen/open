package com.mapzen.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
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
    Handler delayButtonHandler;
    Animation fadeIn, fadeInSlow, fadeOut;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);
        View rootView = getWindow().getDecorView().getRootView();
        app = (MapzenApplication) getApplication();
        ButterKnife.inject(this, rootView);
        getActionBar().hide();
        if (app.isLoggedIn()) {
            startBaseActivity();
        }
        loadAnimations();
        animateViewTransitions();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Token userAuthenticationToken = getTokenFromCallback(intent);
        if (userAuthenticationToken != null) {
            app.setAccessToken(userAuthenticationToken);
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

    private void openSignUpPage() {
        String signUpURL = getString(R.string.osm_sign_up_url);
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(signUpURL));
        startActivity(signUpIntent);
    }

    private void loginRoutine() {
        (new AsyncTask<Void, Void, Token>() {
            @Override
            protected Token doInBackground(Void... params) {
                return app.getOsmOauthService().getRequestToken();
            }

            @Override
            protected void onPostExecute(Token url) {
                openLoginPage(url);
            }
        }).execute();
    }

    protected void openLoginPage(Token url) {
        String authenticationUrl = app.getOsmOauthService().getAuthorizationUrl(url);
        Intent oauthIntent = new Intent(Intent.ACTION_VIEW);
        oauthIntent.setData(Uri.parse(authenticationUrl));
        startActivity(oauthIntent);
    }

    private void startBaseActivity() {
        Intent baseActivity = new Intent(this, BaseActivity.class);
        startActivity(baseActivity);
        finish();
    }

    private Token getTokenFromCallback(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        String token = uri.getQueryParameter("oauth_token");
        String verifier = uri.getQueryParameter("oauth_verifier");
        return new Token(token, verifier);
    }

    private void animateViewTransitions() {
        fadeOutMotto();
        delayButtonHandler = new Handler();
        delayButtonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setMottoInvisible();
                fadeInLoginView();
            }
        }, 2000);
    }

    private void setMottoInvisible() {
        findViewById(R.id.motto).setVisibility(LinearLayout.INVISIBLE);
    }

    private void fadeOutMotto() {
        findViewById(R.id.motto).startAnimation(fadeOut);
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
}
