package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.ArenaRecord;
import com.pip.itimes.server.bean.ArenaRecordWorldWar;

public class ArenaRecordDao extends BaseDao{

    public ArenaRecordDao() {
    }
    public void addArenaRecord(ArenaRecord arenarecord) throws DataAccessException {
        makePersistent(arenarecord);
    }
    
    public void addArenaRecordWorldWar(ArenaRecordWorldWar arenarecordworldwar) throws DataAccessException {
        makePersistent(arenarecordworldwar);
    }
}
