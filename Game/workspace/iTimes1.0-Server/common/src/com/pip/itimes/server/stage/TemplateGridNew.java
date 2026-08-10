package com.pip.itimes.server.stage;

public class TemplateGridNew{
	private TemplateGrid grid ;
	private int consumeMode;
	public TemplateGridNew(IItemTemplate template,int count,int consumeMode){
		 this.grid = new TemplateGrid(template,count);
		 this.consumeMode = consumeMode;
	}
	public TemplateGrid getGrid() {
		return grid;
	}
	public void setGrid(TemplateGrid grid) {
		this.grid = grid;
	}
	public int getConsumeMode() {
		return consumeMode;
	}
	public void setConsumeMode(int consumeMode) {
		this.consumeMode = consumeMode;
	}
	
}