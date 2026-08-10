package peony.game;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import peony.game.changed.ChangedItem;

public class LockableIntPropertyUserType implements UserType {

	private static final int[] SQL_TYPES = { Hibernate.INTEGER.sqlType() };

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return ((LockableIntProperty) value).clone();
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y)
			return true;
		if (x == null || y == null)
			return false;
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return true;
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
			throws HibernateException, SQLException {
		int value = resultSet.getInt(names[0]);
		if (names[0].startsWith("money")) {
			return new LockableIntProperty(ChangedItem.MONEY, value);
		} else if(names[0].startsWith("honor")){
			return new LockableIntProperty(ChangedItem.HONOR,value);
		} else if(names[0].startsWith("credit")){
			return new LockableIntProperty(ChangedItem.CREDIT,value);
		}
		return null;
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException, SQLException {
		LockableIntProperty pro = (LockableIntProperty)value;
		statement.setInt(index, pro.value);
	}

	public Object replace(Object original, Object target, Object owner) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public Class returnedClass() {
		return LockableIntProperty.class;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
