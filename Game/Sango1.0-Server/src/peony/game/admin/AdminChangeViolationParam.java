package peony.game.admin;

import java.lang.reflect.Field;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

//public static int TIME_ERROR_SCORE_1 = 5;					// 初级时间错误扣分值                                                     0
//public static int TIME_ERROR_SCORE_2 = 10;					// 中级时间错误扣分值						1
//public static int TIME_ERROR_SCORE_3 = 20;					// 高级时间错误扣分值                                                      2
//public static int MAX_MOVE_DISTANCE = 5000;					// 单次MOVE最大允许发送间隔（毫秒）                   3  
//public static int EXCEED_DISTANCE_SCORE = 100;				// 超过单次MOVE最大允许移动距离扣分值              4
//public static int POSITION_ERROR_SCORE1 = 10;				// 移动速度过快初级错误扣分值                                   5
//public static int POSITION_ERROR_SCORE2 = 20;				// 移动速度过快高级错误扣分值                                   6
//public static int TOTAL_CHEAT_ERROR_SCORE = 20;				// 累计移动距离超过设定速度扣分值                         7
//public static int TOO_MUCH_MOVE_ERROR_SCORE = 100;			// MOVE包发送过于频繁扣分值                                        8
//public static double MOVE_CHEAT_VALVE1 = 0.5;				// 移动速度过快初级错误阈值                                         9
//public static double MOVE_CHEAT_VALVE2 = 1.0;				// 移动速度过快高级错误阈值                                         10
//public static double TOTAL_CHEAT_VALVE = 5;					// 累计移动距离错误阈值					 11
//public static int TOO_MUCH_MOVE_VALVE = 20000;				// 判断MOVE包发送过快的时间阈值			 12 


public class AdminChangeViolationParam extends ClientSessionAsyncCall{

	private int serial;
	
	private int xuhao;
	
	private String value;
	
	public static int TIME_ERROR_SCORE_1 = 0;					
	public static int TIME_ERROR_SCORE_2 = 1;				
	public static int TIME_ERROR_SCORE_3 = 2;					
	public static int MAX_MOVE_DISTANCE =  3;					
	public static int EXCEED_DISTANCE_SCORE = 4;				
	public static int POSITION_ERROR_SCORE1 = 5;			
	public static int POSITION_ERROR_SCORE2 = 6;			
	public static int TOTAL_CHEAT_ERROR_SCORE = 7;				
	public static int TOO_MUCH_MOVE_ERROR_SCORE = 8;			
	public static double MOVE_CHEAT_VALVE1 = 9;				
	public static double MOVE_CHEAT_VALVE2 = 10;				
	public static double TOTAL_CHEAT_VALVE = 11;					
	public static int TOO_MUCH_MOVE_VALVE = 12;				
	
	public AdminChangeViolationParam(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		xuhao = packet.get();
		value = packet.getString();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet packet = new Packet(OpCode.ADMIN_CHANGE_VIOLATIONPARAM_SERVER);
			packet.putInt(serial);
			session.send(packet);
		}else{
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_CHANGE_VIOLATIONPARAM_CLIENT,errorMessage );
		}
	}

	public void run() {
		executeOrder();
		addToClientSession();
	}
	
	//执行一个原子命令
	private void executeOrder(){
		int orderNum = xuhao;
		try{
			if(orderNum == TIME_ERROR_SCORE_1){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TIME_ERROR_SCORE_1");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == TIME_ERROR_SCORE_2){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TIME_ERROR_SCORE_2");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == TIME_ERROR_SCORE_3){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TIME_ERROR_SCORE_3");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == MAX_MOVE_DISTANCE){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("MAX_MOVE_DISTANCE");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == EXCEED_DISTANCE_SCORE){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("EXCEED_DISTANCE_SCORE");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == POSITION_ERROR_SCORE1){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("POSITION_ERROR_SCORE1");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == POSITION_ERROR_SCORE2){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("POSITION_ERROR_SCORE2");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == TOTAL_CHEAT_ERROR_SCORE){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TOTAL_CHEAT_ERROR_SCORE");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == TOO_MUCH_MOVE_ERROR_SCORE){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TOO_MUCH_MOVE_ERROR_SCORE");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == MOVE_CHEAT_VALVE1){
				double ord = Double.parseDouble(value);
				Field field = Player.class.getDeclaredField("MOVE_CHEAT_VALVE1");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == MOVE_CHEAT_VALVE2){
				double ord = Double.parseDouble(value);
				Field field = Player.class.getDeclaredField("MOVE_CHEAT_VALVE2");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == TOTAL_CHEAT_VALVE){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TOTAL_CHEAT_VALVE");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else if(orderNum == TOO_MUCH_MOVE_VALVE){
				int ord = Integer.parseInt(value);
				Field field = Player.class.getDeclaredField("TOO_MUCH_MOVE_VALVE");
				field.setAccessible(true);
				field.set(Player.class,ord);
			}else{
				error("序号不正确");
			}
		}catch(Exception e){
		}
		
	}

}
