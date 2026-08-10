package com.pip.net;

import java.util.HashMap;
import java.util.Map;

public class DefaultRequestService implements IRequestService {
	
	private Map<Integer,IRequest> map = new HashMap<Integer,IRequest>();

	public void add(int requestId,IRequest request) {
		map.put(requestId, request);
	}

	public IRequest remove(int id) {
		return map.remove(id);
	}

}
