package com.pip.mapeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.pip.image.workshop.GenericChooseDialog;
import com.pip.image.workshop.editor.CompressTextureOptionDialog;
import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.ImageViewerListener;
import com.pip.image.workshop.editor.JPEGMergeOptionDialog;
import com.pip.image.workshop.editor.TileLibSelector;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.CompressTextureOption;
import com.pipimage.image.JPEGMergeOption;
import com.pipimage.image.MergeAreaTest;
import com.pipimage.image.PackageFile;
import com.pipimage.image.PackageFileItem;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.utils.GZIP;

public class ReorgANPDialog extends Dialog implements ImageViewerListener, DisposeListener {
	class SourceImageContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			int count = sourceImageFileList.size();
			Object[] ret = new Object[count];
			for (int i = 0; i < count; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class SourceImageLabelProvider extends LabelProvider {
		public String getText(Object element) {
			int index = ((Integer)element).intValue();
			String fileName = sourceImageFileList.get(index);
			PipImage image = sourceImageList.get(index);
			if (image.getJPEGOption() != null) {
				return fileName + ",JPEG模式(质量" + image.getJPEGOption().quality + ",透明位" + 
						image.getJPEGOption().alphaBits + ",容错边框" + image.getJPEGOption().borderWidth + ")";
			} else if (image.getCompressTextureOption() != null) {
				if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP)) {
					return fileName + ",4位色PVRTC";
				} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP2)) {
					return fileName + ",分离透明色的4位色PVRTC";
				} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC1)) {
					return fileName + ",ETC1";
				} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC2)) {
					return fileName + ",带透明度的ETC1";
				} else {
					throw new IllegalArgumentException("不支持的格式。");
				}
			} else if (image.isTrueColor()) {
				return fileName + ",真彩色模式";
			} else if (image.isSupportMoreColors()) {
				return fileName + ",65536色模式";
			} else {
				return fileName + ",256色模式";
			}
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class TargetImageContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			int count = targetImageFileList.size();
			Object[] ret = new Object[count];
			for (int i = 0; i < count; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class TargetImageLabelProvider extends LabelProvider {
		public String getText(Object element) {
			int index = ((Integer)element).intValue();
			String fileName = targetImageFileList.get(index);
			PipImage image = targetImageList.get(index);
			if (image.getJPEGOption() != null) {
				return fileName + ",JPEG模式(质量" + image.getJPEGOption().quality + ",透明位" + 
						image.getJPEGOption().alphaBits + ",容错边框" + image.getJPEGOption().borderWidth + ")";
			} else if (image.getCompressTextureOption() != null) {
				if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP)) {
					return fileName + ",4位色PVRTC,容错边框" + image.getCompressTextureOption().borderWidth;
				} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.PVRTC_4BPP2)) {
					return fileName + ",分离透明色的4位色PVRTC,容错边框" + image.getCompressTextureOption().borderWidth;
				} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC1)) {
					return fileName + ",ETC1,容错边框" + image.getCompressTextureOption().borderWidth;
				} else if (image.getCompressTextureOption().format.equals(CompressTextureOption.ETC2)) {
					return fileName + ",带透明度的ETC1,容错边框" + image.getCompressTextureOption().borderWidth;
				} else {
					throw new IllegalArgumentException("不支持的格式。");
				}
			} else if (image.isTrueColor()) {
				return fileName + ",真彩色模式";
			} else if (image.isSupportMoreColors()) {
				return fileName + ",65536色模式";
			} else {
				return fileName + ",256色模式";
			}
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private PackageFile anpPackage;
	private List<String> sourceImageFileList = new ArrayList<String>();
	private List<PipImage> sourceImageList = new ArrayList<PipImage>();
	private List<boolean[]> sourceImageVisibleFlag = new ArrayList<boolean[]>();
	private List<String> targetImageFileList = new ArrayList<String>();
	private List<PipImage> targetImageList = new ArrayList<PipImage>();
	
	// 记录重组前的帧ID和重组后的帧ID的对应关系，int高16位是image索引，低16位是frame索引
	private Map<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
	
	private Combo sourceImageCombo;
	private Combo targetImageCombo;
	private Composite targetImageViewerContainer;
	private ComboViewer targetImageComboViewer;
	private ImageViewer targetImageViewer;
	private ImageViewer targetImageViewer2;
	private TileLibSelector sourceImageViewer;
	private Composite sourceImageViewerContainer;
	private ComboViewer sourceImageComboViewer;
	private Composite targetImageViewer2Container;
	
	private Image previewImage;
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public ReorgANPDialog(Shell parentShell, PackageFile pkg) throws IOException {
		super(parentShell);
		
		anpPackage = pkg;
		PackageFileItem[] items = pkg.getFiles();
		for (PackageFileItem item : items) {
			if (item.name.toLowerCase().endsWith(".pip")) {
				sourceImageFileList.add(item.name);
				PipImage img = new PipImage();
				img.load(new ByteArrayInputStream(item.data));
				sourceImageList.add(img);
				boolean[] flag = new boolean[img.getImgCount()];
				Arrays.fill(flag, true);
				sourceImageVisibleFlag.add(flag);
			}
		}
		targetImageFileList.add("0.pip");
		PipImage img = new PipImage();
		img.setMergeMode(false);
		img.setTrueColor(true);
		img.setSupportColorOp(false);
		img.setSupportMoreColors(false);
		img.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.PVRTC_4BPP));
		img.getCompressTextureOption().borderWidth = 2;
		targetImageList.add(img);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Group targetImageGroup = new Group(container, SWT.NONE);
		targetImageGroup.setText("目标文件");
		final GridData gd_targetImageGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		targetImageGroup.setLayoutData(gd_targetImageGroup);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		targetImageGroup.setLayout(gridLayout_1);

		targetImageViewerContainer = new Composite(targetImageGroup, SWT.NONE);
		targetImageViewerContainer.setLayout(new FillLayout());
		final GridData gd_targetImageViewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_targetImageViewerContainer.widthHint = 500;
		targetImageViewerContainer.setLayoutData(gd_targetImageViewerContainer);
		
		targetImageViewer = new ImageViewer(targetImageViewerContainer, SWT.NONE);
		targetImageViewer.setImageViewerListener(this);

		targetImageViewer2Container = new Composite(targetImageGroup, SWT.NONE);
		targetImageViewer2Container.setLayout(new FillLayout());
		final GridData gd_targetImageViewer2Container = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		targetImageViewer2Container.setLayoutData(gd_targetImageViewer2Container);
		
		targetImageViewer2 = new ImageViewer(targetImageViewer2Container, SWT.NONE);

		targetImageComboViewer = new ComboViewer(targetImageGroup, SWT.READ_ONLY);
		targetImageComboViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent arg0) {
				onTargetImageSelectionChanged();
			}
		});
		targetImageComboViewer.setContentProvider(new TargetImageContentProvider());
		targetImageComboViewer.setLabelProvider(new TargetImageLabelProvider());
		targetImageCombo = targetImageComboViewer.getCombo();
		targetImageCombo.setVisibleItemCount(10);
		final GridData gd_targetImageCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
		targetImageCombo.setLayoutData(gd_targetImageCombo);
		targetImageComboViewer.setInput(this);

		final Button newFileButton = new Button(targetImageGroup, SWT.NONE);
		newFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onNewTargetImage();
			}
		});
		newFileButton.setText("新建文件");

		final Button deleteFileButton = new Button(targetImageGroup, SWT.NONE);
		deleteFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onDeleteTargetImage();
			}
		});
		deleteFileButton.setText("删除文件");

		final Group sourceImageGroup = new Group(container, SWT.NONE);
		sourceImageGroup.setText("源文件");
		final GridData gd_sourceImageGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		sourceImageGroup.setLayoutData(gd_sourceImageGroup);
		sourceImageGroup.setLayout(new GridLayout());

		sourceImageViewerContainer = new Composite(sourceImageGroup, SWT.NONE);
		sourceImageViewerContainer.setLayout(new FillLayout());
		final GridData gd_sourceImageViewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_sourceImageViewerContainer.widthHint = 500;
		sourceImageViewerContainer.setLayoutData(gd_sourceImageViewerContainer);
		
		sourceImageViewer = new TileLibSelector(sourceImageViewerContainer, SWT.NONE);
		sourceImageViewer.setImageViewerListener(this);

		sourceImageComboViewer = new ComboViewer(sourceImageGroup, SWT.READ_ONLY);
		sourceImageComboViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				onSourceImageSelectionChanged();
			}
		});
		sourceImageComboViewer.setLabelProvider(new SourceImageLabelProvider());
		sourceImageComboViewer.setContentProvider(new SourceImageContentProvider());
		sourceImageCombo = sourceImageComboViewer.getCombo();
		sourceImageCombo.setVisibleItemCount(10);
		final GridData gd_sourceImageCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
		sourceImageCombo.setLayoutData(gd_sourceImageCombo);
		sourceImageComboViewer.setInput(this);
		
		sourceImageCombo.select(0);
		targetImageCombo.select(0);
		onSourceImageSelectionChanged();
		onTargetImageSelectionChanged();
		
		container.addDisposeListener(this);
		
		return container;
	}
	
	private void onSourceImageSelectionChanged() {
		int index = sourceImageCombo.getSelectionIndex();
		sourceImageViewer.setPipImageFrameVisibleFlag(sourceImageVisibleFlag.get(index));
		sourceImageViewer.setInput(sourceImageList.get(index), 0, 0);
		sourceImageViewer.redraw();
	}
	
	private void onTargetImageSelectionChanged() {
		int index = targetImageCombo.getSelectionIndex();
		targetImageViewer.setInput(targetImageList.get(index));
		targetImageViewer.refresh();
		refreshPreview(index);
	}
	
	private void refreshPreview(int index) {
		if (previewImage != null) {
			previewImage.dispose();
		}
		try {
			previewImage = generatePreviewImage(targetImageList.get(index));
			targetImageViewer2.setInput(previewImage);
			targetImageViewer2.refresh();
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}
	}
	
	// 用一个pip图片的所有帧，按客户端拼装纹理的算法，拼装成一个大的纹理图片。
	private Image generatePreviewImage(PipImage img) throws IOException {
		int borderSize = 1;
		if (img.getJPEGOption() != null) {
			borderSize = img.getJPEGOption().borderWidth;
		} else if (img.getCompressTextureOption() != null) {
			borderSize = img.getCompressTextureOption().borderWidth;
		}
		
		Vector<PipImageData> data = img.getImageDatas();
		int[][] sizes = new int[data.size()][2];
		for (int i = 0; i < data.size(); i++) {
			sizes[i][0] = data.get(i).width + borderSize * 2;
			sizes[i][1] = data.get(i).height + borderSize * 2;
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
				for (int j = 0; j < data.get(i).height; j++) {
					System.arraycopy(frameData, j * data.get(i).width, buffer, (ty + j) * mergeSize + tx + borderSize, data.get(i).width);
				}
			}
			
			// 创建预览图片
	    	return SWTUtils.createImage(buffer, mergeSize, mergeSize);
		}
	}
	
	private void onNewTargetImage() {
		GenericChooseDialog dlg = new GenericChooseDialog(getShell(), "图片格式", "请选择目标格式：", new Object[] { "JPEG合并模式", "PVRTC4压缩纹理", "分离透明色的PVRTC4压缩纹理", "ETC1压缩纹理", 
			"带透明度的ETC1压缩纹理", "真彩色模式", "65536色模式", "256色模式" });
		if (dlg.open() != Dialog.OK) {
			return;
		}
		int newName = 0;
		while (targetImageFileList.contains(newName + ".pip")) {
			newName++;
		}
		targetImageFileList.add(newName + ".pip");
		PipImage newImg = new PipImage();
		switch (dlg.getSelectionIndex()) {
		case 0:  // jpeg
			newImg.setSupportColorOp(false);
			newImg.setSupportMoreColors(false);
			newImg.setMergeMode(false);
			newImg.setTrueColor(true);
            JPEGMergeOption option = JPEGMergeOptionDialog.choose(newImg.getJPEGOption());
            if (option == null) {
            	newImg.setJPEGOption(new JPEGMergeOption());
            } else {
            	newImg.setJPEGOption(option);
            }
            break;
		case 1:  // pvrtc
			newImg.setSupportColorOp(false);
			newImg.setSupportMoreColors(false);
			newImg.setMergeMode(false);
			newImg.setTrueColor(true);
            newImg.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.PVRTC_4BPP));
            newImg.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
            break;
		case 2:  // pvrtc2
			newImg.setSupportColorOp(false);
			newImg.setSupportMoreColors(false);
			newImg.setMergeMode(false);
			newImg.setTrueColor(true);
            newImg.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.PVRTC_4BPP2));
            newImg.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
            break;
		case 3:  // etc1
			newImg.setSupportColorOp(false);
			newImg.setSupportMoreColors(false);
			newImg.setMergeMode(false);
			newImg.setTrueColor(true);
			newImg.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.ETC1));
			newImg.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
            break;
		case 4:  // etc2
			newImg.setSupportColorOp(false);
			newImg.setSupportMoreColors(false);
			newImg.setMergeMode(false);
			newImg.setTrueColor(true);
			newImg.setCompressTextureOption(new CompressTextureOption(CompressTextureOption.ETC2));
			newImg.getCompressTextureOption().borderWidth = CompressTextureOptionDialog.choose(2);
            break;
		case 5:  // true color
			newImg.setSupportColorOp(false);
        	newImg.setSupportMoreColors(false);
        	newImg.setMergeMode(false);
            newImg.setTrueColor(true);
            newImg.setJPEGOption(null);
            newImg.setCompressTextureOption(null);
            break;
		case 6:  // 65536
			newImg.setSupportColorOp(true);
			newImg.setSupportMoreColors(true);
			newImg.setMergeMode(false);
			newImg.setTrueColor(false);
			newImg.setJPEGOption(null);
			newImg.setCompressTextureOption(null);
            break;
		case 7:  // 256
			newImg.setSupportColorOp(true);
            newImg.setSupportMoreColors(false);
            newImg.setMergeMode(false);
            newImg.setTrueColor(false);
            newImg.setJPEGOption(null);
            newImg.setCompressTextureOption(null);
            break;
		}
		targetImageList.add(newImg);
		targetImageComboViewer.refresh();
		targetImageCombo.select(targetImageList.size() - 1);
		onTargetImageSelectionChanged();
	}
	
	private void onDeleteTargetImage() {
		if (targetImageList.size() <= 1) {
			MessageDialog.openError(getShell(), "错误", "不能删除最后一个文件。");
			return;
		}
		int index = targetImageCombo.getSelectionIndex();
		PipImage img = targetImageList.get(index);
		if (img.getImgCount() > 0) {
			if (!MessageDialog.openConfirm(getShell(), "确认", "你确认要删除这个文件？删除后此文件中的帧会被自动退回到原始文件中。")) {
				return;
			}
		}
		
		// 搜索所有已经加入目标文件中的帧的信息。如果其imageid等于index，则需要退回原始文件（修改sourceImageVisibleFlag）；如果其imageid
		// 大于index，则imageid需要减去1。
		Object[] arr = frameMap.keySet().toArray();
		for (Object key : arr) {
			int keyValue = ((Integer)key).intValue();
			int targetValue = frameMap.get(key);
			int sourceImageID = keyValue >> 16;
			int sourceFrameID = keyValue & 0xFFFF;
			int targetImageID = targetValue >> 16;
			int targetFrameID = targetValue & 0xFFFF;
			if (targetImageID == index) {
				sourceImageVisibleFlag.get(sourceImageID)[sourceFrameID] = true;
				frameMap.remove(keyValue);
			} else if (targetImageID > index) {
				targetImageID--;
				targetValue = (targetImageID << 16) | targetFrameID;
				frameMap.put(keyValue, targetValue);
			}
		}
		
		targetImageList.remove(index);
		targetImageFileList.remove(index);
		targetImageComboViewer.refresh();
		if (index >= targetImageList.size()) {
			targetImageCombo.select(index - 1);
		} else {
			targetImageCombo.select(index);
		}
		onTargetImageSelectionChanged();
		onSourceImageSelectionChanged();
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(1400, 800);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("重组");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// DEBUG，输入所有anp中的文件到d:\reorg_before
			//extractPackage(anpPackage, new File("d:/reorg_before"));

			// 检查是否所有的图片都被拖到左边了
			for (int i = 0; i < sourceImageVisibleFlag.size(); i++) {
				boolean[] arr = sourceImageVisibleFlag.get(i);
				for (int j = 0; j < arr.length; j++) {
					if (arr[j]) {
						MessageDialog.openError(getShell(), "错误", sourceImageFileList.get(i) + "里还有未加入目标文件的帧。");
						return;
					}
				}
			}
			
			try {
				// 从anp文件中删除所有旧的pip文件，加入新的pip文件
				PackageFileItem[] items = anpPackage.getFiles();
				for (int i = items.length - 1; i >= 0; i--) {
					if (items[i].name.toLowerCase().endsWith(".pip")) {
						anpPackage.removeFile(i);
					}
				}
				for (int i = 0; i < targetImageFileList.size(); i++) {
					String fname = targetImageFileList.get(i);
					PipImage img = targetImageList.get(i);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					img.save(dos, true);
					dos.flush();
					anpPackage.addFile(i + ".pip", bos.toByteArray());
				}
				
				// 从anp文件中载入0.ctn，替换其中所有的帧引用为新的帧引用，并替换其中的文件列表
				byte[] zdata = anpPackage.findFile("0.ctn");
				byte[] uzdata = GZIP.inflate(zdata);
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(uzdata));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				replaceFrameRefInCTN(dis, dos);
				dos.flush();
				uzdata = bos.toByteArray();
				bos = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(bos);
				zos.write(uzdata);
				zos.flush();
				zos.close();
				zdata = bos.toByteArray();
				anpPackage.addFile("0.ctn", zdata);
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", e);
				return;
			}

			// DEBUG，输入所有anp中的文件到d:\reorg_after
			//extractPackage(anpPackage, new File("d:/reorg_after"));
		}
		super.buttonPressed(buttonId);
	}
	
	private void extractPackage(PackageFile pkg, File dir) {
		dir.mkdirs();
		File[] fs = dir.listFiles();
		for (File f : fs) {
			if (f.isFile()) {
				f.delete();
			}
		}
		PackageFileItem[] items = pkg.getFiles();
		for (int i = 0; i < items.length; i++) {
			try {
				Utils.saveFileData(new File(dir, items[i].name), items[i].data);
			} catch (Exception e) {
			}
		}
	}
	
	private void replaceFrameRefInCTN(DataInputStream dis, DataOutputStream dos) throws IOException {
		// 前2个字节是version和帧数量
		int tmp = dis.readShort() & 0xFFFF;
		int fcount = tmp & 0x3FFF;
		byte version = (byte)(tmp >> 14);
		
		// 读取帧定义，替换其中的引用图块序号
		PipAnimateSet ans = new PipAnimateSet();
		for (int i = 0; i < sourceImageList.size(); i++) {
			ans.addSourceFile(sourceImageFileList.get(i), sourceImageList.get(i));
		}
		ans.setVersion(version);
		List<PipAnimateFrame> frames = new ArrayList<PipAnimateFrame>();
		for (int i = 0; i < fcount; i++) {
			PipAnimateFrame fr = new PipAnimateFrame(ans);
			fr.load(dis, true);
			for (int j = 0; j < fr.getPieceCount(); j++) {
				PipAnimateFramePiece piece = fr.getPiece(j);
				int oldImg = piece.getImageID();
				int oldFrm = piece.getFrame();
				int v = frameMap.get((oldImg << 16) | oldFrm);
				piece.setImageID(v >> 16);
				piece.setFrame(v & 0xFFFF);
			}
			frames.add(fr);
		}
		
		// 读取动画定义
		int acount = dis.readByte() & 0xFF;
		List<PipAnimate> animates = new ArrayList<PipAnimate>();
		for (int i = 0; i < acount; i++) {
			PipAnimate ani = new PipAnimate(ans);
			ani.load(dis, true);
			animates.add(ani);
		}
		
		// 尝试写出帧和动画，找出最适合的版本号
		for (int newv = 0; newv <= 3; newv++) {
			ans.setVersion((byte)newv);
			ByteArrayOutputStream newbos = new ByteArrayOutputStream();
			DataOutputStream ndos = new DataOutputStream(newbos);
			try {
				for (PipAnimateFrame f : frames) {
					f.save(ndos, false);
				}
				ndos.writeByte(acount);
				for (PipAnimate ani : animates) {
					ani.save(ndos, false);
				}
				ndos.flush();
				dos.writeShort((newv << 14) | fcount);
				dos.write(newbos.toByteArray());
				break;
			} catch (IOException e) {
				if (newv == 3) {
					throw e;
				}
				continue;
			}
		}
		
		// 读取源文件部分，完全丢弃，写入新的文件定义
		int fileCount = dis.readByte() & 0x3F;
		for (int i = 0; i < fileCount; i++) {
			String fname = dis.readUTF();
		}
		dos.writeByte(targetImageFileList.size());
		for (int i = 0; i < targetImageFileList.size(); i++) {
			dos.writeUTF(i + ".pip");
		}
	
		// 读取extension段，不修改直接写出
		int ecount = 0;
		try {
			ecount = dis.readByte() & 0xFF;
		} catch (IOException e) {
			return;
		}
		dos.writeByte(ecount);
		for (int i = 0; i < ecount; i++) {
			byte[] buf = new byte[4];
			dis.readFully(buf);
			dos.write(buf);

			int dataLen = dis.readShort() & 0xFFFF;
			byte[] data = new byte[dataLen];
			dis.readFully(data);
			dos.writeShort(dataLen);
			dos.write(data);
		}
	}
	
	public void widgetDisposed(DisposeEvent e) {
		if (previewImage != null) {
			previewImage.dispose();
		}
	}
	
	public void areaSelected(Object source) {}
	
	public void frameSelectionChanged(Object source, int newFrame) {}
	
	public void frameDoubleClicked(Object source, int frame) {
		if (source == sourceImageViewer) {
			// 向左边的图片中添加帧
			int index = sourceImageCombo.getSelectionIndex();
			boolean[] flag = sourceImageVisibleFlag.get(index);
			int[] selFrames = sourceImageViewer.getSelectedFrames();
			int targetIndex = targetImageCombo.getSelectionIndex();
			PipImage targetImage = targetImageList.get(targetIndex);
			for (int i = 0; i < selFrames.length; i++) {
				int fid = selFrames[i];
				if (!flag[fid]) {
					continue;
				}
				
				// 帧加入目标文件
				try {
					PipImage sourceImg = sourceImageList.get(index);
					PipImageData data = sourceImg.getImageData(fid);
					int[] rgb = data.make(sourceImg.isTrueColor() ? null : sourceImg.getImagePalettes().get(0));
					boolean oldFlag = PipImage.limitSize;
					PipImage.limitSize = false;
					int[][] rgb2 = new int[data.height][data.width];
					for (int j = 0; j < data.height; j++) {
						System.arraycopy(rgb, j * data.width, rgb2[j], 0, data.width);
					}
					targetImage.addFrame(rgb2);
					PipImage.limitSize = oldFlag;
				} catch (Exception e) {
					SWTUtils.showError(getShell(), "错误", e);
					break;
				}
				frameMap.put((index << 16) | fid, (targetIndex << 16) | (targetImage.getImgCount() - 1));
				flag[fid] = false;
			}
			sourceImageViewer.redraw();
			targetImageViewer.refresh();
			refreshPreview(targetIndex);
		} else if (source == targetImageViewer) {
			// 删除左边选中的帧
			int targetIndex = targetImageCombo.getSelectionIndex();
			int targetFrame = targetImageViewer.getSelectedFrame();
			PipImage targetImg = targetImageList.get(targetIndex);
			if (targetFrame < 0 || targetFrame >= targetImg.getImgCount()) {
				return;
			}
			targetImg.getImageDatas().remove(targetFrame);
			
			// 找出对应的右边的帧序号，顺便更新一下同图片后面的帧的ID，都减去1
			Object[] arr = frameMap.keySet().toArray();
			for (Object key : arr) {
				int keyValue = ((Integer)key).intValue();
				int targetValue = frameMap.get(key);
				int sourceImageID = keyValue >> 16;
				int sourceFrameID = keyValue & 0xFFFF;
				int targetImageID = targetValue >> 16;
				int targetFrameID = targetValue & 0xFFFF;
				if (targetImageID == targetIndex && targetFrameID == targetFrame) {
					sourceImageVisibleFlag.get(sourceImageID)[sourceFrameID] = true;
					frameMap.remove(keyValue);
				} else if (targetImageID == targetIndex && targetFrameID > targetFrame) {
					targetFrameID--;
					targetValue = (targetImageID << 16) | targetFrameID;
					frameMap.put(keyValue, targetValue);
				}
			}
			sourceImageViewer.redraw();
			targetImageViewer.refresh();
			refreshPreview(targetIndex);
		}
	}
	
	public void contentChanged(Object source) {}
}
