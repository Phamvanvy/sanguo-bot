package com.pip.sanguo.performancetest.client;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import com.pip.sanguo.performancetest.net.UWAPSegment;
import com.pip.sanguo.performancetest.net.UWAPSocketConnection;

public class iTimesClient implements Runnable{
    private String accountName;
    private String accountPassword;
    private String actorName;
    private boolean running;
    private int status;
    private int oldSstatus;

    private boolean dirty;
    private String errorMessage;
    private UWAPSocketConnection connection;

    private int sendCount;
    private int sendBytes;
    private int recvCount;
    private int recvBytes;

    private long lastPositionTime;
    private long lastChatTime;
    private int positionCount;
    private int attackCount;
    private int chatCount;
    private long startTime;
    private int lastX = 300;
    private int lastY = 200;
    private int lastMapId = 1633;

    private int timeGap;

    private List<UWAPSegment> segments = new Vector<UWAPSegment>();

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

    private static final String SERVER_URL = "socket://221.179.216.50:30001#DDB3D8327533";

    public static final byte CONN_ERROR = -1;
    public static final byte CONN_ACCOUNTREG = 1;
    public static final byte CONN_ACCOUNTREG_OK = 2;
    public static final byte CONN_ACTORCREATE = 3;
    public static final byte CONN_ACTORCREATE_OK = 4;
    public static final byte CONN_GET_ACTORLIST = 5;
    public static final byte CONN_GET_ACTORLIST_OK = 6;
    public static final byte CONN_PALYER_LOGIN = 7;
    public static final byte CONN_PALYER_LOGIN_OK = 8;
    public static final byte CONN_RELOGIN = 9;
    public static final byte CONN_RELOGIN_RESULT = 10;
    public static final byte CONN_GET_FILE = 11;
    public static final byte CONN_GOTO = 11;
    public static final byte CONN_SEND_POSITION = 12;
    public static final byte CMCC_CHARGE = 13;
    public static final byte CMCC_CHARGE_OK = 13;
    public static final byte CONN_BBS_POST = 14;
    public static final byte CONN_BBS_POST_OK = 15;
    public static final byte CONN_BBS_GET_LIST = 16;
    public static final byte CONN_GET_BBS_LIST = 16;
    public static final byte CONN_GET_BBS_CONTENT = 17;
    public static final byte CONN_BBS_CONTENT = 18;
    public static final byte CONN_FACE_LIST = 19;
    public static final byte CONN_TOUCH_NPC = 23;

    public static final byte CONN_MAIL_POST = 24;
    public static final byte CONN_MAIL_POST_OK = 24;
    public static final byte CONN_MAIL_GET_LIST = 25;
    public static final byte CONN_MAIL_LIST = 25;
    public static final byte CONN_GET_MAIL_CONTENT = 26;
    public static final byte CONN_MAIL_CONTENT = 26;
    public static final byte CONN_GET_ATTACHMENT = 27;
    public static final byte CONN_GET_ATTACHMENT_OK = 27;
    public static final byte CONN_DELETE_MAIL = 28;
    public static final byte CONN_NEWMAIL = 29;
    public static final byte CONN_QUICK_REG = 30;
    public static final byte CONN_REQUEST_FRIEND_LIST = 31;

    public static final byte CONN_USE_ITEM = 33;

    public static final byte CONN_BATTLE_RESULT = 34;
    public static final byte CONN_ADD_PROPERTY_POINT = 36;

    public static final byte CONN_TEAM_CREATE = 38;
    public static final byte CONN_TEAM_CREATE_OK = 39;
    public static final byte CONN_TEAM_INVITE = 40;
    public static final byte CONN_TEAM_INVIT_RESULT = 41;
    public static final byte CONN_TEAM_JOIN_OK = 42;
    public static final byte CONN_TEAM_JOIN_FAIL = 43;
    public static final byte CONN_TEAM_LEAVE = 44;

    public static final byte CONN_BATTLE_REQUEST = 45;
    public static final byte CONN_BATTLE_INIT = 46;
    public static final byte CONN_BATTLE_JOIN = 47;
    public static final byte CONN_BATTLE_JOIN_RESULT = 48;
    public static final byte CONN_BATTLE_START = 49;
    public static final byte CONN_BATTLE_ABORT = 50;
    public static final byte CONN_BATTLE_FIGHT = 51;
    public static final byte CONN_BATTLE_ROUND_END = 52;
    public static final byte CONN_UPLOAD = 55;
    public static final byte CONN_UPLOAD_OK = 56;

    public static final byte CONN_ISHOP_LIST = 57;
    public static final byte CONN_ISHOP_TRADE = 58;

    public static final byte CONN_SNEAK_ATTACK = 59;

    public static final byte CONN_PK_REQUEST = 60;
    public static final byte CONN_PK_CREATED = 61;
    public static final byte CONN_PK_CANCEL = 62;
    public static final byte CONN_PK_REFUSE = 63;
    public static final byte CONN_PK_OK = 64;
    public static final byte CONN_PK_START = 65;
    public static final byte CONN_PK_FIGHT = 66;
    public static final byte CONN_PK_ROUND_END = 67;

    public static final byte CONN_GENERIC_LIST = 68;
    public static final byte CONN_GENERIC_CONTENT = 69;

    public static final byte CONN_EQU_CHANGED = 70;
    public static final byte CONN_EQU_CHANGED_OK = 71;

    public static final byte CONN_CHAT_MESSAGE = 72;
    public static final byte CONN_CHAT_OPTION = 73;
    public static final byte CONN_CHATFAVORITE_LIST = 74;
    public static final byte CONN_CHATFAVORITE_DESC = 75;
    public static final byte CONN_CHANGE_CHATFAVORITE = 76;

    public static final byte CONN_LOGIN = 77;
    public static final byte CONN_LOGINOK = 78;
    public static final byte CONN_REFRESH = 80;
    public static final byte CONN_COMMAND = 81;
    public static final byte CONN_MESSAGE = 82;

    public static final byte CONN_LOOK_EQU = 83;
    public static final byte CONN_LOOK_EQU_OK = 83;

    public static final byte CONN_REQUEST_ITEM_LINK = 84;

    public static final byte CONN_STORE_ITEM_LIST = 85;
    public static final byte CONN_STORE_TRADE = 86;
    public static final byte CONN_STORE_TRADE_OK = 86;

    public static final byte CONN_ADD_FRIEND = 87;
    public static final byte CONN_ADD_FRIEND_OK = 87;

    public static final byte CONN_REQUEST_TASK_DESC = 88;
    public static final byte CONN_REQUEST_TASK_DESC_OK = 88;

    public static final byte CONN_ADD_POINT = 89;
    public static final byte CONN_ADD_POINT_OK = 89;

    public static final byte CONN_SYNCTIME = 90;

    public static final byte CONN_ADD_BLACK = 91;

    public static final byte CONN_GET_ITEM = 94;
    public static final byte CONN_GATHER = 95;
    public static final byte CONN_GATHER_OK = 96;
    public static final byte CONN_GATHER_RESULT = 97;
    public static final byte CONN_LEARN_SKILL = 98;
    public static final byte CONN_LEARN_SKILL_OK = 99;
    public static final byte CONN_SKILL_LIST = 100;
    public static final byte CONN_GET_DESC = 101;
    public static final byte CONN_DESC = 102;
    public static final byte CONN_PRODUCT = 103;
    public static final byte CONN_ABILITY_LIST = 105;
    public static final byte CONN_LEARN_ABILITY = 106;
    public static final byte CONN_LEARN_ABILITY_OK = 107;
    public static final byte CONN_TASK_COMPLETED = 108;
    public static final byte CONN_TASK_COMPLETED_OK = 108;

    public static final byte CONN_SHOP_CREATE_OK = 110;
    public static final byte CONN_SHOP_LIST = 111;
    public static final byte CONN_REQUEST_SHOP_ITEM_LIST = 112;
    public static final byte CONN_SHOP_ITEM_LIST = 112;
    public static final byte CONN_SHOP_ADD_ITEM = 113;
    public static final byte CONN_SHOP_ADD_ITEM_OK = 113;
    public static final byte CONN_SHOP_REMOVE_ITEM = 114;
    public static final byte CONN_SHOP_REMOVE_ITEM_OK = 114;
    public static final byte CONN_SHOP_MONEY_CHANGE = 115;
    public static final byte CONN_SHOP_MONEY_CHANGE_OK = 115;
    public static final byte CONN_SHOP_CHANGE = 116;
    public static final byte CONN_SHOP_CHANGE_OK = 116;

    public static final byte CONN_AUCTION_TYPE_LIST = 117;
    public static final byte CONN_REQUEST_AUCTION_LIST = (byte) 118;
    public static final byte CONN_AUCTION_LIST = (byte) 118;
    public static final byte CONN_AUCTION_REQUEST_ITEM_DESC = (byte) 119;
    public static final byte CONN_AUCTION_ITEM_DESC = (byte) 119;
    public static final byte CONN_AUCTION_PRICE = (byte) 120;
    public static final byte CONN_AUCTION_PRICE_OK = (byte) 120;
    public static final byte CONN_AUCTION_ITEM = (byte) 121;
    public static final byte CONN_AUCTION_ITEM_OK = (byte) 121;

    public static final byte CONN_BUY_MATERIAL_TYPE_LIST = (byte) 122;
    public static final byte CONN_REQUEST_BUY_MATERIAL_LIST = (byte) 123;
    public static final byte CONN_BUY_MATERIAL_LIST = (byte) 123;
    public static final byte CONN_SELL_MATERIAL = (byte) 124;
    public static final byte CONN_SELL_MATERIAL_OK = (byte) 124;
    public static final byte CONN_OEM_TYPE_LIST = (byte) 125;
    public static final byte CONN_REQUEST_OEM_LIST = (byte) 126;
    public static final byte CONN_OEM_LIST = (byte) 126;
    public static final byte CONN_OEM = (byte) 127;
    public static final byte CONN_OEM_OK = (byte) 127;

    public static final byte CONN_TONG_CREATE_OK = (byte) 130;
    public static final byte CONN_REQUEST_TONG_MEMBERS = (byte) 131;
    public static final byte CONN_TONG_MEMBERS_LIST = (byte) 131;
    public static final byte CONN_TONG_GRANT = (byte) 132;
    public static final byte CONN_TONG_GRANT_OK = (byte) 132;
    public static final byte CONN_TONG_MODIFY_TITLE = (byte) 133;

    public static final byte CONN_FRIEND_STATUS = (byte) 134;

    public static final byte CONN_TASK_ABANDON = (byte) 135;
    public static final byte CONN_TASK_ABANDON_RESULT = (byte) 135;

    public static final byte CONN_ADD_PET_POINT = (byte) 136;
    public static final byte CONN_ADD_PET_POINT_OK = (byte) 136;
    public static final byte CONN_BUY_PET_POINT = (byte) 137;
    public static final byte CONN_USE_PET = (byte) 138;
    public static final byte CONN_USE_PET_OK = (byte) 138;
    public static final byte CONN_PET_FEED = (byte) 139;
    public static final byte CONN_DELETE_USER = (byte) 140;
    public static final byte CONN_DELETE_USER_OK = (byte) 140;
    public static final byte CONN_CHANGE_OPTION = (byte) 141;
    public static final byte CONN_REPAIRE_LIST = (byte) 142;
    public static final byte CONN_REPAIRE = (byte) 143;
    public static final byte CONN_REPAIRE_OK = (byte) 143;

    public static final byte CONN_NETSPEED_TEST = (byte) 177; //测试网速

    public static final byte CONN_KILL_SERVER = (byte) 190;
    public static final byte CONN_BILLING_OK = (byte) 205;

    public iTimesClient(){
        accountName = null;
        accountPassword = null;
        actorName = null;
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
                        connection = new UWAPSocketConnection(SERVER_URL, this);
                        new Thread(connection).start();

                        if(accountPassword != null && accountPassword.length() > 0){
                            status = STATUS_ACC_LOGIN;
                        }else{
                            status = STATUS_ACC_REG;
                        }
                    }catch(Exception e){
                        e.printStackTrace();

                        errorMessage = "建立连接失败";
                        status = STATUS_ERROR;
                        dirty();
                    }
                }
                    break;
                case STATUS_ACC_LOGIN: {
                    UWAPSegment segment = new UWAPSegment(CONN_LOGIN);
                    segment.writeString(accountName);
                    segment.writeString(accountPassword);
                    segment.writeString("NK-6600");
                    segment.writeString("3.3");
                    segment.writeString("");
                    segment.writeString("");
                    segment.writeString("");
                    connection.writeSegment(segment);

                    status = STATUS_ACC_LOGIN_DOING;
                }
                    break;
                case STATUS_ACC_LOGIN_DOING:
                    break;
                case STATUS_ACC_REG: {
                    UWAPSegment segment = new UWAPSegment(CONN_ACCOUNTREG);
                    segment.writeString(accountName);
                    segment.writeString("13801381038");
                    segment.writeString("");
                    segment.writeString("NK-6600");
                    segment.writeString("3.3");
                    segment.writeBoolean(true);
                    segment.writeString("");
                    segment.writeString("");
                    segment.writeString("");
                    connection.writeSegment(segment);

                    status = STATUS_ACC_REG_DOING;
                }
                    break;
                case STATUS_GET_ACTOR_LIST: {
                    UWAPSegment segment = new UWAPSegment(CONN_GET_ACTORLIST);
                    segment.writeString(accountName);
                    segment.writeString(accountPassword);
                    connection.writeSegment(segment);
                    status = STATUS_GET_ACTOR_LIST_DOING;
                }
                    break;
                case STATUS_ACTOR_LOGIN: {
                    UWAPSegment segment = new UWAPSegment(CONN_PALYER_LOGIN);
                    segment.writeString(actorName);
                    connection.writeSegment(segment);

                    status = STATUS_ACTOR_LOGIN_DOING;
                }
                    break;
                case STATUS_ACTOR_CREATE: {
                    UWAPSegment segment = new UWAPSegment(CONN_ACTORCREATE);
                    segment.writeString(accountName);
                    segment.writeByte((byte) 0);
                    segment.writeInt(0);
                    connection.writeSegment(segment);

                    status = STATUS_ACTOR_CREATE_DOING;
                }
                    break;
                case STATUS_ACTOR_CREATE_DOING:
                    break;
                case STATUS_RUNNING: {
                    long now = System.currentTimeMillis();

                    if(now - lastPositionTime > 5000){
                        //发move包
                        switch(ClientManager.rand.nextInt(5)){
                            case 0:
                                // 1/5概率大面积移动
                                lastX = ClientManager.rand.nextInt(100) + 250;
                                lastY = ClientManager.rand.nextInt(100) + 150;
                                break;
                            case 1:
                                lastX -= 32;
                                break;
                            case 2:
                                lastX += 32;
                                break;
                            case 3:
                                lastY -= 32;
                                break;
                            case 4:
                                lastY += 32;
                                break;
                        }

                        UWAPSegment segment = new UWAPSegment(CONN_SEND_POSITION);
                        segment.writeShort((short) lastMapId);
                        segment.writeShort((short) lastX);
                        segment.writeShort((short) lastY);
                        segment.writeBoolean(true);
                        connection.writeSegment(segment);

                        lastPositionTime = now;
                    }

                    if(now - lastChatTime > 60000){
                        //聊天包
                        UWAPSegment segment = new UWAPSegment(CONN_CHAT_MESSAGE);
                        segment.writeInt(0);
                        segment.writeString("");
                        segment.writeInt(-2);
                        segment.writeString("请问。。。有人在吗？为什么没人理我撒！");
                        connection.writeSegment(segment);

                        lastChatTime = now;
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

    private void cycleSegment(UWAPSegment segment){
        switch(segment.type){
            case CONN_ERROR: {
                byte type = segment.readByte();
                String message = segment.readString();
                errorMessage = message;
                status = STATUS_ERROR;
            }
                break;
            case CONN_LOGINOK: {
                status = STATUS_GET_ACTOR_LIST;
            }
                break;
            case CONN_ACCOUNTREG_OK: {
                String pass = segment.readString();
                String message = segment.readString();

                accountPassword = pass;
                status = STATUS_ACC_LOGIN;
            }
                break;
            case CONN_GET_ACTORLIST_OK: {
                int count = segment.readByte();

                if(count > 0){
                    String name = segment.readString();
                    int level = segment.readShort();
                    int sex = segment.readByte();
                    int reborn = segment.readByte();

                    actorName = name;
                    status = STATUS_ACTOR_LOGIN;
                }else{
                    status = STATUS_ACTOR_CREATE;
                }
            }
                break;
            case CONN_PALYER_LOGIN_OK: {
                status = STATUS_RUNNING;
            }
                break;
            case CONN_ACTORCREATE_OK: {
                int id = segment.readInt();
                String name = segment.readString();
                segment.readBytes();
                segment.readInt();
                segment.readInt();

                actorName = name;
                status = STATUS_ACTOR_LOGIN;
            }
                break;
            case CONN_SEND_POSITION:
                positionCount++;
                dirty();

                break;
            case CONN_CHAT_MESSAGE:
                chatCount++;
                dirty();

                break;
        }
    }

    private void cycleSegments(){
        synchronized(segments){
            while(segments.size() > 0){
                UWAPSegment segment = segments.remove(0);
                cycleSegment(segment);
            }
        }
    }

    public void processSegment(UWAPSegment segment){
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

            sendBytes = 0;
            sendCount = 0;
            recvBytes = 0;
            recvCount = 0;

            timeGap = 0;
            lastPositionTime = 0;
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

    public static iTimesClient createFromString(String data){
        String[] ss = splitString(data, ',');
        iTimesClient client = new iTimesClient();
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
