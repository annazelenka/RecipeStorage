package com.example.recipestorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.recipestorage.fragments.RecipeSectionFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;

public class EditRecipeActivity<recipe> extends AddRecipeActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ivRecipeImage.setVisibility(View.GONE);
        etRecipeTitle.setText(recipe.getTitle());
    }

    @Override
    protected void setDefaultFragment() {
        Fragment fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.INGREDIENT, recipe.getParsedIngredients());
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    @Override
    protected void initializeRecipe() {
        recipe = (Recipe) Parcels.unwrap(getIntent().getParcelableExtra("recipe"));
        ingredients = recipe.getParsedIngredients();
        directions = recipe.getParsedDirections();
        notes = recipe.getParsedNotes();
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

    @Override
    protected boolean canSubmitRecipe() {
        boolean canSubmitRecipe = super.canSubmitRecipe() && recipeDataChanged;
        if (!canSubmitRecipe) {
            String toast = "Recipe data has not changed!";
            Toast.makeText(EditRecipeActivity.this, toast, Toast.LENGTH_SHORT).show();
        }
        return canSubmitRecipe;
    }

    @Override
    protected void saveRecipe(ParseUser currentUser, boolean hasPhotoFile, File photoFile) {
        recipe.setUser(currentUser);
        recipe.setTitle(etRecipeTitle.getText().toString());

        if (ingredientsDataChanged) {
            recipe.clearIngredients();
            recipe.saveRecipe();
            recipe.addIngredients(ingredients);
        }

        if (directionsDataChanged) {
            recipe.clearDirections();
            recipe.saveRecipe();
            recipe.addDirections(directions);
        }

        if (notesDataChanged) {
            recipe.clearNotes();
            recipe.saveRecipe();
            recipe.addNotes(notes);
        }

        if (hasPhotoFile) {
            recipe.setImage(new ParseFile(photoFile));
        }

        recipe.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(EditRecipeActivity.this, "error while saving!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(EditRecipeActivity.this, "Saved!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EditRecipeActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

}
