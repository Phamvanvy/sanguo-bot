package peony.service.read;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import peony.game.Player;

public class BookUserType implements UserType{
	
	private static final int[] SQL_TYPES = { Hibernate.BINARY.sqlType() };

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return ((Books) value).clone();
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
		byte[] bytes = resultSet.getBytes(names[0]);
		if (bytes == null)
			return new Books((Player)owner);
		else {
			return Books.fromDBBytes(bytes, (Player)owner);
		}
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null)
			statement.setNull(index, Hibernate.BINARY.sqlType());
		else {
			statement.setBytes(index, ((Books)value).toDBBytes());
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return target;
	}

	@SuppressWarnings("unchecked")
	public Class returnedClass() {
		return Books.class;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
