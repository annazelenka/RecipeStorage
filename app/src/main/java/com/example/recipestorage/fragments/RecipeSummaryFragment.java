package com.example.recipestorage.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipestorage.BitmapScaler;
import com.example.recipestorage.HomeActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

import static android.app.Activity.RESULT_OK;
import static com.example.recipestorage.AddRecipeActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;

public class RecipeSummaryFragment extends Fragment {
    public static final String TAG = "RecipeSummaryFragment";
    public final static int PICK_PHOTO_CODE = 1046;

    private File photoFile;
    public String photoFileName = "photo.jpg";
    String originalTitle;

    Recipe recipe;

    ImageButton btnCamera;
    ImageButton btnFavorite;
    ShareButton btnShare;
    ImageButton btnDelete;
    EditText etTitle;
    //EditText etRecipeTime;
    ImageView ivRecipeImage;
    FloatingActionButton fabSubmit;
    MaterialDialog mDialog;
    MaterialDialog photoDialog;
    Chip chipBreakfast;
    Chip chipLunch;
    Chip chipDinner;

    ArrayList<Chip> chips;

    boolean isFavoriteRecipe;
    boolean hasExistingRecipe;
    boolean tagsChanged;
    SharePhotoContent photoContent;
    OnDataPass dataPasser;
    int position;
    String returnFragment;

    public RecipeSummaryFragment() {
        // Required empty public constructor
        this.recipe = new Recipe();
        this.hasExistingRecipe = false;
        this.isFavoriteRecipe = false;
    }

    public RecipeSummaryFragment(Recipe setRecipe, int setPosition, String setReturnFragment) {
        this.recipe = setRecipe;
        this.originalTitle = setRecipe.getTitle();
        this.hasExistingRecipe = true;
        this.isFavoriteRecipe = recipe.isFavorite();
        this.position = setPosition;
        this.returnFragment = setReturnFragment;
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
        btnShare = view.findViewById(R.id.btnShare);
        btnDelete = view.findViewById(R.id.btnDelete);
        fabSubmit = view.findViewById(R.id.fabSubmit);
        chipBreakfast = view.findViewById(R.id.chipBreakfast);
        chipLunch = view.findViewById(R.id.chipLunch);
        chipDinner = view.findViewById(R.id.chipDinner);

        chips = new ArrayList<Chip>();
        chips.add(chipBreakfast);
        chips.add(chipLunch);
        chips.add(chipDinner);

        ivRecipeImage = view.findViewById(R.id.ivPicture);
        etTitle = view.findViewById(R.id.etTitle);
        //etRecipeTime = view.findViewById(R.id.etRecipeTime);

        etTitle.setHint("recipe title");
        tagsChanged = false;
        //etRecipeTime.setHint("recipe time");

        if (hasExistingRecipe) {
            populateRecipeFields();
            setUpFacebookShareContent();
        } else {
            btnDelete.setVisibility(View.GONE);
            btnShare.setVisibility(View.GONE);
            ivRecipeImage.setVisibility(View.GONE);
        }
        setupFabSubmit();
        setupChips();



        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhotoDialog(view);
            }
        });


        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFavoriting();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOnFacebook();
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

        etTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String text = etTitle.getText().toString();
                if (text.isEmpty()) {
                    return false;
                }
                hideSoftKeyboard(view);
                dataPasser.onChangeTitlePass(text);
                return true;
            }
        });
    }

    private void setupChips() {
        if (hasExistingRecipe) {
            // show chips as selected if recipe contains tags
            if (recipe.isBreakfast()) {
                chipBreakfast.setChecked(true);
            }

            if (recipe.isLunch()) {
                chipLunch.setChecked(true);
            }
            if (recipe.isDinner()) {
                chipDinner.setChecked(true);
            }
        }

        chipBreakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsChanged = true;
            }
        });

        chipLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsChanged = true;
            }
        });

        chipDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsChanged = true;
            }
        });

    }


    // from CodePath
    public void hideSoftKeyboard(View view){
        InputMethodManager imm =(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showPhotoDialog(View view) {
        photoDialog = new MaterialDialog.Builder(getActivity())
                .setTitle("Add photo")
                .setMessage("Add photo from gallery or take a photo?")
                .setCancelable(true)
                .setPositiveButton("photo gallery", R.drawable.ic_delete_24px, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // Delete Operation
                        onPickPhoto(view);
                        photoDialog.dismiss();
                    }
                })
                .setNegativeButton("camera", R.drawable.ic_clear_24px, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        launchCamera();
//                        dialogInterface.dismiss();
                        photoDialog.dismiss();
                    }
                })
                .build();

        // Show Dialog
        photoDialog.show();
    }

    // from CodePath guide
    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public interface OnDataPass {
        public void setOriginalTitle(String originalTitle);
        public void setReturnFragment(String returnFragment);
        public void onChangeTitlePass(String newTitle);
        public void onChangeImagePass(File newPhotoFile);
        public void onSaveRecipeSummaryPass();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (RecipeSummaryFragment.OnDataPass) context;
        dataPasser.setReturnFragment("HomeActivity");
        dataPasser.setOriginalTitle(originalTitle);
    }

    private void setupFabSubmit() {
        fabSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasTitle = hasTitle();
                if (!hasTitle) {
                    Toast.makeText(getContext(), "Title is missing!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tagsChanged) saveChangedTags();

                if (!hasExistingRecipe) {
                    launchIngredientsFragment();
                } else {
                    dataPasser.onSaveRecipeSummaryPass();
                }
            }
        });
    }

    private void launchIngredientsFragment() {
        String title = etTitle.getText().toString();
        //int recipeTime = Integer.parseInt(etRecipeTime.getText().toString());
        recipe.setTitle(title);

        RecipeSectionFragment fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.INGREDIENT, recipe);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.flContainer, fragment, "AddRecipe summary to ingredients")
                .commit();
    }

    private void saveChangedTags() {
        if (hasExistingRecipe) {
            if (chipBreakfast.isChecked() != recipe.isBreakfast()) {
                recipe.setIsBreakfast(chipBreakfast.isChecked());
            }
            if (chipLunch.isChecked() != recipe.isLunch()) {
                recipe.setIsLunch(chipLunch.isChecked());
            }
            if (chipDinner.isChecked() != recipe.isDinner()) {
                recipe.setIsDinner(chipDinner.isChecked());
            }
            recipe.saveRecipe();
        } else {
            if (chipBreakfast.isChecked()) {
                recipe.setIsBreakfast(true);
            }
            if (chipLunch.isChecked()) {
                recipe.setIsLunch(true);
            }
            if (chipDinner.isChecked()) {
                recipe.setIsDinner(true);
            }
        }
    }

    private boolean hasTitle() {
        return !etTitle.getText().toString().isEmpty();
    }

//    private boolean hasRecipeTime() {
//        return !etRecipeTime.getText().toString().isEmpty();
//    }

    private void setUpFacebookShareContent() {
        ParseFile fileObject = recipe.getImage();

        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                        SharePhoto photo = new SharePhoto.Builder()
                                .setBitmap(bmp)
                                .build();

                        String hashtag =  "#" + recipe.getTitle().replaceAll("\\s","");
                        SharePhotoContent photoContent = new SharePhotoContent.Builder()
                                .addPhoto(photo)
                                .setShareHashtag(new ShareHashtag.Builder()
                                        .setHashtag(hashtag)
                                        .build())

                                .build();

                        btnShare.setShareContent(photoContent);
                    } else {
                        Log.d("test",
                                "failed to load the image data");
                    }
                }
            });
        }


    }

    private void shareOnFacebook() {
        ShareDialog shareDialog = new ShareDialog(RecipeSummaryFragment.this);
        shareDialog.show(photoContent, ShareDialog.Mode.AUTOMATIC);
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
            btnFavorite.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
            btnFavorite.setImageResource(R.drawable.ic_favorite_black_18dp);
        }

        if (hasExistingRecipe) {
            recipe.setIsFavorite(isFavoriteRecipe);
            recipe.saveRecipe();
        }
    }

    private void showDeleteDialog() {
        mDialog = new MaterialDialog.Builder(getActivity())
                .setTitle("Delete?")
                .setMessage("Are you sure you want to delete this file?")
                .setCancelable(false)
                .setPositiveButton("Delete", R.drawable.ic_delete_24px, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // Delete Operation
                        launchHomeActivityAfterDeletion();
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

    // deletes the recipe then launches home activity
    private void launchHomeActivityAfterDeletion() {
        mDialog.dismiss();
        Intent data = new Intent();
        data.putExtra("titleToDelete", recipe.getTitle());
        data.putExtra("objectIdToDelete", recipe.getObjectId());
        data.putExtra("returnFragment", returnFragment);
        data.putExtra("position", position);
        recipe.deleteInBackground();
        getActivity().setResult(RESULT_OK, data); // set result code and bundle data for response
        getActivity().finishAfterTransition(); // closes the activity, pass data to parent
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
            photoDialog.dismiss();
            Log.i(TAG, String.valueOf(resultCode));
            if (resultCode == RESULT_OK) { // User took picture
                // by this point we have the camera photo on disk

                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                photoFile = getResizedPhotoFileUri(takenImage);
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivRecipeImage.setImageBitmap(takenImage);
                ivRecipeImage.setVisibility(View.VISIBLE);
                recipe.setImage(new ParseFile(photoFile));
                dataPasser.onChangeImagePass(photoFile);
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            photoDialog.dismiss();
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);
            photoFile = getResizedPhotoFileUri(selectedImage);
            // Load the selected image into a preview
            ivRecipeImage.setImageBitmap(selectedImage);
            ivRecipeImage.setVisibility(View.VISIBLE);
            recipe.setImage(new ParseFile(photoFile));
            dataPasser.onChangeImagePass(photoFile);
        }
    }

    // from CodePath
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
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });
    }
}