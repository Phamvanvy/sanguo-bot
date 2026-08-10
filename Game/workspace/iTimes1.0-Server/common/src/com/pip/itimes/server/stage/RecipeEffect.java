package com.pip.itimes.server.stage;

public class RecipeEffect extends Effect {
	private int recipeId;
	public RecipeEffect(int recipeId) {
		this.recipeId = recipeId;
	}

	public byte getType() {
		return 65;
	}
	
	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}

	public int getRecipeId() {
		return recipeId;
	}
}