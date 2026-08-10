package com.pip.servermgr.report;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 * 用户分类计数统计图表数据。
 * 
 * @author lighthu
 */
public class ReportData {
	protected AbstractReportEngine engine;
	// 统计项类型
	public int type;
	// 统计分类
	public String[] category;
	// 分类计数
	public int[] data;

	/**
	 * 生成统计报表数据。
	 * @param players 玩家数据
	 * @param type 统计项类型
	 * @param segCount 最大分类数量
	 * @return
	 */
	public static ReportData makeReport(AbstractReportEngine engine, List<IPlayer> players, int type, int segCount) {
		ReportData report = new ReportData();
		report.engine = engine;
		report.type = type;
		Class cls = engine.getDataType(type);
		if (cls == Boolean.class) {
			makeBooleanReport(report, players);
		} else if (cls == Integer.class) {
			makeIntReport(report, players, segCount);
		} else if (cls == Float.class) {
			makeFloatReport(report, players, segCount);
		}
		return report;
	}

	protected static void makeFloatReport(ReportData report, List<IPlayer> players, int segCount) {
		// 首先取出所有玩家的数据，计算最大最小值，并确定分段规则
		float[] allData = new float[players.size()];
		float minValue = Integer.MAX_VALUE;
		float maxValue = Integer.MIN_VALUE;
		for (int i = 0; i < players.size(); i++) {
			allData[i] = ((Float) players.get(i).getValue(report.type))
					.floatValue();
			if (allData[i] < minValue) {
				minValue = allData[i];
			}
			if (allData[i] > maxValue) {
				maxValue = allData[i];
			}
		}
		float[][] segs = new float[segCount][2];
		float gap = (float) (maxValue - minValue) / segCount;
		for (int i = 0; i < segCount; i++) {
			segs[i][0] = minValue + gap * i;
			segs[i][1] = minValue + gap * (i + 1) - 0.00000001f;
		}
		segs[segCount - 1][1] = maxValue;

		// 生成分类名称
		report.category = new String[segs.length];
		for (int i = 0; i < segs.length; i++) {
			report.category[i] = DataFilter.formatFloat(segs[i][0]) + " - "
					+ DataFilter.formatFloat(segs[i][1]);
		}

		// 生成分类数据
		report.data = new int[segs.length];
		for (int i = 0; i < allData.length; i++) {
			for (int j = 0; j < segs.length; j++) {
				if (allData[i] >= segs[j][0] && allData[i] <= segs[j][1]) {
					report.data[j]++;
					break;
				}
			}
		}
	}

	protected static void makeIntReport(ReportData report, List<IPlayer> players, int segCount) {
		// 首先取出所有玩家的数据，计算最大最小值，并确定分段规则
		int[] allData = new int[players.size()];
		int minValue = Integer.MAX_VALUE;
		int maxValue = Integer.MIN_VALUE;
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>(); // 每个取值的个数
		for (int i = 0; i < players.size(); i++) {
			allData[i] = ((Integer) players.get(i).getValue(report.type))
					.intValue();
			if (allData[i] < minValue) {
				minValue = allData[i];
			}
			if (allData[i] > maxValue) {
				maxValue = allData[i];
			}
			if (counts.containsKey(allData[i])) {
				counts.put(allData[i], counts.get(allData[i]) + 1);
			} else {
				counts.put(allData[i], 1);
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
		for (int i = 0; i < allData.length; i++) {
			for (int j = 0; j < segs.length; j++) {
				if (allData[i] >= segs[j][0] && allData[i] <= segs[j][1]) {
					report.data[j]++;
					break;
				}
			}
		}
	}

	protected static void makeBooleanReport(ReportData report, List<IPlayer> players) {
		report.category = new String[] { "是", "否" };
		report.data = new int[2];
		for (IPlayer p : players) {
			boolean value = ((Boolean) p.getValue(report.type)).booleanValue();
			if (value) {
				report.data[0]++;
			} else {
				report.data[1]++;
			}
		}
	}

	/**
	 * 根据生成的数据创建一个柱形图。
	 * @return
	 */
	public JFreeChart createBarChart() {
		JFreeChart chart = ChartFactory.createBarChart("", // chart title
				engine.getTypeName(type), // domain axis label
				"取值", // range axis label
				createBarDataset(), // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// get a reference to the plot for further customisation...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// the SWTGraphics2D class doesn't handle GradientPaint well, so
		// replace the gradient painter from the default theme with a
		// standard painter...
		renderer.setBarPainter(new StandardBarPainter());

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;
	}

	private CategoryDataset createBarDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String name = engine.getTypeName(type);
		for (int i = 0; i < category.length; i++) {
			dataset.addValue(data[i], name, category[i]);
		}
		return dataset;
	}
	
	/**
	 * 根据生成的数据创建一个饼图。
	 * @return
	 */
	public JFreeChart createPieChart() {
		JFreeChart chart = ChartFactory.createPieChart("", // chart title
				createPieDataset(), // data
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionOutlinesVisible(false);
        // plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("没有数据");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        return chart;
	}

	private PieDataset createPieDataset() {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (int i = 0; i < category.length; i++) {
			dataset.setValue(category[i], data[i]);
		}
		return dataset;
	}
}
