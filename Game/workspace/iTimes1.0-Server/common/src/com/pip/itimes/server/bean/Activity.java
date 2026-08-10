package com.pip.itimes.server.bean;

import java.util.Date;

import com.pip.itimes.server.util.PropertyPool;

/**
 * 消费活动表
 * @author hchen
 *
 */
public class Activity implements java.io.Serializable {
	/**
	 * 消费活动ID
	 */
	private int id;
	
	/**
	 * 消费活动名称
	 */
	private String name;
	
	/**
	 * 开始时间
	 */
	private Date begintime;
	
	/**
	 * 结束时间
	 */
	private Date endtime;
	
	/**
	 * 是否有效
	 */
	private boolean valid;
	
	/**
	 * 是否开启
	 */
	private boolean enable;
	
	/**
     * 参数池
     */
    private PropertyPool pool;
    
    public Activity () {
    	
    }
    
    public Activity (Date begintime, Date endtime, String name) {
    	this.begintime = begintime;
    	this.endtime = endtime;
    	this.name = name;
        this.valid = true;
        this.enable = false;
    }
	
	public void setId (int id) {
		this.id = id;
	}
	public int getId () {
		return id;
	}
	
	public void setName (String name) {
		this.name = name; 
	}
	public String getName () {
		return name;
	}
	
	public void setBegintime (Date begintime) {
		this.begintime = begintime;
	}
	public Date getBegintime () {
		return begintime;
	}
	
	public void setEndtime (Date endtime) {
		this.endtime = endtime;
	}
	public Date getEndtime () {
		return endtime;
	}
	
	public void setValid (boolean valid) {
		this.valid = valid;
	}
	public boolean getValid () {
		return valid;
	}
	
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	public boolean getEnable () {
		return enable;
	}

	public PropertyPool getPool() {
		return pool;
	}

	public void setPool(PropertyPool pool) {
		this.pool = pool;
	}
}
