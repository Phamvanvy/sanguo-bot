package com.pip.engine;


import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;
import com.pip.sanguo.GameMain;


/**
 * 动画资源包装类
 * 具有功能：
 * 1、载入一个动画资源
 * 2、指定当前展现动画
 * 3、每帧自动切换
 * 4、在指定位置显示
 * 5、设置当前是否需要显示
 * 6、某移动话播放完毕后回调
 * 
 * @author leo
 *
 */
public class AnimatePlayer{
    private static Tool keyMaker = new Tool();
    
    /**
     * 动画文件
     */
    private PipAnimateSet animate;

    /**
     * 动画index
     */
    private int index;

    /**
     * 当前动画所显示的frame
     */
    private int frame;

    /**
     * 当前动画是否显示
     */
    private boolean isShown;
    
    /**
     * 已经播放过一次
     */
    private boolean playedOnce;

    /**
     * 动画名称
     */
    private String name;

    /**
     * 当前动画播放完一次后的回调函数，小于0则为不回调
     */
    private int callBackIndex;

    /**
     * 需要回调时需传入压栈的参数 (测试版本存回调的类）
     */
    private IAnimateCallback callBackParams;
    
    private int key;

    /**
     * 动画播放方式
     * 0：一直播放
     * 1：播放一次
     * 2：播放后停在最后一帧
     */
    private int playType;

    /**
     * 绘画某一动画帧时的显示或偏移设置
     * 第一维对应AnimateSet的ImageSet数组，为null则不处理offset
     * 第二维对应ImageSet中的某一图块的偏移量该值为((short)replaceImageId) << 16 | (short)replaceOffset构成
     * replaceImageId小于0则表示用自己的imageSet，否则用replaceImages[replaceImageId]来绘画
     * replaceOffset小于0则表示绘画时跳过，否则绘画图块id+replaceOffset的那个图块
     */
    private int[][] drawOffset;

    /**
     * 用来替换绘画的图片资源
     */
    private ImageSet[] replaceImages;

    /**
     * 进行关联copy的时候，设置parent，子实例的frame会和parent连动
     */
    private AnimatePlayer parent;
    
    /**
     * 定位参数
     */
    private int anchor;
    
    /**
     * 绘制顺序
     */
    private int order;
    
    /**
     * 不能作为icon
     */
    private boolean notIcon;
    
    /**
     * 层数
     */
    private int layer;
    
    //优化，避免过多的new int[]
    private int[] animateBox = new int[4];
    
    /**
     * 构造函数
     * @param name
     */
    public AnimatePlayer(String name){
        this.name = name;
        parent = null;
        anchor = Tool.SPRITE_ANCHOR_X_REF | Tool.SPRITE_ANCHOR_Y_REF;
        order = Tool.DRAW_ORDER_FRONT;
        
        key = keyMaker.nextKey();
    }
    
    /**
     * 得到一个动画播放器的拷贝，并且和原文件共享换装数据
     * @return
     */
    public AnimatePlayer getCopy(){
        AnimatePlayer result = new AnimatePlayer(name);
        
        result.frame = frame;
        result.index = (index/4)*4; //默认取正向
        result.animate = animate;
        result.drawOffset = drawOffset;
        result.replaceImages = replaceImages;
        
        return result;
    }
    
    /**
     * 取得一个关联拷贝
     * @return
     */
    public AnimatePlayer getRelateCopy(){
        AnimatePlayer result = getCopy();
        result.parent = this;
        
        return result;
    }

    /**
     * 返回AnimateData的名字
     * @return
     */
    public String getAnimateName(){
        return name;
    }
    
    public int getKey(){
        return key;
    }
    
    public int getAnchor(){
        return anchor;
    }
    
    public void setAnchor(int anchor){
        this.anchor = anchor;
    }
    
    public int getOrder(){
        return order;
    }
    
    public void setOrder(int order){
        this.order = order;
    }
    
    public boolean isNotIcon(){
        return notIcon;
    }
    
    public void setNotIcon(boolean notIcon){
        this.notIcon = notIcon;
    }
    
    public int getLayer(){
        return layer;
    }
    
    public void setLayer(int layer){
        this.layer = layer;
    }
    
    /**
     * 初始化动画数据
     * @param animate 动画实例
     */
    public void init(PipAnimateSet animate){
        this.animate = animate;
        this.drawOffset = new int[animate.getImageCount()][];
    }

    /**
     * 设置动画中的某一图片资源的drawOffset
     * @param imageId 图片ID
     * @param index 图片中图块的下标，小于0时则清除该图片资源的drawOffset设置以节省内存
     * @param replaceImageId，绘画时替换的imageId，小于0则为不替换，否则替换为replaceImages[replaceImageId]
     * @param replaceOffset，在绘画相应图块是将图块ID加上drawOffset的值
     */
    public void setAnimateImageDrawOffset(int imageId, int index, int replaceImageId, int replaceOffset){
        if(index < 0){
            drawOffset[imageId] = null;
        }else{
            if(drawOffset[imageId] == null){
                drawOffset[imageId] = new int[animate.getImageFrameCount(imageId)];

                int count = drawOffset[imageId].length;
                for(int i = 0; i < count; i++){
                    drawOffset[imageId][i] = (Tool.ANIMATE_IMAGE_NO_REPLACE) << 16 | 0;
                }
            }

            drawOffset[imageId][index] = (replaceImageId << 16) | (replaceOffset & 0xFFFF);
        }
    }

    /**
     * 添加可替换的图片资源
     * @param imageId
     * @param imageName
     */
    public void addReplaceAnimateImage(String imageName){
        ImageSet imageSet =  GameMain.resourceManager.findImageSet(imageName, true);

        Vector tmp = new Vector();

        if(replaceImages != null){
            for(int i = 0; i < replaceImages.length; i++){
                tmp.addElement(replaceImages[i]);
            }
        }

        tmp.addElement(imageSet);
        replaceImages = new ImageSet[tmp.size()];

        tmp.copyInto(replaceImages);
    }

    /**
     * 在指定位置绘制动画
     * @param g
     * @param x
     * @param y
     */
    public void draw(Graphics g, int x, int y){
        if(isShown){
            if(parent == null){
                animate.drawAnimateFrameWithOffset(g, index, frame, x, y, drawOffset, replaceImages);
            }else{
                animate.drawAnimateFrameWithOffset(g, parent.index, parent.frame, x, y, drawOffset, replaceImages);
            }
        }
    }
    
    /**
     * 绘制某一个图片桢
     * @param g
     * @param x
     * @param y
     */
    public void drawSingleFrame(Graphics g, int index, int x, int y){
        if(isShown){
            animate.drawFrameWithOffset(g, index, x, y, drawOffset, replaceImages);
        }
    }

    /**
     * 驱动函数
     */
    public void cycle(){
        if(parent == null){
            if(isShown){
            	//#if NewUI2
            	//# if((GameMain.tick & 0x1) == 0){
            		//# frame++;
            	//# }
            	//#else
                frame++;
                //#endif
                
                if(frame >= animate.getAnimateLength(index)){
                    switch(playType){
                        case Tool.ANIMATE_PLAY_TYPE_ALWAYS:
                            frame = 0;
                            break;
                        case Tool.ANIMATE_PLAY_TYPE_ONCE:
                        case Tool.ANIMATE_PLAY_TYPE_HOLD:
                            frame--;
                            break;
                    }
        
                    if(callBackIndex > 0 && !playedOnce){
                        callBack(callBackIndex, callBackParams);
                    }
                    
                    playedOnce = true;
                }
            }
        }
    }
    
    public int getPlayType() {
    	return playType;
    }
    
    public boolean isHunmanAnimate(){
        if(animate.getAnimateCount() >= GameMain.humanAnimateIndex){
            return true;
        }else{
            return false;
        }
    }
    
    public int getAnimateCount(){
        return animate.getAnimateCount();
    }
    
    public boolean playing(){
        if(playType != Tool.ANIMATE_PLAY_TYPE_ALWAYS && callBackIndex > 0 && !playedOnce && isShown){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 测试版本需要，临时用此函数
     * @param callBackFunction
     * @param callBackParams
     */
    public void callBack(int callBackIndex, IAnimateCallback callBackParams){
        callBackParams.callback(callBackIndex, key);
    }

    /**
     * 设置当前动画index
     * @param index
     */
    public void setAnimate(int index, int playType, int callBackIndex, IAnimateCallback callBackParams){
        if(index != this.index || callBackIndex >= 0){
            if(index < 0){
                index = animate.getAnimateCount() - 1;
            }else if(index >= animate.getAnimateCount()){
                index = 0;
            }

            this.index = index;
            frame = 0;
            playedOnce = false;
        }

        this.playType = playType;
        this.callBackIndex = callBackIndex;
        this.callBackParams = callBackParams;
    }

    /**
     * 设置是否显示
     * @param shown
     */
    public void setShown(boolean shown){
        isShown = shown;
    }

    /**
     * 获得当前是否显示
     * @return
     */
    public boolean isShown(){
        return isShown;
    }

    /**
     * 返回按当前动画的显示区域
     * @return
     */
    public int[] getDrawArea(){
        return animate.getAnimateSize(index);
    }
    
    /**
     * 返回当前动画的显示矩形
     * @return
     */
    public int[] getAnimateBox(int index){
        if(index < 0){
            return animate.getAnimateBox(animateBox, this.index);
        }else{
            return animate.getAnimateBox(animateBox, index);
        }
    }

    /**
     * 比较两个动画是否一样
     */
    public boolean equals(Object obj){
        if(obj instanceof AnimatePlayer){
            AnimatePlayer other = (AnimatePlayer)obj;

            if(other.name.equals(this.name)){
                return true;
            }else{
                return false;
            }
        }else{
            return super.equals(obj);
        }
    }
}
