package com.pipimage.utils;

import java.util.List;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.mango.jni.GLUtils;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImagePalette;

public class ImageUtil {

	/**
	 * 比较两个图像数据，计算匹配率
	 * @param data1
	 * @param data2
	 * @return
	 */
	public static double compareData(int[][] data1, int[][] data2) {
		if (data1.length != data2.length || data1[0].length != data2[0].length) {
			return 100.0;
		}
	    int totalCount = data1.length * data1[0].length;
	    double errorCount = 0;
	    for (int i = data1.length - 1; i >= 0; i--) {
	        for (int j = data1[0].length - 1; j >= 0; j--) {
	        	int p1 = data1[i][j];
	        	int p2 = data2[i][j];
	        	if (p1 == p2) {
	        		continue;
	        	}
	        	int a = ((p1 >> 24) & 0xFF) - ((p2 >> 24) & 0xFF);
	        	int r = ((p1 >> 16) & 0xFF) - ((p2 >> 16) & 0xFF);
	        	int g = ((p1 >> 8) & 0xFF) - ((p2 >> 8) & 0xFF);
	        	int b = (p1 & 0xFF) - (p2 & 0xFF);
	        	int diff = Math.abs(a) + Math.abs(r) + Math.abs(g) + Math.abs(b);
	        	if (diff > 60) {
	        		errorCount += 100.0;
	        	} else {
	        		errorCount += diff * 100.0 / 60;
	        	}
	        }
	    }
	    return errorCount / totalCount;
	}

	/**
	 * 比较一组图片帧数据，并找出它们两两之间的相似度。
	 * @param frameDatas
	 * @return 如果有n个图片，返回nxn的数组，存储每一个图片和其他图片的相似度。
	 */
	public static double[][] compareFrames(List<int[][]> frameDatas) {
		double[][] ret = new double[frameDatas.size()][frameDatas.size()];
		int[] offset = new int[3];
		for (int i = 0; i < frameDatas.size() - 1; i++) {
			for (int j = i + 1; j < frameDatas.size(); j++) {
				double value = compareFrame(frameDatas.get(i), frameDatas.get(j), offset);
				ret[i][j] = value;
				ret[j][i] = value;
			}
		}
		return ret;
	}

	/**
	 * 比较两个图片，找出相似度。两个图片大小可以不一样。
	 * @param frame1
	 * @param frame2
	 * @param offset 输出参数，用来保存最佳匹配的偏移量（frame2相对于frame1），还有翻转值（0或1，1表示水平翻转）
	 * @return 最佳匹配的相似度，0表示不建议匹配，1表示最佳匹配。
	 */
	public static double compareFrame(int[][] frame1, int[][] frame2, int[] offset) {
		int h1 = frame1.length;
		int w1 = h1 == 0 ? 0 : frame1[0].length;
		int h2 = frame2.length;
		int w2 = h2 == 0 ? 0 : frame2[0].length;
		
		// 空帧不处理
		if (h1 == 0 || w1 == 0 || h2 == 0 || w2 == 0) {
			return 0;
		}
		
		// 宽度或高度差超过30%的，不匹配
		double wratio = (double)w1 / w2;
		double hratio = (double)h1 / h2;
		if (wratio < 0.77 || wratio > 1.3 || hratio < 0.77 || hratio > 1.3) {
			return 0;
		}
		
		// 用宽度和高度差进行穷举比较
		double maxMatch = 0;
		int wdiff = w2 - w1;
		// 如果wdiff < 0，第二章图比较小，那么偏移量应该是从0到-wdiff
		// 如果wdiff > 0，第二章图比较大，那么偏移量应该是从-wdiff到0
		int xoffmin = wdiff < 0 ? 0 : -wdiff;
		int xoffmax = wdiff < 0 ? -wdiff : 0;
		int hdiff = h2 - h1;
		int yoffmin = hdiff < 0 ? 0 : -hdiff;
		int yoffmax = hdiff < 0 ? -hdiff : 0;
		for (int xoff = xoffmin; xoff <= xoffmax; xoff++) {
			for (int yoff = yoffmin; yoff <= yoffmax; yoff++) {
				double value = compareFrame(frame1, frame2, xoff, yoff, 0);
				if (value > maxMatch) {
					maxMatch = value;
					offset[0] = xoff;
					offset[1] = yoff;
					offset[2] = 0;
				}
				value = compareFrame(frame1, frame2, xoff, yoff, 1);
				if (value > maxMatch) {
					maxMatch = value;
					offset[0] = xoff;
					offset[1] = yoff;
					offset[2] = 1;
				}
			}
		}
		return maxMatch;
	}
	
	/**
	 * 逐个像素比较两个图片的的相似度。两个图片大小可能不一样，所以给定的参数还说明了第二个图片相对于第一个图片的偏移量。
	 * @param frame1 图片数据，不能为空
	 * @param frame2 图片数据，不能为空
	 * @param xoff 比较的时候，第二张图片的x方向偏移量
	 * @param yoff 比较的时候，第二张图片的y方向偏移量
	 * @param trans 0表示不翻转，1表示水平翻转
	 * @return 相似程度，0表示完全不相似；1表示完全相等。
	 */
	public static double compareFrame(int[][] frame1, int[][] frame2, int xoff, int yoff, int trans) {
		int h1 = frame1.length;
		int w1 = frame1[0].length;
		int h2 = frame2.length;
		int w2 = frame2[0].length;
		int h = Math.max(h1, h2);
		int w = Math.max(w1, w2);
		int startx = xoff < 0 ? xoff : 0;
		int starty = yoff < 0 ? yoff : 0;
		double sum = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int x1 = x + startx;
				int y1 = y + starty;
				int p1;
				if (x1 >= 0 && x1 < w1 && y1 >= 0 && y1 < h1) {
					p1 = frame1[y1][x1];
				} else {
					p1 = 0;
				}
				int x2 = x1 - xoff;
				int y2 = y1 - yoff;
				int p2;
				if (x2 >= 0 && x2 < w2 && y2 >= 0 && y2 < h2) {
					if (trans == 1) {
						p2 = frame2[y2][w2 - x2 - 1];
					} else {
						p2 = frame2[y2][x2];
					}
				} else {
					p2 = 0;
				}
				sum += comparePixel(p1, p2);
			}
		}
		return sum / (w * h);
	}
	
	/**
	 * 比较两个像素的相似度。通过分别比较32位像素的ARGB字节，计算相似程度。
	 * @param p1
	 * @param p2
	 * @return 0-1的相似度。
	 */
	public static double comparePixel(int p1, int p2) {
		// 特殊处理，透明的像素是一样的
		if ((p1 >> 24) == 0 && (p2 >> 24) == 0) {
			return 1;
		}
		double adiff = Math.abs((((p1 >> 24) & 0xFF) - ((p2 >> 24) & 0xFF)) / 256.0);
		double rdiff = Math.abs((((p1 >> 16) & 0xFF) - ((p2 >> 16) & 0xFF)) / 256.0);
		double gdiff = Math.abs((((p1 >> 8) & 0xFF) - ((p2 >> 8) & 0xFF)) / 256.0);
		double bdiff = Math.abs(((p1 & 0xFF) - (p2 & 0xFF)) / 256.0);
		return (1 - adiff) * (1 - rdiff) * (1 - gdiff) * (1 - bdiff);
	}
	
	
	/**
	 * 创建某一帧的缩略图。
	 * @param frame
	 * @param fitWidth 最适合的宽度
	 * @param fitHeight 最适合的高度
	 * @return
	 */
	public static Image getFrameThumb(PipImage img, Device device, int frame, int fitWidth, int fitHeight) {
		Image frameImg = img.getImageDraw(frame).createSWTImage(device, 0);
		int w = frameImg.getBounds().width;
		int h = frameImg.getBounds().height;
		if (w < fitWidth && h < fitHeight) {
			return frameImg;
		}
		double xratio = fitWidth / (double)w;
		double yratio = fitHeight / (double)h;
		double ratio = Math.min(xratio, yratio);
		Image ret = new Image(device, (int)(w * ratio), (int)(h * ratio));
		GC gc = new GC(ret);
		gc.drawImage(frameImg, 0, 0, w, h, 0, 0, (int)(w * ratio), (int)(h * ratio));
		gc.dispose();
		GLUtils.unloadImage(frameImg);
		return ret;
	}
	
	/**
	 * 根据RGB像素数据创建图片对象。
	 * @param device
	 * @param data
	 * @return
	 */
	public static Image createRGBImage(Device device, int[][] data) {
        int w = data[0].length;
        int h = data.length;
    	ImageData idata = new ImageData(w, h, 32, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
    	idata.alphaData = new byte[w * h];
    	int pos = 0;
    	for (int y = 0; y < h; y++) {
    		for (int x = 0; x < w; x++) {
    			int clr = data[y][x];
    			idata.data[pos * 4] = (byte)(clr >> 24);
    			idata.data[pos * 4 + 1] = (byte)(clr >> 16);
    			idata.data[pos * 4 + 2] = (byte)(clr >> 8);
    			idata.data[pos * 4 + 3] = (byte)clr;
    			idata.alphaData[pos] = (byte)(clr >> 24);
    			pos++;
    		}
    	}
    	return new Image(device, idata);
	}
}
