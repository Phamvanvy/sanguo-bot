package com.pip.itimes.server.billing;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.pip.itimes.net.JettyServer;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.auth.*;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.bean.Fee;
import com.umpay.SignEnc;
import com.umpay.SignEncException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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

    // 信任IP
    private Set trustip_directfee = new HashSet();
    private Set trustip_wapfeeold = new HashSet();
    private Set trustip_wapfee = new HashSet();

    // WAP支付黑名单管理
    private HashMap wapBlackList = new HashMap();
    private long wapBlackListFileTime = 0;

    // 支付黑名单管理
    private HashMap blackList = new HashMap();
    private long blackListFileTime = 0;

    // 支付次数管理
    private HashMap billingCounts = new HashMap();
    private long lastAccessTime = System.currentTimeMillis();


    public Server() {
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
        Collections.addAll(trustip_directfee,
                           configuration.getStringArray("trustip_directfee"));
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
        httpServer.addServlet("/FeeOld", new FeeServlet());
        httpServer.addServlet("/ClearMonthFee", new ClearMonthFeeServlet());
        httpServer.addServlet("/Unsubscribe", new UnsubscribeServlet());

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
        httpServer.addServlet("/sendOrderWAP_umpay", new SendOrderWapServlet(this));
        httpServer.addServlet("/queryReport", new QueryReportServlet());
        httpServer.addServlet("/querySubscribe", new QuerySubscribeServlet());

        // 计费限制管理接口
        httpServer.addServlet("/limit_list", new LimitListServlet());
        httpServer.addServlet("/limit_add", new LimitAddServlet());
        httpServer.addServlet("/limit_delete", new LimitDeleteServlet());


        httpServer.start();
    }

    // 载入支付黑名单，缓存到内存，如果文件改变，则重新载入
    synchronized void loadBlackList() {
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
    boolean inBlackList(String phone) {
        return blackList.containsKey(phone);
    }

    // 载入支付黑名单，缓存到内存，如果文件改变，则重新载入
    synchronized void loadWapBlackList() {
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
    boolean inWapBlackList(String mid) {
        return wapBlackList.containsKey(mid);
    }

    // 增加一个用户的计费次数
    void incrementBillingCount(String msisdn) {
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
    boolean isTrustWapFeeIP(String ip) {
    	return trustip_wapfee.contains(ip);
    }

    // 取得一个用户的计费次数
    int getBillingCount(String msisdn) {
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
     * 完成一个账单。如果在创建账单时没有提供金额和渠道号，则这里必须提供。即使创建账单时提供了金额和渠道号 ，
     * 如果这里也提供新的数据，可覆盖原设定。
     * @param feeId 账单ID
     * @param iMoney 续费金额，单位为i币*100，0表示使用账单创建时的金额
     * @param channel 渠道ID，参见Const类，null或空串表示使用账单创建时的渠道
     * @return 如果账单处理成功，返回续费的帐户名称，失败返回null。失败的原因可能是：账单已完成，账单不存在，金额未提供等等。
     */
    String fee(int feeId, int iMoney, String channel) {
        Fee fee = findFee(feeId);

        // 检查账单是否存在或有效
        if (fee == null || fee.isCharged()) {
        	return null;
        }

        // 检查金额和渠道是否完成了
        if (iMoney != 0) {
        	fee.setAmount(iMoney);
        }
        if (channel != null && channel.length() > 0) {
        	fee.setChannel(channel);
        }
        if (fee.getAmount() == 0 || fee.getChannel() == null || fee.getChannel().length() == 0) {
        	return null;
        }

        // 账单有效，完成订单，修改帐户余额
        Account a = findAccount(fee.getAccountId());
		if (a != null && a.getValid() == false && "注册".equals(a.getCause()) && Const.UNIPAY_REG.equals(fee.getChannel())) {
			// 如果账户是新注册的且没有续费成功，对此账户续费2元产生的效果是激活账户，并返回账户密码
			a.setValid(true);
			a.setiMoney(fee.getAmount());
			fee.setCharged(true);
			fee.setFinishTime(new Date());
			updateAccount(a, fee);
			log.info("AccountID[" + a.getId() + "]FeeID[" + fee.getId() + "]Actived");
			return "*" + a.getUserName() + "$$" + a.getPassword();
		} else if (a != null) {
			if (fee.getAmount() == Const.MONTH_MAX) {
				// 包月
				log.info("AccountID[" + a.getId() + "]MonthFee[" + a.getMonthFee() + "]");
				a.setiMoney(0);
				a.setMonthFee(a.getMonthFee() + Const.MONTH_MAX);
				a.setLastBillingTime(new Date());
			} else {
				// 单次
				log.info("AccountID[" + a.getId() + "]FeeID[" + fee.getId() + "]Fee["
						+ fee.getAmount() + "]iMoney[" + a.getiMoney() + "]");
				a.setiMoney(fee.getAmount());
			}
            fee.setCharged(true);
			fee.setFinishTime(new Date());
			updateAccount(a, fee);
			return a.getUserName();
		} else {
			return null;
		}
	}

    /**
     * 根据帐户名字创建一条新的计费账单。对于预先知道续费渠道的账单，生成账单时提供金额和渠道号，完成账单时不需要再提供。
     * 对于不知道续费渠道的账单，生成账单时可先不提供这两个数据，但在完成账单时必须提供。
     * @param name 计费帐户名
     * @param amount 账单金额（单位为i币*100），0表示不确定
     * @param channel 渠道ID，参见Const类，null或空串表示不确定
     * @return 新创建的账单ID，-1表示系统错误，-2表示用户余额过多，暂时不允许冲值
     */
    private int prepareFee(String name, int amount, String channel) {
        if (name == null || name.length() == 0) {
			return -1;
        }
        Account a = findAccountByName(name);
		if (a == null) {
			return -1;
		}
		if (a.getiMoney() >= ((Integer)Const.BALANCE_MAX.get(channel)).intValue()) {
			return -2;
		}

		// 如果是包月产品，而用户本月已经消费够了，则不让充值
		if (amount == Const.MONTH_MAX && a.getLastBillingTime() != null && a.getMonthFee() >= Const.MONTH_MAX) {
			Date lastBillingTime = a.getLastBillingTime();
			Date currentTime = new Date();
			if (Const.inLaterMonth(lastBillingTime, currentTime)) {
				return -2;
			}
		}
		if (amount == Const.MONTH_MAX && a.getSubscribeStatus() == Account.SUBSCRIBED) {
			return -2;
		}

		Fee fee = newFee(a.getId(), amount, channel);
		if (fee != null) {
			return fee.getId();
		}
		return -1;
    }

    /**
     * 清除帐户当月包月数据。
     */
    private void clearMonthFee(int id) {
    	Account a = findAccount(id);
    	if (a != null) {
    		a.setMonthFee(a.getMonthFee() - Const.MONTH_MAX);
    		a.setiMoney(0);
    		updateAccount(a, null);
    	}
    }

    /**
     * 根据帐户ID查找帐户对象，如果帐户不存在，返回null。
     */
    Account findAccount(int id) {
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
    Account findAccountByName(String name) {
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
    Account findAccountBySubscribePhone(String phone) {
        try {
			String result = requestBilling("findAccountBySubscribePhone", new String[] { "phone" }, new String[] { phone });
			return (Account)Const.stringToObject(result);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据ID查找账单对象，如果账单不存在，返回null。
     */
    Fee findFee(int id) {
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
    Fee findLatestFee(String channel) {
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
     * @param accountId 计费帐户ID
     * @param amount 账单金额（单位为i币*100），0表示不确定
     * @param channel 渠道ID，参见Const类，null或空串表示不确定
     * @return 新创建的账单对象
     */
    Fee newFee(int accountId, int amount, String channel) {
    	try {
    		String result = requestBilling("newFee", new String[] { "accountId", "amount", "channel" },
    				new String[] { String.valueOf(accountId), String.valueOf(amount), String.valueOf(channel) });
			return (Fee)Const.stringToObject(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return null;
    	}
    }

    /**
     * 更新账户对象，其中，iMoney部分提供的是修改值。newFee对象可以为null。
     */
    boolean updateAccount(Account newAcc, Fee newFee) {
    	try {
    		String result = requestBilling("updateAccount", new String[] { "newAcc", "newFee" },
    				new String[] { Const.objectToString(newAcc), Const.objectToString(newFee) });
    		return "1".equals(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return false;
    	}
    }

    /**
     * 查找某一个渠道本月的消费总额。
     */
    int getMonthPayment(String channel) {
    	try {
    		String result = requestBilling("getMonthPayment", new String[] { "channel" }, new String[] { channel });
    		return Integer.parseInt(result);
    	} catch (Exception e) {
    		log.error(e, e);
    		return 0;
    	}
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
            if (is != null) {
                is.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 填写URL中未完成的主机名：端口部分
    private String fillURL(String url) {
        url = url.replaceAll("<host>", host);
        url = url.replaceAll("<port>", String.valueOf(port));
        return url;
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
            String result = fee(feeId, 0, null);
            log.info("FeeID[" + feeId + "]");
            response.setContentType("text/plain;charset=GBK");
            response.setCharacterEncoding("GBK");
            response.getWriter().println(result);
        }

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

        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String s = request.getParameter("AccountID");
            int accountID = Integer.parseInt(s);
            String addr = request.getRemoteAddr();
            if (!trustip_directfee.contains(addr)) {
                log.info("ClearMonthFeeServlet: AddressRejected[" + addr + "] for AccountID[" + accountID + "]");
                return;
            }
            clearMonthFee(accountID);
            response.setStatus(HttpServletResponse.SC_OK);
        }

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
            Account acc = findAccountBySubscribePhone(phone);
            if (acc != null) {
            	acc.setSubscribeStatus(Account.NO_SUBSCRIBE);
            	acc.setiMoney(0);
                updateAccount(acc, null);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        }

        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }

    /**
     * 创建账单接口。用于联丰WAP续费方式和新浪WAP续费老方式。
     */
    class WapFeeServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String s = request.getParameter("account");
            String s1 = request.getParameter("account2");
            String channel = request.getParameter("channel");
            log.info("account[" + s + "]");
            log.info("account[" + s1 + "]");
            log.info("channel[" + channel + "]");
            if (s == null || s1 == null || channel == null) {
                response.sendRedirect(Const.PORTAL_REDIRECT + Const.INPUT_ERROR);
                return;
            }
            if (!s.equals(s1)) {
                response.sendRedirect(Const.PORTAL_REDIRECT +
                                      Const.ACCOUNT_INPUT_ERROR);
                return;
            }
            if (!channel.equals("wap") && !channel.equals("wapsina")) {
                response.sendRedirect(Const.PORTAL_REDIRECT + Const.INPUT_ERROR);
                return;
            }
            int feeId;
            if (channel.equals("wap")) {
                feeId = prepareFee(s, Const.LINKRICH_WAP_FEE, Const.LINKRICH);
            } else {
                feeId = prepareFee(s, Const.SINA_WAP_FEE, Const.SINA);
            }
            if (feeId > 0) {
                log.info("WapFee[" + feeId + "]");
                if (channel.equals("wap"))
                    response.sendRedirect(fillURL(Const.LINKRICH_WAP_FEE_URL +
                                                  feeId));
                else
                    response.sendRedirect(fillURL(Const.SINA_WAP_FEE_PRE_URL +
                                                  feeId));
            } else {
                if (feeId == -1)
                    response.sendRedirect(Const.PORTAL_REDIRECT +
                                          Const.ACCOUNT_NOT_FOUND);
                else if (feeId == -2)
                    response.sendRedirect(Const.PORTAL_REDIRECT +
                                          Const.NOT_ALLOWED);
            }
        }

        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }


    /**
     * 账单完成通知接口。用于联丰WAP续费方式和新浪WAP续费老方式。对此接口的请求必须从MISC平台的WTBS发出，其他来源的请求一律拒绝。
     */
    class WapFeeOkServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String address = request.getRemoteAddr();
            if (!trustip_wapfeeold.contains(address)) {
                log.info("WapFeeOkServlet: AddressRejected[" + address + "]");
                return;
            }
            String s = request.getParameter("FeeID");
            int feeId = Integer.parseInt(s);
            String MID = request.getParameter("MISC_MID");
            String ServiceID = request.getParameter("MISC_ServiceID");
            String SessionID = request.getParameter("MISC_SessionID");
            log.info("MID[" + MID + "]");
            log.info("ServiceID[" + ServiceID + "]");
            log.info("SessionID[" + SessionID + "]");

            if (MID == null || ServiceID == null || SessionID == null) {
                log.info("FeeID[" + feeId + "]Parameter Error");
                return;
            }
            String result = fee(feeId, 0, null);
            if (result != null) {
                log.info("WapFeeOk[" + feeId + "]");
                response.sendRedirect(Const.PORTAL_REDIRECT + Const.OK);
            } else {
                log.info("WapFeeFail[" + feeId + "]");
                response.sendRedirect(Const.PORTAL_REDIRECT +
                                      Const.BILL_NOT_FOUND);
            }
        }

        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }


    /**
     * 续费过程中间通知接口。用于新浪WAP续费老方式。因为新浪要求在访问反向订购地址前必须访问一个统计地址，所以被迫加上这一条。
     */
    class WapFeePreServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String s = request.getParameter("FeeID");
            response.sendRedirect(fillURL(Const.SINA_WAP_FEE_URL + s));
        }

        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }


    /**
     * 创建账单接口。用于新浪WAP续费新方式。
     */
    class WapFeeServlet1 extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String s = request.getParameter("account");
            String s1 = request.getParameter("account2");
            String channel = request.getParameter("channel");
            log.info("account[" + s + "]");
            log.info("account[" + s1 + "]");
            log.info("channel[" + channel + "]");
            // 暂停此渠道
//            if (true) {
//            	response.sendRedirect(Const.PORTAL_REDIRECT + Const.NOT_ALLOWED);
//            	return;
//            }
            if (s == null || s1 == null || channel == null) {
                response.sendRedirect(Const.PORTAL_REDIRECT + Const.INPUT_ERROR);
                return;
            }
            if (!s.equals(s1)) {
                response.sendRedirect(Const.PORTAL_REDIRECT +
                                      Const.ACCOUNT_INPUT_ERROR);
                return;
            }
            if (!channel.equals("wapsina")) {
                response.sendRedirect(Const.PORTAL_REDIRECT + Const.INPUT_ERROR);
                return;
            }
            int feeId = prepareFee(s, Const.SINA_WAP_FEE, Const.SINA);
            if (feeId > 0) {
                log.info("WapFee[" + feeId + "]");
                response.sendRedirect(fillURL(Const.SINA_WAP_FEE_PRE_URL_NEW +
                                              feeId));
            } else {
                if (feeId == -1)
                    response.sendRedirect(Const.PORTAL_REDIRECT +
                                          Const.ACCOUNT_NOT_FOUND);
                else if (feeId == -2)
                    response.sendRedirect(Const.PORTAL_REDIRECT +
                                          Const.NOT_ALLOWED);
            }
        }

        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws
                ServletException,
                IOException {
            doPost(request, response);
        }
    }

    /**
     * 续费过程中间通知接口。用于新浪WAP续费老方式。因为新浪要求在访问反向订购地址前必须访问一个统计地址，所以被迫加上这一条。
     */
    class WapFeePreServlet1 extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
                ServletException,
                IOException {
            String s = request.getParameter("FeeID");
            response.sendRedirect(fillURL(Const.SINA_WAP_FEE_URL_NEW + s));
        }

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
}
