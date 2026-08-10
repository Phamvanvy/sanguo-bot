package com.pip.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRequestService implements IRequestService {
	
	private Map<Integer,IRequest> map = new ConcurrentHashMap<Integer,IRequest>();

	public void add(int requestId,IRequest request) {
		map.put(requestId, request);
	}

	public IRequest remove(int id) {
		return map.remove(id);
	}

}
