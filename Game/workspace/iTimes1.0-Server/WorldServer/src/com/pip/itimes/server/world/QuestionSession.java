package com.pip.itimes.server.world;

import org.apache.mina.common.IdleStatus;

import com.pip.itimes.net.Packet;
import com.pip.itimes.net.Session;

public class QuestionSession extends Session {

	private QuestionService questionService;
	
	public void setQuestionService(QuestionService questionService) {
		this.questionService = questionService;
	}
	
	public void closed() {
		// TODO Auto-generated method stub
		
	}

	public void created() {
		// TODO Auto-generated method stub
		
	}

	public void handle(Packet packet) {
		// TODO Auto-generated method stub
		
	}

	public void idle(IdleStatus status) {
		// TODO Auto-generated method stub
		
	}

	public void opened() {
		// TODO Auto-generated method stub
		
	}

}
