package com.example.recipestorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipestorage.fragments.RecipeSectionFragment;
import com.example.recipestorage.fragments.RecipeSummaryFragment;
import com.example.recipestorage.utils.KeyboardUtils;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddRecipeActivity extends AppCompatActivity implements RecipeSectionFragment.OnDataPass, RecipeSummaryFragment.OnDataPass {
    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final int CAMERA_POSITION = 4;
    public static final int INGREDIENTS_POSITION = 1;
    public static final int DIRECTIONS_POSITION = 2;
    public static final int NOTES_POSITION = 3;
    public static final int HOME_POSITION = 0;

    private File photoFile;
    public String photoFileName = "photo.jpg";
    String originalTitle;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    Toolbar toolbar;
    ImageView ivRecipeImage;
    Recipe recipe;
    //EditText etRecipeTitle;
    TextView tvTitle;
    FloatingActionButton fabSubmitRecipe;

    String title;
    ArrayList<String> ingredients;
    ArrayList<String> directions;
    ArrayList<String> notes;

    boolean recipeDataChanged;
    boolean titleChanged;
    boolean imageChanged;
    boolean ingredientsDataChanged;
    boolean directionsDataChanged;
    boolean notesDataChanged;
    BubbleNavigationConstraintView bubbleNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        //etRecipeTitle = findViewById(R.id.etRecipeTitle);
        tvTitle = findViewById(R.id.tvTitle);
        bubbleNavigation = findViewById(R.id.equal_navigation_bar);

        initializeRecipe();

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        fabSubmitRecipe = findViewById(R.id.fabSubmitRecipe);
        fabSubmitRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSubmittingRecipe();
            }
        });

        setDefaultFragment();
        titleChanged = false;
        imageChanged = false;
        recipeDataChanged = false;
        ingredientsDataChanged = false;
        directionsDataChanged = false;
        notesDataChanged = false;

        bubbleNavigation.setVisibility(View.GONE);
        setVisibilityFabSubmit(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        return true;
    }

    protected void initializeRecipe() {
        recipe = new Recipe();
        this.ingredients = new ArrayList<String>();
        this.directions = new ArrayList<String>();
        this.notes = new ArrayList<String>();
    }

    protected void setDefaultFragment() {
        toolbar.setVisibility(View.GONE);
        Fragment fragment = new RecipeSummaryFragment();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    protected void startAnimatedFragment(Fragment newFragment) {
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fts.replace(R.id.flContainer, newFragment, "fragment");
        fts.commit();
    }

    private void handleSubmittingRecipe() {
        boolean canSubmitRecipe = canSubmitRecipe();

        if (!canSubmitRecipe) {
            return;
        }

        if (photoFile == null || ivRecipeImage.getDrawable() == null) {
            Toast.makeText(AddRecipeActivity.this, "There is no image!", Toast.LENGTH_SHORT).show();
            saveRecipe(ParseUser.getCurrentUser(), false, photoFile);
            launchHomeActivity();
        } else {
            saveRecipe(ParseUser.getCurrentUser(), true, photoFile);
        }
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    protected boolean canSubmitRecipe() {
        // TODO: add title checking
        boolean canSubmitRecipe =  ingredients.size() != 0 &&
                directions.size() != 0;
        if (!canSubmitRecipe) {
            String toast = "Recipe is missing ingredients and/or directions";
            Toast.makeText(AddRecipeActivity.this, toast, Toast.LENGTH_SHORT).show();
        }
        return canSubmitRecipe;
    }

    protected void saveRecipe(ParseUser currentUser, boolean hasPhotoFile, File photoFile) {
        recipe.setUser(currentUser);
        //recipe.setTitle(etRecipeTitle.getText().toString());
        recipe.addIngredients(ingredients);
        recipe.addDirections(directions);
        recipe.addNotes(notes);

        if (hasPhotoFile) {
            recipe.setImage(new ParseFile(photoFile));
        }

        recipe.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(AddRecipeActivity.this, "error while saving!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(AddRecipeActivity.this, "Saved!", Toast.LENGTH_LONG).show();
                launchHomeActivity();
            }
        });
    }

    private void launchHomeActivity() {
        Intent intent = new Intent(AddRecipeActivity.this, HomeActivity.class);
        startActivity(intent);
        finishAfterTransition();
    }


    @Override
    public void onAddDataPass(RecipeSectionFragment.RecipeSection recipeSection, ArrayList<String>data) {
        Log.d("LOG","hello " + data);
        switch (recipeSection) {
            case INGREDIENT:
                ingredients = data;
                break;
            case DIRECTION:
                directions = data;
                break;
            case NOTE:
            default:
                notes = data;
                break;
        }
    }

    @Override
    public void setVisibilityFabSubmit(boolean setVisible) {
        if (setVisible) {
            fabSubmitRecipe.setVisibility(View.VISIBLE);
        } else {
            fabSubmitRecipe.setVisibility(View.GONE);
        }
    }

    @Override
    public void setVisibilityBubbleNavigation(boolean setVisible) {
        if (setVisible) {
            bubbleNavigation.setVisibility(View.VISIBLE);
        } else {
            bubbleNavigation.setVisibility(View.GONE);
        }
    }

    @Override
    public void onIngredientsChangedPass(boolean dataChanged) {
        this.recipeDataChanged = dataChanged;
        this.ingredientsDataChanged = dataChanged;
    }

    @Override
    public void onDirectionsChangedPass(boolean dataChanged) {
        this.recipeDataChanged = dataChanged;
        this.directionsDataChanged = dataChanged;
    }

    @Override
    public void onNotesChangedPass(boolean dataChanged) {
        this.recipeDataChanged = dataChanged;
        this.notesDataChanged = dataChanged;
    }


    @Override
    public void setOriginalTitle(String setOriginalTitle) {
        this.originalTitle = setOriginalTitle;
    }

    @Override
    public void onChangeTitlePass(String newTitle) {
        title = newTitle;
        tvTitle.setText(newTitle);
        this.titleChanged = true;
        this.recipeDataChanged = true;
    }

    @Override
    public void onChangeImagePass(File newImage) {
        this.photoFile = newImage;
        this.imageChanged = true;
        this.recipeDataChanged = true;
    }
}