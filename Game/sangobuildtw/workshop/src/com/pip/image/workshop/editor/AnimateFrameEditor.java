package com.pip.image.workshop.editor;

import java.util.Arrays;

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
 * A widget to edit a frame(PipAnimateFrame). The frame may be composed by several pieces.
 */
public class AnimateFrameEditor extends AbstractImageViewer {
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

	protected boolean allowDeletePiece = true;

	protected FramePieceSelectedListener pieceSelectedListener;
	
	public void setInput(Object input) {
		super.setInput(input);
		selectedPiece = new int[0];
		updateMenu(true);
		isDragging = false;
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
	
	private PipAnimateFramePiece[] getSelectedPiece() {
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
	
	private void setPieceSelected(int id, boolean flag) {
	    int index = findPieceSelected(id);
	    if (flag) {
	        if (index != -1) {
	            return;
	        }
	        int[] newarr = new int[selectedPiece.length + 1];
	        System.arraycopy(selectedPiece, 0, newarr, 1, selectedPiece.length);
	        newarr[0] = id;
	        selectedPiece = newarr;
	    } else {
	        if (index == -1) {
	            return;
	        }
            int[] newarr = new int[selectedPiece.length + 1];
            System.arraycopy(selectedPiece, 0, newarr, 0, index);
            System.arraycopy(selectedPiece, index + 1, newarr, index, selectedPiece.length - index - 1);
            selectedPiece = newarr;
	    }
	}
	
	private int getPieceAt(int x, int y) {
		PipAnimateFrame frame = (PipAnimateFrame)input;
		Rectangle size = getBounds();
		for (int i = frame.getPieceCount() - 1; i >= 0; i--) {
			PipAnimateFramePiece piece = frame.getPiece(i);
			if(piece.getVisible()==false){
				continue;
			}
			Rectangle rect = new Rectangle(piece.getDx(), piece.getDy(), piece.getWidth(), piece.getHeight());
			rect = zoom(rect);
			rect.x += size.width / 2 + paintOffset.x;
			rect.y += size.height / 2 + paintOffset.y;
			if (rect.contains(x, y)) {
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
	        int px = piece.getDx();
	        int py = piece.getDy();
	        int pw = piece.getWidth();
	        int ph = piece.getHeight();
	        if (selRect.intersects(px, py, pw, ph)) {
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
		super(parent, style | SWT.NO_BACKGROUND);
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
    				                if(pieceSelectedListener!=null){
    				                	pieceSelectedListener.pieceSelected(clickedPiece);
    				                }
    				            }
                                isDragging = true;
                                dragStartPoint = new Point(e.x, e.y);
                                dragStartPos = new Point(getFirstSelectedPiece().getDx(), getFirstSelectedPiece().getDy()); 
        					} else {
        						selectedPiece = new int[0];
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
						redraw();
						int offx = (int)((e.x - dragStartPoint.x) / ratio);
						int offy = (int)((e.y - dragStartPoint.y) / ratio);
						if (offx != 0 || offy != 0) {
							fireContentChanged();
						}
					} else if (isSelecting && input != null) {
					    selEnd = new Point(e.x, e.y);
					    checkSelectedPieces();
					    isSelecting = false;
					    redraw();
					} else if (isDraggingRefPoint) {
					    isDraggingRefPoint = false;
					    redraw();
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
		popMenu = mgr.createContextMenu(this);
	}

	protected void paste() {
		PipAnimateFrame frameData = (PipAnimateFrame)input;
		for(int idx:copiedPiece){
			PipAnimateFramePiece piece = (PipAnimateFramePiece) copySource.getPiece(idx).clone();
			frameData.addPiece(piece);
		}
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
		if (selectedPiece[0] == count - 1) {
			return;
		}
		frameObj.swapPiece(selectedPiece[0], selectedPiece[0] + 1);
		selectedPiece = new int[] { selectedPiece[0] + 1 };
		redraw();
		fireContentChanged();
	}
	
	public void bringDownPiece() {
	    if (input == null || selectedPiece.length == 0) {
            return;
        }
        PipAnimateFrame frameObj = (PipAnimateFrame)input;
        if (selectedPiece[0] == 0) {
            return;
        }
        frameObj.swapPiece(selectedPiece[0], selectedPiece[0] - 1);
        selectedPiece = new int[] { selectedPiece[0] - 1 };
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
		if (isDraggingRefPoint || (mouseInRefPointArea && !isDragging && !isSelecting)) {
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
        gc.drawText(info2, size.x / 2 - ts.x / 2, size.y - ts.y - 20);
	}

	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
		} else {
			if (refFrame != null) {
				refFrame.draw(gc, size.x / 2 + paintOffset.x, size.y / 2 + paintOffset.y, ratio);
			}
			PipAnimateFrame frame = (PipAnimateFrame)input;
			frame.draw(gc, size.x / 2 + paintOffset.x, size.y / 2 + paintOffset.y, ratio);
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
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (isDragging || input == null) {
			return;
		}
		PipAnimateFramePiece[] piece = getSelectedPiece();
		PipAnimateFrame frame = (PipAnimateFrame)input;
		boolean changed = false;
		switch (keyCode) {
		case SWT.ARROW_UP:
			for (int i = 0; i < piece.length; i++) {
				piece[i].setDy(piece[i].getDy() - 1);
				changed = true;
			}
			break;
		case SWT.ARROW_DOWN:
		    for (int i = 0; i < piece.length; i++) {
				piece[i].setDy(piece[i].getDy() + 1);
				changed = true;
			}
			break;
		case SWT.ARROW_LEFT:
		    for (int i = 0; i < piece.length; i++) {
				piece[i].setDx(piece[i].getDx() - 1);
				changed = true;
			}
			break;
		case SWT.ARROW_RIGHT:
		    for (int i = 0; i < piece.length; i++) {
				piece[i].setDx(piece[i].getDx() + 1);
				changed = true;
			}
			break;
		case SWT.HOME:
			if (frame.getPieceCount() > 0) {
				selectedPiece = new int[] { 0 };
			} else {
			    selectedPiece = new int[0];
			}
			break;
		case SWT.END:
			if (frame.getPieceCount() > 0) {
				selectedPiece = new int[] { frame.getPieceCount() - 1 };
			} else {
			    selectedPiece = new int[0];
			}
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
			fireContentChanged();
		}
	}

	public void setOwner(AnimateEditor owner) {
		this.owner = owner;
	}
	
	public void setSelectedPiece(int i) {
		selectedPiece = new int[] { i };
	}

    public Point getRefPoint() {
        return refPoint;
    }

    public interface FramePieceSelectedListener{
    	void pieceSelected(int idx);
	}

	public void setPieceSelectedListener(FramePieceSelectedListener pieceSelectedListener) {
		this.pieceSelectedListener = pieceSelectedListener;
	}
}