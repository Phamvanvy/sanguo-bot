package peony.service.account;

import peony.net.ClientSession;

import com.pip.net.IMessage;

/**
 * 实现帐户系统接口的抽象接口。游戏服务器和帐户系统之间通过Message交互。
 * @author lighthu
 */
public interface AccountService {
	/**
	 * 发送一个不需要返回的Message到帐户系统。
	 * @param message 
	 */
	public void postMessage(IMessage message);
	
	/**
	 * 发送一个Message到帐户系统，并等待返回。
	 * @param message 
	 * @param call 回调接口
	 */
	public void sendAndRegister(IMessage message, AccountAsyncCall call);
	
	/**
	 * 把一个异步调用放到请求队列中等待执行。
	 * @param call
	 */
	public void schedule(AccountAsyncCall call);
	
	/**
	 * 登记一个用户连接，此方法在登录成功后调用。
	 * @param session
	 */
	public void registerClientSession(ClientSession session);
	
	/**
	 * 通过帐号ID查找登录的帐号。
	 * @param accountId
	 * @return
	 */
	public Account getAccount(int accountId);
	
	/**
	 * 通过帐号ID查找用户连接。
	 * @param accountId
	 * @return
	 */
	public ClientSession getClientSession(int accountId);

	/**
	 * 检查现在是否允许登录。只有没有到达服务器上限，并且排队队列中没有请求时，才可以登录。
	 */
	public boolean allowLogin(ClientSession session);
	
	/**
	 * 服务器满时把一个登录请求加入到排队队列中去。
	 * @param call
	 * @return
	 */
	public boolean scheduleLogin(AccountLoginCall call);
}
