package com.pip.mapeditor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import com.pip.image.workshop.WorkshopPlugin;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.ImageViewerListener;
import com.pip.mapeditor.data.CellMap;
import com.pip.util.Utils;
import com.swtdesigner.ResourceManager;

public class CellMapEditor extends EditorPart implements ImageViewerListener {
    public static final int PROPERTY_CANUNDO = 1;
    public static final int PROPERTY_CANREDO = 2;

    public static final String ID = "com.pip.mapeditor.CellMapEditor"; //$NON-NLS-1$

    private File sourceFile;
    private boolean dirty = false;
    private CellMap mapFile;
    
    private byte[][] undoBuffer;
    private int undoCurrent, undoLast;
    
    private CellMapViewer mapViewer;
    
    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        final Composite viewerContainer = new Composite(container, SWT.NONE);
        viewerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewerContainer.setLayout(new FillLayout());

        final ToolBar toolBar = new ToolBar(container, SWT.NONE);

        final ToolItem importToolItem = new ToolItem(toolBar, SWT.PUSH);
        importToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onImportImage();
            }
        });
        importToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/merge.gif"));
        importToolItem.setText("导入图片");

        final ToolItem exportPathToolItem = new ToolItem(toolBar, SWT.PUSH);
        exportPathToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onExportPath();
            }
        });
        exportPathToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/qsplit_large.gif"));
        exportPathToolItem.setText("导出路径");
        
        mapViewer = new CellMapViewer(viewerContainer, SWT.NONE);
        mapViewer.setImageViewerListener(this);
        mapViewer.setInput(mapFile);
    }

    @Override
    public void setFocus() {
        // Set the focus
        mapViewer.setFocus();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            mapFile.save(sourceFile);
            setDirty(false);
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            monitor.setCanceled(true);
        }
    }

    @Override
    public void doSaveAs() {
        // Do the Save As operation
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
        URI url = finput.getURI();
        String filePath = Utils.urlToPath(url);
        sourceFile = new File(filePath);
        try {
            mapFile = new CellMap(1, 1, 1);
            mapFile.load(sourceFile);
        } catch (Exception e) {
            MessageDialog.openError(site.getShell(), "错误", "文件格式错误。");
        }
        undoBuffer = new byte[1000][];
        undoCurrent = undoLast = -1;
        saveState();
    }

    private boolean saveState() {
        try {
            byte[] newData = mapFile.save();
            if (undoCurrent >= undoBuffer.length - 2) {
                byte[][] newBuf = new byte[undoBuffer.length + 1000][];
                System.arraycopy(undoBuffer, 0, newBuf, 0, undoBuffer.length);
            }
            if (undoCurrent != -1 && Arrays.equals(newData, undoBuffer[undoCurrent])) {
                return false;
            }
            undoBuffer[++undoCurrent] = newData;
            undoLast = undoCurrent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    private void restoreState(byte[] data) {
        try {
            mapFile.load(data);
            mapViewer.redraw();
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
        restoreState(undoBuffer[--undoCurrent]);
        firePropertyChange(PROPERTY_CANUNDO);
        firePropertyChange(PROPERTY_CANREDO);
    }
    
    public void redo() {
        if (!canRedo()) {
            return;
        }
        restoreState(undoBuffer[++undoCurrent]);
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

    public boolean canUndo() {
        return undoCurrent > 0;
    }
    
    public boolean canRedo() {
        return undoCurrent < undoLast;
    }
    
    public void areaSelected(Object source) {}
    public void frameSelectionChanged(Object source, int newFrame) {}
    public void frameDoubleClicked(Object source, int frame) {}
    
    public void contentChanged(Object source) {
        if (source == mapViewer) {
            setDirty(true);
        }
    }
    
    private void onImportImage() {
        FileDialog openFileDlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        openFileDlg.setFilterExtensions(new String[] { "*.png", "*.gif", "*.*" });
        openFileDlg.setFilterNames(new String[] { "PNG图片文件(*.png)", "GIF图片文件(*.gif)", "所有文件(*.*)" });
        String imgFile = openFileDlg.open();
        if (imgFile != null) {
            try {
                Image newImg = new Image(getSite().getShell().getDisplay(), imgFile);
                int[][] imgData = ImageViewer.getImageData(newImg, newImg.getBounds());
                int mw = mapFile.width < imgData[0].length ? mapFile.width : imgData[0].length;
                int mh = mapFile.height < imgData.length ? mapFile.height : imgData.length;
                for (int y = 0; y < mh; y++) {
                    for (int x = 0; x < mw; x++) {
                        if (imgData[y][x] == 0) {
                            mapFile.data[y][x] = 0;
                        } else {
                            mapFile.data[y][x] = 1;
                        }
                    }
                }
                setDirty(true);
                mapViewer.redraw();
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
    }

    private List<Point> findAdjacentPoints(int x, int y) {
        List<Point> ret = new ArrayList<Point>();
        if (x > 0 && mapFile.data[y][x - 1] != 0) {
            ret.add(new Point(x - 1, y));
        }
        if (x < mapFile.width - 1 && mapFile.data[y][x + 1] != 0) {
            ret.add(new Point(x + 1, y));
        }
        if (y > 0 && mapFile.data[y - 1][x] != 0) {
            ret.add(new Point(x, y - 1));
        }
        if (y < mapFile.height - 1 && mapFile.data[y + 1][x] != 0) {
            ret.add(new Point(x, y + 1));
        }
        if (x > 0 && y > 0 && mapFile.data[y - 1][x - 1] != 0) {
            ret.add(new Point(x - 1, y - 1));
        }
        if (x < mapFile.width - 1 && y > 0 && mapFile.data[y - 1][x + 1] != 0) {
            ret.add(new Point(x + 1, y - 1));
        }
        if (x > 0 && y < mapFile.height - 1 && mapFile.data[y + 1][x - 1] != 0) {
            ret.add(new Point(x - 1, y + 1));
        }
        if (x < mapFile.width - 1 && y < mapFile.height - 1 && mapFile.data[y + 1][x + 1] != 0) {
            ret.add(new Point(x + 1, y + 1));
        }
        return ret;
    }
    
    private List<Point> findStartPoint() {
        List<Point> ret = new ArrayList<Point>();
        for (int y = 0; y < mapFile.height; y++) {
            for (int x = 0; x < mapFile.width; x++) {
                if (mapFile.data[y][x] != 0 && findAdjacentPoints(x, y).size() == 1) {
                    ret.add(new Point(x, y));
                    
                }
            }
        }
        return ret;
    }
    
    private List<Point> findPath(Point start) {
        List<Point> ret = new ArrayList<Point>();
        Map<Point, Point> searchTable = new HashMap<Point, Point>();
        Point current = start;
        ret.add(current);
        searchTable.put(current, current);
        while (current != null) {
            List<Point> adjs = this.findAdjacentPoints(current.x, current.y);
            boolean foundNext = false;
            for (int i = 0; i < adjs.size(); i++) {
                if (searchTable.containsKey(adjs.get(i))) {
                    continue;
                } else {
                    foundNext = true;
                    current = adjs.get(i);
                    ret.add(current);
                    searchTable.put(current, current);
                    break;
                }
            }
            if (!foundNext) {
                break;
            }
        }
        return ret;
    }
    
    private void onExportPath() {
        List<Point> starts = findStartPoint();
        if (starts.size() == 0) {
            MessageDialog.openInformation(getSite().getShell(), "错误", "没有找到起点。");
        }
        Point startPoint;
        if (starts.size() > 1) {
            ChooseStartPointDialog dlg = new ChooseStartPointDialog(getSite().getShell(), starts);
            if (dlg.open() != ChooseStartPointDialog.OK) {
                return;
            } else {
                startPoint = dlg.getSelected();
            }
        } else {
            startPoint = starts.get(0);
        }
        List<Point> path = findPath(startPoint);
        if (MessageDialog.openQuestion(getSite().getShell(), "找到路径", "路径总长" + path.size() + "个点，是否保存？")) {
            FileDialog saveFileDlg = new FileDialog(getSite().getShell(), SWT.SAVE);
            saveFileDlg.setFilterExtensions(new String[] { "*.pth", "*.*" });
            saveFileDlg.setFilterNames(new String[] { "路径文件(*.pth)", "所有文件(*.*)" });
            String imgFile = saveFileDlg.open();
            if (imgFile != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(imgFile);
                    DataOutputStream dos = new DataOutputStream(fos);
                    dos.writeShort(path.size());
                    for (Point p : path) {
                        dos.writeShort(p.x);
                        dos.writeShort(p.y);
                    }
                    dos.close();
                } catch (Exception e) {
                    MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                }
            }
        }
    }
}
