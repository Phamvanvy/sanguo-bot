package com.pip.image.workshop.editor;

import java.text.DecimalFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 本组件显示一个图片的可替换图片。
 * @author lighthu
 */
public class ImageFrameReplaceViewer extends Composite {
	public static interface EventListener {
		public void onPreviewReplace(ImageFrameReplaceViewer source, int sourceFrame, int targetFrame);
	}
	
	protected int frameID;
	protected Image[] images;
	protected int[] replaces;
	protected double[] replaceRates;
	protected EventListener listener;
	
	protected Button[] replaceButtons;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ImageFrameReplaceViewer(Composite parent, int style, int frameID, Image[] images, int[] replaces, double[] replaceRates) {
		super(parent, style);
		this.frameID = frameID;
		this.images = images;
		this.replaces = replaces;
		this.replaceRates = replaceRates;
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3 + replaces.length;
		setLayout(gridLayout);

		final Label labelFrameName = new Label(this, SWT.NONE);
		labelFrameName.setText("帧" + (frameID + 1) + "：");

		final Label labelSourceImage = new Label(this, SWT.NONE);
		final GridData gd_labelSourceImage = new GridData(70, 70);
		labelSourceImage.setLayoutData(gd_labelSourceImage);
		labelSourceImage.setImage(images[frameID]);

		final Label label = new Label(this, SWT.NONE);
		label.setText("替换为：");

		replaceButtons = new Button[replaces.length];
		for (int i = 0; i < replaces.length; i++) {
			replaceButtons[i] = new Button(this, SWT.NONE);
			replaceButtons[i].setLayoutData(new GridData(120, 70));
			replaceButtons[i].setImage(images[replaces[i]]);
			replaceButtons[i].setText(formatPercent(replaceRates[i]));
			replaceButtons[i].addSelectionListener(new SelectionAdapter() {
	        	public void widgetSelected(final SelectionEvent e) {
	        		for (int j = 0; j < replaceButtons.length; j++) {
	        			if (e.widget == replaceButtons[j]) {
	        				previewReplace(j);
	        			}
	        		}
	        	}
	        });
		}
	}
	
	public int getFrameID() {
		return frameID;
	}
	
	public void setListener(EventListener l) {
		listener = l;
	}
	
	// 打开一个图块替换
	protected void previewReplace(int index) {
		if (listener != null) {
			listener.onPreviewReplace(this, frameID, replaces[index]);
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private static final DecimalFormat percentFormat = new DecimalFormat("####.##"); 
    
    public static String formatPercent(double p) {
        return percentFormat.format(p * 100) + "%";
    }
}
