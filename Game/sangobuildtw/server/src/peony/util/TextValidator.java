package peony.util;


/**
 * 文字合法性检查器。某些非法字符会导致数据库错误，所以需要过滤。
 * @author lighthu
 */
public class TextValidator implements IStringValidator {
	public int valid(String value) {
		if (value == null) {
			return IStringValidator.NULL;
		}
		if (!checkString(value)) {
			return IStringValidator.ILLEGAL_CHAR;
		}
		return IStringValidator.OK;
	}

	private boolean checkString(String s){
        for(int i = 0; i < s.length(); i++){
            char ch = s.charAt(i);
            if (!isValidChar(ch)) {
            	return false;
            }
        }
        return true;
    }
    
	private boolean isValidChar(char ch) {
    	if(ch >= 0x20 && ch <= 0x7e)
            return true;
        if(ch >= 0x2018 && ch <= 0x201D)
            return true;
        if(ch == 2026)
            return true;
        if(ch >= 3001 && ch <= 0x3002)
            return true;
        if(ch >= 3008 && ch <= 3011)
            return true;
        if(ch >= 4e00 && ch <= 0x9fa5)
            return true;
        if(ch >= 0xf92c && ch <= 0xfa29)
            return true;
        if(ch >= 0xff01 && ch <= 0xffe5)
            return true;
        return false;
    }
}
