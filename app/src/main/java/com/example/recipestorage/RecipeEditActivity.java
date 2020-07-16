package com.example.recipestorage;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.example.recipestorage.fragments.RecipeSectionFragment;
import com.google.android.material.appbar.AppBarLayout;

import org.parceler.Parcels;

public class RecipeEditActivity extends RecipeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipe = (Recipe) Parcels.unwrap(getIntent().getParcelableExtra("recipe"));
        toolbar.setBackgroundResource(R.drawable.burger);
        ivRecipeImage.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.miCamera:
                super.launchCamera();
            case R.id.miIngredients:
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.INGREDIENT, recipe.getParsedIngredients());
                break;
            case R.id.miDirections:
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.DIRECTION, recipe.getParsedDirections());
                break;
            case R.id.miNotes:
            default:
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.NOTE, recipe.getParsedNotes());
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
        return true;
    }

}
