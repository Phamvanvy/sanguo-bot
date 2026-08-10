package pip.gm.fw;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

import pip.gm.MainAppRES;
import pip.util.XmlConfig;

/**
 * 基础的配置,
 */
public class BaseConfig extends XmlConfig {
	public static final int BRANCH_PIP = 0; // 明珠客服
	public static final int BRANCH_TW = 1;  // 台哥大、极致 明珠三国
	public static final int BRANCH_5DING = 2; // 畅游互动 网页三国
	public static final int BRANCH_GameFlier = 3; // 游戏新干线 网页三国
	public static final int BRANCH_MY = 4; // 马来西亚 网页三国
	public static final int BRANCH_VIETNAM_TinhVan = 5; // 越南 TinhVan Java 版三国合作
	public static final int BRANCH_360 = 6; // 360平台 网页三国
	public static final int BRANCH_US = 7; // 美国 网页三国
	public static final int BRANCH_JP = 8; // 日本 网页三国
	public static final int BRANCH_XD = 9; // 星蝶平台
	public static final int BRANCH_SHUWEI = 10; //蜀魏平台
	public static final int BRANCH_KAIXIN = 11; //开心平台
	public static final int BRANCH_RENREN = 12; //人人平台
	public static final int BRANCH_DAYOU = 13; //达游平台
	public static final int BRANCH_FUYUN = 14; //浮云平台
	public static final int BRANCH_QIDIAN = 16; //起点平台
	public static final int BRANCH_KOREA_MOBILE = 17; //韩文版手机三国合作
	public static final int BRANCH_JAPAN_MOBILE = 18; //日文版手机三国合作
	public static final int BRANCH_I8 = 19; //I8平台
	public static final int BRANCH_TH = 20; //泰国
	public static final int BRANCH_SKY = 21; //斯凯侠客行
	public static final int BRANCH_CMCC = 22; //移动平台网页三国

	/** 如果是从Brach创建的版本，把CVS_BRANCH变量设置成下列的值。后续根据项目进度自行添加*/
	public static final int FixVersion_head = Integer.MAX_VALUE;
	public static final int FixVersion_2011_07_26 = 20110726; 
	public static final int FixVersion_2011_10_25 = 20111025;
	public static final int FixVersion_2012_05_29 = 20120529;
	public static final int FixVersion_2012_06_26 = 20120626;
	public static final int FixVersion_2012_07_10 = 20120710;
	
	//cvs的Brach版本
	public static int CVS_BRANCH = FixVersion_head;
	//Revision版本
	public static final int BRANCH = BRANCH_PIP;
	//域
	public static String DOMAIN = "pip";
	
	public static final String DOMAIN_PIP = "pip";
	public static final String DOMAIN_JAPAN_MOBILE = "sanguo_jp";
	
	public static final String configFileDirName = "gm";
	/** 工具的版本号 BRANCH前面数字含义是MMddHH，  */
	public static String APP_VERSION = "8.2.101714" + DOMAIN; 
	/** 主机地址 */
	public String host =  
	// "124.65.152.118"; 
	 "192.168.0.54";
	/** 端口 */
	public int port = 2259;
	/** 代理参数 */
	public long sharpId;
	/** 登录帐号 */
	public String account = ""; 
	/** 登录密码 */
	public String password = "";
	/** 日志文件 */
	public String log = null;
	public OutputStream out;
	/** 游戏类型,决定游戏启动的引擎 */
	public String game = "FIT";
	/** 游戏代码，用来分类 */
	public String gameGroup = MainAppRES.unGrouped;
	/** 显示的游戏服务器名称 */
	public String title = null;
	/** 服务器协议版本 */
	public int clientVersionId = 0;
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		Locale l = Locale.JAPAN;
		return port;
	}

	public String getAccount() {
		return account;
	}

	public String getPassword() {
		return password;
	}
	/** 取得配置文件中整数属性.如果没有此属性,返回缺省值为0.属性的配置可以是0x开头的16进制 */
	public int getIntProperty(String propertyName) {
		String s = this.getStringProperty(propertyName);
		if (s == null) {
			return 0;
		}
		s = s.toLowerCase();
		if(s.startsWith("0x")) {
			return Integer.parseInt(s.substring(2), 16);
		}
		return Integer.parseInt(s);
	}

	public void printHistory(String s) {
		try {
			if (out == null && log != null) {
				out = new FileOutputStream(this.log, true);
			}
			if (out != null) {
				out.write(s.getBytes("GBK"));
				out.write(0x0d);
				out.write(0x0a);
				out.flush();
			}
		} catch (Exception ex) {
		}
	}
}
