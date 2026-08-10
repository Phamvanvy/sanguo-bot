package com.pip.itimes.server.world.question;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问题的保存
 * @author yufengchen
 *
 */
public class QuestionService {

	  private static ConcurrentHashMap<Integer,Vector> questionAllMap = new ConcurrentHashMap<Integer,Vector>();		//按照Question的version号对问题进行保存
	  private static ConcurrentHashMap<Integer,ConcurrentHashMap> questionIDMap = new ConcurrentHashMap<Integer,ConcurrentHashMap>();
	 /**
	  * 根据玩家接触的NPC确定，玩家应该大的问题集
	  * @param id
	  * @return
	  */
	  public static Question changeQuestions(int id){
	    	Question q = null;
	    	if(questionAllMap.containsKey(id) && questionIDMap.containsKey(id)){		// 
	    		q = new Question(questionAllMap.get(id),questionIDMap.get(id));
	    	} 
	    	return q;
	    }
	  public static void addQuestions(int id,Vector<Question> questionReference){
	    	questionAllMap.put(id, questionReference);
	    }
	    
	    public static void addQuestions(int id,ConcurrentHashMap<Integer, Question> questions){
	    	questionIDMap.put(id, questions);
	    }
}
