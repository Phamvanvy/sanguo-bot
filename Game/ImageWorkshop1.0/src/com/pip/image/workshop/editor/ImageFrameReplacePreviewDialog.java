package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;

public class ImageFrameReplacePreviewDialog extends Dialog {
	protected PipAnimateSet animateSet;
	protected int imageID;
	protected int sourceFrame;
	protected int targetFrame;
	protected int adjustX;
	protected int adjustY;
	protected int adjustTrans;
	
	protected boolean[] visibleFlags;
	protected TileLibSelector sourcePreviewer;
	protected TileLibSelector targetPreviewer;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ImageFrameReplacePreviewDialog(Shell parentShell, PipAnimateSet animateSet,
			int imageID, int sourceFrame, int targetFrame, int adjustX, int adjustY, int trans) {
		super(parentShell);
		this.animateSet = animateSet;
		this.imageID = imageID;
		this.sourceFrame = sourceFrame;
		this.targetFrame = targetFrame;
		this.adjustX = adjustX;
		this.adjustY = adjustY;
		this.adjustTrans = trans;
		
		// 搜索出包含指定图块的动画序列
		visibleFlags = new boolean[animateSet.getAnimateCount()];
		for (int i = 0; i < animateSet.getAnimateCount(); i++) {
			PipAnimate pa = animateSet.getAnimate(i);
			for (int j = 0; j < pa.getFrameCount(); j++) {
				PipAnimateFrame paf = pa.getFrame(j).realize();
				for (int k = 0; k < paf.getPieceCount(); k++) {
					PipAnimateFramePiece pfp = paf.getPiece(k);
					if (pfp.getImageID() == imageID && pfp.getFrame() == sourceFrame) {
						visibleFlags[i] = true;
						break;
					}
				}
				if (visibleFlags[i]) {
					break;
				}
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

		final Label label = new Label(container, SWT.NONE);
		label.setText("替换前：");

		final Composite sourceContainer = new Composite(container, SWT.NONE);
		sourceContainer.setLayout(new FillLayout());
		final GridData gd_sourceContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		sourceContainer.setLayoutData(gd_sourceContainer);
		
		sourcePreviewer = new TileLibSelector(sourceContainer, SWT.NONE);
		
		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("替换后：");

		final Composite targetContainer = new Composite(container, SWT.NONE);
		targetContainer.setLayout(new FillLayout());
		final GridData gd_targetContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		targetContainer.setLayoutData(gd_targetContainer);

		targetPreviewer = new TileLibSelector(targetContainer, SWT.NONE);
		
		sourcePreviewer.setInput(animateSet, 16, 16);
		sourcePreviewer.setAnimateVisibleFlag(visibleFlags);
		targetPreviewer.setInput(animateSet, 16, 16);
		targetPreviewer.setAnimateVisibleFlag(visibleFlags);
		targetPreviewer.setReplaceImage(imageID, sourceFrame, targetFrame, adjustX, adjustY, adjustTrans);

		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(810, 644);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("预览替换效果");
	}
}
