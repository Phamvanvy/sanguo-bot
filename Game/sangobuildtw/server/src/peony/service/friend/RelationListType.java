package peony.service.friend;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * 用于把数据库中的字符串映射为RelationList对象的Hibernate数据类型。
 * @author lighthu
 */
public class RelationListType implements UserType {
	private static final int[] SQL_TYPES = { Hibernate.STRING.sqlType() };

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		if(value==null)
			return null;
		return ((RelationList)value).clone();
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
		String str = resultSet.getString(names[0]);
		if (str == null)
			return null;
		RelationList ret = new RelationList();
		ret.parse(str);
		return ret;
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null)
			statement.setNull(index, SQL_TYPES[0]);
		else {
			statement.setString(index, value.toString());
		}
	}

	public Object replace(Object original, Object target, Object owner) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public Class returnedClass() {
		return RelationList.class;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}
}
