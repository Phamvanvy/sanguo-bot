package peony.service.account.adapter;

import java.io.StringReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * 台湾极致科技（台湾大哥大）Qme帐号系统的支持类。提供帐号注册、帐号登录、扣费等功能。
 * @author lighthu
 */
public class QmeAdapter {
	private static Logger log = Logger.getLogger(QmeAdapter.class);
	private static String IGB_URL = "http://idc2.somuch.com.tw/igb/api/index.php";
	private static String PAY_URL = "http://paygw.somuch.com.tw/qbonus/api/";
	private static String CP_ID = "8";
	private static String CP_ACC = "gameta";
	private static String CP_PWD = "8244d77a246df9b8648228065c942c1d";
	private static Map<Integer, Integer> contentIDMap = new HashMap<Integer, Integer>();
	static {
		contentIDMap.put(50, 75);
		contentIDMap.put(100, 76);
		contentIDMap.put(150, 77);
		contentIDMap.put(250, 78);
		contentIDMap.put(500, 79);
	}
	public static int QB_CONTENT_ID = 123;
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat SEQ_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
	private static AtomicInteger idGen = new AtomicInteger(1);
	
	/**
	 * 注册QME帐号。
	 * @param name 帐号名
	 * @param password 密码
	 * @param email 邮件地址
	 * @return 如果注册成功，返回QME帐号信息。帐号信息中包含有用于激活Q库的SMS信息。
	 * @throws QmeException 如果出错，抛出带消息的异常。
	 */
	public static QmeAccount register(String name, String password, String email) throws QmeException {
		GetMethod method = new GetMethod(IGB_URL);
		method.addRequestHeader( "Connection", "close");
		log.info("[QME_REG]name[" + name + "]password[" + password + "]email[" + email + "]");
		try {
			// 组织参数
			// do = qme
			// ac = join
			// ver = 4.0
			// aid = 帐号名称
			// pwd = 密码
			// email = 邮箱地址
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("do", "qme"));
            params.add(new NameValuePair("ac", "join"));
            params.add(new NameValuePair("ver", "4.0"));
            params.add(new NameValuePair("aid", name));
            params.add(new NameValuePair("pwd", password));
            params.add(new NameValuePair("email", email));
            NameValuePair[] arr = new NameValuePair[params.size()];
            params.toArray(arr);
            method.setQueryString(arr);
            
            // 发起注册请求，返回格式为20100121860,123456,sms://55123;qmeigb.tsi=20100121860
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String result = method.getResponseBodyAsString();
                log.info("[QME_REG]result[" + result.trim() + "]");
                String[] secs = result.trim().split(",");
                if (secs.length < 3) {
                    throw new QmeException("注册时发生系统内部错误");
                }
                int retID = Integer.parseInt(secs[1]);
                if (retID > 0) {
                	// 注册成功
                	QmeAccount acc = new QmeAccount();
                	acc.name = name;
                	acc.qmeID = retID;
                	acc.tsi = secs[0];
                	acc.smsCode = secs[2];
                	return acc;
                } else {
                	// 失败
                	switch (retID) {
                	case -1:
                		throw new QmeException("用户名不合法");
                	case -2:
                		throw new QmeException("包含不允许注册的词语");
                	case -3:
                		throw new QmeException("用户名已经存在");
                	case -4:
                		throw new QmeException("Email格式有误");
                	case -5:
                		throw new QmeException("Email不允许注册");
                	case -6:
                		throw new QmeException("该Email已经被注册");
                	case -7:
                		throw new QmeException("缺少帐号/密码/email必填栏位");
            		default:
                		throw new QmeException("未定义错误");	
                	}
                }
            } else {
                log.info("[QME_REG]code[" + code + "]ERROR");
                throw new QmeException("注册时发生系统内部错误");
            }
		} catch (QmeException qe) {
			throw qe;
        } catch (Exception ex1) {
            log.error(ex1, ex1);
            throw new QmeException("注册时发生系统内部错误");
        } finally {
            method.releaseConnection();
        }
	}
	
	/**
	 * 登录QME帐号。
	 * @param name 帐号名
	 * @param password 密码
	 * @return 如果登录成功，返回QME帐号信息。帐号信息中可能包含激活Q库需要发送的短信。如果已激活Q库，返回帐号信息里应该包含Q库使用者ID。
	 * @throws QmeException 如果出错，抛出带消息的异常。
	 */
	public static QmeAccount login(String name, String password) throws QmeException {
		GetMethod method = new GetMethod(IGB_URL);
		method.addRequestHeader( "Connection", "close");
		log.info("[QME_LOGIN]name[" + name + "]password[" + password + "]");
		try {
			// 组织参数
			// do = tsi
			// ac = sync
			// ver = 4.0
			// qmeacc = 帐号名称
			// qmepwd = 密码
			// mode = qme
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("do", "tsi"));
            params.add(new NameValuePair("ac", "sync"));
            params.add(new NameValuePair("ver", "4.0"));
            params.add(new NameValuePair("qmeacc", name));
            params.add(new NameValuePair("qmepwd", password));
            params.add(new NameValuePair("mode", "qme"));
            NameValuePair[] arr = new NameValuePair[params.size()];
            params.toArray(arr);
            method.setQueryString(arr);
            
            // 发起登录请求，有3种结果：登录成功(已开通Q库), 登录成功(未登录Q库), 登录失败
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String result = method.getResponseBodyAsString();
                log.info("[QME_LOGIN]result[" + result.trim() + "]");
                String[] secs = result.trim().split(",");
                if (secs.length < 4) {
                    throw new QmeException("登入时发生系统内部错误");
                }
                String lastSec = secs[secs.length - 1];
                if (lastSec.startsWith("sms://")) {
                	QmeAccount acc = new QmeAccount();
                	acc.name = name;
                	acc.tsi = secs[2];
                	acc.smsCode = lastSec;
                	return acc;
                } else {
                	int qbID = 0;
                	try {
                		qbID = Integer.parseInt(lastSec);
                	} catch (Exception e) {
                		throw new QmeException(MessageFormat.format("登入时发生错误：{0}", lastSec));
                	}
                	QmeAccount acc = new QmeAccount();
                	acc.name = name;
                	acc.tsi = secs[2];
                	acc.qbID = qbID;
                	return acc;
                }
            } else {
                log.info("[QME_LOGIN]code[" + code + "]ERROR");
                throw new QmeException("登入时发生系统内部错误");
            }
		} catch (QmeException qe) {
			throw qe;
        } catch (Exception ex1) {
            log.error(ex1, ex1);
            throw new QmeException("登入时发生系统内部错误");
        } finally {
            method.releaseConnection();
        }
	}
	
	public static void main(String[] args) throws Exception {
		login("aabbcc", "aabbcc");
	}
	
	/**
	 * 消费Q币。
	 * @param userID 使用者ID
	 * @param contentID 内容ID
	 * @param amount 数量
	 * @throws QmeException 如果出错，抛出带消息的异常。
	 */
	public static void pay(int userID, int amount) throws QmeException {
		if (!contentIDMap.containsKey(amount)) {
			throw new QmeException("非法购买金额");
		}
		int contentID = contentIDMap.get(amount);
		GetMethod method = new GetMethod(PAY_URL);
		method.addRequestHeader( "Connection", "close");
		log.info("[QME_PAY]user[" + userID + "]content[" + contentID + "]amount[" + amount + "]");
		try {
			// 组织参数
			// ap = bonus
			// ac = storedValue
			// ver = 1.0
			// pid = 系统指定CPID
			// acc = 系统指定CP帐号
			// pwd = 系统指定CP密码
			// content_id = 内容ID
			// user_id = 使用者代码
			// result = 交易序列号
			// result_time = 交易时间，格式2009-04-15 13:23:46
			// unit = 商品单位
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("ap", "bonus"));
            params.add(new NameValuePair("ac", "storedValue"));
            params.add(new NameValuePair("ver", "1.0"));
            params.add(new NameValuePair("pid", CP_ID));
            params.add(new NameValuePair("acc", CP_ACC));
            params.add(new NameValuePair("pwd", CP_PWD));
            params.add(new NameValuePair("content_id", String.valueOf(contentID)));
            params.add(new NameValuePair("user_id", String.valueOf(userID)));
            params.add(new NameValuePair("result", getPaySeq()));
            params.add(new NameValuePair("result_time", DATE_FORMAT.format(new Date())));
            params.add(new NameValuePair("unit", "1"));
            NameValuePair[] arr = new NameValuePair[params.size()];
            params.toArray(arr);
            method.setQueryString(arr);
            
            // 发起支付请求，返回xml文档
            int code = httpclient.executeMethod(method);
            if (code == 200) {
            	SAXReader reader = new SAXReader();
                String s = new String(method.getResponseBody(), "UTF-8").trim();
                log.info("[QME_PAY]result[" + s + "]");
                
                Document doc = reader.read(new StringReader(s));
                String retCode = doc.getRootElement().element("OUTPUT").elementText("returnCode");
                String retMsg = doc.getRootElement().element("OUTPUT").elementText("returnMessage");
                if ("100000015".equals(retCode)) {
                	return;
                } else {
                	throw new QmeException(MessageFormat.format("支付时发生错误：{0}", retMsg));
                }
            } else {
                log.info("[QME_PAY]code[" + code + "]ERROR");
                throw new QmeException("支付时发生系统内部错误");
            }
		} catch (QmeException qe) {
			throw qe;
        } catch (Exception ex1) {
            log.error(ex1, ex1);
            throw new QmeException("支付时发生系统内部错误");
        } finally {
            method.releaseConnection();
        }
	}
	
	/**
	 * 查询Q币余额。
	 * @param userID 使用者ID
	 * @throws QmeException 如果出错，抛出带消息的异常。
	 */
	public static int queryBalance(int userID) throws QmeException {
		GetMethod method = new GetMethod(PAY_URL);
		method.addRequestHeader( "Connection", "close");
		log.info("[QME_QUERY_BALANCE]user[" + userID + "]");
		try {
			// 组织参数
			// ap = user
			// ac = ckaccbonus
			// ver = 1.0
			// pid = 系统指定CPID
			// acc = 系统指定CP帐号
			// pwd = 系统指定CP密码
			// user_id = 使用者代码
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("ap", "user"));
            params.add(new NameValuePair("ac", "ckaccbonus"));
            params.add(new NameValuePair("ver", "1.0"));
            params.add(new NameValuePair("pid", CP_ID));
            params.add(new NameValuePair("acc", CP_ACC));
            params.add(new NameValuePair("pwd", CP_PWD));
            params.add(new NameValuePair("user_id", String.valueOf(userID)));
            NameValuePair[] arr = new NameValuePair[params.size()];
            params.toArray(arr);
            method.setQueryString(arr);
            
            // 发起支付请求，返回xml文档
            int code = httpclient.executeMethod(method);
            if (code == 200) {
            	SAXReader reader = new SAXReader();
                String s = new String(method.getResponseBody(), "UTF-8").trim();
                log.info("[QME_QUERY_BALANCE]result[" + s + "]");
                
                Document doc = reader.read(new StringReader(s));
                String retCode = doc.getRootElement().element("OUTPUT").elementText("returnCode");
                if ("100000019".equals(retCode)) {
                	return Integer.parseInt(doc.getRootElement().element("OUTPUT").elementText("bonus")) / 100;
                } else {
                	String retMsg = doc.getRootElement().element("OUTPUT").elementText("returnMessage");
                	throw new QmeException(MessageFormat.format("支付时发生错误：{0}",retMsg));
                }
            } else {
                log.info("[QME_QUERY_BALANCE]code[" + code + "]ERROR");
                throw new QmeException("支付时发生系统内部错误");
            }
		} catch (QmeException qe) {
			throw qe;
        } catch (Exception ex1) {
            log.error(ex1, ex1);
            throw new QmeException("支付时发生系统内部错误");
        } finally {
            method.releaseConnection();
        }
	}
	
	/*
	 * 生成一个唯一的交易序列号。
	 */
	private static String getPaySeq() {
		int id = idGen.getAndIncrement();
		return SEQ_FORMAT.format(new Date()) + id;
	}
}
