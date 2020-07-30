package com.example.recipestorage.utils;

import android.widget.Filter;

import com.example.recipestorage.Recipe;
import com.example.recipestorage.adapters.RecipeAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomFilter extends Filter {

    private List<Recipe> recipeList;
    private List<Recipe> filteredRecipeList;
    private RecipeAdapter adapter;
    private RecipeTrie recipeTrie;

    public CustomFilter(List<Recipe> setRecipeList, RecipeAdapter setAdapter, RecipeTrie setRecipeTrie) {
        this.adapter = setAdapter;
        this.recipeList = setRecipeList;
        this.filteredRecipeList = new ArrayList();
        this.recipeTrie = setRecipeTrie;
    }

    @Override
    protected FilterResults performFiltering(CharSequence searchText) {
        String searchTextString = searchText.toString().toLowerCase();
        filteredRecipeList.clear();
        final FilterResults results = new FilterResults();

        //here you need to add proper items to filteredContactList
//        for (final Recipe item : recipeList) {
//            if (item.getName().toLowerCase().trim().contains("pattern")) {
//                filteredRecipeList.add(item);
//            }
//        }

        if (searchTextString == null || searchTextString.equals("")) {
            filteredRecipeList.addAll(recipeList);
        } else {
            List<Recipe> recipes = recipeTrie.find(searchTextString);
            if (recipes != null) {
                filteredRecipeList.addAll(recipes);
            }
        }


        results.values = filteredRecipeList;
        results.count = filteredRecipeList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.updateList(filteredRecipeList);
        adapter.notifyDataSetChanged();
    }


}