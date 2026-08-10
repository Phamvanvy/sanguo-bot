package com.pip.sanguo.performancetest.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.pip.sanguo.performancetest.net.UASegment;
import com.pip.sanguo.performancetest.net.UASocketConnection;

public class SanguoClient implements Runnable{
    private String accountName;
    private String accountPassword;
    private int actorId;
    private String actorName;
    private boolean running;
    private int status;
    private int oldSstatus;

    private boolean dirty;
    private String errorMessage;
    private UASocketConnection connection;

    private int sendCount;
    private int sendBytes;
    private int recvCount;
    private int recvBytes;

    private long lastServerTime;
    private long lastPositionTime;
    private long lastAttackTime;
    private long lastChatTime;
    private long lastCheckMapTime;
    private int positionCount;
    private int attackCount;
    private int chatCount;
    private long startTime;
    private int lastX = 300;
    private int lastY = 200;
    
    private int targetMapID;
    private int mapID;
    private int mapWidth;
    private int mapHeight;
    private int level;
    private int sex;
    private int clazz;
    private int faction;
    private int targetLevel;

    private int timeGap;
    private int serverTime;
    private long lastSyncServerTime;

    private List<UASegment> segments = new Vector<UASegment>();

    private static final int SLEEP_TIME = 75;

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_CONNECT = 1;
    private static final int STATUS_ACC_LOGIN = 2;
    private static final int STATUS_ACC_LOGIN_DOING = 3;
    private static final int STATUS_ACC_REG = 4;
    private static final int STATUS_ACC_REG_DOING = 5;
    private static final int STATUS_GET_ACTOR_LIST = 6;
    private static final int STATUS_GET_ACTOR_LIST_DOING = 7;
    private static final int STATUS_ACTOR_LOGIN = 8;
    private static final int STATUS_ACTOR_LOGIN_DOING = 9;
    private static final int STATUS_ACTOR_CREATE = 10;
    private static final int STATUS_ACTOR_CREATE_DOING = 11;
    private static final int STATUS_RUNNING = 12;
    private static final int STATUS_ERROR = -1;

//    private static final String SERVER_URL = "socket://192.168.0.55:7003";//"socket://192.168.0.55:7001#C0A800371B5B";
    //public static String SERVER_URL = "socket://192.168.0.55:7001#C0A800371B5C";
    public static String SERVER_URL = "socket://s01.sg.5ding.com:8080#7F0000016D62";
    //private static final String SERVER_URL = "socket://221.179.216.49:30002#DDB3D8346D61";
    //private static final String SERVER_URL = "socket://192.168.0.190:7000";

    private static String[][] MAP_CONFIG = {
    	{ "48", "三国村", "416", "320" },
    	{ "49", "三国村集市", "512", "448" },
    	{ "50", "三国平原", "800", "800" },
    	{ "512", "危险边境", "544", "512" },
    	{ "513", "边境后山", "512", "512" },
    	{ "992", "江津村外", "912", "800" },
    	{ "993", "江津村", "512", "512" },
    	{ "704", "危险边境", "896", "912" },
    	{ "705", "边境后山", "512", "512" },
    	{ "706", "未命名场景", "1008", "1008" },
    	{ "1312", "农场庄园", "720", "720" },
    	{ "464", "桃源", "640", "480" },
    	{ "465", "桃源东", "480", "640" },
    	{ "466", "桃源码头", "480", "640" },
    	{ "192", "许田镇(魏)|6-10级", "512", "512" },
    	{ "193", "许田镇外(魏)|6-10级", "912", "800" },
    	{ "128", "颖川(魏)|11-15级", "640", "512" },
    	{ "129", "颖川城外(魏)|11-15级", "800", "1008" },
    	{ "256", "许昌城外(魏)", "512", "512" },
    	{ "272", "魏都-许昌(魏)", "1024", "1024" },
    	{ "96", "白马(魏)|16-20级", "1040", "896" },
    	{ "97", "官渡(魏)|16-20级", "512", "512" },
    	{ "112", " 白马地宫(魏)|16-20级", "512", "448" },
    	{ "144", "徐州城(魏)|21-25级", "720", "512" },
    	{ "145", "洪泽湖(魏)|21-25级", "800", "1008" },
    	{ "160", "下邳(魏)|26-30级", "512", "496" },
    	{ "161", "下邳外山寨(魏)|26-30级", "800", "1008" },
    	{ "400", "小沛(魏)|31-35级", "512", "512" },
    	{ "401", "微山湖(魏)|31-35级", "960", "800" },
    	{ "544", "幽州(魏)|36-40级", "720", "512" },
    	{ "545", "涿县(魏)|36-40级", "896", "896" },
    	{ "672", "渔阳(魏)|41-45级", "512", "512" },
    	{ "673", "燕山(魏)|41-45级", "928", "928" },
    	{ "656", "乌桓(魏)|46-50级", "592", "464" },
    	{ "657", "白狼山(魏)|46-50级", "960", "912" },
    	{ "720", "未命名场景", "640", "448" },
    	{ "721", "未命名场景", "960", "800" },
    	{ "576", "桃源", "640", "480" },
    	{ "577", "桃源东", "480", "640" },
    	{ "578", "桃源码头", "480", "640" },
    	{ "208", "江津村(蜀)|6-10级", "496", "496" },
    	{ "209", "江津村外(蜀)|6-10级", "912", "800" },
    	{ "64", "峨眉山(蜀)|11-15级", "480", "336" },
    	{ "80", "峨眉后山(蜀)|11-15级", "1040", "848" },
    	{ "224", "成都城外(蜀)", "512", "512" },
    	{ "240", "蜀都-成都", "960", "960" },
    	{ "32", "落凤坡(蜀)|16-20级", "512", "512" },
    	{ "33", "落凤谷(蜀)|16-20级", "896", "896" },
    	{ "34", "庞统祠(蜀)|16-20级", "768", "768" },
    	{ "35", "庞统祠内院(蜀)|16-20级", "432", "368" },
    	{ "16", "永昌郡(蜀)|21-25级", "608", "416" },
    	{ "17", "腾冲(蜀)|21-25级", "960", "800" },
    	{ "176", "且兰郡(蜀)|26-30级", "512", "496" },
    	{ "177", "且兰城外(蜀)|26-30级", "1056", "1008" },
    	{ "528", "一登(蜀)|31-35", "512", "512" },
    	{ "529", "盘越山寨(蜀)|31-35", "1008", "1008" },
    	{ "480", "建宁(蜀)|36-40级", "576", "512" },
    	{ "481", "宛温(蜀)|36-40级", "960", "960" },
    	{ "608", "朱提大营(蜀)|41-45级", "512", "512" },
    	{ "609", "念湖(蜀)|41-45级", "1024", "800" },
    	{ "624", "夜郎(蜀)|46-50级", "688", "432" },
    	{ "625", "北盘江(蜀)|46-50级", "960", "800" },
    	{ "640", "桃源", "640", "480" },
    	{ "641", "桃源东", "480", "640" },
    	{ "642", "桃源码头", "480", "640" },
    	{ "384", "稻香村(吴)|6-10级", "512", "512" },
    	{ "385", "稻香村外(吴)|6-10级", "912", "800" },
    	{ "336", "秦淮河(吴)|11-15级", "512", "512" },
    	{ "337", "秦淮河对岸(吴)|11-15级", "800", "1008" },
    	{ "368", "建业城外", "512", "512" },
    	{ "352", "吴都-建业", "960", "960" },
    	{ "304", "曲阿(吴)|16-20级", "512", "512" },
    	{ "305", "曲阿城外(吴)|16-20级", "896", "896" },
    	{ "320", "曲阿金矿洞内(吴)|16-20级", "512", "448" },
    	{ "288", "会稽城(吴)|21-25级", "512", "512" },
    	{ "289", "雁荡山(吴)|21-25级", "960", "960" },
    	{ "416", "茅山(吴)|26-30级", "496", "496" },
    	{ "417", "茅山外(吴)|26-30级", "1056", "800" },
    	{ "496", "越王之墓(吴)|31-35级", "1040", "896" },
    	{ "497", "越王池(吴)|31-35级", "512", "512" },
    	{ "560", "建安(吴)|36-40级", "720", "512" },
    	{ "561", "章安(吴)|36-40级", "1056", "800" },
    	{ "592", "武夷山(吴)|41-45级", "512", "480" },
    	{ "593", "武夷九曲溪(吴)|41-45级", "960", "800" },
    	{ "688", "瓯宁(吴)|46-50级", "592", "400" },
    	{ "689", "瓯宁外(吴)|46-50级", "992", "800" },
    	{ "432", "河东废城官衙内部|31-35级", "608", "464" },
    	{ "433", "河东废城官衙|70级", "608", "464" },
    	{ "448", "河东废城城外|31-35级", "512", "512" },
    	{ "449", "河东废城城内|31-35级", "992", "992" },
    	{ "450", "河东废城|70级", "992", "992" },
    	{ "768", "古墓入口|51-53级", "560", "432" },
    	{ "769", "古墓地宫前厅|51-53级", "800", "608" },
    	{ "770", "古墓地宫左室|51-53级", "560", "432" },
    	{ "771", "古墓地宫右室|51-53级", "560", "432" },
    	{ "772", "古墓地宫主室|51-53级", "832", "720" },
    	{ "773", "古墓地宫左副室|51-53级", "352", "256" },
    	{ "1136", "古墓入口", "560", "432" },
    	{ "1137", "古墓地宫前厅", "800", "608" },
    	{ "1138", "古墓地宫左室", "560", "432" },
    	{ "1139", "古墓地宫右室", "560", "432" },
    	{ "1140", "古墓地宫主室", "832", "720" },
    	{ "1141", "古墓地宫左副室", "352", "256" },
    	{ "736", "婚礼礼堂(蜀)", "624", "464" },
    	{ "784", "婚礼礼堂(魏)", "624", "464" },
    	{ "800", "婚礼礼堂(吴)", "624", "464" },
    	{ "1264", "国公殿(魏)", "624", "464" },
    	{ "1280", "国公殿(蜀)", "624", "464" },
    	{ "1296", "国公殿(吴)", "624", "464" },
    	{ "848", "西域|51-55级", "1504", "1504" },
    	{ "849", "城站场", "1040", "736" },
    	{ "1008", "西域山城", "1504", "1504" },
    	{ "816", "朔方(56-60级)", "1440", "1600" },
    	{ "832", "朔方山洞", "800", "512" },
    	{ "833", "朔方矿洞", "800", "512" },
    	{ "1024", "匈奴王庭", "1504", "1504" },
    	{ "896", "江陵(中立)|61-65级", "1504", "1504" },
    	{ "880", "江陵藏兵洞(中立)|61-65级", "800", "512" },
    	{ "1056", "荆州(中立)|61-65级", "1504", "1504" },
    	{ "864", "南海|66-70级", "1504", "1504" },
    	{ "944", "南海苍梧山洞|66-70级", "800", "800" },
    	{ "1040", "南越|66-70级", "1504", "1504" },
    	{ "752", "南郡战场(魏vs蜀)", "1024", "1024" },
    	{ "912", "南郡战场(吴VS魏)", "1024", "1024" },
    	{ "928", "南郡战场(蜀VS吴)", "1024", "1024" },
    	{ "960", "荆州战场(魏vs蜀)", "1008", "1008" },
    	{ "976", "天龙阵入口", "512", "512" },
    	{ "977", "五行天龙阵", "1280", "1024" },
    	{ "978", "五行天龙阵(内阵)", "1280", "1024" },
    	{ "1072", "魏国国战场", "1024", "1024" },
    	{ "1088", "蜀国国战场", "960", "960" },
    	{ "1104", "吴国国战场", "960", "960" },
    	{ "1152", "反击战场（魏国）", "1024", "1280" },
    	{ "1232", "反击战场（蜀国）", "1024", "1280" },
    	{ "1248", "反击战场（吴国）", "1024", "1280" },
    	{ "1120", "碧水阁", "704", "608" },
    	{ "1168", "闯关场|(50-55级)", "704", "800" },
    	{ "1184", "闯关场|(56-60级)", "704", "800" },
    	{ "1200", "闯关场|(61-65级)", "704", "800" },
    	{ "1216", "闯关场|(66级以上)", "704", "800" },
    	{ "1344", "洛阳行宫外|70级", "512", "512" },
    	{ "1345", "洛阳行宫内|70级", "1008", "1008" },
    	{ "1328", "行宫别苑", "448", "352" },
    };
    
    public SanguoClient(){
        accountName = null;
        accountPassword = null;
        actorId = -1;
        actorName = null;
    }

    /** 取得服务器时间 */
    public int getServerTime(){
        return serverTime + (int) (System.currentTimeMillis() - lastSyncServerTime);
    }

    /** 设置服务器时间 */
    public void setServerTime(int time){
        lastSyncServerTime = System.currentTimeMillis();
        serverTime = time;
    }

    public void run(){
        while(running){
            try{
                cycle();

                if(oldSstatus != status){
                    oldSstatus = status;
                    dirty();
                }

                if(dirty){
                    dirty = false;
                    ClientManager.setDirty(true);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try{
                    Thread.sleep(SLEEP_TIME);
                }catch(Exception e){
                }
            }
        }
    }

    private void dirty(){
        dirty = true;
    }
    
    private int getMapWidth(int mapID) {
    	for (int i = 0; i < MAP_CONFIG.length; i++) {
    		if (MAP_CONFIG[i][0].equals(String.valueOf(mapID))) {
    			return Integer.parseInt(MAP_CONFIG[i][2]);
    		}
    	}
    	return 400;
    }
    
    private int getMapHeight(int mapID) {
    	for (int i = 0; i < MAP_CONFIG.length; i++) {
    		if (MAP_CONFIG[i][0].equals(String.valueOf(mapID))) {
    			return Integer.parseInt(MAP_CONFIG[i][3]);
    		}
    	}
    	return 400;
    }

    public void cycle(){
        try{
            cycleSegments();

            switch(status){
                case STATUS_IDLE: {
                    status = STATUS_CONNECT;
                }
                    break;
                case STATUS_CONNECT: {
                    try{
                        connection = new UASocketConnection(SERVER_URL, this);
                        new Thread(connection).start();

                        if(accountPassword != null && accountPassword.length() > 0){
                            status = STATUS_ACC_LOGIN;
                        }else{
                            status = STATUS_ACC_REG;
                        }

                        //发同步时间包
                        UASegment segment = new UASegment(OperationCode.SYNC_TIME_CLIENT);
                        segment.writeInt(getServerTime());
                        connection.writeSegment(segment);
                    }catch(Exception e){
                        e.printStackTrace();

                        errorMessage = "建立连接失败";
                        status = STATUS_ERROR;
                        dirty();
                    }
                }
                    break;
                case STATUS_ACC_LOGIN: {
                    UASegment segment = new UASegment(OperationCode.ACCOUNT_LOGIN_CLIENT, true);
                    segment.writeString(accountName);
                    if (accountPassword.equals("?")) {
                    	// Flash版本，自动生成密码
                    	int now = (int)(System.currentTimeMillis() / 1000L);
                    	String pass = "123456" + accountName + now;
                    	MessageDigest md5 = MessageDigest.getInstance("MD5");
                    	byte[] dest = md5.digest(pass.getBytes("UTF-8"));
                    	String enc = "";
                    	char Digest[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    				'A', 'B', 'C', 'D', 'E', 'F' };
                    	for (int i = 0; i < dest.length; i++) {
                    		int b1 = (dest[i] & 0xF0) >> 4;
                    		int b2 = (dest[i] & 0x0F);
                    		enc += Digest[b1];
                    		enc += Digest[b2];
                    	}
                    	segment.writeString(now + "@" + enc);
                    } else {
                    	segment.writeString(accountPassword);
                    }
                    segment.writeString("Midp2Big/Test");
                    segment.writeString("0.0.1-Test");
                    connection.writeSegment(segment);

                    status = STATUS_ACC_LOGIN_DOING;
                }
                    break;
                case STATUS_ACC_LOGIN_DOING:
                    break;
                case STATUS_ACC_REG: {
                    UASegment segment = new UASegment(OperationCode.ACCOUNT_REG_CLIENT, true);
                    segment.writeString(accountName);
                    segment.writeString("13801381038");
                    segment.writeString("test");
                    segment.writeString("1.0.0");
                    connection.writeSegment(segment);

                    status = STATUS_ACC_REG_DOING;
                }
                    break;
                case STATUS_GET_ACTOR_LIST: {
                    UASegment segment = new UASegment(OperationCode.ACTOR_LIST_CLIENT, true);
                    connection.writeSegment(segment);
                    status = STATUS_GET_ACTOR_LIST_DOING;
                }
                    break;
                case STATUS_ACTOR_LOGIN: {
                    UASegment segment = new UASegment(OperationCode.ACTOR_LOGIN_CLIENT, true);
                    segment.writeInt(actorId);
                    connection.writeSegment(segment);

                    status = STATUS_ACTOR_LOGIN_DOING;
                }
                    break;
                case STATUS_ACTOR_CREATE: {
                	Random rand = new Random();
                    UASegment segment = new UASegment(OperationCode.ACTOR_CREATE_CLIENT, true);
                    segment.writeString(accountName);
                    segment.writeByte((byte)rand.nextInt(2));  // 性别
                    segment.writeByte((byte)rand.nextInt(4));  // 职业 
                    segment.writeByte((byte)(rand.nextInt(3) + 1));  // 阵营
                    targetLevel = rand.nextInt(65) + 5;
                    connection.writeSegment(segment);

                    status = STATUS_ACTOR_CREATE_DOING;
                }
                    break;
                case STATUS_ACTOR_CREATE_DOING:
                    break;
                case STATUS_RUNNING: {
                    long now = System.currentTimeMillis();

                    if(now - lastServerTime > 10000){
                        //发同步时间包
                        UASegment segment = new UASegment(OperationCode.SYNC_TIME_CLIENT);
                        segment.writeInt(getServerTime());
                        connection.writeSegment(segment);

                        lastServerTime = now;
                    }

                    if(now - lastPositionTime > 5000){
                        //发move包，从当前位置出发，随机移动到一个位置，但不超过地图大小
                    	int w = getMapWidth(mapID);
                    	int h = getMapHeight(mapID);
                    	
                    	int range = ClientManager.rand.nextInt(200);
                    	int x1 = Math.max(0, lastX - range);
                    	int x2 = Math.min(w, lastX + range);
                    	int y1 = Math.max(0, lastY - range);
                    	int y2 = Math.min(h, lastY + range);
                    	
                    	lastX = x1 + ClientManager.rand.nextInt(Math.abs(x2 - x1) + 1);
                    	lastY = y1 + ClientManager.rand.nextInt(Math.abs(y2 - y1) + 1);
                    	
                        UASegment segment = new UASegment(OperationCode.MOVE_CLIENT);
                        segment.writeInt(getServerTime());
                        segment.writeShort((short)lastX);
                        segment.writeShort((short)lastY);
                        segment.writeByte((byte) ClientManager.rand.nextInt(4));
                        segment.writeShort((short) 0);
                        connection.writeSegment(segment);

                        lastPositionTime = now;
                    }

                    if(now - lastAttackTime > 30000){
                        //发攻击包
//                        UASegment segment = new UASegment(OperationCode.SKILL_ATTACK_CLIENT);
//                        segment.writeInt(getServerTime());
//                        segment.writeShort((short)lastX);
//                        segment.writeShort((short)lastY);
//                        segment.writeByte((byte) ClientManager.rand.nextInt(4));
//                        segment.writeInt(actorId);
//                        segment.writeInt(1);
//                        connection.writeSegment(segment);

                        lastAttackTime = now;
                    }

                    if(now - lastChatTime > 600000){
                        //聊天包
                        UASegment segment = new UASegment(OperationCode.CHAT_CLIENT);
                        segment.writeByte((byte) 2);
                        segment.writeInt(-1);
                        segment.writeString("请问。。。有人在吗？为什么没人理我撒！！！");
                        segment.writeBytes(new byte[0]);
                        //connection.writeSegment(segment);

                        lastChatTime = now;
                    }
                    
                    if (now - lastCheckMapTime > 10000) {
                    	// 如果不在目标地图，则跳转
                    	if (targetMapID != mapID) {
                    		int w = getMapWidth(targetMapID);
                        	int h = getMapHeight(targetMapID);
                        	UASegment seg = new UASegment(OperationCode.CHAT_CLIENT);
        	                seg.writeByte((byte) 2);
        	                seg.writeInt(-1);
        	                seg.writeString("/go " + targetMapID + " " + (w / 2) + " " + (h / 2));
        	                seg.writeBytes(new byte[0]);
        	                connection.writeSegment(seg);
                    	}
                    	lastCheckMapTime = System.currentTimeMillis();
                    }
                }
                    break;
                case STATUS_ERROR: {
                    if(connection != null){
                        connection.close();
                    }
                    
                    running = false;
                }
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
            status = STATUS_ERROR;
        }
    }
    
    private static int[][] MAP_WEI = {
    	{ 10, 192, 193 },
    	{ 15, 128, 129 },
    	{ 20, 256, 272, 96, 97 },
    	{ 25, 144, 145 },
    	{ 30, 160, 161 },
    	{ 35, 400, 401 },
    	{ 40, 544, 545 }, 
    	{ 45, 672, 673 },
    	{ 50, 656, 657 },
    	{ 55, 848, 1008 },
    	{ 60, 816, 1024 },
    	{ 65, 896, 1056 },
    	{ 100, 864, 1040 }
    };
    private static int[][] MAP_SHU = {
    	{ 10, 208, 209 },
    	{ 15, 64, 80 },
    	{ 20, 224, 240, 32, 33, 34, 35 },
    	{ 25, 16, 17 },
    	{ 30, 176, 177 },
    	{ 35, 528, 529 },
    	{ 40, 480, 481 }, 
    	{ 45, 608, 609 },
    	{ 50, 624, 625 },
    	{ 55, 848, 1008 },
    	{ 60, 816, 1024 },
    	{ 65, 896, 1056 },
    	{ 100, 864, 1040 }
    };
    private static int[][] MAP_WU = {
    	{ 10, 384, 385 },
    	{ 15, 336, 337 },
    	{ 20, 368, 352, 304, 305 },
    	{ 25, 288, 289 },
    	{ 30, 416, 417 },
    	{ 35, 496, 497 }, 
    	{ 40, 560, 561 },
    	{ 45, 592, 593},
    	{ 50, 688, 689},
    	{ 55, 848, 1008 },
    	{ 60, 816, 1024 },
    	{ 65, 896, 1056 },
    	{ 100, 864, 1040 }
    };
    private int getTargetMap(int faction, int level) {
    	int[][] list;
    	switch (faction) {
    	case 1:
    		list = MAP_WEI;
    		break;
    	case 2:
    		list = MAP_SHU;
    		break;
    	case 3:
    		list = MAP_WU;
    		break;
    	default:
    		return 896;
    	}
    	for (int i = 0; i < list.length; i++) {
    		if (level <= list[i][0]) {
    			int index = ClientManager.rand.nextInt(list[i].length - 1);
    			return list[i][index + 1];
    		}
    	}
    	return 896;
    }

	public static final int[] LEVELUP_EXP = {
		0,
		5 ,
		6 ,
		8 ,
		15 ,
		30 ,
		57 ,
		101 ,
		169 ,
		267 ,
		405 ,
		590 ,
		834 ,
		1147 ,
		1541 ,
		1692 ,
		2189 ,
		2789 ,
		3504 ,
		4349 ,
		5338 ,
		5561 ,
		6698 ,
		8000 ,
		9484 ,
		11166 ,
		13061 ,
		15189 ,
		17566 ,
		20213 ,
		23148 ,
		26391 ,
		29964 ,
		33888 ,
		38186 ,
		42880 ,
		47994 ,
		53552 ,
		59580 ,
		66103 ,
		79243 ,
		87469 ,
		96320 ,
		105825 ,
		116018 ,
		136693 ,
		149254 ,
		162662 ,
		176953 ,
		192166 ,
		238102 ,
		257728 ,
		278544 ,
		300596 ,
		323932 ,
		392177 ,
		421486 ,
		452407 ,
		485000 ,
		519323 ,
		678866 ,
		857134 ,
		1055465 ,
		1275253 ,
		1517954 ,
		2125094 ,
		2710701 ,
		3358548 ,
		4072675 ,
		4857276 ,
		8929951 ,
		13787227 ,
		22717179 ,
		36504406 ,
		59221585 ,
		95725991 ,
		154947576 ,
		250673567 ,
		405621143 ,
		656294711 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
	};
	
    private void cycleSegment(UASegment segment) throws IOException {
        switch(segment.type){
            case OperationCode.ERROR: {
                int serial = segment.readInt();
                short type = segment.readShort();
                String message = segment.readString();
                errorMessage = message;
                status = STATUS_ERROR;
            }
                break;
            case OperationCode.ACCOUNT_LOGIN_SERVER: {
                int serial = segment.readInt();
                int accountId = segment.readInt();
                String accountName = segment.readString();
                int iMoney = segment.readInt();

                status = STATUS_GET_ACTOR_LIST;
            }
                break;
            case OperationCode.ACCOUNT_REG_SERVER: {
                int serial = segment.readInt();
                String name = segment.readString();
                int id = segment.readInt();
                String pass = segment.readString();

                accountPassword = pass;
                status = STATUS_ACC_LOGIN;
            }
                break;
            case OperationCode.ACTOR_LIST_SERVER: {
                int serial = segment.readInt();
                int count = segment.readUnsignedByte();

                if(count > 0){
                    int id = segment.readInt();
                    String name = segment.readString();
                    int sex = segment.readByte();
                    int level = segment.readByte();
                    int clazz = segment.readByte();
                    int faction = segment.readByte();

                    actorId = id;
                    actorName = name;
                    status = STATUS_ACTOR_LOGIN;
                }else{
                    status = STATUS_ACTOR_CREATE;
                }
            }
                break;
            case OperationCode.ACTOR_LOGIN_SERVER: {
                int serial = segment.readInt();
                byte[] data = segment.readBytes();
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                try {
                	int playerID = dis.readInt();
                	String name = dis.readUTF();
                	sex = dis.readByte();
                	level = dis.readByte();
                	clazz = dis.readByte();
                	faction = dis.readByte();
                	for (int i = 0; i < 26; i++) {
                		dis.readShort();
                	}
                	dis.readInt();
                	dis.readInt();
                	dis.readInt();
                	mapID = dis.readShort();
                	dis.readInt();
                	lastX = dis.readShort();
                	lastY = dis.readShort();
                } catch (Exception e) {
                }

                UASegment seg = new UASegment(OperationCode.LOADING_FINISHED_CLIENT, false);
                connection.writeSegment(seg);
                status = STATUS_RUNNING;
                lastCheckMapTime = System.currentTimeMillis();
                
                // 如果级别没有达到目标级别，则发/exp命令升级
                try {
	                seg = new UASegment(OperationCode.CHAT_CLIENT);
	                seg.writeByte((byte) 2);
	                seg.writeInt(-1);
	                seg.writeString("timeismoney@@!!");
	                seg.writeBytes(new byte[0]);
	                connection.writeSegment(seg);
	                if (level == 1) {
	                	int exp = 0;
	                	for (int i = level; i < targetLevel; i++) {
	                		exp += LEVELUP_EXP[i];
	                	}
	                	seg = new UASegment(OperationCode.CHAT_CLIENT);
		                seg.writeByte((byte) 2);
		                seg.writeInt(-1);
		                seg.writeString("/exp " + exp);
		                seg.writeBytes(new byte[0]);
		                connection.writeSegment(seg);
		                level = targetLevel;
	                } else {
	                	targetLevel = level;
	                }
                } catch (Exception e) {
                }
                
                // 根据级别和阵营选择目标地图
                targetMapID = getTargetMap(faction, level);
            }
                break;
            case OperationCode.ACTOR_CREATE_SERVER: {
                int serial = segment.readInt();
                int id = segment.readInt();
                String name = segment.readString();
                int sex = segment.readByte();
                int level = segment.readByte();
                int clazz = segment.readByte();

                actorId = id;
                actorName = name;
                status = STATUS_ACTOR_LOGIN;
            }
                break;
            case OperationCode.UNIT_REFRESH_SERVER:
            case OperationCode.UNIT_MOVE_SERVER:
                positionCount++;
                dirty();

                break;
            case OperationCode.ATTACK_FAIL_SERVER:
            case OperationCode.SKILL_ATTACK_SERVER:
            case OperationCode.SKILL_ATTACKED_SERVER:
                attackCount++;
                dirty();

                break;
            case OperationCode.CHAT_SERVER:
                chatCount++;
                dirty();

                break;
            case OperationCode.SYNC_TIME_SERVER: {
                int ctime = segment.readInt();
                int stime = segment.readInt();
                timeGap = stime - ctime;
                setServerTime(stime);
                dirty();
                break;
            }
           
            case OperationCode.DIE_SERVER: {
            	// 死亡后自动复活
            	UASegment seg = new UASegment(OperationCode.RELIVE_CLIENT);
            	seg.writeInt(0);
            	connection.writeSegment(seg);
            	break;
            }
            
            case OperationCode.RELIVE_SERVER: {
            	/**
            	 * 通知玩家复活
            	 * InstanceId							int
            	 * 复活时的地图mapId						int
            	 * 复活时的mapInstanceId					int
            	 * 复活时的x坐标							int
            	 * 复活时的y坐标							int
            	 * 复活所使用的动画						int				
            	 */
            	int instanceID = segment.readInt();
            	mapID = segment.readInt();
            	int mapInstanceID = segment.readInt();
            	lastX = segment.readInt();
            	lastY = segment.readInt();
            	
            	UASegment seg = new UASegment(OperationCode.LOADING_FINISHED_CLIENT, false);
                connection.writeSegment(seg);
            	break;
            }
            
            case OperationCode.FORCE_GOMAP_SERVER:
            	// 强制过地图
            	mapID = segment.readInt();
            	segment.readInt();
            	lastX = segment.readInt();
            	lastY = segment.readInt();
            	UASegment seg = new UASegment(OperationCode.LOADING_FINISHED_CLIENT, false);
                connection.writeSegment(seg);
            	break;
        }
    }

    private void cycleSegments(){
        synchronized(segments){
            while(segments.size() > 0){
                UASegment segment = segments.remove(0);
                try {
                	cycleSegment(segment);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        }
    }

    public void processSegment(UASegment segment){
        synchronized(segments){
            segments.add(segment);
        }
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();

        if(accountName != null){
            sb.append(accountName.trim());
        }

        sb.append(',');

        if(accountPassword != null){
            sb.append(accountPassword.trim());
        }

        sb.append('\n');

        return sb.toString();
    }

    public String toLogString(){
        StringBuffer sb = new StringBuffer();

        sb.append(this.getAccountName());
        sb.append('\t');
        sb.append('\t');
        sb.append(this.getAccountPassword());
        sb.append('\t');
        sb.append('\t');
        sb.append(this.getActorName());
        sb.append('\t');
        sb.append('\t');
        sb.append(this.getStatus());
        sb.append('\t');
        sb.append(this.getSendInfo());
        sb.append('\t');
        sb.append('\t');
        sb.append(this.getRecvInfo());
        sb.append('\t');
        sb.append('\t');
        sb.append(String.valueOf(this.getPositionCount()));
        sb.append('\t');
        sb.append('\t');
        sb.append(String.valueOf(this.getAttackCount()));
        sb.append('\t');
        sb.append('\t');
        sb.append(String.valueOf(this.getChatCount()));
        sb.append('\t');
        sb.append('\t');
        sb.append(String.valueOf(this.getTimeGap()));
        sb.append('\t');
        sb.append('\t');
        sb.append(this.getRunTime());
        sb.append('\t');
        sb.append('\t');
        sb.append(this.getAvgBytes());

        return sb.toString();
    }

    public String getAccountName(){
        return accountName;
    }

    public void setAccountName(String accountName){
        this.accountName = accountName;
    }

    public String getAccountPassword(){
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword){
        this.accountPassword = accountPassword;
    }

    public int getActorId(){
        return actorId;
    }

    public void setActorId(int actorId){
        this.actorId = actorId;
    }

    public String getActorName(){
        return actorName;
    }

    public void setActorName(String actorName){
        this.actorName = actorName;
    }

    public String getStatus(){
        switch(status){
            case STATUS_IDLE:
                return "未启动";
            case STATUS_CONNECT:
                return "正在建立连接";
            case STATUS_ACC_LOGIN:
            case STATUS_ACC_LOGIN_DOING:
                return "正在登录帐户";
            case STATUS_ACC_REG:
            case STATUS_ACC_REG_DOING:
                return "正在注册帐户";
            case STATUS_GET_ACTOR_LIST:
            case STATUS_GET_ACTOR_LIST_DOING:
                return "正在下载角色列表";
            case STATUS_ACTOR_LOGIN:
            case STATUS_ACTOR_LOGIN_DOING:
                return "正在登录角色";
            case STATUS_ACTOR_CREATE:
            case STATUS_ACTOR_CREATE_DOING:
                return "正在创建角色";
            case STATUS_RUNNING:
                return "正在运行";
            case STATUS_ERROR:
                return errorMessage;
        }

        return "未知状态";
    }

    public void addSendBytes(int size){
        sendBytes += size;
        sendCount++;
        dirty();
    }

    public void addRecvBytes(int size){
        recvBytes += size;
        recvCount++;
        dirty();
    }

    public String getSendInfo(){
        return sendCount + " : " + getNetInfo(sendBytes);
    }

    public String getRecvInfo(){
        return recvCount + " : " + getNetInfo(recvBytes);
    }

    public int getPositionCount(){
        return positionCount;
    }

    public int getAttackCount(){
        return attackCount;
    }

    public int getChatCount(){
        return chatCount;
    }

    public int getTimeGap(){
        return timeGap;
    }

    private String getNetInfo(int bytes){
        StringBuffer sb = new StringBuffer();

        if(bytes > 1048510){
            sb.append(bytes / 1048510);
            sb.append('.');
            sb.append((bytes % 1048510) * 10 / 1048510);
            sb.append('M');
        }else if(bytes > 1024){
            sb.append(bytes / 1024);
            sb.append('.');
            sb.append((bytes % 1024) * 10 / 1024);
            sb.append('K');
        }else{
            sb.append(bytes);
            sb.append('B');
        }

        return sb.toString();
    }

    public String getRunTime(){
        long runTime = System.currentTimeMillis() - startTime;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeInMillis(cal.getTimeInMillis() + runTime);

        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.HOUR_OF_DAY));
        sb.append(':');
        sb.append(cal.get(Calendar.MINUTE));
        sb.append(':');
        sb.append(cal.get(Calendar.SECOND));

        return sb.toString();
    }

    public String getAvgBytes(){
        int secondBytes = (int) (((long) sendBytes + recvBytes) * 1000 / (System.currentTimeMillis() - startTime));
        int hourBytes = (int) (((long) sendBytes + recvBytes) * 3600 * 1000 / (System.currentTimeMillis() - startTime));

        StringBuffer sb = new StringBuffer();

        sb.append(getNetInfo(secondBytes));
        sb.append("/S ");
        sb.append(getNetInfo(hourBytes));
        sb.append("/H");

        return sb.toString();
    }

    public void setRunning(boolean running){
        this.running = running;

        if(running){
            status = STATUS_IDLE;
            oldSstatus = STATUS_IDLE;

            targetLevel = 0;
            
            sendBytes = 0;
            sendCount = 0;
            recvBytes = 0;
            recvCount = 0;

            timeGap = 0;
            lastServerTime = 0;
            lastPositionTime = 0;
            lastAttackTime = 0;
            lastChatTime = 0;
            positionCount = 0;
            attackCount = 0;
            chatCount = 0;
            startTime = System.currentTimeMillis();

            connection = null;
            errorMessage = "出错终止";
            dirty = false;
        }else{
            if(connection != null){
                connection.close();
            }

            status = STATUS_IDLE;
        }
    }

    public boolean getRunning(){
        return running;
    }

    public static SanguoClient createFromString(String data){
        String[] ss = splitString(data, ',');
        SanguoClient client = new SanguoClient();
        client.setAccountName(ss[0].trim());
        client.setAccountPassword(ss[1].trim());

        return client;
    }

    private static String[] splitString(String s, char ch){
        int startIndex = 0;
        int endIndex = 0;
        Vector<String> vS = new Vector<String>();
        while(true){
            endIndex = s.indexOf(ch, startIndex);
            if(endIndex == -1){
                vS.addElement(s.substring(startIndex));
                break;
            }else{
                vS.addElement(s.substring(startIndex, endIndex));
                startIndex = endIndex + 1;
            }
        }
        String[] strs = new String[vS.size()];
        vS.copyInto(strs);
        return strs;
    }
}
