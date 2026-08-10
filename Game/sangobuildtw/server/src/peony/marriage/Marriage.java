package peony.marriage;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="marriage")
public class Marriage {
	
	private int id; // 主键
	
	private int manId; // 新郎
	
	private int womanId; // 新娘
	
	private Date createTime; // 结婚时间
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name="manid",length=11,nullable=false)
	public int getManId() {
		return manId;
	}
	public void setManId(int manId) {
		this.manId = manId;
	}
	
	@Column(name="womanid",length=11,nullable=false)
	public int getWomanId() {
		return womanId;
	}
	public void setWomanId(int womanId) {
		this.womanId = womanId;
	}
	
	@Column(name="createtime",nullable=false)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Marriage other = (Marriage) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
