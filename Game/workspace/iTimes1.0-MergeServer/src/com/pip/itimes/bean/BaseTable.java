package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;

public abstract class BaseTable{
    protected byte status = STATUS_INSERT;

    public static final byte STATUS_INSERT = 0;
    public static final byte STATUS_UPDATE = 1;
    public static final byte STATUS_DROP = 2;
    public static final byte STATUS_DELETE_OLD = 3;
    public static final byte STATUS_DELETE = 4;

    public String toSqlString(){
        switch(status){
            case STATUS_INSERT: {
                StringBuffer sb = new StringBuffer();

                sb.append("INSERT INTO `");
                sb.append(getClass().getSimpleName().toLowerCase());
                sb.append('`');
                sb.append(" (");
                sb.append(getColumnNames());
                sb.append(")");
                sb.append(" VALUES (");
                sb.append(toString());
                sb.append(");");

                return sb.toString();
            }
            case STATUS_DELETE: {
                StringBuffer sb = new StringBuffer();

                sb.append("DELETE FROM `");
                sb.append(getClass().getSimpleName().toLowerCase());
                sb.append('`');
                sb.append(" WHERE id = ");
                sb.append(Tools.toSqlString(getId()));
                sb.append(";");

                return sb.toString();
            }
            case STATUS_UPDATE: {
                StringBuffer sb = new StringBuffer();

                sb.append("UPDATE `");
                sb.append(getClass().getSimpleName().toLowerCase());
                sb.append('`');
                sb.append(" SET ");
                sb.append(toString());
                sb.append(" WHERE id = ");
                sb.append(Tools.toSqlString(getId()));
                sb.append(";");

                return sb.toString();
            }
        }

        return "";
    }

    public abstract String getColumnNames();
    
    public void dropRecord(){
        status = STATUS_DROP;
    }
    
    public void setStatus(byte status){
    	this.status = status;
    }

    public abstract void process(MergeData mergeData, ServerConfig serverConfig);

    public abstract int getId();
}
