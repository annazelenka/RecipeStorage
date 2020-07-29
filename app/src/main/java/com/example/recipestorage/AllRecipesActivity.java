package com.example.recipestorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.widget.SearchView;

import com.example.recipestorage.adapters.RecipeAdapter;
import com.example.recipestorage.utils.RecipeTrie;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class AllRecipesActivity extends AppCompatActivity {
    public static final String TAG = "AllRecipesActivity";
    public static final int NUM_COLUMNS = 2;
    private static final int REQUEST_CODE = 24;

    RecyclerView rvRecipes;
    List<Recipe> allRecipes;
    RecipeAdapter adapter;
    Map<String, Recipe> recipeNameMap;
    RecipeTrie recipeTrie;
    SearchView searchView;
    FloatingActionButton fabAddRecipe;

    ParseUser currentUser;
    Boolean isFacebookUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);

        searchView = findViewById(R.id.recipe_search);

        // Find the recycler view
        rvRecipes = findViewById(R.id.rvRecipes);
        fabAddRecipe = findViewById(R.id.fabAddRecipe);

        currentUser = ParseUser.getCurrentUser();
        allRecipes = Parcels.unwrap(getIntent().getParcelableExtra("allRecipes"));
        recipeNameMap = Parcels.unwrap(getIntent().getParcelableExtra("recipeNameMap"));
        isFacebookUser = getIntent().getBooleanExtra("isFacebookUser", false);
        //recipeTrie = Parcels.unwrap(getIntent().getParcelableExtra("recipeTrie"));
        recipeTrie = new RecipeTrie();
        recipeTrie.populateRecipeTrie(allRecipes);


        // Initialize the list of tweets and adapter
        //allRecipes = new ArrayList<Recipe>();
        adapter = new RecipeAdapter(AllRecipesActivity.this, this, allRecipes);

        // Recycler view setup: layout manager and the adapter
        rvRecipes.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        rvRecipes.setAdapter(adapter);
        adapter.setFilter(recipeTrie);

        setupSearchView();

        if (AccessToken.getCurrentAccessToken() != null) {
            loadGraphData();
        }

        fabAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAddRecipe();
            }
        });
    }

    private void launchAddRecipe() {
        Intent intent = new Intent(AllRecipesActivity.this, AddRecipeActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void loadGraphData() {
        final JSONObject data = new JSONObject();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.e(TAG,object.toString());
                        Log.e(TAG,response.toString());
                        String firstName, lastName, email, birthday, gender;
                        try {
                            String userId = object.getString("id");
                            URL profilePicture = new URL("https://graph.facebook.com/" + userId + "/picture?width=500&height=500");
                            if(object.has("first_name"))
                                firstName = object.getString("first_name");
                            if(object.has("last_name"))
                                lastName = object.getString("last_name");
                            if (object.has("email"))
                                email = object.getString("email");
                            if (object.has("birthday"))
                                birthday = object.getString("birthday");
                            if (object.has("gender"))
                                gender = object.getString("gender");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");   //and location parameter
        request.setParameters(parameters);
        request.executeAsync();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data (Tweet) from the intent (need to use Parcelable b/c Tweet is custom object)
            Recipe recipe = Parcels.unwrap(data.getParcelableExtra("newRecipe"));
            // Update the Recycler View with this new tweet

            // Modify data source of tweets
            allRecipes.add(0, recipe);
            // Update adapter
            adapter.notifyItemInserted(0);
            rvRecipes.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}