/**
 * 
 */
package com.pip.image.workshop.editor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.DirectoryView;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.Utils;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;

/**
 * @author jhkang
 * 装备预览工具
 */
public class EquipmentPreviewer extends EditorPart implements IFileModificationListener{
	/**
	 * 混杂的TreeProvider,给动画树和装备树用
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
			}else if(arg0 instanceof List){//hooks
				return ((List)arg0).toArray();
			}else if(arg0 instanceof PipAni4AniFramePiece){
				int idx = 0;
				for(idx = 0; idx<bodyDef.hooks.size(); idx++){
					if(bodyDef.hooks.get(idx).getImageID() == ((PipAni4AniFramePiece)arg0).getImageID())
						break;
				}
				List eqpsList = equipAndCtsNamePare4hook[idx];
				return eqpsList.toArray();
			}else if(arg0 instanceof String){
				String txt = (String)arg0;
				return leveledCts4eqp.get(txt).toArray();
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
			}else if(arg0 instanceof PipAni4AniFramePiece){
				int idx = 0;
				for(idx = 0; idx<bodyDef.hooks.size(); idx++){
					if(bodyDef.hooks.get(idx).getImageID() == ((PipAni4AniFramePiece)arg0).getImageID())
						break;
				}
				List eqpsList = equips4hook[idx];
				if(eqpsList == null || eqpsList.size()==0){
					return false;
				}else{
					return true;
				}
			}else if(arg0 instanceof String){
//				String txt = (String)arg0;
//				return txt.indexOf(".eqp")>0 && txt.indexOf(".cts")>0;
//				return txt.indexOf("(equ.cts)")>0;
			}
			return false;
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

		}

	}
	
	/**
	 * 混杂的LabelProvider,给动画树和装备树用
	 */
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
//				int idx = bodyAniSets.indexOf(obj);
//				return eqp2hook.getHookFileName(idx);
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
			}else if(obj instanceof PipAni4AniFramePiece){
				return ((PipAni4AniFramePiece)obj).name;
			}else if(obj instanceof String){
				return (String) obj;
			}
			return obj.toString();
		}
	}

	private AnimateViewer bodyAnimateViewer;
	private TreeViewer bodyAniTree;
	private PipAnimateSet bodyAniSet;
	private PipAnimateFrame curBodyFrame;
	private AnimateContentTreeProvider animateContentTreeProvider;
	private Thread hookAniDriver;
	/**
	 * 当前预览的素体使用的形象动画
	 */
	private Label bodyCtsName;
	/**
	 * 素体定义(hk)
	 */
	private BodyDef bodyDef;
	private CheckboxTreeViewer hookAndEquipTree;
	/**
	 * hk文件名
	 */
	private String bodyHkName;
	/**
	 * 挂接点和装配文件的映射;List的元素是装配文件的名称字符串;下标是挂接点的序号
	 */
	private ArrayList<String>[] equips4hook;
	/**
	 * List的元素是 装配文件的名称(.eqp):装备动画名称(.cts)<br/>
	 * 下标是挂接点的序号
	 */
	private ArrayList<String>[] equipAndCtsNamePare4hook;
	/**
	 * 某件装备的各个级别的动画的名称<br/>
	 * key:装配文件的名称(.eqp):装备动画名称(.cts)<br/>
	 * value:List of String(cts file name)
	 */
	private HashMap<String, List<String>> leveledCts4eqp;
	/**
	 * 保存某个挂接点下,有哪些eqp文件
	 */
	private ArrayList<EquipHookMap>[] equipHookMaps;
	private TileLibSelector eqpAniView;
	/**
	 * body hk file
	 */
	private String bodyFilePath;
	private String bodyFileDir;
	private int selAnimateIndex = -1;
	private int selFrameRefIndex = -1;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

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
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.setSite(site);
		super.setInput(input);
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		bodyFilePath = Utils.urlToPath(url).replace(".pre", ".hk");
		bodyFileDir = new File(bodyFilePath).getParent()+File.separator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
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
	public void createPartControl(Composite arg0) {
		SashForm sashForm = new SashForm(arg0, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createLeftPart(sashForm);
		createRightPart(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });
		postCreatePartControl();
		//==
		if(bodyAniTree.getTree().getItemCount()>0){
			ISelection selection = new StructuredSelection(bodyAniTree.getTree().getItem(0).getData());
			bodyAniTree.setSelection(selection);
		}
	}

	private void postCreatePartControl() {
		try{
//			String dirPath = "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\";
//			bodyHkName = "male4.hk";
			File bodyFile = new File(bodyFilePath);
			FileWatcher.watch(bodyFile, this);
			String dirPath = bodyFile.getParentFile().getAbsolutePath()+File.separator;
			bodyHkName = bodyFile.getName();
			bodyDef = new BodyDef();
			bodyDef.loadHooks(dirPath + bodyHkName);
			equips4hook = new ArrayList[bodyDef.hooks.size()]; 
			leveledCts4eqp = new HashMap<String, List<String>>();
			equipAndCtsNamePare4hook = new ArrayList[bodyDef.hooks.size()];
			equipHookMaps = new ArrayList[equips4hook.length];
			for(int i=0; i<equips4hook.length; i++){
				equips4hook[i] = new ArrayList<String>();
				equipAndCtsNamePare4hook[i] = new ArrayList<String>();
				equipHookMaps[i] = new ArrayList<EquipHookMap>();
			}
			bodyCtsName.setText(bodyDef.ctsFile);
			bodyCtsName.setSize(bodyCtsName.computeSize(-1, -1));
			bodyAniSet = new PipAnimateSet();
			File pasFile = null;
			pasFile = new File(dirPath + bodyDef.ctsFile);
			FileWatcher.watch(pasFile, this);
			bodyAniSet.load(pasFile);
			bodyDef.embedHookPieces(bodyAniSet);
			bodyAniTree.setInput(bodyAniSet);
			hookAndEquipTree.setInput(bodyDef.hooks);
			for(TreeItem item:hookAndEquipTree.getTree().getItems()){
				item.setChecked(true);
			}
			this.setPartName("预览"+bodyHkName);
		} catch (Throwable e) {
			MessageDialog.openError(getSite().getShell(), "Error", "Error load:"+e);
			e.printStackTrace();
		}
	}
	
	private void adjustEqp(String equipFilePath2) {
		DirectoryView view = (DirectoryView) getSite().getPage().findView(DirectoryView.ID);
		view.adjustEqp(equipFilePath2, true);
	}


	private void addEquip(String dirPath, String eqpFileName) throws Exception{
		if(!dirPath.endsWith(File.separator)){
			dirPath += File.separator;
		}
		EquipHookMap eqp2hook = new EquipHookMap();
		eqp2hook.load(dirPath + eqpFileName);
		FileWatcher.watch(new File(dirPath + eqpFileName), this);
		if(eqp2hook.hasBody(bodyHkName)==false){
			FileWatcher.unwatch(new File(dirPath + eqpFileName), this);

			adjustEqp(dirPath + eqpFileName);
			
			eqp2hook = new EquipHookMap();
			eqp2hook.load(dirPath + eqpFileName);
			FileWatcher.watch(new File(dirPath + eqpFileName), this);
			if(eqp2hook.hasBody(bodyHkName)==false){
				MessageDialog.openError(getSite().getShell(), "Error", "没有在装配文件"+eqpFileName+"中找到有关素体"+bodyHkName+"的装配信息.");
				return;
			}
		}
		
		int idx = eqp2hook.getBodyIndex(bodyHkName);
		int bindHookId = eqp2hook.getHookId(idx);
		int i = 0;
		for(PipAni4AniFramePiece hook:bodyDef.hooks){
			if(hook.getImageID() == bindHookId){
				if(equips4hook[i].indexOf((dirPath + eqpFileName).replace(bodyFileDir, ""))>=0){
					MessageDialog.openInformation(getSite().getShell(), "Info", "装备"+eqpFileName+"已经添加过了.");
					break;//防止添加同一装备
				}
				equips4hook[i].add((dirPath + eqpFileName).replace(bodyFileDir, ""));
				String eqpKey = (dirPath).replace(bodyFileDir, "") + eqpFileName+"("+eqp2hook.getEquipCtsName()+")";
				equipAndCtsNamePare4hook[i].add(eqpKey);
				List<String> leveledCtsNames = searchLeveledCts(dirPath, eqp2hook.getEquipCtsName());
				if(leveledCtsNames.size()==0){
					List<String> tmp = leveledCtsNames;
					leveledCtsNames = new ArrayList<String>();
					leveledCtsNames.addAll(tmp);
					tmp.clear();
					leveledCtsNames.add(eqp2hook.getEquipCtsName());
				}
				for(String ctsName:leveledCtsNames){
					FileWatcher.watch(new File(dirPath + ctsName), this);
				}
				leveledCts4eqp.put(eqpKey, leveledCtsNames);
				equipHookMaps[i].add(eqp2hook);
				hookAndEquipTree.expandToLevel(hook, 1);
				hookAndEquipTree.refresh();
				///////equipping default
				if(equips4hook[i].size() == 1){
					String ctsName = null;
					if(leveledCtsNames.size()>0){
						ctsName = leveledCtsNames.get(0);
					}else{
						ctsName = eqpKey;
					}
					hookAndEquipTree.expandToLevel(eqpKey, 1);
					switchEquip2(ctsName, true);
				}
				break;
			}
			i++;
		}
		
	}

	private List<String> searchLeveledCts(String selDir, String equipCtsName) {
//		List<String> ret = new ArrayList<String>();
		String pattern = equipCtsName;
		pattern = pattern.replace(".cts", "");
		pattern = pattern.substring(0, pattern.lastIndexOf("_")+1);
		pattern += "\\d+\\\\.cts$";
		final String regx = pattern;
		
		File dir = new File(selDir.substring(0, selDir.lastIndexOf("\\")));//new File(bodyFilePath).getParentFile();
		String[] names = dir.list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				if(dir.getName().matches("\\d+") && name.endsWith(".cts")) {
					return true;
				} else {
					return false;
				}
//				return name.matches(regx);
			}
		});
		
		for(int i=0; i<names.length; i++) {
			names[i] = dir.getAbsolutePath() + "\\" + names[i];
			
			if(names[i].contains("\\xz\\")) {
				names[i] = names[i].substring(names[i].indexOf("\\xz\\"));
			} else if(names[i].contains("\\zd\\")) {
				names[i] = names[i].substring(names[i].indexOf("\\zd\\"));
			}
			
		}
		
//		ret = Arrays.asList(names);
//		return ret;
		return Arrays.asList(names);
	}

	private void createRightPart(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRightTop(sashForm);
		createRightBottom(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });		
	}

	private void createRightBottom(Composite parent) {
		Composite composite = new Composite(parent, SWT.None);
		GridLayout gridLayout = new GridLayout(	);
		gridLayout.marginBottom = 0;
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("挂接点:");
		
		Button addEquipBtn = new Button(composite, SWT.PUSH);
		addEquipBtn.setText("增加装备");
		addEquipBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				addEquipSource();
			}
		});
		
		bodyCtsName = new Label(composite, SWT.NONE);
		bodyCtsName.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		bodyCtsName.setText("");
		
		
		hookAndEquipTree = new CheckboxTreeViewer(composite);
		hookAndEquipTree.setContentProvider(new AnimateContentTreeProvider());
		hookAndEquipTree.setLabelProvider(new AnimateLabelProvider());
		hookAndEquipTree.addCheckStateListener(new ICheckStateListener(){
			public void checkStateChanged(CheckStateChangedEvent arg0) {
				hookOrEquipCheckChanged(arg0.getElement(), arg0.getChecked());
			}
		});
		hookAndEquipTree.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				equipSelectionChanged();
			}
		});
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalSpan = gridLayout.numColumns;
		hookAndEquipTree.getTree().setLayoutData(gridData);
	}

	/**
	 * 选择了装备cts,注意不是响应勾选动作
	 */
	protected void equipSelectionChanged() {
		IStructuredSelection sel = (IStructuredSelection)hookAndEquipTree.getSelection();
		if (sel.isEmpty()) {
			return ;
		}
		Object selObj = sel.getFirstElement();
		if(selObj instanceof String){
			if(leveledCts4eqp.containsKey(selObj)){
				List<String> list = leveledCts4eqp.get(selObj);
				if(list.size()>0){
					PipAnimateSet pas = findEquipPas((String)selObj);
					eqpAniView.setInput(pas, 1, 1);
				}else{
					eqpAniView.setInput(null, 1, 1);
				}
				eqpAniView.redraw();
			}
		}
	}

	protected void addEquipSource() {
		DirectoryDialog dlg = new DirectoryDialog(getSite().getShell(), SWT.MULTI | SWT.OPEN);
//		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.MULTI | SWT.OPEN);
//		dlg.setFilterExtensions(new String[]{"*.eqp"});
//		dlg.setFilterNames(new String[]{"装配文件(*.eqp)"});
//		dlg.setFilterPath("E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\");
		dlg.setFilterPath(new File(bodyFilePath).getParentFile().getAbsolutePath());
		if(dlg.open()==null){
			return;
		}
		String dir = dlg.getFilterPath();
		File dirFile = new File(dir);
		List<File> files = findEqpFiles(dirFile);
        for (File eqpFile : files) {
			try {
				addEquip(eqpFile.getParentFile().getAbsolutePath(), "equ.eqp");
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "Error", "Error addEquipSource:"+e);
				e.printStackTrace();
			}
        }
        
//		String dir = dlg.getFilterPath();
//		String[] names = dlg.getFileNames();
//		for(String eqpFileName:names){
//			try {
//				addEquip(dir, eqpFileName);
//			} catch (Exception e) {
//				MessageDialog.openError(getSite().getShell(), "Error", "Error addEquipSource:"+e);
//				e.printStackTrace();
//			}
//		}
	}
	
	private List<File> findEqpFiles(File dir) {
		if(dir.isDirectory() == false) {
			return null;
		}
		List<File> files = new ArrayList<File>();
		
		String[] filesName = dir.list();
		for(String fileName : filesName) {
			if(new File(dir, fileName).isDirectory()) {
				files.addAll(findEqpFiles(new File(dir, fileName)));				
			} else {
				if(fileName.endsWith(".eqp")) {
					files.add(new File(dir, fileName));
				}
			}
		}
		
		return files;
	}

	private void createRightTop(Composite parent) {
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

	protected void bodyAniTreeSelChange() {
		IStructuredSelection sel = (IStructuredSelection)bodyAniTree.getSelection();
		if (sel.isEmpty()) {
			return ;
		}
		Object selObj = sel.getFirstElement();
		if(selObj instanceof PipAnimateSet){
//			curBodyFrame = null;
			bodyAniSet = (PipAnimateSet) selObj;
			changeSelBody();
			bodyAnimateViewer.stop();
			bodyAnimateViewer.setInput(null);
			bodyAnimateViewer.redraw();
		}else if(selObj instanceof PipAnimate){
			PipAnimate selAnimate = (PipAnimate)selObj; 
			bodyAniSet = selAnimate.getParent();
			selAnimateIndex = bodyAniSet.getAnimateIndex(selAnimate);
			selFrameRefIndex = -1;
			changeSelBody();
			bodyAnimateViewer.setInput(selAnimate);
			bodyAnimateViewer.setCurrentFrame(0);
			bodyAnimateViewer.play();
//			curBodyFrame = null;
//			bodyAnimateViewer.redraw();
		}else if(selObj instanceof PipAnimateFrameRef){
			PipAnimateFrameRef selFrameRef = (PipAnimateFrameRef)selObj;
			PipAnimate selAnimate = selFrameRef.getParent();
			bodyAniSet = selAnimate.getParent();
			selAnimateIndex = bodyAniSet.getAnimateIndex(selAnimate);
			selFrameRefIndex = selAnimate.getFrameIndex(selFrameRef);
			changeSelBody();
			curBodyFrame = ((PipAnimateFrameRef)selObj).realize();
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
	private void runHookAniDriver() {
		if(hookAniDriver == null){
			hookAniDriver = new Thread(new Runnable(){
				public void run() {
					while(true){
						if(bodyAniSet == null){
							break;
						}
						if(curBodyFrame != null){
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
	private void changeSelBody() {
		// TODO Auto-generated method stub
		
	}

	private void createLeftPart(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createLeftTop(sashForm);
		createLeftBottom(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });		
	}

	private void createLeftBottom(Composite parent) {
		eqpAniView = new TileLibSelector(parent, SWT.None);
	}
	/**
	 * 刷新素体(hk),eqp,素体动画(cts),装备动画(cts)
	 */
	protected void refreshEquip(File f){
		String changedCtsName = f.getName();
		if(changedCtsName.endsWith(".hk")){//hk改变
			refreshHk();
			refreshCheckedEquip(null, true);
		}else if(changedCtsName.equals(bodyDef.ctsFile)){//形象动画cts改变
			refreshHk();
			refreshCheckedEquip(null, true);
		}else if(changedCtsName.endsWith(".eqp")){
			EquipHookMap eqp2hook = new EquipHookMap();
			try {
				eqp2hook.load(f.getAbsolutePath());
				String eqpKey = f.getName()+"("+eqp2hook.getEquipCtsName()+")";
				int hookIdx = 0;
				int equipIdx = 0;
				for(ArrayList<String> list:equipAndCtsNamePare4hook){
					equipIdx = 0;
					boolean hit = false;
					for(String item:list){
						if(item.equals(eqpKey)){
							hit = true;
							break;
						}
						equipIdx++;
					}
					if(hit){
						break;
					}
					hookIdx++;
				}
				equipHookMaps[hookIdx].add(equipIdx, eqp2hook);
				equipHookMaps[hookIdx].remove(equipIdx+1);
				PipAnimateSet equipPas = findEquipPas(eqp2hook.getEquipCtsName());
				eqp2hook.doEquip(bodyAniSet, equipPas, bodyHkName);
			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Error", "刷新eqp文件失败:\n"+e);
				e.printStackTrace();
			}
		}else{//装备动画改变
			refreshEquipAniViewer(f);
			refreshCheckedEquip(changedCtsName);
		}
	}
	/**
	 * 刷新已经装配上的装备动画
	 * @param changedCtsName
	 */
	private void refreshCheckedEquip(String changedCtsName) {
		refreshCheckedEquip(changedCtsName, false);
	}
	/**
	 * 刷新已经装配上的装备动画
	 * @param changedCtsName
	 * @param reEquip 是否重新装配,若是,则所以选中了的装配都会重新装配,否则只装配changedCtsName指定的.
	 */
	private void refreshCheckedEquip(String changedCtsName, boolean reEquip) {	
		Object[] checked = hookAndEquipTree.getCheckedElements();
		for(Object elem:checked){
			if(elem instanceof PipAni4AniFramePiece){
				continue;
			}
			if(leveledCts4eqp.containsKey(elem)){
				continue;
			}
			if(reEquip == false && changedCtsName.equals(elem)==false){
				continue;
			}
			hookOrEquipCheckChanged(elem, true);
		}		
	}

	/**
	 * 更新装备动画预览
	 * @param f
	 */
	private void refreshEquipAniViewer(File f) {
		String ctsName = f.getName();
		IStructuredSelection sel = (IStructuredSelection)hookAndEquipTree.getSelection();
		if (sel.isEmpty()) {
			return ;
		}
		Object selObj = sel.getFirstElement();
		if(ctsName.equals(selObj)){
			//update equipView
			equipSelectionChanged();
		}		
	}

	/**
	 * hk文件变化,重新加载;由于形象动画已经嵌入了挂接点,不能再次使用,所以形象动画也会重新加载
	 */
	private void refreshHk(){
		//body,重新载入形象动画,重新嵌入挂接点
		File bodyFile = new File(bodyFilePath);
		String dirPath = bodyFile.getParentFile().getAbsolutePath()+File.separator;
		PipAnimateSet pas = new PipAnimateSet();
		try {
			pas.load(new File(dirPath + bodyDef.ctsFile));
			bodyDef.loadHooks(bodyFilePath);
			bodyAniSet = pas;
			bodyAniTree.setInput(bodyAniSet);
			bodyDef.embedHookPieces(bodyAniSet);
			resetBodyAni();
		} catch (IOException e1) {
			MessageDialog.openError(getSite().getShell(), "Error", "刷新素体文件(hk)失败:\n"+e1);
			e1.printStackTrace();
			return;
		}		
	}

	private void resetBodyAni() {
		if(selAnimateIndex >= 0){
			PipAnimate selPa = bodyAniSet.getAnimate(selAnimateIndex);
			bodyAnimateViewer.setInput(selPa);
			ISelection selection = null;
			if(selFrameRefIndex>=0){
				PipAnimateFrameRef frameRef = selPa.getFrame(selFrameRefIndex);
				bodyAnimateViewer.setCurrentFrame(selFrameRefIndex);
				selection = new StructuredSelection(frameRef);
			}else{
				selection = new StructuredSelection(selPa);
			}
			bodyAniTree.setSelection(selection);
		}
		bodyAnimateViewer.redraw();		
	}

	protected void hookOrEquipCheckChanged(Object element, boolean checked) {
		if(element instanceof PipAni4AniFramePiece){
			PipAni4AniFramePiece hook = (PipAni4AniFramePiece) element;
			int cnt = bodyAniSet.getFrameCount();
			for(int i=0; i<cnt; i++){
				PipAni4AniFramePiece hookPiece = bodyAniSet.getFrame(i).getHook(hook.getImageID());
				if(hookPiece != null){
					hookPiece.setVisible(checked);
				}
			}
			bodyAnimateViewer.redraw();
		}else if(element instanceof String){
			switchEquip2((String) element, checked);
		}
	}
	/**
	 * use testFindItem
	 * @param element
	 * @param checked
	 */
	private void switchEquip2(String element, boolean checked) {
		//single check
		TreeItem treeItem = (TreeItem) hookAndEquipTree.testFindItem(element);
		if(treeItem==null){
			return;
		}
		for(TreeItem ti:treeItem.getParentItem().getItems()){
			ti.setChecked(false);
		}
		treeItem.setChecked(checked);
		//switch equip cts
		PipAnimateSet equipPas = null;
		TreeItem eqpItem;
		int hookIdx = 0;
		int eqpIdx = 0;
//		if(checked){
			boolean hadSelCts = false;
			if(leveledCts4eqp.containsKey(element)){
				//eqp selected
				eqpItem = treeItem;
//				List<String> list = leveledCts4eqp.get(element);
//				if(list.size()>0){
//					element = list.get(0);
					hadSelCts = true;
//				}
			}else{
				//leveled cts selected
				eqpItem = treeItem.getParentItem();
			}
			//find which equip
			for(TreeItem ti:eqpItem.getParentItem().getItems()){
				if(ti == eqpItem){
					break;
				}
				eqpIdx++;
			}
			//find which hook
			TreeItem hookItem = eqpItem.getParentItem(); 
			for(TreeItem ti:hookAndEquipTree.getTree().getItems()){
				if(ti == hookItem){
					break;
				}
				hookIdx++;
			}
//			System.out.println(element);
			if(hadSelCts && checked){
				equipPas = findEquipPas(element);
			}
//		}
//		//eqp节点选中时,选择等级节点才产生影响
//		if(eqpIdx<equipHookMaps[hookIdx].size()){
			equipHookMaps[hookIdx].get(eqpIdx).doEquip(bodyAniSet, equipPas, bodyHkName);
			bodyAnimateViewer.redraw();
//		}
	}
	private PipAnimateSet findEquipPas(String element) {
		PipAnimateSet equipPas = new PipAnimateSet();
		element = element.replace("equ.eqp(equ.cts)", "equ.cts");
		File ctsFile = new File(new File(this.bodyFilePath).getParentFile(), element);
		try {
			equipPas.load(ctsFile);
		} catch (IOException e) {
			MessageDialog.openError(getSite().getShell(), "Error", "装载装备动画出错:\n"+e);
			e.printStackTrace();
			return null;
		}
		return equipPas;
	}

	private void createLeftTop(Composite parent) {
		bodyAnimateViewer = new AnimateViewer(parent, SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		FileWatcher.unwatch(this);
		bodyAniSet = null;
		super.dispose();
	}

	public void fileModified(final File f) {
		this.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
            	refreshEquip(f);
            }
        });
	}

}
