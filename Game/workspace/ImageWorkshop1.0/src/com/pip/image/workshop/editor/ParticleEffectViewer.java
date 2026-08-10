package com.pip.image.workshop.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.pip.image.workshop.Settings;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipParticle;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display particle effect.
 */
public class ParticleEffectViewer extends AbstractImageViewer implements Runnable {
	private boolean playing;
	private Thread playThread;
	private int playTimer;
	private Point refPoint = new Point(0, 0);
	private boolean isDraggingRefPoint;
	private boolean mouseInRefPointArea;
	protected Point dragStartPoint;
	private Point dragStartPos;
	private Point lastPoint;
	private int totalTime;
	private int startTick;
	private int stopTick;
	private PipAnimateSet animates;
	private Image backgroundImage;
	protected PipAnimate refAnimate;
	
	public void setAnimates(PipAnimateSet set) {
		animates = set;
	}
	
	public void setBackgroundImage(Image img) {
		backgroundImage = img;
		redraw();
	}

	public void setRefAnimate(PipAnimate input) {
		this.refAnimate = input;
	}
	
	public void setInput(PipParticle[] input, int st, int et) {
		super.setInput(input);
		if (input != null) {
			totalTime = 0;
			for (PipParticle p : input) {
				if (p.startTime + p.path.length > totalTime) {
					totalTime = p.startTime + p.path.length;
				}
			}
			if (st == -1) {
				this.startTick = 0;
			} else {
				this.startTick = st;
				if (this.startTick >= totalTime) {
					this.startTick = totalTime;
				}
			}
			if (et == -1) {
				this.stopTick = totalTime;
			} else {
				this.stopTick = et;
				if (this.stopTick >= totalTime) {
					this.stopTick = totalTime;
				}
			}
			playing = true;
			if (playThread == null) {
				playThread = new Thread(this);
				playThread.start();
			}
		} else {
			playing = false;
		}
	}
	
	public void run() {
		while (playing && input != null && playThread == Thread.currentThread()) {
			long preTime = System.currentTimeMillis();
			playTimer++;
			try {
			    Display display = getDisplay();
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                        	if(!isDisposed())
                        		redraw();
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
            	long processTime = System.currentTimeMillis() - preTime;
            	if (processTime < Settings.animateFrameDelay) {
            		Thread.sleep(Settings.animateFrameDelay - processTime);
            	}
			} catch (Exception e) {
			}
		}
	}
	
	private Point calcCoord(Point p) {
		Point size = getSize();
		int x = (int)(fitGrid(p.x - size.x / 2 - paintOffset.x) / ratio);
		int y = (int)(fitGrid(p.y - size.y / 2 - paintOffset.y) / ratio);
		return new Point(x, y);
	}
	
    private boolean inRefPointAnchor(int x, int y) {
        Point size = getSize();
        int refX = (int)(refPoint.x * ratio) + size.x / 2 + paintOffset.x;
        int refY = (int)(refPoint.y * ratio) + size.y / 2 + paintOffset.y;
        if (x >= refX - 3 && x <= refX + 3 && y >= refY - 3 && y <= refY + 3) {
            return true;
        }
        return false;
    }	
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ParticleEffectViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
			    onMouseMove(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				onMouseDown(e);
			}
			public void mouseUp(MouseEvent e) {
				onMouseUp(e);
			}
		});
	}

	protected void onMouseMove(MouseEvent e) {
		mouseInRefPointArea = inRefPointAnchor(e.x, e.y);
		if (isDraggingRefPoint) {
            int offx = (int)((e.x - dragStartPoint.x) / ratio);
            int offy = (int)((e.y - dragStartPoint.y) / ratio);
            offx += dragStartPos.x;
            offy += dragStartPos.y;
            refPoint.x = offx;
            refPoint.y = offy;
        }
		lastPoint = calcCoord(new Point(e.x, e.y));
		redraw();		
	}

	protected void onMouseUp(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
		if (e.button == 1) {
			if (isDraggingRefPoint) {
                isDraggingRefPoint = false;
                redraw();
            }
		}		
	}

	protected void onMouseDown(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
		if (e.button == 1) {
		    if (inRefPointAnchor(e.x, e.y)) {
                isDraggingRefPoint = true;
                dragStartPoint = new Point(e.x, e.y);
                dragStartPos = new Point(refPoint.x, refPoint.y);
                redraw();
                return;
            }
		}		
	}

	protected void drawInformation(GC gc) {
		super.drawInformation(gc);

		Point size = getSize();
		gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
        int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
        gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
        gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
        if (isDraggingRefPoint || mouseInRefPointArea) {
            gc.drawRectangle(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
        }

		drawScreenFrame(gc);
		
		gc.setForeground(invert(getBackground()));
		
		String coordStr;
        if (isDraggingRefPoint) {
            coordStr = refPoint.x + "," + refPoint.y;
        } else {
            return;
        }
        Point ts = gc.textExtent(coordStr);
        gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
	}

	protected void paintInput(GC gc) {
		Point size = getSize();
		if (backgroundImage != null) {
			int iw = backgroundImage.getBounds().width;
			int ih = backgroundImage.getBounds().height;
			gc.drawImage(backgroundImage, 0, 0, iw, ih, (int)(size.x / 2 - iw / 2 * ratio), 
					(int)(size.y / 2 - ih / 2 * ratio), 
					(int)(iw * ratio), (int)(ih * ratio));
		}
		if (refAnimate != null) {
			int f = refAnimate.getFrameAtTime(playTimer);
			if (f != -1) {
				PipAnimateFrameRef frameRef = refAnimate.getFrame(f);
				PipAnimateFrame frame = frameRef.realize();
				int dx = size.x / 2;
				int dy = size.y / 2;
				dx += frameRef.getDx() * ratio;
				dy += frameRef.getDy() * ratio;
				frame.draw(gc, dx + paintOffset.x, dy + paintOffset.y, ratio);
			}
		}
		if (input == null) {
			return;
		}
		if (stopTick - startTick == 0) {
			return;
		}
		
		PipParticle[] arr = (PipParticle[])input;
		int time = (playTimer % (stopTick - startTick)) + startTick;
		for (PipParticle p : arr) {
			if (p.path.length == 0) {
				continue;
			}
			if (time >= p.startTime && time < p.startTime + p.path.length) {
				int[] pos = p.path[time - p.startTime];
				if (pos[0] == -1000) {
					continue;
				}
				PipAnimate animate = animates.getAnimate(p.particleID);
				int f = animate.getFrameAtTime(time - p.startTime);
				if (f != -1) {
					PipAnimateFrameRef frameRef = animate.getFrame(f);
					PipAnimateFrame frame = frameRef.realize();
					int dx = size.x / 2;
					int dy = size.y / 2;
					dx += (frameRef.getDx() + pos[0]) * ratio;
					dy += (frameRef.getDy() + pos[1]) * ratio;
					frame.draw(gc, dx + paintOffset.x, dy + paintOffset.y, ratio);
				}
			}
		}
	}
	
	private void drawScreenFrame(GC gc) {
		Point size = getSize();
		int sw = Settings.screenWidth;
		int sh = Settings.screenHeight;
		Rectangle screen = new Rectangle(-sw / 2, -sh / 2, sw, sh);
		screen = zoom(screen);
		screen.x += size.x / 2 + paintOffset.x;
		screen.y += size.y / 2 + paintOffset.y;
		gc.setForeground(invert(gc.getBackground()));
		gc.drawRectangle(screen);
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    playing = false;
	    input = null;
	}
	
    public Point getRefPoint() {
        return refPoint;
    }
}
