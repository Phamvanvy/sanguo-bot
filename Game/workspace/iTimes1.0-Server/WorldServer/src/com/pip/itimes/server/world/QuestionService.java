package com.pip.itimes.server.world;

import java.util.Calendar;
import java.util.Date;

import com.pip.itimes.server.bean.Question;
import com.pip.itimes.server.dao.QuestionDao;

public class QuestionService implements Runnable{

	private QuestionDao dao;

	ChatService chatService;

	private static boolean open;
	private static int delay;
	private static Object[] time;

	public QuestionService(QuestionDao dao) {
		this.dao = dao;
		open = false;
		delay = 0;
		new Thread(this).start();
	}

	public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}

	public Question getQuestion(int questionid) {
		return dao.getQuestion(questionid);
	}

	public void addQuestion(Question question) {
		dao.addQuestion(question);
	}

	public void setQuestion(Question question) {
		dao.setQuestion(question);
	}

	public void run() {
		while(true) {
			try {
				Thread.sleep(60000);
				if(open)
					delay++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(!open) {
				long now = System.currentTimeMillis()%86400000;
				for(int i = 0 ; i < com.pip.itimes.server.world.question.Question.questionTimes.size() ; i ++) {
					time = (Object[])com.pip.itimes.server.world.question.Question.questionTimes.get(i);
					if(now >= Long.parseLong(String.valueOf(time[1])) && now <= (Long.parseLong(String.valueOf(time[1])) + 120000)) {
						//开始
						open = true;
						Calendar c1 = Calendar.getInstance();
						c1.setTime(new Date(Long.parseLong(String.valueOf(time[1]))));
						Calendar c2 = Calendar.getInstance();
						c2.setTime(new Date(Long.parseLong(String.valueOf(time[2]))));
						String str = c1.get(Calendar.HOUR_OF_DAY) + "点" + c1.get(Calendar.MINUTE) + "分到" + c2.get(Calendar.HOUR_OF_DAY) + "点" + c2.get(Calendar.MINUTE) + "分";
						chatService.sendSystemMessage(str + "的答题时间已到,请到瓦伊特杂货店商人处购买随身答题果直接答题,或到海滨浴场智慧水晶处免费答题!");
						break;
					}
				}
			}else {
				if(delay >= 19) {
					//再发公告
					open = false;
					delay = 0;
					Calendar c1 = Calendar.getInstance();
					c1.setTime(new Date(Long.parseLong(String.valueOf(time[1]))));
					Calendar c2 = Calendar.getInstance();
					c2.setTime(new Date(Long.parseLong(String.valueOf(time[2]))));
					String str = c1.get(Calendar.HOUR_OF_DAY) + "点" + c1.get(Calendar.MINUTE) + "分到" + c2.get(Calendar.HOUR_OF_DAY) + "点" + c2.get(Calendar.MINUTE) + "分";
					chatService.sendSystemMessage(str + "的答题时间已到,请到瓦伊特杂货店商人处购买随身答题果直接答题,或到海滨浴场智慧水晶处免费答题!");
				}
			}
		}
	}
}
