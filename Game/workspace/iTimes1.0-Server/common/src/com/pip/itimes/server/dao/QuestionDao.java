package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Question;

public class QuestionDao extends BaseDao{

	public QuestionDao() {
		super();
	}
	
	public Question getQuestion(int questionid){
        try {
            String hql = "from Question q where q.questionId='" + questionid + "'";
            return (Question) uniqueResult(hql);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public void addQuestion(Question question){
        try {
            makePersistent(question);
        } catch (DataAccessException ex) {
        }
    }
    
    public void setQuestion(Question question) {
    	try {
    		makePersistent(question);
    	} catch (DataAccessException ex) {
    		
    	}
    }
}
