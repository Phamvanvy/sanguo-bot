package com.pip.server.billing.yeepay;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
import com.pip.server.billing.chinarund.PayInfo;

/**
 * 易宝支付相关常量。
 * @author lighthu
 */
public class ConstYeepay {
	private static Logger log = Logger.getLogger(ConstYeepay.class);
    public static String CMD = "AnnulCard";
    public static String CARD_TYPE_SZX = "SZX";
    public static String CARD_TYPE_UNICOM = "UNICOM";
    public static String CARD_TYPE_TELECOM = "TELECOM";
    public static String PAY_MODE = "1";

    /** 正式环境 */
    public static String ORDER_URL = "http://www.yeepay.com/app-merchant-proxy/command.action";
    // public static String ORDER_URL = "http://59.151.25.90/app-merchant-proxy/command.action";
    
    /** 一个易宝账号。有一些特殊渠道和特殊版本使用单独的易宝账号。 */
    public static class ChargeAccount {
    	public String id;
    	public String key;
    	public int gameCode;
    	public Pattern channel;
    }
    public static List<ChargeAccount> chargeAccounts;
    
    /** 充值卡金额对应的i币额 */
    public static HashMap<Integer, Integer> IMONEY_MAP;
    
    /** 充值获得双倍的概率 */
    public static double doubleRate = 0.00;
    
    private static long configFileModifyTime;
    
    /**
     * 立刻载入配置。
     * @throws Exception
     */
    public static void loadConfig() throws Exception {
    	SAXReader reader = new SAXReader();
    	File f = new File("yeepay_config.xml");
    	configFileModifyTime = f.lastModified();
		Document doc = reader.read(f);
		Element root = doc.getRootElement();
		
		// 读取易宝账号
		List<ChargeAccount> accounts = new ArrayList<ChargeAccount>();
		Iterator itor1 = root.elementIterator("charge_account");
		while (itor1.hasNext()) {
			Element element = (Element)itor1.next();
			ChargeAccount acc = new ChargeAccount();
			acc.id = element.attributeValue("id");
			acc.key = element.attributeValue("key");
			acc.gameCode = Integer.parseInt(element.attributeValue("gamecode"));
			acc.channel = Pattern.compile(element.attributeValue("channel"), Pattern.DOTALL | Pattern.MULTILINE);
			accounts.add(acc);
		}
		chargeAccounts = accounts;
		
		// 读取费率
		HashMap<Integer, Integer> moneyMap = new HashMap<Integer, Integer>();
		Iterator itor2 = root.elementIterator("charge_rate");
		while (itor2.hasNext()) {
			Element element = (Element) itor2.next();
			int money = Integer.parseInt(element.attributeValue("money"));
			int imoney = Integer.parseInt(element.attributeValue("imoney"));
			moneyMap.put(money, imoney);
		}
		IMONEY_MAP = moneyMap;
		
		// 读取双倍率
		doubleRate = Double.parseDouble(root.elementText("double_rate"));
    }
    
    /**
     * 检查配置是否更新，如有更新立刻重载。
     * @throws Exception
     */
    public static void checkLoadConfig() throws Exception {
    	File f = new File("yeepay_config.xml");
    	if (configFileModifyTime != f.lastModified()) {
    		loadConfig();
    	}
    }
    
    /**
     * 取得推广渠道对应的支付帐号。
     * @param channel
     * @return 支付帐号
     */
    public static String getMerchantID(int gameCode, String channel, Account acc) {
        // 推广渠道处理
        String regChannel = "";
        if (acc != null && acc.getServiceVersion() != null) {
            String[] s = acc.getServiceVersion().split("-");
            if (s.length > 1) {
                regChannel = s[1];
            }
        }
        if (channel == null) {
        	channel = "";
        }
        
        for (ChargeAccount cacc : chargeAccounts) {
        	if (cacc.gameCode != -1 && cacc.gameCode != gameCode) {
        		continue;
        	}
        	if (!cacc.channel.matcher(channel).matches() && !cacc.channel.matcher(regChannel).matches()) {
        		continue;
        	}
        	return cacc.id;
        }
        return null;
    }
    
    /**
     * 得到商户ID对应的交易密钥。
     * @param merchantID
     * @return
     */
    public static String getMerchantKey(String merchantID) {
    	for (ChargeAccount cacc : chargeAccounts) {
    		if (cacc.id.equals(merchantID)) {
    			return cacc.key;
    		}
        }
        return null;
    }

    /**
     * 根据充值金额计算对应i币。
     * @param amount 金额（分）
     * @return i币数量
     */
    public static int calcIMoney(int amount) {
        if (IMONEY_MAP.containsKey(amount)) {
            return IMONEY_MAP.get(amount);
        } else {
            return amount * 36 / 10;
        }
    }
    
    /** 错误码对应的错误信息 */
    public static HashMap<String, String> MESSAGE_MAP = new HashMap<String, String>();
    static {
        MESSAGE_MAP.put("1002", "本张卡密您提交过于频繁，请您稍后再试");
        MESSAGE_MAP.put("1003", "不支持的卡类型");
        MESSAGE_MAP.put("1004", "密码错误或充值卡无效");
        MESSAGE_MAP.put("1005", "卡密错误");
        MESSAGE_MAP.put("1006", "充值卡无效");
        MESSAGE_MAP.put("1007", "卡内余额不足");
        MESSAGE_MAP.put("1008", "余额卡过期(有效期1个月)");
        MESSAGE_MAP.put("1010", "此卡正在处理中");
        MESSAGE_MAP.put("2001", "移动系统或网络问题");
        MESSAGE_MAP.put("2002", "商户风险问题");
        MESSAGE_MAP.put("2003", "未知原因");

        MESSAGE_MAP.put("2005", "此卡已使用");
        MESSAGE_MAP.put("2006", "卡密在系统处理中");
        MESSAGE_MAP.put("2007", "此卡为假卡");
        MESSAGE_MAP.put("2008", "该卡种正在维护");
        MESSAGE_MAP.put("2009", "浙江省移动维护");
        MESSAGE_MAP.put("2010", "江苏省移动维护");
        MESSAGE_MAP.put("2011", "福建省移动维护");
        MESSAGE_MAP.put("2012", "辽宁省移动维护");
        
        MESSAGE_MAP.put("10000", "未知错误");
        MESSAGE_MAP.put("80", "卡号或密码校验失败");
        MESSAGE_MAP.put("7", "卡号卡密或卡面额不符合规范");
    }
    
    /** 根据错误代码获取错误信息 */
    public static String getErrorMessage(String code) {
        String ret = MESSAGE_MAP.get(code.trim());
        if (ret == null) {
            ret = "未知原因";
        }
        return ret;
    }
    
    /**
     * 1天内提交过请求的卡号和密码的组合。
     */
    private static HashMap<String, Integer> submitRecords = new HashMap<String, Integer>();
    /**
     * 1天内提交过请求的卡号的组合。
     */
    private static HashMap<String, Integer> submitCards = new HashMap<String, Integer>();
    /**
     * 一天内每个用户的错误次数。
     */
    private static HashMap<Integer, Integer> errorCounts = new HashMap<Integer, Integer>();
    /**
     * 上次清除记录的时间。
     */
    private static long lastClearRecordTime = System.currentTimeMillis();
    /**
     * 上次成功提交时间。
     */
    private static long lastSuccSubmitTime = System.currentTimeMillis();
    /**
     * 上次收到通知时间。
     */
    private static long lastSuccNotifyTime = System.currentTimeMillis();
    /**
     * 10分钟内连续提交错误次数。
     */
    private static int failSubmitTimes = 0;
    /**
     * 上次提交错误时间。
     */
    private static long lastFailSubmitTime = System.currentTimeMillis();
    
    /**
     * 检查一个卡号和密码是否允许提交。同一卡号和密码一天只能提交3次。
     * @param cardno
     * @param cardpass
     * @return
     */
    public static boolean checkSubmitPermission(int accountId, String cardno, String cardpass) {
    	synchronized (submitRecords) {
    		// 每天清除一次
    		if (System.currentTimeMillis() > lastClearRecordTime + 86400000L) {
    			submitRecords.clear();
    			submitCards.clear();
    			errorCounts.clear();
    			lastClearRecordTime = System.currentTimeMillis();
    		}
    		
    		// 检查是否到3次，单卡不能超过6次，单用户不能超过15次错误，如果没有，添加计数并返回true
    		String key = cardno + "\n" + cardpass;
    		Integer oldValue = submitRecords.get(key);
    		if (oldValue == null) {
    			oldValue = new Integer(0);
    		}
    		Integer oldValue2 = submitCards.get(cardno);
    		if (oldValue2 == null) {
    			oldValue2 = new Integer(0);
    		}
    		Integer oldValue3 = errorCounts.get(accountId);
    		if (oldValue3 == null) {
    		    oldValue3 = new Integer(0);
    		}
			return (oldValue.intValue() < 3 && oldValue2.intValue() < 6 && oldValue3.intValue() < 15);
    	}
    }
    
    /**
     * 记录某个卡成功提交一次。
     */
    public static void addSubmitRecord(String cardno, String cardpass) {
        String key = cardno + "\n" + cardpass;
        Integer oldValue = submitRecords.get(key);
        if (oldValue == null) {
            oldValue = new Integer(0);
        }
        Integer oldValue2 = submitCards.get(cardno);
        if (oldValue2 == null) {
            oldValue2 = new Integer(0);
        }
        submitRecords.put(key, oldValue.intValue() + 1);
        submitCards.put(cardno, oldValue2.intValue() + 1);
        
        lastSuccSubmitTime = System.currentTimeMillis();
        if (lastFailSubmitTime < lastSuccSubmitTime - 600000L) {
    		failSubmitTimes = 0;
    	}
    }
    
    /**
     * 记录一次下单失败（访问易宝平台失败）。
     */
    public static void recordSubmitFail() {
    	if (lastFailSubmitTime < System.currentTimeMillis() - 600000L) {
    		failSubmitTimes = 1;
    	} else {
    		failSubmitTimes++;
    	}
    	lastFailSubmitTime = System.currentTimeMillis();
    }
    
    /**
     * 记录一次成功从易宝收到的通知。
     */
    public static void recordNotify() {
    	lastSuccNotifyTime = System.currentTimeMillis();
    }
    
    /**
     * 检查易宝接口状态是否健康。
     * 不健康的判断标准是：
     * 10分钟内没有成功下单（1-2点以及7-8点之间放宽到20分钟，2-7点之间放宽为40分钟）。
     * 10分钟内没有收到通知（1-2点以及7-8点之间放宽到20分钟，2-7点之间放宽为40分钟）。
     * 10分钟内下单错误次数达到或超过2次。
     * @return
     */
    public static boolean isHealthy() {
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(System.currentTimeMillis());
    	int hour = cal.get(Calendar.HOUR_OF_DAY);
    	long timeValve = 600000L;
    	if (hour >= 1 && hour < 2) {
    		timeValve *= 2;
    	} else if (hour >= 7 && hour < 8) {
    		timeValve *= 2;
    	} else if (hour >= 2 && hour < 7) {
    		timeValve *= 4;
    	}
    	if (System.currentTimeMillis() - lastSuccSubmitTime > timeValve) {
    		return false;
    	}
    	if (System.currentTimeMillis() - lastSuccNotifyTime > timeValve) {
    		return false;
    	}
    	if (failSubmitTimes >= 2) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * 检查卡号和密码是否合法。
     * 目前支持的卡号和密码都必须全部是数字，可用的位数包括：
     * 全国移动充值卡	17	18
     * 全国联通一卡充	15	19
     * 全国电信卡	19	18
     * 福建移动呱呱通充值卡	16	17
     * 江苏移动充值卡	16	17
     * 辽宁移动电话交费卡	16	21
     * 浙江移动缴费券	10	8
     * @param cardno 卡号
     * @param cardpass 密码
     * @return 如果不合法，返回false。
     */
    public static boolean checkInput(String cardno, String cardpass) {
    	if (isDigit(cardno) && isDigit(cardpass)) {
    		int cl = cardno.length();
    		int cp = cardpass.length();
    		if (cl == 17 && cp == 18) {
    			return true;
    		}
    		if (cl == 15 && cp == 19) {
    			return true;
    		}
    		if (cl == 19 && cp == 18) {
    			return true;
    		}
    		if (cl == 16 && cp == 17) {
    			return true;
    		}
    		if (cl == 16 && cp == 21) {
    			return true;
    		}
    		if (cl == 10 && cp == 8) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /*
     * 判断一个字符串是否全是数字。
     */
    private static boolean isDigit(String str) {
    	for (int i = str.length() - 1; i >= 0; i--) {
    		char ch = str.charAt(i);
    		if (ch < '0' || ch > '9') {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * 记录一个用户计费失败一次。一天超过20次失败就不能再提交请求了。
     * @param accountId
     * @return
     */
    public static void addFailRecord(int accountId) {
        synchronized (submitRecords) {
            Integer oldValue3 = errorCounts.get(accountId);
            if (oldValue3 == null) {
                oldValue3 = new Integer(0);
            }
            errorCounts.put(accountId, oldValue3.intValue() + 1);
        }
    }
    
    /**
     * 随机送礼活动。
     * @param pinfo
     * @return
     */
    public static Random randSeed = new Random(System.currentTimeMillis());
    public static float randomGiftRatio(Server server, PayInfo pinfo) {
    	int rand = randSeed.nextInt(10000);
    	if (rand < doubleRate * 10000) {
    		return 2.0f;
    	}
    	return 1.0f;
    }
}
