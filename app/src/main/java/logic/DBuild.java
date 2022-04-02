/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author zlotn
 */
public class DBuild {
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection("xxx");
        }
        return conn;
    }
}
