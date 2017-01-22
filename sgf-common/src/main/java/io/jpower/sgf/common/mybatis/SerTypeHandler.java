package io.jpower.sgf.common.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import io.jpower.sgf.ser.Ser;

/**
 * javaType: Ser Object <-> jdbcType: bytes
 *
 * @author zheng.sun
 */
public class SerTypeHandler<T> extends BaseTypeHandler<T> {

    private Class<T> type;

    private Ser ser = Ser.ins();

    public SerTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, ser.serialize(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] data = rs.getBytes(columnName);
        if (data == null) {
            return null;
        } else {
            return ser.deserialize(data, type);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] data = rs.getBytes(columnIndex);
        if (data == null) {
            return null;
        } else {
            return ser.deserialize(data, type);
        }
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] data = cs.getBytes(columnIndex);
        if (data == null) {
            return null;
        } else {
            return ser.deserialize(data, type);
        }
    }

}
