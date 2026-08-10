package com.pip.itimes.server.stage;

public class HouseWaiter {

    private int id;
    private String name;
    private int imageId;

    public HouseWaiter(int id,String name,int imageId) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getId() {
        return id;
    }
}
