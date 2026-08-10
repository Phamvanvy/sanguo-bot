package com.pip.image.workshop.editor;

import java.util.ArrayList;
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
public class FramePiecesView {
	public CheckboxTableViewer piecesTableView;
	private Table piecesTable;
	protected AnimateFrameEditor frameEditor;
	private PipAnimateFrame curFrame;

	public FramePiecesView(Composite parent, int style) {

		Composite container = new Composite(parent, style);
		GridLayout gridLayout = new GridLayout(5, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);
		Button selectAll = new Button(container, SWT.PUSH);
		selectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				selectAllPieces(true);
			}
		});
		selectAll.setText("全选");
		Button deselectAll = new Button(container, SWT.PUSH);
		deselectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				selectAllPieces(false);
			}
		});
		deselectAll.setText("取消");

		Button moveUpBtn = new Button(container, SWT.PUSH);
		moveUpBtn.setText("上移");
		moveUpBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				movePieceUp();
			}
		});

		Button moveDownBtn = new Button(container, SWT.PUSH);
		moveDownBtn.setText("下移");
		moveDownBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				movePieceDown();
			}
		});

		Button refreshBtn = new Button(container, SWT.PUSH);
		refreshBtn.setText("刷新");
		refreshBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (curFrame != null) {
					piecesTableView.refresh();
				}
			}

		});

		piecesTableView = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
		piecesTableView.setContentProvider(new FramePiecesContentProvier());
		piecesTableView.setLabelProvider(new FramePiecesLabelProvier());
		piecesTable = piecesTableView.getTable();
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalSpan = gridLayout.numColumns;
		piecesTable.setLayoutData(gridData);

		TableColumn col = new TableColumn(piecesTable, SWT.FILL);
		col.setWidth(70);
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
				frameEditor.redraw();
			}
		});
		piecesTableView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				StructuredSelection sel = (StructuredSelection) arg0.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Integer intObj = (Integer) sel.getFirstElement();
				int idx = intObj.intValue();
				frameEditor.setSelectedPiece(idx);
				frameEditor.redraw();
			}
		});
		piecesTableView.getControl().addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				movePiece(e);
			}

		});
		/*
		 * / piecesTable.addListener(SWT.MeasureItem, new Listener(){ public
		 * void handleEvent(Event event) { if(event.index == 0){ TableItem ti =
		 * (TableItem) event.item; Image img = ti.getImage(); Rectangle imgRect
		 * = img.getBounds(); event.width = imgRect.width +
		 * event.gc.textExtent(ti.getText()).x; event.height = imgRect.height; }
		 * } }); piecesTable.addListener(SWT.PaintItem, new Listener(){ public
		 * void handleEvent(Event event) { if(event.index == 0){ Image img =
		 * ((TableItem)event.item).getImage(); event.gc.drawImage(img, event.x,
		 * event.y); event.doit = false; } } });
		 */

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
		frameEditor.redraw();
	}

	protected void movePiece(KeyEvent e) {
		e.doit = false;
		Event e2 = new Event();
		e2.keyCode = e.keyCode;
		frameEditor.notifyListeners(SWT.KeyDown, e2);
	}

	protected void movePieceDown() {
		if (checkPieceSel() == false) {
			return;
		}
		if(piecesTable.getSelectionIndex()<piecesTable.getItemCount() - 1){
			frameEditor.bringDownPiece();
			piecesTable.select(piecesTable.getSelectionIndex() + 1);
			piecesTableView.refresh();
		}
	}

	protected void movePieceUp() {
		if (checkPieceSel() == false) {
			return;
		}
		if(piecesTable.getSelectionIndex() > 0){
			frameEditor.bringUpPiece();
			piecesTable.select(piecesTable.getSelectionIndex() - 1);
			piecesTableView.refresh();
		}
	}

	protected boolean checkPieceSel() {
		if (piecesTable.getSelectionIndex() < 0) {
			MessageDialog.openInformation(frameEditor.getShell(), "Info", "请选择要移动的图块或挂接点");
			return false;
		}
		return true;
	}

	public AnimateFrameEditor getFrameEditor() {
		return frameEditor;
	}

	public void setFrameEditor(AnimateFrameEditor frameEditor) {
		this.frameEditor = frameEditor;
		this.frameEditor.setPieceSelectedListener(new FramePieceSelectedListener(){
			public void pieceSelected(int idx) {
				piecesTable.select(piecesTable.getItemCount() - idx - 1);
			}
		});
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
		int h = framePiece.getHeight();
		if (h < 16) {
			h = 16;
		}
		Image img = new Image(frameEditor.getShell().getDisplay(), w, h);
		GC gc = new GC(img);
		framePiece.draw(gc, 0, 0, 1, null);
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
				return "图块";
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
		this.curFrame = curFrame;
		piecesTableView.getLabelProvider().dispose();
		piecesTableView.setInput(curFrame);
		piecesTableView.setAllChecked(true);
	}
}