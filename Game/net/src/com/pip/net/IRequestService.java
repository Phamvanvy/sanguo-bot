package com.pip.net;

public interface IRequestService {
	public void add(int requestId,IRequest request);
	public IRequest remove(int id);
}
