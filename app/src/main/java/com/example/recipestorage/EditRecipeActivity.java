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
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.parceler.Parcels;

import java.io.File;

public class EditRecipeActivity<recipe> extends AddRecipeActivity {

    int adapterPosition;
    String returnFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvTitle.setText(recipe.getTitle());

        setupBubbleNavigation();
        setDefaultFragment();
//        setVisibilityFabSubmit(false);
        //fabSubmitRecipe.setVisibility(View.GONE);

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if (isVisible) {
                    // hide fabSubmit button and bubble navigation bar
                    //fabSubmitRecipe.setVisibility(View.INVISIBLE);
                    bubbleNavigation.setVisibility(View.INVISIBLE);
                } else {
                    //fabSubmitRecipe.setVisibility(View.VISIBLE);
                    bubbleNavigation.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void setDefaultFragment() {
        returnFragment = getIntent().getStringExtra("returnFragment");
        adapterPosition = getIntent().getIntExtra("position",0);
        toolbar.setVisibility(View.GONE);
        Fragment fragment = new RecipeSummaryFragment(recipe, adapterPosition, returnFragment);
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
                        fragment = new RecipeSummaryFragment(recipe, adapterPosition, returnFragment);
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

        if (titleChanged) {
            recipe.setTitle(title);
        }

        if (imageChanged) {
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

                launchHomeActivity();
            }
        });
    }

    @Override
    protected void launchHomeActivity() {
        String returnFragment = getIntent().getStringExtra("fragmentType");

        Intent intent = new Intent();
        intent.putExtra("recipeToEdit", Parcels.wrap(recipe));
        intent.putExtra("returnFragment", returnFragment);
        intent.putExtra("position", adapterPosition);

        if (titleChanged) {
            intent.putExtra("originalTitle", originalTitle);
        }
        // Activity finished ok, return the data
        setResult(RESULT_OK, intent); // set result code and bundle data for response
        finishAfterTransition(); // closes the activity, pass data to parent
    }

}
