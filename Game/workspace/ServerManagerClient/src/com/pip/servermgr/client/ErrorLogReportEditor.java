package com.pip.servermgr.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.experimental.chart.swt.ChartComposite;

import com.pip.servermgr.report.IPlayer;
import com.pip.servermgr.report.ReportData;

public class ErrorLogReportEditor extends EditorPart {
	public static final String ID = "com.pip.servermgr.client.ErrorLogReportEditor"; //$NON-NLS-1$

	class ExceptionTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return einput.exceptionRecords.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class LongTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			Object[] ret = new Object[einput.longReport.opcodeTime.size() + einput.longReport.callTime.size()];
			int index = 0;
			for (int opcode : einput.longReport.opcodeTime.keySet()) {
				ret[index] = opcode;
				index++;
			}
			for (String call : einput.longReport.callTime.keySet()) {
				ret[index] = call;
				index++;
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class LongTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Integer) {
				if (columnIndex == 0) {
					return String.valueOf(einput.longReport.opcodeTime.get(element).size());
				} else {
					return "OPCODE " + element;
				}
			} else if (element instanceof String) {
				if (columnIndex == 0) {
					return String.valueOf(einput.longReport.callTime.get(element).size());
				} else {
					return "CALL " + element;
				}
			}
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ExceptionTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			ExceptionRecord r = (ExceptionRecord)element;
			if (columnIndex == 0) {
				return String.valueOf(r.repeatCount);
			} else if (columnIndex == 1) {
				return r.source;
			} else {
				return r.title;
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	private Table longTable;
	private Text exceptionStackViewer;
	private Table exceptionTable;
	protected ErrorLogReportInput einput;
	private TableViewer exceptionTableViewer;
	private TableViewer longTableViewer;
	private ChartComposite reportViewer;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout_c = new GridLayout();
		gridLayout_c.numColumns = 2;
		container.setLayout(gridLayout_c);

		final Composite composite = new Composite(container, SWT.NONE);
		final GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_composite.widthHint = 200;
		composite.setLayoutData(gd_composite);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);

		exceptionTableViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);
		exceptionTableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent arg0) {
				StructuredSelection sel = (StructuredSelection)exceptionTableViewer.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				ExceptionRecord r = (ExceptionRecord)sel.getFirstElement();
				exceptionStackViewer.setText(r.fullStackTrace);
			}
		});
		exceptionTableViewer.setContentProvider(new ExceptionTableContentProvider());
		exceptionTableViewer.setLabelProvider(new ExceptionTableLabelProvider());
		exceptionTable = exceptionTableViewer.getTable();
		exceptionTable.setLinesVisible(true);
		exceptionTable.setHeaderVisible(true);
		final GridData gd_exceptionTable = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd_exceptionTable.heightHint = 400;
		exceptionTable.setLayoutData(gd_exceptionTable);

		final TableColumn exceptionCountColumn = new TableColumn(exceptionTable, SWT.NONE);
		exceptionCountColumn.setWidth(100);
		exceptionCountColumn.setText("出现次数");

		final TableColumn exceptionSourceColumn = new TableColumn(exceptionTable, SWT.NONE);
		exceptionSourceColumn.setWidth(100);
		exceptionSourceColumn.setText("来源");

		final TableColumn exceptionTitleColumn = new TableColumn(exceptionTable, SWT.NONE);
		exceptionTitleColumn.setWidth(376);
		exceptionTitleColumn.setText("异常");

		exceptionStackViewer = new Text(composite, SWT.V_SCROLL | SWT.MULTI | SWT.H_SCROLL | SWT.BORDER);
		exceptionStackViewer.setEditable(false);
		final GridData gd_exceptionStackViewer = new GridData(SWT.FILL, SWT.FILL, true, true);
		exceptionStackViewer.setLayoutData(gd_exceptionStackViewer);

		final Composite composite_1 = new Composite(container, SWT.NONE);
		final GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_composite_1.widthHint = 200;
		composite_1.setLayoutData(gd_composite_1);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 1;
		composite_1.setLayout(gridLayout_1);

		longTableViewer = new TableViewer(composite_1, SWT.FULL_SELECTION | SWT.BORDER);
		longTableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				StructuredSelection sel = (StructuredSelection)longTableViewer.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Object selObj = sel.getFirstElement();
				List<Integer> data;
				if (selObj instanceof Integer) {
					data = einput.longReport.opcodeTime.get(selObj);
				} else {
					data = einput.longReport.callTime.get(selObj);
				}
				
				// 绘制报表
				makeTimeReport(data);
			}
		});
		longTableViewer.setContentProvider(new LongTableContentProvider());
		longTableViewer.setLabelProvider(new LongTableLabelProvider());
		longTable = longTableViewer.getTable();
		longTable.setLinesVisible(true);
		longTable.setHeaderVisible(true);
		final GridData gd_longTable = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd_longTable.heightHint = 400;
		longTable.setLayoutData(gd_longTable);

		final TableColumn longCountColumn = new TableColumn(longTable, SWT.NONE);
		longCountColumn.setWidth(100);
		longCountColumn.setText("出现次数");

		final TableColumn longNameColumn = new TableColumn(longTable, SWT.NONE);
		longNameColumn.setWidth(182);
		longNameColumn.setText("OPCODE/CALL");

		final Composite barComposite = new Composite(composite_1, SWT.NONE);
		barComposite.setLayout(new FillLayout());
		final GridData gd_barComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
		barComposite.setLayoutData(gd_barComposite);
		
		reportViewer = new ChartComposite(barComposite, SWT.NONE, null, true);
		
		// init
		exceptionTableViewer.setInput(einput);
		longTableViewer.setInput(einput);
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
		einput = (ErrorLogReportInput)input;
		
		for (int i = 0; i < einput.exceptionRecords.size() - 1; i++) {
			for (int j = i + 1; j < einput.exceptionRecords.size(); j++) {
				ExceptionRecord r1 = einput.exceptionRecords.get(i);
				ExceptionRecord r2 = einput.exceptionRecords.get(j);
				if (r1.repeatCount < r2.repeatCount) {
					einput.exceptionRecords.set(i, r2);
					einput.exceptionRecords.set(j, r1);
				}
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
	
	protected void makeTimeReport(List<Integer> sourceData) {
		ReportData report = new ReportData();
		int segCount = 10;
		
		// 首先取出所有玩家的数据，计算最大最小值，并确定分段规则
		int minValue = Integer.MAX_VALUE;
		int maxValue = Integer.MIN_VALUE;
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>(); // 每个取值的个数
		for (int i = 0; i < sourceData.size(); i++) {
			if (sourceData.get(i) < minValue) {
				minValue = sourceData.get(i);
			}
			if (sourceData.get(i) > maxValue) {
				maxValue = sourceData.get(i);
			}
			if (counts.containsKey(sourceData.get(i))) {
				counts.put(sourceData.get(i), counts.get(sourceData.get(i)) + 1);
			} else {
				counts.put(sourceData.get(i), 1);
			}
		}
		int[][] segs;
		if (counts.size() <= segCount) {
			segCount = counts.size();
			Object[] arro = counts.keySet().toArray();
			int[] arr = new int[counts.size()];
			for (int i = 0; i < arro.length; i++) {
				arr[i] = ((Integer)arro[i]).intValue();
			}
			Arrays.sort(arr);
			segs = new int[segCount][2];
			for (int i = 0; i < arr.length; i++) {
				segs[i][0] = arr[i];
				segs[i][1] = arr[i];
			}
		} else if (maxValue - minValue + 1 <= segCount) {
			segCount = maxValue - minValue + 1;
			segs = new int[segCount][2];
			for (int i = 0; i < segCount; i++) {
				segs[i][0] = minValue + i;
				segs[i][1] = minValue + i;
			}
		} else {
			segs = new int[segCount][2];
			double gap = (double) (maxValue - minValue) / segCount;
			double curValue = minValue;
			for (int i = 0; i < segCount; i++) {
				double nextValue = curValue + gap;
				segs[i][0] = (int) curValue;
				segs[i][1] = (int) nextValue - 1;
				curValue = nextValue;
			}
			segs[segCount - 1][1] = maxValue;
		}

		// 生成分类名称
		report.category = new String[segs.length];
		for (int i = 0; i < segs.length; i++) {
			if (segs[i][0] == segs[i][1]) {
				report.category[i] = String.valueOf(segs[i][0]);
			} else {
				report.category[i] = String.valueOf(segs[i][0]) + " - "
						+ String.valueOf(segs[i][1]);
			}
		}

		// 生成分类数据
		report.data = new int[segs.length];
		for (int i = 0; i < sourceData.size(); i++) {
			for (int j = 0; j < segs.length; j++) {
				if (sourceData.get(i) >= segs[j][0] && sourceData.get(i) <= segs[j][1]) {
					report.data[j]++;
					break;
				}
			}
		}
		
		// 生成jfreechart的数据
		reportViewer.setChart(report.createPieChart());
		reportViewer.forceRedraw();
	}
}
