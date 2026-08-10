package com.pipimage.image;

import java.io.*;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;

public class PipAnimateFramePiece {
	protected PipAnimateFrame parent;
	protected int imageID;
	protected int frame;
	protected int transition;
	protected int dx;
	protected int dy;
	protected boolean visible = true;
	// 旋转角度-32768-32767
	public int rotate = 0;
	// X方向缩放比例（单位1%）
	public int scalex = 100;
	// Y方向缩放比例（单位1%）
	public int scaley = 100;
	// 颜色变换
	public int color = 0xFFFFFFFF;
	
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
    		GLUtils.unloadImage(img);
    	}
	}
	
	public void draw(GC g, int x, int y, double ratio, ImageDrawCache cache,
			int repImg, int src, int tgt, int offx, int offy, int trans) {
		boolean replace = false;
		int oldTrans = transition;
		if (repImg == imageID && src == frame) {
			frame = tgt;
			dx += offx;
			dy += offy;
			if (trans == 1) {
				// 水平翻转
				switch (transition) {
				case 0:
					transition = 2;
					break;
				case 1:
					transition = 3;
					break;
				case 2:
					transition = 0;
					break;
				case 3:
					transition = 1;
					break;
				case 4:
					transition = 6;
					break;
				case 5:
					transition = 7;
					break;
				case 6:
					transition = 4;
					break;
				case 7:
					transition = 5;
					break;
				}
			}
			replace = true;
		}
		draw(g, x, y, ratio, cache);
    	if (replace) {
    		frame = src;
    		dx -= offx;
    		dy -= offy;
    		transition = oldTrans;
    	}
	}
	
	public void draw(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache, int rotate2, int scalex2, int scaley2, int color2) {
		if (transition >= 4) {
			// 带90度旋转的图片，无法按新模式处理，完全按老模式绘制
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
	        	img = dd.createSWTImage(null, transition);
	        	if (cache != null) {
	        	    cache.add(image, frame, transition, img);
	        	}
			}
			Rectangle rect = img.getBounds();
	    	int drawx = (int)(dx * ratio + x);
	    	int drawy = (int)(dy * ratio + y);
	    	if (ratio == 1.0) {
	    	    g.drawTexture(GLUtils.loadImage(img), 0, 0, drawx, drawy);
	    	} else {
	        	int draww = (int)(rect.width * ratio);
	        	int drawh = (int)(rect.height * ratio);
	    	    g.drawTexture(GLUtils.loadImage(img), 0, 0, drawx, drawy, draww, drawh);
	    	}
	    	if (cache == null) {
	    		GLUtils.unloadImage(img);
	    	}
			return;
		}
		// 先生成没有做任何旋转处理的图片
		PipImage image = parent.getParent().getSourceImage(imageID);
		Image img = null;
		if (cache != null) {
		    img = cache.get(image, frame, 0);
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
        	img = dd.createSWTImage(null, 0);
        	if (cache != null) {
        	    cache.add(image, frame, 0, img);
        	}
		}
		
		// 生成原始的四个顶点坐标
		Rectangle rect = img.getBounds();
		double[][] points = new double[][] {
				{ 0, 0, 1 },
				{ rect.width, 0, 1 },
				{ 0, rect.height, 1 },
				{ rect.width, rect.height, 1 }
		};
		
		// 如果基准点不在左上角，根据基准点位置调整4个顶点坐标
		PipImageData id = image.getImageData(frame);
		if (id.anchorx != 0 || id.anchory != 0) {
			points = GLUtils.mul(points, new double[][] { 
					{ 1, 0, 0 },
					{ 0, 1, 0 },
					{ -id.anchorx, -id.anchory, 1 }
			});
		}
		
		// 如果图片做了水平翻转或者垂直翻转，调整4个顶点以实现翻转效果
		if ((transition & 2) != 0) {
			points[0][0] = -points[0][0];
			points[1][0] = -points[1][0];
			points[2][0] = -points[2][0];
			points[3][0] = -points[3][0];
		}
		if ((transition & 1) != 0) {
			points[0][1] = -points[0][1];
			points[1][1] = -points[1][1];
			points[2][1] = -points[2][1];
			points[3][1] = -points[3][1];
		}
		
		// 缩放
		points = GLUtils.mul(points, new double[][] {
				{ ratio * scalex / 100.0, 0, 0 },
				{ 0, ratio * scaley / 100.0, 0 },
				{ 0, 0, 1 }
		});
		
		// 旋转
		double angle = ((rotate % 360) + 360) % 360;
		angle = angle * Math.PI / 180;
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		points = GLUtils.mul(points, new double[][] {
				{ cos, -sin, 0 },
				{ sin, cos, 0 },
				{ 0, 0, 1}
		});
		
		// 计算基准点位置
		int[] apos = getAnchorPos();
		points = GLUtils.mul(points, new double[][] {
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ apos[0] * ratio, apos[1] * ratio, 1 }
		});
		
		// 如果整个动画也有缩放和旋转，则这里进行处理
		if (scalex2 != 100 || scaley2 != 100) {
			points = GLUtils.mul(points, new double[][] {
					{ scalex2 / 100.0, 0, 0 },
					{ 0, scaley2 / 100.0, 0 },
					{ 0, 0, 1 }
			});
		}
		if (rotate2 != 0) {
			double angle2 = ((rotate2 % 360) + 360) % 360;
			angle2 = angle2 * Math.PI / 180;
			double sin2 = Math.sin(angle2);
			double cos2 = Math.cos(angle2);
			points = GLUtils.mul(points, new double[][] {
					{ cos2, -sin2, 0 },
					{ sin2, cos2, 0 },
					{ 0, 0, 1}
			});
		}
		int drawColor = color;
		if (color2 != 0xFFFFFFFF) {
			drawColor = GLUtils.mulColor(color, color2);
		}
		
		// 加上帧基准点偏移
		points = GLUtils.mul(points, new double[][] {
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ x, y, 1 }
		});
		
		g.drawTextureFree(GLUtils.loadImage(img), 0, (float)points[0][0], (float)points[0][1], (float)points[1][0], (float)points[1][1],
				(float)points[2][0], (float)points[2][1], (float)points[3][0], (float)points[3][1], drawColor);
    	if (cache == null) {
    		GLUtils.unloadImage(img);
    	}
	}
	
	// 拟合计算中间帧位置并绘制。
	public void drawTransform(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache, PipAnimateFramePiece nextPiece, double percent, int rotate2, int scalex2, int scaley2, int color2) {
		int saveDx = dx;
		int saveDy = dy;
		int saveRotate = rotate;
		int saveScalex = scalex;
		int saveScaley = scaley;
		int saveColor = color;
		
		if (nextPiece != null) {
			dx = GLUtils.getInternValue(dx, nextPiece.dx, percent);
			dy = GLUtils.getInternValue(dy, nextPiece.dy, percent);
			rotate = GLUtils.getInternValue(rotate, nextPiece.rotate, percent);
			scalex = GLUtils.getInternValue(scalex, nextPiece.scalex, percent);
			scaley = GLUtils.getInternValue(scaley, nextPiece.scaley, percent);
			color = GLUtils.getInternColor(color, nextPiece.color, percent);
		} else {
			color = GLUtils.getInternColor(color, color & 0xFFFFFF, percent);
		}
		
		draw(g, x, y, ratio, cache, rotate2, scalex2, scaley2, color2);
		
		dx = saveDx;
		dy = saveDy;
		rotate = saveRotate;
		scalex = saveScalex;
		scaley = saveScaley;
		color = saveColor;
	}
	
	/**
	 * 取得缩放旋转后的4个顶点位置（相对于基准点）。
	 * @return
	 */
	public double[][] getBounds() {
		PipImage image = parent.getParent().getSourceImage(imageID);
		PipImageData id = image.getImageData(frame);
		
		if (transition >= 4) {
			// 带90度旋转的图片，无法按新模式处理，完全按老模式绘制
			return new double[][] {
				{ dx, dy, 1 },
				{ dx + id.height, dy, 1 },
				{ dx + id.height, dy + id.width, 1 },
				{ dx, dy + id.width, 1 }
			};
		} else {
			// 生成原始的四个顶点坐标
			double[][] points = new double[][] {
					{ 0, 0, 1 },
					{ id.width, 0, 1 },
					{ id.width, id.height, 1 },
					{ 0, id.height, 1 }
			};

			// 如果基准点不在左上角，根据基准点位置调整4个顶点坐标
			if (id.anchorx != 0 || id.anchory != 0) {
				points = GLUtils.mul(points, new double[][] { 
						{ 1, 0, 0 },
						{ 0, 1, 0 },
						{ -id.anchorx, -id.anchory, 1 }
				});
			}
			
			// 如果图片做了水平翻转或者垂直翻转，调整4个顶点以实现翻转效果
			if ((transition & 2) != 0) {
				points[0][0] = -points[0][0];
				points[1][0] = -points[1][0];
				points[2][0] = -points[2][0];
				points[3][0] = -points[3][0];
			}
			if ((transition & 1) != 0) {
				points[0][1] = -points[0][1];
				points[1][1] = -points[1][1];
				points[2][1] = -points[2][1];
				points[3][1] = -points[3][1];
			}
			
			// 缩放
			points = GLUtils.mul(points, new double[][] {
					{ scalex / 100.0, 0, 0 },
					{ 0, scaley / 100.0, 0 },
					{ 0, 0, 1 }
			});
			
			// 旋转
			double angle = ((rotate % 360) + 360) % 360;
			angle = angle * Math.PI / 180;
			double sin = Math.sin(angle);
			double cos = Math.cos(angle);
			points = GLUtils.mul(points, new double[][] {
					{ cos, -sin, 0 },
					{ sin, cos, 0 },
					{ 0, 0, 1}
			});
			
			// 计算基准点位置
			int[] apos = getAnchorPos();
			points = GLUtils.mul(points, new double[][] {
					{ 1, 0, 0 },
					{ 0, 1, 0 },
					{ apos[0], apos[1], 1 }
			});
			
			return points;
		}
	}
	
	/**
	 * 计算实际基准点位置（翻转以后基准点位置会变化，不是原始的dx和dy）
	 * @return
	 */
	public int[] getAnchorPos() {
		PipImage image = parent.getParent().getSourceImage(imageID);
		PipImageData id = image.getImageData(frame);
		int basex = dx;
		int basey = dy;
		if ((transition & 2) != 0) {
			basex += id.width;
		}
		if ((transition & 1) != 0) {
			basey += id.height;
		}
		return new int[] { basex, basey };
	}
	
	public void draw(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache,
			int repImg, int src, int tgt, int offx, int offy, int trans) {
		boolean replace = false;
		int oldTrans = transition;
		if (repImg == imageID && src == frame) {
			frame = tgt;
			dx += offx;
			dy += offy;
			if (trans == 1) {
				// 水平翻转
				switch (transition) {
				case 0:
					transition = 2;
					break;
				case 1:
					transition = 3;
					break;
				case 2:
					transition = 0;
					break;
				case 3:
					transition = 1;
					break;
				case 4:
					transition = 6;
					break;
				case 5:
					transition = 7;
					break;
				case 6:
					transition = 4;
					break;
				case 7:
					transition = 5;
					break;
				}
			}
			replace = true;
		}
		draw(g, x, y, ratio, cache, 0, 100, 100, 0xFFFFFFFF);
    	if (replace) {
    		frame = src;
    		dx -= offx;
    		dy -= offy;
    		transition = oldTrans;
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
		ret.rotate = rotate;
		ret.scalex = scalex;
		ret.scaley = scaley;
		ret.color = color;
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
	
	/**
	 * 从另一个图块对象复制所有属性。
	 * @param p
	 */
	public void update(PipAnimateFramePiece p) {
		imageID = p.imageID;
		frame = p.frame;
		transition = p.transition;
		dx = p.dx;
		dy = p.dy;
		rotate = p.rotate;
		scalex = p.scalex;
		scaley = p.scaley;
		color = p.color;
	}
}
