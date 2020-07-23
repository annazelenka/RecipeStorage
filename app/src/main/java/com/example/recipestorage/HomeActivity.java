package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.recipestorage.fragments.RecipeCarouselFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseUser;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import java.util.Map;

//import me.ibrahimsn.lib.OnItemSelectedListener;
//import me.ibrahimsn.lib.SmoothBottomBar;

public class HomeActivity extends AppCompatActivity implements Filterable {

    private static final String TAG = "HomeActivity";
    private static final int RECIPE_LIMIT = 20;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    FloatingActionButton fabAddRecipe;
    ImageButton btnLogout;
    ImageButton btnSearch;
    Button btnAllRecipes;
    EditText etSearch;
    Map<String, Recipe> recipeNameMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // TODO: Set default selection
        //queryRecipes();

        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        btnLogout = findViewById(R.id.btnLogout);
        btnSearch = findViewById(R.id.btnSearch);
        btnAllRecipes = findViewById(R.id.btnAllRecipes);
        etSearch = findViewById(R.id.etSearch);

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

        RecipeCarouselFragment fragment = new RecipeCarouselFragment();

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
        recipeNameMap = fragment.getRecipeNameMap();

//        btnSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String searchText = etSearch.getText().toString();
//
//                if (searchText.isEmpty()) {
//                    Toast.makeText(HomeActivity.this, "Search cannot be empty!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Recipe recipe = recipeNameMap.get(searchText);
//                if (recipe != null) {
//
//                }
//            }
//        });

        btnAllRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AllRecipesActivity.class);
                startActivity(intent);
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