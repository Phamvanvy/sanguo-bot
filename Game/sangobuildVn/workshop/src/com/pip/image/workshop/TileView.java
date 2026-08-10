package com.pip.image.workshop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.ImageViewerListener;
import com.pip.image.workshop.editor.TileLibSelector;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.data.ProjectOwner;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.util.PropertiesEx;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;

public class TileView extends ViewPart implements ImageViewerListener {
	public static final String ID = "com.pip.image.workshop.TileView"; //$NON-NLS-1$

	private Object image;
	private File imageFile;
	private int tileWidth;
	private int tileHeight;

	private TileLibSelector tileViewer;
	private Spinner spinWidth, spinHeight;

	public int curFileHashCode;
	public String curFilePath;
	
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 4;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		parent.setLayout(gridLayout);

		final Composite viewerContainer = new Composite(parent, SWT.NONE);
		viewerContainer.setLayout(new FillLayout());
		viewerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		final Label label = new Label(parent, SWT.NONE);
		final GridData gd_label = new GridData();
		gd_label.horizontalIndent = 5;
		label.setLayoutData(gd_label);
		label.setText("贴图宽度：");

		spinWidth = new Spinner(parent, SWT.BORDER);
		spinWidth.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tileWidth = spinWidth.getSelection();
				setupViewer();
			}
		});
		spinWidth.setMinimum(4);
		spinWidth.setMaximum(320);
		final GridData gd_spinWidth = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinWidth.setLayoutData(gd_spinWidth);

		final Label label_1 = new Label(parent, SWT.NONE);
		final GridData gd_label_1 = new GridData();
		gd_label_1.horizontalIndent = 5;
		label_1.setLayoutData(gd_label_1);
		label_1.setText("贴图高度：");

		spinHeight = new Spinner(parent, SWT.BORDER);
		spinHeight.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tileHeight = spinHeight.getSelection();
				setupViewer();
			}
		});
		spinHeight.setMinimum(4);
		spinHeight.setMaximum(320);
		final GridData gd_spinHeight = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinHeight.setLayoutData(gd_spinHeight);
		
		tileViewer = new TileLibSelector(viewerContainer, SWT.NONE);
		tileViewer.setImageViewerListener(this);
		
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
	}

	public void setFocus() {
		// Set the focus
	}
	
	private Object loadImage(String file) throws Exception {
		try {
			if (file.toLowerCase().endsWith(".pip")) {
				PipImage pimg = new PipImage();
				pimg.load(file);
				return pimg;
			} else if (file.toLowerCase().endsWith(".cts")) {
			    PipAnimateSet as = new PipAnimateSet();
			    as.load(new File(file));
			    return as;
			} else if (file.toLowerCase().endsWith(".ldf")) {
			    LandformImage pimg = new LandformImage();
                pimg.load(file);
                return pimg;
            } else {
				Image img = new Image(getSite().getShell().getDisplay(), file);
				return img;
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(), "Error", "加载图片失败!\n"+file+"\n"+e.toString());
			return new Image(getSite().getShell().getDisplay(), 1, 1);
		}
	}
	
	public void openTileFile(File file) throws Exception {
		if (image != null && image instanceof Image) {
			saveTileSetting();
		}
		Object img = loadImage(file.getAbsolutePath());
		if (image != null && image instanceof Image) {
			((Image)image).dispose();
		}
		image = img;
		imageFile = file;
		if (image instanceof Image) {
			tileWidth = 16;
			tileHeight = 8;
			loadTileSetting();
			spinWidth.setEnabled(true);
			spinHeight.setEnabled(true);
			spinWidth.setSelection(tileWidth);
			spinHeight.setSelection(tileHeight);
		} else {
			spinWidth.setEnabled(false);
			spinHeight.setEnabled(false);
		}
		
		if(setupViewer()){
			curFileHashCode = ProjectParser.getFileHashCode(file);
			curFilePath = file.getAbsolutePath();
			this.setTitleToolTip(curFilePath);
			if(img instanceof PipAnimateSet){
				File ctnFile = new File(file.getParent(), file.getName().replaceAll("\\.cts", ".ctn"));
				if(ctnFile.exists()==false){
					((PipAnimateSet)img).save(ctnFile, false);
				}
			}
		}
	}
	
	private void loadTileSetting() {
		try {
			if (imageFile != null && image != null) {
				File setFile = new File(imageFile.getParentFile(), ".tilesettings");
				PropertiesEx prop = new PropertiesEx();
				FileInputStream fis = new FileInputStream(setFile);
				prop.load(fis, "GBK");
				fis.close();
				String config = prop.getProperty(imageFile.getName());
				String[] secs = config.split(",");
				tileWidth = Integer.parseInt(secs[0]);
				if (tileWidth < 4) {
					throw new Exception();
				}
				if (tileWidth > 320) {
					throw new Exception();
				}
				tileHeight = Integer.parseInt(secs[1]);
				if (tileHeight < 4) {
					throw new Exception();
				}
				if (tileHeight > 320) {
					throw new Exception();
				}
			}
		} catch (Throwable e) {
			tileWidth = ((Image)image).getBounds().width;
			tileHeight = ((Image)image).getBounds().height;
		}
	}
	
	private void saveTileSetting() {
		if (imageFile == null || image == null) {
			return;
		}
		PropertiesEx prop = new PropertiesEx();
		File setFile = new File(imageFile.getParentFile(), ".tilesettings");
		try {
			FileInputStream fis = new FileInputStream(setFile);
			prop.load(fis, "GBK");
			fis.close();
		} catch (Throwable e) {
		}
		try {
			prop.setProperty(imageFile.getName(), tileWidth + "," + tileHeight);
			FileOutputStream fos = new FileOutputStream(setFile);
			prop.save(fos, "GBK");
			fos.close();
		} catch (Throwable e) {
		}
	}
	/**
	 * 
	 * @return view changed or not if image is null
	 */
	private boolean setupViewer() {
		if (image == null) {
			return false;
		}
		tileViewer.setInput(image, tileWidth, tileHeight);
		tileViewer.redraw();
		return true;
	}

	public void dispose() {
		if (image != null && image instanceof Image) {
			((Image)image).dispose();
			saveTileSetting();
		}
		super.dispose();
	}

	public void areaSelected(Object source) {}

	public void contentChanged(Object source) {}
	/**
	 * 检查当前显示的图片和当前地图是否在同一个项目的Data目录中
	 * @param mapEditor
	 * @return
	 */
	private boolean checkLibModeConstraints(MapEditor mapEditor){
		if(mapEditor.isLibMode()){
			File editorFilePrj = ProjectOwner.getProjectRootPath(mapEditor.getFilePath());
			File tileViewFilePrj = ProjectOwner.getProjectRootPath(curFilePath);
			return editorFilePrj.equals(tileViewFilePrj);
		}
		return true;
	}
	public void doCopyRes(String source, String dest){
		
	}
	public void frameDoubleClicked(Object source, int frame) {
		IEditorPart editor = getSite().getPage().getActiveEditor();
		if (editor != null && editor instanceof MapEditor) {
			MapEditor mapEditor = (MapEditor) editor;
			if(checkLibModeConstraints(mapEditor)==false){
				String msg = "库模式错误:\n当前显示的资源:\n" +
						curFilePath+"\n" +
						"不在当前地图文件:\n"+
						mapEditor.getFilePath() +"\n"+
						"所在项目的Data目录(或其子目录)里.\n请先移动资源.";
				MessageDialog.openInformation(getSite().getShell(), "提示", msg);	
//				boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认", msg);
//				if(ret){
//					doCopyRes(curFilePath, mapEditor.getFilePath());
//				}
				return;
			}
			int[] frames = ((TileLibSelector)source).getSelectedFrames();
			if (image instanceof LandformImage) {
			    ((MapEditor)editor).addLandform((LandformImage)image);
			    return;
			}
			if(mapEditor.isLibMode()){
				if( image instanceof Image || image instanceof PipImage){
					mapEditor.addImageRef(image,frames, tileWidth, tileHeight);
					return;
				}
			}
			for (int i = 0; i < frames.length; i++) {
				if (image instanceof Image) {
					Image img = (Image)image;
					Rectangle bounds = img.getBounds();
					int cols = bounds.width / tileWidth;
					int cellx = frames[i] % cols;
					int celly = frames[i] / cols;
					int[][] data = ImageViewer.getImageData(img, new Rectangle(cellx * tileWidth, 
							celly * tileHeight, tileWidth, tileHeight));
					((MapEditor)editor).addImage(data);
				} else if (image instanceof PipImage) {
					Image img = ((PipImage)image).getImageDraw(frames[i]).createSWTImage(getSite().getShell().getDisplay(), 0);
					int[][] data = ImageViewer.getImageData(img, img.getBounds());
					img.dispose();
					((MapEditor)editor).addImage(data);
				} else if (image instanceof PipAnimateSet) {
				    ((MapEditor)editor).addAnimate((PipAnimateSet)image, frames[i]);
				}
			}
		}
	}

	public void frameSelectionChanged(Object source, int newFrame) {}
}
