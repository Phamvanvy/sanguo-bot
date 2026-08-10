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
	private CheckboxTableViewer hookPosList;
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
	private int copyDx;
	private int copyDy;
	private int copyHooKId = -1;
	private FramePiecesView piecesView;
	private int pieceIndex;
	private int allHookFromFrame;
	
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
		
		
		hookPosList = CheckboxTableViewer.newCheckList(composite, SWT.BORDER|SWT.FULL_SELECTION);
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
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.center = true;
		rowLayout.spacing = 1;
		positionOps.setLayout(rowLayout);
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
		
		Button copyAllHook = new Button(positionOps, SWT.PUSH);
		copyAllHook.setText("复制全部");
		copyAllHook.setToolTipText("复制当前帧所有挂接点的位置和层次");
		copyAllHook.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				copyAllHook();
			}
		});
		
		Button pasteAllHook = new Button(positionOps, SWT.PUSH);
		pasteAllHook.setText("粘贴全部");
		pasteAllHook.setToolTipText("粘贴当前帧所有挂接点的位置和层次");
		pasteAllHook.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				pasteAllHook();
			}
		});
		
		Label label3 = new Label(positionOps, SWT.NONE);
		label3.setText("已复制 挂接点ID -1 帧 -1");
		label3.setEnabled(false);
		copyHookPosBtn.setData(label3);
	}

	protected void pasteHookPos2all() {
		if(copyHooKId<0){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请先从其他帧复制一个挂接点的位置");
			return;
		}
		int frameCnt = animateSet.getFrameCount();
		for(int i=0; i<frameCnt; i++){
			if(copiedFrameIdx == i){
				continue;
			}
			PipAni4AniFramePiece hookInFrame = animateSet.getFrame(i).getHook(copyHooKId);
			hookInFrame.setRealDx(copyDx);
			hookInFrame.setRealDy(copyDy);
		}
		frameEditor.redraw();
		frameSelector.refresh();
	}

	protected void flipHookPos() {
		int idx = hookPosList.getTable().getSelectionIndex();
		if(idx<0){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请从列表中选择挂接点");
			return;
		}
		PipAni4AniFramePiece hook = hooks.get(idx);
		PipAni4AniFramePiece hookInFrame = curFrame.getHook(hook.getImageID());
		int x = 0 - hookInFrame.getRealDx();
		hookInFrame.setRealDx(x);
		frameEditor.redraw();
		frameSelector.refresh();
		setDirty(true);
	}

	protected void pasteAllHook(){
		if(allHookFromFrame<0){
			return;
		}
		if(allHookFromFrame>=animateSet.getFrameCount()){
			return;
		}
		PipAnimateFrame frame = animateSet.getFrame(allHookFromFrame);
		if(curFrame == null){
			return;
		}
		if(frame.getPieceCount() != curFrame.getPieceCount()){
			MessageDialog.openInformation(getSite().getShell(), "Info", "两个帧的图块数不相等,不能粘贴");
			return;
		}
		int curFrameIdx = frameSelector.getSelectedFrame();
		HashMap<Integer, PipAni4AniFramePiece> chaosPieces = new LinkedHashMap<Integer, PipAni4AniFramePiece>();
		int cnt;
		cnt = curFrame.getPieceCount();
		for(int i=cnt - 1; i>=0; i--){
			PipAnimateFramePiece p = curFrame.getPiece(i);
			if(p instanceof PipAni4AniFramePiece){
				chaosPieces.put(p.getImageID(), (PipAni4AniFramePiece) p);
				curFrame.removePiece(i);
			}
		}
		PipAnimateFrame fromFrame = animateSet.getFrame(allHookFromFrame);
		cnt = fromFrame.getPieceCount();
		for(int i=0; i<cnt; i++){
			PipAnimateFramePiece p = fromFrame.getPiece(i);
			if(p instanceof PipAni4AniFramePiece){
				int hookId = p.getImageID();
				PipAni4AniFramePiece p2 = chaosPieces.get(hookId);
				p2.setRealDx(((PipAni4AniFramePiece) p).getRealDx());
				p2.setRealDy(((PipAni4AniFramePiece) p).getRealDy());
				curFrame.addPieceAt(i, p2);
			}
		}
		setDirty(true);
		piecesView.piecesTableView.refresh();
		frameEditor.redraw();
		frameSelector.refresh();
	}
	
	protected void copyAllHook(){
		allHookFromFrame = frameSelector.getSelectedFrame();
	}

	protected void pasteHookPos() {
		if(copyHooKId<0){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请先从其他帧复制一个挂接点的位置");
			return;
		}
		PipAni4AniFramePiece hookInFrame = curFrame.getHook(copyHooKId);
		hookInFrame.setRealDx(copyDx);
		hookInFrame.setRealDy(copyDy);
		int idx = curFrame.getPieceIndex(hookInFrame);
		if(idx>pieceIndex){
			curFrame.removePiece(idx);
			curFrame.addPieceAt(pieceIndex , hookInFrame);
		}else if(idx<pieceIndex){
			curFrame.removePiece(idx);
			curFrame.addPieceAt(pieceIndex - 1, hookInFrame);
		}
		piecesView.piecesTableView.refresh();
		frameEditor.redraw();
		frameSelector.refresh();
	}

	protected void copyHookPos(Button copyBtn) {
		int idx = hookPosList.getTable().getSelectionIndex();
		if(idx<0){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请从列表中选择挂接点");
			return;
		}
		PipAni4AniFramePiece hook = hooks.get(idx);
		PipAni4AniFramePiece hookInFrame = curFrame.getHook(hook.getImageID());
		pieceIndex = curFrame.getPieceIndex(hookInFrame);
		copyHooKId = hook.getImageID();
		copyDx = hookInFrame.getRealDx();
		copyDy = hookInFrame.getRealDy();
		copiedFrameIdx = frameSelector.getSelectedFrame();
		//update label
		if(copyBtn==null){
			return;
		}
		Label copyInfo = (Label) copyBtn.getData();
		copyInfo.setText("已复制 挂接点ID"+hook.getImageID()+" 帧"+frameSelector.getSelectedFrame());
		copyInfo.setEnabled(true);
//		copyInfo.setSize(copyInfo.computeSize(-1, -1));
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
		hk.setImageID(nextHookId);
		hk.setDx((hk.getWidth()+2)*(nextHookId+1));
		hk.setDy(5);
		hooks.add(hk);
		for(int i=0; i<animateSet.getFrameCount(); i++){
			animateSet.getFrame(i).addPiece((PipAnimateFramePiece) hk.clone());
		}
		frameEditor.redraw();
		frameSelector.refresh();
		hookPosList.refresh();
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
		frameEditor.setPieceSelectedListener(this);
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
	public void pieceSelected(int idx) {
		PipAnimateFramePiece piece = curFrame.getPiece(idx);
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
}
