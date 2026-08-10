package com.pip.itimes.server.stage;

public class DeleteAddItemEffect extends Effect {
	private int deletedItemId;
	private int deletedItemCount;
	private int addedItemId;
	private int addedItemCount;
	
	public DeleteAddItemEffect(int deletedItemId, int deletedItemCount, int addedItemId, int addedItemCount){
		this.deletedItemId = deletedItemId;
		this.deletedItemCount = deletedItemCount;
		this.addedItemId = addedItemId;
		this.addedItemCount = addedItemCount;
	}
	
	@Override
	public byte getType() {
		return 106;
	}

    public int getDeletedItemId(){
        return deletedItemId;
    }

    public int getDeletedItemCount(){
        return deletedItemCount;
    }

    public int getAddedItemId(){
        return addedItemId;
    }

    public int getAddedItemCount(){
        return addedItemCount;
    }
}
