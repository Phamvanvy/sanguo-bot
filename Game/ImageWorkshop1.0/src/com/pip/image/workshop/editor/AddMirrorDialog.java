package com.pip.image.workshop.editor;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;

public class AddMirrorDialog extends Dialog {
    class ContentProvider implements IStructuredContentProvider {
    	public Object[] getElements(Object inputElement) {
    		return indices.toArray();
    	}
    	public void dispose() {
    	}
    	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	}
    }
    class ListLabelProvider extends LabelProvider {
    	public String getText(Object element) {
    		int index = ((Integer)element).intValue();
    		return index + ". " + animateSet.getFrame(index).getName();
    	}
    	public Image getImage(Object element) {
    		return null;
    	}
    }
    private List rightList;
    private List leftList;
    
    private PipAnimateSet animateSet;
    private java.util.List<Integer> indices;
    private java.util.List<PipAnimateFrame> frames;
	private ListViewer leftListViewer;
	private ListViewer rightListViewer;
	
	private java.util.List<Integer> leftFrames;
	private java.util.List<Integer> rightFrames;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public AddMirrorDialog(Shell parentShell, PipAnimateSet animateSet) {
        super(parentShell);
        this.animateSet = animateSet;
        indices = new ArrayList<Integer>();
        frames = new ArrayList<PipAnimateFrame>();
        for (int i = 0; i < animateSet.getFrameCount(); i++) {
        	PipAnimateFrame frame = animateSet.getFrame(i);
        	if (animateSet.getMirrorFrame(frame) == null) {
        		indices.add(i);
        		frames.add(frame);
        	}
        }
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 20;
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        leftListViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
        leftListViewer.setContentProvider(new ContentProvider());
        leftListViewer.setLabelProvider(new ListLabelProvider());
        leftList = leftListViewer.getList();
        leftList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        leftListViewer.setInput(this);

        rightListViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
        rightListViewer.setLabelProvider(new ListLabelProvider());
        rightListViewer.setContentProvider(new ContentProvider());
        rightList = rightListViewer.getList();
        rightList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        rightListViewer.setInput(this);

        return container;
    }
    
    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(728, 621);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("添加对称帧");
    }
    
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			int[] indices1 = leftList.getSelectionIndices();
			int[] indices2 = rightList.getSelectionIndices();
			if (indices1.length == 0 || indices2.length == 0) {
				MessageDialog.openError(getShell(), "错误", "请从两侧列表中选择需要建立对称关系的帧。");
				return;
			}
			if (indices1.length != indices2.length) {
				MessageDialog.openError(getShell(), "错误", "两侧选择的帧数必须相等。");
				return;
			}
			leftFrames = new ArrayList<Integer>();
			rightFrames = new ArrayList<Integer>();
			for (int i = 0; i < indices1.length; i++) {
				leftFrames.add(indices.get(indices1[i]));
				rightFrames.add(indices.get(indices2[i]));
			}
			for (int i = 0; i < indices1.length; i++) {
				if (rightFrames.contains(indices1[i])) {
					MessageDialog.openError(getShell(), "错误", "两侧选择的帧数不能重叠。");
					return;
				}
			}
		}
		super.buttonPressed(buttonId);
	}
	
	public PipAnimateFrame[][] getMirrorFrames() {
		PipAnimateFrame[][] ret = new PipAnimateFrame[leftFrames.size()][2];
		for (int i = 0; i < leftFrames.size(); i++) {
			ret[i][0] = animateSet.getFrame(leftFrames.get(i));
			ret[i][1] = animateSet.getFrame(rightFrames.get(i));
		}
		return ret;
	}
}
