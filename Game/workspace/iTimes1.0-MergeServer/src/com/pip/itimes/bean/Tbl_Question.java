package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_QuestionDao;

public class Tbl_Question extends BaseTable{
    /*
    CREATE TABLE `tbl_question` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `questionid` int(11) DEFAULT '0',
      `succeed` int(11) DEFAULT '0',
      `fail` int(11) DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
    */
    private int id;
    private int questionid;
    private int succeed;
    private int fail;

    @Override
    public String getColumnNames(){
        return Tbl_QuestionDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(questionid));
        sb.append(", ");
        sb.append(Tools.toSqlString(succeed));
        sb.append(", ");
        sb.append(Tools.toSqlString(fail));

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

    public int getQuestionid(){
        return questionid;
    }

    public void setQuestionid(int questionid){
        this.questionid = questionid;
    }

    public int getSucceed(){
        return succeed;
    }

    public void setSucceed(int succeed){
        this.succeed = succeed;
    }

    public int getFail(){
        return fail;
    }

    public void setFail(int fail){
        this.fail = fail;
    }
}
