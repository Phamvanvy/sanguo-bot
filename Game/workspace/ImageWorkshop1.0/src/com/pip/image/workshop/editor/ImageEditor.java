package com.pip.image.workshop.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.util.Utils;
import com.pipimage.data.ImageDescription;
import com.pipimage.data.TileInfo1;
import com.pipimage.data.TileInfo2;
import com.pipimage.image.JPEGMergeOption;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImagePalette;
import com.swtdesigner.ResourceManager;

public class ImageEditor extends EditorPart implements ImageViewerListener {
	private Combo comboMode;
	public static final int PROPERTY_CANUNDO = 1;
	public static final int PROPERTY_CANREDO = 2;

	class FrameViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int row = ((Integer)element).intValue();
			return "第" + (row + 1) + "帧";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return WorkshopPlugin.getDefault().getImageRegistry().get("image");
		}
	}
	
	class FrameViewContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Integer[] ret = new Integer[allImage.getImgCount()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		
		public void dispose() {}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	class PaletteViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int row = ((Integer)element).intValue();
			return "调色板" + (row + 1);
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return WorkshopPlugin.getDefault().getImageRegistry().get("palette");
		}
	}
	
	class PaletteViewContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Integer[] ret = new Integer[allImage.getImagePalettes().size()];
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
	
	private Table frameTable;
	private Table paletteTable;
	public static final String ID = "com.pip.image.workshop.editor.ImageEditor"; //$NON-NLS-1$
	
	private File imageFile;
	private boolean dirty = false;
	private PipImage allImage;
	private Image importImage;
	private String importImageFile;
	private TableViewer palettesViewer;
	private PaletteEditor paletteEditor;
	private TableViewer framesViewer;
	private ImageViewer frameViewer;
	private ImageViewer allViewer;
	private ImageViewer sourceViewer;
	
	private Action deletePaletteAction, duplicatePaletteAction, optimizePaletteAction;
	private Action duplicateFrameAction, hflipFrameAction, vflipFrameAction, deleteFrameAction;
	private Action moveUpAction, moveDownAction;
	public static FileDialog openFileDlg;
	public static DirectoryDialog dirDlg;
	private ToolItem splitItem, quickSplitItem;
	
	private StateManager stateMgr;
	private Composite composite_1;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);

		final Composite composite = new Composite(sashForm, SWT.NONE);
		composite.setLayout(new FillLayout());

		final SashForm sashForm_1 = new SashForm(composite, SWT.VERTICAL);

		allViewer = new ImageViewer(sashForm_1, SWT.NONE);
		allViewer.setImageViewerListener(this);

		sourceViewer = new ImageViewer(sashForm_1, SWT.NONE);
		sourceViewer.setImageViewerListener(this);
		sashForm_1.setWeights(new int[] {1, 1 });
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		composite_1 = new Composite(sashForm, SWT.NONE);
		composite_1.setLayout(new FillLayout());

		final SashForm sashForm_2 = new SashForm(composite_1, SWT.VERTICAL);

		palettesViewer = new TableViewer(sashForm_2, SWT.BORDER | SWT.FULL_SELECTION);
		palettesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				paletteSelectionChanged();
			}
		});
		palettesViewer.setLabelProvider(new PaletteViewLabelProvider());
		palettesViewer.setContentProvider(new PaletteViewContentProvider());
		palettesViewer.setInput(new Object());
		paletteTable = palettesViewer.getTable();

		final TableColumn paletteNameColumn = new TableColumn(paletteTable, SWT.NONE);
		paletteNameColumn.setWidth(200);
		paletteNameColumn.setText("Name");

		paletteEditor =  new PaletteEditor(sashForm_2, SWT.NONE);
		paletteEditor.setImageViewerListener(this);
		
		framesViewer = new TableViewer(sashForm_2, SWT.BORDER | SWT.FULL_SELECTION);
		framesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				int sel = frameTable.getSelectionIndex();
				if (sel != -1) {
					frameViewer.setInput(allImage.getImageDraw(sel));
				} else {
					frameViewer.setInput(null);
				}
				allViewer.setSelectedFrame(sel);
				frameViewer.redraw();
				if (frameTable.getMenu() != null) {
					frameTable.getMenu().dispose();
				}
				if (sel == -1) {
					frameTable.setMenu(null);
				} else {
					MenuManager mgr = new MenuManager();
					mgr.add(moveUpAction);
					mgr.add(moveDownAction);
					mgr.add(duplicateFrameAction);
					mgr.add(hflipFrameAction);
					mgr.add(vflipFrameAction);
					mgr.add(deleteFrameAction);
					Menu menu = mgr.createContextMenu(frameTable);
					frameTable.setMenu(menu);
				}
			}
		});
		framesViewer.setLabelProvider(new FrameViewLabelProvider());
		framesViewer.setContentProvider(new FrameViewContentProvider());
		framesViewer.setInput(new Object());
		frameTable = framesViewer.getTable();
		frameTable.setVisible(false);

		final TableColumn frameNameColumn = new TableColumn(frameTable, SWT.NONE);
		frameNameColumn.setWidth(200);
		frameNameColumn.setText("Name");

		frameViewer = new ImageViewer(sashForm_2, SWT.NONE);
		sashForm_2.setWeights(new int[] {3, 1, 1, 3 });
		sashForm.setWeights(new int[] {3, 1 });


		final ToolBar toolBar = new ToolBar(container, SWT.NONE);
		toolBar.setLayoutData(new GridData());

		final ToolItem loadFileItem = new ToolItem(toolBar, SWT.PUSH);
		loadFileItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onLoadImage();
			}
		});
		loadFileItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/open_large.gif"));
		loadFileItem.setText("载入图片");

		splitItem = new ToolItem(toolBar, SWT.PUSH);
		splitItem.setEnabled(false);
		splitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onSplit();
			}
		});
		splitItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/split_large.gif"));
		splitItem.setText("切分图片");

		quickSplitItem = new ToolItem(toolBar, SWT.PUSH);
		quickSplitItem.setEnabled(false);
		quickSplitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onQuickSplit(false);
			}
		});
		quickSplitItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/qsplit_large.gif"));
		quickSplitItem.setText("快速切分");

		final ToolItem addPaletteItem = new ToolItem(toolBar, SWT.PUSH);
		addPaletteItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onImportPalette();
			}
		});
		addPaletteItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/palette_large.gif"));
		addPaletteItem.setText("导入调色板");

		final ToolItem enlargeItem = new ToolItem(toolBar, SWT.PUSH);
		enlargeItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        allImage.enlarge();
		        frameViewer.redraw();
		        allViewer.redraw();
		        setDirty(true);
		    }
		});
		enlargeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/enlarge_large.gif"));
		enlargeItem.setText("放大一倍");

		allViewer.setInput(allImage);

		final Composite configComposite = new Composite(container, SWT.NONE);
		configComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		configComposite.setLayout(gridLayout_1);

		final Label label = new Label(configComposite, SWT.NONE);
		label.setText("模式：");

		comboMode = new Combo(configComposite, SWT.READ_ONLY);
		comboMode.setVisibleItemCount(20);
        comboMode.setItems(new String[] {"256色模式", "256色合并模式", "65536色模式", "真彩色模式", "JPEG合并模式"});
        if (allImage.isTrueColor()) {
        	if (allImage.getJPEGOption() == null) {
        		comboMode.select(3);
        	} else {
        		comboMode.select(4);
        	}
        } else if (allImage.isMergeMode()) {
		    comboMode.select(1);
		} else if (allImage.isSupportMoreColors()) {
	        comboMode.select(2);
		} else {
            comboMode.select(0);
		}
        comboMode.addModifyListener(new ModifyListener() {
		    public void modifyText(final ModifyEvent e) {
                switch (comboMode.getSelectionIndex()) {
                case 0:
                    allImage.setSupportColorOp(true);
                    allImage.setSupportMoreColors(false);
                    allImage.setMergeMode(false);
                    allImage.setTrueColor(false);
                    break;
                case 1:
                    allImage.setSupportColorOp(false);
                    allImage.setSupportMoreColors(false);
                    allImage.setMergeMode(true);
                    allImage.setTrueColor(false);
                    break;
                case 2:
                    allImage.setSupportColorOp(true);
                    allImage.setSupportMoreColors(true);
                    allImage.setMergeMode(false);
                    allImage.setTrueColor(false);
                    break;
                case 3:
                	allImage.setSupportColorOp(false);
                	allImage.setSupportMoreColors(false);
                    allImage.setMergeMode(false);
                    allImage.setTrueColor(true);
                    break;
                case 4:
                	allImage.setSupportColorOp(false);
                	allImage.setSupportMoreColors(false);
                    allImage.setMergeMode(false);
                    allImage.setTrueColor(true);
                    JPEGMergeOption option = JPEGMergeOptionDialog.choose(allImage.getJPEGOption());
                    if (option == null) {
                    	allImage.setJPEGOption(new JPEGMergeOption());
                    } else {
                    	allImage.setJPEGOption(option);
                    }
                    break;
                }
		        setDirty(true);
                paletteSelectionChanged();
		    }
		});
		comboMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Composite composite_2 = new Composite(configComposite, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		composite_2.setLayout(gridLayout_2);

		final Button buttonPreview = new Button(composite_2, SWT.NONE);
		buttonPreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buttonPreview.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        onPreviewTarget();
		    }
		});
		buttonPreview.setText("预览...");

		final Button buttonOptimize = new Button(composite_2, SWT.NONE);
		buttonOptimize.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        try {
		            onOptimize();
		        } catch (Exception e1) {
		            e1.printStackTrace();
		        }
		    }
		});
		buttonOptimize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buttonOptimize.setText("减色...");

		this.setPartName(this.getEditorInput().getName());
	}

	public void setFocus() {
		palettesViewer.getControl().setFocus();
	}

	public void doSave(IProgressMonitor monitor) {
		try {
		    switch (comboMode.getSelectionIndex()) {
		    case 0:
		        allImage.setSupportColorOp(true);
                allImage.setSupportMoreColors(false);
		        allImage.setMergeMode(false);
		        allImage.setTrueColor(false);
		        break;
            case 1:
                allImage.setSupportColorOp(false);
                allImage.setSupportMoreColors(false);
                allImage.setMergeMode(true);
                allImage.setTrueColor(false);
                break;
            case 2:
                allImage.setSupportColorOp(true);
                allImage.setSupportMoreColors(true);
                allImage.setMergeMode(false);
                allImage.setTrueColor(false);
                break;
            case 3:
                allImage.setSupportColorOp(false);
                allImage.setSupportMoreColors(false);
                allImage.setMergeMode(false);
                allImage.setTrueColor(true);
                break;
            case 4:
                allImage.setSupportColorOp(false);
                allImage.setSupportMoreColors(false);
                allImage.setMergeMode(false);
                allImage.setTrueColor(true);
                break;
		    }
			allImage.save(imageFile);
			setDirty(false);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			if (monitor != null) {
			    monitor.setCanceled(true);
			}
		}
	}

	public void doSaveAs() {
	    FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
	    dlg.setFilterExtensions(new String[] { "*.pip" });
	    dlg.setFilterNames(new String[] { "PIP图片文件(*.pip)" });
	    String f = dlg.open();
	    if (f != null) {
	        imageFile = new File(f);
            setPartName(imageFile.getName());
	        doSave(null);
	    }
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		createActions();
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		String filePath = Utils.urlToPath(url);
		imageFile = new File(filePath);
		try {
		    if (filePath.toLowerCase().endsWith(".ldf")) {
		        allImage = new LandformImage();
		    } else {
		        allImage = new PipImage();
		    }
			if (imageFile.length() > 0) {
				allImage.load(imageFile.getAbsolutePath());
			}
		} catch (Exception e) {
			MessageDialog.openError(site.getShell(), "错误", "文件格式错误。\n"+e);
		}
		stateMgr = new StateManager(10000000);
		saveState();
	}

	private void saveState() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			boolean value = allImage.isMergeMode();
			allImage.setMergeMode(false);
			allImage.save(new DataOutputStream(bos), true);
			allImage.setMergeMode(value);
			stateMgr.push(bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void restoreState(byte[] data) {
		try {
			allImage.load(new ByteArrayInputStream(data));
			palettesViewer.refresh();
			int sel = paletteTable.getSelectionIndex();
			if (sel >= allImage.getImagePalettes().size()) {
				paletteTable.setSelection(-1);
			}
			paletteSelectionChanged();
			framesViewer.refresh();
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
		return true;
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
		deletePaletteAction = new Action("删除调色板") {
			public void run() {
				onDeletePalette();
			}
		};
		duplicatePaletteAction = new Action("复制调色板") {
			public void run() {
				onDuplicatePalette();
			}
		};
		optimizePaletteAction = new Action("优化调色板") {
            public void run() {
                onOptimizePalette();
            }
        };
		duplicateFrameAction = new Action("复制") {
			public void run() {
				onDuplicateFrame();
			}
		};
		hflipFrameAction = new Action("水平翻转") {
			public void run() {
				onHflipFrame();
			}
		};
		vflipFrameAction = new Action("垂直翻转") {
			public void run() {
				onVflipFrame();
			}
		};
		deleteFrameAction = new Action("删除") {
			public void run() {
				onDeleteFrame();
			}
		};
		moveUpAction = new Action("上移") {
			public void run() {
				onMoveUp();
			}
		};
		moveDownAction = new Action("下移") {
			public void run() {
				onMoveDown();
			}
		};
	}
	
	public void dispose() {
		super.dispose();
		if (importImage != null) {
			importImage.dispose();
		}
	}
	
	private void onLoadImage() {
		if (openFileDlg == null) {
			openFileDlg = new FileDialog(getSite().getShell(), SWT.OPEN|SWT.MULTI);
		}
		openFileDlg.setFilterExtensions(new String[] { "*.png", "*.gif", "*.*" });
		openFileDlg.setFilterNames(new String[] { "PNG图片文件(*.png)", "GIF图片文件(*.gif)", "所有文件(*.*)" });
		String imgFile = openFileDlg.open();
		if (imgFile != null) {
			try {
				String names[] = openFileDlg.getFileNames();
				if(names.length>1){
					onMultiLoad(openFileDlg.getFilterPath(), names);
					return;
				}
				Image newImg = new Image(getSite().getShell().getDisplay(), imgFile);
				if (importImage != null) {
					importImage.dispose();
				}
				importImage = newImg;
				importImageFile = imgFile;
				sourceViewer.setInput(importImage);
				sourceViewer.redraw();
				splitItem.setEnabled(true);
				quickSplitItem.setEnabled(true);
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
		}
	}
	private void onMultiLoad(String path, String[] names){
		for(int i=0;i<names.length;i++){
			String imgFile = path+File.separator+names[i];
			Image newImg = new Image(getSite().getShell().getDisplay(), imgFile);
			if (importImage != null) {
				importImage.dispose();
			}
			importImage = newImg;
			importImageFile = imgFile;
			sourceViewer.setInput(importImage);
			onQuickSplit(true);
		}
		sourceViewer.redraw();
	}
	private void onSplit() {
		if (openFileDlg == null) {
			openFileDlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		}
		openFileDlg.setFilterExtensions(new String[] { "*.s", "*.*" });
		openFileDlg.setFilterNames(new String[] { "图片描述文件(*.s)", "所有文件(*.*)" });
		String sfile = openFileDlg.open();
		if (sfile != null) {
			try {
				ImageDescription id = new ImageDescription();
				id.load(new File(sfile));
				PipImage.initPalette(allImage, new File(importImageFile));
				if (id.type == ImageDescription.VERSION_1) {
					Object[] tiles = id.getTileList();
					for (int i = 0; i < tiles.length; i++) {
						TileInfo1 info = (TileInfo1)tiles[i];
						int x = importImage.getBounds().width / id.tileWidth;
						int y = info.imageID / x;
						x = info.imageID % x;
						int[][] rawData = ImageViewer.getImageData(importImage, new Rectangle(x * id.tileWidth, y * id.tileHeight, id.tileWidth, id.tileHeight));
						allImage.addFrame(rawData);
						if ((info.param & ImageDescription.T_HORIZONTAL) > 0) {
			            	allImage.getImageData(allImage.getImgCount() - 1).hflip();
			            }
			            if ((info.param & ImageDescription.T_VERTICAL) > 0) {
			            	allImage.getImageData(allImage.getImgCount() - 1).vflip();
			            }
						setDirty(true);
					}
				} else if (id.type == ImageDescription.VERSION_2) {
					Object[] tiles = id.getTileList();
					for (int i = 0; i < tiles.length; i++){
			            TileInfo2 info = (TileInfo2)tiles[i];
			            int[][] rawData = ImageViewer.getImageData(importImage, new Rectangle(info.x, info.y, info.width, info.height));
			            allImage.addFrame(rawData);
			            if ((info.param & ImageDescription.T_HORIZONTAL) > 0) {
			            	allImage.getImageData(allImage.getImgCount() - 1).hflip();
			            }
			            if ((info.param & ImageDescription.T_VERTICAL) > 0) {
			            	allImage.getImageData(allImage.getImgCount() - 1).vflip();
			            }
			            setDirty(true);
			        }
				}
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
			
			// Update the UI
			palettesViewer.refresh();
			framesViewer.refresh();
			allViewer.redraw();
			
			this.paletteSelectionChanged();
		}
	}


	private void onQuickSplit(boolean auto) {
		int row = 0;
		int col = 0;
		QuickSplitDialog dlg;
		if(auto){
			row = 1;
			col = 1;
		}else{
			dlg = new QuickSplitDialog(getSite().getShell());
			if(dlg.open() == QuickSplitDialog.OK) {
				row = dlg.getRow();
				col = dlg.getCol();
			}
		}
		if (row>0 && col>0){
			int imageWidth = importImage.getBounds().width;
			int imageHeight = importImage.getBounds().height;
			if ((imageWidth % col) != 0 || (imageHeight % row) != 0) {
				if (!MessageDialog.openConfirm(getSite().getShell(), "快速切分", "图片大小不能被指定的行列数整除，继续？")) {
					return;
				}
			}
			int tw = imageWidth / col;
			int th = imageHeight /row;
			PipImage.initPalette(allImage, new File(importImageFile));
			try {
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						int[][] rawData = ImageViewer.getImageData(importImage, new Rectangle(j * tw, i * th, tw, th));
			            allImage.addFrame(rawData);
			            setDirty(true);
					}
				}
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
			
			// Update the UI
			palettesViewer.refresh();
			framesViewer.refresh();
			allViewer.redraw();
			
			this.paletteSelectionChanged();
		}
	}
	
	private void onImportPalette() {
		if (openFileDlg == null) {
			openFileDlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		}
		openFileDlg.setFilterExtensions(new String[] { "*.act", "*.*" });
		openFileDlg.setFilterNames(new String[] { "调色板文件(*.act)", "所有文件(*.*)" });
		String paletteFile = openFileDlg.open();
		if (paletteFile != null) {
			try {
				PipImagePalette p = PipImage.readPalette(paletteFile);
				if (p == null) {
					throw new Exception("文件格式错误。");
				}
				if (allImage.getImagePalettes().size() > 0 && 
					p.getPalette().length != allImage.getImagePalettes().get(0).getPalette().length) {
					throw new Exception("无效的调色板，需要" + allImage.getImagePalettes().get(0).getPalette().length +
							"种颜色，实有" + p.getPalette().length + "种颜色。");
				}
				allImage.getImagePalettes().add(p);
				setDirty(true);
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
			
			// Update the UI
			palettesViewer.refresh();
			framesViewer.refresh();
			allViewer.redraw();
			
			this.paletteSelectionChanged();
		}
	}
	
	private void onDeletePalette() {
		int sel = paletteTable.getSelectionIndex();
		if (sel != -1) {
			if (allImage.getImagePalettes().size() == 1) {
				if (allImage.getImgCount() > 0) {
					if (!MessageDialog.openConfirm(getSite().getShell(), "确认", "删除最后一个调色板将导致所有帧被删除，是否继续？")) {
						return;
					}
					allImage.getImageDatas().clear();
					frameTable.setSelection(new int[0]);
				}
			}
			allImage.setPaletteIndex(0);
			allImage.getImagePalettes().remove(sel);
			setDirty(true);

			// Update the UI
			frameViewer.setInput(null);
			palettesViewer.refresh();
			framesViewer.refresh();
			frameViewer.redraw();
			allViewer.redraw();
			
			this.paletteSelectionChanged();
		}
	}

	private void onDuplicatePalette() {
		int sel = paletteTable.getSelectionIndex();
		if (sel != -1) {
			PipImagePalette pal = allImage.getImagePalettes().get(sel).duplicate();
			allImage.getImagePalettes().add(pal);
			setDirty(true);

			// Update the UI
			frameViewer.setInput(null);
			palettesViewer.refresh();
			framesViewer.refresh();
			frameViewer.redraw();
			allViewer.redraw();
			
			paletteTable.setSelection(allImage.getImagePalettes().size() - 1);
			this.paletteSelectionChanged();
		}
	}
	
	private void onOptimizePalette() {
        int sel = paletteTable.getSelectionIndex();
        if (sel != -1) {
            int[] unused = allImage.getNonUsedColors();
            if (unused.length == 0) {
                MessageDialog.openInformation(getSite().getShell(), "信息", "没有未使用的颜色。");
            } else {
                String msg = "共有" + unused.length + "种颜色没有被使用，是否删除这些颜色？";
                if (allImage.getImagePalettes().size() > 1) {
                    msg += "注意：此操作将影响所有调色板！！！";
                }
                if (MessageDialog.openConfirm(getSite().getShell(), "优化", msg)) {
                    allImage.deleteColors(unused);
                    setDirty(true);
    
                    // Update the UI
                    frameViewer.redraw();
                    allViewer.redraw();
                    paletteEditor.redraw();
                }
            }
        }
	}

	private void onDuplicateFrame() {
		int sel = frameTable.getSelectionIndex();
		if (sel != -1) {
			PipImageData data = allImage.getImageData(sel);
			allImage.getImageDatas().add(data.duplicate());
			setDirty(true);
			
			// Update the UI
			palettesViewer.refresh();
			framesViewer.refresh();
			allViewer.redraw();
		}
	}
	
	private void onHflipFrame() {
		int sel = frameTable.getSelectionIndex();
		if (sel != -1) {
			allImage.getImageData(sel).hflip();
			setDirty(true);
			frameViewer.setInput(allImage.getImageDraw(sel));
			frameViewer.redraw();
			allViewer.redraw();
		}
	}
	
	private void onVflipFrame() {
		int sel = frameTable.getSelectionIndex();
		if (sel != -1) {
			allImage.getImageData(sel).vflip();
			setDirty(true);
			frameViewer.setInput(allImage.getImageDraw(sel));
			frameViewer.redraw();
			allViewer.redraw();
		}
	}
	
	private void onDeleteFrame() {
		int sel = frameTable.getSelectionIndex();
		if (sel != -1) {
			allImage.getImageDatas().remove(sel);
            if (allImage instanceof LandformImage) {
                ((LandformImage)allImage).onFrameRemoved(sel);
            }
			setDirty(true);
			
			// Update the UI
			framesViewer.refresh();
			frameViewer.setInput(null);
			frameViewer.redraw();
			allViewer.redraw();			
		}
	}

	private void onMoveUp() {
		int sel = frameTable.getSelectionIndex();
		if (sel != -1 && sel > 0) {
			PipImageData data1 = allImage.getImageData(sel);
			allImage.getImageDatas().set(sel, allImage.getImageData(sel - 1));
			allImage.getImageDatas().set(sel - 1, data1);
            if (allImage instanceof LandformImage) {
                ((LandformImage)allImage).onFrameSwap(sel, sel - 1);
            }
			frameTable.setSelection(sel - 1);
			setDirty(true);
			
			// Update the UI
			framesViewer.refresh();
			frameViewer.redraw();
			allViewer.redraw();			
		}
	}

	private void onMoveDown() {
		int sel = frameTable.getSelectionIndex();
		if (sel != -1 && sel < allImage.getImgCount() - 1) {
			PipImageData data1 = allImage.getImageData(sel);
			allImage.getImageDatas().set(sel, allImage.getImageData(sel + 1));
			allImage.getImageDatas().set(sel + 1, data1);
            if (allImage instanceof LandformImage) {
                ((LandformImage)allImage).onFrameSwap(sel, sel + 1);
            }
			frameTable.setSelection(sel + 1);
			setDirty(true);
			
			// Update the UI
			framesViewer.refresh();
			frameViewer.redraw();
			allViewer.redraw();
		}
	}
	
	private void onPreviewTarget() {
	    try {
	        PipImage mergeImg = allImage.generateMergedImage();
	        int targetSize = 0;
	        for (PipImageData d : mergeImg.getImageDatas()) {
	            targetSize += d.getWidth() * d.getHeight();
	        }
	        int rawSize = 0;
	        for (PipImageData d : allImage.getImageDatas()) {
	            rawSize += d.getWidth() * d.getHeight();
	        }
	        if (rawSize > 0) {
	            double wasteRate = (double)(targetSize - rawSize) / (double)rawSize;
	            MessageDialog.openInformation(getSite().getShell(), "消息", "浪费率：" + (wasteRate * 100) + "%");
	        }
            sourceViewer.setInput(mergeImg);
            sourceViewer.redraw();
	    } catch (Exception e) {
	        MessageDialog.openError(getSite().getShell(), "错误", e.toString());
	    }
	}
	
	private void onOptimize() {
	    if (allImage.getPaletteCount() != 1) {
	        MessageDialog.openError(getSite().getShell(), "错误", "只有具有1个调色板的图片能执行缩色操作。");
	        return;
	    }
	    int count = allImage.getImagePalettes().get(0).getPalette().length;
	    IntegerInputDialog dlg = new IntegerInputDialog(getSite().getShell(), 1, 65536, 
	            count, "缩色", "目标颜色数");
        if (dlg.open() == IntegerInputDialog.OK /*&& count > dlg.getValue()*/) {
            // 优化图片
            allImage.optimizeColor(dlg.getValue());

            palettesViewer.refresh();
            allViewer.refresh();
            paletteEditor.redraw();
            setDirty(true);
        }
	}

	public void areaSelected(Object source) {
		// Retrieve selected image data
		int[][] rawData = ImageViewer.getImageData(importImage, sourceViewer.getSelectedArea());
		PipImage.initPalette(allImage, new File(importImageFile));
		
		// Add the frame
		try {
			allImage.addFrame(rawData);
			setDirty(true);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
		
		// Update the UI
		palettesViewer.refresh();
		framesViewer.refresh();
		allViewer.redraw();
	}
	
	public void frameDoubleClicked(Object source, int frame) {
	}

	public void frameSelectionChanged(Object source, int newFrame) {
		frameTable.setSelection(newFrame);
		if (newFrame != -1) {
			frameViewer.setInput(allImage.getImageDraw(newFrame));
		} else {
			frameViewer.setInput(null);
		}
		frameViewer.redraw();
	}
	
	public void contentChanged(Object source) {
		if (source == this.allViewer) {
			this.setDirty(true);
			framesViewer.refresh();
			frameViewer.redraw();
		} else if (source == this.paletteEditor) {
			this.setDirty(true);
			allViewer.redraw();
			frameViewer.redraw();
		}
	}
	
	private void paletteSelectionChanged() {
		int psel = paletteTable.getSelectionIndex();
		if (psel != -1) {
			allImage.setPaletteIndex(psel);
			paletteEditor.setInput(allImage.getImagePalettes().get(psel));
			paletteEditor.redraw();
		} else {
			allImage.setPaletteIndex(0);
			paletteEditor.setInput(null);
			paletteEditor.redraw();
		}
		allViewer.redraw();
		
		int sel = frameTable.getSelectionIndex();
		if (sel != -1) {
			frameViewer.setInput(allImage.getImageDraw(sel));
		} else {
			frameViewer.setInput(null);
		}
		frameViewer.redraw();

		if (paletteTable.getMenu() != null) {
			paletteTable.getMenu().dispose();
		}
		if (psel == -1) {
			paletteTable.setMenu(null);
		} else {
			MenuManager mgr = new MenuManager();
			mgr.add(deletePaletteAction);
			mgr.add(duplicatePaletteAction);
			mgr.add(optimizePaletteAction);
			Menu menu = mgr.createContextMenu(paletteTable);
			paletteTable.setMenu(menu);
		}
	}

	public boolean canUndo() {
		return stateMgr.canUndo();
	}
	
	public boolean canRedo() {
	    return stateMgr.canRedo();
	}
	
	/**
	 * 选择目录。
	 */
	public static String chooseDir(Shell shell, String message) {
		if (dirDlg == null) {
			dirDlg = new DirectoryDialog(shell, SWT.OPEN);
		}
		dirDlg.setMessage(message);
		return dirDlg.open();
	}
}
