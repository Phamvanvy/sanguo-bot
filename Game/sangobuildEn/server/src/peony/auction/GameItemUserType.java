package peony.auction;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import peony.game.GameItem;
import peony.game.ItemUtil;

public class GameItemUserType implements UserType {

	private static final int[] SQL_TYPES = { Hibernate.BINARY.sqlType() };

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		if (value == null)
			return null;
		return ((GameItem) value).clone();
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
		return false;
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
			throws HibernateException, SQLException {
		byte[] bytes = resultSet.getBytes(names[0]);
		if (bytes == null)
			return null;
		int version = bytes[0];
		byte[] bytes1 = new byte[bytes.length-1];
		System.arraycopy(bytes, 1, bytes1, 0, bytes1.length);
		return ItemUtil.getGameItemFromDB(bytes1,version);
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null)
			statement.setNull(index, Hibernate.BINARY.sqlType());
		else {
			byte[] bytes = ItemUtil.getGameItemDBBytes((GameItem)value);
			byte[] bytes1 = new byte[bytes.length+1];
			bytes1[0] = ItemUtil.VERSION;
			System.arraycopy(bytes, 0, bytes1, 1, bytes.length);
			statement.setBytes(index, bytes1);
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return target;
	}

	public Class returnedClass() {
		return GameItem.class;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
