package com.pip.server.auth.cmcc.billing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pip.server.auth.cmcc.CmccException;

/**
 * 一个待计费渠道用户。
 * @author lighthu
 */
public class Channel {
    /**
     * 渠道ID。如果一个渠道ID是3，那么在tbl_fee表里面的accountid就是-3.
     */
    public int id;
    /**
     * 渠道登录名。
     */
    public String name;
    /**
     * 渠道密码。
     */
    public String password;
    /**
     * 允许访问的IP地址。
     */
    public Set<String> allowIP = new HashSet<String>();
    /**
     * 允许使用的计费代码。
     */
    public Map<String, String> allowCode = new HashMap<String, String>();
    /**
     * 同步URL。
     */
    public String syncURL;
    
    /**
     * 检查密码和IP。
     * @param password
     * @param ip
     * @return
     */
    public String check(String password, String ip, String code) throws CmccException {
        if (!this.password.equals(password)) {
            throw new CmccException("用户名或密码错误。");
        }
//        if (!allowIP.contains(ip)) {
//            throw new CmccException("禁止访问。");
//        }
        if (!allowCode.containsKey(code)) {
            throw new CmccException("非法计费代码。");
        }
        return allowCode.get(code);
    }
}
