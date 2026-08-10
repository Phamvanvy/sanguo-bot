package com.pip.image;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.pip.sanguo.GameMain;

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class ImageSet{
    /**
     * 模式1：內置PipImage对象
     */
    public PipImage pipImg;

    /**
     * 模式2：内置Image对象，所有图块等大
     */
    Image[] img; //图片
    int tileNum; //每张图片图块个数
    long[] tileRects;
    //#if TransitMethod == CreateImageWithBuffer
    //# Hashtable transBuffer;
    //#endif

    /**
     * 创建一个简单图片组，其中每个图片的宽度和高度都是一样的。
     * @param imgFile 图片文件路径
     * @param rows 图片组中包含图片的行数
     * @param cols 图片组中包含图片的列数
     */
    public ImageSet(String imgFile, int rows, int cols){
        byte[] data = GameMain.resourceManager.findResource(imgFile);
        Image img = Image.createImage(data, 0, data.length);
        int tileWidth = img.getWidth() / cols;
        int tileHeight = img.getHeight() / rows;
        initSimpleFrameSet(img, tileWidth, tileHeight, rows, cols);
    }

    /**
     * 创建一个简单图片组，其中每个图片的宽度和高度都是一样的。
     * @param imgFile 图片文件路径
     * @param fw 图片组中小图片的宽度
     * @param fh 图片组中小图片的高度
     * @param ns 无意义，只是为了和上面一个构造方法区分
     */
    public ImageSet(String imgFile, int fw, int fh, int ns){
        byte[] data = GameMain.resourceManager.findResource(imgFile);
        Image img = Image.createImage(data, 0, data.length);
        int rows = img.getHeight() / fh;
        int cols = img.getWidth() / fw;
        initSimpleFrameSet(img, fw, fh, rows, cols);
    }

    private void initSimpleFrameSet(Image img, int fw, int fh, int rows, int cols){
        this.img = new Image[] { img };
        tileNum = rows * cols;
        tileRects = new long[tileNum];
        long tileWidth = fw;
        long tileHeight = fh;
        for(int i = 0; i < tileNum; i++){
            long tileX = (i % cols) * tileWidth;
            long tileY = (i / cols) * tileHeight;
            tileRects[i] = (tileX << 48) | (tileY << 32) | (tileWidth << 16) | tileHeight;
        }
    }
    
    private void initMergPip(PipImage pipImage){
        this.img = pipImage.getMergeImage();
        int[] frameInfo = pipImage.getMergeFrameInfo();
        tileNum = frameInfo.length >> 1;
        tileRects = new long[tileNum];
        
        for(int i = 0; i < tileNum; i++){
            long tileX = (frameInfo[i << 1] >> 16) & 0xFFFF;
            long tileY = frameInfo[i << 1] & 0xFFFF;
            long tileWidth = (frameInfo[(i << 1) + 1] >> 16) & 0xFFFF;
            long tileHeight = frameInfo[(i << 1) + 1] & 0xFFFF;
            tileRects[i] = (tileX << 48) | (tileY << 32) | (tileWidth << 16) | tileHeight;
        }
    }

    public ImageSet(String imgFile) throws IOException {
        byte[] data = GameMain.resourceManager.findResource(imgFile);
        if(data[0] == (byte)0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47){
            Image img = Image.createImage(data, 0, data.length);
            initSimpleFrameSet(img, img.getWidth(), img.getHeight(), 1, 1);
        }else{
            pipImg = new PipImage(new ByteArrayInputStream(data));
            
            if(pipImg.isMergeImage()){
                initMergPip(pipImg);
                pipImg = null;
            }
        }
    }

    public ImageSet(byte[] data) throws IOException {
        if(data[0] == (byte)0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47){
            Image img = Image.createImage(data, 0, data.length);
            initSimpleFrameSet(img, img.getWidth(), img.getHeight(), 1, 1);
        }else{
            pipImg = new PipImage(new ByteArrayInputStream(data));
            
            if(pipImg.isMergeImage()){
                initMergPip(pipImg);
                pipImg = null;
            }
        }
    }

    public void drawFrame(Graphics g, int fid, int x, int y){
        drawFrame(g, fid, x, y, 0, Graphics.TOP | Graphics.LEFT);
    }

    public void drawFrame(Graphics g, int fid, int x, int y, int trans){
        drawFrame(g, fid, x, y, trans, Graphics.TOP | Graphics.LEFT);
    }

    public void drawFrame(Graphics g, int fid, int x, int y, int trans, int anchor){
        int mx = x;
        int my = y;
        
        if(pipImg != null){
            // PipImage模式
            //TODO test
            GameMain.drawTimes++;
            pipImg.draw(g, fid, x, y, trans, anchor);
            return;
        }

        // 简单Image模式
        long rct = tileRects[fid];
        int fx = (short)(rct >> 48);
        int iid = (fx >> 14) & 0x03;
        fx &= 0x3FFF;
        int fy = (short)(rct >> 32);
        int fw = (short)(rct >> 16);
        int fh = (short)rct;
        if (trans < 4) {
            if((anchor & Graphics.HCENTER) > 0){
                mx -= fw / 2;
            }else if((anchor & Graphics.RIGHT) > 0){
                mx -= fw;
            }
            if((anchor & Graphics.VCENTER) > 0){
                my -= fh / 2;
            }else if((anchor & Graphics.BOTTOM) > 0){
                my -= fh;
            }
        }else{
            if((anchor & Graphics.HCENTER) > 0){
                mx -= fh / 2;
            }else if((anchor & Graphics.RIGHT) > 0){
                mx -= fh;
            }
            if((anchor & Graphics.VCENTER) > 0){
                my -= fw / 2;
            }else if((anchor & Graphics.BOTTOM) > 0){
                my -= fw;
            }
        }

        drawPNGFrame(g, img[iid], fid, fx, fy, fw, fh, trans, mx, my);
        return;
    }

    public int getFrameCount(){
        if(pipImg != null){
            return pipImg.getFrameCount();
        }
        return tileNum;
    }

    public int getBlockCount(){
        if(pipImg != null){
            return pipImg.getBlockCount();
        }

        return 0;
    }

    public int getFrameWidth(int frame){
        if(pipImg != null){
            return pipImg.getWidth(frame);
        }
        return (int)((tileRects[frame] >> 16) & 0xFFFF);
    }

    public int getFrameHeight(int frame){
        if(pipImg != null){
            return pipImg.getHeight(frame);
        }
        return (int)(tileRects[frame] & 0xFFFF);
    }

    /**
     * 在屏幕上绘制图片的一部分。
     * @param g 
     * @param image 原始图片
     * @param x_src 需要绘制的部分在原始图片中的X位置
     * @param y_src 需要绘制的部分在原始图片中的Y位置
     * @param width 需要绘制的部分的宽度
     * @param height 需要绘制的部分的高度
     * @param trans 翻转类型，取值参考MIDP2.0规范
     * @param x_dest 绘制屏幕位置
     * @param y_dest 绘制屏幕位置
     */
    public void drawPNGFrame(Graphics g, Image image, int fid, int x_src, int y_src, int width, int height, int trans, int x_dest, int y_dest){
        //TODO test
        GameMain.drawTimes++;
        
        //#if TransitMethod == NokiaUI
        //# int clipX = g.getClipX();
        //# int clipY = g.getClipY();
        //# int clipWidth = g.getClipWidth();
        //# int clipHeight = g.getClipHeight();
        //# if(trans < 4){
        //#     g.clipRect(x_dest, y_dest, width, height);
        //# }else{
        //#     g.clipRect(x_dest, y_dest, height, width);
        //# }
        //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //# int nokiatrans = PipImage.TRANS_MAP[trans];
        //# switch (trans) {
        //#     case PipImage.TRANS_MIRROR_ROT180:
        //#         dg.drawImage(image, x_dest - x_src, y_dest - (image.getHeight() - height - y_src), Graphics.TOP | Graphics.LEFT, nokiatrans);
        //#         break;
        //#     case PipImage.TRANS_MIRROR:
        //#         dg.drawImage(image, x_dest - (image.getWidth() - width - x_src), y_dest - y_src, Graphics.TOP | Graphics.LEFT, nokiatrans);
        //#         break;
        //#     case PipImage.TRANS_ROT180:
        //#         dg.drawImage(image, x_dest - (image.getWidth() - width - x_src), y_dest - (image.getHeight() - height - y_src), Graphics.TOP | Graphics.LEFT, nokiatrans);
        //#         break;
        //#     case PipImage.TRANS_ROT90:
        //#         dg.drawImage(image, x_dest - (image.getHeight() - height - y_src), y_dest - x_src, Graphics.TOP | Graphics.LEFT, nokiatrans);
        //#         break;
        //#     case PipImage.TRANS_ROT270:
        //#         dg.drawImage(image, x_dest - y_src, y_dest - (image.getWidth() - width - x_src), Graphics.TOP | Graphics.LEFT, nokiatrans);
        //#         break;
        //#     default:
        //#         g.drawImage(image, x_dest - x_src, y_dest - y_src, Graphics.TOP | Graphics.LEFT);
        //#         break;
        //# }
        //# dg = null;
        //# g.setClip(clipX, clipY, clipWidth, clipHeight);
        //#elif TransitMethod == CreateImageWithBuffer
        //# int clipX = g.getClipX();
        //# int clipY = g.getClipY();
        //# int clipWidth = g.getClipWidth();
        //# int clipHeight = g.getClipHeight();
        //# if(trans < 4){
            //# g.clipRect(x_dest, y_dest, width, height);
        //# }else{
            //# g.clipRect(x_dest, y_dest, height, width);
        //# }
        //# if(trans != 0){
            //# if(transBuffer == null){
                //# transBuffer = new Hashtable();
            //# }
            //# Integer key = new Integer((fid << 3) | (trans & 0x07));
            //# Image frameImg = (Image)transBuffer.get(key);
            //# if(frameImg == null){
                //# frameImg = Image.createImage(image, x_src, y_src, width, height, trans);
                //# transBuffer.put(key, frameImg);
            //# }
            //# g.drawImage(frameImg, x_dest, y_dest, Graphics.TOP | Graphics.LEFT);
        //# }else{
            //# g.drawImage(image, x_dest - x_src, y_dest - y_src, Graphics.TOP | Graphics.LEFT);
        //# }
        //# g.setClip(clipX, clipY, clipWidth, clipHeight);
        //#elif TransitMethod == CreateImage
        //# int clipX = g.getClipX();
        //# int clipY = g.getClipY();
        //# int clipWidth = g.getClipWidth();
        //# int clipHeight = g.getClipHeight();
        //# if(trans < 4){
        //#     g.clipRect(x_dest, y_dest, width, height);
        //# }else{
        //#     g.clipRect(x_dest, y_dest, height, width);
        //# }
        //# if (trans != 0) {
        //#     Image frameImg = Image.createImage(image, x_src, y_src, width, height, trans);
        //#     g.drawImage(frameImg, x_dest, y_dest, Graphics.TOP | Graphics.LEFT);
        //# } else {
        //#     g.drawImage(image, x_dest - x_src, y_dest - y_src, Graphics.TOP | Graphics.LEFT);
        //# }
        //# g.setClip(clipX, clipY, clipWidth, clipHeight);
        //#else
        g.drawRegion(image, x_src, y_src, width, height, trans, x_dest, y_dest, Graphics.TOP | Graphics.LEFT);
        //#endif
    }
}
