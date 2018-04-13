package io.jpower.sgf.common.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

/**
 * javaType: Long <-> jdbcType: Timestamp
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class LongTimestampTypeHandler extends BaseTypeHandler<Long> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setTimestamp(i, new Timestamp(parameter));
    }

    @Override
    public Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.getTime();
        }
    }

    @Override
    public Long getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.getTime();
        }
    }

    @Override
    public Long getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.getTime();
        }
    }

}
