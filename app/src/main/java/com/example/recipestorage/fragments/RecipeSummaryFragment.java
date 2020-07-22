package com.example.recipestorage.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.recipestorage.AddRecipeActivity;
import com.example.recipestorage.BitmapScaler;
import com.example.recipestorage.HomeActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

import static android.app.Activity.RESULT_OK;
import static com.example.recipestorage.AddRecipeActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;

public class RecipeSummaryFragment extends Fragment {
    public static final String TAG = "RecipeSummaryFragment";

    private File photoFile;
    public String photoFileName = "photo.jpg";

    //CAPTURE IMAGE REQUEST CODE = 1034???

    Recipe recipe;

    ImageButton btnCamera;
    ImageButton btnFavorite;
    ImageButton btnDelete;
    EditText etTitle;
    EditText etRecipeTime;
    ImageView ivRecipeImage;

    boolean isFavoriteRecipe;
    boolean hasRecipe;

    public RecipeSummaryFragment() {
        // Required empty public constructor
        this.recipe = null;
        this.hasRecipe = false;
        this.isFavoriteRecipe = false;
    }

    public RecipeSummaryFragment(Recipe setRecipe) {
        this.recipe = setRecipe;
        this.hasRecipe = true;
        this.isFavoriteRecipe = recipe.isFavorite();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCamera = view.findViewById(R.id.btnCamera);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        btnDelete = view.findViewById(R.id.btnDelete);

        ivRecipeImage = view.findViewById(R.id.ivRecipeImage);
        etTitle = view.findViewById(R.id.etTitle);
        etRecipeTime = view.findViewById(R.id.etRecipeTime);

        if (hasRecipe) {
            populateRecipeFields();
        } else {
            btnDelete.setVisibility(View.GONE);
            etTitle.setHint("Add a recipe name!");
            ivRecipeImage.setVisibility(View.INVISIBLE);
        }


        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });


        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFavoriting();
            }
        });
        ivRecipeImage.setOnClickListener( new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {

                // Single tap here.
            }

            @Override
            public void onDoubleClick(View view) {

                // Double tap here.
                handleFavoriting();
            }
            // TODO: change after testing on physical device
          }, 500));
    }

    private void populateRecipeFields() {
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });

        if (!isFavoriteRecipe) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border_black_18dp);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_black_18dp);
        }

        etTitle.setText(recipe.getTitle());
        etRecipeTime.setText(String.valueOf(recipe.getCookTimeMin()));


        boolean hasImage = (recipe.getImage() != null);
        if (hasImage) {
            ImageLoader imageLoader = Coil.imageLoader(getContext());
            LoadRequest request = LoadRequest.builder(getContext())
                    .data(recipe.getImage().getUrl())
                    .crossfade(true)
                    .target(ivRecipeImage)
                    .build();
            imageLoader.execute(request);
            scheduleStartPostponedTransition(ivRecipeImage);
        } else {
            ivRecipeImage.setVisibility(View.GONE);
        }
    }

    private void handleFavoriting() {
        isFavoriteRecipe = !isFavoriteRecipe;
        if (!isFavoriteRecipe) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border_black_18dp);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_black_18dp);
        }

        if (hasRecipe) {
            recipe.setIsFavorite(isFavoriteRecipe);
            recipe.saveRecipe();
        }
    }


    private void showDeleteDialog() {
        MaterialDialog mDialog = new MaterialDialog.Builder(getActivity())
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

    private void launchHomeActivity() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
    }

    protected void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference for future access
        photoFile =  getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
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
        File mediaStorageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

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
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Schedules the shared element transition to be started immediately
     * after the shared element has been measured and laid out within the
     * activity's view hierarchy. Some common places where it might make
     * sense to call this method are:
     *
     * (1) Inside a Fragment's onCreateView() method (if the shared element
     *     lives inside a Fragment hosted by the called Activity).
     *
     * (2) Inside a Picasso Callback object (if you need to wait for Picasso to
     *     asynchronously load/scale a bitmap before the transition can begin).
     **/
    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }
}