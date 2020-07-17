package com.example.recipestorage.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;

import org.parceler.Parcels;

import java.util.ArrayList;

public class RecipeSectionFragment extends Fragment {

    TextView tvTitle;

    RecipeSection recipeSection;
    String recipeSectionString;
    Recipe recipe;
    ArrayList<String> recipeSectionContents;
    boolean isBlankRecipe;
    ListView lvItems;

    EditText etAddRecipeSection;

    ArrayAdapter<String> itemsAdapter;
    OnDataPass dataPasser;


    public enum RecipeSection {
        INGREDIENT,
        DIRECTION,
        NOTE
    }

    public interface OnDataPass {
        public void onDataPass(RecipeSection recipeSection, ArrayList<String> data);
        // this method used only for RecipeEditActivity to notify RecipeActivity
        public void onDataChangedPass(boolean dataChanged);
    }

    public RecipeSectionFragment() {
        // Required empty public constructor
        recipeSectionString = "empty";
    }

    public RecipeSectionFragment(boolean setIsBlankRecipe, RecipeSection setRecipeSection, ArrayList<String> setRecipeSectionContents) {
        this.isBlankRecipe = setIsBlankRecipe;
        this.recipeSection = setRecipeSection;
        this.recipeSectionContents = setRecipeSectionContents;

        switch(recipeSection) {
            case INGREDIENT:
                recipeSectionString = "ingredient";
                break;
            case DIRECTION:
                recipeSectionString = "direction";
                break;
            case NOTE:
                recipeSectionString = "note";
                break;
            default:
                recipeSectionString = "empty";
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void passData(RecipeSection recipeSection, ArrayList<String> data) {
        dataPasser.onDataPass(recipeSection, data);
    }

    public void passDataChanged(boolean dataHasChanged) {
        dataPasser.onDataChangedPass(dataHasChanged);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvItems = view.findViewById(R.id.lvItems);
        tvTitle = view.findViewById(R.id.tvTitle);
        etAddRecipeSection = view.findViewById(R.id.etAddRecipeSection);

        populateListView(view);
        tvTitle.setText(recipeSectionString + "s");

        if (!isBlankRecipe) {
            passData(recipeSection, recipeSectionContents);
        }

        etAddRecipeSection.setHint("new " + recipeSectionString);
        etAddRecipeSection.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    String text = etAddRecipeSection.getText().toString();
                    if (text.isEmpty()) {
                        Toast.makeText(getContext(), "New " + recipeSectionString + " cannot be empty!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    recipeSectionContents.add(text);
                    itemsAdapter.notifyDataSetChanged();
                    passData(recipeSection, recipeSectionContents);
                    passDataChanged(true);
                    etAddRecipeSection.setText("");

                    return true;
                }
                return false;
            }
        });
    }

    private void populateListView(View view) {
        itemsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, recipeSectionContents);
        ListView listView = (ListView) view.findViewById(R.id.lvItems);
        listView.setAdapter(itemsAdapter);
    }
}