package com.pip.itimes.server.bean;

import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Treasure {

    private int id;
    private int playerId;
    private short mapId;
    private short x,y;
    private int itemGroupId;
    private int shovelId;
    private Date createTime;

//    private static final String[][] DIRECT = { {"西北,请再往左上走几步试试~", "北,请再往上走几步试试~", "东北,请再往右上走几步试试~"}, {"西,请再往左边走几步试试~", "脚下,请左右走几步试试~",
//                                             "东,请再往右边走几步试试~"}, {"西南,请再往左下走几步试试~", "南,请再往下面走几步试试~", "东南,请再往右下走几步试试~"}
//    };
    private static final String[][] DIRECT = { {"左上", "上面", "右上"}, {"左边", "脚下","右边"}, {"左下", "下面", "右下"}
    };
    
    private static final String[] DISTANCE ={"再走几步就到了。", "还要再走一段路才到。", "还有很长一段路要走，加油哦！坚持就是胜利！"};
    public Treasure() {
    }

    public short getY() {
        return y;
    }

    public short getX() {
        return x;
    }


    public int getPlayerId() {
        return playerId;
    }

    public short getMapId() {
        return mapId;
    }

    public int getItemGroupId() {
        return itemGroupId;
    }

    public int getId() {
        return id;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setY(short y) {
        this.y = y;
    }

    public void setX(short x) {
        this.x = x;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public void setItemGroupId(int itemGroupId) {
        this.itemGroupId = itemGroupId;
    }
    
    public void setShovelId (int shovelId) {
    	this.shovelId = shovelId;
    }
    
    public int getShovelId () {
    	return shovelId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public boolean isValid(short x,short y){
        if(Math.abs(this.x-x)<=32&&Math.abs(this.y-y)<=32)
            return true;
        return false;
    }

    public String getNotifyMessage(short x,short y){
        int x0 = 0;
        int y0 = 0;
        if(Math.abs(this.y - y) <= 32){
        	x0 = 1;
        }else if(this.y>y){
            x0 = 2;
        }else{
            x0 = 0;
        }
        if(Math.abs(this.x-x) <=32){
        	y0 = 1;
        }else if(this.x>x){
            y0 = 2;
        }else{
            y0 = 0;
        }
        if(x0 == 1 && y0 == 1){
        	return DIRECT[x0][y0] + "，请左右走几步试试~";
        }else{
	        int dis = (int)(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
	        if(dis <= Math.pow(32 + 40,2)){
	        	dis = 0;
	        }else if(dis <= Math.pow(32 + 120,2)){
	        	dis = 1;
	        }else{
	        	dis = 2;
	        }
	        return DIRECT[x0][y0] + "，" + DISTANCE[dis];
        }
    }
    
}
