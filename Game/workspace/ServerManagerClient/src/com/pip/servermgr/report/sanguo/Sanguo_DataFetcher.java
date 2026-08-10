package com.pip.servermgr.report.sanguo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.pip.util.DBConfig;
import com.pip.util.DBUtils;
import com.pip.util.DataFetcher;
import com.pip.util.ResultRow;

public class Sanguo_DataFetcher implements DataFetcher {
	protected String dbURL;
	protected String dbUser;
	protected String dbPass;
	protected String poolID;
	protected String productName;
	protected String serverName;
	protected int dateType;
	protected Date startDate;
	protected Date endDate;
	protected int minLevel;
	protected int maxCount;
	protected int batchCount;
	protected boolean canceled;
	protected boolean over;
	protected String errorMessage;
	protected String progressText;
	protected File resultFile;

	/**
	 * 设置数据库参数。
	 * @param url 数据库URL
	 * @param name 数据库登录名
	 * @param pass 数据库密码
	 */
	public void setDBInfo(String url, String name, String pass) {
		dbURL = url;
		dbUser = name;
		dbPass = pass;
	}
	
	/**
	 * 设置参数。
	 * @param params
	 */
	public void setParams(Map<String, String> params) {
		productName = params.get("productName");
		serverName = params.get("serverName");
		dateType = Integer.parseInt(params.get("dateType"));
		startDate = new Date(Long.parseLong(params.get("startDate")));
		endDate = new Date(Long.parseLong(params.get("endDate")));
		minLevel = Integer.parseInt(params.get("minLevel"));
		maxCount = Integer.parseInt(params.get("maxCount"));
		batchCount = Integer.parseInt(params.get("batchCount"));
	}
	
	/**
	 * 判断是否获取完成。
	 * @return
	 */
	public boolean isOver() {
		return over;
	}
	
	/**
	 * 判断是否发生错误。
	 * @return 如果发生错误，返回错误内容，否则返回null。
	 */
	public String getError() {
		return errorMessage;
	}
	
	/**
	 * 获得当前进度字符串。
	 * @return
	 */
	public String getProgress() {
		return progressText;
	}
	
	/**
	 * 取消操作，并删除所有临时文件。
	 */
	public void cancel() {
		canceled = true;
		over = true;
		errorMessage = "已取消";
	}
	
	/**
	 * 获取完成后，取得保存数据的文件。
	 * @return
	 */
	public File getFile() {
		return resultFile;
	}
	
	/**
	 * 执行清理工作，删除临时文件。
	 */
	public void clean() {
		resultFile.delete();
	}
	
	public void run() {
		try {
			// 查询总数量，最小ID和最大ID
			poolID = DBConfig.register(dbURL, dbUser, dbPass);
			progressText = "正在查询数量";
			String condition = (dateType == 0 ? "createtime" : "lastlogin") + " between ? and ? and level >= " + minLevel;
			Object[] params = new Object[] { startDate, endDate };
			String sql = "select count(1) from player where " + condition;
			ArrayList<ResultRow> rows = DBUtils.query(poolID, sql, params);
			int count = rows.get(0).getInt(1);
			if (maxCount < count) {
				count = maxCount;
			}
			if (canceled) {
				DBUtils.clearPool(poolID);
				return;
			}
			progressText = "正在提取：0/" + count;
			
			// 生成临时文件
			File tempFile = File.createTempFile("_report_data", ".dat");
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
			
			// 写出报表文件基本信息：产品、分区、起始日期、结束日期
			dos.writeUTF(productName);
			dos.writeUTF(serverName);
			dos.writeLong(startDate.getTime());
			dos.writeLong(endDate.getTime() - 1000);
			
			// 循环提取记录，每次最多batchCount条
			int start = 0;
			int writeCount = 0;
			while (start < count) {
				// 读取用户数据
				int fetchCount = count - start;
				if (fetchCount > batchCount) {
					fetchCount = batchCount;
				}
				sql = "select id, name, level, sex, money, x, y, mapid, faction, bag, vm, skills, skillpoint, accountid, clazz, propertypoint, equipments, " +
					"actionbar, credit, exist, createtime, honor, lastlogin, horses, weekcredit, rank, titles, formulalist, depot from player where " +
					condition + " limit " + start + "," + fetchCount;
				rows = DBUtils.query(poolID, sql, params);
				if (rows.size() == 0) {
					break;
				}
				int[] ids = new int[rows.size()];
				for (int i = 0; i < rows.size(); i++) {
					byte[] odata = DBUtils.objectToBytes(rows.get(i));
					dos.write(0);
					dos.writeInt(odata.length);
					dos.write(odata);
					writeCount++;
					ids[i] = rows.get(i).getInt(1);
				}
				
				// 读取这些用户的PVP数据
				StringBuilder sb = new StringBuilder();
				sb.append("select id, totaldiecount, totalkillcount from pvpinfo where id in (");
				for (int i = 0; i < ids.length; i++) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append(ids[i]);
				}
				sb.append(')');
				rows = DBUtils.query(poolID, sb.toString());
				for (int i = 0; i < rows.size(); i++) {
					byte[] odata = DBUtils.objectToBytes(rows.get(i));
					dos.write(1);
					dos.writeInt(odata.length);
					dos.write(odata);
				}
				
				// 读取这些用户的消费数据
				sb = new StringBuilder();
				sb.append("select playerid, sum(imoney) from ibuy where playerid in (");
				for (int i = 0; i < ids.length; i++) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append(ids[i]);
				}
				sb.append(") group by playerid");
				rows = DBUtils.query(poolID, sb.toString());
				for (int i = 0; i < rows.size(); i++) {
					byte[] odata = DBUtils.objectToBytes(rows.get(i));
					dos.write(2);
					dos.writeInt(odata.length);
					dos.write(odata);
				}
				
				// 读取这些用户的好友数据
				sb = new StringBuilder();
				sb.append("select id, friends from relation where id in (");
				for (int i = 0; i < ids.length; i++) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append(ids[i]);
				}
				sb.append(')');
				rows = DBUtils.query(poolID, sb.toString());
				for (int i = 0; i < rows.size(); i++) {
					byte[] odata = DBUtils.objectToBytes(rows.get(i));
					dos.write(3);
					dos.writeInt(odata.length);
					dos.write(odata);
				}
				
				start += batchCount;
				if (canceled) {
					dos.close();
					tempFile.delete();
					DBUtils.clearPool(poolID);
					return;
				}
				progressText = "正在提取：" + start + "/" + count;
			}
			dos.write(-1);  // 类型-1的记录表示结束
			dos.flush();
			dos.close();
			DBUtils.clearPool(poolID);
			
			// 提取成功
			progressText = "已提取" + writeCount + "条数据。";
			resultFile = tempFile;
			over = true;
		} catch (Exception e) {
			e.printStackTrace();
			over = true;
			errorMessage = e.toString();
		}
	}
}
