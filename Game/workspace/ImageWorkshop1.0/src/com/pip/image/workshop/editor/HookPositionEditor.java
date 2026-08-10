/**
 * 
 */
package com.pip.image.workshop.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.AutoBody;
import com.pip.image.workshop.editor.AnimateFrameEditor.FramePieceSelectedListener;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.Utils;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;

/**
 * @author jhkang
 *
 */
public class HookPositionEditor extends EditorPart implements ImageViewerListener, FramePieceSelectedListener, IFileModificationListener{

	public class HookPosLabelProvider  extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object arg0, int colIdx) {
			return null;
		}

		public String getColumnText(Object arg0, int arg1) {
			PipAni4AniFramePiece hook = (PipAni4AniFramePiece) arg0;
			if(arg1==0){
				return hook.getImageID()+"";
			}else if(arg1==1){
				return hook.name;
			}
			return arg0.toString();
		}
	}

	public class HookPosContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
		public Object[] getElements(Object arg0) {
			if(true){
				return hooks.toArray();
			}
			return null;
		}

	}

	private PipAnimateSet animateSet;
	private AnimateFrameEditor frameEditor;
	private TableViewer hookPosList;
	private ArrayList<PipAni4AniFramePiece> hooks = new ArrayList<PipAni4AniFramePiece>();
	private AnimateFrameSelector frameSelector;
	private PipAnimateFrame curFrame;
	private Set<Integer> curHooksIdSet = new HashSet<Integer>();
	private Table piecesTable;
	private CheckboxTableViewer piecesTableView;
	private boolean dirty;
	private BodyDef bodyDef = new BodyDef();
	private String hookFilePath;
	private Label bodyCtsName;
	
	private StateManager stateMgr;
	/**
	 * 从哪一帧复制的坐标
	 */
	private int copiedFrameIdx = -1;
	private int[] copiedHookIds = null;
	private FramePiecesView piecesView;
	
	public static final int PROPERTY_CANUNDO = 1;
	public static final int PROPERTY_CANREDO = 2;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor arg0) {
		try {
			FileWatcher.unwatch(this);
			bodyDef.save(animateSet, hookFilePath);
			FileWatcher.watch(new File(hookFilePath), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		Element rootElement = new Element("hooks");
//		rootElement.addAttribute("ctsName", "abc.cts");
//		for(PipAni4AniFramePiece hk:hooks){
//			hk.saveToXMLNode(rootElement);
//		}
//		Document doc = new Document(rootElement);
//		try {
//			Utils.saveDOM(doc, System.out);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		setDirty(false);
	}
	
	private void setDirty(boolean b) {
		dirty = b;
		firePropertyChange(PROP_DIRTY);
		if (b) {
			saveState();
			firePropertyChange(PROPERTY_CANUNDO);
			firePropertyChange(PROPERTY_CANREDO);
		}
	}
	private void saveState() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			animateSet.saveState(new DataOutputStream(bos));
			bodyDef.saveState(animateSet, new DataOutputStream(bos));
			stateMgr.push(bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite arg0, IEditorInput arg1) throws PartInitException {
		setSite(arg0);
		setInput(arg1);
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		hookFilePath = Utils.urlToPath(url);
		FileWatcher.watch(new File(hookFilePath), this);
		stateMgr = new StateManager(20000000);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createTopPart(sashForm);
		createBottomPart(sashForm);

		sashForm.setWeights(new int[] { 1, 1 });
		//
		postCreateControl();
		frameSelectionChanged(null, 0);
	}
	private void createTopPart(Composite parent){
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createTopLeft(sashForm);
		createTopRight(sashForm);

		sashForm.setWeights(new int[] { 1, 1 });
	}
	
	private void createTopRight(Composite parent){
		Composite composite = new Composite(parent, SWT.None);
		GridLayout gridLayout = new GridLayout(	);
		gridLayout.marginBottom = 0;
		gridLayout.numColumns = 8;
		composite.setLayout(gridLayout);
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("挂接点:");
		
		
		Button buttonAdd = new Button(composite, SWT.PUSH);
		buttonAdd.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		buttonAdd.setText("新增");
		buttonAdd.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				addHook();
			}
		});
		
		final Button buttonRemove = new Button(composite, SWT.PUSH);
		buttonRemove.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		buttonRemove.setText("删除");
		buttonRemove.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				removeHook();
				if(hooks.size()==0){
					buttonRemove.setEnabled(false);
				}
			}
		});
		buttonRemove.setEnabled(false);
		
		Button zoomInHook = new Button(composite, SWT.PUSH);
		zoomInHook.setText("放大显示");
		zoomInHook.setToolTipText("放大挂接点以便调整层次,位置");
		zoomInHook.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				zoomHook(1);
			}
		});
		
		Button zoomOutHook = new Button(composite, SWT.PUSH);
		zoomOutHook.setText("缩小显示");
		zoomOutHook.setToolTipText("缩小挂接点");
		zoomOutHook.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				zoomHook(-1);
			}
		});
		
		Button zoomIn = new Button(composite, SWT.PUSH);
		zoomIn.setText("整体放大");
		zoomIn.setToolTipText("所有挂接点位置放大一倍");
		zoomIn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				zoomIn();
			}
		});
		
		Button zoomOut = new Button(composite, SWT.PUSH);
		zoomOut.setText("整体缩小");
		zoomOut.setToolTipText("所有挂接点位置缩小一倍");
		zoomOut.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				zoomOut();
			}
		});
		
		bodyCtsName = new Label(composite, SWT.NONE);
		bodyCtsName.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		bodyCtsName.setText("");
		
		
		hookPosList = new TableViewer(composite, SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI);
		hookPosList.setContentProvider(new HookPosContentProvider());
		hookPosList.setLabelProvider(new HookPosLabelProvider()	);
		hookPosList.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent arg0) {
				if(hookPosList.getTable().getSelectionIndex()>=0){
					buttonRemove.setEnabled(true);
				}
				hookSelected();
			}
		});
		hookPosList.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent arg0) {
				hookDoubleClicked();
			}
		});
		hookPosList.getControl().addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				piecesView.movePiece(e);
			}
		});
		Table hookListTable = hookPosList.getTable();
		hookListTable.setHeaderVisible(true);
		final TableColumn hookIdColumn = new TableColumn(hookListTable, SWT.NONE);
		hookIdColumn.setWidth(100);
		hookIdColumn.setText("ID");
		hookIdColumn.setResizable(true);
		final TableColumn hookNameColumn = new TableColumn(hookListTable, SWT.NONE);
		hookNameColumn.setWidth(100);
		hookNameColumn.setText("Name");
		hookNameColumn.setResizable(true);
		hookPosList.setInput(hooks);
		///
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalSpan = gridLayout.numColumns;
		hookListTable.setLayoutData(gridData);
		///位置相关操作按钮区
		Composite positionOps = new Composite(composite, SWT.None);
		GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData2.horizontalSpan = gridLayout.numColumns;
		positionOps.setLayoutData(gridData2);
		positionOps.setLayout(new GridLayout(7, false));
		Label label2 = new Label(positionOps, SWT.NONE);
		label2.setText("位置:");
		
		final Button copyHookPosBtn = new Button(positionOps, SWT.PUSH);
		copyHookPosBtn.setText("复制");
		copyHookPosBtn.setToolTipText("将选中的挂接点在当前帧中的位置复制下来");
		copyHookPosBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				copyHookPos(copyHookPosBtn);
			}
		});
		
		Button pasteHookPosBtn = new Button(positionOps, SWT.PUSH);
		pasteHookPosBtn.setText("粘贴");
		pasteHookPosBtn.setToolTipText("将复制的挂接点位置设置给当前帧中对应的挂接点");
		pasteHookPosBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				pasteHookPos();
			}
		});
		
		Button pasteHookPos2allBtn = new Button(positionOps, SWT.PUSH);
		pasteHookPos2allBtn.setText("粘贴至所有");
		pasteHookPos2allBtn.setToolTipText("将复制的挂接点位置设置给所有其他帧中对应的挂接点");
		pasteHookPos2allBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				pasteHookPos2all();
			}
		});
		
		Button flipPosBtn = new Button(positionOps, SWT.PUSH);
		flipPosBtn.setText("翻转");
		flipPosBtn.setToolTipText("将选中的挂接点的位置水平翻转");
		flipPosBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				flipHookPos();
			}
		});
		
		Button exchangePosBtn = new Button(positionOps, SWT.PUSH);
		exchangePosBtn.setText("左右互换");
		exchangePosBtn.setToolTipText("将左右对称的挂接点位置互换");
		exchangePosBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				exchangeHookPos();
			}
		});

		Button pieceToBackgroundBtn = new Button(positionOps, SWT.PUSH);
		pieceToBackgroundBtn.setText("背景模式");
		pieceToBackgroundBtn.setToolTipText("把所有实际图块调整到所有挂接点下方，作为背景显示");
		pieceToBackgroundBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				switchToBackgroundMode();
			}
		});
		
//		Button autoSetBtn = new Button(positionOps, SWT.PUSH);
//		autoSetBtn.setText("自动设置");
//		autoSetBtn.setToolTipText("根据一个参考动画文件自动设置挂接点的位置");
//		autoSetBtn.addSelectionListener(new SelectionAdapter(){
//			public void widgetSelected(SelectionEvent e) {
//				autoSetHookPos();
//			}
//		});

		Button setFromImageBtn = new Button(positionOps, SWT.PUSH);
		setFromImageBtn.setText("自动设置");
		setFromImageBtn.setToolTipText("根据一组点图自动设置挂接点的位置");
		setFromImageBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				autoSetHookPosFromImage();
			}
		});
		
		Label label3 = new Label(positionOps, SWT.NONE);
		label3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		label3.setText("");
		copyHookPosBtn.setData(label3);
	}

	protected void flipHookPos() {
		StructuredSelection sel = (StructuredSelection)hookPosList.getSelection();
		if (sel.isEmpty()) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "请从列表中选择挂接点。");
			return;
		}
		Object[] sels = sel.toArray();
		for (int i = 0; i < sels.length; i++) {
			PipAni4AniFramePiece hook = (PipAni4AniFramePiece)sels[i];
			PipAni4AniFramePiece hookInFrame = curFrame.getHook(hook.getImageID());
			hookInFrame.setRealDx(-hookInFrame.getRealDx());
		}
		animateSet.syncMirrorFrame(curFrame);
		
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
	}
	
	// 切换为背景模式，所有素体图块移动到所有挂接点下面作为背景显示
	protected void switchToBackgroundMode() {
		for (int i = 0; i < animateSet.getFrameCount(); i++) {
			PipAnimateFrame frame = animateSet.getFrame(i);
			for (int j = 1; j < frame.getPieceCount(); j++) {
				PipAnimateFramePiece p1 = frame.getPiece(j);
				if (p1 instanceof PipAni4AniFramePiece) {
					continue;
				}
				int k = j - 1;
				while (k >= 0) {
					if (!(frame.getPiece(k) instanceof PipAni4AniFramePiece)) {
						break;
					}
					k--;
				}
				k++;
				if (k != j) {
					frame.removePiece(j);
					frame.addPieceAt(k, p1);
				}
			}
		}
		piecesView.piecesTableView.refresh();
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
	}

	protected void exchangeHookPos() {
		// 查找左右对应的挂接点对
		boolean found = false;
		for (int i = 0; i < hooks.size() - 1; i++) {
			String name1 = hooks.get(i).name;
			String matchName;
			if (name1.contains("左")) {
				matchName = name1.substring(0, name1.indexOf("左")) + "右";
			} else if (name1.contains("右")) {
				matchName = name1.substring(0, name1.indexOf("右")) + "左";
			} else {
				continue;
			}
			for (int j = i + 1; j < hooks.size(); j++) {
				if (hooks.get(j).name.startsWith(matchName)) {
					// i,j两个挂接点在当前帧的位置互换
					exchangeHookPos(hooks.get(i), hooks.get(j));
					found = true;
				}
			}
		}
		if (found) {
			frameEditor.redraw();
			frameSelector.refresh();
			setDirty(true);
		}
	}
	
	protected void exchangeHookPos(PipAni4AniFramePiece hook1, PipAni4AniFramePiece hook2) {
		PipAni4AniFramePiece hookInFrame1 = curFrame.getHook(hook1.getImageID());
		PipAni4AniFramePiece hookInFrame2 = curFrame.getHook(hook2.getImageID());
		int rx1 = hookInFrame1.getRealDx();
		int ry1 = hookInFrame1.getRealDy();
		hookInFrame1.setRealDx(hookInFrame2.getRealDx());
		hookInFrame1.setRealDy(hookInFrame2.getRealDy());
		hookInFrame2.setRealDx(rx1);
		hookInFrame2.setRealDy(ry1);
	}
	
	protected void pasteHookPos2all() {
		if (copiedFrameIdx < 0) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "请先从其他帧复制挂接点的位置。");
			return;
		}
		
		int frameCnt = animateSet.getFrameCount();
		for (int i = 0; i < frameCnt; i++) {
			if (copiedFrameIdx == i) {
				continue;
			}
			for (int hid : copiedHookIds) {
				copyHookToFrame(copiedFrameIdx, hid, i);
			}
		}
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
	}

	protected void pasteHookPos() {
		int curIndex = frameSelector.getSelectedFrame();
		if (curIndex == copiedFrameIdx) {
			return;
		}
		if (copiedFrameIdx < 0) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "请先从其他帧复制挂接点的位置。");
			return;
		}
		for (int hid : copiedHookIds) {
			copyHookToFrame(copiedFrameIdx, hid, curIndex);
		}
		animateSet.syncMirrorFrame(animateSet.getFrame(curIndex));
		piecesView.piecesTableView.refresh();
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
	}
	
	/*
	 * 从一帧中把一个挂接点的位置复制到另外一帧。
	 */
	private void copyHookToFrame(int srcFrame, int hookId, int targetFrame) {
		PipAnimateFrame srcFrameObj = animateSet.getFrame(srcFrame);
		PipAni4AniFramePiece srcPiece = srcFrameObj.getHook(hookId);
		if (srcPiece == null) {
			return;
		}
		int x = srcPiece.getRealDx();
		int y = srcPiece.getRealDy();
		int idx = srcFrameObj.getPieceIndex(srcPiece);
		
		PipAnimateFrame tgtFrameObj = animateSet.getFrame(targetFrame);
		PipAni4AniFramePiece tgtPiece = tgtFrameObj.getHook(hookId);
		if (tgtPiece == null) {
			return;
		}
		tgtPiece.setRealDx(x);
		tgtPiece.setRealDy(y);
		int newIdx = tgtFrameObj.getPieceIndex(tgtPiece);
		if (newIdx != idx) {
			tgtFrameObj.removePiece(newIdx);
			tgtFrameObj.addPieceAt(idx, tgtPiece);
		}
	}

	protected void copyHookPos(Button copyBtn) {
		StructuredSelection sel = (StructuredSelection)hookPosList.getSelection();
		if (sel.isEmpty()) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "请从列表中选择挂接点。");
			return;
		}
		Object[] sels = sel.toArray();
		int[] selHookIds = new int[sels.length];
		for (int i = 0; i < sels.length; i++) {
			PipAni4AniFramePiece hook = (PipAni4AniFramePiece)sels[i];
			selHookIds[i] = hook.getImageID();
		}
		copiedFrameIdx = frameSelector.getSelectedFrame();
		copiedHookIds = selHookIds;
		
		// update label
		Label copyInfo = (Label) copyBtn.getData();
		String msg = "已从第" + copiedFrameIdx + "帧复制了挂接点";
		for (int i = 0; i < selHookIds.length; i++) {
			if (i > 0) {
				msg += ",";
			}
			msg += selHookIds[i];
		}
		copyInfo.setText(msg);
	}

	protected void zoomHook(int i) {
		int idx = hookPosList.getTable().getSelectionIndex();
		if(idx<0){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请从列表中选择挂接点");
			return;
		}
		PipAni4AniFramePiece hook = hooks.get(idx);
		if(i>0){
			hook.setWidth(hook.getWidth()+10);
			hook.setHeight(hook.getHeight()+10);
		}else{
			if(hook.getWidth()>10){
				hook.setWidth(hook.getWidth() - 10);
				hook.setHeight(hook.getHeight() - 10);
			}
		}
		updateHookSizeInFrame(hook);
		frameEditor.redraw();
	}

	private void updateHookSizeInFrame(PipAni4AniFramePiece hook) {
		PipAni4AniFramePiece inDrawHook = curFrame.getHook(hook.getImageID());
		inDrawHook.setWidth(hook.getWidth());
		inDrawHook.setHeight(hook.getHeight());
	}

	protected void hookDoubleClicked() {
		int idx = hookPosList.getTable().getSelectionIndex();
		String input = getInput(false, hooks.get(idx).name);
		if(input != null){
			hooks.get(idx).name = input;
			hookPosList.refresh(hooks.get(idx) , true);
			updateHookNameInAniSet(hooks.get(idx).getImageID(), input);
			setDirty(true);
		}
	}

	private void updateHookNameInAniSet(int hookId, String input) {
		for(int i=0; i<animateSet.getFrameCount(); i++){
			animateSet.getFrame(i).getHook(hookId).name = input;
		}		
		piecesTableView.refresh();
	}

	protected void hookSelected() {
		int[] idxes = hookPosList.getTable().getSelectionIndices();
		frameEditor.clearSelection();
		for (int j = 0; j < idxes.length; j++) {
			int idx = idxes[j];
			PipAni4AniFramePiece hook = hooks.get(idx);
			for(int i=0; i<curFrame.getPieceCount(); i++){
				PipAnimateFramePiece piece = curFrame.getPiece(i);
				if(piece.getFrame() == 0xFF && piece.getImageID() == hook.getImageID()){
					frameEditor.setPieceSelected(i, true);
					break;
				}
			}
		}
		frameEditor.redraw();
	}

	synchronized protected void removeHook() {
		int selIdx = hookPosList.getTable().getSelectionIndex();
		if(selIdx  < 0){
			return;
		}
		PipAni4AniFramePiece piece = hooks.remove(selIdx);
		curHooksIdSet.remove(piece.getImageID());
		for(int i=0; i<animateSet.getFrameCount(); i++){
			PipAnimateFrame frame = animateSet.getFrame(i);
			for(int j=0; j<frame.getPieceCount(); j++){
				PipAnimateFramePiece pieceInFrame = frame.getPiece(j);
				if(pieceInFrame.getFrame() == 0xFF && pieceInFrame.getImageID() == piece.getImageID()){
					frame.removePiece(pieceInFrame);
				}
			}
		}
		frameEditor.setSelectedPiece(0);
		frameEditor.redraw();
		frameSelector.refresh();
		hookPosList.refresh();
		if(selIdx<hooks.size()){
			hookPosList.getTable().setSelection(selIdx);
		}else if(selIdx>0){
			hookPosList.getTable().setSelection(selIdx - 1);
		}
		setDirty(true);
	}

	synchronized protected void addHook() {
		String input = getInput(true, "");
		if(input == null){
			return;
		}
		int nextHookId = getNexHookId();
		if(nextHookId<0){
			MessageDialog.openError(getSite().getShell(), "Error", "挂接点数量不能超过255");
			return;
		}
		PipAni4AniFramePiece hk = new PipAni4AniFramePiece(curFrame);
		hk.name = input;
		
		// 如果有选中一个挂接点，则复制这个挂接点的位置
		int selIdx = hookPosList.getTable().getSelectionIndex();
		hk.setImageID(nextHookId);
		if (selIdx  < 0) {
			hk.setDx((hk.getWidth()+2)*(nextHookId+1));
			hk.setDy(5);
			hooks.add(hk);
			for (int i = 0; i < animateSet.getFrameCount(); i++) {
				animateSet.getFrame(i).addPiece((PipAnimateFramePiece) hk.clone());
			}
		} else {
			int copyHookID = hooks.get(selIdx).getImageID();
			PipAni4AniFramePiece copyHk = curFrame.getHook(copyHookID);
			int copyIndex = curFrame.getPieceIndex(copyHk);
			hk.setRealDx(copyHk.getRealDx());
			hk.setRealDy(copyHk.getRealDy());
			hooks.add(hk);
			for (int i = 0; i < animateSet.getFrameCount(); i++) {
				PipAni4AniFramePiece newHk = (PipAni4AniFramePiece)hk.clone();
				PipAnimateFrame frame = animateSet.getFrame(i);
				copyHk = frame.getHook(copyHookID);
				copyIndex = frame.getPieceIndex(copyHk);
				newHk.setRealDx(copyHk.getRealDx());
				newHk.setRealDy(copyHk.getRealDy());
				animateSet.getFrame(i).addPieceAt(copyIndex + 1, newHk);
			}
		}
		frameEditor.redraw();
		frameSelector.refresh();
		hookPosList.refresh();
		piecesView.setCurFrame(curFrame);
		setDirty(true);
	}

	synchronized private int getNexHookId() {
		for(int i=0; i<Byte.MAX_VALUE; i++){
			if(curHooksIdSet.contains(i)){
				continue;
			}
			curHooksIdSet.add(i);
			return i;
		}
		return -1;
	}

	private String getInput(boolean fakeInput, String defaultValue) {
		if(fakeInput){
			return "挂接点"+hooks.size();
		}
		InputDialog dlg = new InputDialog(getSite().getShell(), "Input", "请输入挂接点名称", defaultValue,null);
		if(dlg.open() == InputDialog.OK){
			String ret = dlg.getValue();
			if(ret != null && ret.equals("") == false){
				return dlg.getValue();
			}
		}
		return null;
	}

	private void createTopLeft(Composite parent){
		frameEditor = new AnimateFrameEditor(parent, SWT.None);
		frameEditor.setImageViewerListener(this);
		frameEditor.allowDeletePiece = false;
		frameEditor.addPieceSelectedListener(this);
	}
	private void postCreateControl(){
		this.setPartName(this.getEditorInput().getName());
		try {
//			String hookFilePath = "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\male3.hk";
			File hookFile = new File(hookFilePath);
			DataInputStream dis = new DataInputStream(new FileInputStream(hookFile));
			bodyDef.loadHooks(dis);
			dis.close();
			bodyCtsName.setText(bodyDef.ctsFile);
			bodyCtsName.setSize(bodyCtsName.computeSize(-1, -1)); 
			hooks = bodyDef.hooks;
			curHooksIdSet = bodyDef.curHooksIdSet;
			File ctsFile = new File(hookFile.getParentFile(), bodyDef.ctsFile);
			PipAnimateSet pas = new PipAnimateSet();
			pas.load(ctsFile);
			animateSet = pas;
			bodyDef.embedHookPieces(animateSet);
			//
			frameSelector.setInput(animateSet);
			frameSelector.setSelectedFrame(0);
			frameSelector.redraw();
			saveState();
		} catch (IOException e) {
			MessageDialog.openError(getSite().getShell(), "Error", "初始化错误:\n"+e);
			e.printStackTrace();
		}
	}
	
	private void createBottomPart(Composite parent){
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createBottomLeft(sashForm);
		createBottomRight(sashForm);
		sashForm.setWeights(new int[] { 2, 1 });
	}
	private void createBottomRight(Composite parent){
		piecesView = new FramePiecesView(parent, SWT.FILL);
		piecesView.setFrameEditor(frameEditor);
		piecesTableView = piecesView.getPiecesTableView();
		piecesTable = piecesView.getPiecesTable();
	}

	private void createBottomLeft(Composite parent){
		frameSelector = new AnimateFrameSelector(parent, SWT.None);
		frameSelector.needDrawTip = false;
		frameSelector.needMenu = false;
		frameSelector.setImageViewerListener(this);
		
		frameSelector.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
            	if (event.keyCode == SWT.DEL) {
            		onDeleteFrame();
            	}
            }
        });
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void areaSelected(Object source) {
		// TODO Auto-generated method stub
		
	}

	public void contentChanged(Object source) {
		updateHooksTable();
		frameSelector.refresh();
		setDirty(true);
		if (source == this.frameEditor) {
			animateSet.syncMirrorFrame(curFrame);
			piecesView.contentChanged(source);
		}
	}
	synchronized private void updateHooksTable(){
		hookPosList.refresh();
	}

	public void frameDoubleClicked(Object source, int frame) {
		// TODO Auto-generated method stub
		
	}

	public void frameSelectionChanged(Object source, int newFrame) {
		curFrame = animateSet.getFrame(newFrame);
		piecesView.setCurFrame(curFrame);
		frameEditor.setInput(curFrame);
		updateHooksTable();
		frameEditor.redraw();
	}

	public boolean canUndo() {
		return stateMgr.canUndo();
	}
	
	public boolean canRedo() {
	    return stateMgr.canRedo();
	}
	private void restoreState(byte[] data) {
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
//			animateSet.restoreState(dis);
			bodyDef.restoreState(animateSet, dis);
			frameSelectionChanged(null, frameSelector.getSelectedFrame());
			hooks = bodyDef.hooks;
			hookPosList.refresh();
			frameSelector.refresh();
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void undo() {
		if (canUndo()) {
			restoreState(stateMgr.getUndoData());
		}
		firePropertyChange(PROPERTY_CANUNDO);
		firePropertyChange(PROPERTY_CANREDO);		
	}

	public void redo() {
		if (canRedo()) {
			restoreState(stateMgr.getRedoData());
		}
		firePropertyChange(PROPERTY_CANUNDO);
		firePropertyChange(PROPERTY_CANREDO);		
	}

//	@Override
	public void handleEvent(Event evt) {
		int[] sels = frameEditor.getSelection();
		List<Integer> selList = new ArrayList<Integer>();
		for (int sel : sels) {
			PipAnimateFramePiece piece = curFrame.getPiece(sel);
			if(piece instanceof PipAni4AniFramePiece){
				int hookId = ((PipAni4AniFramePiece)piece).getImageID();
				int i = 0;
				boolean hit = false;
				for(PipAni4AniFramePiece hk:hooks){
					if(hk.getImageID() == hookId){
						hit = true;
						break;
					}
					i++;
				}
				if(hit){
					selList.add(i);
				}
			}
		}
		sels = new int[selList.size()];
		for (int i = 0; i < selList.size(); i++) {
			sels[i] = selList.get(i);
		}
		hookPosList.getTable().setSelection(sels);
	}

	public void fileModified(File f) {
		this.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				bodyDef = new BodyDef();
				postCreateControl();
				frameSelector.refresh();
				frameSelectionChanged(null, frameSelector.getSelectedFrame());
				setDirty(false);
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		FileWatcher.unwatch(this);
	}
	
	/*
	 * 删除选中的动画帧。
	 */
	private void onDeleteFrame() {
		int sel = frameSelector.getSelectedFrame();
		if (animateSet.getAnimateCount() == 1) {
			MessageDialog.openError(getSite().getShell(), "错误", "至少要留一帧。");
			return;
		}
		if (sel < 0 || sel >= animateSet.getFrameCount()) {
			return;
		}
		
		// 删除动画帧
		animateSet.removeFrame(sel);
		
		// 重新设置选择
		frameSelector.setInput(animateSet);
		if (sel >= animateSet.getFrameCount()) {
			sel = animateSet.getFrameCount() - 1;
		}
		frameSelector.setSelectedFrame(sel);
		frameSelector.refresh();
		frameSelectionChanged(null, sel);
		setDirty(true);
		
		// animateset需要修改保存
		File ctsFile = new File(new File(hookFilePath).getParentFile(), bodyDef.ctsFile);
		PipAnimateSet pas = new PipAnimateSet();
		try {
			pas.load(ctsFile);
			pas.removeFrame(sel);
			pas.save(ctsFile, true);
			String ctsPath = ctsFile.getAbsolutePath();
			String ctnPath = Utils.replaceSuffix(ctsPath, "ctn");
			pas.save(new File(ctnPath), false);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", "修改动画文件失败。");
			return;
		}
	}
	
	// 选择一个参考动画文件，自动设置所有挂接点的位置。
	// 参考的动画文件必须和HK文件在同一目录下，并且名为ref.cts。
	// ref.cts必须和HK文件对应的cts有相同的帧数。
	// 如果有N个挂接点（ID从0到N-1），那么，ref.cts必须引用N个PIP文件，名字分别为0.pip到(N-1).pip。
	// 这N个pip文件必须另外有一个单独的同名的CTS文件，描述这个PIP里面每一帧图片的参考位置。
	protected void autoSetHookPos() {
		File hkFile = new File(hookFilePath);
		
		// 检查ref.cts是否存在
		PipAnimateSet refAnimateSet = new PipAnimateSet();
		try {
			refAnimateSet.load(new File(hkFile.getParentFile(), "ref.cts"));
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", "在hk文件同目录下没有找到ref.cts。");
			return;
		}
		
		// 检查ref.cts和hk文件对应的cts有相同的帧数。
		if (refAnimateSet.getFrameCount() != animateSet.getFrameCount()) {
			MessageDialog.openError(getSite().getShell(), "错误", "ref.cts所含帧数和hk文件对应的动画所含帧数不同。");
			return;
		}
		
		// 检查ref.cts是否包含了0.pip到(N-1).pip
		int hookCount = bodyDef.hooks.size();
		int[] pipIndex = new int[hookCount];
		for (int i = 0; i < hookCount; i++) {
			if (bodyDef.hooks.get(i).getImageID() != i) {
				MessageDialog.openError(getSite().getShell(), "错误", "要支持自动设置功能，挂接点ID必须严格从0开始顺序编号。");
				return;
			}
			boolean found = false;
			for (int j = 0; j < refAnimateSet.getFileCount(); j++) {
				if (refAnimateSet.getFileName(j).equals(i + ".pip")) {
					found = true;
					pipIndex[i] = j;
					break;
				}
			}
			if (!found) {
				MessageDialog.openError(getSite().getShell(), "错误", "ref.cts里没有找到" + i + ".pip。");
				return;
			}
		}
		
		// 检查是否存在0.cts到(N-1).cts
		PipAnimateSet[] posAnis = new PipAnimateSet[hookCount];
		for (int i = 0; i < hookCount; i++) {
			posAnis[i] = new PipAnimateSet();
			try {
				posAnis[i].load(new File(hkFile.getParentFile(), i + ".cts"));
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "没有找到" + i + ".cts。");
				return;
			}
			if (posAnis[i].getFileCount() != 0 && !posAnis[i].getFileName(0).equals(i + ".pip")) {
				MessageDialog.openError(getSite().getShell(), "错误", i + ".cts必须引用且只引用" + i + ".pip。");
				return;
			}
			if (posAnis[i].getFrameCount() != posAnis[i].getSourceImage(0).getImgCount()) {
				MessageDialog.openError(getSite().getShell(), "错误", i + ".cts包含帧数必须和" + i + ".pip包含帧数相同。");
				return;
			}
			for (int j = 0; j < posAnis[i].getFrameCount(); j++) {
				PipAnimateFrame refFrame = posAnis[i].getFrame(j);
				if (refFrame.getPieceCount() != 1) {
					MessageDialog.openError(getSite().getShell(), "错误", i + ".cts包含的帧定义必须和" + i + ".pip中的帧一一对应。");
					return;
				}
				if (refFrame.getPiece(0).getImageID() != 0 || refFrame.getPiece(0).getFrame() != j) {
					MessageDialog.openError(getSite().getShell(), "错误", i + ".cts包含的帧定义必须和" + i + ".pip中的帧一一对应。");
					return;
				}
			}
		}
		
		// 扫描整个素体文件，每一帧分别处理
		for (int i = 0; i < animateSet.getFrameCount(); i++) {
			PipAnimateFrame frame = animateSet.getFrame(i);
			int[] hookIndex = new int[hookCount];
			int[] hookLayer = new int[hookCount];
			for (int j = 0; j < hookCount; j++) {
				// 找出在这一帧中这个挂接点的位置
				int index = -1;
				for (int k = 0; k < frame.getPieceCount(); k++) {
					if (frame.getPiece(k) instanceof PipAni4AniFramePiece) {
						PipAni4AniFramePiece pp = (PipAni4AniFramePiece)frame.getPiece(k);
						if (pp.getImageID() == j) {
							index = k;
							break;
						}
					}
				}
				if (index == -1) {
					MessageDialog.openError(getSite().getShell(), "错误", i + "HK文件错误：在第" + i + "帧中没有找到挂接点" + j + "。");
					return;
				}
				hookIndex[j] = index;
				
				// 找出这个挂接点对应的pip文件，在参考CTS中的这一帧中的piece，并且用参考CTS中的位置和层次作为素体中这一个挂接点的位置和层次
				PipAnimateFrame refFrame = refAnimateSet.getFrame(i);
				PipAnimateFramePiece refPiece = null;
				for (int k = 0; k < refFrame.getPieceCount(); k++) {
					if (refFrame.getPiece(k).getImageID() == j) {
						hookLayer[j] = k;
						refPiece = refFrame.getPiece(k);
						break;
					}
				}
				if (refPiece == null) {
					// 没有找到引用，无需设置
					// MessageDialog.openError(getSite().getShell(), "错误", i + "参考文件错误：在第" + i + "帧中没有找到对" + j + ".pip的帧引用。");
					continue;
				}
				
				// 找到引用了，根据提供位置的animateset里的帧位置，计算出挂接点位置
				int baseX = refPiece.getDx();
				int baseY = refPiece.getDy();
				PipAnimateFramePiece posPiece = posAnis[j].getFrame(refPiece.getFrame()).getPiece(0);
				int posx = posPiece.getDx();
				int posy = posPiece.getDy();
				int posw = posAnis[j].getSourceImage(posPiece.getImageID()).getImageData(posPiece.getFrame()).width;
				int posh = posAnis[j].getSourceImage(posPiece.getImageID()).getImageData(posPiece.getFrame()).height;
				int realX;
				int realY;
				switch (refPiece.getTransition()) {
				case 0:
					realX = baseX - posx;
					realY = baseY - posy;
					break;
				case 1:   // 垂直翻转
					realX = baseX - posx;
					realY = baseY + (posy + posh);
					break;
				case 2:   // 水平翻转
					realX = baseX + (posx + posw);
					realY = baseY - posy;
					break;
				case 3:   // 水平+垂直
					realX = baseX + (posx + posw);
					realY = baseY + (posy + posh);
					break;
				case 4:   // 水平+左转90度
					realX = baseX - posy;
					realY = baseY - posx;
					break;
				case 5:   // 右转90度
					realX = baseX + (posy + posh);
					realY = baseY - posx;
					break;
				case 6:   // 左转90度
					realX = baseX - posy;
					realY = baseY + (posx + posw);
					break;
				case 7:   // 水平+右转90度
					realX = baseX + (posy + posh);
					realY = baseY + (posx + posw);
					break;
				default:
					realX = baseX - posx;
					realY = baseY - posy;
					break;
				}
				frame.getPiece(index).setDx(realX);
				frame.getPiece(index).setDy(realY);
			}
			
			// 根据hookLayer中保存的在参考cts中的绘制顺序，重新调整hk文件中piece的顺序
			for (int j = 0; j < hookLayer.length - 1; j++) {
				for (int k = j + 1; k < hookLayer.length; k++) {
					if (hookLayer[j] > hookLayer[k]) {
						int tmp = hookLayer[j];
						hookLayer[j] = hookLayer[k];
						hookLayer[k] = tmp;
						PipAni4AniFramePiece ptmp = (PipAni4AniFramePiece)frame.getPiece(hookIndex[j]);
						PipAni4AniFramePiece ptmp2 = (PipAni4AniFramePiece)frame.getPiece(hookIndex[k]);
						frame.removePiece(hookIndex[j]);
						frame.addPieceAt(hookIndex[j], ptmp2);
						frame.removePiece(hookIndex[k]);
						frame.addPieceAt(hookIndex[k], ptmp);
					}
				}
			}
		}
		
		// 操作成功
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
	}
	
	// 所有挂接点的位置放大一倍
	private void zoomIn() {
		for(int i=0; i<animateSet.getFrameCount(); i++){
			PipAnimateFrame frame = animateSet.getFrame(i);
			for(int j=0; j<frame.getPieceCount(); j++){
				PipAnimateFramePiece piece = frame.getPiece(j);
				if(piece instanceof PipAni4AniFramePiece){
					PipAni4AniFramePiece hook = (PipAni4AniFramePiece) piece;
					hook.setRealDx(hook.getRealDx() * 2);
					hook.setRealDy(hook.getRealDy() * 2);
				}
			}
		}
		setDirty(true);
		
		frameEditor.redraw();
		frameSelector.refresh();
	}

	// 所有挂接点的位置缩小一倍
	private void zoomOut() {
		for(int i=0; i<animateSet.getFrameCount(); i++){
			PipAnimateFrame frame = animateSet.getFrame(i);
			for(int j=0; j<frame.getPieceCount(); j++){
				PipAnimateFramePiece piece = frame.getPiece(j);
				if(piece instanceof PipAni4AniFramePiece){
					PipAni4AniFramePiece hook = (PipAni4AniFramePiece) piece;
					hook.setRealDx(hook.getRealDx() / 2);
					hook.setRealDy(hook.getRealDy() / 2);
				}
			}
		}	
		setDirty(true);

		frameEditor.redraw();
		frameSelector.refresh();
	}
	
	/*
	 * 从挂接点列表中提取选中的挂接点，从下方帧列表中提取选中的帧，选择一个挂接点文件所在目录，自动提取挂接点位置设置。
	 */
	private void autoSetHookPosFromImage() {
		// 挂接点列表必须选择一个挂接点
		StructuredSelection sel = (StructuredSelection)hookPosList.getSelection();
		Object[] sels = sel.toArray();
		if (sels.length != 1) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "请从右上方列表中选择一个挂接点。");
			return;
		}
		PipAni4AniFramePiece hook = (PipAni4AniFramePiece)sels[0];
		
		// 帧列表中必须选择多个帧
		int[] selFrames = frameSelector.getSelectedFrames();
		if (selFrames.length == 0) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "请从下方帧选择区域选择动画帧。");
			return;
		}
		Arrays.sort(selFrames);
		
		// 选择参考点文件目录
		String refDirName = ImageEditor.chooseDir(getSite().getShell(), "请选择参考点图片目录");
		if (refDirName == null) {
			return;
		}
		File refDir = new File(refDirName);
		String[] refArr = Utils.listFile(refDir, "png");
		if (refArr.length != selFrames.length) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", "此目录中有" + refArr.length + "个图片文件，需要" + selFrames.length + "个。");
			return;
		}
		Arrays.sort(refArr);
		
		// 从选中的目录中读取图片文件，从中分析出挂接点的位置，设置到帧中
		for (int i = 0; i < refArr.length; i++) {
			int[] hookPos = AutoBody.getHookPoint(new File(refDir, refArr[i]));
			PipAnimateFrame frame = animateSet.getFrame(selFrames[i]);
			PipAni4AniFramePiece hookInFrame = frame.getHook(hook.getImageID());
			hookInFrame.setRealDx(hookPos[0]);
			hookInFrame.setRealDy(hookPos[1]);
			animateSet.syncMirrorFrame(frame);
		}
		
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
		
		MessageDialog.openInformation(getSite().getShell(), "成功", "已设置" + selFrames.length + "帧。");
	}
}
