package com.pip.itimes.server.stage;

public class DropGroupDiamondEffect extends Effect {
	private int dropGroupId;
	private int diamondCount;
	private boolean resetBinded;
	private boolean setBinded;
	
	public DropGroupDiamondEffect(int dropGroupId, int diamondCount, boolean resetBinded, boolean setBinded){
		this.dropGroupId = dropGroupId;
		this.diamondCount = diamondCount;
		this.resetBinded = resetBinded;
		this.setBinded = setBinded;
	}
	
	@Override
	public byte getType() {
		return 103;
	}
	
	public int getDropGroupId(){
		return dropGroupId;
	}
	
	public int getDiamondCount(){
		return diamondCount;
	}
	
	public boolean getResetBinded(){
		return resetBinded;
	}
	
	public boolean getSetBinded(){
		return setBinded;
	}
}
