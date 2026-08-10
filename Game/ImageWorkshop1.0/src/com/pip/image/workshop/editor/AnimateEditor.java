package com.pip.image.workshop.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AnimateFrameEditor.FramePieceSelectedListener;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mango.jni.ParticleEffectPlayer;
import com.pip.mango.jni.ParticleSystemManager;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.propertysheet.StringMapping;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.DrawFrameListener;
import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.ext.HookPointExtension;
import com.pipimage.utils.ImageUtil;
import com.swtdesigner.ResourceManager;

public class AnimateEditor extends EditorPart implements ImageViewerListener, IFileModificationListener, DrawFrameListener, ImageViewerListenerEx {
	public static final int PROPERTY_CANUNDO = 1;
	public static final int PROPERTY_CANREDO = 2;
	
	private ImageDrawCache imageCache = new ImageDrawCache(1000);
	
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
			} else if (columnIndex == 1) {
				return String.valueOf(animate.getFrame(row).getDelay());
			} else {
				if (animate.getFrame(row).enableTransform) {
					return "开启插值";
				} else {
					return "";
				}
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
			if (optimizeRecords.containsKey(element)) {
				return element.toString() + "(已优化" + optimizeRecords.get(element).size() + "个图块)";
			} else {
				return element.toString();
			}
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
	private Action newFrameAction, dupFrameAction, delFrameAction, hflipFrameAction, vflipFrameAction, moveUpFrameAction, moveDownFrameAction, checkFrameRefAction, cleanPiece, cleanFrameAction, mergeToPipAction;
	private Action delAnimateFrameAction, moveUpAnimateFrameAction, moveDownAnimateFrameAction;
	private Action createSequenceAction, createFramesAction, fillFramesAction, renameFramesAction, autoFillAnimateAction;
	private Action newImageAction, editImageAction, replaceImageAction, delImageAction, renameImageAction, upImageAction, downImageAction;
	private Action saveOptimizeRecordAction, redoOptimizeAction;
	private Action splitFileAction;
	private CTabItem tabItem_1;
	private Display display;
	private AnimateEdgeEditor edgeEditor;
	private AnimateHeadEditor headEditor;
	private AnimateActionEditor actionEditor;
	private AnimateMirrorFrameEditor mirrorEditor;
	private AnimateHookPointEditor hookEditor;
	
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
	
	/*
	 * 保存图片的优化记录。value是一个有顺序的合并记录（每条记录2个int，第一个是被合并帧，第二个是合并帧，
	 * 注意前面的动作完成后，后面的ID以新的为准）。
	 */
	private HashMap<String, List<int[]>> optimizeRecords = new HashMap<String, List<int[]>>();
	
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
		animateFrameEditor.setImageCache(imageCache);

		imageViewer = new ImageViewer(sashForm_3, SWT.NONE);
		imageViewer.setFlatMode(true);
		imageViewer.setImageViewerListener(this);
		imageViewer.setImageViewerListenerEx(this);
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

		final CTabItem tabItem_6 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_6.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/enlarge.gif"));
		tabItem_6.setText("挂接点");
		
		final Composite composite_8 = new Composite(tabFolder, SWT.NONE);
		composite_8.setLayout(new FillLayout());
		tabItem_6.setControl(composite_8);
		
		hookEditor = new AnimateHookPointEditor(composite_8, SWT.NONE, this, animateSet);
		hookEditor.setImageCache(imageCache);

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
		edgeEditor.setImageCache(imageCache);

		final Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		composite_2.setLayout(new FillLayout());
		tabItem_1.setControl(composite_2);

		final SashForm sashForm_2 = new SashForm(composite_2, SWT.NONE);

		final SashForm sashForm = new SashForm(sashForm_2, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);

		animateViewer = new AnimateViewer(sashForm, SWT.NONE);
		animateViewer.setImageViewerListener(this);
		animateViewer.setImageCache(imageCache);

		frameSelector = new AnimateFrameSelector(sashForm, SWT.NONE);
		frameSelector.setImageViewerListener(this);
		frameSelector.setInput(animateSet);
		frameSelector.setImageCache(imageCache);
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
		
		final TableColumn frameExtColumn = new TableColumn(animateFramesTable, SWT.NONE);
		frameExtColumn.setWidth(100);
		
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
			SWTUtils.showError(getSite().getShell(), "错误", e);
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
					SWTUtils.showError(getSite().getShell(), "错误", "文件格式错误。", e);
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
			animateSet.setDrawFrameListener(this);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", "文件格式错误。", e);
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
			hookEditor.refreshFrameList();
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
        saveOptimizeRecordAction = new Action("保存优化记录...") {
        	public void run() {
        		saveOptimizeRecord();
        	}
        };
        redoOptimizeAction = new Action("按保存的记录优化...") {
        	public void run() {
        		optimizeByRecord();
        	}
        };
        splitFileAction = new Action("拆分文件...") {
        	public void run() {
        		splitImage();
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
        autoFillAnimateAction = new Action("全自动创建动画...") {
        	public void run() {
        		onAutoFillAnimates();
        	}
        };
        mergeToPipAction = new Action("合并所有图块") {
        	public void run() {
        		onMergeToPip();
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
		imageCache.clear();
	}
	
	/**
	 * 把当前编辑的帧合并成一张大图片，放到当前的pip文件中。
	 */
	protected void onMergeToPip() {
		int selFrame = framesTable.getSelectionIndex();
		if (selFrame == -1) {
			MessageDialog.openError(getSite().getShell(), "错误", "请选中一帧。");
			return;
		}
		int selImg = imagesTable.getSelectionIndex();
		if (selImg == -1) {
			MessageDialog.openError(getSite().getShell(), "错误", "请选中一个图片文件。");
			return;
		}
		
		PipAnimateFrame frame = animateSet.getFrame(selFrame);
		if (frame.getPieceCount() == 0) {
			MessageDialog.openError(getSite().getShell(), "错误", "选中的帧是空的。");
			return;
		}
		String message = "确认把" + frame.getName() + "合并为一个图块？此帧内目前包含" + frame.getPieceCount() + "个图块。";
		if (!MessageDialog.openConfirm(getSite().getShell(), "合并图块", message)) {
			return;
		}
		
		// 把帧画到一张图片上
		Rectangle bounds = frame.getBounds();
		Image img = new Image(getSite().getShell().getDisplay(), bounds.width, bounds.height);
		GC gc = new GC(img);
		frame.draw(gc, -bounds.x, -bounds.y, 1.0f, null);
		gc.dispose();
		
		// 把图片添加到当前选中的pip文件中，作为一个图块
		int[][] imgData = SWTUtils.getImageData(img, new com.pip.util.Rectangle(0, 0, bounds.width, bounds.height));
		PipImage targetImage = animateSet.getSourceImage(selImg);
		try {
			targetImage.addFrame(imgData);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
			return;
		}
		
		// 修改FramePiece
		while (frame.getPieceCount() > 1) {
			frame.removePiece(0);
		}
		PipAnimateFramePiece piece = frame.getPiece(0);
		piece.setImageID(selImg);
		piece.setFrame(targetImage.getImgCount() - 1);
		piece.setDx(bounds.x);
		piece.setDy(bounds.y);
		piece.setTransition(0);
		
		// 刷新显示
		imageViewer.refresh();
		animateFrameEditor.redraw();
		
		// 设置修改标志
		animateSet.setFileModified(selImg);
		setDirty(true);
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
		hookEditor.refreshFrameList();
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
        hookEditor.refreshFrameList();
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
	    hookEditor.refreshFrameList();
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
			hookEditor.refreshFrameList();
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
		hookEditor.refreshFrameList();
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
		hookEditor.refreshFrameList();
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
        hookEditor.refreshFrameList();
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
        hookEditor.refreshFrameList();
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
				SWTUtils.showError(getSite().getShell(), "错误", e);
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
        	SWTUtils.showError(getSite().getShell(), "错误", e);
            return;
        }
        
        if (newImg.getImgCount() != animateSet.getSourceImage(sel).getImgCount()) {
            String msg = "新图片有" + newImg.getImgCount() + "帧，原图片有" + animateSet.getSourceImage(sel).getImgCount() + "帧，确认？";
            if (!MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
                return;
            }
        }
        
        animateSet.replaceFile(sel, newFile, newImg);
        
        imageCache.clear();
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
            	SWTUtils.showError(getSite().getShell(), "错误", e);
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
				animateSet.setDrawFrameListener(this);
				framesViewer.setInput(animateSet);
				int idx = animatesViewer.getTable().getSelectionIndex();
				animatesViewer.setInput(animateSet);
				if(idx>=0 && idx<animateSet.getAnimateCount()){
					animateViewer.setInput(animateSet.getAnimate(idx));
					animateViewer.setCurrentFrame(0);
				}else{
					animateViewer.setInput(null);
				}
				imageCache.clear();
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
            
            // 选择匹配方式
            GenericChooseDialog cdlg = new GenericChooseDialog(getSite().getShell(), "选择帧匹配方式", "请选择", 
            		new String[] { "按帧名字匹配", "按帧序号匹配" });
            if (cdlg.open() != Dialog.OK) {
            	return;
            }
            int[] frameMapping;
            if (cdlg.getSelectionIndex() == 0) {
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
	            frameMapping = map.mapping;
            } else {
            	if (animateSet.getFrameCount() != mergeSet.getFrameCount()) {
            		MessageDialog.openError(getSite().getShell(), "错误", "只有帧数相同的动画才能用帧序号匹配方式进行合并。");
                    return;
            	}
            	frameMapping = new int[animateSet.getFrameCount()];
            	for (int i = 0; i < frameMapping.length; i++) {
            		frameMapping[i] = i + 1;
            	}
            }

            // 选择匹配方式
            cdlg = new GenericChooseDialog(getSite().getShell(), "选择动画序列匹配方式", "请选择", 
            		new String[] { "按动画序列名字匹配", "按动画序列序号匹配" });
            if (cdlg.open() != Dialog.OK) {
            	return;
            }
            int[] animateMapping;
            if (cdlg.getSelectionIndex() == 0) {
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
	            StringMapping map = new StringMapping();
	            map.init(animateNames2, animateNames1);
	            MappingDialog mdlg = new MappingDialog(getSite().getShell(), "动画序列匹配");
	            mdlg.setMapping(map);
	            if (mdlg.open() != MappingDialog.OK) {
	                return;
	            }
	            animateMapping = map.mapping;
            } else {
            	if (animateSet.getAnimateCount() != mergeSet.getAnimateCount()) {
            		MessageDialog.openError(getSite().getShell(), "错误", "只有动画序列数相同的动画才能用动画序列序号匹配方式进行合并。");
                    return;
            	}
            	animateMapping = new int[animateSet.getAnimateCount()];
            	for (int i = 0; i < animateMapping.length; i++) {
            		animateMapping[i] = i + 1;
            	}
            }
            
            getSite().getWorkbenchWindow().getActivePage().saveEditor(this, false);
            animateSet.merge(mergeSet, frameMapping, animateMapping);
            setDirty(true);
            getSite().getWorkbenchWindow().getActivePage().saveEditor(this, false);
            imagesViewer.refresh();
            framesViewer.refresh();
            hookEditor.refreshFrameList();
            animateFrameEditor.redraw();
        } catch (Exception e) {
        	SWTUtils.showError(getSite().getShell(), "错误", e);
        }
	}

    private void onSamllAnimateSet(boolean withFile) {
        animateSet.smaller(withFile);
        imageCache.clear();
        setDirty(true);
        imagesViewer.refresh();
        framesViewer.refresh();
        hookEditor.refreshFrameList();
        animateFrameEditor.redraw();
    }
    
    private void onEnlargeAnimateSet(boolean withFile) {
        animateSet.enlarge(withFile);
        imageCache.clear();
        setDirty(true);
        imagesViewer.refresh();
        framesViewer.refresh();
        hookEditor.refreshFrameList();
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
		imageCache.clear();
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
            	SWTUtils.showError(getSite().getShell(), "错误", e);
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
		EditAnimateFrameDialog dlg = new EditAnimateFrameDialog(getSite().getShell(), ref);
		if (dlg.open() == Dialog.OK) {
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
			hookEditor.refreshFrameList();
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
			mgr.add(new Separator());
			mgr.add(saveOptimizeRecordAction);
			mgr.add(redoOptimizeAction);
			mgr.add(splitFileAction);
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
		mgr.add(mergeToPipAction);
		
		MenuManager batchMenu = new MenuManager("批量处理");
		mgr.add(batchMenu);
		batchMenu.add(createSequenceAction);
		batchMenu.add(createFramesAction);
		batchMenu.add(fillFramesAction);
		batchMenu.add(renameFramesAction);
		batchMenu.add(new Separator());
		batchMenu.add(autoFillAnimateAction);
		
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
		hookEditor.refreshFrameList();
	}
	
	public void contentChanged(Object source) {
		if (source == imageViewer) {
			imageCache.clear();
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
			// 如果选中了多帧，则多帧联动
			int[] sels = framesTable.getSelectionIndices();
			if ((animateFrameEditor.allOffx != 0 || animateFrameEditor.allOffy != 0) && sels.length > 1) {
				// 找出所有受影响的切片关联的pip
				Set<Integer> affectImages = new HashSet<Integer>();
				for (PipAnimateFramePiece p : animateFrameEditor.getSelectedPiece()) {
					affectImages.add(p.getImageID());
				}
				
				// 所有选中帧中，用到这些pip的切片都关联移动
				for (int sel : sels) {
					PipAnimateFrame anf = animateSet.getFrame(sel);
					if (anf == animateFrameEditor.getFrame()) {
						continue;
					}
					for (int i = 0; i < anf.getPieceCount(); i++) {
						PipAnimateFramePiece p = anf.getPiece(i);
						if (affectImages.contains(p.getImageID())) {
							p.setDx(p.getDx() + animateFrameEditor.allOffx);
							p.setDy(p.getDy() + animateFrameEditor.allOffy);
						}
					}
					animateSet.syncMirrorFrame(anf);
				}
				animateFrameEditor.allOffx = 0;
				animateFrameEditor.allOffy = 0;
			}
			animateSet.syncMirrorFrame(animateFrameEditor.getFrame());
			this.piecesView.contentChanged(source);
		}
		setDirty(true);
	}
	
	public void onTransferFrame(AbstractImageViewer source) {
		if (source == imageViewer) {
			int[] frames = imageViewer.getSelectedFrames();
			if (frames.length == 0) {
				return;
			}
			if (animateSet.getFileCount() <= 1) {
				MessageDialog.openError(getSite().getShell(), "错误", "没有可移动的目标图片。");
				return;
			}
			List<String> targets = new ArrayList<String>();
			List<Integer> targetIndex = new ArrayList<Integer>();
			for (int i = 0; i < animateSet.getFileCount(); i++) {
				if (i == imagesTable.getSelectionIndex()) {
					continue;
				}
				targets.add(animateSet.getFileName(i));
				targetIndex.add(i);
			}
			GenericChooseDialog dlg = new GenericChooseDialog(getSite().getShell(), "选择目标", "请选择要移动的目标图片：", targets);
			if (dlg.open() != Dialog.OK) {
				return;
			}
			int index = dlg.getSelectionIndex();
			int targetImageIdx = targetIndex.get(index);
			int srcImageIdx = imagesTable.getSelectionIndex();
			PipImage srcImage = animateSet.getSourceImage(srcImageIdx);
			PipImage targetImage = animateSet.getSourceImage(targetImageIdx);
			if (srcImage.getPaletteCount() > 1 || targetImage.getPaletteCount() > 1) {
				MessageDialog.openError(getSite().getShell(), "错误", "2个图片都必须是单调色板图片才可以执行此操作。");
				return;
			}
				
			// 创建移动前后的帧号对应关系表
			Arrays.sort(frames);
			Map<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < frames.length; i++) {
				int srcKey = (srcImageIdx << 16) | frames[i];
				int dstKey = (targetImageIdx << 16) | (targetImage.getImgCount() + i);
				frameMap.put(srcKey, dstKey);
			}
			for (int i = 0; i < srcImage.getImgCount(); i++) {
				int skipCount = 0;
				boolean removed = false;
				for (int j = 0; j < frames.length; j++) {
					if (i == frames[j]) {
						removed = true;
						break;
					} else if (i > frames[j]) {
						skipCount++;
					}
				}
				if (!removed && skipCount > 0) {
					int srcKey = (srcImageIdx << 16) | i;
					int dstKey = (srcImageIdx << 16) | (i - skipCount);
					frameMap.put(srcKey, dstKey);
				}
			}
			
			// 尝试移动帧内容
			int oldFrameCount = targetImage.getImgCount();
			try {
				for (int i = 0; i < frames.length; i++) {
					Image fimg = srcImage.getImageDraw(frames[i]).createSWTImage(getSite().getShell().getDisplay(), 0);
					int[][] data = SWTUtils.getImageData(fimg, new com.pip.util.Rectangle(0, 0, fimg.getBounds().width, fimg.getBounds().height));
					targetImage.addFrame(data, srcImage, frames[i]);
				}
			} catch (Exception e) {
				// 添加帧失败，回滚
				while (targetImage.getImgCount() > oldFrameCount) {
					targetImage.getImageDatas().remove(targetImage.getImgCount() - 1);
				}
				SWTUtils.showError(getSite().getShell(), "错误", e);
				return;
			}
			for (int i = frames.length - 1; i >= 0; i--) {
				srcImage.getImageDatas().remove(frames[i]);
			}
			
			// 调整动画
			animateSet.adjustSourceFrame(frameMap);
			if (animateSet.getAutoFetchImageInfo() != null) {
				animateSet.getAutoFetchImageInfo().adjustByFrameMap(frameMap);
			}

			// 刷新view
			imageCache.clear();
			imageViewer.clearSelection();
			imageViewer.refresh();
			
			// 设置修改标志
			animateSet.setFileModified(srcImageIdx);
			animateSet.setFileModified(targetImageIdx);
			setDirty(true);
		}
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
		imageCache.clear();
		if (particleThread != null) {
			particlePlayer.stop();
			particleThread.stopRunning();
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
        	SWTUtils.showError(getSite().getShell(), "错误", e);
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
        	SWTUtils.showError(getSite().getShell(), "错误", e);
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
	        hookEditor.refreshFrameList();
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
		hookEditor.refreshFrameList();
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
				SWTUtils.showError(getSite().getShell(), "错误", e);
				return;
			}
			try {
				animateSet.addSourceFile(newPipFile.getAbsolutePath());
				imageCache.clear();
				imagesViewer.refresh();
				imagesTable.setSelection(animateSet.getFileCount() - 1);
				imageSelectionChanged();
				frameSelectionChanged();
				setDirty(true);
			} catch (Exception e) {
				SWTUtils.showError(getSite().getShell(), "错误", e);
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
			File[] imgFiles = new File[imgArr.length];
			for (int i = 0; i < imgArr.length; i++) {
				imgFiles[i] = new File(imgDir, imgArr[i]);
			}
			File[] refFiles = new File[refArr.length];
			for (int i = 0; i < refArr.length; i++) {
				refFiles[i] = new File(refDir, refArr[i]);
			}
			fillFrames(sels, imgFiles, refFiles, targetImageIndex, type);
			animateSet.setFileModified(targetImageIndex);
			setDirty(true);
			MessageDialog.openInformation(getSite().getShell(), "成功", "已处理" + sels.length + "帧。");
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
			return;
		}
	}
	
	/*
	 * 从指定的图片目录和参考图片目录中读取图片文件，提取其中的图块，添加到选中的帧和pip文件里。
	 * @param frameIdx 选中的帧ID，从小到大排列
	 * @param imgFiles 素材图片列表
	 * @param refFiles 参考点图片列表
	 * @param pipIndex 输出pip文件在动画文件中的索引
	 * @param type 参考点提取方式，0表示提取挂接点，1表示提取世界原点
	 */
	private void fillFrames(int[] frameIdx, File[] imgFiles, File[] refFiles, int pipIndex, int type) throws Exception {
		PipImage targetPip = animateSet.getSourceImage(pipIndex);
		int picIndex = targetPip.getImgCount();
		for (int i = 0; i < imgFiles.length; i++) {
			// 提取文件，fetchPos保存提取出的图片在原图中的坐标
			int[] fetchPos = AutoBody.fetchFrameImage(imgFiles[i], targetPip);
			animateSet.getAutoFetchImageInfo().setSourceFile(animateSet.getFileName(pipIndex), 
					targetPip.getImgCount() - 1, imgFiles[i].getName());
			
			// 在参考点文件中查找挂接点位置作为参考点，保存到hookPos
			int[] hookPos;
			if (type == 0) {
				hookPos = AutoBody.findHookPoint(refFiles[i]);
			} else {
				hookPos = AutoBody.findRefPoint(refFiles[i]);
			}
			
			// 创建一个新图块，它的位置应该是fetchPos - hookPos，如果在此帧中已经有一个图块，则替换之
			PipAnimateFrame frame = animateSet.getFrame(frameIdx[i]);
			PipAnimateFramePiece piece = null;
			for (int k = 0; k < frame.getPieceCount(); k++) {
				PipAnimateFramePiece p = frame.getPiece(k);
				if (p.getImageID() == pipIndex) {
					piece = p;
					break;
				}
			}
			if (piece == null) {
				piece = frame.addPiece(pipIndex, picIndex + i);
				piece.setDx(fetchPos[0] - hookPos[0]);
				piece.setDy(fetchPos[1] - hookPos[1]);
			} else {
				piece.setFrame(picIndex + i);
			}
			animateSet.syncMirrorFrame(frame);
		}
		
		// PIP图片重新填充后，清除以前的优化记录
		optimizeRecords.remove(animateSet.getFileName(pipIndex));
		imageCache.clear();
		imagesViewer.refresh();
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
		hookEditor.refreshFrameList();
		setDirty(true);
	}
	
	/*
	 * 添加一个文件的优化记录。
	 */
	private void addOptimizeRecord(int imgIndex, List<Integer> sourceFrames, List<Integer> targetFrames) {
		String name = animateSet.getFileName(imgIndex);
		int[] src = new int[sourceFrames.size()];
		int[] dest = new int[targetFrames.size()];
		for (int i = 0; i < sourceFrames.size(); i++) {
			src[i] = sourceFrames.get(i);
			dest[i] = targetFrames.get(i);
		}
		List<int[]> list = optimizeRecords.get(name);
		if (list == null) {
			list = new ArrayList<int[]>();
			optimizeRecords.put(name, list);
		}
		for (int i = 0; i < src.length; i++) {
			list.add(new int[] { src[i], dest[i] });
			for (int j = i + 1; j < src.length; j++) {
				if (src[j] > src[i]) {
					src[j]--;
				}
				if (dest[j] > src[i]) {
					dest[j]--;
				}
			}
		}
		imagesViewer.refresh();
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
		
		// 弹出选项，让用户选择是全局优化还是只限定在选中的帧进行优化
		GenericChooseDialog cdlg = new GenericChooseDialog(getSite().getShell(), "优化范围", "请选择优化帧范围：", 
				new String[] { "全部", "只优化选中帧", "只优化选中帧，且只从选中帧里寻找替代" });
		if (cdlg.open() != Dialog.OK) {
			return;
		}
		int optimizeRange = cdlg.getSelectionIndex();
		Set<Integer> inRangeFrames = new HashSet<Integer>();
		if (optimizeRange == 1 || optimizeRange == 2) {
			// 找出选中的帧包含选中图片的图块序号
			int[] sels = framesTable.getSelectionIndices();
			if (sels.length == 0) {
				MessageDialog.openError(getSite().getShell(), "错误", "请选择要优化的动画帧范围。");
				return;
			}
			for (int index : sels) {
				PipAnimateFrame frame = animateSet.getFrame(index);
				for (int i = 0; i < frame.getPieceCount(); i++) {
					PipAnimateFramePiece piece = frame.getPiece(i);
					if (piece.getImageID() == sel) {
						inRangeFrames.add(piece.getFrame());
					}
				}
			}
		}
		
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
		
		// 如果限定了优化的范围，那么把不在优化范围内的优化选项去掉
		if (optimizeRange == 1 || optimizeRange == 2) {
			for (int i = 0; i < replaces.length; i++) {
				if (!inRangeFrames.contains(i)) {
					replaces[i] = new int[0];
					replaceMatchRate[i] = new double[0];
				} else if (optimizeRange == 2 && replaces[i] != null && replaces[i].length > 0) {
					List<Integer> matchReps = new ArrayList<Integer>();
					List<Double> matchRates = new ArrayList<Double>();
					for (int j = 0; j < replaces[i].length; j++) {
						if (inRangeFrames.contains(replaces[i][j])) {
							matchReps.add(replaces[i][j]);
							matchRates.add(replaceMatchRate[i][j]);
						}
					}
					replaces[i] = new int[matchReps.size()];
					replaceMatchRate[i] = new double[matchReps.size()];
					for (int j = 0; j < matchReps.size(); j++) {
						replaces[i][j] = matchReps.get(j);
						replaceMatchRate[i][j] = matchRates.get(j);
					}
				}
			}
		}
		
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
						GLUtils.unloadImage(frameImages[i]);
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
				int adjustTrans = dlg.adjustTrans.get(i);
				count += replaceSingleFrame(sel, source, sel, target, adjustX, adjustY, adjustTrans);
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
			
			// 记录优化操作
			addOptimizeRecord(sel, dlg.replaceSourceFrames, dlg.replaceTargetFrames);
			
			this.setDirty(true);
			imageCache.clear();
			imagesViewer.refresh();
	        framesViewer.refresh();
	        hookEditor.refreshFrameList();
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
			
			// 如果限定了优化的范围，那么把不在优化范围内的优化选项去掉
			if (optimizeRange == 1 || optimizeRange == 2) {
				for (int i = 0; i < replaces.length; i++) {
					if (!inRangeFrames.contains(i)) {
						replaces[i] = new int[0];
						replaceMatchRate[i] = new double[0];
					} else if (optimizeRange == 2 && replaces[i] != null && replaces[i].length > 0) {
						List<Integer> matchReps = new ArrayList<Integer>();
						List<Double> matchRates = new ArrayList<Double>();
						for (int j = 0; j < replaces[i].length; j++) {
							if (inRangeFrames.contains(replaces[i][j])) {
								matchReps.add(replaces[i][j]);
								matchRates.add(replaceMatchRate[i][j]);
							}
						}
						replaces[i] = new int[matchReps.size()];
						replaceMatchRate[i] = new double[matchReps.size()];
						for (int j = 0; j < matchReps.size(); j++) {
							replaces[i][j] = matchReps.get(j);
							replaceMatchRate[i][j] = matchRates.get(j);
						}
					}
				}
			}
			
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
	private int replaceSingleFrame(int srcImg, int srcFrame, int tgtImg, int tgtFrame, int adjustX, int adjustY, int adjustTrans) {
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
					if (adjustTrans == 1) {
						switch (piece.getTransition()) {
						case 0:
							piece.setTransition(2);
							break;
						case 1:
							piece.setTransition(3);
							break;
						case 2:
							piece.setTransition(0);
							break;
						case 3:
							piece.setTransition(1);
							break;
						case 4:
							piece.setTransition(6);
							break;
						case 5:
							piece.setTransition(7);
							break;
						case 6:
							piece.setTransition(4);
							break;
						case 7:
							piece.setTransition(5);
							break;
						}
					}
					count++;
				}
			}
		}
		return count;
	}
	
	private void saveOptimizeRecord() {
		int sel = imagesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		String fname = animateSet.getFileName(sel);
		if (!optimizeRecords.containsKey(fname)) {
			return;
		}
		List<int[]> records = optimizeRecords.get(fname);
		if (records.size() == 0) {
			return;
		}
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
        dlg.setFilterExtensions(new String[] { "*.dat" });
        dlg.setFilterNames(new String[] { "优化记录数据文件(*.dat)" });
        String newFile = dlg.open();
        if (newFile == null) {
            return;
        }
        try {
        	FileOutputStream fos = new FileOutputStream(newFile);
        	DataOutputStream dos = new DataOutputStream(fos);
        	// 优化前帧数
        	dos.writeInt(records.size() + animateSet.getSourceImage(sel).getFrameCount());
        	// 优化帧数
        	dos.writeInt(records.size());
        	for (int[] r : records) {
        		dos.writeInt(r[0]);
        		dos.writeInt(r[1]);
        	}
        	dos.close();
        } catch (Exception e) {
        	SWTUtils.showError(getSite().getShell(), "错误", e);
        }
	}

	private void optimizeByRecord() {
		int sel = imagesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
        dlg.setFilterExtensions(new String[] { "*.dat" });
        dlg.setFilterNames(new String[] { "优化记录数据文件(*.dat)" });
        String newFile = dlg.open();
        if (newFile == null) {
            return;
        }
        
        // 读取优化记录
        List<int[]> records = new ArrayList<int[]>();
        try {
        	FileInputStream fis = new FileInputStream(newFile);
        	DataInputStream dis = new DataInputStream(fis);
        	// 优化前帧数
        	int totalFrames = dis.readInt();
        	if (totalFrames != animateSet.getSourceImage(sel).getFrameCount()) {
        		dis.close();
        		MessageDialog.openError(getSite().getShell(), "错误", "保存的优化记录和选定的图片帧数不匹配（" + totalFrames + "!=" + 
        				animateSet.getSourceImage(sel).getFrameCount() + "）。");
        		return;
        	}
        	int optCount = dis.readInt();
        	for (int i = 0; i < optCount; i++) {
        		int[] r = new int[2];
        		r[0] = dis.readInt();
        		r[1] = dis.readInt();
        		records.add(r);
        	}
        	dis.close();
        } catch (Exception e) {
        	SWTUtils.showError(getSite().getShell(), "错误", e);
        	return;
        }
        
        // 确认继续
        String msg = "优化记录中保存了" + records.size() + "帧的优化操作，是否执行？";
        if (!MessageDialog.openConfirm(getSite().getShell(), "优化", msg)) {
        	return;
        }
        
        // 进行优化
        int totalCount = 0;
        for (int[] r : records) {
        	totalCount += optimizeOneFrame(sel, r[0], r[1]);
        }
        
        // 保存优化记录
        animateSet.setFileModified(sel);
        optimizeRecords.put(animateSet.getFileName(sel), records);
        
        // 刷新显示
        imageCache.clear();
        animateSet.setFileModified(sel);
        this.setDirty(true);
		imagesViewer.refresh();
        framesViewer.refresh();
        hookEditor.refreshFrameList();
        imageViewer.refresh();
        animateFrameEditor.redraw();
        
        MessageDialog.openInformation(getSite().getShell(), "优化完成", "已完成合并，替换" + totalCount + "个引用，删除" + records.size() + "个图块。");
	}
	
	private int optimizeOneFrame(int sel, int srcFrame, int destFrame) {
		PipImage targetImage = animateSet.getSourceImage(sel);
		
		// 分析图片文件，找出最恰当的偏移量
		int[][] srcFrameData = targetImage.getFramePixels(srcFrame);
		int[][] destFrameData = targetImage.getFramePixels(destFrame);
		int[] off = new int[3];
		ImageUtil.compareFrame(srcFrameData, destFrameData, off);
		int count = replaceSingleFrame(sel, srcFrame, sel, destFrame, off[0], off[1], off[2]);
		
		// 准备删除图块，分析删除前和删除后的ID变化表
		Map<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
		int fcount = targetImage.getImgCount();
		int pcount = targetImage.getImagePalettes().size(); 
		for (int i = 0; i < fcount; i++) {
			int dcount = 0;
			if (i > srcFrame) {
				dcount = 1;
			} else if (i == srcFrame) {
				dcount = -1;
			}
			if (dcount != -1) {
				for (int j = 0; j < pcount; j++) {
					frameMap.put(j * fcount + i, j * (fcount - 1) + i - dcount);
				}
			}
		}
		
		// 删除图块，并对动画中的图块引用进行调整
		targetImage.getImageDatas().remove(srcFrame);
		animateSet.adjustSourceFrame(sel, frameMap);
		
		return count;
	}
	
	private void splitImage() {
		int sel = imagesTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		String msg = "把选定的文件按占用内存拆分成多个，请输入单个文件最大占用内存（单位KB）（占用内存=宽度x高度x4，kb）：";
		InputDialog dlg = new InputDialog(getSite().getShell(), "输入", msg, "1024", new IInputValidator() {
			public String isValid(String value) {
				try {
					int val = Integer.parseInt(value);
					if (val < 100) {
						return "太小了";
					} else if (val > 10240) {
						return "太大了";
					}
					return null;
				} catch (Exception e) {
					return "请输入整数。";
				}
			}
		});
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		int maxMem = Integer.parseInt(dlg.getValue()) * 1024;
		
		// 分析文件，看看需要拆分成多少个文件
		int splitCount = 0;
		int sizeTemp = 0;
		PipImage allImage = animateSet.getSourceImage(sel);
		List<Integer> splitPoint = new ArrayList<Integer>();
		for (int i = 0; i < allImage.getImgCount(); i++) {
			sizeTemp += (int)allImage.getImageData(i).getWidth() * (int)allImage.getImageData(i).getHeight() * 4;
			if (sizeTemp >= maxMem) {
				splitCount++;
				splitPoint.add(i + 1);
				sizeTemp = 0;
			}
		}
		if (sizeTemp > 0) {
			splitCount++;
			splitPoint.add(allImage.getImgCount());
		}
		if (splitCount <= 1) {
			MessageDialog.openInformation(getSite().getShell(), "消息", "文件无需拆分。");
			return;
		}
		msg = "将把这个图片文件拆分成" + splitCount + "个，是否确定拆分？";
		if (!MessageDialog.openConfirm(getSite().getShell(), "拆分", msg)) {
			return;
		}
		
		// 开始拆分
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			allImage.save(new DataOutputStream(bos), true);
			byte[] fdata = bos.toByteArray();
			
			String fileName = animateSet.getFileName(sel);
			if (fileName.contains(".")) {
				fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			}
			HashMap<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < splitCount - 1; i++) {
				String newName = fileName + "_" + (i + 1);
				while (animateSet.containsFile(newName + ".pip")) {
					newName += "_" + (i + 1);
				}
				newName = newName + ".pip";
				PipImage newImg = new PipImage();
				newImg.load(new ByteArrayInputStream(fdata));
				animateSet.addSourceFile(newName, newImg);
				animateSet.setFileModified(animateSet.getFileCount() - 1);
				
				int startIndex = splitPoint.get(i);
				int endIndex = splitPoint.get(i + 1);
				int count = newImg.getImgCount() - endIndex;
				for (int j = 0; j < count; j++) {
					newImg.getImageDatas().remove(endIndex);
				}
				for (int j = startIndex - 1; j >= 0; j--) {
					newImg.getImageDatas().remove(0);
				}
				for (int j = startIndex; j < endIndex; j++) {
					int srcID = (sel << 16) | j;
					int destID = ((animateSet.getFileCount() - 1) << 16) | (j - startIndex);
					frameMap.put(srcID, destID);
				}
			}
			for (int i = allImage.getImgCount() - 1; i >= splitPoint.get(0); i--) {
				allImage.getImageDatas().remove(i);
			}
			animateSet.setFileModified(sel);
			animateSet.adjustSourceFrame(frameMap);
	        this.setDirty(true);
	        imageCache.clear();
			imagesViewer.refresh();
	        framesViewer.refresh();
	        hookEditor.refreshFrameList();
	        imageViewer.refresh();
	        animateFrameEditor.redraw();
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
			return;
		}
	}

	private static class ParticleDriverThread extends Thread {
		protected ParticleSystemManager manager;
		
		public ParticleDriverThread(ParticleSystemManager mgr) {
			manager = mgr;
		}
		
		public ParticleSystemManager getManager() {
			return manager;
		}
		
		public void stopRunning() {
			manager.destroy();
			manager = null;
		}
		
		public void run() {
			while (manager != null) {
				try {
					Thread.sleep(Settings.animateFrameDelay);
				} catch (Exception e) {
				}
				if (manager != null) {
					synchronized (manager) {
						manager.update(Settings.animateFrameDelay / 1000.0f);
					}
				}
			}
		}
	}
	private ParticleDriverThread particleThread = null;
	private ParticleEffectPlayer particlePlayer = null;
	private String hookName = null;
	private boolean hookLoop;
	private String hookTemplateName;
	
	/**
	 * 从psdata文件中选择一个粒子效果，绑定到某一个挂接点上。
	 * @param hookPoint
	 */
	public void hookParticleEffect(HookPointExtension.HookPoint hookPoint) {
		if (particleThread != null) {
			if (particlePlayer != null) {
				particlePlayer.stop();
				particlePlayer = null;
			}
			particleThread.stopRunning();
			particleThread = null;
		}
		
		// 选择psdata文件
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.psdata" });
		dlg.setFilterNames(new String[] { "粒子效果文件(*.psdata)" });
		String path = dlg.open();
		if (path == null) {
			return;
		}
		
		// 载入psdata文件选择模板
		ParticleSystemManager manager = new ParticleSystemManager();
		manager.loadTemplates(path);
		String names = manager.getRootNames();
		if (names.length() == 0) {
			MessageDialog.openError(getSite().getShell(), "错误", "此psdata文件中不包含任何可用的粒子系统模板。");
			manager.destroy();
			return;
		}
		String[] rootNames = names.split(",");
		GenericChooseDialog cdlg = new GenericChooseDialog(getSite().getShell(), "选择", "粒子效果模板：", rootNames);
		if (cdlg.open() != Dialog.OK) {
			manager.destroy();
			return;
		}
		hookTemplateName = (String)cdlg.getSelection();
		cdlg = new GenericChooseDialog(getSite().getShell(), "选择", "是否循环播放：", new String[] { "是", "否" });
		if (cdlg.open() != Dialog.OK) {
			manager.destroy();
			return;
		}
		hookLoop = cdlg.getSelectionIndex() == 0;
		
		if (hookLoop) {
			particlePlayer = new ParticleEffectPlayer(manager, hookTemplateName, 0, 0, false);
			particlePlayer.setLoop(true);
		}
		this.hookName = hookPoint.name;
		particleThread = new ParticleDriverThread(manager);
		particleThread.start();
	}
	
	public boolean beforeDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GC g, int x, int y, double ratio) {
		return true;
	}
	
	public void afterDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GC g, int x, int y, double ratio) {
	}
	
	public boolean beforeDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GLGraphics g, int x, int y, double ratio) {
		if (particleThread != null) {
			HookPointExtension.HookPoint hp = as.getHookPoints().findHookPoint(hookName);
			if (hp == null) {
				if (!hookLoop && particlePlayer != null) {
					particlePlayer.stop();
					particlePlayer = null;
				}
				return true;
			}
			HookPointExtension.Position pos = hp.posList.get(frame);
			if (pos == null) {
				if (!hookLoop && particlePlayer != null) {
					particlePlayer.stop();
					particlePlayer = null;
				}
				return true;
			}
			if (!hookLoop && particlePlayer != null && particlePlayer.isEnd()) {
				particlePlayer.stop();
				particlePlayer = null;
			}
			if (pos.direction >= 1000) {
				return true;
			}
			if (particlePlayer == null) {
				particlePlayer = new ParticleEffectPlayer(particleThread.getManager(), hookTemplateName, 0, 0, false);
			}
			
			// 绘制先画的粒子效果
			float scale = g.getScale();
			g.setScale((float)ratio);
			int degree = pos.direction - 90;
			synchronized (particlePlayer.manager) {
				particlePlayer.setPosition((int)(x / ratio + pos.x), (int)(y / ratio + pos.y));
				particlePlayer.setRotation(30, 0, degree);
				particlePlayer.draw(g.getHandle(), 0, 0);
			}
			g.setScale(scale);
		}
		return true;
	}
	
	public void afterDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GLGraphics g, int x, int y, double ratio) {
		if (particleThread != null) {
			HookPointExtension.HookPoint hp = as.getHookPoints().findHookPoint(hookName);
			if (hp == null) {
				if (!hookLoop && particlePlayer != null) {
					particlePlayer.stop();
					particlePlayer = null;
				}
				return;
			}
			HookPointExtension.Position pos = hp.posList.get(frame);
			if (pos == null) {
				if (!hookLoop && particlePlayer != null) {
					particlePlayer.stop();
					particlePlayer = null;
				}
				return;
			}
			if (!hookLoop && particlePlayer != null && particlePlayer.isEnd()) {
				particlePlayer.stop();
				particlePlayer = null;
			}
			if (pos.direction < 1000) {
				return;
			}
			if (particlePlayer == null) {
				particlePlayer = new ParticleEffectPlayer(particleThread.getManager(), hookTemplateName, 0, 0, false);
			}
			
			// 绘制后画的粒子效果
			float scale = g.getScale();
			g.setScale((float)ratio);
			int degree = pos.direction - 1090;
			synchronized (particlePlayer.manager) {
				double x1 = x / ratio + pos.x;
				double y1 = y / ratio + pos.y;
				particlePlayer.setPosition((int)x1, (int)y1);
				particlePlayer.setRotation(30, 0, degree);
				particlePlayer.draw(g.getHandle(), 0, 0);
			}
			g.setScale(scale);
		}
	}
	
	/*
	 * 根据一个目录下子目录中的多个部位图片自动创建动画帧和动画序列。
	 * 每个部位2个子目录，一个存储此部位的所有帧图片，一个存储每个图片对应的挂接点信息。
	 * 存储帧图片的目录名为：b-<部位名称>，例如b-arms
	 * 存储挂接点信息的目录名为：s-<部位名称>，例如s-arms
	 * 帧图片的文件名为：b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png，例如b-1_待机_arms_20.png
	 * 挂接点信息文件名为：s-<动作序号>_<动作名称>_<部位名称>_<帧号>.png，例如s-1_待机_arms_20.png
	 * 
	 * 本操作会从目录信息中检测出素体部位和动作，每个部位创建一个pip图片，每个动作创建一个动画序列。
	 */
	private void onAutoFillAnimates() {
		// 选择素材目录
		String imgDirName = ImageEditor.chooseDir(getSite().getShell(), "请选择素材图片目录");
		if (imgDirName == null) {
			return;
		}
		File imgDir = new File(imgDirName);
		AutoFillAnimatesInfo info;
		try {
			info = new AutoFillAnimatesInfo(imgDir);
		} catch (Exception e) {
			MessageDialog.openConfirm(getSite().getShell(), "提示", e.toString());
			return;
		}
		
		// 选择参考点位置
		String[] options = { "以挂接点为参考点(用于制作装备动画)", "以世界原点为参考点(用于制作素体动画)" };
		GenericChooseDialog cdlg = new GenericChooseDialog(getSite().getShell(), "选择", "请选择参考点提取方式：", options);
		if (cdlg.open() != Dialog.OK) {
			return;
		}
		int type = cdlg.getSelectionIndex();
		
		// 自动创建帧和动画序列，数量为所有动作帧数之和
		try {
			for (int i = 0; i < info.animateNames.length; i++) {
				PipAnimate ani = animateSet.addAnimate(info.animateNames[i]);
				int base = animateSet.getFrameCount();
				int[] ids = new int[info.animateFrameCounts[i]];
				for (int j = 0; j < info.animateFrameCounts[i]; j++) {
					animateSet.addFrame(info.animateNames[i] + (j + 1));
					ids[j] = base + j;
					ani.addFrame(base + j);
				}
				
				// 为每个部位填充帧内容，每个部位添加一个图片文件
				for (int j = 0; j < info.partNames.length; j++) {
					int pipIndex = findOrCreateImage(info.partNames[j] + ".pip");
					fillFrames(ids, info.getImageFiles(j, i), info.getAnchorFiles(j, i), pipIndex, type);
					animateSet.setFileModified(pipIndex);
				}
			}
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
			return;
		}
		framesViewer.refresh();
		hookEditor.refreshFrameList();
		animatesViewer.refresh();
		imagesViewer.refresh();
		setDirty(true);
		MessageDialog.openInformation(getSite().getShell(), "成功", "操作完成，共导入" + info.partNames.length + "个部位，" + 
				info.animateNames.length + "个动作，" + info.getTotalFrames() + "帧。");
	}
	
	private int findOrCreateImage(String name) throws Exception {
		for (int i = 0; i < animateSet.getFileCount(); i++) {
			if (animateSet.getFileName(i).equals(name)) {
				return i;
			}
		}
		
		File newPipFile = new File(animateFile.getParentFile(), name);
		PipImage newPip = new PipImage();
		newPip.setSupportMoreColors(true);
		newPip.save(newPipFile);
		animateSet.addSourceFile(newPipFile.getAbsolutePath()); 
		return animateSet.getFileCount() - 1;
	}
}
