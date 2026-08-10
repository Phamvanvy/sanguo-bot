package com.pipimage.image;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;

public class PipAnimateFrame {
	protected PipAnimateSet parent;
	protected String name;
	protected ArrayList<PipAnimateFramePiece> pieces; 
	
	public PipAnimateFrame(PipAnimateSet parent) {
		this.parent = parent;
		pieces = new ArrayList<PipAnimateFramePiece>();
	}
	
	public int getPieceCount() {
		return pieces.size();
	}
	
	public PipAnimateFramePiece getPiece(int index) {
		return pieces.get(index);
	}
	
	public void removePiece(int index) {
		pieces.remove(index);
	}
	public void removePiece(PipAnimateFramePiece newObj){
		pieces.remove(newObj);
	}
	public void addPiece(PipAnimateFramePiece newObj){
		pieces.add(newObj);
	}
	public PipAnimateFramePiece addPiece(int imageID, int frame) {
		PipAnimateFramePiece newObj = new PipAnimateFramePiece(this, imageID, frame);
		pieces.add(newObj);
		return newObj;
	}
	
	public void swapPiece(int index1, int index2) {
	    if (index2 < 0) {
	        PipAnimateFramePiece piece1 = pieces.get(index1);
	        pieces.remove(index1);
	        pieces.add(0, piece1);
	    } else if (index2 >= pieces.size()) {
            PipAnimateFramePiece piece1 = pieces.get(index1);
            pieces.remove(index1);
            pieces.add(piece1);
	    } else {
	        PipAnimateFramePiece piece1 = pieces.get(index1);
	        pieces.set(index1, pieces.get(index2));
	        pieces.set(index2, piece1);
	    }
	}

	public void adjustImageIndex(int index) {
		for (int i = 0; i < pieces.size(); i++) {
			if (pieces.get(i).getImageID() == index) {
				pieces.remove(i);
				i--;
			} else if (pieces.get(i).getImageID() > index) {
				pieces.get(i).setImageID(pieces.get(i).getImageID() - 1);
			}
		}
	}
	
	public void swapImageIndex(int index1, int index2) {
        for (int i = 0; i < pieces.size(); i++) {
            if(pieces.get(i).getImageID() == index1){
                pieces.get(i).setImageID(index2);
            }else if(pieces.get(i).getImageID() == index2){
                pieces.get(i).setImageID(index1);
            }
        }
    }

	public void adjustFrameIndex(int image, Map<Integer, Integer> frameMap) {
		for (int i = 0; i < pieces.size(); i++) {
			PipAnimateFramePiece piece = pieces.get(i);
			if (piece.getImageID() == image) {
				Integer newFrame = frameMap.get(piece.getFrame());
				if (newFrame == null) {
					pieces.remove(i);
					i--;
				} else {
					piece.setFrame(newFrame.intValue());
				}
			}
		}
	}
	
	public void adjustFrameIndex(Map<Integer, Integer> frameMap) {
		for (int i = 0; i < pieces.size(); i++) {
			PipAnimateFramePiece piece = pieces.get(i);
			int id = (piece.getImageID() << 16) | piece.getFrame();
			if (frameMap.containsKey(id)) {
				int newFrame = frameMap.get(id);
				piece.setImageID(newFrame >> 16);
				piece.setFrame(newFrame & 0xFFFF);
			}
		}
	}

	public void load(DataInputStream dis) throws IOException {
		load(dis, false);
	}
	
	public void load(DataInputStream dis, boolean isctn) throws IOException {
		if (isctn) {
			name = "N/A";
		} else {
			name = dis.readUTF();
		}
		int fcount = dis.readByte() & 0xFF;
		for (int i = 0; i < fcount; i++) {
			PipAnimateFramePiece piece = new PipAnimateFramePiece(this);
			piece.load(dis);
			if (!isctn) {
				if (piece.getImageID() >= parent.getFileCount()) {
					// 检查依赖的图片是否存在
					continue;
				}
				PipImage sourceImg = parent.getSourceImage(piece.getImageID());
				if (piece.getFrame() >= sourceImg.getImgCount() * sourceImg.getImagePalettes().size()) {
					// 检查指定的帧是否存在
					continue;
				}
			}
			pieces.add(piece);
		}
	}
	
	public void save(DataOutputStream dos, boolean saveFullName) throws IOException {
	    if (pieces.size() >= 256) {
	        throw new IOException("每帧不能超过256个图块。");
	    }
		if (saveFullName) {
			dos.writeUTF(name);
		}
		dos.writeByte(pieces.size());
		for (int i = 0; i < pieces.size(); i++) {
			pieces.get(i).save(dos);
		}
	}

	public void draw(GC g, int x, int y, double ratio) {
	    draw(g, x, y, ratio, null);
	}

	public void draw(GC g, int x, int y, double ratio, ImageDrawCache cache) {
		if (parent.drawFrameListener != null) {
			if (!parent.drawFrameListener.beforeDrawFrame(parent, this, g, x, y, ratio)) {
				return;
			}
		}
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			if(pieces.get(i).visible)
				pieces.get(i).draw(g, x, y, ratio, cache);
		}
		if (parent.drawFrameListener != null) {
	    	parent.drawFrameListener.afterDrawFrame(parent, this, g, x, y, ratio);
	    }
	}
	
	public void draw(GC g, int x, int y, double ratio, int repImg, int src, int tgt,
			int offx, int offy, int trans) {
		if (parent.drawFrameListener != null) {
			if (!parent.drawFrameListener.beforeDrawFrame(parent, this, g, x, y, ratio)) {
				return;
			}
		}
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			if(pieces.get(i).visible)
				pieces.get(i).draw(g, x, y, ratio, null, repImg, src, tgt, offx, offy, trans);
		}
		if (parent.drawFrameListener != null) {
	    	parent.drawFrameListener.afterDrawFrame(parent, this, g, x, y, ratio);
	    }
	}
	
	public void draw(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache) {
		if (parent.drawFrameListener != null) {
			if (!parent.drawFrameListener.beforeDrawFrame(parent, this, g, x, y, ratio)) {
				return;
			}
		}
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			if(pieces.get(i).visible)
				pieces.get(i).draw(g, x, y, ratio, cache, 0, 100, 100, 0xFFFFFFFF);
		}
		if (parent.drawFrameListener != null) {
	    	parent.drawFrameListener.afterDrawFrame(parent, this, g, x, y, ratio);
	    }
	}
	
	public void draw(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache, int rotate, int scalex, int scaley, int color) {
		if (parent.drawFrameListener != null) {
			if (!parent.drawFrameListener.beforeDrawFrame(parent, this, g, x, y, ratio)) {
				return;
			}
		}
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			if(pieces.get(i).visible)
				pieces.get(i).draw(g, x, y, ratio, cache, rotate, scalex, scaley, color);
		}
		if (parent.drawFrameListener != null) {
	    	parent.drawFrameListener.afterDrawFrame(parent, this, g, x, y, ratio);
	    }
	}
	
	// 拟合绘制过渡帧
	public void drawTransform(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache, PipAnimateFrame nextFrame, double percent, int rotate, int scalex, int scaley, int color) {
		if (parent.drawFrameListener != null) {
			if (!parent.drawFrameListener.beforeDrawFrame(parent, this, g, x, y, ratio)) {
				return;
			}
		}
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			PipAnimateFramePiece piece = pieces.get(i);
			if (!piece.visible) {
				continue;
			}
			PipAnimateFramePiece nextPiece = null;
			if (nextFrame != null) {
				for (int j = 0; j < nextFrame.getPieceCount(); j++) {
					PipAnimateFramePiece np = nextFrame.getPiece(j);
					if (np.getImageID() == piece.getImageID() && np.getFrame() == piece.getFrame() && np.getTransition() == piece.getTransition()) {
						nextPiece = np;
						break;
					}
				}
			}
			piece.drawTransform(g, x, y, ratio, cache, nextPiece, percent, rotate, scalex, scaley, color);
		}
		if (parent.drawFrameListener != null) {
	    	parent.drawFrameListener.afterDrawFrame(parent, this, g, x, y, ratio);
	    }
	}
	
	public void draw(GLGraphics g, int x, int y, double ratio, int repImg, int src, int tgt,
			int offx, int offy, int trans) {
		if (parent.drawFrameListener != null) {
			if (!parent.drawFrameListener.beforeDrawFrame(parent, this, g, x, y, ratio)) {
				return;
			}
		}
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			if(pieces.get(i).visible)
				pieces.get(i).draw(g, x, y, ratio, null, repImg, src, tgt, offx, offy, trans);
		}
		if (parent.drawFrameListener != null) {
	    	parent.drawFrameListener.afterDrawFrame(parent, this, g, x, y, ratio);
	    }
	}

	public PipAnimateSet getParent() {
		return parent;
	}

	public void setParent(PipAnimateSet parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Rectangle getBounds() {
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (int i = 0; i < pieces.size(); i++) {
			PipAnimateFramePiece piece = pieces.get(i);
			double[][] points = piece.getBounds();
			for (int j = 0; j < 4; j++) {
				int px = (int)points[j][0];
				int py = (int)points[j][1];
				if (px < minx) {
					minx = px;
				}
				if (py < miny) {
					miny = py;
				}
				if (px > maxx) {
					maxx = px;
				}
				if (py > maxy) {
					maxy = py;
				}
			}
		}
		Rectangle rect = new Rectangle(minx, miny, maxx - minx, maxy - miny);
		if (rect.isEmpty()) {
			rect = new Rectangle(0, 0, 16, 16);
		}
		return rect;
	}

	public Rectangle getBounds(int repImg, int src, int tgt, int offx, int offy) {
		int minx = 0;
		int miny = 0;
		int maxx = 0;
		int maxy = 0;
		for (int i = 0; i < pieces.size(); i++) {
			PipAnimateFramePiece piece = pieces.get(i);
			boolean replace = false;
			if (piece.getImageID() == repImg && piece.getFrame() == src) {
				piece.setFrame(tgt);
				piece.setDx(piece.getDx() + offx);
				piece.setDy(piece.getDy() + offy);
				replace = true;
			}
			int px = piece.getDx();
			int py = piece.getDy();
			int pw = piece.getWidth();
			int ph = piece.getHeight();
			if (px < minx) {
				minx = px;
			}
			if (py < miny) {
				miny = py;
			}
			if (px + pw > maxx) {
				maxx = px + pw;
			}
			if (py + ph > maxy) {
				maxy = py + ph;
			}
			if (replace) {
				piece.setFrame(src);
				piece.setDx(piece.getDx() - offx);
				piece.setDy(piece.getDy() - offy);
			}
		}
		Rectangle rect = new Rectangle(minx, miny, maxx - minx, maxy - miny);
		if (rect.isEmpty()) {
			rect = new Rectangle(0, 0, 16, 16);
		}
		return rect;
	}
	
	public Object clone() {
		PipAnimateFrame ret = new PipAnimateFrame(parent);
		ret.name = name;
		for (int i = 0; i < pieces.size(); i++) {
			PipAnimateFramePiece piece = (PipAnimateFramePiece)pieces.get(i).clone();
			piece.setParent(ret);
			ret.pieces.add(piece);
		}
		return ret;
	}
	
	public void hflip() {
	    for (PipAnimateFramePiece piece : pieces) {
	    	if (piece instanceof PipAni4AniFramePiece) {
	    		piece.dx = -piece.dx;
	    	} else {
		        piece.transition = PipImage.hflip(piece.transition);
		        piece.dx = -piece.dx - piece.getWidth();
		        if (piece.rotate != 0) {
		        	int angle = ((piece.rotate % 360) + 360) % 360;
		        	piece.rotate = (piece.rotate - angle) + (360 - angle);
		        }
	    	}
	    }
	}
	
	public void vflip() {
        for (PipAnimateFramePiece piece : pieces) {
        	if (piece instanceof PipAni4AniFramePiece) {
	    		piece.dy = -piece.dy;
        	} else {
	            piece.transition = PipImage.vflip(piece.transition);
	            piece.dy = -piece.dy - piece.getHeight();
	            if (piece.rotate != 0) {
		        	int angle = ((piece.rotate % 360) + 360) % 360;
		        	piece.rotate = (piece.rotate - angle) + (360 - angle);
		        }
        	}
        }
	}
	
	public void enlarge() {
	    for (PipAnimateFramePiece piece : pieces) {
	        piece.enlarge();
	    }
	}

	public void smaller() {
	    for (PipAnimateFramePiece piece : pieces) {
	        piece.smaller();
	    }
	}

	public PipAni4AniFramePiece getHook(int hookId) {
		for (PipAnimateFramePiece piece : pieces) {
			if(piece instanceof PipAni4AniFramePiece && piece.imageID==hookId){
				return (PipAni4AniFramePiece) piece;
			}
        }
		return null;
	}

	public void addPieceAt(int frame, PipAnimateFramePiece hook) {
		if (frame > pieces.size()) {
			frame = pieces.size();
		}
		pieces.add(frame, hook);
	}

	public int getPieceIndex(PipAnimateFramePiece pc) {
		return pieces.indexOf(pc);
	}
	
	public boolean containsPiece(int imageID, int frameID) {
		for (PipAnimateFramePiece pc : pieces) {
			if (pc.imageID == imageID && pc.frame == frameID) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 从另外一个动画帧复制所有图块。考虑到可能会有PipAni4AniFramePiece挂接点图块的情况，所以处理起来比较复杂一些。
	 * 挂接点类型的图块要保证调整后顺序一致，且不破坏之前挂接的PipAnimate对象。普通类型的图块复制属性即可。
	 * @param frame
	 */
	public void update(PipAnimateFrame frame) {
		// 首先同步所有普通的图块属性
		int i = 0;
		int j = 0;
		PipAnimateFramePiece p1, p2;
		List<PipAni4AniFramePiece> hooks1 = new ArrayList<PipAni4AniFramePiece>();
		List<PipAni4AniFramePiece> hooks2 = new ArrayList<PipAni4AniFramePiece>();
		while (i < pieces.size() || j < frame.pieces.size()) {
			if (i >= pieces.size()) {
				// 目标中图块多
				p2 = frame.pieces.get(j);
				if (p2 instanceof PipAni4AniFramePiece) {
					hooks2.add((PipAni4AniFramePiece)p2);
					j++;
				} else {
					p1 = addPiece(0, 0);
					p1.update(p2);
					i++;
					j++;
				}
			} else if (j >= frame.pieces.size()) {
				// 本帧中图块多
				p1 = pieces.get(i);
				if (p1 instanceof PipAni4AniFramePiece) {
					hooks1.add((PipAni4AniFramePiece)p1);
					i++;
				} else {
					pieces.remove(i);
				}
			} else {
				p1 = pieces.get(i);
				if (p1 instanceof PipAni4AniFramePiece) {
					hooks1.add((PipAni4AniFramePiece)p1);
					i++;
					continue;
				}
				p2 = frame.pieces.get(j);
				if (p2 instanceof PipAni4AniFramePiece) {
					hooks2.add((PipAni4AniFramePiece)p2);
					j++;
					continue;
				}
				p1.update(p2);
				i++;
				j++;
			}
		}
		
		// 如果有挂接点，按照目标帧中挂接点的顺序调整本帧中挂接点的顺序，并复制位置
		if (hooks1.size() != hooks2.size()) {
			throw new IllegalArgumentException("hook counts mismatch.");
		}
		for (i = 0; i < hooks1.size(); i++) {
			if (hooks1.get(i).imageID == hooks2.get(i).getImageID()) {
				hooks1.get(i).setRealDx(hooks2.get(i).getRealDx());
				hooks1.get(i).setRealDy(hooks2.get(i).getRealDy());
				continue;
			}
			for (j = i + 1; j < hooks1.size(); j++) {
				if (hooks1.get(j).imageID == hooks2.get(i).getImageID()) {
					break;
				}
			}
			if (i >= hooks1.size()) {
				throw new IllegalArgumentException("hook id mismatch.");
			}
			PipAni4AniFramePiece tp1 = hooks1.get(i);
			PipAni4AniFramePiece tp2 = hooks1.get(j);
			int index1 = pieces.indexOf(tp1);
			int index2 = pieces.indexOf(tp2);
			pieces.set(index2, tp1);
			pieces.set(index1, tp2);
			hooks1.set(i, tp2);
			hooks1.set(j, tp1);
			tp2.setRealDx(hooks2.get(i).getRealDx());
			tp2.setRealDy(hooks2.get(i).getRealDy());
		}
		
		// 按目标帧中挂接点的层次调整本帧中挂接点的层次
		for (i = 0; i < hooks1.size(); i++) {
			int index1 = pieces.indexOf(hooks1.get(i));
			int index2 = frame.pieces.indexOf(hooks2.get(i));
			if (index1 != index2) {
				pieces.remove(index1);
				pieces.add(index2, hooks1.get(i));
			}
		}
	}
	
	/**
	 * 当一个图块被拆分以后，调整用到被拆分的图块的帧。
	 * @param imgID
	 * @param frame
	 * @param splitPlan
	 */
	public void onImageSplit(int imgID, int frame, int[][] splitPlan, int oldw, int oldh) {
		for (int i = pieces.size() - 1; i >= 0; i--) {
			PipAnimateFramePiece p = pieces.get(i);
			if (p.imageID == imgID && p.frame == frame) {
				pieces.remove(i);
				for (int j = 0; j < splitPlan.length; j++) {
					PipAnimateFramePiece newp = new PipAnimateFramePiece(this, imgID, frame + j);
					newp.transition = p.transition;
					int bx = splitPlan[j][0];
					int by = splitPlan[j][1];
					int bw = splitPlan[j][2];
					int bh = splitPlan[j][3];
					switch (p.transition) {
					case 1: // 垂直翻转
						newp.dx = p.dx + bx;
						newp.dy = p.dy + oldh - bh - by;
						break;
					case 2: // 水平翻转
						newp.dx = p.dx + oldw - bw - bx;
						newp.dy = p.dy + by;
						break;
					case 3: // 垂直+水平翻转
						newp.dx = p.dx + oldw - bw - bx;
						newp.dy = p.dy + oldh - bh - by;
						break;
					case 4: // 水平翻转+逆时针90度
						newp.dx = p.dx + by;
						newp.dy = p.dy + bx;
						break;
					case 5: // 顺时针90度
						newp.dx = p.dx + oldh - bh - by;
						newp.dy = p.dy + bx;
						break;
					case 6: // 逆时针90度
						newp.dx = p.dx + by;
						newp.dy = p.dy + oldw - bw - bx;
						break;
					case 7: // 水平翻转+顺时针90度
						newp.dx = p.dx + oldh - bh - by;
						newp.dy = p.dy + oldw - bw - bx;
						break;
					default:
						newp.dx = p.dx + bx;
						newp.dy = p.dy + by;
						break;
					}
					pieces.add(i + j, newp);
				}
			} else if (p.imageID == imgID && p.frame > frame) {
				p.frame += splitPlan.length - 1;
			}
		}
	}
}
