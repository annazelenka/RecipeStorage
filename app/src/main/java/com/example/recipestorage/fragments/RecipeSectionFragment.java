package com.example.recipestorage.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipestorage.AddRecipeActivity;
import com.example.recipestorage.HomeActivity;
import com.example.recipestorage.R;
import com.example.recipestorage.Recipe;
import com.example.recipestorage.adapters.RecipeAdapter;
import com.example.recipestorage.adapters.RecipeSectionAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;

public class RecipeSectionFragment extends Fragment implements RecipeSectionAdapter.AdapterInterface {
    TextView tvTitle;
    RecyclerView rvItems;
    EditText etAddRecipeSection;
    FloatingActionButton fabSubmit;

    boolean isBlankRecipe;
    RecipeSection recipeSection;
    String recipeSectionString;
    ArrayList<String> recipeSectionContents;
    RecipeSectionAdapter recipeSectionAdapter;
    OnDataPass dataPasser;
    Recipe newRecipe;

    public enum RecipeSection {
        INGREDIENT,
        DIRECTION,
        NOTE
    }

    public interface OnDataPass {
        public void onAddDataPass(RecipeSection recipeSection, ArrayList<String> data);
        public void onIngredientsChangedPass(boolean dataChanged);
        public void onDirectionsChangedPass(boolean dataChanged);
        public void onNotesChangedPass(boolean dataChanged);
        public void onSaveNewRecipePass(Recipe newRecipe);
        public void onSaveExistingRecipePass();
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

    public RecipeSectionFragment(boolean setIsBlankRecipe, RecipeSection setRecipeSection, Recipe setNewRecipe) {
        this.isBlankRecipe = setIsBlankRecipe;
        this.recipeSection = setRecipeSection;
        this.newRecipe = setNewRecipe;

        switch(recipeSection) {
            case INGREDIENT:
                recipeSectionString = "ingredient";
                recipeSectionContents = newRecipe.getParsedIngredients();
                break;
            case DIRECTION:
                recipeSectionString = "direction";
                recipeSectionContents = newRecipe.getParsedDirections();
                break;
            case NOTE:
                recipeSectionString = "note";
                recipeSectionContents = newRecipe.getParsedNotes();
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
        dataPasser.onAddDataPass(recipeSection, data);
    }

    public void passDataChanged(boolean dataHasChanged) {
        switch (recipeSection) {
            case INGREDIENT:
                dataPasser.onIngredientsChangedPass(true);
                break;
            case DIRECTION:
                dataPasser.onDirectionsChangedPass(true);
                break;
            case NOTE:
                dataPasser.onNotesChangedPass(true);
                break;
        }
    }

    // onDataDeleted and onDataEdited called from adapter
    @Override
    public void onDataDeleted(int position) {
        switch (recipeSection) {
            case INGREDIENT:
                dataPasser.onIngredientsChangedPass(true);
                break;
            case DIRECTION:
                dataPasser.onDirectionsChangedPass(true);
                break;
            case NOTE:
                dataPasser.onNotesChangedPass(true);
                break;
        }
    }

    @Override
    public void onDataEdited(int position, String newData){
        switch (recipeSection) {
            case INGREDIENT:
                dataPasser.onIngredientsChangedPass(true);
            case DIRECTION:
                dataPasser.onDirectionsChangedPass(true);
            case NOTE:
                dataPasser.onNotesChangedPass(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvItems = view.findViewById(R.id.rvItems);
        tvTitle = view.findViewById(R.id.tvTitle);
        etAddRecipeSection = view.findViewById(R.id.etAddRecipeSection);
        fabSubmit = view.findViewById(R.id.fabSubmit);

        setUpRecyclerView();
        tvTitle.setText(recipeSectionString + "s");

        if (!isBlankRecipe) {
            passData(recipeSection, recipeSectionContents);
        }
        setupFab();

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
                    rvItems.getAdapter().notifyDataSetChanged();
                    passData(recipeSection, recipeSectionContents);
                    passDataChanged(true);
                    etAddRecipeSection.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    private void setupFab() {
        fabSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipeSectionContents = (ArrayList<String>) recipeSectionAdapter.getItems();
                if (recipeSection != RecipeSection.NOTE && recipeSectionContents.isEmpty()) {
                    Toast.makeText(getContext(), recipeSectionString + "s cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isBlankRecipe) {
                    launchNextFragment();
                } else {
                    dataPasser.onSaveExistingRecipePass();
                }
            }
        });
    }

    private void launchNextFragment() {
        RecipeSectionFragment fragment;
        switch(recipeSection) {
            case INGREDIENT:
                newRecipe.clearIngredients();
                newRecipe.addIngredients(recipeSectionContents);
                fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.DIRECTION, newRecipe);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.flContainer, fragment, "AddRecipe ingredients to directions")
                        .commit();
                return;
            case DIRECTION:
                newRecipe.clearDirections();
                newRecipe.addDirections(recipeSectionContents);
                fragment = new RecipeSectionFragment(true, RecipeSectionFragment.RecipeSection.NOTE, newRecipe);
                recipeSectionString = "direction";
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.flContainer, fragment, "AddRecipe directions to notes")
                        .commit();
                return;
            case NOTE:
                newRecipe.clearNotes();
                newRecipe.addNotes(recipeSectionContents);
                recipeSectionString = "note";
                dataPasser.onSaveNewRecipePass(newRecipe);
                break;
            default:
                recipeSectionString = "empty";
        }

    }

    private void setUpRecyclerView() {
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeSectionAdapter = new RecipeSectionAdapter(getContext(), RecipeSectionFragment.this, recipeSectionContents);
        rvItems.setAdapter(recipeSectionAdapter);
        rvItems.setHasFixedSize(true);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }

    /**
     * Swipe-to-delete modified from:
     * https://medium.com/nemanja-kovacevic/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary-6bf6a6601214
     */
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(getContext(), R.drawable.ic_clear_24px);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getContext().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                RecipeSectionAdapter recipeSectionAdapter = (RecipeSectionAdapter)recyclerView.getAdapter();
                if (recipeSectionAdapter.isUndoOn() && recipeSectionAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                RecipeSectionAdapter adapter = (RecipeSectionAdapter)rvItems.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rvItems);
    }

    /**
     * from https://medium.com/nemanja-kovacevic/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary-6bf6a6601214
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        rvItems.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }
        });
    }

}