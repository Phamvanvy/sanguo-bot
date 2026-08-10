package com.pip.server.billing.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class SmsUtil {
    protected static Logger log = Logger.getLogger(SmsUtil.class);
    
    static String URL_AORAN = "http://www.ensms.com/fuction/eeqpost.asp?";
    static String NAME_AORAN = "zhshfx";
    static String PW_AORAN = "123456";
    static String split = "&";

    
    public static boolean send(String mobilephone,String sms){
        StringBuffer sbufUrl = new StringBuffer();
        HttpURLConnection con = null;
        URL url = null;
        InputStream is = null;
        String result = "";
        try {
            sbufUrl.append("mobile=").append(NAME_AORAN).append(split);
            sbufUrl.append("pwd=").append(PW_AORAN).append(split);
            sbufUrl.append("tmobile=").append(mobilephone).append(split);
            sbufUrl.append("msg=").append(sms).append(split);
            sbufUrl.append("action=sendmsg");
            String str = sbufUrl.toString();
            url = new URL(URL_AORAN + str);
            con = (HttpURLConnection) url.openConnection();
            is = con.getInputStream();
            if (is != null) {
                int len = is.available();
                if (len > 0) {
                    byte[] data = new byte[len];
                    is.read(data);
                    result = new String(data);
                }
                is.close();
            }
            log.info(result);
            if (result.indexOf("OK") > 0) {
                return true;
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                con.disconnect();
                con = null;
            }
        }
        return false;

    }
}
