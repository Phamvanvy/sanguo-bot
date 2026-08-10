package com.pip.servermgr.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.servermgr.data.TextFile;

public class AdvancedAnalyseEditor extends EditorPart {
	class FieldListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element == keyField) {
				return element.toString() + "(*)";
			} else {
				return element.toString();
			}
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ReportTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			int row = ((Integer)element).intValue();
			return reportData.get(row)[columnIndex];
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	class ReportTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Object[] ret = new Object[reportData.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class FieldListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return dataFields.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private org.eclipse.swt.widgets.List fieldList;
	private Table dataTable;
	private Text textCurFile;
	private StyledText logEditText;
	private Text textFilter;
	private Text textSourceFile;
	private Button buttonPrevPage;
	private Label labelPage;
	private Button buttonNextPage;
	private Button buttonPrevStep;
	public static final String ID = "com.pip.servermgr.client.AdvancedAnalyseEditor"; //$NON-NLS-1$
	
	protected File initLogFile;
	protected List<File> tempFiles = new ArrayList<File>();
	protected List<String> filters = new ArrayList<String>();
	protected TextFile currentTextFile;
	protected int currentPage = 1;

	// 字段定义
	static class FieldDef {
		public String name;			// 字段名称（或要汇总的字段的名称）
		public String prefix;		// 提取前缀
		public String suffix;		// 提取后缀
		public boolean isSumField;  // 是否汇总字段
		public int sumType;			// 汇总类型：0 - 计数，1 - 求和，2 - 平均，3 - 最大，4 - 最小
		
		public String toString() {
			if (isSumField) {
				switch (sumType) {
				case 0:
					return "count(" + name + ")";
				case 1:
					return "sum(" + name + ")";
				case 2:
					return "average(" + name + ")";
				case 3:
					return "max(" + name + ")";
				case 4:
					return "min(" + name + ")";
				}
				return name;
			} else {
				return name;
			}
		}
		
		public FieldDef dup() {
			FieldDef ret = new FieldDef();
			ret.name = name;
			ret.prefix = prefix;
			ret.suffix = suffix;
			ret.isSumField = isSumField;
			ret.sumType = sumType;
			return ret;
		}
	}
	
	protected List<FieldDef> dataFields = new ArrayList<FieldDef>();
	protected FieldDef keyField = null;
	protected List<FieldDef> reportFields = new ArrayList<FieldDef>();
	protected List<String[]> reportData = new ArrayList<String[]>();
	private TableViewer dataTableViewer;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final CTabFolder tabFolder = new CTabFolder(container, SWT.NONE);

		final CTabItem filterTabItem = new CTabItem(tabFolder, SWT.NONE);
		filterTabItem.setText("过滤");

		final Composite composite = new Composite(tabFolder, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);
		filterTabItem.setControl(composite);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("源文件：");

		textSourceFile = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_textSourceFile = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textSourceFile.setLayoutData(gd_textSourceFile);

		final Button buttonBrowse = new Button(composite, SWT.NONE);
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
				dlg.setFilterNames(new String[] { "日志文件(*.log)", "所有文件(*.*)" });
				dlg.setFilterExtensions(new String[] { "*.log", "*.*" });
				dlg.setFilterPath(initLogFile.getParent());
				String newFile = dlg.open();
				if (newFile != null) {
					for (int i = 0; i < tempFiles.size(); i++) {
						tempFiles.get(i).delete();
					}
					initLogFile = new File(newFile);
					tempFiles.clear();
					filters.clear();
					loadPage(1);
				}
			}
		});
		buttonBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		buttonBrowse.setText("更换..");

		final Label label_2 = new Label(composite, SWT.NONE);
		label_2.setText("当前文件：");

		textCurFile = new Text(composite, SWT.BORDER);
		textCurFile.setEditable(false);
		final GridData gd_textCurFile = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textCurFile.setLayoutData(gd_textCurFile);

		final Button buttonOpenDir = new Button(composite, SWT.NONE);
		buttonOpenDir.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				File f = getCurrentFile();
            	if (f.isFile()) {
            		f = f.getParentFile();
            	}
                String cmd = "explorer.exe \"" + f.getAbsolutePath() + "\"";
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception e1) {
                }
			}
		});
		buttonOpenDir.setText("浏览...");

		final Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("筛选条件：");

		textFilter = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_textFilter = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textFilter.setLayoutData(gd_textFilter);

		final Button buttonFilter = new Button(composite, SWT.NONE);
		buttonFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				InputDialog dlg = new InputDialog(getSite().getShell(), "输入", "请输入筛选关键字（如果要使用正则表达式，请用reg:开头）：", "", null);
				if (dlg.open() == InputDialog.OK) {
					String value = dlg.getValue();
					if (value.length() == 0) {
						return;
					}
					addFilter(value);
				}
			}
		});
		final GridData gd_buttonFilter = new GridData(SWT.FILL, SWT.CENTER, false, false);
		buttonFilter.setLayoutData(gd_buttonFilter);
		buttonFilter.setText("添加...");

		logEditText = new StyledText(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		logEditText.setEditable(false);
		final GridData gd_logEditText = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		logEditText.setLayoutData(gd_logEditText);

		final Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.verticalSpacing = 0;
		gridLayout_1.marginWidth = 0;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.numColumns = 2;
		composite_2.setLayout(gridLayout_1);

		final Composite composite_4 = new Composite(composite_2, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.verticalSpacing = 0;
		gridLayout_2.marginWidth = 0;
		gridLayout_2.marginHeight = 0;
		gridLayout_2.numColumns = 3;
		composite_4.setLayout(gridLayout_2);

		buttonPrevPage = new Button(composite_4, SWT.NONE);
		buttonPrevPage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				loadPage(currentPage - 1);
			}
		});
		buttonPrevPage.setEnabled(false);
		buttonPrevPage.setText("上一页");

		labelPage = new Label(composite_4, SWT.CENTER);
		final GridData gd_labelPage = new GridData(50, SWT.DEFAULT);
		labelPage.setLayoutData(gd_labelPage);
		labelPage.setText("1/1");

		buttonNextPage = new Button(composite_4, SWT.NONE);
		buttonNextPage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				loadPage(currentPage + 1);
			}
		});
		buttonNextPage.setEnabled(false);
		buttonNextPage.setText("下一页");

		buttonPrevStep = new Button(composite_2, SWT.NONE);
		buttonPrevStep.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (tempFiles.size() > 0) {
					tempFiles.get(tempFiles.size() - 1).delete();
					tempFiles.remove(tempFiles.size() - 1);
					filters.remove(filters.size() - 1);
					loadPage(1);
				}
			}
		});
		buttonPrevStep.setEnabled(false);
		final GridData gd_buttonPrevStep = new GridData(SWT.FILL, SWT.CENTER, false, false);
		buttonPrevStep.setLayoutData(gd_buttonPrevStep);
		buttonPrevStep.setText("上一步");

		final CTabItem reportTabItem = new CTabItem(tabFolder, SWT.NONE);
		reportTabItem.setText("统计");

		final Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		composite_1.setLayout(gridLayout_3);
		reportTabItem.setControl(composite_1);

		final ListViewer fieldListViewer = new ListViewer(composite_1, SWT.BORDER);
		fieldListViewer.setLabelProvider(new FieldListLabelProvider());
		fieldListViewer.setContentProvider(new FieldListContentProvider());
		fieldList = fieldListViewer.getList();
		final GridData gd_fieldList = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd_fieldList.widthHint = 200;
		fieldList.setLayoutData(gd_fieldList);
		fieldListViewer.setInput(this);

		dataTableViewer = new TableViewer(composite_1, SWT.BORDER);
		dataTableViewer.setLabelProvider(new ReportTableLabelProvider());
		dataTableViewer.setContentProvider(new ReportTableContentProvider());
		dataTable = dataTableViewer.getTable();
		dataTable.setLinesVisible(true);
		dataTable.setHeaderVisible(true);
		final GridData gd_dataTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		dataTable.setLayoutData(gd_dataTable);

		final Composite composite_5 = new Composite(composite_1, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		final GridLayout gridLayout_4 = new GridLayout();
		gridLayout_4.numColumns = 5;
		composite_5.setLayout(gridLayout_4);

		final Button buttonAddField = new Button(composite_5, SWT.NONE);
		buttonAddField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				// 输入字段名称
				InputDialog dlg = new InputDialog(getSite().getShell(), "输入", "请输入字段名称：", "", new IInputValidator() {
					public String isValid(String str) {
						str = str.trim();
						if (str.length() == 0) {
							return "名称不能为空。";
						}
						if (findField(str) != null) {
							return "字段名称不能重复。";
						}
						return null;
					}
				});
				if (dlg.open() != InputDialog.OK) {
					return;
				}
				String name = dlg.getValue().trim();
				
				// 输入字段前缀
				dlg = new InputDialog(getSite().getShell(), "输入", "请输入字段提取前缀（例如]ID[）：", "", new IInputValidator() {
					public String isValid(String str) {
						if (str.length() == 0) {
							return "不能为空。";
						}
						return null;
					}
				});
				if (dlg.open() != InputDialog.OK) {
					return;
				}
				String prefix = dlg.getValue();
				
				// 输入字段后缀
				dlg = new InputDialog(getSite().getShell(), "输入", "请输入字段提取后缀（例如]）：", "", new IInputValidator() {
					public String isValid(String str) {
						if (str.length() == 0) {
							return "不能为空。";
						}
						return null;
					}
				});
				if (dlg.open() != InputDialog.OK) {
					return;
				}
				String suffix = dlg.getValue();
				
				FieldDef newField = new FieldDef();
				newField.name = name;
				newField.prefix = prefix;
				newField.suffix = suffix;
				newField.isSumField = false;
				dataFields.add(newField);
				
				fieldListViewer.refresh();
			}
		});
		buttonAddField.setText("添加字段");

		final Button buttonAddSumField = new Button(composite_5, SWT.NONE);
		buttonAddSumField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				List<FieldDef> rawFields = new ArrayList<FieldDef>();
				for (FieldDef fd : dataFields) {
					if (!fd.isSumField) {
						rawFields.add(fd);
					}
				}
				if (rawFields.size() == 0) {
					MessageDialog.openError(getSite().getShell(), "错误", "请先添加数据字段。");
					return;
				}
				
				// 先检查是否有关键字字段
				GenericChooseDialog dlg = new GenericChooseDialog(getSite().getShell(), "请选择关键字字段", rawFields);
				if (dlg.open() != Dialog.OK) {
					return;
				}
				keyField = (FieldDef)dlg.getSelection();
				fieldListViewer.refresh();
				
				// 选择汇总字段名称
				dlg = new GenericChooseDialog(getSite().getShell(), "请选择要汇总的字段", rawFields);
				if (dlg.open() != Dialog.OK) {
					return;
				}
				FieldDef rawF = (FieldDef)dlg.getSelection();
				
				// 选择汇总方式 0 - 计数，1 - 求和，2 - 平均，3 - 最大，4 - 最小
				List<String> types = new ArrayList<String>();
				types.add("计数");
				types.add("求和");
				types.add("平均");
				types.add("最大");
				types.add("最小");
				dlg = new GenericChooseDialog(getSite().getShell(), "请选择汇总方式", types);
				if (dlg.open() != Dialog.OK) {
					return;
				}
				int sumType = types.indexOf(dlg.getSelection());
				
				FieldDef newField = new FieldDef();
				newField.name = rawF.name;
				newField.isSumField = true;
				newField.sumType = sumType;
				dataFields.add(newField);
				
				fieldListViewer.refresh();
			}
		});
		final GridData gd_buttonAddSumField = new GridData();
		buttonAddSumField.setLayoutData(gd_buttonAddSumField);
		buttonAddSumField.setText("添加汇总字段");

		final Button buttonDeleteField = new Button(composite_5, SWT.NONE);
		buttonDeleteField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				int index = fieldList.getSelectionIndex();
				if (index == -1) {
					return;
				}
				FieldDef delField = dataFields.remove(index);
				if (!delField.isSumField) {
					// 如果一个数据字段被删除，所有依赖于这个字段的汇总字段也要删除
					for (int i = 0; i < dataFields.size(); i++) {
						if (dataFields.get(i).name.equals(delField.name)) {
							dataFields.remove(i);
							i--;
						}
					}
				}
				if (delField == keyField) {
					// 如果关键字被删除，所有汇总字段都要删除
					keyField = null;
					for (int i = 0; i < dataFields.size(); i++) {
						if (dataFields.get(i).isSumField) {
							dataFields.remove(i);
							i--;
						}
					}
				}
				fieldListViewer.refresh();
				if (index < dataFields.size()) {
					fieldList.select(index);
				} else if (dataFields.size() > 0) {
					fieldList.select(dataFields.size() - 1);
				}
			}
		});
		buttonDeleteField.setText("删除字段");

		final Button buttonRefresh = new Button(composite_5, SWT.NONE);
		buttonRefresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					makeReportData();
					updateReportTable();
				} catch (Exception e1) {
					MessageDialog.openError(getSite().getShell(), "错误", e1.toString());
				}
			}
		});
		buttonRefresh.setText("执行统计");

		final Button buttonExport = new Button(composite_5, SWT.NONE);
		buttonExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (reportFields.size() == 0 || reportData.size() == 0) {
					return;
				}
				FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
				dlg.setFilterNames(new String[] { "日志文件(*.log)", "所有文件(*.*)" });
				dlg.setFilterExtensions(new String[] { "*.log", "*.*" });
				dlg.setFilterPath(initLogFile.getParent());
				String path = dlg.open();
				if (path == null) {
					return;
				}
				if (new File(path).exists()) {
					String msg = "目标文件已存在，是否覆盖？";
					if (!MessageDialog.openQuestion(getSite().getShell(), "覆盖", msg)) {
						return;
					}
				}
				try {
					FileWriter fw = new FileWriter(path);
					PrintWriter pw = new PrintWriter(fw);
					for (int i = 0; i < reportFields.size(); i++) {
						if (i > 0) {
							pw.print('\t');
						}
						pw.print(reportFields.get(i).toString());
					}
					pw.println();
					for (int i = 0; i < reportData.size(); i++) {
						String[] data = reportData.get(i);
						for (int j = 0; j < data.length; j++) {
							if (j > 0) {
								pw.print('\t');
							}
							pw.print(data[j]);
						}
						pw.println();
					}
					pw.close();
					
					MessageDialog.openInformation(getSite().getShell(), "成功", "导出成功。");
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e1.toString());
				}
			}
		});
		buttonExport.setText("导出文件");

		final CTabItem analyseTabItem = new CTabItem(tabFolder, SWT.NONE);
		analyseTabItem.setText("分析");

		final Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		composite_3.setLayout(new GridLayout());
		analyseTabItem.setControl(composite_3);

		tabFolder.setSelection(0);
		
		//
		textSourceFile.setText(initLogFile.getAbsolutePath());
		loadPage(1);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		for (int i = 0; i < tempFiles.size(); i++) {
			tempFiles.get(i).delete();
		}
	}
	
	protected File getCurrentFile() {
		File curFile;
		if (tempFiles.size() > 0) {
			curFile = tempFiles.get(tempFiles.size() - 1);
		} else {
			curFile = initLogFile;
		}
		return curFile;
	}

	protected void loadPage(int pageNo) {
		File curFile = getCurrentFile();
		try {
			if (currentTextFile == null || !(currentTextFile.getSource().equals(curFile))) {
				currentTextFile = new TextFile(curFile);
			}
			String text = currentTextFile.getPage(pageNo);
			currentPage = pageNo;
			logEditText.setText(text);
			buttonPrevPage.setEnabled(pageNo > 1);
			labelPage.setText(pageNo + "/" + currentTextFile.getTotalPages());
			buttonNextPage.setEnabled(pageNo < currentTextFile.getTotalPages());
			buttonPrevStep.setEnabled(tempFiles.size() > 0);
			
			// 拼filter字符串
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < filters.size(); i++) {
				if (i > 0) {
					sb.append(" -> ");
				}
				sb.append(filters.get(i));
			}
			textFilter.setText(sb.toString());
			textCurFile.setText(curFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Do the Save operation
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
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		String filePath = url.getPath();
		if (filePath.indexOf(':') >= 0) {
			filePath = filePath.substring(1);
		}
		initLogFile = new File(filePath);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	protected void addFilter(String filter) {
		try {
			File curFile = getCurrentFile();
			File tempFile = File.createTempFile("_svc", ".log");
			if (filter.startsWith("reg:")) {
				String patStr = filter.substring(4);
				Pattern pat = Pattern.compile(patStr);
				filter(curFile, tempFile, pat);
			} else {
				filter(curFile, tempFile, filter);
			}
			tempFiles.add(tempFile);
			filters.add(filter);
			loadPage(1);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
	}

	/*
	 * 过滤一个文件，输出到另外一个文件。cong可以是一个Pattern，也可以是一个关键字字符串。
	 */
	protected void filter(File input, File output, Object cond) throws IOException {
		FileReader fr = null;
		FileWriter fw = null;
		try {
			fr = new FileReader(input);
			fw = new FileWriter(output);
			BufferedReader br = new BufferedReader(fr);
			PrintWriter pw = new PrintWriter(fw);
			String line;
			String condStr = null;
			Pattern condPat = null;
			if (cond instanceof String) {
				condStr = (String)cond;
			} else {
				condPat = (Pattern)cond;
			}
			while ((line = br.readLine()) != null) {
				if (condStr != null) {
					if (line.contains(condStr)) {
						pw.println(line);
					}
				} else {
					if (condPat.matcher(line).matches()) {
						pw.println(line);
					}
				}
			}
			pw.flush();
		} finally {
			if (fr != null) {
				fr.close();
			}
			if (fw != null) {
				fw.close();
			}
		}
	}
	
	protected FieldDef findField(String name) {
		for (FieldDef fd : dataFields) {
			if (fd.name.equals(name)) {
				return fd;
			}
		}
		return null;
	}
	
	// 对过滤结果进行统计分析，生成统计数据
	protected void makeReportData() throws IOException {
		List<FieldDef> reportFlds = new ArrayList<FieldDef>();
		List<FieldDef> basicFlds = new ArrayList<FieldDef>();
		Map<String, Integer> basicFldInds = new HashMap<String, Integer>();
		boolean sumMode = false;
		
		// 整理最终显示在结果中的字段列表（如果一个非关键字字段有汇总字段，需要删除）
		for (FieldDef fd : dataFields) {
			reportFlds.add(fd.dup());
			if (fd.isSumField) {
				sumMode = true;
			} else {
				basicFlds.add(fd);
				basicFldInds.put(fd.name, basicFlds.size() - 1);
			}
		}
		if (sumMode) {
			for (int i = 0; i < reportFlds.size(); i++) {
				FieldDef fd = reportFlds.get(i);
				if (!fd.isSumField && !fd.name.equals(keyField.name)) {
					reportFlds.remove(i);
					i--;
				}
			}
		}
		
		// 逐行分析
		List<String[]> basicDatas = new ArrayList<String[]>();		   // 每个字段的原始数据
		Map<String, int[]> sumDatas = new HashMap<String, int[]>();    // 汇总数据，key是关键字取值，value是每个字段的所有汇总值（每个字段4个int：计数、求和、最大、最小）
		FileReader fr = new FileReader(getCurrentFile());
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ((line = br.readLine()) != null) {
			// 在这一行中找出所有的字段（只要有一个没有找到，这一行就算是无效数据）
			String[] basicData = new String[basicFlds.size()];
			boolean ok = true;
			for (int i = 0; i < basicFlds.size(); i++) {
				basicData[i] = getField(line, basicFlds.get(i));
				if (basicData[i] == null) {
					ok = false;
					break;
				}
			}
			if (!ok) {
				continue;
			}
			
			// 计算汇总数据
			if (sumMode) {
				// 取得关键字取值
				String keyValue = basicData[basicFldInds.get(keyField.name)];
				if (sumDatas.get(keyValue) == null) {
					int[] tmpRow = new int[basicFldInds.size() * 4];
					for (int i = 0; i < basicFldInds.size(); i++) {
						tmpRow[i * 4 + 2] = Integer.MIN_VALUE;
						tmpRow[i * 4 + 3] = Integer.MAX_VALUE;
					}
					sumDatas.put(keyValue, tmpRow);
				}
				
				// 对所有字段进行4项汇总
				int[] sumData = sumDatas.get(keyValue);
				for (int i = 0; i < basicFlds.size(); i++) {
					int value = 0;
					try {
						value = Integer.parseInt(basicData[i]);
					} catch (Exception e) {
					}
					sumData[i * 4]++;
					sumData[i * 4 + 1] += value;
					if (value > sumData[i * 4 + 2]) {
						sumData[i * 4 + 2] = value;
					}
					if (value < sumData[i * 4 + 3]) {
						sumData[i * 4 + 3] = value;
					}
				}
			} else {
				basicDatas.add(basicData);
			}
		}
		
		// 生成报表数据
		reportFields = reportFlds;
		reportData.clear();
		if (sumMode) {
			Object[] arr = sumDatas.keySet().toArray();
			for (int i = 0; i < arr.length; i++) {
				String key = (String)arr[i];
				String[] row = new String[reportFields.size()];
				int[] sumValues = sumDatas.get(key);
				for (int j = 0; j < row.length; j++) {
					FieldDef fld = reportFields.get(j);
					if (!fld.isSumField) {
						row[j] = key;
					} else {
						int idx = basicFldInds.get(fld.name);
						int value = 0;
						switch (fld.sumType) {
						case 0:
							value = sumValues[idx * 4];
							break;
						case 1:
							value = sumValues[idx * 4 + 1];
							break;
						case 2:
							value = sumValues[idx * 4] == 0 ? 0 : sumValues[idx * 4 + 1] / sumValues[idx * 4];
							break;
						case 3:
							value = sumValues[idx * 4 + 2];
							break;
						case 4:
							value = sumValues[idx * 4 + 3];
							break;
						}
						row[j] = String.valueOf(value);
					}
				}
				reportData.add(row);
			}
		} else {
			for (int i = 0; i < basicDatas.size(); i++) {
				String[] row = new String[reportFields.size()];
				String[] basicData = basicDatas.get(i);
				for (int j = 0; j < row.length; j++) {
					row[j] = basicData[basicFldInds.get(reportFields.get(j).name)];
				}
				reportData.add(row);
			}
		}
	}
	
	protected String getField(String str, FieldDef fld) {
		int pos1 = str.indexOf(fld.prefix);
		if (pos1 == -1) {
			return null;
		}
		pos1 += fld.prefix.length();
		int pos2 = str.indexOf(fld.suffix, pos1);
		if (pos2 == -1) {
			return null;
		} else {
			return str.substring(pos1, pos2);
		}
	}
	
	protected void updateReportTable() {
		dataTableViewer.setInput(null);
		
		// 删除旧的所有column
		TableColumn[] oldCols = dataTable.getColumns();
		for (int i = 0; i < oldCols.length; i++) {
			oldCols[i].dispose();
		}
		
		// 创建新的column
		for (FieldDef fd : reportFields) {
			final TableColumn newColumnTableColumn = new TableColumn(dataTable, SWT.NONE);
			newColumnTableColumn.setWidth(100);
			newColumnTableColumn.setText(fd.toString());
		}
		
		dataTableViewer.setInput(this);
	}
}
