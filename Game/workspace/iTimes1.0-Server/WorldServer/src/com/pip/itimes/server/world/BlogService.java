package com.pip.itimes.server.world;

import java.util.Date;
import java.util.List;

import com.pip.itimes.server.bean.Blog;
import com.pip.itimes.server.dao.BlogDao;
import com.pip.itimes.server.dao.DataAccessException;

public class BlogService {

    private static BlogDao dao;

    public BlogService(BlogDao dao) {
        this.dao = dao;
    }

    public void addBlog(Blog blog) throws DataAccessException{
        dao.addBlog(blog);
    }

    public Blog deleteBlog(int id) throws DataAccessException{
        return dao.deleteBlog(id);
    }

    public Blog getBlog(int id) throws DataAccessException{
        return dao.getBlog(id);
    }

    public void saveBlog(Blog blog) throws DataAccessException{
        dao.makePersistent(blog);
    }

    public BlogResult getBbsList(int playerId, int pageSize, int pageNo) throws
            DataAccessException {
        int total = dao.getBlogCount(playerId);
        if (pageNo * pageSize >= total) {
            BlogResult ret = new BlogResult();
            ret.blogs = new Blog[0];
            ret.pageCount = 0;
            return ret;
        }
        int pageCount = total / pageSize;
        if (total % pageSize != 0)
            pageCount++;
        List l = dao.getBlogList(playerId, pageNo * pageSize, pageSize);
        BlogResult ret = new BlogResult();
        Blog[] blogs = new Blog[l.size()];
        l.toArray(blogs);
        ret.blogs = blogs;
        ret.pageCount = pageCount;
        return ret;
    }

    public static class BlogResult{
        public Blog[] blogs;
        public int pageCount;
    }
    public static Date getPreviousBlob(int playerId) throws DataAccessException{
    	List list= dao.getPreviousBlob(playerId);
    	if(list !=null && list.size() >0){
    		Blog blog = (Blog) list.get(0);
    		return blog.getCreateTime();    		
    	}else{
    		return null;
    	}
    }

}
