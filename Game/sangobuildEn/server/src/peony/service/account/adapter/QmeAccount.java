package peony.service.account.adapter;

/**
 * 一个Qme帐号的相关信息。
 * @author lighthu
 */
public class QmeAccount {
	/**
	 * 帐号名。
	 */
	public String name;
	/**
	 * Qme使用者编号。
	 */
	public int qmeID;
	/**
	 * Q库使用者编号。
	 */
	public int qbID;
	/**
	 * tsi（类似于session key）
	 */
	public String tsi;
	/**
	 * 如果未开通付费服务（Q库），指定注册开通的短信内容。
	 */
	public String smsCode;
}
