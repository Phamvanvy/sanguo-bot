package com.pip.itimes.server.connect;

import org.apache.mina.common.IoSession;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPData;

public class CmccClientSession extends ClientSession{
    public CmccClientSession(IoSession session) {
        super(session);
    }

    public void cmccCharge(UWAPData data) throws Exception{
        String userId = data.readString();
        String key = data.readString();
        int charge = data.readInt();
        UWAPSegment segment = new UWAPSegment(ClientConstants.CMCC_CHARGE,data.getSerial(),getSessionId());
        segment.writeString(userId);
        segment.writeString(key);
        segment.writeInt(charge);
        authSession.write(segment);
    }

    public void register(UWAPData data) throws Exception {
        log.debug("start register");
//        if(Server.isMaintance)
//            throw new ITimesException("服务器正在维护状态",data.getSerial(),data.getAppType());
        String name = data.readString();
        String phone = data.readString();
        String recommend = data.readString();
        String model = data.readString();
        String versionString = data.readString();
        boolean needReturn = data.readBoolean();
        String cmccUserId = data.readString().trim();
        String cmccKey = data.readString();
        version = versionService.getVersion(versionString);
        if ("NK-NGage".equals(model) || "MotoV300".equals(model)) {
            needFastSyncMode = true;
        }
        if (version == null) {
            throw new ITimesException("不支持的版本", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (version.getStatus() == Version.STATUS_CANCELED) {
            throw new ITimesException(version.getMessage(), data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (!version.isCanReg()) {
            throw new ITimesException("此版本不支持注册功能", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }

        UWAPSegment seg = new UWAPSegment(ClientConstants.ACCOUNT_REG,
                                          data.getSerial(), getSessionId());
        seg.writeString(name);
        seg.writeString(phone);
        seg.writeString(recommend);
        seg.writeString(model);
        seg.writeString(versionString);
        seg.writeStrings(version.getCharge());
        seg.writeString(version.getFeeplan());
        seg.writeBoolean(needReturn);
        seg.writeString(cmccUserId);
        seg.writeString(cmccKey);
        authSession.write(seg);
        log.debug("end register");
    }

    public void login(UWAPData data) throws Exception {
        synchronized (this) {

            accountName = data.readString();
            password = data.readString();
            model = data.readString();
            String versionString = data.readString();
            version = versionService.getVersion(versionString);

            String cmccUserId;
            String cmccKey;

            if(accountName.startsWith("test") || accountName.startsWith("gm")){
                cmccUserId = "";
                cmccKey = "";
            }else{
                cmccUserId = data.readString().trim();
                cmccKey = data.readString();
            }

            log.info("Version[" + versionString + "]");

            if (!accountName.startsWith("test") && Server.isMaintance)
                throw new ITimesException("服务器正在维护状态", data.getSerial(),
                                          data.getAppType());
            if ("NK-NGage".equals(model) || "MotoV300".equals(model)) {
                needFastSyncMode = true;
            }
            if (version == null) {
                throw new ITimesException("不支持的版本", data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            if (version.getStatus() == Version.STATUS_CANCELED) {
                throw new ITimesException(version.getMessage(), data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN,
                                              data.getSerial(), getSessionId());
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString(model);
            seg.writeString(versionString);
            seg.writeStrings(version.getCharge());
            seg.writeString(version.getFeeplan());
//            seg.writeBoolean(version.isCharge());
//            seg.writeInt(version.getFeeplan());
            seg.writeString(cmccUserId);
            seg.writeString(cmccKey);
            authSession.write(seg);
        }
//        Account a = accountDao.getAccountByNameAndPassword(name,password);
//        if(a!=null){
//            accountId = a.getId();
//            accountLogined = true;
//            UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN_OK,data.getSerial());
//            write(seg);
//        }

//        authSession.forward(data, sessionId);
    }

    protected void quickReg(UWAPData data) throws Exception{
//        if(Server.isMaintance)
//            throw new ITimesException("服务器正在维护状态",data.getSerial(),data.getAppType());
        String phone = data.readString();
        String version = data.readString();
        String model = data.readString();
        String cmccUserId = data.readString().trim();
        String cmccKey = data.readString();
        UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,data.getSerial(),getSessionId());
        seg.writeString(phone);
        seg.writeString(version);
        seg.writeString(model);
        seg.writeString(cmccUserId);
        seg.writeString(cmccKey);
        authSession.write(seg);
    }

    protected void relogin(UWAPData data) throws Exception{
//        UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
//                                          data.getSerial(),
//                                          data.getSessionId());
//        seg.write((byte) 2);
//        write(seg);

        if(Server.isMaintance){
            throw new ITimesException("服务器正在维护状态",data.getSerial(),data.getAppType());
        }
        synchronized(this){
            accountName = data.readString();
            password = data.readString();
            playerName = data.readString();
            model = data.readString();
            String versionString = data.readString();
            byte type = data.readByte();
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            version = versionService.getVersion(versionString);
            if ("NK-NGage".equals(model) || "MotoV300".equals(model)) {
                needFastSyncMode = true;
            }
            if (version == null) {
                throw new ITimesException("不支持的版本", data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            if (version.getStatus() == Version.STATUS_CANCELED) {
                throw new ITimesException(version.getMessage(), data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN,
                                              data.getSerial(), getSessionId());
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString(playerName);
            seg.writeString(model);
            seg.writeString(versionString);
            seg.writeStrings(version.getCharge());
            seg.writeString(version.getFeeplan());
            seg.write(type);
            seg.writeString(cmccUserId);
            seg.writeString(cmccKey);
            authSession.write(seg);
        }
    }
}
