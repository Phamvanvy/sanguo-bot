package com.pip.engine;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameRole;
import com.pip.sanguo.GameSprite;
import com.pip.sanguo.GameWorld;
import com.pip.util.SortHashtable;
import com.pip.util.VMCounter;


/**
 * 动画精灵类
 * 具有功能：
 * 1、支持相同帧序列动画组（用来处理诸如人物、马匹、武器等等帧序列匹配的动画）
 * 2、支持不同帧序列动画组（用来处理武器闪光、中毒特效等等帧序列不匹配的动画）
 * 
 * @author leo
 *
 */
public class Sprite{
    /**
     * AnimateData Vector，保存帧序列相同的动画组
     */
    private Vector animateList = new Vector();

    /**
     * AnimateName AnimateData Hashtable，保存所有动画资源，加速操作
     */
    private Hashtable animateTable = new Hashtable();

    /**
     * 精灵的x坐标
     */
    private int x = -1000; //默认值表示还未从server赋值过来

    /**
     * 精灵的y坐标
     */
    private int y;

    /**
     * 精灵当前的方向
     */
    private byte dir;
    
    /**
     * 动画的斜向朝向
     */
    private byte animateSubDir;
    
    /**
     * 动画方向
     */
    private byte animateDir;

    /**
     * 精灵移动的步长（单位为：像素/帧)
     */
    private short speed;
    
    /**
     * 速度附加改变（百分比）
     */
    private short speedAddon;

    /**
     * 是否在移动
     */
    private boolean moving;

    /**
     * 是否显示此精灵
     */
    private boolean showing;

    /**
     * 是否处于工作状态，true将会由系统调用cycle函数
     */
    private boolean working;

    /**
     * 是否需要进行碰撞检测
     */
    private boolean collision;

    /**
     * 动画显示区域，相对动画参考点的坐标
     * int[4] 0:x 1:y, 2:w, 3:h
     */
    private int[] animateBox = new int[4];
    
    /**
     * 头顶文字信息
     * 内容Object[2]，[0]String 文字，[1]Integer 颜色
     */
    private Vector headStrings = new Vector();

    /**
     * 头顶文字配置
     * int[6] [0]类型 0按动画显示大小来设置，1按x，y配置 [1]行间距 [2]绘画模式 0:3D, 1:普通 [3]offsetx偏移x [4]offsety偏移y [5]绘制顺序
     */
    private int[] headStringConfig = new int[6];

    /**
     * 是否显示头顶文字
     */
    private boolean headStringShow;
    
    /**
     * 头顶文字高度
     */
    private int headStringHeight;

    /**
     * 飘字信息
     * 内容int[5] [0]数值， [1]颜色（0红色，1绿色，2黄色，3蓝色）[2]移动距离 [3]总时间cycle数 [4]当前播放时间
     */
    private Vector flyingStrings = new Vector();
    
    private Vector flyingStringQueue = new Vector();

    /**
     * 抖动信息
     * 内容int[3] [0]dir, [1]总时间cycle数 [2]震动强度 [3]当前播放时间
     */
    private Vector vibras = new Vector();

    /**
     * 地图id
     */
    private int mapId = -1;
    private int mapInstanceId = -1;

    public int getMapInstanceId() {
		return mapInstanceId;
	}

	public void setMapInstanceId(int mapInstanceId) {
		this.mapInstanceId = mapInstanceId;
	}

	private int currentStep;
    private int currentStep100;
    private boolean playingAnimate;
    
    public int movedStep;
    
    //为了加速程序避免new int
    protected boolean needMakeAnimateBox = false;
    protected int[] realAnimateBox = new int[4];
    
    /**
     * 构造函数
     */
    public Sprite(GameSprite ownerSprite){
        wayPointInfo.ownerSprite = ownerSprite;
    }

    /**
     * 取得精灵方向
     * @return
     */
    public byte getDir(){
        return dir;
    }

    /**
     * 取得精灵x坐标
     * @return
     */
    public int getX(){
        return x;
    }

    /**
     * 取得精灵y坐标
     * @return
     */
    public int getY(){
        return y;
    }
    
    /**
     * 取得精灵的速度
     * @return
     */
    public int getSpeed(){
        return speed;
    }
    
    /**
     * 设置精灵的速度
     * @param speed
     */
    public void setSpeed(int speed){
        this.speed = (short)speed;
    }
    
    /**
     * 获得精灵速度附加值
     * @return
     */
    public int getSpeedAddon(){
        return speedAddon;
    }
    
    /**
     * 设置精灵速度附加值
     * @param speedAddon
     */
    public void setSpeedAddon(int speedAddon){
        this.speedAddon = (short)speedAddon;
        if(speedAddon <= -100){
        	speedAddon = - 99;
        }
    }

    /**
     * 设置是否显示头顶文字
     * @param show
     */
    public void setHeadStringShow(boolean show){
        headStringShow = show;
    }

    /**
     * 获得精灵的动画
     * @param animateName 动画名称
     * @return 
     */
    public AnimatePlayer getAnimatePlayer(String animateName) {
    	return (AnimatePlayer)animateTable.get(animateName);
    }
    
    /**
     * 为精灵添加动画资源
     * @param animateData  动画数据
     * @param sync 是否为同步动画
     */
    public void addAnimate(AnimatePlayer animateData){
        if(!animateTable.containsKey(animateData.getAnimateName())){
            animateList.addElement(animateData);
            animateTable.put(animateData.getAnimateName(), animateData);
        }
    }

    /**
     * 移除动画资源
     * @param animateName 动画资源名称
     */
    public void removeAnimate(String animateName){
        animateTable.remove(animateName);
        
        int count = animateList.size();
        int idx = -1;
        
        for(int i = 0; i < count; i++){
            AnimatePlayer animatePlayer = (AnimatePlayer)animateList.elementAt(i);
            
            if(animatePlayer.getAnimateName().equals(animateName)){
                idx = i;
                
                break;
            }
        }
        
        if(idx >= 0){
            animateList.removeElementAt(idx);
        }
    }

    /**
     * 返回所有动画的名称
     * @return
     */
    public Vector getAllAnimateNames(){
        Vector result = new Vector();
        Enumeration emu = animateTable.keys();

        while(emu.hasMoreElements()){
            result.addElement(emu.nextElement());
        }

        return result;
    }
    
    public boolean isHumanAnimate(String animateName){
        AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);
        
        if(tmp != null){
            return tmp.isHunmanAnimate();
        }
        
        return false;
    }

    public AnimatePlayer getIconAnimate(){
        AnimatePlayer result = null;

        for(int i = 0; i < animateList.size(); i++){
            AnimatePlayer animate = (AnimatePlayer)animateList.elementAt(i);

            if(animate.isShown() && !animate.isNotIcon()){
                result = animate;
                
                break;
            }
        }

        return result;
    }

    /**
     * 开启显示某一个动画资源
     * @param animateName 动画资源名称
     */
    public void showAnimate(String animateName){
        AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);

        if(tmp != null){
            tmp.setShown(true);
        }
    }

    /**
     * 关闭显示某一个动画资源
     * @param animateName 动画资源名称
     */
    public void hideAnimate(String animateName){
        AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);

        if(tmp != null){
            tmp.setShown(false);
        }
    }
    
    /**
     * 设置某一动画不能作为
     * @param animateName
     */
    public void setAnimateNotIcon(String animateName){
        AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);

        if(tmp != null){
            tmp.setNotIcon(true);
        }
    }
    
    public void setAnimateLayer(String animateName, int layer){
        AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);

        if(tmp != null){
            tmp.setLayer(layer);
        }
    }
    
    public void regroupAnimate(){
        int size = animateList.size();
        
        for(int i = 0; i < size - 1; i++){
            AnimatePlayer p1 = (AnimatePlayer)animateList.elementAt(i);
            
            for(int j = i + 1; j < size; j++){
                AnimatePlayer p2 = (AnimatePlayer)animateList.elementAt(j);
                
                if(p1.getLayer() > p2.getLayer()){
                    animateList.setElementAt(p1, j);
                    animateList.setElementAt(p2, i);
                    p1 = p2;
                }
            }
        }
    }

    public boolean hasAnimate(String animateName){
        return animateTable.get(animateName) != null;
    }

    /**
     * 设置某一个动画资源的当前显示动画，对于相同帧序列动画，设置其中的一个，会连带设置其他的
     * @param animateName 动画资源名称
     * @param index 动画index
     */
    public void setAnimateIndex(String animateName, int index, int playType, int callBackIndex, Object callBackParams, boolean whole){
        if(whole){
            int size = animateList.size();
            
            for(int i = 0; i < size; i++){
                AnimatePlayer tmp = (AnimatePlayer)animateList.elementAt(i);
                tmp.setAnimate(index, playType, callBackIndex, (IAnimateCallback)callBackParams);
            }
        }else{
            AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);
            
            if(tmp != null){
                tmp.setAnimate(index, playType, callBackIndex, (IAnimateCallback)callBackParams);
            }
        }

        /**
         * 由于更换动画序列可能会改变sprite的大小，清除现在的碰撞检测区域，以便重新计算
         */
        needMakeAnimateBox = true;
    }

    /**
     * 设置精灵位置
     * @param x 坐标x
     * @param y 坐标y
     */
    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * 返回精灵当前方向和位置信息
     * @param bodyCount 返回距离精灵几个身位的位置
     * @return int[3] 0:dir 1:x 2:y
     */
    public int[] getPosition(int boxCount){
        if(needMakeAnimateBox){
            makeAnimateBox();
        }

        int px = x + Tool.calulateStepWithBackMatrix(Tool.X_AXIS, dir, animateBox[2]) * boxCount;
        int py = y + Tool.calulateStepWithBackMatrix(Tool.Y_AXIS, dir, animateBox[3]) * boxCount;

        return new int[]{
                        dir, px, py
        };
    }

    /**
     * 设置方向
     * @param dir
     */
    public void setDir(int dir){
        this.dir = (byte)dir;
    }

    /**
     * 获得动画方向
     * @return
     */
    public int getAnimateDir(){
        return animateDir;
    }
    
    /**
     * 设置动画方向
     * @param animateDir
     */
    public void setAnimateDir(int animateDir){
        this.animateDir = (byte)animateDir;
    }
    
    /**
     * 获得动画的斜向朝向
     * @return
     */
    public int getAnimateSubDir(){
        return animateSubDir;
    }
    
    /**
     * 设置动画的斜向朝向
     * @param subDir
     */
    public void setAnimateSubDir(int subDir){
        this.animateSubDir = (byte)subDir;
    }

    /**
     * 设置是否移动
     */
    public void setMove(boolean moving){
        this.moving = moving;
    }

    /**
     * 取得是否在移动
     * @return
     */
    public boolean getMove(){
        return moving;
    }

    /**
     * 设置是否显示
     * @param showing
     */
    public void setShow(boolean showing){
        this.showing = showing;
    }

    /**
     * 设置是否处于工作状态
     * @param working
     */
    public void setWork(boolean working){
        this.working = working;
    }

    /**
     * 设置是否需要进行碰撞检测
     * @param collision
     */
    public void setCollision(boolean collision){
        this.collision = collision;
    }
    
    /**
     * 设置头顶文字配置
     * @param config
     */
    public void setHeadStringConfig(int[] config){
        headStringConfig = config;
    }

    /**
     * 添加头顶文字信息
     * @param str 信息
     * @param color 信息颜色
     * @param image 附加图片
     * @param imageIndexs 图片索引数组
     */
    public void addHeadString(String str, int color, ImageSet image, int[] imageIndex){
        headStrings.addElement(new Object[]{
                        str, new Integer(color), image, imageIndex
        });
    }

    /**
     * 清空头顶文字信息
     */
    public void clearHeadString(){
        headStrings.removeAllElements();
    }
    
    public int getHeadStringHeight(){
        return headStringHeight;
    }

    /**
     * 设置动画图片的drawOffset
     * @param animateName
     * @param imageId
     * @param index
     * @param replaceImageId
     * @param replaceOffset
     */
    public void setAnimateImageDrawOffset(String animateName, int imageId, int index, int replaceImageId, int replaceOffset){
        AnimatePlayer tmp = new AnimatePlayer(animateName);

        tmp = (AnimatePlayer)animateTable.get(animateName);

        if(tmp != null){
            tmp.setAnimateImageDrawOffset(imageId, index, replaceImageId, replaceOffset);
        }
    }

    /**
     * 添加可替换图片资源
     * @param animateName
     * @param imageId
     * @param imageName
     */
    public void addReplaceAnimateImage(String animateName, String imageName){
        AnimatePlayer tmp = (AnimatePlayer)animateTable.get(animateName);

        if(tmp != null){
            tmp.addReplaceAnimateImage(imageName);
        }
    }

    /**
     * 添加飘字
     * @param number 数字
     * @param paletteColor 调色板颜色
     * @param distance 移动距离
     * @param time 持续时间
     */
    public void addFlyingString(byte type, String str, int number, int paletteColor, int distance, int time, int order, int delayTick){
        FlyingStringInfo fly = new FlyingStringInfo();
        fly.type = type;
        
        if(fly.type == 0){
            fly.number = number;
        }else{
            fly.str = str;
        }
        
        fly.color = paletteColor;
        fly.distance = distance;
        fly.time = time;
        fly.order = order;
        fly.delayTick = delayTick;

        addFlyingData(fly);
    }
    
    /**
     * 添加侧向飘字
     * @param number 数字
     * @param paletteColor 调色板颜色
     * @param distance 移动距离
     * @param time 持续时间
     */
    public void addFlyingString(byte type, String str, int number, int paletteColor, int dir, int hCycleCount, int hSpeed, int stopCycleCount, int vCycleCount, int vSpeed, int order, int delayTick){
        FlyingStringInfo fly = new FlyingStringInfo();
        fly.type = type;
        
        if(fly.type == 0){
            fly.number = number;
        }else{
            fly.str = str;
        }
        
        fly.dir = dir;
        fly.color = paletteColor;
        fly.hCycleCount = hCycleCount;
        fly.hSpeed = hSpeed;
        fly.stopCycleCount = stopCycleCount;
        fly.vCycleCount = vCycleCount;
        fly.vSpeed = vSpeed;
        fly.order = order;
        fly.isAcross = true;
        fly.time = hCycleCount + vCycleCount + stopCycleCount;
        fly.delayTick = delayTick;
        
        addFlyingData(fly);
    }
    
    private void addFlyingData(FlyingStringInfo fly){
        boolean found = false;
        int count = flyingStrings.size();
        
        for(int i = 0; i < count; i++){
            FlyingStringInfo tmp = (FlyingStringInfo)flyingStrings.elementAt(i);
            
            if(tmp.equals(fly)){
                found = true;
                
                break;
            }
        }
        
        count = flyingStringQueue.size();
        
        for(int i = 0; i < count; i++){
            FlyingStringInfo tmp = (FlyingStringInfo)((Object[])flyingStringQueue.elementAt(i))[0];
            
            if(tmp.equals(fly)){
                found = true;
                
                break;
            }
        }
        
        if(!found){
            flyingStringQueue.addElement(new Object[]{
                            fly, new Integer(GameMain.spriteFlyingStringDelay)
            });
        }
    }
    
	
    /*
	 * 头顶泡泡
	 */
	private int bubbleCounterKey = -1;	//气泡计时器
	public String[] bubbleBuffer = null;	//
	public static final int RES_UI_TIP_ARROW = 13; //提示框箭头
	public static final int RES_UI_TIP_TOPRIGHT = 14; //提示框右上
	
	
	/**
	 * 添加泡泡
	 * @param text	显示内容
	 * @param time	显示时间
	 */
	public void addBubble(String text,int time){
		bubbleBuffer = null;
		bubbleBuffer = Tool.splitString(text, '\n');
		if(bubbleCounterKey != -1){
			VMCounter.removeVMCounter(bubbleCounterKey);
		}
		bubbleCounterKey = VMCounter.createVMCounter(time);
	}
	
	public void drawBubble(Graphics g, int screenX, int screenY, boolean isTop){
        if(needMakeAnimateBox){
            makeAnimateBox();
        }
        GameWorld.gameView.addPendingBubble(bubbleBuffer, screenX, screenY - animateBox[3] - headStrings.size() * Utilities.CHAR_HEIGHT, isTop);
    }

    public void addVibar(int dir, int time, int distance){
        int[] tmp = new int[4];

        tmp[0] = dir;
        tmp[1] = time;
        tmp[2] = distance;
        tmp[3] = 0;

        vibras.addElement(tmp);
    }

    private void drawHeadString(Graphics g, int screenX, int screenY, boolean pending, boolean isTop){
         if(headStringShow){
             if(needMakeAnimateBox){
                 makeAnimateBox();
             }
             
            int size = headStrings.size();

            int headx = screenX;
            int heady = screenY;

            switch(headStringConfig[0]){
                case Tool.HEAD_STRING_TYPE_DRAWAREA:
                    headx = animateBox[2] / 2 + screenX + animateBox[0] + headStringConfig[3];
                    heady = animateBox[1] + screenY + headStringConfig[4];

                    break;
                case Tool.HEAD_STRING_TYPE_ABSOLUTE:
                    headx = screenX + headStringConfig[3];
                    heady = screenY + headStringConfig[4];

                    break;
                case Tool.HEAD_STRING_TYPE_XAREA:
                    headx = screenX + headStringConfig[3];
                    heady = animateBox[1] + screenY + headStringConfig[4];
                    
                    break;
            }

            for(int i = 0; i < size; i++){
                Object[] tmp = (Object[])headStrings.elementAt(i);
                String str = (String)tmp[0];
                int color = ((Integer)tmp[1]).intValue();
                ImageSet strImage = (ImageSet)tmp[2];
                int[] imgIndexs = (int[])tmp[3];
                
                int subx = headx - Utilities.font.stringWidth(str) / 2;
                int line =  Utilities.CHAR_HEIGHT;
                
                if(strImage != null){
                    for(int j = 0; j < imgIndexs.length; j++){
                        int index1 = imgIndexs[j] >> 16;
                        int index2 = imgIndexs[j] & 0xFFFF;
                        
                        subx -= strImage.getFrameWidth(index2);
                        line = Math.max(Utilities.CHAR_HEIGHT, strImage.getFrameHeight(index2));
                        
                        if(pending){
                            if(index1 >= 0){
                                GameWorld.gameView.addPendingImage(strImage, index1, subx, heady - line / 2, Graphics.VCENTER | Graphics.LEFT, isTop);
                            }
                            
                            GameWorld.gameView.addPendingImage(strImage, index2, subx, heady - line / 2, Graphics.VCENTER | Graphics.LEFT, isTop);
                        }else{
                            if(index1 >= 0){
                                strImage.drawFrame(g, index1, subx, heady - line / 2, 0, Graphics.VCENTER | Graphics.LEFT);
                            }
                            
                            strImage.drawFrame(g, index2, subx, heady - line / 2, 0, Graphics.VCENTER | Graphics.LEFT);
                        }
                    }
                    
                     subx = headx - Utilities.font.stringWidth(str) / 2;
                }
                
                switch(headStringConfig[2]){
                    case Tool.DRAW_STRING_3D:
                        if(pending){
                            GameWorld.gameView.addPendingHeadString(str, subx, heady, color, 0x0, Graphics.BOTTOM | Graphics.LEFT, true, isTop);
                        }else{
                            Tool.draw3DString(g, str, subx, heady, color, 0x0, Graphics.BOTTOM | Graphics.LEFT);
                        }

                        break;
                    case Tool.DRAW_STRING_NORMAL:
                        if(pending){
                            GameWorld.gameView.addPendingHeadString(str, subx, heady, color, 0x0, Graphics.BOTTOM | Graphics.LEFT, false, isTop);
                        }else{
                            g.setColor(color);
                            Tool.drawString(g, str, subx, heady, Graphics.BOTTOM | Graphics.LEFT);
                        }

                        break;
                }

                heady -= line + headStringConfig[1];
            }
            
            headStringHeight = screenY + animateBox[1] - heady;
        }else{
            headStringHeight = 0;
        }
    }
    
    private void drawFlyString(Graphics g, int screenX, int screenY, boolean isBack){
        int size = flyingStrings.size();

        Vector remainFlyingNumbers = new Vector();

        for(int i = 0; i < size; i++){
            FlyingStringInfo fly = (FlyingStringInfo)flyingStrings.elementAt(i);
            int flyX = screenX;
            int flyY = screenY;
            
            if(fly.delayTick > 0){
                remainFlyingNumbers.addElement(fly);
                continue;
            }
            
            if(isBack){
                if(fly.order != Tool.DRAW_ORDER_BACK){
                    remainFlyingNumbers.addElement(fly);
                    continue;
                }
            }
            
            if(fly.order == Tool.DRAW_ORDER_TOP){
                GameWorld.gameView.addPendingFlyString(fly, flyX, flyY, false);
            }else if(fly.order == Tool.DRAW_ORDER_TOP_TOP){
                GameWorld.gameView.addPendingFlyString(fly, flyX, flyY, true);
            }else{
            	if(fly.isAcross) {
            		//计算侧漂轨迹
            		if(fly.calculate <= fly.hCycleCount) {            			
            			flyX = flyX + fly.hSpeed * fly.calculate * fly.dir;
            			flyY = flyY - fly.hSpeed * fly.calculate;
            			
            			fly.drawFlying(g, flyX, flyY, fly.number, fly.color, 0, 0, 0);
            		} else if(fly.calculate - fly.hCycleCount < fly.stopCycleCount) {            			
            			flyX = flyX + fly.hSpeed * fly.hCycleCount * fly.dir;
            			flyY = flyY - fly.hSpeed * fly.hCycleCount;
            			fly.drawFlying(g, flyX, flyY, fly.number, fly.color, 0, 0, 0);
            		} else{
            			int vCycleCount = fly.calculate - fly.hCycleCount - fly.stopCycleCount;
            			flyX = flyX + fly.hSpeed * fly.hCycleCount * fly.dir;
            			flyY = flyY - fly.hSpeed * fly.hCycleCount - fly.vSpeed * vCycleCount;
            			
            			fly.drawFlying(g, flyX, flyY, fly.number, fly.color, 0, 0, 0);
            		}
            		
            	} else {
            		fly.drawFlying(g, flyX, flyY, fly.number, fly.color, fly.distance, fly.calculate * 100 / fly.time, fly.calculate);
            	}
            }
            
            if(fly.calculate < fly.time){
                remainFlyingNumbers.addElement(fly);
            }
        }

        flyingStrings = remainFlyingNumbers;
    }
    
    
    public static int HP_BAR_WIDTH = 20;
    public static int HP_BAR_HEIGHT = 3;
    private static int needNotDrawHpMp = -1;
    private int isMyAttendant = -1;
    /**
     * 绘制精灵
     * @param g
     */
    public void draw(Graphics g, int viewX, int viewY){
        if(showing){
            int size;

            int screenX = x - viewX;
            int screenY = y - viewY;

            int vx = screenX;
            int vy = screenY;

            size = vibras.size();

            if(size > 0){
                int[] vibra = (int[])vibras.firstElement();

                vx += Tool.calulateOffsetWithVibraMatrix(Tool.X_AXIS, vibra[0], vibra[3]) * vibra[2];
                vy += Tool.calulateOffsetWithVibraMatrix(Tool.Y_AXIS, vibra[0], vibra[3]) * vibra[2];

                vibra[3]++;

                if(vibra[3] >= vibra[1]){
                    vibras.removeElementAt(0);
                }
            }

            if(headStringConfig[5] == Tool.DRAW_ORDER_BACK){
                drawHeadString(g, screenX, screenY, false, false);
            }
            
            drawFlyString(g, screenX, screenY, true);
            
            size = animateList.size();

            for(int i = 0; i < size; i++){
                AnimatePlayer tmp = (AnimatePlayer)animateList.elementAt(i);
                tmp.draw(g, vx, vy);
            }

            if(headStringConfig[5] == Tool.DRAW_ORDER_FRONT){
                drawHeadString(g, screenX, screenY, false, false);
            }else if(headStringConfig[5] == Tool.DRAW_ORDER_TOP){
                drawHeadString(g, screenX, screenY, true, false);
            }else{
                drawHeadString(g, screenX, screenY, true, true);
            }
            
            drawFlyString(g, screenX, screenY, false);
            //气泡
            if(bubbleBuffer != null){
            	drawBubble(g, screenX, screenY, true);
            }
            //随从血蓝
            drawAttendantHpMp(g,screenX,screenY);
        }
    }
    
    private void drawAttendantHpMp(Graphics g,int screenX,int screenY){
    	GameSprite gs = wayPointInfo.ownerSprite;
        if(gs.getType() != Tool.SPRITE_TYPE_ATTENDANT){
        	return;
        }
    	if(needNotDrawHpMp == -1){
    		needNotDrawHpMp = Tool.getGlobalInt("NEED_DRAW_HP_MP_2011");
    	}
    	
    	if(needNotDrawHpMp == 1){
    		return;
    	}
    	
    	if(isMyAttendant == -1){
    		SortHashtable sht = (SortHashtable)GameWorld.instance.readGameData("vmDataAttendantBag");
    		if(sht == null){
    			return;
    		}
    		if(sht.get(new Integer(gs.getInstanceId())) == null){
    			isMyAttendant = 0;
    		} else {
    			isMyAttendant = 1;
    		}
    	}
    	
    	if(isMyAttendant == 0){
    		return;
    	}
    	
    	if(GameMain.viewWidth >= 480){
    		HP_BAR_WIDTH = 40;
    		HP_BAR_HEIGHT = 5;
    	}
    	
    	if(needMakeAnimateBox){
    		makeAnimateBox();
    	}
    	
    	int hpX = screenX - (HP_BAR_WIDTH >> 1);
//    	int hpY = screenY - animateBox[3] - Utilities.font.getHeight() - 4;
    	int hpY = screenY - animateBox[3] - headStringConfig[4] - Utilities.CHAR_HEIGHT - ((HP_BAR_HEIGHT + 2) << 1);
    	//血条
    	g.setColor(0xffffff);
    	g.fillRect(hpX,hpY,HP_BAR_WIDTH,HP_BAR_HEIGHT);
    	g.setColor(0x222222);
    	g.drawRect(hpX,hpY,HP_BAR_WIDTH,HP_BAR_HEIGHT);
    	g.setColor(0xff0000);
    	if(gs.hpMax != 0){
    		if(GameMain.viewWidth >= 480){
    			g.setColor(0xff855b);
        		g.drawLine(hpX + 1, hpY + 1, hpX + 1 + gs.hp * (HP_BAR_WIDTH - 2) / gs.hpMax, hpY + 1);
        		g.setColor(0xff3919);
        		g.drawLine(hpX + 1, hpY + 2, hpX + 1 + gs.hp * (HP_BAR_WIDTH - 2) / gs.hpMax, hpY + 2);
        		g.setColor(0xb10000);
        		g.drawLine(hpX + 1, hpY + 3, hpX + 1 + gs.hp * (HP_BAR_WIDTH - 2) / gs.hpMax, hpY + 3);
        		g.setColor(0xcf0058);
        		g.drawLine(hpX + 1, hpY + 4, hpX + 1 + gs.hp * (HP_BAR_WIDTH - 2) / gs.hpMax, hpY + 4);
    		} else {
    			g.setColor(0xff4200);
        		g.drawLine(hpX + 1, hpY + 1, hpX + 1 + gs.hp * (HP_BAR_WIDTH - 2) / gs.hpMax, hpY + 1);
        		g.setColor(0xbc0017);
        		g.drawLine(hpX + 1, hpY + 2, hpX + 1 + gs.hp * (HP_BAR_WIDTH - 2) / gs.hpMax, hpY + 2);
    		}
    	}
    	//蓝条
    	g.setColor(0xffffff);
    	g.fillRect(hpX,hpY + HP_BAR_HEIGHT + 2,HP_BAR_WIDTH,HP_BAR_HEIGHT);
    	g.setColor(0x0);
    	g.drawRect(hpX,hpY + HP_BAR_HEIGHT + 2,HP_BAR_WIDTH,HP_BAR_HEIGHT);
    	g.setColor(0x0000ff);
    	if(gs.mpMax != 0){
    		if(GameMain.viewWidth >= 480){
    			g.setColor(0x57e1ff);
        		g.drawLine(hpX + 1, hpY + HP_BAR_HEIGHT + 3, hpX + 1 + gs.mp * (HP_BAR_WIDTH - 2) / gs.mpMax, hpY + HP_BAR_HEIGHT + 3);
        		g.setColor(0x1c9cff);
        		g.drawLine(hpX + 1, hpY + HP_BAR_HEIGHT + 4, hpX + 1 + gs.mp * (HP_BAR_WIDTH - 2) / gs.mpMax, hpY + HP_BAR_HEIGHT + 4);
        		g.setColor(0x0051d2);
        		g.drawLine(hpX + 1, hpY + HP_BAR_HEIGHT + 5, hpX + 1 + gs.mp * (HP_BAR_WIDTH - 2) / gs.mpMax, hpY + HP_BAR_HEIGHT + 5);
        		g.setColor(0x00a2ff);
        		g.drawLine(hpX + 1, hpY + HP_BAR_HEIGHT + 6, hpX + 1 + gs.mp * (HP_BAR_WIDTH - 2) / gs.mpMax, hpY + HP_BAR_HEIGHT + 6);
    		} else {
    			g.setColor(0x00ccff);
        		g.drawLine(hpX + 1, hpY + HP_BAR_HEIGHT + 3, hpX + 1 + gs.mp * (HP_BAR_WIDTH - 2) / gs.mpMax, hpY + HP_BAR_HEIGHT + 3);
        		g.setColor(0x00a2ff);
        		g.drawLine(hpX + 1, hpY + HP_BAR_HEIGHT + 4, hpX + 1 + gs.mp * (HP_BAR_WIDTH - 2) / gs.mpMax, hpY + HP_BAR_HEIGHT + 4);
    		}
    	}
    }

    /**
     * 驱动函数
     */
    public void cycle(){
        if(working){
            /**
             * 处理移动信息
             */
            if(moving || wayPointInfo.needHandle){
            	if(wayPointInfo.needHandle) {
            		if (wayPointInfo.ownerSprite.getType() == Tool.SPRITE_TYPE_ROLE) {
            			calculateStep();
            		}
            		wayPointInfo.processWayPoint();
            	} else {
            	    calculateStep();
            	   //#if ModelID == Android || ModelID == AndroidLarge
            	  //#  if(Tool.getGlobalInt("InsideDirRectKey") == 1)
            	  //#   {
            	  //#   handleMove_Android(); 	
            	  //#   }
            	  //#   else
            	  //#   {
            	  //#   handleMoveNew();	
            	  //#   }
            	   //#elif ModelID == AndroidAuto
            	  //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) || GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
            	  //# {
            	  //# if(Tool.getGlobalInt("InsideDirRectKey") == 1)
            	  //# {
            	  //# handleMove_Android();
            	  //# }
            	  //# else
            	  //# {
            	  //# handleMoveNew();	
            	  //# }
//#             		
            	  //# }
            	  //# else
            	  //# {
            	  //# handleMove();	
            	  //# }
            	   //#else
          		  handleMove();
            	   //#endif 
            	}            	
            }
        }
        
        /**
         * 处理动画信息
         */
        playingAnimate = false;
        int size;
        size = animateList.size();

        for(int i = 0; i < size; i++){
            AnimatePlayer tmp = (AnimatePlayer)animateList.elementAt(i);
            tmp.cycle();
            
            if(tmp.playing()){
                playingAnimate = true;
            }
        }
        
        size = flyingStrings.size();
        
        if(size > 0){
            Vector remainFlyingString = new Vector();
            long now = Tool.getSystemTime();

            for(int i = 0; i < size; i++){
                FlyingStringInfo fly = (FlyingStringInfo)flyingStrings.elementAt(i);
                
                if(fly.delayTick > 0){
                    fly.delayTick--;
                    remainFlyingString.addElement(fly);
                    continue;
                }
                
                fly.calculate++;
                
                if(now - fly.lastProcessTime < GameMain.dropflyingStringTime){
                    remainFlyingString.addElement(fly);
                }
            }
            
            flyingStrings = remainFlyingString;
        }
        
        size = flyingStringQueue.size();
        
        if(size > 0){
            Object[] tmpData = (Object[])flyingStringQueue.firstElement();
            int delayTick = ((Integer)tmpData[1]).intValue();
            delayTick--;
            
            if(delayTick <= 0){
                flyingStringQueue.removeElementAt(0);
                FlyingStringInfo fly = (FlyingStringInfo)tmpData[0];
                fly.lastProcessTime = Tool.getSystemTime();
                flyingStrings.addElement(fly);
            }else{
                tmpData[1] = new Integer(delayTick);
            }
        }
        //更新泡泡
        if(bubbleCounterKey != -1){
        	int restTime = VMCounter.getSaveTimeMillis(bubbleCounterKey);
        	if(restTime <= 0){
        		bubbleCounterKey = -1;
        		bubbleBuffer = null;
        	}
        }
    }
    
    /**
     * 返回精灵是否在播放动画中，或有飘字
     * @return
     */
    public boolean isPlayingAnimate(){
        return playingAnimate || (flyingStrings.size() != 0);
    }

    /**
     * 返回精灵的碰撞检测矩形
     * @return int[] [0]x [1]y [2]w [3]h
     */
    public int[] getCollisionBox(int[] box, boolean useAnimateWidth){
        if(needMakeAnimateBox){
            makeAnimateBox();
        }
        
//        int useWidth = Tool.DEFAULT_TILE_WIDTH;
//        int useHeight = Tool.DEFAULT_TILE_HEIGHT;
//        
//        if(GameWorld.gameView != null){
//            useWidth = GameWorld.gameView.tileWidth;
//            useHeight = GameWorld.gameView.tileHeight;
//        }
        //#if ModelID == AndroidAuto
        //# int useWidth = 0;
        //# int useHeight = 0;
        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
        //# useWidth = 18;
        //# useHeight = 18;
        //# }
        //# else
        //# {
        //# useWidth = 10;
        //# useHeight = 8;
        //# }
        //#elif DoubleScreen == true
        //# int useWidth = 16;
        //# int useHeight = 16;
        //#else
        int useWidth = 8;
        int useHeight = 8;
        //#endif
        
        if(useAnimateWidth){
            useWidth = animateBox[2];
        }
        
        box[0] = x - useWidth / 2;
        box[1] = y - useHeight / 4;
        box[2] = useWidth;
        box[3] = useHeight / 2;

        return box;
    }

    /**
     * 返回精灵在游戏世界显示动画所占的矩形
     * @return int[] [0]x [1]y [2]w [3]h
     */
    public int[] getAnimateBox(){
        if(needMakeAnimateBox){
            makeAnimateBox();
        }

        realAnimateBox[0] = animateBox[0] + x;
        realAnimateBox[1] = animateBox[1] + y;
        realAnimateBox[2] = animateBox[2];
        realAnimateBox[3] = animateBox[3];

        return realAnimateBox;
    }

    /**
     * 重算cycle移动步长
     */
    private void calculateStep(){
        currentStep100 += (speed * (100 + speedAddon) * GameMain.averageMillis / 1000);
        currentStep = currentStep100 / 100;
        currentStep100 -= currentStep * 100;
        GameMain.clientMoving = true;  
    }
    
    /**
     * 重建精灵动画显示区域矩形
     */
    private void makeAnimateBox(){
        animateBox[0] = 0;
        animateBox[1] = 0;
        animateBox[2] = 0;
        animateBox[3] = 0;

        int size = animateList.size();

        for(int i = 0; i < size; i++){
            AnimatePlayer tmp = (AnimatePlayer)animateList.elementAt(i);

            if(tmp.isShown()){
                Tool.mergeBox(animateBox, tmp.getDrawArea());
            }
        }
        
        needMakeAnimateBox = false;
    }

    /**
     * 处理精灵的移动并处理碰撞检测
     */
  //#if ModelID == AndroidAuto || ModelID == Android || ModelID == AndroidLarge
    //# public static final String VAR_DIRECT_KEY = "varGamePanelDirectKey";
    //# public static final String VAR_DIRECT_KEY_POINT = "varGamePanelDirectKeyPointIndex";
    //# public static final int DIRECT_KEY_R = 7;
    //# public static final int DIRECT_KEY_RND_X = 8;
    //# public static final int DIRECT_KEY_RND_Y = 9;
  //#   
    //# //划分平面用到的几个三角函数值
    //# public static final double tan1_8Pi = 0.41421356237309503;	//tan(Pi * 1 / 8)
    //# public static final double tan3_8Pi = 2.414213562373095;		//tan(Pi * 3 / 8)
    //# public static final double tan5_8Pi = -2.4142135623730954;	//tan(Pi * 5 / 8)
    //# public static final double tan7_8Pi = -0.41421356237309515;	//tan(Pi * 7 / 8)
  //#	
    //# public static final double cot1_8Pi = 2.414213562373095;		//cot(Pi * 1 / 8)
    //# public static final double cot3_8Pi = 0.41421356237309503;	//cot(Pi * 3 / 8)
    //# public static final double cot5_8Pi = -0.41421356237309515;	//cot(Pi * 5 / 8)
    //# public static final double cot7_8Pi = -2.4142135623730954;	//cot(Pi * 7 / 8)
  //#   
    //# public static final double sin1_4Pi = 0.7071067811865475;	//sin(Pi * 1 / 4)
    //# public static final double cos1_4Pi = 0.7071067811865476;	//sin(Pi * 1 / 4)
  //#   
    //# public static final double sqrt2 = 1.4142135623730951;		//sqrt2
  //#   
  //#
  //#    
     //# private int[] collisionBox = new int[4];
//#     
    //#if NewUI
      //# private int coordinatesToDir(int px,int py,int rx,int ry){
      	//# if(py >= px - rx + ry && py >= 0 - px + rx + ry && px <= py + rx - ry && px >= 0 - py + rx + ry){
  			//# return Tool.DIR_DOWN;
  		//# } else if(py <= px - rx + ry && py <= 0 - px + rx + ry && px >= py + rx - ry && px <= 0 - py + rx + ry){
  			//# return Tool.DIR_UP;
  		//# } else if(py > px - rx + ry && py < 0 - px + rx + ry && px < py + rx - ry && px < 0 - py + rx + ry){
  			//# return Tool.DIR_LEFT;
  		//# } else if(py < px - rx + ry && py > 0 - px + rx + ry && px > py + rx - ry && px > 0 - py + rx + ry){
  			//# return Tool.DIR_RIGHT;
  		//# } else {
  			//# return Tool.DIR_DOWN;
  		//# }
      //# }
  //# //新界面8方向行走方式
  //# private void handleMove_Android(){
        //# if(!GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) && !GameMain.getUIModel().equals(GameMain.ANDROID_LARGE)){
        	//# return;
        //# }
  //#       	
     	//# int xx, yy, realStep;
     	//# int spritex = x;
     	//# int spritey = y;
     	//# int spritew;
     	//# int spriteh;
  //#    	   
     	//# int px = 0;
     	//# int py = 0;
//#if NewUI2
  //#     int pointIndex = Tool.getGlobalInt(VAR_DIRECT_KEY_POINT);
  //#  	if(GameMain.GetPressedFlag(pointIndex)){
  //#  	   px = GameMain.GetPointPressX(pointIndex);
  //#  	   py = GameMain.GetPointPressY(pointIndex);
  //#  	}
  //#  	if(GameMain.GetDraggedFlag(pointIndex)){
  //#  		px = GameMain.GetPointDragX(pointIndex);
  //#  		py = GameMain.GetPointDragY(pointIndex);
  //#  	}
//#else
     	//# if(GameMain.GetPressedFlag()){
     	   //# px = GameMain.GetPointPressX();
     	   //# py = GameMain.GetPointPressY();
     	//# }
     	//# if(GameMain.GetDraggedFlag()){
     		//# px = GameMain.GetPointDragX();
     		//# py = GameMain.GetPointDragY();
     	//# }
    
//#endif
     	//# double sinA  = 0;
     	//# double cosA = 1;
     	//# int[] directKey = (int[])Tool.getGlobalObject(VAR_DIRECT_KEY);
  //#    	
     	//# //导航球圆心坐标
     	//# int rx = directKey[DIRECT_KEY_RND_X];
     	//# int ry = directKey[DIRECT_KEY_RND_Y];
  //#    	
     	//# //判定点落在那个区域
     	//# //划分空间的四条直线
     	//# //设x0,y0为圆心坐标,n = 1,3,5,7:
     	//# // y = tan(n/8Pi) * (x - x0) + y0 || x = cot(n/8Pi) * (y - y0)  + x0
     	//# if(py >= tan3_8Pi * (px - rx) + ry && py > tan5_8Pi * (px - rx) + ry && px <= cot3_8Pi * (py - ry) + rx && px > cot5_8Pi * (py - ry) + rx){//正下
     		//# sinA = 1;
     		//# cosA = 0;
     	//# } else if(py <= tan5_8Pi * (px - rx) + ry && py > tan7_8Pi * (px - rx) + ry && px <= cot5_8Pi * (py - ry) + rx && px > cot7_8Pi * (py - ry) + rx){//左下
     		//# sinA = sin1_4Pi;
     		//# cosA = -cos1_4Pi;
     	//# } else if(py <= tan7_8Pi * (px - rx) + ry && py > tan1_8Pi * (px - rx) + ry && px <= cot7_8Pi * (py - ry) + rx && px < cot1_8Pi * (py - ry) + rx){//正左
     		//# sinA = 0;
     		//# cosA = -1;
     	//# } else if(py <= tan1_8Pi * (px - rx) + ry && py > tan3_8Pi * (px - rx) + ry && px >= cot1_8Pi * (py - ry) + rx && px < cot3_8Pi * (py - ry) + rx){//左上
     		//# sinA = -sin1_4Pi;
     		//# cosA = -cos1_4Pi;
     	//# } else if(py <= tan3_8Pi * (px - rx) + ry && py < tan5_8Pi * (px - rx) + ry && px >= cot3_8Pi * (py - ry) + rx && px < cot5_8Pi * (py - ry) + rx){//正上
     		//# sinA = -1;
     		//# cosA = 0;
     	//# } else if(py >= tan5_8Pi * (px - rx) + ry && py < tan7_8Pi * (px - rx) + ry && px >= cot5_8Pi * (py - ry) + rx && px < cot7_8Pi * (py - ry) + rx){//右上
     		//# sinA = -sin1_4Pi;
     		//# cosA = cos1_4Pi;
     	//# } else if(py >= tan7_8Pi * (px - rx) + ry && py < tan1_8Pi * (px - rx) + ry && px >= cot7_8Pi * (py - ry) + rx && px > cot1_8Pi * (py - ry) + rx){//正右
     		//# sinA = 0;
     		//# cosA = 1;
     	//# } else if(py >= tan1_8Pi * (px - rx) + ry && py < tan3_8Pi * (px - rx) + ry && px <= cot1_8Pi * (py - ry) + rx && px > cot3_8Pi * (py - ry) + rx){//右下
     		//# sinA = sin1_4Pi;
     		//# cosA = cos1_4Pi;
     	//# }
  //#    		
  //#    		
  //#            
           //# if(collision){
               //# if(needMakeAnimateBox){
                   //# makeAnimateBox();
               //# }
  //#        
               //# collisionBox = getCollisionBox(collisionBox,false);
  //#        
                //# spritex = collisionBox[0];
                //# spritey = collisionBox[1];
                //# spritew = collisionBox[2];
                //# spriteh = collisionBox[3];
  //#                
                //# xx = (int)((currentStep * cosA) + spritex);
                //# yy = (int)((currentStep * sinA) + spritey);
                //# int newDir = coordinatesToDir(px, py, rx, ry);
                //# realStep = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newDir, currentStep, spritex, spritey);
  //#               
                //# if(realStep == 0){
  //#
                //# } else {
             	   //# DirectMove(cosA, sinA, realStep);
                //# }
            //# } else {
         	   //# DirectMove(cosA, sinA, currentStep);
            //# }
   //# }    
    //# //旧界面360°行走方式
    //#else
  //#    private void handleMove_Android(){
  //#    	if(!GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) && !GameMain.getUIModel().equals(GameMain.ANDROID_LARGE)){
  //#    		return;
  //#    	}
  //#        int xx, yy, realStep;
  //#        int spritex = x;
  //#        int spritey = y;
  //#        int spritew;
  //#        int spriteh;
  //#        
  //#        double k = 0.0;
  //#        int nX = 0;
  //#        int nY = 0;
  //#        if(GameMain.GetPressedFlag()){
  //#        	nX = GameMain.GetPointPressX();
  //#        	nY = GameMain.GetPointPressY();
  //#        }
  //#        if(GameMain.GetDraggedFlag()){
  //#        	nX = GameMain.GetPointDragX();
  //#            nY = GameMain.GetPointDragY();
  //#        }
  //#        double distance = 0;
  //#    	double sinA  = 0;
  //#        double cosA = 0;
  //#        if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL)){
  //#        	distance = Math.sqrt((GameMain.viewHeight - 61 - nY)*(GameMain.viewHeight - 61 - nY) + (nX - 61)*(nX - 61));
  //#        	sinA = (nY - (GameMain.viewHeight - 61)) / distance;
  //#        	cosA = (nX - (61)) / distance;
  //#    	} else {
  //#    		distance = Math.sqrt((GameMain.viewHeight - 98 - nY)*(GameMain.viewHeight - 98 - nY) + (nX - 98)*(nX - 98));
  //#     	   	sinA = (nY - (GameMain.viewHeight - 98)) / distance;
  //#     	   	cosA = (nX - (98)) / distance;   		   
  //#    	}
  //#        
  //#       if(collision){
  //#           if(needMakeAnimateBox){
  //#               makeAnimateBox();
  //#           }
  //#    
  //#           collisionBox = getCollisionBox(collisionBox,false);
  //#    
  //#            spritex = collisionBox[0];
  //#            spritey = collisionBox[1];
  //#            spritew = collisionBox[2];
  //#            spriteh = collisionBox[3];
  //#            
  //#            xx = (int)(currentStep*cosA*Math.sqrt(2)) + spritex;
  //#            yy = (int)(currentStep*sinA*Math.sqrt(2)) + spritey;
  //#            
  //#            realStep = GameWorld.collisionWorld(xx, yy, spritew, spriteh, dir, currentStep, spritex, spritey);
  //#    
  //#            if(realStep == 0){
  //#                for(int i = 1; i <= GameMain.COLLISION_MAX_STEP; i += GameMain.COLLISION_STEP_ADD){
  //#                    int newdir2 = dir, newdir3 = dir;
  //#                    int newRealStep2, newRealStep3;
  //#                    int newStep = currentStep * i;
  //#    
  //#                    switch(dir){
  //#                        case Tool.DIR_UP:
  //#                        case Tool.DIR_DOWN:
  //#                            newdir2 = Tool.DIR_LEFT;
  //#    
  //#                            break;
  //#                        case Tool.DIR_LEFT:
  //#                        case Tool.DIR_RIGHT:
  //#                            newdir2 = Tool.DIR_UP;
  //#    
  //#                            break;
  //#                    }
  //#    
  //#                    xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir2, newStep) + spritex;
  //#                    yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir2, newStep) + spritey;
  //#    
  //#                    newRealStep2 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir2, newStep, spritex, spritey);
  //#    
  //#                    switch(dir){
  //#                        case Tool.DIR_UP:
  //#                        case Tool.DIR_DOWN:
  //#                            newdir3 = Tool.DIR_RIGHT;
  //#    
  //#                            break;
  //#                        case Tool.DIR_LEFT:
  //#                        case Tool.DIR_RIGHT:
  //#                            newdir3 = Tool.DIR_DOWN;
  //#    
  //#                            break;
  //#                    }
  //#    
  //#                    xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir3, newStep) + spritex;
  //#                    yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir3, newStep) + spritey;
  //#    
  //#                    newRealStep3 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir3, newStep, spritex, spritey);
  //#    
  //#                    if(newRealStep2 != newRealStep3){
  //#                        if(newRealStep2 > newRealStep3){
  //#                            newRealStep2 = Math.min(newRealStep2, currentStep >> 1);
  //#                            doMove(newdir2, newRealStep2);
  //#                        }else{
  //#                            newRealStep3 = Math.min(newRealStep3, currentStep >> 1);
  //#                            doMove(newdir3, newRealStep3);
  //#                        }
  //#                        
  //#                        break;
  //#                    }else if(newRealStep2 == 0){
  //#                        break;
  //#                    }
  //#                }
  //#            }else{
  //#            	DirectMove(cosA, sinA, realStep);
  //#            }
  //#        }else{
  //#        	DirectMove(cosA, sinA, currentStep);
  //#        }
  //#     }
    //#endif
//#  
//#     
//#    
   //# private void DirectMove(double cosA, double sinA, int doStep){
    //# x += cosA*doStep;
    //# y += sinA*doStep;
	//# if (x < animateBox[2] >> 1) {
	//# x = animateBox[2] >> 1;
	//# } else if (x > GameWorld.gameView.map.width - animateBox[2] / 2) {
	//# x = GameWorld.gameView.map.width - animateBox[2] / 2;
	//# }
	//# if (y < animateBox[3] >> 1) {
	//# y = animateBox[3] >> 1;
	//# } else if (y >= GameWorld.gameView.map.height) {
	//# y = GameWorld.gameView.map.height;
	//# }
   //# }
   //# private void doMove(int doDir, int doStep){
        //# x += Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, doDir, doStep);
        //# y += Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, doDir, doStep);
   //# }
   //# private void handleMoveNew(){
   //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) || GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
//# {
    //# int xx, yy, realStep;
    //# int spritex = x;
    //# int spritey = y;
    //# int spritew;
    //# int spriteh;
   //# if(collision){
        //# if(needMakeAnimateBox){
           //# makeAnimateBox();
        //# }
        //# collisionBox = getCollisionBox(collisionBox,false);
        //# spritex = collisionBox[0];
        //# spritey = collisionBox[1];
        //# spritew = collisionBox[2];
        //# spriteh = collisionBox[3];
        //# xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, dir, currentStep) + spritex;
        //# yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, dir, currentStep) + spritey;
        //# realStep = GameWorld.collisionWorld(xx, yy, spritew, spriteh, dir, currentStep, spritex, spritey);
        //# if(realStep == 0){
            //# for(int i = 1; i <= GameMain.COLLISION_MAX_STEP; i += GameMain.COLLISION_STEP_ADD){
               //# int newdir2 = dir, newdir3 = dir;
               //# int newRealStep2, newRealStep3;
               //# int newStep = currentStep * i;
                 //# switch(dir){
                     //# case Tool.DIR_UP:
                     //# case Tool.DIR_DOWN:
                        //# newdir2 = Tool.DIR_LEFT;
                          //# break;
                     //# case Tool.DIR_LEFT:
                     //# case Tool.DIR_RIGHT:
                          //# newdir2 = Tool.DIR_UP;
                         //# break;
                 //# }
                  //# xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir2, newStep) + spritex;
                  //# yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir2, newStep) + spritey;
                  //# newRealStep2 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir2, newStep, spritex, spritey);
    	             //# switch(dir){
                       //# case Tool.DIR_UP:
                       //# case Tool.DIR_DOWN:
                           //# newdir3 = Tool.DIR_RIGHT;
                           //# break;
                       //# case Tool.DIR_LEFT:
                       //# case Tool.DIR_RIGHT:
                           //# newdir3 = Tool.DIR_DOWN;
                           //# break;
                   //# }
                    //# xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir3, newStep) + spritex;
                    //# yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir3, newStep) + spritey;
                    //# newRealStep3 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir3, newStep, spritex, spritey);
                    //# if(newRealStep2 != newRealStep3){
                        //# if(newRealStep2 > newRealStep3){
                            //# newRealStep2 = Math.min(newRealStep2, currentStep >> 1);
                            //# doMove(newdir2, newRealStep2);
                        //# }else{
                            //# newRealStep3 = Math.min(newRealStep3, currentStep >> 1);
                            //# doMove(newdir3, newRealStep3);
                        //# }
                        //# break;
                    //# }else if(newRealStep2 == 0){
                        //# break;
                    //# }
                //# }
            //# }else{
                //# doMove(dir, realStep);
            //# }
        //# }else{
             //# doMove(dir, currentStep);
          //# }
    //# }
   //# }
//#
   //# private void handleMove(){
    //# if (!GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL))
   	 //# {
       //# int xx, yy, realStep;
       //# int spritex = x;
       //# int spritey = y;
       //# int spritew;
       //# int spriteh;
//#        
//#        
      //# if(collision){
          //# if(needMakeAnimateBox){
              //# makeAnimateBox();
          //# }
//#
          //# collisionBox = getCollisionBox(collisionBox,false);
//#
           //# spritex = collisionBox[0];
           //# spritey = collisionBox[1];
           //# spritew = collisionBox[2];
           //# spriteh = collisionBox[3];
//#            
//#            
           //# xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, dir, currentStep) + spritex;
           //# yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, dir, currentStep) + spritey;
//#            
           //# realStep = GameWorld.collisionWorld(xx, yy, spritew, spriteh, dir, currentStep, spritex, spritey);
//#
           //# if(realStep == 0){
               //# for(int i = 1; i <= GameMain.COLLISION_MAX_STEP; i += GameMain.COLLISION_STEP_ADD){
                   //# int newdir2 = dir, newdir3 = dir;
                   //# int newRealStep2, newRealStep3;
                   //# int newStep = currentStep * i;
//#    
                    //# switch(dir){
                       //# case Tool.DIR_UP:
                       //# case Tool.DIR_DOWN:
                           //# newdir2 = Tool.DIR_LEFT;
//#    
                            //# break;
                       //# case Tool.DIR_LEFT:
                       //# case Tool.DIR_RIGHT:
                           //# newdir2 = Tool.DIR_UP;
//#    
                           //# break;
                   //# }
//#    
                   //# xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir2, newStep) + spritex;
                   //# yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir2, newStep) + spritey;
//#    
                   //# newRealStep2 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir2, newStep, spritex, spritey);
//#    
                   //# switch(dir){
                       //# case Tool.DIR_UP:
                       //# case Tool.DIR_DOWN:
                           //# newdir3 = Tool.DIR_RIGHT;
//#    
                           //# break;
                       //# case Tool.DIR_LEFT:
                       //# case Tool.DIR_RIGHT:
                           //# newdir3 = Tool.DIR_DOWN;
//#    
                           //# break;
                   //# }
//#    
                   //# xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir3, newStep) + spritex;
                   //# yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir3, newStep) + spritey;
//#    
                   //# newRealStep3 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir3, newStep, spritex, spritey);
//#
                   //# if(newRealStep2 != newRealStep3){
                       //# if(newRealStep2 > newRealStep3){
                           //# newRealStep2 = Math.min(newRealStep2, currentStep >> 1);
                           //# doMove(newdir2, newRealStep2);
                       //# }else{
                           //# newRealStep3 = Math.min(newRealStep3, currentStep >> 1);
                           //# doMove(newdir3, newRealStep3);
                       //# }
//#                        
                       //# break;
                   //# }else if(newRealStep2 == 0){
                       //# break;
                   //# }
               //# }
           //# }else{
               //# doMove(dir, realStep);
           //# }
       //# }else{
           //# doMove(dir, currentStep);
       //# }
     //# }
    //# }
    //#else
   private void handleMove(){
        int xx, yy, realStep;
        int spritex = x;
        int spritey = y;
        int spritew;
        int spriteh;
//#         
        int[] collisionBox = new int[4];
//#         
        if(collision){
            collisionBox = getCollisionBox(collisionBox, false);
//#    
            spritex = collisionBox[0];
            spritey = collisionBox[1];
            spritew = collisionBox[2];
            spriteh = collisionBox[3];
//#             
//#             
            xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, dir, currentStep) + spritex;
            yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, dir, currentStep) + spritey;
//#             
            realStep = GameWorld.collisionWorld(xx, yy, spritew, spriteh, dir, currentStep, spritex, spritey);
//#    
            if(realStep == 0){
                for(int i = 1; i <= GameMain.COLLISION_MAX_STEP; i += GameMain.COLLISION_STEP_ADD){
                    int newdir2 = dir, newdir3 = dir;
                    int newRealStep2, newRealStep3;
                    int newStep = currentStep * i;
//#     
                    switch(dir){
                        case Tool.DIR_UP:
                        case Tool.DIR_DOWN:
                            newdir2 = Tool.DIR_LEFT;
//#     
                            break;
                        case Tool.DIR_LEFT:
                        case Tool.DIR_RIGHT:
                            newdir2 = Tool.DIR_UP;
//#     
                            break;
                    }
//#     
                    xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir2, newStep) + spritex;
                    yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir2, newStep) + spritey;
//#     
                    newRealStep2 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir2, newStep, spritex, spritey);
//#     
                    switch(dir){
                        case Tool.DIR_UP:
                        case Tool.DIR_DOWN:
                            newdir3 = Tool.DIR_RIGHT;
//#     
                            break;
                        case Tool.DIR_LEFT:
                        case Tool.DIR_RIGHT:
                            newdir3 = Tool.DIR_DOWN;
//#     
                            break;
                    }
//#     
                    xx = Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, newdir3, newStep) + spritex;
                    yy = Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, newdir3, newStep) + spritey;
//#     
                    newRealStep3 = GameWorld.collisionWorld(xx, yy, spritew, spriteh, newdir3, newStep, spritex, spritey);
//#    
                    if(newRealStep2 != newRealStep3){
                        if(newRealStep2 > newRealStep3){
                            newRealStep2 = Math.min(newRealStep2, currentStep >> 1);
                            doMove(newdir2, newRealStep2);
                        }else{
                            newRealStep3 = Math.min(newRealStep3, currentStep >> 1);
                            doMove(newdir3, newRealStep3);
                        }
//#                         
                        break;
                    }else if(newRealStep2 == 0){
                        break;
                    }
                }
            }else{
                doMove(dir, realStep);
            }
        }else{
            doMove(dir, currentStep);
        }
    }
    private void doMove(int doDir, int doStep){
        x += Tool.calulateStepWithMoveMatrix(Tool.X_AXIS, doDir, doStep);
        y += Tool.calulateStepWithMoveMatrix(Tool.Y_AXIS, doDir, doStep);
    }
    //#endif
    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getMapId() {
        return mapId;
    }
        
    /**
     * 设置路点模式的动画序列，精灵自动按方向设置动画
     * @param index
     */
    private void setWayPointAnimate(int index){
        if(animateList.size() == 0 || wayPointInfo.ownerSprite.die){
            return;
        }

        AnimatePlayer animate = (AnimatePlayer)animateList.firstElement();

        if(animate != null){
            setAnimateIndex(animate.getAnimateName(), index, Tool.ANIMATE_PLAY_TYPE_ALWAYS, Tool.NO_CALL_BACK, null, true);
            wayPointInfo.animateOK = true;
        }
    }
    
    public WayPointInfo wayPointInfo = new WayPointInfo();

    public void addWayPoint(int dest_x, int dest_y, int moveAnimateIndex, int stopAnimateIndex, int speed, GameSprite gameSprite) {
        addWayPoint(dest_x, dest_y, moveAnimateIndex, stopAnimateIndex, false, 0, 0, speed, gameSprite, false, -1);
    }
    
    public void addWayPoint(int dest_x, int dest_y, int moveAnimateIndex, int stopAnimateIndex, boolean keepGoing, int angle, int time, int speed, GameSprite gameSprite, boolean needCorrectDir, int targetPos) {
        if(wayPointInfo.ownerSprite.die){
            return;
        }
        
        if(keepGoing && targetPos < 0 && GameMain.keepGoingDistance > 0){
            //以当前位置与路点位置距离和更大的速度算出到达修正点的时间
            int dest_t_server = Utilities.getServerTime() - time; //客户端和服务器时间差
            int dest_x_server = dest_x;
            int dest_y_server = dest_y;
            int goingDistance = GameMain.keepGoingDistance;
            
            if(dest_t_server > 0){
                int dest_d_server = dest_t_server * speed / 1000;
                dest_x_server = dest_x + (int)((long)dest_d_server * Tool.cos(angle) / 10000);
                dest_y_server = dest_y + (int)((long)dest_d_server * Tool.sin(angle) / 10000);
            }
            
            int dd = Tool.distance(x, y, dest_x_server, dest_y_server); //客户端位置和服务器位置实际差距
            
            if(dd == 0){
                wayPointInfo.startPosX = x;
                wayPointInfo.startPosY = y;
                wayPointInfo.endPosX = dest_x_server + (int)((long)goingDistance * Tool.cos(angle) / 10000);
                wayPointInfo.endPosY = dest_y_server + (int)((long)goingDistance * Tool.sin(angle) / 10000);
                wayPointInfo.currentSpeed = speed;
                wayPointInfo.modifyMode = false;
            }else{
                int ds = Math.max(wayPointInfo.currentSpeed, speed);
                int dt = dd * 1000 / ds;
                
                //根据到达修正点的时间，算出服务器路径上的修正点位置
                int td = speed * dt / 1000;
                int tx = dest_x_server + (int)((long)td * Tool.cos(angle) / 10000);
                int ty = dest_y_server + (int)((long)td * Tool.sin(angle) / 10000);
                
                //根据修正点位置算出客户端到达修正点的速度
                int md = Tool.distance(x, y, tx, ty);
                int ms = md * 1000 / dt;
                
                //更新修正点相关数据
                wayPointInfo.startPosX = x;
                wayPointInfo.startPosY = y;
                wayPointInfo.endPosX = tx;
                wayPointInfo.endPosY = ty;
                wayPointInfo.currentSpeed = ms;
                
                //更新到达修正点后的相关数据
                wayPointInfo.modifyNextPosX = dest_x_server + (int)((long)goingDistance * Tool.cos(angle) / 10000);
                wayPointInfo.modifyNextPosY = dest_y_server + (int)((long)goingDistance * Tool.sin(angle) / 10000);
                wayPointInfo.modifyNextSpeed = speed;
                wayPointInfo.modifyMode = true;
            }
        }else{
            wayPointInfo.startPosX = x;
            wayPointInfo.startPosY = y;
            
            if(targetPos > 0){
                wayPointInfo.endPosX = (targetPos >> 16) & 0xFFFF;
                wayPointInfo.endPosY = targetPos & 0xFFFF;
                //#if ModelID == AndroidAuto
                //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
            	//# {
                //# wayPointInfo.endPosX <<= 1;
                //# wayPointInfo.endPosY <<= 1;
                //# }
                //#elif DoubleScreen == true
                //# wayPointInfo.endPosX <<= 1;
                //# wayPointInfo.endPosY <<= 1;
                //#endif            
            } else {
                wayPointInfo.endPosX = dest_x;
                wayPointInfo.endPosY = dest_y;
            }
            wayPointInfo.currentSpeed = speed;
            wayPointInfo.modifyMode = false;
        }
        
        if(needCorrectDir){
            wayPointInfo.needCorrectDir = true;
            
            if(angle < 0){
                angle += (-angle / 360 + 1) * 360;
            }

            wayPointInfo.correctAngle = angle % 360;
            
        }else{
            wayPointInfo.needCorrectDir = false;
        }
        
        wayPointInfo.gameSprite = gameSprite;
        wayPointInfo.moveAnimateIndex = moveAnimateIndex;
        if(keepGoing && targetPos < 0){
        	wayPointInfo.stopAnimateIndex = moveAnimateIndex;
        }else{
        	wayPointInfo.stopAnimateIndex = stopAnimateIndex;
        }
        wayPointInfo.animateOK = false;
        wayPointInfo.needHandle = true;
        
        int[] dest_dir = wayPointInfo.startWayPoint();
        
        setDir(dest_dir[0]);
        setAnimateDir(dest_dir[0]);
        setAnimateSubDir(dest_dir[1]);
        
        if(wayPointInfo.gameSprite.isHumanAnimate()){
            setWayPointAnimate(moveAnimateIndex + dir);
        }else{
            setWayPointAnimate(moveAnimateIndex + animateSubDir);
        }
    }
    
    public class WayPointInfo{
        public GameSprite ownerSprite;
        public int startPosX; //路点开始位置x
        public int startPosY; //路点开始位置y
        public int endPosX; //路点结束位置x
        public int endPosY; //路点结束位置y

        public int startTime; //路点开始时间
        public int endTime; //预计结束时间
        public int currentSpeed; //行进速度
        public int distance; //路点经过的距离
        
        public boolean modifyMode = false; //修正模式
        public int modifyNextPosX; //到达修正点后的后续点位置x
        public int modifyNextPosY; //到达修正点后的后续点位置y
        public int modifyNextSpeed; //到达修正点后的后续速度
        
        public boolean needCorrectDir = false;
        public int correctAngle;
        
        public boolean needHandle; //是否正在处理
        public GameSprite gameSprite; //主精灵
        public int moveAnimateIndex; //移动时的动画
        public int stopAnimateIndex; //停止时的动画
        public boolean animateOK; //动画是否已经load完毕
        
        public int[] dirArray = new int[2];

        public int[] startWayPoint(){
            dirArray[0] = dir;
            dirArray[1] = animateSubDir;
            
            distance = Tool.distance(startPosX, startPosY, endPosX, endPosY);

            if(distance > 0){
                startTime = Utilities.getServerTime();
                endTime = startTime;
                movedStep = 0;

                if(currentSpeed != 0 && speedAddon > -100){
                    endTime += distance * 1000 / (currentSpeed * (100 + speedAddon) / 100);
                }

                Tool.calulateDirWithWayPointMatrix(dir, animateSubDir, startPosX, startPosY, endPosX, endPosY, dirArray);
            }

            return dirArray;
        }

        public void processWayPoint(){
        	if(ownerSprite.getType() == Tool.SPRITE_TYPE_ROLE){
           		// 主角行走单独处理
        		if (ownerSprite.die) {
        			x = endPosX;
        			y = endPosY;
        			finishWayPoint(true);
        		} else {
        			int dx = endPosX - startPosX;
	                int dy = endPosY - startPosY;
	                movedStep += ((GameRole)ownerSprite).sprite.currentStep;
	                
	                int D = movedStep;
	                
	                if (D > distance) {
	                	D = distance;
	                }
	                
	                int dest_x;
	                int dest_y;
	                
	                if(distance == 0){
	                    dest_x = endPosX;
	                    dest_y = endPosY;
	                }else{
	                    //基于原始点做偏移，减少误差
	                    dest_x = startPosX + D * dx / distance;
	                    dest_y = startPosY + D * dy / distance;
	                }
	
	                x = dest_x;
	                y = dest_y;
	                
	                
	                if(!this.ownerSprite.sprite.wayPointInfo.animateOK){
	                    Tool.calulateDirWithWayPointMatrix(this.ownerSprite.sprite.dir, this.ownerSprite.sprite.animateSubDir, x, y, endPosX, endPosY, dirArray);
	                    
	                    
	                    this.ownerSprite.sprite.setDir(dirArray[0]);
	                    this.ownerSprite.sprite.setAnimateDir(dirArray[0]);
	                    this.ownerSprite.sprite.setAnimateSubDir(dirArray[1]);
	                    
	                    this.ownerSprite.sprite.setWayPointAnimate(moveAnimateIndex + dir);
	                }
        		}
        	} else {
                if(Utilities.getServerTime() > endTime || ownerSprite.die){
                    x = endPosX;
                    y = endPosY;
                    finishWayPoint(true);
                }else if(x != endPosX || y != endPosY){
                    int elapse = Utilities.getServerTime() - startTime;

                    int dx = endPosX - startPosX;
                    int dy = endPosY - startPosY;
                    int D = (elapse * currentSpeed) / 1000;
                    
                    int dest_x;
                    int dest_y;
                    
                    if(distance == 0){
                        dest_x = endPosX;
                        dest_y = endPosY;
                    }else{
                        //基于原始点做偏移，减少误差
                        dest_x = startPosX + D * dx / (distance);
                        dest_y = startPosY + D * dy / (distance);
                    }

                    x = dest_x;
                    y = dest_y;
                    
                    if(!wayPointInfo.animateOK){
                        Tool.calulateDirWithWayPointMatrix(dir, animateSubDir, x, y, endPosX, endPosY, dirArray);
                        
                        setDir(dirArray[0]);
                        setAnimateDir(dirArray[0]);
                        setAnimateSubDir(dirArray[1]);
                        
                        if(gameSprite.isHumanAnimate()){
                            setWayPointAnimate(moveAnimateIndex + dir);
                        }else{
                            setWayPointAnimate(moveAnimateIndex + animateSubDir);
                        }
                    }
                }
        	}

        }

        public void finishWayPoint(boolean setAnimate){
            if(modifyMode){
                startPosX = x;
                startPosY = y;
                endPosX = modifyNextPosX;
                endPosY = modifyNextPosY;
                currentSpeed = modifyNextSpeed;

                modifyMode = false;
                needHandle = true;
        
                int[] dest_dir = startWayPoint();
                
                setDir(dest_dir[0]);
                setAnimateDir(dest_dir[0]);
                setAnimateSubDir(dest_dir[1]);
                
                if(gameSprite.isHumanAnimate()){
                    setWayPointAnimate(moveAnimateIndex + dir);
                }else{
                    setWayPointAnimate(moveAnimateIndex + animateSubDir);
                }
            }else{
                needHandle = false;
                
                if(needCorrectDir){
                    int tx = x + (int)((long)GameMain.keepGoingDistance * Tool.cos(correctAngle) / 10000);
                    int ty = y + (int)((long)GameMain.keepGoingDistance * Tool.sin(correctAngle) / 10000);
                    Tool.calulateDirWithWayPointMatrix(dir, animateSubDir, x, y, tx, ty, dirArray);
                    
                    setDir(dirArray[0]);
                    setAnimateDir(dirArray[0]);
                    setAnimateSubDir(dirArray[1]);
                }
                
                if(setAnimate && gameSprite != null){
                    if(gameSprite.isHumanAnimate()){
                        setWayPointAnimate(stopAnimateIndex + dir);
                    }else{
                        setWayPointAnimate(stopAnimateIndex + animateSubDir);
                    }
                }
            }
        }
    }
}
