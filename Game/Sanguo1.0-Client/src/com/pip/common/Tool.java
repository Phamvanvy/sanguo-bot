package com.pip.common;
//#if NewUI2
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPInputStream;
//#endif

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
//#if ModelID == AndroidAuto
//#if opengl == true
//# import com.pip.android.opengl.GLGraphics;
//#endif
//#endif
import com.pip.engine.IAnimateCallback;
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;
import com.pip.image.PipImage;
import com.pip.io.UASegment;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameNetPlayer;
import com.pip.sanguo.GameNpc;
import com.pip.sanguo.GameSprite;
import com.pip.sanguo.GameWorld;
import com.pip.sanguo.SanguoMIDlet;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class Tool{
    //常用颜色定义
    public static final int TRANS_NONE = 0;
    public static final int TRANS_MIRROR_ROT180 = 1;
    public static final int TRANS_MIRROR = 2;
    public static final int TRANS_ROT180 = 3;
    public static final int TRANS_MIRROR_ROT270 = 4;
    public static final int TRANS_ROT90 = 5;
    public static final int TRANS_ROT270 = 6;
    public static final int TRANS_MIRROR_ROT90 = 7;

    //定位参数
    public static final int G_BOTTOM = 32;
    public static final int G_HCENTER = 1;
    public static final int G_LEFT = 4;
    public static final int G_RIGHT = 8;
    public static final int G_TOP = 16;
    public static final int G_VCENTER = 2;
    public static final int G_TOPLEFT = 20;
    public static final int G_CENTER = 3;
    public static final int G_TOPCENTER = 17;
    public static final int G_TOPRIGHT = 24;
    public static final int G_BOTTOMLEFT = 36;
    public static final int G_BOTTOMRIGHT = 40;
    public static final int G_BOTTOMCENTER = 33;
    public static final int G_LEFTCENTER = 6;
    public static final int G_RIGHTCENTER = 10;

    // 文字水平对齐方式
    public static final byte H_CENTER = 0;
    public static final byte H_LEFT = 1;
    public static final byte H_RIGHT = 2;

    public static Random rnd = new Random(Tool.getSystemTime());

    // 上次绘制半透明框的数据缓存
    private static int[] alphaColors;
    
    public static long getSystemTime(){
        return System.currentTimeMillis();
    }

    /**
     * 返回索引一个精灵需要的key
     * @param instanceId 精灵instanceId
     * @return
     */
    public static Integer getSpriteKey(int instanceId){
        return new Integer(instanceId);
    }

    /**
     * 返回索引一个精灵需要的key
     * 
     * @param type 精灵类型
     * @param in 精灵id
     * @return
     */
    public static Long getSpriteKey(int type, int id){
        return new Long(((long) type << 32) | id);
    }

    public static int getSpriteKeyType(Long key){
        return (int) (key.longValue() >> 32);
    }

    public static int getSpriteKeyId(Long key){
        return (int) (key.longValue() & 0xFFFFFFFF);
    }

    /**
     * 按找方向返回相应坐标轴方向的移动步长
     * @param axis 坐标轴
     * @param dir 方向
     * @param step 输入步长
     * @return
     */
    public static int calulateStepWithMoveMatrix(byte axis, int dir, int step){
        return MOVE_MATRIX[(dir << 1) + axis] * step;
    }

    /**
     * 按找方向返回相应坐标轴方向的后背步长
     * @param axis 坐标轴
     * @param dir 方向
     * @param step 输入步长
     * @return
     */
    public static int calulateStepWithBackMatrix(byte axis, int dir, int step){
        return BACK_MATRIX[(dir << 1) + axis] * step;
    }

    /**
     * 按照移动方向和tick数返回相应坐标轴方向的抖动位移
     * @param axis 坐标轴
     * @param dir 方向
     * @param tick cylce数
     * @return
     */
    public static int calulateOffsetWithVibraMatrix(byte axis, int dir, int tick){
        return VIBRA_MATRIX[(dir << 2) + ((tick & 0x1) << 1) + axis];
    }

    /**
     * 按路点矩阵返回src位置向dest位置移动的当前方向和斜向朝向
     * @param src_x 原始方向
     * @param src_x
     * @param src_y
     * @param dest_x
     * @param dest_y
     * @return
     */
    public static void calulateDirWithWayPointMatrix(int srcDir, int srcSubDir, int src_x, int src_y, int dest_x, int dest_y, int[] newDir){
        int dx = dest_x - src_x;
        int dy = dest_y - src_y;
        
        newDir[0] = srcDir;
        newDir[1] = srcSubDir;

        if(dx == 0 && dy == 0){
            return;
        }

        if(Math.abs(dx) - Math.abs(dy) >= 0){
            if(dx >= 0){
                newDir[0] = DIR_RIGHT;

                if(dy >= 0){
                    newDir[1] = SUB_DIR_BOTTOMRIGHT;
                }else{
                    newDir[1] = SUB_DIR_TOPRIGHT;
                }
            }else{
                newDir[0] = DIR_LEFT;

                if(dy >= 0){
                    newDir[1] = SUB_DIR_BOTTOMLEFT;
                }else{
                    newDir[1] = SUB_DIR_TOPLEFT;
                }
            }
        }else{
            if(dy >= 0){
                newDir[0] = DIR_DOWN;

                if(dx >= 0){
                    newDir[1] = SUB_DIR_BOTTOMRIGHT;
                }else{
                    newDir[1] = SUB_DIR_BOTTOMLEFT;
                }
            }else{
                newDir[0] = DIR_UP;

                if(dx >= 0){
                    newDir[1] = SUB_DIR_TOPRIGHT;
                }else{
                    newDir[1] = SUB_DIR_TOPLEFT;
                }
            }
        }
    }

    /**
     * 为路点移动矩阵的输入参数做数组下标修正，大于0返回2，等于0返回1，小于0返回0
     * @param para 输入参数
     * @return
     */
    private static int correctWayPointMatrixParameter(int para){
        return (para > 0? 1: (para == 0? 0: -1)) + 1;
    }

    /**
     * 生成一个随机数。
     * @param min 最小值(含)
     * @param max 最大值(不含)
     * @return
     */
    public static int getNextRnd(int min, int max){
        if(max <= min){
            return min;
        }
        return(min + Math.abs(rnd.nextInt()) % (max - min));
    }

    /**
     * 将box与box1合并，使box可以同时包容两个box，box的数据为x1, y1, w, h
     * @param box
     * @param box1
     * @return
     */
    public static void mergeBox(int[] box, int[] box1){
        if(box1[2] == 0){
            return;
        }else if(box[2] == 0){
            box[0] = box1[0];
            box[1] = box1[1];
            box[2] = box1[2];
            box[3] = box1[3];
        }else{
            int bx = box[0];
            int by = box[1];
            box[0] = (bx < box1[0])? bx: box1[0];
            box[1] = (by < box1[1])? by: box1[1];
            box[2] = (((bx + box[2]) > (box1[0] + box1[2]))? (bx + box[2]): (box1[0] + box1[2])) - box[0];
            box[3] = (((by + box[3]) > (box1[1] + box1[3]))? (by + box[3]): (box1[1] + box1[3])) - box[1];
        }
    }

    /**
     * 把一个字符串按指定分隔符分段。
     * @param s 原始字符串
     * @param ch 分隔符
     * @return 分出的段的数组
     */
    public static String[] splitString(String s, char ch){
        int startIndex = 0;
        int endIndex = 0;
        Vector vS = new Vector();
        while(true){
            endIndex = s.indexOf(ch, startIndex);
            if(endIndex == -1){
                vS.addElement(s.substring(startIndex));
                break;
            }else{
                vS.addElement(s.substring(startIndex, endIndex));
                startIndex = endIndex + 1;
            }
        }
        String[] strs = new String[vS.size()];
        vS.copyInto(strs);
        return strs;
    }

    /**
     * 把一个字符串分成多行。
     * @param s 原始字符串
     * @return 分割后的行数组
     */
    public static String[] splitString(String s){
        return splitString(s, '\n');
    }

    /**
     * 获取字符串数组的最大宽度
     * @param strs
     * @return
     */
    public static int getStringsMaxWidth(String[] strs, boolean mixed){
        int width = 0;
        if(strs != null){
            int sw = 0;
            for(int i = 0; i < strs.length; i++){
                if(mixed){
                    sw = drawMixedText(null, strs[i], 0, 0, 0, 0, true, 0);
                }else{
                    sw = Utilities.font.stringWidth(strs[i]);
                }

                if(width < sw){
                    width = sw;
                }
            }
        }

        return width;
    }

	//#if AlphaMethod == rgbimage
    public static void clearAlphaImageMap() {
    	alphaImageMap.clear();
    }
    //#endif
    
    /**
     * 绘制一个半透明的矩形。
     * @param g
     * @param rgb 带ALPHA度的色值
     * @param x
     * @param y
     * @param width
     * @param height
     */
    //#if AlphaMethod == rgbimage
    private static Image alphaImage = null;
    private static Hashtable alphaImageMap = new Hashtable();

    //#endif
    public static void fillAlphaRect(Graphics g, int rgb, int x, int y, int width, int height){
        int alpha = rgb & 0xFF000000;
        if(alpha == 0){
            return;
        }else if(alpha == 0xFF000000){
            g.setColor(rgb & 0xFFFFFF);
            g.fillRect(x, y, width, height);
            return;
        }
//#if opengl == true
         //# if (Canvas.openglMode) {
    		 //# g.setColor(rgb);
    		 //# g.fillRect(x, y, width, height);
    		 //# return;
    	 //# }
//#endif
        //g
        int clipX = g.getClipX();
		int clipY = g.getClipY();
		int clipW = g.getClipWidth();
		int clipH = g.getClipHeight();
        //#if AlphaMethod == rgbimage
        alphaImage = (Image) alphaImageMap.get(new Integer(rgb));
        if(alphaImage == null){
            int[] tmp = new int[GameMain.viewWidth];
            for(int i = 0; i < GameMain.viewWidth; i++){
                tmp[i] = rgb;
            }
            alphaImage = Image.createRGBImage(tmp, GameMain.viewWidth, 1, true);
            alphaImageMap.put(new Integer(rgb), alphaImage);
        }
        g.clipRect(x, y, width, height);
        for(int j = 0; j < height; j++){
            g.drawImage(alphaImage, x, y + j, Graphics.TOP | Graphics.LEFT);
        }
        g.setClip(clipX, clipY, clipW, clipH);
        //#elif AlphaMethod == rgb
        //# boolean bnew = false;
        //# if (alphaColors == null) {
        //# bnew = true;
        //# }else if(width != alphaColors.length || rgb != alphaColors[0]){
        //# bnew = true;
        //# }
        //# if(bnew == true){
        //# alphaColors = new int[width];
        //# for(int i = 0; i < width; i++){
        //# alphaColors[i] = rgb;
        //# }
        //# }
        //# g.drawRGB(alphaColors, 0, 0, x, y, width, height, true);
        //#elif AlphaMethod == nokiaui
        //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //# int[] xPoints = {x, x+width, x+width, x};
        //# int[] yPoints = {y, y, y+height, y+height};
        //# dg.fillPolygon(xPoints, 0, yPoints, 0, 4, rgb);
        //#else
        //# g.setColor(rgb & 0xFFFFFF);
        //# g.fillRect(x, y, width, height);
        //# return;
        //#endif
    }

    /**
     * 绘制一个带立体效果的字符串。
     * @param g
     * @param text
     * @param x
     * @param y
     * @param color 字体颜色
     * @param bgColor 背景色，当小于0时表示没有背景。
     */
    public static void draw3DString(Graphics g, String text, int x, int y, int color, int bgColor, int anchor){
    	if(text == null || text.length()==0){
    		return;
    	}
    	//#if opengl == true
    	 //# if (Canvas.openglMode) {
        	 //# GLGraphics gg = (GLGraphics)g;
        	 //# switch(GameMain.draw3DStringLevel){
        	 //# case 0:
        	 //# case 1:
        		 //# gg.setColor(color);
        		 //# gg.draw3DString(text, x, y, anchor, bgColor);
        		 //# break;
        	 //# case 2:
        		 //# g.setColor(bgColor);
                 //# drawString(g, text, x + 1, y + 1, anchor);
                 //# g.setColor(color);
                 //# drawString(g, text, x, y, anchor);
                 //# break;
        	 //# default:
        		 //# g.setColor(color);
                 //# drawString(g, text, x + 1, y + 1, anchor);
        		 //# break;
        	 //# }
        	 //# return;
         //# }
//#endif
    	
    	if(bgColor < 0){
            g.setColor(color);
            drawString(g, text, x, y, anchor);
            return;
        }

        switch(GameMain.draw3DStringLevel){
            case 0:
                g.setColor(bgColor);
                drawString(g, text, x + 1, y - 1, anchor);
                drawString(g, text, x - 1, y + 1, anchor);
                drawString(g, text, x - 1, y - 1, anchor);
                drawString(g, text, x + 1, y + 1, anchor);
                drawString(g, text, x, y - 1, anchor);
                drawString(g, text, x, y + 1, anchor);
                drawString(g, text, x - 1, y, anchor);
                drawString(g, text, x + 1, y, anchor);

                break;
            case 1:
                g.setColor(bgColor);
                drawString(g, text, x, y - 1, anchor);
                drawString(g, text, x, y + 1, anchor);
                drawString(g, text, x - 1, y, anchor);
                drawString(g, text, x + 1, y, anchor);

                break;
            case 2:
                g.setColor(bgColor);
                drawString(g, text, x + 1, y + 1, anchor);
        }

        g.setColor(color);
        drawString(g, text, x, y, anchor);
    }

    public static void draw3DString2(Graphics g, String text, int x, int y, int color, int bgColor, int anchor){
//#if opengl == true
   	 //# if (Canvas.openglMode) {
       	 //# GLGraphics gg = (GLGraphics)g;
       	 //# switch(GameMain.draw3DStringLevel){
       	 //# case 0:
       	 //# case 1:
       		 //# gg.setColor(color);
       		 //# gg.draw3DString(text, x, y, anchor, bgColor);
       		 //# break;
       	 //# case 2:
       		 //# g.setColor(bgColor);
                //# drawString(g, text, x + 1, y + 1, anchor);
                //# g.setColor(color);
                //# drawString(g, text, x, y, anchor);
                //# break;
       	 //# default:
       		 //# g.setColor(color);
                //# drawString(g, text, x + 1, y + 1, anchor);
       		 //# break;
       	 //# }
       	 //# return;
        //# }
//#endif
    	if(bgColor < 0){
            g.setColor(color);
            drawString(g, text, x, y, anchor);
            return;
        }

        switch(GameMain.draw3DStringLevel){
            case 0:
                g.setColor(bgColor);
                drawString(g, text, x + 2, y, anchor);
                drawString(g, text, x, y + 2, anchor);
                drawString(g, text, x, y, anchor);
                drawString(g, text, x + 2, y + 2, anchor);
                drawString(g, text, x + 1, y, anchor);
                drawString(g, text, x + 1, y + 2, anchor);
                drawString(g, text, x, y + 1, anchor);
                drawString(g, text, x + 2, y + 1, anchor);

                break;
            case 1:
                g.setColor(bgColor);
                drawString(g, text, x + 1, y, anchor);
                drawString(g, text, x + 1, y + 2, anchor);
                drawString(g, text, x, y + 1, anchor);
                drawString(g, text, x + 2, y + 1, anchor);

                break;
            case 2:
                g.setColor(bgColor);
                drawString(g, text, x + 1, y + 1, anchor);
                g.setColor(color);
                drawString(g, text, x, y, anchor);
                return;
        }

        g.setColor(color);
        drawString(g, text, x + 1, y + 1, anchor);
    }
    
    public static int get3DStringWidth(String str){
        return get3DStringWidthEx(str, Utilities.font);
    }
    
    public static int get3DStringWidthEx(String str, Font font){
        int _3dStringWidth = font.stringWidth(str);
        switch(GameMain.draw3DStringLevel){
            case 0:
            case 1:
                _3dStringWidth += 2;
                break;
            case 2:
                _3dStringWidth++;
                break;
        }
        return _3dStringWidth;
    }

    public static int get3DStringHeight(){
        return get3DStringHeightEx(Utilities.font);
    }
    
    public static int get3DStringHeightEx(Font font){
        int _3dStringHeight = font.getHeight();

        switch(GameMain.draw3DStringLevel){
            case 0:
            case 1:
                _3dStringHeight += 2;
                break;
            case 2:
                _3dStringHeight++;
                break;
        }

        return _3dStringHeight;
    }

    /**
     * 绘制字符串。
     */
    public static void drawString(Graphics g, String text, int x, int y, int anchor){
        g.drawString(text, x, y - Utilities.CHAR_OFFSET, anchor);
    }

    public static int drawImageNumber(Graphics g, ImageSet numberImg, String str, int x, int y, int anchor){
        return drawImageNumber(g, numberImg, 0, str, x, y, 0, anchor);
    }

    /**
     * 在指定位置绘制一串数字。
     * @param g
     * @param numberImg 图片文件 
     * @param startIndex 0在numberImg中的索引
     * @param str 只能包含数字0-9，或者/
     * @param x 
     * @param y
     * @param space 数字之间的间隔
     * @param anchor 对齐方式，参考MIDP规范
     */
    public static int drawImageNumber(Graphics g, ImageSet numberImg, int startIndex, String str, int x, int y, int space, int anchor){
        if(numberImg == null){
            return 0;
        }
        int cw = numberImg.getFrameWidth(startIndex);
        int ch = numberImg.getFrameHeight(startIndex);
        int len = str.length();
        int tw = len * cw;
        if((anchor & Graphics.HCENTER) > 0){
            x -= tw / 2;
        }else if((anchor & Graphics.RIGHT) > 0){
            x -= tw;
        }
        if((anchor & Graphics.VCENTER) > 0){
            y -= ch / 2;
        }else if((anchor & Graphics.BOTTOM) > 0){
            y -= ch;
        }
        int totalWidth = 0;
        for(int i = 0; i < len; i++){
            char chr = str.charAt(i);
            int ind = -1;
            if(chr >= '0' && chr <= '9'){
                ind = chr - '0';
            }else if(chr == '+'){
                ind = 10;
            }else if(chr == '-'){
                ind = 11;
            }else if(chr == '/'){
                ind = 12;
            }else if(chr == '('){
                ind = 13;
            }else if(chr == ')'){
                ind = 14;
            }

            if(g != null){
                numberImg.drawFrame(g, startIndex + ind, x, y);
            }

            x += cw + space;
            totalWidth += cw + space;
        }

        return totalWidth;
    }

    public static void drawFlyingString(Graphics g, int x, int y, String str, int color, int distance, int percent, int tick, boolean is3d){
        int rx = x;
        int ry = y;

        ry -= distance * percent / 100;

        if(is3d){
            Tool.draw3DString(g, str, rx, ry, color, 0x000000, Graphics.HCENTER | Graphics.BOTTOM);
        }else{
            g.setColor(color);
            g.drawString(str, rx, ry, Graphics.HCENTER | Graphics.BOTTOM);
        }
    }

    public static void drawFlyingNumber(Graphics g, int x, int y, int[] frames, int paletteColor, int distance, int percent, int tick){
        int rx = x;
        int ry = y;
        int offset = GameMain.flyNumberIndex;

        offset += paletteColor * GameMain.flyNumberBlockCount;

        rx -= (frames.length * GameMain.numberImage.getFrameWidth(offset)) / 2;
        ry -= distance * percent / 100;

        for(int i = 0; i < frames.length; i++){
            int frame = frames[i] + offset;

            //            if(tick < 2){
            //                frame = offset + GameMain.flyNumberBlockCount + 2 + tick;
            //            }

            GameMain.numberImage.drawFrame(g, frame, rx, ry);
            rx += GameMain.numberImage.getFrameWidth(frame);
        }
    }

    public static int[] getNumberFrameArray(int number){
        Vector tmp = new Vector();

        if(number < 0){
            tmp.addElement(new Integer(11));
            number = -number;
        }else{
            tmp.addElement(new Integer(10));
        }
        String str = "" + number;

        for(int i = 0; i < str.length(); i++){
            tmp.addElement(new Integer(str.charAt(i) - '0'));
        }

        int[] result = new int[tmp.size()];
        
        for(int i = 0; i < result.length; i++){
            result[i] = ((Integer) tmp.elementAt(i)).intValue();
        }

        return result;
    }

    /**
     * 计算矩形2按照direct的方向移动时距离碰撞到矩形1的距离
     * @param x1 矩形1
     * @param y1
     * @param w1
     * @param h1
     * @param x2 矩形2
     * @param y2
     * @param w2
     * @param h2
     * @param direct 移动方向
     * @return 距离
     */
    public static int calculateDistance(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int direct){
        int result = 0;

        switch(direct){
            case DIR_UP:
                result = y2 - (y1 + h1);

                break;
            case DIR_DOWN:
                result = y1 - (y2 + h2);

                break;
            case DIR_LEFT:
                result = x2 - (x1 + w1);

                break;
            case DIR_RIGHT:
                result = x1 - (x2 + w2);

                break;
        }

        if(result < 0){
            result = 0;
        }

        return result;
    }

    /**
     * 判断两个矩形是否相交
     * @param x1 第一个矩形
     * @param y1
     * @param w1
     * @param h1
     * @param x2 第二个矩形
     * @param y2
     * @param w2
     * @param h2
     * @return
     */
    public static boolean rectIntersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
        return(!(x1 + w1 <= x2 || x1 >= x2 + w2 || y1 + h1 <= y2 || y1 >= y2 + h2));
    }
    
    /**
     * 计算相交矩形，如果不相交返回null
     * 
     * @param x1 第一个矩形
     * @param y1
     * @param w1
     * @param h1
     * @param x2 第二个矩形
     * @param y2
     * @param w2
     * @param h2
     * @return
     */
    public static void rectGetIntersection(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int[] ret) {
		int tx1 = x1;
		int ty1 = y1;
		int rx1 = x2;
		int ry1 = y2;
		long tx2 = tx1; tx2 += w1;
		long ty2 = ty1; ty2 += h1;
		long rx2 = rx1; rx2 += w2;
		long ry2 = ry1; ry2 += h2;
		if (tx1 < rx1) tx1 = rx1;
		if (ty1 < ry1) ty1 = ry1;
		if (tx2 > rx2) tx2 = rx2;
		if (ty2 > ry2) ty2 = ry2;
		tx2 -= tx1;
		ty2 -= ty1;
		// tx2,ty2 will never overflow (they will never be
		// larger than the smallest of the two source w,h)
		// they might underflow, though...
		if (tx2 < Integer.MIN_VALUE) tx2 = Integer.MIN_VALUE;
		if (ty2 < Integer.MIN_VALUE) ty2 = Integer.MIN_VALUE;

		ret[0] = tx1;
		ret[1] = ty1;
		ret[2] = (int)tx2;
		ret[3] = (int)ty2;
    }

    /**
     * 判断第一个矩形是否包含 第二个矩形
     * @param x1 第一个矩形
     * @param y1
     * @param w1
     * @param h1
     * @param x2 第二个矩形
     * @param y2
     * @param w2
     * @param h2
     * @return
     */
    public static boolean rectContain(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
        if(x1 <= x2 && x1 + w1 >= x2 + w2 && y1 <= y2 && y1 + h1 >= y2 + h2){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 一个点是否在一个矩形中
     * @param x1
     * @param y1
     * @param w1
     * @param h1
     * @param x2
     * @param y2
     * @return
     */
    public static boolean rectIn(int x1, int y1, int w1, int h1, int x2, int y2){
        if(x1 <= x2 && x1 + w1 >= x2 && y1 <= y2 && y1 + h1 >= y2){
            return true;
        }else{
            return false;
        }
    }

    public static int distance(int x1, int y1, int x2, int y2){
        int dx = x2 - x1;
        int dy = y2 - y1;

        return (int) sqrt(dx * dx + dy * dy);
    }

    public static long sqrt(long x){
        long y = 0;
        long b = (~Long.MAX_VALUE) >>> 1;

        while(b > 0){
            if(x >= y + b){
                x -= y + b;
                y >>= 1;
                y += b;
            }else{
                y >>= 1;
            }

            b >>= 2;
        }

        return y;
    }

    /**
     * 输出调试过信息
     * @param msg
     */
    public static void debug(String msg){
        System.out.println(msg);
    }

    /**
     * 获取一个角度的正弦值
     * @param angle
     * @return
     */
    public static int sin(int angle){
        if(angle < 0){
            angle += (-angle / 360 + 1) * 360;
        }

        int reaAngle = angle % 360;
        int ret = 0;

        if(reaAngle >= 0 && reaAngle <= 90){
            ret = SIN_TABLE[reaAngle];
        }else if(reaAngle > 90 && reaAngle <= 180){
            ret = SIN_TABLE[180 - reaAngle];
        }else if(reaAngle > 180 && reaAngle <= 270){
            ret = -SIN_TABLE[reaAngle - 180];
        }else if(reaAngle > 270 && reaAngle < 360){
            ret = -SIN_TABLE[360 - reaAngle];
        }

        return ret;
    }

    /**
     * 获取一个角度的余弦值
     * @param angle
     * @return
     */
    public static int cos(int angle){
        return sin(90 - angle);
    }

    /**
     * 求m和n的最大公约数
     * @param m
     * @param n
     * @return
     */
    public static int gcd(int m, int n){
        if(m == 0){
            return n;
        }

        if(n == 0){
            return m;
        }

        if(m < n){
            int tmp = m;
            m = n;
            n = tmp;
        }

        while(n != 0){
            int tmp = m % n;
            m = n;
            n = tmp;
        }

        return m;
    }

    /**
     * 从jar包中读取一个文件
     * @param name 文件名
     * @return byte[]
     * @throws Exception
     */
    public static byte[] loadLocalResource(String name){
        byte[] result = null;
        InputStream is = null;

        try{
			//#if ModelID == AndroidAuto
//#         	if (GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
//#         	{
//#         	is = SanguoMIDlet.instance.getClass().getResourceAsStream("/AndroidSmall/" + name);
//#         	}
//#         	else if(GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL))
//#         	{
//#         	is = SanguoMIDlet.instance.getClass().getResourceAsStream("/Android/" + name);
//#         	}
//#         	else
//#         	{
//#         	is = SanguoMIDlet.instance.getClass().getResourceAsStream("/AndroidLarge/" + name);
//#         	}
			//#else
				is = SanguoMIDlet.instance.getClass().getResourceAsStream("/" + name);
			//#endif

            result = Utilities.getBytesFromInput(is);
        }catch(Exception e){
            //#ifdef buildtest
            System.out.println("load local resource error : " + name);
            e.printStackTrace();
            //#endif
        }finally{
            if(is != null){
                try{
                    is.close();
                }catch(Exception e){
                }
            }
        }

        return result;
    }

    /**
     * 指定一个颜色数组和大小来绘制一个带1像素宽度嵌套边框的填充矩形，第0个颜色是底色
     * @param g
     * @param x
     * @param y
     * @param w
     * @param h
     * @param colors
     */
    public static void drawFrameBox(Graphics g, int x, int y, int width, int height, int[] colors){
        int count = colors.length;

        g.setColor(colors[0]);
        g.fillRect(x, y, width - 1, height - 1);

        int bx = x - 1;
        int by = y - 1;
        int bw = width + 1;
        int bh = height + 1;

        for(int i = 1; i < count; i++){
            bx++;
            by++;
            bw -= 2;
            bh -= 2;

            g.setColor(colors[i]);
            g.drawRect(bx, by, bw, bh);
        }
    }

    /**
     * 用一张图片通过旋转自动画指定区域的4个角
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     * @param image
     * @param index
     */
    public static void drawBoxCorner(Graphics g, int x, int y, int width, int height, ImageSet image, int index){
        int imageWidth = image.getFrameWidth(index);
        int imageHeight = image.getFrameHeight(index);

        //左上角
        image.drawFrame(g, index, x, y, TRANS_NONE);
        //右上角
        image.drawFrame(g, index, x + width - imageWidth, y, TRANS_MIRROR);
        //左下角
        image.drawFrame(g, index, x, y + height - imageHeight, TRANS_MIRROR_ROT180);
        //右下角
        image.drawFrame(g, index, x + width - imageWidth, y + height - imageHeight, TRANS_ROT180);
    }
    
	public static final int RES_UI_TIP_ARROW = 13; //提示框箭头
	public static final int RES_UI_TIP_TOPRIGHT = 14; //提示框右上
	/**
	 * 画气泡
	 */
    public static void drawTip(Graphics g, int arrowX, int arrowY, String[] tipStr, ImageSet res_Image) {
    	if(tipStr == null){
    		return;
    	}
    	int tipCornerWidth = res_Image.getFrameWidth(RES_UI_TIP_TOPRIGHT);
    	int tipCornerHeight = res_Image.getFrameHeight(RES_UI_TIP_TOPRIGHT);
    	int tipArrowHeight = res_Image.getFrameWidth(RES_UI_TIP_ARROW);
    	int tipRows = tipStr.length;
    	
    	int maxWidth = 0;
    	for(int i=0;i<tipRows;i++){
    		int width = drawMixedText(null, tipStr[i], 0, 0, 0, 0, true, 0);
    		if(maxWidth < width){
    			maxWidth = width;
    		}
    	}
    	
    	int bagTipWidth = (tipCornerWidth << 1) + maxWidth + 2;
    	int bagTipHeight = Utilities.font.getHeight() * tipRows;
    	if(bagTipHeight < (tipCornerHeight << 1)) {
    		bagTipHeight = (tipCornerHeight << 1);
    	}
    	
    	int bagTipX = arrowX - tipCornerWidth - (bagTipWidth >> 1);
    	int bagTipY = arrowY - tipArrowHeight - bagTipHeight;

    	int bagTipRx = bagTipX + bagTipWidth;
    	//填充底色
//    	g.setColor(0xFAFFE5);
    	Tool.fillAlphaRect(g, 0xaaFAFFE5, bagTipX, bagTipY + tipCornerHeight, bagTipWidth, bagTipHeight - (tipCornerHeight<<1));
    	Tool.fillAlphaRect(g, 0xaaFAFFE5, bagTipX + tipCornerWidth, bagTipY, bagTipWidth - (tipCornerWidth<<1), tipCornerHeight);
    	Tool.fillAlphaRect(g, 0xaaFAFFE5, bagTipX + tipCornerWidth, bagTipY + bagTipHeight - tipCornerHeight, bagTipWidth - (tipCornerWidth<<1), tipCornerHeight);
//    	g.fillRect(bagTipX, bagTipY + tipCornerHeight, bagTipWidth, bagTipHeight - (tipCornerHeight<<1));
//    	g.fillRect(bagTipX + tipCornerWidth, bagTipY, bagTipWidth - (tipCornerWidth<<1), tipCornerHeight);
//    	g.fillRect(bagTipX + tipCornerWidth, bagTipY + bagTipHeight - tipCornerHeight, bagTipWidth - (tipCornerWidth<<1), tipCornerHeight);
    	//描边（3条横线）
    	g.setColor(0x4F0900);
    	g.drawLine(bagTipX + tipCornerWidth, bagTipY, bagTipX + bagTipWidth - tipCornerWidth, bagTipY);
    	g.drawLine(bagTipX + tipCornerWidth, bagTipY + bagTipHeight, bagTipX + bagTipWidth - tipCornerWidth, bagTipY + bagTipHeight);
    	//描边（2条竖线）
    	g.drawLine(bagTipX, bagTipY + tipCornerHeight, bagTipX, bagTipY + bagTipHeight - tipCornerHeight);
    	g.drawLine(bagTipRx, bagTipY + tipCornerHeight, bagTipRx, bagTipY + bagTipHeight - tipCornerHeight);
    	
    	//箭头
    	res_Image.drawFrame(g, RES_UI_TIP_ARROW, arrowX, arrowY - tipArrowHeight, TRANS_ROT180 , G_TOPRIGHT);

    	//四个角
    	Tool.drawBoxCorner(g, bagTipX, bagTipY, bagTipWidth, bagTipHeight, res_Image, RES_UI_TIP_TOPRIGHT);
    	//画提示描述文字
    	g.setColor(0x0);
    	for(int i=0;i<tipRows;i++){
    		if(tipStr[i] != null){
    			drawMixedText(g, tipStr[i], bagTipX + tipCornerWidth, bagTipY + Utilities.font.getHeight() * i, 0, 0,tipStr[i].indexOf("{x}") != -1, G_TOPLEFT);				
    		}
    	}
    }

    /**
     * 指定宽度拼接一行图片
     * @param g
     * @param x
     * @param y
     * @param width
     * @param image
     * @param index
     * @param trans
     */
    public static void drawSpellRow(Graphics g, int x, int y, int width, ImageSet image, int index, int trans){
    	//#if opengl == true
    	//# if(Canvas.openglMode){
    	//# 	int height;
    	//# 	if(trans == TRANS_MIRROR_ROT270 || trans == TRANS_MIRROR_ROT90 || trans == TRANS_ROT270 || trans == TRANS_ROT90){
    	//# 		height = image.getFrameWidth(index);
    	//# 	} else {
    	//# 		height = image.getFrameHeight(index);
    	//#     } 
    	//# 	image.drawFrame(g, index, x, y, trans, Graphics.TOP|Graphics.LEFT, width, height);
    	//# 	return;
    	//# }
    	//#endif
    	
    	int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        int imageWidth = image.getFrameWidth(index);
        int imageHeight = image.getFrameHeight(index);
        
        if(trans == TRANS_MIRROR_ROT270 || trans == TRANS_MIRROR_ROT90 || trans == TRANS_ROT270 || trans == TRANS_ROT90){
        	int tmp = imageHeight;
        	imageHeight = imageWidth;
        	imageWidth = tmp;
        }

        int imageCount = width / imageWidth;

        if(width % imageWidth > 0){
            imageCount++;
        }

        g.clipRect(x, y, width, imageHeight);

        int cx = x;

        for(int i = 0; i < imageCount; i++){
            image.drawFrame(g, index, cx, y, trans);
            cx += imageWidth;
        }

        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    /**
     * 指定高度拼接一列图片
     * @param g
     * @param x
     * @param y
     * @param height
     * @param image
     * @param index
     * @param trans
     */
    public static void drawSpellCol(Graphics g, int x, int y, int height, ImageSet image, int index, int trans){
    	//#if opengl == true
    	//# if(Canvas.openglMode){
    	//# 	int width;
    	//# 	if(trans == TRANS_MIRROR_ROT270 || trans == TRANS_MIRROR_ROT90 || trans == TRANS_ROT270 || trans == TRANS_ROT90){
    	//# 		width = image.getFrameHeight(index);
    	//# 	} else {
    	//# 		width = image.getFrameWidth(index);
    	//#     } 
    	//# 	image.drawFrame(g, index, x, y, trans, Graphics.TOP|Graphics.LEFT, width, height);
    	//# 	return;
    	//# }
    	//#endif
    	int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        int imageWidth = image.getFrameWidth(index);
        int imageHeight = image.getFrameHeight(index);
        
        if(trans == TRANS_MIRROR_ROT270 || trans == TRANS_MIRROR_ROT90 || trans == TRANS_ROT270 || trans == TRANS_ROT90){
        	int tmp = imageHeight;
        	imageHeight = imageWidth;
        	imageWidth = tmp;
        }

        int imageCount = height / imageHeight;

        if(height % imageHeight > 0){
            imageCount++;
        }

        g.clipRect(x, y, imageWidth, height);

        int cy = y;

        for(int i = 0; i < imageCount; i++){
            image.drawFrame(g, index, x, cy, trans);
            cy += imageHeight;
        }

        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    /**
     * 指定一个区域自动拼接图片
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     * @param image
     * @param index
     * @param trans
     */
    public static void drawSpellArea(Graphics g, int x, int y, int width, int height, ImageSet image, int index, int trans){
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        int imageWidth = image.getFrameWidth(index);
        int imageHeight = image.getFrameHeight(index);

        int imageColCount = width / imageWidth;

        if(width % imageWidth > 0){
            imageColCount++;
        }

        int imageRowCount = height / imageHeight;

        if(height % imageHeight > 0){
            imageRowCount++;
        }

        g.clipRect(x, y, width, height);

        int cx = x;
        int cy = y;

        for(int i = 0; i < imageRowCount; i++){
            cx = x;

            for(int j = 0; j < imageColCount; j++){
                image.drawFrame(g, index, cx, cy, trans);
                cx += imageWidth;
            }

            cy += imageHeight;
        }

        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    public static final int DRAWBACK_TYPE_NORMAL = 0;
    public static final int DRAWBACK_TYPE_GARY = 1;
    public static final int DRAWBACK_TYPE_HIGHLIGHT = 2;
    public static final int DRAWBACK_TYPE_DARKLIGHT = 3;
    public static final int DRAWBACK_TYPE_GREEN = 4;
    
    public static final int[] DRAWBACK_TYPE_NORMAL_DATA = {
        0xF9D597, 0xC07425, 0xE2A45C, 0xE3B366, 0xE8C590
    };

    public static void drawBack(Graphics g, int x, int y, int width, int height){
        drawBack(g, x, y, width, height, DRAWBACK_TYPE_NORMAL, 0);
    }

    public static void drawBack(Graphics g, int x, int y, int width, int height, int type, int value){
        int[] c = null;

        switch(type){
            case DRAWBACK_TYPE_GREEN:
                c = new int[]{
                                0x97f9a0, 0x24bf34, 0x5de36a, 0x66e373, 0x90e899
                };
                for(int i = 0; i < c.length; i++){
                    c[i] = PipImage.lighter(c[i], value);
                }
                break;
            case DRAWBACK_TYPE_NORMAL:
                c = DRAWBACK_TYPE_NORMAL_DATA;
                break;
            case DRAWBACK_TYPE_GARY:
                c = new int[]{
                                0xcccccc, 0x737373, 0xa0a0a0, 0xa9a9a9, 0xbfbfbf
                };

                if(value != 0)
                    for(int i = 0; i < c.length; i++){
                        c[i] = PipImage.lighter(c[i], value);
                    }
                break;
            case DRAWBACK_TYPE_HIGHLIGHT: {
                c = new int[DRAWBACK_TYPE_NORMAL_DATA.length];
                System.arraycopy(DRAWBACK_TYPE_NORMAL_DATA, 0, c, 0, DRAWBACK_TYPE_NORMAL_DATA.length);
                for(int i = 0; i < c.length; i++){
                    c[i] = PipImage.lighter(c[i], value);
                }
            }
                break;
            case DRAWBACK_TYPE_DARKLIGHT:
                c = new int[DRAWBACK_TYPE_NORMAL_DATA.length];
                System.arraycopy(DRAWBACK_TYPE_NORMAL_DATA, 0, c, 0, DRAWBACK_TYPE_NORMAL_DATA.length);
                for(int i = 0; i < c.length; i++){
                    c[i] = PipImage.darker(c[i], value);
                }
                break;
        }

        g.setColor(c[0]);
        g.fillRect(x, y, width, height);
        g.setColor(c[1]);
        g.drawRect(x, y, width, height);
        g.setColor(c[2]);
        g.drawRect(x + 1, y + 1, width - 2, height - 2);
        g.setColor(c[3]);
        g.drawRect(x + 2, y + 2, width - 4, height - 4);
        g.setColor(c[4]);
        g.drawRect(x + 3, y + 3, width - 6, height - 6);
    }

    public static String isNumString(String s){
        int l1 = s.indexOf("<i>");
        int l2 = s.indexOf("</i>");

        if(l1 != -1 && l2 != -1){
            return s.substring(l1 + 3, l2);
        }else{
            return null;
        }
    }

    /** Light添加，绘制一个不折行的多媒体文本，支持格式为<cxxxxx>xxx</c>格式的字体颜色，也支持颜色字符串。
     * 
     * @param g
     * @param obj
     * @param font
     * @param x
     * @param y
     * @param line
     * @param color
     * @param anchor
     */
    public static int drawMixedText(Graphics g, String str, int x, int y, int color, int bkColor, boolean use3dString, int anchor){
    	Vector vec = formatString(str, 100000, Utilities.font, true);
    	return drawMixedText(g, vec, x, y, color, bkColor, use3dString, anchor);
    }
    
    public static int drawMixedText(Graphics g, String str, int x, int y, int color, int bkColor, boolean use3dString, int anchor, Font font){
    	Vector vec = formatString(str, 100000, font, true);
    	return drawMixedText(g, vec, x, y, color, bkColor, use3dString, anchor);
    }
    
    public static int drawMixedText(Graphics g, Vector vec, int x, int y, int color, int bkColor, boolean use3dString, int anchor){
    	return drawMixedText(g, vec, x, y, color, bkColor, use3dString, anchor, Utilities.font);
    }
    
    private static int textTick = 0;
    public static int drawMixedText(Graphics g, Vector vec, int x, int y, int color, int bkColor, boolean use3dString, int anchor, Font font){
        int count = vec.size();
        boolean oldUse3dString = use3dString;
        for(int i = 0; i < count; i++){
            Object[] sec = (Object[]) vec.elementAt(i);
            int secColor = color;
            int picFlag = 0;
            boolean isSec2String = true;
            if(sec[1] != null){
                picFlag = ((Integer) sec[1]).intValue();
                if(picFlag != -1){
                    secColor = picFlag;
                }else{
                    //表示是图片
                    //#if NewUI2
                	//# if(g != null){
                    //# int frameWidth = 0;
                    //# 	if(sec[2] instanceof PipAnimateSet){
                    //# 		PipAnimateSet pset = (PipAnimateSet)sec[2];
                    //# 		int[] box = pset.getAnimateSize(((Integer) sec[4]).intValue());
                    //# 		int len = pset.getAnimateLength(((Integer) sec[4]).intValue());
                    //# 		pset.drawAnimateFrame(g, ((Integer) sec[4]).intValue(), GameMain.semiTick % len, x + (box[2] >> 1) - (box[0] + (box[2]>>1)), y + (box[3] >> 1) - (box[1] + (box[3]>>1)) + ((font.getHeight() - box[3]) >> 1));
                    //# 		
                    //# 		frameWidth = box[2];
                    //# 	} else {
                    //# 		((ImageSet) sec[2]).drawFrame(g, ((Integer) sec[4]).intValue(), x, y + (font.getHeight()) / 2, 0, Graphics.LEFT | Graphics.VCENTER);
                    //# 		frameWidth = ((ImageSet) sec[2]).getFrameWidth(((Integer) sec[4]).intValue());
                    //# 		if(sec.length > 6 && sec[5] != null){
                    //#         	((ImageSet) sec[5]).drawFrame(g, ((Integer) sec[7]).intValue(), x + ((Integer)sec[6]).intValue(), y + (font.getHeight()) / 2, 0, Graphics.LEFT | Graphics.VCENTER);
                    //#        }
                    //# 	}
                    //# x += frameWidth;
                	//# continue;
                    //# }
                    //#else
                	if(g != null){
                        ((ImageSet) sec[2]).drawFrame(g, ((Integer) sec[4]).intValue(), x, y + (font.getHeight()) / 2, 0, Graphics.LEFT | Graphics.VCENTER);
                        if(sec.length > 6){
                        	((ImageSet) sec[5]).drawFrame(g, ((Integer) sec[7]).intValue(), x + ((Integer)sec[6]).intValue(), y + (font.getHeight()) / 2, 0, Graphics.LEFT | Graphics.VCENTER);
                        }
                    }
                    x += ((ImageSet) sec[2]).getFrameWidth(((Integer) sec[4]).intValue());
                    continue;
                    //#endif
                }
            }
            String text = (String) sec[2];
            if(sec[5] != null && ((Integer) (sec[5])).intValue() == 1){
                use3dString = false;
            }else if(sec[5] != null && ((Integer) (sec[5])).intValue() == 2){
                use3dString = true;
            }else{
                use3dString = oldUse3dString;
            }
            x = drawMoneyString(g, text, x, y, anchor, secColor, bkColor, use3dString, true, font);
        }
        return x;
    }

    public static int drawMoneyString(Graphics g, String str, int x, int y, int anchor, int color, int bkColor, boolean use3dString, boolean isShort, Font font){
        int ret = x;
        try {
        	if(str == null || str.length()==0){
            	return ret;
            }
            String ns = isNumString(str);

            if(ns == null){
                if(g != null){
                    if(use3dString){
                        Tool.draw3DString(g, str, x, y, color, bkColor, anchor);
                    }else{
                        g.setColor(color);
                        g.drawString(str, x, y, anchor);
                    }
                }
                ret += font.stringWidth(str);
            }else{
            	if(ns == null || ns.length()==0){
                	return ret;
                }
                ret += Tool.drawImageNumber(g, GameMain.numberImage, GameMain.numberImageIndex, ns, ret, y + Utilities.LINE_HEIGHT / 2, 0, Graphics.LEFT | Graphics.VCENTER) + 2;
            }
            return ret;
		} catch (Exception e) {
			//#ifdef buildtest
			e.printStackTrace();
			//#endif
			return ret;
		}
    }

    /*
     * 取指定长度的名称
     */
    public static String getName(String name, int width){
        int w = Utilities.font.stringWidth(name);
        boolean changed = false;
        while(w > width){
            name = name.substring(0, name.length() - 1);
            w = Utilities.font.stringWidth(name + "..");
            changed = true;
        }
        if(changed)
            name += "..";
        return name;
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, int color){
        g.setFont(Utilities.font);
        g.setColor(0x000000);
        g.drawString(s, x + 1, y + 1, Graphics.LEFT | Graphics.TOP);
        g.setColor(color);
        g.drawString(s, x, y, Graphics.LEFT | Graphics.TOP);
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, boolean selected){
        drawShadowString(g, s, x, y, selected? 0xFFFF00: 0xFFFFFF);
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, int anchor, int color){
        g.setFont(Utilities.font);
        g.setColor(0x000000);
        g.drawString(s, x + 1, y + 1, anchor);
        g.setColor(color);
        g.drawString(s, x, y, anchor);
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, int anchor, boolean selected){
        g.setFont(Utilities.font);
        g.setColor(0x000000);
        g.drawString(s, x + 1, y + 1, anchor);
        g.setColor(selected? 0xFFFF00: 0xFFFFFF);
        g.drawString(s, x, y, anchor);
    }

    public static void drawString(Graphics g, String s, int x, int y, boolean selected){
        drawShadowString(g, s, x, y, selected);
    }

    public static int getNextSelect(int preSelect, int arrLen){
        int select = ++preSelect;
        if(select >= arrLen)
            select = 0;
        return select;
    }

    public static int getPreSelect(int preSelect, int arrLen){
        int select = --preSelect;
        if(select < 0)
            select = (byte) (arrLen - 1);
        return select;
    }

    /**
     * 指定倍数放缩一张图片
     * @param srcImg
     * @param scalePercent
     * @return
     */
    public static Image zoomImage(Image srcImg, int scalePercent){
        return zoomImage(srcImg, srcImg.getWidth() * scalePercent / 100, srcImg.getHeight() * scalePercent / 100);
    }

    /**
     * 指定目标大小放缩一张图片
     * @param srcImg
     * @param desW
     * @param desH
     * @return
     */
    public static Image zoomImage(Image srcImg, int desW, int desH){
        int srcW = srcImg.getWidth(); //原始图像宽
        int srcH = srcImg.getHeight(); //原始图像高

        int[] srcBuf = new int[srcW * srcH]; //原始图片像素信息缓存

        //srcBuf获取图片像素信息
        srcImg.getRGB(srcBuf, 0, srcW, 0, 0, srcW, srcH);

        //计算插值表
        int[] tabY = new int[desH];
        int[] tabX = new int[desW];

        int sb = 0;
        int db = 0;
        int tems = 0;
        int temd = 0;
        int distance = srcH > desH? srcH: desH;

        for(int i = 0; i <= distance; i++){ /*垂直方向*/
            tabY[db] = (short) sb;
            tems += srcH;
            temd += desH;

            if(tems > distance){
                tems -= distance;
                sb++;
            }

            if(temd > distance){
                temd -= distance;
                db++;
            }
        }

        sb = 0;
        db = 0;
        tems = 0;
        temd = 0;
        distance = srcW > desW? srcW: desW;

        for(int i = 0; i <= distance; i++){ /*水平方向*/
            tabX[db] = (short) sb;
            tems += srcW;
            temd += desW;

            if(tems > distance){
                tems -= distance;
                sb++;
            }

            if(temd > distance){
                temd -= distance;
                db++;
            }
        }

        //生成放大缩小后图形像素buf
        int[] desBuf = new int[desW * desH];
        int dx = 0;
        int dy = 0;
        int sx = 0;
        int sy = 0;
        int oldy = -1;

        for(int i = 0; i < desH; i++){
            if(oldy == tabY[i]){
                System.arraycopy(desBuf, dy - desW, desBuf, dy, desW);
            }else{
                dx = 0;

                for(int j = 0; j < desW; j++){
                    desBuf[dy + dx] = srcBuf[sy + tabX[j]];
                    dx++;
                }

                sy += (tabY[i] - oldy) * srcW;
            }

            oldy = tabY[i];
            dy += desW;
        }

        //生成图片
        return Image.createRGBImage(desBuf, desW, desH, true);
    }

    //原Gzip
    // M醩caras para el flag.
    private static final int FTEXT_MASK = 1;
    private static final int FHCRC_MASK = 2;
    private static final int FEXTRA_MASK = 4;
    private static final int FNAME_MASK = 8;
    private static final int FCOMMENT_MASK = 16;
    // Tipos de bloques.
    private static final int BTYPE_NONE = 0;
    private static final int BTYPE_FIXED = 1;
    private static final int BTYPE_DYNAMIC = 2;
    private static final int BTYPE_RESERVED = 3;
    // L韒ites.
    private static final int MAX_BITS = 16;
    private static final int MAX_CODE_LITERALS = 287;
    private static final int MAX_CODE_DISTANCES = 31;
    private static final int MAX_CODE_LENGTHS = 18;
    private static final int EOB_CODE = 256;
    // Datos prefijados (LENGTH: 257..287 / DISTANCE: 0..29 / DYNAMIC_LENGTH_ORDER: 0..18).
    private static final int LENGTH_EXTRA_BITS[] = {
                    0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 99, 99
    };
    private static final int LENGTH_VALUES[] = {
                    3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 0, 0
    };
    private static final int DISTANCE_EXTRA_BITS[] = {
                    0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
    };
    private static final int DISTANCE_VALUES[] = {
                    1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
    };
    private static final int DYNAMIC_LENGTH_ORDER[] = {
                    16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
    };

    /*************************************************************************/

    // Variables para la lectura de datos comprimidos.
    private static int gzipIndex, gzipByte, gzipBit;

    /*************************************************************************/
    /*************************************************************************/
//#if NewUI2
  //#	public static synchronized byte[] inflate(byte gzip[]) throws IOException{
  //#		byte[] unzipBytes = null;
  //#		DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(gzip))));
  //#		
  //#		ByteArrayOutputStream baos = new ByteArrayOutputStream();
  //#		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
  //#		byte[] buff = new byte[256];
  //#		int size = 0;
  //#		while((size = dis.read(buff)) != -1){
  //#			dos.write(buff, 0, size);
  //#		}
  //#		dos.flush();
		
  //#		dis.close();
  //#		unzipBytes = baos.toByteArray();
  //#		dos.close();
  //#		return unzipBytes;
  //#	}

//#else
    /**
     * Descomprime un fichero GZIP.
     *
     * @param gzip Array con los datos del fichero comprimido
     *
     * @return Array con los datos descomprimidos
     */
    public static synchronized byte[] inflate(byte gzip[]) throws IOException{
        // Inicializa.
        gzipIndex = gzipByte = gzipBit = 0;
        // Cabecera.
        if(readBits(gzip, 16) != 0x8B1F || readBits(gzip, 8) != 8)
            throw new IOException("Invalid GZIP format");
        // Flag.
        int flg = readBits(gzip, 8);
        // Fecha(4) / XFL(1) / OS(1).
        gzipIndex += 6;
        // Comprueba los flags.
        if((flg & FEXTRA_MASK) != 0)
            gzipIndex += readBits(gzip, 16);
        if((flg & FNAME_MASK) != 0)
            while(gzip[gzipIndex++] != 0)
                ;
        if((flg & FCOMMENT_MASK) != 0)
            while(gzip[gzipIndex++] != 0)
                ;
        if((flg & FHCRC_MASK) != 0)
            gzipIndex += 2;
        // Tama駉 de los datos descomprimidos.
        int index = gzipIndex;
        gzipIndex = gzip.length - 4;
        byte uncompressed[] = new byte[readBits(gzip, 16) | (readBits(gzip, 16) << 16)];
        int uncompressedIndex = 0;
        gzipIndex = index;
        // Bloque con datos comprimidos.
        int bfinal = 0, btype = 0;
        do{
            // Lee la cabecera del bloque.
            bfinal = readBits(gzip, 1);
            btype = readBits(gzip, 2);
            // Comprueba el tipo de compresi髇.
            if(btype == BTYPE_NONE){
                // Ignora los bits dentro del byte actual.
                gzipBit = 0;
                // LEN.
                int len = readBits(gzip, 16);
                // NLEN.
                int nlen = readBits(gzip, 16);
                // Lee los datos.
                System.arraycopy(gzip, gzipIndex, uncompressed, uncompressedIndex, len);
                gzipIndex += len;
                // Actualiza el 韓dice de los datos descomprimidos.
                uncompressedIndex += len;
            }else{
                int literalTree[], distanceTree[];
                if(btype == BTYPE_DYNAMIC){
                    // N鷐ero de datos de cada tipo.
                    int hlit = readBits(gzip, 5) + 257;
                    int hdist = readBits(gzip, 5) + 1;
                    int hclen = readBits(gzip, 4) + 4;
                    // Lee el n鷐ero de bits para cada c骴igo de longitud.
                    byte lengthBits[] = new byte[MAX_CODE_LENGTHS + 1];
                    for(int i = 0; i < hclen; i++)
                        lengthBits[DYNAMIC_LENGTH_ORDER[i]] = (byte) readBits(gzip, 3);
                    // Crea los c骴igos para la longitud.
                    int lengthTree[] = createHuffmanTree(lengthBits, MAX_CODE_LENGTHS);
                    // Genera los 醨boles.
                    literalTree = createHuffmanTree(decodeCodeLengths(gzip, lengthTree, hlit), hlit - 1);
                    distanceTree = createHuffmanTree(decodeCodeLengths(gzip, lengthTree, hdist), hdist - 1);
                }else{
                    byte literalBits[] = new byte[MAX_CODE_LITERALS + 1];
                    for(int i = 0; i < 144; i++)
                        literalBits[i] = 8;
                    for(int i = 144; i < 256; i++)
                        literalBits[i] = 9;
                    for(int i = 256; i < 280; i++)
                        literalBits[i] = 7;
                    for(int i = 280; i < 288; i++)
                        literalBits[i] = 8;
                    literalTree = createHuffmanTree(literalBits, MAX_CODE_LITERALS);
                    //
                    byte distanceBits[] = new byte[MAX_CODE_DISTANCES + 1];
                    for(int i = 0; i < distanceBits.length; i++)
                        distanceBits[i] = 5;
                    distanceTree = createHuffmanTree(distanceBits, MAX_CODE_DISTANCES);
                }
                // Descomprime el bloque.
                int code = 0, leb = 0, deb = 0;
                while((code = readCode(gzip, literalTree)) != EOB_CODE){
                    if(code > EOB_CODE){
                        code -= 257;
                        int length = LENGTH_VALUES[code];
                        if((leb = LENGTH_EXTRA_BITS[code]) > 0)
                            length += readBits(gzip, leb);
                        code = readCode(gzip, distanceTree);
                        int distance = DISTANCE_VALUES[code];
                        if((deb = DISTANCE_EXTRA_BITS[code]) > 0)
                            distance += readBits(gzip, deb);
                        // Repite la informaci髇.
                        int offset = uncompressedIndex - distance;
                        while(distance < length){
                            System.arraycopy(uncompressed, offset, uncompressed, uncompressedIndex, distance);
                            uncompressedIndex += distance;
                            length -= distance;
                            distance <<= 1;
                        }
                        System.arraycopy(uncompressed, offset, uncompressed, uncompressedIndex, length);
                        uncompressedIndex += length;
                    }else
                        uncompressed[uncompressedIndex++] = (byte) code;
                }
            }
        }while(bfinal == 0);
        //
        return uncompressed;
    }
//#endif
    
    /**
     * Lee un n鷐ero de bits
     *
     * @param n N鷐ero de bits [0..16]
     */
    private static int readBits(byte gzip[], int n){
        // Asegura que tenemos un byte.
        int data = (gzipBit == 0? (gzipByte = (gzip[gzipIndex++] & 0xFF)): (gzipByte >> gzipBit));
        // Lee hasta completar los bits.
        for(int i = (8 - gzipBit); i < n; i += 8){
            gzipByte = (gzip[gzipIndex++] & 0xFF);
            data |= (gzipByte << i);
        }
        // Ajusta la posici髇 actual.
        gzipBit = (gzipBit + n) & 7;
        // Devuelve el dato.
        return(data & ((1 << n) - 1));
    }

    /**
     * Lee un c骴igo.
     */
    private static int readCode(byte gzip[], int tree[]){
        int node = tree[0];
        while(node >= 0){
            // Lee un byte si es necesario.
            if(gzipBit == 0)
                gzipByte = (gzip[gzipIndex++] & 0xFF);
            // Accede al nodo correspondiente.
            node = (((gzipByte & (1 << gzipBit)) == 0)? tree[node >> 16]: tree[node & 0xFFFF]);
            // Ajusta la posici髇 actual.
            gzipBit = (gzipBit + 1) & 7;
        }
        return(node & 0xFFFF);
    }

    /**
     * Decodifica la longitud de c骴igos (usado en bloques comprimidos con c骴igos din醡icos).
     */
    private static byte[] decodeCodeLengths(byte gzip[], int lengthTree[], int count){
        byte bits[] = new byte[count];
        for(int i = 0, code = 0, last = 0; i < count;){
            code = readCode(gzip, lengthTree);
            if(code >= 16){
                int repeat = 0;
                if(code == 16){
                    repeat = 3 + readBits(gzip, 2);
                    code = last;
                }else{
                    if(code == 17)
                        repeat = 3 + readBits(gzip, 3);
                    else
                        repeat = 11 + readBits(gzip, 7);
                    code = 0;
                }
                while(repeat-- > 0)
                    bits[i++] = (byte) code;
            }else
                bits[i++] = (byte) code;
            //
            last = code;
        }
        return bits;
    }

    /**
     * Crea el 醨bol para los c骴igos Huffman.
     */
    private static int[] createHuffmanTree(byte bits[], int maxCode){
        // N鷐ero de c骴igos por cada longitud de c骴igo.
        int bl_count[] = new int[MAX_BITS + 1];
        for(int i = 0; i < bits.length; i++)
            bl_count[bits[i]]++;
        // M韓imo valor num閞ico del c骴igo para cada longitud de c骴igo.
        int code = 0;
        bl_count[0] = 0;
        int next_code[] = new int[MAX_BITS + 1];
        for(int i = 1; i <= MAX_BITS; i++)
            next_code[i] = code = (code + bl_count[i - 1]) << 1;
        // Genera el 醨bol.
        // Bit 31 => Nodo (0) o c骴igo (1).
        // (Nodo) bit 16..30 => 韓dice del nodo de la izquierda (0 si no tiene).
        // (Nodo) bit 0..15 => 韓dice del nodo de la derecha (0 si no tiene).
        // (C骴igo) bit 0..15
        int tree[] = new int[(maxCode << 1) + MAX_BITS];
        int treeInsert = 1;
        for(int i = 0; i <= maxCode; i++){
            int len = bits[i];
            if(len != 0){
                code = next_code[len]++;
                // Lo mete en en 醨bol.
                int node = 0;
                for(int bit = len - 1; bit >= 0; bit--){
                    int value = code & (1 << bit);
                    // Inserta a la izquierda.
                    if(value == 0){
                        int left = tree[node] >> 16;
                        if(left == 0){
                            tree[node] |= (treeInsert << 16);
                            node = treeInsert++;
                        }else
                            node = left;
                    }
                    // Inserta a la derecha.
                    else{
                        int right = tree[node] & 0xFFFF;
                        if(right == 0){
                            tree[node] |= treeInsert;
                            node = treeInsert++;
                        }else
                            node = right;
                    }
                }
                // Inserta el c骴igo.
                tree[node] = 0x80000000 | i;
            }
        }
        return tree;
    }

    //原KeyMaker
    private Hashtable maxKey = new Hashtable();
    private Integer key = new Integer(0);

    public Tool(){
        maxKey.put(key, key);
    }

    public int nextKey(){
        synchronized(maxKey){
            int k = ((Integer) maxKey.get(key)).intValue() + 1;
            maxKey.put(key, new Integer(k));
            return k;
        }
    }

    //原Const
    //常用颜色定义
    public static final int CL_BLACK = 0x000000;
    public static final int CL_DARKGRAY = 0x808080;
    public static final int CL_GRAY = 0xC0C0C0;
    public static final int CL_WHITE = 0xFFFFFF;
    public static final int CL_RED = 0xFF0000;
    public static final int CL_YELLOW = 0xFFFF00;
    public static final int CL_GREEN = 0x00FF00;
    public static final int CL_LIGHTBLUE = 0x00FFFF;
    public static final int CL_BLUE = 0x6fBBF9;
    public static final int CL_PURPLE = 0xFF00FF;
    public static final int CL_LIGHTYELLOW = 0xFFFF80;
    public static final int CL_LIGHTGREEN = 0x00FF80;
    public static final int CL_WHITEBLUE = 0x80FFFF;
    public static final int CL_DARKBLUE = 0x8080FF;
    public static final int CL_DARKRED = 0xFF0080;
    public static final int CL_BROWN = 0xFF8040;
    public static final int CL_PEPC = 0x3D2000;//纸上的选中颜色

    //绘画元素类型
    public static final byte DRAW_ITEMS_ROLE = 1; //主角
    public static final byte DRAW_ITEMS_GROUND_MAPNPC = 2; //地面层地图NPC
    public static final byte DRAW_ITEMS_ROLE_MAPNPC = 3; //人物层地图NPC
    public static final byte DRAW_ITEMS_SKY_MAPNPC = 4; //天空层地图NPC
    public static final byte DRAW_ITEMS_NPC = 5; //NPC
    public static final byte DRAW_ITEMS_PLAYER = 6; //其他玩家
    public static final byte DRAW_ITEMS_CREATURE = 7; //怪物
    public static final byte DRAW_ITEMS_CORPSE = 8; //尸体
    public static final byte DRAW_ITEMS_EXIT = 9; //出口
    public static final byte DRAW_ITEMS_LEAVING_SPRITE = 10; //需要播放走出视野的精灵
    public static final byte DRAW_ITEMS_ATTENDANT = 11; //随从

    public static final int DEFAULT_TILE_WIDTH = 16;
    public static final int DEFAULT_TILE_HEIGHT = 16;

    public static final int DRAW_ORDER_TOP = 0; //在整个屏幕最上层绘制
    public static final int DRAW_ORDER_FRONT = 1; //在精灵的前方绘制，但和精灵共享绘制时机
    public static final int DRAW_ORDER_BACK = 2; //在精灵后方绘制，但和精灵共享绘制时机
    public static final int DRAW_ORDER_TOP_TOP = 3; //在整个屏幕最上层绘制，对于头顶叹号之类再加一层，会更晚绘制

    public static final int CHECK_RESOURCE_SAVE_KEY = -2;

    /************************************精灵相关begin*************************************/

    //相对精灵的定位方式
    public static final int SPRITE_ANCHOR_BOX_TOP = 1;
    public static final int SPRITE_ANCHOR_BOX_BOTTOM = 2;
    public static final int SPRITE_ANCHOR_BOX_LEFT = 4;
    public static final int SPRITE_ANCHOR_BOX_RIGHT = 8;
    public static final int SPRITE_ANCHOR_BOX_HCENTER = 16;
    public static final int SPRITE_ANCHOR_BOX_VCENTER = 32;
    public static final int SPRITE_ANCHOR_X_REF = 64;
    public static final int SPRITE_ANCHOR_Y_REF = 128;
    public static final int SPRITE_ANCHOR_HEAD = 256;

    //精灵类型
    public static final byte VM_PROCESSOR_PANEL = -2;
    public static final byte VM_PROCESSOR_WORLD = -1;
    public static final byte VM_PROCESSOR_GAMESPRITE = -100; //gameSprite按照instanceid取
    public static final byte SPRITE_TYPE_ROLE = 0; //主角 客户端用
    public static final byte SPRITE_TYPE_PLAYER = 1; //玩家
    public static final byte SPRITE_TYPE_NPC = 3; //npc
    public static final byte SPRITE_TYPE_CREATURE = 2; //怪物
    public static final byte SPRITE_TYPE_CORPSE = 4; //尸体
    public static final byte SPRITE_TYPE_GATHER_NPC = 5; //采集NPC
    public static final byte SPRITE_TYPE_ATTENDANT = 6; //随从
    public static final byte SPRITE_TYPE_EXIT = 99; //门，客户端用
    public static final byte SPRITE_TYPE_ICON = 100; //图标，客户端用

    //方向定义
    public static final int DIR_NONE = -1;
    public static final int DIR_DOWN = 0;
    public static final int DIR_RIGHT = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_UP = 3;

    public static final int SUB_DIR_TOPRIGHT = 0;
    public static final int SUB_DIR_TOPLEFT = 1;
    public static final int SUB_DIR_BOTTOMRIGHT = 2;
    public static final int SUB_DIR_BOTTOMLEFT = 3;

    //路点模式状态
    public static final byte WAY_POINT_STATUS_IDLE = 0;
    public static final byte WAY_POINT_STATUS_DOING = 1;
    public static final byte WAY_POINT_STATUS_DONE = 2;

    //坐标轴定义
    public static final byte X_AXIS = 0;
    public static final byte Y_AXIS = 1;

    //头顶文字定位设置
    /**
     * 头顶文字类型：按精灵动画大小来设置
     */
    public static final byte HEAD_STRING_TYPE_DRAWAREA = 0;

    /**
     * 头顶文字类型：按配置的绝对x、y偏移来设置
     */
    public static final byte HEAD_STRING_TYPE_ABSOLUTE = 1;

    /**
     * 头顶文字类型：按精灵动画的大小来设置Y值，X值锁定动画参考点
     */
    public static final byte HEAD_STRING_TYPE_XAREA = 2;

    //画文字方式
    public static final byte DRAW_STRING_3D = 0;
    public static final byte DRAW_STRING_NORMAL = 1;

    /**
     * 无回调函数
     */
    public static final byte NO_CALL_BACK = -1;

    /**
     * 无回调参数
     */
    public static final IAnimateCallback NO_CALL_BACK_SPRITE = null;

    /**
     * 动画图片资源无替换
     */
    public static final byte ANIMATE_IMAGE_NO_REPLACE = -1;

    //动画播放方式
    public static final byte ANIMATE_PLAY_TYPE_ALWAYS = 0;
    public static final byte ANIMATE_PLAY_TYPE_ONCE = 1;
    public static final byte ANIMATE_PLAY_TYPE_HOLD = 2;

    //飘字方式
    public static final int FLYING_STRING_TYPE_NUMBER = 0;
    public static final int FLYING_STRING_TYPE_STRING = 1;
    public static final int FLYING_STRING_TYPE_3DSTRING = 2;

    /************************************精灵相关end***************************************/

    /************************************怪物相关begin*************************************/

    /************************************怪物相关end***************************************/

    /************************************人物相关begin*************************************/

    //攻击类型
    public static final int ATTACK_TYPE_WEAPON = 0;
    public static final int ATTACK_TYPE_BOW = 1;
    public static final int ATTACK_TYPE_MAGIC = 2;

    //特效所对应的动画编号
    public static final int ANIMATE_EFFECT_MAGIC = 0;
    public static final int ANIMATE_EFFECT_POSION = 1;
    public static final int ANIMATE_EFFECT_MAGIC_HIT = 2;
    public static final int ANIMATE_EFFECT_ATTACK_HIT = 3;
    public static final int ANIMATE_EFFECT_ARROW_HIT = 4;

    //动画名称
    public static final String ANIMATE_SPRITE_ROLE_MALE = "male.ctn";
    public static final String ANIMATE_SPRITE_ROLE_HORSE = "horse.ctn";
    public static final String ANIMATE_SPRITE_ROLE_EFFECT = "effect.ctn";
    public static final String ANIMATE_SPRITE_ROLE_MAGIC = "magic.ctn";

    //人物动画图片中的图片帧定义，用于换装
    public static final int ANIMATE_IMAGE_ID_BODY1 = 0;
    public static final int ANIMATE_IMAGE_ID_BODY2 = 1;
    public static final int ANIMATE_IMAGE_ID_ATTACK = 2;
    public static final int ANIMATE_IMAGE_ID_LIMB1 = 3;
    public static final int ANIMATE_IMAGE_ID_LIMB2 = 4;
    public static final int ANIMATE_IMAGE_ID_HORSE = 5;
    public static final int ANIMATE_IMAGE_ID_WEAPON = 6;
    public static final int ANIMATE_IMAGE_FRAME_ID_HANDLE = 0;
    public static final int ANIMATE_IMAGE_FRAME_ID_WEAPON = 1;
    public static final int ANIMATE_IMAGE_FRAME_ID_HEAD_1 = 0;
    public static final int ANIMATE_IMAGE_FRAME_ID_HEAD_2 = 1;
    public static final int ANIMATE_IMAGE_FRAME_ID_HEAD_3 = 2;
    public static final int ANIMATE_IMAGE_FRAME_ID_BODY_1 = 3;
    public static final int ANIMATE_IMAGE_FRAME_ID_BODY_2 = 4;
    public static final int ANIMATE_IMAGE_FRAME_ID_BODY_3 = 5;
    public static final int ANIMATE_IMAGE_FRAME_ID_BODY_4 = 6;

    /************************************人物相关end***************************************/

    /************************************优化相关begin*************************************/

    /**
     * 移动矩阵，下右左上
     *              {
                                    0, -1
                    }, {
                                    -1, 0
                    }, {
                                    1, 0
                    }, {
                                    0, 1
                    }
     */
    public static final int[] BACK_MATRIX = new int[]{
                    0, -1, -1, 0, 1, 0, 0, 1
    };

    /**
     * 后背矩阵，下右左上
     *              {
                                    0, 1
                    }, {
                                    1, 0
                    }, {
                                    -1, 0
                    }, {
                                    0, -1
                    }
     */
    public static final int[] MOVE_MATRIX = new int[]{
                    0, 1, 1, 0, -1, 0, 0, -1
    };

    //左后
    public static final int[] ASSIS_LEFT_MATRIX = new int[]{
                    1, -1, -1, -1, 1, 1, -1, 1
    };

    //右后
    public static final int[] ASSIS_RIGHT_MATRIX = new int[]{
                    -1, -1, -1, 1, 1, -1, 1, 1
    };

    public static final int FOLLOW_DIS_X = 20;
    public static final int FOLLOW_DIS_Y = 20;

    /**
     * 方阵
     * 
     * (Const.FOLLOW_DIS_MATRIX.length)就是可添加跟随者的最大值
     * 
     * 方阵索引位置
     * 
     * 
     * 20     21      22      23       24
     * 
     * 
     * 
     * 12     13      14      15       19
     * 
     * 
     * 
     * 6      7       8       11       18
     *     
     *
     *     
     * 3      4   -   5       10       17
     *            |  
     *          disy
     *            |
     * 0      1   -   2       9        16 
     * |-disx-|
     * 
     */
    public static final int[] FOLLOW_DIS_MATRIX = {
                    /*0*/0, 0,
                    /*1*/FOLLOW_DIS_X, 0,
                    /*2*/FOLLOW_DIS_X << 1, 0,
                    /*3*/0, FOLLOW_DIS_Y,
                    /*4*/FOLLOW_DIS_X, FOLLOW_DIS_Y,
                    /*5*/FOLLOW_DIS_X << 1, FOLLOW_DIS_Y,
                    /*6*/0, FOLLOW_DIS_Y << 1,
                    /*7*/FOLLOW_DIS_X, FOLLOW_DIS_Y << 1,
                    /*8*/FOLLOW_DIS_X << 1, FOLLOW_DIS_Y << 1,

                    /*9*/FOLLOW_DIS_X * 3, 0,
                    /*10*/FOLLOW_DIS_X * 3, FOLLOW_DIS_Y,
                    /*11*/FOLLOW_DIS_X * 3, FOLLOW_DIS_Y << 1,
                    /*12*/0, FOLLOW_DIS_Y * 3,
                    /*13*/FOLLOW_DIS_X, FOLLOW_DIS_Y * 3,
                    /*14*/FOLLOW_DIS_X << 1, FOLLOW_DIS_Y * 3,
                    /*15*/FOLLOW_DIS_X * 3, FOLLOW_DIS_Y * 3,

                    /*16*/FOLLOW_DIS_X << 2, 0,
                    /*17*/FOLLOW_DIS_X << 2, FOLLOW_DIS_Y,
                    /*18*/FOLLOW_DIS_X << 2, FOLLOW_DIS_Y << 1,
                    /*19*/FOLLOW_DIS_X << 2, FOLLOW_DIS_Y * 3,
                    /*20*/0, FOLLOW_DIS_Y << 2,
                    /*21*/FOLLOW_DIS_X, FOLLOW_DIS_Y << 2,
                    /*22*/FOLLOW_DIS_X << 1, FOLLOW_DIS_Y << 2,
                    /*23*/FOLLOW_DIS_X * 3, FOLLOW_DIS_Y << 2,
                    /*24*/FOLLOW_DIS_X << 2, FOLLOW_DIS_Y << 2,

    };

    /**
     * 抖动方向矩阵
     */
    public static final int[] VIBRA_MATRIX = new int[]{
                    0, 1, 0, -1, 1, 0, -1, 0, -1, 0, 1, 0, 0, -1, 0, 1
    };

    /**
     * 正弦查找表(0-90度) 
     * 角度为数组的索引
     * 数值均扩大10000倍
     */
    public static final int[] SIN_TABLE = new int[]{
                    0, 1750, 3490, 5230, 6980, 8720, 1045, 1219, 1392, 1564, 1736, 1908, 2079, 2250, 2419, 2588, 2756, 2924, 3090, 3256, 3420, 3584, 3746, 3907, 4067, 4226, 4384, 4540, 4695, 4848,
                    5000, 5150, 5299, 5446, 5592, 5736, 5878, 6018, 6157, 6293, 6428, 6561, 6691, 6820, 6947, 7071, 7193, 7314, 7431, 7547, 7660, 7771, 7880, 7986, 8090, 8192, 8290, 8387, 8480, 8572,
                    8660, 8746, 8829, 8910, 8988, 9063, 9135, 9205, 9272, 9336, 9397, 9455, 9511, 9563, 9613, 9659, 9703, 9744, 9781, 9816, 9848, 9877, 9903, 9925, 9945, 9962, 9976, 9986, 9994, 9998,
                    10000,

    };

    /************************************优化相关end***************************************/

    //原GlobalVar
    private static Hashtable globalVars = new Hashtable();

    public static void setGlobalValue(String varName, int varValue){
        globalVars.put(varName, new Integer(varValue));
    }

    public static void setGlobalValue(String varName, Object varValue){
        globalVars.put(varName, varValue);
    }

    public static void deleteGlobalVar(String varName){
        globalVars.remove(varName);
    }
    
    public static void clearGlobalVar(){
    	globalVars.clear();
    }

    public static int getGlobalInt(String varName){
        int intValue = 0;
        if(globalVars.containsKey(varName)){
            intValue = ((Integer) (globalVars.get(varName))).intValue();
        }else{
            intValue = 0;
        }
        return intValue;
    }

    public static String getGlobalString(String varName){
        String str = "";
        if(globalVars.containsKey(varName)){
            str = (String) (globalVars.get(varName));
        }else{
            str = "";
        }
        return str;
    }

    public static Object getGlobalObject(String varName){
        return globalVars.get(varName);
    }

    //原RmsUtil
    public static int addRecord(String rmsName, byte[] data){
        RecordStore rs = null;
        int result = -1;

        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            result = rs.addRecord(data, 0, data.length);
        }catch(Exception e){
            //#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }

        return result;
    }

    public static boolean saveRecord(String rmsName, int recordId, byte[] data){
        RecordStore rs = null;
        boolean result = false;

        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            rs.setRecord(recordId, data, 0, data.length);
            result = true;
        }catch(Exception e){
            //#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }

        return result;
    }

    public static byte[] loadRecord(String rmsName, int recordId){
        RecordStore rs = null;
        byte[] result = null;

        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            result = rs.getRecord(recordId);
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }

        return result;
    }

    public static boolean deleteRecord(String rmsName, int recordId){
        RecordStore rs = null;
        boolean result = false;

        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            rs.deleteRecord(recordId);
            result = true;
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }

        return result;
    }

    public static boolean saveData(String rmsName, byte[] data, byte index){
        RecordStore rs = null;
        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            int recordId = -1;
            while(re.hasNextElement()){
                int id = re.nextRecordId();
                byte[] record = rs.getRecord(id);
                if(record.length > 0 && record[0] == index){
                    recordId = id;
                    break;
                }
            }
            re.destroy();
            byte[] newdata = new byte[data.length + 1];
            System.arraycopy(data, 0, newdata, 1, data.length);
            newdata[0] = index;
            if(recordId == -1){
                rs.addRecord(newdata, 0, newdata.length);
            }else{
                rs.setRecord(recordId, newdata, 0, newdata.length);
            }
            return true;
        }catch(Exception e){
            return false;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    public static byte[] getData(String rmsName, byte index){
        RecordStore rs = null;
        byte[] ret = null;
        try{
            rs = RecordStore.openRecordStore(rmsName, false);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            while(re.hasNextElement()){
                byte[] record = re.nextRecord();
                if(record.length > 0 && record[0] == index){
                    ret = record;
                    break;
                }
            }
            re.destroy();
            if(ret != null){
                byte[] ret1 = new byte[ret.length - 1];
                System.arraycopy(ret, 1, ret1, 0, ret.length - 1);
                ret = ret1;
            }
            return ret;
        }catch(Exception e){
            return null;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    public static boolean deleteData(String rmsName, byte index){
        RecordStore rs = null;
        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            int recordId = -1;
            while(re.hasNextElement()){
                int id = re.nextRecordId();
                byte[] record = rs.getRecord(id);
                if(record.length > 0 && record[0] == index){
                    recordId = id;
                    break;
                }
            }
            re.destroy();
            if(recordId != -1){
                rs.deleteRecord(recordId);
            }
            return true;
        }catch(Exception e){
            return false;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    public static void deleteRMSFile(String dbName){
        try{
            RecordStore.deleteRecordStore(dbName);
        }catch(Exception e){

        }
    }

    //原StringUtil
    /**
     * 对一个混合格式的字符串进行排版. 混合格式的字符串是在普通字符串中加入格式控制指令形成的, 目前支持的格式控制指令有两种: 颜色, 金钱和高亮.
     * 要在混合格式字符串中插入一个带颜色的字符串的格式是: <cFF0000>红色字符</c>. 其中<c>标签中是16进制的颜色值.
     * 要插入金钱的格式是: $3 200. 这个例子中3是后续的金额字符串的长度, 在3和200之间必须有一个空格.
     * 要使某一行高亮, 在行头插入一个字符"|".
     * @param s 混合格式字符串
     * @param width 排版宽度
     * @param font 排版字体
     * @return 返回字符串分段的数组. 注意在混合格式字符串中, 一行可能包括1到多个分段. 每个分段是一个有6个元素的Object数组, 其中:
     *    第一个元素: Integer, 分段所在行号,从0开始;
     *    第二个元素: Integer, 分段的颜色, null表示没有指定, 使用缺省颜色. -1表示一个图片
     *    第三个元素: String, 分段的字符串, 如果这一段是一个金钱, 则这个字符串包括前面的$n控制字符。如果是图片则表示ImageSet
     *    第四个元素: Integer, 该分段的X位置
     *    第五个元素: Integer, 本分段是否高亮, null表示否， 如果是图片，表示图片索引
     *    第六个元素: Integer, 本分段是否强制3d效果,1:强制没有,2:强制有
     */
    public static Vector formatString(String s, int width, Font font, boolean isDraw){
        Vector ret = new Vector();

        int length = s.length();
        char ch;
        Integer currColor = null;
        boolean isForceNo3d = false;
        boolean force3d = false;
        int line = 0;
        int xOffset = 0;
        String currStr = "";
        int currentStrWid = 0;

        Object[] strObj = null;
        boolean currLineSelected = false;

        boolean tagEnd = false;
        for(int pos = 0; pos < length; pos++){
            ch = s.charAt(pos);

            //baiyang added
            //一个图片，格式是{#VarUIRes,bgindex,VarUIRes,fgindex}可以只有背景
            if(ch == '{' && s.charAt(pos + 1) == '#'){
                int endTagPos = s.indexOf('}', pos + 2);
                if(endTagPos != -1){
                    String picItem = s.substring(pos + 2, endTagPos);
                    String[] info = Tool.splitString(picItem, ',');
                    if(info.length >= 2){
                    	if(currentStrWid > 0){
                    	//#if NewUI2
                    	//#	strObj = new Object[9];
                    	//#else
                            strObj = new Object[6];
                        //#endif
                            strObj[0] = new Integer(line);
                            strObj[1] = currColor;
                            strObj[2] = currStr;
                            strObj[3] = new Integer(xOffset);
                            if(currLineSelected){
                                strObj[4] = new Integer(1);
                            }
                            xOffset += currentStrWid;
                            currStr = "";
                            ret.addElement(strObj);
                        }
                        //#if NewUI2
                        //# if(info.length == 2){
                    	//# 	strObj = new Object[9];
                    	//# } else if(info.length == 4){
                    	//# 	strObj = new Object[9];
                    	//# }
                        //#else
                    	if(info.length == 2){
                    		strObj = new Object[6];
                    	} else if(info.length == 4){
                    		strObj = new Object[8];
                    	}
                        //#endif
                        strObj[0] = new Integer(line);
                        strObj[1] = new Integer(-1); //图片标示
                        boolean error = false;
                        try{
                            strObj[2] = info[0]; //背景图片资源名
                            strObj[3] = new Integer(xOffset);
                            strObj[4] = new Integer(Integer.parseInt(info[1])); //图片索引
                            strObj[2] = Tool.getGlobalObject((String) strObj[2]);
                            
                            if(info.length == 4 && Integer.parseInt(info[3]) >= 0){
                                strObj[5] = info[2]; //前景景图片资源名
                                
                                strObj[7] = new Integer(Integer.parseInt(info[3])); //图片索引
                                strObj[5] = Tool.getGlobalObject((String) strObj[5]);
                                //修正位置
                                int bgw = ((ImageSet) strObj[2]).getFrameWidth(((Integer) strObj[4]).intValue());
                                int fgw = ((ImageSet) strObj[5]).getFrameWidth(((Integer) strObj[7]).intValue());
                                if(bgw > fgw){
                                	strObj[6] = new Integer((bgw - fgw) / 2);
                                } else {
                                	strObj[6] = new Integer((fgw - bgw) / 2);
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            error = true;
                        }
                        if(!error && strObj[2] != null){
                            // 计算其宽度看是否能放到当前行
                            //#if NewUI2
                            //# if(strObj[2] instanceof PipAnimateSet){
                        	//# 	PipAnimateSet pset = (PipAnimateSet)strObj[2];
                        	//# 	int[] box = pset.getAnimateSize(((Integer) strObj[4]).intValue());
                        	//# 	currentStrWid = box[2];
                        	//# 	strObj[8] = new Integer(box[3]);
                        	//# } else {
                        	//# 	if(strObj[5] != null){
                        	//# 		currentStrWid = ((ImageSet) strObj[5]).getFrameWidth(((Integer) strObj[7]).intValue());
                            //# 		strObj[8] = new Integer(((ImageSet) strObj[5]).getFrameHeight(((Integer) strObj[7]).intValue()));
                        	//# 	} else {
                        	//# 		currentStrWid = ((ImageSet) strObj[2]).getFrameWidth(((Integer) strObj[4]).intValue());
                            //# 		strObj[8] = new Integer(((ImageSet) strObj[2]).getFrameHeight(((Integer) strObj[4]).intValue()));
                        	//# 	}
                        	//# 	
                        	//# }
                            
                            //#else
                            currentStrWid = ((ImageSet) strObj[2]).getFrameWidth(((Integer) strObj[4]).intValue());
                            //#endif
                            if(xOffset + currentStrWid > width){
                                // 折行
                                xOffset = 0;
                                line++;
                            }
                            if(!isDraw){
                                strObj[2] = s.substring(pos, endTagPos + 1);
                            }
                            strObj[0] = new Integer(line);
                            strObj[3] = new Integer(xOffset);
                            xOffset += currentStrWid;
                            ret.addElement(strObj);
                            pos = endTagPos;
                            currentStrWid = 0;
                            continue;
                        }
                    }
                }

            }
            if(ch == '<'){
                //leo added
                if(s.charAt(pos + 1) == 'i'){
                    if(s.charAt(pos + 2) == '>'){ //确认是数字定义
                        int p2 = s.indexOf('<', pos + 3);
                        int p3 = s.indexOf("</i>", pos + 3);
                        if(p2 != -1 && p2 == p3){ //确认数字定义格式完整
                            String numStr = "";
                            int offset = 0;

                            while(s.charAt(pos + 3 + offset) != '<'){
                                numStr += s.charAt(pos + 3 + offset);
                                offset++;
                            }
                            boolean error = false;

                            try{
                                Integer.parseInt(numStr);
                            }catch(Exception e){
                                error = true;
                            }

                            if(error == false){
                                if(currentStrWid > 0){
                                    strObj = new Object[6];
                                    strObj[0] = new Integer(line);
                                    strObj[1] = currColor;
                                    strObj[2] = currStr;
                                    strObj[3] = new Integer(xOffset);
                                    if(currLineSelected){
                                        strObj[4] = new Integer(1);
                                    }
                                    xOffset += currentStrWid;
                                    currStr = "";
                                    ret.addElement(strObj);
                                }

                                currentStrWid = Tool.drawImageNumber(null, GameMain.numberImage, GameMain.numberImageIndex, numStr, 0, 0, 0, Graphics.TOP | Graphics.LEFT) + 2;
                                strObj = new Object[6];
                                strObj[1] = null;
                                strObj[2] = s.substring(pos, p3 + 4);
                                if(xOffset + currentStrWid > width){
                                    // 折行
                                    xOffset = 0;
                                    line++;
                                }
                                strObj[0] = new Integer(line);
                                strObj[3] = new Integer(xOffset);
                                xOffset += currentStrWid;
                                ret.addElement(strObj);
                                pos = p3 + 3;
                                currentStrWid = 0;
                                continue;
                            }
                        }
                    }
                }else if(s.charAt(pos + 1) == 'c'){//color tag start
                    //<cFFFFFF>
                    pos += 2;
                    String clrStr = "";
                    while(s.charAt(pos) != '>'){
                        clrStr += s.charAt(pos);
                        pos++;
                    }

                    if(currentStrWid > 0){
                        strObj = new Object[6];
                        strObj[0] = new Integer(line);
                        strObj[1] = currColor;
                        strObj[2] = currStr;
                        strObj[3] = new Integer(xOffset);
                        if(currLineSelected){
                            strObj[4] = new Integer(1);
                            currLineSelected = false;
                        }
                        xOffset += currentStrWid;
                        ret.addElement(strObj);
                    }

                    if("{x}".equals(s.substring(pos + 1, pos + 4))){
                        isForceNo3d = true;
                        pos += 3;
                    }else if("{d}".equals(s.substring(pos + 1, pos + 4))){
                        pos += 3;
                        force3d = true;
                    }else{
                        isForceNo3d = false;
                    }
                    currColor = new Integer(Integer.parseInt(clrStr, 16));
                    currStr = "";
                    currentStrWid = 0;
                    continue;
                }else if(s.charAt(pos + 1) == '/'){
                    //</c>
                    pos += 3;

                    if(currentStrWid > 0){
                        strObj = new Object[6];
                        strObj[0] = new Integer(line);
                        strObj[1] = currColor;
                        strObj[2] = currStr;
                        strObj[3] = new Integer(xOffset);
                        if(currLineSelected){
                            strObj[4] = new Integer(1);
                            currLineSelected = false;
                        }
                        if(isForceNo3d){
                            strObj[5] = new Integer(1);
                        }else if(force3d){
                            strObj[5] = new Integer(2);
                        }
                        xOffset += currentStrWid;
                        currStr = "";
                        currentStrWid = 0;
                        ret.addElement(strObj);
                        tagEnd = true;
                    }
                    currColor = null;
                    continue;
                }else{
                    //not a tag
                }
            }else if(ch == '|'){
                currLineSelected = true;
            }
            ch = s.charAt(pos);

            if(ch == '\r'){
                // \r字符被忽略
                continue;
            }else if (ch == '\n'){
                // 如果遇到\n，立刻强制换行
                ret.addElement(new Object[] { new Integer(line), currColor, currStr, new Integer(xOffset), null, null});
                xOffset = 0;
                currStr = "";
                currentStrWid = 0;
                line++;
            }else{
                int cw = font.charWidth(ch);
                if(xOffset + currentStrWid + cw > width){
                    strObj = new Object[6];
                    strObj[0] = new Integer(line);
                    strObj[1] = currColor;
                    strObj[2] = currStr;
                    strObj[3] = new Integer(xOffset);
                    if(currLineSelected){
                        strObj[4] = new Integer(1);
                        currLineSelected = false;
                    }
                    xOffset = 0;
                    currStr = "" + ch;
                    currentStrWid = cw;
                    ret.addElement(strObj);
                    line++;
                }else if(ch != '\n' && ch != '|'){
                    currStr += ch;
                    currentStrWid += cw;
                }
                if(ch == '\n' || pos == s.length() - 1){
                    strObj = new Object[6];
                    strObj[0] = new Integer(line);
                    strObj[1] = currColor;
                    strObj[2] = currStr;
                    strObj[3] = new Integer(xOffset);
                    if(currLineSelected){
                        strObj[4] = new Integer(1);
                        currLineSelected = false;
                    }
                    xOffset = 0;
                    currStr = "";
                    currentStrWid = 0;
                    ret.addElement(strObj);
                    line++;
                }
            }
            
            tagEnd = false;
        }

        return ret;
    }

    public static String[] formatText(String text, int width, Font font){
    	return formatText(text, width, font, false);
    }
    /**
     * 对混合格式字符串进行排版. 这个方法是formatString方法的一个包装, 返回值简化为String数组, 便于脚本界面操作. 
     * @param text 混合格式字符串
     * @param width 排版宽度 
     * @param font 排版字体
     * @return 返回一个String数组, 每个数组元素对应于一行. 这一行文本本身还是一个混合格式字符串, 在绘制的时候还需要
     *     对其进行重新排版计算. 这样格式的字符串可以用drawMixedText方法进行绘制. 
     */
//#if NewUI2
  //#         public static String[] formatText(String text, int width, Font font,boolean markLineHeight){
  //#        Vector vec = new Vector();
  //#        vec = formatString(text, width, font, false);
  //#
  //#        // 把同一行的分段重新合起来
  //#        Vector newVec = new Vector();
  //#        int len = vec.size();
  //#        int lineNum = 0;
  //#        int lineHeight = font.getHeight();
  //#        String currLine = "";
  //#        for(int i = 0; i < len; i++){
  //#            Object[] sec = (Object[]) vec.elementAt(i);
  //#            int line = ((Integer) sec[0]).intValue();
  //#            String secText = "";
  //#            if(sec[1] == null){
  //#                secText = (String) sec[2];
  //#            }else{
  //#                int clr = ((Integer) sec[1]).intValue();
  //#                if(clr != -1){
  //#                    if(sec[5] != null){
  //#                        secText = "<c" + Integer.toHexString(clr) + ">{x}" + sec[2] + "</c>";
  //#                    }else{
  //#                        secText = "<c" + Integer.toHexString(clr) + ">" + sec[2] + "</c>";
  //#                    }
  //#
  //#                }else{
  //#                    secText = (String) sec[2];
  //#                }
  //#            }
  //#            
  //#            if(line != lineNum){
  //#            	if(markLineHeight){
  //#            		newVec.addElement(currLine.concat("{$}").concat(String.valueOf(lineHeight)));
  //#            	} else {
  //#            		newVec.addElement(currLine);
  //#            	}
  //#            	lineHeight = font.getHeight();
  //#                currLine = secText;
  //#                lineNum = line;
  //#            }else{
  //#                currLine += secText;
  //#            }
  //#            
  //#            //统计实际行高
  //#            if(markLineHeight && sec.length == 9){
  //#            	if(sec[8] != null){
  //#            		int objHeight = ((Integer)sec[8]).intValue();
  //#                	if(objHeight > lineHeight){
  //#                		lineHeight = objHeight;
  //#                	}
  //#            	} else {
  //#            		lineHeight = font.getHeight();
  //#            	}
  //#            	
  //#            }
  //#        }
  //#        if(currLine.length() > 0 || newVec.size() == 0){
  //#        	if(markLineHeight){
  //#        		newVec.addElement(currLine.concat("{$}").concat(String.valueOf(lineHeight)));
  //#        	} else {
  //#        		newVec.addElement(currLine);
  //#        	}
  //#        	lineHeight = font.getHeight();
  //#        }
  //#
  //#        // Construct return values.
  //#        String[] ret = new String[newVec.size()];
  //#        newVec.copyInto(ret);
  //#        return ret;
  //#    }
//#else
    public static String[] formatText(String text, int width, Font font,boolean markLineHeight){
        Vector vec = new Vector();
        vec = formatString(text, width, font, false);

        // 把同一行的分段重新合起来
        Vector newVec = new Vector();
        int len = vec.size();
        int lineNum = 0;
        String currLine = "";
        for(int i = 0; i < len; i++){
            Object[] sec = (Object[]) vec.elementAt(i);
            int line = ((Integer) sec[0]).intValue();
            String secText = "";
            if(sec[1] == null){
                secText = (String) sec[2];
            }else{
                int clr = ((Integer) sec[1]).intValue();
                if(clr != -1){
                    if(sec[5] != null){
                        secText = "<c" + Integer.toHexString(clr) + ">{x}" + sec[2] + "</c>";
                    }else{
                        secText = "<c" + Integer.toHexString(clr) + ">" + sec[2] + "</c>";
                    }

                }else{
                    secText = (String) sec[2];
                }
            }
            if(line != lineNum){
                newVec.addElement(currLine);
                currLine = secText;
                lineNum = line;
            }else{
                currLine += secText;
            }
        }
        if(currLine.length() > 0 || newVec.size() == 0){
            newVec.addElement(currLine);
        }

        // Construct return values.
        String[] ret = new String[newVec.size()];
        newVec.copyInto(ret);
        return ret;
    }
//#endif
    private static final String punctation = ",.?:\"!;，。？：“”！；";

    /**
     * 对普通文本进行排版. 这个方法不处理控制字符. 注意绝对不要把这个方法和drawMsgTip方法混合使用, 可能会出现计算错误的情况.
     * @param text 排版文本
     * @param width 排版宽度
     * @param font 排版字体
     * @return 返回行文本的数组
     */
    public static String[] splitString(String text, int width, Font font){
        Vector vec = new Vector();
        int lineStart = 0;
        int lineWid = 0;
        int charCount = text.length();

        // Loop to break the text into lines.
        int i = 0;

        while(i < charCount){
            char ch = text.charAt(i);

            if(ch == '\n'){
                // If new line is found, record current line information and
                // step to next line.
                if(i > 0 && text.charAt(i - 1) == '\r'){
                    vec.addElement(text.substring(lineStart, i - 1));
                }else{
                    vec.addElement(text.substring(lineStart, i));
                }

                lineStart = i + 1;
                lineWid = 0;
            }else{
                //#if "${UseImageFont}" == "true"
                //# int charWid = GameCanvas.instance.iFont.charWidth(ch);
                //#else
                int charWid = font.charWidth(ch);
                //#endif

                if(lineWid == 0 || lineWid + charWid <= width){
                    // If current character is the first in current line, or
                    // it doesn't exceed the given width, just add it into
                    // current line.
                    lineWid += charWid;
                }else{
                    // If current character exceed the given width, record
                    // current line information and add current character into
                    // the next line.

                    // Don't put punctation at the head of line
                    if(punctation.indexOf(ch) >= 0){
                        i--;
                        //#if "${UseImageFont}" == "true"
                        //# charWid += GameCanvas.instance.iFont.charWidth(text.charAt(i));
                        //#else
                        charWid += font.charWidth(text.charAt(i));
                        //#endif
                    }

                    vec.addElement(text.substring(lineStart, i));
                    lineStart = i;
                    lineWid = charWid;
                }
            }

            i++;
        }

        // Handle the last line.
        if(lineWid > 0){
            vec.addElement(text.substring(lineStart));
        }

        // Construct return values.
        String[] ret = new String[vec.size()];
        vec.copyInto(ret);

        return ret;
    }

    //合并字符串
    public static String mergeString(Vector v){
        StringBuffer sb = new StringBuffer();

        int count = v.size();
        for(int i = 0; i < count; i++){
            sb.append((String) v.elementAt(i));
        }

        return sb.toString();
    }

    /**
     * 合并字符串,末尾追加"\n"(最后一个不加),忽略null和空串
     * @param v
     * @return
     */
    public static String mergeString2(Vector v){
        StringBuffer sb = new StringBuffer();

        int count = v.size();
        for(int i = 0; i < count; i++){
            String tmp = (String) v.elementAt(i);
            if(tmp != null && tmp.equals("") == false){
                sb.append(tmp);
                if(i < count - 1){
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    //原ByteStream
    /**
    * 从字节流中得到一个short值。
    */
    public static short getShort(byte[] data, int off){
        return (short) (((data[off] & 0xFF) << 8) | (data[off + 1] & 0xFF));
    }

    /**
     * 保存一个short值到字节流中。
     */
    public static void setShort(byte[] data, int off, short value){
        data[off] = (byte) ((value >> 8) & 0xFF);
        data[off + 1] = (byte) (value & 0xFF);
    }

    /**
     * 从字节流中得到一个int值。
     */
    public static int getInt(byte[] data, int off){
        return ((data[off] & 0xFF) << 24) | ((data[off + 1] & 0xFF) << 16) | ((data[off + 2] & 0xFF) << 8) | (data[off + 3] & 0xFF);
    }

    /**
     * 保存一个int值到字节流中。
     */
    public static void setInt(byte[] data, int off, int value){
        data[off] = (byte) ((value >> 24) & 0xFF);
        data[off + 1] = (byte) ((value >> 16) & 0xFF);
        data[off + 2] = (byte) ((value >> 8) & 0xFF);
        data[off + 3] = (byte) (value & 0xFF);
    }

    /**
     * 从流中读取一个UTF-16BE字符串。
     */
    public static String readUTF16(DataInputStream is) throws IOException{
        int slen = is.readByte();
        if((slen & 0x80) != 0){ // 字符串长度大于128
            int slen2 = is.readByte();
            slen = ((slen & 0x7F) << 8) + (slen2 & 0xFF);
        }
        char[] buf = new char[slen];
        for(int i = 0; i < slen; i++){
            buf[i] = is.readChar();
        }
        return new String(buf);
    }
    
    public final static String readUTF(DataInputStream in) throws IOException{
    	//#if NewUI2
    	return in.readUTF();
    	//#else
    	//#        int utflen = in.readUnsignedShort();
    	//#        byte[] bytearr = new byte[utflen];
    	//#        char[] chararr = new char[utflen];
    	//#
    	//#        int c, char2 = 0, char3 = 0;
    	//#       int count = 0;
    	//#       int chararr_count = 0;
    	//#
    	//#       in.readFully(bytearr, 0, utflen);
    	//#
    	//#      while(count < utflen){
    	//#          c = (int) bytearr[count] & 0xff;
    	//#          if(c > 127)
    	//#           break;
              //#          count++;
              //#          chararr[chararr_count++] = (char) c;
              //#      }
              //#
              //#      while(count < utflen){
              //#          c = (int) bytearr[count] & 0xff;
              //#         if(c < 0x80){
              //#            count++;
              //#            chararr[chararr_count++] = (char) c;
              //#         }else if(c < 0xe0){
              //#             count += 2;
              //#             if(count > utflen)
              //#                  throw new UTFDataFormatException("malformed input: partial character at end");
              //#              char2 = (int) bytearr[count - 1];
              //#              if((char2 & 0xC0) != 0x80)
              //#                  throw new UTFDataFormatException("malformed input around byte " + count);
              //#              chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
              //#          }else if(c < 0xf0){
              //#              /* 1110 xxxx  10xx xxxx  10xx xxxx */
              //#             count += 3;
              //#            if(count > utflen)
              //#                 throw new UTFDataFormatException("malformed input: partial character at end");
              //#             char2 = (int) bytearr[count - 2];
              //#             char3 = (int) bytearr[count - 1];
              //#             if(((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
              //#                 throw new UTFDataFormatException("malformed input around byte " + (count - 1));
              //#              chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
              //#           }
              //#      }

              //#      // The number of chars produced may be less than utflen
              //#     return new String(chararr, 0, chararr_count);
      //#endif
    }

    //原GameNetwork
    /** 时间同步 Client->Server */
    public static final short CONN_SYNC_CLIENT = 101;

    /** 时间同步 Server->Client */
    public static final short CONN_SYNC_SERVER = 102;

    /** 用户登陆 Client->Server */
    public static final short CONN_LOGIN_CLIENT = 103;

    /** 用户登陆 Server->Client */
    public static final short CONN_LOGIN_SERVER = 104;

    /** 移动 Client->Server */
    public static final short CONN_MOVE_CLIENT = 105;

    /** 移动 Server->Client */
    public static final short CONN_MOVE_SERVER = 106;

    /** 上马 Client->Server */
    public static final short CONN_ON_HORSE_CLIENT = 107;

    /** 下马 Client->Server */
    public static final short CONN_OFF_HORSE_CLIENT = 108;

    /** 攻击 Client->Server */
    public static final short CONN_ATTACK_CLIENT = 109;

    /** 攻击 Server->Client */
    public static final short CONN_ATTACK_SERVER = 110;

    /** 被攻击 Server->Client */
    public static final short CONN_ATTACKED_SERVER = 111;

    /** 下线 Client->Server */
    public static final short CONN_LOGOUT_CLIENT = 113;

    /** 下线 Server->Client */
    public static final short CONN_LOGOUT_SERVER = 114;

    /** 单位不可见 Server->Client */
    public static final short CONN_UNIT_INVISIBLE_SERVER = 115;

    /** touch出口 Client->Server */
    public static final short CONN_TOUCHEXIT_CLIENT = 116;

    /** 关卡数据 Server->Client */
    public static final short CONN_PKG_SERVER = 117;

    /** Touch Npc Client->Server */
    public static final short CONN_TOUCHNPC_CLIENT = 120;

    /** Npc Chat Message Server->Client */
    public static final short CONN_NPC_CHAT_SERVER = 121;

    /** Message Server->Client */
    public static final short CONN_MESSAGE_SERVER = 122;

    /** Question Message Server->Client */
    public static final short CONN_QUESTION_SERVER = 123;

    /** 技能列表 Server->Client */
    public static final short CONN_ABILITIES_SERVER = 124;

    /** 添加关卡中可以接受的任务 Server->Client */
    public static final short CONN_QUEST_START_ADDED_SERVER = 125;

    /** 移除关卡中可以接受的任务 Server->Client */
    public static final short CONN_QUEST_START_REMOVED_SERVER = 126;

    /** 添加关卡中可完成的任务 Server->Client */
    public static final short CONN_QUEST_FINISH_ADDED_SERVER = 127;

    /** 移除关卡中可完成的任务 Server->Client */
    public static final short CONN_QUEST_FINISH_REMOVED_SERVER = 128;

    /** 获取任务描述 Server->Client */
    public static final short CONN_QUEST_DESC_CLIENT = 129;

    /** 任务描述 Server->Client */
    public static final short CONN_QUEST_DESC_SERVER = 130;

    /** 接受任务 Client->Server */
    public static final short CONN_QUEST_ACCEPT_CLIENT = 131;

    /** 接受任务成功 Server->Client */
    public static final short CONN_QUEST_ACCEPTED_SERVER = 132;

    /** 载入地图完成 Client->Server */
    public static final short CONN_LOADING_FINISHED_CLIENT = 133;

    /** 允许跳转地图 Server->Client */
    public static final short CONN_GOMAP_ALLOW = 134;

    /** 任务完成 Server->Client */
    public static final short CONN_QUEST_FINISHED_SERVER = 135;

    /** 攻击失败 Server->Client */
    public static final short CONN_ATTACK_FAIL_SERVER = 136;

    /** 完成任务 Client->Server */
    public static final short CONN_QUEST_FINISH_CLIENT = 137;

    /** 完成任务失败 Server->Client */
    public static final short CONN_QUEST_FINISH_FAIL_SERVER = 138;

    /** 背包变化 Server->Client */
    public static final short CONN_BAG_CHANGED_SERVER = 139;

    /** 获取物品信息 Client->Server */
    public static final short CONN_ITEMINFO_CLIENT = 142;

    /** 获取物品信息 Server->Client */
    public static final short CONN_ITEMINFO_SERVER = 143;

    /** 同步player信息 Server->Client */
    public static final short CONN_SYNC_PLAYER_SERVER = 144;

    /** 使用物品 Client->Server */
    public static final short CONN_USEITEM_CLIENT = 145;

    /** 下载背包 Client->Server */
    public static final short CONN_BAG_CLIENT = 146;

    /** 下载背包 Server->Client */
    public static final short CONN_BAG_SERVER = 147;

    /** 移除物品 Client->Server */
    public static final short CONN_REMOVEITEM_CLIENT = 148;

    /** 取得任务中间描述 Client->Server */
    public static final short CONN_QUEST_PREDESC_CLIENT = 149;

    /** 取得任务中间描述 Server->Client */
    public static final short CONN_QUEST_PREDESC_SERVER = 150;

    /** 取得任务结束描述 Client->Server */
    public static final short CONN_QUEST_POSTDESC_CLIENT = 151;

    /** 取得任务结束描述 Server->Client */
    public static final short CONN_QUEST_POSTDESC_SERVER = 152;

    /** 取得自身技能列表 Client->Server */
    public static final short CONN_SKILL_LIST_CLIENT = 153;

    /** 取得自身技能列表 Server->Client */
    public static final short CONN_SKILL_LIST_SERVER = 154;

    /** 技能加点 Client->Server */
    public static final short CONN_SKILL_ADDPOINT_CLIENT = 155;

    /** 自身全部技能洗点 Client->Server */
    public static final short CONN_SKILL_REFRESH_CLIENT = 156;

    /** 取技能名字列表 Client->Server */
    public static final short CONN_SKILL_NAMELIST_CLIENT = 157;

    /** 取技能名字列表 Server->Client */
    public static final short CONN_SKILL_NAMELIST_SERVER = 158;

    /** 获取任务列表 Client->Server */
    public static final short CONN_QUEST_LIST_CLIENT = 159;

    /** 获取任务列表 Server->Client */
    public static final short CONN_QUEST_LIST_SERVER = 160;

    /** 任务信息下发 Server->Client */
    public static final short CONN_QUEST_INFO_SERVER = 161;

    /** 客户端与服务器同步变量 Client->Server */
    public static final short CONN_VM_VARIABLE_SYNC_CLIENT = 162;

    /** 服务器与客户端同步变量 Server->Client */
    public static final short CONN_VM_VARIABLE_SYNC_SERVER = 163;

    /** 获取物品描述 Client->Server */
    public static final short CONN_ITEM_DESC_CLIENT = 164;

    /** 获取物品描述 Server->Client */
    public static final short CONN_ITEM_DESC_SERVER = 165;

    /**
     * 放弃任务 serial int 任务Id int
     */
    public static final short CONN_QUEST_ABANDON_CLIENT = 172;

    /**
     * 放弃任务成功 serial int 任务Id int
     */
    public static final short CONN_QUEST_ABANDON_SERVER = 173;

    /**
     * 界面Notify 任务Id int notifyId byte notifyType byte 1 chat 2 message 3
     * question questionAnswer byte
     */
    public static final short CONN_NOTIFY_CLIENT = 174;

    /**
     * 技能攻击 源类型 byte 源Id int 攻击时间 int x int y int 方向 byte 目标类型 byte 目标Id int
     * 技能Id int
     * 
     */
    public static final short CONN_SKILL_ATTACK_SERVER = 186;

    /**
     * 被攻击 目标类型 byte 目标id int 时间 int 源类型 byte 源id int 攻击结果类型 byte 0 命中 1 miss 2
     * 免疫 3 暴击 4 反弹 伤害值 int 只有在命中时有意义 受攻击动画 int
     */
    public static final short CONN_SKILL_ATTACKED_SERVER = 187;

    /**
     * 版本比较 数量 short 循环n次 文件名 string 版本 int
     */
    public static final short CONN_VERSION_COMPARE_CLIENT = 188;

    /**
     * 版本比较结果 数量 short 循环n次 需要删除缓存的文件名 string
     */
    public static final short CONN_VERSION_COMPARE_SERVER = 189;

    /**
     * 强制进行版本比较
     */
    public static final short CONN_SYNC_VERSION_SERVER = 190;

    /**
     * 获取文件 文件名 string
     */
    public static final short CONN_GETFILE_CLIENT = 191;
    /**
     * 文件信息
     * 文件名                      string
     * 版本信息                     int
     * 文件内容                     byte[]
     */
    public static final short CONN_GETFILE_SERVER = 192;

    /**
     * NPC，玩家，怪物进入(走出)视野
     * type                         byte(第7位如果为1，表示走出视野)
     * id                           int
     * instanceId                   int
     */
    public static final short CONN_UNIT_REFRESH_SERVER = 193;

    /**
     * 刷新多人走入走出视野信息
     * 数量                           byte
     * 循环n次
     *  type                        byte
     *  id                          int
     *  instanceId                  int
     *  imageId                     short (怪物，采集npc在走进视野的时候存在)
     */
    public static final short CONN_UNIT_MULTI_REFRESH_SERVER = 194;

    /**
     * UNIT行走信息
     * type                         byte 起始的5位分别代表一下的5段内容是否包含
     * instanceId                   int
     * mapid                        short (第一段)
     * x                            short (第一段)
     * y                            short (第一段) 
     * 角度                           byte（角度/2）(第二段)
     * 时间                           int(从系统启动开始计算的毫秒速) (第二段)
     * 速度                           byte(每秒的像素） (第二段)
     * hp百分比                        byte(200为单位)    (第三段)
     * mp百分比                        byte(200为单位) (第三段)
     * state                        short (第四段) (0 running 1 attack 2 ride 3 die 4 组队 5 队长)
     * 第五段的Mask                 byte  (只有第五段存在时次字段才存在 0 name 1 level 2 faction 3 装备分数)
     * name                         string (第五段 0)
     * level                        byte (第五段 1)
     * faction                      byte (第五段 2)
     * headscore                    int  (第五段 3)
     * bodyscore                    int  (第五段 3)
     * weaponscore                  int  (第五段 3)
     */
    public static final short CONN_UNIT_MOVE_SERVER = 195;

    /**
     * 请求unit信息
     * instanceId                   int
     */
    public static final short CONN_UNIT_INFO_CLIENT = 196;

    /**
     * unit信息
     * instanceId                   int
     * 如果是NPC
     *  NPCImageID                  int
     *  通过性                     byte
     *  是否是功能NPC                byte
     *  功能名字                        string
     * 如果是其他玩家
     *  性别                          byte
     *  职业                          byte
     *  工会                          string
     *  荣誉                          string
     * 如果是资源
     *  NPCImageID                  int
     *  任务ID                        int (如果小于0，那么跟任务无关)
     */
    public static final short CONN_UNIT_INFO_SERVER = 197;

    /**
     * 开始采集
     * instanceId                   int
     * 
     */
    public static final short CONN_GATHER_START_CLIENT = 198;

    /**
     * 采集结束
     * instanceId                   int
     * 
     */
    public static final short CONN_GATHER_END_CLIENT = 199;

    public static final short CONN_RELOAD_CLIENT = 200;

    /**
     * 获取动作条Option
     * client -> server
     * type                             byte        -1: 空
     *                                              0：普通攻击
     *                                              1：技能
     *                                              2：物品
     * id                               int
     */
    public static final short CONN_ACTIONBAR_OPTION_CLIENT = 236;
    /**
     * 动作条Option
     * server -> client                              byte[]
     */
    public static final short CONN_ACTIONBAR_OPTION_SERVER = 237;

    /**
     * 设置动作条Option
     * serial                               int
     * options                              byte[]
     */
    public static final short SET_ACTIONBAR_OPTION_CLIENT = 238;

    /**
     * 设置动作条Option成功
     * serial                               int
     */
    public static final short SET_ACTIONBAR_OPTION_SERVER = 239;

    /**
    * 脚本名字                             string
    * 参数字符串                            string
    */
    public static final short CONN_OPENUI_SERVER = 235;

    /**
     * 技能攻击
     * 源InstanceId                  int
     * 目标InstanceId             int
     * 技能Id                     int
     * 起手动画ID                   int
     * 
     */
    public static final short CONN_SKILL_PREPARE_ATTACK_SERVER = 297;

    /**
     * 追击
     * sourceId                         int
     * targetId                         int (如果停止追击值为-1)
     * 停止范围                         short
     * 开始追击的x坐标                 short
     * 开始追击的y坐标                 short
     * 追击速度                         byte
     */
    public static final short CONN_CHASE_SERVER = 336;

    /**
     * 强制请求一次完整move
     */
    public static final short CONN_REQUEST_WHOLE_MOVE = 348;
    
    /**
	 * 获取文件
	 * 文件名						string
	 */
	public static final short NEW_CONN_GETFILE_CLIENT = 2381;
	/**
	 * 文件信息
	 * 客户端机型                   string
	 * 文件名						string
	 * 版本信息						int
	 * 文件内容总长度				int
	 * 下发的数据在文件中的起始索引	int
	 * 文件内容(分段下发)			byte[]
	 */
	public static final short NEW_CONN_GETFILE_SERVER = 2382;

    public static Hashtable unitViewCache = new Hashtable();

    public static void removeUnitViewCacheByType(int type){
        Hashtable restTable = new Hashtable();
        Enumeration enu = unitViewCache.keys();

        while(enu.hasMoreElements()){
            Long key = (Long) enu.nextElement();

            if(Tool.getSpriteKeyType(key) != type){
                restTable.put(key, unitViewCache.get(key));
            }
        }

        unitViewCache = restTable;
    }

    public static void requestWholeMove(int instanceId){
        UASegment segment = new UASegment(CONN_REQUEST_WHOLE_MOVE);

        try{
            segment.writeInt(instanceId);

            Utilities.sendRequest(segment);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void sendPosition(int dir, int x, int y, int state){
        UASegment segment = new UASegment(CONN_MOVE_CLIENT);

        try{
            segment.writeInt(Utilities.getServerTime());
            //#if ModelID == AndroidAuto
            //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
        	//# {
            //# x >>= 1;
        	//# y >>= 1;
            //# }
            //#elif DoubleScreen == true
            //# x >>= 1;
        	//# y >>= 1;
            //#endif
            segment.writeShort((short) x);
            segment.writeShort((short) y);
            segment.writeByte((byte) dir);
            segment.writeShort((short) state);

            Utilities.sendRequest(segment);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * NPC，玩家，怪物进入(走出)视野
     * type                         byte(第7位如果为1，表示走出视野)
     * id                           int
     * instanceId                   int
     * 
     * public static final short UNIT_REFRESH_SERVER = 193;
     * 
     * @param segment
     */
    public static void recvUnitView(UASegment segment){
        int type = segment.readUnsignedByte();
        boolean isOutView = (type & 0x80) == 0x80;
        type = type & 0x7F;
        int id = segment.readInt();
        int instanceId = segment.readInt();

        switch(type){
            case Tool.SPRITE_TYPE_PLAYER:
                processUnitView(type, id, instanceId, isOutView, -1);
                break;
            case Tool.SPRITE_TYPE_GATHER_NPC:
            case Tool.SPRITE_TYPE_NPC:
            case Tool.SPRITE_TYPE_ATTENDANT:
                if(isOutView){
                    processUnitView(type, id, instanceId, isOutView, -1);
                }else{
                    processUnitView(type, id, instanceId, isOutView, segment.readUnsignedShort());
                }
                break;
        }

        segment.handled = true;
    }

    /**
     * 刷新多人走入走出视野信息
     * 数量                           byte
     * 循环n次
     *  type                        byte
     *  id                          int
     *  instanceId                  int
     *  imageId                     short (怪物，采集npc在走进视野的时候存在)
     */
    public static void recvMultiUnitView(UASegment segment){
        int count = segment.readUnsignedByte();

        for(int i = 0; i < count; i++){
            int type = segment.readUnsignedByte();
            boolean isOutView = (type & 0x80) == 0x80;
            type = type & 0x7F;
            int id = segment.readInt();
            int instanceId = segment.readInt();

            switch(type){
                case Tool.SPRITE_TYPE_PLAYER:
                    processUnitView(type, id, instanceId, isOutView, -1);
                    break;
                case Tool.SPRITE_TYPE_GATHER_NPC:
                case Tool.SPRITE_TYPE_NPC:
                case Tool.SPRITE_TYPE_ATTENDANT:
                    if(isOutView){
                        processUnitView(type, id, instanceId, isOutView, -1);
                    }else{
                        processUnitView(type, id, instanceId, isOutView, segment.readUnsignedShort());
                    }
                    break;
            }
        }

        segment.handled = true;
    }

    private static void processUnitView(int type, int id, int instanceId, boolean isOutView, int imageId){
        switch(type){
            case Tool.SPRITE_TYPE_PLAYER: {
                GameNetPlayer player = GameWorld.findPlayerByInstanceId(instanceId);

                if(isOutView){
                    if(player != null){
                        player.isOutView = true;
                        if(!player.noNeedRemove){
                            if(GameWorld.teamInfo.get(new Integer(player.getInstanceId())) == null){
                                GameWorld.doDestorySprite(player, false, false); //走出视野时，直接删掉
                            }
                        }else{
                            GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_PLAYER_OUT_VIEW, new Integer(player.getInstanceId()));
                        }
                    }
                }else{
                    if(player == null){
                        player = GameNetPlayer.createGameNetPlayer(id, instanceId);
                        GameWorld.addSprite(player);
                        sendUnitInfo(instanceId);
                    }
                    if(player.noNeedRemove){
                        GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_PLAYER_IN_VIEW, new Integer(player.getInstanceId()));
                    }
                    player.sprite.setPosition(-1000, player.sprite.getY());
                    player.isOutView = false;
                }
            }
                break;
            case Tool.SPRITE_TYPE_GATHER_NPC:
            case Tool.SPRITE_TYPE_NPC: 
            case Tool.SPRITE_TYPE_ATTENDANT:{
                GameNpc npc = GameWorld.findNpcByInstanceId(instanceId);

                if(isOutView){
                    GameWorld.doDestorySprite(npc, true, false);

                  //#ifdef buildtest
                    System.out.println("--------- 走出视野:id=" + id);
                  //#endif
                }else{
                    if(npc == null){
                        npc = GameNpc.createGameNpc((byte) type, id, instanceId, imageId);
                        GameWorld.addSprite(npc);
                        sendUnitInfo(instanceId);
                        if(type == Tool.SPRITE_TYPE_ATTENDANT){
                    		npc.sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, new Integer(npc.getInstanceId()));
                    	}
                        
                      //#ifdef buildtest
                        System.out.println("--------- 走入视野:id=" + id);
                      //#endif
                    }
                }

            }
                break;
        }
    }

    /**
     * UNIT行走信息
     * type                         byte 起始的5位分别代表一下的5段内容是否包含
     * instanceId                   int
     * mapid                        short (第一段)
     * x                            short (第一段)
     * y                            short (第一段) 
     * 角度                           byte（角度/2）(第二段)
     * 时间                           int(从系统启动开始计算的毫秒速) (第二段)
     * 速度                           byte(每秒的像素） (第二段)
     * hp百分比                        byte(200为单位)    (第三段)
     * mp百分比                        byte(200为单位) (第三段)
     * state                        short (第四段) (0 running 1 attack 2 ride 3 die 4 组队 5 队长 6 恐惧 7 麻痹 8 定身)
     * 第五段的Mask                 byte  (只有第五段存在时次字段才存在 0 name 1 level 2 faction 3 装备分数 4 sex)
     * name                         string (第五段 0)
     * level                        byte (第五段 1)
     * faction                      byte (第五段 2)
     * headscore                    int  (第五段 3)
     * bodyscore                    int  (第五段 3)
     * weaponscore                  int  (第五段 3)
     * sex                          byte (第五段 4)
     * 
     * public static final short UNIT_MOVE_SERVER = 195;
     * 
     */
    public static void recvUnitMove(UASegment segment){
        segment.reset();
        int type = segment.readUnsignedByte();
        boolean firstSect = ((type >> 7) & 0x1) == 1;
        boolean secondSect = ((type >> 6) & 0x1) == 1;
        boolean thirdSect = ((type >> 5) & 0x1) == 1;
        boolean forthSect = ((type >> 4) & 0x1) == 1;
        boolean fifthSect = ((type >> 3) & 0x1) == 1;
        int instanceId = segment.readInt();
        type &= 0x07;

        int mapId = 0, x = 0, y = 0;
        int angle = 0, time = 0;
        int speed = 0;
        short state = 0;
        int targetPos = -1;
        int movePackage = 100000;

        GameSprite gameSprite = null;

        switch(type){
            case Tool.SPRITE_TYPE_PLAYER:
                gameSprite = GameWorld.findPlayerByInstanceId(instanceId);

                if(gameSprite != null){
                    ((GameNetPlayer) gameSprite).lastSyncMoveTime = Tool.getSystemTime();
                }else{
                    //组队成员
                    gameSprite = GameNetPlayer.createGameNetPlayer(instanceId, instanceId);
                    GameWorld.addSprite(gameSprite);
                    sendUnitInfo(instanceId);
                }

                break;
            case Tool.SPRITE_TYPE_GATHER_NPC:
            case Tool.SPRITE_TYPE_NPC:
            case Tool.SPRITE_TYPE_ATTENDANT:
            	gameSprite = GameWorld.findNpcByInstanceId(instanceId);
                movePackage = 200000;

                break;
        }

        if(firstSect){
            movePackage += 10000;

            mapId = segment.readUnsignedShort();
            int mapInstanceId = -1;
            if((mapId >> 15) == 1){
                mapInstanceId = segment.readInt();
                mapId = mapId & 0x7FFF;
            }

            boolean mapChanged = false;
            if(gameSprite != null){
            	if (gameSprite.sprite.getMapId() != mapId) {
            		mapChanged = true;
            	}
                gameSprite.sprite.setMapId(mapId);
                //mapInstanceId为副本服务，仅对其他玩家有效
                if(type == Tool.SPRITE_TYPE_PLAYER){
                    gameSprite.sprite.setMapInstanceId(mapInstanceId);
                }else{
                    gameSprite.sprite.setMapInstanceId(GameWorld.player.sprite.getMapInstanceId());
                }
            }

            x = segment.readUnsignedShort();
            y = segment.readUnsignedShort();
            //#if ModelID == AndroidAuto
            //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
        	//# {
            //# x <<= 1;
            //# y <<= 1;
            //# }
            //#elif DoubleScreen == true
            //# x <<= 1;
            //# y <<= 1;
            //#endif
            
            if(gameSprite != null && (gameSprite.sprite.getX() < -500 || mapChanged)){
                //初始化sprite的位置
                gameSprite.sprite.setPosition(x, y);
            }
        }

        if(secondSect){
            movePackage += 1000;

            angle = segment.readUnsignedByte();
            time = segment.readInt();
            speed = segment.readUnsignedByte();
            //#if ModelID == AndroidAuto
            //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
        	//# {
            //# speed <<= 1;
            //# }
            //#elif DoubleScreen == true
            //# speed <<= 1;
            //#endif

            switch(type){
                case Tool.SPRITE_TYPE_GATHER_NPC:
                case Tool.SPRITE_TYPE_NPC:
                case Tool.SPRITE_TYPE_ATTENDANT:
                    targetPos = segment.readInt();
            }
        }

        if(gameSprite != null && thirdSect){
            movePackage += 100;

            gameSprite.hpMax = 200;
            gameSprite.hp = segment.readUnsignedByte();
            gameSprite.mpMax = 200;
            gameSprite.mp = segment.readUnsignedByte();
            if(type == Tool.SPRITE_TYPE_PLAYER && GameWorld.player.isTeamState()){
                VM vm = VMGame.getVMGame("game_panel").getVM();
                synchronized(vm){
                    vm.callback(VMGame.CALLBACK_REFRESH_TEAM_PANEL, new int[]{
                                    vm.makeTempObject(segment), vm.makeTempObject(gameSprite), instanceId, gameSprite.hpMax, gameSprite.hp, gameSprite.mpMax, gameSprite.mp
                    });
                }
            }

            if(gameSprite == GameWorld.player.target){
                GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_SYNC_TARGET, null);
            }
        }

        if(gameSprite != null && forthSect){
            movePackage += 10;

            state = segment.readShort();

            //如果出第1位(running)外, 其他位有不同时由脚本处理
            if((gameSprite.state & 0xFFFE) != (state & 0xFFFE)){
                VM vm = VMGame.getVMGameByVMKey(VMGame.gameWorldVMGameKey).getVM();

                synchronized(vm){
                    vm.callback(VMGame.CALLBACK_UNIT_MOVE_FORTH, new int[]{
                                    vm.makeTempObject(segment), vm.makeTempObject(gameSprite), type, instanceId, state, gameSprite.state
                    });
                }
            }

            gameSprite.state = state;
        }

        if(gameSprite != null && fifthSect){
            movePackage += 1;

            int mask = segment.readUnsignedByte();
            VM vm = VMGame.getVMGame("game_world").getVM();

            synchronized(vm){
                vm.callback(VMGame.CALLBACK_UNIT_MOVE_FIFTH, new int[]{
                                vm.makeTempObject(segment), vm.makeTempObject(gameSprite), type, instanceId, mask
                });
            }
        }

        //添加路点
        if(gameSprite != null && !gameSprite.chaseMode && gameSprite.status.size() == 0 && firstSect && secondSect && forthSect) {
        	if (gameSprite.sprite.getMapId() == GameWorld.player.sprite.getMapId() && gameSprite.sprite.getMapInstanceId() == GameWorld.player.sprite.getMapInstanceId()) {
        		if (gameSprite.sprite.getX() != x || gameSprite.sprite.getY() != y) {
        			gameSprite.addWayPoint(x, y, gameSprite.moveAnimateIndex, gameSprite.stopAnimateIndex, (state & 0x1) == 1, angle << 1, time, speed, targetPos);
        		} else {
        			gameSprite.sprite.setPosition(x, y);
        		}
        	}
        }

        if(type == Tool.SPRITE_TYPE_PLAYER && gameSprite != null && !gameSprite.infoRecved && !gameSprite.infoForceRequested){
            //send force request all info of sprite
            requestWholeMove(instanceId);
            gameSprite.infoForceRequested = true;
        }

        segment.handled = true;

        //TODO delete
        /*
        Integer recvKey = new Integer(movePackage);
        int[] recvSize = (int[]) UASocketConnection.recvStat.get(recvKey);
        if(recvSize == null){
            recvSize = new int[]{
                            0, 0
            };
        }
        recvSize[0]++;
        UASocketConnection.recvStat.put(recvKey, recvSize);*/
    }

    /**
     * 请求unit信息
     * instanceId                   int
     * 
     * public static final short CONN_UNIT_INFO_CLIENT = 196;
     * 
     * @param instanceId
     */
    public static void sendUnitInfo(int instanceId){
        GameSprite sprite = GameWorld.getSprite(instanceId);

        if(sprite != null){
            UASegment segment = (UASegment) unitViewCache.get(Tool.getSpriteKey(sprite.getType(), sprite.getId()));

            if(segment != null){
                segment.reset();
                segment.setInt(instanceId);
                GameWorld.BroadcastPacket(segment);

                return;
            }
        }

        UASegment segment = new UASegment(CONN_UNIT_INFO_CLIENT);
        try{
            segment.writeInt(instanceId);
        }catch(IOException e){
            e.printStackTrace();
        }

        Utilities.sendRequest(segment);
    }

    public static void sendLogout(){
        UASegment segment = new UASegment(CONN_LOGOUT_CLIENT);
        Utilities.sendRequest(segment);
    }

    public static void recvLogout(UASegment segment){
        int instanceId = segment.readInt();
        segment.handled = true;

        GameWorld.playerLogout(instanceId);
    }

    public static void recvInvisible(UASegment segment){
        int instanceId = segment.readInt();
        GameSprite gameSprite = GameWorld.getSprite(instanceId);

        if(gameSprite != null){
            switch(gameSprite.getType()){
                case Tool.SPRITE_TYPE_PLAYER: {
                    GameWorld.playerLogout(instanceId);
                }
                    break;
                case Tool.SPRITE_TYPE_NPC:
                    break;
                case Tool.SPRITE_TYPE_CREATURE:
                    break;
                case Tool.SPRITE_TYPE_CORPSE:
                    break;
            }
        }

    }

    public static void sendHorseAction(int dir, int x, int y, boolean on){
        UASegment segment;

        if(on){
            segment = new UASegment(CONN_ON_HORSE_CLIENT);
        }else{
            segment = new UASegment(CONN_OFF_HORSE_CLIENT);
        }

        try{
            segment.writeInt(Utilities.getServerTime());
            segment.writeInt(x);
            segment.writeInt(y);
            segment.writeByte((byte) dir);

            Utilities.sendRequest(segment);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void recvAttack(UASegment segment, boolean isPreAttack){
        /**
         * 技能攻击
         * 源InstanceId                  int
         * 目标源InstanceId                int
         * 释放动画ID                       int
         */
        int instanceId = segment.readInt();
        int targetInstanceId = segment.readInt();
        int startAnimateId = segment.readInt();

        GameSprite gameSprite = GameWorld.getSprite(instanceId);

        if(gameSprite != null){
            if(isPreAttack){
                gameSprite.sendCommand(VMGame.GAME_COMMAND_SPRITE_PLAY_PRE_ATTACK, new int[]{
                                1, instanceId, targetInstanceId, startAnimateId, VM.FALSE
                });
            }else{
                gameSprite.sendCommand(VMGame.GAME_COMMAND_SPRITE_PLAY_ATTACK, new int[]{
                                1, instanceId, targetInstanceId, startAnimateId, VM.FALSE
                });
            }
        }
    }

    public static void recvAttacked(UASegment segment){
        /**
         * 被攻击
         * 目标InstanceId             int
         * 时间                           int
         * 源InstanceId                  int
         * 攻击结果类型                   byte    0 命中 1 miss 2 免疫 3 命中(加血，加蓝)且暴击
         * 伤害类型                         byte     0 物理伤害 1 法术伤害 2 抽蓝 3 加DEBUFF 4 加血 5 回蓝 6 加BUFF
         * 伤害值                      int     只有在命中时有意义
         * 受攻击动画                    int
         */
        int targetInstanceId = segment.readInt();
        int time = segment.readInt();
        int sourceInstanceId = segment.readInt();
        int attackType = segment.readByte();
        int damageType = segment.readByte();
        int damage = segment.readInt();
        int magicType = segment.readInt();

        GameSprite gameSprite = GameWorld.getSprite(targetInstanceId);

        if(gameSprite != null){
            gameSprite.sendCommand(VMGame.GAME_COMMAND_SPRITE_PLAY_ATTACKED, new int[]{
                            1, targetInstanceId, time, sourceInstanceId, attackType, damageType, damage, magicType
            });
        }
    }

    public static void recvAttackFail(UASegment segment){
        /**
         * 攻击失败
         * 类型                       byte    1 距离太远 2 当前有攻击正在进行 3 目标已经死亡 4 目标不存在 
         *                                  5 没有技能 6 此技能不能在马上使用 7 这个技能必须选择一个目标
         *                                  8 这个技能不能对这个目标使用 9 目标没有死亡 10 次技能CD时间没到 11 当前状态不能使用此技能
         *                                  12 没有足够的mana
         * sourceInstanceId         int     源InstanceId
         * targetInstanceId         int     目标InstanceId
         * attackId                 int     技能Id
         */
        int reason = segment.readUnsignedByte();
        int sourceInstanceId = segment.readInt();
        int targetInstanceId = segment.readInt();
        int attackId = segment.readInt();

        GameSprite gameSprite = GameWorld.getSprite(sourceInstanceId);

        if(gameSprite != null){
            gameSprite.sendCommand(VMGame.GAME_COMMAND_SPRITE_PLAY_ATTACK_FAIL, new int[]{
                            1, reason, sourceInstanceId, targetInstanceId, attackId
            });
        }
    }

    public static void sendTouchExit(int exitId){
        UASegment segment = new UASegment(CONN_TOUCHEXIT_CLIENT);

        try{
            segment.writeInt(Utilities.getServerTime());
            segment.writeInt(exitId);

            Utilities.sendRequest(segment);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void sendLoadMapFinished(){
        UASegment segment = new UASegment(CONN_LOADING_FINISHED_CLIENT);

        try{
            Utilities.sendRequest(segment);
            VMGame vmg = VMGame.getVMGame("ui_gamemenu");
            if(vmg != null){
                VM vm = vmg.getVM();
                synchronized(vm){
                    vm.callback(VMGame.CALLBACK_LOADING_FINISH, new int[0]);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //该方法已作废，该包已在在脚本中先被处理了
    public static void recvAllowGomap(UASegment segment){
        segment.handled = true;
        GameWorld.goMap(segment.readInt(), segment.readInt(), segment.readInt(), segment.readInt());
    }

    public static void sendGetFile(String name){
        //UASegment segment = new UASegment(CONN_GETFILE_CLIENT);
    	UASegment segment = new UASegment(NEW_CONN_GETFILE_CLIENT);
        try{
            segment.writeString(GameMain.getUIModel());
            segment.writeString(name);
            Utilities.sendRequest(segment);

          //#ifdef buildtest
            System.out.println("Get File : " + name);
          //#endif
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void recvGetFile(UASegment segment){
        String name = segment.readString();
        int version = segment.readInt();
        int fileLength = segment.readInt();
        int startIndex = segment.readInt();
        byte[] data = segment.readBytes();
        //9.16
        //#if NewUI2
        if(name.endsWith(".pkg")){
        	GameMain.resourceManager.mapSize = fileLength;
        }
        //#endif
        if(fileLength == 0 || fileLength <= data.length){
        	GameMain.resourceManager.recvResource(name, version, data);
        }else{
        	GameMain.resourceManager.recvFileData(name, version, data, fileLength, startIndex);
        }
    }

    public static void sendSyncVMVarialbe(int questId, int addr, int value){
        UASegment segment = new UASegment(CONN_VM_VARIABLE_SYNC_CLIENT);

        try{
            segment.writeInt(questId);
            segment.writeInt(addr);
            segment.writeInt(value);

            Utilities.sendRequest(segment);
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }
    }

    public static void sendNotifyServer(int questId, int notifyId, int notifyType, int questionAnswer){
        UASegment segment = new UASegment(CONN_NOTIFY_CLIENT);

        try{
            segment.writeInt(questId);
            segment.writeByte((byte) notifyId);
            segment.writeByte((byte) notifyType);
            segment.writeByte((byte) questionAnswer);

            Utilities.sendRequest(segment);
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }
    }

    public static void recvChaseServer(UASegment segment){
        int sourceId = segment.readInt();
        int targetId = segment.readInt();
        int startX = segment.readShort();
        int startY = segment.readShort();
        //#if ModelID == AndroidAuto
        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
        //# startX <<= 1;
        //# startY <<= 1;
        //# }
		//#elif DoubleScreen == true
        //# startX <<= 1;
        //# startY <<= 1;
		//#endif
        int speed = segment.readUnsignedByte();
        int distanceAllow = segment.readShort();
        segment.handled = true;

        GameSprite gameSprite = GameWorld.getSprite(sourceId);

        if(gameSprite != null){
            gameSprite.sendCommand(VMGame.GAME_COMMAND_SPRITE_START_CHASE, new int[]{
                            1, sourceId, targetId, distanceAllow, startX, startY, speed
            });
        }
    }
    /**绘制世界地图--整图绘制*/
    public static void drawWorldMap(ImageSet img, Vector frame, Vector transit, int mapTileWidth, int mapTileHeight, Graphics g,int x,int y){
    	int mapRows = frame.size();
    	int mapCols = ((byte[])frame.elementAt(0)).length;
    	for(int i=0; i<mapRows; i++){
    		for(int j=0; j<mapCols; j++){
    			img.drawFrame(g, ((byte[])frame.elementAt(i))[j],x + j * mapTileWidth,y + i * mapTileHeight, ((byte[])transit.elementAt(i))[j], 0);
    		}
    	}
    }
    public static void drawWorldMap(ImageSet img, Vector frame, Vector transit, int mapTileWidth, int mapTileHeight, Graphics g){
    	drawWorldMap(img, frame, transit, mapTileWidth, mapTileHeight, g, 0, 0);
    }
    /**绘制世界地图--单屏绘制*/
    public static void drawWorldMap(ImageSet img, Vector frame, Vector transit, int x, int y, int screenWidth, int screenHeight, int mapTileWidth, int mapTileHeight, Graphics g){
    	int mapRows = frame.size();
    	int mapCols = ((byte[])frame.elementAt(0)).length;
    	int mapWidth = mapCols * mapTileWidth;
    	int mapHeight = mapRows * mapTileHeight;
    	//屏幕左上角的地图坐标
    	int screenX;
    	int screenY;
    	if(x < screenWidth/2){
    		screenX = 0;
    	}
    	else if(mapWidth - x < screenWidth/2){
    		screenX = x - (screenWidth - (mapWidth - x));
    	}
    	else{
    		screenX = x - screenWidth/2;
    	}
    	if(y < screenHeight/2){
    		screenY = 0;
    	}
    	else if(mapHeight - y < screenHeight/2){
    		screenY = y - (screenHeight - (mapHeight - y));
    	}
    	else{
    		screenY = y - screenHeight/2;
    	}
    	int _offX = screenX % mapTileWidth;
    	int _offY = screenY % mapTileHeight;
    	//绘制图块起始点坐标
    	int _xx = 0 - _offX;
    	int _yy = 0 - _offY;
    	//绘制行数
    	int _rows = (screenHeight + _offY) % mapTileHeight;
    	if(_rows > 0){
    		_rows = (screenHeight + _offY) / mapTileHeight + 1;
    	}
    	else{
    		_rows = (screenHeight + _offY) / mapTileHeight;
    	}
    	//绘制列数
    	int _cols = (screenWidth + _offX) % mapTileWidth;
    	if(_cols > 0){
    		_cols = (screenWidth + _offX) / mapTileWidth + 1;
    	}
    	else{
    		_cols = (screenWidth + _offX) / mapTileWidth;
    	}
    	//起始图块索引
    	int _sRows = screenY / mapTileHeight;
    	int _sCols = screenX / mapTileWidth;
    	for(int i=0; i<_rows; i++){
    		for(int j=0; j<_cols; j++){
    			img.drawFrame(g, ((byte[])frame.elementAt(i + _sRows))[j + _sCols], _xx + j * mapTileWidth, _yy + i * mapTileHeight, ((byte[])transit.elementAt(i + _sRows))[j + _sCols], 0);
    		}
    	}
    }
    
    /**
     * 取设备IMEI
     * @return
     */
    public static String getIMEI(){
    	String imei = ""; 
    	//Nokia
    	imei = System.getProperty("phone.imei"); 
        if (imei == null || "".equals(imei.trim())){
        	imei = System.getProperty("com.nokia.IMEI"); 
        } 
        if (imei == null || "".equals(imei.trim())){ 
            imei = System.getProperty("com.nokia.mid.imei"); 
        }
    	//SonyEricsson
        if (imei == null || "".equals(imei.trim())){ 
        	imei = System.getProperty("com.sonyericsson.imei");
        }
        //Siemens
        if (imei == null || "".equals(imei.trim())){ 
        	imei = System.getProperty("com.siemens.imei");
        }
        //Motorola
        if (imei == null || "".equals(imei.trim())){ 
        	imei = System.getProperty("com.motorola.IMEI");
        }
        if (imei == null || "".equals(imei.trim())){ 
        	imei = System.getProperty("phone.IMEI");
        }
        //Samsung
        if (imei == null || "".equals(imei.trim())){ 
        	imei = System.getProperty("com.samsung.imei");
        }
        //Other
        if (imei == null || "".equals(imei.trim())){
        	imei = System.getProperty("IMEI");
        }
        
        if(imei == null){
        	imei = "";
        }
        
    	return imei.trim();
    }
    
    /**
     * 处理半透明图片
     * @param src
     * @param alpha
     * @return
     */
    public static Image processAlphaImage(Image src,int alpha){
    	Image ret = null;
    	//半透明
		int w = src.getWidth();
		int h = src.getHeight();
		int[] argb = new int[w*h];
		src.getRGB(argb, 0, w, 0, 0, w, h);
		for (int i = 0; i < argb.length; i++) {
			if(argb[i] == 0xFFFFFFFF){//纯白
				argb[i] = 0x00FFFFFF;
			} else {
				if(argb[i] != 0x00FFFFFF){
					argb[i] = argb[i] + alpha;
				}
			}
			
		}
		ret = Image.createRGBImage(argb, w,h, true);
    	return ret;
    }
    
//#if ModelID == Lenovo
//#    public static void setCurrentConnType(Activity activty)
//#    {
//#    		try{
//#	    		ConnectivityManager cm  = ((ConnectivityManager) activty.getSystemService(activty.CONNECTIVITY_SERVICE));//
//#	    		NetworkInfo ni = cm.getActiveNetworkInfo();//
//#	    		Uri uri =Uri.parse("content://telephony/carriers/preferapn");    
//#    		Cursor cr = activty.getContentResolver().query(uri, null, null, null, null);
//#    		String typeName = null;
//#	    		String name = null;
//#    		if(cr!=null && cr.moveToNext())
//#	    		{    
//#    		     name = cr.getString(cr.getColumnIndex("name"));
//#    		}
//#    		if(ni != null)
//#    		{
//#    			typeName = typeName = ni.getTypeName();
//#    		}
//#    		if("CTWAP".equals(name) && "mobile".equals(typeName))
//#    		{
//#    			setGlobalValue("ConnType",name);
//#    		}
//#  		}
//#  		catch(Exception e)
//# 		{
    			//#ifdef buildtest
	            //# e.printStackTrace();
	            //#endif 
//#  		}
//#  }
//#endif
    
    // 测试用，用聊天向服务器发送日志信息
    public static void sendLog(String str) {
        UASegment segment = new UASegment(201);

        try{
        	segment.writeByte((byte)8);
        	segment.writeInt(-1);
        	segment.writeString(str);
        	segment.writeBytes(new byte[0]);

            Utilities.sendRequest(segment);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
