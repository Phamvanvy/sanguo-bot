package peony.common;

import peony.net.ClientSession;

/**
 * 将异步操作绑定到一个ClientSession，在run执行以后将有容器或者手工调用addToClientSession方法，将调用加入到ClientSession
 * 的异步请求队列中，由ClientSession在每个cycle中执行callFinish
 * @author Jeffrey
 *
 */
public abstract class ClientSessionAsyncCall implements AsyncCall {

	protected ClientSession session;
	public boolean success = true;
	protected Exception exception;
	protected String errorMessage;
	
	public ClientSessionAsyncCall(ClientSession session){
		this.session = session;
	}
	
	public void addToClientSession(){
		session.addAsyncCall(this);
	}
	
	public void error(Exception exception,String errorMessage){
		this.success = false;
		this.exception = exception;
		this.errorMessage = errorMessage;
	}
	
	public void error(String errorMessage){
		this.success = false;
		this.errorMessage = errorMessage;
	}
	
	public ClientSession getSession() {
		return session;
	}	
}
