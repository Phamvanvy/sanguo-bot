package com.pipimage.image;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

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

	public void load(DataInputStream dis) throws IOException {
		name = dis.readUTF();
		int fcount = dis.readByte() & 0xFF;
		for (int i = 0; i < fcount; i++) {
			PipAnimateFramePiece piece = new PipAnimateFramePiece(this);
			piece.load(dis);
			if (piece.getImageID() >= parent.getFileCount()) {
				// 检查依赖的图片是否存在
				continue;
			}
			PipImage sourceImg = parent.getSourceImage(piece.getImageID());
			if (piece.getFrame() >= sourceImg.getImgCount() * sourceImg.getImagePalettes().size()) {
				// 检查指定的帧是否存在
				continue;
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
		int count = pieces.size();
		for (int i = 0; i < count; i++) {
			if(pieces.get(i).visible)
			pieces.get(i).draw(g, x, y, ratio, cache);
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
		int minx = 0;
		int miny = 0;
		int maxx = 0;
		int maxy = 0;
		for (int i = 0; i < pieces.size(); i++) {
			PipAnimateFramePiece piece = pieces.get(i);
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
	        piece.transition = PipImage.hflip(piece.transition);
	        piece.dx = -piece.dx - piece.getWidth();
	    }
	}
	
	public void vflip() {
        for (PipAnimateFramePiece piece : pieces) {
            piece.transition = PipImage.vflip(piece.transition);
            piece.dy = -piece.dy - piece.getHeight();
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
}
