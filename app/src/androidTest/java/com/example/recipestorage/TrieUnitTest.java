package com.example.recipestorage;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.recipestorage.utils.Trie;
import com.parse.Parse;
import com.parse.ParseObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TrieUnitTest {
    private static Context appContext;
    private static Recipe recipe;
    private static Recipe recipe2;
    private static Recipe recipe3;
    private static ArrayList<Recipe> recipes;
    private static ArrayList<Recipe> expectedRecipes;


    @Before
    public void testSetUp() {
        // Set up Parse so that we can use Recipe objects.
        // for more info, see ParseApplication.
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        ParseObject.registerSubclass(Recipe.class);

        // for more info, see ParseApplication
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        Parse.initialize(new Parse.Configuration.Builder(appContext)
                .applicationId(BuildConfig.APP_ID) // should correspond to APP_ID env variable
                .clientKey(BuildConfig.MASTER_KEY)  // set explicitly unless clientKey is explicitly configured on Parse server
                .clientBuilder(builder)
                .server(BuildConfig.SERVER_URL).build()); // USE HTTPS!!

    }

    @Test
    public void testFindNullTree() {
        Trie trie = new Trie();

        assertEquals(null, trie.find("a"));

        // make sure find didn't break anything
        Trie.TrieNode root = trie.getRoot();
        assertFalse(root.isRecipe());
        assertEquals(0, root.getValue());
        assertEquals(0, root.getChildren().size());
        assertNull(root.getRecipe());
    }


    @Test
    public void testSimpleInsertAndFind() {
        String key = "a";

        Trie trie = new Trie();
        trie.insert(key, null);

        expectedRecipes = new ArrayList<Recipe>();
        expectedRecipes.add(null);

        assertTrue(expectedRecipes.equals(trie.find(key)));
    }

    @Test
    public void testOneInsertAndFind() {
        String key = "tacos";
        Recipe recipe4 = new Recipe();
        recipe4.setTitle("tacos");
        recipe4.setIsFavorite(true);


        Trie trie = new Trie();
        trie.insert(key, recipe4);

        expectedRecipes = new ArrayList<Recipe>();
        expectedRecipes.add(recipe4);
        assertTrue(expectedRecipes.equals(trie.find(key)));
    }

    @Test
    public void testMultipleInsertAndFind() {
        // set up some default recipes
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        ArrayList<Recipe> expectedRecipes = new ArrayList<Recipe>();

        recipe = (Recipe) ParseObject.create("Recipe");
        recipe2 = (Recipe) ParseObject.create("Recipe");
        recipe3 = (Recipe) ParseObject.create("Recipe");
        recipe.setTitle("tacos");
        recipe.setIsFavorite(true);
        recipe2.setTitle("pecan pie");
        recipe2.setCookTimeMin(45);
        recipe3.setTitle("taco");

        recipes.add(recipe);
        recipes.add(recipe2);
        recipes.add(recipe3);

        expectedRecipes = new ArrayList<Recipe>();

        Trie trie = new Trie();
        expectedRecipes = new ArrayList<Recipe>();

        for (Recipe recipe: recipes) {
            trie.insert(recipe.getTitle(), recipe);
        } //problem putting in recipe2, "taco"

        String key = "taco";
        String key2 = "pecan pie";
        String key3 = "smoothie";

        // make sure searching for "smoothie" returns list w/ null
        assertNull(trie.find(key3));

        // make sure searching for "taco" returns both taco AND tacos recipe
        expectedRecipes.add(recipe);
        expectedRecipes.add(recipe3);
        assertEquals(expectedRecipes,trie.find(key));

        // make sure searching for "pecan pie" returns ONLY pecan pie recipe
        expectedRecipes.set(0, recipe2);
        expectedRecipes.remove(1);
        assertEquals(expectedRecipes, trie.find(key2));
    }
}