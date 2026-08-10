package com.pip.itimes.server.connect;

import java.util.*;

import com.pip.itimes.net.*;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.Bbs;
import com.pip.itimes.server.dao.BbsDao;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.dao.DataAccessException;

public class BbsService {

    private BbsDao dao;
    private Map forbids = new HashMap();
    private Set forbidenbbsId = new HashSet();

    public BbsService(BbsDao dao) {
        this.dao = dao;
    }

    public void addBbs(ClientSession stub, UWAPData data) throws
            ITimesException {
        try {
            if(isForbiden(stub.getPlayerId()))
                throw new ITimesException("已经禁用此功能",data.getSerial(),data.getSessionId(),data.getAppType());

            int bbsId = data.readInt();
            if(forbidenbbsId.contains(new Integer(bbsId)))
                throw new ITimesException("此BBS不支持提交",data.getSerial(),data.getSessionId(),data.getAppType());
            String title = data.readString();
            String content = data.readString();
            Bbs bbs = new Bbs();
            bbs.setBbsId(bbsId);
            bbs.setPlayerId(stub.getPlayerId());
            bbs.setPlayerName(stub.getPlayerName());
            bbs.setTitle(title);
            bbs.setContent(content);
            bbs.setPostTime(new Date());
            bbs.setPriority(100);
            dao.addBbs(bbs);
            UWAPSegment seg = new UWAPSegment(ClientConstants.BBS_POST_OK,
                                  data.getSerial());
            stub.write(seg);
        }
        catch(ITimesException ex){
            throw ex;
        }
        catch (Exception e) {
            throw new ITimesException("提交BBS出错", data.getSerial(),
                                      data.getAppType());
        }

    }

    public void addBbs(int bbsId, int playerId, String playerName, String title,
                       String content, int priority) throws DataAccessException {
        Bbs bbs = new Bbs();
        bbs.setBbsId(bbsId);
        bbs.setPlayerId(playerId);
        bbs.setPlayerName(playerName);
        bbs.setTitle(title);
        bbs.setContent(content);
        bbs.setPostTime(new Date());
        bbs.setPriority(priority);
        dao.addBbs(bbs);
    }

    public void getBbsList(ClientSession stub, UWAPData data) throws
            ITimesException {
        try {
            int bbsId = data.readInt();
            int pageSize = data.readShort();
            int pageNo = data.readInt();
            int total = dao.getBbsCount(bbsId);
            if (pageNo * pageSize >= total) {
                throw new ITimesException("没有可显示的公告", data.getSerial(),
                                          data.getSessionId(), data.getAppType());
            }
            int pageCount = total / pageSize;
            if (total % pageSize != 0)
                pageCount++;
            List l = dao.getBbsList(bbsId, pageNo * pageSize, pageSize);
            int retCount = l.size();
            UWAPSegment seg = new UWAPSegment(ClientConstants.BBS_LIST,
                                              data.getSerial());
            seg.writeInt(bbsId);
            seg.writeShort((short) pageSize);
            seg.writeInt(pageNo);
            seg.writeInt(pageCount);
            seg.writeShort((short) retCount);
            for (int i = 0; i < l.size(); i++) {
                Bbs bbs = (Bbs) l.get(i);
                seg.writeInt(bbs.getId());
                seg.writeString(bbs.getPlayerName());
                seg.writeString(bbs.getTitle());
                seg.writeString(Utils.getDateString(bbs.getPostTime()));
            }
            stub.write(seg);
        } catch (ITimesException e) {
            throw e;
        } catch (Exception e) {
            throw new ITimesException("读取BBS列表出错", data.getSerial(),
                                      data.getAppType());
        }
    }


    public void getContent(ClientSession stub, UWAPData data) throws
            ITimesException {
        try {
            int id = data.readInt();
            Bbs bbs = dao.getBbs(id);
            UWAPSegment seg = new UWAPSegment(ClientConstants.BBS_CONTENT,
                                              data.getSerial());
            seg.writeInt(bbs.getId());
            seg.writeInt(bbs.getPlayerId());
            seg.writeString(bbs.getPlayerName());
            seg.writeString(bbs.getTitle());
            seg.writeString(bbs.getContent());
            seg.writeString(Utils.getDateString(bbs.getPostTime()));
            stub.write(seg);
        } catch (Exception e) {
            throw new ITimesException("读取BBS内容出错", data.getSerial(),
                                      data.getAppType());
        }
    }

    public void addForbiden(int id, int second) {
        if (second == 0) {
            forbids.remove(new Integer(id));
        } else {
            Forbiden forbiden = new Forbiden(id,
                                             System.currentTimeMillis() +
                                             second * 1000);
            forbids.put(new Integer(id), forbiden);
        }
    }

    public boolean isForbiden(int id) {
        Forbiden f = (Forbiden) forbids.get(new Integer(id));
        if (f == null)
            return false;
        return System.currentTimeMillis() < f.validTime;
    }

    public void setForbidenBbs(Set ids){
        forbidenbbsId = ids;
    }
}


class Forbiden {
    int id;
    long validTime;
    public Forbiden(int id, long validTime) {
        this.id = id;
        this.validTime = validTime;
    }
}
