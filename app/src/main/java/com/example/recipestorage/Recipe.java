package com.example.recipestorage;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

@Parcel(analyze={Recipe.class})
@ParseClassName("Recipe") // name needs to match what you named it in Parse Dashboard
public class Recipe extends ParseObject {

    public static final String KEY_CREATED_KEY = "createdAt";
    public static final String KEY_USER = "user";
    private static final String KEY_TITLE = "title";
    private static final String KEY_INGREDIENTS = "ingredients";
    private static final String KEY_DIRECTIONS = "directions";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_RECIPE_IMAGE = "recipeImage";
    private static final String KEY_COOK_TIME_MIN = "cookTimeMin";

    // empty constructor needed by Parcelable library
    public Recipe() {
    }

    public Recipe(ParseUser user, String title, ArrayList<String> ingredients,
                  ArrayList<String> directions, ArrayList<String> notes, int cookTimeMin) {
        setUser(user);
        setTitle(title);
        addIngredients(ingredients);
        addDirections(directions);
        addNotes(notes);
        setCookTimeMin(cookTimeMin);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public String getParsedCreatedAt() {
        Format formatter = new SimpleDateFormat("h:mm a, MMMM d, yyyy");
        String strDate = formatter.format(getCreatedAt());
        return strDate;
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
        saveInBackground();
    }

    public JSONArray getIngredients() { return getJSONArray(KEY_INGREDIENTS); }

    public ArrayList<String> getParsedIngredients() {
        JSONArray rawIngredients = getIngredients();
        ArrayList<String> ingredients = new ArrayList<String>();

        if (rawIngredients != null) {
            for (int i = 0; i < rawIngredients.length(); i++) {
                String ingredient = "";
                try {
                    ingredient = rawIngredients.get(i).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Post", "failed to retrieve ingredient");
                    return null;
                }
                ingredients.add(ingredient);

            }
        }
        return ingredients;
    }

    public void addIngredient(String ingredient) {
        addUnique(KEY_INGREDIENTS, ingredient);
    }

    public void addIngredients(ArrayList<String> ingredients) {
        addAllUnique(KEY_INGREDIENTS, ingredients);
    }

    public void removeIngredient(String ingredient) {
        removeAll(KEY_INGREDIENTS, Arrays.asList(ingredient));
    }

    public void removeIngredients(ArrayList<String> ingredients) {
        removeAll(KEY_INGREDIENTS, ingredients);
    }

    public void clearIngredients() {
        ArrayList<String> ingredient = getParsedIngredients();
        removeNotes(ingredient);
    }

    public JSONArray getDirections() { return getJSONArray(KEY_DIRECTIONS); }

    public ArrayList<String> getParsedDirections() {
        JSONArray rawDirections = getDirections();
        ArrayList<String> directions = new ArrayList<String>();

        if (rawDirections != null) {
            for (int i = 0; i < rawDirections.length(); i++) {
                String direction = "";
                try {
                    direction = rawDirections.get(i).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Post", "failed to retrieve direction");
                    return null;
                }
                directions.add(direction);

            }
        }
        return directions;
    }

    public void addDirection(String direction) {
        addUnique(KEY_DIRECTIONS, direction);
    }

    public void addDirections(ArrayList<String> directions) {
        addAllUnique(KEY_DIRECTIONS, directions);
    }

    public void removeDirection(String direction) {
        removeAll(KEY_DIRECTIONS, Arrays.asList(direction));
    }

    public void removeDirections(ArrayList<String> directions) {
        removeAll(KEY_DIRECTIONS, directions);
    }

    public void clearDirections() {
        ArrayList<String> directions = getParsedDirections();
        removeNotes(directions);
    }


    public JSONArray getNotes() { return getJSONArray(KEY_NOTES); }

    public ArrayList<String> getParsedNotes() {
        JSONArray rawNotes = getNotes();
        ArrayList<String> notes = new ArrayList<String>();

        if (rawNotes != null) {
            for (int i = 0; i < rawNotes.length(); i++) {
                String note = "";
                try {
                    note = rawNotes.get(i).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Post", "failed to retrieve note");
                    return null;
                }
                notes.add(note);
            }
        }
        return notes;
    }

    public void addNote(String note) {
        addUnique(KEY_NOTES, note);
    }

    public void addNotes(ArrayList<String> notes) {
        addAllUnique(KEY_NOTES, notes);
    }

    public void removeNote(String note) {
        removeAll(KEY_NOTES, Arrays.asList(note));
    }

    public void removeNotes(ArrayList<String> notes) {
        removeAll(KEY_NOTES, notes);
    }

    public void clearNotes() {
        ArrayList<String> notes = getParsedNotes();
        removeNotes(notes);
    }

    public ParseFile getProfilePic() { return getUser().getParseFile(KEY_INGREDIENTS); }

    public void setProfilePic(ParseFile profilePic) {
        getUser().put(KEY_INGREDIENTS, profilePic);
    }

    public void addCommentToDatabase(String comment) {
        add(KEY_TITLE, comment);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_RECIPE_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_RECIPE_IMAGE, parseFile);
    }

    public int getCookTimeMin() {
        return getInt(KEY_COOK_TIME_MIN);
    }

    public void setCookTimeMin(int cookTimeMin) {
        put(KEY_COOK_TIME_MIN, cookTimeMin);
    }

    public void saveRecipe() { saveInBackground(); }
}