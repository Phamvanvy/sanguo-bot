package com.pip.image;


import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;


public abstract class RGBGraphics{
    protected int clipX;
    protected int clipY;
    protected int clipWidth;
    protected int clipHeight;
    protected int drawWidth;
    protected int drawHeight;

    private int[] oldClip = null;

    protected void saveClip(){
        oldClip = new int[4];

        oldClip[0] = clipX;
        oldClip[1] = clipY;
        oldClip[2] = clipWidth;
        oldClip[3] = clipHeight;
    }

    protected void restoreClip(){
        if(oldClip == null){
            return;
        }

        clipX = oldClip[0];
        clipY = oldClip[1];
        clipWidth = oldClip[2];
        clipHeight = oldClip[3];

        oldClip = null;
    }

    protected int correctX(int x, int width, int anchor){
        if((anchor & Graphics.HCENTER) != 0){
            return x - width / 2;
        }else if((anchor & Graphics.LEFT) != 0){
            return x;
        }else{
            return x - width;
        }
    }

    protected int correctY(int y, int height, int anchor){
        if((anchor & Graphics.VCENTER) != 0){
            return y - height / 2;
        }else if((anchor & Graphics.TOP) != 0){
            return y;
        }else{
            return y - height;
        }
    }

    protected int deCorrectX(int x, int width, int anchor){
        if((anchor & Graphics.HCENTER) != 0){
            return x + width / 2;
        }else if((anchor & Graphics.LEFT) != 0){
            return x;
        }else{
            return x + width;
        }
    }

    protected int deCorrectY(int y, int height, int anchor){
        if((anchor & Graphics.VCENTER) != 0){
            return y + height / 2;
        }else if((anchor & Graphics.TOP) != 0){
            return y;
        }else{
            return y + height;
        }
    }

    public int getClipHeight(){
        return clipHeight;
    }

    public int getClipWidth(){
        return clipWidth;
    }

    public int getClipX(){
        return clipX;
    }

    public int getClipY(){
        return clipY;
    }

    public void setClip(int x, int y, int width, int height){
        clipX = x;
        clipY = y;
        clipWidth = width;
        clipHeight = height;

        clipRect(0, 0, drawWidth, drawHeight);
    }

    public void clipRect(int x, int y, int width, int height){
        int newX1 = Math.max(x, clipX);
        int newY1 = Math.max(y, clipY);
        int newX2 = Math.min(x + width, clipX + clipWidth);
        int newY2 = Math.min(y + height, clipY + clipHeight);

        if(newX1 < 0){
            newX1 = 0;
        }else if(newX1 > drawWidth){
            newX1 = drawWidth - 1;
        }

        if(newY1 < 0){
            newY1 = 0;
        }else if(newY1 > drawHeight){
            newY1 = drawHeight - 1;
        }

        if(newX2 < 0){
            newX2 = 0;
        }else if(newX2 > drawWidth){
            newX2 = drawWidth - 1;
        }

        if(newY2 < 0){
            newY2 = 0;
        }else if(newY2 > drawHeight){
            newY2 = drawHeight - 1;
        }

        clipX = newX1;
        clipY = newY1;
        clipWidth = Math.max(0, newX2 - newX1);
        clipHeight = Math.max(0, newY2 - newY1);
    }

    public void setColor(int r, int g, int b){
        setColor(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public abstract void copyArea(int x_src, int y_src, int width, int height, int x_dest, int y_dest, int anchor);

    public abstract void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    public abstract void drawChar(char character, int x, int y, int anchor);

    public abstract void drawChars(char[] data, int offset, int length, int x, int y, int anchor);

    public abstract void drawImage(RGBImage image, int x, int y, int anchor);

    public abstract void drawCircle(int x0, int y0, int radius);

    public abstract void drawLine(int x1, int y1, int x2, int y2);

    public abstract void drawRect(int x, int y, int width, int height);

    public abstract void drawRegion(RGBImage image, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest, int anchor);

    public abstract void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha);

    public abstract void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);

    public abstract void drawString(String str, int x, int y, int anchor);

    public abstract void drawSubstring(String str, int offset, int len, int x, int y, int anchor);

    public abstract void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    public abstract void fillRect(int x, int y, int width, int height);

    public abstract void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);

    public abstract void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3);

    public abstract int getBlueComponent();

    public abstract int getColor();

    public abstract int getDisplayColor(int arg0);

    public abstract Font getFont();

    public abstract int getGrayScale();

    public abstract int getGreenComponent();

    public abstract int getRedComponent();

    public abstract int getStrokeStyle();

    public abstract int getTranslateX();

    public abstract int getTranslateY();

    public abstract void setColor(int color);

    public abstract void setFont(Font font);

    public abstract void setGrayScale(int value);

    public abstract void setStrokeStyle(int style);

    public abstract void translate(int x, int y);
}
