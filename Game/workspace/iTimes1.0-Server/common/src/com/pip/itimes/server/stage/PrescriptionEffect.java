package com.pip.itimes.server.stage;

public class PrescriptionEffect extends Effect {
	private int recipeId;
	public PrescriptionEffect(int recipeId) {
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