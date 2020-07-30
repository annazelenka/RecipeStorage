package com.example.recipestorage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipestorage.EditRecipeActivity;
import com.example.recipestorage.HomeActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.utils.CustomFilter;
import com.example.recipestorage.utils.RecipeTrie;

import org.parceler.Parcels;

import java.util.List;
import java.util.Map;

import coil.Coil;
import coil.ImageLoader;
import coil.request.LoadRequest;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public static final int DELETE_REQUEST_CODE = 10;

    Context context;
    List<Recipe> allRecipes;
    Activity activity;
    List<Recipe> recipesFiltered;
    CustomFilter filter;

    public RecipeAdapter(Activity setActivity, Context context, List<Recipe> setRecipes) {
        this.activity = setActivity;
        this.context = context;
        this.allRecipes = setRecipes;

        this.recipesFiltered = setRecipes;
    }

    public void addAll(List<Recipe> list) {
        allRecipes.addAll(list);
        recipesFiltered.addAll(list);
        notifyDataSetChanged();
    }

    public void setFilter(RecipeTrie recipeTrie) {
        filter = new CustomFilter(allRecipes, this, recipeTrie);
    }

    public void reloadRecipes() {
        setList(allRecipes);
    }

    // set adapter filtered list
    public void setList(List<Recipe> list) {
        this.recipesFiltered = list;
        notifyDataSetChanged();
    }
    //call when you want to filter
    public void filterList(String text) {
        filter.filter(text);
    }

    public void updateList(List<Recipe> setRecipes) {
        this.recipesFiltered = setRecipes;
    }


    //Usually involves inflating a layout from XML (item_movie.xml) and return it inside a viewholder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("MovieAdapter", "onCreateViewHolder");
        View movieView = LayoutInflater.from(context).inflate(R.layout.item_recipe_preview, parent, false);
        return new ViewHolder(movieView);
    }

    //Involves populating data into the item through holder (take data at position and put into
    // View contained within ViewHolder)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("MovieAdapter", "onBindViewHolder " + position);
        //Get the movie at the passed in position
        Recipe data = recipesFiltered.get(position);
        //Bind the movie data into the View Holder
        holder.bind(data);
    }

    //Returns teh total count of items in teh list
    @Override
    public int getItemCount() {
        return recipesFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle;
        ImageView ivPicture;
        ImageButton btnEditRecipe;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivPicture = itemView.findViewById(R.id.ivPicture);
            btnEditRecipe = itemView.findViewById(R.id.btnEditRecipe);

            // add this as the itemView's OnClickListener
            itemView.setOnClickListener(this);
        }

        public void bind(final Recipe recipe) {
            tvTitle.setText(recipe.getTitle());

            ivPicture.setTransitionName("recipeImage");
            tvTitle.setTransitionName("recipeTitle");

            ImageLoader imageLoader = Coil.imageLoader(context);

            boolean hasImage = (recipe.getImage() != null);
            if (hasImage) {
                LoadRequest request = LoadRequest.builder(context)
                        .data(recipe.getImage().getUrl())
                        .crossfade(true)
                        .target(ivPicture)
                        .build();
                imageLoader.execute(request);
            } else {
                ivPicture.setVisibility(View.VISIBLE);
            }

            tvTitle.setText(recipe.getTitle());

            btnEditRecipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchRecipeEditActivity(recipe, ivPicture, tvTitle);
                }
            });
        }

        @Override
        public void onClick(View view) {
            // get item position
            int position = getAdapterPosition();
            //make sure position is valid, ie actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                Recipe data = allRecipes.get(position);
            }
        }

        private void launchRecipeEditActivity(Recipe recipe, ImageView ivPicture, TextView tvTitle) {
            Intent intent = new Intent(context, EditRecipeActivity.class);
            intent.putExtra("recipe", Parcels.wrap(recipe));
            Pair<View, String> p1 = Pair.create((View)ivPicture, "recipeImage");
            Pair<View, String> p2 = Pair.create((View)tvTitle, "tvTitle");
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, p1, p2);
            ((HomeActivity) context).startActivityForResult(intent, DELETE_REQUEST_CODE, options.toBundle());
        }
    }
}
