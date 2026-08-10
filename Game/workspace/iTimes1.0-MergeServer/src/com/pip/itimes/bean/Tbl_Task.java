package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_TaskDao;

public class Tbl_Task extends BaseTable{
    /*
    CREATE TABLE `tbl_task` (
      `id` int(11) NOT NULL DEFAULT '0',
      `current` blob,
      `finished` blob,
      `savedata` blob,
      PRIMARY KEY (`id`),
      UNIQUE KEY `id` (`id`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;
    */
    private int id;
    private byte[] current;
    private byte[] finished;
    private byte[] savedata;

    @Override
    public String getColumnNames(){
        return Tbl_TaskDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(current));
        sb.append(", ");
        sb.append(Tools.toSqlString(finished));
        sb.append(", ");
        sb.append(Tools.toSqlString(savedata));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //¥¶¿Ìid
    	id = mergeData.procPlayerId(id);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public byte[] getCurrent(){
        return current;
    }

    public void setCurrent(byte[] current){
        this.current = current;
    }

    public byte[] getFinished(){
        return finished;
    }

    public void setFinished(byte[] finished){
        this.finished = finished;
    }

    public byte[] getSavedata(){
        return savedata;
    }

    public void setSavedata(byte[] savedata){
        this.savedata = savedata;
    }
}
