package com.pip.image.workshop.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;

import com.pip.image.workshop.AutoBody;
import com.pip.image.workshop.GenericChooseDialog;
import com.pip.image.workshop.Settings;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.SWTUtils;
import com.pipimage.data.ImageDescription;
import com.pipimage.data.TileInfo2;
import com.pipimage.image.ColorsExceedException;
import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;

/**
 * A widget to display image. The image may be a PipImage, a PipImageDraw or a Image.
 * When input is PipImage, all frames in the image are shown; when input is 
 * PipImageDraw, only the frame specified by the object is shown; when input is Image,
 * the whole image is shown and region selection function is actived.
 */
public class ImageViewer extends AbstractImageViewer implements IFileModificationListener {
	public static Cursor cursorCross = new Cursor(Display.getCurrent() ,SWT.CURSOR_CROSS);
	public static Cursor cursorArrow = new Cursor(Display.getCurrent() ,SWT.CURSOR_ARROW);
	public static Cursor cursorMove = new Cursor(Display.getCurrent() ,SWT.CURSOR_SIZEALL);
	public static Cursor cursorSizeHorizontal = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZEWE);
	public static Cursor cursorSizeVertical = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZENS);
	public static Cursor cursorSizeNESW = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZENESW);
	public static Cursor cursorSizeNWSE = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZENWSE);

	private boolean editable = true;		// 是否允许编辑
	
	private Point startPoint, endPoint;
	private boolean isSelecting;
	private Rectangle selectedArea;
	private Point lastPoint;
	private boolean isDraggingSelection;
	private int dragAnchor;
	private Rectangle dragStartArea;
	private Point dragStartPoint;
	
	// 多帧模式的变量
	private Point[] frameLayout;
	private int selectedFrame;
	private List<Integer> selectedFrames = new ArrayList<Integer>();
	private int selectingFrame;
	private boolean flatMode;
	private Menu popMenu;
	private Map<Integer, Integer> frameMap;
	private boolean flatModeWarned;
	
	// 切割模式的变量
	private ArrayList<Rectangle> cutAreas = null;
	
	// 用系统编辑器编辑中的图块
	class EditingFrame {
		File file;
		int frame;
	}
	private ArrayList<EditingFrame> editingFrames;
	
	// pip模式下缓存图片
	private Image pipBufferImg;
	
	// 如果刚刚执行了一个拆分图块操作，这个保存拆分方案
	public int[][] splitPlan;
	public int splitOriginalWidth;
	public int splitOriginalHeight;
	
	// 当前编辑的动画文件
	public PipAnimateSet currentAnimate;
	
	// opengl模式下缓存图片
	private ArrayList<Image> bufferImages = new ArrayList<Image>();
	private ImageDrawCache cache = null;
	
	public void setInput(Object input) {
		super.setInput(input);
		clearBuffer();
		selectedArea = null;
		isDraggingSelection = false;
		isSelecting = false;
		selectedFrame = -1;
		selectedFrames.clear();
		setMenu(null);
		if (input instanceof Image) {
			setCursor(cursorCross);
		} else {
			setCursor(cursorArrow);
		}
		cutAreas = new ArrayList<Rectangle>();
		editingFrames = new ArrayList<EditingFrame>();
		flatModeWarned = false;
		
		// 创建弹出菜单
        MenuManager mgr = new MenuManager();
        
        mgr.add(new Action("前移") {
            public void run() {
                onMoveUp();
            }
        });
        mgr.add(new Action("后移") {
            public void run() {
                onMoveDown();
            }
        });
        mgr.add(new Action("复制") {
            public void run() {
                onDuplicateFrame();
            }
        });
        mgr.add(new Action("水平翻转") {
            public void run() {
                onHflipFrame();
            }
        });
        mgr.add(new Action("垂直翻转") {
            public void run() {
                onVflipFrame();
            }
        });
        mgr.add(new Action("删除") {
            public void run() {
                onDeleteFrame();
            }
        });
        mgr.add(new Separator());
        mgr.add(new Action("用系统编辑器编辑") {
            public void run() {
                onEditFrame();
            }
        });
        mgr.add(new Action("导出所有帧") {
            public void run() {
                onExportAll();
            }
        });
        mgr.add(new Separator());
        mgr.add(new Action("导出所有帧（合并）") {
            public void run() {
                onExportAll2();
            }
        });
        mgr.add(new Action("从合并图片导入所有帧") {
            public void run() {
                onImportAll();
            }
        });
        if (input instanceof LandformImage) {
            mgr.add(new Separator());
            mgr.add(new Action("设置地形块...") {
                public void run() {
                    onEditLandformFrame();
                }
            });
        }
        if (flatMode) {
        	mgr.add(new Action("拆分图块...") {
                public void run() {
                    onSplitImage();
                }
            });
        	mgr.add(new Action("拆分图块（固定大小）...") {
                public void run() {
                    onSplitImageFixSize();
                }
            });
        	if (listenerEx != null) {
	        	mgr.add(new Action("移动到...") {
	        		public void run() {
	        			fireTransferFrame();
	        		}
	        	});
        	}
        }
		if (popMenu != null) {
			popMenu.dispose();
		}
		if (editable) {
			popMenu = mgr.createContextMenu(this);
		} else {
			popMenu = null;
		}
    }
	
	public void setImageCache(ImageDrawCache cache) {
		this.cache = cache;
	}
	
	/**
	 * 设置是否允许编辑。
	 * @param value
	 */
	public void setEditable(boolean value) {
		editable = value;
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ImageViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND/*, GLUtils.glEnabled*/);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (isSelectionAllowed()) {
					Point pt = new Point(e.x, e.y);
					pt = calcCoord(pt);
					if (!isDraggingSelection) {
						if (isSelecting) {
							setCursor(cursorCross);
						} else {
							setCursor(getCursorOfAnchor(getDragAnchor(e.x, e.y)));
						}
					}
					lastPoint = pt;
					if (isSelecting) {
						endPoint = new Point(e.x, e.y);
						selectedArea = calcSelection();
					}
					if (isDraggingSelection) {
						Point dragNow = new Point(e.x, e.y);
						selectedArea = dragSelection(dragStartArea, dragStartPoint, dragNow);
						normalize(selectedArea);
					}
					redraw();
				}
				if (input != null && input instanceof PipImageDraw) {
					Point pt = new Point(e.x, e.y);
					pt = calcCoord(pt);
					lastPoint = pt;
					redraw();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isSelectionAllowed()) {
					Point pt = new Point(e.x, e.y);
					int anchor = getDragAnchor(e.x, e.y);
					if (anchor >= 0) {
						isDraggingSelection = true;
						dragAnchor = anchor;
						dragStartArea = selectedArea;
						dragStartPoint = new Point(e.x, e.y);
						return;
					}
					startPoint = pt;
					endPoint = startPoint;
					selectedArea = calcSelection();
					isSelecting = true;
					redraw();
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isSelectionAllowed()) {
					if (isSelecting) {
						endPoint = new Point(e.x, e.y);
						selectedArea = calcSelection();
						isSelecting = false;
					}
					if (isDraggingSelection) {
						isDraggingSelection = false;
						if (selectedArea != null) {
							selectedArea.intersect(((Image)input).getBounds());
							if (selectedArea.isEmpty()) {
								selectedArea = null;
							}
						}
					}
					redraw();
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectingFrame && newFrame != -1) {
						if ((e.stateMask & SWT.CTRL) != 0) {
							addSelectedFrame(newFrame);
						} else if ((e.stateMask & SWT.SHIFT) != 0) {
							addSelectedFrameTo(newFrame);
						} else {
							setSelectedFrame(newFrame);
						}
						fireFrameSelectionChanged(selectedFrame);
					}
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					Point pt = new Point(e.x, e.y);
					if (selectedArea != null && selectedArea.contains(calcCoord(pt))) {
						Rectangle rect = new Rectangle(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);
						cutAreas.add(rect);
						fireAreaSelected();
						redraw();
					}
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectedFrame) {
						fireFrameDoubleClicked(selectedFrame);
					}
				}
			}
		});
		setCursor(cursorArrow);
	}
	
	public static Cursor getCursorOfAnchor(int anchor) {
		switch (anchor) {
		case -1:
			return cursorCross;
		case 0:
			return cursorMove;
		case 1:
		case 3:
			return cursorSizeHorizontal;
		case 2:
		case 4:
			return cursorSizeVertical;
		case 5:
		case 7:
			return cursorSizeNWSE;
		case 6:
		case 8:
			return cursorSizeNESW;
		default:
			return cursorCross;
		}
	}
	
	// Compute the drag anchor. The return value may be one of the following:
	// -1 : no anchor
	//  0 : center
	//  1 : west edge
	//  2 : north edge;
	//  3 : east edge
	//  4 : south edge
	//  5 : north-west corner
	//  6 : north-east corner
	//  7 : south-east corner
	//  8 : south-west corner
	private int getDragAnchor(int x, int y) {
		if (isSelecting || !isSelectionAllowed() || selectedArea == null) {
			return -1;
		}
		Image img = (Image)input;
		Rectangle imgSize = zoom(img.getBounds());
		Point size = getSize();
		Rectangle area = zoom(selectedArea);
		area.x += (size.x - imgSize.width) / 2 + paintOffset.x;
		area.y += (size.y - imgSize.height) / 2 + paintOffset.y;
		
		// check north west corner
		if (new Rectangle(area.x - 3, area.y - 3, 6, 6).contains(x, y)) {
			return 5;
		}
		// check north east corner
		if (new Rectangle(area.x + area.width - 3, area.y - 3, 6, 6).contains(x, y)) {
			return 6;
		}
		// check south east corner
		if (new Rectangle(area.x + area.width - 3, area.y + area.height - 3, 6, 6).contains(x, y)) {
			return 7;
		}
		// check south west corner
		if (new Rectangle(area.x - 3, area.y + area.height - 3, 6, 6).contains(x, y)) {
			return 8;
		}
		// check west edge
		if (new Rectangle(area.x - 3, area.y, 6, area.height).contains(x, y)) {
			return 1;
		}
		// check north edge
		if (new Rectangle(area.x, area.y - 3, area.width, 6).contains(x, y)) {
			return 2;
		}
		// check east edge
		if (new Rectangle(area.x + area.width - 3, area.y, 6, area.height).contains(x, y)) {
			return 3;
		}
		// check south edge
		if (new Rectangle(area.x, area.y + area.height - 3, area.width, 6).contains(x, y)) {
			return 4;
		}
		// check center
		if (area.contains(x, y)) {
			return 0;
		}
		return -1;
	}

	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		
		if (input == null) {
		} else if (input instanceof Image) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawRectangle(drawX - 1 + paintOffset.x, drawY - 1 + paintOffset.y, zoomSize.width + 1, zoomSize.height + 1);
			drawCutAreas(gc);
			drawSelection(gc);
		} else if (input instanceof PipImage) {
			PipImage img = (PipImage)input;
			Point[] pos = getBestLayout(img, false);
			frameLayout = pos;
			if (pos == null) {
				return;
			}
			int offx = (int)(size.x - pos[0].x * ratio) / 2;
			int offy = (int)(size.y - pos[0].y * ratio) / 2;
			int startPalette, endPalette;
			int paletteCount = img.getImagePalettes().size();
			if (flatMode) {
				startPalette = 0;
				endPalette = paletteCount;
			} else {
				startPalette = img.getPaletteIndex();
				endPalette = startPalette + 1;
			}
			for (int pp = startPalette; pp < endPalette; pp++) {
				img.setPaletteIndex(pp);
				for (int i = 0; i < img.getImgCount(); i++) {
					int posIndex = i + (pp - startPalette) * img.getImgCount() + 1;
					int dx = (int)(pos[posIndex].x * ratio) + offx;
					int dy = (int)(pos[posIndex].y * ratio) + offy;
					PipImageData frameData = img.getImageData(i);
					int dw = (int)(frameData.getWidth() * ratio);
					int dh = (int)(frameData.getHeight() * ratio);
					if (selectedFrames.contains(posIndex - 1)) {
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
						gc.drawRectangle(dx + paintOffset.x, dy + paintOffset.y, dw, dh);
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
					} else {
						gc.drawRectangle(dx + paintOffset.x, dy + paintOffset.y, dw, dh);
					}
				}
			}
		} else if (input instanceof PipImageDraw) {
			Rectangle imgSize = ((PipImageDraw)input).getBounds(0);
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawRectangle(drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
		}

		gc.setForeground(invert(getBackground()));
		if (lastPoint != null) {
			String coordStr = lastPoint.x + "," + lastPoint.y;
			Point ts = gc.textExtent(coordStr);
			gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
		}
		if (selectedArea != null) {
			String selStr = selectedArea.x + "," + selectedArea.y + "," + selectedArea.width + "," + selectedArea.height;
			if (!isSelecting && this.listener != null) {
				selStr += "(双击添加)";
			}
			Point ts = gc.textExtent(selStr);
			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(selStr, 4, size.y - ts.y - 5);
		}
//		if (flatMode && input != null && input instanceof PipImage && selectedFrame != -1) {
//			String str = "(双击添加)";
//			Point ts = gc.textExtent(str);
//			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
//			gc.drawText(str, 4, size.y - ts.y - 5);
//		}
		if (input != null && input instanceof PipImage) {
			int mem = ((PipImage)input).getEstimateMemory();
			String str = "图片面积: " + mem + " 内存占用: 最大" + getMemStr(mem * 4 * ((PipImage)input).getImagePalettes().size());
			Point ts = gc.textExtent(str);
			gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(str, size.x - ts.x - 5, size.y - ts.y - 5);
			
			str = "图片索引：" + String.valueOf(selectedFrame);
			if (currentAnimate != null) {
				int index = currentAnimate.findImage((PipImage)input);
				if (index != -1) {
					String source = currentAnimate.getAutoFetchImageInfo().getSourceFile(currentAnimate.getFileName(index), selectedFrame);
					if (source != null) {
						str += "    来自：" + source;
					}
				}
			}
            ts = gc.textExtent(str);
            gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
            gc.drawText(str, 4, size.y - ts.y - 5);
		}
		if (input != null && input instanceof PipImageDraw) {
			Image img = ((PipImageDraw)input).createSWTImage(getDisplay(), 0);
			String str = img.getBounds().width + ", " + img.getBounds().height;
			img.dispose();
			Point ts = gc.textExtent(str);
			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(str, 4, size.y - ts.y - 5);
		}
		
		// 编辑PIP图片时，可以快捷键调整帧顺序
		if (input != null && input instanceof PipImage && this.editable) {
			String info2 = "SHFIT+左  -  前移   SHIFT+右  -  后移";
	        Point ts = gc.textExtent(info2);
	        gc.drawText(info2, size.x / 2 - ts.x / 2, size.y - ts.y * 2 - 5);
		}
	}
	
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		
		if (input == null) {
		} else if (input instanceof Image) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawRect(drawX - 1 + paintOffset.x, drawY - 1 + paintOffset.y, zoomSize.width + 1, zoomSize.height + 1);
			drawCutAreas(gc);
			drawSelection(gc);
		} else if (input instanceof PipImage) {
			PipImage img = (PipImage)input;
			Point[] pos = getBestLayout(img, false);
			frameLayout = pos;
			if (pos == null) {
				return;
			}
			int offx = (int)(size.x - pos[0].x * ratio) / 2;
			int offy = (int)(size.y - pos[0].y * ratio) / 2;
			int startPalette, endPalette;
			int paletteCount = img.getImagePalettes().size();
			if (flatMode) {
				startPalette = 0;
				endPalette = paletteCount;
			} else {
				startPalette = img.getPaletteIndex();
				endPalette = startPalette + 1;
			}
			for (int pp = startPalette; pp < endPalette; pp++) {
				img.setPaletteIndex(pp);
				for (int i = 0; i < img.getImgCount(); i++) {
					int posIndex = i + (pp - startPalette) * img.getImgCount() + 1;
					int dx = (int)(pos[posIndex].x * ratio) + offx;
					int dy = (int)(pos[posIndex].y * ratio) + offy;
					PipImageData frameData = img.getImageData(i);
					int dw = (int)(frameData.getWidth() * ratio);
					int dh = (int)(frameData.getHeight() * ratio);
					if (selectedFrames.contains(posIndex - 1)) {
						Color c = getDisplay().getSystemColor(SWT.COLOR_RED);
						gc.setColor(0xFF, c.getRed(), c.getGreen(), c.getBlue());
						gc.drawRect(dx + paintOffset.x, dy + paintOffset.y, dw, dh);
						c = getDisplay().getSystemColor(SWT.COLOR_BLACK);
						gc.setColor(0xFF, c.getRed(), c.getGreen(), c.getBlue());
					} else {
						gc.drawRect(dx + paintOffset.x, dy + paintOffset.y, dw, dh);
					}
				}
			}
		} else if (input instanceof PipImageDraw) {
			Rectangle imgSize = ((PipImageDraw)input).getBounds(0);
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawRect(drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
		}

		Color c = invert(getBackground());
		gc.setColor(0xFF, c.getRed(), c.getGreen(), c.getBlue());
		if (lastPoint != null) {
			String coordStr = lastPoint.x + "," + lastPoint.y;
			int tw = gc.stringWidth(coordStr);
			int th = gc.getFontHeight();
			gc.drawRect(size.x - tw - 9, size.y - th - 8, tw + 7, th + 6);
			gc.drawString(coordStr, size.x - tw - 5, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
		}
		if (selectedArea != null) {
			String selStr = selectedArea.x + "," + selectedArea.y + "," + selectedArea.width + "," + selectedArea.height;
			if (!isSelecting && this.listener != null) {
				selStr += "(双击添加)";
			}
			int tw = gc.stringWidth(selStr);
			int th = gc.getFontHeight();
			gc.drawRect(1, size.y - th - 8, tw + 7, th + 6);
			gc.drawString(selStr, 4, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
		}
//		if (flatMode && input != null && input instanceof PipImage && selectedFrame != -1) {
//			String str = "(双击添加)";
//			int tw = gc.stringWidth(str);
//			int th = gc.getFontHeight();
//			gc.drawRect(1, size.y - th - 8, tw + 7, th + 6);
//			gc.drawString(str, 4, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
//		}
		if (input != null && input instanceof PipImage) {
			int mem = ((PipImage)input).getEstimateMemory();
			String str = "图片面积: " + mem + " 内存占用: 最大" + getMemStr(mem * 4 * ((PipImage)input).getImagePalettes().size());
			int tw = gc.stringWidth(str);
			int th = gc.getFontHeight();
			gc.drawRect(size.x - tw - 9, size.y - th - 8, tw + 7, th + 6);
			gc.drawString(str, size.x - tw - 5, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
			
			str = "图片索引：" + String.valueOf(selectedFrame);
			if (currentAnimate != null) {
				int index = currentAnimate.findImage((PipImage)input);
				if (index != -1) {
					String source = currentAnimate.getAutoFetchImageInfo().getSourceFile(currentAnimate.getFileName(index), selectedFrame);
					if (source != null) {
						str += "    来自：" + source;
					}
				}
			}
			tw = gc.stringWidth(str);
            gc.drawRect(1, size.y - th - 8, tw + 7, th + 6);
            gc.drawString(str, 4, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
		}
		if (input != null && input instanceof PipImageDraw) {
			Image img = ((PipImageDraw)input).createSWTImage(getDisplay(), 0);
			String str = img.getBounds().width + ", " + img.getBounds().height;
			img.dispose();
			int tw = gc.stringWidth(str);
			int th = gc.getFontHeight();
			gc.drawRect(1, size.y - th - 8, tw + 7, th + 6);
			gc.drawString(str, 4, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
		}
		
		// 编辑PIP图片时，可以快捷键调整帧顺序
		if (input != null && input instanceof PipImage && this.editable) {
			String info2 = "SHFIT+左  -  前移   SHIFT+右  -  后移";
			int tw = gc.stringWidth(info2);
			int th = gc.getFontHeight();
	        gc.drawString(info2, size.x / 2 - tw / 2, size.y - th * 2 - 5, GLGraphics.TOP | GLGraphics.LEFT);
		}
	}
	
	private String getMemStr(int mem) {
		int kk = mem / 1024;
		if (kk == 0) {
			kk = 1;
		}
		return kk + "kb";
	}

	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
		} else if (input instanceof Image) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawImage(img, 0, 0, imgSize.width, imgSize.height, drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
		} else if (input instanceof PipImage) {
			PipImage img = (PipImage)input;
			Point[] pos = getBestLayout(img, false);
			frameLayout = pos;
			if (pos == null) {
				return;
			}
			int iw = (int)(pos[0].x * ratio);
			int ih = (int)(pos[0].y * ratio);
			if (pipBufferImg != null && (pipBufferImg.getBounds().width != iw || pipBufferImg.getBounds().height != ih)) {
				pipBufferImg.dispose();
				pipBufferImg = null;
			}
			if (pipBufferImg == null) {
				pipBufferImg = new Image(getDisplay(), iw, ih);
				GC bgc = new GC(pipBufferImg);
				try {
					bgc.setBackground(getBackground());
					bgc.fillRectangle(0, 0, iw, ih);
					
					int startPalette, endPalette;
					int paletteCount = img.getImagePalettes().size();
					if (flatMode) {
						startPalette = 0;
						endPalette = paletteCount;
					} else {
						startPalette = img.getPaletteIndex();
						endPalette = startPalette + 1;
					}
					for (int pp = startPalette; pp < endPalette; pp++) {
						img.setPaletteIndex(pp);
						for (int i = 0; i < img.getImgCount(); i++) {
							int posIndex = i + (pp - startPalette) * img.getImgCount() + 1;
							int dx = (int)(pos[posIndex].x * ratio);
							int dy = (int)(pos[posIndex].y * ratio);
							PipImageData frameData = img.getImageData(i);
							int dw = (int)(frameData.getWidth() * ratio);
							int dh = (int)(frameData.getHeight() * ratio);
							Image frameImg = img.getImageDraw(i).createSWTImage(getDisplay(), 0);
							bgc.drawImage(frameImg, 0, 0, frameData.getWidth(), frameData.getHeight(), dx, dy, dw, dh);
							frameImg.dispose();
						}
					}
				} finally {
					bgc.dispose();
				}
			}
			int offx = (int)(size.x - pos[0].x * ratio) / 2;
			int offy = (int)(size.y - pos[0].y * ratio) / 2;
			gc.drawImage(pipBufferImg, offx + paintOffset.x, offy + paintOffset.y);
		} else if (input instanceof PipImageDraw) {
			Image img = ((PipImageDraw)input).createSWTImage(getDisplay(), 0);
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawImage(img, 0, 0, imgSize.width, imgSize.height, drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
			img.dispose();
		}
	}
	
	protected void paintInput(GLGraphics gc) {
		Point size = getSize();
		if (input == null) {
		} else if (input instanceof Image) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawTexture(GLUtils.loadImage(img), 0, 0, drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
		}  else if (input instanceof PipImage) {
			PipImage img = (PipImage)input;
			Point[] pos = getBestLayout(img, false);
			frameLayout = pos;
			if (pos == null) {
				return;
			}
			int startPalette, endPalette;
			int paletteCount = img.getImagePalettes().size();
			if (flatMode) {
				startPalette = 0;
				endPalette = paletteCount;
			} else {
				startPalette = img.getPaletteIndex();
				endPalette = startPalette + 1;
			}
			if (bufferImages.size() != (endPalette - startPalette + 1) * img.getImgCount()) {
				clearBuffer();
			}
			int bufferIndex = 0;
			int offx = (int)(size.x - pos[0].x * ratio) / 2 + paintOffset.x;
			int offy = (int)(size.y - pos[0].y * ratio) / 2 + paintOffset.y;
			for (int pp = startPalette; pp < endPalette; pp++) {
				img.setPaletteIndex(pp);
				for (int i = 0; i < img.getImgCount(); i++) {
					int posIndex = i + (pp - startPalette) * img.getImgCount() + 1;
					int dx = (int)(pos[posIndex].x * ratio);
					int dy = (int)(pos[posIndex].y * ratio);
					PipImageData frameData = img.getImageData(i);
					int dw = (int)(frameData.getWidth() * ratio);
					int dh = (int)(frameData.getHeight() * ratio);
					Image frameImg;
					if (bufferIndex >= bufferImages.size()) {
						frameImg = img.getImageDraw(i).createSWTImage(getDisplay(), 0);
						bufferImages.add(frameImg);
					} else {
						frameImg = bufferImages.get(bufferIndex);
					}
					gc.drawTexture(GLUtils.loadImage(frameImg), 0, 0, dx + offx, dy + offy, dw, dh);
					bufferIndex++;
				}
			}
		} else if (input instanceof PipImageDraw) {
			Image img;
			if (bufferImages.size() > 0) {
				img = bufferImages.get(0);
			} else {
				img = ((PipImageDraw)input).createSWTImage(getDisplay(), 0);
				bufferImages.add(img);
			}
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawTexture(GLUtils.loadImage(img), 0, 0, drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
		}
	}
	
	private void drawCutAreas(GC gc) {
		Point size = getSize();
		Image img = (Image)input;
		Rectangle imgSize = img.getBounds();
		Rectangle zoomSize = zoom(imgSize);
		for (int i = 0; i < cutAreas.size(); i++) {
			Rectangle rect = zoom(cutAreas.get(i));
			rect.x += (size.x - zoomSize.width) / 2 + paintOffset.x;
			rect.y += (size.y - zoomSize.height) / 2 + paintOffset.y;
			gc.setXORMode(true);
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(rect);
			gc.setXORMode(false);
		}
	}
	
	private void drawCutAreas(GLGraphics gc) {
		Point size = getSize();
		Image img = (Image)input;
		Rectangle imgSize = img.getBounds();
		Rectangle zoomSize = zoom(imgSize);
		for (int i = 0; i < cutAreas.size(); i++) {
			Rectangle rect = zoom(cutAreas.get(i));
			rect.x += (size.x - zoomSize.width) / 2 + paintOffset.x;
			rect.y += (size.y - zoomSize.height) / 2 + paintOffset.y;
			int clr = gc.getColor();
			gc.setColor(0x80000000);
			//gc.setPaintOption(true, GLGraphics.GL_ONE, GLGraphics.GL_ONE, GLGraphics.GL_FUNC_SUBTRACT);
			gc.fillRect(rect.x, rect.y, rect.width, rect.height);
			//gc.setPaintOption(true, GLGraphics.GL_SRC_ALPHA, GLGraphics.GL_ONE_MINUS_SRC_ALPHA, GLGraphics.GL_FUNC_ADD);
			gc.setColor(clr);
		}
	}
	
	private int calcPointFrame(Point p) {
		if (frameLayout == null) {
			return -1;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].x * ratio) / 2 + paintOffset.x;
		int offy = (int)(size.y - frameLayout[0].y * ratio) / 2 + paintOffset.y;
		PipImage pi = (PipImage)input;
		for (int i = 1; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			PipImageData frameData = pi.getImageData(i - 1);
			int dw = (int)(frameData.getWidth() * ratio);
			int dh = (int)(frameData.getHeight() * ratio);
			if (new Rectangle(dx, dy, dw, dh).contains(p)) {
				return i - 1;
			}
		}
		return -1;
	}
	
	private Point calcCoord(Point p) {
		if (input instanceof Image) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			Point size = getSize();
			zoomSize.x = (size.x - zoomSize.width) / 2 + paintOffset.x;
			zoomSize.y = (size.y - zoomSize.height) / 2 + paintOffset.y;
			int x = (int)(fitGrid(p.x - zoomSize.x) / ratio);
			int y = (int)(fitGrid(p.y - zoomSize.y) / ratio);
			return new Point(x, y);
		} else {
			PipImageDraw imgDraw = (PipImageDraw)input;
			Rectangle imgSize = imgDraw.getBounds(0);
			Rectangle zoomSize = zoom(imgSize);
			Point size = getSize();
			zoomSize.x = (size.x - zoomSize.width) / 2 + paintOffset.x;
			zoomSize.y = (size.y - zoomSize.height) / 2 + paintOffset.y;
			int x = (int)(fitGrid(p.x - zoomSize.x) / ratio);
			int y = (int)(fitGrid(p.y - zoomSize.y) / ratio);
			return new Point(x, y);
		}
	}
	
	private void normalize(Rectangle rect) {
		if (rect.width < 0) {
			rect.x = rect.x + rect.width;
			rect.width = -rect.width;
		}
		if (rect.height < 0) {
			rect.y = rect.y + rect.height;
			rect.height = -rect.height;
		}
	}
	
	//  1 : west edge
	//  2 : north edge;
	//  3 : east edge
	//  4 : south edge
	//  5 : north-west corner
	//  6 : north-east corner
	//  7 : south-east corner
	//  8 : south-west corner
	private Rectangle dragSelection(Rectangle original, Point start, Point end) {
		int offx = end.x - start.x;
		int offy = end.y - start.y;
		offx = (int)(fitGrid(offx) / ratio);
		offy = (int)(fitGrid(offy) / ratio);
		switch (dragAnchor) {
		case 0:
			return new Rectangle(original.x + offx, original.y + offy, original.width, original.height);
		case 1:
			return new Rectangle(original.x + offx, original.y, original.width - offx, original.height);
		case 2:
			return new Rectangle(original.x, original.y + offy, original.width, original.height - offy);
		case 3:
			return new Rectangle(original.x, original.y, original.width + offx, original.height);
		case 4:
			return new Rectangle(original.x, original.y, original.width, original.height + offy);
		case 5:
			return new Rectangle(original.x + offx, original.y + offy, original.width - offx, original.height - offy);
		case 6:
			return new Rectangle(original.x, original.y + offy, original.width + offx, original.height - offy);
		case 7:
			return new Rectangle(original.x, original.y, original.width + offx, original.height + offy);
		case 8:
			return new Rectangle(original.x + offx, original.y, original.width - offx, original.height + offy);
		default:
			return original;
		}
	}
	
	private Rectangle calcSelection() {
		Image img = (Image)input;
		Rectangle imgSize = img.getBounds();
		Rectangle zoomSize = zoom(imgSize);
		Point size = getSize();
		zoomSize.x = (size.x - zoomSize.width) / 2 + paintOffset.x;
		zoomSize.y = (size.y - zoomSize.height) / 2 + paintOffset.y;
		Rectangle selection = new Rectangle(Math.min(startPoint.x, endPoint.x), 
				Math.min(startPoint.y, endPoint.y), Math.abs(startPoint.x - endPoint.x),
				Math.abs((startPoint.y - endPoint.y)));
//		selection.intersect(zoomSize);
		int x = (int)(fitGrid(selection.x - zoomSize.x) / ratio);
		int y = (int)(fitGrid(selection.y - zoomSize.y) / ratio);
		int x2 = (int)(fitGrid(selection.x - zoomSize.x + selection.width) / ratio);
		int y2 = (int)(fitGrid(selection.y - zoomSize.y + selection.height) / ratio);
		Rectangle resultRect = new Rectangle(x, y, x2 - x, y2 - y);
		resultRect.intersect(imgSize);
		if (resultRect.isEmpty()) {
			return null;
		}
		return resultRect;
	}
	
	private void drawSelection(GC gc) {
		if (isSelecting) {
			Rectangle selection = new Rectangle(Math.min(startPoint.x, endPoint.x), 
					Math.min(startPoint.y, endPoint.y), Math.abs(startPoint.x - endPoint.x),
					Math.abs((startPoint.y - endPoint.y)));
			gc.setXORMode(true);
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.drawRectangle(selection);
			gc.setXORMode(false);
		} else if (selectedArea != null) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			Point size = getSize();
			Rectangle zoomArea = zoom(selectedArea);
			zoomArea.x += (size.x - zoomSize.width) / 2;
			zoomArea.y += (size.y - zoomSize.height) / 2;
			gc.setXORMode(true);
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			zoomArea.x += paintOffset.x;
			zoomArea.y += paintOffset.y;
			gc.drawRectangle(zoomArea);
			gc.setXORMode(false);
		}
	}
	
	private void drawSelection(GLGraphics gc) {
		if (isSelecting) {
			Rectangle selection = new Rectangle(Math.min(startPoint.x, endPoint.x), 
					Math.min(startPoint.y, endPoint.y), Math.abs(startPoint.x - endPoint.x),
					Math.abs((startPoint.y - endPoint.y)));
			int clr = gc.getColor();
			gc.setColor(0xFFFFFFFF);
			gc.setPaintOption(true, GLGraphics.GL_ONE, GLGraphics.GL_ONE, GLGraphics.GL_FUNC_SUBTRACT);
			gc.drawRect(selection.x, selection.y, selection.width, selection.height);
			gc.setPaintOption(true, GLGraphics.GL_SRC_ALPHA, GLGraphics.GL_ONE_MINUS_SRC_ALPHA, GLGraphics.GL_FUNC_ADD);
			gc.setColor(clr);
		} else if (selectedArea != null) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			Point size = getSize();
			Rectangle zoomArea = zoom(selectedArea);
			zoomArea.x += (size.x - zoomSize.width) / 2;
			zoomArea.y += (size.y - zoomSize.height) / 2;
			int clr = gc.getColor();
			gc.setColor(0xFFFFFF);
			zoomArea.x += paintOffset.x;
			zoomArea.y += paintOffset.y;
			gc.setPaintOption(true, GLGraphics.GL_ONE, GLGraphics.GL_ONE, GLGraphics.GL_FUNC_SUBTRACT);
			gc.drawRect(zoomArea.x, zoomArea.y, zoomArea.width, zoomArea.height);
			gc.setPaintOption(true, GLGraphics.GL_SRC_ALPHA, GLGraphics.GL_ONE_MINUS_SRC_ALPHA, GLGraphics.GL_FUNC_ADD);
			gc.setColor(clr);
		}
	}
	
	public Rectangle getSelection() {
		return selectedArea;
	}
	
	private Point[] getBestLayout(PipImage img, boolean forExport) {
		if (img.getImgCount() == 0) {
			return null;
		}
		
		if (forExport && img instanceof LandformImage) {
			// 地形文件特殊处理，拼成一长条
			int count = img.getImgCount();
			int currentX = 0;
			int maxHeight = 0;
			Point[] ret = new Point[count + 1];
			for (int i = 0; i < count; i++) {
				ret[i + 1] = new Point(currentX, 0);
				currentX += img.getImageData(i).width;
				if (img.getImageData(i).height > maxHeight) {
					maxHeight = img.getImageData(i).height;
				}
			}
			ret[0] = new Point(currentX, maxHeight);
			return ret;
		}
		
		int w = 0, h = 0;
		int count = img.getImgCount();
		if (flatMode) {
			count *= img.getImagePalettes().size();
		}
		for (int i = 0; i < count; i++) {
			PipImageData data = img.getImageData(i);
			w += data.getWidth() + 2;
			if (data.getHeight() + 2 > h) {
				h = data.getHeight() + 2;
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
			PipImageData data = img.getImageData(i);
			if (dx != 0 && dx + data.getWidth() + 2 > w) {
				dx = 0;
				dy += lh;
				lh = 0;
				i--;
				continue;
			} else {
				ret[i + 1].x = dx;
				ret[i + 1].y = dy;
				dx += data.getWidth() + 2;
				if (lh < data.getHeight() + 2) {
					lh = data.getHeight() + 2;
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
	
	private boolean isSelectionAllowed() {
		return input != null && input instanceof Image;
	}
	
	private boolean isFrameSelectionAllowed() {
		return input != null && input instanceof PipImage;
	}
	
	public Rectangle getSelectedArea() {
		return selectedArea;
	}

	public int getSelectedFrame() {
		return selectedFrame;
	}
	
	public int[] getSelectedFrames() {
		Object[] arr = selectedFrames.toArray();
		int[] ret = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			ret[i] = ((Integer)arr[i]).intValue();
		}
		return ret;
	}
	
	public void setSelectedFrame(int selectedFrame) {
		this.selectedFrame = selectedFrame;
		selectedFrames.clear();
		if (this.selectedFrame == -1) {
			setMenu(null);
		} else {
			setMenu(popMenu);
			selectedFrames.add(this.selectedFrame);
		}
		redraw();
	}
	
	public void addSelectedFrame(int newFrame) {
		this.selectedFrame = newFrame;
		if (!selectedFrames.contains(newFrame)) {
			selectedFrames.add(newFrame);
		}
		setMenu(popMenu);
		redraw();
	}
	
	/**
	 * 选中从上一次选中的帧到当前选中的帧之间的所有帧。
	 * @param newFrame
	 */
	public void addSelectedFrameTo(int newFrame) {
		if (selectedFrame == -1 || selectedFrame == newFrame) {
			return;
		}
		selectedFrames.clear();
		if (selectedFrame < newFrame) {
			for (int i = selectedFrame; i <= newFrame; i++) {
				selectedFrames.add(i);
			}
		} else {
			for (int i = newFrame; i <= selectedFrame; i++) {
				selectedFrames.add(i);
			}
		}
		setMenu(popMenu);
		redraw();
	}

	public void setFlatMode(boolean flatMode) {
		this.flatMode = flatMode;
	}
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (input != null && input instanceof PipImage) {
			PipImage p = (PipImage)input;
			switch (keyCode) {
			case SWT.ARROW_UP:
			    if (!this.flatMode && this.editable) {
			        this.onMoveUp();
			    }
			    break;
			case SWT.ARROW_LEFT:
				if ((keyEventMask & SWT.SHIFT) != 0) {
					if (this.editable) {
						onMoveUp();
					}
				} else {
					int nf = selectedFrame - 1;
					if (nf < 0) {
						nf = p.getImgCount() - 1;
					}
					setSelectedFrame(nf);
				}
				break;
			case SWT.ARROW_DOWN:
			    if (!this.flatMode && this.editable) {
			        this.onMoveDown();
			    }
			    break;
			case SWT.ARROW_RIGHT:
				if ((keyEventMask & SWT.SHIFT) != 0) {
					if (this.editable) {
						onMoveDown();
					}
				} else {
					int nf = selectedFrame + 1;
					if (nf >= p.getImgCount()) {
						nf = 0;
						if (p.getImgCount() == 0) {
							nf = -1;
						}
					}
					setSelectedFrame(nf);
				}
				break;
			case SWT.DEL:
				if (this.editable) {
					onDeleteFrame();
				}
				break;
			default:
				break;
			}
			redraw();
			return;
		}
		if (input == null || isSelecting || isDraggingSelection || selectedArea == null) {
			return;
		}
		if (!isSelectionAllowed()) {
			return;
		}
		switch (keyCode) {
		case SWT.ARROW_UP:
			selectedArea.y--;
			break;
		case SWT.ARROW_DOWN:
			selectedArea.y++;
			break;
		case SWT.ARROW_LEFT:
			selectedArea.x--;
			break;
		case SWT.ARROW_RIGHT:
			selectedArea.x++;
			break;
		default:
			return;
		}
		selectedArea.intersect(((Image)input).getBounds());
		if (selectedArea.isEmpty()) {
			selectedArea = null;
		}
		redraw();
	}
	
	private boolean checkFlatModeEditing() {
		if (flatModeWarned || ((PipImage)input).getImagePalettes().size() <= 1) {
			return true;
		}
		String msg = "你现在正在编辑一个多调色板模式的图片，对任何一帧的改动都将影响到不同调色板的对应帧，是否继续？";
		boolean ret = MessageDialog.openConfirm(getShell(), "警告", msg);
		if (ret) {
			flatModeWarned = true;
		}
		return ret;
	}

	private void onDuplicateFrame() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (!flatMode) {
			PipImageData data = allImage.getImageData(sel);
			allImage.getImageDatas().add(sel + 1, data.duplicate());
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			setSelectedFrame(sel + 1);
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		} else if (checkFlatModeEditing()) {
			int fcount = allImage.getImgCount();
			int pcount = allImage.getImagePalettes().size(); 
			sel = sel % allImage.getImgCount(); 
			frameMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < fcount; i++) {
				for (int j = 0; j < pcount; j++) {
					if (i <= sel) {
						frameMap.put(j * fcount + i, j * (fcount + 1) + i);
					} else {
						frameMap.put(j * fcount + i, j * (fcount + 1) + i + 1);
					}
				}
			}
			PipImageData data = allImage.getImageData(sel);
			allImage.getImageDatas().add(sel + 1, data.duplicate());
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			setSelectedFrame(sel + 1);
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		}
	}
	
	private void onHflipFrame() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (!flatMode) {
			allImage.getImageData(sel).hflip();
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			redraw();
		} else if (checkFlatModeEditing()) {
			frameMap = null;
			allImage.getImageData(sel).hflip();
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			redraw();
		}
	}
	
	private void onVflipFrame() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (!flatMode) {
			allImage.getImageData(sel).vflip();
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			redraw();
		} else if (checkFlatModeEditing()) {
			frameMap = null;
			allImage.getImageData(sel).vflip();
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			redraw();
		}
	}
	
	public void onDeleteFrame() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (!flatMode) {
			allImage.getImageDatas().remove(sel);
            if (allImage instanceof LandformImage) {
                ((LandformImage)allImage).onFrameRemoved(sel);
            }
            splitPlan = null;
            pipBufferImg = null;
			fireContentChanged();
			int nf = sel;
			while (nf >= allImage.getImgCount()) {
				nf--;
			}
			setSelectedFrame(nf);
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		} else if (checkFlatModeEditing()) {
			int fcount = allImage.getImgCount();
			int pcount = allImage.getImagePalettes().size(); 
			sel = sel % allImage.getImgCount(); 
			frameMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < fcount; i++) {
				if (i < sel) {
					for (int j = 0; j < pcount; j++) {
						frameMap.put(j * fcount + i, j * (fcount - 1) + i);
					}
				} else if (i > sel) {
					for (int j = 0; j < pcount; j++) {
						frameMap.put(j * fcount + i, j * (fcount - 1) + i - 1);
					}
				}
			}
			allImage.getImageDatas().remove(sel);
			splitPlan = null;
			pipBufferImg = null;
			fireContentChanged();
			int nf = sel;
			while (nf >= allImage.getImgCount()) {
				nf--;
			}
			setSelectedFrame(nf);
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		}
	}
	
	public Map<Integer, Integer> getFrameMap() {
		return frameMap;
	}
	
    private void onEditLandformFrame() {
        int sel = selectedFrame;
        if (sel == -1) {
            return;
        }
        LandformImage allImage = (LandformImage)input;
        EditLandformFrameDialog dlg = new EditLandformFrameDialog(getShell());
        dlg.setType(allImage.getFrameType(sel));
        dlg.setPriority(allImage.getFramePriority(sel));
        if (dlg.open() == EditLandformFrameDialog.OK) {
            allImage.setFrameAttr(sel, dlg.getType(), dlg.getPriority());
            splitPlan = null;
            pipBufferImg = null;
            fireContentChanged();
            redraw();
        }
    }

    public void onEditFrame() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (flatMode) {
			if (checkFlatModeEditing()) {
				sel = sel % allImage.getImgCount();
			} else {
				return;
			}
		}
		allImage.setPaletteIndex(0);
		Image img = allImage.getImageDraw(sel).createSWTImage(getDisplay(), 0);
		PngEncoder enc = new PngEncoder(img);
		try {
			File tmpFile = File.createTempFile("_iws_", ".png");
			FileOutputStream fos = new FileOutputStream(tmpFile);
			enc.encode32(fos, false);
			fos.close();
			tmpFile.deleteOnExit();
			Runtime.getRuntime().exec(new String[] {
				Settings.imageEditor,
				java.text.MessageFormat.format(Settings.imageEditorArg, tmpFile.getAbsolutePath())
			});
			
			EditingFrame ef = new EditingFrame();
			ef.file = tmpFile;
			ef.frame = sel;
			FileWatcher.watch(ef.file, this);
			synchronized (ef) {
				editingFrames.add(ef);
			}
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}
		img.dispose();
	}

    private void onExportAll() {
        DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
        String path = dlg.open();
        if (path == null) {
            return;
        }
        PipImage allImage = (PipImage)input;
        try {
            for (int i = 0; i < allImage.getImgCount(); i++) {
                File tmpFile = new File(path, "_iws_" + i + ".png");
                Image img = allImage.getImageDraw(i).createSWTImage(getDisplay(), 0);
                PngEncoder enc = new PngEncoder(img);
                FileOutputStream fos = new FileOutputStream(tmpFile);
                enc.encode32(fos, false);
                fos.close();
                tmpFile.deleteOnExit();
                img.dispose();
                
                EditingFrame ef = new EditingFrame();
                ef.file = tmpFile;
                ef.frame = i;
                FileWatcher.watch(ef.file, this);
                synchronized (ef) {
                    editingFrames.add(ef);
                }
            }
        } catch (Exception e) {
        	SWTUtils.showError(getShell(), "错误", e);
        }
    }

    /*
     * 导出所有帧到一个大图中，并生成一个.s文件。
     */
    private void onExportAll2() {
        FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
        dlg.setFilterExtensions(new String[] { "*.png" });
        dlg.setFilterNames(new String[] { "PNG文件(*.png)" });
        String picFile = dlg.open();
        if (picFile == null) {
            return;
        }
        PipImage allImage = (PipImage)input;
        try {
            boolean oldMode = flatMode;
            flatMode = false;
            Point[] pos = getBestLayout(allImage, true);
            flatMode = oldMode;
            if (pos == null) {
                return;
            }
            
            ImageData bufferData = new ImageData(pos[0].x, pos[0].y, 32, new PaletteData(0x0000FF00, 0x00FF0000, 0xFF000000));
            bufferData.alphaData = new byte[pos[0].x * pos[0].y];
            allImage.setPaletteIndex(0);
            for (int i = 0; i < allImage.getImgCount(); i++) {
                Image frameImg = allImage.getImageDraw(i).createSWTImage(getDisplay(), 0);
                ImageData fdata = frameImg.getImageData();
                for (int y = 0; y < fdata.height; y++) {
                	for (int x = 0; x < fdata.width; x++) {
                		int tx = pos[i + 1].x + x;
                		int ty = pos[i + 1].y + y;
                		int value = fdata.getPixel(x, y);
                		bufferData.alphaData[ty * bufferData.width + tx] = (byte)(value & 0xFF);
                		int sp = ty * bufferData.bytesPerLine + tx * 4;
                		bufferData.data[sp++] = (byte)((value >> 24) & 0xFF);
                		bufferData.data[sp++] = (byte)((value >> 16) & 0xFF);
                		bufferData.data[sp++] = (byte)((value >> 8) & 0xFF);
                		bufferData.data[sp++] = (byte)(value & 0xFF);
                	}
                }
                frameImg.dispose();
            }
            
            // Write .png file
            Image tmpImg = new Image(getDisplay(), bufferData);
            PngEncoder enc = new PngEncoder(tmpImg);
            FileOutputStream fos = new FileOutputStream(picFile);
            enc.encode32(fos, false);
            fos.close();
            tmpImg.dispose();
            
            // Write .s file
            int lastPos = picFile.lastIndexOf('.');
            String sfile = picFile.substring(0, lastPos) + ".s";
            ImageDescription imageDesc = new ImageDescription();
            imageDesc.type = ImageDescription.VERSION_4;
            for (int i = 0; i < allImage.getImgCount(); i++) {
                TileInfo2 ti2 = new TileInfo2();
                ti2.x = pos[i + 1].x;
                ti2.y = pos[i + 1].y;
                ti2.width = allImage.getImageData(i).getWidth();
                ti2.height = allImage.getImageData(i).getHeight();
                imageDesc.tileList2.add(ti2);
            }
            imageDesc.save(new File(sfile));
        } catch (Exception e) {
        	SWTUtils.showError(getShell(), "错误", e);
        }
    }
    
    private void onImportAll() {
        FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.png" });
        dlg.setFilterNames(new String[] { "PNG文件(*.png)" });
        String picFile = dlg.open();
        if (picFile == null) {
            return;
        }
        int pos = picFile.lastIndexOf('.');
        String sFile = picFile.substring(0, pos) + ".s";
        if (!new File(sFile).exists()) {
            MessageDialog.openError(getShell(), "错误", "没有找到对应的.s文件。");
            return;
        }
        
        try {
            ImageDescription id = new ImageDescription();
            id.load(new File(sFile));
            if (id.type != ImageDescription.VERSION_4) {
                MessageDialog.openError(getShell(), "错误", "这个.s文件不是使用本工具导出生成的。");
                return;
            }

            PipImage allImage = (PipImage)input;
            
            // 删除所有已有数据
            allImage.getImagePalettes().clear();
            allImage.getImageDatas().clear();
            
            // 读取调色板
            PipImage.initPalette(allImage, new File(picFile));
            
            // 读取帧数据
            Image newImg = new Image(getShell().getDisplay(), picFile);
            Object[] tiles = id.getTileList();
            for (int i = 0; i < tiles.length; i++){
                TileInfo2 info = (TileInfo2)tiles[i];
                int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(info.x, info.y, info.width, info.height));
                allImage.addFrame(rawData);
                if ((info.param & ImageDescription.T_HORIZONTAL) > 0) {
                    allImage.getImageData(allImage.getImgCount() - 1).hflip();
                }
                if ((info.param & ImageDescription.T_VERTICAL) > 0) {
                    allImage.getImageData(allImage.getImgCount() - 1).vflip();
                }
            }
            newImg.dispose();
        } catch (Exception e) {
        	SWTUtils.showError(getShell(), "错误", e);
        }
        splitPlan = null;
        pipBufferImg = null;
        redraw();
        fireContentChanged();
    }

    private void onMoveUp() {
		PipImage allImage = (PipImage)input;
		if (selectedFrames.size() == 0) {
			return;
		}
		for (int sel : selectedFrames) {
			if (sel >= allImage.getImgCount()) {
				return;
			}
		}
		int[] selArr = new int[selectedFrames.size()];
		for (int i = 0; i < selectedFrames.size(); i++) {
			selArr[i] = selectedFrames.get(i);
		}
		Arrays.sort(selArr);
		if (!flatMode) {
			for (int i = 0; i < selArr.length; i++) {
				int sel = selArr[i];
				if (sel > 0) {
					PipImageData data1 = allImage.getImageData(sel);
					allImage.getImageDatas().set(sel, allImage.getImageData(sel - 1));
					allImage.getImageDatas().set(sel - 1, data1);
		            if (allImage instanceof LandformImage) {
		                ((LandformImage)allImage).onFrameSwap(sel, sel - 1);
		            }
				}
			}
			splitPlan = null;
            pipBufferImg = null;
			fireContentChanged();
			if (selectedFrame > 0) {
				selectedFrame--;
			}
			for (int i = 0; i < selectedFrames.size(); i++) {
				if (selectedFrames.get(i) > 0) {
					selectedFrames.set(i, selectedFrames.get(i) - 1);
				}
			}
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		} else if (checkFlatModeEditing()) {
			int[] newOrder = new int[allImage.getImgCount()];
			for (int i = 0; i < newOrder.length; i++) {
				newOrder[i] = i;
			}
			for (int i = 0; i < selArr.length; i++) {
				int sel = selArr[i];
				if (sel > 0) {
					PipImageData data1 = allImage.getImageData(sel);
					allImage.getImageDatas().set(sel, allImage.getImageData(sel - 1));
					allImage.getImageDatas().set(sel - 1, data1);
		            if (allImage instanceof LandformImage) {
		                ((LandformImage)allImage).onFrameSwap(sel, sel - 1);
		            }
		            
		            int tmp = newOrder[sel - 1];
		            newOrder[sel - 1] = newOrder[sel];
		            newOrder[sel] = tmp;
				}
			}
			int fcount = allImage.getImgCount();
			int pcount = allImage.getImagePalettes().size();
			frameMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < newOrder.length; i++) {
				for (int j = 0; j < pcount; j++) {
					frameMap.put(j * fcount + newOrder[i], j * fcount + i);
				}
			}
			splitPlan = null;
	        pipBufferImg = null;
			fireContentChanged();
			if (selectedFrame > 0) {
				selectedFrame--;
			}
			for (int i = 0; i < selectedFrames.size(); i++) {
				if (selectedFrames.get(i) > 0) {
					selectedFrames.set(i, selectedFrames.get(i) - 1);
				}
			}
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		}
	}
	
	private void onMoveDown() {
		PipImage allImage = (PipImage)input;
		if (selectedFrames.size() == 0) {
			return;
		}
		for (int sel : selectedFrames) {
			if (sel >= allImage.getImgCount()) {
				return;
			}
		}
		int[] selArr = new int[selectedFrames.size()];
		for (int i = 0; i < selectedFrames.size(); i++) {
			selArr[i] = selectedFrames.get(i);
		}
		Arrays.sort(selArr);
		if (!flatMode) {
			for (int i = selArr.length - 1; i >= 0; i--) {
				int sel = selArr[i];
				if (sel < allImage.getImgCount() - 1) {
					PipImageData data1 = allImage.getImageData(sel);
					allImage.getImageDatas().set(sel, allImage.getImageData(sel + 1));
					allImage.getImageDatas().set(sel + 1, data1);
		            if (allImage instanceof LandformImage) {
		                ((LandformImage)allImage).onFrameSwap(sel, sel + 1);
		            }
				}
			}
			splitPlan = null;
            pipBufferImg = null;
			fireContentChanged();
			if (selectedFrame < allImage.getImgCount() - 1) {
				selectedFrame++;
			}
			for (int i = 0; i < selectedFrames.size(); i++) {
				if (selectedFrames.get(i) < allImage.getImgCount() - 1) {
					selectedFrames.set(i, selectedFrames.get(i) + 1);
				}
			}
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		} else if (checkFlatModeEditing()) {
			int[] newOrder = new int[allImage.getImgCount()];
			for (int i = 0; i < newOrder.length; i++) {
				newOrder[i] = i;
			}
			for (int i = selArr.length - 1; i >= 0; i--) {
				int sel = selArr[i];
				if (sel < allImage.getImgCount() - 1) {
					PipImageData data1 = allImage.getImageData(sel);
					allImage.getImageDatas().set(sel, allImage.getImageData(sel + 1));
					allImage.getImageDatas().set(sel + 1, data1);
		            if (allImage instanceof LandformImage) {
		                ((LandformImage)allImage).onFrameSwap(sel, sel + 1);
		            }
		            
		            int tmp = newOrder[sel + 1];
		            newOrder[sel + 1] = newOrder[sel];
		            newOrder[sel] = tmp;
				}
			}
			int fcount = allImage.getImgCount();
			int pcount = allImage.getImagePalettes().size();
			frameMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < newOrder.length; i++) {
				for (int j = 0; j < pcount; j++) {
					frameMap.put(j * fcount + newOrder[i], j * fcount + i);
				}
			}
			splitPlan = null;
	        pipBufferImg = null;
			fireContentChanged();
			if (selectedFrame < allImage.getImgCount() - 1) {
				selectedFrame++;
			}
			for (int i = 0; i < selectedFrames.size(); i++) {
				if (selectedFrames.get(i) < allImage.getImgCount() - 1) {
					selectedFrames.set(i, selectedFrames.get(i) + 1);
				}
			}
			fireFrameSelectionChanged(selectedFrame);
			redraw();
		}
	}
	
	private void onSplitImage() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (allImage.getPaletteCount() > 1) {
			MessageDialog.openError(getShell(), "错误 ", "多调色板模式的图片不支持此功能。");
			return;
		}
		
		// 让用户设置切分方案
		Image img = allImage.getImageDraw(selectedFrame).createSWTImage(getShell().getDisplay(), 0);
		CutPieceDialog dlg = new CutPieceDialog(getShell(), img);
		if (dlg.open() != Dialog.OK) {
			return;
		}
		int[][] rects = dlg.getCutAreas();
		PipImage cutFrames = dlg.getCutImage();
		
		// 拆分图块
		int oldWidth = allImage.getImageData(selectedFrame).width;
		int oldHeight = allImage.getImageData(selectedFrame).height;
		allImage.splitFrame(selectedFrame, rects, cutFrames);
		splitPlan = rects;
		splitOriginalWidth = oldWidth;
		splitOriginalHeight = oldHeight;
		
		pipBufferImg = null;
		fireContentChanged();
		redraw();
	}
	
	private void onSplitImageFixSize() {
		int sel = selectedFrame;
		PipImage allImage = (PipImage)input;
		if (sel == -1) {
			return;
		}
		if (allImage.getPaletteCount() > 1) {
			MessageDialog.openError(getShell(), "错误 ", "多调色板模式的图片不支持此功能。");
			return;
		}
		
		// 输入大小
		InputDialog idlg = new InputDialog(getShell(), "宽度", "请输入拆分块宽度：", "128", new IInputValidator() {
			public String isValid(String value) {
				try {
					int i = Integer.parseInt(value);
					if (i < 1 || i > 100000) {
						return "请输入1-100000之间的整数。";
					}
					return null;
				} catch (Exception e) {
					return "请输入合法的整数。";
				}
			}
		});
		if (idlg.open() != InputDialog.OK) {
			return;
		}
		int blockWidth = Integer.parseInt(idlg.getValue());
		
		idlg = new InputDialog(getShell(), "高度", "请输入拆分块高度：", "128", new IInputValidator() {
			public String isValid(String value) {
				try {
					int i = Integer.parseInt(value);
					if (i < 1 || i > 100000) {
						return "请输入1-100000之间的整数。";
					}
					return null;
				} catch (Exception e) {
					return "请输入合法的整数。";
				}
			}
		});
		if (idlg.open() != InputDialog.OK) {
			return;
		}
		int blockHeight = Integer.parseInt(idlg.getValue());
		
		List<String> opts = new ArrayList<String>();
		opts.add("去除图块四周透明的区域，且忽略全透明图块");
		opts.add("仅忽略全透明图块");
		opts.add("不优化");
		GenericChooseDialog cdlg = new GenericChooseDialog(getShell(), "选项", "是否去除透明区域？", opts);
		if (cdlg.open() != Dialog.OK) {
			return;
		}
		int optMode = cdlg.getSelectionIndex();
		
		
		// 切分图块
		Image img = allImage.getImageDraw(selectedFrame).createSWTImage(getShell().getDisplay(), 0);
		PipImage cutFrames = new PipImage();
		cutFrames.setTrueColor(true);
		ArrayList<int[]> cutAreas = new ArrayList<int[]>();
		int width = img.getBounds().width;
		int height = img.getBounds().height;
		for (int y = 0; y < height; y += blockHeight) {
			for (int x = 0; x < width; x += blockWidth) {
				int realw = x + blockWidth > width ? width - x : blockWidth;
				int realh = y + blockHeight > height ? height - y : blockHeight;
				int[][] rawData = getImageData(img, new Rectangle(x, y, realw, realh));
				Rectangle untransArea;
				int[][] untransData;
				if (optMode == 0) {
					untransArea = AutoBody.findUntransparentArea(rawData);
					if (untransArea.width == 1 && untransArea.height == 1 &&
							(rawData[untransArea.y][untransArea.x] & 0xFF000000) == 0) {
						// 没有不透明像素，略过
						continue;
					}
					untransArea.x += x;
					untransArea.y += y;
					untransData = getImageData(img, untransArea);
				} else if (optMode == 1) {
					untransArea = AutoBody.findUntransparentArea(rawData);
					if (untransArea.width == 1 && untransArea.height == 1 &&
							(rawData[untransArea.y][untransArea.x] & 0xFF000000) == 0) {
						// 没有不透明像素，略过
						continue;
					}
					untransArea = new Rectangle(x, y, realw, realh);
					untransData = rawData;
				} else {
					untransArea = new Rectangle(x, y, realw, realh);
					untransData = rawData;
				}
				try {
					boolean oldValue = PipImage.limitSize;
					PipImage.limitSize = false;
					cutFrames.addFrame(untransData);
					PipImage.limitSize = oldValue;
				} catch (ColorsExceedException ce) {
					// impossible
				}
				cutAreas.add(new int[] { untransArea.x, untransArea.y, untransArea.width, untransArea.height });
			}
		}
		img.dispose();
		int[][] rects = new int[cutAreas.size()][];
		cutAreas.toArray(rects);
		
		// 拆分图块
		int oldWidth = allImage.getImageData(selectedFrame).width;
		int oldHeight = allImage.getImageData(selectedFrame).height;
		allImage.splitFrame(selectedFrame, rects, cutFrames);
		splitPlan = rects;
		splitOriginalWidth = oldWidth;
		splitOriginalHeight = oldHeight;
		
		pipBufferImg = null;
		fireContentChanged();
		redraw();
	}
	
	public void fileModified(File f) {
		synchronized (editingFrames) {
			for (EditingFrame ef : editingFrames) {
				if (ef.file.equals(f)) {
					getDisplay().asyncExec(new EditingFrameChanged(ef));
				}
			}
		}
	}
	
	public static int[][] getImageData(Image img, Rectangle area) {
		return SWTUtils.getImageData(img, new com.pip.util.Rectangle(area.x, area.y, area.width, area.height));
	}

	public static int[] getImageData2(Image img, Rectangle area) {
		return SWTUtils.getImageData2(img, new com.pip.util.Rectangle(area.x, area.y, area.width, area.height));
	}
	
	class EditingFrameChanged implements Runnable {
		private EditingFrame ef;
		
		public EditingFrameChanged(EditingFrame ef) {
			this.ef = ef;
		}
		
		public void run() {
			PipImage allImage = (PipImage)input;
			if (ef.frame >= allImage.getImgCount()) {
				return;
			}
			try {
				frameMap = null;
				Image newImg = new Image(getDisplay(), ef.file.getAbsolutePath());
				int[][] newData = getImageData(newImg, newImg.getBounds());
				newImg.dispose();
				allImage.addFrame(newData);
				PipImageData newFrame = allImage.getImageDatas().get(allImage.getImgCount() - 1);
				allImage.getImageDatas().set(ef.frame, newFrame);
				allImage.getImageDatas().remove(allImage.getImgCount() - 1);
				splitPlan = null;
		        pipBufferImg = null;
				fireContentChanged();
				redraw();
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", e);
			}
		}
	}

	public void widgetDisposed(DisposeEvent e) {
		super.widgetDisposed(e);
		if (pipBufferImg != null) {
			pipBufferImg.dispose();
		}
		while (bufferImages.size() > 0) {
			GLUtils.unloadImage(bufferImages.remove(0));
		}
		FileWatcher.unwatch(this);
		if (popMenu != null) {
			popMenu.dispose();
		}
	}
	
	private void clearBuffer() {
		if (pipBufferImg != null) {
			pipBufferImg.dispose();
			pipBufferImg = null;
		}
		while (bufferImages.size() > 0) {
			GLUtils.unloadImage(bufferImages.remove(0));
		}
	}
	
	public void refresh() {
		clearBuffer();
		redraw();
	}
	
	public void clearSelection() {
		selectedFrame = -1;
		selectedFrames.clear();
	}
}
