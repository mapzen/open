package com.mapzen.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.mapzen.R;


public class InitActivity extends Activity {
    @InjectView(R.id.sign_up_button) Button signUp;
    @InjectView(R.id.log_in_button) Button logIn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);
        View rootView = getWindow().getDecorView().getRootView();
        ButterKnife.inject(this, rootView);
        getActionBar().hide();
    }

    @OnClick(R.id.sign_up_button)
    @SuppressWarnings("unused")
    public void onClickSignUp() {
        openSignUpPage();
    }

    @OnClick(R.id.log_in_button)
    @SuppressWarnings("unused")
    public void onClickLogIn() {
        openSignUpPage();
    }

    public void openSignUpPage() {
        String signUpURL = getString(R.string.osm_sign_up_url);
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(signUpURL));
        startActivity(signUpIntent);
    }
}