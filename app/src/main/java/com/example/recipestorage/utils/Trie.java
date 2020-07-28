package com.example.recipestorage.utils;
// https://www.interviewcake.com/concept/java/trie background info:
// "The trie is a tree of nodes which supports Find and Insert operations.
// Find returns the value for a key string,
// Insert inserts a string (the key) and a value into the trie.
// Both Insert and Find run in O(m) time, where m is the length of the key.
// more info: https://www.geeksforgeeks.org/trie-insert-and-search/

import com.example.recipestorage.Recipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Trie {
    // public so that Parcel can be used to wrap these in intents
    public TrieNode root;
    public ArrayList<TrieNode> leafNodes;
    public Stack<TrieNode> stack;

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
        TrieNode current = getInsertionNode(key, false);

        if (current == null) return null;

        // now, we have reached the end of input word;
        // retrieve all possible Recipes
        leafNodes = new ArrayList<TrieNode>();
        populateLeafNodesAtSubroot(current);
//        return leafNodes;


        ArrayList<Recipe> recipes =  new ArrayList<Recipe>();
        for (TrieNode node: leafNodes) {
            recipes.add(node.recipe);
        }
        return recipes;
    }

    /**
     Helper method to populate "leafNodes" variable w/ all recipes at a subroot.
     @param subroot the place in the tree where we start
     */
    private void populateLeafNodesAtSubroot(TrieNode subroot) {
        if (subroot == null)  return;
        if (subroot.isRecipe) {
            //leafNodes.add(subroot.recipe);
            leafNodes.add(subroot);
            return;
        }
        for (TrieNode child: subroot.children) {
            populateLeafNodesAtSubroot(child);
        }
    }


    /**
     inserts a recipe into the trie.
     @param key the title of the recipe, used to find the recipe in the tree
     @param recipe the recipe to be inserted
     */
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

    /**
     Given a list of recipe, this method adds all the recipes to the trie.
     @param recipes the recipes to be inserted
     */
    public void populateRecipeTrie(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            insert(recipe.getTitle(), recipe);
        }
    }

    /**
     Helper method to figure out where a key ends in the trie.
     @param key the recipe title
     @param shouldPopulateStack whether the method should track how to get back to the parent
     @return the TrieNode where the key ends
     */
    private TrieNode getInsertionNode(String key, boolean shouldPopulateStack) {
        key = key.toLowerCase();
        TrieNode current = root;

        stack = new Stack<TrieNode>();

        outerloop:
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            for (TrieNode child: current.children) {
                if (child.value == c) {
                    if (shouldPopulateStack) {
                        stack.add(current);
                    }
                    current = child;
                    continue outerloop;
                }
            }
            // none of the children match, so key doesn't exist in this trie
            return null;
        }
        return current;
    }

    /**
     Deletes a specific recipe from the tree. Deletes a recipe instead of all recipes under a given
     key because RecipeStorage users can only delete recipes, not all recipes with a specific title.
     @param recipeToDelete the recipe to be deleted
     */
    public void delete(Recipe recipeToDelete) {
        TrieNode current = getInsertionNode(recipeToDelete.getTitle(), true);
        leafNodes = new ArrayList<TrieNode>();
        populateLeafNodesAtSubroot(current);

        if (leafNodes.size() == 0) return;

        // delete the key (or part of it) if there is only one recipe under this key, otherwise
        // leave it alone because there are other recipes
        boolean shouldDeleteKey = (leafNodes.size() == 1);

        for (TrieNode recipeNode: leafNodes) {
            if (recipeNode.recipe == recipeToDelete) {
                current.children.remove(recipeNode);
                recipeNode = null;
            }
        }

        if (shouldDeleteKey) {
            while (!stack.empty()) {
                boolean currentHasChildren = current.children.size() != 0;
                if (currentHasChildren) return;
                TrieNode parent = stack.pop();
                parent.children.remove(current);
                current = parent;
            }
        }
    }

}
