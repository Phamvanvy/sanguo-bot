package com.pipimage.image;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;


public class PipImagePalette{
    private static final byte[] HEAD = {
                    'P', 'L', 'T', 'E'
    };

    public static int[] tmp = {
                    0, 0, 0, 6, 6, 6, 10, 10, 10, 14, 14, 14, 18, 18, 18, 22, 22, 22, 26, 26, 26, 30, 30, 30, 34, 34, 34, 38, 38, 38, 42, 42, 42, 46, 46, 46, 50, 50, 50, 54, 54, 54, 59, 59, 59, 63,
                    63, 63, 20, 0, 0, 23, 0, 0, 28, 0, 0, 33, 1, 1, 38, 2, 2, 47, 4, 3, 54, 6, 5, 63, 0, 0, 63, 0, 0, 63, 14, 11, 63, 18, 15, 63, 22, 19, 63, 22, 18, 63, 32, 28, 63, 37, 33, 63, 43,
                    39, 18, 5, 2, 21, 6, 3, 24, 6, 3, 27, 6, 3, 31, 10, 4, 36, 15, 7, 40, 20, 9, 44, 25, 11, 48, 30, 15, 53, 36, 18, 56, 41, 21, 60, 46, 24, 63, 50, 28, 63, 55, 33, 63, 60, 39, 63,
                    62, 44, 12, 6, 0, 22, 15, 0, 32, 25, 0, 41, 34, 1, 48, 43, 1, 57, 50, 1, 59, 56, 1, 62, 62, 1, 62, 62, 21, 63, 63, 0, 63, 63, 10, 63, 63, 19, 63, 63, 28, 63, 63, 41, 63, 63, 49,
                    63, 63, 60, 5, 3, 2, 7, 5, 3, 10, 7, 5, 13, 10, 7, 16, 13, 9, 18, 16, 11, 21, 19, 14, 24, 22, 16, 27, 25, 19, 29, 28, 21, 32, 31, 24, 35, 34, 28, 38, 37, 31, 41, 40, 36, 45, 44,
                    40, 49, 46, 43, 0, 0, 15, 0, 0, 19, 1, 1, 27, 2, 2, 32, 3, 3, 37, 2, 2, 41, 3, 3, 46, 0, 3, 51, 0, 0, 58, 0, 0, 63, 7, 7, 63, 14, 14, 63, 21, 21, 63, 28, 28, 63, 38, 38, 63, 42,
                    42, 63, 7, 4, 14, 9, 5, 17, 11, 6, 20, 13, 8, 23, 15, 10, 26, 18, 12, 29, 21, 14, 32, 24, 17, 35, 27, 20, 38, 31, 23, 41, 34, 27, 44, 38, 31, 47, 42, 35, 50, 46, 40, 53, 50, 44,
                    56, 54, 49, 59, 5, 9, 10, 7, 11, 13, 9, 14, 16, 11, 17, 19, 13, 20, 22, 16, 23, 25, 19, 26, 28, 22, 30, 32, 26, 33, 35, 30, 37, 39, 33, 40, 42, 38, 44, 46, 42, 48, 50, 47, 52, 53,
                    52, 56, 57, 57, 60, 61, 0, 4, 2, 0, 8, 5, 0, 11, 8, 1, 15, 11, 2, 19, 15, 4, 23, 19, 6, 26, 22, 8, 30, 26, 11, 34, 29, 13, 38, 33, 17, 42, 37, 21, 46, 41, 26, 50, 45, 31, 54, 49,
                    36, 58, 52, 42, 62, 57, 23, 10, 6, 28, 14, 8, 33, 18, 10, 36, 23, 13, 40, 28, 16, 43, 32, 19, 45, 33, 21, 47, 33, 24, 49, 33, 27, 50, 36, 30, 52, 39, 33, 54, 42, 37, 55, 45, 40,
                    57, 48, 43, 58, 51, 47, 60, 54, 50, 12, 4, 0, 16, 6, 0, 21, 8, 1, 24, 10, 2, 27, 12, 3, 30, 15, 6, 33, 18, 8, 36, 21, 11, 39, 25, 14, 42, 29, 17, 45, 33, 21, 48, 37, 25, 51, 41,
                    30, 55, 46, 35, 59, 50, 41, 63, 56, 48, 9, 3, 1, 12, 5, 2, 15, 7, 3, 18, 9, 4, 22, 13, 7, 25, 16, 9, 28, 19, 12, 32, 23, 15, 35, 28, 18, 39, 32, 22, 42, 36, 27, 46, 41, 31, 49,
                    45, 36, 53, 50, 42, 57, 55, 48, 61, 60, 55, 0, 7, 0, 0, 9, 0, 1, 12, 0, 2, 15, 1, 4, 19, 2, 6, 24, 3, 8, 31, 5, 11, 39, 7, 14, 45, 9, 0, 49, 0, 6, 49, 0, 11, 49, 0, 19, 49, 19,
                    26, 49, 22, 32, 49, 26, 28, 49, 31, 0, 6, 23, 0, 8, 27, 1, 10, 31, 3, 12, 35, 5, 15, 40, 7, 17, 44, 10, 20, 48, 13, 23, 50, 17, 27, 52, 21, 31, 55, 25, 35, 56, 30, 39, 58, 35, 43,
                    59, 39, 47, 60, 44, 51, 61, 50, 55, 63, 9, 5, 3, 12, 7, 4, 15, 10, 6, 18, 13, 8, 22, 17, 11, 25, 20, 13, 28, 23, 16, 31, 27, 19, 34, 30, 22, 37, 34, 26, 40, 37, 30, 44, 41, 34,
                    47, 45, 38, 50, 49, 42, 53, 52, 46, 56, 55, 50, 0, 0, 0, 19, 23, 9, 15, 22, 7, 25, 30, 21, 24, 32, 0, 13, 13, 0, 21, 25, 11, 25, 26, 13, 13, 13, 63, 0, 38, 38, 29, 28, 15, 19, 26,
                    15, 0, 0, 0, 36, 36, 0, 50, 50, 0, 63, 63, 0
    };
    public static int[] genPal = {
                    0xff000000, 0xff181818, 0xff282828, 0xff383838, 0xff484848, 0xff585858, 0xff686868, 0xff787878, 0xff888888, 0xff989898, 0xffa8a8a8, 0xffb8b8b8, 0xffc8c8c8, 0xffd8d8d8, 0xffececec,
                    0xfffcfcfc, 0xff500000, 0xff5c0000, 0xff700000, 0xff840404, 0xff980808, 0xffbc100c, 0xffd81814, 0xfffc0000, 0xfffc0000, 0xfffc382c, 0xfffc483c, 0xfffc584c, 0xfffc5848, 0xfffc8070,
                    0xfffc9484, 0xfffcac9c, 0xff481408, 0xff54180c, 0xff60180c, 0xff6c180c, 0xff7c2810, 0xff903c1c, 0xffa05024, 0xffb0642c, 0xffc0783c, 0xffd49048, 0xffe0a454, 0xfff0b860, 0xfffcc870,
                    0xfffcdc84, 0xfffcf09c, 0xfffcf8b0, 0xff301800, 0xff583c00, 0xff806400, 0xffa48804, 0xffc0ac04, 0xffe4c804, 0xffece004, 0xfff8f804, 0xfff8f854, 0xfffcfc00, 0xfffcfc28, 0xfffcfc4c,
                    0xfffcfc70, 0xfffcfca4, 0xfffcfcc4, 0xfffcfcf0, 0xff140c08, 0xff1c140c, 0xff281c14, 0xff34281c, 0xff403424, 0xff48402c, 0xff544c38, 0xff605840, 0xff6c644c, 0xff747054, 0xff807c60,
                    0xff8c8870, 0xff98947c, 0xffa4a090, 0xffb4b0a0, 0xffc4b8ac, 0xff00003c, 0xff00004c, 0xff04046c, 0xff080880, 0xff0c0c94, 0xff0808a4, 0xff0c0cb8, 0xff000ccc, 0xff0000e8, 0xff0000fc,
                    0xff1c1cfc, 0xff3838fc, 0xff5454fc, 0xff7070fc, 0xff9898fc, 0xffa8a8fc, 0xff1c1038, 0xff241444, 0xff2c1850, 0xff34205c, 0xff3c2868, 0xff483074, 0xff543880, 0xff60448c, 0xff6c5098,
                    0xff7c5ca4, 0xff886cb0, 0xff987cbc, 0xffa88cc8, 0xffb8a0d4, 0xffc8b0e0, 0xffd8c4ec, 0xff142428, 0xff1c2c34, 0xff243840, 0xff2c444c, 0xff345058, 0xff405c64, 0xff4c6870, 0xff587880,
                    0xff68848c, 0xff78949c, 0xff84a0a8, 0xff98b0b8, 0xffa8c0c8, 0xffbcd0d4, 0xffd0e0e4, 0xffe4f0f4, 0xff001008, 0xff002014, 0xff002c20, 0xff043c2c, 0xff084c3c, 0xff105c4c, 0xff186858,
                    0xff207868, 0xff2c8874, 0xff349884, 0xff44a894, 0xff54b8a4, 0xff68c8b4, 0xff7cd8c4, 0xff90e8d0, 0xffa8f8e4, 0xff5c2818, 0xff703820, 0xff844828, 0xff905c34, 0xffa07040, 0xffac804c,
                    0xffb48454, 0xffbc8460, 0xffc4846c, 0xffc89078, 0xffd09c84, 0xffd8a894, 0xffdcb4a0, 0xffe4c0ac, 0xffe8ccbc, 0xfff0d8c8, 0xff301000, 0xff401800, 0xff542004, 0xff602808, 0xff6c300c,
                    0xff783c18, 0xff844820, 0xff90542c, 0xff9c6438, 0xffa87444, 0xffb48454, 0xffc09464, 0xffcca478, 0xffdcb88c, 0xffecc8a4, 0xfffce0c0, 0xff240c04, 0xff301408, 0xff3c1c0c, 0xff482410,
                    0xff58341c, 0xff644024, 0xff704c30, 0xff805c3c, 0xff8c7048, 0xff9c8058, 0xffa8906c, 0xffb8a47c, 0xffc4b490, 0xffd4c8a8, 0xffe4dcc0, 0xfff4f0dc, 0xff001c00, 0xff002400, 0xff043000,
                    0xff083c04, 0xff104c08, 0xff18600c, 0xff207c14, 0xff2c9c1c, 0xff38b424, 0xff00c400, 0xff18c400, 0xff2cc400, 0xff4cc44c, 0xff68c458, 0xff80c468, 0xff70c47c, 0xff00185c, 0xff00206c,
                    0xff04287c, 0xff0c308c, 0xff143ca0, 0xff1c44b0, 0xff2850c0, 0xff345cc8, 0xff446cd0, 0xff547cdc, 0xff648ce0, 0xff789ce8, 0xff8cacec, 0xff9cbcf0, 0xffb0ccf4, 0xffc8dcfc, 0xff24140c,
                    0xff301c10, 0xff3c2818, 0xff483420, 0xff58442c, 0xff645034, 0xff705c40, 0xff7c6c4c, 0xff887858, 0xff948868, 0xffa09478, 0xffb0a488, 0xffbcb498, 0xffc8c4a8, 0xffd4d0b8, 0xffe0dcc8,
                    0xff000000, 0xff4c5c24, 0xff3c581c, 0xff647854, 0xff608000, 0xff343400, 0xff54642c, 0xff646834, 0xff3434fc, 0xff009898, 0xff74703c, 0xff4c683c, 0xff000000, 0xff909000, 0xffc8c800,
                    0xfffcfc00
    };

    public static int[][][] data = new int[32][32][32];

    public static int[] col_diff = new int[3 * 128];

    public static void main(String[] args){
//        for(int i = 0; i < tmp.length; i += 3){
//            int rgb = toRGB(tmp[i] << 2, tmp[i + 1] << 2, tmp[i + 2] << 2);
//            System.out.print("0x" + Integer.toHexString(0xff000000 | rgb) + " ,");
//        }

        int i = getBestfitColor(0xc9, 0x18, 0x18);
        System.out.println(i + " clr:" + Integer.toHexString(genPal[i]));

        createRgbTable();
        System.out.println();
    }

    public static void init(){
        int i;
        for(i = 1; i < 64; i++){
            int k = i * i;
            col_diff[0 + i] = col_diff[0 + 128 - i] = k * (59 * 59);
            col_diff[128 + i] = col_diff[128 + 128 - i] = k * (30 * 30);
            col_diff[256 + i] = col_diff[256 + 128 - i] = k * (11 * 11);
        }
    }

    public static void createRgbTable(){
        int i, r, g, b;
        for(r = 0, i = 0; r < 32; r++)
            for(g = 0; g < 32; g++){
                for(b = 0; b < 32; b++)
                    data[r][g][b] = getBestfitColor(r << 1, g << 1, b << 1);
                if(++i >= 4){
                    i = 0;
                }
            }
    }

    public static int getBestfitColor(int r, int g, int b){
        int i, lowest, bestfit;
        if(col_diff[1] == 0)
            init();
        bestfit = 0;
        lowest = Integer.MAX_VALUE;
        if((r == 63) && (g == 0) && (b == 63))
            i = 0;
        else
            i = 1;

        while(i < 256){
            int rgb = genPal[i];
            int[] cc = parseRGB(rgb);

            int rr = cc[0];
            int gg = cc[1];
            int bb = cc[2];

            int dr = Math.abs(rr - r);
            int dg = Math.abs(gg - g);
            int db = Math.abs(bb - b);

            if(dr == 0 && dg == 0 && db == 0)
                return i;
            else if(dr + dg + db < lowest){
                lowest = dr + dg + db;
                bestfit = i;
            }
            i++;
        }

        return bestfit;
    }

    private int[] palette;

    private int index;
    
    public PipImagePalette duplicate() {
    	PipImagePalette pal = new PipImagePalette();
    	pal.palette = new int[palette.length];
    	System.arraycopy(palette, 0, pal.palette, 0, palette.length);
    	return pal;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public int[] getPalette(){
        return palette;
    }

    public int getColor(int idx){
        return palette[idx];
    }

    public void setPalette(int[] palette){
    	this.palette = palette;
    }

    public void setPalette(int[] palette, byte[] trans){
        this.palette = palette;
        if(trans != null){
            for(int i = 0; i < palette.length; i++){
                byte a = 0;
                if(i >= trans.length)
                    a = (byte)0xff;
                else
                    a = trans[i];
                palette[i] |= a << 24;
            }
        } else {
        	for(int i = 0; i < palette.length; i++){
                palette[i] |= 0xff << 24;
            }
        }
    }

    public void save(DataOutputStream dos) throws IOException{
        dos.writeInt(palette.length);
        dos.write(HEAD);
        for(int i = 0; i < palette.length; i++){
            dos.writeInt(palette[i]);
        }
    }

    public void read(DataInputStream dis) throws IOException{
        int len = dis.readInt();
        int[] palette = new int[len];

        byte[] head = new byte[4];
        dis.read(head);

        for(int i = 0; i < palette.length; i++){
            palette[i] = dis.readInt();
        }

        setPalette(palette);
    }

    public void optimize(int clrCount){
        int[] newPalette = new int[clrCount];
        System.arraycopy(palette, 0, newPalette, 0, clrCount);
        palette = newPalette;
    }

    public static final int toRGB(int r, int g, int b){
        return (r << 16) | (g << 8) | b;
    }

    public static final int[] parseRGB(int rgb){
        int rr = (rgb & 0xff0000) >> 16;
        int gg = (rgb & 0x00ff00) >> 8;
        int bb = rgb & 0x0000ff;
        return new int[]{
                        rr, gg, bb
        };
    }

    public String toString(){
        return "µ÷É«°å" + index;
    }

    public PaletteData createSWTPalette() {
    	RGB[] colors = new RGB[palette.length];
    	for (int i = 0; i < palette.length; i++) {
    		int[] aa = parseRGB(palette[i]);
    		colors[i] = new RGB(aa[0], aa[1], aa[2]);
    	}
    	return new PaletteData(colors);
    }
    
    public int getTransparentIndex() {
    	for (int i = 0; i < palette.length; i++) {
    		if ((palette[i] & 0xFF000000) == 0) {
    			return i;
    		}
    	}
    	return -1;
    }
}
