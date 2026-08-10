package com.pip.itimes.server.stage;

import org.apache.commons.collections.primitives.ByteList;
import org.apache.commons.collections.primitives.ArrayByteList;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ArrayShortList;
import java.util.List;
import java.util.ArrayList;
import com.pip.itimes.server.stage.RoadPoint;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class MonsterGroup{

    private short iconId;

    private byte type;

    private ByteList monsterIds = new ArrayByteList();
    private ShortList probabilities = new ArrayShortList();

    private byte side;

    private byte tileX;
    private byte tileY;
    private short x,y;
    private int id;
    private short refreshSecond;
    private boolean visible;

    private byte eyeshot;
    private int range;
    private List roadPoints = new ArrayList();

    public MonsterGroup() {
    }

    public void setIconId(short id){
        this.iconId = id;
    }

    public short getIconId() {
        return iconId;
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void addMonster(byte monsterId,short probability){
        monsterIds.add(monsterId);
        probabilities.add(probability);
    }

    public byte[] getMonstersId() {
        return monsterIds.toArray();
    }


    public short[] getProbabilities() {
        return probabilities.toArray();
    }

    public void setSide(byte side){
        this.side = side;
    }

    public byte getSide() {
        return side;
    }

    public void setTileX(byte x){
        this.tileX = x;
    }

    public byte getTileX() {
        return tileX;
    }

    public void setTileY(byte y){
        this.tileY = y;
    }

    public byte getTileY() {
        return tileY;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }


    public void setRefreshSecond(short refreshSecond){
        this.refreshSecond = refreshSecond;
    }

    public short getRefreshSecond() {
        return refreshSecond;
    }

    public void setEyeshot(byte eyeshot){
        this.eyeshot = eyeshot;
    }

    public byte getEyeshot(){
        return eyeshot;
    }

    public void setRange(int range){
        this.range = range;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setY(short y) {
        this.y = y;
    }

    public void setX(short x) {
        this.x = x;
    }

    public int getRange(){
        return range;
    }

    public boolean isVisible() {
        return visible;
    }

    public short getY() {
        return y;
    }

    public short getX() {
        return x;
    }

    public void addRoadPoint(RoadPoint roadPoint){
        roadPoints.add(roadPoint);
    }

    public RoadPoint[] getRoadPoints(){
        RoadPoint[] ret = new RoadPoint[roadPoints.size()];
        roadPoints.toArray(ret);
        return ret;
    }

}
