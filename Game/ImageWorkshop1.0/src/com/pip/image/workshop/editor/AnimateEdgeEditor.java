package com.pip.image.workshop.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
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
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.ext.EdgeExtension;
import com.swtdesigner.SWTResourceManager;

public class AnimateEdgeEditor extends Composite implements ImageViewerListener {
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return "轮廓" + element;
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
			if (ext != null) {
				int count = ext.edges.size();
				Object[] ret = new Object[count];
				for (int i = 0; i < count; i++) {
					ret[i] = new Integer(i + 1);
				}
				return ret;
			} else {
				return new Object[0];
			}
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private boolean updating = false;
	private Text textLastAnimate;
	private Text textFirstAnimate;
	private List borderList;
	private AnimateEditor owner;
	private PipAnimateSet animateSet;
	private Button buttonFlag;
	private ListViewer borderListViewer;
	private EdgeEditViewer editViewer;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateEdgeEditor(Composite parent, int style, AnimateEditor oo, PipAnimateSet aset) {
		super(parent, style);
		this.owner = oo;
		this.animateSet = aset;
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		buttonFlag = new Button(this, SWT.CHECK);
		buttonFlag.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				owner.setDirty(true);
			}
		});
		buttonFlag.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonFlag.setText("包含轮廓定义");

		borderListViewer = new ListViewer(this, SWT.BORDER);
		borderListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent arg0) {
				edgeSelectionChanged();
			}
		});
		borderListViewer.setLabelProvider(new ListLabelProvider());
		borderListViewer.setContentProvider(new ContentProvider());
		borderList = borderListViewer.getList();
		final GridData gd_borderList = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd_borderList.widthHint = 237;
		borderList.setLayoutData(gd_borderList);

		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 4;
		composite.setLayout(gridLayout_1);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("起始动画ID：");

		textFirstAnimate = new Text(composite, SWT.BORDER);
		textFirstAnimate.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				if (updating) {
					return;
				}
				try {
					EdgeExtension.Edge edge = getEditingEdge();
					int value = Integer.parseInt(textFirstAnimate.getText());
					if (value == -1 || (value >= 0 && value < animateSet.getAnimateCount())) {
						edge.beginAnimateIndex = value;
						owner.setDirty(true);
						textFirstAnimate.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
						return;
					}
				} catch (Exception e) {
				}
				textFirstAnimate.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});
		final GridData gd_textFirstAnimate = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textFirstAnimate.setLayoutData(gd_textFirstAnimate);

		final Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("结束动画ID：");

		textLastAnimate = new Text(composite, SWT.BORDER);
		textLastAnimate.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				if (updating) {
					return;
				}
				try {
					EdgeExtension.Edge edge = getEditingEdge();
					int value = Integer.parseInt(textLastAnimate.getText());
					if (value == -1 || (value >= 0 && value <= animateSet.getAnimateCount())) {
						edge.endAnimateIndex = value;
						owner.setDirty(true);
						textLastAnimate.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
						return;
					}
				} catch (Exception e) {
				}
				textLastAnimate.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});
		final GridData gd_textLastAnimate = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textLastAnimate.setLayoutData(gd_textLastAnimate);

		final Composite editViewerContainer = new Composite(composite, SWT.NONE);
		editViewerContainer.setLayout(new FillLayout());
		final GridData gd_editViewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		editViewerContainer.setLayoutData(gd_editViewerContainer);
		
		editViewer = new EdgeEditViewer(editViewerContainer, SWT.NONE);
		editViewer.setImageViewerListener(this);
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("添加") {
			public void run() {
				onAddEdge();
			}
		});
		mgr.add(new Action("删除") {
			public void run() {
				onDeleteEdge();
			}
		});
		
		Menu menu = mgr.createContextMenu(borderList);
		borderList.setMenu(menu);
		
		// 设置初始值
		borderListViewer.setInput(new Object());
		EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
		if (ext == null) {
			buttonFlag.setSelection(false);
		} else {
			buttonFlag.setSelection(true);
		}
		edgeSelectionChanged();
	}
	
	public void setImageCache(ImageDrawCache cache) {
		editViewer.setImageCache(cache);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected EdgeExtension.Edge getEditingEdge() {
		EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
		if (ext == null) {
			return null;
		}
		int sel = borderList.getSelectionIndex();
		if (sel == -1) {
			return null;
		}
		return ext.edges.get(sel);
	}

	protected void onAddEdge() {
		EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
		if (ext == null) {
			ext = new EdgeExtension();
			animateSet.addExtension(ext);
		}
		EdgeExtension.Edge newEdge = new EdgeExtension.Edge();
		newEdge.beginAnimateIndex = -1;
		newEdge.endAnimateIndex = -1;
		newEdge.beginY = 0;
		newEdge.height = 0;
		newEdge.beginX = new int[0];
		newEdge.endX = new int[0];
		ext.edges.add(newEdge);
		owner.setDirty(true);
		borderListViewer.refresh();
		edgeSelectionChanged();
	}
	
	protected void onDeleteEdge() {
		EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
		if (ext == null) {
			return;
		}
		int sel = borderList.getSelectionIndex();
		ext.edges.remove(sel);
		owner.setDirty(true);
		borderListViewer.refresh();
		edgeSelectionChanged();
	}
	
	protected void edgeSelectionChanged() {
		EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
		int sel = borderList.getSelectionIndex();
		if (ext == null || sel == -1) {
			textFirstAnimate.setEnabled(false);
			textLastAnimate.setEnabled(false);
			editViewer.setInput(null, null);
		} else {
			textFirstAnimate.setEnabled(true);
			textLastAnimate.setEnabled(true);
			updating = true;
			textFirstAnimate.setText(String.valueOf(ext.edges.get(sel).beginAnimateIndex));
			textLastAnimate.setText(String.valueOf(ext.edges.get(sel).endAnimateIndex));
			editViewer.setInput(animateSet, ext.edges.get(sel));
			updating = false;
		}
	}
	
	public void checkSave() throws Exception {
		if (!buttonFlag.getSelection()) {
			animateSet.removeExtension("EDGE");
			return;
		}
		
		// 数据合法性检查，检查所有边界是否有重复的部分，以及是否有结束ID小于起始ID的情况
		EdgeExtension ext = (EdgeExtension)animateSet.findExtension("EDGE");
		if (ext == null) {
			return;
		}
		boolean[] coverFlag = new boolean[animateSet.getAnimateCount()];
		for (int i = 0; i < ext.edges.size(); i++) {
			EdgeExtension.Edge edge = ext.edges.get(i);
			if (edge.beginAnimateIndex != -1 && edge.endAnimateIndex != -1 && edge.beginAnimateIndex >= edge.endAnimateIndex) {
				throw new Exception("边界" + (i + 1) + "：起始动画ID不能超过结束动画ID。");
			}
			if (edge.beginAnimateIndex != -1 && (edge.beginAnimateIndex < 0 || edge.beginAnimateIndex >= animateSet.getAnimateCount())) {
				throw new Exception("边界" + (i + 1) + "：起始动画ID越界。");
			}
			if (edge.endAnimateIndex != -1 && (edge.endAnimateIndex < 0 || edge.endAnimateIndex > animateSet.getAnimateCount())) {
				throw new Exception("边界" + (i + 1) + "：起始动画ID越界。");
			}
			int start = edge.beginAnimateIndex;
			if (start == -1) {
				start = 0;
			}
			int end = edge.endAnimateIndex;
			if (end == -1) {
				end = animateSet.getAnimateCount();
			}
			for (int j = start; j < end; j++) {
				if (coverFlag[j]) {
					throw new Exception("边界" + (i + 1) + "：动画ID范围和前面的边界有重叠。");
				}
				coverFlag[j] = true;
			}
		}
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
}
