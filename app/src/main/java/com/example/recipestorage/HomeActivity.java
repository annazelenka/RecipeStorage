package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.recipestorage.fragments.RecipeCarouselFragment;
import com.example.recipestorage.utils.RecipeTrie;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.parceler.Parcels;

import java.util.List;

//import me.ibrahimsn.lib.OnItemSelectedListener;
//import me.ibrahimsn.lib.SmoothBottomBar;

public class HomeActivity extends AppCompatActivity implements Filterable {

    private static final String TAG = "HomeActivity";
    private static final int RECIPE_LIMIT = 20;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    ImageButton btnLogout;
    Button btnAllRecipes;
    TextView tvWelcome;

    RecipeTrie recipeTrie;
    List<Recipe> allRecipes;
    ParseUser currentUser;
    boolean isFacebookUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // TODO: Set default selection
        //queryRecipes();

        btnLogout = findViewById(R.id.btnLogout);
        btnAllRecipes = findViewById(R.id.btnAllRecipes);
        tvWelcome = findViewById(R.id.tvWelcome);

        currentUser = ParseUser.getCurrentUser();
        btnAllRecipes.setVisibility(View.GONE);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });


        btnAllRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allRecipes != null) {
                    launchAllRecipesScreen();
                }
            }
        });

        String name;
        isFacebookUser = isFacebookUser();
        if (isFacebookUser) {
            name = Profile.getCurrentProfile().getFirstName();
        } else {
            name = currentUser.getUsername();
        }
        tvWelcome.setText("Welcome, " + name + "!");

        populateRecipes();
    }

    public boolean isFacebookUser() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    private void launchAllRecipesScreen() {
        Intent intent = new Intent(HomeActivity.this, AllRecipesActivity.class);
        intent.putExtra("allRecipes", Parcels.wrap(allRecipes));
        intent.putExtra("isFacebookUser", isFacebookUser);
        //intent.putExtra("recipeTrie", Parcels.wrap(recipeTrie));

        startActivity(intent);
    }

    protected void populateRecipes() {

        // query recipes
        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        query.include(Recipe.KEY_USER);
        query.setLimit(RECIPE_LIMIT);
        query.whereEqualTo(Recipe.KEY_USER, currentUser);
        query.addDescendingOrder(Recipe.KEY_CREATED_KEY);
        query.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> recipes, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting recipes", e);
                    return;
                }

                Log.i("HomeActivity", "success getting recipes");
                allRecipes = recipes;
                recipeTrie = new RecipeTrie();
                recipeTrie.populateRecipeTrie(allRecipes);
                launchRecipeCarouselFragment();
                btnAllRecipes.setVisibility(View.VISIBLE);
            }

        });

    }


    protected void launchRecipeCarouselFragment() {
        RecipeCarouselFragment fragment = new RecipeCarouselFragment(allRecipes, recipeTrie);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
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
                        dialogInterface.dismiss();
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


    @Override
    public Filter getFilter() {
        return null;
    }


}