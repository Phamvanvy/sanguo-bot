package com.pip.servermgr.report;

import java.io.DataInputStream;
import java.util.List;

import com.pip.servermgr.report.itimes.ITimes_ReportEngine;
import com.pip.servermgr.report.mzc.MZC_ReportEngine;
import com.pip.servermgr.report.mzx.MZX_ReportEngine;
import com.pip.servermgr.report.sanguo.Sanguo_ReportEngine;
import com.pip.servermgr.report.wulin.Wulin_ReportEngine;
import com.pip.servermgr.report.xiyou.Xiyou_ReportEngine;
import com.pip.servermgr.report.xuanyuan.Xuanyuan_ReportEngine;

/**
 * 某个产品的报表支撑引擎，包括数据类型定义，以及其他相关类的入口。
 * @author lighthu
 */
public abstract class AbstractReportEngine {
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public abstract int getMaxType();
	/**
	 * 取得某个统计项数据类型。
	 * @param type 参见DataFilter里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public abstract Class getDataType(int type);
	
	/**
	 * 取得某个统计项名称。
	 * @param type 参见DataFilter里的常量
	 * @return 
	 */
	public abstract String getTypeName(int type);
	
	/**
	 * 取得某个统计项的描述。
	 * @param type 参见DataFilter里的常量
	 * @return 
	 */
	public abstract String getTypeComments(int type);
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public abstract String getDataFetcherClass();
	
	/**
	 * 准备数据。
	 * @return 是否准备成功
	 */
	public abstract boolean init();
	
	/**
	 * 从文件中解析出玩家数据。
	 */
	public abstract List<IPlayer> parseFile(DataInputStream dis) throws Exception;
	
	/**
	 * 取得玩家表的名称。
	 */
	public abstract String getPlayerTable();
	
	/**
	 * 根据产品名称创建报表引擎。
	 * @param productName
	 * @return
	 */
	public static AbstractReportEngine create(String productName) {
		if (productName.contains("三国")) {
			return new Sanguo_ReportEngine();
		} else if (productName.contains("武林")) {
			return new Wulin_ReportEngine();
		} else if (productName.contains("幻想")) {
			return new ITimes_ReportEngine();
		} else if (productName.contains("西游")) {
			return new Xiyou_ReportEngine();
		} else if (productName.contains("轩辕")) {
			return new Xuanyuan_ReportEngine();
		} else if (productName.contains("明珠侠")) {
			return new MZX_ReportEngine();
		} else if (productName.contains("明珠城") || productName.contains("圣域龙斗士")) {
			return new MZC_ReportEngine();
		} else {
			throw new IllegalArgumentException();
		}
	}
}
