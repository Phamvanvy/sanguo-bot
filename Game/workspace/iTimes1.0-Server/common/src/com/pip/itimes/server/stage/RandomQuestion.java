package com.pip.itimes.server.stage;

/**
 * @author wpjiang 
 * 服务器启动的时候随机产生200个问题，用于战斗防刷
 */
public class RandomQuestion {
	
	public byte getNum1() {
		return num1;
	}


	public void setNum1(byte num1) {
		this.num1 = num1;
	}


	public byte getNum2() {
		return num2;
	}


	public void setNum2(byte num2) {
		this.num2 = num2;
	}





	public byte getAnswer() {
		return answer;
	}


	public void setAnswer(byte answer) {
		this.answer = answer;
	}

	

	public byte getResult() {
		return result;
	}


	public void setResult(byte result) {
		this.result = result;
	}

	
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}



	public byte getErrorResult() {
		return errorResult;
	}


	public void setErrorResult(byte errorResult) {
		this.errorResult = errorResult;
	}

	
	/**
	 * 问题编号
	 */
	int id;
	
	/**
	 * 简单运算数字1
	 */
	byte num1;
	
	/**
	 * 简单运算数字2
	 */
	byte num2;
	
	
	/**
	 * 运算结果
	 */
	byte result;
	
	
	/**
	 * 正确的答案序号
	 */
	byte answer;
	
	/**
	 * 错误答案
	 */
	byte errorResult;
	
	
	/**
	 * 奖励经验数量
	 */
	int exp;
	

	public int getExp() {
		return exp;
	}


	public void setExp(int exp) {
		this.exp = exp;
	}


	public RandomQuestion(int id, byte num1, byte num2, byte reslut, byte errorResult, byte answer){
		
		this.id = id;
		this.num1 = num1;
		this.num2 = num2;
		this.result = reslut;
		this.errorResult = errorResult;
		this.answer = answer;
	}
	
	public String getRandomQuestionShow(){
		StringBuffer t = new StringBuffer();
		t.append("请回答以下问题:" + num1);
		if(answer == 0){
			t.append("+" + num2);
		}else{
			t.append("×" + num2);
		}
		t.append("=?\n1.");
		
		if(answer == 0){
			t.append(result + "\n2.");
			t.append(errorResult);
		}else{
			t.append(errorResult+ "\n2.");
			t.append(result);
		}
		return t.toString();
	}
}
