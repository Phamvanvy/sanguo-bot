package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.stage.IStoreItem;
import com.pip.itimes.server.stage.RoleFaceData;
import com.pip.itimes.server.stage.HouseTemplate;
import com.pip.itimes.server.stage.HousePart;
import java.util.Random;

public class StoreService {

    private Map<Integer,Request> id2request = new HashMap<Integer,Request>();
    private Map<Integer,ChargeRequest> id2charge = new HashMap<Integer,ChargeRequest>();
    private Map<Integer,CmccHistoryRequest> id2history = new HashMap<Integer,CmccHistoryRequest>();
    private AtomicInteger id = new AtomicInteger(0);
    private boolean needAddConsumeCode;

    public StoreService(boolean needAddConsumeCode) {
        this.needAddConsumeCode = needAddConsumeCode;
    }

    //mengjie add 为他人购买
    public Request request(WorldPlayer player,IStoreItem item,int count,int money,int serial,ConnectSession session,String forothername){
        Request request = new Request(id.incrementAndGet(),player.getId(),item,count,money,serial,session,forothername);
        if(needAddConsumeCode){
            return addConsumeCode(request,money);
        }
        id2request.put(request.id,request);
        return request;
    }

    public Request request(WorldPlayer player,RoleFaceData face,int money,int serial,ConnectSession session){
        Request request = new Request(id.incrementAndGet(),player.getId(),face,money,serial,session);
        if (needAddConsumeCode) {
            return addConsumeCode(request, money);
        }
        id2request.put(request.id,request);
        return request;
    }

    public Request request(WorldPlayer player,HouseTemplate house,int money,short areaId,int serial,ConnectSession session){
        Request request = new Request(id.incrementAndGet(),player.getId(),house,money,areaId,serial,session);
        if(needAddConsumeCode){
            return addConsumeCode(request,money);
        }
        id2request.put(request.id,request);
        return request;
    }

    public Request request(WorldPlayer player,HouseTemplate house,int money,int serial,ConnectSession session){
        Request request = new Request(id.incrementAndGet(),player.getId(),house,money,serial,session);
        if(needAddConsumeCode){
            return addConsumeCode(request,money);
        }
        id2request.put(request.id,request);
        return request;
    }

    public Request request(WorldPlayer player,HousePart part,int money,int serial,ConnectSession session){
        Request request = new Request(id.incrementAndGet(),player.getId(),part,money,serial,session);
        if(needAddConsumeCode){
            return addConsumeCode(request,money);
        }
        id2request.put(request.id,request);
        return request;
    }

    public Request request(WorldPlayer player,int money,String consumeCode,int serial,int type,ConnectSession session){
        Request request = new Request(id.incrementAndGet(),player.getId(),money,consumeCode,serial,type,session);
        if(needAddConsumeCode){
            return addConsumeCode(request,money);
        }
        id2request.put(request.id,request);
        return request;
    }

    public ChargeRequest charge(int value,WorldPlayer player,int serial,int sessionId,ConnectSession sesssion){
        ChargeRequest request = new ChargeRequest(id.incrementAndGet(),value,player,serial,sessionId,sesssion);
        id2charge.put(request.id,request);
        return request;
    }

    public CmccHistoryRequest history(int type,String begin,String end, int startSequence, int pageSize, ConnectSession session, int sessionId, int serial){
        CmccHistoryRequest request = new CmccHistoryRequest(id.incrementAndGet(),type,begin,end,startSequence,pageSize, session, sessionId, serial);
        id2history.put(request.id, request);
        return request;
    }

    public CmccHistoryRequest removeHistory(int requestId){
        return id2history.remove(requestId);
    }

    public ChargeRequest removeCharge(int id){
        return id2charge.remove(id);
    }

    public void addCmccSmsRequest(Request request){
        id2request.put(request.id, request);
    }
    
    public Request get(int id) {
        return id2request.get(id);
    }
    
    public Request remove(int id){
        return id2request.remove(id);
    }

    private Random rnd = new Random();

    protected Request addConsumeCode(Request request,int money){
        String s = getConsumeCode(money);
        if(s==null)
            return null;
        request.consumeCode = s;
        id2request.put(request.id,request);
        return request;
    }

    protected String getConsumeCode(int value){
        String[] s = ConsumeCodes.getConsumeCode(value);
        if(s==null)
            return null;
        else{
            if(s.length==1)
                return s[0];
            else{
                return s[rnd.nextInt(s.length)];
            }
        }
    }

    public static class Request {
        public static final int ITEM = 0;
        public static final int FACE = 1;
        public static final int HOUSE = 2;
        public static final int STYLE = 3;
        public static final int PART = 4;
        public static final int WAITER = 5;
        public int type;  //0 item 1 face
        public int id;
        public int playerId;
        public IStoreItem item;
        public RoleFaceData face;
        public HouseTemplate ht;
        public HousePart hp;
        public int count;
        public int price;
        public int serial;
        public String consumeCode; //专门为卓望版本定制，如果为空那么就是pip版本，如果不为空那么就是卓望版本
        public ConnectSession session;
        public String cmccSmsCode; //专门为卓望版本定制，存储短信代码
        public boolean cmccSmsMode = false; //转为卓望版本定制，如果为true，则为cmcc短信支付成功后的结果，需要将物品通过Changed发往客户端
        public String cmccUserId; // 短信购买时，存储平台用户ID
        
        //mengjie add 为他人购买
        public String forothername;
        
        //zxyu add
        public boolean sendClient = false;		//默认不需要发送到客户端
        public byte sendClientType;				//下发客户端的类型
        public int sendClientCount;				//下发客户端的个数
        public UWAPData sendClentData;
        public int sendSetup = 0;
        public int sendSessionId;
        public boolean sendItem = true;			//默认发送物品 不发送时为隐藏商品
        public int delitemid = -1;			//删除的物品 在失败的时候可以进行恢复
        public int delitemcount = 0;
        
        public Request(int id, int playerId,IStoreItem item,int count,int price,int serial,ConnectSession session,String forothername) {
            this.type = ITEM;
            this.id = id;
            this.playerId = playerId;
            this.item = item;
            this.count = count;
            this.serial = serial;
            this.price = price;
            this.consumeCode = item.consumeCode;
            this.session = session;
            this.forothername = forothername;
        }

        public Request(int id,int playerId,RoleFaceData face,int price,int serial,ConnectSession session){
            this.type = FACE;
            this.id = id;
            this.playerId = playerId;
            this.face = face;
            this.price = price;
            this.serial = serial;
            this.consumeCode = face.getConsumeCode();
            this.session = session;
        }

        public Request(int id,int playerId,HouseTemplate ht,int price,short areaId,int serial,ConnectSession session){
            this.type = HOUSE;
            this.id = id;
            this.playerId = playerId;
            this.ht = ht;
            this.price = price;
            this.consumeCode = ht.getConsumeCode();
            this.count = areaId;
            this.serial = serial;
            this.session = session;
        }

        public Request(int id,int playerId,HouseTemplate ht,int price,int serial,ConnectSession session){
            this.type = STYLE;
            this.id = id;
            this.playerId = playerId;
            this.ht = ht;
            this.price = price;
            this.consumeCode = ht.getStyleConsumeCode();
            this.serial = serial;
            this.session = session;
        }

        public Request(int id,int playerId,HousePart hp,int price,int serial,ConnectSession session){
            this.type = PART;
            this.id = id;
            this.playerId = playerId;
            this.hp = hp;
            this.price = price;
            this.consumeCode = hp.getConsumeCode();
            this.serial = serial;
            this.session = session;
        }

        public Request(int id,int playerId,int price,String consumeCode,int serial,int type,ConnectSession session){
            this.type = type;
            this.id = id;
            this.playerId = playerId;
            this.price = price;
            this.consumeCode = consumeCode;
            this.serial = serial;
            this.session = session;
        }
    }

    public static class ChargeRequest{
        public int id;
        public int value;
        public int serial;
        public int playerId;
        public ConnectSession connectSession;
        public int sessionId;

        public ChargeRequest(int id,int value,WorldPlayer player,int serial,int sessionId,ConnectSession session){
            this.id = id;
            this.value = value;
            this.serial = serial;
            this.sessionId = sessionId;
            this.connectSession = session;
            if(player!=null){
                playerId = player.getId();
            }else{
                playerId = -1;
            }
        }
    }

    public static class CmccHistoryRequest{
    public int id;
    public ConnectSession session;
    public int serial;
    public int sessionId;
    public String begin;
    public String end;
    public int startSequence;
    public int pageSize;
    public int type;  //1 consume history 2 charge history

    public CmccHistoryRequest(int id,int type,String begin,String end, int startSequence, int pageSize, ConnectSession session, int sessionId, int serial){
            this.id = id;
            this.begin = begin;
            this.end = end;
            this.type = type;
            this.startSequence = startSequence;
            this.pageSize = pageSize;
            this.session = session;
            this.sessionId = sessionId;
            this.serial = serial;
    }
}

}
