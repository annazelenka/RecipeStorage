package com.example.recipestorage.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipestorage.AllRecipesActivity;
import com.example.recipestorage.EditRecipeActivity;
import com.example.recipestorage.HomeActivity;
import com.example.recipestorage.LoginActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.utils.RecipeTrie;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.jama.carouselview.CarouselView;
import com.jama.carouselview.CarouselViewListener;
import com.jama.carouselview.enums.IndicatorAnimationType;
import com.jama.carouselview.enums.OffsetType;
import com.parse.ParseUser;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

import org.parceler.Parcels;

import java.util.List;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

public class HomeFragment extends Fragment {

    TextView tvWelcome;
    CarouselView carouselView;
    ParseUser currentUser;

    boolean isFacebookUser;
    List<Recipe> allRecipes;
    RecipeTrie trie;

    public HomeFragment(List<Recipe> setAllRecipes, RecipeTrie setTrie) {
        // Required empty public constructor
        this.allRecipes = setAllRecipes;
        this.trie = setTrie;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        carouselView = view.findViewById(R.id.carouselView);
        currentUser = ParseUser.getCurrentUser();

        String name;
        isFacebookUser = isFacebookUser();
        if (isFacebookUser) {
            name = Profile.getCurrentProfile().getFirstName();
        } else {
            name = currentUser.getUsername();
        }
        tvWelcome.setText("Welcome, " + name + "!");

        handleCarouselView(view);
    }

    public boolean isFacebookUser() {
        return AccessToken.getCurrentAccessToken() != null;
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
                ImageButton btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

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
                    ivPicture.setVisibility(View.INVISIBLE);
                }

                tvTitle.setText(recipe.getTitle());


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


}