package pip.gm.fw;

/**
 * 当网络连接空闲(一般需要发送同步包以保持网络连接)时候的后台任务.
 */
public interface IdleTask {
	/** 在网络空闲时期执行,如果本次运行触发了网络请求则返回真 */
	public boolean processIdleTask(AbstractClient abstractClient);
}
