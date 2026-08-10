package com.pipimage.image;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import org.eclipse.swt.graphics.GC;

import com.pip.util.SWTUtils;
import com.pip.util.Rectangle;
import com.pipimage.data.ImageDescription;
import com.pipimage.data.TileInfo1;
import com.pipimage.data.TileInfo2;
import com.pipimage.png.PngEncoder;
import com.pipimage.png.PngFile;
import com.pipimage.utils.GZIP;
import com.pipimage.utils.Utils;


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
    
    public void setTrueColor(boolean value) {
    	trueColor = value;
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
                    throw new IOException("合并模式中每张图片的宽度和高度不能超过255。");
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

    public void save(DataOutputStream dos, boolean compress) throws IOException{
//    	if (data.size() > 255) {
//    		throw new IOException("一张图片最多容纳256帧。");
//    	}
    	if (!mergeMode) {
    		if (trueColor) {
    			// 真彩色图片
    	    	dos.write(HEAD_TRUE_CLR);
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
            load(fis);
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
			int outIndex = 0;
			for (int i = 0; i < rawData.length; i++) {
				for (int j = 0; j < rawData[i].length; j++) {
					int clrInd = -1;
					int pixel = rawData[i][j];
					for (int k = 0; k < colorCount; k++) {
						if (pixel == newpalette[k]) {
							clrInd = k;
							break;
						}
						if ((pixel & 0xFF000000) == 0 && (newpalette[k] & 0xFF000000) == 0) {
							clrInd = k;
							break;
						}
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
	                    colorCount++;
	                } else {
	                	indexData[outIndex] = (short)clrInd;
	                }
					outIndex++;
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
		DataOutputStream dos = new DataOutputStream(bos);
		if(image.getImagePalettes().get(0).getPalette().length > 256){
		    image.setMergeMode(false);
		}
	    image.save(dos, true);
	    dos.flush();
	    return bos.toByteArray();
	}
}
