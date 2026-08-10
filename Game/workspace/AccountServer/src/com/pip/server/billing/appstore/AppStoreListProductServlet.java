package com.pip.server.billing.appstore;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * Appstore商品查询接口。
 * 请求参数：
 *     bid = 产品bundle id
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果失败，第二行是错误信息，如果成功，从第二行开始，每一行表示一个商品。
 *     每一行包括4列：商品ID、商品描述、商品价格(美分)、商品对应i币(单位是i)，4列之间用TAB分隔。
 */
public class AppStoreListProductServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(AppStoreListProductServlet.class);
    private Server server;
    
    public AppStoreListProductServlet(Server s) {
        server = s;
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
    	String bid = request.getParameter("bid");
    	ConstAppStore.AppStoreProduct[] arr = ConstAppStore.listProduct(bid);
        
        // 设置返回格式
    	response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        if (arr == null) {
        	out.println("1");
        	out.println("无效的应用ID");
        } else {
        	out.println("0");
        	for (int i = 0; i < arr.length ;i++) {
        		out.println(arr[i].productID + "\t" + arr[i].productName + "\t" + arr[i].price + "\t" + arr[i].imoney);
        	}
        }
    }
}
