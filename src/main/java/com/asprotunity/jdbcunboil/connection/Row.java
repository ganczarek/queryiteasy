package com.asprotunity.jdbcunboil.connection;


public interface Row {
    String getString(String columnName);
    Integer getInteger(String columnName);
    Double getDouble(String columnName);
    Float getFloat(String columnName);
    Byte getByte(String columnName);
}
