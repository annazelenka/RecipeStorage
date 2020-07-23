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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class AddRecipeActivity extends AppCompatActivity implements RecipeSectionFragment.OnDataPass {
    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final int CAMERA_POSITION = 4;
    public static final int INGREDIENTS_POSITION = 1;
    public static final int DIRECTIONS_POSITION = 2;
    public static final int NOTES_POSITION = 3;
    public static final int HOME_POSITION = 0;

    private File photoFile;
    public String photoFileName = "photo.jpg";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    Toolbar toolbar;
    ImageView ivRecipeImage;
    Recipe recipe;
    EditText etRecipeTitle;
    ImageButton btnHelp;
    ImageButton btnDelete;
    FloatingActionButton fabSubmitRecipe;

    ArrayList<String> ingredients;
    ArrayList<String> directions;
    ArrayList<String> notes;

    boolean recipeDataChanged;
    boolean ingredientsDataChanged;
    boolean directionsDataChanged;
    boolean notesDataChanged;
    BubbleNavigationConstraintView bubbleNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        etRecipeTitle = findViewById(R.id.etRecipeTitle);
        bubbleNavigation = findViewById(R.id.equal_navigation_bar);
        btnDelete = findViewById(R.id.btnDelete);

        initializeRecipe();

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupBubbleNavigation();

        fabSubmitRecipe = findViewById(R.id.fabSubmitRecipe);
        fabSubmitRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSubmittingRecipe();
            }
        });

        setDefaultFragment();
        recipeDataChanged = false;
        ingredientsDataChanged = false;
        directionsDataChanged = false;
        notesDataChanged = false;

        //btnHelp.setOnClickListener();
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });

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

    protected void setupBubbleNavigation() {
        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                Fragment fragment;
                switch (position) {
                    case HOME_POSITION:
                        toolbar.setVisibility(View.GONE);
                        fragment = new RecipeSummaryFragment();
                        break;
                    case INGREDIENTS_POSITION: //miDirections
                        toolbar.setVisibility(View.VISIBLE);
                        fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.INGREDIENT, ingredients);
                        break;
                    case DIRECTIONS_POSITION: //miNotes
                        toolbar.setVisibility(View.VISIBLE);
                        fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.DIRECTION, directions);
                        break;
                    case NOTES_POSITION:
                    default:
                        toolbar.setVisibility(View.VISIBLE);
                        fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.NOTE, notes);
                        break;
                }
                startAnimatedFragment(fragment);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });
        bubbleNavigation.setCurrentActiveItem(HOME_POSITION);
    }

    private void showDeleteDialog() {
        MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .setTitle("Delete?")
                .setMessage("Are you sure you want to delete this file?")
                .setCancelable(false)
                .setPositiveButton("Delete", R.drawable.ic_delete_24px, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // Delete Operation
                        recipe.deleteInBackground();
                        launchHomeActivity();
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
        } else {
            saveRecipe(ParseUser.getCurrentUser(), true, photoFile);
        }
    }

    protected boolean canSubmitRecipe() {
        boolean canSubmitRecipe = !etRecipeTitle.getText().toString().isEmpty() && ingredients.size() != 0 &&
                directions.size() != 0;
        if (!canSubmitRecipe) {
            String toast = "Recipe is missing ingredients and/or directions";
            Toast.makeText(AddRecipeActivity.this, toast, Toast.LENGTH_SHORT).show();
        }
        return canSubmitRecipe;
    }

    protected void saveRecipe(ParseUser currentUser, boolean hasPhotoFile, File photoFile) {
        recipe.setUser(currentUser);
        recipe.setTitle(etRecipeTitle.getText().toString());
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


}