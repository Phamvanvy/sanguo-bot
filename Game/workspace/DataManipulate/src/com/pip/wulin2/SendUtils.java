package com.pip.wulin2;

import com.pip.util.DBUtils;

public class SendUtils {
	/**
	 * 为指定游戏区的某个角色发送一个邮件。
	 * @param poolID 游戏区连接池ID
	 * @param roleID 角色ID
	 * @param name 角色名称
	 * @param title 邮件标题
	 * @param content 邮件内容
	 * @param attachItem 附件ID（只能是扩展物品）, -1表示没有
	 * @param count 物品数量
	 * @throws Exception
	 */
	public static void sendMail(String poolID, int roleID, String name, String title, String content, 
			int attachItem, int count) throws Exception {
		if (attachItem == -1) {
			String sql = "insert into tbl_mail (sourceid, sourcename, destid, destname, title, content, attachment" +
				",price,posttime,readed,validtime) values (-1, '系统', ?, ?, ?, ?, null, 0, now(), 0, now())";
			DBUtils.update(poolID, sql, new Object[] { roleID, name, title, content });
		} else {
			String sql = "insert into tbl_mail (sourceid, sourcename, destid, destname, title, content, attachment" +
				",price,posttime,readed,validtime) values (-1, '系统', ?, ?, ?, ?, ?, 0, now(), 0, now())";
			byte[] attData = new byte[7];
			attData[0] = 2;
			attData[1] = (byte)((attachItem >> 24) & 0xFF);
			attData[2] = (byte)((attachItem >> 16) & 0xFF);
			attData[3] = (byte)((attachItem >> 8) & 0xFF);
			attData[4] = (byte)(attachItem & 0xFF);
			attData[5] = (byte)((count >> 8) & 0xFF);
			attData[6] = (byte)(count & 0xFF);
			DBUtils.update(poolID, sql, new Object[] { roleID, name, title, content, attData });
		}
	}
}
