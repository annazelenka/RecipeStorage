package com.example.recipestorage.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;

import org.parceler.Parcels;

import java.util.ArrayList;

public class RecipeSectionFragment extends Fragment {

    TextView tvTitle;

    RecipeSection recipeSection;
    String recipeSectionString;
    Recipe recipe;
    ArrayList<String> recipeSectionContents;
    boolean isBlankRecipe;
    ListView lvItems;

    public enum RecipeSection {
        INGREDIENT,
        DIRECTION,
        NOTE
    }

    public RecipeSectionFragment() {
        // Required empty public constructor
        recipeSectionString = "empty";
    }

    public RecipeSectionFragment(boolean setIsBlankRecipe, RecipeSection setRecipeSection, ArrayList<String> setRecipeSectionContents) {
        this.isBlankRecipe = setIsBlankRecipe;
        this.recipeSection = setRecipeSection;
        this.recipeSectionContents = setRecipeSectionContents;

        switch(recipeSection) {
            case INGREDIENT:
                recipeSectionString = "ingredients";
                break;
            case DIRECTION:
                recipeSectionString = "directions";
                break;
            case NOTE:
                recipeSectionString = "notes";
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

        lvItems = view.findViewById(R.id.lvItems);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(recipeSectionString);

        if (!isBlankRecipe) {
            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, recipeSectionContents);

            ListView listView = (ListView) view.findViewById(R.id.lvItems);
            listView.setAdapter(itemsAdapter);
        }

    }
}