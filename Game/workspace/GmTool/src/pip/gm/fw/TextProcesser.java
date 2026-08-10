package pip.gm.fw;

/**
 * 文本信息处理引擎.
 * 用来处理工具中输入的文本信息。根据信息类型分别处理。
 */
public interface TextProcesser {
	/** 按指定类型处理文本信息 */
	public void processText(String type, String s);
	/** 获得本引擎支持的各种处理文本消息的类型，工具中会把这些类型作为选项 */
	public String[] getTypes();

}
