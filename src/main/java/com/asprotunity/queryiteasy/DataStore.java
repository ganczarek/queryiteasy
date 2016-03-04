package com.asprotunity.queryiteasy;

import com.asprotunity.queryiteasy.connection.Connection;
import com.asprotunity.queryiteasy.exception.InvalidArgumentException;
import com.asprotunity.queryiteasy.internal.connection.RuntimeSQLExceptionWrapper;
import com.asprotunity.queryiteasy.internal.connection.WrappedJDBCConnection;

import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataStore {

    private DataSource dataSource;

    public DataStore(DataSource dataSource) {
        if (dataSource == null) {
            throw new InvalidArgumentException("dataSource cannot be null");
        }
        this.dataSource = dataSource;
    }

    public void execute(Consumer<Connection> transaction) {
        RuntimeSQLExceptionWrapper.execute(() -> {
                    try (WrappedJDBCConnection connection = new WrappedJDBCConnection(dataSource.getConnection())) {
                        transaction.accept(connection);
                        connection.commit();
                    }
                }
        );
    }


    public <ResultType> ResultType executeWithResult(Function<Connection, ResultType> transaction) {
        return RuntimeSQLExceptionWrapper.executeAndReturnResult(() -> {
                    try (WrappedJDBCConnection connection = new WrappedJDBCConnection(dataSource.getConnection())) {
                        ResultType result = transaction.apply(connection);
                        connection.commit();
                        return result;
                    }
                }
        );
    }
}
