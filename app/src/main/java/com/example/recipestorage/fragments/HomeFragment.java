package com.example.recipestorage.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.example.recipestorage.EditRecipeActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.utils.RecipeTrie;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jama.carouselview.CarouselView;
import com.jama.carouselview.CarouselViewListener;
import com.jama.carouselview.enums.IndicatorAnimationType;
import com.jama.carouselview.enums.OffsetType;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

public class HomeFragment extends Fragment {
    public static final int EDIT_REQUEST_CODE = 5;
    public static final int DEFAULT_SIZE = 5;

    TextView tvWelcome;
//    TextView tvNoRecipes;
    CarouselView carouselView;
    ChipGroup chipGroup;
    Chip chipFavorites;
    Chip chipBreakfast;
    Chip chipLunch;
    Chip chipDinner;
    ParseUser currentUser;

    boolean isFacebookUser;
    boolean shouldIncludeFavorites;
    boolean shouldIncludeBreakfast;
    boolean shouldIncludeLunch;
    boolean shouldIncludeDinner;

    List<Recipe> originalAllRecipes;
    List<Recipe> displayedRecipes;
    List<Recipe> favoriteRecipes;
    List<Recipe> breakfastRecipes;
    List<Recipe> lunchRecipes;
    List<Recipe> dinnerRecipes;
    RecipeTrie trie;

    View onCreatedView;

    SkeletonScreen skeletonScreen;
    List<SkeletonScreen> screens;
    boolean isLoading;
    Set<Recipe> filteredRecipes;

    public HomeFragment(List<Recipe> setAllRecipes, RecipeTrie setTrie, boolean setIsLoading) {
        this.originalAllRecipes = setAllRecipes;
        this.displayedRecipes = setAllRecipes;
        this.trie = setTrie;
        this.isLoading = setIsLoading;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        carouselView = view.findViewById(R.id.carouselView);
        currentUser = ParseUser.getCurrentUser();
        chipGroup = view.findViewById(R.id.chipGroup);
        chipFavorites = view.findViewById(R.id.chipFavorites);
        chipBreakfast = view.findViewById(R.id.chipBreakfast);
        chipLunch = view.findViewById(R.id.chipLunch);
        chipDinner = view.findViewById(R.id.chipDinner);
//        tvNoRecipes = view.findViewById(R.id.tvNoRecipes);

        String name;
        isFacebookUser = isFacebookUser();
        if (isFacebookUser) {
            name = Profile.getCurrentProfile().getFirstName();
        } else {
            name = currentUser.getUsername();
        }
        tvWelcome.setText("Welcome, " + name + "!");

        onCreatedView = view;
        screens = new ArrayList<SkeletonScreen>();
        int allRecipesSize = displayedRecipes == null ? 0 : displayedRecipes.size();
        setupSkeleton();
        setupChips();
    }

    public boolean isFacebookUser() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    public void setFilteredRecipes(List<Recipe> setFavoriteRecipes, List<Recipe> setBreakfastRecipes, List<Recipe> setLunchRecipes, List<Recipe> setDinnerRecipes) {
        this.favoriteRecipes = setFavoriteRecipes;
        this.breakfastRecipes = setBreakfastRecipes;
        this.lunchRecipes = setLunchRecipes;
        this.dinnerRecipes = setDinnerRecipes;
    }

    public void setOriginalAllRecipes(List<Recipe> setAllRecipes) {
        this.originalAllRecipes = setAllRecipes;
    }

    public void reloadCarouselView(Set<Recipe> filteredRecipes) {
        List<Recipe> filteredList = new ArrayList<Recipe>();
        filteredList.addAll(filteredRecipes);
        reloadCarouselView(filteredList, false);
    }

    public void reloadCarouselView(List<Recipe> setAllRecipes, boolean shouldUseSkeleton) {
        carouselView = onCreatedView.findViewById(R.id.carouselView);
        this.displayedRecipes = setAllRecipes;
        int recipesSize = setAllRecipes == null ? 0 : setAllRecipes.size();
        if (skeletonScreen == null) {
            setupSkeleton();
        }

        final Runnable r = new Runnable() {
            public void run() {
                if (shouldUseSkeleton) setupCarouselViewWithSkeletonLoad(onCreatedView, recipesSize, false);
                else setupCarouselView(onCreatedView, recipesSize);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(r, 500);
    }

    private void setupChips() {
        chipFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldIncludeFavorites = chipFavorites.isChecked();
                filter();
            }
        });

        chipBreakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldIncludeBreakfast = chipBreakfast.isChecked();
                filter();
            }
        });

        chipLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldIncludeLunch = chipLunch.isChecked();
                filter();
            }
        });

        chipDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldIncludeDinner = chipDinner.isChecked();
                filter();
            }
        });

        chipFavorites.setChecked(true);
        shouldIncludeFavorites = true;
    }

    public void filter() {
        filteredRecipes = new HashSet<Recipe>();

        if (shouldIncludeFavorites) {
            filteredRecipes.addAll(favoriteRecipes);
        }
        if (shouldIncludeBreakfast) {
            filteredRecipes.addAll(breakfastRecipes);
        }
        if (shouldIncludeLunch) {
            filteredRecipes.addAll(lunchRecipes);
        }
        if (shouldIncludeDinner) {
            filteredRecipes.addAll(dinnerRecipes);
        }

        if (!shouldIncludeFavorites && filteredRecipes.isEmpty()) {
            reloadCarouselView(originalAllRecipes, true);
            return;
        }
        reloadCarouselView(filteredRecipes);

//        if (filteredRecipes.isEmpty()) {
//            tvNoRecipes.setVisibility(View.VISIBLE);
//        } else {
//            tvNoRecipes.setVisibility(View.GONE);
//        }
    }

    public void setupSkeleton() {
        int layout = R.layout.item_recipe_skeleton_carousel;
        carouselView.setSize(DEFAULT_SIZE);
        carouselView.setResource(layout);
        carouselView.setAutoPlay(false);
        carouselView.setScaleOnScroll(true);
        carouselView.setIndicatorAnimationType(IndicatorAnimationType.SLIDE);
        carouselView.setCarouselOffset(OffsetType.CENTER);
        carouselView.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int position) {
                // Example here is setting up a full image carousel
                skeletonScreen = Skeleton.bind(view)
                        .load(R.layout.item_recipe_skeleton)
                        .shimmer(true)
                        .duration(200)
                        .show();
                screens.add(skeletonScreen);

            }

        });
        carouselView.show();
    }

    private void setupCarouselView(View view, int size) {
        carouselView = view.findViewById(R.id.carouselView);
        int layout = R.layout.item_recipe_preview;
        carouselView.setSize(size);
        carouselView.setResource(layout);
        carouselView.setAutoPlay(false);
        carouselView.setScaleOnScroll(true);
        carouselView.setIndicatorAnimationType(IndicatorAnimationType.SLIDE);
        carouselView.setCarouselOffset(OffsetType.CENTER);
        carouselView.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int position) {
                final TextView tvTitle = view.findViewById(R.id.tvTitle);
                final ImageView ivPicture = view.findViewById(R.id.ivPicture);
                ImageButton btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

                ImageLoader imageLoader = Coil.imageLoader(getContext());

                if (displayedRecipes == null || displayedRecipes.size() == 0 || position >= displayedRecipes.size()) {
                    return;
                }


                final Recipe recipe = displayedRecipes.get(position);
                boolean hasImage = (recipe.getImage() != null);
                if (hasImage) {
                    LoadRequest request = LoadRequest.builder(getContext())
                            .data(recipe.getImage().getUrl())
                            .crossfade(true)
                            .target(ivPicture)
                            .build();
                    imageLoader.execute(request);
                } else {
                    ivPicture.setVisibility(View.INVISIBLE);
                }
                tvTitle.setText(recipe.getTitle());

                btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchRecipeEditActivity(recipe, ivPicture, tvTitle, position);
                    }
                });
            }
        });
       carouselView.show();
    }


    private void setupCarouselViewWithSkeletonLoad(View view, int allRecipesSize, boolean isSkeletonView) {
        carouselView = view.findViewById(R.id.carouselView);
//        tvNoRecipes.setVisibility(View.GONE);

        int layout = R.layout.item_recipe_preview;
        carouselView.setSize(allRecipesSize);
        carouselView.setResource(layout);
        carouselView.setAutoPlay(false);
        carouselView.setScaleOnScroll(true);
        carouselView.setIndicatorAnimationType(IndicatorAnimationType.SLIDE);
        carouselView.setCarouselOffset(OffsetType.CENTER);
        carouselView.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int position) {
                skeletonScreen = Skeleton.bind(view)
                        .load(R.layout.item_recipe_skeleton)
                        .show();
                screens.add(skeletonScreen);

                final TextView tvTitle = view.findViewById(R.id.tvTitle);
                final ImageView ivPicture = view.findViewById(R.id.ivPicture);
                ImageButton btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

                ImageLoader imageLoader = Coil.imageLoader(getContext());

                if (displayedRecipes == null || displayedRecipes.size() == 0 || position >= displayedRecipes.size()) {
                    return;
                }


                final Recipe recipe = displayedRecipes.get(position);
                boolean hasImage = (recipe.getImage() != null);
                if (hasImage) {
                    LoadRequest request = LoadRequest.builder(getContext())
                            .data(recipe.getImage().getUrl())
                            .crossfade(true)
                            .target(ivPicture)
                            .build();
                    imageLoader.execute(request);
                } else {
                    ivPicture.setVisibility(View.INVISIBLE);
                }
                tvTitle.setText(recipe.getTitle());

                btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchRecipeEditActivity(recipe, ivPicture, tvTitle, position);
                    }
                });
            }
        });
        // After you finish setting up, show the CarouselView
        final Runnable r = new Runnable() {
            public void run() {

                for (SkeletonScreen screen: screens) {
                    screen.hide();
                }
                carouselView.show();

            }
        };
        Handler handler = new Handler();
        handler.postDelayed(r, 500);
    }

    private void launchRecipeEditActivity(Recipe recipe, ImageView ivPicture, TextView tvTitle, int position) {
        Intent intent = new Intent(getContext(), EditRecipeActivity.class);
        intent.putExtra("recipe", Parcels.wrap(recipe));
        intent.putExtra("returnFragment", "HomeFragment");
        intent.putExtra("position", position);
        Pair<View, String> p1 = Pair.create((View)ivPicture, "recipeImage");
        Pair<View, String> p2 = Pair.create((View)tvTitle, "tvTitle");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), p1, p2);
        getActivity().startActivityForResult(intent, EDIT_REQUEST_CODE, options.toBundle());
    }

}