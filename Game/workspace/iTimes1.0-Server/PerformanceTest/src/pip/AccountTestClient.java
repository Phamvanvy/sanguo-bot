package pip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import com.pip.wulin.server.io.UWAPApp;
import com.pip.wulin.server.io.UWAPConnection;
import com.pip.wulin.server.io.UWAPData;
import com.pip.wulin.server.io.UWAPDataListener;
import com.pip.wulin.server.io.UWAPSegment;
import com.pip.wulin.server.io.UWAPSocketSConnection;


public class AccountTestClient
    implements Runnable, UWAPApp, UWAPDataListener
{
    private Account account;
	private int id;
	private String host;
	private int port;
    private String realHost;
    private int realPort;
	UWAPConnection connection;
	private boolean connected = false;
	private boolean logined = false;
	private boolean reged = false;
	private boolean userLogined = false;
	private boolean justCreateCharactor = false;
	private boolean downloadTask = false;
	
	private static final String version = "1.22.0";
	
	private static HashMap<Account, Account> accountTable = new HashMap<Account, Account>();

    public AccountTestClient(int id, String host, int port, String realHost, int realPort)
    {
        this.host = host;
        this.port = port;
        this.id = id;
        this.realHost = realHost;
        this.realPort = realPort;
        
        this.account = getAccountFromTable();
        
        if(this.account == null){
            this.account = new Account();
        }
    }
    
    public static int ip2num(String host) {
        String[] secs = host.split("\\.");
        return (Integer.parseInt(secs[0]) << 24) | (Integer.parseInt(secs[1]) << 16) |
            (Integer.parseInt(secs[2]) << 8) | Integer.parseInt(secs[3]);
    }

    public void run()
    {
    	int counter = 0;
        try
        {
            connect();
            while (!connected) {
            	Thread.sleep(100);
            }
            
            if(account.getName() == null){
                System.out.println("Agent[" + id + "]开始注册");
                quickReg();
                
                while(!reged){
                    Thread.sleep(100);
                }
            }
            
            login();
            System.out.println("Agent[" + id + "]开始登录 ： " + account.getName() + " , " + account.getPassword());
            
            while (!logined) {
            	Thread.sleep(100);
            }
            
            userLogin();
            
            while (!userLogined) {
            	Thread.sleep(100);
            }

            while (true) {
            	Thread.sleep(5000);
//                Thread.sleep(6000);
//            	sendMove();
//            	syncTime();
            	
            	ishopTrade();
            	
            	counter++;
//            	if ((counter % 40) == 1) {  // 每2分钟发一次世界聊天
//            		sendChat();
//            	}
//            	if ((counter % 80) == 21) {  // 每4分钟下载一次关卡
//            		downloadStage();
//            	}
//            	if ((counter % 20) == 11) {  // 每1分钟发送一次战斗结束 
//            		sendBattleResult();
//            	}
//            	if ((counter % 80) == 41) {  // 每4分钟取一次BBS
//            		getBBSList();
//            	}
//            	if ((counter % 40) == 31) {  // 每2分钟取一次邮件列表
//            		getMailList();
//            	}
            	
            	if(counter > 10){
            	    connection.close();
            	    break;
            	}
            }
            
            System.out.println("Agent[" + id + "]正常退出");
        }
        catch(Exception ex)
        {
        	System.out.println("Agent[" + id + "]异常退出");
            ex.printStackTrace();
        }
    }

    private void connect() throws Exception
    {
        Socket socket = new Socket(host, port);
        UWAPSocketSConnection connection = new UWAPSocketSConnection(socket, this);
        
        if(realHost != null && realHost.trim().length() > 0){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(ip2num(realHost));
            dos.writeShort(realPort);
            dos.flush();
            connection.outStream.write(bos.toByteArray());
        }
    }

    public void registerSession(UWAPConnection conn)
    {
        connection = conn;
        connection.addDataListener(this);
        connected = true;
    }

    public boolean onGotData(UWAPConnection conn, UWAPData data[], int serialNum, int requestId)
        throws Exception
    {
        for(int i = 0; i < data.length; i++)
            process(data[i]);

        return false;
    }

    private void process(UWAPData data) throws Exception
    {
        switch(data.getAppType())
        {
        case 30:
            reged = true;
            account.setName(data.readString());
            account.setPassword(data.readString());
            System.out.println("Agent[" + id + "]注册成功 ：" + account.getName() + " , " + account.getPassword());
            account.login();
            addAccount(account);
            
            break;
        case 90: // 'Z'
            onSynctime(data);
            break;
        case 78:
        	logined = true;
        	System.out.println("Agent[" + id + "]登录成功");
        	break;
        case 4:
        	System.out.println("Agent[" + id + "]注册成功");
        	connection.close();
        	break;
        case -1:
        	data.readByte();
        	String str = data.readString();
        	if (str.equals("没有可显示的邮件")) {
        		break;
        	}
        	System.out.println("Agent[" + id + "]错误：" + str + " , " + account.getName() + " , " + account.getPassword());
        	break;
        case 8:
        	this.userLogined = true;
        	System.out.println("Agent[" + id + "]登录用户成功");
        	break;
        case 16:
//        	System.out.println("Agent[" + id + "]取BBS成功");
        	break;
        case 25:
//        	System.out.println("Agent[" + id + "]取邮件成功");
        	break;
        case 11:
//        	System.out.println("Agent[" + id + "]下载成功");
        	data.readShort();
        	if (data.readShort() == (short)2) {
        		byte[] etfData = data.readBytes();
        		int taskID = ((etfData[6] & 0xFF) << 8) + (etfData[7] & 0xFF);
        		if (taskID != 10104) {
        			break;
        		}
//        		System.out.println("下载任务成功");
        		// 任务，放弃重新下载
        		abortTask();
        	}
        	break;
        case 58:
            //System.out.println("Agent[" + id + "]购买成功");
            
            break;
        case (byte)135:
//        	System.out.println("放弃任务成功");
        	downloadTask();
        	break;
        }
    }

    private void onSynctime(UWAPData uwapdata)
    {
    }
    
    private static java.util.Random rand = new java.util.Random(); 
    private void syncTime() throws Exception {
    	UWAPSegment seg = new UWAPSegment((byte)177);
    	int len = rand.nextInt(1000) + 1;
    	byte[] data = new byte[len];
    	int b = rand.nextInt(255);
    	java.util.Arrays.fill(data, (byte)b);
    	seg.writeByteArr(data);
    	connection.write(seg, -1);
    }
    
    private void ishopTrade() throws Exception{
        UWAPSegment seg = new UWAPSegment((byte)58);
        seg.writeInt(200175);
        seg.writeInt(1);
        connection.write(seg, -1);
    }
    
    private void quickReg() throws Exception {
        UWAPSegment seg = new UWAPSegment(30);
        seg.write("13801381038");
        seg.write(version + "-CCCCCPiP");
        seg.write("NK-6600");
        connection.write(seg, -1);
    }
    
    private void login() throws Exception {
        account.login();
        
    	UWAPSegment seg = new UWAPSegment(77);
    	seg.write(account.getName());
    	seg.write(account.getPassword());
    	seg.write("NK-6600");
    	seg.write(version + "-CCCCCPiP");
    	connection.write(seg, -1);
    }
    
    private void createCharactor() throws Exception {
    	UWAPSegment seg = new UWAPSegment(3);
    	seg.write("aatest" + id);
    	seg.writeByte((byte)0);
    	seg.writeInt(0);
    	connection.write(seg, -1);
    }
    
    private void userLogin() throws Exception {
    	UWAPSegment seg = new UWAPSegment(7);
    	seg.write(account.getName());
    	connection.write(seg, -1);
    }
    
    private void sendMove() throws Exception {
    	UWAPSegment seg = new UWAPSegment(12);
        seg.writeShort((short)1619);
        seg.writeShort((short)(400 + rand.nextInt(300)));
        seg.writeShort((short)(400 + rand.nextInt(300)));
        connection.write(seg, -1);
    }
    
    private void downloadStage() throws Exception {
        UWAPSegment segment = new UWAPSegment(11);
        segment.writeString("NK-6600"); //客户端机型
        segment.writeShort((short)1); //用户等级 用来判断下载的关卡任务。如果客户端还不知道用户级别，可填-1。
        segment.writeShort((short)1); //下载类型 1 为关卡2为任务 3 怪物组 4 npc 5 怪物
        segment.writeShort((short)101); //下载序号 下载关卡时为关卡号，下载任务时为任务ID
        connection.write(segment, -1);
    }

    private void downloadTask() throws Exception {
        UWAPSegment segment = new UWAPSegment(11);
        segment.writeString("NK-6600"); //客户端机型
        segment.writeShort((short)1); //用户等级 用来判断下载的关卡任务。如果客户端还不知道用户级别，可填-1。
        segment.writeShort((short)2); //下载类型 1 为关卡2为任务 3 怪物组 4 npc 5 怪物
        segment.writeShort((short)10104); //下载序号 下载关卡时为关卡号，下载任务时为任务ID
        connection.write(segment, -1);
    }
    
    private void abortTask() throws Exception {
    	UWAPSegment segment = new UWAPSegment(135);
    	segment.writeShort((short)10104);
    	connection.write(segment, -1);
    }

    private void sendBattleResult() throws Exception {
        UWAPSegment segment = new UWAPSegment(34);

        segment.writeByte((byte)1);
        segment.writeInt(56);
        segment.writeInt(53);
        segment.writeInt(0);
        segment.writeInt(0);
        segment.writeInt(0x6532015);
        byte count = 1;
        segment.writeByte((count));
        for(int i = 0; i < count; i++){
            segment.writeByte((byte)0);
            segment.writeByte((byte)1);
        }
        segment.writeLong(0x0032003200320032L);
        connection.write(segment, -1);
    }
    
    private void getBBSList() throws Exception {
    	UWAPSegment segment = new UWAPSegment(16);
        segment.writeInt(0x651000a);
        segment.writeShort((short)10);
        segment.writeInt(0);
        connection.write(segment, -1);
    }
    
    private void getMailList() throws Exception {
    	UWAPSegment segment = new UWAPSegment(25);
        segment.writeShort((short)10);
        segment.writeInt(0);
        connection.write(segment, -1);
    }
    
    private static final String[] MESSAGES = new String[] {
    	"    	一.飞鸽相关",
    	"增加发送多个附件的功能",
    	"限制一次最多可以添加附件的数量,在飞鸽选择添加附件时可以通过多次操作来实现添加多个附件,每个附件的金额需要单独设置,对方在收一邮件后,提取附件时可以单独提取也可以全部提取",
"",
    	"二.战斗相关",
    	"1.PK过程中对补血药品的使用",
    	    "为了防止玩家在PK过程中无限量使用药品导致战斗过程过长,因此限制玩家在PK及帮派挑战过程中使用补血类药品的次数",
"",
    	"2.战斗过程中对补内力药品的使用",
    	"为了解决玩家在打怪与PK过程中对内力消耗过度而无法补充的问题,因此在玩家战斗界面增加[补内力]选项,玩家点击后显示玩家背包中被内力药品列表,使用规则与补血药品相同",
    	"3.关于战斗过程中逃跑的处理",
    	"3.1打怪状态",
    	"玩家在打怪过程中选择[逃跑]根据双方的属性计算出逃跑成功的概率,若逃跑成功则不会获得打怪所得",
    	"3.2非强制PK状态",
    	    "玩家双方处于非强制PK状态下如果一方选择[逃跑]且逃跑成功则视为失败,扣除所押赌注金钱,胜利方可以获得金钱与声望",
    	"3.3强制PK状态",
    	   "玩家双方处于强制PK状态下,如果一方选择[逃跑]且逃跑成功则此次PK无效,双方无所得也无损失",
    	"3.4擂台状态",
    	"玩家在普通擂台及帮派擂台状态中时如果选择逃跑且逃跑成功则视为其在本轮比赛中失败,不可进入下一轮比赛",
    	"3.5帮派挑战状态"
    };
    
    private void sendChat() throws Exception {
    	UWAPSegment seg = new UWAPSegment((byte)72);
    	seg.writeInt(-1);
    	seg.writeString("");
    	seg.writeInt(-1);
    	seg.writeString(MESSAGES[rand.nextInt(MESSAGES.length)]);
    	connection.write(seg, -1);
    }
    
    public void onSignal(UWAPConnection uwapconnection, int i, String s)
    {
    }
    
    private static void loadAccounts(){
        BufferedReader br = null;
        
        try{
            br = new BufferedReader(new FileReader("accounts.txt"));

            String line = br.readLine();

            while(line != null){
                int idx = line.indexOf("|");
                
                if(idx >= 0){
                    String name = line.substring(0, idx);
                    String password = line.substring(idx + "|".length());
                    
                    Account account = new Account(name, password);
                    accountTable.put(account, account);
                    
                    System.out.println(account);
                }
                
                line = br.readLine();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                br.close();
            }catch(Exception e){
            }
        }
    }
    
    private static synchronized Account getAccountFromTable(){
        Iterator<Account> it = accountTable.keySet().iterator();
        
        while(it.hasNext()){
            Account acc = it.next();
            
            if(!acc.online()){
                acc.login();
                
                return acc;
            }
        }
        
        return null;
    }
    
    private static synchronized void addAccount(Account account){
        BufferedWriter bw = null;
        
        try{
            accountTable.put(account, account);
            
            bw = new BufferedWriter(new FileWriter("accounts.txt", true));
            bw.newLine();
            bw.write(account.getName() + "|" + account.getPassword());
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                bw.close();
            }catch(Exception e){
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        loadAccounts();
        
        int count = 10;
        
        if(args.length > 0){
            count = Integer.parseInt(args[0]);
        }
        
        String host = "218.206.80.188";
        //String host = "192.168.0.30";
        int port = 20008;
        
        for(int i = 0; i < count; i++){
            AccountTestClient client = new AccountTestClient(i, host, port, null, 0);
            new Thread(client).start();
            Thread.sleep(100);
        }
//    	int count = 10;
//    	String host = "127.0.0.1";
//    	int port = 7777;
//    	boolean create = false;
//    	boolean downloadTask = false;
//    	
//    	for (int i = 0; i < count; i++) {
//    		AccountTestClient client = new AccountTestClient(i, host, port, null, 0);
//    		client.justCreateCharactor = create;
//    		client.downloadTask = downloadTask;
//    		new Thread(client).start();
//    		Thread.sleep(100);
//    	}
    }
}
