package peony.service.activity;

import java.lang.reflect.Constructor;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import peony.game.CycleListener;
import peony.service.Service;

/**
 * 定义一个推广活动。一个活动是一个临时性的服务，在一定时间范围内有效。
 * @author lighthu
 */
@Entity
@Table(name = "activity")
public class Activity implements Service {
	protected static Logger log = Logger.getLogger(Activity.class);
	
	// ID
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	protected int id;
	// 内部名称
	@Column(name="name",nullable=false)
	protected String name;
	// 标题
	@Column(name="title",nullable=false)
	protected String title;
	// 描述
	@Column(name="description",nullable=false)
	protected String description;
	// 时间安排
	@Column(name="schedule",nullable=false)
	@Type(type = "peony.service.activity.ActivityScheduleUserType")
	protected ActivitySchedule schedule;
	// 实现类名称
	@Column(name="implclass",nullable=false)
	protected String implClass;
	// 配置信息
	@Column(name="configdata",nullable=false)
	protected String configData;
	// 是否有效
	@Column(name="enabled",nullable=false)
	protected boolean enabled;
	// 是否玩家可见
	@Column(name="visible",nullable=false)
	protected boolean visible;

	// 是否已启动标志
	@Transient
	protected boolean active;
	// 实现类
	@Transient
	protected IActivityImpl impl;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ActivitySchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(ActivitySchedule schedule) {
		this.schedule = schedule;
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * 取得实现类。
	 * @return
	 */
	public IActivityImpl getImpl() {
		if (impl == null) {
			try {
				Class cls = Class.forName("peony.service.activity." + implClass);
				Constructor c = cls.getConstructor(Activity.class);
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
	public boolean isActive() {
		return active;
	}
	
	/**
	 * 启动服务。
	 * @throws Exception
	 */
	public void startup() throws Exception {
		active = true;
		getImpl().startup();
	}
	
	/**
	 * 关闭服务。
	 */
	public void shutdown() {
		active = false;
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
	 * 每cycle更新。
	 * @param diff
	 * @return
	 */
	public boolean update(int diff) {
		if (getImpl() instanceof CycleListener) {
			return ((CycleListener)getImpl()).update(diff);
		} else {
			return false;
		}
	}
}
