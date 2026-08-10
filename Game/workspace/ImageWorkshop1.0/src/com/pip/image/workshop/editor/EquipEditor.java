/**
 * 
 */
package com.pip.image.workshop.editor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.DirectoryView;
import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.EquipAnimateViewer.EquipAnimateSaver;
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
 * 
 */
public class EquipEditor extends EditorPart implements
		IFileModificationListener, ImageViewerListener {
	public static final String ID = "com.pip.image.workshop.editor.EquipEditor";
	
	public class SelectHookOnBodyDialog extends Dialog {

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
		 * 
		 * @param parent
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, "确定", true);
			createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
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
			if (arg0 instanceof List) {
				return ((List) arg0).toArray();
			} else if (arg0 instanceof PipAnimateSet) {
				PipAnimateSet animateSet = (PipAnimateSet) arg0;
				int cnt = animateSet.getAnimateCount();
				Object[] ret = new Object[cnt];
				for (int i = 0; i < cnt; i++) {
					ret[i] = (animateSet.getAnimate(i));
				}
				return ret;
			} else if (arg0 instanceof PipAnimate) {
				PipAnimate animate = (PipAnimate) arg0;
				int cnt = animate.getFrameCount();
				PipAnimateFrameRef[] frameRefs = new PipAnimateFrameRef[cnt];
				for (int i = 0; i < cnt; i++) {
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
			if (arg0 instanceof PipAnimate) {
				return ((PipAnimate) arg0).getParent();
			} else if (arg0 instanceof PipAnimateFrameRef) {
				return ((PipAnimateFrameRef) arg0).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object arg0) {
			if (arg0 instanceof List) {
				return ((List) arg0).size() > 0;
			} else if (arg0 instanceof PipAnimateSet) {
				return ((PipAnimateSet) arg0).getAnimateCount() > 0;
			} else if (arg0 instanceof PipAnimate) {
				return ((PipAnimate) arg0).getFrameCount() > 0;
			}
			return false;
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

		}

	}

	class AnimateLabelProvider extends LabelProvider {
		public Image getImage(Object obj) {
			if (obj instanceof PipAnimateSet) {
				return WorkshopPlugin.getDefault().getImageRegistry().get("grid");
			} else if (obj instanceof PipAnimate) {
				boolean matched = false;
				PipAnimate ani = (PipAnimate)obj;
				for (int i = 0; i < ani.getFrameCount(); i++) {
					if (checkUsage(ani.getFrame(i))) {
						matched = true;
						break;
					}
				}
				if (matched) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("used");
				} else {
					return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
				}
			} else if (obj instanceof PipAnimateFrameRef) {
				if (checkUsage((PipAnimateFrameRef)obj)) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("used");
				} else {
					return WorkshopPlugin.getDefault().getImageRegistry().get("image");
				}
			}
			return null;
		}
		
		// 检查一个动画帧里是否绑定了当前选中的装备动画序列
		private boolean checkUsage(PipAnimateFrameRef ref) {
			PipAnimateFrame f = ref.realize();
			PipAni4AniFramePiece hook = f.getHook(selHookId);
			if (hook == null) {
				return false;
			}
			if (hook.getBindAnimate() == null) {
				return false;
			}
			if (filterAnimateID < 0 || filterAnimateID >= equipAniSet.getAnimateCount()) {
				return false;
			}
			return hook.getBindAnimate() == equipAniSet.getAnimate(filterAnimateID);
		}

		public String getText(Object obj) {
			if (obj instanceof PipAnimateSet) {
				int idx = bodyAniSets.indexOf(obj);
				return eqp2hook.getHookFileName(idx);
			} else if (obj instanceof PipAnimate) {
				int aniIdxInAniSet = 0;
				// 取此动画在动画组中的下标
				PipAnimate pa = (PipAnimate) obj;
				PipAnimateSet pas = pa.getParent();
				int cnt = pas.getAnimateCount();
				for (int i = 0; i < cnt; i++) {
					if (pas.getAnimate(i) == obj) {
						aniIdxInAniSet = i;
					}
				}
				// 返回下标和动画名称
				return aniIdxInAniSet + ":" + ((PipAnimate) obj).getName();
			} else if (obj instanceof PipAnimateFrameRef) {
				int frameIdxInAniSet = ((PipAnimateFrameRef) obj).getFrame();
				return frameIdxInAniSet + ":"
						+ ((PipAnimateFrameRef) obj).realize().getName();
			}
			return obj.toString();
		}
	}

	private EquipAnimateViewer bodyAnimateViewer;
	private TileLibSelector allBodyViewer;
	private TreeViewer bodyAniTree;
	private AnimateContentTreeProvider animateContentTreeProvider;

	private Display display;
	private PipAnimateSet bodyAniSet;
	private List<PipAnimateSet> bodyAniSets = new ArrayList<PipAnimateSet>();
	/**
	 * 当前选择的body的帧
	 */
	private PipAnimateFrame curBodyFrame;
	private File equipAniFile;
	private PipAnimateSet equipAniSet;
	private boolean equipSaving = false;
	private int selHookId = -1;
	private String selHookName;
	private Label curHookLabel;
	private List<BodyDef> bodyDefs = new ArrayList<BodyDef>();
	private EquipAnimateSelector eqpAniView;
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
	private Composite leftTopComp;
	
	private int filterAnimateID = -1;

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
		if (selEquipIdx < 0) {
			return;
		}
		if (bodyAniSets.size() == 0) {
			MessageDialog.openInformation(getSite().getShell(), "Info",
					"请先添加素体.");
			return;
		}
		if (bodyAniSet == null) {
			MessageDialog.openInformation(getSite().getShell(), "Info",
					"请先选择素体.");
			return;
		}
		IStructuredSelection sel = (IStructuredSelection) bodyAniTree
				.getSelection();
		if (sel.isEmpty()) {
			MessageDialog.openInformation(getSite().getShell(), "Info",
					"请先选择形象动画.");
			return;
		}
		Object selObj = sel.getFirstElement();
		if (selObj instanceof PipAnimate) {
			PipAnimate equipPa = equipAniSet.getAnimate(selEquipIdx);
			PipAnimate bodyPa = (PipAnimate) selObj;
			int cnt = bodyPa.getFrameCount();
			for (int i = 0; i < cnt; i++) {
				PipAnimateFrame frame = bodyPa.getFrame(i).realize();
				frame.getHook(selHookId).bindAnimate(equipPa);
			}
			allBodyViewer.clearCache();
			bodyAnimateViewer.redraw();
			setDirty(true);
			runHookAniDriver();
		} else {
			MessageDialog.openInformation(getSite().getShell(), "Info",
					"请先选择形象动画.(注意不是选择形象帧)");
			return;
		}
	}
	
	protected void showControl(Control target, boolean visible) {
		Object obj = target.getLayoutData();
		if (obj != null && obj instanceof GridData) {
			((GridData)obj).exclude = !visible;
		}
		target.setVisible(visible);
	}

	protected void bodyAniTreeSelChange() {
		IStructuredSelection sel = (IStructuredSelection) bodyAniTree
				.getSelection();
		if (sel.isEmpty()) {
			return;
		}
		Object selObj = sel.getFirstElement();
		if (selObj instanceof PipAnimateSet) {
			curBodyFrame = null;
			bodyAniSet = (PipAnimateSet) selObj;
			changeSelBody();
			bodyAnimateViewer.stop();
			bodyAnimateViewer.setInput(null);
			bodyAnimateViewer.redraw();
			showControl(bodyAnimateViewer, false);
			allBodyViewer.setInput(bodyAniSet, 1, 1);
			showControl(allBodyViewer, true);
			leftTopComp.layout();
		} else if (selObj instanceof PipAnimate) {
			bodyAniSet = ((PipAnimate) selObj).getParent();
			changeSelBody();
			bodyAnimateViewer.setInput((PipAnimate) selObj);
			bodyAnimateViewer.setCurrentFrame(0);
			bodyAnimateViewer.play();
			curBodyFrame = null;
			showControl(bodyAnimateViewer, true);
			showControl(allBodyViewer, false);
			leftTopComp.layout();
			// bodyAnimateViewer.redraw();
		} else if (selObj instanceof PipAnimateFrameRef) {
			bodyAniSet = ((PipAnimateFrameRef) selObj).getParent().getParent();
			changeSelBody();
			curBodyFrameIdxInAniSet = ((PipAnimateFrameRef) selObj).getFrame();
			curBodyFrame = bodyAniSet.getFrame(curBodyFrameIdxInAniSet);
			bodyAnimateViewer.stop();
			PipAnimate pa = (PipAnimate) animateContentTreeProvider
					.getParent(selObj);
			bodyAnimateViewer.setInput(pa);
			int cnt = pa.getFrameCount();
			int frameIdxInAni = 0;
			for (int i = 0; i < cnt; i++) {
				if (pa.getFrame(i) == selObj) {
					frameIdxInAni = i;
					break;
				}
			}
			bodyAnimateViewer.setCurrentFrame(frameIdxInAni);
			bodyAnimateViewer.redraw();
			runHookAniDriver();
			showControl(bodyAnimateViewer, true);
			showControl(allBodyViewer, false);
			leftTopComp.layout();
			
			// 如果有绑定的话，修改下面装备动画的选中
			PipAnimateFrame selF = ((PipAnimateFrameRef) selObj).realize();
			PipAni4AniFramePiece hook = selF.getHook(selHookId);
			if (hook == null) {
				return;
			}
			if (hook.getBindAnimate() == null) {
				return;
			}
			for (int i = 0; i < equipAniSet.getAnimateCount(); i++) {
				if (equipAniSet.getAnimate(i) == hook.getBindAnimate()) {
					eqpAniView.setSelectedFrame(i);
					this.frameSelectionChanged(eqpAniView, i);
					break;
				}
			}
		}
	}

	private void createLeftBottom(Composite parent) {
		eqpAniView = new EquipAnimateSelector(parent, SWT.None, this);
		eqpAniView.setImageViewerListener(this);
	}

	private void createLeftPart(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createLeftTop(sashForm);
		createLeftBottom(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });
	}

	private void createLeftTop(Composite parent) {
		leftTopComp = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		leftTopComp.setLayout(gl);
		
		// 平时的编辑器
		bodyAnimateViewer = new EquipAnimateViewer(leftTopComp, SWT.NONE);
		bodyAnimateViewer.setEquipAniSaver(new EquipAnimateSaver() {
			public boolean saveEquipAnimate(PipAnimate equipAni) {
				// saveEquipAnimateSet();
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
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		bodyAnimateViewer.setLayoutData(gd);
		
		// 预览器
		allBodyViewer = new TileLibSelector(leftTopComp, SWT.NONE);
		allBodyViewer.setVisible(false);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.exclude = true;
		allBodyViewer.setLayoutData(gd);
	}

	protected void saveEquipAnimateSet() {
		equipSaving = true;
		try {
			File dir = new File(equipFilePath).getParentFile();
			File cts = new File(dir, equipCtsName);
			equipAniSet.save(cts, true);
			File ctn = new File(dir, equipCtsName.replaceAll(".cts$", ".ctn"));
			equipAniSet.save(ctn, false);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "Error", "保存装备动画出错\n"
					+ e);
			e.printStackTrace();
			equipSaving = false;
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
		display = this.getSite().getShell().getDisplay();

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createLeftPart(sashForm);
		createRighPart(sashForm);
		sashForm.setWeights(new int[] { 2, 1 });
		postCreatePartControl();
	}

	private void postCreatePartControl() {
		this.setPartName(this.getEditorInput().getName());
		// String dirPath = "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\";
		File equipFile = new File(equipFilePath);
		File dir = equipFile.getParentFile();
		String dirPath = dir.getAbsolutePath() + File.separator;
		try {
			String hookFileName;
			PipAnimateSet pas;
			eqp2hook.load(equipFilePath);

			equipCtsName = eqp2hook.getEquipCtsName();
			File equipCtsFile = new File(dir, equipCtsName);
			if (equipCtsFile.exists() == false) {
				adjustEqp(equipFilePath);
				eqp2hook = new EquipHookMap();
				eqp2hook.load(equipFilePath);
				equipCtsName = eqp2hook.getEquipCtsName();
			}
			equipCtsNameLabel.setText("当前装备文件:" + equipCtsName);
			equipCtsNameLabel.setSize(equipCtsNameLabel.computeSize(-1, -1));
			pas = new PipAnimateSet();
			equipAniFile = new File(dir, equipCtsName);
			pas.load(equipAniFile);
			equipAniSet = pas;
			FileWatcher.watch(equipAniFile, this);
			eqpAniView.setInput(pas, 1, 1);
			eqpAniView.redraw();

			int bodyCnt = eqp2hook.getBodyCnt();
			for (int i = 0; i < bodyCnt; i++) {
				hookFileName = eqp2hook.getHookFileName(i);
				BodyDef bodyDef = new BodyDef();
				pas = loadBody(dirPath, hookFileName, bodyDef);
				bodyAniSets.add(pas);
				bodyDefs.add(bodyDef);
				int hookId = eqp2hook.getHookId(i);
				bodyDef.embedHookPieces(pas, hookId, true);
				eqp2hook.doEquip(pas, equipAniSet, i);
			}
			bodyAniTree.setInput(bodyAniSets);
			updateLevelTable();
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "Error", "初始化错误:\n"
					+ e);
			e.printStackTrace();
		}
	}

	private void adjustEqp(String equipFilePath2) {
		DirectoryView view = (DirectoryView) getSite().getPage().findView(
				DirectoryView.ID);
		view.adjustEqp(equipFilePath2, false);
	}

	/**
	 * 扫描此装备的其他级别
	 */
	private void updateLevelTable() {
		String pattern = this.equipCtsName;
		pattern = pattern.replace(".cts", "");
		pattern = pattern.substring(0, pattern.lastIndexOf("_") + 1);
		pattern += "\\d+\\.cts$";
		final String regx = pattern;

		File dir = new File(this.equipFilePath).getParentFile();
		String[] names = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(regx);
			}
		});
		levelTableView.setInput(names);
	}

	public static void main(String[] args) {
		String pattern = "abc_1.cts";
		pattern = pattern.replace(".cts", "");
		pattern = pattern.substring(0, pattern.lastIndexOf("_") + 1);
		pattern += "\\d+\\.cts$";

		System.out.println("abc_2.cts".matches(pattern));
		System.out.println("abc_20.cts".matches(pattern));
		System.out.println("abc_.cts".matches(pattern));
		System.out.println("abc_z3.cts".matches(pattern));
	}

	/**
	 * 加载素体(.hk)文件,返回此素体关联的形象动画组
	 * 
	 * @param dir
	 * @param hookFilePath
	 * @param bodyDef
	 * @return
	 * @throws IOException
	 */
	private PipAnimateSet loadBody(String dir, String hookFilePath,
			BodyDef bodyDef) throws IOException {
		if (!dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		bodyDef.loadHooks(dir + hookFilePath);
		String bodyCtsName;
		bodyCtsName = bodyDef.ctsFile;
		PipAnimateSet pas = new PipAnimateSet();
		pas.load(new File(new File(dir + hookFilePath).getParentFile(),
				bodyCtsName));
		return pas;
	}

	private void createRighPart(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRightTop(sashForm);
		createRightBottom(sashForm);
		sashForm.setWeights(new int[] { 1, 1 });
	}

	private void createRightBottom(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRightBottomOperation(sashForm);
		createRightBottomLevelList(sashForm);
		sashForm.setWeights(new int[] { 4, 3 });
	}

	private void createRightBottomLevelList(Composite parent) {
		parent = new Composite(parent, SWT.BORDER);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		parent.setLayout(gridLayout);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		label.setText("匹配到的级别:");

		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		button.setText("刷新");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLevelTable();
			}
		});
		// /level table
		levelTableView = CheckboxTableViewer.newCheckList(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		levelTableView.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
				// TODO Auto-generated method stub

			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
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

	private void createRightBottomOperation(Composite parent) {
		Composite container = new Composite(parent, SWT.None);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);

		equipCtsNameLabel = new Label(container, SWT.NONE);
		equipCtsNameLabel.setText("当前装备文件:");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Button renameEquipCtsName = new Button(container, SWT.PUSH);
		renameEquipCtsName.setText("重命名装备文件名称");
		renameEquipCtsName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				renameEquipCtsName();
			}
		});

		Button addBodyBtn = new Button(container, SWT.PUSH);
		addBodyBtn.setLayoutData(new GridData());
		addBodyBtn.setText("增加素体");
		addBodyBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addBody();
			}
		});

		Button removeBodyBtn = new Button(container, SWT.PUSH);
		removeBodyBtn.setLayoutData(new GridData());
		removeBodyBtn.setText("移除素体");
		removeBodyBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeBody();
			}
		});

		curHookLabel = new Label(container, SWT.NONE);
		curHookLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		curHookLabel.setText("当前挂接点:");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Button btn = new Button(container, SWT.PUSH);
		btn.setText("装配到当前形象动画");
		btn.setToolTipText("装配到选中的动画里的各个帧上");
		btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bindToAllFrame();
			}
		});

		Button bindToSelBodyFrame = new Button(container, SWT.PUSH);
		bindToSelBodyFrame.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		bindToSelBodyFrame.setText("装配到当前形象帧");
		bindToSelBodyFrame.setToolTipText("将选择的装备动画装配到选中的形象帧上");
		bindToSelBodyFrame.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bindToBodyFrame(curBodyFrame);
			}
		});

		Button unloadBtn = new Button(container, SWT.PUSH);
		unloadBtn.setText("从当前形象帧卸载");
		unloadBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				unbindFromCurFrame();
			}
		});

		final Button buttonAutoEquip = new Button(container, SWT.NONE);
		buttonAutoEquip.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				autoEquip();
			}
		});
		final GridData gd_buttonAutoEquip = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		buttonAutoEquip.setLayoutData(gd_buttonAutoEquip);
		buttonAutoEquip.setText("按顺序自动装配");
		buttonAutoEquip.setToolTipText("从右上方选择一个动画序列，左下方选择一个装备动画，自动按顺序装配整个动画序列的所有帧");

		final Button button = new Button(container, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onOptimize();
			}
		});
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		button.setText("删除没有用到的装备动画");
	}

	protected void renameEquipCtsName() {
		File dir = new File(this.equipFilePath).getParentFile();
		File equipCtsFile = new File(dir, this.equipCtsName);
		IEditorPart editor = null;
		String equipCtsPath = new File(dir, this.equipCtsName)
				.getAbsolutePath();
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(
				new Path(equipCtsPath));
		FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
		editor = getSite().getWorkbenchWindow().getActivePage().findEditor(
				input);
		if (editor != null) {
			MessageDialog.openInformation(getSite().getShell(), "确认",
					"装备动画正在被编辑,请先关闭.");
			getSite().getWorkbenchWindow().getActivePage().activate(editor);
		} else {
			InputDialog inputDlg = new InputDialog(getSite().getShell(), "输入",
					"新文件名", equipCtsFile.getName(), new IInputValidator() {

						public String isValid(String newText) {
							if (newText == null || newText.equals("")) {
								return "文件名不能为空";
							}
							return null;
						}

					});
			int ret = inputDlg.open();
			if (ret != InputDialog.OK) {
				return;
			}
			String newName = inputDlg.getValue();
			if (!newName.endsWith(".cts")) {
				newName += ".cts";
			}
			boolean succ = equipCtsFile.renameTo(new File(dir, newName));
			if (!succ) {
				MessageDialog.openWarning(getSite().getShell(), "警告", "重命名失败");
			} else {
				eqp2hook.setEquipCtsName(newName);
				equipCtsName = newName;
				equipCtsNameLabel.setText("当前装备文件:" + equipCtsName);
				equipCtsNameLabel
						.setSize(equipCtsNameLabel.computeSize(-1, -1));
				setDirty(true);
				System.out.println("EquipEditor.renameEquipCtsName() renameOK");
				// DirectoryView view = (DirectoryView)
				// getSite().getWorkbenchWindow
				// ().getActivePage().findView(DirectoryView.ID);
				// view.
			}
		}
	}

	protected void removeBody() {
		if (bodyAniSet == null) {
			MessageDialog.openInformation(getSite().getShell(), "Error",
					"请先选择素体.");
			return;
		}
		int idx = bodyAniSets.indexOf(bodyAniSet);
		String selBodyName = eqp2hook.getHookFileName(idx);
		boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认",
				"确定移除素体:" + selBodyName + "?");
		if (ret) {
			curBodyFrame = null;
			eqp2hook.remove(idx);
			bodyDefs.remove(idx);
			bodyAniSets.remove(idx);
			bodyAniTree.refresh();
			bodyAniSet = null;
			bodyAnimateViewer.setInput(null);
			bodyAnimateViewer.redraw();
			allBodyViewer.setInput(null);
			allBodyViewer.redraw();
			changeSelBody();
			setDirty(true);
		}
	}

	protected void bindToBodyFrame(PipAnimateFrame bodyFrame) {
		if (selHookId < 0) {
			MessageDialog.openInformation(getSite().getShell(), "Error",
					"请先选择素体");
			return;
		}
		if (bodyFrame == null) {
			MessageDialog.openInformation(getSite().getShell(), "Error",
					"请选择要挂接到的帧.");
			return;
		}
		int selEquipIdx = checkEquipAniSel();
		if (selEquipIdx < 0) {
			return;
		}

		PipAni4AniFramePiece hook = bodyFrame.getHook(selHookId);
		if (hook == null) {
			String alertMsg = null;
			alertMsg = "没有找到挂接点 " + selHookId + ":" + selHookName + "\n" + "在帧"
					+ curBodyFrameIdxInAniSet + ":" + bodyFrame.getName();
			MessageDialog.openError(getSite().getShell(), "Error", alertMsg);
			return;
		}
		PipAnimate pa = equipAniSet.getAnimate(selEquipIdx);
		hook.bindAnimate(pa);
		setDirty(true);
		allBodyViewer.clearCache();
		bodyAnimateViewer.redraw();
		runHookAniDriver();
	}

	private int checkEquipAniSel() {
		int[] selEqpAniIds = this.eqpAniView.getSelectedFrames();
		if (selEqpAniIds.length == 0) {
			MessageDialog.openInformation(getSite().getShell(), "Error",
					"请选择一个装备动画.");
			return -1;
		} else if (selEqpAniIds.length > 1) {
			MessageDialog.openInformation(getSite().getShell(), "Error",
					"你选择了多个装备动画.只允许选择一个进行挂载.");
			return -1;
		}
		return selEqpAniIds[0];
	}

	private void runHookAniDriver() {
		if (hookAniDriver == null) {
			hookAniDriver = new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (equipAniSet == null) {
							break;
						}
						long preTime = System.currentTimeMillis();
						if (curBodyFrame != null
								&& curBodyFrame.getHook(selHookId).binded()) {
							Display dsp = getSite().getShell().getDisplay();
							dsp.asyncExec(new Runnable() {
								public void run() {
									try {
										if (bodyAnimateViewer.isDisposed()) {
											return;
										}
										bodyAnimateViewer.redraw();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
						try {
			            	long processTime = System.currentTimeMillis() - preTime;
			            	if (processTime < Settings.animateFrameDelay) {
			            		Thread.sleep(Settings.animateFrameDelay - processTime);
			            	}
						} catch (Exception e) {
						}
					}
				}
			});
			hookAniDriver.start();
		}
	}

	protected void unbindFromCurFrame() {
		if (curBodyFrame != null) {
			PipAni4AniFramePiece hook = curBodyFrame.getHook(selHookId);
			hook.unbind();
			setDirty(true);
			allBodyViewer.clearCache();
			bodyAnimateViewer.redraw();
		} else {
			MessageDialog
					.openInformation(getSite().getShell(), "info", "请先选择帧");
		}
	}

	protected void addBody() {
		FileDialog fdlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		fdlg.setFilterExtensions(new String[] { "*.hk" });
		fdlg.setFilterNames(new String[] { "素体文件(*.hk)" });
		String restrictDir = new File(equipFilePath).getParentFile()
				.getAbsolutePath();
		fdlg.setFilterPath(restrictDir);
		String file = fdlg.open();
		if (file != null) {
			// 素体文件和装备文件必须在同一个盘上
			String relatePath = Utils.getRelatePath(file, restrictDir);
			if (relatePath == null) {
				MessageDialog.openInformation(getSite().getShell(), "Info",
						"素体文件必须和装备文件位于同一磁盘中。");
				return;
			}
			if (eqp2hook.hasBody(relatePath)) {
				MessageDialog.openInformation(getSite().getShell(), "Info",
						"素体文件" + relatePath + "已经添加过了.");
				return;
			}
			// String fullFilePath = dir+File.separator+filenames[0];
			BodyDef bodyDef = new BodyDef();
			PipAnimateSet newAddBodyPas = null;
			try {
				newAddBodyPas = loadBody(restrictDir, relatePath, bodyDef);
			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Error",
						"加载素体文件错误:\n" + e);
				e.printStackTrace();
				return;
			}
			String[] hookNames = bodyDef.getHookNames();
			SelectHookOnBodyDialog dlg = new SelectHookOnBodyDialog(getSite()
					.getShell());
			dlg.setHookNames(hookNames);
			if (dlg.open() == Dialog.OK) {
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
		bodyAniSet = bodyAniSets.get(bodyAniSets.size() - 1);
		bodyDef.embedHookPieces(bodyAniSet, selHookId, true);
		updateHookLabel();
	}

	private void changeSelBody() {
		if (bodyAniSet == null) {
			selHookId = -1;
			selHookName = null;
			updateHookLabel();
			return;
		}
		int idx = bodyAniSets.indexOf(bodyAniSet);
		selHookId = eqp2hook.getHookId(idx);
		bodyAnimateViewer.setBindHookId(selHookId);
		for (PipAni4AniFramePiece hook : bodyDefs.get(idx).hooks) {
			if (hook.getImageID() == selHookId) {
				selHookName = hook.name;
				break;
			}
		}
		updateHookLabel();
	}

	private void updateHookLabel() {
		if (selHookId < 0) {
			curHookLabel.setText("未选择素体");
			return;
		}
		// /update label
		curHookLabel.setText("当前挂接点:" + selHookId + ":" + selHookName);
		Point p = curHookLabel.getSize();
		p = curHookLabel.computeSize(-1, p.y, true);
		curHookLabel.setSize(p);
		curHookLabel.redraw();
	}

	@Override
	public void dispose() {
		super.dispose();
		equipAniSet = null;// make hookAniDriver thread exit;
		FileWatcher.unwatch(this);
	}

	private void createRightTop(Composite parent) {
		bodyAniTree = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		animateContentTreeProvider = new AnimateContentTreeProvider();
		bodyAniTree.setContentProvider(animateContentTreeProvider);
		bodyAniTree.setLabelProvider(new AnimateLabelProvider());
		bodyAniTree
				.addSelectionChangedListener(new ISelectionChangedListener() {
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
		// String filePath =
		// "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\male3.eqp";
		try {
			eqp2hook.save(equipFilePath, bodyAniSets, equipAniSet);
			if (bodyAnimateViewer.isEquipCtsModified()) {
				saveEquipAnimateSet();
				bodyAnimateViewer.setEquipCtsModified(false);
			}

			if (bodyAnimateViewer.isHookPosChanged()) {
				saveChangedBodyDef();
				bodyAnimateViewer.setHookPosChanged(false);
			}
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "Error",
					"Error save equip-hook mapping.\n" + e);
			e.printStackTrace();
		}
		setDirty(false);
		firePropertyChange(PROP_DIRTY);
	}

	private void saveChangedBodyDef() throws Exception {
		File dir = new File(equipFilePath).getParentFile();
		for (PipAnimateSet bodyAni : changedBodies) {
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
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		FileStoreEditorInput finput = (FileStoreEditorInput) getEditorInput();
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

	public void setDirty(boolean b) {
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

	public void fileModified(File f) {
		if (f.equals(equipAniFile)) {
			if (equipSaving) {
				return;
			} else {
				equipSaving = false;
			}
			display.asyncExec(new Runnable() {
	            public void run() {
	            	// 如果CTS被其他文件修改，並且本编辑器也修改了，则需要提示是否重载文件放弃当前修改。
	            	if (bodyAnimateViewer.isEquipCtsModified()) {
	            		if (!MessageDialog.openConfirm(getSite().getShell(), "文件修改", equipAniFile.getName() + "正在编辑并已被其他编辑程序修改, 是否放弃当前修改重新加载？")) {
	            			return;
	            		}
	    			}
	            	
	            	// 重载CTS文件
	            	PipAnimateSet newSet = new PipAnimateSet();
	            	try {
	            		newSet.load(equipAniFile);
	            	} catch (Exception e) {
	            		MessageDialog.openError(getSite().getShell(), "错误", e.toString());
	            		return;
	            	}
	            	eqp2hook.changeEquipAni(bodyAniSets, equipAniSet, newSet);
	            	equipAniSet = newSet;
    				bodyAnimateViewer.setEquipCtsModified(false);
    				
    				// 刷新显示
    				bodyAnimateViewer.redraw();
    				eqpAniView.setInput(equipAniSet, 1, 1);
    				eqpAniView.redraw();
	            }
	        });
		}
	}
	
	/**
	 * 打开编辑器编辑第N个动画。如果这个动画里只包含一个piece，则打开系统编辑器编辑这一帧。
	 * @param index
	 */
	public void editAnimate(int index) {
		// 尝试打开cts文件
		IEditorPart editor = null;
        IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((equipAniFile.getAbsolutePath())));
        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        editor = page.findEditor(input);
        if (editor != null) {
            page.activate(editor);
        } else {
            try {
            	editor = page.openEditor(input, AnimateEditor.ID);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                return;
            }
        }
        
        // 通知此editor编辑指定动画序列
        ((AnimateEditor)editor).editAnimate(index);
	}
	
	/**
	 * 在下方动画预览区设置选中。
	 */
	public void setFocusAnimate(int sel) {
		eqpAniView.setSelectedFrame(sel);
	}
	
	public void areaSelected(Object source) {
	}
	public void frameSelectionChanged(Object source, int newFrame) {
		filterAnimateID = newFrame; 
		bodyAniTree.refresh(true);
	}
	public void frameDoubleClicked(Object source, int frame) {
	}
	public void contentChanged(Object source) {
		if (source == eqpAniView) {
			bodyAnimateViewer.setEquipCtsModified(true);
			setDirty(true);
		}
	}
	
	protected void onOptimize() {
		// 首先必须保存才可以执行此操作
		if (isDirty()) {
			MessageDialog.openError(getSite().getShell(), "错误", "请保存后再执行此操作。");
			return;
		}
		
		// 找出没有用到的动画序列
		boolean[] flags = new boolean[equipAniSet.getAnimateCount()];
		for (int i = 0; i < eqp2hook.getBodyCnt(); i++) {
			int hookId = eqp2hook.getHookId(i);
			PipAnimateSet pas = bodyAniSets.get(i);
			for (int j = 0; j < pas.getFrameCount(); j++) {
				PipAnimateFrame frame = pas.getFrame(j);
				PipAni4AniFramePiece hook = frame.getHook(hookId);
				PipAnimate pa = hook.getBindAnimate();
				if (pa != null) {
					int equIndex = equipAniSet.getAnimateIndex(pa);
					flags[equIndex] = true;
				}
			}
		}
		List<Integer> unusedIndices = new ArrayList<Integer>();
		for (int i = 0; i < flags.length; i++) {
			if (!flags[i]) {
				unusedIndices.add(i);
			}
		}
		if (unusedIndices.size() == 0) {
			MessageDialog.openError(getSite().getShell(), "错误", "所有装备动画序列都被用到了。");
			return;
		}
		
		// 提示用户是否继续
		String msg = "有" + unusedIndices.size() + "个装备动画序列没有被用到，是否删除？";
		if (!MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
			return;
		}
		msg = "此操作将删除装备动画里的动画序列（帧和图片不会被删除），如果有其他文件引用此动画文件可能会发生错误，确定要继续吗？";
		if (!MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
			return;
		}

//		// 组织一个新旧索引对应表
//		int newIndex = 0;
//		Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
//		for (int oldIndex = 0; oldIndex < flags.length; oldIndex++) {
//			if (flags[oldIndex]) {
//				indexMap.put(oldIndex, newIndex);
//				newIndex++;
//			}
//		}
		
		// 从动画文件中删除需要删除的帧
		for (int i = unusedIndices.size() - 1; i >= 0; i--) {
			equipAniSet.removeAnimate(unusedIndices.get(i));
		}
		bodyAnimateViewer.setEquipCtsModified(true);
		setDirty(true);
		
		msg = "操作已完成，请保存以完成修改。如果要进一步优化帧和图片，请编辑装备动画文件，使用动画编辑器中的优化功能进行优化。";
		MessageDialog.openInformation(getSite().getShell(), "完成", msg);
	}

	/*
	 * 从右上方选择一个动画序列，左下方选择一个装备动画，自动按顺序装配整个动画序列的所有帧。
	 */
	private void autoEquip() {
		// 提取右上方选择的动画序列
		IStructuredSelection sel = (IStructuredSelection) bodyAniTree.getSelection();
		if (sel.isEmpty()) {
			MessageDialog.openError(getSite().getShell(), "错误", "请从右上方列表中选择一个动画序列。");
			return;
		}
		Object selObj = sel.getFirstElement();
		if (!(selObj instanceof PipAnimate)) {
			MessageDialog.openError(getSite().getShell(), "错误", "请从右上方列表中选择一个动画序列。");
			return;
		}
		PipAnimate bodyPa = (PipAnimate)selObj;
		
		// 提取左下方选择的装备动画
		int[] selEqpAniIds = this.eqpAniView.getSelectedFrames();
		if (selEqpAniIds.length != 1) {
			MessageDialog.openInformation(getSite().getShell(), "错误", "请从左下方装备动画列表中选择一个装备动画。");
			return;
		}
		int startID = selEqpAniIds[0];

		// 依次自动绑定
		int cnt = bodyPa.getFrameCount();
		if (startID + cnt > equipAniSet.getAnimateCount()) {
			MessageDialog.openInformation(getSite().getShell(), "错误", "选取坐标越界，请重新选择。");
			return;
		}
		for (int i = 0; i < cnt; i++) {
			PipAnimateFrame frame = bodyPa.getFrame(i).realize();
			frame.getHook(selHookId).bindAnimate(equipAniSet.getAnimate(startID + i));
		}
		allBodyViewer.clearCache();
		bodyAnimateViewer.redraw();
		setDirty(true);
		runHookAniDriver();
	}
}
