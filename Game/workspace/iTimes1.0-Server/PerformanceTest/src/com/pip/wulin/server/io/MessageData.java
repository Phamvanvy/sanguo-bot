package com.pip.wulin.server.io;

import java.io.*;

/** 聊天或通知消息描述对象 */
public class MessageData {
    /** 特殊ID：所有人 */
    public static final int EVERYBODY = -1;
    /** 特殊ID：系统 */
    public static final int SYSTEMBODY = -2;
    /** 特殊ID：场景中的所有人 */
    public static final int SCENEBODY = -3;
    /** 特殊ID：小队 */
    public static final int TEAMBODY = -4;
    /** 特殊ID：关卡中的所有人 */
    public static final int AREABODY = -5;
    /** 特殊ID：帮会中的所有人 */
    public static final int TONYBODY = -6;

    /** 发送方ID */
    public int fromId = 0;
    /** 发送方名字 */
    public String fromName = "";
    /** 接收方ID */
    public int toId = EVERYBODY;
    /** 接收方名字，可以为空 */
    public String toName = "";
    /** 发言类型：0 - 喊，1 - 说 */
    public int saidType = 0;
    /** 消息体 */
    public String msg;

    /** 把消息对象转换为UWAP数据包。 */
    public UWAPSegment getSeg() throws IOException {
        UWAPSegment rt = new UWAPSegment(ServerConstants.SENDMESSAGE);
        rt.writeInt(fromId);
        rt.writeString(fromName);
        rt.writeInt(toId);
        rt.writeString(toName);
        rt.writeInt(saidType);
        rt.writeString(msg);
        return rt;
    }

    /** 从UWAP数据包中读取消息对象。 */
    public static MessageData readFrom(UWAPData data) throws Exception {
        if (data.getAppType() != ServerConstants.SENDMESSAGE) {
            throw new Exception("不是消息包");
        }
        MessageData rt = new MessageData();
        rt.fromId = data.readInt();
        rt.fromName = data.readString();
        rt.toId = data.readInt();
        rt.toName = data.readString();
        rt.saidType = data.readInt();
        rt.msg = data.readString();
        return rt;
    }
}
