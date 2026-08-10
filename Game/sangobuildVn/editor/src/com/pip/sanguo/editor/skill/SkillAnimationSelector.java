package com.pip.sanguo.editor.skill;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.text.html.ImageView;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pip.image.workshop.editor.AnimateEditor;
import com.pip.image.workshop.editor.ImageViewer;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.IMapLayer;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.MapNPC;
import com.pip.mapeditor.data.MapNPCLayer;
import com.pip.mapeditor.data.NPCImageInfo;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.Utils;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to choose animation of skill.
 */
public class SkillAnimationSelector extends AbstractImageViewer implements Runnable {
    private int[][] FRAME_MAP;
    private PipAnimateSet[] animates;
    
	private Rectangle[] frameLayout;
	private Rectangle[] frameBounds;
	private int selectedFrame;
	private int selectingFrame;

    private int hoverFrame;
	private int currentTime;
	private boolean disposed = false;
	private Display display;
	
	public void setInput(Object input) {
		super.setInput(input);
		hoverFrame = -1;
		selectedFrame = -1;
		currentTime = 0;
		frameLayout = null;
		frameBounds = null;
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SkillAnimationSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		
		animates = new PipAnimateSet[2];
		File f = new File(EditorApplication.getProj().baseDir, "client_res/effect.cts");
		List<int[]> list = new ArrayList<int[]>();
		animates[0] = new PipAnimateSet();
		try {
		    animates[0].load(f);
		    for (int i = 0; i < animates[0].getAnimateCount(); i++) {
		        list.add(new int[] { 0, i, i });
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
        f = new File(EditorApplication.getProj().baseDir, "client_res/magic.cts");
        animates[1] = new PipAnimateSet();
        try {
            animates[1].load(f);
            for (int i = 0; i < animates[1].getAnimateCount(); i++) {
                list.add(new int[] { 1, i, 100 + i });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FRAME_MAP = new int[list.size()][];
        list.toArray(FRAME_MAP);
		
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				hoverFrame = calcPointFrame(new Point(e.x, e.y));
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectingFrame && newFrame != -1) {
						setSelectedIndex(newFrame);
						fireFrameSelectionChanged(selectedFrame);
					}
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
			}
		});
		display = getDisplay();
        new Thread(this).start();
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		getBestLayout();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2;
		
		for (int i = 0; i < FRAME_MAP.length + 1; i++) {
			Rectangle rect = zoom(frameLayout[i + 1]);
			rect.x += offx + paintOffset.x;
			rect.y += offy + paintOffset.y;
			
			if (i == 0) {
			    // 第0帧是空白
			     gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			     gc.fillRectangle(rect);
			} else {
    			// 绘制动画
    			int animateX = rect.x - (int)(frameBounds[i].x * ratio);
    			int animateY = rect.y - (int)(frameBounds[i].y * ratio);
    			PipAnimate animate = animates[FRAME_MAP[i - 1][0]].getAnimate(FRAME_MAP[i - 1][1]);
    			int animateFrame = animate.getFrameAtTime(currentTime);
    			animate.drawFrame(gc, animateFrame, animateX, animateY, ratio, null);
			}
			
			// 画框
			if (i == selectedFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				gc.drawRectangle(rect);
			}
			if (i == hoverFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				gc.drawRectangle(rect);
			}
		}
	}
	
	// 计算鼠标点到的位置的动画
	private int calcPointFrame(Point p) {
        getBestLayout();
		if (frameLayout == null) {
			return -1;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2 + paintOffset.x;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2 + paintOffset.y;
		for (int i = 1; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			int dw = (int)(frameLayout[i].width * ratio);
			int dh = (int)(frameLayout[i].height * ratio);
			if (new Rectangle(dx, dy, dw, dh).contains(p)) {
				return i - 1;
			}
		}
		return -1;
	}
	
	// 计算最佳布局
	private void getBestLayout() {
	    if (frameLayout != null) {
	        return;
	    }
	    if (input == null) {
	        return;
	    }
		int count = FRAME_MAP.length + 1;
		
		// 计算所有动画占用的大小
		frameBounds = new Rectangle[count];
		for (int i = 0; i < count; i++) {
		    if (i == 0) {
		        frameBounds[i] = new Rectangle(0, 0, 20, 20);
		    } else {
		        frameBounds[i] = animates[FRAME_MAP[i - 1][0]].getAnimate(FRAME_MAP[i - 1][1]).getBounds();
		    }
		}
		
		// 计算最佳布局
		frameLayout = getBestLayout(getSize(), frameBounds);
	}
	
	public int getSelectedFrame() {
	    if (selectedFrame == 0) {
	        return -1;
	    } else {
	        return FRAME_MAP[selectedFrame - 1][2];
	    }
	}

	private void setSelectedIndex(int index) {
	    this.selectedFrame = index;
	    redraw();
	}
	
	public void setSelectedFrame(int selectedFrame) {
	    if (selectedFrame < 0) {
	        this.selectedFrame = 0;
	    } else {
	        for (int i = 0; i < FRAME_MAP.length; i++) {
	            if (FRAME_MAP[i][2] == selectedFrame) {
	                this.selectedFrame = i + 1;
	                break;
	            }
	        }
	    }
		redraw();
	}

	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (input != null) {
			switch (keyCode) {
			case SWT.ARROW_UP:
			case SWT.ARROW_LEFT:
				this.selectedFrame--;
				if (this.selectedFrame < 0) {
					this.selectedFrame = FRAME_MAP.length;
				}
				break;
			case SWT.ARROW_DOWN:
			case SWT.ARROW_RIGHT:
				this.selectedFrame++;
				if (this.selectedFrame >= FRAME_MAP.length + 1) {
					this.selectedFrame = 0;
					if (this.selectedFrame >= FRAME_MAP.length + 1) {
						this.selectedFrame = -1;
					}
				}
				break;
			default:
				break;
			}
			redraw();
		}
	}

	/**
	 * 动画播放到下一帧。
	 */
	public void step() {
	    currentTime++;
	}

    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        this.disposed = true;
    }
    
    public void run() {
        while (!disposed) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            step();
            if (disposed) {
                break;
            }
            display.asyncExec(new Runnable() {
                public void run() {
                    redraw();
                }
            });
        }
    }
}
