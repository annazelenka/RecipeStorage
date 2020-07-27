package com.example.recipestorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.widget.SearchView;

import com.example.recipestorage.adapters.RecipeAdapter;
import com.example.recipestorage.utils.Trie;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllRecipesActivity extends AppCompatActivity {
    public static final String TAG = "AllRecipesActivity";
    public static final int NUM_COLUMNS = 2;

    RecyclerView rvRecipes;
    List<Recipe> allRecipes;
    RecipeAdapter adapter;
    Map<String, Recipe> recipeNameMap;
    Trie recipeTrie;
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
        allRecipes = Parcels.unwrap(getIntent().getParcelableExtra("allRecipes"));
        recipeNameMap = Parcels.unwrap(getIntent().getParcelableExtra("recipeNameMap"));
        //recipeTrie = Parcels.unwrap(getIntent().getParcelableExtra("recipeTrie"));
        recipeTrie = new Trie();
        recipeTrie.populateRecipeTrie(allRecipes);


        // Initialize the list of tweets and adapter
        //allRecipes = new ArrayList<Recipe>();
        adapter = new RecipeAdapter(AllRecipesActivity.this, this, allRecipes);

        // Recycler view setup: layout manager and the adapter
        rvRecipes.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        rvRecipes.setAdapter(adapter);
        adapter.setFilter(recipeTrie);

        setupSearchView();
    }

    private void setupSearchView() {
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchView.getQuery().toString();
                adapter.filterList(query);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("onQueryTextChange", "called");
//                if (newText.isEmpty()) {
//                    adapter.filterList(newText);
//                    return true;
//                }
                adapter.filterList(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                //adapter.filterList(query);
                return false;
            }

        });
    }

    public Map<String, Recipe> getRecipeNameMap() {
        return recipeNameMap;
    }

}