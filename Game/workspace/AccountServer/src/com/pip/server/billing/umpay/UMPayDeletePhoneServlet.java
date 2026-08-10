package com.pip.server.billing.umpay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

/**
 * 为帐号删除一个绑定手机号。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     phone = 删除的手机号
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果成功，第二到五行分别是：帐号ID、帐号名、绑定手机号、最后修改时间
 *     如果失败，第二行是错误信息
 */
public class UMPayDeletePhoneServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(UMPayDeletePhoneServlet.class);
	private UMPayDataDAO umpayDataDAO;
	private Server server;
	
	public UMPayDeletePhoneServlet(Server s, UMPayDataDAO dao) {
		server = s;
		umpayDataDAO = dao;
	}

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 取得参数
        String name = request.getParameter("name");
        int id = -1;
        if (name == null) {
            id = Integer.parseInt(request.getParameter("id"));
        }
        String phone = request.getParameter("phone");
        
        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证用户名和密码
        Account acc;
        if (name != null) {
            acc = server.findAccountByName(name);
        } else {
            acc = server.findAccount(id);
        }
        if (acc == null) {
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 获取绑定手机号信息
        UMPayData umpayData = umpayDataDAO.getOrCreate(acc.getId(), acc.getName());
        
        // 检查是否到允许删除的时间了
        if (umpayData.getLastModifyTime().getTime() > System.currentTimeMillis() - 86400000L * 30) {
            out.println("1");
            out.println("一个月只能修改一个绑定手机号");
            return;
        }
        
        // 删除手机号
        String[] phones = umpayData.getPhones().split(",");
        StringBuffer newBuf = new StringBuffer();
        for (int i = 0; i < phones.length; i++) {
            if (phones[i].equals(phone)) {
                continue;
            }
            if (newBuf.length() > 0) {
                newBuf.append(",");
            }
            newBuf.append(phones[i]);
        }
        umpayData.setPhones(newBuf.toString());
        umpayData.setLastModifyTime(new java.util.Date());
        umpayDataDAO.update(umpayData);
        
        // 返回信息
        out.println("0");
        out.println(acc.getId());
        out.println(acc.getName());
        out.println(acc.getBalance().getValue());
        out.println(umpayData.getPhones());
        out.println(umpayData.getLastModifyTime().getTime());
    }
}
