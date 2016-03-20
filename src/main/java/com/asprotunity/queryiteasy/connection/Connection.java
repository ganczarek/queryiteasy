package com.asprotunity.queryiteasy.connection;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Connection {
    void update(String sql, InputParameter... parameters);

    void update(String sql, List<Batch> batches);

    <ResultType> ResultType select(String sql, Function<Stream<ResultSet>, ResultType> rowProcessor,
                                   InputParameter... parameters);

    void call(String sql, Parameter...parameters);
}
