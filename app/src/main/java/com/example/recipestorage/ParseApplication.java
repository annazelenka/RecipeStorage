package com.example.recipestorage;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.facebook.ParseFacebookUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Recipe.class);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.APP_ID) // should correspond to APP_ID env variable
                .clientKey(BuildConfig.MASTER_KEY)  // set explicitly unless clientKey is explicitly configured on Parse server
                .clientBuilder(builder)
                .server(BuildConfig.SERVER_URL).build()); // USE HTTPS!!

        ParseFacebookUtils.initialize(this);
    }
}