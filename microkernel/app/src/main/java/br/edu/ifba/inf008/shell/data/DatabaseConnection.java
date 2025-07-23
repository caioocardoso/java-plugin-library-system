package br.edu.ifba.inf008.shell.data;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mariadb://127.0.0.1:3307/bookstore";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MariaDB JDBC Driver not found.", e);
            }
        }
        return connection;
    }
}
