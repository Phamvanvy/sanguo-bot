package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class SkillNpc extends TaskNpc{

    private List recipes = new ArrayList();

    public SkillNpc() {
        super();
    }

    public void addRecipe(Recipe recpie){
        recipes.add(recpie);
    }

    public Recipe[] getRecipes(){
        Recipe[] ret = new Recipe[recipes.size()];
        recipes.toArray(ret);
        return ret;
    }
}
