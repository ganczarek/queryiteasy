package com.asprotunity.queryiteasy.acceptance_tests;


import com.asprotunity.queryiteasy.TransactionExecutor;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.asprotunity.queryiteasy.connection.PositionalBinder.bind;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ValuesAreInsertedInDBCorrectlyTest {


    private JDBCDataSource dataSource;

    @Before
    public void setUp() {
        dataSource = new JDBCDataSource();
        dataSource.setDatabase("jdbc:hsqldb:mem:testdb");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }

    @After
    public void tearDown() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("SHUTDOWN");
        statement.close();
        connection.commit();
        connection.close();
    }


    @Test
    public void inserts_with_no_bind_values() throws SQLException {

        TransactionExecutor executor = new TransactionExecutor(dataSource);

        executor.executeUpdate(connection -> {
            connection.executeUpdate("CREATE TABLE testtable (index INTEGER NOT NULL)");
            connection.executeUpdate("INSERT INTO testtable (index) VALUES (10)");
        });

        List<Integer> expectedValues = query("SELECT index FROM testtable", rs -> rs.getInt("index"));
        assertThat(expectedValues.size(), is(1));
        assertThat(expectedValues.get(0), is(10));
    }


    @Test
    public void inserts_with_some_bind_values() throws SQLException {

        TransactionExecutor executor = new TransactionExecutor(dataSource);

        executor.executeUpdate(connection -> {
            connection.executeUpdate("CREATE TABLE testtable (index INTEGER NOT NULL, name VARCHAR(20) NOT NULL)");
            connection.executeUpdate("INSERT INTO testtable (index, name) VALUES (?, ?)",
                    bind(10),
                    bind("aname"));
        });

        List<TestTableFields> expectedValues = query("SELECT * FROM testtable",
                rs -> new TestTableFields(rs.getInt("index"), rs.getString("name")));
        assertThat(expectedValues.size(), is(1));
        assertThat(expectedValues.get(0).index, is(10));
        assertThat(expectedValues.get(0).name, is("aname"));
    }


    @Test
    public void queries_with_no_bind_values() throws SQLException {

        TransactionExecutor executor = new TransactionExecutor(dataSource);

        executor.executeUpdate(connection -> {
            connection.executeUpdate("CREATE TABLE testtable (index INTEGER NOT NULL)");
            connection.executeUpdate("INSERT INTO testtable (index) VALUES (10)");
            connection.executeUpdate("INSERT INTO testtable (index) VALUES (11)");
        });

        List<Integer> result = executor.executeQuery(connection ->
                        connection.executeQuery("SELECT index FROM testtable ORDER BY index ASC", row -> row.getInt("index"))
        );


        assertThat(result.size(), is(2));
        assertThat(result.get(0), is(10));
        assertThat(result.get(1), is(11));
    }

    @Test
    public void queries_with_bind_values() throws SQLException {

        TransactionExecutor executor = new TransactionExecutor(dataSource);

        executor.executeUpdate(connection -> {
            connection.executeUpdate("CREATE TABLE testtable (index INTEGER NOT NULL, name VARCHAR(20) NOT NULL)");

            for (int index = 10; index < 12; ++index) {
                connection.executeUpdate("INSERT INTO testtable (index, name) VALUES (?, ?)",
                        bind(index),
                        bind("aname" + index));
            }
        });

        List<TestTableFields> result = executor.executeQuery(connection ->
                        connection.executeQuery("SELECT index, name FROM testtable WHERE index = ? AND name = ?",
                                row -> new TestTableFields(row.getInt("index"), row.getString("name")),
                                bind(10),
                                bind("aname10"))
        );


        assertThat(result.size(), is(1));
        assertThat(result.get(0).index, is(10));
        assertThat(result.get(0).name, is("aname10"));
    }

    private <ResultType> List<ResultType> query(String sql, ResultSetMapper<ResultType> mapper) throws SQLException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            ArrayList<ResultType> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapper.apply(rs));
            }
            connection.commit();
            return result;
        }
    }


    static class TestTableFields {
        public final int index;
        public final String name;

        public TestTableFields(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    @FunctionalInterface
    interface ResultSetMapper<T> {
        T apply(ResultSet rs) throws SQLException;
    }

}
