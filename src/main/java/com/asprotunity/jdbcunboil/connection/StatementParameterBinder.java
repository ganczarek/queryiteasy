package com.asprotunity.jdbcunboil.connection;

public interface StatementParameterBinder {
    void bind(String value);

    void bind(Integer value);

    void bind(Double value);
}
