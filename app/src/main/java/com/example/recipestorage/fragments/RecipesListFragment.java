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

import com.example.recipestorage.R;
import com.example.recipestorage.RecipeActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class RecipesListFragment extends Fragment {

    Button btnEditRecipe;

    public RecipesListFragment() {
        // Required empty public constructor
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


        btnEditRecipe = view.findViewById(R.id.btnEditRecipe);

        btnEditRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RecipeActivity.class);
                intent.putExtra("title", recipeTitle);
                intent.putExtra("ingredients", ingredients);
                intent.putExtra("directions", directions);
                startActivity(intent);

                //TODO: do an onActivityResult listener so that once activity returns, this picture is updated
            }
        });
    }
}