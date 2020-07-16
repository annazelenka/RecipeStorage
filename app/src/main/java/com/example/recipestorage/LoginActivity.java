package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout etUsername;
    TextInputLayout etPassword;
    Button btnLogin;
    Button btnSignup;
    Button btnLoginFacebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        ParseFacebookUtils.initialize(LoginActivity.this);

        btnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLoginFacebook();
            }
        });
    }

    private void handleLogin() {
        launchHomeScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    final List<String> permissions = Arrays.asList("public_profile", "email");

    private void handleLoginFacebook() {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                    launchHomeScreen();
                }
            }
        });
    }

    private void launchHomeScreen() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}