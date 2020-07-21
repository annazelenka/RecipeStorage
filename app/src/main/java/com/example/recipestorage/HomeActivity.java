package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.recipestorage.fragments.RecipesListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import java.util.List;

//import me.ibrahimsn.lib.OnItemSelectedListener;
//import me.ibrahimsn.lib.SmoothBottomBar;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int RECIPE_LIMIT = 20;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    FloatingActionButton fabAddRecipe;
    ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // TODO: Set default selection
        queryRecipes();

        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        btnLogout = findViewById(R.id.btnLogout);

        fabAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });
    }

    private void showLogoutDialog() {
        MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .setTitle("log out?")
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", R.drawable.ic_power_settings_new_black_18dp, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // Delete Operation
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        ParseUser.logOut();
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", R.drawable.ic_clear_24px, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                })
                .build();

        // Show Dialog
        mDialog.show();
    }

    protected void queryRecipes() {

        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        query.include(Recipe.KEY_USER);
        query.setLimit(RECIPE_LIMIT);
        query.whereEqualTo(Recipe.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Recipe.KEY_CREATED_KEY);
        query.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> recipes, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting recipes", e);
                    return;
                }

                Log.i("HomeActivity", "success getting recipes");
                RecipesListFragment fragment = new RecipesListFragment(recipes);

                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }

        });

    }


}