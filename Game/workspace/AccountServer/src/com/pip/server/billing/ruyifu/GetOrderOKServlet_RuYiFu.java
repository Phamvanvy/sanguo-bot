package com.pip.server.billing.ruyifu;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * 如意付渠道下单返回地址。
 * 请求参数：
 *     见如意付规范。
 * 返回(UTF-8编码)：
 *     无
 */
public class GetOrderOKServlet_RuYiFu extends HttpServlet {
    private static Logger log = Logger.getLogger(GetOrderOKServlet_RuYiFu.class);
    private Order_RuYiFuDAO dataDAO;
    private Server server;
    private EncDec cipher;
    
    public GetOrderOKServlet_RuYiFu(Server s, Order_RuYiFuDAO dao) {
        server = s;
        dataDAO = dao;
        cipher = new EncDec();
        cipher.setKey(Const_RuYiFu.MERCHANT_KEY);
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
    }
}
