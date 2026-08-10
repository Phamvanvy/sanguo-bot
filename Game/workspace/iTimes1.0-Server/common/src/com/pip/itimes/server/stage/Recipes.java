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
public class Recipes {

    private static Map recipes = new HashMap();


    public static void addRecipe(Recipe recipe){
        recipes.put(new Integer(recipe.getId()),recipe);
    }

    public static Recipe getRecipe(int id){
        return (Recipe)recipes.get(new Integer(id));
    }


}
