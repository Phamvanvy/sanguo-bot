package peony.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "property")
public class Property {
	
	@Id
	@Column(name="id")
	public int id;
	
	
	@Column(name="pool")
	@Type(type="peony.game.PropertyPoolType")
	public PropertyPool pool;
	
	public Property(){
		
	}
	
	public Property(int id){
		this.id = id;
		this.pool = new PropertyPool();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PropertyPool getPool() {
		return pool;
	}

	public void setPool(PropertyPool pool) {
		this.pool = pool;
	}
	
}
