package io.jpower.sgf.common.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.TagEnum;
import io.jpower.sgf.enumtype.Tag;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * javaType: {@link Tag}和{@link TagEnum} <-> jdbcType: int
 *
 * @param <E>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class TagEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private Class<E> type;

    public TagEnumTypeHandler(Class<E> type) {
        this.type = type;

        if (!type.isEnum()) {
            throw new IllegalArgumentException("The class is not Enum: " + type);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        int tag = EnumUtils.tagOf(parameter);
        ps.setInt(i, tag);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int tag = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return parseTagEnum(tag);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int tag = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return parseTagEnum(tag);
        }
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int tag = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return parseTagEnum(tag);
        }
    }

    private E parseTagEnum(int Tag) {
        return EnumUtils.valueOf(type, Tag);
    }

}
