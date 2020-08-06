package com.example.recipestorage.adapters;


import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.recipestorage.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RecyclerView adapter enabling undo on a swiped away item.
 * MODIFIED FROM https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete
 */

public class RecipeSectionAdapter extends RecyclerView.Adapter implements Filterable {

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

    List<String> items;
    List<String> itemsPendingRemoval;
    int lastInsertedIndex; // so we can add some more items for testing purposes
    boolean undoOn; // is undo on, you can turn it on from the toolbar menu

    private Handler handler = new Handler(); // hanlder for running delayed runnables
    HashMap<String, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be
    private AdapterInterface adapterListener;
    private TestViewHolder viewHolder;
    private Context context;

    @Override
    public Filter getFilter() {
        return null;
    }

    public interface AdapterInterface {
        void onDataDeleted(int position);
        void onDataEdited(int position, String newData);
    }

    private AdapterInterface adapterInterface;

    public RecipeSectionAdapter() {
        items = new ArrayList<>();
        itemsPendingRemoval = new ArrayList<>();
        // let's generate some items
        lastInsertedIndex = 15;
        // this should give us a couple of screens worth
        for (int i=1; i<= lastInsertedIndex; i++) {
            items.add("Item " + i);
        }
    }
    public RecipeSectionAdapter(Context setContext, AdapterInterface setAdapterListener, List<String> setItems){
        this.context = setContext;
        this.adapterInterface = setAdapterListener;
        this.items = setItems;
        itemsPendingRemoval = new ArrayList<>();
        lastInsertedIndex = setItems.size();
    }

    public List<String> getItems() {
        return items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final AdapterInterface newAdapterInterface = adapterInterface;
        return new TestViewHolder(parent, newAdapterInterface);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        viewHolder = (TestViewHolder)holder;
        final String item = items.get(position);

        viewHolder.etData.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                String originalText = items.get(position);
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    String text = viewHolder.etData.getText().toString();
                    if (text.isEmpty()) {
                        Toast.makeText(context, "text cannot be empty!", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (originalText.equals(text)) {
                        Toast.makeText(context, "text has not changed!", Toast.LENGTH_SHORT).show();
                        hideKeyboard();
                        return false;
                    }
                    // pass data changed
                    adapterInterface.onDataEdited(position, text);
                    items.set(position, text);
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to undo the removal, let's cancel the pending task
                Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                pendingRunnables.remove(item);
                if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                itemsPendingRemoval.remove(item);
                // this will rebind the row in "normal" state
                notifyItemChanged(items.indexOf(item));
            }
        });

        if (itemsPendingRemoval.contains(item)) {
            // we need to show the "undo" state of the row
            viewHolder.etDataLayout.setVisibility(View.GONE);
            viewHolder.ivMore.setVisibility(View.GONE);
            viewHolder.itemView.setBackgroundColor(Color.RED);
            viewHolder.undoButton.setVisibility(View.VISIBLE);
            viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                    pendingRunnables.remove(item);
                    if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(item);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(items.indexOf(item));
                }
            });
        } else {
            // we need to show the "normal" state
            viewHolder.itemView.setBackgroundColor(Color.WHITE);
            viewHolder.etDataLayout.setVisibility(View.VISIBLE);
            viewHolder.etData.setText(item);
            viewHolder.ivMore.setVisibility(View.VISIBLE);

            viewHolder.undoButton.setVisibility(View.GONE);
            viewHolder.undoButton.setOnClickListener(null);
        }
    }

    // from https://stackoverflow.com/questions/3413157/hide-soft-keyboard-on-done-keypress-in-android
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                context.getSystemService(context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setupUndoButton(TestViewHolder viewHolder, final String item) {

        viewHolder.undoButton.setVisibility(View.VISIBLE);

        viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to undo the removal, let's cancel the pending task
                Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                pendingRunnables.remove(item);
                if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                itemsPendingRemoval.remove(item);
                // this will rebind the row in "normal" state
                notifyItemChanged(items.indexOf(item));
            }
        });
    }

    /**
     *  Utility method to add some rows for testing purposes. You can add rows from the toolbar menu.
     */
    public void addItems(int howMany){
        if (howMany > 0) {
            for (int i = lastInsertedIndex + 1; i <= lastInsertedIndex + howMany; i++) {
                items.add("Item " + i);
                notifyItemInserted(items.size() - 1);
            }
            lastInsertedIndex = lastInsertedIndex + howMany;
        }
    }

    public void setUndoOn(boolean undoOn) {
        this.undoOn = undoOn;
    }

    public boolean isUndoOn() {
        return undoOn;
    }

    public void pendingRemoval(int position) {
        final String item = items.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            //setupUndoButton(viewHolder, item);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(items.indexOf(item));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        String item = items.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (items.contains(item)) {
            items.remove(position);
            // tell the fragment that data was deleted so it will delete it
            adapterInterface.onDataDeleted(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        String item = items.get(position);
        return itemsPendingRemoval.contains(item);
    }
}

/**
 * ViewHolder capable of presenting two states: "normal" and "undo" state.
 */
// TODO: figure out if this should be static
 class TestViewHolder extends RecyclerView.ViewHolder {
    TextInputLayout etDataLayout;
    EditText etData;
    Button undoButton;
    ImageView ivMore;

    private RecipeSectionAdapter.AdapterInterface newAdapterInterface;

    public TestViewHolder(ViewGroup parent, RecipeSectionAdapter.AdapterInterface setAdapterInterface) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view, parent, false));
        //tvData = (TextView) itemView.findViewById(R.id.tvData);
        this.newAdapterInterface = setAdapterInterface;
        undoButton = (Button) itemView.findViewById(R.id.undo_button);
        etDataLayout = (TextInputLayout) itemView.findViewById(R.id.etDataLayout);
        etData = (EditText) itemView.findViewById(R.id.etData);
        ivMore = (ImageView) itemView.findViewById(R.id.ivMore);
        final int position = getAdapterPosition();

        etData.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etData.setRawInputType(InputType.TYPE_CLASS_TEXT);

    }

}
