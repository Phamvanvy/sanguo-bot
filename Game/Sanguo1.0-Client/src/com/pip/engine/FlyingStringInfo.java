package com.pip.engine;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;

/**
 * 飞行文字信息
 * @author leo
 */

public class FlyingStringInfo{
    public byte type; //类型 0：数字，1：文字
    public String str; //文字数据
    public int number; //数字数据
    public int color; //颜色
    public int distance; //移动距离
    public int time; //持续时间
    public int order; //绘制顺序
    public int calculate; //计算数据
    public int delayTick; //延时播放的tick数
    
    public boolean isAcross; //是否测漂
    public int dir;          //漂字方向, 左-1， 右+1
    public int hCycleCount;  //测漂水平cycle记数
    public int hSpeed;       //测漂水平距离
    public int stopCycleCount; //中间停止的cycle记数
    public int vCycleCount;  //测漂水平cycle记数
    public int vSpeed;       //测漂水平距离
    
    public long lastProcessTime;
    
    public int _oldNumber = Integer.MAX_VALUE;
    public int[] numberArray = new int[0];

    public void drawFlying(Graphics _g, int _x, int _y, int _number, int _paletteColor, int _distance, int _percent, int _tick) {
        switch(this.type){
            case Tool.FLYING_STRING_TYPE_NUMBER:
                if(_oldNumber != _number){
                    numberArray = Tool.getNumberFrameArray(_number);
                    _oldNumber = _number;
                }
                Tool.drawFlyingNumber(_g, _x, _y, numberArray, this.color, _distance, _percent, _tick);
                break;
            case Tool.FLYING_STRING_TYPE_STRING:
                Tool.drawFlyingString(_g, _x, _y, this.str, this.color, _distance, _percent, _tick, false);
                break;
            case Tool.FLYING_STRING_TYPE_3DSTRING:
                Tool.drawFlyingString(_g, _x, _y, this.str, this.color, _distance, _percent, _tick, true);
                break;                        
        }
    }
    public boolean equals(Object obj){
        FlyingStringInfo other = (FlyingStringInfo)obj;
        
        if(other.type != Tool.FLYING_STRING_TYPE_NUMBER  && type == other.type){
            if(str != null && str.equals(other.str) && color == other.color){
                return true;
            }
        }
        
        return false;
    }
}
