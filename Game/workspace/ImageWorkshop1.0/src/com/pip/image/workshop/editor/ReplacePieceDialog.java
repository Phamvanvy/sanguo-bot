package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipImage;

public class ReplacePieceDialog extends Dialog implements ImageViewerListener {
	private AnimateFrameMultiSelector previewer;
	private ImageViewer pieceSelector;
	private PipAnimateFrame[] matchFrames;
	private PipImage candidateImage;
	private int srcImageID;
	private int srcFrameID;
	private List<PipAnimateFramePiece> affectPieces = new ArrayList<PipAnimateFramePiece>();
	private List<PipAnimateFramePiece> affectPiecesBackup = new ArrayList<PipAnimateFramePiece>();
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public ReplacePieceDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setData(PipAnimateFrame[] frames, PipImage image, int imageID, int frameID) {
		matchFrames = frames;
		candidateImage = image;
		srcImageID = imageID;
		srcFrameID = frameID;
		
		for (PipAnimateFrame frame : frames) {
			for (int i = 0; i < frame.getPieceCount(); i++) {
				PipAnimateFramePiece p = frame.getPiece(i);
				if (p.getImageID() == srcImageID && p.getFrame() == srcFrameID) {
					affectPieces.add(p);
					affectPiecesBackup.add((PipAnimateFramePiece)p.clone());
				}
			}
		}
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
		previewer = new AnimateFrameMultiSelector(composite, SWT.NONE);
		previewer.setImageViewerListener(this);
		previewer.setInput(matchFrames);
		for (int i = 0; i < matchFrames.length; i++) {
			previewer.setFrameSelected(i, true);
		}

		final Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new FillLayout());
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pieceSelector = new ImageViewer(composite_1, SWT.NONE);
		pieceSelector.setImageViewerListener(this);
		pieceSelector.setInput(candidateImage);
		pieceSelector.setSelectedFrame(srcFrameID);
		
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
		newShell.setText("替换图块");
	}
	
	protected void restore() {
		for (int i = 0; i < affectPieces.size(); i++) {
			PipAnimateFramePiece p1 = affectPieces.get(i);
			PipAnimateFramePiece p2 = affectPiecesBackup.get(i);
			p1.setFrame(p2.getFrame());
			p1.setTransition(p2.getTransition());
			p1.setDx(p2.getDx());
			p1.setDy(p2.getDy());
		}
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CANCEL_ID) {
			// 取消时恢复数据
			restore();
		}
		super.buttonPressed(buttonId);
	}
	
	private boolean isAffected(PipAnimateFramePiece p) {
		return affectPieces.contains(p);
	}
	
	protected void setup(int newFrame) {
		// 选中改变时，修改选中帧中的图块
		for (int i = 0; i < matchFrames.length; i++) {
			if (!previewer.isFrameSelected(i)) {
				continue;
			}
			for (int j = 0; j < matchFrames[i].getPieceCount(); j++) {
				PipAnimateFramePiece p = matchFrames[i].getPiece(j);
				if (isAffected(p)) {
					p.setFrame(newFrame);
				}
			}
		}
		previewer.refresh();
	}
	
	public void areaSelected(Object source) {}
	public void frameSelectionChanged(Object source, int newFrame) {
		if (source == pieceSelector) {
			setup(newFrame);
		} else if (source == previewer) {
			if (previewer.isFrameSelected(newFrame)) {
				// 替换
				if (pieceSelector.getSelectedFrame() == -1) {
					return;
				}
				for (int j = 0; j < matchFrames[newFrame].getPieceCount(); j++) {
					PipAnimateFramePiece p = matchFrames[newFrame].getPiece(j);
					if (isAffected(p)) {
						p.setFrame(pieceSelector.getSelectedFrame());
					}
				}
				previewer.refresh();
			} else {
				// 取消替换
				for (int i = 0; i < affectPieces.size(); i++) {
					PipAnimateFramePiece p1 = affectPieces.get(i);
					PipAnimateFramePiece p2 = affectPiecesBackup.get(i);
					if (p1.getParent() == matchFrames[newFrame]) {
						p1.setFrame(p2.getFrame());
						p1.setTransition(p2.getTransition());
						p1.setDx(p2.getDx());
						p1.setDy(p2.getDy());
					}
				}
				previewer.refresh();
			}
		}
	}
	public void frameDoubleClicked(Object source, int frame) {
		if (source == previewer) {
			PipAnimateFrame pframe = matchFrames[frame];
			List<PipAnimateFramePiece> pcs = new ArrayList<PipAnimateFramePiece>();
			for (PipAnimateFramePiece p : affectPieces) {
				if (p.getParent() == pframe) {
					pcs.add(p);
				}
			}
			EditSinglePieceDialog dlg = new EditSinglePieceDialog(getShell());
			dlg.setData(pframe, pcs);
			dlg.open();
			previewer.refresh();
		}
	}
	public void contentChanged(Object source) {}
}
