package com.pip.image.workshop.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import com.pip.image.workshop.AutoBody;
import com.pip.image.workshop.GenericChooseDialog;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AnimateFrameEditor.FramePieceSelectedListener;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.propertysheet.StringMapping;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.Utils;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.utils.ImageUtil;
import com.swtdesigner.ResourceManager;

public class AnimateEditor extends EditorPart implements ImageViewerListener, IFileModificationListener {
	public static final int PROPERTY_CANUNDO = 1;
	public static final int PROPERTY_CANREDO = 2;
	
	class FramesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Integer[] ret = new Integer[animateSet.getFrameCount()];
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
	class FramesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int index = ((Integer)element).intValue();
			if (columnIndex == 0) {
                return index + ". " + animateSet.getFrame(index).getName();
            } else if (columnIndex == 1) {
                return String.valueOf(animateSet.getFrame(index).getPieceCount());
            } else {
            	return "";
            }
		}
		public Image getColumnImage(Object element, int columnIndex) {
		    if(columnIndex == 0){
		        return WorkshopPlugin.getDefault().getImageRegistry().get("image");
		    } else if (columnIndex == 2) {
		    	if (filterImageID != -1) {
		    		int index = ((Integer)element).intValue();
		    		PipAnimateFrame frame = animateSet.getFrame(index);
		    		if (frame.containsPiece(filterImageID, filterFrameID)) {
		    			return WorkshopPlugin.getDefault().getImageRegistry().get("used");
		    		}
		    	}
		    }
	        return null;
		}
	}
	class AnimatesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Integer[] ret = new Integer[animateSet.getAnimateCount()];
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
	class AnimatesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int index = ((Integer)element).intValue();
			return index + ". " + animateSet.getAnimate(index).getName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
	        return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
		}
	}
	class AnimateFramesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			int sel = animatesTable.getSelectionIndex();
			if (sel == -1) {
				return new Object[0];
			}
			PipAnimate animate = animateSet.getAnimate(sel);
			Integer[] ret = new Integer[animate.getFrameCount()];
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
	class AnimateFramesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int row = ((Integer)element).intValue();
            int sel = animatesTable.getSelectionIndex();
            if (sel == -1) {
                return "";
            }
            PipAnimate animate = animateSet.getAnimate(sel);
			if (columnIndex == 0) {
				return (row + 1) + ". " + animate.getFrame(row).realize().getName();
			} else {
				return String.valueOf(animate.getFrame(row).getDelay());
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
	class ImagesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			int count = animateSet.getFileCount();
			Object[] ret = new Object[count];
			for (int i = 0; i < count; i++) {
				ret[i] = animateSet.getFileName(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ImagesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return WorkshopPlugin.getDefault().getImageRegistry().get("image");
		}
	}
	
	public static final String ID = "com.pip.image.workshop.editor.AnimateEditor"; //$NON-NLS-1$

	private ImageViewer imageViewer;
	private AnimateViewer animateViewer;
	private AnimateFrameEditor animateFrameEditor;
	private AnimateFrameSelector frameSelector;
	private TableViewer animatesViewer;
	private CheckboxTableViewer animateFramesViewer; 
	private TableViewer imagesViewer, framesViewer;
	private Table imagesTable;
	private Table animateFramesTable;
	private Table animatesTable;
	private Table framesTable;
	private ToolItem playItem, stopItem;
	private Action newAnimateAction, delAnimateAction, moveUpAnimateAction, moveDownAnimateAction, dupAnimateAction1, dupAnimateAction2, flipAnimateAction, dupAllAnimateAction;
	private Action newFrameAction, dupFrameAction, delFrameAction, hflipFrameAction, vflipFrameAction, moveUpFrameAction, moveDownFrameAction, checkFrameRefAction, cleanPiece, cleanFrameAction;
	private Action delAnimateFrameAction, moveUpAnimateFrameAction, moveDownAnimateFrameAction;
	private Action createSequenceAction, createFramesAction, fillFramesAction, renameFramesAction;
	private Action newImageAction, editImageAction, replaceImageAction, delImageAction, renameImageAction, upImageAction, downImageAction;
	private CTabItem tabItem_1;
	private Display display;
	private AnimateEdgeEditor edgeEditor;
	private AnimateHeadEditor headEditor;
	private AnimateActionEditor actionEditor;
	private AnimateMirrorFrameEditor mirrorEditor;
	
	private File animateFile;
	private boolean dirty = false;
	private PipAnimateSet animateSet;
	private PipAnimateSet refAnimateSet;
	private PipAnimateSet refImageSet;
	private boolean isAutoGenerated = false;
	
	private StateManager stateMgr;
	private FramePiecesView piecesView;
	/**
	 * 是否是本界面进行了保存;由于cts变化后会重新加载,自身的保存也会引起重新加载,靠此变量来区分,
	 * 是自己保存的,则不重新加载
	 */
	private boolean selfSave;
	
	// 当前用来检查使用情况的帧的图片ID和帧号
	private int filterImageID = -1;
	private int filterFrameID = -1;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
	    display = this.getSite().getShell().getDisplay();
	    
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final CTabFolder tabFolder = new CTabFolder(container, SWT.BOTTOM);
		tabFolder.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		        if (e.item == tabItem_1) {
		            frameSelector.refresh();
		        }
		    }

		    public void widgetDefaultSelected(SelectionEvent e) {
		    }
		});

		final CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("帧");
		tabItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/image.gif"));

		final Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		composite_1.setLayout(new FillLayout());
		tabItem.setControl(composite_1);

		final SashForm sashForm_4 = new SashForm(composite_1, SWT.NONE);

		final SashForm sashForm_3 = new SashForm(sashForm_4, SWT.VERTICAL);

		animateFrameEditor = new AnimateFrameEditor(sashForm_3, SWT.NONE);
		animateFrameEditor.setImageViewerListener(this);
		animateFrameEditor.addPieceSelectedListener(new FramePieceSelectedListener(){
			public void handleEvent(Event evt) {
				framePieceSelected(evt.index);
			}
		});
		animateFrameEditor.setOwner(this);

		imageViewer = new ImageViewer(sashForm_3, SWT.NONE);
		imageViewer.setFlatMode(true);
		imageViewer.setImageViewerListener(this);
		sashForm_3.setWeights(new int[] {1, 1 });

		final Composite composite_3 = new Composite(sashForm_4, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite_3.setLayout(gridLayout);

		createRightPart(composite_3);
		
		final ToolBar toolBar_1 = new ToolBar(composite_3, SWT.NONE);
		toolBar_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final ToolItem optimizeItem = new ToolItem(toolBar_1, SWT.PUSH);
		optimizeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onOptimizeImage();
			}
		});
		optimizeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mainGroup.gif"));
		optimizeItem.setText("优化");

		final ToolItem addImageItem = new ToolItem(toolBar_1, SWT.PUSH);
		addImageItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onLoadFile();
			}
		});
		addImageItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/folder.gif"));
		addImageItem.setText("载入");

        final ToolItem refImageItem = new ToolItem(toolBar_1, SWT.PUSH);
        refImageItem.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		chooseRefImage();
        	}
        });
        refImageItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/tiles.gif"));
        refImageItem.setText("背景");
        final ToolItem mergeItem = new ToolItem(toolBar_1, SWT.PUSH);
        mergeItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onMergeAnimateSet();
            }
        });
        mergeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/items.gif"));
        mergeItem.setText("合并");

		final ToolItem enlargeItem = new ToolItem(toolBar_1, SWT.PUSH);
		enlargeItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        onEnlargeAnimateSet(true);
		    }
		});
		enlargeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/zoomin.gif"));
		enlargeItem.setText("放大");

		final ToolItem enlarge2Item = new ToolItem(toolBar_1, SWT.PUSH);
		enlarge2Item.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        onEnlargeAnimateSet(false);
		    }
		});
		enlarge2Item.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/zoomin.gif"));
		enlarge2Item.setText("放大动画");
		sashForm_4.setWeights(new int[] { 2,1 });

		final ToolItem smallItem = new ToolItem(toolBar_1, SWT.PUSH);
		smallItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		    	onSamllAnimateSet(true);
		    }
		});
		smallItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/zoomout.gif"));
		smallItem.setText("缩小");

		final ToolItem replacePieceItem = new ToolItem(toolBar_1, SWT.PUSH);
		replacePieceItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onReplacePiece();
			}
		});
		replacePieceItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/center.png"));
		replacePieceItem.setText("替换图块");
		
		tabItem_1 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_1.setText("动画");
		tabItem_1.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/animate.gif"));

		final CTabItem tabItem_2 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_2.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/map.gif"));
		tabItem_2.setText("轮廓");

		final CTabItem tabItem_3 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_3.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/character.gif"));
		tabItem_3.setText("头像");

		final CTabItem tabItem_4 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_4.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/ComNPC.png"));
		tabItem_4.setText("动作定义");

		final CTabItem tabItem_5 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_5.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/enlarge.gif"));
		tabItem_5.setText("对称设置");

		final Composite composite_7 = new Composite(tabFolder, SWT.NONE);
		composite_7.setLayout(new FillLayout());
		tabItem_5.setControl(composite_7);
		
		mirrorEditor = new AnimateMirrorFrameEditor(composite_7, SWT.NONE, this, animateSet);

		final Composite composite_6 = new Composite(tabFolder, SWT.NONE);
		composite_6.setLayout(new FillLayout());
		tabItem_4.setControl(composite_6);

		actionEditor = new AnimateActionEditor(composite_6, SWT.NONE, this, animateSet);

		final Composite composite_5 = new Composite(tabFolder, SWT.NONE);
		composite_5.setLayout(new FillLayout());
		tabItem_3.setControl(composite_5);
		
		headEditor = new AnimateHeadEditor(composite_5, SWT.NONE, this, animateSet);

		final Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		composite_4.setLayout(new FillLayout());
		tabItem_2.setControl(composite_4);
		
		edgeEditor = new AnimateEdgeEditor(composite_4, SWT.NONE, this, animateSet);

		final Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		composite_2.setLayout(new FillLayout());
		tabItem_1.setControl(composite_2);

		final SashForm sashForm_2 = new SashForm(composite_2, SWT.NONE);

		final SashForm sashForm = new SashForm(sashForm_2, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);

		animateViewer = new AnimateViewer(sashForm, SWT.NONE);
		animateViewer.setImageViewerListener(this);

		frameSelector = new AnimateFrameSelector(sashForm, SWT.NONE);
		frameSelector.setImageViewerListener(this);
		frameSelector.setInput(animateSet);
		sashForm.setWeights(new int[] {1, 1 });

		final Composite composite = new Composite(sashForm_2, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.marginWidth = 0;
		gridLayout_1.marginHeight = 0;
		composite.setLayout(gridLayout_1);

		final SashForm sashForm_1 = new SashForm(composite, SWT.NONE);
		sashForm_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm_1.setOrientation(SWT.VERTICAL);

		animatesViewer = new TableViewer(sashForm_1, SWT.BORDER | SWT.FULL_SELECTION);
		animatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				animateSelectionChanged();
			}
		});
		animatesViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				animateDoubleClicked();
			}
		});
		animatesViewer.setContentProvider(new AnimatesContentProvider());
		animatesViewer.setLabelProvider(new AnimatesLabelProvider());
		animatesTable = animatesViewer.getTable();
		animatesTable.setBounds(31, 10, 36, 73);
		animatesTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if ((event.stateMask & SWT.SHIFT) != 0) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        onMoveUpAnimate();
                        event.doit = false;
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        onMoveDownAnimate();
                        event.doit = false;
                    }
                }
                if (event.keyCode == SWT.DEL) {
                    onDelAnimate();
                }
            }
        });

		final TableColumn animateNameColumn = new TableColumn(animatesTable, SWT.NONE);
		animateNameColumn.setWidth(200);
		animateNameColumn.setText("动画名称");
		animatesViewer.setInput(new Object());

		final ToolBar toolBar = new ToolBar(composite, SWT.NONE);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		playItem = new ToolItem(toolBar, SWT.PUSH);
		playItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onPlay();
			}
		});
		playItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/play_large.gif"));
		playItem.setText("播放");
		playItem.setEnabled(false);

		stopItem = new ToolItem(toolBar, SWT.PUSH);
		stopItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onStop();
			}
		});
		stopItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/stop_large.gif"));
		stopItem.setText("停止");
		sashForm_2.setWeights(new int[] {3, 1 });
		stopItem.setEnabled(false);

		final ToolItem newItemToolItem = new ToolItem(toolBar, SWT.PUSH);
		newItemToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/open_large.gif"));
		newItemToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				chooseRefAnimateSet();
			}
		});
		newItemToolItem.setText("参考动画");

		animateFramesViewer = CheckboxTableViewer.newCheckList(sashForm_1, SWT.BORDER | SWT.FULL_SELECTION);
		animateFramesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				animateFrameSelectionChanged();
			}
		});
		animateFramesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				animateFrameSelectionChanged();
			}
		});
		animateFramesViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				animateFrameDoubleClicked();
			}
		});
		animateFramesViewer.setLabelProvider(new AnimateFramesLabelProvider());
		animateFramesViewer.setContentProvider(new AnimateFramesContentProvider());
		animateFramesTable = animateFramesViewer.getTable();
		animateFramesTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if ((event.stateMask & SWT.SHIFT) != 0) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        onMoveUpAnimateFrame();
                        event.doit = false;
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        onMoveDownAnimateFrame();
                        event.doit = false;
                    } 
                }
                if (event.keyCode == SWT.DEL) {
                    onDelAnimateFrame();
                } else if (event.keyCode == SWT.ARROW_LEFT) {
                    onDecreaseFrameDelay();
                    event.doit = false;
                } else if (event.keyCode == SWT.ARROW_RIGHT) {
                    onIncreaseFrameDelay();
                    event.doit = false;
                } else if (event.keyCode >= '0' && event.keyCode <= '9') {
                    onSetFrameDelay(event.keyCode - '0');
                    event.doit = false;
                }
            }
        });

		final TableColumn frameIndexColumn = new TableColumn(animateFramesTable, SWT.NONE);
		frameIndexColumn.setWidth(170);

		final TableColumn frameDelayColumn = new TableColumn(animateFramesTable, SWT.NONE);
		frameDelayColumn.setWidth(30);
		animateFramesViewer.setInput(new Object());
		sashForm_1.setWeights(new int[] {1, 1 });

		animateSelectionChanged();
		animateFrameSelectionChanged();
		imageSelectionChanged();
		frameSelectionChanged();

		this.setPartName(this.getEditorInput().getName());
		tabFolder.setSelection(0);
		
		this.setPartProperty("", "");
		imageViewer.currentAnimate = animateSet;
	}

	protected void framePieceSelected(int idx) {
		if (idx == -1) {
			return;
		}
		PipAnimateFrame frame = animateFrameEditor.getFrame();
		int imgId = frame.getPiece(idx).getImageID();
		int pieceId = frame.getPiece(idx).getFrame();
		imagesTable.select(imgId);
		imageSelectionChanged();
		imageViewer.setSelectedFrame(pieceId);
		updateUsage(imgId, pieceId);
	}

	private void createRightPart(Composite composite_3) {
		final SashForm sashForm_5 = new SashForm(composite_3, SWT.VERTICAL);
		sashForm_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createFrameTable(sashForm_5);
		createFramePieceTable(sashForm_5);
		createImageTable(sashForm_5);
		
		sashForm_5.setWeights(new int[] {3, 3, 1 });
		
	}

	private void createFramePieceTable(Composite parent) {
		piecesView = new FramePiecesView(parent, SWT.FILL);
		piecesView.setFrameEditor(this.animateFrameEditor);
	}

	private void createImageTable(Composite sashForm_5) {
		imagesViewer = new TableViewer(sashForm_5, SWT.BORDER | SWT.FULL_SELECTION);
		imagesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				imageSelectionChanged();
			}
		});
		imagesViewer.setContentProvider(new ImagesContentProvider());
		imagesViewer.setLabelProvider(new ImagesLabelProvider());
		imagesTable = imagesViewer.getTable();
		imagesTable.setBounds(37, 98, 91, 73);

		final TableColumn imageNameColumn = new TableColumn(imagesTable, SWT.NONE);
		imageNameColumn.setWidth(200);
		imagesViewer.setInput(new Object());
		
	}

	private void createFrameTable(Composite sashForm_5) {
		framesViewer = new TableViewer(sashForm_5, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		framesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				frameSelectionChanged();
			}
		});
		framesViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				frameDoubleClicked();
			}
		});
		framesTable = framesViewer.getTable();
		framesTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if ((event.stateMask & SWT.SHIFT) != 0) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        onMoveUpFrame();
                        event.doit = false;
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        onMoveDownFrame();
                        event.doit = false;
                    }
                }
                if (event.keyCode == SWT.DEL) {
                    onDelFrame();
                }
            }
        });

		final TableColumn frameNameColumn = new TableColumn(framesTable, SWT.NONE);
		frameNameColumn.setWidth(170);
		final TableColumn framePiecesColumn = new TableColumn(framesTable, SWT.NONE);
		framePiecesColumn.setWidth(40);
		final TableColumn frameUsageColumn = new TableColumn(framesTable, SWT.NONE);
		frameUsageColumn.setWidth(40);
		framesViewer.setContentProvider(new FramesContentProvider());
		framesViewer.setLabelProvider(new FramesLabelProvider());
		framesViewer.setInput(new Object());		
	}

	public void setFocus() {
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			if(checkRefConfirm()==false){
				return;
			}
			this.edgeEditor.checkSave();
			this.headEditor.checkSave();
			this.actionEditor.checkSave();
			selfSave = true;
			animateSet.save(animateFile, true);
			String path = animateFile.getAbsolutePath();
			path = path.substring(0, path.length() - 1) + "n";
			animateSet.save(new File(path), false);
			setDirty(false);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			monitor.setCanceled(true);
		}
	}

	private boolean checkRefConfirm() {
		try {
			if(ProjectParser.getFileRefList(animateFile.getAbsolutePath()).length>0){
				boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认", "此文件已被地图引用.确认修改?");
				return ret;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
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
		
		// 如果打开的是ctn文件，自动生成一个cts文件
		if (filePath.endsWith(".ctn")) {
			String ctsPath = Utils.replaceSuffix(filePath, "cts");
			if (!new File(ctsPath).exists()) {
				// 自动生成
				try {
					PipAnimateSet ani = new PipAnimateSet();
					ani.load(new File(filePath), true);
					ani.save(new File(ctsPath), true);
				} catch (Exception e) {
				    e.printStackTrace();
					MessageDialog.openError(site.getShell(), "错误", "文件格式错误。\n"+e);
				}
				isAutoGenerated = true;
			}
			IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(ctsPath));
			finput = new FileStoreEditorInput(fileStore);
			setInput(finput);
			filePath = ctsPath;
		}
		
		animateFile = new File(filePath);
		try {
			animateSet = new PipAnimateSet();
			animateSet.load(animateFile);
			FileWatcher.watch(animateFile, this);
		} catch (Exception e) {
		    e.printStackTrace();
			MessageDialog.openError(site.getShell(), "错误", "文件格式错误。\n"+e);
		}
		stateMgr = new StateManager(20000000);
		saveState();
	}
	
	private void saveState() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			animateSet.saveState(new DataOutputStream(bos));
			stateMgr.push(bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void restoreState(byte[] data) {
		try {
			animateSet.restoreState(new DataInputStream(new ByteArrayInputStream(data)));
			imagesViewer.refresh();
			imageSelectionChanged();
			framesViewer.refresh();
			frameSelectionChanged();
			int oldsel = animateFramesTable.getSelectionIndex();
			animatesViewer.refresh();
			animateSelectionChanged();
			animateFramesViewer.refresh();
			if (oldsel < animateFramesTable.getItemCount()) {
				animateFramesTable.setSelection(oldsel);
			}
			animateFrameSelectionChanged();
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

	public void setDirty(boolean value) {
		dirty = value;
		firePropertyChange(PROP_DIRTY);
		if (value) {
			saveState();
			firePropertyChange(PROPERTY_CANUNDO);
			firePropertyChange(PROPERTY_CANREDO);
		}
	}

	private void createActions() {
		newAnimateAction = new Action("新建动画") {
			public void run() {
				onNewAnimate();
			}
		};
		delAnimateAction = new Action("删除动画") {
			public void run() {
				onDelAnimate();
			}
		};
		moveUpAnimateAction = new Action("上移(SHIFT+上)") {
		    public void run() {
		        onMoveUpAnimate();
		    }
		};
		moveDownAnimateAction = new Action("下移(SHIFT+下)") {
		    public void run() {
		        onMoveDownAnimate();
		    }
		};
		dupAnimateAction1 = new Action("复制动画(添加到最后)") {
            public void run() {
                onDupAnimate(true);
            }
        };
        dupAnimateAction2 = new Action("复制动画(添加到本动画后)") {
            public void run() {
                onDupAnimate(false);
            }
        };
        flipAnimateAction = new Action("翻转并复制") {
            public void run() {
                onHflipAnimate();
            }
        };
		dupAllAnimateAction = new Action("翻转并复制全部动画") {
		    public void run() {
		        onDupAndHflipAllAnimates();
		    }
		};
		
		newFrameAction = new Action("新建帧") {
			public void run() {
				onNewFrame();
			}
		};
		dupFrameAction = new Action("复制帧") {
			public void run() {
				onDupFrame();
			}
		};
		delFrameAction = new Action("删除帧") {
			public void run() {
				onDelFrame();
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
        moveUpFrameAction = new Action("上移(SHIFT+上)") {
            public void run() {
                onMoveUpFrame();
            }
        };
        moveDownFrameAction = new Action("下移(SHIFT+下)") {
            public void run() {
                onMoveDownFrame();
            }
        };
        checkFrameRefAction = new Action("检查所有帧中引用的图块") {
        	public void run() {
        		onValidateFrames();
        	}
        };
        cleanPiece = new Action("清理无用的图块"){
        	public void run(){
        		onCleanPiece();
        	}
        };
        cleanFrameAction = new Action("清理无用的帧") {
        	public void run() {
        		onCleanFrame();
        	}
        };
		delAnimateFrameAction = new Action("删除帧") {
			public void run() {
				onDelAnimateFrame();
			}
		};
		moveUpAnimateFrameAction = new Action("上移(SHIFT+上)") {
			public void run() {
				onMoveUpAnimateFrame();
			}
		};
		moveDownAnimateFrameAction = new Action("下移(SHIFT+下)") {
			public void run() {
				onMoveDownAnimateFrame();
			}
		};
		newImageAction = new Action("添加文件...") {
			public void run() {
				onLoadFile();
			}
		};
		editImageAction = new Action("编辑文件") {
            public void run() {
                onEditFile();
            }
        };
        replaceImageAction = new Action("替换文件...") {
            public void run() {
                onReplaceFile();
            }
        };
		delImageAction = new Action("删除文件") {
			public void run() {
				onDeleteImage();
			}
		};
        renameImageAction = new Action("文件改名") {
            public void run() {
                onRenameImage();
            }
        };
        upImageAction = new Action("上移") {
            public void run() {
                onSwapImage(0);
            }
        };
        downImageAction = new Action("下移") {
            public void run() {
                onSwapImage(1);
            }
        };
        createSequenceAction = new Action("自动创建动画序列...") {
        	public void run() {
        		onAutoCreateSequence();
        	}
        };
        createFramesAction = new Action("新建一组帧...") {
        	public void run() {
        		onCreateFrames();
        	}
        };
        renameFramesAction = new Action("重命名一组帧...") {
        	public void run() {
        		onRenameFrames();
        	}
        };
        fillFramesAction = new Action("自动填充帧内容...") {
        	public void run() {
        		onFillFrames();
        	}
        };
	}
	
	protected void onCleanPiece() {
		boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认清理", "此操作将会更改使用到的pip文件\n" +
				"可能会造成使用了这些pip的其他cts出现错误\n" +
				"是否继续清理?");
		if(ret == false){
			return;
		}
		int cnt = animateSet.getFrameCount();
		Set<String> usedPieces = new HashSet<String>();
		for(int i=0; i<cnt; i++){
			PipAnimateFrame frame = animateSet.getFrame(i);
			int pcnt = frame.getPieceCount();
			for(int j=0; j<pcnt; j++){
				PipAnimateFramePiece piece = frame.getPiece(j);
				String key = piece.getImageID()+":"+piece.getFrame();
				usedPieces.add(key);
			}
		}
		cnt = animateSet.getFileCount();
		for(int i=0; i<cnt; i++){
//			if(i!=1)continue;
			PipImage img = animateSet.getSourceImage(i);
			imagesTable.select(i);
			imageViewer.setInput(img);
			int pcnt = img.getFrameCount();
			int deleted = 0;
			for(int j=0; j<pcnt; j++){
//				if(j!=2 && j!=5)continue;
				String key = i+":"+j;
				if(usedPieces.contains(key)==false){
					System.out.println("AnimateEditor.onCleanPiece() "+key);
					imageViewer.setSelectedFrame(j - deleted);
					imageViewer.onDeleteFrame();
					deleted ++;
//					contentChanged(imageViewer);
				}
			}
		}
	}
	
	protected void onCleanFrame() {
		boolean[] usedFlag = new boolean[animateSet.getFrameCount()];
		for (int i = 0; i < animateSet.getAnimateCount(); i++) {
			PipAnimate pa = animateSet.getAnimate(i);
			for (int j = 0; j < pa.getFrameCount(); j++) {
				PipAnimateFrameRef ref = pa.getFrame(j);
				usedFlag[ref.getFrame()] = true;
			}
		}
		int unusedCount = 0;
		for (int i = 0; i < usedFlag.length; i++) {
			if (!usedFlag[i]) {
				unusedCount++;
			}		
		}
		if (unusedCount == 0) {
			MessageDialog.openInformation(getSite().getShell(), "信息", "没有未用到的帧。");
			return;
		}
		boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认清理", "共有" + unusedCount + "帧会被删除，确认继续清理？");
		if (ret == false) {
			return;
		}
		for (int i = usedFlag.length - 1; i >= 0; i--) {
			if (!usedFlag[i]) {
				animateSet.removeFrame(i);
			}
		}
		framesViewer.refresh();
		frameSelectionChanged();
		animateFramesViewer.refresh();
		animateFrameSelectionChanged();
		setDirty(true);
	}

	private void onNewAnimate() {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建动画", "请输入动画标题：", "", new IInputValidator() {
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
			animateSet.addAnimate(newname);
			animatesViewer.refresh();
			animatesTable.setSelection(animateSet.getAnimateCount() - 1);
			this.animateSelectionChanged();
			setDirty(true);
		}
	}
	
	private void onDelAnimate() {
		int sel = animatesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		animateSet.removeAnimate(sel);
		animatesViewer.refresh();
		if (sel < animateSet.getAnimateCount()) {
			animatesTable.setSelection(sel);
		} else if (sel > 0) {
			animatesTable.setSelection(sel - 1);
		} else {
			animatesTable.setSelection(new int[0]);
		}
		animateSelectionChanged();
		setDirty(true);
	}
	
	private void onMoveUpAnimate() {
        int sel = animatesTable.getSelectionIndex();
        if (sel == -1 || sel == 0) {
            return;
        }
        animateSet.swapAnimates(sel, sel - 1);
        animatesViewer.refresh();
        animatesTable.setSelection(sel - 1);
        animateSelectionChanged(); 
        setDirty(true );
	}
	
	private void onMoveDownAnimate() {
        int sel = animatesTable.getSelectionIndex();
        if (sel == -1 || sel == animateSet.getAnimateCount() - 1) {
            return;
        }
        animateSet.swapAnimates(sel, sel + 1);
        animatesViewer.refresh();
        animatesTable.setSelection(sel + 1);
        animateSelectionChanged(); 
        setDirty(true );
	}
	
	private void onDupAnimate(boolean addToTail) {
	    int sel = animatesTable.getSelectionIndex();
        if (sel == -1 || sel >= animateSet.getAnimateCount()) {
            return;
        }
        animateSet.dupAnimate(sel, addToTail);
        animatesViewer.refresh();
        if (addToTail) {
            animatesTable.setSelection(animateSet.getAnimateCount() - 1);
        } else {
            animatesTable.setSelection(sel + 1);
        }
        animateSelectionChanged(); 
        setDirty(true );
	}
	
	private void onHflipAnimate() {
	    int sel = animatesTable.getSelectionIndex();
        if (sel == -1 || sel > animateSet.getAnimateCount() - 1) {
            return;
        }
        int frameCnt = animateSet.getFrameCount();
        animateSet.dupAndHflip(sel);
        if(frameCnt!=animateSet.getFrameCount()){
        	frameSelector.refresh();
        }
        animatesViewer.refresh();
        framesViewer.refresh();
        animatesTable.setSelection(animateSet.getAnimateCount() - 1);
        animateSelectionChanged(); 
        setDirty(true );
	}
	
	private void onDupAndHflipAllAnimates() {
	    int count = animateSet.getAnimateCount();
	    for (int i = 0; i < count; i++) {
	        animateSet.dupAndHflip(i);
	    }
	    animatesViewer.refresh();
	    framesViewer.refresh();
	    setDirty(true );
	}
	
	private void onNewFrame() {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建帧", "请输入帧标题：", "", new IInputValidator() {
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
			animateSet.addFrame(newname);
			framesViewer.refresh();
			framesTable.setSelection(animateSet.getFrameCount() - 1);
			this.frameSelectionChanged();
			setDirty(true);
		}
	}
	
	private void onDupFrame() {
		int sel = framesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		int[] sels = framesTable.getSelectionIndices();
		animateSet.dupFrame(sels);
		framesViewer.refresh();
		framesTable.setSelection(sels[sels.length - 1] + 1);
		this.frameSelectionChanged();
		setDirty(true);
	}
	
	private void onDelFrame() {
		int sel = framesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		int[] sels = framesTable.getSelectionIndices();
		for (int i = sels.length - 1; i >= 0; i--) {
		    animateSet.removeFrame(sels[i]);
		}
		framesViewer.refresh();
		if (sel < animateSet.getFrameCount()) {
			framesTable.setSelection(sel);
		} else if (sel > 0) {
			framesTable.setSelection(sel - 1);
		} else {
			framesTable.setSelection(new int[0]);
		}
		frameSelectionChanged();
		animateFramesViewer.refresh();
		animateFrameSelectionChanged();
		setDirty(true);
	}
	
	private void onHflipFrame() {
        int sel = framesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        int[] sels = framesTable.getSelectionIndices();
        for (int i = 0; i < sels.length; i++) {
            PipAnimateFrame frame = animateSet.getFrame(sels[i]);
            frame.hflip();
            animateSet.syncMirrorFrame(frame);
        }
        animateFrameEditor.redraw();
        setDirty(true);
	}

   private void onVflipFrame() {
        int sel = framesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        int[] sels = framesTable.getSelectionIndices();
        for (int i = 0; i < sels.length; i++) {
            PipAnimateFrame frame = animateSet.getFrame(sels[i]);
            frame.vflip();
            animateSet.syncMirrorFrame(frame);
        }
        animateFrameEditor.redraw();
        setDirty(true);
    }
   
    private void onMoveUpFrame() {
        int sel = framesTable.getSelectionIndex();
        if (sel == -1 || sel == 0) {
            return;
        }
        int[] sels = framesTable.getSelectionIndices();
        for (int i = 0; i < sels.length; i++) {
            if(sels[i] == 0){
                return;
            }
        }
        for (int i = 0; i < sels.length; i++) {
            animateSet.swapFrames(sels[i], sels[i] - 1);
            sels[i]--;
        }
        framesTable.setSelection(sels);
        framesViewer.refresh();
        frameSelectionChanged();
        animateFramesViewer.refresh();
        animateFrameSelectionChanged();
        setDirty(true);
    }
    
    private void onMoveDownFrame() {
        int sel = framesTable.getSelectionIndex();
        if (sel == -1 || sel == animateSet.getFrameCount() - 1) {
            return;
        }
        int[] sels = framesTable.getSelectionIndices();
        for (int i = sels.length - 1; i >= 0; i--) {
            if(sels[i] == animateSet.getFrameCount() - 1){
                return;
            }
        }
        for (int i = sels.length - 1; i >= 0; i--) {
            animateSet.swapFrames(sels[i], sels[i] + 1);
            sels[i]++;
        }
        framesTable.setSelection(sels);
        framesViewer.refresh();
        frameSelectionChanged();
        animateFramesViewer.refresh();
        animateFrameSelectionChanged();
        setDirty(true);
    }
    
    private void onValidateFrames() {
    	boolean changed = false;
    	for (int i = 0; i < animateSet.getFrameCount(); i++) {
    		PipAnimateFrame frame = animateSet.getFrame(i);
    		for (int j = 0; j < frame.getPieceCount(); j++) {
    			PipAnimateFramePiece piece = frame.getPiece(j);
    			if (piece.getImageID() < 0 || piece.getImageID() >= animateSet.getFileCount()) {
    				String message = "第" + (i + 1) + "帧中引用了不存在的图片，是否删除？";
    				if (MessageDialog.openConfirm(getSite().getShell(), "删除", message)) {
    					frame.removePiece(j);
    					j--;
    					changed = true;
    				}
    			} else if (piece.getFrame() < 0 || piece.getFrame() >= animateSet.getSourceImage(piece.getImageID()).getFrameCount()) {
    				String message = "第" + (i + 1) + "帧中引用了图片" + animateSet.getFileName(piece.getImageID()) + "中不存在的帧，是否删除？";
    				if (MessageDialog.openConfirm(getSite().getShell(), "删除", message)) {
    					frame.removePiece(j);
    					j--;
    					changed = true;
    				}
    			}
    		}
    	}
    	if (changed) {
    		setDirty(true);
    	}
    }

	private void onDelAnimateFrame() {
		PipAnimate ani = getSelectedAnimate();
		if (ani == null) {
			return;
		}
		int sel = animateFramesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		ani.removeFrame(sel);
		animateFramesViewer.refresh();
		if (sel < ani.getFrameCount()) {
			animateFramesTable.setSelection(sel);
		} else if (sel > 0) {
			animateFramesTable.setSelection(sel - 1);
		} else {
			animateFramesTable.setSelection(new int[0]);
		}
		animateFrameSelectionChanged();
		setDirty(true);
	}
	
	private void onMoveUpAnimateFrame() {
		PipAnimate ani = getSelectedAnimate();
		if (ani == null) {
			return;
		}
		int sel = animateFramesTable.getSelectionIndex();
		if (sel == -1 || sel == 0) {
			return;
		}
		ani.swapFrames(sel, sel - 1);
		animateFramesViewer.refresh();
		animateFramesTable.setSelection(sel - 1);
		animateFrameSelectionChanged();
		setDirty(true);
	}
	
	private void onMoveDownAnimateFrame() {
		PipAnimate ani = getSelectedAnimate();
		if (ani == null) {
			return;
		}
		int sel = animateFramesTable.getSelectionIndex();
		if (sel == -1 || sel == ani.getFrameCount() - 1) {
			return;
		}
		ani.swapFrames(sel, sel + 1);
		animateFramesViewer.refresh();
		animateFramesTable.setSelection(sel + 1);
		animateFrameSelectionChanged();
		setDirty(true);
	}
	
	private void onDecreaseFrameDelay() {
	    PipAnimate ani = getSelectedAnimate();
        if (ani == null) {
            return;
        }
        int sel = animateFramesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        PipAnimateFrameRef ref = ani.getFrame(sel);
        if (ref.getDelay() > 0) {
            ref.setDelay(ref.getDelay() - 1);
            animateFramesViewer.refresh();
            animateFramesViewer.refresh();
            animateFramesTable.setSelection(sel);
            setDirty(true);
        }
	}

    private void onIncreaseFrameDelay() {
        PipAnimate ani = getSelectedAnimate();
        if (ani == null) {
            return;
        }
        int sel = animateFramesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        PipAnimateFrameRef ref = ani.getFrame(sel);
        ref.setDelay(ref.getDelay() + 1);
        animateFramesViewer.refresh();
        animateFramesViewer.refresh();
        animateFramesTable.setSelection(sel);
        setDirty(true);
    }

    private void onSetFrameDelay(int delay) {
        PipAnimate ani = getSelectedAnimate();
        if (ani == null) {
            return;
        }
        int sel = animateFramesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        PipAnimateFrameRef ref = ani.getFrame(sel);
        ref.setDelay(delay);
        animateFramesViewer.refresh();
        animateFramesViewer.refresh();
        animateFramesTable.setSelection(sel);
        setDirty(true);
    }
    
	private void onLoadFile() {
		if (ImageEditor.openFileDlg == null) {
			ImageEditor.openFileDlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		}
		ImageEditor.openFileDlg.setFilterPath(animateFile.getParent());
		ImageEditor.openFileDlg.setFilterExtensions(new String[] { "*.pip", "*.*" });
		ImageEditor.openFileDlg.setFilterNames(new String[] { "PiP图片文件(*.pip)", "所有文件(*.*)" });
		String imgFile = ImageEditor.openFileDlg.open();
		if (imgFile != null) {
			try {
				animateSet.addSourceFile(imgFile);
				imagesViewer.refresh();
				imagesTable.setSelection(animateSet.getFileCount() - 1);
				imageSelectionChanged();
				setDirty(true);
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
		}
	}
	
    private void onReplaceFile() {
        int sel = imagesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        File srcFile = animateSet.getSourceFile(sel);
        
        // 选择一个目标文件
        FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.pip" });
        dlg.setFilterNames(new String[] { "PIP图片文件(*.pip)" });
        dlg.setFilterPath(srcFile.getParent());
        String newFile = dlg.open();
        if (newFile == null) {
            return;
        }
        
        // 用目标文件替换原始文件
        PipImage newImg = new PipImage();
        try {
            newImg.load(newFile);
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            return;
        }
        
        if (newImg.getImgCount() != animateSet.getSourceImage(sel).getImgCount()) {
            String msg = "新图片有" + newImg.getImgCount() + "帧，原图片有" + animateSet.getSourceImage(sel).getImgCount() + "帧，确认？";
            if (!MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
                return;
            }
        }
        
        animateSet.replaceFile(sel, newFile, newImg);
        
        imageViewer.redraw();
        imagesViewer.refresh();
        
        setDirty(true);
    }

    private void onEditFile() {
	    int sel = imagesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        File srcFile = animateSet.getSourceFile(sel);
        
        // 检查是否已经打开编辑器，如果已经打开则激活，否则打开
        IEditorPart editor = null;
        IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((srcFile.getAbsolutePath())));
        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        editor = page.findEditor(input);
        if (editor != null) {
            page.activate(editor);
        } else {
            try {
                page.openEditor(input, ImageEditor.ID);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
        
        // 监控文件的变化
        FileWatcher.watch(srcFile, this);
	}
	
	// pip文件变化
	public void fileModified(final File f) {
        display.asyncExec(new Runnable() {
            public void run() {
            	if(f.getName().endsWith(".cts")){
            		if(selfSave == false){
            			reloadAnimateFile();
            		}else{
            			selfSave = false;
            		}
            	}else{
            		notifyFileChanged();
            	}
            }
        });
	}
	
	protected void reloadAnimateFile() {
		try{
			boolean ret = true;
			if (this.isDirty()) {
				ret = MessageDialog.openConfirm(getSite().getShell(), "确认", animateFile.getName() + "正在编辑并已被其他编辑程序修改, 是否放弃当前修改重新加载？");
			}
			if (ret) {
				animateSet = new PipAnimateSet();
				animateSet.load(animateFile);		
				framesViewer.setInput(animateSet);
				int idx = animatesViewer.getTable().getSelectionIndex();
				animatesViewer.setInput(animateSet);
				if(idx>=0 && idx<animateSet.getAnimateCount()){
					animateViewer.setInput(animateSet.getAnimate(idx));
					animateViewer.setCurrentFrame(0);
				}else{
					animateViewer.setInput(null);
				}
				animateFrameEditor.setInput(getSelectedFrame());
				animateViewer.redraw();
				setDirty(false);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void notifyFileChanged() {
	    setDirty(true);
	    MessageDialog.openInformation(getSite().getShell(), "文件改变", "引用文件已经改变，请关闭编辑器重新打开。");
	}
	
	private void onMergeAnimateSet() {
	    if (false) {
	        // 检查未用帧
	        List<Integer> fs = this.animateSet.findUnusedFrames();
	        String msg = "未使用的帧：";
	        for (int i = 0; i < fs.size(); i++) {
	            msg = msg + fs.get(i) + ",";
	        }
	        MessageDialog.openInformation(getSite().getShell(), "统计", msg);
	        return;
	    }
	    if (false) {
	        // 检查未用图块
	        for (int i = 0; i < animateSet.getFileCount(); i++) {
	            List<Integer> fs = this.animateSet.findUnusedPiece(i);;
	            String msg = "未使用的图块：";
	            for (int j = 0; j < fs.size(); j++) {
	                msg = msg + fs.get(j) + ",";
	            }
	            MessageDialog.openInformation(getSite().getShell(), "统计", msg);
	            return; 
	        }
	    }
	    
        FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.cts", "*.*" });
        dlg.setFilterNames(new String[] { "动画文件(*.cts)", "所有文件(*.*)" });
        dlg.setFilterPath(this.animateFile.getParent());
        String mergeFile = dlg.open();
        if (mergeFile == null) {
            return;
        }
        if (!new File(mergeFile).getParentFile().equals(this.animateFile.getParentFile())) {
            MessageDialog.openError(getSite().getShell(), "错误", "只能合并在同一目录下的动画文件。");
            return;
        }
        try {
            PipAnimateSet mergeSet = new PipAnimateSet();
            mergeSet.load(new File(mergeFile));
            if (isDirty() && !MessageDialog.openConfirm(getSite().getShell(), "改名", "需要先保存才能继续，是否保存？")) {
                return;
            }
            
            // 进行帧匹配
            String[] frameNames1 = new String[animateSet.getFrameCount() + 1];
            String[] frameNames2 = new String[mergeSet.getFrameCount()];
            frameNames1[0] = "< 新建帧 >";
            for (int i = 0; i < animateSet.getFrameCount(); i++) {
                frameNames1[i + 1] = animateSet.getFrame(i).getName();
            }
            for (int i = 0; i < mergeSet.getFrameCount(); i++) {
                frameNames2[i] = mergeSet.getFrame(i).getName();
            }
            StringMapping map = new StringMapping();
            map.init(frameNames2, frameNames1);
            MappingDialog mdlg = new MappingDialog(getSite().getShell(), "动画帧匹配");
            mdlg.setMapping(map);
            if (mdlg.open() != MappingDialog.OK) {
                return;
            }
            int[] frameMapping = map.mapping;

            // 进行动画序列匹配
            String[] animateNames1 = new String[animateSet.getAnimateCount() + 1];
            String[] animateNames2 = new String[mergeSet.getAnimateCount()];
            animateNames1[0] = "< 新建动画序列 >";
            for (int i = 0; i < animateSet.getAnimateCount(); i++) {
                animateNames1[i + 1] = animateSet.getAnimate(i).getName();
            }
            for (int i = 0; i < mergeSet.getAnimateCount(); i++) {
                animateNames2[i] = mergeSet.getAnimate(i).getName();
            }
            map = new StringMapping();
            map.init(animateNames2, animateNames1);
            mdlg = new MappingDialog(getSite().getShell(), "动画序列匹配");
            mdlg.setMapping(map);
            if (mdlg.open() != MappingDialog.OK) {
                return;
            }
            int[] animateMapping = map.mapping;
            
            getSite().getWorkbenchWindow().getActivePage().saveEditor(this, false);
            animateSet.merge(mergeSet, frameMapping, animateMapping);
            setDirty(true);
            getSite().getWorkbenchWindow().getActivePage().saveEditor(this, false);
            imagesViewer.refresh();
            framesViewer.refresh();
            animateFrameEditor.redraw();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
	}

    private void onSamllAnimateSet(boolean withFile) {
        animateSet.smaller(withFile);
        setDirty(true);
        imagesViewer.refresh();
        framesViewer.refresh();
        animateFrameEditor.redraw();
    }
    
    private void onEnlargeAnimateSet(boolean withFile) {
        animateSet.enlarge(withFile);
        setDirty(true);
        imagesViewer.refresh();
        framesViewer.refresh();
        animateFrameEditor.redraw();
    }
    	
	private void onDeleteImage() {
		int sel = imagesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		if (!MessageDialog.openConfirm(getSite().getShell(), "删除文件", "此操作将删除所有属于此文件的图块，不可恢复，是否继续？")) {
		    return;
		}
		animateSet.removeSourceFile(sel);
		imagesViewer.refresh();
		if (sel < animateSet.getFileCount()) {
			imagesTable.setSelection(sel);
		} else if (sel > 0) {
			imagesTable.setSelection(sel - 1);
		} else {
			imagesTable.setSelection(new int[0]);
		}
		imageSelectionChanged();
		setDirty(true);
	}
	
	private void onSwapImage(int dir){
	    int sel = imagesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        
        int index1 = sel;
        int index2 = sel;
        
        if(dir == 0){
            if(sel - 1 < 0){
                return;
            }
            
            index2 = sel - 1;
        }else{
            if(sel + 1 > imagesTable.getItemCount()){
                return;
            }
            
            index2 = sel + 1;
        }
        
        animateSet.swapSourceFile(index1, index2);
        imagesViewer.refresh();
        imagesTable.setSelection(sel);
        imageSelectionChanged();
        setDirty(true);
	}
	
	private void onRenameImage() {
	    int sel = imagesTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        String oldName = animateSet.getFileName(sel);
        InputDialog dlg = new InputDialog(getSite().getShell(), "新建动画", "请输入动画文件名：", oldName, new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "文件名不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() == InputDialog.OK) {
            String newname = dlg.getValue();
            if (!newname.toLowerCase().endsWith(".pip")) {
                newname += ".pip";
            }
            try {
                if (isDirty() && !MessageDialog.openConfirm(getSite().getShell(), "改名", "需要先保存才能继续，是否保存？")) {
                    return;
                }
                getSite().getWorkbenchWindow().getActivePage().saveEditor(this, false);
                animateSet.renameSourceFile(sel, newname);
                setDirty(true);
                getSite().getWorkbenchWindow().getActivePage().saveEditor(this, false);
                imagesViewer.refresh();
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
	}
	
	private void onPlay() {
		animateViewer.play();
		if (animateViewer.isPlaying()) {
			playItem.setEnabled(false);
			stopItem.setEnabled(true);
		}
	}
	
	private void onStop() {
		animateViewer.stop();
		playItem.setEnabled(true);
		stopItem.setEnabled(false);
		animateFrameSelectionChanged();
	}
	
	private void animateSelectionChanged() {
		int sel = animatesTable.getSelectionIndex();
		if (sel == -1) {
			animateViewer.setRefAnimate(null);
			animateViewer.setInput(null);
			playItem.setEnabled(false);
			stopItem.setEnabled(false);
		} else {
			if (refAnimateSet != null && refAnimateSet.getAnimateCount() > sel) {
				animateViewer.setRefAnimate(refAnimateSet.getAnimate(sel));
			} else {
				animateViewer.setRefAnimate(null);
			}
			animateViewer.setInput(animateSet.getAnimate(sel));
			playItem.setEnabled(true);
			stopItem.setEnabled(false);
		}
        animateFramesViewer.refresh();
        animateFramesViewer.setAllChecked(false);
        animateFramesTable.setSelection(new int[] { 0 });
        animateFrameSelectionChanged();
		animateViewer.redraw();
		
		MenuManager mgr = new MenuManager();
		mgr.add(newAnimateAction);
		if (sel != -1) {
			mgr.add(delAnimateAction);
			mgr.add(moveUpAnimateAction);
			mgr.add(moveDownAnimateAction);
			mgr.add(dupAnimateAction1);
			mgr.add(dupAnimateAction2);
			mgr.add(flipAnimateAction);
			mgr.add(dupAllAnimateAction);
		}
		Menu menu = mgr.createContextMenu(animatesTable);
		if (animatesTable.getMenu() != null) {
			animatesTable.getMenu().dispose();
		}
		animatesTable.setMenu(menu);
	}
	
	private void animateFrameSelectionChanged() {
		int sel = animateFramesTable.getSelectionIndex();
		if (!animateViewer.isPlaying()) {
			Object[] checkedArr = animateFramesViewer.getCheckedElements();
			int[] indexArr = new int[checkedArr.length];
			for (int i = 0; i < indexArr.length; i++) {
				indexArr[i] = ((Integer)checkedArr[i]).intValue();
			}
			animateViewer.setVisibleFrames(indexArr);
			animateViewer.setCurrentFrame(sel);
			animateViewer.redraw();
		}
		
		if (this.getSelectedAnimate() == null) {
			animateFramesTable.setMenu(null);
			return;
		}
		if (sel != -1) {
			MenuManager mgr = new MenuManager();
			mgr.add(moveUpAnimateFrameAction);
			mgr.add(moveDownAnimateFrameAction);
			mgr.add(delAnimateFrameAction);
			Menu menu = mgr.createContextMenu(animateFramesTable);
			if (animateFramesTable.getMenu() != null) {
				animateFramesTable.getMenu().dispose();
			}
			animateFramesTable.setMenu(menu);
		} else {
			if (animateFramesTable.getMenu() != null) {
				animateFramesTable.getMenu().dispose();
			}
			animateFramesTable.setMenu(null);
		}
	}
	
	private void animateFrameDoubleClicked() {
		int sel = animateFramesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		PipAnimate ani = getSelectedAnimate();
		if (ani == null) {
			return;
		}
		PipAnimateFrameRef ref = ani.getFrame(sel);
		IntegerInputDialog dlg = new IntegerInputDialog(getSite().getShell(), 1, 15, ref.getDelay(), "修改停顿帧数", "停顿帧数");
		if (dlg.open() == IntegerInputDialog.OK && ref.getDelay() != dlg.getValue()) {
			ref.setDelay(dlg.getValue());
			animateFramesViewer.refresh();
			setDirty(true);
		}
	}
	
	private void animateDoubleClicked() {
		PipAnimate ani = getSelectedAnimate();
		if (ani == null) {
			return;
		}
		InputDialog dlg = new InputDialog(getSite().getShell(), "修改标题", "请输入新标题：", ani.getName(), new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "标题不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK && !dlg.getValue().equals(ani.getName())) {
			String newname = dlg.getValue();
			ani.setName(newname);
			animatesViewer.refresh();
			setDirty(true);
		}
	}
	
	private void frameDoubleClicked() {
		PipAnimateFrame fr = getSelectedFrame();
		if (fr == null) {
			return;
		}
		InputDialog dlg = new InputDialog(getSite().getShell(), "修改标题", "请输入新标题：", fr.getName(), new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "标题不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK && !dlg.getValue().equals(fr.getName())) {
			String newname = dlg.getValue();
			fr.setName(newname);
			framesViewer.refresh();
			setDirty(true);
		}
	}
	
	private void imageSelectionChanged() {
		int sel = imagesTable.getSelectionIndex();
		if (sel == -1) {
			imageViewer.setInput(null);
		} else {
			imageViewer.setInput(animateSet.getSourceImage(sel));
		}
		imageViewer.redraw();
		
		MenuManager mgr = new MenuManager();
		mgr.add(newImageAction);
		if (sel != -1) {
		    mgr.add(editImageAction);
			mgr.add(delImageAction);
            mgr.add(replaceImageAction);
			mgr.add(renameImageAction);
			mgr.add(upImageAction);
			mgr.add(downImageAction);
		}
		Menu menu = mgr.createContextMenu(imagesTable);
		if (imagesTable.getMenu() != null) {
			imagesTable.getMenu().dispose();
		}
		imagesTable.setMenu(menu);
	}
	
	private void frameSelectionChanged() {
		int sel = framesTable.getSelectionIndex();
		if (sel == -1) {
			animateFrameEditor.setRefFrame(null);
			animateFrameEditor.setInput(null);
		} else {
			if (refImageSet != null && refImageSet.getFrameCount() > 0) {
				animateFrameEditor.setRefFrame(refImageSet.getFrame(0));
			}
			animateFrameEditor.setInput(animateSet.getFrame(sel));
			PipAnimateFrame curFrame = animateSet.getFrame(sel);
			piecesView.setCurFrame(curFrame);
		}
		animateFrameEditor.redraw();
		
		MenuManager mgr = new MenuManager();
		mgr.add(newFrameAction);
		if (sel != -1) {
			mgr.add(dupFrameAction);
			mgr.add(delFrameAction);
			mgr.add(hflipFrameAction);
            mgr.add(vflipFrameAction);
            mgr.add(moveUpFrameAction);
            mgr.add(moveDownFrameAction);
            mgr.add(checkFrameRefAction);
		}
		mgr.add(cleanPiece);
		mgr.add(cleanFrameAction);
		
		MenuManager batchMenu = new MenuManager("批量处理");
		mgr.add(batchMenu);
		batchMenu.add(createSequenceAction);
		batchMenu.add(createFramesAction);
		batchMenu.add(fillFramesAction);
		batchMenu.add(renameFramesAction);
		
		Menu menu = mgr.createContextMenu(framesTable);
		if (framesTable.getMenu() != null) {
			framesTable.getMenu().dispose();
		}
		framesTable.setMenu(menu);
	}

	public void areaSelected(Object source) {}

	public void frameDoubleClicked(Object source, int frame) {
		if (source == imageViewer) {
			PipAnimateFrame f = getSelectedFrame();
			if (f == null) {
				return;
			}
			PipAnimateFramePiece newPiece = f.addPiece(imagesTable.getSelectionIndex(), frame);
			newPiece.setDx(animateFrameEditor.getRefPoint().x);
            newPiece.setDy(animateFrameEditor.getRefPoint().y);
            animateSet.syncMirrorFrame(f);
			animateFrameEditor.setSelectedPiece(f.getPieceCount() - 1);
			animateFrameEditor.redraw();
			setDirty(true);
		} else if (source == frameSelector) {
			PipAnimate ani = getSelectedAnimate();
			if (ani == null) {
				return;
			}
			PipAnimateFrameRef ref = ani.addFrame(frame);
			ref.setDx(animateViewer.getRefPoint().x);
			ref.setDy(animateViewer.getRefPoint().y);
			animateFramesViewer.refresh();
			animateFramesTable.setSelection(ani.getFrameCount() - 1);
			this.animateFrameSelectionChanged();
			setDirty(true);
		}
	}

	public void frameSelectionChanged(Object source, int newFrame) {
		if (source == imageViewer) {
			updateUsage(imagesTable.getSelectionIndex(), newFrame);
		}
	}
	
	private void updateUsage(int image, int frame) {
		filterImageID = image;
		filterFrameID = frame;
		framesViewer.refresh();
	}
	
	public void contentChanged(Object source) {
		if (source == imageViewer) {
			if (imageViewer.splitPlan == null) {
				// 某一图块被修改
				int imgID = imagesTable.getSelectionIndex();
				Map<Integer, Integer> frameMap = imageViewer.getFrameMap();
				animateSet.setFileModified(imgID);
				if (frameMap != null) {
					animateSet.adjustSourceFrame(imgID, frameMap);
				}
				this.animateFrameEditor.redraw();
			} else {
				// 某一图块被拆分
				int imgID = imagesTable.getSelectionIndex();
				animateSet.setFileModified(imgID);
				animateSet.onImageSplit(imgID, imageViewer.getSelectedFrame(), 
						imageViewer.splitPlan, imageViewer.splitOriginalWidth,
						imageViewer.splitOriginalHeight);
				this.animateFrameEditor.redraw();
				this.piecesView.contentChanged(source);
			}
		} else if (source == animateFrameEditor) {
			animateSet.syncMirrorFrame(animateFrameEditor.getFrame());
			this.piecesView.contentChanged(source);
		}
		setDirty(true);
	}
	
	private PipAnimate getSelectedAnimate() {
		int sel = animatesTable.getSelectionIndex();
		if (sel == -1) {
			return null;
		}
		return animateSet.getAnimate(sel);
	}
	
	private PipAnimateFrame getSelectedFrame() {
		int sel = framesTable.getSelectionIndex();
		if (sel == -1) {
			return null;
		}
		return animateSet.getFrame(sel);
	}
	
	public boolean canUndo() {
		return stateMgr.canUndo();
	}
	
	public boolean canRedo() {
	    return stateMgr.canRedo();
	}
	
	public int getSelectedPiece() {
		return imageViewer.getSelectedFrame();
	}
	
	public int getSelectedImage() {
		return imagesTable.getSelectionIndex();
	}

	public void dispose() {
		super.dispose();
		FileWatcher.unwatch(this);
		if (isAutoGenerated) {
			animateFile.delete();
		}
	}
	
	private void chooseRefAnimateSet() {
        // 选择一个目标文件
        FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.cts" });
        dlg.setFilterNames(new String[] { "CTS动画文件(*.cts)" });
        if (animateSet.getFileCount() > 0) {
        	dlg.setFilterPath(animateSet.getSourceFile(0).getParent());
        }
        String newFile = dlg.open();
        if (newFile == null) {
            return;
        }
        
        // 载入
        try {
        	PipAnimateSet ani = new PipAnimateSet();
        	ani.load(new File(newFile));
        	refAnimateSet = ani;
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            return;
        }
        animateSelectionChanged();
    }
	
	private void chooseRefImage() {
        // 选择一个目标文件
        FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.cts" });
        dlg.setFilterNames(new String[] { "CTS动画文件(*.cts)" });
        if (animateSet.getFileCount() > 0) {
        	dlg.setFilterPath(animateSet.getSourceFile(0).getParent());
        }
        String newFile = dlg.open();
        if (newFile == null) {
            return;
        }
        
        // 载入
        try {
        	PipAnimateSet ani = new PipAnimateSet();
        	ani.load(new File(newFile));
        	refImageSet = ani;
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            return;
        }
        frameSelectionChanged();
    }
	
	/**
	 * 开始编辑第N个动画，如果这个动画只包含一个piece，则直接开始编辑这一帧图片。
	 * @param index
	 */
	public void editAnimate(int index) {
		if (index < 0 || index >= animateSet.getAnimateCount()) {
			return;
		}
		animatesTable.setSelection(index);
		animateSelectionChanged();
		PipAnimate ani = animateSet.getAnimate(index);
		Set<Integer> pieces = new HashSet<Integer>();
		
		// 遍历这个动画序列，找出所有用到的图块，并且在编辑器中选中第一个图块
		for (int i = 0; i < ani.getFrameCount(); i++) {
			PipAnimateFrameRef ref = ani.getFrame(i);
			if (i == 0) {
				framesTable.setSelection(ref.getFrame());
				frameSelectionChanged();
			}
			PipAnimateFrame frame = ref.realize();
			for (int j = 0; j < frame.getPieceCount(); j++) {
				PipAnimateFramePiece piece = frame.getPiece(j);
				if (i == 0 && j == 0) {
					imagesTable.setSelection(piece.getImageID());
					imageSelectionChanged();
					imageViewer.setSelectedFrame(piece.getFrame());
				}
				int refKey = (piece.getImageID() << 16) | piece.getFrame();
				pieces.add(refKey);
			}
		}
		
		// 如果只有一个图块，打开系统编辑器编辑
		imageViewer.onEditFrame();
	}

	/*
	 * 打开一个向导，把某一个图块用另外一个图块替换。
	 */
	private void onReplacePiece() {
		int iid = getSelectedImage();
		if (iid == -1) {
			return;
		}
		int fid = this.getSelectedPiece();
		if (fid == -1) {
			return;
		}
		
		// 找出用到选中图块的所有帧
		List<PipAnimateFrame> frameList = new ArrayList<PipAnimateFrame>();
		for (int i = 0; i < animateSet.getFrameCount(); i++) {
			PipAnimateFrame frame = animateSet.getFrame(i);
			if (frame.containsPiece(iid, fid)) {
				frameList.add(frame);
			}
		}
		if (frameList.size() == 0) {
			MessageDialog.openInformation(getSite().getShell(), "消息", "选中图块没有被任何帧引用。");
			return;
		}
		
		// 弹出对话框
		ReplacePieceDialog dlg = new ReplacePieceDialog(getSite().getShell());
		PipAnimateFrame[] frames = new PipAnimateFrame[frameList.size()];
		frameList.toArray(frames);
		dlg.setData(frames, animateSet.getSourceImage(iid), iid, fid);
		if (dlg.open() == Dialog.OK) {
	        framesViewer.refresh();
	        frameSelectionChanged();
	        animateFramesViewer.refresh();
	        animateFrameSelectionChanged();
			setDirty(true);
		}
	}
	
	/*
	 * 用选中的动画帧，自动创建一个动画序列。动画序列名称和动画帧名称相同。
	 */
	private void onAutoCreateSequence() {
		int[] sels = framesTable.getSelectionIndices();
		if (sels == null || sels.length == 0) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", "请选中要用来创建动画序列的动画帧。");
			return;
		}
		if (sels.length == 1) {
			autoCreateSequence(sels);
		} else {
			String[] options = { "用所有选中的帧创建一个动画序列", "用每一个选中的帧分别创建动画序列" };
			GenericChooseDialog dlg = new GenericChooseDialog(getSite().getShell(), "选择模式", "请选择创建方式：", options);
			if (dlg.open() == Dialog.OK) {
				if (dlg.getSelectionIndex() == 0) {
					autoCreateSequence(sels);
				} else {
					for (int i = 0; i < sels.length; i++) {
						autoCreateSequence(new int[] { sels[i] });
					}
				}
			}
		}
		animatesViewer.refresh();
		setDirty(true);
	}
	
	private void autoCreateSequence(int[] sels) {
		PipAnimateFrame frame = animateSet.getFrame(sels[0]);
		String name = frame.getName();
		// 去掉名字后面的数字（除非名字完全是数字）
		int lastIndex = name.length() - 1;
		while (lastIndex >= 0) {
			if (Character.isDigit(name.charAt(lastIndex))) {
				lastIndex--;
			} else {
				break;
			}
		}
		if (lastIndex >= 0) {
			name = name.substring(0, lastIndex + 1);
		}
		PipAnimate ani = animateSet.addAnimate(name);
		for (int i = 0; i < sels.length; i++) {
			ani.addFrame(sels[i]);
		}
	}
	
	/*
	 * 一次性创建多个系列帧。
	 */
	private void onCreateFrames() {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建帧", "请输入帧标题：", "", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "标题不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		String newname = dlg.getValue();
		dlg = new InputDialog(getSite().getShell(), "数量", "请输入数量：", "1", new IInputValidator() {
			public String isValid(String newText) {
				try {
					int i = Integer.parseInt(newText.trim());
					if (i < 1 || i > 100) {
						throw new Exception();
					}
					return null;
				} catch (Exception e) {
					return "请输入1-100的数字。";
				}
			}
		});
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		int count = Integer.parseInt(dlg.getValue());
		int[] indices = new int[count];
		int base = animateSet.getFrameCount();
		for (int i = 0; i < count; i++) {
			animateSet.addFrame(newname + (i + 1));
			indices[i] = base + i;
		}
		framesViewer.refresh();
		framesTable.setSelection(indices);
		this.frameSelectionChanged();
		setDirty(true);
	}
	
	/*
	 * 选择一组帧后，用一组从3D模型导出的图片（包括素材图片和表示参考点的点图）来填充这一组帧。
	 * 在进行操作之前，需要：
	 * 1. 选择一个素材图片目录（里面必须包含有和选中的帧数相同数量的png图片，按文件名排序）。
	 * 2. 选择参考点图片目录（里面必须包含有和选中的帧数相同数量的png图片，按文件名排序）。
	 * 3. 选择把加入的图片添加到哪一个pip文件里去（或选择新建）。
	 * 本方法会依次对读取每一个素材文件，从中提取图片（去掉透明部分），并从对应的参考点图片中分析参考点位置（红色的点）。
	 * 操作完成后，每一帧里增加一个图块，而图块内容都加入到选中的pip文件中。
	 */
	private void onFillFrames() {
		int[] sels = framesTable.getSelectionIndices();
		if (sels == null || sels.length == 0) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", "请先选中要填充的帧。");
			return;
		}
		Arrays.sort(sels);
		
		// 选择素材目录
		String imgDirName = ImageEditor.chooseDir(getSite().getShell(), "请选择素材图片目录");
		if (imgDirName == null) {
			return;
		}
		File imgDir = new File(imgDirName);
		String[] imgArr = Utils.listFile(imgDir, "png");
		if (imgArr.length != sels.length) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", "此目录中有" + imgArr.length + "个图片文件，需要" + sels.length + "个。");
			return;
		}
		Arrays.sort(imgArr);
		
		// 选择参考点文件目录
		String refDirName = ImageEditor.chooseDir(getSite().getShell(), "请选择参考点图片目录");
		if (refDirName == null) {
			return;
		}
		File refDir = new File(refDirName);
		String[] refArr = Utils.listFile(refDir, "png");
		if (refArr.length != sels.length) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", "此目录中有" + refArr.length + "个图片文件，需要" + sels.length + "个。");
			return;
		}
		Arrays.sort(refArr);
		
		// 选择一个PIP文件
		List<String> pips = new ArrayList<String>();
		pips.add("创建新文件");
		for (int i = 0; i < animateSet.getFileCount(); i++) {
			pips.add(animateSet.getFileName(i));
		}
		GenericChooseDialog dlg = new GenericChooseDialog(getSite().getShell(), "选择", "选择添加图片的PIP文件", pips);
		if (dlg.open() != Dialog.OK) {
			return;
		}
		int targetImageIndex;
		if (dlg.getSelectionIndex() == 0) {
			// 创建新的PIP文件
			File newPipFile;
			while (true) {
				InputDialog idlg = new InputDialog(getSite().getShell(), "新建图片", "请输入图片文件名：", "", new IInputValidator() {
					public String isValid(String newText) {
						if (newText.trim().length() == 0) {
							return "文件名不能为空。";
						} else {
							return null;
						}
					}
				});
				if (idlg.open() != Dialog.OK) {
					return;
				}
				String newPipName = idlg.getValue().trim();
				if (!newPipName.toLowerCase().endsWith(".pip")) {
					newPipName += ".pip";
				}
				newPipFile = new File(animateFile.getParentFile(), newPipName);
				if (newPipFile.exists()) {
					String msg = newPipName + "已经存在，是否覆盖？";
					if (!MessageDialog.openConfirm(getSite().getShell(), "覆盖", msg)) {
						continue;
					}
				}
				break;
			}
			try {
				PipImage newPip = new PipImage();
				newPip.setSupportMoreColors(true);
				newPip.save(newPipFile);
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				return;
			}
			try {
				animateSet.addSourceFile(newPipFile.getAbsolutePath());
				imagesViewer.refresh();
				imagesTable.setSelection(animateSet.getFileCount() - 1);
				imageSelectionChanged();
				frameSelectionChanged();
				setDirty(true);
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				return;
			}
			targetImageIndex = animateSet.getFileCount() - 1;
		} else {
			targetImageIndex = dlg.getSelectionIndex() - 1;
		}
		
		// 选择参考点位置
		String[] options = { "以挂接点为参考点(用于制作装备动画)", "以世界原点为参考点(用于制作素体动画)" };
		GenericChooseDialog cdlg = new GenericChooseDialog(getSite().getShell(), "选择", "请选择参考点提取方式：", options);
		if (cdlg.open() != Dialog.OK) {
			return;
		}
		int type = cdlg.getSelectionIndex();
		
		// 执行分析操作
		try {
			fillFrames(sels, imgDir, imgArr, refDir, refArr, targetImageIndex, type);
			animateSet.setFileModified(targetImageIndex);
			setDirty(true);
			MessageDialog.openInformation(getSite().getShell(), "成功", "已处理" + sels.length + "帧。");
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			return;
		}
	}
	
	/*
	 * 从指定的图片目录和参考图片目录中读取图片文件，提取其中的图块，添加到选中的帧和pip文件里。
	 * @param frameIdx 选中的帧ID，从小到大排列
	 * @param imgDir 素材图片所在目录
	 * @param imgNames 素材图片文件名
	 * @param refDir 参考点图片所在目录
	 * @param refNames 参考点图片文件名
	 * @param pipIndex 输出pip文件在动画文件中的索引
	 * @param type 参考点提取方式，0表示提取挂接点，1表示提取世界原点
	 */
	private void fillFrames(int[] frameIdx, File imgDir, String[] imgNames, File refDir, String[] refNames, int pipIndex, int type) throws Exception {
		PipImage targetPip = animateSet.getSourceImage(pipIndex);
		int picIndex = targetPip.getImgCount();
		for (int i = 0; i < imgNames.length; i++) {
			// 提取文件，fetchPos保存提取出的图片在原图中的坐标
			int[] fetchPos = AutoBody.fetchFrameImage(new File(imgDir, imgNames[i]), targetPip);
			animateSet.getAutoFetchImageInfo().setSourceFile(animateSet.getFileName(pipIndex), 
					targetPip.getImgCount() - 1, imgNames[i]);
			
			// 在参考点文件中查找挂接点位置作为参考点，保存到hookPos
			int[] hookPos;
			if (type == 0) {
				hookPos = AutoBody.findHookPoint(new File(refDir, refNames[i]));
			} else {
				hookPos = AutoBody.findRefPoint(new File(refDir, refNames[i]));
			}
			
			// 创建一个新图块，它的位置应该是fetchPos - hookPos
			PipAnimateFrame frame = animateSet.getFrame(frameIdx[i]);
			PipAnimateFramePiece piece = frame.addPiece(pipIndex, picIndex + i);
			piece.setDx(fetchPos[0] - hookPos[0]);
			piece.setDy(fetchPos[1] - hookPos[1]);
			animateSet.syncMirrorFrame(frame);
		}
	}
	
	/*
	 * 重命名选中的一组帧。
	 */
	private void onRenameFrames() {
		int[] sels = framesTable.getSelectionIndices();
		if (sels == null || sels.length == 0) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", "请先选中要重命名的帧。");
			return;
		}
		Arrays.sort(sels);
		
		InputDialog dlg = new InputDialog(getSite().getShell(), "重命名帧", "请输入帧标题：", "", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "标题不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		String newname = dlg.getValue();
		for (int i = 0; i < sels.length; i++) {
			animateSet.getFrame(sels[i]).setName(newname + (i + 1));
		}
		framesViewer.refresh();
		setDirty(true);
	}
	
	/*
	 * 启动图块智能优化。本算法比较当前选中的pip文件中的所有图块，找出其中比较近似的，可能被合并的图块，让美术选择
	 * 是否合并。在合并之前，会让操作者看到合并前和合并后的对比效果。
	 */
	private void onOptimizeImage() {
		// 取得选中的图片文件
		int sel = imagesTable.getSelectionIndex();
		if (sel == -1) {
			MessageDialog.openError(getSite().getShell(), "错误", "请选择要优化的图片文件。");
			return;
		}
		PipImage targetImage = animateSet.getSourceImage(sel);
		
		// 分析图片文件，两两比对图块之间的相似度
		List<int[][]> frameDatas = new ArrayList<int[][]>();
		for (int i = 0; i < targetImage.getImgCount(); i++) {
			frameDatas.add(targetImage.getFramePixels(i));
		}
		double[][] matchRate = ImageUtil.compareFrames(frameDatas);
		
		// 找出可能的替换方案
		int[][] replaces;
		double[][] replaceMatchRate;
		Object[] tmpArr = analyzeMatchRate(matchRate);
		replaces = (int[][])tmpArr[0];
		replaceMatchRate = (double[][])tmpArr[1];
		
		// 打开对话框开始预览合并
		while (true) {
			// 创建所有帧的图片
			Image[] frameImages = new Image[frameDatas.size()];
			for (int i = 0; i < frameDatas.size(); i++) {
				frameImages[i] = ImageUtil.getFrameThumb(targetImage, getSite().getShell().getDisplay(), i, 64, 64);
			}
			
			ImageFrameReplaceDialog dlg;
			try {
				dlg = new ImageFrameReplaceDialog(getSite().getShell(), animateSet, sel, 
						frameDatas, frameImages, replaces, replaceMatchRate);
				dlg.open();
			} catch (IllegalArgumentException e) {
				MessageDialog.openError(getSite().getShell(), "错误", "没有符合条件可能被合并的图块。");
				return;
			} finally {
				// 操作完成，释放之前创建的图片帧
				for (int i = 0; i < frameImages.length; i++) {
					if (frameImages[i] != null) {
						frameImages[i].dispose();
					}
				}
			}
			if (dlg.opmode == 0) {
				// 用户选择取消
				break;
			}
			
			// 替换所有帧
			int count = 0;
			List<Integer> rframes = dlg.replaceSourceFrames;
			for (int i = 0; i < rframes.size(); i++) {
				// 替换帧
				int source = rframes.get(i);   // 被替换的帧
				int target = dlg.replaceTargetFrames.get(i);   // 替换的目标帧
				int adjustX = dlg.adjustXs.get(i);
				int adjustY = dlg.adjustYs.get(i);
				count += replaceSingleFrame(sel, source, sel, target, adjustX, adjustY);
			}
			
			// 准备删除图块，分析删除前和删除后的ID变化表
			Map<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
			int fcount = targetImage.getImgCount();
			int pcount = targetImage.getImagePalettes().size(); 
			for (int i = 0; i < fcount; i++) {
				int dcount = 0;
				for (int j = 0; j < rframes.size(); j++) {
					if (i > rframes.get(j)) {
						dcount++;
					} else if (i == rframes.get(j)) {
						dcount = -1;
						break;
					}
				}
				if (dcount != -1) {
					for (int j = 0; j < pcount; j++) {
						frameMap.put(j * fcount + i, j * (fcount - rframes.size()) + i - dcount);
					}
				}
			}
			
			// 删除所有图块，并对动画中的图块引用进行调整
			int[] opFids = new int[rframes.size()];
			for (int i = 0; i < rframes.size(); i++) {
				opFids[i] = rframes.get(i);
			}
			Arrays.sort(opFids);
			for (int i = opFids.length - 1; i >= 0; i--) {
				targetImage.getImageDatas().remove(opFids[i]);
			}
			animateSet.setFileModified(sel);
			animateSet.adjustSourceFrame(sel, frameMap);
			
			this.setDirty(true);
			imagesViewer.refresh();
	        framesViewer.refresh();
	        imageViewer.refresh();
	        animateFrameEditor.redraw();
	        
			// 从比较数据中删除被合并的帧的数据，并继续合并
	        for (int i = opFids.length - 1; i >= 0; i--) {
				frameDatas.remove(opFids[i]);
				matchRate = deleteColAndRow(matchRate, opFids[i]);
	        }
			tmpArr = analyzeMatchRate(matchRate);
			replaces = (int[][])tmpArr[0];
			replaceMatchRate = (double[][])tmpArr[1];
			
			if (!MessageDialog.openConfirm(getSite().getShell(), "成功", 
					"已完成合并，替换" + count + "个引用，删除" + rframes.size() + 
					"个图块。是否继续进行优化？")) {
				break;
			}
		}
	}
	
	// 从二维数组中删除一行和一列
	private double[][] deleteColAndRow(double[][] arr, int index) {
		double[][] ret = new double[arr.length - 1][];
		System.arraycopy(arr, 0, ret, 0, index);
		System.arraycopy(arr, index + 1, ret, index, arr.length - index - 1);
		for (int i = 0; i < ret.length; i++) {
			double[] tmp1 = ret[i];
			double[] tmp2 = new double[tmp1.length - 1];
			System.arraycopy(tmp1, 0, tmp2, 0, index);
			System.arraycopy(tmp1, index + 1, tmp2, index, arr.length - index - 1);
			ret[i] = tmp2;
		}
		return ret;
	}
	
	// 分析图片比较结果，从中找出可能的替换。
	private Object[] analyzeMatchRate(double[][] matchRate) {
		// 找出每一个图块可能的替换图块（匹配度超过70%，取最高的5个）
		int[][] replaces = new int[matchRate.length][];   // 存储每个图块的替换目标
		double[][] replaceMatchRate = new double[matchRate.length][];  // 存储每个图块的替换图块相似度
		for (int i = 0; i < matchRate.length; i++) {
			// 取出和这一帧匹配度超过70%的帧
			List<Integer> cands = new ArrayList<Integer>();
			for (int j = 0; j < matchRate.length; j++) {
				if (matchRate[i][j] > 0.7) {
					cands.add(j);
				}
			}
			
			// 按匹配度排序
			for (int j = 0; j < cands.size() - 1; j++) {
				for (int k = j + 1; k < cands.size(); k++) {
					if (matchRate[i][cands.get(j)] < matchRate[i][cands.get(k)]) {
						int temp = cands.get(j);
						cands.set(j, cands.get(k));
						cands.set(k, temp);
					}
				}
			}
			
			// 最多取前5个
			if (cands.size() > 5) {
				cands = cands.subList(0, 5);
			}
			
			// 填入replaces和replaceMatchRate数组
			replaces[i] = new int[cands.size()];
			replaceMatchRate[i] = new double[cands.size()];
			for (int j = 0; j < cands.size(); j++) {
				replaces[i][j] = cands.get(j);
				replaceMatchRate[i][j] = matchRate[i][cands.get(j)];
			}
		}
		return new Object[] { replaces, replaceMatchRate };
	}
	
	// 替换所有帧中的一个图块为另外一个图块
	private int replaceSingleFrame(int srcImg, int srcFrame, int tgtImg, int tgtFrame, int adjustX, int adjustY) {
		int count = 0;
		for (int i = 0; i < animateSet.getFrameCount(); i++) {
			PipAnimateFrame frameObj = animateSet.getFrame(i);
			for (int j = 0; j < frameObj.getPieceCount(); j++) {
				PipAnimateFramePiece piece = frameObj.getPiece(j);
				if (piece.getImageID() == srcImg && piece.getFrame() == srcFrame) {
					piece.setImageID(tgtImg);
					piece.setFrame(tgtFrame);
					piece.setDx(piece.getDx() + adjustX);
					piece.setDy(piece.getDy() + adjustY);
					count++;
				}
			}
		}
		return count;
	}
}
