package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Blog;
import java.util.List;

public class BlogDao extends BaseDao{

    public BlogDao() {
    }
    public void addBlog(Blog blog) throws DataAccessException {
        makePersistent(blog);
    }

    public List getBlogList(int playerId, int begin, int maxCount) throws
            DataAccessException {
        return getLimitedList("from Blog b where b.playerId=" + playerId +" order by b.createTime desc",
                              begin, maxCount);
    }

    public Blog getBlog(int id) throws DataAccessException {
        return (Blog) getObject(Blog.class, new Integer(id));
    }

    public int getBlogCount(int playerId) throws DataAccessException {
        return getCount("from Blog b where b.playerId=" + playerId);
    }

    public Blog deleteBlog(int id) throws DataAccessException{
        Blog bbs = (Blog)getObject(Blog.class,new Integer(id));
        if(bbs!=null)
            makeTransient(bbs);
        return bbs;
    }
    public List getPreviousBlob(int playerId) throws DataAccessException{
       return  getLimitedList("from Blog b where b.playerId=" + playerId +" order by b.createTime desc",  1, 1);
    }

}
