package com.pipimage.image;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;

import com.pipimage.png.ColorQuantization;

public class PipImageDraw{
    private PipImagePalette palette;
    private PipImageData data;
    private int[] refPalette;
    private Map<Integer, Integer> colorSearchMap;

    public PipImageDraw(PipImagePalette palette, PipImageData data){
        this.palette = palette;
        this.data = data;
    }
    
    public void setRefPalette(int[] pal) {
    	refPalette = pal;
    }

    private int[] transit(int[] rgb, int trans) {
    	if (trans == 0) {
    		return rgb;
    	}
    	int w = data.getWidth();
    	int h = data.getHeight();
    	int[][] ret;
    	if (trans < 4) {
    		ret = new int[h][w];
    	} else {
    		ret = new int[w][h];
    	}
    	int srcpos = 0;
    	for (int y = 0; y < h; y++) {
    		for (int x = 0; x < w; x++) {
    			switch (trans) {
    			case 1:
    				ret[h - 1 - y][x] = rgb[srcpos];
    				break;
    			case 2:
    				ret[y][w - 1 - x] = rgb[srcpos];
    				break;
    			case 3:
    				ret[h - 1 - y][w - 1 - x] = rgb[srcpos];
    				break;
    			case 4:
    				ret[x][y] = rgb[srcpos];
    				break;
    			case 5:
    				ret[x][h - 1 - y] = rgb[srcpos];
    				break;
    			case 6:
    				ret[w - 1 - x][y] = rgb[srcpos];
    				break;
    			case 7:
    				ret[w - 1 - x][h - 1 - y] = rgb[srcpos];
    				break;
    			}
    			srcpos++;
    		}
    	}
    	int[] ret2 = new int[w * h];
    	int tarpos = 0;
    	for (int i = 0; i < ret.length; i++) {
    		System.arraycopy(ret[i], 0, ret2, tarpos, ret[i].length);
    		tarpos += ret[i].length;
    	}
    	return ret2;
    }
    
    private ImageData make(PipImagePalette palette, int trans){
        int w = data.getWidth();
        int h = data.getHeight();
        int[] idata = transit(this.data.getData(), trans);
        ImageData bufferImg;
        if (palette != null) {
	        if (trans < 4) {
	        	bufferImg = new ImageData(w, h, 32, new PaletteData(0x0000FF00, 0x00FF0000, 0xFF000000));
	        } else {
	        	bufferImg = new ImageData(h, w, 32, new PaletteData(0x0000FF00, 0x00FF0000, 0xFF000000));
	        }
	        bufferImg.alphaData = new byte[idata.length];
	        int pos1 = 0, pos2 = 0;
	        int[] oldpal = palette.getPalette();
	        int[] pal;
	        if (refPalette == null) {
	        	pal = oldpal;
	        } else {
	        	pal = new int[oldpal.length];
	        	for (int i = 0; i < oldpal.length; i++) {
	        		pal[i] = getRefColor(oldpal[i]);
	        	}
	        }
	        if (trans < 4) {
		        for (int i = 0; i < h; i++) {
		            int p1 = pos1;
		            int p2 = pos2;
		            for (int k = 0; k < w; k++) {
		                int c = pal[idata[p1++] & 0xFFFF];
		                bufferImg.data[p2++] = (byte)(c);
		                bufferImg.data[p2++] = (byte)(c >> 8); 
	                    bufferImg.data[p2++] = (byte)(c >> 16); 
	                    bufferImg.data[p2++] = (byte)(c >> 24);
		            }
		        	pos1 += w;
		        	pos2 += bufferImg.bytesPerLine;
		        }
	        } else {
		        for (int i = 0; i < w; i++) {
	                int p1 = pos1;
	                int p2 = pos2;
	                for (int k = 0; k < h; k++) {
	                    int c = pal[idata[p1++] & 0xFFFF];
	                    bufferImg.data[p2++] = (byte)(c); 
	                    bufferImg.data[p2++] = (byte)(c >> 8); 
	                    bufferImg.data[p2++] = (byte)(c >> 16); 
	                    bufferImg.data[p2++] = (byte)(c >> 24); 
	                }
		        	pos1 += h;
		        	pos2 += bufferImg.bytesPerLine;
		        }
	        }
	        for (int i = 0; i < idata.length; i++) {
	        	int clr = pal[idata[i] & 0xFFFF];
	        	bufferImg.alphaData[i] = (byte)(clr >> 24);
	        }
        } else {
        	// Õæ²ÊÉ«Í¼Æ¬
        	bufferImg = new ImageData(w, h, 32, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
        	bufferImg.alphaData = new byte[idata.length];
        	for (int i = 0; i < idata.length; i++) {
        		int clr = idata[i];
        		if (refPalette != null) {
        			clr = getRefColor(clr);
        		}
        		bufferImg.data[i * 4] = (byte)(clr >> 24);
        		bufferImg.data[i * 4 + 1] = (byte)(clr >> 16);
        		bufferImg.data[i * 4 + 2] = (byte)(clr >> 8);
        		bufferImg.data[i * 4 + 3] = (byte)clr;
        		bufferImg.alphaData[i] = (byte)(clr >> 24);
	        }
        }
        return bufferImg;
    }

    public void draw(GC g, int x, int y, int trans) {

        int xx = x;
        int yy = y;

        Image img = new Image(g.getDevice(), make(palette, trans));
        g.drawImage(img, xx, yy);
        img.dispose();
    }
    
    public Rectangle getBounds(int trans) {
    	int w, h;
    	if (trans < 4) {
    		w = data.getWidth();
    		h = data.getHeight();
    	} else {
    		w = data.getHeight();
    		h = data.getWidth();
    	}
    	return new Rectangle(0, 0, w, h);
    }
    
    public Image createSWTImage(Device device, int trans) {
    	return new Image(device, make(palette, trans));
    }
    
    public int[][] getPixels(int trans) {
        int w = data.getWidth();
        int h = data.getHeight();
        int[] data = transit(this.data.getData(), trans);
        int[][] ret;
        if (trans < 4) {
        	ret = new int[h][w];
        } else {
        	ret = new int[w][h];
        }
        int index = 0;
        for (int i = 0; i < ret.length; i++) {
        	for (int j = 0; j < ret[0].length; j++) {
        		ret[i][j] = palette.getColor(data[index] & 0xFFFF);
        		if ((ret[i][j] & 0xFF000000) == 0) {
        			ret[i][j] = 0;
        		}
        		index++;
        	}
        }
        return ret;
    }
    
    private int getRefColor(int color) {
    	if (colorSearchMap == null) {
    		colorSearchMap = new HashMap<Integer, Integer>();
    	}
    	if (colorSearchMap.containsKey(color)) {
    		return colorSearchMap.get(color);
    	}
    	double minDist = 100000000.0;
    	int bestColor = 0;
    	boolean found = false;
    	for (int i = 0; i < refPalette.length; i++) {
    		double dist = ColorQuantization.dist(color, refPalette[i]);
    		if (dist < minDist) {
    			bestColor = refPalette[i];
    			minDist = dist;
    			found = true;
    		}
    	}
    	if (found) {
    		return bestColor;
    	} else {
    		return color;
    	}
    }
}
