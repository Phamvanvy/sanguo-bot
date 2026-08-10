package com.pip.itimes.server.stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * @author wpjiang
 *	用于管理随机生出的问题
 */
public class RandomQuestionManager {
	
	
	private static final Logger log = Logger.getLogger(RandomQuestionManager.class);
	
	/**
	 * 随机问题数量 
	 */
	public static final int randomQuestion = 81;
	
	public static Map<Integer, RandomQuestion> randomQuestionMap = new HashMap<Integer, RandomQuestion>();
	
	public RandomQuestionManager(){
		//randomQuestionMap.clear();
		//makeRandomQuestion();
	}
	
	public static RandomQuestion makeRandomQuestion(int id){
		RandomQuestion randQuestion = null;
		Random rand = new Random();
		//for(int i = 0; i < randomQuestion; i++){
			byte num1 = (byte) (1 + rand.nextInt(10));
			byte num2 = (byte) (1 +  rand.nextInt(10));
			byte answer = (byte) (rand.nextInt(2));
			byte result = 0;
			byte errorResult = 0;
			switch(answer){
				case 0:
					result = (byte) (num1 + num2);
					errorResult = (byte) Math.abs(result - (rand.nextInt(result) + 1));
					break;
				case 1:
					result = (byte) (num1 * num2);
					errorResult = (byte) Math.abs(result - (rand.nextInt(result) + 1));
					break;
				default:
					break;
			
			}
			randQuestion = new RandomQuestion(id , num1, num2, result, errorResult, answer);
			//randomQuestionMap.put(i, randQuestion);
			
		//}
		return randQuestion;
	}
	
	/*public static RandomQuestion getRandomQuestion(int id){
		
		return randomQuestionMap.get(id);
	}*/
	
	/*public static int getRandomQuestionSize(){
		return randomQuestionMap.size();
	}*/
}
