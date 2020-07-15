package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.recipestorage.fragments.RecipesListFragment;
import com.example.recipestorage.fragments.RecipesSharedFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

//import me.ibrahimsn.lib.OnItemSelectedListener;
//import me.ibrahimsn.lib.SmoothBottomBar;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
//    private SmoothBottomBar bottomBar;

    Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        bottomBar = findViewById(R.id.bottomBar);
//
//        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public boolean onItemSelect(int i) {
//                Fragment fragment = null;
//                boolean isFragment = true;
//                switch (i) {
//                    case 0:
//                        fragment = new RecipesListFragment();
//                        break;
//                    case 1:
//                        fragment = new RecipesSharedFragment();
//                        break;
//                    case 2:
//                    default:
//                        Intent intent = new Intent(HomeActivity.this, RecipeActivity.class);
//                        startActivity(intent);
//                        isFragment = false;
//                }
//                if (isFragment) {
//                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
//                }
//                // TODO: change if needed when launching RecipeActivity
//                return true;
//            }
//        });
        // TODO: Set default selection
        queryRecipes();

    }

    protected void queryRecipes() {

        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        query.include(Recipe.KEY_USER);
        query.setLimit(5);
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