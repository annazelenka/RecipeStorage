package com.example.recipestorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.recipestorage.fragments.RecipeSectionFragment;
import com.example.recipestorage.fragments.RecipeSummaryFragment;
import com.example.recipestorage.utils.KeyboardUtils;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
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

        etRecipeTitle.setText(recipe.getTitle());
        setupBubbleNavigation();

        setVisibilityFabSubmit(true);

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if (isVisible) {
                    // hide fabSubmit button and bubble navigation bar
                    fabSubmitRecipe.setVisibility(View.INVISIBLE);
                    bubbleNavigation.setVisibility(View.INVISIBLE);
                } else {
                    fabSubmitRecipe.setVisibility(View.VISIBLE);
                    bubbleNavigation.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void setDefaultFragment() {
        toolbar.setVisibility(View.GONE);
        Fragment fragment = new RecipeSummaryFragment(recipe);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    @Override
    protected void initializeRecipe() {
        recipe = (Recipe) Parcels.unwrap(getIntent().getParcelableExtra("recipe"));
        ingredients = recipe.getParsedIngredients();
        directions = recipe.getParsedDirections();
        notes = recipe.getParsedNotes();
    }

    public void setupBubbleNavigation() {
        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                Fragment fragment;
                switch (position) {
                    case HOME_POSITION:
                        toolbar.setVisibility(View.GONE);
                        fragment = new RecipeSummaryFragment(recipe);
                        break;
                    case INGREDIENTS_POSITION: //miDirections
                        toolbar.setVisibility(View.VISIBLE);
                        fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.INGREDIENT, ingredients);
                        break;
                    case DIRECTIONS_POSITION: //miNotes
                        toolbar.setVisibility(View.VISIBLE);
                        fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.DIRECTION, directions);
                        break;
                    case NOTES_POSITION:
                    default:
                        toolbar.setVisibility(View.VISIBLE);
                        fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.NOTE, notes);
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });
        bubbleNavigation.setCurrentActiveItem(HOME_POSITION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.miDelete:
                showDeleteDialog();
                return true;
            case R.id.miHome:
                fragment = new RecipeSummaryFragment(recipe);
                break;
            case R.id.miIngredients:
                toolbar.setVisibility(View.VISIBLE);
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.INGREDIENT, recipe.getParsedIngredients());
                break;
            case R.id.miDirections:
                toolbar.setVisibility(View.VISIBLE);
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.DIRECTION, recipe.getParsedDirections());
                break;
            case R.id.miNotes:
            default:
                toolbar.setVisibility(View.VISIBLE);
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.NOTE, recipe.getParsedNotes());
                break;

        }
        //fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
        startAnimatedFragment(fragment);
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
