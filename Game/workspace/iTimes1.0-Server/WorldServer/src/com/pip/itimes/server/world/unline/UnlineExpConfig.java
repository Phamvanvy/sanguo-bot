package com.pip.itimes.server.world.unline;

import java.util.Date;

import com.pip.itimes.server.world.BathHouse;
import com.pip.itimes.server.world.Discount;
import com.pip.itimes.server.world.Server;

public class UnlineExpConfig {
	public static final int LIFEVALUE_MAX = 120;			//最大活力值
	public static final int LIFEVALUE_MAX_ONLINE = 60;		//每天在线积累最大值 
	public static final int UNLINEEXP_HOUR_MAX = 100;		//最大小时数
	
	public static String loginMessage;
	public static String unloginMessage;
	public static int newCount;
	public static UnlineExpNew news[];
	
	public static int[] UnlineExp = {10, 30, 50, 60};		//小时数对应的活力值
	
	public static float PIP_MULTIPLE = 1.0f;					//PIP离线经验值的倍数 
	public static float CMCC_MULTIPLE = 1.0f;					//cmcc
	public static float QQ_MULTIPLE = 1.0f;						//qq
	
	//获得离线等级的经验 该经验通过BathHouse中设置的等级经验值计算而来
	public static int getLevelExp(int level){
		//if(level >= 100) return 0;
		float multiple = PIP_MULTIPLE;
		if(Server.iMoneyType == Server.IMONEY_TYPE_QQ){
			multiple = QQ_MULTIPLE;
		}
		if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
			multiple = CMCC_MULTIPLE;
		}
		if(level == 100){
			return (int)(BathHouse.EXP[level - 1] * 3.6f * 3 / 5 * multiple);
		}
		return (int)(BathHouse.EXP[level] * 3.6f * 3 / 5 * multiple);
	}
	
	/**
	 * 获得指定活力的时间
	 * @param life
	 * @return
	 */
	public static int getExpHour(int life){
		for(int i=0; i<UnlineExp.length; i++){
			if(life == UnlineExp[i]){
				return i + 1;
			}
		}
		return 0;
	}
	
	/**
	 * 获得跟指定时间的小时数
	 * @param date
	 * @return
	 */
	public static int getHour(Date date){
		if(date == null){
			return 0;
		}
		Date d = new Date();
		long timeStart = date.getTime();
		long timeEnd = d.getTime();
		return (int)((timeEnd - timeStart) / 1000 / 3600);
	}
	
	/**
	 * 获得当前公告
	 * @return
	 */
	public static String getNews(){
		Date date = new Date();
		long time = date.getTime();
		String message = "";
		int index = 1;
		for(int i=0; i<newCount; i++){
			if(time >= news[i].getStartTime() && time < news[i].getEndTime()){
				if(message.equals("")){
					message += "　" + index + "." + news[i].getMessage();
				}else{
					message += "\n　" + index + "." + news[i].getMessage();
				}
				index ++;
			}
		}
		if(message.equals("")){
			message = "目前没有新的公告。";
		}
		return message;
	}
}