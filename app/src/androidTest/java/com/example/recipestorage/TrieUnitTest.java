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
    Context appContext;
    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        ParseObject.registerSubclass(Recipe.class);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(appContext)
                .applicationId(BuildConfig.APP_ID) // should correspond to APP_ID env variable
                .clientKey(BuildConfig.MASTER_KEY)  // set explicitly unless clientKey is explicitly configured on Parse server
                .clientBuilder(builder)
                .server(BuildConfig.SERVER_URL).build()); // USE HTTPS!!
    }

//    @Test
//    public void useAppContext() {
//        // Context of the app under test.
//        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        assertEquals("com.example.recipestorage", appContext.getPackageName());
//    }


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

        ArrayList<Recipe> expectedRecipes = new ArrayList<Recipe>();
        expectedRecipes.add(null);

        assertTrue(expectedRecipes.equals(trie.find(key)));
    }

    @Test
    public void testOneInsertAndFind() {
        String key = "tacos";
        Recipe recipe = new Recipe();
        recipe.setTitle("tacos");
        recipe.setIsFavorite(true);


        Trie trie = new Trie();
        trie.insert(key, recipe);

        ArrayList<Recipe> expectedRecipes = new ArrayList<Recipe>();
        expectedRecipes.add(recipe);

        assertTrue(expectedRecipes.equals(trie.find(key)));
    }

    @Test
    public void testMultipleInsertAndFind() {
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        ArrayList<Recipe> expectedRecipes = new ArrayList<Recipe>();

        Recipe recipe1 = (Recipe) ParseObject.create("Recipe");
        Recipe recipe2 = (Recipe) ParseObject.create("Recipe");
        Recipe recipe3 = (Recipe) ParseObject.create("Recipe");
        recipe1.setTitle("tacos");
        recipe1.setIsFavorite(true);
        recipe2.setTitle("pecan pie");
        recipe2.setCookTimeMin(45);
        recipe3.setTitle("taco");

        recipes.add(recipe1);
        recipes.add(recipe2);
        recipes.add(recipe3);

        Trie trie = new Trie();

        for (Recipe recipe: recipes) {
            trie.insert(recipe.getTitle(), recipe);
        } //problem putting in recipe2, "taco"

        String key = "taco";
        String key2 = "pecan pie";
        String key3 = "smoothie";

        // make sure searching for "smoothie" returns list w/ null
        assertNull(trie.find(key3));

        // make sure searching for "taco" returns both taco AND tacos recipe
        expectedRecipes.add(recipe1);
        expectedRecipes.add(recipe3);
        assertEquals(expectedRecipes,trie.find(key));

        // make sure searching for "pecan pie" returns ONLY pecan pie recipe
        expectedRecipes.set(0, recipe2);
        expectedRecipes.remove(1);
        assertEquals(expectedRecipes, trie.find(key2));
    }
}