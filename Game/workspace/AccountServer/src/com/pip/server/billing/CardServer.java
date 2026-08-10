package com.pip.server.billing;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pip.net.http.JettyServer;
import com.pip.server.billing.card.CheckCardServlet;
import com.pip.server.billing.card.GenerateCardServlet;
import com.pip.server.billing.f3g.EventListServlet;
import com.pip.server.billing.f3g.EventReportServlet;
import com.pip.server.billing.f3g.ServerInfoServlet;
/**
 * 计费服务器。提供续费相关的HTTP接口。
 */
public class CardServer {
    private static Logger log;
    private PropertiesConfiguration configuration;
    private JettyServer httpServer;
    private String host;
    private int port;

    public CardServer() {
    }

    
    public String getHost(){
    	return host;
    }
    
    public int getPort(){
    	return port;
    }
    
    // 初始化Log4j
    private void initLog() throws Exception {
        PropertyConfigurator.configure("billing_log4j.properties");
        log = Logger.getLogger(CardServer.class);
    }

    public void launch() throws Exception {
        // 载入配置文件
        configuration = new PropertiesConfiguration("billing_config.properties");
        host = configuration.getString("host");
        port = configuration.getInt("port");
        String[] allows = configuration.getStringArray("trust_ips");

        // 启动Jetty服务器
        httpServer = new JettyServer(host, port, 3, 50);
        
        // 道具兑换卡系统
        httpServer.addServlet("/card_gen", new GenerateCardServlet(allows, 12));
        httpServer.addServlet("/card_check", new CheckCardServlet(allows));
        
        // 服务器信息获取
        httpServer.addServlet("/serverinfo", new ServerInfoServlet(allows));
        
        // 大事件管理
        httpServer.addServlet("/evt_list", new EventListServlet(allows));
        httpServer.addServlet("/evt_report", new EventReportServlet(allows));
        
        httpServer.start();
    }
    
    public String getServerURL() {
        return "http://" + host + ":" + port;
    }

    public static void main(String[] args) {
        CardServer server = new CardServer();
        try {
            server.initLog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            server.launch();
        } catch (Exception e) {
            log.error(e, e);
        }
    }
}
