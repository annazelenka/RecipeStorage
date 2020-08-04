package com.example.recipestorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

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
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddRecipeActivity extends AppCompatActivity implements RecipeSectionFragment.OnDataPass, RecipeSummaryFragment.OnDataPass {
    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final int HOME_POSITION = 0;

    private File photoFile;
    public String photoFileName = "photo.jpg";
    String originalTitle;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    Toolbar toolbar;
    ImageView ivRecipeImage;
    Recipe recipe;
    TextView tvTitle;

    String title;
    String returnFragment;
    ArrayList<String> ingredients;
    ArrayList<String> directions;
    ArrayList<String> notes;
    ParseUser currentUser;

    boolean recipeDataChanged;
    boolean titleChanged;
    boolean imageChanged;
    boolean ingredientsDataChanged;
    boolean directionsDataChanged;
    boolean notesDataChanged;

    //BubbleNavigationConstraintView bubbleNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvTitle = findViewById(R.id.tvTitle);

        initializeRecipe();
        setupTabLayout();

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setDefaultFragment();
        titleChanged = false;
        imageChanged = false;
        recipeDataChanged = false;
        ingredientsDataChanged = false;
        directionsDataChanged = false;
        notesDataChanged = false;
        currentUser = ParseUser.getCurrentUser();
    }

    protected void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setVisibility(View.GONE);
        ViewPager vpPager = findViewById(R.id.vpPager);
        vpPager.setVisibility(View.GONE);
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

    private void handleSubmittingRecipe() {
        boolean canSubmitRecipe = canSubmitRecipe();

        if (!canSubmitRecipe) {
            return;
        }

        if (photoFile == null || ivRecipeImage.getDrawable() == null) {
            Toast.makeText(AddRecipeActivity.this, "There is no image!", Toast.LENGTH_SHORT).show();
            saveRecipe(currentUser, false, photoFile);
            launchHomeActivity();
        } else {
            saveRecipe(currentUser, true, photoFile);
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

    protected void launchHomeActivity() {
        Intent intent = new Intent();
        intent.putExtra("newRecipe", Parcels.wrap(recipe));
        intent.putExtra("returnFragment", Parcels.wrap(returnFragment));
        // set result code and bundle data for response
        setResult(AddRecipeActivity.RESULT_OK, intent);
        // closes the activity, pass data to parent
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
    public void setReturnFragment(String setReturnFragment) {
        this.returnFragment = setReturnFragment;
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

    @Override
    public void onSaveRecipeSummaryPass() {
        saveRecipe(currentUser, imageChanged, photoFile);
    }

    @Override
    public void onSaveNewRecipePass(Recipe newRecipe) {
        recipe = newRecipe;
        saveRecipe(currentUser, imageChanged, photoFile);
    }

    @Override
    public void onSaveExistingRecipePass() {
        saveRecipe(currentUser, imageChanged, photoFile);
    }
}