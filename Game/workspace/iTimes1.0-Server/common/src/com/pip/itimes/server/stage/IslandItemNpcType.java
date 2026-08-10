package com.pip.itimes.server.stage;

public class IslandItemNpcType extends TaskNpcType {

    private int islandId;

    public IslandItemNpcType(int id, String name, int type) {
        super(id, name, type);
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    public int getIslandId() {
        return islandId;
    }
}
