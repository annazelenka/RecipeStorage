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
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements Filterable {
    public static final int HOME_POSITION = 0;
    public static final int ALL_RECIPES_POSITION = 1;
    public static final int MORE_POSITION = 2;
    private static final int RECIPE_LIMIT = 20;
    private static final String TAG = "HomeActivity";
    private static final int ADD_RECIPE_REQUEST_CODE = 24;
    public static final int EDIT_REQUEST_CODE = 5;


    final FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment currentFragment;
    HomeFragment homeFragment;

    BubbleNavigationConstraintView bubbleNavigation;

    RecipeTrie recipeTrie;
    List<Recipe> allRecipes;
    List<Recipe> favoritedRecipes;
    List<Recipe> breakfastRecipes;
    List<Recipe> lunchRecipes;
    List<Recipe> dinnerRecipes;
    List<Recipe> dessertRecipes;
    ParseUser currentUser;
    boolean isFacebookUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bubbleNavigation = findViewById(R.id.bubbleNavigation);
        currentUser = ParseUser.getCurrentUser();

        currentFragment = null;
        setupBubbleNavigation();
        populateRecipes();
    }

    public void setupBubbleNavigation() {
        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                currentFragment = null;
                switch (position) {
                    case HOME_POSITION:
                    default:
                        currentFragment = homeFragment;
                        ((HomeFragment) currentFragment).filter();
                        break;
                    case ALL_RECIPES_POSITION: //miDirections
                        currentFragment = new AllRecipesFragment(allRecipes, recipeTrie, isFacebookUser);
                        break;
                    case MORE_POSITION: //miNotes
                        currentFragment = new MoreFragment(currentUser);
                        break;
                }
                fragmentManager
                        .beginTransaction().replace(R.id.flContainer, currentFragment).commit();
            }
        });
        bubbleNavigation.setCurrentActiveItem(HOME_POSITION);
        currentFragment = new HomeFragment(allRecipes, recipeTrie, true);
        homeFragment = (HomeFragment) currentFragment;
        fragmentManager.beginTransaction().replace(R.id.flContainer, currentFragment).commit();
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
                boolean shouldUseSkeletonView = allRecipes == null;
                allRecipes = recipes;

                homeFragment.setOriginalAllRecipes(allRecipes);
                favoritedRecipes = getFavoritedRecipes();
                breakfastRecipes = getBreakfastRecipes();
                lunchRecipes = getLunchRecipes();
                dinnerRecipes = getDinnerRecipes();
                dessertRecipes = getDessertRecipes();
                homeFragment.setFilteredRecipes(favoritedRecipes, breakfastRecipes, lunchRecipes, dinnerRecipes, dessertRecipes);
                homeFragment.filter();
                recipeTrie = new RecipeTrie();

                recipeTrie.populateRecipeTrie(allRecipes);
            }
        });
    }

    protected List<Recipe> getFavoritedRecipes() {
        List<Recipe> favorites = new ArrayList<Recipe>();

        for (Recipe recipe: allRecipes) {
            if (recipe.isFavorite()) {
                favorites.add(recipe);
            }
        }
        return favorites;
    }

    protected List<Recipe> getBreakfastRecipes() {
        List<Recipe> returnRecipes = new ArrayList<Recipe>();

        for (Recipe recipe: allRecipes) {
            if (recipe.isBreakfast()) {
                returnRecipes.add(recipe);
            }
        }
        return returnRecipes;
    }

    protected List<Recipe> getLunchRecipes() {
        List<Recipe> returnRecipes = new ArrayList<Recipe>();

        for (Recipe recipe: allRecipes) {
            if (recipe.isLunch()) {
                returnRecipes.add(recipe);
            }
        }
        return returnRecipes;
    }

    protected List<Recipe> getDinnerRecipes() {
        List<Recipe> returnRecipes = new ArrayList<Recipe>();

        for (Recipe recipe: allRecipes) {
            if (recipe.isDinner()) {
                returnRecipes.add(recipe);
            }
        }
        return returnRecipes;
    }

    protected List<Recipe> getDessertRecipes() {
        List<Recipe> returnRecipes = new ArrayList<Recipe>();

        for (Recipe recipe: allRecipes) {
            if (recipe.isDessert()) {
                returnRecipes.add(recipe);
            }
        }
        return returnRecipes;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null && resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == ADD_RECIPE_REQUEST_CODE && resultCode == RESULT_OK) {
            Recipe recipe = Parcels.unwrap(data.getParcelableExtra("newRecipe"));
            // Update the Recycler View with this new recipe
            allRecipes.add(0, recipe);
            recipeTrie.insert(recipe.getTitle(), recipe);
            AllRecipesFragment fragment = (AllRecipesFragment) currentFragment;
            fragment.notifyItemInserted(0);
            fragmentManager.beginTransaction().replace(R.id.flContainer, currentFragment).commit();
        } else if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", 0);
            String returnFragment = data.getStringExtra("returnFragment");
            if (data.hasExtra("recipeToEdit")) {
                boolean tagsChanged = data.getBooleanExtra("tagsChanged", true);
                Recipe recipeToEdit = (Recipe) Parcels.unwrap(data.getParcelableExtra("recipeToEdit"));
                if (data.hasExtra("originalTitle")) {
                    String originalTitle = data.getStringExtra("originalTitle");
                    recipeTrie.delete(originalTitle, recipeToEdit.getObjectId());
                    recipeTrie.insert(recipeToEdit.getTitle(), recipeToEdit);
                }
                allRecipes.set(position, recipeToEdit);
                launchReturnFragment(returnFragment, false, position, tagsChanged);
            } else { // recipe was deleted
                String title = data.getStringExtra("titleToDelete");
                String objectId = data.getStringExtra("objectIdToDelete");
                recipeTrie.delete(title, objectId);
                allRecipes.remove(position);
                launchReturnFragment(returnFragment, true, position, false);
            }
        }
    }

    private void launchReturnFragment(String returnFragment, boolean itemRemoved, int position, boolean tagsChanged) {
        if (returnFragment == null) {
            Log.i(TAG, "returnFragment is null");
            return;
        }

        if (returnFragment.equals("AllRecipesFragment")) {
            if (tagsChanged) populateRecipes();
            AllRecipesFragment allRecipesFragment =  (AllRecipesFragment) currentFragment;
            if (itemRemoved) allRecipesFragment.notifyItemRemoved(position);
            else allRecipesFragment.notifyItemChanged(position);

        } else if (returnFragment.equals("HomeFragment")) {
            populateRecipes();
            homeFragment = (HomeFragment) currentFragment;
            fragmentManager.beginTransaction().replace(R.id.flContainer, currentFragment).commit();

        }
    }

}