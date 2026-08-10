package peony.common;

/**
 * 异步操作的接口，用于操作异步的请求，包括认证服务器请求以及数据库操作等等
 * 接口包括两个方法run以及callFinish，run执行的是请求操作，比如想认证服务器发送请求或者想数据库保存数据，此方法会在一个线程池中被调用。
 * callFinish用于调用结束后的操作
 * @author Jeffrey
 *
 */
public interface AsyncCall extends Runnable{
	public void callFinish() throws Exception;
}
