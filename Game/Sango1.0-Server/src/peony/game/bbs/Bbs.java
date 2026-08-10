package peony.game.bbs;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@Table(name="bbs")
public class Bbs {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	@Column(name="priv")
	public int order;
	@Column(name="title")
	public String title;
	@Column(name="message")
	public String message;
	@Column(name="createtime")
	public Date createTime;
	
	public Bbs(){
		
	}
	
	
}
