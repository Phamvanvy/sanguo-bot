package com.pip.image.workshop.editor;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import com.pip.image.workshop.Settings;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to edit a frame(PipAnimateFrame). The frame may be composed by several pieces.
 */
public class AnimateFrameEditor extends AbstractImageViewer implements PieceColorDialog.ColorChangeListener {
	private static final int FRAME_PIECE_SELECTED = 10000;

	private Menu popMenu;
	
	private int[] selectedPiece;
	private  int[] copiedPiece;
	private  PipAnimateFrame copySource;
	private  Action pasteAction;
	private boolean isDragging;
	private Point dragStartPoint;
	private Point dragStartPos;
	private Point lastPoint;
	private AnimateEditor owner;
	private boolean isSelecting;
	private Point selStart, selEnd;
	private Point refPoint = new Point(0, 0);
	private boolean isDraggingRefPoint;
	private boolean mouseInRefPointArea;
	private PipAnimateFrame refFrame;
	private java.util.List<PipAnimateFramePiece> lockPieces;

	protected boolean allowDeletePiece = true;
	
	private ImageDrawCache cache = null;
	private Image anchorButtonImage, colorButtonImage, rotateButtonImage, scaleButtonImage, scalexButtonImage, scaleyButtonImage;
	// 当前选中图块的操作按钮位置
	private Rectangle[] pieceButtonPos;
	// 是否正在拖动当前选中图块的操作按钮
	private boolean isDraggingPieceButton;
	// 拖动的图块操作按钮的索引
	private int draggingButtonIndex;
	// 开始拖动时的图块操作按钮位置
	private Rectangle[] draggingStartPieceButtonPos;
	// 开始拖动时的图块参数
	private int[] draggingStartPieceParam;
	
	// 所有图块被拖动
	public int allOffx = 0;
	public int allOffy = 0;

	public void setInput(Object input) {
		super.setInput(input);
		selectedPiece = new int[0];
		updateMenu(true);
		isDragging = false;
		isDraggingRefPoint = false;
		isDraggingPieceButton = false;
		isSelecting = false;
	}
	
	public void setImageCache(ImageDrawCache cache) {
		this.cache = cache;
	}

	public void setLockPieces(java.util.List<PipAnimateFramePiece> p) {
		lockPieces = p;
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("水平翻转") {
			public void run() {
				hflipPiece();
			}
		});
		mgr.add(new Action("垂直翻转") {
			public void run() {
				vflipPiece();
			}
		});
		mgr.add(new Action("旋转90度") {
			public void run() {
				rotatePiece();
			}
		});
		if (popMenu != null) {
			popMenu.dispose();
		}
		popMenu = mgr.createContextMenu(this);
		updateMenu(true);
	}
	
	public void setRefFrame(PipAnimateFrame f) {
		refFrame = f;
	}
	
	private void updateMenu(boolean force) {
	    if(copiedPiece!=null && copiedPiece.length>0){
	    	pasteAction.setEnabled(true);
	    }else{
	    	pasteAction.setEnabled(false);
	    }
	    if (force) {
	        setMenu(popMenu);
	    }
	}
	
	public PipAnimateFramePiece[] getSelectedPiece() {
	    PipAnimateFramePiece[] ret = new PipAnimateFramePiece[selectedPiece.length];
	    for (int i = 0; i < selectedPiece.length; i++) {
	        ret[i] = ((PipAnimateFrame)input).getPiece(selectedPiece[i]);
	    }
		return ret;
	}
	
	private PipAnimateFramePiece getFirstSelectedPiece() {
	    if (selectedPiece.length == 0) {
	        return null;
	    }
	    return ((PipAnimateFrame)input).getPiece(selectedPiece[0]);
	}
	
	private int findPieceSelected(int id) {
	    for (int i = 0; i < selectedPiece.length; i++) {
	        if (id == selectedPiece[i]) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	private boolean isPieceSelected(int id) {
	    return findPieceSelected(id) != -1;
	}
	
	public void setPieceSelected(int id, boolean flag) {
	    int index = findPieceSelected(id);
	    if (flag) {
	        if (index != -1) {
	            return;
	        }
	        int[] newarr = new int[selectedPiece.length + 1];
	        System.arraycopy(selectedPiece, 0, newarr, 1, selectedPiece.length);
	        newarr[0] = id;
	        selectedPiece = newarr;
	        fireSelectionChanged();
	    } else {
	        if (index == -1) {
	            return;
	        }
            int[] newarr = new int[selectedPiece.length + 1];
            System.arraycopy(selectedPiece, 0, newarr, 0, index);
            System.arraycopy(selectedPiece, index + 1, newarr, index, selectedPiece.length - index - 1);
            selectedPiece = newarr;
            fireSelectionChanged();
	    }
	}
	
	private Polygon getPieceBounds(PipAnimateFramePiece piece, boolean isScreenBounds) {
		double[][] points = piece.getBounds();
		Rectangle size = getBounds();
		if (isScreenBounds) {
			points = GLUtils.mul(points, new double[][] {
					{ ratio, 0, 0 },
					{ 0, ratio, 0 },
					{ size.width / 2 + paintOffset.x, size.height / 2 + paintOffset.y, 1 }
			});
		}
		Polygon poly = new Polygon(new int[] { (int)points[0][0], (int)points[1][0], (int)points[2][0], (int)points[3][0] },
				new int[] { (int)points[0][1], (int)points[1][1], (int)points[2][1], (int)points[3][1] }, 4);
		return poly;
	}
	
	private Point getPieceAnchor(PipAnimateFramePiece piece, boolean isScreenBounds) {
		int[] apos = piece.getAnchorPos();
		Rectangle size = getBounds();
		if (isScreenBounds) {
			apos[0] = (int)(apos[0] * ratio + size.width / 2 + paintOffset.x);
			apos[1] = (int)(apos[1] * ratio + size.height / 2 + paintOffset.y);
		}
		return new Point(apos[0], apos[1]);
	}
	
	private int getPieceAt(int x, int y) {
		PipAnimateFrame frame = (PipAnimateFrame)input;
		for (int i = frame.getPieceCount() - 1; i >= 0; i--) {
			PipAnimateFramePiece piece = frame.getPiece(i);
			if(piece.getVisible()==false){
				continue;
			}
			if (this.lockPieces != null && !this.lockPieces.contains(piece)) {
				continue;
			}
			Polygon poly = getPieceBounds(piece, true);
			if (poly.contains(x, y)) {
				return i;
			}
		}
		return -1;
	}
	
	private Point calcCoord(Point p) {
		Point size = getSize();
		int x = (int)(fitGrid(p.x - size.x / 2 - paintOffset.x) / ratio);
		int y = (int)(fitGrid(p.y - size.y / 2 - paintOffset.y) / ratio);
		return new Point(x, y);
	}
	
	private void checkSelectedPieces() {
	    selectedPiece = new int[0];
        if (input == null) {
            return;
        }
	    selStart = calcCoord(selStart);
	    selEnd = calcCoord(selEnd);
	    int x = selStart.x;
	    int y = selStart.y;
	    int w = selEnd.x - selStart.x;
	    int h = selEnd.y - selStart.y;
        if (w < 0) {
            x += w;
            w = -w;
        }
        if (h < 0) {
            y += h;
            h = -h;
        }
        Rectangle selRect = new Rectangle(x, y, w, h);

        PipAnimateFrame frame = (PipAnimateFrame)input;
	    for (int i = 0; i < frame.getPieceCount(); i++) {
	        PipAnimateFramePiece piece = frame.getPiece(i);
	        if(piece.getVisible()==false){
	        	continue;
	        }
	        if (this.lockPieces != null && !this.lockPieces.contains(piece)) {
				continue;
			}
	        
	        Polygon pbounds = getPieceBounds(piece, false);
	        if (pbounds.intersects(selRect.x, selRect.y, selRect.width, selRect.height)) {
	            setPieceSelected(i, true);
	        }
	    }
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
	public AnimateFrameEditor(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND, GLUtils.glEnabled);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
			    mouseInRefPointArea = inRefPointAnchor(e.x, e.y);
				if (isDragging && input != null) {
					int offx = (int)((e.x - dragStartPoint.x) / ratio);
					int offy = (int)((e.y - dragStartPoint.y) / ratio);
					offx += dragStartPos.x;
					offy += dragStartPos.y;
					offx -= getFirstSelectedPiece().getDx();
					offy -= getFirstSelectedPiece().getDy();
					PipAnimateFramePiece[] sels = getSelectedPiece();
					for (int i = 0; i < sels.length; i++) {
					    sels[i].setDx(sels[i].getDx() + offx);
                        sels[i].setDy(sels[i].getDy() + offy);
					}
				} else if (isSelecting) {
				    selEnd = new Point(e.x, e.y);
				} else if (isDraggingRefPoint) {
				    int offx = (int)((e.x - dragStartPoint.x) / ratio);
                    int offy = (int)((e.y - dragStartPoint.y) / ratio);
                    offx += dragStartPos.x;
                    offy += dragStartPos.y;
                    refPoint.x = offx;
                    refPoint.y = offy;
				} else if (isDraggingPieceButton) {
					if (selectedPiece.length == 0) {
						isDraggingPieceButton = false;
					} else {
						PipAnimateFramePiece piece = getSelectedPiece()[0];
						
	                    // 拖动的点位置保存在draggingButtonIndex：0 - 基准点、1 - 旋转点、2 - 缩放、3 - x方向缩放、4 - y方向缩放
	                    // 开始拖动时图块的参数保存在draggingStartPieceParam;
	                    // 开始拖动时图块各按钮点的位置保存在draggingStartPieceButtonPos
	                    if (draggingButtonIndex == 0) {
	                    	// 拖动基准点
	                    	double offx = (e.x - dragStartPoint.x) / ratio;
	                    	double offy = (e.y - dragStartPoint.y) / ratio;
	                    	
	                    	// 按旋转角度反向旋转
	                    	double angle = ((-piece.rotate % 360) + 360) % 360;
	                		angle = angle * Math.PI / 180;
	                		double sin = Math.sin(angle);
	                		double cos = Math.cos(angle);
	                		double[][] points = new double[][] {
	                				{ offx, offy, 1 }
	                		};
	                		points = GLUtils.mul(points, new double[][] {
	                				{ cos, -sin, 0 },
	                				{ sin, cos, 0 },
	                				{ 0, 0, 1}
	                		});
	                		
	                		// 缩放
	                		offx = points[0][0] * 100 / piece.scalex;
	                		offy = points[0][1] * 100 / piece.scaley;
	                		
	                		// 改变参考点
	                		PipImage image = piece.getParent().getParent().getSourceImage(piece.getImageID());
	                		PipImageData id = image.getImageData(piece.getFrame());
	                		if ((piece.getTransition() & 2) == 0) {
	                			id.anchorx = (int)(draggingStartPieceParam[0] + (int)offx);
	                		} else {
	                			id.anchorx = (int)(draggingStartPieceParam[0] - (int)offx);
	                		}
	                		if ((piece.getTransition() & 1) == 0) {
	                			id.anchory = (int)(draggingStartPieceParam[1] + (int)offy);
	                		} else {
	                			id.anchory = (int)(draggingStartPieceParam[1] - (int)offy);
	                		}
	                		piece.setDx((int)(draggingStartPieceParam[5] + (int)offx));
	                		piece.setDy((int)(draggingStartPieceParam[6] + (int)offy));
	                    } else if (draggingButtonIndex == 1) {
	                    	// 旋转
	                    	double offx = e.x - dragStartPoint.x;
	                    	double offy = e.y - dragStartPoint.y;
	                    	double anglex = draggingStartPieceButtonPos[1].x + offx - draggingStartPieceButtonPos[0].x;
	                    	double angley = draggingStartPieceButtonPos[1].y + offy - draggingStartPieceButtonPos[0].y;
	                    	angley = -angley;
	                    	double dist = Math.sqrt(anglex * anglex + angley * angley);
	                    	if (dist > 0.00000001) {
	                    		// 计算新的角度0-360
		                    	double sin = angley / dist;
		                    	double angle = Math.asin(sin);
		                    	if (anglex < 0) {
		                    		angle = Math.PI - angle;
		                    	}
		                    	angle += Math.PI / 2;
		                    	int newAngle = (int)(angle * 180 / Math.PI);
		                    	newAngle = (newAngle + 720) % 360;
		                    	
		                    	// 计算老的角度0-360
		                    	int oldAngle = ((piece.rotate % 360) + 360) % 360;
		                    	
		                    	// 计算拖动偏移角度
		                    	int angleDist = (((newAngle - oldAngle) % 360) + 360) % 360;
		                    	
		                    	// 如果偏移角度<180，表示逆时针旋转，否则表示顺时针旋转
		                    	if (angleDist < 180) {
		                    		piece.rotate += angleDist;
			                    	if ((e.stateMask & SWT.SHIFT) != 0) {
			                    		piece.rotate -= piece.rotate % 10;
			                    	}
		                    	} else {
		                    		piece.rotate -= 360 - angleDist;
			                    	if ((e.stateMask & SWT.SHIFT) != 0) {
			                    		piece.rotate -= piece.rotate % 10;
			                    	}
		                    	}
	                    	}
	                    } else if (draggingButtonIndex == 2) {
	                    	// 水平和垂直方向同时缩放
	                    	double oldoffx = draggingStartPieceButtonPos[2].x - draggingStartPieceButtonPos[0].x;
	                    	double oldoffy = draggingStartPieceButtonPos[2].y - draggingStartPieceButtonPos[0].y;
	                    	double newoffx = e.x - draggingStartPieceButtonPos[0].x;
	                    	double newoffy = e.y - draggingStartPieceButtonPos[0].y;
	                    	double olddist = Math.sqrt(oldoffx * oldoffx + oldoffy * oldoffy);
	                    	double newdist = Math.sqrt(newoffx * newoffx + newoffy * newoffy);
	                    	if (olddist < 0.01) {
	                    		olddist = 0.01;
	                    	}
	                    	if (newdist < 0.01) {
	                    		newdist = 0.01;
	                    	}
	                    	piece.scalex = (int)(draggingStartPieceParam[3] * newdist / olddist);
	                    	if ((e.stateMask & SWT.SHIFT) != 0) {
	                    		piece.scalex -= piece.scalex % 10;
	                    	}
	                    	if (piece.scalex < 1) {
	                    		piece.scalex = 1;
	                    	}
	                    	piece.scaley = (int)(draggingStartPieceParam[4] * newdist / olddist);
	                    	if ((e.stateMask & SWT.SHIFT) != 0) {
	                    		piece.scaley -= piece.scaley % 10;
	                    	}
	                    	if (piece.scaley < 1) {
	                    		piece.scaley = 1;
	                    	}
	                    } else if (draggingButtonIndex == 3) {
	                    	// 水平方向缩放
	                    	double oldoffx = draggingStartPieceButtonPos[3].x - draggingStartPieceButtonPos[0].x;
	                    	double oldoffy = draggingStartPieceButtonPos[3].y - draggingStartPieceButtonPos[0].y;
	                    	double newoffx = e.x - draggingStartPieceButtonPos[0].x;
	                    	double newoffy = e.y - draggingStartPieceButtonPos[0].y;
	                    	double olddist = Math.sqrt(oldoffx * oldoffx + oldoffy * oldoffy);
	                    	double newdist = Math.sqrt(newoffx * newoffx + newoffy * newoffy);
	                    	if (olddist < 0.01) {
	                    		olddist = 0.01;
	                    	}
	                    	if (newdist < 0.01) {
	                    		newdist = 0.01;
	                    	}
	                    	piece.scalex = (int)(draggingStartPieceParam[3] * newdist / olddist);
	                    	if ((e.stateMask & SWT.SHIFT) != 0) {
	                    		piece.scalex -= piece.scalex % 10;
	                    	}
	                    	if (piece.scalex < 1) {
	                    		piece.scalex = 1;
	                    	}
	                    } else if (draggingButtonIndex == 4) {
	                    	// 垂直方向缩放
	                    	double oldoffx = draggingStartPieceButtonPos[4].x - draggingStartPieceButtonPos[0].x;
	                    	double oldoffy = draggingStartPieceButtonPos[4].y - draggingStartPieceButtonPos[0].y;
	                    	double newoffx = e.x - draggingStartPieceButtonPos[0].x;
	                    	double newoffy = e.y - draggingStartPieceButtonPos[0].y;
	                    	double olddist = Math.sqrt(oldoffx * oldoffx + oldoffy * oldoffy);
	                    	double newdist = Math.sqrt(newoffx * newoffx + newoffy * newoffy);
	                    	if (olddist < 0.01) {
	                    		olddist = 0.01;
	                    	}
	                    	if (newdist < 0.01) {
	                    		newdist = 0.01;
	                    	}
	                    	piece.scaley = (int)(draggingStartPieceParam[4] * newdist / olddist);
	                    	if ((e.stateMask & SWT.SHIFT) != 0) {
	                    		piece.scaley -= piece.scaley % 10;
	                    	}
	                    	if (piece.scaley < 1) {
	                    		piece.scaley = 1;
	                    	}
	                    }
					}
				}
				lastPoint = calcCoord(new Point(e.x, e.y));
				redraw();
				updateMenu(false);
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
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
				    if (pieceButtonPos != null) {
				    	for (int i = 0; i < pieceButtonPos.length; i++) {
				    		if (pieceButtonPos[i].contains(e.x, e.y)) {
				    			if (i == 5) {
				    				// 这是颜色按钮
				    				PipAnimateFramePiece piece = getSelectedPiece()[0];
				    				int initColor = piece.color;
				    				int clr = PieceColorDialog.choose(initColor, AnimateFrameEditor.this);
				    				piece.color = clr;
				    				redraw();
				    				if (clr != initColor) {
				    					fireContentChanged();
				    				}
				    				return;
				    			}
				    			isDraggingPieceButton = true;
				    			dragStartPoint = new Point(e.x, e.y);
				    			draggingStartPieceButtonPos = pieceButtonPos;
				    			PipAnimateFramePiece piece = getSelectedPiece()[0];
				    			PipImage image = piece.getParent().getParent().getSourceImage(piece.getImageID());
				    			PipImageData id = image.getImageData(piece.getFrame());
				    			draggingStartPieceParam = new int[] { id.anchorx, id.anchory, piece.rotate, piece.scalex, piece.scaley, piece.getDx(), piece.getDy() };
				    			draggingButtonIndex = i;
				    			redraw();
				    			return;
				    		}
				    	}
				    }
				    if (input != null) {
                        int clickedPiece = getPieceAt(e.x, e.y);
    				    if ((e.stateMask & SWT.SHIFT) != 0) {
    				        if (clickedPiece != -1) {
    				            if (isPieceSelected(clickedPiece)) {
    				                setPieceSelected(clickedPiece, false);
    				            } else {
    				                setPieceSelected(clickedPiece, true);
    				            }
    				        }
    				    } else {
    				        if (clickedPiece != -1) {
    				            if (!isPieceSelected(clickedPiece)) {
    				                selectedPiece = new int[] { clickedPiece };
    				                fireSelectionChanged();
    				            }
                                isDragging = true;
                                dragStartPoint = new Point(e.x, e.y);
                                dragStartPos = new Point(getFirstSelectedPiece().getDx(), getFirstSelectedPiece().getDy()); 
        					} else {
        						selectedPiece = new int[0];
        						fireSelectionChanged();
        						isSelecting = true;
        						selStart = new Point(e.x, e.y);
        						selEnd = new Point(e.x, e.y);
        					}
    				    }
                        redraw();
                        updateMenu(false);
				    }
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					if (isDragging && input != null) {
						isDragging = false;
						int offx = (int)((e.x - dragStartPoint.x) / ratio);
						int offy = (int)((e.y - dragStartPoint.y) / ratio);
						offx += dragStartPos.x;
						offy += dragStartPos.y;
						offx -= getFirstSelectedPiece().getDx();
						offy -= getFirstSelectedPiece().getDy();
						PipAnimateFramePiece[] sels = getSelectedPiece();
						for (int i = 0; i < sels.length; i++) {
						    sels[i].setDx(sels[i].getDx() + offx);
	                        sels[i].setDy(sels[i].getDy() + offy);
						}
						redraw();
						offx = (int)((e.x - dragStartPoint.x) / ratio);
						offy = (int)((e.y - dragStartPoint.y) / ratio);
						if (offx != 0 || offy != 0) {
							allOffx = offx;
							allOffy = offy;
							fireContentChanged();
						}
					} else if (isSelecting && input != null) {
					    selEnd = new Point(e.x, e.y);
					    checkSelectedPieces();
					    fireSelectionChanged();
					    isSelecting = false;
					    redraw();
					} else if (isDraggingRefPoint) {
					    isDraggingRefPoint = false;
					    redraw();
					} else if (isDraggingPieceButton) {
						isDraggingPieceButton = false;
						redraw();
						int offx = (int)((e.x - dragStartPoint.x) / ratio);
						int offy = (int)((e.y - dragStartPoint.y) / ratio);
						if (offx != 0 || offy != 0) {
							fireContentChanged();
						}
					}
	                updateMenu(false);
				}
			}
		});

		MenuManager mgr = new MenuManager();
		
		//copy
		mgr.add(new Action("复制") {
			public void run() {
				copy();
			}
		});
		if(pasteAction==null){
			pasteAction = new Action("粘贴") {
				public void run() {
					paste();
				}
			};
		}
		pasteAction.setEnabled(false);
		mgr.add(pasteAction);
		//copy end
		
		MenuManager layoutMenu = new MenuManager("叠放次序");
		mgr.add(layoutMenu);
		layoutMenu.add(new Action("置于顶层") {
			public void run() {
				bringTopPiece();
			}
		});
		layoutMenu.add(new Action("置于底层") {
			public void run() {
				bringBottomPiece();
			}
		});
		layoutMenu.add(new Action("上移一层") {
			public void run() {
				bringUpPiece();
			}
		});
		layoutMenu.add(new Action("下移一层") {
			public void run() {
				bringDownPiece();
			}
		});

		mgr.add(new Action("删除图块") {
			public void run() {
				deletePiece();
			}
		});
		mgr.add(new Action("水平翻转") {
			public void run() {
				hflipPiece();
			}
		});
		mgr.add(new Action("垂直翻转") {
			public void run() {
				vflipPiece();
			}
		});
		mgr.add(new Action("旋转90度") {
			public void run() {
				rotatePiece();
			}
		});
		mgr.add(new Action("替换相同图块") {
			public void run() {
				replaceSamePiece();
			}
		});
		mgr.add(new Action("替换同图片图块") {
			public void run() {
				replaceSameImagePiece();
			}
		});
		mgr.add(new Action("在所有帧中替换此图块") {
            public void run() {
                replaceAllSamePiece();
            }
        });
		mgr.add(new Action("在所有帧中删除此图块") {
            public void run() {
                deleteAllSamePiece();
            }
        });
		mgr.add(new Separator());
		mgr.add(new Action("自动选中相同图片的所有图块") {
            public void run() {
                autoSelectSameImagePiece();
            }
        });
		if (popMenu != null) {
			popMenu.dispose();
		}
		popMenu = mgr.createContextMenu(this);
		
		anchorButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/anchor.gif");
		colorButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/color.gif");
		rotateButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/rotate.gif");
		scaleButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/scale.gif");
		scalexButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/scalex.gif");
		scaleyButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/scaley.gif");
	}

	protected void paste() {
		PipAnimateFrame frameData = (PipAnimateFrame)input;
		for(int idx:copiedPiece){
			PipAnimateFramePiece piece = (PipAnimateFramePiece) copySource.getPiece(idx).clone();
			frameData.addPiece(piece);
		}
		redraw();
		fireContentChanged();
	}

	protected void copy() {
		copiedPiece = Arrays.copyOf(selectedPiece, selectedPiece.length);
		Arrays.sort(copiedPiece);
		copySource =  (PipAnimateFrame)input;
	}

	private void deletePiece() {
		if(!allowDeletePiece ){
			return;
		}
		PipAnimateFrame frameData = (PipAnimateFrame)input;
		if (frameData == null || selectedPiece.length == 0) {
			return;
		}
		Arrays.sort(selectedPiece);
		for (int i = selectedPiece.length - 1; i >= 0; i--) {
		    frameData.removePiece(selectedPiece[i]);
		}
		selectedPiece = new int[0];
		redraw();
		fireContentChanged();
	}
	
	private void hflipPiece() {
		PipAnimateFramePiece[] piece = getSelectedPiece();
		if (piece.length == 0) {
			return;
		}
		for (int i = 0; i < piece.length; i++) {
    		int trans = piece[i].getTransition();
    		trans = PipImage.hflip(trans);
    		piece[i].setTransition(trans);
    		if (piece[i].rotate != 0) {
    			int angle = ((piece[i].rotate % 360) + 360) % 360;
	        	piece[i].rotate = (piece[i].rotate - angle) + (360 - angle);
	        }
		}
		redraw();
		fireContentChanged();
	}
	
	private void vflipPiece() {
	    PipAnimateFramePiece[] piece = getSelectedPiece();
        if (piece.length == 0) {
            return;
        }
        for (int i = 0; i < piece.length; i++) {
            int trans = piece[i].getTransition();
            trans = PipImage.vflip(trans);
            piece[i].setTransition(trans);
            if (piece[i].rotate != 0) {
            	int angle = ((piece[i].rotate % 360) + 360) % 360;
	        	piece[i].rotate = (piece[i].rotate - angle) + (360 - angle);
	        }
        }
		redraw();
		fireContentChanged();
	}
	
	private void rotatePiece() {
	    PipAnimateFramePiece[] piece = getSelectedPiece();
        if (piece.length == 0) {
            return;
        }
        for (int i = 0; i < piece.length; i++) {
            int trans = piece[i].getTransition();
            trans = PipImage.rotate90(trans);
    		piece[i].setTransition(trans);
        }
		redraw();
		fireContentChanged();
	}
	
	private void replaceSamePiece() {
		PipAnimateFramePiece piece = getFirstSelectedPiece();
		if (piece == null) {
			return;
		}
		int newImage = owner.getSelectedImage();
		int newPiece = owner.getSelectedPiece();
		if (newImage == -1 || newPiece == -1) {
			return;
		}
		int oldImage = piece.getImageID();
		int oldPiece = piece.getFrame();
		PipAnimateFrame frameObj = (PipAnimateFrame)input;
		for (int i = 0; i < frameObj.getPieceCount(); i++) {
			piece = frameObj.getPiece(i);
			if (piece.getImageID() == oldImage && piece.getFrame() == oldPiece) {
				piece.setImageID(newImage);
				piece.setFrame(newPiece);
			}
		}
		redraw();
		fireContentChanged();
	}
	
    private void replaceAllSamePiece() {
        PipAnimateFramePiece piece = getFirstSelectedPiece();
        if (piece == null) {
            return;
        }
        int newImage = owner.getSelectedImage();
        int newPiece = owner.getSelectedPiece();
        if (newImage == -1 || newPiece == -1) {
            return;
        }
        int oldImage = piece.getImageID();
        int oldPiece = piece.getFrame();
        PipAnimateFrame frameObj = (PipAnimateFrame)input;
        PipAnimateSet aniSet = frameObj.getParent();
        for (int k = 0; k < aniSet.getFrameCount(); k++) {
            PipAnimateFrame thisFrame = aniSet.getFrame(k);
            for (int i = 0; i < thisFrame.getPieceCount(); i++) {
                piece = thisFrame.getPiece(i);
                if (piece.getImageID() == oldImage && piece.getFrame() == oldPiece) {
                    piece.setImageID(newImage);
                    piece.setFrame(newPiece);
                }
            }
        }
        redraw();
        fireContentChanged();
    }
    
    private void deleteAllSamePiece() {
        PipAnimateFramePiece piece = getFirstSelectedPiece();
        if (piece == null) {
            return;
        }
        int oldImage = piece.getImageID();
        int oldPiece = piece.getFrame();
        PipAnimateFrame frameObj = (PipAnimateFrame)input;
        PipAnimateSet aniSet = frameObj.getParent();
        for (int k = 0; k < aniSet.getFrameCount(); k++) {
            PipAnimateFrame thisFrame = aniSet.getFrame(k);
            for (int i = 0; i < thisFrame.getPieceCount(); i++) {
                piece = thisFrame.getPiece(i);
                if (piece.getImageID() == oldImage && piece.getFrame() == oldPiece) {
                	thisFrame.removePiece(i);
                	i--;
                }
            }
        }
        redraw();
        fireContentChanged();
    }

    private void replaceSameImagePiece() {
		PipAnimateFramePiece piece = getFirstSelectedPiece();
		if (piece == null) {
			return;
		}
		int newImage = owner.getSelectedImage();
		if (newImage == -1) {
			return;
		}
		int oldImage = piece.getImageID();
		PipAnimateFrame frameObj = (PipAnimateFrame)input;
		PipImage newImgObj = frameObj.getParent().getSourceImage(newImage);
		int newCount = newImgObj.getImgCount() * newImgObj.getImagePalettes().size();
		for (int i = 0; i < frameObj.getPieceCount(); i++) {
			piece = frameObj.getPiece(i);
			if (piece.getImageID() == oldImage && piece.getFrame() < newCount) {
				piece.setImageID(newImage);
			}
		}
		redraw();
		fireContentChanged();	
	}
	
	private void bringTopPiece() {
		if (input == null || selectedPiece.length == 0) {
			return;
		}
		PipAnimateFrame frameObj = (PipAnimateFrame)input;
		int count = frameObj.getPieceCount();
		if (selectedPiece[0] == count - 1) {
			return;
		}
		frameObj.swapPiece(selectedPiece[0], count);
		selectedPiece = new int[] { count - 1 };
		redraw();
		fireContentChanged();
	}
	
	private void bringBottomPiece() {
        if (input == null || selectedPiece.length == 0) {
            return;
        }
		PipAnimateFrame frameObj = (PipAnimateFrame)input;
		if (selectedPiece[0] == 0) {
			return;
		}
		frameObj.swapPiece(selectedPiece[0], -1);
		selectedPiece = new int[] { 0 };
		redraw();
		fireContentChanged();
	}

	public void bringUpPiece() {
	    if (input == null || selectedPiece.length == 0) {
            return;
        }
		PipAnimateFrame frameObj = (PipAnimateFrame)input;
		int count = frameObj.getPieceCount();
		
		int[] arr = new int[selectedPiece.length];
		System.arraycopy(selectedPiece, 0, arr, 0, arr.length);
		Arrays.sort(arr);
		if (arr[arr.length - 1] == count - 1) {
			return;
		}
		for (int i = arr.length - 1; i >= 0; i--) {
			frameObj.swapPiece(arr[i], arr[i] + 1);
			selectedPiece[i]++;
		}

		redraw();
		fireContentChanged();
	}
	
	public void bringDownPiece() {
	    if (input == null || selectedPiece.length == 0) {
            return;
        }
        PipAnimateFrame frameObj = (PipAnimateFrame)input;
        
        int[] arr = new int[selectedPiece.length];
		System.arraycopy(selectedPiece, 0, arr, 0, arr.length);
		Arrays.sort(arr);
		if (arr[0] == 0) {
			return;
		}
		for (int i = 0; i < arr.length; i++) {
			frameObj.swapPiece(arr[i], arr[i] - 1);
			selectedPiece[i]--;
		}
		
        redraw();
        fireContentChanged();
	}

	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
		int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
		gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
		gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
		if (isDraggingRefPoint || (mouseInRefPointArea && !isDragging && !isSelecting && !isDraggingPieceButton)) {
		    gc.drawRectangle(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
		}

		if (input != null) {
			drawSelection(gc);
		}
		
		drawScreenFrame(gc);
		
		gc.setForeground(invert(getBackground()));
		
		String coordStr;
		if (isDragging) {
			coordStr = getFirstSelectedPiece().getDx() + "," + getFirstSelectedPiece().getDy();
		} else if (isDraggingRefPoint) {
		    coordStr = refPoint.x + "," + refPoint.y;
		} else if (lastPoint != null) {
			coordStr = lastPoint.x + "," + lastPoint.y;
		} else {
			return;
		}
		Point ts = gc.textExtent(coordStr);
		gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
		gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
		
		// 显示选中图块的位置和大小
		PipAnimateFramePiece selPiece = this.getFirstSelectedPiece();
		if (selPiece != null) {
			String str = selPiece.getDx() + "," + selPiece.getDy() + "," + selPiece.getWidth() + "," + selPiece.getHeight();
			ts = gc.textExtent(str);
			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(str, 4, size.y - ts.y - 5);
		}
		
		String info1 = "HOME-第一块 END-最后一块 PGUP-上一块 PGDN-下一块";
		ts = gc.textExtent(info1);
		gc.drawText(info1, size.x / 2 - ts.x / 2, size.y - ts.y - 5);
		String info2 = "R-上移一层 E-移到最顶层 F-下移一层 D-移到最底层";
        ts = gc.textExtent(info2);
        gc.drawText(info2, size.x / 2 - ts.x / 2, size.y - ts.y * 2 - 5);
	}
	
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		gc.setColor(0xFF, 0xFF, 0x99, 0x00);
		int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
		gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
		gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
		if (isDraggingRefPoint || (mouseInRefPointArea && !isDragging && !isSelecting && !isDraggingPieceButton)) {
		    gc.drawRect(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
		}

		if (input != null) {
			drawSelection(gc);
		}
		
		drawScreenFrame(gc);
		
		Color clr = invert(getBackground());
		gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
		
		String coordStr;
		if (isDragging) {
			coordStr = getFirstSelectedPiece().getDx() + "," + getFirstSelectedPiece().getDy();
		} else if (isDraggingRefPoint) {
		    coordStr = refPoint.x + "," + refPoint.y;
		} else if (lastPoint != null) {
			coordStr = lastPoint.x + "," + lastPoint.y;
		} else {
			return;
		}
		Point ts = gc.textExtent(coordStr);
		gc.drawRect(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
		gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
		
		// 显示选中图块的位置和大小
		PipAnimateFramePiece selPiece = this.getFirstSelectedPiece();
		if (selPiece != null) {
			String str = selPiece.getDx() + "," + selPiece.getDy() + "," + selPiece.getWidth() + "," + selPiece.getHeight();
			ts = gc.textExtent(str);
			gc.drawRect(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(str, 4, size.y - ts.y - 5);
		}
		
		String info1 = "HOME-第一块 END-最后一块 PGUP-上一块 PGDN-下一块";
		ts = gc.textExtent(info1);
		gc.drawText(info1, size.x / 2 - ts.x / 2, size.y - ts.y - 5);
		String info2 = "R-上移一层 E-移到最顶层 F-下移一层 D-移到最底层";
        ts = gc.textExtent(info2);
        gc.drawText(info2, size.x / 2 - ts.x / 2, size.y - ts.y * 2 - 5);
	}

	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
		} else {
			if (refFrame != null) {
				refFrame.draw(gc, size.x / 2 + paintOffset.x, size.y / 2 + paintOffset.y, ratio);
			}
			PipAnimateFrame frame = (PipAnimateFrame)input;
			frame.draw(gc, size.x / 2 + paintOffset.x, size.y / 2 + paintOffset.y, ratio, cache);
		}
	}
	
	protected void paintInput(GLGraphics gc) {
		Point size = getSize();
		if (input == null) {
		} else {
			if (refFrame != null) {
				refFrame.draw(gc, size.x / 2 + paintOffset.x, size.y / 2 + paintOffset.y, ratio, cache);
			}
			PipAnimateFrame frame = (PipAnimateFrame)input;
			frame.draw(gc, size.x / 2 + paintOffset.x, size.y / 2 + paintOffset.y, ratio, cache);
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
   
    private void drawScreenFrame(GLGraphics gc) {
        Point size = getSize();
        int sw = Settings.screenWidth;
        int sh = Settings.screenHeight;
        Rectangle screen = new Rectangle(-sw / 2, -sh / 2, sw, sh);
        screen = zoom(screen);
        screen.x += size.x / 2 + paintOffset.x;
        screen.y += size.y / 2 + paintOffset.y;
        Color clr = invert(getBackground());
		gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
        gc.drawRect(screen);
    }

	private void drawSelection(GC gc) {
		PipAnimateFramePiece[] piece = getSelectedPiece();
		Point size = getSize();
		for (int i = 0; i < piece.length; i++) {
			int ax = (int)(piece[i].getDx() * ratio) + size.x / 2;
			int ay = (int)(piece[i].getDy() * ratio) + size.y / 2;
			int aw = (int)(piece[i].getWidth() * ratio);
			int ah = (int)(piece[i].getHeight() * ratio);
			if (i == 0) {
			    gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
			} else {
			    gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
			}
			gc.drawRectangle(ax + paintOffset.x, ay + paintOffset.y, aw, ah);
		}
		if (isSelecting) {
		    gc.setXORMode(true);
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            int x = selStart.x;
            int y = selStart.y;
            int w = selEnd.x - selStart.x;
            int h = selEnd.y - selStart.y;
            if (w < 0) {
                x = x + w;
                w = -w;
            }
            if (h < 0) {
                y = y + h;
                h = -h;
            }
            gc.drawRectangle(x, y, w, h);
            gc.setXORMode(false);
		}
	}
	
	// 计算当前选中帧的几个按钮位置：参考点，旋转，缩放，颜色
	private Rectangle[] calcPieceButtonPos(PipAnimateFramePiece piece) {
		Polygon pbounds = getPieceBounds(piece, true);

		// 参考点按钮就在参考点上
		Point panchor = getPieceAnchor(piece, true);
		Rectangle anchorRect = new Rectangle(panchor.x - 8, panchor.y - 8, 16, 16);

		// 旋转按钮，在参考点Y位置下方50 * scale像素
		double[][] ptemp = new double[][] {
				{ 0, piece.scaley * ratio * 50 / 100, 1 }
		};
		double angle = ((piece.rotate % 360) + 360) % 360;
		angle = piece.rotate * Math.PI / 180;
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		ptemp = GLUtils.mul(ptemp, new double[][] {
				{ cos, -sin, 0 },
				{ sin, cos, 0 },
				{ 0, 0, 1}
		});
		Rectangle rotateRect = new Rectangle((int)ptemp[0][0] + panchor.x - 8, (int)ptemp[0][1] + panchor.y - 8, 16, 16);
		
		// 缩放按钮，在右下角顶点上
		Rectangle scaleRect = new Rectangle(pbounds.xpoints[2] - 8, pbounds.ypoints[2] - 8, 16, 16);

		// 水平缩放按钮，在右边中点上
		Rectangle scalexRect = new Rectangle((pbounds.xpoints[1] + pbounds.xpoints[2]) / 2 - 8, (pbounds.ypoints[1] + pbounds.ypoints[2]) / 2 - 8, 16, 16);

		// 垂直缩放按钮，在下边中点上
		Rectangle scaleyRect = new Rectangle((pbounds.xpoints[2] + pbounds.xpoints[3]) / 2 - 8, (pbounds.ypoints[2] + pbounds.ypoints[3]) / 2 - 8, 16, 16);
		
		// 颜色按钮，在参考点Y位置上方50像素
		ptemp = new double[][] {
				{ 0, -50, 1 }
		};
		Rectangle colorRect = new Rectangle((int)ptemp[0][0] + panchor.x - 8, (int)ptemp[0][1] + panchor.y - 8, 16, 16);
		return new Rectangle[] { anchorRect, rotateRect, scaleRect, scalexRect, scaleyRect, colorRect };
	}
	
	private void drawSelection(GLGraphics gc) {
		PipAnimateFramePiece[] piece = getSelectedPiece();
		pieceButtonPos = null;
		for (int i = 0; i < piece.length; i++) {
			if (i == 0) {
			    gc.setColor(0xFFFF0000);
			} else {
				gc.setColor(0xFF0000FF);
			}
			Polygon pbounds = getPieceBounds(piece[i], true);
			gc.drawLine(pbounds.xpoints[0], pbounds.ypoints[0], pbounds.xpoints[1], pbounds.ypoints[1]);
			gc.drawLine(pbounds.xpoints[1], pbounds.ypoints[1], pbounds.xpoints[2], pbounds.ypoints[2]);
			gc.drawLine(pbounds.xpoints[2], pbounds.ypoints[2], pbounds.xpoints[3], pbounds.ypoints[3]);
			gc.drawLine(pbounds.xpoints[3], pbounds.ypoints[3], pbounds.xpoints[0], pbounds.ypoints[0]);
			if (!isSelecting && piece.length == 1 && !(piece[0] instanceof PipAni4AniFramePiece)) {
				// 绘制当前帧的参考点、旋转、缩放、颜色操作控件
				pieceButtonPos = calcPieceButtonPos(piece[i]);
				gc.drawTexture(GLUtils.loadImage(anchorButtonImage), 0, 0, pieceButtonPos[0].x, pieceButtonPos[0].y);
				gc.drawTexture(GLUtils.loadImage(rotateButtonImage), 0, 0, pieceButtonPos[1].x, pieceButtonPos[1].y);
				gc.drawTexture(GLUtils.loadImage(scaleButtonImage), 0, 0, pieceButtonPos[2].x, pieceButtonPos[2].y);
				gc.drawTexture(GLUtils.loadImage(scalexButtonImage), 0, 0, pieceButtonPos[3].x, pieceButtonPos[3].y);
				gc.drawTexture(GLUtils.loadImage(scaleyButtonImage), 0, 0, pieceButtonPos[4].x, pieceButtonPos[4].y);
				gc.drawTexture(GLUtils.loadImage(colorButtonImage), 0, 0, pieceButtonPos[5].x, pieceButtonPos[5].y);
				
				// 绘制当前拖动点的参数
				if (isDraggingPieceButton && draggingButtonIndex == 1) {
					String text = String.valueOf(piece[i].rotate);
					Point ts = gc.textExtent(text);
					gc.setColor(0xFF000000);
					gc.drawRect(pieceButtonPos[1].x - ts.x / 2 - 2, pieceButtonPos[1].y - 24, ts.x + 6, ts.y + 6);
					gc.drawText(text, pieceButtonPos[1].x - ts.x / 2 + 2, pieceButtonPos[1].y - 21);
				}
				if (isDraggingPieceButton && draggingButtonIndex == 2) {
					String text = String.valueOf(piece[i].scalex + "%/" + piece[i].scaley + "%");
					Point ts = gc.textExtent(text);
					gc.setColor(0xFF000000);
					gc.drawRect(pieceButtonPos[2].x - ts.x / 2 - 2, pieceButtonPos[2].y - 24, ts.x + 6, ts.y + 6);
					gc.drawText(text, pieceButtonPos[2].x - ts.x / 2 + 2, pieceButtonPos[2].y - 21);
				}
				if (isDraggingPieceButton && draggingButtonIndex == 3) {
					String text = String.valueOf(piece[i].scalex + "%");
					Point ts = gc.textExtent(text);
					gc.setColor(0xFF000000);
					gc.drawRect(pieceButtonPos[3].x - ts.x / 2 - 2, pieceButtonPos[3].y - 24, ts.x + 6, ts.y + 6);
					gc.drawText(text, pieceButtonPos[3].x - ts.x / 2 + 2, pieceButtonPos[3].y - 21);
				}
				if (isDraggingPieceButton && draggingButtonIndex == 4) {
					String text = String.valueOf(piece[i].scaley + "%");
					Point ts = gc.textExtent(text);
					gc.setColor(0xFF000000);
					gc.drawRect(pieceButtonPos[4].x - ts.x / 2 - 2, pieceButtonPos[4].y - 24, ts.x + 6, ts.y + 6);
					gc.drawText(text, pieceButtonPos[4].x - ts.x / 2 + 2, pieceButtonPos[4].y - 21);
				}
			}
		}
		if (isSelecting) {
			gc.setColor(0x80000000);
            int x = selStart.x;
            int y = selStart.y;
            int w = selEnd.x - selStart.x;
            int h = selEnd.y - selStart.y;
            if (w < 0) {
                x = x + w;
                w = -w;
            }
            if (h < 0) {
                y = y + h;
                h = -h;
            }
            gc.drawRect(x, y, w, h);
		}
	}
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (isDragging || input == null) {
			return;
		}
		PipAnimateFramePiece[] piece = getSelectedPiece();
		PipAnimateFrame frame = (PipAnimateFrame)input;
		boolean changed = false;
		int changeX = 0;
		int changeY = 0;
		switch (keyCode) {
		case SWT.ARROW_UP:
			for (int i = 0; i < piece.length; i++) {
				if ((keyEventMask & SWT.SHIFT) != 0) {
					piece[i].setDy(piece[i].getDy() - 10);
					changeY = -10;
				} else {
					piece[i].setDy(piece[i].getDy() - 1);
					changeY = -1;
				}
				changed = true;
			}
			break;
		case SWT.ARROW_DOWN:
		    for (int i = 0; i < piece.length; i++) {
		    	if ((keyEventMask & SWT.SHIFT) != 0) {
		    		piece[i].setDy(piece[i].getDy() + 10);
		    		changeY = 10;
		    	} else {
		    		piece[i].setDy(piece[i].getDy() + 1);
		    		changeY = 1;
		    	}
				changed = true;
			}
			break;
		case SWT.ARROW_LEFT:
		    for (int i = 0; i < piece.length; i++) {
		    	if ((keyEventMask & SWT.SHIFT) != 0) {
		    		piece[i].setDx(piece[i].getDx() - 10);
		    		changeX = -10;
		    	} else {
		    		piece[i].setDx(piece[i].getDx() - 1);
		    		changeX = -1;
		    	}
				changed = true;
			}
			break;
		case SWT.ARROW_RIGHT:
		    for (int i = 0; i < piece.length; i++) {
		    	if ((keyEventMask & SWT.SHIFT) != 0) {
		    		piece[i].setDx(piece[i].getDx() + 10);
		    		changeX = 10;
		    	} else {
		    		piece[i].setDx(piece[i].getDx() + 1);
		    		changeX = 1;
		    	}
				changed = true;
			}
			break;
		case SWT.HOME:
			if (frame.getPieceCount() > 0) {
				selectedPiece = new int[] { 0 };
			} else {
			    selectedPiece = new int[0];
			}
			fireSelectionChanged();
			break;
		case SWT.END:
			if (frame.getPieceCount() > 0) {
				selectedPiece = new int[] { frame.getPieceCount() - 1 };
			} else {
			    selectedPiece = new int[0];
			}
			fireSelectionChanged();
			break;
		case SWT.PAGE_UP:
		    int newsel = selectedPiece[0] - 1;
			if (newsel < 0) {
			    newsel = frame.getPieceCount() - 1;
			}
			selectedPiece = new int[] { newsel };
			if (newsel >= 0) {
                selectedPiece = new int[] { newsel };
            } else {
                selectedPiece = new int[0];
            }
			fireSelectionChanged();
			break;
		case SWT.PAGE_DOWN:
		    newsel = selectedPiece[0] + 1;
			if (newsel > frame.getPieceCount() - 1) {
				newsel = 0;
				if (frame.getPieceCount() == 0) {
				    newsel = -1;
				}
			}
			if (newsel >= 0) {
			    selectedPiece = new int[] { newsel };
			} else {
			    selectedPiece = new int[0];
			}
			fireSelectionChanged();
			break;
		case SWT.DEL:
			deletePiece();
			break;
		case 'r':
			this.bringUpPiece();
			break;
		case 'e':
			this.bringTopPiece();
			break;
		case 'f':
			this.bringDownPiece();
			break;
		case 'd':
			this.bringBottomPiece();
		default:
			return;
		}
		redraw();
        updateMenu(false);
		if (changed) {
			allOffx = changeX;
			allOffy = changeY;
			fireContentChanged();
		}
	}

	public void setOwner(AnimateEditor owner) {
		this.owner = owner;
	}
	
	public void setSelectedPiece(int i) {
		selectedPiece = new int[] { i };
	}
	
	public int[] getSelection() {
		if (selectedPiece == null) {
			return new int[0];
		} else {
			int[] ret = new int[selectedPiece.length];
			System.arraycopy(selectedPiece, 0, ret, 0, selectedPiece.length);
			return ret;
		}
	}
	
	public void setSelection(int[] idx) {
		selectedPiece = idx;
	}
	
	public void clearSelection() {
		selectedPiece = new int[0];
	}

    public Point getRefPoint() {
        return refPoint;
    }

    public interface FramePieceSelectedListener extends Listener{
	}

	public void addPieceSelectedListener(FramePieceSelectedListener pieceSelectedListener) {
		this.addListener(FRAME_PIECE_SELECTED, pieceSelectedListener);
	}

	public PipAnimateFrame getFrame() {
		return (PipAnimateFrame) input;
	}
	
	public void fireSelectionChanged() {
        Event evt = new Event();
        if (this.selectedPiece != null && this.selectedPiece.length > 0) {
        	evt.index = this.selectedPiece[0];
        } else {
        	evt.index = -1;
        }
        AnimateFrameEditor.this.notifyListeners(FRAME_PIECE_SELECTED, evt);
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		super.widgetDisposed(e);
		if (popMenu != null) {
			popMenu.dispose();
		}
	}
	
	public void colorChanged(int newClr) {
		PipAnimateFramePiece piece = getSelectedPiece()[0];
		piece.color = newClr;
		redraw();
	}
	
	protected void autoSelectSameImagePiece() {
		PipAnimateFrame frame = (PipAnimateFrame)input;
		Set<Integer> imgSet = new HashSet<Integer>();
	    for (int i = 0; i < selectedPiece.length; i++) {
	        imgSet.add(frame.getPiece(selectedPiece[i]).getImageID());
	    }
		List<Integer> sels = new ArrayList<Integer>();
		for (int i = 0; i < frame.getPieceCount(); i++) {
			if (imgSet.contains(frame.getPiece(i).getImageID())) {
				sels.add(i);
			}
		}
		selectedPiece = new int[sels.size()];
		for (int i = 0; i < sels.size(); i++) {
			selectedPiece[i] = sels.get(i);
		}
		redraw();
	}
}