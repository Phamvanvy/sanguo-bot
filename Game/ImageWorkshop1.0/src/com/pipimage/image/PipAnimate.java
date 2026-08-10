package com.pipimage.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.*;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.util.Point;

public class PipAnimate {
	protected PipAnimateSet parent;
	protected String name;
	protected ArrayList<PipAnimateFrameRef> frames;
	
	public PipAnimate(PipAnimateSet parent) {
		this.parent = parent;
		name = "";
		frames = new ArrayList<PipAnimateFrameRef>();
	}
	
	public int getFrameCount() {
		return frames.size();
	}
	
	public PipAnimateFrameRef getFrame(int index) {
		return frames.get(index);
	}
	
	public void removeFrame(int index) {
		frames.remove(index);
	}
	
	public int getFrameIndex(PipAnimateFrameRef frameRef){
		return frames.indexOf(frameRef);
	}
	
	public PipAnimateFrameRef addFrame(int frame) {
		PipAnimateFrameRef newFrame = new PipAnimateFrameRef(this, frame);
//		PipAnimateFrame realFrame = parent.getFrame(frame);
//		newFrame.setDx(-realFrame.getBounds().width / 2);
//		newFrame.setDy(-realFrame.getBounds().height / 2);
		newFrame.setDelay(1);
		frames.add(newFrame);
		return newFrame;
	}
	
	public int getFrameAtTime(int time) {
		int[] tmp = new int[1000];
		int index = 0;
		for (int i = 0; i < frames.size(); i++) {
			PipAnimateFrameRef ref = frames.get(i);
			for (int j = 0; j < ref.delay; j++) {
				tmp[index++] = i;
			}
		}
		if (index == 0) {
			return -1;
		}
		return tmp[time % index];
	}
	
	public int getOffsetInFrame(int time) {
		int total = 0;
		for (int i = 0; i < frames.size(); i++) {
			total += frames.get(i).delay;
		}
		if (total == 0) { 
			return 0;
		}
		time = time % total;
		for (int i = 0; i < frames.size(); i++) {
			if (time < frames.get(i).delay) {
				return time;
			}
			time -= frames.get(i).delay;
		}
		return 0;
	}
	
	public int getTimeOfFrame(int frame) {
		int ret = 0;
		for (int i = 0; i < frame; i++) {
			PipAnimateFrameRef ref = frames.get(i);
			ret += ref.delay;
		}
		return ret;
	}
	
	public void swapFrames(int index1, int index2) {
		PipAnimateFrameRef temp = frames.get(index1);
		frames.set(index1, frames.get(index2));
		frames.set(index2, temp);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
			PipAnimateFrameRef frame = new PipAnimateFrameRef(this);
			frame.load(dis);
			frames.add(frame);
		}
	}
	
	public void save(DataOutputStream dos, boolean saveFullName) throws IOException {
	    if (frames.size() >= 256) {
	        throw new IOException("每个动画序列不能超过256帧。");
	    }
		if (saveFullName) {
			dos.writeUTF(name);
		}
		dos.writeByte(frames.size());
		for (int i = 0; i < frames.size(); i++) {
			frames.get(i).save(dos);
		}
	}
	
	public void drawFrame(GC g, int frame, int x, int y, double ratio) {
	    drawFrame(g, frame, x, y, ratio, null);
	}

	public void drawFrame(GC g, int frame, int x, int y, double ratio, ImageDrawCache cache) {
		if (frame < 0 || frame >= frames.size()) {
			return;
		}
		frames.get(frame).draw(g, x, y, ratio, cache);
	}

	public void drawFrame(GLGraphics g, int frame, int x, int y, double ratio, ImageDrawCache cache) {
		if (frame < 0 || frame >= frames.size()) {
			return;
		}
		frames.get(frame).draw(g, x, y, ratio, cache);
	}
	
	public void drawAnimateFrame(GC g, int time, int x, int y, double ratio, ImageDrawCache cache) {
		int currentFrame = getFrameAtTime(time);
		if (currentFrame == -1) {
			return;
		}
		PipAnimateFrameRef frameRef = frames.get(currentFrame);
		PipAnimateFrame frame = frameRef.realize();
		x += frameRef.getDx() * ratio;
		y += frameRef.getDy() * ratio;
		frame.draw(g, x, y, ratio, cache);
	}
	
	public void drawAnimateFrame(GLGraphics g, int time, int x, int y, double ratio, ImageDrawCache cache) {
		int currentFrame = getFrameAtTime(time);
		if (currentFrame == -1) {
			return;
		}
		int frameOffset = getOffsetInFrame(time);
		PipAnimateFrameRef frameRef = frames.get(currentFrame);
		PipAnimateFrame frame = frameRef.realize();
		if (frameRef.enableTransform) {
			// 播放渐变效果
			double factor = frameRef.speedFactor / 100.0;
			// 设定第一帧的速度是x，则最后一帧的速度是x * factor，中间速度匀速变化delay - 1次，每次变化量为x * (factor - 1) / (delay - 1)
			// 总共的距离是x + x * (1 + (factor - 1) / (delay - 1)) + ... + x * factor
			//     = (x + x * factor) * delay / 2
			//     = x * (1 + factor) * delay / 2
			//     = 1
			// 那么，初始速度x = 2 / (delay * (1 + factor))
			double initSpeed = 2 / (frameRef.getDelay() * (1 + factor));
			double accel = initSpeed * (factor - 1) / (frameRef.getDelay() - 1);
			double endSpeed = initSpeed + accel * (frameOffset - 1);
			double percent = (initSpeed + endSpeed) * frameOffset / 2;
			PipAnimateFrameRef nextFrameRef = null;
			PipAnimateFrame nextFrame = null;
			if (currentFrame < getFrameCount() - 1) {
				nextFrameRef = getFrame(currentFrame + 1);
				nextFrame = nextFrameRef.realize();
			}
			if (nextFrameRef != null) {
				int dx2 = GLUtils.getInternValue(frameRef.getDx(), nextFrameRef.getDx(), percent);
				int dy2 = GLUtils.getInternValue(frameRef.getDy(), nextFrameRef.getDy(), percent);
				x += dx2 * ratio;
				y += dy2 * ratio;
			} else {
				x += frameRef.getDx() * ratio;
				y += frameRef.getDy() * ratio;
			}
			if (nextFrameRef != null && frameRef.getFrame() == nextFrameRef.getFrame()) {
				int rotate2 = GLUtils.getInternValue(frameRef.rotate, nextFrameRef.rotate, percent);
				int scalex2 = GLUtils.getInternValue(frameRef.scalex, nextFrameRef.scalex, percent);
				int scaley2 = GLUtils.getInternValue(frameRef.scaley, nextFrameRef.scaley, percent);
				int color2 = GLUtils.getInternColor(frameRef.color, nextFrameRef.color, percent);
				frame.drawTransform(g, x, y, ratio, cache, nextFrame, percent, rotate2, scalex2, scaley2, color2);
			} else {
				frame.drawTransform(g, x, y, ratio, cache, nextFrame, percent, frameRef.rotate, frameRef.scalex, frameRef.scaley, frameRef.color);
			}
		} else {
			x += frameRef.getDx() * ratio;
			y += frameRef.getDy() * ratio;
			frame.draw(g, x, y, ratio, cache, frameRef.rotate, frameRef.scalex, frameRef.scaley, frameRef.color);
		}
	}
	
	public PipAnimateSet getParent() {
		return parent;
	}

	public void setParent(PipAnimateSet parent) {
		this.parent = parent;
	}

	public void onFrameRemoved(int frame) {
		for (int i = 0; i < frames.size(); i++) {
			if (frames.get(i).getFrame() == frame) {
				frames.remove(i);
				i--;
			} else if (frames.get(i).getFrame() > frame) {
				frames.get(i).setFrame(frames.get(i).getFrame() - 1);
			}
		}
	}
	
	public void onFrameAdded(int frame) {
        for (int i = 0; i < frames.size(); i++) {
            if (frames.get(i).getFrame() >= frame) {
                frames.get(i).setFrame(frames.get(i).getFrame() + 1);
            }
        }
    }
	
	public void onFrameSwap(int frame1, int frame2) {
	    for (int i = 0; i < frames.size(); i++) {
            if (frames.get(i).getFrame() == frame1) {
                frames.get(i).setFrame(frame2);
            } else if (frames.get(i).getFrame() == frame2) {
                frames.get(i).setFrame(frame1);
            }
        }
	}
	
	public Rectangle getBounds() {
        int minx = 0;
        int miny = 0;
        int maxx = 0;
        int maxy = 0;
        int count = frames.size();
        for (int i = 0; i < count; i++) {
        	PipAnimateFrameRef pafr = frames.get(i);
            Rectangle frameBounds = pafr.realize().getBounds();
            if (pafr.rotate != 0 || pafr.scalex != 100 || pafr.scaley != 100) {
            	double[][] points = new double[][] {
            			{ frameBounds.x, frameBounds.y, 1 },
            			{ frameBounds.x + frameBounds.width, frameBounds.y , 1 },
            			{ frameBounds.x + frameBounds.width, frameBounds.y + frameBounds.height, 1 },
            			{ frameBounds.x, frameBounds.y + frameBounds.height, 1 }
            	};
            	points = GLUtils.mul(points, new double[][] {
            			{ pafr.scalex / 100.0, 0, 0 },
            			{ 0, pafr.scaley / 100.0, 0 },
            			{ 0, 0, 1 }
            	});
            	points = GLUtils.rotate(points, pafr.rotate);
            	points = GLUtils.mul(points, new double[][] {
        				{ 1, 0, 0 },
        				{ 0, 1, 0 },
        				{ pafr.dx, pafr.dy, 1 }
        		});
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
            } else {
	            int px = frameBounds.x + pafr.dx;
	            int py = frameBounds.y + pafr.dy;
	            int pw = frameBounds.width;
	            int ph = frameBounds.height;
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
        }
        
        Rectangle rect = null;
        int w = maxx - minx;
        int h = maxy - miny;
        if(w <= 0 || h <= 0) {
        	rect = new Rectangle(0, 0, 16, 16);
        } else {
        	rect = new Rectangle(minx, miny, w, h);
        }
        return rect;
    }

	public Rectangle getBounds(int repImg, int src, int tgt, int offx, int offy) {
        int minx = 0;
        int miny = 0;
        int maxx = 0;
        int maxy = 0;
        int count = frames.size();
        for (int i = 0; i < count; i++) {
        	PipAnimateFrameRef pafr = frames.get(i);
            Rectangle frameBounds = pafr.realize().getBounds(repImg, src, tgt, offx, offy);
            int px = frameBounds.x + pafr.dx;
            int py = frameBounds.y + pafr.dy;
            int pw = frameBounds.width;
            int ph = frameBounds.height;
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
        
        Rectangle rect = null;
        int w = maxx - minx;
        int h = maxy - miny;
        if(w <= 0 || h <= 0) {
        	rect = new Rectangle(0, 0, 16, 16);
        } else {
        	rect = new Rectangle(minx, miny, w, h);
        }
        return rect;
    }
	
	public void enlarge() {
	    for (PipAnimateFrameRef ref : frames) {
	        ref.enlarge();
	    }
	}

	public void smaller() {
	    for (PipAnimateFrameRef ref : frames) {
	        ref.smaller();
	    }
	}

	public void fillWith(PipAnimate animate, HashMap<Integer, Integer> frameIDMap) {
		for (int i = 0; i < animate.getFrameCount(); i++) {
	        PipAnimateFrameRef afr = animate.getFrame(i);
	        PipAnimateFrameRef newafr = addFrame(frameIDMap.get(afr.getFrame()));
	        newafr.setDx(afr.getDx());
	        newafr.setDy(afr.getDy());
	        newafr.setDelay(afr.getDelay());
	    }		
	}

	public HashSet<Point> getUsedFrames() {
	    HashSet<Point> usedFrames = new HashSet<Point>();
	    for (int i = 0; i < getFrameCount(); i++) {
	        PipAnimateFrame af = getFrame(i).realize();
	        for (int j = 0; j < af.getPieceCount(); j++) {
	            PipAnimateFramePiece pc = af.getPiece(j);
	            usedFrames.add(new Point(pc.getImageID(), pc.getFrame()));
	        }
	    }
	    return usedFrames;
	}
	
	public void update(PipAnimate other) {
		name = other.name;
		frames.clear();
		for (PipAnimateFrameRef ref : other.frames) {
			PipAnimateFrameRef newRef = new PipAnimateFrameRef(this);
			newRef.update(ref);
			frames.add(newRef);
		}
	}
}
