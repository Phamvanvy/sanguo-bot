package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;

/**
 * 帧对称关系编辑器。
 * @author lighthu
 */
public class AnimateMirrorFrameEditor extends Composite implements SelectionListener {
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int index = ((Integer)element).intValue();
			if (columnIndex == 0) {
				return "";
			} else if (columnIndex == 1) {
				PipAnimateFrame frame = leftFrames.get(index);
				int index1 = animateSet.findFrame(frame);
				return index1 + ". " + frame.getName();
			} else if (columnIndex == 2) {
				return "<->";
			} else if (columnIndex == 3) {
				PipAnimateFrame frame = rightFrames.get(index);
				int index1 = animateSet.findFrame(frame);
				return index1 + ". " + frame.getName();
			}
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Object[] ret = new Object[leftFrames.size()];
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
	
	private Table table;
	private AnimateEditor owner;
	private PipAnimateSet animateSet;
	
	private List<PipAnimateFrame> leftFrames = new ArrayList<PipAnimateFrame>();
	private List<PipAnimateFrame> rightFrames = new ArrayList<PipAnimateFrame>();
	private Button buttonAdd;
	private Composite container;
	private Button buttonRemove;
	private TableViewer tableViewer;
	
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateMirrorFrameEditor(Composite parent, int style, AnimateEditor oo, PipAnimateSet aset) {
		super(parent, style);
		this.owner = oo;
		this.animateSet = aset;
		final GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		container = new Composite(this, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 1;
		container.setLayout(gridLayout_1);

		final Composite buttonPanel = new Composite(container, SWT.NONE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		gridLayout_2.verticalSpacing = 0;
		gridLayout_2.marginWidth = 0;
		gridLayout_2.marginHeight = 0;
		gridLayout_2.horizontalSpacing = 0;
		buttonPanel.setLayout(gridLayout_2);
		final GridData gd_buttonPanel = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		buttonPanel.setLayoutData(gd_buttonPanel);

		buttonAdd = new Button(buttonPanel, SWT.NONE);
		buttonAdd.addSelectionListener(this);
		buttonAdd.setText("添加对称关系");

		buttonRemove = new Button(buttonPanel, SWT.NONE);
		buttonRemove.addSelectionListener(this);
		buttonRemove.setText("删除对称关系");

		tableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableColumn emptyColumn = new TableColumn(table, SWT.NONE);

		final TableColumn leftColumn = new TableColumn(table, SWT.CENTER);
		leftColumn.setWidth(200);

		final TableColumn centerColumn = new TableColumn(table, SWT.CENTER);
		centerColumn.setWidth(72);

		final TableColumn rightColumn = new TableColumn(table, SWT.CENTER);
		rightColumn.setWidth(200);
		
		configLines();
		tableViewer.setInput(this);
	}
	
	public void onFrameDeleted(PipAnimateFrame frame) {
		int index = leftFrames.indexOf(frame);
		if (index == -1) {
			index = rightFrames.indexOf(frame);
		}
		if (index != -1) {
			leftFrames.remove(index);
			rightFrames.remove(index);
			tableViewer.refresh();
		}
	}
	
	private void configLines() {
		Set<PipAnimateFrame> processed = new HashSet<PipAnimateFrame>();
		for (int i = 0; i < animateSet.getFrameCount(); i++) {
			PipAnimateFrame ani1 = animateSet.getFrame(i);
			if (processed.contains(ani1)) {
				continue;
			}
			PipAnimateFrame ani2 = animateSet.getMirrorFrame(ani1);
			if (ani2 == null || processed.contains(ani2)) {
				continue;
			}
			leftFrames.add(ani1);
			rightFrames.add(ani2);
			processed.add(ani1);
			processed.add(ani2);
		}
	}
	

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == buttonAdd) {
			AddMirrorDialog dlg = new AddMirrorDialog(getShell(), animateSet);
			if (dlg.open() != Dialog.OK) {
				return;
			}
			PipAnimateFrame[][] frames = dlg.getMirrorFrames();
			for (int i = 0; i < frames.length; i++) {
				animateSet.getMirrorSetting().setMirror(frames[i][0], frames[i][1]);
				leftFrames.add(frames[i][0]);
				rightFrames.add(frames[i][1]);
			}
			tableViewer.refresh();
			owner.setDirty(true);
		} else if (e.getSource() == buttonRemove) {
			int[] indices = table.getSelectionIndices();
			if (indices == null || indices.length == 0) {
				return;
			}
			Arrays.sort(indices);
			for (int i = indices.length - 1; i >= 0; i--) {
				animateSet.getMirrorSetting().removeMirror(leftFrames.get(indices[i]));
				leftFrames.remove(indices[i]);
				rightFrames.remove(indices[i]);
			}
			tableViewer.refresh();
			owner.setDirty(true);
		}
	}
	  
	public void widgetDefaultSelected(SelectionEvent e) {}
}
