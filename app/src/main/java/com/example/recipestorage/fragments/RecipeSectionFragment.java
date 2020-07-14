package com.example.recipestorage.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.recipestorage.R;
public class RecipeSectionFragment extends Fragment {

    TextView tvTitle;
    RecipeSection recipeSection;
    String recipeSectionString;

    public enum RecipeSection {
        INGREDIENT,
        DIRECTION,
        NOTE
    }

    public RecipeSectionFragment() {
        // Required empty public constructor
        recipeSectionString = "empty";
    }

    public RecipeSectionFragment(RecipeSection setRecipeSection) {
        this.recipeSection = setRecipeSection;

        switch(recipeSection) {
            case INGREDIENT:
                recipeSectionString = "ingredient";
                break;
            case DIRECTION:
                recipeSectionString = "direction";
                break;
            case NOTE:
                recipeSectionString = "note";
                break;
            default:
                recipeSectionString = "empty";
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(recipeSectionString);
    }
}