package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.widget.SearchView;

import com.example.recipestorage.adapters.RecipeAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllRecipesActivity extends AppCompatActivity {
    public static final String TAG = "AllRecipesActivity";
    public static final int NUM_COLUMNS = 3;

    RecyclerView rvRecipes;
    List<Recipe> allRecipes;
    RecipeAdapter adapter;
    Map<String, Recipe> recipeNameMap;
    SearchView searchView;

    ParseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);

        searchView = findViewById(R.id.recipe_search);

        // Find the recycler view
        rvRecipes = findViewById(R.id.rvRecipes);

        currentUser = ParseUser.getCurrentUser();
        populateRecipes();

        // Initialize the list of tweets and adapter
        allRecipes = new ArrayList<Recipe>();
        adapter = new RecipeAdapter(AllRecipesActivity.this, this, allRecipes);

        // Recycler view setup: layout manager and the adapter
        rvRecipes.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        rvRecipes.setAdapter(adapter);

    }

    protected void populateRecipes() {
        // query recipes
        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        query.include(Recipe.KEY_USER);
        query.whereEqualTo(Recipe.KEY_USER, currentUser);
        query.addDescendingOrder(Recipe.KEY_CREATED_KEY);
        query.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> recipes, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting recipes", e);
                    return;
                }

                Log.i("HomeActivity", "success getting recipes");
                //allRecipes = recipes;
                adapter.addAll(recipes);
                adapter.notifyDataSetChanged();
                populateRecipeNameMap(allRecipes);
                adapter.setFilter(recipeNameMap);

                searchView.setOnSearchClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String query = searchView.getQuery().toString();
                        if (query.isEmpty()) {
                            adapter.reloadRecipes();
                            return;
                        }
                        adapter.filterList(query);
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        //Log.e("onQueryTextChange", "called");
                        if (newText.isEmpty()) {
                            adapter.filterList(newText);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        adapter.filterList(query);
                        return true;
                    }

                });
            }

        });

    }

    public Map<String, Recipe> getRecipeNameMap() {
        return recipeNameMap;
    }

    private void populateRecipeNameMap(List<Recipe> recipes) {
        recipeNameMap = new HashMap<String, Recipe>();
        for (Recipe recipe : recipes) {
            recipeNameMap.put(recipe.getTitle().toLowerCase(), recipe);
        }
    }
}