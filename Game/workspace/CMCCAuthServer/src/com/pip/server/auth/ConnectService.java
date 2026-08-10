package com.pip.server.auth;

import com.pip.server.auth.net.UWAPSegment;

/**
 * 管理世界服务器连接。
 */
public class ConnectService {
    /*
     * 最多20个世界服务器连接。
     */
    private ConnectSession[] connects = new ConnectSession[20];

    public ConnectService() {
    }

    /**
     * 添加一个世界服务器连接。
     * @param session
     */
    public void addConnect(ConnectSession session) {
        synchronized (this) {
            for (int i = 0; i < connects.length; i++) {
                if (connects[i] == null) {
                    connects[i] = session;
                    break;
                }
            }
        }
    }

    /**
     * 向所有世界服务器广播消息。通常用于同步账户余额。
     * @param seg
     */
    public void broadcast(UWAPSegment seg) {
        for (int i = 0; i < connects.length; i++) {
            if (connects[i] != null)
                connects[i].write(seg);
        }
    }

    /**
     * 删除一个世界服务器连接。
     * @param session
     */
    public void removeConnect(ConnectSession session) {
        synchronized (this) {
            for (int i = 0; i < connects.length; i++) {
                if (connects[i] == session)
                    connects[i] = null;
            }
        }
    }
    
    /**
     * 取得所有的连接。
     */
    public ConnectSession[] getAllConnects() {
        return connects;
    }
}
