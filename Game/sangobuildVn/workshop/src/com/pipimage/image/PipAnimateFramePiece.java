package com.pipimage.image;

import java.io.*;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class PipAnimateFramePiece {
	protected PipAnimateFrame parent;
	protected int imageID;
	protected int frame;
	protected int transition;
	protected int dx;
	protected int dy;
	protected boolean visible = true;
	
	public PipAnimateFramePiece(PipAnimateFrame parent) {
		this.parent = parent;
	}
	
	public PipAnimateFramePiece(PipAnimateFrame parent, int iid, int ff) {
		this.parent = parent;
		imageID = iid;
		frame = ff;
	}
	
	public int getImageID() {
		return imageID;
	}
	
	public void setImageID(int imageID) {
		this.imageID = imageID;
	}
	
	public int getFrame() {
		return frame;
	}
	
	public void setFrame(int frame) {
		this.frame = frame;
	}
	
	public int getTransition() {
		return transition;
	}
	
	public void setTransition(int transition) {
		this.transition = transition;
	}
	
	public int getDx() {
		return dx;
	}
	
	public void setDx(int dx) {
		this.dx = dx;
	}
	
	public int getDy() {
		return dy;
	}
	
	public void setDy(int dy) {
		this.dy = dy;
	}
	
	public void load(DataInputStream dis) throws IOException {
		if (parent.parent.getVersion() == 0) {
            // 版本0：一帧压缩在一个int内，位布局为3/8/3/9/9（图片ID、帧序号、翻转、x、y）
    	    int i = dis.readInt();
        	imageID = (i >> 29) & 0x07;
    		frame = (i >> 21) & 0xFF;
    		transition = (i >> 18) & 0x07;
    		dx = (i >> 9) & 0x1FF;
    		if (dx > 255) {
    			dx -= 512;
    		}
    		dy = i & 0x1FF;
    		if (dy > 255) {
    			dy -= 512;
    		}
        } else if (parent.parent.getVersion() == 1) {
            // 版本1：一帧压缩在一个int内，位布局为5/8/3/8/8（图片ID、帧序号、翻转、x、y）
            int i = dis.readInt();
            imageID = (i >> 27) & 0x1F;
            frame = (i >> 19) & 0xFF;
            transition = (i >> 16) & 0x07;
            dx = (i >> 8) & 0xFF;
            if (dx > 127) {
                dx -= 256;
            }
            dy = i & 0xFF;
            if (dy > 127) {
                dy -= 256;
            }
        } else if (parent.parent.getVersion() == 2) {
            // 版本2：一帧用48位存储，位布局为5/8/3/16/16（图片ID、帧序号、翻转、x、y）
            short s = dis.readShort();
            imageID = (s >> 11) & 0x1F;
            frame = (s >> 3) & 0xFF;
            transition = s & 0x07;
            dx = dis.readShort();
            dy = dis.readShort();
        } else {
        	// 版本3：一帧用48位存储，位布局为5/12/3/14/14（图片ID、帧序号、翻转、x、y）
        	int i = dis.readInt();
        	imageID = (i >> 27) & 0x1F;
        	frame = (i >> 15) & 0xFFF;
        	transition = (i >> 12) & 0x07;
        	short s = dis.readShort();
        	dx = ((i & 0xFFF) << 2) | ((s >> 14) & 0x03);
        	if (dx > 8191) {
        		dx -= 16384;
        	}
        	dy = s & 0x3FFF;
        	if (dy > 8191) {
        		dy -= 16384;
        	}
        }
	}
	
	public void save(DataOutputStream dos) throws IOException {
	    if (parent.parent.getVersion() == 0) {
	        // 版本0：一帧压缩在一个int内，位布局为3/8/3/9/9（图片ID、帧序号、翻转、x、y）
    		if (imageID > 7) {
    			throw new IOException("引用图片不能超过8个。");
    		}
    		if (frame > 255) {
    			throw new IOException("每张图片中包含图块数不能超过255个。");
    		}
    		if (Math.abs(dx) > 255 || Math.abs(dy) > 255) {
    			throw new IOException("帧内图块偏移量绝对值不能超过255。");
    		}
    		int i = 0;
    		i |= (imageID & 0x07) << 29;
    		i |= (frame & 0xFF) << 21;
    		i |= (transition & 0x07) << 18;
    		i |= (dx & 0x1FF) << 9;
    		i |= dy & 0x1FF;
    		dos.writeInt(i);
	    } else if (parent.parent.getVersion() == 1) {
	        // 版本1：一帧压缩在一个int内，位布局为5/8/3/8/8（图片ID、帧序号、翻转、x、y）
            if (imageID > 31) {
                throw new IOException("引用图片不能超过31个。");
            }
            if (frame > 255) {
                throw new IOException("每张图片中包含图块数不能超过255个。");
            }
            if (Math.abs(dx) > 127 || Math.abs(dy) > 127) {
                throw new IOException("帧内图块偏移量绝对值不能超过127。");
            }
            int i = 0;
            i |= (imageID & 0x1F) << 27;
            i |= (frame & 0xFF) << 19;
            i |= (transition & 0x07) << 16;
            i |= (dx & 0xFF) << 8;
            i |= dy & 0xFF;
            dos.writeInt(i);
	    } else if (parent.parent.getVersion() == 2) {
	        // 版本2：一帧用48位存储，位布局为5/8/3/16/16（图片ID、帧序号、翻转、x、y）
	        if (imageID > 31) {
                throw new IOException("引用图片不能超过31个。");
            }
            if (frame > 255) {
                throw new IOException("每张图片中包含图块数不能超过255个。");
            }
            if (Math.abs(dx) > 32767 || Math.abs(dy) > 32767) {
                throw new IOException("帧内图块偏移量绝对值不能超过32767。");
            }
            short s = 0;
            s |= (imageID & 0x1F) << 11;
            s |= (frame & 0xFF) << 3;
            s |= (transition & 0x07);
            dos.writeShort(s);
            dos.writeShort(dx);
            dos.writeShort(dy);
	    } else {
	    	// 版本3：一帧用48位存储，位布局为5/12/3/14/14（图片ID、帧序号、翻转、x、y）
	        if (imageID > 31) {
                throw new IOException("引用图片不能超过31个。");
            }
            if (frame > 4096) {
                throw new IOException("每张图片中包含图块数不能超过4096个。");
            }
            if (Math.abs(dx) > 8191 || Math.abs(dy) > 8191) {
                throw new IOException("帧内图块偏移量绝对值不能超过8192。");
            }
            int i = 0;
            
            i |= (imageID & 0x1F) << 27;
            i |= (frame & 0xFFF) << 15;
            i |= (transition & 0x07) << 12;
            i |= (dx >> 2) & 0xFFF;
            dos.writeInt(i);
            
            i = 0;
            i |= (dx & 0x03) << 14;
            i |= dy & 0x3FFF;
            dos.writeShort(i);
	    }
	}
	
	public int getWidth() {
		PipImage image = parent.getParent().getSourceImage(imageID);
		PipImageData imageData = image.getImageData(frame);
		if (transition < 4) {
			return imageData.getWidth();
		} else {
			return imageData.getHeight();
		}
	}

	public int getHeight() {
		PipImage image = parent.getParent().getSourceImage(imageID);
		PipImageData imageData = image.getImageData(frame);
		if (transition < 4) {
			return imageData.getHeight();
		} else {
			return imageData.getWidth();
		}
	}

	public PipImageData getImageData() {
		PipImage image = parent.getParent().getSourceImage(imageID);
		return image.getImageData(frame);
	}
	
	public void draw(GC g, int x, int y, double ratio, ImageDrawCache cache) {
		PipImage image = parent.getParent().getSourceImage(imageID);
		Image img = null;
		if (cache != null) {
		    img = cache.get(image, frame, transition);
		}
		if (img == null) {
			int imgCount = image.getImgCount();
    		int pp = frame / imgCount;
        	int ff = frame % imgCount;
        	image.setPaletteIndex(pp);
        	PipImageDraw dd = image.getImageDraw(ff);
        	if (cache != null && cache.getPalette() != null) {
        		dd.setRefPalette(cache.getPalette());
        	}
        	img = dd.createSWTImage(g.getDevice(), transition);
        	if (cache != null) {
        	    cache.add(image, frame, transition, img);
        	}
		}
		Rectangle rect = img.getBounds();
    	int drawx = (int)(dx * ratio + x);
    	int drawy = (int)(dy * ratio + y);
    	if (ratio == 1.0) {
    	    g.drawImage(img, drawx, drawy);
    	} else {
        	int draww = (int)(rect.width * ratio);
        	int drawh = (int)(rect.height * ratio);
    	    g.drawImage(img, 0, 0, rect.width, rect.height, drawx, drawy, draww, drawh);
    	}
    	if (cache == null) {
    	    img.dispose();
    	}
	}
	
	public PipAnimateFrame getParent() {
		return parent;
	}

	public void setParent(PipAnimateFrame parent) {
		this.parent = parent;
	}
	
	public Object clone() {
		PipAnimateFramePiece ret = new PipAnimateFramePiece(parent);
		ret.imageID = imageID;
		ret.frame = frame;
		ret.transition = transition;
		ret.dx = dx;
		ret.dy = dy;
		return ret;
	}
	
	public void enlarge() {
	    dx <<= 1;
	    dy <<= 1;
	}

	public void smaller() {
	    dx >>= 1;
	    dy >>= 1;
	}

	public boolean getVisible() {
		return visible;
	}

	public void setVisible(boolean visible2) {
		visible = visible2;
	}
}
