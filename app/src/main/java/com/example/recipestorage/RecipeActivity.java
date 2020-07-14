package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.recipestorage.fragments.RecipeSectionFragment;

public class RecipeActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.miIngredients:
                fragment = new RecipeSectionFragment(RecipeSectionFragment.RecipeSection.INGREDIENT);
                break;
            case R.id.miDirections:
                fragment = new RecipeSectionFragment(RecipeSectionFragment.RecipeSection.DIRECTION);
                break;
            case R.id.miNotes:
            default:
                fragment = new RecipeSectionFragment(RecipeSectionFragment.RecipeSection.NOTE);
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
//        return super.onOptionsItemSelected(item);
        return true;
    }
}