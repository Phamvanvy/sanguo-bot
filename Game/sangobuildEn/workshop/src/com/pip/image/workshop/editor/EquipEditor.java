/**
 * 
 */
package com.pip.image.workshop.editor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.DirectoryView;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.EquipAnimateViewer.EquipAnimateSaver;
import com.pip.util.Utils;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;

/**
 * @author jhkang
 * 
 */
public class EquipEditor extends EditorPart {

	public class SelectHookOnBodyDialog extends Dialog{

		private Combo hookOp;
		private String[] hookNames;
		private int selIdx;

		protected SelectHookOnBodyDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = 5;
			gridLayout.marginHeight = 5;
			gridLayout.verticalSpacing = 0;
			gridLayout.horizontalSpacing = 0;
			container.setLayout(gridLayout);

			Label selHookLabel = new Label(container, SWT.None);
			selHookLabel.setText("选择挂接点:");
			hookOp = new Combo(container, SWT.READ_ONLY);
			hookOp.setItems(hookNames);
			hookOp.select(0);
			return container;
		}
		
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("选择挂接点");
		}
		
		public void setHookNames(String[] hookNames2) {
			hookNames = hookNames2;
		}
		/**
		 * Create contents of the button bar
		 * @param parent
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, "确定",
					true);
			createButton(parent, IDialogConstants.CANCEL_ID,
					"取消", false);
		}
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				selIdx = hookOp.getSelectionIndex(); 
			}
			super.buttonPressed(buttonId);
		}
		public int getSelHookIdx() {
			return selIdx;
		}
		
	}
	/**
	 * @author jhkang
	 * 
	 */
	public class AnimateContentTreeProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object arg0) {
			if(arg0 instanceof List){
				return ((List)arg0).toArray();
			}else if (arg0 instanceof PipAnimateSet) {
				PipAnimateSet animateSet = (PipAnimateSet) arg0;
				int cnt = animateSet.getAnimateCount();
				PipAnimate[] ret = new PipAnimate[cnt];
				for (int i = 0; i < cnt ; i++) {
					ret[i] = (animateSet.getAnimate(i));
				}
				return ret;
			}else if(arg0 instanceof PipAnimate){
				PipAnimate animate = (PipAnimate) arg0;
				int cnt = animate.getFrameCount();
				PipAnimateFrameRef[] frameRefs = new PipAnimateFrameRef[cnt];
				for(int i=0; i<cnt; i++){
					frameRefs[i] = animate.getFrame(i);
				}
				return frameRefs;
			}
			return null;
		}

		public Object[] getElements(Object arg0) {
			return getChildren(arg0);
		}

		public Object getParent(Object arg0) {
			if(arg0 instanceof PipAnimate){
				return ((PipAnimate)arg0).getParent();
			}else if(arg0 instanceof PipAnimateFrameRef){
				return ((PipAnimateFrameRef)arg0).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object arg0) {
			if(arg0 instanceof List){
				return ((List)arg0).size()>0;
			}else if(arg0 instanceof PipAnimateSet){
				return ((PipAnimateSet)arg0).getAnimateCount()>0;
			}else if(arg0 instanceof PipAnimate){
				return ((PipAnimate)arg0).getFrameCount()>0;
			}
			return false;
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

		}

	}
	
	class AnimateLabelProvider extends LabelProvider{
		public Image getImage(Object obj) {
			if(obj instanceof PipAnimateSet){
				return WorkshopPlugin.getDefault().getImageRegistry().get("grid");
			}else if(obj instanceof PipAnimate){
				return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
			}else if(obj instanceof PipAnimateFrameRef){
				return WorkshopPlugin.getDefault().getImageRegistry().get("image");
			}
			return null;
		}
		public String getText(Object obj) {
			if(obj instanceof PipAnimateSet){
				int idx = bodyAniSets.indexOf(obj);
				return eqp2hook.getHookFileName(idx);
			}else if(obj instanceof PipAnimate){
				int aniIdxInAniSet = 0;
				//取此动画在动画组中的下标
				PipAnimate pa = (PipAnimate) obj;
				PipAnimateSet pas = pa.getParent();
				int cnt = pas.getAnimateCount();
				for(int i=0; i<cnt; i++){
					if(pas.getAnimate(i)==obj){
						aniIdxInAniSet = i;
					}
				}
				//返回下标和动画名称
				return aniIdxInAniSet+":"+((PipAnimate)obj).getName();
			}else if(obj instanceof PipAnimateFrameRef){
				int frameIdxInAniSet = ((PipAnimateFrameRef)obj).getFrame();
				return frameIdxInAniSet+":"+((PipAnimateFrameRef)obj).realize().getName();
			}
			return obj.toString();
		}
	}
	private EquipAnimateViewer bodyAnimateViewer;
	private TreeViewer bodyAniTree;
	private AnimateContentTreeProvider animateContentTreeProvider;

	private PipAnimateSet bodyAniSet;
	private List<PipAnimateSet> bodyAniSets = new ArrayList<PipAnimateSet>();
	/**
	 * 当前选择的body的帧
	 */
	private PipAnimateFrame curBodyFrame;
	private PipAnimateSet equipAniSet;
	private int selHookId = -1;
	private String selHookName;
	private Label curHookLabel;
	private List<BodyDef> bodyDefs = new ArrayList<BodyDef>();
	private TileLibSelector eqpAniView;
	private int curBodyFrameIdxInAniSet;
	private Thread hookAniDriver;
	private boolean dirty;
	/**
	 * eqp file, not equipment cts file
	 */
	private String equipFilePath;
	private EquipHookMap eqp2hook = new EquipHookMap();
	private Label equipCtsNameLabel;
	private String equipCtsName;
	private CheckboxTableViewer levelTableView;
	protected Set<PipAnimateSet> changedBodies = new HashSet<PipAnimateSet>();

	/**
	 * 
	 */
	public EquipEditor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 绑定至选中的动画所包含的各个帧
	 */
	protected void bindToAllFrame() {
		int selEquipIdx = checkEquipAniSel();
		if(selEquipIdx<0){
			return;
		}
		if(bodyAniSets.size()==0){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请先添加素体.");
			return;
		}
		if(bodyAniSet == null){
			MessageDialog.openInformation(getSite().getShell(), "Info", "请先选择素体.");
			return;
		}
		IStructuredSelection sel = (IStructuredSelection)bodyAniTree.getSelection();
		if (sel.isEmpty()) {
			MessageDialog.openInformation(getSite().getShell(), "Info", "请先选择形象动画.");
			return ;
		}
		Object selObj = sel.getFirstElement();
		if(selObj instanceof PipAnimate){
			PipAnimate equipPa = equipAniSet.getAnimate(selEquipIdx);
			PipAnimate bodyPa = (PipAnimate) selObj;
			int cnt = bodyPa.getFrameCount();
			for(int i=0; i<cnt; i++){
				PipAnimateFrame frame = bodyPa.getFrame(i).realize();
				frame.getHook(selHookId).bindAnimate(equipPa);
			}
			bodyAnimateViewer.redraw();
			setDirty(true);
			runHookAniDriver();
		}else{
			MessageDialog.openInformation(getSite().getShell(), "Info", "请先选择形象动画.(注意不是选择形象帧)");
			return;
		}
	}

	protected void bodyAniTreeSelChange() {
		IStructuredSelection sel = (IStructuredSelection)bodyAniTree.getSelection();
		if (sel.isEmpty()) {
			return ;
		}
		Object selObj = sel.getFirstElement();
		if(selObj instanceof PipAnimateSet){
			curBodyFrame = null;
			bodyAniSet = (PipAnimateSet) selObj;
			changeSelBody();
			bodyAnimateViewer.stop();
			bodyAnimateViewer.setInput(null);
			bodyAnimateViewer.redraw();
		}else if(selObj instanceof PipAnimate){
			bodyAniSet = ((PipAnimate)selObj).getParent();
			changeSelBody();
			bodyAnimateViewer.setInput((PipAnimate)selObj);
			bodyAnimateViewer.setCurrentFrame(0);
			bodyAnimateViewer.play();
			curBodyFrame = null;
//			bodyAnimateViewer.redraw();
		}else if(selObj instanceof PipAnimateFrameRef){
			bodyAniSet = ((PipAnimateFrameRef)selObj).getParent().getParent();
			changeSelBody();
			curBodyFrameIdxInAniSet = ((PipAnimateFrameRef)selObj).getFrame();
			curBodyFrame = bodyAniSet.getFrame(curBodyFrameIdxInAniSet);
			bodyAnimateViewer.stop();
			PipAnimate pa = (PipAnimate) animateContentTreeProvider.getParent(selObj);
			bodyAnimateViewer.setInput(pa);
			int cnt = pa.getFrameCount();
			int frameIdxInAni = 0;
			for(int i=0; i<cnt; i++){
				if(pa.getFrame(i) == selObj){
					frameIdxInAni = i;
					break;
				}
			}
			bodyAnimateViewer.setCurrentFrame(frameIdxInAni);
			bodyAnimateViewer.redraw();
			runHookAniDriver();
		}
	}

	private void createLeftBottom(Composite parent){
		eqpAniView = new TileLibSelector(parent, SWT.None);
	}

	private void createLeftPart(Composite parent){
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createLeftTop(sashForm);
		createLeftBottom(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });
	}

	private void createLeftTop(Composite parent){
//		bodyAnimateViewer = new AnimateViewer(parent, SWT.NONE);
		bodyAnimateViewer = new EquipAnimateViewer(parent, SWT.NONE);
		((EquipAnimateViewer)bodyAnimateViewer).setEquipAniSaver(new EquipAnimateSaver(){
			public boolean saveEquipAnimate(PipAnimate equipAni) {
//				saveEquipAnimateSet();
				setDirty(true);
				return true;
			}

			public void equipCtsModifed() {
				setDirty(true);
			}

			public void hookPosChanged() {
				setDirty(true);
				changedBodies.add(bodyAniSet);
			}
		});
	}
	protected void saveEquipAnimateSet() {
		try{
			File dir = new File(equipFilePath).getParentFile();
			File cts = new File(dir, equipCtsName);
			equipAniSet.save(cts, true);
			File ctn = new File(dir, equipCtsName.replaceAll(".cts$", ".ctn"));
			equipAniSet.save(ctn, false);
		}catch(Exception e){
			MessageDialog.openError(getSite().getShell(), "Error", "保存装备动画出错\n"+e);
			e.printStackTrace();
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createLeftPart(sashForm);
		createRighPart(sashForm);
		sashForm.setWeights(new int[] { 2, 1 });
		postCreatePartControl();
	}
	private void postCreatePartControl() {
		this.setPartName(this.getEditorInput().getName());
//		String dirPath = "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\";
		File equipFile = new File(equipFilePath);
		File dir = equipFile.getParentFile();
		String dirPath = dir.getAbsolutePath()+File.separator;
		try{
			String hookFileName;
			PipAnimateSet pas;
			eqp2hook.load(equipFilePath);

			equipCtsName = eqp2hook.getEquipCtsName();
			File equipCtsFile = new File(dir, equipCtsName);
			if(equipCtsFile.exists()==false){
				adjustEqp(equipFilePath);
				eqp2hook = new EquipHookMap();
				eqp2hook.load(equipFilePath);
				equipCtsName = eqp2hook.getEquipCtsName();
			}
			equipCtsNameLabel.setText("当前装备文件:"+equipCtsName);
			equipCtsNameLabel.setSize(equipCtsNameLabel.computeSize(-1, -1));
			pas = new PipAnimateSet();
			pas.load(new File(dir, equipCtsName));
			equipAniSet = pas;
			eqpAniView.setInput(pas, 1, 1);
			eqpAniView.redraw();
			
			int bodyCnt = eqp2hook.getBodyCnt();
			for(int i=0; i<bodyCnt; i++){
				hookFileName = eqp2hook.getHookFileName(i);
				BodyDef bodyDef = new BodyDef();
				pas = loadBody(dirPath, hookFileName, bodyDef);
				bodyAniSets.add(pas);
				bodyDefs.add(bodyDef);
				int hookId = eqp2hook.getHookId(i);
				bodyDef.embedHookPieces(pas, hookId );
				eqp2hook.doEquip(pas, equipAniSet, i);
			}
			bodyAniTree.setInput(bodyAniSets);
			updateLevelTable();
		}catch(Exception e){
			MessageDialog.openError(getSite().getShell(), "Error", "初始化错误:\n"+e);
			e.printStackTrace();
		}
	}

	private void adjustEqp(String equipFilePath2) {
		DirectoryView view = (DirectoryView) getSite().getPage().findView(DirectoryView.ID);
		view.adjustEqp(equipFilePath2, false);
	}

	/**
	 * 扫描此装备的其他级别
	 */
	private void updateLevelTable() {
		String pattern = this.equipCtsName;
		pattern = pattern.replace(".cts", "");
		pattern = pattern.substring(0, pattern.lastIndexOf("_")+1);
		pattern += "\\d+\\.cts$";
		final String regx = pattern;
		
		File dir = new File(this.equipFilePath).getParentFile();
		String[] names = dir.list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.matches(regx);
			}
		});
		levelTableView.setInput(names);
	}
	public static void main(String[] args){
		String pattern = "abc_1.cts";
		pattern = pattern.replace(".cts", "");
		pattern = pattern.substring(0, pattern.lastIndexOf("_")+1);
		pattern += "\\d+\\.cts$";
		
		System.out.println("abc_2.cts".matches(pattern));
		System.out.println("abc_20.cts".matches(pattern));
		System.out.println("abc_.cts".matches(pattern));
		System.out.println("abc_z3.cts".matches(pattern));
	}
	/**
	 * 加载素体(.hk)文件,返回此素体关联的形象动画组
	 * @param dir
	 * @param hookFilePath
	 * @param bodyDef
	 * @return
	 * @throws IOException
	 */
	private PipAnimateSet loadBody(String dir, String hookFilePath, BodyDef bodyDef) throws IOException {
		if(!dir.endsWith(File.separator)){
			dir += File.separator;
		}
		bodyDef.loadHooks(dir+hookFilePath);
		String bodyCtsName;
		bodyCtsName = bodyDef.ctsFile;
		PipAnimateSet pas = new PipAnimateSet();
		pas.load(new File(new File(dir + hookFilePath).getParentFile(), bodyCtsName));
		return pas;
	}

	private void createRighPart(Composite parent){
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRightTop(sashForm);
		createRightBottom(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });
	}
	private void createRightBottom(Composite parent){
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRightBottomOperation(sashForm);
		createRightBottomLevelList(sashForm);
		sashForm.setWeights(new int[] { 4, 3 });
	}
	private void createRightBottomLevelList(Composite parent){
		parent = new Composite(parent, SWT.BORDER);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		parent.setLayout(gridLayout);
		
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("匹配到的级别:");
		
		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		button.setText("刷新");
		button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLevelTable();
			}
		});
		///level table
		levelTableView = CheckboxTableViewer.newCheckList(parent, SWT.BORDER|SWT.FULL_SELECTION);
		levelTableView.setContentProvider(new IStructuredContentProvider(){

			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}

			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
			
		});
		levelTableView.setLabelProvider(new LabelProvider());
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalSpan = 2;
		levelTableView.getControl().setLayoutData(gridData);
		
	}
	private void createRightBottomOperation(Composite parent){
		Composite container = new Composite(parent, SWT.None);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);
		
		equipCtsNameLabel = new Label(container, SWT.NONE);
		equipCtsNameLabel.setText("当前装备文件:");
		
		Button renameEquipCtsName = new Button(container, SWT.PUSH);
		renameEquipCtsName.setText("重命名装备文件名称");
		renameEquipCtsName.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				renameEquipCtsName();
			}
		});
		
		Button addBodyBtn = new Button(container, SWT.PUSH);
		addBodyBtn.setText("增加素体");
		addBodyBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				addBody();
			}
		});
		
		Button removeBodyBtn = new Button(container, SWT.PUSH);
		removeBodyBtn.setText("移除素体");
		removeBodyBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				removeBody();
			}
		});
		
		curHookLabel = new Label(container, SWT.NONE);
		curHookLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		curHookLabel.setText("当前挂接点:");
		
		Button btn = new Button(container, SWT.PUSH);
		btn.setText("装配到当前形象动画");
		btn.setToolTipText("装配到选中的动画里的各个帧上");
		btn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				bindToAllFrame();
			}
		});
		
		Button bindToSelBodyFrame = new Button(container, SWT.PUSH);
		bindToSelBodyFrame.setText("装配到当前形象帧");
		bindToSelBodyFrame.setToolTipText("将选择的装备动画装配到选中的形象帧上");
		bindToSelBodyFrame.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				bindToBodyFrame(curBodyFrame);
			}
		});
		
		Button unloadBtn = new Button(container, SWT.PUSH);
		unloadBtn.setText("从当前形象帧卸载");
		unloadBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				unbindFromCurFrame();
			}
		});
	}
	protected void renameEquipCtsName() {
		File dir = new File(this.equipFilePath).getParentFile();
		File equipCtsFile = new File(dir, this.equipCtsName);
		IEditorPart editor = null;
		String equipCtsPath = new File(dir, this.equipCtsName).getAbsolutePath();
        IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(equipCtsPath));
        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
        editor = getSite().getWorkbenchWindow().getActivePage().findEditor(input);
        if (editor != null) {
        	MessageDialog.openInformation(getSite().getShell(), "确认", "装备动画正在被编辑,请先关闭.");
            getSite().getWorkbenchWindow().getActivePage().activate(editor);
        }else{
        	InputDialog inputDlg = new InputDialog(getSite().getShell(), "输入", "新文件名", equipCtsFile.getName(), new IInputValidator(){

				public String isValid(String newText) {
					if(newText == null || newText.equals("")){
						return "文件名不能为空";
					}
					return null;
				}
        		
        	});
        	int ret = inputDlg.open();
        	if(ret != InputDialog.OK){
        		return;
        	}
        	String newName = inputDlg.getValue();
        	if(!newName.endsWith(".cts")){
        		newName += ".cts";
        	}
        	boolean succ = equipCtsFile.renameTo(new File(dir, newName));
        	if(!succ){
        		MessageDialog.openWarning(getSite().getShell(), "警告", "重命名失败");
        	}else{
        		eqp2hook.setEquipCtsName(newName);
        		equipCtsName = newName;
        		equipCtsNameLabel.setText("当前装备文件:"+equipCtsName);
    			equipCtsNameLabel.setSize(equipCtsNameLabel.computeSize(-1, -1));
    			setDirty(true);
        		System.out.println("EquipEditor.renameEquipCtsName() renameOK");
//        		DirectoryView view = (DirectoryView) getSite().getWorkbenchWindow().getActivePage().findView(DirectoryView.ID);
//        		view.
        	}
        }
	}

	protected void removeBody() {
		if(bodyAniSet == null){
			MessageDialog.openInformation(getSite().getShell(), "Error", "请先选择素体.");
			return;
		}
		int idx = bodyAniSets.indexOf(bodyAniSet);	
		String selBodyName = eqp2hook.getHookFileName(idx);
		boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认", "确定移除素体:"+selBodyName+"?");
		if(ret){
			curBodyFrame = null;
			eqp2hook.remove(idx);
			bodyDefs.remove(idx);
			bodyAniSets.remove(idx);
			bodyAniTree.refresh();
			bodyAniSet = null;
			bodyAnimateViewer.setInput(bodyAniSet);
			bodyAnimateViewer.redraw();
			changeSelBody();
			setDirty(true);
		}
	}

	protected void bindToBodyFrame(PipAnimateFrame bodyFrame) {
		if(selHookId < 0){
			MessageDialog.openInformation(getSite().getShell(), "Error", "请先选择素体");
			return;
		}
		if(bodyFrame == null){
			MessageDialog.openInformation(getSite().getShell(), "Error", "请选择要挂接到的帧.");
			return;
		}
		int selEquipIdx = checkEquipAniSel(); 
		if(selEquipIdx<0){
			return;
		}
		
		PipAni4AniFramePiece hook = bodyFrame.getHook(selHookId);
		if(hook==null){
			String alertMsg = null;
			alertMsg = "没有找到挂接点 "+selHookId+":"+selHookName+"\n" +
					"在帧"+curBodyFrameIdxInAniSet+":"+bodyFrame.getName();
			MessageDialog.openError(getSite().getShell(), "Error", alertMsg);
			return;
		}
		PipAnimate pa = equipAniSet.getAnimate(selEquipIdx);
		hook.bindAnimate(pa);
		setDirty(true);
		bodyAnimateViewer.redraw();
		runHookAniDriver();
	}

	private int checkEquipAniSel() {
		int[] selEqpAniIds = this.eqpAniView.getSelectedFrames();
		if(selEqpAniIds.length == 0){
			MessageDialog.openInformation(getSite().getShell(), "Error", "请选择一个装备动画.");
			return -1;
		}else if(selEqpAniIds.length>1){
			MessageDialog.openInformation(getSite().getShell(), "Error", "你选择了多个装备动画.只允许选择一个进行挂载.");
			return -1;
		}
		return selEqpAniIds[0];
	}

	private void runHookAniDriver() {
		if(hookAniDriver == null){
			hookAniDriver = new Thread(new Runnable(){
				public void run() {
					while(true){
						if(equipAniSet == null){
							break;
						}
						if(curBodyFrame != null && curBodyFrame.getHook(selHookId).binded()){
							Display dsp = getSite().getShell().getDisplay();
							dsp.asyncExec(new Runnable() {
								public void run() {
									if(bodyAnimateViewer.isDisposed()){
										return;
									}
									bodyAnimateViewer.redraw();
								}
							});
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			hookAniDriver.start();
		}
	}

	protected void unbindFromCurFrame() {
		if(curBodyFrame != null){
			PipAni4AniFramePiece hook = curBodyFrame.getHook(selHookId);
			hook.unbind();
			setDirty(true);
			bodyAnimateViewer.redraw();
		}else{
			MessageDialog.openInformation(getSite().getShell(), "info", "请先选择帧");
		}
	}

	protected void addBody() {
		FileDialog fdlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		fdlg.setFilterExtensions(new String[]{"*.hk"});
		fdlg.setFilterNames(new String[]{"素体文件(*.hk)"});
		String restrictDir = new File(equipFilePath).getParentFile().getAbsolutePath();
		fdlg.setFilterPath(restrictDir);
		String file = fdlg.open();
		if (file != null) {
			// 素体文件和装备文件必须在同一个盘上
			String relatePath = Utils.getRelatePath(file, restrictDir);
			if (relatePath == null) {
				MessageDialog.openInformation(getSite().getShell(), "Info", "素体文件必须和装备文件位于同一磁盘中。");
				return;
			}
			if (eqp2hook.hasBody(relatePath)) {
				MessageDialog.openInformation(getSite().getShell(), "Info", "素体文件"+relatePath+"已经添加过了.");
				return ;
			}
//			String fullFilePath = dir+File.separator+filenames[0]; 
			BodyDef bodyDef = new BodyDef();
			PipAnimateSet newAddBodyPas = null;
			try {
				newAddBodyPas = loadBody(restrictDir, relatePath, bodyDef);
			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Error", "加载素体文件错误:\n"+e);
				e.printStackTrace();
				return;
			}
			String[] hookNames = bodyDef.getHookNames();
			SelectHookOnBodyDialog dlg = new SelectHookOnBodyDialog(getSite().getShell());
			dlg.setHookNames(hookNames);
			if(dlg.open() == Dialog.OK){
				int selHookIdx = dlg.getSelHookIdx();
				selHookId = bodyDef.hooks.get(selHookIdx).getImageID();
				selHookName = bodyDef.hooks.get(selHookIdx).name;
				eqp2hook.addHookFileName(relatePath);
				eqp2hook.addHookId(selHookId);
				bodyAniSets.add(newAddBodyPas);
				bodyDefs.add(bodyDef);
				newBodyAdded(bodyDef);
				setDirty(true);
			}
		}
	}

	private void newBodyAdded(BodyDef bodyDef) {
		bodyAniTree.refresh();
		bodyAniSet = bodyAniSets.get(bodyAniSets.size()-1);
		bodyDef.embedHookPieces(bodyAniSet, selHookId);
		updateHookLabel();
	}
	private void changeSelBody() {
		if(bodyAniSet == null){
			selHookId = -1;
			selHookName = null;
			updateHookLabel();
			return;
		}
		int idx = bodyAniSets.indexOf(bodyAniSet);
		selHookId = eqp2hook.getHookId(idx);
		bodyAnimateViewer.setBindHookId(selHookId);
		for(PipAni4AniFramePiece hook:bodyDefs.get(idx).hooks){
			if(hook.getImageID() == selHookId){
				selHookName = hook.name;
				break;
			}
		}
		updateHookLabel();
	}

	private void updateHookLabel(){
		if(selHookId<0){
			curHookLabel.setText("未选择素体");
			return;
		}
		///update label
		curHookLabel.setText("当前挂接点:"+selHookId+":"+selHookName);
		Point p = curHookLabel.getSize();
		p = curHookLabel.computeSize(-1, p.y, true);
		curHookLabel.setSize(p);
		curHookLabel.redraw();
	}

	@Override
	public void dispose() {
		super.dispose();
		equipAniSet = null;//make hookAniDriver thread exit;
	}

	private void createRightTop(Composite parent){
		bodyAniTree = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		animateContentTreeProvider = new AnimateContentTreeProvider();
		bodyAniTree.setContentProvider(animateContentTreeProvider);
		bodyAniTree.setLabelProvider(new AnimateLabelProvider());
		bodyAniTree.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent arg0) {
				bodyAniTreeSelChange();
			}
		});
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor arg0) {
//		String filePath = "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\male3.eqp";
		try {
			eqp2hook.save(equipFilePath, bodyAniSets, equipAniSet);
			if(bodyAnimateViewer.isEquipCtsModified()){
				saveEquipAnimateSet();
				bodyAnimateViewer.setEquipCtsModified(false);
			}
			
			if(bodyAnimateViewer.isHookPosChanged()){
				saveChangedBodyDef();
				bodyAnimateViewer.setHookPosChanged(false);
			}
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "Error", "Error save equip-hook mapping.\n"+e);
			e.printStackTrace();
		}
		setDirty(false);
		firePropertyChange(PROP_DIRTY);
	}
	private void saveChangedBodyDef() throws Exception {
		File dir = new File(equipFilePath).getParentFile();
		for(PipAnimateSet bodyAni:changedBodies){
			int idx = bodyAniSets.indexOf(bodyAni);
			BodyDef bodyDef = bodyDefs.get(idx);
			String file = eqp2hook.getHookFileName(idx);
			File f = new File(dir, file);
			bodyDef.save(bodyAni, f.getAbsolutePath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		equipFilePath = Utils.urlToPath(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean b){
		dirty = b;
		firePropertyChange(PROP_DIRTY);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
