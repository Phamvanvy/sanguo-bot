package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.LeaveMessage;
import java.util.List;

public class LeaveMessageDao extends BaseDao {

    public LeaveMessageDao() {
    }

    public void addLeaveMessage(LeaveMessage lm) throws DataAccessException {
        makePersistent(lm);
    }

    public List getLeaveMessageList(int playerId, int begin, int maxCount) throws
            DataAccessException {
        return getLimitedList("from LeaveMessage l where l.ownerId=" + playerId +" order by l.createTime desc",
                              begin, maxCount);
    }

    public LeaveMessage getLeaveMessage(int id) throws DataAccessException {
        return (LeaveMessage) getObject(LeaveMessage.class, new Integer(id));
    }

    public int getLeaveMessageCount(int playerId) throws DataAccessException {
        return getCount("from LeaveMessage l where l.ownerId=" + playerId);
    }

    public LeaveMessage deleteMessage(int id) throws DataAccessException{
        LeaveMessage bbs = (LeaveMessage)getObject(LeaveMessage.class,new Integer(id));
        if(bbs!=null)
            makeTransient(bbs);
        return bbs;
    }
}
