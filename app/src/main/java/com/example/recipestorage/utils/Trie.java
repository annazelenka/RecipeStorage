package com.example.recipestorage.utils;
// https://www.interviewcake.com/concept/java/trie background info:
// "The trie is a tree of nodes which supports Find and Insert operations.
// Find returns the value for a key string,
// Insert inserts a string (the key) and a value into the trie.
// Both Insert and Find run in O(m) time, where m is the length of the key.
// more info: https://www.geeksforgeeks.org/trie-insert-and-search/

import com.example.recipestorage.Recipe;
import java.util.ArrayList;
import java.util.List;

public class Trie {
    public TrieNode root;
    public ArrayList<Recipe> leafNodes;

    public class TrieNode {
        ArrayList<TrieNode> children;
        char value;
        boolean isRecipe;
        Recipe recipe;

        public TrieNode() {
            children = new ArrayList<TrieNode>();
            isRecipe = false;
        }

        public ArrayList<TrieNode> getChildren() {
            return children;
        }

        public char getValue() {
            return value;
        }

        public boolean isRecipe() {
            return isRecipe;
        }

        public Recipe getRecipe() {
            return recipe;
        }
    }

    public Trie() {
        root = new TrieNode();
        root.children = new ArrayList<TrieNode>();
        root.isRecipe = false;
        root.recipe = null;
    }

    /**
     Retrieve trie root. Used for testing purposes.
     @return  the trie trie root
     */
    public TrieNode getRoot() {
        return root;
    }

    /**
     Attempts to find given key in trie.
     @param key the key to search for
     @return  If key exists, returns arraylist of all recipes that
     contain that key. Otherwise returns null.
     */
    public ArrayList<Recipe> find(String key) {
        key = key.toLowerCase();
        TrieNode current = root;

        outerloop:
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            for (TrieNode child: current.children) {
                if (child.value == c) {
                    current = child;
                    continue outerloop;
                }
            }
            // none of the children match, so key doesn't exist in this trie
            return null;
        }

        // now, we have reached the end of input word;
        // retrieve all possible Recipes
        leafNodes = new ArrayList<Recipe>();
        populateLeafNodesAtSubroot(current);


        return leafNodes;
    }

    /**
     Helper method to populate "leafNodes" variable w/ all recipes at a subroot.
     @param subroot the place in the tree where we start
     */
    private void populateLeafNodesAtSubroot(TrieNode subroot) {
        if (subroot.isRecipe) {
            leafNodes.add(subroot.recipe);
            return;
        }
        for (TrieNode child: subroot.children) {
            populateLeafNodesAtSubroot(child);
        }
    }

    public void insert(String key, Recipe recipe) {
        key = key.toLowerCase();
        TrieNode current = root;
        int insertPosition = key.length();

        outerloop:
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            for (TrieNode child: current.children) {
                if (child.value == c) {
                    current = child;
                    // PROBLEM
                    continue outerloop;
                }


            }
            // none of the children match, so it's time to insert
            insertPosition = i;
            break;
        }

        for (int i = insertPosition; i < key.length(); i++) {
            TrieNode newChild = new TrieNode();
            newChild.value = key.charAt(i);
            current.children.add(newChild);
            current = newChild;
        }

        TrieNode recipeNode = new TrieNode();
        recipeNode.isRecipe = true;
        recipeNode.recipe = recipe;

        current.children.add(recipeNode);
    }

    public void populateRecipeTrie(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            insert(recipe.getTitle(), recipe);
        }
    }
}
