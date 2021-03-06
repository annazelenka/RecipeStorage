package com.example.recipestorage.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.example.recipestorage.AddRecipeActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.adapters.RecipeAdapter;
import com.example.recipestorage.utils.KeyboardUtils;
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

public class AllRecipesFragment extends Fragment {

    public static final String TAG = "AllRecipesActivity";
    public static final int NUM_COLUMNS = 2;
    private static final int ADD_REQUEST_CODE = 24;

    RecyclerView rvRecipes;
    SkeletonScreen skeletonScreen;
    SearchView searchView;
    FloatingActionButton fabAddRecipe;

    List<Recipe> allRecipes;
    RecipeAdapter adapter;
    RecipeTrie recipeTrie;
    ParseUser currentUser;
    Boolean isFacebookUser;

    public AllRecipesFragment(List<Recipe> setAllRecipes, RecipeTrie setTrie, boolean setIsFacebookUser) {
        this.allRecipes = setAllRecipes;
        this.recipeTrie = setTrie;
        this.isFacebookUser = setIsFacebookUser;
        this.currentUser = ParseUser.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.recipe_search);

        rvRecipes = view.findViewById(R.id.rvRecipes);
        fabAddRecipe = view.findViewById(R.id.fabAddRecipe);

        adapter = new RecipeAdapter(getActivity(), getContext(), allRecipes);

        // Recycler view setup: layout manager and the adapter
        rvRecipes.setLayoutManager(new GridLayoutManager(getContext(), NUM_COLUMNS));
        rvRecipes.setAdapter(adapter);
        adapter.setFilter(recipeTrie);

         // set up placeholders when loading
        skeletonScreen = Skeleton.bind(rvRecipes)
                .adapter(adapter)
                .load(R.layout.item_recipe_skeleton)
                .show();

        rvRecipes.postDelayed(new Runnable() {
            @Override
            public void run() {
                skeletonScreen.hide();
            }
        }, 500);

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

        KeyboardUtils.addKeyboardToggleListener(getActivity(), new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if (isVisible) {
                    fabAddRecipe.setVisibility(View.GONE);
                } else {
                    fabAddRecipe.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void launchAddRecipe() {
        Intent intent = new Intent(getContext(), AddRecipeActivity.class);
        getActivity().startActivityForResult(intent, ADD_REQUEST_CODE);
    }

    public void notifyDatasetChanged() {
        adapter.notifyDataSetChanged();
    }

    public void notifyItemInserted(int position) {
        adapter.notifyItemInserted(position);
        rvRecipes.smoothScrollToPosition(position);
    }

    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
        rvRecipes.smoothScrollToPosition(position);
    }

    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
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
}