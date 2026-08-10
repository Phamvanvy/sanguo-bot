package com.pip.servermgr.client;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchLogDialog extends Dialog {
	private Combo comboFilters;
	private Text textMaxSize;
	private Text textSearchText;
	private Button buttonRegEx;
	private DateTime startDateChooser;
	private DateTime startTimeChooser;
	private DateTime endDateChooser;
	private DateTime endTimeChooser;
	
	public String searchText;
	public boolean isRegEx;
	public Date startTime;
	public Date endTime;
	public long maxSize = 1024000;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SearchLogDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.NONE);
		label.setText("搜索：");

		textSearchText = new Text(container, SWT.BORDER);
		final GridData gd_textSearchText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textSearchText.setLayoutData(gd_textSearchText);

		buttonRegEx = new Button(container, SWT.CHECK);
		buttonRegEx.setText("正则表达式");

		comboFilters = new Combo(container, SWT.READ_ONLY);
		comboFilters.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onAddFilter(comboFilters.getSelectionIndex());
			}
		});
		comboFilters.setItems(new String[] {"常用过滤条件...", "账号ID(仅适用于新日志格式)", "角色ID", "两个关键字"});
		comboFilters.select(0);
		final GridData gd_comboFilters = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		comboFilters.setLayoutData(gd_comboFilters);

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("开始时间：");

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		gridLayout_1.marginWidth = 0;
		gridLayout_1.marginHeight = 0;
		composite.setLayout(gridLayout_1);

		startDateChooser = new DateTime(composite, SWT.DATE);
		startTimeChooser = new DateTime(composite, SWT.TIME);

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("结束时间：");

		final Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		gridLayout_2.marginWidth = 0;
		gridLayout_2.marginHeight = 0;
		composite_1.setLayout(gridLayout_2);

		endDateChooser = new DateTime(composite_1, SWT.DATE);
		endTimeChooser = new DateTime(composite_1, SWT.TIME);

		final Label label_3 = new Label(container, SWT.NONE);
		label_3.setText("最大字节数：");

		textMaxSize = new Text(container, SWT.BORDER);
		textMaxSize.setText("1024000");
		final GridData gd_textMaxSize = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		textMaxSize.setLayoutData(gd_textMaxSize);
		
		setDefaults();
		
		return container;
	}
	
	private void setDefaults() {
		if (searchText != null) {
			textSearchText.setText(searchText);
		}
		buttonRegEx.setSelection(isRegEx);
		if (startTime != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startTime);
			startDateChooser.setYear(cal.get(Calendar.YEAR));
			startDateChooser.setMonth(cal.get(Calendar.MONTH));
			startDateChooser.setDay(cal.get(Calendar.DAY_OF_MONTH));
			startTimeChooser.setHours(cal.get(Calendar.HOUR_OF_DAY));
			startTimeChooser.setMinutes(cal.get(Calendar.MINUTE));
			startTimeChooser.setSeconds(cal.get(Calendar.SECOND));
		} else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -1);
			startDateChooser.setYear(cal.get(Calendar.YEAR));
			startDateChooser.setMonth(cal.get(Calendar.MONTH));
			startDateChooser.setDay(cal.get(Calendar.DAY_OF_MONTH));
			startTimeChooser.setHours(0);
			startTimeChooser.setMinutes(0);
			startTimeChooser.setSeconds(0);
		}
		if (endTime != null) {
			Calendar cal = Calendar.getInstance();
			endDateChooser.setYear(cal.get(Calendar.YEAR));
			endDateChooser.setMonth(cal.get(Calendar.MONTH));
			endDateChooser.setDay(cal.get(Calendar.DAY_OF_MONTH));
			endTimeChooser.setHours(23);
			endTimeChooser.setMinutes(59);
			endTimeChooser.setSeconds(59);
		}
		textMaxSize.setText(String.valueOf(maxSize));
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "查询",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"退出", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(501, 251);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("查询条件");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			searchText = textSearchText.getText();
			if (searchText.length() == 0) {
				MessageDialog.openInformation(getShell(), "输入", "必须输入一个查询条件。");
				return;
			}
			isRegEx = buttonRegEx.getSelection();
			Calendar cal = Calendar.getInstance();
			cal.set(startDateChooser.getYear(), startDateChooser.getMonth(), startDateChooser.getDay(), 
					startTimeChooser.getHours(), startTimeChooser.getMinutes(), startTimeChooser.getSeconds());
			startTime = cal.getTime();
			cal.set(endDateChooser.getYear(), endDateChooser.getMonth(), endDateChooser.getDay(), 
					endTimeChooser.getHours(), endTimeChooser.getMinutes(), endTimeChooser.getSeconds());
			endTime = cal.getTime();
			try {
				maxSize = Long.parseLong(textMaxSize.getText());
			} catch (Exception e) {
				MessageDialog.openInformation(getShell(), "输入", "最大字节数输入错误。");
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
	
	protected void onAddFilter(int filterType) {
		if (filterType == 1) {
			// 账号ID 
			InputDialog dlg = new InputDialog(getShell(), "请输入账号ID", "", "", null);
			if (dlg.open() == InputDialog.OK) {
				String key = dlg.getValue();
				textSearchText.setText("]ACC[" + key + "]");
				buttonRegEx.setSelection(false);
			}
		} else if (filterType == 2) {
			// 角色ID
			InputDialog dlg = new InputDialog(getShell(), "请输入角色ID", "", "", null);
			if (dlg.open() == InputDialog.OK) {
				String key = dlg.getValue();
				textSearchText.setText("]ID[" + key + "]");
				buttonRegEx.setSelection(false);
			}
		} else if (filterType == 3) {
			// 两个关键字
			InputDialog dlg = new InputDialog(getShell(), "请输入第一个关键字", "", "", null);
			if (dlg.open() != InputDialog.OK) {
				return;
			}
			String key1 = dlg.getValue();
			dlg = new InputDialog(getShell(), "请输入第二个关键字", "", "", null);
			if (dlg.open() != InputDialog.OK) {
				return;
			}
			String key2 = dlg.getValue();
			textSearchText.setText(".*" + key1 + ".*|.*" + key2 + ".*");
			buttonRegEx.setSelection(true);
		}
	}
}
