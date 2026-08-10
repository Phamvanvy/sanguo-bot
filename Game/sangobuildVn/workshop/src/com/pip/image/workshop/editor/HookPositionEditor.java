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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.jdom.Document;
import org.jdom.Element;

import com.pip.image.workshop.editor.AnimateFrameEditor.FramePieceSelectedListener;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.Utils;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateFrameRef;
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
		gridLayout.numColumns = 6;
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
		zoomInHook.setText("放大");
		zoomInHook.setToolTipText("放大挂接点以便调整层次,位置");
		zoomInHook.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				zoomHook(1);
			}
		});
		
		Button zoomOutHook = new Button(composite, SWT.PUSH);
		zoomOutHook.setText("缩小");
		zoomOutHook.setToolTipText("缩小挂接点");
		zoomOutHook.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				zoomHook(-1);
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
		positionOps.setLayout(new GridLayout(5, false));
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
		
		Label label3 = new Label(positionOps, SWT.NONE);
		label3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
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
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
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
			piecesTableView.refresh();
		}		
	}

	protected void hookSelected() {
		int idx = hookPosList.getTable().getSelectionIndex();
		if(idx<0){
			return;
		}
		PipAni4AniFramePiece hook = hooks.get(idx);
		for(int i=0; i<curFrame.getPieceCount(); i++){
			PipAnimateFramePiece piece = curFrame.getPiece(i);
			if(piece.getFrame() == 0xFF && piece.getImageID() == hook.getImageID()){
				frameEditor.setSelectedPiece(i);
				frameEditor.redraw();
				break;
			}
		}
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
		PipAnimateFramePiece piece = curFrame.getPiece(evt.index);
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
				hookPosList.getTable().setSelection(i);
			}
		}
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
}
