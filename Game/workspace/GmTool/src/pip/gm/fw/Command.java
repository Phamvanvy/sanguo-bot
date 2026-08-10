package pip.gm.fw;

/** 
 * 命令解释器引擎.
 * 用来轮询接受一个命令并处理.
 */
public abstract class Command {
	/** 获得命令的名称,这个名称是在命令行中紧跟斜线后面的引导命令id */
    public abstract String getCommand(Auth auth);
    /** 获取本命令的权限设置 */
    public abstract long getAuth();
//    {
//    	return -1;
//    }
    /** 命令的名称 */
    public abstract String getName(Auth auth);
    /** 具备格式的描述 */
    public abstract String getDescription(Auth auth);
    /**
     * 解释并执行命令行内容.
     * @param cmd 命令id
     * @param wd 供回调用的UWAP客户端应用
     * @param s 命令行信息
     * @return 如果为真则表示这个命令被识别并执行,将不被排在后面的命令解释器执行.
     */
    public abstract boolean exec(String cmd, AbstractClient wd, String []s) throws Exception;
    /** 返回某命令行是否可被此解释器解释并执行 */
    public boolean isCommand(Auth auth, String s) {
    	String cmd = getCommand(auth);
    	if (cmd == null) {
    		return false;
    	}
        return cmd.equals(s.toLowerCase());
    }
}
