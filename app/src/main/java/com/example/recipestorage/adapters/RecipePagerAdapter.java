package com.example.recipestorage.adapters;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.recipestorage.PageFragment;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.fragments.RecipeSectionFragment;
import com.example.recipestorage.fragments.RecipeSummaryFragment;

import org.parceler.Parcels;

import java.util.ArrayList;

public class RecipePagerAdapter extends androidx.fragment.app.FragmentPagerAdapter {
    final int PAGE_COUNT = 4;
    public static final int HOME_POSITION = 0;
    public static final int INGREDIENTS_POSITION = 1;
    public static final int DIRECTIONS_POSITION = 2;
    public static final int NOTES_POSITION = 3;

    private Context context;
    Toolbar toolbar;

    Recipe recipe;
    int adapterPosition;
    String returnFragment;
    ArrayList<String> ingredients;
    ArrayList<String> directions;
    ArrayList<String> notes;
    FragmentManager fragmentManager;


    public RecipePagerAdapter(FragmentManager fm, Context context, Toolbar setToolbar, Recipe setRecipe, int setAdapterPosition, String setReturnFragment) {
        super(fm);
        this.fragmentManager = fm;
        this.context = context;
        this.toolbar = setToolbar;
        this.recipe = setRecipe;
        this.adapterPosition = setAdapterPosition;
        this.returnFragment = setReturnFragment;

        ingredients = recipe.getParsedIngredients();
        directions = recipe.getParsedDirections();
        notes = recipe.getParsedNotes();
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case HOME_POSITION:
                toolbar.setVisibility(View.GONE);
                fragment = new RecipeSummaryFragment(recipe, adapterPosition, returnFragment);
                break;
            case INGREDIENTS_POSITION: //miDirections
                toolbar.setVisibility(View.VISIBLE);
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.INGREDIENT, ingredients);
                break;
            case DIRECTIONS_POSITION: //miNotes
                toolbar.setVisibility(View.VISIBLE);
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.DIRECTION, directions);
                break;
            case NOTES_POSITION:
            default:
                toolbar.setVisibility(View.VISIBLE);
                fragment = new RecipeSectionFragment(false, RecipeSectionFragment.RecipeSection.NOTE, notes);
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
