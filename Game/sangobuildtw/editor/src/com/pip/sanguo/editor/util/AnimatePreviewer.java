package com.pip.sanguo.editor.util;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;

import com.pip.image.workshop.FileInput;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AnimateEditor;
import com.pip.image.workshop.editor.AnimateViewer;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;

/**
 * 一个预览动画文件内容的组件。
 * @author lighthu
 */
public class AnimatePreviewer extends Composite implements IFileModificationListener, DisposeListener {
    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (animateSet == null) {
                return new Object[0];
            } else {
                Integer[] ret = new Integer[animateSet.getAnimateCount()];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = new Integer(i);
                }
                return ret;
            }
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            int index = ((Integer)element).intValue();
            return index + ". " + animateSet.getAnimate(index).getName();
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
        }
    }
    private TableViewer animateList;
    private Table animateListTable;
    private AnimateViewer animateViewer;
    private Button buttonEdit;
    private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());

    private File animateFile;
    private PipAnimateSet animateSet;

    /**
     * Create the composite
     * @param parent
     * @param style
     */
    public AnimatePreviewer(Composite parent, int style) {
        super(parent, style);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        setLayout(gridLayout);
        toolkit.adapt(this);
        toolkit.paintBordersFor(this);

        animateList = new TableViewer(this, SWT.FULL_SELECTION | SWT.BORDER);
        animateList.setContentProvider(new ContentProvider());
        animateList.setLabelProvider(new TableLabelProvider());
        animateList.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                updatePreviewFrame();
            }
        });
        animateListTable = animateList.getTable();
        toolkit.adapt(animateListTable, true, true);
        final GridData gd_animateListTable = new GridData(SWT.FILL, SWT.FILL, false, true);
        animateListTable.setLayoutData(gd_animateListTable);

        final TableColumn column1 = new TableColumn(animateListTable, SWT.NONE);
        column1.setWidth(150);

        animateList.setInput(new Object());

        animateViewer = new AnimateViewer(this, SWT.NONE);
        animateViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        animateViewer.setLayout(new GridLayout());

        buttonEdit = new Button(this, SWT.NONE);
        buttonEdit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onEdit();
            }
        });
        final GridData gd_buttonOpenFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        buttonEdit.setLayoutData(gd_buttonOpenFile);
        toolkit.adapt(buttonEdit, true, true);
        buttonEdit.setText("编辑...");
        
        addDisposeListener(this);
    }

    public void setAnimateFile(File f) {
        if (animateFile != null) {
            FileWatcher.unwatch(animateFile, this);
        }
        animateFile = f;
        if (animateFile != null) {
            FileWatcher.watch(animateFile, this);
        }
        loadFile();
    }
    
    private void loadFile() {
        try {
            animateSet = new PipAnimateSet();
            animateSet.load(animateFile);
        } catch (Exception e) {
            animateSet = null;
        }
        updateUI();
    }
    
    public void updateUI() {
        animateList.refresh();
        if (animateListTable.getItemCount() > 0) {
            animateListTable.setSelection(0);
        }
        updatePreviewFrame();
    }
    
    private void updatePreviewFrame() {
        int index = animateListTable.getSelectionIndex();
        if (index == -1) {
            animateViewer.setInput(null);
        } else {
            PipAnimate animate = animateSet.getAnimate(index);
            animateViewer.setInput(animate);
            if (animateViewer.isPlaying()) {
                animateViewer.stop();
            }
            animateViewer.play();
        }
    }
    
    private void onEdit() {
        if (animateFile == null) {
            return;
        }
        try {
            IFileStore fileStore =  EFS.getLocalFileSystem().fromLocalFile(animateFile);
            IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileStore);
        } catch (Exception e) {
        }
    }
    
    public void fileModified(File f) {
        if (f.equals(animateFile)) {
            EditorApplication.getInstance().display.asyncExec(new Runnable() {
                public void run() {
                    loadFile();
                }
            });
        }
    }
    
    public void widgetDisposed(DisposeEvent e) {
        if (animateFile != null) {
            FileWatcher.unwatch(animateFile, this);
        }
    }
    
    public void setListVisible(boolean value) {
        GridData gd = (GridData)animateListTable.getLayoutData();
        gd.exclude = true;
        this.layout();
    }
    
    public void setEditEnable(boolean value) {
        GridData gd = (GridData)buttonEdit.getLayoutData();
        gd.exclude = true;
        this.layout();
    }
}
