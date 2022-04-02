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
            conn = DriverManager.getConnection("jdbc:postgresql://ec2-34-247-151-118.eu-west-1.compute.amazonaws.com:5432/d1v1eetd9rgfsh?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory",
                    "mnwvocrgecchbs", "e0fbc54ffe89e6bb12b8ca3e8e6971143b97d97bded509b81afe492cfacbdaee");
        }
        return conn;
    }
}
