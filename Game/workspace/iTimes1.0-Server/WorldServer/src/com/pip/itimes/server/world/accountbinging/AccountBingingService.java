package com.pip.itimes.server.world.accountbinging;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.StageService;
import java.net.URLEncoder;

public class AccountBingingService implements Runnable
	{
	//打印日志
    private Logger log = Logger.getLogger(AccountBingingService.class);
    //要处理的AccountBingingData数据的队列
    private static BlockingQueue<AccountBingingData> dataQueue = new LinkedBlockingQueue<AccountBingingData>();
    private static BlockingQueue<AccountBingingData> dataQueueRepeat = new LinkedBlockingQueue<AccountBingingData>();

    protected ConnectService connectservice;
    protected StageService stageService;
    protected MailService mailService;

    //DispatchDataProcessor的构造函数
    AccountBingingData bingingData = new AccountBingingData();

    private String Billingip = "http://211.151.99.71:8102";//http://218.206.80.185:8102
    //加入队列
    public void addToQueue(AccountBingingData data) {
        try {
            // 将要处理的AccountBingingData数据放到队列中。
            dataQueue.put(data);
            log.debug("将AccountBingingData数据（" + data.getAccountID() + "）放入队列成功。");
        } catch (InterruptedException ex) {
            log.error("将AccountBingingData数据（" + data.getAccountID() + "）放入队列出错。", ex);
        }
    }

    public void addToQueueRepeat(AccountBingingData data) {
        try {
            // 将要处理的AccountBingingData数据放到队列中。
            dataQueueRepeat.put(data);
            log.debug("将AccountBingingData数据（" + data.getAccountID() + "）放入Repeat队列成功。");
        } catch (InterruptedException ex) {
            log.error("将AccountBingingData数据（" + data.getAccountID() + "）放入Repeat队列出错。", ex);
        }
    }
    //
    public void launch() {
        Thread thread = new Thread(this);
        thread.start();
    }
    public String bingingstatus(String AccountID) throws Exception {

		//发送绑定信息
		int code = -1;
		BufferedReader br = null;
		PostMethod method = new PostMethod(Billingip + "/status");
    	method.getParams().setContentCharset("utf-8");
        method.addRequestHeader( "Connection", "close");
		method.setParameter("id", AccountID);
		method.setParameter("type", "simple");
		try{
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			log.info("AccountID["+AccountID+"]发送校验"+code);
			br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));

		}catch(Exception ex){
			log.error(ex,ex);

		}finally{

		}
		if (code == 200){
    		//发送成功，提示用户请求已经发出。
    		String line = br.readLine();
            int retCode = Integer.parseInt(line);
            if (retCode == 0) {//取成功
            	line = br.readLine();
            	return line;
            }else if (retCode == 1){
            	return "取得绑定状态失败。";
            }
    	}else{
    		return "取得绑定状态失败。(通讯失败)";
    	}
		method.releaseConnection();
		return "取得绑定状态失败。";
    }

    //执行
    public void process() {
        try {
        	if (!dataQueue.isEmpty()){
        		AccountBingingData data;
        		data = dataQueue.take();

        		//发送绑定信息
        		int statsint = 0;
        		int code = -1;
        		BufferedReader br = null;
        		PostMethod method = new PostMethod(Billingip + "/status");
	        	method.getParams().setContentCharset("utf-8");
                method.addRequestHeader( "Connection", "close");
				method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
				method.setParameter("type", "simple");
				try{
					HttpClient httpclient = new HttpClient();
					httpclient.getHttpConnectionManager().getParams()
							.setConnectionTimeout(30000);
					httpclient.getParams().setSoTimeout(30000);
					code = httpclient.executeMethod(method);
					log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]发送校验"+code);
					br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));

				}catch(Exception ex){
					log.error(ex,ex);

				}finally{

				}
				String line = "";
				if (code == 200){
	        		//发送成功，提示用户请求已经发出。
					line = br.readLine();
	                int retCode = Integer.parseInt(line);
	                if (retCode == 0) {//取成功
	                	line = br.readLine();
	                	switch (data.getType()) {

	                    case 1://手机
	                    	if ("1".equalsIgnoreCase(line.substring(0, 1))){
	                    		connectservice.sendMessage(data.getPlayerID(),"您已经绑定过手机了哦。");
	    	                	statsint = -1;
	                    	}
	                    	break;
	                    case 2://邮箱
	                    	if ("1".equalsIgnoreCase(line.substring(3, 4))){
	                    		connectservice.sendMessage(data.getPlayerID(),"您已经绑定过邮箱了哦。");
	    	                	statsint = -1;
	                    	}
	                    	break;
	                    case 3://身份证
	                    	if ("1".equalsIgnoreCase(line.substring(1, 2))){
	                    		connectservice.sendMessage(data.getPlayerID(),"您已经绑定过身份证了哦。");
	    	                	statsint = -1;
	                    	}
	                    	break;
	                    case 4://自定义问题和答案
	                    	if ("1".equalsIgnoreCase(line.substring(2, 3))){
	                    		connectservice.sendMessage(data.getPlayerID(),"您已经绑定过提示问题和答案了哦。");
	    	                	statsint = -1;
	                    	}
	                    	break;
	                	}
	                }else if (retCode == 1){
	                	connectservice.sendMessage(data.getPlayerID(),"您的绑定申请提交失败，请稍后再试。");
	                	statsint = -1;
	                }
	        	}else{
	        		statsint = -1;
	        		connectservice.sendMessage(data.getPlayerID(),"您的绑定申请提交失败，请稍后再试。");
	        	}
				method.releaseConnection();
				if (statsint == 0){//校验正常
					//二次校验
					if (!"0000".equalsIgnoreCase(line)){
						if ("1".equalsIgnoreCase(line.substring(1, 2))){//身份证
							byte[] bytes = stageService.getTaskBytes((short) 31001,
	                                new String[] {"您需要输入绑定的身份证号才可以继续绑定哦，现在就输入?\n1.是\n2.不记得了，一会儿再说吧。",
	                                "身份证号:", "accountbindingrepeat 3 " + data.getType() + " " + data.getUsestring() + " " + data.getUsestringtwo() + " "});
	                        UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                GET_FILE_OK);
	                        seg.writeShort((short) 31001);
	                        seg.writeShort((short) 2);
	                        seg.write(bytes);
	                        connectservice.writeTo(seg, data.getPlayerID());
	                        statsint = 1;
						}else if ("1".equalsIgnoreCase(line.substring(0, 1))){//手机
							byte[] bytes = stageService.getTaskBytes((short) 31001,
	                                new String[] {"您需要输入绑定的手机号才可以继续绑定哦，现在就输入?\n1.是\n2.不记得了，一会儿再说吧。",
	                                "手机号:", "accountbindingrepeat 1 " + data.getType() + " " + data.getUsestring() + " " + data.getUsestringtwo() + " "});
	                        UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                GET_FILE_OK);
	                        seg.writeShort((short) 31001);
	                        seg.writeShort((short) 2);
	                        seg.write(bytes);
	                        connectservice.writeTo(seg, data.getPlayerID());
	                        statsint = 1;
						}else if ("1".equalsIgnoreCase(line.substring(3, 4))){//邮箱
							byte[] bytes = stageService.getTaskBytes((short) 31001,
	                                new String[] {"您需要输入绑定的邮箱地址才可以继续绑定哦，现在就输入?\n1.是\n2.不记得了，一会儿再说吧。",
	                                "邮箱地址:", "accountbindingrepeat 2 " + data.getType() + " " + data.getUsestring() + " " + data.getUsestringtwo() + " "});
	                        UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                GET_FILE_OK);
	                        seg.writeShort((short) 31001);
	                        seg.writeShort((short) 2);
	                        seg.write(bytes);
	                        connectservice.writeTo(seg, data.getPlayerID());
							statsint = 1;
						}else if ("1".equalsIgnoreCase(line.substring(2, 3))){//自定义问题
							method = new PostMethod(Billingip + "/status");
							method.getParams().setContentCharset("utf-8");
							method.addRequestHeader( "Connection", "close");
	    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
	    					method.setParameter("type", "blur");
	    					try{
	    						HttpClient httpclient = new HttpClient();
	    						httpclient.getHttpConnectionManager().getParams()
	    								.setConnectionTimeout(30000);
	    						httpclient.getParams().setSoTimeout(30000);
	    						code = httpclient.executeMethod(method);
	    						log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]发送绑定"+code);
	    						br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));

	    					}catch(Exception ex){
	    						log.error(ex,ex);
	    					}finally{

	    					}
	    					if (code == 200){
	    		        		//发送成功，提示用户请求已经发出。
	    		        		String line1 = br.readLine();
	    		        		if ("0".equalsIgnoreCase(line1)){
	    		        			line1 = br.readLine();//手机号
		    		        		line1 = br.readLine();//身份证
		    		        		line1 = br.readLine();//问题
		    		        		if (line1 != null){
		    		        			if (!"".equalsIgnoreCase(line1)){
		    		        				byte[] bytes = stageService.getTaskBytes((short) 31001,
		    		                                new String[] {"您需要输入绑定的自定义问题的答案才可以继续绑定哦，现在就输入?\n1.是\n2.不记得了，一会儿再说吧。",
		    		                                "自定义问题:" + line1 + "\n答案：", "accountbindingrepeat 4 " + data.getType() + " " + data.getUsestring() + " " + data.getUsestringtwo() + " " + line1 + " "});
		    		                        UWAPSegment seg = new UWAPSegment(ClientConstants.
		    		                                GET_FILE_OK);
		    		                        seg.writeShort((short) 31001);
		    		                        seg.writeShort((short) 2);
		    		                        seg.write(bytes);
		    		                        connectservice.writeTo(seg, data.getPlayerID());
		    		        				statsint = 1;
		    		        			}
		    		        		}
	    		        		}
	    					}else{
	    						String line1 = br.readLine();//失败原因
			                	connectservice.sendMessage(data.getPlayerID(),line1);
	    					}
	    					method.releaseConnection();
						}
					}
				}
				if (statsint == 0){//校验正常
					method = new PostMethod(Billingip + "/modify");
                	method.getParams().setContentCharset("utf-8");                	
					switch (data.getType()) {
                    case 1://手机
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("phone", "");
                    	break;
                    case 2://邮箱
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("mail", data.getUsestring());
                    	break;
                    case 3://身份证
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("idcard", data.getUsestring());
                    	break;
                    case 4://自定义问题和答案
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("question", data.getUsestring());
    					method.setParameter("answer", data.getUsestringtwo());
                    	break;
                	}
					try{
						HttpClient httpclient = new HttpClient();
						httpclient.getHttpConnectionManager().getParams()
								.setConnectionTimeout(30000);
						httpclient.getParams().setSoTimeout(30000);
						code = httpclient.executeMethod(method);
						log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]发送绑定"+code);
						br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));

					}catch(Exception ex){
						log.error(ex,ex);
					}finally{

					}
					if (code == 200){
		        		//发送成功，提示用户请求已经发出。
		        		line = br.readLine();
		                int retCode = Integer.parseInt(line);
		                if (retCode == 0) {//取成功
		                	IItemTemplate itemtemplate = null;
		                	byte[] att = null;
		                	switch (data.getType()) {
		                    case 1://手机
		                    	line = br.readLine();
		                    	//调发短信的脚本
		                    	byte[] bytes = stageService.getTaskBytes((short) 31031,new String[] {"1",line});
								UWAPSegment seg = new UWAPSegment(ClientConstants.
								                                 GET_FILE_OK);
								seg.writeShort((short) 31031);
								seg.writeShort((short) 2);
								seg.write(bytes);
								connectservice.writeTo(seg, data.getPlayerID());
								connectservice.sendMessage(data.getPlayerID(),"绑定手机申请提交成功，请确认您的手机可以正常发送短信。");

		                    	break;
		                    case 2://邮箱
		                    	connectservice.sendMessage(data.getPlayerID(),"现在需要你去邮箱里确认哦，才可以最终完成绑定，赶紧去吧。");
		    	                break;
		                    case 3://身份证
		                    	connectservice.sendMessage(data.getPlayerID(),"恭喜您绑定成功，您的奖励已通过精灵速递发送。请稍候查收。");
		                    	//送东西
//		                    	itemtemplate = Items.getTemplate(550018);//家园地区邀请函
		                    	itemtemplate = Items.getTemplate(113);//特效急救恢复药水
		                    	att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),10);
//		                        mailService.sendMail(data.getPlayerID(), data.getPlayername(), -1, "系统",
//		                        		itemtemplate.getName() + "*" + 10, "恭喜您绑定身份证成功，系统特奖励您的礼物在附件里。请提取。", att, 0, true);
		                        mailService.sendMail(data.getPlayerID(), data.getPlayername(), -1, "系统",
		                        		itemtemplate.getName() + "*" + 10, "恭喜您绑定身份证成功，系统特奖励您的礼物在附件里。请提取。", att, 0, true);

		    	                break;
		                    case 4://自定义问题和答案
		                    	connectservice.sendMessage(data.getPlayerID(),"恭喜您绑定成功，您的奖励已通过精灵速递发送。请稍候查收。");
		                    	//送东西
//		                    	itemtemplate = Items.getTemplate(200613);//优质宠物金蛋
		                    	//2011年4月12日14:13:38 金蛋开出来的宠物可以交易 改成不可交易的物品
		                    	itemtemplate = Items.getTemplate(200885);//顶级精华定向包
		                    	att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),1);
		                        mailService.sendMail(data.getPlayerID(), data.getPlayername(), -1, "系统",
		                        		itemtemplate.getName() + "*" + 1, "恭喜您绑定自定义问题成功，系统特奖励您的礼物在附件里。请提取。", att, 0, true);

		                    	break;
		                	}
		                	log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]binging Success!type["+data.getType()+ "]");
		                }else{
		                	line = br.readLine();//失败原因
		                	connectservice.sendMessage(data.getPlayerID(),line);
		                }
					}
					method.releaseConnection();
				}
        	}else if (!dataQueueRepeat.isEmpty()){//二次校验输入后发送绑定申请
        		AccountBingingData data;
        		data = dataQueueRepeat.take();
        		//发送绑定信息
        		int statsint = 0;
        		int code = -1;
        		BufferedReader br = null;
        		PostMethod method = new PostMethod(Billingip + "/valid");
	        	method.getParams().setContentCharset("utf-8");
                method.addRequestHeader( "Connection", "close");
				method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
				switch (data.getTypeRepeat()) {
                case 1://手机
                	method.setParameter("phone", data.getUsestringRepeat());
                	break;
                case 2://邮箱
                	method.setParameter("mail", data.getUsestringRepeat());
                	break;
                case 3://身份证
                	method.setParameter("idcard", data.getUsestringRepeat());
                	break;
                case 4://自定义问题和答案
                	method.setParameter("question", data.getUsestringRepeat());
                	method.setParameter("answer", data.getUsestringRepeattwo());
                	break;
            	}

				try{
					HttpClient httpclient = new HttpClient();
					httpclient.getHttpConnectionManager().getParams()
							.setConnectionTimeout(30000);
					httpclient.getParams().setSoTimeout(30000);
					code = httpclient.executeMethod(method);
					log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]发送校验"+code);
					br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));

				}catch(Exception ex){
					log.error(ex,ex);

				}finally{

				}
				String line = "";
				if (code == 200){
	        		//发送成功，提示用户请求已经发出。
					line = br.readLine();
	                int retCode = Integer.parseInt(line);
	                if (retCode == 0) {//二次校验成功

	                }else if (retCode == 1){
	                	connectservice.sendMessage(data.getPlayerID(),"您输入的绑定信息不正确哦，无法继续绑定。");
	                	statsint = -1;
	                }
	        	}else{
	        		statsint = -1;
	        		connectservice.sendMessage(data.getPlayerID(),"网络故障，请稍后再试。");
	        	}
				method.releaseConnection();

				if (statsint == 0){//校验正常
					method = new PostMethod(Billingip + "/modify");
                	method.getParams().setContentCharset("utf-8");
					switch (data.getType()) {
                    case 1://手机
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("phone", "");
                    	break;
                    case 2://邮箱
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("mail", data.getUsestring());
                    	break;
                    case 3://身份证
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("idcard", data.getUsestring());
                    	break;
                    case 4://自定义问题和答案
                    	method.addRequestHeader( "Connection", "close");
    					method.setParameter("id", Integer.valueOf(data.getAccountID()).toString());
    					method.setParameter("question", data.getUsestring());
    					method.setParameter("answer", data.getUsestringtwo());
                    	break;
                	}
					try{
						HttpClient httpclient = new HttpClient();
						httpclient.getHttpConnectionManager().getParams()
								.setConnectionTimeout(30000);
						httpclient.getParams().setSoTimeout(30000);
						code = httpclient.executeMethod(method);
						log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]发送绑定"+code);
						br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));

					}catch(Exception ex){
						log.error(ex,ex);
					}finally{

					}
					if (code == 200){
		        		//发送成功，提示用户请求已经发出。
		        		line = br.readLine();
		                int retCode = Integer.parseInt(line);
		                if (retCode == 0) {//取成功
		                	IItemTemplate itemtemplate = null;
		                	byte[] att = null;
		                	switch (data.getType()) {
		                    case 1://手机
		                    	line = br.readLine();
		                    	//调发短信的脚本
		                    	byte[] bytes = stageService.getTaskBytes((short) 31031,new String[] {"1",line});
								UWAPSegment seg = new UWAPSegment(ClientConstants.
								                                 GET_FILE_OK);
								seg.writeShort((short) 31031);
								seg.writeShort((short) 2);
								seg.write(bytes);
								connectservice.writeTo(seg, data.getPlayerID());
								connectservice.sendMessage(data.getPlayerID(),"绑定手机申请提交成功，请确认您的手机可以正常发送短信。");

		                    	break;
		                    case 2://邮箱
		                    	connectservice.sendMessage(data.getPlayerID(),"现在需要你去邮箱里确认哦，才可以最终完成绑定，赶紧去吧。");
		    	                break;
		                    case 3://身份证
		                    	connectservice.sendMessage(data.getPlayerID(),"恭喜您绑定成功，您的奖励已通过精灵速递发送。请稍候查收。");
		                    	//送东西
		                    	itemtemplate = Items.getTemplate(550018);//家园地区邀请函
		                    	att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),10);
		                        mailService.sendMail(data.getPlayerID(), data.getPlayername(), -1, "系统",
		                        		itemtemplate.getName() + "*" + 10, "恭喜您绑定身份证成功，系统特奖励您的礼物在附件里。请提取。", att, 0, true);

		    	                break;
		                    case 4://自定义问题和答案
		                    	connectservice.sendMessage(data.getPlayerID(),"恭喜您绑定成功，您的奖励已通过精灵速递发送。请稍候查收。");
		                    	//送东西
//		                    	itemtemplate = Items.getTemplate(200613);//优质宠物金蛋
		                    	//2011年4月12日14:13:38 金蛋开出来的宠物可以交易 改成不可交易的物品
		                    	itemtemplate = Items.getTemplate(200885);//顶级精华定向包
		                    	att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),1);
		                        mailService.sendMail(data.getPlayerID(), data.getPlayername(), -1, "系统",
		                        		itemtemplate.getName() + "*" + 1, "恭喜您绑定自定义问题成功，系统特奖励您的礼物在附件里。请提取。", att, 0, true);

		                    	break;
		                	}
		                	log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]binging Success!type["+data.getType()+ "]");
		                }else{
		                	line = br.readLine();//失败原因
		                	connectservice.sendMessage(data.getPlayerID(),line);
		                }
					}
					method.releaseConnection();
				}
        	}

        } catch (InterruptedException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (Exception ex) {
            log.error(ex, ex);
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
        	try {
                Thread.sleep(1*1000L);
            } catch (InterruptedException ex) {
            }
            try {
            	process();
        	} catch (Exception ex1) {
        		ex1.printStackTrace();
            }

        }

    }
    public AccountBingingService() throws Exception {

        //new Thread(this).start();
    }
    public void setConnectService(ConnectService connectService) {
        this.connectservice = connectService;
    }

	public void setStageService(StageService stageService) {
		this.stageService = stageService;
	}
	public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }
}
