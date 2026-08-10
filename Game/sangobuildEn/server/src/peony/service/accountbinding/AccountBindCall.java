package peony.service.accountbinding;


import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.util.IStringValidator;
import peony.util.PatternStringValidator;

public class AccountBindCall extends ClientSessionAsyncCall {
	
	private static final Logger log = Logger.getLogger(AccountBindCall.class);
	
	int serial;
	int type;
	String value1,value2,idcard;
	int accountId;
	int playerId;
	
	public static int[][] REWARDS = {  //绑定以后的奖励，前两个是绑定身份证后的奖励，后两个是绑定问题后的奖励
		{651,5},
		{661,5},
		{652,5},
		{819,5},
	};
	
	public AccountBindCall(ClientSession session,Packet pt){
		super(session);
		serial = pt.getInt();
		type = pt.getByte();
		value1 = pt.getString();
		value2 = pt.getString();
		idcard = pt.getString();
		accountId = session.getIdentity().getId();
		playerId = ((Player)session.getClient()).id;
	}

	public void callFinish() throws Exception {
		
	}

//	(1 绑定手机 2 绑定身份证 3 绑定问题 4 绑定邮箱)
	public void run() {
		String retMessage = null;
		String sms = null;
		try {
			log.info("[ACCOUNTBIND]ACCOUNT["+accountId+"]TYPE["+type+"]VALUE1["+value1+"]VALUE2["+value2+"]");
			if(type==1){
				if(!validPhone(value1)){
					ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "錯誤的手机號");
					return;
				}
				if(!Server.server.getServiceRegistry().getAccountBindingService().validIdcard(accountId, idcard)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "身份証號錯誤");
					return;
				}
				sms = Server.server.getServiceRegistry().getAccountBindingService().bindPhone(accountId, value1);
				retMessage = "綁定手机申請提交成功,請确認您的手机可以正常發送短信.";
			}else if(type==2){
				if(!validIdcard(value1)){
					ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "身份証格式錯誤");
					return;
				}
				Server.server.getServiceRegistry().getAccountBindingService().bindIdCard(accountId, value1);
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, "系統", "綁定身份証獎勵", "", 0, ObjectAccessor.createGameItem(REWARDS[0][0]), REWARDS[0][1], "ACCBIND");
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, "系統", "綁定身份証獎勵", "", 0, ObjectAccessor.createGameItem(REWARDS[1][0]), REWARDS[1][1], "ACCBIND");
				retMessage = "恭喜您綁定成功,您的獎勵已通過飛鴿發送.請稍候查收.";
			}else if(type==3){
				if(!validQna(value1, value2)){
					ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "問題和答案字數必須在3到30之間");
					return;
				}
				if(!Server.server.getServiceRegistry().getAccountBindingService().validIdcard(accountId, idcard)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "身份証號錯誤");
					return;
				}
				Server.server.getServiceRegistry().getAccountBindingService().bindQna(accountId, value1, value2);
				retMessage = "恭喜您綁定成功,您的獎勵已通過飛鴿發送.請稍候查收.";
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, "系統", "綁定問題獎勵", "", 0, ObjectAccessor.createGameItem(REWARDS[2][0]), REWARDS[2][1], "ACCBIND");
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, "系統", "綁定問題獎勵", "", 0, ObjectAccessor.createGameItem(REWARDS[3][0]), REWARDS[3][1], "ACCBIND");

			}else if(type==4){
				if(!validMail(value1)){
					ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "郵件格式錯誤");
					return;
				}
				if(!Server.server.getServiceRegistry().getAccountBindingService().validIdcard(accountId, idcard)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, "身份証號錯誤");
					return;
				}
				Server.server.getServiceRegistry().getAccountBindingService().bindMail(accountId, value1);
				retMessage = "現在需要你去郵箱里确認哦,才可以最終完成綁定,赶緊去吧.";
			}
			if (type == 1) {
				Packet pt = new Packet(OpCode.ACCOUNTBINDING_SMS_SERVER);
				pt.putInt(serial);
				pt.putString(sms);
				session.send(pt);
			} else {
				Packet pt = new Packet(OpCode.ACCOUNTBINDING_SERVER);
				pt.putInt(serial);
				pt.putString(retMessage);
				session.send(pt);
			}
		} catch (BindException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNTBINDING_CLIENT, e.getMessage());
		}
	}
	
	protected static IStringValidator phoneValidator = new PatternStringValidator("^13[0-9]{1}[0-9]{8}|^15[0-9]{1}[0-9]{8}|^18[0-9]{1}[0-9]{8}");
	protected static IStringValidator idcardValidator = new PatternStringValidator("\\d{15}|\\d{17}[\\dXx]");;
	protected static IStringValidator mailValidator = new PatternStringValidator("^([a-z0-9A-Z]+[_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	
	
	public static void main(String[] args){
		System.out.println(validMail("ss_918@hotmail.com"));
	}
	
	public static boolean validPhone(String phone){
		return phoneValidator.valid(phone) == IStringValidator.OK;
	}
	
	public static boolean validMail(String mail){
		return mailValidator.valid(mail) == IStringValidator.OK;
	}
	
	public static boolean validIdcard(String idcard){
		return idcardValidator.valid(idcard) == IStringValidator.OK;
	}
	
	
//	public boolean validMail(String mail){
//    	if (mail.indexOf("@")>=0){
//    		for(int i=0;i<mail.length();i++){
//                if("@".equalsIgnoreCase(mail.substring(i, i+1))){
//                	mail = mail.substring(0,i);
//                	mail = mail.substring(i+1);
//                	break;
//                }
//            }
//    		if ((mail == null) || (mail == null)){
//    			return false;
//    		}else{
//    			if (mail.indexOf(".")<0){
//    				return false;
//        		}
//    		}
//    	}else{
//    		return false;
//    	}
//    	return true;
//	}
//	
//	public boolean validPhone(String phone){
//		if(phone.length()!=11)
//			return false;
//		for(int i=0;i<phone.length();i++){
//			if(!Character.isDigit(phone.charAt(i))){
//				return false;
//			}
//		}
//		return true;
//	}
//	
//	public boolean validIdcard(String idCard){
//    	if ((idCard.length() != 18) && (idCard.length() != 15)){
//    		return false;
//    	}
//    	if((idCard.substring(idCard.length()-1).equalsIgnoreCase("X")) ||
//    			(idCard.substring(idCard.length()-1).equalsIgnoreCase("x"))){
//    		idCard=idCard.substring(0, idCard.length()-1) + "0";
//    	}
//    	if((("a".compareTo(idCard)<=0) && ("z".compareTo(idCard)>=0))||
//    			(("A".compareTo(idCard)<=0) && ("Z".compareTo(idCard)>=0))){
//    		return false;
//    	}
//    	return true;
//	}
	
	public boolean validQna(String question,String answer){
		if(question.length()<3||question.length()>30)
			return false;
		if(answer.length()<3||answer.length()>30)
			return false;
		return true;
	}
}
