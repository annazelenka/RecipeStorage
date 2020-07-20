package com.example.recipestorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.recipestorage.fragments.RecipeSectionFragment;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddRecipeActivity extends AppCompatActivity implements RecipeSectionFragment.OnDataPass {
    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final int CAMERA_POSITION = 0;
    public static final int INGREDIENTS_POSITION = 1;
    public static final int DIRECTIONS_POSITION = 2;
    public static final int NOTES_POSITION = 3;

    private File photoFile;
    public String photoFileName = "photo.jpg";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    Toolbar toolbar;
    ImageView ivRecipeImage;
    Recipe recipe;
    EditText etRecipeTitle;
    Button btnHelp;
    FloatingActionButton fabSubmitRecipe;

    ArrayList<String> ingredients;
    ArrayList<String> directions;
    ArrayList<String> notes;

    boolean recipeDataChanged;
    BubbleNavigationLinearView bubbleNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        etRecipeTitle = findViewById(R.id.etRecipeTitle);
        bubbleNavigation = findViewById(R.id.equal_navigation_bar);

        setRecipe();

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                Fragment fragment;
                switch (position) {
                    case CAMERA_POSITION: //
                        launchCamera();
                    case INGREDIENTS_POSITION: //miDirections
                        fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.INGREDIENT, ingredients);
                        break;
                    case DIRECTIONS_POSITION: //miNotes
                        fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.DIRECTION, directions);
                        break;
                    case NOTES_POSITION:
                    default:
                        fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.NOTE, notes);
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });
        bubbleNavigation.setCurrentActiveItem(INGREDIENTS_POSITION);

        fabSubmitRecipe = findViewById(R.id.fabSubmitRecipe);
        fabSubmitRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSubmittingRecipe();
            }
        });

        setDefaultFragment();
        recipeDataChanged = false;

        btnHelp.setOnClickListener();
    }

    protected void setRecipe() {
        recipe = new Recipe();
        this.ingredients = new ArrayList<String>();
        this.directions = new ArrayList<String>();
        this.notes = new ArrayList<String>();
    }

    protected void setDefaultFragment() {
        Fragment fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.INGREDIENT, ingredients);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();

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
                directions.size() != 0 && notes.size() != 0;
        if (!canSubmitRecipe) {
            String toast = "Recipe is missing ingredients, directions, and/or notes";
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
                Intent intent = new Intent(AddRecipeActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDataPass(RecipeSectionFragment.RecipeSection recipeSection, ArrayList<String>data) {
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
    public void onDataChangedPass(boolean dataChanged) {
        this.recipeDataChanged = dataChanged;
    }



    protected void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference for future access
        photoFile =  getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(AddRecipeActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(AddRecipeActivity.this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }


    private File getResizedPhotoFileUri(Bitmap takenPhoto) {
        // by this point we have the camera photo on disk
        int imageWidth = 400;
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenPhoto, imageWidth);
        // Then we can write that smaller bitmap back to disk with:

        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File resizedFile = getPhotoFileUri(photoFileName + "_resized");
        Log.i(TAG, "reached");
        try {
            resizedFile.createNewFile();
        } catch (IOException e) {
            Log.d(TAG, "failed to resize file");
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(resizedFile);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Resized file not found");
            e.printStackTrace();
        }
        // Write the bytes of the bitmap to file
        try {
            fos.write(bytes.toByteArray());
        } catch (IOException e) {
            Log.d(TAG, "failed to write file");
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            Log.d(TAG, "failed to close FileOutputStream");
            e.printStackTrace();
        }
        return resizedFile;
    }


    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(AddRecipeActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode: " + String.valueOf(requestCode));
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.i(TAG, String.valueOf(resultCode));
            if (resultCode == RESULT_OK) { // User took picture
                // by this point we have the camera photo on disk

                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                photoFile = getResizedPhotoFileUri(takenImage);
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivRecipeImage.setImageBitmap(takenImage);
                ivRecipeImage.setVisibility(View.VISIBLE);
            } else { // Result was a failure
                Toast.makeText(AddRecipeActivity.this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}