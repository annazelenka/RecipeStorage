package com.example.recipestorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.recipestorage.adapters.RecipePagerAdapter;
import com.example.recipestorage.fragments.RecipeSummaryFragment;
import com.example.recipestorage.utils.KeyboardUtils;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;

public class EditRecipeActivity<recipe> extends AddRecipeActivity {

    int adapterPosition;
    String returnFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postponeEnterTransition();

        tvTitle.setText(recipe.getTitle());
        setDefaultFragment();
        setupTabLayout();

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if (isVisible) {
                } else {
                }
            }
        });
        FrameLayout flContainer = findViewById(R.id.flContainer);
        flContainer.setVisibility(View.GONE);
    }

    @Override
    protected void setupTabLayout() {
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        RecipePagerAdapter adapterViewPager = new RecipePagerAdapter(fragmentManager, EditRecipeActivity.this, toolbar,
                recipe, adapterPosition, returnFragment);
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(HOME_POSITION);
        vpPager.setVisibility(View.VISIBLE);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(vpPager);
        tabLayout.setVisibility(View.VISIBLE);

        int[] imageResId = {
                R.drawable.default_icon,
                R.drawable.ic_fastfood_black_18dp,
                R.drawable.ic_list_24px,
                R.drawable.ic_content_paste_black_18dp
        };

        for (int i = 0; i < imageResId.length; i++) {
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }

    }

    @Override
    protected void setDefaultFragment() {
        returnFragment = getIntent().getStringExtra("returnFragment");
        adapterPosition = getIntent().getIntExtra("position",0);
        toolbar.setVisibility(View.GONE);
        Fragment fragment = new RecipeSummaryFragment(recipe, adapterPosition, returnFragment);
//        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    @Override
    protected void initializeRecipe() {
        recipe = (Recipe) Parcels.unwrap(getIntent().getParcelableExtra("recipe"));
        ingredients = recipe.getParsedIngredients();
        directions = recipe.getParsedDirections();
        notes = recipe.getParsedNotes();
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
        setResult(RESULT_OK, intent); // set result code and bundle data for response
        finishAfterTransition(); // closes the activity, pass data to parent
    }
}
