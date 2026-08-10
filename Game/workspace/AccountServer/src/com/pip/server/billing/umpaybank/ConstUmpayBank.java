package com.pip.server.billing.umpaybank;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ConstUmpayBank {
    // public static String ORDER_URL = "http://211.136.93.20:8081/mer/spOrder.jsp";
    // public static String ORDER_URL = "http://211.136.93.21/mer/spOrder.jsp";
    public static String ORDER_URL = "http://payment.umpay.com/mer/spOrder.jsp";
    public static String FUNC_CODE = "8817";
    public static String SPID = "5557";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    public static Random rand = new Random();
    
    public static String getDate() {
        return dateFormat.format(new Date());
    }

    public static String getRandomSign() {
        char[] arr = new char[6];
        for (int i = 0; i < 6; i++) {
            arr[i] = (char)('0' + rand.nextInt(10));
        }
        return new String(arr);
    }
    
    public static String getRemark(int amount) {
        amount = amount * 360 / 100;
        return amount + "Ã÷Öéi±Ò";
    }
}
