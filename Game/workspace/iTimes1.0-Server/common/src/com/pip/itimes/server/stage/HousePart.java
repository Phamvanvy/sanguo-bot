package com.pip.itimes.server.stage;

public class HousePart {
    private int id;
    private int[] indexes;
    private int price;
    private String desc;
    
    // 卓望版本消费代码
    private String consumeCode;

    public HousePart(int id,int[] indexes,int price,String desc,String consumeCode){
        this.id = id;
        this.indexes = indexes;
        this.price = price;
        this.desc = desc;
        this.consumeCode = consumeCode;
    }

    public String getConsumeCode() {
        return consumeCode;
    }

    public void setConsumeCode(String consumeCode) {
        this.consumeCode = consumeCode;
    }

    public int getPrice() {
        return price;
    }

    public int[] getIndexes() {
        return indexes;
    }

    public int getId() {
        return id;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setIndexes(int[] indexes) {
        this.indexes = indexes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }
}
