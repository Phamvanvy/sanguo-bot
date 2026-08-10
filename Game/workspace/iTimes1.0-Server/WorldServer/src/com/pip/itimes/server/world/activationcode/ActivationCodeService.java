package com.pip.itimes.server.world.activationcode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.ActivationCode;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.StageService;
import java.net.URLEncoder;

public class ActivationCodeService implements Runnable
	{
	//打印日志
    private Logger log = Logger.getLogger(ActivationCodeService.class);
    //要处理的AccountBingingData数据的队列
    private static BlockingQueue<ActivationCodeData> dataQueue = new LinkedBlockingQueue<ActivationCodeData>();
    private static BlockingQueue<ActivationCodeData> dataQueueRepeat = new LinkedBlockingQueue<ActivationCodeData>();

    protected ConnectService connectservice;
    protected StageService stageService;
    protected MailService mailService;

    //DispatchDataProcessor的构造函数
    ActivationCodeData activationcode = new ActivationCodeData();

    private String serverip = "http://211.151.99.71:8102";
    //加入队列
    public void addToQueue(ActivationCodeData data) {
        try {
            // 将要处理的ActivationCodeData数据放到队列中。
            dataQueue.put(data);
            log.debug("将ActivationCodeData数据（" + data.getAccountID() + "）放入队列成功。");
        } catch (InterruptedException ex) {
            log.error("将ActivationCodeData数据（" + data.getAccountID() + "）放入队列出错。", ex);
        }
    }

    public void addToQueueRepeat(ActivationCodeData data) {
        try {
            // 将要处理的AccountBingingData数据放到队列中。
            dataQueueRepeat.put(data);
            log.debug("将ActivationCodeData数据（" + data.getAccountID() + "）放入Repeat队列成功。");
        } catch (InterruptedException ex) {
            log.error("将ActivationCodeData数据（" + data.getAccountID() + "）放入Repeat队列出错。", ex);
        }
    }
    //
    public void launch() {
        Thread thread = new Thread(this);
        thread.start();
    }
    

    //执行
    public void process() {
        try {
        	if (!dataQueue.isEmpty()){
        		ActivationCodeData data;
        		data = dataQueue.take();
        		int code = -1;
        		BufferedReader br = null;
        		//发送绑定信息
        		PostMethod method = new PostMethod(serverip + "/card_check");
            	method.getParams().setContentCharset("utf-8");
                method.addRequestHeader( "Connection", "close");
        		method.setParameter("cardno", data.getActivationcode());//兑换卡号
        		method.setParameter("gamecode", String.valueOf(data.getGamecode()));//游戏代码，1 - 幻想
        		method.setParameter("cardtype", String.valueOf(data.getType()));//允许的兑换物品类型
        		method.setParameter("accountid", String.valueOf(data.getAccountID()));// 帐号ID
				try{
					HttpClient httpclient = new HttpClient();
					httpclient.getHttpConnectionManager().getParams()
							.setConnectionTimeout(30000);
					httpclient.getParams().setSoTimeout(30000);
					code = httpclient.executeMethod(method);
					log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]发送校验"+code+"激活码："+data.getActivationcode());
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
	                	log.info("AccountID["+Long.valueOf(data.getAccountID()).toString()+"]ok"+"激活码："+data.getActivationcode());
	                	int itemstype = Integer.valueOf(br.readLine()).intValue();
	                	if (itemstype>0){
	                		itemstype = ActivationCode.getactivationcode(itemstype).getItemsid();
	                		//送东西
	                		IItemTemplate itemtemplate = null;
	                		byte[] att = null;
	                    	itemtemplate = Items.getTemplate(itemstype);//按照type区分给与的物品id
	                    	if (itemtemplate != null){
	                    		att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),1);
		                        mailService.sendMail(data.getPlayerID(), "", -1, "系统",
		                        		itemtemplate.getName() + "*" + 1, 
		                        		"恭喜您兑换成功，您的兑换的道具在附件里。请提取。", att, 0, true);
		                        connectservice.sendMessage(data.getPlayerID(),"兑换成功，礼品已经通过精灵速递发放给您了，请查收。");
	                    	}
	                	}
	                }else if (retCode == 1){
	                	connectservice.sendMessage(data.getPlayerID(),"您输入的激活码不正确，请核实后再试。");
	                }else if (retCode == 2){
	                	connectservice.sendMessage(data.getPlayerID(),"您输入的激活码不是“明珠幻想”的激活码，请核实后再试。");
	                }else if (retCode == 3){
	                	connectservice.sendMessage(data.getPlayerID(),"您输入的激活码在这个兑换员处无法兑换，请找另一个兑换员。");
	                }else if (retCode == 4){
	                	connectservice.sendMessage(data.getPlayerID(),"您输入的激活码已经兑换过了。");
	                }else if (retCode == 5){
	                	connectservice.sendMessage(data.getPlayerID(),"您输入的激活码已过期。");
	                }
	        	}else{
	        		connectservice.sendMessage(data.getPlayerID(),"您的兑换申请提交失败，请稍后再试。");
	        	}
				
				method.releaseConnection();
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
    public ActivationCodeService() throws Exception {

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
