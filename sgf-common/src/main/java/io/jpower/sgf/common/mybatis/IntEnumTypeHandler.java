package io.jpower.sgf.common.mybatis;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * javaType: IntEnum <-> jdbcType: int
 *
 * @param <T>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IntEnumTypeHandler<T extends IntEnum> extends BaseTypeHandler<T> {

    private Class<T> type;

    private final Method findByIdMethod;

    private final T[] intEnums;

    public IntEnumTypeHandler(Class<T> type) {
        this.type = type;

        if (!type.isEnum()) {
            throw new IllegalArgumentException("The class is not Enum: " + type);
        }

        // 优先使用findById方法
        this.findByIdMethod = findFindByIdMethod(type);

        if (this.findByIdMethod == null) {
            // 没有findById，就用数组
            T[] enumConstants = (T[]) type.getEnumConstants();
            this.intEnums = EnumUtils.toArray(enumConstants);
        } else {
            this.intEnums = null;
        }
    }

    private Method findFindByIdMethod(Class<?> c) {
        try {
            return c.getMethod("findById", int.class);
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.getId());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int id = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return parseIntEnum(id);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return parseIntEnum(id);
        }
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int id = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return parseIntEnum(id);
        }
    }

    @SuppressWarnings("unchecked")
    private T parseIntEnum(int id) {
        T rst = null;
        if (this.findByIdMethod != null) {
            try {
                rst = (T) findByIdMethod.invoke(null, id);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Cannot convert " + id + " to " + type.getSimpleName() + " by IntEnum id.",
                        e);
            }
        } else {
            if (id >= 0 && id < intEnums.length) {
                rst = intEnums[id];
            }
        }
        return rst;
    }

}
