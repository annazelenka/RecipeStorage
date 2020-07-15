package com.example.recipestorage.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.recipestorage.RecipeActivity;
import com.jama.carouselview.CarouselView;
import com.jama.carouselview.CarouselViewListener;
import com.jama.carouselview.enums.IndicatorAnimationType;
import com.jama.carouselview.enums.OffsetType;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipesListFragment extends Fragment {
    public static final String TAG = "RecipeListFragment";

    private List<Recipe> allRecipes;
    private int[] images = {R.drawable.pie,
            R.drawable.lobster, R.drawable.salad, R.drawable.fried_rice};
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

        //TODO: REPLACE WITH REAL DATA
        final String recipeTitle = "Burger";
        final ArrayList<String> ingredients = new ArrayList<String> (Arrays.asList("1 lb ground beef", "salt and pepper", "cheese"));
        final ArrayList<String> directions = new ArrayList<String> (Arrays.asList("1. Mix ground beef, salt, and pepper", "2. Grill", "3. Add cheese"));

        carouselView = view.findViewById(R.id.carouselView);

        carouselView.setSize(images.length);
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

                ivPicture.setImageDrawable(getResources().getDrawable(images[position]));
                if (allRecipes.size() == 0) {
                    return;
                }

                Recipe recipe = allRecipes.get(position);

                // Example here is setting up a full image carousel

                tvTitle.setText(recipe.getTitle());


                btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), RecipeActivity.class);
                        //intent.putExtra("recipe", Parcels.wrap(recipe));
                        startActivity(intent);

                        //TODO: do an onActivityResult listener so that once activity returns, this picture is updated
                    }
                });
            }
        });
        // After you finish setting up, show the CarouselView
        carouselView.show();

    }

    private void handleCarouselView(View view) {
        carouselView = view.findViewById(R.id.carouselView);

        carouselView.setSize(images.length);
        carouselView.setResource(R.layout.item_recipe_preview);
        carouselView.setAutoPlay(false);
        carouselView.setScaleOnScroll(true);
        carouselView.setIndicatorAnimationType(IndicatorAnimationType.SLIDE);
        carouselView.setCarouselOffset(OffsetType.CENTER);
        carouselView.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int position) {
                TextView tvTitle;
                ImageView ivPicture;
                Button btnEditRecipe;

                tvTitle = view.findViewById(R.id.tvTitle);
                ivPicture = view.findViewById(R.id.ivPicture);
                btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

                if (allRecipes.size() != 0) {
                    return;
                }

                final Recipe recipe = allRecipes.get(position);

                // Example here is setting up a full image carousel

                tvTitle.setText(recipe.getTitle());

                ivPicture.setImageDrawable(getResources().getDrawable(images[position]));
                btnEditRecipe.setText(recipe.getCookTimeMin());

                btnEditRecipe = view.findViewById(R.id.btnEditRecipe);
                btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), RecipeActivity.class);
                        //intent.putExtra("recipe", Parcels.wrap(recipe));
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