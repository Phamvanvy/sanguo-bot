package com.pip.itimes.server.world.activityService.activity;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import com.pip.itimes.server.bean.Activity;
import com.pip.itimes.server.util.PropertyPool;

public class ActivityData implements Service {
	protected static Logger log = Logger.getLogger(Activity.class);
	
	private Calendar calendar = Calendar.getInstance();
	
	private Activity activity;
	
	private static final String IMPL_CLASS = "implClass";
	private static final String CONFIG_DATA = "configData";
	/*private static final String TIME_PERIODS = "timePeriods";*/
	
	// 实现类名称
	protected String implClass;
	// 配置信息
	protected String configData;
	// 实现类
	protected IActivityImpl impl;
	/**
	 * 一天中生效的时间段。单位是时，分，每4个数字表示起始时间（小时，分钟）和结束时间（小时，分钟）。
	 * null表示不限制。前包后不包
	 */
	protected int[] timePeriods;
	
	/**
	 * 参数池
	 */
	protected PropertyPool pool = new PropertyPool();
	
	public ActivityData (Activity activity) {
		this.activity = activity;
		initPool();
	}
	
	private void initPool () {
		if (activity.getPool() == null) {
			setImplClass(pool.getString(IMPL_CLASS));
			setConfigData(pool.getString(CONFIG_DATA));
			/*setTimePeriods(parse(pool.getString(TIME_PERIODS)));*/
		} else {
			setImplClass(activity.getPool().getString(IMPL_CLASS));
			setConfigData(activity.getPool().getString(CONFIG_DATA));
		/*	setTimePeriods(parse(activity.getPool().getString(TIME_PERIODS)));*/
			pool = activity.getPool().clone();
		}
	}
	
	public Activity getActivity () {
		return activity;
	}
	
	public void setValid (boolean valid) {
		activity.setValid(valid);
	}
	
	public boolean getValid () {
		return activity.getValid();
	}
	
	public void reset () {
		resetPool();
	}
	
	protected void resetPool () {
		resetImplClass();
		resetConfigData();
		/*resetTimePeriods();*/
		activity.setPool(pool);
	}
	
	protected void resetImplClass () {
		pool.setString(IMPL_CLASS, getImplClass());
	}
	
	protected void resetConfigData () {
		pool.setString(CONFIG_DATA, getConfigData());
	}
	
	/*protected void resetTimePeriods () {
		if (getTimePeriods() != null) {
			pool.setString(TIME_PERIODS, translateTimeString(getTimePeriods()));
		}
	}*/
	
	public void setPool (PropertyPool poolText) {
		activity.setPool(poolText);
    }
	
	public PropertyPool getPool () {
    	return pool;
    }
	
	public int getId() {
		return activity.getId();
	}

	public void setId(int id) {
		activity.setId(id);
	}

	public String getName() {
		return activity.getName();
	}

	public void setName(String name) {
		activity.setName(name);
	}
	
	public void setBeginTime (Date beginTime) {
		activity.setBegintime(beginTime);
	}
	
	public Date getBeginTime () {
		return activity.getBegintime();
	}
	
	public void setEndTime (Date endTime) {
		activity.setEndtime(endTime);
	}
	
	public Date getEndTime () {
		return activity.getEndtime();
	}

	public String getImplClass() {
		return implClass;
	}

	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

	public String getConfigData() {
		return configData;
	}

	public void setConfigData(String configData) {
		this.configData = configData;
	}
	
	/*public void setTimePeriods (int[] timePeriods) {
		this.timePeriods = timePeriods;
	}
	
	public int[] getTimePeriods () {
		return timePeriods;
	}*/
	
	/**
	 * 将字符串xx,xx,xx,格式转换成int[]
	 * @param str
	 * @return
	 *//*
	public int[] parse (String str) {
		if (str != null && str.length() > 0) {
			String[] tmp = str.split(",");
			if (tmp.length > 0) {
				int[] ret = new int[tmp.length];
				for (int i = 0; i < tmp.length; i++) {
					ret[i] = Integer.parseInt(tmp[i]);
				}
				return ret;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}*/
	
	/**
	 * 将int[]时间格式转换成String：xx,xx,xx,xx,xx
	 * @param parameter
	 * @return
	 *//*
	public String translateTimeString (int[] parameter) {
		if (parameter != null && parameter.length > 0) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < parameter.length; i ++) {
				buf.append(parameter[i] + ',');
			}
			return buf.toString();
		} else {
			return null;
		}
	}*/
	
	/**
	 * 取得实现类。
	 * @return
	 */
	public IActivityImpl getImpl() {
		if (impl == null) {
			try {
				Class cls = Class.forName("com.pip.itimes.server.world.activityService.activity." + implClass);
				Constructor c = cls.getConstructor(ActivityData.class);
				impl = (IActivityImpl)c.newInstance(this);
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		return impl;
	}
	
	/**
	 * 判断当前活动是否正在进行。
	 */
	public boolean isEnabled() {
		return activity.getEnable();
	}
	
	public void setEnabled (boolean enabled) {
		activity.setEnable(enabled);
	}
	
	/**
	 * 启动服务。
	 * @throws Exception
	 */
	public void startup() throws Exception {
		activity.setEnable(true);
		getImpl().startup();
	}
	
	/**
	 * 关闭服务。
	 */
	public void shutdown() {
		activity.setEnable(false);
		getImpl().shutdown();
	}
	
	/**
	 * 把当前活动状态保存起来。用于服务器shutdown的时候保存状态。
	 */
	public void save() {
		getImpl().save();
	}
	
	/**
	 * 载入先前保存的状态。用于服务器startup的时候载入上次状态。
	 */
	public void load() {
		getImpl().load();
	}
	
	/**
	 * 清除所有和此活动有关的存储数据。
	 */
	public void clear() {
		getImpl().clear();
	}
	
	/**
	 * 判断当前时间是否在活动有效时间段内。
	 *//*
	public boolean in() {
		long now = System.currentTimeMillis();
		if (now < getBeginTime().getTime() || now >= getEndTime().getTime()) {
			return false;
		}
		calendar.setTimeInMillis(System.currentTimeMillis());
		if (timePeriods != null) {
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			boolean match = false;
			for (int i = 0; i < timePeriods.length; i += 4) {
				if ((hour >= timePeriods[i] && minute >= timePeriods[i + 1])
						&& (hour < timePeriods[i + 2] && minute < timePeriods[i + 3])) {
					return true;
				}
			}
			if (!match) {
				return false;
			}
		}
		return true;
	}*/
	
	public void process (long time) {
		/*if (getValid()) {
			boolean in = in();
			
			// 活动已开始，判断此活动是否过期
			if (isEnabled() && !in) {
				try {
					shutdown();
				} catch (Exception e) {
					log.error(e, e);
				}
			}
			
			// 活动未开始，判断此活动是否开启
			if (!isEnabled() && in) {
				try {
					startup();
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		} else {
			// 如果活动被禁用了，则关闭
			if (isEnabled()) {
				try {
					shutdown();
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}*/
		
		if (getValid()) {
			if (isEnabled()) {	// 活动已开始，判断此活动是否过期
				if (time >= getEndTime().getTime()) {
					try {
						shutdown();
					} catch (Exception e) {
						log.error(e, e);
					}
				}
			} else {	// 活动未开始，判断此活动是否开启
				if (time >= getBeginTime().getTime() && time < getEndTime().getTime()) {
					try {
						startup();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			// 如果活动被禁用了，则关闭之
			if (isEnabled()) {
				try {
					shutdown();
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
    }
	
}
