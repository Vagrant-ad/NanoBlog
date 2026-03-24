package com.vagrant.nanoblog.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * 适配MySQL DATETIME类型的LocalDateTime处理器
 * 解决Druid getObject()不支持问题
 */
@MappedTypes(LocalDateTime.class)  // 映射Java的LocalDateTime类型
@MappedJdbcTypes({JdbcType.DATE, JdbcType.TIME, JdbcType.TIMESTAMP})  // 兼容DATETIME/TIMESTAMP
public class CustomLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    // 写入数据库：LocalDateTime → Timestamp（兼容DATETIME字段）
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    // 读取数据库（按列名）：DATETIME/TIMESTAMP → Timestamp → LocalDateTime
    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 关键：用getTimestamp()读取DATETIME字段，避开getObject()
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    // 读取数据库（按列索引）
    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    // 存储过程用
    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}