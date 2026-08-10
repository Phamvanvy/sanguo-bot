package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.ext.CharacterActionExtension;

public class AnimateActionEditor extends Composite implements ModifyListener {
	private Text textAnimateID10;
	private Text textActionName10;
	private Text textAnimateID9;
	private Text textActionName9;
	private Text textAnimateID8;
	private Text textActionName8;
	private Text textAnimateID7;
	private Text textActionName7;
	private Text textAnimateID6;
	private Text textActionName6;
	private Text textAnimateID5;
	private Text textActionName5;
	private Text textAnimateID4;
	private Text textActionName4;
	private Text textAnimateID3;
	private Text textActionName3;
	private Text textAnimateID2;
	private Text textActionName2;
	private Text textAnimateID1;
	private boolean updating = false;
	private Text textActionName1;
	private AnimateEditor owner;
	private PipAnimateSet animateSet;
	
	private Text[] actionNameFields;
	private Text[] animateIDFields;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateActionEditor(Composite parent, int style, AnimateEditor oo, PipAnimateSet aset) {
		super(parent, style);
		this.owner = oo;
		this.animateSet = aset;
		final GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 4;
		composite.setLayout(gridLayout_1);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("动作名称：");

		textActionName1 = new Text(composite, SWT.BORDER);
		textActionName1.addModifyListener(this);
		final GridData gd_textActionName1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName1.setLayoutData(gd_textActionName1);

		final Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("对应动画序列ID：");

		textAnimateID1 = new Text(composite, SWT.BORDER);
		textAnimateID1.addModifyListener(this);
		final GridData gd_textAnimateID1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID1.setLayoutData(gd_textAnimateID1);

		final Label label_2 = new Label(composite, SWT.NONE);
		label_2.setText("动作名称：");

		textActionName2 = new Text(composite, SWT.BORDER);
		textActionName2.addModifyListener(this);
		final GridData gd_textActionName2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName2.setLayoutData(gd_textActionName2);

		final Label label_3 = new Label(composite, SWT.NONE);
		label_3.setText("对应动画序列ID：");

		textAnimateID2 = new Text(composite, SWT.BORDER);
		textAnimateID2.addModifyListener(this);
		final GridData gd_textAnimateID2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID2.setLayoutData(gd_textAnimateID2);

		final Label label_4 = new Label(composite, SWT.NONE);
		label_4.setText("动作名称：");

		textActionName3 = new Text(composite, SWT.BORDER);
		textActionName3.addModifyListener(this);
		final GridData gd_textActionName3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName3.setLayoutData(gd_textActionName3);

		final Label label_5 = new Label(composite, SWT.NONE);
		label_5.setText("对应动画序列ID：");

		textAnimateID3 = new Text(composite, SWT.BORDER);
		textAnimateID3.addModifyListener(this);
		final GridData gd_textAnimateID3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID3.setLayoutData(gd_textAnimateID3);

		final Label label_6 = new Label(composite, SWT.NONE);
		label_6.setText("动作名称：");

		textActionName4 = new Text(composite, SWT.BORDER);
		textActionName4.addModifyListener(this);
		final GridData gd_textActionName4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName4.setLayoutData(gd_textActionName4);

		final Label label_7 = new Label(composite, SWT.NONE);
		label_7.setText("对应动画序列ID：");

		textAnimateID4 = new Text(composite, SWT.BORDER);
		textAnimateID4.addModifyListener(this);
		final GridData gd_textAnimateID4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID4.setLayoutData(gd_textAnimateID4);

		final Label label_8 = new Label(composite, SWT.NONE);
		label_8.setText("动作名称：");

		textActionName5 = new Text(composite, SWT.BORDER);
		textActionName5.addModifyListener(this);
		final GridData gd_textActionName5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName5.setLayoutData(gd_textActionName5);

		final Label label_9 = new Label(composite, SWT.NONE);
		label_9.setText("对应动画序列ID：");

		textAnimateID5 = new Text(composite, SWT.BORDER);
		textAnimateID5.addModifyListener(this);
		final GridData gd_textAnimateID5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID5.setLayoutData(gd_textAnimateID5);

		final Label label_10 = new Label(composite, SWT.NONE);
		label_10.setText("动作名称：");

		textActionName6 = new Text(composite, SWT.BORDER);
		textActionName6.addModifyListener(this);
		final GridData gd_textActionName6 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName6.setLayoutData(gd_textActionName6);

		final Label label_11 = new Label(composite, SWT.NONE);
		label_11.setText("对应动画序列ID：");

		textAnimateID6 = new Text(composite, SWT.BORDER);
		textAnimateID6.addModifyListener(this);
		final GridData gd_textAnimateID6 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID6.setLayoutData(gd_textAnimateID6);

		final Label label_12 = new Label(composite, SWT.NONE);
		label_12.setText("动作名称：");

		textActionName7 = new Text(composite, SWT.BORDER);
		textActionName7.addModifyListener(this);
		final GridData gd_textActionName7 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName7.setLayoutData(gd_textActionName7);

		final Label label_13 = new Label(composite, SWT.NONE);
		label_13.setText("对应动画序列ID：");

		textAnimateID7 = new Text(composite, SWT.BORDER);
		textAnimateID7.addModifyListener(this);
		final GridData gd_textAnimateID7 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID7.setLayoutData(gd_textAnimateID7);

		final Label label_14 = new Label(composite, SWT.NONE);
		label_14.setText("动作名称：");

		textActionName8 = new Text(composite, SWT.BORDER);
		textActionName8.addModifyListener(this);
		final GridData gd_textActionName8 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName8.setLayoutData(gd_textActionName8);

		final Label label_15 = new Label(composite, SWT.NONE);
		label_15.setText("对应动画序列ID：");

		textAnimateID8 = new Text(composite, SWT.BORDER);
		textAnimateID8.addModifyListener(this);
		final GridData gd_textAnimateID8 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID8.setLayoutData(gd_textAnimateID8);

		final Label label_16 = new Label(composite, SWT.NONE);
		label_16.setText("动作名称：");

		textActionName9 = new Text(composite, SWT.BORDER);
		textActionName9.addModifyListener(this);
		final GridData gd_textActionName9 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName9.setLayoutData(gd_textActionName9);

		final Label label_17 = new Label(composite, SWT.NONE);
		label_17.setText("对应动画序列ID：");

		textAnimateID9 = new Text(composite, SWT.BORDER);
		textAnimateID9.addModifyListener(this);
		final GridData gd_textAnimateID9 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID9.setLayoutData(gd_textAnimateID9);

		final Label label_18 = new Label(composite, SWT.NONE);
		label_18.setText("动作名称：");

		textActionName10 = new Text(composite, SWT.BORDER);
		textActionName10.addModifyListener(this);
		final GridData gd_textActionName10 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textActionName10.setLayoutData(gd_textActionName10);

		final Label label_19 = new Label(composite, SWT.NONE);
		label_19.setText("对应动画序列ID：");

		textAnimateID10 = new Text(composite, SWT.BORDER);
		textAnimateID10.addModifyListener(this);
		final GridData gd_textAnimateID10 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textAnimateID10.setLayoutData(gd_textAnimateID10);

		final Label label_20 = new Label(composite, SWT.NONE);
		label_20.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		label_20.setText("注意：动作名称必须是4个字符的英文或者数字。");
		
		actionNameFields = new Text[] {
				textActionName1, textActionName2, textActionName3, textActionName4, textActionName5,
				textActionName6, textActionName7, textActionName8, textActionName9, textActionName10
		};
		animateIDFields = new Text[] {
				textAnimateID1, textAnimateID2, textAnimateID3, textAnimateID4, textAnimateID5, 
				textAnimateID6, textAnimateID7, textAnimateID8, textAnimateID9, textAnimateID10
		};
		
		// 设置初始值
		CharacterActionExtension ext = (CharacterActionExtension)animateSet.findExtension("CACT");
		if (ext != null) {
			for (int i = 0; i < ext.actionAnimate.size(); i++) {
				actionNameFields[i].setText(ext.actionNames.get(i));
				animateIDFields[i].setText(String.valueOf(ext.actionAnimate.get(i)));
			}
		}
	}
	
	public void modifyText(final ModifyEvent arg0) {
		if (updating) {
			return;
		}
		owner.setDirty(true);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected CharacterActionExtension getExtension() {
		CharacterActionExtension ext = (CharacterActionExtension)animateSet.findExtension("CACT");
		if (ext == null) {
			ext = new CharacterActionExtension();
			animateSet.addExtension(ext);
		}
		return ext;
	}
	
	public void checkSave() throws Exception {
		List<String> names = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			String t1 = actionNameFields[i].getText().trim();
			String t2 = animateIDFields[i].getText().trim();
			if (t1.length() == 0 && t2.length() == 0) {
				continue;
			}
			if (t1.length() != 4) {
				throw new Exception("动作名称必须是4个字符。");
			}
			for (int j = 0; j < 4; j++) {
				char ch = t1.charAt(j);
				if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
					continue;
				} else {
					throw new Exception("动作名称只能包含字符和数字。");
				}
			}
			int aid;
			try {
				aid = Integer.parseInt(t2);
			} catch (Exception e) {
				throw new Exception("动画ID必须为数字。");
			}
			if (aid < 0 || aid >= animateSet.getAnimateCount()) {
				throw new Exception("动画ID越界，必须在0到" + (animateSet.getAnimateCount() - 1) + "之间。");
			}
			names.add(t1);
			ids.add(Integer.parseInt(t2));
		}
		if (names.size() == 0) {
			animateSet.removeExtension("CACT");
		} else {
			CharacterActionExtension ext = getExtension();
			ext.actionNames.clear();
			ext.actionAnimate.clear();
			ext.actionNames.addAll(names);
			ext.actionAnimate.addAll(ids);
		}
	}
}
