package com.pip.engine;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Utilities;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameWorld;

public class Weather{
//#if SupportWeather == true
    private int type;
    private int speed;
    private int speedDiff;
    private int wind;
    private int color;
    private int size;
    private int count;
    private int dieCount;
    private int die;
    private int endTime;
    private short[][] data;
    private Vector dieData = new Vector();

    private static final Random rand = new Random(System.currentTimeMillis());

    public static final int WEATHER_TYPE_RAIN = 0;
    public static final int WEATHER_TYPE_SNOW = 1;
    
    public static final int WEATHER_PARA_SIZE = 0;
    public static final int WEATHER_PARA_COUNT = 1;
    public static final int WEATHER_PARA_SPEED = 2;
    public static final int WEATHER_PARA_SPEED_DIFF = 3;
    public static final int WEATHER_PARA_WIND = 4;
    public static final int WEATHER_PARA_COLOR = 5;
    public static final int WEATHER_PARA_DIE = 6;
    public static final int WEATHER_PARA_DIE_COUNT = 7;
    public static final int WEATHER_PARA_END_TIME = 8;

    public Weather(int type, int size, int count, int speed, int speedDiff, int wind, int color, int die, int dieCount, int endTime){
        this.type = type;
        this.speed = speed;
        this.speedDiff = speedDiff;
        this.wind = wind;
        this.color = color;
        this.size = size;
        this.count = count;
        this.dieCount = dieCount;
        this.die = die;
        this.endTime = endTime;
        data = new short[7][count];
        init(0, count);
    }

    public void adjustPara(int subType, int value){
        switch(subType){
            case WEATHER_PARA_SIZE:
                setSize(value);
                break;
            case WEATHER_PARA_COUNT:
                setCount(value);
                break;
            case WEATHER_PARA_SPEED:
                setSpeed(value);
                break;
            case WEATHER_PARA_SPEED_DIFF:
                setSpeedDiff(value);
                break;
            case WEATHER_PARA_WIND:
                setWind(value);
                break;
            case WEATHER_PARA_COLOR:
                setColor(value);
                break;
            case WEATHER_PARA_DIE:
                setDie(value);
                break;
            case WEATHER_PARA_DIE_COUNT:
                setDie(value);
                break;
            case WEATHER_PARA_END_TIME:
                setEndTime(value);
                break;
        }
    }
    
    public int getDieCount(){
        return dieCount;
    }
    
    public void setDieCount(int dieCount){
        this.dieCount = dieCount;
    }
    
    public int getColor(){
        return color;
    }
    
    public void setColor(int color){
        this.color = color;
    }
    
    public int getEndtime(){
        return endTime;
    }
    
    public void setEndTime(int endTime){
        this.endTime = endTime;
    }
    
    public int getDie(){
        return die;
    }

    public void setDie(int die){
        if(die < 0){
            die = 0;
        }

        this.die = die;
        rebuildDiePara();
    }

    public int getSize(){
        return size;
    }

    public void setSize(int size){
        if(size < 0){
            size = 0;
        }

        this.size = size;
    }

    public int getSpeed(){
        return speed;
    }

    public void setSpeed(int speed){
        if(speed < 0){
            speed = 0;
        }

        this.speed = speed;
        rebuildSpeed();
    }

    public int getSpeedDiff(){
        return speedDiff;
    }

    public void setSpeedDiff(int speedDiff){
        if(speedDiff < 0){
            speedDiff = 0;
        }

        this.speedDiff = speedDiff;
        rebuildSpeed();
    }

    public int getCount(){
        return count;
    }

    public void setCount(int count){
        if(count < 0){
            count = 0;
        }

        if(count > this.count){
            for(int i = 0; i < data.length; i++){
                short[] tmp = new short[count];
                System.arraycopy(data[i], 0, tmp, 0, data[i].length);
                data[i] = tmp;
            }

            int oldCount = this.count;
            this.count = count;
            init(oldCount, count);
        }else{
            for(int i = 0; i < data.length; i++){
                short[] tmp = new short[count];
                System.arraycopy(data[i], 0, tmp, 0, count);
                data[i] = tmp;
            }

            this.count = count;
        }
    }

    public int getWind(){
        return wind;
    }

    public void setWind(int wind){
        this.wind = wind;
    }

    public void draw(Graphics g){
        switch(type){
            case WEATHER_TYPE_RAIN: {
                g.setColor(color);

                for(int i = 0; i < count; i++){
                    if(data[6][i] == 1){
                        g.drawLine(data[0][i], data[1][i], data[2][i], data[3][i]);
                    }
                }

                int dieCount = dieData.size();
                Vector restDieData = new Vector();

                for(int i = 0; i < dieCount; i++){
                    short[] dieInfo = (short[]) dieData.elementAt(i);
                    dieInfo[5]++;

                    if(dieInfo[5] >= dieInfo[4]){
                        continue;
                    }else{
                        restDieData.addElement(dieInfo);

                        if(dieInfo[5] < 2){
                            continue;
                        }

                        int x = dieInfo[0] + dieInfo[2] - GameWorld.viewX;
                        int y = dieInfo[1] + dieInfo[3] - GameWorld.viewY;

                        g.drawArc(x, y, dieInfo[5], dieInfo[5] / 2, 0, 360);
                    }
                }

                dieData = restDieData;
            }
                break;
            case WEATHER_TYPE_SNOW: {
                g.setColor(color);

                for(int i = 0; i < count; i++){
                    g.fillRect(data[0][i], data[1][i], data[2][i], data[2][i]);
                }
            }
                break;
        }
    }

    public void cycle(){
        if(endTime != -1 && Utilities.getServerTime() > endTime){
            GameMain.weather = null;
            
            return;
        }
        
        switch(type){
            case WEATHER_TYPE_RAIN: {
                for(int i = 0; i < count; i++){
                    boolean reborn = false;

                    data[0][i] += wind;
                    data[1][i] += data[4][i];
                    data[2][i] += wind;
                    data[3][i] += data[4][i];

                    data[5][i] -= data[4][i];

                    if(data[5][i] <= 0 && dieData.size() < dieCount){
                        data[6][i] = 0;

                        short[] dieInfo = new short[6];
                        dieInfo[0] = data[2][i];
                        dieInfo[1] = data[3][i];
                        dieInfo[2] = (short) GameWorld.viewX;
                        dieInfo[3] = (short) GameWorld.viewY;
                        dieInfo[4] = (short) die;
                        dieInfo[5] = 0;

                        dieData.addElement(dieInfo);

                        reborn = true;
                    }

                    if(data[1][i] > GameMain.viewHeight){
                        data[1][i] -= GameMain.viewHeight;
                        data[6][i] = 1;
                        reborn = true;
                    }else{
                        if(wind >= 0 && data[0][i] > GameMain.viewWidth){
                            data[0][i] -= GameMain.viewWidth;
                            data[6][i] = 1;
                            reborn = true;
                        }else if(wind < 0 && data[0][i] < 0){
                            data[0][i] += GameMain.viewWidth;
                            data[6][i] = 1;
                            reborn = true;
                        }
                    }

                    if(reborn){
                        data[2][i] = (short) (data[0][i] + wind);
                        data[3][i] = (short) (data[1][i] + size);

                        if(GameMain.viewHeight - data[3][i] > 0){
                            data[5][i] = (short) rand.nextInt(GameMain.viewHeight - data[3][i]);
                        }else{
                            data[5][i] = Short.MAX_VALUE;
                        }
                    }
                }
            }
                break;
            case WEATHER_TYPE_SNOW: {
                for(int i = 0; i < count; i++){
                    boolean reborn = false;
                    
                    data[5][i]++;

                    if(data[5][i] > size * speed){
                        rebuildSnowPara1(i);
                    }
                    
                    data[0][i] += data[6][i];
                    data[1][i] += data[4][i];

                    if(data[1][i] > GameMain.viewHeight){
                        data[1][i] -= GameMain.viewHeight;
                        reborn = true;
                    }else{
                        if(data[6][i] >= 0 && data[0][i] > GameMain.viewWidth){
                            data[0][i] -= GameMain.viewWidth;
                            reborn = true;
                        }else if(data[6][i] < 0 && data[0][i] < 0){
                            data[0][i] += GameMain.viewWidth;
                            reborn = true;
                        }
                    }

                    if(reborn){
                        rebuildSnowPara(i);
                    }
                }
            }
                break;
        }
    }

    private void init(int start, int end){
        for(int i = start; i < end; i++){
            born(i);
        }

        rebuildSpeed();
    }

    private void born(int index){
        switch(type){
            case WEATHER_TYPE_RAIN: {
                data[0][index] = (short) rand.nextInt(GameMain.viewWidth);
                data[1][index] = (short) rand.nextInt(GameMain.viewHeight);
                data[2][index] = (short) (data[0][index] + wind);
                data[3][index] = (short) (data[1][index] + size);
                data[6][index] = 1;
            }
                break;
            case WEATHER_TYPE_SNOW: {
                data[0][index] = (short) rand.nextInt(GameMain.viewWidth);
                data[1][index] = (short) rand.nextInt(GameMain.viewHeight);
            }
                break;
        }
    }

    private void rebuildSnowPara(int index){
        if(rand.nextInt(2) == 0){
            data[2][index] = (short) size;
        }else{
            data[2][index] = (short) (size / 2);
        }

        rebuildSnowPara1(index);
    }

    private void rebuildSnowPara1(int index){
        data[3][index] = (short) rand.nextInt(3);
        data[5][index] = 0;

        switch(data[3][index]){
            case 0:
                data[6][index] = (short) wind;

                break;
            case 1:
                data[6][index] = (short) (wind >= 0? wind + 1: wind - 1);

                break;
            case 2:
                data[6][index] = (short) (wind >= 0? wind - 1: wind + 1);
        }
    }

    private void rebuildSpeed(){
        switch(type){
            case WEATHER_TYPE_RAIN: {
                for(int i = 0; i < count; i++){
                    data[4][i] = (short) (speed + rand.nextInt(speed * speedDiff / 100 + 1));
                }
            }
                break;
            case WEATHER_TYPE_SNOW: {
                for(int i = 0; i < count; i++){
                    data[4][i] = (short) (speed + rand.nextInt(speed * speedDiff / 100 + 1));
                    rebuildSnowPara(i);
                }
            }
                break;
        }

        rebuildDiePara();
    }

    private void rebuildDiePara(){
        switch(type){
            case WEATHER_TYPE_RAIN: {
                for(int i = 0; i < count; i++){
                    if(GameMain.viewHeight - data[3][i] > 0){
                        data[5][i] = (short) rand.nextInt(GameMain.viewHeight - data[3][i]);
                    }else{
                        data[5][i] = Short.MAX_VALUE;
                    }
                }

                dieData.removeAllElements();
            }
                break;
            case WEATHER_TYPE_SNOW: {
                dieData.removeAllElements();
            }
                break;
        }
    }
//#endif
}
