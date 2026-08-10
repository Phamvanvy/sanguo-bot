package com.pip.server.billing.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecurityUtil {
	
	public static final String ATT_PHONE = "phone";
	public static final String ATT_ID = "id";
	public static final String ATT_IDCARD = "idcard";
	public static final String ATT_QUESTION = "question";
	public static final String ATT_ANSWER = "answer";
	public static final String ATT_MAIL = "mail";
	public static final String ATT_TYPE = "type";
	public static final String ATT_UID = "uid";
	
	public static final String TYPE_MAIL = "mail";
	public static final String TYPE_PHONE = "phone";
	public static final String TYPE_QNA = "qna";
	
	public static final String TYPE_SIMPLE = "simple";
	public static final String TYPE_BLUR = "blur";
	public static final String TYPE_AWARD = "award";
	
	public static final String ERROR_INVALIDREQUEST = "请求参数不正确";
	public static final String ERROR_ILLEGALID = "帐号不存在";
	public static final String ERROR_ILLEGALPHONE = "无效的手机号";
	public static final String ERROR_ILLEGALMAIL = "无效的Email地址";
	public static final String ERROR_ILLEGALQUESTION = "无效的问题";
	public static final String ERROR_ILLEGALANSWER = "无效的答案";
	public static final String ERROR_ILLEGALIDCARD = "无效的身份证号码";
	public static final String ERROR_BINDSTATUS = "相关字段没有绑定";
	public static final String ERROR_NOTENOUGHPAYMENT = "没有足够i币";
	public static final String ERROR_REQUEST = "请求参数错误，可能是有效时间已过。";
	public static final String ERROR_VERIFICATION_QNA = "答案错误";
	public static final String ERROR_PHONEBINDED = "您的帐号已经绑定过手机了";
	public static final String ERROR_MAILBINDED = "您的帐号已经绑定过Email了";
	public static final String ERROR_NOIDCARD = "必须先绑定身份证号码";
	
	public static final String MSG_OLDBINDPHONE = "有新手机号绑定";
	public static final String MSG_NEWPHONE = "绑定手机号";
	
	public static String trimParameter(HttpServletRequest req,String param){
		String result = req.getParameter(param);
		if(result!=null)
			return result.trim();
		return result;
	}
	
	public static int getId(HttpServletRequest req) throws IllegalIdException{
		String s = trimParameter(req,ATT_ID);
		if(s==null)
			throw new IllegalIdException();
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IllegalIdException();
		}
	}
	
	public static String getPhone(HttpServletRequest req){
		return trimParameter(req,ATT_PHONE);
	}
	
	public static String getIdcard(HttpServletRequest req){
		return trimParameter(req,ATT_IDCARD);
	}
	
	public static String getQuestion(HttpServletRequest req){
		return trimParameter(req,ATT_QUESTION);
	}
	
	public static String getAnswer(HttpServletRequest req){
		return trimParameter(req, ATT_ANSWER);
	}
	
	public static String getMail(HttpServletRequest req){
		return trimParameter(req, ATT_MAIL);
	}
	
	public static String getType(HttpServletRequest req){
		return trimParameter(req, ATT_TYPE);
	}
	
	public static String getUid(HttpServletRequest req){
		return trimParameter(req,ATT_UID);
	}
	
	public static void error(HttpServletResponse resp,String error) throws IOException{
		resp.getWriter().println("1");
		if(error!=null)
			resp.getWriter().println(error);
	}
	
	public static void ok(HttpServletResponse resp,String msg) throws IOException{
		resp.getWriter().println("0");
		if(msg!=null){
			resp.getWriter().println(msg);
		}
	}
}
