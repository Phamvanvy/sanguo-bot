package pip;

import com.pip.wulin.server.io.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

import com.pip.wulin.server.io.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;


public class DumbClient
    implements Runnable, UWAPApp, UWAPDataListener
{
	
	private int id;
	private String host;
	private int port;
    private String realHost;
    private int realPort;
	UWAPConnection connection;
	private boolean connected = false;
	private boolean logined = false;
	private boolean userLogined = false;
	private boolean justCreateCharactor = false;
	private boolean downloadTask = false;

    public DumbClient(int id, String host, int port, String realHost, int realPort)
    {
        this.host = host;
        this.port = port;
        this.id = id;
        this.realHost = realHost;
        this.realPort = realPort;
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
            System.out.println("Agent[" + id + "]开始登录");
            login();
            while (!logined) {
            	Thread.sleep(100);
            }
            if (justCreateCharactor) {
            	createCharactor();
            	return;
            }
            userLogin();
            while (!userLogined) {
            	Thread.sleep(100);
            }
            if (downloadTask) {
            	downloadTask();
            }
            while (true) {
            	Thread.sleep(12000);
//                Thread.sleep(6000);
            	sendMove();
            	syncTime();
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
            }
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
        	System.out.println(str);
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
    
    private void login() throws Exception {
    	UWAPSegment seg = new UWAPSegment(77);
    	seg.write("test" + id);
    	seg.write("2008");
    	seg.write("NK-6600");
    	seg.write("2.0.0-CCCCCPiP");
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
    	seg.write("test" + id);
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
    
    public static void main(String[] args) throws Exception {
    	int start = 3000, end = 3500;
    	String host = "192.168.0.9";
    	int port = 7002;
        String realHost = "192.168.0.9";
        int readPort = 29999;
    	boolean create = false;
    	boolean downloadTask = false;
    	for (int i = 0; i < args.length; i++) {
    		if (args[i].equals("-help")) {
    			System.out.println("Usage: java DumbClient [-sStart] [-eEnd] [-help] [-hHost] [-pPort] [-rhRealHost] [-rpRealPort] [-c] [-t]");
    			return;
    		}
    		if (args[i].startsWith("-s")) {
    			start = Integer.parseInt(args[i].substring(2));
    		}
    		if (args[i].startsWith("-e")) {
    			end = Integer.parseInt(args[i].substring(2));
    		}
    		if (args[i].startsWith("-h")) {
    			host = args[i].substring(2);
    		}
    		if (args[i].startsWith("-p")) {
    			port = Integer.parseInt(args[i].substring(2));
    		}
            if (args[i].startsWith("-rh")) {
                realHost = args[i].substring(3);
            }
            if (args[i].startsWith("-rp")) {
                readPort = Integer.parseInt(args[i].substring(3));
            }
    		if (args[i].startsWith("-c")) {
    			create = true;
    		}
    		if (args[i].startsWith("-t")) {
    			downloadTask = true;
    		}
    	}
    	for (int i = start; i < end; i++) {
    		DumbClient client = new DumbClient(i, host, port, realHost, readPort);
    		client.justCreateCharactor = create;
    		client.downloadTask = downloadTask;
    		new Thread(client).start();
    		Thread.sleep(100);
    	}
    }
}
