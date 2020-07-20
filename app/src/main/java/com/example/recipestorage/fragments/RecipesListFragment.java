package com.example.recipestorage.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import org.parceler.Parcels;

import java.util.List;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

public class RecipesListFragment extends Fragment {
    public static final String TAG = "RecipeListFragment";

    private List<Recipe> allRecipes;
    //private int[] images = {R.drawable.taco,
            //R.drawable.salmon, R.drawable.lobster, R.drawable.fried_rice, R.drawable.burger, R.drawable.salad, R.drawable.pie};
    CarouselView carouselView;

    public RecipesListFragment() {
        // Required empty public constructor
    }

    public RecipesListFragment(List<Recipe> recipes) {
        // Required empty public constructor
        this.allRecipes = recipes;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (allRecipes != null) {
            handleCarouselView(view);
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
                TextView tvTitle = view.findViewById(R.id.tvTitle);
                ImageView ivPicture = view.findViewById(R.id.ivPicture);
                Button btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

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

                btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), EditRecipeActivity.class);
                        intent.putExtra("recipe", Parcels.wrap(recipe));
                        startActivity(intent);

                        //TODO: do an onActivityResult listener so that once activity returns, this picture is updated
                    }
                });
            }
        });
        // After you finish setting up, show the CarouselView
        carouselView.show();
    }


}