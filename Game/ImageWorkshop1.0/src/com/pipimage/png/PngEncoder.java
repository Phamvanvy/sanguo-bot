package com.pipimage.png;

import java.io.*;
import java.util.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

/**
 * Encode a BufferedImage to a PNG file. Currently this encoder can only support
 * indexed color model with less than 256 colors. That means, this encoder can
 * only output PNG8.
 * Notice: this encoder don't support transparency.
 */
public class PngEncoder {
    protected ImageData imageData;

    public PngEncoder(Image img) {
        imageData = img.getImageData();
    }
    
    public PngEncoder(ImageData id) {
    	imageData = id;
    }

    public void encode(OutputStream os, boolean bestCompress) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        PngFile png = new PngFile();
        png.width = imageData.width;
        png.height = imageData.height;
        png.bitDepth = (byte)8;                // support 256 colors
        png.colorType = (byte)3;               // Indexed color image

        // Build scanline data and palette
        HashMap paletteMap = new HashMap();
        int colorCount = 0;
        int[] palette = new int[256];
        png.scanlines = new ArrayList<byte[]>(png.height);
        for (int i = 0; i < png.height; i++) {
        	int[] samples = new int[png.width];
        	imageData.getPixels(0, i, png.width, samples, 0);
            byte[] scanline = new byte[samples.length];
            for (int j = 0; j < samples.length; j++) {
            	int clr;
            	if (imageData.palette.isDirect) {
            		clr = reverse(samples[j]);
            	} else {
            		RGB rgb = imageData.palette.colors[samples[j]];
            		clr = (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
            		if (imageData.alphaData != null) {
            			byte alpha = imageData.alphaData[i * png.width + j];
            			clr |= alpha << 24;
            		} else if (samples[j] != imageData.transparentPixel) {
            			clr |= 0xFF000000;
            		}
            	}
                Integer clrInd = (Integer)paletteMap.get(new Integer(clr));
                if (clrInd == null) {
                    if (colorCount >= 256) {
                        throw new IOException("More than 256 colors!");
                    }
                    paletteMap.put(new Integer(clr), new Integer(colorCount));
                    palette[colorCount] = clr;
                    scanline[j] = (byte)colorCount;
                    colorCount++;
                } else {
                    scanline[j] = (byte)(clrInd.intValue());
                }
            }
            png.scanlines.add(scanline);
        }
        png.palette = new int[colorCount];
        png.transparency = new byte[colorCount];
        for (int i = 0; i < colorCount; i++) {
            png.palette[i] = palette[i] & 0xFFFFFF;
            png.transparency[i] = (byte)(palette[i] >> 24);
        }
        PngOptimizer.optimizeTransparency(png);

        // Save png file.
        png.writePng(dos, bestCompress);
        dos.flush();
    }
    
    /**
     * write a png32 format file.
     */
    public void encode32(OutputStream os, boolean bestCompress) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        PngFile png = new PngFile();
        png.width = imageData.width;
        png.height = imageData.height;
        png.bitDepth = (byte)8;                // 8-bits sample
        png.colorType = (byte)6;               // rgba color image

        // Build scanline data
        png.scanlines = new ArrayList<byte[]>(png.height);
        for (int i = 0; i < png.height; i++) {
            int[] samples = new int[png.width];
            imageData.getPixels(0, i, png.width, samples, 0);
            byte[] scanline = new byte[samples.length * 4];
            for (int j = 0; j < samples.length; j++) {
                int clr;
                if (imageData.palette.isDirect) {
                	clr = samples[j];
                	int a, r, g, b;
            		if (imageData.alphaData != null) {
            			a = imageData.alphaData[i * imageData.width + j] & 0xFF;
            		} else if (imageData.maskData != null) {
            			int mlsize = ((imageData.width + 7) / 8 + (imageData.maskPad - 1)) / imageData.maskPad * imageData.maskPad;
            			int offset = mlsize * i + j / 8;
            			int offset2 = 7 - (j % 8);
            			int flag = (imageData.maskData[offset] >> offset2) & 0x01;
            			a = flag == 1 ? 0xFF : 0;
            		} else if (imageData.depth == 24) {
            			a = 0xFF;
            		} else if (imageData.palette.blueShift == 0 || imageData.palette.redShift == 0 || imageData.palette.greenShift == 0){
            			a = (clr >> 24) & 0xFF;
            		} else {
            			a = clr & 0xFF;
            		}
            		r = ((clr & imageData.palette.redMask) >> -(imageData.palette.redShift)) & 0xFF;
            		g = ((clr & imageData.palette.greenMask) >> -(imageData.palette.greenShift)) & 0xFF;
					b = ((clr & imageData.palette.blueMask) >> -(imageData.palette.blueShift)) & 0xFF;
					clr = (a << 24) | (r << 16) | (g << 8) | b;
                } else {
                    RGB rgb = imageData.palette.colors[samples[j]];
                    clr = (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
                    if (imageData.alphaData != null) {
                        byte alpha = imageData.alphaData[i * png.width + j];
                        clr |= alpha << 24;
                    } else if (samples[j] != imageData.transparentPixel) {
                        clr |= 0xFF000000;
                    }
                }
                scanline[j * 4] = (byte)(clr >> 16);
                scanline[j * 4 + 1] = (byte)(clr >> 8);
                scanline[j * 4 + 2] = (byte)clr;
                scanline[j * 4 + 3] = (byte)(clr >> 24);
            }
            png.scanlines.add(scanline);
        }

        // Save png file.
        png.writePng(dos, bestCompress);
        dos.flush();
    }
    
    public static void encode(OutputStream os, int[] palette, byte[][] data) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        PngFile png = new PngFile();
        png.width = data.length == 0 ? 0 : data[0].length;
        png.height = data.length;
        png.bitDepth = (byte)8;                // support 256 colors
        png.colorType = (byte)3;               // Indexed color image

        // Build scanline data and palette
        png.scanlines = new ArrayList(png.height);
        for (int i = 0; i < png.height; i++) {
            png.scanlines.add(data[i]);
        }
        png.palette = new int[palette.length];
        png.transparency = new byte[palette.length];
        for (int i = 0; i < palette.length; i++) {
            png.palette[i] = palette[i] & 0xFFFFFF;
            png.transparency[i] = (byte)(palette[i] >> 24);
        }
        PngOptimizer.optimizeTransparency(png);

        // Save png file.
        png.writePng(dos, true);
        dos.flush();
    }
    
    /**
     * 特殊编码方式，把调色板数据和文件数据写到两个不同的流里。
     * @param os1 图片数据
     * @param os2 调色板数据
     * @param palette 
     * @param data
     * @throws IOException
     */
    private static void encodeSpecial(OutputStream os1, OutputStream os2, int[] palette, byte[][] data) throws IOException {
        DataOutputStream dos1 = new DataOutputStream(os1);
        DataOutputStream dos2 = null;
        if (os2 != null) {
            dos2 = new DataOutputStream(os2);
        }
        
        PngFile png = new PngFile();
        png.width = data.length == 0 ? 0 : data[0].length;
        png.height = data.length;
        png.bitDepth = (byte)8;                // support 256 colors
        png.colorType = (byte)3;               // Indexed color image

        // Build scanline data and palette
        png.scanlines = new ArrayList(png.height);
        for (int i = 0; i < png.height; i++) {
            png.scanlines.add(data[i]);
        }
        png.palette = new int[palette.length];
        png.transparency = new byte[palette.length];
        for (int i = 0; i < palette.length; i++) {
            png.palette[i] = palette[i] & 0xFFFFFF;
            png.transparency[i] = (byte)(palette[i] >> 24);
        }
        // PngOptimizer.optimizeTransparency(png);

        // Save png file.
        png.writePngSpecial(dos1, dos2, true);
        dos1.flush();
        if (dos2 != null) {
            dos2.flush();
        }
    }
    
    /**
     * 对多个具有相同调色板的图片进行特殊编码。生成N+1个文件，N个图片文件+1个调色板文件。
     * @param os 所有输出流，应该有N+1个
     * @param palette 调色板
     * @param data 所有图片数据，N个
     * @throws IOException
     */
    public static void encode(OutputStream[] os, int[] palette, byte[][][] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                encodeSpecial(os[i], os[os.length - 1], palette, data[i]);
            } else {
                encodeSpecial(os[i], null, palette, data[i]);
            }
        }
    }
    
    public static int reverse(int value) {
    	int i1 = (value >> 24) & 0xFF;
    	int i2 = (value >> 16) & 0xFF;
    	int i3 = (value >> 8) & 0xFF;
    	int i4 = value & 0xFF;
    	return (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
    }
}
