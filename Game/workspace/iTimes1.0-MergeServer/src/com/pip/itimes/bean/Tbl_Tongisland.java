package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_TongislandDao;

public class Tbl_Tongisland extends BaseTable{
    /*
    CREATE TABLE `tbl_tongisland` (
      `id` int(11) NOT NULL DEFAULT '0',
      `tongid` int(11) NOT NULL DEFAULT '0',
      `begintime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `endtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
    */
    private int id;
    private int tongid;
    private Date begintime;
    private Date endtime;

    @Override
    public String getColumnNames(){
        return Tbl_TongislandDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongid));
        sb.append(", ");
        sb.append(Tools.toSqlString(begintime));
        sb.append(", ");
        sb.append(Tools.toSqlString(endtime));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        status = STATUS_DROP;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getTongid(){
        return tongid;
    }

    public void setTongid(int tongid){
        this.tongid = tongid;
    }

    public Date getBegintime(){
        return begintime;
    }

    public void setBegintime(Date begintime){
        this.begintime = begintime;
    }

    public Date getEndtime(){
        return endtime;
    }

    public void setEndtime(Date endtime){
        this.endtime = endtime;
    }
}
