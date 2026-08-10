package com.pip.sanguo.data.item;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;

public class DropNode extends DataObject{
    public static final int ERROR_VALUE = -1;
    public static final int ERROR_SYMBOL = -2;
    public static final int ERROR_OUT_OF_RANGE = -3;
    public static final int ERROR_GEN = -4;
    
    public static final int VALUE_YES = 1;
    public static final int VALUE_NO = 0;
    
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_EQUIPMENT = 1;
    public static final int TYPE_DROPGROUP = 2;
//    public static final int TYPE_MONEY = 3;
    /** 掉落机率精确度 */
    public static final int RATE_ACCURACY = 1000000;
    /**
     * 节点类型
     * 物品
     * 装备
     * 掉落组
     * 金钱
     */
    public int type;
    
    /**
     * 掉落物品最大数量
     */
    public int quantityMax;
    /**
     * 掉落物品的最少数量
     */
    public int quantityMin;
    /** 
     * 掉落机率值
     *  掉落机率 = dropRate/1,000,000
     */
    public int dropRate;
    
    /**
     * 是否是任务掉落
     */
    public boolean isTask;
    
    /**
     * 当该掉落是一个任务掉落的时候，存储相关任务id
     */
    public int taskId = -1;
    
    
    public DropNode(int type){
        this.type = type;
    }
    
    public DropNode(){
        
    }

    public boolean depends(DataObject obj) {
        return false;
    }

    public DataObject duplicate() {
        DropNode node = new DropNode();
        node.update(this);
        return node;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }

    public void load(Element elem) {
        type = Integer.parseInt(elem.getAttributeValue("droptype"));
        id = Integer.parseInt(elem.getAttributeValue("dropid"));
        quantityMax = Integer.parseInt(elem.getAttributeValue("quantityMax"));
        quantityMin = Integer.parseInt(elem.getAttributeValue("quantityMin"));
        isTask = Boolean.parseBoolean(elem.getAttributeValue("isTask"));
        dropRate = Integer.parseInt(elem.getAttributeValue("dropRate"));
        taskId = Integer.parseInt(elem.getAttributeValue("taskId"));
    }

    public Element save() {
        Element ret = new Element("dropnode");
        ret.addAttribute("droptype",String.valueOf(type));
        ret.addAttribute("dropid",String.valueOf(id));
        ret.addAttribute("quantityMax",String.valueOf(quantityMax));
        ret.addAttribute("quantityMin",String.valueOf(quantityMin));
        ret.addAttribute("isTask",String.valueOf(isTask));
        ret.addAttribute("dropRate",String.valueOf(dropRate));
        ret.addAttribute("taskId",String.valueOf(taskId));
        return ret;
    }

    public void update(DataObject obj) {
        DropNode node = (DropNode)obj;
        id = node.id;
        type = node.type;
        quantityMax = node.quantityMax;
        quantityMin = node.quantityMin;
        isTask = node.isTask;
        dropRate = node.dropRate;
        taskId = node.taskId;
    }
    
    /**
     * 
     * @param rate
     * @return
     */
    public int getDropRate(String rate){
        if(rate == null || "".equals(rate)){
            return ERROR_GEN;
        }
        
        /* 示例：1% = 1/100   1%% = 1/10,000   1%%% = 1/1,000,000 */
        int value = 0;
        int symbolCnt = 0;
        String valStr ;
        
        int index = rate.indexOf("%");
        if(index != -1){
            valStr = rate.substring(0, index);
            String accuracy = rate.substring(index);
            
            //判断后续的符号中是否都是“%”
            while(accuracy.length() > 0){
                if(accuracy.endsWith("%")){                    
                    accuracy = accuracy.substring(0 , accuracy.length() - 1);
                    symbolCnt++;
                }
                else{
                    return ERROR_SYMBOL;
                }
            }
        }
        else{
            valStr = rate;
        }
        
        try {
            value = Integer.parseInt(valStr);
        }
        catch (NumberFormatException e) {
            return ERROR_VALUE;
        }
        
        if((symbolCnt > 3) || (symbolCnt == 1 && value > 100) 
                || (symbolCnt == 2 && value > 10000) || (value > 1000000)){
            return ERROR_OUT_OF_RANGE;
        }
        
        return value * (int)Math.pow(100 , 3 - symbolCnt);
    }
    
    
    public String getRateString(){
        if(dropRate % 10000 == 0){
            return dropRate/10000 + "%";
        }
        else if(dropRate % 100 == 0){
            return dropRate/100 + "%%";
        }
        else{
            return String.valueOf(dropRate);
        }
    }
}
