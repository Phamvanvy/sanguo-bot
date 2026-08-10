package com.pip.j0ide.i18n;

import java.io.IOException;

public interface IMessageFile {
	/**
	 * 把一个字符串转换为国际化表示。
	 * @param value 带""前后缀的字符串常量
	 * @return 如果这个字符串不需要国际化，返回null。
	 */
	public String input(String value);
	/**
	 * 保存到文件。
	 * @throws IOException
	 */
	public void save() throws IOException;
	/**
	 * 取得新增字符串数量。
	 * @return
	 */
	public int getNewCount();
}
