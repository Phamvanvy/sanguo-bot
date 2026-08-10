package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_AdminDao;

public class Tbl_Admin extends BaseTable{
    /*
    CREATE TABLE `tbl_admin` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `name` varchar(255) NOT NULL DEFAULT '',
      `password` varchar(255) NOT NULL DEFAULT '',
      `auth` mediumtext NOT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;
    */
    private int id;
    private String name;
    private String password;
    private String auth;

    @Override
    public String getColumnNames(){
        return Tbl_AdminDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(name));
        sb.append(", ");
        sb.append(Tools.toSqlString(password));
        sb.append(", ");
        sb.append(Tools.toSqlString(auth));

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

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getAuth(){
        return auth;
    }

    public void setAuth(String auth){
        this.auth = auth;
    }
}
