package com.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/banking_db";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password"; // TODO: change it

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL 8 driver
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Add it to classpath.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
