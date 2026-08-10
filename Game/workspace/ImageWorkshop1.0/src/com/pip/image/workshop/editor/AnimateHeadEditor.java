package com.pip.image.workshop.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.mapeditor.CollisionAreaEditor;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.ext.HeadExtension;
import com.swtdesigner.SWTResourceManager;

public class AnimateHeadEditor extends Composite implements ImageViewerListener {
	private Text textHeadHeight;
	private boolean updating = false;
	private Text textHeadWidth;
	private Text textAnimateID;
	private AnimateEditor owner;
	private PipAnimateSet animateSet;
	private Button buttonFlag;
	private CollisionAreaEditor editViewer;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateHeadEditor(Composite parent, int style, AnimateEditor oo, PipAnimateSet aset) {
		super(parent, style);
		this.owner = oo;
		this.animateSet = aset;
		final GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		buttonFlag = new Button(this, SWT.CHECK);
		buttonFlag.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				owner.setDirty(true);
			}
		});
		buttonFlag.setLayoutData(new GridData());
		buttonFlag.setText("包含头像定义");

		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 6;
		composite.setLayout(gridLayout_1);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("动画ID：");

		textAnimateID = new Text(composite, SWT.BORDER);
		textAnimateID.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				if (updating) {
					return;
				}
				try {
					HeadExtension ext = getHeadExt();
					int value = Integer.parseInt(textAnimateID.getText());
					if (value == -1 || (value >= 0 && value < animateSet.getAnimateCount())) {
						ext.headAnimateIndex = value;
						owner.setDirty(true);
						textAnimateID.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
						resetEditView();
						return;
					}
				} catch (Exception e) {
				}
				textAnimateID.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});
		final GridData gd_textAnimateID = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID.setLayoutData(gd_textAnimateID);

		final Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("宽度：");

		textHeadWidth = new Text(composite, SWT.BORDER);
		textHeadWidth.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				if (updating) {
					return;
				}
				try {
					HeadExtension ext = getHeadExt();
					int value = Integer.parseInt(textHeadWidth.getText());
					if (value >= 0) {
						ext.headWidth = value;
						owner.setDirty(true);
						textHeadWidth.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
						resetEditView();
						return;
					}
				} catch (Exception e) {
				}
				textHeadWidth.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});
		final GridData gd_textHeadWidth = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textHeadWidth.setLayoutData(gd_textHeadWidth);

		final Label label_2 = new Label(composite, SWT.NONE);
		label_2.setText("高度：");

		textHeadHeight = new Text(composite, SWT.BORDER);
		textHeadHeight.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				if (updating) {
					return;
				}
				try {
					HeadExtension ext = getHeadExt();
					int value = Integer.parseInt(textHeadHeight.getText());
					if (value >= 0) {
						ext.headHeight = value;
						owner.setDirty(true);
						textHeadHeight.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
						resetEditView();
						return;
					}
				} catch (Exception e) {
				}
				textHeadHeight.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});
		final GridData gd_textHeadHeight = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textHeadHeight.setLayoutData(gd_textHeadHeight);

		final Composite editViewerContainer = new Composite(composite, SWT.NONE);
		editViewerContainer.setLayout(new FillLayout());
		final GridData gd_editViewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		editViewerContainer.setLayoutData(gd_editViewerContainer);
		
		editViewer = new CollisionAreaEditor(editViewerContainer, SWT.NONE);
		editViewer.setImageViewerListener(this);
		editViewer.allowOutRange = true;
		
		// 设置初始值
		HeadExtension ext = (HeadExtension)animateSet.findExtension("HEAD");
		if (ext == null) {
			buttonFlag.setSelection(false);
		} else {
			buttonFlag.setSelection(true);
			updating = true;
			textAnimateID.setText(String.valueOf(ext.headAnimateIndex));
			textHeadWidth.setText(String.valueOf(ext.headWidth));
			textHeadHeight.setText(String.valueOf(ext.headHeight));
			updating = false;
		}
		resetEditView();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void resetEditView() {
		HeadExtension ext = getHeadExt();
		if (ext.headAnimateIndex >= 0 && ext.headAnimateIndex < animateSet.getAnimateCount()) {
			PipAnimate animate = animateSet.getAnimate(ext.headAnimateIndex);
			editViewer.setInput(animate);
			editViewer.setAreaCnt(1);
			editViewer.setAreaSize(ext.headWidth, ext.headHeight);
			Rectangle bounds = animate.getBounds();
			editViewer.setSelectedArea(new Rectangle[] {
					new Rectangle(ext.headX - bounds.x, ext.headY - bounds.y, ext.headWidth, ext.headHeight)
			});
		}
	}
	
	protected HeadExtension getHeadExt() {
		HeadExtension ext = (HeadExtension)animateSet.findExtension("HEAD");
		if (ext == null) {
			ext = new HeadExtension();
			animateSet.addExtension(ext);
		}
		return ext;
	}
	
	public void checkSave() throws Exception {
		if (!buttonFlag.getSelection()) {
			animateSet.removeExtension("HEAD");
			return;
		}
	}

	@Override
	public void areaSelected(Object source) {
	}

	@Override
	public void contentChanged(Object source) {
		Rectangle rect = editViewer.getSelectedArea()[0];
		HeadExtension ext = getHeadExt();
		PipAnimate animate = animateSet.getAnimate(ext.headAnimateIndex);
		Rectangle bounds = animate.getBounds();
		ext.headX = rect.x + bounds.x;
		ext.headY = rect.y + bounds.y;
		owner.setDirty(true);
	}

	@Override
	public void frameDoubleClicked(Object source, int frame) {
	}

	@Override
	public void frameSelectionChanged(Object source, int newFrame) {
	}
}
