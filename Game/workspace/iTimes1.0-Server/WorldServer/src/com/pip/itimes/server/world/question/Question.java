package com.pip.itimes.server.world.question;


import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.QuestionService;


public class Question{
    private static Random random = new Random(System.currentTimeMillis());

    public static Vector<Object[]> questionTimes = new Vector<Object[]>();
    public static String questionTimes_str = new String();
    //保留原始的使用题库，每次根据npc对应的题库来 重新覆盖掉原来的题库。
    private  Vector<Question> questionReference = new Vector<Question>();
    private  ConcurrentHashMap<Integer, Question> questionMap = new ConcurrentHashMap<Integer, Question>();
    
    //原始的pip，cmcc，qq题库
//    private static Vector<Question> oldQquestionReference = new Vector<Question>();
//    private static ConcurrentHashMap<Integer, Question> oldQuestionMap = new ConcurrentHashMap<Integer, Question>();
    //cmcc题库
//    private static Vector<Question> cmccQuestionReference = new Vector<Question>();
//    private static ConcurrentHashMap<Integer, Question> CmccQuestionMap = new ConcurrentHashMap<Integer, Question>();
    
  
    
    public Question(Vector<Question> questionReference,ConcurrentHashMap<Integer, Question> questionMap){
    	this.questionReference = questionReference;
    	this.questionMap = questionMap;
    }
    
    
    public static QuestionService questionService;

    private int id;
    private String question;
    private int select;
    private String[] answers;

    public Question(int i, String q, int s, String[] a){
        id = i;
        question = q;
        select = s;
        answers = a;
    }
//    public static void addQuestion(Question q){
//        questionReference.addElement(q);
//        questionMap.put(q.id, q);
//        //加载默认老题库
//        oldQquestionReference.addElement(q);
//        oldQuestionMap.put(q.id, q);
    	
//    }
    
//    public static void addCmccQuestion(Question q){
//        cmccQuestionReference.addElement(q);
//        CmccQuestionMap.put(q.id, q);
//    }
    public static void addQuestionTime(Object[] o){
        questionTimes.add(o);
    }

    public  Question getQuestionById(int id){
        return questionMap.get(id);
    }

    public  Question getNextQuestion(){
        if(questionReference.size() > 0){
            return questionReference.elementAt(random.nextInt(questionReference.size()));
        }else{
            return null;
        }
    }

    public int getId(){
        return id;
    }

    public String getQuestion(){
        return question;
    }

    public int getSelect(){
        return this.select;
    }

    public String[] getAnswers(){
        return answers;
    }
}
