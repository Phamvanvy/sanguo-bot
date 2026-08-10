package com.pip.image.workshop.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import com.pip.image.workshop.WorkshopApplication;
import com.pip.image.workshop.WorkshopPlugin;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display animation. The widget has two modes: editing and playing.
 * In editing mode, user can drag current frame; in playing mode, the frames is played
 * in sequence and user can't drag them.
 */
public class AnimateViewer extends AbstractImageViewer implements Runnable {
	protected int currentFrame;
	protected boolean isDragging;
	protected Point dragStartPoint;
	private Point dragStartPos;
	private Point lastPoint;
	private boolean playing;
	private Thread playThread;
	private int playTimer;
	private int[] visibleFrames;
	private Point refPoint = new Point(0, 0);
	private boolean isDraggingRefPoint;
	private boolean mouseInRefPointArea;
	protected PipAnimate refAnimate;
	
	public void setInput(PipAnimate input) {
		super.setInput(input);
		currentFrame = -1;
		isDragging = false;
		this.stop();
		visibleFrames = new int[0];
	}
	
	public void setRefAnimate(PipAnimate input) {
		this.refAnimate = input;
	}
	
	public void setVisibleFrames(int[] arr) {
		visibleFrames = arr;
	}
	
	public void setCurrentFrame(int frame) {
		currentFrame = frame;
		isDragging = false;
	}
	
	public void play() {
		if (input == null || ((PipAnimate)input).getFrameCount() == 0) {
			return;
		}
		setInput(input);
		playing = true;
		playTimer = 0;
		playThread = new Thread(this);
		playThread.start();
	}
	
	public void stop() {
		playing = false;
		try {
			playThread.join();
		} catch (Exception e) {
		}
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public void run() {
		while (playing && input != null && playThread == Thread.currentThread()) {
			long preTime = System.currentTimeMillis();
			currentFrame = ((PipAnimate)input).getFrameAtTime(playTimer);
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
            	if(processTime < 80) {
            		Thread.sleep(80 - processTime);
            	}
//            	Thread.sleep(100);
				
			} catch (Exception e) {
			}
		}
	}
	
	protected PipAnimateFrameRef getFrameData() {
		return ((PipAnimate)input).getFrame(currentFrame);
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
	public AnimateViewer(Composite parent, int style) {
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
		if (playing) {
			return;
		}
		mouseInRefPointArea = inRefPointAnchor(e.x, e.y);
		if (isDragging && input != null && currentFrame != -1) {
			int offx = (int)((e.x - dragStartPoint.x) / ratio);
			int offy = (int)((e.y - dragStartPoint.y) / ratio);
			offx += dragStartPos.x;
			offy += dragStartPos.y;
			getFrameData().setDx(offx);
			getFrameData().setDy(offy);
		} else if (isDraggingRefPoint) {
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
		if (playing) {
			return;
		}
		if (e.button == 1) {
			if (isDragging && input != null && currentFrame != -1) {
				isDragging = false;
				redraw();
				int offx = (int)((e.x - dragStartPoint.x) / ratio);
				int offy = (int)((e.y - dragStartPoint.y) / ratio);
				if (offx != 0 || offy != 0) {
					fireContentChanged();
				}
			}
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
		if (playing) {
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
		    if (input != null && currentFrame != -1) {
				isDragging = true;
				dragStartPoint = new Point(e.x, e.y);
				dragStartPos = new Point(getFrameData().getDx(), getFrameData().getDy()); 
				redraw();
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
        if (isDraggingRefPoint || (mouseInRefPointArea && !isDragging && !playing)) {
            gc.drawRectangle(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
        }

		if (input == null || currentFrame == -1) {
			drawScreenFrame(gc);
			return;
		} else {
			if (!playing) {
				drawSelection(gc);
				drawScreenFrame(gc);
			} else {
				drawScreenFrame(gc);
				return;
			}
		}
		
		gc.setForeground(invert(getBackground()));
		PipAnimateFrameRef frameRef = getFrameData();
		if (frameRef != null) {
			PipAnimateFrame frame = frameRef.realize();
			if (frame == null) {
				return;
			}
			Rectangle fb = frame.getBounds();
			fb.x += frameRef.getDx();
			fb.y += frameRef.getDy();
			String coordStr = fb.x + "," + fb.y + "," + fb.width + "," + fb.height;
			Point ts = gc.textExtent(coordStr);
			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
            gc.drawText(coordStr, 4, size.y - ts.y - 5);
		}
		
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
		if (input == null) {
			return;
		}
		// 先画背景帧
		int dx, dy;
		PipAnimateFrameRef frameRef;
		PipAnimateFrame frame;
		if (!isPlaying()) {
			for (int i = 0; i < visibleFrames.length; i++) {
				PipAnimate ani = (PipAnimate)input;
				if (visibleFrames[i] >= ani.getFrameCount()) {
					continue;
				}
				
				if (refAnimate != null) {
					int t = ani.getTimeOfFrame(visibleFrames[i]);
					int f = refAnimate.getFrameAtTime(t);
					if (f != -1) {
						frameRef = refAnimate.getFrame(f);
						frame = frameRef.realize();
						dx = size.x / 2;
						dy = size.y / 2;
						dx += frameRef.getDx() * ratio;
						dy += frameRef.getDy() * ratio;
						frame.draw(gc, dx + paintOffset.x, dy + paintOffset.y, ratio);
					}
				}
				
				frameRef = ani.getFrame(visibleFrames[i]);
				frame = frameRef.realize();
				dx = size.x / 2;
				dy = size.y / 2;
				dx += frameRef.getDx() * ratio;
				dy += frameRef.getDy() * ratio;
				frame.draw(gc, dx + paintOffset.x, dy + paintOffset.y, ratio);
			}
		}
		if (currentFrame != -1) {
			// 最后画选中帧
			
			if (refAnimate != null) {
				int t = ((PipAnimate)input).getTimeOfFrame(currentFrame);
				int f = refAnimate.getFrameAtTime(t);
				if (f != -1) {
					frameRef = refAnimate.getFrame(f);
					frame = frameRef.realize();
					dx = size.x / 2;
					dy = size.y / 2;
					dx += frameRef.getDx() * ratio;
					dy += frameRef.getDy() * ratio;
					frame.draw(gc, dx + paintOffset.x, dy + paintOffset.y, ratio);
				}
			}
			
			frameRef = getFrameData();
			frame = frameRef.realize();
			dx = size.x / 2;
			dy = size.y / 2;
			dx += frameRef.getDx() * ratio;
			dy += frameRef.getDy() * ratio;
			frame.draw(gc, dx + paintOffset.x, dy + paintOffset.y, ratio);
		}
	}
	
	private void drawScreenFrame(GC gc) {
		Point size = getSize();
		int sw = WorkshopPlugin.screenWidth;
		int sh = WorkshopPlugin.screenHeight;
		Rectangle screen = new Rectangle(-sw / 2, -sh / 2, sw, sh);
		screen = zoom(screen);
		screen.x += size.x / 2 + paintOffset.x;
		screen.y += size.y / 2 + paintOffset.y;
		gc.setForeground(invert(gc.getBackground()));
		gc.drawRectangle(screen);
	}
	
	protected void drawSelection(GC gc) {
		Point size = getSize();
		PipAnimateFrameRef frameRef = getFrameData();
		PipAnimateFrame frame = frameRef.realize();
		int dx = size.x / 2;
		int dy = size.y / 2;
		dx += frameRef.getDx() * ratio;
		dy += frameRef.getDy() * ratio;
		Rectangle frameRec = zoom(frame.getBounds());
		gc.setForeground(getDisplay().getSystemColor(isDragging ? SWT.COLOR_RED : SWT.COLOR_BLUE));
		gc.drawRectangle(frameRec.x + dx + paintOffset.x, frameRec.y + dy + paintOffset.y, frameRec.width, frameRec.height);
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    if (playing) {
	        stop();
	    }
	    input = null;
	}
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (isDragging || input == null || currentFrame == -1) {
			return;
		}
		PipAnimateFrameRef ref = ((PipAnimate)input).getFrame(currentFrame);
		switch (keyCode) {
		case SWT.ARROW_UP:
			ref.setDy(ref.getDy() - 1);
			break;
		case SWT.ARROW_DOWN:
			ref.setDy(ref.getDy() + 1);
			break;
		case SWT.ARROW_LEFT:
			ref.setDx(ref.getDx() - 1);
			break;
		case SWT.ARROW_RIGHT:
			ref.setDx(ref.getDx() + 1);
			break;
		default:
			return;
		}
		redraw();
		fireContentChanged();
	}

    public Point getRefPoint() {
        return refPoint;
    }
}
