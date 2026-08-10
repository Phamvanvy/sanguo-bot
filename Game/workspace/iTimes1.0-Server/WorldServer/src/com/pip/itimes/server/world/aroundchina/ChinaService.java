package com.pip.itimes.server.world.aroundchina;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.ConnectSession;

public class ChinaService implements Runnable
	{
	//打印日志
    private Logger log = Logger.getLogger(ChinaService.class);
    //要处理的ChinaAroundData数据的队列
    private static BlockingQueue<ChinaAroundData> dataQueue = new LinkedBlockingQueue<ChinaAroundData>();

    public Configuration configuration;
    protected ConnectService connectservice;
    private long lastCheckTime = Utils.getTodayStart();
    private long UNIT_TIME = 1000L * 60L * 3L;
    
    // 充值保有的I币
    private int[] IMONEY_RETAIN = new int[] {
		// 10元
    	3600,
    	// 20元
    	7200,
    	// 30元
    	10800,
    	// 50元
    	18000,
    	// 100元
    	36000,
    	// 300元
    	108000,
    	// 500元
    	180000,	
    };
    
    // 充值获得的优惠I币(默认值，此后每3分钟后都会去BILLING同步一次)
    public static String[] IMONEY_ADDITIONAL = new String[] {
    	// 10元
    	"36",
    	// 20元
    	"144",
    	// 30元
    	"216",
    	// 50元
    	"720",
    	// 100元
    	"2880",
    	// 300元
    	"17280",
    	// 500元
    	"36000",
    };
    
    //DispatchDataProcessor的构造函数
    ChinaAroundData CNData = new ChinaAroundData();


    //加入队列
    public void addToQueue(ChinaAroundData data) {
        try {
            // 将要处理的ChinaAroundData数据放到队列中。
            dataQueue.put(data);
            log.debug("将ChinaAroundData数据（" + data.getUsername() + "）放入队列成功。");
        } catch (InterruptedException ex) {
            log.error("将ChinaAroundData数据（" + data.getUsername() + "）放入队列出错。", ex);
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
        		ChinaAroundData data;
        		Header header = null;
        		data = dataQueue.take();
        		configuration = new PropertiesConfiguration(
                "config.properties");
        		//发送充值信息
        		int code = -1;
	        	PostMethod method ;
//	        	data.setType(1);
	        	if (data.getType() != 3){
		        	//不管data.getType()是0还是1，神州行还是呱呱通，均走易宝支付平台(以下均修改完毕）
	        		method = new PostMethod("http://211.151.99.71:8102/yeepay_order2");
	        		method.addRequestHeader( "Connection", "close");
					method.setParameter("id", Integer.valueOf(data.getUserID()).toString());
					method.setParameter("name", data.getUsername());
					method.setParameter("cardsn",data.getSerialnum());
					method.setParameter("password",data.getPassword());
					method.setParameter("money",Integer.valueOf(data.getAmount()).toString());
	                                method.setParameter("game","1");
					method.setParameter("returnhttp","http://" + configuration.getString("localip")
															+ ":" + configuration.getString("webport")
															+ "/chinarun");
					method.setParameter("channel", data.getChannel());
	        	}else{
//	        	}else{//刮刮通
//	        		method = new PostMethod("http://218.206.80.185:8102/19payd_order");
//	        		method.addRequestHeader( "Connection", "close");
//					method.setParameter("id", Integer.valueOf(data.getUserID()).toString());
//					method.setParameter("name", data.getUsername());
//					method.setParameter("amount",Integer.valueOf(data.getAmount() * 100).toString());//金额
//					method.setParameter("gamecode","1");//gamecode 
//					method.setParameter("cardno",data.getSerialnum());
//					method.setParameter("cardpass",data.getPassword());
//					method.setParameter("cardtype","5");//0 - 全国联通一卡充，1 - 全国移动充值卡，2 - 辽宁移动电话交费卡，3 - 江苏移动充值卡，4 - 浙江移动缴费券，5 - 福建移动呱呱通充值卡
//					method.setParameter("channel", data.getChannel());
//					method.setParameter("returnhttp","http://" + configuration.getString("localip")
//															+ ":" + configuration.getString("webport")
//															+ "/chinarun");
//	        	}
				
		        	//易宝切换成19pay渠道2011年9月23日9:48:19
					method = new PostMethod("http://211.151.99.71:8102/19payd_order");
	        		method.addRequestHeader( "Connection", "close");
					method.setParameter("id", Integer.valueOf(data.getUserID()).toString());
	//				method.setParameter("name", data.getUsername());
					method.setParameter("amount",Integer.valueOf(data.getAmount() * 100).toString());//金额
					method.setParameter("gamecode","1");//gamecode 
					method.setParameter("cardno",data.getSerialnum());
					method.setParameter("cardpass",data.getPassword());
					method.setParameter("cardtype","6");//0 - 全国联通一卡充，1 - 全国移动充值卡，2 - 辽宁移动电话交费卡，3 - 江苏移动充值卡，4 - 浙江移动缴费券，5 - 福建移动呱呱通充值卡，6 - 中国电信充值付费卡
					method.setParameter("channel", data.getChannel());
					method.setParameter("returnhttp","http://" + configuration.getString("localip")
															+ ":" + configuration.getString("webport")
															+ "/chinarun");
	        	}
				
	        	String[]  resultValue = null;
				try{
					HttpClient httpclient = new HttpClient();
					httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
					httpclient.getParams().setSoTimeout(30000);
					code = httpclient.executeMethod(method);
//					header = method.getResponseHeader("result");
					if (data.getType() != 3){
						header = method.getResponseHeader("result");
					}else{
						String resultStr = new String(method.getResponseBody(),"UTF8");
						resultValue = resultStr.split("\n");
					}
					log.info("UserID["+Long.valueOf(data.getUserID()).toString()+"]发送"+code+"accountid"+Integer.valueOf(data.getUserID()).toString()+"name="+data.getUsername()+"版本号："+data.getChannel());
				}catch(Exception ex){
					log.error(ex,ex);
				}finally{
					method.releaseConnection();
				}
				ConnectSession[] cc = connectservice.getConnectSession();
	        	if (code == 200){
	        		//发送成功，提示用户请求已经发出。
	        		if(data.getType() != 3){
		        		if ("200".equals(header.getValue())){
		        			connectservice.sendMessage(data.getID(),"您的充值申请已提交成功，请稍等几分钟。");
		        		}else{
		        			connectservice.sendMessage(data.getID(),"您的充值申请提交失败，请核对序列号和密码重新输入。");
		        		}
	        		}else{
	        			if ("0".equals(resultValue[0])){
		        			connectservice.sendMessage(data.getID(),"您的充值申请已提交成功，请稍等几分钟。");
		        		}else{
//		        			log.info("resultValue[0] = " + resultValue[0]);
		        			connectservice.sendMessage(data.getID(),"您的充值申请提交失败，请核对序列号和密码重新输入。");
//		        			log.info("resultValue[1] = " + resultValue[1]);
		        			log.info("china server error " + resultValue[1].toString());
		        		}
	        		}
	        	}else{
	        		connectservice.sendMessage(data.getID(),"您的充值申请提交失败，请核对序列号和密码重新输入。");
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
    
  	// 每3分钟获取当前神州行充值兑换i币比例
    public void getImoneyRateProcess() {
        try {
			int code = -1;
			PostMethod method = new PostMethod("http://211.151.99.71:8102/yeepay_charge_rate");
			try {
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				httpclient.getParams().setSoTimeout(30000);
				code = httpclient.executeMethod(method);
				if (code == 200) {
					String result = method.getResponseBodyAsString();
					if (result != null) {
						String[] tmpImoneyRate = result.split("\n");
						for (int i = 0; i < tmpImoneyRate.length; i++) {
							String[] imoneyRate = tmpImoneyRate[i].split(" ");
							int add = Integer.parseInt(imoneyRate[1]) - IMONEY_RETAIN[i];
							IMONEY_ADDITIONAL[i] = add + "";
						}
					}
				}
			} catch(Exception ex) {
				log.error(ex,ex);
			} finally {
				method.releaseConnection();
			}
        } catch (Exception ex) {
            log.error(ex, ex);
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
        	try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException ex) {
            }
            try {
            	long now = System.currentTimeMillis();
            	if (now - lastCheckTime > UNIT_TIME) {
            		getImoneyRateProcess();
            		lastCheckTime = now;
            	}
            	process();
        	} catch (Exception ex1) {
        		ex1.printStackTrace();
            }

        }

    }
    public ChinaService() throws Exception {
    	 getImoneyRateProcess();
        //new Thread(this).start();
    }
    /**
     * 单态的
    private ChinaService(){}

    private static ChinaService instance = new ChinaService();

    //这里提供了一个供外部访问本class的静态方法，可以直接访问　　
    public static ChinaService getInstance() {
    	return instance;
    }

    **/
    public void setConnectService(ConnectService connectService) {
        this.connectservice = connectService;
    }
}
