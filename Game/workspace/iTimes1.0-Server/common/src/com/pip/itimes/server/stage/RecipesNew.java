package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class RecipesNew {

    private static Map recipesNew = new HashMap();


    public static void addRecipeNew(RecipeNew recipeNew){
        recipesNew.put(new Integer(recipeNew.getId()),recipeNew);
    }

    public static RecipeNew getRecipeNew(int id){
        return (RecipeNew)recipesNew.get(new Integer(id));
    }


}