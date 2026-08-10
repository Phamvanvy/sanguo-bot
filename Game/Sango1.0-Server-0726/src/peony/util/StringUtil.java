package peony.util;

import java.io.File;
import java.util.List;

/**
 * 字符串相关方法库。
 * @author lighthu
 */
public class StringUtil {
	protected static KeywordsManager keywordsManager;
//	static {
//		String dir = System.getProperty("user.dir");
//		File nameFile = new File(dir, "invalidname.txt");
//		File keywordsFile = new File(dir, "keywords.xml");
//		try {
//			keywordsManager = new KeywordsManager(nameFile, keywordsFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static final void init(File nameFile, File keywordsFile, List<String> strings) throws Exception{
		keywordsManager = new KeywordsManager(nameFile, keywordsFile,strings);
		nameValidator = new NameValidator(keywordsManager, 1, 16, false, false);
		tongNameValidator = new NameValidator(keywordsManager, 1, 16, false, true);
		passwordValidator = new NameValidator(keywordsManager, 1, 16, false, false);
	}
	
	protected static IStringValidator numberStringValidator = new NumberStringValidator();
	protected static IStringValidator phoneValidator = new PatternStringValidator("^13[0-9]{1}[0-9]{8}|^15[0-9]{1}[0-9]{8}|^18[0-9]{1}[0-9]{8}");
	protected static IStringValidator idcardValidator = new PatternStringValidator("\\d{15}|\\d{17}[\\dXx]");;
	protected static IStringValidator mailValidator = new PatternStringValidator("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	protected static IStringValidator accountNameValidator = new PatternStringValidator("sg[0-9]{4}|sg3[0-9]{4}");
	protected static IStringValidator nameValidator;
	protected static IStringValidator tongNameValidator;
	protected static IStringValidator passwordValidator ;
	protected static IStringValidator textValidator = new TextValidator();
	
	/**
	 * 检查一个字符串是否完全数字。
	 * @param str
	 * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
	 */
	public static int isNumber(String str) {
		return numberStringValidator.valid(str);
	}
	
	/**
	 * 检查一个字符串是否合法的手机号。
	 * @param str
	 * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
	 */
	public static int isPhone(String str) {
		return phoneValidator.valid(str);
	}
	
	/**
	 * 检查一个字符串是否正确的身份证号。
	 * @param str
	 * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
	 */
	public static int isIdcard(String str) {
		return idcardValidator.valid(str);
	}
	
	/**
	 * 检查一个字符串是否合法的名字。
	 * @param str
	 * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
	 */
	public static int isValidName(String str) {
		return nameValidator.valid(str);
	}

    /**
     * 检查一个字符串是否合法的军团名字。
     * @param str
     * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
     */
    public static int isValidTongName(String str) {
        return tongNameValidator.valid(str);
    }
	
	/**
	 * 检查一个字符串是否合法的密码。
	 * @param str
	 * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
	 */
	public static int isValidPassword(String str) {
		return passwordValidator.valid(str);
	}
	
	/**
	 * 检查一个字符串是否合法的文本。
	 * @param str
	 * @return 如果通过检查，返回0。错误信息可通过{#getValidatorMessage}获取。
	 */
	public static int isValidText(String str) {
		return textValidator.valid(str);
	}
	
	public static boolean isAccountNameValid(String name){
		return accountNameValidator.valid(name)==IStringValidator.OK;
	}
	
	/**
	 * 过滤一个字符串中出现的敏感字符串并替换为XXX。
	 * @param str
	 * @return 过滤后的字符串。
	 */
	public static String filterBadWords(String str) {
		return keywordsManager.filterBadWords(str);
	}
	
	public static boolean hasBadWord(String str){
		return keywordsManager.containsBadWord(str);
	}
	
	
	/**
	 * 根据错误码返回错误原因。
	 */
	public static String getValidatorMessage(int code) {
		switch (code) {
		case IStringValidator.MAX_LIMIT:
			return "太长";
		case IStringValidator.MIN_LIMIT:
			return "太短";
		case IStringValidator.ILLEGAL_CHAR:
			return "包含非法字符";
		case IStringValidator.NULL:
			return "不能为空";
		case IStringValidator.ERROR_PATTERN:
			return "格式不正确";
		default:
			return "正确";
		}
	}
	
	public static final void main(String[] args) {
//		Pattern p = Pattern.compile("：[a-zA-z]+://[^\\s]*");
		System.out.println("我爱你中国dng.cn".replaceAll("(\\w+\\.)+[a-zA-Z]+", "3g.pipgame.cn"));
//		System.out.println(Pattern.matches("(\\w+\\.)+[a-zA-Z]+", "123cn"));
//		TestService service = new TestService();
//		AppContext.reg(TestService.class, service);
//		new ServerContext();
//		for (int j = 0; j < 10; j++) {
//			long t = System.currentTimeMillis();
//			for (int i = 0; i < 10000000; i++) {
//				TestService s = (TestService) AppContext.get(TestService.class);
//			}
//			System.out.println("time1:" + (System.currentTimeMillis() - t));
//		}
//		for (int j = 0; j < 10; j++) {
//			long t = System.currentTimeMillis();
//			for (int i = 0; i < 10000000; i++) {
//				TestService s = ServerContext.context.getReg().getTestService();
//			}
//			System.out.println("time2:" + (System.currentTimeMillis() - t));
//		}
//		System.out.println(String.format("%3.1f",11.0f));
	}
}
