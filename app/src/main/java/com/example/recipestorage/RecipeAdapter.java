package com.example.recipestorage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    Context context;
    List<String> items;

    public RecipeAdapter(Context context, List<String> setItems) {
        this.context = context;
        this.items = setItems;
    }


    //Usually involves inflating a layout from XML (item_movie.xml) and return it inside a viewholder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("MovieAdapter", "onCreateViewHolder");
        View movieView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(movieView);
    }

    //Involves populating data into the item through holder (take data at position and put into
    // View contained within ViewHolder)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("MovieAdapter", "onBindViewHolder " + position);
        //Get the movie at the passed in position
        String data = items.get(position);
        //Bind the movie data into the View Holder
        holder.bind(data);
    }

    //Returns teh total count of items in teh list
    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tvData);

            // add this as the itemView's OnClickListener
            itemView.setOnClickListener(this);
        }

        public void bind(String data) {
            tvData.setText(data);

        }

        @Override
        public void onClick(View view) {
            // get item position
            int position = getAdapterPosition();
            //make sure position is valid, ie actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                String data = items.get(position);

            }

        }
    }
}
