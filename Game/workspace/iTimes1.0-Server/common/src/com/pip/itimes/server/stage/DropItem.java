package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class DropItem {

    private int id;
    private IItemTemplate item;
    private int rate;
    private int min;
    private int max;

    public DropItem() {
    }

    public int getRate() {
        return rate;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public IItemTemplate getItem() {
        return item;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setItem(IItemTemplate item) {
        this.item = item;
    }

    public int getId() {
        return id;
    }
}
