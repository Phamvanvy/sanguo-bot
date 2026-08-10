package com.pip.itimes.server.stage;

import java.util.*;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class HouseTemplate {

    private int instanceId;
    private int level;
    private int style;
    private int price;
    private int gridSize;
    private int addGridSize;			// 扩充的格数
    private int stylePrice;
    private String desc;
    private String styleDesc;
    private Map<Integer,HousePart> parts = new TreeMap<Integer,HousePart>();
    private HousePart defaultPart;
    private int dropGroup;
    private int waiterNpcId;
    private int waiterPrice;
    
    // 卓望版本消费代码
    private String consumeCode;
    private String waiterConsumeCode;
    private String styleConsumeCode;

    public String getConsumeCode() {
        return consumeCode;
    }

    public void setConsumeCode(String consumeCode) {
        this.consumeCode = consumeCode;
    }

    public String getWaiterConsumeCode() {
        return waiterConsumeCode;
    }

    public void setWaiterConsumeCode(String waiterConsumeCode) {
        this.waiterConsumeCode = waiterConsumeCode;
    }

    public String getStyleConsumeCode() {
        return styleConsumeCode;
    }

    public void setStyleConsumeCode(String styleConsumeCode) {
        this.styleConsumeCode = styleConsumeCode;
    }

    private int spMapId,spX,spY;

    public HouseTemplate(int instanceId, int level, int style, int gridSize, int price, int stylePrice, String desc,
                         String styleDesc, int dropGroup) {
        this.instanceId = instanceId;
        this.level = level;
        this.style = style;
        this.price = price;
        this.gridSize = gridSize;
        this.stylePrice = stylePrice;
        this.desc = desc;
        this.styleDesc = styleDesc;
        this.dropGroup = dropGroup;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public int getPrice() {
        return price;
    }

    public int getStyle() {
        return style;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setStyleDesc(String styleDesc) {
        this.styleDesc = styleDesc;
    }

    public void setStylePrice(int stylePrice) {
        this.stylePrice = stylePrice;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setParts(HousePart[] parts) {
        for(int i=0;i<parts.length;i++){
            this.parts.put(parts[i].getId(),parts[i]);
        }
    }

    public HousePart getPart(int index){
        return parts.get(index);
    }

    public Collection<HousePart> getParts(){
        return parts.values();
    }

    public void setDefaultPart(HousePart defaultPart) {
        this.defaultPart = defaultPart;
    }

    public void setDropGroup(int dropGroup) {
        this.dropGroup = dropGroup;
    }

    public void setWaiterNpcId(int waiterNpcId) {
        this.waiterNpcId = waiterNpcId;
    }

    public void setWaiterPrice(int waiterPrice) {
        this.waiterPrice = waiterPrice;
    }

    public void setSpY(int spY) {
        this.spY = spY;
    }

    public void setSpX(int spX) {
        this.spX = spX;
    }

    public void setSpMapId(int spMapId) {
        this.spMapId = spMapId;
    }

    public String getDesc(){
        return desc;
    }

    public String getStyleDesc() {
        return styleDesc;
    }

    public int getStylePrice() {
        return stylePrice;
    }

    public int getGridSize() {
        return gridSize;
    }



    public HousePart getDefaultPart() {
        return defaultPart;
    }

    public int getDropGroup() {
        return dropGroup;
    }

    public int getWaiterNpcId() {
        return waiterNpcId;
    }

    public int getWaiterPrice() {
        return waiterPrice;
    }

    public int getSpY() {
        return spY;
    }

    public int getSpX() {
        return spX;
    }

    public int getSpMapId() {
        return spMapId;
    }

    public int getAddGridSize() {
		return addGridSize;
	}

	public void setAddGridSize(int addGridSize) {
		this.addGridSize = addGridSize;
	}

	public int[] getVisibleIndexes(byte[] visibles){
        IntList l = new ArrayIntList();
        for(int i=0;i<visibles.length;i++){
            HousePart part = parts.get((int)visibles[i]);
            if(part!=null){
                int[] indexes = part.getIndexes();
                for(int j=0;j<indexes.length;j++){
                    l.add(indexes[j]);
                }
            }
        }
        if(defaultPart!=null){
            int[] indexes = defaultPart.getIndexes();
            for(int i=0;i<indexes.length;i++){
                l.add(indexes[i]);
            }
        }
        return l.toArray();
    }

}
