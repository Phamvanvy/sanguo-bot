package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class SkillNpcType extends TaskNpcType{

    private int clazz;
    private Recipe[] recipes;

    public SkillNpcType(int id,String name,int type) {
        super(id,name,type);
    }

    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }

    public void setRecipes(Recipe[] recipes){
        this.recipes = recipes;
    }

    public Recipe[] getRecipes(){
        return recipes;
    }

}
