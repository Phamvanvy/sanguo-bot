package com.pip.servermgr.report;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import com.pip.servermgr.client.Settings;
import com.pip.util.ResultRow;

public class UserReportEditor extends EditorPart {
	private Text textComments;
	class FilterListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return filters.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private Combo comboGraphType;
	private Text textSegCount;
	private Combo comboStatType;
	private Combo comboBooleanValue;
	private Text textMaxValue;
	private Text textMinValue;
	private List filterList;
	private Combo comboFilterType;
	private ChartComposite reportViewer;
	
	public static final String ID = "com.pip.servermgr.report.UserReportEditor"; //$NON-NLS-1$
	
	protected File dataFile;
	protected String productName;
	protected String serverName;
	protected Date startDate;
	protected Date endDate;
	protected AbstractReportEngine engine;
	
	protected java.util.List<IPlayer> players;
	protected java.util.List<IPlayer> filteredPlayers = new ArrayList<IPlayer>();
	private Label labelRecordCount;
	private Label labelMinValue;
	private Label labelMaxValue;
	private Label labelBooleanValue;
	
	protected java.util.List<DataFilter> filters = new ArrayList<DataFilter>();
	private ListViewer filterListViewer;
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		composite.setLayout(gridLayout_1);

		final Composite composite_1 = new Composite(composite, SWT.NONE);
		final GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd_composite_1.widthHint = 200;
		composite_1.setLayoutData(gd_composite_1);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite_1.setLayout(gridLayout);

		final Label label = new Label(composite_1, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label.setText("数据过滤：");

		filterListViewer = new ListViewer(composite_1, SWT.BORDER);
		filterListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent arg0) {
				int index = filterList.getSelectionIndex();
				if (index >= 0 && index < filters.size()) {
					filters.remove(index);
					filterListViewer.refresh();
					doFilter();
				}
			}
		});
		filterListViewer.setContentProvider(new FilterListContentProvider());
		filterList = filterListViewer.getList();
		filterList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		filterListViewer.setInput(this);

		final Label label_1 = new Label(composite_1, SWT.NONE);
		label_1.setText("类型：");

		comboFilterType = new Combo(composite_1, SWT.READ_ONLY);
		comboFilterType.setVisibleItemCount(50);
		comboFilterType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				int index = comboFilterType.getSelectionIndex();
				if (index < 0) {
					return;
				}
				Class cls = engine.getDataType(index + 1);
				if (cls == Boolean.class) {
					showControl(labelBooleanValue);
					showControl(comboBooleanValue);
					hideControl(labelMinValue);
					hideControl(textMinValue);
					hideControl(labelMaxValue);
					hideControl(textMaxValue);
				} else {
					hideControl(labelBooleanValue);
					hideControl(comboBooleanValue);
					showControl(labelMinValue);
					showControl(textMinValue);
					showControl(labelMaxValue);
					showControl(textMaxValue);
				}
			}
		});
		final GridData gd_comboFilterType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboFilterType.setLayoutData(gd_comboFilterType);

		labelBooleanValue = new Label(composite_1, SWT.NONE);
		labelBooleanValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		labelBooleanValue.setText("取值：");

		comboBooleanValue = new Combo(composite_1, SWT.READ_ONLY);
		comboBooleanValue.setItems(new String[] {"是", "否"});
		comboBooleanValue.select(0);
		final GridData gd_comboBooleanValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboBooleanValue.setLayoutData(gd_comboBooleanValue);

		labelMinValue = new Label(composite_1, SWT.NONE);
		labelMinValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		labelMinValue.setText("最小：");

		textMinValue = new Text(composite_1, SWT.BORDER);
		textMinValue.setText("1");
		final GridData gd_textMinValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textMinValue.setLayoutData(gd_textMinValue);

		labelMaxValue = new Label(composite_1, SWT.NONE);
		labelMaxValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		labelMaxValue.setText("最大：");

		textMaxValue = new Text(composite_1, SWT.BORDER);
		textMaxValue.setText("1");
		final GridData gd_textMaxValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textMaxValue.setLayoutData(gd_textMaxValue);

		final Button buttonAddFilter = new Button(composite_1, SWT.NONE);
		buttonAddFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				addFilter();
			}
		});
		final GridData gd_buttonAddFilter = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		buttonAddFilter.setLayoutData(gd_buttonAddFilter);
		buttonAddFilter.setText("添加过滤");

		labelRecordCount = new Label(composite_1, SWT.CENTER);
		final GridData gd_labelRecordCount = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		labelRecordCount.setLayoutData(gd_labelRecordCount);

		final Label label_5 = new Label(composite_1, SWT.NONE);
		label_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label_5.setText("数据汇总：");

		final Label label_7 = new Label(composite_1, SWT.NONE);
		label_7.setText("类型：");

		comboStatType = new Combo(composite_1, SWT.READ_ONLY);
		comboStatType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				updateComments();
			}
		});
		comboStatType.setVisibleItemCount(50);
		final GridData gd_comboStatType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboStatType.setLayoutData(gd_comboStatType);

		final Label label_8 = new Label(composite_1, SWT.NONE);
		label_8.setText("分段数：");

		textSegCount = new Text(composite_1, SWT.BORDER);
		textSegCount.setText("10");
		final GridData gd_textSegCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textSegCount.setLayoutData(gd_textSegCount);

		final Label label_9 = new Label(composite_1, SWT.NONE);
		label_9.setText("图表：");

		comboGraphType = new Combo(composite_1, SWT.READ_ONLY);
		comboGraphType.setItems(new String[] {"饼图", "柱状图"});
		comboGraphType.select(0);
		final GridData gd_comboGraphType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboGraphType.setLayoutData(gd_comboGraphType);

		final Button buttonMakeGraph = new Button(composite_1, SWT.NONE);
		buttonMakeGraph.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					doReport();
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e1.toString());
				}
			}
		});
		final GridData gd_buttonMakeGraph = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		buttonMakeGraph.setLayoutData(gd_buttonMakeGraph);
		buttonMakeGraph.setText("生成图表");

		final Composite graphContainer = new Composite(composite, SWT.NONE);
		graphContainer.setLayout(new FillLayout());
		final GridData gd_graphContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		graphContainer.setLayoutData(gd_graphContainer);
		
		reportViewer = new ChartComposite(graphContainer, SWT.NONE, null, true);
		
		// 动态生成类型选项
		String[] items = new String[engine.getMaxType()];
		for (int i = 1; i <= engine.getMaxType(); i++) {
			items[i - 1] = engine.getTypeName(i);
		}
		comboFilterType.setItems(items);
		comboFilterType.select(0);
		items = new String[engine.getMaxType()];
		for (int i = 1; i <= engine.getMaxType(); i++) {
			items[i - 1] = engine.getTypeName(i);
		}
		comboStatType.setItems(items);
		comboStatType.select(0);
		
		hideControl(labelMinValue);
		hideControl(textMinValue);
		hideControl(labelMaxValue);
		hideControl(textMaxValue);

		textComments = new Text(composite, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI | SWT.BORDER);
		final GridData gd_textComments = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd_textComments.widthHint = 200;
		textComments.setLayoutData(gd_textComments);
			
		doFilter();
		updateComments();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		this.setPartName(productName + " " + serverName + "(" + df.format(startDate) + "-" + df.format(endDate) + ")");
	}
	
	protected void updateComments() {
		int index = comboStatType.getSelectionIndex();
		if (index < 0) {
			return;
		}
		int type = index + 1;
		textComments.setText(engine.getTypeComments(type));
	}
	
	protected void doReport() {
		int index = comboStatType.getSelectionIndex();
		if (index < 0) {
			return;
		}
		int type = index + 1;
		int segCount;
		try {
			segCount = Integer.parseInt(textSegCount.getText());
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", "请输入正确的分段数量。");
			return;
		}
		ReportData report = ReportData.makeReport(engine, filteredPlayers, type, segCount);
		int style = comboGraphType.getSelectionIndex();
		JFreeChart chart;
		if (style == 0) {
			chart = report.createPieChart();
		} else {
			chart = report.createBarChart();
		}
		reportViewer.setChart(chart);
		reportViewer.forceRedraw();
	}
	
	protected void addFilter() {
		int index = comboFilterType.getSelectionIndex();
		if (index < 0) {
			return;
		}
		int type = index + 1;
		Class cls = engine.getDataType(type);
		DataFilter newFilter;
		if (cls == Boolean.class) {
			newFilter = new DataFilter(engine, type, comboBooleanValue.getSelectionIndex() == 0);
		} else if (cls == Integer.class) {
			int minValue;
			int maxValue;
			try {
				minValue = Integer.parseInt(textMinValue.getText());
				maxValue = Integer.parseInt(textMaxValue.getText());
				if (minValue > maxValue) {
					throw new Exception();
				}
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "请输入正确的最小值和最大值。");
				return;
			}
			newFilter = new DataFilter(engine, type, minValue, maxValue);
		} else if (cls == Float.class) {
			float minValue;
			float maxValue;
			try {
				minValue = Float.parseFloat(textMinValue.getText());
				maxValue = Float.parseFloat(textMaxValue.getText());
				if (minValue > maxValue) {
					throw new Exception();
				}
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "请输入正确的最小值和最大值。");
				return;
			}
			newFilter = new DataFilter(engine, type, minValue, maxValue);
		} else {
			return;
		}
		filters.add(newFilter);
		filterListViewer.refresh();
		doFilter();
	}
	
	protected void doFilter() {
		filteredPlayers.clear();
		for (IPlayer p : players) {
			boolean match = true;
			for (DataFilter filter : filters) {
				if (!filter.filter(p)) {
					match = false;
					break;
				}
			}
			if (match) {
				filteredPlayers.add(p);
			}
		}
		labelRecordCount.setText("共" + filteredPlayers.size() + "条记录");
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
		this.dataFile = ((UserReportInput)input).dataFile;
		
		players = new ArrayList<IPlayer>();
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(dataFile);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
			productName = dis.readUTF();
			serverName = dis.readUTF();
			startDate = new Date(dis.readLong());
			endDate = new Date(dis.readLong());
			
			// 创建报表引擎
			engine = AbstractReportEngine.create(productName);
			if (!engine.init()) {
				return;
			}
			
			// 解析文件
			players = engine.parseFile(dis);
		} catch (Throwable e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	private void hideControl(Control obj) {
        obj.setVisible(false);
        ((GridData) obj.getLayoutData()).exclude = true;
        obj.getParent().layout();
    }

    private void showControl(Control obj) {
        obj.setVisible(true);
        ((GridData) obj.getLayoutData()).exclude = false;
        obj.getParent().layout();
    }
}
