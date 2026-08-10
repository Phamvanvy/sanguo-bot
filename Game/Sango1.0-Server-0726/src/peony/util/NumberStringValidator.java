package peony.util;

/**
 * 数字字符串检查器。接受全部是数字的字符串。
 * @author lighthu
 */
public class NumberStringValidator implements IStringValidator {
	public int valid(String value) {
		if (value == null) {
			return IStringValidator.NULL;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < '0' || c > '9')
				return IStringValidator.ILLEGAL_CHAR;
		}
		return IStringValidator.OK;
	}
}
