package com.pip.image.workshop.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.ext.EdgeExtension;
import com.pipimage.image.ext.HookPointExtension;
import com.swtdesigner.SWTResourceManager;

public class AnimateHookPointEditor extends Composite implements ImageViewerListener {
	class FrameListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			int index = ((Integer)element).intValue();
			return (index + 1) + ". " + animateSet.getFrame(index).getName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	
	class FrameContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Object[] ret = new Object[animateSet.getFrameCount()];
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
	
	class HookListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			int index = ((Integer)element).intValue();
			HookPointExtension.HookPoint hp = animateSet.getHookPoints().hooks.get(index);
			return hp.name;
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	
	class HookContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Object[] ret = new Object[animateSet.getHookPoints().hooks.size()];
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
	
	private List frameList;
	private List hookList;
	private AnimateEditor owner;
	private PipAnimateSet animateSet;
	private ListViewer hookListViewer;
	private ListViewer frameListViewer;
	private AnimateHookPointEditViewer editViewer;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateHookPointEditor(Composite parent, int style, AnimateEditor oo, PipAnimateSet aset) {
		super(parent, style);
		this.owner = oo;
		this.animateSet = aset;
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		hookListViewer = new ListViewer(this, SWT.V_SCROLL | SWT.BORDER);
		hookListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent arg0) {
				hookSelectionChanged();
			}
		});
		hookListViewer.setLabelProvider(new HookListLabelProvider());
		hookListViewer.setContentProvider(new HookContentProvider());
		hookList = hookListViewer.getList();
		final GridData gd_hookList = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd_hookList.heightHint = 100;
		gd_hookList.widthHint = 237;
		hookList.setLayoutData(gd_hookList);

		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		final GridLayout gridLayout_1 = new GridLayout();
		composite.setLayout(gridLayout_1);

		final Composite editViewerContainer = new Composite(composite, SWT.NONE);
		editViewerContainer.setLayout(new FillLayout());
		final GridData gd_editViewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		editViewerContainer.setLayoutData(gd_editViewerContainer);
		
		editViewer = new AnimateHookPointEditViewer(editViewerContainer, SWT.NONE);
		editViewer.setImageViewerListener(this);
		editViewer.setOwnerEditor(owner);
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("添加") {
			public void run() {
				onAddHook();
			}
		});
		mgr.add(new Action("删除") {
			public void run() {
				onDeleteHook();
			}
		});
		
		Menu menu = mgr.createContextMenu(hookList);
		hookList.setMenu(menu);
		
		frameListViewer = new ListViewer(this, SWT.V_SCROLL | SWT.BORDER);
		frameListViewer.setLabelProvider(new FrameListLabelProvider());
		frameListViewer.setContentProvider(new FrameContentProvider());
		frameList = frameListViewer.getList();
		frameList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		frameListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent arg0) {
				hookSelectionChanged();
			}
		});

		// 设置初始值
		hookListViewer.setInput(new Object());
		frameListViewer.setInput(new Object());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void onAddHook() {
		InputDialog dlg = new InputDialog(getShell(), "输入", "请输入挂接点名称", "挂接点", new IInputValidator() {
			public String isValid(String value) {
				if (value.trim().isEmpty()) {
					return "必须输入一个名字。";
				}
				return null;
			}
		});
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		String name = dlg.getValue().trim();
		
		HookPointExtension ext = animateSet.getHookPoints();
		HookPointExtension.HookPoint hp = new HookPointExtension.HookPoint();
		hp.name = name;
		ext.hooks.add(hp);
		owner.setDirty(true);
		hookListViewer.refresh();
		hookSelectionChanged();
	}
	
	protected void onDeleteHook() {
		int sel = hookList.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		HookPointExtension ext = animateSet.getHookPoints();
		ext.hooks.remove(sel);
		owner.setDirty(true);
		hookListViewer.refresh();
		hookSelectionChanged();
	}
	
	protected void hookSelectionChanged() {
		HookPointExtension ext = animateSet.getHookPoints();
		int sel = hookList.getSelectionIndex();
		if (sel == -1) {
			editViewer.setInput(null, null);
			return;
		}
		int sel2 = frameList.getSelectionIndex();
		if (sel2 == -1) {
			editViewer.setInput(null, null);
			return;
		}

		HookPointExtension.HookPoint hp = ext.hooks.get(sel);
		PipAnimateFrame frame = animateSet.getFrame(sel2);
		editViewer.setInput(frame, hp);
	}
	
	public void refreshFrameList() {
		frameListViewer.refresh();
		hookSelectionChanged();
	}
	
	@Override
	public void areaSelected(Object source) {
	}

	@Override
	public void contentChanged(Object source) {
		owner.setDirty(true);
	}

	@Override
	public void frameDoubleClicked(Object source, int frame) {
	}

	@Override
	public void frameSelectionChanged(Object source, int newFrame) {
	}
	
	public void setImageCache(ImageDrawCache cache) {
		editViewer.setImageCache(cache);
	}
}
