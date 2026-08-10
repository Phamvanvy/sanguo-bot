package com.pip.mapeditor;

import java.util.Arrays;
import java.util.Random;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.pipimage.image.LandformImage;

/**
 * 模糊地图相关的工具函数。
 * @author lighthu
 */
public class BlurMapUtil {
    /**
     * 生成一个矩形。
     * @param w 宽度
     * @param h 高度
     * @return
     */
    public static byte[][] makeRectangle(int w, int h) {
        byte[][] ret = new byte[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (i == 0) {
                    if (j == 0) {
                        ret[i][j] = 1;  // 0001
                    } else if (j == w - 1) {
                        ret[i][j] = 2;  // 0010
                    } else {
                        ret[i][j] = 3;  // 0011
                    }
                } else if (i == h - 1) {
                    if (j == 0) {
                        ret[i][j] = 4;  // 0100
                    } else if (j == w - 1) {
                        ret[i][j] = 8;  // 1000
                    } else {
                        ret[i][j] = 12; // 1100
                    }
                } else {
                    if (j == 0) {
                        ret[i][j] = 5;  // 0101
                    } else if (j == w - 1) {
                        ret[i][j] = 10; // 1010
                    } else {
                        ret[i][j] = 15; // 1111
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * 根据模糊图层的数据生成指定一种地形的格点数据。
     * @param data 模糊图层数据
     * @param lfid 需求地形
     * @return
     */
    public static byte[][] makeLayer(byte[][] data, int lfid) {
        int rows = data.length;
        int cols = data[0].length;
        byte[][] ret = new byte[rows + 2][cols + 2];
        byte[][] sg = makeRectangle(3, 3);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (data[i][j] != lfid) {
                    continue;
                }
                ret[i][j] |= sg[0][0];
                ret[i][j + 1] |= sg[0][1];
                ret[i][j + 2] |= sg[0][2];
                ret[i + 1][j] |= sg[1][0];
                ret[i + 1][j + 1] |= sg[1][1];
                ret[i + 1][j + 2] |= sg[1][2];
                ret[i + 2][j] |= sg[2][0];
                ret[i + 2][j + 1] |= sg[2][1];
                ret[i + 2][j + 2] |= sg[2][2];
            }
        }
        return ret;
    }
    
    /**
     * 绘制模糊地图的地形。
     * @param gc 绘图环境
     * @param data 格点数据
     * @param img 地形图片 
     * @param rand 随机数生成器 
     * @param x 绘图X位置
     * @param y 绘图Y位置
     * @param tw 格点宽度
     * @param th 格点高度
     * @param ratio 缩放比例
     */
    public static void drawLandform(GC gc, LandformImage img, Random rand, byte[][] data, int x, int y, int tw, int th, double ratio) {
        img.generateSearchTable();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] == 0) {
                    continue;
                }
                int rtw = (int)(tw * ratio);
                int rth = (int)(th * ratio);
                int tx = x + (int)(tw * j * ratio);
                int ty = y + (int)(th * i * ratio);
                int[] tile = img.getTile(rand, data[i][j]);
                if (tile[0] == -1) {
                    continue;
                }
                Image timg = img.getImageDraw(tile[0]).createSWTImage(gc.getDevice(), tile[1]);
                gc.drawImage(timg, 0, 0, tw, th, tx, ty, rtw, rth);
                timg.dispose();
            }
        }
    }
}
