package io.jpower.sgf.common.mybatis;

import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;
import io.jpower.sgf.enumtype.Tag;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * javaType: {@link Tag}和{@link IntEnum} <-> jdbcType: int
 *
 * @param <E>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IdEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private Class<E> type;

    public IdEnumTypeHandler(Class<E> type) {
        this.type = type;

        if (!type.isEnum()) {
            throw new IllegalArgumentException("The class is not Enum: " + type);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        int id = EnumUtils.idOf(parameter);
        ps.setInt(i, id);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int id = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return parseIntEnum(id);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return parseIntEnum(id);
        }
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int id = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return parseIntEnum(id);
        }
    }

    private E parseIntEnum(int id) {
        return EnumUtils.valueOf(type, id);
    }

}
