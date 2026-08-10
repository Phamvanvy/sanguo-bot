package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.pip.image.workshop.editor.AnimateFrameEditor.FramePieceSelectedListener;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;

/**
 * 帧包含的图块次序的预览工具
 * 
 * @author jhkang
 * 
 */
public class FramePiecesView extends Composite {
	public CheckboxTableViewer piecesTableView;
	private Table piecesTable;
	protected AnimateFrameEditor frameEditor;
	protected EquipmentPreviewer previewer;
	private PipAnimateFrame curFrame;
	private boolean updating = false;

	public FramePiecesView(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(5, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		this.setLayout(gridLayout);
		Button selectAll = new Button(this, SWT.PUSH);
		selectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				selectAllPieces(true);
			}
		});
		selectAll.setText("全选");
		Button deselectAll = new Button(this, SWT.PUSH);
		deselectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				selectAllPieces(false);
			}
		});
		deselectAll.setText("取消");

		Button moveUpBtn = new Button(this, SWT.PUSH);
		moveUpBtn.setText("上移");
		moveUpBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				movePieceUp();
			}
		});

		Button moveDownBtn = new Button(this, SWT.PUSH);
		moveDownBtn.setText("下移");
		moveDownBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				movePieceDown();
			}
		});

		Button refreshBtn = new Button(this, SWT.PUSH);
		refreshBtn.setText("刷新");
		refreshBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (curFrame != null) {
					piecesTableView.refresh();
					syncSelection();
				}
			}

		});

		piecesTableView = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		piecesTableView.setContentProvider(new FramePiecesContentProvier());
		piecesTableView.setLabelProvider(new FramePiecesLabelProvier());
		piecesTable = piecesTableView.getTable();
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalSpan = gridLayout.numColumns;
		piecesTable.setLayoutData(gridData);

		TableColumn col = new TableColumn(piecesTable, SWT.FILL);
		col.setWidth(160);
		col.setText("ID");
		col.setResizable(true);
		TableColumn col2 = new TableColumn(piecesTable, SWT.FILL);
		col2.setWidth(190);
		col2.setText("Name");
		col2.setResizable(true);
		piecesTable.setHeaderVisible(true);
		piecesTableView.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent arg0) {
				Object elem = arg0.getElement();
				boolean checked = arg0.getChecked();
				int idx = ((Integer) elem).intValue();
				PipAnimateFramePiece piece = curFrame.getPiece(idx);
				piece.setVisible(checked);
				if (frameEditor != null) {
					frameEditor.redraw();
				}
				if (previewer != null) {
					previewer.frameVisibleChanged();
				}
			}
		});
		piecesTableView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				if (updating) {
					return;
				}
				if (frameEditor == null) {
					return;
				}
				StructuredSelection sel = (StructuredSelection) arg0.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Object[] arr = sel.toArray();
				int[] ids = new int[arr.length];
				for (int i = 0; i < arr.length; i++) {
					ids[i] = ((Integer)arr[i]).intValue();
				}
				frameEditor.setSelection(ids);
				frameEditor.redraw();
			}
		});
		piecesTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.stateMask & SWT.SHIFT) != 0) {
                    if (e.keyCode == SWT.ARROW_UP) {
                        movePieceUp();
                        e.doit = false;
                    } else if (e.keyCode == SWT.ARROW_DOWN) {
                    	movePieceDown();
                        e.doit = false;
                    }
                }
				if (e.doit) {
					movePiece(e);
				}
			}
		});
	}

	@Override
	public void dispose() {
		piecesTableView.getLabelProvider().dispose();
		super.dispose();
	}

	protected void selectAllPieces(boolean allChecked) {
		if(curFrame == null){
			return;
		}
		piecesTableView.setAllChecked(allChecked);
		int cnt = curFrame.getPieceCount();
		for (int i = 0; i < cnt; i++) {
			curFrame.getPiece(i).setVisible(allChecked);
		}
		if (frameEditor != null) {
			frameEditor.redraw();
		}
		if (previewer != null) {
			previewer.frameVisibleChanged();
		}
	}

	protected void movePiece(KeyEvent e) {
		if (frameEditor == null) {
			return;
		}
		e.doit = false;
		Event e2 = new Event();
		e2.keyCode = e.keyCode;
		frameEditor.notifyListeners(SWT.KeyDown, e2);
	}

	protected void movePieceDown() {
		if (frameEditor == null) {
			int[] ids = piecesTable.getSelectionIndices();
			if (ids == null || ids.length == 0) {
				return;
			}
			for (int i = 0; i < ids.length; i++) {
				ids[i] = curFrame.getPieceCount() - 1 - ids[i];
			}
			Arrays.sort(ids);
			if (ids[0] == 0) {
				return;
			}
			for (int i = 0; i < ids.length; i++) {
				if (!(curFrame.getPiece(ids[i]) instanceof PipAni4AniFramePiece)) {
					MessageDialog.openError(getShell(), "错误", "在这里只能调整挂接点的层次，不能调整动画图块的层次。");
					return;
				}
			}
			for (int i = 0; i < ids.length; i++) {
				curFrame.swapPiece(ids[i], ids[i] - 1);
				ids[i]--;
				ids[i] = curFrame.getPieceCount() - 1 - ids[i];
			}
			curFrame.getParent().syncMirrorFrame(curFrame);
			piecesTableView.refresh();
			piecesTable.setSelection(ids);
			if (previewer != null) {
				previewer.frameChanged();
			}
		} else {
			frameEditor.bringDownPiece();
		}
	}

	protected void movePieceUp() {
		if (frameEditor == null) {
			int[] ids = piecesTable.getSelectionIndices();
			if (ids == null || ids.length == 0) {
				return;
			}
			for (int i = 0; i < ids.length; i++) {
				ids[i] = curFrame.getPieceCount() - 1 - ids[i];
			}
			Arrays.sort(ids);
			if (ids[ids.length - 1] == curFrame.getPieceCount() - 1) {
				return;
			}
			for (int i = 0; i < ids.length; i++) {
				if (!(curFrame.getPiece(ids[i]) instanceof PipAni4AniFramePiece)) {
					MessageDialog.openError(getShell(), "错误", "在这里只能调整挂接点的层次，不能调整动画图块的层次。");
					return;
				}
			}
			
			// 如果对称帧和本帧结构一致，则一同调整，否则无视
			PipAnimateFrame mirrorFrame = curFrame.getParent().getMirrorFrame(curFrame);
			if (mirrorFrame != null && mirrorFrame.getPieceCount() == curFrame.getPieceCount()) {
				for (int i = 0; i < ids.length; i++) {
					if (!(mirrorFrame.getPiece(ids[i]) instanceof PipAni4AniFramePiece)) {
						mirrorFrame = null;
						break;
					}
				}
			}
			
			for (int i = ids.length - 1; i >= 0; i--) {
				curFrame.swapPiece(ids[i], ids[i] + 1);
				if (mirrorFrame != null) {
					mirrorFrame.swapPiece(ids[i], ids[i] + 1);
				}
				ids[i]++;
				ids[i] = curFrame.getPieceCount() - 1 - ids[i];
			}
			piecesTableView.refresh();
			piecesTable.setSelection(ids);
			if (previewer != null) {
				previewer.frameChanged();
			}
		} else {
			frameEditor.bringUpPiece();
		}
	}

	public AnimateFrameEditor getFrameEditor() {
		return frameEditor;
	}

	public void setFrameEditor(AnimateFrameEditor frameEditor) {
		this.frameEditor = frameEditor;
		this.frameEditor.addPieceSelectedListener(new FramePieceSelectedListener(){
			public void handleEvent(Event evt) {
				syncSelection();
			}
		});
	}
	
	public void setEquipmentPreviewer(EquipmentPreviewer viewer) {
		previewer = viewer;
	}
	
	// 和编辑器同步选中图块
	private void syncSelection() {
		if (frameEditor == null) {
			return;
		}
		int[] arr = this.frameEditor.getSelection();
		updating = true;
		int[] newIdx = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			newIdx[i] = piecesTable.getItemCount() - 1 - arr[i];
		}
		piecesTable.setSelection(newIdx);
		updating = false;
	}

	public CheckboxTableViewer getPiecesTableView() {
		return piecesTableView;
	}

	public Table getPiecesTable() {
		return piecesTable;
	}

	public Image createPieceImg(int idx) {
		PipAnimateFramePiece framePiece = curFrame.getPiece(idx);
		int dx = framePiece.getDx();
		int dy = framePiece.getDy();
		framePiece.setDx(0);
		framePiece.setDy(0);
		int w = framePiece.getWidth();
		if (w < 16) {
			w = 16;
		}
		if (w > 64) {
			w = 64;
		}
		int h = framePiece.getHeight();
		if (h < 16) {
			h = 16;
		}
		if (h > 64) {
			h = 64;
		}
		Image img = new Image(getShell().getDisplay(), w, h);
		GC gc = new GC(img);
		float xratio = (float)w / framePiece.getWidth();
		float yratio = (float)h / framePiece.getHeight();
		framePiece.draw(gc, 0, 0, xratio > yratio ? xratio : yratio, null);
		framePiece.setDx(dx);
		framePiece.setDy(dy);
		gc.dispose();
		return img;
	}

	public class FramePiecesLabelProvier extends LabelProvider implements ITableLabelProvider {

		private List<Image> createdImages = new ArrayList<Image>();

		@Override
		public void dispose() {
			for (Image image : createdImages) {
				image.dispose();
			}
			createdImages.clear();
			super.dispose();
		}

		public Image getColumnImage(Object pieceIdx, int colIdx) {
			if (true && colIdx == 0) {
				Image image = createPieceImg(((Integer) pieceIdx).intValue());
				createdImages.add(image);
				return image;
			}
			return null;
		}

		public String getColumnText(Object arg0, int arg1) {
			int pieceIdx = ((Integer) arg0).intValue();
			PipAnimateFramePiece piece = curFrame.getPiece(pieceIdx);
			if (piece instanceof PipAni4AniFramePiece) {
				if (arg1 == 0) {
					return piece.getImageID() + "";
				} else if (arg1 == 1) {
					return ((PipAni4AniFramePiece) piece).name;
				}
				return piece.getFrame() + ":" + ((PipAni4AniFramePiece) piece).name;
			}
			if (arg1 == 0) {
				return piece.getFrame() + "";
			} else if (arg1 == 1) {
				return piece.getParent().getParent().getFileName(piece.getImageID());
			}
			return piece.getFrame() + "";
		}
	}

	public class FramePiecesContentProvier implements IStructuredContentProvider {
		public void dispose() {
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}

		public Object[] getElements(Object arg0) {
			Integer pieceIdx[] = new Integer[curFrame.getPieceCount()];
			for (int i = 0; i < pieceIdx.length; i++) {
				pieceIdx[i] = pieceIdx.length - i - 1;
			}
			return pieceIdx;
		}
	}
	public static void main(String[] args){
		String a = null;
		String b = a;
		System.out.println(b);
		a = "hello";
		System.out.println(b);
	}

	public void setCurFrame(PipAnimateFrame curFrame) {
		updating = true;
		this.curFrame = curFrame;
		piecesTableView.getLabelProvider().dispose();
		piecesTableView.setInput(curFrame);
		piecesTableView.setAllChecked(true);
		if (curFrame != null) {
			for (int i = 0; i < curFrame.getPieceCount(); i++) {
				if (!curFrame.getPiece(i).getVisible()) {
					piecesTableView.setChecked(new Integer(i), false);
				}
			}
		}
		syncSelection();
		updating = false;
	}

	public void contentChanged(Object source) {
		updating = true;
		piecesTableView.refresh();
		syncSelection();
		updating = false;
	}
}