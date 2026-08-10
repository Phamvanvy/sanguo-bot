package com.pip.server.auth;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

/**
 * 保存常量的类
 * 
 * @author Frank
 * 
 */
public class Const {
    /** 每月最大扣费数 */
    public static final int MONTH_MAX = 576000;

    /** 把对象转换为String。如果对象为null，返回空串。 */
    public static String objectToString(Object obj) {
        try {
            if (obj == null) {
                return "";
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            String ret = new sun.misc.BASE64Encoder().encode(bos.toByteArray());
            ret = ret.replace("\n", "");
            ret = ret.replace("\r", "");
            return ret;
        } catch (Exception e) {
            return "";
        }
    }

    /** 把String转换为对象。 */
    public static Object stringToObject(String str) {
        try {
            if (str == null || str.length() == 0) {
                return null;
            }
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer(str);
            return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查某个时间是否在本月或者将来的月份。
     */
    public static boolean inLaterMonth(java.util.Date checkDate, java.util.Date now) {
        if (checkDate == null) {
            return false;
        }
        Calendar current = Calendar.getInstance();
        Calendar last = Calendar.getInstance();

        current.setTime(now);
        last.setTime(checkDate);
        int m1 = last.get(Calendar.YEAR) * 100 + last.get(Calendar.MONTH);
        int m2 = current.get(Calendar.YEAR) * 100 + current.get(Calendar.MONTH);
        return m1 >= m2;
    }
}
