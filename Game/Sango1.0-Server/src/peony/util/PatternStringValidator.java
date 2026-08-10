package peony.util;

import java.util.regex.Pattern;

/**
 * 正则表达式模式匹配检查器。
 * @author lighthu
 */
public class PatternStringValidator implements IStringValidator {
	protected Pattern pattern;

	public PatternStringValidator(String regex) {
		pattern = Pattern.compile(regex);
	}

	public int valid(String value) {
		if (value == null) {
			return IStringValidator.NULL;
		}
		if (pattern.matcher(value).matches()) {
			return IStringValidator.OK;
		}
		return IStringValidator.ERROR_PATTERN;
	}
}
