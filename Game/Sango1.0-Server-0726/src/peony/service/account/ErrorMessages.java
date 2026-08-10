package peony.service.account;

import java.util.HashMap;
import java.util.Map;

import peony.service.account.cmcc.CmccErrorMessage;

import com.pip.net.message.ErrorMessage;

public class ErrorMessages {
    public static final int UNKNOW = 1;
    public static final String UNKNOW_MESSAGE = "未知错误";

    public static final int UNKNOW_ACCOUNT = 2;
    public static final String UNKNOW_ACCOUNT_MESSAGE = "不存在此账号";

    public static final int UNKOWN_SOURCE = 3;
    public static final String UNKNOW_SOURCE_MESSAGE = "未知的数据源";

    public static final int ILLEGAL_SESSIONID = 4;
    public static final String ILLEGAL_SESSIONID_MESSAGE = "登录会话已失效，请退出游戏重新登录";

    public static final int NOT_ENOUGH_BALANCE = 5;
    public static final String NOT_ENOUGH_BALANCE_MESSAGE = "没有足够的元宝,请及时充值";

    public static final int ILLEGAL_VALUE = 6;
    public static final String ILLEGAL_VALUE_MESSAGE = "错误的数值";

    public static final int ERROR_OLD_PASSWORD = 7;
    public static final String ERROR_OLD_PASSWORD_MESSAGE = "错误的原密码";

    public static final int ILLEGAL_PHONE = 8;
    public static final String ILLEGAL_PHONE_MESSAGE = "错误的手机号码";

    public static final int NOT_ENOUGH_TRUST_BALANCE = 9;
    public static final String NOT_ENOUGH_TRUST_BALANCE_MESSAGE = "可用元宝不足(某些充值渠道,例如苹果商店支付,购买的元宝不能用于交易)";
    
    //create account exceptions
    public static final int DUPLICATE_NAME = 101;
    public static final String DUPLICATE_NAME_MESSAGE = "已存在相同的账户名";

    public static final int NULL_NAME = 102;
    public static final String NULL_NAME_MESSAGE = "无效的用户名";

    public static final int ILLEGAL_NAME_CHAR = 103;
    public static final String ILLEGAL_NAME_CHAR_MESSAGE = "非法的用户名";

    public static final int ILLEGAL_LENGTH = 104;
    public static final String ILLEGAL_LENGTH_MESSAGE = "帐户名长度太长";

    public static final int NULL_PASSWORD = 105;
    public static final String NULL_PASSWORD_MESSAGE = "无效的密码";

    public static final int ILLEGAL_PASSWORD_CHAR = 106;
    public static final String ILLEGAL_PASSWORD_CHAR_MESSAGE = "密码存在非法字符";

    public static final int ILLEGAL_PASSWORD_LENGTH = 107;
    public static final String  ILLEGAL_PASSWORD_LENGTH_MESSSAGE = "密码长度太长";

    public static final int ILLEGAL_GAMECODE = 108;
    public static final String ILLEGAL_GAMECODE_MESSAGE = "非法的游戏代码";

    public static final int NULL_GAMECODE = 109;
    public static final String NULL_GAMECODE_MESSAGE = "非法的游戏代码";

    //login exceptions
    public static final int ERROR_NAMEORPASSWORD = 201;
    public static final String ERROR_NAMEORPASSWORD_MESSAGE = "错误的帐号或者密码";

    public static final int ALREADY_LOGON = 202;
    public static final String ALREADY_LOGON_MESSAGE = "帐号已经登录";

    public static final int NO_LOGON = 203;
    public static final String NO_LOGON_MESSAGE = "帐号没有登录";

    public static final int ERROR_GAME = 204;
    public static final String ERROR_GAME_MESSAGE = "错误的名字";

    public static final int ACCOUNT_FREEZE = 205;
    public static final String ACCOUNT_FREEZE_MESSAGE = "帐号被冻结";

    public static final int ACCOUNT_INVALID = 206;
    public static final String ACCOUNT_INVALID_MESSAGE = "帐号被封";

    public static final int TIME_LIMIT = 207;
    public static final String TIME_LIMIT_MESSAGE = "两次登录时间间隔太短，请稍后重试";
    
    public static final int ACCOUNT_FREEZE_PASSATTACK = 208;
    public static final String ACCOUNT_FREEZE_PASSATTACK_MESSAGE = "您的密码已多次输入错误，请稍后再试，为了您的账号安全，请及时修改您的账号密码";

    public static final int ILLEGAL_IMONEY_CARD = 301;
    public static final String ILLEGAL_IMONEY_CARD_MESSAGE = "此卡已失效";

    public static final int[] ERRORS = {
                                       UNKNOW,
                                       UNKNOW_ACCOUNT,
                                       UNKOWN_SOURCE,
                                       ILLEGAL_SESSIONID,
                                       NOT_ENOUGH_BALANCE,
                                       ILLEGAL_VALUE,
                                       ERROR_OLD_PASSWORD,
                                       ILLEGAL_PHONE,
                                       NOT_ENOUGH_TRUST_BALANCE,
                                       DUPLICATE_NAME,
                                       NULL_NAME,
                                       ILLEGAL_NAME_CHAR,
                                       ILLEGAL_LENGTH,
                                       NULL_PASSWORD,
                                       ILLEGAL_PASSWORD_CHAR,
                                       ILLEGAL_PASSWORD_LENGTH,
                                       ILLEGAL_GAMECODE,
                                       NULL_GAMECODE,
                                       ERROR_NAMEORPASSWORD,
                                       ALREADY_LOGON,
                                       NO_LOGON,
                                       ERROR_GAME,
                                       ACCOUNT_FREEZE,
                                       ACCOUNT_INVALID,
                                       TIME_LIMIT,
                                       ACCOUNT_FREEZE_PASSATTACK,
                                       ILLEGAL_IMONEY_CARD, 
    };

    public static final String[] ERROR_MESSAGES = {
                                                  UNKNOW_MESSAGE,
                                                  UNKNOW_ACCOUNT_MESSAGE,
                                                  UNKNOW_SOURCE_MESSAGE,
                                                  ILLEGAL_SESSIONID_MESSAGE,
                                                  NOT_ENOUGH_BALANCE_MESSAGE,
                                                  ILLEGAL_VALUE_MESSAGE,
                                                  ERROR_OLD_PASSWORD_MESSAGE,
                                                  ILLEGAL_PHONE_MESSAGE,
                                                  NOT_ENOUGH_TRUST_BALANCE_MESSAGE,
                                                  DUPLICATE_NAME_MESSAGE,
                                                  NULL_NAME_MESSAGE,
                                                  ILLEGAL_NAME_CHAR_MESSAGE,
                                                  ILLEGAL_LENGTH_MESSAGE,
                                                  NULL_PASSWORD_MESSAGE,
                                                  ILLEGAL_PASSWORD_CHAR_MESSAGE,
                                                   ILLEGAL_PASSWORD_LENGTH_MESSSAGE,
                                                  ILLEGAL_GAMECODE_MESSAGE,
                                                  NULL_GAMECODE_MESSAGE,
                                                  ERROR_NAMEORPASSWORD_MESSAGE,
                                                  ALREADY_LOGON_MESSAGE,
                                                  NO_LOGON_MESSAGE,
                                                  ERROR_GAME_MESSAGE,
                                                  ACCOUNT_FREEZE_MESSAGE,
                                                  ACCOUNT_INVALID_MESSAGE,
                                                  TIME_LIMIT_MESSAGE,
                                                  ACCOUNT_FREEZE_PASSATTACK_MESSAGE,
                                                  ILLEGAL_IMONEY_CARD_MESSAGE,
    };


    private static final Map<Integer,String> messages = new HashMap<Integer,String>();


    static{
        for(int i=0;i<ERRORS.length;i++){
            messages.put(ERRORS[i],ERROR_MESSAGES[i]);
        }
    }

    private static final String getErrorMesssage(int code){
        String msg = messages.get(code);
        if(msg==null){
            return ""+code;
        }
        return msg;
    }
    
    public static final String getErrorMesssage(ErrorMessage message){
    	if(message instanceof CmccErrorMessage){
    		return ((CmccErrorMessage)message).getMessage();
    	}else{
    		return getErrorMesssage(message.getCode());
    	}
    }
}
