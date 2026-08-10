package peony.util;

import java.io.UnsupportedEncodingException;

import peony.game.Server;

/**
 * 名字字符串检查器。名字字符串只允许出现'_', 数字，字母和汉字。如果指定了allowColon选项，还 允许出现':'字符。
 * @author lighthu
 */
public class NameValidator implements IStringValidator {
	protected KeywordsManager km;
	protected int minLimit;
	protected int maxLimit;
	protected boolean allowColon;
	protected boolean allowAny;
	protected boolean allowForeignChars;

	/**
	 * 构造一个名字检查器。
	 * 
	 * @param km
	 *            关键字管理
	 * @param minLimit
	 *            最小GBK字节数（包含）
	 * @param maxLimit
	 *            最大GBK字节数（包含）
	 * @param allowColon
	 *            是否允许出现':'符号
	 */
	public NameValidator(KeywordsManager km, int minLimit, int maxLimit,
			boolean allowColon, boolean allowAny) {
		this.km = km;
		this.minLimit = minLimit;
		this.maxLimit = maxLimit;
		this.allowColon = allowColon;
		this.allowAny = allowAny;
		
		if("true".equals(Server.server.getConfig().getString("allow_foreign"))){
		    allowForeignChars = true;
		}else{
		    allowForeignChars = false;
		}
	}

	public int valid(String value) {
		if (value == null) {
			return IStringValidator.NULL;
		}
		int length = value.length();
		try {
			length = value.getBytes("GBK").length;
		} catch (UnsupportedEncodingException e) {
		}
		if (length < minLimit) {
			return IStringValidator.MIN_LIMIT;
		}
		if (length > maxLimit) {
			return IStringValidator.MAX_LIMIT;
		}
		if (!allowAny) {
    		if (!checkString(value, allowColon)) {
    			return IStringValidator.ILLEGAL_CHAR;
    		}
		}
		if (km.isInvalidName(value)) {
			return IStringValidator.ILLEGAL_CHAR;
		}
		if (km.containsBadWord(value)) {
			return IStringValidator.ILLEGAL_CHAR;
		}
		return IStringValidator.OK;
	}

	/*
	 * 检查字符串中是否只包含合法字符。合法字符包括：'_', 字母，数字，汉字。如果allowColon为true，
	 * 也可以包含':'。
	 * @param s
	 * @param allowColon
	 * @return
	 */
	private boolean checkString(String s, boolean allowColon) {
		if (s == null) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			boolean isValid = false;
			if (ch >= 'a' && ch <= 'z') {
				isValid = true;
			} else if (ch >= 'A' && ch <= 'Z') {
				isValid = true;
			} else if (ch >= '0' && ch <= '9') {
				isValid = true;
			} else if (ch == '_') {
				isValid = true;
			} else if (ch >= 0x4E00 && ch <= 0x9FA5) {
				isValid = true;
			} else if (Server.server.REVISION_TYPE_VN.equals(Server.server.revision)){
				if (ch >= 0x00C0 && ch <= 0x1EF9) {
					isValid = true;
				}
			} else if (allowForeignChars && ch >= 0x0080 && ch <= 0xFFFE) {
                isValid = true;
            } else if (allowColon && ch == ':') {
				isValid = true;
			}
			if (!isValid) {
				return false;
			}
		}
		return true;
	}
}
