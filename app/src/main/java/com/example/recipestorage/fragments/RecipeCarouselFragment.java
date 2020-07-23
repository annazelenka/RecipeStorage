package com.example.recipestorage.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.EditRecipeActivity;
import com.jama.carouselview.CarouselView;
import com.jama.carouselview.CarouselViewListener;
import com.jama.carouselview.enums.IndicatorAnimationType;
import com.jama.carouselview.enums.OffsetType;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

public class RecipeCarouselFragment extends Fragment {
    public static final String TAG = "RecipeListFragment";
    public static final int RECIPE_LIMIT = 20;

    private List<Recipe> allRecipes;
    CarouselView carouselView;

    Map<String, Recipe> recipeNameMap;

    public RecipeCarouselFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_carousel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (allRecipes == null) {
            populateRecipes(view);
        }
    }

    private void handleCarouselView(View view) {
        carouselView = view.findViewById(R.id.carouselView);

        carouselView.setSize(allRecipes.size());
        carouselView.setResource(R.layout.item_recipe_preview);
        carouselView.setAutoPlay(false);
        carouselView.setScaleOnScroll(true);
        carouselView.setIndicatorAnimationType(IndicatorAnimationType.SLIDE);
        carouselView.setCarouselOffset(OffsetType.CENTER);
        carouselView.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int position) {
                // Example here is setting up a full image carousel
                final TextView tvTitle = view.findViewById(R.id.tvTitle);
                final ImageView ivPicture = view.findViewById(R.id.ivPicture);
                Button btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

                ivPicture.setTransitionName("recipeImage");
                tvTitle.setTransitionName("recipeTitle");


                ImageLoader imageLoader = Coil.imageLoader(getContext());

                if (allRecipes == null || allRecipes.size() == 0 || position >= allRecipes.size()) {
                    return;
                }

                final Recipe recipe = allRecipes.get(position);
                boolean hasImage = (recipe.getImage() != null);
                if (hasImage) {
                    LoadRequest request = LoadRequest.builder(getContext())
                            .data(recipe.getImage().getUrl())
                            .crossfade(true)
                            .target(ivPicture)
                            .build();
                    imageLoader.execute(request);
                } else {
                    ivPicture.setVisibility(View.GONE);
                }

                tvTitle.setText(recipe.getTitle());
                recipeNameMap.put(recipe.getTitle(), recipe);

                btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchRecipeEditActivity(recipe, ivPicture, tvTitle);
                    }
                });
            }
        });
        // After you finish setting up, show the CarouselView
        carouselView.show();
    }

    private void launchRecipeEditActivity(Recipe recipe, ImageView ivPicture, TextView tvTitle) {
        Intent intent = new Intent(getContext(), EditRecipeActivity.class);
        intent.putExtra("recipe", Parcels.wrap(recipe));
        Pair<View, String> p1 = Pair.create((View)ivPicture, "recipeImage");
        Pair<View, String> p2 = Pair.create((View)tvTitle, "tvTitle");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), p1, p2);
        startActivity(intent, options.toBundle());

    }

    public void setRecipes(List<Recipe> recipes) {
        allRecipes = recipes;

    }

    protected void populateRecipes(final View view) {

        // query recipes
        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        query.include(Recipe.KEY_USER);
        query.setLimit(RECIPE_LIMIT);
        query.whereEqualTo(Recipe.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Recipe.KEY_CREATED_KEY);
        query.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> recipes, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting recipes", e);
                    return;
                }

                Log.i("HomeActivity", "success getting recipes");
                allRecipes = recipes;
                recipeNameMap = new HashMap<String, Recipe>();
                handleCarouselView(view);
            }

        });

    }

    public Map<String, Recipe> getRecipeNameMap() {
        return recipeNameMap;
    }




}