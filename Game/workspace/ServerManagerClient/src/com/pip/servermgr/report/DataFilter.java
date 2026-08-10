package com.pip.servermgr.report;

import java.text.DecimalFormat;

/**
 * 玩家数据过滤器。
 * @author lighthu
 */
public class DataFilter {
	protected AbstractReportEngine engine;
	
	public int type;
	public boolean valueB;			// Boolean类型适用
	public int minValueI;			// Integer类型适用 
	public int maxValueI;			// Integer类型适用
	public float minValueF;			// Float类型适用
	public float maxValueF;			// Float类型适用
	
	public DataFilter(AbstractReportEngine engine, int t, boolean v) {
		if (engine.getDataType(t) != Boolean.class) {
			throw new IllegalArgumentException();
		}
		this.engine = engine;
		type = t;
		valueB = v;
	}
	
	public DataFilter(AbstractReportEngine engine, int t, int v1, int v2) {
		if (engine.getDataType(t) != Integer.class) {
			throw new IllegalArgumentException();
		}
		this.engine = engine;
		type = t;
		minValueI = v1;
		maxValueI = v2;
	}
	
	public DataFilter(AbstractReportEngine engine, int t, float v1, float v2) {
		if (engine.getDataType(t) != Float.class) {
			throw new IllegalArgumentException();
		}
		this.engine = engine;
		type = t;
		minValueF = v1;
		maxValueF = v2;
	}
	
	/**
	 * 检查一个玩家是否符合条件。
	 * @param p
	 * @return
	 */
	public boolean filter(IPlayer p) {
		Class cls = engine.getDataType(type);
		if (cls == Boolean.class) {
			return ((Boolean)p.getValue(type)).booleanValue() == valueB;
		} else if (cls == Integer.class) {
			int v = ((Integer)p.getValue(type)).intValue();
			return v >= minValueI && v <= maxValueI;
		} else if (cls == Float.class) {
			float v = ((Float)p.getValue(type)).floatValue();
			return v >= minValueF && v <= maxValueF;
		} else {
			return false;
		}
	}
	
	public String toString() {
		String name = engine.getTypeName(type);
		Class cls = engine.getDataType(type);
		if (cls == Boolean.class) {
			return name + ": " + (valueB ? "是" : "否");
		} else if (cls == Integer.class) {
			return name + "：" + minValueI + " - " + maxValueI;
		} else if (cls == Float.class) {
			return name + ": " + formatFloat(minValueF) + " - " + formatFloat(maxValueF);
		} else {
			return "无效过滤器";
		}
	}
	
	private static final DecimalFormat percentFormat = new DecimalFormat("####.####"); 
	 
    public static String formatFloat(double p) {
        return percentFormat.format(p);
    }
}
