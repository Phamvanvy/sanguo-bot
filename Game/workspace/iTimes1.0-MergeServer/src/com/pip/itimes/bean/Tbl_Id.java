package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_IdDao;

public class Tbl_Id extends BaseTable{
    /*
    CREATE TABLE `tbl_id` (
      `usedid` int(11) NOT NULL DEFAULT '0',
      `id` int(11) NOT NULL AUTO_INCREMENT,
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
    */
    private int usedid;
    private int id;

    @Override
    public String getColumnNames(){
        return Tbl_IdDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(usedid));
        sb.append(", ");
        sb.append(Tools.toSqlString(id));

        return sb.toString();
    }
    
    @Override
    public String toSqlString(){
        if(status == STATUS_UPDATE){
            StringBuffer sb = new StringBuffer();

            sb.append("UPDATE tbl_id set usedid = ");
            sb.append(Tools.toSqlString(usedid));
            sb.append(" WHERE id = ");
            sb.append(Tools.toSqlString(id));
            sb.append(';');

            return sb.toString();
        }else{
            return super.toSqlString();
        }
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        status = STATUS_DROP;
    }

    public int getUsedid(){
        return usedid;
    }

    public void setUsedid(int usedid){
        this.usedid = usedid;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }
    
    public void rebuild(int id, int usedid){
        this.id = id;
        this.usedid = usedid;
        status = STATUS_UPDATE;
    }
}
