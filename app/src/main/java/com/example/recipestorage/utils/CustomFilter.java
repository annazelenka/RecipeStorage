package com.example.recipestorage.utils;

import android.widget.Filter;

import com.example.recipestorage.Recipe;
import com.example.recipestorage.adapters.RecipeAdapter;
import com.example.recipestorage.adapters.RecipeSectionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomFilter extends Filter {

    private List<Recipe> recipeList;
    private List<Recipe> filteredRecipeList;
    private RecipeAdapter adapter;
    private Map<String, Recipe> recipeNameMap;

    public CustomFilter(List<Recipe> setRecipeList, RecipeAdapter setAdapter, Map<String, Recipe> setRecipeNameMap) {
        this.adapter = setAdapter;
        this.recipeList = setRecipeList;
        this.filteredRecipeList = new ArrayList();
        this.recipeNameMap = setRecipeNameMap;
    }

    @Override
    protected FilterResults performFiltering(CharSequence searchText) {
        filteredRecipeList.clear();
        final FilterResults results = new FilterResults();

        //here you need to add proper items to filteredContactList
//        for (final Recipe item : recipeList) {
//            if (item.getName().toLowerCase().trim().contains("pattern")) {
//                filteredRecipeList.add(item);
//            }
//        }
        // TODO: replace with trie
        Recipe recipe = recipeNameMap.get(searchText);
        if (recipe != null) {
            filteredRecipeList.add(recipe);
        }


        results.values = filteredRecipeList;
        results.count = filteredRecipeList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.updateList(filteredRecipeList);
        //adapter.notifyDataSetChanged();
    }
}