package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Question;

public class Tbl_QuestionDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_QuestionDao.class);

    public static final String SQL_PARA = "id, questionid, succeed, fail";

    public Tbl_QuestionDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Question> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Question.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Question.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Question> list = new ArrayList<Tbl_Question>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Question question = new Tbl_Question();

                question.setId(rs.getInt(1));
                question.setQuestionid(rs.getInt(2));
                question.setSucceed(rs.getInt(3));
                question.setFail(rs.getInt(4));

                list.add(question);
            }
        }catch(Exception e){
            log.error(e, e);
        }finally{
            try{
                statement.close();

                if(rs != null){
                    rs.close();
                }
            }catch(Exception e){
            }
        }

        return list;
    }
}
