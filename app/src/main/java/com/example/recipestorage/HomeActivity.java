package com.example.recipestorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.recipestorage.fragments.AllRecipesFragment;
import com.example.recipestorage.fragments.HomeFragment;
import com.example.recipestorage.fragments.MoreFragment;
import com.example.recipestorage.utils.RecipeTrie;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import org.parceler.Parcels;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements Filterable {
    public static final int HOME_POSITION = 0;
    public static final int ALL_RECIPES_POSITION = 1;
    public static final int MORE_POSITION = 2;
    private static final int RECIPE_LIMIT = 20;
    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE = 24;
    public static final int DELETE_REQUEST_CODE = 10;


    final FragmentManager fragmentManager = getSupportFragmentManager();

    BubbleNavigationConstraintView bubbleNavigation;

    RecipeTrie recipeTrie;
    List<Recipe> allRecipes;
    ParseUser currentUser;
    boolean isFacebookUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bubbleNavigation = findViewById(R.id.bubbleNavigation);
        currentUser = ParseUser.getCurrentUser();

        populateRecipes();
        setupBubbleNavigation();
    }

    public void setupBubbleNavigation() {
        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                Fragment fragment = null;
                switch (position) {
                    case HOME_POSITION:
                        fragment = new HomeFragment(allRecipes, recipeTrie);
                        break;
                    case ALL_RECIPES_POSITION: //miDirections
                        launchAllRecipesScreen();
                        break;
                    case MORE_POSITION: //miNotes
                        fragment = new MoreFragment(currentUser);
                        break;
                    default:
                        break;
                }
                if (fragment != null) {
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                }
            }
        });
        bubbleNavigation.setCurrentActiveItem(HOME_POSITION);
    }

    private void launchAllRecipesScreen() {
        Fragment fragment = new AllRecipesFragment(allRecipes, recipeTrie, isFacebookUser);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    protected void populateRecipes() {

        // query recipes
        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        query.include(Recipe.KEY_USER);
        query.setLimit(RECIPE_LIMIT);
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
                allRecipes = recipes;
                recipeTrie = new RecipeTrie();
                recipeTrie.populateRecipeTrie(allRecipes);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data (Tweet) from the intent (need to use Parcelable b/c Tweet is custom object)
            Recipe recipe = Parcels.unwrap(data.getParcelableExtra("newRecipe"));
            // Update the Recycler View with this new recipe
            allRecipes.add(0, recipe);

            recipeTrie.insert(recipe.getTitle(), recipe);
        } else if (requestCode == DELETE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data (Tweet) from the intent (need to use Parcelable b/c Tweet is custom object)
            Recipe recipe = Parcels.unwrap(data.getParcelableExtra("recipeToDelete"));

            recipeTrie.delete(recipe);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}