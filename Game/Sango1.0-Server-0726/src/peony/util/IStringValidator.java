package peony.util;

/**
 * 字符串格式检查接口。
 * @author jeffrey
 */
public interface IStringValidator {
	/**
	 * 检查通过。
	 */
	public static final int OK = 0;
	/**
	 * 超过最大长度限制。
	 */
	public static final int MAX_LIMIT = 1;
	/**
	 * 没到最小长度限制。
	 */
	public static final int MIN_LIMIT = 2;
	/**
	 * 出现非法字符。
	 */
	public static final int ILLEGAL_CHAR = 3;
	/**
	 * 传入参数为空。
	 */
	public static final int NULL = 4;
	/**
	 * 模式不匹配。
	 */
	public static final int ERROR_PATTERN = 5;
	
	/**
	 * 判断一个字符串是否是一个合法的字符串。
	 * @param value 
	 * @return
	 */
	int valid(String value);
}
