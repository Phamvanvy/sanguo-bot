package com.pip.servermgr.client;

/**
 * 服务器发现的一个异常的记录。
 * @author lighthu
 */
public class ExceptionRecord {
	public String title;
	public int repeatCount;
	public String fullStackTrace;
	public String source;
	
	public ExceptionRecord(int count, String trace, String source) {
		this.repeatCount = count;
		this.fullStackTrace = trace;
		this.source = source;
		int index = trace.indexOf('\n');
		if (index == -1) {
			title = trace.trim();
		} else {
			title = trace.substring(0, index).trim();
		}
	}
}
