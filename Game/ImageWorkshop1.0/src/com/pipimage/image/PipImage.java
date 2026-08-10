package com.pipimage.image;


import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.pip.util.Point;
import com.pip.util.Rectangle;
import com.pip.util.SWTUtils;
import com.pipimage.data.ImageDescription;
import com.pipimage.data.TileInfo1;
import com.pipimage.data.TileInfo2;
import com.pipimage.png.ColorQuantization;
import com.pipimage.png.PngEncoder;
import com.pipimage.png.PngFile;
import com.pipimage.utils.GZIP;
import com.pipimage.utils.Utils;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class PipImage{

    protected static final byte[] HEAD = {
                    'P', 'I', 'P'
    };
    protected static final byte[] HEAD_EX = {
    	'P', 'I', 'E'
    };
    protected static final byte[] HEAD_MG = {
        'P', 'I', 'M'
    };
    protected static final byte[] HEAD_CLR = {
                    'P', 'J', 'P'
    };
    protected static final byte[] HEAD_CLR_EX = {
                    'P', 'J', 'E'
    };
    protected static final byte[] HEAD_TRUE_CLR = {
    	'P', 'T', 'P'
    };
    protected static final byte[] HEAD_TRUE_CLR_JPEG = {
    	'P', 'T', 'J'
    };
    protected static final byte[] HEAD_TRUE_CLR_COMPRESSED = {
    	'P', 'T', 'C'
    };

    protected Vector<PipImagePalette> palette = new Vector<PipImagePalette>();
    protected Vector<PipImageData> data = new Vector<PipImageData>();
    protected int paletteIndex;
    // 是否支持变色，不支持变色的图片在使用fullbuffer2模式时可以节省内存
    protected boolean supportColorOp = false;
    // 是否合并模式，合并模式的图片使用一个大PNG来存储图像数据，可以减少图片对象数量
    // 合并模式的图片不支持变色，也不支持halfbuffer模式
    protected boolean mergeMode = false;
    // 是否支持超过256色的图片，一旦设置了这个标志，则不能使用mergeMode = true
    protected boolean supportMoreColors = false;
    // 是否真彩色，一旦设置了这个标志，则没有调色板，直接保存像素数据
    protected boolean trueColor = false;
    // JPEG合并存储选项，只有在trueColor = true时有效，如果不为空，则图片存储时用JPEG存储。
    protected JPEGMergeOption jpegOption;
    // 压缩纹理格式
    protected CompressTextureOption compTexOption;
    
    // 临时变量，用于关卡生成时测算图片大小，保证每个合并JPEG不至于太大
    public int borderSize = 1;
    public int maxMergeHeight = 1024;
    public static boolean limitSize = true;
    
    // 用于解析纹理图片的编码/解码器
    public static CompressedTextureHandler compressTextureHandler = null;

    public JPEGMergeOption getJPEGOption() {
    	return jpegOption;
    }
    
    public void setJPEGOption(JPEGMergeOption option) {
    	if (option != null) {
        	if (!trueColor) {
        		throw new IllegalArgumentException("必须是真彩色图片！");
        	}
    		this.jpegOption = option;
    		compTexOption = null;
    	} else {
    		this.jpegOption = null;
    	}
    }

    public CompressTextureOption getCompressTextureOption() {
    	return compTexOption;
    }
    
    public void setCompressTextureOption(CompressTextureOption option) {
    	if (option != null) {
        	if (!trueColor) {
        		throw new IllegalArgumentException("必须是真彩色图片！");
        	}
        	this.compTexOption = option;
        	jpegOption = null;
    	} else {
    		this.compTexOption = null;
    	}
    }
    
    public boolean isSupportColorOp() {
    	return supportColorOp;
    }
    
    public void setSupportColorOp(boolean value) {
    	supportColorOp = value;
    }
    
    public boolean isSupportMoreColors() {
        return supportMoreColors;
    }
    
    public void setSupportMoreColors(boolean value) {
        supportMoreColors = value;
    }
    
    public boolean isTrueColor() {
    	return trueColor;
    }
    
    /**
     * 设置是否真彩色模式。当图片从真彩色图片向索引色转换，或者反向，都需要做数据转换处理，而不只是做一个标志。
     * @param value
     */
    public void setTrueColor(boolean value) {
    	if (value != trueColor) {
    		if (value) {
    			// 由索引色转换为真彩色（删除调色板）
    			if (palette.size() > 1) {
    				throw new IllegalArgumentException("有多个调色板的图片不能转换为真彩色。");
    			}
    			for (int i = 0; i < data.size(); i++) {
    				data.get(i).data = data.get(i).make(palette.get(0));
    			}
    			palette.clear();
    			PipImagePalette newPal = new PipImagePalette();
    			newPal.setPalette(new int[0]);
    			palette.add(newPal);
    		} else {
    			// 由真彩色转换为索引色（重建调色板）
    			int nextColorId = 0;
    			Map<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
    			for (int i = 0; i < data.size(); i++) {
    				int[] pixels = data.get(i).data;
    				for (int j = 0; j < pixels.length; j++) {
    					int p = pixels[j];
    					if ((p & 0xFF000000) == 0) {
    						p = 0;
    					}
    					if (!colorMap.containsKey(p)) {
    						colorMap.put(p, nextColorId);
    						nextColorId++;
    					}
    				}
    			}
    			if (this.supportMoreColors) {
    				if (nextColorId > 65536) {
    					throw new IllegalArgumentException("不能超过65536色。");
    				}
    			} else {
    				if (nextColorId > 256) {
    					throw new IllegalArgumentException("不能超过256色。");
    				}
    			}
    			
    			palette.clear();
    			PipImagePalette newPal = new PipImagePalette();
    			int[] colors = new int[colorMap.size()];
    			for (int color : colorMap.keySet()) {
    				colors[colorMap.get(color)] = color;
    			}
    			newPal.setPalette(colors);
    			palette.add(newPal);
    			
    			for (int i = 0; i < data.size(); i++) {
    				int[] pixels = data.get(i).data;
    				for (int j = 0; j < pixels.length; j++) {
    					int p = pixels[j];
    					if ((p & 0xFF000000) == 0) {
    						p = 0;
    					}
    					pixels[j] = colorMap.get(p);
    				}
    			}
    		}
    		trueColor = value;
    	}
    }
    
    public boolean isMergeMode() {
        return mergeMode;
    }
    
    public void setMergeMode(boolean value) {
        mergeMode = value;
    }
    
    public void create(File pngFile) throws Exception{
        PngFile png = new PngFile();
        png.readPng(new DataInputStream(new FileInputStream(pngFile)));

        readPalette(png);

        PipImageData data = new PipImageData();
        data.setFlip((byte)0);
        data.setFrame((byte)0);
        data.setWidth((short)png.width);
        data.setHeight((short)png.height);

        int[] imgData = new int[png.width * png.height];

        for(int i = 0; i < png.scanlines.size(); i++){
            byte[] line = (byte[])png.scanlines.get(i);
            for (int j = 0; j < line.length; j++) {
                imgData[i * png.width + j] = (short)(line[j] & 0xFF);
            }
        }
        data.setData(imgData);
        this.data.add(data);
    }

    public void create(File pngFile, File desc) throws Exception{
        PngFile png = new PngFile();
        png.readPng(new DataInputStream(new FileInputStream(pngFile)));

        readPalette(png);

        ImageDescription id = new ImageDescription();
        id.load(desc);
        switch(id.type){
            case ImageDescription.VERSION_1:
                readVersion1(png, id);
                break;
            case ImageDescription.VERSION_2:
                readVersion2(png, id);
                break;
        }
    }

    public Vector<PipImageData> getImageDatas(){
        return data;
    }

    public Vector<PipImagePalette> getImagePalettes(){
        return palette;
    }

    public void addPalette(PipImagePalette p){
        orderPalette();
        p.setIndex(palette.size());
        palette.add(p);
    }

    public void removePalette(int idx){
        if(idx < 0 || idx >= palette.size())
            return;
        palette.remove(idx);
        orderPalette();
    }

    public void orderPalette(){
        for(int i = 0; i < palette.size(); i++){
            palette.get(i).setIndex(i);
        }
    }

    public void readPalette(PngFile png) throws IOException {
        if (png.palette.length > 65536) {
            throw new IOException("不能超过65536色。");
        }
        int[] p = new int[png.palette.length];
        System.arraycopy(png.palette, 0, p, 0, p.length);
        PipImagePalette palette = new PipImagePalette();
        palette.setPalette(p, png.transparency);
        this.palette.add(palette);
    }

    protected void readVersion1(PngFile png, ImageDescription desc){
    	Object[] tiles = desc.getTileList();
        for(int i = 0; i < tiles.length; i++){
            TileInfo1 info = (TileInfo1)tiles[i];

            PipImageData data = new PipImageData();
            data.setWidth((short)desc.tileWidth);
            data.setHeight((short)desc.tileHeight);
            data.setFlip(info.param);

            data.setFrame((byte)i);

            int x = png.width / desc.tileWidth;
            int y = info.imageID / x;
            x = info.imageID % x;

            byte[] imgData = copyPngData(png, x * desc.tileWidth, y * desc.tileHeight, desc.tileWidth, desc.tileHeight, info.param);
            int[] sdata = new int[imgData.length];
            for (int j = 0; j < imgData.length; j++) {
                sdata[j] = (int)(imgData[j] & 0xFF);
            }
            data.setData(sdata);
            this.data.add(data);
        }
    }

    protected void readVersion2(PngFile png, ImageDescription desc){
    	Object[] tiles = desc.getTileList();
        for(int i = 0; i < tiles.length; i++){
            TileInfo2 info = (TileInfo2)tiles[i];

            PipImageData data = new PipImageData();
            data.setWidth((short)info.width);
            data.setHeight((short)info.height);
            data.setFlip((byte)info.param);
            data.setFrame((byte)i);

            int x = info.x;
            int y = info.y;

            byte[] imgData = copyPngData(png, x, y, info.width, info.height, info.param);
            int[] sdata = new int[imgData.length];
            for (int j = 0; j < imgData.length; j++) {
                sdata[j] = (int)(imgData[j] & 0xFF);
            }
            data.setData(sdata);
            if(info.collision != 0)
                data.setCollision(new byte[]{
                                (byte)info.collX, (byte)info.collY, (byte)info.collWidth, (byte)info.collHeight
                });
            this.data.add(data);
        }
    }
    
    /**
     * 判断图片中第一个调色板是否包含半透明。
     */
    public boolean hasHalfTransparent() {
    	if (palette.size() == 0) {
    		return false;
    	}
    	int[] pals = palette.get(0).getPalette();
		for (int i = 0; i < pals.length; i++) {
			int trns = (pals[i] >> 24) & 0xFF;
			if (trns != 0 && trns != 0xFF) {
				return true;
			}
		}
		return false;
    }
    
    /**
     * 判断图片中是否是完全不透明的。
     */
    public boolean isOpaque() {
    	if (isTrueColor()) {
    		for (int i = 0; i < data.size(); i++) {
    			int[] pixels = data.get(i).data;
    			for (int j = 0; j < pixels.length; j++) {
    				int trns = (pixels[j] >> 24) & 0xFF;
    				if (trns != 0xFF) {
    					return false;
    				}
    			}
    		}
    	} else {
    		for (int i = 0; i < palette.size(); i++) {
    			int[] pals = palette.get(i).getPalette();
    			for (int j = 0; j < pals.length; j++) {
    				int trns = (pals[j] >> 24) & 0xFF;
    				if (trns != 0xFF) {
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }

    public void save(File f) throws IOException{
        save(f, true);
    }

    public void save(File f, boolean compress) throws IOException{
        // 检查数据合法性
//        if (data.size() > 255) {
//            throw new IOException("一张图片最多容纳256帧。");
//        }
        if (mergeMode) {
            if (palette.size() != 1) {
                throw new IOException("合并模式必须有且仅有一个调色板。");
            }
            
            // 合并模式，每个小图的w, h都不能超过255
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getWidth() > 255 || data.get(i).getHeight() > 255) {
//                    throw new IOException("合并模式中每张图片的宽度和高度不能超过255。");
                	mergeMode = false;
                	break;
                }
            }
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        save(dos, compress);
        dos.close();
        if(f.exists())
            f.delete();
        f.createNewFile();
        dos = new DataOutputStream(new FileOutputStream(f));
        dos.write(bos.toByteArray());
        dos.close();
    }

    public void optimizePalette(){
        PipImagePalette defaultPal = palette.get(0);
        for(int i = 1; i < palette.size(); i++){
            PipImagePalette pal = palette.get(i);
            pal.optimize(defaultPal.getPalette().length);
        }
    }

    /*
     * 从合并的大的JPEG图片里载入位图。
     */
    protected void readMergeJPEG(DataInputStream dis) throws IOException {
    	// 读取压缩选项
    	jpegOption.quality = (dis.read() & 0xFF) / 100.0f;
    	jpegOption.alphaBits = dis.read();
    	jpegOption.borderWidth = dis.read();
    	
    	// 读入合并帧信息
    	List<Rectangle> framePos = new ArrayList<Rectangle>();
    	int frameCount = dis.readShort();
    	for (int i = 0; i < frameCount; i++) {
    		int fx = dis.readShort() & 0xFFFF;
    		int fy = dis.readShort() & 0xFFFF;
    		int fw = dis.readShort() & 0xFFFF;
    		int fh = dis.readShort() & 0xFFFF;
    		framePos.add(new Rectangle(fx, fy, fw, fh));
    	}
    	
    	// 读取合并图片大小
    	int mergeW = dis.readShort() & 0xFFFF;
    	int mergeH = dis.readShort() & 0xFFFF;
		
    	// 读取alpha通道数据
    	byte[] alphaData = new byte[dis.readInt()];
    	int alphaW = dis.readShort() & 0xFFFF;
    	int alphaH = dis.readShort() & 0xFFFF;
    	dis.readFully(alphaData);
    	alphaData = GZIP.inflate(alphaData);
    	if (alphaData.length != alphaH * alphaW) {
    		throw new IllegalArgumentException("file corrupt");
    	}
    	byte[][] alpha = new byte[mergeH][mergeW];
    	for (int yy = 0; yy < mergeH; yy++) {
    		int base = yy * alphaW;
    		for (int xx = 0; xx < mergeW; xx++) {
    			if (jpegOption.alphaBits == 8) {
    				alpha[yy][xx] = alphaData[base + xx];
    			} else if (jpegOption.alphaBits == 4) {
    				int ind1 = xx / 2;
    				int ind2 = xx % 2;
    				int value = (alphaData[base + ind1] << (ind2 * 4)) & 0xF0;
					alpha[yy][xx] = (byte)(value | (value >> 4));
    			} else if (jpegOption.alphaBits == 2) {
    				int ind1 = xx / 4;
    				int ind2 = xx % 4;
    				int value = (alphaData[base + ind1] << (ind2 * 2)) & 0xC0;
    				alpha[yy][xx] = (byte)(value | (value >> 2) | (value >> 4) | (value >> 6));
    			} else if (jpegOption.alphaBits == 1) {
    				int ind1 = xx / 8;
    				int ind2 = xx % 8;
    				int value = (alphaData[base + ind1] << ind2) & 0x80;
    				if (value == 0x80) {
    					alpha[yy][xx] = (byte)0xFF;
    				}
    			} else {
    				throw new IllegalArgumentException("invalid alpha bits: " + jpegOption.alphaBits);
    			}
    		}
    	}
    	
    	// 从JPEG文件载入，并恢复ALPHA数据
    	byte[] jpegData = new byte[dis.readInt()];
    	dis.readFully(jpegData);
    	Image loadImage = new Image(null, new ByteArrayInputStream(jpegData));
        int[][] imgData = SWTUtils.getImageData(loadImage, new Rectangle(0, 0, mergeW, mergeH));
        loadImage.dispose();
        for (int i = 0; i < imgData.length; i++) {
        	for (int j = 0; j < imgData[i].length; j++) {
        		imgData[i][j] &= 0xFFFFFF;
        		imgData[i][j] |= alpha[i][j] << 24;
        	}
        }
        
        // 创建帧
        for(int i = 0; i < frameCount; i++) {
            PipImageData data = new PipImageData();
            Rectangle pos = framePos.get(i);
            data.width = (short)pos.width;
            data.height = (short)pos.height;
            data.data = new int[data.width * data.height];
            for (int row = 0; row < data.height; row++) {
            	System.arraycopy(imgData[pos.y + row], pos.x, data.data, row * data.width, data.width);
            }
            this.data.addElement(data);
        }
    }
    
    /*
     * 把真彩色图片合并成一个大的JPEG图片保存。
     */
    protected void writeMergeJPEG(DataOutputStream dos) throws IOException {
    	// 写出压缩选项
		dos.writeByte((int)(jpegOption.quality * 100));
		dos.writeByte(jpegOption.alphaBits);
		dos.writeByte(jpegOption.borderWidth);
		
		// 计算合并方案
		MergeAreaTest atest = new MergeAreaTest(1024, 256);
		List<Point> framePos = new ArrayList<Point>();
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).width + jpegOption.borderWidth * 2 > 1024) {
				throw new IOException("单帧图片宽度太大了，不能超过1024-边框宽度*2。");
			}
			int[] pos = atest.addImage(data.get(i).width + jpegOption.borderWidth * 2, data.get(i).height + jpegOption.borderWidth * 2);
			while (pos == null) {
				atest.addHeight(10);
				pos = atest.addImage(data.get(i).width + jpegOption.borderWidth * 2, data.get(i).height + jpegOption.borderWidth * 2);
			}
			framePos.add(new Point(pos[0] + jpegOption.borderWidth, pos[1] + jpegOption.borderWidth));
		}
		
		// 写出每帧图片在合并图片中的位置
		dos.writeShort(data.size());
		for (int i = 0; i < data.size(); i++) {
			dos.writeShort(framePos.get(i).x);
			dos.writeShort(framePos.get(i).y);
			dos.writeShort(data.get(i).width);
			dos.writeShort(data.get(i).height);
		}
		
		// 写出合并图片大小
		int mergeW = atest.getWidth();
		int mergeH = atest.getHeight();
		dos.writeShort(mergeW);
		dos.writeShort(mergeH);
		
		// 生成合并图片数据
		int[][] imgData = new int[mergeH][mergeW];
		for (int i = 0; i < data.size(); i++) {
			Point pos = framePos.get(i);
			PipImageData pdata = data.get(i);
			int[] fdata = pdata.data;
			for (int row = 0; row < pdata.height; row++) {
				System.arraycopy(fdata, row * pdata.width, imgData[pos.y + row], pos.x, pdata.width);
			}
		}
		
		// 提取并生成alpha通道数据
        byte[][] alpha;
        if (jpegOption.alphaBits == 8) {
	        alpha = new byte[mergeH][mergeW];
	        for (int i = 0; i < mergeH; i++) {
	        	for (int j = 0; j < mergeW; j++) {
	        		alpha[i][j] = (byte)(imgData[i][j] >> 24);
	        	}
	        }
        } else if (jpegOption.alphaBits == 4) {
        	alpha = new byte[mergeH][(mergeW + 1) / 2];
        	for (int i = 0; i < mergeH; i++) {
        		for (int j = 0; j < mergeW; j += 2) {
        			int p1 = (imgData[i][j] >> 28) & 0x0F;
        			int p2 = (j + 1 < mergeW) ? ((imgData[i][j + 1] >> 28) & 0x0F) : 0;
        			alpha[i][j / 2] = (byte)((p1 << 4) | p2);
        		}
        	}
        } else if (jpegOption.alphaBits == 2) {
        	alpha = new byte[mergeH][(mergeW + 3) / 4];
        	for (int i = 0; i < mergeH; i++) {
        		for (int j = 0; j < mergeW; j += 4) {
        			int p1 = (imgData[i][j] >> 30) & 0x03;
        			int p2 = (j + 1 < mergeW) ? ((imgData[i][j + 1] >> 30) & 0x03) : 0;
        			int p3 = (j + 2 < mergeW) ? ((imgData[i][j + 2] >> 30) & 0x03) : 0;
        			int p4 = (j + 3 < mergeW) ? ((imgData[i][j + 3] >> 30) & 0x03) : 0;
        			alpha[i][j / 4] = (byte)((p1 << 6) | (p2 << 4) | (p3 << 2) | p4);
        		}
        	}
        } else if (jpegOption.alphaBits == 1) {
        	alpha = new byte[mergeH][(mergeW + 7) / 8];
        	for (int i = 0; i < mergeH; i++) {
        		for (int j = 0; j < mergeW; j += 8) {
        			int b = 0;
        			for (int k = 0; k < 8; k++) {
        				if (j + k >= mergeW) {
        					break;
        				}
        				int p = (imgData[i][j + k] >> 31) & 0x01;
        				if (p == 1) {
        					b |= 1 << (7 - k);
        				}
        			}
        			alpha[i][j / 8] = (byte)b;
        		}
        	}
        } else {
        	throw new IllegalArgumentException("invalid alpha bits: " + jpegOption.alphaBits);
        }
        
        // 写出alpha通道
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        for (int i = 0; i < alpha.length; i++) {
        	gos.write(alpha[i]);
        }
        gos.flush();
        gos.close();
        byte[] alphaData = bos.toByteArray();
        dos.writeInt(alphaData.length);
        dos.writeShort(alpha[0].length);
        dos.writeShort(alpha.length);
        dos.write(alphaData);
        
        // 给每一帧图加边
        if (jpegOption.borderWidth > 0) {
			for (int i = 0; i < data.size(); i++) {
				Point pos = framePos.get(i);
				PipImageData pdata = data.get(i);
				addBorderForJPEG(imgData, pos.x - jpegOption.borderWidth, pos.y - jpegOption.borderWidth, 
						pdata.width + jpegOption.borderWidth * 2, pdata.height + jpegOption.borderWidth * 2, 
						jpegOption.borderWidth);
			}
        }
        
        // 保存JPEG内容
        BufferedImage awtImg = new BufferedImage(mergeW, mergeH, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < mergeH; i++) {
        	awtImg.setRGB(0, i, mergeW, 1, imgData[i], 0, mergeW);
        }
        bos = new ByteArrayOutputStream();
        JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(awtImg);
        param.setQuality(jpegOption.quality, true);
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos, param);
        encoder.encode(awtImg);
        bos.close();
        byte[] jpegData = bos.toByteArray();
        dos.writeInt(jpegData.length);
        dos.write(jpegData);
        
        // DEBUG
//        Utils.saveFileData(new File("c:/users/lighthu/desktop/demo.jpg"), jpegData);
//        Utils.saveFileData(new File("c:/users/lighthu/desktop/demo.alp"), alphaData);
    }
    
    /*
     * 从合并的压缩纹理中载入位图。
     */
    protected void readMergeCompressedTexture(DataInputStream dis) throws IOException {
    	// 读取压缩算法扩展参数（可选）
    	compTexOption.load(dis);
    	
    	// 读入合并帧信息
    	List<Rectangle> framePos = new ArrayList<Rectangle>();
    	int frameCount = dis.readShort();
    	for (int i = 0; i < frameCount; i++) {
    		int fx = dis.readShort() & 0xFFFF;
    		int fy = dis.readShort() & 0xFFFF;
    		int fw = dis.readShort() & 0xFFFF;
    		int fh = dis.readShort() & 0xFFFF;
    		framePos.add(new Rectangle(fx, fy, fw, fh));
    	}
    	
    	// 读取合并图片大小
    	int mergeW = dis.readShort() & 0xFFFF;
    	int mergeH = dis.readShort() & 0xFFFF;
		
    	// 读取压缩纹理数据
    	byte[] texData = new byte[dis.readInt()];
    	dis.readFully(texData);
    	Image loadImage = compressTextureHandler.decodeTexture(compTexOption.format, texData, mergeW, mergeH);
    	int[][] imgData = SWTUtils.getImageData(loadImage, new Rectangle(0, 0, mergeW, mergeH));
        loadImage.dispose();
        
        // 创建帧
        for(int i = 0; i < frameCount; i++) {
            PipImageData data = new PipImageData();
            Rectangle pos = framePos.get(i);
            data.width = (short)pos.width;
            data.height = (short)pos.height;
            data.data = new int[data.width * data.height];
            for (int row = 0; row < data.height; row++) {
            	System.arraycopy(imgData[pos.y + row], pos.x, data.data, row * data.width, data.width);
            }
            this.data.addElement(data);
        }
    }
    
    /*
     * 生成压缩纹理内容写出。
     */
    protected void writeMergeCompressedTexture(DataOutputStream dos) throws IOException {
    	// 写出压缩选项
    	compTexOption.save(dos);
		
    	// 计算合并方案
    	int borderWidth = compTexOption.borderWidth;
		int[][] framePos = new int[data.size()][];
		int[][] frameSize = new int[data.size()][2];
		for (int i = 0; i < data.size(); i++) {
			frameSize[i][0] = data.get(i).width + borderWidth * 2;
			frameSize[i][1] = data.get(i).height + borderWidth * 2;
		}
		MergeAreaTest atest = null;
		for (int h = 32; h < 8192; h *= 2) {
			atest = new MergeAreaTest(h, h);
			if (atest.addImage(frameSize, framePos)) {
				break;
			}
		}
		for (int i = 0; i < data.size(); i++) {
			framePos[i][0] += borderWidth;
			framePos[i][1] += borderWidth;
		}
		
		// 写出每帧图片在合并图片中的位置
		dos.writeShort(data.size());
		for (int i = 0; i < data.size(); i++) {
			dos.writeShort(framePos[i][0]);
			dos.writeShort(framePos[i][1]);
			dos.writeShort(data.get(i).width);
			dos.writeShort(data.get(i).height);
		}
		
		// 写出合并图片大小
		int mergeW = atest.getWidth();
		int mergeH = atest.getHeight();
		dos.writeShort(mergeW);
		dos.writeShort(mergeH);
		
		// 生成合并图片数据
		int[][] imgData = new int[mergeH][mergeW];
		for (int i = 0; i < data.size(); i++) {
			int[] pos = framePos[i];
			PipImageData pdata = data.get(i);
			int[] fdata = pdata.data;
			for (int row = 0; row < pdata.height; row++) {
				System.arraycopy(fdata, row * pdata.width, imgData[pos[1] + row], pos[0], pdata.width);
			}
		}
		
        // 给每一帧图加1像素的边
		for (int i = 0; i < data.size(); i++) {
			int[] pos = framePos[i];
			PipImageData pdata = data.get(i);
			addBorder(imgData, pos[0] - borderWidth, pos[1] - borderWidth, 
					pdata.width + borderWidth * 2, pdata.height + borderWidth * 2, 
					borderWidth, true);
		}
        
        // 生成压缩纹理
    	Image tempImg = SWTUtils.createImage(imgData);
        byte[] texData = compressTextureHandler.encodeTexture(compTexOption.format, tempImg);
        tempImg.dispose();
        dos.writeInt(texData.length);
        dos.write(texData);
    }
    
    /**
     * 给一个大图中，位于某个区域的一个小图周围描和边缘同色的边。
     * @param imgData 大图数据
     * @param x 小图区域位置（包括边）
     * @param y 小图区域位置（包括边）
     * @param width 小图宽度（包括两边）
     * @param height 小图高度（包括两边）
     * @param border 边界宽度
     */
	public static void addBorderForJPEG(int[][] imgData, int x, int y, int width, int height, int border) {
		// 查找每一行非透明内容的起始坐标和结束坐标，起始坐标-1表示本行空
		int[] xstart = new int[height];
		int[] xend = new int[height];
		for (int yy = y; yy < y + height; yy++) {
			int minx = -1;
			int maxx = -1;
			for (int xx = x; xx < x + width; xx++) {
				int p = imgData[yy][xx];
				if ((p & 0xFF000000) != 0) {
					if (minx == -1) {
						minx = xx;
					}
					maxx = xx;
				}
			}
			xstart[yy - y] = minx;
			xend[yy - y] = maxx;
		}
		
		// 水平方向，所有非空行，2边填充
		for (int yy = y; yy < y + height; yy++) {
			int minx = xstart[yy - y];
			int maxx = xend[yy - y];
			if (minx == -1) {
				continue;
			}
			for (int i = 1; i <= border; i++) {
				imgData[yy][minx - i] = imgData[yy][minx];
				imgData[yy][maxx + i] = imgData[yy][maxx];
			}
		}
		
		// 垂直方向，所有空行，从上边或者下边复制
		for (int yy = y; yy < y + height; yy++) {
			if (xstart[yy - y] != -1) {
				continue;
			}
			
			// 向下边寻找复制行
			boolean found = false;
			for (int i = 1; i <= border; i++) {
				if (yy < y + height - i && xstart[yy + i - y] != -1) {
					int minx = xstart[yy + i - y];
					int maxx = xend[yy + i - y];
					System.arraycopy(imgData[yy + i], minx, imgData[yy], minx, maxx - minx + 1);
					found = true;
					break;
				}
			}
			if (found) {
				continue;
			}
			
			// 向上边寻找复制行
			for (int i = 1; i <= border; i++) {
				if (yy >= y + i && xstart[yy - i - y] != -1) {
					int minx = xstart[yy - i - y];
					int maxx = xend[yy - i - y];
					System.arraycopy(imgData[yy - i], minx, imgData[yy], minx, maxx - minx + 1);
					break;
				}
			}
		}
	}
	
	/**
	 * 为材质图片增加1像素的同色边，防止在缩放拼接的图块时边界出现黑色的缝隙。
	 */
	public void addBorder(int[][] imgData, int x, int y, int width, int height, int border, boolean fixEdge) {
		// 填充每一行第一个像素和最后一个像素
		for (int yy = y + border; yy < y + height - border; yy++) {
			for (int j = 0; j < border; j++) {
				imgData[yy][x + j] = imgData[yy][x + border];
				imgData[yy][x + width - 1 - j] = imgData[yy][x + width - 1 - border];
			}
		}
		
		// 第一行和最后一行分别用第二行和倒数第二行填充
		for (int j = 0; j < border; j++) {
			System.arraycopy(imgData[y + border], x, imgData[y + j], x, width);
			System.arraycopy(imgData[y + height - 1 - border], x, imgData[y + height - 1 - j], x, width);
		}
		
		if (fixEdge) {
			// 在材质数据中搜索不透明部分的边缘，把边缘外的透明像素的颜色改成和不透明的边缘相同，以免在放大图片时产生黑边。
			for (int yy = y; yy < y + height; yy++) {
				for (int xx = x; xx < x + width; xx++) {
					int p = imgData[yy][xx];
					if ((p & 0xFF000000) == 0) {
						// 如果一个像素是透明的，需要把它的颜色设置为最近的非透明点
						if (yy > y && xx > x && (imgData[yy - 1][xx - 1] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy - 1][xx - 1] & 0x00FFFFFF;
							continue;
						}
						if (yy > y && (imgData[yy - 1][xx] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy - 1][xx] & 0x00FFFFFF;
							continue;
						}
						if (yy > y && xx < x + width - 1 && (imgData[yy - 1][xx + 1] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy - 1][xx + 1] & 0x00FFFFFF;
							continue;
						}
						if (xx > x && (imgData[yy][xx - 1] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy][xx - 1] & 0x00FFFFFF;
							continue;
						}
						if (xx < x + width - 1 && (imgData[yy][xx + 1] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy][xx + 1] & 0x00FFFFFF;
							continue;
						}
						if (yy < y + height - 1 && xx > x && (imgData[yy + 1][xx - 1] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy + 1][xx - 1] & 0x00FFFFFF;
							continue;
						}
						if (yy < y + height - 1 && (imgData[yy + 1][xx] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy + 1][xx] & 0x00FFFFFF;
							continue;
						}
						if (yy < y + height - 1 && xx < x + width - 1 && (imgData[yy + 1][xx + 1] & 0xFF000000) != 0) {
							imgData[yy][xx] = imgData[yy + 1][xx + 1] & 0x00FFFFFF;
							continue;
						}
					}
				}
			}
		}
	}
    
    public void save(DataOutputStream dos, boolean compress) throws IOException{
//    	if (data.size() > 255) {
//    		throw new IOException("一张图片最多容纳256帧。");
//    	}
    	if (!mergeMode) {
    		if (trueColor) {
    			// 真彩色图片
    			if (compTexOption != null) {
    				dos.write(HEAD_TRUE_CLR_COMPRESSED);
    				dos.writeUTF(compTexOption.format);
    				dos.writeByte(0);
    				writeMergeCompressedTexture(dos);
    				return;
    			} else if (jpegOption == null) {
    				dos.write(HEAD_TRUE_CLR);
    			} else {
    				dos.write(HEAD_TRUE_CLR_JPEG);
    				dos.writeByte(0);
    				writeMergeJPEG(dos);
    				return;
    			}
    		} else if (!supportMoreColors) {
    	        if (palette.size() > 0 && palette.get(0).getPalette().length > 256) {
                    throw new IOException("不能超过256色。");
                }
                if (supportColorOp) {
                    dos.write(HEAD_EX);
                } else {
                    dos.write(HEAD);
                }
    	    } else {
    	        if (supportColorOp) {
                    dos.write(HEAD_CLR_EX);
                } else {
                    dos.write(HEAD_CLR);
                }
    	    }
    		if (trueColor) {
    			dos.writeByte(0);
    		} else {
	            dos.writeByte(palette.size());
	            for(int i = 0; i < palette.size(); i++){
	                palette.get(i).save(dos);
	            }
    		}
    		if (data.size() >= 255) {
    			dos.writeByte(255);
    			dos.writeShort(data.size());
    		} else {
    			dos.writeByte(data.size());
    		}
            for(int i = 0; i < data.size(); i++){
            	data.get(i).setFrame((byte)i);
                data.get(i).save(dos, compress, supportMoreColors, trueColor);
            }
    	} else {
        	if (data.size() > 255) {
	    		throw new IOException("一张图片最多容纳256帧。");
	    	}
    		dos.write(HEAD_MG);
    	    
    	    // 合并模式只能有一个调色板
    	    if (palette.size() != 1) {
    	        throw new IOException("合并模式必须有且仅有一个调色板。");
    	    }

    	    // 合并模式不能超过256色
    	    if (palette.get(0).getPalette().length > 256) {
    	        throw new ColorsExceedException("合并模式不能超过256色。");
    	    }
    	    
    	    // 合并模式，每个小图的w, h都不能超过255
    	    Rectangle[] rects = new Rectangle[data.size()];
    	    for (int i = 0; i < data.size(); i++) {
    	        if (data.get(i).getWidth() > 255 || data.get(i).getHeight() > 255) {
    	            throw new IOException("合并模式中每张图片的宽度和高度不能超过255。");
    	        }
    	        rects[i] = new Rectangle(0, 0, data.get(i).getWidth(), data.get(i).getHeight());
    	    }
    	    Rectangle[] bounds = SWTUtils.getBestLayout(rects);
    	    
    	    // 写出帧位置信息
    	    byte[] frameInfo = makeFrameInfo(rects);
    	    dos.writeShort(frameInfo.length);
    	    dos.write(frameInfo);
    	    
    	    // 合并图片数据并写出
    	    ByteArrayOutputStream[] oss = makePaletteData(rects, bounds);
    	    dos.write(bounds.length);
            byte[] pdata = oss[bounds.length].toByteArray();
            if (pdata.length > 65535) {
            	throw new IOException("单张图片过大，不能使用合并模式。");
            }
    	    dos.writeShort(pdata.length);
    	    dos.write(pdata);
    	    for (int img = 0; img < bounds.length; img++) {
    	        byte[] idata = oss[img].toByteArray();
    	        if (idata.length > 65535) {
                	throw new IOException("单张图片过大，不能使用合并模式。");
                }
    	        dos.writeShort(idata.length);
    	        dos.write(idata);
    	    }
    	}
    }

	/**
	 * @param rects
	 * @param bounds
	 * @return
	 * @throws IOException
	 */
	private ByteArrayOutputStream[] makePaletteData(Rectangle[] rects, Rectangle[] bounds) throws IOException {
		// 合并图片数据并写出
		ByteArrayOutputStream[] oss = new ByteArrayOutputStream[bounds.length + 1];
		byte[][][] imgData = new byte[bounds.length][][];
		for (int img = 0; img < bounds.length; img++) {
		    oss[img] = new ByteArrayOutputStream();
		    byte[][] idd = new byte[bounds[img].height][bounds[img].width];
		    imgData[img] = idd;
		    for (int i = 0; i < data.size(); i++) {
		        if (((rects[i].x >> 14) & 0x03) != img) {
		            continue;
		        }
		        PipImageData fd = data.get(i);
		        int[] d = fd.getData();
		        byte[] fdd = new byte[d.length];
		        for (int r = 0; r < d.length; r++) {
		            fdd[r] = (byte)d[r];
		        }
		        int fw = fd.getWidth();
		        int fh = fd.getHeight();
		        for (int r = 0; r < fh; r++) {
		            System.arraycopy(fdd, r * fw, idd[r + rects[i].y], rects[i].x & 0x3FFF, fw);
		        }
		    }
		}
		oss[bounds.length] = new ByteArrayOutputStream();
		PngEncoder.encode(oss, palette.get(0).getPalette(), imgData);
		return oss;
	}

	/**
	 * // 写出帧位置信息
	 * @param rects
	 * @return
	 * @throws IOException
	 */
	private byte[] makeFrameInfo(Rectangle[] rects) throws IOException {
		// 写出帧位置信息
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(bos);
		DataOutputStream ddos = new DataOutputStream(gos);
		ddos.writeByte(rects.length);
		for (int i = 0; i < rects.length; i++) {
		    ddos.writeShort(rects[i].x);
		    ddos.writeShort(rects[i].y);
		    ddos.writeByte(rects[i].width);
		    ddos.writeByte(rects[i].height);
		}
		ddos.flush();
		ddos.close();
		gos.close();
		byte[] frameInfo = bos.toByteArray();
		return frameInfo;
	}
    
    /**
     * 所有帧合并为一个或2个图片。
     */
    public PipImage generateMergedImage() throws IOException {
        // 合并模式只能有一个调色板
        if (palette.size() != 1) {
            throw new IOException("合并模式必须有且仅有一个调色板。");
        }
        
        // 合并模式，每个小图的w, h都不能超过255
        Rectangle[] rects = new Rectangle[data.size()];
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getWidth() > 255 || data.get(i).getHeight() > 255) {
                throw new IOException("合并模式中每张图片的宽度和高度不能超过255。");
            }
            rects[i] = new Rectangle(0, 0, data.get(i).getWidth(), data.get(i).getHeight());
        }
        Rectangle[] bounds = SWTUtils.getBestLayout(rects);
        
        // 生成返回的图片
        PipImage ret = new PipImage();
        ret.addPalette(palette.get(0).duplicate());
        int defaultClr = 0;
        int[] clrs = palette.get(0).getPalette();
        while (defaultClr < clrs.length && (clrs[defaultClr] & 0xFF000000) == 0) {
            defaultClr++;
        }
        for (int img = 0; img < bounds.length; img++) {
        	int[][] imgData = new int[bounds[img].height][bounds[img].width];
            for (int i = 0; i < bounds[img].height; i++) {
                Arrays.fill(imgData[i], defaultClr);
            }
            for (int i = 0; i < data.size(); i++) {
                if (((rects[i].x >> 14) & 0x03) == img) {
                    PipImageData fd = data.get(i);
                    int[] fdd = fd.getData();
                    int fw = fd.getWidth();
                    int fh = fd.getHeight();
                    for (int r = 0; r < fh; r++) {
                        System.arraycopy(fdd, r * fw, imgData[r + rects[i].y], rects[i].x & 0x3FFF, fw);
                    }
                }
            }
            PipImageData newData = new PipImageData();
            newData.setFlip((byte)0);
            newData.setFrame((byte)img);
            newData.setWidth((short)bounds[img].width);
            newData.setHeight((short)bounds[img].height);
            int[] ndata = new int[bounds[img].height * bounds[img].width];
            for (int i = 0; i < bounds[img].height; i++) {
                System.arraycopy(imgData[i], 0, ndata, i * bounds[img].width, bounds[img].width);
            }
            newData.setData(ndata);
            ret.data.add(newData);
        }
        return ret;
    }

    public void draw(GC g, int frame, int x, int y, int trans) {
    	int pp = frame / data.size();
    	int ff = frame % data.size();
    	this.setPaletteIndex(pp);
        getImageDraw(ff).draw(g, x, y, trans);
    }

    public PipImageDraw getImageDraw(int frame){
        return createDraw(frame);
    }

    protected PipImageDraw createDraw(int frame){
        PipImageData pipData = (PipImageData)data.elementAt(frame);
        return new PipImageDraw(trueColor ? null : palette.get(paletteIndex), pipData);
    }
    
    public int[][] getFramePixels(int frame){
        PipImageData pipData = (PipImageData)data.elementAt(frame);
        int[] data;
        if (trueColor) {
        	data = pipData.data;
        } else {
        	data = pipData.make(palette.get(paletteIndex));
        }
        int[][] ret = new int[pipData.height][pipData.width];
        for (int i = 0; i < pipData.height; i++) {
        	System.arraycopy(data, i * pipData.width, ret[i], 0, pipData.width);
        }
        return ret;
    }

    public void setPaletteIndex(int pidx){
        if(pidx < palette.size() && pidx >= 0){
            paletteIndex = pidx;
        }
    }
    
    public int getPaletteCount() {
        return palette.size();
    }
    
    public int getPaletteIndex() {
    	return paletteIndex;
    }

    public static byte[] copyPngData(PngFile png, int px, int py, int width, int height, int trans){
        byte[] ret = new byte[width * height];

        for(int y = py; y < py + height; y++){
            byte[] data = (byte[])png.scanlines.get(y);
            System.arraycopy(data, px, ret, (y - py) * width, width);
        }

        ret = Utils.transData(ret, width, trans);
        return ret;
    }

    public void load(String fileName) throws IOException{
        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(fileName);
        	if(fis.available()>0)
            load(new BufferedInputStream(fis));
        } finally {
        	if (fis != null) {
        		fis.close();
        	}
        }
    }

    public void load(InputStream is) throws IOException{
        load(new DataInputStream(is));
    }

    public void load(DataInputStream dis) throws IOException{
        palette.clear();
        data.clear();

        byte[] head = new byte[3];
        dis.read(head);
        if (Arrays.equals(head, HEAD_EX)) {
        	this.supportColorOp = true;
        	this.mergeMode = false;
        	this.supportMoreColors = false;
        	this.trueColor = false;
        } else if (Arrays.equals(head, HEAD_MG)) {
            this.supportColorOp = false;
            this.supportMoreColors = false;
            this.mergeMode = true;
            this.trueColor = false;
        } else if (Arrays.equals(head, HEAD_CLR)) {
            this.supportColorOp = false;
            this.supportMoreColors = true;
            this.mergeMode = false;
            this.trueColor = false;
        } else if (Arrays.equals(head, HEAD_CLR_EX)) {
            this.supportColorOp = true;
            this.supportMoreColors = true;
            this.mergeMode = false;
            this.trueColor = false;
        } else if (Arrays.equals(head, HEAD_TRUE_CLR)) {
        	this.supportColorOp = false;
            this.supportMoreColors = false;
            this.mergeMode = false;
            this.trueColor = true;
        } else if (Arrays.equals(head, HEAD_TRUE_CLR_JPEG)) {
        	this.supportColorOp = false;
            this.supportMoreColors = false;
            this.mergeMode = false;
            this.trueColor = true;
            this.jpegOption = new JPEGMergeOption();
        } else if (Arrays.equals(head, HEAD_TRUE_CLR_COMPRESSED)) {
        	this.supportColorOp = false;
            this.supportMoreColors = false;
            this.mergeMode = false;
            this.trueColor = true;
            this.compTexOption = new CompressTextureOption(dis.readUTF());
        } else {
            this.supportColorOp = false;
            this.supportMoreColors = false;
            this.mergeMode = false;
            this.trueColor = false;
        }
        
        if (!mergeMode) {
        	int c = dis.readByte();
        	if (trueColor) {
        		PipImagePalette p = new PipImagePalette();
        		p.setPalette(new int[0]);
                this.palette.add(p); 
                if (jpegOption != null) {
                	readMergeJPEG(dis);
                	return;
                } else if (compTexOption != null) {
                	readMergeCompressedTexture(dis);
                	return;
                }
        	} else {
	            for(int i = 0; i < c; i++){
	                PipImagePalette palette = new PipImagePalette();
	                palette.read(dis);
	                palette.setIndex(i);
	                this.palette.add(palette);
	            }
        	}
    
            int size = dis.readByte() & 0xFF;
            if (size == 255) {
            	size = dis.readShort() & 0xFFFF;
            }
            for(int i = 0; i < size; i++){
                PipImageData data = new PipImageData();
                data.read(dis, supportMoreColors, trueColor);
                this.data.addElement(data);
            }
        } else {
            // 帧位置信息
            byte[] frameInfo = new byte[dis.readShort()];
            dis.readFully(frameInfo);
            frameInfo = GZIP.inflate(frameInfo);
            DataInputStream ddis = new DataInputStream(new ByteArrayInputStream(frameInfo));
            int size = ddis.readByte() & 0xFF;
            int[] xs = new int[size];
            int[] ys = new int[size];
            int[] ws = new int[size];
            int[] hs = new int[size];
            for (int i = 0; i < size; i++) {
                xs[i] = ddis.readShort() & 0xFFFF;
                ys[i] = ddis.readShort() & 0xFFFF;
                ws[i] = ddis.readByte() & 0xFF;
                hs[i] = ddis.readByte() & 0xFF;
            }
            
            // 帧图片数据
            int imgCount = dis.readByte() & 0xFF;
            byte[] paletteData = new byte[dis.readShort() & 0xFFFF];
            dis.readFully(paletteData);
            PngFile[] pngs = new PngFile[imgCount];
            for (int i = 0; i < imgCount; i++) {
                byte[] part1 = new byte[33];
                byte[] part2 = new byte[(dis.readShort() & 0xFFFF) - 33];
                dis.readFully(part1);
                dis.readFully(part2);
                byte[] allData = new byte[part1.length + paletteData.length + part2.length];
                System.arraycopy(part1, 0, allData, 0, part1.length);
                System.arraycopy(paletteData, 0, allData, part1.length, paletteData.length);
                System.arraycopy(part2, 0, allData, part1.length + paletteData.length, part2.length);
                pngs[i] = new PngFile();
                pngs[i].readPng(new DataInputStream(new ByteArrayInputStream(allData)));
            }
            
            // 读取PNG图片颜色作为调色板
            readPalette(pngs[0]);
            
            // 切割图片数据作为数据帧
            for (int i = 0; i < size; i++) {
                PipImageData data = new PipImageData();
                data.setWidth((short)ws[i]);
                data.setHeight((short)hs[i]);
                data.setFlip((byte)0);
                data.setFrame((byte)i);
                
                int imgid = (xs[i] >> 14) & 0x03;
                int fx = xs[i] & 0x3FFF;
    
                byte[] d = copyPngData(pngs[imgid], fx, ys[i], ws[i], hs[i], (byte)0);
                int[] dd = new int[d.length];
                for (int j = 0; j < d.length; j++) {
                    dd[j] = (int)(d[j] & 0xFF);
                }
                data.setData(dd);
                this.data.addElement(data);
            }
        }
    }

    protected void orderData(){
        PipImageData[] datas = new PipImageData[data.size()];
        data.copyInto(datas);

        for(int i = 0; i < datas.length; i++){
            for(int j = i; j < datas.length; j++){
                if(datas[i].getFrame() > datas[j].getFrame()){
                    PipImageData tmp = datas[i];
                    datas[i] = datas[j];
                    datas[j] = tmp;
                }
            }
        }

        data.removeAllElements();

        for(int i = 0; i < datas.length; i++){
            data.addElement(datas[i]);
        }
    }

    public int getImgCount(){
        return data.size();
    }
    
    public int getFrameCount() {
    	return data.size() * palette.size();
    }

    public PipImageData getImageData(int frame){
    	return data.elementAt(frame % data.size());
    }

    public static PipImagePalette readPalette(String actFile){
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(actFile);
        }catch(FileNotFoundException e){
            return null;
        }

        try{
            if(fis != null){
                Vector<Integer> v = new Vector<Integer>();
                int c = 0;
                for(int i = 0; i < 256; i++){
                    byte[] clr = new byte[3];
                    fis.read(clr);
                    c = PipImagePalette.toRGB(clr[0] & 0xff, clr[1] & 0xff, clr[2] & 0xff);
                    v.add(c);
                }

                int len = 0;
                byte[] a = null;
                int l = fis.available();
                if(fis.available() > 0){
                    byte[] b = new byte[l];
                    fis.read(b);
                    len = b[0] << 8 | b[1];
                    a = new byte[l - 2];
                    System.arraycopy(b, 2, a, 0, a.length);
                    len &= 0xff;
                }else{
                    len = v.size();
                }

                int[] p = new int[len];
                for(int i = 0; i < len; i++){
                    p[i] = v.elementAt(i);
                    if(a == null){
                        p[i] |= 0xff000000;
                    }else{
                        boolean alpha = false;
                        for(int j = 0; j < a.length; j += 2){
                            int aid = a[j + 1] & 0xff;
                            if(i == aid){
                                alpha = true;
                                break;
                            }
                        }
                        if(!alpha){
                            p[i] |= 0xff000000;
                        }
                    }
                }
                PipImagePalette palette = new PipImagePalette();
                palette.setPalette(p);
                return palette;
            }
        }catch(IOException e){

        }

        return null;
    }
    
    public void addFrame(int[][] rawData) throws ColorsExceedException {
    	addFrame(rawData, null, 0);
    }
    
    /**
     * 向图片中添加一帧。
     * @param rawData 像素原始数据（ARGB)
     * @param srcImage 如果有，指定此图像数据的来源，可提高效率
     * @param srcFrame 如果有，指定此图像数据的来源，可提高效率
     * @throws ColorsExceedException
     */
    public void addFrame(int[][] rawData, PipImage srcImage, int srcFrame) throws ColorsExceedException {
    	// 20111117 为了支持opengl模式的客户端，任何一个pipimage都必须能合并放到一个1024x1024的区域里
    	if (limitSize && getCompressTextureOption() == null && !testAddFrame(rawData[0].length, rawData.length)) {
    		throw new ColorsExceedException("图片过大，单个pip文件的所有帧必须可以合并到一个1024x" + maxMergeHeight + "的区域里。");
    	}
    	
    	if (!trueColor) {
			// Use the first palette. If there is no palette yet, a new one is created.
			PipImagePalette refpal;
			if (palette.size() == 0) {
				refpal = new PipImagePalette();
				refpal.setPalette(new int[0]);
				palette.add(refpal);
			} else {
				refpal = palette.get(0);
			}
			
			// Prepare the space for new palette
			int[] newpalette = new int[65536];
			int colorCount = refpal.getPalette().length;
			System.arraycopy(refpal.getPalette(), 0, newpalette, 0, colorCount);
			
			// Convert the image data to indexed data and update the palette
			int[] indexData = new int[rawData.length * rawData[0].length];
			if (srcImage == null) {
				Map<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
				int transColorIndex = -1;
				for (int i = 0; i < colorCount; i++) {
					colorMap.put(newpalette[i], i);
					if ((newpalette[i] & 0xFF000000) == 0) {
						transColorIndex = i;
					}
				}
				int outIndex = 0;
				for (int i = 0; i < rawData.length; i++) {
					for (int j = 0; j < rawData[i].length; j++) {
						int clrInd = -1;
						int pixel = rawData[i][j];
						Integer ii = colorMap.get(pixel);
						if (ii != null) {
							clrInd = ii.intValue();
						} else if ((pixel & 0xFF000000) == 0) {
							clrInd = transColorIndex;
						}
						if (clrInd == -1) {
						    if (this.supportMoreColors) {
		                        if (colorCount >= 65536) {
		                        	throw new ColorsExceedException("颜色数超过65536。");
		                        }
						    } else {
						        if (colorCount >= 256) {
		                            throw new ColorsExceedException("颜色数超过256。");
		                        }
						    }
		                    newpalette[colorCount] = pixel;
		                    indexData[outIndex] = (short)colorCount;
		                    colorMap.put(pixel, colorCount);
		                    colorCount++;
		                } else {
		                	indexData[outIndex] = (short)clrInd;
		                }
						outIndex++;
					}
				}
			} else {
				int[] srcPal = srcImage.getImagePalettes().get(0).getPalette();
				int[] colorMap = new int[srcPal.length];
				int[] srcData = srcImage.getImageData(srcFrame).data;
				
				// 新调色板和旧调色板的颜色进行对应
				Arrays.fill(colorMap, -1);
				Map<Integer, Integer> colorMap2 = new HashMap<Integer, Integer>();
				int transColorIndex = -1;
				for (int i = 0; i < colorCount; i++) {
					colorMap2.put(newpalette[i], i);
					if ((newpalette[i] & 0xFF000000) == 0) {
						transColorIndex = i;
					}
				}
				for (int i = 0; i < srcPal.length; i++) {
					Integer ii = colorMap2.get(srcPal[i]);
					if (ii != null) {
						colorMap[i] = ii.intValue();
					} else if ((srcPal[i] & 0xFF000000) == 0) {
						colorMap[i] = transColorIndex;
					}
				}
				
				for (int i = 0; i < srcData.length; i++) {
					int pixel = srcData[i];
					if (colorMap[pixel] == -1) {
						if (this.supportMoreColors) {
	                        if (colorCount >= 65536) {
	                        	throw new ColorsExceedException("颜色数超过65536。");
	                        }
					    } else {
					        if (colorCount >= 256) {
	                            throw new ColorsExceedException("颜色数超过256。");
	                        }
					    }
						newpalette[colorCount] = srcPal[pixel];
						colorMap[pixel] = colorCount;
	                    indexData[i] = (short)colorCount;
	                    colorCount++;
					} else {
						indexData[i] = (short)colorMap[pixel];
					}
				}
			}
			
			// If new color is added, all palettes need to be update
			if (colorCount > refpal.getPalette().length) {
				for (int i = 0; i < palette.size(); i++) {
					PipImagePalette pp = palette.get(i);
					int[] ppd = pp.getPalette();
					int[] newppd = new int[colorCount];
					System.arraycopy(newpalette, 0, newppd, 0, colorCount);
					System.arraycopy(ppd, 0, newppd, 0, ppd.length);
					pp.setPalette(newppd);
				}
			}
			
			// Add the new frame into frame list
			PipImageData newFrame = new PipImageData();
			newFrame.setWidth((short)rawData[0].length);
			newFrame.setHeight((short)rawData.length);
			newFrame.setData(indexData);
			data.add(newFrame);
    	} else {
    		// Add the new frame into frame list
			PipImageData newFrame = new PipImageData();
			newFrame.setWidth((short)rawData[0].length);
			newFrame.setHeight((short)rawData.length);
			int[] data1 = new int[newFrame.getWidth() * newFrame.getHeight()];
			for (int i = 0; i < newFrame.getHeight(); i++) {
				System.arraycopy(rawData[i], 0, data1, newFrame.getWidth() * i, newFrame.getWidth());
			}
			newFrame.setData(data1);
			data.add(newFrame);
			if (palette.size() == 0) {
				PipImagePalette p = new PipImagePalette();
        		p.setPalette(new int[0]);
                palette.add(p);
			}
    	}
	}
    
    public int getEstimateMemory() {
    	int ret = 0;
    	for (int i = 0; i < data.size(); i++) {
    		ret += data.get(i).getWidth() * data.get(i).getHeight();
    	}
    	return ret;
    }
    
    public int[] getNonUsedColors() {
        if (this.palette.size() == 0) {
            return new int[0];
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        boolean[] useFlag = new boolean[palette.get(0).getPalette().length];
        for (int i = 0; i < data.size(); i++) {
        	int[] frameData = data.get(i).getData();
            for (int j = frameData.length - 1; j >= 0; j--) {
                useFlag[frameData[j] & 0xFFFF] = true;
            }
        }
        for (int i = 0; i < useFlag.length; i++) {
            if (!useFlag[i]) {
                list.add(i);
            }
        }
        int[] ret = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }
    
    public void deleteColors(int[] indices) {
        if (this.palette.size() == 0) {
            return;
        }
        int totalCount = palette.get(0).getPalette().length;
        HashMap<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < totalCount; i++) {
            int newIndex = i;
            for (int j = 0; j < indices.length; j++) {
                if (i == indices[j]) {
                    newIndex = -1;
                    break;
                }
                if (i > indices[j]) {
                    newIndex--;
                }
            }
            colorMap.put(i, newIndex);
        }
        
        for (int i = 0; i < palette.size(); i++) {
            int[] colors = palette.get(i).getPalette();
            for (int j = 0; j < colors.length; j++) {
                int newIndex = colorMap.get(j);
                if (newIndex != -1) {
                    colors[newIndex] = colors[j];
                }
            }
            palette.get(i).optimize(colors.length - indices.length);
        }
        
        for (int i = 0; i < data.size(); i++) {
        	int[] frameData = data.get(i).getData();
            for (int j = frameData.length - 1; j >= 0; j--) {
                int newIndex = colorMap.get(frameData[j] & 0xFFFF);
                frameData[j] = newIndex;
            }
        }
    }
    
    public static int hflip(int trans) {
        switch (trans) {
        case 0:
            return 2;
        case 1:
            return 3;
        case 2:
            return 0;
        case 3:
            return 1;
        case 4:
            return 5;
        case 5:
            return 4;
        case 6:
            return 7;
        case 7:
            return 6;
        }
        return trans;
    }
    
    public static int vflip(int trans) {
        switch (trans) {
        case 0:
            return 1;
        case 1:
            return 0;
        case 2:
            return 3;
        case 3:
            return 2;
        case 4:
            return 6;
        case 5:
            return 7;
        case 6:
            return 4;
        case 7:
            return 5;
        }
        return trans;
    }
    
    public static int rotate90(int trans) {
        switch (trans) {
        case 0:
            return 5;
        case 1:
            return 4;
        case 2:
            return 7;
        case 3:
            return 6;
        case 4:
            return 2;
        case 5:
            return 3;
        case 6:
            return 0;
        case 7:
            return 1;
        }
        return trans;
    }
 
    /**
     * 查找指定图片数据中，没有在本图片的调色板中存在的颜色数。
     * @param imgData
     * @return
     */
    public int findUnmatchColors(int[][] imgData) {
        // Use the first palette. If there is no palette yet, a new one is created.
        HashSet<Integer> existColors = new HashSet<Integer>();
        if (palette.size() > 0) {
            int[] cs = palette.get(0).getPalette();
            for (int c : cs) {
                existColors.add(c);
            }
        }
        
        // scan image data
        HashSet<Integer> newColors = new HashSet<Integer>();
        int rows = imgData.length;
        for (int i = 0; i < rows; i++) {
            int cols = imgData[i].length;
            for (int j = 0; j < cols; j++) {
                int c = imgData[i][j];
                if (!existColors.contains(c)) {
                    newColors.add(c);
                }
            }
        }
        return newColors.size();
    }
    
    /**
     * 把图片中的所有帧放大一倍。
     */
    public void enlarge() {
        for (PipImageData frame : data) {
            frame.enlarge();
        }
    }
    
    /**
     * 把图片中的所有帧缩小一倍。
     */
    public void smaller() {
        for (PipImageData frame : data) {
            frame.smaller();
        }
    }

    public static void main(String[] args) {
        File[] files = new File("C:\\workspace\\Sanguo-Editor1.0\\data\\Animations").listFiles();
        for (File f : files) {
            String name = f.getName();
            if (Character.isDigit(name.charAt(0)) && name.endsWith(".pip")) {
                System.out.println("开始转换：" + name);
                
                // 载入图片
                PipImage img = new PipImage();
                try {
                    img.load(f.getAbsolutePath());
                    if (img.getPaletteCount() > 1) {
                        System.out.println("不止一个调色板：放弃");
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                
                // 预览浪费率
                try {
                    PipImage mergeImg = img.generateMergedImage();
                    int originalSize = 0;
                    for (PipImageData frame : img.data) {
                        originalSize += frame.getWidth() * frame.getHeight();
                    }
                    int newSize = 0;
                    for (PipImageData frame : mergeImg.data) {
                        newSize += frame.getWidth() * frame.getHeight();
                    }
                    double wasteRate = (newSize - originalSize) / (double)originalSize;
                    if (wasteRate >= 0.10) {
                        System.out.println("浪费率太高：放弃");
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                
                // 尝试合并
                img.setMergeMode(true);
                try {
                    img.save(f);
                } catch (Exception e) {
                    e.printStackTrace();
                    img.setMergeMode(false);
                    img.setSupportColorOp(false);
                    try {
                        img.save(f);
                        System.out.println("保存为普通模式");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public static void initPalette(PipImage pipImage, File pngFile) {
        if (pipImage.getImagePalettes().size() == 0) {
            try {
                PngFile png = new PngFile();
                FileInputStream fis = new FileInputStream(pngFile);
                png.readPng(new DataInputStream(fis));
                fis.close();
                pipImage.readPalette(png);
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * 和另外一个PIP图片比较，并返回相似度。只有帧数和每帧大小完全相同的图片才能计算相似度，否则相似度算为0.
     * @param img
     * @return
     */
    public double compare(PipImage img) {
    	if (palette.size() != 1 || img.palette.size() != 1) {
    		return 0;
    	}
    	if (data.size() != img.data.size()) {
    		return 0;
    	}
    	int matchCount = 0, unmatchCount = 0;
    	for (int i = 0; i < data.size(); i++) {
    		PipImageData data1 = data.get(i);
    		PipImageData data2 = img.data.get(i);
    		if (data1.getWidth() != data2.getWidth() || data1.getHeight() != data2.getHeight()) {
    			return 0;
    		}
    		int[] idata1 = data1.make(isTrueColor() ? null : palette.get(0));
    		int[] idata2 = data2.make(img.isTrueColor() ? null : img.palette.get(0));
    		for (int j = 0; j < idata1.length; j++) {
    			if (idata1[j] == idata2[j]) {
    				matchCount++;
    			} else {
    				unmatchCount++;
    			}
    		}
    	}
    	return matchCount / (double)(matchCount + unmatchCount);
    }
    
    /**
     * 保存一个PIP图片为JPEG压缩模式。
     * @param image
     * @return
     * @throws Exception
     */
    public static byte[] makeJPEGMergeFile(PipImage image, JPEGMergeOption jpegOption) throws Exception {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	image.setSupportColorOp(false);
    	image.setSupportMoreColors(false);
        image.setMergeMode(false);
        image.setTrueColor(true);
        image.setJPEGOption(jpegOption);
		DataOutputStream dos = new DataOutputStream(bos);
	    image.save(dos, true);
	    dos.flush();
	    return bos.toByteArray();
    }

	/**
	 * 保存PIP图片。
	 * @param fullCompress 如果为true，则图片帧数据不压缩，而是做整体压缩；如果为false，图片每帧压缩。
	 */
	public static byte[] makeImageFile(PipImage image, boolean fullCompress) throws Exception {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    if (fullCompress) {
	        if (image.getPaletteCount() == 1) {
	            image.setMergeMode(true);
	        }
	    }
		if (image.getImagePalettes().get(0).getPalette().length > 256){
		    image.setMergeMode(false);
		} else {
			image.setSupportMoreColors(false);
		}
		DataOutputStream dos = new DataOutputStream(bos);
	    image.save(dos, true);
	    dos.flush();
	    return bos.toByteArray();
	}
	
	/**
	 * 自动缩减颜色。
	 */
	public void optimizeColor(int colorCount) {
		// 第一步，把所有图片拼成一个数组
        int totalSize = 0;
        for (PipImageData id : getImageDatas()) {
            totalSize += id.getWidth() * id.getHeight();
        }
        int pos = 0;
        int[] mergeData = new int[totalSize];
        PipImagePalette pal = getImagePalettes().get(0);
        for (PipImageData id : getImageDatas()) {
            int[] fdata = id.make(isTrueColor() ? null : pal);
            System.arraycopy(fdata, 0, mergeData, pos, fdata.length);
            pos += fdata.length;
        }
        
        // 第二步，用优化算法构建新的颜色表
        ColorQuantization cq = new ColorQuantization(mergeData, colorCount);
        cq.process();
        
        if (isTrueColor()) {
        	// 第三步：真彩色图片，直接替换所有像素点
        	for (PipImageData id : getImageDatas()) {
        		int[] fdata = id.data;
        		for (int i = 0; i < fdata.length; i++) {
        			fdata[i] = cq.convert(fdata[i]);
        		}
        	}
        } else {
	        // 第三步，转换原调色板中的所有颜色，形成新调色板
	        int[] paldata = pal.getPalette();
	        List<Integer> newpaldata = new ArrayList<Integer>();
	        Set<Integer> clrset = new HashSet<Integer>();
	        Map<Integer, Integer> clrmap = new HashMap<Integer, Integer>();
	        for (int i = 0; i < paldata.length; i++) {
	            int nc = cq.convert(paldata[i]);
	            if (!clrset.contains(nc)) {
	                newpaldata.add(nc);
	                clrset.add(nc);
	                clrmap.put(i, newpaldata.size() - 1);
	            } else {
	                int index = newpaldata.indexOf(nc);
	                clrmap.put(i, index);
	            }
	        }
	        
	        // 第四步，更新所有图块中的颜色索引值
	        for (PipImageData id : getImageDatas()) {
	            int[] rdata = id.getData();
	            for (int i = 0; i < rdata.length; i++) {
	                int v = rdata[i] & 0xFFFF;
	                v = clrmap.get(v);
	                rdata[i] = (short)v;
	            }
	        }
	        
	        // 第五步，替换颜色表
	        int[] newpal = new int[newpaldata.size()];
	        for (int i = 0; i < newpal.length; i++) {
	            newpal[i] = newpaldata.get(i);
	        }
	        pal.setPalette(newpal);
        }
	}
	
	private int findMatchColor(int color, int[] pal) {
    	double minDist = 100000000.0;
    	int bestColor = 0;
    	boolean found = false;
    	for (int i = 0; i < pal.length; i++) {
    		double dist = ColorQuantization.dist(color, pal[i]);
    		if (dist < minDist) {
    			bestColor = pal[i];
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
	
	/**
	 * 按指定调色板缩减颜色。
	 */
	public void optimizeColor(int[] pal) {
		Map<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
        if (isTrueColor()) {
        	// 真彩色图片，处理每个像素
        	for (PipImageData id : getImageDatas()) {
        		int[] fdata = id.data;
        		for (int i = 0; i < fdata.length; i++) {
        			if (colorMap.containsKey(fdata[i])) {
        				fdata[i] = colorMap.get(fdata[i]);
        			} else {
        				int c = findMatchColor(fdata[i], pal);
        				colorMap.put(fdata[i], c);
        				fdata[i] = c;
        			}
        		}
        	}
        } else {
        	// 转换原调色板中的所有颜色，形成新调色板
	        int[] paldata = palette.get(0).getPalette();
	        List<Integer> newpaldata = new ArrayList<Integer>();
	        Set<Integer> clrset = new HashSet<Integer>();
	        Map<Integer, Integer> clrmap = new HashMap<Integer, Integer>();
	        for (int i = 0; i < paldata.length; i++) {
	            int nc = findMatchColor(paldata[i], pal);
	            if (!clrset.contains(nc)) {
	                newpaldata.add(nc);
	                clrset.add(nc);
	                clrmap.put(i, newpaldata.size() - 1);
	            } else {
	                int index = newpaldata.indexOf(nc);
	                clrmap.put(i, index);
	            }
	        }
	        
	        // 更新所有图块中的颜色索引值
	        for (PipImageData id : getImageDatas()) {
	            int[] rdata = id.getData();
	            for (int i = 0; i < rdata.length; i++) {
	                int v = rdata[i] & 0xFFFF;
	                v = clrmap.get(v);
	                rdata[i] = (short)v;
	            }
	        }
	        
	        // 替换颜色表
	        int[] newpal = new int[newpaldata.size()];
	        for (int i = 0; i < newpal.length; i++) {
	            newpal[i] = newpaldata.get(i);
	        }
	        palette.get(0).setPalette(newpal);
        }
	}
	
	/**
	 * 测试在现在的帧列表中，再加上指定大小的一帧后，所有的图片是否还可以成功地加到一个1024x1024的区域里去。
	 */
	public boolean testAddFrame(int newWidth, int newHeight) {
		int[][] sizes;
		if (newWidth != -1 && newHeight != -1) {
			sizes = new int[data.size() + 1][2];
			for (int i = 0; i < data.size(); i++) {
				sizes[i][0] = data.get(i).width + borderSize * 2;
				sizes[i][1] = data.get(i).height + borderSize * 2;
			}
			sizes[sizes.length - 1][0] = newWidth + borderSize * 2;
			sizes[sizes.length - 1][1] = newHeight + borderSize * 2;
		} else {
			sizes = new int[data.size()][2];
			for (int i = 0; i < data.size(); i++) {
				sizes[i][0] = data.get(i).width + borderSize * 2;
				sizes[i][1] = data.get(i).height + borderSize * 2;
			}
		}
		
		MergeAreaTest test = new MergeAreaTest(1024, maxMergeHeight);
		return test.addImage(sizes, null);
	}
	
	/**
	 * 把指定一个图块拆分成多块。
	 * @param index
	 * @param rects
	 * @param cutFrames 拆分的帧图片，必须是truecolor的
	 */
	public void splitFrame(int index, int[][] rects, PipImage cutFrames) {
		if (!cutFrames.isTrueColor()) {
			throw new IllegalArgumentException();
		}
		PipImageData oldData = data.get(index);
		data.remove(index);
		Map<Integer, Integer> paletteMap = new HashMap<Integer, Integer>();
		int transparentIndex = -1;
		boolean noTransColor = false;
		if (!trueColor) {
			int[] pal = palette.get(0).getPalette();
			for (int i = 0; i < pal.length; i++) {
				if ((pal[i] & 0xFF000000) == 0) {
					transparentIndex = i;
				} else {
					paletteMap.put(pal[i], i);
				}
			}
			if (transparentIndex == -1) {
				if (this.supportMoreColors) {
					if (pal.length >= 65536) {
						throw new IllegalArgumentException("调色板已满，无法添加透明色。");
					}
				} else {
					if (pal.length >= 256) {
						throw new IllegalArgumentException("调色板已满，无法添加透明色。");
					}
				}
				transparentIndex = pal.length;
				noTransColor = true;
			}
		}
		boolean hasTransPixels = false;
		for (int i = 0; i < rects.length; i++) {
			PipImageData newData = new PipImageData();
			PipImageData refData = cutFrames.getImageData(i);
			newData.flip = oldData.flip;
			newData.frame = oldData.frame;
	    	newData.width = refData.width;
	    	newData.height = refData.height;
	    	newData.data = new int[refData.data.length];
	    	for (int j = 0; j < refData.data.length; j++) {
	    		int clr = refData.data[j];
	    		if (trueColor) {
	    			newData.data[j] = clr;
	    		} else {
	    			if ((clr & 0xFF000000) == 0) {
	    				hasTransPixels = true;
	    				newData.data[j] = transparentIndex;
	    			} else {
	    				newData.data[j] = paletteMap.get(clr);
	    			}
	    		}
	    	}
	    	data.add(index + i, newData);
		}
		if (hasTransPixels && noTransColor) {
			// 如果原来调色板中没有透明色，而新的图片里增加了透明色，则在调色板中增加一个透明的颜色
			for (PipImagePalette pal : palette) {
				int[] pdata = pal.getPalette();
				int[] newpdata = new int[pdata.length + 1];
				System.arraycopy(pdata, 0, newpdata, 0, pdata.length);
				newpdata[pdata.length] = 0;
				pal.setPalette(newpdata);
			}
		}
	}
}
