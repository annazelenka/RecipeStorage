package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    TextInputEditText etUsername;
    TextInputEditText etPassword;
    Button btnSignup;
    Button btnSignupFacebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnSignupFacebook = findViewById(R.id.btnSignupFacebook);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignup();
                Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btnSignupFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFacebookSignup();
                Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


    }

    // TODO: incorporate Facebook SDK so users can sign up using pre-existing Facebook account
    private void handleFacebookSignup() {
    }

    private void handleSignup() {
    }



}