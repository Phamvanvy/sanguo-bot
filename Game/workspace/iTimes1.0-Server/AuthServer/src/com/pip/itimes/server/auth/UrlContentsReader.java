package com.pip.itimes.server.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * 根据给定的URL，从前台页面中读取页面内容的类
 * 
 * @author Frank
 *
 */
public class UrlContentsReader {
    // logger
    private static final Logger log = Logger.getLogger(UrlContentsReader.class);
    
    // 建立的Http连接
    private HttpURLConnection connection = null;
    // 页面中首个META标签中CONTENT的值
    private String metaContent = "";
    
    /**
     * UrlContentsReader的构造方法。
     */
    public UrlContentsReader() {
    }
    
    /**
     * 根据指定的URL建立Http连接。
     * 
     * @param urlString 指定的URL
     */
    public void connect(String urlString) throws Exception {
        try {
            // 通过指定的URL创建URL
            URL url = new URL(urlString);
            // 建立连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方式为POST
            // connection.setRequestMethod("POST");
            int code = connection.getResponseCode();
            if (code != 200) {
            	throw new Exception("Wrong response code!");
            }
        } catch (MalformedURLException e) {
            log.debug("Create url error.");
            throw e;
        } catch (IOException e) {
            log.debug("Open connection error.");
            throw e;
        }
    }
    
    /**
     * 从给定连接中读取首个META标签中MobilePayPlatform参数的值。
     * 
     * @return META标签中CONTENT的值
     */
    public String readMetaContent() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;
            // 循环查找META标签
            while ((line = reader.readLine()) != null) {
                log.debug(line);
                // 找到META标签时，查看是否MobilePayPlatform参数，如果是则取出CONTENT的值
                int pos;
                if ((pos = CommonFunction.expect(line, "<META", 0, null, true)) == -1) {
                	continue;
                }
                pos = CommonFunction.expect(line, "MobilePayPlatform", pos, null, true);
                if (pos == -1) {
                	continue;
                }
                pos = CommonFunction.expect(line, "CONTENT", 0, null, true);
                if (pos == -1) {
                	continue;
                }
                pos = CommonFunction.expect(line, "=", pos, null, true);
                if (pos == -1) {
                	continue;
                }
                pos = CommonFunction.expect(line, "\"", pos, null, true);
                if (pos == -1) {
                	continue;
                }
                pos++;
                StringBuffer buf = new StringBuffer();
                CommonFunction.expect(line, "\"", pos, buf, false);
                metaContent = buf.toString();
                break;
            }
        } catch (IOException e) {
            log.debug("Read url contents error.");
            log.error(e, e);
        }
        
        return metaContent;
    }
    
    /**
     * 分析META中CONTENT的值，根据UMPay返回结果的位置，取出相应的值。
     * 
     * @param Index UMPay返回结果的位置，从1开始编号
     * @return UMPay的返回结果
     * 
     * 注意：在调用这个方法之前需要调用readMetaContent()方法来进行对metaContent的设置。
     */
    public String getResultFromMetaContent(int index) {
    	String[] secs = metaContent.split("\\|");
    	if (index <= secs.length) {
    		return secs[index - 1];
    	}
    	return "";
    }
    
    /**
     * 关闭连接，释放资源。
     */
    public void close() {
    	if (connection != null) {
    		try {
    			connection.disconnect();
    		} catch (Exception e) {
    		}
    	}
    }
}
