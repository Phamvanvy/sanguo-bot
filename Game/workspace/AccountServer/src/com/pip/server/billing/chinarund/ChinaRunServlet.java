package com.pip.server.billing.chinarund;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Account;
import com.pip.server.account.util.DES;
import com.pip.server.billing.Server;

public class ChinaRunServlet extends HttpServlet {
	
	private static final String VERSION = "3";
	private static final String CHINARUN_URL = "http://pay3.shenzhoufu.com/interface/version3/serverconnszx/entry.aspx";
	private static final String BUSINESSID = "123844";
	private static final String DES_KEY = "6aFhsJvyxxY=";
	private static final String VERIFY_TYPE = "1";
	private static final String MD5_SUFFIX = "112233";
	
	private String cb_callback = "http://218.206.80.188:8102/chinaruncb";
	
	private static final Random RND = new Random();
	protected Cipher en_cipher = null;
    private final MessageDigest md5 = MessageDigest.getInstance( "MD5" );
	private static Logger log = Logger.getLogger(ChinaRunServlet.class);
	
    private static final byte[] highDigits;

    private static final byte[] lowDigits;
    
    private final SimpleDateFormat sf = new SimpleDateFormat("HHmmss");
    private final SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMdd");
    
    private final SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static{
        final byte[] digits = {
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

        int i;
        byte[] high = new byte[256];
        byte[] low = new byte[256];

        for(i = 0; i < 256; i++){
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 0x0F];
        }

        highDigits = high;
        lowDigits = low;
    }
	
    protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    protected PayInfoDAO dao;
    protected Server server;
    
    private Map<String,String> callbacks;
    
    
	public ChinaRunServlet(Server server, PayInfoDAO dao,Map<String,String> callbacks,String cb_callback) throws Exception{
		this.server = server;
		DESKeySpec dks = new DESKeySpec(DES_KEY.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		en_cipher = Cipher.getInstance("DES");
		en_cipher.init(Cipher.ENCRYPT_MODE, securekey);
		this.dao = dao;
		this.callbacks = callbacks;

		if(cb_callback!=null){
			this.cb_callback = cb_callback;
		}
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
		String sId = request.getParameter("id");
		String name = request.getParameter("name");
		int id = -1;
		try {
			id = Integer.parseInt(sId);
		} catch (Exception e) {
		}
		if (id == -1) {
			Account acc = server.findAccountByName(name);
			if (acc == null) {
				response.setHeader("result", "1002");
				return;
			} else {
				id = acc.getId();
			}
		}
		String cardno = request.getParameter("cardsn");
		String password = request.getParameter("password");
		int money = Integer.parseInt(request.getParameter("money"));
		String gameString = request.getParameter("game");
		int gameId = 1;
		if (gameString != null) {
			gameId = Integer.parseInt(gameString);
		}
		String channel = request.getParameter("channel");
		String callback = request.getParameter("returnhttp");
		log.info("id["+id+"]name["+name+"]cardsn["+cardno+"]password["+password+"]money["+money+"]callback["+callback+"]game["+gameId+"]");
		String orderId = getOrderId();
		boolean insertok = false;
		Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
		try{
			PayInfo pi = new PayInfo();
			pi.setAccountId(id);
			pi.setAddIFail(true);
			pi.setGame(gameId); //1 幻想 2武林
 			pi.setI_sum(0);
			pi.setMoney(money+"");
			pi.setPayId(orderId);
			pi.setPayTime(sf1.format(new Date()));
			pi.setUserName(name);
			pi.setValid(false);
			pi.setChannel(channel);
			pi.setCardno(cardno);
			pi.setCardpass(password);
			dao.create(pi);
			tx.commit();
			log.info("payinfo created");
			insertok = true;
		}catch(Exception ex){
			log.error(ex,ex);
			tx.rollback();
		}
		if (insertok) {
			PostMethod method = new PostMethod(CHINARUN_URL);
			method.addRequestHeader( "Connection", "close");

			try {
				StringRequestEntity entity = new StringRequestEntity(
						getChargeString(orderId, id, cardno, password, money),
						"text/xml", "utf-8");
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(
						10000);
				httpclient.getParams().setSoTimeout(30000);
				method.setRequestEntity(entity);
				int code = httpclient.executeMethod(method);
				log.info("code:" + code);
				if (code == 200) {
					Header header = method.getResponseHeader("szfResponseCode");
					log.info("result:"+header.getValue());
					response.setHeader("result", header.getValue());
					response.setHeader("orderId", orderId);
					response.setStatus(HttpServletResponse.SC_OK);
					log.info("callbackurl:"+callback);
					callbacks.put(orderId, callback);
				}else{
					response.setHeader("result", "1001");
					response.setHeader("orderId", orderId);
				}
				
			} catch (Exception ex1) {
				log.error(ex1, ex1);
			} finally {
				method.releaseConnection();
			}
		}
        
	}

//	<?xml version="1.0" encoding="utf-8"?>
//	<message>
//	<version>版本号</version>
//	<merId>商户ID</merId>
//	<payMoney>支付金额[单位：分]</payMoney>
//	<orderId>订单号</orderId>
//	<returnUrl>服务器返回地址 </ returnUrl >
//	<cardInfo>充值卡加密信息 — 面值[单位：元]@序列号@密码  DES加密并作BASE64后的数据</cardInfo >
//	<merUserName>商户的用户姓名</merUserName >
//	<merUserMail>商户的用户邮箱</merUserMail>
//	<privateField>商户私有数据</privateField >
//	<verifyType>数据校验方式</verifyType>
//	<md5String>MD5(version +merId+ payMoney + orderId + returnUrl + cardInfo + privateField + verifyType+privateKey) </md5String>
//	<signString >证书签名(对md5加密后的32位字符串 进行签名)</ signString >
//	</message>

	
	private String getChargeString(String orderId,int id,String cardno,String password,int money) throws Exception{
		StringBuilder sb = new StringBuilder(500);
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<message>");
		sb.append("<version>");
		sb.append(VERSION);
		sb.append("</version>");
		sb.append("<merId>");
		sb.append(BUSINESSID);
		sb.append("</merId>");
		sb.append("<payMoney>");
		sb.append(money*100);
		sb.append("</payMoney>");
		sb.append("<orderId>");
		sb.append(orderId);
		sb.append("</orderId>");
		sb.append("<returnUrl>");
		sb.append(cb_callback);
		sb.append("</returnUrl>");
		sb.append("<cardInfo>");
		String cardinfo = getCardInfo(cardno,password,money);
		sb.append(cardinfo);
		sb.append("</cardInfo>");		
		sb.append("<merUserName>");
		sb.append(id);
		sb.append("</merUserName>");
	    sb.append("<merUserMail>");
	    sb.append(id + "@pipgame.cn");
	    sb.append("</merUserMail>");
	    sb.append("<privateField>");
	    sb.append(id);
	    sb.append("</privateField>");
		sb.append("<verifyType>");
		sb.append(VERIFY_TYPE); //md5校验方式
		sb.append("</verifyType>");
		sb.append("<cardTypeCombine>0</cardTypeCombine>");
		sb.append("<md5String>");
		sb.append(getDigest(VERSION,BUSINESSID,money*100,orderId,cb_callback,cardinfo,""+id,VERIFY_TYPE,MD5_SUFFIX));
		sb.append("</md5String>");
		sb.append("</message>");
		log.info("chargestring:"+sb.toString());
		return sb.toString();
	}

    public static String getHexString(byte[] in){
        StringBuilder out = new StringBuilder((in.length * 2));

        for(int i = 0; i < in.length; i++){
            int byteValue = in[i] & 0xFF;
            out.append((char)highDigits[byteValue]);
            out.append((char)lowDigits[byteValue]);
        }
        return out.toString();
    }
//    version +merId+ payMoney + orderId + returnUrl + cardInfo + privateField + verifyType+privateKey
	private String getDigest(String version, String merId, int payMoney,
			String orderId, String returnUrl, String cardInfo,
			String privateField, String verifyType, String privateKey)
			throws Exception {
		byte[] bs = md5
				.digest((version + merId + payMoney + orderId + returnUrl
						+ cardInfo + privateField + verifyType + privateKey)
						.getBytes());
		return getHexString(bs);
	}
	
	private String getOrderId(){
		Date current = new Date();
		return sf2.format(current)+"-"+BUSINESSID+"-"+sf.format(current)+RND.nextInt(9999);
	}
	
	private String getCardInfo(String cardno,String password,int money) throws Exception{
		String s = money+"@"+cardno+"@"+password;
		return DES.encode(s, DES_KEY);
//		byte[] bs = en_cipher.doFinal(s.getBytes());
//		return (new sun.misc.BASE64Encoder()).encodeBuffer(bs);
	}
	
}
