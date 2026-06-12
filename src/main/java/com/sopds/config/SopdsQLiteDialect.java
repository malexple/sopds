package com.sopds.config;

import org.hibernate.community.dialect.SQLiteDialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

public class SopdsQLiteDialect extends SQLiteDialect {

    public SopdsQLiteDialect(DialectResolutionInfo info) {
        super(info);
    }

    /**
     * SQLite требует LIMIT ? OFFSET ?, а не OFFSET ? LIMIT ?
     * LimitOffsetLimitHandler генерирует именно правильный порядок.
     */
    @Override
    public LimitHandler getLimitHandler() {
        return LimitOffsetLimitHandler.INSTANCE;
    }

    @Override
    public boolean equivalentTypes(int typeCode1, int typeCode2) {
        if (matches(typeCode1, typeCode2, SqlTypes.BIGINT, SqlTypes.INTEGER))   return true;
        if (matches(typeCode1, typeCode2, SqlTypes.BOOLEAN, SqlTypes.INTEGER))  return true;
        if (matches(typeCode1, typeCode2, SqlTypes.TIMESTAMP, SqlTypes.VARCHAR)) return true;
        if (matches(typeCode1, typeCode2, SqlTypes.TIMESTAMP, SqlTypes.TIMESTAMP_WITH_TIMEZONE)) return true;
        return super.equivalentTypes(typeCode1, typeCode2);
    }

    @Override
    public JdbcType resolveSqlTypeDescriptor(
            String columnTypeName,
            int jdbcTypeCode,
            int precision,
            int scale,
            JdbcTypeRegistry jdbcTypeRegistry) {
        if ("datetime".equalsIgnoreCase(columnTypeName)) {
            return jdbcTypeRegistry.getDescriptor(SqlTypes.TIMESTAMP);
        }
        return super.resolveSqlTypeDescriptor(columnTypeName, jdbcTypeCode, precision, scale, jdbcTypeRegistry);
    }

    private boolean matches(int a, int b, int x, int y) {
        return (a == x && b == y) || (a == y && b == x);
    }
}