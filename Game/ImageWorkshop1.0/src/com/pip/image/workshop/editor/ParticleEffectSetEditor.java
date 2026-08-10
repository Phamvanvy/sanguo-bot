package com.pip.image.workshop.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AnimateEditor.AnimateFramesContentProvider;
import com.pip.image.workshop.editor.AnimateEditor.AnimateFramesLabelProvider;
import com.pip.image.workshop.editor.AnimateEditor.AnimatesContentProvider;
import com.pip.image.workshop.editor.AnimateEditor.AnimatesLabelProvider;
import com.pip.image.workshop.editor.AnimateEditor.FramesContentProvider;
import com.pip.image.workshop.editor.AnimateEditor.FramesLabelProvider;
import com.pip.image.workshop.editor.AnimateEditor.ImagesContentProvider;
import com.pip.image.workshop.editor.AnimateEditor.ImagesLabelProvider;
import com.pip.mango.jni.GLUtils;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.propertysheet.StringMapping;
import com.pip.util.FileWatcher;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipParticleEffect;
import com.pipimage.image.PipParticleEffectSet;
import com.pipimage.image.PipParticlePath;
import com.pipimage.image.PipParticleSet;
import com.pipimage.image.path.StayPath;
import com.swtdesigner.ResourceManager;

public class ParticleEffectSetEditor extends EditorPart {
	public static final int PROPERTY_CANUNDO = 1;
	public static final int PROPERTY_CANREDO = 2;
	
	class EffectsContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Integer[] ret = new Integer[effectSet.getEffectCount()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class EffectsLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int index = ((Integer)element).intValue();
			if (columnIndex == 0) {
                return index + ". " + effectSet.getEffect(index).title;
            } else {
                return String.valueOf(effectSet.getEffect(index).particleSets.size());
            }
		}
		public Image getColumnImage(Object element, int columnIndex) {
		    if(columnIndex == 0){
		        return WorkshopPlugin.getDefault().getImageRegistry().get("image");
		    }else{
		        return null;
		    }
		}
	}
	class ParticlesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			int sel = effectsTable.getSelectionIndex();
			if (sel == -1) {
				return new Object[0];
			}
			PipParticleEffect effect = effectSet.getEffect(sel);
			Integer[] ret = new Integer[effect.particleSets.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ParticlesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int row = ((Integer)element).intValue();
			int sel = effectsTable.getSelectionIndex();
			if (sel == -1) {
				return "";
			}
			PipParticleEffect effect = effectSet.getEffect(sel);
			if (columnIndex == 0) {
				return (row + 1) + ". " + effect.particleSets.get(row).title;
			} else {
				return "";
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return WorkshopPlugin.getDefault().getImageRegistry().get("image");
			} else {
				return null;
			}
		}
	}
	
	public static final String ID = "com.pip.image.workshop.editor.ParticleEffectSetEditor"; //$NON-NLS-1$

	private TableViewer effectsViewer;
	private Table effectsTable;
	private CheckboxTableViewer particlesViewer;
	private Table particlesTable;
	
	private ParticleEffectViewer effectViewer;
	
	private Action newEffectAction, delEffectAction;
	private Action newParticleAction, delParticleAction; 
	private Display display;
	
	private File effectFile;
	private boolean dirty = false;
	private PipParticleEffectSet effectSet;
	private Image backgroundImage;
	
	private StateManager stateMgr;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
	    display = this.getSite().getShell().getDisplay();
	    
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.verticalSpacing = 0;
		gridLayout_1.marginWidth = 0;
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.marginHeight = 0;
		container.setLayout(gridLayout_1);

		final SashForm sashForm_4 = new SashForm(container, SWT.NONE);
		sashForm_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		effectViewer = new ParticleEffectViewer(sashForm_4, SWT.NONE);
		effectViewer.setAnimates(effectSet.getSourceAnimate());

		final Composite composite_3 = new Composite(sashForm_4, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite_3.setLayout(gridLayout);

		final ToolBar toolBar = new ToolBar(container, SWT.NONE);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final ToolItem exportCTSItem = new ToolItem(toolBar, SWT.PUSH);
		exportCTSItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onExportCTS();
			}
		});
		exportCTSItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/qsplit_large.gif"));
		exportCTSItem.setText("导出CTS文件");

		final ToolItem exportClientItem = new ToolItem(toolBar, SWT.PUSH);
		exportClientItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onExportClientFormat();
			}
		});
		exportClientItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/palette_large.gif"));
		exportClientItem.setText("导出客户端文件");

		final ToolItem setBackgroundItem = new ToolItem(toolBar, SWT.PUSH);
		setBackgroundItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onSetBackground();
			}
		});
		setBackgroundItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/open_large.gif"));
		setBackgroundItem.setText("设置背景");
		
		final ToolItem newItemToolItem = new ToolItem(toolBar, SWT.PUSH);
		newItemToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/open_large.gif"));
		newItemToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				chooseRefAnimateSet();
			}
		});
		newItemToolItem.setText("参考动画");
		
		sashForm_4.setWeights(new int[] {3, 1 });
		
		createRightPart(composite_3);
		
		effectSelectionChanged();
		particleSelectionChanged();

		this.setPartName(this.getEditorInput().getName());
		this.setPartProperty("", "");
	}

	private void createRightPart(Composite composite_3) {
		final SashForm sashForm_5 = new SashForm(composite_3, SWT.VERTICAL);
		sashForm_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createEffectTable(sashForm_5);
		createParticleTable(sashForm_5);
		
		sashForm_5.setWeights(new int[] {1, 1 });
	}

	private void createEffectTable(Composite sashForm_5) {
		effectsViewer = new TableViewer(sashForm_5, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		effectsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				effectSelectionChanged();
			}
		});
		effectsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				effectDoubleClicked();
			}
		});
		effectsTable = effectsViewer.getTable();
		effectsTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if (event.keyCode == SWT.DEL) {
                    onDelEffect();
                }
            }
        });

		final TableColumn effectNameColumn = new TableColumn(effectsTable, SWT.NONE);
		effectNameColumn.setWidth(170);
		final TableColumn effectParticlesColumn = new TableColumn(effectsTable, SWT.NONE);
		effectParticlesColumn.setWidth(30);
		effectsViewer.setContentProvider(new EffectsContentProvider());
		effectsViewer.setLabelProvider(new EffectsLabelProvider());
		effectsViewer.setInput(new Object());		
	}
	
	private void createParticleTable(Composite sashForm_5) {
		particlesViewer = CheckboxTableViewer.newCheckList(sashForm_5, SWT.BORDER | SWT.FULL_SELECTION);
		particlesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				particleSelectionChanged();
			}
		});
		particlesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				particleCheckChanged();
			}
		});
		particlesViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				particleDoubleClicked();
			}
		});
		particlesViewer.setLabelProvider(new ParticlesLabelProvider());
		particlesViewer.setContentProvider(new ParticlesContentProvider());
		particlesViewer.setInput(new Object());
		particlesTable = particlesViewer.getTable();
		particlesTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if (event.keyCode == SWT.DEL) {
                    onDelParticle();
                } 
                if ((event.stateMask & SWT.SHIFT) != 0) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        onMoveUpParticle();
                        event.doit = false;
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        onMoveDownParticle();
                        event.doit = false;
                    }
                }
            }
        });		
	}

	public void setFocus() {
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			effectSet.save(effectFile);
			setDirty(false);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
			monitor.setCanceled(true);
		}
	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		createActions();
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		String filePath = Utils.urlToPath(url);
		effectFile = new File(filePath);
		try {
			effectSet = new PipParticleEffectSet();
			effectSet.load(effectFile);
		} catch (Exception e) {
			SWTUtils.showError(site.getShell(), "错误", "文件格式错误。", e);
		}
		stateMgr = new StateManager(20000000);
		saveState();
	}
	
	private void saveState() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			effectSet.saveState(new DataOutputStream(bos));
			stateMgr.push(bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void restoreState(byte[] data) {
		try {
			effectSet.restoreState(new DataInputStream(new ByteArrayInputStream(data)));
			effectsViewer.refresh();
			effectSelectionChanged();
			particlesViewer.refresh();
			particleSelectionChanged();
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void undo() {
		if (!canUndo()) {
			return;
		}
		restoreState(stateMgr.getUndoData());
		firePropertyChange(PROPERTY_CANUNDO);
		firePropertyChange(PROPERTY_CANREDO);
	}
	
	public void redo() {
		if (!canRedo()) {
			return;
		}
		restoreState(stateMgr.getRedoData());
		firePropertyChange(PROPERTY_CANUNDO);
		firePropertyChange(PROPERTY_CANREDO);
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	private void setDirty(boolean value) {
		dirty = value;
		firePropertyChange(PROP_DIRTY);
		if (value) {
			saveState();
			firePropertyChange(PROPERTY_CANUNDO);
			firePropertyChange(PROPERTY_CANREDO);
		}
	}

	private void createActions() {
		newEffectAction = new Action("新建效果") {
			public void run() {
				onNewEffect();
			}
		};
		delEffectAction = new Action("删除效果") {
			public void run() {
				onDelEffect();
			}
		};
		newParticleAction = new Action("新建粒子") {
			public void run() {
				onNewParticle();
			}
		};
		delParticleAction = new Action("删除粒子") {
			public void run() {
				onDelParticle();
			}
		};
	}
	
	private void onNewEffect() {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建效果", "请输入效果标题：", "", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "标题不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			String newname = dlg.getValue();
			PipParticleEffect effect = new PipParticleEffect();
			effect.title = newname;
			effectSet.addEffect(effect);
			effectsViewer.refresh();
			effectsTable.setSelection(effectSet.getEffectCount() - 1);
			effectSelectionChanged();
			setDirty(true);
		}
	}
	
	private void onDelEffect() {
		int sel = effectsTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		effectSet.removeEffect(sel);
		effectsViewer.refresh();
		if (sel < effectSet.getEffectCount()) {
			effectsTable.setSelection(sel);
		} else if (sel > 0) {
			effectsTable.setSelection(sel - 1);
		} else {
			effectsTable.setSelection(new int[0]);
		}
		effectSelectionChanged();
	}
	
	private void onNewParticle() {
		PipParticleEffect effect = getSelectedEffect();
		if (effect == null) {
			return;
		}
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建粒子", "请输入粒子标题：", "", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "标题不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			String newname = dlg.getValue();
			PipParticleSet pset = new PipParticleSet();
			pset.title = newname;
			pset.path = new StayPath();
			effect.particleSets.add(pset);
			particlesViewer.refresh();
			particlesTable.setSelection(effect.particleSets.size() - 1);
			particlesViewer.setChecked(new Integer(effect.particleSets.size() - 1), true);
			particleSelectionChanged();
			setDirty(true);
		}
	}
	
	private void onDelParticle() {
		PipParticleEffect effect = getSelectedEffect();
		if (effect == null) {
			return;
		}
		int sel = particlesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		effect.particleSets.remove(sel);
		particlesViewer.refresh();
		if (sel < effect.particleSets.size()) {
			particlesTable.setSelection(sel);
		} else if (sel > 0) {
			particlesTable.setSelection(sel - 1);
		} else {
			particlesTable.setSelection(new int[0]);
		}
		particleSelectionChanged();
		setDirty(true);
	}

	private void onMoveUpParticle() {
		PipParticleEffect effect = getSelectedEffect();
		if (effect == null) {
			return;
		}
		int sel = particlesTable.getSelectionIndex();
		if (sel < 1) {
			return;
		}
		PipParticleSet ps1 = effect.particleSets.get(sel - 1);
		PipParticleSet ps2 = effect.particleSets.get(sel);
		effect.particleSets.set(sel - 1, ps2);
		effect.particleSets.set(sel, ps1);
		particlesViewer.refresh();
		particlesTable.setSelection(sel - 1);
		particleSelectionChanged();
		setDirty(true);
	}
	
	private void onMoveDownParticle() {
		PipParticleEffect effect = getSelectedEffect();
		if (effect == null) {
			return;
		}
		int sel = particlesTable.getSelectionIndex();
		if (sel == -1 || sel >= effect.particleSets.size() - 1) {
			return;
		}
		PipParticleSet ps1 = effect.particleSets.get(sel);
		PipParticleSet ps2 = effect.particleSets.get(sel + 1);
		effect.particleSets.set(sel, ps2);
		effect.particleSets.set(sel + 1, ps1);
		particlesViewer.refresh();
		particlesTable.setSelection(sel + 1);
		particleSelectionChanged();
		setDirty(true);
	}

	private void effectSelectionChanged() {
		int sel = effectsTable.getSelectionIndex();
		particlesViewer.refresh();
		particlesViewer.setAllChecked(true);
		particlesTable.setSelection(new int[] { 0 });
		particleSelectionChanged();
		updateEffectViewer();
		
		MenuManager mgr = new MenuManager();
		mgr.add(newEffectAction);
		if (sel != -1) {
			mgr.add(delEffectAction);
		}
		Menu menu = mgr.createContextMenu(effectsTable);
		if (effectsTable.getMenu() != null) {
			effectsTable.getMenu().dispose();
		}
		effectsTable.setMenu(menu);
	}
	
	private void particleSelectionChanged() {
		if (this.getSelectedEffect() == null) {
			particlesTable.setMenu(null);
			return;
		}
		MenuManager mgr = new MenuManager();
		mgr.add(newParticleAction);
		if (particlesTable.getSelectionIndex() != -1) {
			mgr.add(delParticleAction);
		}
		Menu menu = mgr.createContextMenu(particlesTable);
		if (particlesTable.getMenu() != null) {
			particlesTable.getMenu().dispose();
		}
		particlesTable.setMenu(menu);
	}
	
	private void particleCheckChanged() {
		updateEffectViewer();
	}
	
	private void effectDoubleClicked() {
		PipParticleEffect effect = getSelectedEffect();
		if (effect == null) {
			return;
		}
		
		// 编辑参数
		EditParticleEffectDialog dlg = new EditParticleEffectDialog(getSite().getShell(), effect);
		if (dlg.open() == Dialog.OK) {
			effectsViewer.refresh();
			particlesViewer.refresh();
			setDirty(true);
			updateEffectViewer();
		}
	}
	
	private void updateEffectViewer() {
		int sel = effectsTable.getSelectionIndex();
		if (sel == -1) {
			effectViewer.setInput(null);
		} else {
			PipParticleEffect eff = effectSet.getEffect(sel);
			boolean[] visibleFlag = new boolean[eff.particleSets.size()];
			Object[] objs = particlesViewer.getCheckedElements();
			for (Object obj : objs) {
				int index = ((Integer)obj).intValue();
				if (index >= 0 && index < visibleFlag.length) {
					visibleFlag[index] = true;
				}
			}
			try {
				effectViewer.setInput(eff.generateParticles(visibleFlag), eff.startTick, eff.stopTick);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void particleDoubleClicked() {
		int sel = particlesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		PipParticleEffect effect = getSelectedEffect();
		if (effect == null) {
			return;
		}
		PipParticleSet pset = effect.particleSets.get(sel);
		
		// 编辑粒子参数
		PipParticleSet psetEdit = new PipParticleSet();
		psetEdit.update(pset);
		EditParticleSetDialog dlg = new EditParticleSetDialog(getSite().getShell(), effectSet.getSourceAnimate(), psetEdit, effectSet);
		if (dlg.open() == Dialog.OK) {
			pset.update(psetEdit);
			particlesViewer.refresh();
			setDirty(true);
			updateEffectViewer();
		}
	}
	
	private PipParticleEffect getSelectedEffect() {
		int sel = effectsTable.getSelectionIndex();
		if (sel == -1) {
			return null;
		}
		return effectSet.getEffect(sel);
	}
	
	public boolean canUndo() {
		return stateMgr.canUndo();
	}
	
	public boolean canRedo() {
	    return stateMgr.canRedo();
	}
	
	public void dispose() {
		super.dispose();
	}
	
	private void onExportCTS() {
		InputDialog dlg = new InputDialog(getSite().getShell(), "CTS文件名", "请输入CTS文件名：", "", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "文件名不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			try {
				String ctsname = dlg.getValue();
				if (!ctsname.toLowerCase().endsWith(".cts")) {
					ctsname = ctsname + ".cts";
				}
				PipAnimateSet newAnimateSet = effectSet.toAnimateSet();
				File outputFile = new File(effectSet.getOriginalFile().getParentFile(), ctsname);
				newAnimateSet.save(outputFile, true);
				MessageDialog.openInformation(getSite().getShell(), "导出CTS", "导出成功！");
			} catch (Exception e) {
				SWTUtils.showError(getSite().getShell(), "错误", e);
			}
		}
	}
	
	private void onExportClientFormat() {
		try {
			String fname = effectSet.getOriginalFile().getName();
			fname = fname.substring(0, fname.length() - 1) + "c";
			File outputFile = new File(effectSet.getOriginalFile().getParentFile(), fname);
			effectSet.saveClientFormat(outputFile);
			MessageDialog.openInformation(getSite().getShell(), "导出客户端文件", "导出成功！");
		} catch (Exception e) {
			e.printStackTrace();
			SWTUtils.showError(getSite().getShell(), "错误", e);
		}
	}
	
	private void onSetBackground() {
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.png", "*.*" });
		dlg.setFilterNames(new String[] { "PNG图片文件(*.png)", "所有文件(*.*)" });
		String imgFile = dlg.open();
		if (imgFile != null) {
			try {
				Image newImg = new Image(getSite().getShell().getDisplay(), imgFile);
				if (backgroundImage != null) {
					GLUtils.unloadImage(backgroundImage);
				}
				backgroundImage = newImg;
				effectViewer.setBackgroundImage(backgroundImage);
			} catch (Exception e) {
			}
		}
	}
	
	private void chooseRefAnimateSet() {
        // 选择一个目标文件
        FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.cts" });
        dlg.setFilterNames(new String[] { "CTS动画文件(*.cts)" });
        if (effectFile != null) {
        	dlg.setFilterPath(effectFile.getParent());
        }
        String newFile = dlg.open();
        if (newFile == null) {
            return;
        }
        
        // 载入
        try {
        	PipAnimateSet ani = new PipAnimateSet();
        	ani.load(new File(newFile));
        	if (ani.getAnimateCount() == 0) {
        		effectViewer.setRefAnimate(null);
        	} else { 
        		effectViewer.setRefAnimate(ani.getAnimate(0));
        	}
        } catch (Exception e) {
        	SWTUtils.showError(getSite().getShell(), "错误", e);
            return;
        }
    }
}
