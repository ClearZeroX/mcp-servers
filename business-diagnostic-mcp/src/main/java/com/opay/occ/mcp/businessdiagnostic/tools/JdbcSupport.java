package com.opay.occ.mcp.businessdiagnostic.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Read-only JDBC connection factory shared by diagnostic queries.
 */
public final class JdbcSupport {

    private JdbcSupport() {
    }

    /** Opens a MySQL connection and forces the session into read-only mode. */
    public static Connection openReadOnly(String host, int port, String database,
                                          String user, String password) throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
                + "&connectTimeout=3000&socketTimeout=5000";
        Connection connection = DriverManager.getConnection(url, user, password);
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET SESSION TRANSACTION READ ONLY");
        }
        connection.setReadOnly(true);
        return connection;
    }
}
