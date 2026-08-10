package com.pip.itimes.server.world;

import com.pip.itimes.server.dao.LeaveMessageDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.bean.LeaveMessage;
import java.util.List;

public class LeaveMessageService {

    private LeaveMessageDao dao;

    public LeaveMessageService(LeaveMessageDao dao) {
        this.dao = dao;
    }

    public void addLeaveMessage(LeaveMessage lm) throws DataAccessException {
        dao.addLeaveMessage(lm);
    }

    public List getLeaveMessageList(int playerId, int begin, int maxCount) throws
            DataAccessException {
        return dao.getLeaveMessageList(playerId,begin,maxCount);
    }

    public LeaveMessage getLeaveMessage(int id) throws DataAccessException {
        return dao.getLeaveMessage(id);
    }



    public LeaveMessage deleteMessage(int id) throws DataAccessException{
        return dao.deleteMessage(id);
    }
}
