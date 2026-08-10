package com.pip.itimes.server.auth;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 共通函数的类
 * 
 * @author Frank
 *
 */
public class CommonFunction {
    /**
     * 日期格式化工具
     */
    private static SimpleDateFormat dateFormat = new SimpleDateFormat();
    
    /**
     * 按照yyyyMMdd格式得到当前日期。
     * 
     * @return 当前日期
     */
    public static String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();
        String currDate = "";
        
        try {
            // 设置日期格式
            dateFormat.applyPattern("yyyyMMdd");
            // 格式化日期
            currDate = dateFormat.format(currentTime);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            currDate = "";
        }
        
        return currDate;
    }
    
    /**
     * 字符串搜索工具。
     * @param source 内容字符串
     * @param search 要搜的字符串
     * @param from 开始位置
     * @param buf 可选，如果不为空，则从开始未知到找到的字符串未知之间的内容被放到buf里
     * @param ignoreCase 如果为true，搜索时忽略大小写
     * @return 如果找到，返回找到目标的位置，否则返回-1.
     */
    public static int expect(String source, String search, int from, StringBuffer buf, boolean ignoreCase) {
    	int pos;
    	if (ignoreCase) {
    		pos = source.toUpperCase().indexOf(search.toUpperCase(), from);
    	} else {
    		pos = source.indexOf(search, from);
    	}
    	if (pos == -1) {
    		if (buf != null) {
    			buf.append(source.substring(from));
    		}
    		return -1;
    	} else {
    		if (buf != null) {
    			buf.append(source.substring(from, pos));
    		}
    		return pos;
    	}
    }
}
