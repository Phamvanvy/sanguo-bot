package com.pip.image.workshop.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.png.PngEncoder;

/**
 * A widget to display all frames in an animate. User can double click one frame to add 
 * it into current animate.
 */
public class AnimateFrameSelector extends AbstractImageViewer {
	// 多帧模式的变量
	private Point[] frameLayout;
	private int selectedFrame;
	private List<Integer> selectedFrames = new ArrayList<Integer>();
	private int selectingFrame;
	private Image mapBuffer;
	private Menu popMenu;
	public boolean needDrawTip = true;
	public boolean needMenu = true;
	
	public void setInput(Object input) {
		super.setInput(input);
		selectedFrame = -1;
		selectedFrames.clear();
		if(needMenu){
			setMenu(popMenu);
		}
	}

    public void zoomin() {
        if (ratio < 64) {
            ratio *= 2;
            refresh();
        }
    }
    
    public void zoomout() {
        if (ratio > 0.125) {
            ratio /= 2;
            refresh();
        }
    }

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateFrameSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectingFrame && newFrame != -1) {
						if ((e.stateMask & SWT.SHIFT) != 0) {
							setSelectedFrame(newFrame, 1);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							setSelectedFrame(newFrame, 2);
						} else {
							setSelectedFrame(newFrame, 0);
						}
						fireFrameSelectionChanged(selectedFrame);
					}
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectedFrame) {
						fireFrameDoubleClicked(selectedFrame);
					}
				}
			}
		});
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("导出...") {
			public void run() {
				exportSelection();
			}
		});
		mgr.add(new Action("导出全部...") {
			public void run() {
				exportAll();
			}
		});
		if (popMenu != null) {
			popMenu.dispose();
		}
		popMenu = mgr.createContextMenu(this);
	}

    protected void createMapBuffer() {
        PipAnimateSet animate = (PipAnimateSet)input;
        Point[] pos = getBestLayout(animate);
        frameLayout = pos;
        if (pos == null) {
            return;
        }
        int mapw = (int)(pos[0].x * ratio);
        int maph = (int)(pos[0].y * ratio);
        mapBuffer = new Image(getDisplay(), mapw, maph);
        GC gc = new GC(mapBuffer);
        
        gc.setBackground(this.getBackground());
        gc.fillRectangle(0, 0, mapw, maph);
        
        for (int i = 0; i < animate.getFrameCount(); i++) {
            PipAnimateFrame fr = animate.getFrame(i);
            int dx = (int)(pos[i + 1].x * ratio);
            int dy = (int)(pos[i + 1].y * ratio);
            Rectangle frameRec = zoom(fr.getBounds());
            fr.draw(gc, dx - frameRec.x, dy - frameRec.y, ratio);
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
            gc.drawRectangle(dx, dy, frameRec.width, frameRec.height);
        }
        gc.dispose();
    }
   
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
		} else {
		    if (mapBuffer == null) {
		        createMapBuffer();
		    }
		    if (mapBuffer == null) {
		        return;
		    }
			int offx = (size.x - mapBuffer.getBounds().width) / 2;
			int offy = (size.y - mapBuffer.getBounds().height) / 2;
		    gc.drawImage(mapBuffer, offx + paintOffset.x, offy + paintOffset.y);
		}
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		PipAnimateSet animate = (PipAnimateSet)input;
		if (input == null) {
		} else {
		    Point[] pos = frameLayout;
//			Point[] pos = getBestLayout(animate);
//			frameLayout = pos;
			if (pos != null) {
				int offx = (int)(size.x - pos[0].x * ratio) / 2;
				int offy = (int)(size.y - pos[0].y * ratio) / 2;
				for (int i = 0; i < animate.getFrameCount(); i++) {
					PipAnimateFrame fr = animate.getFrame(i);
					int dx = (int)(pos[i + 1].x * ratio) + offx;
					int dy = (int)(pos[i + 1].y * ratio) + offy;
					Rectangle frameRec = zoom(fr.getBounds());
					if (selectedFrames.contains(i)) {
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
						gc.drawRectangle(dx + paintOffset.x, dy + paintOffset.y, frameRec.width, frameRec.height);
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
					} else {
//						gc.drawRectangle(dx + paintOffset.x, dy + paintOffset.y, frameRec.width, frameRec.height);
					}
				}
			}
		}

		gc.setForeground(invert(getBackground()));
		if (input != null && selectedFrame != -1 && needDrawTip) {
			String str = "(双击添加)";
			Point ts = gc.textExtent(str);
			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(str, 4, size.y - ts.y - 5);
		}
		
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		String str = "索引：" + String.valueOf(selectedFrame);
        Point ts = gc.textExtent(str);
        gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(str, 4, size.y - ts.y - 5);
        
        str = "按住SHIFT/CTRL可以多选";
        ts = gc.textExtent(str);
        gc.drawRectangle(size.x - ts.x - 8, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(str, size.x - ts.x - 5, size.y - ts.y - 5);
	}
	
	private int calcPointFrame(Point p) {
		PipAnimateSet animate = (PipAnimateSet)input;
		if (frameLayout == null) {
			return -1;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].x * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].y * ratio) / 2;
		for (int i = 1; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			PipAnimateFrame fr = animate.getFrame(i - 1);
			Rectangle frameRec = zoom(fr.getBounds());
			frameRec.x = dx + paintOffset.x;
			frameRec.y = dy + paintOffset.y;
			if (frameRec.contains(p)) {
				return i - 1;
			}
		}
		return -1;
	}
	
	private Point[] getBestLayout(PipAnimateSet animate) {
		if (animate.getFrameCount() == 0) {
			return null;
		}
		
		int w = 0, h = 0;
		int count = animate.getFrameCount();
		for (int i = 0; i < count; i++) {
			PipAnimateFrame data = animate.getFrame(i);
			Rectangle rect = data.getBounds();
			w += rect.width + 2;
			if (rect.height + 2 > h) {
				h = rect.height + 2;
			}
		}
		if (w / h > 3) {
			w = (int)(w / Math.sqrt(w / (h * 3)));
		}
		Point[] ret = new Point[count + 1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new Point(0, 0);
		}
		int rw = 0, lh = 0, dx = 0, dy = 0;
		for (int i = 0; i < count; i++) {
			PipAnimateFrame data = animate.getFrame(i);
			Rectangle rect = data.getBounds();
			if (dx != 0 && dx + rect.width + 2 > w) {
				dx = 0;
				dy += lh;
				lh = 0;
				i--;
				continue;
			} else {
				ret[i + 1].x = dx;
				ret[i + 1].y = dy;
				dx += rect.width + 2;
				if (lh < rect.height + 2) {
					lh = rect.height + 2;
				}
				if (dx > rw) {
					rw = dx;
				}
			}
		}
		ret[0].x = rw;
		ret[0].y = dy + lh;
		return ret;
	}
	
	private boolean isFrameSelectionAllowed() {
		return input != null;
	}

	public int getSelectedFrame() {
		return selectedFrame;
	}

	public void setSelectedFrame(int selectedFrame) {
		this.selectedFrame = selectedFrame;
		selectedFrames.clear();
		if (selectedFrame != -1) {
			selectedFrames.add(selectedFrame);
		}
		redraw();
	}
	
	/*
	 * optype = 0，改变frame
	 * optype = 1，SHIFT选择
	 * optype = 2，CTRL选择
	 */
	protected void setSelectedFrame(int frame, int optype) {
		if (optype == 0) {
			setSelectedFrame(frame);
		} else if (optype == 1) {
			if (selectedFrames.size() == 0) {
				setSelectedFrame(frame);
			} else {
				int startFrame = selectedFrames.get(0);
				selectedFrames.clear();
				if (startFrame <= frame) {
					for (int f = startFrame; f <= frame; f++) {
						selectedFrames.add(f);
					}
				} else {
					for (int f = startFrame; f >= frame; f--) {
						selectedFrames.add(f);
					}
				}
				this.selectedFrame = frame;
				redraw();
			}
		} else {
			this.selectedFrame = frame;
			selectedFrames.add(frame);
			redraw();
		}
	}

	public void refresh() {
	    if (mapBuffer != null) {
	        mapBuffer.dispose();
	    }
        mapBuffer = null;
        redraw();
    }
	
	public void widgetDisposed(DisposeEvent evt) {
	    super.widgetDisposed(evt);
	    if (mapBuffer != null) {
            mapBuffer.dispose();
        }
	    if (popMenu != null) {
	    	popMenu.dispose();
	    }
	}
	
	public void exportSelection() {
		if (selectedFrame == -1) {
			return;
		}
		
		// 选择文件
		FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		dlg.setFilterExtensions(new String[] { "*.png" });
		dlg.setFilterNames(new String[] { "PNG图片文件(*.png)" });
		String file = dlg.open();
		if (file == null) {
			return;
		}
		
		// 到处一帧
		PipAnimateSet animateSet = (PipAnimateSet)input;
		PipAnimateFrame af = animateSet.getFrame(selectedFrame);
		try {
			exportOneFrame(af, new File(file));
	        MessageDialog.openInformation(getShell(), "导出", "导出成功！");
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "错误", e.toString());
		}
	}
	
	private void exportOneFrame(PipAnimateFrame frame, File f) throws Exception {
		Rectangle size = frame.getBounds();
		Image img = new Image(getShell().getDisplay(), size.width, size.height);
		GC gc = new GC(img);
		frame.draw(gc, -size.x, -size.y, 1.0f);
		gc.dispose();
		
		// 保存文件
		try {
	        PngEncoder enc = new PngEncoder(img);
	        FileOutputStream fos = new FileOutputStream(f);
	        enc.encode32(fos, false);
	        fos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			img.dispose();
		}
	}
	
	public void exportAll() {
		// 选择文件
		DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.SAVE);
		String path = dlg.open();
		if (path == null) {
			return;
		}
		
		// 创建内存图片
		PipAnimateSet animateSet = (PipAnimateSet)input;
		try {
			for (int i = 0; i < animateSet.getFrameCount(); i++) {
				PipAnimateFrame af = animateSet.getFrame(i);
				exportOneFrame(af, new File(path, "frame" + i + ".png"));
			}
	        MessageDialog.openInformation(getShell(), "导出", "导出成功！");
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "错误", e.toString());
		}
	}
	
	public int[] getSelectedFrames() {
		int[] ret = new int[selectedFrames.size()];
		for (int i = 0; i < selectedFrames.size(); i++) {
			ret[i] = selectedFrames.get(i);
		}
		return ret;
	}
}
