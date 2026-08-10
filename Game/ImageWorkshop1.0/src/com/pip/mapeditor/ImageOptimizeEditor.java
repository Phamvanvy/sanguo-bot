package com.pip.mapeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.GenericChooseDialog;
import com.pip.image.workshop.editor.CompressTextureOptionDialog;
import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.JPEGMergeOptionDialog;
import com.pip.util.Rectangle;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.CompressTextureOption;
import com.pipimage.image.JPEGMergeOption;
import com.pipimage.image.MergeAreaTest;
import com.pipimage.image.PackageFile;
import com.pipimage.image.PackageFileItem;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.png.PngEncoder;
import com.pipimage.utils.GZIP;

public class ImageOptimizeEditor extends EditorPart {
	class FileListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (sourceFile == null) {
				return new Object[0];
			}
			if (packageFile == null) {
				// 当前打开的一个pip文件
				return new Object[] { currentFileName };
			} else {
				PackageFileItem[] fileItems = packageFile.getFiles();
				ArrayList<String> pipNames = new ArrayList<String>();
				for (PackageFileItem fi : fileItems) {
					if (fi.name.toLowerCase().endsWith(".pip")) {
						pipNames.add(fi.name);
					} else if (fi.name.toLowerCase().endsWith(".anp")) {
						try {
							PackageFile anp = new PackageFile();
							anp.load(new ByteArrayInputStream(fi.data));
							PackageFileItem[] fileItems2 = anp.getFiles();
							for (PackageFileItem fi2 : fileItems2) {
								if (fi2.name.toLowerCase().endsWith(".pip")) {
									pipNames.add(fi.name + "/" + fi2.name);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				return pipNames.toArray();
			}
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private List fileList;
	public static final String ID = "com.pip.mapeditor.ImageOptimizeEditor"; //$NON-NLS-1$
	
	private File sourceFile;
	private PackageFile packageFile;
	private String currentFileName;
	private byte[] currentFileData;
	private PipImage image;
	private ListViewer fileListViewer;
	private ImageViewer previewer;
	private Label formatLabel;
	
	private Image previewImage;
	private byte[] sourceImageData;
	private byte[] changedFileData;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);

		final Button openButton = new Button(container, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onOpen();
			}
		});
		openButton.setText("打开文件");

		final Button buttonReorgPkg = new Button(container, SWT.NONE);
		buttonReorgPkg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (sourceFile == null || packageFile == null) {
					MessageDialog.openError(getSite().getShell(), "错误", "请先打开一个pkg文件。");
					return;
				}
				
				// 找出关卡里的anp文件
				PackageFileItem[] fileItems = packageFile.getFiles();
				ArrayList<String> pipNames = new ArrayList<String>();
				PackageFileItem anpItem = null;
				for (PackageFileItem fi : fileItems) {
					if (fi.name.toLowerCase().endsWith(".anp")) {
						anpItem = fi;
					}
				}
				if (anpItem == null) {
					MessageDialog.openError(getSite().getShell(), "错误", "这个pkg文件中没有找到anp文件。");
					return;
				}
				
				try {
					// 解析anp文件
					PackageFile anpPkg = new PackageFile();
					anpPkg.load(new ByteArrayInputStream(anpItem.data));
					
					// 打开界面，对anp文件进行重组
					ReorgANPDialog dlg = new ReorgANPDialog(getSite().getShell(), anpPkg);
					if (dlg.open() != Dialog.OK) {
						return;
					}
					
					// 保存
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					anpPkg.save(bos);
					anpItem.data = bos.toByteArray();
					long oldSize = sourceFile.length();
					packageFile.save(sourceFile);
					long newSize = sourceFile.length();
					
					currentFileName = null;
					currentFileData = null;
					changedFileData = null;
					image = null;
					fileListViewer.refresh();
					fileListViewer.setSelection(new StructuredSelection());
					refreshPreviewer();
					
					MessageDialog.openInformation(getSite().getShell(), "成功", "保存成功，保存前" + oldSize + "字节，保存后" + newSize + "字节。");
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		buttonReorgPkg.setText("重组关卡文件");

		final Label filePathLabel = new Label(container, SWT.NONE);
		final GridData gd_filePathLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filePathLabel.setLayoutData(gd_filePathLabel);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		composite.setLayout(gridLayout_1);

		fileListViewer = new ListViewer(composite, SWT.BORDER);
		fileListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent arg0) {
				if (sourceFile != null && packageFile != null) {
					StructuredSelection sel = (StructuredSelection)fileListViewer.getSelection();
					if (!sel.isEmpty()) {
						try {
							currentFileName = (String)sel.getFirstElement();
							if (currentFileName.contains("/")) {
								String[] secs = currentFileName.split("/");
								PackageFile newPkg = new PackageFile();
								newPkg.load(new ByteArrayInputStream(packageFile.findFile(secs[0])));
								currentFileData = newPkg.findFile(secs[1]);
							} else {
								currentFileData = packageFile.findFile(currentFileName);
							}
							changedFileData = currentFileData;
							image = new PipImage();
							image.load(new ByteArrayInputStream(currentFileData));
							refreshPreviewer();
							firePropertyChange(PROP_DIRTY);
						} catch (Exception e) {
							SWTUtils.showError(getSite().getShell(), "错误", e);
						}
					}
				}
			}
		});
		fileListViewer.setContentProvider(new FileListContentProvider());
		fileList = fileListViewer.getList();
		final GridData gd_fileList = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2);
		gd_fileList.widthHint = 200;
		fileList.setLayoutData(gd_fileList);
		fileListViewer.setInput(this);

		final Composite imageViewerComposite = new Composite(composite, SWT.NONE);
		final GridData gd_imageViewerComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		imageViewerComposite.setLayoutData(gd_imageViewerComposite);
		imageViewerComposite.setLayout(new FillLayout());
		
		previewer = new ImageViewer(imageViewerComposite, SWT.NONE);

		final Composite buttonComposite = new Composite(composite, SWT.NONE);
		final GridData gd_buttonComposite = new GridData(SWT.FILL, SWT.FILL, false, false);
		buttonComposite.setLayoutData(gd_buttonComposite);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 3;
		buttonComposite.setLayout(gridLayout_2);

		final Button convertButton = new Button(buttonComposite, SWT.NONE);
		convertButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onConvertFormat();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		convertButton.setText("转换格式");

		final Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onExport();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		exportButton.setText("导出");

		final Button importButton = new Button(buttonComposite, SWT.NONE);
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					onImport();
				} catch (Exception e1) {
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		importButton.setText("导入");

		formatLabel = new Label(composite, SWT.RIGHT);
		final GridData gd_formatLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_formatLabel.widthHint = 250;
		formatLabel.setLayoutData(gd_formatLabel);

		this.setPartName("图片优化工具");
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (currentFileName == null) {
			return;
		}
		try {
			currentFileData = changedFileData;
			if (packageFile == null) {
				// 单个文件，直接保存
				Utils.saveFileData(sourceFile, currentFileData);
			} else {
				// 修改PackageFileItem的内容，并保存
				if (currentFileName.contains("/")) {
					String[] secs = currentFileName.split("/");
					PackageFile newPkg = new PackageFile();
					newPkg.load(new ByteArrayInputStream(packageFile.findFile(secs[0])));
					newPkg.addFile(secs[1], currentFileData);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					newPkg.save(bos);
					packageFile.addFile(secs[0], bos.toByteArray());
					packageFile.save(sourceFile);
				} else {
					packageFile.addFile(currentFileName, currentFileData);
					packageFile.save(sourceFile);
				}
			}
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
			monitor.setCanceled(true);
		}
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		// Initialize the editor part
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return currentFileData != changedFileData;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	private void onOpen() {
		FileDialog openDlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		openDlg.setFilterExtensions(new String[] { "*.pkg", "*.pip" });
		openDlg.setFilterNames(new String[] { "关卡包文件(*.pkg)", "图片文件(*.pip)" });
		String path = openDlg.open();
		if (path == null) {
			return;
		}
		try {
			sourceFile = new File(path);
			if (sourceFile.getName().toLowerCase().endsWith(".pkg")) {
				packageFile = new PackageFile();
				packageFile.load(sourceFile);
				currentFileName = null;
				currentFileData = null;
				changedFileData = null;
				image = null;
				fileListViewer.refresh();
				fileListViewer.setSelection(new StructuredSelection());
				refreshPreviewer();
			} else {
				packageFile = null;
				currentFileName = sourceFile.getName();
				currentFileData = Utils.loadFileData(sourceFile);
				changedFileData = currentFileData;
				image = new PipImage();
				image.load(path);
				fileListViewer.refresh();
				refreshPreviewer();
			}
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
		}
	}
	
	private void refreshPreviewer() throws IOException {
		if (sourceFile == null || currentFileData == null) {
			previewer.setInput(null);
			previewer.refresh();
			formatLabel.setText("");
			return;
		}
		
		// 提取图片格式
		setFormatLabel();
		
		// 提取大图
		createPreviewImage(image, changedFileData);
		previewer.setInput(previewImage);
		previewer.refresh();
	}
	
	private void setFormatLabel() {
		// 提取图片格式
		if (image.getJPEGOption() != null) {
			formatLabel.setText("JPEG模式(质量" + image.getJPEGOption().quality + ",透明位" + 
					image.getJPEGOption().alphaBits + ",容错边框" + image.getJPEGOption().borderWidth + ")," +
					changedFileData.length + "字节");
		} else if (image.getCompressTextureOption() != null) {
			if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP)) {
				formatLabel.setText("4位色PVRTC(容错边框" + image.getCompressTextureOption().borderWidth + ")," + changedFileData.length + "字节");
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP2)) {
				formatLabel.setText("分离透明色的4位色PVRTC(容错边框" + image.getCompressTextureOption().borderWidth + ")," + changedFileData.length + "字节");
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC1)) {
				formatLabel.setText("ETC1(容错边框" + image.getCompressTextureOption().borderWidth + ")," + changedFileData.length + "字节");
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC2)) {
				formatLabel.setText("带透明度的ETC1(容错边框" + image.getCompressTextureOption().borderWidth + ")," + changedFileData.length + "字节");
			} else {
				throw new IllegalArgumentException("不支持的格式。");
			}
		} else if (image.isTrueColor()) {
			formatLabel.setText("真彩色模式," + changedFileData.length + "字节");
		} else if (image.isSupportMoreColors()) {
			formatLabel.setText("65536色模式," + changedFileData.length + "字节");
		} else {
			formatLabel.setText("256色模式," + changedFileData.length + "字节");
		}
	}
	
	// 用PIP图片创建合并后的预览图片。如果PIP图片本身已经是合并模式了，则直接从文件中提取合并图片。
	private void createPreviewImage(PipImage pipImg, byte[] fileData) throws IOException {
		if (pipImg.getJPEGOption() != null) {
			// JPEG合并模式
			getJPEGDataFromPip(new DataInputStream(new ByteArrayInputStream(fileData)));
		} else if (pipImg.getCompressTextureOption() != null) {
			if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP)) {
				// PVRTC压缩纹理
				getCompressedTextureDataFromPip(new DataInputStream(new ByteArrayInputStream(fileData)));
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP2)) {
				// PVRTC压缩纹理
				getCompressedTextureDataFromPip(new DataInputStream(new ByteArrayInputStream(fileData)));
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC1)) {
				// ETC压缩纹理
				getCompressedTextureDataFromPip(new DataInputStream(new ByteArrayInputStream(fileData)));
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC2)) {
				// 带透明度的ETC压缩纹理
				getCompressedTextureDataFromPip(new DataInputStream(new ByteArrayInputStream(fileData)));
			} else {
				throw new IllegalArgumentException("不支持的格式。");
			}
		} else {
			// 普通模式，生成一个合并版本（加1像素同色边）
			generateMergeImage(pipImg);
		}
	}
	
	// 用一个pip图片的所有帧，按客户端拼装纹理的算法，拼装成一个大的纹理图片。
	private void generateMergeImage(PipImage img) throws IOException {
		Vector<PipImageData> data = img.getImageDatas();
		int[][] sizes = new int[data.size()][2];
		for (int i = 0; i < data.size(); i++) {
			sizes[i][0] = data.get(i).width + 2;
			sizes[i][1] = data.get(i).height + 2;
		}
		
		int mergeSize = 32;
		while (true) {
			MergeAreaTest test = new MergeAreaTest(mergeSize, mergeSize);
			int[][] framePos = new int[data.size()][];
			if (!test.addImage(sizes, framePos)) {
				mergeSize *= 2;
				continue;
			}
			
			// 生成一个大的图片缓存，然后向其中填充图像数据
			int[] buffer = new int[mergeSize * mergeSize];
			for (int i = 0; i < data.size(); i++) {
				int tx = framePos[i][0];
				int ty = framePos[i][1];
				int tw = framePos[i][2];
				int th = framePos[i][3];
				int[] frameData = data.get(i).make(img.isTrueColor() ? null : img.getImagePalettes().get(0));
				frameData = addBorder(frameData, tw - 2, th - 2);
				fixEdgeColor(frameData, tw, th);
				for (int j = 0; j < th; j++) {
					System.arraycopy(frameData, j * tw, buffer, (ty + j) * mergeSize + tx, tw);
				}
			}
			
			// 创建预览图片
	    	if (previewImage != null) {
	    		previewImage.dispose();
	    	}
	    	previewImage = SWTUtils.createImage(buffer, mergeSize, mergeSize);
	    	break;
		}
	}
	
	/**
	 * 为材质图片增加1像素的同色边，防止在缩放拼接的图块时边界出现黑色的缝隙。这个方法只有pixels有效时才可以调用。
	 */
	public int[] addBorder(int[] pixels, int width, int height) {
		// 扩大数据区域
		int[] newBuf = new int[(width + 2) * (height + 2)];
		for (int y = 0; y < height; y++) {
			System.arraycopy(pixels, y * width, newBuf, (y + 1) * (width + 2) + 1, width);
		}
		
		// 填充每一行第一个像素和最后一个像素
		for (int y = 1; y < height + 1; y++) {
			newBuf[y * (width + 2)] = newBuf[y * (width + 2) + 1];
			newBuf[y * (width + 2) + width + 1] = newBuf[y * (width + 2) + width];
		}
		
		// 第一行和最后一行分别用第二行和倒数第二行填充
		System.arraycopy(newBuf, width + 2, newBuf, 0, width + 2);
		System.arraycopy(newBuf, height * (width + 2), newBuf, (height + 1) * (width + 2), width + 2);
		
		return newBuf;
	}
	
	/**
     * 在材质数据中搜索不透明部分的边缘，把边缘外的透明像素的颜色改成和不透明的边缘相同，以免在放大图片时产生黑边。
     */
	protected void fixEdgeColor(int[] pixels, int width, int height) {
		int[] arr = new int[] { -1, 1, -width, width, -width - 1, -width + 1, width - 1, width + 1 };
		for (int y = 1; y < height - 1; y++) {
			int base = y * width;
			for (int x = 1; x < width - 1; x++) {
				int ind = base + x;
				int p = pixels[ind];
				if ((p & 0xFF000000) == 0) {
					// 如果一个像素是透明的，需要把它的颜色设置为最近的非透明点
					for (int i = 0; i < 8; i++) {
						int p2 = pixels[ind + arr[i]];
						if ((p2 & 0xFF000000) != 0) {
							pixels[ind] = p2 & 0x00FFFFFF;
							break;
						}
					}
				}
			}
		}
	}
	
	// 从一个压缩纹理模式的PIP图片中读取压缩纹理内容。原始压缩纹理内容放到sourceImageData变量，图片放到previewImage
	private void getCompressedTextureDataFromPip(DataInputStream dis) throws IOException {
		dis.skipBytes(3); // head
		String format = dis.readUTF(); // 纹理格式
		CompressTextureOption tcOption = new CompressTextureOption(format);
		dis.readByte(); // palette count
		tcOption.load(dis);
    	
    	// 跳过合并帧信息
    	int frameCount = dis.readShort();
    	dis.skipBytes(8 * frameCount);
    	
    	// 读取合并图片大小
    	int mergeW = dis.readShort() & 0xFFFF;
    	int mergeH = dis.readShort() & 0xFFFF;
		
    	// 读取压缩纹理数据
    	sourceImageData = new byte[dis.readInt()];
    	dis.readFully(sourceImageData);
    	if (previewImage != null) {
    		previewImage.dispose();
    	}
    	previewImage = PipImage.compressTextureHandler.decodeTexture(tcOption.format, sourceImageData, mergeW, mergeH);
	}
	
	// 从一个合并JPEG模式的PIP图片中读取JPEG内容。原始JPEG内容放到sourceImageData变量，图片加上透明色后放到previewImage
	private void getJPEGDataFromPip(DataInputStream dis) throws IOException {
		dis.skipBytes(3); // head
		dis.readByte(); // palette count
		float quality = (dis.read() & 0xFF) / 100.0f;
    	int alphaBits = dis.read();
    	int borderWidth = dis.read();
    	
    	// 跳过合并帧信息
    	int frameCount = dis.readShort();
    	dis.skipBytes(8 * frameCount);
    	
    	// 读取合并图片大小
    	int mergeW = dis.readShort() & 0xFFFF;
    	int mergeH = dis.readShort() & 0xFFFF;
		
    	// 读取alpha通道数据
    	byte[] alphaData = new byte[dis.readInt()];
    	int alphaW = dis.readShort() & 0xFFFF;
    	int alphaH = dis.readShort() & 0xFFFF;
    	dis.readFully(alphaData);
    	alphaData = GZIP.inflate(alphaData);
    	if (alphaData.length != alphaH * alphaW) {
    		throw new IllegalArgumentException("file corrupt");
    	}
    	byte[][] alpha = new byte[mergeH][mergeW];
    	for (int yy = 0; yy < mergeH; yy++) {
    		int base = yy * alphaW;
    		for (int xx = 0; xx < mergeW; xx++) {
    			if (alphaBits == 8) {
    				alpha[yy][xx] = alphaData[base + xx];
    			} else if (alphaBits == 4) {
    				int ind1 = xx / 2;
    				int ind2 = xx % 2;
    				int value = (alphaData[base + ind1] << (ind2 * 4)) & 0xF0;
					alpha[yy][xx] = (byte)(value | (value >> 4));
    			} else if (alphaBits == 2) {
    				int ind1 = xx / 4;
    				int ind2 = xx % 4;
    				int value = (alphaData[base + ind1] << (ind2 * 2)) & 0xC0;
    				alpha[yy][xx] = (byte)(value | (value >> 2) | (value >> 4) | (value >> 6));
    			} else if (alphaBits == 1) {
    				int ind1 = xx / 8;
    				int ind2 = xx % 8;
    				int value = (alphaData[base + ind1] << ind2) & 0x80;
    				if (value == 0x80) {
    					alpha[yy][xx] = (byte)0xFF;
    				}
    			} else {
    				throw new IllegalArgumentException("invalid alpha bits: " + alphaBits);
    			}
    		}
    	}
    	
    	// 从JPEG文件载入，并恢复ALPHA数据
    	sourceImageData = new byte[dis.readInt()];
    	dis.readFully(sourceImageData);
    	Image tempImg = new Image(null, new ByteArrayInputStream(sourceImageData));
        int[][] imgData = SWTUtils.getImageData(tempImg, new Rectangle(0, 0, mergeW, mergeH));
        tempImg.dispose();
        for (int i = 0; i < imgData.length; i++) {
        	for (int j = 0; j < imgData[i].length; j++) {
        		imgData[i][j] &= 0xFFFFFF;
        		imgData[i][j] |= alpha[i][j] << 24;
        	}
        }
        
        // 创建预览图片(带透明)
    	if (previewImage != null) {
    		previewImage.dispose();
    	}
    	previewImage = SWTUtils.createImage(imgData);
	}
	
	// 替换一个JPEG合并模式PIP图片中的JPEG内容
	private byte[] replaceJpegInPip(byte[] source, byte[] jpegData) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(source));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		byte[] head = new byte[3];
		dis.readFully(head);
		dos.write(head);
		
		dos.writeByte(dis.readByte());  // palette count

		dos.writeByte(dis.readByte());  // quality
		dos.writeByte(dis.readByte());  // alphaBits
		dos.writeByte(dis.readByte());  // borderWidth
		
    	// 跳过合并帧信息
    	int frameCount = dis.readShort();
    	dos.writeShort(frameCount);
    	byte[] frameData = new byte[8 * frameCount];
    	dis.readFully(frameData);
    	dos.write(frameData);
    	
    	// 读取合并图片大小
    	int mergeW = dis.readShort() & 0xFFFF;
    	int mergeH = dis.readShort() & 0xFFFF;
    	dos.writeShort(mergeW);
    	dos.writeShort(mergeH);
		
    	// 读取alpha通道数据
    	int alphaLen = dis.readInt();
    	dos.writeInt(alphaLen);
    	byte[] alphaData = new byte[alphaLen];
    	int alphaW = dis.readShort() & 0xFFFF;
    	int alphaH = dis.readShort() & 0xFFFF;
    	dis.readFully(alphaData);
    	dos.writeShort(alphaW);
    	dos.writeShort(alphaH);
    	dos.write(alphaData);
    	
    	// 替换JPEG内容
    	int imageLen = dis.readInt();
    	dis.skipBytes(imageLen);
    	dos.writeInt(jpegData.length);
    	dos.write(jpegData);
    	
    	dos.flush();
    	return bos.toByteArray();
	}
	
	// 替换一个压缩纹理格式PIP图片中的压缩纹理内容
	private byte[] replaceCompressTextureInPip(byte[] source, byte[] newData) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(source));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		byte[] head = new byte[3];
		dis.readFully(head);
		dos.write(head);
		String format = dis.readUTF();
		CompressTextureOption tcOption = new CompressTextureOption(format);
		dos.writeUTF(format);
		dos.writeByte(dis.readByte());  // palette count
		tcOption.load(dis);
		tcOption.save(dos);
		
    	// 跳过合并帧信息
    	int frameCount = dis.readShort();
    	dos.writeShort(frameCount);
    	byte[] frameData = new byte[8 * frameCount];
    	dis.readFully(frameData);
    	dos.write(frameData);
    	
    	// 读取合并图片大小
    	int mergeW = dis.readShort() & 0xFFFF;
    	int mergeH = dis.readShort() & 0xFFFF;
    	dos.writeShort(mergeW);
    	dos.writeShort(mergeH);
		
    	// 替换文件内容
    	int imageLen = dis.readInt();
    	dis.skipBytes(imageLen);
    	dos.writeInt(newData.length);
    	dos.write(newData);
    	
    	dos.flush();
    	return bos.toByteArray();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (previewImage != null) {
			previewImage.dispose();
		}
	}
	
	private void onExport() throws IOException {
		if (currentFileName == null) {
			return;
		}
		if (image.getJPEGOption() != null) {
			// JPEG格式，导出sourceImageData里的JPEG图片
			FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
			dlg.setFilterExtensions(new String[] { "*.jpg" });
			dlg.setFilterNames(new String[] { "JPEG文件(*.jpg)" });
			String path = dlg.open();
			if (path != null) {
				Utils.saveFileData(new File(path), sourceImageData);
			}
		} else if (image.getCompressTextureOption() != null) {
			// 压缩纹理格式
			if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.pvr" });
				dlg.setFilterNames(new String[] { "PVRTC压缩纹理文件(*.pvr)" });
				String path = dlg.open();
				if (path != null) {
					Utils.saveFileData(new File(path), GZIP.inflate(sourceImageData));
				}
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP2)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.pvr" });
				dlg.setFilterNames(new String[] { "PVRTC压缩纹理文件(*.pvr)" });
				String path = dlg.open();
				if (path != null) {
					Utils.saveFileData(new File(path), GZIP.inflate(sourceImageData));
				}
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC1)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.etc" });
				dlg.setFilterNames(new String[] { "ETC压缩纹理文件(*.etc)" });
				String path = dlg.open();
				if (path != null) {
					Utils.saveFileData(new File(path), GZIP.inflate(sourceImageData));
				}
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC2)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.etc" });
				dlg.setFilterNames(new String[] { "ETC压缩纹理文件(*.etc)" });
				String path = dlg.open();
				if (path != null) {
					Utils.saveFileData(new File(path), GZIP.inflate(sourceImageData));
				}
			} else {
				throw new IllegalArgumentException("不支持的格式");
			}
		} else {
			// 普通PIP格式，导出previewImage为PNG图片
			FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
			dlg.setFilterExtensions(new String[] { "*.png" });
			dlg.setFilterNames(new String[] { "PNG文件(*.png)" });
			String path = dlg.open();
			if (path != null) {
				PngEncoder enc = new PngEncoder(previewImage);
	            FileOutputStream fos = new FileOutputStream(path);
	            enc.encode32(fos, false);
	            fos.close();
			}
		}
	}
	
	private void onImport() throws IOException {
		if (currentFileName == null) {
			return;
		}
		if (image.getJPEGOption() != null) {
			// JPEG格式，导入JPEG到sourceImageData里
			FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
			dlg.setFilterExtensions(new String[] { "*.jpg" });
			dlg.setFilterNames(new String[] { "JPEG文件(*.jpg)" });
			String path = dlg.open();
			if (path != null) {
				// 检查图片大小是否一致
				byte[] fileData = Utils.loadFileData(new File(path));
				Image tempImg = new Image(null, new ByteArrayInputStream(fileData));
				if (tempImg.getBounds().width != previewImage.getBounds().width || tempImg.getBounds().height != previewImage.getBounds().height) {
					tempImg.dispose();
					throw new IOException("图片尺寸不一致。");
				}
				tempImg.dispose();
				
				// 重新生成预览图片
				sourceImageData = fileData;
				changedFileData = replaceJpegInPip(changedFileData, sourceImageData);
				createPreviewImage(image, changedFileData);
				previewer.setInput(previewImage);
				previewer.refresh();
				firePropertyChange(PROP_DIRTY);
			}
		} else if (image.getCompressTextureOption() != null) {
			// 压缩纹理格式
			if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.pvr" });
				dlg.setFilterNames(new String[] { "PVRTC压缩纹理文件(*.pvr)" });
				String path = dlg.open();
				if (path != null) {
					// 读取文件内容，PVPTC文件需要在进行一次GZIP压缩
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
		            GZIPOutputStream zos = new GZIPOutputStream(bos);
		            DataOutputStream zdos = new DataOutputStream(zos);
		            zdos.write(Utils.loadFileData(new File(path)));
		            zdos.close();
		            sourceImageData = bos.toByteArray();
		            
					// 重新生成预览图片
					changedFileData = replaceCompressTextureInPip(changedFileData, sourceImageData);
					createPreviewImage(image, changedFileData);
					previewer.setInput(previewImage);
					previewer.refresh();
					firePropertyChange(PROP_DIRTY);
				}
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP2)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.pvr" });
				dlg.setFilterNames(new String[] { "PVRTC压缩纹理文件(*.pvr)" });
				String path = dlg.open();
				if (path != null) {
					// 读取文件内容，PVPTC文件需要在进行一次GZIP压缩
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
		            GZIPOutputStream zos = new GZIPOutputStream(bos);
		            DataOutputStream zdos = new DataOutputStream(zos);
		            zdos.write(Utils.loadFileData(new File(path)));
		            zdos.close();
		            sourceImageData = bos.toByteArray();
		            
					// 重新生成预览图片
					changedFileData = replaceCompressTextureInPip(changedFileData, sourceImageData);
					createPreviewImage(image, changedFileData);
					previewer.setInput(previewImage);
					previewer.refresh();
					firePropertyChange(PROP_DIRTY);
				}
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC1)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.etc" });
				dlg.setFilterNames(new String[] { "ETC压缩纹理文件(*.etc)" });
				String path = dlg.open();
				if (path != null) {
					// 读取文件内容，ETC文件需要在进行一次GZIP压缩
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
		            GZIPOutputStream zos = new GZIPOutputStream(bos);
		            DataOutputStream zdos = new DataOutputStream(zos);
		            zdos.write(Utils.loadFileData(new File(path)));
		            zdos.close();
		            sourceImageData = bos.toByteArray();
		            
					// 重新生成预览图片
					changedFileData = replaceCompressTextureInPip(changedFileData, sourceImageData);
					createPreviewImage(image, changedFileData);
					previewer.setInput(previewImage);
					previewer.refresh();
					firePropertyChange(PROP_DIRTY);
				}
			} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC2)) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.etc" });
				dlg.setFilterNames(new String[] { "ETC压缩纹理文件(*.etc)" });
				String path = dlg.open();
				if (path != null) {
					// 读取文件内容，ETC文件需要在进行一次GZIP压缩
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
		            GZIPOutputStream zos = new GZIPOutputStream(bos);
		            DataOutputStream zdos = new DataOutputStream(zos);
		            zdos.write(Utils.loadFileData(new File(path)));
		            zdos.close();
		            sourceImageData = bos.toByteArray();
		            
					// 重新生成预览图片
					changedFileData = replaceCompressTextureInPip(changedFileData, sourceImageData);
					createPreviewImage(image, changedFileData);
					previewer.setInput(previewImage);
					previewer.refresh();
					firePropertyChange(PROP_DIRTY);
				}
			} else {
				throw new IllegalArgumentException("不支持的格式");
			}
		} else {
			// 普通PIP格式，暂时不支持导入
			throw new IOException("目前只支持JPEG格式。");
		}
	}
	
	// 转换格式
	private void onConvertFormat() throws IOException {
		GenericChooseDialog dlg = new GenericChooseDialog(getSite().getShell(), "转换格式", "请选择目标格式：", new Object[] { "JPEG合并模式", "PVRTC4压缩纹理", "分离透明色的PVRTC4纹理", "ETC1压缩纹理", "带透明色的ETC1压缩纹理" });
		if (dlg.open() != Dialog.OK) {
			return;
		}
		if (dlg.getSelectionIndex() == 0) {
			// 转换为JPEG
			convertToJPEG();
		} else if (dlg.getSelectionIndex() == 1) {
			// 转换为PVRTC
			convertToPVRTC4();
		} else if (dlg.getSelectionIndex() == 2) {
			// 转换为PVRTC
			convertToPVRTC42();
		} else if (dlg.getSelectionIndex() == 3) {
			// 转换为ETC
			convertToETC1();
		} else {
			// 转换为带透明度的ETC
			convertToETC2();
		}
	}
	
	private void convertToJPEG() throws IOException {
		image = new PipImage();
		image.load(new ByteArrayInputStream(changedFileData));
		JPEGMergeOption option = JPEGMergeOptionDialog.choose(image.getJPEGOption());
        if (option == null) {
        	return;
        }
        image.setSupportColorOp(false);
        image.setSupportMoreColors(false);
        image.setMergeMode(false);
        image.setTrueColor(true);
        image.setJPEGOption(option);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        changedFileData = bos.toByteArray();
        
        refreshPreviewer();
        firePropertyChange(PROP_DIRTY);
	}
	
	private void convertToPVRTC4() throws IOException { 
		image = new PipImage();
		image.load(new ByteArrayInputStream(changedFileData));
		image.setSupportColorOp(false);
        image.setSupportMoreColors(false);
        image.setMergeMode(false);
        image.setTrueColor(true);
        image.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.PVRTC_4BPP));
        image.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        changedFileData = bos.toByteArray();
        
        refreshPreviewer();
        firePropertyChange(PROP_DIRTY);
	}
	
	private void convertToPVRTC42() throws IOException { 
		image = new PipImage();
		image.load(new ByteArrayInputStream(changedFileData));
		image.setSupportColorOp(false);
        image.setSupportMoreColors(false);
        image.setMergeMode(false);
        image.setTrueColor(true);
        image.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.PVRTC_4BPP2));
        image.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        changedFileData = bos.toByteArray();
        
        refreshPreviewer();
        firePropertyChange(PROP_DIRTY);
	}
	
	private void convertToETC1() throws IOException { 
		image = new PipImage();
		image.load(new ByteArrayInputStream(changedFileData));
		if (!image.isOpaque()) {
			String msg = "图片中含有透明或半透明像素，使用ETC1将会导致透明数据丢失，是否确认继续？";
	    	if (!MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
	    		return;
	    	}
		}
		image.setSupportColorOp(false);
        image.setSupportMoreColors(false);
        image.setMergeMode(false);
        image.setTrueColor(true);
        image.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.ETC1));
        image.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        changedFileData = bos.toByteArray();
        
        refreshPreviewer();
        firePropertyChange(PROP_DIRTY);
	}
	
	private void convertToETC2() throws IOException { 
		image = new PipImage();
		image.load(new ByteArrayInputStream(changedFileData));
		image.setSupportColorOp(false);
        image.setSupportMoreColors(false);
        image.setMergeMode(false);
        image.setTrueColor(true);
        image.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.ETC2));
        image.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        changedFileData = bos.toByteArray();
        
        refreshPreviewer();
        firePropertyChange(PROP_DIRTY);
	}
}
