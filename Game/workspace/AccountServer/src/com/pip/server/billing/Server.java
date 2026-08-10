package com.pip.server.billing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pip.net.http.JettyServer;
import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.alipay.AlipayCallBack4WebServlet;
import com.pip.server.billing.alipay.AlipayCallBackServlet;
import com.pip.server.billing.alipay.AlipayNotify4ClientServlet;
import com.pip.server.billing.alipay.AlipayNotify4WebServlet;
import com.pip.server.billing.alipay.AlipayNotifyServlet;
import com.pip.server.billing.alipay.GetOrder4ClientAlipayServlet;
import com.pip.server.billing.alipay.GetOrder4WebAlipayServlet;
import com.pip.server.billing.alipay.GetOrderAlipayServlet;
import com.pip.server.billing.alipay.Order_AlipayDAO;
import com.pip.server.billing.appstore.AppStoreListProductServlet;
import com.pip.server.billing.appstore.AppStoreOrderManager;
import com.pip.server.billing.appstore.AppStoreOrderServlet;
import com.pip.server.billing.appstore.ConstAppStore;
import com.pip.server.billing.appstore.Order_AppStoreDAO;
import com.pip.server.billing.card.CheckCardServlet;
import com.pip.server.billing.card.GenerateCardServlet;
import com.pip.server.billing.chinarund.ChianRunCallbackServlet;
import com.pip.server.billing.chinarund.ChinaRunServlet;
import com.pip.server.billing.chinarund.PayInfoDAO;
import com.pip.server.billing.kongzhong.KongZhongNotifyServlet;
import com.pip.server.billing.paypal.Order_PaypalDAO;
import com.pip.server.billing.paypal.PaypalChargeRateServlet;
import com.pip.server.billing.paypal.PaypalNotifyServlet;
import com.pip.server.billing.paypal.PaypalOrderServlet;
import com.pip.server.billing.paypal.PaypalQueryServlet;
import com.pip.server.billing.paypal.PaypalWapCallbackServlet;
import com.pip.server.billing.paypal.PaypalWapCancelServlet;
import com.pip.server.billing.paypal.PaypalWapOrderServlet;
import com.pip.server.billing.ruyifu.GetOrderOKServlet_RuYiFu;
import com.pip.server.billing.ruyifu.GetOrderServlet_RuYiFu;
import com.pip.server.billing.ruyifu.NotifyServlet_RuYiFu;
import com.pip.server.billing.ruyifu.Order_RuYiFuDAO;
import com.pip.server.billing.security.AccountSecurityDAO;
import com.pip.server.billing.security.AccountSecurityService;
import com.pip.server.billing.security.BindCallbackServlet;
import com.pip.server.billing.security.BindRequestDAO;
import com.pip.server.billing.security.GetBackSecurityServlet;
import com.pip.server.billing.security.MailUtil;
import com.pip.server.billing.security.MoServlet;
import com.pip.server.billing.security.ModifySecurityServlet;
import com.pip.server.billing.security.SecurityStatusServlet;
import com.pip.server.billing.security.ValidSecurityServlet;
import com.pip.server.billing.tiantian.TTFGetOrderServlet;
import com.pip.server.billing.tiantian.TTFNotifyServlet;
import com.pip.server.billing.u19pay.Callback19PayServlet;
import com.pip.server.billing.u19pay.GetOrder19PayServlet;
import com.pip.server.billing.u19pay.GetOrderServlet_19PayD;
import com.pip.server.billing.u19pay.Order_19PayDAO;
import com.pip.server.billing.u19pay.PaymentNotifyServlet_19PayD;
import com.pip.server.billing.umpay.GetOrderServlet;
import com.pip.server.billing.umpay.ProductManager;
import com.pip.server.billing.umpay.ReceivePaymentNotifyServlet;
import com.pip.server.billing.umpay.SendOrderWapServlet;
import com.pip.server.billing.umpay.UMPayAddPhoneServlet;
import com.pip.server.billing.umpay.UMPayDataDAO;
import com.pip.server.billing.umpay.UMPayDeletePhoneServlet;
import com.pip.server.billing.umpay.UMPayListPhonesServlet;
import com.pip.server.billing.umpay.UMPayLoginServlet;
import com.pip.server.billing.umpaybank.PaymentNotifyServlet_Umpay2;
import com.pip.server.billing.umpaybank.PlaceOrderServlet_Umpay2;
import com.pip.server.billing.yeepay.ConstYeepay;
import com.pip.server.billing.yeepay.QueryChargeRecordServlet;
import com.pip.server.billing.yeepay.YeepayChargeRateServlet;
import com.pip.server.billing.yeepay.YeepayNotifyServlet;
import com.pip.server.billing.yeepay.YeepayNotifyServlet2;
import com.pip.server.billing.yeepay.YeepayOrderManager;
import com.pip.server.billing.yeepay.YeepayOrderServlet;
import com.pip.server.billing.yeepay.YeepayOrderServlet2;
import com.pip.server.billing.yeepay.YeepayQueryServlet;
import com.pip.server.billing.yeepay.YeepayStatusServlet;
import com.pip.server.billing.yeepaybank.YeepaybankNotify4WebServlet;
import com.pip.server.billing.yeepaybank.YeepaybankNotifyServlet;
import com.pip.server.billing.yeepaybank.YeepaybankOrder4WebServlet;
import com.pip.server.billing.yeepaybank.YeepaybankOrderServlet;
import com.umpay.SignEnc;
import com.umpay.SignEncException;
/**
 * 计费服务器。提供续费相关的HTTP接口。
 */
public class Server {
    private static Logger log;
    private PropertiesConfiguration configuration;
    private JettyServer httpServer;
    private String host;
    private int port;

    // 计费请求地址
    private String redirectServer;
    
    // 手机帐号密码充置需要访问的服务器地址
    private String redirectServer2;

    // 信任IP
    public final Set trustip_directfee = new HashSet();
    public final Set trustip_wapfeeold = new HashSet();
    public final Set trustip_wapfee = new HashSet();

    // WAP支付黑名单管理
    private HashMap wapBlackList = new HashMap();
    private long wapBlackListFileTime = 0;

    // 支付黑名单管理
    private HashMap blackList = new HashMap();
    private long blackListFileTime = 0;

    // 支付次数管理
    private final HashMap billingCounts = new HashMap();
    private long lastAccessTime = System.currentTimeMillis();
    
    public UMPayDataDAO umpayDataDAO;
    Order_19PayDAO order19PayDAO;
    Order_RuYiFuDAO orderRuYiFuDAO;

    AccountSecurityDAO accountSecurityDAO;
    AccountSecurityService accountSecurityService;
    BindRequestDAO bindRequestDAO;
    
    Order_AlipayDAO alipayDAO;
    
    Order_PaypalDAO paypalDAO;
    
    public GetOrderServlet_19PayD servlet19pay;

    public Server() {
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
        log = Logger.getLogger(Server.class);
    }

    public void launch() throws Exception {
        // 载入配置文件
        configuration = new PropertiesConfiguration("billing_config.properties");
        host = configuration.getString("host");
        port = configuration.getInt("port");
        redirectServer = configuration.getString("redirect_server");
        redirectServer2 = configuration.getString("redirect_server2");
        for (String ip : configuration.getStringArray("trustip_directfee")) {
        	if (ip.contains("-")) {
        		int pos1 = ip.lastIndexOf('.');
        		int pos2 = ip.indexOf('-');
        		int v1 = Integer.parseInt(ip.substring(pos1 + 1, pos2));
        		int v2 = Integer.parseInt(ip.substring(pos2 + 1));
        		String prefix = ip.substring(0, pos1 + 1);
        		for (int v = v1; v <= v2; v++) {
        			trustip_directfee.add(prefix + v);
        		}
        	} else {
        		trustip_directfee.add(ip);
        	}
        }
        Collections.addAll(trustip_wapfeeold,
                           configuration.getStringArray("trustip_wapfeeold"));
        Collections.addAll(trustip_wapfee,
                           configuration.getStringArray("trustip_wapfee"));

        // 初始化产品管理器
        ProductManager.instance = new ProductManager(this);

        // 载入支付黑名单
        loadBlackList();
        loadWapBlackList();

        // 启动Jetty服务器
        httpServer = new JettyServer(host, port, 3, 50);

        // 直接完成订单接口，仅用作内部用途
        httpServer.addServlet("/DirectFee", new DirectFeeServlet());
        httpServer.addServlet("/DirectFee2", new DirectFee2Servlet());
        httpServer.addServlet("/FeeOld", new FeeServlet());
        httpServer.addServlet("/ClearMonthFee", new ClearMonthFeeServlet());
        httpServer.addServlet("/Unsubscribe", new UnsubscribeServlet());
        httpServer.addServlet("/CheckLogin", new CheckLoginServlet());
        httpServer.addServlet("/CheckAccountName", new CheckAccountNameServlet());
        httpServer.addServlet("/GetAccountBalance", new GetBalanceServlet());
        httpServer.addServlet("/GetAccountCredit", new GetCreditServlet());

//        // 新浪老续费方式，可以刷
//        httpServer.addServlet("/WapFeeOld", new WapFeeServlet());
//        httpServer.addServlet("/WapFeeOkOld", new WapFeeOkServlet());
//        httpServer.addServlet("/WapFeePreOld", new WapFeePreServlet());
//
//        // 新浪新续费方式
//        httpServer.addServlet("/WapFee1", new WapFeeServlet1());
//        httpServer.addServlet("/WapFeeOk1", new WapFeeOkServlet1(this));
//        httpServer.addServlet("/WapFeePre1", new WapFeePreServlet1());

        // 手机钱包(Unipay)续费接口
        httpServer.addServlet("/receivePaymentNotify", new ReceivePaymentNotifyServlet(this));
        httpServer.addServlet("/getOrder", new GetOrderServlet(this));
//        httpServer.addServlet("/getOrder", new GetNewOrderServlet(this));
//        httpServer.addServlet("/receivePaymentNotify", new ReceiveNewPaymentNotifyServlet(this));
        httpServer.addServlet("/sendOrderWAP_umpay", new SendOrderWapServlet(this));
        httpServer.addServlet("/queryReport", new QueryReportServlet());
        httpServer.addServlet("/querySubscribe", new QuerySubscribeServlet());

        // 计费限制管理接口
        httpServer.addServlet("/limit_list", new LimitListServlet());
        httpServer.addServlet("/limit_add", new LimitAddServlet());
        httpServer.addServlet("/limit_delete", new LimitDeleteServlet());
        
        // 神州行直冲接口
        PayInfoDAO payInfoDAO = new PayInfoDAO();
        Map<String,String> callbacks = new ConcurrentHashMap<String,String>();
        httpServer.addServlet("/chinarun", new ChinaRunServlet(this, payInfoDAO,callbacks,configuration.getString("callback")));
        httpServer.addServlet("/chinaruncb", new ChianRunCallbackServlet(this, payInfoDAO,callbacks,configuration.getString("chargeurl")));
        
        // 手机钱包手机号绑定系统
        umpayDataDAO = new UMPayDataDAO();
        httpServer.addServlet("/ump_login", new UMPayLoginServlet(this, umpayDataDAO));
        httpServer.addServlet("/ump_listphones", new UMPayListPhonesServlet(this, umpayDataDAO));
        httpServer.addServlet("/ump_deletephone", new UMPayDeletePhoneServlet(this, umpayDataDAO));
        httpServer.addServlet("/ump_addphone", new UMPayAddPhoneServlet(this, umpayDataDAO));
        
        // 19Pay支付系统
        order19PayDAO = new Order_19PayDAO();
        httpServer.addServlet("/19pay_order", new GetOrder19PayServlet(this, order19PayDAO));
        httpServer.addServlet("/19pay_callback", new Callback19PayServlet(this, order19PayDAO));
        httpServer.addServlet("/19pay_notify", new PaymentNotifyServlet_19PayD(this, order19PayDAO));
        servlet19pay = new GetOrderServlet_19PayD(this, order19PayDAO);
        httpServer.addServlet("/19payd_order", servlet19pay);
        httpServer.addServlet("/19payd_notify", new PaymentNotifyServlet_19PayD(this, order19PayDAO));
        
        // 如意付支付系统
        orderRuYiFuDAO = new Order_RuYiFuDAO();
        httpServer.addServlet("/ruyifu_order", new GetOrderServlet_RuYiFu(this, orderRuYiFuDAO));
        httpServer.addServlet("/ruyifu_orderok", new GetOrderOKServlet_RuYiFu(this, orderRuYiFuDAO));
        httpServer.addServlet("/ruyifu_notify", new NotifyServlet_RuYiFu(this, orderRuYiFuDAO));
        
        // 空中短信支付
        httpServer.addServlet("/kongzhong_notify", new KongZhongNotifyServlet(this));
        
        // 手机钱包银行卡支付系统
        httpServer.addServlet("/umpay2_order", new PlaceOrderServlet_Umpay2(this));
        httpServer.addServlet("/umpay2_notify", new PaymentNotifyServlet_Umpay2(this));
        
        // 易宝神州行支付系统
        ConstYeepay.loadConfig();
        httpServer.addServlet("/yeepay_order", new YeepayOrderServlet(this, payInfoDAO));
        httpServer.addServlet("/yeepay_notify", new YeepayNotifyServlet(this, payInfoDAO));
        httpServer.addServlet("/yeepay_query", new YeepayQueryServlet(this));
        Map<String,String> callbacks2 = new ConcurrentHashMap<String,String>();
        httpServer.addServlet("/yeepay_order2", new YeepayOrderServlet2(this, payInfoDAO, callbacks2));
        httpServer.addServlet("/yeepay_notify2", new YeepayNotifyServlet2(this, payInfoDAO, callbacks2));
        httpServer.addServlet("/yeepay_charge_rate", new YeepayChargeRateServlet(this));
        httpServer.addServlet("/yeepaybank_order", new YeepaybankOrderServlet(this,payInfoDAO));
        httpServer.addServlet("/yeepaybank_notify", new YeepaybankNotifyServlet(this,payInfoDAO));
        httpServer.addServlet("/yeepaybank4web_order", new YeepaybankOrder4WebServlet(this,payInfoDAO));
        httpServer.addServlet("/yeepaybank4web_notify", new YeepaybankNotify4WebServlet(this,payInfoDAO));
        
        // 自动订单重试系统
        new YeepayOrderManager().start();
        
        // 密码保护系统
        accountSecurityDAO = new AccountSecurityDAO();
        bindRequestDAO = new BindRequestDAO();
        accountSecurityService = new AccountSecurityService(accountSecurityDAO,bindRequestDAO,this);
        httpServer.addServlet("/modify", new ModifySecurityServlet(accountSecurityService));
        httpServer.addServlet("/valid", new ValidSecurityServlet(accountSecurityService));
        httpServer.addServlet("/getback", new GetBackSecurityServlet(accountSecurityService));
        httpServer.addServlet("/status", new SecurityStatusServlet(accountSecurityService));
        httpServer.addServlet("/bindconfirm", new BindCallbackServlet(accountSecurityService,this));
        httpServer.addServlet("/smsconfirm", new MoServlet(accountSecurityService,this));
        MailUtil.init("mails.pearlinpalm.com", "addressbak", "AddressBak071105");
        MailUtil.start();
        
        //支付宝支付系统    
        alipayDAO = new Order_AlipayDAO();
        //1.支付宝WAP支付方式
        httpServer.addServlet("/alipay_order", new GetOrderAlipayServlet(this,alipayDAO));
        httpServer.addServlet("/alipay_notify", new AlipayNotifyServlet(this,alipayDAO));
        httpServer.addServlet("/alipay_callback", new AlipayCallBackServlet(this,alipayDAO));
        //2.支付宝手机客户端支付方式
        httpServer.addServlet("/alipay_order_4client", new GetOrder4ClientAlipayServlet(this,alipayDAO));
        httpServer.addServlet("/alipay_notify_4client", new AlipayNotify4ClientServlet(this,alipayDAO));
        //3.支付宝WEB支付方式
        httpServer.addServlet("/alipay_order_4web", new GetOrder4WebAlipayServlet(this,alipayDAO));
        httpServer.addServlet("/alipay_notify_4web", new AlipayNotify4WebServlet(this,alipayDAO));
        httpServer.addServlet("/alipay_callback_4web", new AlipayCallBack4WebServlet(this,alipayDAO));
        
        //Paypal支付系统    
        paypalDAO = new Order_PaypalDAO();
        httpServer.addServlet("/paypal_order", new PaypalOrderServlet(this,paypalDAO));
        httpServer.addServlet("/paypal_notify", new PaypalNotifyServlet(this,paypalDAO));
        httpServer.addServlet("/paypal_charge_rate", new PaypalChargeRateServlet(this));
        httpServer.addServlet("/paypal_query", new PaypalQueryServlet(this,paypalDAO));
        httpServer.addServlet("/paypalw_order", new PaypalWapOrderServlet(this,paypalDAO));
        httpServer.addServlet("/paypalw_callback", new PaypalWapCallbackServlet(this,paypalDAO));
        httpServer.addServlet("/paypalw_cancel", new PaypalWapCancelServlet(this,paypalDAO));
        
        //天天付支付系统
        httpServer.addServlet("/ttf_order", new TTFGetOrderServlet(this,payInfoDAO));
        httpServer.addServlet("/ttf_notify", new TTFNotifyServlet(this,payInfoDAO));
        
        // appstore支付系统
        ConstAppStore.loadConfig();
        AppStoreOrderManager.server = this;
        httpServer.addServlet("/appstore_order", new AppStoreOrderServlet(this));
        httpServer.addServlet("/appstore_list_product", new AppStoreListProductServlet(this));
        new AppStoreOrderManager().start();
        
        httpServer.start();
        
        // 道具兑换卡系统
        httpServer.addServlet("/card_gen", new GenerateCardServlet(null, 10));
        httpServer.addServlet("/card_check", new CheckCardServlet(null));
        
        // 充值记录查询
        httpServer.addServlet("/query_charge", new QueryChargeRecordServlet(this, payInfoDAO, order19PayDAO, orderRuYiFuDAO, new Order_AppStoreDAO()));
        
        // 运行状态查询
        httpServer.addServlet("/query_status", new YeepayStatusServlet(this));
    }
    
    public String getServerURL() {
        return "http://" + host + ":" + port;
    }

    // 载入支付黑名单，缓存到内存，如果文件改变，则重新载入
    public synchronized void loadBlackList() {
        try {
            File blackListFile = new File(System.getProperty("user.dir") +
                                          "/blacklist.txt");
            if (blackListFile.exists() &&
                blackListFile.lastModified() != blackListFileTime) {
                HashMap newList = new HashMap();
                FileReader fr = new FileReader(blackListFile);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        newList.put(line, line);
                    }
                }
                fr.close();
                blackList = newList;
                blackListFileTime = blackListFile.lastModified();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    // 判断一个手机号是否在黑名单中
    public boolean inBlackList(String phone) {
        return blackList.containsKey(phone);
    }

    // 载入支付黑名单，缓存到内存，如果文件改变，则重新载入
    public synchronized void loadWapBlackList() {
        try {
            File blackListFile = new File(System.getProperty("user.dir") +
                                          "/wapblacklist.txt");
            if (blackListFile.exists() &&
                blackListFile.lastModified() != wapBlackListFileTime) {
                HashMap newList = new HashMap();
                FileReader fr = new FileReader(blackListFile);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        newList.put(line, line);
                    }
                }
                fr.close();
                wapBlackList = newList;
                wapBlackListFileTime = blackListFile.lastModified();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    // 判断一个手机的MID是否在黑名单中
    public boolean inWapBlackList(String mid) {
        return wapBlackList.containsKey(mid);
    }

    // 增加一个用户的计费次数
    public void incrementBillingCount(String msisdn) {
    	synchronized (billingCounts) {
    		Integer oldValue = (Integer)billingCounts.get(msisdn);
    		if (oldValue == null) {
    			billingCounts.put(msisdn, new Integer(1));
    		} else {
    			billingCounts.put(msisdn, new Integer(oldValue.intValue() + 1));
    		}
    	}
    }

    // 判断一个IP地址是否合法的新浪续费IP地址
    public boolean isTrustWapFeeIP(String ip) {
    	return trustip_wapfee.contains(ip);
    }

    // 取得一个用户的计费次数
    public int getBillingCount(String msisdn) {
    	synchronized (billingCounts) {
    		// 检查是否过了9点，过了9点应清除
    		long now = System.currentTimeMillis();
    		Calendar oldCal = Calendar.getInstance();
    		Calendar newCal = Calendar.getInstance();
    		oldCal.setTimeInMillis(lastAccessTime);
    		newCal.setTimeInMillis(now);
    		if (oldCal.get(Calendar.HOUR_OF_DAY) < 9 && newCal.get(Calendar.HOUR_OF_DAY) >= 9) {
    			billingCounts.clear();
    		}
    		lastAccessTime = now;

    		Integer value = (Integer)billingCounts.get(msisdn);
    		if (value == null) {
    			return 0;
    		} else {
    			return value.intValue();
    		}
    	}
    }

    /**
     * 完成一个账单。
     * @param feeId 账单ID
     * @return 如果账单处理成功，返回true，否则返回false。
     */
    public boolean fulfillOrder(int feeID) {
		try {
    		String result = requestBilling("fulfillOrder", new String[] { "feeID" },
    				new String[] { String.valueOf(feeID) });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
	}

    /**
     * 完成一个c币账单。
     * @param feeId 账单ID
     * @return 如果账单处理成功，返回true，否则返回false。
     */
    public boolean fulfillOrder2(int feeID) {
		try {
    		String result = requestBilling("fulfillOrder2", new String[] { "feeID" },
    				new String[] { String.valueOf(feeID) });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
	}
    
    /**
     * 根据帐户ID查找帐户对象，如果帐户不存在，返回null。
     */
    public Account findAccount(int id) {
        try {
			String result = requestBilling("findAccount", new String[] { "id" }, new String[] { String.valueOf(id) });
			return (Account)Const.stringToObject(result);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据帐户名称查找帐户对象，如果帐户不存在，返回null。
     */
    public Account findAccountByName(String name) {
        try {
			String result = requestBilling("findAccountByName", new String[] { "name" }, new String[] { name });
			return (Account)Const.stringToObject(result);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据手机号查找对应的包月账户，如果帐户不存在，返回null。
     */
    public Account findAccountBySubscribePhone(String phone, int productCode) {
        try {
			String result = requestBilling("findAccountBySubscribePhone", new String[] { "phone", "productCode" }, 
					new String[] { phone, String.valueOf(productCode) });
			return (Account)Const.stringToObject(result);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据ID查找账单对象，如果账单不存在，返回null。
     */
    public Fee findFee(int id) {
    	try {
    		String result = requestBilling("findFee", new String[] { "id" }, new String[] { String.valueOf(id) });
			return (Fee)Const.stringToObject(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return null;
    	}
    }

    /**
     * 查找某个渠道的最新一条续费记录。
     */
    public Fee findLatestFee(String channel) {
    	try {
    		String result = requestBilling("findLatestFee", new String[] { "channel" }, new String[] { channel });
    		return (Fee)Const.stringToObject(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return null;
    	}
    }

    /**
     * 创建一条新的计费账单。对于预先知道续费渠道的账单，生成账单时提供金额和渠道号，完成账单时不需要再提供。
     * 对于不知道续费渠道的账单，生成账单时可先不提供这两个数据，但在完成账单时必须提供。
     * @param accountName 计费帐户名称
     * @param amount 账单金额（单位为i币*100），0表示不确定
     * @param channel 渠道ID，参见Const类，null或空串表示不确定
     * @return 新创建的账单对象
     */
    public Fee newFee(String accountName, int amount, String channel) {
    	try {
    		String result = requestBilling("newFee", new String[] { "accountName", "amount", "channel" },
    				new String[] { accountName, String.valueOf(amount), String.valueOf(channel) });
			return (Fee)Const.stringToObject(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return null;
    	}
    }

    /**
     * 查找某一个渠道本月的消费总额。
     */
    public int getMonthPayment(String channel) {
    	try {
    		String result = requestBilling("getMonthPayment", new String[] { "channel" }, new String[] { channel });
    		return Integer.parseInt(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return 0;
    	}
    }
    
    /**
     * 查找某一个帐号总计神州行充值金额。
     */
    public int getChinarunCharge(int id) {
        try {
            String result = requestBilling("getChinarunCharge", new String[] { "id" }, new String[] { String.valueOf(id) });
            return Integer.parseInt(result);
        } catch (Exception e) {
            log.error(e, e);
            return 0;
        }
    }
    
    /**
     * 取消用户订购的包月服务。
     * @param accountName 帐户名称
     * @param productCode 产品代码
     * @return 
     */
    public boolean unPurchase(String accountName, int productCode) {
    	try {
    		String result = requestBilling("unPurchase", new String[] { "accountName", "productCode" }, 
    				new String[] { accountName, String.valueOf(productCode) });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
    }

    /**
     * 为用户订购包月服务。
     * @param feeID 预先创建的计费记录ID
     * @param accountName 帐户名称
     * @param productCode 产品代码
     * @return 
     */
    public boolean purchaseProduct(int feeID, int productCode, String phone) {
    	try {
    		String result = requestBilling("purchaseProduct", new String[] { "feeID", "productCode", "phone" }, 
    				new String[] { String.valueOf(feeID), String.valueOf(productCode), phone });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
    }

    public String resetPassword(String accountName,int pay){
    	try {
    		String result = requestBilling("resetPassword2", new String[] { "accountName","pay" }, 
    				new String[] { accountName,String.valueOf(pay) });
    		return result;
    	} catch (Exception e) {
    		log.error(e, e);
    		return null;
    	}   	
    }
    
    /**
     * 重置用户密码。
     * @param feeID 预先创建的计费记录ID
     * @return 
     */
    public String resetPassword(int feeID) {
    	try {
    		String result = requestBilling("resetPassword", new String[] { "feeID" }, 
    				new String[] { String.valueOf(feeID) });
    		return result;
    	} catch (Exception e) {
    		log.error(e, e);
    		return null;
    	}
    }
    
    /**
     * 重置用户密码。
     * @param feeID 预先创建的计费记录ID
     * @return 
     */
    public boolean changePhone(int feeID, String newPhone) {
    	try {
    		String result = requestBilling("changePhone", new String[] { "feeID", "newPhone" }, 
    				new String[] { String.valueOf(feeID), newPhone });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
    }
    
    /**
     * 判断用户是否订购了某个产品。
     * @param accountName 帐号名称
     * @param productCode 产品代码
     */
    public boolean hasPurchased(String accountName, int productCode) {
    	try {
    		String result = requestBilling("hasPurchased", new String[] { "accountName", "productCode" }, 
    				new String[] { accountName, String.valueOf(productCode) });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
    }
    
    /**
     * 根据帐户ID查询积分。
     */
    public int queryCredit(int id) {
        try {
            String result = requestBilling("queryCredit", new String[] { "id" }, new String[] { String.valueOf(id) });
            return Integer.parseInt(result);
        } catch (Exception e) {
            log.error(e, e);
            return 0;
        }
    }

    /**
     * 根据帐户ID添加积分。
     * @amount 积分
     */
    public int addCredit(int id, int amount) {
        try {
            String result = requestBilling("addCredit", new String[] { "id", "amount" }, new String[] { String.valueOf(id), String.valueOf(amount) });
            return Integer.parseInt(result);
        } catch (Exception e) {
            log.error(e, e);
            return 0;
        }
    }
    
    /**
     * 根据帐户ID和充值金额添加积分。
     * @amount 金额 元
     */
    public int addCreditByMoney(int id, int amount) {
        return addCredit(id, amount * 10);
    }
    
    // 访问计费URL，并得到结果
    private String requestBilling(String cmd, String[] params, String values[]) throws
            Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            // 创建url
            StringBuffer urlBuf = new StringBuffer();
            urlBuf.append(redirectServer);
            urlBuf.append("?cmd=" + cmd);
            for (int i = 0; i < params.length; i++) {
                if (values[i] == null) {
                    continue;
                }
                urlBuf.append("&");
                urlBuf.append(params[i]);
                urlBuf.append("=");
                urlBuf.append(java.net.URLEncoder.encode(values[i], "UTF-8"));
            }

            // 建立连接
            log.info(urlBuf.toString());
            URL url = new URL(urlBuf.toString());
            connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            if (code != 200) {
                throw new Exception("Wrong response code!");
            }

            // 读取结果
            is = connection.getInputStream();
            return new BufferedReader(new InputStreamReader(is, "UTF-8")).
                    readLine();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 访问手机帐户注册URL，并得到结果
     * @return ret : 1.添加成功；2.手机号已存在，密码重置成功；3.添加失败;
     */
    public int requestPhoneReg(String[] params, String values[]) throws
            Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            // 创建url
            StringBuffer urlBuf = new StringBuffer();
            urlBuf.append(redirectServer2);
            urlBuf.append("/phonereg?");
            urlBuf.append("cmd=" + "");
            for (int i = 0; i < params.length; i++) {
                if (values[i] == null) {
                    continue;
                }
                urlBuf.append("&");
                urlBuf.append(params[i]);
                urlBuf.append("=");
                urlBuf.append(java.net.URLEncoder.encode(values[i], "UTF-8"));
            }

            // 建立连接
            log.info(urlBuf.toString());
            URL url = new URL(urlBuf.toString());
            connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            if (code != 200) {
                throw new Exception("Wrong response code!");
            }

	        // 读取结果
	        is = connection.getInputStream();
	        String result = new BufferedReader(new InputStreamReader(is, "UTF-8")).
                    readLine();
            return Integer.parseInt(result.trim());
        } 
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * 修改手机帐户状态
     */
    public boolean requestAccountService(String cmd, String[] params, String values[])
			throws Exception {
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			// 创建url
			StringBuffer urlBuf = new StringBuffer();
			urlBuf.append(redirectServer2);
            urlBuf.append("/backdoor?");
            urlBuf.append("cmd=" + cmd);
			for (int i = 0; i < params.length; i++) {
				if (values[i] == null) {
					continue;
				}
				urlBuf.append("&");
				urlBuf.append(params[i]);
				urlBuf.append("=");
				urlBuf.append(java.net.URLEncoder.encode(values[i], "UTF-8"));
			}

			// 建立连接
			log.info(urlBuf.toString());
			URL url = new URL(urlBuf.toString());
			connection = (HttpURLConnection) url.openConnection();
			int code = connection.getResponseCode();
			if (code != 200) {
				throw new Exception("Wrong response code!");
			}

			// 读取结果
			is = connection.getInputStream();
			String result = new BufferedReader(new InputStreamReader(is,
					"UTF-8")).readLine();
			return result.equals("status ok");
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (Exception e) {
			}
		}
	}
    public static void main(String[] args) {
        Server server = new Server();
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

    /**
     * 通知账单已经完成的接口。此接口用于从客户端直接发起的WAP计费。现在此接口用于临时补费。
     */
    class FeeServlet extends HttpServlet {

        @Override
		protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String s = request.getParameter("FeeID");
            int feeId = Integer.parseInt(s);
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.info("FeeServlet: AddressRejected[" + addr + "] for FeeID[" +
                         feeId + "]");
                return;
            }
            boolean result = fulfillOrder(feeId);
            log.info("FeeID[" + feeId + "]");
            response.setContentType("text/plain;charset=GBK");
            response.setCharacterEncoding("GBK");
            response.getWriter().println(result ? "OK" : "ERROR");
        }

        @Override
		protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }

    /**
     * 小额直接充值接口。
     */
    class DirectFeeServlet extends HttpServlet {
        @Override
		protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
        	String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.info("DirectFeeServlet: AddressRejected[" + addr + "]");
                return;
            }
        	String accountName = request.getParameter("name");
        	int amount = Integer.parseInt(request.getParameter("amount"));
        	String channel = request.getParameter("channel");
        	Fee fee = newFee(accountName, amount, channel);
        	boolean result = false;
        	if (fee != null) {
        		result = fulfillOrder(fee.getId());
        	}
            response.setContentType("text/plain;charset=GBK");
            response.setCharacterEncoding("GBK");
            response.getWriter().println(result ? "OK" : "ERROR");
            log.info("DirectFeeServlet: account[" + accountName + "] amount[" + amount + "] channel[" + 
            		channel + "] result[" + result + "]");
        }

        @Override
		protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }
    
    
    /**
     * 小额直接充值接口(C币)。
     */
    class DirectFee2Servlet extends HttpServlet {
        @Override
		protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
        	String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.info("DirectFee2Servlet: AddressRejected[" + addr + "]");
                return;
            }
        	String accountName = request.getParameter("name");
        	int amount = Integer.parseInt(request.getParameter("amount"));
        	String channel = request.getParameter("channel");
        	Fee fee = newFee(accountName, amount, channel);
        	boolean result = false;
        	if (fee != null) {
        		result = fulfillOrder2(fee.getId());
        	}
            response.setContentType("text/plain;charset=GBK");
            response.setCharacterEncoding("GBK");
            response.getWriter().println(result ? "OK" : "ERROR");
            log.info("DirectFee2Servlet: account[" + accountName + "] amount[" + amount + "] channel[" + 
            		channel + "] result[" + result + "]");
        }

        @Override
		protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }
    
    /**
     * 清除包月记录冲正地址。用于内部。
     */
    class ClearMonthFeeServlet extends HttpServlet {

        @Override
		protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
//            String s = request.getParameter("AccountID");
//            int accountID = Integer.parseInt(s);
//            String addr = request.getRemoteAddr();
//            if (!trustip_directfee.contains(addr)) {
//                log.info("ClearMonthFeeServlet: AddressRejected[" + addr + "] for AccountID[" + accountID + "]");
//                return;
//            }
//            clearMonthFee(accountID);
            response.setStatus(HttpServletResponse.SC_OK);
        }

        @Override
		protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }

    /**
     * 包月记录退订地址。用于内部。
     */
    class UnsubscribeServlet extends HttpServlet {

        @Override
		protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String phone = request.getParameter("phone");
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.info("UnsubscribeServlet: AddressRejected[" + addr + "] for MSISDN[" + phone + "]");
                return;
            }

            // 查找该手机号对应的帐号
            Account acc = findAccountBySubscribePhone(phone, 1);
            boolean result = false;
            if (acc != null) {
            	result = unPurchase(acc.getName(), 1);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        }

        @Override
		protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }

    /**
     * 向手机钱包平台查询账单的接口。
     */
    class QueryReportServlet extends HttpServlet {
        @Override
		public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
            // 得到查询日期
            String queryDate = request.getParameter("date");
            // 产生随机数
            String rdpwd = "" + (Math.round(Math.random() * 800000) + 100000) +
                           "";
            String queryStr = "SPID=" + Const.UNIPAY_SPID + "&REQDATE=" +
                              queryDate + "&RDPWD=" + rdpwd;
            String tmpSign;
            try {
                // 得到签名对象
                tmpSign = SignEnc.sign(queryStr);
            } catch (SignEncException e) {
                log.error(e, e);
                throw new ServletException(e);
            }
            queryStr += "&SIGN=" + java.net.URLEncoder.encode(tmpSign, "UTF-8");
            String redirectURL = Const.UNIPAY_REPORT_URL + "?" + queryStr;
            response.sendRedirect(redirectURL);
        }
    }

    /**
     * 向手机钱包平台查询用户订购关系。
     */
    class QuerySubscribeServlet extends HttpServlet {
        @Override
		public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
        	// 得到手机号
        	String phone = request.getParameter("phone");
            // 产生随机数
            String rdpwd = "" + (Math.round(Math.random() * 800000) + 100000);
            String queryStr = "SPID=" + Const.UNIPAY_SPID + "&GOODSID=555701&MOBILEID=" +
            	phone + "&RDPWD=" + rdpwd;
            String tmpSign;
            try {
                // 得到签名对象
                tmpSign = SignEnc.sign(queryStr);
            } catch (SignEncException e) {
                log.error(e, e);
                throw new ServletException(e);
            }
            queryStr += "&SIGN=" + java.net.URLEncoder.encode(tmpSign, "UTF-8");
            String redirectURL = Const.UNIPAY_QUERY_SUB_URL + "?" + queryStr;
            response.sendRedirect(redirectURL);
        }
    }

    class LimitListServlet extends HttpServlet {
    	@Override
		public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
    		response.setContentType("text/html;encoding=GBK");
    		response.setCharacterEncoding("GBK");
    		PrintWriter out = response.getWriter();
    		out.println("<html><body><table>");
    		out.println("<tr><td>手机号</td><td>续费次数</td></tr>");
    		synchronized (billingCounts) {
    			Iterator itor = billingCounts.keySet().iterator();
    			while (itor.hasNext()) {
    				Object key = itor.next();
    				Object value = billingCounts.get(key);
    				out.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
    			}
    		}
    		out.println("</table></body></html>");
    		out.flush();
    	}
    }

    class LimitAddServlet extends HttpServlet {
    	@Override
		public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
    		String msisdn = request.getParameter("msisdn");
    		if (msisdn != null) {
    			incrementBillingCount(msisdn);
    		}
    		response.setContentType("text/html");
    		PrintWriter out = response.getWriter();
    		out.println("<html><body>OK.</body></html>");
    		out.flush();
    	}
    }

    class LimitDeleteServlet extends HttpServlet {
    	@Override
		public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
    		String msisdn = request.getParameter("msisdn");
    		if (msisdn != null) {
    			synchronized (billingCounts) {
    				billingCounts.remove(msisdn);
    			}
    		}
    		response.setContentType("text/html");
    		PrintWriter out = response.getWriter();
    		out.println("<html><body>OK.</body></html>");
    		out.flush();
    	}
    }
    
    // 检查用户名和密码。
    // Input: name = 用户名，password = 密码
    // Output: 第一行是返回码，0表示成功；如果失败，第二行是错误信息；如果成功，第二行开始分别是用户ID、用户名称、用户余额。
    class CheckLoginServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
            // 验证请求IP
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.warn("Possible attack from [" + addr + "] is rejected.");
                return;
            }
            
            // 取得参数
            String name = request.getParameter("name");
            String password = request.getParameter("password");
            
            // 设置返回格式
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            // 验证用户名和密码
            Account acc = findAccountByName(name);
            if (acc == null || !acc.getPasswordDec().equals(password)) {
                out.println("1");
                out.println("帐号不存在或密码错误");
                return;
            }
            
            // 返回信息
            out.println("0");
            out.println(acc.getId());
            out.println(acc.getName());
            out.println(acc.getBalance().getValue());
        }
    }

    // 根据用户名查找用户ID。
    // Input: name = 用户名
    // Output: 第一行是返回码，0表示成功；如果失败，第二行是错误信息；如果成功，第二行开始分别是用户ID、用户名称。
    class CheckAccountNameServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
            // 验证请求IP
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.warn("Possible attack from [" + addr + "] is rejected.");
                return;
            }
            
            // 取得参数
            String name = request.getParameter("name");
            
            // 设置返回格式
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            // 验证用户名和密码
            Account acc = findAccountByName(name);
            if (acc == null) {
                out.println("1");
                out.println("帐号不存在");
                return;
            }
            
            // 返回信息
            out.println("0");
            out.println(acc.getId());
            out.println(acc.getName());
        }
    }

    // 取得帐户余额的方法。
    // Input: name = 用户名称
    // Output: 第一行是返回码，0表示成功；如果失败，第二行是错误信息；如果成功，第二行是用户余额。
    class GetBalanceServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
            // 验证请求IP
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.warn("Possible attack from [" + addr + "] is rejected.");
                return;
            }
            
            // 取得参数
            String name = request.getParameter("name");
            
            // 设置返回格式
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            // 验证用户名和密码
            Account acc = findAccountByName(name);
            if (acc == null) {
                out.println("1");
                out.println("帐号不存在或密码错误");
                return;
            }
            
            // 返回信息
            out.println("0");
            out.println(acc.getBalance().getValue());
        }
    }
    
    // 取得帐户积分的方法。
    // Input: name = 用户名称
    // Output: 第一行是返回码，0表示成功；如果失败，第二行是错误信息；如果成功，第二行是用户余额。
    class GetCreditServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest request,
                            HttpServletResponse response) throws
                ServletException, IOException {
            // 验证请求IP
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.warn("Possible attack from [" + addr + "] is rejected.");
                return;
            }
            
            // 取得参数
            String name = request.getParameter("name");
            
            // 设置返回格式
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            // 验证用户名和密码
            Account acc = findAccountByName(name);
            if (acc == null) {
                out.println("1");
                out.println("帐号不存在或密码错误");
                return;
            }
            
            // 返回信息
            out.println("0");
            out.println(queryCredit(acc.getId()));
        }
    }
    
    public String wrapCallbackURL(String url) {
    	if (url.startsWith("http://221.179.216.") || url.startsWith("http://10.10.10.")) {
    		try {
	    		return "http://221.179.216.49:8872/proxy/rpx?url=" + 
	    			URLEncoder.encode(url, "UTF-8");
    		} catch (Exception e) {
    			return url;
    		}
    	} else {
    		return url;
    	}
    }
}
