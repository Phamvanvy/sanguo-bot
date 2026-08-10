package com.pip.image.workshop.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.pip.image.workshop.AutoBody;
import com.pip.mango.jni.GLUtils;
import com.pip.util.SWTUtils;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipImage;
import com.pipimage.utils.ImageUtil;

public class CutPieceDialog extends Dialog implements ImageViewerListener, DisposeListener {
	private ImageViewer previewer;
	private ImageViewer pieceSelector;
	private Image sourceImage;
	private PipImage previewImage;
	private List<int[]> cutAreas;
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public CutPieceDialog(Shell parentShell, Image img) {
		super(parentShell);
		sourceImage = img;
		previewImage = new PipImage();
		previewImage.setTrueColor(true);
		cutAreas = new ArrayList<int[]>();
	}
	
	/**
	 * 取得拆分方案。
	 * @return
	 */
	public int[][] getCutAreas() {
		int[][] ret = new int[cutAreas.size()][];
		cutAreas.toArray(ret);
		return ret;
	}
	
	/**
	 * 取得拆分出来的帧。
	 * @return
	 */
	public PipImage getCutImage() {
		return previewImage;
	}
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		previewer = new ImageViewer(composite, SWT.NONE);
		previewer.setEditable(false);
		previewer.setImageViewerListener(this);
		previewer.setInput(previewImage);

		final Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new FillLayout());
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pieceSelector = new ImageViewer(composite_1, SWT.NONE);
		pieceSelector.setImageViewerListener(this);
		pieceSelector.setInput(sourceImage);
		
		container.addDisposeListener(this);
		
		return container;
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
		return new Point(948, 643);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("拆分");
	}
	
	public void areaSelected(Object source) {
		// 在下方选择了一个区域以后，双击
		Rectangle area = pieceSelector.getSelectedArea();
		
		// 提取图块区域像素
		int[][] rawData = ImageViewer.getImageData(sourceImage, area);
		Rectangle untransArea = AutoBody.findUntransparentArea(rawData);
		if (untransArea.width == 1 && untransArea.height == 1 &&
				(rawData[untransArea.y][untransArea.x] & 0xFF000000) == 0) {
			MessageDialog.openError(getShell(), "错误", "选中区域没有不透明的像素。");
			pieceSelector.setInput(sourceImage);
			return;
		}
		untransArea.x += area.x;
		untransArea.y += area.y;
		int[][] untransData = ImageViewer.getImageData(sourceImage, untransArea);
		
		try {
			// 添加到预览区域，并把对应区域添加到区域列表
			previewImage.addFrame(untransData);
			cutAreas.add(new int[] { untransArea.x, untransArea.y, untransArea.width, untransArea.height });
			previewer.refresh();
			
			// 修改sourceImage，把选中区域全部清空，并重新构建
			rawData = ImageViewer.getImageData(sourceImage, sourceImage.getBounds());
			for (int y = area.y; y < area.y + area.height; y++) {
				for (int x = area.x; x < area.x + area.width; x++) {
					rawData[y][x] = 0;
				}
			}
			Image oldImage = sourceImage;
			sourceImage = ImageUtil.createRGBImage(getShell().getDisplay(), rawData);
			rawData = ImageViewer.getImageData(sourceImage, sourceImage.getBounds());
			pieceSelector.setInput(sourceImage);
			GLUtils.unloadImage(oldImage);
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}

	}
	
	public void frameSelectionChanged(Object source, int newFrame) {
	}

	public void frameDoubleClicked(Object source, int frame) {
	}
	
	public void contentChanged(Object source) {}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// 检查是否还有剩余的未分配像素
			int[][] rawData = ImageViewer.getImageData(sourceImage, sourceImage.getBounds());
			Rectangle untransArea = AutoBody.findUntransparentArea(rawData);
			if (untransArea.width == 1 && untransArea.height == 1 &&
					(rawData[untransArea.y][untransArea.x] & 0xFF000000) == 0) {
				super.buttonPressed(buttonId);
			} else {
				MessageDialog.openError(getShell(), "错误", "还有未提取的非透明像素。");
			}
			return;
		}
		super.buttonPressed(buttonId);
	}
	
	public void widgetDisposed(DisposeEvent e) {
		if (sourceImage != null) {
			GLUtils.unloadImage(sourceImage);
		}
	}
}
