package peony.service;

/**
 * 系统中的服务。一个服务提供一个功能模块对应的底层API或数据。
 * @author lighthu
 */
public interface Service {
	/**
	 * 启动服务。
	 * @throws Exception
	 */
	public void startup() throws Exception;
	
	/**
	 * 关闭服务。
	 */
	public void shutdown();
}
